package com.chaosmonkeys.inputservice;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Class containing all possible service calls (API methods)
 */
@Path("/services")
public class InputService {
    // Constants operating with dataset upload and storage
    private static final String DATA_SET_PATH = "./Datasets/";
    private static final String EXECUTION_DATA = "Execution";
    private static final String TRAINING_DATA = "Predications";

    // states variables
    private boolean hasDatasetFolder = false;

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
     * Store file under dataset folder
     *
     * @param fileInputStream
     * @param fileMetaData
     * @param dataName
     * @param userId
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

        String UPLOAD_PATH = DATA_SET_PATH;
        if (null != userId && !userId.equals("")) {
            System.out.println("Received upload request from" + userId);
        }
        //create Datasets folder if it does not exist yet
        File datasetFolder = createDatasetFolder();
        //TODO: determine it is execution or not
        // Open the direct parent folder
        File executionFolder = createNewFolderUnder(EXECUTION_DATA, datasetFolder);
        try {
            int read = 0;       // the total number of bytes read into the buffer
            byte[] bytes = new byte[1024];
            File targetFolder = createNewFolderUnder(dataName, executionFolder);
            OutputStream out = new FileOutputStream(new File(targetFolder, fileMetaData.getFileName()));
            while ((read = fileInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
            // TODO: store file path in database
            DatasetRecord record = new DatasetRecord(projectId, targetFolder.getAbsolutePath());
            datasets.add(record);
        } catch (IOException e) {
            throw new WebApplicationException("Error while uploading file. Please try again !!");
        }
        return Response.ok("Data uploaded successfully!").build();
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
