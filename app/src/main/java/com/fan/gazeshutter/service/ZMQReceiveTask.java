package com.fan.gazeshutter.service;

import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.fan.gazeshutter.MainApplication;
import com.fan.gazeshutter.R;
import com.fan.gazeshutter.utils.NetworkUtils;

import org.zeromq.ZMQ;
/**
 * Created by fan on 3/26/16.
 * ref. https://www.novoda.com/blog/minimal-zeromq-client-server/
 */

public class ZMQReceiveTask extends AsyncTask<String, Double, String> {
    static final String TAG = "ZMQReceiveTask";
    static final String SERVER_IP = "192.168.0.117";
    static final String SERVER_PORT = NetworkUtils.PORT;

    static final String SUB_DT    = "dt";
    static final String SUB_GAZE  = "gaze_positions";
    static final String SUB_PUPIL = "pupil_positions";
    static final String SUB_GAZE_ON_SURFACE = "realtime gaze on unnamed"; //[TODO] tend to be changed

    View mView;
    OverlayService mService;
    WindowManager.LayoutParams mParams;
    public ZMQReceiveTask(OverlayService service){
        mService = service;
        mView = mService.mLayoutInflater.inflate(R.layout.overlay, null);
        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
    }

    @Override
    protected String doInBackground(String... params) {

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.SUB);

        socket.connect("tcp://"+params[0]+":"+SERVER_PORT);
        socket.subscribe("".getBytes(ZMQ.CHARSET));

        //while (!Thread.currentThread ().isInterrupted ()) {
        while (!isCancelled()) {
            String address  = socket.recvStr ();
            String contents = socket.recvStr();

            Double[] xy = parseMessageToRatio(contents);
            publishProgress(xy);
            Log.d(TAG,address + " : " + contents);
        }

        String result = new String(socket.recv(0));
        socket.close();
        context.term();

        return result;
    }

    @Override
    protected void onProgressUpdate(Double... xy){
        if(0<=xy[0] && xy[0]<=1 && 0<=xy[1] && xy[1]<=1) {
            if(!mView.isShown()) {
                mService.mWindowManager.addView(mView, mParams);
            }
            MainApplication mainApplication = MainApplication.getInstance();
            mParams.x = (int)(xy[0]*mainApplication.mScreenWidth);
            mParams.y = (int)((1-xy[1])*mainApplication.mScreenHeight);
            mService.mWindowManager.updateViewLayout(mView, mParams);
        }
        else{
            if(mView.isShown()) {
                mService.mWindowManager.removeViewImmediate(mView);
            }
            Log.d(TAG, "onProgressUpdate: gaze out of screen");
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d(TAG,"result:"+result);

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.d(TAG,"onCanceled");
        mService.mWindowManager.removeView(mView);
    }

    protected Double[] parseMessageToRatio(String content){
        content = content.substring(1,content.length()-1);
        Log.d(TAG,content);

        String[] xy = content.split(",");//[TODO] split w/ regex
        Double x = Double.valueOf(xy[0]);
        Double y = Double.valueOf(xy[1]);
        //Log.d(TAG,"x:"+x+"  y:"+y);

        return new Double[]{x,y};
    }
}
