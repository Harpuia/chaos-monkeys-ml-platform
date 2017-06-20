package com.chaosmonkeys.inputservice;

/**
 * DTO for InputService,
 * including IP, name, description and InputServiceStatus
 */
public class InputServiceStatusInfo {
    private String IP;
    private String Status;
    private InputServiceWorkState InputServiceStatus;


    public InputServiceStatusInfo(){}

    public InputServiceStatusInfo(String IP, String status, InputServiceWorkState inputServiceStatus) {
        this.IP = IP;
        Status = status;
        InputServiceStatus = inputServiceStatus;
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

    public InputServiceWorkState getInputServiceStatus() {
        return InputServiceStatus;
    }

    public void setInputServiceStatus(InputServiceWorkState inputServiceStatus) {
        InputServiceStatus = inputServiceStatus;
    }
}
