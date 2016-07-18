package cn.mailchat.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils.TruncateAt;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.AccountStats;
import cn.mailchat.BaseAccount;
import cn.mailchat.FontSizes;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.Accounts;
import cn.mailchat.activity.ActivityListener;
import cn.mailchat.activity.FolderInfoHolder;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.helper.SizeFormatter;
import cn.mailchat.helper.power.TracingPowerManager;
import cn.mailchat.helper.power.TracingPowerManager.TracingWakeLock;
import cn.mailchat.mail.Folder;
import cn.mailchat.mail.Message;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.search.LocalSearch;
import cn.mailchat.search.SearchSpecification.Attribute;
import cn.mailchat.search.SearchSpecification.Searchfield;

import com.umeng.analytics.MobclickAgent;

/**
 * 
 * @copyright © 35.com
 * @file name ：TabDFragment.java
 * @author ：zhangjx
 * @create Data ：2014-9-18下午5:30:02
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-9-20下午5:30:02
 * @Modified by：zhangjx
 * @Description :左侧邮件夹--页面
 */
public class FolderListFragment extends Fragment {
	private static final String TAG = "FolderListFragment";
	public static final String DRAWER_MENU_TAG = "cn.mailchat.fragment.folderListFragment";
	private static final String EXTRA_ACCOUNT = "account";
	private static final String EXTRA_FOLDER_NAME = "folderName";
	private static final String EXTRA_FROM_SHORTCUT = "fromShortcut";
	private static final boolean REFRESH_REMOTE = true;

	private FragmentActivity mActivity;

	private ListView mListView;

	private FolderListAdapter mAdapter;

	private LayoutInflater mInflater;

	private Account mAccount;

	private FolderListHandler mHandler = new FolderListHandler();

	private int mUnreadMessageCount;

	private FontSizes mFontSizes = MailChat.getFontSizes();
	private Context context;
	private int selected = -1;
	private String mFolderName;
	private FolderListListener mFolderListListener;

	// private SwipeRefreshLayout mSwipeRefreshLayout;

	public static FolderListFragment newInstance(Account account,
			String mFolderName, boolean fromShortcut) {
		FolderListFragment fragment = new FolderListFragment();
		Bundle args = new Bundle();
		args.putString(EXTRA_ACCOUNT, account.getUuid());
		args.putString(EXTRA_FOLDER_NAME, mFolderName);
		if (fromShortcut) {
			args.putBoolean(EXTRA_FROM_SHORTCUT, true);
		}
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Log.d(TAG, "onAttach");
		mActivity = getActivity();
		try {
			mFolderListListener = (FolderListListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.getClass()
					+ " must implement FolderListListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Log.d(TAG, "FolderListFragment-->>onCreate");
		// if (UpgradeDatabases.actionUpgradeDatabases(mActivity,
		// mActivity.getIntent())) {
		// mActivity.finish();
		// return;
		// }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Log.d(TAG, "FolderListFragment-->>onCreateView");
		View view = inflater.inflate(R.layout.fragment_folder_list, null);
		initView(view);
		return view;
	}

	private void initView(View view) {
		mListView = (ListView) view.findViewById(R.id.folder_list);
		// mSwipeRefreshLayout = (SwipeRefreshLayout) view
		// .findViewById(R.id.folder_lsit_refresh);
		// mSwipeRefreshLayout.setColorSchemeResources(R.color.gray,
		// R.color.green, R.color.blue);
		mListView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		mListView.setLongClickable(true);
		mListView.setFastScrollEnabled(true);
		mListView.setScrollingCacheEnabled(false);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// Log.d(TAG,"==第>"+position+"=》"+((FolderInfoHolder)
				// mAdapter.getItem(position)).name);
				selected = position;
				// setItemChecked(position);
				onOpenFolder(((FolderInfoHolder) mAdapter.getItem(position)).name);
				mAdapter.notifyDataSetChanged();
			}
		});
		// mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
		// @Override
		// public void onRefresh() {
		// checkFolder(REFRESH_REMOTE);
		// }
		// });
		registerForContextMenu(mListView);

		mListView.setSaveEnabled(true);

