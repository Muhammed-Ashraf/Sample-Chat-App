package com.example.user.samplechatapp.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.user.samplechatapp.receiver.ConnectivityReceiver;
import com.example.user.samplechatapp.receiver.SensorRestarterBroadcastReceiver;
import com.example.user.samplechatapp.xmpp.SampleChatConnectionService;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by fabio on 30/01/2016.
 */
public class SensorService extends Service implements ConnectivityReceiver.ConnectivityReceiverListener {
    public int counter=0;
    private ConnectivityReceiver mConnectivityReceiver;
    public SensorService(Context applicationContext) {
        super();
        Log.i("HERE", "here I am!");
    }

    public SensorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mConnectivityReceiver = new ConnectivityReceiver(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        registerReceiver(mConnectivityReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        unregisterReceiver(mConnectivityReceiver);
        Intent broadcastIntent = new Intent(this, SensorRestarterBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  "+ (counter++));
            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.d("SAMPLECHATAPPTAG", "NetworkConnection Available: " + isConnected);
        String message = isConnected ? "Good! Connected to Internet" : "Sorry! Not connected to internet";
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
//        Log.d("SAMPLECHATAPPTAG", "Network  connectivity detected for first time" + isFirstTime);
//        if (!isFirstTime) {
//            isFirstTime = false;
        if (isConnected) {
////            mTHandler.post(new Runnable() {
////                @Override
////                public void run() {
////                    if (mConnection != null) {
////                        mConnection.sendMessage("message from background", "ashraf8534@jabber.at");
////                        Log.d(LOGTAG, "message sent from  background");
////                    }
////                }
////            });
//
//           stopService(new Intent(this,SampleChatConnectionService.class));
//            Log.d("Rooster", "servicestopped");
           startService(new Intent(this,SampleChatConnectionService.class));
            Log.d("Rooster", "servicestarted");
        }
//        }


    }
}