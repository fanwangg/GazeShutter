package com.fan.gazeshutter.activity;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.fan.gazeshutter.MainApplication;
import com.fan.gazeshutter.R;

import org.zeromq.ZMQ;

import butterknife.ButterKnife;

/**
 * Created by fan on 4/24/16.
 */
public class ZMQSendingActivity extends Activity implements View.OnTouchListener{
    static final String TAG = "ZMQSendingActivity";
    static final String PORT = "5566";
    ViewGroup mLayout;
    ZMQ.Context mContext;
    ZMQ.Socket mPublisher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zmq_send);
        ButterKnife.bind(this);

        init();
    }

    @Override
    protected void onStop(){
        new Thread(){
            public void run(){
                mPublisher.close();
                mContext.term();
            }
        }.start();

        super.onStop();
    }

    void init(){
        mContext = ZMQ.context(1);
        mPublisher = mContext.socket(ZMQ.PUB);

        new Thread(){
            public void run(){
                mPublisher.bind("tcp://*:"+PORT);
            }
        }.start();

        mLayout =  (ViewGroup)this.getWindow().getDecorView().getRootView();
        mLayout.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Message msg = new Message();
        msg.arg1 = (int)event.getX();
        msg.arg2 = (int)event.getY();
        mZMQSendHandler.sendMessage(msg);
        return false;
    }

    private Handler mZMQSendHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            MainApplication mainApplication = MainApplication.getInstance();
            final double xRatio = ((double)msg.arg1)/mainApplication.mScreenWidth;
            final double yRatio = 1 - ((double)msg.arg2)/mainApplication.mScreenHeight;
            Log.d(TAG,"("+xRatio+","+yRatio+")");
            new Thread() {
                public void run() {
                    mPublisher.send("(" + xRatio + "," + yRatio + ")");
                }
            }.start();
        }
    };

}
