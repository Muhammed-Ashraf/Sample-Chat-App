package com.example.user.samplechatapp.persistence;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.example.user.samplechatapp.model.Contact;


/**
 * Created by gakwaya on 2018/1/20.
 */

public class ContactCursorWrapper extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public ContactCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Contact getContact() {

        String subscriptionTypeString = getString(getColumnIndex(Contact.Cols.SUBSCRIPTION_TYPE));
        String jid = getString(getColumnIndex(Contact.Cols.CONTACT_JID));
        int contactUniqueId = getInt(getColumnIndex(Contact.Cols.CONTACT_UNIQUE_ID));
        String profileImagePath = getString(getColumnIndex(Contact.Cols.PROFILE_IMAGE_PATH));


        Contact.SubscriptionType subscriptionType = null;

        if (subscriptionTypeString.equals("NONE_NONE")) {
            subscriptionType = Contact.SubscriptionType.NONE_NONE;
        } else if (subscriptionTypeString.equals("NONE_PENDING")) {
            subscriptionType = Contact.SubscriptionType.NONE_PENDING;
        } else if (subscriptionTypeString.equals("NONE_TO")) {
            subscriptionType = Contact.SubscriptionType.NONE_TO;
        } else if (subscriptionTypeString.equals("PENDING_NONE")) {
            subscriptionType = Contact.SubscriptionType.PENDING_NONE;
        }else if (subscriptionTypeString.equals("PENDING_PENDING")) {
            subscriptionType = Contact.SubscriptionType.PENDING_PENDING;
        } else if (subscriptionTypeString.equals("PENDING_TO")) {
            subscriptionType = Contact.SubscriptionType.PENDING_TO;
        }

        else if (subscriptionTypeString.equals("FROM_NONE")) {
            subscriptionType = Contact.SubscriptionType.FROM_NONE;
        }else if (subscriptionTypeString.equals("FROM_PENDING")) {
            subscriptionType = Contact.SubscriptionType.FROM_PENDING;
        } else if (subscriptionTypeString.equals("FROM_TO")) {
            subscriptionType = Contact.SubscriptionType.FROM_TO;
        }

        Contact contact = new Contact(jid, subscriptionType);
        contact.setPersistID(contactUniqueId);
        contact.setProfileImagePath(profileImagePath);
        return contact;
    }
}
