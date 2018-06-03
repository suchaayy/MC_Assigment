package com.example.sucharitharumesh.g7_a3.svmwrapperlibrary;

import com.example.sucharitharumesh.g7_a3.Values;

import java.util.List;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_problem;
import libsvm.svm_parameter;
import libsvm.svm;
/**
 * Created by Joel on 02-04-2017.
 */

public class SvmWrapper {


    public SvmWrapper()
    {}

    private static svm_parameter paramObject = new svm_parameter();

    public svm_model trainer(List<DataBean> inputData)throws Exception{
        svm_problem prob = problemPacker(inputData);
        svm_model modelTrained = svm.svm_train(prob,defineParamObject());
        return modelTrained;
    }

    public svm_problem problemPacker(List<DataBean> inputData){
        svm_problem prob = new svm_problem();
        // variables to input
        double nodeVals[][] = new double[inputData.size()][];
        int nodeIndices[][] = new int[inputData.size()][];
        int iterIdx = 0;

        // preparing double arrays
        for(DataBean iter:inputData){
            if(iter.getAccleromterData().size() > 0){
                nodeVals[iterIdx] = new double[iter.getAccleromterData().size()];
                nodeIndices[iterIdx] = new int[iter.getAccleromterData().size()];

                for (int i = 0; i < nodeVals.length; i++) {
                    nodeVals[iterIdx][i] = iter.getAccleromterData().get(i);
                    nodeIndices[iterIdx][i] = i+1;
                }
                iterIdx++;
            }
        }

        prob.y = new double[inputData.size()];
        prob.l = inputData.size();
        prob.x = new svm_node[inputData.size()][];
        svm_node nodeTemp = null;

        // packing data in svm objects
        for (int i = 0; i < inputData.size(); i++) {
            prob.y[i] = inputData.get(i).getActType().fId;
            double[] thisVals = nodeVals[i];
            int[] indices = nodeIndices[i];
            prob.x[i] = new svm_node[thisVals.length];
            for (int j = 0; j < thisVals.length; j++) {
                nodeTemp = new svm_node();
                nodeTemp.index = indices[j];
                nodeTemp.value = thisVals[j];
                prob.x[i][j] = nodeTemp;
            }
        }
        return prob;
    }

    public double doCrossValidation(List<DataBean> inputData){
        double [] predClasses = new double[inputData.size()];
        int ctr = 0;
        svm.svm_cross_validation(problemPacker(inputData),defineParamObject(), Values.NR_FOLD_CROSS_VALID,predClasses);
        for (int i = 0; i < predClasses.length; i++)
        {
            ctr += (Double.compare(predClasses[i],(double) inputData.get(i).getActType().fId)==0)?1:0;
        }
        return ((double) ctr)/inputData.size();
    }

    public double[] predictFromSetOfInputs(List<DataBean> inputData,svm_model trainedModel){
        double predictedVals[] = new double[inputData.size()];
        for (int i = 0; i < predictedVals.length; i++) {
            predictedVals[i] = predictFromSingleInput(inputData.get(i),trainedModel);
        }
        return predictedVals;
    }

    public double predictFromSingleInput(DataBean inputBean,svm_model trainedModel){
        double predictProb = 0;
        svm_node[] inputNodeInstance = new svm_node[inputBean.getAccleromterData().size()];
        svm_node atomNode = null;

        for (int i = 0; i < inputBean.getAccleromterData().size(); i++) {
            atomNode = new svm_node();
            atomNode.index = i+1;
            atomNode.value = inputBean.getAccleromterData().get(i);
            inputNodeInstance[i] = atomNode;
        }

        int numOfCategories = svm.svm_get_nr_class(trainedModel);
        int [] labels = new int[numOfCategories];
        svm.svm_get_labels(trainedModel,labels);

        double[] predictedClassWeights = new double[numOfCategories];
        predictProb = svm.svm_predict_probability(trainedModel,inputNodeInstance,
                predictedClassWeights);
        double predictedClass = svm.svm_predict(trainedModel,inputNodeInstance);
        return predictedClass;
    }



    public svm_parameter defineParamObject(){
        paramObject.probability = 1;
        paramObject.gamma = 0.5;
        paramObject.nu = 0.5;
        paramObject.C = 1;
        paramObject.svm_type = svm_parameter.C_SVC;
        paramObject.kernel_type = svm_parameter.LINEAR;
        paramObject.cache_size = 20000;
        paramObject.eps = 0.001;
        return paramObject;
    }
}
