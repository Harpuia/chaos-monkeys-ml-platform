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

    public Algorithm setAlgorithmName(String name){
        set("name",name);
        return this;
    }

    public Algorithm setAlgorithmPath(String path){
        set("path",path);
        return this;
    }

    public Algorithm setAlgorithmDescription(String description){
        set("description", description);
        return this;
    }
    public Algorithm setAlgorithmLanguage(String language){
        set("language",language);
        return this;
    }
    public Algorithm setAlgorithmUserName(String userName){
        set("username",userName);
        return this;
    }

}
