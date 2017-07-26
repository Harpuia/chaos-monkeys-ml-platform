package com.chaosmonkeys.train.task;

import com.chaosmonkeys.Utilities.FileUtils;
import com.chaosmonkeys.Utilities.LogType;
import com.chaosmonkeys.Utilities.Logger;
import com.chaosmonkeys.Utilities.StringUtils;
import com.chaosmonkeys.Utilities.db.DbUtils;
import com.chaosmonkeys.dao.Experiment;
import com.chaosmonkeys.dao.PredictionModel;
import com.chaosmonkeys.train.task.interfaces.OnTaskUpdateListener;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.process.ProcessUtil;
import org.zeroturnaround.process.Processes;
import org.zeroturnaround.process.SystemProcess;
import org.zeroturnaround.process.WindowsProcess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Training task runner
 */
public class TrainingTask extends AbsTask{

    private TrainingTaskInfo taskInfo;
    private Future<ProcessResult> mOnProcessingFuture;
    private Process startedProcess;
    // volatile used for checking cancelled status
    private volatile boolean cancelled = false;

    public TrainingTask(TrainingTaskInfo trainTaskInfo, OnTaskUpdateListener listener){
        this.taskInfo = trainTaskInfo;
        this.setTaskUpdateListener(listener);
    }

