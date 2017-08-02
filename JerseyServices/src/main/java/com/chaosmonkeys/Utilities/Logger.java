package com.chaosmonkeys.Utilities;

/**
 * Class that is used to log information and error messages.
 */
public class Logger {
    private static String serviceType;
    public static void SaveLog(LogType type, String message){
        //TODO: Implement logic here
        System.out.println(message);
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
}