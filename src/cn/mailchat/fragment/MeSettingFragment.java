package cn.mailchat.fragment;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.AccountStats;
import cn.mailchat.BaseAccount;
import cn.mailchat.EmailAddressValidator;
import cn.mailchat.GlobalConstants;
import cn.mailchat.LogCollector;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.AccountSettingActivity;
import cn.mailchat.activity.RecommendUseMailChatActivity;
import cn.mailchat.activity.SetPasswordActivity;
import cn.mailchat.activity.SettingFeedbackActivity;
import cn.mailchat.activity.WebViewWithErrorViewActivity;
import cn.mailchat.activity.setup.SelectEmailActivity;
import cn.mailchat.adapter.AccountAdapter;
import cn.mailchat.chatting.protocol.Connection;
import cn.mailchat.chatting.protocol.Protocol;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.fragment.MeSettingFragment.RingAdapter.RingViewHolder;
import cn.mailchat.helper.SizeFormatter;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.mail.internet.MimeUtility;
import cn.mailchat.mail.store.LocalStore;
import cn.mailchat.mail.store.StorageManager;
import cn.mailchat.utils.RSAUtils;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.utils.Utility;
import cn.mailchat.view.MailDialog;
import cn.mailchat.utils.WeemailUtil;

import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.net.w;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

/**
 * 
 * @copyright © 35.com
 * @file name ：TabDFragment.java
 * @author ：zhangjx
 * @create Data ：2014-9-18下午5:30:02
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-9-18下午5:30:02
 * @Modified by：zhangjx
 * @Description :我--页面
 */
public class MeSettingFragment extends Fragment {
	private static final String TAG = "MeSettingFragment";
	private List<BaseAccount> accounts = new ArrayList<BaseAccount>();
	// private SearchAccount mAllMessagesAccount = null;
	// private SearchAccount mUnifiedInboxAccount = null;
	private static final int SPECIAL_ACCOUNTS_COUNT = 2;
	private static final BaseAccount[] EMPTY_BASE_ACCOUNT_ARRAY = new BaseAccount[0];
	private static final String PARAMETRIC = "parametric";
	/*
	 * Must be serializable hence implementation class used for declaration.
	 */
	private ConcurrentHashMap<String, AccountStats> accountStats = new ConcurrentHashMap<String, AccountStats>();
	private ConcurrentMap<BaseAccount, String> pendingWork = new ConcurrentHashMap<BaseAccount, String>();
	private ListView listAccount;
	// private ListView switchSetAccount;
	// 验证邮箱格式
	private EmailAddressValidator mEmailValidator = new EmailAddressValidator();
	// List<Map<String, Object>> listData = new ArrayList<Map<String,
	// Object>>();
	private LinearLayout addAccount;
	// 新消息通知
	private RelativeLayout topMsgNotify;
	private CheckBox isTopMsgNotify;
	// 新邮件通知
	private RelativeLayout mLayoutNotifySwitch;
	private CheckBox mCheckBoxNotify;
	// 安静时间段控件
	private RelativeLayout setQuietTime;/* 打开关闭安静时间 */
	private CheckBox isSetQuietTime;/* 打开关闭安静时间 */
	private LinearLayout layoutQuiet;/* 隐藏显示安静时间 */
	private RelativeLayout setQuietStart;/* 设置安静开始时间 */
	private TextView quietStart;/* 安静时间开始 */
	private RelativeLayout setQuietEnd;/* 设置安静结束时间 */
	private TextView quietEnd, aboutMailChat;/* 安静时间结束 */
	private LayoutInflater mLayoutInflater;
	private Context mContext;
	// private MeSettingFragmentListener mMeSettingFragmentListener;

	private CheckBox mCheckBoxBell;
	private CheckBox mCheckBoxVibrate;
	private RelativeLayout mLayoutVibrateSwitch;
	private RelativeLayout mLayoutBeelSwitch;
	private TextView mTextVersonName;
	private AccountAdapter mAdapter;
	private MessagingController mController;
	private StorageManager mStorageManager;
	private RelativeLayout checkNewVersion;
	private TextView mTextPrivacyAgreement;
	private RelativeLayout mLayoutFeedback;
	private AccountsHandler mHandler = new AccountsHandler();
	private int mUnreadMessageCount = 0;
	private static String ACCOUNT_STATS = "accountStats";
	private static String STATE_UNREAD_COUNT = "unreadCount";
	private static String SELECTED_CONTEXT_ACCOUNT = "selectedContextAccount";

	public static final String EXTRA_STARTUP = "startup";

	public static final String ACTION_IMPORT_SETTINGS = "importSettings";
	private String mSelectedContextAccount;
	private RelativeLayout modifyGesturePassword;
	private RelativeLayout gesture;
	private CheckBox isGesture;
	private RelativeLayout mLayoutAttachmentShortcuts;
	private CheckBox mCheckBoxAttachmentShortcuts;
	private RelativeLayout mLayout35CloudServices;
	private CheckBox mCheckBox35CloudServices;
	private RelativeLayout mLayoutNotifySyncError;
	private CheckBox mCheckBoxNotifySyncError;
	private LinearLayout mLayoutBetaFunctions;
	private RelativeLayout recommendedLayout;
	private RelativeLayout surveyLayout, activityLayout;
	private View activityLineView, setRingLineView;
	private LinearLayout setRingLayout;
	private TextView setRingNameTv;
	private String mHttpUrl = GlobalConstants.BASE_URL;
	private String mMqttHost = GlobalConstants.MQTT_HOST;
	private int mMqttPort = GlobalConstants.MQTT_PORT;

	public static MeSettingFragment newInstance(String account) {
		MeSettingFragment fragment = new MeSettingFragment();
		Bundle args = new Bundle();
		args.putString(PARAMETRIC, account);
		fragment.setArguments(args);

		return fragment;
	}

