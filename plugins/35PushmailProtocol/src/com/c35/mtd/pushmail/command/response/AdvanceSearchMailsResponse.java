package com.c35.mtd.pushmail.command.response;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.beans.C35Message;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.util.JsonUtil;

/**
 * 
 * @Description:AdvanceSearchMails命令响应
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class AdvanceSearchMailsResponse extends BaseResponse {

	private List<C35Message> mails;

	public List<C35Message> getMails() {
		return mails;
	}

	public void setMails(List<C35Message> mails) {
		this.mails = mails;
	}

	@Override
	public void initFeild(String response) throws MessagingException {
		super.initFeild(response);
		mails = new ArrayList<C35Message>();
		try {
			JSONObject obj = new JSONObject(commandMessage);
			JSONArray arr = obj.getJSONArray("mails");
			for (int i = 0; i < arr.length(); i++) {
				JSONObject mail = arr.getJSONObject(i);
				C35Message msg = JsonUtil.parseMessageHead(mail);
				if (msg != null) {
					mails.add(msg);
				}
			}
		} catch (JSONException e) {
			Debug.w("C35", "JsonExp", e);
			throw new MessagingException(MessagingException.RETURN_COMMAND_ERROR_ADVANCESEARCHMAILS, e.getMessage());
		}
	}

}
