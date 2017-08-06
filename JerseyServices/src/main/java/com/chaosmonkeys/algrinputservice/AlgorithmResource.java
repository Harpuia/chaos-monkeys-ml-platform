package com.chaosmonkeys.algrinputservice;

import com.chaosmonkeys.DTO.BaseResponse;
import com.chaosmonkeys.Utilities.*;
import com.chaosmonkeys.Utilities.db.DbUtils;
import com.chaosmonkeys.dao.AlgorithmLanguage;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class containing all possible service calls relevant to upload algorithm(API methods)
 *
 * created on 2017/6/28.
 */

@Path("/services/algr")
public class AlgorithmResource {

    // Constants operating with service status
    private static final String STATUS_RUN = "RUNNING";
    private static final String STATUS_IDLE = "IDLE";

    // states variables
    public static String serviceStatus = STATUS_IDLE;

    private static final List<String> supportDevLanguageList = new ArrayList<>();  // Arrays.asList("R","Python")

    // sets store data sets name that are under processing
    public static Set<String> uploadSet = new HashSet<>();
    public static Set<String> checkSet = new HashSet<>();

    // Success Code
    /**
     * Basic successful response will have code=0 and success = true
     * HTTP status code = 200 and if failed http status code will be assigned to 400(BAD REQUEST)
     */
    public static final int CHECK_SUCCESS = 0;
    // Error Code
    private static final int ERR_BLANK_PARAMS = 201;
    private static final int ERR_UNSUPPORTED_LANG = 202;
    private static final int ERR_TRANSMISSION_FILE = 203;
    private static final int ERR_FILE_BODYPART_MISSING = 204;
    private static final int ERR_UNZIP_EXCEPTION = 205;
    private static final int ERR_REQUIRED_FILE_MISSING = 206;
    private static final int ERR_CANNOT_CREATE_FILE = 207;
    private static final int ERR_INVALID_ZIP_EXT = 208;
    private static final int ERR_UNKNOWN = 299;

