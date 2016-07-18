package cn.mailchat.contacts.beans;

import java.io.Serializable;

import cn.mailchat.utils.StringUtil;



/**
 * 联系人信息父类
 * 
 * @copyright © 35.com
 * @file name ：ContactBase.java
 * @author ：zhangyq
 * @create Data ：2014-9-28下午6:18:51 
 * @Current Version：v1.0 
 * @History memory :
 * @Date : 2014-9-28下午6:18:51 
 * @Modified by：zhangyq
 * @Description :
 */
public class ContactBase implements Serializable  {

	/** 
	* @Fields serialVersionUID : TODO
	*/ 
	private static final long serialVersionUID = 1461614472181450181L;

	
	protected String nickName;
	
	protected Character firstChar;

	

	
	public String getNickName() {
		return nickName;
	}

	
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	
	public Character getFirstChar() {
		return firstChar;
	}

	
	public void setFirstChar(Character firstChar) {
		this.firstChar = firstChar;
	}
}
