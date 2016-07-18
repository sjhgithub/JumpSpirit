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
 * @Description:GetIdsById命令响应
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class GetIdsByIdResponse extends BaseResponse {

	private List<String> ids = new ArrayList<String>();
	private String TAG = "GetIdsByIdResponse";

	@Override
	public void initFeild(String response) throws MessagingException {
		super.initFeild(response);
		try {
			JSONObject jsonObj = new JSONObject(commandMessage);
			JSONArray jsonArray = jsonObj.getJSONArray("ids");
			for (int i = 0; i < jsonArray.length(); i++) {
				ids.add((String) jsonArray.get(i));
			}
		} catch (JSONException e) {
			Debug.w("C35", "JsonExp", e);
			throw new MessagingException(MessagingException.RETURN_COMMAND_ERROR_GETIDSBYID, e.getMessage());
		}
	}

	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

}
