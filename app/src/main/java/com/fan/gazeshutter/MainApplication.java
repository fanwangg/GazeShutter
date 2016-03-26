package com.fan.gazeshutter;

import android.app.Application;

/**
 * Created by fan on 3/25/16.
 */
public class MainApplication extends Application {
    private static MainApplication mInstance= null;
    public int mScreenWidth;
    public int mScreenHeight;


    public MainApplication(){
    }

    public static synchronized MainApplication getInstance(){
        if(null == mInstance){
            mInstance = new MainApplication();
        }
        return mInstance;
    }
}
