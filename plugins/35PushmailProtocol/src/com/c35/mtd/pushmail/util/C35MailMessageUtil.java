package com.c35.mtd.pushmail.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.R;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.exception.AuthenticationFailedException;
import com.c35.mtd.pushmail.exception.CertificateValidationException;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.logic.C35AccountManager;
import com.c35.mtd.pushmail.store.LocalStore;

/**
 * 
 * @ClassName: C35MailMessageUtil mail的广播工具类
 * @Description: 描述
 * @author:liujie
 * @date 2011-9-22 下午02:59:38
 * 
 */
public class C35MailMessageUtil {

	public static final Uri MAILMESSAGE = Uri.parse("content://com.c35.mtd.pushmail.mailmessageprovider");
	private static Intent sendIntent;
	private static Bundle bundle;


	static int messages = 0;
	static int unreads = 0;
	private static final String TAG = "C35MailMessageUtil";

	public static void sendMailMessageBroadcast(Context context, LocalStore localStore, boolean delete) {
		if (context != null) {

			try {
				if (!delete) {

					// int[] arr = localStore.getMailMessage(Email.MAILBOX_INBOX);
					Account account = EmailApplication.getCurrentAccount();
					if (account != null) {
						int unread = localStore.getInboxUnreadCount(account);
						int inboxcount = localStore.getFolderMailsCountLocal(account, EmailApplication.MAILBOX_INBOX);
						messages = inboxcount;
						unreads = unread;
					} else {
						messages = 0;
						unreads = 0;
					}
				}
				if (sendIntent == null) {
					sendIntent = new Intent("com.c35.mtd.pushmail.mailmessage");
				}
				if (bundle == null) {
					bundle = new Bundle();
				}
				bundle.clear();
				bundle.putInt("ALL_MESSAGE_COUNT", messages);
				bundle.putInt("ALL_UNREAD_MESSAGE_COUNT", unreads);
				sendIntent.putExtras(bundle);
				localStore = null;
				context.sendBroadcast(sendIntent, null);
				Debug.d(TAG, "send broadcast and AllMessages = " + messages + "Unreads = " + unreads);
				// 制空一下fuck
				messages = 0;
				unreads = 0;
			} catch (Exception e) {
				e.printStackTrace();
				Debug.e("failfast", "failfast_AA", e);
			}
		}
	}


	/**
	 *  发送邮件失败时，弹出提示框（activity搞得）
	 * @author liujie
	 * @date 2011-9-22
	 * @return void
	 * @throws
	 */
	public static void sendMailFailedBroadcast(Context context, String messageSubject, String errorCode, String messageUid, Account account) {
	    // Modified by LL
	    // BEGIN
	    /*
		Intent it1 = new Intent(context, C35AlertActivity.class);
		it1.putExtra("Subject", messageSubject);
		it1.putExtra("messageUid", messageUid);
		it1.putExtra("errorCode", errorCode);
		Bundle bundle = new Bundle();
		bundle.putSerializable("account", account);
		it1.putExtras(bundle);
		it1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(it1);
		*/
	    // END
	}

	/**
	 * 
	 * @Description:邮件发送成功信息提示
	 * @param:
	 * @return:
	 * @throws:
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2012-7-6
	 */
	public static void sendMailSuccessBroadcast(Context context, String str) {
//		Intent intent = new Intent("com.c35.mtd.pushmail.sendsuccess");
//		intent.putExtra("sendsuccess", str);
//		context.sendBroadcast(intent);
		if (EmailApplication.mHandler != null) {
			EmailApplication.mHandler.sendEmptyMessage(EmailApplication.HAN_MSG_SEND_SUCCESS);
			
		}
		Debug.i(TAG, "c35MailMessageUtil___________sendmaiSuccessBroadcast");
	}

