package com.chaosmonkeys.train.task;

import com.chaosmonkeys.train.task.interfaces.OnTaskUpdateListener;

import java.util.concurrent.ExecutorService;

/**
 * Base class of Async task
 */
public abstract class AbsTask {

    private volatile boolean isCancelled = false;
    private TaskState state = TaskState.IDLE;

    private OnTaskUpdateListener taskUpdateListener = null;
    //private ResultType result;

    public AbsTask(){}

    protected abstract void initialize();

    protected abstract void performTask();

    protected abstract void cleanUp();

    public ExecutorService getExcutorService(){
        return null;
    }

    protected abstract void cancelWorks();

    public final boolean isCancelled() {
        return ( isCancelled || state == TaskState.CANCELLED );
    }
    public final boolean isFinished() {
        return this.state.value() > TaskState.STARTED.value();
    }
    public final TaskState getState(){
        return state;
    }

    /**
     * package - only task pacakge can set state
     */
    void setState(TaskState tState){
        this.state = tState;
    }
    final void setTaskUpdateListener(OnTaskUpdateListener listener){
        this.taskUpdateListener = listener;
    }

}
