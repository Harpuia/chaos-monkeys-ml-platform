package com.chaosmonkeys.dao;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.DbName;

/**
 * class used to manipulate database using ActiveJDBC ORM
 */
@DbName("ConfigurationDatabase")
public class Dataset extends Model {
    private volatile String dataSetPath;

    /**
     * Query once and retur dataset path
     * @return dataset file path
     */
    public String getDatasetPath(){
        if(null == dataSetPath){
            dataSetPath = getString("path");
        }
        return dataSetPath;
    }

    public Dataset setDatasetName(String name){
        set("name",name);
        return this;
    }

    public Dataset setDatasetPath(String path){
        set("path", path);
        return this;
    }

    public Dataset setDescription(String description){
        set("description", description);
        return this;
    }
    public Dataset setFormat(String format){
        set("format", format);
        return this;
    }

}
