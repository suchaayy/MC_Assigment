package com.example.sucharitharumesh.g7_a3;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import libsvm.svm;
import libsvm.svm_model;

public class MainActivity extends AppCompatActivity{

    private SQLiteDatabase dbCon;
    private SensorManager AcclManager;// = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    private Sensor Accelerometer;// = AcclManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    String tableName;
    boolean flag=true;


    private int activity_label; //0- walking, 1- running,  2 - eating
    private int columnSize=0;
    private String rowToBeInserted="";
    //private svm_model model = null;
    private SVMActivity serviceObject = null;
    //private SVMActivity serviceObject2 = null;
    private Button visualizationButton;
    private Button aboutButton;
    private Button accuracy;
    public String model_file_name;
    ProgressDialog progress;
    private String dbPath;
    private String dataDirectoryPath;
    String activityToBeRecorded;
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
                progress= ProgressDialog.show(MainActivity.this,"","Checking activity",true);

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

    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try{
            checkDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
        } catch (SQLiteException e) {

        }
        if(checkDB !=null )
             return true;
        else
            return false;

    }

    private void displayTable(SQLiteDatabase db, String t_name)
    {
        String selectQuery= "SELECT * FROM " + tableName+";";
        Cursor sel = dbCon.rawQuery(selectQuery, null);
        sel.moveToFirst();
        int c=0;
        do{
            int noOfColumns= sel.getColumnCount();
            String row="";
            row= row+ Integer.toString(sel.getInt(0));
            for(int i=1;i<noOfColumns-1;i++)
            {
                row= row+","+Float.toString(sel.getFloat(i));
            }
            row= row +","+ Integer.toString(sel.getInt(noOfColumns-1));
            Log.d("Row No. "+Integer.toString(c), row);
            c++;
        }while (sel.moveToNext());
    }
    private void deleteTestTable(SQLiteDatabase db)
    {
        displayTable(db, Values.TEST_TABLE);
        db.execSQL("DROP TABLE IF EXISTS Test");
    }

    public boolean isTableExists(String tableName) {
      //Cursor c = null;
        boolean tableExists = false;
        /* get cursor on it */
        try
        {
            dbCon.query(tableName, null,
                    null, null, null, null, null);
            tableExists = true;
        }
        catch (Exception e) {
            /* fail */

        }

        return tableExists;

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
        dataDirectoryPath= "/data/data/"+getApplicationContext().getPackageName();
        dbPath=dataDirectoryPath+"/"+ Values.dbName;
        model_file_name=dataDirectoryPath+ "/test_svm.model";


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button insertRecord= (Button) findViewById(R.id.record);
        insertRecord.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                dbCon=openOrCreateDatabase(dbPath,MODE_PRIVATE,null);
                RadioButton run= (RadioButton) findViewById(R.id.run);
                if (run.isChecked()) {
                    activityToBeRecorded = "Running";
                    activity_label= 1;
                }
                RadioButton walk= (RadioButton) findViewById(R.id.walk);
                if (walk.isChecked()) {
                    activityToBeRecorded = "Walking";
                    activity_label= 0;
                }
                RadioButton eat= (RadioButton) findViewById(R.id.Jump);
                if(eat.isChecked()) {
                    activityToBeRecorded = "Jumping";
                    activity_label= 2;
                }
                tableName= Values.TRAINING_TABLE;
                createTable(dbCon,tableName);
                registerAcclListener(activityToBeRecorded);
            }
        });


        Button trainActivity= (Button) findViewById(R.id.train);
        trainActivity.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                boolean check = checkDataBase();
                if(check == false )
                {
                    Toast.makeText(getApplicationContext(), Values.NO_DATA,Toast.LENGTH_LONG).show();
                        return;

                }
                else {
                    dbCon = openOrCreateDatabase(dbPath,MODE_PRIVATE,null);
                    Cursor checkRowCursor = dbCon.rawQuery(Values.SQL_TRAINING_SELECT,null);

                    if(checkRowCursor.getCount() < 60)
                    {
                        Toast.makeText(getApplicationContext(), Values.INSUFFICIENT_DATA,Toast.LENGTH_LONG).show();
                        return;
                    }
                    else
                    {
                        //dbCon = openOrCreateDatabase(dbPath,MODE_PRIVATE,null);
                        progress = progress.show(MainActivity.this,"","Training of model ongoing",true);
                        serviceObject = new SVMActivity(getApplicationContext());
                        serviceObject.train(dbCon,model_file_name);
                        /*tableName= Values.TRAINED_TABLE;
                        createTable(dbCon,tableName);
                        Cursor c = dbCon.rawQuery(Values.SQL_TRAINED_INSERT, null);
                   */
                        progress.dismiss();
                        Toast.makeText(getApplicationContext(), Values.TRAINING_COMPLETED,Toast.LENGTH_LONG).show();
                        return;
                    }

                }
          }
        });

        Button ecredit = (Button) findViewById(R.id.ecredit);
        ecredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestActivity.class);
                //Bundle bundle = new Bundle();
                //bundle.putString("accuracy", acc);
                //intent.putExtras(bundle);
                startActivity(intent);
                setContentView(R.layout.activity_test);

            }
        });
