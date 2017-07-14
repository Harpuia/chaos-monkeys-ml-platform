package com.chaosmonkeys.train.task;

/**
 * Information about a training task
 */
public class TrainingTaskInfo extends BaseTaskInfo {

    /**
     * Input workspace and other path information
     * make sure they are valid before constructing
     * the task info object
     */
    public TrainingTaskInfo(){
        super(TaskType.TRAIN);
    }

    public TrainingTaskInfo(String name, ResourceInfo resInfo){
        super(TaskType.TRAIN, name, resInfo);
    }

}
