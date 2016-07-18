package com.c35.mtd.pushmail.command.request;

import java.util.List;

/**
 * @Description: 取得本地一批邮件的最新状态(同步用)
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-10-18
 */
public class GetMailsStatusRequest extends BaseRequest {

	private List<String> mailIds;// 邮件ID（也就是UID）
	private String folderId; // 邮件夹id

	public List<String> getMailIds() {
		return mailIds;
	}

	public void setMailIds(List<String> mailIds) {
		this.mailIds = mailIds;
	}

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

}
