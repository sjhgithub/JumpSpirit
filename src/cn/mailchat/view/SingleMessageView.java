package cn.mailchat.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewParent;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.crypto.CryptoProvider;
import cn.mailchat.crypto.PgpData;
import cn.mailchat.fragment.MessageViewFragment;
import cn.mailchat.helper.ClipboardManager;
import cn.mailchat.helper.Contacts;
import cn.mailchat.helper.HtmlConverter;
import cn.mailchat.helper.Utility;
import cn.mailchat.mail.Address;
import cn.mailchat.mail.Flag;
import cn.mailchat.mail.Message;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.mail.Multipart;
import cn.mailchat.mail.Part;
import cn.mailchat.mail.internet.MimeUtility;
import cn.mailchat.mail.store.LocalStore;
import cn.mailchat.mail.store.LocalStore.LocalMessage;
import cn.mailchat.provider.AttachmentProvider.AttachmentProviderColumns;
import cn.mailchat.utils.GlobalTools;
import cn.mailchat.utils.SystemUtil;
import cn.mailchat.view.MessageWebView.DisplayFinish;
import cn.mailchat.view.NonLockingScrollView.ScrollViewListener;


public class SingleMessageView extends LinearLayout implements OnClickListener,
        MessageHeader.OnLayoutChangedListener, OnCreateContextMenuListener {
    private static final int MENU_ITEM_LINK_VIEW = Menu.FIRST;
    private static final int MENU_ITEM_LINK_SHARE = Menu.FIRST + 1;
    private static final int MENU_ITEM_LINK_COPY = Menu.FIRST + 2;

    private static final int MENU_ITEM_IMAGE_VIEW = Menu.FIRST;
    private static final int MENU_ITEM_IMAGE_SAVE = Menu.FIRST + 1;
    private static final int MENU_ITEM_IMAGE_COPY = Menu.FIRST + 2;

    private static final int MENU_ITEM_PHONE_CALL = Menu.FIRST;
    private static final int MENU_ITEM_PHONE_SAVE = Menu.FIRST + 1;
    private static final int MENU_ITEM_PHONE_COPY = Menu.FIRST + 2;

    private static final int MENU_ITEM_EMAIL_SEND = Menu.FIRST;
    private static final int MENU_ITEM_EMAIL_SAVE = Menu.FIRST + 1;
    private static final int MENU_ITEM_EMAIL_COPY = Menu.FIRST + 2;

    private static final String[] ATTACHMENT_PROJECTION = new String[] {
        AttachmentProviderColumns._ID,
        AttachmentProviderColumns.DISPLAY_NAME
    };
    private static final int DISPLAY_NAME_INDEX = 1;


    private MessageCryptoView mCryptoView;
    private MessageOpenPgpView mOpenPgpView;
    private MessageWebView mMessageContentView;
    private MessageHeader mHeaderContainer;
    
	// 正在加载提示和重新加载提示
    // Modified by LL
    // BEGIN
    private LinearLayout mLoadingStatus;
    private RelativeLayout mReload;
    private TextView mReloadTitle;
    private TextView mReloadSubtitle;
    // END
    
    private LinearLayout mAttachments;
    private Button mShowHiddenAttachments;
    private LinearLayout mHiddenAttachments;
    private View mShowPicturesAction;
    private View mShowMessageAction;
    private View mShowAttachmentsAction;
    private boolean mShowPictures;
    private boolean mHasAttachments;
    private Button mDownloadRemainder;
    private LayoutInflater mInflater;
    private Contacts mContacts;
    private AttachmentView.AttachmentFileDownloadCallback attachmentCallback;
    private View mAttachmentsContainer;
    private SavedState mSavedState;
    private ClipboardManager mClipboardManager;
    private String mText;
    private TextView attachmentCountText;
    
    /*
    // Distinct normal and inline attachments
    // Modified by LL
    private int attachmentCount = 0;//记录附件数
    private int inlineAttachmentCount = 0;
    */
    private int attachmentCount = 0;//记录附件数
    private LinearLayout attachmentBuoyView;
    private TextView attachmentCountBuoyText;
    private NonLockingScrollView nonLockingScrollView;
    private int screenHeigh;
    private int actionBarheight;
    private int buttonHeight;
    private int statusBarHeight;
    private boolean isFirstShow =true;;
	public void initialize(Fragment fragment) {
        Activity activity = fragment.getActivity();
        mMessageContentView = (MessageWebView) findViewById(R.id.message_content);
        mMessageContentView.configure();
        activity.registerForContextMenu(mMessageContentView);
        mMessageContentView.setOnCreateContextMenuListener(this);
        mHeaderContainer = (MessageHeader) findViewById(R.id.header_container);
        mHeaderContainer.setOnLayoutChangedListener(this);
        
        // 正在加载提示和重新加载提示
        // Modified by LL
        // BEGIN
        mLoadingStatus = (LinearLayout) findViewById(R.id.message_view_loading_status);
        mLoadingStatus.setVisibility(View.GONE);
        
        mReload = (RelativeLayout) findViewById(R.id.message_view_reload);
        mReloadTitle = (TextView) findViewById(R.id.message_view_reload_title);
        mReloadSubtitle = (TextView) findViewById(R.id.message_view_reload_subtitle);
        mReload.setVisibility(View.GONE);
        mReload.findViewById(R.id.message_view_reload_button)
        	.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mController.loadMessageForView(mAccount, 
						mMessage.getFolder().getName(), 
						mMessage.getUid(), 
						mListener);
				mReload.setVisibility(View.GONE);
			}
        	
        });
        // END
        
        mAttachmentsContainer = findViewById(R.id.attachments_container);
        mAttachments = (LinearLayout) findViewById(R.id.attachments);
        mHiddenAttachments = (LinearLayout) findViewById(R.id.hidden_attachments);
        mHiddenAttachments.setVisibility(View.GONE);
        mShowHiddenAttachments = (Button) findViewById(R.id.show_hidden_attachments);
        mShowHiddenAttachments.setVisibility(View.GONE);
        mCryptoView = (MessageCryptoView) findViewById(R.id.layout_decrypt);
        mCryptoView.setFragment(fragment);
        mCryptoView.setupChildViews();
        mOpenPgpView = (MessageOpenPgpView) findViewById(R.id.layout_decrypt_openpgp);
        mOpenPgpView.setFragment(fragment);
        mOpenPgpView.setupChildViews();
        mShowPicturesAction = findViewById(R.id.show_pictures);
        mShowMessageAction = findViewById(R.id.show_message);
        
        mShowAttachmentsAction = findViewById(R.id.show_attachments);
        
        attachmentCountText = (TextView) findViewById(R.id.attachment_count);
        
        /*
        // Add onClickListener to show hidden inline attachments
        // Modified by LL
        attachmentCountText.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				attachmentCountText.setEnabled(false);
				attachmentCountText.setTextColor(Color.GRAY);
				mHiddenAttachments.setVisibility(View.VISIBLE);
				attachmentCountBuoyText.setText(Integer.toString(attachmentCount + inlineAttachmentCount));
			}
		});
		*/
        
        
        attachmentBuoyView =(LinearLayout) findViewById(R.id.layout_msgview_attachment_sign);
        attachmentCountBuoyText= (TextView) findViewById(R.id.tv_attachment_count);
        mShowPictures = false;

        mContacts = Contacts.getInstance(activity);

        mInflater = ((MessageViewFragment) fragment).getFragmentLayoutInflater();
        mDownloadRemainder = (Button) findViewById(R.id.download_remainder);
        mDownloadRemainder.setVisibility(View.GONE);
        mAttachmentsContainer.setVisibility(View.GONE);
        mMessageContentView.setVisibility(View.VISIBLE);

        // the HTC version of WebView tries to force the background of the
        // titlebar, which is really unfair.
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.messageViewHeaderBackgroundColor, outValue, true);
        mHeaderContainer.setBackgroundColor(outValue.data);
        // also set background of the whole view (including the attachments view)
        setBackgroundColor(outValue.data);

        mShowHiddenAttachments.setOnClickListener(this);
        mShowMessageAction.setOnClickListener(this);
        mShowAttachmentsAction.setOnClickListener(this);
        mShowPicturesAction.setOnClickListener(this);
        attachmentBuoyView.setOnClickListener(this);
        mClipboardManager = ClipboardManager.getInstance(activity);
        nonLockingScrollView =(NonLockingScrollView) findViewById(R.id.nonLockingScrollView);
        //计算附件区域在屏幕内所需要数据
        Display  display  =activity.getWindowManager().getDefaultDisplay();
        screenHeigh=display.getHeight();
        actionBarheight=  SystemUtil.getActionBarHeight(activity);
        buttonHeight = GlobalTools.dip2px(activity, 48.5f);
        statusBarHeight= SystemUtil.getStatusBarHeight(activity);
        //webView界面加载完成监听
        mMessageContentView.setDf(new DisplayFinish() {
			@Override
			public void After() {
				// TODO Auto-generated method stub
				
				//if(isFirstShow){
				// 修复附件数为零时也显示附件浮标问题
				// Modified by LL
				if(isFirstShow && attachmentCount > 0){
				
				    if(mAttachmentsContainer.getY()<(screenHeigh-actionBarheight-buttonHeight-statusBarHeight-attachmentCountText.getHeight())){
				    	attachmentBuoyView.setVisibility(View.GONE);
				    }else{
				    	attachmentBuoyView.setVisibility(View.VISIBLE);
				    }
				}
			}
		});
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu);

        WebView webview = (WebView) v;
        WebView.HitTestResult result = webview.getHitTestResult();

        if (result == null) {
            return;
        }

        int type = result.getType();
        Context context = getContext();

        switch (type) {
            case HitTestResult.SRC_ANCHOR_TYPE: {
                final String url = result.getExtra();
                OnMenuItemClickListener listener = new OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case MENU_ITEM_LINK_VIEW: {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivityIfAvailable(getContext(), intent);
                                break;
                            }
                            case MENU_ITEM_LINK_SHARE: {
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_TEXT, url);
                                startActivityIfAvailable(getContext(), intent);
                                break;
                            }
                            case MENU_ITEM_LINK_COPY: {
                                String label = getContext().getString(
                                        R.string.webview_contextmenu_link_clipboard_label);
                                mClipboardManager.setText(label, url);
                                break;
                            }
                        }
                        return true;
                    }
                };

                menu.setHeaderTitle(url);

                menu.add(Menu.NONE, MENU_ITEM_LINK_VIEW, 0,
                        context.getString(R.string.webview_contextmenu_link_view_action))
                        .setOnMenuItemClickListener(listener);

                menu.add(Menu.NONE, MENU_ITEM_LINK_SHARE, 1,
                        context.getString(R.string.webview_contextmenu_link_share_action))
                        .setOnMenuItemClickListener(listener);

                menu.add(Menu.NONE, MENU_ITEM_LINK_COPY, 2,
                        context.getString(R.string.webview_contextmenu_link_copy_action))
                        .setOnMenuItemClickListener(listener);

                break;
            }
            case HitTestResult.IMAGE_TYPE:
            case HitTestResult.SRC_IMAGE_ANCHOR_TYPE: {
                final String url = result.getExtra();
                final boolean externalImage = url.startsWith("http");
                OnMenuItemClickListener listener = new OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case MENU_ITEM_IMAGE_VIEW: {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                if (!externalImage) {
                                    // Grant read permission if this points to our
                                    // AttachmentProvider
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                }
                                startActivityIfAvailable(getContext(), intent);
                                break;
                            }
                            case MENU_ITEM_IMAGE_SAVE: {
                                new DownloadImageTask().execute(url);
                                break;
                            }
                            case MENU_ITEM_IMAGE_COPY: {
                                String label = getContext().getString(
                                        R.string.webview_contextmenu_image_clipboard_label);
                                mClipboardManager.setText(label, url);
                                break;
                            }
                        }
                        return true;
                    }
                };

                menu.setHeaderTitle((externalImage) ?
                        url : context.getString(R.string.webview_contextmenu_image_title));

                menu.add(Menu.NONE, MENU_ITEM_IMAGE_VIEW, 0,
                        context.getString(R.string.webview_contextmenu_image_view_action))
                        .setOnMenuItemClickListener(listener);

                menu.add(Menu.NONE, MENU_ITEM_IMAGE_SAVE, 1,
                        (externalImage) ?
                            context.getString(R.string.webview_contextmenu_image_download_action) :
                            context.getString(R.string.webview_contextmenu_image_save_action))
                        .setOnMenuItemClickListener(listener);

                if (externalImage) {
                    menu.add(Menu.NONE, MENU_ITEM_IMAGE_COPY, 2,
                            context.getString(R.string.webview_contextmenu_image_copy_action))
                            .setOnMenuItemClickListener(listener);
                }

                break;
            }
            case HitTestResult.PHONE_TYPE: {
                final String phoneNumber = result.getExtra();
                OnMenuItemClickListener listener = new OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case MENU_ITEM_PHONE_CALL: {
                                Uri uri = Uri.parse(WebView.SCHEME_TEL + phoneNumber);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivityIfAvailable(getContext(), intent);
                                break;
                            }
                            case MENU_ITEM_PHONE_SAVE: {
                                Contacts contacts = Contacts.getInstance(getContext());
                                contacts.addPhoneContact(phoneNumber);
                                break;
                            }
                            case MENU_ITEM_PHONE_COPY: {
                                String label = getContext().getString(
                                        R.string.webview_contextmenu_phone_clipboard_label);
                                mClipboardManager.setText(label, phoneNumber);
                                break;
                            }
                        }

                        return true;
                    }
                };

                menu.setHeaderTitle(phoneNumber);

                menu.add(Menu.NONE, MENU_ITEM_PHONE_CALL, 0,
                        context.getString(R.string.webview_contextmenu_phone_call_action))
                        .setOnMenuItemClickListener(listener);

                menu.add(Menu.NONE, MENU_ITEM_PHONE_SAVE, 1,
                        context.getString(R.string.webview_contextmenu_phone_save_action))
                        .setOnMenuItemClickListener(listener);

                menu.add(Menu.NONE, MENU_ITEM_PHONE_COPY, 2,
                        context.getString(R.string.webview_contextmenu_phone_copy_action))
                        .setOnMenuItemClickListener(listener);

                break;
            }
            case WebView.HitTestResult.EMAIL_TYPE: {
                final String email = result.getExtra();
                OnMenuItemClickListener listener = new OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case MENU_ITEM_EMAIL_SEND: {
                                Uri uri = Uri.parse(WebView.SCHEME_MAILTO + email);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivityIfAvailable(getContext(), intent);
                                break;
                            }
                            case MENU_ITEM_EMAIL_SAVE: {
                                Contacts contacts = Contacts.getInstance(getContext());
                                contacts.createContact(new Address(email));
                                break;
                            }
                            case MENU_ITEM_EMAIL_COPY: {
                                String label = getContext().getString(
                                        R.string.webview_contextmenu_email_clipboard_label);
                                mClipboardManager.setText(label, email);
                                break;
                            }
                        }

                        return true;
                    }
                };

                menu.setHeaderTitle(email);

                menu.add(Menu.NONE, MENU_ITEM_EMAIL_SEND, 0,
                        context.getString(R.string.webview_contextmenu_email_send_action))
                        .setOnMenuItemClickListener(listener);

                menu.add(Menu.NONE, MENU_ITEM_EMAIL_SAVE, 1,
                        context.getString(R.string.webview_contextmenu_email_save_action))
                        .setOnMenuItemClickListener(listener);

                menu.add(Menu.NONE, MENU_ITEM_EMAIL_COPY, 2,
                        context.getString(R.string.webview_contextmenu_email_copy_action))
                        .setOnMenuItemClickListener(listener);

                break;
            }
        }
    }

    private void startActivityIfAvailable(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.error_activity_not_found, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.show_hidden_attachments: {
               // onShowHiddenAttachments();
                break;
            }
            case R.id.show_message: {
               // onShowMessage();
                break;
            }
            case R.id.show_attachments: {
               // onShowAttachments();
                break;
            }
            case R.id.show_pictures: {
//                // Allow network access first...
//                setLoadPictures(true);
//                // ...then re-populate the WebView with the message text
//                loadBodyFromText(mText);
                break;
            }
            case R.id. layout_msgview_attachment_sign:{
            	nonLockingScrollView.scrollTo(0, (int)mAttachmentsContainer.getY());
            	attachmentBuoyView.setVisibility(View.GONE);
            	break;
            }
        }
    }

    private void onShowHiddenAttachments() {
        mShowHiddenAttachments.setVisibility(View.GONE);
        mHiddenAttachments.setVisibility(View.VISIBLE);
    }

    public void onShowMessage() {
        showShowMessageAction(false);
        showAttachments(false);
        showShowAttachmentsAction(mHasAttachments);
        showMessageWebView(true);
    }

    public void onShowAttachments() {
        showMessageWebView(false);
        showShowAttachmentsAction(false);
        showShowMessageAction(true);
        showAttachments(true);
    }
    /**
     * 显示全部
     * 
     * @Description:
     * @author:shengli
     * @see:
     * @since:
     * @copyright © 35.com
     * @Date:2014-9-17
     */
    public void onShowAll(){
    	 showMessageWebView(true);
    	 showAttachments(mHasAttachments);	 
         mMessageContentView.blockNetworkData(false);
         setShowPictures(true);

    	 showShowMessageAction(false);
    	 showShowAttachmentsAction(false);
         showShowPicturesAction(false);
         
         // Hide inline attachments
         // Modified by LL
         // BEGIN
         //onShowHiddenAttachments();         
         // END
    }
    
    public SingleMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public boolean showPictures() {
        return mShowPictures;
    }

    public void setShowPictures(Boolean show) {
        mShowPictures = show;
    }

    /**
     * Enable/disable image loading of the WebView. But always hide the
     * "Show pictures" button!
     *
     * @param enable true, if (network) images should be loaded.
     *               false, otherwise.
     */
    public void setLoadPictures(boolean enable) {
        mMessageContentView.blockNetworkData(!enable);
        setShowPictures(enable);
        showShowPicturesAction(false);
    }

    public Button downloadRemainderButton() {
        return  mDownloadRemainder;
    }

    public void showShowPicturesAction(boolean show) {
        mShowPicturesAction.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    public void showShowMessageAction(boolean show) {
        mShowMessageAction.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    public void showShowAttachmentsAction(boolean show) {
        mShowAttachmentsAction.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Fetch the message header view.  This is not the same as the message headers; this is the View shown at the top
     * of messages.
     * @return MessageHeader View.
     */
    public MessageHeader getMessageHeaderView() {
        return mHeaderContainer;
    }

    public void setHeaders(final Message message, Account account) {
        try {
            mHeaderContainer.populate(message, account);
            mHeaderContainer.setVisibility(View.VISIBLE);


        } catch (Exception me) {
            Log.e(MailChat.LOG_TAG, "setHeaders - error", me);
        }
    }

    public void setShowDownloadButton(Message message) {
        if (message.isSet(Flag.X_DOWNLOADED_FULL)) {
            mDownloadRemainder.setVisibility(View.GONE);
        } else {
            mDownloadRemainder.setEnabled(true);
            mDownloadRemainder.setVisibility(View.VISIBLE);
        }
    }

    public void setOnFlagListener(OnClickListener listener) {
        mHeaderContainer.setOnFlagListener(listener);
    }

    public void showAllHeaders() {
        mHeaderContainer.onShowAdditionalHeaders();
    }

    public boolean additionalHeadersVisible() {
        return mHeaderContainer.additionalHeadersVisible();
    }
    
    public void setMessage(Account account, LocalMessage message, PgpData pgpData,
            MessagingController controller, MessagingListener listener) throws MessagingException {
        resetView();

        String text = null;
        if (pgpData != null) {
            text = pgpData.getDecryptedData();
            if (text != null) {
                text = HtmlConverter.textToHtml(text);
            }
        }

        if (text == null) {
            text = message.getTextForDisplay();
        }

        // Save the text so we can reset the WebView when the user clicks the "Show pictures" button
        mText = text;

        mHasAttachments = message.hasAttachments();

        if (mHasAttachments) {
            renderAttachments(message, 0, message, account, controller, listener);
        }

        mHiddenAttachments.setVisibility(View.GONE);

        boolean lookForImages = true;
        if (mSavedState != null) {
            if (mSavedState.showPictures) {
                setLoadPictures(true);
                lookForImages = false;
            }

            if (mSavedState.attachmentViewVisible) {
                onShowAttachments();
            } else {
                onShowMessage();
            }

            if (mSavedState.hiddenAttachmentsVisible) {
                onShowHiddenAttachments();
            }

            mSavedState = null;
        } else {
            onShowMessage();
        }

        if (text != null && lookForImages) {
            // If the message contains external pictures and the "Show pictures"
            // button wasn't already pressed, see if the user's preferences has us
            // showing them anyway.
            if (Utility.hasExternalImages(text) && !showPictures()) {
                Address[] from = message.getFrom();
                if ((account.getShowPictures() == Account.ShowPictures.ALWAYS) ||
                        ((account.getShowPictures() == Account.ShowPictures.ONLY_FROM_CONTACTS) &&
                         // Make sure we have at least one from address
                         (from != null && from.length > 0) &&
                         mContacts.isInContacts(from[0].getAddress()))) {
                    setLoadPictures(true);
                } else {
                    showShowPicturesAction(true);
                }
            }
        }

        if (text != null) {
            loadBodyFromText(text);
            updateCryptoLayout(account.getCryptoProvider(), pgpData, message);
            mOpenPgpView.updateLayout(account, pgpData.getDecryptedData(),
                    pgpData.getSignatureResult(), message);
        } else {
            showStatusMessage(getContext().getString(R.string.webview_empty_message));
        }
    }
    /**
     * 设置邮件显示全部(完整邮件，图片，附件)
     * 
     * @Description:
     * @author:shengli
     * @see:
     * @since:
     * @copyright © 35.com
     * @Date:2014-9-17
     */
    public void setShowAllMessage(Account account, LocalMessage message, PgpData pgpData,
            MessagingController controller, MessagingListener listener) throws MessagingException {
        resetView();

        String text = null;
        if (pgpData != null) {
            text = pgpData.getDecryptedData();
            if (text != null) {
                text = HtmlConverter.textToHtml(text);
            }
        }

        if (text == null) {
            text = message.getTextForDisplay();
        }
        
        if (text != null) {
            text = text.replaceAll("<\\s*img\\s", "<img style=\"max-width:100%;height:auto\" ");
        }

        // Save the text so we can reset the WebView when the user clicks the "Show pictures" button
        mText = text;

        //mHasAttachments = message.hasAttachments();
        // mAttachmentCount仅存储非内联附件数目，
        // 故对所有邮件都应继续检查是否有内联附件
        // Modified by LL
        mHasAttachments = true;
        
        if (mHasAttachments) {
            //renderAttachments(message, 0, message, account, controller, listener);
        	// Modified by LL
        	// BEGIN
        	synchronized (MailChat.attachmentList) {
        		Log.v(MailChat.LOG_TAG, String.format("%s attachments removed.", MailChat.attachmentList.size()));
	        	MailChat.attachmentList.clear();
        	}
        	renderAttachments(message, 0, message, account, controller, listener);
        	synchronized (MailChat.attachmentList) {
        		for (int i = 0, count = mAttachments.getChildCount(); i < count; i++) {
        			AttachmentView av = (AttachmentView)mAttachments.getChildAt(i);
        			MailChat.attachmentList.put(av.mDownloadingId, av);
                }
        		Log.v(MailChat.LOG_TAG, String.format("%s attachments added.", MailChat.attachmentList.size()));
        	}
        	
        	attachmentCount = message.getAttachmentCount();
        	// END
            
            if (attachmentCount > 0) {
            	attachmentCountText.setText(String.format(getResources().getString(R.string.messageBody_Attachment), 
            		attachmentCount));
            	attachmentCountBuoyText.setText(Integer.toString(attachmentCount));                        
            	attachmentBuoyView.setVisibility(View.VISIBLE);
                nonLockingScrollView.setScrollViewListener(new ScrollViewListener() {
        			@Override
        			public void onScrollChanged(NonLockingScrollView scrollView, int x, int y,
        					int oldx, int oldy) {
        				// TODO Auto-generated method stub
        				if(y>(mAttachmentsContainer.getY()-(screenHeigh-actionBarheight-buttonHeight-statusBarHeight)+attachmentCountText.getHeight())){
        					attachmentBuoyView.setVisibility(View.GONE);
        				}else{
        					attachmentBuoyView.setVisibility(View.VISIBLE);
        				}
        				isFirstShow=false;
       			   }
        		});
            }
            else {
            	attachmentCountText.setVisibility(View.GONE);
            	attachmentBuoyView.setVisibility(View.GONE);
            }
        }else{
        	attachmentBuoyView.setVisibility(View.GONE);
        }
                
        if (text != null) {
        	onShowAll();
            loadBodyFromText(text);
            updateCryptoLayout(account.getCryptoProvider(), pgpData, message);
            mOpenPgpView.updateLayout(account, pgpData.getDecryptedData(),
                    pgpData.getSignatureResult(), message);
        } else {
            onShowAll();
            showStatusMessage(getContext().getString(R.string.webview_empty_message));
        }
        
        // Save related objects and load inline attachments automatically.
        // Modified by LL
    	// BEGIN
    	mAccount = account;
    	mPgpData = pgpData;
    	mMessage = message;
    	loadInlineAttachments();
    	// END
    	
    	// Modified by LL
    	// BEGIN
    	mController = controller;
    	mListener = listener;
    	// END
    }
    public void showStatusMessage(String status) {
        String text = "<div style=\"text-align:center; color: grey;\">" + status + "</div>";
        loadBodyFromText(text);
        mCryptoView.hide();
    }

    private void loadBodyFromText(String emailText) {
        mMessageContentView.setText(emailText);
    }

    public void updateCryptoLayout(CryptoProvider cp, PgpData pgpData, Message message) {
        mCryptoView.updateLayout(cp, pgpData, message);
    }

    public void showAttachments(boolean show) {
        mAttachmentsContainer.setVisibility(show ? View.VISIBLE : View.GONE);        
        boolean showHidden = (show && mHiddenAttachments.getVisibility() == View.GONE &&
                mHiddenAttachments.getChildCount() > 0);

        // Hide show attachments button.
        // Modified by LL
        // BEGIN
        showHidden = false;
        // END
        
        mShowHiddenAttachments.setVisibility(showHidden ? View.VISIBLE : View.GONE);
    }

    public void showMessageWebView(boolean show) {
        mMessageContentView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setAttachmentsEnabled(boolean enabled) {
        for (int i = 0, count = mAttachments.getChildCount(); i < count; i++) {
            AttachmentView attachment = (AttachmentView) mAttachments.getChildAt(i);
            
            // Repair attachments downloading still can be triggered
            // during a ongoing download bug.
            // Modified by LL
            // BEGIN
            attachment.setEnabled(enabled);
            // END
        }
    }

    public void removeAllAttachments() {
        for (int i = 0, count = mAttachments.getChildCount(); i < count; i++) {
            mAttachments.removeView(mAttachments.getChildAt(i));
        }
    }
    
    public boolean isAllAttachmentsDownloaded() {
    	for (int i = 0, count = mAttachments.getChildCount(); i < count; i++) {
            if (((AttachmentView)(mAttachments.getChildAt(i))).mStatus
            		!= AttachmentView.Status.COMPLETE) {
            	return false;
            }
        }
    	for (int i = 0, count = mHiddenAttachments.getChildCount(); i < count; i++) {
            if (((AttachmentView)(mHiddenAttachments.getChildAt(i))).mStatus
            		!= AttachmentView.Status.COMPLETE) {
            	return false;
            }
        }
    	return true;
    }
   
    public void renderAttachments(Part part, int depth, Message message, Account account,
                                  MessagingController controller, MessagingListener listener) throws MessagingException {	
        if (part.getBody() instanceof Multipart) {
            Multipart mp = (Multipart) part.getBody();                 
            for (int i = 0; i < mp.getCount(); i++) {
                renderAttachments(mp.getBodyPart(i), depth + 1, message, account, controller, listener);
            }
        } else if (part instanceof LocalStore.LocalAttachmentBodyPart) {
        	/*
            AttachmentView view = (AttachmentView)mInflater.inflate(R.layout.message_view_attachment, null);
            view.setCallback(attachmentCallback);

            try {
            	// Hide inline attachments, only normal attachments are counted.
            	// Modified by LL
            	// BEGIN
                if (view.populateFromPart(part, message, account, controller, listener)) {
                    addAttachment(view);
                    attachmentCount+=1;
                } else {
                    addHiddenAttachment(view);
                }
                // END
            } catch (Exception e) {
                Log.e(MailChat.LOG_TAG, "Error adding attachment view", e);
            }
            */
        	// Modified by LL
        	// BEGIN
        	LocalStore.LocalAttachmentBodyPart attPart = (LocalStore.LocalAttachmentBodyPart)part;
        	String downloadingId = attPart.getAttachmentId() + message.getUid() + account.getUuid();
        	
        	AttachmentView view = null;
        	synchronized(MailChat.downloadingList) {
        		view = MailChat.downloadingList.get(downloadingId);
        		
        		if (view != null && view.getListener() == null) {
        			view.setListener(listener);
        		}
        	}
        	
        	if (view == null) {
                view = (AttachmentView)mInflater.inflate(R.layout.message_view_attachment, null);
                view.setCallback(attachmentCallback);

                try {
                	// Hide inline attachments, only normal attachments are counted.
                	// Modified by LL
                	// BEGIN
                    if (view.populateFromPart(part, message, account, controller, listener)) {
                        addAttachment(view);
                        
                        //attachmentCount+=1;
                        // 修复重新加载后附件数量显示错误问题
                        // Modified by LL
                        //attachmentCount = mAttachments.getChildCount();
                    } else {
                        addHiddenAttachment(view);
                    }
                    // END
                } catch (Exception e) {
                    Log.e(MailChat.LOG_TAG, "Error adding attachment view", e);
                }
        	} else {
        		view.setCallback(attachmentCallback);
                if (view.contentId == null) {
                    addAttachment(view);
                    
                    //attachmentCount+=1;
                    // 修复重新加载后附件数量显示错误问题
                    // Modified by LL
                    //attachmentCount = mAttachments.getChildCount();
                } else {
                    addHiddenAttachment(view);
                }
        	}
        	// END
        }
    }

    public void addAttachment(View attachmentView) {
    	// 避免AttachmentView同时被多个Layout引用
    	// Modified by LL
    	// BEGIN
    	ViewParent parent = attachmentView.getParent();
    	if (parent instanceof LinearLayout) {
    		((LinearLayout) parent).removeView(attachmentView);
    	}
    	// END
    	
    	mAttachments.addView(attachmentView);
    }

    public void addHiddenAttachment(View attachmentView) {
    	// 避免AttachmentView同时被多个Layout引用
    	// Modified by LL
    	// BEGIN
    	ViewParent parent = attachmentView.getParent();
    	if (parent instanceof LinearLayout) {
    		((LinearLayout) parent).removeView(attachmentView);
    	}
    	// END
    	
        mHiddenAttachments.addView(attachmentView);
    }

    public void zoom(KeyEvent event) {
        if (event.isShiftPressed()) {
            mMessageContentView.zoomIn();
        } else {
            mMessageContentView.zoomOut();
        }
    }

    public void beginSelectingText() {
        mMessageContentView.emulateShiftHeld();
    }

    public void resetView() {
        mMessageContentView.clearCache(true);
        
        mDownloadRemainder.setVisibility(View.GONE);
        setLoadPictures(false);
        showShowAttachmentsAction(false);
        showShowMessageAction(false);
        showShowPicturesAction(false);
        mAttachments.removeAllViews();
        mHiddenAttachments.removeAllViews();

        /*
         * Clear the WebView content
         *
         * For some reason WebView.clearView() doesn't clear the contents when the WebView changes
         * its size because the button to download the complete message was previously shown and
         * is now hidden.
         */
        loadBodyFromText("");
    }

    public void resetHeaderView() {
        mHeaderContainer.setVisibility(View.GONE);
        
        // Modified by LL
        // BEGIN
        mLoadingStatus.setVisibility(View.GONE);
        mReload.setVisibility(View.GONE);
        // END
    }

    public AttachmentView.AttachmentFileDownloadCallback getAttachmentCallback() {
        return attachmentCallback;
    }

    public void setAttachmentCallback(
        AttachmentView.AttachmentFileDownloadCallback attachmentCallback) {
        this.attachmentCallback = attachmentCallback;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState savedState = new SavedState(superState);

        savedState.attachmentViewVisible = (mAttachmentsContainer != null &&
                mAttachmentsContainer.getVisibility() == View.VISIBLE);
        savedState.hiddenAttachmentsVisible = (mHiddenAttachments != null &&
                mHiddenAttachments.getVisibility() == View.VISIBLE);
        savedState.showPictures = mShowPictures;

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState)state;
        super.onRestoreInstanceState(savedState.getSuperState());

        mSavedState = savedState;
    }

    @Override
    public void onLayoutChanged() {
        if (mMessageContentView != null) {
            mMessageContentView.invalidate();
        }
    }

    static class SavedState extends BaseSavedState {
        boolean attachmentViewVisible;
        boolean hiddenAttachmentsVisible;
        boolean showPictures;

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };


        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.attachmentViewVisible = (in.readInt() != 0);
            this.hiddenAttachmentsVisible = (in.readInt() != 0);
            this.showPictures = (in.readInt() != 0);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt((this.attachmentViewVisible) ? 1 : 0);
            out.writeInt((this.hiddenAttachmentsVisible) ? 1 : 0);
            out.writeInt((this.showPictures) ? 1 : 0);
        }
    }

    class DownloadImageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0];
            try {
                boolean externalImage = urlString.startsWith("http");

                String filename = null;
                String mimeType = null;
                InputStream in = null;

                try {
                    if (externalImage) {
                        URL url = new URL(urlString);
                        URLConnection conn = url.openConnection();
                        in = conn.getInputStream();

                        String path = url.getPath();

                        // Try to get the filename from the URL
                        int start = path.lastIndexOf("/");
                        if (start != -1 && start + 1 < path.length()) {
                            filename = URLDecoder.decode(path.substring(start + 1), "UTF-8");
                        } else {
                            // Use a dummy filename if necessary
                            filename = "saved_image";
                        }

                        // Get the MIME type if we couldn't find a file extension
                        if (filename.indexOf('.') == -1) {
                            mimeType = conn.getContentType();
                        }
                    } else {
                        ContentResolver contentResolver = getContext().getContentResolver();
                        Uri uri = Uri.parse(urlString);

                        // Get the filename from AttachmentProvider
                        Cursor cursor = contentResolver.query(uri, ATTACHMENT_PROJECTION, null, null, null);
                        if (cursor != null) {
                            try {
                                if (cursor.moveToNext()) {
                                    filename = cursor.getString(DISPLAY_NAME_INDEX);
                                }
                            } finally {
                                cursor.close();
                            }
                        }

                        // Use a dummy filename if necessary
                        if (filename == null) {
                            filename = "saved_image";
                        }

                        // Get the MIME type if we couldn't find a file extension
                        if (filename.indexOf('.') == -1) {
                            mimeType = contentResolver.getType(uri);
                        }

                        in = contentResolver.openInputStream(uri);
                    }

                    // Do we still need an extension?
                    if (filename.indexOf('.') == -1) {
                        // Use JPEG as fallback
                        String extension = "jpeg";
                        if (mimeType != null) {
                            // Try to find an extension for the given MIME type
                            String ext = MimeUtility.getExtensionByMimeType(mimeType);
                            if (ext != null) {
                                extension = ext;
                            }
                        }
                        filename += "." + extension;
                    }

                    String sanitized = Utility.sanitizeFilename(filename);

                    File directory = new File(MailChat.getAttachmentDefaultPath());
                    File file = Utility.createUniqueFile(directory, sanitized);
                    FileOutputStream out = new FileOutputStream(file);
                    try {
                        IOUtils.copy(in, out);
                        out.flush();
                    } finally {
                        out.close();
                    }

                    return file.getName();

                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String filename) {
            String text;
            if (filename == null) {
                text = getContext().getString(R.string.image_saving_failed);
            } else {
                text = getContext().getString(R.string.image_saved_as, filename);
            }

            Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
        }
    }
	
	// Objects and functions for automatically downloading inline attachments
	// and updating message view
	private Account mAccount;
	private PgpData mPgpData;
	private Message mMessage;
	
	public void updateMessageContentView(String cid, String uri) {
		if (mText != null) {
			mText = mText.replace("cid:" + cid, uri);
		}
		this.loadBodyFromText(mText);
		updateCryptoLayout(mAccount.getCryptoProvider(), mPgpData, mMessage);
        mOpenPgpView.updateLayout(mAccount, mPgpData.getDecryptedData(),
                mPgpData.getSignatureResult(), mMessage);
	}
	
	private void loadInlineAttachments() {
		// Optimize inline attachments' loading sequence
		for (int i = mHiddenAttachments.getChildCount() - 1; i >= 0; i--) {
			View view = mHiddenAttachments.getChildAt(i);
			((AttachmentView)view).loadInlineAttachment();
		}
	}
	
	// 显示和隐藏正在加载提示与重新加载提示
	private MessagingController mController;
	private MessagingListener mListener;
	
	public void setLoadingStatus(boolean isLoading) {
		mLoadingStatus.setVisibility(isLoading ? View.VISIBLE : View.GONE);
	}
	
	public void setReload(boolean isReload) {
		mReload.setVisibility(isReload ? View.VISIBLE : View.GONE);
	}
	
	public void setReloadText(String title, String subtitle) {
		mReloadTitle.setText(title);
		if (subtitle == null) {
		    mReloadSubtitle.setVisibility(View.GONE);
		} else {
		    mReloadSubtitle.setText(subtitle);
		}
	}

	public void onReload() {
	    synchronized (MailChat.downloadingList) {
            for (int i = 0, count = mAttachments.getChildCount(); i < count; i++) {
                AttachmentView av = (AttachmentView)mAttachments.getChildAt(i);
                if (MailChat.downloadingList.remove(av.mDownloadingId) != null) {
                    av.cancel();
                }
            }
        }
	}
}
