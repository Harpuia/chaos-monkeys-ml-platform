package com.chaosmonkeys.train.task;

import com.chaosmonkeys.Utilities.LogType;
import com.chaosmonkeys.Utilities.Logger;
import com.chaosmonkeys.train.task.interfaces.OnTaskUpdateListener;

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
}
