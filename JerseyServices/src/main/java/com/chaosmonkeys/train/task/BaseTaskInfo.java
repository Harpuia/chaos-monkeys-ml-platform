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

    /**
     * Make sure subclass invoke this super() constructor
     */
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

    public String getDatasetPath() {
        return datasetPath;
    }

    public void setDatasetPath(String datasetPath) {
        this.datasetPath = datasetPath;
    }

    public String getAlgorithmPath() {
        return algorithmPath;
    }

    public void setAlgorithmPath(String algorithmPath) {
        this.algorithmPath = algorithmPath;
    }

    public String getWorkspacePath() {
        return workspacePath;
    }

    public void setWorkspacePath(String workspacePath) {
        this.workspacePath = workspacePath;
    }

    public File getDatasetFolder() {
        return datasetFolder;
    }

    public void setDatasetFolder(File datasetFolder) {
        this.datasetFolder = datasetFolder;
    }

    public File getAlgorithmFolder() {
        return algorithmFolder;
    }

    public void setAlgorithmFolder(File algorithmFolder) {
        this.algorithmFolder = algorithmFolder;
    }

    public File getWorkspaceFolder() {
        return workspaceFolder;
    }

    public void setWorkspaceFolder(File workspaceFolder) {
        this.workspaceFolder = workspaceFolder;
    }
}
