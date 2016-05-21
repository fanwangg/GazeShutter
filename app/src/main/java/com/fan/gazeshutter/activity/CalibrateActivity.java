package com.fan.gazeshutter.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.fan.gazeshutter.R;
import com.fan.gazeshutter.utils.Common;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CalibrateActivity extends AppCompatActivity{
    private static final String TAG = "SettingActivity";
    boolean isFinished = false;
    /*
     *  lifecycle
     */

    @Bind(R.id.img_marker) ImageView mMakerStart;
    @Bind(R.id.img_marker_stop) ImageView mMakerStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        ButterKnife.bind(this);

        Common.hideNavigationBar(this);
    }

    @OnClick(R.id.layout_marker)
    void foo(){
        if(!isFinished){
            mMakerStart.setVisibility(View.VISIBLE);
            mMakerStop.setVisibility(View.INVISIBLE);
            isFinished = true;
        }
        else{
            mMakerStart.setVisibility(View.INVISIBLE);
            mMakerStop.setVisibility(View.VISIBLE);
            isFinished = false;
        }
        return;
    }

}

