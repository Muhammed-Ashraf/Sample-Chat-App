package com.example.user.samplechatapp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.samplechatapp.R;
import com.example.user.samplechatapp.adapters.ChatMessagesAdapter;
import com.example.user.samplechatapp.model.Chat;
import com.example.user.samplechatapp.model.ChatMessagesModel;
import com.example.user.samplechatapp.model.Contact;
import com.example.user.samplechatapp.model.ContactModel;
import com.example.user.samplechatapp.util.Constants;
import com.example.user.samplechatapp.util.KeyboardUtil;
import com.example.user.samplechatapp.xmpp.SampleChatConnectionService;


public class ChatViewActivity extends AppCompatActivity implements ChatMessagesAdapter.OnInformRecyclerViewToScrollDownListener
        ,KeyboardUtil.KeyboardVisibilityListener,ChatMessagesAdapter.OnItemLongClickListener {

    private static final String LOGTAG = "ChatViewActivity" ;
    RecyclerView chatMessagesRecyclerView ;
    private EditText textSendEditText;
    private ImageButton sendMessageButton;
    ChatMessagesAdapter adapter;
    private String counterpartJid;
    private BroadcastReceiver mReceiveMessageBroadcastReceiver;

    private View snackbar;
    private View snackbarStranger;

    private TextView snackBarActionAccept;
    private TextView snackBarActionDeny;

    private TextView snackBarStrangerAddContact;
    private TextView snackBarStrangerBlock;

    private Chat.ContactType chatType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_view);

        //Get the counterpart Jid
        Intent intent = getIntent();
        counterpartJid = intent.getStringExtra("contact_jid");
        chatType = (Chat.ContactType)intent.getSerializableExtra("chat_type");
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

        Contact contactCheck = ContactModel.get(getApplicationContext()).getContactByJidString(counterpartJid);

        if(!ContactModel.get(getApplicationContext()).isContactStranger(counterpartJid))
        {
            if(contactCheck.isOnlineStatus())
            {
                Log.d(LOGTAG,counterpartJid + "is ONLINE");
                sendMessageButton.setImageDrawable(ContextCompat.getDrawable(ChatViewActivity.this,R.drawable.ic_send_text_online));
            }else
            {
                sendMessageButton.setImageDrawable(ContextCompat.getDrawable(ChatViewActivity.this,R.drawable.ic_send_text_offline));

                Log.d(LOGTAG,counterpartJid + "is OFFLINE");
            }

        }


        snackbar = findViewById(R.id.snackbar);
        snackbarStranger = findViewById(R.id.snackbar_stranger);

        if( !ContactModel.get(getApplicationContext()).isContactStranger(counterpartJid))
        {
            snackbarStranger.setVisibility(View.GONE);
            Log.d(LOGTAG,counterpartJid + " is not a stranger");
            Contact contact = ContactModel.get(this).getContactByJidString(counterpartJid);
            Log.d(LOGTAG,"We got a contact with JID :" + contact.getJid());

            if( contact.isPendingFrom())
            {
                Log.d(LOGTAG," Your subscription to "+ contact.getJid() + " is in the FROM direction is in pending state. Should show the snackbar");
                int paddingBottom = getResources().getDimensionPixelOffset(R.dimen.chatview_recycler_view_padding_huge);
                chatMessagesRecyclerView.setPadding(0,0,0,paddingBottom);
                snackbar.setVisibility(View.VISIBLE);
            }else
            {
                int paddingBottom = getResources().getDimensionPixelOffset(R.dimen.chatview_recycler_view_padding_normal);
                chatMessagesRecyclerView.setPadding(0,0,0,paddingBottom);
                snackbar.setVisibility(View.GONE);
            }

        }else
        {
            if(chatType == Chat.ContactType.STRANGER)
            {
                Log.d(LOGTAG,"Chat type is STRANGER");
                //We fall here if this was a subscription request from a stranger
                int paddingBottom = getResources().getDimensionPixelOffset(R.dimen.chatview_recycler_view_padding_huge);
                chatMessagesRecyclerView.setPadding(0,0,0,paddingBottom);
                snackbar.setVisibility(View.VISIBLE);
                snackbarStranger.setVisibility(View.GONE);

            }else
            {
                Log.d(LOGTAG,counterpartJid + " is a stranger. Hiding snackbar");
                int paddingBottom = getResources().getDimensionPixelOffset(R.dimen.chatview_recycler_view_padding_huge);
                chatMessagesRecyclerView.setPadding(0,0,0,paddingBottom);
                snackbarStranger.setVisibility(View.VISIBLE);
                snackbar.setVisibility(View.GONE);

            }


        }
        snackBarActionAccept = (TextView) findViewById(R.id.snackbar_action_accept);
        snackBarActionAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //User accepts presence subscription

                //Add Them to your roster if they are strangers
                if (ContactModel.get(getApplicationContext()).isContactStranger(counterpartJid))
                {
                    if(ContactModel.get(getApplicationContext()).addContact(new Contact(counterpartJid, Contact.SubscriptionType.NONE)))
                    {
                        Log.d(LOGTAG,"Previously stranger contact "+counterpartJid + "now successfully added to local Roster");
                    }
                }
                Log.d(LOGTAG," Accept presence subscription from :" + counterpartJid);
                if(SampleChatConnectionService.getConnection().subscribed(counterpartJid))
                {
                    ContactModel.get(getApplicationContext()).updateContactSubscriptionOnSendSubscribed(counterpartJid);
                    Toast.makeText(ChatViewActivity.this,"Subscription from "+counterpartJid + "accepted",
                            Toast.LENGTH_LONG).show();
                }
                snackbar.setVisibility(View.GONE);

            }
        });

        snackBarActionDeny = (TextView) findViewById(R.id.snackbar_action_deny);
        snackBarActionDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //User denies presence subscription
                Log.d(LOGTAG," Deny presence subscription from :" + counterpartJid);
                if(SampleChatConnectionService.getConnection().unsubscribed(counterpartJid))
                {
                    ContactModel.get(getApplicationContext()).updateContactSubscriptionOnSendSubscribed(counterpartJid);

                    //No action required in the Contact Model regarding subscriptions
                    Toast.makeText(getApplicationContext(),"Subscription Rejected",Toast.LENGTH_LONG).show();
                }
                snackbar.setVisibility(View.GONE);

            }
        });

        snackBarStrangerAddContact= findViewById(R.id.snackbar_action_accept_stranger);
        snackBarStrangerAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContactModel.get(getApplicationContext()).addContact(new Contact(counterpartJid, Contact.SubscriptionType.NONE)))
                {
                    if(SampleChatConnectionService.getConnection().addContactToRoster(counterpartJid))
                    {
                        Log.d(LOGTAG,counterpartJid + " successfully added to remote roster");
                        snackbarStranger.setVisibility(View.GONE);
                    }
                }

            }
        });

        snackBarStrangerBlock = findViewById(R.id.snackbar_action_deny_stranger);
        snackBarStrangerBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatViewActivity.this,"Feature not implemented yet",Toast.LENGTH_SHORT).show();


            }
        });

        KeyboardUtil.setKeyboardVisibilityListener(this,this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if ( item.getItemId() == R.id.contact_details_chat_view)
        {
            Intent i = new Intent(ChatViewActivity.this,ContactDetailsActivity.class);
            i.putExtra("contact_jid",counterpartJid);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
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
                    case Constants.BroadCastMessages.UI_ONLINE_STATUS_CHANGE:
                        String contactJid = intent.getStringExtra(Constants.ONLINE_STATUS_CHANGE_CONTACT);
                        Log.d(LOGTAG," Online status change for "+contactJid + " received in ChatViewActivity");

                        if(!ContactModel.get(getApplicationContext()).isContactStranger(counterpartJid))
                        {
                            if(counterpartJid.equals(contactJid))
                            {
                                Contact mContact = ContactModel.get(getApplicationContext()).getContactByJidString(contactJid);
                                if(mContact.isOnlineStatus())
                                {
                                    Log.d(LOGTAG,"From Chat View, user " +contactJid + " has come ONLINE");
                                    sendMessageButton.setImageDrawable(ContextCompat.getDrawable(ChatViewActivity.this,R.drawable.ic_send_text_online));

                                }else
                                {
                                    Log.d(LOGTAG,"From Chat View, user " +contactJid + " has gone OFFLINE");
                                    sendMessageButton.setImageDrawable(ContextCompat.getDrawable(ChatViewActivity.this,R.drawable.ic_send_text_offline));

                                }
                            }

                        }
                        return;
                }

            }
        };

        IntentFilter filter = new IntentFilter(Constants.BroadCastMessages.UI_NEW_MESSAGE_FLAG);
        filter.addAction(Constants.BroadCastMessages.UI_ONLINE_STATUS_CHANGE);
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
