package cn.mailchat.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import cn.mailchat.MailChat;
import cn.mailchat.mail.store.StorageManager;

/**
 * That BroadcastReceiver is only interested in MOUNT events.
 */
public class StorageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        final Uri uri = intent.getData();

        if (uri == null || uri.getPath() == null) {
            return;
        }

        if (MailChat.DEBUG) {
            Log.v(MailChat.LOG_TAG, "StorageReceiver: " + intent.toString());
        }

        final String path = uri.getPath();

        if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            StorageManager.getInstance(MailChat.app).onMount(path,
                    intent.getBooleanExtra("read-only", true));
        }
    }

}
