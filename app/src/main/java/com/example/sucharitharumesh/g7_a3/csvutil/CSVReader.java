package com.example.sucharitharumesh.g7_a3.csvutil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sdj on 4/6/17.
 */
public class CSVReader
{
    public List<String[]> csvReader(String csvFile){
        String line = "";
        String cvsSplitBy = ",";
        boolean firstLine = true;
        List<String[]> outList = new ArrayList<>();
        try {
            // Change this line to read from the path mentioned in Constants and test it
            BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(csvFile)));

            while ((line = br.readLine()) != null) {
                if (firstLine){
                    firstLine = !firstLine;
                    continue;
                }
                // use comma as separator
                String[] country = line.split(cvsSplitBy);
                // data is 3,3,3 values with running, walking and eating order
                outList.add(country);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outList;
    }
}
