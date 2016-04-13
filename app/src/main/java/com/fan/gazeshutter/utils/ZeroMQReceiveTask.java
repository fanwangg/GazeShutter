package com.fan.gazeshutter.utils;

import android.os.AsyncTask;
import android.util.Log;
import org.zeromq.ZMQ;
/**
 * Created by fan on 3/26/16.
 * ref. https://www.novoda.com/blog/minimal-zeromq-client-server/
 */

public class ZeroMQReceiveTask extends AsyncTask<String, Void, String> {
    static final String TAG = "ZeroMQReceiveTask";
    static final String SERVER_IP = "192.168.0.117";
    static final String SERVER_PORT = NetworkUtils.PORT;

    static final String SUB_DT    = "dt";
    static final String SUB_GAZE  = "gaze_positions";
    static final String SUB_PUPIL = "pupil_positions";


    public ZeroMQReceiveTask(){
    }

    @Override
    protected String doInBackground(String... params) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.SUB);

        socket.connect("tcp://"+params[0]+":"+SERVER_PORT);
        socket.subscribe("".getBytes(ZMQ.CHARSET));

        while (!Thread.currentThread ().isInterrupted ()) {
            // Read envelope with address
            String address = socket.recvStr ();
            // Read message contents
            String contents = socket.recvStr ();

            Log.d(TAG,address + " : " + contents);
        }

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
