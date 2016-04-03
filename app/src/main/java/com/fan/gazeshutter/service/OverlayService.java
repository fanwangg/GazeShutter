package com.fan.gazeshutter.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.fan.gazeshutter.R;
import com.fan.gazeshutter.utils.CursorLayer;

/**
 * Created by fan on 3/27/16.
 * ref. http://www.whycouch.com/2013/01/how-to-overlay-view-over-everything-on.html
 */
public class OverlayService extends Service{
    static final boolean SHOWING_MARKER = false;
    static final String TAG = "OverlayService";
    CursorLayer mCursorLayer;

    //@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mCursorLayer = new CursorLayer(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,//100,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                //WindowManager.LayoutParams.TYPE_PHONE,
                //WindowManager.LayoutParams.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                //WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        wm.addView(mCursorLayer, params);

        if(SHOWING_MARKER){
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup mView =  (ViewGroup) inflater.inflate(R.layout.service_marker_overlay, null);
            wm.addView(mView,params);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mCursorLayer!=null){
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.removeView(mCursorLayer);
        }
    }
}
