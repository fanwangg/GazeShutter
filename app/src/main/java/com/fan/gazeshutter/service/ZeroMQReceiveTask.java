package com.fan.gazeshutter.service;

import android.app.Service;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.fan.gazeshutter.MainApplication;
import com.fan.gazeshutter.R;
import com.fan.gazeshutter.service.OverlayService;
import com.fan.gazeshutter.utils.DispUtils;
import com.fan.gazeshutter.utils.NetworkUtils;

import org.zeromq.ZMQ;
/**
 * Created by fan on 3/26/16.
 * ref. https://www.novoda.com/blog/minimal-zeromq-client-server/
 */

public class ZeroMQReceiveTask extends AsyncTask<String, Point, String> {
    static final String TAG = "ZeroMQReceiveTask";
    static final String SERVER_IP = "192.168.0.117";
    static final String SERVER_PORT = NetworkUtils.PORT;

    static final String SUB_DT    = "dt";
    static final String SUB_GAZE  = "gaze_positions";
    static final String SUB_PUPIL = "pupil_positions";
    static final String SUB_GAZE_ON_SURFACE = "realtime gaze on unnamed"; //[TODO] tend to be changed

    View mView;
    OverlayService mService;
    WindowManager.LayoutParams mParams;
    public ZeroMQReceiveTask(OverlayService service){
        mService = service;
        mView = mService.mLayoutInflater.inflate(R.layout.overlay, null);
        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mService.mWindowMangager.addView(mView,mParams);
    }

    @Override
    protected String doInBackground(String... params) {

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.SUB);

        socket.connect("tcp://"+params[0]+":"+SERVER_PORT);
        socket.subscribe("".getBytes(ZMQ.CHARSET));

        while (!Thread.currentThread ().isInterrupted ()) {
            String address  = socket.recvStr ();
            String contents = socket.recvStr();

            Point p = parseMessageToPoint(contents);
            publishProgress(p);
            //Log.d(TAG,address + " : " + contents);
        }

        String result = new String(socket.recv(0));
        socket.close();
        context.term();

        return result;
    }

    @Override
    protected void onProgressUpdate(Point... point){
        mParams.x = point[0].x;//DispUtils.dp2px(point[0].x);
        mParams.y = point[0].y;
        mService.mWindowMangager.updateViewLayout(mView, mParams);

    }

    @Override
    protected void onPostExecute(String result) {
        Log.d(TAG,"result:"+result);
        //uiThreadHandler.sendMessage(Util.bundledMessage(uiThreadHandler, result));
    }

    protected Point parseMessageToPoint(String content){
        MainApplication mainApplication = MainApplication.getInstance();
        content = content.substring(1,content.length()-1);

        String[] xy = content.split(",");//[TODO] split w/ regex
        Log.d(TAG,content);
        int x = (int)(Double.valueOf(xy[0])*mainApplication.mScreenWidth);
        int y = (int)(Double.valueOf(xy[1])*mainApplication.mScreenHeight);
        //Log.d(TAG,"x:"+x+"  y:"+y);
        return new Point(x,y);
    }


}
