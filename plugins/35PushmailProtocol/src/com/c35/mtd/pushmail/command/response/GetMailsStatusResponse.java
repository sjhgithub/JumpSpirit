package com.c35.mtd.pushmail.command.response;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.beans.MailStatusObj;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.util.JsonUtil;

/**
 * @Description:GetMailsStatus命令响应
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-10-18
 */
public class GetMailsStatusResponse extends BaseResponse {

	private String user;// 账号
	private List<MailStatusObj> mailStatusObjs;// 邮件状态对象
	private String folderId;// 邮件所在文件夹

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public List<MailStatusObj> getMailStatusObjs() {
		return mailStatusObjs;
	}

	public void setMailStatusObjs(List<MailStatusObj> mailStatusObjs) {
		this.mailStatusObjs = mailStatusObjs;
	}

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	@Override
	public void initFeild(String response) throws MessagingException {
		super.initFeild(response);
		try {
			JSONObject jsonObject = new JSONObject(commandMessage);
			this.setUser(jsonObject.getString("user"));
			this.setFolderId(jsonObject.getString("folderId"));
			this.setMailStatusObjs(JsonUtil.parseMailStatusObjs(jsonObject));
		} catch (JSONException e) {
			Debug.w("C35", "JsonExp", e);
			throw new MessagingException(MessagingException.RETURN_COMMAND_ERROR_GETMAILSSTATUS,e.getMessage());
		}

	}

}
