package com.c35.mtd.pushmail.command.response;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.exception.MessagingException;


/**
 * 
 * @Description:Logout命令响应
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class LogoutResponse extends BaseResponse {

	/*
	 * public LoginoutResponse(String response) throws Exception { super(response); // TODO Auto-generated
	 * constructor stub Debug.d("LoginoutResponse","response = " +response); }
	 */

	@Override
	public void initFeild(String response) throws MessagingException {
		super.initFeild(response);
		Debug.d("LoginoutResponse", "response is = " + response + "  commandMessage = " + commandMessage);
	}
}