    //---------------------------------------
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
        if(isFinished()){
            // success
            final ExecutorService executorService = getExecutorService();
            executorService.submit(this::moveOutputToModel);
            // todo: error or cancel
            executorService.submit(cleanUpPerformer);
        }
    }

    @Override
    protected void cancelWorks() {
        //TODO this method may have some concurrent bug in the future, when unexpected cancelling process happened, check this method
        // set the task cancelled flag as true
        cancelled = true;       // let runnable check the volatile variable and throw interruption
        taskUpdateListener.onCancelled(getTaskId());
    }

    //** Task Performers  ----------------------------------------------------------------------
    /**
     * Runnable for checking
     */
    private Runnable initTaskPerformer = () -> {
        final ResourceInfo res = taskInfo.getResourceInfo();
        try {
            // copy algorithm content to workspace
            if(!cancelled){
                FileUtils.copyDirectory(res.getAlgorithmFolder(),res.getWorkspaceFolder());
                if(cancelled){ throw new InterruptedException(); }
                File tmpInputFolder = new File(res.getWorkspaceFolder(), "input");
                // copy data set content to workspace input folder
                FileUtils.copyDirectory(res.getDatasetFolder(), tmpInputFolder);
                if(cancelled){ throw new InterruptedException(); }
                // invoke initialized
                if(!cancelled){
                    taskUpdateListener.onInitialized(taskInfo.getTaskId());
                }
            }else {
                Logger.Info("Experiment cancelled during initializing");
            }
        } catch (IOException e) {
            Logger.Error("Initializing experiment error when copying resource to temp workspace");
            e.printStackTrace();
            //TODO: trigger error and clean up
        }catch (InterruptedException ex){   // cancelled
            Logger.Info("Experiment cancelled during initializing");
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
        Process rProcess = null;    // use for canceling R application
        boolean processStarted = false;
        boolean processFinished = false;
        Path rFilePath = entryFile.toPath();
        //TODO: check kinds of OS and use different command
        ProcessExecutor procExecutor = new ProcessExecutor().directory(workspaceFolder).command("Rscript", rFilePath.toString());
        Optional<String> output = Optional.empty();
        try {
            Logger.Info("Experiment starts at " + rFilePath);
            StartedProcess proc = procExecutor.readOutput(true).destroyOnExit().start();
            rProcess = proc.getProcess();
            Future<ProcessResult> futureResult = proc.getFuture();
            Optional<ProcessResult> resultOptional = Optional.empty();
            try {
                // check the status here, if found cancelled signal, kill the process using Zt-Killer
                while (!futureResult.isDone()) {
                    if (cancelled) {
                        processStarted = true;
                        throw new InterruptedException();
                    }
                }
                processFinished = true;
                resultOptional = Optional.ofNullable(futureResult.get());
            } catch (ExecutionException e) {  //exception may be thrown because future cancellation
                e.printStackTrace();
            }
            output = Optional.ofNullable(resultOptional.get().getOutput().getUTF8());
//            output =  Optional.ofNullable(procExecutor.readOutput(true).destroyOnExit().execute().outputUTF8());
            // search error string in output if output is present
            if (!cancelled) {
                if (output.isPresent()) {
                    //TODO: find a right way to identify error
                    String outputStr = output.get();
                    boolean matched = StringUtils.containsIgnoreCase(outputStr, "error");
                    Logger.Info(outputStr);
                    if (matched) {
                        Logger.Error("Training task terminated with error output " + outputStr);
                        Exception ex = new Exception("training experiment terminated with error output");
                        if (!cancelled) {
                            taskUpdateListener.onError(ex, getTaskId());
                        }
                    } else {
                        //TODO: move output folder to the dest
                        if (!cancelled) {
                            taskUpdateListener.onSuccess(getTaskId());
                        }
                    }
                } else {
                    //TODO: move output folder to the dest
                    if (!cancelled) {
                        taskUpdateListener.onSuccess(getTaskId());
                    }
                }
            }
        } catch (IOException e) {
            Logger.Error("IOException happened when starting training experiment");
            e.printStackTrace();
            taskUpdateListener.onError(e, getTaskId());
        } catch (InterruptedException e) {
            if (cancelled) {
                //invoke zt-killer
                if (processStarted) {
                    if (!processFinished) {
                        Optional<Process> procOptional = Optional.ofNullable(rProcess);
                        if (procOptional.isPresent()) {
                            SystemProcess sysProcess = Processes.newStandardProcess(procOptional.get());
                            try {
                                ProcessUtil.destroyGracefullyOrForcefullyAndWait(sysProcess, 2, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
                                //TODO: add more proper exception handling
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            } catch (TimeoutException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            } else {
                Logger.Error("The training experiment has been interrupted in accidentally");
                e.printStackTrace();
                taskUpdateListener.onError(e, getTaskId());
            }
        }

    };
    /**
     * Delete temp workspace folder in the background
     */
    private Runnable cleanUpPerformer = () -> {
        // create a model folder

    };

    private void moveOutputToModel(){
        //create Algorithm folder if it does not exist yet
        File modelFolder = FileUtils.createModelFolder();
        Logger.SaveLog(LogType.Information, "PredictionModel storage root folder has been created");
        //create dev language folder if it does not exist yet
        String language = getTaskInfo().getExperimentLanguage();
        File langFolder = FileUtils.createNewFolderUnder(language, modelFolder);
        // create target folder
        String targetFolderName = StringUtils.genModelStorageFolderName(getTaskInfo().getExperimentName());
        File targetFolder = FileUtils.createNewFolderUnder(targetFolderName, langFolder);
        // copy output folder content to target folder
        final File workspaceFolder = getTaskInfo().getResourceInfo().getWorkspaceFolder();
        final File outputFolder = new File(workspaceFolder, "output");

        try {
            FileUtils.copyDirectory(outputFolder, targetFolder);
        } catch (IOException e) {
            //TODO: trigger error handler and cleanup
            e.printStackTrace();
            Logger.Error("Error happened when moving output to model folder");
        }
        // store in database
        //TODO: move to DbUtils
        String expName = getTaskInfo().getExperimentName();
        DbUtils.openConnection();
        List<Experiment> experiments = Experiment.where("experiment_name = ?", expName);
        Experiment experiment = experiments.get(0);
        int projectId = experiment.getInteger("project_id");
        int experimentId = (Integer) experiment.getId();
        PredictionModel model = new PredictionModel();
        try {
            model.setModelName(expName + "-model")
                    .setDescription("sample description")
                    .setPath(targetFolder.toPath().toRealPath().toString())
                    .setProjectId(projectId)
                    .setExperimentId(experimentId);
            model.save();
            DbUtils.closeConnection();
            Logger.Info("a new model has been created and stored in the system");
        } catch (IOException e) {
            e.printStackTrace();
            Logger.Error("database error when insert a new model");
            DbUtils.closeConnection();
            taskUpdateListener.onError(e, getTaskId());
        }

    }

    //** Utils -----------------------------------------------------------------------------------

    public String getTaskId(){
        return taskInfo.getTaskId();
    }

    public TrainingTaskInfo getTaskInfo() {
        return taskInfo;
    }
}
