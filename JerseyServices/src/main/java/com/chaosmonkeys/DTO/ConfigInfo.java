package com.chaosmonkeys.DTO;

/**
 * DTO for basic info loaded from the configuration file.
 */
public class ConfigInfo {
    private String serviceName;
    private String serviceType;
    private String serviceDescription;
    private String coordinationIP;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getCoordinationIP() {
        return coordinationIP;
    }

    public void setCoordinationIP(String coordinationIP) {
        this.coordinationIP = coordinationIP;
    }
}
