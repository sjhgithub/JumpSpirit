package cn.mailchat.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.james.mime4j.codec.EncoderUtil;
import org.apache.james.mime4j.util.MimeUtil;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleHtmlSerializer;
import org.htmlcleaner.TagNode;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.Account.MessageFormat;
import cn.mailchat.Account.QuoteStyle;
import cn.mailchat.FontSizes;
import cn.mailchat.GlobalConstants;
import cn.mailchat.Identity;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.loader.AttachmentContentLoader;
import cn.mailchat.activity.loader.AttachmentInfoLoader;
import cn.mailchat.activity.misc.Attachment;
import cn.mailchat.adapter.EmailAddressAutoCompletedAdapter;
import cn.mailchat.beans.PickedFileInfo;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.crypto.PgpData;
import cn.mailchat.fragment.ProgressDialogFragment;
import cn.mailchat.helper.HtmlConverter;
import cn.mailchat.helper.IdentityHelper;
import cn.mailchat.helper.SizeFormatter;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.helper.Utility;
import cn.mailchat.mail.Address;
import cn.mailchat.mail.Body;
import cn.mailchat.mail.FetchProfile;
import cn.mailchat.mail.Flag;
import cn.mailchat.mail.Message;
import cn.mailchat.mail.Message.RecipientType;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.mail.Multipart;
import cn.mailchat.mail.Part;
import cn.mailchat.mail.internet.MimeBodyPart;
import cn.mailchat.mail.internet.MimeHeader;
import cn.mailchat.mail.internet.MimeMessage;
import cn.mailchat.mail.internet.MimeMultipart;
import cn.mailchat.mail.internet.MimeUtility;
import cn.mailchat.mail.internet.TextBody;
import cn.mailchat.mail.internet.TextBodyBuilder;
import cn.mailchat.mail.store.LocalStore;
import cn.mailchat.mail.store.LocalStore.LocalAttachmentBody;
import cn.mailchat.mail.store.LocalStore.LocalAttachmentBodyPart;
import cn.mailchat.mail.store.LocalStore.TempFileBody;
import cn.mailchat.mail.store.LocalStore.TempFileMessageBody;
import cn.mailchat.utils.AttachmentUtil;
import cn.mailchat.utils.CommonUtils;
import cn.mailchat.utils.DataTransfer;
import cn.mailchat.utils.FileUtil;
import cn.mailchat.utils.NetUtil;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.utils.TimeUtils;
import cn.mailchat.utils.ViewUtils;
import cn.mailchat.view.AddressViewCallBack;
import cn.mailchat.view.ChoseAddressView;
import cn.mailchat.view.MailDialog;
import cn.mailchat.view.MessageViewWebView;
import cn.mailchat.view.MessageViewWebView.DisplayFinish;

import com.umeng.analytics.MobclickAgent;

/**
 * * 邮件撰写页 写信
 * 
 * @Description:
 * @author:xuqq
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-8-13
 * 
 * @copyright © 35.com
 * @file name ：MailComposeActivity.java
 * @author ：zhangjx
 * @create Data ：2014-12-9下午2:11:10
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-12-9下午2:11:10
 * @Modified by：zhangjx
 * @Description :
 */
