package com.chaosmonkeys.train.task;

import com.chaosmonkeys.Utilities.db.DbUtils;
import com.chaosmonkeys.dao.Experiment;
import com.chaosmonkeys.train.task.interfaces.OnTaskUpdateListener;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Execution/Prediction task manager used for handling task callback
 * adding task, invoking updating database and provide proper info for
 * heartbeats (maybe)
 */
public enum ExecutionTaskManager implements  TaskManager{
    // singleton
    INSTANCE;

    AtomicInteger runningTaskNum = new AtomicInteger();
    // taskId -> Task
    private Map<String, ExecutionTask> taskMap = new ConcurrentHashMap<>();
    // map experiment name to task ID because the frontend would like to use experiment name as identifier
    private Map<String, String> taskNameIdMap = new ConcurrentHashMap<>();


    @Override
    public boolean submitTask(BaseTaskInfo taskInfo) {
        ExecutionTaskInfo predictTaskInfo = (ExecutionTaskInfo) taskInfo;
        ExecutionTask predictTask = new ExecutionTask(predictTaskInfo, mOnTaskUpdateListener);
        // update the reference of experiment name -> taskId -> task
        taskMap.put(predictTaskInfo.getTaskId(), predictTask);
        taskNameIdMap.put(predictTaskInfo.getExperimentName(), predictTaskInfo.getTaskId());
        predictTask.initialize();

        return true;
    }

    @Override
    public boolean cancelTask(String taskId) {
        ExecutionTask predictTask  = taskMap.get(taskId);
        predictTask.cancelWorks();
        return true;
    }

    @Override
    public boolean cancelTaskByExperimentName(String experimentName) {
        String taskId = taskNameIdMap.get(experimentName);
        ExecutionTask predictTask  = taskMap.get(taskId);
        predictTask.cancelWorks();
        return true;
    }

    //---------------
    /**
     * Update experiment task status in database
     * @param taskId abstract task ID (the UUID one)
     * @param state new task state
     */
    public ExecutionTask updateTaskStatus(String taskId, TaskState state){
        ExecutionTask task = taskMap.get(taskId);
        BaseTaskInfo trainTaskInfo = task.getTaskInfo();
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
        if(state == TaskState.ERROR){
            String errorMsg = task.getErrorMsg();
            exp.set("error_message",errorMsg);
        }
        //TODO: add error handler for connection error
        exp.save();
        DbUtils.closeConnection();
        return task;
    }

    /**
     * Listener for monitoring and controling the lifecycle of tasks
     */
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
            ExecutionTask task = updateTaskStatus(taskId, TaskState.INITIALIZED);
            // task is permitted to run
            task.performTask();
        }

        @Override
        public void onStarted(String taskId) {
            updateTaskStatus(taskId, TaskState.STARTED);
        }

        @Override
        public void onCancelled(String taskId) {
            updateTaskStatus(taskId, TaskState.CANCELLED);
            runningTaskNum.decrementAndGet();
        }

        @Override
        public void onSuccess(String taskId) {
            ExecutionTask task = updateTaskStatus(taskId, TaskState.SUCCESS);
            runningTaskNum.decrementAndGet();
            task.cleanUp();
        }

        @Override
        public void onError(Throwable ex, String taskId, String errorMessage) {
            updateTaskStatus(taskId, TaskState.ERROR);
            runningTaskNum.decrementAndGet();
        }
    };

    public int getRunningTaskNum(){
        return runningTaskNum.get();
    }
}
