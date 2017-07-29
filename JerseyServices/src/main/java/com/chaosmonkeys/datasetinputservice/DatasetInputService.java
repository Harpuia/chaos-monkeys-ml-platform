package com.chaosmonkeys.datasetinputservice;

import com.chaosmonkeys.DTO.BaseResponse;
import com.chaosmonkeys.Utilities.*;
import com.chaosmonkeys.Utilities.db.DbUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Class containing all possible service calls (API methods)
 */
@Path("/services/datasets")
public class DatasetInputService {

    // Constants operating with service status
    private static final String STATUS_RUN = "RUNNING";
    private static final String STATUS_IDLE = "IDLE";

    // states variables
    public static String serviceStatus = STATUS_IDLE;

    // sets store data sets name that are under processing
    public static Set<String> uploadSet = new HashSet<>();
    public static Set<String> checkSet = new HashSet<>();

    // temporary support dataset format list
    private static final List<String> supportDatasetFormatList = Arrays.asList("CSV","JSON","csv","json");
    // Success Code
    public static final int CHECK_SUCCESS = 0;
    // Error Code
    public static final int ERR_BLANK_PARAMS = 101;
    public static final int ERR_UNSUPPORTED_FORMAT = 102;
    public static final int ERR_TRANSMISSION_FILE = 103;
    public static final int ERR_FILE_BODYPART_MISSING = 104;
    public static final int ERR_STORE_IN_DB = 105;
    public static final int ERR_INVALID_FILE = 106;
    public static final int ERR_INVALID_FILE_EXT = 107; // invalid file extension
    public static final int ERR_UNKNOWN = 199;

    /**
     * Handling upload request and store file under dataset folder
     *  using multipart form
     * @param fileInputStream
     * @param fileMetaData
     * @param dataName
     * @param dataDescription
     * @param userId
     * @param projectId
     * @param format
     * @return
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@FormDataParam("file") InputStream fileInputStream,
                               @FormDataParam("file") FormDataContentDisposition fileMetaData,
                               @FormDataParam("name") String dataName,
                               @FormDataParam("description") String dataDescription,
                               @FormDataParam("user_id") String userId,
                               @FormDataParam("project_id") String projectId,
                               @FormDataParam("format") String format){
        refreshServiceState();
        int validCode = CHECK_SUCCESS;
        if (null != userId && !userId.equals("")) {
            Logger.SaveLog(LogType.Information, "INPUT: Received dataset upload request from" + userId);
        }
        // check parameters
        validCode = detectUploadServiceParamError(fileInputStream, fileMetaData, dataName, userId, projectId, format);
        if(CHECK_SUCCESS != validCode){
            return genErrorResponse(validCode);
        }
        //create Datasets folder if it does not exist yet
        File datasetFolder = FileUtils.createDatasetFolder();
        //TODO: determine it is execution or not, delete the folder if uploading fail
        // Open the direct parent folder
        File executionFolder = FileUtils.createNewFolderUnder(FileUtils.EXECUTION_DATA, datasetFolder);
        File targetFolder = FileUtils.createNewFolderUnder(dataName, executionFolder);
        // + FileUtils.sanitizeFilename(fileMetaData.getFileName()) // if you wanna the original filename
        String dateTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MMM-d-HH-mm-ss", Locale.US)); // check locale when deploying
        String dataFileName = userId + "-" + dateTimeStr + "-" +  FileUtils.sanitizeFilename(dataName)  + "." + format.toLowerCase();
        boolean receiveSucess = receiveFile(fileInputStream, targetFolder, dataFileName);
        if(!receiveSucess){
            validCode = ERR_TRANSMISSION_FILE;
            return genErrorResponse(validCode);
        }
        File targetFile = new File(targetFolder, dataFileName);
        //insert data sets into database.
        boolean inserted = false;
        try {
            inserted = DbUtils.storeDataSet(userId,projectId,dataName,dataDescription, targetFile.getCanonicalPath(),format);
        } catch (IOException e) {
            Logger.Exception("Fail to store new datasets into database");
            e.printStackTrace();
        }
        if(!inserted){
            FileUtils.deleteQuietly(targetFolder);
            Logger.Info("insert dataset "+ dataName +" failed");
            validCode = ERR_STORE_IN_DB;
            return genErrorResponse(validCode);
        }
        refreshServiceState();
        return genSuccResponse();
    }


    /**
     * Receive dataset file
     * @param fileInputStream
     * @param targetFolder
     * @param fileName
     * @return
     */
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
            Logger.Exception("IOException happened while the server are receiving dataset file");
            Logger.Exception("Error while uploading datasets file.");
            FileUtils.deleteQuietly(targetFolder);
            Logger.Info("Target folder for datasets has been deleted");
            e.printStackTrace();
            return false;
        }
        refreshServiceState();
        return true;
    }

    /**
     * check whether all dataset parameters for uploading dataset
     * @param fileInputStream
     * @param fileMetaData
     * @param name
     * @param userId
     * @param projectId
     * @param format
     * @return
     */
    public int detectUploadServiceParamError(InputStream fileInputStream, FormDataContentDisposition fileMetaData, String name, String userId, String projectId, String format){
        // check all string parameters are not blank
        // check whether the bodypart/file content are attached
        if(null == fileInputStream || null == fileMetaData){
            return ERR_FILE_BODYPART_MISSING;
        }
        boolean isParamsValid = StringUtils.isNoneBlank(name, userId, projectId, format);
        if(!isParamsValid){
            return ERR_BLANK_PARAMS;
        }
        //TODO: dynamically load the supported format when receive supported language update notification from coordination service
        // or from database in the future, but it may involve compute cost for each upload/other services, so let's keep R, Python C++ and Matlab now
        boolean formatSupported = supportDatasetFormatList.stream().anyMatch(fileFormat -> fileFormat.equals(format));
        if(!formatSupported){
            Logger.Info("Unsupported datasets file format: " + format);
            return ERR_UNSUPPORTED_FORMAT;
        }
        boolean fileExtensionValid = DataVerification.isFileExtensionValidate(fileMetaData.getFileName());
        if(!fileExtensionValid){
            Logger.Info("Invalid datasets file name");
            return ERR_INVALID_FILE_EXT;
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
                msg = "Some required form fields from your input is empty or blank";
                break;
            case(ERR_UNSUPPORTED_FORMAT):
                msg = "The datasets file format you specified in the upload form is invalid";
                break;
            case(ERR_FILE_BODYPART_MISSING):
                msg = "The file bodypart is missing in the form. Please check whether you select file need to be uploaded or not";
                break;
            case(ERR_INVALID_FILE):
                msg = "The uploaded file is not a valid JSON/CSV file";
                break;
            case(ERR_STORE_IN_DB):
                msg = "The server is unable to store your file now due to DB issue, please contact the administrator";
                break;
            case(ERR_INVALID_FILE_EXT):
                msg = "The datasets file you uploaded has an invalid file extension";
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
        responseEntity.successful("Upload successfully");

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

    /**
     * Refresh the service status at first, then check whether it is idle or not
     * @return true if it is idld
     */
    public boolean isIdle(){
        refreshServiceState();
        if (this.serviceStatus.equals(STATUS_IDLE)){
            return true;
        }
        return false;
    }
}
