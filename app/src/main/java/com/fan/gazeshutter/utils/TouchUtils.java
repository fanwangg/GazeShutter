package com.fan.gazeshutter.utils;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by fan on 5/11/16.
 */
public class TouchUtils {
    String TAG = "TouchUtils";
    public void simulateTouch(int x, int y) {
        try {
            Log.d(TAG,"trying to simulate touch"+x+" "+y);
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());

            String cmd = String.format("/system/bin/input tap %d %d\n", x, y);
            os.writeBytes(cmd);
            os.writeBytes("exit\n");
            os.flush();
            os.close();
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
