package cn.mailchat.mail.store;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.james.mime4j.codec.QuotedPrintableOutputStream;
import org.apache.james.mime4j.util.MimeUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.Telephony.Mms.Inbox;
import android.text.TextUtils;
import android.util.Log;
import cn.mailchat.Account;
import cn.mailchat.Account.MessageFormat;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.FolderInfoHolder;
import cn.mailchat.activity.Search;
import cn.mailchat.activity.UpgradeDatabases;
import cn.mailchat.beans.Eis35Bean;
import cn.mailchat.beans.SearchVo;
import cn.mailchat.chatting.beans.CAttachment;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.chatting.beans.CMessage;
import cn.mailchat.chatting.beans.CMessage.State;
import cn.mailchat.chatting.beans.CMessage.Type;
import cn.mailchat.chatting.beans.DAttachment;
import cn.mailchat.chatting.beans.DChat;
import cn.mailchat.chatting.beans.DChatMessage;
import cn.mailchat.chatting.beans.MixedChatting;
import cn.mailchat.chatting.beans.PendingHTTPSCommand;
import cn.mailchat.chatting.beans.PendingMQTTConmmand;
import cn.mailchat.chatting.protocol.ActionListener;
import cn.mailchat.chatting.protocol.ActionListener.Action;
import cn.mailchat.chatting.protocol.Command;
import cn.mailchat.chatting.protocol.MQTTCommand;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessageException;
import cn.mailchat.controller.MessageRemovalListener;
import cn.mailchat.controller.MessageRetrievalListener;
import cn.mailchat.helper.HtmlConverter;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.helper.Utility;
import cn.mailchat.mail.Address;
import cn.mailchat.mail.Body;
import cn.mailchat.mail.BodyPart;
import cn.mailchat.mail.CompositeBody;
import cn.mailchat.mail.FetchProfile;
import cn.mailchat.mail.Flag;
import cn.mailchat.mail.Folder;
import cn.mailchat.mail.Message;
import cn.mailchat.mail.Message.RecipientType;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.mail.Part;
import cn.mailchat.mail.Store;
import cn.mailchat.mail.filter.Base64OutputStream;
import cn.mailchat.mail.internet.MimeBodyPart;
import cn.mailchat.mail.internet.MimeHeader;
import cn.mailchat.mail.internet.MimeMessage;
import cn.mailchat.mail.internet.MimeMultipart;
import cn.mailchat.mail.internet.MimeUtility;
import cn.mailchat.mail.internet.MimeUtility.ViewableContainer;
import cn.mailchat.mail.internet.TextBody;
import cn.mailchat.mail.store.Columns.HTTPSPendingAction;
import cn.mailchat.mail.store.Columns.MQTTPendingAction;
import cn.mailchat.mail.store.Columns.TbBusinessContactDepartment;
import cn.mailchat.mail.store.Columns.TbBusinessContactDepartment_User;
import cn.mailchat.mail.store.Columns.TbBusinessContactUser;
import cn.mailchat.mail.store.Columns.TbCAttachments;
import cn.mailchat.mail.store.Columns.TbCGroup;
import cn.mailchat.mail.store.Columns.TbCGroup_CMember;
import cn.mailchat.mail.store.Columns.TbCMember;
import cn.mailchat.mail.store.Columns.TbCMessages;
import cn.mailchat.mail.store.Columns.TbContactRemark;
import cn.mailchat.mail.store.Columns.TbDAttachments;
import cn.mailchat.mail.store.Columns.TbDchat;
import cn.mailchat.mail.store.Columns.TbDchatMessage;
import cn.mailchat.mail.store.Columns.TbUserContacts;
import cn.mailchat.mail.store.ImapStore.ImapMessage;
import cn.mailchat.mail.store.LockableDatabase.DbCallback;
import cn.mailchat.mail.store.LockableDatabase.WrappedException;
import cn.mailchat.mail.store.StorageManager.StorageProvider;
import cn.mailchat.provider.AttachmentProvider;
import cn.mailchat.provider.ChattingProvider;
import cn.mailchat.provider.EmailProvider;
import cn.mailchat.provider.EmailProvider.MessageColumns;
import cn.mailchat.search.LocalSearch;
import cn.mailchat.search.SearchResult;
import cn.mailchat.search.SearchSpecification.Attribute;
import cn.mailchat.search.SearchSpecification.Searchfield;
import cn.mailchat.search.SqlQueryBuilder;
import cn.mailchat.utils.DateUtil;
import cn.mailchat.utils.GlobalTools;
import cn.mailchat.utils.HanziToPinyin;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.view.AttachmentView;

/**
 * <pre>
 * Implements a SQLite database backed local store for Messages.
 * </pre>
 */
public class LocalStore extends Store implements Serializable {

	private static final long serialVersionUID = -5142141896809423072L;
	
	private static final String TAG = "LocalStore";

	// 群组成员 信息 缓存Cach
	public static HashMap<String, CGroupMember> cMembersTmpMap = new HashMap<String, CGroupMember>();// uid+groupUid

	/**
	 * 单聊列表
	 */
	private static String SQL_TABLE_TB_DCHAT_CREATE;
	private static String SQL_TABLE_TB_DCHAT_CLEAR = "DROP TABLE IF EXISTS "
			+ Columns.TbDchat.TB_NAME;

	/**
	 * 单聊消息
	 */
	private static String SQL_TABLE_TB_DMESSAGE_CREATE;
	private static String SQL_TABLE_TB_DMESSAGE_CLEAR = "DROP TABLE IF EXISTS "
			+ Columns.TbDchatMessage.TB_NAME;

	/**
	 * 单聊附件
	 */
	private static String SQL_TABLE_TB_DATTCHMENT_CREATE;
	private static String SQL_TABLE_TB_DATTCHMENT_CLEAR = "DROP TABLE IF EXISTS "
			+ Columns.TbDAttachments.TB_NAME;

	/**
	 * 联系人列表
	 */
	private static String SQL_TABLE_TB_USER_CONTACTS_CREATE;
	private static String SQL_TABLE_TB_USER_CONTACTS_CLEAR = "DROP TABLE IF EXISTS "
			+ Columns.TbUserContacts.TB_NAME;

	/**
	 * 创建群
	 */
	private static String SQL_TABLE_TB_CGROUP_CREATE;
	private static final String SQL_TABLE_TB_CGROUP_CLEAR = "DROP TABLE IF EXISTS "
			+ Columns.TbCGroup.TB_NAME;

	/**
	 * 群成员
	 */
	private static String SQL_TABLE_TB_CMEMBER_CREATE;
	private static final String SQL_TABLE_TB_CMEMBER_CLEAR = "DROP TABLE IF EXISTS "
			+ Columns.TbCMember.TB_NAME;
	/**
	 * 关系表
	 */
	private static String SQL_TABLE_TB_CGROUP_CMEMBER_CREATE;
	private static final String SQL_TABLE_TB_CGROUP_CMEMBER_CLEAR = "DROP TABLE IF EXISTS "
			+ Columns.TbCGroup_CMember.TB_NAME;
	/**
	 * 群消息
	 */
	private static String SQL_TABLE_TB_CMESSAGES_CREATE;
	private static final String SQL_TABLE_TB_CMESSAGES_CLEAR = "DROP TABLE IF EXISTS "
			+ Columns.TbCMessages.TB_NAME;

	/**
	 * 群附件
	 */
	private static String SQL_TABLE_TB_CATTACHMENTS_CREATE;
	private static String SQL_TABLE_TB_CATTACHMENTS_CLEAR = "DROP TABLE IF EXISTS "
			+ Columns.TbCAttachments.TB_NAME;
	/**
	 * MQTT失败缓存
	 */
	private static String SQL_TABLE_TB_MQTT_PENDING_CREATE;
	private static String SQL_TABLE_TB_MQTT_PENDING__CLEAR = "DROP TABLE IF EXISTS "
			+ Columns.MQTTPendingAction.TB_NAME;
	/**
	 * HTTPS失败缓存
	 */
	private static String SQL_TABLE_TB_HTTPS_PENDING_CREATE;
	private static String SQL_TABLE_TB_HTTPS_PENDING__CLEAR = "DROP TABLE IF EXISTS "
			+ Columns.HTTPSPendingAction.TB_NAME;

    /**
     * 企业联系人部门表
     */
    private static String SQL_TABLE_TB_B_CONTACT_DEPARTMENT_CREATE;
    private static String SQL_TABLE_TB_B_CONTACT_DEPARTMENT_CLEAR = "DROP TABLE IF EXISTS "
            + Columns.HTTPSPendingAction.TB_NAME;

    /**
     * 企业联系人成员表
     */
    private static String SQL_TABLE_TB_B_CONTACT_USER_CREATE;
    private static String SQL_TABLE_TB_B_CONTACT_USER_CLEAR = "DROP TABLE IF EXISTS "
            + Columns.HTTPSPendingAction.TB_NAME;

    /**
     * 企业联系人关系表
     */
    private static String SQL_TABLE_TB_B_CONTACT_DEPARTMENT_USER_CREATE;
    private static String SQL_TABLE_TB_B_CONTACT_DEPARTMENT_USER_CLEAR = "DROP TABLE IF EXISTS "
            + Columns.HTTPSPendingAction.TB_NAME;

	private static final Message[] EMPTY_MESSAGE_ARRAY = new Message[0];
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final Flag[] EMPTY_FLAG_ARRAY = new Flag[0];
	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	/*
	 * a String containing the columns getMessages expects to work with in the
	 * correct order.
	 */
	static private String GET_MESSAGES_COLS = "subject, sender_list, date, uid, flags, messages.id, to_list, cc_list, "
			+ "bcc_list, reply_to_list, attachment_count, internal_date, messages.message_id, "
			+ "folder_id, preview, threads.id, threads.root, deleted, read, flagged, answered, "
			+ "forwarded, mail_id ";

	private static final String GET_FOLDER_COLS = "folders.id, name, visible_limit, last_updated, status, push_state, last_pushed, "
			+ "integrate, top_group, poll_class, push_class, display_class, notify_class,is_allow_push,is_custom_folder";

	private static final int FOLDER_ID_INDEX = 0;
	private static final int FOLDER_NAME_INDEX = 1;
	private static final int FOLDER_VISIBLE_LIMIT_INDEX = 2;
	private static final int FOLDER_LAST_CHECKED_INDEX = 3;
	private static final int FOLDER_STATUS_INDEX = 4;
	private static final int FOLDER_PUSH_STATE_INDEX = 5;
	private static final int FOLDER_LAST_PUSHED_INDEX = 6;
	private static final int FOLDER_INTEGRATE_INDEX = 7;
	private static final int FOLDER_TOP_GROUP_INDEX = 8;
	private static final int FOLDER_SYNC_CLASS_INDEX = 9;
	private static final int FOLDER_PUSH_CLASS_INDEX = 10;
	private static final int FOLDER_DISPLAY_CLASS_INDEX = 11;
	private static final int FOLDER_NOTIFY_CLASS_INDEX = 12;
	private static final int FOLDER_IS_ALLOW_PUSH_STATE_INDEX = 13;
	private static final int FOLDER_CUSTOM_FOLDER = 14;
	private static final String[] UID_CHECK_PROJECTION = { "uid" };

	/**
	 * Maximum number of UIDs to check for existence at once.
	 * 
	 * @see LocalFolder#extractNewMessages(List)
	 */
	private static final int UID_CHECK_BATCH_SIZE = 500;

	/**
	 * Maximum number of messages to perform flag updates on at once.
	 * 
	 * @see #setFlag(List, Flag, boolean, boolean)
	 */
	private static final int FLAG_UPDATE_BATCH_SIZE = 500;

	/**
	 * Maximum number of threads to perform flag updates on at once.
	 * 
	 * @see #setFlagForThreads(List, Flag, boolean)
	 */
	private static final int THREAD_FLAG_UPDATE_BATCH_SIZE = 500;

	public static final int DB_VERSION = 77;

	public static String getColumnNameForFlag(Flag flag) {
		switch (flag) {
		case SEEN: {
			return MessageColumns.READ;
		}
		case FLAGGED: {
			return MessageColumns.FLAGGED;
		}
		case ANSWERED: {
			return MessageColumns.ANSWERED;
		}
		case FORWARDED: {
			return MessageColumns.FORWARDED;
		}
		default: {
			throw new IllegalArgumentException(
					"Flag must be a special column flag");
		}
		}
	}

	protected String uUid = null;

	private final Application mApplication;

	private LockableDatabase database;

	private ContentResolver mContentResolver;
	private boolean isAddOnce=false;
	/**
	 * local://localhost/path/to/database/uuid.db This constructor is only used
	 * by {@link Store#getLocalInstance(Account, Application)}
	 * 
	 * @param account
	 * @param application
	 * @throws UnavailableStorageException
	 *             if not {@link StorageProvider#isReady(Context)}
	 */
	public LocalStore(final Account account, final Application application)
			throws MessagingException {
		super(account);
		database = new LockableDatabase(application, account.getUuid(),
				new StoreSchemaDefinition());

		mApplication = application;
		mContentResolver = application.getContentResolver();
		database.setStorageProviderId(account.getLocalStorageProviderId());
		uUid = account.getUuid();

		database.open();
	}

	public void switchLocalStorage(final String newStorageProviderId)
			throws MessagingException {
		database.switchProvider(newStorageProviderId);
	}

	protected SharedPreferences getPreferences() {
		return Preferences.getPreferences(mApplication).getPreferences();
	}

	private class StoreSchemaDefinition implements
			LockableDatabase.SchemaDefinition {
		@Override
		public int getVersion() {
			return DB_VERSION;
		}

		@Override
		public void doDbUpgrade(final SQLiteDatabase db) {
			try {
				upgradeDatabase(db);
			} catch (Exception e) {
				Log.e(MailChat.LOG_TAG,
						"Exception while upgrading database. Resetting the DB to v0",
						e);
				db.setVersion(0);
				upgradeDatabase(db);
			}
		}

		private void upgradeDatabase(final SQLiteDatabase db) {
			Log.i(MailChat.LOG_TAG, String.format(Locale.US,
					"Upgrading database from version %d to version %d",
					db.getVersion(), DB_VERSION));

			AttachmentProvider.clear(mApplication);

			db.beginTransaction();
			try {
				// schema version 29 was when we moved to incremental updates
				// in the case of a new db or a < v29 db, we blow away and start
				// from scratch
				if (db.getVersion() < 29) {

				    db.execSQL(SQL_TABLE_TB_DCHAT_CLEAR);
				    db.execSQL(SQL_TABLE_TB_DMESSAGE_CLEAR);
				    db.execSQL(SQL_TABLE_TB_DATTCHMENT_CLEAR);
				    db.execSQL(SQL_TABLE_TB_USER_CONTACTS_CLEAR);
				    db.execSQL(SQL_TABLE_TB_CGROUP_CLEAR);
				    db.execSQL(SQL_TABLE_TB_CMEMBER_CLEAR);
				    db.execSQL(SQL_TABLE_TB_CGROUP_CMEMBER_CLEAR);
				    db.execSQL(SQL_TABLE_TB_CMESSAGES_CLEAR);
				    db.execSQL(SQL_TABLE_TB_CATTACHMENTS_CLEAR);
				    db.execSQL(SQL_TABLE_TB_MQTT_PENDING__CLEAR);
				    db.execSQL(SQL_TABLE_TB_HTTPS_PENDING__CLEAR);
				    db.execSQL(SQL_TABLE_TB_B_CONTACT_DEPARTMENT_CLEAR);
				    db.execSQL(SQL_TABLE_TB_B_CONTACT_USER_CLEAR);
				    db.execSQL(SQL_TABLE_TB_B_CONTACT_DEPARTMENT_USER_CLEAR);

					db.execSQL("DROP TABLE IF EXISTS folders");
					db.execSQL("CREATE TABLE folders (id INTEGER PRIMARY KEY, name TEXT, "
							+ "last_updated INTEGER, unread_count INTEGER, visible_limit INTEGER, status TEXT, "
							+ "push_state TEXT, last_pushed INTEGER, flagged_count INTEGER default 0, "
							+ "integrate INTEGER, top_group INTEGER, poll_class TEXT, push_class TEXT, display_class TEXT, notify_class TEXT"
							+ ")");

					db.execSQL("CREATE INDEX IF NOT EXISTS folder_name ON folders (name)");
					db.execSQL("DROP TABLE IF EXISTS messages");
					db.execSQL("CREATE TABLE messages ("
							+ "id INTEGER PRIMARY KEY, "
							+ "deleted INTEGER default 0, "
							+ "folder_id INTEGER, " + "uid TEXT, "
							+ "subject TEXT, " + "date INTEGER, "
							+ "flags TEXT, " + "sender_list TEXT, "
							+ "to_list TEXT, " + "cc_list TEXT, "
							+ "bcc_list TEXT, " + "reply_to_list TEXT, "
							+ "html_content TEXT, " + "text_content TEXT, "
							+ "attachment_count INTEGER, "
							+ "internal_date INTEGER, " + "message_id TEXT, "
							+ "preview TEXT, " + "mime_type TEXT, "
							+ "normalized_subject_hash INTEGER, "
							+ "empty INTEGER, " + "read INTEGER default 0, "
							+ "flagged INTEGER default 0, "
							+ "answered INTEGER default 0, "
							+ "forwarded INTEGER default 0" + ")");

					db.execSQL("DROP TABLE IF EXISTS headers");
					db.execSQL("CREATE TABLE headers (id INTEGER PRIMARY KEY, message_id INTEGER, name TEXT, value TEXT)");
					db.execSQL("CREATE INDEX IF NOT EXISTS header_folder ON headers (message_id)");

					db.execSQL("CREATE INDEX IF NOT EXISTS msg_uid ON messages (uid, folder_id)");
					db.execSQL("DROP INDEX IF EXISTS msg_folder_id");
					db.execSQL("DROP INDEX IF EXISTS msg_folder_id_date");
					db.execSQL("CREATE INDEX IF NOT EXISTS msg_folder_id_deleted_date ON messages (folder_id,deleted,internal_date)");

					db.execSQL("DROP INDEX IF EXISTS msg_empty");
					db.execSQL("CREATE INDEX IF NOT EXISTS msg_empty ON messages (empty)");

					db.execSQL("DROP INDEX IF EXISTS msg_read");
					db.execSQL("CREATE INDEX IF NOT EXISTS msg_read ON messages (read)");

					db.execSQL("DROP INDEX IF EXISTS msg_flagged");
					db.execSQL("CREATE INDEX IF NOT EXISTS msg_flagged ON messages (flagged)");

					db.execSQL("DROP INDEX IF EXISTS msg_composite");
					db.execSQL("CREATE INDEX IF NOT EXISTS msg_composite ON messages (deleted, empty,folder_id,flagged,read)");

					db.execSQL("DROP TABLE IF EXISTS threads");
					db.execSQL("CREATE TABLE threads ("
							+ "id INTEGER PRIMARY KEY, "
							+ "message_id INTEGER, " + "root INTEGER, "
							+ "parent INTEGER" + ")");

					db.execSQL("DROP INDEX IF EXISTS threads_message_id");
					db.execSQL("CREATE INDEX IF NOT EXISTS threads_message_id ON threads (message_id)");

					db.execSQL("DROP INDEX IF EXISTS threads_root");
					db.execSQL("CREATE INDEX IF NOT EXISTS threads_root ON threads (root)");

					db.execSQL("DROP INDEX IF EXISTS threads_parent");
					db.execSQL("CREATE INDEX IF NOT EXISTS threads_parent ON threads (parent)");

					db.execSQL("DROP TRIGGER IF EXISTS set_thread_root");
					db.execSQL("CREATE TRIGGER set_thread_root "
							+ "AFTER INSERT ON threads "
							+ "BEGIN "
							+ "UPDATE threads SET root=id WHERE root IS NULL AND ROWID = NEW.ROWID; "
							+ "END");

					db.execSQL("DROP TABLE IF EXISTS attachments");
					db.execSQL("CREATE TABLE attachments (id INTEGER PRIMARY KEY, message_id INTEGER,"
							+ "store_data TEXT, content_uri TEXT, size INTEGER, name TEXT,"
							+ "mime_type TEXT, content_id TEXT, content_disposition TEXT)");

					db.execSQL("DROP TABLE IF EXISTS pending_commands");
					db.execSQL("CREATE TABLE pending_commands "
							+ "(id INTEGER PRIMARY KEY, command TEXT, arguments TEXT)");

					db.execSQL("DROP TRIGGER IF EXISTS delete_folder");
					db.execSQL("CREATE TRIGGER delete_folder BEFORE DELETE ON folders BEGIN DELETE FROM messages WHERE old.id = folder_id; END;");

					db.execSQL("DROP TRIGGER IF EXISTS delete_message");
					db.execSQL("CREATE TRIGGER delete_message BEFORE DELETE ON messages BEGIN DELETE FROM attachments WHERE old.id = message_id; "
							+ "DELETE FROM headers where old.id = message_id; END;");
					mailchatDB(db);
				} else {
					// in the case that we're starting out at 29 or newer, run
					// all the needed updates

					if (db.getVersion() < 30) {
						try {
							db.execSQL("ALTER TABLE messages ADD deleted INTEGER default 0");
						} catch (SQLiteException e) {
							if (!e.toString().startsWith(
									"duplicate column name: deleted")) {
								throw e;
							}
						}
					}
					if (db.getVersion() < 31) {
						db.execSQL("DROP INDEX IF EXISTS msg_folder_id_date");
						db.execSQL("CREATE INDEX IF NOT EXISTS msg_folder_id_deleted_date ON messages (folder_id,deleted,internal_date)");
					}
					if (db.getVersion() < 32) {
						db.execSQL("UPDATE messages SET deleted = 1 WHERE flags LIKE '%DELETED%'");
					}
					if (db.getVersion() < 33) {

						try {
							db.execSQL("ALTER TABLE messages ADD preview TEXT");
						} catch (SQLiteException e) {
							if (!e.toString().startsWith(
									"duplicate column name: preview")) {
								throw e;
							}
						}

					}
					if (db.getVersion() < 34) {
						try {
							db.execSQL("ALTER TABLE folders ADD flagged_count INTEGER default 0");
						} catch (SQLiteException e) {
							if (!e.getMessage().startsWith(
									"duplicate column name: flagged_count")) {
								throw e;
							}
						}
					}
					if (db.getVersion() < 35) {
						try {
							db.execSQL("update messages set flags = replace(flags, 'X_NO_SEEN_INFO', 'X_BAD_FLAG')");
						} catch (SQLiteException e) {
							Log.e(MailChat.LOG_TAG,
									"Unable to get rid of obsolete flag X_NO_SEEN_INFO",
									e);
						}
					}
					if (db.getVersion() < 36) {
						try {
							db.execSQL("ALTER TABLE attachments ADD content_id TEXT");
						} catch (SQLiteException e) {
							Log.e(MailChat.LOG_TAG,
									"Unable to add content_id column to attachments");
						}
					}
					if (db.getVersion() < 37) {
						try {
							db.execSQL("ALTER TABLE attachments ADD content_disposition TEXT");
						} catch (SQLiteException e) {
							Log.e(MailChat.LOG_TAG,
									"Unable to add content_disposition column to attachments");
						}
					}

					// Database version 38 is solely to prune cached attachments
					// now that we clear them better
					if (db.getVersion() < 39) {
						try {
							db.execSQL("DELETE FROM headers WHERE id in (SELECT headers.id FROM headers LEFT JOIN messages ON headers.message_id = messages.id WHERE messages.id IS NULL)");
						} catch (SQLiteException e) {
							Log.e(MailChat.LOG_TAG,
									"Unable to remove extra header data from the database");
						}
					}

					// V40: Store the MIME type for a message.
					if (db.getVersion() < 40) {
						try {
							db.execSQL("ALTER TABLE messages ADD mime_type TEXT");
						} catch (SQLiteException e) {
							Log.e(MailChat.LOG_TAG,
									"Unable to add mime_type column to messages");
						}
					}

					if (db.getVersion() < 41) {
						try {
							db.execSQL("ALTER TABLE folders ADD integrate INTEGER");
							db.execSQL("ALTER TABLE folders ADD top_group INTEGER");
							db.execSQL("ALTER TABLE folders ADD poll_class TEXT");
							db.execSQL("ALTER TABLE folders ADD push_class TEXT");
							db.execSQL("ALTER TABLE folders ADD display_class TEXT");
						} catch (SQLiteException e) {
							if (!e.getMessage().startsWith(
									"duplicate column name:")) {
								throw e;
							}
						}

						Cursor cursor = null;
						try {
							SharedPreferences prefs = getPreferences();
							cursor = db.rawQuery(
									"SELECT id, name FROM folders", null);
							while (cursor.moveToNext()) {
								try {
									int id = cursor.getInt(0);
									String name = cursor.getString(1);
									update41Metadata(db, prefs, id, name);
								} catch (Exception e) {
									Log.e(MailChat.LOG_TAG,
											" error trying to ugpgrade a folder class",
											e);
								}
							}
						} catch (SQLiteException e) {
							Log.e(MailChat.LOG_TAG,
									"Exception while upgrading database to v41. folder classes may have vanished",
									e);
						} finally {
							Utility.closeQuietly(cursor);
						}
					}
					if (db.getVersion() == 41) {
						try {
							long startTime = System.currentTimeMillis();
							SharedPreferences.Editor editor = getPreferences()
									.edit();

							List<? extends Folder> folders = getPersonalNamespaces(true);
							for (Folder folder : folders) {
								if (folder instanceof LocalFolder) {
									LocalFolder lFolder = (LocalFolder) folder;
									lFolder.save(editor);
								}
							}

							editor.commit();
							long endTime = System.currentTimeMillis();
							Log.i(MailChat.LOG_TAG,
									"Putting folder preferences for "
											+ folders.size()
											+ " folders back into Preferences took "
											+ (endTime - startTime) + " ms");
						} catch (Exception e) {
							Log.e(MailChat.LOG_TAG,
									"Could not replace Preferences in upgrade from DB_VERSION 41",
									e);
						}
					}
					if (db.getVersion() < 43) {
						try {
							// If folder "OUTBOX" (old, v3.800 - v3.802) exists,
							// rename it to
							// "K9MAIL_INTERNAL_OUTBOX" (new)
							LocalFolder oldOutbox = new LocalFolder("OUTBOX");
							if (oldOutbox.exists()) {
								ContentValues cv = new ContentValues();
								cv.put("name", Account.OUTBOX);
								db.update("folders", cv, "name = ?",
										new String[] { "OUTBOX" });
								Log.i(MailChat.LOG_TAG,
										"Renamed folder OUTBOX to "
												+ Account.OUTBOX);
							}

							// Check if old (pre v3.800) localized outbox folder
							// exists
							String localizedOutbox = MailChat.app
									.getString(R.string.special_mailbox_name_outbox);
							LocalFolder obsoleteOutbox = new LocalFolder(
									localizedOutbox);
							if (obsoleteOutbox.exists()) {
								// Get all messages from the localized outbox
								// ...
								Message[] messages = obsoleteOutbox
										.getMessages(null, false);

								if (messages.length > 0) {
									// ... and move them to the drafts folder
									// (we don't want to
									// surprise the user by sending potentially
									// very old messages)
									LocalFolder drafts = new LocalFolder(
											mAccount.getDraftsFolderName());
									obsoleteOutbox.moveMessages(messages,
											drafts);
								}

								// Now get rid of the localized outbox
								obsoleteOutbox.delete();
								obsoleteOutbox.delete(true);
							}
						} catch (Exception e) {
							Log.e(MailChat.LOG_TAG,
									"Error trying to fix the outbox folders", e);
						}
					}
					if (db.getVersion() < 44) {
						try {
							db.execSQL("ALTER TABLE messages ADD thread_root INTEGER");
							db.execSQL("ALTER TABLE messages ADD thread_parent INTEGER");
							db.execSQL("ALTER TABLE messages ADD normalized_subject_hash INTEGER");
							db.execSQL("ALTER TABLE messages ADD empty INTEGER");
						} catch (SQLiteException e) {
							if (!e.getMessage().startsWith(
									"duplicate column name:")) {
								throw e;
							}
						}
					}
					if (db.getVersion() < 45) {
						try {
							db.execSQL("DROP INDEX IF EXISTS msg_empty");
							db.execSQL("CREATE INDEX IF NOT EXISTS msg_empty ON messages (empty)");

							db.execSQL("DROP INDEX IF EXISTS msg_thread_root");
							db.execSQL("CREATE INDEX IF NOT EXISTS msg_thread_root ON messages (thread_root)");

							db.execSQL("DROP INDEX IF EXISTS msg_thread_parent");
							db.execSQL("CREATE INDEX IF NOT EXISTS msg_thread_parent ON messages (thread_parent)");
						} catch (SQLiteException e) {
							if (!e.getMessage().startsWith(
									"duplicate column name:")) {
								throw e;
							}
						}
					}
					if (db.getVersion() < 46) {
						db.execSQL("ALTER TABLE messages ADD read INTEGER default 0");
						db.execSQL("ALTER TABLE messages ADD flagged INTEGER default 0");
						db.execSQL("ALTER TABLE messages ADD answered INTEGER default 0");
						db.execSQL("ALTER TABLE messages ADD forwarded INTEGER default 0");

						String[] projection = { "id", "flags" };

						ContentValues cv = new ContentValues();
						List<Flag> extraFlags = new ArrayList<Flag>();

						Cursor cursor = db.query("messages", projection, null,
								null, null, null, null);
						try {
							while (cursor.moveToNext()) {
								long id = cursor.getLong(0);
								String flagList = cursor.getString(1);

								boolean read = false;
								boolean flagged = false;
								boolean answered = false;
								boolean forwarded = false;

								if (flagList != null && flagList.length() > 0) {
									String[] flags = flagList.split(",");

									for (String flagStr : flags) {
										try {
											Flag flag = Flag.valueOf(flagStr);

											switch (flag) {
											case ANSWERED: {
												answered = true;
												break;
											}
											case DELETED: {
												// Don't store this in column
												// 'flags'
												break;
											}
											case FLAGGED: {
												flagged = true;
												break;
											}
											case FORWARDED: {
												forwarded = true;
												break;
											}
											case SEEN: {
												read = true;
												break;
											}
											case DRAFT:
											case RECENT:
											case X_DESTROYED:
											case X_DOWNLOADED_FULL:
											case X_DOWNLOADED_PARTIAL:
											case X_GOT_ALL_HEADERS:
											case X_REMOTE_COPY_STARTED:
											case X_SEND_FAILED:
											case X_SEND_IN_PROGRESS: {
												extraFlags.add(flag);
												break;
											}
											}
										} catch (Exception e) {
											// Ignore bad flags
										}
									}
								}

								cv.put("flags", serializeFlags(extraFlags
										.toArray(EMPTY_FLAG_ARRAY)));
								cv.put("read", read);
								cv.put("flagged", flagged);
								cv.put("answered", answered);
								cv.put("forwarded", forwarded);

								db.update("messages", cv, "id = ?",
										new String[] { Long.toString(id) });

								cv.clear();
								extraFlags.clear();
							}
						} finally {
							cursor.close();
						}

						db.execSQL("CREATE INDEX IF NOT EXISTS msg_read ON messages (read)");
						db.execSQL("CREATE INDEX IF NOT EXISTS msg_flagged ON messages (flagged)");
					}

					if (db.getVersion() < 47) {
						// Create new 'threads' table
						db.execSQL("DROP TABLE IF EXISTS threads");
						db.execSQL("CREATE TABLE threads ("
								+ "id INTEGER PRIMARY KEY, "
								+ "message_id INTEGER, " + "root INTEGER, "
								+ "parent INTEGER" + ")");

						// Create indices for new table
						db.execSQL("DROP INDEX IF EXISTS threads_message_id");
						db.execSQL("CREATE INDEX IF NOT EXISTS threads_message_id ON threads (message_id)");

						db.execSQL("DROP INDEX IF EXISTS threads_root");
						db.execSQL("CREATE INDEX IF NOT EXISTS threads_root ON threads (root)");

						db.execSQL("DROP INDEX IF EXISTS threads_parent");
						db.execSQL("CREATE INDEX IF NOT EXISTS threads_parent ON threads (parent)");

						// Create entries for all messages in 'threads' table
						db.execSQL("INSERT INTO threads (message_id) SELECT id FROM messages");

						// Copy thread structure from 'messages' table to
						// 'threads'
						Cursor cursor = db.query("messages", new String[] {
								"id", "thread_root", "thread_parent" }, null,
								null, null, null, null);
						try {
							ContentValues cv = new ContentValues();
							while (cursor.moveToNext()) {
								cv.clear();
								long messageId = cursor.getLong(0);

								if (!cursor.isNull(1)) {
									long threadRootMessageId = cursor
											.getLong(1);
									db.execSQL(
											"UPDATE threads SET root = (SELECT t.id FROM "
													+ "threads t WHERE t.message_id = ?) "
													+ "WHERE message_id = ?",
											new String[] {
													Long.toString(threadRootMessageId),
													Long.toString(messageId) });
								}

								if (!cursor.isNull(2)) {
									long threadParentMessageId = cursor
											.getLong(2);
									db.execSQL(
											"UPDATE threads SET parent = (SELECT t.id FROM "
													+ "threads t WHERE t.message_id = ?) "
													+ "WHERE message_id = ?",
											new String[] {
													Long.toString(threadParentMessageId),
													Long.toString(messageId) });
								}
							}
						} finally {
							cursor.close();
						}

						// Remove indices for old thread-related columns in
						// 'messages' table
						db.execSQL("DROP INDEX IF EXISTS msg_thread_root");
						db.execSQL("DROP INDEX IF EXISTS msg_thread_parent");

						// Clear out old thread-related columns in 'messages'
						ContentValues cv = new ContentValues();
						cv.putNull("thread_root");
						cv.putNull("thread_parent");
						db.update("messages", cv, null, null);
					}

					if (db.getVersion() < 48) {
						db.execSQL("UPDATE threads SET root=id WHERE root IS NULL");

						db.execSQL("CREATE TRIGGER set_thread_root "
								+ "AFTER INSERT ON threads "
								+ "BEGIN "
								+ "UPDATE threads SET root=id WHERE root IS NULL AND ROWID = NEW.ROWID; "
								+ "END");
					}
					if (db.getVersion() < 49) {
						db.execSQL("CREATE INDEX IF NOT EXISTS msg_composite ON messages (deleted, empty,folder_id,flagged,read)");

					}
					if (db.getVersion() < 50) {
						try {
							db.execSQL("ALTER TABLE folders ADD notify_class TEXT default '"
									+ Folder.FolderClass.INHERITED.name() + "'");
						} catch (SQLiteException e) {
							if (!e.getMessage().startsWith(
									"duplicate column name:")) {
								throw e;
							}
						}

						ContentValues cv = new ContentValues();
						cv.put("notify_class",
								Folder.FolderClass.FIRST_CLASS.name());

						db.update("folders", cv, "name = ?",
								new String[] { getAccount()
										.getInboxFolderName() });
					}
				}
				if (db.getVersion() < 57) {
					// 这次将以前不要的字段删除，将附件标字段统一，用于滑动大图用.出包时注意打开,在这个内测版本
					// 删除更新相关表格
					db.execSQL(SQL_TABLE_TB_DCHAT_CLEAR);
					db.execSQL(SQL_TABLE_TB_DMESSAGE_CLEAR);
					db.execSQL(SQL_TABLE_TB_DATTCHMENT_CLEAR);
					db.execSQL(SQL_TABLE_TB_CGROUP_CLEAR);
					db.execSQL(SQL_TABLE_TB_CMEMBER_CLEAR);
					db.execSQL(SQL_TABLE_TB_CMESSAGES_CLEAR);
					db.execSQL(SQL_TABLE_TB_CATTACHMENTS_CLEAR);
					db.execSQL(SQL_TABLE_TB_CGROUP_CMEMBER_CLEAR);

					// 单聊相关表格
					db.execSQL(SQL_TABLE_TB_DCHAT_CREATE);
					db.execSQL(SQL_TABLE_TB_DMESSAGE_CREATE);
					db.execSQL(SQL_TABLE_TB_DATTCHMENT_CREATE);
					// 群组相关表
					db.execSQL(SQL_TABLE_TB_CGROUP_CREATE);
					db.execSQL(SQL_TABLE_TB_CMEMBER_CREATE);
					db.execSQL(SQL_TABLE_TB_CMESSAGES_CREATE);
					db.execSQL(SQL_TABLE_TB_CATTACHMENTS_CREATE);
					db.execSQL(SQL_TABLE_TB_CGROUP_CMEMBER_CREATE);
				}
				if (db.getVersion() < 58) {
					mailchatPendingDB(db);
				}
				if (db.getVersion() < 59) {
					db.execSQL("ALTER TABLE "+Columns.TbCAttachments.TB_NAME+" ADD " +Columns.TbCAttachments.F_DOWNLOAD_PAUSE_FLAG+ " INTEGER default 0");
					db.execSQL("ALTER TABLE "+Columns.TbCAttachments.TB_NAME+" ADD " +Columns.TbCAttachments.F_DOWNLOAD_PROGRESS+ " INTEGER default 0");
					db.execSQL("ALTER TABLE "+Columns.TbDAttachments.TB_NAME+" ADD " +Columns.TbDAttachments.F_DOWNLOAD_PAUSE_FLAG+ " INTEGER default 0");
					db.execSQL("ALTER TABLE "+Columns.TbDAttachments.TB_NAME+" ADD " +Columns.TbDAttachments.F_DOWNLOAD_PROGRESS+ " INTEGER default 0");
				}
				if (db.getVersion() < 60) {
					copyCAttachmentsAndDAttachment(db);
					//邮件夹是否接收推送并提醒，1表示接收   0表示不接收
					db.execSQL("ALTER TABLE folders ADD is_allow_push INTEGER default 0");
					//是否是自定义邮件夹，1表示自定义邮件夹   0表示非自定义邮件夹
					db.execSQL("ALTER TABLE folders ADD is_custom_folder INTEGER default 0");
				}
				if (db.getVersion() < 61) {
					Cursor cursor = null;
					try {
						cursor = db.rawQuery(
								"SELECT id, name,top_group FROM folders", null);
						while (cursor.moveToNext()) {
							try {
								int id = cursor.getInt(0);
								String name = cursor.getString(1);
								// //1:系统邮件夹 0：自定义邮件夹
								int isCustomFolder = 0;
								// //邮件夹是否接收推送并提醒，1表示接收 0表示不接收
								int isAllowPush = 0;
								if (mAccount.isSpecialFolder(name)) {
									isCustomFolder = 1;
									if (name.equalsIgnoreCase(mAccount
											.getInboxFolderName())) {
										isAllowPush = 1;
									}
								} else {
									isCustomFolder = 0;
								}
								db.execSQL(
										"UPDATE folders SET is_allow_push = ? ,is_custom_folder= ? WHERE id = ?",
										new Object[] { isAllowPush,
												isCustomFolder, id });
							} catch (Exception e) {
								Log.e(MailChat.LOG_TAG,
										" error trying to ugpgrade a folder class",
										e);
							}
						}
						long startTime = System.currentTimeMillis();
						SharedPreferences.Editor editor = getPreferences()
								.edit();

						List<? extends Folder> folders = getPersonalNamespaces(true);
						for (Folder folder : folders) {
							if (folder instanceof LocalFolder) {
								LocalFolder lFolder = (LocalFolder) folder;
								lFolder.save(editor);
							}
						}

						editor.commit();
						long endTime = System.currentTimeMillis();
						Log.i(MailChat.LOG_TAG,
								"Putting folder preferences for "
										+ folders.size()
										+ " folders back into Preferences took "
										+ (endTime - startTime) + " ms");
					} catch (SQLiteException e) {
						Log.e(MailChat.LOG_TAG,
								"Exception while upgrading database to v41. folder classes may have vanished",
								e);
					} catch (Exception e) {
						Log.e(MailChat.LOG_TAG,
								"Could not replace Preferences in upgrade from DB_VERSION 61",
								e);
					} finally {
						Utility.closeQuietly(cursor);
					}

				}
				if (db.getVersion() < 62) {
					db.execSQL("ALTER TABLE "+Columns.TbCGroup.TB_NAME+" ADD " +Columns.TbCGroup.F_SEND_STATE+ " INTEGER default 0");
					db.execSQL("ALTER TABLE "+Columns.TbDchat.TB_NAME+" ADD " +Columns.TbDchat.F_SEND_STATE+ " INTEGER default 0");
					db.execSQL("ALTER TABLE "+Columns.TbDchat.TB_NAME+" ADD " +Columns.TbDchat.F_INPUT_TYPE+ " INTEGER default 1");
				}
				if (db.getVersion() < 63) {
					//是否使用过邮洽，1表示已使用    0表示未使用
					db.execSQL("ALTER TABLE tb_user_contacts ADD f_is_used_mailchat INTEGER default 0");
				}
				if (db.getVersion() < 64) {
				    // 修复\u0000作为邮件列表分隔符造成的数据库搜索问题
				    ContentValues cv = new ContentValues();
				    String[] projection = { "id", "sender_list", "to_list", "cc_list", "bcc_list", "reply_to_list" };
				    Cursor cursor = db.query("messages", projection, null,  null, null, null, null);
				    
                    try {
                        while (cursor.moveToNext()) {
                            long id = cursor.getLong(0);
                            
                            String[] mailLists = new String[5];
                            for (int i = 0; i < 5; i++) {
                                String mailList = cursor.getString(i + 1);
                                if (mailList != null && mailList.length() > 0) {
                                    mailList = mailList.replace('\u0000', '|');
                                }
                                mailLists[i] = mailList;
                            }
                            
                            cv.put("sender_list", mailLists[0]);
                            cv.put("to_list", mailLists[1]);
                            cv.put("cc_list", mailLists[2]);
                            cv.put("bcc_list", mailLists[3]);
                            cv.put("reply_to_list", mailLists[4]);

                            db.update("messages", cv, "id = ?",
                                    new String[] { Long.toString(id) });

                            cv.clear();
                        }
                    } finally {
                        cursor.close();
                    }
                }

				if (db.getVersion() < 65) {
					db.execSQL("ALTER TABLE "+Columns.TbDchat.TB_NAME+" ADD " +Columns.TbDchat.F_DCHAT_TYPE+ " INTEGER default 0");
				}
				if(db.getVersion() < 66){
					//单聊消息字段
					db.execSQL("ALTER TABLE "+Columns.TbDchatMessage.TB_NAME+" ADD " +Columns.TbDchatMessage.F_OA_ID+ " TEXT");
					db.execSQL("ALTER TABLE "+Columns.TbDchatMessage.TB_NAME+" ADD " +Columns.TbDchatMessage.F_OA_FROM+ " TEXT");
					db.execSQL("ALTER TABLE "+Columns.TbDchatMessage.TB_NAME+" ADD " +Columns.TbDchatMessage.F_OA_SUBJECT+ " TEXT");
					db.execSQL("ALTER TABLE "+Columns.TbDchatMessage.TB_NAME+" ADD " +Columns.TbDchatMessage.F_OA_TIME+ " long DEFAULT 0");
					db.execSQL("ALTER TABLE "+Columns.TbDchatMessage.TB_NAME+" ADD " +Columns.TbDchatMessage.F_URL+ " TEXT");
					db.execSQL("ALTER TABLE "+Columns.TbDchatMessage.TB_NAME+" ADD " +Columns.TbDchatMessage.F_READ_FLAG+ " INTEGER default 0");
					//群消息字段
					db.execSQL("ALTER TABLE "+Columns.TbCMessages.TB_NAME+" ADD " +Columns.TbCMessages.F_READ_FLAG+ " INTEGER default 0");
				}
                if(db.getVersion() < 67){
                    //联系人表新增 公司,部门，职位,删除标记字段
                    db.execSQL("ALTER TABLE "+Columns.TbUserContacts.TB_NAME+" ADD " +Columns.TbUserContacts.F_COMPANY+ " TEXT");
                    db.execSQL("ALTER TABLE "+Columns.TbUserContacts.TB_NAME+" ADD " +Columns.TbUserContacts.F_DEPARTMENT+ " TEXT");
                    db.execSQL("ALTER TABLE "+Columns.TbUserContacts.TB_NAME+" ADD " +Columns.TbUserContacts.F_POSITION+ " TEXT");
                    db.execSQL("ALTER TABLE "+Columns.TbUserContacts.TB_NAME+" ADD " +Columns.TbUserContacts.F_IS_VISIBILITY+ " INTEGER default 1");

                    db.execSQL("ALTER TABLE "+Columns.TbUserContacts.TB_NAME+" ADD " +Columns.TbUserContacts.F_PHONE+ " TEXT");
                    db.execSQL("ALTER TABLE "+Columns.TbUserContacts.TB_NAME+" ADD " +Columns.TbUserContacts.F_ADDR+ " TEXT");
                    db.execSQL("ALTER TABLE "+Columns.TbUserContacts.TB_NAME+" ADD " +Columns.TbUserContacts.F_REMARKS+ " TEXT");
                    createTbBusinessContact(db);
                }
                if (db.getVersion() < 68) {
                    //附件表新增编码字段
                    db.execSQL("ALTER TABLE attachments ADD content_transfer_encoding TEXT");
                }
				if (db.getVersion() < 69) {
					db.execSQL("ALTER TABLE " + Columns.TbDchat.TB_NAME+ " ADD " + Columns.TbDchat.F_IS_UNTREATED + " INTEGER default 0");
				}
				if (db.getVersion() < 70) {
					//创建联系人本地修改备注表
					createTbContactRemark(db);
					db.execSQL("ALTER TABLE " + Columns.TbCMessages.TB_NAME+ " ADD " + Columns.TbCMessages.F_SERVER_MESSAGE_TYPE + " INTEGER default 0");
					db.execSQL("ALTER TABLE " + Columns.TbDchatMessage.TB_NAME+ " ADD " + Columns.TbDchatMessage.F_SERVER_MESSAGE_TYPE + " INTEGER default 0");
				}
                if (db.getVersion() < 71) {
                    Preferences preferences = Preferences.getPreferences(MailChat.app);
                    List<Account> accounts = preferences.getAccounts();
                    for (Account account : accounts) {
                        account.setLoginSuccessedAccount(true);
                        account.save(preferences);
                    }
                }
				if (db.getVersion() < 72) {
					db.execSQL("ALTER TABLE "
							+ Columns.TbBusinessContactUser.TB_NAME + " ADD "
							+ Columns.TbBusinessContactUser.F_COMPANY + " TEXT");
					db.execSQL("ALTER TABLE "
							+ Columns.TbUserContacts.TB_NAME + " ADD "
							+ Columns.TbUserContacts.F_IS_ADD + " INTEGER default 0");
					//删除该时间段异常引起的单聊条目。
					try {
						db.execSQL("delete from tb_user_contacts where f_email in (select f_to_email from tb_d_chatlist where f_last_time>1451289600000 and f_last_time<1451293200000) and f_email <> 'fb@mailchat.cn'");
						db.execSQL("delete from tb_d_chatlist where f_last_time>1451289600000 and f_last_time<1451293200000 and f_to_email <> 'fb@mailchat.cn'");
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
				if (db.getVersion() < 73) {
					//部门拥有子部门数
					db.execSQL("ALTER TABLE " + Columns.TbBusinessContactDepartment.TB_NAME+ " ADD " + Columns.TbBusinessContactDepartment.F_CHILD_DEP_COUNT + " INTEGER default 0");
				}
				if (db.getVersion() < 74) {
					//由于下载逻辑修,更新下载暂停状态
					db.execSQL("UPDATE " + Columns.TbCAttachments.TB_NAME+ " SET " + Columns.TbCAttachments.F_DOWNLOAD_PAUSE_FLAG + " = 1 WHERE " +Columns.TbCAttachments.F_DOWNLOAD_PROGRESS + " = 0");
					db.execSQL("UPDATE " + Columns.TbDAttachments.TB_NAME+ " SET " + Columns.TbDAttachments.F_DOWNLOAD_PAUSE_FLAG + " = 1 WHERE " +Columns.TbDAttachments.F_DOWNLOAD_PROGRESS + " = 0");
				}
				if(db.getVersion() < 75){
					//单聊附件字段
					db.execSQL("ALTER TABLE "+Columns.TbDAttachments.TB_NAME+" ADD " +Columns.TbDAttachments.F_WIDTH+ " INTEGER default 0");
					db.execSQL("ALTER TABLE "+Columns.TbDAttachments.TB_NAME+" ADD " +Columns.TbDAttachments.F_HEIGHT+ " INTEGER default 0");
					db.execSQL("ALTER TABLE "+Columns.TbDAttachments.TB_NAME+" ADD " +Columns.TbDAttachments.F_IS_IMAGE_LOAD+ " INTEGER default 0");
					//群聊附件字段
					db.execSQL("ALTER TABLE "+Columns.TbCAttachments.TB_NAME+" ADD " +Columns.TbCAttachments.F_WIDTH+ " INTEGER default 0");
					db.execSQL("ALTER TABLE "+Columns.TbCAttachments.TB_NAME+" ADD " +Columns.TbCAttachments.F_HEIGHT+ " INTEGER default 0");
					db.execSQL("ALTER TABLE "+Columns.TbCAttachments.TB_NAME+" ADD " +Columns.TbCAttachments.F_IS_IMAGE_LOAD+ " INTEGER default 0");
				}
				if(db.getVersion() < 76){
					//群聊消息字段
					db.execSQL("ALTER TABLE "+Columns.TbCMessages.TB_NAME+" ADD " +Columns.TbCMessages.F_MAIL_FROM_EMAIL+ " TEXT");
					db.execSQL("ALTER TABLE "+Columns.TbCMessages.TB_NAME+" ADD " +Columns.TbCMessages.F_MAIL_FROM_NICKNAME+ " TEXT");
					db.execSQL("ALTER TABLE "+Columns.TbCMessages.TB_NAME+" ADD " +Columns.TbCMessages.F_MAIL_SUBJECT+ " TEXT");
					db.execSQL("ALTER TABLE "+Columns.TbCMessages.TB_NAME+" ADD " +Columns.TbCMessages.F_MAIL_PREVIEW+ " TEXT");
					//单聊消息字段
					db.execSQL("ALTER TABLE "+Columns.TbDchatMessage.TB_NAME+" ADD " +Columns.TbDchatMessage.F_MAIL_FROM_EMAIL+ " TEXT");
					db.execSQL("ALTER TABLE "+Columns.TbDchatMessage.TB_NAME+" ADD " +Columns.TbDchatMessage.F_MAIL_FROM_NICKNAME+ " TEXT");
					db.execSQL("ALTER TABLE "+Columns.TbDchatMessage.TB_NAME+" ADD " +Columns.TbDchatMessage.F_MAIL_SUBJECT+ " TEXT");
					db.execSQL("ALTER TABLE "+Columns.TbDchatMessage.TB_NAME+" ADD " +Columns.TbDchatMessage.F_MAIL_PREVIEW+ " TEXT");
					//修复bug,由于忘记修改保存消息附件下载状态逻辑，导致保存还是原来的标记值。
					db.execSQL("UPDATE " + Columns.TbCAttachments.TB_NAME+ " SET " + Columns.TbCAttachments.F_DOWNLOAD_PAUSE_FLAG + " = 1 WHERE " +Columns.TbCAttachments.F_DOWNLOAD_PROGRESS + " = 0");
					db.execSQL("UPDATE " + Columns.TbDAttachments.TB_NAME+ " SET " + Columns.TbDAttachments.F_DOWNLOAD_PAUSE_FLAG + " = 1 WHERE " +Columns.TbDAttachments.F_DOWNLOAD_PROGRESS + " = 0");
				}
                if (db.getVersion() < 77) {
                    //邮件列表新增mail_id字段
                    db.execSQL("ALTER TABLE messages ADD mail_id TEXT");
                }

				db.setVersion(DB_VERSION);
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}

			if (db.getVersion() != DB_VERSION) {
				throw new RuntimeException("Database upgrade failed!");
			}
		}
		private void createTbContactRemark(SQLiteDatabase db){
			//联系人本地修改保存及与服务端同步
			StringBuffer tbContactRemark = new StringBuffer();
			tbContactRemark.append("CREATE TABLE IF NOT EXISTS ")
			.append(Columns.TbContactRemark.TB_NAME)
			.append(" (").append(Columns.TbContactRemark.F_EMAIL)
			.append(" TEXT NOT NULL UNIQUE,")
			.append(Columns.TbContactRemark.F_NAME)
			.append(" TEXT,")
			.append(Columns.TbContactRemark.F_NICK_NAME)
			.append(" TEXT,")
			.append(Columns.TbContactRemark.F_IMG_HEAD_HASH)
			.append(" TEXT DEFAULT NULL,")
			.append(Columns.TbContactRemark.F_BIRTHDAY)
			.append(" TEXT,")
			.append(Columns.TbContactRemark.F_PHONE)
			.append(" TEXT,")
			.append(Columns.TbContactRemark.F_DATE)
			.append(" TEXT,")
			.append(Columns.TbContactRemark.F_REMARK)
			.append(" TEXT,")
			.append(Columns.TbContactRemark.F_ADDR)
			.append(" TEXT,")
			.append(Columns.TbContactRemark.F_DEPARTMENT)
			.append(" TEXT,")
			.append(Columns.TbContactRemark.F_COMPANY)
			.append(" TEXT,")
			.append(Columns.TbContactRemark.F_POSITION)
			.append(" TEXT")
//			.append("foreign key(").append(Columns.TbContactRemark.F_EMAIL)
//			.append(") references ").append(Columns.TbBusinessContactDepartment_User.TB_NAME)
//			.append("(").append(Columns.TbBusinessContactDepartment_User.F_USER_EMAIL).append(")")
			.append(");");
			db.execSQL(tbContactRemark.toString());
		}
		private void mailchatDB(SQLiteDatabase db) {

			// 单聊列表
			StringBuffer tb_single_chat_list = new StringBuffer();
			tb_single_chat_list.append("CREATE TABLE IF NOT EXISTS ")
					.append(Columns.TbDchat.TB_NAME).append(" (")
					.append(Columns.TbDchat.F_UID)
					.append(" TEXT  NOT NULL UNIQUE,")
					.append(Columns.TbDchat.F_IS_VISIBILITY)
					.append(" INTEGER DEFAULT 1,")
					.append(Columns.TbDchat.F_IS_DCHAT_ALERT)
					.append("  INTEGER default 0,")
					.append(Columns.TbDchat.F_STICKED_DATE)
					.append(" long DEFAULT 0,")
					.append(Columns.TbDchat.F_IS_STICKED)
					.append(" INTEGER DEFAULT 0,")
					.append(Columns.TbDchat.F_TO_EMAIL).append(" TEXT, ")
					.append(Columns.TbDchat.F_DRAFT).append(" TEXT,")
					.append(Columns.TbDchat.F_IS_DRAFT)
					.append(" INTEGER default 0,")
					.append(Columns.TbDchat.F_LAST_MESSAGE).append(" TEXT,")
					.append(Columns.TbDchat.F_LAST_MESSAGE_TYPE)
					.append(" INTEGER,").append(Columns.TbDchat.F_UNREADCOUNT)
					.append(" INTEGER DEFAULT 0,")
					.append(Columns.TbDchat.F_LAST_TIME)
					.append("  long DEFAULT 0,")
					.append(Columns.TbDchat.F_LAST_MESSAGE_EMAIL)
					.append(" TEXT );");
			SQL_TABLE_TB_DCHAT_CREATE = tb_single_chat_list.toString();

			// 单聊消息
			StringBuffer tb_single_chat_message = new StringBuffer();
			tb_single_chat_message.append("CREATE TABLE IF NOT EXISTS ")
					.append(Columns.TbDchatMessage.TB_NAME).append(" (")
					.append(Columns.TbDchatMessage.F_UID)
					.append(" TEXT NOT NULL UNIQUE,")
					.append(Columns.TbDchatMessage.F_DCHAT_UID)
					.append(" TEXT,").append(Columns.TbDchatMessage.F_TO_EMAIL)
					.append("  TEXT,")
					.append(Columns.TbDchatMessage.F_FROM_EMAIL)
					.append(" TEXT,")
					.append(Columns.TbDchatMessage.F_MESSAGE_CONTENT)
					.append(" TEXT,").append(Columns.TbDchatMessage.F_TIME)
					.append(" long DEFAULT 0,")
					.append(Columns.TbDchatMessage.F_MESSAGE_TYPE)
					.append(" INTEGER,")
					.append(Columns.TbDchatMessage.F_DELETE_FLAG)
					.append(" INTEGER DEFAULT 0,")
					.append(Columns.TbDchatMessage.F_MESSAGE_STATE)
					.append(" INTEGER,")
					.append(Columns.TbDchatMessage.F_LOCATION_TYPE)
					.append("  TEXT,").append(Columns.TbDchatMessage.F_LAT)
					.append("  TEXT,").append(Columns.TbDchatMessage.F_LON)
					.append(" TEXT,").append(Columns.TbDchatMessage.F_ADDRESS)
					.append(" TEXT,")
					.append(Columns.TbDchatMessage.F_LOCATION_NAME)
					.append(" TEXT);");
			SQL_TABLE_TB_DMESSAGE_CREATE = tb_single_chat_message.toString();

			// 单聊附件
			StringBuffer tb_single_chat_attachment = new StringBuffer();
			tb_single_chat_attachment.append("CREATE TABLE IF NOT EXISTS ")
					.append(Columns.TbDAttachments.TB_NAME).append(" (")
					.append(Columns.TbDAttachments.F_UID)
					.append(" TEXT  NOT NULL UNIQUE, ")
					.append(Columns.TbDAttachments.F_MESSAGE_UID)
					.append(" TEXT,").append(Columns.TbDAttachments.F_NAME)
					.append(" TEXT,").append(Columns.TbDAttachments.F_FILE_ID)
					.append(" TEXT,").append(Columns.TbDAttachments.F_SIZE)
					.append(" long,")
					.append(Columns.TbDAttachments.F_MIME_TYPE)
					.append(" TEXT,")
					.append(Columns.TbDAttachments.F_FILE_PATH)
					.append(" TEXT,")
					.append(Columns.TbDAttachments.F_VOICE_LENGTH)
					.append("  INTEGER,")
					.append(Columns.TbDAttachments.F_READ_FLAG)
					.append(" INTEGER,")
					.append(Columns.TbDAttachments.F_LOCALPATH_FLAG)
					.append(" INTEGER  default 0,")
					.append(Columns.TbDAttachments.F_FORWARD_FLAG)
					.append(" INTEGER default 0);");
			SQL_TABLE_TB_DATTCHMENT_CREATE = tb_single_chat_attachment
					.toString();

			// 联系人列表
			StringBuffer tb_user_contacts = new StringBuffer();
			tb_user_contacts.append("CREATE TABLE IF NOT EXISTS ")
					.append(Columns.TbUserContacts.TB_NAME).append(" (")
					.append(Columns.TbUserContacts.F_ID)
					.append(" INTEGER PRIMARY KEY, ")
					.append(Columns.TbUserContacts.F_EMAIL)
					.append("  varchar(30) NOT NULL UNIQUE,")
					.append(Columns.TbUserContacts.F_NICK_NAME)
					.append(" varchar(30) ,")
					.append(Columns.TbUserContacts.F_SPELL_NAME)
					.append(" varchar(30) ,")
					.append(Columns.TbUserContacts.F_SEND_COUNT)
					.append(" INTEGER  default 0,")
					.append(Columns.TbUserContacts.F_RECEIVE_COUNT)
					.append(" INTEGER  default 0,")
					.append(Columns.TbUserContacts.F_IMG_HEAD_NAME)
					.append(" TEXT,")
					.append(Columns.TbUserContacts.F_IMG_HEAD_HASH)
					.append(" TEXT DEFAULT NULL,")
					.append(Columns.TbUserContacts.F_UPLOAD_STATE)
					.append(" TEXT DEFAULT NULL,")
					.append(Columns.TbUserContacts.F_DATE).append(" INTEGER);");
			SQL_TABLE_TB_USER_CONTACTS_CREATE = tb_user_contacts.toString();

			// 群组表
			StringBuffer tb_cgroup = new StringBuffer();
			tb_cgroup
					.append("CREATE TABLE IF NOT EXISTS ")
					.append(Columns.TbCGroup.TB_NAME)
					.append(" (")
					.append(Columns.TbCGroup.F_ID)
					.append(" INTEGER PRIMARY KEY, ")
					.append(Columns.TbCGroup.F_UID)
					.append(" TEXT  NOT NULL UNIQUE, ")
					.append(Columns.TbCGroup.F_GROUP_NAME)
					.append(" TEXT not null,")
					.append(Columns.TbCGroup.F_RENAME)
					.append(" INTEGER default 0,")
					// 0未修改过
					.append(Columns.TbCGroup.F_GROUP_DESC).append(" TEXT,")
					.append(Columns.TbCGroup.F_AVATAR).append(" TEXT,")
					.append(Columns.TbCGroup.F_IS_PRIV).append(" INTEGER,")
					.append(Columns.TbCGroup.F_IS_MEMBER).append(" INTEGER,")
					.append(Columns.TbCGroup.F_IS_ADMIN).append(" INTEGER,")
					.append(Columns.TbCGroup.F_C_USER)
					.append("  INTEGER default 0,")
					.append(Columns.TbCGroup.F_C_POSTS)
					.append("  INTEGER default 0,")
					.append(Columns.TbCGroup.F_GROUP_TYPE).append(" INTEGER,")
					.append(Columns.TbCGroup.F_STICKED_DATE)
					.append(" long DEFAULT 0,")
					.append(Columns.TbCGroup.F_IS_STICKED)
					.append(" long DEFAULT 0,")
					.append(Columns.TbCGroup.F_IS_MESSAGE_ALERT)
					.append("  INTEGER default 0,")
					.append(Columns.TbCGroup.F_IS_MESSAGE_VOICEREMINDER)
					.append("  INTEGER default 0,")
					.append(Columns.TbCGroup.F_IS_VISIBILITY)
					.append(" INTEGER DEFAULT 1,")
					.append(Columns.TbCGroup.F_DRAFT).append(" TEXT,")
					.append(Columns.TbCGroup.F_IS_DRAFT)
					.append(" INTEGER default 0,")
					.append(Columns.TbCGroup.F_LAST_SEND_UID).append(" TEXT,")
					.append(Columns.TbCGroup.F_UNTREATED_COUNT)
					.append(" INTEGER DEFAULT 0,")
					.append(Columns.TbCGroup.F_LAST_SENDNICKNAME)
					.append(" TEXT,")
					.append(Columns.TbCGroup.F_LAST_SEND_CONTENT)
					.append(" TEXT,").append(Columns.TbCGroup.F_LAST_SEND_TYPE)
					.append(" TEXT,").append(Columns.TbCGroup.F_LAST_SENDDATE)
					.append("  TEXT,").append(Columns.TbCGroup.F_IS_UNTREATED)
					.append("  INTEGER,").append(Columns.TbCGroup.F_INPUT_TYPE)
					.append(" INTEGER default 1,").append("foreign key(")
					.append(Columns.TbCGroup.F_UID).append(") references ")
					.append(Columns.TbCGroup_CMember.TB_NAME).append("(")
					.append(Columns.TbCGroup_CMember.F_CGROUP_UID).append(")")
					.append(");");
			SQL_TABLE_TB_CGROUP_CREATE = tb_cgroup.toString();

			// 群成员表
			StringBuffer tb_cmember = new StringBuffer();
			tb_cmember.append("CREATE TABLE IF NOT EXISTS ");
			tb_cmember.append(Columns.TbCMember.TB_NAME).append(" (");
			tb_cmember.append(Columns.TbCMember.F_UID).append(" TEXT, ");
			tb_cmember.append(Columns.TbCMember.F_NICK_NAME).append(" TEXT,");
			tb_cmember.append(Columns.TbCMember.F_EMAIL).append(" TEXT,");
			tb_cmember.append(Columns.TbCMember.F_AVATAR).append(" TEXT,");
			tb_cmember.append(Columns.TbCMember.F_BIG_AVATAR).append(" TEXT,");
			tb_cmember.append(Columns.TbCMember.F_AVATAR_HASH).append(" TEXT,");
			tb_cmember.append("PRIMARY KEY (").append(Columns.TbCMember.F_UID)
					.append("),");
			tb_cmember.append("foreign key(").append(Columns.TbCMember.F_UID)
					.append(") references ");
			tb_cmember.append(Columns.TbCGroup_CMember.TB_NAME).append("(");
			tb_cmember.append(Columns.TbCGroup_CMember.F_CMEMBER_UID)
					.append(")").append(");");
			SQL_TABLE_TB_CMEMBER_CREATE = tb_cmember.toString();

			// 关系表
			StringBuffer tb_group_cmember = new StringBuffer();
			tb_group_cmember.append("CREATE TABLE IF NOT EXISTS ")
					.append(Columns.TbCGroup_CMember.TB_NAME).append(" (")
					.append(Columns.TbCGroup_CMember.F_CGROUP_UID)
					.append(" TEXT, ")
					.append(Columns.TbCGroup_CMember.F_CMEMBER_UID)
					.append(" TEXT,")
					.append(Columns.TbCGroup_CMember.F_IS_ADMIN)
					.append(" INTEGER DEFAULT 0,")
					.append(Columns.TbCGroup_CMember.F_IS_INVITE)
					.append(" INTEGER DEFAULT 0,").append("PRIMARY KEY (")
					.append(Columns.TbCGroup_CMember.F_CGROUP_UID).append(",")
					.append(Columns.TbCGroup_CMember.F_CMEMBER_UID).append(")")
					.append(");");
			SQL_TABLE_TB_CGROUP_CMEMBER_CREATE = tb_group_cmember.toString();

			// 群消息表
			StringBuffer tb_cmessages = new StringBuffer();
			tb_cmessages.append("CREATE TABLE IF NOT EXISTS ")
					.append(Columns.TbCMessages.TB_NAME).append(" (")
					.append(Columns.TbCMessages.F_ID)
					.append(" INTEGER PRIMARY KEY, ")
					.append(Columns.TbCMessages.F_UID)
					.append(" TEXT  NOT NULL UNIQUE, ")
					.append(Columns.TbCMessages.F_CGROUP_UID).append(" TEXT,")
					.append(Columns.TbCMessages.F_CMEMBER_UID).append(" TEXT,")
					.append(Columns.TbCMessages.F_SENDTIME)
					.append("  long DEFAULT 0,")
					.append(Columns.TbCMessages.F_MESSAGETYPE).append(" TEXT,")
					.append(Columns.TbCMessages.F_LAT).append(" TEXT,")
					.append(Columns.TbCMessages.F_LON).append(" TEXT,")
					.append(Columns.TbCMessages.F_ADDRESS).append(" TEXT,")
					.append(Columns.TbCMessages.F_LOCATION_NAME)
					.append(" TEXT,")
					.append(Columns.TbCMessages.F_MESSAGE_STATE)
					.append(" INTEGER default 0,")
					.append(Columns.TbCMessages.F_CONTENT).append(" TEXT,")
					.append(Columns.TbCMessages.F_DELETE_FLAG)
					.append(" INTEGER default 0);");
			SQL_TABLE_TB_CMESSAGES_CREATE = tb_cmessages.toString();

			// 群附件表
			StringBuffer tb_cattachments = new StringBuffer();
			tb_cattachments.append("CREATE TABLE IF NOT EXISTS ")
					.append(Columns.TbCAttachments.TB_NAME).append(" (")
					.append(Columns.TbCAttachments.F_ID)
					.append(" INTEGER PRIMARY KEY, ")
					.append(Columns.TbCAttachments.F_UID)
					.append(" TEXT  NOT NULL UNIQUE, ")
					.append(Columns.TbCAttachments.F_CGROUP_UID)
					.append(" TEXT,")
					.append(Columns.TbCAttachments.F_CMESSAGES_UID)
					.append(" TEXT,").append(Columns.TbCAttachments.F_NAME)
					.append(" TEXT,").append(Columns.TbCAttachments.F_FILE_ID)
					.append(" TEXT,").append(Columns.TbCAttachments.F_SIZE)
					.append(" long,")
					.append(Columns.TbCAttachments.F_DOWNLOAD_STATE)
					.append(" INTEGER,")
					.append(Columns.TbCAttachments.F_MIME_TYPE)
					.append(" TEXT,")
					.append(Columns.TbCAttachments.F_FILE_PATH)
					.append(" TEXT,")
					.append(Columns.TbCAttachments.F_READ_FLAG)
					.append(" INTEGER  default 0,")
					.append(Columns.TbCAttachments.F_VOICE_LENGTH)
					.append(" INTEGER ,")
					.append(Columns.TbCAttachments.F_LOCALPATH_FLAG)
					.append("  INTEGER  default 0,")
					.append(Columns.TbCAttachments.F_FORWARD_FLAG)
					.append(" INTEGER  default 0);");
			SQL_TABLE_TB_CATTACHMENTS_CREATE = tb_cattachments.toString();

			// 单聊相关表格
			db.execSQL(SQL_TABLE_TB_DCHAT_CREATE);
			db.execSQL(SQL_TABLE_TB_DMESSAGE_CREATE);
			db.execSQL(SQL_TABLE_TB_DATTCHMENT_CREATE);
			// 联系人列表
			db.execSQL(SQL_TABLE_TB_USER_CONTACTS_CREATE);
			// 群组相关表
			db.execSQL(SQL_TABLE_TB_CGROUP_CREATE);
			db.execSQL(SQL_TABLE_TB_CMEMBER_CREATE);
			db.execSQL(SQL_TABLE_TB_CMESSAGES_CREATE);
			db.execSQL(SQL_TABLE_TB_CATTACHMENTS_CREATE);
			db.execSQL(SQL_TABLE_TB_CGROUP_CMEMBER_CREATE);

		}

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

			StringBuffer tb_httpsPending = new StringBuffer();
			tb_httpsPending.append("CREATE TABLE IF NOT EXISTS ")
					.append(Columns.HTTPSPendingAction.TB_NAME).append("(")
					.append(Columns.HTTPSPendingAction.ID)
					.append(" INTEGER PRIMARY KEY, ")
					.append(Columns.HTTPSPendingAction.COMMAND)
					.append(" TEXT,")
					.append(Columns.HTTPSPendingAction.PARAMETERS)
					.append(" TEXT)");
			SQL_TABLE_TB_HTTPS_PENDING_CREATE = tb_httpsPending.toString();

			db.execSQL(SQL_TABLE_TB_MQTT_PENDING_CREATE);
			db.execSQL(SQL_TABLE_TB_HTTPS_PENDING_CREATE);
		}

		/**
		 * 复制单聊及群聊数据，重新建表，让附件ID可以对应多个消息（即去除附件ID唯一约束）
		 *
		 * @Description:
		 * @param: SQLiteDatabase
		 * @see:
		 * @since:
		 * @author: shengli
		 * @return
		 * @throws UnavailableStorageException
		 * @date:2015-6-10
		 */
		private void copyCAttachmentsAndDAttachment(SQLiteDatabase db){
			//创建群附临时存储件表
			StringBuffer tb_cattachments_temp = new StringBuffer();
			tb_cattachments_temp.append("CREATE TABLE IF NOT EXISTS ")
					.append("tb_c_attachments_temp").append(" (")
					.append(Columns.TbCAttachments.F_ID)
					.append(" INTEGER PRIMARY KEY, ")
					.append(Columns.TbCAttachments.F_UID)
					.append(" TEXT  NOT NULL, ")
					.append(Columns.TbCAttachments.F_CGROUP_UID)
					.append(" TEXT,")
					.append(Columns.TbCAttachments.F_CMESSAGES_UID)
					.append(" TEXT,")
					.append(Columns.TbCAttachments.F_NAME)
					.append(" TEXT,")
					.append(Columns.TbCAttachments.F_FILE_ID)
					.append(" TEXT,")
					.append(Columns.TbCAttachments.F_SIZE)
					.append(" long,")
					.append(Columns.TbCAttachments.F_DOWNLOAD_STATE)
					.append(" INTEGER,")
					.append(Columns.TbCAttachments.F_MIME_TYPE)
					.append(" TEXT,")
					.append(Columns.TbCAttachments.F_FILE_PATH)
					.append(" TEXT,")
					.append(Columns.TbCAttachments.F_READ_FLAG)
					.append(" INTEGER default 0,")
					.append(Columns.TbCAttachments.F_VOICE_LENGTH)
					.append(" INTEGER,")
					.append(Columns.TbCAttachments.F_LOCALPATH_FLAG)
					.append(" INTEGER default 0,")
					.append(Columns.TbCAttachments.F_FORWARD_FLAG)
					.append(" INTEGER default 0,")
					.append(Columns.TbCAttachments.F_DOWNLOAD_PAUSE_FLAG)
					.append(" INTEGER default 0,")
					.append(Columns.TbCAttachments.F_DOWNLOAD_PROGRESS)
					.append(" INTEGER default 0);");
			db.execSQL(tb_cattachments_temp.toString());
			//单聊附件临时存储表
			StringBuffer tb_single_chat_attachment_temp = new StringBuffer();
			tb_single_chat_attachment_temp.append("CREATE TABLE IF NOT EXISTS ")
					.append("tb_d_chat_attachment_temp").append(" (")
					.append(Columns.TbDAttachments.F_UID)
					.append(" TEXT  NOT NULL,")
					.append(Columns.TbDAttachments.F_MESSAGE_UID)
					.append(" TEXT,")
					.append(Columns.TbDAttachments.F_NAME)
					.append(" TEXT,")
					.append(Columns.TbDAttachments.F_FILE_ID)
					.append(" TEXT,")
					.append(Columns.TbDAttachments.F_SIZE)
					.append(" long,")
					.append(Columns.TbDAttachments.F_MIME_TYPE)
					.append(" TEXT,")
					.append(Columns.TbDAttachments.F_FILE_PATH)
					.append(" TEXT,")
					.append(Columns.TbDAttachments.F_VOICE_LENGTH)
					.append(" INTEGER,")
					.append(Columns.TbDAttachments.F_READ_FLAG)
					.append(" INTEGER,")
					.append(Columns.TbDAttachments.F_LOCALPATH_FLAG)
					.append(" INTEGER  default 0,")
					.append(Columns.TbDAttachments.F_FORWARD_FLAG)
					.append(" INTEGER default 0,")
					.append(Columns.TbDAttachments.F_DOWNLOAD_PAUSE_FLAG)
					.append(" INTEGER default 0,")
					.append(Columns.TbDAttachments.F_DOWNLOAD_PROGRESS)
					.append(" INTEGER default 0);");
			db.execSQL(tb_single_chat_attachment_temp.toString());

			//复制单群聊数据到临时表
			db.execSQL("insert into tb_c_attachments_temp select * from tb_c_attachments");
			db.execSQL("insert into tb_d_chat_attachment_temp select * from tb_d_chat_attachment");

			//删除原单群聊附件表，并创建新表
			db.execSQL(SQL_TABLE_TB_CATTACHMENTS_CLEAR);
			db.execSQL(SQL_TABLE_TB_DATTCHMENT_CLEAR);
			// 群附件表
			StringBuffer tb_cattachments = new StringBuffer();
			tb_cattachments.append("CREATE TABLE IF NOT EXISTS ")
					.append(Columns.TbCAttachments.TB_NAME).append(" (")
					.append(Columns.TbCAttachments.F_ID)
					.append(" INTEGER PRIMARY KEY, ")
					.append(Columns.TbCAttachments.F_UID)
					.append(" TEXT  NOT NULL, ")
					.append(Columns.TbCAttachments.F_CGROUP_UID)
					.append(" TEXT,")
					.append(Columns.TbCAttachments.F_CMESSAGES_UID)
					.append(" TEXT,")
					.append(Columns.TbCAttachments.F_NAME)
					.append(" TEXT,")
					.append(Columns.TbCAttachments.F_FILE_ID)
					.append(" TEXT,")
					.append(Columns.TbCAttachments.F_SIZE)
					.append(" long,")
					.append(Columns.TbCAttachments.F_DOWNLOAD_STATE)
					.append(" INTEGER,")
					.append(Columns.TbCAttachments.F_MIME_TYPE)
					.append(" TEXT,")
					.append(Columns.TbCAttachments.F_FILE_PATH)
					.append(" TEXT,")
					.append(Columns.TbCAttachments.F_READ_FLAG)
					.append(" INTEGER default 0,")
					.append(Columns.TbCAttachments.F_VOICE_LENGTH)
					.append(" INTEGER,")
					.append(Columns.TbCAttachments.F_LOCALPATH_FLAG)
					.append(" INTEGER default 0,")
					.append(Columns.TbCAttachments.F_FORWARD_FLAG)
					.append(" INTEGER default 0,")
					.append(Columns.TbCAttachments.F_DOWNLOAD_PAUSE_FLAG)
					.append(" INTEGER default 0,")
					.append(Columns.TbCAttachments.F_DOWNLOAD_PROGRESS)
					.append(" INTEGER default 0);");
			SQL_TABLE_TB_CATTACHMENTS_CREATE = tb_cattachments.toString();
			// 单聊附件
			StringBuffer tb_single_chat_attachment = new StringBuffer();
			tb_single_chat_attachment.append("CREATE TABLE IF NOT EXISTS ")
					.append(Columns.TbDAttachments.TB_NAME).append(" (")
					.append(Columns.TbDAttachments.F_UID)
					.append(" TEXT  NOT NULL,")
					.append(Columns.TbDAttachments.F_MESSAGE_UID)
					.append(" TEXT,")
					.append(Columns.TbDAttachments.F_NAME)
					.append(" TEXT,")
					.append(Columns.TbDAttachments.F_FILE_ID)
					.append(" TEXT,")
					.append(Columns.TbDAttachments.F_SIZE)
					.append(" long,")
					.append(Columns.TbDAttachments.F_MIME_TYPE)
					.append(" TEXT,")
					.append(Columns.TbDAttachments.F_FILE_PATH)
					.append(" TEXT,")
					.append(Columns.TbDAttachments.F_VOICE_LENGTH)
					.append(" INTEGER,")
					.append(Columns.TbDAttachments.F_READ_FLAG)
					.append(" INTEGER,")
					.append(Columns.TbDAttachments.F_LOCALPATH_FLAG)
					.append(" INTEGER  default 0,")
					.append(Columns.TbDAttachments.F_FORWARD_FLAG)
					.append(" INTEGER default 0,")
					.append(Columns.TbDAttachments.F_DOWNLOAD_PAUSE_FLAG)
					.append(" INTEGER default 0,")
					.append(Columns.TbDAttachments.F_DOWNLOAD_PROGRESS)
					.append(" INTEGER default 0);");
			SQL_TABLE_TB_DATTCHMENT_CREATE = tb_single_chat_attachment.toString();
			db.execSQL(SQL_TABLE_TB_CATTACHMENTS_CREATE);
			db.execSQL(SQL_TABLE_TB_DATTCHMENT_CREATE);

			//复制临时表数据到新表
			db.execSQL("insert into tb_c_attachments select * from tb_c_attachments_temp");
			db.execSQL("insert into tb_d_chat_attachment select * from tb_d_chat_attachment_temp");
			//删除临时表
			db.execSQL("DROP TABLE IF EXISTS tb_c_attachments_temp");
			db.execSQL("DROP TABLE IF EXISTS tb_d_chat_attachment_temp");
		}

		/**
		 * 创建企业联系人相关表格
		 *
		 * @Description:
		 * @param: SQLiteDatabase
		 * @see:
		 * @since:
		 * @author: shengli
		 * @return
		 * @date:2015-8-18
		 */
		private void createTbBusinessContact(SQLiteDatabase db){
			//创建企业联系人部门表
			StringBuffer tb_b_contact_department = new StringBuffer();
			tb_b_contact_department.append("CREATE TABLE IF NOT EXISTS ")
			.append(Columns.TbBusinessContactDepartment.TB_NAME)
			.append(" (").append(Columns.TbBusinessContactDepartment.F_ID)
			.append(" TEXT NOT NULL UNIQUE,")
			.append(Columns.TbBusinessContactDepartment.F_NAME)
			.append(" TEXT,")
			.append(Columns.TbBusinessContactDepartment.F_PARENT_ID)
			.append(" TEXT,")
			.append(Columns.TbBusinessContactDepartment.F_SORT_ID)
			.append(" TEXT,")
			.append(Columns.TbBusinessContactDepartment.F_IS_OPEN)
			.append(" INTEGER default 0,")
			.append(Columns.TbBusinessContactDepartment.F_USER_TOTAL_COUNT)
			.append(" INTEGER default 0,")
			.append("foreign key(").append(Columns.TbBusinessContactDepartment.F_ID)
			.append(") references ").append(Columns.TbBusinessContactDepartment_User.TB_NAME)
			.append("(").append(Columns.TbBusinessContactDepartment_User.F_DEP_ID).append(")")
			.append(" );");
			SQL_TABLE_TB_B_CONTACT_DEPARTMENT_CREATE =tb_b_contact_department.toString();
			db.execSQL(SQL_TABLE_TB_B_CONTACT_DEPARTMENT_CREATE);

			//创建企业联系人成员表
			StringBuffer tb_b_contact_user = new StringBuffer();
			tb_b_contact_user.append("CREATE TABLE IF NOT EXISTS ")
			.append(Columns.TbBusinessContactUser.TB_NAME)
			.append(" (").append(Columns.TbBusinessContactUser.F_EMAIL)
			.append(" TEXT NOT NULL UNIQUE,")
			.append(Columns.TbBusinessContactUser.F_NAME)
			.append(" TEXT,")
			.append(Columns.TbBusinessContactUser.F_NICK_NAME)
			.append(" TEXT,")
			.append(Columns.TbBusinessContactUser.F_SPELL_NAME)
			.append(" TEXT,")
			.append(Columns.TbBusinessContactUser.F_IMG_HEAD_HASH)
			.append(" TEXT DEFAULT NULL,")
			.append(Columns.TbBusinessContactUser.F_BIRTHDAY)
			.append(" TEXT,")
			.append(Columns.TbBusinessContactUser.F_PHONE)
			.append(" TEXT,")
			.append(Columns.TbBusinessContactUser.F_SEND_COUNT)
			.append(" INTEGER default 0,")
			.append(Columns.TbBusinessContactUser.F_RECEIVE_COUNT)
			.append(" INTEGER default 0,")
			.append(Columns.TbBusinessContactUser.F_UPLOAD_STATE)
			.append(" TEXT DEFAULT NULL,")
			.append(Columns.TbBusinessContactUser.F_DATE)
			.append(" INTEGER,")
			.append(Columns.TbBusinessContactUser.F_IS_USED_MAILCHAT)
			.append(" INTEGER default 0,")
			.append(Columns.TbBusinessContactUser.F_REMARK)
			.append(" TEXT,")
			.append(Columns.TbBusinessContactUser.F_ADDR)
			.append(" TEXT,")
			.append("foreign key(").append(Columns.TbBusinessContactUser.F_EMAIL)
			.append(") references ").append(Columns.TbBusinessContactDepartment_User.TB_NAME)
			.append("(").append(Columns.TbBusinessContactDepartment_User.F_USER_EMAIL).append(")")
			.append(");");
			SQL_TABLE_TB_B_CONTACT_USER_CREATE =tb_b_contact_user.toString();
			db.execSQL(SQL_TABLE_TB_B_CONTACT_USER_CREATE);

			//创建企业联系人关系表
			StringBuffer tb_b_contact_department_user = new StringBuffer();
			tb_b_contact_department_user.append("CREATE TABLE IF NOT EXISTS ")
			.append(Columns.TbBusinessContactDepartment_User.TB_NAME)
			.append(" (").append(Columns.TbBusinessContactDepartment_User.F_DEP_ID)
			.append(" TEXT NOT NULL,")
			.append(Columns.TbBusinessContactDepartment_User.F_USER_EMAIL)
			.append(" TEXT NOT NULL,")
			.append(Columns.TbBusinessContactDepartment_User.F_IS_LEADER)
			.append(" INTEGER default 0,")
			.append(Columns.TbBusinessContactDepartment_User.F_POSITION)
			.append(" TEXT);");
			SQL_TABLE_TB_B_CONTACT_DEPARTMENT_USER_CREATE =tb_b_contact_department_user.toString();
			db.execSQL(SQL_TABLE_TB_B_CONTACT_DEPARTMENT_USER_CREATE);
		}

		private void update41Metadata(final SQLiteDatabase db,
				SharedPreferences prefs, int id, String name) {

			Folder.FolderClass displayClass = Folder.FolderClass.NO_CLASS;
			Folder.FolderClass syncClass = Folder.FolderClass.INHERITED;
			Folder.FolderClass pushClass = Folder.FolderClass.SECOND_CLASS;
			boolean inTopGroup = false;
			boolean integrate = false;
			if (mAccount.getInboxFolderName().equals(name)) {
				displayClass = Folder.FolderClass.FIRST_CLASS;
				syncClass = Folder.FolderClass.FIRST_CLASS;
				pushClass = Folder.FolderClass.FIRST_CLASS;
				inTopGroup = true;
				integrate = true;
			}

			try {
				displayClass = Folder.FolderClass.valueOf(prefs.getString(uUid
						+ "." + name + ".displayMode", displayClass.name()));
				syncClass = Folder.FolderClass.valueOf(prefs.getString(uUid
						+ "." + name + ".syncMode", syncClass.name()));
				pushClass = Folder.FolderClass.valueOf(prefs.getString(uUid
						+ "." + name + ".pushMode", pushClass.name()));
				inTopGroup = prefs.getBoolean(
						uUid + "." + name + ".inTopGroup", inTopGroup);
				integrate = prefs.getBoolean(uUid + "." + name + ".integrate",
						integrate);
			} catch (Exception e) {
				Log.e(MailChat.LOG_TAG,
						" Throwing away an error while trying to upgrade folder metadata",
						e);
			}

			if (displayClass == Folder.FolderClass.NONE) {
				displayClass = Folder.FolderClass.NO_CLASS;
			}
			if (syncClass == Folder.FolderClass.NONE) {
				syncClass = Folder.FolderClass.INHERITED;
			}
			if (pushClass == Folder.FolderClass.NONE) {
				pushClass = Folder.FolderClass.INHERITED;
			}

			db.execSQL(
					"UPDATE folders SET integrate = ?, top_group = ?, poll_class=?, push_class =?, display_class = ? WHERE id = ?",
					new Object[] { integrate, inTopGroup, syncClass, pushClass,
							displayClass, id });

		}
		
	}

	public long getSize() throws UnavailableStorageException {

		final StorageManager storageManager = StorageManager
				.getInstance(mApplication);

		final File attachmentDirectory = storageManager.getAttachmentDirectory(
				uUid, database.getStorageProviderId());

		return database.execute(false, new DbCallback<Long>() {
			@Override
			public Long doDbWork(final SQLiteDatabase db) {
				final File[] files = attachmentDirectory.listFiles();
				long attachmentLength = 0;
				if (files != null) {
					for (File file : files) {
						if (file.exists()) {
							attachmentLength += file.length();
						}
					}
				}

				final File dbFile = storageManager.getDatabase(uUid,
						database.getStorageProviderId());
				return dbFile.length() + attachmentLength;
			}
		});
	}

	public void compact() throws MessagingException {
		if (MailChat.DEBUG)
			Log.i(MailChat.LOG_TAG, "Before compaction size = " + getSize());

		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				db.execSQL("VACUUM");
				return null;
			}
		});
		if (MailChat.DEBUG)
			Log.i(MailChat.LOG_TAG, "After compaction size = " + getSize());
	}

	public void clear() throws MessagingException {
		if (MailChat.DEBUG)
			Log.i(MailChat.LOG_TAG, "Before prune size = " + getSize());

		pruneCachedAttachments(true);
		if (MailChat.DEBUG) {
			Log.i(MailChat.LOG_TAG, "After prune / before compaction size = "
					+ getSize());

			Log.i(MailChat.LOG_TAG, "Before clear folder count = "
					+ getFolderCount());
			Log.i(MailChat.LOG_TAG, "Before clear message count = "
					+ getMessageCount());

			Log.i(MailChat.LOG_TAG, "After prune / before clear size = "
					+ getSize());
		}
		// don't delete messages that are Local, since there is no copy on the
		// server.
		// Don't delete deleted messages. They are essentially placeholders for
		// UIDs of messages that have
		// been deleted locally. They take up insignificant space
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db) {
				// Delete entries from 'threads' table
				db.execSQL("DELETE FROM threads WHERE message_id IN "
						+ "(SELECT id FROM messages WHERE deleted = 0 AND uid NOT LIKE 'Local%')");

				// Set 'root' and 'parent' of remaining entries in 'thread'
				// table to 'NULL' to make
				// sure the thread structure is in a valid state (this may
				// destroy existing valid
				// thread trees, but is much faster than adjusting the tree by
				// removing messages
				// one by one).
				db.execSQL("UPDATE threads SET root=id, parent=NULL");

				// Delete entries from 'messages' table
				db.execSQL("DELETE FROM messages WHERE deleted = 0 AND uid NOT LIKE 'Local%'");
				return null;
			}
		});

		compact();

		if (MailChat.DEBUG) {
			Log.i(MailChat.LOG_TAG, "After clear message count = "
					+ getMessageCount());

			Log.i(MailChat.LOG_TAG, "After clear size = " + getSize());
		}
	}

	public int getMessageCount() throws MessagingException {
		return database.execute(false, new DbCallback<Integer>() {
			@Override
			public Integer doDbWork(final SQLiteDatabase db) {
				Cursor cursor = null;
				try {
					cursor = db.rawQuery("SELECT COUNT(*) FROM messages", null);
					cursor.moveToFirst();
					return cursor.getInt(0); // message count
				} finally {
					Utility.closeQuietly(cursor);
				}
			}
		});
	}

	public int getFolderCount() throws MessagingException {
		return database.execute(false, new DbCallback<Integer>() {
			@Override
			public Integer doDbWork(final SQLiteDatabase db) {
				Cursor cursor = null;
				try {
					cursor = db.rawQuery("SELECT COUNT(*) FROM folders", null);
					cursor.moveToFirst();
					return cursor.getInt(0); // folder count
				} finally {
					Utility.closeQuietly(cursor);
				}
			}
		});
	}

	@Override
	public LocalFolder getFolder(String name) {
		return new LocalFolder(name);
	}

	public LocalFolder getFolderById(long folderId) {
		return new LocalFolder(folderId);
	}

	// TODO this takes about 260-300ms, seems slow.
	@Override
	public List<? extends Folder> getPersonalNamespaces(boolean forceListAll)
			throws MessagingException {
		final List<LocalFolder> folders = new LinkedList<LocalFolder>();
		try {
			database.execute(false, new DbCallback<List<? extends Folder>>() {
				@Override
				public List<? extends Folder> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;

					try {
						cursor = db.rawQuery("SELECT " + GET_FOLDER_COLS
								+ " FROM folders " + "ORDER BY name ASC", null);
						while (cursor.moveToNext()) {
							if (cursor.isNull(FOLDER_ID_INDEX)) {
								continue;
							}
							String folderName = cursor
									.getString(FOLDER_NAME_INDEX);
							LocalFolder folder = new LocalFolder(folderName);
							folder.open(cursor);

							folders.add(folder);
						}
						return FolderInfoHolder.orderFolder(folders);
					} catch (MessagingException e) {
						throw new WrappedException(e);
					} finally {
						Utility.closeQuietly(cursor);
					}
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}
		return FolderInfoHolder.orderFolder(folders);
	}

	@Override
	public void checkSettings() throws MessagingException {
	}

	public void delete() throws UnavailableStorageException {
		database.delete();
	}

	public void recreate() throws UnavailableStorageException {
		database.recreate();
	}

	public long getCacheSize() {
        long size = 0;
        File[] files = null;
        MailChat mMailChat = MailChat.getInstance();
        // 计算聊天相关所占存储空间
		File chatImageChaceDirectory = new File(
				mMailChat.getChatImageCacheDirectory(mAccount));
		files = chatImageChaceDirectory.listFiles();
		size = getFilesSize(files, size);

		File chatThumbnailImageChaceDirectory = new File(
				mMailChat.getChatThumbnailImageCacheDirectory(mAccount));
		files = chatThumbnailImageChaceDirectory.listFiles();
		size = getFilesSize(files, size);

		File chatLocalThumbnailImageChaceDirectory = new File(
				mMailChat.getChatLocalThumbnailImageCacheDirectory(mAccount));
		files = chatLocalThumbnailImageChaceDirectory.listFiles();
		size = getFilesSize(files, size);

		File chatAttachmentDirectory = new File(
				mMailChat.getChatAttachmentDirectory(mAccount));
		files = chatAttachmentDirectory.listFiles();
		size = getFilesSize(files, size);
		
		File ImageLoaderDirectory = new File(
				MailChat.getInstance().getImageLoaderPath());
		files = ImageLoaderDirectory.listFiles();
		// 过滤掉ImageLoader Journal
		if (files != null && files.length > 1) {
		    size = getFilesSize(files, size);
		}

        // 计算附件临时存储所占空间
        File temporaryAttachmentDirectory = new File(
				mMailChat.getMailAttachmentDirectory(mAccount));
        files = temporaryAttachmentDirectory.listFiles();
        size  = getFilesSize(files,size);

        // 计算附件存储所占空间
        StorageManager storageManager = StorageManager.getInstance(mApplication);
        File attachmentDirectory = storageManager.getAttachmentDirectory(
                uUid, database.getStorageProviderId());
        files = attachmentDirectory.listFiles();
        size  = getFilesSize(files,size);

        return size;
	}

	private long getFilesSize(File[] files,long stratSize){
		long size = stratSize;
		if (files != null) {
            for (File file : files) {
                if (file.exists()) {
					size += file.length();
                }
            }
        }
		return size;
	}

	public void clearCache() {
	    try {
			File[] files = null;
			MailChat mMailChat = MailChat.getInstance();
			// 删除聊天相关所占存储空间
			File chatImageChaceDirectory = new File(
					mMailChat.getChatImageCacheDirectory(mAccount));
			files = chatImageChaceDirectory.listFiles();
			deleteFiles(files);

			File chatThumbnailImageChaceDirectory = new File(
					mMailChat.getChatThumbnailImageCacheDirectory(mAccount));
			files = chatThumbnailImageChaceDirectory.listFiles();
			deleteFiles(files);

			File chatLocalThumbnailImageChaceDirectory = new File(
					mMailChat.getChatLocalThumbnailImageCacheDirectory(mAccount));
			files = chatLocalThumbnailImageChaceDirectory.listFiles();
			deleteFiles(files);

			File chatAttachmentDirectory = new File(
					mMailChat.getChatAttachmentDirectory(mAccount));
			files = chatAttachmentDirectory.listFiles();
			deleteFiles(files);
			
//			File ImageLoaderDirectory = new File(
//					MailChat.getInstance().getImageLoaderPath());
//			files = ImageLoaderDirectory.listFiles();
//			deleteFiles(files);
			MailChat.getInstance().cleanImageLoaderCache();

			//更改聊天数据库状态
			for(String cUid:listTypeCAttachmentUid(2)){
				updateCAttachmentDownloadPauseFlag(cUid,true);
				updateCAttachmentDownloadProgress(cUid,0);
			}
			for(String dUid:listTypeDAttachmentUid(2)){
				updateDAttachmentDownloadPauseFlag(dUid,true);
				updateDAttachmentDownloadProgress(dUid,0);
			}
			updateCAttImageLoadStata("all", false, true);
			updateDAttImageLoadStata("all", false, true);
			
            // 清除附件临时存储
            File temporaryAttachmentDirectory = new File(
				mMailChat.getMailAttachmentDirectory(mAccount));
            files = temporaryAttachmentDirectory.listFiles();
            deleteFiles(files);

            // 清除downloadingList
            synchronized (MailChat.downloadingList) {
                if (MailChat.downloadingList.size() > 0) {
                    NotificationManager notificationManager = (NotificationManager) MailChat.app
                            .getSystemService(Context.NOTIFICATION_SERVICE);
                    for (AttachmentView av : MailChat.downloadingList.values()) {
                        if (av.mDownloadingNotification != null) {
                            notificationManager.cancel(av.mDownloadingNotification.mId);
                        }
                    }
                    MailChat.downloadingList.clear();
                }
            }

	        // 清除附件存储
	        database.execute(false, new DbCallback<Void>() {
	            @Override
	            public Void doDbWork(final SQLiteDatabase db)
	                    throws WrappedException {
	                Cursor cursor = null;
	                List<String> localAtts = new ArrayList<String>();
                    try {
                        cursor = db.query("attachments",
                                new String[] { "id", "size", "store_data" },
                                "content_uri IS NOT NULL",
                                null, null, null, null);

                        while (cursor.moveToNext()) {
                            int id = cursor.getInt(0);
                            int size = cursor.getInt(1);
                            String store = cursor.getString(2);

                            if (store == null || store.length() == 0) {
                                localAtts.add(Integer.toString(id));
                            } else {
                                ContentValues cv = new ContentValues();
                                cv.putNull("content_uri");
                                cv.put("size", MimeUtility.getBase64Size(size));

                                db.update("attachments", cv, "id = ?",
                                        new String[] { Integer.toString(id) });
                            }
                        }
                    } finally {
                        Utility.closeQuietly(cursor);
                    }

                    StorageManager storageManager = StorageManager.getInstance(mApplication);
                    File attachmentDirectory = storageManager.getAttachmentDirectory(
                            uUid, database.getStorageProviderId());
                    File[] files = attachmentDirectory.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.exists() && !localAtts.contains(file.getName())) {
                                if (!file.delete()) {
                                    file.deleteOnExit();
                                }
                            }
                        }
                    }

	                return null;
	            }
	        });

	        resetVisibleLimits();
        } catch (Exception e) {
            Log.e(MailChat.LOG_COLLECTOR_TAG, e.toString());
        }
	}

	private void deleteFiles(File[] files){
		 if (files != null) {
             for (File file : files) {
                 if (file.exists()) {
                     if (!file.delete()) {
                         file.deleteOnExit();
                     }
                 }
             }
         }
	}

	public void pruneCachedAttachments() throws MessagingException {
		pruneCachedAttachments(false);
	}

	/**
	 * Deletes all cached attachments for the entire store.
	 * 
	 * @param force
	 * @throws cn.mailchat.mail.MessagingException
	 */
	private void pruneCachedAttachments(final boolean force)
			throws MessagingException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				if (force) {
					ContentValues cv = new ContentValues();
					cv.putNull("content_uri");
					db.update("attachments", cv, null, null);
				}
				final StorageManager storageManager = StorageManager
						.getInstance(mApplication);
				File[] files = storageManager.getAttachmentDirectory(uUid,
						database.getStorageProviderId()).listFiles();
				for (File file : files) {
					if (file.exists()) {
						if (!force) {
							Cursor cursor = null;
							try {
								cursor = db.query("attachments",
										new String[] { "store_data" },
										"id = ?",
										new String[] { file.getName() }, null,
										null, null);
								if (cursor.moveToNext()) {
									if (cursor.getString(0) == null) {
										if (MailChat.DEBUG)
											Log.d(MailChat.LOG_TAG,
													"Attachment "
															+ file.getAbsolutePath()
															+ " has no store data, not deleting");
										/*
										 * If the attachment has no store data
										 * it is not recoverable, so we won't
										 * delete it.
										 */
										continue;
									}
								}
							} finally {
								Utility.closeQuietly(cursor);
							}
						}
						if (!force) {
							try {
								ContentValues cv = new ContentValues();
								cv.putNull("content_uri");
								db.update("attachments", cv, "id = ?",
										new String[] { file.getName() });
							} catch (Exception e) {
								/*
								 * If the row has gone away before we got to
								 * mark it not-downloaded that's okay.
								 */
							}
						}
						if (!file.delete()) {
							file.deleteOnExit();
						}
					}
				}
				return null;
			}
		});
	}

	public void resetVisibleLimits() throws UnavailableStorageException {
		resetVisibleLimits(mAccount.getDisplayCount());
	}

	public void resetVisibleLimits(int visibleLimit)
			throws UnavailableStorageException {
		final ContentValues cv = new ContentValues();
		cv.put("visible_limit", Integer.toString(visibleLimit));
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				db.update("folders", cv, null, null);
				return null;
			}
		});
	}

	public ArrayList<PendingCommand> getPendingCommands()
			throws UnavailableStorageException {
		return database.execute(false,
				new DbCallback<ArrayList<PendingCommand>>() {
					@Override
					public ArrayList<PendingCommand> doDbWork(
							final SQLiteDatabase db) throws WrappedException {
						Cursor cursor = null;
						try {
							cursor = db.query("pending_commands", new String[] {
									"id", "command", "arguments" }, null, null,
									null, null, "id ASC");
							ArrayList<PendingCommand> commands = new ArrayList<PendingCommand>();
							while (cursor.moveToNext()) {
								PendingCommand command = new PendingCommand();
								command.mId = cursor.getLong(0);
								command.command = cursor.getString(1);
								String arguments = cursor.getString(2);
								command.arguments = arguments.split(",");
								for (int i = 0; i < command.arguments.length; i++) {
									command.arguments[i] = Utility
											.fastUrlDecode(command.arguments[i]);
								}
								commands.add(command);
							}
							return commands;
						} finally {
							Utility.closeQuietly(cursor);
						}
					}
				});
	}

	public void addPendingCommand(PendingCommand command)
			throws UnavailableStorageException {
		try {
			for (int i = 0; i < command.arguments.length; i++) {
				command.arguments[i] = URLEncoder.encode(command.arguments[i],
						"UTF-8");
			}
			final ContentValues cv = new ContentValues();
			cv.put("command", command.command);
			cv.put("arguments", Utility.combine(command.arguments, ','));
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					db.insert("pending_commands", "command", cv);
					return null;
				}
			});
		} catch (UnsupportedEncodingException uee) {
			throw new Error(
					"Aparently UTF-8 has been lost to the annals of history.");
		}
	}

	public void removePendingCommand(final PendingCommand command)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				db.delete("pending_commands", "id = ?",
						new String[] { Long.toString(command.mId) });
				return null;
			}
		});
	}

	public void removePendingCommands() throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				db.delete("pending_commands", null, null);
				return null;
			}
		});
	}

	public static class PendingCommand {
		private long mId;
		public String command;
		public String[] arguments;

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(command);
			sb.append(": ");
			for (String argument : arguments) {
				sb.append(", ");
				sb.append(argument);
				// sb.append("\n");
			}
			return sb.toString();
		}
	}

	@Override
	public boolean isMoveCapable() {
		return true;
	}

	@Override
	public boolean isCopyCapable() {
		return true;
	}

	public Message[] searchForMessages(
			MessageRetrievalListener retrievalListener, LocalSearch search)
			throws MessagingException {

		StringBuilder query = new StringBuilder();
		List<String> queryArgs = new ArrayList<String>();
		SqlQueryBuilder.buildWhereClause(mAccount, search.getConditions(),
				query, queryArgs);

		// Avoid "ambiguous column name" error by prefixing "id" with the
		// message table name
		String where = SqlQueryBuilder.addPrefixToSelection(
				new String[] { "id" }, "messages.", query.toString());

		String[] selectionArgs = queryArgs.toArray(EMPTY_STRING_ARRAY);

		String sqlQuery = "SELECT "
				+ GET_MESSAGES_COLS
				+ "FROM messages "
				+ "LEFT JOIN threads ON (threads.message_id = messages.id) "
				+ "LEFT JOIN folders ON (folders.id = messages.folder_id) WHERE "
				+ "((empty IS NULL OR empty != 1) AND deleted = 0)"
				+ ((!StringUtils.isNullOrEmpty(where)) ? " AND (" + where + ")"
						: "") + " ORDER BY date DESC";

		if (MailChat.DEBUG) {
			Log.d(MailChat.LOG_TAG, "Query = " + sqlQuery);
		}

		return getMessages(retrievalListener, null, sqlQuery, selectionArgs);
	}

	/*
	 * Given a query string, actually do the query for the messages and call the
	 * MessageRetrievalListener for each one
	 */
	private Message[] getMessages(final MessageRetrievalListener listener,
			final LocalFolder folder, final String queryString,
			final String[] placeHolders) throws MessagingException {
		final ArrayList<LocalMessage> messages = new ArrayList<LocalMessage>();
		final int j = database.execute(false, new DbCallback<Integer>() {
			@Override
			public Integer doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				Cursor cursor = null;
				int i = 0;
				try {
					cursor = db.rawQuery(queryString + " LIMIT 10",
							placeHolders);

					while (cursor.moveToNext()) {
						LocalMessage message = new LocalMessage(null, folder);
						message.populateFromGetMessageCursor(cursor);

						messages.add(message);
						if (listener != null) {
							listener.messageFinished(message, i, -1);
						}
						i++;
					}
					cursor.close();
					cursor = db.rawQuery(queryString + " LIMIT -1 OFFSET 10",
							placeHolders);

					while (cursor.moveToNext()) {
						LocalMessage message = new LocalMessage(null, folder);
						message.populateFromGetMessageCursor(cursor);

						messages.add(message);
						if (listener != null) {
							listener.messageFinished(message, i, -1);
						}
						i++;
					}
				} catch (Exception e) {
					Log.d(MailChat.LOG_TAG, "Got an exception", e);
				} finally {
					Utility.closeQuietly(cursor);
				}
				return i;
			}
		});
		if (listener != null) {
			listener.messagesFinished(j);
		}

		return messages.toArray(EMPTY_MESSAGE_ARRAY);

	}

	public Message[] getMessagesInThread(final long rootId)
			throws MessagingException {
		String rootIdString = Long.toString(rootId);

		LocalSearch search = new LocalSearch();
		search.and(Searchfield.THREAD_ID, rootIdString, Attribute.EQUALS);

		return searchForMessages(null, search);
	}

	public AttachmentInfo[] getAttachmentInfos(final String messageId)
	        throws UnavailableStorageException {
	    return database.execute(false, new DbCallback<AttachmentInfo[]>() {
            @Override
            public AttachmentInfo[] doDbWork(final SQLiteDatabase db)
                    throws WrappedException {
                
                Cursor cursor = null;
                AttachmentInfo[] atts = new AttachmentInfo[3];
                
                try {
                    cursor = db.query("attachments",
                            new String[] { "id", "name", "size", "mime_type", "store_data", "content_uri" },
                            "message_id = ? AND content_id IS NULL",
                            new String[] { messageId }, null, null, null);
                    int i = 0;
                    while (i < 3 && cursor.moveToNext()) {
                        AttachmentInfo att = new AttachmentInfo();
                        att.id = cursor.getInt(0);
                        att.name = cursor.getString(1);
                        att.size = cursor.getInt(2);
                        att.type = cursor.getString(3);
                        att.store = cursor.getString(4);
                        att.uri = cursor.getString(5);
                        atts[i] = att;
                        i++;
                    }
                } finally {
                    Utility.closeQuietly(cursor);
                }
                
                return atts;
            }
        });
	}
	
	public AttachmentInfo getAttachmentInfo(final String attachmentId)
			throws UnavailableStorageException {
		return database.execute(false, new DbCallback<AttachmentInfo>() {
			@Override
			public AttachmentInfo doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				String name;
				String type;
				int size;
				Cursor cursor = null;
				try {
					cursor = db.query("attachments", new String[] { "name",
							"size", "mime_type" }, "id = ?",
							new String[] { attachmentId }, null, null, null);
					if (!cursor.moveToFirst()) {
						return null;
					}
					name = cursor.getString(0);
					size = cursor.getInt(1);
					type = cursor.getString(2);
					final AttachmentInfo attachmentInfo = new AttachmentInfo();
					attachmentInfo.name = name;
					attachmentInfo.size = size;
					attachmentInfo.type = type;
					return attachmentInfo;
				} finally {
					Utility.closeQuietly(cursor);
				}
			}
		});
	}

	public static class AttachmentInfo {
	    public int id;
		public String name;
		public int size;
		public String type;
		public String store;
		public String uri;
	}

	public void createFolders(final List<LocalFolder> foldersToCreate,
			final int visibleLimit,final int mailVersion) throws UnavailableStorageException {
		database.execute(true, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				for (LocalFolder folder : foldersToCreate) {
					String name = folder.getName();
					final LocalFolder.PreferencesHolder prefHolder = folder.new PreferencesHolder();
					// When created, special folders should always be displayed
					// inbox should be integrated
					// and the inbox and drafts folders should be syncced by
					// default
					if (mAccount.isSpecialFolder(name)) {
						prefHolder.isCustomFolder=true;
						prefHolder.inTopGroup = true;
						prefHolder.displayClass = LocalFolder.FolderClass.FIRST_CLASS;
						if (name.equalsIgnoreCase(mAccount.getInboxFolderName())) {
							//是收件箱允许push
							prefHolder.isAllowPush=true;
							prefHolder.integrate = true;
							prefHolder.notifyClass = LocalFolder.FolderClass.FIRST_CLASS;
							prefHolder.pushClass = LocalFolder.FolderClass.FIRST_CLASS;
							prefHolder.syncClass = LocalFolder.FolderClass.FIRST_CLASS;
						} else {
							prefHolder.pushClass = LocalFolder.FolderClass.INHERITED;
							prefHolder.syncClass = LocalFolder.FolderClass.NO_CLASS;
						}
						
						/*	 草稿箱不自动同步
						if (name.equalsIgnoreCase(mAccount.getInboxFolderName())
								|| name.equalsIgnoreCase(mAccount
										.getDraftsFolderName())) {
						*/
						// Modified by LL
					}else {
						prefHolder.isCustomFolder=false;
						// 35企业邮箱账号（含客户使用的）默认收件箱和自定义邮件夹全部接收推送
						if (mailVersion== 1||mailVersion == 2) {
							//是35域邮箱允许push
							prefHolder.isAllowPush=true;
						}else {
							prefHolder.isAllowPush=false;
						}
					}
					folder.refresh(name, prefHolder);
					db.execSQL(
							"INSERT INTO folders (name, visible_limit, top_group, display_class, poll_class, notify_class, push_class, integrate,is_allow_push,is_custom_folder) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?,?)",
							new Object[] { name, visibleLimit,
									prefHolder.inTopGroup ? 1 : 0,
									prefHolder.displayClass.name(),
									prefHolder.syncClass.name(),
									prefHolder.notifyClass.name(),
									prefHolder.pushClass.name(),
									prefHolder.integrate ? 1 : 0,
									prefHolder.isAllowPush ? 1 : 0,prefHolder.isCustomFolder ? 1 : 0});	
				}
				if (!isAddOnce) {
					isAddOnce=true;
					if (mailVersion== 1||mailVersion == 2) {
						Log.d(MailChat.LOG_COLLECTOR_TAG,"::: "+ mAccount.getEmail()+" is 35 mail ( "+mailVersion+" )");
						//是35域邮箱
						mAccount.setIs35Mail(true);
						if (StringUtils.isNullOrEmpty(mAccount
								.getSettingAccountNotifationScopeSecondTitle())) {
							mAccount.setSettingAccountNotifationScopeSecondTitle(MailChat.app
									.getString(R.string.setting_account_notifation_scope_second_title35));
						}
					}else {
						mAccount.setIs35Mail(false);
						if (StringUtils.isNullOrEmpty(mAccount
								.getSettingAccountNotifationScopeSecondTitle())) {
							mAccount.setSettingAccountNotifationScopeSecondTitle(MailChat.app
									.getString(R.string.setting_account_notifation_scope_second_title));
						}
					}
					mAccount.save(Preferences.getPreferences(MailChat.app));
				}
				return null;
			}
		});
	}

	private String serializeFlags(Flag[] flags) {
		List<Flag> extraFlags = new ArrayList<Flag>();

		for (Flag flag : flags) {
			switch (flag) {
			case DELETED:
			case SEEN:
			case FLAGGED:
			case ANSWERED:
			case FORWARDED: {
				break;
			}
			default: {
				extraFlags.add(flag);
			}
			}
		}

		return Utility.combine(extraFlags.toArray(EMPTY_FLAG_ARRAY), ',')
				.toUpperCase(Locale.US);
	}

	public class LocalFolder extends Folder implements Serializable {
		/**
         *
         */
		private static final long serialVersionUID = -1973296520918624767L;
		private String mName = null;
		private long mFolderId = -1;
		private int mVisibleLimit = -1;
		private String prefId = null;
		private FolderClass mDisplayClass = FolderClass.NO_CLASS;
		private FolderClass mSyncClass = FolderClass.INHERITED;
		private FolderClass mPushClass = FolderClass.SECOND_CLASS;
		private FolderClass mNotifyClass = FolderClass.INHERITED;
		private String mPushState = null;
		private boolean mIsAllowPush=false;//邮件夹是否接收推送并提醒，1表示接收   0表示不接收
		private boolean mIsCustomFolder=false;//是否自定义邮件夹，1标识默认系统邮件夹，0表示自定义邮件夹
		private boolean mIntegrate = false;
		// mLastUid is used during syncs. It holds the highest UID within the
		// local folder so we
		// know whether or not an unread message added to the local folder is
		// actually "new" or not.
		private Integer mLastUid = null;
		private boolean mInTopGroup=false;

		public LocalFolder(String name) {
			super(LocalStore.this.mAccount);
			this.mName = name;

			if (LocalStore.this.mAccount.getInboxFolderName().equals(getName())) {
				mSyncClass = FolderClass.FIRST_CLASS;
				mPushClass = FolderClass.FIRST_CLASS;
				mInTopGroup = true;
				mIsCustomFolder=false;
			}

		}

		public LocalFolder(long id) {
			super(LocalStore.this.mAccount);
			this.mFolderId = id;
		}

		public long getId() {
			return mFolderId;
		}

		@Override
		public void open(final int mode) throws MessagingException {

			if (isOpen() && (getMode() == mode || mode == OPEN_MODE_RO)) {
				return;
			} else if (isOpen()) {
				// previously opened in READ_ONLY and now requesting READ_WRITE
				// so close connection and reopen
				close();
			}

			try {
				database.execute(false, new DbCallback<Void>() {
					@Override
					public Void doDbWork(final SQLiteDatabase db)
							throws WrappedException {
						Cursor cursor = null;
						try {
							String baseQuery = "SELECT " + GET_FOLDER_COLS
									+ " FROM folders ";

							if (mName != null) {
								cursor = db.rawQuery(baseQuery
										+ "where folders.name = ?",
										new String[] { mName });
							} else {
								cursor = db
										.rawQuery(baseQuery
												+ "where folders.id = ?",
												new String[] { Long
														.toString(mFolderId) });
							}

							if (cursor.moveToFirst()
									&& !cursor.isNull(FOLDER_ID_INDEX)) {
								int folderId = cursor.getInt(FOLDER_ID_INDEX);
								if (folderId > 0) {
									open(cursor);
								}
							} else {
								Log.w(MailChat.LOG_TAG, "Creating folder "
										+ getName() + " with existing id "
										+ getId());
								create(FolderType.HOLDS_MESSAGES);
								open(mode);
							}
						} catch (MessagingException e) {
							throw new WrappedException(e);
						} finally {
							Utility.closeQuietly(cursor);
						}
						return null;
					}
				});
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}
		}

		private void open(Cursor cursor) throws MessagingException {
			mFolderId = cursor.getInt(FOLDER_ID_INDEX);
			mName = cursor.getString(FOLDER_NAME_INDEX);
			mVisibleLimit = cursor.getInt(FOLDER_VISIBLE_LIMIT_INDEX);
			mPushState = cursor.getString(FOLDER_PUSH_STATE_INDEX);
//			super.setmIsAllowPush(cursor.getInt(FOLDER_IS_ALLOW_PUSH_STATE_INDEX));
//			super.setIsCustomFolder(cursor.getInt(FOLDER_CUSTOM_FOLDER));
			super.setStatus(cursor.getString(FOLDER_STATUS_INDEX));
			// Only want to set the local variable stored in the super class.
			// This class
			// does a DB update on setLastChecked
			super.setLastChecked(cursor.getLong(FOLDER_LAST_CHECKED_INDEX));
			super.setLastPush(cursor.getLong(FOLDER_LAST_PUSHED_INDEX));
			mInTopGroup = (cursor.getInt(FOLDER_TOP_GROUP_INDEX)) == 1 ? true
					: false;
			mIsAllowPush= (cursor.getInt(FOLDER_IS_ALLOW_PUSH_STATE_INDEX)) == 1 ? true
					: false;
			mIsCustomFolder= (cursor.getInt(FOLDER_CUSTOM_FOLDER)) == 0 ? true
					: false;
			mIntegrate = (cursor.getInt(FOLDER_INTEGRATE_INDEX) == 1) ? true
					: false;
			String noClass = FolderClass.NO_CLASS.toString();
			String displayClass = cursor.getString(FOLDER_DISPLAY_CLASS_INDEX);
			mDisplayClass = Folder.FolderClass
					.valueOf((displayClass == null) ? noClass : displayClass);
			String notifyClass = cursor.getString(FOLDER_NOTIFY_CLASS_INDEX);
			mNotifyClass = Folder.FolderClass
					.valueOf((notifyClass == null) ? noClass : notifyClass);
			String pushClass = cursor.getString(FOLDER_PUSH_CLASS_INDEX);
			mPushClass = Folder.FolderClass
					.valueOf((pushClass == null) ? noClass : pushClass);
			String syncClass = cursor.getString(FOLDER_SYNC_CLASS_INDEX);
			mSyncClass = Folder.FolderClass
					.valueOf((syncClass == null) ? noClass : syncClass);
		}

		@Override
		public boolean isOpen() {
			return (mFolderId != -1 && mName != null);
		}

		@Override
		public int getMode() {
			return OPEN_MODE_RW;
		}

		@Override
		public String getName() {
			return mName;
		}

		@Override
		public boolean exists() throws MessagingException {
			return database.execute(false, new DbCallback<Boolean>() {
				@Override
				public Boolean doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					try {
						cursor = db.rawQuery("SELECT id FROM folders "
								+ "where folders.name = ?",
								new String[] { LocalFolder.this.getName() });
						if (cursor.moveToFirst()) {
							int folderId = cursor.getInt(0);
							return (folderId > 0);
						}

						return false;
					} finally {
						Utility.closeQuietly(cursor);
					}
				}
			});
		}

		@Override
		public boolean create(FolderType type) throws MessagingException {
			return create(type, mAccount.getDisplayCount());
		}

		@Override
		public boolean create(FolderType type, final int visibleLimit)
				throws MessagingException {
			if (exists()) {
				throw new MessagingException("Folder " + mName
						+ " already exists.");
			}
			List<LocalFolder> foldersToCreate = new ArrayList<LocalFolder>(1);
			foldersToCreate.add(this);
			LocalStore.this.createFolders(foldersToCreate, visibleLimit,0);

			return true;
		}

		private class PreferencesHolder {
			FolderClass displayClass = mDisplayClass;
			FolderClass syncClass = mSyncClass;
			FolderClass notifyClass = mNotifyClass;
			FolderClass pushClass = mPushClass;
			boolean inTopGroup = mInTopGroup;
			boolean isAllowPush=mIsAllowPush;
			boolean isCustomFolder=mIsCustomFolder;
			boolean integrate = mIntegrate;
		}

		@Override
		public void close() {
			mFolderId = -1;
		}

		@Override
		public int getMessageCount() throws MessagingException {
			try {
				return database.execute(false, new DbCallback<Integer>() {
					@Override
					public Integer doDbWork(final SQLiteDatabase db)
							throws WrappedException {
						try {
							open(OPEN_MODE_RW);
						} catch (MessagingException e) {
							throw new WrappedException(e);
						}
						Cursor cursor = null;
						try {
							cursor = db
									.rawQuery(
											"SELECT COUNT(id) FROM messages WHERE (empty IS NULL OR empty != 1) AND deleted = 0 and folder_id = ?",
											new String[] { Long
													.toString(mFolderId) });
							cursor.moveToFirst();
							return cursor.getInt(0); // messagecount
						} finally {
							Utility.closeQuietly(cursor);
						}
					}
				});
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}
		}

		@Override
		public int getUnreadMessageCount() throws MessagingException {
			if (mFolderId == -1) {
				open(OPEN_MODE_RW);
			}

			try {
				return database.execute(false, new DbCallback<Integer>() {
					@Override
					public Integer doDbWork(final SQLiteDatabase db)
							throws WrappedException {
						int unreadMessageCount = 0;
						Cursor cursor = db
								.query("messages",
										new String[] { "COUNT(id)" },
										"folder_id = ? AND (empty IS NULL OR empty != 1) AND deleted = 0 AND read=0",
										new String[] { Long.toString(mFolderId) },
										null, null, null);

						try {
							if (cursor.moveToFirst()) {
								unreadMessageCount = cursor.getInt(0);
							}
						} finally {
							cursor.close();
						}

						return unreadMessageCount;
					}
				});
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}
		}

		@Override
		public int getFlaggedMessageCount() throws MessagingException {
			if (mFolderId == -1) {
				open(OPEN_MODE_RW);
			}

			try {
				return database.execute(false, new DbCallback<Integer>() {
					@Override
					public Integer doDbWork(final SQLiteDatabase db)
							throws WrappedException {
						int flaggedMessageCount = 0;
						Cursor cursor = db
								.query("messages",
										new String[] { "COUNT(id)" },
										"folder_id = ? AND (empty IS NULL OR empty != 1) AND deleted = 0 AND flagged = 1",
										new String[] { Long.toString(mFolderId) },
										null, null, null);

						try {
							if (cursor.moveToFirst()) {
								flaggedMessageCount = cursor.getInt(0);
							}
						} finally {
							cursor.close();
						}

						return flaggedMessageCount;
					}
				});
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}
		}

		@Override
		public void setLastChecked(final long lastChecked)
				throws MessagingException {
			try {
				open(OPEN_MODE_RW);
				LocalFolder.super.setLastChecked(lastChecked);
			} catch (MessagingException e) {
				throw new WrappedException(e);
			}
			updateFolderColumn("last_updated", lastChecked);
		}

		@Override
		public void setLastPush(final long lastChecked)
				throws MessagingException {
			try {
				open(OPEN_MODE_RW);
				LocalFolder.super.setLastPush(lastChecked);
			} catch (MessagingException e) {
				throw new WrappedException(e);
			}
			updateFolderColumn("last_pushed", lastChecked);
		}

		public int getVisibleLimit() throws MessagingException {
			open(OPEN_MODE_RW);
			return mVisibleLimit;
		}

		public void purgeToVisibleLimit(MessageRemovalListener listener)
				throws MessagingException {
			// don't purge messages while a Search is active since it might
			// throw away search results
			if (!Search.isActive()) {
				if (mVisibleLimit == 0) {
					return;
				}
				open(OPEN_MODE_RW);
				Message[] messages = getMessages(null, false);
				for (int i = mVisibleLimit; i < messages.length; i++) {
					if (listener != null) {
						listener.messageRemoved(messages[i]);
					}
					messages[i].destroy();
				}
			}
		}

		public void setVisibleLimit(final int visibleLimit)
				throws MessagingException {
			mVisibleLimit = visibleLimit;
			updateFolderColumn("visible_limit", mVisibleLimit);
		}

		@Override
		public void setStatus(final String status) throws MessagingException {
			updateFolderColumn("status", status);
		}
		/**
		 * 
		 * method name: setFolderIsAllowPush
		 * function @Description: TODO
		 * Parameters and return values description:
		 *      @param isAllowPush
		 *      @throws MessagingException   field_name
		 *      void   return type
		 *  @History memory：
		 *     @Date：2015-6-10 下午4:57:00	@Modified by：zhangjx
		 *     @Description:邮件夹是否接收推送并提醒，1表示接收   0表示不接收
		 */
		public void setFolderIsAllowPush(final boolean isAllowPush)
				throws MessagingException {
			mIsAllowPush=isAllowPush;
			updateFolderColumn("is_allow_push", isAllowPush? 1 : 0);
		}
		public void setPushState(final String pushState)
				throws MessagingException {
			mPushState = pushState;
			updateFolderColumn("push_state", pushState);
		}

		private void updateFolderColumn(final String column, final Object value)
				throws MessagingException {
			try {
				database.execute(false, new DbCallback<Void>() {
					@Override
					public Void doDbWork(final SQLiteDatabase db)
							throws WrappedException {
						try {
							open(OPEN_MODE_RW);
						} catch (MessagingException e) {
							throw new WrappedException(e);
						}
						db.execSQL("UPDATE folders SET " + column
								+ " = ? WHERE id = ?", new Object[] { value,
								mFolderId });
						return null;
					}
				});
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}
		}

		public String getPushState() {
			return mPushState;
		}


		@Override
		public FolderClass getDisplayClass() {
			return mDisplayClass;
		}

		@Override
		public FolderClass getSyncClass() {
			return (FolderClass.INHERITED == mSyncClass) ? getDisplayClass()
					: mSyncClass;
		}

		public FolderClass getRawSyncClass() {
			return mSyncClass;
		}

		public FolderClass getNotifyClass() {
			return (FolderClass.INHERITED == mNotifyClass) ? getPushClass()
					: mNotifyClass;
		}

		public FolderClass getRawNotifyClass() {
			return mNotifyClass;
		}

		@Override
		public FolderClass getPushClass() {
			return (FolderClass.INHERITED == mPushClass) ? getSyncClass()
					: mPushClass;
		}

		public FolderClass getRawPushClass() {
			return mPushClass;
		}

		public void setDisplayClass(FolderClass displayClass)
				throws MessagingException {
			mDisplayClass = displayClass;
			updateFolderColumn("display_class", mDisplayClass.name());

		}

		public void setSyncClass(FolderClass syncClass)
				throws MessagingException {
			mSyncClass = syncClass;
			updateFolderColumn("poll_class", mSyncClass.name());
		}

		public void setPushClass(FolderClass pushClass)
				throws MessagingException {
			mPushClass = pushClass;
			updateFolderColumn("push_class", mPushClass.name());
		}

		public void setNotifyClass(FolderClass notifyClass)
				throws MessagingException {
			mNotifyClass = notifyClass;
			updateFolderColumn("notify_class", mNotifyClass.name());
		}

		public boolean isIntegrate() {
			return mIntegrate;
		}

		public void setIntegrate(boolean integrate) throws MessagingException {
			mIntegrate = integrate;
			updateFolderColumn("integrate", mIntegrate ? 1 : 0);
		}

		private String getPrefId(String name) {
			if (prefId == null) {
				prefId = uUid + "." + name;
			}

			return prefId;
		}

		private String getPrefId() throws MessagingException {
			open(OPEN_MODE_RW);
			return getPrefId(mName);

		}

		public void delete() throws MessagingException {
			String id = getPrefId();

			SharedPreferences.Editor editor = LocalStore.this.getPreferences()
					.edit();

			editor.remove(id + ".displayMode");
			editor.remove(id + ".syncMode");
			editor.remove(id + ".pushMode");
			editor.remove(id + ".inTopGroup");
			editor.remove(id + ".isAllowPush");
			editor.remove(id + ".isCustomFolder");
			editor.remove(id + ".integrate");
			editor.commit();
		}

		public void save() throws MessagingException {
			SharedPreferences.Editor editor = LocalStore.this.getPreferences()
					.edit();
			save(editor);
			editor.commit();
		}

		public void save(SharedPreferences.Editor editor)
				throws MessagingException {
			String id = getPrefId();

			// there can be a lot of folders. For the defaults, let's not save
			// prefs, saving space, except for INBOX
			if (mDisplayClass == FolderClass.NO_CLASS
					&& !mAccount.getInboxFolderName().equals(getName())) {
				editor.remove(id + ".displayMode");
			} else {
				editor.putString(id + ".displayMode", mDisplayClass.name());
			}

			if (mSyncClass == FolderClass.INHERITED
					&& !mAccount.getInboxFolderName().equals(getName())) {
				editor.remove(id + ".syncMode");
			} else {
				editor.putString(id + ".syncMode", mSyncClass.name());
			}

			if (mNotifyClass == FolderClass.INHERITED
					&& !mAccount.getInboxFolderName().equals(getName())) {
				editor.remove(id + ".notifyMode");
			} else {
				editor.putString(id + ".notifyMode", mNotifyClass.name());
			}

			if (mPushClass == FolderClass.SECOND_CLASS
					&& !mAccount.getInboxFolderName().equals(getName())) {
				editor.remove(id + ".pushMode");
			} else {
				editor.putString(id + ".pushMode", mPushClass.name());
			}
			editor.putBoolean(id + ".inTopGroup", mInTopGroup);
			editor.putBoolean(id + ".isAllowPush", mIsAllowPush);
			editor.putBoolean(id + ".isCustomFolder", mIsCustomFolder);
			editor.putBoolean(id + ".integrate", mIntegrate);

		}

		public void refresh(String name, PreferencesHolder prefHolder) {
			String id = getPrefId(name);

			SharedPreferences preferences = LocalStore.this.getPreferences();

			try {
				prefHolder.displayClass = FolderClass.valueOf(preferences
						.getString(id + ".displayMode",
								prefHolder.displayClass.name()));
			} catch (Exception e) {
				Log.e(MailChat.LOG_TAG, "Unable to load displayMode for "
						+ getName(), e);
			}
			if (prefHolder.displayClass == FolderClass.NONE) {
				prefHolder.displayClass = FolderClass.NO_CLASS;
			}

			try {
				prefHolder.syncClass = FolderClass.valueOf(preferences
						.getString(id + ".syncMode",
								prefHolder.syncClass.name()));
			} catch (Exception e) {
				Log.e(MailChat.LOG_TAG, "Unable to load syncMode for "
						+ getName(), e);

			}
			if (prefHolder.syncClass == FolderClass.NONE) {
				prefHolder.syncClass = FolderClass.INHERITED;
			}

			try {
				prefHolder.notifyClass = FolderClass.valueOf(preferences
						.getString(id + ".notifyMode",
								prefHolder.notifyClass.name()));
			} catch (Exception e) {
				Log.e(MailChat.LOG_TAG, "Unable to load notifyMode for "
						+ getName(), e);
			}
			if (prefHolder.notifyClass == FolderClass.NONE) {
				prefHolder.notifyClass = FolderClass.INHERITED;
			}

			try {
				prefHolder.pushClass = FolderClass.valueOf(preferences
						.getString(id + ".pushMode",
								prefHolder.pushClass.name()));
			} catch (Exception e) {
				Log.e(MailChat.LOG_TAG, "Unable to load pushMode for "
						+ getName(), e);
			}
			if (prefHolder.pushClass == FolderClass.NONE) {
				prefHolder.pushClass = FolderClass.INHERITED;
			}
			prefHolder.inTopGroup = preferences.getBoolean(id + ".inTopGroup",
					prefHolder.inTopGroup);
			prefHolder.isAllowPush = preferences.getBoolean(id + ".isAllowPush",
					prefHolder.isAllowPush);
			prefHolder.isCustomFolder = preferences.getBoolean(id + ".isCustomFolder",
					prefHolder.isCustomFolder);
			prefHolder.integrate = preferences.getBoolean(id + ".integrate",
					prefHolder.integrate);

		}

		@Override
		public void fetch(final Message[] messages, final FetchProfile fp,
				final MessageRetrievalListener listener)
				throws MessagingException {
			try {
				database.execute(false, new DbCallback<Void>() {
					@Override
					public Void doDbWork(final SQLiteDatabase db)
							throws WrappedException {
						try {
							open(OPEN_MODE_RW);
							if (fp.contains(FetchProfile.Item.BODY)) {
								for (Message message : messages) {
									LocalMessage localMessage = (LocalMessage) message;
									Cursor cursor = null;
									MimeMultipart mp = new MimeMultipart();
									mp.setSubType("mixed");
									try {
										cursor = db
												.rawQuery(
														"SELECT html_content, text_content, mime_type FROM messages "
																+ "WHERE id = ?",
														new String[] { Long
																.toString(localMessage.mId) });
										cursor.moveToNext();
										String htmlContent = cursor
												.getString(0);
										String textContent = cursor
												.getString(1);
										String mimeType = cursor.getString(2);
										if (mimeType != null
												&& mimeType.toLowerCase(
														Locale.US).startsWith(
														"multipart/")) {
											// If this is a multipart message,
											// preserve both text
											// and html parts, as well as the
											// subtype.
											mp.setSubType(mimeType.toLowerCase(
													Locale.US).replaceFirst(
													"^multipart/", ""));
											if (textContent != null) {
												LocalTextBody body = new LocalTextBody(
														textContent,
														htmlContent);
												MimeBodyPart bp = new MimeBodyPart(
														body, "text/plain");
												mp.addBodyPart(bp);
											}

											if (mAccount.getMessageFormat() != MessageFormat.TEXT) {
												if (htmlContent != null) {
													TextBody body = new TextBody(
															htmlContent);
													MimeBodyPart bp = new MimeBodyPart(
															body, "text/html");
													mp.addBodyPart(bp);
												}

												// If we have both text and html
												// content and our MIME type
												// isn't multipart/alternative,
												// then corral them into a new
												// multipart/alternative part
												// and put that into the parent.
												// If it turns out that this is
												// the only part in the parent
												// MimeMultipart, it'll get
												// fixed below before we attach
												// to
												// the message.
												if (textContent != null
														&& htmlContent != null
														&& !mimeType
																.equalsIgnoreCase("multipart/alternative")) {
													MimeMultipart alternativeParts = mp;
													alternativeParts
															.setSubType("alternative");
													mp = new MimeMultipart();
													mp.addBodyPart(new MimeBodyPart(
															alternativeParts));
												}
											}
										} else if (mimeType != null
												&& mimeType
														.equalsIgnoreCase("text/plain")) {
											// If it's text, add only the plain
											// part. The MIME
											// container will drop away below.
											if (textContent != null) {
												LocalTextBody body = new LocalTextBody(
														textContent,
														htmlContent);
												MimeBodyPart bp = new MimeBodyPart(
														body, "text/plain");
												mp.addBodyPart(bp);
											}
										} else if (mimeType != null
												&& mimeType
														.equalsIgnoreCase("text/html")) {
											// If it's html, add only the html
											// part. The MIME
											// container will drop away below.
											if (htmlContent != null) {
												TextBody body = new TextBody(
														htmlContent);
												MimeBodyPart bp = new MimeBodyPart(
														body, "text/html");
												mp.addBodyPart(bp);
											}
										} else {
											// MIME type not set. Grab whatever
											// part we can get,
											// with Text taking precedence. This
											// preserves pre-HTML
											// composition behaviour.
											if (textContent != null) {
												LocalTextBody body = new LocalTextBody(
														textContent,
														htmlContent);
												MimeBodyPart bp = new MimeBodyPart(
														body, "text/plain");
												mp.addBodyPart(bp);
											} else if (htmlContent != null) {
												TextBody body = new TextBody(
														htmlContent);
												MimeBodyPart bp = new MimeBodyPart(
														body, "text/html");
												mp.addBodyPart(bp);
											}
										}

									} catch (Exception e) {
										Log.e(MailChat.LOG_TAG,
												"Exception fetching message:",
												e);
									} finally {
										Utility.closeQuietly(cursor);
									}

									try {
										cursor = db
												.query("attachments",
														new String[] { "id",
																"size", "name",
																"mime_type",
																"store_data",
																"content_uri",
																"content_id",
																"content_disposition",
																"content_transfer_encoding"},
														"message_id = ?",
														new String[] { Long
																.toString(localMessage.mId) },
														null, null, null);

										while (cursor.moveToNext()) {
											long id = cursor.getLong(0);
											int size = cursor.getInt(1);
											String name = cursor.getString(2);
											String type = cursor.getString(3);
											String storeData = cursor
													.getString(4);
											String contentUri = cursor
													.getString(5);
											String contentId = cursor
													.getString(6);
											String contentDisposition = cursor
													.getString(7);
											String encoding = cursor
                                                    .getString(8);
											if (encoding == null || encoding.length() == 0) {
    											encoding = MimeUtility
    													.getEncodingforType(type);
											}
											Body body = null;

											if (contentDisposition == null) {
												contentDisposition = "attachment";
											}

											if (contentUri != null) {
												if (MimeUtil.isMessage(type)) {
													body = new LocalAttachmentMessageBody(
															Uri.parse(contentUri),
															mApplication);
												} else {
													body = new LocalAttachmentBody(
															Uri.parse(contentUri),
															mApplication);
												}
											}

											MimeBodyPart bp = new LocalAttachmentBodyPart(
													body, id, localMessage.mId);
											bp.setEncoding(encoding);
											
											/*
											if (name != null) {
												bp.setHeader(
														MimeHeader.HEADER_CONTENT_TYPE,
														String.format(
																"%s;\r\n name=\"%s\"",
																type, name));
												bp.setHeader(
														MimeHeader.HEADER_CONTENT_DISPOSITION,
														String.format(
																Locale.US,
																"%s;\r\n filename=\"%s\";\r\n size=%d",
																contentDisposition,
																name, // TODO:
																		// Should
																		// use
																		// encoded
																		// word
																		// defined
																		// in
																		// RFC
																		// 2231.
																size));

											} else {
												bp.setHeader(
														MimeHeader.HEADER_CONTENT_TYPE,
														type);
												bp.setHeader(
														MimeHeader.HEADER_CONTENT_DISPOSITION,
														String.format(
																Locale.US,
																"%s;\r\n size=%d",
																contentDisposition,
																size));
											}
											*/
											// 修正发送附件size为Base64编码后正确大小
											// Modified by LL
											// BEGIN
											if (name != null) {
												bp.setHeader(
														MimeHeader.HEADER_CONTENT_TYPE,
														String.format(
																"%s;\r\n name=\"%s\"",
																type, name));
												bp.setHeader(
														MimeHeader.HEADER_CONTENT_DISPOSITION,
														String.format(
																Locale.US,
																"%s;\r\n filename=\"%s\";\r\n size=%d",
																contentDisposition,
																name, // TODO:
																		// Should
																		// use
																		// encoded
																		// word
																		// defined
																		// in
																		// RFC
																		// 2231.
																fp.contains(FetchProfile.Item.SEND) && bp.getBody() != null ?
																		MimeUtility.getBase64Size(size) : size));

											} else {
												bp.setHeader(
														MimeHeader.HEADER_CONTENT_TYPE,
														type);
												bp.setHeader(
														MimeHeader.HEADER_CONTENT_DISPOSITION,
														String.format(
																Locale.US,
																"%s;\r\n size=%d",
																contentDisposition,
																fp.contains(FetchProfile.Item.SEND) && bp.getBody() != null ?
																		MimeUtility.getBase64Size(size) : size));
											}
											// END

											bp.setHeader(
													MimeHeader.HEADER_CONTENT_ID,
													contentId);
											/*
											 * HEADER_ANDROID_ATTACHMENT_STORE_DATA
											 * is a custom header we add to that
											 * we can later pull the attachment
											 * from the remote store if
											 * necessary.
											 */
											/*
											bp.setHeader(
													MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA,
													storeData);
											*/
											// 还原MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA原始存储形式
											// Modified by LL
											// BEGIN
											if (storeData != null) {
												StringTokenizer tokens = new StringTokenizer(storeData, ",", false);
												while (tokens.hasMoreTokens()) {
													bp.setHeader(
															MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA,
															tokens.nextToken());
												}
											}
											// END

											mp.addBodyPart(bp);
										}
									} finally {
										Utility.closeQuietly(cursor);
									}

									if (mp.getCount() == 0) {
										// If we have no body, remove the
										// container and create a
										// dummy plain text body. This check
										// helps prevents us from
										// triggering T_MIME_NO_TEXT and
										// T_TVD_MIME_NO_HEADERS
										// SpamAssassin rules.
										localMessage.setHeader(
												MimeHeader.HEADER_CONTENT_TYPE,
												"text/plain");
										localMessage.setBody(new TextBody(""));
									} else if (mp.getCount() == 1
											&& (mp.getBodyPart(0) instanceof LocalAttachmentBodyPart) == false)

									{
										// If we have only one part, drop the
										// MimeMultipart container.
										BodyPart part = mp.getBodyPart(0);
										localMessage.setHeader(
												MimeHeader.HEADER_CONTENT_TYPE,
												part.getContentType());
										localMessage.setBody(part.getBody());
									} else {
										// Otherwise, attach the MimeMultipart
										// to the message.
										localMessage.setBody(mp);
									}
								}
							}
						} catch (MessagingException e) {
							throw new WrappedException(e);
						}
						return null;
					}
				});
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}
		}

		@Override
		public Message[] getMessages(int start, int end, Date earliestDate,
				MessageRetrievalListener listener) throws MessagingException {
			open(OPEN_MODE_RW);
			throw new MessagingException(
					"LocalStore.getMessages(int, int, MessageRetrievalListener) not yet implemented");
		}

		/**
		 * Populate the header fields of the given list of messages by reading
		 * the saved header data from the database.
		 * 
		 * @param messages
		 *            The messages whose headers should be loaded.
		 * @throws UnavailableStorageException
		 */
		private void populateHeaders(final List<LocalMessage> messages)
				throws UnavailableStorageException {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(final SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					Cursor cursor = null;
					if (messages.isEmpty()) {
						return null;
					}
					try {
						Map<Long, LocalMessage> popMessages = new HashMap<Long, LocalMessage>();
						List<String> ids = new ArrayList<String>();
						StringBuilder questions = new StringBuilder();

						for (int i = 0; i < messages.size(); i++) {
							if (i != 0) {
								questions.append(", ");
							}
							questions.append("?");
							LocalMessage message = messages.get(i);
							Long id = message.getId();
							ids.add(Long.toString(id));
							popMessages.put(id, message);

						}

						cursor = db.rawQuery(
								"SELECT message_id, name, value FROM headers "
										+ "WHERE message_id in ( " + questions
										+ ") ORDER BY id ASC",
								ids.toArray(EMPTY_STRING_ARRAY));

						while (cursor.moveToNext()) {
							Long id = cursor.getLong(0);
							String name = cursor.getString(1);
							String value = cursor.getString(2);
							// Log.i(MailChat.LOG_TAG, "Retrieved header name= "
							// +
							// name + ", value = " + value + " for message " +
							// id);
							popMessages.get(id).addHeader(name, value);
						}
					} finally {
						Utility.closeQuietly(cursor);
					}
					return null;
				}
			});
		}

		public String getMessageUidById(final long id)
				throws MessagingException {
			try {
				return database.execute(false, new DbCallback<String>() {
					@Override
					public String doDbWork(final SQLiteDatabase db)
							throws WrappedException,
							UnavailableStorageException {
						try {
							open(OPEN_MODE_RW);
							Cursor cursor = null;

							try {
								cursor = db
										.rawQuery(
												"SELECT uid FROM messages "
														+ "WHERE id = ? AND folder_id = ?",
												new String[] {
														Long.toString(id),
														Long.toString(mFolderId) });
								if (!cursor.moveToNext()) {
									return null;
								}
								return cursor.getString(0);
							} finally {
								Utility.closeQuietly(cursor);
							}
						} catch (MessagingException e) {
							throw new WrappedException(e);
						}
					}
				});
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}
		}

		@Override
		public LocalMessage getMessage(final String uid)
				throws MessagingException {
			try {
				return database.execute(false, new DbCallback<LocalMessage>() {
					@Override
					public LocalMessage doDbWork(final SQLiteDatabase db)
							throws WrappedException,
							UnavailableStorageException {
						try {
							open(OPEN_MODE_RW);
							LocalMessage message = new LocalMessage(uid,
									LocalFolder.this);
							Cursor cursor = null;

							try {
								cursor = db
										.rawQuery(
												"SELECT "
														+ GET_MESSAGES_COLS
														+ "FROM messages "
														+ "LEFT JOIN threads ON (threads.message_id = messages.id) "
														+ "WHERE uid = ? AND folder_id = ?",
												new String[] {
														message.getUid(),
														Long.toString(mFolderId) });
								if (!cursor.moveToNext()) {
									return null;
								}
								message.populateFromGetMessageCursor(cursor);
							} finally {
								Utility.closeQuietly(cursor);
							}
							return message;
						} catch (MessagingException e) {
							throw new WrappedException(e);
						}
					}
				});
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}
		}

		@Override
		public Message[] getMessages(MessageRetrievalListener listener)
				throws MessagingException {
			return getMessages(listener, true);
		}

		@Override
		public Message[] getMessages(final MessageRetrievalListener listener,
				final boolean includeDeleted) throws MessagingException {
			try {
				return database.execute(false, new DbCallback<Message[]>() {
					@Override
					public Message[] doDbWork(final SQLiteDatabase db)
							throws WrappedException,
							UnavailableStorageException {
						try {
							open(OPEN_MODE_RW);
							return LocalStore.this
									.getMessages(
											listener,
											LocalFolder.this,
											"SELECT "
													+ GET_MESSAGES_COLS
													+ "FROM messages "
													+ "LEFT JOIN threads ON (threads.message_id = messages.id) "
													+ "WHERE (empty IS NULL OR empty != 1) AND "
													+ (includeDeleted ? ""
															: "deleted = 0 AND ")
													+ "folder_id = ? ORDER BY date DESC",
											new String[] { Long
													.toString(mFolderId) });
						} catch (MessagingException e) {
							throw new WrappedException(e);
						}
					}
				});
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}
		}

		@Override
		public Message[] getMessages(String[] uids,
				MessageRetrievalListener listener) throws MessagingException {
			open(OPEN_MODE_RW);
			if (uids == null) {
				return getMessages(listener);
			}
			ArrayList<Message> messages = new ArrayList<Message>();
			for (String uid : uids) {
				Message message = getMessage(uid);
				if (message != null) {
					messages.add(message);
				}
			}
			return messages.toArray(EMPTY_MESSAGE_ARRAY);
		}

		@Override
		public Map<String, String> copyMessages(Message[] msgs, Folder folder)
				throws MessagingException {
			if (!(folder instanceof LocalFolder)) {
				throw new MessagingException(
						"copyMessages called with incorrect Folder");
			}
			return ((LocalFolder) folder).appendMessages(msgs, true);
		}

		@Override
		public Map<String, String> moveMessages(final Message[] msgs,
				final Folder destFolder) throws MessagingException {
			if (!(destFolder instanceof LocalFolder)) {
				throw new MessagingException(
						"moveMessages called with non-LocalFolder");
			}

			final LocalFolder lDestFolder = (LocalFolder) destFolder;

			final Map<String, String> uidMap = new HashMap<String, String>();

			try {
				database.execute(false, new DbCallback<Void>() {
					@Override
					public Void doDbWork(final SQLiteDatabase db)
							throws WrappedException,
							UnavailableStorageException {
						try {
							lDestFolder.open(OPEN_MODE_RW);
							for (Message message : msgs) {
								LocalMessage lMessage = (LocalMessage) message;

								String oldUID = message.getUid();
								message.setMailId(null);

								if (MailChat.DEBUG) {
									Log.d(MailChat.LOG_TAG,
											"Updating folder_id to "
													+ lDestFolder.getId()
													+ " for message with UID "
													+ message.getUid()
													+ ", id "
													+ lMessage.getId()
													+ " currently in folder "
													+ getName());
								}

								String newUid = MailChat.LOCAL_UID_PREFIX
										+ UUID.randomUUID().toString();
								message.setUid(newUid);

								uidMap.put(oldUID, newUid);

								// Message threading in the target folder
								ThreadInfo threadInfo = lDestFolder
										.doMessageThreading(db, message);

								/*
								 * "Move" the message into the new folder
								 */
								long msgId = lMessage.getId();
								String[] idArg = new String[] { Long
										.toString(msgId) };

								ContentValues cv = new ContentValues();
								cv.put("folder_id", lDestFolder.getId());
								cv.put("uid", newUid);
								cv.put("mail_id", message.getMailId());

								db.update("messages", cv, "id = ?", idArg);

								// Create/update entry in 'threads' table for
								// the message in the
								// target folder
								cv.clear();
								cv.put("message_id", msgId);
								if (threadInfo.threadId == -1) {
									if (threadInfo.rootId != -1) {
										cv.put("root", threadInfo.rootId);
									}

									if (threadInfo.parentId != -1) {
										cv.put("parent", threadInfo.parentId);
									}

									db.insert("threads", null, cv);
								} else {
									db.update(
											"threads",
											cv,
											"id = ?",
											new String[] { Long
													.toString(threadInfo.threadId) });
								}

								/*
								 * Add a placeholder message so we won't
								 * download the original message again if we
								 * synchronize before the remote move is
								 * complete.
								 */

								// We need to open this folder to get the folder
								// id
								open(OPEN_MODE_RW);

								cv.clear();
								cv.put("uid", oldUID);
								cv.putNull("flags");
								cv.put("read", 1);
								cv.put("deleted", 1);
								cv.put("folder_id", mFolderId);
								cv.put("empty", 0);

								String messageId = message.getMessageId();
								if (messageId != null) {
									cv.put("message_id", messageId);
								}

								final long newId;
								if (threadInfo.msgId != -1) {
									// There already existed an empty message in
									// the target folder.
									// Let's use it as placeholder.

									newId = threadInfo.msgId;

									db.update(
											"messages",
											cv,
											"id = ?",
											new String[] { Long.toString(newId) });
								} else {
									newId = db.insert("messages", null, cv);
								}

								/*
								 * Update old entry in 'threads' table to point
								 * to the newly created placeholder.
								 */

								cv.clear();
								cv.put("message_id", newId);
								db.update("threads", cv, "id = ?",
										new String[] { Long.toString(lMessage
												.getThreadId()) });
							}
						} catch (MessagingException e) {
							throw new WrappedException(e);
						}
						return null;
					}
				});

				notifyChange();

				return uidMap;
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}

		}

		/**
		 * Convenience transaction wrapper for storing a message and set it as
		 * fully downloaded. Implemented mainly to speed up DB transaction
		 * commit.
		 * 
		 * @param message
		 *            Message to store. Never <code>null</code>.
		 * @param runnable
		 *            What to do before setting {@link Flag#X_DOWNLOADED_FULL}.
		 *            Never <code>null</code>.
		 * @return The local version of the message. Never <code>null</code>.
		 * @throws MessagingException
		 */
		public Message storeSmallMessage(final Message message,
				final Runnable runnable) throws MessagingException {
			return database.execute(true, new DbCallback<Message>() {
				@Override
				public Message doDbWork(final SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					try {
						appendMessages(new Message[] { message });
						final String uid = message.getUid();
						final Message result = getMessage(uid);
						runnable.run();
						// Set a flag indicating this message has now be fully
						// downloaded
						result.setFlag(Flag.X_DOWNLOADED_FULL, true);
						return result;
					} catch (MessagingException e) {
						throw new WrappedException(e);
					}
				}
			});
		}

		/**
		 * The method differs slightly from the contract; If an incoming message
		 * already has a uid assigned and it matches the uid of an existing
		 * message then this message will replace the old message. It is
		 * implemented as a delete/insert. This functionality is used in saving
		 * of drafts and re-synchronization of updated server messages.
		 * 
		 * NOTE that although this method is located in the LocalStore class, it
		 * is not guaranteed that the messages supplied as parameters are
		 * actually {@link LocalMessage} instances (in fact, in most cases, they
		 * are not). Therefore, if you want to make local changes only to a
		 * message, retrieve the appropriate local message instance first (if it
		 * already exists).
		 */
		@Override
		public Map<String, String> appendMessages(Message[] messages)
				throws MessagingException {
			return appendMessages(messages, false);
		}

		public void destroyMessages(final Message[] messages) {
			try {
				database.execute(true, new DbCallback<Void>() {
					@Override
					public Void doDbWork(final SQLiteDatabase db)
							throws WrappedException,
							UnavailableStorageException {
						for (Message message : messages) {
							try {
								message.destroy();
							} catch (MessagingException e) {
								throw new WrappedException(e);
							}
						}
						return null;
					}
				});
			} catch (MessagingException e) {
				throw new WrappedException(e);
			}
		}

		private ThreadInfo getThreadInfo(SQLiteDatabase db, String messageId,
				boolean onlyEmpty) {
			String sql = "SELECT t.id, t.message_id, t.root, t.parent "
					+ "FROM messages m "
					+ "LEFT JOIN threads t ON (t.message_id = m.id) "
					+ "WHERE m.folder_id = ? AND m.message_id = ? "
					+ ((onlyEmpty) ? "AND m.empty = 1 " : "")
					+ "ORDER BY m.id LIMIT 1";
			String[] selectionArgs = { Long.toString(mFolderId), messageId };
			Cursor cursor = db.rawQuery(sql, selectionArgs);

			if (cursor != null) {
				try {
					if (cursor.getCount() > 0) {
						cursor.moveToFirst();
						long threadId = cursor.getLong(0);
						long msgId = cursor.getLong(1);
						long rootId = (cursor.isNull(2)) ? -1 : cursor
								.getLong(2);
						long parentId = (cursor.isNull(3)) ? -1 : cursor
								.getLong(3);

						return new ThreadInfo(threadId, msgId, messageId,
								rootId, parentId);
					}
				} finally {
					cursor.close();
				}
			}

			return null;
		}

		// 获取非内联附件数目，分为推送邮件和解析邮件两种情况
		// Modified by LL
		// BEGIN
		private int getAttachmentCount(Message message, List<Part> atts, String html) {
			if (message.isSet(Flag.X_PUSH_MAIL)) {
				return ((MimeMessage) message).getAttachmentCount();
			} else {
				int count = 0;
				for (Part att : atts) {
					try {
						String cid = att.getContentId();
						if (cid == null
								|| cid.length() == 0
								|| (html != null
								    && html.length() > 0
								    && !html.contains("cid:" + cid))) {
							count++;
						}
					} catch (Exception e) {
						Log.e(MailChat.LOG_TAG, "Get Content-ID FAILED.");
					}
				}
				return count;
			}
		}

		// END

		/**
		 * The method differs slightly from the contract; If an incoming message
		 * already has a uid assigned and it matches the uid of an existing
		 * message then this message will replace the old message. This
		 * functionality is used in saving of drafts and re-synchronization of
		 * updated server messages.
		 * 
		 * NOTE that although this method is located in the LocalStore class, it
		 * is not guaranteed that the messages supplied as parameters are
		 * actually {@link LocalMessage} instances (in fact, in most cases, they
		 * are not). Therefore, if you want to make local changes only to a
		 * message, retrieve the appropriate local message instance first (if it
		 * already exists).
		 * 
		 * @param messages
		 * @param copy
		 * @return Map<String, String> uidMap of srcUids -> destUids
		 */
		private Map<String, String> appendMessages(final Message[] messages,
				final boolean copy) throws MessagingException {
			open(OPEN_MODE_RW);
			try {
				final Map<String, String> uidMap = new HashMap<String, String>();
				database.execute(true, new DbCallback<Void>() {
					@Override
					public Void doDbWork(final SQLiteDatabase db)
							throws WrappedException,
							UnavailableStorageException {
						try {
							for (Message message : messages) {
								if (!(message instanceof MimeMessage)) {
									throw new Error(
											"LocalStore can only store Messages that extend MimeMessage");
								}

								long oldMessageId = -1;
								String uid = message.getUid();
								if (uid == null || copy) {
									/*
									 * Create a new message in the database
									 */
									String randomLocalUid = MailChat.LOCAL_UID_PREFIX
											+ UUID.randomUUID().toString();

									if (copy) {
										// Save mapping: source UID -> target
										// UID
										uidMap.put(uid, randomLocalUid);
									} else {
										// Modify the Message instance to
										// reference the new UID
										message.setUid(randomLocalUid);
									}

									// The message will be saved with the newly
									// generated UID
									uid = randomLocalUid;
								} else {
									/*
									 * Replace an existing message in the
									 * database
									 */
									LocalMessage oldMessage = getMessage(uid);

									if (oldMessage != null) {
										oldMessageId = oldMessage.getId();
									}

									deleteAttachments(message.getUid());
								}

								long rootId = -1;
								long parentId = -1;

								if (oldMessageId == -1) {
									// This is a new message. Do the message
									// threading.
									ThreadInfo threadInfo = doMessageThreading(
											db, message);
									oldMessageId = threadInfo.msgId;
									rootId = threadInfo.rootId;
									parentId = threadInfo.parentId;
								}

								boolean isDraft = (message
										.getHeader(MailChat.IDENTITY_HEADER) != null);

								List<Part> attachments;
								String text;
								String html;
								if (isDraft) {
									// Don't modify the text/plain or text/html
									// part of our own
									// draft messages because this will cause
									// the values stored in
									// the identity header to be wrong.
									ViewableContainer container = MimeUtility
											.extractPartsFromDraft(message);

									text = container.text;
									html = container.html;
									attachments = container.attachments;
								} else {
									ViewableContainer container = MimeUtility
											.extractTextAndAttachments(
													mApplication, message);

									attachments = container.attachments;
									text = container.text;
									html = HtmlConverter
											.convertEmoji2Img(container.html);
								}

								String preview = Message
										.calculateContentPreview(text);

								try {
									ContentValues cv = new ContentValues();
									cv.put("uid", uid);
									cv.put("subject", message.getSubject());
									cv.put("sender_list",
											Address.pack(message.getFrom()));
									cv.put("date",
											message.getSentDate() == null ? System
													.currentTimeMillis()
													: message.getSentDate()
															.getTime());
									cv.put("flags",
											serializeFlags(message.getFlags()));
									cv.put("deleted",
											message.isSet(Flag.DELETED) ? 1 : 0);
									cv.put("read", message.isSet(Flag.SEEN) ? 1
											: 0);
									cv.put("flagged",
											message.isSet(Flag.FLAGGED) ? 1 : 0);
									cv.put("answered", message
											.isSet(Flag.ANSWERED) ? 1 : 0);
									cv.put("forwarded", message
											.isSet(Flag.FORWARDED) ? 1 : 0);
									cv.put("folder_id", mFolderId);
									cv.put("to_list", Address.pack(message
											.getRecipients(RecipientType.TO)));
									cv.put("cc_list", Address.pack(message
											.getRecipients(RecipientType.CC)));
									cv.put("bcc_list", Address.pack(message
											.getRecipients(RecipientType.BCC)));
									cv.put("html_content",
											html.length() > 0 ? html : null);
									cv.put("text_content",
											text.length() > 0 ? text : null);
									cv.put("preview",
											preview.length() > 0 ? preview
													: null);
									cv.put("reply_to_list",
											Address.pack(message.getReplyTo()));
									/*
									 * cv.put("attachment_count",
									 * attachments.size());
									 */
									// 存储非内联附件数目
									// Modified by LL
									// BEGIN
									cv.put("attachment_count",
											getAttachmentCount(message,
													attachments,
													html));
									// END

									cv.put("internal_date",
											message.getInternalDate() == null ? System
													.currentTimeMillis()
													: message.getInternalDate()
															.getTime());
									cv.put("mime_type", message.getMimeType());
									cv.put("empty", 0);

									String messageId = message.getMessageId();
									if (messageId != null && messageId.length() > 0) {
										cv.put("message_id", messageId);
									}

									if (!copy) {
    									String mailId = message.getMailId();
    									if (mailId != null && mailId.length() > 0) {
    									    cv.put("mail_id", mailId);
    									}
									}

									long msgId;
									if (oldMessageId == -1) {
										msgId = db.insert("messages", "uid", cv);

										// Create entry in 'threads' table
										cv.clear();
										cv.put("message_id", msgId);

										if (rootId != -1) {
											cv.put("root", rootId);
										}
										if (parentId != -1) {
											cv.put("parent", parentId);
										}

										db.insert("threads", null, cv);
									} else {
										db.update(
												"messages",
												cv,
												"id = ?",
												new String[] { Long
														.toString(oldMessageId) });
										msgId = oldMessageId;
									}

									for (Part attachment : attachments) {
										saveAttachment(msgId, attachment, copy);
									}
									saveHeaders(msgId, (MimeMessage) message);
									if (!message.isSendMessage()) {
										if (!message.isSet(Flag.X_PUSH_MAIL)) {
											boolean isTrash = (message
													.getFolder().getName().equals(mAccount.getSpamFolderName()));
											if (!isTrash) {
												saveContactsInfos(message);
											}
										}
									}else {
										saveContactsInfos(message);
									}
								} catch (Exception e) {
									throw new MessagingException(
											"Error appending message", e);
								}
							}
						} catch (MessagingException e) {
							throw new WrappedException(e);
						}
						return null;
					}
				});

				notifyChange();

				return uidMap;
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}
		}

		private void saveContactsInfos(final Message message)
				throws MessagingException {
			database.execute(true, new DbCallback<Void>() {
				@Override
				public Void doDbWork(final SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {

					try {
						// 解析并保存联系人信息
						parseAndSaveEmail(message.getFrom());
						parseAndSaveEmail(message
								.getRecipients(RecipientType.CC));
						parseAndSaveEmail(message
								.getRecipients(RecipientType.BCC));
						parseAndSaveEmail(message
								.getRecipients(RecipientType.TO));
					} catch (MessagingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;
				}
			});

		}

		public List<ContactAttribute> parseAndSaveEmail(Address[] addresses) {
			if (addresses == null) {
				return null;
			}
			List<ContactAttribute> emailList = new ArrayList<ContactAttribute>();
			for (int i = 0, count = addresses.length; i < count; i++) {
				Address address = addresses[i];
				String frontEmail = null;
				ContactAttribute contactAttribute = new ContactAttribute();
				String email = address.getAddress();
				if (email != null) {
					contactAttribute.setEmail(StringUtil.getEmailSuffix(email));

					String personal = address.getPersonal();
					if (personal != null) {
						personal = personal.replaceAll("\"", "\\\"");
					}

					int index = email.indexOf("@");
					if (index < 1) {
					    continue;
					} else {
					    frontEmail = email.substring(0, email.indexOf("@"));
					}

					if (!StringUtil.isEmpty(personal)) {
						if (personal.startsWith("'") && personal.endsWith("'")) {
							StringBuilder sb = new StringBuilder(personal)
									.deleteCharAt(personal.length() - 1)
									.deleteCharAt(0);
							contactAttribute.setNickName(sb.toString());
						} else {
							contactAttribute.setNickName(personal);
						}
					} else {
						contactAttribute.setNickName(frontEmail);
					}
					contactAttribute.setSpellName(HanziToPinyin
							.toPinyin(frontEmail));
					contactAttribute.setReceiveCount(1);
					contactAttribute.setDate(System.currentTimeMillis() + "");
					emailList.add(contactAttribute);
				}
			}

			try {
				persistContactsInfo(emailList);
			} catch (UnavailableStorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return emailList;
		}

		/**
		 * Update the given message in the LocalStore without first deleting the
		 * existing message (contrast with appendMessages). This method is used
		 * to store changes to the given message while updating attachments and
		 * not removing existing attachment data. TODO In the future this method
		 * should be combined with appendMessages since the Message contains
		 * enough data to decide what to do.
		 * 
		 * @param message
		 * @throws MessagingException
		 */
		public void updateMessage(final LocalMessage message)
				throws MessagingException {
			open(OPEN_MODE_RW);
			try {
				database.execute(false, new DbCallback<Void>() {
					@Override
					public Void doDbWork(final SQLiteDatabase db)
							throws WrappedException,
							UnavailableStorageException {
						try {
							message.buildMimeRepresentation();

							ViewableContainer container = MimeUtility
									.extractTextAndAttachments(mApplication,
											message);

							List<Part> attachments = container.attachments;

							boolean isDownloadedFull = true;
							for (Part attachment : attachments) {
								if (attachment.getBody() == null) {
									isDownloadedFull = false;
									break;
								}
							}
							if (isDownloadedFull) {
								if (message.isSet(Flag.X_DOWNLOADED_PARTIAL)) {
									message.setFlag(Flag.X_DOWNLOADED_FULL, true);
								}
							} else {
								message.setFlag(Flag.X_DOWNLOADED_FULL, false);
							}

							String text = container.text;
							String html = HtmlConverter
									.convertEmoji2Img(container.html);

							String preview = Message
									.calculateContentPreview(text);

							try {
								db.execSQL(
										"UPDATE messages SET "
												+ "uid = ?, mail_id = ?, subject = ?, sender_list = ?, date = ?, flags = ?, "
												+ "folder_id = ?, to_list = ?, cc_list = ?, bcc_list = ?, "
												+ "html_content = ?, text_content = ?, preview = ?, reply_to_list = ?, "
												+ "attachment_count = ?, read = ?, flagged = ?, answered = ?, forwarded = ? "
												+ "WHERE id = ?",
										new Object[] {
												message.getUid(),
												message.getMailId(),
												message.getSubject(),
												Address.pack(message.getFrom()),
												message.getSentDate() == null ? System
														.currentTimeMillis()
														: message.getSentDate()
																.getTime(),
												serializeFlags(message
														.getFlags()),
												mFolderId,
												Address.pack(message
														.getRecipients(RecipientType.TO)),
												Address.pack(message
														.getRecipients(RecipientType.CC)),
												Address.pack(message
														.getRecipients(RecipientType.BCC)),
												html.length() > 0 ? html : null,
												text.length() > 0 ? text : null,
												preview.length() > 0 ? preview
														: null,
												Address.pack(message
														.getReplyTo()),

												// attachments.size(),
												// 存储非内联附件数目
												message.mAttachmentCount,

												message.isSet(Flag.SEEN) ? 1
														: 0,
												message.isSet(Flag.FLAGGED) ? 1
														: 0,
												message.isSet(Flag.ANSWERED) ? 1
														: 0,
												message.isSet(Flag.FORWARDED) ? 1
														: 0, message.mId });

								for (int i = 0, count = attachments.size(); i < count; i++) {
									Part attachment = attachments.get(i);
									if (attachment.getBody() != null) {
									    saveAttachment(message.mId, attachment, false);
									}
								}
								saveHeaders(message.getId(), message);
							} catch (Exception e) {
								throw new MessagingException(
										"Error appending message", e);
							}
						} catch (MessagingException e) {
							throw new WrappedException(e);
						}
						return null;
					}
				});
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}

			notifyChange();
		}

		/**
		 * Save the headers of the given message. Note that the message is not
		 * necessarily a {@link LocalMessage} instance.
		 * 
		 * @param id
		 * @param message
		 * @throws cn.mailchat.mail.MessagingException
		 *             qxian
		 */
		private void saveHeaders(final long id, final MimeMessage message)
				throws MessagingException {
			database.execute(true, new DbCallback<Void>() {
				@Override
				public Void doDbWork(final SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {

					deleteHeaders(id);
					for (String name : message.getHeaderNames()) {
						String[] values = message.getHeader(name);
						for (String value : values) {
							ContentValues cv = new ContentValues();
							cv.put("message_id", id);
							cv.put("name", name);
							cv.put("value", value);
							db.insert("headers", "name", cv);
						}
					}

					// Remember that all headers for this message have been
					// saved, so it is
					// not necessary to download them again in case the user
					// wants to see all headers.
					List<Flag> appendedFlags = new ArrayList<Flag>();
					appendedFlags.addAll(Arrays.asList(message.getFlags()));
					appendedFlags.add(Flag.X_GOT_ALL_HEADERS);

					db.execSQL(
							"UPDATE messages " + "SET flags = ? "
									+ " WHERE id = ?",
							new Object[] {
									serializeFlags(appendedFlags
											.toArray(EMPTY_FLAG_ARRAY)), id });

					return null;
				}
			});
		}

		private void deleteHeaders(final long id)
				throws UnavailableStorageException {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(final SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					db.execSQL("DELETE FROM headers WHERE message_id = ?",
							new Object[] { id });
					return null;
				}
			});
		}

		/**
		 * @param messageId
		 * @param attachment
		 * @param saveAsNew
		 * @throws IOException
		 * @throws MessagingException
		 */
		private void saveAttachment(final long messageId,
				final Part attachment, final boolean saveAsNew)
				throws IOException, MessagingException {
			try {
				database.execute(true, new DbCallback<Void>() {
					@Override
					public Void doDbWork(final SQLiteDatabase db)
							throws WrappedException,
							UnavailableStorageException {
						try {
							long attachmentId = -1;
							Uri contentUri = null;
							int size = -1;
							File tempAttachmentFile = null;
							boolean isMessageIdDiff = false;

							if ((!saveAsNew)
									&& (attachment instanceof LocalAttachmentBodyPart)) {
							    LocalAttachmentBodyPart localAtt = (LocalAttachmentBodyPart) attachment;
							    attachmentId = localAtt.getAttachmentId();
							    if (localAtt.getMessageId() != messageId) {
								    isMessageIdDiff = true;
								}
							}

							final File attachmentDirectory = StorageManager
									.getInstance(mApplication)
									.getAttachmentDirectory(uUid,
											database.getStorageProviderId());
							if (attachment.getBody() != null) {
								Body body = attachment.getBody();
								if (!isMessageIdDiff && body instanceof LocalAttachmentBody) {
									contentUri = ((LocalAttachmentBody) body)
											.getContentUri();
								} else if (body instanceof Message) {
									// It's a message, so use Message.writeTo()
									// to output the
									// message including all children.
									Message message = (Message) body;
									tempAttachmentFile = File.createTempFile(
											"att", null, attachmentDirectory);
									FileOutputStream out = new FileOutputStream(
											tempAttachmentFile);
									try {
										message.writeTo(out);
									} finally {
										out.close();
									}
									size = (int) (tempAttachmentFile.length() & 0x7FFFFFFFL);
								} else {
									/*
									 * If the attachment has a body we're
									 * expected to save it into the local store
									 * so we copy the data into a cached
									 * attachment file.
									 */
									InputStream in = attachment.getBody()
											.getInputStream();
									try {
										tempAttachmentFile = File
												.createTempFile("att", null,
														attachmentDirectory);
										FileOutputStream out = new FileOutputStream(
												tempAttachmentFile);
										try {
											size = IOUtils.copy(in, out);
										} finally {
											out.close();
										}
									} finally {
										try {
											in.close();
										} catch (Throwable ignore) {
										}
									}
								}
							}

							if (size == -1) {
								/*
								 * If the attachment is not yet downloaded see
								 * if we can pull a size off the
								 * Content-Disposition.
								 */
								String disposition = attachment
										.getDisposition();
								if (disposition != null) {
									String sizeParam = MimeUtility
											.getHeaderParameter(disposition,
													"size");
									if (sizeParam != null) {
										try {
											size = Integer.parseInt(sizeParam);
										} catch (NumberFormatException e) { /* Ignore */
										}
									}
								}
							}
							if (size == -1) {
							    if (attachment instanceof ImapMessage) {
							        ImapMessage message = (ImapMessage) attachment;
							        size = message.getSize();
							    } else {
							        size = 0;
							    }
							}

//							String storeData = Utility.combine(
//									attachment
//											.getHeader(MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA),
//									',');
						    String storeData = null;
						    if (attachment instanceof ImapMessage) {
						        storeData = "TEXT";
						    } else {
						        storeData = Utility.combine(
						                attachment.getHeader(MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA),
						                ',');
						    }

							String name = MimeUtility.getHeaderParameter(
									attachment.getContentType(), "name");
							String contentId = MimeUtility.getHeaderParameter(
									attachment.getContentId(), null);
							
							String contentTransferEncoding = MimeUtility.getFirstHeader(
							        attachment,
							        MimeHeader.HEADER_CONTENT_TRANSFER_ENCODING);

							String contentDisposition = attachment.getDisposition();
							String dispositionType = contentDisposition;

							if (dispositionType != null) {
								int pos = dispositionType.indexOf(';');
								if (pos != -1) {
									// extract the disposition-type,
									// "attachment", "inline" or extension-token
									// (see the RFC 2183)
									dispositionType = dispositionType
											.substring(0, pos);
								}
							}

							if (name == null && contentDisposition != null) {
								name = MimeUtility.getHeaderParameter(
										contentDisposition, "filename");
							}
							if (attachmentId == -1) {
								ContentValues cv = new ContentValues();
								cv.put("message_id", messageId);
								cv.put("content_uri",
										contentUri != null ? contentUri
												.toString() : null);
								cv.put("store_data", storeData);
								cv.put("size", size);
								cv.put("name", name);
								cv.put("mime_type", attachment.getMimeType());
								cv.put("content_id", contentId);
								cv.put("content_disposition", dispositionType);
								cv.put("content_transfer_encoding", contentTransferEncoding);

								attachmentId = db.insert("attachments",
										"message_id", cv);
							} else {
								ContentValues cv = new ContentValues();
								cv.put("content_uri",
										contentUri != null ? contentUri
												.toString() : null);
								cv.put("size", size);
								db.update("attachments", cv, "id = ?",
										new String[] { Long
												.toString(attachmentId) });
							}

							if (attachmentId != -1
									&& tempAttachmentFile != null) {
								File attachmentFile = new File(
										attachmentDirectory, Long
												.toString(attachmentId));
								tempAttachmentFile.renameTo(attachmentFile);
								contentUri = AttachmentProvider
										.getAttachmentUri(mAccount,
												attachmentId);
								if (MimeUtil.isMessage(attachment.getMimeType())) {
									attachment
											.setBody(new LocalAttachmentMessageBody(
													contentUri, mApplication));
								} else {
									attachment.setBody(new LocalAttachmentBody(
											contentUri, mApplication));
								}
								ContentValues cv = new ContentValues();
								cv.put("content_uri",
										contentUri != null ? contentUri
												.toString() : null);
								db.update("attachments", cv, "id = ?",
										new String[] { Long
												.toString(attachmentId) });
							}

							/* The message has attachment with Content-ID */
							// 存储附件时不更新正文中内联附件src地址
							// TODO 注意检查此修改是否影响其他功能
							// Modified by LL
							/*
							if (contentId != null && contentUri != null) {
								Cursor cursor = db.query(
										"messages",
										new String[] { "html_content" },
										"id = ?",
										new String[] { Long.toString(messageId) },
										null, null, null);
								try {
									if (cursor.moveToNext()) {
										String htmlContent = cursor
												.getString(0);

										if (htmlContent != null) {
											String newHtmlContent = htmlContent.replaceAll(
													Pattern.quote("cid:"
															+ contentId),
													contentUri.toString());

											ContentValues cv = new ContentValues();
											cv.put("html_content",
													newHtmlContent);
											db.update(
													"messages",
													cv,
													"id = ?",
													new String[] { Long
															.toString(messageId) });
										}
									}
								} finally {
									Utility.closeQuietly(cursor);
								}
							}
							*/

							if (attachmentId != -1
									&& attachment instanceof LocalAttachmentBodyPart) {
								((LocalAttachmentBodyPart) attachment)
										.setAttachmentId(attachmentId);
							}
							return null;
						} catch (MessagingException e) {
							throw new WrappedException(e);
						} catch (IOException e) {
							throw new WrappedException(e);
						}
					}
				});
			} catch (WrappedException e) {
				final Throwable cause = e.getCause();
				if (cause instanceof IOException) {
					throw (IOException) cause;
				}

				throw new MessagingException(TAG, e.getCause());
			}
		}

		/**
		 * Changes the stored uid of the given message (using it's internal id
		 * as a key) to the uid in the message.
		 * 
		 * @param message
		 * @throws cn.mailchat.mail.MessagingException
		 */
		public void changeUid(final LocalMessage message)
				throws MessagingException {
			open(OPEN_MODE_RW);
			final ContentValues cv = new ContentValues();
			cv.put("uid", message.getUid());
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(final SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					db.update("messages", cv, "id = ?",
							new String[] { Long.toString(message.mId) });
					return null;
				}
			});

			// TODO: remove this once the UI code exclusively uses the database
			// id
			notifyChange();
		}

		@Override
		public void setFlags(final Message[] messages, final Flag[] flags,
				final boolean value) throws MessagingException {
			open(OPEN_MODE_RW);

			// Use one transaction to set all flags
			try {
				database.execute(true, new DbCallback<Void>() {
					@Override
					public Void doDbWork(final SQLiteDatabase db)
							throws WrappedException,
							UnavailableStorageException {

						for (Message message : messages) {
							try {
								message.setFlags(flags, value);
							} catch (MessagingException e) {
								Log.e(MailChat.LOG_TAG,
										"Something went wrong while setting flag",
										e);
							}
						}

						return null;
					}
				});
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}
		}

		@Override
		public void setFlags(Flag[] flags, boolean value)
				throws MessagingException {
			open(OPEN_MODE_RW);
			for (Message message : getMessages(null)) {
				message.setFlags(flags, value);
			}
		}

		@Override
		public String getUidFromMessageId(Message message)
				throws MessagingException {
			throw new MessagingException(
					"Cannot call getUidFromMessageId on LocalFolder");
		}

		public void clearMessagesOlderThan(long cutoff)
				throws MessagingException {
			open(OPEN_MODE_RO);

			Message[] messages = LocalStore.this
					.getMessages(
							null,
							this,
							"SELECT "
									+ GET_MESSAGES_COLS
									+ "FROM messages "
									+ "LEFT JOIN threads ON (threads.message_id = messages.id) "
									+ "WHERE (empty IS NULL OR empty != 1) AND "
									+ "(folder_id = ? and date < ?)",
							new String[] { Long.toString(mFolderId),
									Long.toString(cutoff) });

			for (Message message : messages) {
				message.destroy();
			}

			notifyChange();
		}

		public void clearAllMessages() throws MessagingException {
			final String[] folderIdArg = new String[] { Long
					.toString(mFolderId) };

			open(OPEN_MODE_RO);

			try {
				database.execute(false, new DbCallback<Void>() {
					@Override
					public Void doDbWork(final SQLiteDatabase db)
							throws WrappedException {
						try {
							// Get UIDs for all messages to delete
							Cursor cursor = db
									.query("messages",
											new String[] { "uid" },
											"folder_id = ? AND (empty IS NULL OR empty != 1)",
											folderIdArg, null, null, null);

							try {
								// Delete attachments of these messages
								while (cursor.moveToNext()) {
									deleteAttachments(cursor.getString(0));
								}
							} finally {
								cursor.close();
							}

							// Delete entries in 'threads' and 'messages'
							db.execSQL(
									"DELETE FROM threads WHERE message_id IN "
											+ "(SELECT id FROM messages WHERE folder_id = ?)",
									folderIdArg);
							db.execSQL(
									"DELETE FROM messages WHERE folder_id = ?",
									folderIdArg);

							return null;
						} catch (MessagingException e) {
							throw new WrappedException(e);
						}
					}
				});
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}

			notifyChange();

			setPushState(null);
			setLastPush(0);
			setLastChecked(0);
			setVisibleLimit(mAccount.getDisplayCount());
		}

		@Override
		public void delete(final boolean recurse) throws MessagingException {
			try {
				database.execute(false, new DbCallback<Void>() {
					@Override
					public Void doDbWork(final SQLiteDatabase db)
							throws WrappedException,
							UnavailableStorageException {
						try {
							// We need to open the folder first to make sure
							// we've got it's id
							open(OPEN_MODE_RO);
							Message[] messages = getMessages(null);
							for (Message message : messages) {
								deleteAttachments(message.getUid());
							}
						} catch (MessagingException e) {
							throw new WrappedException(e);
						}
						db.execSQL("DELETE FROM folders WHERE id = ?",
								new Object[] { Long.toString(mFolderId), });
						return null;
					}
				});
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}
		}
		@Override
		public boolean equals(Object o) {
			if (o instanceof LocalFolder) {
				return ((LocalFolder) o).mName.equals(mName);
			}
			return super.equals(o);
		}

		@Override
		public int hashCode() {
			return mName.hashCode();
		}

		private void deleteAttachments(final long messageId)
				throws MessagingException {
			open(OPEN_MODE_RW);
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(final SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					Cursor attachmentsCursor = null;
					try {
						String accountUuid = mAccount.getUuid();
						Context context = mApplication;

						// Get attachment IDs
						String[] whereArgs = new String[] { Long
								.toString(messageId) };
						attachmentsCursor = db.query("attachments",
								new String[] { "id" }, "message_id = ?",
								whereArgs, null, null, null);

						final File attachmentDirectory = StorageManager
								.getInstance(mApplication)
								.getAttachmentDirectory(uUid,
										database.getStorageProviderId());

						while (attachmentsCursor.moveToNext()) {
							String attachmentId = Long
									.toString(attachmentsCursor.getLong(0));
							try {
								// Delete stored attachment
								File file = new File(attachmentDirectory,
										attachmentId);
								if (file.exists()) {
									file.delete();
								}

								// Delete thumbnail file
								AttachmentProvider.deleteThumbnail(context,
										accountUuid, attachmentId);
							} catch (Exception e) { /* ignore */
							}
						}

						// Delete attachment metadata from the database
						db.delete("attachments", "message_id = ?", whereArgs);
					} finally {
						Utility.closeQuietly(attachmentsCursor);
					}
					return null;
				}
			});
		}

		private void deleteAttachments(final String uid)
				throws MessagingException {
			open(OPEN_MODE_RW);
			try {
				database.execute(false, new DbCallback<Void>() {
					@Override
					public Void doDbWork(final SQLiteDatabase db)
							throws WrappedException,
							UnavailableStorageException {
						Cursor messagesCursor = null;
						try {
							messagesCursor = db.query("messages",
									new String[] { "id" },
									"folder_id = ? AND uid = ?", new String[] {
											Long.toString(mFolderId), uid },
									null, null, null);
							while (messagesCursor.moveToNext()) {
								long messageId = messagesCursor.getLong(0);
								deleteAttachments(messageId);

							}
						} catch (MessagingException e) {
							throw new WrappedException(e);
						} finally {
							Utility.closeQuietly(messagesCursor);
						}
						return null;
					}
				});
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}
		}

		@Override
		public boolean isInTopGroup() {
			return mInTopGroup;
		}
		@Override
		public boolean isAllowPush() {
			return mIsAllowPush;
		}
		@Override
		public boolean isCustomFolder() {
			return mIsCustomFolder;
		}
		public void setInTopGroup(boolean inTopGroup) throws MessagingException {
			mInTopGroup = inTopGroup;
			updateFolderColumn("top_group", mInTopGroup ? 1 : 0);
		}

		public Integer getLastUid() {
			return mLastUid;
		}

		/**
		 * <p>
		 * Fetches the most recent <b>numeric</b> UID value in this folder. This
		 * is used by
		 * {@link cn.mailchat.controller.MessagingController#shouldNotifyForMessage}
		 * to see if messages being fetched are new and unread. Messages are
		 * "new" if they have a UID higher than the most recent UID prior to
		 * synchronization.
		 * </p>
		 * 
		 * <p>
		 * This only works for protocols with numeric UIDs (like IMAP). For
		 * protocols with alphanumeric UIDs (like POP), this method quietly
		 * fails and shouldNotifyForMessage() will always notify for unread
		 * messages.
		 * </p>
		 * 
		 * <p>
		 * Once Issue 1072 has been fixed, this method and
		 * shouldNotifyForMessage() should be updated to use internal dates
		 * rather than UIDs to determine new-ness. While this doesn't solve
		 * things for POP (which doesn't have internal dates), we can likely use
		 * this as a framework to examine send date in lieu of internal date.
		 * </p>
		 * 
		 * @throws MessagingException
		 */
		public void updateLastUid() throws MessagingException {
			Integer lastUid = database.execute(false,
					new DbCallback<Integer>() {
						@Override
						public Integer doDbWork(final SQLiteDatabase db) {
							Cursor cursor = null;
							try {
								open(OPEN_MODE_RO);
								cursor = db
										.rawQuery(
												"SELECT MAX(uid) FROM messages WHERE folder_id=?",
												new String[] { Long
														.toString(mFolderId) });
								if (cursor.getCount() > 0) {
									cursor.moveToFirst();
									return cursor.getInt(0);
								}
							} catch (Exception e) {
								Log.e(MailChat.LOG_TAG,
										"Unable to updateLastUid: ", e);
							} finally {
								Utility.closeQuietly(cursor);
							}
							return null;
						}
					});
			if (MailChat.DEBUG)
				Log.d(MailChat.LOG_TAG, "Updated last UID for folder " + mName
						+ " to " + lastUid);
			mLastUid = lastUid;
		}

		public Long getOldestMessageDate() throws MessagingException {
			return database.execute(false, new DbCallback<Long>() {
				@Override
				public Long doDbWork(final SQLiteDatabase db) {
					Cursor cursor = null;
					try {
						open(OPEN_MODE_RO);
						cursor = db
								.rawQuery(
										"SELECT MIN(date) FROM messages WHERE folder_id=?",
										new String[] { Long.toString(mFolderId) });
						if (cursor.getCount() > 0) {
							cursor.moveToFirst();
							return cursor.getLong(0);
						}
					} catch (Exception e) {
						Log.e(MailChat.LOG_TAG,
								"Unable to fetch oldest message date: ", e);
					} finally {
						Utility.closeQuietly(cursor);
					}
					return null;
				}
			});
		}

		private ThreadInfo doMessageThreading(SQLiteDatabase db, Message message)
				throws MessagingException {
			long rootId = -1;
			long parentId = -1;

			String messageId = message.getMessageId();

			// If there's already an empty message in the database, update that
			ThreadInfo msgThreadInfo = getThreadInfo(db, messageId, true);

			// Get the message IDs from the "References" header line
			String[] referencesArray = message.getHeader("References");
			List<String> messageIds = null;
			if (referencesArray != null && referencesArray.length > 0) {
				messageIds = Utility.extractMessageIds(referencesArray[0]);
			}

			// Append the first message ID from the "In-Reply-To" header line
			String[] inReplyToArray = message.getHeader("In-Reply-To");
			String inReplyTo = null;
			if (inReplyToArray != null && inReplyToArray.length > 0) {
				inReplyTo = Utility.extractMessageId(inReplyToArray[0]);
				if (inReplyTo != null) {
					if (messageIds == null) {
						messageIds = new ArrayList<String>(1);
						messageIds.add(inReplyTo);
					} else if (!messageIds.contains(inReplyTo)) {
						messageIds.add(inReplyTo);
					}
				}
			}

			if (messageIds == null) {
				// This is not a reply, nothing to do for us.
				return (msgThreadInfo != null) ? msgThreadInfo
						: new ThreadInfo(-1, -1, messageId, -1, -1);
			}

			for (String reference : messageIds) {
				ThreadInfo threadInfo = getThreadInfo(db, reference, false);

				if (threadInfo == null) {
					// Create placeholder message in 'messages' table
					ContentValues cv = new ContentValues();
					cv.put("message_id", reference);
					cv.put("folder_id", mFolderId);
					cv.put("empty", 1);

					long newMsgId = db.insert("messages", null, cv);

					// Create entry in 'threads' table
					cv.clear();
					cv.put("message_id", newMsgId);
					if (rootId != -1) {
						cv.put("root", rootId);
					}
					if (parentId != -1) {
						cv.put("parent", parentId);
					}

					parentId = db.insert("threads", null, cv);
					if (rootId == -1) {
						rootId = parentId;
					}
				} else {
					if (rootId != -1 && threadInfo.rootId == -1
							&& rootId != threadInfo.threadId) {
						// We found an existing root container that is not
						// the root of our current path (References).
						// Connect it to the current parent.

						// Let all children know who's the new root
						ContentValues cv = new ContentValues();
						cv.put("root", rootId);
						db.update("threads", cv, "root = ?",
								new String[] { Long
										.toString(threadInfo.threadId) });

						// Connect the message to the current parent
						cv.put("parent", parentId);
						db.update("threads", cv, "id = ?", new String[] { Long
								.toString(threadInfo.threadId) });
					} else {
						rootId = (threadInfo.rootId == -1) ? threadInfo.threadId
								: threadInfo.rootId;
					}
					parentId = threadInfo.threadId;
				}
			}

			// TODO: set in-reply-to "link" even if one already exists

			long threadId;
			long msgId;
			if (msgThreadInfo != null) {
				threadId = msgThreadInfo.threadId;
				msgId = msgThreadInfo.msgId;
			} else {
				threadId = -1;
				msgId = -1;
			}

			return new ThreadInfo(threadId, msgId, messageId, rootId, parentId);
		}

		public List<Message> extractNewMessages(final List<Message> messages)
				throws MessagingException {

			try {
				return database.execute(false, new DbCallback<List<Message>>() {
					@Override
					public List<Message> doDbWork(final SQLiteDatabase db)
							throws WrappedException {
						try {
							open(OPEN_MODE_RW);
						} catch (MessagingException e) {
							throw new WrappedException(e);
						}

						List<Message> result = new ArrayList<Message>();

						List<String> selectionArgs = new ArrayList<String>();
						Set<String> existingMessages = new HashSet<String>();
						int start = 0;

						while (start < messages.size()) {
							StringBuilder selection = new StringBuilder();

							selection.append("folder_id = ? AND UID IN (");
							selectionArgs.add(Long.toString(mFolderId));

							int count = Math.min(messages.size() - start,
									UID_CHECK_BATCH_SIZE);

							for (int i = start, end = start + count; i < end; i++) {
								if (i > start) {
									selection.append(",?");
								} else {
									selection.append("?");
								}

								selectionArgs.add(messages.get(i).getUid());
							}

							selection.append(")");

							Cursor cursor = db.query("messages",
									UID_CHECK_PROJECTION, selection.toString(),
									selectionArgs.toArray(EMPTY_STRING_ARRAY),
									null, null, null);

							try {
								while (cursor.moveToNext()) {
									String uid = cursor.getString(0);
									existingMessages.add(uid);
								}
							} finally {
								Utility.closeQuietly(cursor);
							}

							for (int i = start, end = start + count; i < end; i++) {
								Message message = messages.get(i);
								if (!existingMessages.contains(message.getUid())) {
									result.add(message);
								}
							}
							
							try {
							    synchronized(MailChat.remoteSearchList) {
                                    for (Message msg : getMessages(existingMessages.toArray(new String[]{}), null)) {
                                        MailChat.remoteSearchList.add(new SearchResult(msg.getUid(), SearchResult.Type.LOCAL));
                                        if (!msg.isSet(Flag.X_REMOTE_SEARCH_MAIL)) {
                                            msg.setFlag(Flag.X_REMOTE_SEARCH_MAIL, true);
                                        }
                                    }
							    }
                            } catch (MessagingException e) {
                                // DO NOTHING
                            }

							existingMessages.clear();
							selectionArgs.clear();
							start += count;
						}

						return result;
					}
				});
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}
		}
	}

	public static class LocalTextBody extends TextBody {
		/**
		 * This is an HTML-ified version of the message for display purposes.
		 */
		private String mBodyForDisplay;

		public LocalTextBody(String body) {
			super(body);
		}

		public LocalTextBody(String body, String bodyForDisplay) {
			super(body);
			this.mBodyForDisplay = bodyForDisplay;
		}

		public String getBodyForDisplay() {
			return mBodyForDisplay;
		}

		public void setBodyForDisplay(String mBodyForDisplay) {
			this.mBodyForDisplay = mBodyForDisplay;
		}

	}// LocalTextBody

	public class LocalMessage extends MimeMessage {
		private long mId;
		private int mAttachmentCount;
		private String mSubject;

		private String mPreview = "";

		private boolean mHeadersLoaded = false;
		private boolean mMessageDirty = false;

		private long mThreadId;
		private long mRootId;

		public LocalMessage() {
		}

		LocalMessage(String uid, Folder folder) {
			this.mUid = uid;
			this.mFolder = folder;
		}

		private void populateFromGetMessageCursor(Cursor cursor)
				throws MessagingException {
			final String subject = cursor.getString(0);
			this.setSubject(subject == null ? "" : subject);

			Address[] from = Address.unpack(cursor.getString(1));
			if (from.length > 0) {
				this.setFrom(from[0]);
			}
			this.setInternalSentDate(new Date(cursor.getLong(2)));
			this.setUid(cursor.getString(3));
			String flagList = cursor.getString(4);
			if (flagList != null && flagList.length() > 0) {
				String[] flags = flagList.split(",");

				for (String flag : flags) {
					try {
						this.setFlagInternal(Flag.valueOf(flag), true);
					}

					catch (Exception e) {
						if (!"X_BAD_FLAG".equals(flag)) {
							Log.w(MailChat.LOG_TAG, "Unable to parse flag "
									+ flag);
						}
					}
				}
			}
			this.mId = cursor.getLong(5);
			this.setRecipients(RecipientType.TO,
					Address.unpack(cursor.getString(6)));
			this.setRecipients(RecipientType.CC,
					Address.unpack(cursor.getString(7)));
			this.setRecipients(RecipientType.BCC,
					Address.unpack(cursor.getString(8)));
			this.setReplyTo(Address.unpack(cursor.getString(9)));
			this.setAttachmentCount(cursor.getInt(10));
			this.mAttachmentCount = cursor.getInt(10);
			this.setInternalDate(new Date(cursor.getLong(11)));
			this.setMessageId(cursor.getString(12));

			final String preview = cursor.getString(14);
			mPreview = (preview == null ? "" : preview);

			if (this.mFolder == null) {
				LocalFolder f = new LocalFolder(cursor.getInt(13));
				f.open(LocalFolder.OPEN_MODE_RW);
				this.mFolder = f;
			}

			mThreadId = (cursor.isNull(15)) ? -1 : cursor.getLong(15);
			mRootId = (cursor.isNull(16)) ? -1 : cursor.getLong(16);

			boolean deleted = (cursor.getInt(17) == 1);
			boolean read = (cursor.getInt(18) == 1);
			boolean flagged = (cursor.getInt(19) == 1);
			boolean answered = (cursor.getInt(20) == 1);
			boolean forwarded = (cursor.getInt(21) == 1);

			setFlagInternal(Flag.DELETED, deleted);
			setFlagInternal(Flag.SEEN, read);
			setFlagInternal(Flag.FLAGGED, flagged);
			setFlagInternal(Flag.ANSWERED, answered);
			setFlagInternal(Flag.FORWARDED, forwarded);

			this.setMailId(cursor.getString(22));
		}

		/**
		 * Fetch the message text for display. This always returns an HTML-ified
		 * version of the message, even if it was originally a text-only
		 * message.
		 * 
		 * @return HTML version of message for display purposes or null.
		 * @throws MessagingException
		 */
		public String getTextForDisplay() throws MessagingException {
			String text = null; // First try and fetch an HTML part.
			Part part = MimeUtility.findFirstPartByMimeType(this, "text/html");
			if (part == null) {
				// If that fails, try and get a text part.
				part = MimeUtility.findFirstPartByMimeType(this, "text/plain");
				if (part != null
						&& part.getBody() instanceof LocalStore.LocalTextBody) {
					text = ((LocalStore.LocalTextBody) part.getBody())
							.getBodyForDisplay();
				}
			} else {
				// We successfully found an HTML part; do the necessary
				// character set decoding.
				text = MimeUtility.getTextFromPart(part);
			}
			return text;
		}

		/*
		 * Custom version of writeTo that updates the MIME message based on
		 * localMessage changes.
		 */

		@Override
		public void writeTo(OutputStream out) throws IOException,
				MessagingException {
			if (mMessageDirty)
				buildMimeRepresentation();
			super.writeTo(out);
		}

		private void buildMimeRepresentation() throws MessagingException {
			if (!mMessageDirty) {
				return;
			}

			super.setSubject(mSubject);
			if (this.mFrom != null && this.mFrom.length > 0) {
				super.setFrom(this.mFrom[0]);
			}

			super.setReplyTo(mReplyTo);
			super.setSentDate(this.getSentDate());
			super.setRecipients(RecipientType.TO, mTo);
			super.setRecipients(RecipientType.CC, mCc);
			super.setRecipients(RecipientType.BCC, mBcc);
			if (mMessageId != null)
				super.setMessageId(mMessageId);

			mMessageDirty = false;
		}

		@Override
		public String getPreview() {
			return mPreview;
		}

		@Override
		public String getSubject() {
			return mSubject;
		}

		@Override
		public void setSubject(String subject) throws MessagingException {
			mSubject = subject;
			mMessageDirty = true;
		}

		@Override
		public void setMessageId(String messageId) {
			mMessageId = messageId;
			mMessageDirty = true;
		}

		@Override
		public boolean hasAttachments() {
			return (mAttachmentCount > 0);
		}

		public int getAttachmentCount() {
			return mAttachmentCount;
		}

		@Override
		public void setFrom(Address from) throws MessagingException {
			this.mFrom = new Address[] { from };
			mMessageDirty = true;
		}

		@Override
		public void setReplyTo(Address[] replyTo) throws MessagingException {
			if (replyTo == null || replyTo.length == 0) {
				mReplyTo = null;
			} else {
				mReplyTo = replyTo;
			}
			mMessageDirty = true;
		}

		/*
		 * For performance reasons, we add headers instead of setting them (see
		 * super implementation) which removes (expensive) them before adding
		 * them
		 */
		@Override
		public void setRecipients(RecipientType type, Address[] addresses)
				throws MessagingException {
			if (type == RecipientType.TO) {
				if (addresses == null || addresses.length == 0) {
					this.mTo = null;
				} else {
					this.mTo = addresses;
				}
			} else if (type == RecipientType.CC) {
				if (addresses == null || addresses.length == 0) {
					this.mCc = null;
				} else {
					this.mCc = addresses;
				}
			} else if (type == RecipientType.BCC) {
				if (addresses == null || addresses.length == 0) {
					this.mBcc = null;
				} else {
					this.mBcc = addresses;
				}
			} else {
				throw new MessagingException("Unrecognized recipient type.");
			}
			mMessageDirty = true;
		}

		public void setFlagInternal(Flag flag, boolean set)
				throws MessagingException {
			super.setFlag(flag, set);
		}

		@Override
		public long getId() {
			return mId;
		}

		@Override
		public void setFlag(final Flag flag, final boolean set)
				throws MessagingException {

			try {
				database.execute(true, new DbCallback<Void>() {
					@Override
					public Void doDbWork(final SQLiteDatabase db)
							throws WrappedException,
							UnavailableStorageException {
						try {
							if (flag == Flag.DELETED && set) {
								delete();
							}

							LocalMessage.super.setFlag(flag, set);
						} catch (MessagingException e) {
							throw new WrappedException(e);
						}
						/*
						 * Set the flags on the message.
						 */
						ContentValues cv = new ContentValues();
						cv.put("flags", serializeFlags(getFlags()));
						cv.put("read", isSet(Flag.SEEN) ? 1 : 0);
						cv.put("flagged", isSet(Flag.FLAGGED) ? 1 : 0);
						cv.put("answered", isSet(Flag.ANSWERED) ? 1 : 0);
						cv.put("forwarded", isSet(Flag.FORWARDED) ? 1 : 0);

						db.update("messages", cv, "id = ?",
								new String[] { Long.toString(mId) });

						return null;
					}
				});
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}

			notifyChange();
		}

		/*
		 * If a message is being marked as deleted we want to clear out it's
		 * content and attachments as well. Delete will not actually remove the
		 * row since we need to retain the uid for synchronization purposes.
		 */
		private void delete() throws MessagingException

		{
			/*
			 * Delete all of the message's content to save space.
			 */
			try {
				database.execute(true, new DbCallback<Void>() {
					@Override
					public Void doDbWork(final SQLiteDatabase db)
							throws WrappedException,
							UnavailableStorageException {
						String[] idArg = new String[] { Long.toString(mId) };

						ContentValues cv = new ContentValues();
						cv.put("deleted", 1);
						cv.put("empty", 1);
						cv.putNull("subject");
						cv.putNull("sender_list");
						cv.putNull("date");
						cv.putNull("to_list");
						cv.putNull("cc_list");
						cv.putNull("bcc_list");
						cv.putNull("preview");
						cv.putNull("html_content");
						cv.putNull("text_content");
						cv.putNull("reply_to_list");

						db.update("messages", cv, "id = ?", idArg);

						/*
						 * Delete all of the message's attachments to save
						 * space. We do this explicit deletion here because
						 * we're not deleting the record in messages, which
						 * means our ON DELETE trigger for messages won't
						 * cascade
						 */
						try {
							((LocalFolder) mFolder).deleteAttachments(mId);
						} catch (MessagingException e) {
							throw new WrappedException(e);
						}

						db.delete("attachments", "message_id = ?", idArg);
						return null;
					}
				});
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}
			((LocalFolder) mFolder).deleteHeaders(mId);

			notifyChange();
		}

		/*
		 * Completely remove a message from the local database
		 * 
		 * TODO: document how this updates the thread structure
		 */
		@Override
		public void destroy() throws MessagingException {
			try {
				database.execute(true, new DbCallback<Void>() {
					@Override
					public Void doDbWork(final SQLiteDatabase db)
							throws WrappedException,
							UnavailableStorageException {
						try {
							LocalFolder localFolder = (LocalFolder) mFolder;

							localFolder.deleteAttachments(mId);

							if (hasThreadChildren(db, mId)) {
								// This message has children in the thread
								// structure so we need to
								// make it an empty message.
								ContentValues cv = new ContentValues();
								cv.put("id", mId);
								cv.put("folder_id", localFolder.getId());
								cv.put("deleted", 0);
								cv.put("message_id", getMessageId());
								cv.put("empty", 1);

								db.replace("messages", null, cv);

								// Nothing else to do
								return null;
							}

							// Get the message ID of the parent message if it's
							// empty
							long currentId = getEmptyThreadParent(db, mId);

							// Delete the placeholder message
							deleteMessageRow(db, mId);

							/*
							 * Walk the thread tree to delete all empty parents
							 * without children
							 */

							while (currentId != -1) {
								if (hasThreadChildren(db, currentId)) {
									// We made sure there are no empty leaf
									// nodes and can stop now.
									break;
								}

								// Get ID of the (empty) parent for the next
								// iteration
								long newId = getEmptyThreadParent(db, currentId);

								// Delete the empty message
								deleteMessageRow(db, currentId);

								currentId = newId;
							}

						} catch (MessagingException e) {
							throw new WrappedException(e);
						}
						return null;
					}
				});
			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}

			notifyChange();
		}

		/**
		 * Get ID of the the given message's parent if the parent is an empty
		 * message.
		 * 
		 * @param db
		 *            {@link SQLiteDatabase} instance to access the database.
		 * @param messageId
		 *            The database ID of the message to get the parent for.
		 * 
		 * @return Message ID of the parent message if there exists a parent and
		 *         it is empty. Otherwise {@code -1}.
		 */
		private long getEmptyThreadParent(SQLiteDatabase db, long messageId) {
			Cursor cursor = db.rawQuery("SELECT m.id " + "FROM threads t1 "
					+ "JOIN threads t2 ON (t1.parent = t2.id) "
					+ "LEFT JOIN messages m ON (t2.message_id = m.id) "
					+ "WHERE t1.message_id = ? AND m.empty = 1",
					new String[] { Long.toString(messageId) });

			try {
				return (cursor.moveToFirst() && !cursor.isNull(0)) ? cursor
						.getLong(0) : -1;
			} finally {
				cursor.close();
			}
		}

		/**
		 * Check whether or not a message has child messages in the thread
		 * structure.
		 * 
		 * @param db
		 *            {@link SQLiteDatabase} instance to access the database.
		 * @param messageId
		 *            The database ID of the message to get the children for.
		 * 
		 * @return {@code true} if the message has children. {@code false}
		 *         otherwise.
		 */
		private boolean hasThreadChildren(SQLiteDatabase db, long messageId) {
			Cursor cursor = db.rawQuery("SELECT COUNT(t2.id) "
					+ "FROM threads t1 "
					+ "JOIN threads t2 ON (t2.parent = t1.id) "
					+ "WHERE t1.message_id = ?",
					new String[] { Long.toString(messageId) });

			try {
				return (cursor.moveToFirst() && !cursor.isNull(0) && cursor
						.getLong(0) > 0L);
			} finally {
				cursor.close();
			}
		}

		/**
		 * Delete a message from the 'messages' and 'threads' tables.
		 * 
		 * @param db
		 *            {@link SQLiteDatabase} instance to access the database.
		 * @param messageId
		 *            The database ID of the message to delete.
		 */
		private void deleteMessageRow(SQLiteDatabase db, long messageId) {
			String[] idArg = { Long.toString(messageId) };

			// Delete the message
			db.delete("messages", "id = ?", idArg);

			// Delete row in 'threads' table
			// TODO: create trigger for 'messages' table to get rid of the row
			// in 'threads' table
			db.delete("threads", "message_id = ?", idArg);
		}

		private void loadHeaders() throws UnavailableStorageException {
			ArrayList<LocalMessage> messages = new ArrayList<LocalMessage>();
			messages.add(this);
			mHeadersLoaded = true; // set true before calling populate headers
									// to stop recursion
			((LocalFolder) mFolder).populateHeaders(messages);

		}

		@Override
		public void addHeader(String name, String value)
				throws UnavailableStorageException {
			if (!mHeadersLoaded)
				loadHeaders();
			super.addHeader(name, value);
		}

		@Override
		public void setHeader(String name, String value)
				throws UnavailableStorageException {
			if (!mHeadersLoaded)
				loadHeaders();
			super.setHeader(name, value);
		}

		@Override
		public String[] getHeader(String name)
				throws UnavailableStorageException {
			if (!mHeadersLoaded)
				loadHeaders();
			return super.getHeader(name);
		}

		@Override
		public void removeHeader(String name)
				throws UnavailableStorageException {
			if (!mHeadersLoaded)
				loadHeaders();
			super.removeHeader(name);
		}

		@Override
		public Set<String> getHeaderNames() throws UnavailableStorageException {
			if (!mHeadersLoaded)
				loadHeaders();
			return super.getHeaderNames();
		}

		@Override
		public LocalMessage clone() {
			LocalMessage message = new LocalMessage();
			super.copy(message);

			message.mId = mId;
			message.mAttachmentCount = mAttachmentCount;
			message.mSubject = mSubject;
			message.mPreview = mPreview;
			message.mHeadersLoaded = mHeadersLoaded;
			message.mMessageDirty = mMessageDirty;

			return message;
		}

		public long getThreadId() {
			return mThreadId;
		}

		public long getRootId() {
			return mRootId;
		}
	}

	public static class LocalAttachmentBodyPart extends MimeBodyPart {
		private long mAttachmentId = -1;
		private long mMessageId = -1;

		public LocalAttachmentBodyPart(Body body, long attachmentId, long messageId)
				throws MessagingException {
			super(body);
			mAttachmentId = attachmentId;
			mMessageId = messageId;
		}

		/**
		 * Returns the local attachment id of this body, or -1 if it is not
		 * stored.
		 * 
		 * @return
		 */
		public long getAttachmentId() {
			return mAttachmentId;
		}

		public void setAttachmentId(long attachmentId) {
			mAttachmentId = attachmentId;
		}

		public long getMessageId() {
            return mMessageId;
        }

        public void setMessageId(long messageId) {
            mMessageId = messageId;
        }

        @Override
		public String toString() {
			return "" + mAttachmentId;
		}
	}
	
	// Modified by LL for debug
	// BEGIN
	public static class RemoteAttachmentBodyPart extends MimeBodyPart {
		public final String ATT_IDENTITY_HEADER = "X_MailChat_Att_Identity";
		
		public RemoteAttachmentBodyPart(String attIdentity) throws MessagingException {
			super(null);
			addHeader(ATT_IDENTITY_HEADER, attIdentity);
		}
	}
	// END

	public abstract static class BinaryAttachmentBody implements Body {
		protected String mEncoding;

		@Override
		public abstract InputStream getInputStream() throws MessagingException;

		@Override
		public void writeTo(OutputStream out) throws IOException,
				MessagingException {
			InputStream in = getInputStream();
			try {
				boolean closeStream = false;
				if (MimeUtil.isBase64Encoding(mEncoding)) {
					out = new Base64OutputStream(out);
					closeStream = true;
				} else if (MimeUtil.isQuotedPrintableEncoded(mEncoding)) {
					out = new QuotedPrintableOutputStream(out, false);
					closeStream = true;
				}

				try {
					IOUtils.copy(in, out);
				} finally {
					if (closeStream) {
						out.close();
					}
				}
			} finally {
				in.close();
			}
		}

		@Override
		public void setEncoding(String encoding) throws MessagingException {
			mEncoding = encoding;
		}

		public String getEncoding() {
			return mEncoding;
		}
	}

	public static class TempFileBody extends BinaryAttachmentBody {
		private final File mFile;

		public TempFileBody(String filename) {
			mFile = new File(filename);
		}

		@Override
		public InputStream getInputStream() throws MessagingException {
			try {
				return new FileInputStream(mFile);
			} catch (FileNotFoundException e) {
				return new ByteArrayInputStream(EMPTY_BYTE_ARRAY);
			}
		}
	}

	public static class LocalAttachmentBody extends BinaryAttachmentBody {
		private Application mApplication;
		private Uri mUri;

		public LocalAttachmentBody(Uri uri, Application application) {
			mApplication = application;
			mUri = uri;
		}

		@Override
		public InputStream getInputStream() throws MessagingException {
			try {
				return mApplication.getContentResolver().openInputStream(mUri);
			} catch (FileNotFoundException fnfe) {
				/*
				 * Since it's completely normal for us to try to serve up
				 * attachments that have been blown away, we just return an
				 * empty stream.
				 */
				return new ByteArrayInputStream(EMPTY_BYTE_ARRAY);
			}
		}

		public Uri getContentUri() {
			return mUri;
		}
	}

	/**
	 * A {@link LocalAttachmentBody} extension containing a message/rfc822 type
	 * body
	 * 
	 */
	public static class LocalAttachmentMessageBody extends LocalAttachmentBody
			implements CompositeBody {

		public LocalAttachmentMessageBody(Uri uri, Application application) {
			super(uri, application);
		}

		@Override
		public void writeTo(OutputStream out) throws IOException,
				MessagingException {
			AttachmentMessageBodyUtil.writeTo(this, out);
		}

		@Override
		public void setUsing7bitTransport() throws MessagingException {
			/*
			 * There's nothing to recurse into here, so there's nothing to do.
			 * The enclosing BodyPart already called
			 * setEncoding(MimeUtil.ENC_7BIT). Once writeTo() is called, the
			 * file with the rfc822 body will be opened for reading and will
			 * then be recursed.
			 */

		}

		@Override
		public void setEncoding(String encoding) throws MessagingException {
			if (!MimeUtil.ENC_7BIT.equalsIgnoreCase(encoding)
					&& !MimeUtil.ENC_8BIT.equalsIgnoreCase(encoding)) {
				throw new MessagingException(
						"Incompatible content-transfer-encoding applied to a CompositeBody");
			}
			mEncoding = encoding;
		}
	}

	public static class TempFileMessageBody extends TempFileBody implements
			CompositeBody {

		public TempFileMessageBody(String filename) {
			super(filename);
		}

		@Override
		public void writeTo(OutputStream out) throws IOException,
				MessagingException {
			AttachmentMessageBodyUtil.writeTo(this, out);
		}

		@Override
		public void setUsing7bitTransport() throws MessagingException {
			// see LocalAttachmentMessageBody.setUsing7bitTransport()
		}

		@Override
		public void setEncoding(String encoding) throws MessagingException {
			if (!MimeUtil.ENC_7BIT.equalsIgnoreCase(encoding)
					&& !MimeUtil.ENC_8BIT.equalsIgnoreCase(encoding)) {
				throw new MessagingException(
						"Incompatible content-transfer-encoding applied to a CompositeBody");
			}
			mEncoding = encoding;
		}
	}

	public static class AttachmentMessageBodyUtil {
		public static void writeTo(BinaryAttachmentBody body, OutputStream out)
				throws IOException, MessagingException {
			InputStream in = body.getInputStream();
			try {
				if (MimeUtil.ENC_7BIT.equalsIgnoreCase(body.getEncoding())) {
					/*
					 * If we knew the message was already 7bit clean, then it
					 * could be sent along without processing. But since we
					 * don't know, we recursively parse it.
					 */
					MimeMessage message = new MimeMessage(in, true);
					message.setUsing7bitTransport();
					message.writeTo(out);
				} else {
					IOUtils.copy(in, out);
				}
			} finally {
				in.close();
			}
		}
	}

	static class ThreadInfo {
		public final long threadId;
		public final long msgId;
		public final String messageId;
		public final long rootId;
		public final long parentId;

		public ThreadInfo(long threadId, long msgId, String messageId,
				long rootId, long parentId) {
			this.threadId = threadId;
			this.msgId = msgId;
			this.messageId = messageId;
			this.rootId = rootId;
			this.parentId = parentId;
		}
	}

	public LockableDatabase getDatabase() {
		return database;
	}

	private void notifyChange() {
		Uri uri = Uri.withAppendedPath(EmailProvider.CONTENT_URI, "account/"
				+ uUid + "/messages");
		mContentResolver.notifyChange(uri, null);
	}

	/**
	 * Split database operations with a large set of arguments into multiple SQL
	 * statements.
	 * 
	 * <p>
	 * At the time of this writing (2012-12-06) SQLite only supports around 1000
	 * arguments. That's why we have to split SQL statements with a large set of
	 * arguments into multiple SQL statements each working on a subset of the
	 * arguments.
	 * </p>
	 * 
	 * @param selectionCallback
	 *            Supplies the argument set and the code to query/update the
	 *            database.
	 * @param batchSize
	 *            The maximum size of the selection set in each SQL statement.
	 * 
	 * @throws MessagingException
	 */
	public void doBatchSetSelection(final BatchSetSelection selectionCallback,
			final int batchSize) throws MessagingException {

		final List<String> selectionArgs = new ArrayList<String>();
		int start = 0;

		while (start < selectionCallback.getListSize()) {
			final StringBuilder selection = new StringBuilder();

			selection.append(" IN (");

			int count = Math.min(selectionCallback.getListSize() - start,
					batchSize);

			for (int i = start, end = start + count; i < end; i++) {
				if (i > start) {
					selection.append(",?");
				} else {
					selection.append("?");
				}

				selectionArgs.add(selectionCallback.getListItem(i));
			}

			selection.append(")");

			try {
				database.execute(true, new DbCallback<Void>() {
					@Override
					public Void doDbWork(final SQLiteDatabase db)
							throws WrappedException,
							UnavailableStorageException {

						selectionCallback.doDbWork(db, selection.toString(),
								selectionArgs.toArray(EMPTY_STRING_ARRAY));

						return null;
					}
				});

				selectionCallback.postDbWork();

			} catch (WrappedException e) {
				throw new MessagingException(TAG, e.getCause());
			}

			selectionArgs.clear();
			start += count;
		}
	}

	/**
	 * Defines the behavior of
	 * {@link LocalStore#doBatchSetSelection(BatchSetSelection, int)}.
	 */
	public interface BatchSetSelection {
		/**
		 * @return The size of the argument list.
		 */
		int getListSize();

		/**
		 * Get a specific item of the argument list.
		 * 
		 * @param index
		 *            The index of the item.
		 * 
		 * @return Item at position {@code i} of the argument list.
		 */
		String getListItem(int index);

		/**
		 * Execute the SQL statement.
		 * 
		 * @param db
		 *            Use this {@link SQLiteDatabase} instance for your SQL
		 *            statement.
		 * @param selectionSet
		 *            A partial selection string containing place holders for
		 *            the argument list, e.g. {@code " IN (?,?,?)"} (starts with
		 *            a space).
		 * @param selectionArgs
		 *            The current subset of the argument list.
		 * @throws UnavailableStorageException
		 */
		void doDbWork(SQLiteDatabase db, String selectionSet,
				String[] selectionArgs) throws UnavailableStorageException;

		/**
		 * This will be executed after each invocation of
		 * {@link #doDbWork(SQLiteDatabase, String, String[])} (after the
		 * transaction has been committed).
		 */
		void postDbWork();
	}

	/**
	 * Change the state of a flag for a list of messages.
	 * 
	 * <p>
	 * The goal of this method is to be fast. Currently this means using as few
	 * SQL UPDATE statements as possible.
	 * 
	 * @param messageIds
	 *            A list of primary keys in the "messages" table.
	 * @param flag
	 *            The flag to change. This must be a flag with a separate column
	 *            in the database.
	 * @param newState
	 *            {@code true}, if the flag should be set. {@code false},
	 *            otherwise.
	 * 
	 * @throws MessagingException
	 */
	public void setFlag(final List<Long> messageIds, final Flag flag,
			final boolean newState) throws MessagingException {

		final ContentValues cv = new ContentValues();
		cv.put(getColumnNameForFlag(flag), newState);

		doBatchSetSelection(new BatchSetSelection() {

			@Override
			public int getListSize() {
				return messageIds.size();
			}

			@Override
			public String getListItem(int index) {
				return Long.toString(messageIds.get(index));
			}

			@Override
			public void doDbWork(SQLiteDatabase db, String selectionSet,
					String[] selectionArgs) throws UnavailableStorageException {

				db.update("messages", cv,
						"(empty IS NULL OR empty != 1) AND id" + selectionSet,
						selectionArgs);
			}

			@Override
			public void postDbWork() {
				notifyChange();
			}
		}, FLAG_UPDATE_BATCH_SIZE);
	}

	/**
	 * Change the state of a flag for a list of threads.
	 * 
	 * <p>
	 * The goal of this method is to be fast. Currently this means using as few
	 * SQL UPDATE statements as possible.
	 * 
	 * @param threadRootIds
	 *            A list of root thread IDs.
	 * @param flag
	 *            The flag to change. This must be a flag with a separate column
	 *            in the database.
	 * @param newState
	 *            {@code true}, if the flag should be set. {@code false},
	 *            otherwise.
	 * 
	 * @throws MessagingException
	 */
	public void setFlagForThreads(final List<Long> threadRootIds, Flag flag,
			final boolean newState) throws MessagingException {

		final String flagColumn = getColumnNameForFlag(flag);

		doBatchSetSelection(new BatchSetSelection() {

			@Override
			public int getListSize() {
				return threadRootIds.size();
			}

			@Override
			public String getListItem(int index) {
				return Long.toString(threadRootIds.get(index));
			}

			@Override
			public void doDbWork(SQLiteDatabase db, String selectionSet,
					String[] selectionArgs) throws UnavailableStorageException {

				db.execSQL(
						"UPDATE messages SET "
								+ flagColumn
								+ " = "
								+ ((newState) ? "1" : "0")
								+ " WHERE id IN ("
								+ "SELECT m.id FROM threads t "
								+ "LEFT JOIN messages m ON (t.message_id = m.id) "
								+ "WHERE (m.empty IS NULL OR m.empty != 1) AND m.deleted = 0 "
								+ "AND t.root" + selectionSet + ")",
						selectionArgs);
			}

			@Override
			public void postDbWork() {
				notifyChange();
			}
		}, THREAD_FLAG_UPDATE_BATCH_SIZE);
	}

	/**
	 * Get folder name and UID for the supplied messages.
	 * 
	 * @param messageIds
	 *            A list of primary keys in the "messages" table.
	 * @param threadedList
	 *            If this is {@code true}, {@code messageIds} contains the
	 *            thread IDs of the messages at the root of a thread. In that
	 *            case return UIDs for all messages in these threads. If this is
	 *            {@code false} only the UIDs for messages in {@code messageIds}
	 *            are returned.
	 * 
	 * @return The list of UIDs for the messages grouped by folder name.
	 * 
	 * @throws MessagingException
	 */
	public Map<String, List<String>> getFoldersAndUids(
			final List<Long> messageIds, final boolean threadedList)
			throws MessagingException {

		final Map<String, List<String>> folderMap = new HashMap<String, List<String>>();

		doBatchSetSelection(new BatchSetSelection() {

			@Override
			public int getListSize() {
				return messageIds.size();
			}

			@Override
			public String getListItem(int index) {
				return Long.toString(messageIds.get(index));
			}

			@Override
			public void doDbWork(SQLiteDatabase db, String selectionSet,
					String[] selectionArgs) throws UnavailableStorageException {

				if (threadedList) {
					String sql = "SELECT m.uid, f.name "
							+ "FROM threads t "
							+ "LEFT JOIN messages m ON (t.message_id = m.id) "
							+ "LEFT JOIN folders f ON (m.folder_id = f.id) "
							+ "WHERE (m.empty IS NULL OR m.empty != 1) AND m.deleted = 0 "
							+ "AND t.root" + selectionSet;

					getDataFromCursor(db.rawQuery(sql, selectionArgs));

				} else {
					String sql = "SELECT m.uid, f.name "
							+ "FROM messages m "
							+ "LEFT JOIN folders f ON (m.folder_id = f.id) "
							+ "WHERE (m.empty IS NULL OR m.empty != 1) AND m.id"
							+ selectionSet;

					getDataFromCursor(db.rawQuery(sql, selectionArgs));
				}
			}

			private void getDataFromCursor(Cursor cursor) {
				try {
					while (cursor.moveToNext()) {
						String uid = cursor.getString(0);
						String folderName = cursor.getString(1);

						List<String> uidList = folderMap.get(folderName);
						if (uidList == null) {
							uidList = new ArrayList<String>();
							folderMap.put(folderName, uidList);
						}

						uidList.add(uid);
					}
				} finally {
					cursor.close();
				}
			}

			@Override
			public void postDbWork() {
				notifyChange();

			}
		}, UID_CHECK_BATCH_SIZE);

		return folderMap;
	}

	/**
	 * persist single chat message
	 * 
	 * method name: persistDChatMessage function @Description: Parameters and
	 * return values description：
	 * 
	 * @param message
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-9-26 下午5:11:38 @Modified by：zhangyq
	 * @Description：
	 */
	public void persistDChatMessage(DChatMessage m)
			throws UnavailableStorageException {

		final ContentValues cv = new ContentValues();
		cv.put("f_message_uid", m.getUuid());
		cv.put("f_message_dchat_uid", m.getDchatUid());
		cv.put("f_to_email", m.getReceiverEmail());
		cv.put("f_from_email", m.getSenderEmail());
		cv.put("f_message_content", m.getMessageContent());
		cv.put("f_time", m.getTime());
		cv.put("f_messagetype", m.getMessageType().ordinal());
		cv.put("f_delete_flag", m.getDeleteflag());
		cv.put("f_message_state", m.getMessageState());
		cv.put("f_location_type", m.getLocationType());
		cv.put("f_lat", m.getLatitude());
		cv.put("f_lon", m.getLongitude());
		cv.put("f_address", m.getAddress());
		cv.put("f_location_name", m.getLocationName());
		cv.put(TbDchatMessage.F_OA_ID, m.getOAId());
		cv.put(TbDchatMessage.F_OA_FROM, m.getOAFrom());
		cv.put(TbDchatMessage.F_OA_SUBJECT, m.getOASubject());
		cv.put(TbDchatMessage.F_URL, m.getURL());
		cv.put(TbDchatMessage.F_READ_FLAG,m.getReadFlag());
		cv.put(TbDchatMessage.F_SERVER_MESSAGE_TYPE,m.getServerMessageType());
		cv.put(TbDchatMessage.F_MAIL_FROM_EMAIL,m.getMailFrom());
		cv.put(TbDchatMessage.F_MAIL_FROM_NICKNAME,m.getMailFromNickName());
		cv.put(TbDchatMessage.F_MAIL_SUBJECT,m.getMailSubject());
		cv.put(TbDchatMessage.F_MAIL_PREVIEW,m.getMailPreview());
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// db.insert("tb_d_chat_message", null, cv);
					insertOrIgnore("tb_d_chat_message", cv);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			new UnavailableStorageException(
					"persist single chat message fail.", e);
		}
	}

	/**
	 * persist single chat list boolean type is automatically converted to int.
	 * true --> 1. false --> 0.
	 * 
	 * method name: persistDChatList function @Description: Parameters and
	 * return values description：
	 * 
	 * @param dChat
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-9-28 上午10:26:16 @Modified by：zhangyq
	 * @Description：
	 */
	public void persistDChatList(DChat dc) throws UnavailableStorageException {

		final ContentValues cv = new ContentValues();
		cv.put("f_message_dchat_uid", dc.getUid());
		cv.put("f_is_dchat_alert", dc.isDChatAlert() ? 1 : 0);
		cv.put("f_sticked_date", dc.getStickedDate());
		cv.put("f_is_sticked", dc.isSticked() ? 1 : 0);
		cv.put("f_to_email", dc.getEmail());
		cv.put("f_last_message", dc.getLastMessage());
		if (dc.getLastMessageType() != null) {
			cv.put("f_last_message_type", dc.getLastMessageType().ordinal());
		}
		cv.put("f_unread_count", dc.getUnReadCount());
		cv.put("f_last_time", dc.getLastTime());
		cv.put("f_last_message_email", dc.getLastMessageEmail());
		cv.put("f_is_visibility", dc.isVisibility() ? 1 : 0);
		cv.put(Columns.TbDchat.F_IS_UNTREATED, dc.isUnTreated() ? 1 : 0);
		if(dc.getdChatType()!=null){
			cv.put("f_dchat_type", dc.getdChatType().ordinal());
		}
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// db.insert("tb_d_chatlist", null, cv);
					insertOrIgnore("tb_d_chatlist", cv);
					notifyChattingChange();
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			new UnavailableStorageException("persist single chat list fail.", e);
		}
	}

	/**
	 * persist single chat attachment
	 * 
	 * method name: persistDChatAttachment function @Description: Parameters and
	 * return values description：
	 * 
	 * @param dAttachment
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-9-28 下午4:19:52 @Modified by：zhangyq
	 * @Description：
	 */
	public void persistDChatAttachment(DAttachment dAttachment)
			throws UnavailableStorageException {

		final ContentValues cv = new ContentValues();
		cv.put("f_uid", dAttachment.getAttchmentId());
		cv.put("f_message_uid", dAttachment.getMessageUid());
		cv.put("f_name", dAttachment.getName());
		cv.put(TbDAttachments.F_FILE_ID, dAttachment.getFileid());
		cv.put("f_size", dAttachment.getSize());
		cv.put("f_mime_type", dAttachment.getContentType());
		cv.put("f_file_path", dAttachment.getFilePath());
		cv.put("f_voice_length", dAttachment.getVoiceLength());
		cv.put("f_read_flag", dAttachment.getReadFlag());
		cv.put("f_localPath_flag", dAttachment.getLocalPathFlag());
		cv.put("f_forward_flag", dAttachment.getForwardFlag());
		cv.put(TbDAttachments.F_WIDTH, dAttachment.getImageWidth());
		cv.put(TbDAttachments.F_HEIGHT, dAttachment.getImageHeight());
		cv.put(TbDAttachments.F_IS_IMAGE_LOAD, dAttachment.isImageLoad() ? 0 : 1);
		cv.put(TbDAttachments.F_DOWNLOAD_PAUSE_FLAG, dAttachment.isDownloadPause()? 1 :0);

		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// db.insert("tb_d_chat_attachment", null, cv);
					insertOrIgnore("tb_d_chat_attachment", cv);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			new UnavailableStorageException(
					"persist single chat attachment fail.", e);
		}
	}

	/**
	 * persist contacts info <br>
	 * if contact exists already, modify receive count
	 * 
	 * method name: persistContactsInfo function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param contactList
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-9 上午9:49:48 @Modified by：zhangyq
	 * @Description：
	 */
	public void persistContactsInfo(final List<ContactAttribute> contactList)
			throws UnavailableStorageException {

		List<ContactAttribute> contactDB = null;
		try {
			contactDB = getAllContacts();
		} catch (MessagingException e1) {
			e1.printStackTrace();
		}
		final List<ContactAttribute> contactTemp = new ArrayList<ContactAttribute>(
				contactDB);

		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					for (ContactAttribute contact : contactList) {
						ContentValues cv = new ContentValues();

						if (contactTemp == null || contactTemp.isEmpty()) {
							cv.put("f_email", contact.getEmail());
							cv.put("f_nick_name", contact.getNickName());
							cv.put("f_spell_name", contact.getSpellName());
							cv.put("f_send_count", contact.getSendCount());
							cv.put("f_receive_count", contact.getReceiveCount());
							cv.put("f_img_head", contact.getImgHeadPath());
							cv.put("f_img_head_hash", contact.getUploadState());
							cv.put("f_date", contact.getDate());
							db.insert("tb_user_contacts", null, cv);
						} else {
							int flag = 0; // 0-不重复 1-重复
							int receiveCount = 0;
							for (ContactAttribute contactdb : contactTemp) {
								if (contact.getEmail().equalsIgnoreCase(
										contactdb.getEmail())) {
									flag = 1;
									receiveCount = contactdb.getReceiveCount();
									break;
								}
							}
							if (flag == 0) {
								cv.put("f_email", contact.getEmail());
								cv.put("f_nick_name", contact.getNickName());
								cv.put("f_spell_name", contact.getSpellName());
								cv.put("f_send_count", contact.getSendCount());
								cv.put("f_receive_count",
										contact.getReceiveCount());
								cv.put("f_img_head", contact.getImgHeadPath());
								cv.put("f_img_head_hash",
										contact.getUploadState());
								cv.put("f_date", contact.getDate());
								db.insert("tb_user_contacts", null, cv);
							} else {
								cv.put("f_receive_count", receiveCount + 1);
								if (!contact.getNickName().equals(
										contact.getEmail()
												.substring(
														0,
														contact.getEmail()
																.indexOf("@")))) {
									cv.put("f_nick_name", contact.getNickName());
								}
								db.update("tb_user_contacts", cv,
										"f_email = ?",
										new String[] { contact.getEmail() });
							}
						}
					}
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			new UnavailableStorageException("persist contacts info fail.", e);
		}
	}

	/**
	 * 存储联系人 备注：聊天生成的
	 * 
	 * @Description:
	 * @param email
	 * @param nickName
	 * @param isReplace 是否整个更新该联系人
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws UnavailableStorageException
	 * @date:2014-12-3
	 */
	public void savaContact(final String email, final String nickName,final boolean isReplace) throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {

			@Override
			public Void doDbWork(SQLiteDatabase db)
					throws WrappedException, UnavailableStorageException {
				ContactAttribute contactAttribute = new ContactAttribute();
				contactAttribute.setEmail(StringUtil.getEmailSuffix(email));
				contactAttribute.setNickName(nickName);
				contactAttribute.setSpellName(HanziToPinyin.toPinyin(email
						.substring(0, email.indexOf("@"))));
//				contactAttribute.setReceiveCount(1);
				contactAttribute.setDate(System.currentTimeMillis() + "");
				ContentValues cv = new ContentValues();
				cv.put("f_email", contactAttribute.getEmail());
				cv.put("f_nick_name", contactAttribute.getNickName());
				cv.put("f_spell_name", contactAttribute.getSpellName());
				cv.put("f_send_count", contactAttribute.getSendCount());
				cv.put("f_receive_count",
						contactAttribute.getReceiveCount());
				cv.put("f_img_head", contactAttribute.getImgHeadPath());
				cv.put("f_img_head_hash", contactAttribute.getUploadState());
				cv.put("f_date", contactAttribute.getDate());
				cv.put("f_is_used_mailchat", contactAttribute.isUsedMailchat() ? 1 : 0);
				cv.put(TbUserContacts.F_COMPANY, contactAttribute.getCompany());
				cv.put(TbUserContacts.F_DEPARTMENT, contactAttribute.getDepartment());
				cv.put(TbUserContacts.F_POSITION, contactAttribute.getPosition());
				// db.insert("tb_user_contacts", null, cv);
				if(isReplace){
					insertOrReplace("tb_user_contacts", cv);
				}else{
					insertOrIgnore("tb_user_contacts", cv);
				}
				return null;
			}
		});
	}

	/**
	 * 存储联系人
	 *
	 * @Description:
	 * @param ContactAttribute
	 * @param isReplace 是否强制替换该条所有属性
	 * @param isAdd 是否为收到增加的
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-3
	 */
	public void savaContactAttribute(final ContactAttribute contact,final boolean isReplace,final boolean isAdd) {
		try {
			database.execute(false, new DbCallback<Void>() {

				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					String email = contact.getEmail();
					String nickName = contact.getNickName();
					String company= contact.getCompany();
					String department= contact.getDepartment();
					String position= contact.getPosition();
					String phone= contact.getPhones();
					String addr= contact.getAddr();
					String other= contact.getOtherRemarks();
					boolean isUsedMailchat = contact.isUsedMailchat();
					ContentValues cv = new ContentValues();
					if (!StringUtils.isNullOrEmpty(email)) {
						cv.put("f_email", StringUtil.getEmailSuffix(email));
					}
					if (!StringUtils.isNullOrEmpty(nickName)) {
					cv.put("f_nick_name", nickName);
					}
					if (!StringUtils.isNullOrEmpty(email)) {
					cv.put("f_spell_name", HanziToPinyin.toPinyin(email
							.substring(0, email.indexOf("@"))));
					}
					cv.put("f_send_count", contact.getSendCount());
					cv.put("f_receive_count",1);
					if (!StringUtils.isNullOrEmpty(contact.getImgHeadPath())) {
					cv.put("f_img_head", contact.getImgHeadPath());
					}
					if (!StringUtils.isNullOrEmpty( contact.getImgHeadHash())&&!"null".equals(contact.getImgHeadHash())) {
					cv.put("f_img_head_hash", contact.getImgHeadHash());
					}
					if (!StringUtils.isNullOrEmpty(contact.getDate())) {
					cv.put("f_date", contact.getDate());
					}
					cv.put("f_is_used_mailchat",isUsedMailchat);
					if (!StringUtils.isNullOrEmpty(company)) {
					cv.put(TbUserContacts.F_COMPANY, company);
					}
					if (!StringUtils.isNullOrEmpty(department)) {
					cv.put(TbUserContacts.F_DEPARTMENT,department);
					}
					if (!StringUtils.isNullOrEmpty(position)) {
					cv.put(TbUserContacts.F_POSITION, position);
					}
					if (!StringUtils.isNullOrEmpty(addr)) {
					cv.put(TbUserContacts.F_ADDR, addr);
					}
					if (!StringUtils.isNullOrEmpty(phone)) {
					cv.put(TbUserContacts.F_PHONE, phone);
					}
					if (!StringUtils.isNullOrEmpty(other)) {
					cv.put(TbUserContacts.F_REMARKS,other);
					}
					if(isAdd){
						cv.put(TbUserContacts.F_IS_ADD,1);
					}
					// db.insert("tb_user_contacts", null, cv);
					if(isReplace){
						insertOrReplace("tb_user_contacts", cv);
					}else{
						insertOrIgnore("tb_user_contacts", cv);
					}
					//插入数据到remark表
					ContactAttribute contactAttribute=new ContactAttribute(contact.getEmail(), null,
							contact.getrNickName(), contact.getrImgHeadHash(), contact.getrCompany(),
							null, contact.getrDepartment(), contact.getrPosition(), contact.getrPhones(), 
							contact.getrAddr(), contact.getrOtherRemarks(), DateUtil.dateToString(new Date(), DateUtil.LONG_DATE_FORMAT));
					insertContactRemark(contactAttribute);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * method name: insertContactRemark function @Description: TODO Parameters
	 * and return values description:
	 * 
	 * @param contactAttribute
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-11-20 下午5:44:57 @Modified by：zhangjx
	 * @Description:保存联系人备注信息
	 */
	public void insertContactRemark(ContactAttribute contactAttribute) {
		ContentValues cv = new ContentValues();
		cv.put(TbContactRemark.F_EMAIL, contactAttribute.getrEmail());
		cv.put(TbContactRemark.F_NICK_NAME, contactAttribute.getrNickName());
		cv.put(TbContactRemark.F_NAME, contactAttribute.getrName());
		cv.put(TbContactRemark.F_IMG_HEAD_HASH,
				contactAttribute.getrImgHeadHash());
		cv.put(TbContactRemark.F_REMARK, contactAttribute.getrOtherRemarks());
		cv.put(TbContactRemark.F_PHONE, contactAttribute.getrPhones());
		cv.put(TbContactRemark.F_DEPARTMENT, contactAttribute.getrDepartment());
		cv.put(TbContactRemark.F_POSITION, contactAttribute.getrPosition());
		cv.put(TbContactRemark.F_ADDR, contactAttribute.getrAddr());
		cv.put(TbContactRemark.F_COMPANY, contactAttribute.getrCompany());
		cv.put(TbContactRemark.F_DATE, contactAttribute.getrData());
		try {
			insertOrReplace("tb_user_contact_remark", cv);
		} catch (UnavailableStorageException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * method name: getContactAttribute function @Description: TODO Parameters
	 * and return values description:
	 * 
	 * @param email
	 * @param isEis
	 *            是否eis
	 * @return field_name ContactAttribute return type
	 * @throws UnavailableStorageException
	 * @History memory：
	 * @Date：2015-11-24 下午1:41:27 @Modified by：zhangjx
	 * @Description:
	 */
	public ContactAttribute getContactAttribute(final String email,
			final boolean isEis) throws UnavailableStorageException {
		return database.execute(false, new DbCallback<ContactAttribute>() {

			@Override
			public ContactAttribute doDbWork(SQLiteDatabase db)
					throws WrappedException, UnavailableStorageException {
				ContactAttribute ca = new ContactAttribute();
				StringBuffer sql = new StringBuffer();
				Cursor cursor = null;
				try {
					if (isEis) {
						sql.append("select  * from (")
								.append("select e.*,f.f_name from ")
								.append("(select c.*,d.f_dep_id,d.f_is_leader ,d.f_position from")
								.append("(select * from tb_b_contact_user where f_email =?) ")
								.append("AS c left join tb_b_contact_department_user d on c.f_email = d.f_user_email)")
								.append("AS e left join tb_b_contact_department f on f.f_id = e.f_dep_id ")
								.append(") g left join tb_user_contact_remark h on (g.f_email = h.f_r_email)");

						cursor = db.rawQuery(sql.toString(),
								new String[] { email });
						if (cursor.moveToFirst()) {
							ca.setEmail(cursor.getString(cursor
									.getColumnIndex(TbBusinessContactUser.F_EMAIL)));
							ca.setName(cursor.getString(1));
							ca.setSendCount(cursor.getInt(cursor
									.getColumnIndex(TbBusinessContactUser.F_SEND_COUNT)));
							ca.setReceiveCount(cursor.getInt(cursor
									.getColumnIndex(TbBusinessContactUser.F_RECEIVE_COUNT)));
							ca.setNickName(cursor.getString(1));
							ca.setUsedMailchat(cursor.getInt(cursor
									.getColumnIndex(TbBusinessContactUser.F_IS_USED_MAILCHAT)) == 1 ? true
									: false);
							ca.setEisName("");
							ca.setDepartment(cursor.getString(18));
							ca.setPosition(cursor.getString(17));
							ca.setAddr(cursor.getString(13));
							ca.setPhones(cursor.getString(6));
							ca.setOtherRemarks(cursor.getString(12));
							ca.setEisContact(true);
							if (ca.getImgHeadHash() == null) {
								String avatarHash = cursor.getString(4);
								if (avatarHash != null
										&& !avatarHash.equals("null")) {
									ca.setImgHeadHash(avatarHash);
									ca.setImgHeadPath(GlobalConstants.HOST_IMG
											+ avatarHash);
								}
							}
							getContactRemarkByEmail(ca, cursor);
						} else {
							ca = null;
						}
					} else {
						sql.append("select  * from ")
								.append("(select * from tb_user_contacts where f_is_visibility = 1  and f_email = ?)")
								.append(" a left join tb_user_contact_remark b on (a.f_email = b.f_r_email) order by a.f_email desc");
						// sql =
						// "select  * from (select distinct f_email, f_nick_name, f_spell_name, "
						// +
						// "f_send_count,f_receive_count, f_img_head, f_img_head_hash,f_is_used_mailchat,"
						// +
						// "f_company,f_department,f_position,f_phone,f_addr,f_remarks from tb_user_contacts "
						// +
						// "where f_is_visibility = 1  and f_email = ?) a left join tb_user_contact_remark b on "
						// +
						// "(a.f_email = b.f_r_email) order by a.f_email desc";
						cursor = db.rawQuery(sql.toString(),
								new String[] { email });
						if (cursor.moveToFirst()) {
							// String selection = TbUserContacts.F_EMAIL +
							// "=? ";
							// String[] selectionArgs = new String[] { email };
							// cursor = db.query(TbUserContacts.TB_NAME, null,
							// selection, selectionArgs, null, null, null);
							// cursor.moveToFirst();
							ca.setEmail(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_EMAIL)));
							ca.setNickName(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_NICK_NAME)));
							ca.setSpellName(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_SPELL_NAME)));
							String imgHeadHash = cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_IMG_HEAD_HASH));
							setUserHeadUrl(ca, imgHeadHash);
							ca.setImgHeadHash(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_IMG_HEAD_HASH)));
							ca.setUsedMailchat(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_IS_USED_MAILCHAT)) == 1 ? true
									: false);
							ca.setEisContact(false);
							ca.setEisName("");
							ca.setCompany(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_COMPANY)));
							ca.setDepartment(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_DEPARTMENT)));
							ca.setPosition(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_POSITION)));
							ca.setPhones(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_PHONE)));
							ca.setAddr(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_ADDR)));
							ca.setOtherRemarks(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_REMARKS)));
							getContactRemarkByEmail(ca, cursor);
						} else {
							ca = null;
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					ca = null;
				} finally {
					Utility.closeQuietly(cursor);
				}
				return ca;
			}
		});
	}

	private void setUserHeadUrl(ContactAttribute ca, String imgHeadHash) {
		if (!StringUtils.isNullOrEmpty(imgHeadHash)
				&& !"null".equals(imgHeadHash)) {
			if (imgHeadHash.startsWith("http")) {
				ca.setImgHeadPath(imgHeadHash);
			} else if (!TextUtils.isEmpty(imgHeadHash)) {
				ca.setImgHeadPath(GlobalConstants.HOST_IMG + imgHeadHash);
			}
		}
	}
	/**
	 * 更新常用联系人标记数
	 * 
	 * @Description:
	 * @param email
	 *            //联系人email
	 * @param receiveCount
	 *            联系人使用数
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-3
	 */
	public void updateContactAttributeReceiveCount(final String email,
			final int receiveCount) {
		try {
			database.execute(false, new DbCallback<Void>() {

				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					ContentValues cv = new ContentValues();
					String whereClause = TbUserContacts.F_EMAIL + "=?";
					String[] whereArgs = new String[] { email };
					cv.put(TbUserContacts.F_RECEIVE_COUNT, receiveCount);
					db.update(TbUserContacts.TB_NAME, cv, whereClause,
							whereArgs);
					return null;
				}

			});
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * get 10 important contacts
	 * 
	 * method name: getImportantContacts function @Description: Parameters and
	 * return values description：
	 * 
	 * @return
	 * @throws MessagingException
	 *             field_name List<ContactAttribute> return type
	 * @History memory：
	 * @Date：2014-9-29 下午1:44:06 @Modified by：zhangyq
	 * @Description：
	 */
	public List<ContactAttribute> getImportantContacts(final String keyWord)
			throws MessagingException {
		final List<ContactAttribute> contactList = new LinkedList<ContactAttribute>();
		try {
			database.execute(false, new DbCallback<List<ContactAttribute>>() {
				public List<ContactAttribute> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					try {
						cursor = db
								.rawQuery("select  * from ("+
										"select distinct f_email, f_nick_name, f_spell_name, f_send_count,f_receive_count, f_img_head, f_img_head_hash,f_company," +
										"f_department,f_position,f_phone,f_addr,f_remarks "
												+ "from tb_user_contacts where f_is_visibility = 1 and (f_email like ? or f_nick_name like ?) order by F_RECEIVE_COUNT desc limit 10 offset 0"
										+" ) a left join tb_user_contact_remark b on (a.f_email = b.f_r_email)",
										new String[]{"%"+keyWord+"%","%"+keyWord+"%"});
						while (cursor.moveToNext()) {
							ContactAttribute ca = new ContactAttribute();
							ca.setEmail(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_EMAIL)));
							ca.setNickName(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_NICK_NAME)));
							ca.setSpellName(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_SPELL_NAME)));
							ca.setSendCount(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_SEND_COUNT)));
							ca.setReceiveCount(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_RECEIVE_COUNT)));
							ca.setImgHeadPath(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_IMG_HEAD_NAME)));
							ca.setImgHeadHash(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_IMG_HEAD_HASH)));
							ca.setEisContact(false);
							ca.setEisName("");
							ca.setCompany(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_COMPANY)));
							ca.setDepartment(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_DEPARTMENT)));
							ca.setPosition(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_POSITION)));
							ca.setPhones(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_PHONE)));
							ca.setAddr(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_ADDR)));
							ca.setOtherRemarks(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_REMARKS)));
							getContactRemarkByEmail(ca, cursor);
							contactList.add(ca);
						}
						return contactList;
					} catch (Exception e) {
						e.printStackTrace();
						throw new WrappedException(e);
					} finally {
						Utility.closeQuietly(cursor);
					}
				}
			});
		} catch (WrappedException e) {
			e.printStackTrace();
		}
		return contactList;
	}

	public List<ContactAttribute> getImportantContactsWith35Eis(final Account account)
			throws MessagingException {
		final List<ContactAttribute> contactList = new LinkedList<ContactAttribute>();
		try {
			database.execute(false, new DbCallback<List<ContactAttribute>>() {
				public List<ContactAttribute> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					try {
						StringBuffer sql = new StringBuffer();
						sql.append("select  * from (")
						.append("select e.*,f.f_name from ")
								.append("(select c.*,d.f_dep_id,d.f_is_leader ,d.f_position from")
								.append("(select a.*,b.f_name,b.f_birthday,b.f_phone,b.f_remark,b.f_address,b.f_img_head_hash,b.f_company from (select f_email, f_nick_name, f_spell_name, f_send_count,f_receive_count, f_img_head, f_img_head_hash from tb_user_contacts  where f_is_visibility = 1 order by f_receive_count desc limit 10 offset 0) AS a left join tb_b_contact_user b on a.f_email = b.f_email) ")
								.append("AS c left join tb_b_contact_department_user d on c.f_email = d.f_user_email)")
								.append("AS e left join tb_b_contact_department f on f.f_id = e.f_dep_id ")
								.append(") g left join tb_user_contact_remark h on (g.f_email = h.f_r_email)");
						cursor = db.rawQuery(sql.toString(), null);
						while (cursor.moveToNext()) {
							ContactAttribute ca = new ContactAttribute();
							ca.setEmail(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_EMAIL)));
							ca.setNickName(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_NICK_NAME)));
							ca.setSpellName(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_SPELL_NAME)));
							ca.setSendCount(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_SEND_COUNT)));
							ca.setReceiveCount(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_RECEIVE_COUNT)));
							ca.setEisName(cursor.getString(7));
							ca.setDepartment(cursor.getString(17));
							ca.setOtherRemarks(cursor.getString(cursor
									.getColumnIndex(TbBusinessContactUser.F_REMARK)));
							ca.setPosition(cursor.getString(cursor
									.getColumnIndex(TbBusinessContactDepartment_User.F_POSITION)));
							ca.setPhones(cursor.getString(cursor
									.getColumnIndex(TbBusinessContactUser.F_PHONE)));
							ca.setAddr(cursor.getString(cursor
									.getColumnIndex(TbBusinessContactUser.F_ADDR)));
							ca.setCompany(cursor.getString(cursor
									.getColumnIndex(TbBusinessContactUser.F_COMPANY)));
							String string = cursor.getString(cursor
									.getColumnIndex(TbBusinessContactDepartment_User.F_DEP_ID));
							if (string != null) {
								ca.setEisContact(true);
							}
							String imgHeadHash = cursor.getString(6);
							if (imgHeadHash != null) {
								if (imgHeadHash.startsWith("http")) {
									ca.setImgHeadPath(imgHeadHash);
								} else if (!TextUtils.isEmpty(imgHeadHash)) {
									ca.setImgHeadPath(GlobalConstants.HOST_IMG
											+ imgHeadHash);
								}
							} else {
								String imgHash = cursor.getString(12);
								if (imgHash != null) {
									if (imgHash.startsWith("http")) {
										ca.setImgHeadPath(imgHash);
									} else if (!TextUtils.isEmpty(imgHash)
											&& !imgHash.equals("null")) {
										ca.setImgHeadPath(GlobalConstants.HOST_IMG
												+ imgHash);
									}
								}
							}
							getContactRemarkByEmail(ca,cursor);
							contactList.add(ca);
						}
						return contactList;
					} catch (Exception e) {
						throw new WrappedException(e);
					} finally {
						Utility.closeQuietly(cursor);
					}
				}
			});
		} catch (WrappedException e) {
			e.printStackTrace();
		}
		return contactList;
	}
	/**
	 * get all contacts
	 * 
	 * method name: getAllContacts function @Description: Parameters and return
	 * values description：
	 * 
	 * @return
	 * @throws MessagingException
	 *             field_name List<ContactAttribute> return type
	 * @History memory：
	 * @Date：2014-9-29 下午1:45:36 @Modified by：zhangyq
	 * @Description：
	 */
	public List<ContactAttribute> getAllContacts() throws MessagingException {
		final List<ContactAttribute> contactList = new LinkedList<ContactAttribute>();
		try {
			database.execute(false, new DbCallback<List<ContactAttribute>>() {

				public List<ContactAttribute> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					try {
						cursor = db
								.rawQuery(
										"select distinct f_email, f_nick_name, f_spell_name, f_send_count,f_receive_count, f_img_head, f_img_head_hash,f_is_used_mailchat,f_company,f_department,f_position,f_phone,f_addr,f_remarks from tb_user_contacts where f_is_visibility = ?",
										new String []{"1"});
						while (cursor.moveToNext()) {
							ContactAttribute ca = new ContactAttribute();
							ca.setEmail(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_EMAIL)));
							ca.setNickName(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_NICK_NAME)));
							ca.setSpellName(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_SPELL_NAME)));
							ca.setSendCount(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_SEND_COUNT)));
							ca.setReceiveCount(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_RECEIVE_COUNT)));
							ca.setImgHeadPath(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_IMG_HEAD_NAME)));
							ca.setImgHeadHash(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_IMG_HEAD_HASH)));
							ca.setUsedMailchat(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_IS_USED_MAILCHAT))==1? true:false);
							ca.setCompany(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_COMPANY)));
							ca.setDepartment(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_DEPARTMENT)));
							ca.setPosition(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_POSITION)));
							ca.setPhones(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_PHONE)));
							ca.setAddr(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_ADDR)));
							ca.setOtherRemarks(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_REMARKS)));
							contactList.add(ca);
						}
						return contactList;
					} catch (Exception e) {
						throw new WrappedException(e);
					} finally {
						Utility.closeQuietly(cursor);
					}
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}

		return contactList;
	}

	/**
	 * update contact info by email
	 * 
	 * method name: updateContactInfoByEmail function @Description: Parameters
	 * and return values description：
	 * 
	 * @param email
	 * @param contact
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-9-30 上午10:38:03 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateContactInfoByEmail(final String email,
			ContactAttribute contact,final boolean isUpdateChatList) throws UnavailableStorageException {
		// if email or contact data error, direct return
		if (email == null || "".equals(email) || contact == null) {
			if (MailChat.DEBUG)
				Log.d(MailChat.LOG_TAG, "inaccurate email or contact");
			return;
		}
		final ContentValues cv = new ContentValues();
		if (!StringUtils.isNullOrEmpty(contact.getEmail())) {
			cv.put("f_email", contact.getEmail());
		}
		if (!StringUtils.isNullOrEmpty(contact.getNickName())) {
			cv.put("f_nick_name", contact.getNickName());
		}
		if (!StringUtils.isNullOrEmpty(contact.getSpellName())) {
			cv.put("f_spell_name", contact.getSpellName());
		}
		if (contact.getSendCount() > 0) {
			cv.put("f_send_count", contact.getSendCount());
		}
		if (contact.getReceiveCount() > 0) {
			cv.put("f_receive_count", contact.getReceiveCount());
		}
		if (!StringUtils.isNullOrEmpty(contact.getImgHeadPath())) {
			cv.put("f_img_head", GlobalConstants.HOST_IMG+contact.getImgHeadPath());
		}
		if (!StringUtils.isNullOrEmpty(contact.getImgHeadHash())) {
			cv.put("f_img_head_hash", contact.getImgHeadHash());
		}
		if (!StringUtils.isNullOrEmpty(contact.getUploadState())) {
			cv.put("f_upload_state", contact.getUploadState());
		}
		if (!StringUtils.isNullOrEmpty(contact.getCompany())) {
			cv.put("f_company", contact.getCompany());
		}
		if (!StringUtils.isNullOrEmpty(contact.getDepartment())) {
			cv.put("f_department", contact.getDepartment());
		}
		if (!StringUtils.isNullOrEmpty(contact.getPosition())) {
			cv.put("f_position", contact.getPosition());
		}
		cv.put("f_is_used_mailchat", contact.isUsedMailchat()?1:0);
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				int result = db.update("tb_user_contacts", cv, "f_email = ?",
						new String[] { email });
				if(result>0 &&isUpdateChatList){
					notifyChattingChange();
				}
				return null;
			}
		});
	}

	/**
	 * get all DChat list
	 * 
	 * method name: listDchats function @Description: Parameters and return
	 * values description：
	 * 
	 * @return
	 * @throws MessagingException
	 *             field_name List<DChat> return type
	 * @History memory：
	 * @Date：2014-9-30 下午2:00:22 @Modified by：zhangyq
	 * @Description：
	 */
	public List<DChat> listDchats() throws MessagingException {
		final List<DChat> dChatList = new LinkedList<DChat>();
		try {
			database.execute(false, new DbCallback<List<DChat>>() {

				public List<DChat> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					String selection = TbDchat.F_IS_VISIBILITY + " = ?";
					String selectionArgs[] = new String[] { String.valueOf(1) };
					String orderBy = TbDchat.F_IS_STICKED + " desc ,"
							+ TbDchat.F_STICKED_DATE + " desc ,"
							+ TbDchat.F_LAST_TIME + " desc";

					try {
						cursor = db.query(TbDchat.TB_NAME, null, selection,
								selectionArgs, null, null, orderBy);

						while (cursor.moveToNext()) {
							DChat dChat = new DChat();
							dChat.setUid(cursor.getString(cursor
									.getColumnIndex(TbDchat.F_UID)));
							dChat.setLastMessage(cursor.getString(cursor
									.getColumnIndex(TbDchat.F_LAST_MESSAGE)));
							dChat.setLastMessageEmail(cursor.getString(cursor
									.getColumnIndex(TbDchat.F_LAST_MESSAGE_EMAIL)));
							dChat.setLastTime(cursor.getLong(cursor
									.getColumnIndex(TbDchat.F_LAST_TIME)));
							dChat.setEmail(cursor.getString(cursor
									.getColumnIndex(TbDchat.F_TO_EMAIL)));
							dChat.setStickedDate(cursor.getLong(cursor
									.getColumnIndex(TbDchat.F_STICKED_DATE)));
							dChat.setSticked(cursor.getInt(cursor
									.getColumnIndex(TbDchat.F_IS_STICKED)) == 1 ? true
									: false);
							dChat.setUnReadCount(cursor.getInt(cursor
									.getColumnIndex(TbDchat.F_UNREADCOUNT)));
							DChatMessage.Type type = DChatMessage.Type.values()[cursor.getInt((cursor
									.getColumnIndex(TbDchat.F_LAST_MESSAGE_TYPE)))];
							dChat.setLastMessageType(type);
							dChatList.add(dChat);
						}
						return dChatList;
					} catch (Exception e) {
						throw new WrappedException(e);
					} finally {
						Utility.closeQuietly(cursor);
					}
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}
		return dChatList;
	}

	/**
	 * get DChat info. eg: A talk with B
	 * 
	 * method name: getDChat function @Description: values description：
	 * 
	 * @param uuid
	 * @return
	 * @throws MessagingException
	 *             field_name DChat return type
	 * @History memory：
	 * @Date：2014-9-30 下午2:45:49 @Modified by：zhangyq
	 * @Description：
	 */
	public DChat getDChat(final String uuid) throws MessagingException {
		return database.execute(false, new DbCallback<DChat>() {

				public DChat doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					DChat dChat = null;
					try {
						String sql = "SELECT d.*, c.f_nick_name, c.f_img_head, c.f_img_head_hash FROM tb_d_chatlist d LEFT JOIN tb_user_contacts c ON (d.f_to_email = c.f_email) WHERE d.f_message_dchat_uid = ? ORDER BY d.f_is_sticked DESC, d.f_sticked_date DESC, d.f_last_time DESC";
						cursor = db.rawQuery(sql,new String[] {uuid});
						if(cursor.moveToFirst()){
							dChat = new DChat();
							dChat = parseCursorDChat(cursor);
						}
					} catch (Exception e) {
						dChat = null;
					} finally {
						Utility.closeQuietly(cursor);
					}
					return dChat;
				}
			});
	}

	/**
	 * update dchat <br>
	 * deleteFlat: 1-visibility 0-hidden -1-no action
	 * 
	 * method name: updateDchat function @Description: Parameters and return
	 * values description：
	 * 
	 * @param dChat
	 * @param deleteFlag
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-9 下午3:25:25 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateDchat(final DChat dChat, final int deleteFlag,final boolean isLastContentToEmpty)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues values = new ContentValues();
				// 文件夹id
				values.put(TbDchat.F_TO_EMAIL, dChat.getEmail());
				if(isLastContentToEmpty){
					values.put(TbDchat.F_LAST_MESSAGE, "");
					values.put(TbDchat.F_LAST_MESSAGE_TYPE,0);
					values.put(TbDchat.F_LAST_MESSAGE_EMAIL,"");
				}else{
					values.put(TbDchat.F_LAST_MESSAGE, dChat.getLastMessage());
					values.put(TbDchat.F_LAST_MESSAGE_TYPE, dChat
							.getLastMessageType().ordinal());
					values.put(TbDchat.F_LAST_MESSAGE_EMAIL,
							dChat.getLastMessageEmail());
				}
				values.put(TbDchat.F_LAST_TIME, dChat.getLastTime());
				values.put(TbDchat.F_UNREADCOUNT, dChat.getUnReadCount());
				if (deleteFlag != -1) {
					values.put(TbDchat.F_IS_VISIBILITY, deleteFlag);
				}
				values.put(TbDchat.F_SEND_STATE, dChat.getMessageState());
				String whereClause = TbDchat.F_UID + "=?";
				String[] whereArgs = { dChat.getUid() };
				int result = db.update(TbDchat.TB_NAME, values, whereClause, whereArgs);
				if (result > 0) {
					notifyChattingChange();
				}
				return null;
			}
		});
	}

	/**
	 * update dchat unread marks
	 * 
	 * method name: updateDchat function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param uuid
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-9 下午3:30:27 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateDchatUntreatedCount(final String uuid,final int count)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues values = new ContentValues();
				// 文件夹id
				values.put(TbDchat.F_UNREADCOUNT, count);
				int result;
				if ( uuid == null ){//uuid为空时更新全部
					result  = db.update(TbDchat.TB_NAME, values, null, null);
				}else{
					String whereClause = TbDchat.F_UID + "=?";
					String[] whereArgs = { uuid };
					result  = db.update(TbDchat.TB_NAME, values, whereClause, whereArgs);
				}
				if (result > 0) {
					notifyChattingChange();
				}
				return null;
			}
		});
	}

	/**
	 * update dchat to-top status
	 * 
	 * method name: updateDchatSticked function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param dChatUid
	 * @param isSticked
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-9 下午3:36:58 @Modified by：zhangyq
	 * @Description：
	 */
	public boolean updateDchatSticked(final String dChatUid,
			final boolean isSticked) throws UnavailableStorageException {
		return database.execute(false, new DbCallback<Boolean>() {
			@Override
			public Boolean doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TbDchat.F_STICKED_DATE,
						isSticked ? System.currentTimeMillis() : 0);
				contentValues.put(TbDchat.F_IS_STICKED, isSticked ? 1 : 0);
				String whereClause = TbDchat.F_UID + "=?";
				String[] whereArgs = { dChatUid };
				int result = db.update(TbDchat.TB_NAME, contentValues,
						whereClause, whereArgs);
				if (result > 0) {
					notifyChattingChange();
					return true;
				} else {
					return false;
				}
			}
		});
	}

	/**
	 * update dchat delete flag
	 * 
	 * method name: updateDchatDeleteFlag function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param dChatUid
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-9 下午3:40:21 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateDchatDeleteFlag(final String dChatUid,final boolean isVisibility)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TbDchat.F_IS_VISIBILITY, isVisibility ? 1 : 0);
				String whereClause = TbDchat.F_UID + "=?";
				String[] whereArgs = { dChatUid };
				int result = db.update(TbDchat.TB_NAME, contentValues, whereClause,
						whereArgs);
				if(result>0){
					notifyChattingChange();
				}
				return null;
			}
		});
	}

	/**
	 * update dchat to-top status
	 * 
	 * method name: updateDchatAlert function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param dChatUid
	 * @param isAlert
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-9 下午3:44:55 @Modified by：zhangyq
	 * @Description：
	 */
	public boolean updateDchatAlert(final String dChatUid, final boolean isAlert)
			throws UnavailableStorageException {
		return database.execute(false, new DbCallback<Boolean>() {
			@Override
			public Boolean doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues contentValues = new ContentValues();
				// isAlert 0为开启，1为关闭。
				contentValues.put(TbDchat.F_IS_DCHAT_ALERT, isAlert ? 1 : 0);
				String whereClause = TbDchat.F_UID + "=?";
				String[] whereArgs = { dChatUid };
				int result = db.update(TbDchat.TB_NAME, contentValues,
						whereClause, whereArgs);
				if(result>0){
					notifyChattingChange();
					return true;
				} else {
					return false;
				}
			}
		});
	}

	/**
	 * delete dchat message
	 * 
	 * method name: deleteDchatMessageFlag function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param dChatDMessageUid
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-9 下午3:50:07 @Modified by：zhangyq
	 * @Description：
	 */
	public boolean deleteDchatMessageFlag(final String dChatDMessageUid)
			throws UnavailableStorageException {
		return database.execute(false, new DbCallback<Boolean>() {
			@Override
			public Boolean doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TbDchatMessage.F_DELETE_FLAG, 1);
				String whereClause = TbDchatMessage.F_UID + "=?";
				String[] whereArgs = { dChatDMessageUid };
				int result = db.update(TbDchatMessage.TB_NAME, contentValues,
						whereClause, whereArgs);
				if (result > 0) {
					return true;
				} else {
					return false;
				}
			}
		});
	}
	/**
	 * 更新该单聊消息的删除标记
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-9-18
	 */
	public boolean updateAllDchatMessageFlag(final String dChatUid,final int flag)
			throws UnavailableStorageException {
		return database.execute(false, new DbCallback<Boolean>() {
			@Override
			public Boolean doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TbDchatMessage.F_DELETE_FLAG, flag);
				String whereClause = TbDchatMessage.F_DCHAT_UID + "=?";
				String[] whereArgs = { dChatUid };
				int result = db.update(TbDchatMessage.TB_NAME, contentValues,
						whereClause, whereArgs);
				if (result > 0) {
					return true;
				} else {
					return false;
				}
			}
		});
	}

	/**
	 * update dchat message send status
	 * 
	 * method name: updateDchatMessageState function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param uuid
	 * @param state
	 * @param sendTime
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-9 下午3:53:54 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateDchatMessageState(final String uuid, final int state,
			final long sendTime) throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TbDchatMessage.F_MESSAGE_STATE, state);
				if (sendTime != -1) {
					contentValues.put(TbDchatMessage.F_TIME, sendTime);
				}
				// TODO:处理发送成功时间，列表展示也要处理
				String whereClause = TbDchatMessage.F_UID + "=?";
				String[] whereArgs = { uuid };
				db.update(TbDchatMessage.TB_NAME, contentValues, whereClause,
						whereArgs);

				return null;
			}
		});
	}
	
	public void updateDchatMessageTime(final String uuid,final long sendTime) throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TbDchatMessage.F_TIME, sendTime);
				// TODO:处理发送成功时间，列表展示也要处理
				String whereClause = TbDchatMessage.F_UID + "=?";
				String[] whereArgs = { uuid };
				db.update(TbDchatMessage.TB_NAME, contentValues, whereClause,
						whereArgs);
				return null;
			}
		});
	}
	/**
	 * list of the current dchat message
	 * 
	 * method name: listDchatMessages function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param DchatUid
	 * @param count
	 * @return
	 * @throws MessagingException
	 *             field_name List<DChatMessage> return type
	 * @History memory：
	 * @Date：2014-10-9 下午4:36:28 @Modified by：zhangyq
	 * @Description：
	 */
	public List<DChatMessage> listDchatMessages(final String DchatUid,
			final int count) throws MessagingException {
		final List<DChatMessage> dChatMessageList = new ArrayList<DChatMessage>();
		try {
			database.execute(false, new DbCallback<List<DChatMessage>>() {
				@Override
				public List<DChatMessage> doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					Cursor cursor = null;
					StringBuffer sqlBuffer = new StringBuffer();
					sqlBuffer.append("SELECT * FROM " + TbDchatMessage.TB_NAME)
							.append(" WHERE ")
							.append(TbDchatMessage.F_DCHAT_UID + "=? and ")
							.append(TbDchatMessage.F_DELETE_FLAG + "=0")
							.append(" ORDER BY ").append(TbDchatMessage.F_TIME)
							.append("  desc")
							.append(" LIMIT " + String.valueOf(count));
					try {
						cursor = db.rawQuery(sqlBuffer.toString(),
								new String[] { DchatUid });
						while (cursor.moveToNext()) {
							DChatMessage dChatMessage = new DChatMessage();
							dChatMessage.setUuid(cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_UID)));
							dChatMessage.setDchatUid(cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_DCHAT_UID)));
							dChatMessage.setReceiverEmail(cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_TO_EMAIL)));
							dChatMessage.setSenderEmail(cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_FROM_EMAIL)));
							dChatMessage.setTime(cursor.getLong(cursor
									.getColumnIndex(TbDchatMessage.F_TIME)));
							dChatMessage.setDeleteflag(cursor.getInt(cursor
									.getColumnIndex(TbDchatMessage.F_DELETE_FLAG)));
							dChatMessage.setMessageContent(cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_MESSAGE_CONTENT)));
							if (cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_MESSAGE_STATE)) != null) {
								dChatMessage.setMessageState(cursor.getInt(cursor
										.getColumnIndex(TbDchatMessage.F_MESSAGE_STATE)));
							}
							dChatMessage.setLocationType(cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_LOCATION_TYPE)));
							dChatMessage.setLocationName(cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_LOCATION_NAME)));
							dChatMessage.setAddress(cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_ADDRESS)));
							dChatMessage.setLatitude(Double.valueOf(cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_LAT))));
							dChatMessage.setLongitude(Double.valueOf(cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_LON))));
							dChatMessage.setURL(cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_URL)));
							dChatMessage.setOAId(cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_OA_ID)));
							dChatMessage.setOAFrom(cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_OA_FROM)));
							dChatMessage.setOASubject(cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_OA_SUBJECT)));
							dChatMessage.setReadFlag(cursor.getInt(cursor
									.getColumnIndex(TbDchatMessage.F_READ_FLAG)));
							DChatMessage.Type type = DChatMessage.Type.values()[cursor.getInt((cursor
									.getColumnIndex(TbDchatMessage.F_MESSAGE_TYPE)))];
							dChatMessage.setMailFrom(cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_MAIL_FROM_EMAIL)));
							dChatMessage.setMailFromNickName(cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_MAIL_FROM_NICKNAME)));
							dChatMessage.setMailSubject(cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_MAIL_SUBJECT)));
							dChatMessage.setMailPreview(cursor.getString(cursor
									.getColumnIndex(TbDchatMessage.F_MAIL_PREVIEW)));
							dChatMessage.setMessageType(type);
							try {
								switch (type) {
								case IMAGE:

								case VOICE:

								case ATTACHMENT:
									dChatMessage.setAttachments(getDAttachments(dChatMessage.getUuid()));
									break;
								case OA_ANNOUNCE:

								case OA_NEW_TRANS:
									String fromInfo = dChatMessage.getOAFrom();
									int startIndex = fromInfo.indexOf("<");
									int endIndex = fromInfo.lastIndexOf(">");
									if(startIndex!=-1&&endIndex!=-1&&endIndex>startIndex){
										fromInfo =fromInfo.substring(startIndex+1, endIndex);
										fromInfo =StringUtil.convertChinaChannelTo35CN(fromInfo);
										ContactAttribute contact = getContactAttribute(fromInfo,false);
										if(contact!=null){
											dChatMessage.setImgHeadHash(contact.getImgHeadHash());
											dChatMessage.setNickName(contact.getNickName());
										}
									}
									break;
								default:
									break;
								}
							} catch (MessagingException e) {
								e.printStackTrace();
							}
							dChatMessageList.add(dChatMessage);
						}
						Collections.reverse(dChatMessageList);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} finally {
						Utility.closeQuietly(cursor);
					}
					return dChatMessageList;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dChatMessageList;
	}

	/**
	 * get this dchat message attachment
	 * 
	 * method name: getDAttachments function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param dMessageUid
	 * @return
	 * @throws MessagingException
	 *             field_name List<DAttachment> return type
	 * @History memory：
	 * @Date：2014-10-9 下午6:02:12 @Modified by：zhangyq
	 * @Description：
	 */
	public List<DAttachment> getDAttachments(final String dMessageUid)
			throws MessagingException {
		final List<DAttachment> mDattachments = new ArrayList<DAttachment>();
		try {
			database.execute(false, new DbCallback<List<DAttachment>>() {

				public List<DAttachment> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					String selection = TbDAttachments.F_MESSAGE_UID + "=?";

					try {
						cursor = db.query(TbDAttachments.TB_NAME, null,
								selection, new String[] { dMessageUid }, null,
								null, null);
						while (cursor.moveToNext()) {
							DAttachment dAttachment = new DAttachment();
							dAttachment.setAttchmentId(cursor.getString(cursor
									.getColumnIndex(TbDAttachments.F_UID)));
							dAttachment.setFilePath(cursor.getString(cursor
									.getColumnIndex(TbDAttachments.F_FILE_PATH)));
							dAttachment.setForwardFlag(cursor.getInt(cursor
									.getColumnIndex(TbDAttachments.F_FORWARD_FLAG)));
							dAttachment.setLocalPathFlag(cursor.getInt(cursor
									.getColumnIndex(TbDAttachments.F_LOCALPATH_FLAG)));
							dAttachment.setMessageUid(cursor.getString(cursor
									.getColumnIndex(TbDAttachments.F_MESSAGE_UID)));
							dAttachment.setReadFlag(cursor.getInt(cursor
									.getColumnIndex(TbDAttachments.F_READ_FLAG)));
							dAttachment.setVoiceLength(cursor.getInt(cursor
									.getColumnIndex(TbDAttachments.F_VOICE_LENGTH)));
							if (cursor.getString(cursor
									.getColumnIndex(TbDAttachments.F_NAME)) != null) {
								dAttachment.setName(cursor.getString(cursor
										.getColumnIndex(TbDAttachments.F_NAME)));
							}
							dAttachment.setFileid(cursor.getString(cursor
									.getColumnIndex(TbDAttachments.F_FILE_ID)));
							dAttachment.setSize(cursor.getLong(cursor
									.getColumnIndex(TbDAttachments.F_SIZE)));
							dAttachment.setDownloadProgress(cursor.getInt(cursor
									.getColumnIndex(TbDAttachments.F_DOWNLOAD_PROGRESS)));
							dAttachment.setDownloadPause(cursor.getInt(cursor
									.getColumnIndex(TbDAttachments.F_DOWNLOAD_PAUSE_FLAG))==1 ? true :false);
							dAttachment.setImageWidth(cursor.getInt(cursor
									.getColumnIndex(TbDAttachments.F_WIDTH)));
							dAttachment.setImageHeight(cursor.getInt(cursor
									.getColumnIndex(TbDAttachments.F_HEIGHT)));
							dAttachment.setImageLoad(cursor.getInt(cursor
									.getColumnIndex(TbDAttachments.F_IS_IMAGE_LOAD))==0 ? true :false);
							mDattachments.add(dAttachment);
						}
					} catch (Exception e) {
						throw new WrappedException(e);
					} finally {
						Utility.closeQuietly(cursor);
					}
					return mDattachments.size() > 0 ? mDattachments : null;
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}
		return mDattachments.size() > 0 ? mDattachments : null;
	}

	/**
	 * update dchat message download attachment
	 * 
	 * method name: updateDAttachmentFilePath function @Description: Parameters
	 * and return values description：
	 * 
	 * @param attId
	 * @param newPath
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-9 下午6:09:53 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateDAttachmentFilePath(final int attId, final String newPath)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				StringBuffer sql = new StringBuffer();
				sql.append("update ").append(TbDAttachments.TB_NAME)
						.append(" SET ").append(TbDAttachments.F_FILE_PATH)
						.append("=? WHERE (").append(TbDAttachments.F_UID)
						.append("=?);");
				db.execSQL(sql.toString(), new Object[] { newPath, attId });

				return null;
			}
		});
	}

	/**
	 * dchat voice read updates
	 * 
	 * method name: updateDAttachmentReadFlag function @Description: Parameters
	 * and return values description：
	 * 
	 * @param attId
	 * @param readFlag
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-9 下午6:13:58 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateDAttachmentReadFlag(final String attId, final int readFlag)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				StringBuffer sql = new StringBuffer();
				sql.append("update ").append(TbDAttachments.TB_NAME)
						.append(" SET ").append(TbDAttachments.F_READ_FLAG)
						.append("=? WHERE (").append(TbDAttachments.F_UID)
						.append("=?);");
				db.execSQL(sql.toString(), new Object[] { readFlag, attId });

				return null;
			}
		});
	}

	/**
	 * 创建或保存群组
	 * 
	 * method name: saveOrUpdateCGroup function @Description: Parameters and
	 * return values description：
	 * 
	 * @param group
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-15 下午2:02:25 @Modified by：zhangyq
	 * @Description：
	 */
	public void saveOrUpdateCGroup(CGroup group)
			throws UnavailableStorageException {

		final ContentValues contentValues = parseValuesUpdateGroup(group);

		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					long id = db.insertWithOnConflict(TbCGroup.TB_NAME, null,
							contentValues, SQLiteDatabase.CONFLICT_IGNORE);
					if(id >= 0){
						notifyChattingChange();
					}
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			new UnavailableStorageException("save or update cgroup fail.", e);
		}
	}

	/**
	 * 从服务器获取群信息，更新相应字段，如群名称,//TODO：后期借口改变肯能需要增加多管理员
	 * 
	 * method name: saveOrUpdateCGroupConflictReplace function @Description:
	 * TODO Parameters and return values description：
	 * 
	 * @param group
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-11-7 下午5:05:58 @Modified by：zhangyq
	 * @Description：
	 */
	public void UpdateCGroupNameAndAdmin(final CGroup group)
			throws UnavailableStorageException {
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					ContentValues contentValues = new ContentValues();
					contentValues.put(TbCGroup.F_GROUP_NAME,
							group.getGroupName());
					if (group.getIsAdmin() != null) {
						contentValues.put(TbCGroup.F_IS_ADMIN,
								group.getIsAdmin());
					}
					contentValues.put(TbCGroup.F_RENAME, group.isReName() ? 1
							: 0);
					db.update(TbCGroup.TB_NAME, contentValues, TbCGroup.F_UID
							+ "= ?", new String[] { group.getUid() });
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			new UnavailableStorageException("save or update cgroup fail.", e);
		}
	}

	/**
	 * 更新群名称
	 * 
	 * @Description:
	 * @param group
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-11-19
	 */
	public void UpdateCGroupName(final CGroup group)
			throws UnavailableStorageException {
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					ContentValues contentValues = new ContentValues();
					contentValues.put(TbCGroup.F_GROUP_NAME,
							group.getGroupName());
					contentValues.put(TbCGroup.F_RENAME, group.isReName() ? 1
							: 0);
					db.update(TbCGroup.TB_NAME, contentValues, TbCGroup.F_UID
							+ "= ?", new String[] { group.getUid() });
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			new UnavailableStorageException("save or update cgroup fail.", e);
		}
	}

	/**
	 * 创建或保存群组的 ContentValues
	 * 
	 * method name: parseValuesUpdateGroup function @Description: Parameters and
	 * return values description：
	 * 
	 * @param group
	 * @return field_name ContentValues return type
	 * @History memory：
	 * @Date：2014-10-15 下午3:49:13 @Modified by：zhangyq
	 * @Description：
	 */
	private ContentValues parseValuesUpdateGroup(CGroup group) {
		ContentValues contentValues = new ContentValues();
		if (group.getIsPriv() != null) {
			contentValues.put(TbCGroup.F_IS_PRIV, group.getIsPriv());
		}
		contentValues.put(TbCGroup.F_GROUP_NAME, group.getGroupName());
		contentValues.put(TbCGroup.F_GROUP_DESC, group.getDesc());
		contentValues.put(TbCGroup.F_UID, group.getUid());
		if (group.getAvatar() != null) {
			contentValues.put(TbCGroup.F_AVATAR, group.getAvatar());
		}
		if (group.getIsMember() != null) {
			contentValues.put(TbCGroup.F_IS_MEMBER, group.getIsMember());
		}
		if (group.getIsAdmin() != null) {
			contentValues.put(TbCGroup.F_IS_ADMIN, group.getIsAdmin());
		}
		if (group.getIsSticked() != null) {
			contentValues.put(TbCGroup.F_IS_STICKED, group.getIsSticked() ? 1
					: 0);
		}
		if (group.getcUser() != 0) {
			contentValues.put(TbCGroup.F_C_USER, group.getcUser());
		}
		if (group.getcPosts() != 0) {
			contentValues.put(TbCGroup.F_C_POSTS, group.getcPosts());
		}
		if (group.getIsMessageAlert() != null) {
			contentValues.put(TbCGroup.F_IS_MESSAGE_ALERT,
					group.getIsMessageAlert() ? 0 : 1);
		}
		if (group.getIsMessageVoiceReminder() != null) {
			contentValues.put(TbCGroup.F_IS_MESSAGE_VOICEREMINDER,
					group.getIsMessageVoiceReminder() ? 0 : 1);
		}
		if (group.getInputType() != 0) {
			contentValues.put(TbCGroup.F_INPUT_TYPE, group.getInputType());
		}
		contentValues.put(TbCGroup.F_GROUP_TYPE, group.getGroupType());
		contentValues.put(TbCGroup.F_UNTREATED_COUNT, group.getUnreadCount());
		if (group.getStickedDate() != -1) {// -1为默认值表明未修改
			contentValues.put(TbCGroup.F_STICKED_DATE, group.getStickedDate());
		}
		contentValues.put(TbCGroup.F_IS_VISIBILITY, group.getIsVisibility());

		// 添加最后一条消息
		Type lastMessage = group.getLastMessageType();

		if (lastMessage != null) {
			contentValues
					.put(TbCGroup.F_LAST_SENDDATE, group.getLastSendDate());
			contentValues.put(TbCGroup.F_LAST_SEND_CONTENT,
					group.getLastMessageContent());
			contentValues.put(TbCGroup.F_LAST_SENDNICKNAME,
					group.getLastMemberNickName());
			contentValues.put(TbCGroup.F_LAST_SEND_TYPE, lastMessage.ordinal());
			contentValues.put(TbCGroup.F_LAST_SEND_UID,
					group.getLastMessageUid());
		}
		if (group.getLastSendDate() != 0) {
			contentValues
					.put(TbCGroup.F_LAST_SENDDATE, group.getLastSendDate());
		}
		contentValues.put(TbCGroup.F_RENAME, group.isReName() ? 1 : 0);
		return contentValues;
	}

	/**
	 * 更新群组最后一条消息（注意消息发送状态和群组最后一条消息表中的状态对应）
	 * 
	 * method name: updateCgroupLastCmessage function @Description: Parameters
	 * and return values description：
	 * 
	 * @param groupUid
	 * @param cmessage
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-15 下午4:32:32 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateCgroupLastCmessage(final String groupUid,
			final CMessage cmessage) throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues contentValues = new ContentValues();
				if (cmessage == null) {
					contentValues.put(TbCGroup.F_LAST_SEND_UID, "");
					contentValues.put(TbCGroup.F_LAST_SENDNICKNAME, "");
					contentValues.put(TbCGroup.F_LAST_SEND_CONTENT, "");
				} else {
					contentValues.put(TbCGroup.F_LAST_SEND_UID,
							cmessage.getUid());
					Type type = cmessage.getMessageType();
					contentValues.put(TbCGroup.F_LAST_SEND_TYPE, type.ordinal());
					contentValues.put(TbCGroup.F_LAST_SENDNICKNAME, cmessage
							.getMember().getNickName());
					switch (type) {
					case FROM_MAIL_INFO:
						String nickName = cmessage.getMailFromNickName();
						if (StringUtil.isEmpty(nickName)) {
							nickName = StringUtil.getPrdfixStr(cmessage
									.getMailFrom());
						}
						String content = String.format(MailChat.getInstance()
								.getString(R.string.mail_info_to_chat_subject),
								nickName, cmessage.getMailSubject());
						contentValues
								.put(TbCGroup.F_LAST_SEND_CONTENT, content);
						break;
					default:
						contentValues.put(TbCGroup.F_LAST_SEND_CONTENT,
								cmessage.getContent());
						break;
					}
					
					contentValues.put(TbCGroup.F_LAST_SENDDATE,
							cmessage.getSendTime());
					//特别提醒：注意消息发送状态和群组最后一条消息表中的状态对应。参照CMessage state和Cgroup messageState对比。
					int messageState =0;
					State state =cmessage.getMessageState();
					if(state!=null){
						switch (state) {
						case sendFail:
							messageState=1;
							break;
						case sending:
							messageState=2;
							break;
						case sendSuccess:
							messageState=0;
							break;
						default:
							break;
						}
					}
					contentValues.put(TbCGroup.F_SEND_STATE, messageState);
				}
				String whereClause = TbCGroup.F_UID + "=?";
				String whereArgs[] = new String[] { groupUid };
				int result = db.update(TbCGroup.TB_NAME, contentValues, whereClause,
						whereArgs);
				if(result>0){
					notifyChattingChange();
				}
				return null;
			}
		});
	}

	/**
	 * 修改群组信息
	 * 
	 * method name: updateCGroup function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param group
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-15 下午4:53:44 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateCGroup(final CGroup group)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues contentValues = parseValuesUpdateGroup(group);
				String whereClause = TbCGroup.F_UID + "=?";
				String whereArgs[] = new String[] { group.getUid() };
				db.update(TbCGroup.TB_NAME, contentValues, whereClause,
						whereArgs);
				// 添加群组所带的成员数据
				if (group.getMembers() != null && group.getMembers().size() > 0) {
					saveOrUpdateGroupMembers(group.getUid(), group.getMembers());
				}

				return null;
			}
		});
	}

	/**
	 * 添加或更新联系人
	 * 
	 * method name: saveOrUpdateGroupMembers function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param groupUid
	 * @param members
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-10-15 下午4:53:08 @Modified by：zhangyq
	 * @Description：
	 */
	public synchronized void saveOrUpdateGroupMembers(String groupUid,
			List<CGroupMember> members) {
		try {
			for (int i = 0; i < members.size(); i++) {
				CGroupMember member = members.get(i);
				saveOrUpdateGroupMember(groupUid, member);
			}
		} catch (Exception e) {
			Log.e(MailChat.LOG_TAG, this.getClass().getName()
					+ "saveOrUpdateGroupMembers " + e.toString());
		}
	}

	/**
	 * 更新或插入单个群组的成员 function @Description: TODO Parameters and return values
	 * description：
	 * 
	 * @param groupUid
	 * @param member
	 * @throws MessageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-15 下午4:58:45 @Modified by：zhangyq
	 * @Description：
	 */
	public synchronized void saveOrUpdateGroupMember(String groupUid,
			CGroupMember member) throws MessageException {
		ContentValues values = parseValuesMember(member);
		try {
			insertOrReplace(TbCMember.TB_NAME, values);
			// 创建关系
			saveCGroupCmember(groupUid, member.getUid(), member.isAdmin(),
					member.isInviteMember());
		} catch (Exception e) {
			throw new MessageException(
					MessageException.UNKNOW_DATABASE_EXCEPTION, e.getMessage());
		}

	}

	/**
	 * 执行插入操作, 如果已经存在相关记录(插入时发生约束冲突), 则进行整行更新(替换)
	 * 
	 * method name: insertOrReplace function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param tableName
	 * @param values
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-15 下午5:21:17 @Modified by：zhangyq
	 * @Description：
	 */
	public void insertOrReplace(final String tableName,
			final ContentValues values) throws UnavailableStorageException {
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					db.insertWithOnConflict(tableName, null, values,
							SQLiteDatabase.CONFLICT_REPLACE);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			new UnavailableStorageException("insert or replace fail.", e);
		}
	}

	private ContentValues parseValuesMember(CGroupMember member) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(TbCMember.F_UID, member.getUid());
		contentValues.put(TbCMember.F_EMAIL, member.getEmail());
		contentValues.put(TbCMember.F_NICK_NAME, member.getNickName());
		contentValues.put(TbCMember.F_AVATAR_HASH, member.getAvatarHash());
		return contentValues;
	}

	/**
	 * 添加群组和成员关系
	 * 
	 * method name: saveCGroupCmember function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param groupUid
	 * @param memberUid
	 * @param isAdmin
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-10-15 下午5:24:11 @Modified by：zhangyq
	 * @Description：
	 */
	public void saveCGroupCmember(String groupUid, String memberUid,
			boolean isAdmin, boolean isInvite) {
		ContentValues values = parseValuesGroupMember(groupUid, memberUid,
				isAdmin, isInvite);
		try {
			insertOrReplace(TbCGroup_CMember.TB_NAME, values);
		} catch (Exception e) {
			new UnavailableStorageException(this.getClass().getName()
					+ "saveCGroupCmember " + e.toString(), e);
		}
	}

	private ContentValues parseValuesGroupMember(String groupUid,
			String memberUid, boolean isAdmin, boolean isInvite) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(TbCGroup_CMember.F_CGROUP_UID, groupUid);
		contentValues.put(TbCGroup_CMember.F_CMEMBER_UID, memberUid);
		contentValues.put(TbCGroup_CMember.F_IS_ADMIN, isAdmin ? 1 : 0);
		contentValues.put(TbCGroup_CMember.F_IS_INVITE, isInvite ? 1 : 0);
		return contentValues;
	}

	/**
	 * 执行插入操作, 如果插入过程中发生了数据库约束冲突, 则不做任何事情
	 * 
	 * method name: insertOrIgnore function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param tableName
	 * @param values
	 * @return field_name long return type
	 * @History memory：
	 * @Date：2014-10-15 下午5:25:39 @Modified by：zhangyq
	 * @Description：
	 */
	public void insertOrIgnore(String tableName, ContentValues values) {
		try {
			insertWithConflict(tableName, values,
					SQLiteDatabase.CONFLICT_IGNORE);
		} catch (UnavailableStorageException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入数据（替换或忽略）
	 * 
	 * method name: insertWithConflict function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param tableName
	 * @param values
	 * @param conflictFlag
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-15 下午5:30:39 @Modified by：zhangyq
	 * @Description：
	 */
	public void insertWithConflict(final String tableName,
			final ContentValues values, final int conflictFlag)
			throws UnavailableStorageException {
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					db.insertWithOnConflict(tableName, null, values,
							conflictFlag);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			new UnavailableStorageException("insert or conflict fail.", e);
		}
	}

	/**
	 * 修改群组输入状态
	 * 
	 * method name: updateCGroupInputType function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param group
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-15 下午5:37:51 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateCGroupInputType(final CGroup group)
			throws UnavailableStorageException {
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					ContentValues contentValues = new ContentValues();
					if (group.getInputType() != 0) {
						contentValues.put(TbCGroup.F_INPUT_TYPE,
								group.getInputType());
					}
					String whereClause = TbCGroup.F_UID + "=?";
					String whereArgs[] = new String[] { group.getUid() };
					db.update(TbCGroup.TB_NAME, contentValues, whereClause,
							whereArgs);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			new UnavailableStorageException("update CGroup input status fail.",
					e);
		}
	}

	/**
	 * 获取群组信息
	 * 
	 * method name: getCGroup function @Description: TODO Parameters and return
	 * values description：
	 * 
	 * @param uid
	 * @return
	 * @throws MessagingException
	 *             field_name CGroup return type
	 * @History memory：
	 * @Date：2014-10-15 下午7:11:44 @Modified by：zhangyq
	 * @Description：
	 */
	public CGroup getCGroup(final String uid) throws MessagingException {
		final ArrayList<CGroup> mGroupList = new ArrayList<CGroup>();
		try {
			database.execute(false, new DbCallback<CGroup>() {
				public CGroup doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					CGroup mGroup = null;
					try {
						cursor = db.query(TbCGroup.TB_NAME, null,
								TbCGroup.F_UID + "=?", new String[] { uid },
								null, null, null);
						if (cursor != null && cursor.getCount() > 0) {
							while (cursor.moveToNext()) {
								mGroup = new CGroup();
								mGroup.setUid(cursor.getString(cursor
										.getColumnIndex(TbCGroup.F_UID)));
								mGroup.setGroupName(cursor.getString(cursor
										.getColumnIndex(TbCGroup.F_GROUP_NAME)));
								mGroup.setDesc(cursor.getString(cursor
										.getColumnIndex(TbCGroup.F_GROUP_DESC)));
								mGroup.setAvatar(cursor.getString(cursor
										.getColumnIndex(TbCGroup.F_AVATAR)));
								mGroup.setIsPriv(cursor.getInt(cursor
										.getColumnIndex(TbCGroup.F_IS_PRIV)) == 0 ? false
										: true);
								mGroup.setIsMember(cursor.getInt(cursor
										.getColumnIndex(TbCGroup.F_IS_MEMBER)) == 0 ? false
										: true);
								mGroup.setIsAdmin(cursor.getInt(cursor
										.getColumnIndex(TbCGroup.F_IS_ADMIN)) == 0 ? false
										: true);
								mGroup.setcUser(cursor.getInt(cursor
										.getColumnIndex(TbCGroup.F_C_USER)));
								mGroup.setcPosts(cursor.getInt(cursor
										.getColumnIndex(TbCGroup.F_C_POSTS)));
								mGroup.setGroupType(cursor.getInt(cursor
										.getColumnIndex(TbCGroup.F_GROUP_TYPE)));
								mGroup.setIsUntreated(cursor.getInt(cursor
										.getColumnIndex(TbCGroup.F_IS_UNTREATED)) == 0 ? false
										: true);
								mGroup.setLastMemberNickName(cursor.getString(cursor
										.getColumnIndex(TbCGroup.F_LAST_SENDNICKNAME)));
								mGroup.setLastMessageType(Type.values()[cursor.getInt(cursor
										.getColumnIndex(TbCGroup.F_LAST_SEND_TYPE))]);
								mGroup.setLastMessageContent(cursor.getString(cursor
										.getColumnIndex(TbCGroup.F_LAST_SEND_CONTENT)));
								mGroup.setLastMessageUid(cursor.getString(cursor
										.getColumnIndex(TbCGroup.F_LAST_SEND_UID)));
								mGroup.setLastSendDate(cursor.getLong(cursor
										.getColumnIndex(TbCGroup.F_LAST_SENDDATE)));
								mGroup.setIsSticked(cursor.getInt(cursor
										.getColumnIndex(TbCGroup.F_IS_STICKED)) == 0 ? false
										: true);
								mGroup.setStickedDate(cursor.getLong(cursor
										.getColumnIndex(TbCGroup.F_STICKED_DATE)));
								mGroup.setIsMessageAlert(cursor.getInt(cursor
										.getColumnIndex(TbCGroup.F_IS_MESSAGE_ALERT)) == 1 ? false
										: true);
								mGroup.setIsMessageVoiceReminder(cursor.getInt(cursor
										.getColumnIndex(TbCGroup.F_IS_MESSAGE_VOICEREMINDER)) == 1 ? false
										: true);
								mGroup.setUnreadCount(cursor.getInt(cursor
										.getColumnIndex(TbCGroup.F_UNTREATED_COUNT)));
								mGroup.setInputType(cursor.getInt(cursor
										.getColumnIndex(TbCGroup.F_INPUT_TYPE)));
								mGroup.setIsVisibility(cursor.getInt(cursor
										.getColumnIndex(TbCGroup.F_IS_VISIBILITY)));
								mGroup.setReName(cursor.getInt(cursor
										.getColumnIndex(TbCGroup.F_RENAME)) == 1 ? true
										: false);
								// holy shit
								// mGroup.setMembers(getCMembers(mGroup.getUid()));
								mGroupList.add(mGroup);
							}
						}
					} catch (Exception e) {
						throw new WrappedException(e);
					} finally {
						Utility.closeQuietly(cursor);
					}
					return mGroup;
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}
		return mGroupList.size() > 0 ? mGroupList.get(0) : null;
	}

	private List<CGroup> parserCursorCGroups(Cursor cursor) {
		List<CGroup> mGroupList = new ArrayList<CGroup>();
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				mGroupList.add(parseCursorCGroup(cursor));
			}
		}
		return mGroupList;
	}

	private CGroup parseCursorCGroup(Cursor cursor) {
		CGroup mGroup = new CGroup();
		mGroup.setUid(cursor.getString(cursor.getColumnIndex(TbCGroup.F_UID)));
		mGroup.setGroupName(cursor.getString(cursor
				.getColumnIndex(TbCGroup.F_GROUP_NAME)));
		mGroup.setDesc(cursor.getString(cursor
				.getColumnIndex(TbCGroup.F_GROUP_DESC)));
		mGroup.setAvatar(cursor.getString(cursor
				.getColumnIndex(TbCGroup.F_AVATAR)));
		mGroup.setIsPriv(cursor.getInt(cursor
				.getColumnIndex(TbCGroup.F_IS_PRIV)) == 0 ? false : true);
		mGroup.setIsMember(cursor.getInt(cursor
				.getColumnIndex(TbCGroup.F_IS_MEMBER)) == 0 ? false : true);
		mGroup.setIsAdmin(cursor.getInt(cursor
				.getColumnIndex(TbCGroup.F_IS_ADMIN)) == 0 ? false : true);
		mGroup.setcUser(cursor.getInt(cursor.getColumnIndex(TbCGroup.F_C_USER)));
		mGroup.setcPosts(cursor.getInt(cursor
				.getColumnIndex(TbCGroup.F_C_POSTS)));
		mGroup.setGroupType(cursor.getInt(cursor
				.getColumnIndex(TbCGroup.F_GROUP_TYPE)));
		mGroup.setIsUntreated(cursor.getInt(cursor
				.getColumnIndex(TbCGroup.F_IS_UNTREATED)) == 0 ? false : true);
		mGroup.setLastMemberNickName(cursor.getString(cursor
				.getColumnIndex(TbCGroup.F_LAST_SENDNICKNAME)));
		mGroup.setLastMessageType(Type.values()[cursor.getInt(cursor
				.getColumnIndex(TbCGroup.F_LAST_SEND_TYPE))]);
		mGroup.setLastMessageContent(cursor.getString(cursor
				.getColumnIndex(TbCGroup.F_LAST_SEND_CONTENT)));
		mGroup.setLastMessageUid(cursor.getString(cursor
				.getColumnIndex(TbCGroup.F_LAST_SEND_UID)));
		mGroup.setLastSendDate(cursor.getLong(cursor
				.getColumnIndex(TbCGroup.F_LAST_SENDDATE)));
		mGroup.setIsSticked(cursor.getInt(cursor
				.getColumnIndex(TbCGroup.F_IS_STICKED)) == 0 ? false : true);
		mGroup.setStickedDate(cursor.getLong(cursor
				.getColumnIndex(TbCGroup.F_STICKED_DATE)));
		mGroup.setIsMessageAlert(cursor.getInt(cursor
				.getColumnIndex(TbCGroup.F_IS_MESSAGE_ALERT)) == 1 ? false
				: true);
		mGroup.setIsMessageVoiceReminder(cursor.getInt(cursor
				.getColumnIndex(TbCGroup.F_IS_MESSAGE_VOICEREMINDER)) == 1 ? false
				: true);
		mGroup.setUnreadCount(cursor.getInt(cursor
				.getColumnIndex(TbCGroup.F_UNTREATED_COUNT)));
		mGroup.setInputType(cursor.getInt(cursor
				.getColumnIndex(TbCGroup.F_INPUT_TYPE)));
		mGroup.setIsVisibility(cursor.getInt(cursor
				.getColumnIndex(TbCGroup.F_IS_VISIBILITY)));
		mGroup.setReName(cursor.getInt(cursor.getColumnIndex(TbCGroup.F_RENAME)) == 1 ? true
				: false);
		mGroup.setDraftContent(cursor.getString(cursor
				.getColumnIndex(TbCGroup.F_DRAFT)));
		mGroup.setDraft(cursor.getInt(cursor
				.getColumnIndex(TbCGroup.F_IS_DRAFT)) == 1 ? true : false);
		mGroup.setMessageState(cursor.getInt(cursor.getColumnIndex(TbCGroup.F_SEND_STATE)));
		// holy shit
		// mGroup.setMembers(getCMembers(mGroup.getUid()));
		return mGroup;
	}

	/**
	 * 获取没有隐藏的群组
	 * 
	 * method name: getCGroups function @Description: TODO Parameters and return
	 * values description：
	 * 
	 * @return
	 * @throws MessagingException
	 *             field_name List<CGroup> return type
	 * @History memory：
	 * @Date：2014-10-15 下午7:37:03 @Modified by：zhangyq
	 * @Description：
	 */
	public List<CGroup> getCGroups() throws MessagingException {
		final List<CGroup> mGroupList = new ArrayList<CGroup>();
		try {
			database.execute(false, new DbCallback<List<CGroup>>() {

				public List<CGroup> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					try {
						String selection = TbCGroup.F_IS_VISIBILITY + "=?";
						String whererArg0[] = new String[] { String.valueOf(1) };

						String orderBy = TbCGroup.F_IS_STICKED + " desc ,"
								+ TbCGroup.F_STICKED_DATE + " desc ,"
								+ TbCGroup.F_LAST_SENDDATE + " desc";
						cursor = db.query(TbCGroup.TB_NAME, null, selection,
								whererArg0, null, null, orderBy, null);
						mGroupList.addAll(parserCursorCGroups(cursor));
						return mGroupList;
					} catch (Exception e) {
						throw new WrappedException(e);
					} finally {
						Utility.closeQuietly(cursor);
					}
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}
		return mGroupList;
	}

	/**
	 * 获取所有群组
	 * 
	 * method name: getAllCGroups function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @return
	 * @throws MessagingException
	 *             field_name List<CGroup> return type
	 * @History memory：
	 * @Date：2014-10-21 下午2:36:28 @Modified by：zhangyq
	 * @Description：
	 */
	public List<CGroup> getAllCGroups() throws MessagingException {
		final List<CGroup> mGroupList = new ArrayList<CGroup>();
		try {
			database.execute(false, new DbCallback<List<CGroup>>() {

				public List<CGroup> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					try {
						cursor = db.query(TbCGroup.TB_NAME, null, null, null,
								null, null, null);
						mGroupList.addAll(parserCursorCGroups(cursor));
						return mGroupList;
					} catch (Exception e) {
						throw new WrappedException(e);
					} finally {
						Utility.closeQuietly(cursor);
					}
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}
		return mGroupList;
	}

	/**
	 * 搜索指定群组
	 * 
	 * method name: searchCGroups function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param keyWord
	 * @return
	 * @throws MessagingException
	 *             field_name List<CGroup> return type
	 * @History memory：
	 * @Date：2014-10-15 下午8:07:15 @Modified by：zhangyq
	 * @Description：
	 */
	public List<CGroup> searchCGroups(final String keyWord)
			throws MessagingException {
		final List<CGroup> mGroupList = new ArrayList<CGroup>();
		try {
			database.execute(false, new DbCallback<List<CGroup>>() {

				public List<CGroup> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					try {
						StringBuffer sql = new StringBuffer();
						sql.append("SELECT * FROM ")
								.append(Columns.TbCGroup.TB_NAME)
								.append(" WHERE ")
								.append(Columns.TbCGroup.F_GROUP_NAME)
								.append(" LIKE ?");
						String whereArgs[] = new String[] { "%" + keyWord + "%", };
						cursor = db.rawQuery(sql.toString(), whereArgs);
						mGroupList.addAll(parserCursorCGroups(cursor));
						return mGroupList;
					} catch (Exception e) {
						throw new WrappedException(e);
					} finally {
						Utility.closeQuietly(cursor);
					}
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}
		return mGroupList;
	}

	/**
	 * 更改群组状态 删除群组，实质为不可见状态，有新消息自动恢复可见状态
	 * 
	 * method name: updateCGroupState function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param groupUid
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-15 下午8:25:34 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateCGroupState(final String groupUid)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TbCGroup.F_IS_VISIBILITY, 0);
				String whereClause = TbCGroup.F_UID + "=?";
				String[] whereArgs = new String[] { groupUid };
				db.update(TbCGroup.TB_NAME, contentValues, whereClause,
						whereArgs);

				return null;
			}
		});
	}

	/**
	 * 置顶/取消置顶群组
	 * 
	 * method name: stickGroup function @Description: TODO Parameters and return
	 * values description：
	 * 
	 * @param groupUid
	 * @param stick
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-21 下午2:38:56 @Modified by：zhangyq
	 * @Description：
	 */
	public void stickGroup(final String groupUid, final boolean stick)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues values = new ContentValues();
				values.put(TbCGroup.F_STICKED_DATE,
						stick ? System.currentTimeMillis() : 0);
				values.put(TbCGroup.F_IS_STICKED, stick ? 1 : 0);
				int result = db.update(TbCGroup.TB_NAME, values, TbCGroup.F_UID + "=?",
						new String[] { groupUid });
				if (result > 0) {
					notifyChattingChange();
				}
				return null;
			}
		});
	}

	/**
	 * 群消息提醒
	 * 
	 * method name: msgAlertGroup function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param groupUid
	 * @param isAlert
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-15 下午8:31:14 @Modified by：zhangyq
	 * @Description：
	 */
	public void msgAlertGroup(final String groupUid, final boolean isAlert)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues values = new ContentValues();
				values.put(TbCGroup.F_IS_MESSAGE_ALERT, isAlert ? 0 : 1);
				int result = db.update(TbCGroup.TB_NAME, values, TbCGroup.F_UID + "=?",
						new String[] { groupUid });
				if(result>0){
					notifyChattingChange();
				}
				return null;
			}
		});
	}

	/**
	 * 群消息声音提醒
	 * 
	 * method name: msgVoiceReminderGroup function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param groupUid
	 * @param isAlert
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 上午11:26:00 @Modified by：zhangyq
	 * @Description：
	 */
	public void msgVoiceReminderGroup(final String groupUid,
			final boolean isAlert) throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues values = new ContentValues();
				values.put(TbCGroup.F_IS_MESSAGE_VOICEREMINDER, isAlert ? 0 : 1);
				db.update(TbCGroup.TB_NAME, values, TbCGroup.F_UID + "=?",
						new String[] { groupUid });

				return null;
			}
		});
	}

	/**
	 * 获取单个群成员
	 * 
	 * method name: getCMember function @Description: TODO Parameters and return
	 * values description：
	 * 
	 * @param cMemberUid
	 * @return
	 * @throws UnavailableStorageException
	 * @throws MessagingException
	 *             field_name List<CGroupMember> return type
	 * @History memory：
	 * @Date：2014-10-17 上午11:31:22 @Modified by：zhangyq
	 * @Description：
	 */
	public List<CGroupMember> getCMember(final String cMemberUid) throws UnavailableStorageException  {
		return database.execute(false, new DbCallback<List<CGroupMember>>() {

			public List<CGroupMember> doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				Cursor cursor = null;
				try {
					String selection = TbCMember.F_UID + "=?";
					cursor = db.query(TbCMember.TB_NAME, null, selection,
							new String[] { cMemberUid }, TbCMember.F_EMAIL, null,
							null, null);
					return parserCursorCMember(cursor);
				} finally {
					Utility.closeQuietly(cursor);
				}
			}
		});
	}

	private List<CGroupMember> parserCursorCMember(Cursor cursor) {
		List<CGroupMember> mCMembers = new ArrayList<CGroupMember>();
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				CGroupMember mCmember = new CGroupMember();
				mCmember.setUid(cursor.getString(cursor
						.getColumnIndex(TbCMember.F_UID)));
				mCmember.setEmail(cursor.getString(cursor
						.getColumnIndex(TbCMember.F_EMAIL)));
				mCmember.setAvatarHash(cursor.getString(cursor
						.getColumnIndex(TbCMember.F_AVATAR_HASH)));
				mCmember.setNickName(cursor.getString(cursor
						.getColumnIndex(TbCMember.F_NICK_NAME)));
				if (cursor.getColumnIndex(TbCGroup_CMember.F_IS_ADMIN) != -1) {
					mCmember.setAdmin(cursor.getInt(cursor
							.getColumnIndex(TbCGroup_CMember.F_IS_ADMIN)) == 0 ? false
							: true);
				}
				if (cursor.getColumnIndex(TbCGroup_CMember.F_IS_INVITE) != -1) {
					mCmember.setInviteMember(cursor.getInt(cursor
							.getColumnIndex(TbCGroup_CMember.F_IS_INVITE)) == 0 ? false
							: true);
				}
				mCMembers.add(mCmember);
			}
		}
		return mCMembers;
	}

	/**
	 * 获取此组内所有成员
	 * 
	 * method name: getCMembers function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param groupUid
	 * @return
	 * @throws UnavailableStorageException
	 * @throws MessagingException
	 *             field_name List<CGroupMember> return type
	 * @History memory：
	 * @Date：2014-10-17 上午11:36:36 @Modified by：zhangyq
	 * @Description：
	 */
	public List<CGroupMember> getCMembers(final String groupUid)throws UnavailableStorageException {
		return database.execute(false, new DbCallback<List<CGroupMember>>() {

			public List<CGroupMember> doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				// 语句
				StringBuffer sql = new StringBuffer();
				sql.append("select * from ").append(TbCMember.TB_NAME)
						.append(",").append(TbCGroup_CMember.TB_NAME)
						.append(" where (").append(TbCGroup_CMember.TB_NAME)
						.append(".").append(TbCGroup_CMember.F_CGROUP_UID)
						.append("=?) and (").append(TbCMember.TB_NAME)
						.append(".").append(TbCMember.F_UID).append("=")
						.append(TbCGroup_CMember.TB_NAME).append(".")
						.append(TbCGroup_CMember.F_CMEMBER_UID).append(")");
				Cursor cursor = null;
				try {
					cursor = db.rawQuery(sql.toString(),
							new String[] { groupUid });
					return parserCursorCMember(cursor);
				} finally {
					Utility.closeQuietly(cursor);
				}
			}
		});
	}

	/**
	 * 批量保存群组消息
	 * 
	 * method name: saveGroupMessages function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param groupUid
	 * @param messages
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 上午11:43:25 @Modified by：zhangyq
	 * @Description：
	 */
	public void saveGroupMessages(final String groupUid,
			final List<CMessage> messages) throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				for (CMessage message : messages) {
					try {
						saveOrUpdateCMessageAndCAttach(groupUid, message); // TODO:应该用事物
					} catch (MessageException e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		});
	}

	/**
	 * 添加消息
	 * 
	 * method name: saveOrUpdateCMessageAndCAttach function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param groupUid
	 * @param message
	 * @throws MessageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 上午11:45:05 @Modified by：zhangyq
	 * @Description：
	 */
	public synchronized void saveOrUpdateCMessageAndCAttach(String groupUid,
			CMessage message) throws MessageException {
		// 保存成员
		// CGroupMember mMember = message.getMember();
		// if (mMember != null) {
		// saveOrUpdateGroupMember(groupUid, message.getMember());
		// }
		ContentValues initialValues = new ContentValues();
		initialValues.put(TbCMessages.F_UID, message.getUid());
		initialValues.put(TbCMessages.F_CGROUP_UID, groupUid);
		initialValues.put(TbCMessages.F_CMEMBER_UID, message.getMember()
				.getUid());
		int dbMessageType = message.getMessageType().ordinal();
		initialValues.put(TbCMessages.F_MESSAGETYPE, dbMessageType);
		initialValues.put(TbCMessages.F_SENDTIME, message.getSendTime());
		initialValues.put(TbCMessages.F_CONTENT, message.getContent());
		initialValues.put(TbCMessages.F_LON, message.getLongitude());
		initialValues.put(TbCMessages.F_LAT, message.getLatitude());
		initialValues.put(TbCMessages.F_ADDRESS, message.getAddress());
		initialValues.put(TbCMessages.F_LOCATION_NAME,
				message.getLocationName());
		initialValues.put(TbCMessages.F_DELETE_FLAG, message.getDelete_flag());

		if (message.getMessageState() != null) {
			initialValues.put(TbCMessages.F_MESSAGE_STATE, message
					.getMessageState().ordinal());
		}
		initialValues.put(TbCMessages.F_SERVER_MESSAGE_TYPE,message.getServerMessageType());
		initialValues.put(TbCMessages.F_MAIL_FROM_EMAIL,message.getMailFrom());
		initialValues.put(TbCMessages.F_MAIL_FROM_NICKNAME,message.getMailFromNickName());
		initialValues.put(TbCMessages.F_MAIL_SUBJECT,message.getMailSubject());
		initialValues.put(TbCMessages.F_MAIL_PREVIEW,message.getMailPreview());
		try {
			insertOrReplace(TbCMessages.TB_NAME, initialValues);
		} catch (UnavailableStorageException e1) {
			e1.printStackTrace();
		}
		// 判断是否是附件
		Type messageType = message.getMessageType();
		if (messageType == Type.VOICE || messageType == Type.IMAGE
				|| messageType == Type.ATTACHMENT) {
			CAttachment mAttachment = message.getAttachment();
			ContentValues attachmentsValues = new ContentValues();
			attachmentsValues.put(TbCAttachments.F_CGROUP_UID, groupUid);
			attachmentsValues.put(TbCAttachments.F_CMESSAGES_UID,
					message.getUid());
			attachmentsValues.put(TbCAttachments.F_UID,
					mAttachment.getAttchmentId());
			attachmentsValues.put(TbCAttachments.F_NAME, mAttachment.getName());
			attachmentsValues.put(TbCAttachments.F_FILE_ID,
					mAttachment.getFileid());
			attachmentsValues.put(TbCAttachments.F_SIZE, mAttachment.getSize());
			attachmentsValues.put(TbCAttachments.F_DOWNLOAD_STATE,
					mAttachment.getDownloadState());
			attachmentsValues.put(TbCAttachments.F_MIME_TYPE,
					mAttachment.getContentType());
			attachmentsValues.put(TbCAttachments.F_FILE_PATH,
					mAttachment.getFilePath());
			attachmentsValues.put(TbCAttachments.F_VOICE_LENGTH,
					mAttachment.getVoiceLength());
			attachmentsValues.put(TbCAttachments.F_READ_FLAG, 0);
			// 保存本地图片附件的标记
			if (mAttachment.getLocalPathFlag() != 0) {
				attachmentsValues.put(TbCAttachments.F_LOCALPATH_FLAG,
						mAttachment.getLocalPathFlag());
			}
			if (mAttachment.getForwardFlag() != 0) {
				attachmentsValues.put(TbCAttachments.F_FORWARD_FLAG,
						mAttachment.getForwardFlag());
			}
			attachmentsValues.put(TbCAttachments.F_WIDTH, mAttachment.getImageWidth());
			attachmentsValues.put(TbCAttachments.F_HEIGHT, mAttachment.getImageHeight());
			attachmentsValues.put(TbCAttachments.F_IS_IMAGE_LOAD, mAttachment.isImageLoad() ? 0 : 1);
			attachmentsValues.put(TbCAttachments.F_DOWNLOAD_PAUSE_FLAG, mAttachment.isDownloadPause()? 1 : 0);
			try {
				insertOrReplace(TbCAttachments.TB_NAME, attachmentsValues);
			} catch (UnavailableStorageException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * 判断是否存在此附件信息
	 * 
	 * method name: isExistsCAttachmentUid function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param attachmentUid
	 * @return
	 * @throws MessagingException
	 *             field_name Boolean return type
	 * @History memory：
	 * @Date：2014-10-17 上午11:52:31 @Modified by：zhangyq
	 * @Description：
	 */
	public Boolean isExistsCAttachmentUid(final String attachmentUid)
			throws MessagingException {
		final Boolean isExists = false;
		try {
			database.execute(false, new DbCallback<Boolean>() {

				public Boolean doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					try {
						String selection = TbCAttachments.F_UID + "=?";
						cursor = db.query(TbCAttachments.TB_NAME,
								new String[] { TbCAttachments.F_ID },
								selection, new String[] { attachmentUid },
								null, null, null);
						return cursor.moveToFirst();
					} catch (Exception e) {
						throw new WrappedException(e);
					} finally {
						Utility.closeQuietly(cursor);
					}
				}
			});
		} catch (WrappedException e) {
			e.printStackTrace();
			// throw new MessagingException(TAG, e.getCause());
		}
		return isExists;
	}

	/**
	 * 更新附件消息外键
	 * 
	 * method name: updateAttachMessageUid function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param message
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午1:11:09 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateAttachMessageUid(final CMessage message)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				Type messageType = message.getMessageType();
				if (messageType == Type.VOICE || messageType == Type.IMAGE
						|| messageType == Type.ATTACHMENT) {
					CAttachment mAttachment = message.getAttachment();
					ContentValues contentValues = new ContentValues();
					contentValues.put(TbCAttachments.F_CMESSAGES_UID,
							message.getUid());
					String whereClause = TbCAttachments.F_UID + "=?";
					String[] whereArgs = { mAttachment.getAttchmentId() };
					db.update(TbCAttachments.TB_NAME, contentValues,
							whereClause, whereArgs);
				}
				return null;
			}
		});
	}

	/**
	 * 更新删除标记
	 * 
	 * method name: updateCMessageDeleteFlag function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param messageUid
	 * @param flag
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午1:15:53 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateCMessageDeleteFlag(final String messageUid, final int flag)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TbCMessages.F_DELETE_FLAG, flag);
				String whereClause = TbCMessages.F_UID + "=?";
				String[] whereArgs = { messageUid };
				db.update(TbCMessages.TB_NAME, contentValues, whereClause,
						whereArgs);
				return null;
			}
		});
	}

	public void updateAllCMessageDeleteFlag(final String cGroupUid, final int flag)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TbCMessages.F_DELETE_FLAG, flag);
				String whereClause = TbCMessages.F_CGROUP_UID + "=?";
				String[] whereArgs = { cGroupUid };
				db.update(TbCMessages.TB_NAME, contentValues, whereClause,
						whereArgs);
				return null;
			}
		});
	}

	/**
	 * 更新消息状态
	 * 
	 * method name: updateCMessageState function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param messageUid
	 * @param state
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午1:17:28 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateCMessageState(final String messageUid, final State state)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TbCMessages.F_MESSAGE_STATE, state.ordinal());
				String whereClause = TbCMessages.F_UID + "=?";
				String[] whereArgs = { messageUid };
				db.update(TbCMessages.TB_NAME, contentValues, whereClause,
						whereArgs);
				return null;
			}
		});
	}

	/**
	 * 更新消息发送到服务器时间
	 * 
	 * method name: updateCMessageState function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param messageUid
	 * @param state
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2015-1-16 下午1:17:28 @Modified by：shengli
	 * @Description：
	 */
	public void updateCMessageTime(final String messageUid, final long time)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TbCMessages.F_SENDTIME, time);
				String whereClause = TbCMessages.F_UID + "=?";
				String[] whereArgs = { messageUid };
				db.update(TbCMessages.TB_NAME, contentValues, whereClause,
						whereArgs);
				return null;
			}
		});
	}
	
	/**
	 * 更新newUid F_MESSAGE_STATE
	 * 
	 * method name: updateCMessageWhenSendOK function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param oldUid
	 * @param newUid
	 * @param message
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午1:23:07 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateCMessageWhenSendOK(final String oldUid,
			final String newUid, final CMessage message)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				try {
					String whereClause = TbCMessages.F_UID + "=?";
					updateAttachMessageUid(message);
					ContentValues contentValues = new ContentValues();
					contentValues.put(TbCMessages.F_UID, newUid);
					contentValues.put(TbCMessages.F_MESSAGE_STATE,
							State.sendSuccess.ordinal());
					String[] whereArgs = { oldUid };
					db.update(TbCMessages.TB_NAME, contentValues, whereClause,
							whereArgs);
				} catch (UnavailableStorageException e) {
					// 删除旧的id
					try {
						deleteCMessagesByMsgUid(oldUid);
					} catch (UnavailableStorageException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
				return null;
			}
		});
	}

	/**
	 * 获取当前群组消息内容
	 * 
	 * method name: getCMessages function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param groupUid
	 * @param lastId
	 * @param pageSize
	 * @return
	 * @throws MessagingException
	 *             field_name List<CMessage> return type
	 * @History memory：
	 * @Date：2014-10-17 下午1:30:22 @Modified by：zhangyq
	 * @Description：
	 */
	public List<CMessage> getCMessages(final String groupUid,
			final String lastId, final int pageSize) throws MessagingException {
		final List<CMessage> messages = new ArrayList<CMessage>();
		try {
			database.execute(false, new DbCallback<List<CMessage>>() {

				public List<CMessage> doDbWork(final SQLiteDatabase db)
						throws WrappedException {

					StringBuffer sqlBuffer = new StringBuffer();
					sqlBuffer.append("SELECT * FROM " + TbCMessages.TB_NAME)
							.append(" WHERE ")
							.append(TbCMessages.F_CGROUP_UID + "=? and ")
							.append(TbCMessages.F_DELETE_FLAG + "=0")
							.append(" ORDER BY ")
							.append(TbCMessages.F_SENDTIME).append("  desc")
							.append(" LIMIT " + String.valueOf(pageSize));
					Cursor cursor = null;
					try {
						cursor = db.rawQuery(sqlBuffer.toString(),
								new String[] { groupUid });
						List<CMessage> cMessages = parserCursorCMessages(
								groupUid, cursor);
						messages.addAll(cMessages);
						Collections.reverse(messages);
					} catch (Exception e) {
						throw new WrappedException(e);
					} finally {
						Utility.closeQuietly(cursor);
					}
					return messages;
				}
			});
		} catch (WrappedException e) {
			e.printStackTrace();
			// throw new MessagingException(TAG, e.getCause());
		}
		return messages;
	}

	private List<CMessage> parserCursorCMessages(String groupUid, Cursor cursor) {
		List<CMessage> messages = new ArrayList<CMessage>();
		if (cursor != null && cursor.getCount() > 0) {
			cMembersTmpMap.clear();// 暂时todo 为了保持最新
			while (cursor.moveToNext()) {
				CMessage message = new CMessage();
				message.setUid(cursor.getString(cursor
						.getColumnIndex(TbCMessages.F_UID)));
				// message.setSenderUid(cursor.getString(cursor.getColumnIndex(TbCMessages.F_CMEMBER_UID)));
				message.setSendTime(cursor.getLong(cursor
						.getColumnIndex(TbCMessages.F_SENDTIME)));
				message.setGroupUid(cursor.getString(cursor
						.getColumnIndex(TbCMessages.F_CGROUP_UID)));
				Type type = Type.values()[cursor.getInt((cursor
						.getColumnIndex(TbCMessages.F_MESSAGETYPE)))];
				message.setMessageType(type);
				message.setLatitude(cursor.getDouble(cursor
						.getColumnIndex(TbCMessages.F_LAT)));
				message.setLongitude(cursor.getDouble(cursor
						.getColumnIndex(TbCMessages.F_LON)));
				message.setAddress(cursor.getString(cursor
						.getColumnIndex(TbCMessages.F_ADDRESS)));
				message.setLocationName(cursor.getString(cursor
						.getColumnIndex(TbCMessages.F_LOCATION_NAME)));
				message.setContent(cursor.getString(cursor
						.getColumnIndex(TbCMessages.F_CONTENT)));
				message.setMessageState(State.values()[cursor.getInt(cursor
						.getColumnIndex(TbCMessages.F_MESSAGE_STATE))]);
				message.setMailFrom(cursor.getString(cursor
						.getColumnIndex(TbCMessages.F_MAIL_FROM_EMAIL)));
				message.setMailFromNickName(cursor.getString(cursor
						.getColumnIndex(TbCMessages.F_MAIL_FROM_NICKNAME)));
				message.setMailSubject(cursor.getString(cursor
						.getColumnIndex(TbCMessages.F_MAIL_SUBJECT)));
				message.setMailPreview(cursor.getString(cursor
						.getColumnIndex(TbCMessages.F_MAIL_PREVIEW)));

				if (!type.equals(Type.TEXT)) {// 优化db：纯文本不用再查询附件表
					try {
						message.setAttachment(getCAttachment(message.getUid()));
					} catch (UnavailableStorageException e) {
						e.printStackTrace();
					}
				}
				message.setDelete_flag(cursor.getInt(cursor
						.getColumnIndex(TbCMessages.F_DELETE_FLAG)));
				try {
					message.setMember(getCMember(
							cursor.getString(cursor
									.getColumnIndex(TbCMessages.F_CMEMBER_UID)))
							.get(0));
				} catch (MessagingException e) {
					e.printStackTrace();
				}
				messages.add(message);
			}
			cMembersTmpMap.clear();// 暂时todo 为了保持最新
		}

		return messages;
	}

	/**
	 * 获取此群组中某成员的消息内容
	 * 
	 * method name: getMemberCMessages function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param groupUid
	 * @param membersUid
	 * @return
	 * @throws MessagingException
	 *             field_name List<CMessage> return type
	 * @History memory：
	 * @Date：2014-10-17 下午1:39:27 @Modified by：zhangyq
	 * @Description：
	 */
	public List<CMessage> getMemberCMessages(final String groupUid,
			final String membersUid) throws MessagingException {
		final List<CMessage> messages = new ArrayList<CMessage>();
		try {
			database.execute(false, new DbCallback<List<CMessage>>() {

				public List<CMessage> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					StringBuffer sqlBuffer = new StringBuffer();
					sqlBuffer.append("SELECT * FROM " + TbCMessages.TB_NAME);
					sqlBuffer.append(" WHERE ");
					sqlBuffer.append(TbCMessages.F_CGROUP_UID + "=?");
					sqlBuffer
							.append(" and " + TbCMessages.F_CMEMBER_UID + "=?");
					sqlBuffer
							.append(" and " + TbCMessages.F_DELETE_FLAG + "=0");
					sqlBuffer.append("  ORDER BY " + TbCMessages.F_SENDTIME
							+ " desc");

					Cursor cursor = null;
					try {
						cursor = db.rawQuery(sqlBuffer.toString(),
								new String[] { groupUid, membersUid });
						messages.addAll(parserCursorCMessages(groupUid, cursor));
						return messages;
					} catch (Exception e) {
						throw new WrappedException(e);
					} finally {
						Utility.closeQuietly(cursor);
					}
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}
		return messages;
	}

	/**
	 * 获取混合聊天列表
	 * 
	 * method name: listMixedChatting function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @return field_name List<MixedChatting> return type
	 * @History memory：
	 * @Date：2014-10-17 下午2:00:38 @Modified by：zhangyq
	 * @Description：
	 */
	public List<MixedChatting> listMixedChatting() {
		ArrayList<MixedChatting> mixedChattings = new ArrayList<MixedChatting>();
		Cursor dchatCursor = null;
		Cursor groupCursor = null;
		try {
			groupCursor = loadCGroupCursor();
			dchatCursor = listDchatsCursor();
			while (groupCursor.moveToNext()) {
				mixedChattings.add(MixedChatting
						.build(parseCursorCGroup(groupCursor)));
			}
			while (dchatCursor.moveToNext()) {
				mixedChattings.add(MixedChatting
						.build(parseCursorDChat(dchatCursor)));
			}
		} catch (Exception e) {
			Log.e("e", e.toString());
		} finally {
			GlobalTools.closeCursor(dchatCursor);
			GlobalTools.closeCursor(groupCursor);
		}
		return sortMixedChatting(mixedChattings);
	}

	/**
	 * 获取单聊列表数据库结果集
	 * 
	 * method name: listDchatsCursor function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @return
	 * @throws MessagingException
	 *             field_name Cursor return type
	 * @History memory：
	 * @Date：2014-10-17 下午1:55:10 @Modified by：zhangyq
	 * @Description：
	 */
	private Cursor listDchatsCursor() throws MessagingException {
		final List<Cursor> cursorList = new ArrayList<Cursor>();
		try {
			database.execute(false, new DbCallback<List<Cursor>>() {

				public List<Cursor> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					String sql = "SELECT d.*, c.f_nick_name, c.f_img_head, c.f_img_head_hash FROM tb_d_chatlist d LEFT JOIN tb_user_contacts c ON (d.f_to_email = c.f_email) WHERE d.f_is_visibility = ? ORDER BY d.f_is_sticked DESC, d.f_sticked_date DESC, d.f_last_time DESC";
					try {
						cursor = db.rawQuery(sql,
								new String[] { String.valueOf(1) });
						cursorList.add(cursor);
					} catch (Exception e) {
						throw new WrappedException(e);
					}
					return cursorList;
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}
		return (cursorList == null || cursorList.isEmpty()) ? null : cursorList
				.get(0);
	}

	/**
	 * 获取群聊列表数据库结果集
	 * 
	 * method name: loadCGroupCursor function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @return
	 * @throws MessagingException
	 *             field_name Cursor return type
	 * @History memory：
	 * @Date：2014-10-17 下午1:58:09 @Modified by：zhangyq
	 * @Description：
	 */
	private Cursor loadCGroupCursor() throws MessagingException {
		final List<Cursor> cursorList = new ArrayList<Cursor>();
		try {
			database.execute(false, new DbCallback<List<Cursor>>() {

				public List<Cursor> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					String selection = TbCGroup.F_IS_VISIBILITY + "=?";
					String whererArg0[] = new String[] { String.valueOf(1) };

					String orderBy = TbCGroup.F_IS_STICKED + " desc ,"
							+ TbCGroup.F_STICKED_DATE + " desc ,"
							+ TbCGroup.F_LAST_SENDDATE + " desc";
					Cursor cursor = null;
					try {
						cursor = db.query(TbCGroup.TB_NAME, null, selection,
								whererArg0, null, null, orderBy, null);
						cursorList.add(cursor);
					} catch (Exception e) {
						throw new WrappedException(e);
					}
					return cursorList;
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}
		return (cursorList == null || cursorList.isEmpty()) ? null : cursorList
				.get(0);
	}

	private DChat parseCursorDChat(Cursor cursor) {
		DChat dChat = new DChat();
		dChat.setUid(cursor.getString(cursor.getColumnIndex(TbDchat.F_UID)));
		dChat.setLastMessage(cursor.getString(cursor
				.getColumnIndex(TbDchat.F_LAST_MESSAGE)));
		dChat.setLastMessageEmail(cursor.getString(cursor
				.getColumnIndex(TbDchat.F_LAST_MESSAGE_EMAIL)));
		dChat.setLastTime(cursor.getLong(cursor
				.getColumnIndex(TbDchat.F_LAST_TIME)));
		dChat.setEmail(cursor.getString(cursor
				.getColumnIndex(TbDchat.F_TO_EMAIL)));
		dChat.setStickedDate(cursor.getLong(cursor
				.getColumnIndex(TbDchat.F_STICKED_DATE)));
		dChat.setSticked(cursor.getInt(cursor
				.getColumnIndex(TbDchat.F_IS_STICKED)) == 1 ? true : false);
		dChat.setDChatAlert(cursor.getInt(cursor.getColumnIndex(TbDchat.F_IS_DCHAT_ALERT)) == 1 ? true
				: false);
		DChatMessage.Type type = DChatMessage.Type.values()[cursor
				.getInt((cursor.getColumnIndex(TbDchat.F_LAST_MESSAGE_TYPE)))];
		dChat.setLastMessageType(type);
		dChat.setUnReadCount(cursor.getInt(cursor
				.getColumnIndex(TbDchat.F_UNREADCOUNT)));
		dChat.setNickName(cursor.getString(cursor
				.getColumnIndex("f_nick_name")));
		dChat.setImgHead(cursor.getString(cursor.getColumnIndex("f_img_head")));
		dChat.setImgHeadHash(cursor.getString(cursor
				.getColumnIndex("f_img_head_hash")));
		dChat.setDraftContent(cursor.getString(cursor
				.getColumnIndex(TbDchat.F_DRAFT)));
		dChat.setDraft(cursor.getInt(cursor.getColumnIndex(TbDchat.F_IS_DRAFT)) == 1 ? true
				: false);
		dChat.setMessageState(cursor.getInt(cursor.getColumnIndex(TbDchat.F_SEND_STATE)));
		dChat.setInputType(cursor.getInt(cursor.getColumnIndex(TbCGroup.F_INPUT_TYPE)));
		dChat.setVisibility(cursor.getInt(cursor.getColumnIndex(TbCGroup.F_IS_VISIBILITY))==1);
		dChat.setdChatType(DChat.Type.values()[cursor.getInt((cursor.getColumnIndex(TbDchat.F_DCHAT_TYPE)))]);
		return dChat;
	}

	/**
	 * 给混合聊天列表排序
	 * 
	 * method name: sortMixedChatting function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param mixedChattings
	 * @return field_name ArrayList<MixedChatting> return type
	 * @History memory：
	 * @Date：2014-10-17 下午1:59:48 @Modified by：zhangyq
	 * @Description：
	 */
	public List<MixedChatting> sortMixedChatting(
			List<MixedChatting> mixedChattings) {
		// 先按时间排序
		for (int i = 0; i < mixedChattings.size() - 1; i++) {
			for (int j = i + 1; j < mixedChattings.size(); j++) {
				if (mixedChattings.get(i).isGroup()
						&& mixedChattings.get(j).isGroup()) {
					if (mixedChattings.get(i).getGroup().getLastSendDate() < mixedChattings
							.get(j).getGroup().getLastSendDate()) {
						Collections.swap(mixedChattings, i, j);
					}
				} else if (!mixedChattings.get(i).isGroup()
						&& mixedChattings.get(j).isGroup()) {
					if (mixedChattings.get(i).getDchat().getLastTime() < mixedChattings
							.get(j).getGroup().getLastSendDate()) {
						Collections.swap(mixedChattings, i, j);
					}
				} else if (mixedChattings.get(i).isGroup()
						&& !mixedChattings.get(j).isGroup()) {
					if (mixedChattings.get(i).getGroup().getLastSendDate() < mixedChattings
							.get(j).getDchat().getLastTime()) {
						Collections.swap(mixedChattings, i, j);
					}
				} else if (!mixedChattings.get(i).isGroup()
						&& !mixedChattings.get(j).isGroup()) {
					if (mixedChattings.get(i).getDchat().getLastTime() < mixedChattings
							.get(j).getDchat().getLastTime()) {
						Collections.swap(mixedChattings, i, j);
					}
				}
			}
		}
		// 再将置顶筛选出来
		List<MixedChatting> removeMixedChattings = new ArrayList<MixedChatting>();
		for (MixedChatting mixedChatting : mixedChattings) {
			if (mixedChatting.isGroup()) {
				if (mixedChatting.getGroup().getIsSticked()) {
					removeMixedChattings.add(mixedChatting);
				}
			} else {
				if (mixedChatting.getDchat().isSticked()) {
					removeMixedChattings.add(mixedChatting);
				}
			}
		}
		mixedChattings.removeAll(removeMixedChattings);
		// 再按置顶时间排序
		for (int i = 0; i < removeMixedChattings.size() - 1; i++) {
			for (int j = i + 1; j < removeMixedChattings.size(); j++) {
				if (removeMixedChattings.get(i).isGroup()
						&& removeMixedChattings.get(j).isGroup()) {
					if (removeMixedChattings.get(i).getGroup().getStickedDate() < removeMixedChattings
							.get(j).getGroup().getStickedDate()) {
						Collections.swap(removeMixedChattings, i, j);
					}
				} else if (!removeMixedChattings.get(i).isGroup()
						&& removeMixedChattings.get(j).isGroup()) {
					if (removeMixedChattings.get(i).getDchat().getStickedDate() < removeMixedChattings
							.get(j).getGroup().getStickedDate()) {
						Collections.swap(removeMixedChattings, i, j);
					}
				} else if (removeMixedChattings.get(i).isGroup()
						&& !removeMixedChattings.get(j).isGroup()) {
					if (removeMixedChattings.get(i).getGroup().getStickedDate() < removeMixedChattings
							.get(j).getDchat().getStickedDate()) {
						Collections.swap(removeMixedChattings, i, j);
					}
				} else if (!removeMixedChattings.get(i).isGroup()
						&& !removeMixedChattings.get(j).isGroup()) {
					if (removeMixedChattings.get(i).getDchat().getStickedDate() < removeMixedChattings
							.get(j).getDchat().getStickedDate()) {
						Collections.swap(removeMixedChattings, i, j);
					}
				}
			}
		}
		mixedChattings.addAll(0, removeMixedChattings);
		removeMixedChattings = null;
		return mixedChattings;
	}

	/*------------------------我是华丽的分割线--------------------------*/

	/**
	 * <<<<<<< .mine 移除置顶群组 ======= 隐藏群组 >>>>>>> .r45402
	 * 
	 * method name: hiddenGroup function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param groupUid
	 * @param hidden
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午2:59:06 @Modified by：zhangyq
	 * @Description：
	 */
	public void hiddenGroup(final String groupUid, final boolean hidden)
			throws UnavailableStorageException {

		final ContentValues cv = new ContentValues();
		cv.put(TbCGroup.F_IS_VISIBILITY, hidden ? 0 : 1);
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(SQLiteDatabase db) throws WrappedException,
					UnavailableStorageException {
				int result = db.update(TbCGroup.TB_NAME, cv, TbCGroup.F_UID + "=?",
						new String[] { groupUid });
				if(result>0){
					notifyChattingChange();
				}
				return null;
			}
		});
	}

	/**
	 * 删除群组/退出群组
	 * 
	 * method name: deleteCGroupAllInfo function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param groupUid
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午3:08:08 @Modified by：zhangyq
	 * @Description：
	 */
	public void deleteCGroupAllInfo(String groupUid) {
		try {
			// 删除群
			deleteCGroup(groupUid);
			// 删除此组内所有的聊天内容、包括附件
			deleteCMessagesByCgroupUid(groupUid);
		} catch (UnavailableStorageException e) {
			e.printStackTrace();
		}
	}

	// 删除群组
	public void deleteCGroup(final String groupUid)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			public Void doDbWork(SQLiteDatabase db) throws WrappedException,
					UnavailableStorageException {
				// 删除所有关系
				deleteCGroupCmembers(groupUid);
				// 删除群
				String selection = TbCGroup.F_UID + "=?";
				int count = db.delete(TbCGroup.TB_NAME, selection,
						new String[] { groupUid });
				if(count>0){
					notifyChattingChange();
				}
				return null;
			}
		});
	}

	/**
	 * 删除此群组所有消息//包括附件
	 * 
	 * method name: deleteCMessagesByCgroupUid function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param gruopUid
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午3:20:19 @Modified by：zhangyq
	 * @Description：
	 */
	public void deleteCMessagesByCgroupUid(final String gruopUid)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			public Void doDbWork(SQLiteDatabase db) throws WrappedException,
					UnavailableStorageException {
				String selection = TbCMessages.F_CGROUP_UID + "=?";
				db.delete(TbCMessages.TB_NAME, selection,
						new String[] { gruopUid });
				deleteCAttachmentByCgroupUid(gruopUid);
				return null;
			}
		});
	}

	/**
	 * 通过群组id删除附件
	 * 
	 * method name: deleteCAttachmentByCgroupUid function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param groupUid
	 * @return
	 * @throws UnavailableStorageException
	 *             field_name long return type
	 * @History memory：
	 * @Date：2014-10-17 下午3:18:07 @Modified by：zhangyq
	 * @Description：
	 */
	public long deleteCAttachmentByCgroupUid(final String groupUid)
			throws UnavailableStorageException {
		final List<Long> longList = new ArrayList<Long>();
		database.execute(false, new DbCallback<List<Long>>() {
			public List<Long> doDbWork(SQLiteDatabase db)
					throws WrappedException, UnavailableStorageException {
				String selection = TbCAttachments.F_CGROUP_UID + "=?";
				long n = db.delete(TbCAttachments.TB_NAME, selection,
						new String[] { groupUid });
				longList.add(n);
				return longList;
			}
		});
		return (longList == null || longList.isEmpty()) ? 0 : longList.get(0);
	}

	/**
	 * 删除群组和所有成员关系
	 * 
	 * method name: deleteCGroupCmembers function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param groupUid
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午3:25:17 @Modified by：zhangyq
	 * @Description：
	 */
	public void deleteCGroupCmembers(final String groupUid)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			public Void doDbWork(SQLiteDatabase db) throws WrappedException,
					UnavailableStorageException {
				String whereClause = TbCGroup_CMember.F_CGROUP_UID + "=?";
				String[] whereArgs = new String[] { groupUid };
				db.delete(TbCGroup_CMember.TB_NAME, whereClause, whereArgs);
				return null;
			}
		});
	}

	/**
	 * 获取本地收到的最后一条消息的uid
	 * 
	 * method name: getRecentGroupMessageUid function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param groupUid
	 * @return
	 * @throws UnavailableStorageException
	 *             field_name String return type
	 * @History memory：
	 * @Date：2014-10-17 下午3:44:20 @Modified by：zhangyq
	 * @Description：
	 */
	public String getRecentGroupMessageUid(final String groupUid)
			throws UnavailableStorageException {
		final List<String> strList = new ArrayList<String>();
		database.execute(false, new DbCallback<List<String>>() {
			public List<String> doDbWork(SQLiteDatabase db)
					throws WrappedException, UnavailableStorageException {
				StringBuilder sqlBuilder = new StringBuilder();
				sqlBuilder.append("SELECT " + TbCMessages.F_UID);
				sqlBuilder.append(" FROM " + TbCMessages.TB_NAME);
				sqlBuilder.append(" WHERE " + TbCMessages.F_CGROUP_UID + "=?");
				sqlBuilder.append(" AND " + TbCMessages.F_MESSAGE_STATE + "=?");
				sqlBuilder.append(" ORDER BY " + TbCMessages.F_SENDTIME
						+ " desc");
				sqlBuilder.append(" LIMIT 1");
				String[] args = new String[] { groupUid,
						String.valueOf(CMessage.State.sendSuccess.ordinal()) };
				Cursor cursor = null;

				cursor = db.rawQuery(sqlBuilder.toString(), args);
				if (cursor.moveToFirst()) {
					strList.add(cursor.getString(0));
				}
				return strList;
			}
		});
		return (strList == null || strList.isEmpty()) ? null : strList.get(0);
	}

	/**
	 * 通过uid删除消息 包括附件
	 * 
	 * method name: deleteCMessages function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param uids
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午3:50:36 @Modified by：zhangyq
	 * @Description：
	 */
	public void deleteCMessages(final String[] uids)
			throws UnavailableStorageException {
		database.execute(true, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				String selection = TbCMessages.F_UID + "=?";
				try {
					for (String uid : uids) {
						db.delete(TbCMessages.TB_NAME, selection,
								new String[] { uid });
					}
					deleteCAttachmentByMessageUids(uids);
				} catch (UnavailableStorageException e) {
					e.printStackTrace();
				}
				return null;
			}
		});
	}

	/**
	 * 通过消息id删除附件
	 * 
	 * method name: deleteCAttachmentByMessageUids function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param messageUids
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午3:50:52 @Modified by：zhangyq
	 * @Description：
	 */
	public void deleteCAttachmentByMessageUids(final String[] messageUids)
			throws UnavailableStorageException {
		database.execute(true, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				String selection = TbCAttachments.F_CMESSAGES_UID + "=?";
				for (String uid : messageUids) {
					db.delete(TbCAttachments.TB_NAME, selection,
							new String[] { uid });
				}
				return null;
			}
		});
	}

	/**
	 * 删除此消息（旧） method name: deleteCMessagesByMsgUid function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param oldUid
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午4:32:10 @Modified by：zhangyq
	 * @Description：
	 */
	private void deleteCMessagesByMsgUid(final String oldUid)
			throws UnavailableStorageException {
		database.execute(true, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				String selection = TbCMessages.F_UID + "=?";
				db.delete(TbCMessages.TB_NAME, selection,
						new String[] { oldUid });
				return null;
			}
		});
	}

	/**
	 * 获取此groupid下所有附件
	 * 
	 * method name: getCAttachments function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param groupUid
	 * @return
	 * @throws UnavailableStorageException
	 *             field_name List<CAttachment> return type
	 * @History memory：
	 * @Date：2014-10-22 下午1:51:09 @Modified by：zhangyq
	 * @Description：
	 */
	public List<CAttachment> getCAttachments(final String groupUid)
			throws UnavailableStorageException {
		final List<CAttachment> cAttachment = new ArrayList<CAttachment>();
		database.execute(false, new DbCallback<List<CAttachment>>() {
			public List<CAttachment> doDbWork(SQLiteDatabase db)
					throws WrappedException, UnavailableStorageException {
				String selection = TbCAttachments.F_CGROUP_UID + "=?";
				Cursor cursor = null;
				try {
					cursor = db.query(TbCAttachments.TB_NAME, null, selection,
							new String[] { groupUid }, null, null, null);
					cAttachment.addAll(parserCursorCAttachments(cursor));
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Utility.closeQuietly(cursor);
				}
				return cAttachment;
			}
		});
		return cAttachment;
	}

	private List<CAttachment> parserCursorCAttachments(Cursor cursor) {
		List<CAttachment> mCattachment = new ArrayList<CAttachment>();
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				CAttachment mAttachment = new CAttachment();
				mAttachment.setAttchmentId(cursor.getString(cursor
						.getColumnIndex(TbCAttachments.F_UID)));
				mAttachment.setContentType(cursor.getString(cursor
						.getColumnIndex(TbCAttachments.F_MIME_TYPE)));
				mAttachment.setFilePath(cursor.getString(cursor
						.getColumnIndex(TbCAttachments.F_FILE_PATH)));
				mAttachment.setDownloadState(cursor.getInt(cursor
						.getColumnIndex(TbCAttachments.F_DOWNLOAD_STATE)));
				mAttachment.setMessageUid(cursor.getString(cursor
						.getColumnIndex(TbCAttachments.F_CMESSAGES_UID)));
				mAttachment.setName(cursor.getString(cursor
						.getColumnIndex(TbCAttachments.F_NAME)));
				// 服务端路径，可以根据本地标记判断
				mAttachment.setFileid(cursor.getString(cursor
						.getColumnIndex(TbCAttachments.F_FILE_ID)));
				mAttachment.setSize(cursor.getLong(cursor
						.getColumnIndex(TbCAttachments.F_SIZE)));
				mAttachment.setVoiceLength(cursor.getInt(cursor
						.getColumnIndex(TbCAttachments.F_VOICE_LENGTH)));
				mAttachment.setReadFlag(cursor.getInt(cursor
						.getColumnIndex(TbCAttachments.F_READ_FLAG)));
				// 图片路径判断相关
				mAttachment.setLocalPathFlag(cursor.getInt(cursor
						.getColumnIndex(TbCAttachments.F_LOCALPATH_FLAG)));
				mAttachment.setForwardFlag(cursor.getInt(cursor
						.getColumnIndex(TbCAttachments.F_FORWARD_FLAG)));
				//附件下载进度相关
				mAttachment.setDownloadProgress(cursor.getInt(cursor
						.getColumnIndex(TbCAttachments.F_DOWNLOAD_PROGRESS)));
				mAttachment.setDownloadPause(cursor.getInt(cursor
						.getColumnIndex(TbCAttachments.F_DOWNLOAD_PAUSE_FLAG))==1 ? true :false);
				mAttachment.setImageWidth(cursor.getInt(cursor
						.getColumnIndex(TbCAttachments.F_WIDTH)));
				mAttachment.setImageHeight(cursor.getInt(cursor
						.getColumnIndex(TbCAttachments.F_HEIGHT)));
				mAttachment.setImageLoad(cursor.getInt(cursor
						.getColumnIndex(TbCAttachments.F_IS_IMAGE_LOAD))==0 ? true :false);
				mCattachment.add(mAttachment);
			}
		}
		return mCattachment;
	}

	/**
	 * 获取此消息下的附件
	 * 
	 * method name: getCAttachment function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param cmessageUid
	 * @return
	 * @throws UnavailableStorageException
	 *             field_name CAttachment return type
	 * @History memory：
	 * @Date：2014-10-17 下午4:50:09 @Modified by：zhangyq
	 * @Description：
	 */
	public CAttachment getCAttachment(final String cmessageUid)
			throws UnavailableStorageException {
		final List<CAttachment> caList = new ArrayList<CAttachment>();
		database.execute(false, new DbCallback<List<CAttachment>>() {
			public List<CAttachment> doDbWork(SQLiteDatabase db)
					throws WrappedException, UnavailableStorageException {
				String selection = TbCAttachments.F_CMESSAGES_UID + "=?";
				Cursor cursor = null;
				try {
					cursor = db.query(TbCAttachments.TB_NAME, null, selection,
							new String[] { cmessageUid }, null, null, null);
					caList.addAll(parserCursorCAttachments(cursor));
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Utility.closeQuietly(cursor);
				}
				return caList;
			}
		});
		return (caList == null || caList.isEmpty()) ? null : caList.get(0);
	}

	/**
	 * 通过附件id删除单个附件
	 * 
	 * method name: deleteCAttachmentByUids function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param uids
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午4:56:35 @Modified by：zhangyq
	 * @Description：
	 */
	public void deleteCAttachmentByUids(final String[] uids)
			throws UnavailableStorageException {
		database.execute(true, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				String selection = TbCAttachments.F_UID + "=?";
				for (String uid : uids) {
					db.delete(TbCAttachments.TB_NAME, selection,
							new String[] { uid });
				}
				return null;
			}
		});
	}

	/**
	 * 查找群消息
	 * 
	 * method name: searchGroupMessages function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param groupUid
	 * @param keyWord
	 * @return
	 * @throws UnavailableStorageException
	 *             field_name List<CMessage> return type
	 * @History memory：
	 * @Date：2014-10-17 下午5:04:05 @Modified by：zhangyq
	 * @Description：
	 */
	public List<CMessage> searchGroupMessages(final String groupUid,
			final String tempKeyWord) throws UnavailableStorageException {
		final String keyWord = sqliteEscape(tempKeyWord);
		return database.execute(false, new DbCallback<List<CMessage>>() {
			public List<CMessage> doDbWork(SQLiteDatabase db)
					throws WrappedException, UnavailableStorageException {
				StringBuilder sqlBuilder = new StringBuilder();
				sqlBuilder.append("SELECT MSG.* FROM ")
						.append(TbCMessages.TB_NAME).append(" MSG");
				sqlBuilder.append(" JOIN ").append(TbCMember.TB_NAME)
						.append(" M");
				sqlBuilder.append(" ON MSG.").append(TbCMessages.F_CMEMBER_UID)
						.append("=").append(" M.").append(TbCMember.F_UID);
				sqlBuilder.append(" WHERE ").append(" MSG.")
						.append(TbCMessages.F_CGROUP_UID).append(" = ? ");
				sqlBuilder.append(" AND ").append(" MSG.")
						.append(TbCMessages.F_DELETE_FLAG).append("=0 ");
				sqlBuilder.append(" AND ").append(" (MSG.")
						.append(TbCMessages.F_CONTENT).append(" LIKE '%")
						.append(keyWord).append("%' ESCAPE '/' ")
						.append(" OR ");
				sqlBuilder.append(" M.").append(TbCMember.F_NICK_NAME)
						.append(" LIKE '%").append(keyWord)
						.append("%' ESCAPE '/')");
				sqlBuilder.append(" GROUP BY ").append(" MSG.")
						.append(TbCMessages.F_UID);
				Cursor cursor = null;
				try {
					cursor = db.rawQuery(sqlBuilder.toString(),
							new String[] { groupUid });
					return parserCursorCMessages(groupUid, cursor);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Utility.closeQuietly(cursor);
				}
				return null;
			}
		});
	}

	/**
	 * 更新附件状态
	 * 
	 * method name: updateCAttachmentState function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param attchmentId
	 * @param state
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午5:06:29 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateCAttachmentState(final String attchmentId, final int state)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			public Void doDbWork(SQLiteDatabase db) throws WrappedException,
					UnavailableStorageException {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TbCAttachments.F_DOWNLOAD_STATE, state);
				String whereClause = TbCAttachments.F_UID + " =? ";
				String[] whereArgs = { attchmentId };
				db.update(TbCAttachments.TB_NAME, contentValues, whereClause,
						whereArgs);
				return null;
			}
		});
	}

	/**
	 * 更新附件已读状态
	 * 
	 * method name: updateCAttachmentReadState function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param uid
	 * @param readFlag
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午5:08:56 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateCAttachmentReadState(final String uid, final int readFlag)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			public Void doDbWork(SQLiteDatabase db) throws WrappedException,
					UnavailableStorageException {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TbCAttachments.F_READ_FLAG, readFlag);
				String whereClause = TbCAttachments.F_UID + " =? ";
				String[] whereArgs = { uid };
				db.update(TbCAttachments.TB_NAME, contentValues, whereClause,
						whereArgs);
				return null;
			}
		});
	}

	/**
	 * 更改群组未读数量
	 * 
	 * method name: updateCGroupUntreatedCount function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param groupUid
	 * @param count
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午5:10:24 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateCGroupUntreatedCount(final String groupUid,
			final int count) throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			public Void doDbWork(SQLiteDatabase db) throws WrappedException,
					UnavailableStorageException {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TbCGroup.F_UNTREATED_COUNT, count);
				int result;
				if ( groupUid == null ){//groupUid为空时更新全部
					result = db.update(TbCGroup.TB_NAME, contentValues, null,null);
				}else{
					String whereClause = TbCGroup.F_UID + " =? ";
					String[] whereArgs = { groupUid };
					result = db.update(TbCGroup.TB_NAME, contentValues, whereClause,whereArgs);
				}
				if(result>0){
					notifyChattingChange();
				}
				return null;
			}
		});
	}

	/**
	 * 通过群聊成员添加邮件地址
	 * 
	 * method name: saveMailAddressByLocalCMember function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param groupUid
	 * @param mMembers
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午5:47:09 @Modified by：zhangyq
	 * @Description：
	 */
	// public synchronized void saveMailAddressByLocalCMember(final String
	// groupUid, final List<CGroupMember> mMembers) throws
	// UnavailableStorageException {
	// if (mMembers == null || mMembers.isEmpty()) {
	// return;
	// }
	// database.execute(false, new DbCallback<Void>() {
	// public Void doDbWork(SQLiteDatabase db) throws WrappedException,
	// UnavailableStorageException {
	// for (CGroupMember member : mMembers) {
	// ContentValues values = new ContentValues();
	// values.put(Columns.TbUserContacts.F_MESSAGE_ID, groupUid);
	// values.put(Columns.TbUserContacts.F_EMAIL,
	// member.getEmail());
	// values.put(Columns.TbUserContacts.F_DATE,
	// System.currentTimeMillis());
	// values.put(Columns.TbUserContacts.F_NAME,
	// member.getNickName());
	// values.put(Columns.TbUserContacts.F_SPELL_NAME,
	// HanziToPinyin.toPinyin(member.getNickName()));
	// db.insertWithOnConflict(Columns.TbUserContacts.TB_NAME,
	// null, values, SQLiteDatabase.CONFLICT_REPLACE);
	// }
	// return null;
	// }
	// });
	// }

	/**
	 * 删除群组和成员关系
	 * 
	 * method name: deleteCGroupCmember function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param groupUid
	 * @param memberUids
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午6:06:08 @Modified by：zhangyq
	 * @Description：
	 */
	public void deleteCGroupCmember(final String groupUid,
			final String[] memberUids) throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			public Void doDbWork(SQLiteDatabase db) throws WrappedException,
					UnavailableStorageException {
				String whereClause = TbCGroup_CMember.F_CGROUP_UID + "=? and "
						+ TbCGroup_CMember.F_CMEMBER_UID + "=?";
				for (String uid : memberUids) {
					String[] whereArgs = new String[] { groupUid, uid };
					db.delete(TbCGroup_CMember.TB_NAME, whereClause, whereArgs);
				}
				return null;
			}
		});
	}

	/**
	 * 获取所在群中所有成员uid
	 * 
	 * method name: getCGroupCmember function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param groupUid
	 * @return
	 * @throws UnavailableStorageException
	 *             field_name List<String> return type
	 * @History memory：
	 * @Date：2014-10-17 下午6:10:22 @Modified by：zhangyq
	 * @Description：
	 */
	public List<String> getCGroupCmember(final String groupUid)
			throws UnavailableStorageException {
		final List<String> memberUids = new ArrayList<String>();
		database.execute(false, new DbCallback<List<String>>() {
			public List<String> doDbWork(SQLiteDatabase db)
					throws WrappedException, UnavailableStorageException {
				Cursor cursor = null;
				String[] columns = new String[] { TbCGroup_CMember.F_CMEMBER_UID };
				String selection = TbCGroup_CMember.F_CGROUP_UID + "=? ";
				String[] selectionArgs = new String[] { groupUid };

				cursor = db.query(TbCGroup_CMember.TB_NAME, columns, selection,
						selectionArgs, null, null, null);
				while (cursor.moveToNext()) {
					memberUids.add(cursor.getString(cursor
							.getColumnIndex(TbCGroup_CMember.F_CMEMBER_UID)));
				}
				return memberUids;
			}
		});
		return memberUids;
	}

	/**
	 * 搜索混合聊天列表
	 * 
	 * method name: searchMixedChatting function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param keyWord
	 * @return
	 * @throws UnavailableStorageException
	 *             field_name List<MixedChatting> return type
	 * @History memory：
	 * @Date：2014-10-17 下午7:11:48 @Modified by：zhangyq
	 * @Description：
	 */
	public List<MixedChatting> searchMixedChatting(final String tempKeyWord)
			throws UnavailableStorageException {
		final List<MixedChatting> mixedChattings = new ArrayList<MixedChatting>();
		final String keyWord = sqliteEscape(tempKeyWord);
		database.execute(false, new DbCallback<List<MixedChatting>>() {
			public List<MixedChatting> doDbWork(SQLiteDatabase db)
					throws WrappedException, UnavailableStorageException {
				Cursor cGroupsCursor = null;
				Cursor dchatCursor = null;
				try {
					cGroupsCursor = searchGroup(keyWord);
					dchatCursor = searchDchat(keyWord);
					while (dchatCursor.moveToNext()) {
						mixedChattings.add(MixedChatting
								.build(parseCursorDChat(dchatCursor)));
					}
					while (cGroupsCursor.moveToNext()) {
						mixedChattings.add(MixedChatting
								.build(parseCursorCGroup(cGroupsCursor)));
					}
				} catch (MessagingException e) {
					e.printStackTrace();
				} finally {
					GlobalTools.closeCursor(dchatCursor);
					GlobalTools.closeCursor(cGroupsCursor);
				}

				return sortMixedChatting(mixedChattings);
			}
		});
		return sortMixedChatting(mixedChattings);
	}

	/**
	 * 搜索群聊结果集
	 *
	 * method name: searchGroup function @Description: TODO Parameters and
	 * return values description：
	 *
	 * @param keyWord
	 * @return
	 * @throws MessagingException
	 *             field_name Cursor return type
	 * @History memory：
	 * @Date：2014-10-17 下午7:01:46 @Modified by：zhangyq
	 * @Description：
	 */
	private Cursor searchGroup(final String keyWord) throws MessagingException {
		final List<Cursor> cursorList = new ArrayList<Cursor>();
		try {
			database.execute(false, new DbCallback<List<Cursor>>() {
				public List<Cursor> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					StringBuffer buffer = new StringBuffer();
					buffer.append(TbCGroup.F_IS_VISIBILITY + "=1 AND (");
					buffer.append(TbCGroup.F_GROUP_NAME).append(
							" LIKE ? ESCAPE '/' OR ");
					buffer.append(TbCGroup.F_GROUP_DESC).append(
							" LIKE ? ESCAPE '/' OR ");
					buffer.append(TbCGroup.F_LAST_SEND_CONTENT).append(
							" LIKE ? ESCAPE '/' )");
					buffer.append(" ORDER BY ")
							.append(TbCGroup.F_LAST_SENDDATE).append(" desc");
					String args[] = new String[] { "%" + keyWord + "%",
							"%" + keyWord + "%", "%" + keyWord + "%" };
					try {
						cursor = db.query(TbCGroup.TB_NAME, null,
								buffer.toString(), args, null, null, null);
						cursorList.add(cursor);
					} catch (Exception e) {
						throw new WrappedException(e);
					}
					return cursorList;
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}
		return (cursorList == null || cursorList.isEmpty()) ? null : cursorList
				.get(0);
	}

	/**
	 * 转发界面根据名称规则搜索群聊结果集
	 *
	 * method name: searchGroup function @Description: TODO Parameters and
	 * return values description：
	 *
	 * @param keyWord
	 * @return
	 * @throws MessagingException
	 *             field_name Cursor return type
	 * @History memory：
	 * @Date：2015-6-18 下午7:01:46 @Modified by：shengli
	 * @Description：
	 */
	private Cursor searchGroupByForward(final String keyWord) throws MessagingException {
		final List<Cursor> cursorList = new ArrayList<Cursor>();
		try {
			database.execute(false, new DbCallback<List<Cursor>>() {
				public List<Cursor> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					StringBuffer buffer = new StringBuffer();
					buffer.append(TbCGroup.F_IS_VISIBILITY + "=1 AND (");
					buffer.append(TbCGroup.F_GROUP_NAME).append(
							" LIKE ? ESCAPE '/')");
					buffer.append(" ORDER BY ")
							.append(TbCGroup.F_LAST_SENDDATE).append(" desc");
					String args[] = new String[] { "%" + keyWord + "%"};
					try {
						cursor = db.query(TbCGroup.TB_NAME, null,
								buffer.toString(), args, null, null, null);
						cursorList.add(cursor);
					} catch (Exception e) {
						throw new WrappedException(e);
					}
					return cursorList;
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}
		return (cursorList == null || cursorList.isEmpty()) ? null : cursorList
				.get(0);
	}

	/**
	 * 搜索单聊结果集
	 * 
	 * method name: searchDchat function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param keyWord
	 * @return field_name Cursor return type
	 * @History memory：
	 * @Date：2014-10-17 下午6:22:57 @Modified by：zhangyq
	 * @Description：
	 */
	private Cursor searchDchat(final String tempKeyWord)
			throws MessagingException {
		final List<Cursor> cursorList = new ArrayList<Cursor>();
		final String keyWord = sqliteEscape(tempKeyWord);
		try {
			database.execute(false, new DbCallback<List<Cursor>>() {

				public List<Cursor> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;

					String sql = "SELECT d.*, c.f_nick_name, c.f_img_head, c.f_img_head_hash FROM tb_d_chatlist d LEFT JOIN tb_user_contacts c ON (d.f_to_email = c.f_email) "
							+ "WHERE d.f_is_visibility = ? AND (f_to_email LIKE ? ESCAPE '/' OR f_nick_name LIKE ? ESCAPE '/' OR f_last_message_email LIKE ? ESCAPE '/' OR f_last_message LIKE ? ESCAPE '/') ORDER BY d.f_is_sticked DESC, d.f_sticked_date DESC, d.f_last_time DESC";
					try {
						cursor = db.rawQuery(sql,
								new String[] { String.valueOf(1),
										"%" + keyWord + "%",
										"%" + keyWord + "%",
										"%" + keyWord + "%",
										"%" + keyWord + "%" });
						cursorList.add(cursor);
					} catch (Exception e) {
						throw new WrappedException(e);
					}
					return cursorList;
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}
		return (cursorList == null || cursorList.isEmpty()) ? null : cursorList
				.get(0);
	}

	/**
	 * 搜索单聊结果集,用于转发界面
	 *
	 * method name: searchDchat function @Description: TODO Parameters and
	 * return values description：
	 *
	 * @param keyWord
	 * @return field_name Cursor return type
	 * @History memory：
	 * @Date：2015-6-18 下午6:22:57 @Modified by：shengli
	 * @Description：
	 */
	private Cursor searchDchatByForward(final String tempKeyWord)
			throws MessagingException {
		final List<Cursor> cursorList = new ArrayList<Cursor>();
		final String keyWord = sqliteEscape(tempKeyWord);
		try {
			database.execute(false, new DbCallback<List<Cursor>>() {

				public List<Cursor> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;

					String sql = "SELECT d.*, c.f_nick_name, c.f_img_head, c.f_img_head_hash FROM tb_d_chatlist d LEFT JOIN tb_user_contacts c ON (d.f_to_email = c.f_email) "
							+ "WHERE d.f_is_visibility = ? AND d.f_dchat_type = 0 AND (f_to_email LIKE ? ESCAPE '/' OR f_nick_name LIKE ? ESCAPE '/') ORDER BY d.f_is_sticked DESC, d.f_sticked_date DESC, d.f_last_time DESC";
					try {
						cursor = db.rawQuery(sql,
								new String[] { String.valueOf(1),
										"%" + keyWord + "%",
										"%" + keyWord + "%"});
						cursorList.add(cursor);
					} catch (Exception e) {
						throw new WrappedException(e);
					}
					return cursorList;
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}
		return (cursorList == null || cursorList.isEmpty()) ? null : cursorList
				.get(0);
	}

	/**
	 * 获取单条群消息
	 * 
	 * @Description:
	 * @param cMessageUid
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-22
	 */
	public CMessage getCMessage(final String cMessageUid)
			throws UnavailableStorageException {
		final List<CMessage> messageList = new ArrayList<CMessage>();
		database.execute(false, new DbCallback<List<CMessage>>() {
			public List<CMessage> doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				StringBuffer sqlBuffer = new StringBuffer();
				sqlBuffer.append("SELECT * FROM " + TbCMessages.TB_NAME)
						.append(" WHERE ")
						.append(TbCMessages.F_UID + "=? and ")
						.append(TbCMessages.F_DELETE_FLAG + "=0");
				Cursor cursor = null;
				cursor = db.rawQuery(sqlBuffer.toString(),
						new String[] { cMessageUid });
				CMessage cMessage = parserCursorCMessages(cursor);
				if (cMessage != null) {
					messageList.add(cMessage);
				}
				return messageList;
			}
		});
		return messageList.size() > 0 ? messageList.get(0) : null;
	}

	/**
	 * 获取单条群消息cursor
	 * 
	 * @Description:
	 * @param cursor
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-22
	 */
	private CMessage parserCursorCMessages(Cursor cursor) {
		CMessage message = null;
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				message = new CMessage();
				message.setUid(cursor.getString(cursor
						.getColumnIndex(TbCMessages.F_UID)));
				// message.setSenderUid(cursor.getString(cursor.getColumnIndex(TbCMessages.F_CMEMBER_UID)));
				message.setSendTime(cursor.getLong(cursor
						.getColumnIndex(TbCMessages.F_SENDTIME)));
				message.setGroupUid(cursor.getString(cursor
						.getColumnIndex(TbCMessages.F_CGROUP_UID)));
				Type type = Type.values()[cursor.getInt((cursor
						.getColumnIndex(TbCMessages.F_MESSAGETYPE)))];
				message.setMessageType(type);
				message.setLatitude(cursor.getDouble(cursor
						.getColumnIndex(TbCMessages.F_LAT)));
				message.setLongitude(cursor.getDouble(cursor
						.getColumnIndex(TbCMessages.F_LON)));
				message.setAddress(cursor.getString(cursor
						.getColumnIndex(TbCMessages.F_ADDRESS)));
				message.setLocationName(cursor.getString(cursor
						.getColumnIndex(TbCMessages.F_LOCATION_NAME)));
				message.setContent(cursor.getString(cursor
						.getColumnIndex(TbCMessages.F_CONTENT)));
				message.setMessageState(State.values()[cursor.getInt(cursor
						.getColumnIndex(TbCMessages.F_MESSAGE_STATE))]);
				if (!type.equals(Type.TEXT)) {// 优化db：纯文本不用再查询附件表
					try {
						message.setAttachment(getCAttachment(message.getUid()));
					} catch (UnavailableStorageException e) {
						e.printStackTrace();
					}
				}
				message.setDelete_flag(cursor.getInt(cursor
						.getColumnIndex(TbCMessages.F_DELETE_FLAG)));
				try {
					message.setMember(getCMember(
							cursor.getString(cursor
									.getColumnIndex(TbCMessages.F_CMEMBER_UID)))
							.get(0));
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}

		}
		return message;
	}

	/**
	 * 获取单条单聊消息
	 * 
	 * @Description:
	 * @param dMessageUid
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-23
	 */
	public DChatMessage getDMessage(final String dMessageUid)
			throws UnavailableStorageException {
		final List<DChatMessage> messageList = new ArrayList<DChatMessage>();
		database.execute(false, new DbCallback<List<DChatMessage>>() {
			public List<DChatMessage> doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				StringBuffer sqlBuffer = new StringBuffer();
				sqlBuffer.append("SELECT * FROM " + TbDchatMessage.TB_NAME)
						.append(" WHERE ")
						.append(TbDchatMessage.F_UID + "=? and ")
						.append(TbDchatMessage.F_DELETE_FLAG + "=0");
				Cursor cursor = null;
				cursor = db.rawQuery(sqlBuffer.toString(),
						new String[] { dMessageUid });
				cursor.moveToFirst();
				DChatMessage dChatMessage = new DChatMessage();
				dChatMessage.setUuid(cursor.getString(cursor
						.getColumnIndex(TbDchatMessage.F_UID)));
				dChatMessage.setDchatUid(cursor.getString(cursor
						.getColumnIndex(TbDchatMessage.F_DCHAT_UID)));
				dChatMessage.setReceiverEmail(cursor.getString(cursor
						.getColumnIndex(TbDchatMessage.F_TO_EMAIL)));
				dChatMessage.setSenderEmail(cursor.getString(cursor
						.getColumnIndex(TbDchatMessage.F_FROM_EMAIL)));
				dChatMessage.setTime(cursor.getLong(cursor
						.getColumnIndex(TbDchatMessage.F_TIME)));
				dChatMessage.setDeleteflag(cursor.getInt(cursor
						.getColumnIndex(TbDchatMessage.F_DELETE_FLAG)));
				dChatMessage.setMessageContent(cursor.getString(cursor
						.getColumnIndex(TbDchatMessage.F_MESSAGE_CONTENT)));
				if (cursor.getString(cursor
						.getColumnIndex(TbDchatMessage.F_MESSAGE_STATE)) != null) {
					dChatMessage.setMessageState(cursor.getInt(cursor
							.getColumnIndex(TbDchatMessage.F_MESSAGE_STATE)));
				}
				DChatMessage.Type type = DChatMessage.Type.values()[cursor.getInt((cursor
						.getColumnIndex(TbDchatMessage.F_MESSAGE_TYPE)))];
				dChatMessage.setMessageType(type);
				if (type != DChatMessage.Type.TEXT
						|| type != DChatMessage.Type.LOCATION) {
					try {
						dChatMessage
								.setAttachments(getDAttachments(dChatMessage
										.getUuid()));
					} catch (MessagingException e) {
						e.printStackTrace();
					}
				}
				dChatMessage.setLocationType(cursor.getString(cursor
						.getColumnIndex(TbDchatMessage.F_LOCATION_TYPE)));
				dChatMessage.setLocationName(cursor.getString(cursor
						.getColumnIndex(TbDchatMessage.F_LOCATION_NAME)));
				dChatMessage.setAddress(cursor.getString(cursor
						.getColumnIndex(TbDchatMessage.F_ADDRESS)));
				dChatMessage.setLatitude(Double.valueOf(cursor.getString(cursor
						.getColumnIndex(TbDchatMessage.F_LAT))));
				dChatMessage.setLongitude(Double.valueOf(cursor
						.getString(cursor.getColumnIndex(TbDchatMessage.F_LON))));

				messageList.add(dChatMessage);

				return messageList;
			}
		});
		return messageList.size() > 0 ? messageList.get(0) : null;
	}

	/**
	 * 以联系人表数据为基准同步群成员信息。<br>
	 * 同步信息包括昵称、头像、头像hash等。<br>
	 * 
	 * method name: updateCGroupMemberInfo function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param memberList
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2014-10-23 下午5:17:05 @Modified by：zhangyq
	 * @Description：
	 */
	public void updateCGroupMemberInfo(final List<CGroupMember> memberList)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				// // 方法一：此方法sql语句性能太低，舍弃
				// long start = System.currentTimeMillis();
				// for (CGroupMember m : memberList) {
				// db.execSQL(
				// "UPDATE tb_c_member SET f_nick_name = (SELECT f_nick_name FROM "
				// +
				// "tb_user_contacts WHERE f_email = ?), f_avatar = (SELECT f_img_head FROM "
				// +
				// "tb_user_contacts WHERE f_email = ?), f_avatar_hash = (SELECT f_img_head_hash FROM "
				// +
				// "tb_user_contacts WHERE f_email = ?) WHERE tb_c_member.f_email = ?",
				// new String[] { m.getEmail(), m.getEmail(), m.getEmail(),
				// m.getEmail() });
				// }
				// long end = System.currentTimeMillis();

				// 方法二：
				final ContentValues cv = new ContentValues();
				try {
					List<ContactAttribute> contactList = getAllContacts();
					for (CGroupMember member : memberList) {
						for (ContactAttribute contact : contactList) {
							if (member.getEmail().equals(contact.getEmail())) {
								cv.clear();
								cv.put("f_nick_name", contact.getNickName());
								cv.put("f_avatar", contact.getImgHeadPath());
								cv.put("f_avatar_hash",
										contact.getImgHeadHash());
								db.update("tb_c_member", cv,
										"tb_c_member.f_email = ?",
										new String[] { contact.getEmail() });
							}
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return null;
			}
		});
	}

	/**
	 * 删除单聊记录
	 * 
	 * method name: deleteDchatFlag function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param dChatUid
	 * @return
	 * @throws UnavailableStorageException
	 *             field_name boolean return type
	 * @History memory：
	 * @Date：2014-10-24 下午1:12:25 @Modified by：zhangyq
	 * @Description：
	 */
	public boolean deleteDchatFlag(final String dChatUid)
			throws UnavailableStorageException {
		final ContentValues contentValues = new ContentValues();
		return database.execute(false, new DbCallback<Boolean>() {
			@Override
			public Boolean doDbWork(SQLiteDatabase db) throws WrappedException,
					UnavailableStorageException {
				contentValues.put(TbDchat.F_IS_VISIBILITY, 0);
				String whereClause = TbDchat.F_UID + "=?";
				String[] whereArgs = { dChatUid };
				int result = db.update(TbDchat.TB_NAME, contentValues,
						whereClause, whereArgs);
				if (result >0) {
					notifyChattingChange();
					return true;
				} else {
					return false;
				}
			}
		});
	}

	/**
	 * 统计混合聊天列表未读数
	 * 
	 * method name: getFolderCount function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @return
	 * @throws MessagingException
	 *             field_name int return type
	 * @History memory：
	 * @Date：2014-10-24 下午4:50:19 @Modified by：zhangyq
	 * @Description：
	 */
	public int getMixedUnreadCount() throws MessagingException {
		return database.execute(false, new DbCallback<Integer>() {
			@Override
			public Integer doDbWork(final SQLiteDatabase db) {
				Cursor dCursor = null;
				Cursor cCursor = null;
				try {
					dCursor = db
							.rawQuery(
									"SELECT SUM(f_unread_count) FROM tb_d_chatlist WHERE f_is_visibility = 1",
									null);
					dCursor.moveToFirst();
					int dCount = dCursor.getInt(0); // dchat unread count
					cCursor = db
							.rawQuery(
									"SELECT SUM(f_untreated_count) FROM tb_c_group WHERE f_is_visibility = 1",
									null);
					cCursor.moveToFirst();
					int cCount = cCursor.getInt(0); // cgroup unread count

					return dCount + cCount;
				} finally {
					Utility.closeQuietly(dCursor);
					Utility.closeQuietly(cCursor);
				}
			}
		});
	}

	/**
	 * 邮件搜索
	 * 
	 * method name: searchMsgs function @Description: TODO Parameters and return
	 * values description：
	 * 
	 * @param keyWord
	 * @return
	 * @throws MessagingException
	 *             field_name Message[] return type
	 * @History memory：
	 * @Date：2014-10-30 下午3:12:52 @Modified by：zhangyq
	 * @Description：
	 */
	public List<LocalMessage> searchMsgs(final String keyWord)
			throws MessagingException {
		final List<LocalMessage> messages = new ArrayList<LocalMessage>();
		database.execute(false, new DbCallback<List<LocalMessage>>() {
			@Override
			public List<LocalMessage> doDbWork(SQLiteDatabase db)
					throws WrappedException, UnavailableStorageException {
				String sql = "SELECT "
						+ GET_MESSAGES_COLS
						+ "FROM messages "
						+ "LEFT JOIN threads ON (threads.message_id = messages.id) "
						+ "LEFT JOIN folders ON (folders.id = messages.folder_id) WHERE "
						+ "((empty IS NULL OR empty != 1) AND deleted = 0) "
						+ "AND (sender_list LIKE ? OR subject LIKE ? OR "
						+ "preview LIKE ? OR text_content LIKE ? OR "
						+ "html_content LIKE ?) ORDER BY date DESC";
				String tempKeyWord = "%" + keyWord + "%";
				String[] selectionArgs = new String[] { tempKeyWord,
						tempKeyWord, tempKeyWord, tempKeyWord, tempKeyWord };
				Cursor cursor = null;
				try {
					cursor = db.rawQuery(sql, selectionArgs);
					while (cursor.moveToNext()) {
						LocalMessage message = new LocalMessage(null, null);
						message.populateFromGetMessageCursor(cursor);
						messages.add(message);
					}
				} catch (MessagingException e) {
					Log.d(MailChat.LOG_TAG, "Got an exception", e);
				} finally {
					Utility.closeQuietly(cursor);
				}
				return messages;
			}
		});
		return messages;
	}

	/**
	 * 联系人搜索
	 * 
	 * method name: searchContacts function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param keyWord
	 * @return
	 * @throws MessagingException
	 *             field_name List<ContactAttribute> return type
	 * @History memory：
	 * @Date：2014-10-30 下午3:13:07 @Modified by：zhangyq
	 * @Description：
	 */
	public List<ContactAttribute> searchContacts(final String tempKeyWord,final boolean isSearchUnUsedMailchat)
			throws MessagingException {
		final List<ContactAttribute> contactList = new LinkedList<ContactAttribute>();
		final String keyWord = sqliteEscape(tempKeyWord);
		try {
			database.execute(false, new DbCallback<List<ContactAttribute>>() {
				public List<ContactAttribute> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					String sql ="";
					if (isSearchUnUsedMailchat) {
						 sql = "select f_email, f_nick_name, f_spell_name, f_send_count,f_receive_count, f_img_head, f_img_head_hash ,f_is_used_mailchat,f_company,f_department,f_position,f_phone,f_addr,f_remarks "
								+ "from tb_user_contacts where  f_is_used_mailchat = 0 and (f_email like ? ESCAPE '/' or f_nick_name like ? ESCAPE '/' or f_spell_name like ? ESCAPE '/')";
					}else{
						sql = "select f_email, f_nick_name, f_spell_name, f_send_count,f_receive_count, f_img_head, f_img_head_hash ,f_is_used_mailchat,f_company,f_department,f_position,f_phone,f_addr,f_remarks "
							+ "from tb_user_contacts where  f_email like ? ESCAPE '/' or f_nick_name like ? ESCAPE '/' or f_spell_name like ? ESCAPE '/'";
					}
					String[] selectionArgs = new String[] {
							"%" + keyWord + "%", "%" + keyWord + "%",
							"%" + keyWord + "%" };
					try {
						cursor = db.rawQuery(sql, selectionArgs);
						while (cursor.moveToNext()) {
							ContactAttribute ca = new ContactAttribute();
							ca.setEmail(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_EMAIL)));
							ca.setNickName(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_NICK_NAME)));
							ca.setSpellName(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_SPELL_NAME)));
							ca.setSendCount(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_SEND_COUNT)));
							ca.setReceiveCount(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_RECEIVE_COUNT)));
							ca.setImgHeadPath(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_IMG_HEAD_NAME)));
							ca.setImgHeadHash(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_IMG_HEAD_HASH)));
							ca.setUsedMailchat(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_IS_USED_MAILCHAT))==1? true:false);
							ca.setCompany(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_COMPANY)));
							ca.setDepartment(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_DEPARTMENT)));
							ca.setPosition(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_POSITION)));
							ca.setPhones(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_PHONE)));
							ca.setAddr(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_ADDR)));
							ca.setOtherRemarks(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_REMARKS)));
							contactList.add(ca);
						}
						return contactList;
					} catch (Exception e) {
						throw new WrappedException(e);
					} finally {
						Utility.closeQuietly(cursor);
					}
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}
		return contactList;
	}

	/**
	 * 把搜索结果集封装成对象。包括联系人、消息、邮件。<br>
	 * 通过getXxx()方法取你所需。<br>
	 * 
	 * method name: searchWithKeyWord function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param keyWord
	 * @return
	 * @throws MessagingException
	 *             field_name SearchVo return type
	 * @History memory：
	 * @Date：2014-10-30 下午3:25:14 @Modified by：zhangyq
	 * @Description：
	 */
	public SearchVo searchWithKeyWord(String keyWord) throws MessagingException {
		SearchVo sv = new SearchVo();
		sv.setEmailList(searchMsgs(keyWord));
		sv.setContactList(searchContacts(keyWord,false));
		sv.setMixChatList(searchMixedChatting(keyWord));
		return sv;
	}

	/**
	 * 通过email取得对应昵称。<br>
	 * 
	 * method name: getNickNameByEmail function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param email
	 * @return
	 * @throws MessagingException
	 *             field_name String return type
	 * @History memory：
	 * @Date：2014-11-3 下午2:59:10 @Modified by：zhangyq
	 * @Description：
	 */
	public String getNickNameByEmail(final String email)
			throws MessagingException {
		try {
			return database.execute(false, new DbCallback<String>() {
				public String doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					try {
						cursor = db
								.rawQuery(
										"select f_nick_name from tb_user_contacts where f_email = ?",
										new String[] { email });
						if (cursor.moveToFirst()) {
							String nickName = cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_NICK_NAME));
							if (nickName != null && !"".equals(nickName)) {
								return nickName;
							}
						}
					} catch (Exception e) {
						throw new WrappedException(e);
					} finally {
						Utility.closeQuietly(cursor);
					}
					return email.substring(0, email.indexOf("@"));
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}
	}

	public String getImgHeadHash(final String email)throws UnavailableStorageException {
		return database.execute(false, new DbCallback<String>() {
			@Override
			public String doDbWork(SQLiteDatabase db) throws WrappedException,
					UnavailableStorageException {
				Cursor cursor = null;
				String imgHeadHash=null;
				try {
					cursor = db.rawQuery("select f_img_head_hash from tb_user_contacts where f_email = ?",
									new String[] { email });
					if (cursor.moveToFirst()) {
						imgHeadHash = cursor.getString(cursor.getColumnIndex(TbUserContacts.F_IMG_HEAD_HASH));
					}
				} catch (Exception e) {
					throw new WrappedException(e);
				} finally {
					Utility.closeQuietly(cursor);
				}
				return imgHeadHash;
			}
		});
	}
	public List<DChatMessage> searchDChatMessages(final String dChatUid,
			final String tempKeyWord) throws UnavailableStorageException {

		final String keyWord = sqliteEscape(tempKeyWord);
		return database.execute(false, new DbCallback<List<DChatMessage>>() {
			@Override
			public List<DChatMessage> doDbWork(SQLiteDatabase db)
					throws WrappedException, UnavailableStorageException {
				StringBuilder sqlBuilder = new StringBuilder();
				sqlBuilder.append("SELECT * FROM ").append(
						TbDchatMessage.TB_NAME);
				sqlBuilder.append(" WHERE ").append(TbDchatMessage.F_DCHAT_UID)
						.append("=? ");
				sqlBuilder.append(" AND f_delete_flag = 0 AND ")
						.append(TbDchatMessage.F_MESSAGE_CONTENT)
						.append(" LIKE '%").append(keyWord)
						.append("%' ESCAPE '/' ").append(" OR ")
						.append(TbDchatMessage.F_LOCATION_NAME)
						.append(" LIKE '%").append(keyWord)
						.append("%' ESCAPE '/' ORDER BY f_time desc");
				Cursor cursor = null;

				try {
					cursor = db.rawQuery(sqlBuilder.toString(),
							new String[] { dChatUid });
					return parserCursorDMessages(cursor);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Utility.closeQuietly(cursor);
				}

				return null;
			}

		});
	}

	private List<DChatMessage> parserCursorDMessages(Cursor cursor) {
		List<DChatMessage> messageList = new ArrayList<DChatMessage>();
		while (cursor.moveToNext()) {
			DChatMessage dChatMessage = new DChatMessage();
			dChatMessage.setUuid(cursor.getString(cursor
					.getColumnIndex(TbDchatMessage.F_UID)));
			dChatMessage.setDchatUid(cursor.getString(cursor
					.getColumnIndex(TbDchatMessage.F_DCHAT_UID)));
			dChatMessage.setReceiverEmail(cursor.getString(cursor
					.getColumnIndex(TbDchatMessage.F_TO_EMAIL)));
			dChatMessage.setSenderEmail(cursor.getString(cursor
					.getColumnIndex(TbDchatMessage.F_FROM_EMAIL)));
			dChatMessage.setTime(cursor.getLong(cursor
					.getColumnIndex(TbDchatMessage.F_TIME)));
			dChatMessage.setDeleteflag(cursor.getInt(cursor
					.getColumnIndex(TbDchatMessage.F_DELETE_FLAG)));
			dChatMessage.setMessageContent(cursor.getString(cursor
					.getColumnIndex(TbDchatMessage.F_MESSAGE_CONTENT)));
			if (cursor.getString(cursor
					.getColumnIndex(TbDchatMessage.F_MESSAGE_STATE)) != null) {
				dChatMessage.setMessageState(cursor.getInt(cursor
						.getColumnIndex(TbDchatMessage.F_MESSAGE_STATE)));
			}
			DChatMessage.Type type = DChatMessage.Type.values()[cursor
					.getInt((cursor
							.getColumnIndex(TbDchatMessage.F_MESSAGE_TYPE)))];
			dChatMessage.setMessageType(type);
			if (type != DChatMessage.Type.TEXT) {
				try {
					dChatMessage.setAttachments(getDAttachments(dChatMessage
							.getUuid()));
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}
			dChatMessage.setLocationType(cursor.getString(cursor
					.getColumnIndex(TbDchatMessage.F_LOCATION_TYPE)));
			dChatMessage.setLocationName(cursor.getString(cursor
					.getColumnIndex(TbDchatMessage.F_LOCATION_NAME)));
			dChatMessage.setAddress(cursor.getString(cursor
					.getColumnIndex(TbDchatMessage.F_ADDRESS)));
			dChatMessage.setLatitude(Double.valueOf(cursor.getString(cursor
					.getColumnIndex(TbDchatMessage.F_LAT))));
			dChatMessage.setLongitude(Double.valueOf(cursor.getString(cursor
					.getColumnIndex(TbDchatMessage.F_LON))));

			messageList.add(dChatMessage);
		}
		return messageList;
	}

	/**
	 * 特殊字符转义。理论上来说搜索数据库都应先进行特殊字符转义。<br>
	 * 部分搜索已处理，如有遗漏自行补上。<br>
	 * see {@link #searchDChatMessages()} {@link #searchMixedChatting()}<br>
	 * 
	 * method name: sqliteEscape function @Description: Parameters and return
	 * values description：
	 * 
	 * @param keyWord
	 * @return field_name String return type
	 * @History memory：
	 * @Date：2014-12-1 上午11:12:24 @Modified by：zhangyq
	 * @Description：
	 */
	private String sqliteEscape(String keyWord) {
		keyWord = keyWord.replace("/", "//");
		keyWord = keyWord.replace("'", "''");
		keyWord = keyWord.replace("[", "/[");
		keyWord = keyWord.replace("]", "/]");
		keyWord = keyWord.replace("%", "/%");
		keyWord = keyWord.replace("&", "/&");
		keyWord = keyWord.replace("_", "/_");
		keyWord = keyWord.replace("(", "/(");
		keyWord = keyWord.replace(")", "/)");
		return keyWord;
	}

	/**
	 * 更新群聊输入框草稿
	 * 
	 * @Description:
	 * @param
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-12
	 */
	public void updateCGroupDraft(final String groupUid,
			final String draftContent, final boolean isDraft) {
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					ContentValues contentValues = new ContentValues();
					contentValues.put(TbCGroup.F_IS_DRAFT, isDraft ? 1 : 0);
					contentValues.put(TbCGroup.F_DRAFT, draftContent);
					String whereClause = TbCGroup.F_UID + "=?";
					String whereArgs[] = new String[] { groupUid };
					int result = db.update(TbCGroup.TB_NAME, contentValues, whereClause,
							whereArgs);
					if(result>0){
						notifyChattingChange();
					}
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 更新单聊输入框草稿
	 * 
	 * @Description:
	 * @param
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-12
	 */
	public void updateDChatDraft(final String dChatUid,
			final String draftContent, final boolean isDraft) {
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					ContentValues contentValues = new ContentValues();
					contentValues.put(TbDchat.F_IS_DRAFT, isDraft ? 1 : 0);
					contentValues.put(TbDchat.F_DRAFT, draftContent);
					String whereClause = TbDchat.F_UID + "=?";
					String whereArgs[] = new String[] { dChatUid };
					int result =db.update(TbDchat.TB_NAME, contentValues, whereClause,
							whereArgs);
					if(result>0){
						notifyChattingChange();
					}
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					ContentValues contentValues = new ContentValues();
					contentValues.put(MQTTPendingAction.ACTION,
							pendingMQTTConmmand.getAction().ordinal());
					contentValues.put(MQTTPendingAction.COMMAND,
							pendingMQTTConmmand.getCommand().ordinal());
					contentValues.put(MQTTPendingAction.TOPIC,
							pendingMQTTConmmand.getTopic());
					contentValues.put(MQTTPendingAction.CONTENT,
							pendingMQTTConmmand.getContent());
					db.insert(MQTTPendingAction.TB_NAME, null, contentValues);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	public ArrayList<PendingMQTTConmmand> getMQTTPending()
			throws UnavailableStorageException {
		return database.execute(false,
				new DbCallback<ArrayList<PendingMQTTConmmand>>() {
					@Override
					public ArrayList<PendingMQTTConmmand> doDbWork(
							final SQLiteDatabase db) throws WrappedException {
						Cursor cursor = null;
						ArrayList<PendingMQTTConmmand> pendingMQTTConmmands = null;
						try {
							cursor = db.query(MQTTPendingAction.TB_NAME, null,
									null, null, null, null, null);
							pendingMQTTConmmands = new ArrayList<PendingMQTTConmmand>();
							while (cursor.moveToNext()) {
								PendingMQTTConmmand pendingMQTTConmmand = new PendingMQTTConmmand();
								pendingMQTTConmmand.setId(cursor.getInt(cursor
										.getColumnIndex(MQTTPendingAction.ID)));
								pendingMQTTConmmand.setAction(ActionListener.Action
										.values()[cursor.getInt((cursor
										.getColumnIndex(MQTTPendingAction.ACTION)))]);
								pendingMQTTConmmand.setCommand(MQTTCommand
										.values()[cursor.getInt(cursor
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
				});
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
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					db.delete(MQTTPendingAction.TB_NAME, MQTTPendingAction.ID
							+ "=?", new String[] { id });
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 保存HTTPS失败缓存
	 * 
	 * @Description:
	 * @param
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-1-9
	 */
	public void saveHTTPSPending(final PendingHTTPSCommand pendingHTTPSCommand) {
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					ContentValues contentValues = new ContentValues();
					contentValues.put(HTTPSPendingAction.COMMAND,
							pendingHTTPSCommand.getCommand().ordinal());
					contentValues.put(HTTPSPendingAction.PARAMETERS,
							pendingHTTPSCommand.getParameters());
					db.insert(HTTPSPendingAction.TB_NAME, null, contentValues);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获取HTTPS失败缓存
	 * 
	 * @Description:
	 * @param
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws UnavailableStorageException
	 * @date:2015-1-9
	 */
	public ArrayList<PendingHTTPSCommand> getHTTPSPending()
			throws UnavailableStorageException {
		return database.execute(false,
				new DbCallback<ArrayList<PendingHTTPSCommand>>() {
					@Override
					public ArrayList<PendingHTTPSCommand> doDbWork(
							final SQLiteDatabase db) throws WrappedException {
						Cursor cursor = null;
						ArrayList<PendingHTTPSCommand> PendingHTTPSCommands = null;
						try {
							cursor = db.query(HTTPSPendingAction.TB_NAME, null,
									null, null, null, null, null);
							PendingHTTPSCommands = new ArrayList<PendingHTTPSCommand>();
							while (cursor.moveToNext()) {
								PendingHTTPSCommand pendingHTTPSCommand = new PendingHTTPSCommand();
								pendingHTTPSCommand.setId(cursor.getInt(cursor
										.getColumnIndex(HTTPSPendingAction.ID)));
								pendingHTTPSCommand
										.setCommand(Command.values()[cursor.getInt(cursor
												.getColumnIndex(HTTPSPendingAction.COMMAND))]);
								pendingHTTPSCommand.setParameters(cursor.getString(cursor
										.getColumnIndex(HTTPSPendingAction.PARAMETERS)));
								PendingHTTPSCommands.add(pendingHTTPSCommand);
							}
							return PendingHTTPSCommands;
						} finally {
							Utility.closeQuietly(cursor);
						}
					}
				});
	}

	/**
	 * 删除HTTPS失败缓存
	 * 
	 * @Description:
	 * @param
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-1-9
	 */
	public void deleteHTTPSPending(final String id) {
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					db.delete(HTTPSPendingAction.TB_NAME, MQTTPendingAction.ID
							+ "=?", new String[] { id });
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 判断此单聊消息是否存在
	 * 
	 * @Description:
	 * @param
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return 
	 * @throws UnavailableStorageException 
	 * @date:2015-3-31
	 */
	public boolean isDMessageExists(final String dMessageUid) throws UnavailableStorageException {
			return database.execute(false, new DbCallback<Boolean>() {
				@Override
				public Boolean doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					StringBuffer sqlBuffer = new StringBuffer();
					sqlBuffer.append("SELECT "+TbDchatMessage.F_UID+ " FROM " + TbDchatMessage.TB_NAME)
							.append(" WHERE ")
							.append(TbDchatMessage.F_UID + "=?");
					Cursor cursor=null;
					try{
						cursor = db.rawQuery(sqlBuffer.toString(),new String[] { dMessageUid });
						return cursor.moveToFirst();
					} finally {
						Utility.closeQuietly(cursor);
					}
				}
			});
	}
	
	/**
	 * 判断此单聊消息是否存在
	 * 
	 * @Description:
	 * @param
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return 
	 * @throws UnavailableStorageException 
	 * @date:2015-3-31
	 */
	public boolean isCMessageExists(final String cMessageUid) throws UnavailableStorageException {
			return database.execute(false, new DbCallback<Boolean>() {
				@Override
				public Boolean doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					StringBuffer sqlBuffer = new StringBuffer();
					sqlBuffer.append("SELECT "+TbCMessages.F_UID+ " FROM " + TbCMessages.TB_NAME)
							.append(" WHERE ")
							.append(TbCMessages.F_UID + "=?");
					Cursor cursor=null;
					try{
						cursor = db.rawQuery(sqlBuffer.toString(),new String[] { cMessageUid });
						return cursor.moveToFirst();
					} finally {
						Utility.closeQuietly(cursor);
					}
				}
			});
	}
	/**
	 * 更新群附件下载进度
	 * 
	 * @Description:
	 * @param
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return 
	 * @throws UnavailableStorageException 
	 * @date:2015-5-18
	 */
	public void updateCAttachmentDownloadProgress(final String cAttachmentUid,final int downloadProgress){
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					ContentValues contentValues = new ContentValues();
					contentValues.put(TbCAttachments.F_DOWNLOAD_PROGRESS,downloadProgress);
					String whereClause = TbCAttachments.F_UID + "=?";
					String[] whereArgs = {cAttachmentUid};
					db.update(TbCAttachments.TB_NAME, contentValues, whereClause, whereArgs);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 更新群附件下载暂停状态
	 * 
	 * @Description:
	 * @param
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return 
	 * @throws UnavailableStorageException 
	 * @date:2015-5-18
	 */
	public void updateCAttachmentDownloadPauseFlag(final String cAttachmentUid,final boolean isDownloadPause){
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					ContentValues contentValues = new ContentValues();
					contentValues.put(TbCAttachments.F_DOWNLOAD_PAUSE_FLAG, isDownloadPause ? 1 : 0);
					String whereClause = TbCAttachments.F_UID + "=?";
					String[] whereArgs = {cAttachmentUid};
					db.update(TbCAttachments.TB_NAME, contentValues, whereClause, whereArgs);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 更新单聊附件下载进度
	 * 
	 * @Description:
	 * @param
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return 
	 * @throws UnavailableStorageException 
	 * @date:2015-5-18
	 */
	public void updateDAttachmentDownloadProgress(final String dAttachmentUid,final int downloadProgress){
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					ContentValues contentValues = new ContentValues();
					contentValues.put(TbDAttachments.F_DOWNLOAD_PROGRESS, downloadProgress);
					String whereClause = TbDAttachments.F_UID + "=?";
					String[] whereArgs = {dAttachmentUid};
					db.update(TbDAttachments.TB_NAME, contentValues, whereClause, whereArgs);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 更新单聊附件下载暂停状态
	 * 
	 * @Description:
	 * @param
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return 
	 * @throws UnavailableStorageException 
	 * @date:2015-5-18
	 */
	public void updateDAttachmentDownloadPauseFlag(final String dAttachmentUid,final boolean isDownloadPause){
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					ContentValues contentValues = new ContentValues();
					contentValues.put(TbDAttachments.F_DOWNLOAD_PAUSE_FLAG, isDownloadPause ? 1 : 0);
					String whereClause = TbDAttachments.F_UID + "=?";
					String[] whereArgs = {dAttachmentUid};
					db.update(TbDAttachments.TB_NAME, contentValues, whereClause, whereArgs);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 获取已保存图像的群成员头像Hash
	 *
	 * @Description:
	 * @param: email
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @throws UnavailableStorageException
	 * @date:2015-5-28
	 */
	public String getCMemberAvatarHash(final String email) throws UnavailableStorageException  {
		return database.execute(false, new DbCallback<String>() {

			public String doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				Cursor cursor = null;
				String imgHeadHash =null;
				try {
					String selection = TbCMember.F_EMAIL + "=?";
					cursor = db.query(TbCMember.TB_NAME, null, selection,
							new String[] { email }, TbCMember.F_AVATAR_HASH, null,
							null, null);
					if(cursor.moveToFirst()){
						imgHeadHash = cursor.getString(cursor.getColumnIndex(TbCMember.F_AVATAR_HASH));
					}
					return imgHeadHash;
				} finally {
					Utility.closeQuietly(cursor);
				}
			}
		});
	}

	/**
	 * 更新群附件上传文件成功后的属性
	 *
	 * @Description:
	 * @param: CAttachment
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @throws UnavailableStorageException
	 * @date:2015-6-10
	 */
	public void updateUploadCAttServerAttribute(final CAttachment cAttachment) throws UnavailableStorageException  {
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					ContentValues contentValues = new ContentValues();
					contentValues.put(TbCAttachments.F_FILE_ID, cAttachment.getFileid());
					contentValues.put(TbCAttachments.F_NAME, cAttachment.getName());
					contentValues.put(TbCAttachments.F_SIZE, cAttachment.getSize());
					String whereClause = TbCAttachments.F_UID + "=?";
					String[] whereArgs = {cAttachment.getAttchmentId()};
					db.update(TbCAttachments.TB_NAME, contentValues, whereClause, whereArgs);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 更新单聊附件上传文件成功后的属性
	 *
	 * @Description:
	 * @param: DAttachment
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @throws UnavailableStorageException
	 * @date:2015-6-12
	 */
	public void updateUploadDAttServerAttribute(final DAttachment dAttachment) throws UnavailableStorageException  {
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					ContentValues contentValues = new ContentValues();
					contentValues.put(TbDAttachments.F_FILE_ID, dAttachment.getFileid());
					contentValues.put(TbDAttachments.F_NAME, dAttachment.getName());
					contentValues.put(TbDAttachments.F_SIZE, dAttachment.getSize());
					String whereClause = TbDAttachments.F_UID + "=?";
					String[] whereArgs = {dAttachment.getAttchmentId()};
					db.update(TbDAttachments.TB_NAME, contentValues, whereClause, whereArgs);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 根据昵称及Email搜索混合列表
	 *
	 * @Description:
	 * @param: keyWord
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @throws UnavailableStorageException
	 * @date:2015-6-18
	 */
	public List<MixedChatting> searchMixedChattingByForward(final String tempKeyWord)
			throws UnavailableStorageException {
		final List<MixedChatting> mixedChattings = new ArrayList<MixedChatting>();
		final String keyWord = sqliteEscape(tempKeyWord);
		database.execute(false, new DbCallback<List<MixedChatting>>() {
			public List<MixedChatting> doDbWork(SQLiteDatabase db)
					throws WrappedException, UnavailableStorageException {
				Cursor cGroupsCursor = null;
				Cursor dchatCursor = null;
				try {
					cGroupsCursor = searchGroupByForward(keyWord);
					dchatCursor = searchDchatByForward(keyWord);
					while (dchatCursor.moveToNext()) {
						mixedChattings.add(MixedChatting
								.build(parseCursorDChat(dchatCursor)));
					}
					while (cGroupsCursor.moveToNext()) {
						mixedChattings.add(MixedChatting
								.build(parseCursorCGroup(cGroupsCursor)));
					}
				} catch (MessagingException e) {
					e.printStackTrace();
				} finally {
					GlobalTools.closeCursor(dchatCursor);
					GlobalTools.closeCursor(cGroupsCursor);
				}

				return sortMixedChatting(mixedChattings);
			}
		});
		return sortMixedChatting(mixedChattings);
	}

	/**
	 *
	 * method name: getCoustomFoldersCount function @Description: TODO
	 * Parameters and return values description:
	 *
	 * @return field_name int return type
	 * @throws UnavailableStorageException
	 * @History memory：
	 * @Date：2015-6-29 下午6:05:48 @Modified by：zhangjx
	 * @Description:获取自定义邮件夹个数
	 */
	public int getCoustomFoldersCount() throws UnavailableStorageException {
		return database.execute(false, new DbCallback<Integer>() {
			@Override
			public Integer doDbWork(final SQLiteDatabase db) {
				Cursor cursor = null;
				try {
					cursor = db.rawQuery("SELECT COUNT(*) FROM folders where is_custom_folder=0 ", null);
					cursor.moveToFirst();
					return cursor.getInt(0); // coustom folder count
				} finally {
					Utility.closeQuietly(cursor);
				}
			}
		});
	}
	/**
	 * 更新群发送状态
	 *
	 * @Description:
	 * @param cGroupUid
	 * @param sendState 0 发送成功  1发送失败  2.发送中
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-7
	 */
	public void updateCGroupSendState(final String cGroupUid,final int sendState) throws UnavailableStorageException{
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues values = new ContentValues();
				values.put(TbCGroup.F_SEND_STATE,sendState);
				String whereClause = TbCGroup.F_UID + "=?";
				String[] whereArgs = {cGroupUid};
				int result = db.update(TbCGroup.TB_NAME, values, whereClause, whereArgs);
				if(result>0){
					notifyChattingChange();
				}
				return null;
			}
		});
	}
	/**
	 * 更新单聊发送状态
	 *
	 * @Description:
	 * @param dChatUid
	 * @param sendState 0 发送成功  1发送失败  2.发送中
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-7
	 */
	public void updateDChatSendState(final String dChatUid,final int sendState) throws UnavailableStorageException{
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues values = new ContentValues();
				values.put(TbDchat.F_SEND_STATE, sendState);
				String whereClause = TbDchat.F_UID + "=?";
				String[] whereArgs = {dChatUid};
				int result = db.update(TbDchat.TB_NAME, values, whereClause, whereArgs);
				if(result>0){
					notifyChattingChange();
				}
				return null;
			}
		});
	}

	/**
	 * 更新群输入模式
	 *
	 * @Description:
	 * @param cGroupUid
	 * @param inputMode  1：语音 2：文字
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-8
	 */
	public void updateCGroupInputMode(final String cGroupUid,final int inputMode) throws UnavailableStorageException{
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues values = new ContentValues();
				values.put(TbCGroup.F_INPUT_TYPE,inputMode);
				String whereClause = TbCGroup.F_UID + "=?";
				String[] whereArgs = {cGroupUid};
				db.update(TbCGroup.TB_NAME, values, whereClause, whereArgs);
				return null;
			}
		});
	}
	/**
	 * 更新单聊输入模式
	 *
	 * @Description:
	 * @param dChatUid
	 * @param inputMode  1：语音 2：文字
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-8
	 */
	public void updateDChatInputMode(final String dChatUid,final int inputMode) throws UnavailableStorageException{
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues values = new ContentValues();
				values.put(TbDchat.F_INPUT_TYPE, inputMode);
				String whereClause = TbDchat.F_UID + "=?";
				String[] whereArgs = {dChatUid};
				db.update(TbDchat.TB_NAME, values, whereClause, whereArgs);
				return null;
			}
		});
	}
	/**
	 * 单聊消息是否显示
	 *
	 * @Description:
	 * @param dMessageUid
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws UnavailableStorageException
	 * @date:2015-7-10
	 */
	public boolean isDChatMessageVisibility(final String dMessageUid) throws UnavailableStorageException{
		return database.execute(false, new DbCallback<Boolean>() {
			@Override
			public Boolean doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				Cursor cursor = null;
				int flag=0;
				try {
					String selection = TbDchatMessage.F_UID + "=?";
					cursor = db.query(TbDchatMessage.TB_NAME, null, selection,
							new String[] { dMessageUid }, TbDchatMessage.F_DELETE_FLAG, null,
							null, null);
					if( cursor != null && cursor.moveToFirst() ){
						flag=cursor.getInt(cursor.getColumnIndex(TbDchatMessage.F_DELETE_FLAG));
					}
					return flag==0 ? true : false;
				} finally {
					Utility.closeQuietly(cursor);
				}
			}
		});
	}
	/**
	 * 群聊消息是否显示
	 *
	 * @Description:
	 * @param cMessageUid
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws UnavailableStorageException
	 * @date:2015-7-10
	 */
	public boolean isCMessageVisibility(final String cMessageUid) throws UnavailableStorageException{
		return database.execute(false, new DbCallback<Boolean>() {
			@Override
			public Boolean doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				Cursor cursor = null;
				try {
					String selection = TbCMessages.F_UID + "=?";
					cursor = db.query(TbCMessages.TB_NAME, null, selection,
							new String[] { cMessageUid }, TbCMessages.F_DELETE_FLAG, null,
							null, null);
					cursor.moveToFirst();
					return cursor.getInt(cursor.getColumnIndex(TbCMessages.F_DELETE_FLAG))==0 ? true : false;
				} finally {
					Utility.closeQuietly(cursor);
				}
			}
		});
	}

	/**
	 * 获取使用过邮洽的联系人
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws UnavailableStorageException
	 * @date:2015-7-10
	 */
	public List<ContactAttribute> getContactsByUsedMailChat()
			throws MessagingException {
		return database.execute(false,
				new DbCallback<List<ContactAttribute>>() {
					public List<ContactAttribute> doDbWork(
							final SQLiteDatabase db) throws WrappedException {
						List<ContactAttribute> contactList = new ArrayList<ContactAttribute>();
						Cursor cursor = null;
						String selection = TbUserContacts.F_IS_USED_MAILCHAT
								+ " = ?";
						String[] selectionArgs = new String[] { "1" };
						try {
							cursor = db.query(TbUserContacts.TB_NAME, null,
									selection, selectionArgs, null, null, null);
							while (cursor.moveToNext()) {
								ContactAttribute ca = new ContactAttribute();
								ca.setEmail(cursor.getString(cursor
										.getColumnIndex(TbUserContacts.F_EMAIL)));
								ca.setNickName(cursor.getString(cursor
										.getColumnIndex(TbUserContacts.F_NICK_NAME)));
								ca.setSpellName(cursor.getString(cursor
										.getColumnIndex(TbUserContacts.F_SPELL_NAME)));
								ca.setImgHeadPath(cursor.getString(cursor
										.getColumnIndex(TbUserContacts.F_IMG_HEAD_NAME)));
								ca.setImgHeadHash(cursor.getString(cursor
										.getColumnIndex(TbUserContacts.F_IMG_HEAD_HASH)));
								ca.setUsedMailchat(cursor.getInt(cursor
										.getColumnIndex(TbUserContacts.F_IS_USED_MAILCHAT))==1? true:false);
								ca.setCompany(cursor.getString(cursor
										.getColumnIndex(TbUserContacts.F_COMPANY)));
								ca.setDepartment(cursor.getString(cursor
										.getColumnIndex(TbUserContacts.F_DEPARTMENT)));
								ca.setPosition(cursor.getString(cursor
										.getColumnIndex(TbUserContacts.F_POSITION)));
								ca.setPhones(cursor.getString(cursor
										.getColumnIndex(TbUserContacts.F_PHONE)));
								ca.setAddr(cursor.getString(cursor
										.getColumnIndex(TbUserContacts.F_ADDR)));
								ca.setOtherRemarks(cursor.getString(cursor
										.getColumnIndex(TbUserContacts.F_REMARKS)));
								contactList.add(ca);
							}
							return contactList;
						} finally {
							Utility.closeQuietly(cursor);
						}
					}
				});
	}

	/**
	 * 
	 * method name: setDefaultFoldersPushValue function @Description: TODO
	 * Parameters and return values description:
	 * 
	 * @param account
	 * @return
	 * @throws MessagingException
	 *             field_name boolean return type
	 * @History memory：
	 * @Date：2015-7-31 下午6:43:34 @Modified by：zhangjx
	 * @Description:删除账号 恢复默认
	 */
	public boolean setDefaultFoldersPushValue(final Account account)
			throws MessagingException {
		return database.execute(false, new DbCallback<Boolean>() {
			@Override
			public Boolean doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				int result = -1;
				ContentValues contentValues = new ContentValues();
				contentValues.put("is_allow_push", 1);
				// 35企业邮箱账号（含客户使用的）默认收件箱和自定义邮件夹全部接收推送
				if (account.is35Mail()) {
					// 是35域邮箱允许push
					String whereClause = "is_custom_folder" + "=? or name= ? ";

					String[] whereArgs = { "0", "INBOX" };
					result = db.update("folders", contentValues, whereClause,
							whereArgs);
				} else {
					String whereClause = "name= ? ";
					String[] whereArgs = { "INBOX" };
					result = db.update("folders", contentValues, whereClause,
							whereArgs);
				}

				if (result == 1) {
					return true;
				} else {
					return false;
				}
			}
		});
	}
	
	/**
	 * 整个更新企业联系人数据
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws UnavailableStorageException 
	 * @date:2015-8-25
	 */
	public void cleanAllAndSaveAllBContact(final JSONObject resultJson) throws UnavailableStorageException{
		database.execute(true, new DbCallback<Void>() {//开启事务提升效率

			@Override
			public Void doDbWork(SQLiteDatabase db) throws WrappedException,
					UnavailableStorageException {
//				if (resultJson==null) {
//					Log.d("qxian", "eis返回null");
//					return null;
//				}
				if(!resultJson.isNull("result")&&resultJson.optInt("result") == 1){
					//清空所有数据
					db.execSQL("delete from " + TbBusinessContactDepartment_User.TB_NAME);//先清空外键
					db.execSQL("delete from " + TbBusinessContactDepartment.TB_NAME);
					db.execSQL("delete from " + TbBusinessContactUser.TB_NAME);
					//存储新数据
					try {
						String company =null;
						if(!resultJson.isNull("name")){
							company= resultJson.optString("name");
						}
						if(!resultJson.isNull("deps")){//部门
							JSONArray depsJSONArray = resultJson.getJSONArray("deps");
							for(int i =0;i<depsJSONArray.length();i++){
								JSONObject depJSONObject =depsJSONArray.optJSONObject(i);
								if(depJSONObject!=null){
									ContentValues bContactDepartmentValues = new ContentValues();
									bContactDepartmentValues.put(TbBusinessContactDepartment.F_ID, depJSONObject.optString("i"));
									bContactDepartmentValues.put(TbBusinessContactDepartment.F_NAME, depJSONObject.optString("n"));
									bContactDepartmentValues.put(TbBusinessContactDepartment.F_PARENT_ID, depJSONObject.optString("p"));
									bContactDepartmentValues.put(TbBusinessContactDepartment.F_SORT_ID, depJSONObject.optString("s"));
									bContactDepartmentValues.put(TbBusinessContactDepartment.F_USER_TOTAL_COUNT, depJSONObject.optInt("cnt"));
									bContactDepartmentValues.put(TbBusinessContactDepartment.F_CHILD_DEP_COUNT, depJSONObject.optString("sub"));
									db.insertWithOnConflict(TbBusinessContactDepartment.TB_NAME, null, bContactDepartmentValues,
											SQLiteDatabase.CONFLICT_IGNORE);
								}
							}
						}
						if(!resultJson.isNull("users")){
							JSONArray usersJSONArray = resultJson.getJSONArray("users");
							for(int i =0;i<usersJSONArray.length();i++){
								JSONObject userJSONObject =usersJSONArray.optJSONObject(i);
								if(userJSONObject!=null){
									ContentValues bContactUserValues = new ContentValues();
									bContactUserValues.put(TbBusinessContactUser.F_EMAIL, userJSONObject.optString("e"));
									bContactUserValues.put(TbBusinessContactUser.F_NAME, userJSONObject.optString("n"));
									bContactUserValues.put(TbBusinessContactUser.F_NICK_NAME, userJSONObject.optString("k"));
									bContactUserValues.put(TbBusinessContactUser.F_IS_USED_MAILCHAT, userJSONObject.optInt("s"));
									bContactUserValues.put(TbBusinessContactUser.F_IMG_HEAD_HASH, userJSONObject.optString("a"));
									//解析电话
									String phoneSts="";
									JSONArray mpJSONArray =userJSONObject.getJSONArray("mp");
									for (int j = 0; j < mpJSONArray.length(); j++) {
										phoneSts=mpJSONArray.getString(j);
									}
									JSONArray wpJSONArray =userJSONObject.getJSONArray("wp");
									for (int k = 0; k < wpJSONArray.length();k++) {
										phoneSts+=","+wpJSONArray.getString(k);
									}
									JSONArray hpJSONArray =userJSONObject.getJSONArray("hp");
									for (int m = 0; m < hpJSONArray.length(); m++) {
										phoneSts+=","+hpJSONArray.getString(m);
									}
									if (!StringUtil.isEmpty(phoneSts)) {
										bContactUserValues.put(TbBusinessContactUser.F_PHONE, phoneSts);
									}
									if(company!=null){
										bContactUserValues.put(TbBusinessContactUser.F_COMPANY,company);
									}
									db.insertWithOnConflict(TbBusinessContactUser.TB_NAME, null, bContactUserValues,
											SQLiteDatabase.CONFLICT_IGNORE);
								}
							}
						}
						if(!resultJson.isNull("rels")){
							JSONArray relsJSONArray = resultJson.getJSONArray("rels");
							for(int i =0;i<relsJSONArray.length();i++){
								JSONObject relJSONObject =relsJSONArray.optJSONObject(i);
								if(relJSONObject!=null){
									ContentValues bContactDepartmentUserValues = new ContentValues();
									bContactDepartmentUserValues.put(TbBusinessContactDepartment_User.F_DEP_ID, relJSONObject.optString("b"));
									bContactDepartmentUserValues.put(TbBusinessContactDepartment_User.F_USER_EMAIL, relJSONObject.optString("u"));
									bContactDepartmentUserValues.put(TbBusinessContactDepartment_User.F_IS_LEADER, relJSONObject.optInt("l"));
									db.insertWithOnConflict(TbBusinessContactDepartment_User.TB_NAME, null, bContactDepartmentUserValues,
											SQLiteDatabase.CONFLICT_IGNORE);
								}
							}
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return null;
			}
		});
	}

	/**
	 * 更新单聊消息未读状态
	 *
	 * @Description:
	 * @param dChatMessageUid
	 * @param readFlag
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws UnavailableStorageException
	 * @date:2015-8-27
	 */
	public void updateDChatMessageReadFlag(final String dChatMessageUid, final int readFlag)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TbDchatMessage.F_READ_FLAG, readFlag);
				String whereClause = TbDchatMessage.F_UID + "= ?";
				String[] whereArgs = new String []{dChatMessageUid};
				db.update(TbDchatMessage.TB_NAME, contentValues, whereClause, whereArgs);
				return null;
			}
		});
	}

	/**
	 * 获取Eis35Bean集合
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws UnavailableStorageException
	 * @date:2015-8-27
	 */
	public List<Eis35Bean> listEis35Beans(final boolean isInviteContact) throws UnavailableStorageException{
		return database.execute(true, new DbCallback<List<Eis35Bean>>() {

			@Override
			public List<Eis35Bean> doDbWork(SQLiteDatabase db) throws WrappedException,
					UnavailableStorageException {
				// TODO Auto-generated method stub
				List<Eis35Bean> eis35Beans =new ArrayList<Eis35Bean>();
				StringBuffer sql = new StringBuffer();
				if (isInviteContact) {
					sql.append("select  * from (select * from (select * from tb_b_contact_user a left join tb_b_contact_department_user b on a.f_email = b.f_user_email  where f_is_used_mailchat=0) c left join tb_b_contact_department d on c.f_dep_id = d.f_id) e left join tb_user_contact_remark f on e.f_email = f.f_r_email");
				}else {
					sql.append("select  * from (select * from (select * from tb_b_contact_user a left join tb_b_contact_department_user b on a.f_email = b.f_user_email) c left join tb_b_contact_department d on c.f_dep_id = d.f_id) e left join tb_user_contact_remark f on e.f_email = f.f_r_email");
				}
				Cursor userCursor = db.rawQuery(sql.toString(),null);
				while (userCursor.moveToNext()) {
					Eis35Bean userBean = new Eis35Bean();
					userBean.setId(userCursor.getString(0));
					String pid=userCursor.getString(19);
					userBean.setParentId(pid==null?"0":pid);
					userBean.setLeader(userCursor.getInt(17)==1 ? true : false);
					userBean.setName(userCursor.getString(1));
					userBean.setEmail(userCursor.getString(0));
					userBean.setMailChatName(userCursor.getString(2));
					userBean.setImgHeadUrl(userCursor.getString(4));
					userBean.setUsedMailchat(userCursor.getInt(11)==1  ? true : false);
					userBean.setParent(false);
					userBean.setDepartment(userCursor.getString(20));
					userBean.setPosition(userCursor.getString(18));
					userBean.setOtherRemarks(userCursor.getString(12));
					userBean.setAddr(userCursor.getString(13));
					userBean.setPhones(userCursor.getString(6));
					getContactRemarkByEmail(userBean,userCursor);
					eis35Beans.add(userBean);
				}

				Cursor cursor = db.query(TbBusinessContactDepartment.TB_NAME, null, null, null, null, null, TbBusinessContactDepartment.F_SORT_ID);
				while(cursor.moveToNext()){
					Eis35Bean depEis35Bean =new Eis35Bean();//部门
					depEis35Bean.setId(cursor.getString(cursor.getColumnIndex(TbBusinessContactDepartment.F_ID)));
                    String pid = cursor.getString(cursor.getColumnIndex(TbBusinessContactDepartment.F_PARENT_ID));
                    depEis35Bean.setParentId(pid == null ? "0" : pid);
					depEis35Bean.setName(cursor.getString(cursor.getColumnIndex(TbBusinessContactDepartment.F_NAME)));
					depEis35Bean.setSort(cursor.getString(cursor.getColumnIndex(TbBusinessContactDepartment.F_SORT_ID)));
					depEis35Bean.setExpand(cursor.getInt(cursor.getColumnIndex(TbBusinessContactDepartment.F_IS_OPEN))==1 ? true : false);//是否展開
					depEis35Bean.setTotalCount(cursor.getInt(cursor.getColumnIndex(TbBusinessContactDepartment.F_USER_TOTAL_COUNT)));
					depEis35Bean.setChildDepCount(cursor.getInt(cursor.getColumnIndex(TbBusinessContactDepartment.F_CHILD_DEP_COUNT)));
					depEis35Bean.setParent(true);
					eis35Beans.add(depEis35Bean);
				}
				return eis35Beans;
			}
		});
	}
	public void getContactRemarkByEmail(final Eis35Bean contact,Cursor cursor ){
		contact.setrEmail(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_EMAIL)));
		contact.setrName(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_NAME)));
		contact.setrNickName(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_NICK_NAME)));
		contact.setrImgHeadHash(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_IMG_HEAD_HASH)));
		contact.setrOtherRemarks(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_REMARK)));
		contact.setrPosition(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_POSITION)));
		contact.setrPhones(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_PHONE)));
		contact.setrAddr(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_ADDR)));
		contact.setrDepartment(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_DEPARTMENT)));
		contact.setrCompany(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_COMPANY)));
	}
	/**
	 * 更新联系人删除标记
	 *
	 * @Description:
	 * @see:
	 * @param email
	 * @param isVisibility
	 * @since:
	 * @author: shengli
	 * @throws UnavailableStorageException
	 * @date:2015-8-28
	 */
	public void updateContactVisibilityFlag(final String email,final boolean isVisibility) throws UnavailableStorageException{
		database.execute(false, new DbCallback<Void>() {

			@Override
			public Void doDbWork(SQLiteDatabase db) throws WrappedException,
					UnavailableStorageException {
				// TODO Auto-generated method stub
				ContentValues contentValues = new ContentValues();
				contentValues.put(TbUserContacts.F_IS_VISIBILITY, isVisibility ? 1 : 0);
				String whereClause =TbUserContacts.F_EMAIL +"=?";
				String [] whereArgs = new String []{email};
				db.update(TbUserContacts.TB_NAME, contentValues, whereClause, whereArgs);
				return null;
			}
		});
	}
	/**
	 * 筛选企业联系人（同域筛选）
	 *
	 * @Description:
	 * @see:
	 * @param domain
	 * @since:
	 * @author: shengli
	 * @throws MessagingException
	 * @date:2015-8-28
	 */
	public List<ContactAttribute> searchLocalSameDomainContacts(
			final String domain, final String keyWord,
			final boolean isSearchUnUsedMailchat)
			throws MessagingException {
		final List<ContactAttribute> contactList = new LinkedList<ContactAttribute>();
		try {
			database.execute(false, new DbCallback<List<ContactAttribute>>() {

				@Override
				public List<ContactAttribute> doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					Cursor cursor = null;
					String sql = "";
					try {
						if (isSearchUnUsedMailchat) {
							sql = "select  * from (select f_email, _id, f_nick_name, f_spell_name, f_send_count,f_receive_count, f_img_head, f_img_head_hash,f_is_used_mailchat,f_company,f_department,f_position,f_phone,f_addr,f_remarks "
									+ "from tb_user_contacts where f_is_visibility = 1  and f_is_used_mailchat = 0 and f_email like ? ESCAPE '/' and (f_email like ? ESCAPE '/' or f_nick_name like ? ESCAPE '/'))"
									+ " a left join tb_user_contact_remark b on (a.f_email = b.f_r_email) order by a.f_email desc";
						} else {
							sql = "select  * from (select f_email, _id, f_nick_name, f_spell_name, f_send_count,f_receive_count, f_img_head, f_img_head_hash,f_is_used_mailchat,f_company,f_department,f_position,f_phone,f_addr,f_remarks "
									+ "from tb_user_contacts where f_is_visibility = 1 and f_email like ? ESCAPE '/' and (f_email like ? ESCAPE '/' or f_nick_name like ? ESCAPE '/'))"
									+ " a left join tb_user_contact_remark b on (a.f_email = b.f_r_email) order by a.f_email desc";
						}
						String[] selectionArgs = new String[] { "%" + domain,
								"%" + keyWord + "%", "%" + keyWord + "%" };
						cursor = db.rawQuery(sql, selectionArgs);
						while (cursor.moveToNext()) {
							ContactAttribute ca = new ContactAttribute();
							ca.setId(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_ID)));
							ca.setEmail(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_EMAIL)));
							ca.setNickName(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_NICK_NAME)));
							ca.setSpellName(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_SPELL_NAME)));
							ca.setSendCount(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_SEND_COUNT)));
							ca.setReceiveCount(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_RECEIVE_COUNT)));
							ca.setImgHeadPath(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_IMG_HEAD_NAME)));
							ca.setImgHeadHash(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_IMG_HEAD_HASH)));
							ca.setUsedMailchat(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_IS_USED_MAILCHAT)) == 1 ? true
									: false);
							ca.setCompany(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_COMPANY)));
							ca.setDepartment(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_DEPARTMENT)));
							ca.setPosition(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_POSITION)));
							ca.setPhones(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_PHONE)));
							ca.setAddr(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_ADDR)));
							ca.setOtherRemarks(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_REMARKS)));
							getContactRemarkByEmail(ca, cursor);
							contactList.add(ca);
						}
						return contactList;
					} catch (Exception e) {
						throw new WrappedException(e);
					} finally {
						Utility.closeQuietly(cursor);
					}
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}
		return contactList;
	}
	public List<ContactAttribute> searchLocalPersonalContacts(final String domain,final String keyWord,final boolean isSearchUnUsedMailchat,final boolean isEisAccount) throws MessagingException {
		final List<ContactAttribute> contactList = new LinkedList<ContactAttribute>();
		try {
			database.execute(false, new DbCallback<List<ContactAttribute>>() {

				public List<ContactAttribute> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					String sql="";
					try {
						String selectionWhereArgs ="f_email not like ? ESCAPE '/'";
						if(isEisAccount){
							selectionWhereArgs="(f_email not like ? ESCAPE '/' or f_is_add = 1)";
						}
						if (isSearchUnUsedMailchat) {
							sql="select  * from (select distinct f_email, _id, f_nick_name, f_spell_name, f_send_count,f_receive_count, f_img_head, f_img_head_hash,f_is_used_mailchat,f_company,f_department,f_position,f_phone,f_addr,f_remarks " +
									"from tb_user_contacts where f_is_visibility = 1 and f_is_used_mailchat = 0 and "+ selectionWhereArgs +" and (f_email like ? ESCAPE '/' or f_nick_name like ? ESCAPE '/'))" +
									"a left join tb_user_contact_remark b on (a.f_email = b.f_r_email) order by a.f_email desc" ;
						}else{
							sql="select  * from (select distinct f_email,_id, f_nick_name, f_spell_name, f_send_count,f_receive_count, f_img_head, f_img_head_hash,f_is_used_mailchat,f_company,f_department,f_position,f_phone,f_addr,f_remarks " +
									"from tb_user_contacts where f_is_visibility = 1  and "+selectionWhereArgs+" and (f_email like ? ESCAPE '/' or f_nick_name like ? ESCAPE '/'))" +
									" a left join tb_user_contact_remark b on (a.f_email = b.f_r_email) order by a.f_email desc" ;
						}
						cursor = db
								.rawQuery(sql,new String []{"%"+ domain,"%"+ keyWord+"%","%"+ keyWord+"%" });
						while (cursor.moveToNext()) {
							ContactAttribute ca = new ContactAttribute();
							ca.setId(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_ID)));
							ca.setEmail(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_EMAIL)));
							ca.setNickName(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_NICK_NAME)));
							ca.setSpellName(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_SPELL_NAME)));
							ca.setSendCount(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_SEND_COUNT)));
							ca.setReceiveCount(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_RECEIVE_COUNT)));
							ca.setImgHeadPath(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_IMG_HEAD_NAME)));
							ca.setImgHeadHash(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_IMG_HEAD_HASH)));
							ca.setUsedMailchat(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_IS_USED_MAILCHAT))==1? true:false);
							ca.setCompany(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_COMPANY)));
							ca.setDepartment(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_DEPARTMENT)));
							ca.setPosition(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_POSITION)));
							ca.setPhones(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_PHONE)));
							ca.setAddr(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_ADDR)));
							ca.setOtherRemarks(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_REMARKS)));
							getContactRemarkByEmail(ca,cursor);
							contactList.add(ca);
						}
						return contactList;
					} catch (Exception e) {
						throw new WrappedException(e);
					} finally {
						Utility.closeQuietly(cursor);
					}
				}
			});
		} catch (WrappedException e) {
			throw new MessagingException(TAG, e.getCause());
		}

		return contactList;
	}
	public void getContactRemarkByEmail(final ContactAttribute contact,Cursor cursor ){
		contact.setrEmail(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_EMAIL)));
		contact.setrName(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_NAME)));
		contact.setrNickName(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_NICK_NAME)));
		contact.setrImgHeadHash(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_IMG_HEAD_HASH)));
		contact.setrOtherRemarks(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_REMARK)));
		contact.setrPosition(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_POSITION)));
		contact.setrPhones(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_PHONE)));
		contact.setrAddr(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_ADDR)));
		contact.setrDepartment(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_DEPARTMENT)));
		contact.setrCompany(cursor.getString(cursor
				.getColumnIndex(TbContactRemark.F_COMPANY)));
	}
	/**
	 * 
	 * method name: updateEisListExpand function @Description: TODO Parameters
	 * and return values description:
	 * 
	 * @param id
	 *            部門id
	 * @param isExpand
	 *            是否展開
	 * @throws UnavailableStorageException
	 *             field_name void return type
	 * @History memory：
	 * @Date：2015-8-31 下午4:10:29 @Modified by：zhangjx
	 * @Description:
	 */
	public void updateEisListExpandState(final String id, final boolean isExpand)
			throws UnavailableStorageException {
		database.execute(false, new DbCallback<Void>() {

			@Override
			public Void doDbWork(SQLiteDatabase db) throws WrappedException,
					UnavailableStorageException {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TbBusinessContactDepartment.F_IS_OPEN,
						isExpand ? 1 : 0);
				String whereClause = TbBusinessContactDepartment.F_ID + "=?";
				String[] whereArgs = new String[] { id };
				db.update(TbBusinessContactDepartment.TB_NAME, contentValues,
						whereClause, whereArgs);
				return null;
			}
		});
	}
	/**
	 * 聊天列表数据库修改发送通知
	 * @date:2015-9-8
	 */
	private void notifyChattingChange() {
		Uri uri = Uri.withAppendedPath(ChattingProvider.CONTENT_URI, "account/"
				+ uUid + "/chattings");
		mContentResolver.notifyChange(uri, null);
	}
	/**
	 * 获取单聊群聊结果集
	 * 注：排序方式：置顶，置顶时间，最后一条消息时间
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-9-9
	 */
	public Cursor loadCGroupAndDChatCursor(final String cGroupKeyWord,final String dChatKeyWord) throws UnavailableStorageException{
		return database.execute(false, new DbCallback<Cursor>() {

			public Cursor doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				StringBuffer sql =new StringBuffer();
				sql.append("SELECT d.* ,c.f_nick_name, c.f_img_head, c.f_img_head_hash FROM (")
				.append("SELECT cast(a.f_last_senddate as LONG) as 'last_time',cast(a.f_is_sticked as INTEGER) as 'last_is_sticked',cast(a.f_sticked_date as LONG) as 'last_sticked_date', a.*,b.* FROM tb_c_group a LEFT JOIN tb_d_chatlist b on a.f_group_uid =b.f_message_dchat_uid WHERE a.f_is_visibility = ?").append(" UNION ")
				.append("SELECT cast(b.f_last_time as LONG) as 'last_time', cast(b.f_is_sticked as INTEGER) as 'last_is_sticked',cast(b.f_sticked_date as LONG) as 'last_sticked_date',a.*,b.* FROM tb_d_chatlist b LEFT JOIN tb_c_group a on a.f_group_uid =b.f_message_dchat_uid WHERE b.f_is_visibility = ? ")
				.append(") as d LEFT JOIN tb_user_contacts c ON (d.f_to_email = c.f_email)")
				.append("WHERE f_group_name  LIKE ? ESCAPE '/' OR f_last_send_content  LIKE ? ESCAPE '/' OR f_to_email LIKE ? ESCAPE '/' OR f_nick_name LIKE ? ESCAPE '/' OR f_last_message_email LIKE ? ESCAPE '/' OR f_last_message LIKE ? ESCAPE '/'")
				.append(" ORDER BY d.last_is_sticked desc , d.last_sticked_date desc , d.last_time desc");

				Cursor cursor = db.rawQuery(sql.toString(), new String[] { "1",
						"1", "%" + cGroupKeyWord + "%",
						"%" + cGroupKeyWord + "%", "%" + dChatKeyWord + "%",
						"%" + dChatKeyWord + "%", "%" + dChatKeyWord + "%",
						"%" + dChatKeyWord + "%" });
				return cursor;
			}
		});
	}
	//内部版升级数据库时注意删除。
	public void updateDChatSticked() throws UnavailableStorageException{
		database.execute(false, new DbCallback<Cursor>() {

			public Cursor doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				db.execSQL("UPDATE tb_d_chatlist SET f_sticked_date =0 WHERE f_sticked_date =-1");
				notifyChattingChange();
				return null;
			}
		});
	}

	public List<ContactAttribute> searchImportantContactsWith35Eis(final String keyWord)
			throws MessagingException {
		final List<ContactAttribute> contactList = new LinkedList<ContactAttribute>();
		try {
			database.execute(false, new DbCallback<List<ContactAttribute>>() {
				public List<ContactAttribute> doDbWork(final SQLiteDatabase db)
						throws WrappedException {
					Cursor cursor = null;
					try {
						StringBuffer sql = new StringBuffer();
						sql.append("select  * from ").append("(select f.* from (")
								.append("select e.*,f.f_name from ")
								.append("(select c.*,d.f_dep_id,d.f_is_leader ,d.f_position from")
								.append("(select a.f_email, a.f_nick_name, a.f_spell_name, a.f_send_count,a.f_receive_count, a.f_img_head, a.f_img_head_hash,b.f_name,b.f_birthday,b.f_phone,b.f_remark,b.f_address,b.f_img_head_hash from tb_user_contacts AS a left join tb_b_contact_user b on a.f_email = b.f_email order by a.f_receive_count desc limit 10 offset 0) ")
								.append("AS c left join tb_b_contact_department_user d on c.f_email = d.f_user_email)")
								.append("AS e left join tb_b_contact_department f on f.f_id = e.f_dep_id")
								.append(" ) AS f where f.f_email like ? or f.f_nick_name like ? or f.f_name like ? ) a left join tb_user_contact_remark b on (a.f_email = b.f_r_email)");
						cursor = db.rawQuery(sql.toString(), new String[]{"%"+keyWord+"%","%"+keyWord+"%","%"+keyWord+"%"});
						while (cursor.moveToNext()) {
							ContactAttribute ca = new ContactAttribute();
							ca.setEmail(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_EMAIL)));
							ca.setNickName(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_NICK_NAME)));
							ca.setSpellName(cursor.getString(cursor
									.getColumnIndex(TbUserContacts.F_SPELL_NAME)));
							ca.setSendCount(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_SEND_COUNT)));
							ca.setReceiveCount(cursor.getInt(cursor
									.getColumnIndex(TbUserContacts.F_RECEIVE_COUNT)));
							ca.setEisName(cursor.getString(7));
							ca.setDepartment(cursor.getString(16));
							ca.setOtherRemarks(cursor.getString(cursor
									.getColumnIndex(TbBusinessContactUser.F_REMARK)));
							ca.setPosition(cursor.getString(cursor
									.getColumnIndex(TbBusinessContactDepartment_User.F_POSITION)));
							ca.setPhones(cursor.getString(cursor
									.getColumnIndex(TbBusinessContactUser.F_PHONE)));
							ca.setAddr(cursor.getString(cursor
									.getColumnIndex(TbBusinessContactUser.F_ADDR)));
							String string = cursor.getString(cursor
									.getColumnIndex(TbBusinessContactDepartment_User.F_DEP_ID));
							if (string != null) {
								ca.setEisContact(true);
							}
							String imgHeadHash = cursor.getString(6);
							if (imgHeadHash != null) {
								if (imgHeadHash.startsWith("http")) {
									ca.setImgHeadPath(imgHeadHash);
								} else if (!TextUtils.isEmpty(imgHeadHash)) {
									ca.setImgHeadPath(GlobalConstants.HOST_IMG
											+ imgHeadHash);
								}
							} else {
								String imgHash = cursor.getString(12);
								if (imgHash != null) {
									if (imgHash.startsWith("http")) {
										ca.setImgHeadPath(imgHash);
									} else if (!TextUtils.isEmpty(imgHash)
											&& !imgHash.equals("null")) {
										ca.setImgHeadPath(GlobalConstants.HOST_IMG
												+ imgHash);
									}
								}
							}
							getContactRemarkByEmail(ca, cursor);
							contactList.add(ca);
						}
						return contactList;
					} catch (Exception e) {
						throw new WrappedException(e);
					} finally {
						Utility.closeQuietly(cursor);
					}
				}
			});
		} catch (WrappedException e) {
			e.printStackTrace();
		}
		return contactList;
	}

	/**
	 * 搜索Eis35Bean集合
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws UnavailableStorageException
	 * @date:2015-9-23
	 */
	public List<Eis35Bean> searchEis35Beans(final String keyWord,final boolean isSearchUnUsedMailchat) throws UnavailableStorageException{
		return database.execute(true, new DbCallback<List<Eis35Bean>>() {

			@Override
			public List<Eis35Bean> doDbWork(SQLiteDatabase db) throws WrappedException,
					UnavailableStorageException {
				// TODO Auto-generated method stub
				List<Eis35Bean> eis35Beans =new ArrayList<Eis35Bean>();
				StringBuffer sql = new StringBuffer();
				if (isSearchUnUsedMailchat) {
					sql.append("select * from (select * from (select * from (select * from tb_b_contact_user where f_is_used_mailchat = 0 and (f_email like ? or  f_name like ? or f_nick_name like ?)) a left join tb_b_contact_department_user b on (a.f_email = b.f_user_email)) c left join tb_b_contact_department d on (c.f_dep_id = d.f_id)) e left join tb_user_contact_remark f on (e.f_email = f.f_r_email)");
				}else {
					sql.append("select * from (select * from (select * from (select * from tb_b_contact_user where  f_email like ? or  f_name like ? or f_nick_name like ?) a left join tb_b_contact_department_user b on (a.f_email = b.f_user_email)) c left join tb_b_contact_department d on (c.f_dep_id = d.f_id)) e left join tb_user_contact_remark f on (e.f_email = f.f_r_email)");
				}
				Cursor userCursor = db.rawQuery(sql.toString(), new String[]{"%"+keyWord+"%","%"+keyWord+"%","%"+keyWord+"%"});
				while (userCursor.moveToNext()) {
					Eis35Bean userBean = new Eis35Bean();
                    userBean.setId(userCursor.getString(0));
                    String pid = userCursor.getString(18);
                    userBean.setParentId(pid == null ? "0" : pid);
					userBean.setLeader(userCursor.getInt(16)==1 ? true : false);
					userBean.setName(userCursor.getString(1));
					userBean.setEmail(userCursor.getString(0));
					userBean.setMailChatName(userCursor.getString(2));
					userBean.setImgHeadUrl(userCursor.getString(4));
					userBean.setUsedMailchat(userCursor.getInt(11)==1  ? true : false);
					userBean.setParent(false);
					userBean.setDepartment(userCursor.getString(19));
					userBean.setPosition(userCursor.getString(17));
					userBean.setOtherRemarks(userCursor.getString(12));
					userBean.setAddr(userCursor.getString(13));
					userBean.setPhones(userCursor.getString(6));
					getContactRemarkByEmail(userBean,userCursor);
					eis35Beans.add(userBean);
				}
				return eis35Beans;
			}
		});
	}

	/**
	 * 
	 * method name: searchEisAndEmailContact function @Description: TODO
	 * Parameters and return values description:
	 * 
	 * @param keyword
	 * @return field_name List<ContactAttribute> return type
	 * @History memory：
	 * @Date：2015-10-19 上午11:16:04 @Modified by：zhangjx
	 * @Description:35用户搜索全部联系人
	 */
	public List<ContactAttribute> searchEisAndEmailContact(String keyword) {
		return null;
	}
	/**
	 * 更新单聊条目未处理标记
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws UnavailableStorageException
	 * @date:2015-11-10
	 */
	public void updateDChatUnTreatedFlag(final DChat dChat) throws UnavailableStorageException{
		database.execute(false, new DbCallback<Void>() {
			@Override
			public Void doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				ContentValues values = new ContentValues();
				values.put(TbDchat.F_IS_UNTREATED, dChat.isUnTreated()? "1":"0");
				String whereClause = TbDchat.F_UID + "=?";
				String[] whereArgs = { dChat.getUid()};
				int result = db.update(TbDchat.TB_NAME, values, whereClause, whereArgs);
				if (result > 0) {
					notifyChattingChange();
				}
				return null;
			}
		});
	}
//	public void saveContactRemark(final ContactAttribute contactAttribute)
//			throws MessagingException {
//		database.execute(false, new DbCallback<Void>() {
//
//			@Override
//			public Void doDbWork(SQLiteDatabase db) throws WrappedException,
//					UnavailableStorageException {
//				ContentValues cv = new ContentValues();
//				cv.put(TbContactRemark.F_EMAIL, contactAttribute.getEmail());
//				cv.put(TbContactRemark.F_NICK_NAME,
//						contactAttribute.getNickName());
//				cv.put(TbContactRemark.F_NAME, contactAttribute.getName());
//				cv.put(TbContactRemark.F_IMG_HEAD_HASH,
//						contactAttribute.getImgHeadHash());
//				cv.put(TbContactRemark.F_REMARK,
//						contactAttribute.getOtherRemarks());
//				cv.put(TbContactRemark.F_PHONE, contactAttribute.getPhones());
//				cv.put(TbUserContacts.F_DEPARTMENT,
//						contactAttribute.getDepartment());
//				cv.put(TbUserContacts.F_POSITION,
//						contactAttribute.getPosition());
//				cv.put(TbUserContacts.F_ADDR, contactAttribute.getAddr());
//				insertOrIgnore("tb_user_contacts", cv);
//				return null;
//			}
//		});
//	}

	public void updateContactAttribute(final ContactAttribute contact) {
		try {
			database.execute(false, new DbCallback<Void>() {

				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					String id = contact.getId();
					String email = contact.getEmail();
					String nickName = contact.getNickName();
					String company= contact.getCompany();
					String department= contact.getDepartment();
					String position= contact.getPosition();
					String phone= contact.getPhones();
					String addr= contact.getAddr();
					String other= contact.getOtherRemarks();
					boolean isUsedMailchat = contact.isUsedMailchat();
					ContentValues cv = new ContentValues();
					cv.put("f_email", StringUtil.getEmailSuffix(email));
					if (!StringUtils.isNullOrEmpty(nickName)) {
						cv.put("f_nick_name", nickName);
					}
					cv.put("f_spell_name", HanziToPinyin.toPinyin(email
							.substring(0, email.indexOf("@"))));
					cv.put("f_send_count", contact.getSendCount());
//					cv.put("f_receive_count",1);
					cv.put("f_img_head", contact.getImgHeadPath());
					cv.put("f_img_head_hash", contact.getImgHeadHash());
					cv.put("f_date", contact.getDate());
					cv.put("f_is_used_mailchat",isUsedMailchat);
					cv.put(TbUserContacts.F_COMPANY, company);
					cv.put(TbUserContacts.F_DEPARTMENT,department);
					cv.put(TbUserContacts.F_POSITION, position);
					cv.put(TbUserContacts.F_ADDR, addr);
					cv.put(TbUserContacts.F_PHONE, phone);
					cv.put(TbUserContacts.F_REMARKS,other);
					// db.insert("tb_user_contacts", null, cv);
					db.update("tb_user_contacts", cv, "_id = ?",
							new String[] { id });
					//插入数据到remark表
					ContactAttribute contactAttribute=new ContactAttribute(contact.getEmail(), null,
							contact.getrNickName(), contact.getrImgHeadHash(), contact.getrCompany(),
							null, contact.getrDepartment(), contact.getrPosition(), contact.getrPhones(), 
							contact.getrAddr(), contact.getrOtherRemarks(), DateUtil.dateToString(new Date(), DateUtil.LONG_DATE_FORMAT));
					insertContactRemark(contactAttribute);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新联系人的设置值
	 * (无唯一标示，无法更新，后期可以在编辑表中加个ID字段，关联个人联系人中的_ID字段【注：因为EIS联系人编辑时使用的是replace方法,替换是需要将_ID值设置为一起替换】)
	 *
	 * @Description:
	 * @param ContactAttribute 已经赋值的个人联系人
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-12-24
	 */
//	public void updateContactRemark(SQLiteDatabase db,ContactAttribute contactAttribute) {
//		String rEmail = contactAtstribute.getrEmail();
//		String rNickName = contactAttribute.getrNickName();
//		String rName= contactAttribute.getrName();
//		String rImgHeadHash = contactAttribute.getrImgHeadHash();
//		String rOtherRemarks = contactAttribute.getrOtherRemarks();
//		String rPhones = contactAttribute.getrPhones();
//		String rDepartment = contactAttribute.getrDepartment();
//		String rPosition = contactAttribute.getrPosition();
//		String rAddr = contactAttribute.getrAddr();
//		String rCompany = contactAttribute.getrCompany();
//		String rData = contactAttribute.getrData();
//
//		ContentValues cv = new ContentValues();
//		if (!StringUtils.isNullOrEmpty(rEmail)) {
//			cv.put(TbContactRemark.F_EMAIL, rEmail);
//		}
//		if (!StringUtils.isNullOrEmpty(rNickName)) {
//			cv.put(TbContactRemark.F_NICK_NAME, rNickName);
//		}
//		if (!StringUtils.isNullOrEmpty(rName)) {
//			cv.put(TbContactRemark.F_NAME, rName);
//		}
//		if (!StringUtils.isNullOrEmpty(rImgHeadHash)) {
//			cv.put(TbContactRemark.F_IMG_HEAD_HASH, rImgHeadHash);
//		}
//		if (!StringUtils.isNullOrEmpty(rOtherRemarks)) {
//			cv.put(TbContactRemark.F_REMARK, rOtherRemarks);
//		}
//		if (!StringUtils.isNullOrEmpty(rPhones)) {
//			cv.put(TbContactRemark.F_PHONE, rPhones);
//		}
//		if (!StringUtils.isNullOrEmpty(rDepartment)) {
//			cv.put(TbContactRemark.F_DEPARTMENT, rDepartment);
//		}
//		if (!StringUtils.isNullOrEmpty(rPosition)) {
//			cv.put(TbContactRemark.F_POSITION, rPosition);
//		}
//		if (!StringUtils.isNullOrEmpty(rAddr)) {
//			cv.put(TbContactRemark.F_ADDR, rAddr);
//		}
//		if (!StringUtils.isNullOrEmpty(rCompany)) {
//			cv.put(TbContactRemark.F_COMPANY, rCompany);
//		}
//		if (!StringUtils.isNullOrEmpty(rData)) {
//			cv.put(TbContactRemark.F_DATE, rData);
//		}
//		db.update("tb_user_contact_remark", cv, TbContactRemark.F_EMAIL +" = ?" , new String[] { rEmail });
//	}

	/**
	 * 获取联系人在EIS中及自己标记的信息(部门职位等)
	 * 注：聊天生成联系人获取信息使用。
	 *
	 * @Description:
	 * @param ContactAttribute 已经赋值的个人联系人表的相关信息
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws UnavailableStorageException
	 * @date:2015-11-27
	 */
	public ContactAttribute getContactsInfoForChat(final ContactAttribute contact,final boolean isEisAccount) throws UnavailableStorageException{
		return database.execute(false, new DbCallback<ContactAttribute>() {
			public ContactAttribute doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				StringBuffer sql = new StringBuffer();
				Cursor cursor = null;
				try {
					if (isEisAccount) {
						//获取EIS相关信息
						sql.append("select  * from (")
								.append("select e.*,f.f_name from ")
								.append("(select c.*,d.f_dep_id,d.f_is_leader ,d.f_position from")
								.append("(select * from tb_b_contact_user where f_email =?) ")
								.append("AS c left join tb_b_contact_department_user d on c.f_email = d.f_user_email)")
								.append("AS e left join tb_b_contact_department f on f.f_id = e.f_dep_id ")
								.append(") g left join tb_user_contact_remark h on (g.f_email = h.f_r_email)");

						cursor = db.rawQuery(sql.toString(),
								new String[] { contact.getEmail() });
						int count = cursor.getCount();
						if (count>0) {
							while (cursor.moveToNext()) {
								if(StringUtil.isEmpty(contact.getDepartment())){
									contact.setName(cursor.getString(1));
									contact.setNickName(cursor.getString(1));
									contact.setCompany(cursor.getString(cursor.getColumnIndex(TbBusinessContactUser.F_COMPANY)));
									contact.setDepartment(cursor.getString(18));
									contact.setPosition(cursor.getString(cursor.getColumnIndex(TbBusinessContactDepartment_User.F_POSITION)));
									contact.setAddr(cursor.getString(13));
									contact.setPhones(cursor.getString(6));
									contact.setOtherRemarks(cursor.getString(12));
									contact.setEisContact(true);
									if (contact.getImgHeadHash() == null) {
										String avatarHash = cursor.getString(4);
										if (avatarHash != null
												&& !avatarHash.equals("null")) {
											contact.setImgHeadHash(avatarHash);
											contact.setImgHeadPath(GlobalConstants.HOST_IMG
													+ avatarHash);
										}
									}
								}else{
									String department = cursor.getString(18);
									if(!StringUtil.isEmpty(department)){
										contact.setDepartment(contact.getDepartment()+" , "+department);
									}
								}
								getContactRemarkByEmail(contact, cursor);
							}
						}else{
							getContactsInfoWithPersonal(db,cursor,contact);
						}
					} else {
						getContactsInfoWithPersonal(db,cursor,contact);
					}
					return contact;
				} finally {
					Utility.closeQuietly(cursor);
				}
			}
		});
	}
	private void getContactsInfoWithPersonal(SQLiteDatabase db,Cursor cursor,ContactAttribute contact){
		StringBuffer sql = new StringBuffer();
		sql.append("select  * from ")
				.append("(select * from tb_user_contacts where f_email = ?)")
				.append(" a left join tb_user_contact_remark b on (a.f_email = b.f_r_email)");
		cursor = db.rawQuery(sql.toString(),
				new String[] { contact.getEmail() });
		if (cursor.moveToFirst()) {
			contact.setNickName(cursor.getString(cursor
					.getColumnIndex(TbUserContacts.F_NICK_NAME)));
			contact.setSpellName(cursor.getString(cursor
					.getColumnIndex(TbUserContacts.F_SPELL_NAME)));
			String imgHeadHash = cursor.getString(cursor
					.getColumnIndex(TbUserContacts.F_IMG_HEAD_HASH));
			setUserHeadUrl(contact, imgHeadHash);
			contact.setImgHeadHash(cursor.getString(cursor
					.getColumnIndex(TbUserContacts.F_IMG_HEAD_HASH)));
			contact.setEisContact(false);
			contact.setEisName("");
			contact.setCompany(cursor.getString(cursor
					.getColumnIndex(TbUserContacts.F_COMPANY)));
			contact.setDepartment(cursor.getString(cursor
					.getColumnIndex(TbUserContacts.F_DEPARTMENT)));
			contact.setPosition(cursor.getString(cursor
					.getColumnIndex(TbUserContacts.F_POSITION)));
			contact.setPhones(cursor.getString(cursor
					.getColumnIndex(TbUserContacts.F_PHONE)));
			contact.setAddr(cursor.getString(cursor
					.getColumnIndex(TbUserContacts.F_ADDR)));
			contact.setOtherRemarks(cursor.getString(cursor
					.getColumnIndex(TbUserContacts.F_REMARKS)));
			getContactRemarkByEmail(contact, cursor);
		}
	}
//	public void updateEisContactHeadImg(final ContactAttribute contact) {
//		Log.d("qxian", contact.getImgHeadHash());
//		try {
//			database.execute(false, new DbCallback<Void>() {
//
//				@Override
//				public Void doDbWork(SQLiteDatabase db)
//						throws WrappedException, UnavailableStorageException {
//					String email = contact.getEmail();
//					ContentValues cv = new ContentValues();
//					cv.put("f_img_head_hash", contact.getImgHeadHash());
//					db.update("tb_b_contact_user", cv, "f_email = ?",
//							new String[] { email });
//					return null;
//				}
//			});
//		} catch (UnavailableStorageException e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * 查询联系人是否存在（先从个人联系人查询，然后从EIS联系人中查询（如果是EIS账户））
	 *
	 * @Description:
	 * @param email
	 * @param isEisAccount 是否为EIS的账户
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws UnavailableStorageException
	 * @date:2015-12-24
	 */
	public boolean isContactAttributeExist(final String email,
			final boolean isEisAccount) throws UnavailableStorageException {
		return database.execute(false, new DbCallback<Boolean>() {
			public Boolean doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				boolean isContactAttributeExist = false;
				String sql ="";
				Cursor cursor = null;
				try {
					sql = "select * from tb_user_contacts where f_is_visibility = 1  and f_email = ?";
					cursor = db.rawQuery(sql, new String[] { email });
					if (cursor.moveToFirst()) {
						isContactAttributeExist = true;
					}
					if (isEisAccount && !isContactAttributeExist) {
						sql = "select * from tb_b_contact_user where f_email =?";
						cursor = db.rawQuery(sql,new String[] { email });
						if (cursor.moveToFirst()) {
							isContactAttributeExist = true;
						}
					}
				} finally {
					Utility.closeQuietly(cursor);
				}
				return isContactAttributeExist;
			}
		});
	}
	/**
	 * 获取所有群聊消息该类型的附件uid
	 *
	 * @Description:
	 * @param email
	 * @param isEisAccount 是否为EIS的账户
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws UnavailableStorageException
	 * @date:2016-2-2
	 */
	public List<String> listTypeCAttachmentUid(final int type) throws UnavailableStorageException{
		return database.execute(false, new DbCallback<List<String>>() {
			public List<String> doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				List<String> cUids = new ArrayList<String>();
				String sql ="";
				Cursor cursor = null;
				try {
					sql = "select b.f_uid from (select f_uid from tb_c_message where f_messagetype = ?) AS a LEFT JOIN tb_c_attachments AS b on (a.f_uid = b.f_cmessages_uid)";
					cursor = db.rawQuery(sql, new String[] { type+""});
					while(cursor.moveToNext()) {
						cUids.add(cursor.getString(0));
					}
				} finally {
					Utility.closeQuietly(cursor);
				}
				return cUids;
			}
		});
	}
	/**
	 * 获取所有单聊消息该类型的附件uid
	 *
	 * @Description:
	 * @param email
	 * @param isEisAccount 是否为EIS的账户
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws UnavailableStorageException
	 * @date:2016-2-2
	 */
	public List<String> listTypeDAttachmentUid(final int type) throws UnavailableStorageException{
		return database.execute(false, new DbCallback<List<String>>() {
			public List<String> doDbWork(final SQLiteDatabase db)
					throws WrappedException {
				List<String> dUids = new ArrayList<String>();
				String sql ="";
				Cursor cursor = null;
				try {
					sql = "select b.f_uid from (select f_message_uid from tb_d_chat_message where f_messagetype = ?) AS a LEFT JOIN tb_d_chat_attachment AS b on (a.f_message_uid = b.f_message_uid)";
					cursor = db.rawQuery(sql, new String[] { type+""});
					while (cursor.moveToNext()) {
						dUids.add(cursor.getString(0));
					}
				} finally {
					Utility.closeQuietly(cursor);
				}
				return dUids;
			}
		});
	}

	/**
	 * 更新群附件(图片等)宽高
	 *
	 * @Description:
	 * @param: CAttachment
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @throws UnavailableStorageException
	 * @date:2016-2-25
	 */
	public void updateCAttWidthAndHeight(final String uid,final int width,final int height) throws UnavailableStorageException  {
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					ContentValues contentValues = new ContentValues();
					contentValues.put(TbCAttachments.F_WIDTH, width);
					contentValues.put(TbCAttachments.F_HEIGHT, height);
					String whereClause = TbCAttachments.F_UID + "=?";
					String[] whereArgs = {uid};
					db.update(TbCAttachments.TB_NAME, contentValues, whereClause, whereArgs);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 更新单聊附件(图片等)宽高
	 *
	 * @Description:
	 * @param: CAttachment
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @throws UnavailableStorageException
	 * @date:2016-2-25
	 */
	public void updateDAttWidthAndHeight(final String uid,final int width,final int height) throws UnavailableStorageException  {
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					ContentValues contentValues = new ContentValues();
					contentValues.put(TbDAttachments.F_WIDTH, width);
					contentValues.put(TbDAttachments.F_HEIGHT, height);
					String whereClause = TbDAttachments.F_UID + "=?";
					String[] whereArgs = {uid};
					db.update(TbDAttachments.TB_NAME, contentValues, whereClause, whereArgs);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 更新群附件(图片等)是否使用自动下载
	 *
	 * @Description:
	 * @param: CAttachment
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @throws UnavailableStorageException
	 * @date:2016-2-25
	 */
	public void updateCAttImageLoadStata(final String uid,final boolean isImageLoad,final boolean isAllUpdate) throws UnavailableStorageException  {
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					ContentValues contentValues = new ContentValues();
					contentValues.put(TbCAttachments.F_IS_IMAGE_LOAD, isImageLoad?0:1);
					String whereClause=null;
					String[] whereArgs=null;
					if(!isAllUpdate){
						whereClause = TbCAttachments.F_UID + "=?";
						whereArgs = new String[] {uid};
					}
					db.update(TbCAttachments.TB_NAME, contentValues, whereClause, whereArgs);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 更新单聊附件(图片等)宽高
	 *
	 * @Description:
	 * @param: CAttachment
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @throws UnavailableStorageException
	 * @date:2016-2-25
	 */
	public void updateDAttImageLoadStata(final String uid,final boolean isImageLoad,final boolean isAllUpdate) throws UnavailableStorageException  {
		try {
			database.execute(false, new DbCallback<Void>() {
				@Override
				public Void doDbWork(SQLiteDatabase db)
						throws WrappedException, UnavailableStorageException {
					// TODO Auto-generated method stub
					ContentValues contentValues = new ContentValues();
					contentValues.put(TbDAttachments.F_IS_IMAGE_LOAD, isImageLoad?0:1);
					String whereClause=null;
					String[] whereArgs=null;
					if(!isAllUpdate){
						whereClause = TbDAttachments.F_UID + "=?";
						whereArgs = new String[] {uid};
					}
					db.update(TbDAttachments.TB_NAME, contentValues, whereClause, whereArgs);
					return null;
				}
			});
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
