package com.c35.mtd.pushmail.command.response;

import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.beans.C35Folder;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.util.JsonUtil;

/**
 * 
 * @Description:GetFolderList命令响应
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class GetFolderListResponse extends BaseResponse {

	private static final String TAG = "GetFolderListResponse";
	private GetFolderType type;

	public enum GetFolderType {
		// ** 默认值：拿所有箱子 *//*
		DEFAULT,
		// ** 拿自定义的箱子 *//*
		SELFDEFINED,
		// ** 拿归档的箱子 *//*
		ARCHIVEFOLDER
	}

	ArrayList<C35Folder> folders = null;

	private static final int Default = 0, Archive = 2, SelfDefined = 1, Other = 3;

	public ArrayList<C35Folder> getFolders() {
		return folders;
	}

	public void setFolders(ArrayList<C35Folder> folders) {
		this.folders = folders;
	}

	public GetFolderType getType() {
		return type;
	}

	public void setType(GetFolderType type) {
		this.type = type;
	}

	@Override
	public void initFeild(String response) throws MessagingException {
		super.initFeild(response);
		Debug.v(TAG, "type = " + type);
		try {
			setFolders(JsonUtil.parseFolders(new JSONObject(commandMessage)));
		} catch (JSONException e1) {
			throw new MessagingException(MessagingException.RETURN_COMMAND_ERROR_GETFOLDERLIST, e1.getMessage());
		}
	}
}
