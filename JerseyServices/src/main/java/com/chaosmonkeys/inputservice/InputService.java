package com.chaosmonkeys.inputservice;

import com.chaosmonkeys.DTO.DbConfigInfo;
import com.chaosmonkeys.Utilities.DbConfigurationHelper;
import com.chaosmonkeys.Utilities.FileUtils;
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
import java.sql.DriverManager;
import java.sql.Connection;

/**
 * Class containing all possible service calls (API methods)
 */
@Path("/services")
public class InputService {

    // Constants operating with service status
    private static final String STATUS_RUN = "RUNNING";
    private static final String STATUS_IDLE = "IDLE";

    // states variables
    public static String serviceStatus = "IDLE";

    // sets store data sets name that are under processing
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
            Logger.SaveLog(LogType.Information, "Received upload request from" + userId);
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
            storeDataSets(userId,projectId,dataName,dataDescription,targetFolder.getAbsolutePath(),format);

        } catch (IOException e) {
            Logger.SaveLog(LogType.Exception, "Error while uploading file. Please try again !!");
            e.printStackTrace();
        }
        refreshServiceState();
        return Response.ok("Data uploaded successfully").build();
    }

    /**
     * Insert the data sets information to the ConfigurationDatabase database.
     * @param userId the user id.
     * @param projectId the project id.
     * @param dataName the target database name.
     * @param dataDescription the description regarding the input data.
     * @param path the input data path.
     * @param format the input data format.
     */
    public void storeDataSets(String userId, String projectId, String dataName,String dataDescription, String path,String format){

        Connection DBConn=null;
        boolean connectError=false;
        java.sql.Statement statement = null;        // SQL statement pointer
        try
        {
            //load JDBC driver class for MySQL
            Class.forName( "com.mysql.jdbc.Driver" );

            //get dbc onnection info from dbConfig.ini
            DbConfigInfo configInfo = DbConfigurationHelper.loadBasicInfo("dbConfig.ini");
            String userName = configInfo.getUserName();
            String password = configInfo.getPassword();
            String dbName=configInfo.getDbName();
            String SQLServerIP = configInfo.getHost();
            String port=configInfo.getPort();
            String sourceURL = "jdbc:mysql://" + SQLServerIP + ":"+port+"/"+dbName+"";

            //create a connection to the db
            DBConn = DriverManager.getConnection(sourceURL,userName,password);

        } catch (Exception e) {

            String errString =  "\nProblem connecting to database:: " + e;
            Logger.SaveLog(LogType.Exception,errString);
            connectError = true;

        }
        if(!connectError){
            try {
                statement=DBConn.createStatement();
                statement.executeUpdate("INSERT INTO datasets (user_id, project_id,path,name,description,format) "
                        +"VALUES ('"+userId+"', '"+projectId+"', '"+path+"', '"+dataName+"', '"+dataDescription+"', '"+format+"')");
                DBConn.close();
            }catch(Exception ex){
                String errString="\nProblem with insert:: " + ex;
                Logger.SaveLog(LogType.Exception,errString);
                connectError=true;
            }
        }
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
