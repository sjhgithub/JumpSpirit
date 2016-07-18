package com.c35.mtd.pushmail.ent.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract.CommonDataKinds.Email;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.ent.bean.ContactAttribute;
import com.c35.mtd.pushmail.ent.bean.ContactInfo;
import com.c35.mtd.pushmail.ent.database.EntContactColumns.TContactAttributes;
import com.c35.mtd.pushmail.ent.database.EntContactColumns.TContactSync;
import com.c35.mtd.pushmail.ent.database.EntContactColumns.TContacts;
import com.c35.mtd.pushmail.ent.database.EntContactColumns.Constants.Sex;
import com.c35.mtd.pushmail.ent.protocol.SyncEntContactsResponse.CompanyContact;
import com.c35.mtd.pushmail.util.StringUtil;

/**
 * 企业联系人数据库
 * @Description:
 * @author:huangyx2  
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2013-5-22
 */
public class EntContactDb extends SQLiteOpenHelper {

	// 创建表的语句开头
	private static final String CREATE_TABLE_START_SQL = "CREATE TABLE IF NOT EXISTS ";
	// 主键语句
	private static final String CREATE_TABLE_PRIMIRY_SQL = " integer PRIMARY KEY AUTOINCREMENT, ";


	// 企业联系人数据库
	public static final String ENT_DATABASE_NAME = "35PushMail_ENTCONTACT.db";
	public static final int VERSION = 3;
	
	private static EntContactDb instance = new EntContactDb();
	
	public static EntContactDb getInstance(){
		return instance;
	}
	
