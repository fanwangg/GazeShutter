package com.fan.gazeshutter.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fan.gazeshutter.MainApplication;
import com.fan.gazeshutter.R;
import com.fan.gazeshutter.event.GazeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by fan on 4/18/16.
 */
public class PilotStudyActivity extends Activity{
    static final String TAG = "PilotStudyActivity";
    @Bind(R.id.txtTrailId) TextView mTxtViewTrailId;
    static int mCanvasWidth, mCanvasHeight;
    final int TARGET_ROWS = 4;
    final int TARGET_COLS = 4;
    final int TRAIL_PER_TARGET = 3;


    int mTrailNum = 0;
    Trail mCurrentTrail;
    ArrayList<Integer> mTrailTargets = new ArrayList<Integer>();



    boolean mInit = false;

    GridLayout mLayout;
    ImageView[][] mTargetViews = new ImageView[4][4];
    ImageView mPrevTargetView, mCurrentTargetView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilot_study);
        ButterKnife.bind(this);
        init();
    }
    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEvent(GazeEvent ge){
        MainApplication mMainApplicaiton = MainApplication.getInstance();
        int x = (int)(ge.x * mMainApplicaiton.mScreenWidth);
        int y = (int)(ge.x * mMainApplicaiton.mScreenHeight);
        mCurrentTrail.update(x, y);
    }


    void init() {
        mLayout = (GridLayout)findViewById(R.id.layout_pilotstudy);
        mLayout.setColumnCount(TARGET_COLS);
        mLayout.setRowCount(TARGET_ROWS);
        mLayout.post(new Runnable() {
            @Override
            public void run() {
                mCanvasWidth = mLayout.getWidth()-mLayout.getPaddingLeft()-mLayout.getPaddingRight();
                mCanvasHeight = mLayout.getHeight()-mLayout.getPaddingTop()-mLayout.getPaddingBottom();

                for (int r = 0; r < TARGET_ROWS; r++) {
                    for (int c = 0; c < TARGET_COLS; c++) {
                        mTargetViews[r][c] = new ImageView(PilotStudyActivity.this);
                        mTargetViews[r][c].setImageResource(R.drawable.cross);

                        GridLayout.Spec rowSpec = GridLayout.spec(r);
                        GridLayout.Spec colSpec = GridLayout.spec(c);
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);

                        params.width = mCanvasWidth / TARGET_COLS;
                        params.height = mCanvasHeight / TARGET_ROWS;
                        params.setGravity(Gravity.CENTER);
                        mLayout.addView(mTargetViews[r][c], params);
                        mTargetViews[r][c].setVisibility(View.INVISIBLE);
                    }
                }
                nextTrail();
            }
        });

        //init trail targets
        for (int r = 0; r < TARGET_ROWS; r++) {
            for (int c = 0; c < TARGET_COLS; c++) {
                for (int t = 0; t < TRAIL_PER_TARGET; t++) {
                    mTrailTargets.add(r* TARGET_COLS +c);
                }
            }
        }
        Collections.shuffle(mTrailTargets);
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

    public void showCurrentTargetView() {
        if(mPrevTargetView!=null) {
            mPrevTargetView.setVisibility(View.INVISIBLE);
        }
        if(mCurrentTargetView!=null) {
            mCurrentTargetView.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.layout_right)
    public void nextTrail(){
        if(mTrailNum > TARGET_ROWS * TARGET_COLS * TRAIL_PER_TARGET){
            return;
        } else if(mTrailNum == TARGET_ROWS * TARGET_COLS * TRAIL_PER_TARGET){
            showFinishDialog();
            return;
        }

        if(mCurrentTrail != null)
            mCurrentTrail.finishAndOutput();

        mPrevTargetView = mCurrentTargetView;
        mCurrentTrail = new Trail(mTrailNum, mTrailTargets.get(mTrailNum));
        mCurrentTargetView = mTargetViews[mCurrentTrail.getRow()][mCurrentTrail.getCol()];
        showCurrentTargetView();
        mTxtViewTrailId.setText("Trail #"+mTrailNum);
        mTrailNum++;
    }

    public void showFinishDialog(){
        new AlertDialog.Builder(PilotStudyActivity.this)
                .setTitle(R.string.finish_pilot_study_title)
                .setMessage(R.string.finish_pilot_study_msg)
                .setPositiveButton(R.string.finish_pilot_study_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //[TODO] thread to ouptut
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    class Trail{
        static final String USER_KEY = "userID";
        static final String TRAIL_KEY = "trailID";
        static final String TARGET_KEY = "target";
        static final String PATH_KEY = "path";

        final String PACKAGE_NAME = PilotStudyActivity.this.getPackageName();
        final String FILE_PATH = Environment.getExternalStorageDirectory()+"/Android/data/"+PACKAGE_NAME;
        int row, col;
        int target;
        int trailID;
        int userID;
        long startTime;
        int duration;
        STAGE curStage;
        ArrayList<GazePoint> path;

        Trail(int trailID, int target){
            this.startTime = System.currentTimeMillis();

            this.trailID = trailID;
            this.target = target;
            this.row = getRow();
            this.col = getCol();

            this.path = new ArrayList<GazePoint>();
            this.curStage = STAGE.STAGE_0;
        }

        public void finishAndOutput(){
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


                File folder = new File(FILE_PATH + File.separator + userID);
                if (!folder.exists()) {
                    folder.mkdir();
                }
                String fileName = new String(trailID+"_"+target+".json");
                saveJSONObject(json, FILE_PATH + File.separator + userID + File.separator + fileName);
                Log.d(TAG,fileName + "saved");

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

        void update(int x, int y){
            this.path.add(new GazePoint(x, y, System.currentTimeMillis()-this.startTime , this.curStage.ordinal()));
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
            return this.target/ TARGET_COLS;
        }

        int getCol(){
            return target% TARGET_COLS;
        }

        void updateDuration(int elapsedTime){
            if(elapsedTime > this.duration)
                duration = elapsedTime;
            return;
        }

        long getLastTimestamp(){
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
        int x, y;
        long t;
        int stage;

        GazePoint(int x, int y, long t, int s) {
            this.x = x;
            this.y = y;
            this.t = t;
            this.stage = s;
        }
    }
}
