package com.c35.mtd.pushmail.beans;

import java.math.BigInteger;

/**
 * 
 * @Description:文件夹bean
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class C35Folder {

	private int folderType = -1;//folderType	int	0=系统文件夹；1=自定义；2=归档；3=其它邮箱
	private long  orderValue = -1;

	private String folderName = "", folderId = "", parentId = "";
	// 总邮件数
	private int totalCount;
	// 未读邮件数
	private int unreadCount;
	
	// 是否选中
	private boolean selected;

	
	public boolean isSelected() {
		return selected;
	}

	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public C35Folder() {

	}

	public C35Folder(String folderId, String folderName, long orderValue, String parentId, int folderType) {

	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}

	public long getOrderValue() {
		return orderValue;
	}

	public void setOrderValue(long orderValue) {
		this.orderValue = orderValue;
	}

	public int getFolderType() {
		return folderType;
	}

	public void setFolderType(int folderType) {
		this.folderType = folderType;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	@Override
	public String toString() {
		return "Folder [parentId=" + parentId + ", orderValue=" + orderValue + ", folderType=" + folderType + ", folderName=" + folderName + ", folderId=" + folderId + "]";
	}

}