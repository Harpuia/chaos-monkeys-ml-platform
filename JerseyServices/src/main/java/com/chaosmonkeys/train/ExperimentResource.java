package com.chaosmonkeys.train;

import com.chaosmonkeys.DTO.BaseResponse;
import com.chaosmonkeys.Utilities.FileUtils;
import com.chaosmonkeys.Utilities.StringUtils;
import com.chaosmonkeys.Utilities.db.DbUtils;
import com.chaosmonkeys.dao.Algorithm;
import com.chaosmonkeys.dao.Dataset;
import com.chaosmonkeys.dao.Experiment;
import com.chaosmonkeys.dao.Task;
import com.chaosmonkeys.train.dto.ExperimentDto;
import com.chaosmonkeys.train.task.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;

/**
 * Resource for experiment
 * run/cancel
 */
@Path("/services/exp")
public class ExperimentResource {

    // Success Code
    public static final int CHECK_SUCCESS = 0;
    // Error Code
    public static final int ERR_BLANK_PARAMS = 301;
    public static final int ERR_DUPLICATE_EXP_RECORD = 302; // found duplicated experiment records in database
    public static final int ERR_EXP_RECORD_NOT_FOUND = 303;
    public static final int ERR_WRONG_TASK_TYPE = 304;
    public static final int ERR_INVALID_RES_PATH = 305;
    public static final int ERR_ALREADY_FINISHED = 306;
    public static final int ERR_UNKNOWN = 399;

    // reference to the task manager
    private TrainingTaskManager taskManager;

    @POST
    @Path("/start")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startExperiment(ExperimentDto experimentDto){
        int validCode = 0;
        String expName = experimentDto.getExperiment_name();
        // TODO: extract following checking logic to one single method, or maybe impossible..
        // no experiment name
        if(StringUtils.isBlank(expName)){
            validCode = ERR_BLANK_PARAMS;
            return genErrorResponse(validCode);
        }
        DbUtils.openConnection();
        // query from database to get experiment information
        List<Experiment> experiments = Experiment.where("experiment_name = ?", expName);
        if(experiments.size() != 1){
            if(0 == experiments.size()){
                validCode = ERR_EXP_RECORD_NOT_FOUND;
            }else{
                validCode = ERR_DUPLICATE_EXP_RECORD;
            }
            return genErrorResponse(validCode);
        }
        Experiment experiment = experiments.get(0);
        // find the related task (1-n relationship)
        Task task = experiment.parent(Task.class);
        // type
        String taskType = task.getTaskType();
        if(!taskType.equals(Constants.TYPE_TRAIN)){
            validCode = ERR_WRONG_TASK_TYPE;
            return genErrorResponse(validCode);
        }
        // find the related algorithm
        Algorithm algr = task.parent(Algorithm.class);
        Dataset dataset = task.parent(Dataset.class);
        // construct ResourceInfo
        String datasetPath = dataset.getDatasetPath();
        String algrPath = algr.getAlgorithmPath();
        String algrLanguage =algr.getAlgorithmLanguage();
        // close connection
        DbUtils.closeConnection();
        File datasetFolder = new File(datasetPath);
        File algrFolder = new File(algrPath);
        File workspaceFolder = FileUtils.createTempDir();
        ResourceInfo resInfo = new ResourceInfo.ResourceInfoBuilder().
                setDatasetFolder(datasetFolder).
                setAlgorithmFolder(algrFolder).
                setWorkspaceFolder(workspaceFolder).build();
        if(! resInfo.checkRequirement(TaskType.TRAIN)){
            //TODO: delete temp folder, clean up
            validCode = ERR_INVALID_RES_PATH;
            return genErrorResponse(validCode);
        }
        // construct TaskInfo
        TrainingTaskInfo trainingTaskInfo= new TrainingTaskInfo(expName, algrLanguage, resInfo);
        // get task manager instance and submit the task
        TrainingTaskManager.INSTANCE.submitTask(trainingTaskInfo);

        return genSuccResponse();
    }

    @POST
    @Path("/stop")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopExperiment(ExperimentDto experimentDto){
        //TODO: add proper error message to front end
        int validCode = 0;
        String expName = experimentDto.getExperiment_name();
        // TODO: extract following checking logic to one single method, or maybe impossible..
        // no experiment name
        if(StringUtils.isBlank(expName)){
            validCode = ERR_BLANK_PARAMS;
            return genErrorResponse(validCode);
        }
        DbUtils.openConnection();
        // query from database to get experiment information
        List<Experiment> experiments = Experiment.where("experiment_name = ?", expName);
        if(experiments.size() != 1){
            if(0 == experiments.size()){
                validCode = ERR_EXP_RECORD_NOT_FOUND;
            }else{
                validCode = ERR_DUPLICATE_EXP_RECORD;
            }
            return genErrorResponse(validCode);
        }
        Experiment experiment = experiments.get(0);
        String experimentState = experiment.getString("last_status");
        DbUtils.closeConnection();
        //TODO prevent experiment state is null because of test data
        if (!TaskState.isFinished(experimentState)){
            getTaskManager().cancelTaskByExperimentName(expName);
            return genSuccResponseWithMsg("Experiment cancelled");
        }else{
            validCode = ERR_ALREADY_FINISHED;
            return genErrorResponse(validCode);
        }
    }
    //--------------------------------------------------------------------

    /**
     * Get an instance of training task manager
     * @return
     */
    private TrainingTaskManager getTaskManager(){
        if(null == taskManager){
            taskManager = TrainingTaskManager.INSTANCE;
        }
        return taskManager;
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
                msg = "Experiment name is not provided in your request";
                break;
            case(ERR_INVALID_RES_PATH):
                msg = "Resources path stored in system is invalid";
            case(ERR_ALREADY_FINISHED):
                msg = "The experiment has already finished";
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
        responseEntity.successful("Your experiment has been submitted to run");

        Response response = Response.ok()
                .entity(responseEntity)
                .build();
        return response;
    }

    /**
     * Generate corrpesponding successful message
     * @return Success Response
     */
    public Response genSuccResponseWithMsg(String msg){
        BaseResponse responseEntity = new BaseResponse();
        responseEntity.successful(msg);

        Response response = Response.ok()
                .entity(responseEntity)
                .build();
        return response;
    }
}
