package com.chaosmonkeys.train.task;

/**
 * Interface for task manager
 */
public interface TaskManager {

    boolean submitTask(BaseTaskInfo taskInfo);
    boolean cancelTask(String taskId);
    boolean cancelTaskByExperimentName(String experimentName);

}