    /**
     * API for receiving uploading algorithm request
     * @param fileInputStream uploaded file stream
     * @param fileMetaData  uploaded file metadata, containing file name
     * @param algrName algorithm name
     * @param algrDescription   algorithm decription (optional)
     * @param language  algorithm developing language
     * @return  JSON response contains {success:bool, code:int, msg:string}
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadAlgorithmFile(@FormDataParam("file") InputStream fileInputStream,
                                        @FormDataParam("file") FormDataContentDisposition fileMetaData,
                                        @FormDataParam("name") String algrName,
                                        @FormDataParam("description") String algrDescription,
                                        @FormDataParam("language") String language){
        refreshServiceState();
        if (null != algrName && !algrName.equals("")) {
            Logger.Request("Received algorithm upload request - Algorithm Name: " + algrName);
        }
        // --::ERROR DETECTING
        int validCode = detectUploadServiceParamError(fileInputStream, fileMetaData, algrName,language);
        if( validCode != CHECK_SUCCESS){
            Response errorResponse = genErrorResponse(validCode);
            return errorResponse;
        }
        //create Algorithm folder if it does not exist yet
        File algrFolder = FileUtils.createAlgorithmFolder();
        try {
            Logger.Info("Algorithm storage root folder has been created in " + algrFolder.toPath().toRealPath().toString());
        } catch (IOException e) {
            Logger.Error("Algorithm input service cannot create algorithm storage root folder due to unknown reason");
            e.printStackTrace();
            validCode = ERR_CANNOT_CREATE_FILE;
            return genErrorResponse(validCode);
        }
        //create dev language folder if it does not exist yet
        File langFolder = FileUtils.createNewFolderUnder(language, algrFolder);
        // create target folder
        String targetFolderName = StringUtils.genAlgrStorageFolderName(algrName);
        File targetFolder = FileUtils.createNewFolderUnder(targetFolderName, langFolder);
        String fileName = fileMetaData.getFileName();
        // start processing receiving
        validCode = CHECK_SUCCESS;
        boolean succ = receiveFile(fileInputStream, targetFolder, fileName);
        if(!succ){
            validCode = ERR_TRANSMISSION_FILE;
        }else{  // unzip and check folder file structure
            //add to check list
            checkSet.add(algrName);
            File zipFile = new File(targetFolder, fileName);
            boolean unzipSucc = unzipRequiredFile(zipFile, targetFolder);
            if(!unzipSucc){
                validCode = ERR_UNZIP_EXCEPTION;
            }else{
                // all required file need exist
                if(!isAllRequiredFilesProvide(targetFolder)){
                    validCode = ERR_REQUIRED_FILE_MISSING;
                }
            }
            checkSet.remove(algrName);
        }
        if(CHECK_SUCCESS != validCode){
            // delete the folder
            FileUtils.deleteQuietly(targetFolder);
            return genErrorResponse(validCode);
        }else{  // unzipping successfully, delete the zip file
            File zipFile = new File(targetFolder, fileName);   // the downloaded zip file
            FileUtils.deleteQuietly(zipFile);
        }
        try {
            // avoid null value of description
            if(null == algrDescription){
                algrDescription = "";
            }
            DbUtils.storeAlgorithm(algrName, algrDescription, targetFolder.toPath().toRealPath().toString(), language);
            Logger.SaveLog(LogType.Information, "Algorithm received successfully");
            return genSuccResponse();
        } catch (IOException e) {
            e.printStackTrace();
            Logger.Error("Algorithm folder creation failure");
            FileUtils.deleteQuietly(targetFolder);
            validCode = ERR_TRANSMISSION_FILE;
            return genErrorResponse(validCode);
        }

    }

    /**
     * call ZipUtils to unzip zip file
     * @param zipFile  target zip file
     * @param targetFolder  extracting folder
     * @return succeed or not
     */
    private boolean unzipRequiredFile(File zipFile, File targetFolder){
        try {
            ZipUtils.unzip(zipFile, targetFolder);
        } catch (IOException e) {
            Logger.SaveLog(LogType.Exception, "Exception happened when unzip algorithm file");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * check if the required files and folders has been provided
     * @param folder folder that contains extracted files
     * @return  boolean: valid or not
     */
    private boolean isAllRequiredFilesProvide(File folder){
        List<String> optionalFolderList = Arrays.asList("input","output");
        for (String folderName : optionalFolderList){
            File optionalFolder = new File(folder, folderName);
            if(!optionalFolder.exists()){
                optionalFolder.mkdir();
            }
        }
        // check required files
        List<String> requiredFileList = Arrays.asList("Main.R");
        for (String fileName : requiredFileList){
            File requiredFile = new File(folder, fileName);
            if(!requiredFile.exists()){
                return false;
            }
        }
        return true;
    }

    /**
     * File receiving via InputStream
     * @param fileInputStream uploaded file stream
     * @param targetFolder  storage folder
     * @param fileName  new file name
     * @return boolean: succeed or not
     */
    public boolean receiveFile(InputStream fileInputStream, File targetFolder, String fileName){
        // receive all file parts and store in targetFolder using filename
        // if exception happen, delete targetFolder
        uploadSet.add(fileName);
        File targetFile = new File(targetFolder, fileName);
        try {
            FileUtils.receiveFile(fileInputStream, targetFile);
            // remove this item from uploading list
            uploadSet.remove(fileName);
        } catch (IOException e) {
            // remove this one from uploading list
            uploadSet.remove(fileName);
            // delete error dataset
            Logger.Exception("Exception happened while receiving algorithm file from client side.");
            FileUtils.deleteQuietly(targetFolder);
            Logger.Exception("Delete the created folder due to the failure of receiving algorithm file");
            e.printStackTrace();
            return false;
        }
        refreshServiceState();
        return true;
    }

    /**
     * Detect whether all parameters are fulfilled or not
     *
     * @param fileInputStream uploaded file stream
     * @param fileMetaData  file metadata
     *@param name algorithm name, must not be empty
     * @param language algorithm developing language, must be consistent with database
     * @return error code that has been defined in the global config or in the heading of this class
     */
    public int detectUploadServiceParamError(InputStream fileInputStream, FormDataContentDisposition fileMetaData, String name, String language){
        // check all string parameters are not blank
        // check whether the bodypart/file content are attached
        if(null == fileInputStream || null == fileMetaData){
            return ERR_FILE_BODYPART_MISSING;
        }
        // check file extension, if it is not .zip or .ZIP return error message
        String fileName = fileMetaData.getFileName();
        int dotIndex = fileName.lastIndexOf(".");
        if(dotIndex != -1 && dotIndex != fileName.length()-1 ){
            // check the file extension
            String ext = fileName.substring(dotIndex+1);
            if(!ext.toLowerCase().equals("zip") ){
                return ERR_INVALID_ZIP_EXT;
            }
        }else{
            return ERR_INVALID_ZIP_EXT;
        }
        boolean isParamsValid = StringUtils.isNoneBlank(name, language);
        if(!isParamsValid){
            return ERR_BLANK_PARAMS;
        }
        // load supported dev language list
        if(supportDevLanguageList.isEmpty()){   // load the language list from database
            DbUtils.openConnection();
            List<AlgorithmLanguage> languages = AlgorithmLanguage.findAll();
            languages.stream()
                    .filter( lang -> !lang.getLanguage().equals("") )
                    .forEach( lang -> supportDevLanguageList.add(lang.getLanguage()));
            DbUtils.closeConnection();
            Logger.Info("Load supported language list: " + supportDevLanguageList);
        }
        // or from database in the future, but it may involve compute cost for each upload/other services, so let's keep R, Python C++ and Matlab now
        boolean isLangSupported = supportDevLanguageList.stream().anyMatch(lang -> lang.toLowerCase().equals(language.toLowerCase()));
        if(!isLangSupported){
            Logger.Info("Received algorithm uploading request with unsupported machine learning development language");
            return ERR_UNSUPPORTED_LANG;
        }
        return CHECK_SUCCESS;
    }

    /**
     * Generate corrpesponding error message based on error code
     * @param errorCode pre-defined error code
     * @return JSON response contains {success:bool, code:int, msg:string}
     */
    private Response genErrorResponse(int errorCode){
        BaseResponse responseEntity = new BaseResponse();
        String msg;
        switch (errorCode){
            case(ERR_BLANK_PARAMS):
                msg = "Some parameters you input is empty or blank";
                break;
            case(ERR_UNSUPPORTED_LANG):
                msg = "unsupported language";
                break;
            case(ERR_FILE_BODYPART_MISSING):
                msg = "the file bodypart is missing in the form";
                break;
            case(ERR_REQUIRED_FILE_MISSING):
                msg = "uploaded zip file does not include all required files/folders";
                break;
            case(ERR_UNZIP_EXCEPTION):
                msg = "server unzip file throws exception, please check whether the file is corrupt or not";
                break;
            case(ERR_CANNOT_CREATE_FILE):
                msg = "Server cannot store your file at this time, please try again or contact administrator";
                break;
            case(ERR_INVALID_ZIP_EXT):
                msg = "Please upload valid zip file with .zip file extension";
                break;
            default:
                errorCode = ERR_UNKNOWN;
                msg = "unknown error";
        }
        Logger.Response("Server respond with error: " + msg);
        responseEntity.failed(errorCode,msg);
        Response response = Response.status(Response.Status.BAD_REQUEST)
                .entity(responseEntity)
                .build();
        return response;
    }
    /**
     * Generate corrpesponding successful message
     * @return Success Response, JSON response contains {success:bool, code:int, msg:string}
     */
    private Response genSuccResponse(){
        BaseResponse responseEntity = new BaseResponse();
        responseEntity.successful("algorithm upload successfully");
        Logger.Response("Respond success");
        Response response = Response.ok()
                                    .entity(responseEntity)
                                    .build();
        return response;
    }


    /**
     * Refresh Service Status based on the size of checking set and uploading list
     */
    public void refreshServiceState(){
        if( checkSet.isEmpty() && uploadSet.isEmpty() ){
            this.serviceStatus = STATUS_IDLE;
        }else{
            this.serviceStatus = STATUS_RUN;
        }
    }

}
