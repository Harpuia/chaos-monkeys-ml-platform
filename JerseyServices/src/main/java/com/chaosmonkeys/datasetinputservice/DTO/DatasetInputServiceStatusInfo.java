package com.chaosmonkeys.datasetinputservice.DTO;

/**
 * DTO for DatasetInputService,
 * including IP, name, description and InputServiceStatus
 */
public class DatasetInputServiceStatusInfo {
    private String IP;
    private String Type;
    private String Name;
    private String Description;
    private DatasetInputServiceWorkState status;

    public DatasetInputServiceStatusInfo() {
    }

    public DatasetInputServiceStatusInfo(String IP, DatasetInputServiceWorkState status) {
        this.IP = IP;
        this.status = status;
    }

    public DatasetInputServiceStatusInfo(String IP, String type, String name, String description, DatasetInputServiceWorkState status) {
        this.IP = IP;
        Type = type;
        Name = name;
        Description = description;
        this.status = status;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        this.Type = type;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        this.Description = description;
    }

    public DatasetInputServiceWorkState getStatus() {
        return status;
    }

    public void setStatus(DatasetInputServiceWorkState status) {
        this.status = status;
    }
}
