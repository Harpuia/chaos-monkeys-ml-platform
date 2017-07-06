package com.chaosmonkeys.algrinputservice;

import com.chaosmonkeys.DTO.BaseResponse;
import com.chaosmonkeys.Utilities.*;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;

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

    // temporary support language list
    private static final List<String> supportDevLanguageList = Arrays.asList("R","Python");

    // sets store data sets name that are under processing
    public static Set<String> uploadSet = new HashSet<>();
    public static Set<String> checkSet = new HashSet<>();

    // Success Code
    public static final int CHECK_SUCCESS = 0;
    // Error Code
    public static final int ERR_BLANK_PARAMS = 201;
    public static final int ERR_UNSUPPORTED_LANG = 202;
    public static final int ERR_TRANSMISSION_FILE = 203;
    public static final int ERR_FILE_BODYPART_MISSING = 204;
    public static final int ERR_UNZIP_EXCEPTION = 205;
    public static final int ERR_REQUIRED_FILE_MISSING = 206;
    public static final int ERR_UNKNOWN = 299;
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadAlgorithmFile(@FormDataParam("file") InputStream fileInputStream,
                                        @FormDataParam("file") FormDataContentDisposition fileMetaData,
                                        @FormDataParam("name") String algrName,
                                        @FormDataParam("description") String algrDescription,
                                        @FormDataParam("user_id") String userId,
                                        @FormDataParam("language") String language){
        refreshServiceState();
        if (null != userId && !userId.equals("")) {
            Logger.SaveLog(LogType.Information, "INPUT: Received algorithm upload request from" + userId);
        }
        // --::ERROR DETECTING
        int validCode = detectUploadServiceParamError(fileInputStream, fileMetaData, algrName,algrDescription,userId,language);
        if( validCode != CHECK_SUCCESS){
            Response errorResponse = genErrorResponse(validCode);
            return errorResponse;
        }
        //create Algorithm folder if it does not exist yet
        File algrFolder = FileUtils.createAlgorithmFolder();
        Logger.SaveLog(LogType.Information, "Algorithm storage root folder has been created");
        //create dev language folder if it does not exist yet
        File langFolder = FileUtils.createNewFolderUnder(language, algrFolder);
        // create target folder
        String targetFolderName = genAlgrStorageFolderName(algrName, userId);
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
            targetFolder.deleteOnExit();
            return genErrorResponse(validCode);
        }
        //TODO: insert data sets into database.
        //storeDataSets(userId,projectId,dataName,dataDescription,targetFolder.getAbsolutePath(),format);
        Logger.SaveLog(LogType.Information, "Algorithm received sucessfully");
        return genSuccResponse();
    }


    public boolean unzipRequiredFile(File zipFile, File targetFolder){
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
     * @param folder
     * @return
     */
    public boolean isAllRequiredFilesProvide(File folder){
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
     * Generate algorithm storage folder name (valid folder name in common OS)
     * @param name
     * @param userId
     * @return
     */
    public String genAlgrStorageFolderName(String name, String userId){
        StringBuilder strBuilder = new StringBuilder();
        String validName = FileUtils.sanitizeFilename(name);
        strBuilder.append(validName);
        strBuilder.append("-");
        String validUserId = FileUtils.sanitizeFilename(userId);
        strBuilder.append(validUserId);
        strBuilder.append("-");
        strBuilder.append(System.currentTimeMillis());

        return strBuilder.toString();
    }

    public boolean receiveFile(InputStream fileInputStream, File targetFolder, String fileName){
        // receive all file parts and store in targetFolder using filename
        // if exception happen, delete targetFolder
        try {
            int read = 0;       // the total number of bytes read into the buffer
            byte[] bytes = new byte[1024];

            // add to uploading set
            uploadSet.add(fileName);
            OutputStream out = new FileOutputStream(new File(targetFolder, fileName));
            while ((read = fileInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
            // remove this one from uploading list
            uploadSet.remove(fileName);

        } catch (IOException e) {
            // delete error dataset
            Logger.SaveLog(LogType.Exception, "Error while uploading algorithm file.");
            targetFolder.deleteOnExit();
            Logger.SaveLog(LogType.Exception, "delete folder created");
            e.printStackTrace();
            return false;
        }
        refreshServiceState();
        return true;
    }

    /**
     * Detect whether all parameters are fulfilled or not
     *
     * @param fileInputStream
     * @param fileMetaData
     *@param name
     * @param description
     * @param userId
     * @param language     @return error code that has been defined in the global config or in the heading of this class
     */
    public int detectUploadServiceParamError(InputStream fileInputStream, FormDataContentDisposition fileMetaData, String name, String description, String userId, String language){
        // check all string parameters are not blank
        // check whether the bodypart/file content are attached
        if(null == fileInputStream || null == fileMetaData){
            return ERR_FILE_BODYPART_MISSING;
        }
        boolean isParamsValid = StringUtils.isNoneBlank(name, description, userId, language);
        if(!isParamsValid){
            return ERR_BLANK_PARAMS;
        }
        //TODO: dynamically load the supported language when receive supported language update notification from coordination service
        // or from database in the future, but it may involve compute cost for each upload/other services, so let's keep R, Python C++ and Matlab now
        boolean isLangSupported = supportDevLanguageList.stream().anyMatch(lang -> lang.equals(language));
        if(!isLangSupported){
            Logger.SaveLog(LogType.Exception,"Unsupported machine learning development language");
            return ERR_UNSUPPORTED_LANG;
        }
        return CHECK_SUCCESS;
    }

    /**
     * Generate corrpesponding error message based on error code
     * @param errorCode
     * @return
     */
    public Response genErrorResponse(int errorCode){
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
            default:
                errorCode = ERR_UNKNOWN;
                msg = "unknown error";
        }
        responseEntity.failed(errorCode,msg);
        Response response = Response.status(Response.Status.BAD_REQUEST)
                .entity(responseEntity)
                .build();
        return response;
    }
    /**
     * Generate corrpesponding successful message
     * @return Success Response
     */
    public Response genSuccResponse(){
        BaseResponse responseEntity = new BaseResponse();
        responseEntity.successful("algorithm upload successfully");

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
