package cn.mailchat.fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.openintents.openpgp.OpenPgpSignatureResult;

import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.c;

import u.aly.n;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.Account.QuoteStyle;
import cn.mailchat.Identity;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.ChooseFolder;
import cn.mailchat.activity.CreateChattingActivity;
import cn.mailchat.activity.InsertableHtmlContent;
import cn.mailchat.activity.MailComposeActivity;
import cn.mailchat.activity.MessageReference;
import cn.mailchat.cache.TemporaryAttachmentStore;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.controller.MessageRetrievalListener;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.crypto.CryptoProvider.CryptoDecryptCallback;
import cn.mailchat.crypto.PgpData;
import cn.mailchat.fragment.ConfirmationDialogFragment.ConfirmationDialogFragmentListener;
import cn.mailchat.helper.Contacts;
import cn.mailchat.helper.FileBrowserHelper;
import cn.mailchat.helper.FileBrowserHelper.FileBrowserFailOverCallback;
import cn.mailchat.helper.HtmlConverter;
import cn.mailchat.helper.Utility;
import cn.mailchat.mail.Address;
import cn.mailchat.mail.Flag;
import cn.mailchat.mail.MailNotExistException;
import cn.mailchat.mail.Message;
import cn.mailchat.mail.Message.RecipientType;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.mail.Part;
import cn.mailchat.mail.internet.MimeBodyPart;
import cn.mailchat.mail.internet.MimeMessage;
import cn.mailchat.mail.internet.MimeMultipart;
import cn.mailchat.mail.internet.MimeUtility;
import cn.mailchat.mail.internet.TextBody;
import cn.mailchat.mail.internet.TextBodyBuilder;
import cn.mailchat.mail.store.LocalStore.LocalAttachmentBodyPart;
import cn.mailchat.mail.store.LocalStore.LocalMessage;
import cn.mailchat.provider.AttachmentProvider;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.utils.TimeUtils;
import cn.mailchat.view.AttachmentView;
import cn.mailchat.view.AttachmentView.AttachmentFileDownloadCallback;
import cn.mailchat.view.AttachmentView.DownloadAttachmentNotification;
import cn.mailchat.view.MessageHeader;
import cn.mailchat.view.NonLockingScrollView;
import cn.mailchat.view.SetUserInfoDialog;
import cn.mailchat.view.SetUserInfoDialog.SetUserInfoDialogListener;
import cn.mailchat.view.SingleMessageView;

