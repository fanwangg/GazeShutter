package com.fan.gazeshutter.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.fan.gazeshutter.R;
import com.fan.gazeshutter.service.OverlayService;

import butterknife.ButterKnife;

/**
 * Created by fan on 4/18/16.
 */
public class PilotStudyActivity extends Activity {
    final int ROW_TARGET = 4;
    final int COL_TARGET = 4;
    WindowManager mWindowManager;

    WindowManager.LayoutParams mParams;
    ViewGroup mLayout;
    ImageView[][] mTargetView = new ImageView[4][4];

    OverlayService countService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilot_study);
        ButterKnife.bind(this);

        init();
    }

    void init(){
        mLayout =  (ViewGroup)this.getWindow().getDecorView().getRootView();
        mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);

        Intent intent = new Intent(this, OverlayService.class);
        for(int r=0; r<ROW_TARGET; r++){
            for(int c=0; c<COL_TARGET; c++){
                mTargetView[r][c] = new ImageView(this);
                mTargetView[r][c].setImageResource(R.drawable.cross);
                GridLayout.Spec rowSpec = GridLayout.spec(r);
                GridLayout.Spec colSpec = GridLayout.spec(c);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec,colSpec);
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                mLayout.addView(mTargetView[r][c], params);
            }
        }
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            //countService = ((OverlayService.ServiceBinder) service).getService();

        }

        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
        }

    };
}
