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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Prediction/Execution experiment runner
 */
public class ExecutionTask extends AbsTask{

    private BaseTaskInfo taskInfo;
    // volatile used for checking cancelled status
    private volatile boolean cancelled = false;
    private String taskId;
    /** Constructor */
    public ExecutionTask(BaseTaskInfo taskInfo, OnTaskUpdateListener listener){
        this.taskInfo = taskInfo;
        this.setTaskUpdateListener(listener);
        this.taskId = taskInfo.getTaskId();
    }

    /** Task lifecycle related*/
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
            executorService.submit(rPredictPerformer);
        }
    }

    @Override
    protected void cleanUp() {
        if(isFinished()){
            // success
            final ExecutorService executorService = getExecutorService();
            executorService.submit(this::moveOutputToPrediction);
            // todo: error or cancel or finished
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
    @Override
    protected ExecutorService getExecutorService() {
        return super.getExecutorService();
    }

    //---------------------------------
    /** Runnables: if you like clean but complex code, try to extract these runnables to standalone class */

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
                File tmpModelFolder = new File(res.getWorkspaceFolder(), "model");
                // copy data set content to workspace input folder
                FileUtils.copyDirectory(res.getDatasetFolder(), tmpInputFolder);
                if(cancelled){ throw new InterruptedException(); }
                FileUtils.copyDirectory(res.getModelFolder(), tmpModelFolder);
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
            // TODO: maybe delete the workspace folder if user wants it
        }
    };

    /**
     * Runnable used to run prediction/execution task
     * run the application and move resulted files to a proper folder
     * TODO: adding logic to check using which language runtime
     */
    private Runnable rPredictPerformer = () -> {
        // construct command
        final File workspaceFolder = getTaskInfo().getResourceInfo().getWorkspaceFolder();
        // TODO: if the entry point change, modify this, or extrac it to XML or Constants for managementz
        final File entryFile = new File(workspaceFolder, "Main.R");
        Process rProcess = null;    // use for canceling R application
        boolean processStarted = false;
        boolean processFinished = false;
        Path rFilePath = entryFile.toPath();
        //TODO: check kinds of OS and use different command, here we assume the Rscript command has been installed
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
                        Logger.Error("Execution task terminated with error output " + outputStr);
                        Exception ex = new Exception("Execution experiment terminated with error output");
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
                if (processStarted) {   // if process is not started, there is not need to kill it :)
                    if (!processFinished) { // if the process has finished, there is not need to kill it : )
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
                Logger.Error("The execution experiment has been interrupted in accidentally");
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

    // TODO: when we have design, put the prediction output to the right place
    private void moveOutputToPrediction(){
        //create Algorithm folder if it does not exist yet
        File modelFolder = FileUtils.createPredictionFolder();
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
            Logger.Error("Error happened when moving output to prediction folder");
        }
        // store in database
        //TODO: move to DbUtils
        String expName = getTaskInfo().getExperimentName();
        DbUtils.openConnection();
        List<Experiment> experiments = Experiment.where("experiment_name = ?", expName);
        Experiment experiment = experiments.get(0);
        // TODO: check this if later there is no project_id in database
        int projectId = experiment.getInteger("project_id");
        int experimentId = (Integer) experiment.getId();
        // TODO: change model to prediction in the future
        PredictionModel model = new PredictionModel();
        // TODO: the stored sample information should be modified after
        try {
            model.setModelName(expName + "-prediction")
                    .setDescription("sample description")
                    .setPath(targetFolder.toPath().toRealPath().toString())
                    .setProjectId(projectId)
                    .setExperimentId(experimentId);
            model.save();
            DbUtils.closeConnection();
            Logger.Info("a new prediction has been created and stored in the system");
        } catch (IOException e) {
            e.printStackTrace();
            Logger.Error("database error when insert a new prediction");
            DbUtils.closeConnection();
            taskUpdateListener.onError(e, getTaskId());
        }

    }


    //--------------------------------
    /** Utils */
    /**
     * return task ID,
     * this method assumes the instance has been intialized correctlys
     * @return
     */
    public String getTaskId(){
        return this.taskId;
    }

    /**
     * Get task info instance
     * @return BaseTaskInfo which contains all required infomation
     */
    public BaseTaskInfo getTaskInfo(){
        return taskInfo;
    }

    /**
     * Get task info
     * @return ExecutionTaskInfo which contains all required infomation
     */
    public ExecutionTaskInfo getExecutionTaskInfo(){
        ExecutionTaskInfo info = (ExecutionTaskInfo) taskInfo;
        return info;
    }
}
