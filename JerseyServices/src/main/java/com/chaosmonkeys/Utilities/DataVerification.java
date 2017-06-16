package com.chaosmonkeys.Utilities;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.*;


public class DataVerification {

    private String fileName;
    private Gson gson;


    public DataVerification(String fileName) {
        this.fileName = fileName;
        gson = new Gson();

    }

    public static void main(String[] args) {
        DataVerification dv = new DataVerification("test.json");
        System.out.println(dv.isFileValidate());

    }

    public boolean isFileValidate() {
        String fileExtension = getFileExtension(fileName);
        if (fileExtension.toLowerCase().equals("json"))
            return isJSONValid();
        else if (fileExtension.toLowerCase().equals("csv"))
            return isCSVValid();
        return false;
    }

    public String getFileExtension(String fileName) {
        if (fileName == null || fileName.length() == 0)
            return null;
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else return "";
    }


    public boolean isJSONValid() {
        InputStream inputStream=null;
        InputStreamReader reader=null;
        try {
            inputStream = new FileInputStream(fileName);
            reader = new InputStreamReader(inputStream);
            gson.fromJson(reader, Object.class);
            reader.close();
            return true;

        } catch (FileNotFoundException ex) {
            try{
                if(inputStream!=null)
                inputStream.close();
                System.out.println("file not found");
                return false;
            } catch (IOException IOExp) {
                return false;
            }
        }  catch (JsonSyntaxException ex) {
            try{
                if(reader!=null)
                reader.close();
                System.out.println("JSON syntax error");
                return false;
            } catch (IOException IOEx) {
                return false;
            }
        }catch (IOException ex) {
            try{
                reader.close();
                System.out.println("IOException");
                return false;
            } catch (IOException IOEx) {
                return false;
            }

        }

    }

    public boolean isCSVValid() {
        if(getFileExtension(fileName).toLowerCase().equals("csv"))
            return true;
        return false;
    }


}


