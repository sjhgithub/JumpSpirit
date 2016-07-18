package cn.mailchat.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.activity.AttachmentPreviewActivity;
import cn.mailchat.activity.ForwardActivity;
import cn.mailchat.activity.MailDetialActivity;
import cn.mailchat.activity.WebViewWithErrorViewActivity;
import cn.mailchat.activity.misc.SingleAttachment;
import cn.mailchat.activity.setup.AccountSetupBasics;
import cn.mailchat.cache.TemporaryAttachmentStore;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.helper.SizeFormatter;
import cn.mailchat.helper.Utility;
import cn.mailchat.mail.Message;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.mail.Part;
import cn.mailchat.mail.internet.MimeHeader;
import cn.mailchat.mail.internet.MimeUtility;
import cn.mailchat.mail.store.LocalStore.LocalAttachmentBodyPart;
import cn.mailchat.mail.store.LocalStore.LocalMessage;
import cn.mailchat.provider.AttachmentProvider;
import cn.mailchat.service.NotificationService;
import cn.mailchat.utils.AttachmentUtil;
import cn.mailchat.utils.WeemailUtil;

public class AttachmentView extends LinearLayout implements OnClickListener {

	public String mDownloadingId;
	public DownloadAttachmentNotification mDownloadingNotification;
	
	public enum Status {METADATA, DOWNLOADING, COMPLETE};
	public Status mStatus;
	
	public boolean mIsTemp = false;
	
	public AttachmentSquareView mAttachmentSquare;
	public AttachmentPreviewActivity mAttachmentPreviewActivity;
	
	private ProgressBar mProgressBar;
	private TextView mAttachmentName;
	private TextView mAttachmentInfo;
	private TextView mAttachmentHint;
	private ImageView mMenuButton;
	private ImageView mCancelButton;
	private PopupWindow mPopupMenuWindow;

    private Context mContext;
    public LocalAttachmentBodyPart mPart;
    public Message mMessage;
    public Account mAccount;
    private MessagingController mController;
    private MessagingListener mListener;
    public String mName;
    public String mContentType;
    public long mSize;
    public ImageView mIconView;

    private AttachmentFileDownloadCallback callback;

    // Store inline attachment's Content-ID which also serve to distinguish
    // inline attachment from normal one ( contentId == null ).
    public String contentId;

