package com.chaosmonkeys.dao;

/**
 * ORM for model
 */
public class Model extends org.javalite.activejdbc.Model {

    public Model setPath(String path){
        set("path", path);
        return this;
    }

    public Model setModelName(String name){
        set("name", name);
        return this;
    }
    public Model setDescription(String description){
        set("description", description);
        return this;
    }

}
