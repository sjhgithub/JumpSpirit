package com.c35.mtd.pushmail.command.response;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.beans.C35Attachment;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.util.JsonUtil;

/**
 * 从服务器获取附件列表响应
 * 
 * @Description:
 * @author: zhuanggy
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-1-9
 */

public class GetAttachmentListResponse extends BaseResponse {

	private ArrayList<String> attachmentIds;// 附件列表的id

	private List<C35Attachment> attachments;
	private int totalCount = -1;

	public int getTotalCount() {
		return totalCount;
	}

	public List<C35Attachment> getAttachments() {
		Debug.v("", "GetAttachmentListResponse-----getAttachments");
		return attachments;
	}

	// public void setAttachments(List<C35Attachment> attachments) {
	// this.attachments = attachments;
	// }

	@Override
	public void initFeild(String response) throws MessagingException {
		super.initFeild(response);

		Debug.i("GetAttachmentListResponse", "response===" + commandMessage.toString());
		attachments = new ArrayList<C35Attachment>();
		try {
			JSONObject obj = new JSONObject(commandMessage);
			// 解析response，获得附件总数
			totalCount = obj.getInt("totalCount");

			// 解析response，获得附件列表
			JSONArray arr = obj.getJSONArray("attachs");
			for (int i = 0; i < arr.length(); i++) {
				JSONObject attachment = arr.getJSONObject(i);
				attachments.add(JsonUtil.parseAttachmentInfo(attachment));
			}
		} catch (JSONException e) {
			Debug.w("C35", "JsonExp", e);
			throw new MessagingException(MessagingException.RETURN_COMMAND_ERROR_GETATTACHMENTLIST, e.getMessage());
		}

		Debug.v("", "GetAttachmentListResponse-----initFeild");
		Debug.v("", "GetAttachmentListResponse-----attachments size= " + attachments.size());
	}
}
