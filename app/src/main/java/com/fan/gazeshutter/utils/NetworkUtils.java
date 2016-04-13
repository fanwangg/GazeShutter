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
    static final String TAG = "NetworkUtils";
    static final String PORT = "5566";
    static public String getLocalIpAddress(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        String adWifi = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return adWifi;
    }
}
