package com.chaosmonkeys.inputservice.DTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Class that represents the working states of InputService
 * including: datasets is under checking, datasets is under uploading
 */
public class InputServiceWorkState {
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String status;

    private List<String> checking;
    private List<String> uploading;

    private String checkingList;
    private String uploadingList;


    // Constructors

    public InputServiceWorkState() {

    }

    public InputServiceWorkState(List<String> checking, List<String> uploading, String status) {
        this.status = status;
        this.checking = checking;
        this.uploading = uploading;
    }

    public InputServiceWorkState(Set<String> checking, Set<String> uploading, String status) {
        this.status = status;
        this.checking = new ArrayList<>(checking);
        this.uploading = new ArrayList<>(uploading);
        StringBuilder strBuilder = new StringBuilder();
        for (String s : checking) {
            strBuilder.append(s);
            strBuilder.append("; ");
        }
        checkingList = strBuilder.toString();
        StringBuilder sb = new StringBuilder();
        for (String s : uploading) {
            sb.append(s);
            sb.append(";");
        }
        uploadingList = sb.toString();
    }

    public List<String> getChecking() {
        return checking;
    }

    public void setChecking(List<String> checking) {
        this.checking = checking;
    }

    public List<String> getUploading() {
        return uploading;
    }

    public void setUploading(List<String> uploading) {
        this.uploading = uploading;
    }

    public String getCheckingList() {
        return checkingList;
    }

    public void setCheckingList(String checkingList) {
        this.checkingList = checkingList;
    }

    public String getUploadingList() {
        return uploadingList;
    }

    public void setUploadingList(String uploadingList) {
        this.uploadingList = uploadingList;
    }
}
