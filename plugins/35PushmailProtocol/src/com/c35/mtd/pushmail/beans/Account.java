package com.c35.mtd.pushmail.beans;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.R;
import com.c35.mtd.pushmail.logic.C35AccountManager;
import com.c35.mtd.pushmail.util.AesUtil;
import com.c35.mtd.pushmail.util.MailUtil;
import com.c35.mtd.pushmail.util.StoreDirectory;
import com.c35.mtd.pushmail.util.Utility;

/**
 * 
 * 
 * @Description:账户的javaBean，存储在sharedPerformance里。
 * 用于封装账户的各个属性，这些属性很多是跟账户设置挂钩
 * @author:liujie
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-4-26
 */
public class Account implements Serializable {

	private static final String TAG = "Account";
	public static final String DEF_RING_URI = "content://settings/system/notification_sound";
	private static final long serialVersionUID = 2975156672298625121L;
//	private static Context mContext;

	/************ liujie ***************************************************/
	private boolean pushOpen;// 表示服务器端的push业务是否开通
	private boolean localPushOpen;// 表示本地是否开启
	private boolean deskRemind = true;// 桌面提醒开关
	private int mailReview;// 邮件内容预览设置 0:无预览 1:1行预览 2:2行预览 3:3行预览
	private int mailFont;// 邮件字体大小设置
	private boolean tryaccount;// 是否是试用帐号
	// boolean ledOpen;// LED提醒设置 是否开启LED提醒
	// int ledColor;// LED颜色设置 0：红色1：绿色 2：蓝色
	// int ledDuration;// LED持续时长设置 5：持续5分钟，30：持续30分钟 -1：直到用户操作通知才关掉提醒。
	private boolean signatureOpen = false;// 是否使用签名
	private String mailSignature;// 签名内容
	private boolean freeTimeOpen;// 是否是闲时
	private String freeTimeStart;// 闲时开始时间 时间形式为：“9:41”
	private String freeTimeEnd;// 闲时结束时间
	private boolean saveCopy;// 是否删除服务器上的邮件 true不删 false 删
	// int mailSize;//邮件大小的阀值 -1无限制，0仅标题，其他数字为具体的kb值（如35即表示35KB）
	private boolean checkPassword;// 是否校验口令，true校验，false不校验
	private String password;// 登陆密码
	private String accessCode;// 软件密码
	private int recvMailLimit;// 收取邮件封数限制
	private int pushNotifyRange;// Push提醒范围设置
	private boolean messageOnLoad;// 邮件列表触底即加载更多邮件
	private boolean screenAutoChange;// 横竖屏自动切换
	private boolean autoSync;// 自动同步
	private int recvMailMode;// 邮件收取模式 0智能切换，1省流量模式，2完整模式
	private boolean shakeUnRead;// 设置里用的，是否可以摇动
	private String push_uids;// 已读邮件id列表
	private String checkPush_xmlResult;// 已读邮件id列表
	
	public String getCheckPush_xmlResult() {
		return checkPush_xmlResult;
	}

	
	public void setCheckPush_xmlResult(String checkPush_xmlResult) {
		this.checkPush_xmlResult = checkPush_xmlResult;
	}

	private boolean read_flags;// 收件人已读标志
	/**************** liujie ************************************************/
	private String mUuid;
	private int mAccountNumber;//=db中的id？
	private String mStoreUri;
	private String mLocalStoreUri;// local://localhost//mnt/sdcard/com.c35.mtd.pushmail/database/35PushMail.db
//	private String mDescription;
	private String mName;//qiang.yi
	private String mEmail;// qiang.yi@slmetal.cn,1234567_qq_1@try.35.cn
	private String mEmailShow;// 外部账号名称 1234567@qq.com
	private int mAutomaticCheckIntervalMinutes;//自动查收频率 5～30分，2小时
	private long mLastAutomaticCheckTime;
	private boolean mNotifyNewMail;
	// int mAccountNumber;
	private boolean mVibrate;
	private String mRingtoneUri; // 铃声地址
	private boolean autoPicture;// 图片质量自动处理是否开启
	private int pictureQuality;// 图片质量 高1 中2 低4
	private String mLanguage; // 当前语言

	private int mLSUriType; // 本地存储路径的类型：0：MOVINAND; 1: SDCARD; 2: MOVICARD; 3: NULL; add by xuefj

	private String mDomainType; // 登陆时判断用户邮件系统的环境。分1:代收; 2:sofia2; 3:sofia3
	private int mAliasType;//域别名类型： (必填)0:主域名； 1域别名//郭家龙 之前方案是初始化数据，可以指定这个值，后面方案是自动获取，此值基本就没有意义了

	private String hostIP; //

	private int mSendReadReceipts;
	private int mailPort;// Port int 普通命令端口（选填）

