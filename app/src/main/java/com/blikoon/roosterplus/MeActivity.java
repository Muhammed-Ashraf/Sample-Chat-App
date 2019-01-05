package com.blikoon.roosterplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.blikoon.roosterplus.xmpp.RoosterConnection;
import com.blikoon.roosterplus.xmpp.RoosterConnectionService;

public class MeActivity extends AppCompatActivity {

    private BroadcastReceiver mBroadcastReceiver;
    private TextView connectionStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        String status;
        RoosterConnection connection = RoosterConnectionService.getConnection();
        connectionStatusTextView = (TextView) findViewById(R.id.connection_status);

        if(  connection != null)
        {
            status = connection.getConnectionStateString();
            connectionStatusTextView.setText(status);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                switch (action)
                {
                    case Constants.BroadCastMessages.UI_CONNECTION_STATUS_CHANGE_FLAG:

                        String status = intent.getStringExtra(Constants.UI_CONNECTION_STATUS_CHANGE);
                        connectionStatusTextView.setText(status);
                        break;
                }



            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.BroadCastMessages.UI_CONNECTION_STATUS_CHANGE_FLAG);
        this.registerReceiver(mBroadcastReceiver, filter);
    }
}
