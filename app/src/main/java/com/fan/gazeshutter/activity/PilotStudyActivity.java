package com.fan.gazeshutter.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.fan.gazeshutter.R;
import com.fan.gazeshutter.service.OverlayService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.ButterKnife;

/**
 * Created by fan on 4/18/16.
 */
public class PilotStudyActivity extends Activity {
    final int TARGET_ROW_NUM = 4;
    final int TARGET_COL_NUM = 4;
    WindowManager mWindowManager;

    ViewGroup mLayout;
    ImageView[][] mTargetView = new ImageView[4][4];
    int mPrevID;

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
        for(int r = 0; r< TARGET_ROW_NUM; r++){
            for(int c = 0; c< TARGET_COL_NUM; c++){
                mTargetView[r][c] = new ImageView(this);
                mTargetView[r][c].setImageResource(R.drawable.cross);
                GridLayout.Spec rowSpec = GridLayout.spec(r);
                GridLayout.Spec colSpec = GridLayout.spec(c);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec,colSpec);
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                mLayout.addView(mTargetView[r][c], params);
                mTargetView[r][c].setVisibility(View.INVISIBLE);
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


    class Trail{
        static final String USER_KEY = "userID";
        static final String TRAIL_KEY = "trailID";
        static final String TARGET_KEY = "target";
        static final String PATH_KEY = "path";
        int row, col;
        int target;
        int trailID;
        int userID;
        long startTime;
        int duration;
        STAGE curStage;
        ArrayList<GazePoint> path;

        Trail(int user, int trail, int target){
            this.startTime = System.currentTimeMillis();
            this.userID = user;
            this.trailID = trail;

            this.target = target;
            this.row = getRow();
            this.col = getCol();

            this.path = new ArrayList<GazePoint>();
            this.curStage = STAGE.STAGE_0;
        }

        public void output(){
            try {
                JSONObject json = new JSONObject();
                json.put(USER_KEY,  userID);
                json.put(TRAIL_KEY, trailID);
                json.put(TARGET_KEY, target);

                JSONArray pathJSON = new JSONArray();
                for (int i = 0; i < path.size(); i++){
                    JSONObject point = new JSONObject();
                    point.put(GazePoint.POINT_X_KEY, path.get(i).x);
                    point.put(GazePoint.POINT_Y_KEY, path.get(i).y);
                    point.put(GazePoint.POINT_T_KEY, path.get(i).t);
                    point.put(GazePoint.POINT_STAGE_KEY, path.get(i).stage);
                    pathJSON.put(i, point);
                }
                json.put(PATH_KEY, pathJSON);
                String fileName = new String(userID+"/"+trailID+"_"+target+".json");
                saveJSONObject(json, "path");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        void saveJSONObject(JSONObject json, String path){
            try {
                String content = json.toString();
                FileWriter fw = null;
                fw = new FileWriter(path);

                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(content);
                bw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /*
        void updateCurStage(){
            if(curStage==STAGE.STAGE_0 && isWithinTarget(this))
                curStage=STAGE.STAGE_1;
            else if(curStage==STAGE.STAGE_1 && isWithinHaloButton(this))
                curStage=STAGE.STAGE_2;
            else if(curStage==STAGE.STAGE_2 && isWithinTarget(this))
                curStage=STAGE.STAGE_3;
        }
        */

        int getRow(){
            return this.target/TARGET_COL_NUM;
        }

        int getCol(){
            return target%TARGET_COL_NUM;
        }

        void updateDuration(int elapsedTime){
            if(elapsedTime > this.duration)
                duration = elapsedTime;
            return;
        }

        int getLastTimestamp(){
            return path.get(path.size()-1).t;
        }
    }

    enum STAGE {
        STAGE_0, STAGE_1, STAGE_2, STAGE_3;

        private static STAGE[] stages = values();

        public STAGE next() {
            if (this.ordinal() + 1 != stages.length)
                return stages[(this.ordinal() + 1) % stages.length];
            else
                return this;
        }
    }

    class GazePoint {
        static final String POINT_X_KEY = "x";
        static final String POINT_Y_KEY = "y";
        static final String POINT_T_KEY = "t";
        static final String POINT_STAGE_KEY = "s";

        //t for elapsed time in milles
        int t;
        int x, y;
        int stage;

        GazePoint(int x, int y, int t, int s) {
            this.x = x;
            this.y = y;
            this.t = t;
            this.stage = s;
        }
    }
}
