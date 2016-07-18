package com.c35.mtd.pushmail.command.request;

/**
 * 
 * @Description:Close命令请求
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class CloseRequest extends BaseRequest {
	
	private String ticket;

	
	public String getTicket() {
		return ticket;
	}

	
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public CloseRequest() {
	}

}
