package com.chaosmonkeys.Utilities.db;


import com.chaosmonkeys.Utilities.Logger;
import com.chaosmonkeys.dao.Algorithm;
import com.chaosmonkeys.dao.Dataset;
import com.chaosmonkeys.dao.Experiment;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.validation.ValidationException;

import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.locks.ReentrantLock;

public final class DbUtils {

    // lock for managing connection
    private static ReentrantLock connectionLock = new ReentrantLock();
    // lock for managing logging db connection
    private static ReentrantLock logConnectionLock = new ReentrantLock();

    // JDBC connection properties
    private static String url;
    private static String logUrl;
    private static String username;
    private static String password;
    private static String driver;
    private static String dbName;
    private static String logDbName;
    private static ResourceBundle rb = ResourceBundle.getBundle("db.db-config");
    // using SSL for connection but turning off server verification
    private static String options = "?verifyServerCertificate=false&useSSL=true";

    // initialize the driver
    static{
        dbName = rb.getString("jdbc.dbName");
        url = rb.getString("jdbc.baseurl") + rb.getString("jdbc.dbName") + options;
        logUrl = rb.getString("jdbc.baseurl") + "logdatabase" + options;
        username = rb.getString("jdbc.username");
        password = rb.getString("jdbc.password");
        driver = rb.getString("jdbc.driver");
        logDbName = "logdatabase";      // extract to resources file
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * open ActiveJDBC connection when needed in one thread
     * this method only aimed to use the Configuration database
     * if you wanna use another database, refactor it or write a
     * new method
     */
    public static void openConnection(){
        connectionLock.lock();
        new DB(dbName).open(driver, url, username, password);
    }

    /**
     * Close ActiveJDBC connection in current thread
     */
    public static void closeConnection(){
        new DB(dbName).close();
        //TODO: place lock to another proper code
        connectionLock.unlock();
    }

    /**
     * open ActiveJDBC connection when needed in one thread
     * this method only aimed to use the Log database
     * if you wanna use another database, refactor it or write a
     * new method
     */
    public static void openLogConnection(){
        logConnectionLock.lock();
        new DB(logDbName).open(driver, logUrl, username, password);
    }

    /**
     * Close ActiveJDBC connection for LogDatabase in current thread
     */
    public static void closeLogConnection(){
        new DB(logDbName).close();
        //TODO: place lock to another proper code
        logConnectionLock.unlock();
    }

    /**
     * Insert the data sets information to the ConfigurationDatabase database.
     * @param dataName the target database name.
     * @param dataDescription the description regarding the input data.
     * @param path the input data path.
     * @param format the input data format.
     * @return false if insert error
     */
    public static boolean storeDataSet(String dataName, String dataDescription, String path, String format){
        openConnection();
        Dataset dataset = new Dataset()
                .setDatasetName(dataName)
                .setDescription(dataDescription)
                .setDatasetPath(path)
                .setFormat(format);
        // this operation will throw exception when the mapping model is wrong with database schema
        boolean inserted = insertDAO(dataset);
        closeConnection();
        return inserted;
    }

    /**
     * Store algorithm record in database
     * @param name algorithm name
     * @param description algorithm description
     * @param path
     * @param language
     * @return
     */
    public static boolean storeAlgorithm( String name, String description, String path, String language){
        openConnection();
        Algorithm algr= new Algorithm()
                .setAlgorithmName(name)
                .setAlgorithmDescription(description)
                .setAlgorithmPath(path)
                .setAlgorithmLanguage(language);
        // this operation will throw exception when the mapping model is wrong with database schema
        boolean inserted =  insertDAO(algr);
        closeConnection();
        return inserted;
    }

    private static boolean insertDAO(Model m){
        try {
            m.saveIt();
            return true;
        }catch (ValidationException validationEx){
            String errString="\nProblem with insert record: " + validationEx;
            Logger.Error(errString);
            return false;
        }
    }

    /**
     * This method would provide the experiment dao model
     * notice you should only use it when you can ensure the model existed in database
     * @param experimentName
     * @return Experiment DAO model with the specific experiment name
     */
    public static Experiment getExperimentModelByName(String experimentName){
        List<Experiment> expList = Experiment.where("experiment_name = ?", experimentName);
        Experiment exp = expList.get(0);
        return exp;
    }

}
