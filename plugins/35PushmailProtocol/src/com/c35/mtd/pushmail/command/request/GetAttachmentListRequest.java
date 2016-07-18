package com.c35.mtd.pushmail.command.request;

/**
 * 向服务器请求附件列表
 * @Description:
 * @author: zhuanggy
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2013-1-9
 */

public class GetAttachmentListRequest  extends BaseRequest {

	//所需参数
	private String user = "";//账户名
	private String searchKey = "";//搜索关键字
	private int pageNo = 1;//第几页；(必填项，范围：pageNo>=1)
	private int pageSize = 30;//页大小；(必填项，范围：pageSize>=1)
	private int sortFieldNo = 1;//按时间顺序；排序字段(1=发送时间)
	private int ascending = 0;//正序;正倒序(0=倒,1=正)(必填项，范围：0<=ascending<=1)
	private String[] exts = new String[0];//返回对应扩展的数据，为空或size=0时取所有
	
	
	public String[] getExts() {
		return exts;
	}
	
	public void setExts(String[] exts) {
		this.exts = exts;
	}

	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getSearchKey() {
		return searchKey;
	}
	
	public void setSearchKey(String searchKey) {
		this.searchKey = searchKey;
	}
	
	public int getPageNo() {
		return pageNo;
	}
	
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	
	public int getPageSize() {
		return pageSize;
	}
	
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	public int getSortFieldNo() {
		return sortFieldNo;
	}
	
	public void setSortFieldNo(int sortFieldNo) {
		this.sortFieldNo = sortFieldNo;
	}
	
	public int getAscending() {
		return ascending;
	}
	
	public void setAscending(int ascending) {
		this.ascending = ascending;
	}
	
}
