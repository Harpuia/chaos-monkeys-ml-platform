package com.chaosmonkeys.train.task;

/**
 * Information about an execution task
 */
public class ExecutionTaskInfo extends BaseTaskInfo{
    /**
     * Input workspace and other path information
     * make sure they are valid before constructing
     * the task info object
     */
    public ExecutionTaskInfo(){
        super(TaskType.PREDICATE);
    }

    public ExecutionTaskInfo(String name, String language, ResourceInfo resInfo){
        super(TaskType.PREDICATE, name, language, resInfo);
    }

}

