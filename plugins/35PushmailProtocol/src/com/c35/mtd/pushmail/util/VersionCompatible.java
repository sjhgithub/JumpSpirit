package com.c35.mtd.pushmail.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.provider.Contacts.People;
import android.provider.ContactsContract.Presence;
import android.util.Log;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;

/**
 * 版本比较
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class VersionCompatible {
	
	private static final String TAG = "VersionCompatible";

	final String CONTACT_PRESENCE = "contact_presence";
	final String[] PRESENCE_STATUS_PROJECTION = new String[] { "_id", CONTACT_PRESENCE };
	final static String AUTHORITY = "com.android.contacts";
	final static Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
	final Uri CONTENT_URI_METHORD = Uri.withAppendedPath(AUTHORITY_URI, "data");

	private static String getVersionSdk() {
		return VERSION.SDK;
	}

	public Cursor getCursorMethord(String email) {
		Debug.d("versionCompatible", "getCursorMethord");
		// if(versionSdk.equals("3") || versionSdk.equals("4")){
		// Debug.d("versionCompatible","version sdk 3");
		// return mContext.getContentResolver().query(
		// Uri.withAppendedPath(Contacts.ContactMethods.CONTENT_URI, "with_presence"),
		// MessageView.METHODS_WITH_PRESENCE_PROJECTION,
		// Contacts.ContactMethods.DATA + "=?",
		// new String[]{email },
		// null);
		// } else {//if(versionSdk.equals("5"))
		Debug.d("versionCompatible", "version sdk 5");
		return EmailApplication.getInstance().getContentResolver().query(CONTENT_URI_METHORD, PRESENCE_STATUS_PROJECTION, "data1=?", new String[] { email }, null);
		// }

	}

//	public int getResourceId(int status) {
//		Debug.d("versionCompatible", "getResourceId");
//		return Presence.getPresenceIconResourceId(status);
//	}

	/**
	 * 1.5 The content:// style URL for sub-directory of e-mail addresses.
	 */
	public static final Uri CONTENT_EMAIL_URI15 = Uri.parse("content://contacts/contact_methods/email");

	public static final Uri CONTENT_URI_DATA = Uri.withAppendedPath(AUTHORITY_URI, "data");
	public static final Uri CONTENT_URI_EMAIL = Uri.withAppendedPath(CONTENT_URI_DATA, "emails");
	public static final Uri CONTENT_FILTER_URI20 = Uri.withAppendedPath(CONTENT_URI_EMAIL, "filter");
	public static final Uri CONTENT_FILTER_OTHER = Uri.withAppendedPath(CONTENT_URI_DATA, "contacts");

	private static final String[] PROJECTION15 = { "_id", // 0
	"name", // 1
	"data" // 2
	};

	private static final String[] PROJECTION20 = { "_id", // 0
	"display_name", // 1
	"data1" // 2
	};

	public static Uri getContactUri(String filter) {
		String vsnSdk = getVersionSdk();
		if (vsnSdk.equals("3") || vsnSdk.equals("4")) {
			return CONTENT_EMAIL_URI15;
		} else {// if(versionSdk.equals("5"))
			Uri uri = null;
			if (Uri.encode(filter) == null) {
				/*
				 * 无奈啊，此处写死联系人email的数据库路径是因为我们的35phone是2.1系统，而我们编码是1.6环境，找不到
				 * ContactsContract.CommonDataKinds.Email.CONTENT_URI,故写死！
				 */
				uri = Uri.parse("content://com.android.contacts/data/emails");
			} else {
				uri = Uri.withAppendedPath(CONTENT_FILTER_URI20, Uri.encode(filter));
			}
			Debug.d(TAG, uri.toString());
			return uri;
		}

	}

	public static String[] getContactStr() {
		String vsnSdk = getVersionSdk();
		if (vsnSdk.equals("3") || vsnSdk.equals("4")) {
			return PROJECTION15;
		} else {// if(versionSdk.equals("5"))
			return PROJECTION20;
		}
	}

	public static String getContactQueryStr(String where) {
		String vsnSdk = getVersionSdk();
		if (vsnSdk.equals("3") || vsnSdk.equals("4")) {
			return where;
		} else {// if(versionSdk.equals("5"))
			return null;
		}
	}

//	public static String getData() {
//		String vsnSdk = getVersionSdk();
//		if (vsnSdk.equals("3") || vsnSdk.equals("4")) {
//			return "data";
//		} else {// if(versionSdk.equals("5"))
//			return "data1";
//		}
//	}

	private static final String SORT_ORDER20 = "times_contacted" + " DESC, " + "display_name";
	private static final String SORT_ORDER15 = People.TIMES_CONTACTED + " DESC, " + People.NAME;

	public static String getSortOrder() {
		String vsnSdk = getVersionSdk();
		if (vsnSdk.equals("3") || vsnSdk.equals("4")) {
			return SORT_ORDER15;
		} else {// if(versionSdk.equals("5"))
			return SORT_ORDER20;
		}
	}
}
