package com.chaosmonkeys.train.task;

import com.chaosmonkeys.Utilities.FileUtils;
import com.chaosmonkeys.Utilities.Logger;
import com.chaosmonkeys.train.task.interfaces.OnTaskUpdateListener;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * Training task runner
 */
public class TrainingTask extends AbsTask{

    private TrainingTaskInfo taskInfo;

    public TrainingTask(TrainingTaskInfo trainTaskInfo, OnTaskUpdateListener listener){
        this.taskInfo = trainTaskInfo;
        this.setTaskUpdateListener(listener);
    }

    @Override
    protected ExecutorService getExecutorService() {
        return super.getExecutorService();
    }

    @Override
    protected void initialize() {
        // evaluate the current state, backward is not permitted
        if(isIDLE()){
            // set the initializing sign
            taskUpdateListener.onInit(taskInfo.getTaskId());
            // copy files to workspace
            final ExecutorService executorService = getExecutorService();
            executorService.submit(initTaskPerformer);
        }
    }

    @Override
    protected void performTask() {

    }

    @Override
    protected void cleanUp() {

    }

    @Override
    protected void cancelWorks() {

    }

    //** Task Performers  ----------------------------------------------------------------------
    /**
     * Runnable for checking
     */
    private Runnable initTaskPerformer = () -> {
        final ResourceInfo res = taskInfo.getResourceInfo();
        try {
            // copy algorithm content to workspace
            FileUtils.copyDirectory(res.getAlgorithmFolder(),res.getWorkspaceFolder());
            File tmpInputFolder = new File(res.getWorkspaceFolder(), "input");
            // copy data set content to workspace input folder
            FileUtils.copyDirectory(res.getDatasetFolder(), tmpInputFolder);
            // invoke initialized
            taskUpdateListener.onInitialized(taskInfo.getTaskId());
        } catch (IOException e) {
            Logger.Error("Initializing experiment error when copying resource to temp workspace");
            e.printStackTrace();
            //TODO: trigger error and clean up

        }
    };

    private Runnable rTrainingPerformer = () -> {

    };

    //** Utils -----------------------------------------------------------------------------------

    public String getTaskId(){
        return taskInfo.getTaskId();
    }

    public TrainingTaskInfo getTaskInfo() {
        return taskInfo;
    }
}
