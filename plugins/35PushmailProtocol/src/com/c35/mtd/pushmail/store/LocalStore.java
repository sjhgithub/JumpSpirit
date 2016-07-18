package com.c35.mtd.pushmail.store;

import java.io.File;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.GlobalConstants;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.beans.C35Attachment;
import com.c35.mtd.pushmail.beans.C35CompressItem;
import com.c35.mtd.pushmail.beans.C35Folder;
import com.c35.mtd.pushmail.beans.C35Message;
import com.c35.mtd.pushmail.beans.Contact;
import com.c35.mtd.pushmail.beans.ErrorObj;
import com.c35.mtd.pushmail.beans.Label;
import com.c35.mtd.pushmail.beans.MailStatusObj;
import com.c35.mtd.pushmail.beans.OperationHistoryInfo;
import com.c35.mtd.pushmail.beans.StatusObj;
import com.c35.mtd.pushmail.command.response.GetFolderListResponse.GetFolderType;
import com.c35.mtd.pushmail.ent.bean.ContactAttribute;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.logic.AccountUtil;
import com.c35.mtd.pushmail.logic.C35AccountManager;
import com.c35.mtd.pushmail.logic.FlowDataUtil;
import com.c35.mtd.pushmail.provider.AttachmentProvider;
import com.c35.mtd.pushmail.util.Address;
import com.c35.mtd.pushmail.util.C35AppServiceUtil;
import com.c35.mtd.pushmail.util.FileUtil;
import com.c35.mtd.pushmail.util.HtmlContentUtil;
import com.c35.mtd.pushmail.util.MailUtil;
import com.c35.mtd.pushmail.util.NetworkUtil;
import com.c35.mtd.pushmail.util.StringUtil;
import com.c35.mtd.pushmail.util.Utility;

/**
 * A SQLiteOpenHelper
 * 
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-9-17
 */
public class LocalStore extends Store {

	private final String TAG = "LocalStore";
	private final String TAG_DBOPER = "DBOPER";
	/**
	 * History of database revisions. db version Shipped in Notes ---------- ---------- ----- 18 pre-1.0
	 * Development versions. No upgrade path. 18 1.0, 1.1 1.0 Release version. 19 - Added message_id column to
	 * messages table. 20 1.5 Added content_id column to attachments table.
	 */
	private static final int DB_VERSION = 4100;
	private String mPath;
	private SQLiteDatabase mDb;
	private File mAttachmentsDir;
	private static HashMap<String, Long> mUuidToIdMap = new HashMap<String, Long>();// uuid4690229c-cd33-32432,1;
	// private static HashMap<Long, String[]> mFolderGetUuid = new HashMap<Long, String[]>();//
	// accountID,uuid(1,1.ff087989;2,2.Sent;....)
	private static HashMap<Long, String> mFolderGetUuid = new HashMap<Long, String>();// (1,1.ff087989;2,2.Sent;....)
	private static HashMap<String, Long> mFolderGetId = new HashMap<String, Long>();// (accountid+1.ff087989,1;accountid+2.Sent,2;....)
	private static HashMap<String, Integer> mFolderAccountMap = new HashMap<String, Integer>();// accountId+folderId缓存
	private Object createFolderLock = new Object();
	private Object storeAttachmentLock = new Object();
	private Object getMDBLock = new Object();

	private final String SQL_C_TBL_INE = "CREATE TABLE if not exists ";
	private final String SQL_C_IDX_INE = "create index if not exists ";
	private final String SQL_I_INTO = "INSERT INTO ";
	private final String SQL_S_ALL_FROM = "select * from ";
	private final String SQL_DEL_FROM = "DELETE FROM ";

	private static final String TBL_FOLDERS = "folders";
	private static final String IDX_FOLDERS = "IDX_folders";
	private static final String TBL_MESSAGES = "messages";
	private static final String IDX_MESSAGES = "IDX_messages";
	private static final String TBL_QUERY_MAILADDRESS = "query_mailaddress";
	private static final String TBL_ATTACHMENTS = "attachments";
	private static final String TBL_ATTACHMENTS_LIST = "attachments_list";
	private static final String TBL_ACCOUNT = "account";
	private static final String IDX_ACCOUNT = "IDX_account";
	private static final String TBL_COMMON_CONTACT = "common_contact";
	//
	private StringBuffer mSBufferSQLTemp = new StringBuffer();

