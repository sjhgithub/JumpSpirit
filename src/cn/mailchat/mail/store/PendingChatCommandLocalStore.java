package cn.mailchat.mail.store;

import java.util.ArrayList;

import cn.mailchat.MailChat;
import cn.mailchat.chatting.beans.PendingHTTPSCommand;
import cn.mailchat.chatting.beans.PendingMQTTConmmand;
import cn.mailchat.chatting.protocol.ActionListener;
import cn.mailchat.chatting.protocol.MQTTCommand;
import cn.mailchat.helper.Utility;
import cn.mailchat.mail.store.Columns.HTTPSPendingAction;
import cn.mailchat.mail.store.Columns.MQTTPendingAction;
import cn.mailchat.mail.store.LockableDatabase.DbCallback;
import cn.mailchat.mail.store.LockableDatabase.WrappedException;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 缓存聊天命令的数据库
 * 
 * 注：现在主要是为了MQTT退订失败的存储.其他的缓存在账户表中.
 * 
 * @Description:
 * @see:
 * @since:
 * @author: shengli
 * @date:2015-4-15
 */
public class PendingChatCommandLocalStore extends SQLiteOpenHelper {
	private static String TAG = PendingChatCommandLocalStore.class.getSimpleName();
	private static final int DB_VERSION =1;
	private static final String DB_NAME="PendingChatCommand.db";
	private static PendingChatCommandLocalStore pendingChatCommandLocalStore;;
	
	/**
	 * MQTT失败缓存
	 */
	private static String SQL_TABLE_TB_MQTT_PENDING_CREATE;
	private static String SQL_TABLE_TB_MQTT_PENDING__CLEAR = "DROP TABLE IF EXISTS "
			+ Columns.MQTTPendingAction.TB_NAME;
	
	private void mailchatPendingDB(SQLiteDatabase db) {
		StringBuffer tb_mqttPending = new StringBuffer();
		tb_mqttPending.append("CREATE TABLE IF NOT EXISTS ")
				.append(Columns.MQTTPendingAction.TB_NAME).append(" (")
				.append(Columns.MQTTPendingAction.ID)
				.append(" INTEGER PRIMARY KEY, ")
				.append(Columns.MQTTPendingAction.ACTION)
				.append(" INTEGER, ")
				.append(Columns.MQTTPendingAction.COMMAND)
				.append(" INTEGER, ")
				.append(Columns.MQTTPendingAction.TOPIC).append(" TEXT, ")
				.append(Columns.MQTTPendingAction.CONTENT).append(" TEXT)");
		SQL_TABLE_TB_MQTT_PENDING_CREATE = tb_mqttPending.toString();
		db.execSQL(SQL_TABLE_TB_MQTT_PENDING_CREATE);
	}

	private void deleteMailchatPendingDB(SQLiteDatabase db) {
		db.execSQL(SQL_TABLE_TB_MQTT_PENDING__CLEAR);
	}
	
	public PendingChatCommandLocalStore(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		// TODO Auto-generated constructor stub
	}
	public static PendingChatCommandLocalStore getInstance(Context context) {
		if(pendingChatCommandLocalStore==null){
			pendingChatCommandLocalStore=new PendingChatCommandLocalStore(context);
		}
		return pendingChatCommandLocalStore;
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onCreate" + db);
		mailchatPendingDB(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onUpgrade" + db.getVersion());
		deleteMailchatPendingDB(db);
		onCreate(db);
	}
	
	/**
	 * 删除MQTT失败缓存
	 * 
	 * @Description:
	 * @param
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-1-9
	 */
	public void deleteMQTTPending(final String id) {
					// TODO Auto-generated method stub
		SQLiteDatabase db = getWritableDatabase();
		db.delete(MQTTPendingAction.TB_NAME, MQTTPendingAction.ID
							+ "=?", new String[] { id });
	}
	
	/**
	 * 保存MQTT失败缓存
	 * 
	 * @Description:
	 * @param
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-1-9
	 */
	public void saveMQTTPending(final PendingMQTTConmmand pendingMQTTConmmand) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(MQTTPendingAction.ACTION, pendingMQTTConmmand
				.getAction().ordinal());
		contentValues.put(MQTTPendingAction.COMMAND, pendingMQTTConmmand
				.getCommand().ordinal());
		contentValues.put(MQTTPendingAction.TOPIC,
				pendingMQTTConmmand.getTopic());
		contentValues.put(MQTTPendingAction.CONTENT,
				pendingMQTTConmmand.getContent());
		db.insert(MQTTPendingAction.TB_NAME, null, contentValues);
	}
	

	/**
	 * 获取MQTT失败缓存
	 * 
	 * @Description:
	 * @param
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-1-9
	 */
	public ArrayList<PendingMQTTConmmand> getMQTTPending() {
		Cursor cursor = null;
		ArrayList<PendingMQTTConmmand> pendingMQTTConmmands = null;
		try {
			SQLiteDatabase db =getReadableDatabase();
			cursor = db.query(MQTTPendingAction.TB_NAME, null, null, null,
					null, null, null);
			pendingMQTTConmmands = new ArrayList<PendingMQTTConmmand>();
			while (cursor.moveToNext()) {
				PendingMQTTConmmand pendingMQTTConmmand = new PendingMQTTConmmand();
				pendingMQTTConmmand.setId(cursor.getInt(cursor
						.getColumnIndex(MQTTPendingAction.ID)));
				pendingMQTTConmmand
						.setAction(ActionListener.Action.values()[cursor.getInt((cursor
								.getColumnIndex(MQTTPendingAction.ACTION)))]);
				pendingMQTTConmmand.setCommand(MQTTCommand.values()[cursor
						.getInt(cursor
								.getColumnIndex(MQTTPendingAction.COMMAND))]);
				pendingMQTTConmmand.setTopic(cursor.getString(cursor
						.getColumnIndex(MQTTPendingAction.TOPIC)));
				pendingMQTTConmmand.setContent(cursor.getString(cursor
						.getColumnIndex(MQTTPendingAction.CONTENT)));
				pendingMQTTConmmands.add(pendingMQTTConmmand);
			}
			return pendingMQTTConmmands;
		} finally {
			Utility.closeQuietly(cursor);
		}
	}

}
