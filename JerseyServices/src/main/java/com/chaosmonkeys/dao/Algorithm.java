package com.chaosmonkeys.dao;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.DbName;

/**
 * Algorithm entity
 */
@DbName("ConfigurationDatabase")
public class Algorithm extends Model {
    private volatile String algorithmPath;

    /**
     * Query once and return algorithm folder  path
     * @return
     */
    public String getAlgorithmPath(){
        if(null == algorithmPath){
            algorithmPath = getString("path");
        }
        return algorithmPath;
    }
}
