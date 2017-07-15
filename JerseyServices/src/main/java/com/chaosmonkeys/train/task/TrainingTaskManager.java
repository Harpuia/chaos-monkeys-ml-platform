package com.chaosmonkeys.train.task;


import com.chaosmonkeys.Utilities.db.DbUtils;
import com.chaosmonkeys.dao.Experiment;
import com.chaosmonkeys.train.task.interfaces.OnTaskUpdateListener;

import java.sql.Timestamp;
import java.time.Instant;
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
    // map experiment name to task ID because the frontend would like to use experiment name as identifier
    private Map<String, String> taskNameIdMap = new ConcurrentHashMap<>();

    public boolean submitTask(BaseTaskInfo taskInfo){
        TrainingTaskInfo trainTaskInfo = (TrainingTaskInfo) taskInfo;
        TrainingTask trainingTask = new TrainingTask(trainTaskInfo, mOnTaskUpdateListener);
        taskMap.put(trainTaskInfo.getTaskId(), trainingTask);
        taskNameIdMap.put(trainTaskInfo.getExperimentName(), trainTaskInfo.getTaskId());
        trainingTask.initialize();

        // TODO: discover some error case which may return false
        return true;
    }
    public boolean cancelTask(String taskId){
        return false;
    }


    public boolean cancelTaskByExperimentName(String experimentName){
        TrainingTask trainTask  = taskMap.get(experimentName);
        trainTask.cancelWorks();
        return true;
    }

    OnTaskUpdateListener mOnTaskUpdateListener = new OnTaskUpdateListener() {
        // start initializing
        @Override
        public void onInit(String taskId) {
            TrainingTask task = taskMap.get(taskId);
            TrainingTaskInfo trainTaskInfo = task.getTaskInfo();
            // set initializing status and update database record
            DbUtils.openConnection();
            task.setState(TaskState.INITIALIZING);
            Experiment exp = DbUtils.getExperimentModelByName(trainTaskInfo.getExperimentName());
            exp.set("last_status",TaskState.INITIALIZING.StringValue());
            //TODO: handle locale problem in the future
            Timestamp nowTime = Timestamp.from(Instant.now());
            exp.setTimestamp("last_updated",nowTime);
            DbUtils.closeConnection();
        }
        // task workspace has been initialized, the manager should update record and let the task start if available
        @Override
        public void onInitialized(String taskId) {
            TrainingTask task = taskMap.get(taskId);
            TrainingTaskInfo trainTaskInfo = task.getTaskInfo();
            // set initializing status and update database record
            DbUtils.openConnection();
            task.setState(TaskState.INITIALIZED);
            Experiment exp = DbUtils.getExperimentModelByName(trainTaskInfo.getExperimentName());
            exp.set("last_status",task.getState().StringValue());
            //TODO: handle locale problem in the future
            Timestamp nowTime = Timestamp.from(Instant.now());
            exp.setTimestamp("last_updated",nowTime);
            DbUtils.closeConnection();
            // task is permitted to start
            task.performTask();
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