public class MailComposeActivity extends BaseActionbarFragmentActivity
		implements OnClickListener, AddressViewCallBack,
		ProgressDialogFragment.CancelListener {

	public enum SourceFolder {
		DRAFT,
		SENT,
		OUTBOX
	}
	
	private static final String ACTION_EDIT_DRAFT = "cn.mailchat.intent.action.EDIT_DRAFT";
	private static final String ACTION_EDIT_SENT = "cn.mailchat.intent.action.EDIT_SENT";
	private static final String ACTION_EDIT_OUTBOX = "cn.mailchat.intent.action.EDIT_OUTBOX";
	private static final String ACTION_REPLY = "cn.mailchat.intent.action.REPLY";
	private static final String ACTION_REPLY_ALL = "cn.mailchat.intent.action.REPLY_ALL";
	private static final String ACTION_FORWARD = "cn.mailchat.intent.action.FORWARD";
	private static final String ACTION_FORWARD_MESSAGE = "cn.mailchat.intent.action.FORWARD_MESSAGE";
	private static final String EXTRA_ACCOUNT = "account";
	private static final String EXTRA_MESSAGE_BODY = "messageBody";
	private static final String EXTRA_MESSAGE_REFERENCE = "message_reference";
	private static final String EXTRA_MESSAGE_SUBJECT = "message_subject";
	private static final String EXTRA_MESSAGE_TEXT = "message_text";
	private static final String EXTRA_ATTACHMENT_URI = "attachment_uri";
	private static final String EXTRA_ATTACHMENT_NAME = "attachment_name";
	private static final String EXTRA_ATTACHMENT_POSITION = "attachment_position";
	private static final String ACTION_COMPOSE = "cn.mailchat.intent.action.COMPOSE";
	private static final String STATE_KEY_SOURCE_MESSAGE_PROCED = "cn.mailchat.activity.MessageCompose.stateKeySourceMessageProced";
	
	private static final int ACTIVITY_REQUEST_PICK_ATTACHMENT = 1;// 文件的requestcode
	private static final int CAMERA_WITH_DATA = 2; // 照相的requestCode;
	private static final int PHOTO_PICKED_WITH_DATA = 3; // 相册的requestCode;
	private static final int ACTIVITY_REQUEST_ADD_RECIPIENTS_TO = 4;// 调整联系人界面(抄送/收件/密送)
	private static final int ACTIVITY_REQUEST_ADD_RECIPIENTS_CC = 5;
	private static final int ACTIVITY_REQUEST_ADD_RECIPIENTS_BCC = 6;

	private static final long INVALID_DRAFT_ID = MessagingController.INVALID_MESSAGE_ID;
	private static final int MSG_PROGRESS_ON = 1;
	private static final int MSG_PROGRESS_OFF = 2;
	private static final int MSG_SKIPPED_ATTACHMENTS = 3;
	private static final int MSG_SAVED_DRAFT = 4;
	private static final int MSG_DISCARDED_DRAFT = 5;
	private static final int MSG_PERFORM_STALLED_ACTION = 6;

	private static final int REPLY_WRAP_LINE_WIDTH = 72;
	private static final int QUOTE_BUFFER_LENGTH = 512; // amount of extra
														// buffer to allocate to
														// accommodate quoting
														// headers or prefixes
	// Regular expressions to look for various HTML tags. This is no
	// HTML::Parser, but hopefully it's good enough for
	// our purposes.
	private static final Pattern FIND_INSERTION_POINT_HTML = Pattern
			.compile("(?si:.*?(<html(?:>|\\s+[^>]*>)).*)");
	private static final Pattern FIND_INSERTION_POINT_HEAD = Pattern
			.compile("(?si:.*?(<head(?:>|\\s+[^>]*>)).*)");
	private static final Pattern FIND_INSERTION_POINT_BODY = Pattern
			.compile("(?si:.*?(<body(?:>|\\s+[^>]*>)).*)");
	private static final Pattern FIND_INSERTION_POINT_HTML_END = Pattern
			.compile("(?si:.*(</html>).*?)");
	private static final Pattern FIND_INSERTION_POINT_BODY_END = Pattern
			.compile("(?si:.*(</body>).*?)");
	// The first group in a Matcher contains the first capture group. We capture
	// the tag found in the above REs so that
	// we can locate the *end* of that tag.
	private static final int FIND_INSERTION_POINT_FIRST_GROUP = 1;
	// HTML bits to insert as appropriate
	// TODO is it safe to assume utf-8 here?
	private static final String FIND_INSERTION_POINT_HTML_CONTENT = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\r\n<html>";
	private static final String FIND_INSERTION_POINT_HTML_END_CONTENT = "</html>";
	private static final String FIND_INSERTION_POINT_HEAD_CONTENT = "<head><meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\"></head>";
	// Index of the start of the beginning of a String.
	private static final int FIND_INSERTION_POINT_START_OF_STRING = 0;
	// 发件人
	private static final String EXTRA_TO_EMAIL = "toEmail";

	// 收件人地址栏
	private ChoseAddressView mToView;
	// 抄送人地址栏
	private ChoseAddressView mCcView;
	// 密送地址栏
	private ChoseAddressView mBccView;
	private ScrollView scrollview;
	// 添加联系人栏适配器
	private EmailAddressAutoCompletedAdapter mAddressAutoCompAdapter;
	// cc & bcc 是否可见
	private boolean isExtraAddressExpand;
	private ContentWatcher watcher;
	// 判断用户是否有输入，通过此变量判断
	private boolean mIdentityChanged = false;
	// 在保存草稿后有无操作
	private boolean mDraftNeedsSaving = false;
	//签名是否修改
	private boolean mSignatureChanged = false;
	// 主题栏
	private EditText mSubjectView;
	// 正文栏
	private EditText mMessageContentView;
	//签名区
    private EditText mSignatureView;
	// 附件栏
	private GridLayout mAttachmentsLayout;
	private List<Attachment> mHiddenAttachments;
	private Handler mHandler;
	// 保存邮件的定时器
	private Timer mTimer;
	private MessagingController messagingController;
	private Account mAccount;
	// 添加附件按钮
	private RelativeLayout mBtnPickFile, mBtnPickFromAlubm, mBtnPickFromCamera;
	// 照相得到的相片
	private File mPhotoFile;
	private List<CGroupMember> contactMember;
	private List<Address> addressList;
	private MessageViewWebView mQuotedHTML;
	// 引文区域操作条
	private LinearLayout mQuotedTextBar;
	// 是否显示引文
	private CheckBox mQuotedCheckBoxCite;
	// 编辑引用消息
	private TextView tvEdit;
	// 引用消息，引文
	private EditText editedOriginalContent;
	private CheckBox checkBoxUpload;
	private LinearLayout layoutAttachment;
	private View emptyView;
	private FontSizes mFontSizes;
	private MessageReference mMessageReference;
	private Message mSourceMessage;
	private String mSourceMessageBody;
	
	// 记录源邮件的引用信息
	// BEGIN
	private String mSourceIdentity = null;
	private String mMessageSubject = null;
	private String mMessageText = null;
	private Uri mAttachmentUri = null;
	private String mAttachmentName = null;
	private String mAttachmentPosition = null;
	// END
	// 客户端邮件ID
	private String mMailChatMessageID = null;

	private Listener mListener = new Listener();
	private boolean mSourceProcessed = false;
	private View mCustomActionbarView;
	private TextView mTitle;
	private TextView mSubTitle;
	private TextView mActionbarSure;
	private Identity mIdentity;
	private String mReferences;
	private String mInReplyTo;
	private PgpData mPgpData = null;
	private boolean mReadReceipt = false;
	/**
	 * The currently used message format.
	 * 
	 * <p>
	 * <strong>Note:</strong> Don't modify this field directly. Use
	 * {@link #updateMessageFormat()}.
	 * </p>
	 */
	private SimpleMessageFormat mMessageFormat;
	private QuoteStyle mQuoteStyle;
	/**
	 * The database ID of this message's draft. This is used when saving drafts
	 * so the message in the database is updated instead of being created anew.
	 * This property is INVALID_DRAFT_ID until the first save.
	 */
	private long mDraftId = INVALID_DRAFT_ID;
	/**
	 * Indicates that the source message has been processed at least once and
	 * should not be processed on any subsequent loads. This protects us from
	 * adding attachments that have already been added from the restore of the
	 * view state.
	 */
	private boolean mSourceMessageProcessed = false;
	private String mOpenPgpProvider;
	private OpenPgpServiceConnection mOpenPgpServiceConnection;
	/**
	 * When this it {@code true} the message format setting is ignored and we're
	 * always sending a text/plain message.
	 */
	private boolean mForcePlainText = false;
	
	// 是否有引文
	private QuotedTextMode mQuotedTextMode = QuotedTextMode.NONE;
	
	/**
	 * Contains the format of the quoted text (text vs. HTML).
	 */
	private SimpleMessageFormat mQuotedTextFormat;
	/**
	 * 容器为HTML回复的
	 */
	private InsertableHtmlContent mQuotedHtmlContent; // Container for HTML
														// reply as it's being
														// built.
	// Modified by LL
	private String mQuotedString;
	
	//private int mMaxLoaderId = 0;
	// Modified by LL
	private AtomicInteger mMaxLoaderId = new AtomicInteger();
	
	/**
	 * Number of attachments currently being fetched.
	 */
	private int mNumAttachmentsLoading = 0;
	private Action mAction;

	enum Action {
		COMPOSE, REPLY, REPLY_ALL, FORWARD, EDIT_DRAFT, EDIT_OUTBOX, EDIT_SENT, FORWARD_MESSAGE
	}

	enum SimpleMessageFormat {
		TEXT, HTML
	}

	private enum QuotedTextMode {
		NONE, SHOW, HIDE
	}

	// FYI, there's nothing in the code that requires these variables to one
	// letter. They're one
	// letter simply to save space. This name sucks. It's too similar to
	// Account.Identity.
	private enum IdentityField {
		LENGTH("l"), OFFSET("o"), FOOTER_OFFSET("fo"), PLAIN_LENGTH("pl"), PLAIN_OFFSET(
				"po"), MESSAGE_FORMAT("f"), MESSAGE_READ_RECEIPT("r"), SIGNATURE(
				"s"), NAME("n"), EMAIL("e"),
		// TODO - store a reference to the message being replied so we can mark
		// it at the time of send.
		ORIGINAL_MESSAGE("m"), CURSOR_POSITION("p"), // Where in the message
														// your cursor was when
														// you saved.
		QUOTED_TEXT_MODE("q"), QUOTE_STYLE("qs");

		private final String value;

		IdentityField(String value) {
			this.value = value;
		}

		public String value() {
			return value;
		}

		/**
		 * Get the list of IdentityFields that should be integer values.
		 * 
		 * <p>
		 * These values are sanity checked for integer-ness during decoding.
		 * </p>
		 * 
		 * @return The list of integer {@link IdentityField}s.
		 */
		public static IdentityField[] getIntegerFields() {
			return new IdentityField[] { LENGTH, OFFSET, FOOTER_OFFSET,
					PLAIN_LENGTH, PLAIN_OFFSET };
		}
	}

	/**
	 * 
	 * method name: actionCompose function @Description: TODO 邮件撰写action
	 * Parameters and return values description：
	 * 
	 * @param context
	 * @param account
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-11-14 下午1:23:50 @Modified by：zhangjx
	 * @Description：Compose a new message using the given account. If account is
	 *                      null the default account will be used.
	 */
	public static void actionCompose(Context context, Account account,
			String toEmail) {
		String accountUuid = (account == null) ? Preferences
				.getPreferences(context).getDefaultAccount().getUuid()
				: account.getUuid();

		Intent i = new Intent(context, MailComposeActivity.class);
		i.putExtra(EXTRA_ACCOUNT, accountUuid);
		i.putExtra(EXTRA_TO_EMAIL, toEmail);
		i.setAction(ACTION_COMPOSE);
		context.startActivity(i);
	}

	/**
	 * Get intent for composing a new message as a reply to the given message.
	 * If replyAll is true the function is reply all instead of simply reply.
	 * 
	 * @Date：2014-11-14 下午1:23:50 @Modified by：zhangjx
	 * @param context
	 * @param account
	 * @param message
	 * @param replyAll
	 * @param messageBody
	 *            optional, for decrypted messages, null if it should be grabbed
	 *            from the given message
	 */
	public static Intent getActionReplyIntent(Context context, Account account,
			Message message, boolean replyAll, String messageBody) {
		Intent i = new Intent(context, MailComposeActivity.class);
		i.putExtra(EXTRA_MESSAGE_BODY, messageBody);
		i.putExtra(EXTRA_MESSAGE_REFERENCE, message.makeMessageReference());
		if (replyAll) {
			i.setAction(ACTION_REPLY_ALL);
		} else {
			i.setAction(ACTION_REPLY);
		}
		return i;
	}

	/**
	 * Compose a new message as a reply to the given message. If replyAll is
	 * true the function is reply all instead of simply reply.
	 * 
	 * @param context
	 * @param account
	 * @param message
	 * @param replyAll
	 * @param messageBody
	 *            optional, for decrypted messages, null if it should be grabbed
	 *            from the given message
	 */
	public static void actionReply(Context context, Account account,
			Message message, boolean replyAll, String messageBody) {
		context.startActivity(getActionReplyIntent(context, account, message,
				replyAll, messageBody));
	}

	/**
	 * Compose a new message as a forward of the given message.
	 * 
	 * @param context
	 * @param account
	 * @param message
	 * @param messageBody
	 *            optional, for decrypted messages, null if it should be grabbed
	 *            from the given message
	 */
	public static void actionForward(Context context, Account account,
			Message message, String messageBody) {
		Intent i = new Intent(context, MailComposeActivity.class);
		i.putExtra(EXTRA_MESSAGE_BODY, messageBody);
		i.putExtra(EXTRA_MESSAGE_REFERENCE, message.makeMessageReference());
		i.setAction(ACTION_FORWARD);
		context.startActivity(i);
	}

	/**
	 * Continue composition of the given message. This action modifies the way
	 * this Activity handles certain actions. Save will attempt to replace the
	 * message in the given folder with the updated version. Discard will delete
	 * the message from the given folder.
	 * 
	 * @param context
	 * @param message
	 */
	public static void actionEdit(Context context,
			MessageReference messageReference,
			SourceFolder sourceFolder) {
		Intent i = new Intent(context, MailComposeActivity.class);
		i.putExtra(EXTRA_MESSAGE_REFERENCE, messageReference);
		switch(sourceFolder) {
			case DRAFT:
				i.setAction(ACTION_EDIT_DRAFT);
				break;
			case SENT:
				i.setAction(ACTION_EDIT_SENT);
				break;
			case OUTBOX:
				i.setAction(ACTION_EDIT_OUTBOX);
				break;
		}
		context.startActivity(i);
	}
	
	public static void actionForwardSingleAttachment(Context context,
			Account account,
			String toEmail,
			Message message,
			String attPosition,
			String messageText) {
		
		String accountUuid = (account == null)
				? Preferences.getPreferences(context).getDefaultAccount().getUuid()
				: account.getUuid();
		
		Intent i = new Intent(context, MailComposeActivity.class);
		i.putExtra(EXTRA_ACCOUNT, accountUuid);
		i.putExtra(EXTRA_TO_EMAIL, toEmail);
		i.putExtra(EXTRA_MESSAGE_REFERENCE, message.makeMessageReference());
		i.putExtra(EXTRA_ATTACHMENT_POSITION, attPosition);
		i.putExtra(EXTRA_MESSAGE_TEXT, messageText);
		i.setAction(ACTION_FORWARD);
		context.startActivity(i);
	}
	
	public static void actionForwardChatMessage(Context context,
			Account account,
			String toEmail,
			String messageSubject,
			String messageText,
			Uri attUri,
			String attName) {
		
		String accountUuid = (account == null)
				? Preferences.getPreferences(context).getDefaultAccount().getUuid()
				: account.getUuid();

		Intent i = new Intent(context, MailComposeActivity.class);
		i.putExtra(EXTRA_ACCOUNT, accountUuid);
		i.putExtra(EXTRA_TO_EMAIL, toEmail);
		i.putExtra(EXTRA_MESSAGE_SUBJECT, messageSubject);
		i.putExtra(EXTRA_MESSAGE_TEXT, messageText);
		i.putExtra(EXTRA_ATTACHMENT_URI, attUri);
		i.putExtra(EXTRA_ATTACHMENT_NAME, attName);
		i.setAction(ACTION_FORWARD_MESSAGE);
		context.startActivity(i);
	}

	private LoaderManager.LoaderCallbacks<Attachment> mAttachmentInfoLoaderCallback = new LoaderManager.LoaderCallbacks<Attachment>() {
		@Override
		public Loader<Attachment> onCreateLoader(int id, Bundle args) {
			onFetchAttachmentStarted();
			Attachment attachment = args.getParcelable(LOADER_ARG_ATTACHMENT);
			return new AttachmentInfoLoader(MailComposeActivity.this,
					attachment);
		}

		@Override
		public void onLoadFinished(Loader<Attachment> loader,
				Attachment attachment) {
			int loaderId = loader.getId();

			View view = getAttachmentView(loaderId);
			if (view != null) {
				view.setTag(attachment);

				TextView nameView = (TextView) view
						.findViewById(R.id.attachment_name);
				ImageView imgIcon = (ImageView) view
						.findViewById(R.id.attachment_img);
				TextView textInfo = (TextView) view
						.findViewById(R.id.attachment_size);
				nameView.setText(attachment.name);
				
				/*
				imgIcon.setImageBitmap(AttachmentUtil
						.getAttachmentIconForMessageCompose(
								MailComposeActivity.this, attachment.name,
								FileUtil.getPath(MailComposeActivity.this,
										attachment.uri)));
				textInfo.setText(FileUtil.sizeLongToString(attachment.size));
				*/
				// 未下载附件Base64解码后预计大小和已下载附件实际大小区分计算
				// Modified by LL
				// BEGIN
				imgIcon.setImageBitmap(AttachmentUtil.getInstance(MailComposeActivity.this)
						.getAttachmentIcon(attachment.name, false));
				if (attachment.uri != null) {
					textInfo.setText(SizeFormatter.formatSize(MailComposeActivity.this, attachment.size));
				} else {
					// TODO 增宽AttachmentView容纳未下载附件大小标识前缀“约/about”
					textInfo.setText(SizeFormatter.formatSize(MailComposeActivity.this, MimeUtility.getDecodeBase64Size(attachment.size)));
				}
				// END
				
				//attachment.loaderId = ++mMaxLoaderId;
				// Modified by LL
				attachment.loaderId = mMaxLoaderId.incrementAndGet();
				
				// mAttachments_layout.setPadding(0, 0, 0, 13);
				initAttachmentContentLoader(attachment);
			} else {
				onFetchAttachmentFinished();
			}

			getLoaderManager().destroyLoader(loaderId);
		}

		@Override
		public void onLoaderReset(Loader<Attachment> loader) {
			onFetchAttachmentFinished();
		}
	};

	private void onFetchAttachmentStarted() {
		mNumAttachmentsLoading += 1;
	}

	private void initAttachmentContentLoader(Attachment attachment) {
		LoaderManager loaderManager = getLoaderManager();
		Bundle bundle = new Bundle();
		bundle.putParcelable(LOADER_ARG_ATTACHMENT, attachment);
		loaderManager.initLoader(attachment.loaderId, bundle,
				mAttachmentContentLoaderCallback);
	}

	private LoaderManager.LoaderCallbacks<Attachment> mAttachmentContentLoaderCallback = new LoaderManager.LoaderCallbacks<Attachment>() {

		@Override
		public Loader<Attachment> onCreateLoader(int id, Bundle args) {
			Attachment attachment = args.getParcelable(LOADER_ARG_ATTACHMENT);
			return new AttachmentContentLoader(MailComposeActivity.this,
					attachment);
		}

		@Override
		public void onLoadFinished(Loader<Attachment> loader,
				Attachment attachment) {
			int loaderId = loader.getId();

			View view = getAttachmentView(loaderId);
			if (view != null) {
				// 区分处理未下载附件和已下载附件
				if (attachment.state == Attachment.LoadingState.COMPLETE) {
					view.setTag(attachment);
					View progressBar = view.findViewById(R.id.progressBar);
					progressBar.setVisibility(View.GONE);
				} else if (attachment.state == Attachment.LoadingState.METADATA 
						&& attachment.uri == null) {
					view.setTag(attachment);
					View progressBar = view.findViewById(R.id.progressBar);
					progressBar.setVisibility(View.GONE);
				} else {
					mAttachmentsLayout.removeView(view);
				}
			}

			onFetchAttachmentFinished();

			getLoaderManager().destroyLoader(loaderId);
		}

		@Override
		public void onLoaderReset(Loader<Attachment> loader) {
			onFetchAttachmentFinished();
		}
	};

	private String toEmail;

	private View getAttachmentView(int loaderId) {
		for (int i = 0, childCount = mAttachmentsLayout.getChildCount(); i < childCount; i++) {
			View view = mAttachmentsLayout.getChildAt(i);
			Attachment tag = (Attachment) view.getTag();
			if (tag != null && tag.loaderId == loaderId) {
				return view;
			}
		}

		return null;
	}

	private void onFetchAttachmentFinished() {
		// We're not allowed to perform fragment transactions when called from
		// onLoadFinished().
		// So we use the Handler to call performStalledAction().
		mHandler.sendEmptyMessage(MSG_PERFORM_STALLED_ACTION);
	}

	class Listener extends MessagingListener {
		@Override
		public void loadCommonContactForViewFinished(Account account,
				final List<ContactAttribute> commonContacts) {
			if (account.getEmail().equals(mAccount.getEmail())) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mToView.setCommonContacts(commonContacts);
						mCcView.setCommonContacts(commonContacts);
						mBccView.setCommonContacts(commonContacts);
					}
				});
				}
		}
		// 正在发送邮件
		@Override
		public void sendPendingMessagesStarted(Account account) {

		}

		@Override
		public void sendMailSuccess(Account account) {

		}

		@Override
		public void sendPendingMessagesFailed(Account account) {
			if (account.getEmail().equals(mAccount.getEmail())) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						ProgressDialogFragment fragment = (ProgressDialogFragment) getSupportFragmentManager()
								.findFragmentByTag("press");

						if (fragment != null) {
							fragment.dismiss();
						}
						Toast.makeText(MailComposeActivity.this,
								getString(R.string.message_send_failure),
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		}

		@Override
		public void loadMessageForViewStarted(Account account, String folder,
				String uid) {
			if ((mMessageReference == null)
					|| !mMessageReference.uid.equals(uid)) {
				return;
			}

			mHandler.sendEmptyMessage(MSG_PROGRESS_ON);
		}

		@Override
		public void loadMessageForViewFinished(Account account, String folder,
				String uid, Message message) {
			if ((mMessageReference == null)
					|| !mMessageReference.uid.equals(uid)) {
				return;
			}

			mHandler.sendEmptyMessage(MSG_PROGRESS_OFF);
		}

		@Override
		public void loadMessageForViewBodyAvailable(Account account,
				String folder, String uid, final Message message) {
			if ((mMessageReference == null)
					|| !mMessageReference.uid.equals(uid)) {
				return;
			}
			
			// 保存二次编辑邮件原客户端ID
			if (mAction == Action.EDIT_DRAFT || mAction == Action.EDIT_OUTBOX) {
				mMailChatMessageID = MimeUtility.getFirstHeader(message, MailChat.MAILCHAT_MESSAGE_ID_HEADER);
			}
			
			// 仅在源邮件为空时才将源邮件设为当前邮件
			if (mSourceMessage == null) {
				mSourceMessage = message;
			}
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// We check to see if we've previously processed the source
					// message since this
					// could be called when switching from HTML to text replies.
					// If that happens, we
					// only want to update the UI with quoted text (which picks
					// the appropriate
					// part).
					if (mSourceProcessed) {
						try {
							populateUIWithQuotedMessage(true);
						} catch (MessagingException e) {
							// Hm, if we couldn't populate the UI after source
							// reprocessing, let's just delete it?
							showOrHideQuotedText(QuotedTextMode.HIDE);
							Log.e(MailChat.LOG_TAG,
									"Could not re-process source message; deleting quoted text to be safe.",
									e);
						}
						updateMessageFormat();
					} else {
						processSourceMessage(message);
						mSourceProcessed = true;
					}
				}
			});
		}

		@Override
		public void loadMessageForViewFailed(Account account, String folder,
				String uid, Throwable t) {
			if ((mMessageReference == null)
					|| !mMessageReference.uid.equals(uid)) {
				return;
			}
			mHandler.sendEmptyMessage(MSG_PROGRESS_OFF);
			// TODO show network error
		}

		@Override
		public void messageUidChanged(Account account, String folder,
				String oldUid, String newUid) {
			// Track UID changes of the source message
			if (mMessageReference != null) {
				final Account sourceAccount = Preferences.getPreferences(
						MailComposeActivity.this).getAccount(
						mMessageReference.accountUuid);
				final String sourceFolder = mMessageReference.folderName;
				final String sourceMessageUid = mMessageReference.uid;

				if (account.equals(sourceAccount)
						&& (folder.equals(sourceFolder))) {
					if (oldUid.equals(sourceMessageUid)) {
						mMessageReference.uid = newUid;
					}
					if ((mSourceMessage != null)
							&& (oldUid.equals(mSourceMessage.getUid()))) {
						mSourceMessage.setUid(newUid);
					}
				}
			}
		}

		public void searchAllContactSuccess(Account account,
				final List<ContactAttribute> contacts) {
			if (account.getEmail().equals(mAccount.getEmail())) {
					runOnUiThread(new Runnable() {
						public void run() {
							mAddressAutoCompAdapter.setAllAddressLists(contacts);
							}
					});
			}
		}
	}

	class MyToastHandler extends Handler {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_PROGRESS_ON:
				setProgressBarIndeterminateVisibility(true);
				break;
			case MSG_PROGRESS_OFF:
				setProgressBarIndeterminateVisibility(false);
				break;
			case MSG_SKIPPED_ATTACHMENTS:
				// 由于一些附件还没有被下载，因此无法转发这些附件。
				Toast.makeText(
						MailComposeActivity.this,
						getString(R.string.message_compose_attachments_skipped_toast),
						Toast.LENGTH_LONG).show();
				break;
			case MSG_SAVED_DRAFT:
				// 邮件已保存为草稿
				Toast.makeText(MailComposeActivity.this,
						getString(R.string.message_saved_toast),
						Toast.LENGTH_LONG).show();
				break;
			case MSG_DISCARDED_DRAFT:
				// 邮件已忽略
				Toast.makeText(MailComposeActivity.this,
						getString(R.string.message_discarded_toast),
						Toast.LENGTH_LONG).show();
				break;
			case MSG_PERFORM_STALLED_ACTION:
				// performStalledAction();
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (UpgradeDatabases.actionUpgradeDatabases(this, getIntent())) {
			finish();
			return;
		}
		// if (MailChat.getK9ComposerThemeSetting() !=
		// MailChat.Theme.USE_GLOBAL) {
		// // theme the whole content according to the theme (except the action
		// // bar)
		// mThemeContext = new ContextThemeWrapper(
		// this,
		// MailChat.getK9ThemeResourceId(MailChat.getK9ComposerTheme()));
		// View v = ((LayoutInflater) mThemeContext
		// .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
		// .inflate(R.layout.activity_message_compose, null);
		// TypedValue outValue = new TypedValue();
		// // background color needs to be forced
		// mThemeContext.getTheme().resolveAttribute(
		// R.attr.messageViewHeaderBackgroundColor, outValue, true);
		// v.setBackgroundColor(outValue.data);
		// setContentView(v);
		// } else {
		setContentView(R.layout.activity_message_compose);
		// }

		initView();
		parserArgument(savedInstanceState);
		initData();
		initAddrView();
		setEditView();
		initializeActionBar();
		initEvent();
		updateMessageFormat();
		setTitle();
	}

	private void initAddrView() {
		messagingController.addListener(mListener);
		messagingController.loadCommonContactForView(mAccount, mListener);
		messagingController.serchAllContacts(mAccount, "",false, mListener);
		initAddressView();
	}

	public void initializeActionBar() {
		mActionBar.setTitle(null);
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayUseLogoEnabled(true);
		// 返回按钮
		mActionBar.setDisplayHomeAsUpEnabled(true);
		// Inflate the custom view
		LayoutInflater inflater = LayoutInflater.from(this);
		mCustomActionbarView = inflater.inflate(
				R.layout.actionbar_custom_right_btn_with_2title, null);
		mActionBar.setCustomView(mCustomActionbarView);
		mTitle = (TextView) mCustomActionbarView
				.findViewById(R.id.actionbar_title_name);
		mSubTitle = (TextView) mCustomActionbarView
				.findViewById(R.id.actionbar_title_sub);
		mActionbarSure = (TextView) mCustomActionbarView
				.findViewById(R.id.tv_sure);
		mActionbarSure.setText(getString(R.string.send_action));
	}

	private void setTitle() {
		mSubTitle.setText(mAccount.getEmail());
		switch (mAction) {
		case REPLY: {
			mTitle.setText(getResources().getString(
					R.string.compose_title_reply));
			break;
		}
		case REPLY_ALL: {
			mTitle.setText(getResources().getString(
					R.string.compose_title_reply_all));
			break;
		}
		case FORWARD: {
			mTitle.setText(getResources().getString(
					R.string.compose_title_forward));
			break;
		}
		case EDIT_OUTBOX: {
			mTitle.setText(getResources().getString(
					R.string.special_mailbox_name_outbox));
			break;
		}
		case COMPOSE:
		default: {
			mTitle.setText(getResources().getString(
					R.string.compose_title_compose));
			break;
		}
		}
	}

	/**
	 * method name: initData function @Description: TODO Parameters and return
	 * values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-11-14 下午8:11:14 @Modified by：zhangjx
	 * @Description：
	 */
	private void initData() {
	    mHiddenAttachments = new ArrayList<Attachment>();
		mFontSizes = MailChat.getFontSizes();
		configWebView();
		messagingController = MessagingController.getInstance(getApplication());
	}

	/**
	 * method name: parserArgument function @Description: TODO Parameters and
	 * return values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-11-14 下午4:31:47 @Modified by：zhangjx
	 * @Description：
	 */
	private void parserArgument(Bundle savedInstanceState) {
		final Intent intent = getIntent();
		toEmail = intent.getStringExtra(EXTRA_TO_EMAIL);
		mMessageReference = intent.getParcelableExtra(EXTRA_MESSAGE_REFERENCE);
		mSourceMessageBody = intent.getStringExtra(EXTRA_MESSAGE_BODY);
		if (MailChat.DEBUG && mSourceMessageBody != null) {
			Log.d(MailChat.LOG_TAG,
					"Composing message with explicitly specified message body.");
		}
		final String accountUuid = (mMessageReference != null) ? mMessageReference.accountUuid
				: intent.getStringExtra(EXTRA_ACCOUNT);
		mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
		if (mAccount == null) {
			mAccount = Preferences.getPreferences(this).getDefaultAccount();
		}
		
		mMessageSubject = intent.getStringExtra(EXTRA_MESSAGE_SUBJECT);
		mMessageText = intent.getStringExtra(EXTRA_MESSAGE_TEXT);
		mAttachmentUri = intent.getParcelableExtra(EXTRA_ATTACHMENT_URI);
		mAttachmentName = intent.getStringExtra(EXTRA_ATTACHMENT_NAME);
		mAttachmentPosition = intent.getStringExtra(EXTRA_ATTACHMENT_POSITION);
		
		/*
		 * We set this to invisible by default. Other methods will turn it back
		 * on if it's needed.
		 */
		showOrHideQuotedText(QuotedTextMode.NONE);
		if (savedInstanceState != null) {
			/*
			 * This data gets used in onCreate, so grab it here instead of
			 * onRestoreInstanceState
			 */
			mSourceMessageProcessed = savedInstanceState.getBoolean(
					STATE_KEY_SOURCE_MESSAGE_PROCED, false);
		}
		if (initFromIntent(intent)) {
			mAction = Action.COMPOSE;
			mDraftNeedsSaving = true;
		} else {
			String action = intent.getAction();
			if (ACTION_COMPOSE.equals(action)) {
				// 写信
				mAction = Action.COMPOSE;
			} else if (ACTION_REPLY.equals(action)) {
				// 回复
				mAction = Action.REPLY;
			} else if (ACTION_REPLY_ALL.equals(action)) {
				// 回复全部
				mAction = Action.REPLY_ALL;
			} else if (ACTION_FORWARD.equals(action)) {
				// 转发
				mAction = Action.FORWARD;
			} else if (ACTION_FORWARD_MESSAGE.equals(action)) {
				// 转发消息
				mAction = Action.FORWARD_MESSAGE;
			} else if (ACTION_EDIT_DRAFT.equals(action)) {
				// 草稿
				mAction = Action.EDIT_DRAFT;
			} else if (ACTION_EDIT_OUTBOX.equals(action)) {
				// 发件箱
				mAction = Action.EDIT_OUTBOX;
			} else if (ACTION_EDIT_SENT.equals(action)) {
				// 再次编辑
				mAction = Action.EDIT_SENT;
			} else {
				// This shouldn't happen
				Log.w(MailChat.LOG_TAG,
						"MessageCompose was started with an unsupported action");
				mAction = Action.COMPOSE;
			}
		}
//        if (mAccount.isSignatureBeforeQuotedText()) {
//            mSignatureView = upperSignature;
//            lowerSignature.setVisibility(View.GONE);
//        } else {
//            mSignatureView = lowerSignature;
//            upperSignature.setVisibility(View.GONE);
//        }
        //是否使用签名
//        if (!mIdentity.getSignatureUse()) {
//            mSignatureView.setVisibility(View.GONE);
//        }
	}

	private void setEditView() {
		if (!StringUtil.isEmpty(toEmail)) {
			mToView.addContacts(toEmail);
		}
		
		if (mMessageText != null) {
			mMessageContentView.setText(mMessageText);
		}
		
		if (mAttachmentUri != null) {
			if (mAttachmentName == null) {
				addAttachment(mAttachmentUri);
			} else {
				addAttachment(mAttachmentUri,
						Attachment.LoadingState.URI_ONLY,
						null,
						mAttachmentName,
						0,
						null,
						null,
						null,
						null);
			}
		}
		
		if (mAction == Action.FORWARD_MESSAGE) {
		    if (mMessageSubject != null) {
		        mSubjectView.setText(mMessageSubject);
		    } else {
    			String subject = getText(R.string.forward_chat_message_subject).toString();
    			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
    			subject = String.format(subject,
    					mAccount.getName(),
    					sdf.format(new Date(System.currentTimeMillis())));
    			mSubjectView.setText(subject);
		    }
		}
		
		if (mIdentity == null) {
			// Account.Identity(description=初始身份标识, name=张,
			// email=z503241989@163.com, replyTo=null, signature=--
			// Sent from my Android device with K-9 Mail. Please excuse my
			// brevity
			mIdentity = mAccount.getIdentity(0);
		}
		mReadReceipt = mAccount.isMessageReadReceiptAlways();
		mQuoteStyle = mAccount.getQuoteStyle();

		if (!mSourceMessageProcessed) {
			updateSignature();
			if (mAction == Action.REPLY
					|| mAction == Action.REPLY_ALL
					|| mAction == Action.FORWARD
					|| mAction == Action.EDIT_DRAFT
					|| mAction == Action.EDIT_OUTBOX
					|| mAction == Action.EDIT_SENT) {
				/*
				 * If we need to load the message we add ourself as a message
				 * listener here so we can kick it off. Normally we add in
				 * onResume but we don't want to reload the message every time
				 * the activity is resumed. There is no harm in adding twice.
				 */
				messagingController.addListener(mListener);

				final Account account = Preferences.getPreferences(this)
						.getAccount(mMessageReference.accountUuid);
				final String folderName = mMessageReference.folderName;
				final String sourceMessageUid = mMessageReference.uid;
				messagingController.loadMessageForView(account, folderName,
						sourceMessageUid, null);
			}
			if (mAction != Action.EDIT_DRAFT
					&& mAction != Action.EDIT_OUTBOX
					&& mAction != Action.EDIT_SENT) {
				mBccView.addContacts(mAccount.getAlwaysBcc());
			}

			if (mAction == Action.REPLY || mAction == Action.REPLY_ALL) {
				mMessageReference.flag = Flag.ANSWERED;
			}

			if (mAction == Action.REPLY
					|| mAction == Action.REPLY_ALL
					|| mAction == Action.EDIT_DRAFT
					|| mAction == Action.EDIT_OUTBOX
					|| mAction == Action.EDIT_SENT) {
				// change focus to message body.
				mMessageContentView.requestFocus();
			} else {
				// Explicitly set focus to "To:" input field (see issue 2998)
				mToView.requestFocus();
			}

			if (mAction == Action.FORWARD) {
				mMessageReference.flag = Flag.FORWARDED;
			}
			initializeCrypto();

			mOpenPgpProvider = mAccount.getOpenPgpProvider();
			if (mOpenPgpProvider != null) {
				// New OpenPGP Provider API

				// bind to service
				mOpenPgpServiceConnection = new OpenPgpServiceConnection(this,
						mOpenPgpProvider);
				mOpenPgpServiceConnection.bindToService();

				updateMessageFormat();
				// TODO: currently disabled for new openpgp providers (see
				// AccountSettings)
				// mAutoEncrypt = mAccount.isCryptoAutoEncrypt();
			} else {
				// TODO隐藏加密布局
			}
		}
	}

	private void updateMessageFormat() {
		// 仅在邮件格式无赋值时更新格式
		// Modified by LL
		// BEGIN
		if (mMessageFormat != null) {
			return;
		}
		// END
		
		MessageFormat origMessageFormat = mAccount.getMessageFormat();
		SimpleMessageFormat messageFormat;
		if (origMessageFormat == MessageFormat.TEXT) {
			// The user wants to send text/plain messages. We don't override
			// that choice under
			// any circumstances.
			messageFormat = SimpleMessageFormat.TEXT;
		} else if (mForcePlainText && includeQuotedText()) {
			// Right now we send a text/plain-only message when the quoted text
			// was edited, no
			// matter what the user selected for the message format.
			messageFormat = SimpleMessageFormat.TEXT;
		} else if (origMessageFormat == MessageFormat.AUTO) {
			if (mAction == Action.COMPOSE
					|| mAction == Action.FORWARD_MESSAGE
					|| mQuotedTextFormat == SimpleMessageFormat.TEXT
					|| !includeQuotedText()) {
				// If the message format is set to "AUTO" we use text/plain
				// whenever possible. That
				// is, when composing new messages and replying to or forwarding
				// text/plain
				// messages.
				messageFormat = SimpleMessageFormat.TEXT;
			} else {
				messageFormat = SimpleMessageFormat.HTML;
			}
		} else {
			// In all other cases use HTML
			messageFormat = SimpleMessageFormat.HTML;
		}

		setMessageFormat(messageFormat);
	}

	private boolean includeQuotedText() {
		return (mQuotedTextMode == QuotedTextMode.SHOW);
	}

	private void setMessageFormat(SimpleMessageFormat format) {
		// This method will later be used to enable/disable the rich text
		// editing mode.

		mMessageFormat = format;
	}

	// Regexes to check for signature.
	private static final Pattern DASH_SIGNATURE_PLAIN = Pattern.compile(
			"\r\n-- \r\n.*", Pattern.DOTALL);
	private static final Pattern DASH_SIGNATURE_HTML = Pattern.compile(
			"(<br( /)?>|\r?\n)-- <br( /)?>", Pattern.CASE_INSENSITIVE);
	private static final Pattern BLOCKQUOTE_START = Pattern.compile(
			"<blockquote", Pattern.CASE_INSENSITIVE);
	private static final Pattern BLOCKQUOTE_END = Pattern.compile(
			"</blockquote>", Pattern.CASE_INSENSITIVE);

	/**
	 * Regular expression to remove the first localized "Re:" prefix in
	 * subjects.
	 * 
	 * Currently: - "Aw:" (german: abbreviation for "Antwort")
	 */
	private static final Pattern PREFIX = Pattern.compile("^AW[:\\s]\\s*",
			Pattern.CASE_INSENSITIVE);

	// Version identifier for "new style" identity. ! is an impossible value in
	// base64 encoding, so we
	// use that to determine which version we're in.
	private static final String IDENTITY_VERSION_1 = "!";

	private static final String LOADER_ARG_ATTACHMENT = "attachment";

	private static final String FRAGMENT_WAITING_FOR_ATTACHMENT = "waitingForAttachment";

	/**
	 * Build and populate the UI with the quoted message.
	 * 
	 * @param showQuotedText
	 *            {@code true} if the quoted text should be shown, {@code false}
	 *            otherwise.
	 * 
	 * @throws MessagingException
	 */
	private void populateUIWithQuotedMessage(boolean showQuotedText)
			throws MessagingException {
		MessageFormat origMessageFormat = mAccount.getMessageFormat();
		if (mForcePlainText || origMessageFormat == MessageFormat.TEXT) {
			// Use plain text for the quoted message
			mQuotedTextFormat = SimpleMessageFormat.TEXT;
		} else if (origMessageFormat == MessageFormat.AUTO) {
			// Figure out which message format to use for the quoted text by
			// looking if the source
			// message contains a text/html part. If it does, we use that.
			mQuotedTextFormat = (MimeUtility.findFirstPartByMimeType(
					mSourceMessage, "text/html") == null) ? SimpleMessageFormat.TEXT
					: SimpleMessageFormat.HTML;
		} else {
			mQuotedTextFormat = SimpleMessageFormat.HTML;
		}

		// TODO -- I am assuming that mSourceMessageBody will always be a text
		// part. Is this a safe assumption?

		// Handle the original message in the reply
		// If we already have mSourceMessageBody, use that. It's pre-populated
		// if we've got crypto going on.
		String content = (mSourceMessageBody != null) ? mSourceMessageBody
				: getBodyTextFromMessage(mSourceMessage, mQuotedTextFormat);

		if (mQuotedTextFormat == SimpleMessageFormat.HTML) {
			// Strip signature.
			// closing tags such as </div>, </span>, </table>, </pre> will be
			// cut off.
			if (mAccount.isStripSignature()
					&& (mAction == Action.REPLY || mAction == Action.REPLY_ALL)) {
				Matcher dashSignatureHtml = DASH_SIGNATURE_HTML
						.matcher(content);
				if (dashSignatureHtml.find()) {
					Matcher blockquoteStart = BLOCKQUOTE_START.matcher(content);
					Matcher blockquoteEnd = BLOCKQUOTE_END.matcher(content);
					List<Integer> start = new ArrayList<Integer>();
					List<Integer> end = new ArrayList<Integer>();

					while (blockquoteStart.find()) {
						start.add(blockquoteStart.start());
					}
					while (blockquoteEnd.find()) {
						end.add(blockquoteEnd.start());
					}
					if (start.size() != end.size()) {
						Log.d(MailChat.LOG_TAG, "There are " + start.size()
								+ " <blockquote> tags, but " + end.size()
								+ " </blockquote> tags. Refusing to strip.");
					} else if (start.size() > 0) {
						// Ignore quoted signatures in blockquotes.
						dashSignatureHtml.region(0, start.get(0));
						if (dashSignatureHtml.find()) {
							// before first <blockquote>.
							content = content.substring(0,
									dashSignatureHtml.start());
						} else {
							for (int i = 0; i < start.size() - 1; i++) {
								// within blockquotes.
								if (end.get(i) < start.get(i + 1)) {
									dashSignatureHtml.region(end.get(i),
											start.get(i + 1));
									if (dashSignatureHtml.find()) {
										content = content.substring(0,
												dashSignatureHtml.start());
										break;
									}
								}
							}
							if (end.get(end.size() - 1) < content.length()) {
								// after last </blockquote>.
								dashSignatureHtml.region(
										end.get(end.size() - 1),
										content.length());
								if (dashSignatureHtml.find()) {
									content = content.substring(0,
											dashSignatureHtml.start());
								}
							}
						}
					} else {
						// No blockquotes found.
						content = content.substring(0,
								dashSignatureHtml.start());
					}
				}

				// Fix the stripping off of closing tags if a signature was
				// stripped,
				// as well as clean up the HTML of the quoted message.
				HtmlCleaner cleaner = new HtmlCleaner();
				CleanerProperties properties = cleaner.getProperties();

				// see http://htmlcleaner.sourceforge.net/parameters.php for
				// descriptions
				properties.setNamespacesAware(false);
				properties.setAdvancedXmlEscape(false);
				properties.setOmitXmlDeclaration(true);
				properties.setOmitDoctypeDeclaration(false);
				properties.setTranslateSpecialEntities(false);
				properties.setRecognizeUnicodeChars(false);

				TagNode node = cleaner.clean(content);
				SimpleHtmlSerializer htmlSerialized = new SimpleHtmlSerializer(
						properties);
				try {
					content = htmlSerialized.getAsString(node, "UTF8");
				} catch (StackOverflowError e) {
				    Log.e(MailChat.LOG_TAG, "SOF while cleaning quoted message.", e);
				} catch (Exception e) {
				    // Can't imagine this happening.
				    Log.e(MailChat.LOG_TAG, "Problem cleaning quoted message.", e);
				}
			}

			// Add the HTML reply header to the top of the content.
			mQuotedHtmlContent = quoteOriginalHtmlMessage(mSourceMessage,
					content, mQuoteStyle);

			// Load the message with the reply header.
			mQuotedHTML.setText(mQuotedHtmlContent.getQuotedContent());
			// TODO: Also strip the signature from the text/plain part
			editedOriginalContent.setVisibility(View.VISIBLE);
			editedOriginalContent.setText(quoteOriginalTextMessage(
					mSourceMessage,
					getBodyTextFromMessage(mSourceMessage,
							SimpleMessageFormat.TEXT), mQuoteStyle));

		} else if (mQuotedTextFormat == SimpleMessageFormat.TEXT) {
			if (mAccount.isStripSignature()
					&& (mAction == Action.REPLY || mAction == Action.REPLY_ALL)) {
				if (DASH_SIGNATURE_PLAIN.matcher(content).find()) {
					content = DASH_SIGNATURE_PLAIN.matcher(content)
							.replaceFirst("\r\n");
				}
			}
			editedOriginalContent.setVisibility(View.VISIBLE);
			editedOriginalContent.setText(quoteOriginalTextMessage(
					mSourceMessage, content, mQuoteStyle));
		}
		// 显示或者隐藏引用消息
		if (showQuotedText) {
			showOrHideQuotedText(QuotedTextMode.SHOW);
		} else {
			showOrHideQuotedText(QuotedTextMode.HIDE);
		}
	}

	/**
	 * Add quoting markup to a text message.
	 * 
	 * @param originalMessage
	 *            Metadata for message being quoted.
	 * @param messageBody
	 *            Text of the message to be quoted.
	 * @param quoteStyle
	 *            Style of quoting.
	 * @return Quoted text.
	 * @throws MessagingException
	 */
	private String quoteOriginalTextMessage(final Message originalMessage,
			final String messageBody, final QuoteStyle quoteStyle)
			throws MessagingException {
		String body = messageBody == null ? "" : messageBody;
		String sentDate = getSentDateText(originalMessage);
		if (quoteStyle == QuoteStyle.PREFIX) {
			StringBuilder quotedText = new StringBuilder(body.length()
					+ QUOTE_BUFFER_LENGTH);
			if (sentDate.length() != 0) {
				quotedText
						.append(String
								.format(getString(R.string.message_compose_reply_header_fmt_with_date)
										+ "\r\n", sentDate, Address
										.toString(originalMessage.getFrom())));
			} else {
				quotedText.append(String.format(
						getString(R.string.message_compose_reply_header_fmt)
								+ "\r\n",
						Address.toString(originalMessage.getFrom())));
			}

			final String prefix = mAccount.getQuotePrefix();
			final String wrappedText = Utility.wrap(body, REPLY_WRAP_LINE_WIDTH
					- prefix.length());

			// "$" and "\" in the quote prefix have to be escaped for
			// the replaceAll() invocation.
			final String escapedPrefix = prefix.replaceAll("(\\\\|\\$)",
					"\\\\$1");
			quotedText.append(wrappedText.replaceAll("(?m)^", escapedPrefix));

			return quotedText.toString().replaceAll("\\\r", "");
		} else if (quoteStyle == QuoteStyle.HEADER) {
			StringBuilder quotedText = new StringBuilder(body.length()
					+ QUOTE_BUFFER_LENGTH);
			quotedText.append("\r\n");
			quotedText.append(
					getString(R.string.message_compose_quote_header_separator))
					.append("\r\n");
			if (originalMessage.getFrom() != null
					&& Address.toString(originalMessage.getFrom()).length() != 0) {
				quotedText
						.append(getString(R.string.message_compose_quote_header_from))
						.append(" ")
						.append(Address.toString(originalMessage.getFrom()))
						.append("\r\n");
			}
			if (sentDate.length() != 0) {
				quotedText
						.append(getString(R.string.message_compose_quote_header_send_date))
						.append(" ").append(sentDate).append("\r\n");
			}
			if (originalMessage.getRecipients(RecipientType.TO) != null
					&& originalMessage.getRecipients(RecipientType.TO).length != 0) {
				quotedText
						.append(getString(R.string.message_compose_quote_header_to))
						.append(" ")
						.append(Address.toString(originalMessage
								.getRecipients(RecipientType.TO)))
						.append("\r\n");
			}
			if (originalMessage.getRecipients(RecipientType.CC) != null
					&& originalMessage.getRecipients(RecipientType.CC).length != 0) {
				quotedText
						.append(getString(R.string.message_compose_quote_header_cc))
						.append(" ")
						.append(Address.toString(originalMessage
								.getRecipients(RecipientType.CC)))
						.append("\r\n");
			}
			if (originalMessage.getSubject() != null) {
				quotedText
						.append(getString(R.string.message_compose_quote_header_subject))
						.append(" ").append(originalMessage.getSubject())
						.append("\r\n");
			}
			quotedText.append("\r\n");

			quotedText.append(body);

			return quotedText.toString();
		} else {
			// Shouldn't ever happen.
			return body;
		}
	}

	/**
	 * Extract the date from a message and convert it into a locale-specific
	 * date string suitable for use in a header for a quoted message.
	 * 
	 * @param message
	 * @return A string with the formatted date/time
	 */
	private String getSentDateText(Message message) {
		try {
			// final int dateStyle = DateFormat.LONG;
			// final int timeStyle = DateFormat.LONG;
			Date date = message.getSentDate();
			Locale locale = getResources().getConfiguration().locale;
			// return DateFormat.getDateTimeInstance(dateStyle, timeStyle,
			// locale)
			// .format(date);
			return TimeUtils.DataFormatCHINESYYMMDDHHMM.format(date);
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Add quoting markup to a HTML message.
	 * 
	 * @param originalMessage
	 *            Metadata for message being quoted.
	 * @param messageBody
	 *            Text of the message to be quoted.
	 * @param quoteStyle
	 *            Style of quoting.
	 * @return Modified insertable message.
	 * @throws MessagingException
	 */
	private InsertableHtmlContent quoteOriginalHtmlMessage(
			final Message originalMessage, final String messageBody,
			final QuoteStyle quoteStyle) throws MessagingException {
		InsertableHtmlContent insertable = findInsertionPoints(messageBody);

		String sentDate = getSentDateText(originalMessage);
		if (quoteStyle == QuoteStyle.PREFIX) {
			StringBuilder header = new StringBuilder(QUOTE_BUFFER_LENGTH);
			header.append("<div class=\"gmail_quote\">");
			if (sentDate.length() != 0) {
				header.append(HtmlConverter.textToHtmlFragment(String
						.format(getString(R.string.message_compose_reply_header_fmt_with_date),
								sentDate,
								Address.toString(originalMessage.getFrom()))));
			} else {
				header.append(HtmlConverter.textToHtmlFragment(String.format(
						getString(R.string.message_compose_reply_header_fmt),
						Address.toString(originalMessage.getFrom()))));
			}
			header.append("<blockquote class=\"gmail_quote\" "
					+ "style=\"margin: 0pt 0pt 0pt 0.8ex; border-left: 1px solid rgb(204, 204, 204); padding-left: 1ex;\">\r\n");

			String footer = "</blockquote></div>";

			insertable.insertIntoQuotedHeader(header.toString());
			insertable.insertIntoQuotedFooter(footer);
		} else if (quoteStyle == QuoteStyle.HEADER) {

			StringBuilder header = new StringBuilder();
			header.append("<div style='font-size:10.0pt;font-family:\"Tahoma\",\"sans-serif\";padding:3.0pt 0in 0in 0in'>\r\n");
			header.append("<hr style='border:none;border-top:solid #E1E1E1 1.0pt'>\r\n"); // This
																							// gets
																							// converted
																							// into
																							// a
																							// horizontal
																							// line
																							// during
																							// html
																							// to
																							// text
																							// conversion.
			if (originalMessage.getFrom() != null
					&& Address.toString(originalMessage.getFrom()).length() != 0) {
				header.append("<b>")
						.append(getString(R.string.message_compose_quote_header_from))
						.append("</b> ")
						.append(HtmlConverter.textToHtmlFragment(Address
								.toString(originalMessage.getFrom())))
						.append("<br>\r\n");
			}
			if (sentDate.length() != 0) {
				header.append("<b>")
						.append(getString(R.string.message_compose_quote_header_send_date))
						.append("</b> ").append(sentDate).append("<br>\r\n");
			}
			if (originalMessage.getRecipients(RecipientType.TO) != null
					&& originalMessage.getRecipients(RecipientType.TO).length != 0) {
				header.append("<b>")
						.append(getString(R.string.message_compose_quote_header_to))
						.append("</b> ")
						.append(HtmlConverter.textToHtmlFragment(Address
								.toString(originalMessage
										.getRecipients(RecipientType.TO))))
						.append("<br>\r\n");
			}
			if (originalMessage.getRecipients(RecipientType.CC) != null
					&& originalMessage.getRecipients(RecipientType.CC).length != 0) {
				header.append("<b>")
						.append(getString(R.string.message_compose_quote_header_cc))
						.append("</b> ")
						.append(HtmlConverter.textToHtmlFragment(Address
								.toString(originalMessage
										.getRecipients(RecipientType.CC))))
						.append("<br>\r\n");
			}
			if (originalMessage.getSubject() != null) {
				header.append("<b>")
						.append(getString(R.string.message_compose_quote_header_subject))
						.append("</b> ")
						.append(HtmlConverter
								.textToHtmlFragment(originalMessage
										.getSubject())).append("<br>\r\n");
			}
			header.append("</div>\r\n");
			header.append("<br>\r\n");

			insertable.insertIntoQuotedHeader(header.toString());
		}

		return insertable;
	}

	/**
	 * <p>
	 * Find the start and end positions of the HTML in the string. This should
	 * be the very top and bottom of the displayable message. It returns a
	 * {@link InsertableHtmlContent}, which contains both the insertion points
	 * and potentially modified HTML. The modified HTML should be used in place
	 * of the HTML in the original message.
	 * </p>
	 * 
	 * <p>
	 * This method loosely mimics the HTML forward/reply behavior of BlackBerry
	 * OS 4.5/BIS 2.5, which in turn mimics Outlook 2003 (as best I can tell).
	 * </p>
	 * 
	 * @param content
	 *            Content to examine for HTML insertion points
	 * @return Insertion points and HTML to use for insertion.
	 */
	private InsertableHtmlContent findInsertionPoints(final String content) {
		InsertableHtmlContent insertable = new InsertableHtmlContent();

		// If there is no content, don't bother doing any of the regex dancing.
		if (content == null || content.equals("")) {
			return insertable;
		}

		// Search for opening tags.
		boolean hasHtmlTag = false;
		boolean hasHeadTag = false;
		boolean hasBodyTag = false;
		// First see if we have an opening HTML tag. If we don't find one, we'll
		// add one later.
		Matcher htmlMatcher = FIND_INSERTION_POINT_HTML.matcher(content);
		if (htmlMatcher.matches()) {
			hasHtmlTag = true;
		}
		// Look for a HEAD tag. If we're missing a BODY tag, we'll use the close
		// of the HEAD to start our content.
		Matcher headMatcher = FIND_INSERTION_POINT_HEAD.matcher(content);
		if (headMatcher.matches()) {
			hasHeadTag = true;
		}
		// Look for a BODY tag. This is the ideal place for us to start our
		// content.
		Matcher bodyMatcher = FIND_INSERTION_POINT_BODY.matcher(content);
		if (bodyMatcher.matches()) {
			hasBodyTag = true;
		}

		if (MailChat.DEBUG) {
			Log.d(MailChat.LOG_TAG, "Open: hasHtmlTag:" + hasHtmlTag
					+ " hasHeadTag:" + hasHeadTag + " hasBodyTag:" + hasBodyTag);
		}

		// Given our inspections, let's figure out where to start our content.
		// This is the ideal case -- there's a BODY tag and we insert ourselves
		// just after it.
		if (hasBodyTag) {
			insertable.setQuotedContent(new StringBuilder(content));
			insertable.setHeaderInsertionPoint(bodyMatcher
					.end(FIND_INSERTION_POINT_FIRST_GROUP));
		} else if (hasHeadTag) {
			// Now search for a HEAD tag. We can insert after there.

			// If BlackBerry sees a HEAD tag, it inserts right after that, so
			// long as there is no BODY tag. It doesn't
			// try to add BODY, either. Right or wrong, it seems to work fine.
			insertable.setQuotedContent(new StringBuilder(content));
			insertable.setHeaderInsertionPoint(headMatcher
					.end(FIND_INSERTION_POINT_FIRST_GROUP));
		} else if (hasHtmlTag) {
			// Lastly, check for an HTML tag.
			// In this case, it will add a HEAD, but no BODY.
			StringBuilder newContent = new StringBuilder(content);
			// Insert the HEAD content just after the HTML tag.
			newContent.insert(
					htmlMatcher.end(FIND_INSERTION_POINT_FIRST_GROUP),
					FIND_INSERTION_POINT_HEAD_CONTENT);
			insertable.setQuotedContent(newContent);
			// The new insertion point is the end of the HTML tag, plus the
			// length of the HEAD content.
			insertable.setHeaderInsertionPoint(htmlMatcher
					.end(FIND_INSERTION_POINT_FIRST_GROUP)
					+ FIND_INSERTION_POINT_HEAD_CONTENT.length());
		} else {
			// If we have none of the above, we probably have a fragment of
			// HTML. Yahoo! and Gmail both do this.
			// Again, we add a HEAD, but not BODY.
			StringBuilder newContent = new StringBuilder(content);
			// Add the HTML and HEAD tags.
			newContent.insert(FIND_INSERTION_POINT_START_OF_STRING,
					FIND_INSERTION_POINT_HEAD_CONTENT);
			newContent.insert(FIND_INSERTION_POINT_START_OF_STRING,
					FIND_INSERTION_POINT_HTML_CONTENT);
			// Append the </HTML> tag.
			newContent.append(FIND_INSERTION_POINT_HTML_END_CONTENT);
			insertable.setQuotedContent(newContent);
			insertable
					.setHeaderInsertionPoint(FIND_INSERTION_POINT_HTML_CONTENT
							.length()
							+ FIND_INSERTION_POINT_HEAD_CONTENT.length());
		}

		// Search for closing tags. We have to do this after we deal with
		// opening tags since it may
		// have modified the message.
		boolean hasHtmlEndTag = false;
		boolean hasBodyEndTag = false;
		// First see if we have an opening HTML tag. If we don't find one, we'll
		// add one later.
		Matcher htmlEndMatcher = FIND_INSERTION_POINT_HTML_END
				.matcher(insertable.getQuotedContent());
		if (htmlEndMatcher.matches()) {
			hasHtmlEndTag = true;
		}
		// Look for a BODY tag. This is the ideal place for us to place our
		// footer.
		Matcher bodyEndMatcher = FIND_INSERTION_POINT_BODY_END
				.matcher(insertable.getQuotedContent());
		if (bodyEndMatcher.matches()) {
			hasBodyEndTag = true;
		}

		if (MailChat.DEBUG) {
			Log.d(MailChat.LOG_TAG, "Close: hasHtmlEndTag:" + hasHtmlEndTag
					+ " hasBodyEndTag:" + hasBodyEndTag);
		}

		// Now figure out where to put our footer.
		// This is the ideal case -- there's a BODY tag and we insert ourselves
		// just before it.
		if (hasBodyEndTag) {
			insertable.setFooterInsertionPoint(bodyEndMatcher
					.start(FIND_INSERTION_POINT_FIRST_GROUP));
		} else if (hasHtmlEndTag) {
			// Check for an HTML tag. Add ourselves just before it.
			insertable.setFooterInsertionPoint(htmlEndMatcher
					.start(FIND_INSERTION_POINT_FIRST_GROUP));
		} else {
			// If we have none of the above, we probably have a fragment of
			// HTML.
			// Set our footer insertion point as the end of the string.
			insertable.setFooterInsertionPoint(insertable.getQuotedContent()
					.length());
		}

		return insertable;
	}

	/**
	 * Fetch the body text from a message in the desired message format. This
	 * method handles conversions between formats (html to text and vice versa)
	 * if necessary.
	 * 
	 * @param message
	 *            Message to analyze for body part.
	 * @param format
	 *            Desired format.
	 * @return Text in desired format.
	 * @throws MessagingException
	 */
	private String getBodyTextFromMessage(final Message message,
			final SimpleMessageFormat format) throws MessagingException {
		Part part;
		if (format == SimpleMessageFormat.HTML) {
			// HTML takes precedence, then text.
			part = MimeUtility.findFirstPartByMimeType(message, "text/html");
			if (part != null) {
				if (MailChat.DEBUG) {
					Log.d(MailChat.LOG_TAG,
							"getBodyTextFromMessage: HTML requested, HTML found.");
				}
				return MimeUtility.getTextFromPart(part);
			}

			part = MimeUtility.findFirstPartByMimeType(message, "text/plain");
			if (part != null) {
				if (MailChat.DEBUG) {
					Log.d(MailChat.LOG_TAG,
							"getBodyTextFromMessage: HTML requested, text found.");
				}
				return HtmlConverter.textToHtml(MimeUtility
						.getTextFromPart(part));
			}
		} else if (format == SimpleMessageFormat.TEXT) {
			// Text takes precedence, then html.
			part = MimeUtility.findFirstPartByMimeType(message, "text/plain");
			if (part != null) {
				if (MailChat.DEBUG) {
					Log.d(MailChat.LOG_TAG,
							"getBodyTextFromMessage: Text requested, text found.");
				}
				return MimeUtility.getTextFromPart(part);
			}

			part = MimeUtility.findFirstPartByMimeType(message, "text/html");
			if (part != null) {
				if (MailChat.DEBUG) {
					Log.d(MailChat.LOG_TAG,
							"getBodyTextFromMessage: Text requested, HTML found.");
				}
				return HtmlConverter.htmlToText(MimeUtility
						.getTextFromPart(part));
			}
		}

		// If we had nothing interesting, return an empty string.
		return "";
	}

	private void initializeCrypto() {
		if (mPgpData != null) {
			return;
		}
		mPgpData = new PgpData();
	}

	/**
	 * Handle external intents that trigger the message compose activity.
	 * 
	 * <p>
	 * Supported external intents:
	 * <ul>
	 * <li>{@link Intent#ACTION_VIEW}</li>
	 * <li>{@link Intent#ACTION_SENDTO}</li>
	 * <li>{@link Intent#ACTION_SEND}</li>
	 * <li>{@link Intent#ACTION_SEND_MULTIPLE}</li>
	 * </ul>
	 * </p>
	 * 
	 * @param intent
	 *            The (external) intent that started the activity.
	 * 
	 * @return {@code true}, if this activity was started by an external intent.
	 *         {@code false}, otherwise.
	 */
	private boolean initFromIntent(final Intent intent) {
		boolean startedByExternalIntent = false;
		final String action = intent.getAction();

		if (Intent.ACTION_VIEW.equals(action)
				|| Intent.ACTION_SENDTO.equals(action)) {
			/*
			 * Someone has clicked a mailto: link. The address is in the URI.
			 */
			if (intent.getData() != null) {
				Uri uri = intent.getData();
				if ("mailto".equals(uri.getScheme())) {
					// TODO 不晓得干啥的 initializeFromMailto(uri);
					initializeFromMailto(uri);
				}
			}

			/*
			 * Note: According to the documentation ACTION_VIEW and
			 * ACTION_SENDTO don't accept EXTRA_* parameters. And previously we
			 * didn't process these EXTRAs. But it looks like nobody bothers to
			 * read the official documentation and just copies wrong sample code
			 * that happens to work with the AOSP Email application. And because
			 * even big players get this wrong, we're now finally giving in and
			 * read the EXTRAs for those actions (below).
			 */
		}

		if (Intent.ACTION_SEND.equals(action)
				|| Intent.ACTION_SEND_MULTIPLE.equals(action)
				|| Intent.ACTION_SENDTO.equals(action)
				|| Intent.ACTION_VIEW.equals(action)) {
			startedByExternalIntent = true;

			/*
			 * Note: Here we allow a slight deviation from the documented
			 * behavior. EXTRA_TEXT is used as message body (if available)
			 * regardless of the MIME type of the intent. In addition one or
			 * multiple attachments can be added using EXTRA_STREAM.
			 */
			CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
			// Only use EXTRA_TEXT if the body hasn't already been set by the
			// mailto URI
			if (text != null && mMessageContentView.getText().length() == 0) {
				mMessageContentView.setText(text);
			}

			String type = intent.getType();
			if (Intent.ACTION_SEND.equals(action)) {
				Uri stream = (Uri) intent
						.getParcelableExtra(Intent.EXTRA_STREAM);
				if (stream != null) {
					// TODO 添加附件
					addAttachment(stream, type);
				}
			} else {
				List<Parcelable> list = intent
						.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
				if (list != null) {
					for (Parcelable parcelable : list) {
						Uri stream = (Uri) parcelable;
						if (stream != null) {
							// TODO 添加附件
							addAttachment(stream, type);
						}
					}
				}
			}

			String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
			// Only use EXTRA_SUBJECT if the subject hasn't already been set by
			// the mailto URI
			if (subject != null && mSubjectView.getText().length() == 0) {
				mSubjectView.setText(subject);
			}

			String[] extraEmail = intent
					.getStringArrayExtra(Intent.EXTRA_EMAIL);
			String[] extraCc = intent.getStringArrayExtra(Intent.EXTRA_CC);
			String[] extraBcc = intent.getStringArrayExtra(Intent.EXTRA_BCC);

			if (extraEmail != null) {
				// addRecipients(mToView, Arrays.asList(extraEmail));
				mToView.addContacts(extraEmail);
			}

			boolean ccOrBcc = false;
			if (extraCc != null) {
				ccOrBcc |= addRecipients(mCcView, extraCc);

			}

			if (extraBcc != null) {
				ccOrBcc |= addRecipients(mBccView, extraBcc);
			}

			if (ccOrBcc) {
				// Display CC and BCC text fields if CC or BCC recipients were
				// set by the intent.
				onAddCcBcc();
			}
		}

		return startedByExternalIntent;
	}

	/**
	 * When we are launched with an intent that includes a mailto: URI, we can
	 * actually gather quite a few of our message fields from it.
	 * 
	 * @param mailtoUri
	 *            The mailto: URI we use to initialize the message fields.
	 */
	private void initializeFromMailto(Uri mailtoUri) {
		String schemaSpecific = mailtoUri.getSchemeSpecificPart();
		int end = schemaSpecific.indexOf('?');
		if (end == -1) {
			end = schemaSpecific.length();
		}

		// Extract the recipient's email address from the mailto URI if there's
		// one.
		String recipient = Uri.decode(schemaSpecific.substring(0, end));
		/*
		 * mailto URIs are not hierarchical. So calling getQueryParameters()
		 * will throw an UnsupportedOperationException. We avoid this by
		 * creating a new hierarchical dummy Uri object with the query
		 * parameters of the original URI.
		 */
		CaseInsensitiveParamWrapper uri = new CaseInsensitiveParamWrapper(
				Uri.parse("foo://bar?" + mailtoUri.getEncodedQuery()));

		// Read additional recipients from the "to" parameter.
		List<String> to = uri.getQueryParameters("to");
		if (recipient.length() != 0) {
			to = new ArrayList<String>(to);
			to.add(0, recipient);
		}
		addRecipients(mToView, to);

		// Read carbon copy recipients from the "cc" parameter.
		boolean ccOrBcc = addRecipients(mCcView, uri.getQueryParameters("cc"));

		// Read blind carbon copy recipients from the "bcc" parameter.
		ccOrBcc |= addRecipients(mBccView, uri.getQueryParameters("bcc"));

		if (ccOrBcc) {
			// Display CC and BCC text fields if CC or BCC recipients were set
			// by the intent.
			onAddCcBcc();
		}

		// Read subject from the "subject" parameter.
		List<String> subject = uri.getQueryParameters("subject");
		if (!subject.isEmpty()) {
			mSubjectView.setText(subject.get(0));
		}

		// Read message body from the "body" parameter.
		List<String> body = uri.getQueryParameters("body");
		if (!body.isEmpty()) {
			mMessageContentView.setText(body.get(0));
		}
	}

	private static class CaseInsensitiveParamWrapper {
		private final Uri uri;
		private Set<String> mParamNames;

		public CaseInsensitiveParamWrapper(Uri uri) {
			this.uri = uri;
		}

		public List<String> getQueryParameters(String key) {
			final List<String> params = new ArrayList<String>();
			for (String paramName : uri.getQueryParameterNames()) {
				if (paramName.equalsIgnoreCase(key)) {
					params.addAll(uri.getQueryParameters(paramName));
				}
			}
			return params;
		}

	}

	private boolean addRecipients(ChoseAddressView view, String[] recipients) {
		if (view == null || recipients == null || recipients.length == 0) {
			return false;
		}
		view.addContacts(recipients);
		return true;
	}

	private boolean addRecipients(ChoseAddressView view, List<String> recipients) {
		if (view == null || recipients == null || recipients.isEmpty()) {
			return false;
		}
		view.addContacts(recipients);
		return true;
	}

	private void onAddCcBcc() {
		expandAddress(true);
	}

	private void initView() {
		mAddressAutoCompAdapter = new EmailAddressAutoCompletedAdapter(this);
		watcher = new ContentWatcher();
		mHandler = new MyToastHandler();
		mMessageContentView = (EditText) findViewById(R.id.message_content);
		mSignatureView= (EditText) findViewById(R.id.et_signature);
		mSubjectView = (EditText) findViewById(R.id.subject);
		mAttachmentsLayout = (GridLayout) findViewById(R.id.attachments);
		setAttachmentsLayoutColumnCount();
		mBtnPickFile = (RelativeLayout) findViewById(R.id.llMessageComposeBottomFile);
		mBtnPickFromAlubm = (RelativeLayout) findViewById(R.id.llMessageComposeBottomAlbum);
		mBtnPickFromCamera = (RelativeLayout) findViewById(R.id.llMessageComposeBottomTakePictures);
		mQuotedHTML = (MessageViewWebView) findViewById(R.id.webview_msgview_body);
		// mQuotedText = (EolConvertingEditText) findViewById(R.id.quoted_text);
		mQuotedTextBar = (LinearLayout) findViewById(R.id.linearlayout_cite_edit);
		mQuotedCheckBoxCite = (CheckBox) findViewById(R.id.checkbox_cite_original);
		tvEdit = (TextView) findViewById(R.id.tv_edit_original);
		editedOriginalContent = (EditText) findViewById(R.id.et_original_content);
		checkBoxUpload = (CheckBox) findViewById(R.id.checkbox_upload_attachment);
		layoutAttachment = (LinearLayout) findViewById(R.id.linear_layout_attachment);
		emptyView = findViewById(R.id.compose_empty_view);
	}

	private void initEvent() {
		mBtnPickFile.setOnClickListener(this);
		mBtnPickFromCamera.setOnClickListener(this);
		mBtnPickFromAlubm.setOnClickListener(this);
		mSubjectView.addTextChangedListener(watcher);
		mMessageContentView.addTextChangedListener(watcher);
		mMessageContentView.addTextChangedListener(watcher);
		mSubjectView.setOnClickListener(this);
		mMessageContentView.setOnClickListener(this);
		editedOriginalContent.setOnClickListener(this);
		mSubjectView.setOnFocusChangeListener(new OnFocusChangeListener() {// 设置标题栏焦点更改监听器

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							smoothScroll(v);
							checkBoxUpload.setChecked(false);
						}
					}
				});
		mMessageContentView
				.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							smoothScroll(v);
							checkBoxUpload.setChecked(false);
							// mAttachmentsLayout.setBackgroundResource(R.drawable.bg_edittext_p);
						} else {
							// mAttachmentsLayout.setBackgroundResource(R.drawable.bg_edittext_n);
						}
						mAttachmentsLayout.setPadding(0, 0, 0, 13);
					}
				});
		editedOriginalContent
				.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							smoothScroll(v);
							checkBoxUpload.setChecked(false);
						}
					}
				});
		//
		// toView.getAddressViewControl().getAutoCompleteTextView().setOnFocusChangeListener(new
		// OnFocusChangeListener() {
		//
		// @Override
		// public void onFocusChange(View v, boolean hasFocus) {
		// // TODO Auto-generated method stub
		// if (hasFocus) {
		// smoothScroll(v);
		// expandAddress(!isExtraAddressExpand);
		// }
		// }
		// });
		// 是否显示原邮件
		mQuotedCheckBoxCite
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (mQuotedTextBar.getVisibility() == View.VISIBLE) {
							if (isChecked) {
								mQuotedHTML.setVisibility(View.VISIBLE);
								tvEdit.setVisibility(View.VISIBLE);
								// emptyView.setVisibility(View.VISIBLE);
								
								// 存储"引用原邮件"状态
								// Modified by LL
								mQuotedTextMode = QuotedTextMode.SHOW;
							} else {
								mQuotedHTML.setVisibility(View.GONE);
								tvEdit.setVisibility(View.GONE);
								// emptyView.setVisibility(View.GONE);
								
								// 存储"引用原邮件"状态
								// Modified by LL
								mQuotedTextMode = QuotedTextMode.HIDE;
							}
						}
						mDraftNeedsSaving = true;
					}
				});

		checkBoxUpload
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							layoutAttachment.setVisibility(View.VISIBLE);
							// emptyView.setVisibility(View.VISIBLE);
						} else {
							layoutAttachment.setVisibility(View.GONE);
							// emptyView.setVisibility(View.GONE);
						}
					}
				});
		checkBoxUpload.setOnClickListener(this);
		tvEdit.setOnClickListener(this);
		mActionbarSure.setOnClickListener(this);
		 mSignatureView.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
                mDraftNeedsSaving = true;
                mSignatureChanged = true;
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	/**
	 * 移除内嵌图片
	 * 
	 * @Description:
	 * @param mAttachments
	 * @see:
	 * @since:
	 * @author: sunzhongquan
	 * @date:2014-5-4
	 */
	private void removeInlineAttach(Set<Attachment> mAttachments) {
		if (mAttachments != null && mAttachments.size() > 0) {
			ArrayList<Attachment> atts = new ArrayList<Attachment>();
			for (Attachment element : mAttachments) {
				atts.add(element);
			}
			if (atts.size() > 0) {
				mAttachments.removeAll(atts);
			}
		}
	}

	private void initAddressView() {
		scrollview = (ScrollView) findViewById(R.id.scrollview);
		mToView = (ChoseAddressView) findViewById(R.id.compose_address_to);
		mToView.init(ChoseAddressView.AddressType.TO,
				R.string.message_compose_to_hint, this,
				mAddressAutoCompAdapter, true);
		// toView.getNameView().setCompoundDrawablesWithIntrinsicBounds(0, 0,
		// R.drawable.icon_to_down, 0);
		// toView.getNameView().setCompoundDrawablePadding(10);
		mCcView = (ChoseAddressView) findViewById(R.id.compose_address_cc);
		mCcView.init(ChoseAddressView.AddressType.CC,
				R.string.message_compose_cc_hint, this,
				mAddressAutoCompAdapter, false);
		mCcView.setVisibility(View.GONE);
		mBccView = (ChoseAddressView) findViewById(R.id.compose_address_bcc);
		mBccView.init(ChoseAddressView.AddressType.BCC,
				R.string.message_compose_bcc_hint, this,
				mAddressAutoCompAdapter, false);
		mBccView.setVisibility(View.GONE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		messagingController.addListener(mListener);
		// 定时任务每60s执行
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				// 判断用户是否有输入。有就自动保存，没有就不自动保存。
				if (mDraftNeedsSaving) {
					saveIfNeeded();
				}
			}
		}, 1000 * 60, 1000 * 60);
		MobclickAgent.onPageStart("MailComposeActivity"); //统计页面
		MobclickAgent.onResume(this);
	}
	@Override
	public void onPause() {
		mTimer.cancel();
		mTimer = null;
		super.onPause();
		messagingController.removeListener(mListener);
		MobclickAgent.onPageEnd("MailComposeActivity"); //统计页面
		MobclickAgent.onPause(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.attachment_close:
			/*
			 * The view is the delete button, and we have previously set the tag
			 * of the delete button to the view that owns it. We don't use
			 * parent because the view is very complex and could change in the
			 * future.
			 */
			mAttachmentsLayout.removeView((View) v.getTag());
			mDraftNeedsSaving = true;
			break;
		case R.id.tv_sure:
			mPgpData.setEncryptionKeys(null);
			// 发送
			onSend();
			MobclickAgent.onEvent(this, "send_mail");
			break;
		case R.id.tv_edit_original:
			// 编辑引用消息
			// mQuotedTextBar.setVisibility(View.GONE);
			// mQuotedHTML.setVisibility(View.GONE);
			mForcePlainText = true;
			
			// 引用邮件被转为纯文本编辑，清除所有内联附件，
			// 并记录邮件格式为纯文本
			mMessageFormat = SimpleMessageFormat.TEXT;
			
			/*
			ArrayList<View> list = new ArrayList<View>();
			
			for (int i = 0, count = mAttachmentsLayout.getChildCount(); i < count; i++) {
				View view = mAttachmentsLayout.getChildAt(i);
				Attachment att = (Attachment) view.getTag();
				if (att.contentId != null) {
					list.add(view);
				}
			}
			
			for (View view : list) {
				mAttachmentsLayout.removeView(view);
			}
			*/
			mHiddenAttachments.clear();
			
			if (mMessageReference != null) { // shouldn't happen...
				// TODO - Should we check if mSourceMessageBody is already
				// present and bypass the MessagingController call?
				messagingController.addListener(mListener);
				final Account account = Preferences.getPreferences(this)
						.getAccount(mMessageReference.accountUuid);
				final String folderName = mMessageReference.folderName;
				final String sourceMessageUid = mMessageReference.uid;
				messagingController.loadMessageForView(account, folderName,
						sourceMessageUid, null);
			}
			
			mDraftNeedsSaving = true;
			break;
		case R.id.checkbox_upload_attachment:
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if (checkBoxUpload.isChecked()) {
				// imm.hideSoftInputFromWindow(composeTitle.getWindowToken(),
				// 0);
			} else {
				imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
			}
			break;
		case R.id.llMessageComposeBottomFile:
			// 选择文件
			// Intent i = new Intent(this, FilePickerActivity.class);
			// i.putExtra(FilePickerActivity.SELECTED_SIZE, mAllAttachmentSize);
		    //onAddAttachment();
		    Intent i = new Intent(this, FilePickerActivity.class);
		    i.putExtra(FilePickerActivity.SINGLE_CHOICE, false);
		    startActivityForResult(i, ACTIVITY_REQUEST_PICK_ATTACHMENT);
		    break;
		/* 相册 */
		case R.id.llMessageComposeBottomAlbum:
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
			intent.setType("image/*");
			startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
			break;
		/* 拍照 */
		case R.id.llMessageComposeBottomTakePictures:
			try {
				mPhotoFile = new File(MailChat.getInstance().getMailImageCacheDirectory(mAccount),
						FileUtil.getCameraFileName());
				Intent intentTakePicture = new Intent(
						MediaStore.ACTION_IMAGE_CAPTURE, null);
				intentTakePicture.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(mPhotoFile));
				intentTakePicture.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intentTakePicture, CAMERA_WITH_DATA);
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(
						MailChat.getInstance(),
						getString(R.string.message_compose_equipment_not_start),
						Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.message_content:
			checkBoxUpload.setChecked(false);
			break;
		case R.id.subject:
			checkBoxUpload.setChecked(false);
			break;
		case R.id.et_original_content:
			checkBoxUpload.setChecked(false);
			break;
		}
	}

	/**
	 * 
	 * method name: onNameClicked
	 * 
	 * @see cn.mailchat.view.AddressViewCallBack#onNameClicked(cn.mailchat.view.ChoseAddressView)
	 *      function@Description: TODO
	 * @History memory：
	 * @Date：2014-11-14 下午1:11:33 @Modified by：zhangjx
	 * @Description：点击地址栏时回调
	 */
	@Override
	public void onNameClicked(ChoseAddressView view) {
		switch (view.getType()) {
		case TO:
			expandAddress(!isExtraAddressExpand);
			break;
		case CC:
		case BCC:
			view.setActive(true);
			break;
		}
	}

	/**
	 * 
	 * method name: onAddClicked
	 * 
	 * @see cn.mailchat.view.AddressViewCallBack#onAddClicked(cn.mailchat.view.ChoseAddressView)
	 *      function@Description: TODO
	 * @History memory：
	 * @Date：2014-11-14 下午1:10:23 @Modified by：zhangjx
	 * @Description： 跳转联系人页面，点击添加按钮时回调
	 * @param view
	 *            被点击的按钮所在的控件
	 */
	@Override
	public void onAddClicked(ChoseAddressView view) {
		switch (view.getType()) {
		case TO:
			onAddRecipients(ACTIVITY_REQUEST_ADD_RECIPIENTS_TO);
			break;
		case CC:
			onAddRecipients(ACTIVITY_REQUEST_ADD_RECIPIENTS_CC);
			break;
		case BCC:
			onAddRecipients(ACTIVITY_REQUEST_ADD_RECIPIENTS_BCC);
			break;
		}
	}

	/**
	 * 
	 * method name: onAddRecipients function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param type
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-11-17 上午10:10:03 @Modified by：zhangjx
	 * @Description： 跳转到联系人界面
	 */
	private void onAddRecipients(int type) {
		contactMember = new ArrayList<CGroupMember>();
		contactMember.clear();
		if (type == ACTIVITY_REQUEST_ADD_RECIPIENTS_TO) {
			addressList = mToView.getAddresses();
		} else if (type == ACTIVITY_REQUEST_ADD_RECIPIENTS_CC) {
			addressList = mCcView.getAddresses();
		} else if (type == ACTIVITY_REQUEST_ADD_RECIPIENTS_BCC) {
			addressList = mBccView.getAddresses();
		}
		for (Address addres : addressList) {
			CGroupMember member = new CGroupMember();
			member.setEmail(addres.getAddress());
			member.setNickName(addres.getPersonal());
			contactMember.add(member);
		}
		// 添加联系人
		CreateChattingActivity.actionComposeMailView(MailComposeActivity.this,
				true, true, contactMember, mAccount, type);
	}

	/**
	 * 展开/收起抄送密送
	 * 
	 * @Description:
	 * @param expand
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2013-8-13
	 */
	private void expandAddress(boolean expand) {
		isExtraAddressExpand = expand;
		mCcView.setVisibility(expand ? View.VISIBLE : View.GONE);
		mBccView.setVisibility(expand ? View.VISIBLE : View.GONE);
		if (expand) {
			mBccView.setActive(true);
			mCcView.setActive(true);
		} else {
			mToView.setActive(true);
		}
		// mToView.getNameView().setCompoundDrawablesWithIntrinsicBounds(0, 0,
		// expand ? R.drawable.icon_to_up :
		// R.drawable.icon_to_down, 0);
	}

	/**
	 * 自动滚动计算
	 * 
	 * @Description:
	 * @param v
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2013-8-13
	 */
	private void smoothScroll(final View v) {
		scrollview.setVerticalScrollBarEnabled(false);
		// 软键盘有可能尚未弹出，所以scrollview需要延迟滚动
		// mHandler.postDelayed(new Runnable() {
		//
		// public void run() {
		// scrollview.smoothScrollTo(0, v.getTop() - 5);
		// }
		// }, 50);
	}

	/**
	 * 
	 * method name: autoBubbles function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param view
	 * @return field_name boolean return type
	 * @History memory：
	 * @Date：2014-12-19 下午7:28:57 @Modified by：zhangjx
	 * @Description： 自动形成气泡
	 */
	public boolean autoBubbles(ChoseAddressView view) {
		// 判断输入邮箱地址是否正确，若正确加入泡泡联系人中
		String Email = view.getAddressViewControl().getAutoCompleteTextView()
				.getEditableText().toString();
		if (StringUtil.isEmailAllValid(Email)) {
			view.addContacts(Email);
		} else {
			Toast.makeText(this,
					R.string.message_compose_error_wrong_recipients, 0).show();
			return false;
		}
		List<Address> addresses = view.getAddresses();
		// 判断输入联系人与已选择联系人是否重复。
		for (int i = 0; i < addresses.size(); i++) {
			if (Email.equals(addresses.get(i).getAddress())) {
				view.getAddressViewControl().getAutoCompleteTextView()
						.setText("");
			}
		}
		return true;
	}

	/**
	 * 
	 * method name: onSend function @Description: TODO Parameters and return
	 * values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-12-19 下午7:13:27 @Modified by：zhangjx
	 * @Description：发送邮件
	 */
	private void onSend() {
		if (!autoBubbles(mToView)) {
			return;
		}
		if (!autoBubbles(mCcView)) {
			return;
		}
		if (!autoBubbles(mBccView)) {
			return;
		}
		// 判断是否有联系人
		if (mToView.getAddresses().size() == 0&&mCcView.getAddresses().size() == 0&&mBccView.getAddresses().size() == 0) {
			Toast.makeText(MailComposeActivity.this,
					R.string.message_compose_error_no_recipients,
					Toast.LENGTH_SHORT).show();
			return;
		}
		// 发送之前测试网络是否连通！没有网络则进入设置页面
		if (!NetUtil.isActive()) {
			NetUtil.showNoConnectedAlertDlg(MailComposeActivity.this);
			return;
		}
		// 无主题提示
		if (mSubjectView.getText().toString() == null
				|| mSubjectView.getText().toString().trim().length() == 0) {
			showNoSubjectAlertDlg();
		} else {
			performSend();
		}
	}

	/**
	 * method name: performSend function @Description: TODO Parameters and
	 * return values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-11-28 下午3:10:25 @Modified by：zhangjx
	 * @Description：
	 */
	private void performSend() {
		openPgnProvider();
		// 发送邮件
		mActionbarSure.setClickable(false);

		if (isValidAddress(mToView.getAddresses())
		        && isValidAddress(mCcView.getAddresses())
		        && isValidAddress(mBccView.getAddresses())
		        && sendMessage()) {
    		mDraftNeedsSaving = false;
    		finish();
    		CommonUtils.hideSoftInput(this);
		} else {
		    mActionbarSure.setClickable(true);
		    editOrDiscard();
		}
	}

	private boolean isValidAddress(List<Address> addresses) {
	    for (Address address : addresses) {
            if (!StringUtil.isEmail(address.getAddress())) {
                return false;
            }
        }
	    return true;
	}

	/**
	 * 
	 * method name: openPgnProvider function @Description: TODO Parameters and
	 * return values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-11-28 下午1:36:29 @Modified by：zhangjx
	 * @Description：似乎和加密邮件有关
	 */
	private void openPgnProvider() {
		// if (mOpenPgpProvider != null) {
		// // OpenPGP Provider API
		//
		// // If not already encrypted but user wants to encrypt...
		// if (mPgpData.getEncryptedData() == null
		// && (mEncryptCheckbox.isChecked() || mCryptoSignatureCheckbox
		// .isChecked())) {
		//
		// String[] emailsArray = null;
		// if (mEncryptCheckbox.isChecked()) {
		// // get emails as array
		// List<String> emails = new ArrayList<String>();
		//
		// for (Address address : getRecipientAddresses()) {
		// emails.add(address.getAddress());
		// }
		// emailsArray = emails.toArray(new String[emails.size()]);
		// }
		// if (mEncryptCheckbox.isChecked()
		// && mCryptoSignatureCheckbox.isChecked()) {
		// Intent intent = new Intent(
		// OpenPgpApi.ACTION_SIGN_AND_ENCRYPT);
		// intent.putExtra(OpenPgpApi.EXTRA_USER_IDS, emailsArray);
		// executeOpenPgpMethod(intent);
		// } else if (mCryptoSignatureCheckbox.isChecked()) {
		// Intent intent = new Intent(OpenPgpApi.ACTION_SIGN);
		// executeOpenPgpMethod(intent);
		// } else if (mEncryptCheckbox.isChecked()) {
		// Intent intent = new Intent(OpenPgpApi.ACTION_ENCRYPT);
		// intent.putExtra(OpenPgpApi.EXTRA_USER_IDS, emailsArray);
		// executeOpenPgpMethod(intent);
		// }
		//
		// // onSend() is called again in SignEncryptCallback and with
		// // encryptedData set in pgpData!
		// return;
		// }
		// }
	}

	private boolean sendMessage() {
	    try {
            // Create the message from all the data the user has entered.
            MimeMessage message = createMessage(false); // isDraft = false
            
            //如果是发件箱进入，重新发邮件的情况，先将发件箱原有保存的邮件删除再发送邮件
            if (mAction == Action.EDIT_OUTBOX) {
                if (mMessageReference != null) {
                    messagingController.deleteOutboxTempMail(mAccount, mMessageReference.uid);
                }
            }
            
            if (messagingController.sendMessage(mAccount, message, null)) {
                long draftId = mDraftId;
                if (draftId != INVALID_DRAFT_ID) {
                    mDraftId = INVALID_DRAFT_ID;
                    // Delete a sent draft from Drafts folder.
                    messagingController.deleteDraft(mAccount, draftId, false, true);
                }
                return true;
            } else {
                throw new MessagingException("Send message failed");
            }
	    } catch (Exception e) {
	        Log.e(MailChat.LOG_TAG, e.toString());
			mActionbarSure.setClickable(true);
	    }
	    
	    return false;
	}

	/**
	 * 正文框输入改变时记录
	 * 
	 * @Description:
	 * @author:xuqq
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2013-8-14
	 */
	class ContentWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			mDraftNeedsSaving = true;
		}

		@Override
		public void afterTextChanged(Editable s) {

		}

	}

	/**
	 * 
	 * method name: saveDraft function @Description: TODO Parameters and return
	 * values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-11-28 下午2:40:41 @Modified by：zhangjx
	 * @Description：保存草稿
	 */
	private void saveIfNeeded() {
		if (!mDraftNeedsSaving || !needSaveDraft()
				|| !mAccount.hasDraftsFolder()) {
			return;
		}

		if (saveMessage(false)) {
		    mDraftNeedsSaving = false;
		}
	}
	
	private boolean saveMessage(boolean notifySuccess) {
        try {
            // Create the message from all the data the user has entered.
            MimeMessage message = createMessage(true); // isDraft = true

            // Draft messages should be marked as SEEN.
            message.setFlag(Flag.SEEN, true);

            // Save a draft
            if (mAction == Action.EDIT_DRAFT
                    || mAction == Action.EDIT_OUTBOX
                    || mAction == Action.EDIT_SENT) {
                // We're saving a previously saved draft, so update the new
                // message's uid to the old message's uid.
                // Resent message needs specific handling
                if (mMessageReference != null) {
                    if (mAction == Action.EDIT_SENT) {
                        mDraftId = INVALID_DRAFT_ID;
                    } else {
                        message.setUid(mMessageReference.uid);
                    }
                }
            }
            Message draftMessage = null;
			if (mAction == Action.EDIT_OUTBOX) {
				draftMessage = messagingController.saveDraft(mAccount, message,
						mDraftId, true);
			} else {
				draftMessage = messagingController.saveDraft(mAccount, message,
						mDraftId, false);
			}
            mDraftId = messagingController.getId(draftMessage);

            // Update resent message's reference after save draft
            if (mAction == Action.EDIT_SENT) {
                mMessageReference = draftMessage.makeMessageReference();
            }

            if (notifySuccess) {
                mHandler.sendEmptyMessage(MSG_SAVED_DRAFT);
            }
            
            return true;
        } catch (Exception e) {
            MailChat.toast(getString(R.string.message_save_failed_toast));
            Log.e(MailChat.LOG_TAG, e.toString());
        }
        
        return false;
	}
	
	public void editOrDiscard() {
        MailDialog.Builder builder = new MailDialog.Builder(MailComposeActivity.this);
        builder.setTitle(R.string.operate_notice);
        builder.setMessage(getString(R.string.message_save_failed_hint));
        builder.setPositiveButton(getString(R.string.message_save_failed_edit_button),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.setNeutralButton(getString(R.string.message_save_failed_discard_button),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        onDiscard();
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

	/*
	 * Build the Body that will contain the text of the message. We'll decide
	 * where to include it later. Draft messages are treated somewhat
	 * differently in that signatures are not appended and HTML separators
	 * between composed text and quoted text are not added.
	 * 
	 * @param isDraft If we should build a message that will be saved as a draft
	 * (as opposed to sent).
	 */
	private TextBody buildText(boolean isDraft) {
		return buildText(isDraft, mMessageFormat);
	}

	/**
	 * Build the {@link Body} that will contain the text of the message.
	 * 
	 * <p>
	 * Draft messages are treated somewhat differently in that signatures are
	 * not appended and HTML separators between composed text and quoted text
	 * are not added.
	 * </p>
	 * 
	 * @param isDraft
	 *            If {@code true} we build a message that will be saved as a
	 *            draft (as opposed to sent).
	 * @param messageFormat
	 *            Specifies what type of message to build ({@code text/plain}
	 *            vs. {@code text/html}).
	 * 
	 * @return {@link TextBody} instance that contains the entered text and
	 *         possibly the quoted original message.
	 */
	private TextBody buildText(boolean isDraft,
			SimpleMessageFormat messageFormat) {
		String messageText = mMessageContentView.getText().toString();

		TextBodyBuilder textBodyBuilder = new TextBodyBuilder(messageText);

		/*
		 * Find out if we need to include the original message as quoted text.
		 * 
		 * We include the quoted text in the body if the user didn't choose to
		 * hide it. We always include the quoted text when we're saving a draft.
		 * That's so the user is able to "un-hide" the quoted text if (s)he
		 * opens a saved draft.
		 */
		boolean includeQuotedText = (isDraft || mQuotedTextMode == QuotedTextMode.SHOW);
		boolean isReplyAfterQuote = (mQuoteStyle == QuoteStyle.PREFIX && mAccount
				.isReplyAfterQuote());

		textBodyBuilder.setIncludeQuotedText(false);
		
		// 邮件中加入引文
		// 邮件存草稿或"引用原邮件"被勾选都应附上引文
		//if (includeQuotedText && mQuotedCheckBoxCite.isChecked()) {
		// Modified by LL
		if (includeQuotedText) {
		
			if (messageFormat == SimpleMessageFormat.HTML
					&& mQuotedHtmlContent != null) {
				textBodyBuilder.setIncludeQuotedText(true);
				textBodyBuilder.setQuotedTextHtml(mQuotedHtmlContent);
				textBodyBuilder.setReplyAfterQuote(isReplyAfterQuote);
			}

			String quotedText = editedOriginalContent.getText().toString()
					.trim();
			if (messageFormat == SimpleMessageFormat.TEXT
					&& quotedText.length() > 0) {
				textBodyBuilder.setIncludeQuotedText(true);
				textBodyBuilder.setQuotedText(quotedText);
				textBodyBuilder.setReplyAfterQuote(isReplyAfterQuote);
			}
		}

		textBodyBuilder.setInsertSeparator(!isDraft);
		// 是否自动带上签名
		boolean isOutboxFolder=mAction==Action.EDIT_OUTBOX;
		boolean useSignature = (!isDraft&&!isOutboxFolder&& mIdentity.getSignatureUse());
		if (useSignature&&mAction != Action.EDIT_OUTBOX) {
			textBodyBuilder.setAppendSignature(true);
//			String signatureStr=TextUtils.isEmpty(mAccount.getSignature())?getString(R.string.default_signature):mAccount.getSignature();
			textBodyBuilder.setSignature(mSignatureView.getText().toString());
			//textBodyBuilder.setSignatureBeforeQuotedText(mAccount.isSignatureBeforeQuotedText());
			textBodyBuilder.setSignatureBeforeQuotedText(true);
		} else {
			textBodyBuilder.setAppendSignature(false);
		}

		TextBody body;
		if (messageFormat == SimpleMessageFormat.HTML) {
			body = textBodyBuilder.buildTextHtml();
		} else {
			body = textBodyBuilder.buildTextPlain();
		}
		return body;
	}

	

	/**
	 * 把list转为数组
	 * 
	 * @Description:
	 * @param addresses
	 * @return
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2013-8-14
	 */
	private Address[] getAddresses(List<Address> addresses) {
		return addresses.toArray(new Address[addresses.size()]);
	}

	/**
	 * 
	 * @Description:获取邮件地址数组
	 * @param addresses
	 * @return
	 * @see:
	 * @since:
	 * @author:sunzhongquan
	 * @date:2014-4-14
	 */
	private ArrayList<String> getAddressList(Address[] addresses) {
		if (addresses == null || addresses.length < 1) {
			return null;
		}
		ArrayList<String> addressList = new ArrayList<String>();

		for (Address addr : addresses) {
			addressList.add(addr.getAddress());
		}
		return addressList;
	}

	/**
	 * 无主题提示框
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2013-8-13
	 */
	private void showNoSubjectAlertDlg() {
		MailDialog.Builder builder = new MailDialog.Builder(this);
		builder.setTitle(getString(R.string.subject_null));
		builder.setMessage(getString(R.string.Send_without_subject));
		builder.setPositiveButton(getString(R.string.okay_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						// 正式发送
						// 发送邮件
						performSend();
					}
				}).setNeutralButton(getString(R.string.cancel_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	/**
	 * 是否需要保存草稿
	 * 
	 * @Description:收件人、抄送、密送、主题、正文 任意一项有值就保存
	 * @return
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2013-8-15
	 */
	private boolean needSaveDraft() {
		if (mToView.getAddresses().size() > 0
				|| mToView.getInputAddress().length > 0
				|| mCcView.getAddresses().size() > 0
				|| mCcView.getInputAddress().length > 0
				|| mBccView.getAddresses().size() > 0
				|| mBccView.getInputAddress().length > 0
				|| !StringUtil.isEmpty(mSubjectView.getText().toString())
				|| !StringUtil
						.isEmpty(mMessageContentView.getText().toString())
				|| (mAttachmentsLayout.getChildCount() > 0)) {
			return true;
		}
		return false;
	}

	/**
	 * 展示toast
	 * 
	 * @Description:
	 * @param string_id
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2013-8-15
	 */
	private void showToast(String string_id) {
		Toast toast = Toast.makeText(getApplicationContext(), string_id,
				Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 6, 2);
		toast.show();
	}

	@Override
	public void onBackPressed() {
		if (mDraftNeedsSaving) {
			// 返回弹出对话框，提示用户保存
			// leaveOrDelAccount();
			// 返回直接保存起来
			if (saveMessage(true)) {
			    finish();
			} else {
			    editOrDiscard();
			}
		} else {
			// Check if editing an existing draft.
			if (mDraftId == INVALID_DRAFT_ID) {
				onDiscard();
			} else {
				super.onBackPressed();
			}
		}
	}

	public void leaveOrDelAccount() {
		MailDialog.Builder builder = new MailDialog.Builder(
				MailComposeActivity.this);
		builder.setTitle(R.string.operate_notice);
		builder.setMessage(getString(R.string.save_or_discard_draft_message_instructions_fmt));
		builder.setPositiveButton(getString(R.string.save_draft_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						if (saveMessage(true)) {
    						dialog.dismiss();
    						finish();
						}
					}
				});
		builder.setNeutralButton(getString(R.string.discard_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						onDiscard();
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			if (mDraftNeedsSaving) {
				// leaveOrDelAccount();
				// 返回直接保存起来
				if (saveMessage(true)) {
				    finish();
				} else {
				    editOrDiscard();
				}
			} else {
				// Check if editing an existing draft.
				if (mDraftId == INVALID_DRAFT_ID) {
					onDiscard();
				} else {
					super.onBackPressed();
				}
			}
			CommonUtils.hideSoftInput(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void onDiscard() {
		if (mDraftId != INVALID_DRAFT_ID) {
			// messagingController.deleteDraft(mAccount, mDraftId, false);
			// Delete draft from Drafts folder.
			// Modified by LL
			// BEGIN
			messagingController.deleteDraft(mAccount, mDraftId, false, false);
			// END
			mDraftId = INVALID_DRAFT_ID;
		}
		mHandler.sendEmptyMessage(MSG_DISCARDED_DRAFT);
		mDraftNeedsSaving = false;
		finish();
	}

	/**
	 * Kick off a picker for whatever kind of MIME types we'll accept and let
	 * Android take over.
	 */
	private void onAddAttachment() {
		onAddAttachment2("*/*");
	}

	/**
	 * Kick off a picker for the specified MIME type and let Android take over.
	 * 
	 * @param mime_type
	 *            The MIME type we want our attachment to have.
	 */
	private void onAddAttachment2(final String mime_type) {
		// 警告：附件还 *没有* 被签名或者加密
		if (mAccount.getOpenPgpProvider() != null) {
			Toast.makeText(this, R.string.attachment_encryption_unsupported,
					Toast.LENGTH_LONG).show();
		}
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType(mime_type);
		startActivityForResult(Intent.createChooser(i, null),
				ACTIVITY_REQUEST_PICK_ATTACHMENT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case ACTIVITY_REQUEST_PICK_ATTACHMENT:
			// 选择文件返回结果
		    /*
			if (data == null) {
				return;
			}
			addAttachmentsFromResultIntent(data);
			*/
		    if (resultCode == Activity.RESULT_OK) {
		        checkBoxUpload.setChecked(false);

		        if (data == null) {
		            return;
		        }

		        Map<String, PickedFileInfo> checkedFileMap
		            = (Map<String, PickedFileInfo>) data.getSerializableExtra("checkedFileMap");

		        for (Entry<String, PickedFileInfo> Entry : checkedFileMap.entrySet()) {
		            PickedFileInfo file = Entry.getValue();
		            Uri uri = Uri.parse("file://" + file.mContentUri);
		            addAttachment(uri);
		        }
		    }
			break;
		case CAMERA_WITH_DATA:
			// 拍照获得附件
			if (resultCode == Activity.RESULT_OK) {
			    checkBoxUpload.setChecked(false);

				// 拍照返回的uri
				Uri uri = Uri.fromFile(mPhotoFile);
				addAttachment(uri);
			}
			break;
		case PHOTO_PICKED_WITH_DATA:
			// 选择相册获得附件
			if (resultCode == Activity.RESULT_OK) {
			    checkBoxUpload.setChecked(false);

				// String
				// picPath=FileUtil.getMediaFilePathByUri(MailComposeActivity.this,
				// data.getData());
				// String
				// picPath=FileUtil.getMediaFilePathByUri(MailComposeActivity.this,
				// data.getData());
				// Rom 4.4获取图片无法获取到路径
				// String picPath = FileUtil.getPath(MailComposeActivity.this,
				// data.getData());
				// praseAttachment(picPath);
				addAttachmentsFromResultIntent(data);
				mDraftNeedsSaving = true;
			}
			break;
		case ACTIVITY_REQUEST_ADD_RECIPIENTS_TO:
			if (resultCode == RESULT_OK) {
				addContactsFromDataTransfer(mToView);
				mDraftNeedsSaving = true;
				mToView.setActive(true);
				mCcView.setActive(false);
				mBccView.setActive(false);
				checkBoxUpload.setChecked(false);
			}
			break;
		case ACTIVITY_REQUEST_ADD_RECIPIENTS_CC:
			if (resultCode == RESULT_OK) {
				addContactsFromDataTransfer(mCcView);
				mDraftNeedsSaving = true;
				mToView.setActive(false);
				mCcView.setActive(true);
				mBccView.setActive(false);
				checkBoxUpload.setChecked(false);
			}
			break;
		case ACTIVITY_REQUEST_ADD_RECIPIENTS_BCC:
			if (resultCode == RESULT_OK) {
				addContactsFromDataTransfer(mBccView);
				mDraftNeedsSaving = true;
				mToView.setActive(false);
				mCcView.setActive(false);
				mBccView.setActive(true);
				checkBoxUpload.setChecked(false);
			}
			break;
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void addAttachmentsFromResultIntent(Intent data) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			ClipData clipData = data.getClipData();
			if (clipData != null) {
				for (int i = 0, end = clipData.getItemCount(); i < end; i++) {
					Uri uri = clipData.getItemAt(i).getUri();
					if (uri != null) {
						addAttachment(uri);
					}
				}
				return;
			}
		}

		Uri uri = data.getData();
		if (uri != null) {
			addAttachment(uri);
		}
	}

	private void addAttachment(Uri uri) {
		addAttachment(uri, null);
	}
	
	private void addAttachment(Uri uri, String contentType) {
		Attachment attachment = new Attachment();
		attachment.state = Attachment.LoadingState.URI_ONLY;
		attachment.uri = uri;
		attachment.contentType = contentType;
		
		//attachment.loaderId = ++mMaxLoaderId;
		attachment.loaderId = mMaxLoaderId.incrementAndGet();

		addAttachmentView(attachment);

		initAttachmentInfoLoader(attachment);
	}
	
	// 增加附件的新方法，尽量多记录元数据
	private void addAttachment(Uri uri,
			Attachment.LoadingState state,
			String contentType,
			String name,
			long size,
			String filename,
			String contentId,
			String contentDisposition,
			String storeData) {
		
		Attachment attachment = new Attachment();
		
		attachment.uri = uri;
		attachment.state = state;
		attachment.contentType = contentType;
		attachment.name = name;
		attachment.size = size;
		attachment.filename = filename;
		attachment.contentId = contentId;
		attachment.contentDisposition = contentDisposition;
		attachment.storeData = storeData;
		
		//attachment.loaderId = ++mMaxLoaderId;
		attachment.loaderId = mMaxLoaderId.incrementAndGet();
		
		addAttachmentView(attachment);
		
		initAttachmentInfoLoader(attachment);
	}

	private void addAttachmentView(Attachment attachment) {
	    //内联附件
        //if (attachment.contentId != null || attachment.uri == null) {
        if (attachment.contentId != null) {
            mHiddenAttachments.add(attachment);
            return;
        }
	    
		boolean hasMetadata = (attachment.state != Attachment.LoadingState.URI_ONLY);
		boolean isLoadingComplete = (attachment.state == Attachment.LoadingState.COMPLETE);

		View view = getLayoutInflater().inflate(
				R.layout.message_compose_attachments, mAttachmentsLayout,
				false);
		TextView nameView = (TextView) view.findViewById(R.id.attachment_name);
		// ImageView imgIcon = (ImageView)
		// view.findViewById(R.id.attachment_img);
		// TextView textInfo = (TextView)
		// view.findViewById(R.id.attachment_size);
		ImageView imgDelete = (ImageView) view
				.findViewById(R.id.attachment_close);
		View progressBar = view.findViewById(R.id.progressBar);

		if (hasMetadata) {
			nameView.setText(attachment.name);
			// imgIcon.setImageBitmap(AttachmentUtil
			// .getAttachmentIconForMessageCompose(
			// MailComposeActivity.this, attachment.name,
			// uri2Path(attachment.uri)));
			// textInfo.setText(FileUtil.sizeLongToString(attachment.size));
		} else {
			nameView.setText(R.string.loading_attachment);
		}

		progressBar.setVisibility(isLoadingComplete ? View.GONE : View.VISIBLE);

		imgDelete.setOnClickListener(MailComposeActivity.this);
		imgDelete.setTag(view);

		view.setTag(attachment);

		mAttachmentsLayout.addView(view);
	    //mDraftNeedsSaving = true;
	}

	private void initAttachmentInfoLoader(Attachment attachment) {
		LoaderManager loaderManager = getLoaderManager();
		Bundle bundle = new Bundle();
		bundle.putParcelable(LOADER_ARG_ATTACHMENT, attachment);
		loaderManager.initLoader(attachment.loaderId, bundle,
				mAttachmentInfoLoaderCallback);
	}

	@SuppressWarnings("unchecked")
	private void addContactsFromDataTransfer(ChoseAddressView view) {
		List<ContactAttribute> contacts = (List<ContactAttribute>) DataTransfer
				.getInstance()
				.pop(GlobalConstants.DATA_TRANSFER_FETCH_CONTACTS);
		if (contacts.size() > 0) {
			Address[] addresses = new Address[contacts.size()];
			for (int i = 0; i < contacts.size(); i++) {
				ContactAttribute contact = contacts.get(i);
				addresses[i] = new Address(contact.getEmail(),
						contact.getNickName()==null?contact.getName():contact.getNickName());
			}
			view.addContacts(addresses);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& checkBoxUpload.isChecked()) {
			checkBoxUpload.setChecked(false);
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 配置webview
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: sunzhongquan
	 * @date:2014-4-25
	 */
	private void configWebView() {
		// 设置一下webview可以缩放
		mQuotedHTML.setVerticalScrollBarEnabled(false);
		mQuotedHTML.setVerticalScrollbarOverlay(true);
		mQuotedHTML.setHorizontalScrollBarEnabled(false);
		mQuotedHTML.setHorizontalScrollbarOverlay(true);

		// SCROLLBARS_INSIDE_OVERLAY, SCROLLBARS_INSIDE_INSET,
		// SCROLLBARS_OUTSIDE_OVERLAY or
		// SCROLLBARS_OUTSIDE_INSET
		mQuotedHTML.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		mQuotedHTML.setDf(new DisplayFinish() {

			@Override
			public void After() {
			}

		});
		WebSettings webSettings = mQuotedHTML.getSettings();

		// webSettings.setTextSize(TextSize.LARGEST);
		// webSettings.setUseWideViewPort(true);
		// webSettings.setLoadWithOverviewMode(true);
		// Disable the ability to click links in the quoted HTML page. I think
		// this is a nice feature, but if someone
		// feels this should be a preference (or should go away all together),
		// I'm ok with that too. -achen 20101130
		mQuotedHTML.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return true;
			}
		});
		webSettings.setCacheMode(WebSettings.LOAD_NORMAL);
		webSettings.setSupportZoom(true);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setLoadsImagesAutomatically(true);
		webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);

		// webSettings.setLoadWithOverviewMode(true);
		// webSettings.setUseWideViewPort(false);

		webSettings.setBuiltInZoomControls(true);
		webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);// NARROW_COLUMNS会造成进入全屏状态时，网络图片错位;SINGLE_COLUMN会横向占满，没有横向滚动
		webSettings.setRenderPriority(RenderPriority.HIGH);
		// TODO 获取屏幕像素，找个统一调用的位置
		int screenDensity = getResources().getDisplayMetrics().densityDpi;
		int font = 0;
		switch (screenDensity) {
		case DisplayMetrics.DENSITY_LOW:
			font = -50;
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			font = 0;
			break;
		case DisplayMetrics.DENSITY_HIGH:
			font = 0;
			break;
		case DisplayMetrics.DENSITY_XHIGH:
			font = 100;
			break;
		}
		// 后期加上设置默认的字体大小
		// font = font;
		mQuotedHTML.setInitialScale(font);
	}

	/**
	 * 
	 * method name: onRemoveContactsCallBack
	 * 
	 * @see cn.mailchat.view.AddressViewCallBack#onRemoveContactsCallBack()
	 *      function@Description: TODO
	 * @History memory：
	 * @Date：2014-11-14 下午1:10:01 @Modified by：zhangjx
	 * @Description：地址删除回调(地址控件内点击)
	 */
	@Override
	public void onRemoveContactsCallBack(boolean isListValue) {
		// TODO Auto-generated method stub
	}

	/**
	 * 
	 * method name: onViewClicked
	 * 
	 * @see cn.mailchat.view.AddressViewCallBack#onViewClicked(cn.mailchat.view.ChoseAddressView)
	 *      function@Description: TODO
	 * @History memory：
	 * @Date：2014-11-14 下午1:11:01 @Modified by：zhangjx
	 * @Description：点击地址框时回调
	 */
	@Override
	public void onViewClicked(ChoseAddressView view) {
		// TODO Auto-generated method stub
		checkBoxUpload.setChecked(false);
	}

	/**
	 * 
	 * method name: onTextWatcherCallBack
	 * 
	 * @see cn.mailchat.view.AddressViewCallBack#onTextWatcherCallBack(java.lang.String)
	 *      function@Description: TODO
	 * @History memory：
	 * @Date：2014-11-14 下午1:08:51 @Modified by：zhangjx
	 * @Description：地址监听输入的每一个字符
	 */
	@Override
	public void onTextWatcherCallBack(String s) {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * method name: onSelectedAction
	 * 
	 * @see cn.mailchat.view.AddressViewCallBack#onSelectedAction(int)
	 *      function@Description: TODO
	 * @History memory：
	 * @Date：2014-11-14 下午1:08:29 @Modified by：zhangjx
	 * @Description：写信界面更多选项
	 */
	@Override
	public void onSelectedAction(int action) {
		if (action == 0) {// 添加抄送
			mCcView.setVisibility(View.VISIBLE);
			mCcView.setActive(true);
		} else if (action == 1) {// 添加密送
			mBccView.setVisibility(View.VISIBLE);
			mBccView.setActive(true);
		} else if (action == 2) {// 添加联系人
			onAddRecipients(ACTIVITY_REQUEST_ADD_RECIPIENTS_TO);
		}
	}

	/**
	 * method name: onAddContactsCallBack
	 * 
	 * @see cn.mailchat.view.AddressViewCallBack#onAddContactsCallBack(cn.mailchat.mail.Address[])
	 *      function@Description: TODO
	 * @History memory：
	 * @Date：2014-11-14 下午1:09:20 @Modified by：zhangjx
	 * @Description：地址下拉列表新增回调(地址控内点击)
	 */
	@Override
	public void onAddContactsCallBack(Address[] addresses) {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * method name: onActiveChanged
	 * 
	 * @see cn.mailchat.view.AddressViewCallBack#onActiveChanged(cn.mailchat.view.ChoseAddressView,
	 *      boolean) function@Description: TODO
	 * @History memory：
	 * @Date：2014-11-14 下午1:11:59 @Modified by：zhangjx
	 * @Description：地址栏状态改变时回调
	 */
	@Override
	public void onActiveChanged(ChoseAddressView view, boolean active) {
		// view.setBackgroundResource(active ? R.drawable.bg_edittext_p :
		// R.drawable.bg_edittext_n);
		if (active) {
			smoothScroll(view);
			AutoCompleteTextView textView = view.getAutoCompleteTextView();
			InputMethodManager imm = (InputMethodManager) textView.getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);
			// switch(view.getType()){
			// case TO:
			// break;
			// case CC:
			// break;
			// case BCC:
			// break;
			// }
			checkBoxUpload.setChecked(false);
		}

	}

	/**
	 * method name: onCancel
	 * 
	 * @see cn.mailchat.fragment.ProgressDialogFragment.CancelListener#onCancel(cn.mailchat.fragment.ProgressDialogFragment)
	 *      function@Description: TODO
	 * @History memory：
	 * @Date：2014-11-14 下午3:51:31 @Modified by：zhangjx
	 * @Description：
	 */
	@Override
	public void onCancel(ProgressDialogFragment fragment) {
		attachmentProgressDialogCancelled();
	}

	void attachmentProgressDialogCancelled() {
		// mWaitingForAttachments = WaitingAction.NONE;
	}

	/**
	 * method name: createMessage function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param b
	 * @return field_name MimeMessage return type
	 * @History memory：
	 * @Date：2014-11-18 上午11:12:46 @Modified by：zhangjx
	 * @Description：
	 */
	/**
	 * Build the final message to be sent (or saved). If there is another
	 * message quoted in this one, it will be baked into the final message here.
	 * 
	 * @param isDraft
	 *            Indicates if this message is a draft or not. Drafts do not
	 *            have signatures appended and have some extra metadata baked
	 *            into their header for use during thawing.
	 * @return Message to be sent.
	 * @throws MessagingException
	 */
	private MimeMessage createMessage(boolean isDraft)
			throws MessagingException {
		MimeMessage message = new MimeMessage();
		message.setSendMessage(true);
		Date date = new Date();
		message.addSentDate(date);
		message.setInternalDate(date);
		Address from = new Address(mIdentity.getEmail(), mIdentity.getName());
		message.setFrom(from);
		message.setRecipients(RecipientType.TO,
				getAddresses(mToView.getAddresses()));
		message.setRecipients(RecipientType.CC,
				getAddresses(mCcView.getAddresses()));
		message.setRecipients(RecipientType.BCC,
				getAddresses(mBccView.getAddresses()));
		String subject = mSubjectView.getText().toString().trim();
		message.setSubject(StringUtil.isEmpty(subject) ? getString(R.string.no_subject)
				: subject);
		message.setSendMessage(true);
		// Log.d("qxian", "=1=>发件人>" + from);
		// Log.d("qxian", "=2=>收件人>" + mToView.getAddresses());
		// Log.d("qxian", "=3=>抄送>" + mCcView.getAddresses());
		// Log.d("qxian", "=4=>密送>" + mBccView.getAddresses());
		if (mReadReceipt) {
			message.setHeader("Disposition-Notification-To",
					from.toEncodedString());
			message.setHeader("X-Confirm-Reading-To", from.toEncodedString());
			message.setHeader("Return-Receipt-To", from.toEncodedString());
		}

		if (!MailChat.hideUserAgent()) {
			// 用户代理
			message.setHeader("User-Agent",
					getString(R.string.message_header_mua));
		}

		final String replyTo = mIdentity.getReplyTo();
		if (replyTo != null) {
			message.setReplyTo(new Address[] { new Address(replyTo) });
		}

		if (mInReplyTo != null) {
			message.setInReplyTo(mInReplyTo);
		}

		if (mReferences != null) {
			message.setReferences(mReferences);
		}
		
		// 存储源邮件的引用信息
		if (mSourceIdentity != null) {
			message.setHeader(MailChat.SOURCE_IDENTITY_HEADER, mSourceIdentity);
		} else if (mMessageReference != null) {
			message.setHeader(MailChat.SOURCE_IDENTITY_HEADER, 
					mMessageReference.toIdentityString());
		}

		// Build the body.
		// TODO FIXME - body can be either an HTML or Text part, depending on
		// whether we're in
		// HTML mode or not. Should probably fix this so we don't mix up html
		// and text parts.
		TextBody body = null;
		if (mPgpData.getEncryptedData() != null) {
			String text = mPgpData.getEncryptedData();
			body = new TextBody(text);
		} else {
			body = buildText(isDraft);
		}
		
		// 准备发件数据，引用原邮件则更新内联附件在文本中的src地址
		// 不引用原邮件则删除内联附件
		if (!isDraft) {
			boolean isQuoted = mQuotedCheckBoxCite.isChecked();
			String text = body.getText();
			
			for (Attachment att : mHiddenAttachments) {
				if (att.contentId != null) {
					if (!isQuoted) {
						att.state = Attachment.LoadingState.DELETED;
					} else if (att.uri != null) {
						text = text.replaceAll(att.uri.toString(), "cid:" + att.contentId);
					}
				}
			}
			
			body.setText(text);
		}
		
		// Log.d("qxian", "=5=>邮件内容>" + body);
		// text/plain part when mMessageFormat == MessageFormat.HTML
		TextBody bodyPlain = null;

		final boolean hasAttachments = mAttachmentsLayout.getChildCount() + mHiddenAttachments.size() > 0;

		if (mMessageFormat == SimpleMessageFormat.HTML) {
			// HTML message (with alternative text part)

			// This is the compiled MIME part for an HTML message.
			MimeMultipart composedMimeMessage = new MimeMultipart();
			composedMimeMessage.setSubType("alternative"); // Let the receiver
															// select either the
															// text or the HTML
															// part.
			composedMimeMessage.addBodyPart(new MimeBodyPart(body, "text/html"));
			bodyPlain = buildText(isDraft, SimpleMessageFormat.TEXT);
			composedMimeMessage.addBodyPart(new MimeBodyPart(bodyPlain, "text/plain"));

			if (hasAttachments) {
				// If we're HTML and have attachments, we have a MimeMultipart
				// container to hold the
				// whole message (mp here), of which one part is a MimeMultipart
				// container
				// (composedMimeMessage) with the user's composed messages, and
				// subsequent parts for
				// the attachments.

				MimeMultipart mp = new MimeMultipart();
				mp.addBodyPart(new MimeBodyPart(composedMimeMessage));
				addAttachmentsToMessage(mp);
				message.setBody(mp);
			} else {
				// If no attachments, our multipart/alternative part is the only
				// one we need.
				message.setBody(composedMimeMessage);
			}
		} else if (mMessageFormat == SimpleMessageFormat.TEXT) {
			if (isDraft) {
				// Text-only message.
				if (hasAttachments) {
					MimeMultipart mp = new MimeMultipart();
					mp.addBodyPart(new MimeBodyPart(body, "text/plain"));
					addAttachmentsToMessage(mp);
					message.setBody(mp);
				} else {
					// No attachments to include, just stick the text body in the
					// message and call it good.
					message.setBody(body);
				}
			} else {
				// Text-only message.
				
				// This is the compiled MIME part for an TEXT message.
				MimeMultipart composedMimeMessage = new MimeMultipart();
				composedMimeMessage.setSubType("alternative"); // Let the receiver
																// select either the
																// text or the HTML
																// part.
				composedMimeMessage
						.addBodyPart(new MimeBodyPart(body, "text/plain"));
				//String bodyPlainString = buildText(isDraft, SimpleMessageFormat.TEXT).getText();
				TextBody bodyHtml = new TextBody(HtmlConverter.textToHtmlFragment(body.getText()));
				composedMimeMessage.addBodyPart(new MimeBodyPart(bodyHtml, "text/html"));
				
				if (hasAttachments) {
					MimeMultipart mp = new MimeMultipart();
					mp.addBodyPart(new MimeBodyPart(composedMimeMessage));
					addAttachmentsToMessage(mp);
					message.setBody(mp);
				} else {
					// No attachments to include, just stick the text body in the
					// message and call it good.
					message.setBody(composedMimeMessage);
				}
			}
		}

		// If this is a draft, add metadata for thawing.
		if (isDraft) {
			// Add the identity to the message.
			message.addHeader(MailChat.IDENTITY_HEADER,
					buildIdentityHeader(body, bodyPlain));
		}
		
		// 写入客户端邮件ID
		// Modified by LL
		// BEGIN
		message.removeHeader(MailChat.MAILCHAT_MESSAGE_ID_HEADER);
		if (mMailChatMessageID != null) {
			message.addHeader(MailChat.MAILCHAT_MESSAGE_ID_HEADER, mMailChatMessageID);
		} else {
			message.addHeader(MailChat.MAILCHAT_MESSAGE_ID_HEADER, UUID.randomUUID().toString());
		}
		// END
		
		return message;
	}

	/**
	 * Build the identity header string. This string contains metadata about a
	 * draft message to be used upon loading a draft for composition. This
	 * should be generated at the time of saving a draft.<br>
	 * <br>
	 * This is a URL-encoded key/value pair string. The list of possible values
	 * are in {@link IdentityField}.
	 * 
	 * @param body
	 *            {@link TextBody} to analyze for body length and offset.
	 * @param bodyPlain
	 *            {@link TextBody} to analyze for body length and offset. May be
	 *            null.
	 * @return Identity string.
	 */
	private String buildIdentityHeader(final TextBody body,
			final TextBody bodyPlain) {
		Uri.Builder uri = new Uri.Builder();
		if (body.getComposedMessageLength() != null
				&& body.getComposedMessageOffset() != null) {
			// See if the message body length is already in the TextBody.
			uri.appendQueryParameter(IdentityField.LENGTH.value(), body
					.getComposedMessageLength().toString());
			uri.appendQueryParameter(IdentityField.OFFSET.value(), body
					.getComposedMessageOffset().toString());
		} else {
			// If not, calculate it nonow.
			uri.appendQueryParameter(IdentityField.LENGTH.value(),
					Integer.toString(body.getText().length()));
			uri.appendQueryParameter(IdentityField.OFFSET.value(),
					Integer.toString(0));
		}
		if (mQuotedHtmlContent != null) {
			uri.appendQueryParameter(IdentityField.FOOTER_OFFSET.value(),
					Integer.toString(mQuotedHtmlContent
							.getFooterInsertionPoint()));
		}
		if (bodyPlain != null) {
			if (bodyPlain.getComposedMessageLength() != null
					&& bodyPlain.getComposedMessageOffset() != null) {
				// See if the message body length is already in the TextBody.
				uri.appendQueryParameter(IdentityField.PLAIN_LENGTH.value(),
						bodyPlain.getComposedMessageLength().toString());
				uri.appendQueryParameter(IdentityField.PLAIN_OFFSET.value(),
						bodyPlain.getComposedMessageOffset().toString());
			} else {
				// If not, calculate it now.
				uri.appendQueryParameter(IdentityField.PLAIN_LENGTH.value(),
						Integer.toString(body.getText().length()));
				uri.appendQueryParameter(IdentityField.PLAIN_OFFSET.value(),
						Integer.toString(0));
			}
		}
		// Save the quote style (useful for forwards).
		uri.appendQueryParameter(IdentityField.QUOTE_STYLE.value(),
				mQuoteStyle.name());

		// Save the message format for this offset.
		uri.appendQueryParameter(IdentityField.MESSAGE_FORMAT.value(),
				mMessageFormat.name());

		// If we're not using the standard identity of signature, append it on
		// to the identity blob.
		 if (mIdentity.getSignatureUse() && mSignatureChanged) {
		 uri.appendQueryParameter(IdentityField.SIGNATURE.value(),
		 mSignatureView.getText().toString());
		 }

		if (mIdentityChanged) {
			uri.appendQueryParameter(IdentityField.NAME.value(),
					mIdentity.getName());
			uri.appendQueryParameter(IdentityField.EMAIL.value(),
					mIdentity.getEmail());
		}

		if (mMessageReference != null) {
			uri.appendQueryParameter(IdentityField.ORIGINAL_MESSAGE.value(),
					mMessageReference.toIdentityString());
		}

		uri.appendQueryParameter(IdentityField.CURSOR_POSITION.value(),
				Integer.toString(mMessageContentView.getSelectionStart()));

		uri.appendQueryParameter(IdentityField.QUOTED_TEXT_MODE.value(),
				mQuotedTextMode.name());

		String k9identity = IDENTITY_VERSION_1 + uri.build().getEncodedQuery();

		if (MailChat.DEBUG) {
			Log.d(MailChat.LOG_TAG, "Generated identity: " + k9identity);
		}

		return k9identity;
	}

	/**
	 * Pull out the parts of the now loaded source message and apply them to the
	 * new message depending on the type of message being composed.
	 * 
	 * @param message
	 *            The source message used to populate the various text fields.
	 */
	private void processSourceMessage(Message message) {
		try {
			switch (mAction) {
			case REPLY:
			case REPLY_ALL:
				processMessageToReplyTo(message);
				break;
			case FORWARD:
				processMessageToForward(message);
				break;
			case EDIT_SENT:
			case EDIT_OUTBOX:
			case EDIT_DRAFT:
		        // 读取并更新源邮件
	            String[] sourceIdentityHeaders = message.getHeader(MailChat.SOURCE_IDENTITY_HEADER);
	            if (sourceIdentityHeaders != null && sourceIdentityHeaders.length > 0) {
	                // 取SourceIdentity字段
	                mSourceIdentity = sourceIdentityHeaders[0];

	                // 取源邮件引用
	                MessageReference msgRef = new MessageReference(mSourceIdentity);
	                mSourceMessage = msgRef.restoreToLocalMessage(getApplicationContext());

	                // 修复源邮件被移动或删除导致的mSourceMessage为空问题
	                // TODO 检查是否有其他影响
	                if (mSourceMessage != null) {
	                    // 取源邮件Body
	                    FetchProfile fp = new FetchProfile();
	                    fp.add(FetchProfile.Item.BODY);
	                    mSourceMessage.getFolder().fetch(new Message[]{mSourceMessage}, fp, null);
	                }
	            }

				processDraftMessage(message);
				break;
			default:
				Log.w(MailChat.LOG_TAG, "processSourceMessage() called with unsupported action");
				break;
			}
		} catch (MessagingException me) {
			/**
			 * Let the user continue composing their message even if we have a
			 * problem processing the source message. Log it as an error,
			 * though.
			 */
			Log.e(MailChat.LOG_TAG, "Error while processing source message: ",
					me);
		} finally {
			mSourceMessageProcessed = true;
			mDraftNeedsSaving = false;
		}

		updateMessageFormat();
	}

	private void processMessageToReplyTo(Message message)
			throws MessagingException {
		if (message.getSubject() != null) {
			final String subject = PREFIX.matcher(message.getSubject())
					.replaceFirst("");

			if (!subject.toLowerCase(Locale.US).startsWith("re:")) {
				mSubjectView.setText("Re: " + subject);
			} else {
				mSubjectView.setText(subject);
			}
		} else {
			mSubjectView.setText("");
		}

		/*
		 * If a reply-to was included with the message use that, otherwise use
		 * the from or sender address.
		 */
		Address[] replyToAddresses;
		if (message.getReplyTo().length > 0) {
			replyToAddresses = message.getReplyTo();
		} else {
			replyToAddresses = message.getFrom();
		}

		// if we're replying to a message we sent, we probably meant
		// to reply to the recipient of that message
		// 如果收件人是自己的话，有可能是想回复给发件人，有点奇怪，这里
		// if (mAccount.isAnIdentity(replyToAddresses)) {
		// replyToAddresses = message.getRecipients(RecipientType.TO);
		// }
		if (mAction == Action.REPLY) {
			mToView.addContacts(replyToAddresses);
		} else if (mAction == Action.REPLY_ALL) {
			// if (message.getReplyTo().length > 0) {
			// for (Address address : message.getFrom()) {
			// if (!mAccount.isAnIdentity(address)
			// && !Utility
			// .arrayContains(replyToAddresses, address)) {
			mToView.addContacts(message.getFrom());
			// }
			// }
			// }
			if (message.getRecipients(RecipientType.TO).length > 0) {
				ArrayList<Address> tOArrayList = new ArrayList<Address>();
				for (Address address : message.getRecipients(RecipientType.TO)) {
					tOArrayList.add(address);
				}
				if (message.getFrom().length > 0) {
					for (Address addressFrom : message.getFrom()) {
						tOArrayList.add(addressFrom);
					}
				}
				if (tOArrayList != null && tOArrayList.size() > 0) {
					for (int i = 0; i < tOArrayList.size(); i++) {
						if (mAccount.getEmail().equals(
								tOArrayList.get(i).getAddress())) {
							tOArrayList.remove(i);
						}
					}
				}
				mToView.addContacts(getAddresses(tOArrayList));
				// for (Address address :
				// message.getRecipients(RecipientType.TO)) {
				// if (!mAccount.isAnIdentity(address)
				// && !Utility.arrayContains(replyToAddresses, address)) {
				// addAddress(mToView, address);
				// }
				//
				// }
			}
			if (message.getRecipients(RecipientType.CC).length > 0) {
				ArrayList<Address> cCArrayList = new ArrayList<Address>();
				for (Address address : message.getRecipients(RecipientType.CC)) {
					cCArrayList.add(address);
				}
				if (cCArrayList != null && cCArrayList.size() > 0) {
					for (int i = 0; i < cCArrayList.size(); i++) {
						if (mAccount.getEmail().equals(
								cCArrayList.get(i).getAddress())) {
							cCArrayList.remove(i);
						}
					}
				}
				if (cCArrayList.size() > 0) {
					mCcView.addContacts(getAddresses(cCArrayList));
					// for (Address address :
					// message.getRecipients(RecipientType.CC)) {
					// if (!mAccount.isAnIdentity(address)
					// && !Utility
					// .arrayContains(replyToAddresses, address)) {
					// addAddress(mCcView, address);
					// }
					//
					// }
					mCcView.setVisibility(View.VISIBLE);
				}
			}
		}
		if (message.getMessageId() != null
				&& message.getMessageId().length() > 0) {
			mInReplyTo = message.getMessageId();

			String[] refs = message.getReferences();
			if (refs != null && refs.length > 0) {
				mReferences = TextUtils.join("", refs) + " " + mInReplyTo;
			} else {
				mReferences = mInReplyTo;
			}

		} else {
			if (MailChat.DEBUG) {
				Log.d(MailChat.LOG_TAG, "could not get Message-ID.");
			}
		}

		// Quote the message and setup the UI.
		populateUIWithQuotedMessage(mAccount.isDefaultQuotedTextShown());

		// 加载内联图片附件，并更新内联图片src地址
		// Modified by LL
		// BEGIN
		if (!mSourceMessageProcessed) {
			if (mQuotedHtmlContent != null) {
				mQuotedString = mQuotedHtmlContent.getQuotedContent();
			} else {
				mQuotedString = null;
			}
			loadAttachments(message, 0, true, null);
			if (mQuotedHTML != null) {
				mQuotedHTML.setText(mQuotedString);
			}
		}
		// END

		if (mAction == Action.REPLY || mAction == Action.REPLY_ALL) {
			Identity useIdentity = IdentityHelper
					.getRecipientIdentityFromMessage(mAccount, message);
			Identity defaultIdentity = mAccount.getIdentity(0);
			if (useIdentity != defaultIdentity) {
				switchToIdentity(useIdentity);
			}
		}
	}

	private Address[] getAddresses(ArrayList<Address> arrayList) {
		return arrayList.toArray(new Address[arrayList.size()]);
	}
	/**
	 * method name: switchToIdentity function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param useIdentity
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-11-27 下午4:19:14 @Modified by：zhangjx
	 * @Description：
	 */
	private void switchToIdentity(Identity identity) {
		mIdentity = identity;
		mIdentityChanged = true;
		mDraftNeedsSaving = true;
		updateFrom();
		onAddCcBcc();
        updateSignature();
		updateMessageFormat();
	}

	/**
	 * 
	 * method name: updateFrom function @Description: TODO Parameters and return
	 * values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-11-27 下午4:28:38 @Modified by：zhangjx
	 * @Description：修改发件人
	 */
	private void updateFrom() {
		// mChooseIdentityButton.setText(mIdentity.getEmail());
	}

	private void processMessageToForward(Message message)
			throws MessagingException {
		
		if (mAttachmentPosition == null) {
			String subject = message.getSubject();
			if (subject != null
					&& !subject.toLowerCase(Locale.US).startsWith("fwd:")) {
				mSubjectView.setText("Fwd: " + subject);
			} else {
				mSubjectView.setText(subject);
			}
		} else {
			String subject = getText(R.string.forward_single_attachment_subject).toString();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
			subject = String.format(subject,
					mAccount.getName(),
					sdf.format(new Date(System.currentTimeMillis())));
			mSubjectView.setText(subject);
		}
		
		mQuoteStyle = QuoteStyle.HEADER;

		// "Be Like Thunderbird" - on forwarded messages, set the message ID
		// of the forwarded message in the references and the reply to. TB
		// only includes ID of the message being forwarded in the reference,
		// even if there are multiple references.
		if (!StringUtils.isNullOrEmpty(message.getMessageId())) {
			mInReplyTo = message.getMessageId();
			mReferences = mInReplyTo;
		} else {
			if (MailChat.DEBUG) {
				Log.d(MailChat.LOG_TAG, "could not get Message-ID.");
			}
		}
		
		// Quote the message and setup the UI.
		if (mAttachmentPosition == null) {
			populateUIWithQuotedMessage(true);
		}
		
		/*
		if (!mSourceMessageProcessed) {
			if (!loadAttachments(message, 0)) {
				mHandler.sendEmptyMessage(MSG_SKIPPED_ATTACHMENTS);
			}
		}
		*/
		// 更新内联图片src地址
		// Modified by LL
		// BEGIN
		if (!mSourceMessageProcessed) {
			if (mQuotedHtmlContent != null) {
				mQuotedString = mQuotedHtmlContent.getQuotedContent();
			} else {
				mQuotedString = null;
			}
			
			/*
			if (!loadAttachments(message, 0, false)) {
				mHandler.sendEmptyMessage(MSG_SKIPPED_ATTACHMENTS);
			}
			*/
			// 未下载附件将被自动下载转发，关闭未下载附件将被忽略提示
			// Modified by LL
			loadAttachments(message, 0, false, mAttachmentPosition);
			
			if (mQuotedHTML != null) {
				mQuotedHTML.setText(mQuotedString);
			}
		}
		// END
	}

	/**
	 * Add all attachments of an existing message as if they were added by hand.
	 * 
	 * @param part
	 *            The message part to check for being an attachment. This method
	 *            will recurse if it's a multipart part.
	 * @param depth
	 *            The recursion depth. Currently unused.
	 * 
	 * @return {@code true} if all attachments were able to be attached,
	 *         {@code false} otherwise.
	 * 
	 * @throws MessagingException
	 *             In case of an error
	 */
    private boolean loadAttachments(Part part, int depth) throws MessagingException {
    	/*
        if (part.getBody() instanceof Multipart) {
            Multipart mp = (Multipart) part.getBody();
            boolean ret = true;
            for (int i = 0, count = mp.getCount(); i < count; i++) {
                if (!loadAttachments(mp.getBodyPart(i), depth + 1)) {
                    ret = false;
                }
            }
            return ret;
        }
        String contentType = MimeUtility.unfoldAndDecode(part.getContentType());
        String name = MimeUtility.getHeaderParameter(contentType, "name");
        if (name != null) {
            Body body = part.getBody();
            if (body instanceof LocalAttachmentBody) {
                final Uri uri = ((LocalAttachmentBody) body).getContentUri();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        addAttachment(uri);
                    }
                });
            } else {
                return false;
            }
        }
        return true;
        */
    	// 未下载附件也要载入
    	// Modified by LL
    	// BEGIN
    	if (part.getBody() instanceof Multipart) {
            Multipart mp = (Multipart) part.getBody();
            boolean ret = true;
            for (int i = 0, count = mp.getCount(); i < count; i++) {
                if (!loadAttachments(mp.getBodyPart(i), depth + 1)) {
                    ret = false;
                }
            }
            return ret;
        } else if (part instanceof LocalAttachmentBodyPart) {
            String contentType = MimeUtility.unfoldAndDecode(part.getContentType());
            String contentDisposition = MimeUtility.unfoldAndDecode(part.getDisposition());
            String name = MimeUtility.getHeaderParameter(contentType, "name");
            if (name == null) {
                name = MimeUtility.getHeaderParameter(contentDisposition, "filename");
                if (name == null) {
                    String extension = MimeUtility.getExtensionByMimeType(contentType);
                    name = getString(R.string.attachment_noname)
                            + ((extension != null) ? "." + extension : "");
                }
            }
            
            Uri uri = null;
            Attachment.LoadingState state;
            Body body = part.getBody();
            if (body != null) {
                uri = ((LocalAttachmentBody) body).getContentUri();
                state = Attachment.LoadingState.URI_ONLY;
            } else {
            	state = Attachment.LoadingState.METADATA;
            }
            
            long size = 0;
            String sizeParam = MimeUtility.getHeaderParameter(contentDisposition, "size");
            if (sizeParam != null) {
                try {
                    size = Long.parseLong(sizeParam);
                } catch (NumberFormatException e) { /* ignore */ }
            }
            
            String contentId = MimeUtility.unfoldAndDecode(part.getContentId());
            if (contentId != null) {
            	// TODO 内联图片还需要哪些区别处理
            	// TODO 检查uri为空的原因
            	// 更新引用邮件中内联图片src地址
            	// Modified by LL
            	// BEGIN
            	if (mQuotedString != null && uri != null) {
            		mQuotedString = mQuotedString.replaceAll("cid:" + contentId, uri.toString());
            	}
            	// END
            }
            
            /*
            final Uri attUri = uri;
            final Attachment.LoadingState attState = state;
            final String attContentType = contentType;
            final String attName = name;
            final long attSize = size;
            final String attFileName = null;
            final String attContentId = contentId;
            
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    addAttachment(attUri,
                    		attState,
                    		attContentType,
                    		attName,
                    		attSize,
                    		attFileName,
                    		attContentId);
                }
            });
            */
            // Modified by LL
            // BEGIN
            String storeData = Utility.combine(
					part.getHeader(MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA),
					',');
            
            final Uri attUri = uri;
            final Attachment.LoadingState attState = state;
            final String attContentType = part.getContentType();
            final String attName = name;
            final long attSize = size;
            final String attFileName = null;
            final String attContentId = part.getContentId();
            final String attContentDisposition = part.getDisposition();
            final String attStoreData = storeData;
            
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    addAttachment(attUri,
                    		attState,
                    		attContentType,
                    		attName,
                    		attSize,
                    		attFileName,
                    		attContentId,
                    		attContentDisposition,
                    		attStoreData);
                }
            });
            // END
        }
        
        return true;
    	// END
    }
    
    // 分别处理仅加载内联附件和加载所有附件两种情况，通过inlineOnly参数区分
	// Modified by LL
    // BEGIN
    private boolean loadAttachments(Part part, int depth, boolean inlineOnly, String attPosition) throws MessagingException {
    	if (part.getBody() instanceof Multipart) {
            Multipart mp = (Multipart) part.getBody();
            boolean ret = true;
            for (int i = 0, count = mp.getCount(); i < count; i++) {
                if (!loadAttachments(mp.getBodyPart(i), depth + 1, inlineOnly, attPosition)) {
                    ret = false;
                }
            }
            return ret;
        } else if (part instanceof LocalAttachmentBodyPart) {
        	boolean ret = true;
            String contentType = MimeUtility.unfoldAndDecode(part.getContentType());
            String contentDisposition = MimeUtility.unfoldAndDecode(part.getDisposition());
            String name = MimeUtility.getHeaderParameter(contentType, "name");
            if (name == null) {
                name = MimeUtility.getHeaderParameter(contentDisposition, "filename");
                if (name == null) {
                    String extension = MimeUtility.getExtensionByMimeType(contentType);
                    name = getString(R.string.attachment_noname)
                            + ((extension != null) ? "." + extension : "");
                }
            }
            
            Uri uri = null;
            Attachment.LoadingState state;
            Body body = part.getBody();
            if (body != null && body instanceof LocalAttachmentBody) {
                uri = ((LocalAttachmentBody) body).getContentUri();
                state = Attachment.LoadingState.URI_ONLY;
            } else {
            	state = Attachment.LoadingState.METADATA;
            	ret = false;
            }
            
            long size = 0;
            String sizeParam = MimeUtility.getHeaderParameter(contentDisposition, "size");
            if (sizeParam != null) {
                try {
                    size = Long.parseLong(sizeParam);
                } catch (NumberFormatException e) { /* ignore */ }
            }
            
            String contentId = MimeUtility.unfoldAndDecode(part.getContentId());
            if (contentId != null) {

                // 因编辑器目前不支持纯文本外的格式，再编辑已发送邮件时删除内联附件
                if (mAction == Action.EDIT_SENT) {
                    return true;
                }

                if (mQuotedString != null && uri != null) {
                    mQuotedString = mQuotedString.replaceAll("cid:" + contentId, uri.toString());
            	}

            } else {
            	if (inlineOnly) {
            		return true;
            	}
            }
            
            /*
            final Uri attUri = uri;
            final Attachment.LoadingState attState = state;
            final String attContentType = contentType;
            final String attName = name;
            final long attSize = size;
            final String attFileName = null;
            final String attContentId = contentId;
            
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    addAttachment(attUri,
                    		attState,
                    		attContentType,
                    		attName,
                    		attSize,
                    		attFileName,
                    		attContentId);
                }
            });
            */
            // Modified by LL
            // BEGIN
            String storeData = Utility.combine(
					part.getHeader(MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA),
					',');
            
            if (attPosition != null && !attPosition.equals(storeData)) {
            	return ret;
            }
            
            final Uri attUri = uri;
            final Attachment.LoadingState attState = state;
            final String attContentType = part.getContentType();
            final String attName = name;
            final long attSize = size;
            final String attFileName = null;
            final String attContentId = part.getContentId();
            final String attContentDisposition = part.getDisposition();
            final String attStoreData = storeData;
            
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    addAttachment(attUri,
                    		attState,
                    		attContentType,
                    		attName,
                    		attSize,
                    		attFileName,
                    		attContentId,
                    		attContentDisposition,
                    		attStoreData);
                }
            });
            // END
            
            return ret;
        }
    	
    	// Modified by LL for debug
    	// BEGIN
        else if (part instanceof LocalStore.RemoteAttachmentBodyPart) {
        	return false;
        }
    	// END
    	
    	return true;
    }
    // END

	private void processDraftMessage(Message message) throws MessagingException {
		String showQuotedTextMode = "NONE";

		mDraftId = MessagingController.getInstance(getApplication()).getId(
				message);
		mSubjectView.setText(message.getSubject());
		mToView.addContacts(message.getRecipients(RecipientType.TO));
		if (message.getRecipients(RecipientType.CC).length > 0) {
			mCcView.addContacts(message.getRecipients(RecipientType.CC));
			mCcView.setVisibility(View.VISIBLE);
		}

		Address[] bccRecipients = message.getRecipients(RecipientType.BCC);
		if (bccRecipients.length > 0) {
			mBccView.addContacts(bccRecipients);
			String bccAddress = mAccount.getAlwaysBcc();
			if (bccRecipients.length == 1 && bccAddress != null
					&& bccAddress.equals(bccRecipients[0].toString())) {
				// If the auto-bcc is the only entry in the BCC list, don't show
				// the Bcc fields.
				mBccView.setVisibility(View.GONE);
			} else {
				mBccView.setVisibility(View.VISIBLE);
			}
		}

		// Read In-Reply-To header from draft
		final String[] inReplyTo = message.getHeader("In-Reply-To");
		if ((inReplyTo != null) && (inReplyTo.length >= 1)) {
			mInReplyTo = inReplyTo[0];
		}

		// Read References header from draft
		final String[] references = message.getHeader("References");
		if ((references != null) && (references.length >= 1)) {
			mReferences = references[0];
		}
		
		// 加载附件改到引用邮件加载完成
		// Modified by LL
		/*
		if (!mSourceMessageProcessed) {
			loadAttachments(message, 0);
		}
		*/

		// Decode the identity header when loading a draft.
		// See buildIdentityHeader(TextBody) for a detailed description of the
		// composition of this blob.
		Map<IdentityField, String> MailChatidentity = new HashMap<IdentityField, String>();
		if (message.getHeader(MailChat.IDENTITY_HEADER) != null
				&& message.getHeader(MailChat.IDENTITY_HEADER).length > 0
				&& message.getHeader(MailChat.IDENTITY_HEADER)[0] != null) {
			MailChatidentity = parseIdentityHeader(message
					.getHeader(MailChat.IDENTITY_HEADER)[0]);
		}

		Identity newIdentity = new Identity();
		if (MailChatidentity.containsKey(IdentityField.SIGNATURE)) {
			newIdentity.setSignatureUse(true);
			newIdentity.setSignature(MailChatidentity
					.get(IdentityField.SIGNATURE));
			 mSignatureChanged = true;
		} else if(mAction != Action.EDIT_OUTBOX){
			newIdentity.setSignatureUse(message.getFolder().getAccount()
					.getSignatureUse());
			newIdentity.setSignature(mIdentity.getSignature());
		}

		if (MailChatidentity.containsKey(IdentityField.NAME)) {
			newIdentity.setName(MailChatidentity.get(IdentityField.NAME));
			mIdentityChanged = true;
		} else {
			newIdentity.setName(mIdentity.getName());
		}

		if (MailChatidentity.containsKey(IdentityField.EMAIL)) {
			newIdentity.setEmail(MailChatidentity.get(IdentityField.EMAIL));
			mIdentityChanged = true;
		} else {
			newIdentity.setEmail(mIdentity.getEmail());
		}

		// Message references always point to messages in drafts folder
		// Modified by LL
		// BEGIN
		/*
		 * if (MailChatidentity.containsKey(IdentityField.ORIGINAL_MESSAGE)) {
		 * mMessageReference = null; try { String originalMessage =
		 * MailChatidentity .get(IdentityField.ORIGINAL_MESSAGE);
		 * MessageReference messageReference = new MessageReference(
		 * originalMessage);
		 * 
		 * // Check if this is a valid account in our database Preferences prefs
		 * = Preferences .getPreferences(getApplicationContext()); Account
		 * account = prefs .getAccount(messageReference.accountUuid); if
		 * (account != null) { mMessageReference = messageReference; } } catch
		 * (MessagingException e) { Log.e(MailChat.LOG_TAG,
		 * "Could not decode message reference in identity.", e); } }
		 */
		// END

		int cursorPosition = 0;
		if (MailChatidentity.containsKey(IdentityField.CURSOR_POSITION)) {
			try {
				cursorPosition = Integer.valueOf(
						MailChatidentity.get(IdentityField.CURSOR_POSITION))
						.intValue();
			} catch (Exception e) {
				Log.e(MailChat.LOG_TAG,
						"Could not parse cursor position for MessageCompose; continuing.",
						e);
			}
		}

		if (MailChatidentity.containsKey(IdentityField.QUOTED_TEXT_MODE)) {
			showQuotedTextMode = MailChatidentity
					.get(IdentityField.QUOTED_TEXT_MODE);
		}

		mIdentity = newIdentity;
	    updateSignature();
		updateFrom();
		
		/*
		Integer bodyLength = MailChatidentity.get(IdentityField.LENGTH) != null ? Integer
				.valueOf(MailChatidentity.get(IdentityField.LENGTH)) : 0;
		Integer bodyOffset = MailChatidentity.get(IdentityField.OFFSET) != null ? Integer
				.valueOf(MailChatidentity.get(IdentityField.OFFSET)) : 0;
		*/
		// 修复外部草稿内容为空问题
		// Modified by LL
		// BEGIN
		Integer bodyLength = MailChatidentity.get(IdentityField.LENGTH) != null ? Integer
				.valueOf(MailChatidentity.get(IdentityField.LENGTH)) : null;
		Integer bodyOffset = MailChatidentity.get(IdentityField.OFFSET) != null ? Integer
				.valueOf(MailChatidentity.get(IdentityField.OFFSET)) : null;
		// END
		
		Integer bodyFooterOffset = MailChatidentity
				.get(IdentityField.FOOTER_OFFSET) != null ? Integer
				.valueOf(MailChatidentity.get(IdentityField.FOOTER_OFFSET))
				: null;
		Integer bodyPlainLength = MailChatidentity
				.get(IdentityField.PLAIN_LENGTH) != null ? Integer
				.valueOf(MailChatidentity.get(IdentityField.PLAIN_LENGTH))
				: null;
		Integer bodyPlainOffset = MailChatidentity
				.get(IdentityField.PLAIN_OFFSET) != null ? Integer
				.valueOf(MailChatidentity.get(IdentityField.PLAIN_OFFSET))
				: null;
		mQuoteStyle = MailChatidentity.get(IdentityField.QUOTE_STYLE) != null ? QuoteStyle
				.valueOf(MailChatidentity.get(IdentityField.QUOTE_STYLE))
				: mAccount.getQuoteStyle();

		try {
			//quotedMode = QuotedTextMode.valueOf(showQuotedTextMode);
			// Modified by LL
			mQuotedTextMode = QuotedTextMode.valueOf(showQuotedTextMode);
		} catch (Exception e) {
			//quotedMode = QuotedTextMode.NONE;
			// Modified by LL
			mQuotedTextMode = QuotedTextMode.NONE;
		}

		// Always respect the user's current composition format preference, even
		// if the
		// draft was saved in a different format.
		// TODO - The current implementation doesn't allow a user in HTML mode
		// to edit a draft that wasn't saved with MailChatmail.
		/*
		String messageFormatString = MailChatidentity
				.get(IdentityField.MESSAGE_FORMAT);

		MessageFormat messageFormat = null;
		if (messageFormatString != null) {
			try {
				messageFormat = MessageFormat.valueOf(messageFormatString);
			} catch (Exception e) { // do nothing
			}
		}

		if (messageFormat == null) {
			// This message probably wasn't created by us. The exception is
			// legacy
			// drafts created before the advent of HTML composition. In those
			// cases,
			// we'll display the whole message (including the quoted part) in
			// the
			// composition window. If that's the case, try and convert it to
			// text to
			// match the behavior in text mode.
			mMessageContentView.setText(getBodyTextFromMessage(message,
					SimpleMessageFormat.TEXT));
			mForcePlainText = true;

			//showOrHideQuotedText(quotedMode);
			// Modified by LL
			showOrHideQuotedText(mQuotedTextMode);
			
			return;
		}

		if (messageFormat == MessageFormat.HTML) {
			Part part = MimeUtility.findFirstPartByMimeType(message,
					"text/html");
			if (part != null) { // Shouldn't happen if we were the one who saved
								// it.
				mQuotedTextFormat = SimpleMessageFormat.HTML;
				String text = MimeUtility.getTextFromPart(part);
				if (MailChat.DEBUG) {
					Log.d(MailChat.LOG_TAG, "Loading message with offset "
							+ bodyOffset + ", length " + bodyLength
							+ ". Text length is " + text.length() + ".");
				}

				if (bodyOffset + bodyLength > text.length()) {
					// The draft was edited outside of K-9 Mail?
					Log.d(MailChat.LOG_TAG,
							"The identity field from the draft contains an invalid LENGTH/OFFSET");
					bodyOffset = 0;
					bodyLength = 0;
				}
				// Grab our reply text.
				String bodyText = text.substring(bodyOffset, bodyOffset
						+ bodyLength);
				mMessageContentView.setText(HtmlConverter.htmlToText(bodyText));

				// Regenerate the quoted html without our user content in it.
				StringBuilder quotedHTML = new StringBuilder();
				quotedHTML.append(text.substring(0, bodyOffset)); // stuff
																	// before
																	// the reply
				quotedHTML.append(text.substring(bodyOffset + bodyLength));
				if (quotedHTML.length() > 0) {
					mQuotedHtmlContent = new InsertableHtmlContent();
					mQuotedHtmlContent.setQuotedContent(quotedHTML);
					// We don't know if bodyOffset refers to the header or to
					// the footer
					mQuotedHtmlContent.setHeaderInsertionPoint(bodyOffset);
					if (bodyFooterOffset != null) {
						mQuotedHtmlContent
								.setFooterInsertionPoint(bodyFooterOffset);
					} else {
						mQuotedHtmlContent.setFooterInsertionPoint(bodyOffset);
					}
					mQuotedHTML.setText(mQuotedHtmlContent.getQuotedContent());
				}
			}
			if (bodyPlainOffset != null && bodyPlainLength != null) {
				processSourceMessageText(message, bodyPlainOffset,
						bodyPlainLength, false);
			}
		} else if (messageFormat == MessageFormat.TEXT) {
			mQuotedTextFormat = SimpleMessageFormat.TEXT;
			processSourceMessageText(message, bodyOffset, bodyLength, true);
		} else {
			Log.e(MailChat.LOG_TAG, "Unhandled message format.");
		}
		*/
		// 存储邮件格式信息
		// Modified by LL
		// BEGIN
		String messageFormatString = MailChatidentity
				.get(IdentityField.MESSAGE_FORMAT);
		
		if (messageFormatString != null) {
			try {
				mMessageFormat = SimpleMessageFormat.valueOf(messageFormatString);
			} catch (Exception e) { // do nothing
			}
		}

		if (mMessageFormat == null) {
			// This message probably wasn't created by us. The exception is
			// legacy
			// drafts created before the advent of HTML composition. In those
			// cases,
			// we'll display the whole message (including the quoted part) in
			// the
			// composition window. If that's the case, try and convert it to
			// text to
			// match the behavior in text mode.
			mMessageContentView.setText(getBodyTextFromMessage(message,
					SimpleMessageFormat.TEXT));
			mForcePlainText = true;
			mMessageFormat = SimpleMessageFormat.TEXT;
			
			//showOrHideQuotedText(quotedMode);
			// Modified by LL
			showOrHideQuotedText(mQuotedTextMode);
			
			return;
		}

		if (mMessageFormat == SimpleMessageFormat.HTML) {
			Part part = MimeUtility.findFirstPartByMimeType(message,
					"text/html");
			if (part != null) { // Shouldn't happen if we were the one who saved
								// it.
				mQuotedTextFormat = SimpleMessageFormat.HTML;
				String text = MimeUtility.getTextFromPart(part);
				if (MailChat.DEBUG) {
					Log.d(MailChat.LOG_TAG, "Loading message with offset "
							+ bodyOffset + ", length " + bodyLength
							+ ". Text length is " + text.length() + ".");
				}
				
				/*
				if (bodyOffset + bodyLength > text.length()) {
					// The draft was edited outside of K-9 Mail?
					Log.d(MailChat.LOG_TAG,
							"The identity field from the draft contains an invalid LENGTH/OFFSET");
					bodyOffset = 0;
					bodyLength = 0;
				}
				*/
				// 修复外部草稿内容为空问题
				// Modified by LL
				// BEGIN
				if (bodyOffset == null 
						|| bodyLength == null 
						|| bodyOffset + bodyLength > text.length()) {
					// The draft was edited outside of K-9 Mail?
					Log.d(MailChat.LOG_TAG,
							"The identity field from the draft contains an invalid LENGTH/OFFSET");
					bodyOffset = 0;
					bodyLength = text.length();
				}
				// END
				
				// Grab our reply text.
				String bodyText = text.substring(bodyOffset, bodyOffset
						+ bodyLength);
				mMessageContentView.setText(HtmlConverter.htmlToText(bodyText));

				// Regenerate the quoted html without our user content in it.
				StringBuilder quotedHTML = new StringBuilder();
				quotedHTML.append(text.substring(0, bodyOffset)); // stuff
																	// before
																	// the reply
				quotedHTML.append(text.substring(bodyOffset + bodyLength));
				if (quotedHTML.length() > 0) {
					mQuotedHtmlContent = new InsertableHtmlContent();
					mQuotedHtmlContent.setQuotedContent(quotedHTML);
					// We don't know if bodyOffset refers to the header or to
					// the footer
					mQuotedHtmlContent.setHeaderInsertionPoint(bodyOffset);
					if (bodyFooterOffset != null) {
						mQuotedHtmlContent
								.setFooterInsertionPoint(bodyFooterOffset);
					} else {
						mQuotedHtmlContent.setFooterInsertionPoint(bodyOffset);
					}
					mQuotedHTML.setText(mQuotedHtmlContent.getQuotedContent());
				}
			}
			if (bodyPlainOffset != null && bodyPlainLength != null) {
				processSourceMessageText(message, bodyPlainOffset,
						bodyPlainLength, false);
			}
		} else if (mMessageFormat == SimpleMessageFormat.TEXT) {
			mQuotedTextFormat = SimpleMessageFormat.TEXT;
			processSourceMessageText(message, bodyOffset, bodyLength, true);
		} else {
			Log.e(MailChat.LOG_TAG, "Unhandled message format.");
		}
		// END
		
		// Set the cursor position if we have it.
		try {
			mMessageContentView.setSelection(cursorPosition);
		} catch (Exception e) {
			Log.e(MailChat.LOG_TAG,
					"Could not set cursor position in MessageCompose; ignoring.",
					e);
		}

		//showOrHideQuotedText(quotedMode);
		// Modified by LL
		showOrHideQuotedText(mQuotedTextMode);
		
		// 加载附件改到引用邮件加载完成，并更新内联图片src地址
		// Modified by LL
		// BEGIN
		if (!mSourceMessageProcessed) {
			if (mQuotedHtmlContent != null) {
				mQuotedString = mQuotedHtmlContent.getQuotedContent();
			} else {
				mQuotedString = null;
			}
			loadAttachments(message, 0, false, null);
			if (mQuotedHTML != null) {
				mQuotedHTML.setText(mQuotedString);
			}
		}
		// END
	}

	/**
	 * Show or hide the quoted text.
	 * 
	 * @param mode
	 *            The value to set {@link #mQuotedTextMode} to. 显示或隐藏引用条
	 */
	private void showOrHideQuotedText(QuotedTextMode mode) {
		mQuotedTextMode = mode;
		switch (mode) {
		
		/*
		case NONE:
		case HIDE: {
			if (mode == QuotedTextMode.NONE) {
				mQuotedCheckBoxCite.setVisibility(View.GONE);
			} else {
				mQuotedCheckBoxCite.setVisibility(View.VISIBLE);
			}
			mQuotedTextBar.setVisibility(View.GONE);
			editedOriginalContent.setVisibility(View.GONE);
			mQuotedHTML.setVisibility(View.GONE);
			tvEdit.setVisibility(View.GONE);
			break;
		}
		*/
		// 正确区分QuotedTextMode.NONE和HIDE两种状态
		// Modified by LL
		// BEGIN
		case NONE: {
			mQuotedCheckBoxCite.setVisibility(View.GONE);
			mQuotedTextBar.setVisibility(View.GONE);
			editedOriginalContent.setVisibility(View.GONE);
			mQuotedHTML.setVisibility(View.GONE);
			tvEdit.setVisibility(View.GONE);
			break;
		}
		case HIDE: {
			mQuotedCheckBoxCite.setChecked(false);
			mQuotedCheckBoxCite.setVisibility(View.VISIBLE);
			mQuotedTextBar.setVisibility(View.VISIBLE);
			editedOriginalContent.setVisibility(View.GONE);
			mQuotedHTML.setVisibility(View.GONE);
			tvEdit.setVisibility(View.GONE);
			break;
		}
		// END
		
		case SHOW: {

			if (mQuotedTextFormat == SimpleMessageFormat.HTML) {
				if (mAction.equals(Action.FORWARD)) {
					mQuotedCheckBoxCite.setVisibility(View.GONE);
				} else {
					mQuotedCheckBoxCite.setVisibility(View.VISIBLE);
				}
				mQuotedTextBar.setVisibility(View.VISIBLE);
				editedOriginalContent.setVisibility(View.GONE);
				mQuotedHTML.setVisibility(View.VISIBLE);
				tvEdit.setVisibility(View.VISIBLE);
			} else {
				mQuotedTextBar.setVisibility(View.GONE);
				editedOriginalContent.setVisibility(View.VISIBLE);
				mQuotedHTML.setVisibility(View.GONE);
				tvEdit.setVisibility(View.GONE);
			}
			break;
		}
		}
	}

	/**
	 * Parse an identity string. Handles both legacy and new (!) style
	 * identities.
	 * 
	 * @param identityString
	 *            The encoded identity string that was saved in a drafts header.
	 * 
	 * @return A map containing the value for each {@link IdentityField} in the
	 *         identity string.
	 */
	private Map<IdentityField, String> parseIdentityHeader(
			final String identityString) {
		Map<IdentityField, String> identity = new HashMap<IdentityField, String>();

		if (MailChat.DEBUG) {
			Log.d(MailChat.LOG_TAG, "Decoding identity: " + identityString);
		}

		if (identityString == null || identityString.length() < 1) {
			return identity;
		}

		// Check to see if this is a "next gen" identity.
		if (identityString.charAt(0) == IDENTITY_VERSION_1.charAt(0)
				&& identityString.length() > 2) {
			Uri.Builder builder = new Uri.Builder();
			builder.encodedQuery(identityString.substring(1)); // Need to cut
																// off the ! at
																// the
																// beginning.
			Uri uri = builder.build();
			for (IdentityField key : IdentityField.values()) {
				String value = uri.getQueryParameter(key.value());
				if (value != null) {
					identity.put(key, value);
				}
			}

			if (MailChat.DEBUG) {
				Log.d(MailChat.LOG_TAG,
						"Decoded identity: " + identity.toString());
			}

			// Sanity check our Integers so that recipients of this result don't
			// have to.
			for (IdentityField key : IdentityField.getIntegerFields()) {
				if (identity.get(key) != null) {
					try {
						Integer.parseInt(identity.get(key));
					} catch (NumberFormatException e) {
						Log.e(MailChat.LOG_TAG, "Invalid " + key.name()
								+ " field in identity: " + identity.get(key));
					}
				}
			}
		} else {
			// Legacy identity

			if (MailChat.DEBUG) {
				Log.d(MailChat.LOG_TAG, "Got a saved legacy identity: "
						+ identityString);
			}
			StringTokenizer tokenizer = new StringTokenizer(identityString,
					":", false);

			// First item is the body length. We use this to separate the
			// composed reply from the quoted text.
			if (tokenizer.hasMoreTokens()) {
				String bodyLengthS = Utility
						.base64Decode(tokenizer.nextToken());
				try {
					identity.put(IdentityField.LENGTH,
							Integer.valueOf(bodyLengthS).toString());
				} catch (Exception e) {
					Log.e(MailChat.LOG_TAG, "Unable to parse bodyLength '"
							+ bodyLengthS + "'");
				}
			}
			if (tokenizer.hasMoreTokens()) {
				identity.put(IdentityField.SIGNATURE,
						Utility.base64Decode(tokenizer.nextToken()));
			}
			if (tokenizer.hasMoreTokens()) {
				identity.put(IdentityField.NAME,
						Utility.base64Decode(tokenizer.nextToken()));
			}
			if (tokenizer.hasMoreTokens()) {
				identity.put(IdentityField.EMAIL,
						Utility.base64Decode(tokenizer.nextToken()));
			}
			if (tokenizer.hasMoreTokens()) {
				identity.put(IdentityField.QUOTED_TEXT_MODE,
						Utility.base64Decode(tokenizer.nextToken()));
			}
		}

		return identity;
	}

	/**
	 * Pull out the parts of the now loaded source message and apply them to the
	 * new message depending on the type of message being composed.
	 * 
	 * @param message
	 *            Source message
	 * @param bodyOffset
	 *            Insertion point for reply.
	 * @param bodyLength
	 *            Length of reply.
	 * @param viewMessageContent
	 *            Update mMessageContentView or not.
	 * @throws MessagingException
	 */
	private void processSourceMessageText(Message message, Integer bodyOffset,
			Integer bodyLength, boolean viewMessageContent)
			throws MessagingException {
		Part textPart = MimeUtility.findFirstPartByMimeType(message,
				"text/plain");
		if (textPart != null) {
			String text = MimeUtility.getTextFromPart(textPart);
			if (MailChat.DEBUG) {
				Log.d(MailChat.LOG_TAG, "Loading message with offset "
						+ bodyOffset + ", length " + bodyLength
						+ ". Text length is " + text.length() + ".");
			}

			// If we had a body length (and it was valid), separate the
			// composition from the quoted text
			// and put them in their respective places in the UI.
			
			//if (bodyLength > 0) {
			// 修复外部纯文本草稿崩溃问题
			// Modified by LL
			if (bodyLength != null && bodyLength > 0) {
			
				try {
					String bodyText = text.substring(bodyOffset, bodyOffset
							+ bodyLength);

					// Regenerate the quoted text without our user content in it
					// nor added newlines.
					StringBuilder quotedText = new StringBuilder();
					if (bodyOffset == 0
							&& text.substring(bodyLength, bodyLength + 4)
									.equals("\r\n\r\n")) {
						// top-posting: ignore two newlines at start of quote
						quotedText.append(text.substring(bodyLength + 4));
					} else if (bodyOffset + bodyLength == text.length()
							&& text.substring(bodyOffset - 2, bodyOffset)
									.equals("\r\n")) {
						// bottom-posting: ignore newline at end of quote
						quotedText.append(text.substring(0, bodyOffset - 2));
					} else {
						quotedText.append(text.substring(0, bodyOffset)); // stuff
																			// before
																			// the
																			// reply
						quotedText.append(text.substring(bodyOffset
								+ bodyLength));
					}

					if (viewMessageContent) {
						mMessageContentView.setText(bodyText);
					}

					editedOriginalContent.setVisibility(View.VISIBLE);
					editedOriginalContent.setText(quotedText);
				} catch (IndexOutOfBoundsException e) {
					// Invalid bodyOffset or bodyLength. The draft was edited
					// outside of K-9 Mail?
					Log.d(MailChat.LOG_TAG,
							"The identity field from the draft contains an invalid bodyOffset/bodyLength");
					if (viewMessageContent) {
						mMessageContentView.setText(text);
					}
				}
			} else {
				if (viewMessageContent) {
					mMessageContentView.setText(text);
				}
			}
		}
	}

	/**
	 * Add attachments as parts into a MimeMultipart container.
	 * 
	 * @param mp
	 *            MimeMultipart container in which to insert parts.
	 * @throws MessagingException
	 */
	private void addAttachmentsToMessage(final MimeMultipart mp)
			throws MessagingException {
		Body body;
		
		List<Attachment> attachments = new ArrayList<Attachment>();
		
		for (int i = 0, count = mAttachmentsLayout.getChildCount(); i < count; i++) {
		    attachments.add((Attachment) mAttachmentsLayout.getChildAt(i).getTag());
		}
		
		for (Attachment attachment : mHiddenAttachments) {
		    attachments.add(attachment);
		}

		for (Attachment attachment : attachments) {
			// 处理未下载附件
			if (attachment.state != Attachment.LoadingState.COMPLETE) {
				MimeBodyPart mbp = new MimeBodyPart(null);

				if (attachment.contentType != null) {
					mbp.setHeader(MimeHeader.HEADER_CONTENT_TYPE, attachment.contentType);
				}
				if (attachment.contentDisposition != null) {
					mbp.setHeader(MimeHeader.HEADER_CONTENT_DISPOSITION, attachment.contentDisposition);
				}
				if (attachment.contentId != null) {
					mbp.setHeader(MimeHeader.HEADER_CONTENT_ID, attachment.contentId);
				}
				if (attachment.storeData != null) {
					mbp.setHeader(MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA, attachment.storeData);
				}
				mp.addBodyPart(mbp);

				continue;
			}

			String contentType = attachment.contentType;
			if (MimeUtil.isMessage(contentType)) {
				body = new TempFileMessageBody(attachment.filename);
			} else {
				body = new TempFileBody(attachment.filename);
			}
			MimeBodyPart bp = new MimeBodyPart(body);

			/*
			 * Correctly encode the filename here. Otherwise the whole header
			 * value (all parameters at once) will be encoded by
			 * MimeHeader.writeTo().
			 */
			// 确保Content-Type中字段不重复
			// Modified by LL
			// BEGIN
			bp.removeHeader(MimeHeader.HEADER_CONTENT_TYPE);
			contentType = MimeUtility.getHeaderParameter(contentType, null);
			// END
			
			bp.addHeader(MimeHeader.HEADER_CONTENT_TYPE, String.format(
					"%s;\r\n name=\"%s\"", contentType, EncoderUtil
							.encodeIfNecessary(attachment.name,
									EncoderUtil.Usage.WORD_ENTITY, 7)));

			bp.setEncoding(MimeUtility.getEncodingforType(contentType));

			/*
			 * TODO: Oh the joys of MIME...
			 * 
			 * From RFC 2183 (The Content-Disposition Header Field): "Parameter
			 * values longer than 78 characters, or which contain non-ASCII
			 * characters, MUST be encoded as specified in [RFC 2184]."
			 * 
			 * Example:
			 * 
			 * Content-Type: application/x-stuff
			 * title*1*=us-ascii'en'This%20is%20even%20more%20
			 * title*2*=%2A%2A%2Afun%2A%2A%2A%20 title*3="isn't it!"
			 */
			
			/*
			bp.addHeader(MimeHeader.HEADER_CONTENT_DISPOSITION, String.format(
					Locale.US, "attachment;\r\n filename=\"%s\";\r\n size=%d",
					attachment.name, attachment.size));
			*/
			// 确保Content-Disposition中字段不重复并被正确编码，
			// 同时区分处理普通附件和内联附件。
			// Modified by LL
			// BEGIN
			bp.removeHeader(MimeHeader.HEADER_CONTENT_DISPOSITION);
			
			if (attachment.contentId != null) {
				bp.addHeader(MimeHeader.HEADER_CONTENT_DISPOSITION, String.format(
						Locale.US,
						"inline;\r\n filename=\"%s\";\r\n size=%d",
						EncoderUtil.encodeIfNecessary(
								attachment.name,
								EncoderUtil.Usage.WORD_ENTITY,
								7),
						attachment.size));
				bp.addHeader(MimeHeader.HEADER_CONTENT_ID, "<" + attachment.contentId + ">");
			} else {
				bp.addHeader(MimeHeader.HEADER_CONTENT_DISPOSITION, String.format(
						Locale.US,
						"attachment;\r\n filename=\"%s\";\r\n size=%d",
						EncoderUtil.encodeIfNecessary(
								attachment.name,
								EncoderUtil.Usage.WORD_ENTITY,
								7),
						attachment.size));
			}
			
			// END
			
			mp.addBodyPart(bp);
		}
	}

	/**
	 * Fill the encrypt layout with the latest data about signature key and
	 * encryption keys.
	 */
	public void updateEncryptLayout() {
		// if (!mPgpData.hasSignatureKey()) {
		// mCryptoSignatureCheckbox.setText(R.string.btn_crypto_sign);
		// mCryptoSignatureCheckbox.setChecked(false);
		// mCryptoSignatureUserId.setVisibility(View.INVISIBLE);
		// mCryptoSignatureUserIdRest.setVisibility(View.INVISIBLE);
		// } else {
		// // if a signature key is selected, then the checkbox itself has no
		// // text
		// mCryptoSignatureCheckbox.setText("");
		// mCryptoSignatureCheckbox.setChecked(true);
		// mCryptoSignatureUserId.setVisibility(View.VISIBLE);
		// mCryptoSignatureUserIdRest.setVisibility(View.VISIBLE);
		// mCryptoSignatureUserId
		// .setText(R.string.unknown_crypto_signature_user_id);
		// mCryptoSignatureUserIdRest.setText("");
		//
		// String userId = mPgpData.getSignatureUserId();
		// if (userId != null) {
		// String chunks[] = mPgpData.getSignatureUserId().split(" <", 2);
		// mCryptoSignatureUserId.setText(chunks[0]);
		// if (chunks.length > 1) {
		// mCryptoSignatureUserIdRest.setText("<" + chunks[1]);
		// }
		// }
		// }
		// updateMessageFormat();
	}
    private void updateSignature() {
        if (mIdentity.getSignatureUse()) {
			if (mIdentity == null) {
				mIdentity = mAccount.getIdentity(0);
			}
//        	String signatureStr=TextUtils.isEmpty(mAccount.getSignature())?getString(R.string.default_signature):mAccount.getSignature();
            mSignatureView.setText(mIdentity.getSignature());
            mSignatureView.setVisibility(View.VISIBLE);
        } else {
            mSignatureView.setVisibility(View.GONE);
        }
    }
	public void onEncryptionKeySelectionDone() {
		if (mPgpData.hasEncryptionKeys()) {
			onSend();
		} else {
			// 取消发送
			Toast.makeText(this, R.string.send_aborted, Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void onEncryptDone() {
		if (mPgpData.getEncryptedData() != null) {
			onSend();
		} else {
			Toast.makeText(this, R.string.send_aborted, Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mOpenPgpServiceConnection != null) {
			mOpenPgpServiceConnection.unbindFromService();
		}
	}

    public void setAttachmentsLayoutColumnCount() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int width = size.x;
        int gridWidth = ViewUtils.dp2px(this, 75);
        int column = width / gridWidth;
        if (column >0 && column != mAttachmentsLayout.getColumnCount()) {
            if (mAttachmentsLayout.getChildCount() > 0) {
                ArrayList<View> views = new ArrayList<View>();
                for (int i = 0, count = mAttachmentsLayout.getChildCount(); i < count; i++) {
                    View view = mAttachmentsLayout.getChildAt(i);
                    views.add(view);
                }

                mAttachmentsLayout.removeAllViews();
                mAttachmentsLayout.setColumnCount(column);
                for (View view : views) {
                    mAttachmentsLayout.addView(view, new GridLayout.LayoutParams());
                }

                views.clear();
            } else {
                mAttachmentsLayout.setColumnCount(column);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setAttachmentsLayoutColumnCount();
    }
}
