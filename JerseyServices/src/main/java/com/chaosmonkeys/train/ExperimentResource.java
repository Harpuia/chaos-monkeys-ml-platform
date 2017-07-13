package com.chaosmonkeys.train;

import com.chaosmonkeys.DTO.BaseResponse;
import com.chaosmonkeys.Utilities.StringUtils;
import com.chaosmonkeys.dao.Experiment;
import com.chaosmonkeys.train.dto.ExperimentDto;
import com.chaosmonkeys.train.task.TrainingTaskManager;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Resource for experiment
 * run/cancel
 */
@Path("services/exp")
public class ExperimentResource {

    // Success Code
    public static final int CHECK_SUCCESS = 0;
    // Error Code
    public static final int ERR_BLANK_PARAMS = 301;
    public static final int ERR_DUPLICATE_EXP_RECORD = 302; // found duplicated experiment records in database
    public static final int ERR_EXP_RECORD_NOT_FOUND = 303;
    public static final int ERR_FILE_BODYPART_MISSING = 304;
    public static final int ERR_UNZIP_EXCEPTION = 305;
    public static final int ERR_REQUIRED_FILE_MISSING = 306;
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
        // TODO: extract following checking logic to one single method
        // no experiment name
        if(StringUtils.isBlank(expName)){
            validCode = ERR_BLANK_PARAMS;
            return genErrorResponse(validCode);
        }
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
        // find the related task
        




        return null;
    }

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
}
