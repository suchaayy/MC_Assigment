package com.example.sucharitharumesh.g7_a3;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.example.sucharitharumesh.g7_a3.svmwrapperlibrary.ActivityType;
import com.example.sucharitharumesh.g7_a3.svmwrapperlibrary.DataBean;
import com.example.sucharitharumesh.g7_a3.svmwrapperlibrary.SvmWrapper;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import libsvm.svm;
import libsvm.svm_model;

public class SVMActivity {


    private static svm_model modelToTrain = null;
    public SvmWrapper wrapperStub = null;
    private Context appContext = null;
    private double kFoldAccuracy;
    //private String model_file_name;
   // private String dataDirectoryPath;


    SVMActivity(Context appContext){
        wrapperStub = new SvmWrapper();
        this.appContext = appContext;


    }

    SVMActivity(Context appContext, svm_model m){
        modelToTrain = m;
        wrapperStub = new SvmWrapper();
        this.appContext = appContext;
    }

    public svm_model returnsvmmodel (){
        return modelToTrain;
    }

    public double getkFoldAccuracy(){
        return kFoldAccuracy;
    }

    public void train(SQLiteDatabase dbConnection, String model_file_name){
        try {
            List<DataBean> trainingSet = packer(dbConnection,true);
            this.kFoldAccuracy = wrapperStub.doCrossValidation(trainingSet);
            modelToTrain = wrapperStub.trainer(trainingSet);
            svm.svm_save_model(model_file_name,modelToTrain);
        }
        catch (Exception e){
            Toast.makeText(appContext, Values.EXCEPTION_SVM_SERVICE,Toast.LENGTH_LONG).show();
            Log.d(e.toString(),e.toString());
        }

    }

    public String test(SQLiteDatabase dbConnection){
        List<DataBean> testSet;
        ActivityType returnType = null;

        double predictedArr[];
        try {
            testSet = packer(dbConnection,false);

            predictedArr = wrapperStub.predictFromSetOfInputs(testSet,modelToTrain);
            Log.d("Pred array =", Arrays.toString(predictedArr));
            returnType = returnMajorityClass(predictedArr);
            switch(returnType)
            {
                case WALKING:
                        return "WALKING";
                case RUNNING:
                        return "RUNNING";
                case JUMPING:return "JUMPING";
            }
        }
        catch (Exception e){
            Toast.makeText(appContext, Values.EXCEPTION_SVM_SERVICE,Toast.LENGTH_LONG).show();
            Log.d(e.toString(),e.toString());
        }
        return "";
    }

    public ActivityType returnMajorityClass(double [] predictedArr){
        ActivityType retType = ActivityType.JUMPING;
        int eatingCount = 0,walkingCount =0,runningCount = 0;
        for (int i = 0; i < predictedArr.length; i++) {
            switch ((int) predictedArr[i]){
                case 0:
                    walkingCount++;
                    break;
                case 1:
                    runningCount++;
                    break;
                case 2:
                    eatingCount++;
            }
        }
        if(walkingCount > runningCount && walkingCount > eatingCount){
            return ActivityType.WALKING;
        }else if(runningCount > walkingCount && runningCount > eatingCount){
            return ActivityType.RUNNING;
        }
        return retType;
    }

    public List<DataBean> packer(SQLiteDatabase dbConnection,boolean training)throws Exception{
        List<DataBean> returnObject = new ArrayList<>();
        Cursor selectCursor = training ? dbConnection.rawQuery(Values.SQL_TRAINING_SELECT,null)
                : dbConnection.rawQuery(Values.SQL_TEST_SELECT,null);
        int iterLimit = training ? selectCursor.getCount() : Values.TEST_ROWS_LIMIT;
        int iterator = 0;
        DataBean beanObject;
        List<Double> tempList;
        ActivityType setAct;
        selectCursor.moveToFirst();

        while(iterator<iterLimit){
            tempList = new ArrayList<>();
            for (int i = 1; i < selectCursor.getColumnCount()-1; i++) {
                tempList.add(selectCursor.getDouble(i));
            }
            setAct = ActivityType.getValue(selectCursor.getInt(selectCursor.getColumnCount()-1));
            beanObject = new DataBean(setAct,tempList);
            returnObject.add(beanObject);
            iterator++;
            selectCursor.moveToNext();
        }
        return returnObject;
    }
}
