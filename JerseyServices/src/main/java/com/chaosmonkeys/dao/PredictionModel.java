package com.chaosmonkeys.dao;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.DbName;
import org.javalite.activejdbc.annotations.Table;

/**
 * ORM for model
 */
@DbName("ConfigurationDatabase")
@Table("`models`")
public class PredictionModel extends Model {

    public PredictionModel setPath(String path){
        set("path", path);
        return this;
    }

    public PredictionModel setModelName(String name){
        set("name", name);
        return this;
    }
    public PredictionModel setDescription(String description){
        set("description", description);
        return this;
    }
    public PredictionModel setExperimentId(int id){
        set("experiment_id", id);
        return this;
    }

}
