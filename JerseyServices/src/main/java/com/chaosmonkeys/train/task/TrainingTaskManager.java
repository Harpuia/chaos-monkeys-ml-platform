package com.chaosmonkeys.train.task;


import com.chaosmonkeys.Utilities.db.DbUtils;
import com.chaosmonkeys.dao.Experiment;
import com.chaosmonkeys.train.task.interfaces.OnTaskUpdateListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Traning task manager used for handling task callback
 * adding task, invoking updating database and provide proper info for
 * heartbeats
 */

public enum TrainingTaskManager implements TaskManager{
    // singleton
    INSTANCE;

    private Map<String, TrainingTask> taskMap = new ConcurrentHashMap<>();

    public boolean submitTask(BaseTaskInfo taskInfo){
        TrainingTaskInfo trainTaskInfo = (TrainingTaskInfo) taskInfo;
        TrainingTask trainingTask = new TrainingTask(trainTaskInfo, mOnTaskUpdateListener);
        taskMap.put(trainTaskInfo.getExperimentName(), trainingTask);
        // set initializing status and update database record
        DbUtils.openConnection();
        trainingTask.setState(TaskState.INITIALIZING);
        trainingTask.initialize();
        Experiment exp = DbUtils.getExperimentModelByName(trainTaskInfo.getExperimentName());
        exp.set("last_status","initializing");
        //TODO: set datetime
        DbUtils.closeConnection();
        return true;
    }
    public boolean cancelTask(String taskId){
        return false;
    };
    public boolean cancelTaskByExperimentName(String experimentName){
        return false;
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

}
