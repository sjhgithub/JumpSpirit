package com.c35.mtd.pushmail.util;

import java.io.File;
import java.util.List;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.GlobalConstants;
import com.c35.mtd.pushmail.R;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.logic.C35AccountManager;
import com.c35.mtd.pushmail.store.LocalStore;
import com.c35.mtd.pushmail.store.Store;

/**
 * Store目录工具类
 * @author:gongfacun
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class StoreDirectory {

	private static final String TAG = "StoreDirectory";
	private static final File MOVINAND_STORAGE_DIRECTORY = getDirectory("MOVINAND_STORAGE", "/movinand");
	private static final File SDCARD_STORAGE_DIRECTORY = getDirectory("SDCARD_STORAGE", "/sdcard");
	private static final long availableSize = 2 * 1024 * 1024;
	private static final long myavailableSize = 10 * 1024 * 1024;
	public static final int STORAGE_TYPE_MOVINAND = 0;
	public static final int STORAGE_TYPE_SDCARD = 1;
	public static final int STORAGE_TYPE_MOVICARD = 2;
	public static final int STORAGE_TYPE_NULL = 3;
	public static int storeageType;

	private static File getDirectory(String variableName, String defaultPath) {
		String path = System.getenv(variableName);
		return path == null ? new File(defaultPath) : new File(path);
	}

	public static String getBootPath() {
		return android.os.Environment.getRootDirectory().getPath();
	}

	public static String getDataPath() {
		return android.os.Environment.getDataDirectory().getPath();
	}

	public static String getDownloadPath() {
		return android.os.Environment.getDownloadCacheDirectory().getPath();
	}

	/**
	 * 获取SdCard路径
	 */
	public static String getExternalStoragePath() {
		String state = android.os.Environment.getExternalStorageState();
		if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
			if (android.os.Environment.getExternalStorageDirectory().canWrite()) {
				return android.os.Environment.getExternalStorageDirectory().getPath();
			}
		}
		return null;
	}

	public static File getMoviNandStorageDirectory() {
		return MOVINAND_STORAGE_DIRECTORY;
	}

	/**
	 * 获取MoviNand路径
	 */
	public static String getMoviNandStoragePath() {
		File file = getMoviNandStorageDirectory();
		if (file.exists() && file.canWrite()) {
			return file.getPath();
		}
		return null;
	}

	public static int getStoreageType() {
		return storeageType;
	}

	public static void setStoreageType(int storeageType) {
		StoreDirectory.storeageType = storeageType;
	}

	/**
	 * 自动获取存储路径，
	 * 优先级为MOVINAND > SDCARD > MOVICARD
	 * @return uri
	 * @see: 
	 * @since: 
	 * @author: cuiwei
	 * @date:2013-12-2
	 */
	public static String getDefaultLocalStoreUri() {
		String uri = "local://localhost/";
		try {
			switch (getStoreageType()) {
			case STORAGE_TYPE_MOVINAND:
				uri += getMoviNandStoragePath() + GlobalConstants.MAIL_DIRECTORY + GlobalConstants.DATABASE_DIRECTORY + GlobalConstants.DATABASE_NAME;
				break;
			case STORAGE_TYPE_SDCARD:
				uri += getExternalStoragePath() + GlobalConstants.MAIL_DIRECTORY + GlobalConstants.DATABASE_DIRECTORY + GlobalConstants.DATABASE_NAME;
				break;
			case STORAGE_TYPE_MOVICARD:
				uri += EmailApplication.getInstance().getDatabasePath(GlobalConstants.DATABASE_NAME);
				break;
			case STORAGE_TYPE_NULL:
				uri += EmailApplication.getInstance().getDatabasePath(GlobalConstants.DATABASE_NAME);
				break;
			}
		} catch (Exception e) {// 4350 java.lang.NullPointerException
			Debug.e(TAG, "failfast_AA", e);
			uri += EmailApplication.getInstance().getDatabasePath(GlobalConstants.DATABASE_NAME);
		}
		return uri;
	}


	/**
	 * 获取存储卡的剩余容量，单位为字节
	 * 
	 * @param filePath
	 * @return availableSpare
	 */
	public static long getAvailableStore(String filePath) {
		long availableSpare = 0;
		try {
			// 取得sdcard文件路径
			StatFs statFs = new StatFs(filePath);
			// 获取block的SIZE
			long blocSize = statFs.getBlockSize();
			// 获取BLOCK数量
			long totalBlocks = statFs.getBlockCount();
			// 可使用的Block的数量
			long availaBlock = statFs.getAvailableBlocks();
			long total = totalBlocks * blocSize;
			availableSpare = availaBlock * blocSize;
		} catch (Exception e) {// IllegalArgumentException
			Debug.e("failfast", "failfast_AA", e);
		}
		return availableSpare;

	}

	/**
	 * 判断当前邮箱存储的位置（手机内存储或者SD卡）的可用空间是否够用 add by liujie
	 * 
	 * @return 可用空间小于10M时返回 true 可用空间大于10M时返回 false
	 */
	public static boolean isNoAvailableStore() {
		boolean result = false;// 初始化返回值
		if (EmailApplication.getCurrentAccount() != null) {
			int type = EmailApplication.getCurrentAccount().getLSUriType();
			Debug.d(TAG, "isNoAvailableStore()->type:" + type);
			if (type == StoreDirectory.STORAGE_TYPE_SDCARD) {
				if (getAvailableStore(getExternalStoragePath()) < myavailableSize) {
					Debug.i(TAG, "getAvailableStore(getExternalStoragePath()) = " + getAvailableStore(getExternalStoragePath()));
					result = true;
				}
			} else if (type == StoreDirectory.STORAGE_TYPE_MOVICARD) {
				if (getAvailableStore(EmailApplication.getInstance().getFilesDir().getAbsolutePath()) < myavailableSize) {
					Debug.i(TAG, "getAvailableStore(Email.getInstance().getFilesDir().getAbsolutePath()) = " + getAvailableStore(EmailApplication.getInstance().getFilesDir().getAbsolutePath()));
					result = true;
				}
			}
		}
		return result;
	}

	/**
	 * 判断当前是否存在SD卡且可用空间是否够用
	 * 
	 * @author 温楠
	 * 
	 * @return 存在SD卡且可用空间大于10M时返回 true
	 */
	public static boolean isSDCardExistandAvailable() {
		boolean result = false;// 初始化返回值
		String path = getExternalStoragePath();
		if (!TextUtils.isEmpty(path) && getAvailableStore(path) >= myavailableSize) {
			Debug.i(TAG, "getAvailableStore(getExternalStoragePath()) = " + getAvailableStore(getExternalStoragePath()));
			result = true;
		}
		return result;
	}

	/**
	 * 当前存储空间不足时需要弹出的提示
	 * 
	 * @author liujie
	 * @return Dialog
	 */
	public static Dialog showNoAvailableStoreDialog(final Context context) {
		String currentLSUri = "";
		int LSUriType = EmailApplication.getCurrentAccount().getLSUriType();
		Debug.i(TAG, "LSUriType is = " + LSUriType);
		if (LSUriType == StoreDirectory.STORAGE_TYPE_MOVICARD) {
			currentLSUri = EmailApplication.getInstance().getString(R.string.show_no_available_movicard);
		} else if (StoreDirectory.STORAGE_TYPE_SDCARD == LSUriType) {
			currentLSUri = EmailApplication.getInstance().getString(R.string.show_no_available_sdcard);
		}
		
		/*
		return new MailDialog.Builder(context).setTitle(EmailApplication.getInstance().getString(R.string.show_no_available_current) + " " + currentLSUri + " " + context.getString(R.string.show_no_available)).setMessage(context.getString(R.string.show_no_available_part_1) + " " + currentLSUri + " " + context.getString(R.string.show_no_available_part_2)).setPositiveButton(context.getString(R.string.show_no_available_exit35mail), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				// 退出应用
				int sdk_Version = android.os.Build.VERSION.SDK_INT;
				dialog.dismiss();
				if (sdk_Version >= 8) {
					// 2.2
					Intent startMain = new Intent(Intent.ACTION_MAIN);
					startMain.addCategory(Intent.CATEGORY_HOME);
					startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(startMain);
					System.exit(0);// 退出程序
				} else if (sdk_Version < 8) {
					// 2.2之前版本
					ActivityManager activityManager = (ActivityManager) EmailApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
					activityManager.restartPackage("com.c35.mtd.pushmail");
				}
			}
		}).setNegativeButton(EmailApplication.getInstance().getString(R.string.cancel_action), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).create();
		*/
		// Modified by LL
		// BEGIN
		Log.e(TAG, context.getString(R.string.show_no_available_current) + " " + currentLSUri + " " + context.getString(R.string.show_no_available));
		return null;
		// END
	}

	/**
	 * 判断邮箱要存储在哪里, 卡里的剩余容量如果低于2M就不存储。
	 * 
	 * @param context
	 * @param uuid
	 * @return storeUri
	 */
	public static String getStoreageUri() {
		boolean proc = false;
		if (getMoviNandStoragePath() != null) {
			long moviNandSize = getAvailableStore(getMoviNandStoragePath());
			// Debug.d(TAG, "moviNandSize:" + String.valueOf(moviNandSize));
			if (moviNandSize > availableSize) {
				setStoreageType(STORAGE_TYPE_MOVINAND);
				proc = true;
			}
		}
		if (!proc && getExternalStoragePath() != null) {
			long sdCardSize = getAvailableStore(getExternalStoragePath());
			// Debug.d(TAG, "sdCardSize:" + String.valueOf(sdCardSize));
			if (sdCardSize > availableSize) {
				setStoreageType(STORAGE_TYPE_SDCARD);
				proc = true;
			}
		}
		if (!proc) {
			setStoreageType(STORAGE_TYPE_MOVICARD);
		}
		String localStoreUri = getDefaultLocalStoreUri();
		Debug.d(TAG, "localStoreUri:" + localStoreUri + "  type: " + getStoreageType());
		return localStoreUri;
	}

	/**
	 * 查检SDCard是否拔出
	 * 
	 * @return result: true:没有账户的邮箱数据存储在SDCard上; false:有账户的邮箱数据存储在SDCard上，但此时SD卡不可用
	 */
	public static boolean checkSdCard() {
		boolean result = true;
		String state = android.os.Environment.getExternalStorageState();
		// if (android.os.Environment.MEDIA_UNMOUNTED.equals(state)
		// || android.os.Environment.MEDIA_BAD_REMOVAL.equals(state)
		// || android.os.Environment.MEDIA_REMOVED.equals(state)) {
		if (!android.os.Environment.MEDIA_MOUNTED.equals(state)) {
			// 将所有的账户提取出来
			List<Account> accounts = C35AccountManager.getInstance().getAccountsFromSP();
			for (Account sdCardAccount : accounts) {
				int type = sdCardAccount.getLSUriType();
				// Debug.d(TAG, "onStart()->type:" + type);
				if (type == StoreDirectory.STORAGE_TYPE_SDCARD) {
					result = false;
				}
			}
		}
		// Debug.d(TAG, "result:" + result);
		return result;
	}

	/**
	 * add by liujie 查检SDCard是否存在
	 * 
	 * @return result: true:当前账户的邮箱存在在手机，同时sd卡也存在; false:当前账户的邮箱数据没有存储在手机上，或者sd卡不存在。
	 */
	public static boolean checkMOVICARD() {
		boolean result = false;
		String state = android.os.Environment.getExternalStorageState();
		if (android.os.Environment.MEDIA_MOUNTED.equals(state) && getAvailableStore(getExternalStoragePath()) >= myavailableSize) {
			// 将所有的账户提取出来
			Account account = EmailApplication.getCurrentAccount();
			if (account != null) {
				int type = account.getLSUriType();
				// Debug.d(TAG, "onStart()->type:" + type);
				if (type == StoreDirectory.STORAGE_TYPE_MOVICARD) {
					result = true;
				}
			}
		}
		// Debug.d(TAG, "result:" + result);
		return result;
	}

	/**
	 * 将存储存在SdCard里面的数据库改为存储在MoviNaNd里或者手机里
	 * 
	 * @param context
	 */
	public static void changeSdCard2Other(Context context) {
		List<Account> accounts = C35AccountManager.getInstance().getAccountsFromSP();
		for (Account account : accounts) {
			int type = account.getLSUriType();
			Debug.d(TAG, "changeSdCard2Other()->type:" + type);
			if (type == StoreDirectory.STORAGE_TYPE_SDCARD) {
				String localStoreUri = StoreDirectory.getStoreageUri();
				int storeType = StoreDirectory.getStoreageType();
				Debug.i(TAG, "storeType is " + storeType);
				account.setLSUriType(storeType);
				account.setLocalStoreUri(localStoreUri);
				account.save(C35AccountManager.getInstance(), false);
			}
			try {
				C35MailMessageUtil.sendMailMessageBroadcast(context, (LocalStore) Store.getInstance(account.getLocalStoreUri()), false);// new
			} catch (MessagingException e) {
				e.printStackTrace();
				Debug.e(TAG, "failfast_AA", e);
			}
		}
		Store.clearStore();
		C35AccountManager.getInstance().syncAccounts();
		EmailApplication.setServicesEnabled(context);
		
		// Modified by LL
		//PushMailWidget.forceUpdate();

	}

	/**
	 * add by liujie account的邮箱存储路径改为存储在Sd卡里
	 * 
	 * @param context
	 */
	public static void changeMOVICARD2SdCard(Context context) {
		// account的邮箱存储路径改为存储在Sd卡里
		List<Account> accounts = C35AccountManager.getInstance().getAccountsFromSP();
		for (Account account : accounts) {
			int type = account.getLSUriType();
			Debug.d(TAG, "changeMOVICARD2SdCard()->type:" + type);
			if (type == StoreDirectory.STORAGE_TYPE_MOVICARD) {
				String localStoreUri = StoreDirectory.getStoreageUri();
				int storeType = StoreDirectory.getStoreageType();
				Debug.i(TAG, "storeType is " + storeType + "storeURL" + localStoreUri);
				account.setLSUriType(storeType);
				account.setLocalStoreUri(localStoreUri);
				account.save(C35AccountManager.getInstance(), false);
				Debug.i(TAG, "account is " + account.getLSUriType() + "===" + account.getLocalStoreUri());
			}
			try {
				C35MailMessageUtil.sendMailMessageBroadcast(context, (LocalStore) Store.getInstance(account.getLocalStoreUri()), false);
			} catch (MessagingException e) {
				e.printStackTrace();
				Debug.e(TAG, "failfast_AA", e);
			}
		}
		Store.clearStore();
		C35AccountManager.getInstance().syncAccounts();
		EmailApplication.setServicesEnabled(context);
		
		// Modified by LL
		//PushMailWidget.forceUpdate();
	}

	/**
	 * 显示消息
	 */
	class MyHandler extends Handler {

		private static final int NOTIFY_DATA = 1;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case NOTIFY_DATA:
				// mAdapter.notifyDataSetChanged();
				// showGotoPagePanel(false);
				break;
			}
			super.handleMessage(msg);
		}

		void notifyDataChanged() {
			Message message = new Message();
			message.arg1 = NOTIFY_DATA;
			sendMessage(message);
		}
	}

	/**
	 * 显示消息
	 * 
	 */
	public static void showMessage() {
		String message = null;
		switch (getStoreageType()) {
		// case STORAGE_TYPE_MOVINAND:
		// message = EmailApplication.getInstance().getString(R.string.message_movinand);
		// break;
		case STORAGE_TYPE_SDCARD:
			message = EmailApplication.getInstance().getString(R.string.message_sdcard);
			break;
		case STORAGE_TYPE_MOVICARD:
			message = EmailApplication.getInstance().getString(R.string.message_movicard);
			break;
		case STORAGE_TYPE_NULL:
			message = EmailApplication.getInstance().getString(R.string.message_null);
			break;
		}
		if (message != null) {
		    Log.e(TAG, message);
		}
	}
}