/*
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
                        //dbCon = openOrCreateDatabase(dbPath,MODE_PRIVATE,null);
                    Cursor checkRowCursor = dbCon.rawQuery(Values.SQL_TEST_SELECT,null);
                    int counter = checkRowCursor.getCount();
                    if(counter >= 1)
                        // dbCon.execSQL(Values.SQL_DELETE_TEST_TABLE);

                        dbCon = openOrCreateDatabase(dbPath,MODE_PRIVATE,null);
                        createTable(dbCon,Values.TEST_TABLE);
                        tableName=Values.TEST_TABLE;
                        activity_label = -1;
                        registerAcclListener("Testing");
                        dbCon.execSQL(Values.SQL_DELETE_TEST_TABLE);
                        return;

                   }


                //activityToBeRecorded= "performing the activity";
                //tableName= Values.TEST_TABLE;
                //activity_label = -1;
                //dbCon= openOrCreateDatabase(dbPath,MODE_PRIVATE,null);
                //createTable(dbCon,tableName);
                //registerAcclListener(activityToBeRecorded);
             if(!progress.isShowing())
                {
//                  predictActivity(); Open DB; Query Test table; Use SVM, predict and display the class label
                    deleteTestTable(dbCon);
                    dbCon.close();
                }
            }
        });*/

        visualizationButton = (Button) findViewById(R.id.visual);
        visualizationButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view) {
                try
                {
                    File csvFileHandler = new File(dataDirectoryPath+"/data.csv");
                    csvFileHandler.createNewFile();
                    dbCon = openOrCreateDatabase(dbPath, MODE_PRIVATE, null);
                    List<List<Float>> accelerometerValues = new ArrayList<List<Float>>();
                    for (int i = 0; i < 3; i++) {
                        String selectQuery = "SELECT * FROM " + Values.TRAINING_TABLE + " WHERE Activity_Label=" + Integer.toString(i) + ";";
                        List<Float> valuesForActivity = new ArrayList<Float>();
                        Cursor sel = dbCon.rawQuery(selectQuery, null);
                        sel.moveToFirst();
                        do {
                            int noOfColumns = sel.getColumnCount();
                            for (int j = 1; j < noOfColumns - 1; j++) {
                                valuesForActivity.add(sel.getFloat(j));
                            }
                        } while (sel.moveToNext());
                        accelerometerValues.add(valuesForActivity);
                    }
                    FileWriter csvWriter= new FileWriter(csvFileHandler);

                    csvWriter.write("x1,y1,z1,x2,y2,z2,x3,y3,z3\r\n");
                    int counter=0;
                    int[] noOfSamplesList=new int[3];
                    for(int i=0;i<3;i++)
                        noOfSamplesList[i]=accelerometerValues.get(i).size();
                    int maxLines=Math.max(noOfSamplesList[0], Math.max(noOfSamplesList[1],noOfSamplesList[2]));
                    while(counter<maxLines)
                    {
                        String eachLine="";
                        for(int i=0;i<3;i++) //Walk,run,eat
                        {
                            for (int j = 0; j < 3; j++) //X,Y,Z values
                            {
                                if (counter < noOfSamplesList[i]) {
                                    eachLine = eachLine + Float.toString(accelerometerValues.get(i).get(counter + j));
                                }
                                eachLine = eachLine + ",";
                            }
                        }
                        int length=eachLine.length();
                        StringBuilder sbEachLine= new StringBuilder(eachLine);
                        sbEachLine.deleteCharAt(length-1);
                        eachLine= sbEachLine.toString();
                        csvWriter.write(eachLine+"\r\n");

                        Log.d("Line number "+Integer.toString(counter)+":", eachLine);
                        counter+=3;
                    }
                    dbCon.close();
                    csvWriter.flush();
                    csvWriter.close();

//                    File newFile= new File(dataDirecrtoryPath+"/data.csv");
//                    FileReader csvReader=new FileReader(newFile);
//                    BufferedReader br = new BufferedReader(csvReader);
//                    String line="";
//                     while (true)
//                    {
//                        line=br.readLine();
//                        if (line==null)
//                            break;
//                        Log.d("Line is: ",line);
//                    }
//                    csvReader.close();


                    Log.d("Data Directory path:",dataDirectoryPath);

                    Intent newIntention = new Intent(MainActivity.this, Visualization.class);
                    startActivity(newIntention);
                }
                catch (Exception e)
                {
                    Log.d("Visualization Failed:", e.getMessage());
                }
            }

        });
        aboutButton = (Button) findViewById(R.id.parameter);
        aboutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent newIntention = new Intent(MainActivity.this, Parameters.class);
                        if (serviceObject == null){
                            Toast.makeText(getApplicationContext(), Values.TRAIN_BEFORE_ABOUT,Toast.LENGTH_LONG).show();
                            String acc = Values.TRAIN_BEFORE_ABOUT;
                            Bundle bundle = new Bundle();
                            bundle.putString("accuracy", acc);
                            newIntention.putExtras(bundle);
                            startActivity(newIntention);

                        }
                        else {
                            Double accuracy = serviceObject.getkFoldAccuracy();
                            String acc = Double.toString(accuracy);
                            Log.d("Accuracy =",acc);
                            Bundle bundle = new Bundle();
                            bundle.putString("accuracy", acc);
                            newIntention.putExtras(bundle);
                            startActivity(newIntention);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.d("About Failed:",e.getMessage());
                    }

                }
            });
        accuracy = (Button) findViewById(R.id.accuracy);
        accuracy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(serviceObject == null)
                    Toast.makeText(getApplicationContext(), Values.TRAIN_BEFORE_ABOUT,Toast.LENGTH_LONG).show();
                else {
                    Double acc = serviceObject.getkFoldAccuracy() * 100;
                    String a = Double.toString(acc);
                    Toast.makeText(getApplicationContext(), a ,Toast.LENGTH_LONG).show();

                }
            }
        });

    }
}
