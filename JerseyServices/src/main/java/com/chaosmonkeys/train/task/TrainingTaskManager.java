package com.chaosmonkeys.train.task;


import com.chaosmonkeys.dao.Experiment;
import com.chaosmonkeys.train.task.interfaces.OnTaskUpdateListener;

/**
 * Traning task manager used for handling task callback
 * adding task, invoking updating database and provide proper info for
 * heartbeats
 */

public enum TrainingTaskManager implements TaskManager{
    // singleton
    INSTANCE;

    /***
     * using common pattern to obtain an instance
     * @return
     */
    public TrainingTaskManager getInstance() {
        return TrainingTaskManager.INSTANCE;
    }

    OnTaskUpdateListener mOnTaskUpdateListener = new OnTaskUpdateListener() {

        @Override
        public void onWaiting(String taskId) {

        }

        @Override
        public void onInitialized(String taskId) {

        }

        @Override
        public void onStarted(String taskId) {

        }

        @Override
        public void onCancelled(String taskId) {

        }

        @Override
        public void onSuccess(String taskId) {

        }

        @Override
        public void onError(Throwable ex, String taskId) {

        }
    };

    /**
     * Create a new training task based on experiment infomation
     * @param experiment
     */
    public void submitTask(Experiment experiment){

    }

    public boolean submitTask(BaseTaskInfo taskInfo){
        return false;
    };
    public boolean cancelTask(String taskId){
        return false;
    };
    public boolean cancelTaskByExperimentName(String experimentName){
        return false;
    }
    public boolean cancelTaskByExperimentId(String experimentName){
        return false;
    }

}
