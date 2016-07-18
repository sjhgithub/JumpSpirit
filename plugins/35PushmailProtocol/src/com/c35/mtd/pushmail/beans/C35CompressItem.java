package com.c35.mtd.pushmail.beans;

/**
 * @Description:描述 压缩条目的BEAN
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-8-21
 */
public class C35CompressItem {

	private String attachId; // 所属附件id
	private String fileName; // 压缩文档内的文件名（包括路径）
	private long fileSize; // 压缩文档内的文件大小
	private int id;// 序列ID
	private int downState; // 下载状态.0未下载，1下载完成

	public int getDownState() {
		return downState;
	}

	public void setDownState(int downState) {
		this.downState = downState;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAttachId() {
		return attachId;
	}

	public void setAttachId(String attachId) {
		this.attachId = attachId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

}
