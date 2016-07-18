package com.c35.mtd.pushmail.command.request;

/**
 * 
 * @Description:GetMailById命令请求
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class GetMailByIdRequest extends BaseRequest {

	private String mailId;
	private int returnParam; // 返回的数据：
	// 1.只返回纯文本
	// 2.只返回富文本
	// 3.只返回纯文本与富文本
	// （startByte与endByte只对富文本的作截取）
	// 4.接口中定义的所有内容都返回
	// （startByte与endByte只对富文本的作截取）
	private int startByte; // 开始的字节 单位:byte
	private int endByte; // 结束的字节 单位:byte
	private int assemblePicture; // 是否由服务器组装邮件中图片， 0：不组装(cid的形式) 1：组装（组装成http的形式） 默认为1

	public String getMailId() {
		return mailId;
	}

	public void setMailId(String mailId) {
		this.mailId = mailId;
	}

	public int getReturnParam() {
		return returnParam;
	}

	public void setReturnParam(int returnParam) {
		this.returnParam = returnParam;
	}

	public int getStartByte() {
		return startByte;
	}

	public void setStartByte(int startByte) {
		this.startByte = startByte;
	}

	public int getEndByte() {
		return endByte;
	}

	public void setEndByte(int endByte) {
		this.endByte = endByte;
	}

	public int getAssemblePicture() {
		return assemblePicture;
	}

	public void setAssemblePicture(int assemblePicture) {
		this.assemblePicture = assemblePicture;
	}

}
