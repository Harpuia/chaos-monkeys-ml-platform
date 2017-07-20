package com.chaosmonkeys.train.dto;

public class ExecutionServiceStatus {
    private String IP;
    private String status;

    public ExecutionServiceStatus(String IP, String status) {
        this.IP = IP;
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
}