    public AttachmentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }
    public AttachmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
    public AttachmentView(Context context) {
        super(context);
        mContext = context;
    }


    public interface AttachmentFileDownloadCallback {
        /**
         * this method i called by the attachmentview when
         * he wants to show a filebrowser
         * the provider should show the filebrowser activity
         * and save the reference to the attachment view for later.
         * in his onActivityResult he can get the saved reference and
         * call the saveFile method of AttachmentView
         * @param view
         */
        public void showFileBrowser(AttachmentView caller);
    }

    /**
     * Populates this view with information about the attachment.
     *
     * <p>
     * This method also decides which attachments are displayed when the "show attachments" button
     * is pressed, and which attachments are only displayed after the "show more attachments"
     * button was pressed.<br>
     * Inline attachments with content ID and unnamed attachments fall into the second category.
     * </p>
     *
     * @param inputPart
     * @param message
     * @param account
     * @param controller
     * @param listener
     *
     * @return {@code true} for a regular attachment. {@code false}, otherwise.
     *
     * @throws MessagingException
     *          In case of an error
     */
    public boolean populateFromPart(Part inputPart, Message message, Account account,
            MessagingController controller, MessagingListener listener) throws MessagingException {
        boolean firstClassAttachment = true;
        mPart = (LocalAttachmentBodyPart) inputPart;

        mContentType = MimeUtility.unfoldAndDecode(mPart.getContentType());
        String contentDisposition = MimeUtility.unfoldAndDecode(mPart.getDisposition());
        
        // Fetch Content-ID
        contentId = MimeUtility.unfoldAndDecode(mPart.getContentId());

        mName = MimeUtility.getHeaderParameter(mContentType, "name");
        if (mName == null) {
            mName = MimeUtility.getHeaderParameter(contentDisposition, "filename");
        }

        if (mName == null) {
            String extension = MimeUtility.getExtensionByMimeType(mContentType);
            mName = mContext.getString(R.string.attachment_noname)
                    + ((extension != null) ? "." + extension : "");
        }
        
        /*
        // Inline parts with a content-id are almost certainly components of an HTML message
        // not attachments. Only show them if the user pressed the button to show more
        // attachments.
        if (contentDisposition != null
				&& MimeUtility.getHeaderParameter(contentDisposition, null).matches("^(?i:inline)") 
				&& part.getHeader(MimeHeader.HEADER_CONTENT_ID) != null) {        		
            firstClassAttachment = false;
        }
        */
        // Judge inline attachment
        LocalMessage lmsg = (LocalMessage)message;
        String lmsgText = lmsg.getTextForDisplay();
        if (contentId != null && lmsgText != null && lmsgText.contains("cid:" + contentId)) {
            firstClassAttachment = false;
        }

        mAccount = account;
        mMessage = message;
        mController = controller;
        mListener = listener;

        String sizeParam = MimeUtility.getHeaderParameter(contentDisposition, "size");
        if (sizeParam != null) {
            try {
                mSize = Integer.parseInt(sizeParam);
            } catch (NumberFormatException e) { /* ignore */ }
        }

        mContentType = MimeUtility.getMimeTypeForViewing(mPart.getMimeType(), mName);
        final ImageView attachmentIcon = (ImageView) findViewById(R.id.attachment_icon);
        mAttachmentName = (TextView) findViewById(R.id.attachment_name);
        mAttachmentInfo = (TextView) findViewById(R.id.attachment_info);
        mAttachmentHint = (TextView) findViewById(R.id.attachment_hint);
        mProgressBar = (ProgressBar) findViewById(R.id.attachment_progress);
        mMenuButton = (ImageView) findViewById(R.id.attachment_menu);
        mCancelButton = (ImageView) findViewById(R.id.attachment_cancel);

        /*
        if ((!MimeUtility.mimeTypeMatches(contentType, MailChat.ACCEPTABLE_ATTACHMENT_VIEW_TYPES))
                || (MimeUtility.mimeTypeMatches(contentType, MailChat.UNACCEPTABLE_ATTACHMENT_VIEW_TYPES))) {
            viewButton.setVisibility(View.GONE);
        }
        if ((!MimeUtility.mimeTypeMatches(contentType, MailChat.ACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES))
                || (MimeUtility.mimeTypeMatches(contentType, MailChat.UNACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES))) {
            downloadButton.setVisibility(View.GONE);
        }
        if (size > MailChat.MAX_ATTACHMENT_DOWNLOAD_SIZE) {
            viewButton.setVisibility(View.GONE);
            downloadButton.setVisibility(View.GONE);
        }
        
        viewButton.setOnClickListener(this);
        downloadButton.setOnClickListener(this);
        downloadButton.setOnLongClickListener(this);
        */

        mMenuButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        findViewById(R.id.rl_attachment_info).setOnClickListener(this);
        
        mAttachmentName.setText(mName);
        
        //attachmentInfo.setText(SizeFormatter.formatSize(mContext, size));
        // 未下载附件显示预计实际文件大小
        if (mPart.getBody() == null) {
        	mAttachmentInfo.setText(mContext.getString(R.string.predicted_attachment_size_prefix)
        			+ SizeFormatter.formatSize(mContext, MimeUtility.getDecodeBase64Size(mSize)));
        } else {
        	mAttachmentInfo.setText(SizeFormatter.formatSize(mContext, mSize));
        }
        
        new AsyncTask<Void, Void, Bitmap>() {
            protected Bitmap doInBackground(Void... asyncTaskArgs) {
                Bitmap previewIcon = getPreviewIcon();
                return previewIcon;
            }

            protected void onPostExecute(Bitmap previewIcon) {
                if (previewIcon != null) {
                    attachmentIcon.setImageBitmap(previewIcon);
                } else {
                	attachmentIcon.setImageBitmap(AttachmentUtil.getInstance(mContext).getAttachmentIcon(mName, false));
                }
            }
        }.execute();
        
        // Update downloadingId and downloadingList.
        // TODO 检查由于message对象不一致导致的附件重复下载问题
        mDownloadingId = mPart.getAttachmentId() + message.getUid() + account.getUuid();
        synchronized(MailChat.downloadingList) {
	        if (mPart.getBody() == null) {
	        	if (MailChat.downloadingList.containsKey(mDownloadingId)) {
	        		AttachmentView av = MailChat.downloadingList.put(mDownloadingId, this);
	        		mStatus = av.mStatus;
	        		switch(mStatus) {
	        		case METADATA:
	        			loadAttachmentFailed();
	        			break;
	        		case DOWNLOADING:
	        			loadAttachmentStarted();
	        			break;
	        		case COMPLETE:
	        			loadAttachmentFinished();
	        			break;
	        		}
	        		
	        	} else {
	        		mStatus = AttachmentView.Status.METADATA;
	        		loadAttachmentFailed();
	        	}
	        } else {
	        	mStatus = AttachmentView.Status.COMPLETE;
	        	loadAttachmentFinished();
	        }
        }

        return firstClassAttachment;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.rl_attachment_info:
            click();
        	break;
        	
        case R.id.attachment_menu:
            this.postInvalidate();
            int version = mAccount.getVersion_35Mail();
            showPopupMenu(MailChat.is35CloudServices() && version == 1);
        	break;
        	
        case R.id.attachment_cancel:
            cancel();
            break;
        }
    }
    
    public void open() {
    	synchronized(MailChat.downloadingList) {
        	if (mPart.getBody() == null) {
        		if (mStatus == AttachmentView.Status.METADATA) {
        			if (MimeUtility.isLocalUid(mMessage.getUid())) {
        				MailChat.toast(mContext.getString(R.string.cannot_download_attachment_from_unsynced_message));
        			} else {
	        			mStatus = AttachmentView.Status.DOWNLOADING;
	        			MailChat.downloadingList.put(mDownloadingId, this);
	        			loadAttachment();
        			}
        		}
        	} else {
        		if (mDownloadingNotification != null) {
        			mDownloadingNotification.removeNotification();
        		}
        		openAttachment();
        	}
    	}
    }

    public void cancel() {
        mDownloadingNotification.isCanceled.set(true);
        mDownloadingNotification.removeNotification();
        mStatus = AttachmentView.Status.METADATA;
        loadAttachmentFailed();
    }

    public void preview() {
        if (MimeUtility.isLocalUid(mMessage.getUid())) {
            MailChat.toast(mContext.getString(R.string.cannot_preview_attachment_from_unsynced_message));
        } else {
            AttachmentPreviewActivity.sAttachmentView = this;
            AttachmentPreviewActivity.actionPreview(mContext);
        }
    }

    public void click() {
        if (MailChat.is35CloudServices()
                && mAccount.getVersion_35Mail() == 1
                && mStatus == AttachmentView.Status.METADATA) {
            preview();
        } else {
            open();
        }
    }

    private Bitmap getPreviewIcon() {
        Bitmap icon = null;
        try {
            InputStream input = mContext.getContentResolver().openInputStream(
                           AttachmentProvider.getAttachmentThumbnailUri(mAccount,
                                   mPart.getAttachmentId(),
                                   62,
                                   62));
            icon = BitmapFactory.decodeStream(input);
            input.close();
        } catch (Exception e) {
            /*
             * We don't care what happened, we just return null for the preview icon.
             */
        }
        return icon;
    }

    /**
     * Writes the attachment onto the given path
     * @param directory: the base dir where the file should be saved.
     */
    public void writeFile(File directory) {
        try {
            String filename = Utility.sanitizeFilename(mName);
            File file = Utility.createUniqueFile(directory, filename);
            Uri uri = AttachmentProvider.getAttachmentUri(mAccount, mPart.getAttachmentId());
            InputStream in = mContext.getContentResolver().openInputStream(uri);
            OutputStream out = new FileOutputStream(file);
            IOUtils.copy(in, out);
            out.flush();
            out.close();
            in.close();
            attachmentSaved(file.toString());
            //new MediaScannerNotifier(mContext, file);
        } catch (IOException ioe) {
            if (MailChat.DEBUG) {
                Log.e(MailChat.LOG_TAG, "Error saving attachment", ioe);
            }
            attachmentNotSaved();
        }
    }

    /**
     * saves the file to the defaultpath setting in the config, or if the config
     * is not set => to the Environment
     */
    public void writeFile() {
        writeFile(new File(MailChat.getAttachmentDefaultPath()));
    }

    public void saveFile() {
        //TODO: Can the user save attachments on the internal filesystem or sd card only?
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            /*
             * Abort early if there's no place to save the attachment. We don't want to spend
             * the time downloading it and then abort.
             */
            Toast.makeText(mContext,
                           mContext.getString(R.string.message_view_status_attachment_not_saved),
                           Toast.LENGTH_SHORT).show();
            return;
        }
        if (mMessage != null) {
            mController.loadAttachment(mAccount, mMessage, mPart, new Object[] {true, this}, mListener);
        }
    }


    public void showFile() {
        Uri uri = AttachmentProvider.getAttachmentUriForViewing(mAccount, mPart.getAttachmentId());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // We explicitly set the ContentType in addition to the URI because some attachment viewers (such as Polaris office 3.0.x) choke on documents without a mime type
        intent.setDataAndType(uri, mContentType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        try {
            mContext.startActivity(intent);
        } catch (Exception e) {
            Log.e(MailChat.LOG_TAG, "Could not display attachment of type " + mContentType, e);
            Toast toast = Toast.makeText(mContext, mContext.getString(R.string.message_view_no_viewer, mContentType), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    /**
     * Check the {@link PackageManager} if the phone has an application
     * installed to view this type of attachment.
     * If not, {@link #viewButton} is disabled.
     * This should be done in any place where
     * attachment.viewButton.setEnabled(enabled); is called.
     * This method is safe to be called from the UI-thread.
     */
    public void checkViewable() {
        if (mStatus != Status.COMPLETE) {
            // nothing to do
            return;
        }
        try {
            Uri uri = AttachmentProvider.getAttachmentUriForViewing(mAccount, mPart.getAttachmentId());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            if (intent.resolveActivity(mContext.getPackageManager()) == null) {
                //viewButton.setEnabled(false);
            }
            // currently we do not cache re result.
        } catch (Exception e) {
            Log.e(MailChat.LOG_TAG, "Cannot resolve activity to determine if we shall show the 'view'-button for an attachment", e);
        }
    }

    public void attachmentSaved(final String filename) {
        Toast.makeText(mContext, String.format(
                           mContext.getString(R.string.message_view_status_attachment_saved), filename),
                       Toast.LENGTH_LONG).show();
    }

    public void attachmentNotSaved() {
        MailChat.toast(mContext.getString(R.string.message_view_status_attachment_not_saved));
    }
    public AttachmentFileDownloadCallback getCallback() {
        return callback;
    }
    public void setCallback(AttachmentFileDownloadCallback callback) {
        this.callback = callback;
    }
    
    public MessagingListener getListener() {
    	return mListener;
    }
    
    public void setListener(MessagingListener listener) {
    	mListener = listener;
    }
    
    // 下载进度通知栏提示
    public class DownloadAttachmentNotification {
        public AttachmentSquareView mSquareView = null;
        
    	private AttachmentView mView = null;
    	private NotificationManager mNotificationManager = null;
    	private NotificationCompat.Builder mBuilder = null;
    	private NotificationCompat.Builder mOngoingBuilder = null;
        public int mId = 0;
    	private int mMax = 0;
    	private int mProgress = -1;
    	
    	// 下载取消标记
    	public AtomicBoolean isCanceled;
    	
    	public DownloadAttachmentNotification(AttachmentView view) {
    		mView = view;
    		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	        mBuilder = new NotificationCompat.Builder(mContext);
	        mOngoingBuilder = new NotificationCompat.Builder(mContext);
	        mId = MailChat.DOWNLOAD_ATTACHMENT_NOTIFICATION - (int)mView.mPart.getAttachmentId();
	        mMax = (int) MimeUtility.getDecodeBase64Size(mView.mSize);
	        
	        // 初始化下载取消标记
	        isCanceled = new AtomicBoolean(false);
	        
	        mOngoingBuilder.setSmallIcon(R.drawable.ic_notify_check_mail);
	        mOngoingBuilder.setWhen(System.currentTimeMillis());
	        mOngoingBuilder.setOngoing(true);
	        
	        String prefix = mContext.getString(R.string.notification_attachment_downloading_prefix);
	        String surfix = mContext.getString(R.string.notification_attachment_downloading_surfix);
	        
	        String filename = mView.mName;
	        mOngoingBuilder.setTicker(prefix + filename + surfix);
	        Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(),
	        		R.drawable.icon);
	        mBuilder.setLargeIcon(icon);
	        mOngoingBuilder.setLargeIcon(icon);
	        mOngoingBuilder.setContentTitle(prefix + surfix);
	        mOngoingBuilder.setContentText(filename);
	        
	        // 生成下载进度提示通知
	        Intent intent = MailDetialActivity.actionAttachmentIntent(
					mContext, 
					mMessage.makeMessageReference(),
					mDownloadingId,
					mId,
					MailDetialActivity.EXTRA_ATT_OP_CANCEL);
			
			PendingIntent pendingIntent = PendingIntent.getActivity(
					mContext,
			        mId,
			        intent,
			        PendingIntent.FLAG_UPDATE_CURRENT
			);
			
			mOngoingBuilder.setContentIntent(pendingIntent);
			
			if (mAttachmentListener != null) {
				mAttachmentListener.loadAttachmentStarted();
			}
			
			MailChat.toast(prefix + surfix);
    	}
    	
    	// 转发未下载附件相关通知
    	public DownloadAttachmentNotification(AttachmentView view, int id) {
    		mView = view;
    		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	        mBuilder = new NotificationCompat.Builder(mContext);
	        mOngoingBuilder = new NotificationCompat.Builder(mContext);
	        mId = id;
	        mMax = (int) MimeUtility.getDecodeBase64Size(mView.mSize);
	        
	        isCanceled = new AtomicBoolean(false);
	        
	        mOngoingBuilder.setSmallIcon(R.drawable.ic_notify_check_mail);
	        mOngoingBuilder.setWhen(System.currentTimeMillis());
	        mOngoingBuilder.setOngoing(true);
	        
	        String prefix = mContext.getString(R.string.notification_forward_attachment_downloading_prefix);
	        String surfix = mContext.getString(R.string.notification_forward_attachment_downloading_surfix);
	        
	        String filename = mView.mName;
	        mOngoingBuilder.setTicker(prefix + filename + surfix);
	        Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(),
	        		R.drawable.icon);
	        mBuilder.setLargeIcon(icon);
	        mOngoingBuilder.setLargeIcon(icon);
	        mOngoingBuilder.setContentTitle(prefix + surfix);
	        mOngoingBuilder.setContentText(filename);
	        
	        if (mAttachmentListener != null) {
				mAttachmentListener.loadAttachmentStarted();
			}
	        
	        MailChat.toast(prefix + surfix);
    	}
    	
    	public void removeNotification() {
    		mNotificationManager.cancel(mId);
    	}
    	
    	public void showProgress(int progress) {
    	    if (mMax <= 0) {
    	        return;
    	    }

    		int percent = progress * 100 / mMax;
    		if (percent > mProgress) {
    			mProgress = percent;
	    		mOngoingBuilder.setProgress(100, percent, false);	        
		        mNotificationManager.notify(mId, mOngoingBuilder.build());
		        mView.loadAttachmentStarted();
		        mView.mProgressBar.setProgress(percent);
		        
		        if (mAttachmentListener != null) {
					mAttachmentListener.progress(percent);
				}
		        
		        if (mSquareView != null) {
		            if (mDownloadingId.equals(mSquareView.getDownloadingId())) {
		                mSquareView.setProgress(getProgress());
		            } else {
		                mSquareView = null;
		            }
		        }
    		}
    	}
    	
    	public float getProgress() {
    	    if (mProgress > 0) {
    	        return (float) mProgress / 100;
    	    } else {
    	        return 0;
    	    }
    	}
    	
    	public void finished() {
    		mProgress = -1;
    		
    		if (mAttachmentListener != null) {
				mAttachmentListener.loadAttachmentFinished();
			}
    		
    		if (mIsTemp) {
    			mNotificationManager.cancel(mId);
    			return;
    		}
    		
			String prefix = mContext
					.getString(R.string.notification_attachment_downloading_prefix);
			String surfix = mContext
					.getString(R.string.notification_attachment_downloading_finished);
			mBuilder.setTicker(prefix + mView.mName + surfix)
					.setSmallIcon(R.drawable.icon_send_mail_success)
					.setContentTitle(prefix.trim() + surfix)
					.setContentText(mView.mName);

			int defaults = 0;
			
			if (MailChat.isNotifyRingtone()) {
				// defaults = defaults | Notification.DEFAULT_SOUND;
				// 下载成功后的提示音沿用微妹的
				mBuilder.setSound(Uri.parse("android.resource://"
						+ mContext.getPackageName() + "/" + R.raw.att_compl));
			}
			if (MailChat.isNotifyVibrateOn()) {
				defaults = defaults | Notification.DEFAULT_VIBRATE;
			}
			mBuilder.setDefaults(defaults);

			// 下载成功通知
			Intent contentIntent = MailDetialActivity.actionAttachmentIntent(
					mContext,
					mMessage.makeMessageReference(),
					mDownloadingId,
					mId,
					MailDetialActivity.EXTRA_ATT_OP_OPEN);
			PendingIntent contentPendingIntent = PendingIntent.getActivity(
					mContext,
			        mId,
			        contentIntent,
			        PendingIntent.FLAG_ONE_SHOT
			);
			mBuilder.setContentIntent(contentPendingIntent);
			
			Intent deleteIntent = NotificationService.actionNotificationDeletedIntent(
					mDownloadingId);
			PendingIntent deletePendingIntent = PendingIntent.getService(
					mContext,
			        mId,
			        deleteIntent,
			        PendingIntent.FLAG_ONE_SHOT
			);
			mBuilder.setDeleteIntent(deletePendingIntent);
			
			mBuilder.setAutoCancel(true);
			mNotificationManager.notify(mId, mBuilder.build());

			MailChat.toast(prefix + surfix);
    	}
    	
    	public void failed(Throwable t) {
    		mProgress = -1;
    		
    		if (mAttachmentListener != null) {
				mAttachmentListener.loadAttachmentFailed();
			}
    		
    		if (mIsTemp) {
    			mNotificationManager.cancel(mId);
    			return;
    		}
    		
    		boolean isCanceledByUser;
    		if (GlobalConstants.CANCELED_BY_USER.equals(MailChat.getRootCauseMessage(t))) {
    		    isCanceledByUser = true;
    		} else {
    		    isCanceledByUser = false;
    		}
    		
			String prefix = mContext
					.getString(R.string.notification_attachment_downloading_prefix);
			String surfix = isCanceledByUser
			        ? mContext.getString(R.string.notification_attachment_downloading_canceled)
			        : mContext.getString(R.string.notification_attachment_downloading_failed);
			mBuilder.setTicker(prefix + mView.mName + surfix)
					.setSmallIcon(R.drawable.icon_net_info)
					.setContentTitle(prefix.trim() + surfix)
					.setContentText(mView.mName);

			int defaults = 0;
			if (MailChat.isNotifyRingtone()) {
				// defaults = defaults | Notification.DEFAULT_SOUND;
				// 下载失败后的提示音沿用微妹的
				mBuilder.setSound(Uri.parse("android.resource://"
						+ mContext.getPackageName() + "/" + R.raw.att_compl));
			}
			if (MailChat.isNotifyVibrateOn()) {
				defaults = defaults | Notification.DEFAULT_VIBRATE;
			}
			mBuilder.setDefaults(defaults);

			// 下载失败通知
			Intent contentIntent = MailDetialActivity.actionAttachmentIntent(
					mContext,
					mMessage.makeMessageReference(),
					mDownloadingId,
					mId,
					MailDetialActivity.EXTRA_ATT_OP_DOWNLOAD);
			PendingIntent contentPendingIntent = PendingIntent.getActivity(
					mContext,
			        mId,
			        contentIntent,
			        PendingIntent.FLAG_ONE_SHOT
			);
			mBuilder.setContentIntent(contentPendingIntent);
			
			Intent deleteIntent = NotificationService.actionNotificationDeletedIntent(
					mDownloadingId);
			PendingIntent deletePendingIntent = PendingIntent.getService(
					mContext,
			        mId,
			        deleteIntent,
			        PendingIntent.FLAG_ONE_SHOT
			);
			mBuilder.setDeleteIntent(deletePendingIntent);
			
			mBuilder.setAutoCancel(true);
			mNotificationManager.notify(mId, mBuilder.build());

			MailChat.toast(prefix + surfix);
    	}
    }
    
    /**
     * Load related attachment
     */
    public void loadAttachment() {
    	mDownloadingNotification = new DownloadAttachmentNotification(this);
    	if (mAttachmentSquare != null) {
    	    mDownloadingNotification.mSquareView = mAttachmentSquare;
    	    mAttachmentSquare = null;
    	}
    	
    	mController.loadAttachment(mAccount, mMessage, mPart, 
    			new Object[] { false, this}, 
    			mListener);
    }
    
    /**
     * Load related inline attachment
     */
    public void loadInlineAttachment() {
    	mController.loadAttachment(mAccount, mMessage, mPart, new Object[] { null, this }, mListener);
    }
    
    /**
     * Save attachment to downloaded attachments' directory and open it
     */
    public void openAttachment() {
		File file = saveAttachment();
		if (file == null || !file.exists()) {
			attachmentNotSaved();
			return;
		}
			
		Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), mContentType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        
        // 修复MIME类型信息错误导致的附件无法打开问题
        PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (activities.size() == 0) {
        	intent.setDataAndType(Uri.fromFile(file),
        			MimeUtility.getMimeTypeByExtension(file.getName()));
        }
        
        try {
            mContext.startActivity(intent);
        } catch (Exception e) {
            Log.e(MailChat.LOG_TAG, "Could not open attachment of type " + mContentType, e);
            Toast toast = Toast.makeText(mContext, mContext.getString(R.string.message_view_no_viewer, mContentType), Toast.LENGTH_LONG);
            toast.show();
        }
    }
    
    public File saveAttachment() {
    	File file = null;
    	
		try {
			file = TemporaryAttachmentStore.getFileForWriting(mContext, mName);
			Uri uri = AttachmentProvider.getAttachmentUri(mAccount,	mPart.getAttachmentId());
			InputStream in = mContext.getContentResolver().openInputStream(uri);
			OutputStream out = new FileOutputStream(file);
			IOUtils.copy(in, out);
			out.flush();
			out.close();
			in.close();
    	} catch (IOException e) {
    		if (MailChat.DEBUG) {
				Log.e(MailChat.LOG_TAG, "Error saving attachment", e);
			}
    	}
    	
    	return file;
    }
    
    private static class AttachmentViewHandler extends Handler {
        private AttachmentViewHandler() {
            super(MailChat.app.getMainLooper());
        }
    }
    
    AttachmentViewHandler mHandler = new AttachmentViewHandler();
        
    /**
     * Status: attachment downloading started
     */
    public void loadAttachmentStarted() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
            	mProgressBar.setMax(100);
            	mProgressBar.setVisibility(View.VISIBLE);
            	mMenuButton.setVisibility(View.GONE);
            	mCancelButton.setVisibility(View.VISIBLE);
            	mAttachmentHint.setVisibility(View.GONE);
                if (mAttachmentPreviewActivity != null) {
                    mAttachmentPreviewActivity.updateUi(AttachmentView.Status.DOWNLOADING);
                }
            }
        });
    }
    
    /**
     * Status: attachment downloading finished
     */
    public void loadAttachmentFinished() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(View.INVISIBLE);
                mMenuButton.setVisibility(View.VISIBLE);
                mCancelButton.setVisibility(View.GONE);
                mAttachmentHint.setVisibility(View.VISIBLE);
                if (mAttachmentPreviewActivity != null) {
                    mAttachmentPreviewActivity.updateUi(AttachmentView.Status.COMPLETE);
                }
            }
        });
    }
    
    /**
     * Status: attachment downloading failed
     */
    public void loadAttachmentFailed() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
            	mProgressBar.setVisibility(View.INVISIBLE);
            	mMenuButton.setVisibility(View.VISIBLE);
            	mCancelButton.setVisibility(View.GONE);
            	mAttachmentHint.setVisibility(View.GONE);
                if (mAttachmentPreviewActivity != null) {
                    mAttachmentPreviewActivity.updateUi(AttachmentView.Status.METADATA);
                }
            }
        });
    }

    private void showPopupMenu(final boolean isSupportPreview) {
        String[] menuItem;
        if (mStatus == Status.COMPLETE) {
            if (isSupportPreview) {
                menuItem = new String[] {
                        getContext().getString(R.string.attachment_menu_preview),
                        getContext().getString(R.string.attachment_menu_open),
                        getContext().getString(R.string.attachment_menu_forward),
                        getContext().getString(R.string.attachment_menu_save_as)};
            } else {
                menuItem = new String[] {
                        getContext().getString(R.string.attachment_menu_open),
                        getContext().getString(R.string.attachment_menu_forward),
                        getContext().getString(R.string.attachment_menu_save_as)};
            }
		} else {
		    if (isSupportPreview) {
		        menuItem = new String[] {
		                getContext().getString(R.string.attachment_menu_preview),
		                getContext().getString(R.string.attachment_menu_download),
		                getContext().getString(R.string.attachment_menu_forward)};
		    } else {
		        menuItem = new String[] {
		                getContext().getString(R.string.attachment_menu_download),
		                getContext().getString(R.string.attachment_menu_forward)};
		    }
		}
		LinearLayout popupMenuLayout = (LinearLayout) LayoutInflater
				.from(getContext())
				.inflate(R.layout.message_view_attachment_popup_menu, null);
		ListView popupMenuList = (ListView) popupMenuLayout.findViewById(R.id.lv_popup_menu);
		popupMenuList.setAdapter(new ArrayAdapter<String>(getContext(),
				R.layout.message_view_attachment_popup_menu_item,
				R.id.tv_text,
				menuItem));
		
		mPopupMenuWindow = new PopupWindow(getContext());
		mPopupMenuWindow.setBackgroundDrawable(new BitmapDrawable());
		
		WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
		float density = getContext().getResources().getDisplayMetrics().density;
		
		switch (menuItem.length) {
		case 2:
		    wmParams.height = (int) (96 * density);
		    break;
		case 3:
		    wmParams.height = (int) (138 * density);
		    break;
		case 4:
		    wmParams.height = (int) (180 * density);
		    break;
		}
		wmParams.width = (int) (120 * density);
		
		mPopupMenuWindow.setHeight(wmParams.height);
		mPopupMenuWindow.setWidth(wmParams.width);
		mPopupMenuWindow.setFocusable(true);
		mPopupMenuWindow.setContentView(popupMenuLayout);
		
		int[] pos = new int[2];
		mMenuButton.getLocationOnScreen(pos);
		int yOffset = pos[1] - wmParams.height - (int)(5 * density);
		
		mPopupMenuWindow.showAtLocation(this, Gravity.RIGHT | Gravity.TOP, (int)(20 * density), yOffset);
		popupMenuList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			    int index = arg2;
			    if (!isSupportPreview) {
			        index++;
			    }

				switch (index) {
				case 0:
				    preview();
				    break;
				case 1:
					open();
					break;
				case 2:
				    if (mPart.getBody() == null
				            && mStatus == AttachmentView.Status.METADATA
				            && MimeUtility.isLocalUid(mMessage.getUid())) {
				        Toast toast = Toast.makeText(mContext,
				                R.string.cannot_forward_attachment_from_unsynced_message,
				                Toast.LENGTH_LONG);
				        toast.show();
				    } else {
				        String[] attStoreData = null;
				        try {
				            attStoreData = mPart.getHeader(MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA);
				        } catch (MessagingException e) {
				            // DO NOTHING
				        }

				        ForwardActivity.actionForwardSingleAttachment(getContext(),
				                new SingleAttachment(mDownloadingId,
				                        mMessage.makeMessageReference().toIdentityString(),
				                        Utility.combine(attStoreData, ','),
				                        null),
				                mAccount);
				    }
				    break;
				case 3:
				    callback.showFileBrowser(AttachmentView.this);
				    break;
				}

				mPopupMenuWindow.dismiss();
				mPopupMenuWindow = null;
			}
		});
	}
    
    public class AttachmentListener {
    	public void loadAttachmentStarted() {}
    	public void loadAttachmentFinished() {}
    	public void loadAttachmentFailed() {}
    	public void progress(int progress) {}
    }
    
    private AttachmentListener mAttachmentListener = null;
    
    public void setAttachmentListener(AttachmentListener attachmentListener) {
    	mAttachmentListener = attachmentListener;
    }
}
