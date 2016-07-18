package cn.mailchat.activity.loader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.activity.misc.Attachment;

/**
 * Loader to fetch the content of an attachment.
 *
 * This will copy the data to a temporary file in our app's cache directory.
 */
public class AttachmentContentLoader extends AsyncTaskLoader<Attachment> {
    private static final String FILENAME_PREFIX = "attachment";

    private final Attachment mAttachment;

    public AttachmentContentLoader(Context context, Attachment attachment) {
        super(context);
        mAttachment = attachment;
    }

    @Override
    protected void onStartLoading() {
        if (mAttachment.state == Attachment.LoadingState.COMPLETE) {
            deliverResult(mAttachment);
        }

        if (takeContentChanged() || mAttachment.state == Attachment.LoadingState.METADATA) {
            forceLoad();
        }
    }

    @Override
    public Attachment loadInBackground() {
    	// 未下载附件需要特殊处理
    	// Modified by LL
    	// BEGIN
    	if (mAttachment.uri == null) {
    		return mAttachment;
    	}
    	// END
    	
        Context context = getContext();

        try {
            File file = File.createTempFile(FILENAME_PREFIX, null, context.getCacheDir());
            file.deleteOnExit();

            if (MailChat.DEBUG) {
                Log.v(MailChat.LOG_TAG, "Saving attachment to " + file.getAbsolutePath());
            }

            InputStream in = context.getContentResolver().openInputStream(mAttachment.uri);
            try {
                FileOutputStream out = new FileOutputStream(file);
                try {
                    IOUtils.copy(in, out);
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }

            mAttachment.filename = file.getAbsolutePath();
            mAttachment.state = Attachment.LoadingState.COMPLETE;

            return mAttachment;
        } catch (IOException e) {
            e.printStackTrace();
        }

        mAttachment.filename = null;
        mAttachment.state = Attachment.LoadingState.CANCELLED;
        
        // 附件加载失败提示
        MailChat.toast(getContext().getString(R.string.load_attachment_failed));

        return mAttachment;
    }
}