	private int updownloadport;// updownport int 上传下载端口（选填）
	/**
	 * <pre>
	 * 0 Never 
	 * 1 After 7 days 
	 * 2 When I delete from inbox
	 * </pre>
	 */
	public Account() {
//		mContext = context;
		mUuid = UUID.randomUUID().toString();
		mAccountNumber = -1;
		mLocalStoreUri = StoreDirectory.getStoreageUri();
		mLSUriType = StoreDirectory.getStoreageType();
		mAutomaticCheckIntervalMinutes = 5;
		autoPicture = true; // 默认自动处理图片
		pictureQuality = 2; // 设置图片默认是中
		mLanguage = "auto"; // 设置语言默认是自动，根据系统语言确定
		// mAccountNumber = -1;
		mNotifyNewMail = true;
		mVibrate = true;
		deskRemind = true;
		signatureOpen = true;
		mSendReadReceipts = 1;
	
		mailSignature = EmailApplication.getInstance().getResources().getString(R.string.account_settings_signature_editor_value_other);
		// }
		// mailSignature =
		// "发自微妹手机邮箱客户端（支持qq\\163\\Gmail等邮箱）<a href=\"http://www.baidu.com\">安卓客户端 </a>|<a href=\"http://www.baidu.com\">iPhone客户端</a>";
		Debug.d(TAG, "mailSignature = " + mailSignature);
		// voice = true;
		tryaccount = false;
		freeTimeOpen = false;
		saveCopy = true;
		checkPassword = false;
		updownloadport = MailUtil.PROXY_SERVER_ATTACHMENT_PORT;
		mailReview = 2;
//		int screenDensity = mContext.getResources().getDisplayMetrics().densityDpi;
		// mailFont = screenDensity > 320 ? 300 : (screenDensity > DisplayMetrics.DENSITY_HIGH ? 200 : 150);//
		// V6字太小
		mailFont = 150;
		// mailSize = 35;// 默认值是35K
		mRingtoneUri = DEF_RING_URI;
		freeTimeStart = "22:00";
		freeTimeEnd = "07:00";
		password = "";
		recvMailLimit = 20;
		pushNotifyRange = 0;
		messageOnLoad = true;// 默认邮件触底就加载
		screenAutoChange = false;// 默认不自动切换屏幕
		autoSync = true;// 默认为自动同步
		recvMailMode = 0;// 默认为智能切换模式
		shakeUnRead = true; // 摇一摇查看未读邮件
	}

	/**
	 * Account
	 * @param uuid
	 * @param email
	 * @param emailShow
	 */
	public Account(String uuid, String email, String emailShow) {
		this.mUuid = uuid;
		this.mEmail = email;
		this.mEmailShow = emailShow;
	}

	public Account(String uuid) {
		this.mUuid = uuid;
		refreshAccInfoFromSP();
	}


