package com.chaosmonkeys.inputservice;

import com.chaosmonkeys.DTO.StatusInfo;
import com.chaosmonkeys.HeartBeatsClient;
import com.chaosmonkeys.Launcher;
import com.chaosmonkeys.Utilities.LogType;
import com.chaosmonkeys.Utilities.Logger;
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
public class InputServiceHeartBeatsClient {
    private static String coordinationIP;
    private static boolean isStart = false;

    public void startSendHeartBeat(String ip) {
        if (isStart) {
            return;
        }
        coordinationIP = ip;
        ScheduledExecutorService scheduledExecutorService =
                Executors.newScheduledThreadPool(5);

//        Client client = ClientBuilder.newBuilder().newClient(new ClientConfig().register(LoggingFilter.class));

        Thread thread = new Thread(new InputServiceHeartBeatsClient.SendHeartBeatTask());
        thread.start();
    }

    class SendHeartBeatTask implements Runnable {

        @Override
        public void run() {
            Client client = ClientBuilder.newClient(new ClientConfig().register(LoggingFilter.class));
            WebTarget webTarget = client.target(coordinationIP).path("setStatus");     // target to the

            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
            while (true) {
                try {
                    InputServiceWorkState workState = new InputServiceWorkState(InputService.checkSet, InputService.uploadSet, InputService.serviceStatus);
                    InputServiceStatusInfo serviceStatusInfo = new InputServiceStatusInfo(Launcher.getHostIP(), workState);

                    Response response = invocationBuilder.post(Entity.entity(serviceStatusInfo, MediaType.APPLICATION_JSON));
                } catch (ProcessingException e) {
                    Logger.SaveLog(LogType.Exception, "EXCEPTION: Heartbeat Connection Timeout, retrying...");
                }
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
