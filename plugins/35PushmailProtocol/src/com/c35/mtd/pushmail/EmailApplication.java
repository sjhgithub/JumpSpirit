package com.c35.mtd.pushmail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import android.app.Application;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.beans.C35Message;
import com.c35.mtd.pushmail.logic.AccountUtil;
import com.c35.mtd.pushmail.logic.C35AccountManager;
import com.c35.mtd.pushmail.util.StoreDirectory;
import com.c35.mtd.pushmail.util.Utility;
//import com.c35.ptc.as.global.AsApplication;

/**
 * @Description:全局应用对象。Base class for those who need to maintain global application state.
 * @author:
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class EmailApplication extends Application {

    private static final String TAG = "EmailApplication";

	private static EmailApplication mInstance;
	private static Account currentAccount;//全局的默认账户对象
	/**
	 * 得到当前的活动账户
	 * @Description:
	 * @return
	 * @see: 
	 * @since: 
	 * @author: cuiwei
	 * @date:2013-11-21
	 */
	public static Account getCurrentAccount() {
		if(currentAccount!=null){
			return currentAccount;
		}else{
			currentAccount=C35AccountManager.getInstance().getDefaultAccount();
			return currentAccount;
		}
	}

	/**
	 * 账户变更时更改当前账户
	 * @Description:
	 * @param currentAccount
	 * @see: 
	 * @since: 
	 * @author: cuiwei
	 * @date:2013-11-21
	 */
	public static void setCurrentAccount(Account currentAccount) {
		EmailApplication.currentAccount = currentAccount;
	}

	private TeleListener listener;// 电话监听
	private static int telephoneState;// 电话状态 1 响铃时 2 接听时 0 空闲

	public static EmailApplication getInstance() {
		return mInstance;
	}

	public static final String LOG_TAG = "Email";
	public static final int READ_MAIL_UID_SHOW_SIZE = 20;// 已读未读显示数量
	public static final int READ_MAIL_UID_SAVE_SIZE = 20;
	public static Hashtable<String, ArrayList<String>> pushBoxMap = new Hashtable<String, ArrayList<String>>();
	public static Hashtable<String, Boolean> pushBoxReadFlagMap = new Hashtable<String, Boolean>();// true
																									// 标示为已查看
																									// (不显示铃铛)
																									// false
																									// 标示为未查看(铃铛显示)

	private static boolean ischeckSDcard = true;
