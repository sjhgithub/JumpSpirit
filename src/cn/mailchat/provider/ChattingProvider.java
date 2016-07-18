package cn.mailchat.provider;

import java.util.List;

import cn.mailchat.Account;
import cn.mailchat.Preferences;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.mail.store.LocalStore;
import cn.mailchat.mail.store.LockableDatabase;
import cn.mailchat.mail.store.Columns.TbCGroup;
import cn.mailchat.mail.store.LockableDatabase.DbCallback;
import cn.mailchat.mail.store.LockableDatabase.WrappedException;
import cn.mailchat.mail.store.UnavailableStorageException;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * 聊天ContentProvider
 * 
 * @Description:
 * @see:
 * @since:
 * @author: shengli
 * @date:2015-9-6
 */
public class ChattingProvider extends ContentProvider {
	private static final UriMatcher sUriMatcher;
	public static final String AUTHORITY = "cn.mailchat.provider.chatting";
	public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY);
	private static final int CHATTING_BASE = 0;
	private static final int CHATTINGS = CHATTING_BASE + 0;
	private static final int CHATTINGS_SEARCH = CHATTING_BASE + 1;
	private ContentResolver contentResolver;
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, "account/*/chattings", CHATTINGS);
		sUriMatcher.addURI(AUTHORITY, "account/*/chattings/search/*", CHATTINGS_SEARCH);
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		contentResolver = getContext().getContentResolver();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		int match = sUriMatcher.match(uri);
		if (match < 0) {
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		List<String> segments = uri.getPathSegments();
        String accountUuid = segments.get(1);
        Cursor cursor =null;
        Uri notificationUri = Uri.withAppendedPath(CONTENT_URI, "account/" + accountUuid +
                "/chattings");
        switch (match) {
		case CHATTINGS:
			cursor = searchChattingList(accountUuid,"","");
            cursor.setNotificationUri(contentResolver, notificationUri);
			break;
		case CHATTINGS_SEARCH:
			String keyword = segments.get(4);
			cursor = searchChattingList(accountUuid,keyword,keyword);
            cursor.setNotificationUri(contentResolver, notificationUri);
			break;
		default:
			break;
		}
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		throw new RuntimeException("not implemented yet");
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		throw new RuntimeException("not implemented yet");
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			final String[] selectionArgs) {
		// TODO Auto-generated method stub
		throw new RuntimeException("not implemented yet");
	}

	private Preferences mPreferences;

	private Account getAccount(String accountUuid) {
		if (mPreferences == null) {
			Context appContext = getContext().getApplicationContext();
			mPreferences = Preferences.getPreferences(appContext);
		}

		Account account = mPreferences.getAccount(accountUuid);
		// TODO 删除账号后再进入应用系统崩溃，暂时处理,获取默认账号
		if (account == null) {
			account = mPreferences.getDefaultAccount();
		}
		// zhangyq1 UUID: 485b1fd8-8999-44a8-8ddb-46f975b3a026 account == null
		if (account == null) {
			throw new IllegalArgumentException("Unknown account: "
					+ accountUuid);
		}

		return account;
	}

	private LockableDatabase getDatabase(Account account) {
		LocalStore localStore;
		try {
			localStore = account.getLocalStore();
		} catch (MessagingException e) {
			throw new RuntimeException("Couldn't get LocalStore", e);
		}

		return localStore.getDatabase();
	}

	private LocalStore getLocalStore(Account account) {
		LocalStore localStore;
		try {
			localStore = account.getLocalStore();
		} catch (MessagingException e) {
			throw new RuntimeException("Couldn't get LocalStore", e);
		}

		return localStore;
	}

	private Cursor searchChattingList(String accountUuid,String cGroupKeyWord,String dChatKeyWord) {
		Account account = getAccount(accountUuid);
		Cursor cursor =null;
		try {
			cursor = account.getLocalStore().loadCGroupAndDChatCursor(cGroupKeyWord,dChatKeyWord);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cursor;
	}
}
