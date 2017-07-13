package com.chaosmonkeys.train.task;

/**
 * Interface for task manager
 */
public interface TaskManager {

    public boolean submitTask(BaseTaskInfo taskInfo);
    public boolean cancelTask(String taskId);
    public boolean cancelTaskByExperimentName(String experimentName);
    public boolean cancelTaskByExperimentId(String experimentName);

}
