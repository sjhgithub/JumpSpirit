package cn.mailchat.helper;

import android.util.Log;

import cn.mailchat.Account;
import cn.mailchat.Identity;
import cn.mailchat.MailChat;
import cn.mailchat.mail.Address;
import cn.mailchat.mail.Message;
import cn.mailchat.mail.MessagingException;

public class IdentityHelper {

    /**
     * Find the identity a message was sent to.
     *
     * @param account
     *         The account the message belongs to.
     * @param message
     *         The message to get the recipients from.
     *
     * @return The identity the message was sent to, or the account's default identity if it
     *         couldn't be determined which identity this message was sent to.
     *
     * @see Account#findIdentity(cn.mailchat.mail.Address)
     */
    public static Identity getRecipientIdentityFromMessage(Account account, Message message) {
        Identity recipient = null;

        try {
            for (Address address : message.getRecipients(Message.RecipientType.TO)) {
                Identity identity = account.findIdentity(address);
                if (identity != null) {
                    recipient = identity;
                    break;
                }
            }
            if (recipient == null) {
                Address[] ccAddresses = message.getRecipients(Message.RecipientType.CC);
                if (ccAddresses.length > 0) {
                    for (Address address : ccAddresses) {
                        Identity identity = account.findIdentity(address);
                        if (identity != null) {
                            recipient = identity;
                            break;
                        }
                    }
                }
            }
        } catch (MessagingException e) {
            Log.w(MailChat.LOG_TAG, "Error finding the identity this message was sent to", e);
        }

        if (recipient == null) {
            recipient = account.getIdentity(0);
        }

        return recipient;
    }
}
