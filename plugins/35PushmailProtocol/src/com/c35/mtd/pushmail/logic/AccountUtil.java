package com.c35.mtd.pushmail.logic;

import java.net.URI;
import java.util.HashMap;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.c35.mtd.pushmail.C35MailThreadPool;
import com.c35.mtd.pushmail.C35MailThreadPool.ENUM_Thread_Level;
import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.GlobalConstants;
import com.c35.mtd.pushmail.GlobalVariable;
import com.c35.mtd.pushmail.R;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.beans.ProxyServerDomain;
import com.c35.mtd.pushmail.ent.logic.EntContactsLogic;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.store.C35Store;
import com.c35.mtd.pushmail.store.LocalStore;
import com.c35.mtd.pushmail.store.LocalStoreAccountsInfo;
import com.c35.mtd.pushmail.store.Store;
import com.c35.mtd.pushmail.util.C35AppServiceUtil;
import com.c35.mtd.pushmail.util.C35MailMessageUtil;
import com.c35.mtd.pushmail.util.C35ServerUtil;
import com.c35.mtd.pushmail.util.MailUtil;
import com.c35.mtd.pushmail.util.NotificationClose;
import com.c35.mtd.pushmail.util.StoreDirectory;
import com.c35.mtd.pushmail.util.StringUtil;
import com.c35.ptc.as.util.C35OpenHttpPostFile;

/**
 * 对login与添加账户的逻辑进行统一处理的类
 * 
 * @Description:
 * @author:CuiWei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-9
 */
public class AccountUtil {

	private static final String TAG = "AccountUtil";
	private Context context;
	private ProgressDialog mpDialog = null;
	public static ProgressDialog mpDelDialog = null;
	private String email = "";
	private String password = "";

	private Handler myMessageHandler;
	private Account account;
	private boolean fromOther = false;// 从外部获取到账号，并且直接拿来登陆操做的标记

	private String strTag = "LoginValidate";
	private final static String DP_SOFIA_1="1";//代收
	private final static String DP_SOFIA_2="2";
	private final static String DP_SOFIA_3="3";
	// private String mHost = MailUtil.PROXY_SERVER_IP;

	Application app;

	private boolean bSetDefaultAccount = true;
//	public Handler handlerFolderList;

	/**
	 * 此构造方法必须在UI线程中执行，否则myMessageHandler发消息失败。
	 * 
	 * @param context
	 * @param email
	 * @param password
	 * @param account
	 * @param fromOther
	 * @param strTag
	 * @param defaultAccount
	 */
	public AccountUtil(Context context, String email, String password, Account account, boolean fromOther, String strTag, boolean defaultAccount) {
		this.context = context;
		this.email = email;
		this.password = password;
		this.account = account;
		this.fromOther = fromOther;
		this.strTag = strTag;
		this.bSetDefaultAccount = defaultAccount;
		// 若从main activity里跳转过来，且是在后台线程里调用的，因此不能new handler

//		if (!(context instanceof MainActivity)) {
		// Modified by LL
		// BEGIN
		if (!(context == null)) {
		// END
			this.myMessageHandler = new AccountHandler();
//			this.handlerFolderList = new FolderListHandler();
		}
	}

	private boolean isConnectInternet() {
		ConnectivityManager manager = (ConnectivityManager) EmailApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		return info != null;
	}

