package com.example.sucharitharumesh.g7_a3;



public class Values {
    public static int INPUT_DIMENSION_SIZE = 150;
    public static String TRAINING_TABLE = "Training";
    public static String TRAINED_TABLE = "Trained";
    public static String TEST_TABLE = "Test";
    public static String TRAIN_FIRST_STRING = "Train data before Testing!!";
    public static String NO_DATA = "There is no data to train...please train atleast 20 times.";
    public static String INSUFFICIENT_DATA = "Data Insufficient..Please train 20 times for each activity!";
    public static String ACTIVITY_PERFORMED = "Activity performed is ";
    public static String TRAINING_COMPLETED = "Training of data completed Successfully";
    public static String SQL_TRAINING_SELECT = "SELECT * FROM " + Values.TRAINING_TABLE;
    public static String SQL_TRAINED_INSERT = "INSERT INTO" + Values.TRAINED_TABLE + "SELECT * FROM"+ Values.TRAINING_TABLE;
    public static String SQL_DELETE_TEST_TABLE = "DELETE FROM " + Values.TEST_TABLE;
    public static String SQL_TEST_SELECT = "SELECT * FROM " + Values.TEST_TABLE + " ORDER BY ID DESC LIMIT 1";
    public static int INPUT_ROWS_TRAINING = 60;
    public static int TEST_ROWS_LIMIT = 1;
    public static String EXCEPTION_SVM_SERVICE = "Exception occured in SVM Service class! Please take a look at logs";
    public static String dbName="G7_A3.db";
    public static String inputFileName = "/data.csv";
    public static String testFileName = "/test.csv";
    public static int NR_FOLD_CROSS_VALID = 4;
    public static String TRAIN_BEFORE_ABOUT = "Please train data to check accuracy!!";
}

