package com.c35.mtd.pushmail.ent.utils;

import android.content.SharedPreferences;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.util.StringUtil;
import com.c35.ptc.as.util.C35OpenAesUtil;


/**
 * 
 * @Description: 存储PRM系统相关设置信息
 * @author:黄永兴(huangyx2@35.cn)
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-2-23
 */
public class EntSettingUtil {

	private static final String SHARED_NAME = "com.c35.ptc.pushmail.ent";
	
	private static SharedPreferences mShared = null;
	// rest url
	private static final String NAME_REST_URL = "rest_url_";
	// session key
	private static final String NAME_SESSION_KEY = "session_key_";
	// 是否已经同步的一个标志
	private static final String NAME_SYNCED = "name_synced_";
	
	private EntSettingUtil() {
		mShared = EmailApplication.getInstance().getSharedPreferences(SHARED_NAME, 0);
	}
	
	private static EntSettingUtil instance = new EntSettingUtil();
	
	public static EntSettingUtil getInstance(){
		return instance;
	}
	/**
	 * rest url 字段名
	 * @Description:
	 * @param account
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-21
	 */
	private String getRestUrlName(Account account){
		return NAME_REST_URL + account.getEmail();
	}
	/**
	 * 存储rest url
	 * @Description:
	 * @param url
	 * @param account
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-21
	 */
	public void setRestUrl(String url, Account account){
		putString(getRestUrlName(account), url);
	}
	/**
	 * 获取rest url
	 * @Description:
	 * @param account
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-21
	 */
	public String getRestUrl(Account account){
		Debug.d("SyncEntProtocol", "-------" + getRestUrlName(account));
		return getString(getRestUrlName(account), null);
	}
	/**
	 * session key 字段名
	 * @Description:
	 * @param account
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-21
	 */
	public String getSessionKeyName(Account account){
		return NAME_SESSION_KEY + account.getEmail();
	}
	/**
	 * 存储session key
	 * @Description:
	 * @param sessionKey
	 * @param account
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-21
	 */
	public void setSessionKey(String sessionKey, Account account){
		putString(getSessionKeyName(account), C35OpenAesUtil.encrypt(sessionKey));
	}
	/**
	 * 获取session key
	 * @Description:
	 * @param account
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-21
	 */
	public String getSessionKey(Account account){
		return C35OpenAesUtil.decrypt(getString(getSessionKeyName(account), null));
	}
	
	private String getSyncedName(Account account){
		return NAME_SYNCED + StringUtil.getAccountSuffix(account);
	}
	/**
	 * 是否已经同步过企业联系人(只有成功同步过才为true)
	 * @Description:
	 * @param account
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-23
	 */
	public boolean isSynced(Account account){
		return getBoolean(getSyncedName(account), false);
	}
	/**
	 * 设置为已成功同步过(只有成功同步才可以调用)
	 * @Description:
	 * @param account
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-23
	 */
	public void setSynced(Account account){
		putBoolean(getSyncedName(account), true);
	}
	
	/**
	 * 清除
	 * @Description:
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-4-18
	 */
	public void clear(){
		mShared.edit().clear().commit();
	}

	private void putString(String key, String value){
		mShared.edit().putString(key, value).commit();
	}

	private String getString(String key, String defValue){
		return mShared.getString(key, defValue);
	}

	private void putBoolean(String key, boolean value){
		mShared.edit().putBoolean(key, value).commit();
	}
	
	private boolean getBoolean(String key, boolean defValue){
		return mShared.getBoolean(key, defValue);
	}
}
