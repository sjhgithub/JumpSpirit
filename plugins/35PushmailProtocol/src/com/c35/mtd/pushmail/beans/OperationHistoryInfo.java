package com.c35.mtd.pushmail.beans;

/**
 * 
 * @Description:操作履历bean
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class OperationHistoryInfo {

	private int id; // id
	private String account_uid; // 账号
	private String folderId; // 文件夹id
	private String mailId; // 邮件uid
	private int statusId; // 状态id
	private String statusValue; // 状态值
	private long operateTime; // 操作时间
	private int commitStatus; // 提交状态

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	public String getMailId() {
		return mailId;
	}

	public void setMailId(String mailId) {
		this.mailId = mailId;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public String getStatusValue() {
		return statusValue;
	}

	public void setStatusValue(String statusValue) {
		this.statusValue = statusValue;
	}

	public long getOperateTime() {
		return operateTime;
	}

	public void setOperateTime(long operateTime) {
		this.operateTime = operateTime;
	}

	public int getCommitStatus() {
		return commitStatus;
	}

	public void setCommitStatus(int commitStatus) {
		this.commitStatus = commitStatus;
	}

	public String getAccount_uid() {
		return account_uid;
	}

	public void setAccount_uid(String account_uid) {
		this.account_uid = account_uid;
	}

}
