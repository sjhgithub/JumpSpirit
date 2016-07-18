package com.c35.mtd.pushmail.command.request;

import java.util.List;

/**
 * 
 * @Description:GetMailListByMailIds命令请求
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class GetMailListByMailIdsRequest extends BaseRequest {

	List<String> mailIds;
	private int previewSize; // 预览的字数

	public int getPreviewSize() {
		return previewSize;
	}

	public void setPreviewSize(int previewSize) {
		this.previewSize = previewSize;
	}

	public List<String> getMailIds() {
		return mailIds;
	}

	public void setMailIds(List<String> mailIds) {
		this.mailIds = mailIds;
	}
}
