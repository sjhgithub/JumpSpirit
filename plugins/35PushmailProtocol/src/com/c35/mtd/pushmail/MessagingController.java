package com.c35.mtd.pushmail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.c35.mtd.pushmail.C35MailThreadPool.ENUM_Thread_Level;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.beans.C35Attachment;
import com.c35.mtd.pushmail.beans.C35CompressItem;
import com.c35.mtd.pushmail.beans.C35Folder;
import com.c35.mtd.pushmail.beans.C35Message;
import com.c35.mtd.pushmail.beans.Contact;
import com.c35.mtd.pushmail.beans.ErrorObj;
import com.c35.mtd.pushmail.beans.FolderObj;
import com.c35.mtd.pushmail.beans.MailObj;
import com.c35.mtd.pushmail.beans.MailStatusObj;
import com.c35.mtd.pushmail.beans.MessageReadInfo;
import com.c35.mtd.pushmail.beans.OperationHistoryInfo;
import com.c35.mtd.pushmail.beans.StatusObj;
import com.c35.mtd.pushmail.command.request.GetIdsByIdRequest.GetIdsType;
import com.c35.mtd.pushmail.command.response.CommitMailsStatusResponse;
import com.c35.mtd.pushmail.command.response.GetAttachmentListResponse;
import com.c35.mtd.pushmail.command.response.GetMailsStatusResponse;
import com.c35.mtd.pushmail.command.response.UpdateCalendarStateResponse;
import com.c35.mtd.pushmail.ent.bean.ContactAttribute;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.interfaces.AttDownLoadCallback;
import com.c35.mtd.pushmail.interfaces.MessageCallback;
import com.c35.mtd.pushmail.interfaces.MessagingListener;
import com.c35.mtd.pushmail.logic.AccountUtil;
import com.c35.mtd.pushmail.logic.ReceiveMessageModeUtil;
import com.c35.mtd.pushmail.provider.AttachmentProvider;
import com.c35.mtd.pushmail.store.C35Store;
import com.c35.mtd.pushmail.store.Folder.OpenMode;
import com.c35.mtd.pushmail.store.LocalStore;
import com.c35.mtd.pushmail.store.LocalStore.LocalFolder;
import com.c35.mtd.pushmail.store.Store;
import com.c35.mtd.pushmail.util.Address;
import com.c35.mtd.pushmail.util.C35AppServiceUtil;
import com.c35.mtd.pushmail.util.C35MailMessageUtil;
import com.c35.mtd.pushmail.util.MailUtil;
import com.c35.mtd.pushmail.util.NetworkUtil;
import com.c35.mtd.pushmail.util.StoreDirectory;
import com.c35.mtd.pushmail.util.StringUtil;

