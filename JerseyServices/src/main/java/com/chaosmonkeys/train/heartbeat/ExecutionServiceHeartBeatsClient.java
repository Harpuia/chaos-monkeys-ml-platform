package com.chaosmonkeys.train.heartbeat;

import com.chaosmonkeys.Launcher;
import com.chaosmonkeys.Utilities.LogType;
import com.chaosmonkeys.Utilities.Logger;

import com.chaosmonkeys.train.Constants;
import com.chaosmonkeys.train.dto.ExecutionServiceStatus;
import com.chaosmonkeys.train.task.ExecutionTaskManager;
import com.chaosmonkeys.train.task.TrainingTaskManager;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Heartbeats client for Execution Service
 */
public class ExecutionServiceHeartBeatsClient {

    private static String coordinationIP;
    private static boolean isStart = false;

    public void startSendHeartBeat(String ip) {
        if (isStart) {
            return;
        }
        coordinationIP = ip;

//        Client client = ClientBuilder.newBuilder().newClient(new ClientConfig().register(LoggingFilter.class));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::sendHeartBeatTask);
    }

    private void sendHeartBeatTask(){
        Client client = ClientBuilder.newClient(new ClientConfig().register(LoggingFilter.class));
        WebTarget webTarget = client.target(coordinationIP).path("setStatus");     // target to the

        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        while (true) {
            try {
                int runningNum = TrainingTaskManager.INSTANCE.getRunningTaskNum() + ExecutionTaskManager.INSTANCE.getRunningTaskNum();
                String runState = runningNum > 0 ? Constants.STATUS_RUN : Constants.STATUS_IDLE;
                ExecutionServiceStatus status = new ExecutionServiceStatus(Launcher.getHostIP(), runState);

                Response response = invocationBuilder.post(Entity.entity(status, MediaType.APPLICATION_JSON));
            } catch (ProcessingException e) {
                Logger.SaveLog(LogType.Exception, "EXCEPTION: Heartbeat Connection Timeout, retrying...");
            }
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