	/**
	 * LocalStore本地数据库
	 * 
	 * @param uri
	 *            local://localhost//mnt/sdcard/com.c35.mtd.pushmail/database/35 PushMail.db
	 */
	public LocalStore(String _uri) throws MessagingException {
		super(null, null, DB_VERSION);
		Debug.v(TAG_DBOPER, "LocalStore" + _uri);
		URI uri = null;
		try {
			uri = new URI(_uri);// local://localhost//mnt/sdcard/com.c35.mtd.pushmail/database/35PushMail.db
		} catch (Exception e) {
			throw new MessagingException("Invalid uri for LocalStore");
		}
		if (!uri.getScheme().equals("local")) {
			throw new MessagingException("Invalid scheme");
		}
		mPath = uri.getPath();// //mnt/sdcard/com.c35.mtd.pushmail/database/35PushMail.db

		try {
			// 删除老的数据库目录，改到35.com/35mail下面
			File oldPath = new File(GlobalConstants.OLD_MAIL_DIRECTORY);
			if (oldPath != null && oldPath.exists()) {
				File file = new File(GlobalConstants.APPCATION_SCARD_DIRECTORY);
				if (!file.exists()) {
					file.mkdirs();

				}
				file = new File(GlobalConstants.EGG_SHELL_PATH);
				FileUtil.copyFile(new File(GlobalConstants.OLD_MAIL_DIRECTORY + "/" + GlobalConstants.EGG_SHELL_NAME), file);
				FileUtil.deleteFile(GlobalConstants.OLD_MAIL_DIRECTORY);
			}
			if (getMDB().getVersion() != DB_VERSION) {
				delete();
				// deleteData();
				createDB();

			}
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		File parentDir = new File(mPath).getParentFile();// /mnt/sdcard/com.c35.mtd.pushmail/database
		if (!parentDir.exists()) {
			parentDir.mkdirs();
		}
		File dbFile = new File(mPath);// /mnt/sdcard/com.c35.mtd.pushmail/database/35PushMail.db
		if (dbFile.exists() && !dbFile.canWrite()) {
			Debug.d(TAG, "LocalStore()-> unable to open database file : " + mPath);
			throw new MessagingException("unable to open database file : " + mPath);
		}

		mAttachmentsDir = new File(mPath + "_att");// /mnt/sdcard/com.c35.mtd.pushmail/database/35PushMail.db_att
		if (!mAttachmentsDir.exists()) {
			mAttachmentsDir.mkdirs();
			Debug.i(TAG, "Constructor mAttachmentsDir AbsolutePath->" + mAttachmentsDir.getAbsolutePath());
		}
	}

	/**
	 * 创建数据库表，表增加或者删除时请记得维护 deleteData()
	 * 
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-9-17
	 */
	private void createDB() {
		try {
			Debug.v(TAG_DBOPER, "createDB");
			if (Config.LOGV) {
				Debug.v(EmailApplication.LOG_TAG, String.format("Upgrading database from %d to %d", getMDB().getVersion(), DB_VERSION));
			}
			Debug.i(TAG, "oldVersion < 7777------------------ Greate new table ...Mail2.1_ " + getMDB().getVersion() + " _DB_VERSION_ " + DB_VERSION);
			getMDB().beginTransaction();
			// getMDB().execSQL("DROP TABLE IF EXISTS folders");
			mSBufferSQLTemp.setLength(0);
			mSBufferSQLTemp.append(SQL_C_TBL_INE);
			mSBufferSQLTemp.append(TBL_FOLDERS);
			mSBufferSQLTemp.append(" (id INTEGER PRIMARY KEY,account_id INTEGER, name TEXT, ");
			mSBufferSQLTemp.append("last_updated INTEGER, unread_count INTEGER, visible_limit INTEGER,folderId TEXT,orderValue INTEGER,parentId TEXT,folderType INTEGER)");
			getMDB().execSQL(mSBufferSQLTemp.toString());

			mSBufferSQLTemp.setLength(0);
			mSBufferSQLTemp.append(SQL_C_IDX_INE);
			mSBufferSQLTemp.append(IDX_FOLDERS);
			mSBufferSQLTemp.append(" on ");
			mSBufferSQLTemp.append(TBL_FOLDERS);
			mSBufferSQLTemp.append("(folderId)");
			// getMDB().execSQL("create index if not exists IDX_folders on folders(folderId)");
			getMDB().execSQL(mSBufferSQLTemp.toString());

			mSBufferSQLTemp.setLength(0);
			mSBufferSQLTemp.append(SQL_C_TBL_INE);
			mSBufferSQLTemp.append(TBL_MESSAGES);
			mSBufferSQLTemp.append(" (id INTEGER PRIMARY KEY, folder_id INTEGER,account_id INTEGER, subject TEXT,uid TEXT ,date INTEGER,flags TEXT,internal_date INTEGER,");
			mSBufferSQLTemp.append(" sender_list TEXT,to_list TEXT, cc_list TEXT, bcc_list TEXT,reply_to_list TEXT,reader_list TEXT,");
			mSBufferSQLTemp.append(" attachment_count INTEGER,remail_id TEXT,send_type INTEGER,");
			mSBufferSQLTemp.append("preview TEXT,favorite INTEGER DEFAULT 0,down_flag INTEGER,");
			mSBufferSQLTemp.append("read_flag INTEGER,reader_count INTEGER DEFAULT 0,forward_replay_flag INTEGER DEFAULT 0,delete_flag INTEGER DEFAULT 0,send_flag INTEGER,mail_size INTEGER,is_send_me INTEGER DEFAULT 0,is_important_from INTEGER DEFAULT 0,priority INTEGER DEFAULT 3,calendarState INTEGER DEFAULT 0,calendarStartTime TEXT,calendarEndTime TEXT,calendarLocation TEXT,mailType INTEGER DEFAULT 0,compressedToSize long DEFAULT 0,size long default 0)");
			mSBufferSQLTemp.append("");
			mSBufferSQLTemp.append("");
			// getMDB().execSQL("CREATE TABLE if not exists messages" +
			// " (id INTEGER PRIMARY KEY, folder_id INTEGER,account_id INTEGER, subject TEXT,uid TEXT ,date INTEGER,flags TEXT,internal_date INTEGER,"
			// +
			// " sender_list TEXT,to_list TEXT, cc_list TEXT, bcc_list TEXT,reply_to_list TEXT,reader_list TEXT,"
			// + " attachment_count INTEGER,remail_id TEXT,send_type INTEGER,"
			// + "preview TEXT,favorite INTEGER DEFAULT 0,down_flag INTEGER,"
			// +
			// "read_flag INTEGER,reader_count INTEGER DEFAULT 0,forward_replay_flag INTEGER DEFAULT 0,delete_flag INTEGER DEFAULT 0,send_flag INTEGER,mail_size INTEGER,is_send_me INTEGER DEFAULT 0,is_important_from INTEGER DEFAULT 0,priority INTEGER DEFAULT 3,calendarState INTEGER DEFAULT 0,calendarStartTime TEXT,calendarEndTime TEXT,calendarLocation TEXT,mailType INTEGER DEFAULT 0,compressedToSize long DEFAULT 0,size long default 0)");
			getMDB().execSQL(mSBufferSQLTemp.toString());

			mSBufferSQLTemp.setLength(0);
			mSBufferSQLTemp.append(SQL_C_IDX_INE);
			mSBufferSQLTemp.append(IDX_MESSAGES);
			mSBufferSQLTemp.append(" on ");
			mSBufferSQLTemp.append(TBL_MESSAGES);
			mSBufferSQLTemp.append("(id,uid,account_id)");
			// getMDB().execSQL("create index if not exists IDX_messages on messages(id,uid,account_id)");
			getMDB().execSQL(mSBufferSQLTemp.toString());

			mSBufferSQLTemp.setLength(0);
			mSBufferSQLTemp.append(SQL_C_TBL_INE);
			mSBufferSQLTemp.append(TBL_QUERY_MAILADDRESS);
			mSBufferSQLTemp.append(" (account_id INTEGER, query_uid TEXT, ");
			mSBufferSQLTemp.append("mail_address TEXT, date INTEGER)");
			mSBufferSQLTemp.append("");
			mSBufferSQLTemp.append("");
			// getMDB().execSQL("CREATE TABLE if not exists query_mailaddress (account_id INTEGER, query_uid TEXT, "
			// + "mail_address TEXT, date INTEGER)");
			getMDB().execSQL(mSBufferSQLTemp.toString());

			mSBufferSQLTemp.setLength(0);
			mSBufferSQLTemp.append(SQL_C_TBL_INE);
			mSBufferSQLTemp.append(TBL_ATTACHMENTS);
			mSBufferSQLTemp.append(" (id INTEGER PRIMARY KEY,attachment_id TEXT, uid TEXT,message_id INTEGER, store_data TEXT, content_uri TEXT, size INTEGER, name TEXT,path TEXT,downState INTEGER,");
			mSBufferSQLTemp.append("mime_type TEXT, content_id TEXT, source_attachment_id TEXT, source_message_uid TEXT ,compressItemNum INTEGER, duration_time INTEGER)");
			mSBufferSQLTemp.append("");
			mSBufferSQLTemp.append("");
			// getMDB().execSQL("CREATE TABLE if not exists attachments (id INTEGER PRIMARY KEY,attachment_id TEXT, uid TEXT,message_id INTEGER, store_data TEXT, content_uri TEXT, size INTEGER, name TEXT,path TEXT,downState INTEGER,"
			// +
			// "mime_type TEXT, content_id TEXT, source_attachment_id TEXT, source_message_uid TEXT ,compressItemNum INTEGER, duration_time INTEGER)");
			getMDB().execSQL(mSBufferSQLTemp.toString());

			getMDB().execSQL("create index if not exists IDX_attachments on attachments(id,attachment_id,uid,message_id)");
			// getMDB().execSQL("DROP TABLE IF EXISTS pending_commands");
			getMDB().execSQL("CREATE TABLE if not exists pending_commands (id INTEGER PRIMARY KEY,account_id INTEGER,  command TEXT, arguments TEXT)");

			mSBufferSQLTemp.setLength(0);
			mSBufferSQLTemp.append(SQL_C_TBL_INE);
			mSBufferSQLTemp.append(TBL_ACCOUNT);
			mSBufferSQLTemp.append(" (id INTEGER PRIMARY KEY,uuid TEXT,login_time LONG,email TEXT, password TEXT, domain TEXT,emailshow TEXT)");
			mSBufferSQLTemp.append("");
			mSBufferSQLTemp.append("");
			// getMDB().execSQL("CREATE TABLE if not exists account (id INTEGER PRIMARY KEY,uuid TEXT,login_time LONG,email TEXT, password TEXT, domain TEXT,emailshow TEXT)");
			getMDB().execSQL(mSBufferSQLTemp.toString());

			mSBufferSQLTemp.setLength(0);
			mSBufferSQLTemp.append(SQL_C_IDX_INE);
			mSBufferSQLTemp.append(IDX_ACCOUNT);
			mSBufferSQLTemp.append(" on ");
			mSBufferSQLTemp.append(TBL_ACCOUNT);
			mSBufferSQLTemp.append("(id,uuid)");
			getMDB().execSQL(mSBufferSQLTemp.toString());
			// getMDB().execSQL("create index if not exists IDX_account on account(id,uuid)");

			getMDB().execSQL("CREATE TABLE if not exists body (id INTEGER PRIMARY KEY,message_id INTEGER,html_content TEXT,text_content TEXT);");
			getMDB().execSQL("create index if not exists IDX_body on body(id,message_id)");
			// getMDB().execSQL("UPDATE messages SET favorite = 0;");
			getMDB().execSQL("UPDATE messages SET subject ='' where subject is null;");
			getMDB().execSQL("UPDATE messages SET preview ='' where preview is null;");
			// getMDB().execSQL("DROP TABLE IF EXISTS label");
			getMDB().execSQL("CREATE TABLE if not exists label (id TEXT PRIMARY KEY,label_name TEXT,label_color TEXT);");
			// getMDB().execSQL("DROP TABLE IF EXISTS messages_label_staging");
			getMDB().execSQL("CREATE TABLE if not exists messages_label_staging(message_id TEXT,label_id TEXT);");
			getMDB().execSQL("CREATE TABLE if not exists compressitems (id INTEGER PRIMARY KEY,attach_id TEXT,fileName text,fileSize long,downState INTEGER);");

			getMDB().execSQL("CREATE TABLE if not exists operation_history (id INTEGER PRIMARY KEY,account_uid TEXT,folderid text,mailid text ,statusid INTEGER,statusvalue text,operatetime long,commitstatus INTEGER);");
			getMDB().execSQL("CREATE TABLE if not exists attachments_list (id INTEGER PRIMARY KEY, account_id INTEGER, attachment_id TEXT, message_uid TEXT , message_subject TEXT, fromwho TEXT, folder_id TEXT, attachment_name TEXT, download INTEGER, filesize LONG, sendtime LONG);");// 所有附件列表的表格
			getMDB().execSQL("CREATE TABLE if not exists flowdatatable (id integer primary key autoincrement,flow_date text not null ,RxBytes_start integer,TxBytes_start integer ,app_type text,RxBytes_daySum_wifi_orall integer,TxBytes_daySum_wifi_orall integer,RxBytes_daySum_Mobile integer,TxBytes_daySum_Mobile integer);");// 流量数据表

			getMDB().execSQL("CREATE TABLE if not exists  " + TBL_COMMON_CONTACT + " (id INTEGER primary key autoincrement, account_uuid TEXT, name TEXT, email TEXT, type INTEGER)");

			getMDB().setVersion(DB_VERSION);
			getMDB().setTransactionSuccessful();
			getMDB().endTransaction();
			// getMDB().setVersion(30);
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}

	}

	/**
	 * 关闭数据库
	 * 
	 * @see:
	 * @since:
	 * @author: hanlixia
	 * @date:2012-11-2
	 */
	public void close() {
		Debug.v(TAG_DBOPER, "close");
		try {
			if (mDb != null && mDb.isOpen())
				mDb.close();
			mDb = null;
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 获得数据库实例
	 * 
	 * @return
	 * @see:
	 * @since:
	 * @author: yuegl CuiWei
	 * @date:2012-11-20
	 */
	private SQLiteDatabase getMDB() {
		synchronized (getMDBLock) {
			if (mDb == null || !mDb.isOpen()) {
				Debug.i(TAG, "will create database file");

				File parentDir = new File(mPath).getParentFile();
				if (!parentDir.exists()) {
					parentDir.mkdirs();
				}
				try {
					mDb = SQLiteDatabase.openOrCreateDatabase(mPath, null);
				} catch (Exception e) {// SQLiteDatabaseLockedException
					Debug.e("failfast", "failfast_AA", e);
					System.exit(0);// 正常退出程序
				}

			}
			// Debug.i("getMDB", "SQLiteDatabase getMDB()");
			return mDb;
		}
	}

	/**
	 * 同步常用联系人
	 * 
	 * @Description:
	 * @param account
	 * @param contacts
	 * @param type
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:May 30, 2013
	 */
	public void syncCommonContacts(Account account, List<Contact> contacts, int type) {
		Debug.v(TAG_DBOPER, "syncCommonContacts");
		if (contacts != null && contacts.size() > 0) {
			deleteCommonContacts(account, type);
			saveCommonContacts(account, contacts, type);
		}
	}

	/**
	 * 保存常用联系人
	 * 
	 * @Description:
	 * @param account
	 * @param contacts
	 * @param type
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:May 30, 2013
	 */
	public void saveCommonContacts(Account account, List<Contact> contacts, int type) {
		Debug.v(TAG_DBOPER, "saveCommonContacts");
		mSBufferSQLTemp.setLength(0);
		mSBufferSQLTemp.append(SQL_I_INTO);
		mSBufferSQLTemp.append(TBL_COMMON_CONTACT);
		mSBufferSQLTemp.append(" (account_uuid,name,email,type) VALUES (?,?,?,?)");
		// String sql = SQL_I_INTO + TBL_COMMON_CONTACT + " (account_uuid,name,email,type) VALUES (?,?,?,?)";
		for (Contact contact : contacts) {
			getMDB().execSQL(mSBufferSQLTemp.toString(), new String[] { account.getUuid(), contact.getName(), contact.getEmail(), Integer.toString(type) });
		}
	}

	/**
	 * 删除常用联系人
	 * 
	 * @Description:
	 * @param account
	 * @param type
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:May 30, 2013
	 */
	public void deleteCommonContacts(Account account, int type) {
		Debug.v(TAG_DBOPER, "deleteCommonContacts");
		mSBufferSQLTemp.setLength(0);
		mSBufferSQLTemp.append(SQL_DEL_FROM);
		mSBufferSQLTemp.append(TBL_COMMON_CONTACT);
		mSBufferSQLTemp.append(" WHERE account_uuid =? AND type=? ");
		// String sql = SQL_DEL_FROM + TBL_COMMON_CONTACT + " WHERE account_uuid =? AND type=? ";
		getMDB().execSQL(mSBufferSQLTemp.toString(), new String[] { account.getUuid(), Integer.toString(type) });
	}

	/**
	 * 查询常用联系人
	 * 
	 * @Description:
	 * @param account
	 * @param type
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:May 30, 2013
	 */
	public List<ContactAttribute> getCommonContacts(Account account, int type) {
		Debug.v(TAG_DBOPER, "getCommonContacts");
		List<ContactAttribute> contacts = new ArrayList<ContactAttribute>();
		mSBufferSQLTemp.setLength(0);
		mSBufferSQLTemp.append("SELECT name,email FROM ");
		mSBufferSQLTemp.append(TBL_COMMON_CONTACT);
		mSBufferSQLTemp.append(" WHERE account_uuid =? AND type =? ");
		// String sql = "SELECT name,email FROM " + TBL_COMMON_CONTACT +
		// " WHERE account_uuid =? AND type =? ";
		Cursor cursor = null;
		try {
			cursor = getMDB().rawQuery(mSBufferSQLTemp.toString(), new String[] { account.getUuid(), Integer.toString(type) });
			while (cursor.moveToNext()) {
				ContactAttribute contact = new ContactAttribute();
				contact.setUserName(cursor.getString(0));
				contact.setValue(cursor.getString(1));
				contacts.add(contact);
			}
		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return contacts;
	}

	/**
	 * getFolder
	 * 
	 * @param account
	 *            账号名
	 * @param folderId
	 *            邮件夹id
	 * @see:
	 * @since:
	 * @author: hanlixia
	 * @date:2012-11-2
	 */
	public LocalFolder getFolder(Account account, String folderId) throws MessagingException {
		// Debug.v(TAG_DBOPER, "getFolder");
		return new LocalFolder(account.getUuid(), folderId);
	}

	// @Override
	// public void checkSettings() throws MessagingException {
	// }

	/**
	 * 创建account账号下邮件夹
	 * 
	 * @param account
	 * @see:
	 * @since:
	 * @author: hanlixia
	 * @throws Exception
	 * @date:2012-11-2
	 */
	public void initFolders(Account account) throws MessagingException {
		try {
			Debug.v(TAG_DBOPER, "initFolders");
			// String accountUid = account.getUuid();
			long accountId = getAccountIdByUuid(account.getUuid());
			createFolder(accountId, EmailApplication.MAILBOX_INBOX);
			createFolder(accountId, EmailApplication.MAILBOX_FAVORITEBOX);
			createFolder(accountId, EmailApplication.MAILBOX_ATTACHMENTBOX);
			createFolder(accountId, EmailApplication.MAILBOX_OUTBOX);
			createFolder(accountId, EmailApplication.MAILBOX_SENTBOX);
			createFolder(accountId, EmailApplication.MAILBOX_DRAFTSBOX);
			createFolder(accountId, EmailApplication.MAILBOX_TRASHBOX);
			createselfFolder(account);
			initFolderIdMap();
		} catch (MessagingException e) {
			throw e;
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
			throw new MessagingException("unable to initFolders:" + e.getMessage());
		}
	}

	/**
	 * 获取folderId
	 * 
	 * @param id
	 *            本地数据库的folder id 包括所有账户
	 * @return folderId
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Aug 13, 2012
	 */
	public String getFolderId(long id) {
		String folderId = null;
		if (mFolderGetUuid.get(id) != null && !"".equals(mFolderGetUuid.get(id))) {
			return mFolderGetUuid.get(id);
		}
		Debug.v(TAG_DBOPER, "getFolderId" + id);
		mSBufferSQLTemp.setLength(0);
		mSBufferSQLTemp.append("SELECT folderId FROM ");
		mSBufferSQLTemp.append(TBL_FOLDERS);
		mSBufferSQLTemp.append(" WHERE id = ? ");
		// String sql = "SELECT folderId FROM folders WHERE id = ? ";
		Cursor cursor = null;
		try {
			cursor = getMDB().rawQuery(mSBufferSQLTemp.toString(), new String[] { String.valueOf(id) });
			if (cursor.moveToFirst()) {
				folderId = cursor.getString(0);
				mFolderGetUuid.put(id, folderId);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return folderId;
	}

	/**
	 * 初始化folderid对照表
	 * 
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2013-5-22
	 */
	private void initFolderIdMap() {
		Debug.v(TAG_DBOPER, "initFolderIdMap");
		mSBufferSQLTemp.setLength(0);
		mSBufferSQLTemp.append(SQL_S_ALL_FROM);
		mSBufferSQLTemp.append(TBL_FOLDERS);
		// String qSql = SQL_S_ALL_FROM+TBL_FOLDERS;
		Cursor cursor = getMDB().rawQuery(mSBufferSQLTemp.toString(), new String[] {});
		while (cursor.moveToNext()) {
			mFolderGetUuid.put(cursor.getLong(0), cursor.getString(6));// new
																		// String[]{Long.toString(cursor.getLong(1)),
		}
		cursor.close();
	}

	/**
	 * Delete the entire Store and it's backing database.
	 */
	public void delete() {
		Debug.v(TAG_DBOPER, "delete()");
		try {
			if (mDb != null && mDb.isOpen())
				mDb.close();
			mDb = null;
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		try {
			if (mAttachmentsDir != null && mAttachmentsDir.exists()) {
				File[] attachments = mAttachmentsDir.listFiles();
				if (!Utility.isEmptyArray(attachments)) {
					for (File attachment : attachments) {
						if (attachment.exists()) {
							attachment.delete();
						}
					}
				}
				if (mAttachmentsDir.exists()) {
					mAttachmentsDir.delete();
				}
			}
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		try {
			new File(mPath).delete();
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 清空本地缓存
	 * 
	 * @see:
	 * @since:
	 * @author: hanlx
	 * @date:2013-1-17
	 */
	public void clearCache() {
		Debug.v(TAG_DBOPER, "clearCache()");
		try {
			getMDB().beginTransaction();

			String sqlbody = "delete from body";
			getMDB().execSQL(sqlbody, new Object[] {});
			String sqlattachments = "delete from attachments";
			getMDB().execSQL(sqlattachments, new Object[] {});
			String sqlmessages = "delete from messages";
			getMDB().execSQL(sqlmessages, new Object[] {});
			String sqlquery = "delete from  query_mailaddress ";
			getMDB().execSQL(sqlquery, new Object[] {});
			String sqlpending = "delete from  pending_commands ";
			getMDB().execSQL(sqlpending, new Object[] {});
			String sqlcompressitem = "delete from compressitems";
			getMDB().execSQL(sqlcompressitem, new Object[] {});

			String sqllabel = "delete from  label";
			getMDB().execSQL(sqllabel, new Object[] {});

			String sqlmessages_label_staging = "delete from  messages_label_staging";
			getMDB().execSQL(sqlmessages_label_staging, new Object[] {});

			String sqloperation_history = "delete from  operation_history";
			getMDB().execSQL(sqloperation_history, new Object[] {});

			String sqlattachements_list = "delete from attachments_list";
			getMDB().execSQL(sqlattachements_list, new Object[] {});

			getMDB().setTransactionSuccessful();
		} catch (Exception e) {
			C35AppServiceUtil.writeSubscribeInformationToSdcard("clearCache: " + e.toString());// 彩蛋log写入
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			getMDB().endTransaction();
		}
	}

	/**
	 * 根据uuid获取账号的Id,todo使用过于频繁，需要优化
	 * 
	 * @param uuid
	 * @return
	 * @see:
	 * @since:
	 * @author: hanlixia
	 * @date:2012-11-2
	 */
	public long getAccountIdByUuid(String uuid) {
		synchronized (mUuidToIdMap) {
			Cursor cursor = null;
			long id = -1;
			if (mUuidToIdMap.get(uuid) != null) {

				id = mUuidToIdMap.get(uuid);
				Debug.i(TAG, "mUuidToIdMap.get(uuid)_________________mUuidToIdMap.get(uuid):" + id);
				return id;
			} else {
				Debug.v(TAG_DBOPER, "getAccountIdByUuid()" + uuid);
				try {
					mSBufferSQLTemp.setLength(0);
					mSBufferSQLTemp.append("SELECT id FROM ");
					mSBufferSQLTemp.append(TBL_ACCOUNT);
					mSBufferSQLTemp.append(" WHERE uuid = ? ");
					// String sql = "SELECT id FROM "+TBL_ACCOUNT+" WHERE uuid = ? ";
					cursor = getMDB().rawQuery(mSBufferSQLTemp.toString(), new String[] { uuid });
					if (cursor.moveToFirst()) {
						id = cursor.getLong(0);
						mUuidToIdMap.put(uuid, id);
					}
				} catch (Exception e) {
					Debug.e("failfast", "failfast_AA", e);
				} finally {
					if (cursor != null) {
						cursor.close();
					}
				}
				return id;
			}
		}
	}

	/**
	 * 搜索邮件
	 * 
	 * @param account
	 * @param keyWord
	 *            搜索关键字
	 * @param searchKeyPosi
	 *            搜索条件
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @see:
	 * @since:
	 * @author: hanlixia
	 * @date:2012-11-2
	 */
	public List<C35Message> searchMails(Account account, String keyWord, int searchKeyPosi, int pageNo, int pageSize) {
		Debug.v(TAG_DBOPER, "searchMails(Account account, String keyWo");
		List<C35Message> messages = new ArrayList<C35Message>();
		Cursor cursor = null;
		String sql = "";
		try {
			switch (searchKeyPosi) {
			case MailUtil.SEARCH_MAIL_CONTEXT:
				// model=0 表示按照邮件內容搜索
				sql = "SELECT m.id _id,folder_id,subject,sender_list,date,m.uid,read_flag,delete_flag,preview,attachment_count,favorite FROM messages m,body b WHERE m.account_id = (SELECT id FROM account WHERE uuid = ? )  AND b.text_content LIKE ? AND m.id = b.message_id AND delete_flag!=2 AND down_flag!=0  ORDER BY m.date DESC LIMIT ?,?";
				cursor = getMDB().rawQuery(sql, new String[] { account.getUuid(), "%" + keyWord + "%", String.valueOf(pageNo * pageSize), String.valueOf(pageSize) });
				messages = processC35Messages(cursor);
				break;
			case MailUtil.SEARCH_MAIL_SUBJECT:
				// model=1 表示按照主题搜索
				sql = "SELECT id _id,folder_id,subject,sender_list,date,uid,read_flag,delete_flag,preview,attachment_count,favorite  " + "FROM messages WHERE account_id = (SELECT id FROM account WHERE uuid = ? ) AND subject like ? AND delete_flag!=2   ORDER BY date DESC LIMIT ?,?";
				cursor = getMDB().rawQuery(sql, new String[] { account.getUuid(), "%" + keyWord + "%", String.valueOf(pageNo * pageSize), String.valueOf(pageSize) });
				messages = processC35Messages(cursor);
				break;
			case MailUtil.SEARCH_MAIL_RECEIVER:
				// model=2 表示按照收件人搜索
				sql = "SELECT id _id,folder_id,subject,sender_list,date,uid,read_flag,delete_flag,preview,attachment_count,favorite  " + "FROM messages WHERE account_id = (SELECT id FROM account WHERE uuid = ? ) AND to_list like ? AND delete_flag!=2 ORDER BY date DESC LIMIT ?,? ";
				cursor = getMDB().rawQuery(sql, new String[] { account.getUuid(), "%" + keyWord + "%", String.valueOf(pageNo * pageSize), String.valueOf(pageSize) });
				messages = processC35Messages(cursor);
				break;
			case MailUtil.SEARCH_MAIL_SENDER:
				// medel=3 表示按照发件人搜索
				sql = "SELECT id _id,folder_id,subject,sender_list,date,uid,read_flag,delete_flag,preview,attachment_count,favorite  " + "FROM messages WHERE account_id = (SELECT id FROM account WHERE uuid = ? ) AND sender_list like ? AND delete_flag!=2  ORDER BY date DESC LIMIT ?,? ";
				cursor = getMDB().rawQuery(sql, new String[] { account.getUuid(), "%" + keyWord + "%", String.valueOf(pageNo * pageSize), String.valueOf(pageSize) });
				messages = processC35Messages(cursor);
				break;
			case MailUtil.SEARCH_MAIL_ALL:
				// medel=5 表示搜索所有记录
				// 此为搜索message表里的，包含关键字的主题、发件人、收件人
				sql = "SELECT id _id,folder_id,subject,sender_list,date,uid,read_flag,delete_flag,preview,attachment_count,favorite  " + "FROM messages WHERE account_id = (SELECT id FROM account WHERE uuid = ? ) AND (sender_list like ? or to_list like ? or subject like ? ) AND delete_flag!=2   ORDER BY date DESC LIMIT ?,?";
				cursor = getMDB().rawQuery(sql, new String[] { account.getUuid(), "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", String.valueOf(pageNo * pageSize), String.valueOf(pageSize) });
				List<C35Message> messages1 = processC35Messages(cursor);

				// 此为搜索body表里的内容包含关键字
				sql = "SELECT m.id _id,folder_id,subject,sender_list,date,m.uid,read_flag,delete_flag,preview,attachment_count,favorite FROM messages m,body b WHERE m.account_id = (SELECT id FROM account WHERE uuid = ? )  AND b.text_content LIKE ? AND m.id = b.message_id AND delete_flag!=2 AND down_flag!=0  ORDER BY m.date DESC LIMIT ?,?";
				Cursor cursor2 = getMDB().rawQuery(sql, new String[] { account.getUuid(), "%" + keyWord + "%", String.valueOf(pageNo * pageSize), String.valueOf(pageSize) });
				List<C35Message> messages2 = processC35Messages(cursor2);
				// messages1与messages2合并滤重(虽看起来较繁琐，速度还可以)

				HashSet<C35Message> messagesNew = new HashSet<C35Message>();
				for (C35Message msg : messages1) {
					messagesNew.add(msg);
				}

				for (C35Message msg : messages2) {
					messagesNew.add(msg);
				}

				for (C35Message msg : messagesNew) {
					messages.add(msg);
				}
				break;
			case MailUtil.SEARCH_MAIL_BETWEEN:
				// searchKeyPosi=5 表示搜索往来邮件
				sql = "SELECT id _id,folder_id,subject,sender_list,date,uid,read_flag,delete_flag,preview,attachment_count,favorite  " + "FROM messages WHERE account_id = (SELECT id FROM account WHERE uuid = ? ) AND delete_flag!=2 AND ((sender_list like ? AND to_list like ?)or(sender_list like ? AND to_list like ?)) ORDER BY date DESC LIMIT ?,?";
				cursor = getMDB().rawQuery(sql, new String[] { account.getUuid(), "%" + account.getmEmailShow() + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + account.getmEmailShow() + "%", String.valueOf(pageNo * pageSize), String.valueOf(pageSize) });
				messages = processC35Messages(cursor);
				break;
			default:
				break;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return messages;
	}

	/**
	 * 将查询出来的邮件实例化
	 * 
	 * @param cursor
	 * @return
	 * @see:
	 * @since:
	 * @author: hanlixia
	 * @date:2012-11-2
	 */
	private List<C35Message> processC35Messages(Cursor cursor) {
		Debug.v(TAG_DBOPER, "processC35Messages");
		List<C35Message> messages = new ArrayList<C35Message>();
		while (cursor.moveToNext()) {
			C35Message c35Message = new C35Message();
			c35Message.setFolderId(cursor.getString(cursor.getColumnIndex("folder_id")));// TODO 此为序列id
			c35Message.setMailId(cursor.getString(cursor.getColumnIndex("uid")));
			c35Message.setSubject(cursor.getString(cursor.getColumnIndex("subject")));
			// c35Message.setHyperText(cursor.getString(cursor.getColumnIndex("html_content")));
			// c35Message.setPlainText(cursor.getString(cursor.getColumnIndex("text_content")));
			c35Message.setAttachSize(cursor.getInt(cursor.getColumnIndex("attachment_count")));
			// SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			c35Message.setSendTime(EmailApplication.DateFormatYMDHMS.format((cursor.getLong(cursor.getColumnIndex("date")))));
			// c35Message.setDownFalg(cursor.getInt(cursor.getColumnIndex("down_flag")));
			c35Message.setRead(cursor.getInt(cursor.getColumnIndex("read_flag")));

			// 字段已删除
			// c35Message.setForwardFalg(cursor.getInt(cursor.getColumnIndex("forward_flag")));
			// c35Message.setReplayFalg(cursor.getInt(cursor.getColumnIndex("replay_flag")));
			c35Message.setDeliverStatus(cursor.getInt(cursor.getColumnIndex("delete_flag")));
			c35Message.setDeleteFalg(cursor.getInt(cursor.getColumnIndex("delete_flag")));
			// c35Message.setReMailId(cursor.getString(cursor.getColumnIndex("remail_id")));
			c35Message.setPreview(cursor.getString(cursor.getColumnIndex("preview")));
			// c35Message.setSendType(cursor.getInt(cursor.getColumnIndex("send_type")));
			String send = cursor.getString(cursor.getColumnIndex("sender_list"));
			c35Message.setFrom(send);
			c35Message.setImportantFlag(cursor.getInt(cursor.getColumnIndex("favorite")));
			messages.add(c35Message);
		}
		return messages;
	}

	/**
	 * 本地邮件夹实例
	 * 
	 * @author:hanlixia
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2012-11-2
	 */
	public class LocalFolder extends Folder {

		private String mFolderId;
		private long mId = -1;
		private long mAccountId = -1;
		private String mAccountUid = null;

		// private int mUnreadMessageCount = -1;
		// private int mVisibleLimit = -1;
		/**
		 * SELECT id as _id,uid, subject, date,sender_list, read_flag,attachment_count,favorite,preview,
		 * to_list LIKE ? OR to_list LIKE ? tome,forward_replay_flag,to_list,cc_list,bcc_list,
		 * is_important_from,priority,reader_list,reader_count FROM messages WHERE folder_id = ? AND
		 * delete_flag=? AND account_id = ? ORDER BY date DESC LIMIT ?
		 * 
		 * @param queryString
		 * @param placeHolders
		 *            //[%pm3@china-channel.com%, %pm3@35.cn%, 41, 0, 2, 20]
		 * @return
		 * @see:
		 * @since:
		 * @author: cuiwei
		 * @date:2013-11-19
		 */
		private Cursor getMessages(final String queryString, final String[] placeHolders) {// [%pm3@china-channel.com%,
																							// %pm3@35.cn%,
																							// 41, 0, 2, 20]
			Debug.v(TAG_DBOPER, "getMessages(final String queryS");
			return getMDB().rawQuery(queryString, placeHolders);
		}

		/**
		 * 貌似是从这里获得邮件信息，显示到MessageList列表中的（add comment by zhuanggy）
		 * 
		 * @Description:
		 * @param limit
		 * @param email
		 * @return
		 * @throws MessagingException
		 * @see:
		 * @since:
		 * @author: xulei
		 * @date:Apr 11, 2012
		 */
		public Cursor getMessages(int limit, String email, int mailstate) throws MessagingException {
			Debug.v(TAG_DBOPER, "getMessages(int limit, String email, int mailstate) throws MessagingException show____localStore_______message__folderId:");
			try {
				open(OpenMode.READ_WRITE);
				String alias = email.substring(0, email.indexOf('@'));// email
																		// like
																		// xulei@35.cn
				if (email.endsWith(MailUtil.EMAIL_SUFFIX_35CN)) {
					alias = alias + MailUtil.EMAIL_SUFFIX_CHINACHANNEL;
				} else if (email.endsWith(MailUtil.EMAIL_SUFFIX_CHINACHANNEL)) {
					alias = alias + MailUtil.EMAIL_SUFFIX_35CN;
				} else {
					alias = email;
				}
				email = "%" + email + "%";
				alias = "%" + alias + "%";
				String queryString = "SELECT id as _id,uid, subject, date,sender_list, read_flag,attachment_count,favorite,preview,to_list LIKE ? OR to_list LIKE ? tome,forward_replay_flag,to_list,cc_list,bcc_list,is_important_from,priority,reader_list,reader_count FROM messages ";
				String where;
				String orderASC = " ORDER BY date DESC  LIMIT ?";
				String order = " ORDER BY date DESC";
				String whereDate = " AND date >= ? ORDER BY date DESC";
				String readFlag = " AND read_flag = ?";
				String tomeFlag = " AND is_send_me = ?";
				String importantFlag = " AND is_important_from = ?";
				String priorityFlag = " AND priority = ?";
				Cursor nCursor;
				if (mFolderId.equals(EmailApplication.MAILBOX_FAVORITEBOX)) {
					where = "WHERE  favorite = ? AND delete_flag = ? AND account_id = ? ";
					switch (mailstate) {
					
					//case MessageList.SHOW_TOME:
					// Modified by LL
					case 2:
					    
						// nCursor = getMessages(queryString + where + tomeFlag + orderASC, new String[] {
						// email, alias, String.valueOf(C35Message.FAVORITE_FLAG_FAV),
						// String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId),
						// String.valueOf(C35Message.IS_SEND_ME), String.valueOf(limit) });
						mSBufferSQLTemp.setLength(0);
						nCursor = getMessages(mSBufferSQLTemp.append(queryString).append(where).append(tomeFlag).append(orderASC).toString(), new String[] { email, alias, String.valueOf(C35Message.FAVORITE_FLAG_FAV), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(C35Message.IS_SEND_ME), String.valueOf(limit) });
						if (nCursor != null && nCursor.getCount() < limit) {
							mSBufferSQLTemp.setLength(0);
							return getMessages(mSBufferSQLTemp.append(queryString).append(where).append(order).toString(), new String[] { email, alias, String.valueOf(C35Message.FAVORITE_FLAG_FAV), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId) });
						} else if (nCursor != null) {
							nCursor.moveToLast();
							long datetime = nCursor.getLong(nCursor.getColumnIndex("date"));
							mSBufferSQLTemp.setLength(0);
							return getMessages(mSBufferSQLTemp.append(queryString).append(where).append(whereDate).toString(), new String[] { email, alias, String.valueOf(C35Message.FAVORITE_FLAG_FAV), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(datetime) });
						}
						break;
					
					//case MessageList.SHOW_UNREAD:
					// Modified by LL
					case 1:
					    
						mSBufferSQLTemp.setLength(0);
						nCursor = getMessages(mSBufferSQLTemp.append(queryString).append(where).append(readFlag).append(orderASC).toString(), new String[] { email, alias, String.valueOf(C35Message.FAVORITE_FLAG_FAV), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(C35Message.READ_FLAG_UNREAD), String.valueOf(limit) });
						if (nCursor != null && nCursor.getCount() < limit) {
							mSBufferSQLTemp.setLength(0);
							return getMessages(mSBufferSQLTemp.append(queryString).append(where).append(order).toString(), new String[] { email, alias, String.valueOf(C35Message.FAVORITE_FLAG_FAV), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId) });
						} else if (nCursor != null) {
							nCursor.moveToLast();
							long datetime = nCursor.getLong(nCursor.getColumnIndex("date"));
							mSBufferSQLTemp.setLength(0);
							return getMessages(mSBufferSQLTemp.append(queryString).append(where).append(whereDate).toString(), new String[] { email, alias, String.valueOf(C35Message.FAVORITE_FLAG_FAV), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(datetime) });
						}
						break;
						
					//case MessageList.SHOW_IMPORTANT:
					// Modified by LL
					case 3:
					    
						nCursor = getMessages(queryString + where + importantFlag + orderASC, new String[] { email, alias, String.valueOf(C35Message.FAVORITE_FLAG_FAV), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(C35Message.IS_IMPORTANT_FROM), String.valueOf(limit) });
						if (nCursor != null && nCursor.getCount() < limit) {
							return getMessages(queryString + where + order, new String[] { email, alias, String.valueOf(C35Message.FAVORITE_FLAG_FAV), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId) });
						} else if (nCursor != null) {
							nCursor.moveToLast();
							long datetime = nCursor.getLong(nCursor.getColumnIndex("date"));
							return getMessages(queryString + where + whereDate, new String[] { email, alias, String.valueOf(C35Message.FAVORITE_FLAG_FAV), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(datetime) });
						}
						break;
						
					//case MessageList.SHOW_URGENCY:
					// Modified by LL
					case 4:
					    
						nCursor = getMessages(queryString + where + priorityFlag + orderASC, new String[] { email, alias, String.valueOf(C35Message.FAVORITE_FLAG_FAV), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(C35Message.IS_PRIORITY), String.valueOf(limit) });
						if (nCursor != null && nCursor.getCount() < limit) {
							return getMessages(queryString + where + order, new String[] { email, alias, String.valueOf(C35Message.FAVORITE_FLAG_FAV), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId) });
						} else if (nCursor != null) {
							nCursor.moveToLast();
							long datetime = nCursor.getLong(nCursor.getColumnIndex("date"));
							return getMessages(queryString + where + whereDate, new String[] { email, alias, String.valueOf(C35Message.FAVORITE_FLAG_FAV), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(datetime) });
						}
						break;

					}
					where = "WHERE  favorite = ? AND delete_flag = ? AND account_id = ? ORDER BY date DESC  LIMIT ?";
					return getMessages(queryString + where, new String[] { email, alias, String.valueOf(C35Message.FAVORITE_FLAG_FAV), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(limit) });
				} else if (mFolderId.equals(EmailApplication.MAILBOX_TRASHBOX)) {
					where = "WHERE account_id = ? AND delete_flag = ?";
					switch (mailstate) {
					
					//case MessageList.SHOW_TOME:
					// Modified by LL
					case 2:
					    
						nCursor = getMessages(queryString + where + tomeFlag + orderASC, new String[] { email, alias, String.valueOf(mAccountId), String.valueOf(C35Message.DELETE_FLAG_DELETED), String.valueOf(C35Message.IS_SEND_ME), String.valueOf(limit) });
						if (nCursor != null && nCursor.getCount() < limit) {
							return getMessages(queryString + where + order, new String[] { email, alias, String.valueOf(mAccountId), String.valueOf(C35Message.DELETE_FLAG_DELETED) });
						} else if (nCursor != null) {
							nCursor.moveToLast();
							long datetime = nCursor.getLong(nCursor.getColumnIndex("date"));
							return getMessages(queryString + where + whereDate, new String[] { email, alias, String.valueOf(mAccountId), String.valueOf(C35Message.DELETE_FLAG_DELETED), String.valueOf(datetime) });
						}
						break;
						
					//case MessageList.SHOW_UNREAD:
					// Modified by LL
					case 1:
					    
						nCursor = getMessages(queryString + where + readFlag + orderASC, new String[] { email, alias, String.valueOf(mAccountId), String.valueOf(C35Message.DELETE_FLAG_DELETED), String.valueOf(C35Message.READ_FLAG_UNREAD), String.valueOf(limit) });
						if (nCursor != null && nCursor.getCount() < limit) {
							return getMessages(queryString + where + order, new String[] { email, alias, String.valueOf(mAccountId), String.valueOf(C35Message.DELETE_FLAG_DELETED) });
						} else if (nCursor != null) {
							nCursor.moveToLast();
							long datetime = nCursor.getLong(nCursor.getColumnIndex("date"));
							return getMessages(queryString + where + whereDate, new String[] { email, alias, String.valueOf(mAccountId), String.valueOf(C35Message.DELETE_FLAG_DELETED), String.valueOf(datetime) });
						}
						break;
						
					//case MessageList.SHOW_IMPORTANT:
					// Modified by LL
					case  3:
					    
						nCursor = getMessages(queryString + where + importantFlag + orderASC, new String[] { email, alias, String.valueOf(mAccountId), String.valueOf(C35Message.DELETE_FLAG_DELETED), String.valueOf(C35Message.IS_IMPORTANT_FROM), String.valueOf(limit) });
						if (nCursor != null && nCursor.getCount() < limit) {
							return getMessages(queryString + where + order, new String[] { email, alias, String.valueOf(mAccountId), String.valueOf(C35Message.DELETE_FLAG_DELETED) });
						} else if (nCursor != null) {
							nCursor.moveToLast();
							long datetime = nCursor.getLong(nCursor.getColumnIndex("date"));
							return getMessages(queryString + where + whereDate, new String[] { email, alias, String.valueOf(mAccountId), String.valueOf(C35Message.DELETE_FLAG_DELETED), String.valueOf(datetime) });
						}
						break;
						
					//case MessageList.SHOW_URGENCY:
					// Modified by LL
					case 4:
					    
						nCursor = getMessages(queryString + where + priorityFlag + orderASC, new String[] { email, alias, String.valueOf(mAccountId), String.valueOf(C35Message.DELETE_FLAG_DELETED), String.valueOf(C35Message.IS_PRIORITY), String.valueOf(limit) });
						if (nCursor != null && nCursor.getCount() < limit) {
							return getMessages(queryString + where + order, new String[] { email, alias, String.valueOf(mAccountId), String.valueOf(C35Message.DELETE_FLAG_DELETED) });
						} else if (nCursor != null) {
							nCursor.moveToLast();
							long datetime = nCursor.getLong(nCursor.getColumnIndex("date"));
							return getMessages(queryString + where + whereDate, new String[] { email, alias, String.valueOf(mAccountId), String.valueOf(C35Message.DELETE_FLAG_DELETED), String.valueOf(datetime) });
						}
						break;

					}
					where = "WHERE account_id = ? AND delete_flag = ?  ORDER BY date DESC LIMIT ?";
					return getMessages(queryString + where, new String[] { email, alias, String.valueOf(mAccountId), String.valueOf(C35Message.DELETE_FLAG_DELETED), String.valueOf(limit) });
				} else {
					where = "WHERE folder_id = ?  AND delete_flag=? AND account_id = ?";
					switch (mailstate) {
					
					//case MessageList.SHOW_TOME:
					// Modified by LL
					case 2:
					    
						Debug.i(TAG, "queryString:" + queryString + where + tomeFlag + orderASC + "|value:" + email + "|" + alias + "|" + String.valueOf(mId) + "|" + String.valueOf(C35Message.DELETE_FLAG_NORMOR) + "|" + String.valueOf(mAccountId) + "|" + String.valueOf(C35Message.IS_SEND_ME) + "|" + String.valueOf(limit) + "|");
						nCursor = getMessages(queryString + where + tomeFlag + orderASC, new String[] { email, alias, String.valueOf(mId), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(C35Message.IS_SEND_ME), String.valueOf(limit) });
						Debug.i(TAG, "nCursor.getCount()_________:" + nCursor.getCount() + " |limit_______:" + limit);
						if (nCursor != null && nCursor.getCount() < limit) {
							return getMessages(queryString + where + order, new String[] { email, alias, String.valueOf(mId), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId) });
						} else if (nCursor != null) {
							nCursor.moveToLast();
							long datetime = nCursor.getLong(nCursor.getColumnIndex("date"));
							Debug.i(TAG, "datetime:" + datetime);
							return getMessages(queryString + where + whereDate, new String[] { email, alias, String.valueOf(mId), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(datetime) });
						}
						break;
						
					//case MessageList.SHOW_UNREAD:
					// Modified by LL
					case 1:
					    
						Debug.i(TAG, "queryString:" + queryString + where + readFlag + orderASC + "|value:" + email + "|" + alias + "|" + String.valueOf(mId) + "|" + String.valueOf(C35Message.DELETE_FLAG_NORMOR) + "|" + String.valueOf(mAccountId) + "|" + String.valueOf(C35Message.READ_FLAG_UNREAD) + "|" + String.valueOf(limit) + "|");
						nCursor = getMessages(queryString + where + readFlag + orderASC, new String[] { email, alias, String.valueOf(mId), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(C35Message.READ_FLAG_UNREAD), String.valueOf(limit) });
						Debug.i(TAG, "nCursor.getCount()_________:" + nCursor.getCount() + " |limit_______:" + limit);
						if (nCursor != null && nCursor.getCount() < limit) {
							return getMessages(queryString + where + order, new String[] { email, alias, String.valueOf(mId), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId) });
						} else if (nCursor != null) {
							nCursor.moveToLast();
							long datetime = nCursor.getLong(nCursor.getColumnIndex("date"));
							Debug.i(TAG, "datetime:" + datetime);
							Debug.i(TAG, "queryString____last:" + queryString + where + whereDate + "   string:" + email + "|" + alias + "|" + String.valueOf(mId) + "|" + String.valueOf(C35Message.DELETE_FLAG_NORMOR) + "|" + String.valueOf(mAccountId) + "|" + String.valueOf(datetime));
							return getMessages(queryString + where + whereDate, new String[] { email, alias, String.valueOf(mId), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(datetime) });
						}
						break;
						
					//case MessageList.SHOW_IMPORTANT:
					// Modified by LL
					case 3:
					    
						nCursor = getMessages(queryString + where + importantFlag + orderASC, new String[] { email, alias, String.valueOf(mId), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(C35Message.IS_IMPORTANT_FROM), String.valueOf(limit) });
						if (nCursor != null && nCursor.getCount() < limit) {
							return getMessages(queryString + where + order, new String[] { email, alias, String.valueOf(mId), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId) });
						} else if (nCursor != null) {
							nCursor.moveToLast();
							long datetime = nCursor.getLong(nCursor.getColumnIndex("date"));
							return getMessages(queryString + where + whereDate, new String[] { email, alias, String.valueOf(mId), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(datetime) });
						}
						break;
						
					//case MessageList.SHOW_URGENCY:
					// Modified by LL
					case 4:
					    
						nCursor = getMessages(queryString + where + priorityFlag + orderASC, new String[] { email, alias, String.valueOf(mId), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(C35Message.IS_PRIORITY), String.valueOf(limit) });
						if (nCursor != null && nCursor.getCount() < limit) {
							return getMessages(queryString + where + order, new String[] { email, alias, String.valueOf(mId), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId) });
						} else if (nCursor != null) {
							nCursor.moveToLast();
							long datetime = nCursor.getLong(nCursor.getColumnIndex("date"));
							return getMessages(queryString + where + whereDate, new String[] { email, alias, String.valueOf(mId), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(datetime) });
						}
						break;

					}
					where = "WHERE folder_id = ?  AND delete_flag=? AND account_id = ? ORDER BY date DESC LIMIT ?";
					return getMessages(queryString + where, new String[] { email, alias, String.valueOf(mId), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(limit) });
				}
			} catch (Exception e) {
				throw new MessagingException("unable to get messages:" + e.getMessage());
			}
		}

		public Cursor getMessages(String folderId) {
			Debug.v(TAG_DBOPER, "getMessages(String folderId)" + folderId);
			long folderIntId = -1;
			folderIntId = getIdByFolderUuidAndAccountID(mAccountId, folderId);
			mSBufferSQLTemp.setLength(0);
			mSBufferSQLTemp.append("SELECT uid,delete_flag FROM ");
			mSBufferSQLTemp.append(TBL_MESSAGES);
			mSBufferSQLTemp.append(" WHERE folder_id =? and account_id=?");
			// String sql = "SELECT uid,delete_flag FROM messages WHERE folder_id =? and account_id=?";
			if (mFolderId.equals(EmailApplication.MAILBOX_FAVORITEBOX)) {
				mSBufferSQLTemp.setLength(0);
				mSBufferSQLTemp.append("SELECT uid,delete_flag FROM ");
				mSBufferSQLTemp.append(TBL_MESSAGES);
				mSBufferSQLTemp.append(" WHERE favorite = ?  AND account_id = ? ");
				// sql = "SELECT uid,delete_flag FROM messages WHERE favorite = ?  AND account_id = ? ";
				return getMDB().rawQuery(mSBufferSQLTemp.toString(), new String[] { String.valueOf(C35Message.FAVORITE_FLAG_FAV), String.valueOf(mAccountId) });
			}
			if (folderIntId != -1) {
				return getMDB().rawQuery(mSBufferSQLTemp.toString(), new String[] { String.valueOf(folderIntId), String.valueOf(mAccountId) });
			} else {
				return getMDB().rawQuery(mSBufferSQLTemp.toString(), new String[] { String.valueOf(mId), String.valueOf(mAccountId) });
			}
		}

		public Cursor getAllMessages() {
			Debug.v(TAG_DBOPER, "getAllMessages()");
			mSBufferSQLTemp.setLength(0);
			mSBufferSQLTemp.append("SELECT uid,delete_flag FROM ");
			mSBufferSQLTemp.append(TBL_MESSAGES);
			mSBufferSQLTemp.append(" WHERE account_id=?");
			// String sql = "SELECT uid,delete_flag FROM messages WHERE account_id=?";
			return getMDB().rawQuery(mSBufferSQLTemp.toString(), new String[] { String.valueOf(mAccountId) });
		}

		public String getRefUid() {
			Debug.v(TAG_DBOPER, "getRefUid()");
			String result = null;
			mSBufferSQLTemp.setLength(0);
			mSBufferSQLTemp.append("SELECT uid FROM ");
			mSBufferSQLTemp.append(TBL_MESSAGES);
			mSBufferSQLTemp.append(" WHERE folder_id = ? and account_id=? ORDER BY date DESC LIMIT 20");
			// String sql =
			// "SELECT uid FROM messages WHERE folder_id = ? and account_id=? ORDER BY date DESC LIMIT 20";
			Cursor cursor = getMDB().rawQuery(mSBufferSQLTemp.toString(), new String[] { String.valueOf(mId), String.valueOf(mAccountId) });
			if (cursor.moveToLast()) {
				result = cursor.getString(0);
			}
			cursor.close();
			return result;
		}

		/**
		 * 保存邮件的简要信息(uid 日期 下载状态),为与服务器同步提供参考
		 * 
		 * @Title: saveMessagesHeader
		 * @author xulei
		 * @date Dec 6, 2011
		 * @return void
		 * @throws MessagingException
		 */
		public void saveMessagesHeader(Account account, List<C35Message> messages) throws MessagingException {
			if (mAccountId == -1) {
				return;
			}
			try {
				Debug.v(TAG_DBOPER, "saveMessagesHeader()");
				getMDB().beginTransaction();
				for (C35Message message : messages) {

					String from = "";
					long forlderIdForSave = -1;
					forlderIdForSave = getIdByFolderUuidAndAccountID(mAccountId, message.getFolderId());
					if (forlderIdForSave == -1) {
						throw new MessagingException(MessagingException.FOLDER_ID_ERROR, "Get folderId failed， unknown " + message.getFolderId());
					}
					/*
					 * if (message.getFrom() != null && !"".equals(message.getFrom())) { from =
					 * message.getFrom().replaceAll("\"", "").replace("  ", " "); if (from.split(" ").length
					 * == 1) { if (!from.contains("<")) { String name = subAtChar(from); from = name + " <" +
					 * from + ">"; } else { String name = from.replace("<", ""); if (name.indexOf("@") > 0) {
					 * name = name.split("@")[0]; } // from = name + " " + from; } } }
					 */
					ContentValues cv = new ContentValues();
					saveFrom(message, cv);
					from = (String) cv.get("sender_list");
					String to = makeAddressString(message.getTo());
					String cc = makeAddressString(message.getCc());
					String bcc = makeAddressString(message.getBcc());
					String readers = makeReaderListString(message.getDeliveredReadUsers());
					int readflag = message.getRead();
					int attachment_count = message.getAttachSize();
					int readerCount = message.getDeliveredReadCount();
					long size = message.getSize();

					int isSendToMe = 0;
					String tempEmailName;
					if (to != null) {
						if (to.contains(account.getEmail())) {// 如果true说明有主送我的，但false并不代表没有主送我的。因为域名有两个。
							isSendToMe = 1;
						} else {
							if (account.getEmail().endsWith(MailUtil.EMAIL_SUFFIX_35CN)) {// 如果账号是35.cn,则替换成@china-channel.com在去判断一下。
								tempEmailName = account.getEmail().replace(MailUtil.EMAIL_SUFFIX_35CN, MailUtil.EMAIL_SUFFIX_CHINACHANNEL);
								if (to.contains(tempEmailName)) {
									isSendToMe = 1;
								} else {
									isSendToMe = 0;
								}
							} else if (account.getEmail().endsWith(MailUtil.EMAIL_SUFFIX_CHINACHANNEL)) {// 如果账号是@china-channel.com,则替换成35.cn在去判断一下。
								tempEmailName = account.getEmail().replace(MailUtil.EMAIL_SUFFIX_CHINACHANNEL, MailUtil.EMAIL_SUFFIX_35CN);
								if (to.contains(tempEmailName)) {
									isSendToMe = 1;
								} else {
									isSendToMe = 0;
								}
							}
						}
					}

					if (!message.getFolderId().equals(EmailApplication.MAILBOX_TRASHBOX)) {// 不同步已删除
						if (haveThisMsg(account.getUuid(), message.getMailId())) {
							Log.d(TAG, " message.getMailId()==" + message.getMailId() + " message.getFolderId()==" + message.getFolderId());
							getMDB().execSQL("UPDATE messages set folder_id=? ,favorite = ?  , read_flag = ?  ,delete_flag = ?  ,calendarState= ? ,mailType = ?, reader_list = ?, reader_count = ? WHERE uid = ? AND account_id = (SELECT id FROM account WHERE uuid = ? ) ", new Object[] { forlderIdForSave, message.getImportantFlag(), message.getRead(), C35Message.DELETE_FLAG_NORMOR, message.getCalendarState(), message.getMailType(), readers, readerCount, message.getMailId(), account.getUuid() });

						} else {
							// SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							Date date = new Date();
							try {
								date = EmailApplication.DateFormatYMDHMS.parse(message.getSendTime());
							} catch (ParseException e) {
								Debug.e("failfast", "failfast_AA", e);
							}
							Log.d(TAG, " message.getMailId()==" + message.getMailId() + " message.getFolderId()==" + message.getFolderId());
							getMDB().execSQL(SQL_I_INTO + TBL_MESSAGES + " (account_id,folder_id,uid,subject,date,down_flag,preview,to_list,cc_list,bcc_list,sender_list,attachment_count,mail_size,read_flag,is_send_me,is_important_from,priority,calendarState,mailType,favorite,reader_list,reader_count) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new Object[] { mAccountId, forlderIdForSave, message.getMailId(), message.getSubject(), date.getTime(), C35Message.DOWNLOAD_FLAG_ENVELOPE, message.getPreview(), to, cc, bcc, from, attachment_count, size, readflag, isSendToMe, message.getImportantFrom(), message.getPriority(), message.getCalendarState(), message.getMailType(), message.getImportantFlag(), readers, readerCount });
						}
					}

					storeLabel(message);
				}
				getMDB().setTransactionSuccessful();

			} catch (MessagingException e) {
				throw e;
			} catch (Exception e) {
				Debug.e("failfast", "failfast_AA", e);
				throw new MessagingException("save err" + e.getMessage());
			} finally {
				getMDB().endTransaction();
			}
		}

		public void saveSearchHistory(Account account, String keyWord) {
			Debug.v(TAG_DBOPER, "saveSearchHistory()");
			LocalStoreAccountsInfo accountInfo = new LocalStoreAccountsInfo();
			accountInfo.open();
			if (!"".equals(keyWord)) {
				accountInfo.insertSearchHistory(mAccountId, keyWord);
			}
			accountInfo.close();
		}

		public List<String> getSearchHistoryKeyword(Account account) {
			Debug.v(TAG_DBOPER, "getSearchHistoryKeyword()");
			LocalStoreAccountsInfo accountInfo = new LocalStoreAccountsInfo();
			accountInfo.open();
			List<String> resultList = accountInfo.selectSearchHistoryKeyword(mAccountId);
			accountInfo.close();
			return resultList;
		}

		public void deleteSearchHistory() {
			Debug.v(TAG_DBOPER, "deleteSearchHistory()");
			LocalStoreAccountsInfo accountInfo = new LocalStoreAccountsInfo();
			accountInfo.open();
			accountInfo.deleteSearchHistory(mAccountId);
			accountInfo.close();
		}

		@Override
		public long getLastUpdate() {
			Debug.v(TAG_DBOPER, "getLastUpdate()");
			long lastUpdate = System.currentTimeMillis();// ???todo
			Cursor cursor = null;
			try {
				cursor = getMDB().rawQuery("SELECT last_updated from folders where id=?", new String[] { String.valueOf(mId) });
				if (cursor.moveToFirst()) {
					lastUpdate = cursor.getLong(0);
				}
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			return lastUpdate;
		}

		/**
		 * 邮件夹更新时间
		 * 
		 * @Description:
		 * @param lastUpdate
		 * @see:
		 * @since:
		 * @author: cuiwei
		 * @date:2013-12-17
		 */
		public void setLastUpdate(long lastUpdate) {
			Debug.v(TAG_DBOPER, "setLastUpdate()");
			if (getMDB() != null) {
				getMDB().execSQL("UPDATE folders SET last_updated=? WHERE id = ? ", new Object[] { lastUpdate, mId });
			}
		}

		public LocalFolder(String accountUid, String folderId) {
			// Debug.v(TAG_DBOPER, "LocalFolder()");
			this.mFolderId = folderId;
			this.mAccountUid = accountUid;
		}

		@Override
		public void open(OpenMode mode) throws MessagingException {
			synchronized (mFolderId) {
				if (mFolderId == null || mFolderId.equals("") || mFolderId.equals("search")) {
					return;
				}
				// Debug.v(TAG_DBOPER, "open(OpenMode mode()");
				// long folderId = -1;
				mAccountId = getAccountIdByUuid(mAccountUid);
				if (!isOpen()) {
					// "in open methord  其它偶尔导致变慢的因素可能在此附近");//log较频繁，注释掉
					mId = createFolder(mAccountId, mFolderId);
					// }
				}

			}
		}

		@Override
		public boolean isOpen() {
			return mId != -1;
		}

		@Override
		public OpenMode getMode() throws MessagingException {
			return OpenMode.READ_WRITE;
		}

		@Override
		public String getName() {
			return mFolderId;
		}

		@Override
		public void close(boolean expunge) throws MessagingException {
			mId = -1;
			mAccountUid = null;
			mAccountId = -1;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof LocalFolder) {
				return ((LocalFolder) o).mFolderId.equals(mFolderId);
			}
			return super.equals(o);
		}

	}

	/**
	 * 获取已删除邮件
	 * 
	 * @param account
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2013-7-15
	 */
	public Set<Long> getTrashMails(Account account) {
		Debug.v(TAG_DBOPER, "getTrashMails");
		Set<Long> idList = new HashSet<Long>();
		Cursor cursor = null;
		try {
			mSBufferSQLTemp.setLength(0);
			mSBufferSQLTemp.append("SELECT id FROM ");
			mSBufferSQLTemp.append(TBL_MESSAGES);
			mSBufferSQLTemp.append(" WHERE delete_flag = ? AND account_id = (SELECT id FROM account WHERE uuid = ? )");
			// getMDB().rawQuery("SELECT id FROM messages WHERE delete_flag = ? AND account_id = (SELECT id FROM account WHERE uuid = ? )"
			cursor = getMDB().rawQuery(mSBufferSQLTemp.toString(), new String[] { String.valueOf(C35Message.DELETE_FLAG_DELETED), account.getUuid() });
			while (cursor.moveToNext()) {
				long messageId = cursor.getLong(0);
				idList.add(Long.valueOf(messageId));

			}
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return idList;

	}

	/**
	 * 保存邮件地址
	 * 
	 * @param message
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-26
	 */
	private void saveMailAddress(C35Message message) {
		Debug.v(TAG_DBOPER, "saveMailAddress");
		List<String> addressList = new ArrayList<String>();
		addressList.addAll(message.getTo());
		addressList.addAll(message.getCc());
		addressList.addAll(message.getBcc());
		addressList.add(message.getFrom());
		String uid = message.getMailId();
		String sendTime = message.getSendTime();
		Long sendDate = System.currentTimeMillis();
		if (sendTime != null && sendTime.length() > 0) {
			// SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date sDate = null;
			try {
				sDate = EmailApplication.DateFormatYMDHMS.parse(sendTime);
			} catch (ParseException e) {
				Debug.e("failfast", "failfast_AA", e);
			}
			sendDate = (sDate != null ? sDate.getTime() : System.currentTimeMillis());
		}
		getMDB().beginTransaction();
		for (String address : addressList) {
			getMDB().execSQL("insert into query_mailaddress(query_uid, mail_address,date) values(?,?,?)", new Object[] { uid, address, sendDate });
		}
		getMDB().setTransactionSuccessful();
		getMDB().endTransaction();
	}

	/**
	 * 获取收藏
	 * 
	 * @param account
	 * @param uid
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-26
	 */
	public int getFavorite(Account account, String uid) {
		Debug.v(TAG_DBOPER, "getFavorite(Account account, String uid)");
		Cursor cursor = null;
		try {
			mSBufferSQLTemp.setLength(0);
			mSBufferSQLTemp.append("SELECT favorite from ");
			mSBufferSQLTemp.append(TBL_MESSAGES);
			mSBufferSQLTemp.append(" where uid=? AND account_id = (SELECT id FROM account WHERE uuid = ? ) ");
			// cursor =
			// getMDB().rawQuery("SELECT favorite from messages where uid=? AND account_id = (SELECT id FROM account WHERE uuid = ? ) ",
			// new String[] { uid, account.getUuid() });
			cursor = getMDB().rawQuery(mSBufferSQLTemp.toString(), new String[] { uid, account.getUuid() });
			if (cursor.moveToFirst()) {
				int result = cursor.getInt(0);
				return result;
			}
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return -1;
	}

	/**
	 * 收藏
	 * 
	 * @param id
	 * @param newFav
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-26
	 */
	public void setFavorite(Long id, boolean newFav) {
		Debug.v(TAG_DBOPER, "setFavorite(Long id, boolean newFav)");
		Integer fav = newFav ? C35Message.FAVORITE_FLAG_FAV : C35Message.FAVORITE_FLAG_NORMOR;
		try {
			getMDB().execSQL("UPDATE messages set favorite = ? WHERE id = ? ", new Object[] { fav, id });
		} catch (SQLException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 批量收藏
	 * 
	 * @param ids
	 * @param newFav
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-26
	 */
	public void setFavorite(Set<Long> ids, boolean newFav) {
		Debug.v(TAG_DBOPER, "setFavorite(Set<Long> ids, boolean newFav)");
		Integer fav = newFav ? C35Message.FAVORITE_FLAG_FAV : C35Message.FAVORITE_FLAG_NORMOR;
		try {
			getMDB().beginTransaction();
			for (Long id : ids) {
				getMDB().execSQL("UPDATE messages set favorite = ? WHERE id = ? ", new Object[] { fav, id });
			}
			getMDB().setTransactionSuccessful();
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			getMDB().endTransaction();
		}
	}

	/**
	 * 收藏
	 * 
	 * @param account
	 * @param newFav
	 * @param uid
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-26
	 */
	public void setFavorite(Account account, boolean newFav, String uid) {
		Debug.v(TAG_DBOPER, "setFavorite(Account account, boolean newFav, String uid)");
		Integer fav = newFav ? C35Message.FAVORITE_FLAG_FAV : C35Message.FAVORITE_FLAG_NORMOR;
		try {
			getMDB().execSQL("UPDATE messages set favorite = ? WHERE uid = ? AND account_id = (SELECT id FROM account WHERE uuid = ? ) ", new Object[] { fav, uid, account.getUuid() });
		} catch (SQLException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 获取删除标志
	 * 
	 * @param id
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-26
	 */
	public int getDeleteFlag(Long id) {
		Debug.v(TAG_DBOPER, "getDeleteFlag(Long id)");
		int result = 2;
		Cursor cursor = getMDB().rawQuery("SELECT delete_flag FROM messages WHERE id = ?", new String[] { id.toString() });
		if (cursor.moveToFirst()) {
			result = cursor.getInt(0);
		}
		cursor.close();
		return result;
	}

	/**
	 * 设置已读未读
	 * 
	 * @param id
	 * @param newRead
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-26
	 */
	public void setRead(Long id, boolean newRead) {
		Debug.v(TAG_DBOPER, "setRead(Long id, boolean newRead)");
		Integer fav = newRead ? C35Message.READ_FLAG_SEEN : C35Message.READ_FLAG_UNREAD;
		try {
			getMDB().execSQL("UPDATE messages set read_flag = ? WHERE id = ? ", new Object[] { fav, id });
		} catch (SQLException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 设置已读未读
	 * 
	 * @param account
	 * @param uid
	 * @param newRead
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-26
	 */
	private void setRead(Account account, String uid, int newRead) {
		Debug.v(TAG_DBOPER, " setRead(Account account, String uid, int newRead) ");
		try {
			getMDB().execSQL("UPDATE messages set read_flag = ? WHERE uid = ? AND account_id = (SELECT id FROM account WHERE uuid = ? ) ", new Object[] { newRead, uid, account.getUuid() });
		} catch (SQLException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 设置已读未读(批量)
	 * 
	 * @param ids
	 * @param newRead
	 * @throws Exception
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-26
	 */
	public void setRead(Set<Long> ids, boolean newRead) throws Exception {
		Debug.v(TAG_DBOPER, "setRead(Set<Long> ids, boolean newRead) ");
		Integer fav = newRead ? C35Message.READ_FLAG_SEEN : C35Message.READ_FLAG_UNREAD;
		try {
			getMDB().beginTransaction();
			for (Long id : ids) {
				getMDB().execSQL("UPDATE messages set read_flag = ? WHERE id = ? ", new Object[] { fav, id });
			}
			getMDB().setTransactionSuccessful();
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			getMDB().endTransaction();
		}
	}

	/**
	 * UPDATE DownloadFlag
	 * 
	 * @author xulei
	 * @date Dec 19, 2011
	 * @return void
	 * @throws
	 */
	public void setDownloadFlag(Account account, String uid, int downloadFlag) {
		Debug.v(TAG_DBOPER, "setDownloadFlag(Account account, String uid, int downloadFlag)");
		getMDB().execSQL("UPDATE messages set down_flag = ? WHERE uid = ? AND account_id=(SELECT id FROM account WHERE uuid = ? ) ", new Object[] { downloadFlag, uid, account.getUuid() });
	}

	/**
	 * 获得收件箱邮件未读个数
	 * 
	 * @param account
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-28
	 */
	public int getInboxUnreadCount(Account account) {
		Debug.v(TAG_DBOPER, "getInboxUnreadCount(Account account)");
		int count = 0;
		Cursor cursor = null;
		try {
			mSBufferSQLTemp.setLength(0);
			mSBufferSQLTemp.append("SELECT COUNT(*)  FROM ");
			mSBufferSQLTemp.append(TBL_MESSAGES);
			mSBufferSQLTemp.append(" WHERE folder_id = (SELECT id FROM folders WHERE folderId = ? AND account_id = (SELECT id FROM account WHERE uuid = ? ) )  AND delete_flag =0  and read_flag = 0 ");
			// String sql = "SELECT COUNT(*)  FROM messages " +
			// "WHERE folder_id = (SELECT id FROM folders WHERE folderId = ? AND account_id = (SELECT id FROM account WHERE uuid = ? ) )  AND delete_flag =0  and read_flag = 0 ";
			cursor = getMDB().rawQuery(mSBufferSQLTemp.toString(), new String[] { EmailApplication.MAILBOX_INBOX, account.getUuid() });
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();

			}

		}

		return count;
	}

	/**
	 * 根据folderId获得邮件未读个数
	 * 
	 * @return
	 * @see:
	 * @since:
	 */
	public int getUnreadCountByAccountAndFolderId(String folderId) {
		Debug.v(TAG_DBOPER, "getUnreadCountByAccountAndFolderId(String folderId) ");
		int count = 0;
		Cursor cursor = null;
		try {
			mSBufferSQLTemp.setLength(0);
			mSBufferSQLTemp.append("SELECT COUNT(*)  FROM ");
			mSBufferSQLTemp.append(TBL_MESSAGES);
			mSBufferSQLTemp.append(" WHERE folder_id = ?  AND delete_flag =0  and read_flag = 0 ");
			// String sql = "SELECT COUNT(*)  FROM messages " +
			// "WHERE folder_id = ?  AND delete_flag =0  and read_flag = 0 ";
			cursor = getMDB().rawQuery(mSBufferSQLTemp.toString(), new String[] { folderId });
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}

		}

		return count;
	}

	/**
	 * 获得收件箱邮件总数
	 * 
	 * @param:strFolderId 邮箱id
	 * @return:
	 * @throws:
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2012-10-16
	 */
	public int getFolderMailsCountLocal(Account account, String strFolderId) {
		Debug.v(TAG_DBOPER, "getFolderMailsCountLocal(Account account, String strFolderId");
		int count = 0;
		Cursor cursor = null;
		// String sql = "";
		try {
			if (strFolderId.equals(EmailApplication.MAILBOX_TRASHBOX)) {// 已删除箱
				mSBufferSQLTemp.setLength(0);
				mSBufferSQLTemp.append("SELECT COUNT(*)  FROM ");
				mSBufferSQLTemp.append(TBL_MESSAGES);
				mSBufferSQLTemp.append(" WHERE account_id = (SELECT id FROM account WHERE uuid = ? )  AND delete_flag =1  ");
				// sql = "SELECT COUNT(*)  FROM messages " +
				// "WHERE account_id = (SELECT id FROM account WHERE uuid = ? )  AND delete_flag =1  ";
				cursor = getMDB().rawQuery(mSBufferSQLTemp.toString(), new String[] { account.getUuid() });
			} else {
				mSBufferSQLTemp.setLength(0);
				mSBufferSQLTemp.append("SELECT COUNT(*)  FROM ");
				mSBufferSQLTemp.append(TBL_MESSAGES);
				mSBufferSQLTemp.append(" WHERE folder_id = (SELECT id FROM folders WHERE folderId = ? AND account_id = (SELECT id FROM account WHERE uuid = ? ) )  AND delete_flag =0  ");
				// sql = "SELECT COUNT(*)  FROM messages " +
				// "WHERE folder_id = (SELECT id FROM folders WHERE folderId = ? AND account_id = (SELECT id FROM account WHERE uuid = ? ) )  AND delete_flag =0  ";
				cursor = getMDB().rawQuery(mSBufferSQLTemp.toString(), new String[] { strFolderId, account.getUuid() });
			}

			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();

			}
		}

		return count;
	}

	/**
	 * work for widget
	 * 
	 * @author xulei
	 * @date Dec 15, 2011
	 * @return Cursor
	 * @throws
	 */
	public Cursor getInboxMessages(Account account) {
		Debug.v(TAG_DBOPER, "getInboxMessages(Account account)");
		Cursor cursor = null;
		mSBufferSQLTemp.setLength(0);
		mSBufferSQLTemp.append("SELECT uid,read_flag FROM ");
		mSBufferSQLTemp.append(TBL_MESSAGES);
		mSBufferSQLTemp.append(" WHERE folder_id = (SELECT id FROM folders WHERE folderId = ? AND account_id =(SELECT id FROM account WHERE uuid = ? ) )  AND delete_flag ==0  ORDER BY date DESC LIMIT 20");

		// String sql = "SELECT uid,read_flag FROM messages " +
		// "WHERE folder_id = (SELECT id FROM folders WHERE folderId = ? AND account_id =(SELECT id FROM account WHERE uuid = ? ) )  AND delete_flag ==0  ORDER BY date DESC LIMIT 20";
		cursor = getMDB().rawQuery(mSBufferSQLTemp.toString(), new String[] { EmailApplication.MAILBOX_INBOX, account.getUuid() });
		return cursor;
	}

	/**
	 * work for widget
	 * 
	 * @author xulei
	 * @date Dec 15, 2011
	 * @return Cursor
	 * @throws
	 */
	public Cursor getInboxMessage(Account account, String uid) {
		Debug.v(TAG_DBOPER, "getInboxMessage(Account account, String uid)");
		Cursor cursor = null;
		String sql = "SELECT uid, read_flag, date,sender_list,attachment_count,favorite,preview,subject FROM messages WHERE folder_id = (SELECT id FROM folders WHERE folderId = ? AND account_id =(SELECT id FROM account WHERE uuid = ? )  )  AND delete_flag=0 " + (uid == null ? "ORDER BY date DESC LIMIT 1" : "AND uid = ?");
		cursor = getMDB().rawQuery(sql, uid == null ? null : new String[] { EmailApplication.MAILBOX_INBOX, account.getUuid(), uid });
		return cursor;
	}

	/**
	 * 获得下一封邮件
	 * 
	 * @param account
	 * @param uid
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-28
	 */
	public String getNextUid(Account account, String uid) {
		Debug.v(TAG_DBOPER, " getNextUid(Account account, String uid)");
		String result = null;
		Cursor cursor = null;
		try {

			String sql = "SELECT uid FROM  messages WHERE folder_id=(SELECT id FROM folders WHERE folderId = ? AND account_id = (SELECT id FROM account WHERE uuid = ? ) ) AND delete_flag=0 ORDER BY date DESC;";
			cursor = getMDB().rawQuery(sql, new String[] { EmailApplication.MAILBOX_INBOX, account.getUuid() });
			while (cursor.moveToNext()) {
				if (uid.equals(cursor.getString(0)) && cursor.moveToNext()) {
					result = cursor.getString(0);
					break;
				}
			}
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();

			}

		}

		return result;
	}

	/**
	 * 保存邮件
	 * 
	 * @param message
	 * @param folder
	 * @param account
	 * @return MailId
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-28
	 */
	public String saveMessages(C35Message message, String folder, Account account) throws MessagingException {
		Debug.v(TAG_DBOPER, "saveMessages(C35Message message, String folder, Account account) throws MessagingException");
		Cursor cursor = null;
		try {
			long startTime = System.currentTimeMillis();
			// long beginTime = startTime;
			long endTime = 0;
			long folderId;

			folderId = getIdByFolderUuidAndAccountID(this.getAccountIdByUuid(account.getUuid()), message.getFolderId());
			// folderId = createFolder(account.getUuid(), folder);
			// getFolder(folderName).open(OpenMode.READ_WRITE);
			String uid = message.getMailId();
			if (uid == null || uid.trim().equals("")) {
				message.setMailId("Local" + UUID.randomUUID().toString());
				Debug.d(TAG, "mailID = " + message.getMailId());
			}
			ContentValues cv = new ContentValues();
			ContentValues body = new ContentValues();
			cv.put("folder_id", folderId);
			if (message.getSubject() != null && !"".equals(message.getSubject())) {
				cv.put("subject", message.getSubject().trim());
			} else {
				cv.put("subject", "");
			}
			cv.put("uid", message.getMailId());
			String sendTime = message.getSendTime();
			long date;
			if (sendTime == null || sendTime.equals("")) {
				date = System.currentTimeMillis();
			} else {
				try {
					// SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					date = EmailApplication.DateFormatYMDHMS.parse(sendTime).getTime();
				} catch (ParseException e) {
					date = System.currentTimeMillis();
				}
			}
			cv.put("date", date);
			saveFrom(message, cv);
			saveRepiy(message, cv);
			saveToList(message, cv);
			saveCcList(message, cv);
			saveBccList(message, cv);
			saveReaderList(message, cv);
			if (message.getDownFalg() == null) {
				cv.put("down_flag", C35Message.DOWNLOAD_FLAG_ENVELOPE);
			} else {
				cv.put("down_flag", message.getDownFalg());
			}
			// 邮件头已经有了
			// if (message.getRead() == null) {
			// cv.put("read_flag", C35Message.READ_FLAG_UNREAD);
			// } else {
			// cv.put("read_flag", message.getRead());
			// }
			if (message.getForward_replay_Falg() == null) {
				cv.put("forward_replay_flag", 0);
			} else {
				cv.put("forward_replay_flag", message.getForward_replay_Falg());
			}
			if (message.getDeleteFalg() == null) {
				if (message.getMailId() != null) {
					cursor = getMDB().rawQuery("select delete_flag from messages where uid = ? and account_id =(SELECT id FROM account WHERE uuid = ? ) ", new String[] { message.getMailId(), account.getUuid() + "" });
					if (cursor != null && cursor.getCount() > 0) {
						cursor.moveToFirst();
						cv.put("delete_flag", cursor.getInt(cursor.getColumnIndex("delete_flag")));
					} else {
						cv.put("delete_flag", 0);
					}
				} else {
					cv.put("delete_flag", 0);
				}
			} else {
				cv.put("delete_flag", message.getDeleteFalg());
			}
			if (message.getDeliverStatus() == null) {
				cv.put("send_flag", 0);
			} else {
				cv.put("send_flag", message.getDeliverStatus());
			}
			cv.put("remail_id", message.getReMailId());
			cv.put("send_type", message.getSendType());
			cv.put("mail_size", message.getSize());
			cv.put("attachment_count", message.getAttachSize());
			cv.put("favorite", message.getImportantFlag());

			cv.put("mailType", message.getMailType());
			cv.put("calendarState", message.getCalendarState());
			cv.put("calendarStartTime", message.getCalendarStartTime());
			cv.put("calendarEndTime", message.getCalendarEndTime());
			cv.put("calendarLocation", message.getCalendarLocation());

			cv.put("compressedToSize", message.getCompressedToSize());
			cv.put("reader_count", message.getDeliveredReadCount());
			startTime = System.currentTimeMillis();
			String html = message.getHyperText();
			body.put("html_content", html.length() > 0 ? html : null);
			String text = message.getPlainText();
			body.put("text_content", text.length() > 0 ? text : null);
			if (!folder.equals(EmailApplication.MAILBOX_INBOX)) {
				if (text != null && !"".equals(text)) {
					String htmlText = HtmlContentUtil.HtmltoText(text);
					cv.put("preview", htmlText.length() > 349 ? htmlText.substring(0, 349) : htmlText);
				} else {
					String htmlText = HtmlContentUtil.HtmltoText(html.toString());
					cv.put("preview", htmlText.length() > 349 ? htmlText.substring(0, 349) : htmlText);
				}
			}
			cv.put("account_id", getAccountIdByUuid(account.getUuid()));
			startTime = System.currentTimeMillis();

			cursor = getMDB().rawQuery("select id from messages where uid = ? and account_id =(SELECT id FROM account WHERE uuid = ? ) ", new String[] { message.getMailId(), account.getUuid() + "" });
			Long message_id = null;
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				message_id = cursor.getLong(cursor.getColumnIndex("id"));

			}

			if (message_id != null) {
				// Log.d(TAG, "update messages!!!!");
				getMDB().update("messages", cv, "uid=? and delete_flag<2 and account_id = ?", new String[] { message.getMailId(), String.valueOf(getAccountIdByUuid(account.getUuid())) });
			} else {
				// Log.d(TAG, "insert messages!!!!");
				getMDB().insert("messages", null, cv);
				cursor = getMDB().rawQuery("select id from messages where uid = ? and account_id =?", new String[] { message.getMailId(), getAccountIdByUuid(account.getUuid()) + "" });
				if (cursor.moveToNext()) {
					message_id = cursor.getLong(cursor.getColumnIndex("id"));
				}
				// cursorOther.close();
			}

			// cursor.close();
			body.put("message_id", message_id.intValue());
			cursor = getMDB().rawQuery("select message_id from body where message_id =?", new String[] { message_id + "" });
			body.put("html_content", html.length() > 0 ? html : null);
			if (cursor.moveToNext()) {
				// Log.d(TAG, "update body!!!!");
				getMDB().update("body", body, "message_id=?", new String[] { message_id + "" });
			} else {
				// Log.d(TAG, "insert body!!!!");
				getMDB().insert("body", "", body);
			}
			saveMailAddress(message);

			endTime = System.currentTimeMillis();
			Debug.i("==", "db time=" + (endTime - startTime));
			return message.getMailId();
		} catch (Exception e) {
			C35AppServiceUtil.writeSubscribeInformationToSdcard("saveMessages: " + e.toString());// 彩蛋log写入
			return e.toString(); // TODO 临时解决
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	private void saveBccList(C35Message message, ContentValues cv) {
		// Debug.v(TAG_DBOPER, "saveBccList");
		if (!message.getBcc().isEmpty()) {
			cv.put("bcc_list", makeAddressString(message.getBcc()));
		} else {
			cv.put("bcc_list", "");
		}
	}

	private void saveCcList(C35Message message, ContentValues cv) {
		// Debug.v(TAG_DBOPER, "saveCcList");
		if (!message.getCc().isEmpty()) {
			cv.put("cc_list", makeAddressString(message.getCc()));
		} else {
			cv.put("cc_list", "");
		}
	}

	/**
	 * List [pm1 <pm1@35.cn>,pm2@35.cn, pm3@35.cn]
	 * 
	 * @param message
	 * @param cv
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-12-11
	 */
	private void saveToList(C35Message message, ContentValues cv) {
		// Debug.v(TAG_DBOPER, "saveToList");
		if (!message.getTo().isEmpty()) {
			cv.put("to_list", makeAddressString(message.getTo()));
		} else {
			cv.put("to_list", "");
		}
	}

	private void saveFrom(C35Message message, ContentValues cv) {
		// Debug.v(TAG_DBOPER, "saveFrom");
		if (message.getFrom() != null && !"".equals(message.getFrom())) {
			String from = message.getFrom().replaceAll("\"", "").replace("\\", "").replace("  ", " ");
			if (from.split(" ").length == 1) {
				if (!from.contains("<")) {
					String name = subAtChar(from);
					cv.put("sender_list", name + " <" + from + ">");
				} else {
					String name = "";
					int location = from.indexOf("<");
					if (location == 0) {
						name = from.replace("<", "").split("@")[0];
					}
					cv.put("sender_list", name + " " + from);
				}
			} else {
				cv.put("sender_list", from);

			}
		} else {
			cv.put("sender_list", "");
		}
	}

	private void saveRepiy(C35Message message, ContentValues cv) {
		// Debug.v(TAG_DBOPER, "saveRepiy");
		if (message.getReplyTo() != null && !"".equals(message.getReplyTo())) {
			String reply = message.getReplyTo().replaceAll(" {2,}", " ");
			if (reply.contains("\"")) {
				reply = reply.replaceAll("\"", "").trim();
			}
			if (reply.split(" ").length == 1) {
				if (!reply.contains("<")) {
					String name = subAtChar(reply);
					cv.put("reply_to_list", name + " <" + reply + ">");
				} else {
					String name = reply.replace("<", "").split("@")[0];
					cv.put("reply_to_list", name + " " + reply);
				}
			} else {
				cv.put("reply_to_list", reply);
			}
		} else {
			cv.put("reply_to_list", "");
		}
	}

	private void saveReaderList(C35Message message, ContentValues cv) {
		// Debug.v(TAG_DBOPER, "saveReaderList");
		cv.put("reader_list", makeReaderListString(message.getDeliveredReadUsers()));
	}

	/**
	 * 已读的人转换成分号间隔，存入数据库
	 * 
	 * @Description:
	 * @param list
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-6-2
	 */
	private String makeReaderListString(List<String> list) {
		// Debug.v(TAG_DBOPER, "makeReaderListString(List<String> list)");
		mSBufferSQLTemp.setLength(0);
		// String result = "";ss
		if (list != null && list.size() > 0) {
			for (String email : list) {
				// result = result + email + ";";
				mSBufferSQLTemp.append(email).append(";");
			}
		}
		return mSBufferSQLTemp.toString();
	}

	/**
	 * list to string
	 * 
	 * @Description:
	 * @param list
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-12-17
	 */
	private String makeAddressString(List<String> list) {
		// Debug.v(TAG_DBOPER, "makeAddressString");
		String tos = "";
		if (list != null && !list.isEmpty()) {
			StringBuffer toStringBuffer = new StringBuffer();
			String[] toString = list.toString().replace("[", "").replace("]", "").replaceAll("\"", "").replace(" {2,}", " ").split(",");
			for (String to : toString) {

				if (!to.contains("<")) {
					String name = subAtChar(to);
					toStringBuffer.append(name + " <" + to + ">").append("");
				} else {
					if (to.startsWith("<")) {
						String name = subAtChar(to);
						toStringBuffer.append(name + " " + to).append("");
					} else {
						toStringBuffer.append(to).append("");
					}
				}
			}
			tos = toStringBuffer.deleteCharAt(toStringBuffer.length() - 1).toString();
		}
		return tos;
	}

	/**
	 * createFolder
	 * 
	 * @Description:
	 * @param accountId
	 * @param folderId
	 * @return the row ID
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-15
	 */
	private int createFolder(long accountId, String folderId) throws MessagingException {
		synchronized (createFolderLock) {
			int id = -1;
			Cursor cursor = null;
			try {
				if (folderId.equals(EmailApplication.MAILBOX_INBOX) || folderId.equals(EmailApplication.MAILBOX_OUTBOX) || folderId.equals(EmailApplication.MAILBOX_SENTBOX) || folderId.equals(EmailApplication.MAILBOX_TRASHBOX) || folderId.equals(EmailApplication.MAILBOX_FAVORITEBOX) || folderId.equals(EmailApplication.MAILBOX_DRAFTSBOX) || folderId.equals(EmailApplication.MAILBOX_ATTACHMENTBOX))
					if (mFolderAccountMap.get(accountId + folderId) != null && mFolderAccountMap.get(accountId + folderId) > 0) {// 说明有数据不用insert了
						return mFolderAccountMap.get(accountId + folderId);
					}
				Debug.v(TAG_DBOPER, "createFolder(String accountUid, String folderId)" + folderId);
				// long accountId = getAccountIdByUuid(accountUid);
				cursor = getMDB().rawQuery("SELECT id FROM folders WHERE folderId = ? AND account_id= ?", new String[] { folderId, String.valueOf(accountId) });
				while (cursor.moveToNext()) {// ？？
					id = cursor.getInt(0);
				}
				Debug.i(TAG, "folder__________id_tr_______mid:" + id);
				if (id <= 0) {// 没找到
					ContentValues values = new ContentValues();
					values.put("account_id", accountId);
					values.put("name", folderId);
					values.put("last_updated", System.currentTimeMillis());
					values.put("folderId", folderId);
					if (folderId.equals(EmailApplication.MAILBOX_INBOX)) {// 给不同的箱子设置不同的显示值
						values.put("orderValue", EmailApplication.MAIBOX_TYPE_ORDERVALUE_INBOX);
					} else if (folderId.equals(EmailApplication.MAILBOX_OUTBOX)) {
						values.put("orderValue", EmailApplication.MAIBOX_TYPE_ORDERVALUE_OUTBOX);
					} else if (folderId.equals(EmailApplication.MAILBOX_SENTBOX)) {
						values.put("orderValue", EmailApplication.MAIBOX_TYPE_ORDERVALUE_SENTBOX);
					} else if (folderId.equals(EmailApplication.MAILBOX_TRASHBOX)) {
						values.put("orderValue", EmailApplication.MAIBOX_TYPE_ORDERVALUE_TRASHBOX);
					} else if (folderId.equals(EmailApplication.MAILBOX_FAVORITEBOX)) {
						values.put("orderValue", EmailApplication.MAIBOX_TYPE_ORDERVALUE_FAVORITEBOX);
					} else if (folderId.equals(EmailApplication.MAILBOX_DRAFTSBOX)) {
						values.put("orderValue", EmailApplication.MAIBOX_TYPE_ORDERVALUE_DRAFTSBOX);
					} else if (folderId.equals(EmailApplication.MAILBOX_ATTACHMENTBOX)) {
						values.put("orderValue", EmailApplication.MAIBOX_TYPE_ORDERVALUE_ATTACHMENTBOX);
					}
					values.put("parentId", "0");
					if (folderId.equals(EmailApplication.MAILBOX_INBOX) || folderId.equals(EmailApplication.MAILBOX_OUTBOX) || folderId.equals(EmailApplication.MAILBOX_SENTBOX) || folderId.equals(EmailApplication.MAILBOX_TRASHBOX) || folderId.equals(EmailApplication.MAILBOX_FAVORITEBOX) || folderId.equals(EmailApplication.MAILBOX_DRAFTSBOX) || folderId.equals(EmailApplication.MAILBOX_ATTACHMENTBOX)) {
						values.put("folderType", EmailApplication.MAILBOX_TYPE_DEFAULT);
					} else {// 自定义文件夹
						values.put("folderType", EmailApplication.MAILBOX_TYPE_SELF);
					}
					id = (int) getMDB().insert("folders", null, values);
					mFolderAccountMap.put(accountId + folderId, id);
					Debug.i(TAG, "create folder --> " + folderId);
				} else {
					mFolderAccountMap.put(accountId + folderId, id);
					return id;
				}
			} catch (Exception e) {
				Debug.e("failfast", "failfast_AA", e);
				C35AppServiceUtil.writeSubscribeInformationToSdcard("createFolder: " + e.toString());// 彩蛋log写入
				throw new MessagingException("unable to initFolders:" + e.getMessage());
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			return id;

		}

	}

	/**
	 * 同步自定义文件夹
	 * 
	 * @param account
	 * @see:
	 * @since:
	 * @author: gongfacun
	 * @throws Exception
	 * @date:2012-11-14
	 */
	public void createselfFolder(Account account) throws MessagingException {
		// TODO:sofia2.0环境不支持此接口。//文件夹名会比较怪异，要不还是不放开@吴頔
		Boolean result = AccountUtil.isSupportRequest("getSelfDefinedFolderList", account);
		if (result == null || result == false) {
			return;
		}
		Debug.v(TAG_DBOPER, "createselfFolder");
		Cursor cursor = null;// 用来存储本地的文件夹数据
		Cursor qcursor = null;// 用来存储网络文件夹数据
		try {
			C35Store folderStore = (C35Store) Store.getInstance(account.getStoreUri());
			List<C35Folder> listSelfDefined = folderStore.getFolderList(folderStore.getTiket(), GetFolderType.SELFDEFINED); // 得到服务器中的邮件夹列表
			List<C35Folder> listLocal = new ArrayList<C35Folder>();// 获得本地文件夹列表
			long accountId = getAccountIdByUuid(account.getUuid());// 获得账户的数据存储id
			getMDB().beginTransaction();// 数据库事务
			cursor = getMDB().rawQuery("SELECT folderId , name , orderValue ,parentId ,folderType FROM folders WHERE account_id = ? and folderType = ?", new String[] { String.valueOf(accountId), String.valueOf(EmailApplication.MAILBOX_TYPE_SELF) });
			String sql = SQL_I_INTO + TBL_FOLDERS + " (account_id , name ,  last_updated ,folderId ,orderValue ,parentId ,folderType) values (?,?,?,?,?,?,?)";
			if ((!cursor.moveToNext()) && listSelfDefined != null)// 如果本地没有任何数据且服务器端有数据则直接把数据插入数据库
			{
				for (C35Folder folder : listSelfDefined) {
					getMDB().execSQL(sql, new Object[] { accountId, folder.getFolderName(), System.currentTimeMillis(), folder.getFolderId(), folder.getOrderValue(), folder.getParentId(), folder.getFolderType() });
				}
			} else {// 本地有数据则本地与网络数据进行比对
				// Debug.v(TAG, "selffolder have folder___________________");
				cursor.moveToPosition(-1);
				while (cursor.moveToNext()) {// 把数据存储到list中好对数据进行比较
					C35Folder folder = new C35Folder();
					folder.setFolderId(cursor.getString(0));
					folder.setFolderName(cursor.getString(1));
					folder.setOrderValue(cursor.getLong(2));
					folder.setParentId(cursor.getString(3));
					folder.setFolderType(cursor.getInt(4));
					// Debug.v(TAG, "cursor.getString(0):" + cursor.getString(0)
					// + ":" + cursor.getString(1) +
					// ":" + cursor.getInt(2) + ":" + cursor.getString(3) + ":"
					// + cursor.getInt(4));
					listLocal.add(folder);
				}
				if (listSelfDefined != null) {
					List<C35Folder> listtemp = new ArrayList<C35Folder>();// 存储远程的临时数据
					List<C35Folder> listtemp1 = new ArrayList<C35Folder>();// 存储本地的临时数据
					String upsql = "UPDATE folders set name = ? WHERE folderid= ?";
					String upOrderSql = "UPDATE folders set orderValue = ? WHERE folderid= ?";
					String upParentSql = "UPDATE folders set parentId = ? WHERE folderid= ?";
					for (C35Folder folder : listLocal) {// 通过双层循环对数据进行比较

						Debug.v(TAG, "listlocal+folder:" + folder.getFolderId() + ":" + folder.getFolderName() + ":" + folder.getFolderType());
						for (C35Folder folder1 : listSelfDefined) {
							// Debug.v(TAG, "listselfdefined+folder:" +
							// folder1.getFolderId() + ":" +
							// folder1.getFolderName() + ":" +
							// folder1.getFolderType());
							if (folder.getFolderId().equals(folder1.getFolderId())) {// 如果网络数据与本地数据相同则进行保存，由于当时用一个临时的list不可以，才用了两个临时的list
								listtemp.add(folder);// 远程的list
								listtemp1.add(folder1);// 本地list
								if (!folder.getFolderName().equals(folder1.getFolderName())) {// 文件夹id相同，名称不同用服务器名称替换掉本地名称
									getMDB().execSQL(upsql, new Object[] { folder1.getFolderName(), folder.getFolderId() });
								}
								if (!(folder.getOrderValue() == folder1.getOrderValue())) {// 文件夹id相同，排序不同用服务器的序列号替换本地的序列号
									getMDB().execSQL(upOrderSql, new Object[] { folder1.getOrderValue(), folder.getFolderId() });
								}
								if (!(folder.getParentId().equals(folder1.getParentId()))) {// 文件夹id相同，父文件夹id不同用服务器的id替换本地的id
									getMDB().execSQL(upParentSql, new Object[] { folder1.getParentId(), folder.getFolderId() });
								}
								break;
							}
						}
					}

					for (C35Folder folders : listtemp) {// 去掉相同的部分
						listLocal.remove(folders);
					}
					for (C35Folder folder : listtemp1) {// 去掉相同的部分
						listSelfDefined.remove(folder);
					}
					// Debug.v(TAG, "listlocal.size:" + listlocal.size());
					// Debug.v(TAG, "listselfdefined.size:" +
					// listselfdefined.size());
					String qurysql = "select count(*) from folders where account_id = ? and folderId = ? and name = ? ";

					for (C35Folder list : listSelfDefined) {// 把本地没有的文件夹插入到本地数据库
						qcursor = getMDB().rawQuery(qurysql, new String[] { String.valueOf(accountId), list.getFolderId(), list.getFolderName() });
						if (qcursor.moveToNext() && qcursor.getInt(0) > 0) {// 防止重复进行验证

						} else {
							// Debug.v(TAG, "addlist__list.getFolderName():" +
							// list.getFolderName() + ":" +
							// list.getFolderId() + ":" + list.getOrderValue() +
							// ":" + list.getParentId() +
							// ":" +
							// list.getFolderType());
							getMDB().execSQL(sql, new Object[] { accountId, list.getFolderName(), System.currentTimeMillis(), list.getFolderId(), list.getOrderValue(), list.getParentId(), list.getFolderType() });// 插入数据库数据
						}
					}
				} else {// 从服务器获取数据为空时，在彩蛋中进行记录
					C35AppServiceUtil.writeSubscribeInformationToSdcard("LocalStore message::servces return selffolder is null " + "||response is null?:" + C35Store.responseIsNull);// 彩蛋log写入
					Debug.e("servcesReturnSelffolderIsNull", "response is null?::" + C35Store.responseIsNull, new Exception());
					C35Store.responseIsNull = false;
				}
				String rsql = SQL_DEL_FROM + "folders WHERE account_id = ? and folderId = ? and name = ?";
				String rfsql = "select id from messages where folder_id=?";
				for (C35Folder rlist : listLocal) {// 删除服务器上没有的数据
					// Debug.v(TAG, "rlist.getFolderId():" + rlist.getFolderId()
					// + " !!rlist.getFolderName():"
					// + rlist.getFolderName());
					getMDB().execSQL(rsql, new Object[] { accountId, rlist.getFolderId(), rlist.getFolderName() });// 删除文件夹列表中数据
					Cursor delcursor = getMDB().rawQuery(rfsql, new String[] { String.valueOf(rlist.getFolderId()) });// 查询邮件列表中的数据
					while (delcursor.moveToNext()) {// 循环删除邮件列表的数据
						clearMessage(account, delcursor.getColumnName(0));
					}
				}

			}
			getMDB().setTransactionSuccessful();// 提交事务

		} catch (MessagingException e) {
			throw e;
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
			throw new MessagingException(MessagingException.SYN_SELF_FOLDER_ERROR, e.getMessage());
		} finally {
			try {
				getMDB().endTransaction();// 事务完成
			} catch (Exception e) {// java.lang.IllegalStateException: Don't
									// have database lock!
				Debug.w("failfast", "failfast_AAFFFFF", e);
			}
			if (qcursor != null) {
				qcursor.close();
			}
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	/**
	 * 删除指定id的邮件到垃圾箱
	 * 
	 * @throws MessagingException
	 * @author xulei
	 * @date 2011-10-8
	 * @return void
	 * @throws
	 */
	public void deleteMessages(Set<Long> messageIds) throws Exception {
		Debug.v(TAG_DBOPER, "deleteMessages");
		getMDB().beginTransaction();
		try {
			for (Long id : messageIds) {
				getMDB().execSQL("UPDATE " + TBL_MESSAGES + " SET delete_flag = ? " + "WHERE id = ?", new Object[] { C35Message.DELETE_FLAG_DELETED, id });
			}
			getMDB().setTransactionSuccessful();
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			getMDB().endTransaction();
		}
	}

	/**
	 * 彻底删除某邮件 草稿箱等用
	 * 
	 * @author xulei
	 * @date 2011-10-8
	 * @return void
	 * @throws
	 */
	public void destoryMessages(Set<Long> messageIds) {
		Debug.v(TAG_DBOPER, "destoryMessages");
		getMDB().beginTransaction();
		try {
			for (Long id : messageIds) {
				this.clearMessage(id);
			}
			getMDB().setTransactionSuccessful();
			for (Long id : messageIds) {
				deleteAttachments(id);
			}
		} finally {
			getMDB().endTransaction();
		}

	}

	/**
	 * 还原单封邮件
	 * 
	 * @Description:
	 * @param messageId
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-11-16
	 */
	public void restoreMessage(Long messageId) {
		Debug.v(TAG_DBOPER, "restoreMessage");
		getMDB().beginTransaction();
		try {
			getMDB().execSQL("UPDATE " + TBL_MESSAGES + " SET delete_flag = 0 WHERE id = ?", new Object[] { messageId });
			getMDB().setTransactionSuccessful();
		} finally {
			getMDB().endTransaction();
		}
	}

	public void restoreMessage(Account account, String uid) {
		Debug.v(TAG_DBOPER, "restoreMessage(Account account, String uid)");
		getMDB().beginTransaction();
		try {
			getMDB().execSQL("UPDATE " + TBL_MESSAGES + " SET delete_flag = 0 WHERE uid = ? AND account_id = (SELECT id FROM account WHERE uuid = ? )", new Object[] { uid, account.getUuid() });
			getMDB().setTransactionSuccessful();
		} finally {
			getMDB().endTransaction();
		}
	}

	/**
	 * 还原指定id的邮件到原始邮件夹
	 * 
	 * @author xulei
	 * @date 2011-10-8
	 * @return void
	 * @throws
	 */
	public void restoreMessages(Set<Long> messageIds) {
		Debug.v(TAG_DBOPER, "restoreMessages(Set<Long> messageIds)");
		getMDB().beginTransaction();
		try {
			for (Long id : messageIds) {
				getMDB().execSQL("UPDATE " + TBL_MESSAGES + " SET delete_flag = 0 WHERE id = ?", new Object[] { id });
			}
			getMDB().setTransactionSuccessful();
		} finally {
			getMDB().endTransaction();
		}
		// Log.e(TAG, "restoreMessages -- end");
	}

	/**
	 * deleteMessage
	 * 
	 * @author xulei
	 * @date Dec 20, 2011
	 * @return void
	 * @throws
	 */
	public void deleteMessage(Long messageId) throws MessagingException {
		Debug.v(TAG_DBOPER, "deleteMessage(Long messageId) throws Messag");
		getMDB().execSQL("UPDATE " + TBL_MESSAGES + " SET delete_flag = ? " + "WHERE  id = ?", new Object[] { C35Message.DELETE_FLAG_DELETED, messageId });
	}

	/**
	 * 邮件移动
	 * 
	 * @param account
	 *            账号
	 * @param uid
	 *            邮件uid
	 * @param tarFolderId
	 *            目标文件夹
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2013-1-10
	 */
	public void moveMessage(Account account, String uid, String tarFolderId) {
		Debug.v(TAG_DBOPER, "moveMessage(Account account, Strin");
		long aid = this.getAccountIdByUuid(account.getUuid());
		long fid = this.getIdByFolderUuidAndAccountID(aid, tarFolderId);
		getMDB().execSQL("UPDATE " + TBL_MESSAGES + " SET folder_id = ? " + "WHERE  uid = ? AND account_id = (SELECT id FROM account WHERE uuid = ? ) ", new Object[] { fid, uid, account.getUuid() });
	}

	/**
	 * delete the message by uid
	 * 
	 * @author xulei
	 * @date Dec 20, 2011
	 * @return void
	 * @throws
	 */
	public void deleteMessage(Account account, String uid) {
		Debug.v(TAG_DBOPER, "deleteMessage(Account account, String uid)");
		getMDB().execSQL("UPDATE " + TBL_MESSAGES + " SET delete_flag = ? " + "WHERE  uid = ? AND account_id = (SELECT id FROM account WHERE uuid = ? ) ", new Object[] { C35Message.DELETE_FLAG_DELETED, uid, account.getUuid() });
	}

	/**
	 * delete 掉该条数据
	 * 
	 * @param account
	 * @param uid
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-12
	 */
	public void clearMessage(Account account, String uid) {
		Debug.v(TAG_DBOPER, "clearMessage");
		try {
			getMDB().beginTransaction();
			long id = this.getMessageIdByUid(account, uid);
			getMDB().execSQL(SQL_DEL_FROM + "query_mailaddress WHERE query_uid = (SELECT uid FROM  messages WHERE id = ?)", new Object[] { id });
			getMDB().execSQL(SQL_DEL_FROM + "body WHERE message_id = ?", new Object[] { id });
			getMDB().execSQL(SQL_DEL_FROM + "compressitems WHERE attach_id = (SELECT attachment_id FROM  attachments WHERE message_id = ?)", new Object[] { id });
			getMDB().execSQL(SQL_DEL_FROM + "attachments WHERE uid = (SELECT uid FROM  messages WHERE id = ?)", new Object[] { id });
			getMDB().execSQL(SQL_DEL_FROM + "messages where id = ? ", new Object[] { id });
			getMDB().setTransactionSuccessful();
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			getMDB().endTransaction();
		}

	}

	/**
	 * delete 掉该条数据
	 * 
	 * @param account
	 * @param id
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-12
	 */
	public void clearMessage(long id) {
		Debug.v(TAG_DBOPER, "clearMessage(long id)");
		try {
			getMDB().beginTransaction();
			getMDB().execSQL(SQL_DEL_FROM + "query_mailaddress WHERE query_uid = (SELECT uid FROM  messages WHERE id = ?)", new Object[] { id });
			getMDB().execSQL(SQL_DEL_FROM + "body WHERE message_id = ?", new Object[] { id });
			getMDB().execSQL(SQL_DEL_FROM + "compressitems WHERE attach_id = (SELECT attachment_id FROM  attachments WHERE message_id = ?)", new Object[] { id });
			getMDB().execSQL(SQL_DEL_FROM + "attachments WHERE uid = (SELECT uid FROM  messages WHERE id = ?)", new Object[] { id });
			getMDB().execSQL(SQL_DEL_FROM + "messages where id = ? ", new Object[] { id });
			getMDB().setTransactionSuccessful();
		} catch (Exception e) {
			C35AppServiceUtil.writeSubscribeInformationToSdcard("clearMessage: " + e.toString());// 彩蛋log写入
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			getMDB().endTransaction();
		}

	}

	/**
	 * 删除本地已发送邮件
	 * 
	 * @param account
	 * @param folderId
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-14
	 */
	public void clearSentMessages(Account account, String folderId) {
		Debug.v(TAG_DBOPER, "clearSentMessages(Account account, String folderId) {");
		try {
			getMDB().beginTransaction();
			List<Long> mailList = getLocalSentMessagesByFolder(account.getUuid(), folderId);
			for (long id : mailList) {
				this.clearMessage(id);
			}

			getMDB().setTransactionSuccessful();
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			getMDB().endTransaction();
		}
	}

	/**
	 * 获取本地已发送邮件夹"Local"开头邮件uid
	 * 
	 * @param accountUid
	 * @param folderId
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-26
	 */
	private List<Long> getLocalSentMessagesByFolder(String accountUid, String folderId) {
		Debug.v(TAG_DBOPER, "getLocalSentMessagesByFolder(String accountUid, String folderId)");
		long aid = this.getAccountIdByUuid(accountUid);
		long fid = this.getIdByFolderUuidAndAccountID(aid, folderId);
		String sql = "SELECT id,uid FROM " + TBL_MESSAGES + " WHERE account_id = ? and folder_id =? and delete_flag=0 ";
		List<Long> mailIds = new ArrayList<Long>();
		Cursor cursor = null;
		try {
			cursor = getMDB().rawQuery(sql, new String[] { Long.toString(aid), Long.toString(fid) });
			if (cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					long mailid = cursor.getLong(0);
					String uid = cursor.getString(1);
					if (uid.startsWith("Local")) {
						mailIds.add(mailid);
					}

				}
			}
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return mailIds;
	}

	private void deleteAttachments(Long id) {
		Debug.v(TAG_DBOPER, "deleteAttachments(Long id");
		Cursor attachmentsCursor = null;
		try {
			attachmentsCursor = getMDB().rawQuery("SELECT id FROM attachments WHERE uid = (SELECT uid FROM messages WHERE id = ? )", new String[] { id.toString() });
			while (attachmentsCursor.moveToNext()) {
				long attachmentId = attachmentsCursor.getLong(0);
				try {
					File file = new File(mAttachmentsDir, Long.toString(attachmentId));
					if (file.exists()) {
						file.delete();
					}
				} catch (Exception e) {
					Debug.w("failfast", "failfast_AA", e);
				}
			}
		} finally {
			if (attachmentsCursor != null) {
				attachmentsCursor.close();
			}
		}
	}

	/**
	 * 存储附件信息到数据库，
	 * 
	 * @Description:
	 * @param account
	 * @param att
	 * @see:
	 * @since:
	 * @author: commented by zhuanggy
	 * @date:2013-1-9
	 */
	public void storeAttachment(Account account, C35Attachment att) throws MessagingException {
		Debug.v(TAG_DBOPER, "storeAttachment(Account account, C35Attachment att) throws MessagingExceptio");
		synchronized (storeAttachmentLock) {
			String attachmentId = att.getId();
			// 获取message_id
			ContentValues cv = new ContentValues();
			Long message_id = null;
			try {
				Cursor cursorMessageId = getMDB().rawQuery("select id from " + TBL_MESSAGES + " where uid = ? and account_id = (SELECT id FROM account WHERE uuid = ? ) ", new String[] { att.getMailId(), account.getUuid() + "" });

				if (cursorMessageId.getCount() > 0) {
					cursorMessageId.moveToFirst();
					message_id = cursorMessageId.getLong(cursorMessageId.getColumnIndex("id"));
				}
				cursorMessageId.close();
			} catch (Exception e) {
				Debug.w("failfast", "failfast_AA", e);
				C35AppServiceUtil.writeSubscribeInformationToSdcard("select id from messages : " + e.getMessage());// 彩蛋log写入
				throw new MessagingException("storeAttachment error", e);
			}
			cv.put("message_id", message_id);
			cv.put("uid", att.getMailId());
			cv.put("attachment_id", att.getId());
			cv.put("size", att.getFileSize());
			cv.put("name", att.getFileName());
			cv.put("path", att.getPath());
			cv.put("duration_time", att.getTime());

			att.setDownState(att.getDownState() == 1 ? 1 : getAttachmentDownloadState(att.getId(), (int) getAccountIdByUuid(account.getUuid())));// 下载状态不为1时，获得两个表的下载状态
			cv.put("downState", att.getDownState());
			cv.put("compressItemNum", att.getCompressItemNum());

			if (att.getId().startsWith("Local")) {
				cv.put("source_attachment_id", att.getSourceAttachmentId());
				cv.put("source_message_uid", att.getSourceMessageUid());
			} else {
				cv.put("source_attachment_id", att.getId());
				cv.put("source_message_uid", att.getMailId());
			}
			String name = att.getFileName();
			String type = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
			if (att.getCid() != null && !"".equals(att.getCid().trim())) {
				cv.put("mime_type", "image/jpeg");
			} else {
				if (type.equals("jpg") || type.equals("gif") || type.equals("png") || type.equals("jpeg") || type.equals("bmp")) {
					cv.put("mime_type", "image/jpeg");
				} else {
					cv.put("mime_type", "application/octet-stream");
				}
			}
			if (att.getCid() != null) {
				if (StringUtil.isNotEmpty(att.getCid())) {
					cv.put("content_id", att.getCid());
				}
				if (!mAttachmentsDir.exists()) {
					mAttachmentsDir.mkdirs();
				}
				String contentUri = AttachmentProvider.getAttachmentUri(attachmentId).toString();
				cv.put("content_uri", contentUri);
			}
			// Debug.e(TAG, "保存附件信息!!");

			// if (att.getsID() > 0) {
			Cursor cursor = null;
			try {
				cursor = getMDB().rawQuery("select id from attachments where attachment_id = ? and uid = ? ", new String[] { att.getId(), att.getMailId() });
				if (cursor.getCount() == 0) {
					getMDB().insert("attachments", null, cv);
				} else {
					getMDB().execSQL("update attachments set downState = ? where attachment_id=?", new String[] { String.valueOf(att.getDownState()), att.getId() });
				}
			} catch (Exception e) {
				Debug.w("failfast", "failfast_AA", e);
				C35AppServiceUtil.writeSubscribeInformationToSdcard("storeAttachment1 : " + e.getMessage());// 彩蛋log写入
				throw new MessagingException("storeAttachment1 error", e);
			} finally {
				cursor.close();
			}

			// 插入attachments_lsit表:1、不是本地草稿的附件，2、不是邮件内容的内嵌图片
			if (!att.getId().startsWith("Local") && (att.getCid() == null || "".equals(att.getCid().trim()))) {
				Cursor cursor2 = null;
				try {
					cursor2 = getMDB().rawQuery("select id from attachments_list where attachment_id = ?", new String[] { att.getId() });
					if (cursor2 == null || cursor2.getCount() <= 0) {
						ContentValues cv2 = new ContentValues();
						cv2.put("account_id", getAccountIdByUuid(account.getUuid()));
						cv2.put("attachment_id", att.getId());
						cv2.put("attachment_name", att.getFileName());
						cv2.put("message_uid", att.getMailId());
						cv2.put("message_subject", att.getMailSubject());
						cv2.put("fromwho", att.getFromAddr());
						cv2.put("folder_id", att.getFolderId());
						cv2.put("download", 0);
						cv2.put("filesize", att.getFileSize());
						cv2.put("sendtime", att.getSendTime());

						getMDB().insert("attachments_list", null, cv2);
						// Debug.e(TAG, "保存附件， 插入附件表 NAME=" +
						// att.getFileName());
					} else {
						// Debug.e(TAG, "保存附件， 更新附件表 NAME=" +
						// att.getFileName());
						getMDB().execSQL("update attachments_list set download = ? where attachment_id=?", new String[] { String.valueOf(att.getDownState()), att.getId() });
					}
				} catch (Exception e) {
					Debug.w("failfast", "failfast_AA", e);
					C35AppServiceUtil.writeSubscribeInformationToSdcard("storeAttachment2 : " + e.getMessage());// 彩蛋log写入
					throw new MessagingException("storeAttachment2 error", e);
				} finally {
					if (cursor2 != null) {
						cursor2.close();
					}
				}
			}

			Debug.v(TAG, "store or update  Attachment info in db : end");
		}

	}

	/**
	 * 根据账号和邮件uid获取邮件id
	 * 
	 * @param account
	 * @param uid
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-27
	 */
	public long getMessageIdByUid(Account account, String uid) {
		Debug.v(TAG_DBOPER, "getMessageIdByUid(Account account, String uid)");
		String sql = "SELECT id FROM " + TBL_MESSAGES + " WHERE account_id = (SELECT id FROM account WHERE uuid = ? )  AND uid = ?";
		Cursor cursor = null;
		long result = -1;

		try {
			cursor = getMDB().rawQuery(sql, new String[] { account.getUuid(), uid });
			if (cursor.moveToFirst()) {
				result = cursor.getLong(0);
			}
		} catch (Exception e) {
			Debug.e(TAG, "此处报错是在收藏箱子里点击取消收藏，在view尚未刷新前，再次快速点击收藏造成的，概率很小");
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return result;
	}

	/**
	 * 根据邮件id获取uid
	 * 
	 * @param id
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-27
	 */
	public String getMessageUidById(long id) {
		Debug.v(TAG_DBOPER, " getMessageUidById(long id)");
		String sql = "SELECT uid FROM " + TBL_MESSAGES + " WHERE id = ?";
		Cursor cursor = null;
		String result = null;
		try {
			cursor = getMDB().rawQuery(sql, new String[] { String.valueOf(id) });
			if (cursor.moveToFirst()) {
				result = cursor.getString(0);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return result;
	}

	/**
	 * 获取邮件全部信息
	 * 
	 * @param account
	 * @param uid
	 * @return
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-27
	 */
	public C35Message getC35Message(Account account, String uid) throws MessagingException {
		Debug.v(TAG_DBOPER, "getC35Message(Account account, String uid)" + uid);
		C35Message c35Message = null;
		Cursor cursor = null;
		try {
			long messageId = getMessageIdByUid(account, uid);
			if (messageId != -1) {
				c35Message = new C35Message();
				cursor = getMDB().rawQuery("select me.*,bo.html_content,bo.text_content from " + TBL_MESSAGES + " as me ,body as bo where  me.[id]=? and me.[id]= bo.[message_id]", new String[] { String.valueOf(messageId) });
				if (cursor.moveToFirst()) {
					c35Message.setFolderId(cursor.getString(cursor.getColumnIndex("folder_id")));// TODO
																									// 此为序列id
					c35Message.setMailId(cursor.getString(cursor.getColumnIndex("uid")));
					c35Message.setSubject(cursor.getString(cursor.getColumnIndex("subject")));
					c35Message.setHyperText(cursor.getString(cursor.getColumnIndex("html_content")));
					c35Message.setPlainText(cursor.getString(cursor.getColumnIndex("text_content")));
					c35Message.setAttachSize(cursor.getInt(cursor.getColumnIndex("attachment_count")));
					// SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					c35Message.setSendTime(EmailApplication.DateFormatYMDHMS.format((cursor.getLong(cursor.getColumnIndex("date")))));
					c35Message.setDownFalg(cursor.getInt(cursor.getColumnIndex("down_flag")));
					c35Message.setRead(cursor.getInt(cursor.getColumnIndex("read_flag")));

					// 字段已删除
					// c35Message.setForwardFalg(cursor.getInt(cursor.getColumnIndex("forward_flag")));
					// c35Message.setReplayFalg(cursor.getInt(cursor.getColumnIndex("replay_flag")));
					c35Message.setDeliverStatus(cursor.getInt(cursor.getColumnIndex("delete_flag")));
					c35Message.setReMailId(cursor.getString(cursor.getColumnIndex("remail_id")));
					c35Message.setPreview(cursor.getString(cursor.getColumnIndex("preview")));
					c35Message.setSendType(cursor.getInt(cursor.getColumnIndex("send_type")));
					ArrayList<String> toList = new ArrayList<String>();
					String tolistSting = cursor.getString(cursor.getColumnIndex("to_list"));
					String send = cursor.getString(cursor.getColumnIndex("sender_list"));
					c35Message.setFrom(send);
					c35Message.setDeliveredReadCount(cursor.getInt(cursor.getColumnIndex("reader_count")));
					if (tolistSting != null && !"".equals(tolistSting)) {
						String[] stringAddress = cursor.getString(cursor.getColumnIndex("to_list")).split("");
						for (String string : stringAddress) {
							toList.add(string);
						}
					}
					c35Message.setTo(toList);
					ArrayList<String> ccList = new ArrayList<String>();
					String ccString = cursor.getString(cursor.getColumnIndex("cc_list"));
					if (ccString != null && !"".equals(ccString)) {
						String[] stringAddress = cursor.getString(cursor.getColumnIndex("cc_list")).split("");
						for (String string : stringAddress) {
							ccList.add(string);
						}
					}
					c35Message.setCc(ccList);
					ArrayList<String> bccList = new ArrayList<String>();
					String bccString = cursor.getString(cursor.getColumnIndex("bcc_list"));
					if (bccString != null && !"".equals(bccString)) {
						String[] stringAddress = cursor.getString(cursor.getColumnIndex("bcc_list")).split("");
						for (String string : stringAddress) {
							bccList.add(string);
						}
					}
					c35Message.setBcc(bccList);

					ArrayList<String> readerList = new ArrayList<String>();
					String readerString = cursor.getString(cursor.getColumnIndex("reader_list"));
					if (readerString != null && !"".equals(readerString)) {
						String[] stringAddress = cursor.getString(cursor.getColumnIndex("reader_list")).split(";");
						for (String string : stringAddress) {
							readerList.add(string);
						}
					}
					c35Message.setDeliveredReadUsers(readerList);

					c35Message.setAttachs(getAttachment(messageId));
					c35Message.setCompressItems(getAttachmentCompressItems(messageId));
					c35Message.setImportantFrom(cursor.getInt(cursor.getColumnIndex("is_important_from")));
					c35Message.setPriority(cursor.getInt(cursor.getColumnIndex("priority")));
				}
			}
		} catch (Exception e) {
			throw new MessagingException("unable to get the message:" + e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return c35Message;
	}

	/**
	 * 通过uid获取邮件富文本 和附件信息
	 * 
	 * @param uid
	 * @return
	 * @throws MessagingException
	 */
	public C35Message getC35MessageHtmlContent(Account account, String uid) throws MessagingException {
		Debug.v(TAG_DBOPER, "getC35MessageHtmlContent" + uid);
		C35Message c35Message = null;
		Cursor messageCursor = null;
		Cursor attachmentCursor = null;
		try {
			long messageId = getMessageIdByUid(account, uid);
			if (messageId != -1) {
				messageCursor = getMDB().rawQuery("select me.*,bo.html_content,bo.text_content from " + TBL_MESSAGES + " as me ,body as bo where  me.[id]=? and me.[id]= bo.[message_id]", new String[] { String.valueOf(messageId) });
				if (messageCursor.moveToFirst()) {
					c35Message = new C35Message();
					c35Message.setHyperText(messageCursor.getString(messageCursor.getColumnIndex("html_content")));
					c35Message.setPlainText(messageCursor.getString(messageCursor.getColumnIndex("text_content")));
					c35Message.setDownFalg(messageCursor.getInt(messageCursor.getColumnIndex("down_flag")));

					c35Message.setMailType(messageCursor.getInt(messageCursor.getColumnIndex("mailType")));
					c35Message.setCalendarState(messageCursor.getInt(messageCursor.getColumnIndex("calendarState")));
					c35Message.setCalendarStartTime(messageCursor.getString(messageCursor.getColumnIndex("calendarStartTime")));
					c35Message.setCalendarEndTime(messageCursor.getString(messageCursor.getColumnIndex("calendarEndTime")));
					c35Message.setCalendarLocation(messageCursor.getString(messageCursor.getColumnIndex("calendarLocation")));

					c35Message.setCompressedToSize(messageCursor.getLong(messageCursor.getColumnIndex("compressedToSize")));
					c35Message.setSize(messageCursor.getLong(messageCursor.getColumnIndex("size")));

					C35Attachment c35Attachment = null;
					attachmentCursor = getMDB().rawQuery("select a.* from attachments as a where a.message_id =? ", new String[] { String.valueOf(messageId) });
					ArrayList<C35Attachment> achmentList = new ArrayList<C35Attachment>();
					while (attachmentCursor.moveToNext()) {
						c35Attachment = new C35Attachment();
						c35Attachment.setMailId(uid);
						c35Attachment.setContent_uri(attachmentCursor.getString(attachmentCursor.getColumnIndex("content_uri")));
						c35Attachment.setFileSize(attachmentCursor.getLong(attachmentCursor.getColumnIndex("size")));
						c35Attachment.setId(attachmentCursor.getString(attachmentCursor.getColumnIndex("attachment_id")));
						c35Attachment.setFileName(attachmentCursor.getString(attachmentCursor.getColumnIndex("name")));
						c35Attachment.setContentType(attachmentCursor.getString(attachmentCursor.getColumnIndex("mime_type")));
						c35Attachment.setPath(attachmentCursor.getString(attachmentCursor.getColumnIndex("path")));
						c35Attachment.setCid((attachmentCursor.getString(attachmentCursor.getColumnIndex("content_id"))));
						c35Attachment.setDownState((attachmentCursor.getInt(attachmentCursor.getColumnIndex("downState"))));
						c35Attachment.setsID((attachmentCursor.getInt(attachmentCursor.getColumnIndex("id"))));
						c35Attachment.setSourceAttachmentId((attachmentCursor.getString(attachmentCursor.getColumnIndex("source_attachment_id"))));
						c35Attachment.setSourceMessageUid(attachmentCursor.getString(attachmentCursor.getColumnIndex("source_message_uid")));
						c35Attachment.setCompressItemNum(attachmentCursor.getInt(attachmentCursor.getColumnIndex("compressItemNum")));
						c35Attachment.setTime(attachmentCursor.getInt(attachmentCursor.getColumnIndex("duration_time")));
						achmentList.add(c35Attachment);
					}
					c35Message.setAttachs(achmentList);
					c35Message.setCompressItems(getAttachmentCompressItems(messageId));
				} else {// body里没取得可以从message里取，直接抛异常不妥
					messageCursor = getMDB().rawQuery("select me.* from " + TBL_MESSAGES + " as me where  me.[id]=?", new String[] { String.valueOf(messageId) });
					if (messageCursor.moveToFirst()) {
						c35Message = new C35Message();
						c35Message.setHyperText("");// space
						c35Message.setPlainText(messageCursor.getString(messageCursor.getColumnIndex("preview")));
						c35Message.setDownFalg(messageCursor.getInt(messageCursor.getColumnIndex("down_flag")));

						c35Message.setMailType(messageCursor.getInt(messageCursor.getColumnIndex("mailType")));
						c35Message.setCalendarState(messageCursor.getInt(messageCursor.getColumnIndex("calendarState")));
						c35Message.setCalendarStartTime(messageCursor.getString(messageCursor.getColumnIndex("calendarStartTime")));
						c35Message.setCalendarEndTime(messageCursor.getString(messageCursor.getColumnIndex("calendarEndTime")));
						c35Message.setCalendarLocation(messageCursor.getString(messageCursor.getColumnIndex("calendarLocation")));

						c35Message.setCompressedToSize(messageCursor.getLong(messageCursor.getColumnIndex("compressedToSize")));
						c35Message.setSize(messageCursor.getLong(messageCursor.getColumnIndex("size")));

					} else {// 依旧找不到
						throw new MessagingException("unable to get message2");
					}
				}
			} else {
				throw new MessagingException("unable to get message from message_table");
			}
		} catch (Exception e) {
			throw new MessagingException("unable to get message:" + e.getMessage());
		} finally {
			if (messageCursor != null) {
				messageCursor.close();
			}
			if (attachmentCursor != null) {
				attachmentCursor.close();
			}
		}
		return c35Message;
	}

	/**
	 * 从DB中读取邮件头信息messages，label等表
	 * 
	 * @param uid
	 * @param account
	 * @return
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-10-17
	 */
	public C35Message getC35MessageOther(String uid, Account account) throws MessagingException {
		Debug.v(TAG_DBOPER, "getC35MessageOther" + uid);
		C35Message c35Message = new C35Message();
		Cursor cursor = null;
		try {
			// Date date = new Date();
			// long t1 = date.getTime();
			cursor = getMDB().rawQuery("select distinct la.[label_name],la.[id] laid,la.label_color,me.* from " + TBL_MESSAGES + " as me left join  messages_label_staging as mls on  me.[uid]= mls.message_id left join  label as la on mls.label_id=la.id where me.uid=? and me.account_id=?", new String[] { uid, String.valueOf(getAccountIdByUuid(account.getUuid())) });
			if (!cursor.moveToNext()) {
				return null;
			}
			ArrayList<Label> lables = new ArrayList<Label>();
			Label mLabel = new Label();
			if (StringUtil.isNotEmpty(cursor.getString(cursor.getColumnIndex("laid")))) {
				mLabel.setLabelName(cursor.getString(cursor.getColumnIndex("label_name")));
				mLabel.setLabelId(cursor.getString(cursor.getColumnIndex("laid")));
				mLabel.setLabelColor(cursor.getString(cursor.getColumnIndex("label_color")));
				lables.add(mLabel);
			}
			c35Message.setFolderId(cursor.getString(cursor.getColumnIndex("folder_id")));// TODO 此为序列id
			c35Message.setMailId(uid);
			c35Message.setSubject(cursor.getString(cursor.getColumnIndex("subject")));
			// c35Message.setFrom(cursor.getString(cursor.getColumnIndex("sender_list")));
			c35Message.setAttachSize(cursor.getInt(cursor.getColumnIndex("attachment_count")));
			c35Message.setSize(cursor.getInt(cursor.getColumnIndex("mail_size")));
			// SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			c35Message.setSendTime(EmailApplication.DateFormatYMDHMS.format((cursor.getLong(cursor.getColumnIndex("date")))));
			c35Message.setDownFalg(cursor.getInt(cursor.getColumnIndex("down_flag")));
			c35Message.setRead(cursor.getInt(cursor.getColumnIndex("read_flag")));
			// 设置邮件是否为来自重要联系人的
			c35Message.setImportantFrom(cursor.getInt(cursor.getColumnIndex("is_important_from")));
			c35Message.setPriority(cursor.getInt(cursor.getColumnIndex("priority")));// 优先级字段
			// 字段已删除
			// c35Message.setForwardFalg(cursor.getInt(cursor.getColumnIndex("forward_flag")));
			// c35Message.setReplayFalg(cursor.getInt(cursor.getColumnIndex("replay_flag")));
			c35Message.setDeliverStatus(cursor.getInt(cursor.getColumnIndex("delete_flag")));
			c35Message.setPreview(cursor.getString(cursor.getColumnIndex("preview")));
			String send = cursor.getString(cursor.getColumnIndex("sender_list"));
			c35Message.setImportantFlag(cursor.getInt(cursor.getColumnIndex("favorite")));
			c35Message.setDeleteFalg(cursor.getInt(cursor.getColumnIndex("delete_flag")));
			c35Message.setDeliveredReadCount(cursor.getInt(cursor.getColumnIndex("reader_count")));
			c35Message.setFrom(send);
			ArrayList<String> toList = new ArrayList<String>();
			String tolistSting = cursor.getString(cursor.getColumnIndex("to_list"));
			if (tolistSting != null && !"".equals(tolistSting)) {
				String[] stringAddress = cursor.getString(cursor.getColumnIndex("to_list")).split("");
				for (String string : stringAddress) {
					toList.add(string);
				}
			}
			c35Message.setTo(toList);
			ArrayList<String> ccList = new ArrayList<String>();
			String ccString = cursor.getString(cursor.getColumnIndex("cc_list"));
			if (ccString != null && !"".equals(ccString)) {
				String[] stringAddress = cursor.getString(cursor.getColumnIndex("cc_list")).split("");
				for (String string : stringAddress) {
					ccList.add(string);
				}
			}
			c35Message.setCc(ccList);
			ArrayList<String> bccList = new ArrayList<String>();
			String bccString = cursor.getString(cursor.getColumnIndex("bcc_list"));
			if (bccString != null && !"".equals(bccString)) {
				String[] stringAddress = cursor.getString(cursor.getColumnIndex("bcc_list")).split("");
				for (String string : stringAddress) {
					bccList.add(string);
				}
			}
			c35Message.setBcc(bccList);

			ArrayList<String> readerList = new ArrayList<String>();
			String readerString = cursor.getString(cursor.getColumnIndex("reader_list"));
			if (readerString != null && !"".equals(readerString)) {
				String[] stringAddress = cursor.getString(cursor.getColumnIndex("reader_list")).split(";");
				for (String string : stringAddress) {
					readerList.add(string);
				}
			}
			c35Message.setDeliveredReadUsers(readerList);

			c35Message.setFolderName(this.getFolderId(Long.parseLong(c35Message.getFolderId())));
			while (cursor.moveToNext()) {
				if (StringUtil.isNotEmpty(cursor.getString(cursor.getColumnIndex("laid")))) {
					mLabel = new Label();
					mLabel.setLabelName(cursor.getString(cursor.getColumnIndex("label_name")));
					mLabel.setLabelId(cursor.getString(cursor.getColumnIndex("laid")));
					mLabel.setLabelColor(cursor.getString(cursor.getColumnIndex("label_color")));
					lables.add(mLabel);
				}
			}
			c35Message.setLabels(lables);
		} catch (Exception e) {
			throw new MessagingException("unable to get the message:" + e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return c35Message;
	}

	/**
	 * 根据邮件id获得压缩条目
	 * 
	 * @param messageId
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-27
	 */
	private List<C35CompressItem> getAttachmentCompressItems(long messageId) {
		Debug.v(TAG_DBOPER, "getAttachmentCompressItems" + messageId);
		Cursor cursor = null;
		try {
			C35CompressItem c35Attachment = null;
			cursor = getMDB().rawQuery("select id ,attach_id,fileName,fileSize,downState from compressitems where attach_id in (select attachment_id  from attachments  where message_id =? )", new String[] { String.valueOf(messageId) });
			if (cursor.getCount() > 0) {
				ArrayList<C35CompressItem> achmentList = new ArrayList<C35CompressItem>();
				while (cursor.moveToNext()) {
					c35Attachment = new C35CompressItem();
					c35Attachment.setId(cursor.getInt(0));
					c35Attachment.setAttachId(cursor.getString(1));
					c35Attachment.setFileName(cursor.getString(2));
					c35Attachment.setFileSize(cursor.getLong(3));
					c35Attachment.setDownState(cursor.getInt(4));
					achmentList.add(c35Attachment);
				}
				return achmentList;
			}
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	/**
	 * 根据邮件id获得附件
	 * 
	 * @param messageId
	 * @return
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-27
	 */
	private List<C35Attachment> getAttachment(long messageId) throws MessagingException {
		Debug.v(TAG_DBOPER, "getAttachment" + messageId);
		Cursor cursor = null;
		try {
			C35Attachment c35Attachment = null;
			cursor = getMDB().rawQuery(SQL_S_ALL_FROM + TBL_ATTACHMENTS + " where message_id =?", new String[] { String.valueOf(messageId) });
			if (cursor.getCount() > 0) {
				ArrayList<C35Attachment> achmentList = new ArrayList<C35Attachment>();
				while (cursor.moveToNext()) {
					c35Attachment = new C35Attachment();
					c35Attachment.setContent_uri(cursor.getString(cursor.getColumnIndex("content_uri")));
					c35Attachment.setFileSize(cursor.getLong(cursor.getColumnIndex("size")));
					c35Attachment.setId(cursor.getString(cursor.getColumnIndex("attachment_id")));
					c35Attachment.setMailId(cursor.getString(cursor.getColumnIndex("uid")));
					c35Attachment.setFileName(cursor.getString(cursor.getColumnIndex("name")));
					c35Attachment.setContentType(cursor.getString(cursor.getColumnIndex("mime_type")));
					c35Attachment.setPath(cursor.getString(cursor.getColumnIndex("path")));
					c35Attachment.setCid((cursor.getString(cursor.getColumnIndex("content_id"))));
					c35Attachment.setsID(cursor.getInt(cursor.getColumnIndex("id")));
					c35Attachment.setDownState(cursor.getInt(cursor.getColumnIndex("downState")));
					c35Attachment.setSourceAttachmentId(cursor.getString(cursor.getColumnIndex("source_attachment_id")));
					c35Attachment.setSourceMessageUid(cursor.getString(cursor.getColumnIndex("source_message_uid")));
					c35Attachment.setCompressItemNum(cursor.getInt(cursor.getColumnIndex("compressItemNum")));
					c35Attachment.setTime(cursor.getInt(cursor.getColumnIndex("duration_time")));
					achmentList.add(c35Attachment);
				}
				return achmentList;
			}
		} catch (Exception e) {
			throw new MessagingException("unable to get the attachments:" + e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	/**
	 * select me.*,bo.html_content,bo.text_content,att.* from messages as me,body as bo,attachments as att
	 * where uid = (select uid from messages where folder_id = (select id from folders where name="Outbox"))
	 * 
	 * @throws MessagingException
	 */
	public List<C35Message> getMessagesByFolder(Account account, String folderName) throws MessagingException {
		Debug.v(TAG_DBOPER, "getMessagesByFolder" + folderName);
		List<String> msgUids = new ArrayList<String>();
		List<C35Message> messages = new ArrayList<C35Message>();
		Cursor cursor = null;
		try {
			cursor = getMDB().rawQuery("select uid from " + TBL_MESSAGES + " where delete_flag = 0 and folder_id = (select id from folders where folderId=? AND account_id = (SELECT id FROM account WHERE uuid = ? ) )", new String[] { folderName, account.getUuid() });
			while (cursor.moveToNext()) {
				String uid = cursor.getString(cursor.getColumnIndex("uid"));
				msgUids.add(uid);
			}
			for (String uid : msgUids) {
				messages.add(getC35Message(account, uid));
			}
			return messages;
		} catch (Exception e) {
			throw new MessagingException("unable to get the message:" + e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	/**
	 * 删除一个邮件下的所有附件
	 * 
	 * @Description:
	 * @param uid
	 * @see:
	 * @since:
	 * @author: zhangran
	 * @date:2013-4-24
	 */
	public void deleteC35AttachmentsByUid(String uid) {
		Debug.v(TAG_DBOPER, "deleteC35AttachmentsByUid" + uid);
		getMDB().execSQL("delete from attachments where uid = ?", new String[] { uid });
	}

	public void setSendState(int value, String uid) {
		Debug.v(TAG_DBOPER, "setSendState" + uid);
		getMDB().execSQL("UPDATE " + TBL_MESSAGES + " set forward_replay_flag = ? where uid = ?;", new Object[] { value, uid });
	}
	
	/**
	 * 过滤@符号
	 * 
	 * @param: String
	 * @return: String
	 * @throws:
	 * @see:
	 * @since:
	 * @author: yuegl
	 * @date:2012-2-28
	 */
	private String subAtChar(String address) {
		String newAddress = "";
		if (address.indexOf("@") > -1) {
			newAddress = address.substring(0, address.indexOf("@"));
		}
		return newAddress;
	}

	/**
	 * 标签的插入 // TODO: 下一迭代修复 用message_id 外键关联
	 * 
	 * @param: String
	 * @return: String
	 * @throws:
	 * @see:
	 * @since:
	 * @author: liujie
	 * @date:2012-2-28
	 */
	private void storeLabel(C35Message message) {
		List<Label> labels = message.getLabels();
		if (labels == null || labels.isEmpty()) {
			return;
		}
		Debug.v(TAG_DBOPER, "storeLabel");
		for (Label label : labels) {
			Cursor cursor = null;
			try {
				cursor = getMDB().rawQuery("select l.* from label as l where l.id =?", new String[] { label.getLabelId() });
				if (cursor.getCount() > 0) {// 已存在的label
					getMDB().execSQL("UPDATE label set label_name = ? ,label_color =? where id = ?;", new Object[] { label.getLabelName(), label.getLabelColor(), label.getLabelId() });
				} else {// 不存在的label
					getMDB().execSQL(SQL_I_INTO + "label(id,label_name,label_color) values (?,?,?)", new Object[] { label.getLabelId(), label.getLabelName(), label.getLabelColor() });
				}
				getMDB().execSQL(SQL_I_INTO + "messages_label_staging(message_id,label_id) values (?,?)", new Object[] { message.getMailId(), label.getLabelId() });
			} catch (Exception e) {
				Debug.w("failfast", "failfast_AA", e);
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
	}

	/*----------------------------------------------多账号新增API-----------------------------------------------  */
	/**
	 * 添加新账号
	 * 
	 * @Description:
	 * @param account
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 6, 2012
	 */
	public void addAccount(Account account) {
		Debug.v(TAG_DBOPER, "addAccount(Account account");
		synchronized (getMDBLock) {
			long size = 0;
			Cursor accountCursor = null;
			String qsql = "select count(*) from account where uuid = ? or email = ?";
			accountCursor = getMDB().rawQuery(qsql, new String[] { account.getUuid(), account.getEmail() });
			if (accountCursor.moveToFirst()) {
				size = accountCursor.getLong(0);
			}
			Debug.i(TAG, "addAccount_______________________:" + size);
			if (size < 1) {
				Date date = new Date();
				String sql = SQL_I_INTO + TBL_ACCOUNT + " (uuid,email,login_time,emailshow) VALUES (?,?,?,?)";
				getMDB().execSQL(sql, new Object[] { account.getUuid(), account.getEmail(), date.getTime(), account.getmEmailShow() });
			}
			if (accountCursor != null) {
				accountCursor.close();
			}
		}
	}

	/**
	 * 默认文件夹标题分割
	 * 
	 * @Description:
	 * @param matrixCursor
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-12-23
	 */
	public static void addRowFolderDef(MatrixCursor matrixCursor) {
		matrixCursor.addRow(new Object[] { GlobalConstants.MAIlBOX_DEFAULT_NAME_ID, GlobalConstants.MAILBOX_DEFAULT_LIST_NAME, GlobalConstants.MAIlBOX_FILLING_NUMBER, GlobalConstants.MAIlBOX_FILLING_NUMBER, "" + GlobalConstants.MAIlBOX_FILLING_NUMBER, GlobalConstants.FOLDER_LABEL_TYPE, GlobalConstants.MAIlBOX_FILLING_NUMBER });
	}

	/**
	 * 自定义文件夹标题分割
	 * 
	 * @Description:
	 * @param matrixCursor
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-12-23
	 */
	public static void addRowFolderSelDef(MatrixCursor matrixCursor) {
		matrixCursor.addRow(new Object[] { GlobalConstants.MAIlBOX_SELF_NAME_ID, GlobalConstants.MAILBOX_SELF_LIST_NAME, GlobalConstants.MAIlBOX_FILLING_NUMBER, GlobalConstants.MAIlBOX_FILLING_NUMBER, "" + GlobalConstants.MAIlBOX_FILLING_NUMBER, GlobalConstants.FOLDER_LABEL_TYPE, GlobalConstants.MAIlBOX_FILLING_NUMBER });
	}

	/**
	 * 获取指定账号的默认邮件夹和自定义邮件夹的第一层
	 * 
	 * @Description:
	 * @param accountUuid
	 *            账号id
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @chang hanlixia
	 * @date:Jun 4, 2012
	 */
	public Cursor listFolder(String accountUuid) throws MessagingException {
		Debug.v(TAG_DBOPER, "listFolder" + accountUuid);
		String columnNames[] = new String[] { "_id", "name", "unread_count", "last_updated", "folder_id", "folderType", "total_count" };
		MatrixCursor matrixCursor_rtn = new MatrixCursor(columnNames);
		Cursor folderCursor = null;
		Cursor count_cursor = null;
		// 缺省文件夹列表中的name
		addRowFolderDef(matrixCursor_rtn);
		try {
			// long accountid = getAccountIdByUuid(accountUuid);
			String account_id_str = "" + getAccountIdByUuid(accountUuid);
			String sql_from_folders = "SELECT id,name,last_updated,folderType,folderId FROM folders WHERE account_id = ? AND ( parentId='0' or parentId='' or parentId='INBOX') order by folderType asc, orderValue desc ";
			folderCursor = getMDB().rawQuery(sql_from_folders, new String[] { account_id_str });
			// long inboxId = -1;
			// while (folderCursor.moveToNext()) {
			// if (folderCursor.getString(1).equals(Email.MAILBOX_INBOX)) {
			// inboxId = folderCursor.getLong(0);
			// }
			// }
			// folderCursor.moveToPosition(-1);
			int foldertypevalue = 0;// 0def，1自定义
			// int iFoldersNum=0;
			if (folderCursor.getCount() < 5) {// 文件夹为空，所以重新在db里创建
				initFolders(C35AccountManager.getInstance().getDefaultAccount());
				folderCursor = getMDB().rawQuery(sql_from_folders, new String[] { account_id_str });
			}
			if (folderCursor.getCount() < 5) {// 文件夹仍为空，抛出异常
				throw new MessagingException("can not get folders!");
			}
			while (folderCursor.moveToNext()) {
				// iFoldersNum++;
				if (foldertypevalue == 0 && foldertypevalue + folderCursor.getInt(3) == GlobalConstants.SELF_FOLDER_TYPE) {// 自定义文件夹列表中的name
					addRowFolderSelDef(matrixCursor_rtn);//
				}
				foldertypevalue = folderCursor.getInt(3);

				long folderId = folderCursor.getLong(0);
				String folderName = folderCursor.getString(1);
				long lastUpdate = folderCursor.getLong(2);
				long unreadCount = 0;
				long totalCount = 0;
				String selectionArgs[] = null;
				String sql_count_from_messages;
				if (folderName.equals(EmailApplication.MAILBOX_FAVORITEBOX)) {
					// sql =
					// "SELECT COUNT(*) FROM messages WHERE folder_id = ? AND favorite = ? AND delete_flag=? AND read_flag=?";
					// selectionArgs = new String[] { String.valueOf(inboxId),
					// String.valueOf(C35Message.FAVORITE_FLAG_FAV),
					// String.valueOf(C35Message.DELETE_FLAG_NORMOR),
					// String.valueOf(C35Message.READ_FLAG_UNREAD) };
					sql_count_from_messages = "";// 这几个箱子不需要显示已读数量所以为空
					// "SELECT COUNT(*) FROM messages WHERE  favorite = ? AND delete_flag=? AND read_flag=? AND account_id =?";
					selectionArgs = new String[] { String.valueOf(C35Message.FAVORITE_FLAG_FAV), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(C35Message.READ_FLAG_UNREAD), account_id_str };
				} else if (folderName.equals(EmailApplication.MAILBOX_ATTACHMENTBOX)) {
					sql_count_from_messages = "";// 这几个箱子不需要显示已读数量所以为空
													// "SELECT COUNT(*) FROM attachments_list WHERE  account_id =?";
					selectionArgs = new String[] { account_id_str };
				} else if (folderName.equals(EmailApplication.MAILBOX_TRASHBOX)) {
					sql_count_from_messages = "";// 这几个箱子不需要显示已读数量所以为空
					// "SELECT COUNT(*) FROM messages WHERE  delete_flag=? AND read_flag=? AND account_id =?";
					selectionArgs = new String[] { String.valueOf(C35Message.DELETE_FLAG_DELETED), String.valueOf(C35Message.READ_FLAG_UNREAD), account_id_str };
				} else if (folderName.equals(EmailApplication.MAILBOX_OUTBOX) || folderName.equals(EmailApplication.MAILBOX_SENTBOX)) {
					sql_count_from_messages = "";// 这几个箱子不需要显示已读数量所以为空
				} else {
					sql_count_from_messages = "SELECT COUNT(*) FROM messages WHERE folder_id = ?  AND delete_flag=? AND read_flag=? AND account_id =?";
					selectionArgs = new String[] { String.valueOf(folderId), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(C35Message.READ_FLAG_UNREAD), account_id_str };
				}
				if (sql_count_from_messages != "") {
					count_cursor = getMDB().rawQuery(sql_count_from_messages, selectionArgs);// unreadCount
					if (count_cursor.moveToFirst()) {
						unreadCount = count_cursor.getLong(0);
					}
					if (folderName.equals(EmailApplication.MAILBOX_INBOX) || folderName.equals(EmailApplication.MAILBOX_DRAFTSBOX)) {// totalCount
						sql_count_from_messages = "SELECT COUNT(*) FROM messages WHERE folder_id = ?  AND delete_flag=? AND account_id =?";
						selectionArgs = new String[] { String.valueOf(folderId), String.valueOf(C35Message.DELETE_FLAG_NORMOR), account_id_str };
						count_cursor = getMDB().rawQuery(sql_count_from_messages, selectionArgs);
						if (count_cursor.moveToFirst()) {
							totalCount = count_cursor.getLong(0);
						}
					}
				}
				Debug.i(TAG, "folderId:" + folderId + "|folderName:" + folderName + "|unreadCount:" + unreadCount + "|lastUpdate__:" + lastUpdate + "|foldertypevalue" + foldertypevalue);
				matrixCursor_rtn.addRow(new Object[] { folderId, folderName, unreadCount, lastUpdate, folderCursor.getString(4), foldertypevalue, totalCount });

			}

			if (foldertypevalue == 0) {// 最后如果为0，说明需要刷新// 自定义文件夹列表中的name
				addRowFolderSelDef(matrixCursor_rtn);
			}
		} catch (MessagingException e) {
			throw e;
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
			throw new MessagingException("listFolder err" + e.getMessage());
		} finally {
			if (folderCursor != null) {
				folderCursor.close();
			}
			if (count_cursor != null) {
				count_cursor.close();
			}
		}
		return matrixCursor_rtn;
	}

	/**
	 * 获取指定账号的默认邮件夹和自定义邮件夹的第一层
	 * 
	 * @Description:
	 * @param accountUuid
	 *            账号id
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @chang hanlixia
	 * @date:Jun 4, 2012
	 */
	public List<C35Folder> listAllFolders0(String accountUuid) {
		Debug.v(TAG_DBOPER, "listAllFolders0" + accountUuid);
		List<C35Folder> lst = new ArrayList<C35Folder>();
		String sql = "SELECT folderId,name,folderType,parentId, orderValue FROM folders WHERE parentId = '0' and account_id = (SELECT id FROM account WHERE uuid = ? )  order by folderType asc, orderValue desc ";
		Cursor folderCursor = null;
		try {
			folderCursor = getMDB().rawQuery(sql, new String[] { accountUuid });
			while (folderCursor.moveToNext()) {

				String folderId = folderCursor.getString(0);
				String folderName = folderCursor.getString(1);
				int folderType = folderCursor.getInt(2);
				String parentId = folderCursor.getString(3);
				long orderValue = folderCursor.getLong(4);

				C35Folder c35Folder = new C35Folder();
				c35Folder.setFolderId(folderId);
				c35Folder.setFolderName(folderName);
				c35Folder.setFolderType(folderType);
				c35Folder.setParentId(parentId);
				c35Folder.setOrderValue(orderValue);

				lst.add(c35Folder);

			}
		} finally {
			if (folderCursor != null) {
				folderCursor.close();
			}
		}
		return lst;
	}

	public List<C35Folder> listAllFolders(String accountUuid) {
		Debug.v(TAG_DBOPER, "listAllFolders" + accountUuid);
		List<C35Folder> lst = new ArrayList<C35Folder>();
		String sql = "SELECT folderId,name,folderType,parentId, orderValue FROM folders WHERE parentId != '0' and account_id = (SELECT id FROM account WHERE uuid = ? )  order by folderType asc, orderValue asc ";
		Cursor folderCursor = null;
		try {
			folderCursor = getMDB().rawQuery(sql, new String[] { accountUuid });
			while (folderCursor.moveToNext()) {

				String folderId = folderCursor.getString(0);
				String folderName = folderCursor.getString(1);
				int folderType = folderCursor.getInt(2);
				String parentId = folderCursor.getString(3);
				long orderValue = folderCursor.getLong(4);

				C35Folder c35Folder = new C35Folder();
				c35Folder.setFolderId(folderId);
				c35Folder.setFolderName(folderName);
				c35Folder.setFolderType(folderType);
				c35Folder.setParentId(parentId);
				c35Folder.setOrderValue(orderValue);

				lst.add(c35Folder);

			}
		} finally {
			if (folderCursor != null) {
				folderCursor.close();
			}
		}
		return lst;
	}

	/**
	 * 获取自定义文件夹中子文件列表
	 * 
	 * @param accountuuid
	 * @param parentId
	 * @return
	 * @see:
	 * @since:
	 * @author: gongfacun
	 * @date:2012-11-8
	 */
	public Cursor listSelfFolderChild(String accountuuid, String parentId) {
		Debug.v(TAG_DBOPER, "listSelfFolderChild" + accountuuid);
		String columnNames[] = new String[] { "_id", "name", "unread_count", "last_updated", "folder_id" };
		MatrixCursor matrixCursor = new MatrixCursor(columnNames);
		String sql = "SELECT id,name,last_updated,folderType,folderId FROM folders WHERE account_id = (SELECT id FROM account WHERE uuid = ? ) AND parentId=? order by orderValue desc";
		Cursor folderCursor = null;
		try {
			folderCursor = getMDB().rawQuery(sql, new String[] { accountuuid, parentId });
			int foldertypevalue = 0;
			while (folderCursor.moveToNext()) {
				foldertypevalue = folderCursor.getInt(3);
				long folderId = folderCursor.getLong(0);
				String folderName = folderCursor.getString(1);
				long lastUpdate = folderCursor.getLong(2);
				long unreadCount = 0;
				String selectionArgs[] = null;
				Cursor cursor = null;
				sql = "SELECT COUNT(*) FROM messages WHERE folder_id = ?  AND delete_flag=? AND read_flag=?";
				selectionArgs = new String[] { String.valueOf(folderId), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(C35Message.READ_FLAG_UNREAD) };
				cursor = getMDB().rawQuery(sql, selectionArgs);
				if (cursor.moveToFirst()) {
					unreadCount = cursor.getLong(0);
				}
				Debug.i(TAG, "selfchildfolderId:" + folderId + "|folderName:" + folderName + "|unreadCount:" + unreadCount + "|lastUpdate__:" + lastUpdate + "|foldertypevalue" + foldertypevalue);
				matrixCursor.addRow(new Object[] { folderId, folderName, unreadCount, lastUpdate, folderCursor.getString(4) });
				cursor.close();
			}
		} finally {
			if (folderCursor != null) {
				folderCursor.close();
			}
		}
		return matrixCursor;
	}

	/**
	 * 得到附件的mime_type
	 * 
	 * @param accountUuid
	 * @param uid
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-12-11
	 */
	public String getAttachmentType(String accountUuid, String uid) {
		Debug.v(TAG_DBOPER, "getAttachmentType" + uid);
		String result = "application/octet-stream";
		Cursor cursor = null;
		try {
			cursor = getMDB().rawQuery("SELECT mime_type FROM attachments WHERE account_id = (SELECT id FROM account WHERE uuid = ? ) AND attachment_id = ? ", new String[] { accountUuid, uid });
			if (cursor.moveToFirst()) {
				result = cursor.getString(0);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}

		}
		return result;
	}

	/**
	 * 显示账户列表
	 * 
	 * @param:
	 * @return:
	 * @throws:
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2012-6-12
	 */
	public Cursor getAccountsListCur() {
		Debug.v(TAG_DBOPER, "getAccountsListCur");
		String sql = "SELECT account.emailshow,result.unread_count,account.id as _id,account.uuid FROM account LEFT JOIN(SELECT account_id,count(*) unread_count FROM messages WHERE folder_id IN (SELECT id FROM folders WHERE folderId = 'inbox') AND read_flag=" + C35Message.READ_FLAG_UNREAD + " and delete_flag=" + C35Message.DELETE_FLAG_NORMOR + " GROUP BY account_id) result ON account.id=result.account_id";
		Cursor cursor = getMDB().rawQuery(sql, null);
		return cursor;
	}

	/**
	 * 从数据库Account表中取所有账户信息
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-14
	 */
	public List<Account> getAccountsFromDB() {
		Debug.v(TAG_DBOPER, "getAccountsFromDB");
		List<Account> accounts = new ArrayList<Account>();
		String sql = "SELECT uuid,email,emailshow FROM account";// emailshow增加
		Cursor cursor = null;
		try {
			cursor = getMDB().rawQuery(sql, null);
			while (cursor.moveToNext()) {
				accounts.add(new Account(cursor.getString(0), cursor.getString(1), cursor.getString(2)));
			}
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return accounts;
	}

	/**
	 * 删除用户数据库中的数据
	 * 
	 * @param:
	 * @return:
	 * @throws:
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2012-6-13
	 */

	public void deleteAccountByUuid(String accountUid) {
		Debug.v(TAG_DBOPER, "deleteAccountByUuid");
		long accountId;
		// Cursor cursor = null;
		getMDB().beginTransaction();
		try {
			// String sqlaccountid = "select id from account where uuid = ?";
			// cursor = getMDB().rawQuery(sqlaccountid, new String[] {
			// accountUid });
			// if (cursor.moveToFirst()) {
			// accountId = cursor.getInt(0);
			accountId = getAccountIdByUuid(accountUid);
			String sqlbody = "delete from body where message_id in(select id from messages where account_id=?)";
			getMDB().execSQL(sqlbody, new Object[] { accountId });
			String sqlattachments = "delete from attachments where message_id in(select id from messages where account_id=?)";
			getMDB().execSQL(sqlattachments, new Object[] { accountId });
			String sqlmessages = "delete from messages where account_id=?";
			getMDB().execSQL(sqlmessages, new Object[] { accountId });
			String sqlfolders = "delete from folders where account_id=?";
			getMDB().execSQL(sqlfolders, new Object[] { accountId });
			String sqlquery = "delete from query_mailaddress where account_id=?";
			getMDB().execSQL(sqlquery, new Object[] { accountId });
			String sqlpending = "delete from pending_commands where account_id=?";
			getMDB().execSQL(sqlpending, new Object[] { accountId });
			String sqlaccount = "delete from account where id=?";
			getMDB().execSQL(sqlaccount, new Object[] { accountId });
			String sqlattachmentlists = "delete from attachments_list where account_id=?";
			getMDB().execSQL(sqlattachmentlists, new Object[] { accountId });
			getMDB().setTransactionSuccessful();
			// }
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			// if (cursor != null) {
			// cursor.close();
			// }
			mFolderAccountMap.clear();
			mFolderGetId.clear();
			mFolderGetUuid.clear();
			mUuidToIdMap.clear();
			getMDB().endTransaction();// 结束事务,有两种情况：commit,rollback,
			// 事务的提交或回滚是由事务的标志决定的,如果事务的标志为True，事务就会提交，否侧回滚,默认情况下事务的标志为False
		}

	}

	/**
	 * 清除数据库中所有的数据
	 * 
	 * @param:
	 * @return:
	 * @throws:
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2012-7-3
	 */
	public void deletedatabasedata() {
		Debug.v(TAG_DBOPER, "deletedatabasedata");
		getMDB().beginTransaction();
		try {
			String sqlbody = "delete from body";
			getMDB().execSQL(sqlbody, new Object[] {});
			String sqlattachments = "delete from attachments";
			getMDB().execSQL(sqlattachments, new Object[] {});
			String sqlmessages = "delete from messages";
			getMDB().execSQL(sqlmessages, new Object[] {});
			String sqlfolders = "delete from folders ";
			getMDB().execSQL(sqlfolders, new Object[] {});
			String sqlquery = "delete from query_mailaddress ";
			getMDB().execSQL(sqlquery, new Object[] {});
			String sqlpending = "delete from pending_commands ";
			getMDB().execSQL(sqlpending, new Object[] {});
			String sqlaccount = "delete from account";
			getMDB().execSQL(sqlaccount, new Object[] {});

			String sqlcompressitem = "delete from compressitems";
			getMDB().execSQL(sqlcompressitem, new Object[] {});

			String sqloperate_history = "delete from operation_history";
			getMDB().execSQL(sqloperate_history, new Object[] {});

			String sqllabel = "delete from label";
			getMDB().execSQL(sqllabel, new Object[] {});

			String sqlmessages_label_staging = "delete from messages_label_staging";
			getMDB().execSQL(sqlmessages_label_staging, new Object[] {});

			String sqlattachmentlists = "delete from attachments_list";
			getMDB().execSQL(sqlattachmentlists, new Object[] {});

			getMDB().setTransactionSuccessful();
			Debug.i(TAG, "delete_______________account_tables_run");

		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			getMDB().endTransaction();// 结束事务,有两种情况：commit,rollback,
			// 事务的提交或回滚是由事务的标志决定的,如果事务的标志为True，事务就会提交，否侧回滚,默认情况下事务的标志为False
		}
	}

	/**
	 * 更新账户的uuid和email
	 * 
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-12-17
	 */
	public void updateAccount(Account account) {
		Debug.v(TAG_DBOPER, "updateAccount");
		String sql = "UPDATE account SET uuid = ? WHERE email=?";
		getMDB().execSQL(sql, new Object[] { account.getUuid(), account.getEmail() });
	}

	public String showUuiddefault(String accountuuid) {
		Debug.v(TAG_DBOPER, "showUuiddefault" + accountuuid);
		String sql = "select uuid from account where uuid not in(?) ORDER BY login_time";
		Cursor cursor = getMDB().rawQuery(sql, new String[] { accountuuid });
		if (cursor.moveToFirst()) {
			String uuid = cursor.getString(0);
			return uuid;
		}
		return null;
	}

	// 主送我的
	public String getSendToMe(String accountUuid, String messageUid) {
		Debug.v(TAG_DBOPER, " getSendToMe" + accountUuid);
		Cursor cursorSql1 = null;
		Cursor cursorSql2 = null;
		try {
			String accountId = null;
			String sql1 = "select id from account where uuid = ? ";
			cursorSql1 = getMDB().rawQuery(sql1, new String[] { accountUuid });
			if (cursorSql1.moveToFirst()) {
				accountId = cursorSql1.getString(0);
			}
			if (accountId == null) {
				return null;
			}
			String sql2 = "select is_send_me from messages where uid = ? and account_id = ? ";
			cursorSql2 = getMDB().rawQuery(sql2, new String[] { messageUid, accountId });
			if (cursorSql2.moveToFirst()) {
				String isSendMe = cursorSql2.getString(0);
				return isSendMe;
			}
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			if (cursorSql1 != null) {
				cursorSql1.close();
			}
			if (cursorSql2 != null) {
				cursorSql2.close();
			}
		}
		return null;
	}

	/**
	 * 通过Account，messageUids获取邮件头部信息集合。
	 * 
	 * @param account
	 * @param uids
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-12-11
	 */
	public List<C35Message> getMessagesByAccountAndUids(Account account, List<String> uids) {
		Debug.v(TAG_DBOPER, " getMessagesByAccountAndUids");
		List<C35Message> list = new ArrayList<C35Message>();
		try {
			for (String uid : uids) {
				C35Message message = getC35MessageOther(uid, account);
				list.add(message);
			}
		} catch (MessagingException e) {
			Debug.w("failfast", "failfast_AA", e);
		}
		return list;
	}

	/**
	 * 查找常用联系人表，为撰写邮件界面中，根据用户输入信息，提示联系人。
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-12-13
	 */
	public List<Address> getEmalAddressesForMultiAutoComplete() {
		Debug.v(TAG_DBOPER, " getEmalAddressesForMultiAutoComplete");
		List<Address> results = new ArrayList<Address>();
		Cursor cursor = null;
		StringBuilder builder = new StringBuilder();
		try {
			cursor = getMDB().rawQuery("SELECT COUNT(mail_address) AS mc,mail_address FROM query_mailaddress GROUP BY mail_address ORDER BY mc DESC", null);
			while (cursor.moveToNext()) {
				builder.append(cursor.getString(1)).append(",");
			}
			Address[] addresses = parseEmailAddress(builder.toString());
			for (Address address : addresses) {
				if (!checkAddress(address, results)) {
					results.add(address);
				}
			}
		} catch (Exception e) {
			C35AppServiceUtil.writeSubscribeInformationToSdcard("getEmalAddressesForMultiAutoComplete: " + e.toString());// 彩蛋log写入
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return results;
	}

	private boolean checkAddress(Address address, List<Address> addresses) {
		for (Address address2 : addresses) {
			if (address2.getAddress().equals(address.getAddress()) || address.getAddress().contains("@try.35.cn") || address.getAddress().contains("=") || address.getAddress().contains("?")) {
				return true;
			}
		}
		return false;
	}

	private Address[] parseEmailAddress(String addressList) {
		Address[] addresses = Address.parse(addressList);
		for (int i = 0; i < addresses.length; i++) {
			if (addresses[i].getPersonal() == null) {
				addresses[i].setPersonal(subAtChar(addresses[i].getAddress()));
			}
		}
		return addresses;
	}

	// private String getNameFromEmailAddress(String address) {
	// int index = address.indexOf("@");
	// return address.substring(0, index);
	// }

	/**
	 * 更新压缩条目下载状态？？为啥更新数据库,又没有断点续传？
	 * 
	 * @param id
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-26
	 */
	public void updatecompressitemDownloadStatus(int id) {
		Debug.v(TAG_DBOPER, " updatecompressitemDownloadStatus");
		getMDB().execSQL("UPDATE compressitems set downState = 1  where id = ?;", new Integer[] { id });
	}

	/**
	 * 根据附件Uid获取压缩包内文件
	 * 
	 * @param attachmentId
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-26
	 */
	public List<C35CompressItem> getCompressItemsByAttachmentid(String attachmentId) {
		Debug.v(TAG_DBOPER, " getCompressItemsByAttachmentid" + attachmentId);
		String sql = "SELECT attach_id, fileName, fileSize, downState FROM compressitems WHERE attach_id = ? ";
		String[] selectionArgs = new String[] { attachmentId };
		Cursor cursor = getMDB().rawQuery(sql, selectionArgs);
		try {
			List<C35CompressItem> list = new ArrayList<C35CompressItem>();
			while (cursor.moveToNext()) {
				C35CompressItem item = new C35CompressItem();
				item.setAttachId(cursor.getString(cursor.getColumnIndex("attach_id")));
				item.setFileName(cursor.getString(cursor.getColumnIndex("fileName")));
				item.setFileSize(cursor.getLong(cursor.getColumnIndex("fileSize")));
				item.setDownState(cursor.getInt(cursor.getColumnIndex("downState")));
				list.add(item);
			}
			return list;
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			cursor.close();
		}
		return null;
	}

	/**
	 * 保存压缩包内文件
	 * 
	 * @param items
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-26
	 */
	public void storeCompressItemsByC35CompressItems(List<C35CompressItem> items) {
		if (items == null || items.isEmpty()) {
			return;
		}
		try {
			Debug.v(TAG_DBOPER, " storeCompressItemsByC35CompressItems");
			getMDB().beginTransaction();
			getMDB().execSQL(SQL_DEL_FROM + "compressitems WHERE attach_id = ? ", new String[] { items.get(0).getAttachId() });
			for (C35CompressItem item : items) {
				getMDB().execSQL(SQL_I_INTO + "compressitems(attach_id,fileName,fileSize,downState) VALUES (?,?,?,?)", new Object[] { item.getAttachId(), item.getFileName(), item.getFileSize(), item.getDownState() });
			}
			getMDB().setTransactionSuccessful();
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			getMDB().endTransaction();
		}
	}

	/**
	 * 从操作履历表获取要提交的邮件的状态
	 * 
	 * @param account
	 *            账号
	 * @param folderid
	 *            文件夹
	 * @param mailId
	 *            邮件uid
	 * @return
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-23
	 */
	public List<StatusObj> getCommitMailsStatusFromLocal(Account account, String folderid, String mailId) {
		Debug.v(TAG_DBOPER, " getCommitMailsStatusFromLocal");
		Cursor cursor = null;
		try {
			if (account != null && !TextUtils.isEmpty(folderid)) {
				cursor = getMDB().rawQuery("select statusid, statusvalue, operatetime from operation_history  where account_uid =? and folderid=? and mailid=? and commitstatus!=1", new String[] { account.getUuid(), folderid, mailId });
				if (cursor.getCount() > 0) {
					List<StatusObj> statusObjList = new ArrayList<StatusObj>();
					while (cursor.moveToNext()) {
						StatusObj statusObj = new StatusObj();
						statusObj.setStatusId(cursor.getInt(cursor.getColumnIndex("statusid")));
						statusObj.setStatusValue(cursor.getString(cursor.getColumnIndex("statusvalue")));
						statusObj.setOperateTime(cursor.getLong(cursor.getColumnIndex("operatetime")));
						statusObjList.add(statusObj);
					}
					return statusObjList;
				}
			}

		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	/**
	 * 批量保存操作履历
	 * 
	 * @param operationHistoryInfo
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-23
	 */
	public void saveOperationHistory(HashMap<String, OperationHistoryInfo> operationHistoryInfoMap) {
		Set<String> keys = operationHistoryInfoMap.keySet();
		if (keys == null || keys.size() <= 0) {
			return;
		}
		Cursor cursor = null;
		try {
			Debug.v(TAG_DBOPER, "saveOperationHistory");
			getMDB().beginTransaction();
			for (String key : keys) {
				OperationHistoryInfo operationHistoryInfo = operationHistoryInfoMap.get(key);
				cursor = getMDB().rawQuery("select id from operation_history where commitstatus != 1   and account_uid =? and mailid=?  and statusid=? ", new String[] { operationHistoryInfo.getAccount_uid(), operationHistoryInfo.getMailId(), String.valueOf(operationHistoryInfo.getStatusId()) });
				if (cursor.getCount() == 0) {
					getMDB().execSQL(SQL_I_INTO + "operation_history(account_uid ,folderid ,mailid ,statusid ,statusvalue ,operatetime ,commitstatus) values (?,?,?,?,?,?,?)", new Object[] { operationHistoryInfo.getAccount_uid(), operationHistoryInfo.getFolderId(), operationHistoryInfo.getMailId(), operationHistoryInfo.getStatusId(), operationHistoryInfo.getStatusValue(), operationHistoryInfo.getOperateTime(), operationHistoryInfo.getCommitStatus() });
				} else {
					getMDB().execSQL("UPDATE operation_history set statusvalue = ? where commitstatus != 1   and account_uid =? and mailid=? and statusid=? ", new String[] { operationHistoryInfo.getStatusValue(), operationHistoryInfo.getAccount_uid(), operationHistoryInfo.getMailId(), String.valueOf(operationHistoryInfo.getStatusId()) });
				}
			}
			getMDB().setTransactionSuccessful();

		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			getMDB().endTransaction();// no transaction pending at
										// android.database.sqlite.SQLiteDatabase.endTransaction
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	/**
	 * 更新提交状态
	 * 
	 * @param accountUid
	 *            账号uid
	 * @param messageUid
	 *            邮件uid
	 * @param commitstatus
	 *            提交的状态 0:未提交，1:正在提交，-1:提交失败
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-23
	 */
	private void updateCommitstatus(String accountUid, String messageUid, int commitstatus, int statusid) {
		Debug.v(TAG_DBOPER, "updateCommitstatus");
		getMDB().execSQL(" UPDATE operation_history set commitstatus = ?  where account_uid =? and mailid=? and commitstatus=1 and statusid=?", new Object[] { commitstatus, accountUid, messageUid, statusid });
	}

	/**
	 * 更新提交状态
	 * 
	 * @param accountUid
	 *            账号uid
	 * @param commitstatus
	 *            提交的状态 0:未提交，1:正在提交，-1:提交失败
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-23
	 */
	public void updateCommitstatus(String accountUid) {
		Debug.v(TAG_DBOPER, "updateCommitstatus");
		getMDB().execSQL("UPDATE operation_history set commitstatus=1  where account_uid =? and commitstatus!=1", new String[] { accountUid });

	}

	/**
	 * 更新提交状态失败
	 * 
	 * @param accountUid
	 *            账号uid
	 * @param commitstatus
	 *            提交的状态 0:未提交，1:正在提交，-1:提交失败
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-23
	 */
	public void updateCommitstatusForFailed(String accountUid) {
		Debug.v(TAG_DBOPER, "updateCommitstatusForFailed");
		getMDB().execSQL("UPDATE operation_history set commitstatus=-1  where account_uid =? and commitstatus=1", new String[] { accountUid });

	}

	/**
	 * 删除操作履历
	 * 
	 * @param accountUid
	 *            账号uid
	 * @param messageUid
	 *            邮件uid
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-23
	 */
	private void deleteOperation_history(String accountUid, String messageUid, int statusid) {
		Debug.v(TAG_DBOPER, "deleteOperation_history");
		getMDB().execSQL("delete from operation_history  where  account_uid =? and mailid=? and commitstatus=1 and statusid=? ", new String[] { accountUid, messageUid, String.valueOf(statusid) });
	}

	/**
	 * 删除操作履历
	 * 
	 * @param accountUid
	 *            账号uid
	 * @param messageUid
	 *            邮件uid
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-23
	 */
	public void deletecommittingOperation_history(int commitstatus) {
		Debug.v(TAG_DBOPER, "deletecommittingOperation_history");
		getMDB().execSQL("delete from operation_history  where commitstatus=" + commitstatus);
	}

	// /**
	// * getKeyByValue
	// *
	// * @Description:
	// * @param map
	// * @param value
	// * @return
	// * @see:
	// * @since:
	// * @author: cuiwei
	// * @date:2013-12-17
	// */
	// protected List getKeyByValue(Map map, Object value) {
	// List keys = new ArrayList();
	// Iterator it = map.entrySet().iterator();
	// while (it.hasNext()) {
	// Map.Entry entry = (Entry) it.next();
	// Object obj = entry.getValue();
	// if (obj != null && obj.equals(value)) {
	// keys.add(entry.getKey());
	// }
	//
	// }
	// return keys;
	// }

	/**
	 * getIdByFolderId FROM folders
	 * 
	 * @param accountid
	 * @param folderId
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-12-11
	 */
	public long getIdByFolderUuidAndAccountID(long accountid, String folderId) {
		// ArrayList aL=(ArrayList) getKeyByValue(mFolderGetUuid,new
		// String[]{Long.toString(accountid),folderId});
		// if(aL!=null&& aL.size()>0){
		// return (Long) aL.get(0);
		// }
		if (mFolderGetId.get(accountid + folderId) != null && !"".equals(mFolderGetId.get(accountid + folderId))) {
			return mFolderGetId.get(accountid + folderId);
		}
		Debug.v(TAG_DBOPER, "getIdByFolderId" + folderId);
		Cursor cursor = null;
		long id = -1;
		try {
			String sql = "SELECT id FROM folders WHERE folderId = ? and account_id = ? ";
			cursor = getMDB().rawQuery(sql, new String[] { folderId, accountid + "" });
			if (cursor.moveToFirst()) {
				id = cursor.getLong(0);
				mFolderGetId.put(accountid + folderId, id);
			}else{
				//没有文件夹，需要创建
				initFolders(C35AccountManager.getInstance().getDefaultAccount());
			}
		} catch (Exception e) {// 
			Debug.e("failfast", "failfast_AA", e);
			try {
				createFolder(accountid, folderId);
			} catch (Exception e1) {
				Debug.e("failfast", "failfast_AA", e);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return id;
	}

	/**
	 * 获得FolderId
	 * 
	 * @param account
	 * @param messageUid
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-26
	 */
	public String getFolderIdByAccountAndMessageUid(Account account, String messageUid) {
		Debug.v(TAG_DBOPER, "getFolderIdByAccountAndMessageUid" + messageUid);
		Cursor cursor = null;
		String folderid = null;
		// long aid = this.getAccountIdByUuid(account.getUuid());
		long mid = this.getMessageIdByUid(account, messageUid);
		try {
			String sql = "SELECT folderId FROM folders WHERE id = (select folder_id from messages where id=?) ";
			cursor = getMDB().rawQuery(sql, new String[] { String.valueOf(mid) });
			if (cursor.moveToFirst()) {
				folderid = cursor.getString(0);
			}
		} catch (Exception e) {// 
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return folderid;
	}

	/**
	 * 获得FolderName
	 * 
	 * @param folderId
	 * @return FolderName
	 */
	public String getFolderNameByFolderId(String folderId) {
		Debug.v(TAG_DBOPER, "getFolderNameByFolderId" + folderId);
		Cursor cursor = null;
		String folderName = null;
		try {
			String sql = "SELECT name FROM folders WHERE id = ? ";
			cursor = getMDB().rawQuery(sql, new String[] { folderId });
			if (cursor != null && cursor.moveToFirst()) {
				folderName = cursor.getString(0);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return folderName;
	}

	/**
	 * 是否有多次同种操作（比如收藏，取消收藏就算有重复操作）
	 * 
	 * @param accountUid
	 * @param messageUid
	 * @param statusid
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-25
	 */
	private boolean haveThisMsg(String accountUid, String messageUid, int statusid) {
		Debug.v(TAG_DBOPER, "haveThisMsg" + messageUid);
		Cursor cursor = null;
		boolean have = false;
		try {
			cursor = getMDB().rawQuery("select id from operation_history where commitstatus != 1   and account_uid =? and mailid=? and statusid=? ", new String[] { accountUid, messageUid, String.valueOf(statusid) });
			have = cursor.getCount() > 0;
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return have;

	}

	/**
	 * 根据服务器失败结果更新操作履历
	 * 
	 * @param statusids
	 * @param accountUid
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-26
	 */
	public void updateOperationHistoryForFailureFromServer(List<ErrorObj> errorObjs, String accountUid) {
		Debug.v(TAG_DBOPER, "updateOperationHistoryForFailureFromServer" + accountUid);
		if (errorObjs != null && !errorObjs.isEmpty()) {
			try {
				getMDB().beginTransaction();
				for (ErrorObj eo : errorObjs) {
					String mailuid = eo.getMailId();
					int statusid = eo.getStatusId();
					int errorCode = eo.getErrorCode();
					if (this.haveThisMsg(accountUid, mailuid, statusid) || errorCode == GlobalConstants.ERROR_SYNC_OPERATE_FOLDER_NOTEXIST || errorCode == GlobalConstants.ERROR_SYNC_OPERATE_MAIL_NOTEXIST) {
						this.deleteOperation_history(accountUid, mailuid, statusid);
					} else {
						this.updateCommitstatus(accountUid, mailuid, -1, statusid);
					}
				}
				getMDB().setTransactionSuccessful();
			} catch (Exception e) {
				Debug.w("failfast", "failfast_AA", e);
			} finally {
				getMDB().endTransaction();
			}
		}
	}

	/**
	 * 获取操作履历表指定文件夹下邮件列表
	 * 
	 * @param account
	 * @param folderid
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-29
	 */
	public List<String> getOpHistoryMailidFromLocal(Account account, String folderid) {
		Debug.v(TAG_DBOPER, "getOpHistoryMailidFromLocal" + folderid);
		Cursor cursor = null;
		try {
			if (account != null && !TextUtils.isEmpty(folderid)) {
				cursor = getMDB().rawQuery("select mailid from operation_history  where account_uid =? and folderid=? and commitstatus!=1 group by mailid ", new String[] { account.getUuid(), folderid });
				if (cursor.getCount() > 0) {
					List<String> mailidList = new ArrayList<String>();
					while (cursor.moveToNext()) {
						String mailUId = cursor.getString(cursor.getColumnIndex("mailid"));
						if (!mailUId.startsWith("Local")) {
							mailidList.add(mailUId);
						}

					}
					return mailidList;
				}
			}

		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	/**
	 * 获得操作履历表指定账号的文件夹列表
	 * 
	 * @param accountUid
	 *            账号
	 * @return uuid
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-29
	 */
	public List<String> getFolderIdFromHistory(String accountUid) {
		Debug.v(TAG_DBOPER, "getFolderIdFromHistory_" + accountUid);
		Cursor cursor = null;
		try {
			if (!TextUtils.isEmpty(accountUid)) {
				cursor = getMDB().rawQuery("select folderid from operation_history  where account_uid =? and commitstatus!=1 group by folderid ", new String[] { accountUid });
				if (cursor.getCount() > 0) {
					List<String> folderidList = new ArrayList<String>();
					while (cursor.moveToNext()) {
						folderidList.add(cursor.getString(cursor.getColumnIndex("folderid")));
					}
					return folderidList;
				}
			}

		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	/**
	 * 根据服务器返回结果更新messages
	 * 
	 * @param account
	 *            账号
	 * @param srcMailIds
	 *            提交的mail列表
	 * @param mailStatusObjList
	 *            返回的邮件对象列表
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-5
	 */
	public void updateMessagesByResultFromServer(Account account, List<String> srcMailIds, List<MailStatusObj> mailStatusObjList, List<C35Message> visibleMessages, String folderId) throws Exception {
		Debug.v(TAG_DBOPER, "updateMessagesByResultFromServer");
		if (mailStatusObjList != null) {
			try {
				getMDB().beginTransaction();
				List<String> targetList = new ArrayList<String>();
				for (MailStatusObj mailStatusObj : mailStatusObjList) {
					String mailUid = mailStatusObj.getMailId();
					targetList.add(mailUid);
					int read = mailStatusObj.getRead();
					int fav = mailStatusObj.getImportantFlag();
					if (visibleMessages != null && !visibleMessages.isEmpty()) {
						for (C35Message vmObj : visibleMessages) {
							if (mailUid.equals(vmObj.getMailId())) {// 邮件id相等
								if (read != vmObj.getRead()) {// 不一致时才需要更新数据库
									this.setRead(account, mailUid, read);// 更新已读未读
								}
								if (fav != vmObj.getImportantFlag()) {// 不一致时才需要更新数据库
									if (fav == C35Message.FAVORITE_FLAG_FAV) {
										this.setFavorite(account, true, mailUid);// 更新为收藏
									} else {
										this.setFavorite(account, false, mailUid);// 更新为取消收藏
									}
								}
								break;
							}
						}
					} else {
						Debug.d("updateMessagesByResultFromServer", "updateMessagesByResultFromServer");
						// C35AppServiceUtil.writeSubscribeInformationToSdcard("updateMessagesByResultFromServer: visibleMessages==null||visibleMessages.isEmpty()==true");//
						// 彩蛋log写入
					}

					// TODO:sofia2.0环境不支持此接口。
					Boolean result = AccountUtil.isSupportRequest("partOfCommitSyn", account);
					if (result == null || result == false) {
						continue;
					}
					String calendarState = mailStatusObj.getCalendarState();
					if (!TextUtils.isEmpty(calendarState)) {
						this.setCalendarState(account, mailUid, calendarState);// 更新会议状态
					}
					if (EmailApplication.MAILBOX_SENTBOX.equals(folderId)) {// ,只有已发送箱子的邮件才能用到
						// 同步状态时，更新接受者的已读未读状态
						int readerCount = mailStatusObj.getDeliveredReadCount();
						List<String> readerList = mailStatusObj.getDeliveredReadUsers();
						updateReaderInfo(account, mailUid, readerCount, readerList);
					}

				}

				// 删掉没有返回的
				srcMailIds.removeAll(targetList);
				for (String mailUid : srcMailIds) {
					this.clearMessage(account, mailUid);
				}

				getMDB().setTransactionSuccessful();
			} catch (Exception e) {
				Debug.w("failfast", "failfast_AA", e);
				throw e;
			} finally {
				getMDB().endTransaction();
			}
		}
	}

	/**
	 * 更新会议邮件状态
	 * 
	 * @param account
	 *            账号
	 * @param mailUid
	 *            邮件uid
	 * @param calendarState
	 *            会议状态
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2013-1-18
	 */
	public void setCalendarState(Account account, String mailUid, String calendarState) {
		try {
			Debug.v(TAG_DBOPER, "setCalendarState");
			getMDB().execSQL("UPDATE messages set calendarState = ? WHERE uid = ? AND account_id = (SELECT id FROM account WHERE uuid = ? ) ", new Object[] { calendarState, mailUid, account.getUuid() });
		} catch (Exception e) {// SQLException
			Debug.w("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 更新已读人数,只有已发送箱子的邮件才能用到
	 * 
	 * @Description:
	 * @param account
	 * @param mailUid
	 * @param readerCount
	 * @param readerList
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-6-1
	 */
	private void updateReaderInfo(Account account, String mailUid, int readerCount, List<String> readerList) throws MessagingException {
		try {
			Debug.v(TAG_DBOPER, "updateReaderInfo");
			getMDB().execSQL("UPDATE messages set reader_count = ?,reader_list = ? WHERE uid = ? AND account_id = (SELECT id FROM account WHERE uuid = ? ) ", new Object[] { readerCount, makeReaderListString(readerList), mailUid, account.getUuid() });
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
			throw new MessagingException(e.getMessage());
		}
	}

	/**
	 * 通过相应参数获得数据库中的最近邮件uids
	 * 
	 * @param account
	 *            账号
	 * @param folderId
	 *            folder表中的字段（2.5中新添加的）
	 * @param limit
	 *            收取邮件的范围
	 * @return
	 * @see:
	 * @since:
	 * @author:
	 * @date:2012-11-7
	 */
	public List<String> getRecentMessageUidsBy(Account account, String folderId, int limit) throws MessagingException {
		Debug.v(TAG_DBOPER, "getRecentMessageUidsBy");
		List<String> result = new ArrayList<String>();
		long mAccountId = getAccountIdByUuid(account.getUuid());
		long mFolderId = getIdByFolderUuidAndAccountID(mAccountId, folderId);
		Cursor cursor = null;
		try {
			String queryString = "SELECT uid FROM messages ";
			String where;
			getMDB().beginTransaction();
			if (EmailApplication.MAILBOX_FAVORITEBOX.equals(folderId)) {
				where = "WHERE  favorite = ? AND delete_flag = ? AND account_id = ? ORDER BY date DESC  LIMIT ?";
				cursor = getMDB().rawQuery(queryString + where, new String[] { String.valueOf(C35Message.FAVORITE_FLAG_FAV), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(mAccountId), String.valueOf(limit) });
			} else if (EmailApplication.MAILBOX_TRASHBOX.equals(folderId)) {
				where = "WHERE account_id = ? AND delete_flag = ?  ORDER BY date DESC LIMIT ?";
				cursor = getMDB().rawQuery(queryString + where, new String[] { String.valueOf(mAccountId), String.valueOf(C35Message.DELETE_FLAG_DELETED), String.valueOf(limit) });
			} else {
				where = "WHERE folder_id = ?  AND delete_flag=? ORDER BY date DESC LIMIT ?";
				cursor = getMDB().rawQuery(queryString + where, new String[] { String.valueOf(mFolderId), String.valueOf(C35Message.DELETE_FLAG_NORMOR), String.valueOf(limit) });
			}
			if (cursor != null) {
				while (cursor.moveToNext()) {
					String uid = cursor.getString(cursor.getColumnIndex("uid"));
					if (uid != null && !uid.startsWith("Local")) {
						result.add(uid);
					}

				}
			}
			getMDB().setTransactionSuccessful();
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
			throw new MessagingException(e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			getMDB().endTransaction();
		}
		return result;
	}

	/**
	 * 本地DB的messages里，是否存在该邮件
	 * 
	 * @param accountUid
	 * @param messageUid
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-26
	 */
	public boolean haveThisMsg(String accountUid, String messageUid) {
		Debug.v(TAG_DBOPER, "haveThisMsg" + messageUid);
		Cursor cursor = null;
		boolean have = false;
		try {
			cursor = getMDB().rawQuery("select id from messages where  account_id = (SELECT id FROM account WHERE uuid = ? ) and uid = ? ", new String[] { accountUid, messageUid });
			have = cursor.getCount() > 0;
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return have;

	}

	/************ 以下是附件列表相关的 *************/

	/**
	 * 获得附件列表
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @throws MessagingException
	 * @date:2013-1-7
	 */
	public ArrayList<C35Attachment> getAttachmentsFromAttachmentsListDB(int accountId) throws MessagingException {
		Debug.v(TAG_DBOPER, "getAttachmentsFromAttachmentsListDB" + accountId);
		ArrayList<C35Attachment> attachmentlist = new ArrayList<C35Attachment>();
		getMDB().beginTransaction();

		Cursor cursor = null;
		try {
			cursor = getMDB().rawQuery(SQL_S_ALL_FROM + "attachments_list where account_id = ? order by sendtime desc", new String[] { accountId + "" });
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					C35Attachment attachment = new C35Attachment();
					attachment.setAccountId(accountId);
					attachment.setId(cursor.getString(cursor.getColumnIndex("attachment_id")));
					attachment.setMailId(cursor.getString(cursor.getColumnIndex("message_uid")));
					attachment.setMailSubject(cursor.getString(cursor.getColumnIndex("message_subject")));
					attachment.setFromAddr(cursor.getString(cursor.getColumnIndex("fromwho")));
					attachment.setFolderId(cursor.getString(cursor.getColumnIndex("folder_id")));
					attachment.setFileName(cursor.getString(cursor.getColumnIndex("attachment_name")));
					attachment.setFileSize(cursor.getLong(cursor.getColumnIndex("filesize")));
					attachment.setDownState(cursor.getInt(cursor.getColumnIndex("download")));
					attachment.setSendTime(cursor.getLong(cursor.getColumnIndex("sendtime")));
					attachmentlist.add(attachment);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			throw new MessagingException(e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		getMDB().setTransactionSuccessful();
		getMDB().endTransaction();
		return attachmentlist;
	}

	/**
	 * 更新附件列表的表格,无则插入
	 * 
	 * @Description:
	 * @param accountUid
	 * @param attachmentList
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-7
	 */
	public void updateAttachmentsList(int accountId, List<C35Attachment> attachmentList) {
		Debug.v(TAG_DBOPER, "updateAttachmentsList222" + accountId);
		getMDB().beginTransaction();
		for (C35Attachment attachment : attachmentList) {

			attachment.setDownState(getAttachmentDownloadState(attachment.getId(), accountId));

			Cursor cursor = null;
			try {
				cursor = getMDB().rawQuery(SQL_S_ALL_FROM + "attachments_list where attachment_id = ? and account_id = ?", new String[] { attachment.getId(), accountId + "" });
				if (cursor == null || cursor.getCount() == 0) {
					// 插入附件信息
					getMDB().execSQL(SQL_I_INTO + "attachments_list (account_id, attachment_id, message_uid, message_subject, fromwho, folder_id, attachment_name, download, sendtime, filesize) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[] { accountId, attachment.getId(), attachment.getMailId(), attachment.getMailSubject(), attachment.getFromAddr(), attachment.getFolderId(), attachment.getFileName(), attachment.getDownState(), attachment.getSendTime(), attachment.getFileSize() });
				}
			} catch (Exception e) {
				Debug.w("failfast", "failfast_AA", e);
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}

		}
		getMDB().setTransactionSuccessful();
		getMDB().endTransaction();

	}

	/**
	 * 单个附件被下载后，更新两个表的下载状态【attachments_list、attachments】
	 * 
	 * @Description:
	 * @param attachmentid
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-7
	 */
	public void updateAttachmentDownloadState(C35Attachment attachment, int state) {
		Debug.v(TAG_DBOPER, "updateAttachmentDownloadState" + state);
		getMDB().beginTransaction();

		Cursor cursor = null;

		// 更新附件列表
		try {
			cursor = getMDB().rawQuery(SQL_S_ALL_FROM + "attachments_list where attachment_id = ?", new String[] { attachment.getId() });

			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				// Debug.v(TAG,
				// "UPDATE DOWNLOAD STATE OF attachments_list TABLE :state = " +
				// state +
				// " attachmentid=" + attachment.getId());
				// String sql ="UPDATE attachments_list SET download=" + state +
				// " WHERE attachment_id='" +
				// attachment.getId() + "'";
				String sql = "UPDATE attachments_list SET download=? WHERE attachment_id=?";
				// Debug.v(TAG, "SQL = " + sql);
				getMDB().execSQL(sql, new Object[] { state, attachment.getId() });

			}

		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		// 更新attachments表格
		try {
			cursor = getMDB().rawQuery(SQL_S_ALL_FROM + "attachments where attachment_id = ?", new String[] { attachment.getId() });

			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				// Debug.v(TAG,
				// "UPDATE DOWNLOAD STATE OF attachments TABLE :state = " +
				// state);
				// getMDB().execSQL("UPDATE attachments set downState='" + state
				// + "' where attachment_id='" +
				// attachment.getId() + "'");
				String sql = "UPDATE attachments SET downState=? WHERE attachment_id=?";
				getMDB().execSQL(sql, new Object[] { state, attachment.getId() });

			}
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		getMDB().setTransactionSuccessful();
		getMDB().endTransaction();
	}

	/**
	 * 获得附件的下载状态
	 * 
	 * @Description:
	 * @param attachmentid
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-9
	 */
	public int getAttachmentDownloadState(String attachmentid, int accountId) {
		Debug.v(TAG_DBOPER, "getAttachmentDownloadState" + attachmentid);
		int state = 0;
		getMDB().beginTransaction();

		Cursor cursor = null;

		// 查询attachments_list表
		try {
			cursor = getMDB().rawQuery("select downState from attachments where attachment_id = ?", new String[] { attachmentid });

			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				state = cursor.getInt(cursor.getColumnIndex("downState"));
			}

		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		if (state != 1) {// 既然已经获得被下载了，没必要在读取了

			Cursor cursor2 = null;

			// 查询attachments_list表
			try {
				cursor2 = getMDB().rawQuery("select download from attachments_list where attachment_id = ? and account_id = ?", new String[] { attachmentid, accountId + "" });

				if (cursor2 != null && cursor2.getCount() > 0) {
					cursor2.moveToFirst();
					state = cursor2.getInt(cursor2.getColumnIndex("download"));
				}

			} catch (Exception e) {
				Debug.w("failfast", "failfast_AA", e);
			} finally {
				if (cursor2 != null) {
					cursor2.close();
				}
			}
		}

		getMDB().setTransactionSuccessful();
		getMDB().endTransaction();
		// Debug.v(TAG, "GET DOWNLOAD STATE OF ATTACHMENT : state=" + state);
		return state;
	}

	/***** 附件列表同步相关的 ****/
	/**
	 * 从DB获得附件列表所有id
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-7
	 */
	public String[] getAttachmentsIdsFromAttachmentsListDB(int accountId) {
		Debug.v(TAG_DBOPER, "getAttachmentsIdsFromAttachmentsListDB" + accountId);
		String[] ids = null;
		getMDB().beginTransaction();

		Cursor cursor = null;
		try {
			cursor = getMDB().rawQuery("select attachment_id from attachments_list where account_id = ?", new String[] { accountId + "" });// order
																																			// desc?
			if (cursor != null && cursor.getCount() > 0) {
				ids = new String[cursor.getCount()];
				int position = 0;
				cursor.moveToFirst();
				do {
					ids[position] = cursor.getString(cursor.getColumnIndex("attachment_id"));
					position++;
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		getMDB().setTransactionSuccessful();
		getMDB().endTransaction();
		return ids;
	}

	/**
	 * 保存系统流量数据
	 * 
	 * @param date
	 * @param wififlag
	 * @param rdata
	 * @param tdata
	 * @param rdatatotal
	 * @param tdatatotal
	 * @param rdatagnet
	 * @param tdatagnet
	 * @see:
	 * @since:
	 * @author: hanlx
	 * @date:2013-5-11
	 */
	public void insertflowdata(String flow_date, String wififlag, long rdata, long tdata, long rdatatotal, long tdatatotal, long rdatagnet, long tdatagnet) {
		Debug.v(TAG_DBOPER, "insertflowdata" + flow_date);
		Cursor nCursor = null;
		try {
			// getMDB().beginTransaction();
			Log.i("flowdata", "|flow_date:" + flow_date + "wififlag" + wififlag + "|rdata：" + rdata + "|tdata：" + tdata + "|rdatatotal：" + rdatatotal + "|tdatatotal:" + tdatatotal + "rdatagnet:" + rdatagnet + "|tdatagnet:" + tdatagnet);
			long temprdata, temptdata, temprdatatotal, temptdatatotal, temprdatagnet, temptdatagnet;
			long rcompareGnetData = 0, tcompareGnetData = 0;
			nCursor = selectflowdata(flow_date);
			if (nCursor != null && nCursor.getCount() > 0) {// 如果有数据进行更新操作
			} else {// 在没有数据的情况进行插入操作
				String sqlinsert = "insert into flowdatatable(flow_date,RxBytes_start,TxBytes_start,app_type,RxBytes_daySum_wifi_orall,TxBytes_daySum_wifi_orall,RxBytes_daySum_Mobile,TxBytes_daySum_Mobile) values(?,?,?,?,?,?,?,?);";
				getMDB().execSQL(sqlinsert, new Object[] { flow_date, rdata, tdata, FlowDataUtil.MAILTYPE, 0, 0, 0, 0 });
				getMDB().execSQL(sqlinsert, new Object[] { flow_date, rdatatotal, tdatatotal, FlowDataUtil.SYSTEMFLOW, 0, 0, 0, 0 });
				getMDB().execSQL(sqlinsert, new Object[] { flow_date, rdatagnet, tdatagnet, FlowDataUtil.SYSTEMGNETFLOW, 0, 0, 0, 0 });
			}
			while (nCursor != null && nCursor.moveToNext()) {
				String str = nCursor.getString(4);
				Debug.i("flowdata", "FlowDataUtil.MAILTYPE.equals(str):" + FlowDataUtil.MAILTYPE.equals(str) + " FlowDataUtil.WIFIENDFLAG.equals(wififlag):" + FlowDataUtil.WIFIENDFLAG.equals(wififlag) + "   NetworkUtil.isWifi(mContext):" + NetworkUtil.isWifi() + "    NetworkUtil.isMobile(mContext):" + NetworkUtil.isMobile());

				if (FlowDataUtil.SYSTEMFLOW.equals(str)) {
					String sqlupdate = "update flowdatatable  set RxBytes_start =?,TxBytes_start = ?,RxBytes_daySum_wifi_orall = ?,TxBytes_daySum_wifi_orall = ? where id =? ";
					if (rdatatotal >= nCursor.getLong(2)) {
						temprdatatotal = rdatatotal - nCursor.getLong(2) + nCursor.getLong(5);
						temptdatatotal = tdatatotal - nCursor.getLong(3) + nCursor.getLong(6);
					} else {
						temprdatatotal = rdatatotal + nCursor.getLong(5);
						temptdatatotal = tdatatotal + nCursor.getLong(6);
					}
					getMDB().execSQL(sqlupdate, new Object[] { rdatatotal, tdatatotal, temprdatatotal, temptdatatotal, nCursor.getLong(0) });

				}
				if (FlowDataUtil.SYSTEMGNETFLOW.equals(str)) {
					String sqlupdate = " update flowdatatable  set RxBytes_start =?,TxBytes_start = ?,RxBytes_daySum_Mobile = ?,TxBytes_daySum_Mobile = ? where id =?";
					if (rdatagnet >= nCursor.getLong(2)) {
						rcompareGnetData = rdatagnet - nCursor.getLong(2);
						tcompareGnetData = tdatagnet - nCursor.getLong(3);
						temprdatagnet = rcompareGnetData + nCursor.getLong(7);
						temptdatagnet = tcompareGnetData + nCursor.getLong(8);
					} else {
						rcompareGnetData = rdatagnet;
						tcompareGnetData = tdatagnet;
						temprdatagnet = rdatagnet + nCursor.getLong(7);
						temptdatagnet = tdatagnet + nCursor.getLong(8);
					}

					getMDB().execSQL(sqlupdate, new Object[] { rdatagnet, tdatagnet, temprdatagnet, temptdatagnet, nCursor.getLong(0) });
				}

				if (FlowDataUtil.MAILTYPE.equals(str) && (FlowDataUtil.WIFIENDFLAG.equals(wififlag) || (FlowDataUtil.WIFISAVE.equals(wififlag) && NetworkUtil.isWifi()))) {
					String sqlupdate = " update flowdatatable  set RxBytes_start =?,TxBytes_start = ?,RxBytes_daySum_wifi_orall = ?,TxBytes_daySum_wifi_orall = ? where id =?";
					if (rdata >= nCursor.getLong(2)) {
						temprdata = rdata - nCursor.getLong(2) + nCursor.getLong(5);
						temptdata = tdata - nCursor.getLong(3) + nCursor.getLong(6);
						getMDB().execSQL(sqlupdate, new Object[] { rdata, tdata, temprdata, temptdata, nCursor.getLong(0) });
					} else {
						temprdata = rdata + nCursor.getLong(5);
						temptdata = tdata + nCursor.getLong(6);
						getMDB().execSQL(sqlupdate, new Object[] { rdata, tdata, temprdata, temptdata, nCursor.getLong(0) });
					}
				}
				if (FlowDataUtil.MAILTYPE.equals(str) && (FlowDataUtil.WIFISTARTFLAG.equals(wififlag) || (FlowDataUtil.WIFISAVE.equals(wififlag) && NetworkUtil.isMobile()))) {
					String sqlupdate = " update flowdatatable  set RxBytes_start =?,TxBytes_start = ?,RxBytes_daySum_Mobile = ?,TxBytes_daySum_Mobile = ? where id =?";
					if (rdata > nCursor.getLong(2)) {
						if (rcompareGnetData >= (rdata - nCursor.getLong(2))) {
							temprdata = rdata - nCursor.getLong(2) + nCursor.getLong(7);
						} else {
							temprdata = rcompareGnetData + nCursor.getLong(7);
						}
						if (tcompareGnetData >= (tdata - nCursor.getLong(3))) {
							temptdata = tdata - nCursor.getLong(3) + nCursor.getLong(8);
						} else {
							temptdata = tcompareGnetData + nCursor.getLong(8);
						}
					} else {
						if (rcompareGnetData >= rdata) {

							temprdata = rdata + nCursor.getLong(7);
						} else {
							temprdata = rcompareGnetData + nCursor.getLong(7);
						}
						if (tcompareGnetData > tdata) {

							temptdata = tdata + nCursor.getLong(8);
						} else {
							temptdata = tcompareGnetData + nCursor.getLong(8);
						}
					}
					getMDB().execSQL(sqlupdate, new Object[] { rdata, tdata, temprdata, temptdata, nCursor.getLong(0) });
				}

			}
			// getMDB().setTransactionSuccessful();
		} catch (Exception e) {// todo android.database.sqlite.SQLiteException: cannot commit - no transaction
								// is active
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			if (nCursor != null) {
				nCursor.close();
			}
			// getMDB().endTransaction();
		}
	}

	/**
	 * 查询流量数据
	 * 
	 * @param type
	 * @param wififlag
	 * @return
	 */
	private Cursor selectflowdata(String date) {
		Debug.v(TAG_DBOPER, "selectflowdata" + date);
		String qsql = SQL_S_ALL_FROM + "flowdatatable where flow_date = ? order by id desc ";
		Cursor qcousor = getMDB().rawQuery(qsql, new String[] { date });
		return qcousor;
	}

	/**
	 * 清除流量数据表的数据
	 * 
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2013-5-17
	 */
	public void clearflowdata() {
		Debug.v(TAG_DBOPER, "clearflowdata");
		String sql = "delete from flowdatatable";
		getMDB().execSQL(sql);
	}

	/**
	 * 查询15天的数据
	 * 
	 * @return
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2013-5-17
	 */
	public Cursor selecttopdata() {
		Debug.v(TAG_DBOPER, "selecttopdata");
		String qsql = SQL_S_ALL_FROM + "flowdatatable  order by id desc limit 45 ";
		Cursor qcursor = getMDB().rawQuery(qsql, new String[] {});
		return qcursor;
	}

	/**
	 * 删除附件列表中的某附件
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-10
	 */
	public void deleteAttachmentFromAttachmentsListTableById(String attachmentId) {
		Debug.v(TAG_DBOPER, "deleteAttachmentFromAttachmentsListTableById");
		getMDB().beginTransaction();
		try {
			getMDB().execSQL(SQL_DEL_FROM + TBL_ATTACHMENTS_LIST + " WHERE attachment_id = ?", new Object[] { attachmentId });
			getMDB().setTransactionSuccessful();
		} finally {
			getMDB().endTransaction();
		}
	}

	/**
	 * 删除某个账户所有附件，当服务器没有邮件后，同步本地邮件时进行删除
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-14
	 */
	public void deleteAttachmentFromAttachmentsListTableByAccountId(int accountId) {
		Debug.v(TAG_DBOPER, "deleteAttachmentFromAttachmentsListTableByAccountId");
		getMDB().beginTransaction();
		try {
			getMDB().execSQL(SQL_DEL_FROM + TBL_ATTACHMENTS_LIST + " WHERE account_id = ?", new Object[] { accountId });
			getMDB().setTransactionSuccessful();
		} finally {
			getMDB().endTransaction();
		}
	}

	/**
	 * 查找数据库中某个账号的没有下载邮件体的邮件
	 * 
	 * @param account
	 *            账号
	 * @return
	 * @see:
	 * @since:
	 * @author: hanlx
	 * @date:2013-3-28
	 */
	public List<String> selectUnDownLoadMail(Account account) {
		Debug.v(TAG_DBOPER, "selectUnDownLoadMail account");
		Cursor cursor = null;
		List<String> list = new ArrayList<String>();
		List<String> mailList = new ArrayList<String>();
		try {
			cursor = getMDB().rawQuery("select uid from messages where down_flag = 0 and account_id = (select id from account where uuid =?) order by date desc limit 300;", new String[] { account.getUuid() });
			while (cursor.moveToNext()) {
				list.add(cursor.getString(0));
				Debug.d(TAG, "LocalStore查询的Uuid" + cursor.getString(0));
			}
			// 将邮件按照发送时间的早晚升序放到列表中，这样下载队列就可以从最新的邮件开始下载。
			for (int i = list.size() - 1; i >= 0; i--) {
				mailList.add(list.get(i));
			}
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return mailList;
	}
}
