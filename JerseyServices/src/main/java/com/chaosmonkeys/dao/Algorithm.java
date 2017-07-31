package com.chaosmonkeys.dao;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.DbName;

/**
 * Algorithm entity
 */
@DbName("ConfigurationDatabase")
public class Algorithm extends Model {
    private volatile String algorithmPath;
    private volatile String algorithmLanguage;
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

    /**
     * Query once and return algorithm language
     * @return
     */
    public String getAlgorithmLanguage() {
        if(null == algorithmLanguage){
            algorithmLanguage = getString("language");
        }
        return algorithmLanguage;
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

}
