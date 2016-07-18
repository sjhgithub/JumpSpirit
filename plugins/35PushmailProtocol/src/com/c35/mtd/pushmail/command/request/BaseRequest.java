package com.c35.mtd.pushmail.command.request;

/**
 * 
 * @Description:请求命令基类
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class BaseRequest {

	protected String user;

	// protected String requestCommand;

	// public String getRequestCommand() {
	// return requestCommand;
	// }
	//
	// public void setRequestCommand(String requestCommand) {
	// this.requestCommand = requestCommand;
	// }

	// public abstract void creatRequestCommand(String ticket,String user);

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
}
