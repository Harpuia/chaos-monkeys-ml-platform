package com.chaosmonkeys.inputservice;

import com.chaosmonkeys.Utilities.LogType;
import com.chaosmonkeys.Utilities.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Class containing all possible service calls (API methods)
 */
@Path("/services")
public class InputService {
    // Constants operating with dataset upload and storage
    private static final String DATA_SET_PATH = "./Datasets/";
    private static final String EXECUTION_DATA = "Execution";
    private static final String TRAINING_DATA = "Predications";
    // Constants operating with service status
    private static final String STATUS_RUN = "RUNNING";
    private static final String STATUS_IDLE = "IDLE";

    // states variables
    public static String serviceStatus = "IDLE";
    public boolean hasDatasetFolder = false;
    // sets store datasets name that are under processing
    public static Set<String> uploadSet = new HashSet<>();
    public static Set<String> checkSet = new HashSet<>();

    // fake database
    List<DatasetRecord> datasets = new ArrayList<>();
    private static int IdCounter = 0;

    /**
     * Inner class as a fake dataset database
     */
    public class DatasetRecord {
        int id;
        int project_id;
        String path;

        public DatasetRecord(int pId, String storePath) {
            id = IdCounter;
            IdCounter += 1;
            this.project_id = pId;
            this.path = storePath;
        }
    }


    /**
     * Handling upload request and store file under dataset folder
     *  using multipart form
     * @param fileInputStream
     * @param fileMetaData
     * @param dataName
     * @param userId
     * @param projectId
     * @return
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@FormDataParam("file") InputStream fileInputStream,
                               @FormDataParam("file") FormDataContentDisposition fileMetaData,
                               @FormDataParam("name") String dataName,
                               @FormDataParam("user_id") String userId,
                               @FormDataParam("project_id") int projectId) {
        refreshServiceState();
        String UPLOAD_PATH = DATA_SET_PATH;
        if (null != userId && !userId.equals("")) {
            Logger.SaveLog(LogType.Information, "Received upload request from" + userId);
        }
        //create Datasets folder if it does not exist yet
        File datasetFolder = createDatasetFolder();
        //TODO: determine it is execution or not, delete the folder if uploading fail
        // Open the direct parent folder
        File executionFolder = createNewFolderUnder(EXECUTION_DATA, datasetFolder);
        try {
            int read = 0;       // the total number of bytes read into the buffer
            byte[] bytes = new byte[1024];
            File targetFolder = createNewFolderUnder(dataName, executionFolder);
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
            // TODO: store file path in database
            DatasetRecord record = new DatasetRecord(projectId, targetFolder.getAbsolutePath());
            datasets.add(record);
        } catch (IOException e) {
            Logger.SaveLog(LogType.Exception, "Error while uploading file. Please try again !!");
            e.printStackTrace();
            //throw new WebApplicationException("Error while uploading file. Please try again !!");
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

    // Methods used to manage file and folder

    /**
     * Create datasets folder if it does not exist
     *
     * @return
     */
    public File createDatasetFolder() {
        File rootFolder = new File(DATA_SET_PATH);
        if (rootFolder.exists()) {
            hasDatasetFolder = true;
        } else {
            rootFolder.mkdir();
        }
        return rootFolder;
    }

    /**
     * Create a folder with specified name under parent folder
     *  return the File object of folder created
     * @param folderName
     * @param parentFolder
     * @return
     */
    public File createNewFolderUnder(String folderName, File parentFolder) {
        File folder = new File(parentFolder, folderName);
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder;
    }

}
