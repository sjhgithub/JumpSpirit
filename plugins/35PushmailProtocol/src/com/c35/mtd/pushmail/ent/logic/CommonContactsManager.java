package com.c35.mtd.pushmail.ent.logic;

import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.MessagingController;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.ent.bean.ContactAttribute;
import com.c35.mtd.pushmail.logic.AccountUtil;

/**
 * 常用（最近）联系人管理类
 * 
 * @Description:
 * @author:xulei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:May 30, 2013
 */
public class CommonContactsManager {

	private static CommonContactsManager instance = new CommonContactsManager();

	public static final long SYNC_INTERVAL = 7 * 24 * 3600 * 1000;//
	public static final int MAX_FETCH_COUNT = 10;//拉取常用联系人的数量

	private CommonContactsManager() {
	}

	public static CommonContactsManager getInstance() {
		return instance;
	}

	public List<ContactAttribute> getCommonContacts() {
		return getCommonContacts(EmailApplication.getCurrentAccount(), 2);
	}

	/**
	 * 获取常用联系人
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:May 30, 2013
	 */
	public List<ContactAttribute> getCommonContacts(Account account, int type) {
		// sync();
		return MessagingController.getInstance(EmailApplication.getInstance()).getCommonContacts(account, type);
	}

	public void sync() {
		if (canSync()) {
			new SyncContactsTask().execute();
		}
	}

	/**
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:May 30, 2013
	 */
	private boolean canSync() {
		ConnectivityManager manager = (ConnectivityManager) EmailApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info != null && AccountUtil.isSupportRequest("getContacts", EmailApplication.getCurrentAccount())) {
			// TODO: 同步间隔		Boolean result = AccountUtil.isSupportRequest("searchIdsByType", account);
			return true;
		}
		return false;
	}

	class SyncContactsTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// sync
			MessagingController controller = MessagingController.getInstance(EmailApplication.getInstance());
			controller.syncCommonContacts(EmailApplication.getCurrentAccount(), 2, MAX_FETCH_COUNT);
			return null;
		}
	}
}
