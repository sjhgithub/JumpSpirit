package com.c35.mtd.pushmail.command.response;

import org.json.JSONException;
import org.json.JSONObject;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.util.StringUtil;

/**
 * 
 * @Description:Version命令响应
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class VersionResponse extends BaseResponse {

	/*
	 * public VersionResponse(String response) throws Exception { super(response); // TODO Auto-generated
	 * constructor stub if(StringUtil.isNotEmpty(commandMessage)){ JSONObject jsobj = new
	 * JSONObject(commandMessage); setLoginKey((String) jsobj.get("loginKey")); } }
	 */
	private String loginKey;
	private static final String TAG = "VersionResponse";

	public String getLoginKey() {
		return loginKey;
	}

	private void setLoginKey(String loginKey) {
		this.loginKey = loginKey;
	}

	@Override
	public void initFeild(String response) throws MessagingException {
		super.initFeild(response);
		if (StringUtil.isNotEmpty(commandMessage)) {
			try {
				JSONObject jsobj = new JSONObject(commandMessage);
				setLoginKey((String) jsobj.get("loginKey"));
			} catch (JSONException e) {
				Debug.w("C35", "JsonExp", e);
				throw new MessagingException(MessagingException.RETURN_COMMAND_ERROR_VERSION, e.getMessage());
			}
		}
	}

}
