package com.c35.mtd.pushmail.command.request;

/**
 * 
 * @Description:UpdateCalendarState命令请求
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class UpdateCalendarStateRequest extends BaseRequest {

	private String mailId;// 邮件id
	private int state;// 会议状态

	public String getMailId() {
		return mailId;
	}

	public void setMailId(String mailId) {
		this.mailId = mailId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

}
