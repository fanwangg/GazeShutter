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
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.fan.gazeshutter.MainApplication;
import com.fan.gazeshutter.R;
import com.fan.gazeshutter.event.GazeEvent;
import com.fan.gazeshutter.event.ModeEvent;
import com.fan.gazeshutter.service.OverlayService;
import com.fan.gazeshutter.service.ZMQReceiveTask;
import com.fan.gazeshutter.utils.Common;
import com.fan.gazeshutter.utils.DispUtils;
import com.fan.gazeshutter.utils.NetworkUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;


public class MainActivity extends AppCompatActivity {
    @Bind(R.id.txtDeviceIP)
    TextView mTxtDeviceIP;
    @Bind(R.id.txtServerIP)
    EditText mTxtServerIP;
    @Bind(R.id.txtServerPORT)
    EditText mTxtServerPORT;
    @Bind(R.id.btnServiceToggle)
    ToggleButton mBtnServiceToggle;
    @Bind(R.id.spinnerMode)
    Spinner mSpinnerMode;

    public static double globalX, globalY;
    static double GLOBAL_MAX = 10;
    private static final String TAG = "MainActivity";
    private static final String ACTION_USB_PERMISSION = "com.fan.gazeshutter.activity.USB_PERMISSION";
    private static final int REQUEST_CODE = 5566;
    UsbManager mUsbManager;
    IntentFilter filterAttached_and_Detached;
    BroadcastReceiver mUsbReceiver;

    int mMode = 0;
    String mUserName = "";


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
    protected void onDestroy() {
//        unregisterReceiver(mUsbReceiver);
        super.onDestroy();//must be the last
    }

    protected void initScreenSize() {
        MainApplication mainApplication = MainApplication.getInstance();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mainApplication.mScreenWidth = size.x;
        mainApplication.mScreenHeight = size.y;
        Log.d(TAG, "x:" + size.x + " y:" + size.y);
    }

    protected void init() {
        DispUtils.init(this);
        initScreenSize();

        //setupUsbReceiver()

        //network
        mTxtDeviceIP.setText(NetworkUtils.getLocalIpAddress(this));
        mTxtServerIP.setText(NetworkUtils.getLocalIpAddress(this));
        mTxtServerPORT.setText(NetworkUtils.getServerPORT());

        ArrayAdapter<String> modeList = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, PilotStudyActivity.MODE.names());
        mSpinnerMode.setAdapter(modeList);

        Common.hideNavigationBar(this);
    }

    @OnClick(R.id.btnServiceToggle)
    public void checkPermissionAndToggleService() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);
        } else {
            toggleService();
        }
    }

    @OnTextChanged(R.id.txtServerIP)
    public void updateServerIP(CharSequence text) {
        NetworkUtils.setServerIP(text.toString());
    }

    @OnTextChanged(R.id.txtServerPORT)
    public void updateServerPORT(CharSequence text) {
        NetworkUtils.setServerPORT(text.toString());
    }

    @OnClick(R.id.btnZMQSend)
    public void startZmqSend() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, ZMQSendingActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btnPilot)
    public void startPilotStudy() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, PilotStudyActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString(PilotStudyActivity.Trail.USER_KEY, mUserName);
        bundle.putInt(PilotStudyActivity.Trail.USER_MODE, mMode);

        intent.putExtras(bundle);
        startActivity(intent);
    }

    @OnClick(R.id.btnFileSystem)
    public void startFileManager() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent,REQUEST_CODE);
    }

    @OnClick(R.id.btnMaker)
    public void showMaker() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, CalibrateActivity.class);
        startActivity(intent);
    }

    @OnTextChanged(R.id.txtUserName)
    public void updateUserName(CharSequence text) {
        mUserName = text.toString();
    }

    @OnItemSelected(R.id.spinnerMode)
    void onItemSelected(int position) {
        mMode = position;
        Log.d("spinnerMode", "Selected" + position);
        ZMQReceiveTask.BTN_MODE = PilotStudyActivity.MODE.values()[position];
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            //start service with permission granted
            if (Settings.canDrawOverlays(this)) {
                toggleService();
            }
        }
    }


    private void toggleService() {
        if (mBtnServiceToggle.isChecked()) {
            Intent i = new Intent(MainActivity.this, OverlayService.class);
            startService(i);
            Log.d(TAG, "start service");

        } else {
            // Stop the service when the Menu button clicks.
            Intent i = new Intent(MainActivity.this, OverlayService.class);
            stopService(i);
            Log.d(TAG, "stop service");

        }
    }

    private void drawHaloButtons(int x, int y) {

        return;
    }

    public void drawGazePoint(int x, int y) {


    }

    protected void setupUsbReceiver() {
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mUsbReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    synchronized (this) {
                        UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (device != null) {
                            Log.d(TAG, "DEATTCHED-" + device);
                        }
                    }
                }

                if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                    synchronized (this) {
                        UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (device != null) {
                                Log.d(TAG, "ATTACHED-" + device);
                            }
                        } else {
                            PendingIntent mPermissionIntent;
                            mPermissionIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT);
                            mUsbManager.requestPermission(device, mPermissionIntent);
                        }
                    }
                }

                if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (device != null) {
                                Log.d(TAG, "PERMISSION-" + device);
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
        Log.d(TAG, deviceList.size() + " USB device(s) found");
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            Log.d("1", "" + device);
        }
    }
}

