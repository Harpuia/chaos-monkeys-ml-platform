package com.chaosmonkeys.DTO;

/**
 * Status sent to the coordination service by the heartbeat.
 */
public class StatusInfo {
    private String IP;
    private String Status;

    public StatusInfo(String ip, String status) {
        this.IP = ip;
        this.Status = status;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        this.Status = status;
    }

}
