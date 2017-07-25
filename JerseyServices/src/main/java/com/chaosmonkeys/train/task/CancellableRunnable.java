package com.chaosmonkeys.train.task;



/**
 * Cancellable runnable interface
 */
public interface CancellableRunnable extends Runnable{

    boolean isCancelled();
    void cancel();
}