	protected EntContactDb() {
		super(com.c35.mtd.pushmail.EmailApplication.getInstance(), ENT_DATABASE_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		dropTable(db);
		createTables(db);
	}

	/**
	 * 创建表
	 * @Description:
	 * @param db
	 * @see: 
	 * @since: 
	 * @author: 黄永兴 (huangyx2@35.cn)
	 * @date:2012-11-21
	 */
	synchronized private void createTables(SQLiteDatabase db) {

		if(db == null || db.isReadOnly()){
			db  = getWritableDatabase();
		}
		//创建联系人基本信息表的SQL语句
		StringBuffer sql_contacts = new StringBuffer();
		sql_contacts.append(CREATE_TABLE_START_SQL).append(TContacts.TABLE_NAME).append(" ( "); 
		sql_contacts.append(TContacts._ID).append(CREATE_TABLE_PRIMIRY_SQL);
		sql_contacts.append(TContacts.UUID).append(" varchar(35) not null ,");
		sql_contacts.append(TContacts.USERNAME).append(" varchar(30) not null, ");
		sql_contacts.append(TContacts.NICKNAME).append(" varchar(30), ");
		sql_contacts.append(TContacts.SEX).append(" char(1) default '").append(Sex.FEMALE).append("', ");
		sql_contacts.append(TContacts.BIRTHDAY).append(" varchar(10), ");
		sql_contacts.append(TContacts.ACCOUNT).append(" varchar(50) not null )");
				
		// 联系人属性
		StringBuffer sql_contactAttri = new StringBuffer();
		sql_contactAttri.append(CREATE_TABLE_START_SQL).append(TContactAttributes.TABLE_NAME).append("( ");
		sql_contactAttri.append(TContactAttributes._ID).append(CREATE_TABLE_PRIMIRY_SQL);
		sql_contactAttri.append(TContactAttributes.NAME).append(" varchar(20), ");
		sql_contactAttri.append(TContactAttributes.VALUE).append(" not null, ");
		sql_contactAttri.append(TContactAttributes.CATEGORY).append(" varchar(50) not null, ");
		sql_contactAttri.append(TContactAttributes.TYPE).append(" int not null, ");
		sql_contactAttri.append(TContactAttributes.CID).append(" integer not null )");

		StringBuffer sql_sync = new StringBuffer();
		sql_sync.append(CREATE_TABLE_START_SQL).append(TContactSync.TABLE_NAME).append(" ( "); 
		sql_sync.append(TContactSync._ID).append(CREATE_TABLE_PRIMIRY_SQL);
		sql_sync.append(TContactSync.TIME).append(" varchar(30) not null, ");
		sql_sync.append(TContactSync.ACCOUNT).append(" varchar(50) not null )");
		
		db.execSQL(sql_contacts.toString());
		db.execSQL(sql_contactAttri.toString());
		db.execSQL(sql_sync.toString());
		// 索引
		db.execSQL(getCreateIndexSql(TContacts.TABLE_NAME, TContacts.USERNAME));
		db.execSQL(getCreateIndexSql(TContacts.TABLE_NAME, TContacts.NICKNAME));
		db.execSQL(getCreateIndexSql(TContacts.TABLE_NAME, TContacts.UUID ));
		db.execSQL(getCreateIndexSql(TContacts.TABLE_NAME, TContacts.ACCOUNT ));
		db.execSQL(getCreateIndexSql(TContactAttributes.TABLE_NAME, TContactAttributes.NAME));
		db.execSQL(getCreateIndexSql(TContactAttributes.TABLE_NAME, TContactAttributes.VALUE));
		db.execSQL(getCreateIndexSql(TContactAttributes.TABLE_NAME, TContactAttributes.CATEGORY));
	}

	/**
	 * 删除表
	 * @Description:
	 * @param db
	 * @see: 
	 * @since: 
	 * @author: 黄永兴 (huangyx2@35.cn)
	 * @date:2012-3-2
	 */
	synchronized public void dropTable(SQLiteDatabase db){
		if(db == null || db.isReadOnly()){
			db = getWritableDatabase();
		}
	
		db.execSQL(getDropTableSql(TContacts.TABLE_NAME));
		db.execSQL(getDropTableSql(TContactAttributes.TABLE_NAME));
		db.execSQL(getDropTableSql(TContactSync.TABLE_NAME));
    	
		// 删除索引
		db.execSQL(getDropIndexSql(TContacts.TABLE_NAME, TContacts.USERNAME));
		db.execSQL(getDropIndexSql(TContacts.TABLE_NAME, TContacts.NICKNAME));
		db.execSQL(getDropIndexSql(TContacts.TABLE_NAME, TContacts.UUID ));
		db.execSQL(getDropIndexSql(TContacts.TABLE_NAME, TContacts.ACCOUNT ));
		db.execSQL(getDropIndexSql(TContactAttributes.TABLE_NAME, TContactAttributes.NAME));
		db.execSQL(getDropIndexSql(TContactAttributes.TABLE_NAME, TContactAttributes.VALUE));
		db.execSQL(getDropIndexSql(TContactAttributes.TABLE_NAME, TContactAttributes.CATEGORY));
	}
	
	/**
	 * 创建索引的语句
	 * @Description:
	 * @param tableName
	 * @param columnName
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyongxing
	 * @date:2012-8-8
	 */
	private String getCreateIndexSql(String tableName, String columnName){
		StringBuffer buffer = new StringBuffer();
		buffer.append("CREATE INDEX ").append(getIndexName(tableName, columnName));
		buffer.append(" ON ").append(tableName).append("(").append(columnName).append(")");
		return buffer.toString();
	}
	/**
	 * 获取索引名字
	 * @Description:
	 * @param tableName
	 * @param columnName
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyongxing
	 * @date:2012-8-9
	 */
	private String getIndexName(String tableName, String columnName){
		return "INDEX_" + tableName+"_" + columnName;
	}
	/**
	 * 删除表的语句
	 * @Description:
	 * @param tableName
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyongxing
	 * @date:2012-8-8
	 */
	private String getDropTableSql(String tableName){
		return "DROP TABLE IF EXISTS " + tableName;
	}
	
	/**
	 * 删除索引的语句
	 * @Description:
	 * @param indexName
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyongxing
	 * @date:2012-8-9
	 */
	private String getDropIndexSql(String tableName, String columnName){
		return "drop index if exists " + getIndexName(tableName, columnName);
	}
	
	/**
	 * 联系人email地址信息
	 * @Description:
	 * @param account
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-20
	 */
	public List<ContactAttribute> getContactMails(Account account){
		Cursor cursor = null;
		Cursor mailCursor = null;
		// 定义set存放，确保mail地址一样的只有一个
		Map<String, ContactAttribute> emails = new HashMap<String, ContactAttribute>();
		try{
			// 先查出联系人基本信息， 之前用了视图，但效率太低...
			String[] columns = {TContacts.USERNAME, TContacts.NICKNAME, TContacts._ID};
			// 只查当前默认账号的
			String selection = TContacts.ACCOUNT + "=?";
			String[] selectionArgs = null;
			selectionArgs = new String[]{StringUtil.getAccountSuffix(account)};
			cursor = getReadableDatabase().query(TContacts.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
			// 将联系人基本信息封装到map中，以ID为key
			Map<Integer, ContactAttribute> temp = new HashMap<Integer, ContactAttribute>(cursor.getCount());
			while (cursor.moveToNext()) {
				ContactAttribute ca = new ContactAttribute();
				ca.setNickName(cursor.getString(1));
				ca.setUserName(cursor.getString(0));
				temp.put(cursor.getInt(2), ca);
			}
			if(!temp.isEmpty()){
				// 查询出邮箱
				String[] mail_columns = {TContactAttributes.VALUE, TContactAttributes.CID};
				String mail_selection = appendInWhere(TContactAttributes.CID, temp.keySet().toArray());
				mail_selection += " and " + TContactAttributes.CATEGORY + "='" + Email.CONTENT_ITEM_TYPE + "'";
				mailCursor = getReadableDatabase().query(TContactAttributes.TABLE_NAME, mail_columns, mail_selection, null, null, null, null);
				String value = null;
				String lowerValue = null;
				while(mailCursor.moveToNext()){
					int cid = mailCursor.getInt(1);
					value = mailCursor.getString(0).trim();
					if(temp.containsKey(cid)){
						lowerValue = value.toLowerCase(Locale.ENGLISH);
						if(!emails.containsKey(lowerValue)){
							ContactAttribute ca = temp.get(cid).cloneSelf();
							ca.setValue(value);
							// 一个人可能有多个email，所以要将ContactAttribute 放到一个list中
							emails.put(lowerValue, ca);
						}
					}
				}
			}
			temp.clear();
		} catch (Exception e) {
			Debug.e("EntContactDb", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if(mailCursor != null){
				mailCursor.close();
			}
		}
		List<ContactAttribute> results = new ArrayList<ContactAttribute>(emails.values());
		emails.clear();
		return results;
	}
	/**
	 * 保存或修改企业联系人
	 * @Description:
	 * @param deletedContacts
	 * @param updContacts
	 * @param syncTime
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-21
	 */
	public boolean updEntContacts(List<String> deletedContacts, List<CompanyContact> updContacts, String syncTime, Account account){
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		Cursor deleCursor = null;
		Cursor updCursor = null;
		Cursor lastTimeCursor = null;
		boolean result = false;
		try {
			long start = System.currentTimeMillis();
			String accountSuffix = StringUtil.getAccountSuffix(account);
			// 删除企业联系人
			if(deletedContacts != null && !deletedContacts.isEmpty()){
				Object[] emplayeeIdsArray = deletedContacts.toArray();
				List<Integer> ids = new ArrayList<Integer>();
				// 先获取对应联系人ID，再删除联系人属性及联系人，还要删除收藏,头像
				String where = appendInWhere(TContacts.UUID, emplayeeIdsArray);
				if(StringUtil.isNotEmpty(where)){
					where += " and ";
				}
				where += TContacts.ACCOUNT + "=?";
				deleCursor = db.query(TContacts.TABLE_NAME, new String[] { TContacts._ID }, where, new String[]{accountSuffix}, null, null, null);
				while(deleCursor.moveToNext()){
					ids.add(deleCursor.getInt(0));
				}
				// 删除联系人
				db.delete(TContacts.TABLE_NAME, where, null);
				if(!ids.isEmpty()){
					Object[] ids_array = ids.toArray();
					// 删除联系人属性
					String where_attri = appendInWhere(TContactAttributes.CID, ids_array);
					db.delete(TContactAttributes.TABLE_NAME, where_attri, null);
					ids_array = null;
				}
				ids.clear();
				ids = null;
			}
			// 增加或修改企业联系人
			if (updContacts != null && !updContacts.isEmpty()) {
				Map<String, ContactInfo> uuids = new HashMap<String, ContactInfo>(updContacts.size());
				List<Integer> ids = new ArrayList<Integer>();
				// 需要先和数据库中联系人比较是否已经有该联系人
				for (CompanyContact companyContact : updContacts) {
					ContactInfo c = companyContact.convert2ContactModel();
					uuids.put(companyContact.employeeID, c);
				}
				String where = appendInWhere(TContacts.UUID, uuids.keySet().toArray());
				if(StringUtil.isNotEmpty(where)){
					where += " and ";
				}
				where += TContacts.ACCOUNT + "=?";
				updCursor = db.query(TContacts.TABLE_NAME, new String[] {TContacts._ID, TContacts.UUID}, where, new String[]{accountSuffix}, null, null, null);
				while (updCursor.moveToNext()) {
					int id = updCursor.getInt(0);
					ids.add(id);
					String uuid = updCursor.getString(1);
					ContactInfo temp = uuids.get(uuid);
					temp.setId(id);
				}
				if(!ids.isEmpty()){
					Object[] idsArr = ids.toArray();
					// 删除联系人属性
					String where_attri = appendInWhere(TContactAttributes.CID, idsArr);
					db.delete(TContactAttributes.TABLE_NAME, where_attri, null);
					idsArr = null;
				}
				for(ContactInfo c : uuids.values()){
					int id = c.getId();
					c.setAccount(accountSuffix);
					if(id > 0){
						db.update(TContacts.TABLE_NAME, c.baseToContentValues(), TContacts._ID + "=?", new String[]{Integer.toString(id)});
					}else{
						id = (int) db.insert(TContacts.TABLE_NAME, null, c.baseToContentValues());
						c.setId(id);
					}
					// 插入联系人属性
					List<ContentValues> attrisContentValues = c.attris2ContentValues();
					for (ContentValues contentValues : attrisContentValues) {
						db.insert(TContactAttributes.TABLE_NAME, null, contentValues);
					}
				}
				uuids.clear();
				uuids = null;
				ids.clear();
				ids = null;
			}
			// 保存最后一次同步时间
			lastTimeCursor = db.query(TContactSync.TABLE_NAME, new String[]{TContactSync._ID}, TContactSync.ACCOUNT + "=?", new String[]{accountSuffix}, null, null, null);
			int id = -1;
			if(lastTimeCursor.moveToNext()){
				id = lastTimeCursor.getInt(0);
			}
			ContentValues syncTimeValues = new ContentValues();
			if(id != -1){
				syncTimeValues.put(TContactSync._ID, id);
			}
			syncTimeValues.put(TContactSync.ACCOUNT, accountSuffix);
			syncTimeValues.put(TContactSync.TIME, syncTime);
			db.insertWithOnConflict(TContactSync.TABLE_NAME, null, syncTimeValues, SQLiteDatabase.CONFLICT_REPLACE);
			
			db.setTransactionSuccessful();
			result = true;

			Debug.i("EntContactDb", "insert contact used " + (System.currentTimeMillis() - start));
		} catch (Exception e) {
			Debug.e("EntContactDb", "failfast_AA", e);
		} finally {
			if (deleCursor != null) {
				deleCursor.close();
			}
			if (updCursor != null) {
				updCursor.close();
			}
			if(lastTimeCursor != null){
				lastTimeCursor.close();
			}
			db.endTransaction();
		}
		return result;
	}
	
	/**
	 * 最后一次同步时间
	 * @Description:
	 * @param account
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-21
	 */
	public String getLastSyncEntTime(Account account){
		Cursor cursor = null;
		String time = null;
		try{
			cursor = getReadableDatabase().query(TContactSync.TABLE_NAME, new String[]{TContactSync.TIME}, TContactSync.ACCOUNT + "=?", new String[]{StringUtil.getAccountSuffix(account)}, null, null, null);
			if (cursor.moveToNext()) {
				time = cursor.getString(0);
			}
		} catch (Exception e) {
			Debug.e("EntContactDb", "failfast_AA", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return time;
	}
	
	/**
	 * 
	 * @Description: 根据数组参数和字段名称 生成 in 条件  case: in(1,3,4)
	 * @param name 字段名称
	 * @param array 参数
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-21
	 */
	private String appendInWhere(String name,Object[] array) {
		StringBuilder where = new StringBuilder();
		if (array != null && array.length > 0) {
			where.append(name).append(" in(");
			for (Object i : array) {
				if(i instanceof String){
					where.append("'").append(i).append("'").append(",");
				}else{
					where.append(i).append(",");
				}
			}
			where.deleteCharAt(where.length() - 1);
			where.append(")");
			return where.toString();
		}
		return "";
	}
}
