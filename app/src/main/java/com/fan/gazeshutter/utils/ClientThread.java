package com.fan.gazeshutter.utils;

import android.content.Entity;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;


public class ClientThread implements Runnable {
    private static final int BUFFER_SIZE = 2048;
    private static final int SERVER_PORT = 5000;
    private static final String SERVER_IP = "127.0.0.1";

    Socket mSocket;
    private BufferedReader mInputBuffer;

    public ClientThread(){
        InetAddress serverAddr  = null;
        try {
            serverAddr = InetAddress.getByName(SERVER_IP);
            mSocket = new Socket(serverAddr, SERVER_PORT);
            mInputBuffer = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        /*
            thread to read from socket, and obtain MotionEvnet and dispatchTouchEvent

            //parsing...
            Layout = ...
            layout.dispatchTouchEvent(GazeEvnet.obtain());
         */

        String jsonData = receiveDataFromServer();
        Gson gson = new Gson();
        gson.fromJson(jsonData, HashMap.class);
    }

    public void disconnectWithServer(){
        if(mSocket != null){
            if(mSocket.isConnected()) {
                try{
                    mInputBuffer.close();
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String receiveDataFromServer() {
        try {
            String message = "";
            int charsRead = 0;
            char[] buffer = new char[BUFFER_SIZE];

            while ((charsRead = mInputBuffer.read(buffer)) != -1) {
                message += new String(buffer).substring(0, charsRead);
            }

            disconnectWithServer(); // disconnect server
            return message;
        } catch (IOException e) {
            return "Error receiving response:  " + e.getMessage();
        }
    }
}


