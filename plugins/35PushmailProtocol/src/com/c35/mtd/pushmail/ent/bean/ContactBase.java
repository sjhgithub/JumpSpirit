package com.c35.mtd.pushmail.ent.bean;

import com.c35.mtd.pushmail.util.StringUtil;

/**
 * 联系人信息父类
 * @Description:
 * @author:huangyx2  
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2013-5-20
 */
public class ContactBase {

	protected String userName;
	
	protected String nickName;
	
	protected Character firstChar;

	
	public String getUserName() {
		return userName;
	}

	
	public void setUserName(String userName) {
		this.userName = userName;
	}

	
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
	/**
	 * 默认姓名
	 * @Description:
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-20
	 */
	public String getDisplayName(){
		if(!StringUtil.isNotEmpty(userName)){
			return nickName;
		}
		return userName;
	}
	
}
