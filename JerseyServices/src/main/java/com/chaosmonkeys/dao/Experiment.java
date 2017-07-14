package com.chaosmonkeys.dao;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.DbName;

@DbName("ConfigurationDatabase")
public class Experiment extends Model {
    private volatile String experiemntName;

    public String getExperimentName() {
        if(null ==experiemntName){
            experiemntName = getString("experiment_name");
        }
        return experiemntName;
    }
}