	/**
	 * 从SharedPreferences取得最新的账户状态
	 * @param manager
	 * @see:
	 * @since:
	 * @author: CuiWei
	 * @date:2012-11-12
	 */
	public void refreshAccInfoFromSP() {
		// Debug.v(TAG, "doRefresh()");
		// synchronized (manager) {
		try {
			SharedPreferences spfAccount = EmailApplication.getInstance().getSharedPreferences(C35AccountManager.PREFERENCES_FILE_NAME, EmailApplication.MODE_PRIVATE);

			mLSUriType = spfAccount.getInt(mUuid + ".lSUriType", 0);
			mSendReadReceipts = spfAccount.getInt(mUuid + ".sendReadReceipts", 1);
			mStoreUri = Utility.base64Decode(spfAccount.getString(mUuid + ".storeUri", null));
			mLocalStoreUri = spfAccount.getString(mUuid + ".newLocalStoreUri", StoreDirectory.getStoreageUri());
//			mDescription = spfAccount.getString(mUuid + ".description", null);
			mailSignature = spfAccount.getString(mUuid + ".mailSignature", mailSignature);
			// Debug.d(TAG,"signature = "+signature);
			mName = spfAccount.getString(mUuid + ".name", mName);
			// Debug.d(TAG,"mName = "+mName);
			mEmail = spfAccount.getString(mUuid + ".email", mEmail);
			mEmailShow = spfAccount.getString(mUuid + ".mEmailShow", mEmailShow);
			mAutomaticCheckIntervalMinutes = spfAccount.getInt(mUuid + ".automaticCheckIntervalMinutes", -1);
			mailReview = spfAccount.getInt(mUuid + ".mailReview", 0);
			mailFont = spfAccount.getInt(mUuid + ".mailFont", 150);
			recvMailMode = spfAccount.getInt(mUuid + ".recvMailMode", 0);
			recvMailLimit = spfAccount.getInt(mUuid + ".recvMailLimit", 20);
			// mailSize = getInt(strBody, mUuid + ".mailSize", 35);
			mLastAutomaticCheckTime = spfAccount.getLong(mUuid + ".lastAutomaticCheckTime", 0);
			mNotifyNewMail = spfAccount.getBoolean(mUuid + ".notifyNewMail", false);
			pushOpen = spfAccount.getBoolean(mUuid + ".pushOpen", pushOpen);// false
			localPushOpen = spfAccount.getBoolean(mUuid + ".localPushOpen", localPushOpen);// false
			messageOnLoad = spfAccount.getBoolean(mUuid + ".messageOnLoad", true);
			screenAutoChange = spfAccount.getBoolean(mUuid + ".screenAutoChange", false);
			shakeUnRead = spfAccount.getBoolean(mUuid + ".shakeUnRead", true);
			push_uids = spfAccount.getString(mUuid + ".push_uids", push_uids);
			checkPush_xmlResult = spfAccount.getString(mUuid + ".checkPush_xmlResult", checkPush_xmlResult);
			read_flags = spfAccount.getBoolean(mUuid + ".read_flags", true);
			autoSync = spfAccount.getBoolean(mUuid + ".autoSync", true);
			freeTimeOpen = spfAccount.getBoolean(mUuid + ".freeTimeOpen", false);
			saveCopy = spfAccount.getBoolean(mUuid + ".saveCopy", false);
			checkPassword = spfAccount.getBoolean(mUuid + ".checkPassword", false);
			freeTimeStart = spfAccount.getString(mUuid + ".freeTimeStart", freeTimeStart);
			freeTimeEnd = spfAccount.getString(mUuid + ".freeTimeEnd", freeTimeEnd);
			hostIP = spfAccount.getString(mUuid + ".hostIP", MailUtil.STORE_SCHEME_C35PROXY+"??");// cuiwei
			mailPort = spfAccount.getInt(mUuid + ".mailPort", MailUtil.PROXY_SERVER_MAIL_PORT);// cuiwei
			updownloadport = spfAccount.getInt(mUuid + ".updownloadport", MailUtil.PROXY_SERVER_ATTACHMENT_PORT);// cuiwei
			password = AesUtil.decrypt(spfAccount.getString(mUuid + ".password", password));
//			accessCode = spfAccount.getString(mUuid + ".accessCode", accessCode);
			mDomainType = spfAccount.getString(mUuid + ".domainEnv", mDomainType);
			// delete policy was incorrectly set on earlier versions, so we'll upgrade it here.
			// rule: if IMAP account and policy = 0 ("never"), change policy to 2 ("on delete")
			mAliasType = spfAccount.getInt(mUuid + ".aliasType", 0);
//			if (mAliasType == DELETE_POLICY_NEVER && mStoreUri != null && mStoreUri.toString().startsWith(Store.STORE_SCHEME_IMAP)) {
//				mAliasType = DELETE_POLICY_ON_DELETE;
//			}
			// mAccountNumber = getInt(strBody, mUuid + ".accountNumber", 0);
			mVibrate = spfAccount.getBoolean(mUuid + ".vibrate", false);
			deskRemind = spfAccount.getBoolean(mUuid + ".deskRemind", false);
			signatureOpen = spfAccount.getBoolean(mUuid + ".signatureOpen", false);
			tryaccount = spfAccount.getBoolean(mUuid + ".tryaccount", false);
			// voice = getBoolean(strBody, mUuid+".voice", false);
			mRingtoneUri = spfAccount.getString(mUuid + ".ringtone", DEF_RING_URI);
			mLSUriType = spfAccount.getInt(mUuid + ".lSUriType", 0);
			mAccountNumber = spfAccount.getInt(mUuid + ".accountNumber", 0);
			pushNotifyRange = spfAccount.getInt(mUuid + ".pushNotifyRange", 0);
			autoPicture = spfAccount.getBoolean(mUuid + ".autoPicture", true);
			pictureQuality = spfAccount.getInt(mUuid + ".pictureQuality", 2);
			mLanguage = spfAccount.getString(mUuid + ".language", "auto");

		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		// }
	}
	/**
	 * 从SharedPreferences删除一个账号
	 * @Description:
	 * @param manager
	 * @see: 
	 * @since: 
	 * @author: cuiwei
	 * @date:2013-10-14
	 */
	public void delete(C35AccountManager manager) {
		Debug.v(TAG, "clear account now");
		synchronized (manager.mPreferences) {
			String[] ids = manager.mPreferences.getString(C35AccountManager.ACCOUNTS_UUIDS_KEY, "").split(C35AccountManager.ACCOUNTS_IDS_SEPARATOR);
			StringBuffer sb = new StringBuffer();
			SharedPreferences.Editor editor = manager.mPreferences.edit();
			if (ids.length == 1) {
				editor.remove(C35AccountManager.ACCOUNTS_UUIDS_KEY);
				editor.remove(C35AccountManager.DEFAULT_ACCOUNT_UUID_KEY);
			} else {
				for (int i = 0; i < ids.length; i++) {
					if (!ids[i].equals(String.valueOf(mUuid))) {
						if (sb.length() > 0) {
							sb.append(',');
						}
						sb.append(ids[i]);
					}
				}
				String accountsIds = sb.toString();
				editor.putString(C35AccountManager.ACCOUNTS_UUIDS_KEY, accountsIds);
			}
			editor.remove(mUuid + ".storeUri");
			editor.remove(mUuid + ".newLocalStoreUri");
			editor.remove(mUuid + ".senderUri");
			editor.remove(mUuid + ".description");
			editor.remove(mUuid + ".name");
			editor.remove(mUuid + ".email");
			editor.remove(mUuid + ".mEmailShow");
			editor.remove(mUuid + ".automaticCheckIntervalMinutes");
			editor.remove(mUuid + ".mailReview");
			editor.remove(mUuid + ".mailFont");
			editor.remove(mUuid + ".recvMailMode");
			editor.remove(mUuid + ".recvMailLimit");
			editor.remove(mUuid + ".mailSize");
			editor.remove(mUuid + ".lastAutomaticCheckTime");
			editor.remove(mUuid + ".notifyNewMail");
			editor.remove(mUuid + ".aliasType");
			editor.remove(mUuid + ".pushOpen");
			editor.remove(mUuid + ".localPushOpen");
			editor.remove(mUuid + ".messageOnLoad");
			editor.remove(mUuid + ".screenAutoChange");
			editor.remove(mUuid + ".shakeUnRead");
			editor.remove(mUuid + ".read_flags");
			editor.remove(mUuid + ".push_uids");
			editor.remove(mUuid + ".checkPush_xmlResult");
			editor.remove(mUuid + ".autoSync");
			editor.remove(mUuid + ".draftsFolderName");
			editor.remove(mUuid + ".sentFolderName");
			editor.remove(mUuid + ".trashFolderName");
			editor.remove(mUuid + ".outboxFolderName");
			editor.remove(mUuid + ".accountNumber");
			editor.remove(mUuid + ".vibrate");
			editor.remove(mUuid + ".deskRemind");
			editor.remove(mUuid + ".signatureOpen");
			editor.remove(mUuid + ".mailSignature");
			editor.remove(mUuid + ".tryaccount");
			editor.remove(mUuid + ".voice");
			editor.remove(mUuid + ".ringtone");
			editor.remove(mUuid + ".freeTimeOpen");
			editor.remove(mUuid + ".saveCopy");
			editor.remove(mUuid + ".checkPassword");
			editor.remove(mUuid + ".freeTimeStart");
			editor.remove(mUuid + ".freeTimeEnd");
			editor.remove(mUuid + ".password");
			editor.remove(mUuid + ".hostIP");
			editor.remove(mUuid + ".mailPort");
			editor.remove(mUuid + ".updownloadport");
			editor.remove(mUuid + ".lSUriType");
			editor.remove(mUuid + ".accessCode");
			// also delete any deprecated fields
			editor.remove(mUuid + ".transportUri");
			editor.remove(mUuid + ".pushNotifyRange");
			editor.remove(mUuid + ".autoPicture");
			editor.remove(mUuid + ".pictureQuality");
			editor.remove(mUuid + ".language");
			editor.remove(mUuid + ".domainEnv");
			editor.remove(mUuid + ".sendReadReceipts");
			editor.commit();
		}
	}

	public String getPush_uids() {
		return push_uids;
	}

	public void setPush_uids(String push_uids) {
		this.push_uids = push_uids;
	}

	public boolean isRead_flags() {
		return read_flags;
	}

	public void setRead_flags(boolean read_flags) {
		this.read_flags = read_flags;
	}

	/**
	 * 
	 * @Description:保存uri和mail
	 * @param manager
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:Aug 14, 2013
	 */
	public void save_mail_uri(C35AccountManager manager, String uuid) {
		synchronized (manager.mPreferences) {
			SharedPreferences.Editor editor = manager.mPreferences.edit();
			editor.putString(mUuid + ".storeUri", Utility.base64Encode(mStoreUri));
			editor.putString(mUuid + ".email", mEmail);

			editor.putString(mUuid + ".hostIP", hostIP);
			editor.putInt(mUuid + ".updownloadport", updownloadport);
			editor.putInt(mUuid + ".mailPort", mailPort);
			editor.commit();
		}
	}

	/**
	 * 
	 * @Description:保存已读邮件id和已读邮件状态
	 * @see:
	 * @since:
	 * @author: gongfacun
	 * @date:2013-7-3
	 */
	public void save_read_uids(C35AccountManager manager, String uuid, String pushUids) {
		synchronized (manager.mPreferences) {
			SharedPreferences.Editor editor = manager.mPreferences.edit();
			editor.putString(uuid + ".push_uids", pushUids);
			editor.commit();
		}
	}
	/**
	 * 
	 * @Description:保存checkPush_xmlResult
	 * @see:
	 * @since:
	 * @author: gongfacun
	 * @date:2013-7-3
	 */
	public void save_checkPush_xmlResult(C35AccountManager manager, String uuid, String checkPush_xmlResult) {
		synchronized (manager.mPreferences) {
			SharedPreferences.Editor editor = manager.mPreferences.edit();
			editor.putString(uuid + ".checkPush_xmlResult", checkPush_xmlResult);
			editor.commit();
		}
	}
	
	public void save_read_flag(C35AccountManager manager, String uuid, boolean read_flag) {
		synchronized (manager.mPreferences) {
			SharedPreferences.Editor editor = manager.mPreferences.edit();
			editor.putBoolean(uuid + ".read_flags", read_flag);
			editor.commit();
		}
	}

	/**
	 * 保持账户设置SharedPreferences
	 * @param manager
	 * @param first
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-10-9
	 */
	public void save(C35AccountManager manager, Boolean first) {
		List<Account> accounts = null;
		synchronized (manager.mPreferences) {
			if (!manager.mPreferences.getString(C35AccountManager.ACCOUNTS_UUIDS_KEY, "").contains(mUuid)) {
				accounts = manager.getAccountsFromSP();//从SharedPreferences获取全部账号
				int accountsSize = accounts.size();// 暂存list的size大小优化性能
				int[] accountNumbers = new int[accountsSize];
				for (int i = 0; i < accountsSize; i++) {
					accountNumbers[i] = accounts.get(i).getAccountNumber();
				}
				Arrays.sort(accountNumbers);
				for (int accountNumber : accountNumbers) {
					if (accountNumber > mAccountNumber + 1) {
						break;
					}
					mAccountNumber = accountNumber;
				}
				mAccountNumber++;

				String accountids = manager.mPreferences.getString(C35AccountManager.ACCOUNTS_UUIDS_KEY, "");
				accountids += (accountids.length() != 0 ? "," : "") + mUuid;
				SharedPreferences.Editor editor = manager.mPreferences.edit();
				editor.putString(C35AccountManager.ACCOUNTS_UUIDS_KEY, accountids);
				editor.commit();
			}
			SharedPreferences.Editor editor = manager.mPreferences.edit();
			// 以下为私有设置
			editor.putString(mUuid + ".storeUri", Utility.base64Encode(mStoreUri));
			editor.putString(mUuid + ".name", mName);
			editor.putString(mUuid + ".email", mEmail);
			editor.putString(mUuid + ".mEmailShow", mEmailShow);
//			editor.putString(mUuid + ".description", mDescription);
			editor.putString(mUuid + ".hostIP", hostIP);
			editor.putInt(mUuid + ".updownloadport", updownloadport);
			editor.putInt(mUuid + ".mailPort", mailPort);
			editor.putString(mUuid + ".password", AesUtil.encrypt(password));
			editor.putString(mUuid + ".domainEnv", mDomainType);
			editor.remove(mUuid + ".transportUri");
			editor.putInt(mUuid + ".accountNumber", mAccountNumber);
			editor.putInt(mUuid + ".sendReadReceipts", mSendReadReceipts);
			editor.putBoolean(mUuid + ".tryaccount", tryaccount);
			editor.putBoolean(mUuid + ".pushOpen", pushOpen);// 每个账户不同，故为私有设置
			editor.putString(mUuid + ".push_uids", push_uids);
			editor.putString(mUuid + ".checkPush_xmlResult", checkPush_xmlResult);
			// editor.putBoolean(mUuid + ".localPushOpen", localPushOpen);
			String ids[] = manager.mPreferences.getString(C35AccountManager.ACCOUNTS_UUIDS_KEY, "").split(C35AccountManager.ACCOUNTS_IDS_SEPARATOR);
			// TODO:以下为公共设置
			if (first) {// 第一个账户？
				// Debug.d("c35", "save()::" + mSenderUri);
				Account firstaccount = EmailApplication.getCurrentAccount();
				editor.putString(mUuid + ".newLocalStoreUri", firstaccount.getLocalStoreUri());
				editor.putString(mUuid + ".mailSignature", firstaccount.getMailSignature());
				editor.putInt(mUuid + ".automaticCheckIntervalMinutes", firstaccount.getAutomaticCheckIntervalMinutes());
				editor.putInt(mUuid + ".mailReview", firstaccount.getMailReview());
				editor.putInt(mUuid + ".mailFont", firstaccount.getMailFont());
				editor.putInt(mUuid + ".recvMailMode", firstaccount.getRecvMailMode());
				editor.putInt(mUuid + ".recvMailLimit", firstaccount.getRecvMailLimit());
				// editor.putInt(mUuid + ".mailSize", firstaccount.getMailSize());
//				editor.putString(mUuid + ".accessCode", firstaccount.getAccessCode());
				editor.putLong(mUuid + ".lastAutomaticCheckTime", firstaccount.getLastAutomaticCheckTime());
				editor.putBoolean(mUuid + ".notifyNewMail", firstaccount.isNotifyNewMail());
				editor.putInt(mUuid + ".aliasType", firstaccount.getAliasType());
				// editor.putBoolean(mUuid + ".pushOpen", firstaccount.isPushOpen());
				editor.putBoolean(mUuid + ".localPushOpen", firstaccount.isLocalPushOpen());
				editor.putBoolean(mUuid + ".messageOnLoad", firstaccount.isMessageOnLoad());
				editor.putBoolean(mUuid + ".screenAutoChange", firstaccount.isScreenAutoChange());
				editor.putBoolean(mUuid + ".shakeUnRead", firstaccount.isShakeUnRead());
				editor.putBoolean(mUuid + ".autoSync", firstaccount.isAutoSync());
				// editor.putInt(uumUuid + ".accountNumber", mAccountNumber);
				editor.putBoolean(mUuid + ".vibrate", firstaccount.isVibrate());
				editor.putBoolean(mUuid + ".deskRemind", firstaccount.isDeskRemind());
				editor.putBoolean(mUuid + ".signatureOpen", firstaccount.isSignatureOpen());
				// editor.putBoolean(mUuid + ".voice", voice);
				editor.putString(mUuid + ".ringtone", firstaccount.getRingtone());
				editor.putBoolean(mUuid + ".freeTimeOpen", firstaccount.isFreeTimeOpen());
				editor.putBoolean(mUuid + ".saveCopy", firstaccount.isSaveCopy());
				editor.putBoolean(mUuid + ".checkPassword", firstaccount.isCheckPassword());
				editor.putString(mUuid + ".freeTimeStart", firstaccount.getFreeTimeStart());
				editor.putString(mUuid + ".freeTimeEnd", firstaccount.getFreeTimeEnd());
				editor.putInt(mUuid + ".lSUriType", firstaccount.getLSUriType());
				editor.putInt(mUuid + ".pushNotifyRange", firstaccount.getPushNotifyRange());
				editor.putBoolean(mUuid + ".autoPicture", firstaccount.isAutoPicture());
				editor.putInt(mUuid + ".pictureQuality", firstaccount.getPictureQuality());
				editor.putString(mUuid + ".language", firstaccount.getLanguage());
				// also delete any deprecated fields
			} else {// 所有账户设置相同，所有遍历
				for (String id : ids) {// 公共设置
					// Debug.d("c35", "save()::" + mSenderUri);
					editor.putString(id + ".newLocalStoreUri", mLocalStoreUri);
					editor.putString(id + ".mailSignature", mailSignature);
					editor.putInt(id + ".automaticCheckIntervalMinutes", mAutomaticCheckIntervalMinutes);
					editor.putInt(id + ".mailReview", mailReview);
					editor.putInt(id + ".mailFont", mailFont);
					editor.putInt(id + ".recvMailMode", recvMailMode);
					editor.putInt(id + ".recvMailLimit", recvMailLimit);
					// editor.putInt(id + ".mailSize", mailSize);
//					editor.putString(id + ".accessCode", accessCode);//密码，已经无用了
					editor.putLong(id + ".lastAutomaticCheckTime", mLastAutomaticCheckTime);
					editor.putBoolean(id + ".notifyNewMail", mNotifyNewMail);
					editor.putInt(id + ".aliasType", mAliasType);
					// editor.putBoolean(id + ".pushOpen", pushOpen);//与账户相关，非公共设置
					editor.putBoolean(id + ".localPushOpen", localPushOpen);
					editor.putBoolean(id + ".messageOnLoad", messageOnLoad);
					editor.putBoolean(id + ".screenAutoChange", screenAutoChange);
					editor.putBoolean(id + ".shakeUnRead", shakeUnRead);
					editor.putBoolean(id + ".autoSync", autoSync);
					// editor.putInt(uuid + ".accountNumber", mAccountNumber);
					editor.putBoolean(id + ".vibrate", mVibrate);
					editor.putBoolean(id + ".deskRemind", deskRemind);
					editor.putBoolean(id + ".signatureOpen", signatureOpen);
					// editor.putBoolean(mUuid + ".voice", voice);
					editor.putString(id + ".ringtone", mRingtoneUri);
					editor.putBoolean(id + ".freeTimeOpen", freeTimeOpen);
					editor.putBoolean(id + ".saveCopy", saveCopy);
					editor.putBoolean(id + ".checkPassword", checkPassword);
					editor.putString(id + ".freeTimeStart", freeTimeStart);
					editor.putString(id + ".freeTimeEnd", freeTimeEnd);
					editor.putInt(id + ".lSUriType", mLSUriType);
					editor.putInt(id + ".pushNotifyRange", pushNotifyRange);
					editor.putBoolean(id + ".autoPicture", autoPicture);
					editor.putInt(id + ".pictureQuality", pictureQuality);
					editor.putString(id + ".language", mLanguage);
					// also delete any deprecated fields
				}

			}

			editor.commit();
		}
	}

	/**
	 * 
	 * @Description:简单保持账户设置SharedPreferences
	 * @param manager
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-10-10
	 */
	public void saveSimple(C35AccountManager manager) {
		List<Account> accounts = null;
		synchronized (manager.mPreferences) {
			if (!manager.mPreferences.getString(C35AccountManager.ACCOUNTS_UUIDS_KEY, "").contains(mUuid)) {
				accounts = manager.getAccountsFromSP();
				int accountsSize = accounts.size();// 暂存list的size大小优化性能
				int[] accountNumbers = new int[accountsSize];
				for (int i = 0; i < accountsSize; i++) {
					accountNumbers[i] = accounts.get(i).getAccountNumber();
				}
				Arrays.sort(accountNumbers);
				for (int accountNumber : accountNumbers) {
					if (accountNumber > mAccountNumber + 1) {
						break;
					}
					mAccountNumber = accountNumber;
				}
				mAccountNumber++;

				String accountids = manager.mPreferences.getString(C35AccountManager.ACCOUNTS_UUIDS_KEY, "");
				accountids += (accountids.length() != 0 ? "," : "") + mUuid;
				SharedPreferences.Editor editor = manager.mPreferences.edit();
				editor.putString(C35AccountManager.ACCOUNTS_UUIDS_KEY, accountids);
				editor.commit();
			}
			SharedPreferences.Editor editor = manager.mPreferences.edit();
			// 以下为私有设置
			//N/A
			String ids[] = manager.mPreferences.getString(C35AccountManager.ACCOUNTS_UUIDS_KEY, "").split(C35AccountManager.ACCOUNTS_IDS_SEPARATOR);
			// TODO:以下为公共设置
			for (String id : ids) {// 公共设置
				editor.putBoolean(id + ".deskRemind", deskRemind);
			}
			editor.commit();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Account) {
			String emailA = ((Account) o).getEmail();
			String emailB = getEmail();
			if ((emailA.endsWith(MailUtil.EMAIL_SUFFIX_35CN) || emailA.endsWith(MailUtil.EMAIL_SUFFIX_CHINACHANNEL)) && (emailB.endsWith(MailUtil.EMAIL_SUFFIX_35CN) || emailB.endsWith(MailUtil.EMAIL_SUFFIX_CHINACHANNEL))) {
				return emailA.substring(0, emailA.indexOf('@')).equals(emailB.substring(0, emailB.indexOf('@')));
			} else {
				return emailA.equals(emailB);
			}
		}
		return super.equals(o);
	}

	// /**
	// * 邮件大小的阀值 -1无限制，0仅标题，其他数字为具体的kb值（如35即表示35KB）
	// */
	// public int getMailSize() {
	// return mailSize;
	// }
	//
	// public void setMailSize(int mailSize) {
	// GlobalVariable.setEmailSize(mailSize);
	// this.mailSize = mailSize;
	// }

	/** 邮件字体大小设置 0: 小1:默认2:中 3:大 */
	public int getMailFont() {
		return mailFont;
	}

	public void setMailFont(int mailFont) {
		this.mailFont = mailFont;
	}

	// 0表示全部提醒，1表示只提醒重要发件人的邮件，2表示只提醒主送我的邮件
	public int getPushNotifyRange() {
		return pushNotifyRange;
	}

	public void setPushNotifyRange(int pushNotifyRange) {
		this.pushNotifyRange = pushNotifyRange;
	}

	public String getUuid() {
		return mUuid;
	}

	public int getAccountNumber() {
		return mAccountNumber;
	}

	public String getStoreUri() {
		return mStoreUri;
	}

	public void setStoreUri(String storeUri) {
		this.mStoreUri = storeUri;
	}

//	public String getDescription() {
//		return mDescription;
//	}
//
//	public void setDescription(String description) {
//		this.mDescription = description;
//	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getEmail() {
		return mEmail;
	}

