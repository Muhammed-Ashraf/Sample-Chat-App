package com.example.user.samplechatapp.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.user.samplechatapp.model.Chat;
import com.example.user.samplechatapp.model.ChatMessage;
import com.example.user.samplechatapp.model.Contact;


/**
 * Created by gakwaya on 2018/1/20.
 */

public class DatabaseBackend extends SQLiteOpenHelper {

    private static final String LOGTAG = "DatabaseBackend";
    private static DatabaseBackend instance = null;
    private static final String DATABASE_NAME = "roosterPlus_db";
    private static final int DATABASE_VERSION = 1;
    /**
     *  Version 1 :
     *      - Creates chatList , contactList and ChatMessageList tables;
     * */

    //SQL Statements

    //Create Chat List Table
    private static String CREATE_CHAT_LIST_STATEMENT = "create table "
            + Chat.TABLE_NAME + "("
            + Chat.Cols.CHAT_UNIQUE_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"
            + Chat.Cols.CONTACT_TYPE + " TEXT, " + Chat.Cols.CONTACT_JID + " TEXT,"
            + Chat.Cols.LAST_MESSAGE + " TEXT, " + Chat.Cols.UNREAD_COUNT + " NUMBER,"
            + Chat.Cols.LAST_MESSAGE_TIME_STAMP + " NUMBER"
            + ");";

    //Create Contact List Table
    private static String CREATE_CONTACT_LIST_STATEMENT = "create table "
            + Contact.TABLE_NAME + "("
            + Contact.Cols.CONTACT_UNIQUE_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"
            + Contact.Cols.SUBSCRIPTION_TYPE + " TEXT, " + Contact.Cols.CONTACT_JID + " TEXT,"
            + Contact.Cols.PROFILE_IMAGE_PATH + " TEXT"
            + ");";

    //Create Chat Message List Table
    private static String CREATE_CHAT_MESSAGES_STATEMENT = "create table "
            + ChatMessage.TABLE_NAME + "("
            + ChatMessage.Cols.CHAT_MESSAGE_UNIQUE_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ChatMessage.Cols.MESSAGE + " TEXT, "
            + ChatMessage.Cols.MESSAGE_TYPE + " TEXT, "
            + ChatMessage.Cols.TIMESTAMP + " NUMBER, "
            + ChatMessage.Cols.CONTACT_JID + " TEXT"
            + ");";

    private DatabaseBackend(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseBackend getInstance(Context context) {
        Log.d(LOGTAG,"Getting db instance");
        if (instance == null) {
            instance = new DatabaseBackend(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOGTAG,"Creating the tables");
        db.execSQL(CREATE_CONTACT_LIST_STATEMENT);
        db.execSQL(CREATE_CHAT_LIST_STATEMENT);
        db.execSQL(CREATE_CHAT_MESSAGES_STATEMENT);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
