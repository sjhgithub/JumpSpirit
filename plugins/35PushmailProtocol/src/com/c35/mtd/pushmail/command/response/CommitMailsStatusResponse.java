package com.c35.mtd.pushmail.command.response;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.beans.ErrorObj;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.util.JsonUtil;

/**
 * @Description:提交状态返回结果
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-10-18
 */
public class CommitMailsStatusResponse extends BaseResponse {

	private List<ErrorObj> errorObjs;// 失败邮件对象
	private int status = 0;

	/**
	 * 同步结果状态 0：全部同步成功（服务器已处理完毕） 1：部分id同步成功 -1：全部同步失败
	 */
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public List<ErrorObj> getErrorObjs() {
		return errorObjs;
	}

	public void setErrorObjs(List<ErrorObj> errorObjs) {
		this.errorObjs = errorObjs;
	}

	@Override
	public void initFeild(String response) throws MessagingException {
		super.initFeild(response);
		try {
			JSONObject jsonObject = new JSONObject(commandMessage);
			this.setErrorObjs(JsonUtil.parseErrorObjs(jsonObject));
			this.setStatus(jsonObject.getInt("status"));
		} catch (JSONException e) {
			Debug.w("C35", "JsonExp", e);
			throw new MessagingException(MessagingException.RETURN_COMMAND_ERROR_COMMITMAILSTATUS,e.getMessage());
		}

	}

}
