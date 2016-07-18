package com.c35.mtd.pushmail.command.response;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.exception.MessagingException;

/**
 * 
 * @Description:UpdateCalendarState命令响应
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-1-17
 */
public class UpdateCalendarStateResponse extends BaseResponse {

	@Override
	public void initFeild(String response) throws MessagingException {
		super.initFeild(response);
		Debug.d("", "response = " + response);
	}
}
