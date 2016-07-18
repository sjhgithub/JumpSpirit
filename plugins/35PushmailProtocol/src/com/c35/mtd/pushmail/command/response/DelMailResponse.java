package com.c35.mtd.pushmail.command.response;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.exception.MessagingException;

/**
 * 
 * @Description:DelMail命令响应
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class DelMailResponse extends BaseResponse {

	@Override
	public void initFeild(String response) throws MessagingException {
		super.initFeild(response);
		Debug.d("", "response = " + response);
	}
}
