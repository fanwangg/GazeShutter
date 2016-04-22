package com.fan.gazeshutter.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.fan.gazeshutter.R;
import com.fan.gazeshutter.service.OverlayService;

import butterknife.ButterKnife;

/**
 * Created by fan on 4/18/16.
 */
public class PilotStudyActivity extends Activity {
    OverlayService countService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilot_study);
        ButterKnife.bind(this);

        init();
    }


    void init(){
        Intent intent = new Intent(this, OverlayService.class);
        //this.bindService(intent, , BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop(){
        //mService = null;
        //this.unbindService(mServConn);
        super.onStop();
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            //countService = ((OverlayService.ServiceBinder) service).getService();

        }

        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            countService = null;
        }

    };
}
