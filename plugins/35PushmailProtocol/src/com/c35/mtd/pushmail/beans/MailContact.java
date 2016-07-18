package com.c35.mtd.pushmail.beans;

/**
 * 
 * @Description:地址栏联系人bean
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-1-7
 */
public class MailContact {

	@Override
	public int hashCode() {
		return address.hashCode();
	}

	private String name;
	@Override
	public boolean equals(Object o) {
		MailContact m = (MailContact)o;
		return m.getAddress().equals(address);
	}

	private String address;
	private boolean select;
	private boolean match;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isSelect() {
		return select;
	}

	public void setSelect(boolean select) {
		this.select = select;
	}

	public boolean isMatch() {
		return match;
	}

	public void setMatch(boolean match) {
		this.match = match;
	}

}
