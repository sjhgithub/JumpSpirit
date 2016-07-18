package com.c35.mtd.pushmail.beans;

/**
 * 
 * @Description:OABean
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class OABean {

	String senderName;
	String affaisNumber;
	long date;
	String emailAddress;
	String preView;
	String importLevel;

	/**
	 * 
	 * @param importLevel
	 *            重要级别
	 * @param senderName
	 *            发起人姓名
	 * @param affaisNumber
	 *            事务编号
	 * @param l
	 * @param emailAddress
	 *            发起人EMAIL
	 * @param preView
	 *            事务内容
	 */
	public OABean(String importLevel, String senderName, String affaisNumber, long l, String emailAddress, String preView) {
		super();
		this.importLevel = importLevel;
		this.senderName = senderName;
		this.affaisNumber = affaisNumber;
		this.date = l;
		this.emailAddress = emailAddress;
		this.preView = preView;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getAffaisNumber() {
		return affaisNumber;
	}

	public void setAffaisNumber(String affaisNumber) {
		this.affaisNumber = affaisNumber;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getPreView() {
		return preView;
	}

	public void setPreView(String preView) {
		this.preView = preView;
	}

	public String getImportLevel() {
		return importLevel;
	}

	public void setImportLevel(String importLevel) {
		this.importLevel = importLevel;
	}
}
