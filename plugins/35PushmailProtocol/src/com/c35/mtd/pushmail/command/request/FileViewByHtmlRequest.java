package com.c35.mtd.pushmail.command.request;

/**
 * 
 * @Description:FileViewByHtml命令请求
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class FileViewByHtmlRequest extends BaseRequest {

	// 附件id
	private String attachId;
	// 邮件id
	private String mailId;
	// 资源id,当为内嵌资源时，有cid
	private String cid;
	// 附件名
	private String compressFileName;

	public String getAttachId() {
		return attachId;
	}

	public void setAttachId(String attachId) {
		this.attachId = attachId;
	}

	public String getMailId() {
		return mailId;
	}

	public void setMailId(String mailId) {
		this.mailId = mailId;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getCompressFileName() {
		return compressFileName;
	}

	public void setCompressFileName(String compressFileName) {
		this.compressFileName = compressFileName;
	}

}
