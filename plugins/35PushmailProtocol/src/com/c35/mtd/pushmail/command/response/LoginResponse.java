package com.c35.mtd.pushmail.command.response;

import org.json.JSONException;
import org.json.JSONObject;
import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.util.StringUtil;

/**
 * 
 * @Description:Login命令响应
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class LoginResponse extends BaseResponse {

	private String ticket;
	private String protocolKey;

	public String getProtocolKey() {
		return protocolKey;
	}

	public void setProtocolKey(String protocolKey) {
		this.protocolKey = protocolKey;
	}

	public String getTicket() {
		return ticket;
	}

	private void setTicket(String ticket) {
		this.ticket = ticket;
	}

	@Override
	public void initFeild(String response) throws MessagingException {
		super.initFeild(response);
		if (StringUtil.isNotEmpty(commandMessage)) {
			Debug.v("commandMessage", commandMessage);
			try {
				JSONObject jsobj = new JSONObject(commandMessage);
				setTicket("" + jsobj.getString("ticket"));
				setProtocolKey(jsobj.optString("protocolKey"));
			} catch (JSONException e) {
				Debug.w("C35", "JsonExp", e);
				throw new MessagingException(MessagingException.RETURN_COMMAND_ERROR_LOGIN, e.getMessage());
			}
		}

	}

}