	/**
	 * 所有的Exception处理成id
	 * @Description:
	 * @param e
	 * @return
	 * @see: 
	 * @since: 
	 * @author: cuiwei
	 * @date:2013-10-31
	 */
	public static int getIdForMessageException(Exception e) {
		Context gContext = EmailApplication.getInstance();
		int id = R.string.status_network_error;
		if (e instanceof AuthenticationFailedException) {
			id = R.string.account_setup_failed_dlg_auth_message;
			Debug.i(TAG, gContext.getString(id) + "1" + e);
		} else if (e instanceof CertificateValidationException) {
			id = R.string.account_setup_failed_dlg_certificate_message;
			Debug.i(TAG, gContext.getString(id) + "2");
		} else if (e instanceof MessagingException) {
			MessagingException me = (MessagingException) e;
			Debug.d(TAG, "synchronizeMailboxFailed()->me.getExceptionType() = " + me.getExceptionType());
			switch (me.getExceptionType()) {
			case MessagingException.CONNECT_ERROR:
				id = R.string.error_connect_error;
				Debug.e(TAG, gContext.getString(id));
				break;
			case MessagingException.OUT_OF_MEMORY:
				id = R.string.error_out_of_memory;
				Debug.e(TAG, gContext.getString(id));
				break;
			case MessagingException.CODE_CONNECT_ERROR:
				id = R.string.error_code_connect_error;
				Debug.e(TAG, gContext.getString(id));
				break;
			case MessagingException.CODE_LOGIN_NO:
				id = R.string.account_setup_failed_dlg_auth_message;
				Debug.e(TAG, gContext.getString(id));
				break;
			case MessagingException.DISK_IO_ERROR:
				Debug.e(TAG, gContext.getString(id));
				id = R.string.error_disk_io_error;
				break;
			case MessagingException.CODE_AUTH_NO:
				id = R.string.login_35Account_freeze;
				Debug.e(TAG, gContext.getString(id));
				break;
			case MessagingException.SERVER_IO_ERROR:
				Debug.e(TAG, gContext.getString(id));
				id = R.string.error_server_io_error;
				break;
			case MessagingException.REQUEST_DATA_ERROE:
				Debug.e(TAG, gContext.getString(id));
				id = R.string.error_request_parmars;
				break;
			case MessagingException.RESPONSE_ERROR_FORMAT:
				Debug.e(TAG, gContext.getString(id));
				id = R.string.command_response_error;
				break;
			case MessagingException.RETURN_COMMAND_ERROR_LINK_TIMEOUT:
				Debug.e(TAG, gContext.getString(id));
				id = R.string.experience_connectserver_timeout;
				break;
			case MessagingException.CODE_PROGRAM_ERROR:// 909;// 程序错误
				Debug.e(TAG, gContext.getString(id));
				id = R.string.command_response_error;
				break;
			case MessagingException.REQUEST_ERROR_FORMAT:
				Debug.e(TAG, gContext.getString(id));
				id = R.string.error_request_error_format;
				break;
			case MessagingException.CODE_VERSION_NO:
				Debug.e(TAG, gContext.getString(id));
				id = R.string.error_code_version_no;
				break;
			case MessagingException.PROGRAM_RUNING_ERROR:
				id = R.string.dowload_email_error;
				break;
			case MessagingException.FOLDER_ID_ERROR:
				id = R.string.folderId_error;
				break;
			case MessagingException.COMMAND_ERROR_COMMIT_MAILSTATUS:
				id = R.string.commanderror_commit_mailstatus;
				break;
			case MessagingException.COMMAND_ERROR_GET_MAILSTATUS:
				id = R.string.commanderror_get_mailstatus;
				break;
			case MessagingException.COMMAND_ERROR_SEARCH_ATTACH_LIST:
				id = R.string.commanderror_search_attach_list;
				break;
			case MessagingException.RETURN_COMMAND_ERROR_ADVANCESEARCHMAILS:
				id = R.string.command_response_error;
				break;
			case MessagingException.RETURN_COMMAND_ERROR_COMMITMAILSTATUS:
				id = R.string.command_response_commit_mailstatus;
				break;
			case MessagingException.RETURN_COMMAND_ERROR_FILEVIEWBYHTML:
				id = R.string.command_response_error;
				break;
			case MessagingException.RETURN_COMMAND_ERROR_GETATTACHMENTLIST:
				id = R.string.command_response_error;
				break;
			case MessagingException.RETURN_COMMAND_ERROR_GETATTACHMENTLISTIDS:
				id = R.string.command_response_error;
				break;
			case MessagingException.RETURN_COMMAND_ERROR_GETFOLDERLIST:
				id = R.string.command_response_error;
				break;
			case MessagingException.RETURN_COMMAND_ERROR_GETIDSBYID:
				id = R.string.command_response_error;
				break;
			case MessagingException.RETURN_COMMAND_ERROR_GETMAILBYID:
				id = R.string.command_response_error;
				break;
			case MessagingException.RETURN_COMMAND_ERROR_GETMAILIDSBYFOLDER:
				id = R.string.command_response_error;
				break;
			case MessagingException.RETURN_COMMAND_ERROR_GETMAILLISTBYMAILIDS:
				id = R.string.command_response_error;
				break;
			case MessagingException.RETURN_COMMAND_ERROR_GETMAILSSTATUS:
				id = R.string.command_response_get_mailstatus;
				break;
			case MessagingException.RETURN_COMMAND_ERROR_LOGIN:
				id = R.string.command_response_error;
				break;
			case MessagingException.RETURN_COMMAND_ERROR_SEARCHIDSBYTYPE:
				id = R.string.command_response_error;
				break;
			case MessagingException.RETURN_COMMAND_ERROR_VERSION:
				id = R.string.command_response_error;
				break;
			case MessagingException.DOWNLOAD_PHOTO_ERROR_MESSAGE:
				id = R.string.download_photo_error;
				break;
			case MessagingException.DOWNLOAD_ATTACHMENT_ERROR_MESSAGE:
				id = R.string.download_attachment_error;
				break;
			}

		}
		return id;
	}
}
