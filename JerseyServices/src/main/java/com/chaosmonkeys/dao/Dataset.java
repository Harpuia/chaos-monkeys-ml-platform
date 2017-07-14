package com.chaosmonkeys.dao;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.DbName;

@DbName("ConfigurationDatabase")
public class Dataset extends Model {
    private volatile String dataSetPath;

    /**
     * Query once and retur dataset path
     * @return
     */
    public String getDatasetPath(){
        if(null == dataSetPath){
            dataSetPath = getString("path");
        }
        return dataSetPath;
    }

}
