package com.chaosmonkeys.train.dto;

/**
 * DTO as heartbeats message
 */
public class ExecutionServiceStatus {
    private String IP;
    private String Type;
    private String Name;
    private String Description;
    private String status;

    public ExecutionServiceStatus(String IP, String status) {
        this.IP = IP;
        this.status = status;
    }

    public ExecutionServiceStatus(String IP, String type, String name, String description, String status) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
