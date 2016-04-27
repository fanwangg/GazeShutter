package com.fan.gazeshutter.service;

import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.fan.gazeshutter.MainApplication;
import com.fan.gazeshutter.R;
import com.fan.gazeshutter.activity.PilotStudyActivity;
import com.fan.gazeshutter.event.GazeEvent;
import com.fan.gazeshutter.utils.NetworkUtils;

import org.greenrobot.eventbus.EventBus;
import org.zeromq.ZMQ;
/**
 * Created by fan on 3/26/16.
 * ref. https://www.novoda.com/blog/minimal-zeromq-client-server/
 */

public class ZMQReceiveTask extends AsyncTask<String, Float, String> {
    static final String TAG = "ZMQReceiveTask";
    static final String SERVER_IP = "192.168.0.117";
    static final String SERVER_PORT = NetworkUtils.PORT;

    static final String SUB_DT    = "dt";
    static final String SUB_GAZE  = "gaze_positions";
    static final String SUB_PUPIL = "pupil_positions";
    static final String SUB_GAZE_ON_SURFACE = "realtime gaze on unnamed"; //[TODO] tend to be changed

    View mInfoView, mGazePointView;
    WindowManager.LayoutParams mInfoTextParams, mGazePointParams;

    OverlayService mService;

    public ZMQReceiveTask(OverlayService service){
        mService = service;

        mGazePointView = mService.mLayoutInflater.inflate(R.layout.service_gaze_point, null);
        mGazePointParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mGazePointParams.gravity = Gravity.LEFT | Gravity.TOP;

        mInfoView = mService.mLayoutInflater.inflate(R.layout.overlay, null);
        mInfoTextParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mInfoTextParams.gravity = Gravity.RIGHT | Gravity.TOP;
        mService.mWindowManager.addView(mInfoView, mInfoTextParams);
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

            Float[] xy = parseMessageToRatio(contents);

            EventBus.getDefault().post(new GazeEvent(xy[0], xy[1]));
            publishProgress(xy);
            Log.d(TAG,address + " : " + contents);
        }

        String result = new String(socket.recv(0));
        socket.close();
        context.term();

        return result;
    }

    @Override
    protected void onProgressUpdate(Float... xy){
        Log.d(TAG, "onProgressUpdate:"+xy[0]+" "+xy[1]);
        //info
        TextView mInfoTextView = (TextView)mInfoView.findViewById(R.id.txtInfo);
        mInfoTextView.setText("("+xy[0]+","+xy[1]+")");

        //GazePoint
        if(0<=xy[0] && xy[0]<=1 && 0<=xy[1] && xy[1]<=1) {
            if(!mGazePointView.isShown()) {
                mService.mWindowManager.addView(mGazePointView, mGazePointParams);
            }
            MainApplication mainApplication = MainApplication.getInstance();
            mGazePointParams.x = (int)(xy[0]*mainApplication.mScreenWidth);
            mGazePointParams.y = (int)((1-xy[1])*mainApplication.mScreenHeight);
            mService.mWindowManager.updateViewLayout(mGazePointView, mGazePointParams);

        }
        else{
            if(mGazePointView.isShown()) {
                mService.mWindowManager.removeViewImmediate(mGazePointView);
            }
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
        mService.mWindowManager.removeView(mGazePointView);
    }

    protected Float[] parseMessageToRatio(String content) {
        content = content.substring(1, content.length() - 1);
        Log.d(TAG, content);

        String[] xy = content.split(",");//[TODO] split w/ regex
        Float x = Float.valueOf(xy[0]);
        Float y = Float.valueOf(xy[1]);
        //Log.d(TAG,"x:"+x+"  y:"+y);

        return new Float[]{x, y};
    }
}
