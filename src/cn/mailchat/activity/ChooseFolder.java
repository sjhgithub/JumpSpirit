package cn.mailchat.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.setup.AccountSetupNameActivity;
import cn.mailchat.adapter.ChooseFolderAdapter;
import cn.mailchat.beans.ChooseFolderBean;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.mail.Folder;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.utils.NetUtil;

/**
 * 
 * @copyright © 35.com
 * @file name ：ChooseFolder.java
 * @author ：zhangjx
 * @create Data ：2014-10-29下午2:14:45
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-10-29下午2:14:45
 * @Modified by：zhangjx
 * @Description :长按邮件列表-移动--》选择邮件夹页面
 */
public class ChooseFolder extends MailChatListActivity implements
		OnItemClickListener, OnClickListener {
	public static final String EXTRA_ACCOUNT = "cn.mailchat.ChooseFolder_account";
	public static final String EXTRA_CUR_FOLDER = "cn.mailchat.ChooseFolder_curfolder";
	public static final String EXTRA_SEL_FOLDER = "cn.mailchat.ChooseFolder_selfolder";
	public static final String EXTRA_NEW_FOLDER = "cn.mailchat.ChooseFolder_newfolder";
	public static final String EXTRA_MESSAGE = "cn.mailchat.ChooseFolder_message";
	public static final String EXTRA_SHOW_CURRENT = "cn.mailchat.ChooseFolder_showcurrent";
	public static final String EXTRA_SHOW_FOLDER_NONE = "cn.mailchat.ChooseFolder_showOptionNone";
	public static final String EXTRA_SHOW_DISPLAYABLE_ONLY = "cn.mailchat.ChooseFolder_showDisplayableOnly";
	public static final String EXTRA_IS_SHOW_CHECKBOX = "isShowCheckbox";
	public static final int CHOOSE_PUSH_FOLDER = 1001;
	public static final String EXTRA_SECONT_TITLE = "extra_second_title";
	private String mFolder;
	private String mSelectFolder;
	private Account mAccount;
	private MessageReference mMessageReference;
	private ChooseFolderAdapter mAdapter;
	private ChooseFolderHandler mHandler = new ChooseFolderHandler();
	private String mHeldInbox = null;
	private boolean mHideCurrentFolder = true;
	private boolean mShowOptionNone = false;
	private boolean mShowDisplayableOnly = false;
	private List<ChooseFolderBean> newFolderList;
	/**
	 * What folders to display.<br/>
	 * Initialized to whatever is configured but can be overridden via
	 * {@link #onOptionsItemSelected(MenuItem)} while this activity is showing.
	 */
	private Account.FolderMode mMode;
	private CheckBox cb_msg_notify_all_sigle_chat,
			cb_msg_notify_all_group_chat;
	private LinearLayout layout_msg_notify_all_sigle_chat,
			layout_msg_notify_all_group_chat;
	/**
	 * Current filter used by our ArrayAdapter.<br/>
	 * Created on the fly and invalidated if a new set of folders is chosen via
	 * {@link #onOptionsItemSelected(MenuItem)}
	 */
	private FolderListFilter<String> mMyFilter = null;
	private boolean isShowCheckbox;
	private MessagingController mController;
	private boolean isInboxChosed;
	private boolean isSpamChosed;
	private int coustonFolderTotalCount=0;
	private int coustomFolder=0;
	public static void displayFolderChoice(Activity activity, Account account) {
		Intent intent = new Intent(activity,ChooseFolder.class);
		intent.putExtra(ChooseFolder.EXTRA_ACCOUNT, account.getUuid());
		intent.putExtra(ChooseFolder.EXTRA_SEL_FOLDER,
				account.getLastSelectedFolderName());
		intent.putExtra(ChooseFolder.EXTRA_SHOW_CURRENT, "yes");
		intent.putExtra(ChooseFolder.EXTRA_IS_SHOW_CHECKBOX, true);
		activity.startActivityForResult(intent, CHOOSE_PUSH_FOLDER);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置无标题
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_content_simple);
		decodeArguments();
		initTitleBar();
		initView();
		initNotifySettingTop();
		initData();
		setListener();

	}

	private void decodeArguments() {
		Intent intent = getIntent();
		String accountUuid = intent.getStringExtra(EXTRA_ACCOUNT);
		mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
		mMessageReference = intent.getParcelableExtra(EXTRA_MESSAGE);
		mFolder = intent.getStringExtra(EXTRA_CUR_FOLDER);
		isShowCheckbox = intent.getBooleanExtra(EXTRA_IS_SHOW_CHECKBOX, false);
		mSelectFolder = intent.getStringExtra(EXTRA_SEL_FOLDER);
		if (intent.getStringExtra(EXTRA_SHOW_CURRENT) != null) {
			mHideCurrentFolder = false;
		}
		if (intent.getStringExtra(EXTRA_SHOW_FOLDER_NONE) != null) {
			mShowOptionNone = true;
		}
		if (intent.getStringExtra(EXTRA_SHOW_DISPLAYABLE_ONLY) != null) {
			mShowDisplayableOnly = true;
		}
		if (mFolder == null) {
			mFolder = "";
		}
	}

	private void setListener() {
		this.getListView().setOnItemClickListener(this);
	}

	private void initData() {
		// TODO Auto-generated method stub

		newFolderList = new ArrayList<ChooseFolderBean>();

		// mAdapter = new ArrayAdapter<String>(this,
		// android.R.layout.simple_list_item_1) {
		// private Filter myFilter = null;
		//
		// @Override
		// public Filter getFilter() {
		// if (myFilter == null) {
		// myFilter = new FolderListFilter<String>(this);
		// }
		// return myFilter;
		// }
		// };
		mAdapter = new ChooseFolderAdapter(ChooseFolder.this, mAccount,isShowCheckbox);
		setListAdapter(mAdapter);

		mMode = mAccount.getFolderTargetMode();
		mController=MessagingController.getInstance(getApplication());
		mController.listFolders(mAccount,
				false, mListener);
		mController.getCoustomFoldersCount(mAccount, mListener);
	}

	private void initView() {
		getListView().setFastScrollEnabled(false);
		getListView().setItemsCanFocus(false);
		getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);

	}

	private void initNotifySettingTop() {
		if (isShowCheckbox) {
			View notifySettingTopView = LayoutInflater.from(this).inflate(
					R.layout.include_notify_setting_top, null);
			getListView().addHeaderView(notifySettingTopView, null, false);
			LinearLayout layout_msg_notify_all_sigle_chat = (LinearLayout) notifySettingTopView
					.findViewById(R.id.layout_msg_notify_all_sigle_chat);
			LinearLayout layout_msg_notify_all_group_chat = (LinearLayout) notifySettingTopView
					.findViewById(R.id.layout_msg_notify_all_group_chat);
			cb_msg_notify_all_sigle_chat = (CheckBox) notifySettingTopView
					.findViewById(R.id.cb_msg_notify_all_sigle_chat);
			cb_msg_notify_all_group_chat = (CheckBox) notifySettingTopView
					.findViewById(R.id.cb_msg_notify_all_group_chat);
			layout_msg_notify_all_sigle_chat.setOnClickListener(this);
			layout_msg_notify_all_group_chat.setOnClickListener(this);
			cb_msg_notify_all_sigle_chat.setChecked(mAccount.ismIsAllowAllSigleChatNotify());
			cb_msg_notify_all_group_chat.setChecked(mAccount.ismIsAllowAllGroupChatNotify());
		}
	}

	private void initTitleBar() {
		ImageView imgBack = (ImageView) findViewById(R.id.back);
		TextView tvTitle = (TextView) findViewById(R.id.title);
		TextView tvSecTitle = (TextView) findViewById(R.id.tv_sec_title);
		TextView tvSure = (TextView) findViewById(R.id.tv_sure);
	
		tvTitle.setText(getString(R.string.choose_folder_title));
		imgBack.setOnClickListener(this);
		if (isShowCheckbox) {
			tvSecTitle.setVisibility(View.VISIBLE);
			tvSecTitle.setText(mAccount.getEmail());
			tvTitle.setText(getString(R.string.title_notify_range));
			tvSure.setVisibility(View.GONE);
			tvSure.setOnClickListener(this);
		}
	}

	class ChooseFolderHandler extends Handler {
		private static final int MSG_PROGRESS = 1;
		private static final int MSG_SET_SELECTED_FOLDER = 2;

		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_PROGRESS: {
				setProgressBarIndeterminateVisibility(msg.arg1 != 0);
				break;
			}
			case MSG_SET_SELECTED_FOLDER: {
				getListView().setSelection(msg.arg1);
				break;
			}
			}
		}

		public void progress(boolean progress) {
			android.os.Message msg = new android.os.Message();
			msg.what = MSG_PROGRESS;
			msg.arg1 = progress ? 1 : 0;
			sendMessage(msg);
		}

		public void setSelectedFolder(int position) {
			android.os.Message msg = new android.os.Message();
			msg.what = MSG_SET_SELECTED_FOLDER;
			msg.arg1 = position;
			sendMessage(msg);
		}
	}

	/*
	 * 临时注释掉菜单
	 * 
	 * @Override public boolean onCreateOptionsMenu(Menu menu) {
	 * super.onCreateOptionsMenu(menu);
	 * getMenuInflater().inflate(R.menu.folder_select_option, menu);
	 * configureFolderSearchView(menu); return true; }
	 * 
	 * private void configureFolderSearchView(Menu menu) { final MenuItem
	 * folderMenuItem = menu.findItem(R.id.filter_folders); final SearchView
	 * folderSearchView = (SearchView) folderMenuItem.getActionView();
	 * folderSearchView
	 * .setQueryHint(getString(R.string.folder_list_filter_hint));
	 * folderSearchView.setOnQueryTextListener(new
	 * SearchView.OnQueryTextListener() {
	 * 
	 * @Override public boolean onQueryTextSubmit(String query) {
	 * folderMenuItem.collapseActionView(); return true; }
	 * 
	 * @Override public boolean onQueryTextChange(String newText) {
	 * mAdapter.getFilter().filter(newText); return true; } }); }
	 * 
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { switch
	 * (item.getItemId()) { case R.id.display_1st_class: {
	 * setDisplayMode(FolderMode.FIRST_CLASS); return true; } case
	 * R.id.display_1st_and_2nd_class: {
	 * setDisplayMode(FolderMode.FIRST_AND_SECOND_CLASS); return true; } case
	 * R.id.display_not_second_class: {
	 * setDisplayMode(FolderMode.NOT_SECOND_CLASS); return true; } case
	 * R.id.display_all: { setDisplayMode(FolderMode.ALL); return true; } case
	 * R.id.list_folders: { onRefresh(); return true; } default: { return
	 * super.onOptionsItemSelected(item); } } }
	 * 
	 * private void onRefresh() {
	 * MessagingController.getInstance(getApplication()).listFolders(mAccount,
	 * true, mListener); }
	 * 
	 * private void setDisplayMode(FolderMode aMode) { mMode = aMode; //
	 * invalidate the current filter as it is working on an inval if (mMyFilter
	 * != null) { mMyFilter.invalidate(); } //re-populate the list
	 * MessagingController.getInstance(getApplication()).listFolders(mAccount,
	 * false, mListener); }
	 */

	private MessagingListener mListener = new MessagingListener() {
		@Override
		public void listFoldersStarted(Account account) {
			if (!account.equals(mAccount)) {
				return;
			}
			mHandler.progress(true);
		}

		@Override
		public void listFoldersFailed(Account account, String message) {
			if (!account.equals(mAccount)) {
				return;
			}
			mHandler.progress(false);
		}

		@Override
		public void listFoldersFinished(Account account) {
			if (!account.equals(mAccount)) {
				return;
			}
			mHandler.progress(false);
		}

		@Override
		public void listFolders(final Account account, Folder[] folders) {
			if (!account.equals(mAccount)) {
				return;
			}
			Account.FolderMode aMode = mMode;
			Preferences prefs = Preferences.getPreferences(getApplication()
					.getApplicationContext());

			List<ChooseFolderBean> newFolders = new ArrayList<ChooseFolderBean>();
			List<ChooseFolderBean> topFolders = new ArrayList<ChooseFolderBean>();

			for (Folder folder : folders) {
				ChooseFolderBean folderBean =new ChooseFolderBean();
				String name = folder.getName();
				boolean isAllowPush = folder.isAllowPush();
				boolean isCustomFolder = folder.isCustomFolder();
				// Inbox needs to be compared case-insensitively
				if (mHideCurrentFolder
						&& (name.equals(mFolder) || (mAccount
								.getInboxFolderName().equalsIgnoreCase(mFolder) && mAccount
								.getInboxFolderName().equalsIgnoreCase(name)))) {
					continue;
				}
				try {
					folder.refresh(prefs);
					Folder.FolderClass fMode = folder.getDisplayClass();

					if ((aMode == Account.FolderMode.FIRST_CLASS && fMode != Folder.FolderClass.FIRST_CLASS)
							|| (aMode == Account.FolderMode.FIRST_AND_SECOND_CLASS
									&& fMode != Folder.FolderClass.FIRST_CLASS && fMode != Folder.FolderClass.SECOND_CLASS)
							|| (aMode == Account.FolderMode.NOT_SECOND_CLASS && fMode == Folder.FolderClass.SECOND_CLASS)) {
						continue;
					}
				} catch (MessagingException me) {
					Log.e(MailChat.LOG_TAG,
							"Couldn't get prefs to check for displayability of folder "
									+ folder.getName(), me);
				}
				folderBean.setFolderName(name);
				folderBean.setAllowPush(isAllowPush);
				folderBean.setCustomFolder(isCustomFolder);
				if (folder.isInTopGroup()) {
					topFolders.add(folderBean);
				} else {
					newFolders.add(folderBean);
				}
			}

			final Comparator<String> comparator = new Comparator<String>() {
				@Override
				public int compare(String s1, String s2) {
					int ret = s1.compareToIgnoreCase(s2);
					return (ret != 0) ? ret : s1.compareTo(s2);
				}
			};

			// Collections.sort(topFolders, comparator);
			// Collections.sort(newFolders, comparator);

			List<ChooseFolderBean> localFolders = new ArrayList<ChooseFolderBean>(newFolders.size()
					+ topFolders.size() + ((mShowOptionNone) ? 1 : 0));

			if (mShowOptionNone) {
				ChooseFolderBean  bean=new ChooseFolderBean();
				bean.setFolderName(MailChat.FOLDER_NONE);
				localFolders.add(bean);
			}

			localFolders.addAll(topFolders);
			localFolders.addAll(newFolders);

			int selectedFolder = -1;

			/*
			 * We're not allowed to change the adapter from a background thread,
			 * so we collect the folder names and update the adapter in the UI
			 * thread (see finally block).
			 */
			final List<ChooseFolderBean> folderList = new ArrayList<ChooseFolderBean>();
			try {
				int position = 0;
				for (ChooseFolderBean folderBean : localFolders) {
					String name=folderBean.getFolderName();
					if (mAccount.getInboxFolderName().equalsIgnoreCase(name)) {
//						folderBean.setFolderName(getString(R.string.special_mailbox_name_inbox));
//						folderList
//								.add(folderBean);
						mHeldInbox = name;
					} 
//					else if (!MailChat.ERROR_FOLDER_NAME.equals(name)
//							&& !account.getOutboxFolderName().equals(name)) {
//						folderList.add(folderBean);
//					}
					folderList.add(folderBean);
					if (mSelectFolder != null) {
						/*
						 * Never select EXTRA_CUR_FOLDER (mFolder) if
						 * EXTRA_SEL_FOLDER (mSelectedFolder) was provided.
						 */

						if (name.equals(mSelectFolder)) {
							selectedFolder = position;
						}
					} else if (name.equals(mFolder)
							|| (mAccount.getInboxFolderName().equalsIgnoreCase(
									mFolder) && mAccount.getInboxFolderName()
									.equalsIgnoreCase(name))) {
						selectedFolder = position;
					}
					position++;
				}
			} finally {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// Now we're in the UI-thread, we can safely change the
						// contents of the adapter.
						newFolderList.clear();
						//更新folder表中isAllowPush值
						for (ChooseFolderBean folder : folderList) {
							// boolean
							// isAllowPush=mController.getFolderPushState(mAccount,
							// folderName, null);
							// if (mAccount.getVersion_35Mail() == 1
							// && mAccount.getVersion_35Mail() == 2) {
							// if (mAccount.isSpecialFolder(folderName)) {
							// mController.updateFolderPushState(mAccount,folderName,null);
							// }
							// }
							if (isShowCheckbox) {
								if (!isFolderNoDisplay(account,
										folder.getFolderName())) {
									newFolderList.add(addFolders(folder));
								}
							} else {
								newFolderList.add(addFolders(folder));
							}

						}

						mAdapter.setFolderLists(newFolderList);
						mAdapter.notifyDataSetChanged();

						/*
						 * Only enable the text filter after the list has been
						 * populated to avoid possible race conditions because
						 * our FolderListFilter isn't really thread-safe.
						 */
						getListView().setTextFilterEnabled(true);
					}
				});
			}

			if (selectedFolder != -1) {
				mHandler.setSelectedFolder(selectedFolder);
			}
		}
		public void getCoustomFoldersCountFinished(Account account,int count){
			if (account.getEmail().equals(mAccount.getEmail())) {
				coustonFolderTotalCount=count;
			}
		}
	};

	private ChooseFolderBean addFolders(ChooseFolderBean folder) {
		ChooseFolderBean bean = new ChooseFolderBean();
		String folderName = folder.getFolderName();
		bean.setFolderName(folderName);
		bean.setCustomFolder(folder.isCustomFolder());
		// Log.d("qxian", "--folderName--"+folderName
		// +"--folder.isCustomFolder()--"+folder.isCustomFolder());
		if (folder.isCustomFolder() && folder.isAllowPush()) {
			++coustomFolder;
		}
		if (folder.isAllowPush()) {
			if (folderName.equals(mAccount.getInboxFolderName())) {
				isInboxChosed = true;
			} else if (folderName.equals(mAccount.getSpamFolderName())) {
				isSpamChosed = true;
			}

			mAdapter.getmSelectedFolders().add(bean);
		} else {
			if (folderName.equals(mAccount.getInboxFolderName())) {
				isInboxChosed = false;
			} else if (folderName.equals(mAccount.getSpamFolderName())) {
				isSpamChosed = false;
			}
			mAdapter.getmSelectedFolders().remove(bean);
		}
		return bean;
	}

	private boolean isFolderNoDisplay(Account account, String folderName) {
		return (folderName != null
				&& folderName.equals(account.getDraftsFolderName())
				|| folderName.equals(account.getOutboxFolderName()) ||
				// 已刪除
				folderName.equals(account.getTrashFolderName()) || folderName
					.equals(account.getSentFolderName()));
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (!isShowCheckbox) {
			moveChooiseSucc(position);
		} else {
			pushSettingSucc(view, position);
		}
	}

	protected void pushSettingSucc(View view, int position) {

		if (getListView().getHeaderViewsCount() > 0) {
			position = position - getListView().getHeaderViewsCount();
		}
		CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb_choose_folder);
		if (checkBox.isChecked()) {
			mAdapter.getmSelectedFolders().remove(
					mAdapter.getmFolderList().get(position));
			String folderName = mAdapter.getmFolderList().get(position)
					.getFolderName();
			boolean isCoustomFolder = mAdapter.getmFolderList().get(position)
					.isCustomFolder();
			if ("INBOX".equalsIgnoreCase(folderName)
					|| folderName
							.equals(getString(R.string.special_mailbox_name_inbox))) {
				folderName = "INBOX";
				isInboxChosed = false;
			}
			if (folderName
					.equals(mAccount.getSpamFolderName())) {
				isSpamChosed=false;
			}
			if (isCoustomFolder) {
				--coustomFolder;
			}

			mController
					.updateFolderPushState(mAccount, folderName, false, null);
			mAccount.setmIsSetInboxNotify(false);
			checkBox.setChecked(false);
		} else {
			mAdapter.getmSelectedFolders().add(
					mAdapter.getmFolderList().get(position));
			String folderName = mAdapter.getmFolderList().get(position)
					.getFolderName();
			boolean isCoustomFolder = mAdapter.getmFolderList().get(position)
					.isCustomFolder();
			if ("INBOX".equalsIgnoreCase(folderName)
					|| folderName
							.equals(getString(R.string.special_mailbox_name_inbox))) {
				folderName = "INBOX";
				isInboxChosed = true;
			}
			if (folderName
					.equals(mAccount.getSpamFolderName())) {
				isSpamChosed=true;
			}
			if (isCoustomFolder) {
				++coustomFolder;
			}

			mController.updateFolderPushState(mAccount, folderName, true, null);
			checkBox.setChecked(true);
		}
		mAdapter.notifyDataSetChanged();
	}

	private String setScopeSecontTitle() {
		String resultStr="";
		StringBuffer sb = new StringBuffer();
		if (isInboxChosed) {
			sb.append(getString(R.string.special_mailbox_name_inbox));
			sb.append("\\");
		}
		if (isSpamChosed) {
			sb.append(getString(R.string.special_mailbox_name_spam));
			sb.append("\\");
		}
		if (mAccount.ismIsAllowAllSigleChatNotify()) {
			sb.append(getString(R.string.setting_account_notifation_scope_second_title_sign));
			sb.append("\\");
		}
		if (mAccount.ismIsAllowAllGroupChatNotify()) {
			sb.append(getString(R.string.setting_account_notifation_scope_second_title_group));
			sb.append("\\");
		}
		if (coustomFolder==coustonFolderTotalCount) {
			sb.append(getString(R.string.setting_account_notifation_scope_second_title_all_coustom));
			sb.append("\\");
		}else if (coustomFolder!=0) {
			sb.append(getString(R.string.setting_account_notifation_scope_second_title_part_coustom));
			sb.append("\\");
		}
		if (!TextUtils.isEmpty(sb.toString())&&sb.toString().endsWith("\\")) {
			String string=sb.toString();
			 resultStr=string.substring(0,string.length()-1);
		}
		return resultStr;
	}
	private void moveChooiseSucc(int position) {
		Intent result = new Intent();
		result.putExtra(EXTRA_ACCOUNT, mAccount.getUuid());
		result.putExtra(EXTRA_CUR_FOLDER, mFolder);
		// String destFolderName = ((TextView) view).getText()
		// .toString();
		String destFolderName = newFolderList.get(position).getFolderName();
		if (mHeldInbox != null
				&& getString(R.string.special_mailbox_name_inbox).equals(
						destFolderName)) {
			destFolderName = mHeldInbox;
		}
		result.putExtra(EXTRA_NEW_FOLDER, destFolderName);
		result.putExtra(EXTRA_MESSAGE, mMessageReference);
		setResult(RESULT_OK, result);
		finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			if (isShowCheckbox) {
				Intent intent=new Intent();
				intent.putExtra(EXTRA_SECONT_TITLE, setScopeSecontTitle());
			    setResult(RESULT_OK, intent);
			    finish();
			}
			finish();
			break;
		case R.id.tv_sure:
			break;
		case R.id.layout_msg_notify_all_sigle_chat:
			// 所有单聊消息
			cb_msg_notify_all_sigle_chat
					.setChecked(!cb_msg_notify_all_sigle_chat.isChecked());
			mAccount.setmIsAllowAllSigleChatNotify(cb_msg_notify_all_sigle_chat.isChecked());
			break;
		case R.id.layout_msg_notify_all_group_chat:
			// 所有群聊消息
			cb_msg_notify_all_group_chat
					.setChecked(!cb_msg_notify_all_group_chat.isChecked());
			mAccount.setmIsAllowAllGroupChatNotify(cb_msg_notify_all_group_chat.isChecked());
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isShowCheckbox) {
				Intent intent=new Intent();
				intent.putExtra(EXTRA_SECONT_TITLE, setScopeSecontTitle());
			    setResult(RESULT_OK, intent);
			    finish();
			}
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}
}