	/**
	 * 去两个服务器做校验
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: CuiWei
	 * @date:2012-11-9
	 */
	public void doValidate() {
		if (!isConnectInternet()) {
			Log.e(TAG, context.getString(R.string.show_no_connection_activity_caption));
			return;
		}

		// 初始化账户
		// -------------改变progress的文字---------------
		// Modified by LL
		// BEGIN
		/*
		if (!(context instanceof MainActivity)) {
			try {
				// 从MainActivity过来，是从线程里执行的
				mpDialog = ProgressDialog.show(context, null, context.getString(R.string.progress_Info_login), true);//com.c35.mtd.pushmail.activity.LoginActivity has leaked window
				mpDialog.setCancelable(false);
			} catch (Exception e) {
				Debug.e("failfast", "failfast_AA", e);
			}
		}
		*/
		// END

		initAccount(email, password);
		/**
		 * 此线程与服务端做校验，校验内容有： 账号是否为35即时邮 是否为push用户 本地push开关是否打开
		 */
		C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_common).submit(new Runnable() {
			@Override
			public void run() {

				try {
					account = getProxyDomainToAccountFromDP(email, password, account);
					email = account.getEmail();
					email = MailUtil.convert35CNToChinaChannel(email);

//					C35ServerUtil.checkPush(account, email);
					saveAccountAndRegistePush();

					Message msg = new Message();
					msg.what = GlobalConstants.VALIDATE_SUCCESS;
					sendValidateMsg(msg);
				} catch (MessagingException e) {
					Message msg = new Message();
					switch (e.getExceptionType()) {
					case MessagingException.CONNECT_ERROR:
						msg.what = GlobalConstants.CHECK_INTERNET_ERROR;
						break;
					case MessagingException.CODE_LOGIN_NO:
						msg.what = GlobalConstants.VALIDATE_ERROR_PASSWORD;
						break;
					case MessagingException.CODE_VERSION_NO:
						msg.what = GlobalConstants.CHECK_VERSION_ON;
						break;
					case MessagingException.LOGIN_CHECKPUSH_ERROR:
						msg.what = GlobalConstants.LOGIN_CHECKPUSH_ERROR;
						msg.obj = e.getMessage();
						break;
					case MessagingException.LOGIN_SAVEACCOUNT_REGISTEPUSH_ERROR:
						msg.what = GlobalConstants.LOGIN_SAVEACCOUNT_REGISTEPUSH_ERROR;
						msg.obj = e.getMessage();
						break;
					case MessagingException.RETURN_COMMAND_ERROR_LINK_TIMEOUT:
						msg.what = GlobalConstants.LOGIN_LINK_TIMEOUT;
						msg.obj = e.getMessage();
						break;
					default://此message需要判断是否需要删除刚添加的账户
						Debug.e("failfast","e.getExceptionType()"+e.getExceptionType(), e);
						msg.obj = e.getMessage();
						msg.what = GlobalConstants.LOGIN_OTHERS_ERROR;
						break;
					}
					sendValidateMsg(msg);
				}catch (Exception e) {
					Debug.e("failfast", "failfast_AA", e);
					C35AppServiceUtil.writeSubscribeInformationToSdcard("doValidate err:" + e.getMessage());// 彩蛋log写入
					Message m = new Message();
					m.obj = e.getMessage();
					m.what = GlobalConstants.LOGIN_OTHERS_ERROR;
					sendValidateMsg(m);
				}
	
			}

		});
	}

	/**
	 * 初始化Account
	 * 
	 * @Description:
	 * @param email
	 * @param password
	 * @see:
	 * @since:
	 * @author: CuiWei
	 * @date:2012-11-9
	 */
	private void initAccount(String email, String password) {
	    /*
		if (context instanceof LoginActivity) {
			// 本地如果有账户也不能登陆。
			if (EmailApplication.getCurrentAccount() == null) {
				Debug.v("AccountUtil.java", "shifoucong_______________________citongguo::::::::::: ");
				account = C35AccountManager.getInstance().createAccount(email, password);
			}
		} else {
			account = C35AccountManager.getInstance().createAccount(email, password);
		}
		*/
	    // Modified by LL
	    // BEGIN
	    account = C35AccountManager.getInstance().createAccount(email, password);
	    // END
	}

	/**
	 * 通过dp获取帐号代理服务器的地址
	 * @param emailShow   pm1@35.cn
	 * @param password
	 * @param gaccount
	 * @return
	 * @throws Exception
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:Aug 14, 2013
	 */
	public static Account getProxyDomainToAccountFromDP(String emailShow, String password, Account gaccount) throws Exception {
		URI dPUri = new URI(MailUtil.STORE_SCHEME_C35PROXY, emailShow + ":" + password, MailUtil.DP_SERVER_DOMAIN_HOST, MailUtil.DP_SERVER_DOMAIN_PORT, null, null, null);
		Debug.i(TAG, "uri:" + dPUri);// c35proxy://pm3%4035.cn:qyyx12369A@wmail215.cn4e.com:5566
		// 构造c35store                    //  c35proxy://mail35test%40126.com:mail35@mail.magic.35.com:5566
		C35Store dpStore = (C35Store) Store.getInstance(dPUri.toString());
		dpStore.dpOpen();
		ProxyServerDomain psdomain = dpStore.getProxyServerDomainFromDP(emailShow);
		Store.clearAccountStore(dPUri.toString());//c35proxy://pm3%4035.cn:qyyx12369A@wmail215.cn4e.com:5566
		gaccount.setHostIP(psdomain.getHost());//new  mail.35.cn
		gaccount.setMailPort(psdomain.getPort());//new  9999
		gaccount.setUpdownloadport(psdomain.getUpdownport());//new 9998
		URI accountUri = new URI(MailUtil.STORE_SCHEME_C35PROXY, emailShow + ":" + password, psdomain.getHost(), psdomain.getPort() > 0 ? psdomain.getPort() : MailUtil.PROXY_SERVER_MAIL_PORT, null, null, null);
		gaccount.setStoreUri(accountUri.toString());// c35proxy://pm1%4035.cn:qyyx12369@mail.china-channel.com:9999
		gaccount.setUpdownloadport(psdomain.getUpdownport() > 0 ? psdomain.getUpdownport() : MailUtil.PROXY_SERVER_ATTACHMENT_PORT);
		gaccount.setDomainType(Integer.toString(psdomain.getDomaintype()));
		gaccount.setAliasType(psdomain.getAliastype());
		
		//去代理服务器验证账户密码的正确性
		C35Store realStore = (C35Store) Store.getInstance(accountUri.toString());
		realStore.openAndGetTicket();
		//验证正确后（无exception）取真实地址
//		if (psdomain.getDomaintype() > 0 && psdomain.getAliastype() == 1) {//1域别名
		if (psdomain.getDomaintype() == 3 ) {//我确认一下domainType=3时，需要调用getRealMail取真实mail地址，其他情况不需要取真实地址，是这样吧？
			String realMail = realStore.getRealMail(emailShow);
			if (realMail != null) {
				gaccount.setmEmailShow(emailShow);
				gaccount.setEmail(realMail);
			}else{
				C35AppServiceUtil.writeSubscribeInformationToSdcard("getRealMail: null");// 彩蛋log写入
				gaccount.setmEmailShow(emailShow);
				gaccount.setEmail(MailUtil.convert35CNToChinaChannel(emailShow));
			}
		}
		if(psdomain.getDomaintype() == 1){
			gaccount.setEmail(emailShow);
		}
		gaccount.save_mail_uri(C35AccountManager.getInstance(), gaccount.getUuid());
		C35ServerUtil.parseXmlAndSave(gaccount, psdomain.getPushAction());
		return gaccount;
	}


	/**
	 * 
	 * @Description:去服务器做校验，并保存账户数据，注册push功能
	 * @param:
	 * @return:
	 * @throws:
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @throws Exception 
	 * @date:2012-9-11
	 */
	public void saveAccountAndRegistePush() throws MessagingException {
		Log.d("TAG", "保存本地账号，并且注册push");
		try {
			// 保存账号到本地
			save2SharedPreferences(account);
			// 保存账户信息供其它程序访问
			saveAccountInfoToDB(account);

			if (!fromOther) {
				Store.getInstance(account.getLocalStoreUri());// 数据库的初始化
			} else {
				// getAccountsUnreadEmail();// 如果是获取别人的账号列表进行登录的，则获取各个账号的未读邮件
			}
			C35AccountManager.getInstance().insertAccountData(account);

			// 注册IPPush
			registerIPPush();
			
			// 登录成功，上传装机量相关信息；
			uploadInstallInfo();

			if (fromOther) {
				email = "";
				password = "";
				fromOther = false;
			}
		} catch (Exception e) {
			throw new MessagingException(MessagingException.LOGIN_SAVEACCOUNT_REGISTEPUSH_ERROR,e.getMessage());
		} finally {
			if (fromOther) {
				email = "";
				password = "";
				fromOther = false;
			}
			dismissProgressDlg();
		}
	}

	/**
	 *   上传装机信息
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: zhangqian
	 * @date:2012-11-9
	 */
	private void uploadInstallInfo() {
		C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_common).submit(new Runnable() {

			@Override
			public void run() {
				Debug.v(TAG, "登录成功! 上传统计装机量所需的信息");
				boolean ifUpOk = (new C35OpenHttpPostFile()).upload35AppInfos(context, account.getmEmailShow());// 装机量上传
				C35AppServiceUtil.writeSubscribeInformationToSdcard("Login_success_upload35AppInfos:" + ifUpOk);// 彩蛋log写入
			}
		});
	}

	/**
	 * 保存新创建的账户到本地
	 * 
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: CuiWei
	 * @date:2012-11-9
	 */
	//private void save2SharedPreferences(Account account) {
	// Modified by LL
	public void save2SharedPreferences(Account account) {

		C35AccountManager manager = C35AccountManager.getInstance();
		if (manager.getDefaultAccountUuid() == null || "".equals(manager.getDefaultAccountUuid())) {
			account.save(manager, false);
		} else {
			account.save(manager, true);
		}
		if (manager.getDefaultAccountUuid() == null || "".equals(manager.getDefaultAccountUuid())) {
			manager.changeDefaultAccount(account.getUuid());
		}
		
		// Modified by LL
//		EmailApplication.setServicesEnabled(context);
	}

	/**
	 * 保存账户信息到account_info数据库的account表内，用于供其它应用访问
	 * 
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: CuiWei
	 * @date:2012-11-9
	 */
	private void saveAccountInfoToDB(Account account) {
		LocalStoreAccountsInfo accountInfo = new LocalStoreAccountsInfo();
		accountInfo.open();
		accountInfo.insertData(account.getmEmailShow(), account.getPassword(), "");
		accountInfo.close();
		StringUtil.writeString2Array(account.getmEmailShow(), GlobalConstants.FILE_SAVE_ADDED_ACCOUNT_ADDR);
	}


	/**
	 * 注册IPPush
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: CuiWei
	 * @date:2012-11-9
	 */
	private void registerIPPush() {
	    // Modified by LL
	    // BEGIN
	    /*
	    Debug.d(TAG, "注册IPPush");
		String email = account.getEmail();
		// Log.d(TAG,username);
		String password = account.getPassword();
		// if (email.trim().endsWith(MailUtil.DOMAIN_35CN)) {
		// email = email.replace(MailUtil.DOMAIN_35CN, MailUtil.DOMAIN_CHINACHANNEL);
		// }
		email = MailUtil.convert35CNToChinaChannel(email);
		String content = " , " + email + " , RegisterIPPush , Send  Code: LoginActivity.registerIPPush";
		C35AppServiceUtil.writeSubscribeInformationToSdcard(content);
		C35AppServiceUtil.registerIPPush(email, password);
		
		// 无论上一步注册push成功与否，都会起一个service定时注册push
		Intent i = new Intent(context, PushMailService.class);
		i.setAction("TIMER_REGISTE");
		EmailApplication.getInstance().startService(i);
		*/
	    // END
	}

	/**
	 * 
	 * @Description:删除帐号
	 * @param saccount
	 * @param context
	 * @param sizes
	 * @param accountmanager
	 * @see:
	 * @since:
	 * @author: gongfacun
	 * @date:2012-11-15
	 */
	private void delAccount() {

		final String uri = account.getLocalStoreUri();
		mpDelDialog = ProgressDialog.show(context, null, context.getString(R.string.delete_account_show_message), true);
		C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_AtOnce).submit(new Runnable() {

			@Override
			public void run() {
				String user = account.getEmail();
				user = MailUtil.convert35CNToChinaChannel(user);
				String content = " , " + account.getEmail() + " , UnregisterIPPush , Send  Code: FolderList.showDeleteAccountDialog";
				GlobalVariable.downloadCatch.clear();
				try {
					C35Store store = (C35Store) Store.getInstance(account.getStoreUri());
					store.closeSocket();
				} catch (Exception e) {
					Debug.e("failfast", "failfast_AA", e);
				}
				// 通知删掉
//				NotificationManager notifMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				NotificationClose.closeAllNotifications();
				context.getSharedPreferences(GlobalConstants.PREF_INFO, Context.MODE_PRIVATE).edit().clear().commit();
				
				// Modified by LL
				//PushMailService.forceAction(PushMailService.ACTION_ACCOUNT_DEL);
				
				EmailApplication.setServicesEnabled(context);
				// 定时查收选项置为空
				GlobalVariable.setTEMP_TIME("");
				// 删除账号后下次进入算第一次进入
				// 删除数据
				deleteAccountInfo(account);// 从共享数据库里删除账号

				C35AppServiceUtil.writeSubscribeInformationToSdcard(content);
				C35AppServiceUtil.unregisterIPPush(account.getEmail());
				final C35AccountManager manager = C35AccountManager.getInstance();
				int size = manager.getAccountsSize();
				if (size == 1) {//SharedPreferences得到的账户个数
					Debug.i(TAG, "accountsize____size:" + size);
					try {
						C35MailMessageUtil.sendMailMessageBroadcast(context, (LocalStore) Store.getInstance(uri), true);
					} catch (Exception e) {
						Debug.e("failfast", "failfast_AA", e);
					}
					manager.destroyAccount(account);
					Message msg = new Message();
					msg.what = GlobalConstants.DEL_ACCOUNT_ALL;
					myMessageHandler.sendMessage(msg);
					
					// Modified by LL
					// BEGIN
					/*
					ActivityStackManager.getInstance().exitApp();
					MainActivity.actionMainAccount(context);
					*/
					// END
					
					Debug.i(TAG, "delete____all______accounts");
					// } else {
					// Message msg = new Message();
					// msg.what = GlobalConstants.DEL_ACCOUNT_FAIL_SHOW;
					// handlerFolderList.sendMessage(msg);
					// }
				} else {
					Debug.i(TAG, "delete____one______accoun");
					boolean bDelFromDB = manager.deleteAccount(account);
					if(!bDelFromDB){//删除失败
						dismissDelDialog();
						Log.e(TAG, context.getString(R.string.delete_account_failed));
						return;
					}
					Message msg = new Message();
					boolean isDefaultAccount = manager.getDefaultAccountUuid().equals(account.getUuid());
					if (isDefaultAccount) {
						String uuid = manager.choseUuidfordefault(account.getUuid());
						manager.changeDefaultAccount(uuid);
					}
					
					/*
					if (context instanceof SettingAccount) {
						if (isDefaultAccount) {
							msg.what = GlobalConstants.DEL_DEFAULT_ACCOUNT_SHOW_SET;
						} else {
							msg.what = GlobalConstants.DEL_NOT_DEFAULT_ACCOUNT_SHOW_SET;
						}
					} else if (context instanceof FolderList) {
						if (isDefaultAccount) {
							msg.what = GlobalConstants.DEL_DEFAULT_ACCOUNT_SHOW_SET;
						} else {
							msg.what = GlobalConstants.DEL_NOT_DEFAULT_ACCOUNT_SHOW_SET;
						}
					} else {
						msg.what = GlobalConstants.DEL_ACCOUNT_SHOW;
					}
					*/
					// Modified by LL
					msg.what = GlobalConstants.DEL_ACCOUNT_SHOW;
					
					myMessageHandler.sendMessage(msg);
					try {
						C35MailMessageUtil.sendMailMessageBroadcast(context, (LocalStore) Store.getInstance(uri), false);
					} catch (Exception e) {
						Debug.e("failfast", "failfast_AA", e);
					}
				}
			}
		});

	}

	/**
	 * 从共享数据库删除账户 信息
	 * 
	 * @Description:
	 * @param emailAddr
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-9-3
	 */
	private void deleteAccountInfo(Account account) {
		LocalStoreAccountsInfo accountInfo = new LocalStoreAccountsInfo();
		accountInfo.open();
		accountInfo.deleteData(account.getmEmailShow());
		accountInfo.close();
	}

	/**
	 * @Title: showDeleteAccountDialog
	 * @Description: 描述 弹出一个删除账号的对话框，删除账号的操作都在这里了
	 * @return
	 * @return
	 * @throws
	 */
	public void showDeleteAccountDialog() {
	    /*
		Dialog dialog = null;
		MailDialog.Builder builder = new MailDialog.Builder(context);
		builder.setTitle(context.getString(R.string.setting_reset_account_title)).setMessage(context.getString(R.string.delete_account_accountlist) + account.getmEmailShow() + " ?").setPositiveButton(R.string.okay_action, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
				delAccount();

				if (SettingAccount.isSetting&&SettingAccount.mHandler != null) {
					SettingAccount.mHandler.sendEmptyMessage(SetAccountHandler.ACTION_DEL_ACCOUNT);
				}
				if (FolderList.isFolder&&FolderList.mHandler != null) {
					FolderList.mHandler.sendEmptyMessage(MailboxHandler.DEL_ACCOUNT_AFTER_ACTION);
				}

//				if((!FolderList.isFolder&&SettingAccount.mHandler == null)||(!SettingAccount.isSetting&&SettingAccount.mHandler == null)){
//					dismissDelDialog();
//				}

				SettingAccount.isSetting=false;
				FolderList.isFolder=false;
			}
		}).setNegativeButton(R.string.cancel_action, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				Debug.i(TAG, "button cancel1111111111");
				dialog.dismiss();
			}
		});
		dialog = builder.create();
		dialog.show();
		*/
		// Modified by LL
		delAccount();
	}
	
	/**
	 * dismiss删除对话框
	 * @see: 
	 * @since: 
	 * @author: zhangqian
	 * @date:2013-11-27
	 */
	public static void dismissDelDialog(){
		if(mpDelDialog != null && mpDelDialog.isShowing()){
			mpDelDialog.dismiss();
			mpDelDialog=null;
		}
	}
	
	// sofia2.0不支持的接口。
	public static HashMap<String, String> requestCommands = new HashMap<String, String>();

	static {
		// sofia2.0不支持的接口.
		// requestCommands.put("advanceSearchMails", "");// 文档中说只支持部分接口。
		requestCommands.put("fileViewByHtml", "");
		requestCommands.put("getArchiveFolderList", "");// 3.0版本中没有用到。
		requestCommands.put("partOfCommitSyn", "");// 实际没有此接口，这是为了sofia2.0同步操作中过滤收藏和还原操作而定制的。和过滤已发送夹子的同步
		requestCommands.put("getSelfDefinedFolderList", "");
		requestCommands.put("getContacts", "");
		// 3.1中新增的接口。
		requestCommands.put("searchAttachList", "");// 查询附件列表。
		requestCommands.put("getBetweenUsMail", "");// 获取往来邮件。
		requestCommands.put("recallMail", "");// 撤回邮件。
		requestCommands.put("searchIdsByType", "");// 按类型查邮件ID列表。
		requestCommands.put("updateCalendarState", "");// 修改会议状态 。
		requestCommands.put("getRealEmail", "");// 获取别名对应的帐号。
//		requestCommands.put("getSendReadMail", "");// 获取已经读邮件回执。
//		requestCommands.put("updateSendReadMail", "");// 更新已读邮件回执。

		requestCommands.put("synEntContacts", "");// 同步企业联系人。
		requestCommands.put("ReadPush", "");// 接收显示已读push
	}
