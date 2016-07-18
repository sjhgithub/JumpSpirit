package cn.mailchat.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.MessageReference;
import cn.mailchat.cache.TemporaryAttachmentStore;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.helper.Utility;
import cn.mailchat.mail.FetchProfile;
import cn.mailchat.mail.Message;
import cn.mailchat.mail.Part;
import cn.mailchat.mail.internet.MimeHeader;
import cn.mailchat.mail.internet.MimeUtility;
import cn.mailchat.mail.store.LocalStore;
import cn.mailchat.mail.store.LocalStore.LocalAttachmentBodyPart;
import cn.mailchat.mail.store.LocalStore.LocalFolder;
import cn.mailchat.mail.store.LocalStore.LocalMessage;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AttachmentSquareView extends LinearLayout {
    
    private TextView mType;
    private TextView mStatus;
    private TextView mTitle;
    
    private MessageReference mMessageReference;
    private String mStore;
    private String mUri;
    private String mDownloadingId;
    
    private float mProgress;
    
    private Paint mPiePaint;
    private RectF mPieRect;

    private LayoutInflater mInflater;

    public AttachmentSquareView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mPiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPiePaint.setStyle(Paint.Style.FILL);
        mPiePaint.setAlpha(35);
        
        setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                synchronized(MailChat.downloadingList) {
                    if (MailChat.downloadingList.containsKey(mDownloadingId)) {
                        AttachmentView av = MailChat.downloadingList.get(mDownloadingId);
                        switch (av.mStatus) {
                        case COMPLETE:
                            av.open();
                            break;
                        case DOWNLOADING:
                            AttachmentSquareView.this.setProgress(-1);
                            av.cancel();
                            break;
                        case METADATA:
                            av.mAttachmentSquare = AttachmentSquareView.this;
                            av.click();
                            break;
                        }
                    } else {
                        Account account = Preferences.getPreferences(MailChat.app)
                                .getAccount(mMessageReference.accountUuid);
                        try {
                            LocalStore localStore = account.getLocalStore();
                            LocalFolder localFolder = localStore.getFolder(mMessageReference.folderName);
                            
                            LocalMessage localMessage = localFolder.getMessage(mMessageReference.uid);
                            FetchProfile fetchProfile = new FetchProfile();
                            fetchProfile.add(FetchProfile.Item.BODY);
                            localFolder.fetch(new Message[]{localMessage}, fetchProfile, null);
                            
                            List<Part> atts = MimeUtility.collectAttachments(localMessage);
                            LocalAttachmentBodyPart localAtt = null;
                            for (Part att : atts) {
                                String attStore = Utility.combine(att.getHeader(MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA), '.');
                                if (mStore.equals(attStore)) {
                                    localAtt = (LocalAttachmentBodyPart) att;
                                    break;
                                }
                            }
                            
                            MessagingController controller = MessagingController.getInstance(MailChat.app);
                            final AttachmentView attView = (AttachmentView) mInflater.inflate(R.layout.message_view_attachment, null);
                            attView.populateFromPart(localAtt, localMessage, account, controller, new MessagingListener() {

                                @Override
                                public void loadAttachmentStarted(
                                        Account account, Message message,
                                        Part part, Object tag,
                                        boolean requiresDownload) {
                                    attView.loadAttachmentStarted();
                                }

                                @Override
                                public void loadAttachmentFinished(
                                        Account account, Message message,
                                        Part part, Object tag) {
                                    attView.mDownloadingNotification.finished();
                                    attView.loadAttachmentFinished();
                                    attView.mStatus = AttachmentView.Status.COMPLETE;
                                }

                                @Override
                                public void loadAttachmentFailed(
                                        Account account, Message message,
                                        Part part, Object tag, Throwable t) {
                                    attView.mDownloadingNotification.failed(t);
                                    attView.loadAttachmentFailed();
                                    attView.mStatus = AttachmentView.Status.METADATA;
                                    AttachmentSquareView.this.setProgress(-1);
                                }
                                
                            });
                            
                            attView.mAttachmentSquare = AttachmentSquareView.this;
                            attView.click();
                        } catch (Exception e) {
                            if (mUri != null) {
                                openAttachment(Uri.parse(mUri));
                            } else {
                                MailChat.toast("!");
                            }
                        }
                    }
                }
            }
            
        });
    }
    
    public void setProgress(float progress) {
        mProgress = progress;
        postInvalidate();
    }

    public void setType(String type) {
        int color = 0;
        if (type != null && type.length() > 0) {
            color = type.charAt(0) % 26;
        }
        
        switch (color) {
        case 0:
            setBackgroundResource(R.color.att_square_color_01);
            break;
        case 1:
            setBackgroundResource(R.color.att_square_color_02);
            break;
        case 2:
            setBackgroundResource(R.color.att_square_color_03);
            break;
        case 3:
            setBackgroundResource(R.color.att_square_color_04);
            break;
        case 4:
            setBackgroundResource(R.color.att_square_color_05);
            break;
        case 5:
            setBackgroundResource(R.color.att_square_color_06);
            break;
        case 6:
            setBackgroundResource(R.color.att_square_color_07);
            break;
        case 7:
            setBackgroundResource(R.color.att_square_color_08);
            break;
        case 8:
            setBackgroundResource(R.color.att_square_color_09);
            break;
        case 9:
            setBackgroundResource(R.color.att_square_color_10);
            break;
        case 10:
            setBackgroundResource(R.color.att_square_color_11);
            break;
        case 11:
            setBackgroundResource(R.color.att_square_color_12);
            break;
        case 12:
            setBackgroundResource(R.color.att_square_color_13);
            break;
        case 13:
            setBackgroundResource(R.color.att_square_color_14);
            break;
        case 14:
            setBackgroundResource(R.color.att_square_color_15);
            break;
        case 15:
            setBackgroundResource(R.color.att_square_color_16);
            break;
        case 16:
            setBackgroundResource(R.color.att_square_color_17);
            break;
        case 17:
            setBackgroundResource(R.color.att_square_color_18);
            break;
        case 18:
            setBackgroundResource(R.color.att_square_color_19);
            break;
        case 19:
            setBackgroundResource(R.color.att_square_color_20);
            break;
        case 20:
            setBackgroundResource(R.color.att_square_color_21);
            break;
        case 21:
            setBackgroundResource(R.color.att_square_color_22);
            break;
        case 22:
            setBackgroundResource(R.color.att_square_color_23);
            break;
        case 23:
            setBackgroundResource(R.color.att_square_color_24);
            break;
        case 24:
            setBackgroundResource(R.color.att_square_color_25);
            break;
        case 25:
            setBackgroundResource(R.color.att_square_color_26);
            break;
        }
        
        mType.setText(type);
    }

    public void setStatus(String status) {
        mStatus.setText(status);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public void setMessageReference(MessageReference messageReference) {
        mMessageReference = messageReference;
    }

    public void setStore(String store) {
        mStore = store;
    }

    public void setUri(String uri) {
        mUri = uri;
    }

    public String getDownloadingId() {
        return mDownloadingId;
    }

    public void setDownloadingId(String downloadingId) {
        mDownloadingId = downloadingId;
    }

    public void setInflater(LayoutInflater inflater) {
        mInflater = inflater;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mType = (TextView)findViewById(R.id.att_square_type);
        mStatus = (TextView)findViewById(R.id.att_square_status);
        mTitle = (TextView)findViewById(R.id.att_square_title);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        float width = w - getPaddingLeft() - getPaddingRight();
        float height = h - getPaddingTop() - getPaddingBottom();
        
        float radiusX = (float)Math.sqrt(2) / 2 * width;
        float radiusY = (float)Math.sqrt(2) / 2 * height;
        
        float left = getPaddingLeft() + width / 2 - radiusX;
        float top = getPaddingTop() + height / 2 - radiusY;
        float right = left + radiusX * 2;
        float bottom = top + radiusY * 2;

        mPieRect = new RectF(left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (mProgress > 0 && mProgress < 1) {
            float angle = 360 * mProgress;
            canvas.drawArc(mPieRect, -90 + angle, 360 - angle, true, mPiePaint);
        }
    }

    public void openAttachment(Uri uri) {
        File file = saveAttachment(uri);
        if (file == null || !file.exists()) {
            MailChat.toast(MailChat.app.getString(R.string.message_view_status_attachment_not_saved));
            return;
        }

        String contentType = MimeUtility.getMimeTypeByExtension(mTitle.getText().toString());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), contentType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

        // 修复MIME类型信息错误导致的附件无法打开问题
        PackageManager packageManager = MailChat.app.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (activities.size() == 0) {
            intent.setDataAndType(Uri.fromFile(file),
                    MimeUtility.getMimeTypeByExtension(file.getName()));
        }

        try {
            MailChat.app.startActivity(intent);
        } catch (Exception e) {
            Log.e(MailChat.LOG_TAG, "Could not open attachment of type " + contentType, e);
            Toast toast = Toast.makeText(MailChat.app, MailChat.app.getString(R.string.message_view_no_viewer, contentType), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public File saveAttachment(Uri uri) {
        File file = null;

        try {
            file = TemporaryAttachmentStore.getFileForWriting(MailChat.app, mTitle.toString());
            InputStream in = MailChat.app.getContentResolver().openInputStream(uri);
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
}