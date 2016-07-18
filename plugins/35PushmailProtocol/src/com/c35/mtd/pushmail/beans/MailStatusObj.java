package com.c35.mtd.pushmail.beans;

import java.util.List;

/**
 * @Description:邮件状态对象Bean
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-10-18
 */
public class MailStatusObj {

	private String mailId; // 邮件ID
	private int read; // 已读标示
	private int importantFlag; // 收藏标示
	private String calendarState; // 会议状态(0=初始 1=接受 2=拒绝 3:可能)，不是会议邮件则为空

	private int deliveredReadCount;// 已读人数
	private List<String> deliveredReadUsers;// 已读的人

	public String getCalendarState() {
		return calendarState;
	}

	public void setCalendarState(String calendarState) {
		this.calendarState = calendarState;
	}

	public String getMailId() {
		return mailId;
	}

	public void setMailId(String mailId) {
		this.mailId = mailId;
	}

	public int getRead() {
		return read;
	}

	public void setRead(int read) {
		this.read = read;
	}

	public int getImportantFlag() {
		return importantFlag;
	}

	public void setImportantFlag(int importantFlag) {
		this.importantFlag = importantFlag;
	}

	public int getDeliveredReadCount() {
		return deliveredReadCount;
	}

	public void setDeliveredReadCount(int deliveredReadCount) {
		this.deliveredReadCount = deliveredReadCount;
	}

	public List<String> getDeliveredReadUsers() {
		return deliveredReadUsers;
	}

	public void setDeliveredReadUsers(List<String> deliveredReadUsers) {
		this.deliveredReadUsers = deliveredReadUsers;
	}
}
