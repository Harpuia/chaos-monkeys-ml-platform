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

    private java.util.concurrent.atomic.AtomicInteger runningTaskNum;
    // taskId -> Task
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



    /**
     * Update experiment task status in database
     * @param taskId
     * @param state
     */
    public TrainingTask updateTaskStatus(String taskId, TaskState state){
        TrainingTask task = taskMap.get(taskId);
        TrainingTaskInfo trainTaskInfo = task.getTaskInfo();
        // set initializing status and update database record
        DbUtils.openConnection();
        task.setState(state);
        Experiment exp = DbUtils.getExperimentModelByName(trainTaskInfo.getExperimentName());
        exp.set("last_status",task.getState().StringValue());
        //TODO: handle locale problem in the future
        Timestamp nowTime = Timestamp.from(Instant.now());
        exp.setTimestamp("last_updated",nowTime);
        if(state == TaskState.INITIALIZING){
            exp.setTimestamp("start",nowTime);
        }
        if(state.value() > TaskState.STARTED.value()){
            exp.setTimestamp("end",nowTime);
        }
        //TODO: add error handler for connection error
        exp.save();
        DbUtils.closeConnection();
        return task;
    }

    OnTaskUpdateListener mOnTaskUpdateListener = new OnTaskUpdateListener() {
        // start initializing
        @Override
        public void onInit(String taskId) {
            updateTaskStatus(taskId, TaskState.INITIALIZING);
            runningTaskNum.incrementAndGet();
        }
        // task workspace has been initialized, the manager should update record and let the task start if available
        @Override
        public void onInitialized(String taskId) {
            TrainingTask task = updateTaskStatus(taskId, TaskState.INITIALIZED);
            // task is permitted to run
            task.performTask();
        }

        @Override
        public void onStarted(String taskId) {
            updateTaskStatus(taskId, TaskState.STARTED);
        }

        @Override
        public void onCancelled(String taskId) {
            TrainingTask task = updateTaskStatus(taskId, TaskState.CANCELLED);
            runningTaskNum.decrementAndGet();
        }

        @Override
        public void onSuccess(String taskId) {
            TrainingTask task = updateTaskStatus(taskId, TaskState.SUCCESS);
            runningTaskNum.decrementAndGet();
            task.cleanUp();
        }

        @Override
        public void onError(Throwable ex, String taskId) {
            updateTaskStatus(taskId, TaskState.ERROR);
            runningTaskNum.decrementAndGet();
        }
    };

    public int getRunningTaskNum(){
        return runningTaskNum.get();
    }

}
