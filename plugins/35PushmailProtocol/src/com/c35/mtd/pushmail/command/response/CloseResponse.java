package com.c35.mtd.pushmail.command.response;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.exception.MessagingException;


/**
 * 
 * @Description:Close命令响应 
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class CloseResponse extends BaseResponse {

	@Override
	public void initFeild(String response) throws MessagingException {
		super.initFeild(response);
		Debug.d("CloseResponse", "response is = " + response + "  commandMessage = " + commandMessage);
	}
	/*
	 * public CloseResponse(String response) throws Exception { super(response);
	 * Debug.d("LoginoutResponse","response = " +response); }
	 */

}
