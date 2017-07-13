package com.chaosmonkeys.train.dto;


public class ExperimentDto {
    // member variables
    private int id = -1;
    private int project_id = -1;
    private int task_id = -1;
    private String experiment_name;

    /**
     * constructor
     */
    // Must have no-argument constructor
    public ExperimentDto(){
    }

    public ExperimentDto(String experiment_name){
        this.experiment_name = experiment_name;
    }
    public ExperimentDto(int id, int project_id, int task_id, String experiment_name) {
        this.id = id;
        this.project_id = project_id;
        this.task_id = task_id;
        this.experiment_name = experiment_name;
    }


    /**
     * Getter and Setter
     * @return
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProject_id() {
        return project_id;
    }

    public void setProject_id(int project_id) {
        this.project_id = project_id;
    }

    public int getTask_id() {
        return task_id;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public String getExperiment_name() {
        return experiment_name;
    }

    public void setExperiment_name(String experiment_name) {
        this.experiment_name = experiment_name;
    }
}
