package com.chaosmonkeys.Utilities;

import com.chaosmonkeys.DTO.ConfigInfo;
import org.ini4j.Wini;
import java.io.File;
import java.io.IOException;

/**
 * Utility functions for loading configuration from file.
 */
public class ConfigurationHelper {
    /**
     * load service information
     *
     * @param filename
     * @throws IOException
     */
    public static ConfigInfo loadBasicInfo(String filename) {
        // no exception handling here
        Wini ini = null;
        try {
            ini = new Wini(new File(filename));
        } catch (IOException e) {
            Logger.SaveLog(LogType.Exception, "EXCEPTION: Cannot read configuratio file "+ filename);
        }
        ConfigInfo basicInfo = new ConfigInfo();
        basicInfo.setServiceName(ini.get("info", "name", String.class));
        basicInfo.setServiceType(ini.get("info", "type", String.class));
        basicInfo.setServiceDescription(ini.get("info", "description", String.class));
        basicInfo.setCoordinationIP(ini.get("coordination", "IP", String.class));
        return basicInfo;
    }
}
