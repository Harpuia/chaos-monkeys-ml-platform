package com.chaosmonkeys.train.task;

import com.chaosmonkeys.Utilities.StringUtils;

import java.io.File;

/**
 * Base task information class
 * training or predication task should inherit this class
 * to specify the basic information of a task
 *
 * Jiawei Li
 */

public class BaseTaskInfo {

    /** Identifier for a task **/
    public final String TASK_ID;

    public final TaskType taskType;

    protected String experimentName;
    protected String experimentLanguage;    // Python or R

    private ResourceInfo resourceInfo;
    /**
     * Make sure subclass invoke this super() constructor
     */
    public BaseTaskInfo(TaskType type){
        this.TASK_ID = StringUtils.getUUID();
        this.taskType = type;
    }

    public BaseTaskInfo(TaskType type, String experimentName, String experimentLanguage, ResourceInfo resInfo){
        this(type); // assign an UUID as identifier
        this.experimentName = experimentName;
        this.experimentLanguage = experimentLanguage;
        this.resourceInfo = resInfo;
    }


    /**
     * Obtain the identifier of the task
     * @return task id (an instance of UUID string)
     */
    public String getTaskId(){
        return this.TASK_ID;
    }

    public ResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    public void setResourceInfo(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

    public String getExperimentName(){
        return experimentName;
    }
    public String getExperimentLanguage(){
        return experimentLanguage;
    }
}
