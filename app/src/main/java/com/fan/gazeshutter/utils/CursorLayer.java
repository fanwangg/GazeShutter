package com.fan.gazeshutter.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fan.gazeshutter.MainApplication;
import com.fan.gazeshutter.activity.MainActivity;
import com.fan.gazeshutter.service.OverlayService;

/**
 * Created by fan on 3/24/16.
 */

public class CursorLayer extends View implements  View.OnTouchListener, View.OnGenericMotionListener{
    static final String TAG = "CursorLayer";
    static final int haloBtnRadius = 100;
    Paint mPaint;
    boolean isGazing = false;
    Point gazePoint = null;

    public CursorLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CursorLayer(Context context){
        super(context);
        init();
    }

    protected void init(){
        //listener
        setOnTouchListener(this);
        setOnGenericMotionListener(this);

        setImportantForAccessibility( IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);

        //drawing
        //setBackgroundColor(0x88ff0000);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.RED);
        //setWillNotDraw(false);//[TODO]CHECK HERE
    }

    @Override
    public void onDraw(Canvas canvas){
        if(gazePoint!=null) {
            //MainApplication mainApplication = MainApplication.getInstance();
            //canvas.drawCircle(gazePoint.x, gazePoint.y, haloBtnRadius, mPaint);
            if (isGazing) {
                //UP
                canvas.drawCircle(gazePoint.x, 0, haloBtnRadius, mPaint);
                //RIGHT
                canvas.drawCircle(getWidth(), gazePoint.y, haloBtnRadius, mPaint);
                //DOWN
                canvas.drawCircle(gazePoint.x, getHeight(), haloBtnRadius, mPaint);
                //LEFT
                canvas.drawCircle(0, gazePoint.y, haloBtnRadius, mPaint);
            }
        }
    }

    @Override
    public boolean onGenericMotion(View v, MotionEvent event) {
        if(event.getToolType(0)!=MotionEvent.TOOL_TYPE_MOUSE && event.getToolType(0)!=MotionEvent.TOOL_TYPE_FINGER)
            return super.onGenericMotionEvent(event);

        int x = (int)event.getX();
        int y = (int)event.getY();
        Log.d(TAG,"GenericMotion x="+event.getX()+" y="+event.getY()+" action="+event.getAction());

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            isGazing = true;
            gazePoint = new Point(x, y);
        }
        else if(event.getAction() == MotionEvent.ACTION_BUTTON_PRESS){
            isGazing = true;
            gazePoint = new Point(x, y);
        }
        else if(event.getAction()==MotionEvent.ACTION_MOVE){
            gazePoint = new Point(x, y);
        }
        else if(event.getAction()==MotionEvent.ACTION_HOVER_MOVE){
            gazePoint = new Point(x, y);
        }
        else if(event.getAction() == MotionEvent.ACTION_BUTTON_RELEASE) {
            isGazing = false;
        }
        else if(event.getAction()==MotionEvent.ACTION_UP){
            isGazing = false;
        }
        this.invalidate();
        return true;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        this.onGenericMotion(this, event);
        //this.onTouch(this, event);
        return false;//super.dispatchTouchEvent(event);
    }
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        Log.d(TAG,"TouchMotion x="+event.getX()+" y="+event.getY());
        return false;//super.onTouchEvent(event);
    }

    public boolean getGazeState(){
        return isGazing;
    }
    public Point getGazePoint(){
        return gazePoint;
    }
}