//	private static final int MIN_HEAP_SIZE = 6 * 1024 * 1024;
	// public static File tempDirectory;
	/**
	 * If this is enabled there will be additional logging information sent to Log.d, including protocol
	 * dumps.
	 */
	public static boolean DEBUG = true;

	/**
	 * The MIME type(s) of attachments we're willing to send. At the moment it is not possible to open a
	 * chooser with a list of filter types, so the chooser is only opened with the first item in the list. The
	 * entire list will be used to filter down attachments that are added with Intent.ACTION_SEND. TODO: It
	 * should be legal to send anything requested by another app. This would provide parity with Gmail's
	 * behavior.
	 */
	public static final String[] ACCEPTABLE_ATTACHMENT_SEND_TYPES = new String[] { "image/*", "video/*", "35mail/*", "*/*" };

	/**
	 * The MIME type(s) of attachments we're willing to download to SD.
	 */
	public static final String[] ACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES = new String[] { "image/*", };
	/**
	 * The MIME type(s) of attachments we're not willing to download to SD.
	 */
	public static final String[] UNACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES = new String[] {};
	/**
	 * The special name "INBOX" is used throughout the application to mean "Whatever folder the server refers
	 * to as the user's Inbox. Placed here to ease use.
	 */

	// MailBox
	public static final String MAILBOX_INBOX = "inbox";// 收件箱
	public static final String MAILBOX_OUTBOX = ".outbox";// 发件箱，服务器没有
	public static final String MAILBOX_SENTBOX = ".Sent";// 已发送
	public static final String MAILBOX_DRAFTSBOX = ".Draft";// 草稿箱
	public static final String MAILBOX_TRASHBOX = ".Trash";// 已删除
	public static final String MAILBOX_FAVORITEBOX = "multiFolders";// 收藏夹，服务器没有
	public static final String MAILBOX_ATTACHMENTBOX = ".Attachments";// 附件夹

	public static final int MAILBOX_TYPE_DEFAULT = 0;
	public static final int MAIBOX_TYPE_ORDERVALUE_INBOX = 2147483647;// orderalue供显示的时候用
	public static final int MAIBOX_TYPE_ORDERVALUE_FAVORITEBOX = 2147483646;// orderalue供显示的时候用
	public static final int MAIBOX_TYPE_ORDERVALUE_OUTBOX = 2147483645;// orderalue供显示的时候用
	public static final int MAIBOX_TYPE_ORDERVALUE_SENTBOX = 2147483644;// orderalue供显示的时候用
	public static final int MAIBOX_TYPE_ORDERVALUE_DRAFTSBOX = 2147483643;// orderalue供显示的时候用
	public static final int MAIBOX_TYPE_ORDERVALUE_TRASHBOX = 2147483642;// orderalue供显示的时候用
	public static final int MAIBOX_TYPE_ORDERVALUE_ATTACHMENTBOX = 2147483641;// orderalue供显示的时候用
	public static final int MAILBOX_TYPE_SELF = 1;
	// 公共的日期时间格式化对象，都要使用以下现成的方法
	public static final SimpleDateFormat DateFormatYMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat DateFormatMDHM = new SimpleDateFormat("MM-dd HH:mm");
	public static final SimpleDateFormat DateFormatYMD = new SimpleDateFormat("yyyyMMdd");
	public static final SimpleDateFormat DateFormatYYMMDDHHMMSS = new SimpleDateFormat("yyyyMMddHHmmss");
	public static final SimpleDateFormat DateFormatEEE = new SimpleDateFormat("EEEE");
	public static final SimpleDateFormat DateFormatHM = new SimpleDateFormat("HH:mm");
	public static final SimpleDateFormat DataFormatCHINESYYMMDD = new SimpleDateFormat("yyyy年MM月dd日");
	public static final SimpleDateFormat DataFormatENYYMMDD = new SimpleDateFormat("yyyy-MM-dd");

	public static WindowManager.LayoutParams wmParams;
	private static WindowManager mWindowManager;
	public static HashMap<String, View> messageMap = new HashMap<String, View>();// 维护个账号的弹出窗口，key 为用户名 Value
																					// 是 View

	/**
	 * Specifies how many messages will be shown in a folder by default. This number is set on each new folder
	 * and can be incremented with "Load more messages..." by the VISIBLE_LIMIT_INCREMENT
	 */
	public static final int DEFAULT_VISIBLE_LIMIT = 10;

	/**
	 * The maximum size of an attachment we're willing to upload (measured as stored on disk). Attachments
	 * that are base64 encoded (most) will be about 1.375x their actual size so we should probably factor that
	 * in. A 5MB attachment will generally be around 6.8MB uploaded.
	 */
	public static final int MAX_ATTACHMENT_UPLOAD_SIZE = (10 * 1024 * 1024);
	private static View myFV;
	private final static int POSITION_Y = -8000;

	public static float density;// 密度
	public final static int HAN_MSG_SEND_SUCCESS=10;
	public static MailHandler mHandler;
	
	/**
	 * 得到本地文件夹的显示名称
	 * @param folderName
	 * @param unReadNum
	 * @param totalNum
	 * @return
	 * @see: 
	 * @since: 
	 * @author: cuiwei
	 * @date:2013-11-25
	 */
	public String getI18nMailboxName(String folderName, int unReadNum, int totalNum) {
//		Debug.i(TAG, "folderName___:" + folderName);
		if(folderName==null){
			return getString(R.string.special_mailbox_name_inbox);
		}
		String i18nFolderName = "";
		if (folderName.equals(EmailApplication.MAILBOX_INBOX)) {//.Draft
			if(unReadNum<0||totalNum<0){
				i18nFolderName = getString(R.string.special_mailbox_name_inbox);
			}else{
				i18nFolderName = getString(R.string.special_mailbox_name_inbox) + "(" + unReadNum + "/" + totalNum + ")";
			}
		} else if (folderName.equals(EmailApplication.MAILBOX_TRASHBOX)) {
			i18nFolderName = getString(R.string.special_mailbox_name_trash);
		} else if (folderName.equals(EmailApplication.MAILBOX_DRAFTSBOX)) {
			if(unReadNum <0 || totalNum < 0 ){
				i18nFolderName = getString(R.string.special_mailbox_name_drafts);
			}else{
				i18nFolderName = getString(R.string.special_mailbox_name_drafts) + "(" + totalNum + ")";
			}
		} else if (folderName.equals(EmailApplication.MAILBOX_OUTBOX)) {
			i18nFolderName = getString(R.string.special_mailbox_name_outbox);
		} else if (folderName.equals(EmailApplication.MAILBOX_SENTBOX)) {
			i18nFolderName = getString(R.string.special_mailbox_name_sent);
		} else if (folderName.equals(EmailApplication.MAILBOX_FAVORITEBOX)) {
			i18nFolderName = getString(R.string.special_mailbox_name_favorite);
		} else if (folderName.equals(EmailApplication.MAILBOX_ATTACHMENTBOX)) {
			i18nFolderName = getString(R.string.special_mailbox_name_attachment);
		} else {
			if (unReadNum <0 || totalNum < 0 ){
				i18nFolderName = folderName;
			} else {
				i18nFolderName = folderName;
//				i18nFolderName = folderName + "(" + unReadNum + ")";
			}
		}
		return i18nFolderName;
	}
	/**
	 * 全局的吐司等显示处理Handler
	 */
	public class MailHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case HAN_MSG_SEND_SUCCESS:
				Log.i(TAG, mInstance.getString(R.string.send_mail_success));
				break;
			default:
				break;
			}
		}
			
	}
	/**
	 * Called throughout the application when the number of accounts has changed. This method enables or
	 * disables the Compose activity, the boot receiver and the service based on whether any accounts are
	 * configured.
	 */
	public static void setServicesEnabled(Context context) {
		setServicesEnabled(C35AccountManager.getInstance().getAccountsSize() > 0);
	}
	/**
	 * 如果没有账户，其相应的图标不会显示在可启动程序列表里
	 * @param enabled 有账户时为true
	 * @see: 
	 * @since: 
	 * @author: cuiwei
	 * @date:2013-12-5
	 */
	private static void setServicesEnabled(boolean enabled) {
	    // Modified by LL
        // BEGIN
	    /*
		PackageManager pm = mInstance.getPackageManager();
		if (!enabled && pm.getComponentEnabledSetting(new ComponentName(mInstance, PushMailService.class)) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
			// getComponentEnabledSetting会有4个状态，0默认 1可用 2禁止 3user disable
			// If no accounts now exist but the service is still enabled we're about to disable it so we'll
			// reschedule to kill off any existing alarms.
			PushMailService.actionReschedule();
		}
		//PackageManager.setComponentEnabledSetting函数可以把某个ComponentName的state设为false或true，
		//Android中对于state为false的Activity，是不能onCreate，如果该Activity对应与某个APK的Main Activity，
		//则该APK在state为false时，其相应的图标不会显示在可启动程序列表里，
		//即不能通过Main Launcher Intent filter query出来。
		int iNewState=enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		pm.setComponentEnabledSetting(new ComponentName(mInstance, MessageCompose.class), iNewState, PackageManager.DONT_KILL_APP);//行为标签，值可以是DONT_KILL_APP或者0。 0说明杀死包含该组件的app
		pm.setComponentEnabledSetting(new ComponentName(mInstance, BootReceiver.class), iNewState, PackageManager.DONT_KILL_APP);
		pm.setComponentEnabledSetting(new ComponentName(mInstance, PushMailService.class), iNewState, PackageManager.DONT_KILL_APP);
		if (enabled && pm.getComponentEnabledSetting(new ComponentName(mInstance, PushMailService.class)) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
			// And now if accounts do exist then we've just enabled the service and we want to schedule alarms
			// for the new accounts.
			PushMailService.actionReschedule();
		}
		*/
		// END
	}

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			mInstance = this;
			C35AccountManager.getInstance().init(this);
			
			// Modified by LL
			// 关闭推送功能
