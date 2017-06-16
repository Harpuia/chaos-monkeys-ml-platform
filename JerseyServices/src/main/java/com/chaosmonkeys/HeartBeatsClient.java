package com.chaosmonkeys;

import com.chaosmonkeys.DTO.StatusInfo;

import com.chaosmonkeys.Utilities.LogType;
import com.chaosmonkeys.Utilities.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Used to send heartbeats message to coordinationService
 * HTTP Method: POST
 * Content: JSON -> {IP: String, Status: String}
 */
public class HeartBeatsClient {
    private static String coordinationIP;
    private static boolean isStart = false;

    public void startSendHeartBeat(String ip) {
        if (isStart) {
            return;
        }
        coordinationIP = ip;
        Thread thread = new Thread(new SendHeartBeatTask());
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
                    StatusInfo status = new StatusInfo(Launcher.getHostIP(), Launcher.getServiceStatus());
                    Response response = invocationBuilder.post(Entity.entity(status, MediaType.APPLICATION_JSON));
                } catch (ProcessingException e) {
                    Logger.SaveLog(LogType.Exception,"EXCEPTION: Heartbeat Connection Timeout, retrying...");
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
