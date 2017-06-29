package com.chaosmonkeys.DTO;

/**
 * BaseResponse which defines the
 * {@Code code, msg, success}
 * code : error code, default is 0 (no error)
 * msg : could be empty or properly detailed message
 * success: bool value to show the whether the request is succeed or not
 * <p>
 * ! Remember all backend service response DTO should inherit from this BaseResponse to provide these information
 */
public class BaseResponse {
    private String code;
    private String msg;
    private boolean success;

    public BaseResponse(String code, String msg, boolean success) {
        this.code = code;
        this.msg = msg;
        this.success = success;
    }

    public BaseResponse() {
    }

    public BaseResponse successful() {
        this.code = "0";
        this.msg = "request successful";
        this.success = true;
        return this;
    }
    public BaseResponse successful(String message) {
        this.code = "0";
        this.msg = message;
        this.success = true;
        return this;
    }
    public BaseResponse failed() {
        this.code = "-1";
        this.msg = "request failed";
        this.success = false;
        return this;
    }

    public BaseResponse failed(String message) {
        this.code = "-1";
        this.msg = message;
        this.success = false;
        return this;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
