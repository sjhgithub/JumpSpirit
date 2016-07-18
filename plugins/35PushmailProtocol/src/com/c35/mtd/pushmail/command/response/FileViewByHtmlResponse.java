package com.c35.mtd.pushmail.command.response;

import org.json.JSONException;
import org.json.JSONObject;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.exception.MessagingException;

/**
 * 
 * @Description:FileViewByHtml命令响应
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class FileViewByHtmlResponse extends BaseResponse {

	// 预览地址
	private String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public void initFeild(String response) throws MessagingException {
		super.initFeild(response);
		try {
			JSONObject jsonObject = new JSONObject(commandMessage);
			this.setUrl(jsonObject.getString("url"));
		} catch (JSONException e) {
			Debug.w("C35", "JsonExp", e);
			throw new MessagingException(MessagingException.RETURN_COMMAND_ERROR_FILEVIEWBYHTML, e.getMessage());
		}

	}

}
