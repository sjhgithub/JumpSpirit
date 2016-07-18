package com.c35.mtd.pushmail.beans;

/**
 * @Description: 失败邮件对象Bean
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-10-18
 */
public class ErrorObj {

	private String folderId; // 邮件所在文件夹（必然与请求时该邮件ID所在文件夹相同）
	private String mailId; // 邮件ID
	private int errorCode; // 失败原因码（如邮件不存在，文件夹不存在等code）
	private int statusId;// 状态id

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
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

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

}