//			AsApplication.init(this);
//			AsApplication.strClientVersion = this.getPackageName()+"___"+getString(R.string.version_name).trim();//this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
			
			// 错误日志处理
			CrashHandler.getInstance().init();
			listener = new TeleListener();
			TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			manager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

			// Modified by LL
	        // BEGIN
			/*
			ischeckSDcard = StoreDirectory.checkSdCard();
			// sdCard 检测通过后才能继续运行
			if (!ischeckSDcard) {
				// 更新widget
				PushMailWidget.forceUpdate();
			}
			if (ischeckSDcard) {
				// 注册网络状态RECEIVER
				setServicesEnabled(getApplicationContext());
			}
			*/
			// END

			// 获取WindowManager
			mWindowManager = (WindowManager) EmailApplication.getInstance().getSystemService("window");
			// 设置LayoutParams(全局变量）相关参数
			// wmParams = ((MyApplication)getApplication()).getMywmParams();
			wmParams = new WindowManager.LayoutParams();
	
			DisplayMetrics dm = getResources().getDisplayMetrics();
			density = dm.density;
			init_push_data();
			mHandler=new MailHandler();
		} catch (Exception e) {//NameNotFoundException
//			Debug.e("FFA", "APP_onCreate" + e.getMessage(), e);
		}
	}

	/**
	 * 初始化push已读未读数据
	 * @see:
	 * @since:
	 * @author: gongfacun
	 * @date:2013-7-3
	 */
	private void init_push_data() {
		try {
			C35AccountManager manager = C35AccountManager.getInstance();
			List<Account> accounts = manager.getAccountsFromSP();
			for (Account tempAcc : accounts) {
				// TODO:sofia2.0环境不支持此接口。
				Boolean result = AccountUtil.isSupportRequest("ReadPush", tempAcc);
				if (result == null || result == false) {
//					AccountUtil.nosupportRequestToast();
					break;
				}
				if (tempAcc.getPush_uids() != null) {
					ArrayList<String> uidsList = new ArrayList<String>();
					String[] uids = tempAcc.getPush_uids().split(",");
					for (int i = 0; i < uids.length; i++) {
						uidsList.add(uids[i]);
					}
					if (pushBoxMap == null || tempAcc == null || tempAcc.getEmail() == null || uidsList == null) {
						return;// by cuiwei 李总手机报空指针错误/todo
					}
					pushBoxMap.put(tempAcc.getEmail(), uidsList);
					if (pushBoxReadFlagMap == null) {
						return;// by cuiwei 李总手机报空指针错误/todo
					}
					pushBoxReadFlagMap.put(tempAcc.getEmail(), tempAcc.isRead_flags());
					// Modified by LL
			        // BEGIN
					/*
					if (MessageList.mHandler != null && tempAcc.getEmail().equals(EmailApplication.getCurrentAccount().getEmail())) {
						MessageList.mHandler.sendEmptyMessage(ListHandler.SHOW_PUSH_READ_MSG);
					}
					if (FolderList.mHandler != null && tempAcc.getEmail().equals(EmailApplication.getCurrentAccount().getEmail())) {
						FolderList.mHandler.sendEmptyMessage(MailboxHandler.SHOW_PUSH_READ_MSG);
					}
					*/
					// END
				}
			}
		} catch (Exception e) {
			Debug.e("FFA", "init_push_data" + e.getMessage(), e);
		}
	}

	public static void update_push_uids(Account account, List<String> list) {
		StringBuffer realMailUidsStr = new StringBuffer();
		for (String realmailuid : list) {
			realMailUidsStr.append(realmailuid);
			realMailUidsStr.append(",");
		}
		account.save_read_uids(C35AccountManager.getInstance(), account.getUuid(), realMailUidsStr.toString());
	}

	public static void update_push_readflag(Account account, boolean read_flag) {
		account.save_read_flag(C35AccountManager.getInstance(), account.getUuid(), read_flag);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Account account = EmailApplication.getCurrentAccount();
		if (account != null) {
			Utility.setLanguage( account.getLanguage());
		}
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * unregister网络状态RECEIVER
	 * 
	 * @author xuefj
	 */
	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	/**
	 * 透明窗的入口
	 * 
	 * @Description:
	 * @param messages
	 * @param account
	 * @see:
	 * @since:
	 * @author: zhangran
	 * @date:2013-4-15
	 */
	public static void showPushWindow(List<C35Message> messages, Account account) {
	    // Modified by LL
        // BEGIN
	    /*
		if (messageMap.containsKey(account.getUuid())) {// 多行
			mWindowManager.removeView((View) messageMap.get(account.getUuid()));
			messageMap.remove(account.getUuid());
			myFV = new DeskTopNotifyView(EmailApplication.getInstance(), messages, account).initViewsMulti();
		} else if (messages.size() > 1) {
			myFV = new DeskTopNotifyView(EmailApplication.getInstance(), messages, account).initViewsMulti();
		} else {// 单行
			myFV = new DeskTopNotifyView(EmailApplication.getInstance(), messages, account).initViews();
		}

		// myFV = new DeskTopNotifyActivity(Email.getInstance(), messages, account).initViewsMulti();
		// myFV = new DeskTopNotifyActivity(Email.getInstance(), messages, account).initViews();

		// 以下都是WindowManager.LayoutParams的相关属性 具体用途可参考SDK文档

		// // 根据电话状态调整透明窗位置
		wmParams.type = LayoutParams.TYPE_PHONE;

		wmParams.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明

		// 设置Window flag
		wmParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		// 下面的flags属性的效果形同“锁定”。 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。 wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL
		// | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;

		wmParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL; // 调整悬浮窗口至左上角
		// wmParams.gravity=Gravity.LEFT|Gravity.TOP; //调整悬浮窗口至左上角

		// 以屏幕左上角为原点，设置x、y初始值
		wmParams.x = 0;
		wmParams.y = (messageMap.size()) * 30;

		// wmParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;//强制竖屏

		// 设置悬浮窗口长宽数据
		// wmParams.width=140;

		wmParams.height = 540;
		if (density <= 1.5) {// 4350
			wmParams.height = 540;
		} else if (density <= 2.0) {// v6
			wmParams.height = 720;
		} else {
			wmParams.height = 1080;
		}

		//
		messageMap.put(account.getUuid(), myFV);

		// 使其可移动 zhangran
		myFV.setOnTouchListener(new OnTouchListener() {

			int lastX, lastY;
			int paramX, paramY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					lastX = (int) event.getRawX();
					lastY = (int) event.getRawY();
					paramX = wmParams.x;
					paramY = wmParams.y;
					break;
				case MotionEvent.ACTION_MOVE:
					int dx = (int) event.getRawX() - lastX;
					int dy = (int) event.getRawY() - lastY;
					wmParams.x = paramX + dx;
					wmParams.y = paramY + dy;
					// 更新悬浮窗位置
					mWindowManager.updateViewLayout(v, wmParams);
					Debug.d("zhangran_mail", "onTouch() wmParams.x=" + wmParams.x + " wmParams.y=" + wmParams.y);
					break;
				}
				return false;
			}
		});

		mWindowManager.addView(myFV, wmParams);

		// 根据电话状态调整透明窗位置
		if (telephoneState == 0) {// 空闲时
			wmParams.y = 0;
			mWindowManager.updateViewLayout(myFV, wmParams);
		} else {
			wmParams.y = POSITION_Y;
			mWindowManager.updateViewLayout(myFV, wmParams);
			Debug.d("zhangran_mail", "忙时 wmParams.x=" + wmParams.x + " wmParams.y=" + wmParams.y);
		}
		*/
		// END
	}

	/**
	 * 关闭透明窗
	 * 
	 * @Description:
	 * @param userId
	 * @see:
	 * @since:
	 * @author: zhangran
	 * @date:2013-4-15
	 */
	public static void removeWindowByUserId(String userId) {
		if (messageMap.containsKey(userId)) {
			mWindowManager.removeView((View) messageMap.get(userId));
			messageMap.remove(userId);
		}
	}

	/**
	 * 关闭所有透明窗
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-4-17
	 */
	public static void removeAllWindowByUserId() {
		for (Account account : C35AccountManager.getInstance().getAccountsFromSP()) {

			removeWindowByUserId(account.getUuid());
		}
	}

	/**
	 * 监听打电话状态，处理push弹窗位置
	 * 
	 * @Description:
	 * @author: cuiwei
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2013-4-17
	 */
	private class TeleListener extends PhoneStateListener {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// TODO Auto-generated method stub
			super.onCallStateChanged(state, incomingNumber);
			try {
				switch (state) {
				case TelephonyManager.CALL_STATE_RINGING:// 响铃时
					Debug.d("zhangran_mail", "CALL_STATE_RINGING ****************** 响铃 **********************");
					telephoneState = 1;
					if (myFV != null && wmParams != null) {
						wmParams.y = POSITION_Y;
						updateViewLayout(wmParams);
					}
					Debug.d("zhangran_mail", "响铃时 wmParams.x=" + wmParams.x + " wmParams.y=" + wmParams.y);
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:// 接听时
					Debug.d("zhangran_mail", "CALL_STATE_OFFHOOK ****************** 摘机 **********************");
					telephoneState = 2;
					if (myFV != null) {
						wmParams.y = POSITION_Y;
						updateViewLayout(wmParams);
					}
					Debug.d("zhangran_mail", "接听时 wmParams.x=" + wmParams.x + " wmParams.y=" + wmParams.y);
					break;
				case TelephonyManager.CALL_STATE_IDLE:// 挂机时
					Debug.d("zhangran_mail", "CALL_STATE_IDLE ******************** 挂机 *************");
					telephoneState = 0;
					Debug.d("zhangran_mail", "myFV = " + myFV);
					if (myFV != null) {
						wmParams.y = 0;
						if (density <= 1.5) {// 4350
							wmParams.height = 540;
						} else if (density <= 2.0) {// v6
							wmParams.height = 720;
						} else {
							wmParams.height = 1080;
						}
						updateViewLayout(wmParams);
					}
					break;

				default:
					break;
				}
			} catch (Exception e) {
				Debug.e("FFA", "onCallStateChanged" + e.getMessage(), e);
			}
		}

		/**
		 * 
		 * @Description:更新所有弹窗的位置
		 * @param wmP
		 * @see:
		 * @since:
		 * @author: gongfc
		 * @date:Jul 31, 2013
		 */
		private void updateViewLayout(LayoutParams wmP) {
			Collection<View> views = messageMap.values();// 得到所有的view
			Iterator<View> it = views.iterator(); // 得到迭代器
			while (it.hasNext()) {
				mWindowManager.updateViewLayout(it.next(), wmP); // 具体处理view位置
			}
		}

	}
}
