package com.chaosmonkeys.dao;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.BelongsTo;
import org.javalite.activejdbc.annotations.DbName;

@DbName("ConfigurationDatabase")
@BelongsTo(parent = PredictionModel.class, foreignKeyName = "model_id")
public class Task extends Model {

    /**
     * Get task type from database
     * @return
     */
    public String getTaskType(){
        return getString("type");
    }
}
