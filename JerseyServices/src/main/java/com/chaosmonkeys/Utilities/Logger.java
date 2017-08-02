package com.chaosmonkeys.Utilities;

import com.chaosmonkeys.Launcher;
import com.chaosmonkeys.Utilities.db.DbUtils;
import com.chaosmonkeys.dao.ErrorLog;
import com.chaosmonkeys.dao.OpLog;
import org.javalite.activejdbc.Model;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Class that is used to log information and error messages.
 */
public class Logger {

    // service type
    private static String serviceType = "";
    // a list to store log info

    /**
     * Displaying log and store it to database
     * @param type
     * @param message
     */
    public static void SaveLog(LogType type, String message){
        if(serviceType.equals("")){
            serviceType = Launcher.serviceType;
        }
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(serviceType);
        strBuilder.append(" - ");
        strBuilder.append(message);
        String msg = strBuilder.toString();
        Timestamp nowTime = Timestamp.from(Instant.now());
        //TODO: Implement logic here
        System.out.println(msg);
        if(type == LogType.Error || type == LogType.Exception){
            DbUtils.openLogConnection();    // connections must be opened before any models
            ErrorLog errorLogModel = new ErrorLog();
            errorLogModel.set("timestamp", nowTime);
            errorLogModel.set("type", type.toString());
            errorLogModel.set("message", msg);
            errorLogModel.saveIt();
            DbUtils.closeLogConnection();
        }else{
            DbUtils.openLogConnection();
            OpLog opLogModel = new OpLog();
            opLogModel.set("timestamp", nowTime);
            opLogModel.set("type", type.toString());
            opLogModel.set("message", msg);
            opLogModel.saveIt();
            DbUtils.closeLogConnection();
        }
    }

    public static void setServiceType(String type){
        Logger.serviceType = type;
    }

    public static void Error(String message){
        SaveLog(LogType.Error, message);
    }
    public static void Exception(String message) { SaveLog(LogType.Exception, message);}
    public static void Info(String message){
        SaveLog(LogType.Information, message);
    }
    public static void Request(String message){SaveLog(LogType.Request, message);}
    public static void Response(String message){SaveLog(LogType.Response, message);}

    //TODO: if logging slow down the whole system, we will cache log items and flush them when it reaches maxium cache number to avoid frequently connecting
    private class LogItem{
        Timestamp timestamp;
        String log;
        LogType type;
    }
}