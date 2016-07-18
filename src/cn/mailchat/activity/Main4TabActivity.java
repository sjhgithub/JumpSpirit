package cn.mailchat.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.AccountStats;
import cn.mailchat.BaseAccount;
import cn.mailchat.GlobalConstants;
import cn.mailchat.LogCollector;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.setup.AccountSetupIncoming;
import cn.mailchat.activity.setup.AccountSetupNameActivity;
import cn.mailchat.activity.setup.SelectEmailActivity;
import cn.mailchat.adapter.AccountAdapter;
import cn.mailchat.adapter.AccountAdapter.AccountAdapterListener;
import cn.mailchat.adapter.CustomViewPagerAdatper;
import cn.mailchat.chatting.protocol.Connection;
import cn.mailchat.chatting.protocol.Connection.ConnectionStatus;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.controller.NotificationCenter;
import cn.mailchat.drag.ViewDragHelper;
import cn.mailchat.drag.ViewDragWare;
import cn.mailchat.fragment.ChatListFragment;
import cn.mailchat.fragment.FolderListFragment;
import cn.mailchat.fragment.FolderListFragment.FolderListListener;
import cn.mailchat.fragment.MeSettingFragment;
import cn.mailchat.fragment.MessageListFragment;
import cn.mailchat.fragment.MessageListFragment.MessageListFragmentListener;
import cn.mailchat.fragment.WorkSpaceFragment;
import cn.mailchat.fragment.WorkSpaceFragment.WorkSpaceFragmentListener;
import cn.mailchat.fragment.contact.ContactTabsFragment;
import cn.mailchat.fragment.contact.ContactTabsFragment.ContactTabsFragmentListener;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.mail.Folder;
import cn.mailchat.mail.Message;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.preferences.Storage;
import cn.mailchat.search.LocalSearch;
import cn.mailchat.search.SearchAccount;
import cn.mailchat.search.SearchSpecification;
import cn.mailchat.search.SearchSpecification.Attribute;
import cn.mailchat.search.SearchSpecification.SearchCondition;
import cn.mailchat.search.SearchSpecification.Searchfield;
import cn.mailchat.service.MqttConnStatusReceiver;
import cn.mailchat.service.MqttConnStatusReceiver.StatusHandler;
import cn.mailchat.utils.ActivityManager;
import cn.mailchat.utils.EncryptUtil;
import cn.mailchat.utils.NetUtil;
import cn.mailchat.utils.SystemUtil;
import cn.mailchat.utils.Utility;
import cn.mailchat.utils.WeemailUtil;
import cn.mailchat.view.ActionBarDrawerToggle;
import cn.mailchat.view.BadgeView;
import cn.mailchat.view.CustomViewPager;
import cn.mailchat.view.DrawerArrowDrawable;
import cn.mailchat.view.MailDialog;
import cn.mailchat.view.OverflowMenuPopo;
import cn.mailchat.view.OverflowMenuPopo.OverflowMenuPopoListener;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengDialogButtonListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateStatus;

/**
 * 
 * @copyright © 35.com
 * @file name ：Main4TabActivity.java
 * @author ：zhangjx
 * @create Data ：2014-9-25下午3:50:17
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-9-25下午3:50:17
 * @Modified by：zhangjx
 * @Description :主界面
 */
