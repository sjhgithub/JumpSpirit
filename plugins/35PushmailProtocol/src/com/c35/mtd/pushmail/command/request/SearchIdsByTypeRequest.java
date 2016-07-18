package com.c35.mtd.pushmail.command.request;

/**
 * 
 * @Description:SearchIdsByType命令请求
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-1-21
 */
public class SearchIdsByTypeRequest extends BaseRequest {

	private String searchKey;
	private int pageNo;
	private int pageSize;
	private int sortFieldNo;
	private int ascending;
	private int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
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
