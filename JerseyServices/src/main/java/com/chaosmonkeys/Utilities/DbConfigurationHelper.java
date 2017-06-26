package com.chaosmonkeys.Utilities;

import com.chaosmonkeys.DTO.ConfigInfo;
import com.chaosmonkeys.DTO.DbConfigInfo;
import org.ini4j.Wini;
import java.io.File;
import java.io.IOException;
/**
 * Created by rachel on 6/23/17.
 */
public class DbConfigurationHelper {
    /**
     * load db connection information
     *
     * @param filename
     * @throws IOException
     */
    public static DbConfigInfo loadBasicInfo(String filename) {

        Wini ini = null;
        try {
            ini = new Wini(new File(filename));
        } catch (IOException e) {
            Logger.SaveLog(LogType.Exception, "EXCEPTION: Cannot read database configuration file "+ filename);
        }
        DbConfigInfo basicInfo = new DbConfigInfo();
        basicInfo.setHost(ini.get("info", "host", String.class));
        basicInfo.setDbName(ini.get("info", "dbName", String.class));
        basicInfo.setUserName(ini.get("info", "userName", String.class));
        basicInfo.setPassword(ini.get("info", "password", String.class));
        basicInfo.setPort(ini.get("info", "port", String.class));
        return basicInfo;
    }

}

