package com.example.user.samplechatapp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.user.samplechatapp.R;
import com.example.user.samplechatapp.adapters.ChatMessagesAdapter;
import com.example.user.samplechatapp.model.ChatMessagesModel;
import com.example.user.samplechatapp.util.Constants;
import com.example.user.samplechatapp.util.KeyboardUtil;
import com.example.user.samplechatapp.xmpp.SampleChatConnectionService;


public class ChatViewActivity extends AppCompatActivity implements ChatMessagesAdapter.OnInformRecyclerViewToScrollDownListener
        ,KeyboardUtil.KeyboardVisibilityListener,ChatMessagesAdapter.OnItemLongClickListener {

    RecyclerView chatMessagesRecyclerView ;
    private EditText textSendEditText;
    private ImageButton sendMessageButton;
    ChatMessagesAdapter adapter;
    private String counterpartJid;
    private BroadcastReceiver mReceiveMessageBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_view);

        //Get the counterpart Jid
        Intent intent = getIntent();
        counterpartJid = intent.getStringExtra("contact_jid");
        setTitle(counterpartJid);

        chatMessagesRecyclerView = (RecyclerView) findViewById(R.id.chatMessagesRecyclerView);
        chatMessagesRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));

        adapter = new ChatMessagesAdapter(getApplicationContext(),counterpartJid);
        adapter.setmOnInformRecyclerViewToScrollDownListener(this);
        adapter.setOnItemLongClickListener(this);
        chatMessagesRecyclerView.setAdapter(adapter);

        textSendEditText = (EditText) findViewById(R.id.textinput);
        sendMessageButton = (ImageButton) findViewById(R.id.textSendButton);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SampleChatConnectionService.getConnection().sendMessage(textSendEditText.getText().toString(),counterpartJid);
                adapter.onMessageAdd();
                textSendEditText.getText().clear();

            }
        });

        KeyboardUtil.setKeyboardVisibilityListener(this,this);
    }




    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiveMessageBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        adapter.informRecyclerViewToScrollDown();

        mReceiveMessageBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action)
                {
                    case Constants.BroadCastMessages.UI_NEW_MESSAGE_FLAG:
                        adapter.onMessageAdd();
                        return;
                }

            }
        };

        IntentFilter filter = new IntentFilter(Constants.BroadCastMessages.UI_NEW_MESSAGE_FLAG);
        registerReceiver(mReceiveMessageBroadcastReceiver,filter);
    }

    @Override
    public void onInformRecyclerViewToScrollDown(int size) {
        chatMessagesRecyclerView.scrollToPosition(size-1);

    }

    @Override
    public void onKeyboardVisibilityChanged(boolean keyboardVisible) {
        adapter.informRecyclerViewToScrollDown();
    }

    @Override
    public void onItemLongClick(final  int uniqueId, View anchor) {

        PopupMenu popup = new PopupMenu(ChatViewActivity.this,anchor, Gravity.CENTER);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(R.menu.chat_view_popup_menu, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch( item.getItemId())
                {
                    case R.id.delete_message :
                        if(ChatMessagesModel.get(getApplicationContext()).deleteMessage(uniqueId) )
                        {
                            adapter.onMessageAdd();
                            Toast.makeText(
                                    ChatViewActivity.this,
                                    "Message deleted successfully ",
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
