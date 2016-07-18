package com.c35.mtd.pushmail.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.GlobalConstants;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.logic.C35AccountManager;
import com.c35.mtd.pushmail.store.LocalStore;
import com.c35.mtd.pushmail.store.Store;
import com.c35.mtd.pushmail.util.MimeUtility;
import com.c35.mtd.pushmail.util.StoreDirectory;

/**
 * 提供mail的附件列表
 * 
 * @Description:
 * @author: zhuanggy
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class AttachmentProvider extends ContentProvider {

	public static final String TAG = "AttachmentProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://com.c35.mtd.pushmail.attachmentprovider");

	private static final String FORMAT_RAW = "RAW";
	private static final String FORMAT_THUMBNAIL = "THUMBNAIL";

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
	public static class AttachmentProviderColumns {

		public static final String _ID = "_id";
		public static final String DATA = "_data";
		public static final String DISPLAY_NAME = "_display_name";
		public static final String SIZE = "_size";
		public static final String CONTENT_ID = "_content_id";
	}

	@Override
	public boolean onCreate() {
		/*
		 * We use the cache dir as a temporary directory (since Android doesn't give us one) so on startup
		 * we'll clean up any .tmp files from the last run.
		 */
		// Debug.d(TAG, "onCreate()->getContext().getCacheDir():" +
		// getContext().getCacheDir().getAbsolutePath());
		File[] files = getContext().getCacheDir().listFiles();
		for (File file : files) {
			if (file.getName().endsWith(".tmp")) {
				file.delete();
			}
		}
		return true;
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		List<String> segments = uri.getPathSegments();
		String dbName = segments.get(0);
		String id = segments.get(1);
		String format = segments.get(2);
		String attPath = "";
		Account account = EmailApplication.getCurrentAccount();
		int localStorType = account.getLSUriType();
		attPath = getAttPath(localStorType, dbName, id);
		if (FORMAT_THUMBNAIL.equals(format)) {
			int width = Integer.parseInt(segments.get(3));
			int height = Integer.parseInt(segments.get(4));
			String filename = "thmb_" + dbName + "_" + id;
			File dir = getContext().getCacheDir();
			File file = new File(dir, filename);
			if (!file.exists()) {
				Uri attachmentUri = getAttachmentUri(id);// Long.parseLong(id)
				String type = getType(attachmentUri);
				FileInputStream in = null;
				FileOutputStream out = null;
				try {
					in = new FileInputStream(new File(attPath));
					out = new FileOutputStream(file);
					Bitmap thumbnail = createThumbnail(type, in);
					thumbnail = thumbnail.createScaledBitmap(thumbnail, width, height, true);
					thumbnail.compress(Bitmap.CompressFormat.PNG, 100, out);

				} catch (IOException ioe) {
					Debug.e("failfast", "failfast_AA", ioe);
					return null;
				} finally {
					try {
						out.close();
						in.close();
					} catch (Exception e) {
						Debug.e("failfast", "failfast_AA", e);
					}
				}
			}
			return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
		} else {
			// return ParcelFileDescriptor.open(
			// new File(getContext().getDatabasePath(dbName + "_att"), id),
			// ParcelFileDescriptor.MODE_READ_ONLY);

			return ParcelFileDescriptor.open(new File(attPath), ParcelFileDescriptor.MODE_READ_ONLY);
		}
	}

	/**
	 * 获得附件路径（String）
	 * 
	 * @Description:
	 * @param localStorType
	 * @param dbName
	 * @param id
	 * @return
	 * @see:
	 * @since:
	 * @date:2012-11-2
	 */
	private String getAttPath(int localStorType, String dbName, String id) {
		String path = "";
		switch (localStorType) {
		case StoreDirectory.STORAGE_TYPE_MOVINAND:
			path = StoreDirectory.getMoviNandStoragePath() + "/" + GlobalConstants.MAIL_DIRECTORY + "/database/" + dbName + "_att" + "/" + id;
			break;
		case StoreDirectory.STORAGE_TYPE_SDCARD:
			path = StoreDirectory.getExternalStoragePath() + "/" + GlobalConstants.MAIL_DIRECTORY + "/database/" + dbName + "_att" + "/" + id;
			break;
		case StoreDirectory.STORAGE_TYPE_MOVICARD:
			path = getContext().getDatabasePath(dbName) + "_att" + "/" + id;
			break;
		default:
			path = getContext().getDatabasePath(dbName) + "_att" + "/" + id;
			break;
		}
		if (path.equals("")) {
			path = getContext().getDatabasePath(dbName) + "_att" + "/" + id;
		}
		return path;
	}

	/**
	 * 获得附件存储在本地的URI
	 * 
	 * @Description:
	 * @param id
	 * @return
	 * @see:
	 * @since:
	 * @date:2012-11-2
	 */
	public static Uri getAttachmentUri(String id) {
		return CONTENT_URI.buildUpon().appendPath(GlobalConstants.DATABASE_NAME).appendPath(id).appendPath(FORMAT_RAW).build();
	}

	private Bitmap createThumbnail(String type, InputStream data) {
		if (MimeUtility.mimeTypeMatches(type, "image/*")) {
			return createImageThumbnail(data);
		}
		return null;
	}

	private Bitmap createImageThumbnail(InputStream data) {
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(data);
			return bitmap;
		} catch (OutOfMemoryError oome) {
			/*
			 * Improperly downloaded images, corrupt bitmaps and the like can commonly cause OOME due to
			 * invalid allocation sizes. We're happy with a null bitmap in that case. If the system is really
			 * out of memory we'll know about it soon enough.
			 */
			Debug.e("failfast", "failfast_AA", oome);
			return null;
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
			return null;
		}
	}

	@Override
	public String getType(Uri uri) {
		List<String> segments = uri.getPathSegments();
		String attachmentId = segments.get(1);
		String format = segments.get(2);
		String accountUuid = segments.get(0);
		Account account = C35AccountManager.getInstance().getAccount(accountUuid);
		String type = "";
		if (FORMAT_THUMBNAIL.equals(format)) {
			type = "image/png";
		} else {
			try {
				LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
				type = localStore.getAttachmentType(accountUuid, attachmentId);
			} catch (MessagingException e) {
				Debug.e("failfast", "failfast_AA", e);
			}
		}
		return type;
	}

	@Override
	public int delete(Uri uri, String arg1, String[] arg2) {
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// if (projection == null) {
		// projection = new String[] { AttachmentProviderColumns._ID, AttachmentProviderColumns.DATA, };
		// }
		//
		// List<String> segments = uri.getPathSegments();
		// String dbName = segments.get(0);
		// String id = segments.get(1);
		// long accountId = Long.parseLong(segments.get(0));
		// Account account = AccountManager.getInstance(getContext()).getAccount(accountId);
		// // String path = getContext().getDatabasePath(dbName).getAbsolutePath();
		// String path = getPath(account.getLSUriType(), dbName);
		// String name = null;
		// int size = -1;
		// String contentId = null;
		// SQLiteDatabase db = null;
		// Cursor cursor = null;
		// try {
		// db = SQLiteDatabase.openDatabase(path, null, 0);
		// cursor = db.query("attachments", new String[] { "name", "size", "content_id" }, "id = ?", new
		// String[] { id }, null, null, null);
		// if (!cursor.moveToFirst()) {
		// return null;
		// }
		// name = cursor.getString(0);
		// size = cursor.getInt(1);
		// contentId = cursor.getString(2);
		// } finally {
		// if (cursor != null) {
		// cursor.close();
		// }
		// if (db != null) {
		// db.close();
		// }
		// }
		//
		// MatrixCursor ret = new MatrixCursor(projection);
		// Object[] values = new Object[projection.length];
		// for (int i = 0, count = projection.length; i < count; i++) {
		// String column = projection[i];
		// if (AttachmentProviderColumns._ID.equals(column)) {
		// values[i] = id;
		// } else if (AttachmentProviderColumns.DATA.equals(column)) {
		// values[i] = uri.toString();
		// } else if (AttachmentProviderColumns.DISPLAY_NAME.equals(column)) {
		// values[i] = name;
		// } else if (AttachmentProviderColumns.SIZE.equals(column)) {
		// values[i] = size;
		// } else if (AttachmentProviderColumns.CONTENT_ID.equals(column)) {
		// values[i] = contentId;
		// }
		// }
		// ret.addRow(values);
		// return ret;
		// TODO: finish this 6/12 2:42
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}
}