		mInflater = mActivity.getLayoutInflater();

		context = mActivity;

		if (mActivity.isFinishing()) {
			/*
			 * onNewIntent() may call finish(), but execution will still
			 * continue here. We return now because we don't want to display the
			 * changelog which can result in a leaked window error.
			 */
			return;
		}
	}

	// public void setItemChecked(int position) {
	// mListView.setItemChecked(position, true);
	// mListView.setSelection(position);
	// }
	class FolderListHandler extends Handler {

		public void refreshTitle() {
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					// mActionBarTitle.setText(getString(R.string.folders_title));
					//
					// if (mUnreadMessageCount == 0) {
					// mActionBarUnread.setVisibility(View.GONE);
					// } else {
					// mActionBarUnread.setText(Integer.toString(mUnreadMessageCount));
					// mActionBarUnread.setVisibility(View.VISIBLE);
					// }

					// String operation = mAdapter.mListener
					// .getOperation(mActivity);
					// if (operation.length() < 1) {
					// mActionBarSubTitle.setText(mAccount.getEmail());
					// } else {
					// mActionBarSubTitle.setText(operation);
					// }
				}
			});
		}

		public void newFolders(final List<FolderInfoHolder> newFolders) {
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					mAdapter.mFolders.clear();
					mAdapter.mFolders.addAll(newFolders);
					mAdapter.mFilteredFolders = Collections.unmodifiableList(mAdapter.mFolders);
					mHandler.dataChanged();
				}
			});
		}

		public void workingAccount(final int res) {
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					String toastText = getString(res, mAccount.getDescription());
					Toast toast = Toast.makeText(mActivity.getApplication(),
							toastText, Toast.LENGTH_SHORT);
					toast.show();
				}
			});
		}

		public void accountSizeChanged(final long oldSize, final long newSize) {
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					String toastText = getString(R.string.account_size_changed,
							mAccount.getDescription(), SizeFormatter
									.formatSize(mActivity.getApplication(),
											oldSize), SizeFormatter.formatSize(
									mActivity.getApplication(), newSize));

					Toast toast = Toast.makeText(mActivity.getApplication(),
							toastText, Toast.LENGTH_LONG);
					toast.show();
				}
			});
		}

		public void folderLoading(final String folder, final boolean loading) {
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					FolderInfoHolder folderHolder = mAdapter.getFolder(folder);

					if (folderHolder != null) {
						folderHolder.loading = loading;
					}

				}
			});
		}

		public void progress(final boolean progress) {
			// Make sure we don't try this before the menu is initialized
			// this could happen while the activity is initialized.
			// if (mRefreshMenuItem == null) {
			// return;
			// }

			// mActivity.runOnUiThread(new Runnable() {
			// public void run() {
			// mSwipeRefreshLayout.setRefreshing(progress);
			// if (progress) {
			// mRefreshMenuItem.setActionView(mActionBarProgressView);
			// } else {
			// mRefreshMenuItem.setActionView(null);
			// }
			// }
			// });

		}

		private void checkMail(FolderInfoHolder folder) {
			TracingPowerManager pm = TracingPowerManager
					.getPowerManager(getActivity());
			final TracingWakeLock wakeLock = pm.newWakeLock(
					PowerManager.PARTIAL_WAKE_LOCK, "FolderList checkMail");
			wakeLock.setReferenceCounted(false);
			wakeLock.acquire(MailChat.WAKE_LOCK_TIMEOUT);
			MessagingListener listener = new MessagingListener() {
				@Override
				public void synchronizeMailboxFinished(Account account,
						String folder, int totalMessagesInMailbox,
						int numNewMessages) {
					if (!account.equals(mAccount)) {
						return;
					}
					wakeLock.release();
				}

				@Override
				public void synchronizeMailboxFailed(Account account,
						String folder, String message) {
					if (!account.equals(mAccount)) {
						return;
					}
					wakeLock.release();
				}
			};
			MessagingController.getInstance(getActivity().getApplication())
					.synchronizeMailbox(mAccount, folder.name, listener, null);
			sendMail(mAccount);
		}

		private void sendMail(Account account) {
			// MessagingController.getInstance(getActivity().getApplication())
			// .sendPendingMessages(account, mAdapter.mListener);
		}

		public void dataChanged() {
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					mAdapter.notifyDataSetChanged();
				}
			});
		}
	}

	private void initializeActivityView() {
		mAdapter = new FolderListAdapter();
		restorePreviousData();
		mListView.setAdapter(mAdapter);
		mListView.setTextFilterEnabled(mAdapter.getFilter() != null);
	}

	@SuppressWarnings("unchecked")
	private void restorePreviousData() {
		final Object previousData = mActivity.getLastNonConfigurationInstance();

		if (previousData != null) {
			mAdapter.mFolders = (ArrayList<FolderInfoHolder>) previousData;
			mAdapter.mFilteredFolders = Collections
					.unmodifiableList(mAdapter.mFolders);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		MessagingController.getInstance(mActivity.getApplication())
				.removeListener(mAdapter.mListener);
		mAdapter.mListener.onPause(mActivity);
		MobclickAgent.onPageEnd("FolderListFragment");
	}

	/**
	 * On resume we refresh the folder list (in the background) and we refresh
	 * the messages for any folder that is currently open. This guarantees that
	 * things like unread message count and read status are updated.
	 */
	@Override
	public void onResume() {
		super.onResume();
		Bundle bundle = getArguments();
		mUnreadMessageCount = 0;
		String accountUuid = bundle.getString(EXTRA_ACCOUNT);
		mFolderName = bundle.getString(EXTRA_FOLDER_NAME);
		mAccount = Preferences.getPreferences(mActivity)
				.getAccount(accountUuid);

		if (mAccount == null) {
			/*
			 * This can happen when a launcher shortcut is created for an
			 * account, and then the account is deleted or data is wiped, and
			 * then the shortcut is used.
			 */
			mActivity.finish();
			return;
		}

		if (bundle.getBoolean(EXTRA_FROM_SHORTCUT, false)
				&& !MailChat.FOLDER_NONE.equals(mAccount
						.getAutoExpandFolderName())) {
			onOpenFolder(mAccount.getAutoExpandFolderName());
			mActivity.finish();
		} else {
			initializeActivityView();
		}

		if (!mAccount.isAvailable(mActivity)) {
			Log.i(MailChat.LOG_TAG,
					"account unavaliabale, not showing folder-list but account-list");
			Accounts.listAccounts(mActivity);
			mActivity.finish();
			return;
		}
		if (mAdapter == null)
			initializeActivityView();

		mHandler.refreshTitle();

		MessagingController.getInstance(mActivity.getApplication())
				.addListener(mAdapter.mListener);
		// mAccount.refresh(Preferences.getPreferences(getActivity()));
		MessagingController.getInstance(mActivity.getApplication())
				.getAccountStats(mActivity, mAccount, mAdapter.mListener);

		checkFolder(!REFRESH_REMOTE);

		MessagingController.getInstance(mActivity.getApplication())
				.notifyAccountCancel(mActivity, mAccount);
		mAdapter.mListener.onResume(mActivity);
		MobclickAgent.onPageStart("FolderListFragment");
	}

	public void checkFolder(final boolean forceRemote) {
		// 验证是否35 3.0mail
		MessagingController.getInstance(mActivity.getApplication())
				.get35MailVersion(mAccount, true);
		MessagingController.getInstance(mActivity.getApplication())
				.listFolders(mAccount, forceRemote, mAdapter.mListener);

	}

	private void onOpenFolder(String folder) {
		LocalSearch search = new LocalSearch(folder);
		search.addAccountUuid(mAccount.getUuid());
		search.addAllowedFolder(folder);
		mFolderListListener.onChangeFolder(search, folder);
		// Main4TabActivity.actionDisplaySearch(mActivity, search, false,
		// false);
	}

	class FolderListAdapter extends BaseAdapter implements Filterable {
		private ArrayList<FolderInfoHolder> mFolders = new ArrayList<FolderInfoHolder>();
		private List<FolderInfoHolder> mFilteredFolders = Collections
				.unmodifiableList(mFolders);
		private Filter mFilter = new FolderListFilter();

		public Object getItem(long position) {
			return getItem((int) position);
		}

		@Override
		public Object getItem(int position) {
			if (mFilteredFolders != null && mFilteredFolders.size() > 0) {
				return mFilteredFolders.get(position);
			} else {
			    return null;
			}
		}

		public long getItemId(int position) {
			return mFilteredFolders.get(position).folder.getName().hashCode();
		}

		public int getCount() {
			return mFilteredFolders.size();
		}

		@Override
		public boolean isEnabled(int item) {
			return true;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		private ActivityListener mListener = new ActivityListener() {
			@Override
			public void informUserOfStatus() {
				mHandler.refreshTitle();
				mHandler.dataChanged();
			}

			@Override
			public void accountStatusChanged(BaseAccount account,
					AccountStats stats) {
				if (!account.equals(mAccount)) {
					return;
				}
				if (stats == null) {
					return;
				}
				mUnreadMessageCount = stats.unreadMessageCount;
				mHandler.refreshTitle();
			}

			@Override
			public void listFoldersStarted(Account account) {
				if (account.equals(mAccount)) {
					mHandler.progress(true);
				}
				super.listFoldersStarted(account);

			}

			@Override
			public void listFoldersFailed(Account account, String message) {
				if (account.equals(mAccount)) {

					mHandler.progress(false);

				}
				super.listFoldersFailed(account, message);
			}

			@Override
			public void listFoldersFinished(Account account) {
				if (account.equals(mAccount)) {
					mHandler.progress(false);
					MessagingController.getInstance(mActivity.getApplication())
							.refreshListener(mAdapter.mListener);
					mHandler.dataChanged();
				}
				super.listFoldersFinished(account);

			}

			@Override
			public void listFolders(Account account, Folder[] folders) {
				if (account.equals(mAccount)) {
					List<FolderInfoHolder> newFolders = new LinkedList<FolderInfoHolder>();
					List<FolderInfoHolder> topFolders = new LinkedList<FolderInfoHolder>();

					Account.FolderMode aMode = account.getFolderDisplayMode();
					Preferences prefs = Preferences.getPreferences(mActivity
							.getApplication().getApplicationContext());
					for (Folder folder : folders) {
						try {
							folder.refresh(prefs);

							Folder.FolderClass fMode = folder.getDisplayClass();
							// 升级后强制勾选inbox推送设置
							// if
							// (account.getInboxFolderName().equalsIgnoreCase(
							// folder.getName())) {
							// if
							// (!folder.isAllowPush()&&account.isSetInboxNotify())
							// {
							// MessagingController.getInstance(
							// mActivity.getApplication())
							// .updateFolderPushState(account,
							// folder.getName(), true,
							// null);
							// }
							// }
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

						FolderInfoHolder holder = null;

						int folderIndex = getFolderIndex(folder.getName());
						if (folderIndex >= 0) {
							holder = (FolderInfoHolder) getItem(folderIndex);
						}

						if (holder == null) {
							holder = new FolderInfoHolder(context, folder,
									mAccount, -1);
						} else {
							holder.populate(context, folder, mAccount, -1);

						}
						if (folder.isInTopGroup()) {
							topFolders.add(holder);
						} else {
							newFolders.add(holder);
						}
					}
					// Collections.sort(newFolders);
					// Collections.sort(topFolders);
					topFolders.addAll(newFolders);
					mHandler.newFolders(topFolders);
				}
				super.listFolders(account, folders);
			}

			@Override
			public void synchronizeMailboxStarted(Account account, String folder) {
				super.synchronizeMailboxStarted(account, folder);
				if (account.equals(mAccount)) {

					// mHandler.progress(true);
					mHandler.folderLoading(folder, true);
					mHandler.dataChanged();
				}

			}

			@Override
			public void synchronizeMailboxFinished(Account account,
					String folder, int totalMessagesInMailbox,
					int numNewMessages) {
				super.synchronizeMailboxFinished(account, folder,
						totalMessagesInMailbox, numNewMessages);
				if (account.equals(mAccount)) {
					mHandler.progress(false);
					mHandler.folderLoading(folder, false);

					refreshFolder(account, folder);
				}

			}

			private void refreshFolder(Account account, String folderName) {
				// There has to be a cheaper way to get at the localFolder
				// object than this
				Folder localFolder = null;
				try {
					if (account != null && folderName != null) {
						if (!account.isAvailable(mActivity)) {
							Log.i(MailChat.LOG_TAG,
									"not refreshing folder of unavailable account");
							return;
						}
						localFolder = account.getLocalStore().getFolder(
								folderName);
						FolderInfoHolder folderHolder = getFolder(folderName);
						if (folderHolder != null) {
							folderHolder.populate(context, localFolder,
									mAccount, -1);
							folderHolder.flaggedMessageCount = -1;

							mHandler.dataChanged();
						}
					}
				} catch (Exception e) {
					Log.e(MailChat.LOG_TAG,
							"Exception while populating folder", e);
				} finally {
					if (localFolder != null) {
						localFolder.close();
					}
				}

			}

			@Override
			public void synchronizeMailboxFailed(Account account,
					String folder, String message) {
				super.synchronizeMailboxFailed(account, folder, message);
				if (!account.equals(mAccount)) {
					return;
				}

				mHandler.progress(false);
				mHandler.folderLoading(folder, false);

				// String mess = truncateStatus(message);

				// mHandler.folderStatus(folder, mess);
				FolderInfoHolder holder = getFolder(folder);

				if (holder != null) {
					holder.lastChecked = 0;
				}

				mHandler.dataChanged();

			}

			@Override
			public void setPushActive(Account account, String folderName,
					boolean enabled) {
			    // 此处容易导致崩溃，屏蔽对无用字段的更新
			    /*
				if (!account.equals(mAccount)) {
					return;
				}
				FolderInfoHolder holder = getFolder(folderName);

				if (holder != null) {
					holder.pushActive = enabled;

					mHandler.dataChanged();
				}
				*/
			}

			@Override
			public void messageDeleted(Account account, String folder,
					Message message) {
				synchronizeMailboxRemovedMessage(account, folder, message);
			}

			@Override
			public void emptyTrashCompleted(Account account) {
				if (account.equals(mAccount)) {
					refreshFolder(account, mAccount.getTrashFolderName());
				}
			}

			@Override
			public void folderStatusChanged(Account account, String folderName,
					int unreadMessageCount) {
				if (account.equals(mAccount)) {
					refreshFolder(account, folderName);
					informUserOfStatus();
				}
			}

			@Override
			public void sendPendingMessagesCompleted(Account account) {
				super.sendPendingMessagesCompleted(account);
				if (account.equals(mAccount)) {
					refreshFolder(account, mAccount.getOutboxFolderName());
				}
			}

			@Override
			public void sendPendingMessagesStarted(Account account) {
				super.sendPendingMessagesStarted(account);

				if (account.equals(mAccount)) {
					mHandler.dataChanged();
				}
			}

			@Override
			public void sendPendingMessagesFailed(Account account) {
				super.sendPendingMessagesFailed(account);
				if (account.equals(mAccount)) {
					refreshFolder(account, mAccount.getOutboxFolderName());
				}
			}

			@Override
			public void accountSizeChanged(Account account, long oldSize,
					long newSize) {
				if (account.equals(mAccount)) {
					mHandler.accountSizeChanged(oldSize, newSize);
				}
			}
		};

		public int getFolderIndex(String folder) {
			FolderInfoHolder searchHolder = new FolderInfoHolder();
			searchHolder.name = folder;
			return mFilteredFolders.indexOf(searchHolder);
		}

		public FolderInfoHolder getFolder(String folder) {
			FolderInfoHolder holder = null;

			int index = getFolderIndex(folder);
			if (index >= 0) {
				holder = (FolderInfoHolder) getItem(index);
				if (holder != null) {
					return holder;
				}
			}
			return null;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (position <= getCount()) {
				return getItemView(position, convertView, parent);
			} else {
				Log.e(MailChat.LOG_TAG, "getView with illegal positon="
						+ position + " called! count is only " + getCount());
				return null;
			}
		}

		@SuppressWarnings("static-access")
		public View getItemView(int itemPosition, View convertView,
				ViewGroup parent) {
			FolderInfoHolder folder = (FolderInfoHolder) getItem(itemPosition);
			View view;
			if (convertView != null) {
				view = convertView;
			} else {
				view = mInflater.inflate(R.layout.folder_list_item, parent,
						false);
			}

			FolderViewHolder holder = (FolderViewHolder) view.getTag();

			if (holder == null) {
				holder = new FolderViewHolder();
				holder.folderName = (TextView) view
						.findViewById(R.id.folder_name);
				holder.newMessageCount = (TextView) view
						.findViewById(R.id.new_message_count);
				holder.flaggedMessageCount = (TextView) view
						.findViewById(R.id.flagged_message_count);
				holder.newMessageCountWrapper = (View) view
						.findViewById(R.id.new_message_count_wrapper);
				holder.flaggedMessageCountWrapper = (View) view
						.findViewById(R.id.flagged_message_count_wrapper);
				holder.newMessageCountIcon = (View) view
						.findViewById(R.id.new_message_count_icon);
				holder.flaggedMessageCountIcon = (View) view
						.findViewById(R.id.flagged_message_count_icon);

				holder.folderStatus = (TextView) view
						.findViewById(R.id.folder_status);
				holder.activeIcons = (RelativeLayout) view
						.findViewById(R.id.active_icons);
				// holder.chip = view.findViewById(R.id.chip);
				holder.folderIcon = (ImageView) view
						.findViewById(R.id.folder_icon);

				holder.folderListItemLayout = (LinearLayout) view
						.findViewById(R.id.folder_list_item_layout);
				holder.rawFolderName = folder.name;

				view.setTag(holder);
			}

			if (folder == null) {
				return view;
			}
			// 名称
			holder.folderName.setText(folder.displayName);
			holder.folderIcon.setImageDrawable(FolderInfoHolder.getFolderIcon(
					context, mAccount, folder.displayName));
			final String folderStatus;
			// 状态
			if (folder.loading) {
				folderStatus = getString(R.string.status_loading);
			} else if (folder.status != null) {
				folderStatus = folder.status;
			} else if (folder.lastChecked != 0) {
				long now = System.currentTimeMillis();
				int flags = DateUtils.FORMAT_SHOW_TIME
						| DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_SHOW_YEAR;
				CharSequence formattedDate;
				// 接收时间
				if (Math.abs(now - folder.lastChecked) > DateUtils.WEEK_IN_MILLIS) {
					formattedDate = getString(R.string.preposition_for_date,
							DateUtils.formatDateTime(context,
									folder.lastChecked, flags));
				} else {
					formattedDate = DateUtils.getRelativeTimeSpanString(
							folder.lastChecked, now,
							DateUtils.MINUTE_IN_MILLIS, flags);
				}

				folderStatus = getString(
						folder.pushActive ? R.string.last_refresh_time_format_with_push
								: R.string.last_refresh_time_format,
						formattedDate);
			} else {
				folderStatus = null;
			}

			if (folderStatus != null) {
				holder.folderStatus.setText(folderStatus);
				holder.folderStatus.setVisibility(View.GONE);
			} else {
				holder.folderStatus.setVisibility(View.GONE);
			}

			if (folder.unreadMessageCount == -1) {
				folder.unreadMessageCount = 0;
				try {
					folder.unreadMessageCount = folder.folder
							.getUnreadMessageCount();
				} catch (Exception e) {
					Log.e(MailChat.LOG_TAG,
							"Unable to get unreadMessageCount for "
									+ mAccount.getDescription() + ":"
									+ folder.name);
				}
			}
			// 未读数
			if (folder.unreadMessageCount > 0) {
				holder.newMessageCount.setText(Integer
						.toString(folder.unreadMessageCount));
				holder.newMessageCountWrapper
						.setOnClickListener(createUnreadSearch(mAccount, folder));
				holder.newMessageCountWrapper.setVisibility(View.VISIBLE);
				holder.newMessageCountIcon.setBackgroundDrawable(mAccount
						.generateColorChip(false, false, false, false, false)
						.drawable());
			} else {
				holder.newMessageCountWrapper.setVisibility(View.GONE);
			}

			if (folder.flaggedMessageCount == -1) {
				folder.flaggedMessageCount = 0;
				try {
					folder.flaggedMessageCount = folder.folder
							.getFlaggedMessageCount();
				} catch (Exception e) {
					Log.e(MailChat.LOG_TAG,
							"Unable to get flaggedMessageCount for "
									+ mAccount.getDescription() + ":"
									+ folder.name);
				}

			}
			// if (selected != -1) {
			// mListView.setItemChecked(selected, true);
			// mListView.setSelection(selected);
			// }else {
			// Log.d(TAG, "---------------");
			// Log.d(TAG, "-0-"+mFolderName);
			// Log.d(TAG, "-1-"+folder.getDisplayName(context, mAccount,
			// folder.folder.getName()));
			// Log.d(TAG, "--"+folder.folder.equals(mFolderName)+"");
			// Log.d(TAG, "-1-"+folder.getDisplayName(context, mAccount,
			// folder.folder.getName()).equals(mFolderName)+"");
			// Log.d(TAG, "###################");
			// if
			// (folder.folder.equals(mFolderName)|folder.getDisplayName(context,
			// mAccount, folder.folder.getName()).equals(mFolderName)) {
			// mListView.setItemChecked(itemPosition, true);
			// mListView.setSelection(itemPosition);
			// }else {
			// mListView.setItemChecked(0, true);
			// mListView.setSelection(0);
			// }
			// }
			// 点击列表跳转
			if (MailChat.messageListStars() && folder.flaggedMessageCount > 0) {
				holder.flaggedMessageCount.setText(Integer
						.toString(folder.flaggedMessageCount));
				holder.flaggedMessageCountWrapper
						.setOnClickListener(createFlaggedSearch(mAccount,
								folder));
				holder.flaggedMessageCountWrapper.setVisibility(View.VISIBLE);
				holder.flaggedMessageCountIcon.setBackgroundDrawable(mAccount
						.generateColorChip(false, false, false, false, true)
						.drawable());

			} else {
				holder.flaggedMessageCountWrapper.setVisibility(View.GONE);
			}

			// holder.activeIcons.setOnClickListener(new OnClickListener() {
			// public void onClick(View v) {
			// Toast toast = Toast.makeText(mActivity.getApplication(),
			// getString(R.string.tap_hint), Toast.LENGTH_SHORT);
			// toast.show();
			// }
			// });

			// 设置选中状态 end
			if (MailChat.wrapFolderNames()) {
				holder.folderName.setEllipsize(null);
				holder.folderName.setSingleLine(false);
			} else {
				holder.folderName.setEllipsize(TruncateAt.END);
				holder.folderName.setSingleLine(true);
			}
			mFontSizes.setViewTextSize(holder.folderName,
					mFontSizes.getFolderName());
			mFontSizes.setViewTextSize(holder.folderStatus,
					mFontSizes.getFolderStatus());

			return view;
		}

		// private void setSeleted(FolderViewHolder holder) {
		// holder.chip.setBackgroundColor(getResources().getColor(
		// R.color.folder_left_shape_blue));
		// setItemChecked(0);
		// holder.folderName.setTextColor(getResources().getColor(
		// R.color.tv_choice_press));
		// holder.folderStatus.setTextColor(getResources().getColor(
		// R.color.tv_choice_press));
		// }

		// private void setUnSeleted(FolderViewHolder holder) {
		// // holder.chip.setBackgroundColor(getResources()
		// // .getColor(R.color.gray));
		// holder.folderName.setTextColor(getResources().getColor(
		// R.color.gray_text));
		// holder.folderStatus.setTextColor(getResources().getColor(
		// R.color.gray_text));
		// }

		private OnClickListener createFlaggedSearch(Account account,
				FolderInfoHolder folder) {
			String searchTitle = getString(
					R.string.search_title,
					getString(R.string.message_list_title,
							account.getDescription(), folder.displayName),
					getString(R.string.flagged_modifier));

			LocalSearch search = new LocalSearch(searchTitle);
			search.and(Searchfield.FLAGGED, "1", Attribute.EQUALS);

			search.addAllowedFolder(folder.name);
			search.addAccountUuid(account.getUuid());
			search.setFolderSecName(getString(R.string.folder_sec_flagged_title));
			return new FolderClickListener(search);
		}

		private OnClickListener createUnreadSearch(Account account,
				FolderInfoHolder folder) {
			String searchTitle = getString(
					R.string.search_title,
					getString(R.string.message_list_title,
							account.getDescription(), folder.displayName),
					getString(R.string.unread_modifier));

			LocalSearch search = new LocalSearch(searchTitle);
			search.and(Searchfield.READ, "1", Attribute.NOT_EQUALS);

			search.addAllowedFolder(folder.name);
			search.addAccountUuid(account.getUuid());
			search.setFolderSecName(getString(R.string.folder_sec_unread_title));
			// Log.d(TAG,folder.name);
			return new FolderClickListener(search);
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		public boolean isItemSelectable(int position) {
			return true;
		}

		public void setFilter(final Filter filter) {
			this.mFilter = filter;
		}

		public Filter getFilter() {
			return mFilter;
		}

		/**
		 * Filter to search for occurences of the search-expression in any place
		 * of the folder-name instead of doing jsut a prefix-search.
		 * 
		 * @author Marcus@Wolschon.biz
		 */
		public class FolderListFilter extends Filter {
			private CharSequence mSearchTerm;

			public CharSequence getSearchTerm() {
				return mSearchTerm;
			}

			/**
			 * Do the actual search. {@inheritDoc}
			 * 
			 * @see #publishResults(CharSequence, FilterResults)
			 */
			@Override
			protected FilterResults performFiltering(CharSequence searchTerm) {
				mSearchTerm = searchTerm;
				FilterResults results = new FilterResults();

				Locale locale = Locale.getDefault();
				if ((searchTerm == null) || (searchTerm.length() == 0)) {
					ArrayList<FolderInfoHolder> list = new ArrayList<FolderInfoHolder>(
							mFolders);
					results.values = list;
					results.count = list.size();
				} else {
					final String searchTermString = searchTerm.toString()
							.toLowerCase(locale);
					final String[] words = searchTermString.split(" ");
					final int wordCount = words.length;

					final ArrayList<FolderInfoHolder> newValues = new ArrayList<FolderInfoHolder>();

					for (final FolderInfoHolder value : mFolders) {
						if (value.displayName == null) {
							continue;
						}
						final String valueText = value.displayName
								.toLowerCase(locale);

						for (int k = 0; k < wordCount; k++) {
							if (valueText.contains(words[k])) {
								newValues.add(value);
								break;
							}
						}
					}

					results.values = newValues;
					results.count = newValues.size();
				}

				return results;
			}

			/**
			 * Publish the results to the user-interface. {@inheritDoc}
			 */
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				// noinspection unchecked
				mFilteredFolders = Collections
						.unmodifiableList((ArrayList<FolderInfoHolder>) results.values);
				// Send notification that the data set changed now
				notifyDataSetChanged();
			}
		}

	}

	static class FolderViewHolder {
		public TextView folderName;

		public TextView folderStatus;
		private ImageView folderIcon;
		public TextView newMessageCount;
		public TextView flaggedMessageCount;
		public View newMessageCountIcon;
		public View flaggedMessageCountIcon;
		public View newMessageCountWrapper;
		public View flaggedMessageCountWrapper;

		public RelativeLayout activeIcons;
		public String rawFolderName;
		// public View chip;
		public LinearLayout folderListItemLayout;
	}

	private class FolderClickListener implements OnClickListener {

		final LocalSearch search;

		FolderClickListener(LocalSearch search) {
			this.search = search;
		}

		@Override
		public void onClick(View v) {

			mFolderListListener.onChangeFolder(search, "");
			// Main4TabActivity
			// .actionDisplaySearch(mActivity, search, true, false);
		}
	}

	public interface FolderListListener {
		public void onChangeFolder(LocalSearch search, String folder);
	}

	public void setSelected(int selected) {
		this.selected = selected;
	}

}
