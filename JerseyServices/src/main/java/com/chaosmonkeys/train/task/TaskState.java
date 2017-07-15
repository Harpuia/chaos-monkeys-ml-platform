package com.chaosmonkeys.train.task;

import java.util.Arrays;
import java.util.List;

/**
 * The enumeration for the current execution-state of a {@link AbsTask}.
 *
 */
public enum TaskState {
    IDLE(0), INITIALIZING(1), INITIALIZED(2), STARTED(3), CANCELLED(4), SUCCESS(5), ERROR(6);

    private final int value;
    private List<String> strValList = Arrays.asList("IDLE", "INITIALIZING","INITIALIZED", "STARTED", "CANCELLED", "SUCCESS", "ERROR");

    TaskState(int value){
        this.value = value;
    }
    public int value(){
        return this.value;
    }

    /**
     * Obtain string value
     * @return
     */
    public String StringValue(){
        return strValList.get(this.value);
    }
}