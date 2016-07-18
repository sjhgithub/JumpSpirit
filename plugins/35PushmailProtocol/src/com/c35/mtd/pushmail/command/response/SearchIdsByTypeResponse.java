package com.c35.mtd.pushmail.command.response;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.exception.MessagingException;

/**
 * 
 * @Description:SearchIdsByType命令响应
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-1-21
 */
public class SearchIdsByTypeResponse extends BaseResponse {

	private List<String> ids = null;
	private int totalCount = 0;

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	@Override
	public void initFeild(String response) throws MessagingException {
		super.initFeild(response);
		ids = new ArrayList<String>();
		try {
			JSONObject obj = new JSONObject(commandMessage);
			totalCount = obj.getInt("totalCount");
			JSONArray jsonArray = obj.getJSONArray("ids");
			for (int i = 0; i < jsonArray.length(); i++) {
				ids.add((String) jsonArray.get(i));
			}
		} catch (JSONException e) {
			Debug.w("C35", "JsonExp", e);
			throw new MessagingException(MessagingException.RETURN_COMMAND_ERROR_SEARCHIDSBYTYPE, e.getMessage());
		}
	}

}
