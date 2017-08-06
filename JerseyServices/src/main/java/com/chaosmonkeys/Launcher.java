package com.chaosmonkeys;

import com.chaosmonkeys.DTO.ConfigInfo;
import com.chaosmonkeys.DTO.BasicResponse;
import com.chaosmonkeys.DTO.RegistrationInfo;
import com.chaosmonkeys.Utilities.ConfigurationHelper;
import com.chaosmonkeys.Utilities.LogType;
import com.chaosmonkeys.Utilities.Logger;
import com.chaosmonkeys.Utilities.MachineIPHelper;
import com.chaosmonkeys.algrinputservice.AlgorithmInputServiceHeartBeatsClient;
import com.chaosmonkeys.datasetinputservice.DatasetInputServiceHeartBeatsClient;
import com.chaosmonkeys.train.heartbeat.ExecutionServiceHeartBeatsClient;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

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

    //Coordination service IP address
    private static String coordinationIP = NOT_SET;

    //Service information loaded from the config file
    public static String serviceName;
    public static String serviceType;
    public static String serviceDescription;
    private static String hostIP;
    public static String servicePort;
    private static String serviceHost;

    //Service status
    public static String ServiceStatus = "STOPPED";

    //Is service registered with the coordination service
    private static boolean isRegistered = false;

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
     * Sets the service host (IP with port)
     * @param hostIP host IP
     * @param servicePort Service Port
     */
    public static void setServiceHost(String hostIP, String servicePort) {
        serviceHost = hostIP + ":" + servicePort + "/";
    }

    /**
     * return the service host IP
     */
    public static String getServiceHost(){
        return serviceHost;
    }

    public static String getServiceName() {
        return serviceName;
    }
    public static void   setServiceName(String serviceName) {
        Launcher.serviceName = serviceName;
    }
    public static String getServiceType() {
        return serviceType;
    }
    public static void   setServiceType(String serviceType) {
        Launcher.serviceType = serviceType;
    }
    public static String getServiceDescription() {
        return serviceDescription;
    }
    public static void   setServiceDescription(String serviceDescription) {
        Launcher.serviceDescription = serviceDescription;
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

        //Loading the basic info
        ConfigInfo configInfoInfo = ConfigurationHelper.loadBasicInfo(configPath);
        serviceName = configInfoInfo.getServiceName();
        serviceType = configInfoInfo.getServiceType();
        serviceDescription = configInfoInfo.getServiceDescription();
        coordinationIP = configInfoInfo.getCoordinationIP();
        hostIP = configInfoInfo.getServiceIP();
        servicePort = configInfoInfo.getServicePort();
        setServiceHost(hostIP, servicePort);

        //Start http server
        startServiceServer();

        //Displaying service information
        Logger.SaveLog(LogType.Information, "The Coordination IP is :" + coordinationIP);
        Logger.SaveLog(LogType.Information, "Local IP of my system is := " + localhostIP.getHostAddress());
        try {
            Logger.SaveLog(LogType.Information, "Web IP of my system is := " + MachineIPHelper.getIPfromAWS());
        } catch (Exception e) {
            Logger.SaveLog(LogType.Exception, "EXCEPTION: Failed to get IP from AWS.");
        }
        Logger.SaveLog(LogType.Information, "Port of my system is := " + servicePort);

        // input service heartbeats clients
        if (serviceType.startsWith("DataInput-")) {
            DatasetInputServiceHeartBeatsClient datasetInputServiceHeartBeatsClient = new DatasetInputServiceHeartBeatsClient();
            datasetInputServiceHeartBeatsClient.startSendHeartBeat(coordinationIP);
        } else if (serviceType.startsWith("Train-") || serviceType.startsWith("Exec-")) {
            ExecutionServiceHeartBeatsClient executionServiceHeartBeatsClient = new ExecutionServiceHeartBeatsClient();
            executionServiceHeartBeatsClient.startSendHeartBeat(coordinationIP);
        } else if (serviceType.startsWith("AlgInput-")) {
            AlgorithmInputServiceHeartBeatsClient algorithmInputServiceHeartBeatsClient = new AlgorithmInputServiceHeartBeatsClient();
            algorithmInputServiceHeartBeatsClient.startSendHeartBeat(coordinationIP);
        }
    }

    /**
     * Starts server.
     */
    public void startServiceServer() {
        final HttpServer server = startServer();
        Logger.SaveLog(LogType.Information, String.format("Jersey app started with WADL available at " + "%sapplication.wadl\nHit enter to stop it...", serviceHost));
    }

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     *
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        //Create a resource config that scans for JAX-RS resources and providers in com.chaosmonkeys package
        //final ResourceConfig rc = new ResourceConfig().packages("com.chaosmonkeys");
        final ResourceConfig rc;
        if (serviceType.startsWith("DataInput-")) {
            rc = new ResourceConfig().packages("com.chaosmonkeys.datasetinputservice");
        } else if (serviceType.startsWith("AlgInput-")) {
            rc = new ResourceConfig().packages("com.chaosmonkeys.algrinputservice");
        } else if (serviceType.startsWith("Train-") || serviceType.startsWith("Exec-")) {
            rc = new ResourceConfig().packages("com.chaosmonkeys.train");
        } else if (serviceType.equals("JerseyTesting")) {
            rc = new ResourceConfig().packages("com.chaosmonkeys");
        } else {
            Logger.SaveLog(LogType.Error, "ERROR: Service type" + serviceType + " not defined!");
            rc = new ResourceConfig().packages("com.chaosmonkeys");
        }
        // register jackson for parsing JSON
        rc.register(JacksonFeature.class);
        // register multipart for supporting file upload
        rc.register(MultiPartFeature.class);

        rc.register(CORSFilter.class);

        //Create and start a new instance of grizzly http server exposing the Jersey application at Service Host
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(serviceHost), rc);
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
