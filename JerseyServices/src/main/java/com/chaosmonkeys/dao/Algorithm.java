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
     * @return algorithm path
     */
    public String getAlgorithmPath(){
        if(null == algorithmPath){
            algorithmPath = getString("path");
        }
        return algorithmPath;
    }

    /**
     * Query once and return algorithm language
     * @return algorithm language
     */
    public String getAlgorithmLanguage() {
        if(null == algorithmLanguage){
            algorithmLanguage = getString("language");
        }
        return algorithmLanguage;
    }

    /**
     * Set algorithm name and return itself
     * @param name algorithm name
     * @return current algorithm object
     */
    public Algorithm setAlgorithmName(String name){
        set("name",name);
        return this;
    }

    /**
     * set algorithm path and return itself
     * @param path algorithm storage folder path
     * @return  current algorithm object
     */
    public Algorithm setAlgorithmPath(String path){
        set("path",path);
        return this;
    }

    /**
     * set algorithm description
     * @param description algorithm description
     * @return  current algorithm object
     */
    public Algorithm setAlgorithmDescription(String description){
        set("description", description);
        return this;
    }

    /**
     * set algorithm development language
     * @param language language, such as R, Python
     * @return current algorithm object
     */
    public Algorithm setAlgorithmLanguage(String language){
        set("language",language);
        return this;
    }

}
