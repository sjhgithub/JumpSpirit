package com.c35.mtd.pushmail.beans;


public class QuickEmailInfo {

	
	private String toList;
	private String cclist;
	private String bcclist;
	private String uid;
	private int favState;
	private long id;
	private String sub;
	private String sender;
	
	public String getToList() {
		return toList;
	}
	
	public void setToList(String toList) {
		this.toList = toList;
	}
	
	public String getCclist() {
		return cclist;
	}
	
	public void setCclist(String cclist) {
		this.cclist = cclist;
	}
	
	public String getBcclist() {
		return bcclist;
	}
	
	public void setBcclist(String bcclist) {
		this.bcclist = bcclist;
	}
	
	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public int getFavState() {
		return favState;
	}
	
	public void setFavState(int favState) {
		this.favState = favState;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getSub() {
		return sub;
	}
	
	public void setSub(String sub) {
		this.sub = sub;
	}
	
	public String getSender() {
		return sender;
	}
	
	public void setSender(String sender) {
		this.sender = sender;
	}
	
	
}
