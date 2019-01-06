package com.example.user.samplechatapp.xmpp;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;


import com.example.user.samplechatapp.receiver.ConnectivityReceiver;
import com.example.user.samplechatapp.util.Constants;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;

import java.io.IOException;

/**
 * Created by gakwaya on 2018/1/11.
 */

public class SampleChatConnectionService extends Service implements
        ConnectivityReceiver.ConnectivityReceiverListener {
    private static final String LOGTAG = "RoosterConService";

    private boolean mActive;//Stores whether or not the thread is active
    private Thread mThread;
    private Handler mTHandler;//We use this handler to post messages to

    private ConnectivityReceiver mConnectivityReceiver;

    public static SampleChatConnection getConnection() {
        return mConnection;
    }

    //the background thread.
    private static SampleChatConnection mConnection;


    public SampleChatConnectionService() {
    }

    private void initConnection() {
        Log.d(LOGTAG, "initConnection()");
        if (mConnection == null) {
            mConnection = new SampleChatConnection(this);
        }

        try {
            mConnection.connect();
        } catch (IOException | SmackException | XMPPException e) {

            Log.d(LOGTAG, "Something went wrong while connecting ,make sure the credentials are right and try again");

            Intent i = new Intent(Constants.BroadCastMessages.UI_CONNECTION_ERROR);
            i.setPackage(getApplicationContext().getPackageName());
            getApplicationContext().sendBroadcast(i);
            Log.d(LOGTAG, "Sent the broadcast for connection Error from service");

            //Stop the service all together if user is not logged in already.
            boolean logged_in_state = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getBoolean("xmpp_logged_in", false);
            if (!logged_in_state) {
                Log.d(LOGTAG, "Logged in state :" + logged_in_state + "calling stopself()");

                stopSelf();

            } else {
                Log.d(LOGTAG, "Logged in state :" + logged_in_state);

            }

            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ServerPingWithAlarmManager.onCreate(this);
        Log.d(LOGTAG, " Service Created");
//        mConnectivityReceiver = new ConnectivityReceiver(this);
        Toast.makeText(this, "service created", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Do your task here
        start();
        Log.d(LOGTAG, " Service Started");
//        registerReceiver(mConnectivityReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(mConnectivityReceiver);
        ServerPingWithAlarmManager.onDestroy();

        stop();
    }

    public void start() {
        Log.d(LOGTAG, " Service Start() function called. mActive :" + mActive);
        if (!mActive) {
            mActive = true;
            if (mThread == null || !mThread.isAlive()) {
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Looper.prepare();
                        mTHandler = new Handler();
                        initConnection();
                        //THE CODE HERE RUNS IN A BACKGROUND THREAD.
                        Looper.loop();

                    }
                });
                mThread.start();
            }
        }

    }


    public void stop() {
        Log.d(LOGTAG, "stop()");
        mActive = false;
        mTHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mConnection != null) {
                    mConnection.disconnect();
                }
            }
        });

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.d(LOGTAG, "NetworkConnection Available: " + isConnected);
        String message = isConnected ? "Good! Connected to Internet" : "Sorry! Not connected to internet";
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

        if (isConnected) {
//            mTHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (mConnection != null) {
//                        mConnection.sendMessage("message from background", "ashraf8534@jabber.at");
//                        Log.d(LOGTAG, "message sent from  background");
//                    }
//                }
//            });
            startService(new Intent(this, SampleChatConnectionService.class));
        }


    }
}
