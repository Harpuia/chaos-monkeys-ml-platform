package com.chaosmonkeys.train.task.interfaces;

/**
 * Listener for invoking callback from Task Runner
 */
public interface OnTaskUpdateListener {
    void onWaiting(String taskId);      //  when a task has been registered in the manager and not assigned a thread
    void onInitialized(String taskId);
    void onStarted(String taskId);
    void onCancelled(String taskId);
    void onSuccess(String taskId);
    void onError(Throwable ex, String taskId);

}
