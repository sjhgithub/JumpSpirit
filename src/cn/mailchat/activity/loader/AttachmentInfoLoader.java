package cn.mailchat.activity.loader;

import java.io.File;
import java.io.UnsupportedEncodingException;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import cn.mailchat.MailChat;
import cn.mailchat.activity.misc.Attachment;
import cn.mailchat.mail.internet.MimeUtility;


/**
 * Loader to fetch metadata of an attachment.
 */
public class AttachmentInfoLoader  extends AsyncTaskLoader<Attachment> {
    private final Attachment mAttachment;

    public AttachmentInfoLoader(Context context, Attachment attachment) {
        super(context);
        mAttachment = attachment;
    }

    @Override
    protected void onStartLoading() {
    	/*
        if (mAttachment.state == Attachment.LoadingState.METADATA) {
            deliverResult(mAttachment);
        }

        if (takeContentChanged() || mAttachment.state == Attachment.LoadingState.URI_ONLY) {
            forceLoad();
        }
        */
    	// 未下载附件也被载入
    	// Modified by LL
    	// BEGIN
        if (takeContentChanged()
        		|| mAttachment.state == Attachment.LoadingState.URI_ONLY
        		|| mAttachment.state == Attachment.LoadingState.METADATA) {
            forceLoad();
        }
    	// END
    }

    @Override
    public Attachment loadInBackground() {
    	// 处理未下载附件和已具备完整元数据的已下载附件
    	// Modified by LL
    	// BEGIN
    	if (mAttachment.state == Attachment.LoadingState.METADATA) {
    		return mAttachment;
    	} else if (mAttachment.contentType != null
    			&& mAttachment.name != null
    			&& mAttachment.size != -1) {
    		mAttachment.state = Attachment.LoadingState.METADATA;
    		return mAttachment;
    	}
    	// END
    	
        Uri uri = mAttachment.uri;
        String contentType = mAttachment.contentType;

        long size = -1;
        String name = null;

        ContentResolver contentResolver = getContext().getContentResolver();

        Cursor metadataCursor = contentResolver.query(
                uri,
                new String[] { OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE },
                null,
                null,
                null);

        if (metadataCursor != null) {
            try {
                if (metadataCursor.moveToFirst()) {
                    name = metadataCursor.getString(0);
                    size = metadataCursor.getInt(1);
                }
            } finally {
                metadataCursor.close();
            }
        }
        
        if (mAttachment.name != null) {
        	name = mAttachment.name;
        }

        if (name == null) {
            name = uri.getLastPathSegment();
        }
        /*
        else {
        	name = MimeUtility.decode(name);
        }
        */

        String usableContentType = contentType;
        if ((usableContentType == null) || (usableContentType.indexOf('*') != -1)) {
            usableContentType = contentResolver.getType(uri);
        }
        if (usableContentType == null) {
            usableContentType = MimeUtility.getMimeTypeByExtension(name);
        }

        if (size <= 0) {
            String uriString = Uri.decode(uri.toString());
            if (uriString.startsWith("file://")) {
                Log.v(MailChat.LOG_TAG, uriString.substring("file://".length()));
                File f = new File(uriString.substring("file://".length()));
                size = f.length();
            } else {
                Log.v(MailChat.LOG_TAG, "Not a file: " + uriString);
            }
        } else {
            Log.v(MailChat.LOG_TAG, "old attachment.size: " + size);
        }
        Log.v(MailChat.LOG_TAG, "new attachment.size: " + size);

        mAttachment.contentType = usableContentType;
        mAttachment.name = name;
        mAttachment.size = size;
        mAttachment.state = Attachment.LoadingState.METADATA;

        return mAttachment;
    }
}
