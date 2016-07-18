package com.c35.mtd.pushmail.command.request;

import java.util.List;

import com.c35.mtd.pushmail.beans.FolderObj;

/**
 * @Description:提交本地一批邮件的状态(同步用)
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-10-18
 */
public class CommitMailsStatusRequest extends BaseRequest {

	private List<FolderObj> folderObjs;// 文件夹级的同步对象 列表

	public List<FolderObj> getFolderObjs() {
		return folderObjs;
	}

	public void setFolderObjs(List<FolderObj> folderObjs) {
		this.folderObjs = folderObjs;
	}

}
