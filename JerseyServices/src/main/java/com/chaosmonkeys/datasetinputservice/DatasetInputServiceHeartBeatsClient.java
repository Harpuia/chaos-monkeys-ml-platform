package com.chaosmonkeys.datasetinputservice;

import com.chaosmonkeys.Launcher;
import com.chaosmonkeys.Utilities.LogType;
import com.chaosmonkeys.Utilities.Logger;
import com.chaosmonkeys.datasetinputservice.DTO.DatasetInputServiceStatusInfo;
import com.chaosmonkeys.datasetinputservice.DTO.DatasetInputServiceWorkState;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Class for sending input service heartbeats
 */
public class DatasetInputServiceHeartBeatsClient {
    private static String coordinationIP;
    private static boolean isStart = false;

    public void startSendHeartBeat(String ip) {
        if (isStart) {
            return;
        }
        coordinationIP = ip;
        // start a single thread
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        SendHeartBeatTask heartbeatTask = new SendHeartBeatTask();
        scheduledExecutorService.submit(heartbeatTask);

    }

    class SendHeartBeatTask implements Runnable {

        @Override
        public void run() {
            Client client = ClientBuilder.newClient(new ClientConfig().register(LoggingFilter.class));
            WebTarget webTarget = client.target(coordinationIP).path("setStatus");     // target to the

            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
            while (true) {
                try {
                    DatasetInputServiceWorkState workState = new DatasetInputServiceWorkState(DatasetInputService.checkSet, DatasetInputService.uploadSet, DatasetInputService.serviceStatus);
                    DatasetInputServiceStatusInfo serviceStatusInfo = new DatasetInputServiceStatusInfo(Launcher.getServiceHost(), Launcher.getServiceType(), Launcher.getServiceName(), Launcher.getServiceDescription(), workState);

                    Response response = invocationBuilder.post(Entity.entity(serviceStatusInfo, MediaType.APPLICATION_JSON));
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
}
