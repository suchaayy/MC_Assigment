package com.example.sucharitharumesh.g7_a3.csvutil;

import com.example.sucharitharumesh.g7_a3.svmwrapperlibrary.ActivityType;
import com.example.sucharitharumesh.g7_a3.svmwrapperlibrary.DataBean;


import java.util.ArrayList;
import java.util.List;


public class CSVUtil
{
    public static List<DataBean> readFromFile(String fileName){
        List<DataBean> outList = new ArrayList<>();
        List<String[]> inpList = new CSVReader().csvReader(fileName);
        int ctr = 0;
        DataBean running,walking,eating;
        running = new DataBean(ActivityType.RUNNING);
        walking = new DataBean(ActivityType.WALKING);
        eating = new DataBean(ActivityType.JUMPING);

        for(String[] iter : inpList){
            for (int i = 0; i < iter.length; i++)
            {
                if(i<3){
                    running.getAccleromterData().add(Double.parseDouble(iter[i]));
                }else if(i<6){
                    walking.getAccleromterData().add(Double.parseDouble(iter[i]));
                }else {
                    eating.getAccleromterData().add(Double.parseDouble(iter[i]));
                }
            }
            if(ctr!=0 && (ctr+1)%50==0){
                outList.add(running);
                outList.add(walking);
                outList.add(eating);
                running = new DataBean(ActivityType.RUNNING);
                walking = new DataBean(ActivityType.WALKING);
                eating = new DataBean(ActivityType.JUMPING);
            }
            ctr++;
        }
        return outList;
    }
    public static List<DataBean> readSpecColumn(String fileName,ActivityType activityType){
        List<DataBean> outList = new ArrayList<>();
        List<String[]> inpList = new CSVReader().csvReader(fileName);
        int ctr = 0;
        DataBean activity = new DataBean(activityType);

        for(String[] iter : inpList){

            for (int i = 0; i < iter.length; i++)
            {
                if(ActivityType.RUNNING == activityType && i<3)
                {
                    activity.getAccleromterData().add(Double.parseDouble(iter[i]));
                }else if(ActivityType.WALKING == activityType && i>=3 && i<6){
                    activity.getAccleromterData().add(Double.parseDouble(iter[i]));
                }else if(ActivityType.JUMPING == activityType && i>6){
                    activity.getAccleromterData().add(Double.parseDouble(iter[i]));
                }
            }
            if(ctr!=0 && (ctr+1)%50==0){
                outList.add(activity);
                activity = new DataBean(ActivityType.RUNNING);
            }
            ctr++;
        }
        return outList;
    }
}
