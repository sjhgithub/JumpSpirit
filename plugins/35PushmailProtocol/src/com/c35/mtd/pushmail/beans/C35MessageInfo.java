package com.c35.mtd.pushmail.beans;

/**
 * 
 * @Description:邮件信息bean
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class C35MessageInfo {

	private String mailId; // 邮件ID
	private String sendTime; // 发送时间

	public String getMailId() {
		return mailId;
	}

	public void setMailId(String mailId) {
		this.mailId = mailId;
	}

	public String getSendTime() {
		return sendTime;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

}
