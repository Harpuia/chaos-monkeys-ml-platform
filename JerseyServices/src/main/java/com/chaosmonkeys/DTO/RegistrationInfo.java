package com.chaosmonkeys.DTO;

/**
 * Information sent to the Coordination Service when registering.
 */
public class RegistrationInfo {
    private String IP;
    private String Type;
    private String Name;
    private String Description;
    private String Status;

    public RegistrationInfo() {

    }

    public RegistrationInfo(String IP, String type, String name, String description) {
        this.IP = IP;
        this.Type = type;
        this.Name = name;
        this.Description = description;
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

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
