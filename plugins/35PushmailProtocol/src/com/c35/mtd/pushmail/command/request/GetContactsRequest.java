package com.c35.mtd.pushmail.command.request;

/**
 * 
 * @Description:
 * @author:wennan
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2013-5-30
 */
public class GetContactsRequest extends BaseRequest {

	private int type; //1 最近联系人 //2 常用联系人//3 重要联系人（新增） 

	
	public int getType() {
		return type;
	}

	
	public void setType(int type) {
		this.type = type;
	}

	
	public int getSize() {
		return size;
	}

	
	public void setSize(int size) {
		this.size = size;
	}

	private int size;//最大返回的联系人数量

}
