package com.chaosmonkeys.train.task;

import com.chaosmonkeys.Utilities.FileUtils;

import java.io.File;

/**
 * Class used to store experiment required
 * resource info
 * paths are optional
 * Files are necessary to be initialize
 */
public class ResourceInfo {

    // Resource Folder Reference
    protected File datasetFolder;
    protected File algorithmFolder;
    protected File modelFolder;
    protected File workspaceFolder;


    //** Constructor ---------------------------------------------------------------------------


    public ResourceInfo(File datasetFolder, File algorithmFolder, File workspaceFolder) {
        this.datasetFolder = datasetFolder;
        this.algorithmFolder = algorithmFolder;
        this.workspaceFolder = workspaceFolder;
    }
    public ResourceInfo(File datasetFolder, File algorithmFolder, File modelFolder, File workspaceFolder) {
        this(datasetFolder, algorithmFolder, workspaceFolder);
        if(null != modelFolder){
            this.modelFolder = modelFolder;
        }
    }

    //** Check ---------------------------------------------------------------------------------

    /**
     * Check whether ResourceInfo are ready
     * @param type
     * @return
     */
    public boolean checkRequirement(TaskType type){

        boolean modelValid = false;

        boolean datasetValid = FileUtils.checkFolderValid(datasetFolder);
        boolean algrValid = FileUtils.checkFolderValid(algorithmFolder);
        boolean workspaceValid = FileUtils.checkFolderValid(workspaceFolder);

        if(type == TaskType.TRAIN){
            modelValid = true;
        }else{
            modelValid = FileUtils.checkFolderValid(modelFolder);
        }
        boolean valid = datasetValid && algrValid && workspaceValid && modelValid;
        return valid;
    }


    //** Builder -------------------------------------------------------------------------------
    public static class ResourceInfoBuilder {
        private File datasetFolder;
        private File algorithmFolder;
        private File workspaceFolder;
        private File modelFolder;

        public ResourceInfoBuilder setDatasetFolder(File datasetFolder) {
            this.datasetFolder = datasetFolder;
            return this;
        }

        public ResourceInfoBuilder setAlgorithmFolder(File algorithmFolder) {
            this.algorithmFolder = algorithmFolder;
            return this;
        }

        public ResourceInfoBuilder setWorkspaceFolder(File workspaceFolder) {
            this.workspaceFolder = workspaceFolder;
            return this;
        }
        public ResourceInfoBuilder setModelFolder(File modelFolder) {
            this.modelFolder = modelFolder;
            return this;
        }

        public ResourceInfo build() {
            return new ResourceInfo(datasetFolder, algorithmFolder, modelFolder, workspaceFolder);
        }
    }

    //** Getter and Setter ---------------------------------------------------------------------

    public File getDatasetFolder() {
        return datasetFolder;
    }

    public void setDatasetFolder(File datasetFolder) {
        this.datasetFolder = datasetFolder;
    }

    public File getAlgorithmFolder() {
        return algorithmFolder;
    }

    public void setAlgorithmFolder(File algorithmFolder) {
        this.algorithmFolder = algorithmFolder;
    }

    public File getWorkspaceFolder() {
        return workspaceFolder;
    }

    public void setWorkspaceFolder(File workspaceFolder) {
        this.workspaceFolder = workspaceFolder;
    }

    public File getModelFolder() {
        return modelFolder;
    }

    public void setModelFolder(File modelFolder) {
        this.modelFolder = modelFolder;
    }


}
