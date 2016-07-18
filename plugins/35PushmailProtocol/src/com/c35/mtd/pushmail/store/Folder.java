package com.c35.mtd.pushmail.store;

import com.c35.mtd.pushmail.exception.MessagingException;

/**
 * 邮件夹处理
 * @author:xulei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-6-2
 */
public abstract class Folder {

	static public enum OpenMode {
		READ_WRITE, READ_ONLY,
	}

	public enum FolderType {
		HOLDS_FOLDERS, HOLDS_MESSAGES,
	}

	private int unReadCount;
	private int totalMessages;

	public int getUnReadCount() {
		return unReadCount;
	}

	public void setUnReadCount(int unReadCount) {
		this.unReadCount = unReadCount;
	}

	public int getTotalMessages() {
		return totalMessages;
	}

	public void setTotalMessages(int totalMessages) {
		this.totalMessages = totalMessages;
	}

	/**
	 * @Description: Forces an open of the MailProvider. If the provider is already open this function returns
	 *               without doing anything.
	 * 
	 * @param mode
	 *            READ_ONLY or READ_WRITE
	 */
	public abstract void open(OpenMode mode) throws MessagingException;

	/**
	 * @Description: Forces a close of the MailProvider. Any further access will attempt to reopen the
	 *               MailProvider.
	 * 
	 * @param expunge
	 *            If true all deleted messages will be expunged.
	 */
	public abstract void close(boolean expunge) throws MessagingException;

	/**
	 * @return True if further commands are not expected to have to open the connection.
	 */
	// TODO not used, get rid of this - it's a transport function
	public abstract boolean isOpen();

	/**
	 * @Description:Get the mode the folder was opened with. This may be different than the mode the open was
	 *                  requested with.
	 * 
	 * @return
	 */
	public abstract OpenMode getMode() throws MessagingException;

	public abstract String getName();

	/**
	 * @Description:只在LocalFolder中使用
	 * 
	 * @return
	 */
	public long getLastUpdate() {
		return -1;
	};
}
