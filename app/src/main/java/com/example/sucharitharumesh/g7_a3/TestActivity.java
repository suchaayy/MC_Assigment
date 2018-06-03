package com.example.sucharitharumesh.g7_a3;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

import libsvm.svm;
import libsvm.svm_model;

public class TestActivity extends AppCompatActivity {
    private SQLiteDatabase dbCon;
    private SensorManager AcclManager;
    private Sensor Accelerometer;

    String tableName;
    boolean flag=true;


    private int activity_label; //0- walking, 1- running,  2 - eating
    private int columnSize=0;
    private String rowToBeInserted="";

    public String model_file_name;
    ProgressDialog progress;
    private String dbPath;
    private String dataDirectoryPath;

    long previousTime=0;
    private SensorEventListener acclListener=new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent acclEvent) {
            Sensor AcclSensor = acclEvent.sensor;
            if (AcclSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = acclEvent.values[0];
                float y = acclEvent.values[1];
                float z = acclEvent.values[2];
                long currentTime = System.currentTimeMillis();
                String msg=Float.toString(x)+","+Float.toString(y)+","+Float.toString(z);
                if ((currentTime-previousTime)>=90)
                {
                    Log.d("Current Time:",Integer.toString(columnSize)+","+Long.toString(currentTime-previousTime));
                    if (columnSize<50)
                    {
                        rowToBeInserted=rowToBeInserted+","+msg;
                        previousTime=currentTime;
                        columnSize++;
                    }
                    if (columnSize==50)
                    {
                        rowToBeInserted=Long.toString(currentTime)+rowToBeInserted+","+Integer.toString(activity_label);
                        insertRow(rowToBeInserted, activity_label);
                        rowToBeInserted="";
                        columnSize=0;
                        AcclManager.unregisterListener(acclListener);
                        dbCon.close();
                    }
                }
            }
        }

        public void insertRow(String row,int label)
        {
            String t_name= Values.TRAINING_TABLE;
            if(label==-1)
            {
                t_name= Values.TEST_TABLE;
            }
            try {
                dbCon.execSQL("INSERT INTO " + t_name + " VALUES (" + row + ");");
                progress.dismiss();
                Toast.makeText(getApplicationContext()," Row Inserted ",Toast.LENGTH_LONG).show();
                Log.d(" Row insert successful:", rowToBeInserted);
            }
            catch (Exception e)
            {
                Log.d(e.getMessage()," at - Insert part "+rowToBeInserted);
            }
            if (label == -1){
                //dbCon = openOrCreateDatabase(dbPath,MODE_PRIVATE,null);
                progress= ProgressDialog.show(TestActivity.this,"","Checking activity",true);

                svm_model m = null;
                try {
                    m = svm.svm_load_model("/data/data/"+getApplicationContext().getPackageName()+"/test_svm.model");
                } catch (IOException e) {
                    System.out.println("error"); e.printStackTrace();
                }
                SVMActivity SObject= new SVMActivity(getApplicationContext(),m);
                String result_activity = SObject.test(dbCon);
                progress.dismiss();
                Toast.makeText(getApplicationContext(), Values.ACTIVITY_PERFORMED+result_activity,Toast.LENGTH_LONG).show();
                dbCon.execSQL(Values.SQL_DELETE_TEST_TABLE);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d("Sensor Accuracy changed","Sensor Accuracy Changed");
        }
    };

    // Register Sensor to start recording data
    private void registerAcclListener(String activity)
    {
        progress= ProgressDialog.show(this, "", "Recording Activity..."+activity+"...", true);
        AcclManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Accelerometer = AcclManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        AcclManager.registerListener(acclListener,Accelerometer,100000/*0.1 secondsSensorManager.SENSOR_DELAY_NORMAL*/);
    }

    private void createTable(SQLiteDatabase connection, String t_name)
    {
        String createTableName="CREATE TABLE " + t_name + " (ID REAL";
        for (int i=0;i<50;i++)
        {
            String x_attr="Accel_X_"+Integer.toString(i+1)+" REAL";
            String y_attr="Accel_Y_"+Integer.toString(i+1)+" REAL";
            String z_attr="Accel_Z_"+Integer.toString(i+1)+" REAL";
            createTableName=createTableName+", "+x_attr+", "+y_attr+", "+z_attr;
        }
        createTableName=createTableName+", Activity_Label INTEGER);"; //0- walking, 1- running,  2 - eating
        try {
            connection.execSQL(createTableName);
            Log.d("Table Created ", t_name);
        }
        catch (Exception e)
        {
            Log.d("Table Already exists: ",t_name);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        dataDirectoryPath= "/data/data/"+getApplicationContext().getPackageName();
        dbPath=dataDirectoryPath+"/"+ Values.dbName;
        model_file_name=dataDirectoryPath+ "/test_svm.model";


        Button testActivity= (Button) findViewById(R.id.test);
        testActivity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                svm_model m1 = null;
                try {
                    m1= svm.svm_load_model("/data/data/"+getApplicationContext().getPackageName()+"/test_svm.model");
                } catch (IOException e) {
                    System.out.println("error"); e.printStackTrace();
                }



                if( m1 == null){

                    Toast.makeText(getApplicationContext(), Values.TRAIN_FIRST_STRING, Toast.LENGTH_LONG).show();
                    return;
                }
                else
                {

                    dbCon = openOrCreateDatabase(dbPath,MODE_PRIVATE,null);
                    createTable(dbCon, Values.TEST_TABLE);
                    tableName= Values.TEST_TABLE;
                    activity_label = -1;
                    registerAcclListener("Testing");
                    dbCon.execSQL(Values.SQL_DELETE_TEST_TABLE);
                    return;

                }



            }
        });



    }
}

