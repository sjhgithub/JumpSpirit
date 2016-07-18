package cn.mailchat.cache;

import java.io.File;
import java.io.IOException;

import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.helper.Utility;

import android.content.Context;
import android.util.Log;

public class TemporaryAttachmentStore {
    private static long MAX_FILE_AGE = 12 * 60 * 60 * 1000;   // 12h

    public static File getFile(Context context, String attachmentName) {
        File directory = getTemporaryAttachmentDirectory(context);
        String filename = Utility.sanitizeFilename(attachmentName);
        return new File(directory, filename);
    }

    public static File getFileForWriting(Context context, String attachmentName) throws IOException {
        File directory = createOrCleanAttachmentDirectory(context);
        String filename = Utility.sanitizeFilename(attachmentName);
        return new File(directory, filename);
    }

    private static File createOrCleanAttachmentDirectory(Context context) throws IOException {
        File directory = getTemporaryAttachmentDirectory(context);
        if (directory.exists()) {
            cleanOldFiles(directory);
        } else {
            if (!directory.mkdir()) {
                throw new IOException("Couldn't create temporary attachment store: " + directory.getAbsolutePath());
            }
        }
        return directory;
    }

    private static File getTemporaryAttachmentDirectory(Context context) {
    	Account account = Preferences.getPreferences(context).getDefaultAccount();
        return new File(MailChat.getInstance().getMailAttachmentDirectory(account));
    }

    private static void cleanOldFiles(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        long cutOffTime = System.currentTimeMillis() - MAX_FILE_AGE;
        for (File file : files) {
            if (file.lastModified() < cutOffTime) {
                if (file.delete()) {
                    if (MailChat.DEBUG) {
                        Log.d(MailChat.LOG_TAG, "Deleted from temporary attachment store: " + file.getName());
                    }
                } else {
                    Log.w(MailChat.LOG_TAG, "Couldn't delete from temporary attachment store: " + file.getName());
                }
            }
        }
    }
}