	public void setEmail(String email) {
		this.mEmail = email;
	}

	public boolean isVibrate() {
		return mVibrate;
	}

	public void setVibrate(boolean vibrate) {
		mVibrate = vibrate;
	}

	public boolean isSaveCopy() {
		return saveCopy;
	}

	public void setSaveCopy(boolean saveCopy) {
		this.saveCopy = saveCopy;
	}

	public String getRingtone() {
		return mRingtoneUri;
	}

	public void setRingtone(String ringtoneUri) {
		mRingtoneUri = ringtoneUri;
	}

	public String toString() {//mEmail
		return mEmail;
	}

	public String getLocalStoreUri() {
		return mLocalStoreUri;
	}

	public void setLocalStoreUri(String localStoreUri) {
		this.mLocalStoreUri = localStoreUri;
	}

	/**
	 * Returns -1 for never.
	 */
	public int getAutomaticCheckIntervalMinutes() {
		return mAutomaticCheckIntervalMinutes;
	}

	/**
	 * @param automaticCheckIntervalMinutes
	 *            or -1 for never.
	 */
	public void setAutomaticCheckIntervalMinutes(int automaticCheckIntervalMinutes) {
		this.mAutomaticCheckIntervalMinutes = automaticCheckIntervalMinutes;
	}

