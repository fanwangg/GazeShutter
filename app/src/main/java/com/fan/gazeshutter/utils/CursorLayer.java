package com.fan.gazeshutter.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.widget.RelativeLayout;

import com.fan.gazeshutter.MainApplication;
import com.fan.gazeshutter.activity.MainActivity;

/**
 * Created by fan on 3/24/16.
 */
public class CursorLayer extends RelativeLayout {
    public CursorLayer(Context context) {
        super(context);
    }

    @Override
    public void onDraw(Canvas canvas){
        if(((MainActivity)this.getContext()).getGazeState()) {
            MainApplication mainApplication = MainApplication.getInstance();
            Canvas grid = new Canvas(Bitmap.createBitmap(mainApplication.mScreenHeight, mainApplication.mScreenWidth, Bitmap.Config.ARGB_8888));

            grid.drawColor(Color.BLUE);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            grid.drawCircle(mainApplication.mScreenHeight / 2, mainApplication.mScreenWidth / 2, mainApplication.mScreenWidth / 2, paint);

            int haloBtnRadius = 100;
            Point curPoint = ((MainActivity)this.getContext()).getGazePoint();
            //UP
            grid.drawCircle(curPoint.x, 0, haloBtnRadius, paint);
            //RIGHT
            grid.drawCircle(mainApplication.mScreenWidth, curPoint.y, haloBtnRadius, paint);
            //DOWN
            grid.drawCircle(curPoint.x, mainApplication.mScreenHeight, haloBtnRadius, paint);
            //LEFT
            grid.drawCircle(0, curPoint.y, haloBtnRadius, paint);
        }
    }
}
