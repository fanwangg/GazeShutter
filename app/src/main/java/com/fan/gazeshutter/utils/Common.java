package com.fan.gazeshutter.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.fan.gazeshutter.activity.PilotStudyActivity;

/**
 * Created by fan on 5/3/16.
 */
public class Common {
    static final int MARGIN = 24;
    static final String TAG = "Common";

    public static boolean isPointInsideView(int targetX, int targetY, View view){
        int location[] = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        int halfW = view.getWidth();
        int halfH = view.getHeight();

        //s = String.format(s, viewX, viewY, x, y, viewX+view.getWidth(), viewY+ + view.getHeight());
        //Log.d(TAG, s);
        //point is inside view bounds
        if(x-halfW<=targetX && targetX<=x+halfW && y-halfH<=targetY && targetY<=y+halfH){
            return true;
        } else {
            return false;
        }
    }

    public static void hideNavigationBar(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
