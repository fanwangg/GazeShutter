package com.fan.gazeshutter.service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.fan.gazeshutter.activity.MainActivity;
import com.fan.gazeshutter.utils.NetworkUtils;

import org.zeromq.ZMQ;

/**
 * Created by fan on 3/26/16.
 * ref. https://www.novoda.com/blog/minimal-zeromq-client-server/
 */

public class ZeroMQSendTask extends AsyncTask<String, Void, Void> {
    static final String TAG = "ZeroMQSendTask";
    static final String SERVER_PORT = NetworkUtils.PORT;

    static final String SUB_DT    = "dt";
    static final String SUB_GAZE  = "gaze_positions";
    static final String SUB_PUPIL = "pupil_positions";
    static final String SUB_GAZE_ON_SURFACE = "realtime gaze on unnamed"; //[TODO] tend to be changed

    Context mContext;

    public ZeroMQSendTask(Context context){
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket publisher = context.socket(ZMQ.PUB);
        publisher.bind("tcp://*:"+SERVER_PORT);

        while (!Thread.currentThread ().isInterrupted ()) {
            publisher.sendMore (SUB_GAZE_ON_SURFACE);
            double x = ((MainActivity)mContext).globalX;
            double y = ((MainActivity)mContext).globalY;

            publisher.send ("("+x+","+y+")");
        }

        publisher.close();
        context.term();
        return null;
    }
}
