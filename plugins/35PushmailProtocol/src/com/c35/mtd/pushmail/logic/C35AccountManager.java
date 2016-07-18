package com.c35.mtd.pushmail.logic;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.c35.mtd.pushmail.C35MailThreadPool;
import com.c35.mtd.pushmail.C35MailThreadPool.ENUM_Thread_Level;
import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.store.LocalStore;
import com.c35.mtd.pushmail.store.Store;
import com.c35.mtd.pushmail.util.MailUtil;
import com.c35.mtd.pushmail.util.StoreDirectory;

/**
 * :提供账户管理相关API
 * @author:
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class C35AccountManager {

	private static final String TAG = C35AccountManager.class.getSimpleName();

	// public static final String DATABASE_NAME = "35PushMail";

	public static final String ACCOUNTS_UUIDS_KEY = "uuids";
	public static final String DEFAULT_ACCOUNT_UUID_KEY = "defaultUuid";
	public static final String APPLIACTION_UUID_KEY = "applicationUuidKey";
	public static final String ACCOUNTS_IDS_SEPARATOR = ",";
	// Account的账户存储xml文件名
	public static final String PREFERENCES_FILE_NAME = "35PushMail.Main";
	private Context mContext;
	public SharedPreferences mPreferences;

	private C35AccountManager() {

	}

	private static class SingletonHolder {

		public static final C35AccountManager INSTANCE = new C35AccountManager();
	}

	public static C35AccountManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void init(Context context) {
		mContext = context;
		mPreferences = mContext.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
	}

	// public Account createAccount(String email, String password) {
	// return createAccount(email, password, true);
	// }

	/**
	 * 创建账号
	 * @param email
	 * @param password
	 * @param save
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 6, 2012
	 */
	public Account createAccount(String email, String password) {
		Account account = null;
		try {
			account = new Account();
			account.setName(email.substring(0, email.indexOf('@')));
			account.setmEmailShow(email);
			account.setEmail(MailUtil.convert35CNToChinaChannel(email));
			account.setPassword(password);
//			account.setDescription(email);
			account.setNotifyNewMail(true);

		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		return account;
	}

	/**
	 * 添加新的账户到数据库，并创建文件夹
	 * 
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: CuiWei
	 * @throws MessagingException
	 * @date:2012-11-21
	 */
	public void insertAccountData(Account account) throws MessagingException {
		String localUri = StoreDirectory.getStoreageUri();
		LocalStore localStore;
		try {
			localStore = (LocalStore) Store.getInstance(localUri);
			localStore.addAccount(account);
			localStore.initFolders(account);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			Debug.e("failfast", "failfast_AA", e);
			throw e;
		}
	}

	/**
	 * 从SharedPreferences获取全部账号
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 12, 2012
	 */
	public List<Account> getAccountsFromSP() {
		List<String> uuids = getAccountsUuids();
		List<Account> accounts = new ArrayList<Account>();
		for (String uuid : uuids) {
			accounts.add(new Account(uuid));
		}
		return accounts;
	}

	/**
	 * 得到Account的账户存储xml文件名
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: CuiWei
	 * @date:2012-11-12
	 */
	public String getPrefPath() {
		String fileDir = mContext.getFilesDir().getAbsolutePath().trim();// /data/data/com.c35.mtd.pushmail/files
		final int idx = fileDir.lastIndexOf("/");// 31
		if (idx != -1) {
			fileDir = fileDir.substring(0, idx);// /data/data/com.c35.mtd.pushmail
		}
		// Debug.d(TAG, "fetchPrefPath()->fileDir = " + fileDir);
		StringBuffer sb = new StringBuffer();
		sb.append(fileDir);
		sb.append("/shared_prefs/");
		sb.append(PREFERENCES_FILE_NAME);
		sb.append(".xml");
		return sb.toString().trim();// /data/data/com.c35.mtd.pushmail/shared_prefs/35PushMail.Main.xml
	}

	/**
	 * 从SharedPreferences得到账户个数
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: CuiWei
	 * @date:2012-11-21
	 */
	public int getAccountsSize() {
		return getAccountsUuids().size();
	}

	/**
	 * 获取默认账号的id
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 12, 2012
	 */
	public String getDefaultAccountUuid() {
		return mPreferences.getString(DEFAULT_ACCOUNT_UUID_KEY, null);
	}

	/**
	 * 判断是否为软件升级
	 * @return
	 * @see:
	 * @since:
	 * @author: gongfacun
	 * @date:2010-12-12
	 */
	public boolean ifUpdateAPPlication() {
		try {
			int versionCode = mPreferences.getInt(APPLIACTION_UUID_KEY, 0);
			int currentVersionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), PackageManager.GET_CONFIGURATIONS).versionCode;
			if (versionCode == 0 || versionCode < currentVersionCode) {
				mPreferences.edit().putInt(APPLIACTION_UUID_KEY, currentVersionCode).commit();
				return true;
			} else {
				return false;
			}
		} catch (NameNotFoundException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		return true;
	}

	/**
	 * 获取默认账号的对象
	 * 
	 * @return
	 * @see:
	 * @since:
	 * @author: CuiWei
	 * @date:2012-11-21
	 */
	public Account getDefaultAccount() {
		return getAccount(getDefaultAccountUuid());
	}

	/**
	 * 从mPreferences根据account id 获取account
	 * @Description:
	 * @param accountId
	 *            2d967910-010f-49bb-83de-7137386967e4
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 12, 2012
	 */
	public Account getAccount(String accountUid) {
		synchronized (mPreferences) {
			Account account = null;
			if (accountUid != null) {
				for (String uid : getAccountsUuids()) {
					if (accountUid.equals(uid)) {
						account = new Account(accountUid);
						break;
					}
				}
			}
			return account;
		}
	}

	/**
	 * 从mPreferences得到Accounts的Uuid list
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: CuiWei
	 * @date:2012-11-9
	 */
	private List<String> getAccountsUuids() {
		List<String> uids = new ArrayList<String>();
		String idsString = mPreferences.getString(ACCOUNTS_UUIDS_KEY, null);
		if (idsString != null) {
			String idsStr[] = idsString.split(ACCOUNTS_IDS_SEPARATOR);
			for (String uid : idsStr) {
				uids.add(uid);
			}
		}
		return uids;
	}

	/**
	 * 切换账号
	 * @param accountUuid
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 6, 2012
	 */
	public boolean changeDefaultAccount(String accountUuid) {
		// TODO: finish this
		synchronized (mPreferences) {
			mPreferences.edit().putString(DEFAULT_ACCOUNT_UUID_KEY, accountUuid).commit();
			EmailApplication.setCurrentAccount(getDefaultAccount());//更新全局账户
		}
		return false;
	}

	/**
	 * 同步并合并账户
	 * @see:
	 * @since:
	 * @author: CuiWei
	 * @date:2012-11-21
	 */
	public void syncAccounts() {
		Debug.v("syncAccounts", "syncAccounts start!!");
		Debug.v("syncAccounts", "获取账户");
		List<Account> accountsInXML = getAccountsFromSP();
		List<Account> accountsTemp = getAccountsFromSP();
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(StoreDirectory.getStoreageUri());
			List<Account> accountsInDB = localStore.getAccountsFromDB();
			// Debug.v("syncAccounts", "账户取交集");
			// add new account
			accountsTemp.retainAll(accountsInDB);// 交集
			// Debug.v("syncAccounts", "将XML账户插入到DB");
			accountsInXML.removeAll(accountsTemp);
			// xml中有的，而DB中没有的，则插入账户到DB
			for (Account account : accountsInXML) {
				insertAccountData(account);
			}
			// Debug.v("syncAccounts", "从DB删除账户");
			// delete unused account
			// xml中没有，而DB中有的，则从DB中删除
			accountsInDB.removeAll(accountsTemp);
			for (Account account : accountsInDB) {
				localStore.deleteAccountByUuid(account.getUuid());
			}
			// Debug.v("syncAccounts", "更新账户");
			// update account
			// 更新当前所有账户(交集)
			for (Account account : accountsTemp) {
				localStore.updateAccount(account);// 更新账户的uuid和email
			}
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		Debug.v("syncAccounts", "syncAccounts end!!");
	}

	/**
	 * 从DB删除账号
	 * @param:
	 * @return:
	 * @throws:
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2012-7-3
	 */
	public boolean deleteAccount(Account account) {
		if (account != null) {
			account.delete(this);

			String localUri = StoreDirectory.getStoreageUri();
			try {
				LocalStore localStore = (LocalStore) Store.getInstance(localUri);
				localStore.deleteAccountByUuid(account.getUuid());
				Store.clearAccountStore(account.getStoreUri());
				return true;
			} catch (MessagingException e) {
				Debug.e("failfast", "failfast_AA", e);
			}
		}
		return false;
	}

	/**
	 * 删除数据库数据
	 * @param:
	 * @return:
	 * @throws:
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2012-8-21
	 */
	public boolean deleteDatabaseData() {
		try {
			String localUri = StoreDirectory.getStoreageUri();
			LocalStore localstore;
			localstore = (LocalStore) Store.getInstance(localUri);
			localstore.deletedatabasedata();
			return true;
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			Debug.e("failfast", "failfast_AA", e);
		}
		return false;
	}


	/**
	 * 删除账号
	 * @param:
	 * @return:
	 * @throws:
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2012-8-21
	 */
	public boolean destroyAccount(Account account) {
		if (account != null) {
			account.delete(this);
			String localUri = StoreDirectory.getStoreageUri();
			try {
				LocalStore localStore = (LocalStore) Store.getInstance(localUri);
				localStore.delete();
				Store.clearStore();
				return true;
			} catch (MessagingException e) {
				Debug.e("failfast", "failfast_AA", e);
			}
		}
		return false;
	}

	/**
	 * 筛选缺省用户
	 * @param:
	 * @return:
	 * @throws:
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2012-8-21
	 */
	public String choseUuidfordefault(String accountuuid) {
		String locauri = StoreDirectory.getStoreageUri();
		if (getDefaultAccountUuid().toString().equals(accountuuid)) {
			try {
				LocalStore localstore = (LocalStore) Store.getInstance(locauri);
				return localstore.showUuiddefault(accountuuid);
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				Debug.e("failfast", "failfast_AA", e);
			}
		}
		return getDefaultAccountUuid();
	}


	/**
	 * 用来刷新folder表中自定义文件夹
	 * @param account
	 * @see:
	 * @since:
	 * @author: gongfacun
	 * @date:2012-11-19
	 */
	public void refreshfolderlist(Account account) {
		final Account faccount = account;
		
		/*
		Debug.i(TAG, "ShowNoConnectionActivity.isConnectInternet(mContext)" + ShowNoConnectionActivity.isConnectInternet());
		
		FolderList.ifSync = false;
		if (ShowNoConnectionActivity.isConnectInternet()) {
			C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_AtOnce).submit(new Runnable() {

				@Override
				public void run() {

					String localUri = StoreDirectory.getStoreageUri();
					LocalStore localstore;
					try {
						localstore = (LocalStore) Store.getInstance(localUri);
						localstore.createselfFolder(faccount);
						FolderList.ifSync = true;
					} catch (MessagingException e) {
						// TODO Auto-generated catch block
						Debug.e("failfast", "failfast_AA", e);
					}

				}
			});
		}
        */
		// Modified by LL
		// BEGIN
        C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_AtOnce).submit(new Runnable() {

            @Override
            public void run() {

                String localUri = StoreDirectory.getStoreageUri();
                LocalStore localstore;
                try {
                    localstore = (LocalStore) Store.getInstance(localUri);
                    localstore.createselfFolder(faccount);
                } catch (MessagingException e) {
                    // TODO Auto-generated catch block
                    Debug.e("failfast", "failfast_AA", e);
                }

            }
        });
		// END
	}

}
