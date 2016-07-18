package com.c35.mtd.pushmail.provider;

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.store.LocalStore;
import com.c35.mtd.pushmail.store.Store;

/**
 * 提供邮件的总数及未读数
 * 
 * @Description:
 * @author: zhuanggy
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class MailMessageProvider extends ContentProvider {

	private LocalStore localStore = null;
	private static final String TAG = "MailMessageProvider";
	public static final Uri MAILMESSAGE = Uri.parse("content://com.c35.mtd.pushmail.mailmessageprovider");
	private String strToalCount = "";// 邮件总数
	private String strUnreadCount = "";// 邮件未读数

	public static class MailMessageProviderColumns {

		public static final String TOTAL_COUNT = "total";
		public static final String NEW_COUNT = "count";
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		Debug.d(TAG, "onCreate() go !!!");
		return true;
	}

	/**
	 * 得到的Cursor包括全部邮件数和未读邮件数
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		if (projection == null) {
			projection = new String[] { MailMessageProviderColumns.TOTAL_COUNT, MailMessageProviderColumns.NEW_COUNT, };
		}
		Debug.d(TAG, "uri = " + uri);
		Debug.d(TAG, "uri = " + MAILMESSAGE.toString());
		if (uri.toString().equals(MAILMESSAGE.toString())) {
			if (EmailApplication.getCurrentAccount() != null) {
				Debug.d(TAG, "query  mAccount.getEmail = " + EmailApplication.getCurrentAccount().getEmail());
			} else {
				Debug.d(TAG, "query  mAccount is null ");
				return null;
			}
			try {
				localStore = (LocalStore) Store.getInstance(EmailApplication.getCurrentAccount().getLocalStoreUri().toString());
			} catch (MessagingException e) {
				Debug.e("failfast", "failfast_AA", e);
			}
			Debug.d(TAG, MAILMESSAGE.toString());

			strToalCount = localStore.getFolderMailsCountLocal(EmailApplication.getCurrentAccount(),EmailApplication.MAILBOX_INBOX) + "";
			strUnreadCount = localStore.getInboxUnreadCount(EmailApplication.getCurrentAccount()) + "";
			MatrixCursor ret = new MatrixCursor(projection);
			Object[] values = new Object[projection.length];
			for (int i = 0, count = projection.length; i < count; i++) {
				String column = projection[i];
				if (MailMessageProviderColumns.TOTAL_COUNT.equals(column)) {
					values[i] = strToalCount;
				} else if (MailMessageProviderColumns.NEW_COUNT.equals(column)) {
					values[i] = count;
				}
			}
			ret.addRow(new Object[] { strToalCount, strUnreadCount });
			// if (cursor != null) {
			// cursor.close();
			// }
			return ret;
		}
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
