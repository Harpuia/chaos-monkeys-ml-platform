package com.chaosmonkeys.train;

import com.chaosmonkeys.DTO.BaseResponse;
import com.chaosmonkeys.Utilities.FileUtils;
import com.chaosmonkeys.Utilities.Logger;
import com.chaosmonkeys.Utilities.StringUtils;
import com.chaosmonkeys.Utilities.db.DbUtils;
import com.chaosmonkeys.dao.*;
import com.chaosmonkeys.train.dto.ExperimentDto;
import com.chaosmonkeys.train.task.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
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
    public static final int ERR_INVALID_EXP_STATUS = 307;
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
        // task type checking, only TYPE_TRAIN or TYPE_EXECUTION is permitted
        String taskType = task.getTaskType();
        if(!taskType.equals(Constants.TYPE_TRAIN) && !taskType.equals(Constants.TYPE_EXECUTION)){
            validCode = ERR_WRONG_TASK_TYPE;
            return genErrorResponse(validCode);
        }
        boolean isPredictionTask = taskType.equals(Constants.TYPE_EXECUTION) ;
        // find the related algorithm
        // TODO: I guess if there are corrupt data stored in the database, this will crash because of related info cannot be found
        Algorithm algr = task.parent(Algorithm.class);
        Dataset dataset = task.parent(Dataset.class);

        // construct ResourceInfo
        String datasetPath = dataset.getDatasetPath();
        String algrPath = algr.getAlgorithmPath();
        String algrLanguage =algr.getAlgorithmLanguage();

        File datasetFile = new File(datasetPath);
        File algrFolder = new File(algrPath);
        File workspaceFolder = FileUtils.createTempDir();
        ResourceInfo.ResourceInfoBuilder resInfoBuilder = new ResourceInfo.ResourceInfoBuilder().
                setDatasetFolder(datasetFile).
                setAlgorithmFolder(algrFolder).
                setWorkspaceFolder(workspaceFolder);
        if(isPredictionTask){
            PredictionModel predictModel= task.parent(PredictionModel.class);
            String modelPath = predictModel.getString("path");
            File modelFolder = new File(modelPath);
            resInfoBuilder.setModelFolder(modelFolder);
        }
        // close connection
        DbUtils.closeConnection();

        ResourceInfo resInfo = resInfoBuilder.build();
        if(! resInfo.checkRequirement(taskType)){
            FileUtils.deleteQuietly(workspaceFolder);   // delete tmp workspace folder
            validCode = ERR_INVALID_RES_PATH;
            return genErrorResponse(validCode);
        }
        validCode = CHECK_SUCCESS;
        switch (taskType){
            case(Constants.TYPE_TRAIN):
                Logger.Info("Received training experiment request");
                // construct TaskInfo
                TrainingTaskInfo trainingTaskInfo= new TrainingTaskInfo(expName, algrLanguage, resInfo);
                // get task manager instance and submit the task
                TrainingTaskManager.INSTANCE.submitTask(trainingTaskInfo);
                break;
            case(Constants.TYPE_EXECUTION):
                Logger.Info("Received execution experiment request");
                ExecutionTaskInfo executionTaskInfo = new ExecutionTaskInfo(expName, algrLanguage, resInfo);
                ExecutionTaskManager.INSTANCE.submitTask(executionTaskInfo);
                break;
            default:
                Logger.Error("Unknown issue lead to unsupported task type when submitting experiment to run");
                validCode = ERR_WRONG_TASK_TYPE;
                break;
        }
        if(CHECK_SUCCESS != validCode ){
            return genErrorResponse(validCode);
        }

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
            Logger.Exception("Received stop request for an experiment but the request does not contain experiment name");
            validCode = ERR_BLANK_PARAMS;
            return genErrorResponse(validCode);
        }
        Logger.Info("Received stop request for experiment: " + expName);
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
        if ( TaskState.isValidStatus(experimentState) || !TaskState.isFinished(experimentState)){
            getTaskManager().cancelTaskByExperimentName(expName);
            return genSuccResponseWithMsg("Your experiment has been cancelled successfully");
        }else{
            if(TaskState.isValidStatus(experimentState)){
                validCode = ERR_INVALID_EXP_STATUS;
            }else{
                validCode = ERR_ALREADY_FINISHED;
            }
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
        //TODO: add proper error response for all unexpected error
        BaseResponse responseEntity = new BaseResponse();
        String msg;
        switch (errorCode){
            case(ERR_BLANK_PARAMS):
                msg = "Experiment name is not provided in your request";
                break;
            case(ERR_EXP_RECORD_NOT_FOUND):
                msg = "No such experiment stored in the system";
                break;
            case(ERR_WRONG_TASK_TYPE):
                msg = "Invalid associated task type";
                break;
            case(ERR_INVALID_RES_PATH):
                msg = "Resources path stored in system is invalid";
                break;
            case(ERR_ALREADY_FINISHED):
                msg = "The experiment has already finished";
                break;
            case(ERR_DUPLICATE_EXP_RECORD):
                msg = "Found duplicate experiment records in system. This is an issue related with backend database. Please contact administer";
                break;
            case(ERR_INVALID_EXP_STATUS):
                msg = "The experiment has an unexpected status now, cancelation cannot work, please contact administrator";
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
