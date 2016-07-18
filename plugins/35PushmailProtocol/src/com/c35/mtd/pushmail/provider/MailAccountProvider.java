package com.c35.mtd.pushmail.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.GlobalConstants;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.logic.C35AccountManager;

/**
 * 提供Mail已登录的账户信息
 * 
 * @Description:
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-9-4
 */
public class MailAccountProvider extends ContentProvider {

	private static final String TABAL_NAME = "account";// 表名
	private SQLiteDatabase db;
	private static final String TAG = "MailAccountProvider";
	public static final Uri MAIL_ACCOUNT = Uri.parse("content://com.c35.ptc.mail.mailcontentprovider/get_user");// 提供Provider的URI

	/**
	 * 封装查询数据库的列名
	 * 
	 * @Description:
	 * @author: zhuanggy
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2012-11-2
	 */
	public static class MailAccountProviderColumns {

		public static final String _NAME = "name";
		public static final String PASSWORD = "password";
		public static final String DOMAIN = "domain";
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		db = this.getContext().openOrCreateDatabase(GlobalConstants.DATABASE_NAME_ACCOUNTS, 1, null);
		try {
			db.delete(TABAL_NAME, null, null);
			Debug.v(TAG, "delete!!!");
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			db.close();
		}

		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (!uri.toString().equals(MAIL_ACCOUNT.toString())) {
			Debug.v(TAG, "uri errer!");
			return null;
		}
		db = this.getContext().openOrCreateDatabase(GlobalConstants.DATABASE_NAME_ACCOUNTS, 1, null);
		try {
			db.insert(TABAL_NAME, "", values);
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			db.close();
		}

		Debug.v(TAG, "insert!!!" + values.getAsString("name"));
		return null;
	}

	@Override
	public boolean onCreate() {
		Debug.v(TAG, "onCreate() go !!!");
		db = this.getContext().openOrCreateDatabase(GlobalConstants.DATABASE_NAME_ACCOUNTS, 1, null);
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + "account" + " (" + "name" + " varchar(20) unique,password varchar(20),domain varchar(20));");
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			db.close();
		}
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// 返回账户信息
		Debug.v(TAG, "query!!");
		if (projection == null) {
			projection = new String[] { MailAccountProviderColumns._NAME, MailAccountProviderColumns.PASSWORD, MailAccountProviderColumns.DOMAIN };
		}
		if (!uri.toString().equals(MAIL_ACCOUNT.toString())) {
			Debug.v(TAG, "uri errer!");
			return null;
		}

		db = this.getContext().openOrCreateDatabase(GlobalConstants.DATABASE_NAME_ACCOUNTS, 1, null);
		Cursor cursor = null;
		Cursor cursorForReturn = null;
		try {
			cursor = db.query("account", null, null, null, null, null, null);
			cursorForReturn = changeCursor(cursor);
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			db.close();
		}

		return cursorForReturn;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * 将“name”和“password”格式的cursor转为“email”和“password”的Cursor供外部访问
	 * @Description:
	 * @param cur
	 * @return
	 * @see: 
	 * @since: 
	 * @author: zhuanggy
	 * @date:2012-11-2
	 */
	private Cursor changeCursor(Cursor cur) {
		String[] columnNames = { "email", "password" };
		MatrixCursor cursor = null;
		if (cur != null && cur.getCount() > 0) {
			try {
				cursor = new MatrixCursor(columnNames);
				cur.moveToFirst();
				do {
					Object[] obj = new Object[columnNames.length];

					obj[0] = cur.getString(cur.getColumnIndex("name"));
					obj[1] = cur.getString(cur.getColumnIndex("password"));

					cursor.addRow(obj);
				} while (cur.moveToNext());
			} catch (Exception e) {
				Debug.e("failfast", "failfast_AA", e);
			} finally {
				cur.close();
			}
		}
		return cursor;
	}

}
