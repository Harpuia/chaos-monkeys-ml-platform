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

    protected String datasetPath;
    protected String algorithmPath;
    protected String workspacePath;

    protected File datasetFolder;
    protected File algorithmFolder;
    protected File workspaceFolder;

    public BaseTaskInfo(){
        this.TASK_ID = StringUtils.getUUID();
    }

    /**
     * Obtain the identifier of the task
     * @return
     */
    public String getTaskId(){
        return this.TASK_ID;
    }


}
