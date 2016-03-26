package com.fan.gazeshutter.event;

import android.annotation.SuppressLint;
import android.os.SystemClock;
import android.view.MotionEvent;

/**
 * Created by fan on 3/25/16.
 */
public class GazeEvent{
    public MotionEvent obtain(){

        float x = 100;//
        float y = 100;

        MotionEvent me = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_POINTER_DOWN, x, y, 0);
        return me;
    }
}