@SuppressLint("ValidFragment")
public class MessageViewFragment extends Fragment implements OnClickListener,
		CryptoDecryptCallback, ConfirmationDialogFragmentListener,SetUserInfoDialogListener {

	public static final String DRAWER_MESSAGE_VIEW_TAG = "cn.mailchat.fragment.messageViewFragment";

	private static final String ARG_REFERENCE = "reference";

	private static final String STATE_MESSAGE_REFERENCE = "reference";
	private static final String STATE_PGP_DATA = "pgpData";

	private static final int ACTIVITY_CHOOSE_FOLDER_MOVE = 1;
	private static final int ACTIVITY_CHOOSE_FOLDER_COPY = 2;
	private static final int ACTIVITY_CHOOSE_DIRECTORY = 3;

	private static final String EXTRA_SEND_FOLDER = "sendFolder";

	private static final int QUOTE_BUFFER_LENGTH = 512;

	enum SimpleMessageFormat {
		TEXT, HTML
	}

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

	@SuppressLint("ValidFragment")
	public static MessageViewFragment newInstance(MessageReference reference,
			boolean isSendFolder) {
		MessageViewFragment fragment = new MessageViewFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_REFERENCE, reference);
		args.putBoolean(EXTRA_SEND_FOLDER, isSendFolder);
		fragment.setArguments(args);

		return fragment;
	}

	private SingleMessageView mMessageView;
	private PgpData mPgpData;
	private Account mAccount;
	private MessageReference mMessageReference;
	private Message mMessage;
	private MessagingController mController;
	private Listener mListener = new Listener();
	private MessageViewHandler mHandler = new MessageViewHandler();
	private LayoutInflater mLayoutInflater;

	/**
	 * this variable is used to save the calling AttachmentView until the
	 * onActivityResult is called. => with this reference we can identity the
	 * caller
	 */
	private AttachmentView attachmentTmpStore;

	/**
	 * Used to temporarily store the destination folder for refile operations if
	 * a confirmation dialog is shown.
	 */
	private String mDstFolder;

	private MessageViewFragmentListener mFragmentListener;

	/**
	 * {@code true} after {@link #onCreate(Bundle)} has been executed. This is
	 * used by {@code MessageList.configureMenu()} to make sure the fragment has
	 * been initialized before it is used.
	 */
	private boolean mInitialized = false;

	private Context mContext;

	private Contacts mContacts;

	private LinearLayout layoutBottomBtns;
	private ProgressDialog dialog;

	private boolean isSendFolder = false;

	private EditText mReplyContent;

	private Identity mIdentity;

	private InsertableHtmlContent mQuotedHtmlContent;

	private String messageText;

	private NonLockingScrollView nonLockingScrollView;

	private LinearLayout scrollViewInner;

	class MessageViewHandler extends Handler {

		public void progress(final boolean progress) {
			post(new Runnable() {
				@Override
				public void run() {
					setProgress(progress);
				}
			});
		}

		public void addAttachment(final View attachmentView) {
			post(new Runnable() {
				@Override
				public void run() {
					mMessageView.addAttachment(attachmentView);
				}
			});
		}

		/* A helper for a set of "show a toast" methods */
		private void showToast(final String message, final int toastLength) {
			post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getActivity(), message, toastLength).show();
				}
			});
		}

		public void networkError() {
			// FIXME: This is a hack. Fix the Handler madness!
			Context context = getActivity();
			if (context == null) {
				return;
			}

			showToast(context.getString(R.string.status_network_error),
					Toast.LENGTH_LONG);
		}

		public void invalidIdError() {
			Context context = getActivity();
			if (context == null) {
				return;
			}

			showToast(context.getString(R.string.status_invalid_id_error),
					Toast.LENGTH_LONG);
		}

		public void fetchingAttachment() {
			Context context = getActivity();
			if (context == null) {
				return;
			}

			showToast(
					context.getString(R.string.message_view_fetching_attachment_toast),
					Toast.LENGTH_SHORT);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mContext = activity.getApplicationContext();

		try {
			mFragmentListener = (MessageViewFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.getClass()
					+ " must implement MessageViewFragmentListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// This fragments adds options to the action bar
		setHasOptionsMenu(true);

		mController = MessagingController.getInstance(getActivity()
				.getApplication());
		mInitialized = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Context context = new ContextThemeWrapper(inflater.getContext(),
				MailChat.getK9ThemeResourceId(MailChat.getK9MessageViewTheme()));
		mLayoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mLayoutInflater.inflate(R.layout.message, container, false);
		mMessageView = (SingleMessageView) view.findViewById(R.id.message_view);
		initView(view);
		// set a callback for the attachment view. With this callback the
		// attachmentview
		// request the start of a filebrowser activity.
		mMessageView
				.setAttachmentCallback(new AttachmentFileDownloadCallback() {

					@Override
					public void showFileBrowser(final AttachmentView caller) {
						FileBrowserHelper.getInstance()
								.showFileBrowserActivity(
										MessageViewFragment.this, null,
										ACTIVITY_CHOOSE_DIRECTORY, callback);
						attachmentTmpStore = caller;
					}

					FileBrowserFailOverCallback callback = new FileBrowserFailOverCallback() {

						@Override
						public void onPathEntered(String path) {
							attachmentTmpStore.writeFile(new File(path));
						}

						@Override
						public void onCancel() {
							// canceled, do nothing
						}
					};
				});

		mMessageView.initialize(this);
		mMessageView.downloadRemainderButton().setOnClickListener(this);
		mFragmentListener.messageHeaderViewAvailable(mMessageView
				.getMessageHeaderView());

		mContacts = Contacts.getInstance(mContext);
		return view;
	}

	private void initView(View view) {
		dialog = new ProgressDialog(getActivity());
		dialog.setCancelable(false);
		dialog.setMessage(getString(R.string.create_dchat_dialog));
		layoutBottomBtns = (LinearLayout) view
				.findViewById(R.id.layout_message_view_bottom);
		view.findViewById(R.id.btn_message_view_bottom_talk)
				.setOnClickListener(this);
		view.findViewById(R.id.btn_message_view_bottom_reply_all)
				.setOnClickListener(this);
		view.findViewById(R.id.btn_message_view_bottom_reply)
				.setOnClickListener(this);
		view.findViewById(R.id.btn_message_view_bottom_forward)
				.setOnClickListener(this);
		nonLockingScrollView = (NonLockingScrollView) view
				.findViewById(R.id.nonLockingScrollView);
		view.findViewById(R.id.btn_send).setOnClickListener(this);
		mReplyContent = (EditText) view.findViewById(R.id.et_reply_content);
		scrollViewInner = (LinearLayout) view
				.findViewById(R.id.scrollViewInner);
		mReplyContent.setOnTouchListener(changeListener);
		RelativeLayout deleteBtn = (RelativeLayout) view
				.findViewById(R.id.btn_message_view_bottom_delete);
		deleteBtn.setOnClickListener(this);
		RelativeLayout editButton = (RelativeLayout) view
				.findViewById(R.id.btn_message_view_bottom_edit);
		editButton.setOnClickListener(this);
		isSendFolder = getArguments().getBoolean(EXTRA_SEND_FOLDER);
		if (isSendFolder) {
			deleteBtn.setVisibility(View.GONE);
			editButton.setVisibility(View.VISIBLE);
		} else {
			deleteBtn.setVisibility(View.VISIBLE);
			editButton.setVisibility(View.GONE);
		}

	}

	OnTouchListener changeListener = new EditText.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				scrollToBottom(nonLockingScrollView, scrollViewInner);
			}
			return false;
		}
	};

	private List<Integer> dialogItems;

	private void scrollToBottom(final View scroll, final View inner) {
		Handler mHandler = new Handler();

		mHandler.postDelayed(new Runnable() {
			public void run() {
				if (scroll == null || inner == null) {
					return;
				}
				int offset = inner.getMeasuredHeight() - scroll.getHeight();
				if (offset < 0) {
					offset = 0;
				}

				scroll.scrollTo(0, offset);
			}
		}, 200);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		MessageReference messageReference;
		if (savedInstanceState != null) {
			mPgpData = (PgpData) savedInstanceState.get(STATE_PGP_DATA);
			messageReference = (MessageReference) savedInstanceState
					.get(STATE_MESSAGE_REFERENCE);
		} else {
			Bundle args = getArguments();
			messageReference = (MessageReference) args
					.getParcelable(ARG_REFERENCE);
		}
		displayMessage(messageReference, (mPgpData == null));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(STATE_MESSAGE_REFERENCE, mMessageReference);
		outState.putSerializable(STATE_PGP_DATA, mPgpData);
	}

	public void displayMessage(MessageReference ref) {
		displayMessage(ref, true);
	}

	private void displayMessage(MessageReference ref, boolean resetPgpData) {
		mMessageReference = ref;
		if (MailChat.DEBUG) {
			Log.d(MailChat.LOG_TAG, "MessageView displaying message "
					+ mMessageReference);
		}

		Context appContext = getActivity().getApplicationContext();
		mAccount = Preferences.getPreferences(appContext).getAccount(
				mMessageReference.accountUuid);

		if (resetPgpData) {
			// start with fresh, empty PGP data
			mPgpData = new PgpData();
		}

		// Clear previous message
		mMessageView.resetView();
		mMessageView.resetHeaderView();

		mController.loadMessageForView(mAccount, mMessageReference.folderName,
				mMessageReference.uid, mListener);

		mFragmentListener.updateMenu();
	}

	/**
	 * Called from UI thread when user select Delete
	 */
	public void onDelete() {
		/*
		 * if (MailChat.confirmDelete() || (MailChat.confirmDeleteStarred() &&
		 * mMessage.isSet(Flag.FLAGGED))) {
		 * showDialog(R.id.dialog_confirm_delete); } else { delete(); }
		 */
		// 点击删除按钮时都有确认对话框弹出
		showDialog(R.id.dialog_confirm_delete);
	}
	
	// 强制重新加载邮件
	public void onReload() {
		if (MimeUtility.isLocalUid(mMessage.getUid())) {
 			Toast toast = Toast.makeText(getActivity(),
 					R.string.cannot_reload_unsynced_message,
 					Toast.LENGTH_LONG);
 			toast.show();
		} else {
			try {
			    mMessageView.getMessageHeaderView().foldReceiversContainer();
			    
				mMessageView.setReload(false);
				mMessageView.onReload();
				
				mMessage.setFlag(Flag.X_DOWNLOADED_FULL, false);
				mMessage.setFlag(Flag.X_DOWNLOADED_PARTIAL, false);
		        
				mController.loadMessageForView(mAccount, 
						mMessage.getFolder().getName(), 
						mMessage.getUid(), 
						mListener);
			} catch (MessagingException e) {
				MailChat.toast("!");
			}
		}
	}
	
	public void saveMessageToEml() {
	    new Thread() {
	        public void run() {
                try {
                    mController.saveMessageToEml(mAccount,
                            mMessage.getFolder().getName(),
                            mMessage.getUid(),
                            new MessageRetrievalListener() {
                                
                                @Override
                                public void messagesFinished(int total) {
                                    
                                }
                                
                                @Override
                                public void messageStarted(String uid, int number, int ofTotal) {
                                    
                                }
                                
                                @Override
                                public void messageFinished(Message message, int number, int ofTotal) {
                                    if (mMessage.getUid().equals(message.getUid())) {
                                        String filename = mMessage.getSubject();
                                        if (filename == null) {
                                            filename = getString(R.string.attachment_noname);
                                        }
                                        filename = Utility.sanitizeFilename(filename) + ".eml";
                                        
                                        try {
                                            File file = TemporaryAttachmentStore.getFileForWriting(mContext,
                                                    mMessage.getUid() + ".eml");
                                            MailComposeActivity.actionForwardChatMessage(getActivity(),
                                                    mAccount,
                                                    null,
                                                    mMessage.getSubject(),
                                                    null,
                                                    Uri.fromFile(file),
                                                    filename);
                                        } catch (Exception e) {
                                            Log.e(MailChat.LOG_TAG, e.toString());
                                            MailChat.toast("!");
                                        }
                                    }
                                }
                            });
                } catch (Exception e) {
                    Log.e(MailChat.LOG_TAG, e.toString());
                    MailChat.toast("!");
                }
	        }
	    }.start();
	}

	public void onToggleAllHeadersView() {
		mMessageView.getMessageHeaderView().onShowAdditionalHeaders();
	}

	public boolean allHeadersVisible() {
		return mMessageView.getMessageHeaderView().additionalHeadersVisible();
	}

	private void delete() {
		if (mMessage != null) {
			// Disable the delete button after it's tapped (to try to prevent
			// accidental clicks)
			mFragmentListener.disableDeleteAction();
			Message messageToDelete = mMessage;
			mFragmentListener.showNextMessageOrReturn();
			mController.deleteMessages(
					Collections.singletonList(messageToDelete), true, null);
		}
	}

	public void onRefile(String dstFolder) {
		if (!mController.isMoveCapable(mAccount)) {
			return;
		}
		if (!mController.isMoveCapable(mMessage)) {
			Toast toast = Toast.makeText(getActivity(),
					R.string.cannot_copy_or_move_unsynced_message,
					Toast.LENGTH_LONG);
			toast.show();
			return;
		}

		if (MailChat.FOLDER_NONE.equalsIgnoreCase(dstFolder)) {
			return;
		}

		if (mAccount.getSpamFolderName().equals(dstFolder)
				&& MailChat.confirmSpam()) {
			mDstFolder = dstFolder;
			showDialog(R.id.dialog_confirm_spam);
		} else {
			refileMessage(dstFolder);
		}
	}

	private void refileMessage(String dstFolder) {
		String srcFolder = mMessageReference.folderName;
		Message messageToMove = mMessage;
		mFragmentListener.showNextMessageOrReturn();
		mController.moveMessage(mAccount, srcFolder, messageToMove, dstFolder,
				null);
	}

	public void onReply() {
		if (mMessage != null) {
			mFragmentListener.onReply(mMessage, mPgpData);
		}
	}

	public void onReplyAll() {
		if (mMessage != null) {
			mFragmentListener.onReplyAll(mMessage, mPgpData);
		}
	}

	public void onForward() {
		if (mMessage != null && checkForwardPossible(mMessage)) {
			mFragmentListener.onForward(mMessage, mPgpData);
		}
	}
	
	private boolean checkForwardPossible(Message message) {
		if ((MimeUtility.isLocalUid(message.getUid())
				&& !message.isSet(Flag.X_DOWNLOADED_FULL))
				|| message.isSet(Flag.X_PUSH_MAIL)) {
			Toast toast = Toast.makeText(getActivity(),
					R.string.cannot_forward_unsynced_message,
					Toast.LENGTH_LONG);
			toast.show();
			return false;
		}
		return true;
	}

	public void onToggleFlagged() {
		if (mMessage != null) {
			boolean newState = !mMessage.isSet(Flag.FLAGGED);
			mController.setFlag(mAccount, mMessage.getFolder().getName(),
					new Message[] { mMessage }, Flag.FLAGGED, newState);
			
			// 修复显示邮件信封详情时切换收藏旗标导致详情显示错乱问题
			// Modified by LL
			//mMessageView.setHeaders(mMessage, mAccount);
			
			if (newState) {
				MobclickAgent.onEvent(mContext, "collect_mail");
			}else {
				MobclickAgent.onEvent(mContext, "un_collect_mail");
			}

		}
	}

	public void onMove() {
		if ((!mController.isMoveCapable(mAccount)) || (mMessage == null)) {
			return;
		}
		if (!mController.isMoveCapable(mMessage)) {
			Toast toast = Toast.makeText(getActivity(),
					R.string.cannot_copy_or_move_unsynced_message,
					Toast.LENGTH_LONG);
			toast.show();
			return;
		}

		startRefileActivity(ACTIVITY_CHOOSE_FOLDER_MOVE);

	}

	public void onCopy() {
		if ((!mController.isCopyCapable(mAccount)) || (mMessage == null)) {
			return;
		}
		if (!mController.isCopyCapable(mMessage)) {
			Toast toast = Toast.makeText(getActivity(),
					R.string.cannot_copy_or_move_unsynced_message,
					Toast.LENGTH_LONG);
			toast.show();
			return;
		}

		startRefileActivity(ACTIVITY_CHOOSE_FOLDER_COPY);
	}

	public void onArchive() {
		onRefile(mAccount.getArchiveFolderName());
	}

	public void onSpam() {
		onRefile(mAccount.getSpamFolderName());
	}

	public void onSelectText() {
		mMessageView.beginSelectingText();
	}

	private void startRefileActivity(int activity) {
		Intent intent = new Intent(getActivity(), ChooseFolder.class);
		intent.putExtra(ChooseFolder.EXTRA_ACCOUNT, mAccount.getUuid());
		intent.putExtra(ChooseFolder.EXTRA_CUR_FOLDER,
				mMessageReference.folderName);
		intent.putExtra(ChooseFolder.EXTRA_SEL_FOLDER,
				mAccount.getLastSelectedFolderName());
		intent.putExtra(ChooseFolder.EXTRA_MESSAGE, mMessageReference);
		startActivityForResult(intent, activity);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mAccount.getCryptoProvider().onDecryptActivityResult(this,
				requestCode, resultCode, data, mPgpData)) {
			return;
		}

		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		switch (requestCode) {
		case ACTIVITY_CHOOSE_DIRECTORY: {
			if (resultCode == Activity.RESULT_OK && data != null) {
				// obtain the filename
				Uri fileUri = data.getData();
				if (fileUri != null) {
					String filePath = fileUri.getPath();
					if (filePath != null) {
						attachmentTmpStore.writeFile(new File(filePath));
					}
				}
			}
			break;
		}
		case ACTIVITY_CHOOSE_FOLDER_MOVE:
		case ACTIVITY_CHOOSE_FOLDER_COPY: {
			if (data == null) {
				return;
			}

			String destFolderName = data
					.getStringExtra(ChooseFolder.EXTRA_NEW_FOLDER);
			MessageReference ref = data
					.getParcelableExtra(ChooseFolder.EXTRA_MESSAGE);
			if (mMessageReference.equals(ref)) {
				mAccount.setLastSelectedFolderName(destFolderName);
				switch (requestCode) {
				case ACTIVITY_CHOOSE_FOLDER_MOVE: {
					mFragmentListener.showNextMessageOrReturn();
					moveMessage(ref, destFolderName);
					break;
				}
				case ACTIVITY_CHOOSE_FOLDER_COPY: {
					copyMessage(ref, destFolderName);
					break;
				}
				}
			}
			break;
		}
		}
	}

	public void onSendAlternate() {
		if (mMessage != null) {
			mController.sendAlternate(getActivity(), mAccount, mMessage);
		}
	}

	public void onToggleRead() {
		if (mMessage != null) {
		    /*
			mController.setFlag(mAccount, mMessage.getFolder().getName(),
					new Message[] { mMessage }, Flag.SEEN,
					!mMessage.isSet(Flag.SEEN));
			mMessageView.setHeaders(mMessage, mAccount);
			// String subject = mMessage.getSubject();
			// displayMessageSubject(subject);
			// change by zhangjx
			updateMessageViewTitle(mContacts, mMessage);

			mFragmentListener.updateMenu();
			*/
		    mController.setFlag(mAccount,
		            mMessage.getFolder().getName(),
		            new Message[] { mMessage },
		            Flag.SEEN,
		            false);
		}
	}

	private void onDownloadRemainder() {
		if (mMessage.isSet(Flag.X_DOWNLOADED_FULL)) {
			return;
		}
		mMessageView.downloadRemainderButton().setEnabled(false);
		mController.loadMessageForViewRemote(mAccount,
				mMessageReference.folderName, mMessageReference.uid, mListener);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		// 即时沟通
		case R.id.btn_message_view_bottom_talk: {
			customChoseChatTypeDialog();
			break;
		}
		// 回复全部
		case R.id.btn_message_view_bottom_reply_all: {
			onReplyAll();
			MobclickAgent.onEvent(mContext, "reply_all");
			break;
		}
		// 回复
		case R.id.btn_message_view_bottom_reply: {
			onReply();
			MobclickAgent.onEvent(mContext, "reply");
			break;
		}
		// 转发
		case R.id.btn_message_view_bottom_forward: {
			onForward();
			MobclickAgent.onEvent(mContext, "forward");
			break;

		}
		// 快捷回复
		case R.id.btn_send:
			String replyContent = mReplyContent.getText().toString();
			if (replyContent.length() > 0 && !replyContent.trim().equals("")) {
				messageText = mReplyContent.getText().toString();
				
				if (sendMessage()) {
    				mReplyContent.setText(null);
    				MobclickAgent.onEvent(mContext, "easy_to_reply");
				} else {
				    MailChat.toast(getString(R.string.fast_reply_email_failed));
				}
			} else {
				Toast.makeText(getActivity(),
						getString(R.string.chatting_no_content),
						Toast.LENGTH_SHORT).show();
			}
			break;
		// 删除
		case R.id.btn_message_view_bottom_delete:
			onDelete();
			MobclickAgent.onEvent(mContext, "delete_mail");
			break;
		case R.id.btn_message_view_bottom_edit:
			mFragmentListener.onResendMessage(mMessage);
			MobclickAgent.onEvent(mContext, "resend_mail");
			break;
		}
	}
	private void customChoseChatTypeDialog() {
		dialogItems = new ArrayList<Integer>();
		dialogItems.clear();
		dialogItems.add(SetUserInfoDialog.CHAT_SING);
		dialogItems.add(SetUserInfoDialog.CHAT_GROUP);
		SetUserInfoDialog dialog = new SetUserInfoDialog(getActivity(), R.style.dialog,
				dialogItems, this);
		dialog.setPos(0);
		String name=getFromNickname(mContacts, mMessage).toString();
		dialog.setCoustomStr(name==null?getString(R.string.send_mail_to_him):name);
		dialog.show();
	}
	/**
	 * 
	 * method name: sendMessage function @Description: TODO Parameters and
	 * return values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2015-2-10 下午5:40:44 @Modified by：zhangjx
	 * @Description：快速回复邮件
	 */
	private boolean sendMessage() {
	    try {
    		if (mIdentity == null) {
    			mIdentity = mAccount.getIdentity(0);
    		}
    		
            // Create the message from all the data the user has entered.
            MimeMessage message = createMessage(); // isDraft = false
            
            if (mController.sendMessage(mAccount, message, null)) {
                return true;
            } else {
                throw new MessagingException("Send fast reply failed.");
            }
	    } catch (Exception e) {
	        Log.e(MailChat.LOG_TAG, e.toString());
	    }
	    return false;
	}

	/**
	 * 把list转为数组
	 * 
	 * @Description:
	 * @param arrayList
	 * @return
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2013-8-14
	 */
	private Address[] getAddresses(ArrayList<Address> arrayList) {
		return arrayList.toArray(new Address[arrayList.size()]);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ArrayList<Address> getALLAddress(Address[] from, Address[] to, Address[] cc) {
		ArrayList<Address> alist=new ArrayList<Address>();
		for (int j = 0; j < from.length; j++) {
			alist.add(from[j]);
		}
		for (int k = 0; k < to.length; k++) {
			alist.add(to[k]);
		}
		for (int a = 0; a < cc.length; a++) {
			alist.add(cc[a]);
		}
		//去重
        if(!alist.isEmpty()){
            for(int i=0;i<alist.size();i++){
                for(int j=i+1;j<alist.size();j++){
                    if(alist.get(i).getAddress().equals(alist.get(j).getAddress())){
                    	alist.remove(j);
                    }
                }
            }
            if (alist.size() > 1) {
                for (int k = 0; k < alist.size(); k++) {
                    if(alist.get(k).getAddress().equals(mAccount.getEmail())){
                        alist.remove(k);
                    }
                }
            }
        }
		return alist;
	}
	@SuppressWarnings("unchecked")
	private MimeMessage createMessage() throws MessagingException {
		// 发件人
		Address[] fromAddrs = mMessage.getFrom();
		// 收件人
		Address[] toAddrs = mMessage.getRecipients(Message.RecipientType.TO);
		Address[] ccAddrs = mMessage.getRecipients(Message.RecipientType.CC);
		MimeMessage message = new MimeMessage();
		message.setSendMessage(true);
		message.addSentDate(new Date());
		Address from = new Address(mIdentity.getEmail(), mIdentity.getName());
		message.setFrom(from);
		message.setRecipients(RecipientType.TO,
				getAddresses(getALLAddress(fromAddrs, toAddrs, ccAddrs)));

		String subject = "Re:" + mMessage.getSubject().trim();
		message.setSubject(StringUtil.isEmpty(subject) ? getString(R.string.no_subject)
				: subject);

		if (!MailChat.hideUserAgent()) {
			// 用户代理
			message.setHeader("User-Agent",
					getString(R.string.message_header_mua));
		}

		final String replyTo = mIdentity.getReplyTo();
		if (replyTo != null) {
			message.setReplyTo(new Address[] { new Address(replyTo) });
		}

		// if (mInReplyTo != null) {
		// message.setInReplyTo(mInReplyTo);
		// }
		//
		// if (mReferences != null) {
		// message.setReferences(mReferences);
		// }

		// Build the body.
		// TODO FIXME - body can be either an HTML or Text part, depending on
		// whether we're in
		// HTML mode or not. Should probably fix this so we don't mix up html
		// and text parts.
		TextBody body = buildText(false, SimpleMessageFormat.HTML);
		// Log.d("qxian", "=5=>邮件内容>" + body);
		// text/plain part when mMessageFormat == MessageFormat.HTML
		TextBody bodyPlain = null;
		MimeMultipart composedMimeMessage = new MimeMultipart();
		composedMimeMessage.setSubType("alternative");
		composedMimeMessage.addBodyPart(new MimeBodyPart(body, "text/html"));
		bodyPlain = buildText(false, SimpleMessageFormat.TEXT);
		composedMimeMessage.addBodyPart(new MimeBodyPart(bodyPlain,
				"text/plain"));
		message.setBody(composedMimeMessage);
		return message;
	}

	private TextBody buildText(boolean isDraft,
			SimpleMessageFormat messageFormat) {
		String mSourceMessageBody = mPgpData.getDecryptedData();
		try {
			String content = (mSourceMessageBody != null) ? mSourceMessageBody
					: getBodyTextFromMessage(mMessage, SimpleMessageFormat.HTML);
			mQuotedHtmlContent = quoteOriginalHtmlMessage(mMessage, content,
					mAccount.getQuoteStyle());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TextBodyBuilder textBodyBuilder = new TextBodyBuilder(messageText);

		/*
		 * Find out if we need to include the original message as quoted text.
		 * 
		 * We include the quoted text in the body if the user didn't choose to
		 * hide it. We always include the quoted text when we're saving a draft.
		 * That's so the user is able to "un-hide" the quoted text if (s)he
		 * opens a saved draft.
		 */
		boolean includeQuotedText = true;
		boolean isReplyAfterQuote = false;

		textBodyBuilder.setIncludeQuotedText(false);
		// 邮件中加入引文
		if (messageFormat == SimpleMessageFormat.HTML
				&& mQuotedHtmlContent != null) {
			textBodyBuilder.setIncludeQuotedText(true);
			textBodyBuilder.setQuotedTextHtml(mQuotedHtmlContent);
			textBodyBuilder.setReplyAfterQuote(isReplyAfterQuote);
		}

		textBodyBuilder.setInsertSeparator(!isDraft);
		// 是否自动带上签名
		boolean useSignature = (!isDraft && mIdentity.getSignatureUse());
		if (useSignature) {
			textBodyBuilder.setAppendSignature(true);
			String signatureStr=TextUtils.isEmpty(mAccount.getSignature())?getString(R.string.default_signature):mAccount.getSignature();
			textBodyBuilder.setSignature(signatureStr);
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

	private void setProgress(boolean enable) {
		if (mFragmentListener != null) {
			mFragmentListener.setProgress(enable);
		}
	}

	private void displayMessageSubject(CharSequence title, String fromAddr) {
		if (mFragmentListener != null) {
			mFragmentListener.displayMessageSubject(title, fromAddr);
		}
	}

	public void moveMessage(MessageReference reference, String destFolderName) {
		mController.moveMessage(mAccount, mMessageReference.folderName,
				mMessage, destFolderName, null);
	}

	public void copyMessage(MessageReference reference, String destFolderName) {
		mController.copyMessage(mAccount, mMessageReference.folderName,
				mMessage, destFolderName, null);
	}

	class Listener extends MessagingListener {
		@Override
		public void loadMessageForViewHeadersAvailable(final Account account,
				String folder, String uid, final Message message) {
			if (!mMessageReference.uid.equals(uid)
					|| !mMessageReference.folderName.equals(folder)
					|| !mMessageReference.accountUuid.equals(account.getUuid())) {
				return;
			}

			/*
			 * Clone the message object because the original could be modified
			 * by MessagingController later. This could lead to a
			 * ConcurrentModificationException when that same object is accessed
			 * by the UI thread (below).
			 * 
			 * See issue 3953
			 * 
			 * This is just an ugly hack to get rid of the most pressing
			 * problem. A proper way to fix this is to make Message thread-safe.
			 * Or, even better, rewriting the UI code to access messages via a
			 * ContentProvider.
			 */
			final Message clonedMessage = message.clone();

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (!clonedMessage.isSet(Flag.X_DOWNLOADED_FULL)
							&& !clonedMessage.isSet(Flag.X_DOWNLOADED_PARTIAL)) {
						String text = mContext
								.getString(R.string.message_view_downloading);
						mMessageView.showStatusMessage(text);
					}
					mMessageView.setHeaders(clonedMessage, account);
					// final String subject = clonedMessage.getSubject();
					// if (subject == null || subject.equals("")) {
					// displayMessageSubject(mContext.getString(R.string.general_no_subject));
					// } else {
					// displayMessageSubject(clonedMessage.getSubject());
					// }
					// change by zhangjx
					updateMessageViewTitle(mContacts, clonedMessage);

					mMessageView.setOnFlagListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							onToggleFlagged();
						}
					});
				}
			});
		}

		@Override
		public void loadMessageForViewBodyAvailable(final Account account,
				String folder, String uid, final Message message) {
			if (!mMessageReference.uid.equals(uid)
					|| !mMessageReference.folderName.equals(folder)
					|| !mMessageReference.accountUuid.equals(account.getUuid())) {
				return;
			}

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					try {
						mMessage = message;
						mMessageView.setShowAllMessage(account,
								(LocalMessage) message, mPgpData, mController,
								mListener);
						mFragmentListener.updateMenu();

					} catch (MessagingException e) {
						Log.v(MailChat.LOG_TAG,
								"loadMessageForViewBodyAvailable", e);
					}
				}
			});
		}

		@Override
		public void loadMessageForViewFailed(Account account, String folder,
				String uid, final Throwable t) {
			if (!mMessageReference.uid.equals(uid)
					|| !mMessageReference.folderName.equals(folder)
					|| !mMessageReference.accountUuid.equals(account.getUuid())) {
				return;
			}
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					setProgress(false);
					mMessageView.setLoadingStatus(false);
					
					if (t instanceof MailNotExistException) {
						mMessageView.setReloadText(
								mContext.getString(R.string.message_view_reload_mail_not_exist_title),
								mContext.getString(R.string.message_view_reload_mail_not_exist_subtitle));
					} else {
						mMessageView.setReloadText(
								mContext.getString(R.string.message_view_reload_title),
								MailChat.isNotifySyncError()
								? MailChat.getRootCauseMessage(t)
								: null);
					}
					mMessageView.setReload(true);
					
					if (mMessage == null || mMessage.isSet(Flag.X_DOWNLOADED_PARTIAL)) {
						mMessageView.showStatusMessage(mContext
								.getString(R.string.webview_empty_message));
					}
				}
			});
		}

		@Override
		public void loadMessageForViewFinished(Account account, String folder,
				String uid, final Message message) {
			if (!mMessageReference.uid.equals(uid)
					|| !mMessageReference.folderName.equals(folder)
					|| !mMessageReference.accountUuid.equals(account.getUuid())) {
				return;
			}
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					setProgress(false);
					mMessageView.setLoadingStatus(false);
				}
			});
		}

		@Override
		public void loadMessageForViewStarted(Account account, String folder,
				String uid) {
			if (!mMessageReference.uid.equals(uid)
					|| !mMessageReference.folderName.equals(folder)
					|| !mMessageReference.accountUuid.equals(account.getUuid())) {
				return;
			}
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					setProgress(true);
					
					// 显示正在加载提示
					mMessageView.setLoadingStatus(true);
				}
			});
		}

		@Override
		public void loadAttachmentStarted(Account account, Message message,
				Part part, Object tag, boolean requiresDownload) {

			final Object[] params = (Object[]) tag;
			final AttachmentView attView = (AttachmentView) params[1];
			
			// 显示附件下载进度通知栏提示
			if (params[0] != null) {
				attView.mDownloadingNotification.showProgress(0);
			}

			if (mMessage != message) {
				if (!(mAccount.getUuid().equals(account.getUuid()) && mMessage
						.getUid().equals(message.getUid()))) {
					return;
				}
			}

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (params[0] != null) {
						synchronized (MailChat.downloadingList) {
							attView.loadAttachmentStarted();
						}
					}
				}
			});
		}

		@Override
		public void loadAttachmentFinished(final Account account,
				Message message, final Part part, Object tag) {

			final Object[] params = (Object[]) tag;
			final AttachmentView attView = (AttachmentView) params[1];
			final long attId = ((LocalAttachmentBodyPart) part)
					.getAttachmentId();

			// 附件下载完成，更新通知栏提示
			if (params[0] != null) {
				attView.mDownloadingNotification.finished();
			}

			if (mMessage != message) {
				if (!(mAccount.getUuid().equals(account.getUuid()) && mMessage
						.getUid().equals(message.getUid()))) {
					synchronized (MailChat.downloadingList) {
						// 改移除为加标记，后续通知会用到
						attView.mStatus = AttachmentView.Status.COMPLETE;
					}
					return;
				}
			}

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (params[0] != null) {
						synchronized (MailChat.downloadingList) {
							// 改移除为加标记后续通知会用到
							AttachmentView av = attView;
							av.mStatus = AttachmentView.Status.COMPLETE;
							av.loadAttachmentFinished();
						}
					} else {
						Uri uri = AttachmentProvider.getAttachmentUri(account, attId);
						mMessageView.updateMessageContentView(attView.contentId, uri.toString());
					}
				}
			});

		}

		@Override
		public void loadAttachmentFailed(Account account, Message message,
				Part part, Object tag, Throwable t) {

			final Object[] params = (Object[]) tag;
			final AttachmentView attView = (AttachmentView) params[1];

			// 附件下载失败，更新通知栏提示
			if (params[0] != null) {
				attView.mDownloadingNotification.failed(t);
			}

			if (mMessage != message) {
				if (!(mAccount.getUuid().equals(account.getUuid())
				        && mMessage.getUid().equals(message.getUid()))) {
					synchronized (MailChat.downloadingList) {
						// 改移除为加标记后续通知会用到
						attView.mStatus = AttachmentView.Status.METADATA;
					}
					return;
				}
			}

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (params[0] != null) {
						synchronized (MailChat.downloadingList) {
							// 改移除为加标记后续通知会用到
							AttachmentView av = attView;
							av.mStatus = AttachmentView.Status.METADATA;
							av.loadAttachmentFailed();
						}
					}
				}
			});
		}
	}

	/**
	 * Used by MessageOpenPgpView
	 */
	public void setMessageWithOpenPgp(String decryptedData,
			OpenPgpSignatureResult signatureResult) {
		try {
			// TODO: get rid of PgpData?
			PgpData data = new PgpData();
			data.setDecryptedData(decryptedData);
			data.setSignatureResult(signatureResult);
			mMessageView.setShowAllMessage(mAccount, (LocalMessage) mMessage,
					data, mController, mListener);
		} catch (MessagingException e) {
			Log.e(MailChat.LOG_TAG, "displayMessageBody failed", e);
		}
	}

	// This REALLY should be in MessageCryptoView
	@Override
	public void onDecryptDone(PgpData pgpData) {
		Account account = mAccount;
		LocalMessage message = (LocalMessage) mMessage;
		MessagingController controller = mController;
		Listener listener = mListener;
		try {
			mMessageView.setShowAllMessage(account, message, pgpData,
					controller, listener);
		} catch (MessagingException e) {
			Log.e(MailChat.LOG_TAG, "displayMessageBody failed", e);
		}
	}

	private void showDialog(int dialogId) {
		DialogFragment fragment;
		switch (dialogId) {
		case R.id.dialog_confirm_delete: {
			String title = getString(R.string.dialog_confirm_delete_title);
			String message = getString(R.string.dialog_confirm_delete_message);
			String confirmText = getString(R.string.dialog_confirm_delete_confirm_button);
			String cancelText = getString(R.string.dialog_confirm_delete_cancel_button);

			fragment = ConfirmationDialogFragment.newInstance(dialogId, title,
					message, confirmText, cancelText);
			break;
		}
		case R.id.dialog_confirm_spam: {
			String title = getString(R.string.dialog_confirm_spam_title);
			String message = getResources().getQuantityString(
					R.plurals.dialog_confirm_spam_message, 1);
			String confirmText = getString(R.string.dialog_confirm_spam_confirm_button);
			String cancelText = getString(R.string.dialog_confirm_spam_cancel_button);

			fragment = ConfirmationDialogFragment.newInstance(dialogId, title,
					message, confirmText, cancelText);
			break;
		}
		case R.id.dialog_attachment_progress: {
			String message = getString(R.string.dialog_attachment_progress_title);
			fragment = ProgressDialogFragment.newInstance(null, message);
			break;
		}
		default: {
			throw new RuntimeException(
					"Called showDialog(int) with unknown dialog id.");
		}
		}

		fragment.setTargetFragment(this, dialogId);
		fragment.show(getFragmentManager(), getDialogTag(dialogId));
	}

	private void removeDialog(int dialogId) {
		FragmentManager fm = getFragmentManager();

		if (fm == null || isRemoving() || isDetached()) {
			return;
		}

		// Make sure the "show dialog" transaction has been processed when we
		// call
		// findFragmentByTag() below. Otherwise the fragment won't be found and
		// the dialog will
		// never be dismissed.
		fm.executePendingTransactions();

		DialogFragment fragment = (DialogFragment) fm
				.findFragmentByTag(getDialogTag(dialogId));

		if (fragment != null) {
			fragment.dismiss();
		}
	}

	private String getDialogTag(int dialogId) {
		return String.format(Locale.US, "dialog-%d", dialogId);
	}

	public void zoom(KeyEvent event) {
		mMessageView.zoom(event);
	}

	@Override
	public void doPositiveClick(int dialogId) {
		switch (dialogId) {
		case R.id.dialog_confirm_delete: {
			delete();
			break;
		}
		case R.id.dialog_confirm_spam: {
			refileMessage(mDstFolder);
			mDstFolder = null;
			break;
		}
		}
	}

	@Override
	public void doNegativeClick(int dialogId) {
		/* do nothing */
		removeDialog(dialogId);
	}

	@Override
	public void dialogCancelled(int dialogId) {
		/* do nothing */
		removeDialog(dialogId);
	}

	/**
	 * Get the {@link MessageReference} of the currently displayed message.
	 */
	public MessageReference getMessageReference() {
		return mMessageReference;
	}

	public boolean isMessageRead() {
		return (mMessage != null) ? mMessage.isSet(Flag.SEEN) : false;
	}

	public boolean isCopyCapable() {
		return mController.isCopyCapable(mAccount);
	}

	public boolean isMoveCapable() {
		return mController.isMoveCapable(mAccount);
	}

	public boolean canMessageBeArchived() {
		return (!mMessageReference.folderName.equals(mAccount
				.getArchiveFolderName()) && mAccount.hasArchiveFolder());
	}

	public boolean canMessageBeMovedToSpam() {
		return (!mMessageReference.folderName.equals(mAccount
				.getSpamFolderName()) && mAccount.hasSpamFolder());
	}

	public void updateTitle() {
		if (mMessage != null) {
			// displayMessageSubject(mMessage.getSubject());
			// change by zhangjx
			updateMessageViewTitle(mContacts, mMessage);
		}
	}

	public interface MessageViewFragmentListener {
		public void onForward(Message mMessage, PgpData mPgpData);

		public void disableDeleteAction();

		public void onReplyAll(Message mMessage, PgpData mPgpData);

		public void onReply(Message mMessage, PgpData mPgpData);

		public void displayMessageSubject(CharSequence title, String from);

		public void setProgress(boolean b);

		public void showNextMessageOrReturn();

		public void messageHeaderViewAvailable(MessageHeader messageHeaderView);

		public void updateMenu();

		public void onResendMessage(Message message);

	}

	public boolean isInitialized() {
		return mInitialized;
	}

	public LayoutInflater getFragmentLayoutInflater() {
		return mLayoutInflater;
	}

	/**
	 * 
	 * method name: getFromNickname function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param mContacts
	 * @param mMessage
	 * @return field_name CharSequence return type
	 * @History memory：
	 * @Date：2014-10-22 下午4:46:51 @Modified by：zhangjx
	 * @Description：获取发件人的昵称
	 */
	private CharSequence getFromNickname(Contacts mContacts, Message mMessage) {
		final Contacts contacts = MailChat.showContactName() ? mContacts : null;
		CharSequence title = Address.toFriendly(mMessage.getFrom(), contacts);
		return title;
	}

	/**
	 * 
	 * method name: updateMessageViewTitle function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param mContacts
	 * @param mMessage
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-9-16 下午5:38:35 @Modified by：zhangjx
	 * @Description：update title bar value
	 */
	private void updateMessageViewTitle(Contacts mContacts, Message mMessage) {
		final Contacts contacts = MailChat.showContactName() ? mContacts : null;
		CharSequence title = Address.toFriendly(mMessage.getFrom(), contacts);
		Address[] addrs = mMessage.getFrom();
		Address addrName = null;
		if (addrs.length > 0) {
			addrName = addrs[0];
			if (title.length() > 0) {
				if (title.toString().contains("@")) {
					String email = addrName.getAddress();
					if (email != null && !email.equals("null")) {
						title = email.substring(0, email.indexOf("@"));
					}
				}
				displayMessageSubject(
						mContext.getResources().getString(
								R.string.message_come_from)
								+ title,
						StringUtil.getEmailSuffix(addrName.getAddress()));
			} else {
				String email = addrName.getAddress();
				title = email.substring(0, email.indexOf("@"));
				displayMessageSubject(
						mContext.getResources().getString(
								R.string.message_come_from)
								+ title, StringUtil.getEmailSuffix(email));
			}
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("MailDetialActivity");
	}
	@Override
	public void onPause() {
		super.onPause();
		mController.removeListener(mListener);
		MobclickAgent.onPageEnd("MailDetialActivity");
	}

	@Override
	public void onDialogClick(int position) {
		switch (dialogItems.get(position)) {
		case SetUserInfoDialog.CHAT_SING:
			createSingChat();
			MobclickAgent.onEvent(mContext, "create_d_chat_from_mail_detial");
			break;
		case SetUserInfoDialog.CHAT_GROUP:
			if(!mAccount.isAuthenticated()){
				Toast.makeText(mContext,getString(R.string.authenticate_error_no_chat),Toast.LENGTH_SHORT).show();
				return;
			}
			createGroup();
			MobclickAgent.onEvent(mContext, "create_g_chat_from_mail_detial");
			break;
		default:
			break;
		}
	}
	private void createSingChat() {
		String email = mMessage.getFrom()[0].getAddress();
		email = checkEmail(email);
		mController.actionDChatOrInvitation(mAccount, getActivity(),
				mHandler, false, dialog, email,  mMessage.getFrom()[0].getPersonal(),mMessage.getSubject(),mMessage.getPreview());
		MobclickAgent.onEvent(mContext, "create_d_chat");
	}

	private void createGroup() {
		Address[] allAddrs = null;
		try {
			// 发件人
			Address[] fromAddrs = mMessage.getFrom();
			// 收件人
			Address[] toAddrs = mMessage
					.getRecipients(Message.RecipientType.TO);
			Address[] ccAddrs = mMessage
					.getRecipients(Message.RecipientType.CC);
			allAddrs = getAddresses(getALLAddress(fromAddrs, toAddrs, ccAddrs));
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		ArrayList<CGroupMember> mMembers = new ArrayList<CGroupMember>();
		createCGroupMembers(allAddrs, mMembers);

		//透传信息
		String email = checkEmail(mMessage.getFrom()[0].getAddress());
		String [] mailInfoArray =new String []{email,mMessage.getFrom()[0].getPersonal(),mMessage.getSubject(),mMessage.getPreview()};

		CreateChattingActivity.createGroupChattingFromMail(
				getActivity(), true, mMembers, "",mailInfoArray);
	}

	private String checkEmail(String email){
		if(email.equals("null")||email==null){
			if (mMessage.getReplyTo().length > 0) {
				email= mMessage.getReplyTo()[0].getAddress();
			}
		}
		if (email.endsWith("@china-channel.com")) {
			email = email.replace("@china-channel.com", "@35.cn");
		}
		return email;
	}

	private void createCGroupMembers(Address[] Addresses,
			ArrayList<CGroupMember> mMembers) {
		if (Addresses != null) {
			for (Address address : Addresses) {
				String person = address.getPersonal();
				// 若person为空，则截取email
				if (StringUtil.isEmpty(person)) {
					person = address.getAddress().substring(0,
							address.getAddress().indexOf("@"));
				}
				String email = address.getAddress();
				if (email.endsWith("@china-channel.com")) {
					email = email.replace("@china-channel.com", "@35.cn");
				}
				mMembers.add(new CGroupMember(person, email));
			}
		}
	}
}
