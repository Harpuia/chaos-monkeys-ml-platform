package com.chaosmonkeys.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;

/**
 * Utilities to detect IP addresses.
 */
public class MachineIPHelper {
    /**
     * Gets host IP from AWS.
     *
     * @return Service host IP.
     */
    public static String getIPfromAWS() {
        URL whatIsMyIP = null;
        try {
            whatIsMyIP = new URL("http://checkip.amazonaws.com");
        } catch (MalformedURLException e) {
            Logger.SaveLog(LogType.Exception,"EXCEPTION: Could not create Amazon URL (my IP).");
        }

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    whatIsMyIP.openStream()));
        } catch (IOException e) {
            Logger.SaveLog(LogType.Exception,"EXCEPTION: Could not open stream to Amazon (my IP).");
        }

        String ip = null;
        try {
            ip = in.readLine();
        } catch (IOException e) {
            Logger.SaveLog(LogType.Exception,"EXCEPTION: Could not read value (my IP).");
        }
        return ip;
    }

    /**/
    public static String getRealIp() {
        //If no external ip
        String localIP = null;
        //External ip
        String netIP = null;

        //Create network interface
        Enumeration<NetworkInterface> netInterfaces = null;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        //IP
        InetAddress ip = null;

        //Whether the external network IP exists
        boolean found = false;

        //Extract connection information
        while (netInterfaces.hasMoreElements() && !found) {
            NetworkInterface ni = netInterfaces.nextElement();
            Enumeration<InetAddress> address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                ip = address.nextElement();
                if (!ip.isSiteLocalAddress()
                        && !ip.isLoopbackAddress()
                        && ip.getHostAddress().indexOf(":") == -1) {// external network IP
                    netIP = ip.getHostAddress();
                    found = true;
                    break;
                } else if (ip.isSiteLocalAddress()
                        && !ip.isLoopbackAddress()
                        && ip.getHostAddress().indexOf(":") == -1) {// local IP
                    localIP = ip.getHostAddress();
                }
            }
        }

        //Deciding which value to return
        if (netIP != null && !"".equals(netIP)) {
            return netIP;
        } else {
            return localIP;
        }
    }

    /**
     * Gets the localhost IP
     * @return Localhost IP.
     */
    public static String getLocalIP() {
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            Logger.SaveLog(LogType.Exception,"EXCEPTION: Unknown host ");
        }
        //Get local machine IP address
        return address.getHostAddress().toString();
    }
}
