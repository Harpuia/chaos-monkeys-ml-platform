package com.chaosmonkeys.train.task;

import com.chaosmonkeys.train.task.interfaces.OnTaskUpdateListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base class of Async task
 */
public abstract class AbsTask {

    private volatile boolean isCancelled = false;
    private volatile TaskState state = TaskState.IDLE;

    private volatile ExecutorService singleExecutorService;

    protected OnTaskUpdateListener taskUpdateListener = null;
    //private ResultType result;

    public AbsTask(){}

    protected abstract void initialize();

    protected abstract void performTask();

    protected abstract void cleanUp();

    /**
     * Obtains a single thread executor for this Task's following runnables
     * using double-checked lock to ensure thread-safe
     * @return ExecutorService
     * @since JDK 1.5
     */
    protected ExecutorService getExecutorService(){
        if(null == singleExecutorService){
            synchronized (this){
                if(null == singleExecutorService){
                    singleExecutorService = Executors.newSingleThreadExecutor();
                }
            }
        }
        return singleExecutorService;
    }


    protected abstract void cancelWorks();

    public final boolean isCancelled() {
        return ( isCancelled || state == TaskState.CANCELLED );
    }

    public final boolean isIDLE(){
        return this.state.value() == TaskState.IDLE.value();
    }

    public final boolean isInitialized(){
        return this.state.value() >= TaskState.INITIALIZED.value();
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
