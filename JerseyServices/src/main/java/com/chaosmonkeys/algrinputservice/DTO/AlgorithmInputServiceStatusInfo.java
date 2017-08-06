package com.chaosmonkeys.algrinputservice.DTO;

/**
 * DTO for algorithmInputService heartbeats client,
 * including IP, name, description and status
 */
public class AlgorithmInputServiceStatusInfo {
    private String IP;
    private AlgorithmInputServiceWorkState status;

    public AlgorithmInputServiceStatusInfo() {
    }

    public AlgorithmInputServiceStatusInfo(String IP, AlgorithmInputServiceWorkState status) {
        this.IP = IP;
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
}
