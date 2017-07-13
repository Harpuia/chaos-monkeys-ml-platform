package com.chaosmonkeys.datasetinputservice.DTO;

/**
 * DTO for DatasetInputService,
 * including IP, name, description and InputServiceStatus
 */
public class DatasetInputServiceStatusInfo {
    private String IP;
    private DatasetInputServiceWorkState status;

    public DatasetInputServiceStatusInfo() {
    }

    public DatasetInputServiceStatusInfo(String IP, DatasetInputServiceWorkState status) {
        this.IP = IP;
        this.status = status;
    }


    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public DatasetInputServiceWorkState getStatus() {
        return status;
    }

    public void setStatus(DatasetInputServiceWorkState status) {
        this.status = status;
    }
}
