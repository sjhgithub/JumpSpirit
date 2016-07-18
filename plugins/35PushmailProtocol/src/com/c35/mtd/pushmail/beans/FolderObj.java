package com.c35.mtd.pushmail.beans;

import java.util.List;

/**
 * @Description: 文件夹对象Bean
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-10-18
 */
public class FolderObj {

	private String folderId; // 文件夹id
	private List<MailObj> mailObjs;// 邮件状态对象

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	public List<MailObj> getMailObjs() {
		return mailObjs;
	}

	public void setMailObjs(List<MailObj> mailObjs) {
		this.mailObjs = mailObjs;
	}

}
