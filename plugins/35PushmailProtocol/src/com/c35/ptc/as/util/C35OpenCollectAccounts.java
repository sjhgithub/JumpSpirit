package com.c35.ptc.as.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.c35.mtd.pushmail.Debug;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;


/**
 * 
 * @Description:各个应用获取账号或域名接口
 * @author:hanchunxue  
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2012-11-1
 */
public class C35OpenCollectAccounts {

	private static Context context;

	// mail provider 取账号地址
	private static final  Uri MAIL_ACCOUNT = Uri.parse("content://com.c35.ptc.mail.mailcontentprovider/get_user");
	// oa provider 取账号地址
	private static final Uri OA_ACCOUNT = Uri.parse("content://com.c35.ptc.oa.oacontentprovider/get_user");
	// eq provider 取账号地址
	private static final Uri EQ_ACCOUNT = Uri.parse("content://com.c35.ptc.eq.eqcontentprovider/get_user");
	// ewave provider 取账号地址
	private static final Uri EWAVE_ACCOUNT = Uri.parse("content://com.c35.ptc.ewave.ewavecontentprovider/get_user");
	// prm provider 取账号地址
	private static final Uri PRM_ACCOUNT = Uri.parse("content://com.c35.ptc.prm.prmcontentprovider/get_user");
	// emeetting provider 取账号地址
	private static final Uri EMEETTING_ACCOUNT = Uri.parse("content://com.c35.ptc.emeeting.emeetingcontentprovider/get_user");
	// mail 账号
	private static final String STR_MAIL_EMAIL_TAG = "email";
	// mail 密码
	private static final String STR_MAIL_PWD_TAG = "password";
	// oa账号
	private static final String STR_OA_EMAIL_TAG = "user_name";
	// oa密码
	private static final String STR_OA_PWD_TAG = "password";
	// ewave 账号
	private static final String STR_EWAVE_EMAIL_TAG = "user_name";
	// ewave 密码
	private static final String STR_EWAVE_PWD_TAG = "password";
	// eq 账号
	private static final String STR_EQ_EMAIL_TAG = "username";
	// eq 密码
	private static final String STR_EQ_PWD_TAG = "password";
	// prm 账号
	private static final String STR_PRM_EMAIL_TAG = "username";
	// prm 密码
	private static final String STR_PRM_PWD_TAG = "password";
	// emeetting 账号
	private static final String STR_EMEETTING_EMAIL_TAG = "username";
	// emeetting 密码
	private static final String STR_EMEETTING_PWD_TAG = "password";
    //存放账号集合
	private static List<HashMap<String, String>> listAllAccounts ;
	 //存放域名集合
	private static List<HashMap<String, String>> listRealmNames ;

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public static List<HashMap<String, String>> getAcconts() {
        listAllAccounts = new ArrayList<HashMap<String,String>>();
		contentResolverGetAccountsFromOthers(MAIL_ACCOUNT, STR_MAIL_EMAIL_TAG, STR_MAIL_PWD_TAG);
		contentResolverGetAccountsFromOthers(OA_ACCOUNT, STR_OA_EMAIL_TAG, STR_OA_PWD_TAG);
		contentResolverGetAccountsFromOthers(EQ_ACCOUNT, STR_EQ_EMAIL_TAG, STR_EQ_PWD_TAG);
		contentResolverGetAccountsFromOthers(EWAVE_ACCOUNT, STR_EWAVE_EMAIL_TAG, STR_EWAVE_PWD_TAG);
		contentResolverGetAccountsFromOthers(PRM_ACCOUNT, STR_PRM_EMAIL_TAG, STR_PRM_PWD_TAG);
		contentResolverGetAccountsFromOthers(EMEETTING_ACCOUNT, STR_EMEETTING_EMAIL_TAG, STR_EMEETTING_PWD_TAG);
		return listAllAccounts;

	}
	public static List<HashMap<String, String>> getRealmName() {
		listRealmNames = new ArrayList<HashMap<String,String>>();
		contentResolverGetRealmNameFromOthers(MAIL_ACCOUNT, STR_MAIL_EMAIL_TAG, STR_MAIL_PWD_TAG);
		contentResolverGetRealmNameFromOthers(OA_ACCOUNT, STR_OA_EMAIL_TAG, STR_OA_PWD_TAG);
		contentResolverGetRealmNameFromOthers(EQ_ACCOUNT, STR_EQ_EMAIL_TAG, STR_EQ_PWD_TAG);
		contentResolverGetRealmNameFromOthers(EWAVE_ACCOUNT, STR_EWAVE_EMAIL_TAG, STR_EWAVE_PWD_TAG);
		contentResolverGetRealmNameFromOthers(PRM_ACCOUNT, STR_PRM_EMAIL_TAG, STR_PRM_PWD_TAG);
		contentResolverGetRealmNameFromOthers(EMEETTING_ACCOUNT, STR_EMEETTING_EMAIL_TAG, STR_EMEETTING_PWD_TAG);
		return listRealmNames;
	}

	/**
	 * 
	 * @Description:获取账号域名
	 * @param uri
	 * @param emailTag
	 * @param pwdTag
	 * @see:
	 * @since:
	 * @author: hanchunxue
	 * @date:2012-11-1
	 */
	private static void contentResolverGetRealmNameFromOthers(Uri uri, String emailTag, String pwdTag) {
		HashMap<String, String> realmNameMap = new HashMap<String, String>();
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(uri, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					String accountrealmName = cursor.getString(cursor.getColumnIndex(emailTag));
					String realmName = accountrealmName.substring(accountrealmName.indexOf("@"));
					realmNameMap = new HashMap<String, String>();
					realmNameMap.put("realmName", realmName);
					if(!listRealmNames.contains(realmNameMap)){
						listRealmNames.add(realmNameMap);
					}
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

	}

	/**
	 * 
	 * @Description:获得其它应用的账户信息
	 * @param uri
	 * @param emailTag
	 * @param pwdTag
	 * @see:
	 * @since:
	 * @author: hanchunxue
	 * @date:2012-11-1
	 */
	private static void contentResolverGetAccountsFromOthers(Uri uri, String emailTag, String pwdTag) {
		HashMap<String, String> mapOneAccount = new HashMap<String, String>();
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(uri, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					// 得到email和加密的password; 使用decrypt()进行解密
					String userAddr = cursor.getString(cursor.getColumnIndex(emailTag));
					String password = C35OpenAesUtil.decrypt(cursor.getString(cursor.getColumnIndex(pwdTag)));
					Log.i("MainActivity", "ProviderGetEmail:" + userAddr);
					mapOneAccount = new HashMap<String, String>();
					mapOneAccount.put("email", userAddr);// 获得的邮箱地址china-channel
					mapOneAccount.put("pwd", password);
					if (!listAllAccounts.contains(mapOneAccount)) {
						listAllAccounts.add(mapOneAccount);
					}
				} while (cursor.moveToNext());

			} else {
				Log.e("MainActivity", "no account info from URI :" + uri.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
}
