package com.c35.mtd.pushmail.beans;

/**
 * 
 * @Description:描述 标签的BEAN
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class Label {

	private String labelId; // 标签ID
	private String labelName; // 标签名称
	private String labelColor; // 标签颜色
	private long orderValue; // 排序号

	public Label() {
		super();
	}

	public Label(String labelId, String labelName, String labelColor, long orderValue) {
		super();
		this.labelId = labelId;
		this.labelName = labelName;
		this.labelColor = labelColor;
		this.orderValue = orderValue;
	}

	public String getLabelId() {
		return labelId;
	}

	public void setLabelId(String labelId) {
		this.labelId = labelId;
	}

	public String getLabelName() {
		return labelName;
	}

	public void setLabelName(String labelName) {
		this.labelName = labelName;
	}

	public String getLabelColor() {
		return labelColor;
	}

	public void setLabelColor(String labelColor) {
		this.labelColor = labelColor;
	}

	public long getOrderValue() {
		return orderValue;
	}

	public void setOrderValue(long orderValue) {
		this.orderValue = orderValue;
	}
}
