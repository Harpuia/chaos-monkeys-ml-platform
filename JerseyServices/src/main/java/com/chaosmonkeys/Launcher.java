package com.chaosmonkeys;

import com.chaosmonkeys.DTO.ConfigInfo;
import com.chaosmonkeys.DTO.BasicResponse;
import com.chaosmonkeys.DTO.RegistrationInfo;
import com.chaosmonkeys.Utilities.ConfigurationHelper;
import com.chaosmonkeys.Utilities.LogType;
import com.chaosmonkeys.Utilities.Logger;
import com.chaosmonkeys.Utilities.MachineIPHelper;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * Launcher class.
 */
public class Launcher {
    /* Constants */
    //Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/";
    //Service statuses
    //TODO: Add/Replace as required by the service specification
    /*public static final String RUNNING = "RUNNING";
    public static final String STOPPED = "STOPPED";*/
    //IP not set message
    public static final String NOT_SET = "NOT_SET";

    /* Attributes */
    //Path to the configuration file
    private static String configPath;
    //IP of the host machine/VM
    private static String hostIP;
    //Coordination service IP address
    private static String coordinationIP = NOT_SET;
    //Service information
    public static String serviceName;
    public static String serviceType;
    public static String serviceDescription;
    //Service status
    public static String ServiceStatus = "STOPPED";
    //Is service registered with the coordination service
    private static boolean isRegistered = false;

    /**
     * Returns the host IP of the service.
     *
     * @return Host IP of the service.
     */
    public static String getHostIP() {
        return hostIP;
    }

    /**
     * Sets the status of the service, can be called from other files in this project
     *
     * @param status New status
     */
    public static void setServiceStatus(String status) {
        ServiceStatus = status;
    }

    /**
     * Gets the status of the service, can be called from other files in this project
     *
     * @return Current status of the service
     */
    public static String getServiceStatus() {
        return ServiceStatus;
    }

    /**
     * The service initialization process
     *
     * @param filePath configuration file path
     * @throws Exception
     */
    public void start(String filePath) {
        //Initialization the config path
        configPath = filePath;

        //Host IP address
        InetAddress localhostIP = null;
        try {
            localhostIP = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            Logger.SaveLog(LogType.Exception, "EXCEPTION: could not get localhost IP address.");
        }

        //Start http server
        startServiceServer();

        //Loading the basic info
        ConfigInfo configInfoInfo = ConfigurationHelper.loadBasicInfo(configPath);
        serviceName = configInfoInfo.getServiceName();
        serviceType = configInfoInfo.getServiceType();
        serviceDescription = configInfoInfo.getServiceDescription();
        coordinationIP = configInfoInfo.getCoordinationIP();

        //Displaying service information
        Logger.SaveLog(LogType.Information, "The Coordination IP is :" + coordinationIP);
        Logger.SaveLog(LogType.Information, "Local IP of my system is := " + localhostIP.getHostAddress());
        try {
            Logger.SaveLog(LogType.Information, "Web IP of my system is := " + MachineIPHelper.getIPfromAWS());
        } catch (Exception e) {
            Logger.SaveLog(LogType.Exception, "EXCEPTION: Failed to get IP from AWS.");
        }

        //Self IP, which is sent to the coordination service
        //TODO: Replace with appropriate IP (development/production)
        //For testing
        Launcher.hostIP = "127.0.0.1:8080";
        /*For production
        Launcher.hostIP = MachineIPUtility.getRealIp();*/

        //Start registration
        ThreadSafeRegister registerThread = new ThreadSafeRegister();
        registerThread.start();

        //Check register status
        try {
            registerThread.join();
        } catch (InterruptedException e) {
            Logger.SaveLog(LogType.Exception, "EXCEPTION: Failed to register.");
        }
        if (isRegistered) {
            //Send heartbeats
            HeartBeatsClient hbClient = new HeartBeatsClient();
            hbClient.startSendHeartBeat(coordinationIP);
        } else {
            Logger.SaveLog(LogType.Error, "ERROR: Failed to register on coordination service! Please contact administrator.");
        }
    }

    /**
     * Starts server.
     */
    public void startServiceServer() {
        final HttpServer server = startServer();
        Logger.SaveLog(LogType.Information, String.format("Jersey app started with WADL available at " + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
    }

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     *
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        //Create a resource config that scans for JAX-RS resources and providers in com.chaosmonkeys package
        final ResourceConfig rc = new ResourceConfig().packages("com.chaosmonkeys");

        //Create and start a new instance of grizzly http server exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Registration thread.
     */
    public class ThreadSafeRegister extends Thread {
        public volatile boolean exit = false;

        public void run() {
            //Creating a web client to send registration calls and status update calls
            Client client = ClientBuilder.newClient(new ClientConfig().register(LoggingFilter.class));

            //Setting up a web target for the calls
            WebTarget webTarget = client.target(coordinationIP).path("registerService");

            //
            RegistrationInfo serviceInfo = new RegistrationInfo(hostIP, serviceType, serviceName, serviceDescription);
            while (!exit) {
                Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
                Response response = null;
                try {
                    response = invocationBuilder.post(Entity.entity(serviceInfo, MediaType.APPLICATION_JSON));
                } catch (ProcessingException e) {
                    Logger.SaveLog(LogType.Exception, "EXCEPTION: Register Connection timeout, retrying...");
                }
                if (null != response) {
                    BasicResponse basicResponse = response.readEntity(BasicResponse.class);
                    String responseText = basicResponse.getResponse();
                    if (responseText.equals("OK")) {
                        isRegistered = true;
                        exit = true;
                    }
                }
                // sleep 3 seconds
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    Logger.SaveLog(LogType.Exception, "EXCEPTION: Interrupted Exception.");
                }
            }

        }
    }

    /**
     * Launcher entry point.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
        // the first argument should be the absolute path of the config.ini file
        if (args != null) {
            String configurationPath = args[0];
            new Launcher().start(configurationPath);
        } else {
            System.out.println("Error! Argument not provided: path to configuration file (config.ini)");
        }
    }
}