package com.example.user.samplechatapp.xmpp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


import com.example.user.samplechatapp.model.ChatMessage;
import com.example.user.samplechatapp.model.ChatMessagesModel;
import com.example.user.samplechatapp.util.Constants;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;

/**
 * Created by gakwaya on 2018/1/11.
 */

public class SampleChatConnection implements ConnectionListener {

    private static final String LOGTAG = "SampleChatConnection";

    private  final Context mApplicationContext;
    private String mUsername;
    private String mPassword;
    private String mServiceName;
    private XMPPTCPConnection mConnection;
    private ConnectionState mConnectionState;
    private PingManager pingManager;
    private ChatManager chatManager;

    public static enum ConnectionState
    {
        OFFLINE,CONNECTING,ONLINE
    }

    public ConnectionState getmConnectionState() {
        return mConnectionState;
    }

    public void setmConnectionState(ConnectionState mConnectionState) {
        this.mConnectionState = mConnectionState;
    }

    public String getConnectionStateString()
    {
        switch ( mConnectionState)
        {
            case OFFLINE:
                return  "Offline";

            case CONNECTING:
                return  "Connecting...";

            case ONLINE:
                return  "Online";

            default:
                return  "Offline";
        }

    }

    private void updateActivitiesOfConnectionStateChange( ConnectionState mConnectionState)
    {
        ConnectionState connectionState = mConnectionState;
        String status;
        switch ( mConnectionState)
        {
            case OFFLINE:
                status = "Offline";
                break;
            case CONNECTING:
                status = "Connecting...";
                break;
            case ONLINE:
                status = "Online";
                break;
            default:
                status = "Offline";
                break;
        }
        Intent i = new Intent(Constants.BroadCastMessages.UI_CONNECTION_STATUS_CHANGE_FLAG);
        i.putExtra(Constants.UI_CONNECTION_STATUS_CHANGE,status);
        i.setPackage(mApplicationContext.getPackageName());
        mApplicationContext.sendBroadcast(i);
    }

    public SampleChatConnection(Context mApplicationContext) {

        Log.d(LOGTAG,"SampleChatConnection Constructor called.");
        this.mApplicationContext = mApplicationContext;
    }

