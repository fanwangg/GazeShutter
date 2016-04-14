package com.fan.gazeshutter.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;

import com.fan.gazeshutter.MainApplication;

/**
 * Created by fan on 4/13/16.
 */
public class DispUtils {
    static public double mDensity;

    static public void init(Context context){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        mDensity = displayMetrics.density;

    }

    static public int px2dp(int px){
        return (int) ((px/mDensity)+0.5);
    }
    static public int dp2px(int dp){
        return (int) ((dp*mDensity)+0.5);
    }
}
