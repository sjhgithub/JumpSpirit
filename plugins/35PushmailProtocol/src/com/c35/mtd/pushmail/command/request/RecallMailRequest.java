package com.c35.mtd.pushmail.command.request;

import java.util.List;

/**
 * @Description:邮件召回命令请求
 * @see:
 * @since:
 * @author: hanlx
 * @date:2013-1-7
 */
public class RecallMailRequest extends BaseRequest {
	private String mailId;
	private List<String> recallEmails;
	
	public String getMailId() {
		return mailId;
	}
	
	public void setMailId(String mailId) {
		this.mailId = mailId;
	}
	
	public List<String> getRecallEmails() {
		return recallEmails;
	}
	
	public void setRecallEmails(List<String> recallEmails) {
		this.recallEmails = recallEmails;
	}	
	
}
