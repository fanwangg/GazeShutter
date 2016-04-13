package com.fan.gazeshutter.utils;

import android.os.AsyncTask;
import android.util.Log;

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


    public ZeroMQSendTask(){
    }

    @Override
    protected Void doInBackground(String... params) {
        ZMQ.Context context = ZMQ.context(1);

        ZMQ.Socket publisher = context.socket(ZMQ.PUB);
        publisher.bind("tcp://*:"+SERVER_PORT);

        while (!Thread.currentThread ().isInterrupted ()) {
            publisher.sendMore ("A");
            publisher.send ("We don't want to see this");
            publisher.sendMore ("B");
            publisher.send("We would like to see this");
        }

        publisher.close();
        context.term();

        return null;
    }
}
