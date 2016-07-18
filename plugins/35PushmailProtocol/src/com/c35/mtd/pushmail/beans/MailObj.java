package com.c35.mtd.pushmail.beans;

import java.util.List;

/**
 * @Description:邮件对象Bean
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-10-18
 */
public class MailObj {

	private String mailId; // 邮件id
	private List<StatusObj> statusObjs;// 状态对象

	public String getMailId() {
		return mailId;
	}

	public void setMailId(String mailId) {
		this.mailId = mailId;
	}

	public List<StatusObj> getStatusObjs() {
		return statusObjs;
	}

	public void setStatusObjs(List<StatusObj> statusObjs) {
		this.statusObjs = statusObjs;
	}

}
