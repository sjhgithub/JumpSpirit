package com.c35.mtd.pushmail.beans;

import java.util.ArrayList;

/**
 * 已发送邮件，已读未读信息，为减少计算次数
 * @Description:
 * @author: zhuanggy
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2013-6-18
 */

public class MessageReadInfo {

	private boolean reseiversHasGroup;// 已读收件人中是否包含群组
	private int allReceiversCount;// 已读收件人数目
	private ArrayList<String> readersNickName;// 已读人的昵称

	public boolean isReseiversHasGroup() {
		return reseiversHasGroup;
	}

	public void setReseiversHasGroup(boolean reseiversHasGroup) {
		this.reseiversHasGroup = reseiversHasGroup;
	}

	public int getAllReceiversCount() {
		return allReceiversCount;
	}

	public void setAllReceiversCount(int allReceiversCount) {
		this.allReceiversCount = allReceiversCount;
	}

	public ArrayList<String> getReadersNickName() {
		return readersNickName;
	}

	public void setReadersNickName(ArrayList<String> readersNickName) {
		this.readersNickName = readersNickName;
	}

}
