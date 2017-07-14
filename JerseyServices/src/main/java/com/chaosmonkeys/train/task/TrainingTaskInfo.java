package com.chaosmonkeys.train.task;

import com.chaosmonkeys.dao.Experiment;

import java.io.File;

/**
 * Information about a training task
 */
public class TrainingTaskInfo  {

    private String experimentName;

    private Experiment experimentModel;

    /**
     *
     * @param type
     */
    public TrainingTaskInfo(TaskType type){

    }

    /**
     * Input workspace and other path information
     * make sure they are valid before constructing
     * the task info object
     * @param type
     * @param datasetFolder
     * @param algorithmFolder
     * @param workspaceFolder
     */
    private TrainingTaskInfo(TaskType type, File datasetFolder, File algorithmFolder, File workspaceFolder){
        super(type, datasetFolder, algorithmFolder, workspaceFolder);
    }

}
