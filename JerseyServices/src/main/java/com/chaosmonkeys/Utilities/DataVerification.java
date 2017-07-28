package com.chaosmonkeys.Utilities;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.*;
import java.nio.file.Path;
import java.util.Optional;


public class DataVerification {

    private String fileName;
    private File datasetFile;
    private Path datasetPath;
    private Gson gson;


    public DataVerification(String fileName) {
        this.fileName = fileName;
        gson = new Gson();

    }

    public DataVerification(File file){
        this(file.getName());
        this.datasetFile = file;
        this.datasetPath = file.toPath();
    }

    public static void main(String[] args) {
        DataVerification dv = new DataVerification("test.json");
        Logger.Info("datasets verification result: " + dv.isFileValidate());
    }

    /**
     * Pass a filename and see if it is valid json or csv filename
     * @param fileName
     * @return
     */
    public static boolean isFileExtensionValidate(String fileName){
        Optional<String> extensionOptional = Optional.ofNullable(getFileExtension(fileName));
        boolean valid = false;
        if(extensionOptional.isPresent()) {
            String str = extensionOptional.get();
            if (str.toLowerCase().equals("json") || str.toLowerCase().equals("csv")) {
                return true;
            }
        }
        return false;
    }

    public boolean isFileValidate() {
        Optional<String> extensionOptional = Optional.ofNullable(getFileExtension(this.fileName));
        boolean valid = false;
        if(extensionOptional.isPresent()) {
            String str = extensionOptional.get();
            if (str.toLowerCase().equals("json") || str.toLowerCase().equals("csv")) {
                return true;
            }
        }
         return false;
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.length() == 0){
            return null;
        }
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0){
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else{
            return "";
        }
    }


    public boolean isJSONValid() {
        InputStream inputStream=null;
        InputStreamReader reader=null;
        try {
            inputStream = new FileInputStream(datasetFile);
            reader = new InputStreamReader(inputStream);
            gson.fromJson(reader, Object.class);
            reader.close();
            return true;

        } catch (FileNotFoundException ex) {
            try{
                if(inputStream!=null)
                inputStream.close();
                Logger.Exception("file not found");
                return false;
            } catch (IOException IOExp) {
                return false;
            }
        }  catch (JsonSyntaxException ex) {
            try{
                if(reader!=null)
                reader.close();
                Logger.Exception("JSON syntax error");
                return false;
            } catch (IOException IOEx) {
                return false;
            }
        }catch (IOException ex) {
            try{
                reader.close();
                Logger.Exception("IOException");
                return false;
            } catch (IOException IOEx) {
                return false;
            }

        }

    }

    public boolean isCSVValid() {
        if(getFileExtension(fileName).toLowerCase().equals("csv")){
            return true;
        }
        return false;
    }


}


