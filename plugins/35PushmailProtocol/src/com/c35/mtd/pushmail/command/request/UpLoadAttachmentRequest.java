package com.c35.mtd.pushmail.command.request;

/**
 * 
 * @Description:UpLoadAttachment命令请求
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class UpLoadAttachmentRequest extends BaseRequest {

	public enum AttachmentType {
		// 附件类型:1=附件 2=内嵌资源 3=整封EML邮件发送 4=原邮件的附件
		ATT(1), RESOURCE(2), EML(3), RELAY_ATT(4);

		private int type;

		AttachmentType(int type) {
			this.type = type;
		}

		public int getType() {
			return type;
		}
	}

	private String fileName;// 不包括路径
	private int totalBlock;
	private int currentBlock;// 1-*,以1开始
	private int dataLength;
	private byte[] dataContent;// 资源的内容，转成字符再传输，要先进行压缩//文档数据先切分成合适大小的块，再对每块进行压缩传输

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getTotalBlock() {
		return totalBlock;
	}

	public void setTotalBlock(int totalBlock) {
		this.totalBlock = totalBlock;
	}

	public int getCurrentBlock() {
		return currentBlock;
	}

	public void setCurrentBlock(int currentBlock) {
		this.currentBlock = currentBlock;
	}

	public int getDataLength() {
		return dataLength;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	public byte[] getDataContent() {
		return dataContent;
	}

	public void setDataContent(byte[] dataContent) {
		this.dataContent = dataContent;
	}
}