	public long getLastAutomaticCheckTime() {
		return mLastAutomaticCheckTime;
	}

	public void setLastAutomaticCheckTime(long lastAutomaticCheckTime) {
		this.mLastAutomaticCheckTime = lastAutomaticCheckTime;
	}

	public boolean isNotifyNewMail() {
		return mNotifyNewMail;
	}

	public void setNotifyNewMail(boolean notifyNewMail) {
		this.mNotifyNewMail = notifyNewMail;
	}

	public int getAliasType() {
		return mAliasType;
	}

	public void setAliasType(int aliasType) {
		this.mAliasType = aliasType;
	}

	public boolean isTryaccount() {
		return tryaccount;
	}

	public void setTryaccount(boolean tryaccount) {
		this.tryaccount = tryaccount;
	}

	public String getmEmailShow() {
		return mEmailShow;
	}

	public void setmEmailShow(String mEmailShow) {
		this.mEmailShow = mEmailShow;
	}

//	public String getmDescription() {
//		return mDescription;
//	}

	public int getmSendReadReceipts() {
		return mSendReadReceipts;
	}

	public void setmSendReadReceipts(int mSendReadReceipts) {
		this.mSendReadReceipts = mSendReadReceipts;
	}

//	public void setmDescription(String mDescription) {
//		this.mDescription = mDescription;
//	}

