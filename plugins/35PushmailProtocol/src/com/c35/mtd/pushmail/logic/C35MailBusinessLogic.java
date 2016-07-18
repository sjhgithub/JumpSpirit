package com.c35.mtd.pushmail.logic;

import java.util.List;

import android.database.Cursor;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.beans.C35Attachment;
import com.c35.mtd.pushmail.beans.C35CompressItem;
import com.c35.mtd.pushmail.beans.C35Message;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.store.C35Store;
import com.c35.mtd.pushmail.store.LocalStore;
import com.c35.mtd.pushmail.store.Store;
import com.c35.mtd.pushmail.util.StoreDirectory;

/**
 * 35mail业务逻辑部分
 * 
 * @Description:
 * @author: cuiwei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-7-26
 */
public class C35MailBusinessLogic {

	private static final C35MailBusinessLogic blInstance = new C35MailBusinessLogic();

	private C35MailBusinessLogic() {

	}

	public static C35MailBusinessLogic getBLInstance() {
		return blInstance;
	}

	/**
	 * 
	 * @Description:保存到发件箱
	 * @param account
	 * @param message
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2013-7-25
	 */
	public void save2OutBox(final Account account, final C35Message message)  throws MessagingException{

		try {
			LocalStore mLocalStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			List<C35Attachment> atts = message.getAttachs();
			int count = 0;
			for (C35Attachment att : atts) {
				if(att.getCid() != null && !"".equals(att.getCid()) && att.getCid().length() > 0){
					count ++;
				}
			}
			message.setAttachSize(atts.size() - count);
			message.setFolderId(EmailApplication.MAILBOX_OUTBOX);
			String mailId = mLocalStore.saveMessages(message, EmailApplication.MAILBOX_OUTBOX, account);
			// 保存一下地址便于模糊查询
			// mLocalStore.saveMailAddress(message);
			// 存发件箱前先把邮件的原附件信息，防止重存
			mLocalStore.deleteC35AttachmentsByUid(mailId);
			for (int i = 0; i < atts.size(); i++) {
				C35Attachment att = atts.get(i);
				if (att.getId() != null) {
					List<C35CompressItem> compressItems = mLocalStore.getCompressItemsByAttachmentid(att.getId());
					for (C35CompressItem item : compressItems) {
						item.setAttachId(mailId + "_" + i);// 给附件增加id
					}
					mLocalStore.storeCompressItemsByC35CompressItems(compressItems);
					att.setMailId(mailId);
					att.setId(mailId + "_" + i);
					mLocalStore.storeAttachment(account, att);
				} else {
					att.setMailId(mailId);
					att.setId(mailId + "_" + i);
					mLocalStore.storeAttachment(account, att);
				}
			}
		} catch (Exception e) {
			throw new MessagingException("save2OutBox error",e);
		}
	}
	/**
	 * 显示账户列表
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-31
	 */
	public Cursor getAccountslistCur() {
		Cursor cursor = null;
		LocalStore localStore;
		try {
			localStore = (LocalStore) Store.getInstance(StoreDirectory.getStoreageUri());
			cursor = localStore.getAccountsListCur();
		} catch (Exception e) {//android.database.sqlite.SQLiteException: no such table: account
			Debug.e("failfast", "failfast_AA", e);
		}
		return cursor;
	}


}
