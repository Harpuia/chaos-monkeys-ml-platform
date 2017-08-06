package com.chaosmonkeys.algrinputservice.DTO;

/**
 * DTO for algorithmInputService heartbeats client,
 * including IP, name, description and status
 */
public class AlgorithmInputServiceStatusInfo {
    //Basic info of the jersey service
    private String IP;
    private String Type;
    private String Name;
    private String Description;
    private AlgorithmInputServiceWorkState status;


    public AlgorithmInputServiceStatusInfo() {
    }

    public AlgorithmInputServiceStatusInfo(String IP, AlgorithmInputServiceWorkState status) {
        this.IP = IP;
        this.status = status;
    }

    public AlgorithmInputServiceStatusInfo(String IP, String type, String name, String description, AlgorithmInputServiceWorkState status) {
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

    public AlgorithmInputServiceWorkState getStatus() {
        return status;
    }

    public void setStatus(AlgorithmInputServiceWorkState status) {
        this.status = status;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }
}
