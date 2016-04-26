package com.fan.gazeshutter.activity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fan.gazeshutter.MainApplication;
import com.fan.gazeshutter.R;
import com.fan.gazeshutter.service.OverlayService;
import com.fan.gazeshutter.utils.DispUtils;
import com.fan.gazeshutter.utils.NetworkUtils;
import com.fan.gazeshutter.service.ZeroMQSendTask;

import java.util.HashMap;
import java.util.Iterator;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;


public class MainActivity extends AppCompatActivity {
    @Bind(R.id.txtDeviceIP) TextView mTxtDeviceIP;
    @Bind(R.id.txtServerIP) EditText mTxtServerIP;


    public static double globalX, globalY;
    static double GLOBAL_MAX = 10;
    private static final String TAG = "MainActivity";
    private static final String ACTION_USB_PERMISSION = "com.fan.gazeshutter.activity.USB_PERMISSION";
    private static final int REQUEST_CODE = 5566;
    UsbManager mUsbManager;
    IntentFilter filterAttached_and_Detached;
    BroadcastReceiver mUsbReceiver;

    boolean isGazing = false;
    Point gazePoint = null;

    /*
     *  lifecycle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //this.bindService();
        //getWindow().getDecorView().getRootView().setOnGenericMotionListener(this);
        init();
    }


    @Override
    protected void onDestroy(){
        unregisterReceiver(mUsbReceiver);
        super.onDestroy();//must be the last
    }

    protected void initScreenSize(){
        MainApplication mainApplication = MainApplication.getInstance();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mainApplication.mScreenWidth  = size.x;
        mainApplication.mScreenHeight = size.y;
        Log.d(TAG,"x:"+size.x+" y:"+size.y);
    }

    protected void init(){
        DispUtils.init(this);
        initScreenSize();
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mUsbReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    synchronized (this) {
                        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if(device != null){
                            Log.d(TAG,"DEATTCHED-" + device);
                        }
                    }
                }

                if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                    synchronized (this) {
                        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if(device != null){
                                Log.d(TAG,"ATTACHED-" + device);
                            }
                        }
                        else {
                            PendingIntent mPermissionIntent;
                            mPermissionIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT);
                            mUsbManager.requestPermission(device, mPermissionIntent);
                        }
                    }
                }

                if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if(device != null){
                                Log.d(TAG,"PERMISSION-" + device);
                            }
                        }
                    }
                }
            }
        };

        filterAttached_and_Detached = new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        filterAttached_and_Detached.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filterAttached_and_Detached.addAction(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filterAttached_and_Detached);

        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Log.d(TAG, deviceList.size()+" USB device(s) found");
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while(deviceIterator.hasNext()){
            UsbDevice device = deviceIterator.next();
            Log.d("1","" + device);
        }

        //network
        mTxtDeviceIP.setText(NetworkUtils.getLocalIpAddress(this));
        mTxtServerIP.setText(NetworkUtils.getLocalIpAddress(this));
    }


     /*
     * mouse event

    @Override
    public boolean onGenericMotion(View v, MotionEvent event) {
        //if((event.getSource() & InputDevice.SOURCE_MOUSE) == 0)
        if(event.getToolType(0)!=MotionEvent.TOOL_TYPE_MOUSE
                && event.getToolType(0)!=MotionEvent.TOOL_TYPE_FINGER)
            return super.onGenericMotionEvent(event);

        //Log.d(TAG,"GenericMotion x="+event.getX()+" y="+event.getY());
        int x = (int)event.getX();
        int y = (int)event.getY();
        if(event.getButtonState() == MotionEvent.ACTION_DOWN){
            isGazing = true;
            gazePoint = new Point(x, y);
        }
        else if(event.getButtonState() == MotionEvent.ACTION_MOVE) {
            gazePoint = new Point(x, y);
        }
        else if(event.getButtonState() == MotionEvent.ACTION_UP){
            isGazing = false;
        }

        if((event.getEdgeFlags() & MotionEvent.EDGE_LEFT) != 0){
            Log.d(TAG,"edge left");
        }

        cursorLayer.invalidate();

        return true;
    }
     */

    @OnClick(R.id.btnServiceToggle)
    public void checkPermissionAndToggleService() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);
        }
        else {
            toggleService();
        }
    }

    @OnTextChanged(R.id.txtServerIP)
    public void updateServerIP(CharSequence text){
        NetworkUtils.setServerIP(text.toString());
    }

    @OnClick(R.id.btnZMQSend)
    public void startZmqSend() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, ZMQSendingActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btnPilot)
    public void startPilotStudy(){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, PilotStudyActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        if (requestCode == REQUEST_CODE) {
            //start service with permission granted
            if (Settings.canDrawOverlays(this)) {
                toggleService();
            }
        }
    }

    private void toggleService() {
        Intent intent=new Intent(this, OverlayService.class);
        if (!stopService(intent)) {
            startService(intent);
        }
    }

    private void drawHaloButtons(int x, int y){

        return;
    }

    public void drawGazePoint(int x, int y){


    }
}

