package com.c35.mtd.pushmail.command.request;

import java.util.List;

/**
 * 
 * @Description:AdvanceSearchMails命令请求
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class AdvanceSearchMailsRequest extends BaseRequest {

	private String searchKey;
	private int searchKeyPosi;
	private String from;
	private String to;
	private String cc;
	private String folderId;
	private String labelId;
	private String startSendTime;
	private String endSendTime;
	private int attachFlag;
	private int readState;
	private int pageNo;
	private int pageSize;
	private int sortFieldNo;
	private int ascending;
	List<String> sourceSystem;
	private int importantFlag;
	private int fetchBodyType;
	private int sourceProcessState = 0;

	public String getSearchKey() {
		return searchKey;
	}

	public void setSearchKey(String searchKey) {
		this.searchKey = searchKey;
	}

	public int getSearchKeyPosi() {
		return searchKeyPosi;
	}

	public void setSearchKeyPosi(int searchKeyPosi) {
		this.searchKeyPosi = searchKeyPosi;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	public String getLabelId() {
		return labelId;
	}

	public void setLabelId(String labelId) {
		this.labelId = labelId;
	}

	public String getStartSendTime() {
		return startSendTime;
	}

	public void setStartSendTime(String startSendTime) {
		this.startSendTime = startSendTime;
	}

	public String getEndSendTime() {
		return endSendTime;
	}

	public void setEndSendTime(String endSendTime) {
		this.endSendTime = endSendTime;
	}

	public int getAttachFlag() {
		return attachFlag;
	}

	public void setAttachFlag(int attachFlag) {
		this.attachFlag = attachFlag;
	}

	public int getReadState() {
		return readState;
	}

	public void setReadState(int readState) {
		this.readState = readState;
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

	public List<String> getSourceSystem() {
		return sourceSystem;
	}

	public void setSourceSystem(List<String> sourceSystem) {
		this.sourceSystem = sourceSystem;
	}

	public int getImportantFlag() {
		return importantFlag;
	}

	public void setImportantFlag(int importantFlag) {
		this.importantFlag = importantFlag;
	}

	public int getFetchBodyType() {
		return fetchBodyType;
	}

	public void setFetchBodyType(int fetchBodyType) {
		this.fetchBodyType = fetchBodyType;
	}

	public int getSourceProcessState() {
		return sourceProcessState;
	}

	public void setSourceProcessState(int sourceProcessState) {
		this.sourceProcessState = sourceProcessState;
	}
}
