package com.chaosmonkeys.train.task;

import com.chaosmonkeys.Utilities.FileUtils;
import com.chaosmonkeys.Utilities.Logger;
import com.chaosmonkeys.train.task.interfaces.OnTaskUpdateListener;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * Training task runner
 */
public class TrainingTask extends AbsTask{

    private TrainingTaskInfo taskInfo;

    public TrainingTask(TrainingTaskInfo trainTaskInfo, OnTaskUpdateListener listener){
        this.taskInfo = trainTaskInfo;
        this.setTaskUpdateListener(listener);
    }

    @Override
    protected ExecutorService getExecutorService() {
        return super.getExecutorService();
    }

    @Override
    protected void initialize() {
        // evaluate the current state, backward is not permitted
        if(isIDLE()){
            // set the initializing sign
            taskUpdateListener.onInit(taskInfo.getTaskId());
            // copy files to workspace
            final ExecutorService executorService = getExecutorService();
            executorService.submit(initTaskPerformer);
        }
    }

    @Override
    protected void performTask() {
        if(isInitialized()){
            taskUpdateListener.onStarted(getTaskId());
            final ExecutorService executorService = getExecutorService();
            executorService.submit(rTrainingPerformer);
        }
    }

    @Override
    protected void cleanUp() {

    }

    @Override
    protected void cancelWorks() {

    }

    //** Task Performers  ----------------------------------------------------------------------
    /**
     * Runnable for checking
     */
    private Runnable initTaskPerformer = () -> {
        final ResourceInfo res = taskInfo.getResourceInfo();
        try {
            // copy algorithm content to workspace
            FileUtils.copyDirectory(res.getAlgorithmFolder(),res.getWorkspaceFolder());
            File tmpInputFolder = new File(res.getWorkspaceFolder(), "input");
            // copy data set content to workspace input folder
            FileUtils.copyDirectory(res.getDatasetFolder(), tmpInputFolder);
            // invoke initialized
            taskUpdateListener.onInitialized(taskInfo.getTaskId());
        } catch (IOException e) {
            Logger.Error("Initializing experiment error when copying resource to temp workspace");
            e.printStackTrace();
            //TODO: trigger error and clean up

        }
    };
    /**
     * Runnable used to run training task
     * run the application and move resulted files to a proper folder
     * TODO: maybe change it to synchronized method and use asynchronous ProcessExecutor
     */
    private Runnable rTrainingPerformer = () -> {
        // construct command
        final File workspaceFolder = getTaskInfo().getResourceInfo().getWorkspaceFolder();
        final File entryFile = new File(workspaceFolder, "Main.R");
        Path rFilePath = entryFile.toPath();
        //TODO: check kinds of OS and use different command
        ProcessExecutor procExecutor= new ProcessExecutor().command("Rscript",rFilePath.toString());
        Optional<String> output;
        try {
            Logger.Info("Experiment starts at " + rFilePath);
            output =  Optional.ofNullable(procExecutor.readOutput(true).destroyOnExit().execute().outputUTF8());
            // search error string in output if output is present
            if(output.isPresent()){
                String outputStr = output.get();
                String pattern = "error";
                Pattern errPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                boolean matched = errPattern.matcher(outputStr).matches();
                //TODO: delete
                Logger.Info(outputStr);
                if(matched){
                    Logger.Error("Training task terminated with error output " + outputStr);
                    Exception ex = new Exception("training expeirment terminated with error output");
                    taskUpdateListener.onError(ex, getTaskId());
                }else{
                    //TODO: move output folder to the dest
                    taskUpdateListener.onSuccess(getTaskId());
                }
            }else{
                //TODO: move output folder to the dest
                taskUpdateListener.onSuccess(getTaskId());
            }
        } catch (IOException e) {
            Logger.Error("IOException happened when starting training experiment");
            e.printStackTrace();
            taskUpdateListener.onError(e, getTaskId());
        } catch (InterruptedException e) {
            Logger.Error("The training experiment has been interrupted in accidentally");
            e.printStackTrace();
            taskUpdateListener.onError(e, getTaskId());
        } catch (TimeoutException e) {
            Logger.Error("The training experiment timed out");
            e.printStackTrace();
            taskUpdateListener.onError(e, getTaskId());
        }
    };

    private Runnable cleanUpPerformer = () -> {

    };

    //** Utils -----------------------------------------------------------------------------------

    public String getTaskId(){
        return taskInfo.getTaskId();
    }

    public TrainingTaskInfo getTaskInfo() {
        return taskInfo;
    }
}
