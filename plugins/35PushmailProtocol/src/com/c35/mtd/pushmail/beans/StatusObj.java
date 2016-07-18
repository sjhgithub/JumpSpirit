package com.c35.mtd.pushmail.beans;

/**
 * 
 * @Description:状态对象Bean
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-10-18
 */
public class StatusObj {

	private int statusId; // 状态id
	private String statusValue; // 状态value
	private long operateTime; // 该状态最终更新时间

	/**
	 * statusId statusId说明 statusValue statusValue说明 10 readFlag 0/1 是否已读（ 0=未读 1=已读） 11 importantFlag 0/1
	 * 是否收藏（0=非收藏1=收藏） 12 delete 0/1 删除操作（0=移到已删除（如果是已删除文件夹，就彻底删除）；1=彻底删除） 13 moveTo 目标文件夹id 移动操作（目标文件夹id）
	 */
	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public String getStatusValue() {
		return statusValue;
	}

	public void setStatusValue(String statusValue) {
		this.statusValue = statusValue;
	}

	public long getOperateTime() {
		return operateTime;
	}

	public void setOperateTime(long operateTime) {
		this.operateTime = operateTime;
	}

}
