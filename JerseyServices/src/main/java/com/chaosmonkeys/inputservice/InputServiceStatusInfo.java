package com.chaosmonkeys.inputservice;

/**
 * DTO for InputService,
 * including IP, name, description and InputServiceStatus
 */
public class InputServiceStatusInfo {
    private String IP;
    private InputServiceWorkState status;

    public InputServiceStatusInfo() {
    }

    public InputServiceStatusInfo(String IP, InputServiceWorkState status) {
        this.IP = IP;
        this.status = status;
    }


    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public InputServiceWorkState getStatus() {
        return status;
    }

    public void setStatus(InputServiceWorkState status) {
        this.status = status;
    }
}