	public String getmName() {
		return mName;
	}

	public void setmName(String mName) {
		this.mName = mName;
	}

	public int getLSUriType() {
		return mLSUriType;
	}

	public void setLSUriType(int uriType) {
		mLSUriType = uriType;
	}

	/**
	 * 表示本地是否开启
	 * 
	 * @param localPushOpen
	 */
	public void setLocalPushOpen(boolean localPushOpen) {
		Debug.d(TAG, "localPushOpen = " + localPushOpen);
		this.localPushOpen = localPushOpen;
	}

	public boolean isLocalPushOpen() {
		return localPushOpen;
	}

	/**
	 * 设置屏幕是否自动切换
	 */
	public void setScreenAutoChange(boolean screenAuto) {
		this.screenAutoChange = screenAuto;
	}

	public boolean isScreenAutoChange() {
		return screenAutoChange;
	}

	/**
	 * 设置摇一摇查看未读邮件
	 */
	public void setShakeUnRead(boolean shakeUnRead) {
		this.shakeUnRead = shakeUnRead;
	}

	public boolean isShakeUnRead() {
		return shakeUnRead;
	}

	/**
	 * 设置是否自动同步
	 */
	public void setAutoSync(boolean autoSyn) {
		this.autoSync = autoSyn;
	}

	public boolean isAutoSync() {
		return autoSync;
	}

