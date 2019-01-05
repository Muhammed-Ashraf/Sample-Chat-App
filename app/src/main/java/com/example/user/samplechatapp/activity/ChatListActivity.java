package com.example.user.samplechatapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.user.samplechatapp.R;
import com.example.user.samplechatapp.adapters.ChatListAdapter;
import com.example.user.samplechatapp.model.ChatModel;
import com.example.user.samplechatapp.util.Utilities;
import com.example.user.samplechatapp.xmpp.SampleChatConnectionService;


public class ChatListActivity extends AppCompatActivity implements ChatListAdapter.OnItemClickListener,ChatListAdapter.OnItemLongClickListener {

    private static final String LOGTAG = "ChatListActivity";
    private RecyclerView chatsRecyclerView;
    private FloatingActionButton newConversationButton;
    protected static final int REQUEST_EXCEMPT_OP = 188;
    ChatListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);



        boolean logged_in_state = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("xmpp_logged_in",false);
        if(!logged_in_state)
        {
            Log.d(LOGTAG,"Logged in state :"+ logged_in_state );
            Intent i = new Intent(ChatListActivity.this,LoginActivity.class);
            startActivity(i);
            finish();
        }else
        {
            if(!Utilities.isServiceRunning(SampleChatConnectionService.class,getApplicationContext()))
            {
                Log.d(LOGTAG,"Service not running, starting it ...");
                //Start the service
                Intent i1 = new Intent(this,SampleChatConnectionService.class);
                startService(i1);

            }else
            {
                Log.d(LOGTAG ,"The service is already running.");
            }

        }


        chatsRecyclerView = (RecyclerView) findViewById(R.id.chatsRecyclerView);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));

        mAdapter = new ChatListAdapter(getApplicationContext());
        mAdapter.setmOnItemClickListener(this);
        mAdapter.setOnItemLongClickListener(this);
        chatsRecyclerView.setAdapter(mAdapter);

        newConversationButton = (FloatingActionButton) findViewById(R.id.new_conversation_floating_button);
        newConversationButton.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
        newConversationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(ChatListActivity.this,ContactListActivity.class);
                startActivity(i);

            }
        });

        boolean deniedBatteryOptimizationRequest = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("denied_battery_optimization_request",false);

        boolean userHasGoneThroughBatteryOptimizations = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("user_has_gone_through_battery_optimizations",false);

        if( !deniedBatteryOptimizationRequest && !userHasGoneThroughBatteryOptimizations)
        {
            requestBatteryOptimizationsFavor();
        }else
        {
            Log.d(LOGTAG,"User has chosen to opt out of battery optimizations excemption. DONT'T BOTHER THEM AGAIN.");
        }



    }

    private void requestBatteryOptimizationsFavor()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setTitle("Battery optimization request");
            builder.setMessage("Battery optimizations are needed to make the app work right");

            // Set up the buttons
            builder.setPositiveButton(R.string.ignore_bat_allow, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(LOGTAG,"User clicked on OK");

                    Intent intent = new Intent();
                    String packageName = getPackageName();

                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    startActivityForResult(intent,REQUEST_EXCEMPT_OP);


                }
            });
            builder.setNegativeButton(R.string.add_contact_cancel_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(LOGTAG,"User clicked on Cancel");
                    //Save the user's choice and never bother them again.
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    prefs.edit().putBoolean("denied_battery_optimization_request",true).commit();
                    dialog.cancel();
                }
            });
            builder.show();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
        {
            if ( requestCode == REQUEST_EXCEMPT_OP)
            {
                Log.d(LOGTAG,"User wants to excempt app from BATTERY OPTIMIZATIONS");
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent intent = new Intent();
                    String packageName = getPackageName();
                    PowerManager pm = (PowerManager) getSystemService(getApplicationContext().POWER_SERVICE);
                    if (pm.isIgnoringBatteryOptimizations(packageName))
                    {
                        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        startActivity(intent);
                    }

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    prefs.edit().putBoolean("user_has_gone_through_battery_optimizations",true).commit();

                }
            }

        }else
        {
            if ( requestCode == REQUEST_EXCEMPT_OP)
            {
                Log.d(LOGTAG,"Result code is cancel");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_me_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if( item.getItemId()  == R.id.me)
        {
            Intent i = new Intent(ChatListActivity.this,MeActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(String contactJid) {

        Intent i = new Intent(ChatListActivity.this,ChatViewActivity.class);
        i.putExtra("contact_jid",contactJid);
        startActivity(i);
    }

    @Override
    public void onItemLongClick(final String contactJid, final int chatUniqueId, View anchor) {

        PopupMenu popup = new PopupMenu(ChatListActivity.this,anchor, Gravity.CENTER);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(R.menu.chat_list_popup_menu, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch( item.getItemId())
                {
                    case R.id.delete_chat :
                        if(ChatModel.get(getApplicationContext()).deleteChat(chatUniqueId) )
                        {
                            mAdapter.onChatCountChange();
                            Toast.makeText(
                                    ChatListActivity.this,
                                    "Chat deleted successfully ",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                        break;
                }
                return true;
            }
        });
        popup.show();

    }
}
