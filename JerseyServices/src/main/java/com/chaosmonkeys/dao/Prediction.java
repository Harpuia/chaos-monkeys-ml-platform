package com.chaosmonkeys.dao;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.DbName;
import org.javalite.activejdbc.annotations.Table;

/**
 * ORM for Prediction table data
 */
@DbName("ConfigurationDatabase")
@Table("predictions`")
public class Prediction extends Model{
    public Prediction setPath(String path){
        set("path", path);
        return this;
    }

    public Prediction setModelName(String name){
        set("name", name);
        return this;
    }
    public Prediction setDescription(String description){
        set("description", description);
        return this;
    }
    public Prediction setProjectId(int id){
        set("project_id", id);
        return this;
    }
    public Prediction setExperimentId(int id){
        set("experiment_id", id);
        return this;
    }
}
