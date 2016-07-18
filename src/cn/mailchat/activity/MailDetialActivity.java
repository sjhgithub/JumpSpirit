package cn.mailchat.activity;

import java.util.ArrayList;
import java.util.List;

import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.internal.widget.ListPopupWindow;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.controller.NotificationCenter;
import cn.mailchat.crypto.PgpData;
import cn.mailchat.fragment.MessageViewFragment;
import cn.mailchat.fragment.MessageViewFragment.MessageViewFragmentListener;
import cn.mailchat.mail.Message;
import cn.mailchat.search.LocalSearch;
import cn.mailchat.utils.ActivityManager;
import cn.mailchat.utils.CommonUtils;
import cn.mailchat.utils.WeemailUtil;
import cn.mailchat.view.AttachmentView;
import cn.mailchat.view.MessageHeader;
import cn.mailchat.view.MessageTitleView;

public class MailDetialActivity extends BaseActionbarFragmentActivity implements MessageViewFragmentListener,
		OnItemClickListener {
	private static final String EXTRA_MESSAGE_REFERENCE = "message_reference";
	private static final String TAG_NOTIFY_MAIL_FLAG="isNotify";

    // 记录附件相关信息
    private static final String EXTRA_DOWNLOADING_ID = "downloading_id";
    private static final String EXTRA_NOTIFICATION_ID = "notification_id";
    private static final String EXTRA_ATTACHMENT_OP = "attachment_op";
    public static final int EXTRA_ATT_OP_OPEN = 0;
    public static final int EXTRA_ATT_OP_DOWNLOAD = 1;
    public static final int EXTRA_ATT_OP_CANCEL = 2;
    
    // 修复弹出对话框时Fragment未就绪问题
    private boolean mIsStateSaved = false;
    private boolean mHasPendingDialog = false;
    private Intent mIntent;
	
	private FragmentManager mFragmentManager;
	private View mActionButtonIndeterminateProgress;
	private View mCustomActionbarView;
	private Menu mMenu;

	private MessageViewFragment mMessageViewFragment;
	private View mActionBarMessageView;
	private ProgressBar mActionBarProgress;
	private MessageTitleView mActionBarSubject, mActionBarSubheading;
	private MessageReference mMessageReference;
	private Account mAccount;
	private String mFolderName;
	private LocalSearch mSearch;
	private boolean mSingleFolderMode;
	private boolean mSingleAccountMode;
	/**
	 * {@code true} if the message list should be displayed as flat list (i.e.
	 * no threading) regardless whether or not message threading was enabled in
	 * the settings. This is used for filtered views, e.g. when only displaying
	 * the unread messages in a folder.
	 */
	private boolean mNoThreading;
	private MenuAdapter mMenuAdapter;
	private ListPopupWindow mMenuWindow;
	private FragmentTransaction ft;
	private String email;
	private List<String> menuList;
	private NotificationCenter notificationCenter;
	public static void actionDisplayMailDetial(Context context,
			MessageReference messageReference) {
		context.startActivity(actionDisplayMessageIntent(context,
				messageReference));
	}

	public static Intent actionDisplayMessageIntent(Context context,
			MessageReference messageReference) {
		Intent intent = new Intent(context, MailDetialActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(EXTRA_MESSAGE_REFERENCE, messageReference);
		return intent;
	}

	public static Intent actionDisplayMessageIntentByNotify(Context context,
			MessageReference messageReference) {
		Intent intent = new Intent(context, MailDetialActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(EXTRA_MESSAGE_REFERENCE, messageReference);
		intent.putExtra(TAG_NOTIFY_MAIL_FLAG, 1);
		return intent;
	}
	
	// 生成附件相关对话框的Intent
	public static Intent actionAttachmentIntent(
			Context context,
			MessageReference messageReference,
			String downloadingId,
			int notificationId,
			int attachmentOp) {
		Intent intent = new Intent(context, MailDetialActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(EXTRA_MESSAGE_REFERENCE, messageReference);
		intent.putExtra(EXTRA_DOWNLOADING_ID, downloadingId);
		intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
		intent.putExtra(EXTRA_ATTACHMENT_OP, attachmentOp);
		return intent;
	}
	
	private boolean isSendFolder = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detial);
		initializeActionBar();
		initActionbarView();
		initData();
		if (!decodeExtras(getIntent())) {
			finish();
			return;
		}
		initFragment(savedInstanceState);
		notificationCenter = NotificationCenter.getInstance();
		MobclickAgent.onEvent(getApplicationContext(),"into_mail_detial_act");
	}

	private void initData() {
		menuList = new ArrayList<String>();
		menuList.add(getString(R.string.delete_action));
		menuList.add(getString(R.string.mark_as_unread_action));
		menuList.add(getString(R.string.compose_action));
		menuList.add(getString(R.string.message_view_eml_button));
		menuList.add(getString(R.string.message_view_reload_button));
	}

	private boolean decodeExtras(Intent intent) {
		showAttachmentDialog(intent);
		
		mMessageReference = intent.getParcelableExtra(EXTRA_MESSAGE_REFERENCE);
		Preferences prefs = Preferences.getPreferences(MailDetialActivity.this);
		mAccount = prefs.getAccount(mMessageReference.accountUuid);
		
		if (mAccount == null) {
			return false;
		}
		
		String folderName = mMessageReference.folderName;
		if (folderName.equals(mAccount.getSentFolderName())) {
			isSendFolder = true;
		} else {
			menuList.remove(0);
		}
		return true;
	}

	private void initializeActionBar() {
		// add by zhangjx start hide icon
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
		// add by zhangjx end
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setCustomView(R.layout.actionbar_custom);
		// mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayUseLogoEnabled(true);
		mCustomActionbarView = mActionBar.getCustomView();
		mActionButtonIndeterminateProgress = getLayoutInflater().inflate(
				R.layout.actionbar_indeterminate_progress_actionview, null);
		// 返回按钮
		mActionBar.setDisplayHomeAsUpEnabled(true);
	}

	private void initActionbarView() {
		// TODO Auto-generated method stub
		mActionBarMessageView = mCustomActionbarView
				.findViewById(R.id.actionbar_message_view);
		mActionBarSubject = (MessageTitleView) mCustomActionbarView
				.findViewById(R.id.message_title_view);
		// add by zhangjx
		mActionBarSubheading = (MessageTitleView) mCustomActionbarView
				.findViewById(R.id.message_title_view_sub);
		mActionBarProgress = (ProgressBar) mCustomActionbarView
				.findViewById(R.id.actionbar_progress);
		// add by zhangjx
		mActionBarMessageView.setVisibility(View.VISIBLE);
		mActionBarSubheading.setVisibility(View.VISIBLE);
	}

	private void initFragment(Bundle savedInstanceState) {
		mFragmentManager = this.getSupportFragmentManager();
		if (savedInstanceState == null) {
			ft = mFragmentManager.beginTransaction();

			mMessageViewFragment = MessageViewFragment.newInstance(
					mMessageReference, isSendFolder);
			ft.replace(R.id.detial_content, mMessageViewFragment,
					MessageViewFragment.DRAWER_MESSAGE_VIEW_TAG);
			ft.commit();
			configureMenu(mMenu);
			// mActionBar.setTitle(R.string.app_name);

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_mail_detial, menu);
		mMenu = menu;
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		configureMenu(menu);
		return true;
	}

	private void configureMenu(Menu menu) {
		if (menu == null) {
			return;
		}
		if (((mMessageViewFragment == null || !mMessageViewFragment
				.isInitialized()))) {
			menu.findItem(R.id.action_overflow).setVisible(false);

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			if (getIntent().getIntExtra(TAG_NOTIFY_MAIL_FLAG, 0) == 1) {
				MailChat.isMail=true;
				if (mAccount.getIsHaveUnreadMsg()) {
					mAccount.setmIsHaveUnreadMsg(false);
				}
//				mAccount.setName(mAccount.getEmail().substring(0, mAccount.getEmail().indexOf("@")));
				mAccount.save(Preferences.getPreferences(this));
				Preferences.getPreferences(this).setDefaultAccount(mAccount);
				ActivityManager.popAll();
				LocalSearch search = new LocalSearch();
				search.addAllowedFolder(Account.INBOX);
				search.addAccountUuid(mAccount.getUuid());
				Main4TabActivity.actionDisplaySearch(this, search, false, true);
			}
			
			// Modified by LL
			// BEGIN
			synchronized (MailChat.attachmentList) {
	    		Log.v(MailChat.LOG_TAG, String.format("%s attachments removed.", MailChat.attachmentList.size()));
	        	MailChat.attachmentList.clear();
	    	}
			// END
			
			finish();
			CommonUtils.hideSoftInput(this);
			return true;
		case R.id.action_overflow:
			View view = findViewById(itemId);
			showMoreOptionMenu(view);
			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	private void showMoreOptionMenu(View view) {
		mMenuWindow = new ListPopupWindow(this);
		if (mMenuAdapter == null) {
			mMenuAdapter = new MenuAdapter();
		}
		mMenuWindow.setModal(true);
		mMenuWindow.setContentWidth(getResources().getDimensionPixelSize(
				R.dimen.popo_menu_dialog_width));
		mMenuWindow.setAdapter(mMenuAdapter);
		mMenuWindow.setOnItemClickListener(this);
		mMenuWindow.setAnchorView(view);
		mMenuWindow.show();
	}

	class MenuAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return menuList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(
					R.layout.item_overflow_menu, null);
			TextView name = (TextView) convertView.findViewById(R.id.tv_name);
			name.setText(menuList.get(position));
			return convertView;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		if (isSendFolder) {
			switch (position) {
			case 0:
				mMessageViewFragment.onDelete();
				break;
			case 1:
				mMessageViewFragment.onToggleRead();
				break;
			case 2:
				// 跳转写信
				MailComposeActivity.actionCompose(MailDetialActivity.this, null, email);
				break;
			case 3:
			    mMessageViewFragment.saveMessageToEml();
			    break;
			case 4:
			    // 重新加载邮件
				mMessageViewFragment.onReload();
				break;
			}
		} else {
			switch (position) {
			case 0:
				mMessageViewFragment.onToggleRead();
				break;
			case 1:
				// 跳转写信
				MailComposeActivity.actionCompose(MailDetialActivity.this, null, email);
				break;
			case 2:
			    mMessageViewFragment.saveMessageToEml();
			    break;
			case 3:
			    // 重新加载邮件
				mMessageViewFragment.onReload();
				break;
			}
		}

		if (mMenuWindow != null) {
			mMenuWindow.dismiss();
			mMenuWindow = null;
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (ft!=null) {
			ft.remove(mMessageViewFragment);
			mMessageViewFragment = null;
		}
	}

	// #==MessageViewFragmentListener start==
	@Override
	public void updateMenu() {
		// TODO Auto-generated method stub
		invalidateOptionsMenu();
	}

	@Override
	public void showNextMessageOrReturn() {
		Toast.makeText(MailDetialActivity.this, R.string.delete_success,
				Toast.LENGTH_SHORT).show();
		MailDetialActivity.this.finish();
		// if (MailChat.messageViewReturnToList() || !showLogicalNextMessage())
		// {
		// if (MailChat.messageViewReturnToList()) {
		// }
	}

	@Override
	public void setProgress(boolean enable) {
		setProgressBarIndeterminateVisibility(enable);
	}

	@Override
	public void onReplyAll(Message message, PgpData pgpData) {
		// TODO Auto-generated method stub
		MailComposeActivity.actionReply(MailDetialActivity.this, mAccount,
				message, true, pgpData.getDecryptedData());
	}

	@Override
	public void onReply(Message message, PgpData pgpData) {
		// TODO Auto-generated method stub
		MailComposeActivity.actionReply(MailDetialActivity.this, mAccount,
				message, false, pgpData.getDecryptedData());
	}

	@Override
	public void onForward(Message mMessage, PgpData pgpData) {
		// TODO Auto-generated method stub
		MailComposeActivity.actionForward(MailDetialActivity.this, mAccount,
				mMessage, pgpData.getDecryptedData());
	}

	@Override
	public void messageHeaderViewAvailable(MessageHeader messageHeaderView) {
		mActionBarSubject.setMessageHeader(messageHeaderView);
		// add by zhangjx
		mActionBarSubheading.setMessageHeader(messageHeaderView);
	}

	@SuppressWarnings("null")
	@Override
	public void displayMessageSubject(CharSequence title, String fromEmail) {
		email = fromEmail;
		mActionBarSubject.setText(title);
		if (!fromEmail.equals("null")) {
			mActionBarSubheading.setText(fromEmail);
		}else {
			mActionBarSubheading.setVisibility(View.GONE);
		}
	}

	@Override
	public void disableDeleteAction() {

	}
	@Override
	public void onResendMessage(Message message) {
	     MailComposeActivity.actionEdit(this,
	    		 message.makeMessageReference(),
	    		 MailComposeActivity.SourceFolder.SENT);
	}
	// #==MessageViewFragmentListener end==
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if (getIntent().getIntExtra(TAG_NOTIFY_MAIL_FLAG, 0) == 1) {
			MailChat.isMail=true;
			if (mAccount.getIsHaveUnreadMsg()) {
				mAccount.setmIsHaveUnreadMsg(false);
			}
//			mAccount.setName(mAccount.getEmail().substring(0, mAccount.getEmail().indexOf("@")));
			mAccount.save(Preferences.getPreferences(this));
			Preferences.getPreferences(this).setDefaultAccount(mAccount);
			ActivityManager.popAll();
			LocalSearch search = new LocalSearch();
			search.addAllowedFolder(Account.INBOX);
			search.addAccountUuid(mAccount.getUuid());
			Main4TabActivity.actionDisplaySearch(this, search, false, true);
		}
		
		// Modified by LL
		// BEGIN
		synchronized (MailChat.attachmentList) {
    		Log.v(MailChat.LOG_TAG, String.format("%s attachments removed.", MailChat.attachmentList.size()));
        	MailChat.attachmentList.clear();
    	}
		// END
	}
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("MailDetialActivity"); //统计页面
		MobclickAgent.onResume(this);
		notificationCenter.notifyClean(mAccount);
		// 修复显示对话框时Fragment未就绪问题
		// Modified by LL
		// BEGIN
		mIsStateSaved = false;
	    if(mHasPendingDialog){
	        mHasPendingDialog = false;
	        showAttachmentDialog(mIntent);
	    }
	    // END
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("MailDetialActivity"); //统计页面
		MobclickAgent.onPause(this);
		
		// 修复显示对话框时Fragment未就绪问题
		// Modified by LL
		mIsStateSaved = true;
	}
	
	// MailDetailActivity已在顶部
	// Modified by LL
	// BEGIN
	@Override
	protected void onNewIntent(Intent intent) {
		showAttachmentDialog(intent);
	}
	// END
	
	// 附件对话框
	// Modified by LL
	// BEGIN
	private void showAttachmentDialog(Intent intent) {
		// 修复显示对话框时Fragment未就绪问题
		// Modified by LL
		// BEGIN
		if(mIsStateSaved){
	        mHasPendingDialog = true;
	        mIntent = intent;
	        return;
		}
		// END
		
		if (intent.hasExtra(EXTRA_DOWNLOADING_ID)) {
			String downloadingId = intent.getStringExtra(EXTRA_DOWNLOADING_ID);
			String notificationId = intent.getStringExtra(EXTRA_NOTIFICATION_ID);
			int attachmentOp = intent.getIntExtra(EXTRA_ATTACHMENT_OP, -1);
						
			AttachmentView av = null;
			synchronized(MailChat.downloadingList) {
				if (attachmentOp == EXTRA_ATT_OP_OPEN) {
					av = MailChat.downloadingList.remove(downloadingId);
				} else {
					av = MailChat.downloadingList.get(downloadingId);
				}
			}
			
			if (av != null) {
				DialogFragment dialog = new AttachmentDialogFragment(av, attachmentOp, downloadingId);
				dialog.show(getSupportFragmentManager(), notificationId);
			}
		}
	}
	
	private class AttachmentDialogFragment extends DialogFragment {
		
		private AttachmentView mView;
		private int mOp;
		private String mDownloadingId;
		private String mTitle;
		private String mText;
		private String mPositive;
		private String mNegative;
		
		public AttachmentDialogFragment(AttachmentView view, int attOp, String downloadingId) {
			super();
			mView = view;
			mOp = attOp;
			mDownloadingId = downloadingId;
			mText = view.mName;
			mPositive = MailDetialActivity.this.getString(R.string.okay_action);
			mNegative = MailDetialActivity.this.getString(R.string.cancel_action);
			
			switch(mOp) {
			case EXTRA_ATT_OP_OPEN:
				mTitle = MailDetialActivity.this.getString(R.string.attachment_op_open);
				break;
			case EXTRA_ATT_OP_DOWNLOAD:
				mTitle = MailDetialActivity.this.getString(R.string.attachment_op_retry);
				break;
			case EXTRA_ATT_OP_CANCEL:
				mTitle = MailDetialActivity.this.getString(R.string.attachment_op_cancel);
				break;
			}
		}
		
	    @Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setTitle(mTitle)
				.setMessage(mText)
				.setPositiveButton(mPositive, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(mOp) {
						case EXTRA_ATT_OP_OPEN:
							mView.openAttachment();
							break;
						case EXTRA_ATT_OP_DOWNLOAD:
							mView.open();
							break;
						case EXTRA_ATT_OP_CANCEL:
							mView.cancel();
							break;
						}
					}
					
				})
				.setNegativeButton(mNegative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(mOp) {
                        case EXTRA_ATT_OP_OPEN:
                            break;
                        case EXTRA_ATT_OP_DOWNLOAD:
                            synchronized(MailChat.downloadingList) {
                                MailChat.downloadingList.remove(mDownloadingId);
                            }
                            break;
                        case EXTRA_ATT_OP_CANCEL:
                            break;
                        }
                    }
				});
			return builder.create();
		}
	}
	// END
}