/**
 * 充当核心控制器，业务逻辑层。主要负责任务的调度及分发。
 * 
 * @author:
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class MessagingController implements Runnable {

	private static final String TAG = "MessagingController";
	private static final String RUN_TAG = "MCRun";
	// private static final String SEND_MSG_DEC = "sendingMessages";
	// private static final String CMD_CHECK_MAIL_DEC = "checkMail";
	private static final String CMD_SYNC_MAILBOX = "syncMailBox";
	private static final String CMD_SYNC_MESSAGE = "syncMessage";

	ArrayList<String> leftRightToDownList = new ArrayList<String>();
	private BlockingDeque<String> mMessagesQueue = new LinkedBlockingDeque<String>(300);
	private Map<String, MessageCallback> priorityListeners = new Hashtable<String, MessageCallback>();
	private static final MessagingController inst = new MessagingController();
	// 定义由数组支持的Command有界双端阻塞队列
	private BlockingDeque<Command> mCommands = new LinkedBlockingDeque<Command>();
	private HashSet<MessagingListener> mListeners = new HashSet<MessagingListener>();
	private HashSet<String> mSendingMessages = new HashSet<String>();
	// private HashSet<String> mSyncMailBox = new HashSet<String>();
	// private boolean mBusy;
	// private boolean mSync = false;
	private static Application mApplication;
	private Thread mThread = null;
	// protected double megCount = 0;
	// private int totolTask = -1;
	private Command currentCommand;// 当前正在运行中的线程
	// int setRead = 0;
	// private boolean contentZipFlag = true;// 邮件正文压缩

	// 缓存操作履历，用于批量提交
	private HashMap<String, OperationHistoryInfo> operationHistoryInfoCache = new HashMap<String, OperationHistoryInfo>();

	public HashMap<String, OperationHistoryInfo> getOperationHistoryInfoCache() {
		return operationHistoryInfoCache;
	}

	//
	// public void setOperationHistoryInfoCache(HashMap<String, OperationHistoryInfo>
	// operationHistoryInfoCache) {
	// this.operationHistoryInfoCache = operationHistoryInfoCache;
	// }

	private final Object mSaveC35DraftLocker = new Object();
	private final Object mGetMailFromServerPriorityLocker = new Object();
	private final Object mCommitMailsStatusLocker = new Object();
	private final Object mGetMailsStatusLocker = new Object();
	private final Object mPutCmdLocker = new Object();// mPutCmd时所需的锁
	private final Object mSendMessageLocker = new Object();
	// 0=标题 1=正文2=附件名 3=全部(0,1,2) 5=往来邮件
	private final int mSearchSubject = 0;
	private final int mSearchContext = 1;
	// private final int mSearchAttachment = 2;
	private final int mSearchAll = 3;
	private final int mSearchBetweenUser = 5;

	private boolean downLoading = false;// 正在下载邮件体

	private boolean synchronizingMailbox = false;// 是否正在同步,同步过程中不添加操作履历

	// private Command tempCommand = null;
	// private boolean isLoadmore = false;
	// private boolean isSynchronizeMailbox = false;
	/**
	 * 存储操作履历到内存map中
	 * 
	 * @Description:
	 * @param account
	 * @param messageuid
	 * @param statusId
	 * @param statusValue
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-8
	 */
	private void putOperationHistoryInfoMap(final Account account, final String messageuid, final int statusId, final String statusValue) {
		synchronized (operationHistoryInfoCache) {
			if (account.isAutoSync() && !synchronizingMailbox) {
				String strFolderId = this.getStringFolderId(account, messageuid);
				if (strFolderId != null // .Draft
					&& !EmailApplication.MAILBOX_OUTBOX.equals(strFolderId) && !EmailApplication.MAILBOX_DRAFTSBOX.equals(strFolderId) && !EmailApplication.MAILBOX_TRASHBOX.equals(strFolderId)) {// ".outbox"等不应该记录
					String key = account.getUuid() + ":" + messageuid + ":" + statusId;
					operationHistoryInfoCache.put(key, this.createNewOperationHistoryInfo(account, messageuid, statusId, statusValue));
				}
			}

		}

	}

	/**
	 * 是否有发送中消息
	 * 
	 * @return
	 */
	public boolean isSendingMessage() {
		return mSendingMessages.size() > 0;
	}

	/**
	 * constructor
	 * 
	 * @param application
	 */
	private MessagingController() {
		// mApplication = application;
		mThread = new Thread(this);
		mThread.start();
	}

	/**
	 * Gets or creates the singleton instance of MessagingController. Application is used to provide a Context
	 * to classes that need it.
	 * 
	 * @param application
	 * @return
	 */
	public static MessagingController getInstance(Application application) {
		mApplication = application;
		// if (inst == null) {
		// inst = new MessagingController();
		// }
		return inst;
	}

	@Override
	public void run() {
		Debug.v(RUN_TAG, "run()");
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		while (true) {
			currentCommand = null;
			try {
				int iTemp;
				iTemp = mCommands.size();
				if (iTemp > 0)
					Debug.d("QUEUE", "run()->mCommands.size() = " + mCommands.size() + " mCommands:" + mCommands.peekFirst().description);
				if (iTemp > 1)
					Debug.d("QUEUE", "mCommands:" + mCommands.peekLast().description);
				// for(int j=0;j<=iTemp;j++){
				// // Debug.d("QUEUE", "mCommands:"+mCommands.`.description);
				// }
				iTemp = mMessagesQueue.size();
				if (iTemp > 0)
					Debug.d("QUEUE", "run()->messagesQueue.size() = " + mMessagesQueue.size() + " messagesQueue:" + mMessagesQueue.peekFirst().toString());
				// iTemp=C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_AccountSelf).getQueue().size();
				// if(iTemp>0)
				// Debug.d("QUEUE", "run()->C35MailThreadPool.ENUM_Thread_Level.TL_AccountSelf() = " +
				// C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_AccountSelf).getQueue().size());
				iTemp = C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_AtOnce).getQueue().size();
				if (iTemp > 0)
					Debug.d("QUEUE", "run()->C35MailThreadPool.ENUM_Thread_Level.TL_AtOnce() = " + C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_AtOnce).getQueue().size());
				iTemp = C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_common).getQueue().size();
				if (iTemp > 0)
					Debug.d("QUEUE", "run()->C35MailThreadPool.ENUM_Thread_Level.TL_common() = " + C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_common).getQueue().size());
				currentCommand = mCommands.take();
				iTemp = mCommands.size();
				if (iTemp > 0)
					Debug.d("QUEUE", "2run()->mCommands.size() = " + mCommands.size() + " mCommands:" + mCommands.peekFirst().description);
				if (iTemp > 1)
					Debug.d("QUEUE", "mCommands:" + mCommands.peekLast().description);
				iTemp = mMessagesQueue.size();
				if (iTemp > 0)
					Debug.d("QUEUE", "2run()->messagesQueue.size() = " + mMessagesQueue.size() + " messagesQueue:" + mMessagesQueue.peekFirst().toString());
				// iTemp=C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_AccountSelf).getQueue().size();
				// if(iTemp>0)
				// Debug.d("QUEUE", "2run()->C35MailThreadPool.ENUM_Thread_Level.TL_AccountSelf() = " +
				// C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_AccountSelf).getQueue().size());
				iTemp = C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_AtOnce).getQueue().size();
				if (iTemp > 0)
					Debug.d("QUEUE", "2run()->C35MailThreadPool.ENUM_Thread_Level.TL_AtOnce() = " + C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_AtOnce).getQueue().size());
				iTemp = C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_common).getQueue().size();
				if (iTemp > 0)
					Debug.d("QUEUE", "2run()->C35MailThreadPool.ENUM_Thread_Level.TL_common() = " + C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_common).getQueue().size());
			} catch (InterruptedException e) {
				continue; // re-test the condition on the eclosing while
			}
			if (currentCommand != null) {
				if (currentCommand.runnable != null) {

					currentCommand.runnable.run();
					Debug.d(RUN_TAG, "run()->command.runnable.run()->end");

				}
			}
		}
	}

	/**
	 * 向队列中put新cmd
	 * 
	 * @param description
	 * @param listener
	 * @param runnable
	 */
	private void putCmd(String description, MessagingListener listener, Runnable runnable) {
		synchronized (mPutCmdLocker) {
			try {
				for (Command cmd : mCommands) { // 这个循环避免加入重复命令
					if (description.equals(cmd.description)) {
						Debug.d("QUEUE", "putCmd rejected!:" + description);
						return;
					}
				}

				Debug.d("QUEUE", "putCmd:" + description);
				Command command = new Command();
				command.listener = listener;
				command.runnable = runnable;
				command.description = description;
				/*
				 * if(description.equals("loadMessageForViewRemote")){ Log.e(DBG_TAG,
				 * "loadMessageForViewRemote come on!"); this.tempCommand = command; }
				 */
				// Debug.i("no refresh", "mCommands——————————size：" + mCommands.size());
				// for (Command comm : mCommands) {
				// Debug.i("no refresh", "mCommands——————————name：" + comm.description);
				// }
				mCommands.put(command);
				// Debug.i("no refresh", "mCommands——————————size：" + mCommands.size());
			} catch (InterruptedException ie) {
				throw new Error(ie);
			}
		}
	}

	/**
	 * 向队列中put新cmd 带优先级
	 * 
	 * @Description:
	 * @param description
	 * @param thread_Level
	 * @param listener
	 * @param runnable
	 * @return 是否真正插入队列
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-18
	 */
	private boolean putCmd(String description, C35MailThreadPool.ENUM_Thread_Level thread_Level, MessagingListener listener, Runnable runnable) {
		synchronized (mPutCmdLocker) {

			try {
				for (Command cmd : mCommands) { // 这个循环避免加入重复命令
					if (description.equals(cmd.description)) {
						Debug.d("QUEUE", "putCmd rejected!tPool:" + description);
						return false;
					}
				}
				Debug.d("QUEUE", "putCmd thread_Level:" + description);
				Command command = new Command();
				command.listener = listener;
				command.runnable = runnable;
				command.description = description;
				if (thread_Level.equals(C35MailThreadPool.ENUM_Thread_Level.TL_AtOnce)) {
					mCommands.addFirst(command);// todo,判断是否有相同的cmd，相同的不用放入//mCommands.offerFirst()有问题
				} else {
					mCommands.put(command);
				}
				return true;
				// Debug.i("no refresh", "mCommands——————————size：" + mCommands.size());
			} catch (Exception e) {
				Debug.e("failfast", "mCommands", e);
				return false;
			}
		}
	}

	/**
	 * 获取指定账号的全部邮件夹（一层）
	 * 
	 * @param account
	 * @param listener
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-31
	 */
	public Cursor listFolders(Account account, MessagingListener listener) {
		Cursor cursor = null;
		try {
			LocalStore locaStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			cursor = locaStore.listFolder(account.getUuid());
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
			// MailToast.makeText(e.getMessage(), Toast.LENGTH_LONG).show();
		} catch (Exception e) {// 空指针
			Debug.e("failfast", "failfast_AA", e);
			// MailToast.makeText(e.getMessage(), Toast.LENGTH_LONG).show();
		}
		return cursor;
	}

	/**
	 * 获取指定账号的全部邮件夹（一级以外）
	 * 
	 * @param account
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-31
	 */
	public List<C35Folder> listAllFolders(Account account) {
		List<C35Folder> lst = new ArrayList<C35Folder>();
		try {
			LocalStore locaStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			lst = locaStore.listAllFolders(account.getUuid());
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		return lst;
	}

	/**
	 * 获取指定账号的全部邮件夹第一级
	 * 
	 * @param account
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-31
	 */
	public List<C35Folder> listAllFolders0(Account account) {
		List<C35Folder> lst = new ArrayList<C35Folder>();
		try {
			LocalStore locaStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			lst = locaStore.listAllFolders0(account.getUuid());
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		return lst;
	}

	/**
	 * 获取自定义文件夹子文件夹列表
	 * 
	 * @param account
	 * @return
	 * @see:
	 * @since:
	 * @author: gongfacun
	 * @date:2012-11-8
	 */
	public Cursor listSelfFolders(Account account, String parentId) {
		Cursor cursor = null;
		try {
			LocalStore locaStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			cursor = locaStore.listSelfFolderChild(account.getUuid(), parentId);
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		return cursor;
	}

	/**
	 * 加载更多
	 * 
	 * @param account
	 * @param folderName
	 * @param refUid
	 * @param listener
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-31
	 */
	public void loadMoreMessages(final Account account, final String folderId, final String refUid, MessagingListener listener, final List<String> visibleUids, final List<C35Message> visibleMessages) {

		putCmd(CMD_SYNC_MAILBOX + " " + account.getEmail() + " " + folderId, C35MailThreadPool.ENUM_Thread_Level.TL_AtOnce, listener, new Runnable() {

			@Override
			public void run() {// loadMoreMessages
				syncMailBox(account, folderId, refUid, false, visibleUids, visibleMessages, null, 0);// 传值为false，说明是加载更多触发的，便可以用来显示加载已完成
			}
		});
	}

	/**
	 * Start background synchronization of the specified folder.
	 * 
	 * @param account
	 * @param folderId
	 * @param numNewestMessagesToKeep
	 *            Specifies the number of messages that should be considered as part of the window of
	 *            available messages. This number effectively limits the user's view into the mailbox to the
	 *            newest (numNewestMessagesToKeep) messages.
	 * @param listener
	 */
	public boolean synchronizeMailbox(final Account account, final String folderId, MessagingListener listener, final List<String> visibleUids, final List<C35Message> visibleMessages) {// TODO:
		boolean bReturn;
		bReturn = putCmd(CMD_SYNC_MAILBOX + " " + account.getEmail() + " " + folderId, C35MailThreadPool.ENUM_Thread_Level.TL_AtOnce, listener, new Runnable() {

			@Override
			public void run() {
			    // Modified by LL
				//MessageList.mHandler.sendEmptyMessage(MessageList.ListHandler.SYNC_START_REFRESH);

				syncMailBox(account, folderId, null, true, visibleUids, visibleMessages, null, 0);
			}
		});
		return bReturn;
	}

	/**
	 * 获取消息盒子数据
	 * 
	 * @param folderId
	 * @param account
	 * @param accountMsgMap
	 * @param listener
	 * @see:
	 * @since:
	 * @author: wennan
	 * @date:2013-6-1
	 */
	public void loadHeaderDataForInfoBox(final String folderId, final Account account, Hashtable<String, ArrayList<String>> accountMsgMap, MessagingListener listener, boolean ifSync) {
		Log.d(TAG, "loadHeaderDataForInfoBox  begin");
		// TODO:sofia2.0环境不支持此接口。
		Boolean result = AccountUtil.isSupportRequest("ReadPush", account);
		if (result == null || result == false) {
			AccountUtil.nosupportRequestToast();
			return;
		}
		String currentMail = account.getEmail();
		// ArrayList<String> aa = new ArrayList<String>();
		// aa.add("51aaa74c0cf29f9d7cd5b29c");
		// accountMsgMap.put("test2@magic.35.com", aa);
		// aa.add("51aaa33b0cf29f9d7cd5b28c");
		// aa.add("51a956000cf29f9d7cd5b206");

		Log.d(TAG, "loadHeaderDataForInfoBox  currentMail ==" + currentMail);
		if (accountMsgMap.containsKey(currentMail)) {
			List<String> uids = accountMsgMap.get(currentMail);
			if (uids != null && uids.size() > EmailApplication.READ_MAIL_UID_SHOW_SIZE) {
				uids = uids.subList(0, EmailApplication.READ_MAIL_UID_SHOW_SIZE);
			}

			// List<String> messages2SyncUids = new ArrayList<String>();
			// List<String> hasSynUids = new ArrayList<String>();

			List<C35Message> messages = new ArrayList<C35Message>();

			try {
				if (uids != null && !uids.isEmpty()) {
					Log.d(TAG, "loadHeaderDataForInfoBox  uids ==" + uids.toString());
					LocalStore localStore = (LocalStore) (Store.getInstance(account.getLocalStoreUri()));
					LocalFolder localFolder = localStore.getFolder(account, folderId);
					localFolder.open(OpenMode.READ_WRITE);
					if (ifSync) {
						this.commitMailsStatus(account);// 先提交状态
						C35Store remoteStore = (C35Store) Store.getInstance(account.getStoreUri());
						remoteStore.openAndGetTicket();
						List<C35Message> messages2Sync = remoteStore.getMailListByMailIds(uids, 150);
						localFolder.setLastUpdate(System.currentTimeMillis());
						localFolder.saveMessagesHeader(account, messages2Sync);
					}
					// for (String uid : uids) {
					// C35Message message = localStore.getC35MessageOther(uid, account);
					// if (message == null) {
					// messages2SyncUids.add(uid);
					// } else {
					// hasSynUids.add(uid);
					// }
					// }
					// if (messages2SyncUids != null && !messages2SyncUids.isEmpty()) {
					// C35Store remoteStore = (C35Store) Store.getInstance(account.getStoreUri(),
					// mApplication);
					// remoteStore.open();
					// List<C35Message> messages2Sync = remoteStore.getMailListByMailIds(messages2SyncUids,
					// 150);
					//
					// LocalFolder localFolder = localStore.getFolder(account, folderId);
					// localFolder.open(OpenMode.READ_WRITE);
					// localFolder.setLastUpdate(System.currentTimeMillis());
					// localFolder.saveMessagesHeader(account, messages2Sync);
					//
					// }
					// if (hasSynUids != null && !hasSynUids.isEmpty()) {
					// this.getMailsStatus(account, folderId, hasSynUids);
					// }

					List<String> delList = new ArrayList<String>();// 这些是本地已经不存在的
					for (String uid : uids) {
						C35Message message = localStore.getC35MessageOther(uid, account);
						if (message != null) {
							this.getReaderNameListFromMessage(message);
							messages.add(message);
						} else {
							delList.add(uid);
						}

					}

					if (messages.isEmpty()) {
						listener.loadHeaderForInfoBoxDataError();
						Log.d(TAG, "loadHeaderForInfoBoxDataError");
						// 删除掉map和配置文件里的数据
						List<String> list = EmailApplication.pushBoxMap.get(account.getEmail());
						if (list != null) {
							list.remove(delList);
							uids.removeAll(delList);
							EmailApplication.update_push_uids(account, uids);

						}
						return;

					}
				}

				listener.loadHeaderForInfoBoxFinish(messages);
				Log.d(TAG, "loadHeaderForInfoBoxFinish");

			} catch (Exception e) {
				Debug.e("failfast", "failfast_AA", e);
				listener.loadHeaderForInfoBoxOver();
			}

		} else {
			listener.loadHeaderForInfoBoxOver();

		}

	}

	/**
	 * 是否存在这封邮件
	 * 
	 * @param account
	 * @param messageUid
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @throws MessagingException
	 * @date:2013-6-28
	 */
	public boolean hasThisMessageMC(Account account, String messageUid) throws MessagingException {
		LocalStore localStore;
		boolean have = false;
		localStore = (LocalStore) (Store.getInstance(account.getLocalStoreUri()));
		have = localStore.haveThisMsg(account.getUuid(), messageUid);
		return have;

	}

	private void getReaderNameListFromMessage(C35Message cms) {
		List<String> toname_address_List = cms.getTo();
		Log.d(TAG, "loadHeaderDataForInfoBox  getTo ==" + toname_address_List);
		List<String> ccname_address_List = cms.getCc();
		List<String> bccname_address_List = cms.getBcc();
		List<String> addressList = cms.getDeliveredReadUsers();
		Log.d(TAG, "loadHeaderDataForInfoBox  cms.getDeliveredReadUsers() ==" + cms.getDeliveredReadUsers());
		HashMap<String, String> address_name_map = new HashMap<String, String>();
		List<String> nameList = new ArrayList<String>();
		this.nameAddressList2Map(toname_address_List, address_name_map);
		this.nameAddressList2Map(ccname_address_List, address_name_map);
		this.nameAddressList2Map(bccname_address_List, address_name_map);
		if (addressList != null && !addressList.isEmpty()) {
			for (String address : addressList) {
				address = address.trim();
				if (address_name_map.containsKey(address)) {
					String nickName = address_name_map.get(address);
					// 有可能昵称为空的情况，则取前缀
					if (!StringUtil.isNotEmpty(nickName)) {
						nickName = address.contains("@") ? address.substring(0, address.indexOf("@")) : address;
					}
					nameList.add(nickName);
					Log.d(TAG, "loadHeaderDataForInfoBox nickName ==" + nickName);
				} else {// 有可能是群组
					nameList.add(address.contains("@") ? address.substring(0, address.indexOf("@")) : address);
					cms.setReseiversHasGroup(true);
				}
			}
		}
		cms.setNameOfdeliveredReadUsers(nameList);
		Log.d(TAG, "loadHeaderDataForInfoBox setNameOfdeliveredReadUsers ==" + cms.getNameOfdeliveredReadUsers().toString());

	}

	private void nameAddressList2Map(List<String> nameAddressList, HashMap<String, String> addressLNameMap) {
		if (nameAddressList != null && !nameAddressList.isEmpty()) {
			for (String name_address : nameAddressList) {
				if (!TextUtils.isEmpty(name_address) && name_address.contains("<")) {
					name_address = name_address.trim();
					String[] name_addresses = name_address.split("<");
					String name = name_addresses[0];
					String address = name_addresses[1].substring(0, name_addresses[1].length() - 1);
					addressLNameMap.put(address.trim(), name.trim());
				}
			}
		}

	}

	private List<String> getToCcBccList(String emails) {
		List<String> address_List = new ArrayList<String>();
		String[] stringAddress = emails.split("");
		for (String string : stringAddress) {
			if (StringUtil.isNotEmpty(string.trim())) {
				address_List.add(string.trim());
				// Debug.e(TAG, "addToCcBccList =" + string.trim());
			}
		}
		return address_List;
	}

	/**
	 * 获得已发送邮件已读未读情况
	 * 
	 * @Description:
	 * @param to
	 * @param cc
	 * @param bcc
	 * @param readers
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-6-18
	 */
	public MessageReadInfo getSendMessageReaderInfo(String to, String cc, String bcc, String readers, int readersCountFromServer) {
		MessageReadInfo messageInfo = new MessageReadInfo();

		List<String> toname_address_List = getToCcBccList(to);
		List<String> ccname_address_List = getToCcBccList(cc);
		List<String> bccname_address_List = getToCcBccList(bcc);

		List<String> readerAddressList = new ArrayList<String>();
		String[] stringAddress = readers.split(";");
		for (String string : stringAddress) {
			readerAddressList.add(string.trim());
		}
		ArrayList<String> nameList = new ArrayList<String>();
		HashMap<String, String> address_name_map = new HashMap<String, String>();
		this.nameAddressList2Map(toname_address_List, address_name_map);
		this.nameAddressList2Map(ccname_address_List, address_name_map);
		this.nameAddressList2Map(bccname_address_List, address_name_map);
		// 跟据已读账号，比对收件人账号，获得已读账号的昵称
		if (readerAddressList != null && !readerAddressList.isEmpty()) {
			for (String address : readerAddressList) {
				if (address_name_map.containsKey(address)) {
					String nickName = address_name_map.get(address);
					// 有可能昵称为空的情况，则取前缀
					if (!StringUtil.isNotEmpty(nickName)) {
						nickName = address.contains("@") ? address.substring(0, address.indexOf("@")) : address;
					}
					nameList.add(nickName);
					Debug.d(TAG, "loadHeaderDataForList nickName ==" + nickName);
				} else {
					nameList.add(address.contains("@") ? address.substring(0, address.indexOf("@")) : address);
					// 只要已读账号列表中存‘’‘’【【在不包含在收件人账号中的项，则视为收件人中包含群组账号
					messageInfo.setReseiversHasGroup(true);
				}
			}
		}
		messageInfo.setReadersNickName(nameList);
		// 获得所有收件人的数目
		ArrayList<String> allReceivers = new ArrayList<String>();
		for (String a : toname_address_List) {
			// Debug.v(TAG, "toname_address_List =" + a);
			if (!allReceivers.contains(a)) {
				allReceivers.add(a);
				// Debug.v(TAG, "add =" + a);
			}
		}
		for (String a : ccname_address_List) {
			// Debug.v(TAG, "ccname_address_List =" + a);
			if (!allReceivers.contains(a)) {
				allReceivers.add(a);
				// Debug.v(TAG, "add =" + a);
			}
		}
		for (String a : bccname_address_List) {
			if (!allReceivers.contains(a)) {
				allReceivers.add(a);
				// Debug.v(TAG, "add =" + a);
			}
		}

		messageInfo.setAllReceiversCount(allReceivers.size());

		// 若返回count大于已读账户数目，则包含群组
		if (nameList.size() < readersCountFromServer) {
			messageInfo.setReseiversHasGroup(true);
		}

		return messageInfo;
	}

	/**
	 * 从本地或服务器下载邮件体（todo比较耗时需优化）UI
	 * 
	 * @Description:
	 * @param account
	 * @param uid
	 * @param context
	 * @param listener
	 * @param loadFromServer
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-10-14
	 */
	public C35Message loadMessageForView(final Account account, String uid, Context context, final MessagingListener listener, boolean loadFromServer, final boolean isC35, final boolean isUI) {
		LocalStore localStore = null;
		C35Message retMessage = null;
		try {
			localStore = (LocalStore) (Store.getInstance(account.getLocalStoreUri()));
		} catch (MessagingException e1) {
			Debug.e("failfast", "failfast_AA", e1);
		}
		try {
			final C35Message tempmessage = localStore.getC35MessageOther(uid, account); // 根据Uid，Account读取数据库中Messages表，封装邮件头信息
			// localStore.getC35Message(uid); 读取邮件(头，体)信息，
			if (tempmessage == null)
				return null;
			if (isC35) {
				if (tempmessage.getRead() == 0) {
					setReadFlag(account, tempmessage.getMailId(), true);
				}
				// ArrayList<C35Message> msgs= new ArrayList<C35Message>();
				// msgs.add(tempmessage);
				showMessageHeadersView(account, uid, tempmessage);

			} else {
				makeHeaderData(tempmessage);
			}
			// if (tempmessage.getRead() == 0) {
			// setReadFlag(account, tempmessage.getMailId(), true);
			// }
			//
			// 未下载的话去下载正文数据 非显示邮件可以延后处理 todo
			boolean bTemp;
			if (isC35) {
				bTemp = tempmessage.getDownFalg() == C35Message.DOWNLOAD_FLAG_ENVELOPE;
			} else {
				bTemp = loadFromServer && tempmessage.getDownFalg() == C35Message.DOWNLOAD_FLAG_ENVELOPE;
			}
			if (bTemp) {//
				if (StoreDirectory.isNoAvailableStore()) {
				    // Modified by LL
			        // BEGIN
				    /*
					Intent inte = new Intent(context, C35NoSpaceAlertActivity.class);
					inte.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(inte);
					*/
					// END

					if (listener != null)
						listener.loadRemoteMessageForViewFailed(account, uid, new MessagingException(MessagingException.DISK_IO_ERROR, "磁盘空间不足"), tempmessage);
					return tempmessage;
				}

				if (!NetworkUtil.isNetworkAvailable()) {
					if (listener != null) {
						if (isUI) {
							listener.loadRemoteMessageForViewFailed(account, uid, new MessagingException(MessagingException.SERVER_IO_ERROR, "网络错误"), tempmessage);
						} else {
							listener.loadRemoteMessageForViewFailed(account, uid, new MessagingException(MessagingException.SERVER_IO_ERROR_NO_TOAST, "网络错误"), tempmessage);
						}
					}
					return tempmessage;
				}
				// 开始下载 todo 暂时屏蔽以便提速
				getMailFromServerPriority(account, EmailApplication.MAILBOX_INBOX, uid, tempmessage, isC35, listener);
			} else {// 正文已下载过/
				C35Message message = null;
				message = localStore.getC35MessageHtmlContent(account, uid);
				if (message == null) {
					LoadMessageForViewBody(account, uid, tempmessage);
					return tempmessage;
				}
				tempmessage.setCompressItems(message.getCompressItems());
				if (isC35) {
					tempmessage.setHyperText(message.getHyperText());
				} else {
					String text = message.getHyperText();
					if (StringUtil.isNotEmpty(text)) {
						text = processHyperText(text, message);
						tempmessage.setHyperText(text);
					} else {
						tempmessage.setHyperText(null);
					}
					tempmessage.setHasBody(true);
				}
				tempmessage.setPlainText(message.getPlainText());
				tempmessage.setAttachs(message.getAttachs());
				tempmessage.setRead(C35Message.READ_FLAG_SEEN);
				tempmessage.setDownFalg(message.getDownFalg());
				tempmessage.setMailType(message.getMailType());
				tempmessage.setCalendarStartTime(message.getCalendarStartTime());
				tempmessage.setCalendarEndTime(message.getCalendarEndTime());
				tempmessage.setCalendarLocation(message.getCalendarLocation());
				tempmessage.setCalendarState(message.getCalendarState());
				tempmessage.setCompressedToSize(message.getCompressedToSize());
				if (isC35) {
					LoadMessageForViewBody(account, uid, tempmessage);
					// synchronizedLoadMessageForViewFinished(account, uid, tempmessage);
				}
			}
			retMessage = tempmessage;
		} catch (Exception ne) {

			Debug.e("failfast", "failfast_AA", ne);
			listener.loadRemoteMessageForViewFailed(account, uid, ne, retMessage);

			return retMessage;
		}
		return retMessage;
	}

	/**
	 * 正文特殊处理
	 * 
	 * @Description:
	 * @param text
	 * @param message
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-10-17
	 */
	private String processHyperText(String text, C35Message message) {
		text = message.getHyperText();
		List<C35Attachment> atts = message.getAttachs();
		List<C35Attachment> cidAtts = new ArrayList<C35Attachment>();
		for (C35Attachment c35Att : atts) {
			if (StringUtil.isNotEmpty(c35Att.getCid())) {
				cidAtts.add(c35Att);
			}
		}
		for (C35Attachment c35Att : cidAtts) {
			String contentId = c35Att.getCid();
			Uri contentUri = AttachmentProvider.getAttachmentUri(c35Att.getSourceAttachmentId());
			if (contentUri != null) {
				// Regexp which matches ' src="cid:contentId"'.
				String contentIdRe = "\\s+(?i)src=\"cid(?-i):\\Q" + contentId + "\\E\"";
				// Replace all occurrences of src attribute with '
				// src="content://contentUri"'.
				text = text.replaceAll(contentIdRe, " src=\"" + contentUri + "\"");
			}
		}
		// 对来自market的连接进行处理，处理需求如下：1增加一个skey和sn参数,2对skey进行base64编码Utility.base64Encode()
		// if (text.contains("http://ota.35.com:8080/35OTA/") && text.contains("?fn=")) {
		// text = text.replace("?fn=", "?skey=" + Base64Utils.encode(getIMEI().getBytes()) + "&sn=" +
		// mAccount.getmEmailShow() + "&fn=");
		// }
		return text;
	}

	/**
	 * loadOneMessageOnlyHeader
	 * 
	 * @Description:
	 * @param account
	 * @param uid
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-10-17
	 */
	public C35Message loadOneMessageOnlyHeader(Account account, String uid) {
		C35Message tempmessage = null;
		try {
			LocalStore localStore = (LocalStore) (Store.getInstance(account.getLocalStoreUri()));
			tempmessage = localStore.getC35MessageOther(uid, account);
			if (tempmessage != null) {
				makeHeaderData(tempmessage);
				this.getReaderNameListFromMessage(tempmessage);
			}

		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
		}
		return tempmessage;
	}

	/**
	 * 把邮件头信息放入C35Message
	 * 
	 * @Description:
	 * @param cms
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-10-17
	 */
	private void makeHeaderData(C35Message cms) {
		try {
			String name_addersss = cms.getFrom();
			if (!TextUtils.isEmpty(name_addersss) && name_addersss.contains("<")) {
				String displayName = name_addersss.split("<")[0];
				if (displayName.contains("@")) {
					displayName = displayName.split("@")[0];
				}
				String mailAdderss = name_addersss.split("<")[1].substring(0, name_addersss.split("<")[1].length() - 1);
				cms.setFromDisplayName(displayName);
				cms.setFromMailAdderss(mailAdderss);
			}

			String sendTime = cms.getSendTime();
			if (sendTime != null) {
				String times[] = sendTime.split(" ");
				String timeText = times[0];
				String dateText = times[1];
				String ttime = timeText + " " + dateText;
				SimpleDateFormat olddateFormat = EmailApplication.DateFormatYMDHMS;
				olddateFormat.setLenient(false);
				Date date = null;
				try {
					date = olddateFormat.parse(ttime);
				} catch (ParseException e) {
					Debug.e(TAG, "failfast_AA", e);
				}
				SimpleDateFormat dateFormat = EmailApplication.DateFormatMDHM;
				ttime = dateFormat.format(date);
				if (!TextUtils.isEmpty(ttime)) {
					cms.setSendTime(ttime);
				}
			}

			String to = cms.getTo().toString();
			String cc = cms.getCc().toString();
			String bcc = cms.getBcc().toString();
			String toText = Address.toFriendly(Address.parse(to.substring(1, to.length() - 1)));
			String ccText = Address.toFriendly(Address.parse(cc.substring(1, cc.length() - 1)));
			String bccText = Address.toFriendly(Address.parse(bcc.substring(1, bcc.length() - 1)));
			List<String> name_address_List = cms.getTo();
			List<String> ccname_address_List = cms.getCc();
			List<String> bccname_address_List = cms.getBcc();
			ArrayList<String> addressList = new ArrayList<String>();
			ArrayList<String> nameList = new ArrayList<String>();
			for (String name_address : name_address_List) {
				if (!TextUtils.isEmpty(name_address) && name_address.contains("<")) {
					String[] name_addresses = name_address.split("<");
					String name = name_addresses[0];
					String address = name_addresses[1].substring(0, name_addresses[1].length() - 1);
					addressList.add(address);
					nameList.add(name);

				}

			}
			ArrayList<String> ccaddressList = new ArrayList<String>();
			ArrayList<String> ccnameList = new ArrayList<String>();
			for (String name_address : ccname_address_List) {
				if (!TextUtils.isEmpty(name_address) && name_address.contains("<")) {
					String[] name_addresses = name_address.split("<");
					String name = name_addresses[0];
					String address = name_addresses[1].substring(0, name_addresses[1].length() - 1);
					ccaddressList.add(address);
					ccnameList.add(name);
				}

			}

			ArrayList<String> bccaddressList = new ArrayList<String>();
			ArrayList<String> bccnameList = new ArrayList<String>();
			for (String name_address : bccname_address_List) {
				if (!TextUtils.isEmpty(name_address) && name_address.contains("<")) {
					String[] name_addresses = name_address.split("<");
					String name = name_addresses[0];
					String address = name_addresses[1].substring(0, name_addresses[1].length() - 1);
					bccaddressList.add(address);
					bccnameList.add(name);
				}

			}

			cms.setToText(toText);
			cms.setCcText(ccText);
			cms.setBccText(bccText);

			cms.setToNameList(nameList);
			cms.setCcNameList(ccnameList);
			cms.setBccNameList(bccnameList);

			cms.setToAddressList(addressList);
			cms.setCcAddressList(ccaddressList);
			cms.setBccAddressList(bccaddressList);

		} catch (Exception e) {
			Debug.e(TAG, "failfast_AA", e);
		}

	}

	/**
	 * 发送邮件
	 * 
	 * @param account
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-31
	 */
	public void sendPendingMessagesInMultiThreads(final Account account) throws MessagingException {
		synchronized (mSendMessageLocker) {
			LocalStore mLocalStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			List<C35Message> localMessages = mLocalStore.getMessagesByFolder(account, EmailApplication.MAILBOX_OUTBOX);
			//TODO imcore,先判断下对应账号的文件夹是否存在
			mLocalStore.getIdByFolderUuidAndAccountID(mLocalStore.getAccountIdByUuid(account.getUuid()),EmailApplication.MAILBOX_OUTBOX);
			// Log.i(TAG, "Outbox  localMessages = " + localMessages.size());
			synchronizedSendPendingMessagesCompleted(account);
			// if array is null, that will cause synchronized block to crash!
			if (!localMessages.isEmpty()) {
				for (C35Message message : localMessages) {
					String uid = message.getMailId();
					if (!synchronizedSendMessageContains(uid)) {
						synchronizedSendMessageAdd(message.getMailId());
					}
					try {
						sendSingleMessage(account, message);
						synchronizedSendMessageRemove(uid);
						synchronizedSendPendingMessagesCompleted(account);
					} catch (MessagingException e) {
						String exceptionMessage = e.getMessage();
						if (e.getExceptionType() == MessagingException.CONNECT_ERROR) {
							exceptionMessage = "50002";
						}
						if (exceptionMessage == null) {
							exceptionMessage = "50013";
						}
						if (!(message.getSendStats() == C35Message.SEND_SUCCESS)) {
							C35MailMessageUtil.sendMailFailedBroadcast(mApplication, message.getSubject(), exceptionMessage, uid, account);
						}
						synchronizedSendMessageRemove(uid);
						// 　当有一封邮件没有成功发送时，我们捕获异常，并提醒发送失败。然后继续发送其他邮件。
						// 　原来的策略是异常在调用它的地方捕获，导致他后面的邮件无法发送。
						continue;
					} catch (Exception e) {
						String exceptionMessage = e.getMessage();
						if (exceptionMessage == null) {
							exceptionMessage = "50014";
						}
						if (!(message.getSendStats() == C35Message.SEND_SUCCESS)) {
							C35MailMessageUtil.sendMailFailedBroadcast(mApplication, message.getSubject(), exceptionMessage, uid, account);
						}
						synchronizedSendMessageRemove(uid);
						// 　当有一封邮件没有成功发送时，我们捕获异常，并提醒发送失败。然后继续发送其他邮件。
						// 　原来的策略是异常在调用它的地方捕获，导致他后面的邮件无法发送。
						continue;
					}
				}
			}
		}
	}

	/**
	 * sendSingleMessage
	 * 
	 * @Description:
	 * @param account
	 * @param message
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-10-30
	 */
	private void sendSingleMessage(Account account, C35Message message) throws MessagingException {
		try {
			if (message != null) {
				message.setSendStats(C35Message.SEND_IN_PROGRESS);
				C35Store store;
				store = (C35Store) Store.getInstance(account.getStoreUri());
				store.openAndGetTicket();
				String mailId = store.sendMail(message, account, account.getUpdownloadport());
				// String mailId = mLocalStore.saveMessages(message, Email.MAILBOX_SENTBOX, account);
				if (mailId != null) {
					C35MailMessageUtil.sendMailSuccessBroadcast(mApplication, mApplication.getResources().getString(R.string.send_mail_success));
				}
			} else {
				Debug.e(TAG, "message == null");
				throw new MessagingException(MessagingException.REQUEST_DATA_ERROE);
			}
		} catch (MessagingException e) {
		    /*
			// if (e instanceof IOException) {
			if (e.getMessage().startsWith("50001")) {
				C35AppServiceUtil.writeSubscribeInformationToSdcard(e.getMessage());// 彩蛋log写入
				// 以下为弹出失败对话框
				Intent it = new Intent(mApplication, C35AlertFileNotExist.class);
				it.putExtra("messageid", message.getMailId());
				it.putExtra("messagesubject", message.getSubject());
				it.putExtra("folderid", message.getFolderId());
				Bundle bundle = new Bundle();
				bundle.putSerializable("account", account);
				it.putExtras(bundle);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mApplication.startActivity(it);
				//
			} else {
				// throw new MessagingException(e.getMessage());

				// } else if (e instanceof MessagingException) {
				throw e;
			}
			*/
		    // Modified by LL
	        throw e;
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
			throw new MessagingException(e.getMessage());
		}

		// }
	}

	/**
	 * sendSingleMessage
	 * 
	 * @Description:
	 * @param account
	 *            账号信息
	 * @param messageUid
	 *            邮件Uid
	 * @throws MessagingException
	 *             PushMail自定义异常
	 * @see:
	 * @since:
	 * @author:
	 * @date:2013-1-4
	 */
	public void sendSingleMessage(final Account account, final String messageUid) {
		C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_common).submit(new Runnable() {

			@Override
			public void run() {
				try {
					synchronized (mSendMessageLocker) {
						LocalStore mLocalStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
						C35Message c35Message = mLocalStore.getC35Message(account, messageUid);
						if (c35Message != null) {
							String uid = c35Message.getMailId();
							if (!synchronizedSendMessageContains(uid)) {
								synchronizedSendMessageAdd(uid);
							}
							try {
								sendSingleMessage(account, c35Message);
								synchronizedSendMessageRemove(uid);
								synchronizedSendPendingMessagesCompleted(account);
							} catch (MessagingException e) {
								String exceptionMessage = e.getMessage();
								if (e.getExceptionType() == MessagingException.CONNECT_ERROR) {
									exceptionMessage = "50002";
								}
								if (exceptionMessage == null) {
									exceptionMessage = "50013";
								}
								e.printStackTrace();
								if (!(c35Message.getSendStats() == C35Message.SEND_SUCCESS)) {
									C35MailMessageUtil.sendMailFailedBroadcast(mApplication, c35Message.getSubject(), exceptionMessage, uid, account);
								}
								synchronizedSendMessageRemove(uid);
							}
						}
					}
				} catch (Exception e) {
					Debug.e("failfast", "failfast_AA", e);
				}
			}
		});

	}

	/**
	 * Checks mail for one or multiple accounts. If account is null all accounts are checked. TODO: There is
	 * no use case for "check all accounts". Clean up this API to remove that case. Callers can supply the
	 * appropriate list.
	 * 
	 * @param context
	 * @param accountsToCheck
	 *            List of accounts to check, or null to check all accounts
	 * @param listener
	 */
	public void checkMail(final Context context, final Account pushAccount, final MessagingListener listener, final String folderId) {

		// TODO :临时解决 ACCOUNT null 问题 。后面要处理掉

		if (pushAccount == null) {
			Debug.e(TAG, "account is null!!!!!!!!!!!!!!");
			C35AppServiceUtil.writeSubscribeInformationToSdcard("account is null!!!!!!!!!!!!!");// 彩蛋log写入
			return;
		}
		
	    // Modified by LL
        // BEGIN
		/*
		if (!ShowNoConnectionActivity.isConnectInternet()) {
			return;
		}
		*/
		
		putCmd(CMD_SYNC_MAILBOX + " " + pushAccount.getEmail() + " " + folderId, listener, new Runnable() {

			@Override
			public void run() {
				Debug.v(TAG, "checkMail()" + pushAccount);
				syncCheckMailStarted(pushAccount); // TODO this
													// needs to pass
				syncMailBox(pushAccount, folderId, null, true, null, null, null, 0);
				synchronizedCheckMailFinished(pushAccount); // TODO this
				// needs
			}
		});
	}

	/**
	 * 描述 存草稿1、设置附件个数；2、存邮件3、存附件
	 * 
	 * @author liujie
	 * @date 2011-12-23
	 * @return void
	 * @throws
	 */
	public void saveC35Draft(final Account account, final C35Message message) throws MessagingException {
		synchronized (mSaveC35DraftLocker) {
			try {
				LocalStore mLocalStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
				List<C35Attachment> atts = message.getAttachs();
				message.setAttachSize(atts.size());
				message.setFolderId(EmailApplication.MAILBOX_DRAFTSBOX);
				String messageId = mLocalStore.saveMessages(message, EmailApplication.MAILBOX_DRAFTSBOX, account);
				message.setMailId(messageId);
				mLocalStore.deleteC35AttachmentsByUid(message.getMailId());// 先全删除
				for (int i = 0; i < atts.size(); i++) {
					C35Attachment att = atts.get(i);
					if (att.getId() != null) {
						List<C35CompressItem> compressItems = mLocalStore.getCompressItemsByAttachmentid(att.getId());
						for (C35CompressItem item : compressItems) {
							item.setAttachId(messageId + "_" + i);
						}
						mLocalStore.storeCompressItemsByC35CompressItems(compressItems);
						att.setMailId(messageId);
						att.setId(messageId + "_" + i);
						mLocalStore.storeAttachment(account, att);
					} else {
						att.setMailId(messageId);
						att.setId(messageId + "_" + i);
						mLocalStore.storeAttachment(account, att);
					}
				}
			} catch (MessagingException e) {
				Debug.w("failfast", "failfast_AA", e);
				throw e;
			} catch (Exception e) {
				Debug.w("failfast", "failfast_AA", e);
				throw new MessagingException("saveC35Draft error", e);
			}
		}
	}

	/**
	 * 与服务器交互的命令封装类
	 * 
	 * @Description:
	 * @author: cuiwei
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2013-12-13
	 */
	static class Command {

		private Runnable runnable;
		private MessagingListener listener;
		private String description;// 命令描述log用
		// public String uid; //
	}

	public void addListener(MessagingListener listener) {
		// clearListener();
		synchronized (mListeners) {
			if (!containListener(listener)) {
				// Debug.i(TAG, "addListener...listener=" + listener);
				mListeners.add(listener);
			}
		}
	}

	public void removeListener(MessagingListener listener) {
		synchronized (mListeners) {
			Debug.i(TAG, "removeListener's listener=" + listener);
			HashSet<MessagingListener> cache = new HashSet<MessagingListener>();
			for (Iterator<MessagingListener> it = mListeners.iterator(); it.hasNext();) {
				MessagingListener m = it.next();
				if (!m.equals(listener)) {
					cache.add(m);
				}
				it.remove();
			}
			mListeners = cache;
		}
	}

	public boolean containListener(MessagingListener listener) {
		synchronized (mListeners) {
			// Log.i("Listener",
			// "containListener's listener="+listener+"----mListeners="+mListeners);
			return mListeners.contains(listener);
		}
	}

	/**
	 * 邮件接收终了处理
	 * 
	 * @param account
	 * @param folder
	 * @param newMessages
	 * @param messages2Sync
	 */
	private void synchronizedMailboxHeaderFinished(Account account, String folder, int newMessages, List<String> messages2Sync) {
		// megCount = 0;
		GlobalVariable.isProgressing = false;
		GlobalVariable.setCurrentProgress(0);
		synchronized (mListeners) {
			// TODO: 回调方式可以改进，这种回调方式时间开销为n。如果使用hash表的存储结构时间开销就变为1了。
			for (MessagingListener l : mListeners) {
				l.synchronizeMailboxHeaderFinished(account, folder, newMessages, messages2Sync);
			}
		}
	}

	/**
	 * 同步结束，sendmessage通知UI处理进度条等
	 * 
	 * @Description:
	 * @param account
	 * @param folder
	 * @param numMessages
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-12-6
	 */
	private void synchronizedMailboxFinished(Account account, String folder, int numMessages) {
		// megCount = 0;
		GlobalVariable.isProgressing = false;
		GlobalVariable.setCurrentProgress(0);
		synchronized (mListeners) {
			// mSync = false;
			for (MessagingListener l : mListeners) {
				l.synchronizeMailboxFinished(account, folder, numMessages);
			}
		}
	}

	/**
	 * 提交请求失败
	 * 
	 * @Description:
	 * @param account
	 * @param folder
	 * @param e
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-10-22
	 */
	private void processCommandFailed(Account account, String folder, Exception e) {
		GlobalVariable.isProgressing = false;
		GlobalVariable.setCurrentProgress(0);
		// megCount = 0;
		synchronized (mListeners) {
			// mSync = false;
			try {
				for (Iterator<MessagingListener> it = mListeners.iterator(); it.hasNext();) { // reparations为Collection
					MessagingListener l = it.next();
					l.processCMDFailed(account, folder, e);
				}
			} catch (Exception e1) {
				Debug.e("failfast", "failfast_AA", e1);
			}
		}
	}

	/**
	 * 加载邮件内容的回调方法（用于显示邮件详情），正文预览页面和撰写页面用
	 * 
	 * @param account
	 * @param uid
	 * @param message
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-22
	 */
	private void LoadMessageForViewBody(Account account, String uid, C35Message message) {
		synchronized (mListeners) {
			for (MessagingListener l : mListeners) {
				// Debug.d(DBG_TAG, "MessagingListener:" + l.toString());
				l.loadAndShowMessageBodyForView(account, uid, message);
			}
		}
	}

	/**
	 * 显示邮件头信息，撰写页面 主题cc,bcc等
	 * 
	 * @Description:
	 * @param account
	 * @param uid
	 * @param message
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-22
	 */
	private void showMessageHeadersView(Account account, String uid, C35Message message) {
		synchronized (mListeners) {
			for (MessagingListener l : mListeners) {
				l.loadMessageForViewHeaders(account, uid, message);
			}
		}
	}

	/**
	 * Check Mail定时刷新邮件开始
	 * 
	 * @param account
	 */
	private void syncCheckMailStarted(Account account) {
		Debug.i(TAG, " syncCheckMailStarted    account==========" + account);
		synchronized (mListeners) {
			for (MessagingListener l : mListeners) {
				l.checkMailStarted(account);
			}
		}
	}

	/**
	 * 定时刷新邮件结束
	 * 
	 * @param pushAccount
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-12-6
	 */
	private void synchronizedCheckMailFinished(Account pushAccount) {
		synchronized (mListeners) {
			Debug.i("CheckMail", "synchronizedCheckMailFinished");
			for (MessagingListener l : mListeners) {
				l.checkMailFinished(pushAccount);
			}
		}
	}

	private void synchronizedSendPendingMessagesCompleted(Account account) {
		synchronized (mListeners) {
			for (MessagingListener l : mListeners) {
				l.sendPendingMessagesCompleted(account);
			}
		}
	}

	// ///////////////////////////////////////////////////////////////////
	//
	private void synchronizedSendMessageAdd(final String uid) {
		synchronized (mSendingMessages) {
			mSendingMessages.add(uid);
		}
	}

	private boolean synchronizedSendMessageRemove(final String uid) {
		synchronized (mSendingMessages) {
			return mSendingMessages.remove(uid);
		}
	}

	public boolean synchronizedSendMessageContains(final String uid) {
		synchronized (mSendingMessages) {
			return mSendingMessages.contains(uid);
		}
	}

	/**
	 * 从DB查询邮件列表信息list local message
	 * 
	 * @param account
	 * @param folder
	 * @param limit
	 *            max message to return
	 * @param onlyUnread
	 *            true: return unread messages ,false: return all
	 * @param onlyToMe
	 *            true: return messages that only directly send to me ,false: return all;
	 * @param listener
	 * @return
	 * @see:
	 * @since:2.0
	 * @author: xulei
	 * @date:Mar 15, 2012
	 */
	public Cursor loadLocalMessages(final Account account, final String folderId, int limit, MessagingListener listener, int mailstate) {
		// synchronizedListLocalMessagesStarted(account, folder);
		Cursor cursor = null;
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			LocalFolder localFolder = localStore.getFolder(account, folderId);
			localFolder.open(OpenMode.READ_WRITE);
			cursor = localFolder.getMessages(limit, account.getmEmailShow(), mailstate);
			localFolder.close(false);
			// synchronizedListLocalMessagesFinished(account, folder);
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
			// synchronizedListLocalMessagesFailed(account, folder, e.getMessage());
		}
		return cursor;
	}

	/**
	 * 根据id删除邮件
	 * 
	 * @param account
	 * @param id
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-26
	 */
	public void deleteMessage(final Account account, long id) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			int deleteFlag = localStore.getDeleteFlag(id);
			switch (deleteFlag) {
			case C35Message.DELETE_FLAG_NORMOR:
				// localStore.createFolder(account, Email.MAILBOX_TRASHBOX);
				localStore.deleteMessage(id);
				this.putOperationHistoryInfoMap(account, localStore.getMessageUidById(id), GlobalConstants.DELETE_FALG, GlobalConstants.NOT_DEL_COMPLETELY);
				break;
			case C35Message.DELETE_FLAG_DELETED:
				this.putOperationHistoryInfoMap(account, localStore.getMessageUidById(id), GlobalConstants.DELETE_FALG, GlobalConstants.DEL_COMPLETELY);
				localStore.clearMessage(id);
				break;
			}
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 根据uid删除邮件
	 * 
	 * @param account
	 * @param uid
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-26
	 */
	public void deleteLocalMessage(final Account account, String uid) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			long messageId = localStore.getMessageIdByUid(account, uid);
			deleteMessage(account, messageId);
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 批量删除邮件
	 * 
	 * @param account
	 * @param folder
	 * @param ids
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-26
	 */
	public void deleteMessages(final Account account, final String folder, Set<Long> ids) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			if (folder.equals(EmailApplication.MAILBOX_TRASHBOX)) {
				for (Long id : ids) {
					this.putOperationHistoryInfoMap(account, localStore.getMessageUidById(id), GlobalConstants.DELETE_FALG, GlobalConstants.DEL_COMPLETELY);
				}
				localStore.destoryMessages(ids);
			} else {
				// localStore.createFolder(account, Email.MAILBOX_TRASHBOX);
				localStore.deleteMessages(ids);
				for (Long id : ids) {
					this.putOperationHistoryInfoMap(account, localStore.getMessageUidById(id), GlobalConstants.DELETE_FALG, GlobalConstants.NOT_DEL_COMPLETELY);
				}
			}
		} catch (Exception e) {// cannot commit - no transaction is
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 彻底删除邮件
	 * 
	 * @param account
	 * @param uid
	 * @see:
	 * @since:
	 * @author:
	 * @date:2012-11-22
	 */
	public void destroyMessage(Account account, String uid) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			localStore.clearMessage(account, uid);
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 还原单封邮件
	 * 
	 * @Description:
	 * @param account
	 * @param folder
	 * @param id
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-12-18
	 */
	public void restoreMessage(final Account account, final String folder, Long id) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			if (folder.equals(EmailApplication.MAILBOX_TRASHBOX)) {
				localStore.restoreMessage(id);
			}
			// TODO:sofia2.0环境不支持此接口。
			Boolean result = AccountUtil.isSupportRequest("partOfCommitSyn", account);
			if (result == null || result == false) {
				return;
			}
			// 将操作记录在同步履历表中
			String foldId = localStore.getFolderIdByAccountAndMessageUid(account, localStore.getMessageUidById(id));
			this.putOperationHistoryInfoMap(account, localStore.getMessageUidById(id), GlobalConstants.MOVE_TO, foldId);
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 还原单封邮件
	 * 
	 * @Description:
	 * @param account
	 * @param folder
	 * @param id
	 * @see:
	 * @since:
	 * @author: wennan
	 * @date:2012-12-18
	 */
	public void restoreMessage(final Account account, final String folder, String messageUid) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			if (folder.equals(EmailApplication.MAILBOX_TRASHBOX)) {
				localStore.restoreMessage(account, messageUid);
			}
			// TODO:sofia2.0环境不支持此接口。
			Boolean result = AccountUtil.isSupportRequest("partOfCommitSyn", account);
			if (result == null || result == false) {
				return;
			}
			// 将操作记录在同步履历表中
			String foldId = localStore.getFolderIdByAccountAndMessageUid(account, messageUid);
			this.putOperationHistoryInfoMap(account, messageUid, GlobalConstants.MOVE_TO, foldId);
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 邮件还原
	 * 
	 * @param account
	 * @param folder
	 * @param ids
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-31
	 */
	public void restoreMessages(final Account account, final String folder, Set<Long> ids) {
		try {

			// TODO:sofia2.0环境不支持此接口。
			// Boolean result = AccountUtil.isSupportRequest("partOfCommitSyn", account);
			// if (result == null || result == false) {
			// // AccountUtil.nosupportRequestToast();//后台不能吐丝，暂时注掉
			// return;
			// }
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			if (folder.equals(EmailApplication.MAILBOX_TRASHBOX)) {
				localStore.restoreMessages(ids);
			}

			for (Long id : ids) {
				String foldId = localStore.getFolderIdByAccountAndMessageUid(account, localStore.getMessageUidById(id));
				this.putOperationHistoryInfoMap(account, localStore.getMessageUidById(id), GlobalConstants.MOVE_TO, foldId);
			}
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 移动邮件
	 * 
	 * @param account
	 *            账号
	 * @param srcFolderId
	 *            源文件夹
	 * @param tarFolderId
	 *            目标文件夹
	 * @param uid
	 *            邮件uid
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2013-1-11
	 */
	public boolean moveMessage(final Account account, final String srcFolderId, final String tarFolderId, String uid) {
		boolean canBeMoved = true;
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());

			if (tarFolderId.equals(EmailApplication.MAILBOX_TRASHBOX)) {// 已删除
				localStore.deleteMessage(account, uid);
			} else if (tarFolderId.equals(EmailApplication.MAILBOX_FAVORITEBOX)) {// 收藏夹
			} else if (tarFolderId.equals(EmailApplication.MAILBOX_DRAFTSBOX)) {// 草稿箱
			} else if (tarFolderId.equals(EmailApplication.MAILBOX_OUTBOX)) {// 发件箱
			} else if (tarFolderId.equals(EmailApplication.MAILBOX_SENTBOX)) {// 已发送
			} else if (tarFolderId.equals(EmailApplication.MAILBOX_ATTACHMENTBOX)) {// 附件夹
			} else {// 收件箱或者自定义
				if (srcFolderId.equals(EmailApplication.MAILBOX_TRASHBOX)) {
					localStore.restoreMessage(account, uid);
					localStore.moveMessage(account, uid, tarFolderId);
				} else {
					localStore.moveMessage(account, uid, tarFolderId);
				}

			}
			// 将操作记录在同步履历表中
			this.putOperationHistoryInfoMap(account, uid, GlobalConstants.MOVE_TO, tarFolderId);
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
			canBeMoved = false;
		}
		return canBeMoved;
	}

	/**
	 * 移动邮件批量
	 * 
	 * @param account
	 *            账号
	 * @param srcFolderId
	 *            源文件夹
	 * @param tarFolderId
	 *            目标文件夹
	 * @param ids
	 *            邮件id列表
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2013-1-11
	 */
	public boolean moveMessages(final Account account, final String srcFolderId, final String tarFolderId, Set<Long> ids) {
		boolean canBeMoved = true;
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			for (long id : ids) {
				String uid = localStore.getMessageUidById(id);
				this.moveMessage(account, srcFolderId, tarFolderId, uid);
			}
		} catch (MessagingException e) {
			canBeMoved = false;
			Debug.e("failfast", "failfast_AA", e);
		}
		return canBeMoved;
	}

	/**
	 * 获取收藏状态
	 * 
	 * @param account
	 * @param folder
	 * @param uid
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-31
	 */
	public int getFavorite(final Account account, final String folderId, String uid) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			LocalFolder currFolder = localStore.getFolder(account, folderId);
			currFolder.open(OpenMode.READ_WRITE);
			return localStore.getFavorite(account, uid);
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		return -1;
	}

	/**
	 * 创建客户端操作履历对象
	 * 
	 * @Description:
	 * @param account
	 * @param messageuid
	 * @param statusId
	 * @param statusValue
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-8
	 */
	private OperationHistoryInfo createNewOperationHistoryInfo(final Account account, final String messageuid, final int statusId, final String statusValue) {
		OperationHistoryInfo operationHistoryInfo = new OperationHistoryInfo();
		operationHistoryInfo.setAccount_uid(account.getUuid());
		operationHistoryInfo.setFolderId(this.getStringFolderId(account, messageuid));
		operationHistoryInfo.setMailId(messageuid);
		operationHistoryInfo.setStatusId(statusId);
		operationHistoryInfo.setStatusValue(statusValue);
		operationHistoryInfo.setCommitStatus(GlobalConstants.COMMIT_STATUS_DEFAULT);
		operationHistoryInfo.setOperateTime(System.currentTimeMillis());
		return operationHistoryInfo;

	}

	private String getStringFav(boolean newFav) {
		return newFav ? GlobalConstants.YES_FAV : GlobalConstants.NO_FAV;
	}

	private String getStringRead(boolean newRead) {
		return newRead ? GlobalConstants.YES_READ : GlobalConstants.NO_READ;
	}

	/**
	 * 得到folderid //.Draft等
	 * 
	 * @Description:
	 * @param account
	 * @param messageUid
	 * @return //.Draft
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-8
	 */
	private String getStringFolderId(final Account account, String messageUid) {
		String folderid = null;
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			folderid = localStore.getFolderIdByAccountAndMessageUid(account, messageUid);
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}

		return folderid;
	}

	/**
	 * 根据uid收藏
	 * 
	 * @param account
	 * @param uid
	 * @param newFav
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-31
	 */
	public void setFavorite(final Account account, String uid, boolean newFav) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			long messageId = localStore.getMessageIdByUid(account, uid);
			setFavorite(account, messageId, newFav);
			// TODO:sofia2.0环境不支持此接口。
			Boolean result = AccountUtil.isSupportRequest("partOfCommitSyn", account);
			if (result == null || result == false) {
				return;
			}
			this.putOperationHistoryInfoMap(account, uid, GlobalConstants.IMPORTANT_FLAG, this.getStringFav(newFav));
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 根据id收藏
	 * 
	 * @param account
	 * @param id
	 *            邮件id
	 * @param newFav
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-26
	 */
	public void setFavorite(final Account account, Long id, boolean newFav) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			LocalFolder favFolder = localStore.getFolder(account, EmailApplication.MAILBOX_FAVORITEBOX);
			favFolder.open(OpenMode.READ_WRITE);
			localStore.setFavorite(id, newFav);
			// TODO:sofia2.0环境不支持此接口。
			Boolean result = AccountUtil.isSupportRequest("partOfCommitSyn", account);
			if (result == null || result == false) {
				return;
			}
			this.putOperationHistoryInfoMap(account, localStore.getMessageUidById(id), GlobalConstants.IMPORTANT_FLAG, this.getStringFav(newFav));
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 收藏批量
	 * 
	 * @param account
	 * @param folder
	 * @param ids
	 * @param newFav
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-26
	 */
	public void setFavorite(final Account account, final String folder, Set<Long> ids, boolean newFav) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			LocalFolder favFolder = localStore.getFolder(account, EmailApplication.MAILBOX_FAVORITEBOX);
			favFolder.open(OpenMode.READ_WRITE);
			localStore.setFavorite(ids, newFav);
			// TODO:sofia2.0环境不支持此接口。
			Boolean result = AccountUtil.isSupportRequest("partOfCommitSyn", account);
			if (result == null || result == false) {
				return;
			}
			for (Long id : ids) {
				this.putOperationHistoryInfoMap(account, localStore.getMessageUidById(id), GlobalConstants.IMPORTANT_FLAG, this.getStringFav(newFav));
			}
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 标记邮件已读未读状态，存入历史记录
	 * 
	 * @param account
	 * @param messageId
	 * @param newRead
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-11
	 */
	private void setRead(final Account account, Long messageId, boolean newRead) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			localStore.setRead(messageId, newRead);
			this.putOperationHistoryInfoMap(account, localStore.getMessageUidById(messageId), GlobalConstants.READ_FLAG, this.getStringRead(newRead));
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 标记邮件已读未读状态（批量）
	 * 
	 * @param account
	 * @param folder
	 * @param ids
	 * @param newRead
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-31
	 */
	public void setRead(final Account account, final String folder, Set<Long> ids, boolean newRead) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			localStore.setRead(ids, newRead);
			for (Long id : ids) {
				this.putOperationHistoryInfoMap(account, localStore.getMessageUidById(id), GlobalConstants.READ_FLAG, this.getStringRead(newRead));
			}
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 标记邮件已读未读状态
	 * 
	 * @param account
	 * @param uid
	 * @param newRead
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-31
	 */
	public void setReadFlag(final Account account, String uid, boolean newRead) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			long messageId = localStore.getMessageIdByUid(account, uid);
			setRead(account, messageId, newRead);
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 标记下载状态
	 * 
	 * @param account
	 * @param uid
	 * @param downFull
	 * @see:
	 * @since:
	 * @author: liujie
	 * @date:2012-4-5
	 */
	public void setDownFlagFull(final Account account, String uid) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			localStore.setDownloadFlag(account, uid, C35Message.DOWNLOAD_FLAG_FULL);
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 同步联系人
	 * 
	 * @param account
	 * @param Contacts
	 * @param type
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2013-5-30
	 */
	public void syncCommonContacts(final Account account, int type, int size) {
		try {
			List<Contact> contacts = null;
			C35Store remoteStore = (C35Store) Store.getInstance(account.getStoreUri());
			remoteStore.openAndGetTicket();
			contacts = remoteStore.getContacts(type, size);
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			localStore.syncCommonContacts(account, contacts, type);
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 搜索服务器端邮件
	 * 
	 * @param account
	 * @param keyWord
	 * @param searchKeyPosi
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jul 18, 2012
	 */
	public List<C35Message> searchMessagesFromServer(Account account, String folder, String keyWord, int searchKeyPosi, int pageNo, int pageSize, boolean ifSaveHistory) {
		List<C35Message> messages2Show = new ArrayList<C35Message>();
		// search from server
		try {
			String from = null;
			String to = null;
			String historyKeyWord = keyWord;
			switch (searchKeyPosi) {
			case MailUtil.SEARCH_MAIL_CONTEXT:
				searchKeyPosi = mSearchContext;
				break;
			case MailUtil.SEARCH_MAIL_SUBJECT:
				searchKeyPosi = mSearchSubject;
				break;
			case MailUtil.SEARCH_MAIL_RECEIVER:
				to = keyWord;
				keyWord = "";
				searchKeyPosi = mSearchAll;
				break;
			case MailUtil.SEARCH_MAIL_SENDER:
				from = keyWord;
				keyWord = "";
				searchKeyPosi = mSearchAll;
				break;
			case MailUtil.SEARCH_MAIL_ALL:
				searchKeyPosi = mSearchAll;
				break;
			case MailUtil.SEARCH_MAIL_BETWEEN:
				searchKeyPosi = mSearchBetweenUser;
				break;
			default:
				break;
			}
			C35Store remoteStore = (C35Store) Store.getInstance(account.getStoreUri());
			remoteStore.openAndGetTicket();
			List<C35Message> messages;
			if (searchKeyPosi == mSearchBetweenUser) {
				List<String> messageids = remoteStore.getBetweenUsMail(account.getEmail(), "", keyWord, pageNo, pageSize, 1, 0);
				messages = remoteStore.getMailListByMailIds(messageids, 150);
			} else {
				messages = remoteStore.advanceSearchMails(folder, keyWord, from, to, searchKeyPosi, 1, 0, pageNo, pageSize);
			}
			List<C35Message> newMessages = new ArrayList<C35Message>();
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			LocalFolder localFolder = localStore.getFolder(account, EmailApplication.MAILBOX_INBOX);
			localFolder.open(OpenMode.READ_WRITE);
			if (messages != null && messages.size() > 0) {
				Map<String, Integer> localUidMap = new HashMap<String, Integer>();
				Cursor cursor;
				if (searchKeyPosi == mSearchBetweenUser) {
					cursor = localFolder.getAllMessages();
				} else {
					cursor = localFolder.getMessages(folder);
				}
				while (cursor.moveToNext()) {
					localUidMap.put(cursor.getString(0), cursor.getInt(1));
				}
				cursor.close();
				for (C35Message message : messages) {
					if (localUidMap.get(message.getMailId()) == null) {
						newMessages.add(message);
						messages2Show.add(message);
					} else if (localUidMap.get(message.getMailId()) != C35Message.DELETE_FLAG_DESTORYED) {
						messages2Show.add(message);
					}
				}
				localFolder.saveMessagesHeader(account, newMessages);
				if (ifSaveHistory) {
					localFolder.saveSearchHistory(account, historyKeyWord);
				}
			}
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
			if (e.getExceptionType() == MessagingException.CODE_SEARCH_TIME_OUT_FOR_IMAP_ERROR) {
				return null;
			}
		}
		// search from local
		// try {
		// LocalStore localStore = (LocalStore)
		// Store.getInstance(account.getLocalStoreUri(), mApplication);
		// } catch (MessagingException e) {
		// Debug.e("failfast", "failfast_AA", e);
		// }
		return messages2Show;
	}

	public List<String> getSearchHistoryKeyword(Account account) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			LocalFolder localFolder = localStore.getFolder(account, EmailApplication.MAILBOX_INBOX);
			localFolder.open(OpenMode.READ_WRITE);
			return localFolder.getSearchHistoryKeyword(account);
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		return null;
	}

	public void deleteSearchHistory(Account account) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			LocalFolder localFolder = localStore.getFolder(account, EmailApplication.MAILBOX_INBOX);
			localFolder.open(OpenMode.READ_WRITE);
			localFolder.deleteSearchHistory();
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 搜索手机端邮件（ 所有文件夹）
	 * 
	 * @param account
	 * @param keyWord
	 * @param searchKeyPosi
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public List<C35Message> searchMessagesFromLocal(Account account, String keyWord, int searchKeyPosi, int pageNo, int pageSize, boolean ifSaveHistory) {
		List<C35Message> messages = new ArrayList<C35Message>();
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			messages = localStore.searchMails(account, keyWord, searchKeyPosi, pageNo, pageSize);
			LocalFolder localFolder = localStore.getFolder(account, EmailApplication.MAILBOX_INBOX);
			localFolder.open(OpenMode.READ_WRITE);
			if (ifSaveHistory && messages != null && messages.size() > 0) {
				localFolder.saveSearchHistory(account, keyWord);
			}
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		return messages;
	}

	/**
	 * push，下拉刷新，载入更多时调用，取相应箱子的一批邮件
	 * 
	 * @param account
	 *            用户对象
	 * @param folderId
	 *            注意！2.5版本中定义为Folder表中的folderId,不在是原来folder表中的name。
	 * @param refUid
	 *            参考Uid
	 * @param newer
	 *            收取方向。true为收取最新邮件，false为根据参考Uid向下收取相应数量的邮件
	 * @param visibleMailIds
	 *            为当前可见邮件的ids。
	 * @see:
	 * @since:
	 * @date: 2012,11,7
	 */
	private void syncMailBox(final Account account, final String folderId, String refUid, boolean newer, List<String> visibleMailIds, List<C35Message> visibleMessages, String searchKey, int pageNo) {
		try {
			Debug.i("syncMailBox", "syncMailBox_Start  " + account.getmEmailShow() + " " + folderId);
			synchronizingMailbox = true;
			// synchronizedMailboxStarted(account, folderName);
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			LocalFolder localFolder = localStore.getFolder(account, folderId);// 此处传入folderId
			localFolder.open(OpenMode.READ_WRITE);
			C35Store remoteStore = (C35Store) Store.getInstance(account.getStoreUri());
			remoteStore.openAndGetTicket();
			int limit = account.getRecvMailLimit();
			// String folderId = localStore.getFolderIdByFolderNameAndAccount(account, folderName);
			
			System.out.println(" account.isAutoSync()  start");
			if (account.isAutoSync()) {
				this.commitMailsStatus(account);// 提交履历表，同步本地邮件状态到服务器
				List<String> uids = prepareVisibleUidsForGetMailsStatusFunc(account, folderId, visibleMailIds, limit);
				if (uids != null && !uids.isEmpty()) {
					this.getMailsStatus(account, folderId, uids, visibleMessages);
				}

			}
			System.out.println(" account.isAutoSync()  end");

			synchronizingMailbox = false;

			Debug.i("no refresh", "syncUid");
			//
			// 1. sync uid
			/**
			 * @date: APR 10 2012 临时解决方案，修复因不同步造成收取邮件异常的问题 同步实施后，记得处理
			 * */
			// 开始同步邮件ID
			
//			System.out.println(" syncUid   start");
			List<String> messages2Sync = syncUid(remoteStore, localFolder, refUid, limit, newer, folderId, searchKey, pageNo);
		
//			System.out.println(" syncUid   end");
			
//			System.out.println(" messages2Sync.size()   start");
			
			if (messages2Sync.size() == 0) {
				// 此处：messages2Sync.size()==0时，并不一定是从服务器上返回的结果为空，是经过滤重后为空,那载入已完成的提示可能会有问题,在MessageList页避免了重复加载更多的请求
				synchronizedMailboxHeaderFinished(account, folderId, newer ? messages2Sync.size() : -5, messages2Sync); // 加载更多时没有邮件返回-5，不更新列表
				Debug.i("syncMailBox", "syncMailBox_End size() == 0 " + account.getmEmailShow() + " " + folderId);
				return;
			}
			
//			System.out.println(" messages2Sync.size()   end");
			
			// 2. sync header
			// 3. save header保存邮件头信息
			
//			System.out.println(" saveMessagesHeader   start");
			localFolder.saveMessagesHeader(account, remoteStore.getMailListByMailIds(messages2Sync, 150));
			
//			System.out.println(" saveMessagesHeader   end");
			
//			System.out.println(" synchronizedMailboxHeaderFinished   start");
			
			synchronizedMailboxHeaderFinished(account, folderId, newer ? messages2Sync.size() : -1, messages2Sync);
			// 向系统发广播提示未读邮件数目//为了优化性能去掉，已经和产品方面沟通过了
			// C35MailMessageUtil.sendMailMessageBroadcast(mApplication, (LocalStore)
			// Store.getInstance(account.getLocalStoreUri()), false);
			// 4. fetch body if necessary
			
//			System.out.println(" synchronizedMailboxHeaderFinished   end");

			
			
			
//			System.out.println(" mMessagesQueue   start");

			
			// TODO 此下载队列是否应该放在完整模式判断内部？
			// 先清空队列，防止非当前账户id
			mMessagesQueue.clear();
			for (String uid : messages2Sync) {
				if (!mMessagesQueue.contains(uid))
					mMessagesQueue.offerFirst(uid);
			}
			
//			System.out.println(" mMessagesQueue   start");

			
//			System.out.println(" getMailsFromServerInQueue   start");

			
			// 用户设置为完整模式，或者为自动切换模式且符合完整模式的条件
			if (ReceiveMessageModeUtil.getReceiveMode(account)) {
				Debug.v(TAG, "完整模式：加载邮件正文");
				// synchronizeMessagesInqueue(account, folderId);
				// synchronizedMailboxFinished(account, folderId, messages2Sync.size());
				Debug.v(TAG, "downLoading==" + downLoading);
				if (!downLoading) {
					C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_AtOnce).submit(new Runnable() {

						@Override
						public void run() {
							try {
								downLoading = true;
								getMailsFromServerInQueue(account, folderId, false);
								downLoading = false;
							} catch (MessagingException e) {
								Debug.w("failfast", "failfast_AA", e);
								downLoading = false;// 置为false，防止正在下载邮件时突然断网，再重新登录账户时不自动加载邮件内容的问题
								processCommandFailed(account, folderId, e);
								// Toast.makeText(EmailApplication.getInstance(),
								// "getMailsFromServerInQueue failed :\n"+e.getMessage(),
								// Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
			} else {
				Debug.v(TAG, "省流量模式：仅加载邮件头");
			}
			
//			System.out.println(" getMailsFromServerInQueue   end");
			localFolder.setLastUpdate(System.currentTimeMillis());
			Debug.i("syncMailBox", "syncMailBox_End  " + account.getmEmailShow() + " " + folderId);
		} catch (Exception e) {
			Debug.w("syncMailBox", "syncMailBox_EXP_End s  " + account.getmEmailShow() + " " + folderId);
			synchronizingMailbox = false;
//			System.out.println(" Exception   start"+e.getMessage());
			processCommandFailed(account, folderId, e);
			Debug.e("syncMailBox", "syncMailBox_EXP_End e  " + account.getmEmailShow() + " " + folderId);
		}
	}

	/**
	 * 过滤同步uid
	 * 
	 * @Description:
	 * @param remoteStore
	 * @param localFolder
	 * @param refUid
	 * @param limit
	 * @param newer
	 * @param folderId
	 * @param searchKey
	 *            收藏夹单独处理
	 * @param pageNo
	 *            收藏夹单独处理
	 * @return 过滤后实际要请求的邮件list
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-10-15
	 */
	private List<String> syncUid(C35Store remoteStore, LocalFolder localFolder, String refUid, int limit, boolean newer, String folderId, String searchKey, int pageNo) throws MessagingException {
		// 从服务器得到的邮件ID list
		List<String> newMessages = new ArrayList<String>();
		// 返回同步后的邮件ID list
		List<String> messages2Sync = new ArrayList<String>();
		if (newer) {// push 与 下拉刷新时 true
			// refUid = localFolder.getNewestUid();
			if (refUid == null || refUid.startsWith("Local")) {
				refUid = "";
			}
			if (folderId.equals(EmailApplication.MAILBOX_FAVORITEBOX)) {// 收藏夹单独处理
				newMessages = remoteStore.searchIdsByType(5, searchKey, pageNo, limit, 1, 0);
			} else {
				newMessages = remoteStore.getIdsById(folderId, limit, refUid, GetIdsType.NEW);
			}

			// 过滤重复邮件
			filterUids(localFolder, messages2Sync, newMessages, folderId);
		} else {// 载入更多时false
			if (!folderId.equals(EmailApplication.MAILBOX_FAVORITEBOX)) {
				if (refUid == null || refUid.equals("")) {// 本地的参考UID
					refUid = localFolder.getRefUid();
				}
				if (refUid != null) {
					// 从服务器取得相应UID list
					newMessages = remoteStore.getIdsById(folderId, limit, refUid, GetIdsType.OLD);
					// 开始过滤重复邮件
					int done = filterUids(localFolder, messages2Sync, newMessages, folderId);
					while (done < limit && newMessages.size() > 0) {
						refUid = newMessages.get(newMessages.size() - 1);
						newMessages = remoteStore.getIdsById(folderId, limit - done, refUid, GetIdsType.OLD);
						// 递归累加取得的uid数量
						done += filterUids(localFolder, messages2Sync, newMessages, folderId);
					}
				} else {
					newMessages = remoteStore.getIdsById(folderId, limit, "", GetIdsType.NEW);
					filterUids(localFolder, messages2Sync, newMessages, folderId);
				}
			} else {// 收藏夹时

				// 从服务器取得相应UID list
				newMessages = remoteStore.searchIdsByType(5, searchKey, pageNo, limit, 1, 0);
				// 开始过滤重复邮件
				int done = filterUids(localFolder, messages2Sync, newMessages, folderId);
				while (done < limit && newMessages.size() > 0) {
					newMessages = remoteStore.searchIdsByType(5, searchKey, pageNo + 1, limit, 1, 0);
					// 递归累加取得的uid数量
					done += filterUids(localFolder, messages2Sync, newMessages, folderId);
				}
			}
		}
		return messages2Sync;
	}

	/**
	 * 过滤重复邮件
	 * 
	 * @param localFolder
	 * @param messages2Sync
	 * @param newMessages
	 *            从服务器取得的UID list
	 * @return 取得的邮件ID数量（减去本地删除数量的值）
	 */
	private int filterUids(LocalFolder localFolder, List<String> messages2Sync, List<String> newMessages, String folderId) {
		int result = 0;
		if (newMessages.size() > 0) {
			Map<String, Integer> localUidMap = new HashMap<String, Integer>();
			// 得到相应folder下所有邮件结果集

			Cursor cursor = localFolder.getMessages(folderId);
			while (cursor.moveToNext()) {
				// 存入localUidMap uid,delete_flag
				localUidMap.put(cursor.getString(0), cursor.getInt(1));
			}
			cursor.close();
			for (String uid : newMessages) {
				if (localUidMap.get(uid) == null) {// 本地没有 该邮件ID
					Debug.i(TAG, "newMessage   uid:" + uid);
					messages2Sync.add(uid);
					result++;
				} else if (localUidMap.get(uid) == C35Message.DELETE_FLAG_NORMOR) {// 正常邮件
					result++;
				} else {
					Debug.i(TAG, "newMessage 12313  uid:" + uid);
					messages2Sync.add(uid);
				}
			}
		}
		return result;
	}

	/**
	 * 
	 * @Description:优先下载一封邮件
	 * @param account
	 * @param folder
	 * @param uid
	 * @param callback
	 * @see:
	 * @since:2.0
	 * @author: xulei
	 * @date:Mar 15, 2012
	 */
	private void getMailFromServerPriority(final Account account, final String folder, final String uid, final C35Message tempmessage, final boolean isC35, final MessagingListener listener) {
		synchronized (mGetMailFromServerPriorityLocker) {

			// 省流量下不可以调用synchronizeMessagesInqueue，他会下载messagesQueue中所有的邮件体
			// 清掉messagesQueue也不好因为有可能线程正在下载其他的邮件，所以单独调用synchronizeMessage
			if (!mMessagesQueue.isEmpty() && mMessagesQueue.contains(uid)) {
				mMessagesQueue.remove(uid);
			}
			putCmd(CMD_SYNC_MESSAGE + " " + account.getEmail() + " " + folder + " " + uid, C35MailThreadPool.ENUM_Thread_Level.TL_AtOnce, null, new Runnable() {

				@Override
				public void run() {
					MessageCallback callback = new MessageCallback() {

						@Override
						public void getMessageFinished(String uid) {
							if (isC35) {
								try {
									LocalStore localStore = (LocalStore) (Store.getInstance(account.getLocalStoreUri()));
									C35Message message = localStore.getC35MessageHtmlContent(account, uid);
									tempmessage.setCompressItems(message.getCompressItems());
									tempmessage.setHyperText(message.getHyperText());
									tempmessage.setPlainText(message.getPlainText());
									tempmessage.setAttachs(message.getAttachs());
									tempmessage.setRead(C35Message.READ_FLAG_SEEN);
									tempmessage.setDownFalg(message.getDownFalg());
									tempmessage.setMailType(message.getMailType());
									tempmessage.setCalendarStartTime(message.getCalendarStartTime());
									tempmessage.setCalendarEndTime(message.getCalendarEndTime());
									tempmessage.setCalendarLocation(message.getCalendarLocation());
									tempmessage.setCalendarState(message.getCalendarState());
									tempmessage.setCompressedToSize(message.getCompressedToSize());
								} catch (MessagingException e) {
									Debug.e("failfast", "failfast_AA", e);
								}
								LoadMessageForViewBody(account, uid, tempmessage);
							} else {
								if (listener != null)
									listener.loadAndShowMessageBodyForView(account, uid, null);
							}
						}

						@Override
						public void getMessageFailed(String uid) {
							if (listener != null)
								listener.loadRemoteMessageForViewFailed(account, uid, new MessagingException(MessagingException.SERVER_IO_ERROR, "下载失败"), null);
						}
					};
					try {

						priorityListeners.put(uid, callback);
						getMailByIdFromServer(account, folder, uid);
					} catch (Exception e) {
						callback.getMessageFailed(uid);
						processCommandFailed(account, folder, e);
						// Toast.makeText(EmailApplication.getInstance(),
						// "getMailByIdFromServer failed :\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
						if (!mMessagesQueue.contains(uid))
							mMessagesQueue.offerFirst(uid);
					}
				};
			});
		}
	}

	/**
	 * 下载邮件
	 * 
	 * @param account
	 * @param folderName
	 * @param messageQueue
	 *            要下载邮件体的邮件队列
	 * @param isAutoDownLoad
	 *            是否是退出应用并在wifi下下载邮件，否则是下拉刷新或加载更多情况
	 * @throws MessagingException
	 * @see:
	 * @since:2.0
	 * @author: xulei
	 * @date:Mar 15, 2012
	 */
	private void getMailsFromServerInQueue(Account account, String folderId, boolean isAutoDownLoad) throws MessagingException {
		C35Store remoteStore = (C35Store) Store.getInstance(account.getStoreUri());
		if (isAutoDownLoad) {
			isAutoDownLoad = NetworkUtil.isWifi();
		} else {
			isAutoDownLoad = true;
		}

		List<C35Attachment> attachmentsToDownload = new ArrayList<C35Attachment>();

		while (!mMessagesQueue.isEmpty() && isAutoDownLoad) {
			Debug.d(TAG, "mMessagesQueue.size()  " + mMessagesQueue.size());
			String uid = mMessagesQueue.peek();
			// MessageCallback callback = priorityListeners.get(uid);
			try {
				remoteStore.openAndGetTicket();
				C35Message message = remoteStore.getMailById(uid, 0, 0, GlobalConstants.GZIP_BASE64, 4);//imcore
				// 用新的加密解密方式获取getmailbyid
				// 由于服务器没有部署
				// 先注释掉
				// C35Message
				// message
				// =
				// remoteStore.getMailById(uid,
				// 0,
				// 0,
				// GlobalConstants.GZIP_BASE64,
				// 4);
				// if (setRead == 1) {
				// message.setRead(setRead);
				// setRead = 0;
				// }
				message.setDownFalg((message.getAttachSize() == 0 && message.getAttachs().size() == 0) ? C35Message.DOWNLOAD_FLAG_FULL : C35Message.DOWNLOAD_FLAG_PARTIAL);
				LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
				if (!GlobalVariable.clearCache) {
					// 判断是否是清除缓存后保存的第一封邮件，因为清除缓存过程中可能有邮件还在下载，会造成剩一封邮件的情况
					localStore.saveMessages(message, folderId, account);
				} else {
					GlobalVariable.clearCache = false;
				}

				List<C35Attachment> caList = message.getAttachs();
				if (caList != null) {
					for (C35Attachment att : caList) {
						localStore.storeAttachment(account, att);
					}
					localStore.storeCompressItemsByC35CompressItems(message.getCompressItems());
					for (C35Attachment c35Attachment : caList) {

						if (c35Attachment.getFileSize() <= GlobalConstants.AUTODOWN_LOAD_ATT_SIZE) {
							Debug.v(TAG, "add need down load message attachment mesage Sub=" + message.getSubject() + " attachment name=" + c35Attachment.getFileName());
							attachmentsToDownload.add(c35Attachment);// 将要下载的附件存到list中，加载完所有正文后再下载
						}
					}
				}
				// if (callback != null) {
				// callback.getMessageFinished(uid);
				// }

				mMessagesQueue.remove(uid);
				if (!account.isSaveCopy() && message.getDownFalg() == C35Message.DOWNLOAD_FLAG_FULL) {
					deleteMessageOnServer(remoteStore, uid);
				}
			} catch (Exception e) {
				Debug.e("failfast", "failfast_AA", e);
				// if (callback != null) {
				// callback.getMessageFailed(uid);
				// }
				mMessagesQueue.remove(uid);
				throw new MessagingException("sync error", e);
			}
		}

		// 循环下载所有需要下载的附件
		if (attachmentsToDownload != null && attachmentsToDownload.size() > 0) {
			Debug.v(TAG, "下载所有小于2M的附件");
			for (C35Attachment att : attachmentsToDownload) {

				try {
					Debug.v(TAG, "do down load message attachment attachment name=" + att.getFileName());
					this.downloadAtt(folderId, account, att, false, new AttDownLoadCallback() {

						@Override
						public void downloadStarted(C35Attachment att) {

						}

						@Override
						public void updateProgress(C35Attachment att, int progress) {

						}

						@Override
						public void downloadStoped(C35Attachment att) {

						}

						@Override
						public void downloadFinished(C35Attachment att, Account account) {
							Debug.v(TAG, "download attachment Finish  name=" + att.getFileName());
						}

						@Override
						public void downloadFailed(C35Attachment att, MessagingException e) {
							Debug.e(TAG, "download attachment Failed ");
						}
					});
					// Thread.sleep(500);//是否添加延迟，防止内存溢出??
				} catch (Exception e) {
					Debug.w("failfast", "failfast_AA", e);
				}

			}

		}
	}

	/**
	 * 
	 * @Description:下载一封邮件的邮件体
	 * @param account
	 * @param folderId
	 * @param uid
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-12-5
	 */
	private void getMailByIdFromServer(Account account, String folderId, String uid) throws MessagingException {
		C35Store remoteStore = (C35Store) Store.getInstance(account.getStoreUri());
		LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
		Debug.v(TAG, "下载邮件");
		MessageCallback callback = priorityListeners.get(uid);
		try {
			remoteStore.openAndGetTicket();
			C35Message message = remoteStore.getMailById(uid, 0, 0, GlobalConstants.GZIP_BASE64, 4);//imcore
			// 用新的加密解密方式获取getmailbyid 由于服务器没有部署 先注释掉
			// C35Message message = remoteStore.getMailById(uid, 0, 0, GlobalConstants.GZIP_BASE64, 4);
			// if (setRead == 1) {
			// message.setRead(setRead);
			// setRead = 0;
			// }
			message.setDownFalg((message.getAttachSize() == 0 && message.getAttachs().size() == 0) ? C35Message.DOWNLOAD_FLAG_FULL : C35Message.DOWNLOAD_FLAG_PARTIAL);
			localStore.saveMessages(message, folderId, account);
			if (message.getAttachs() != null) {
				for (C35Attachment att : message.getAttachs()) {
					localStore.storeAttachment(account, att);
				}
				// localStore.storeCompressItems(account, message);
				localStore.storeCompressItemsByC35CompressItems(message.getCompressItems());
				if (ReceiveMessageModeUtil.getReceiveMode(account)) {
					// 下载2M以内的附件
					for (C35Attachment att : message.getAttachs()) {// todo 附件应该在正文下载并显示之后再下载，以免阻塞显示体验
						if (att.getFileSize() <= GlobalConstants.AUTODOWN_LOAD_ATT_SIZE) {
							Debug.v(TAG, "try to down load message attachment mesage Sub=" + message.getSubject() + " attachment name=" + att.getFileName());
							this.downloadAtt(folderId, account, att, false, new AttDownLoadCallback() {

								@Override
								public void updateProgress(C35Attachment att, int progress) {
								}

								@Override
								public void downloadStoped(C35Attachment att) {
								}

								@Override
								public void downloadStarted(C35Attachment att) {
								}

								@Override
								public void downloadFinished(C35Attachment att, Account account) {
									Debug.v(TAG, "download attachment Finish  name=" + att.getFileName());
								}

								@Override
								public void downloadFailed(C35Attachment att, MessagingException e) {
									Debug.e(TAG, "download attachment Failed ");
								}
							});
						}

					}
				}

			}
			if (callback != null) {
				callback.getMessageFinished(uid);
			}

			if (!account.isSaveCopy() && message.getDownFalg() == C35Message.DOWNLOAD_FLAG_FULL) {
				deleteMessageOnServer(remoteStore, uid);
			}
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
			if (callback != null) {
				callback.getMessageFailed(uid);
			}
			throw e;
		} catch (Exception e) {
			throw new MessagingException(MessagingException.PROGRAM_RUNING_ERROR, "program runing error");
		}

	}

	private void deleteMessageOnServer(C35Store remoteStore, String uid) throws MessagingException {
		List<String> uids = new ArrayList<String>();
		uids.add(uid);
		remoteStore.deleteMail(uids, false);
	}

	public Date getLastUpdate(Account account, String forderId) {
		Date date = null;
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			LocalFolder folder = localStore.getFolder(account, forderId);
			folder.open(OpenMode.READ_WRITE);
			date = new Date(folder.getLastUpdate());
		} catch (Exception e) {// android.database.sqlite.SQLiteException: no such table: folders
			Debug.e("failfast", "failfast_AA", e);
		}
		return date;
	}

	/**
	 * 描述 下载某一个附件的方法
	 * 
	 * @Description:
	 * @param folder
	 * @param account
	 * @param attachmentInfo
	 * @param fromAttachmentList
	 * @param callback
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-13
	 */
	public void downloadAtt(String folder, Account account, C35Attachment attachmentInfo, boolean fromAttachmentList, AttDownLoadCallback callback) {
		callback.downloadStarted(attachmentInfo);// 开始下载啦，

		C35Attachment attachment = null;
		try {
			LocalStore mLocalStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			C35Store mRemoteStore = (C35Store) Store.getInstance(account.getStoreUri());

			String path = new URI(account.getLocalStoreUri()).getPath() + "_att" + "/";
			if (EmailApplication.MAILBOX_OUTBOX.equals(folder) || EmailApplication.MAILBOX_SENTBOX.equals(folder)) {
				attachment = new C35Attachment();

				attachment.setCid(attachmentInfo.getCid());
				attachment.setContent_uri(attachmentInfo.getContent_uri());
				attachment.setContentType(attachmentInfo.getContentType());
				attachment.setDownState(attachmentInfo.getDownState());
				attachment.setFileName(attachmentInfo.getFileName());
				attachment.setFileSize(attachmentInfo.getFileSize());
				attachment.setId(attachmentInfo.getSourceAttachmentId());
				attachment.setIsIcon(attachmentInfo.getIsIcon());
				attachment.setMailId(attachmentInfo.getSourceMessageUid());
				attachment.setPath(attachmentInfo.getPath());
				attachment.setsID(attachmentInfo.getsID());
				attachment.setType(attachmentInfo.getType());
			}

			if (attachment != null) {
				mRemoteStore.downloadData(attachment, path, callback, account.getUpdownloadport());
			} else {
				mRemoteStore.downloadData(attachmentInfo, path, callback, account.getUpdownloadport());
			}

			attachmentInfo.setDownState(C35Attachment.DOWNLOADED);
			if (attachmentInfo.isCompress()) {
				mLocalStore.updatecompressitemDownloadStatus(attachmentInfo.getsID());
			} else {
				// 如果是从附件列表页面进行下载的，则不不需要更新 attachments表, added by zhuanggy
				if (!fromAttachmentList) {
					mLocalStore.storeAttachment(account, attachmentInfo);
				}
			}
			callback.downloadFinished(attachmentInfo, account);// 下载成功了

		} catch (MessagingException e) {
			Debug.w("c35", "downloadAtt", e);
			if (e.getExceptionType() == MessagingException.STOP_LOAD_ATT) {// 下载取消啦，
				callback.downloadStoped(attachmentInfo);
				GlobalVariable.isCancelDownload = false;
			} else {
				if (GlobalVariable.isCancelDownload) {
					GlobalVariable.isCancelDownload = false;
				}
				callback.downloadFailed(attachmentInfo, e);// 下载失败啦，
			}
		} catch (Exception e) {// URISyntaxException
			Debug.w("failfast", "failfast_AA", e);
			MessagingException me = new MessagingException(MessagingException.DOWNLOAD_ATTACHMENT_ERROR_MESSAGE, e.getMessage());
			callback.downloadFailed(attachmentInfo, me);// 下载失败啦，
		}
	}

	/**
	 * 为撰写邮件界面结束后返回邮件列表页时，实现刷新。
	 * 
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-31
	 */
	public void synMCAutoRefreshMessageListFromMessageCompose() {
		synchronized (mListeners) {
			for (MessagingListener l : mListeners) {
				l.synAutoRefreshMessageListFromMessageCompose();
			}
		}
	}

	/**
	 * 
	 * @Description:提交本地一批邮件的状态（同步调用接口，放到队列里,离开页面提交）
	 * @param account
	 *            账号
	 * @param listener
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-22
	 */
	public void commitMailsStatus(final Account account, MessagingListener listener) {

		putCmd("commitMailsStatus " + account.getEmail(), listener, new Runnable() {

			@Override
			public void run() {
				commitMailsStatus(account);
			}
		});

	}

	/**
	 * 
	 * @Description:修改会议状态
	 * @param accountz
	 *            账号
	 * @param mailId
	 *            邮件uid
	 * @param state
	 *            会议状态
	 * @param listener
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2013-1-17
	 */
	public void updateCalendarState(final Account account, final String mailId, final int state, MessagingListener listener) {
		putCmd("updateCalendarState " + account.getEmail() + " " + mailId + " " + state, listener, new Runnable() {

			@Override
			public void run() {
				updateCalendarState(account, mailId, state);
			}
		});

	}

	/**
	 * 
	 * @Description:修改会议状态
	 * @param accountz
	 *            账号
	 * @param mailId
	 *            邮件uid
	 * @param state
	 *            会议状态
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2013-1-17
	 */
	private void updateCalendarState(Account account, String mailId, int state) {
		try {
			C35Store remoteStore = (C35Store) Store.getInstance(account.getStoreUri());
			remoteStore.openAndGetTicket();
			UpdateCalendarStateResponse response = remoteStore.updateCalendarState(mailId, state);
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			localStore.setCalendarState(account, mailId, String.valueOf(state));
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
			processCommandFailed(account, null, e);
			// Toast.makeText(EmailApplication.getInstance(),
			// "getMailByIdFromServer failed :\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * 
	 * @Description:提交本地一批邮件的状态（同步调用接口）
	 * @param account
	 *            账号
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-22
	 */
	private void commitMailsStatus(Account account) {
		synchronized (mCommitMailsStatusLocker) {
			this.saveOperationHistoryCache(account);// 提交状态前先存库，避免再次刷下刚刚删除的邮件
			List<FolderObj> folderObjs = new ArrayList<FolderObj>();
			List<MailObj> mailObjs = new ArrayList<MailObj>();
			CommitMailsStatusResponse response = null;
			// this.saveOperationHistoryCache(account);
			try {
				C35Store remoteStore = (C35Store) Store.getInstance(account.getStoreUri());
				remoteStore.openAndGetTicket();
				LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());

				List<String> folderidList = localStore.getFolderIdFromHistory(account.getUuid());
				if (folderidList != null && !folderidList.isEmpty()) {
					for (String folderId : folderidList) {
						FolderObj fo = new FolderObj();
						fo.setFolderId(folderId);
						fo.setMailObjs(mailObjs);
						folderObjs.add(fo);
						List<String> mailidList = localStore.getOpHistoryMailidFromLocal(account, folderId);
						if (mailidList != null && !mailidList.isEmpty()) {
							for (String mailid : mailidList) {
								MailObj mo = new MailObj();
								mo.setMailId(mailid);
								List<StatusObj> statusObjs = localStore.getCommitMailsStatusFromLocal(account, folderId, mailid);
								mo.setStatusObjs(statusObjs);
								mailObjs.add(mo);
							}

						}
					}
					response = remoteStore.commitMailsStatus(folderObjs);// 提交状态
					localStore.updateCommitstatus(account.getUuid());// 更新为正在提交
					this.updateOperation_historyByResultFromServer(account, response);// 更新操作履历表
				}

			} catch (MessagingException e) {
				Debug.e("failfast", "failfast_AA", e);
				processCommandFailed(account, null, e);
			}
		}
	}

	/**
	 * 
	 * @Description:根据 commitMailsStatus返回结果更新Operation_history（同步调用接口）
	 * @param response
	 * @param account
	 *            注意必须与提交时的账号一致
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-25
	 */
	private void updateOperation_historyByResultFromServer(Account account, CommitMailsStatusResponse response) {
		LocalStore localStore;
		try {
			localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			if (response != null) {
				if (response.getStatus() == 0) {// 全部成功
					localStore.deletecommittingOperation_history(1);// 刚刚提交成功的履历进行清空
				} else {// 全部失败或者部分别失败
					List<ErrorObj> errorObjs = response.getErrorObjs();
					localStore.updateOperationHistoryForFailureFromServer(errorObjs, account.getUuid());
				}

				if (response.getStatus() == 1) {// 部分失败
					// 注意：先更新后删除，不过应该不会出现这种情况
					localStore.deletecommittingOperation_history(1);
				}
			} else {
				localStore.updateCommitstatusForFailed(account.getUuid());// 更新为提交失败
			}

		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}

	}

	/**
	 * 取得本地一批邮件的最新状态（同步调用接口,调用者给数据）
	 * 
	 * @param account
	 * @param folderId
	 * @param mailIds
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @throws MessagingException
	 * @date:2012-10-23
	 */
	private GetMailsStatusResponse getMailsStatus(Account account, String folderId, List<String> mailIds, List<C35Message> visibleMessages) throws Exception {
		synchronized (mGetMailsStatusLocker) {
			GetMailsStatusResponse response = null;
			if (account.isAutoSync()) {
				C35Store remoteStore = (C35Store) Store.getInstance(account.getStoreUri());
				remoteStore.openAndGetTicket();
				response = remoteStore.getMailsStatus(folderId, mailIds);
				this.updateMessagesByResultFromServer(account, mailIds, response, visibleMessages);// 更新messages
				this.getMailsStatusFinish(response);
			}
			return response;
		}
	}

	private void getMailsStatusFinish(GetMailsStatusResponse response) {
		synchronized (mListeners) {
			for (MessagingListener l : mListeners) {
				l.getMailsStatusFinish(response);
			}
		}
	}

	/**
	 * 
	 * @Description:把操作履历缓存存储到数据库
	 * @param account
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-30
	 */
	public void saveOperationHistoryCache(Account account) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			// Debug.d("QUEUE", "operationHistoryInfoCache = " + operationHistoryInfoCache.size());
			localStore.saveOperationHistory(operationHistoryInfoCache);
			// Debug.d(TAG,operationHistoryInfoCache.keySet().toString());//此处报错：java.util.ConcurrentModificationException，暂时注释
			synchronized (operationHistoryInfoCache) {
				operationHistoryInfoCache.clear();
			}
		} catch (Exception e) {// no transaction pending at
								// android.database.sqlite.SQLiteDatabase.endTransaction
			Debug.e("failfast", "failfast_AA", e);
		}

	}

	/**
	 * 预览接口
	 * 
	 * @param account
	 *            账号
	 * @param mailId
	 *            邮件uid
	 * @param attachId
	 *            附件uid
	 * @param cid
	 *            可空,当为内嵌资源时，有cid
	 * @param compressFileName
	 *            可空,有值时，表示是压缩附件中的文档格式：附件后缀:压缩文档的文件名。如：zip:/a/aaa.txt
	 * @return 预览url
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-10-30
	 */
	public String fileViewByHtml(String mailId, String attachId, String cid, String compressFileName) {
		String response = null;
		try {
			C35Store remoteStore = (C35Store) Store.getInstance(EmailApplication.getCurrentAccount().getStoreUri());
			remoteStore.openAndGetTicket();
			response = remoteStore.fileViewByHtml(mailId, attachId, cid, compressFileName);
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
			response = "faild:" + e.getMessage();
		}
		return response;
	}

	/**
	 * 获取联系人用
	 * 
	 * @param account
	 * @param type
	 *            //1 最近联系人//2 常用联系人//3 重要联系人（新增）
	 * @param size
	 *            最大返回的联系人数量
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2013-5-30
	 */
	public List<ContactAttribute> getCommonContacts(Account account, int type) {
		List<ContactAttribute> contacts = new ArrayList<ContactAttribute>();
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			contacts = localStore.getCommonContacts(account, type);
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		return contacts;
	}

	/**
	 * 根据结果更新messages表
	 * 
	 * @param account
	 * @param response
	 * @param srcMailIds
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-5
	 */
	private void updateMessagesByResultFromServer(Account account, List<String> srcMailIds, GetMailsStatusResponse response, List<C35Message> visibleMessages) throws Exception {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			if (response != null) {
				List<MailStatusObj> mailStatusObjList = response.getMailStatusObjs();
				localStore.updateMessagesByResultFromServer(account, srcMailIds, mailStatusObjList, visibleMessages, response.getFolderId());
			}

		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
			throw e;
		}

	}

	/**
	 * 获得当前邮件列表中显示的邮件的uids。如果没有,查询数据库最近20条信息，如果有则返回由MessageList传递的邮件uids
	 * 
	 * @param account
	 *            账户
	 * @param folderId
	 *            folders表中的folderId字段
	 * @param mailIds
	 *            本地可见的mailIds
	 * @param limit
	 *            收取邮件封数限制
	 * @return
	 * @see:
	 * @since:
	 * @author:
	 * @date:2012-11-7
	 */
	private List<String> prepareVisibleUidsForGetMailsStatusFunc(Account account, String folderId, List<String> mailIds, int limit) throws Exception {
		List<String> result = new ArrayList<String>();
		if (mailIds == null) {
			try {
				LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
				result = localStore.getRecentMessageUidsBy(account, folderId, limit);
			} catch (Exception e) {
				throw e;
			}
			return result;
		} else {
			Iterator<String> i = mailIds.iterator();// 循环删除，要注意
			while (i.hasNext()) {
				String uid = i.next();
				if (uid != null && uid.startsWith("Local")) {// 本地邮件不需要同步
					i.remove();
				}
			}
			return mailIds;
		}
	}

	public List<C35Message> getMessgesByMessageUidAndAccount(List<String> uids, Account account) {
		if (uids != null && uids.size() > 0) {
			try {
				LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
				List<C35Message> resultMessages = localStore.getMessagesByAccountAndUids(account, uids);
				return resultMessages;
			} catch (Exception e) {
				Debug.e("failfast", "failfast_AA", e);
			}
		}
		return null;
	}

	/**
	 * 同步已发送邮件箱
	 * 
	 * @param account
	 * @param listener
	 * @param refUid
	 * @param newer
	 * @param visibleUids
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-15
	 */
	public boolean synchronizeSentBox(final Account account, MessagingListener listener, final String refUid, final boolean newer, final List<String> visibleUids, final List<C35Message> visibleMessages, boolean bFromUI) {
		// TODO:sofia2.0环境不支持此接口。
		// 郭家龙(9136983) 10:44:13客户端没有做同步而已，服务端是支持的true;//
		Boolean result = AccountUtil.isSupportRequest("partOfCommitSyn", account);
		if (result == null || result == false) {
			// 同步收藏夹下拉刷新操作完成操作。
			synchronizedMailboxFinished(account, EmailApplication.MAILBOX_SENTBOX, 0);
			// 提示2.0不支持此命令
			if (bFromUI) {
				if ("1".equals(account.getDomainType())) {
				    Log.e(TAG, EmailApplication.getInstance().getString(R.string.sofia_environment_thirdparty_toast));
				} else {
				    Log.e(TAG, EmailApplication.getInstance().getString(R.string.sofia_environment_as_toast));
				}
			}
			return false;
		}
		boolean bReturn;
		bReturn = putCmd(CMD_SYNC_MAILBOX + " " + account.getEmail() + " " + EmailApplication.MAILBOX_SENTBOX, C35MailThreadPool.ENUM_Thread_Level.TL_AtOnce, listener, new Runnable() {

			@Override
			public void run() {
			    // Modified by LL
				//MessageList.mHandler.sendEmptyMessage(MessageList.ListHandler.SYNC_START_REFRESH);
			    
				clearSentMessages(account, EmailApplication.MAILBOX_SENTBOX);
				syncMailBox(account, EmailApplication.MAILBOX_SENTBOX, refUid, newer, visibleUids, visibleMessages, null, 0);
			}
		});

		return bReturn;
	}

	/**
	 * 同步收藏夹
	 * 
	 * @param account
	 * @param listener
	 * @param refUid
	 * @param newer
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-15
	 */
	public boolean synchronizeFavoriteBox(final Account account, MessagingListener listener, final String refUid, final boolean newer, final List<String> visibleUids, final List<C35Message> visibleMessages, final String searchKey, final int pageNo, boolean bFromUI) {
		// TODO:sofia2.0环境不支持此接口。
		Boolean result = AccountUtil.isSupportRequest("searchIdsByType", account);
		if (result == null || result == false) {
			// 同步收藏夹下拉刷新操作完成操作。
			synchronizedMailboxFinished(account, EmailApplication.MAILBOX_FAVORITEBOX, 0);
			// 提示2.0不支持此命令
			if (bFromUI) {
				if ("1".equals(account.getDomainType())) {
				    Log.e(TAG, EmailApplication.getInstance().getString(R.string.sofia_environment_thirdparty_toast));
				} else {
				    Log.e(TAG, EmailApplication.getInstance().getString(R.string.sofia_environment_as_toast));
				}
			}
			return false;
		}
		boolean bReturn;
		bReturn = putCmd(CMD_SYNC_MAILBOX + " " + account.getEmail() + " " + EmailApplication.MAILBOX_FAVORITEBOX, C35MailThreadPool.ENUM_Thread_Level.TL_AtOnce, listener, new Runnable() {

			@Override
			public void run() {
			    // Modified by LL
				//MessageList.mHandler.sendEmptyMessage(MessageList.ListHandler.SYNC_START_REFRESH);

				syncMailBox(account, EmailApplication.MAILBOX_FAVORITEBOX, refUid, newer, visibleUids, visibleMessages, searchKey, pageNo);
			}
		});

		return bReturn;
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
	private void clearSentMessages(Account account, String folderId) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			localStore.clearSentMessages(account, folderId);
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}

	}

	/******* 附件列表 **/

	/**
	 * 附件列表同步
	 * 
	 * @Description:
	 * @param account
	 * @param listener
	 * @param pageSize
	 * @param pageNo
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-10
	 */
	public void synchronizeAttachmentList(final Account account, MessagingListener listener, final int pageSize, final int pageNo) {

		putCmd("searchAttachList " + account.getEmail(), C35MailThreadPool.ENUM_Thread_Level.TL_AtOnce, listener, new Runnable() {

			@Override
			public void run() {
				synchronizeAttachmentList(account, pageSize, pageNo);
			}
		});
	}

	/**
	 * 被 {@link #synchronizeAttachmentList(Account, MessagingListener, int, int)} 调用，附件列表的同步
	 * 
	 * @Description:
	 * @param account
	 * @param pageSize
	 * @param pageNo
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-10
	 */
	private void synchronizeAttachmentList(Account account, int pageSize, int pageNo) {
		C35Store remoteStore;
		try {
			remoteStore = (C35Store) Store.getInstance(account.getStoreUri());
			remoteStore.openAndGetTicket();

			GetAttachmentListResponse response = remoteStore.getAttachmentListResponse(account, pageSize, pageNo);
			if (response != null) {
				LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());

				int accountId = (int) localStore.getAccountIdByUuid(account.getUuid());

				// 邮件夹更新时间
				LocalFolder localFolder = localStore.getFolder(account, EmailApplication.MAILBOX_ATTACHMENTBOX);// 此处传入folderId
				localFolder.open(OpenMode.READ_WRITE);
				localFolder.setLastUpdate(System.currentTimeMillis());

				List<C35Attachment> arrayAttachments = response.getAttachments();// 从服务器返回的最新数据
				// 更新数据库，本地id已经存在的不变，不存在的保存； 本地存在而返回的数据中不存在的删除；
				if (arrayAttachments != null && arrayAttachments.size() > 0) {
					// 1、删除本地存在而服务器返回数据中不存在的数据
					// 1.1过滤出服务器返回数据中的ids
					ArrayList<String> serverIds = new ArrayList<String>();
					for (C35Attachment att : arrayAttachments) {
						serverIds.add(att.getId());
					}
					// 1.2读取数据库中的id
					String[] localIds = localStore.getAttachmentsIdsFromAttachmentsListDB(accountId);
					for (String id : localIds) {
						if (!serverIds.contains(id)) {
							// 本地有，而服务器没有删除
							localStore.deleteAttachmentFromAttachmentsListTableById(id);
						} else {
							// 本地有，且服务器有:考虑过滤掉重复的，在 2 的存储时提高效率
						}
					}
					// 2、保存本地不存在的数据
					localStore.updateAttachmentsList(accountId, arrayAttachments);
				} else {
					// 返回为空，进一步判断 totalCount是否为0，若为0说明服务器没有邮件了，清空本地附件
					if (response.getTotalCount() == 0) {
						int accountid = (int) localStore.getAccountIdByUuid(account.getUuid());
						localStore.deleteAttachmentFromAttachmentsListTableByAccountId(accountid);
					}
				}

				this.syncAttachmentsListFinished();
			} else {
				this.syncMCAttachmentsListFailed();
			}

			// 勿删，待服务器查询附件id接口ok后使用
			// GetAttachmentListIdsResponse response = remoteStore.syncAttachmentListResponse(account,
			// pageSize, pageNo);
			// if (response != null) {
			// // 从服务器获得附件列表，存储数据库
			// LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri(),
			// mApplication);
			//
			// ArrayList<String> arrayAttachmentsIds = response.getAttachmentsIds();
			//
			// if (arrayAttachmentsIds != null) {
			// // 读取数据库，查询所有附件id
			// String[] localIds =
			// localStore.getAttachmentsIdsFromAttachmentsListDB(account.getAccountNumber());
			// if (localIds != null) {
			// for (String id : localIds) {
			// if (!arrayAttachmentsIds.contains(id)) {
			// // 如果数据库中的附件不在获取的附件列表中，则删除
			// localStore.deleteAttachmentFromAttachmentsListDB(id);
			// }
			// }
			// }
			// }
			// // 通知更新
			// this.getAttachmentListIdsFinish(arrayAttachmentsIds);
			// } else {
			// this.getAttachmentListIdsFailed();
			// }

		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
			this.syncMCAttachmentsListFailed();
		}
	}

	/**
	 * 同步附件附件成功
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-12-6
	 */
	private void syncAttachmentsListFinished() {
		synchronized (mListeners) {
			for (MessagingListener l : mListeners) {
				l.syncAttachmentListFinished();
			}
		}
	}

	private void syncMCAttachmentsListFailed() {
		synchronized (mListeners) {
			for (MessagingListener l : mListeners) {
				l.syncAttachmentListFailed();
			}
		}
	}

	/**
	 * 获取附件列表
	 * 
	 * @Description:
	 * @param account
	 * @param listener
	 * @param pageSize
	 * @param pageNo
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-10
	 */
	public void loadAttachmentList(final Account account, MessagingListener listener, final int pageSize, final int pageNo) {

		putCmd("searchAttachList " + account.getEmail(), C35MailThreadPool.ENUM_Thread_Level.TL_AtOnce, listener, new Runnable() {

			@Override
			public void run() {
				getAttachmentListFromServer(account, pageSize, pageNo);
			}
		});
	}

	/**
	 * 被 {@link #loadAttachmentList(Account, MessagingListener, int, int)} 调用，获取附件列表
	 * 
	 * @Description:
	 * @param account
	 * @param pageSize
	 * @param pageNo
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-10
	 */
	private void getAttachmentListFromServer(Account account, int pageSize, int pageNo) {
		C35Store remoteStore;
		try {
			remoteStore = (C35Store) Store.getInstance(account.getStoreUri());
			remoteStore.openAndGetTicket();
			GetAttachmentListResponse response = remoteStore.getAttachmentListResponse(account, pageSize, pageNo);
			if (response != null) {
				// 从服务器获得附件列表，存储数据库
				LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
				int accountid = (int) localStore.getAccountIdByUuid(account.getUuid());
				// 邮件夹更新时间
				LocalFolder localFolder = localStore.getFolder(account, EmailApplication.MAILBOX_ATTACHMENTBOX);// 此处传入folderId
				localFolder.open(OpenMode.READ_WRITE);
				localFolder.setLastUpdate(System.currentTimeMillis());

				List<C35Attachment> arrayAttachments = new ArrayList<C35Attachment>();
				arrayAttachments = response.getAttachments();

				// 如果是从服务器获得的列表，查询本地数据库判断附件下载状态
				for (C35Attachment attachment : arrayAttachments) {
					attachment.setDownState(localStore.getAttachmentDownloadState(attachment.getId(), accountid));
					// attachment.setDownState(getAttachmentDownloadState(account, attachment.getId()));
				}
				localStore.updateAttachmentsList(accountid, arrayAttachments);

				// 通知更新
				this.getMCAttachmentListFinish(arrayAttachments, response.getTotalCount());

				// TODO : 加上判断，因为是读取最新的操作，防止断档，
				// 判断获取的附件的最小的时间（最旧的附件）是否大于当前本地附件列表中最大的附件时间（最新的附件）
				// 若是，则继续加载第二页，向此方法传值getAttachmentListFailed；
				// 若否，则没有断档，不需继续加载

			} else {
				this.getAttachmentListFailed();
			}

		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
			this.getAttachmentListFailed();
		}
	}

	/**
	 * 获得附件列表完成时的回调
	 * 
	 * @Description:
	 * @param attachments
	 * @param allAttachments
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-12-6
	 */
	private void getMCAttachmentListFinish(List<C35Attachment> attachments, int allAttachments) {
		synchronized (mListeners) {
			for (MessagingListener l : mListeners) {
				l.getAttachmentListFinish(attachments, allAttachments);
			}
		}
	}

	/**
	 * 获取附件列表失败
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-12-6
	 */
	private void getAttachmentListFailed() {
		synchronized (mListeners) {
			for (MessagingListener l : mListeners) {
				l.getAttachmentListFailed();
			}
		}
	}

	/** 附件列表相关的操作数据库 ***/
	/**
	 * 更新附件的下载状态
	 * 
	 * @Description:
	 * @param account
	 * @param attachment
	 * @param state
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-7
	 */
	public void updateMCAttachmentDownloadState(Account account, C35Attachment attachment, int state) {
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			localStore.updateAttachmentDownloadState(attachment, state);
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 读取数据库获得附件列表
	 * 
	 * @Description:
	 * @param account
	 * @param accountid
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-7
	 */
	public ArrayList<C35Attachment> getAttachmentsFromAttachmentsListDB(Account account, String accountUid) {
		ArrayList<C35Attachment> attachmentList = new ArrayList<C35Attachment>();
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			int accountid = (int) localStore.getAccountIdByUuid(account.getUuid());
			attachmentList = localStore.getAttachmentsFromAttachmentsListDB(accountid);
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		return attachmentList;
	}

	/**
	 * 清空邮件下载队列
	 * 
	 * @see:
	 * @since:
	 * @author: hanlx
	 * @date:2013-2-28
	 */
	public void clearMessagesQueue() {
		if (!mMessagesQueue.isEmpty()) {
			mMessagesQueue.clear();
		}
	}

	/**
	 * 清空Cmd队列
	 * 
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-10-15
	 */
	public void clearCmdQueue() {
		if (!mCommands.isEmpty()) {
			mCommands.clear();
		}
	}

	/**
	 * 下载该账户下的未完全下载的邮件
	 * 
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-10-14
	 */
	public void getUnDownloadMails(Account account) {
		// 先清空队列，防止非当前账户id
		mMessagesQueue.clear();
		LocalStore localStore;
		List<String> mailList = new ArrayList<String>();
		try {
			localStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
			mailList = localStore.selectUnDownLoadMail(account);
			for (String uid : mailList) {
				if (!mMessagesQueue.contains(uid))
					mMessagesQueue.offerFirst(uid);
			}
			// 这里folder统一设置为收件箱，其实是不合理的，但是保存的时候并没有用到，所以用收件箱来代替
			getMailsFromServerInQueue(account, EmailApplication.MAILBOX_INBOX, true);
		} catch (MessagingException e) {
			Debug.w(TAG, "failfast_AA", e);
		} catch (Exception e) {
			Debug.w(TAG, "failfast_AA", e);
		}

	}
}
