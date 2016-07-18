package com.c35.mtd.pushmail.store;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.GlobalConstants;
import com.c35.mtd.pushmail.util.AesUtil;
import com.c35.mtd.pushmail.util.MailUtil;

/**
 * 用于本地账户数据库管理，此数据库存储在手机内部存储中，供其它程序获得账户信息;
 * TODO，可以利用sd卡上那个账户表，感觉重复了，这里需要优化
 * @author:ZGY
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-8-31
 */
public class LocalStoreAccountsInfo {

	private static final String DB_TABLE = "account";// 表名
	private static final int DB_VERSION = 2;
	private static final String TAG = "LocalStoreAccountsInfo";

	// 表的各个字段
	public static final String KEY_NAME = "name";
	public static final String KEY_PWD = "password";
	public static final String KEY_PATH = "domain";


	private static final String DB_CREATE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE + " (" + KEY_NAME + " varchar(20) unique,password varchar(20),domain varchar(20));";
	private static final String MESSAGE_HISTORY_TABLE = "CREATE TABLE if not exists messages_history (id INTEGER PRIMARY KEY,account_id INTEGER,history_key TEXT)";

	private static SQLiteDatabase mSQLiteDatabase = null;
	private DatabaseHelper mDatabaseHelper = null;

	public LocalStoreAccountsInfo() {
	}

	public void open() throws SQLException {
		mDatabaseHelper = new DatabaseHelper();
		mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
	}

	public void close() {
		mDatabaseHelper.close();
	}

	/**
	 * 
	 * @Description:插入一条数据
	 * @param name
	 * @param pwd
	 * @param domain
	 * @return
	 * @see:
	 * @since:
	 * @author: zgy
	 * @date:2012-9-3
	 */
	public long insertData(String name, String pwd, String domain) {
		long lResult = -1;
		name = MailUtil.convertChinaChannelTo35CN(name);// china-channel转为35
		Cursor accountCursor = null;
		try {
			mSQLiteDatabase.beginTransaction();
			String qsql = "select * from " + DB_TABLE + " where " + KEY_NAME + " = ?";
			accountCursor = mSQLiteDatabase.rawQuery(qsql, new String[] { name });

			try {
				if (accountCursor == null || accountCursor.getCount() == 0) {
					pwd = AesUtil.encrypt(pwd);
					ContentValues initialValues = new ContentValues();
					initialValues.put(KEY_NAME, name);
					initialValues.put(KEY_PWD, pwd);
					initialValues.put(KEY_PATH, domain);
					Debug.i(TAG, "insert account data-> name = " + name);
					lResult = mSQLiteDatabase.insert(DB_TABLE, "", initialValues);// 第二个参数为空？参考MailAccountProvider的方法
				} else {
					Debug.i(TAG, "not insert account!");
				}
			} catch (Exception e) {
				Debug.e("failfast", "failfast_AA", e);
			} finally {
				if (accountCursor != null) {
					accountCursor.close();
				}
			}

			mSQLiteDatabase.setTransactionSuccessful();
			mSQLiteDatabase.endTransaction();
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}

		return lResult;
	}

	/**
	 * @Description:根据用户名删除一条数据
	 * 
	 * @param name
	 * @return
	 */
	public boolean deleteData(String name) {

		name = MailUtil.convertChinaChannelTo35CN(name);// china-channel转为35
		Debug.i(TAG, "delete account data-> name = " + name);
		try {
			mSQLiteDatabase.beginTransaction();
			mSQLiteDatabase.execSQL("delete from account where " + KEY_NAME + " = ?", new Object[] { name });
			mSQLiteDatabase.setTransactionSuccessful();
			mSQLiteDatabase.endTransaction();
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}

		return true;
	}

	/**
	 * @Description:向account_info.db数据库,messages_history表中插入搜索历史historyUids
	 * 
	 * @param accountId
	 *            用户id
	 * @param historyUids
	 *            搜索历史邮件historyUids
	 * @param history_key
	 *            搜索关键字
	 */
	public void insertSearchHistory(Long accountId, String history_key) {
		try {
			mSQLiteDatabase.beginTransaction();
			Cursor cursor = mSQLiteDatabase.rawQuery("SELECT id FROM messages_history WHERE account_id = ? AND history_key = ?", new String[] { String.valueOf(accountId), history_key });
			try {
				if (cursor == null || cursor.getCount() == 0) {
					mSQLiteDatabase.execSQL("INSERT INTO messages_history (account_id,history_key) VALUES(?,?)", new Object[] { accountId, history_key });
				}
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			mSQLiteDatabase.setTransactionSuccessful();
			mSQLiteDatabase.endTransaction();
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * @Description:删除messages_history表中记录。
	 */
	public void deleteSearchHistory(Long accountId) {
		try {
			mSQLiteDatabase.beginTransaction();
			mSQLiteDatabase.execSQL("DELETE FROM messages_history WHERE account_id = ?", new Object[] { accountId });
			mSQLiteDatabase.setTransactionSuccessful();
			mSQLiteDatabase.endTransaction();
			Debug.d(TAG, "数据库删除语句执行");
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 
	 * @Description:查询账号accountId下的搜索历史
	 * @param accountId
	 * @return
	 * @see:
	 * @since:
	 * @author: hanlixia
	 * @date:2012-11-2
	 */
	public List<String> selectSearchHistoryKeyword(Long accountId) {
		List<String> resultLists = null;
		try {
			mSQLiteDatabase.beginTransaction();
			Cursor cursor = mSQLiteDatabase.rawQuery("SELECT distinct history_key FROM messages_history WHERE account_id = ?", new String[] { String.valueOf(accountId) });
			try {
				if (cursor != null && cursor.getCount() != 0) {
					resultLists = new ArrayList<String>();
					while (cursor.moveToNext()) {
						resultLists.add(cursor.getString(cursor.getColumnIndex("history_key")));
					}
				}
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			mSQLiteDatabase.setTransactionSuccessful();
			mSQLiteDatabase.endTransaction();
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		Debug.d(TAG, "数据库查询结果keyword = :" + resultLists);
		return resultLists;
	}

	/**
	 * 数据库帮助类
	 * @author:zgy
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2012-9-3
	 */
	public static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper() {
			/* 当调用getWritableDatabase()或 getReadableDatabase()方法时 则创建一个数据库 */
			super(EmailApplication.getInstance(), GlobalConstants.DATABASE_NAME_ACCOUNTS, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			/* 数据库没有表时创建一个 */
			db.execSQL(DB_CREATE);
			db.execSQL(MESSAGE_HISTORY_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS account");
			db.execSQL("DROP TABLE IF EXISTS messages_history");
			onCreate(db);
		}

		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS account");
			db.execSQL("DROP TABLE IF EXISTS messages_history");
			onCreate(db);
		}

	}

}
