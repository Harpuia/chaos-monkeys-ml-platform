package com.chaosmonkeys.dao;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.DbName;
/**
 * class used to manipulate database using ActiveJDBC ORM
 */
@DbName("ConfigurationDatabase")
public class Experiment extends Model {
    private volatile String experimentName;

    public String getExperimentName() {
        if(null == experimentName){
            experimentName = getString("experiment_name");
        }
        return experimentName;
    }
}
