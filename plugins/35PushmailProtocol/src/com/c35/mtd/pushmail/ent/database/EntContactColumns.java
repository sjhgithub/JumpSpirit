package com.c35.mtd.pushmail.ent.database;

import android.database.Cursor;
import android.provider.BaseColumns;


public class EntContactColumns {
	/**
	 * 数据库常量值
	 * 
	 * @Description:
	 * @author:黄永兴(huangyx2@35.cn)
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2012-3-2
	 */
	public static class Constants {

		public static class Sex {
			/** 女性 */
			public static final String FEMALE = "F";

			/** 男性 */
			public static final String MALE = "M";
		}
	}

	/**
	 * PRM基本字段
	 * 
	 * @Description:
	 * @author:黄永兴(huangyx2@35.cn)
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2012-3-2
	 */
	public static class PRMBaseColumns implements BaseColumns {

		public static String NONE_VALUE = "NONE";

		public static int getIdValue(Cursor cursor) {
			return cursor.getInt(cursor.getColumnIndex(_ID));
		}
		
		public static int getCount(Cursor cursor){
			return cursor.getInt(cursor.getColumnIndex(_COUNT));
		}
	}

	/**
	 * 联系人基本信息字段
	 * 
	 * @Description:
	 * @author:黄永兴(huangyx2@35.cn)
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2012-3-2
	 */
	public static class TContacts extends PRMBaseColumns {
		
		public static final String TABLE_NAME = "TContacts";

		public static final String UUID = "FUUID";

		/** 姓名 */
		public static final String USERNAME = "FUserName";

		/** 昵称 */
		public static final String NICKNAME = "FNickName";

		/**
		 * 性别
		 */
		public static final String SEX = "FSex";

		/** 生日 */
		public static final String BIRTHDAY = "FBirthday";
		/**
		 * 账号域
		 */
		public static final String ACCOUNT = "FAccount";

	}

	/* 属性表列名 */
	public static class TContactAttributes implements BaseColumns {

		public static final String TABLE_NAME = "TContactAttributes";
		
		/* Columns name */
		public static final String NAME = "FName";
		public static final String VALUE = "FValue";
		public static final String CATEGORY = "FCategory";
		public static final String TYPE = "FType";
		public static final String CID = "FCid";

	}
	
	/**
	 * 同步时间戳记录
	 * @Description:
	 * @author:huangyx2  
	 * @see:   
	 * @since:      
	 * @copyright © 35.com
	 * @Date:2013-5-22
	 */
	public static class TContactSync implements BaseColumns{
		
		public static final String TABLE_NAME = "TContact_sync";
		
		public static final String ACCOUNT = "FAccount";
		
		public static final String TIME = "FTime";
	}

}
