package com.fan.gazeshutter.utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import org.zeromq.ZMQ;
/**
 * Created by fan on 3/26/16.
 * ref. https://www.novoda.com/blog/minimal-zeromq-client-server/
 */

public class ZeroMQMessageTask extends AsyncTask<String, Void, String> {
    static final String TAG = "ZeroMQMessageTask";
    //static final String SERVER_IP = "192.168.0.222";
    static final String SERVER_IP = "192.168.0.117";
    static final String SERVER_PORT = "5566";

    static final String SUB_DT    = "dt";
    static final String SUB_GAZE  = "gaze_positions";
    static final String SUB_PUPIL = "pupil_positions";


    public ZeroMQMessageTask(){
    }

    @Override
    protected String doInBackground(String... params) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.SUB);

        socket.connect("tcp://"+SERVER_IP+":"+SERVER_PORT);
        //socket.subscribe("GOOG".getBytes(ZMQ.CHARSET));
        socket.subscribe("".getBytes(ZMQ.CHARSET));

        while (!Thread.currentThread ().isInterrupted ()) {
            // Read envelope with address
            String address = socket.recvStr ();
            // Read message contents
            String contents = socket.recvStr ();

            Log.d(TAG,address + " : " + contents);
        }
        Log.d(TAG,"4");

        String result = new String(socket.recv(0));
        socket.close();
        context.term();

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d(TAG,"result:"+result);
        //uiThreadHandler.sendMessage(Util.bundledMessage(uiThreadHandler, result));
    }
}
