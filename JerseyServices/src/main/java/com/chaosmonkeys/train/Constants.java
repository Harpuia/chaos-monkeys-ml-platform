package com.chaosmonkeys.train;

/**
 * Constants for training/execution tasks management
 */
public final class Constants {

    private Constants(){}   // restrict initialization
    //TODO: move the status to a global shared constant class
    // Constants operating with service status
    public static final String STATUS_RUN = "RUNNING";
    public static final String STATUS_IDLE = "IDLE";

    // task type columns in task table
    public static final String TYPE_TRAIN = "Training";
    public static final String TYPE_EXECUTION = "Execution";

    // reserved error message length
    public static final int ERR_MSG_LENGTH = 500;
}
