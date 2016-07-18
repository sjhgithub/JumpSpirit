package com.c35.mtd.pushmail.command.response;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.util.JsonUtil;

/**
 * 从服务器获取附件列表ID的响应，用于附件同步
 * 
 * @Description:
 * @author: zhuanggy
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-1-10
 */

public class GetAttachmentListIdsResponse extends BaseResponse {

	private ArrayList<String> attachmentsIds;// 附件列表的id
	private int tootalCount = -1;

	public int getTootalCount() {
		return tootalCount;
	}

	public ArrayList<String> getAttachmentsIds() {
		Debug.v("", "GetAttachmentListIdsResponse-----getAttachmentsIds");
		return attachmentsIds;
	}

	// public void setAttachments(List<C35Attachment> attachments) {
	// this.attachments = attachments;
	// }

	@Override
	public void initFeild(String response) throws MessagingException {
		super.initFeild(response);

		Debug.i("GetAttachmentListResponse", "response===" + commandMessage.toString());
		attachmentsIds = new ArrayList<String>();
		try {
			JSONObject obj = new JSONObject(commandMessage);
			// 解析response，获得附件总数
			tootalCount = obj.getInt("totalCount");

			// 解析response，获得附件列表
			JSONArray arr = obj.getJSONArray("attachs");
			for (int i = 0; i < arr.length(); i++) {
				JSONObject attachment = arr.getJSONObject(i);
				attachmentsIds.add(JsonUtil.parseAttachmentInfoId(attachment));
			}
		} catch (JSONException e) {
			Debug.w("C35", "JsonExp", e);
			throw new MessagingException(MessagingException.RETURN_COMMAND_ERROR_GETATTACHMENTLISTIDS, e.getMessage());
		}

		Debug.v("", "GetAttachmentListIdsResponse-----initFeild");
		Debug.v("", "GetAttachmentListIdsResponse-----attachmentsIds size= " + attachmentsIds.size());
	}
}
