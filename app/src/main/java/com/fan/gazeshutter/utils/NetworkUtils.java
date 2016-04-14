package com.fan.gazeshutter.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by fan on 4/13/16.
 */
public class NetworkUtils {
    static public final String TAG = "NetworkUtils";
    static public final String PORT = "5566";
    static public String serverIP = "192.168.1.1";

    static public String getLocalIpAddress(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        String adWifi = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return adWifi;
    }
    static public void setServerIP(String newIP){
        serverIP = newIP;
    }
    static public String getServerIP(){
        return serverIP;
    }
}