	/**
	 * 设置邮件列表触底是否接受邮件
	 */
	public void setMessageOnLoad(boolean messageOnLoad) {
		this.messageOnLoad = messageOnLoad;
	}

	public boolean isMessageOnLoad() {
		return messageOnLoad;
	}

	/**
	 * 表示服务器端的push业务是否开通
	 * 
	 * @return
	 */
	public boolean isPushOpen() {
		return pushOpen;
	}

	public void setPushOpen(boolean pushOpen) {
		this.pushOpen = pushOpen;
	}

	public boolean isSignatureOpen() {
		return signatureOpen;
	}

	public void setSignatureOpen(boolean signatureOpen) {
		this.signatureOpen = signatureOpen;
	}

	public String getMailSignature() {
		return mailSignature;
	}

	public void setMailSignature(String mailSignature) {
		this.mailSignature = mailSignature;
	}

	public boolean isDeskRemind() {
		return deskRemind;
	}

	public void setDeskRemind(boolean deskRemind) {
		this.deskRemind = deskRemind;
	}

	/**
	 * 邮件内容预览设置 0:无预览 1:1行预览 2:2行预览 3:3行预览
	 * 
	 * @return
	 */
	public int getMailReview() {
		return mailReview;
	}

	public void setMailReview(int mailReview) {
		this.mailReview = mailReview;
	}