    public void connect() throws IOException,XMPPException,SmackException
    {

        mConnectionState = ConnectionState.CONNECTING;
        updateActivitiesOfConnectionStateChange(ConnectionState.CONNECTING);
        gatherCredentials();

        XMPPTCPConnectionConfiguration conf = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain(mServiceName)
                .setHost(mServiceName)
                .setResource("Rooster+")

                //Was facing this issue
                //https://discourse.igniterealtime.org/t/connection-with-ssl-fails-with-java-security-keystoreexception-jks-not-found/62566
                .setKeystoreType(null) //This line seems to get rid of the problem

                .setSendPresence(true)

                .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
                .setCompressionEnabled(true).build();

        SmackConfiguration.DEBUG = true;
        XMPPTCPConnection.setUseStreamManagementDefault(true);


        mConnection = new XMPPTCPConnection(conf);
        mConnection.setUseStreamManagement(true);
        mConnection.setUseStreamManagementResumption(true);
        mConnection.setPreferredResumptionTime(5);
        mConnection.addConnectionListener(this);



        chatManager = ChatManager.getInstanceFor(mConnection);
        chatManager.addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                Log.d(LOGTAG,"message.getBody() :"+message.getBody());
                Log.d(LOGTAG,"message.getFrom() :"+message.getFrom());

                String messageSource = message.getFrom().toString();

                String contactJid="";
                if ( messageSource.contains("/"))
                {
                    contactJid = messageSource.split("/")[0];
                    Log.d(LOGTAG,"The real jid is :" +contactJid);
                    Log.d(LOGTAG,"The message is from :" +from);
                }else
                {
                    contactJid=messageSource;
                }

                //Add message to the model
                ChatMessagesModel.get(mApplicationContext).addMessage(new ChatMessage(message.getBody(),System.currentTimeMillis(), ChatMessage.Type.RECEIVED,contactJid));

                //If the view (ChatViewActivity) is visible, inform it so it can do necessary adjustments
                Intent intent = new Intent(Constants.BroadCastMessages.UI_NEW_MESSAGE_FLAG);
                intent.setPackage(mApplicationContext.getPackageName());
                mApplicationContext.sendBroadcast(intent);



            }
        });


        ServerPingWithAlarmManager.getInstanceFor(mConnection).setEnabled(true);
        pingManager = PingManager.getInstanceFor(mConnection);
        pingManager.setPingInterval(30);

        try {
            Log.d(LOGTAG, "Calling connect() ");
            mConnection.connect();
            mConnection.login(mUsername,mPassword);
            Log.d(LOGTAG, " login() Called ");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void disconnect ()
    {
        Log.d(LOGTAG,"Disconnecting from server "+ mServiceName);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
        prefs.edit().putBoolean("xmpp_logged_in",false).commit();

        if (mConnection != null)
        {
            mConnection.disconnect();
        }
    }

    public void sendMessage (String body , String toJid)
    {
        Log.d(LOGTAG,"Sending message to :"+ toJid);

        EntityBareJid jid = null;

        try {
            jid = JidCreate.entityBareFrom(toJid);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        Chat chat = chatManager.chatWith(jid);
        try {
            Message message = new Message(jid, Message.Type.chat);
            message.setBody(body);
            chat.send(message);
            //Add the message to the model
            ChatMessagesModel.get(mApplicationContext).addMessage(new ChatMessage(body,System.currentTimeMillis(), ChatMessage.Type.SENT,toJid));

        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }



    private void gatherCredentials()
    {
        String jid = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_jid",null);
        mPassword = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_password",null);


        if( jid != null)
        {
            mUsername = jid.split("@")[0];
            mServiceName = jid.split("@")[1];
        }else
        {
            mUsername ="";
            mServiceName="";
        }
    }

    private void notifyUiForConnectionError()
    {
        Intent i = new Intent(Constants.BroadCastMessages.UI_CONNECTION_ERROR);
        i.setPackage(mApplicationContext.getPackageName());
        mApplicationContext.sendBroadcast(i);
        Log.d(LOGTAG,"Sent the broadcast for connection Error");
    }

    @Override
    public void connected(XMPPConnection connection) {

        Log.d(LOGTAG,"Connected");
        mConnectionState = ConnectionState.CONNECTING;
        updateActivitiesOfConnectionStateChange(ConnectionState.CONNECTING);

    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        mConnectionState = ConnectionState.ONLINE;
        updateActivitiesOfConnectionStateChange(ConnectionState.ONLINE);


        Log.d(LOGTAG,"Authenticated");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
        prefs.edit()
                .putBoolean("xmpp_logged_in",true)
                .commit();

        Intent i = new Intent(Constants.BroadCastMessages.UI_AUTHENTICATED);
        i.setPackage(mApplicationContext.getPackageName());
        mApplicationContext.sendBroadcast(i);
        Log.d(LOGTAG,"Sent the broadcast that we are authenticated");
    }

    @Override
    public void connectionClosed() {
        Log.d(LOGTAG,"connectionClosed");
        notifyUiForConnectionError();
        mConnectionState = ConnectionState.OFFLINE;
        updateActivitiesOfConnectionStateChange(ConnectionState.OFFLINE);

    }

    @Override
    public void connectionClosedOnError(Exception e) {
        Log.d(LOGTAG,"connectionClosedOnError");
        notifyUiForConnectionError();
        mConnectionState = ConnectionState.OFFLINE;
        updateActivitiesOfConnectionStateChange(ConnectionState.OFFLINE);


    }

//    @Override
//    public void reconnectionSuccessful() {
//        Log.d(LOGTAG,"reconnectionSuccessful");
//        mConnectionState = ConnectionState.ONLINE;
//        updateActivitiesOfConnectionStateChange(ConnectionState.ONLINE);
//
//
//    }
//
//    @Override
//    public void reconnectingIn(int seconds) {
//        Log.d(LOGTAG,"Reconnecting in " + seconds + "seconds");
//        mConnectionState = ConnectionState.CONNECTING;
//        updateActivitiesOfConnectionStateChange(ConnectionState.CONNECTING);
//
//
//    }
//
//    @Override
//    public void reconnectionFailed(Exception e) {
//        Log.d(LOGTAG,"reconnectionFailed");
//        mConnectionState = ConnectionState.OFFLINE;
//        updateActivitiesOfConnectionStateChange(ConnectionState.OFFLINE);
//
//
//    }
}