public class Main4TabActivity extends BaseActionbarFragmentActivity implements
		FolderListListener, MessageListFragmentListener,
		AccountAdapterListener, ContactTabsFragmentListener, StatusHandler,
		WorkSpaceFragmentListener {
	private static final String TAG = "Main4TabActivity";
	// for this activity
	private static final String EXTRA_SEARCH = "search";
	private static final String EXTRA_NO_THREADING = "no_threading";

	private static final String ACTION_SHORTCUT = "shortcut";
	private static final String EXTRA_SPECIAL_FOLDER = "special_folder";

	private static final String EXTRA_MESSAGE_REFERENCE = "message_reference";
	public static final String EXTRA_SEARCH_ACCOUNT = "cn.mailchat.search_account";
	private static final String EXTRA_SEARCH_FOLDER = "cn.mailchat.search_folder";
	public static final String EXTRA_ACCOUNT = "account";
	private static final String EXTRA_IS_SHOW_MENU = "isShowMenu";
	private static final String IS_SHOW_DIALOG = "is_dialog_shown";
	private static String ACCOUNT_STATS = "accountStats";
	private static String STATE_UNREAD_COUNT = "unreadCount";
	private static final int SPECIAL_ACCOUNTS_COUNT = 2;

	private enum DisplayMode {
		MESSAGE_LIST, MESSAGE_VIEW, SPLIT_VIEW
	}

	public static final int INDEX_MESSAGE = 0;
	public static final int INDEX_WORK_SPACE = 1;
	public static final int INDEX_MAIL = 2;
	public static final int INDEX_CONTACT = 3;
	public static final int INDEX_SETTING = 4;
	int[] tabFalg = { INDEX_MESSAGE, INDEX_WORK_SPACE, INDEX_MAIL,
			INDEX_CONTACT, INDEX_SETTING };
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerArrowDrawable drawerArrow;
	private View mActionButtonIndeterminateProgress;
	private View mCustomActionbarView;
	private Menu mMenu;

	private ProgressBar mActionBarProgress;
	private TextView mActionBarTitle;
	private TextView mActionBarSubTitle;

	private DisplayMode mDisplayMode;
	private MessageReference mMessageReference;
	private Account mAccount;
	private String mFolderName;
	private boolean mSingleFolderMode;
	private boolean mSingleAccountMode;
	/**
	 * {@code true} if the message list should be displayed as flat list (i.e.
	 * no threading) regardless whether or not message threading was enabled in
	 * the settings. This is used for filtered views, e.g. when only displaying
	 * the unread messages in a folder.
	 */
	private boolean mNoThreading;
	private boolean isWorkspaceTab = false;
	private boolean isMailListTab = false;
	private boolean isMessageTab = false;
	private boolean isContactTab = false;
	private boolean isMeTab = false;
	private boolean isSetTitle = true;
	private long mLastExitTime;
	private String receiveEmail = "";
	public static final String DRAWER_CONTENT_TAG = "cn.mailchat.fragment.reightContentFragment";

	private static String ARG_SEARCH = "search";
	public CustomViewPager viewPager;

	private ImageView mImgTabMail, mImgTabMessage, mImgTabContant, mImgTabMe,
			mImgTabWorkspace;
	private LinearLayout tabMailLayout, tabMessageLayout, tabContactLayout,
			tabMeLayout, tabWorkspaceLayout, layoutChangeAccount;
	private TextView mTvTabMail, mTvTabMessage, mTvTabContant, mTvTabMe,
			mTvTabWorkspace;
	private int currIndex = INDEX_MESSAGE;// 当前页卡编号
	private FragmentManager mFragmentManager;
	private BadgeView badgeMail, badgeMsg, badgeSetting, badgeWorkspace;
	private MessageListFragment mMessageListFragment;
	private LocalSearch mSearch;
	private int mFirstBackStackId = -1;
	private CustomViewPagerAdatper viewPagerAdapter;
	private String mCurrentContentTag;
	private LinearLayout mainFooterMenuLayout;
	private SparseArray<Fragment> fragmentList;
	private String mailFolderName;
	private NotificationCenter notificationCenter;

	private LinearLayout noNetLinearLayoutView;
	private TextView setNetTextView;
	private FolderListFragment mFolderListFragment;
	int[] popoLocation = new int[2];
	private LayoutInflater inflater;
	private PopupWindow popupWindow;// 点击帐号区域弹出的窗口，用户列表
	// 隐藏功能触发条件
	// Modified by LL
	// BEGIN
	private int mHiddenFeaturesClickCount = 1;
	private long mHiddenFeaturesClickTime = 0;
	// END
	private String[] mUpdateModeArray;
	private MessagingController mController;
	private ConcurrentHashMap<String, AccountStats> accountStats = new ConcurrentHashMap<String, AccountStats>();
	private ConcurrentMap<BaseAccount, String> pendingWork = new ConcurrentHashMap<BaseAccount, String>();
	// NoPush
	private RelativeLayout noPushLy, authenticateLy;
	private TextView noPushTv, authenticateTv;
	private ImageView closeNoPushIv;
	private Handler messageHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			int unread = msg.what;
			if (unread > 0) {
				String unreadCount = unread > 99 ? getString(R.string.large_size)
						: Integer.toString(unread);
				badgeMsg.setText(unreadCount);
				badgeMsg.show();
			} else {
				badgeMsg.setText("");
				badgeMsg.hide();
			}
		}

	};

	class AccountsHandler extends Handler {

		public void dataChanged() {
			runOnUiThread(new Runnable() {
				public void run() {
					if (mAccountAdapter != null) {
						mAccountAdapter.notifyDataSetInvalidated();
					}
				}
			});
		}

		public void workingAccount(final Account account, final int res) {
			runOnUiThread(new Runnable() {
				public void run() {
					String toastText = getString(res, account.getDescription());

					Toast toast = Toast.makeText(Main4TabActivity.this,
							toastText, Toast.LENGTH_SHORT);
					toast.show();
				}
			});
		}

	}

	private ListView mAccountListView;
	private List<BaseAccount> accounts;
	private AccountAdapter mAccountAdapter;
	private String defaultAccountEmail;
	private int mUnreadMessageCount = 0;
	private MqttConnStatusReceiver statusReceiver;
	private AccountsHandler mAccountsHandler = new AccountsHandler();

	public static Intent actionHandleAccountIntent(Context context,
			Account account, boolean fromShortcut) {
		Intent intent = new Intent(context, Main4TabActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(EXTRA_ACCOUNT, account.getUuid());

		if (fromShortcut) {
			intent.putExtra(EXTRA_IS_SHOW_MENU, true);
		}

		return intent;
	}

	public static void actionDisplaySearch(Context context,
			SearchSpecification search, boolean noThreading, boolean newTask) {
		actionDisplaySearch(context, search, noThreading, newTask, true);
	}

	public static void actionDisplaySearch(Context context,
			SearchSpecification search, boolean noThreading, boolean newTask,
			boolean clearTop) {
		context.startActivity(intentDisplaySearch(context, search, noThreading,
				newTask, clearTop));
	}

	public static Intent intentDisplaySearch(Context context,
			SearchSpecification search, boolean noThreading, boolean newTask,
			boolean clearTop) {
		Intent intent = new Intent(context, Main4TabActivity.class);
		intent.putExtra(EXTRA_SEARCH, search);
		intent.putExtra(EXTRA_NO_THREADING, noThreading);

		if (clearTop) {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
		if (newTask) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}

		return intent;
	}

	public static Intent shortcutIntent(Context context, String specialFolder) {
		Intent intent = new Intent(context, Main4TabActivity.class);
		intent.setAction(ACTION_SHORTCUT);
		intent.putExtra(EXTRA_SPECIAL_FOLDER, specialFolder);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		return intent;
	}

	public static Intent actionDisplayMessageIntent(Context context,
			MessageReference messageReference) {
		Intent intent = new Intent(context, Main4TabActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(EXTRA_MESSAGE_REFERENCE, messageReference);
		return intent;
	}

	private void updateMenu(int tabPosition) {
		// TODO Auto-generated method stub
		switch (tabPosition) {
		// 邮件。。。。
		case INDEX_MAIL:
			isMailListTab = true;
			isMessageTab = false;
			isContactTab = false;
			isMeTab = false;
			isWorkspaceTab = false;
			setIsShowLeftMenu(true);
			// mActionBarTitle.setText(FolderInfoHolder.getDisplayName(
			// Main4TabActivity.this, mAccount, mFolderName));
			setActionBarTitle(mailFolderName);
			break;
		case INDEX_MESSAGE:
			isSetTitle = false;
			isMessageTab = true;
			isMailListTab = false;
			isContactTab = false;
			isMeTab = false;
			isWorkspaceTab = false;
			setIsShowLeftMenu(false);
			setActionBarTitle(getString(R.string.main_tab_message));
			break;
		case INDEX_CONTACT:
			isSetTitle = false;
			isContactTab = true;
			isMailListTab = false;
			isMessageTab = false;
			isMeTab = false;
			isWorkspaceTab = false;
			setIsShowLeftMenu(false);
			setActionBarTitle(getString(R.string.main_tab_contant));
			break;
		case INDEX_SETTING:
			isSetTitle = false;
			isMeTab = true;
			isMailListTab = false;
			isMessageTab = false;
			isContactTab = false;
			isWorkspaceTab = false;
			setIsShowLeftMenu(false);
			setActionBarTitle(getString(R.string.main_tab_me));
			// 让popou消除
			// BubbleUtils.popupWindowTestDismiss();
			// if(MailChat.isShowPopup()){
			// updateShowPopupFlag(false);
			// }
			break;
		case INDEX_WORK_SPACE:
			isSetTitle = false;
			isMessageTab = false;
			isWorkspaceTab = true;
			isMailListTab = false;
			isContactTab = false;
			isMeTab = false;
			setIsShowLeftMenu(false);
			setActionBarTitle(getString(R.string.main_tab_workspace));
			break;
		default:
			break;
		}
		// mActionBarSubTitle.setText(getDefaultEmail());
		// 更新菜单
		configureMenu(mMenu);
	}

	private int getCurrentPage() {
		int current = -1;
		if (isMailListTab) {
			current = INDEX_MAIL;
		} else if (isMessageTab) {
			current = INDEX_MESSAGE;
		} else if (isContactTab) {
			current = INDEX_CONTACT;
		} else if (isMeTab) {
			current = INDEX_SETTING;
		} else if (isWorkspaceTab) {
			current = INDEX_WORK_SPACE;
		}
		return current;
	}

	private String getDefaultEmail() {
		String email = Preferences.getPreferences(Main4TabActivity.this)
				.getDefaultAccount().getEmail();
		return email;
	}

	MessagingListener mListener = new MessagingListener() {

		private boolean isSameEmail;

		@Override
		public void accountStatusChanged(BaseAccount account, AccountStats stats) {
			AccountStats oldStats = accountStats.get(account.getUuid());
			int oldUnreadMessageCount = 0;
			if (oldStats != null) {
				oldUnreadMessageCount = oldStats.unreadMessageCount;
			}
			if (stats == null) {
				stats = new AccountStats(); // empty stats for unavailable
											// accounts
				stats.available = false;
			}
			accountStats.put(account.getUuid(), stats);
			if (account instanceof Account) {
				mUnreadMessageCount += stats.unreadMessageCount
						- oldUnreadMessageCount;
			}
			mAccountsHandler.dataChanged();
			pendingWork.remove(account);

			// if (pendingWork.isEmpty()) {
			// mHandler.progress(Window.PROGRESS_END);
			// mHandler.refreshTitle();
			// } else {
			// int level = (Window.PROGRESS_END / mAdapter.getCount()) *
			// (mAdapter.getCount() - pendingWork.size()) ;
			// mHandler.progress(level);
			// }
		}

		@Override
		public void receiveMessage(final Account acc, Folder folder) {
			// if (!acc.getEmail().equals(mSelectedContextAccount)) {
			// acc.setmIsHaveUnreadMsg(true);
			// mHandler.dataChanged();
			// mMeSettingFragmentListener.showUnreadCount(true);
			// }
			// 如果接收到邮件的是当前账号，通知主界面显示左上角红点
			if (folder != null && receiveEmail.equals(defaultAccountEmail)) {
				showIsUnReadMail(folder);
				// return;
			}
			// 如果接收到消息的用户和上次接收到的不一致，并且没有未读消息
			if (!receiveEmail.equals(acc.getEmail())
					&& !acc.getIsHaveUnreadMsg()) {
				receiveEmail = acc.getEmail();
				isSameEmail = false;
			} else {
				// 如果是相同用户接收到消息，并且红点还在的话，不在通知显示红点了
				isSameEmail = true;
			}

			if (!isSameEmail && !receiveEmail.equals(defaultAccountEmail)) {
				acc.setmIsHaveUnreadMsg(true);
				mAccountsHandler.dataChanged();
				// 在设置右上角显示未读红点
				// showUnreadCount(true);

			}
			// 标题栏显示非当前账号有未读红点
			if (!receiveEmail.equals(defaultAccountEmail)) {
				runOnUiThread(new Runnable() {
					public void run() {
						mImgOtherAccountUnreadTag.setVisibility(View.VISIBLE);
					}
				});
			}
		}

		public void getTotalMsgUnreadCountSuccess(Account account,
				int totalCount) {
			if (mAccount != null
					&& mAccount.getUuid().equals(account.getUuid())) {
				messageHandler.sendEmptyMessageDelayed(totalCount, 0);
			}
		}

		public void refreshMainActionBar(Account account) {
			if (mAccount != null
					&& mAccount.getUuid().equals(account.getUuid())) {
				Preferences prefs = Preferences
						.getPreferences(Main4TabActivity.this
								.getApplicationContext());
				mAccount = prefs.getAccount(account.getUuid());// 重新加载一次
				messageHandler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						configureMenu(mMenu);
						isShownWorkSpaceTab(mAccount);
					}
				});
			}
		}

		public void UserIfCertificateProblem(Account account) {
			if (mAccount != null
					&& mAccount.getUuid().equals(account.getUuid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mAccount = Preferences.getPreferences(
								Main4TabActivity.this).getAccount(
								mAccount.getUuid());
						showAuthenticateLy();
					}
				});
			}
		}

		@Override
		public void folderStatusChanged(Account account, String folderName,
				final int unreadMessageCount) {
			if (mAccount != null && account != null
					&& mAccount.getUuid().equals(account.getUuid())
					&& mFolderName != null && folderName != null
					&& mFolderName.equals(folderName)) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						setUnreadCount(unreadMessageCount);
					}

				});
			}
		}

		@Override
		public void arrivedNewOaMessageArrived(Account account) {
			if (mAccount != null && account != null
					&& mAccount.getUuid().equals(account.getUuid())) {
				setBadgeState(badgeWorkspace, true);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestActionBarMenu();
		ActivityManager.push(this);
		initController();
		MobclickAgent.onEvent(getApplicationContext(), "into_main_act");
		if (UpgradeDatabases.actionUpgradeDatabases(this, getIntent())) {
			finish();
			return;
		}
		setContentView(R.layout.activity_main);
		initViews();
		restoreAccountStats(savedInstanceState);
		umengOnlineConfigure();
		registerNewClient();
		showDialogIfMiuiOrHw();
		showDialogIf35PushMailInstalled();
		initializeActionBar();
		initActionbarView();
		initAddAccountPopowindow();
		if (!decodeExtras(getIntent())) {
			return;
		}
		fragmentList = new SparseArray<Fragment>();
		initRight4Fragment();
		initFragment(savedInstanceState);
		initActionbar();
		initData(fragmentList);
		initListener();
		notificationCenter = NotificationCenter.getInstance();
		registerNetWorkReceiver();
		if (mAccount.isDChatStickedUpdate()) {
			try {
				mAccount.getLocalStore().updateDChatSticked();
				mAccount.setDChatStickedUpdate(false);
				mAccount.save(Preferences.getPreferences(this));
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// initNoPushTv(mAccount);
		// showNoPushLy();
	}

	private void registerNewClient() {
		mController.registerNewClient(SystemUtil
				.getCliendId(getApplicationContext()));
	}

	private void initController() {
		mController = MessagingController.getInstance(getApplication());
		mController.addListener(mListener);
	}

	private void restoreAccountStats(Bundle icicle) {
		if (icicle != null) {
			Map<String, AccountStats> oldStats = (Map<String, AccountStats>) icicle
					.get(ACCOUNT_STATS);
			if (oldStats != null) {
				accountStats.putAll(oldStats);
			}
			mUnreadMessageCount = icicle.getInt(STATE_UNREAD_COUNT);
		}
	}

	private void initAddAccountPopowindow() {
		accounts = new ArrayList<BaseAccount>();
		// 多账号列表布局
		View layout = getLayoutInflater().inflate(
				R.layout.layout_popo_add_accound, null);
		// 添加帐号布局
		LinearLayout userInfoLayout = (LinearLayout) layout
				.findViewById(R.id.userInfoLayout);
		userInfoLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addNewAccount();
				popupWindow.dismiss();
				MobclickAgent.onEvent(Main4TabActivity.this,
						"main_view_jump_to_add_new_account");
			}
		});
		((TextView) layout.findViewById(R.id.textView))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						popupWindow.dismiss();
					}
				});
		// 多帐号列表
		mAccountListView = (ListView) layout.findViewById(R.id.userListView);
		defaultAccountEmail = getDefaultEmail().toString();
		mAccountAdapter = new AccountAdapter(Main4TabActivity.this,
				defaultAccountEmail, false);
		mAccountAdapter.setAccountAdapterListener(this);
		mAccountListView.setAdapter(mAccountAdapter);
		mAccountListView.setOnItemClickListener(switchListener);
		mAccountListView.setFooterDividersEnabled(false);
		// 获取屏幕分辨率
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int pWinWidth = (int) (getResources().getInteger(
				R.integer.popupWindowSize) * dm.density);
		// 初始化多张号弹出层布局
		// popupWindow = new PopupWindow(layout, pWinWidth,
		// android.view.ViewGroup.LayoutParams.WRAP_CONTENT, false);
		popupWindow = new PopupWindow(layout,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT, true);
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.argb(5, 239,
				239, 239)));
		popupWindow.setTouchable(true);
		popupWindow.setOutsideTouchable(true);
		// popupWindow.setTouchInterceptor(new OnTouchListener() {
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
		// popupWindow.dismiss();
		// }
		// return false;
		// }
		// });
		// popupWindow.setOnDismissListener(new OnDismissListener() {
		// @Override
		// public void onDismiss() {
		// isPopShow = false;
		// }
		// });
	}

	private void addNewAccount() {
		SelectEmailActivity.showSelectEmailActivity(Main4TabActivity.this);
	}

	private OnItemClickListener switchListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			receiveEmail = "";
			String accountUuid = accounts.get(position).getUuid();
			Account account = Preferences.getPreferences(Main4TabActivity.this)
					.getAccount(accountUuid);
			String choseEmail = account.getEmail();
			if (getDefaultEmail().equals(choseEmail)) {
				setCurrentItem(INDEX_MESSAGE);
			} else {
				changeAccount(position);
			}
			popupWindow.dismiss();
		}
	};

	private void changeAccount(int position) {
		// Trigger message list refreshing
		// Modified by LL
		// BEGIN
		MailChat.forceRefresh = true;
		// END
		String accountUuid = accounts.get(position).getUuid();
		Account account = Preferences.getPreferences(Main4TabActivity.this)
				.getAccount(accountUuid);
		if (account.getIsHaveUnreadMsg()) {
			account.setmIsHaveUnreadMsg(false);
			runOnUiThread(new Runnable() {
				public void run() {
					if (mAccountAdapter != null) {
						mAccountsHandler.dataChanged();
					}
				}
			});
		}
		// account.setName(account.getEmail().substring(0,
		// account.getEmail().indexOf("@")));
		// account.save(Preferences.getPreferences(Main4TabActivity.this));
		Preferences.getPreferences(Main4TabActivity.this).setDefaultAccount(
				account);
		mAccount = Preferences.getPreferences(Main4TabActivity.this)
				.getDefaultAccount();
		defaultAccountEmail = getDefaultEmail();
		LocalSearch search = new LocalSearch();
		search.addAllowedFolder(Account.INBOX);
		search.addAccountUuid(accountUuid);
		reflashFolderList(account);
		initializeFragments(search);
		// if (getCurrentPage()!=-1) {
		// setCurrentItem(getCurrentPage());
		// }else {
		// setCurrentItem(INDEX_MESSAGE);
		// }
		// 是否显示工作台
		isShownWorkSpaceTab(mAccount);
		NotificationCenter.getInstance().notifyClean(account);
		showAuthenticateLy();
		// 尝试调用SM接口
		mController.getGroupInvitation(mAccount, null, false);
		// 判断该账户是否有绑定OA
		mController.checkIsBindOA(mAccount, false, null);
	}

	private void isShownWorkSpaceTab(Account account) {
		if (account.isOAUser()) {
			tabWorkspaceLayout.setVisibility(View.VISIBLE);
		} else {
			tabWorkspaceLayout.setVisibility(View.GONE);
			if (currIndex == INDEX_WORK_SPACE) {
				setCurrentItem(INDEX_MESSAGE);
				changeMainBottomBtnState(INDEX_MESSAGE);
				currIndex = INDEX_MESSAGE;
			}
		}
	}

	private List<BaseAccount> reloadAccountList() {
		List<BaseAccount> newAccounts = new ArrayList<BaseAccount>();
		accounts.clear();
		accounts.addAll(Preferences.getPreferences(this).getAccounts());
		newAccounts.addAll(accounts);
		// start by shengli
		BaseAccount hideAccount = null;
		for (BaseAccount baseAccount : newAccounts) {
			if (baseAccount instanceof Account
					&& ((Account) baseAccount).isHideAccount()) {
				hideAccount = baseAccount;
				break;
			}
		}
		if (hideAccount != null) {
			newAccounts.remove(hideAccount);
			accounts.remove(hideAccount);
		}
		// end
		mAccountAdapter.setAccounts(newAccounts, defaultAccountEmail);
		for (BaseAccount account : newAccounts) {
			pendingWork.put(account, "true");
		}
		return newAccounts;
	}

	@Override
	public void leaveOrDelAccount(final List<BaseAccount> listData,
			final int position, final BaseAccount realAccount) {
		MailDialog.Builder builder = new MailDialog.Builder(this);
		builder.setTitle(R.string.operate_notice);
		builder.setMessage(getString(R.string.account_delete_tips,
				realAccount.getEmail()));
		builder.setPositiveButton(getString(R.string.okay_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						deleteAccount(listData, realAccount, position);
						dialog.dismiss();
					}
				});
		builder.setNeutralButton(getString(R.string.cancel_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	private void deleteAccount(List<BaseAccount> listData,
			BaseAccount realAccount, int position) {
		if (realAccount instanceof Account) {
			Account account = (Account) realAccount;

			int version = account.getVersion_35Mail();
			if (version == 1 || version == 2) {
				try {
					WeemailUtil.deleteC35Account(account);
				} catch (Exception e) {
					Log.e(MailChat.LOG_COLLECTOR_TAG, "删除微妹账号失败", e);
				}
			}

			mController.unRegisterPush(account);
			mController.unsubscribeAccount(account);
			// 删除账号的时候恢复push默认值
			mController.setDefaultFoldersPushValue(account);
			mController.notifyAccountCancel(Main4TabActivity.this, account);
			Preferences.getPreferences(Main4TabActivity.this).deleteAccount(
					account);
			MailChat.setServicesEnabled(Main4TabActivity.this);
			listData.remove(position);
			reloadAccountList();
			mAccountAdapter.notifyDataSetChanged();
			((MeSettingFragment) fragmentList.get(INDEX_SETTING))
					.refreshListData();
			if (account.getEmail().toString().equals(defaultAccountEmail)) {
				// 删除当前账号后，如果还有其他账号默认切换到第一个
				if (listData.size() > 0) {
					changeAccount(0);
					mAccountsHandler.dataChanged();
				} else {
					// 当前没有账号了，切换到登陆
					addNewAccount();
					Main4TabActivity.this.finish();
					setHelpAccountToDefault();
				}
				popupWindow.dismiss();
			}
		}
	}

	private void umengOnlineConfigure() {
		MobclickAgent.updateOnlineConfig(this);
		// 获取友盟在线参数
		String update_tag = MobclickAgent.getConfigParams(this, "let_update");
		if (MailChat.DEBUG) {
			Log.d(MailChat.LOG_TAG,
					"Main4TabActivity.prepareUmengUpdate, update_tag = "
							+ update_tag);
		}
		if (StringUtils.isNullOrEmpty(update_tag)) {
			return;
		}

		// 转换为数组
		mUpdateModeArray = StringUtils.convertStrToArray(update_tag);

		UmengUpdateAgent.setUpdateOnlyWifi(false); // 在任意网络环境下都进行更新自动提醒
		UmengUpdateAgent.update(this); // 调用umeng更新接口
		String curr_version_name = null;
		try {
			curr_version_name = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < mUpdateModeArray.length; i += 2) {
			if (mUpdateModeArray[i].equals(curr_version_name)) {
				if (mUpdateModeArray[i + 1].equals("y")) {
					UmengUpdateAgent
							.setDialogListener(new UmengDialogButtonListener() {
								@Override
								public void onClick(int status) {
									switch (status) {
									case UpdateStatus.Update:
										break;
									default:
										timer.schedule(task, 5 * 1000); // 5s后执行task,经过1s再次执行
										// 友盟自动更新目前还没有提供在代码里面隐藏/显示更新对话框的
										// "以后再说"按钮的方式，所以在这里弹个Toast比较合适
										Toast.makeText(
												Main4TabActivity.this,
												getString(R.string.force_update_tips),
												Toast.LENGTH_LONG).show();
									}
								}
							});
				}
				break;
			}
		}

	}

	Handler finishHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				timer.cancel();
				Main4TabActivity.this.finish();

			}
		};
	};
	Timer timer = new Timer();
	TimerTask task = new TimerTask() {

		@Override
		public void run() {
			android.os.Message message = new android.os.Message();
			message.what = 1;
			finishHandler.sendMessage(message);
		}
	};
	private ImageView mImgOtherAccountUnreadTag;

	private void showDialogIfMiuiOrHw() {
		Storage storage = Storage.getStorage(this);
		boolean isShowDialog = storage.getBoolean(IS_SHOW_DIALOG, false);
		if (!isShowDialog) {
			// TODO 华为手机的话提示用户 开启自启动 暂时屏蔽，后续ui到位开启
			if (SystemUtil.isHuaWei()) {
				showSettingTipsDialog(0);
			}
			if (SystemUtil.isMIUI()) {
				showSettingTipsDialog(1);
			}
			Editor edit = storage.edit();
			edit.putBoolean(IS_SHOW_DIALOG, true);
			edit.commit();
		}
	}

	// 卸载微妹对话框
	// Modified by LL
	// BEGIN
	private static final String IS_UNINSTALL_35_PUSH_MAIL_DIALOG_SHOWN = "is_uninstall_35_push_mail_dialog_shown";

	private void showDialogIf35PushMailInstalled() {
		Intent intent = getPackageManager().getLaunchIntentForPackage(
				"com.c35.mtd.pushmail");
		if (intent != null) {
			Storage storage = Storage.getStorage(this);
			boolean isDialogShown = storage.getBoolean(
					IS_UNINSTALL_35_PUSH_MAIL_DIALOG_SHOWN, false);
			if (!isDialogShown) {
				showUninstall35PushMailDialog();
				Editor edit = storage.edit();
				edit.putBoolean(IS_UNINSTALL_35_PUSH_MAIL_DIALOG_SHOWN, true);
				edit.commit();
			}
		}
	}

	private void showUninstall35PushMailDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		MailDialog.Builder builder = new MailDialog.Builder(this);

		builder.setTitle(R.string.operate_notice);
		builder.setContentView(inflater.inflate(
				R.layout.dialog_uninstall_35_push_mail, null));

		builder.setPositiveButton(
				getString(R.string.uninstall_35_push_mail_okay_action),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(Intent.ACTION_DELETE);
						intent.setData(Uri
								.parse("package:com.c35.mtd.pushmail"));
						startActivity(intent);
						dialog.dismiss();
					}
				});

		builder.setNeutralButton(getString(R.string.cancel_action),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});

		builder.create().show();
	}

	// END

	private View loadMiuiView() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_with_image, null);
		TextView tvMessageTextView = (TextView) layout
				.findViewById(R.id.tv_message);
		TextView tvTipsTextView = (TextView) layout.findViewById(R.id.tv_tips);
		ImageView imgTop = (ImageView) layout.findViewById(R.id.img_top_icon);
		ImageView imgSetting = (ImageView) layout
				.findViewById(R.id.img_setting);
		tvMessageTextView.setText(R.string.miui_message);
		tvTipsTextView.setText(R.string.miui_tips);
		tvTipsTextView.setTextColor(getResources().getColor(R.color.miui_tips));
		imgTop.setBackgroundResource(R.drawable.icon_miui_logo);
		imgSetting.setBackgroundResource(R.drawable.icon_miui_setting);
		return layout;
	}

	private void showSettingTipsDialog(final int flag) {

		MailDialog.Builder builder = new MailDialog.Builder(this);
		builder.setTitle(R.string.operate_notice);
		if (flag == 0) {
			builder.setMessage(getString(R.string.huawei_tips));
		} else if (flag == 1) {
			builder.setContentView(loadMiuiView());
		}
		builder.setPositiveButton(getString(R.string.now_setting),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						if (flag == 0) {
							jumpToHuaweiSetting();
						} else if (flag == 1) {
							jumpToMiuiSetting();
						}
						dialog.dismiss();
					}
				});
		builder.setNeutralButton(getString(R.string.cancel_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	private void jumpToMiuiSetting() {
		PackageInfo info = null;
		try {
			info = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		Intent i = new Intent("miui.intent.action.APP_PERM_EDITOR");
		i.setClassName("com.android.settings",
				"com.miui.securitycenter.permission.AppPermissionsEditor");
		i.putExtra("extra_package_uid", info.applicationInfo.uid);
		try {
			startActivity(i);
		} catch (Exception e) {
			Toast.makeText(Main4TabActivity.this, "只有MIUI才可以设置哦",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void jumpToHuaweiSetting() {
		Intent intent = new Intent(Settings.ACTION_SETTINGS);
		startActivity(intent);
	}

	private void showCoustomToast(String nickName, String email) {
		View layout = getLayoutInflater().inflate(R.layout.layout_toast,
				(ViewGroup) findViewById(R.id.toast_layout_root));

		TextView textNickname = (TextView) layout
				.findViewById(R.id.tv_nickname);
		TextView tvEmail = (TextView) layout.findViewById(R.id.tv_email);
		textNickname.setText(nickName);
		tvEmail.setText(email);

		Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.TOP, 0, 280);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}

	// @Override
	// public void onSaveInstanceState(Bundle outState) {
	// };
	// @Override
	// protected void onRestoreInstanceState(Bundle savedInstanceState) {
	// // TODO Auto-generated method stub
	// super.onRestoreInstanceState(savedInstanceState);
	// if (savedInstanceState!=null) {
	// mFolderName = savedInstanceState
	// .getString(STATE_KEY_FOLDER_NAME);
	// Log.d(TAG, "onRestoreInstanceState#取出="+mFolderName);
	// updateMenu(0);
	// }
	// }
	@Override
	protected void onRestart() {
		super.onRestart();
	}

	private void initData(SparseArray<Fragment> fragments) {
		viewPager.setSlipping(false);
		viewPager.setOffscreenPageLimit(4);
		viewPagerAdapter = new CustomViewPagerAdatper(mFragmentManager,
				fragments);
		viewPager.setAdapter(viewPagerAdapter);
		// 默认选中
		setCurrentItem(MailChat.getBottomTabPosition());
		changeMainBottomBtnState(MailChat.getBottomTabPosition());
		currIndex = MailChat.getBottomTabPosition();
	}

	/**
	 * @Title: initViews
	 * @Description: TODO初始化控件
	 */
	public void initViews() {
		viewPager = (CustomViewPager) findViewById(R.id.viewPager);
		tabMailLayout = (LinearLayout) findViewById(R.id.tabMailLayout);
		tabMessageLayout = (LinearLayout) findViewById(R.id.tabMessageLayout);
		tabContactLayout = (LinearLayout) findViewById(R.id.tabContactLayout);
		tabWorkspaceLayout = (LinearLayout) findViewById(R.id.tabWorkSpaceLayout);

		mainFooterMenuLayout = (LinearLayout) findViewById(R.id.main_footer);
		// mainFooterMenuLayout.setBackgroundColor(Color.argb(230, 238, 238,
		// 238));
		tabMeLayout = (LinearLayout) findViewById(R.id.tabMeLayout);
		mImgTabMail = (ImageView) findViewById(R.id.img_tab_mail);
		mImgTabMessage = (ImageView) findViewById(R.id.img_tab_message);
		mImgTabContant = (ImageView) findViewById(R.id.img_tab_contact);
		mImgTabMe = (ImageView) findViewById(R.id.img_ab_me);
		mImgTabWorkspace = (ImageView) findViewById(R.id.img_tab_workspace);

		mTvTabMail = (TextView) findViewById(R.id.tv_tab_mail);
		mTvTabMessage = (TextView) findViewById(R.id.tv_tab_message);
		mTvTabContant = (TextView) findViewById(R.id.tv_tab_contact);
		mTvTabMe = (TextView) findViewById(R.id.tv_tab_me);
		mTvTabWorkspace = (TextView) findViewById(R.id.tv_tab_workspace);

		// 以下是设置移动条的初始位置
		int[] location = new int[2];
		mImgTabMail.getLocationInWindow(location);
		// badgeMail = new BadgeView(Main4TabActivity.this, mImgTabMail);
		// badgeMail.setBackgroundResource(R.drawable.badge_count_bg);
		// // badge0.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
		// badgeMail.setIncludeFontPadding(false);
		// badgeMail.setGravity(Gravity.CENTER);
		// badgeMail.setTextSize(10f);
		// badgeMail.setTextColor(Color.WHITE);

		// badgeMsg = new BadgeView(Main4TabActivity.this, mImgTabMessage);
		// badgeMsg.setBackgroundResource(R.drawable.badge_count_bg);
		// badgeMsg.setIncludeFontPadding(false);
		// badgeMsg.setGravity(Gravity.CENTER);
		// badgeMsg.setTextSize(10f);
		// badgeMsg.setTextColor(Color.WHITE);
		badgeMail = newBadageView(mImgTabMail, false);
		badgeMsg = newBadageView(mImgTabMessage, false);
		badgeSetting = newBadageView(mImgTabMe, true);
		badgeWorkspace = newBadageView(mImgTabWorkspace, true);

		noNetLinearLayoutView = (LinearLayout) findViewById(R.id.item_net);
		noNetLinearLayoutView.setVisibility(View.GONE);
		setNetTextView = (TextView) findViewById(R.id.net_set);
		setNetTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// startActivity(new
				// Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
				NetUtil.showSystemSettingView(Main4TabActivity.this);
			}
		});
		// push服务提醒相关UI
		noPushLy = (android.widget.RelativeLayout) findViewById(R.id.ly_no_push_service_reminder);
		noPushLy.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				NoPushGuideActivity.actionNoPushGuideActivity(
						Main4TabActivity.this, mAccount);
			}
		});
		noPushLy.setVisibility(View.GONE);
		// push服务以外停止提醒文字
		noPushTv = (TextView) findViewById(R.id.tv_no_push_service_reminder);
		// noPushTv.setText(Html
		// .fromHtml(getString(R.string.no_push_service_reminder_1)
		// + "<u>"
		// + getString(R.string.no_push_service_reminder_2)
		// + "</u>"));
		// push意外停止服务提醒关闭按钮
		closeNoPushIv = (ImageView) findViewById(R.id.iv_no_push_service_reminder);
		closeNoPushIv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				noPushLy.setVisibility(View.GONE);
			}
		});

		// 验证错误，重新验证UI
		authenticateLy = (RelativeLayout) findViewById(R.id.ly_again_authenticate);
		authenticateLy.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AccountSetupIncoming.editAndUpdateIncomingSettings(
						Main4TabActivity.this, mAccount);
			}
		});
		authenticateLy.setVisibility(View.GONE);
		authenticateTv = (TextView) findViewById(R.id.tv_again_authenticate);
		authenticateTv.setText(Html
				.fromHtml(getString(R.string.again_authenticate_1) + "，"
						+ "<u>" + getString(R.string.again_authenticate_2)
						+ "</u>"));
	}

	private BadgeView newBadageView(View view, boolean isColor) {
		BadgeView badgeView = new BadgeView(Main4TabActivity.this, view);
		if (!isColor) {
			badgeView.setBackgroundResource(R.drawable.badge_count_bg);
			badgeView.setIncludeFontPadding(false);
			badgeView.setGravity(Gravity.CENTER);
			badgeView.setTextSize(10f);
			badgeView.setTextColor(Color.WHITE);
		} else {
			// badgeView.setBadgeBackgroundColor(getResources().getColor(R.color.red));
			badgeView.setBackgroundResource(R.drawable.icon_unread_bg);
			badgeView.setHeight(10);
			badgeView.setBadgeMargin(15, 15);
			badgeView.setGravity(Gravity.RIGHT);
		}
		return badgeView;
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		if (mDrawerLayout.isShown()) {
			mDrawerLayout.closeDrawers();
		}
		mMessageReference = null;
		mSearch = null;
		mFolderName = null;

		if (!decodeExtras(intent)) {
			return;
		}
		reflashFolderList(mAccount);
		initializeFragments(mSearch);
		if (viewPager.getCurrentItem() != INDEX_MESSAGE) {
			setCurrentItem(INDEX_MESSAGE);
		}
		registerNetWorkReceiver();
	}

	@Override
	protected void onResume() {
		super.onResume();
		notificationCenter.notifyClean(mAccount);
		MobclickAgent.onPageStart("Main4TabActivity"); // 统计页面
		MobclickAgent.onResume(this);

		if (NetUtil.isActive()) {
			noNetLinearLayoutView.setVisibility(View.GONE);
		} else {
			noNetLinearLayoutView.setVisibility(View.VISIBLE);
		}
		bindStatusReceiver();
		showAuthenticateLy();
		setSettingBadageView();
	}

	/**
	 * 
	 * method name: setSettingBadageView function @Description: TODO Parameters
	 * and return values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2016-3-25 下午1:40:04 @Modified by：zhangjx
	 * @Description：设置tab右上角显示红点
	 */
	private void setSettingBadageView() {
		if (MailChat.has35Account() && !MailChat.isHideSettingBadageView()) {
			setBadgeState(badgeSetting, true);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("Main4TabActivity"); // 统计页面
		MobclickAgent.onPause(this);
	}

	private void initializeActionBar() {
		mCustomActionbarView = LayoutInflater.from(this).inflate(
				R.layout.actionbar_custom_center_titles, null);
		ActionBar.LayoutParams params = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		mActionBar.setCustomView(mCustomActionbarView, params);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);

	}

	private void initActionbarView() {
		mActionBarTitle = (TextView) mCustomActionbarView
				.findViewById(R.id.actionbar_title_name);
		layoutChangeAccount = (LinearLayout) mCustomActionbarView
				.findViewById(R.id.actionbar_message_view);
		mCustomActionbarView.findViewById(R.id.actionbar_show_more)
				.setVisibility(View.VISIBLE);
		mImgOtherAccountUnreadTag = (ImageView) mCustomActionbarView
				.findViewById(R.id.img_unread_tag);
		/*
		 * mActionBarTitle.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { TextView tv = (TextView)v; if
		 * (getString(R.string.main_tab_me).equals(tv.getText())) { if
		 * (mHiddenFeaturesClickCount++ == 0) { mHiddenFeaturesClickTime =
		 * System.currentTimeMillis(); } else { long currentTime =
		 * System.currentTimeMillis(); if (currentTime -
		 * mHiddenFeaturesClickTime < 1000) { mHiddenFeaturesClickTime =
		 * currentTime; if (mHiddenFeaturesClickCount > 5) {
		 * mHiddenFeaturesClickCount = 1; DialogFragment dialog = new
		 * HiddenFeaturesDialogFragment(); dialog.show(mFragmentManager,
		 * "HiddenFeatures"); } } else { mHiddenFeaturesClickCount = 0; } } } }
		 * });
		 */
		layoutChangeAccount.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showPopowindow(v);
			}
		});
		mActionBarSubTitle = (TextView) mCustomActionbarView
				.findViewById(R.id.actionbar_title_sub);
		mActionBarProgress = (ProgressBar) mCustomActionbarView
				.findViewById(R.id.actionbar_progress);

		/*
		 * mActionBarSubTitle.setLongClickable(true);
		 * mActionBarSubTitle.setOnLongClickListener(new OnLongClickListener() {
		 * 
		 * @Override public boolean onLongClick(View v) { TextView tv = new
		 * TextView(Main4TabActivity.this); tv.setLayoutParams(new
		 * LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		 * tv.setGravity(Gravity.CENTER); tv.setText("测试");
		 * tv.setTextColor(Color.WHITE);
		 * 
		 * BubbleUtils.showBubble(Main4TabActivity.this, mActionBarSubTitle,
		 * BubbleUtils.Position.BOTTOM, tv);
		 * 
		 * return true; } });
		 */
	}

	private void showPopowindow(View v) {
		// 标题栏显示非当前账号有未读红点
		if (mImgOtherAccountUnreadTag.isShown()) {
			mImgOtherAccountUnreadTag.setVisibility(View.INVISIBLE);
		}
		v.getLocationOnScreen(popoLocation);
		// int x = popoLocation[0];
		popupWindow.showAsDropDown(v, 0, 0);
		// popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
		popupWindow.update();
		reloadAccountList();

	}

	private boolean decodeExtras(Intent intent) {
		String action = intent.getAction();
		if (Intent.ACTION_VIEW.equals(action) && intent.getData() != null) {
			Uri uri = intent.getData();
			List<String> segmentList = uri.getPathSegments();

			String accountId = segmentList.get(0);
			Collection<Account> accounts = Preferences.getPreferences(this)
					.getAvailableAccounts();
			for (Account account : accounts) {
				if (String.valueOf(account.getAccountNumber())
						.equals(accountId)) {
					mMessageReference = new MessageReference();
					mMessageReference.accountUuid = account.getUuid();
					mMessageReference.folderName = segmentList.get(1);
					mMessageReference.uid = segmentList.get(2);
					break;
				}
			}
		} else if (ACTION_SHORTCUT.equals(action)) {
			// Handle shortcut intents
			String specialFolder = intent.getStringExtra(EXTRA_SPECIAL_FOLDER);
			if (SearchAccount.UNIFIED_INBOX.equals(specialFolder)) {
				mSearch = SearchAccount.createUnifiedInboxAccount(this)
						.getRelatedSearch();
			} else if (SearchAccount.ALL_MESSAGES.equals(specialFolder)) {
				mSearch = SearchAccount.createAllMessagesAccount(this)
						.getRelatedSearch();
			}
		} else if (intent.getStringExtra(SearchManager.QUERY) != null) {
			// check if this intent comes from the system search ( remote )
			if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
				// Query was received from Search Dialog
				String query = intent.getStringExtra(SearchManager.QUERY);

				mSearch = new LocalSearch(getString(R.string.search_results));
				mSearch.setManualSearch(true);
				mNoThreading = true;

				mSearch.or(new SearchCondition(Searchfield.SENDER,
						Attribute.CONTAINS, query));
				mSearch.or(new SearchCondition(Searchfield.SUBJECT,
						Attribute.CONTAINS, query));
				mSearch.or(new SearchCondition(Searchfield.MESSAGE_CONTENTS,
						Attribute.CONTAINS, query));

				Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
				if (appData != null) {
					mSearch.addAccountUuid(appData
							.getString(EXTRA_SEARCH_ACCOUNT));
					// searches started from a folder list activity will provide
					// an account, but no folder
					if (appData.getString(EXTRA_SEARCH_FOLDER) != null) {
						mSearch.addAllowedFolder(appData
								.getString(EXTRA_SEARCH_FOLDER));
					}
				} else {
					mSearch.addAccountUuid(LocalSearch.ALL_ACCOUNTS);
				}
			}
		} else {
			// regular LocalSearch object was passed
			mSearch = intent.getParcelableExtra(EXTRA_SEARCH);
			mNoThreading = intent.getBooleanExtra(EXTRA_NO_THREADING, false);
		}

		if (mMessageReference == null) {
			mMessageReference = intent
					.getParcelableExtra(EXTRA_MESSAGE_REFERENCE);
		}

		if (mMessageReference != null) {
			mSearch = new LocalSearch();
			mSearch.addAccountUuid(mMessageReference.accountUuid);
			mSearch.addAllowedFolder(mMessageReference.folderName);
		}

		if (mSearch == null) {
			// We've most likely been started by an old unread widget
			String accountUuid = intent.getStringExtra("account");
			String folderName = intent.getStringExtra("folder");

			mSearch = new LocalSearch(folderName);
			mSearch.addAccountUuid((accountUuid == null) ? "invalid"
					: accountUuid);
			if (folderName != null) {
				mSearch.addAllowedFolder(folderName);
			}
		}

		Preferences prefs = Preferences.getPreferences(this
				.getApplicationContext());

		// String[] accountUuids = mSearch.getAccountUuids();
		// if (mSearch.searchAllAccounts()) {
		// List<Account> accounts = prefs.getAccounts();
		// mSingleAccountMode = (accounts.size() == 1);
		// if (mSingleAccountMode) {
		// mAccount = accounts.get(0);
		// }
		// } else {
		// mSingleAccountMode = (accountUuids.length == 1);
		// if (mSingleAccountMode) {
		// mAccount = prefs.getAccount(accountUuids[0]);
		// }
		// mAccount.setName(mAccount.getEmail().substring(0,
		// mAccount.getEmail().indexOf("@")));
		// mAccount.save(Preferences.getPreferences(this));
		// prefs.setDefaultAccount(mAccount);
		// }
		mAccount = prefs.getDefaultAccount();
		defaultAccountEmail = mAccount.getEmail();
		mSingleFolderMode = mSingleAccountMode
				&& (mSearch.getFolderNames().size() == 1);

		// if (mSingleAccountMode
		// && (mAccount == null || !mAccount.isAvailable(this))) {
		// Log.i(MailChat.LOG_TAG,
		// "not opening MessageList of unavailable account");
		// onAccountUnavailable();
		// return false;
		// }
		if (mSingleFolderMode) {
			mFolderName = mSearch.getFolderNames().get(0);
		}

		// now we know if we are in single account mode and need a subtitle
		// mActionBarSubTitle.setVisibility((!mSingleFolderMode) ? View.GONE
		// : View.VISIBLE);
		return true;
	}

	private void setIsShowLeftMenu(boolean isShowLeftMenu) {
		if (mDrawerLayout == null) {
			return;
		}
		if (!isShowLeftMenu) {
			mDrawerLayout
					.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			mDrawerToggle.setDrawerIndicatorEnabled(isShowLeftMenu);
		} else {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			mDrawerToggle.setDrawerIndicatorEnabled(isShowLeftMenu);
		}
		mActionBar.setHomeButtonEnabled(isShowLeftMenu);
		mActionBar.setDisplayShowHomeEnabled(isShowLeftMenu);
		// 返回按钮
		mActionBar.setDisplayHomeAsUpEnabled(isShowLeftMenu);
	}

	private void initActionbar() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		// mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
		// GravityCompat.START);
		drawerArrow = new DrawerArrowDrawable(this) {
			@Override
			public boolean isLayoutRtl() {
				return false;
			}
		};
		mDrawerToggle = new ActionBarDrawerToggle(this, mActionBar,
				mDrawerLayout, drawerArrow, R.string.open, R.string.close) {

			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if (mFolderListFragment != null) {
					mFolderListFragment.checkFolder(true);
				}
				drawerArrow.setDrowUnreadCircleIcon(false);
				invalidateOptionsMenu();
			}
		};
		// if (mDrawerLayout.isShown()) {
		// mDrawerLayout.closeDrawers();
		// }
		// mDrawerLayout.setDrawerListener(new DrawerMenuListener());
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();
	}

	private void initListener() {
		tabMailLayout.setOnClickListener(new MyOnClickListener(INDEX_MAIL));
		tabMessageLayout
				.setOnClickListener(new MyOnClickListener(INDEX_MESSAGE));
		tabContactLayout
				.setOnClickListener(new MyOnClickListener(INDEX_CONTACT));
		tabMeLayout.setOnClickListener(new MyOnClickListener(INDEX_SETTING));
		tabWorkspaceLayout.setOnClickListener(new MyOnClickListener(
				INDEX_WORK_SPACE));

		mImgTabMail.setOnClickListener(new MyOnClickListener(INDEX_MAIL));
		mImgTabMessage.setOnClickListener(new MyOnClickListener(INDEX_MESSAGE));
		mImgTabContant.setOnClickListener(new MyOnClickListener(INDEX_CONTACT));
		mImgTabMe.setOnClickListener(new MyOnClickListener(INDEX_SETTING));
		mImgTabWorkspace.setOnClickListener(new MyOnClickListener(
				INDEX_WORK_SPACE));

		mTvTabMail.setOnClickListener(new MyOnClickListener(INDEX_MAIL));
		mTvTabMessage.setOnClickListener(new MyOnClickListener(INDEX_MESSAGE));
		mTvTabContant.setOnClickListener(new MyOnClickListener(INDEX_CONTACT));
		mTvTabMe.setOnClickListener(new MyOnClickListener(INDEX_SETTING));
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		mTvTabWorkspace.setOnClickListener(new MyOnClickListener(
				INDEX_WORK_SPACE));
	}

	private void initFragment(Bundle savedInstanceState) {
		if (MailChat.isChat) {
			viewPager.postDelayed(new Runnable() {
				@Override
				public void run() {
					setCurrentItem(INDEX_MESSAGE);
				}
			}, 100);
		} else if (MailChat.isMail) {
			viewPager.postDelayed(new Runnable() {
				@Override
				public void run() {
					setCurrentItem(INDEX_MAIL);
				}
			}, 100);
		}
		mFolderName = mAccount.getInboxFolderName();
		mFragmentManager = this.getSupportFragmentManager();

		FragmentTransaction ft = mFragmentManager.beginTransaction();
		if (savedInstanceState == null) {
			mFolderListFragment = FolderListFragment.newInstance(mAccount,
					mFolderName, false);
			ft.replace(R.id.drawer_folder_menu, mFolderListFragment,
					FolderListFragment.DRAWER_MENU_TAG).commit();
			// .replace(R.id.drawer_content, reightContentFragment,
			// ReightContentFragment.DRAWER_CONTENT_TAG);
			// mActionBar.setTitle(R.string.app_name);
		} else {
			/**
			 * 系统回收后，这里会有个奇怪的现象，Activity内部装载fragment会先恢复执行oncreate,
			 * 因为是直接从mFragmentManager缓存中取出,并且恢复回收前的数据,
			 * 所以需要清除mFragmentManager里缓存,才能切换账户成功.
			 */
			if (MailChat.isChat || MailChat.isMail) {
				List<Fragment> fragments = mFragmentManager.getFragments();
				if (fragments != null && fragments.size() > 0) {
					for (Fragment fragment : fragments) {
						ft.remove(fragment);
					}
				}
				ft.commit();
			}
		}
		MailChat.isChat = false;
		MailChat.isMail = false;
	}

	private void initRight4Fragment() {
		mSearch.addAccountUuid(mAccount.getUuid());
		// 下面mAccount.getAutoExpandFolderName()现在所有的账户貌似都是存储的inbox
		// mSearch.addAllowedFolder(mAccount.getAutoExpandFolderName());
		createFragments(mSearch);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
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
		if (isMailListTab) {
			menu.findItem(R.id.action_send_msg).setVisible(false);
			menu.findItem(R.id.action_send_mail).setVisible(true);
			menu.findItem(R.id.action_search_mail).setVisible(true);
			menu.findItem(R.id.action_search_msg).setVisible(false);
			menu.findItem(R.id.action_search_contact).setVisible(false);
			menu.findItem(R.id.action_add_contacts).setVisible(false);
			menu.findItem(R.id.action_send_msg_choice).setVisible(false);
			menu.findItem(R.id.action_webview_home).setVisible(false);
			// if (!mActionBarProgress.isShown()) {
			// mActionBarProgress.setVisibility(View.VISIBLE);
			// }
		} else if (isMessageTab) {
			// 消息列表
			configureMenuForMessageTab(menu);
			if (mActionBarProgress.isShown()) {
				mActionBarProgress.setVisibility(View.INVISIBLE);
			}
		} else if (isMeTab) {
			// 我
			menu.findItem(R.id.action_send_msg).setVisible(false);
			menu.findItem(R.id.action_send_mail).setVisible(false);
			menu.findItem(R.id.action_search_mail).setVisible(false);
			menu.findItem(R.id.action_search_msg).setVisible(false);
			menu.findItem(R.id.action_search_contact).setVisible(false);
			menu.findItem(R.id.action_add_contacts).setVisible(false);
			menu.findItem(R.id.action_send_msg_choice).setVisible(false);
			menu.findItem(R.id.action_webview_home).setVisible(false);
			if (mActionBarProgress.isShown()) {
				mActionBarProgress.setVisibility(View.INVISIBLE);
			}
		} else if (isContactTab) {
			// 联系人列表
			menu.findItem(R.id.action_send_msg).setVisible(false);
			menu.findItem(R.id.action_send_mail).setVisible(false);
			menu.findItem(R.id.action_search_mail).setVisible(false);
			menu.findItem(R.id.action_search_msg).setVisible(false);
			menu.findItem(R.id.action_search_contact).setVisible(true);
			menu.findItem(R.id.action_add_contacts).setVisible(true);
			menu.findItem(R.id.action_send_msg_choice).setVisible(false);
			menu.findItem(R.id.action_webview_home).setVisible(false);
			if (mActionBarProgress.isShown()) {
				mActionBarProgress.setVisibility(View.INVISIBLE);
			}
		} else if (isWorkspaceTab) {
			menu.findItem(R.id.action_send_msg).setVisible(false);
			menu.findItem(R.id.action_send_mail).setVisible(false);
			menu.findItem(R.id.action_search_mail).setVisible(false);
			menu.findItem(R.id.action_search_msg).setVisible(false);
			menu.findItem(R.id.action_search_contact).setVisible(false);
			menu.findItem(R.id.action_add_contacts).setVisible(false);
			menu.findItem(R.id.action_send_msg_choice).setVisible(false);
			menu.findItem(R.id.action_webview_home).setVisible(true);
			if (mActionBarProgress.isShown()) {
				mActionBarProgress.setVisibility(View.INVISIBLE);
			}
		}
	}

	// 更新消息Tab显示
	private void configureMenuForMessageTab(Menu menu) {
		if (menu == null) {
			return;
		}
		if (mAccount.isAuthenticated()) {
			if (mAccount.isOAUser()) {
				menu.findItem(R.id.action_send_msg).setVisible(false);
				menu.findItem(R.id.action_send_msg_choice).setVisible(true);
			} else {
				menu.findItem(R.id.action_send_msg).setVisible(true);
				menu.findItem(R.id.action_send_msg_choice).setVisible(false);
			}
			menu.findItem(R.id.action_create_chating).setVisible(false);
			menu.findItem(R.id.action_send_mail).setVisible(false);
			menu.findItem(R.id.action_search_mail).setVisible(false);
			menu.findItem(R.id.action_search_msg).setVisible(true);
			menu.findItem(R.id.action_search_contact).setVisible(false);
			menu.findItem(R.id.action_add_contacts).setVisible(false);
			menu.findItem(R.id.action_webview_home).setVisible(false);
		} else {
			menu.findItem(R.id.action_send_msg).setVisible(false);
			menu.findItem(R.id.action_send_msg_choice).setVisible(false);
			menu.findItem(R.id.action_create_chating).setVisible(false);
			menu.findItem(R.id.action_send_mail).setVisible(false);
			menu.findItem(R.id.action_search_mail).setVisible(false);
			menu.findItem(R.id.action_search_msg).setVisible(false);
			menu.findItem(R.id.action_search_contact).setVisible(false);
			menu.findItem(R.id.action_add_contacts).setVisible(false);
			menu.findItem(R.id.action_webview_home).setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			return true;
		case R.id.action_send_mail:
			// 跳转至写邮件
			MailComposeActivity.actionCompose(Main4TabActivity.this,
					Preferences.getPreferences(this.getApplicationContext())
							.getDefaultAccount(), "");
			MobclickAgent.onEvent(Main4TabActivity.this,
					"main_view_jump_to_send_mail");
			return true;
		case R.id.action_send_msg:
			// 跳转至发起聊天
			// CreateChattingActivity.forwardAction(Main4TabActivity.this,
			// mAccount, true);
			ChooseContactsActivity.forwardAction(Main4TabActivity.this,
					mAccount, true);
			MobclickAgent.onEvent(Main4TabActivity.this,
					"main_view_jump_to_chat");
			return true;
		case R.id.action_search_mail:
			// 跳转搜索界面
			MailSearchActivity.actionFolderSearch(Main4TabActivity.this,
					mAccount, mFolderName);
			MobclickAgent.onEvent(Main4TabActivity.this,
					"main_view_jump_to_search_mail");
			break;
		case R.id.action_search_msg:
			// 跳转搜索界面
			ChatSearchActivity
					.actionChatSearch(Main4TabActivity.this, mAccount);
			MobclickAgent.onEvent(Main4TabActivity.this,
					"main_view_jump_to_search_msg");
			break;
		case R.id.action_search_contact:
			// 跳转搜索界面
			// GlobalSearchActivity.actionFolderSearch(Main4TabActivity.this,
			// mAccount, mFolderName, INDEX_CONTACT);
			ContactSearchActivity.actionContactSearch(Main4TabActivity.this,
					mAccount);
			MobclickAgent.onEvent(Main4TabActivity.this,
					"main_view_jump_to_search_contact");
			break;
		case R.id.action_add_contacts:
			// 跳转新增联系人
			AddOrEditContactActivity.actionAddOrEditContact(
					Main4TabActivity.this, mAccount, null);
			MobclickAgent.onEvent(Main4TabActivity.this,
					"main_view_jump_to_add_contact");
			break;
		case R.id.action_send_msg_choice:
			new OverflowMenuPopo(Main4TabActivity.this, getResources()
					.getStringArray(R.array.sign_chat_right_menu),
					new OverflowMenuPopoListener() {
						@Override
						public void onMenuItemClick(int position) {
							switch (position) {
							case 0:
								// 跳转至发起聊天
								CreateChattingActivity.forwardAction(
										Main4TabActivity.this, mAccount, true);
								MobclickAgent.onEvent(Main4TabActivity.this,
										"main_view_jump_to_chat");
								break;
							case 1:
								String url = GlobalConstants.OA_BASE_URL_START
										+ (mAccount.getoAHost() != null ? mAccount
												.getoAHost()
												: "oa."
														+ Utility
																.getEmailDomain(mAccount
																		.getEmail()))
										+ GlobalConstants.OA_BASE_NEW_URL_END
										+ "&p="
										+ Utility.getOAUserParam(mAccount)
										+ "&type=" + mAccount.getoALoginType();
								WebViewWithErrorViewActivity
										.forwardOpenUrlActivity(
												Main4TabActivity.this, url,
												null, mAccount.getUuid(), -1,
												true);
								MobclickAgent.onEvent(Main4TabActivity.this,
										"main_view_jump_to_create_oa");
								break;
							}
						}
					}).showMoreOptionMenu(findViewById(item.getItemId()));
			break;
		case R.id.action_webview_home:
			refreshOaMain();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);

	}

	protected void requestActionBarMenu() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");

			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			// presumably, not relevant
		}
	}

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// // FragmentManager fragmentManager = Main4TabActivity.this
	// // .getSupportFragmentManager();
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	// if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
	// mDrawerLayout.closeDrawers();
	// return true;
	// }
	// // else if (fragmentManager.getBackStackEntryCount() > 0) {
	// // fragmentManager.popBackStack();
	// // return true;
	// // }
	// else {
	//
	// return mDoubleClickExitHelper.onKeyDown(keyCode, event);
	// }
	//
	// }
	// return super.onKeyDown(keyCode, event);
	// }

	@Override
	public void onDestroy() {
		updateBottomTabPositionState(currIndex);
		super.onDestroy();
		unRegisterNetWorkReceiver();
		unbindStatusReceiver();
		mController.removeListener(mListener);
	}

	@Override
	public void onBackPressed() {
		if (mDrawerLayout.isShown()) {
			mDrawerLayout.closeDrawers();
		}
		if (MailChat.isGesture() && SetPasswordActivity.ifHasGPassword()) {
			SetPasswordActivity.saveGestureUnclock(true);
		}
		// if (System.currentTimeMillis() - mLastExitTime < 2000) {
		// 退到后台去而不是finish
		moveTaskToBack(true);
		// super.onBackPressed();
		// } else {
		// mLastExitTime = System.currentTimeMillis();
		// Toast.makeText(Main4TabActivity.this,
		// getString(R.string.click_back_again_to_exist),
		// Toast.LENGTH_SHORT).show();
		// }
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (isMailListTab) {
				View drawerView = findViewById(R.id.drawer_folder_menu);

				if (!mDrawerLayout.isDrawerOpen(drawerView)) {
					mDrawerLayout.openDrawer(drawerView);
				} else if (mDrawerLayout.isDrawerOpen(drawerView)) {
					mDrawerLayout.closeDrawer(drawerView);
				}
				return true;
			} else {
				// 捕获菜单按键的按下事件
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			setCurrentItem(index);
		}
	};

	private void changeMainBottomBtnState(int arg0) {
		switch (arg0) {
		case INDEX_MESSAGE:
			updateMenu(INDEX_MESSAGE);
			mImgTabMessage.setImageDrawable(getResources().getDrawable(
					R.drawable.icon_tab_message_p));
			mTvTabMessage.setTextColor(getResources().getColor(
					R.color.main_bottom_bar_tv_press));
			if (currIndex == INDEX_MAIL) {
				mImgTabMail.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_mail_n));
				mTvTabMail.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			} else if (currIndex == INDEX_CONTACT) {
				mImgTabContant.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_contact_n));
				mTvTabContant.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			} else if (currIndex == INDEX_SETTING) {
				mImgTabMe.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_me_n));
				mTvTabMe.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			} else if (currIndex == INDEX_WORK_SPACE) {
				mImgTabWorkspace.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_workspace_n));
				mTvTabWorkspace.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			}
			break;
		case INDEX_MAIL:
			updateMenu(INDEX_MAIL);
			mImgTabMail.setImageDrawable(getResources().getDrawable(
					R.drawable.icon_tab_mail_p));
			mTvTabMail.setTextColor(getResources().getColor(
					R.color.main_bottom_bar_tv_press));
			if (currIndex == INDEX_MESSAGE) {
				mImgTabMessage.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_message_n));
				mTvTabMessage.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			} else if (currIndex == INDEX_CONTACT) {
				mImgTabContant.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_contact_n));
				mTvTabContant.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			} else if (currIndex == INDEX_SETTING) {
				mImgTabMe.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_me_n));
				mTvTabMe.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			} else if (currIndex == INDEX_WORK_SPACE) {
				mImgTabWorkspace.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_workspace_n));
				mTvTabWorkspace.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			}
			// 确保切换Tab时ActionBar上的进度图标正确显示
			// Modified by LL
			// BEGIN
			mMessageListFragment.setActionBarProgress(Main4TabActivity.this);
			// END
			break;
		case INDEX_CONTACT:
			updateMenu(INDEX_CONTACT);
			mImgTabContant.setImageDrawable(getResources().getDrawable(
					R.drawable.icon_tab_contact_p));
			mTvTabContant.setTextColor(getResources().getColor(
					R.color.main_bottom_bar_tv_press));
			if (currIndex == INDEX_MAIL) {
				mImgTabMail.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_mail_n));
				mTvTabMail.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			} else if (currIndex == INDEX_MESSAGE) {
				mImgTabMessage.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_message_n));
				mTvTabMessage.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			} else if (currIndex == INDEX_SETTING) {
				mImgTabMe.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_me_n));
				mTvTabMe.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			} else if (currIndex == INDEX_WORK_SPACE) {
				mImgTabWorkspace.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_workspace_n));
				mTvTabWorkspace.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			}
			break;
		case INDEX_SETTING:
			updateMenu(INDEX_SETTING);
			mImgTabMe.setImageDrawable(getResources().getDrawable(
					R.drawable.icon_tab_me_p));
			mTvTabMe.setTextColor(getResources().getColor(
					R.color.main_bottom_bar_tv_press));
			if (currIndex == INDEX_MAIL) {
				mImgTabMail.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_mail_n));
				mTvTabMail.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			} else if (currIndex == INDEX_MESSAGE) {
				mImgTabMessage.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_message_n));
				mTvTabMessage.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			} else if (currIndex == INDEX_CONTACT) {
				mImgTabContant.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_contact_n));
				mTvTabContant.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			} else if (currIndex == INDEX_WORK_SPACE) {
				mImgTabWorkspace.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_workspace_n));
				mTvTabWorkspace.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			}
			if (badgeSetting.isShown()) {
				updateHideSettingBadageView(true);
				setBadgeState(badgeSetting, false);
			}
			break;
		case INDEX_WORK_SPACE:
			updateMenu(INDEX_WORK_SPACE);
			mImgTabWorkspace.setImageDrawable(getResources().getDrawable(
					R.drawable.icon_tab_workspace_p));
			mTvTabWorkspace.setTextColor(getResources().getColor(
					R.color.main_bottom_bar_tv_press));
			if (currIndex == INDEX_MAIL) {
				mImgTabMail.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_mail_n));
				mTvTabMail.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			} else if (currIndex == INDEX_MESSAGE) {
				mImgTabMessage.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_message_n));
				mTvTabMessage.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			} else if (currIndex == INDEX_SETTING) {
				mImgTabMe.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_me_n));
				mTvTabMe.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			} else if (currIndex == INDEX_CONTACT) {
				mImgTabContant.setImageDrawable(getResources().getDrawable(
						R.drawable.icon_tab_contact_n));
				mTvTabContant.setTextColor(getResources().getColor(
						R.color.main_bottom_bar_tv_normal));
			}
			if (badgeWorkspace.isShown()) {
				updateHideOABadageView(true);
				setBadgeState(badgeWorkspace, false);
			}
			refreshOaMain();
			MobclickAgent.onEvent(Main4TabActivity.this,
					"main_bottom_tab_work_space");
			// if (mAccount.isBindOA() && mAccount.getoAHost() != null
			// && mAccount.getoAEmail() != null
			// && !mAccount.isBindOAUser()) {
			// OALoginActivity.actionLoginOA(Main4TabActivity.this, mAccount);
			// }
			break;
		}
	}

	private void updateHideSettingBadageView(boolean isHideSettingBadageView) {
		Editor editor = Preferences.getPreferences(this).getPreferences()
				.edit();
		MailChat.setHideSettingBadageView(isHideSettingBadageView);
		MailChat.save(editor);
		editor.commit();
	}

	private void updateHideOABadageView(boolean isHideOABadageView) {
		Editor editor = Preferences.getPreferences(this).getPreferences()
				.edit();
		MailChat.setHideOABadageView(isHideOABadageView);
		MailChat.save(editor);
		editor.commit();
	}

	/**
	 * 页卡切换监听
	 * 
	 * @ClassName: MyOnPageChangeListener
	 * @Description: TODO(这里用一句话描述这个类的作用)
	 * @author QXian
	 * @date 2014-8-27 上午1:21:57
	 * 
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			changeMainBottomBtnState(arg0);
			currIndex = arg0;
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	public void setActionBarTitle(String title) {
		mActionBarTitle.setText(title);
	}

	// ---MessageListFragmentListener start---

	@Override
	public void onResumed() {

	}

	/**
	 * method name: enableActionBarProgress
	 * 
	 * @see cn.mailchat.fragment.MessageListFragment.MessageListFragmentListener#enableActionBarProgress(boolean)
	 *      function@Description: TODO
	 * @History memory：
	 * @Date：2014-9-19 下午3:29:38 @Modified by：zhangjx
	 * @Description：下拉会促发该回调
	 */
	@Override
	public void enableActionBarProgress(boolean enable) {
		if (enable) {
			mActionBarProgress.setVisibility(ProgressBar.GONE);
		} else {
			if (mFolderListFragment != null) {
				mFolderListFragment.checkFolder(false);
			}
			if (mActionBarProgress.isShown()) {
				mActionBarProgress.setVisibility(ProgressBar.INVISIBLE);
			}
		}

	}

	@Override
	public void setMessageListProgress(int progress) {
		setProgress(progress);
	}

	@Override
	public void showThread(Account account, String folderName, long threadRootId) {
		// showMessageViewPlaceHolder();

		LocalSearch tmpSearch = new LocalSearch();
		tmpSearch.addAccountUuid(account.getUuid());
		tmpSearch.and(Searchfield.THREAD_ID, String.valueOf(threadRootId),
				Attribute.EQUALS);

		MessageListFragment fragment = MessageListFragment.newInstance(
				tmpSearch, true, false);
		addMessageListFragment(fragment, true);
	}

	private void addMessageListFragment(MessageListFragment fragment,
			boolean addToBackStack) {
		viewPagerAdapter.replaceTabItems(INDEX_MAIL,
				viewPagerAdapter.findFragment(INDEX_MAIL), fragment);
		updateMenu(INDEX_MAIL);
		// fragments.remove(INDEX_MAIL);
		// fragments.put(INDEX_MAIL, fragment);
		// viewPagerAdapter = new CustomViewPagerAdatper(mFragmentManager,
		// fragments);
		// viewPager.setAdapter(viewPagerAdapter);
		// FragmentTransaction ft = Main4TabActivity.this
		// .getSupportFragmentManager().beginTransaction();
		// if (addToBackStack) {
		// ft.addToBackStack(null);
		// }
		// mMessageListFragment = fragment;
		//
		// int transactionId = ft.commit();
		// if (transactionId >= 0 && mFirstBackStackId < 0) {
		// mFirstBackStackId = transactionId;
		// }
	}

	// private void showMessageViewPlaceHolder() {
	// removeMessageViewFragment();
	//
	// // Add placeholder view if necessary
	// if (mMessageViewPlaceHolder.getParent() == null) {
	// mMessageViewContainer.addView(mMessageViewPlaceHolder);
	// }
	//
	// mMessageListFragment.setActiveMessage(null);
	// }

	@Override
	public void showMoreFromSameSender(String senderAddress) {
		// LocalSearch tmpSearch = new LocalSearch("From " + senderAddress);
		// tmpSearch.addAccountUuids(mSearch.getAccountUuids());
		// tmpSearch.and(Searchfield.SENDER, senderAddress, Attribute.CONTAINS);
		//
		// MessageListFragment fragment = MessageListFragment.newInstance(
		// tmpSearch, false, false);
		// addMessageListFragment(fragment, true);
	}

	@Override
	public void onResendMessage(Message message) {
		MailComposeActivity.actionEdit(Main4TabActivity.this,
				message.makeMessageReference(),
				MailComposeActivity.SourceFolder.SENT);
	}

	@Override
	public void onForward(Message message) {
		MailComposeActivity.actionForward(Main4TabActivity.this, message
				.getFolder().getAccount(), message, null);
	}

	@Override
	public void onReply(Message message) {
		MailComposeActivity.actionReply(Main4TabActivity.this, message
				.getFolder().getAccount(), message, false, null);
	}

	@Override
	public void onReplyAll(Message message) {
		MailComposeActivity.actionReply(Main4TabActivity.this, message
				.getFolder().getAccount(), message, true, null);
	}

	// 新邮件
	@Override
	public void onCompose(Account account) {
		// MessageCompose.actionCompose(mActivity, account);
		// MailComposeActivity.actionCompose(Main4TabActivity.this, account,"");
		// reightContentFragmentListener.onCompose(mActivity, account);
	}

	@Override
	public void openMessage(MessageReference messageReference) {
		Preferences prefs = Preferences.getPreferences(Main4TabActivity.this);
		Account account = prefs.getAccount(messageReference.accountUuid);
		String folderName = messageReference.folderName;

		if (folderName.equals(account.getDraftsFolderName())) {
			MailComposeActivity.actionEdit(Main4TabActivity.this,
					messageReference, MailComposeActivity.SourceFolder.DRAFT);
		} else if (folderName.equals(account.getOutboxFolderName())) {
			MailComposeActivity.actionEdit(Main4TabActivity.this,
					messageReference, MailComposeActivity.SourceFolder.OUTBOX);
		} else {
			// 跳转到邮件详情页面
			MailDetialActivity.actionDisplayMailDetial(Main4TabActivity.this,
					messageReference);
		}

	}

	@Override
	public void setMessageListTitle(String title) {
		mailFolderName = title;
		if (isSetTitle) {
			// 更新标题名称
			// updateMenu(0);
			setActionBarTitle(title);
		}
	}

	@Override
	public void setMessageListSubTitle(String subTitle) {
		if (subTitle != null) {
			mActionBarSubTitle.setText(subTitle);
		} else {
			mActionBarSubTitle.setText(getDefaultEmail());
		}
	}

	@Override
	public void setUnreadCount(int unread) {
		if (unread > 0) {
			String unreadCount = unread > 99 ? getString(R.string.large_size)
					: Integer.toString(unread);
			badgeMail.setText(unreadCount);
			badgeMail.show();
		} else {
			badgeMail.setText("");
			badgeMail.hide();
		}
	}

	@Override
	public boolean startSearch(Account account, String folderName) {
		startSearch(account, folderName);
		return true;
	}

	@Override
	public void remoteSearchStarted() {
		remoteSearchStarted();
	}

	@Override
	public void goBack() {
		goBack();
	}

	@Override
	public void updateMenu() {
		updateMenu(viewPager.getCurrentItem());

	}

	@Override
	public void isHideHomeBottomBar(boolean b) {
		if (!b) {
			mainFooterMenuLayout.setVisibility(View.VISIBLE);
		} else {
			mainFooterMenuLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void updateMailSearchReasut(int searchReasutCount) {
		// TODO Auto-generated method stub

	}

	// ---MessageListFragmentListener end---

	// ---MeSettingFragmentListener start---

	public void setCurrentItem(final int item) {
		viewPager.setCurrentItem(item, false);
		updateBadgeViewsDragState(item);
	}

	private void updateBadgeViewsDragState(final int curItem) {
		ViewDragHelper.recover(badgeMsg);
		ViewDragHelper.recover(badgeMail);

		if (curItem == INDEX_MESSAGE) {// 聊天tab

			ViewDragHelper.makeViewDragable(badgeMsg, new ViewDragWare() {

				@Override
				public void reShowOrgView() {
					badgeMsg.show();
				}

				@Override
				public void hideOrgView() {
					badgeMsg.hide();
				}

				@Override
				public View getDragView() {
					return badgeMsg;
				}

				@Override
				public Activity getActivity() {
					return Main4TabActivity.this;
				}

				@Override
				public void doOnActionSuccess() {
					mController.markAllConversationNoUnread(mAccount);
				}
			});
		} else if (curItem == INDEX_MAIL) {// 邮件tab

			ViewDragHelper.makeViewDragable(badgeMail, new ViewDragWare() {

				@Override
				public void reShowOrgView() {
					badgeMail.show();
				}

				@Override
				public void hideOrgView() {
					badgeMail.hide();
				}

				@Override
				public View getDragView() {
					return badgeMail;
				}

				@Override
				public Activity getActivity() {
					return Main4TabActivity.this;
				}

				@Override
				public void doOnActionSuccess() {
					mMessageListFragment.markAllAsRead();
				}
			});
		}
	}

	// @Override
	// public void replace4Tabs() {
	// // 新增账号
	// if (fragments.size() > 0) {
	// fragments.clear();
	// }
	// initRight4Fragment();
	// viewPagerAdapter.replaceAllTabs(tabFalg, fragments);
	// viewPager.postDelayed(new Runnable() {
	//
	// @Override
	// public void run() {
	// viewPager.setCurrentItem(INDEX_MESSAGE, false);
	// }
	// }, 100);
	// }
	//
	// @Override
	public void setBadgeState(final BadgeView badgeView, boolean isShow) {
		if (isShow) {
			runOnUiThread(new Runnable() {
				public void run() {
					badgeView.show();
				}
			});
		} else {
			runOnUiThread(new Runnable() {
				public void run() {
					badgeView.hide();
				}
			});
		}
	}

	// @Override
	public void showIsUnReadMail(Folder f) {
		// 非当前邮件夹收到邮件显示红点
		if (!f.getName().equals(mFolderName)) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					mHandler.sendEmptyMessage(0);
				}
			}).start();
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				runOnUiThread(new Runnable() {
					public void run() {
						drawerArrow.setDrowUnreadCircleIcon(true);
					}
				});
			}
		};
	};

	// ---MeSettingFragmentListener end---
	/**
	 * Create fragment instances if necessary.
	 * 
	 * @see #findFragments()
	 */
	public void initializeFragments(LocalSearch search) {
		// fragmentList = new SparseArray<Fragment>();
		fragmentList.clear();
		createFragments(search);
		viewPagerAdapter.replaceAllTabs(tabFalg, fragmentList);
	}

	public void reflashFolderList(Account account) {
		// if (account!=null) {
		// mFolderName=account.getInboxFolderName();
		// }else {
		// mFolderName=Preferences.getPreferences(Main4TabActivity.this)
		// .getDefaultAccount().getInboxFolderName();
		// }
		mFolderName = Account.INBOX;
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		// ft.remove(mFragmentManager
		// .findFragmentByTag(FolderListFragment.DRAWER_MENU_TAG));
		mFolderListFragment = FolderListFragment.newInstance(account,
				mFolderName, false);
		ft.replace(R.id.drawer_folder_menu, mFolderListFragment,
				FolderListFragment.DRAWER_MENU_TAG);
		ft.commitAllowingStateLoss();
	}

	// ---ChatListFragmentListener end---
	// @Override
	// public void updateContactSearchReasut(int searchReasutCount) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void updateChoseCount(int choseCount) {
	// // TODO Auto-generated method stub
	//
	// }

	/**
	 * 获取适配器
	 * 
	 * @return
	 */
	public CustomViewPagerAdatper getAdapter() {
		return viewPagerAdapter;
	}

	@Override
	public void onChangeFolder(LocalSearch search, String folderName) {
		// mFolderName = search.getFolderNames().get(0);
		// mActionBarTitle.setText(FolderInfoHolder.getDisplayName(
		// Main4TabActivity.this, mAccount, mFolderName));
		mFolderName = folderName;
		mMessageListFragment = MessageListFragment.newInstance(search, false,
				false);
		if (mDrawerLayout.isShown()) {
			mDrawerLayout.closeDrawers();
		}
		viewPagerAdapter
				.replaceTabItems(INDEX_MAIL,
						viewPagerAdapter.findFragment(INDEX_MAIL),
						mMessageListFragment);
	}

	private BroadcastReceiver netWorkReceiver;

	class NetWorkReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			if (NetUtil.isActive()) {
				noNetLinearLayoutView.setVisibility(View.GONE);
			} else {
				noNetLinearLayoutView.setVisibility(View.VISIBLE);
				noPushLy.setVisibility(View.GONE);
			}
		}

	}

	private void registerNetWorkReceiver() {
		if (netWorkReceiver == null) {
			netWorkReceiver = new NetWorkReceiver();
			IntentFilter filter = new IntentFilter(
					"android.net.conn.CONNECTIVITY_CHANGE");
			registerReceiver(netWorkReceiver, filter);
		}
	}

	private void unRegisterNetWorkReceiver() {
		if (netWorkReceiver != null) {
			unregisterReceiver(netWorkReceiver);
			netWorkReceiver = null;
		}
	}

	// END
	/**
	 * 
	 * method name: onActivityResult
	 * 
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int,
	 *      android.content.Intent) function@Description: TODO
	 * @History memory:
	 * @Date：2015-6-29 下午4:00:16 @Modified by：zhangjx
	 * @Description：账号设置页删除账号
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case AccountSetupNameActivity.EXTRA_START_ACTIVITY_TAG:
			String accountUuid = data
					.getStringExtra(AccountSetupNameActivity.EXTRA_ACCOUNT);
			int position = data.getIntExtra(
					AccountSettingActivity.EXTRA_ACCOUNT_POSITION, -1);
			if (position != -1 && !TextUtils.isEmpty(accountUuid)) {
				BaseAccount account = Preferences.getPreferences(this)
						.getAccount(accountUuid);
				deleteAccount(reloadAccountList(), account, position);
			}
			break;
		}
	}

	private void setHelpAccountToDefault() {
		Preferences pre = Preferences.getPreferences(getApplication());
		Account mAccount = pre.getAccount(EncryptUtil.getMd5(SystemUtil
				.getCliendId(this) + GlobalConstants.HIDE_ACCOUNT_SUFFIX));
		if (mAccount != null) {
			pre.setDefaultAccount(mAccount);
		}
	}

	@Override
	public void updateContactSearchReasut(int searchReasutCount) {
	}

	@Override
	public void updateChoseCount(int choseCount) {
	}

	@Override
	public void haveSearchResult() {
		// TODO Auto-generated method stub

	}

	// //加号选择 by shengli
	// //start
	// private MenuAdapter mMenuAdapter;
	// private ListPopupWindow mMenuWindow;
	// private void showMoreOptionMenu(View view) {
	// mMenuWindow = new ListPopupWindow(this);
	// if (mMenuAdapter == null) {
	// mMenuAdapter = new MenuAdapter();
	// }
	// mMenuWindow.setModal(true);
	// mMenuWindow.setContentWidth(getResources().getDimensionPixelSize(
	// R.dimen.popo_main_chatting_menu_dialog_width));
	// mMenuWindow.setAdapter(mMenuAdapter);
	// mMenuWindow.setOnItemClickListener(new OnItemClickListener() {
	//
	// @Override
	// public void onItemClick(AdapterView<?> parent, View view,
	// int position, long id) {
	// // TODO Auto-generated method stub
	// switch (position) {
	// case 0:
	// // 跳转至发起聊天
	// CreateChattingActivity.forwardAction(Main4TabActivity.this,mAccount,
	// true);
	// MobclickAgent.onEvent(Main4TabActivity.this, "main_view_jump_to_chat");
	// break;
	// case 1:
	// WebViewWithErrorViewActivity.forwardOpenUrlActivity(Main4TabActivity.this,GlobalConstants.OA_BASE_URL_START+Utility.getEmailDomain(mAccount.getEmail())+GlobalConstants.OA_BASE_NEW_URL_END+"&p="+Utility.getOAUserParam(mAccount),
	// null,mAccount.getUuid());
	// MobclickAgent.onEvent(Main4TabActivity.this,
	// "main_view_jump_to_create_oa");
	// break;
	// }
	// if (mMenuWindow != null) {
	// mMenuWindow.dismiss();
	// mMenuWindow = null;
	// }
	// }
	// });
	// mMenuWindow.setAnchorView(view);
	// mMenuWindow.show();
	// }
	//
	// class MenuAdapter extends BaseAdapter {
	//
	// @Override
	// public int getCount() {
	// return menuArray.length;
	// }
	//
	// @Override
	// public Object getItem(int position) {
	// return null;
	// }
	//
	// @Override
	// public long getItemId(int position) {
	// return 0;
	// }
	//
	// @SuppressLint("InflateParams")
	// @Override
	// public View getView(int position, View convertView, ViewGroup parent) {
	// convertView = LayoutInflater.from(parent.getContext()).inflate(
	// R.layout.item_overflow_menu, null);
	// TextView name = (TextView) convertView.findViewById(R.id.tv_name);
	// name.setText(menuArray[position]);
	// return convertView;
	// }
	// }
	// //end
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// if
		// (hasFocus&&MailChat.isShowPopup()&&!BubbleUtils.isPopupWindowTestShow())
		// {
		// BubbleUtils.popupWindowTest(Main4TabActivity.this, mImgTabMe);
		// }
	}

	private void updateJoinCampaignFlag(boolean JoinCampaignFlag) {
		Editor editor = Preferences.getPreferences(this).getPreferences()
				.edit();
		MailChat.setJoinCampaign(JoinCampaignFlag);
		MailChat.save(editor);
		editor.commit();
	}

	private void updateShowPopupFlag(boolean isShowPopup) {
		Editor editor = Preferences.getPreferences(this).getPreferences()
				.edit();
		MailChat.setShowPopup(isShowPopup);
		MailChat.save(editor);
		editor.commit();
	}

	/**
	 * 
	 * method name: updateBottomTabPositionState function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param pos
	 *            field_name void return type
	 * @History memory：
	 * @Date：2016-2-23 下午2:11:52 @Modified by：zhangjx
	 * @Description：杀死进程时标识最后一次选中的tab
	 */
	private void updateBottomTabPositionState(int pos) {
		Editor editor = Preferences.getPreferences(this).getPreferences()
				.edit();
		MailChat.setBottomTabPosition(pos);
		MailChat.save(editor);
		editor.commit();
	}

	/**
	 * 初始化无push控件文字提示及点击事件
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-11-25
	 */
	private void initNoPushTv(final Account account) {
		noPushTv.setText(getString(R.string.no_push_service_reminder_1));
		String details_text = getString(R.string.no_push_service_reminder_2);
		SpannableString spStr = new SpannableString(details_text);
		spStr.setSpan(new ClickableSpan() {
			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setColor(Color.WHITE);
				ds.setUnderlineText(true);
			}

			@Override
			public void onClick(View widget) {
				NoPushGuideActivity.actionNoPushGuideActivity(
						Main4TabActivity.this, account);
			}
		}, 0, details_text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		noPushTv.setHighlightColor(Color.TRANSPARENT); // 设置点击后的颜色为透明，否则会一直出现高亮
		noPushTv.append(spStr);
		noPushTv.setMovementMethod(LinkMovementMethod.getInstance());// 开始响应点击事件
	}

	/**
	 * 判断是否显示 MQTT push异常提示
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-11-25
	 */
	// private void showNoPushLy(){
	// SharedPreferences prefs =
	// getSharedPreferences("mqtt_ping",Context.MODE_PRIVATE);
	// long successTime = prefs.getLong("pingSuccessTime",
	// System.currentTimeMillis());
	// boolean isNoRemind = prefs.getBoolean("isNoRemind", false);
	// if(!isNoRemind && System.currentTimeMillis()-successTime>900*1000 &&
	// NetUtil.isActive()){
	// //当前时间大于ping成功的时间间隔
	// noPushLy.setVisibility(View.VISIBLE);
	// }else{
	// noPushLy.setVisibility(View.GONE);
	// }
	// // 更新PingSuccess时间
	// Editor editor = prefs.edit();
	// editor.putLong("pingSuccessTime", System.currentTimeMillis());
	// editor.commit();
	// }
	/**
	 * 判断是否显示账号异常提醒
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-12-22
	 */
	private void showAuthenticateLy() {
		if (mAccount != null) {
			if (!mAccount.isAuthenticated()) {
				authenticateLy.setVisibility(View.VISIBLE);
			} else {
				authenticateLy.setVisibility(View.GONE);
			}
			configureMenuForMessageTab(mMenu);
		}
	}

	private void bindStatusReceiver() {
		statusReceiver = new MqttConnStatusReceiver();
		if (!statusReceiver.hasHandlers()) {
			statusReceiver.registerHandler(this);
		}
		registerReceiver(statusReceiver, new IntentFilter(
				Connection.MQTT_STATUS_INTENT));
	}

	private void unbindStatusReceiver() {
		if (statusReceiver != null) {
			statusReceiver.unregisterHandler(this);
			unregisterReceiver(statusReceiver);
			statusReceiver = null;
		}
	}

	@Override
	public void handleStatus(ConnectionStatus status, String reason) {
		Log.d(MailChat.LOG_COLLECTOR_TAG, ">>handleStatus: status=" + status
				+ ", reason=" + reason);
		if (status == Connection.ConnectionStatus.CONNECTED) {
			onConnectionConnected();
		} else {
			String connStatus = status.toString();
			String subErrorMsg = reason;
			onConnectionDisconnected(connStatus, subErrorMsg);
		}
	}

	/**
	 * 连接到服务器
	 */
	protected void onConnectionConnected() {
		if (noPushLy != null) {
			noPushLy.setVisibility(View.GONE);
		}
	}

	/**
	 * 连接断开
	 */
	protected void onConnectionDisconnected(String connStatus,
			String subErrorMsg) {
		if (!StringUtils.isNullStrOrEmpty(subErrorMsg) && NetUtil.isActive()) {
			noPushTv.setText(Html
					.fromHtml(getString(R.string.no_push_service_reminder_1)
							+ "<u>"
							+ getString(R.string.no_push_service_reminder_2)
							+ "</u>"));
			if (noPushLy != null) {
				noPushLy.setVisibility(View.VISIBLE);
			}

		}
	}

	private void createFragments(LocalSearch mSearch) {
		ChatListFragment mChatListFragment = ChatListFragment.newInstance("",
				false);
		WorkSpaceFragment mOaFragment = WorkSpaceFragment.newInstance(
				getOaUrl(mAccount), null, mAccount.getUuid(),
				mAccount.isOAUser());
		mMessageListFragment = MessageListFragment.newInstance(mSearch, false,
				false);
		ContactTabsFragment mContactListFragment = new ContactTabsFragment();
		MeSettingFragment meSettingFragment = MeSettingFragment
				.newInstance(getDefaultEmail());
		fragmentList.put(INDEX_MESSAGE, mChatListFragment);
		fragmentList.put(INDEX_WORK_SPACE, mOaFragment);
		fragmentList.put(INDEX_MAIL, mMessageListFragment);
		fragmentList.put(INDEX_CONTACT, mContactListFragment);
		fragmentList.put(INDEX_SETTING, meSettingFragment);
	}

	private String getOaUrl(Account account) {
		String mOAUrl = "";
		if (account.isOAUser()) {
			mOAUrl = GlobalConstants.OA_BASE_URL_START
					+ (account.getoAHost() != null ? account.getoAHost()
							: "oa."
									+ Utility
											.getEmailDomain(account.getEmail()))
					+ GlobalConstants.OA_BASE_MAIN_URL_END + "&p="
					+ Utility.getOAUserParam(account) + "&type="
					+ account.getoALoginType();
		} else {
			mOAUrl = GlobalConstants.OA_NO_OPEN_URL;
		}
		return mOAUrl;
	}
	private void refreshOaMain(){
		if (mAccount.isOAUser()) {
			WorkSpaceFragment mOaFragment =(WorkSpaceFragment) viewPagerAdapter.findFragment(INDEX_WORK_SPACE);
			if (mOaFragment!=null) {
				mOaFragment.loadUrl(getOaUrl(mAccount));
			}
		}
	}

	@Override
	public void setWebViewTitle(String title) {

	}
}
