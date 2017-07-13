package com.chaosmonkeys.train.task;

/**
 * The enumeration for the current execution-state of a {@link AbsTask}.
 *
 */
public enum TaskState {
    IDLE(0), INITIALIZING(1), INITIALIZED(2), STARTED(3), CANCELLED(4), SUCCESS(5), ERROR(6), STOPPED(7);

    private final int value;

    private TaskState(int value){
        this.value = value;
    }
    public int value(){
        return this.value;
    }
}