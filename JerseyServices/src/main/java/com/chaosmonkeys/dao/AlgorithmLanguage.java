package com.chaosmonkeys.dao;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.DbName;
import org.javalite.activejdbc.annotations.Table;

/**
 * DAO class used for mapping table algorithm_languages in configurationdatabase
 */
@DbName("ConfigurationDatabase")
@Table("algorithm_languages")
public class AlgorithmLanguage extends Model{
    private String language = "";

    /**
     * Get and cache algorithm language
     * @return one supported machine learning developing language
     */
    public String getLanguage(){
        if(language.equals("")){
            String tmp = getString("language");
            if(null != tmp){
                language = tmp;
            }
        }
        return language;
    }
}
