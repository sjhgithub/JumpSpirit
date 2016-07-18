package com.c35.mtd.pushmail.command.request;

public class GetBetweenUsMailRequest extends BaseRequest {

	private String user;// 账号(必填项，格式为：abc@35.cn)
	private String searchKey;// 搜索标题字符
	private String betweenUser;// 往来的账号 (逻辑: 1)From 包含betweenUser 2)(From 包含 user 并且 to/cc包含betweenUser)
	private int pageNo;// 第几页(必填项，范围：pageNo>=1)
	private int pageSize;// 页大小(必填项，范围：pageSize>=1)
	private int sortFieldNo;// 排序字段(1-日期、2-发件人、3-主题、4-大小、5-收件人)(必填项，范围：1<=sortFieldNo<=5)
	private int ascending;// 正倒序(0=倒,1=正)(必填项，范围：0<=ascending<=1)

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

	public String getBetweenUser() {
		return betweenUser;
	}

	public void setBetweenUser(String betweenUser) {
		this.betweenUser = betweenUser;
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