//	/**
//	 * 
//	 * @Description:更新uri
//	 * @see:
//	 * @since:
//	 * @author: gongfc
//	 * @date:Aug 15, 2013
//	 */
//	private void refreshProxyMailserver() {
//		try {
////			AccountUtil account_util = new AccountUtil(mContext, account.getEmail(), account.getPassword(), account, false, TAG, false);
//			account = AccountUtil.getRealProxyServerDomain(account.getEmail(), account.getPassword(), account);
//			account.save_mail_uri(C35AccountManager.getInstance(), account.getUuid());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	/**
	 * 判断账号所在的系统环境（如sofia2.0，sofia3.0）是否支持某些接口。
	 * 
	 * @Description:
	 * @param requestName
	 * @param account
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-4-15
	 */
	public static Boolean isSupportRequest(String requestName, Account account) {
		// 为覆盖安装临时添加的.
		if ((account.getEmail().endsWith("@try.35.cn")) || (account != null && account.getHostIP().contains("?") ||account != null && account.getDomainType() == null) || (account != null && "".equals(account.getDomainType())) || (account != null && "\n".equals(account.getDomainType()))) {
//			String[] check = C35ServerUtil.mailStateCheck(account.getmEmail());
//			// {"statusCode":0,"desc":"查询成功","type":"zhao@sinnowa.com","domain":"sinnowa.com","version":"2.0"}
//			if ("0".equals(check[0])) {
//				account.setDomainEnv(check[1]);
//				account.save(C35AccountManager.getInstance(), false);
//			} else {// 状态返回错误
//				Debug.d("c35", check[0]);
//			}
			
			try {
				getProxyDomainToAccountFromDP(account.getmEmailShow(), account.getPassword(), account);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Debug.w(TAG, "failfast_AA", e);
				C35AppServiceUtil.writeSubscribeInformationToSdcard("getRealProxyServerDomain:" + e.getMessage());// 彩蛋log写入
			}
		}

		if ((account != null && DP_SOFIA_3.equals(account.getDomainType()))) {
			// 账号为sofia3.0,则所有接口都支持。//||(account != null && DP_SOFIA_1.equals(account.getDomainType()))
			return true;
		} else if (account != null && DP_SOFIA_2.equals(account.getDomainType())) {
			if (requestCommands.get(requestName) == null) {// 没找到。
				// 账号为sofia2.0,requestCommands中没有此接口。
				return true;
			} else if ("".equals(requestCommands.get(requestName))) {// 找到。
				// 账号为sofia2.0,requestCommands中有此接口。
				return false;
			}
		} else if (account != null && DP_SOFIA_1.equals(account.getDomainType())) {// 账号为sofia1.0,则
			if("searchIdsByType".equals(requestName)){//代收不支持搜索，不支持收藏夹同步
				return false;
			}else{
				return true;
			}				
		} else {
			// 其他原因，没有获取到账号系统环境信息。
			return false;
		}
		// 走到这里，说明前面没有符合，也返回null;
		return false;
	}

	private static boolean isShowToast = true;

	// 为sofia2下某些接口不支持，弹吐司操作。
	public static void nosupportRequestToast() {
		if (isShowToast) {
		    Log.e(TAG, EmailApplication.getInstance().getString(R.string.sofia_environment_as_toast));
		}
	}
	/**
	 * 
	 * @Description:
	 * @author:  cuiwei
	 * @see:   
	 * @since:      
	 * @copyright © 35.com
	 * @Date:2013-11-1
	 */
	private class AccountHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
		    // Modified by LL
		    /*
			if (context instanceof MainActivity) {

			} else {
			*/

				switch (msg.what) {
				case GlobalConstants.VALIDATE_SUCCESS:
					loginSuccessLogic();
					dismissProgressDlg();// 弹窗隐藏
					break;
				case GlobalConstants.SERVER_ERROR:
					if (Debug.LOGTOAST&&msg.obj != null) {
					    Log.e(TAG, context.getString(R.string.login_not_35Account) + (String) msg.obj);
					} else {
						if (msg.arg1 > 0) {
							int stringNumber = C35MailMessageUtil.getIdForMessageException(new MessagingException(msg.arg1));
							Log.e(TAG, context.getString(stringNumber));
						} else {
						    Log.e(TAG, context.getString(R.string.login_server_failed));
						}
					}
					dismissProgressDlg();
					break;
				case GlobalConstants.LOGIN_LINK_TIMEOUT:
					if (Debug.LOGTOAST&&msg.obj != null) {
					    Log.e(TAG, context.getString(R.string.experience_connectserver_timeout)+ (String) msg.obj);
					} else {
					    Log.e(TAG, context.getString(R.string.experience_connectserver_timeout));
					}
					dismissProgressDlg();
					break;
				case GlobalConstants.LOGIN_CHECKPUSH_ERROR://此message需要判断是否需要删除刚添加的账户
					if (Debug.LOGTOAST&&msg.obj != null) {
					    Log.e(TAG, "LOGIN_CHECKPUSH_ERROR " + (String) msg.obj);
					} else {
//						MailToast.makeText("LOGIN_CHECKPUSH_ERROR", Toast.LENGTH_SHORT).show();
					}
					C35AppServiceUtil.writeSubscribeInformationToSdcard("LOGIN_CHECKPUSH_ERROR"+ (msg.obj != null?(String) msg.obj:""));
					dismissProgressDlg();
					break;
				case GlobalConstants.LOGIN_SAVEACCOUNT_REGISTEPUSH_ERROR://此message需要判断是否需要删除刚添加的账户
					if (Debug.LOGTOAST&&msg.obj != null) {
					    Log.e(TAG, "LOGIN_SAVEACCOUNT_REGISTEPUSH_ERROR " + (String) msg.obj);
					} else {
//						MailToast.makeText("LOGIN_SAVEACCOUNT_REGISTEPUSH_ERROR", Toast.LENGTH_SHORT).show();
					}
					C35AppServiceUtil.writeSubscribeInformationToSdcard("LOGIN_SAVEACCOUNT_REGISTEPUSH_ERROR"+ (msg.obj != null?(String) msg.obj:""));
					dismissProgressDlg();
					break;
				case GlobalConstants.LOGIN_OTHERS_ERROR://此message需要判断是否需要删除刚添加的账户
					if (Debug.LOGTOAST&&msg.obj != null) {
					    Log.e(TAG, "LOGIN_OTHERS_ERROR " + (String) msg.obj);
					} else {
//						MailToast.makeText("LOGIN_OTHERS_ERROR", Toast.LENGTH_SHORT).show();
					}
					Log.e(TAG, context.getString(R.string.error_code_login_no));
					C35AppServiceUtil.writeSubscribeInformationToSdcard("LOGIN_OTHERS_ERROR "+ (msg.obj != null?(String) msg.obj:""));
					dismissProgressDlg();
					break;
				case GlobalConstants.CHECK_INTERNET_ERROR:
				    Log.e(TAG, context.getString(R.string.show_no_connection_activity_caption));
					dismissProgressDlg();
					break;
				case GlobalConstants.CHECK_VERSION_ON:
				    Log.e(TAG, context.getString(R.string.error_code_version_no));
					dismissProgressDlg();
					break;
				case GlobalConstants.ERROR_EMAIL:
				    Log.e(TAG, context.getString(R.string.login_35Account_account_closed));
					dismissProgressDlg();
					break;
				case GlobalConstants.VALIDATE_ERROR_PASSWORD:
				    Log.e(TAG, context.getString(R.string.login_error_password));
					dismissProgressDlg();
					break;
				case GlobalConstants.SHOW_EXTRA_ACCOUNT_DIALOG:
					dismissProgressDlg();
					mpDialog = ProgressDialog.show(context, null, context.getString(R.string.loading_account_mail) + email.toString() + "" + context.getString(R.string.please_wait), true);
					mpDialog.setCancelable(true);
					break;
					
				case GlobalConstants.DEL_ACCOUNT_SHOW:
					dismissProgressDlg();
					
					// Modified by LL
					/*
					if (context instanceof FolderList) {
						if (FolderList.mHandler != null) {
							FolderList.mHandler.sendEmptyMessage(MailboxHandler.ACTION_ADD_ACCOUNT);
						}
						Debug.i(TAG, "DEL_ACCOUNT_AFTER_ACTION is broadcast");
					}
					*/
					
					break;

				case GlobalConstants.DEL_ACCOUNT_ALL:
					EmailApplication.setCurrentAccount(null);
					dismissProgressDlg();
					break;
				case GlobalConstants.DEL_DEFAULT_ACCOUNT_SHOW_SET:
					dismissProgressDlg();
					
					// Modified by LL
					/*
					ActivityStackManager.getInstance().exitApp();
					MessageList.actionHandleMailbox(context, EmailApplication.MAILBOX_INBOX, EmailApplication.MAILBOX_INBOX);
					*/
					
					break;
				case GlobalConstants.DEL_NOT_DEFAULT_ACCOUNT_SHOW_SET:
					dismissProgressDlg();
					
					// Modified by LL
					/*
					if (FolderList.mHandler != null) {
						FolderList.mHandler.sendEmptyMessage(MailboxHandler.ACTION_ADD_ACCOUNT);
					}
					*/
					
					break;

				}
			
			// Modified by LL
			//}
		    
			super.handleMessage(msg);
		}
	}
	
	private void loginSuccessLogic() {
	    // Modified by LL
	    /*
		// 如果来自登录页，则进入MessageList，然后判断是否显示新手引导。否则是从FolderList来的
		if (context instanceof LoginActivity || context instanceof ExperienceRegPersionActivity || context instanceof ExperienceRegDomainSuccessActivity) {
			C35AccountManager.getInstance().changeDefaultAccount(account.getUuid());
			// Add by xulei
			PushMailWidget.forceUpdate();
			// End by xulei
			Debug.i(strTag, account.isPushOpen() + "has login push is");
			StoreDirectory.showMessage();

			MessageList.actionHandleMailbox(context,EmailApplication.MAILBOX_INBOX, EmailApplication.MAILBOX_INBOX);
			((Activity) context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
			// AccountUtil.showRecommedDialog(context);// 此处废弃 2013.06.04
			((Activity) context).finish();

			// 同步企业联系人
			EntContactsLogic.getInstance().sync(account,false);
		} else if (context instanceof FolderList) {
			FolderList.mHandler.sendEmptyMessage(MailboxHandler.REFRESH_FOLDERLIST);
			// 若设置为默认账户则设置默认账户并跳转到邮件列表，否则停留在FolderList，并发送刷新界面的消息
			if (FolderList.mHandler != null) {
				FolderList.mHandler.sendEmptyMessage(MailboxHandler.ACTION_ADD_ACCOUNT);
			}
			if (bSetDefaultAccount) {
				C35AccountManager.getInstance().changeDefaultAccount(account.getUuid());
				MessageList.actionHandleMailbox(context, EmailApplication.MAILBOX_INBOX, EmailApplication.MAILBOX_INBOX);
				((Activity) context).overridePendingTransition(R.anim.right_in, R.anim.left_out);

			} else {
				// 发送广播，刷新FolderList界面
//				Intent i = new Intent();
//				i.setAction(FolderList.ACTION_UPDATE_FOLDERLISTACTIVITY);
//				context.sendBroadcast(i);
				
			}
		} else if (context instanceof SettingAccount) {
			if (bSetDefaultAccount) {
				C35AccountManager.getInstance().changeDefaultAccount(account.getUuid());
				ActivityStackManager.getInstance().exitApp();
				MessageList.actionHandleMailbox(context,EmailApplication.MAILBOX_INBOX, EmailApplication.MAILBOX_INBOX);
			} else {
//				Intent i = new Intent();
//				i.setAction(SettingAccount.ACTION_ADD_ACCOUNT);
//				context.sendBroadcast(i);
				if (SettingAccount.mHandler != null) {
					SettingAccount.mHandler.sendEmptyMessage(SetAccountHandler.ACTION_ADD_ACCOUNT);
				}
			}
		}
		*/
	}

	private void dismissProgressDlg() {
		if (mpDialog != null && mpDialog.isShowing()) {
			mpDialog.dismiss();
			mpDialog=null;
		}
		if (mpDelDialog != null && mpDelDialog.isShowing()) {
			mpDelDialog.dismiss();
			mpDelDialog=null;
		}
	}

	public void sendValidateMsg(Message m) {
		if (myMessageHandler != null) {
			myMessageHandler.sendMessage(m);
		}
	}
}