	class AccountsHandler extends Handler {
		// 是否显示三五企业邮箱云服务配置项
		public void showBetaFunctions() {
			MailChat.runOnUiThread(new Runnable() {
				public void run() {
					if (MailChat.has35Account()) {
						mLayoutBetaFunctions.setVisibility(View.VISIBLE);
					} else {
						mLayoutBetaFunctions.setVisibility(View.GONE);
					}
				}
			});
		}

		public void dataChanged() {
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					if (mAdapter != null) {
						mAdapter.notifyDataSetInvalidated();
					}
				}
			});
		}

		public void workingAccount(final Account account, final int res) {
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					String toastText = getString(res, account.getDescription());

					Toast toast = Toast.makeText(getActivity(), toastText,
							Toast.LENGTH_SHORT);
					toast.show();
				}
			});
		}

		public void accountSizeChanged(final Account account,
				final long oldSize, final long newSize) {
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					AccountStats stats = accountStats.get(account.getUuid());
					if (newSize != -1 && stats != null
							&& MailChat.measureAccounts()) {
						stats.size = newSize;
					}
					String toastText = getString(R.string.account_size_changed,
							account.getDescription(),
							SizeFormatter.formatSize(getActivity(), oldSize),
							SizeFormatter.formatSize(getActivity(), newSize));

					Toast toast = Toast.makeText(getActivity(), toastText,
							Toast.LENGTH_LONG);
					toast.show();
					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
					}
				}
			});
		}
	}

	MessagingListener mListener = new MessagingListener() {

		private String receiveEmail = "";
		private boolean isSameEmail;

		@Override
		public void loadUserInfoSuccess(Account account,
				final ContactAttribute newContactAttribute) {
			if (account.getEmail().equals(mSelectedContextAccount)) {
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						Preferences.getPreferences(getActivity())
								.getDefaultAccount()
								.setName(newContactAttribute.getNickName());
						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
						}
					}
				});
			}
		}

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
			mHandler.dataChanged();
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
		// 显示非当前账号收到消息或邮件红点
		// @Override
		// public void receiveMessage(final Account acc,Folder folder){
		// // if (!acc.getEmail().equals(mSelectedContextAccount)) {
		// // acc.setmIsHaveUnreadMsg(true);
		// // mHandler.dataChanged();
		// // mMeSettingFragmentListener.showUnreadCount(true);
		// // }
		// //如果接收到邮件的是当前账号，通知主界面显示左上角红点
		// if (folder!=null&&receiveEmail.equals(mSelectedContextAccount)) {
		// mMeSettingFragmentListener.showIsUnReadMail(folder);
		// return;
		// }
		// //如果接收到消息的用户和上次接收到的不一致，并且没有未读消息
		// if (!receiveEmail.equals(acc.getEmail())&&!acc.getIsHaveUnreadMsg())
		// {
		// receiveEmail=acc.getEmail();
		// isSameEmail=false;
		// }else {
		// //如果是相同用户接收到消息，并且红点还在的话，不在通知显示红点了
		// isSameEmail=true;
		// }
		//
		// if (!isSameEmail&&!receiveEmail.equals(mSelectedContextAccount)) {
		// acc.setmIsHaveUnreadMsg(true);
		// mHandler.dataChanged();
		// mMeSettingFragmentListener.showUnreadCount(true);
		// }
		// }
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Log.d(TAG, "MeSettingFragment-->>onAttach");
		mContext = activity.getApplicationContext();
		// try {
		// mMeSettingFragmentListener = (MeSettingFragmentListener) activity;
		// } catch (ClassCastException e) {
		// throw new ClassCastException(activity.getClass() +
		// " must implement MeSettingFragmentListener");
		// }
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Log.d(TAG, "MeSettingFragment-->>onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Log.d("qxian", "MeSettingFragment-->>onCreateView");
		Context context = new ContextThemeWrapper(inflater.getContext(),
				MailChat.getK9ThemeResourceId(MailChat.getK9MessageViewTheme()));
		mLayoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mLayoutInflater.inflate(R.layout.fragment_tab_me,
				container, false);
		initWidget(view);
		initData(savedInstanceState);
		setWidgetEvent();

		return view;
	}

	private void initData(Bundle savedInstanceState) {
		mController = MessagingController.getInstance(getActivity()
				.getApplication());
		mStorageManager = StorageManager.getInstance(getActivity()
				.getApplication());
		mSelectedContextAccount = getArguments().getString(PARAMETRIC);
		mController.addListener(mListener);
		restoreAccountStats(savedInstanceState);
		mAdapter = new AccountAdapter(mContext, mSelectedContextAccount, true);
		listAccount.setAdapter(mAdapter);
		// BaseAccount[] accounts =
		// newAccounts.toArray(EMPTY_BASE_ACCOUNT_ARRAY);
		// for (int i = 0; i < accounts.length; i++) {
		// Map<String, Object> data = new HashMap<String, Object>();
		// if (mEmailValidator.isValidAddressOnly(accounts[i].getEmail())) {
		// data.put("account", accounts[i]);
		// data.put("email", accounts[i].getEmail());
		// data.put("uuid", accounts[i].getUuid());
		// listData.add(data);
		// }
		// }
		addAccount.setOnClickListener(clickListener);
		// TODO 账号设置
		// switchSetAccount.setVisibility(View.GONE);
		Preferences prefs = Preferences.getPreferences(getActivity());
		((MailChat) MailChat.app).loadPrefs(prefs);
		mCheckBoxNotify.setChecked(MailChat.isTopNotifyOn());
		mCheckBoxBell.setChecked(MailChat.isNotifyRingtone());
		if (MailChat.isNotifyRingtone()) {
			setRingLayout.setVisibility(View.VISIBLE);
			setRingLineView.setVisibility(View.VISIBLE);
		} else {
			setRingLayout.setVisibility(View.GONE);
			setRingLineView.setVisibility(View.GONE);
		}
		String ringName = MailChat.getSelectNotifyRingtoneName();
		if (StringUtil.isEmpty(ringName)) {
			setRingNameTv.setText(getString(R.string.setting_ring_auto));
		} else {
			setRingNameTv.setText(ringName);
		}
		mCheckBoxVibrate.setChecked(MailChat.isNotifyVibrateOn());
		isSetQuietTime.setChecked(MailChat.getQuietTimeEnabled());
		isTopMsgNotify.setChecked(MailChat.isTopMsgNotify());
		quietStart.setText(MailChat.getQuietTimeStarts());
		quietEnd.setText(MailChat.getQuietTimeEnds());
		setQuietDefaultData(isSetQuietTime.isChecked());
		isGesture.setChecked(MailChat.isGesture());
		mCheckBoxAttachmentShortcuts.setChecked(MailChat
				.isMessageListAttachmentShortcuts());
		mCheckBox35CloudServices.setChecked(MailChat.is35CloudServices());
		mCheckBoxNotifySyncError.setChecked(MailChat.isNotifySyncError());

		// 版本号
		mTextVersonName
				.setText(getString(R.string.setting_about35Mail_current_version)
						+ getString(R.string.version_name));
	}

	@SuppressWarnings("unchecked")
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

	private StorageManager.StorageListener storageListener = new StorageManager.StorageListener() {

		@Override
		public void onUnmount(String providerId) {
			refresh();
		}

		@Override
		public void onMount(String providerId) {
			refresh();
		}
	};

	public void refreshListData() {
		refresh();
		if (mAdapter != null) {
			mAdapter.notifyDataSetInvalidated();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		refresh();
		// 根据手势密码设置来判断显示
		isGesture.setChecked(SetPasswordActivity.ifHasGPassword());
		savePrefsParameter();
		gesturePasswordViewShow();
		mStorageManager.addListener(storageListener);
		// mListener.onResume(mContext);
		MobclickAgent.onPageStart("MeSettingFragment");
	}

	@Override
	public void onPause() {
		super.onPause();
		mController.removeListener(mListener);
		mStorageManager.removeListener(storageListener);
		// mListener.onPause(mContext);
		MobclickAgent.onPageEnd("MeSettingFragment");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Log.d(TAG, "MeSettingFragment-->>onActivityCreated");
	}

	private void initWidget(View view) {
		mCheckBoxNotify = (CheckBox) view.findViewById(R.id.is_top_notify);
		mCheckBoxBell = (CheckBox) view.findViewById(R.id.is_bell_open);
		mCheckBoxVibrate = (CheckBox) view.findViewById(R.id.is_vibrate_open);
		mLayoutNotifySwitch = (RelativeLayout) view
				.findViewById(R.id.top_notify);
		mLayoutBeelSwitch = (RelativeLayout) view.findViewById(R.id.set_bell);
		mLayoutVibrateSwitch = (RelativeLayout) view
				.findViewById(R.id.set_vibrate);
		mTextPrivacyAgreement = (TextView) view
				.findViewById(R.id.privacy_agreement); // 隐私协议
		mLayoutFeedback = (RelativeLayout) view
				.findViewById(R.id.setting_feedback);

		listAccount = (ListView) view.findViewById(R.id.message_switch_account);
		// switchSetAccount = (ListView) view
		// .findViewById(R.id.message_switch_set_account);
		addAccount = (LinearLayout) view.findViewById(R.id.mine_add_account);
		view.findViewById(R.id.mine_add_account_line).setVisibility(View.GONE);
		// 新消息提醒
		topMsgNotify = (RelativeLayout) view.findViewById(R.id.top_msg_notify);
		isTopMsgNotify = (CheckBox) view.findViewById(R.id.is_top_msg_notify);
		/** 安静时间段设置控件初始化 **/
		/* 打开关闭安静时间 */
		setQuietTime = (RelativeLayout) view.findViewById(R.id.set_quiet_time);
		/* 打开关闭安静时间 */
		isSetQuietTime = (CheckBox) view.findViewById(R.id.is_set_quiet_time);
		/* 隐藏显示安静时间 */
		layoutQuiet = (LinearLayout) view.findViewById(R.id.layout_quiet);
		/* 设置安静开始时间 */
		setQuietStart = (RelativeLayout) view
				.findViewById(R.id.set_quiet_start);
		/* 安静时间开始 */
		quietStart = (TextView) view.findViewById(R.id.quiet_start);
		/* 设置安静结束时间 */
		setQuietEnd = (RelativeLayout) view.findViewById(R.id.set_quiet_end);
		/* 安静时间结束 */
		quietEnd = (TextView) view.findViewById(R.id.quiet_end);
		aboutMailChat = (TextView) view.findViewById(R.id.about_mailchat);
		layoutQuiet.setVisibility(View.GONE);
		// 版本号
		mTextVersonName = (TextView) view
				.findViewById(R.id.setting_main_current_version);
		checkNewVersion = (RelativeLayout) view
				.findViewById(R.id.setting_main_check_version_parent);
		modifyGesturePassword = (RelativeLayout) view
				.findViewById(R.id.modify_gesture_password);
		gesture = (RelativeLayout) view.findViewById(R.id.gesture);
		isGesture = (CheckBox) view.findViewById(R.id.is_gesture);
		mLayoutAttachmentShortcuts = (RelativeLayout) view
				.findViewById(R.id.layout_attachment_shortcuts);
		mCheckBoxAttachmentShortcuts = (CheckBox) view
				.findViewById(R.id.checkbox_attachment_shortcuts);
		mLayout35CloudServices = (RelativeLayout) view
				.findViewById(R.id.layout_35_cloud_services);
		mCheckBox35CloudServices = (CheckBox) view
				.findViewById(R.id.checkbox_35_cloud_services);
		mLayoutNotifySyncError = (RelativeLayout) view
				.findViewById(R.id.layout_notify_sync_error);
		mCheckBoxNotifySyncError = (CheckBox) view
				.findViewById(R.id.checkbox_notify_sync_error);
		mLayoutBetaFunctions = (LinearLayout) view
				.findViewById(R.id.layout_beta_functions);
		recommendedLayout = (RelativeLayout) view
				.findViewById(R.id.setting_recommend);
		surveyLayout = (RelativeLayout) view.findViewById(R.id.setting_survey);
		activityLayout = (RelativeLayout) view
				.findViewById(R.id.setting_activity);
		activityLineView = view.findViewById(R.id.setting_activity_line);
		setRingLayout = (LinearLayout) view.findViewById(R.id.set_ring);
		setRingNameTv = (TextView) view.findViewById(R.id.set_ring_name);
		setRingLineView = view.findViewById(R.id.set_ring_line);
	}

	private void setWidgetEvent() {
		setQuietTime.setOnClickListener(clickListener);
		setQuietStart.setOnClickListener(clickListener);
		setQuietEnd.setOnClickListener(clickListener);
		isSetQuietTime.setOnClickListener(clickListener);
		isSetQuietTime.setOnCheckedChangeListener(checkedListener);
		topMsgNotify.setOnClickListener(clickListener);
		isTopMsgNotify.setOnClickListener(clickListener);

		mCheckBoxNotify.setOnClickListener(clickListener);
		mCheckBoxBell.setOnClickListener(clickListener);
		mCheckBoxVibrate.setOnClickListener(clickListener);

		mLayoutNotifySwitch.setOnClickListener(clickListener);
		mLayoutBeelSwitch.setOnClickListener(clickListener);
		mLayoutVibrateSwitch.setOnClickListener(clickListener);
		checkNewVersion.setOnClickListener(clickListener);
		mTextPrivacyAgreement.setOnClickListener(clickListener);
		mLayoutFeedback.setOnClickListener(clickListener);
		aboutMailChat.setOnLongClickListener(aboutLongClickListener());
		modifyGesturePassword.setOnClickListener(clickListener);
		gesture.setOnClickListener(clickListener);
		isGesture.setOnClickListener(clickListener);
		mLayoutAttachmentShortcuts.setOnClickListener(clickListener);
		mCheckBoxAttachmentShortcuts.setOnClickListener(clickListener);
		setRingLayout.setOnClickListener(clickListener);
		mLayout35CloudServices.setOnClickListener(clickListener);
		mCheckBox35CloudServices.setOnClickListener(clickListener);
		mLayoutNotifySyncError.setOnClickListener(clickListener);
		mCheckBoxNotifySyncError.setOnClickListener(clickListener);

		recommendedLayout.setOnClickListener(clickListener);
		surveyLayout.setOnClickListener(clickListener);
		activityLayout.setOnClickListener(clickListener);

		// 隐藏功能入口
		mLayoutFeedback.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				DialogFragment dialog = new HiddenFeaturesDialogFragment();
				dialog.show(getActivity().getSupportFragmentManager(),
						"HiddenFeatures");
				return true;
			}
		});
	}

	// 隐藏功能对话框
	private class HiddenFeaturesDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Protocol.getInstance();// 初始化一下，防止意外情况
			final String email = Preferences.getPreferences(mContext)
					.getDefaultAccount().getEmail();
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getString(R.string.hidden_features))
					.setItems(
							email.endsWith("@35.cn") ? new String[] {
									MailChat.logCollector == null ? getString(R.string.log_collector_start)
											: getString(R.string.log_collector_stop),
									Connection.getInstance(getActivity())
											.getConnectionOptions()
											.isCleanSession() ? "接收离线消息"
											: "清空mqtt缓存","切换服务器地址 (" + getHostName() + ")" }
									: new String[] {
											MailChat.logCollector == null ? getString(R.string.log_collector_start)
													: getString(R.string.log_collector_stop),
											Connection
													.getInstance(getActivity())
													.getConnectionOptions()
													.isCleanSession() ? "接收离线消息"
													: "清空mqtt缓存"

									},

							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
										if (MailChat.logCollector == null) {
											MailChat.logCollector = new LogCollector(
													new String[] {
															MailChat.LOG_COLLECTOR_TAG
																	+ ":V",
															"*:E" },
													"threadtime", null);
											MailChat.logCollector.startLog();
										} else {
											MailChat.logCollector.stopLog();
											MailChat.logCollector = null;
										}
										break;
									case 1:
										if (!Connection
												.getInstance(getActivity())
												.getConnectionOptions()
												.isCleanSession()) {
											mController.MQTTConnect(true);
										} else {
											mController.MQTTConnect(false);
										}
										break;
									case 2:
										if (email.endsWith("@35.cn")) {
											dialogChooseServerHost();
										} else {
											MailChat.toast("当前用户不支持此功能");
										}
										break;
									}
								}
							});
			return builder.create();
		}
	}

	private String getHostName() {
		if (mMqttHost.equals(GlobalConstants.MQTT_HOST)) {
			return "正式";
		} else if (mMqttHost.equals(GlobalConstants.MQTT_HOST_TEST)) {
			return "测试";
		} else {
			return "内网";
		}
	}

	/**
	 * 
	 * method name: aboutLongClickListener function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @return field_name OnLongClickListener return type
	 * @History memory：
	 * @Date：2015-5-22 下午3:29:58 @Modified by：zhangjx
	 * @Description：长按进入webOA
	 */
	private OnLongClickListener aboutLongClickListener() {
		return new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Account account = Preferences.getPreferences(mContext)
						.getDefaultAccount();
				if (!account.getEmail().endsWith("@35.cn")) {
					MailChat.toast(getString(R.string.fail_to_35OA));
				} else {
					// jumpToWebOA(account);
					showJumpOaDialog();
				}
				return true;
			}
		};
	}

	// private void jumpToWebOA(Account account) {
	// try {
	// StringBuffer sb = new StringBuffer();
	// sb.append("u=");
	// String urlUsername = URLEncoder.encode(account.getEmail(), "UTF-8");
	// String urlPassword = URLEncoder.encode(Utility.getUserPassword(new
	// URI(account.getStoreUri())), "UTF-8");
	// // String urlUsername =
	// URLEncoder.encode("huangwj@huangwj.water2.35test.cn", "UTF-8");
	// // String urlPassword = URLEncoder.encode("111111", "UTF-8");
	// long l = System.currentTimeMillis() / 1000;
	// String strTimestamp = Long.toString(l);
	// sb.append(urlUsername);
	// sb.append("&");
	// sb.append("toPage=webapp");
	// sb.append("&");
	// sb.append("p=");
	// sb.append(urlPassword);
	// sb.append("&");
	// sb.append("t=");
	// sb.append(strTimestamp);
	// String rsaUrl = Base64.encodeToString(
	// RSAUtils.encryptByPublicKey(sb.toString().getBytes("UTF-8"),
	// GlobalConstants.OA_PUBLIC_KEY),
	// Base64.DEFAULT);
	// String resultUrl = GlobalConstants.OA_BASE_URL_START
	// + Utility.getEmailDomain(account.getEmail())
	// // + Utility.getEmailDomain("huangwj@huangwj.water2.35test.cn")
	// + GlobalConstants.OA_BASE_MAIN_URL_END + "&p=" + rsaUrl;
	// // 长按跳转OA
	// // resultUrl = "http://10.35.51.204:8080/MailChatJsSdkTest.html";
	// WebViewWithErrorViewActivity.forwardOpenUrlActivity(getActivity(),
	// resultUrl, null,
	// account.getUuid(), true);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	private void setQuietDefaultData(boolean isShow) {
		if (isShow) {
			layoutQuiet.setVisibility(View.VISIBLE);
			quietStart.setText(MailChat.getQuietTimeStarts());
			quietEnd.setText(MailChat.getQuietTimeEnds());
		} else {
			layoutQuiet.setVisibility(View.GONE);
		}
	}

	private void addNewAccount() {
		SelectEmailActivity.showSelectEmailActivity(mContext);
	}

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.mine_add_account:
				// 新增加邮箱账号
				addNewAccount();
				MobclickAgent.onEvent(mContext, "add_account");
				break;
			case R.id.top_notify:
				// 通知开关
				mCheckBoxNotify.setChecked(!mCheckBoxNotify.isChecked());
				savePrefsParameter();
				break;
			case R.id.set_bell:
				// 铃音开关
				mCheckBoxBell.setChecked(!mCheckBoxBell.isChecked());
				savePrefsParameter();
				if (mCheckBoxBell.isChecked()) {
					setRingLayout.setVisibility(View.VISIBLE);
					setRingLineView.setVisibility(View.VISIBLE);
				} else {
					setRingLayout.setVisibility(View.GONE);
					setRingLineView.setVisibility(View.GONE);
				}
				break;
			case R.id.set_vibrate:
				// 震动开关
				mCheckBoxVibrate.setChecked(!mCheckBoxVibrate.isChecked());
				savePrefsParameter();
				break;
			case R.id.is_top_notify:
				// 通知开关
				savePrefsParameter();
				break;
			case R.id.is_vibrate_open:
				// 震动开关
				savePrefsParameter();
				break;
			case R.id.is_bell_open:
				// 铃音开关
				savePrefsParameter();
				if (mCheckBoxBell.isChecked()) {
					setRingLayout.setVisibility(View.VISIBLE);
					setRingLineView.setVisibility(View.VISIBLE);
				} else {
					setRingLayout.setVisibility(View.GONE);
					setRingLineView.setVisibility(View.GONE);
				}
				break;
			case R.id.set_quiet_time:
				isSetQuietTime.setChecked(!isSetQuietTime.isChecked());
				savePrefsParameter();
				break;
			case R.id.is_set_quiet_time:
				savePrefsParameter();
				break;
			case R.id.set_quiet_start:
				showQuietTime(quietStart);
				break;
			case R.id.set_quiet_end:
				showQuietTime(quietEnd);
				break;
			case R.id.top_msg_notify:
				isTopMsgNotify.setChecked(!isTopMsgNotify.isChecked());
				savePrefsParameter();
				break;
			case R.id.is_top_msg_notify:
				savePrefsParameter();
				break;
			case R.id.setting_main_check_version_parent:
				checkUpdate();
				break;
			case R.id.privacy_agreement:
				String seretUrl = "http://www.mailchat.cn/xieyi.html";
				// 隐私协议跳转
				WebViewWithErrorViewActivity.forwardOpenUrlActivity(
						getActivity(), seretUrl, null, Preferences
								.getPreferences(mContext).getDefaultAccount()
								.getUuid(), -1, false);
				break;
			case R.id.setting_feedback:
				SettingFeedbackActivity.actionView(getActivity());
				// UmengFeekbackActivity.actionChatList(getActivity(),null);
				// FeedbackAgent agent = new FeedbackAgent(getActivity());
				// agent.startFeedbackActivity();
				break;
			case R.id.modify_gesture_password:
				SetPasswordActivity.startActivity(getActivity(), false, true,
						true, false, true);
				break;
			case R.id.gesture:
				isGesture.setChecked(!isGesture.isChecked());
				savePrefsParameter();
				SetPasswordActivity.startActivity(getActivity(), false, true,
						true, !isGesture.isChecked(), false);
				break;
			case R.id.is_gesture:
				savePrefsParameter();
				SetPasswordActivity.startActivity(getActivity(), false, true,
						true, !isGesture.isChecked(), false);
				break;
			case R.id.layout_attachment_shortcuts:
				mCheckBoxAttachmentShortcuts
						.setChecked(!mCheckBoxAttachmentShortcuts.isChecked());
			case R.id.checkbox_attachment_shortcuts:
				savePrefsParameter();
				break;
			case R.id.layout_35_cloud_services:
				mCheckBox35CloudServices.setChecked(!mCheckBox35CloudServices
						.isChecked());
			case R.id.checkbox_35_cloud_services:
				if (mCheckBox35CloudServices.isChecked()) {
					MobclickAgent.onEvent(getActivity(), "att_server_forward");
				} else {
					WeemailUtil.clearC35Accounts();
				}
				savePrefsParameter();
				break;
			case R.id.layout_notify_sync_error:
				mCheckBoxNotifySyncError.setChecked(!mCheckBoxNotifySyncError
						.isChecked());
			case R.id.checkbox_notify_sync_error:
				savePrefsParameter();
				break;
			case R.id.setting_recommend:
				RecommendUseMailChatActivity.startActivity(getActivity());
				break;
			case R.id.setting_survey:
				WebViewWithErrorViewActivity.forwardOpenUrlActivity(mContext,
						GlobalConstants.SURVEY_URL, null, Preferences
								.getPreferences(mContext).getDefaultAccount()
								.getUuid(),-1, false);
				break;
			// case R.id.setting_activity:
			// break;
			case R.id.set_ring:
				showSelectRingDialog();
				break;
			default:
				break;
			}
		}
	};

	private OnCheckedChangeListener checkedListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			switch (buttonView.getId()) {
			case R.id.is_set_quiet_time:
				setQuietDefaultData(isChecked);
				break;
			case R.id.is_top_msg_notify:
				break;
			case R.id.is_bell_open:
				break;

			default:
				break;
			}

		}

	};

	/**
	 * Creates and initializes the special accounts ('Unified Inbox' and 'All
	 * Messages')
	 */
	private void createSpecialAccounts() {
		// mUnifiedInboxAccount = SearchAccount
		// .createUnifiedInboxAccount(mContext);
		// mAllMessagesAccount =
		// SearchAccount.createAllMessagesAccount(mContext);
	}

	private void refresh() {
		accounts.clear();
		accounts.addAll(Preferences.getPreferences(MailChat.app).getAccounts());
		List<BaseAccount> newAccounts = new ArrayList<BaseAccount>();
		newAccounts.addAll(accounts);
		// 查看账户列表
		for (BaseAccount logAccount : accounts) {
			Log.i(MailChat.LOG_COLLECTOR_TAG,
					"Current Account : " + logAccount.getEmail());
		}
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
		mAdapter.setAccounts(newAccounts, mSelectedContextAccount);
		mHandler.showBetaFunctions();
		// setListViewHeightBasedOnChildren(listAccount);
		listAccount.setOnItemClickListener(switchSetListener);
		for (BaseAccount account : newAccounts) {
			pendingWork.put(account, "true");
			// if (account.getEmail().equals(mSelectedContextAccount)) {
			// mController.loadRemoteUserInfo((Account) account, mListener);
			// }
		}
	}

	private boolean isChecking;
	private int flag;

	/**
	 * 账号设置list事件监听
	 */
	private OnItemClickListener switchSetListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			saveShowTipsTag();
			String accountUuid = accounts.get(position).getUuid();
			Account account = Preferences.getPreferences(mContext).getAccount(
					accountUuid);
			AccountSettingActivity.actionAccountSettingActivityForResult(
					getActivity(), account, position);
		}
	};

	/**
	 * 账号设置list事件监听
	 */
	// private OnItemClickListener switchSetListener = new OnItemClickListener()
	// {
	//
	// @Override
	// public void onItemClick(AdapterView<?> parent, View view, int position,
	// long id) {
	// Toast.makeText(mContext, "功能开发中，敬请期待！", Toast.LENGTH_SHORT).show();
	// }
	// };

	/**
	 * 保存控制参数
	 */
	private void savePrefsParameter() {
		SharedPreferences preferences = Preferences.getPreferences(mContext)
				.getPreferences();
		MailChat.setTopNotifyOn(mCheckBoxNotify.isChecked());
		MailChat.setNotifyRingtone(mCheckBoxBell.isChecked());
		MailChat.setNotifyVibrateOn(mCheckBoxVibrate.isChecked());
		MailChat.setQuietTimeEnabled(isSetQuietTime.isChecked());
		MailChat.setTopMsgNotify(isTopMsgNotify.isChecked());
		MailChat.setGesture(isGesture.isChecked());
		MailChat.setMessageListAttachmentShortcuts(mCheckBoxAttachmentShortcuts
				.isChecked());
		MailChat.set35CloudServices(mCheckBox35CloudServices.isChecked());
		MailChat.setNotifySyncError(mCheckBoxNotifySyncError.isChecked());

		if (isSetQuietTime.isChecked()) {
			MailChat.setQuietTimeStarts(quietStart.getText().toString().trim());
			MailChat.setQuietTimeEnds(quietEnd.getText().toString().trim());
		}
		Editor editor = preferences.edit();
		MailChat.save(editor);
		editor.commit();

	}

	private void showQuietTime(final TextView view) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		TimePickerDialog dialog = new TimePickerDialog(getActivity(),
				new OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker arg0, int hour, int minute) {
						String hours = String.valueOf(hour).length() == 1 ? "0"
								+ String.valueOf(hour) : String.valueOf(hour);
						String minutes = String.valueOf(minute).length() == 1 ? "0"
								+ String.valueOf(minute)
								: String.valueOf(minute);
						view.setText(hours + ":" + minutes);
						savePrefsParameter();
					}

				}, hour, minute, true);
		dialog.show();
	}

	@Override
	public void onStart() {
		super.onStart();
		// Log.d(TAG, "MeSettingFragment-->>onStart");
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			// Log.d(TAG, "MeSettingFragment-->>isVisibleToUser  "
			// + isVisibleToUser);

		} else {
			// 相当于Fragment的onPause
			// Log.d(TAG, "MeSettingFragment-->>!!!!!!!isVisibleToUser "
			// + isVisibleToUser);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mController.removeListener(mListener);
	}

	// public interface MeSettingFragmentListener {
	// void changeAccount(LocalSearch search, Account account);
	// void setCurrentItem(int item);
	// void showUnreadCount(boolean isShow);
	// void replace4Tabs();
	// void showIsUnReadMail(Folder f);
	// }

	/** 检查更新 */
	private void checkUpdate() {
		if (isChecking) {
			return;
		}
		isChecking = true;
		Toast.makeText(mContext,
				getResources().getString(R.string.UMCheck_new_version),
				Toast.LENGTH_SHORT).show();
		UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {

			@Override
			public void onUpdateReturned(int updateStatus,
					UpdateResponse updateInfo) {
				// TODO Auto-generated method stub
				switch (updateStatus) {
				case UpdateStatus.Yes: // has update
					UmengUpdateAgent
							.showUpdateDialog(getActivity(), updateInfo);
					// showUpdateDialog(updateInfo.path, updateInfo.updateLog);
					break;
				case UpdateStatus.No: // has no update
					if (isAdded()) {
						Toast.makeText(
								mContext,
								getResources().getString(
										R.string.UMCheck_noupdate),
								Toast.LENGTH_SHORT).show();
					}
					break;
				case UpdateStatus.Timeout: // time out
					if (isAdded()) {
						Toast.makeText(
								mContext,
								getResources().getString(
										R.string.UMCheck_timeout),
								Toast.LENGTH_SHORT).show();
						break;
					}
				}
				isChecking = false;
			}
		});
		UmengUpdateAgent.forceUpdate(getActivity());
	}

	// private void showUpdateDialog(final String downloadUrl, final String
	// message) {
	// MailDialog.Builder builder = new MailDialog.Builder(getActivity());
	// builder.setTitle(R.string.UMUpdateTitle);
	// builder.setMessage(getString(
	// R.string.UMUpdateContent,message));
	// builder.setPositiveButton(getString(R.string.UMUpdateNow),
	// new DialogInterface.OnClickListener() {
	//
	// public void onClick(DialogInterface dialog, int id) {
	// try {
	// startActivity(new Intent(Intent.ACTION_VIEW, Uri
	// .parse(downloadUrl)));
	// } catch (Exception ex) {
	//
	// }
	// dialog.dismiss();
	// }
	// });
	// builder.setNeutralButton(getString(R.string.UMNotNow),
	// new DialogInterface.OnClickListener() {
	//
	// public void onClick(DialogInterface dialog, int id) {
	// dialog.dismiss();
	// }
	// });
	// builder.create().show();
	// }
	private void gesturePasswordViewShow() {
		if (!SetPasswordActivity.ifHasGPassword()) {
			modifyGesturePassword.setVisibility(View.GONE);
		} else {
			modifyGesturePassword.setVisibility(View.VISIBLE);
		}
	}

	private void saveShowTipsTag() {
		MailChat.setShowAccountSettingPopo(false);
		Editor editor = Preferences.getPreferences(mContext).getPreferences()
				.edit();
		MailChat.save(editor);
		editor.commit();
	}

	// 提示音选择加入
	// Modified by shengli
	// BEGIN
	/**
	 * 选择系统提示音Dialog
	 **/
	private RingAdapter mRingAdapter;

	private void showSelectRingDialog() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View layout = inflater.inflate(R.layout.select_ring_list, null);
		final ListView ringListView = (ListView) layout
				.findViewById(R.id.ring_list);
		mRingAdapter = new RingAdapter(getActivity(),
				MailChat.getSelectNotifyRingtone());
		ringListView.setAdapter(mRingAdapter);
		ringListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		ringListView.setOnItemClickListener(mRingOnItemClickListener);
		ringListView.setSelection(MailChat.getSelectNotifyRingtone());
		MailDialog.Builder builder = new MailDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.setting_ring));
		builder.setContentView(layout);
		builder.setPositiveButton(getString(R.string.okay_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						SharedPreferences preferences = Preferences
								.getPreferences(mContext).getPreferences();
						int checked = ringListView.getCheckedItemPosition();
						if (checked == -1) {
							// 没点击过
							checked = MailChat.getSelectNotifyRingtone();
						}
						String name = mRingAdapter.ringList.get(checked);
						MailChat.setSelectNotifyRingtone(checked);
						MailChat.setSelectNotifyRingtoneName(name);
						setRingNameTv.setText(name);
						Editor editor = preferences.edit();
						MailChat.save(editor);
						editor.commit();
						dialog.dismiss();
						MobclickAgent.onEvent(getActivity(), "change_bell");
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

	/**
	 * 系统提示音Adapter
	 **/
	class RingAdapter extends BaseAdapter {

		public List<String> ringList;
		Context mContext;
		public Cursor cursor;
		public RingtoneManager rm;
		public Map<Integer, Boolean> map = new HashMap<Integer, Boolean>();
		public RingViewHolder mHodler;
		public ListView ringView;
		public int index;
		public boolean firstItemState = true;

		/**
		 * 构造方法，index参数作为记录所选铃声的position传入SharedPreferences记录并调取。
		 */
		public RingAdapter(Context context, int index) {
			this.mContext = context;
			this.index = index;
			if (firstItemState) {
				firstItemState = false;
				map.put(index, true);
			}
			getRing();
		}

		public void getRing() {
			ringList = new ArrayList<String>();
			ringList.add(mContext.getString(R.string.setting_ring_auto));
			rm = new RingtoneManager(mContext);
			rm.setType(RingtoneManager.TYPE_NOTIFICATION);
			cursor = rm.getCursor();
			// 游标移动到第一位，如果有下一项，则添加到ringlist中
			if (cursor.moveToFirst()) {
				do {
					ringList.add(cursor
							.getString(RingtoneManager.TITLE_COLUMN_INDEX));
				} while (cursor.moveToNext());
			}
		}

		@Override
		public int getCount() {
			return ringList.size();
		}

		@Override
		public Object getItem(int position) {
			return ringList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.select_ring_item, null);
				mHodler = new RingViewHolder(convertView);
				convertView.setTag(mHodler);
			} else {
				mHodler = (RingViewHolder) convertView.getTag();
			}
			mHodler.iv
					.setBackgroundResource(map.get(position) == null ? R.drawable.icon_pressed
							: R.drawable.icon_checked);
			mHodler.tv.setText(ringList.get(position));
			return convertView;
		}

		public class RingViewHolder {
			TextView tv;
			ImageView iv;

			public RingViewHolder(View v) {
				this.tv = (TextView) v.findViewById(R.id.select_ring_tv);
				this.iv = (ImageView) v.findViewById(R.id.select_ring_btn);
			}
		}
	}

	/*
	 * ringlistView的按钮点击事件
	 */
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private OnItemClickListener mRingOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			RingViewHolder mHolder = mRingAdapter.new RingViewHolder(parent);
			mHolder.iv.setClickable(false);
			mRingAdapter.map.clear();
			mRingAdapter.map.put(position, true);
			mRingAdapter.notifyDataSetChanged();
			if (position != 0) {
				// Ringtone ringtone=null;
				try {
					RingtoneManager rm = new RingtoneManager(getActivity());
					rm.setType(RingtoneManager.TYPE_NOTIFICATION);
					rm.getCursor();
					// 点击多次，会出现无法释放mediaPlayer问题。
					// ringtone = rm.getRingtone(position - 1);
					// ringtone.play();
					startPlay(rm.getRingtoneUri(position - 1).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (position == 0) {
				Uri uri = RingtoneManager.getActualDefaultRingtoneUri(
						getActivity(), RingtoneManager.TYPE_NOTIFICATION);
				// Ringtone ringtone= RingtoneManager.getRingtone(getActivity(),
				// uri);
				// ringtone.play();
				startPlay(uri.toString());
			}

		}

	};

	private void startPlay(String path) {
		try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mediaPlayer.start();
	}

	public void stopPlay() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}
	}

	// END

	private void showJumpOaDialog() {
		final EditText addressEditText = new EditText(getActivity());
		MailDialog.Builder builder = new MailDialog.Builder(getActivity());
		builder.setTitle("OA测试服务器地址");
		builder.setContentView(addressEditText);
		builder.setPositiveButton(getString(R.string.okay_action),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						String address = addressEditText.getText().toString();
						if (address != null && address.length() > 0) {
							WebViewWithErrorViewActivity
									.forwardOpenUrlActivity(getActivity(),
											address, null, null,-1, true);
						} else {
							MailChat.toast("地址为空！");
						}
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

	private void dialogChooseServerHost() {
		// 创建我们的单选对话框
		Dialog dialog = new AlertDialog.Builder(getActivity())
				.setTitle("切换服务器地址")
				.setPositiveButton(R.string.okay_action,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								Log.d("qxian", ">>>http>" + mHttpUrl
										+ "/r/n mqtt>" + mMqttHost + ":"
										+ mMqttPort);
								chooseHost(mHttpUrl, mMqttHost, mMqttPort);
							}
						})
				.setNegativeButton(R.string.cancel_action,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						})
				.setSingleChoiceItems(R.array.choose_server_host,
						lastChoosed(), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								switch (which) {
								case 0:
									mHttpUrl = GlobalConstants.BASE_URL;
									mMqttHost = GlobalConstants.MQTT_HOST;
									mMqttPort = GlobalConstants.MQTT_PORT;
									break;
								case 1:
									mHttpUrl = GlobalConstants.BASE_URL_TEST;
									mMqttHost = GlobalConstants.MQTT_HOST_TEST;
									mMqttPort = GlobalConstants.MQTT_PORT_TEST;
									break;
								case 2:
									mHttpUrl = GlobalConstants.BASE_URL_LAN_TEST;
									mMqttHost = GlobalConstants.MQTT_HOST_LAN_TEST;
									mMqttPort = GlobalConstants.MQTT_PORT_LAN_TEST;
									break;
								}
							}
						}).create();
		dialog.show();
	}

	private void chooseHost(String httpUrl, String mqttHost, int mqttPort) {
		Protocol.setBASE_URL(httpUrl);
		Log.i(TAG, Protocol.getBASE_URL());
		// MQTT切换
		mController.disMQTTConnect();

		Connection.setHost(mqttHost);
		Connection.setPort(mqttPort);
		Connection.setClientHandle(Connection.getUri()
				+ Connection.getClientId());
		Connection.setClient(mqttHost, mqttPort);
		mController.MQTTConnect(false);
		// 切換完服務器后同步消息列表
		mController.syncGroups(Preferences.getPreferences(mContext)
				.getDefaultAccount(), null);
	}

	private int lastChoosed() {
		if (mMqttHost.equals(GlobalConstants.MQTT_HOST)) {
			return 0;
		} else if (mMqttHost.equals(GlobalConstants.MQTT_HOST_TEST)) {
			return 1;
		} else {
			return 2;
		}
	}
}
