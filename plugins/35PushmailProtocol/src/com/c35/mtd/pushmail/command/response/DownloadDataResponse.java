package com.c35.mtd.pushmail.command.response;

/**
 * 
 * @Description:DownloadData命令响应
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class DownloadDataResponse {

	private int totalBlock;
	private int currentBlock;// 1-*,以1开始
	private int dataLength;
	private byte[] dataContent;// 资源的内容，转成字符再传输，要先进行解压缩//文档数据先切分成合适大小的块，再对每块进行压缩传输

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

	public byte[] getDataContent() {
		return dataContent;
	}

	public void setDataContent(byte[] dataContent) {
		this.dataContent = dataContent;
	}

	public int getDataLength() {
		return dataLength;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

}