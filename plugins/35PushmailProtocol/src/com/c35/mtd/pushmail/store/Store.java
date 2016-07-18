package com.c35.mtd.pushmail.store;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.logic.C35AccountManager;
import com.c35.mtd.pushmail.util.MailUtil;

/**
 * store是邮件存储的一个接入点，这个存储的位置可以是本地或者远程的，没有具体协议的限定。 
 *  Store想要优化JavaMail中两个类javax.mail.Store和javax.mail.
 *   Folder，同时增加一些
 *  附加功能来提高移动设备上的性能，这些功能的实现的着重点在尽量减少与网络连接的概率。
 * @author:liujie
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public abstract class Store extends SQLiteOpenHelper {

	public Store(String name, CursorFactory factory, int version) {
		super(EmailApplication.getInstance(), name, factory, version);
	}

	/**
	 * String constants for known store schemes. 协议常量
	 */
	public static final String STORE_SCHEME_IMAP = "imap";
	// public static final String STORE_SCHEME_POP3 = "pop3";
	public static final String STORE_SCHEME_LOCAL = "local";

	/**
	 * A global suggestion to Store implementors on how much of the body should be returned on
	 * FetchProfile.Item.BODY_SANE requests.
	 */
	public static final int FETCH_BODY_SANE_SUGGESTED_SIZE = (50 * 1024);

	private static HashMap<String, Store> mStores = new HashMap<String, Store>();

	/**
	 * 工厂方法，非单例的 new C35Store ，mStores.put
	 * @param uri  //c35proxy://pm3%4035.cn:qyyx12369A@wmail215.cn4e.com:5566
	 * @return
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-1-24
	 */
	public static Store getInstance(String uri) throws MessagingException {
		synchronized (Store.class) {
			Store store = null;
			if (uri != null) {// local://localhost//mnt/sdcard/com.c35.mtd.pushmail/database/35PushMail.db
				store = mStores.get(uri);
				if (store == null) {
					if (uri.startsWith(STORE_SCHEME_LOCAL)) {//local://localhost//mnt/sdcard/35.com/35mail/database/35PushMail.db
						store = new LocalStore(uri);
					} else {// if(uri.startsWith(STORE_SCHEME_C35PROXY)) {
						Debug.d("C35Store", "C35Store creat");
						store = new C35Store(uri);//c35proxy://pm3%4035.cn:qyyx12369A@wmail215.cn4e.com:5566
					}

					// if (store != null) {
					mStores.put(uri, store);// 存入hashmap
					// }
				}
			}
			if (store == null) {
				throw new MessagingException(MessagingException.STORE_GETINSTANCE_ERROR, "Unable to locate an applicable Store for " + uri);
			}

			return store;
		}
	}

	/**
	 * 
	 * @Description:清空store
	 * @see:
	 * @since:
	 * @author: hanlixia
	 * @date:2012-11-2
	 */
	public static void clearStore() {
		synchronized (Store.class) {
			for (String uri : mStores.keySet()) {
				if (uri.startsWith(MailUtil.STORE_SCHEME_C35PROXY)) {
					C35Store c35Store = (C35Store) mStores.get(uri);
					c35Store.closeSocket();
				} else if (uri.startsWith(STORE_SCHEME_LOCAL)) {
					LocalStore localStore = (LocalStore) mStores.get(uri);
					localStore.close();
				}
			}
			mStores.clear();
		}
	}

	public abstract Folder getFolder(Account account, String name) throws MessagingException;

	// public abstract Folder[] getPersonalNamespaces() throws MessagingException;

//	public abstract void checkSettings() throws MessagingException;

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	/**
	 * 
	 * 删除账户对应的storeuri和关闭连接
	 * @param storeUri
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:Aug 2, 2013
	 */
	public static void clearAccountStore(String storeUri) {
		synchronized (Store.class) {
			C35Store c35Store = (C35Store) mStores.get(storeUri);
			if (c35Store != null) {
				c35Store.closeSocket();
				mStores.remove(storeUri);
			}
		}
	}
}
