package com.c35.mtd.pushmail.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.Contacts.People;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Intents;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.R;

/**
 * 关于操作系统通讯录的一些工具方法
 * 
 * @author Administrator
 * 
 */

public class ContactUitl {

	/**
	 * 通过邮件地址 获得 联系人 图片
	 * 
	 * @param mailAddress
	 * @return
	 */
    // Modified by LL
    /*
	public static Bitmap getImageByMailAddress(String mailAddress) {
		if (mailAddress != null && !"".equals(mailAddress)) {
			ContentResolver contentResolver = EmailApplication.getInstance().getContentResolver();
			String[] selectionArgs = new String[] { Email.CONTENT_ITEM_TYPE, mailAddress };
			Cursor cursor = contentResolver.query(Data.CONTENT_URI, new String[] { Email.RAW_CONTACT_ID }, Data.MIMETYPE + "=? AND " + Email.DATA1 + "=?", selectionArgs, null);
			if (cursor != null) {
				try {
					while (cursor.moveToNext()) {
						int rawcontactId = cursor.getInt(cursor.getColumnIndex(Email.RAW_CONTACT_ID));
						Cursor c = contentResolver.query(Data.CONTENT_URI, new String[] { Photo.PHOTO }, Data.MIMETYPE + "='" + Photo.CONTENT_ITEM_TYPE + "' AND " + Photo.RAW_CONTACT_ID + "=" + rawcontactId, null, null);
						if (c != null) {
							try {
								while (c.moveToNext()) {
									byte[] b = c.getBlob(c.getColumnIndex(Photo.PHOTO));
									if (null != b) {
										return BitmapFactory.decodeByteArray(b, 0, b.length);
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
								Debug.e("failfast", "failfast_AA", e);
							} finally {
								c.close();
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					Debug.e("failfast", "failfast_AA", e);
				} finally {
					cursor.close();
				}
			}
		}
		return BitmapFactory.decodeResource(EmailApplication.getInstance().getResources(), R.drawable.mail_contect_photo);
	}
	*/

	/**
	 * 跳到播打电话的应用
	 */
	public static void makeCall(Activity activity, String telNumber) {
		try {
			Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telNumber));
			activity.startActivity(intent);
		} catch (Exception e) {
			// TODO: handle exception
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 跳到发短信的应用
	 */
	public static void sendSMS(Activity activity, String telNumber) {
		try {
			Uri smsToUri = Uri.parse("smsto:" + telNumber);
			Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
			activity.startActivity(intent);
		} catch (Exception e) {
			// TODO: handle exception
			Debug.e("failfast", "failfast_AA", e);
		}

	}

	/**
	 * 判断通讯录中是否存在该联系人，用mail检索
	 * 
	 * @param emailAdderss
	 * @return
	 */
	public static Boolean contactIsExist(Context activity, String emailAdderss) {
		if (emailAdderss != null) {
			ContentResolver contentResolver = activity.getContentResolver();
			String[] selectionArgs = new String[] { Email.CONTENT_ITEM_TYPE, emailAdderss };
			Cursor cursor = contentResolver.query(Data.CONTENT_URI, new String[] { Email.RAW_CONTACT_ID }, Data.MIMETYPE + "=? AND " + Email.DATA1 + "=?", selectionArgs, null);
			int n = cursor.getCount();
			cursor.close();
			if (n > 0) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * 根据邮箱地址获取手机号码
	 * 
	 * @param mailAdderss
	 * @return
	 */
	public static List<String> getTelNumByMail(Context activity, String emailAdderss) {
		if (contactIsExist(activity, emailAdderss)) {
			ContentResolver contentResolver = activity.getContentResolver();
			String[] selectionArgs = new String[] { Email.CONTENT_ITEM_TYPE, emailAdderss };
			Cursor cursor = contentResolver.query(Data.CONTENT_URI, new String[] { Email.RAW_CONTACT_ID }, Data.MIMETYPE + "=? AND " + Email.DATA1 + "=?", selectionArgs, null);
			ArrayList<String> phoneList = new ArrayList<String>();
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int rawcontactId = cursor.getInt(cursor.getColumnIndex(Email.RAW_CONTACT_ID));
					Cursor c = contentResolver.query(Data.CONTENT_URI, new String[] { Phone.NUMBER }, Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "' AND " + Phone.RAW_CONTACT_ID + "=" + rawcontactId, null, null);
					if (c != null) {
						while (c.moveToNext()) {
							String number = c.getString(c.getColumnIndex(Phone.NUMBER));
							phoneList.add(number);
						}
						c.close();
					}
				}
				cursor.close();
			}
			return phoneList;
		} else {
			return null;
		}

	}

	/**
	 * 编辑当前用户的联系人操作
	 * 
	 * @param activity
	 * @param emailAdderss
	 */
	public static void editContact(Activity activity, String emailAdderss) {
		Integer id = null;
		ContentResolver contentResolver = activity.getContentResolver();
		String[] selectionArgs = new String[] { Email.CONTENT_ITEM_TYPE, emailAdderss };
		Cursor cursor = contentResolver.query(Data.CONTENT_URI, new String[] { Email.RAW_CONTACT_ID }, Data.MIMETYPE + "=? AND " + Email.DATA1 + "=?", selectionArgs, null);
		cursor.moveToFirst();
		id = cursor.getInt(cursor.getColumnIndex(Data.RAW_CONTACT_ID));
		cursor.close();
		try {
			if (Build.VERSION.SDK_INT == 4 || Build.VERSION.RELEASE.equals("1.6")) {
				// for sdk1.6
				Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse("content://contacts/people/" + id));
				activity.startActivity(intent);
			} else {
				// for sdk2.1
				Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse("content://com.android.contacts/raw_contacts/" + id));
				activity.startActivity(intent);
			}
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 添加当前用户为本机联系人
	 * 
	 * @param activity
	 * @param emailAdderss
	 * @param name
	 * @param phone
	 * @param curContact
	 */
	public static void addContact(Activity activity, String emailAdderss, String name) {
		try {
			Intent intent = new Intent(Intent.ACTION_INSERT, People.CONTENT_URI);
			if (emailAdderss != null) {
				intent.putExtra(Intents.Insert.EMAIL, emailAdderss);
				intent.putExtra(Intents.Insert.NAME, name);
			}
			activity.startActivity(intent);
		} catch (Exception e) {
			// TODO: handle exception
			Debug.e("failfast", "failfast_AA", e);
		}
	}

}
