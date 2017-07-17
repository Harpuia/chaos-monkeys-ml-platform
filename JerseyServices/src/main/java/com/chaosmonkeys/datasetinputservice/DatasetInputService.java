package com.chaosmonkeys.datasetinputservice;

import com.chaosmonkeys.Utilities.FileUtils;
import com.chaosmonkeys.Utilities.LogType;
import com.chaosmonkeys.Utilities.Logger;
import com.chaosmonkeys.Utilities.db.DbUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Class containing all possible service calls (API methods)
 */
@Path("/services")
public class DatasetInputService {

    // Constants operating with service status
    private static final String STATUS_RUN = "RUNNING";
    private static final String STATUS_IDLE = "IDLE";

    // states variables
    public static String serviceStatus = STATUS_IDLE;

    // sets store data sets name that are under processing
    public static Set<String> uploadSet = new HashSet<>();
    public static Set<String> checkSet = new HashSet<>();

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
    public Response uploadFile(@FormDataParam("file") InputStream fileInputStream,
                               @FormDataParam("file") FormDataContentDisposition fileMetaData,
                               @FormDataParam("name") String dataName,
                               @FormDataParam("description") String dataDescription,
                               @FormDataParam("user_id") String userId,
                               @FormDataParam("project_id") String projectId,
                               @FormDataParam("format") String format){
        refreshServiceState();
        if (null != userId && !userId.equals("")) {
            Logger.SaveLog(LogType.Information, "INPUT: Received dataset upload request from" + userId);
        }
        //create Datasets folder if it does not exist yet
        File datasetFolder = FileUtils.createDatasetFolder();
        //TODO: determine it is execution or not, delete the folder if uploading fail
        // Open the direct parent folder
        File executionFolder = FileUtils.createNewFolderUnder(FileUtils.EXECUTION_DATA, datasetFolder);
        try {
            int read = 0;       // the total number of bytes read into the buffer
            byte[] bytes = new byte[1024];
            File targetFolder = FileUtils.createNewFolderUnder(dataName, executionFolder);
            String fileName = fileMetaData.getFileName();
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

            //insert data sets into database.
            boolean inserted = DbUtils.storeDataSet(userId,projectId,dataName,dataDescription,targetFolder.getAbsolutePath(),format);
            if(!inserted){
                //TODO: clean target folder and return error response
                Logger.Info("insert dataset "+ dataName +" failed");
            }

        } catch (IOException e) {
            Logger.SaveLog(LogType.Exception, "Error while uploading file. Please try again !!");
            e.printStackTrace();
        }
        refreshServiceState();
        return Response.ok("Data uploaded successfully").build();
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