	public String getFreeTimeStart() {
		return freeTimeStart;
	}

	public void setFreeTimeStart(String freeTimeStart) {
		this.freeTimeStart = freeTimeStart;
	}

	public String getFreeTimeEnd() {
		return freeTimeEnd;
	}

	public void setFreeTimeEnd(String freeTimeEnd) {
		this.freeTimeEnd = freeTimeEnd;
	}

	public boolean isFreeTimeOpen() {
		return freeTimeOpen;
	}

	public void setFreeTimeOpen(boolean freeTimeOpen) {
		this.freeTimeOpen = freeTimeOpen;
	}

	public boolean isCheckPassword() {
		return checkPassword;
	}

	public void setCheckPassword(boolean checkPassword) {
		this.checkPassword = checkPassword;
	}

	public int getUpdownloadport() {
		return updownloadport;
	}

	public void setUpdownloadport(int updownloadport) {
		this.updownloadport = updownloadport;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAccessCode() {
		return accessCode;
	}

	public void setAccessCode(String accessCode) {
		this.accessCode = accessCode;
	}

	public int getRecvMailLimit() {
		return recvMailLimit;
	}

	public void setRecvMailLimit(int recvMailLimit) {
		this.recvMailLimit = recvMailLimit;
	}

	public void setAutoPicture(boolean autoPicture) {
		this.autoPicture = autoPicture;
	}

	public boolean isAutoPicture() {
		return autoPicture;
	}

	public void setPictureQuality(int pictureQuality) {
		this.pictureQuality = pictureQuality;
	}

	public int getPictureQuality() {
		return pictureQuality;
	}

	public int getRecvMailMode() {
		return recvMailMode;
	}

	public void setRecvMode(int recvMailMode) {
		this.recvMailMode = recvMailMode;
	}

	/**
	 * 
	 * 设置语言
	 */
	public void setLanguage(String language) {
		this.mLanguage = language;
	}

	public String getLanguage() {
		return mLanguage;
	}

	public String getDomainType() {
		return mDomainType;
	}

	public void setDomainType(String domainEnv) {
		this.mDomainType = domainEnv;
	}
	
	public String getHostIP() {
		return hostIP;
	}

	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}

	public int getMailPort() {
		return mailPort;
	}

	public void setMailPort(int mailPort) {
		this.mailPort = mailPort;
	}

}
