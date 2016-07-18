package com.c35.mtd.pushmail.command.request;

/**
 * 
 * @Description:Attachment命令请求
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class AttachmentRequest extends BaseRequest {

	// 旧邮件的附件（将原邮件的附件作为附件）
	private String attachId;
	// 原邮件id(EML转发时用)
	private String mailId;
	// 资源id
	private String cid;
	// 附件名
	private String fileName;
	// 当附件为URL路径
	private String url;
	// 服务器档案路径
	private String path;
	// 附件类型:1=附件 2=内嵌资源 3=整封EML邮件发送 4=原邮件的附件
	private int type;

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

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
