package com.c35.mtd.pushmail.command.response;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.beans.Contact;
import com.c35.mtd.pushmail.beans.ErrorObj;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.util.JsonUtil;

/**
 * 
 * @Description:
 * @author:wennan  
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2013-5-30
 */
public class GetContactsResponse extends BaseResponse {

	private List<Contact> contacts;// 联系人

	
	public List<Contact> getContacts() {
		return contacts;
	}

	
	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}

	@Override
	public void initFeild(String response) throws MessagingException {
		super.initFeild(response);
		try {
			JSONObject jsonObject = new JSONObject(commandMessage);
			this.setContacts(JsonUtil.parseContacts(jsonObject));
		} catch (JSONException e) {
			Debug.w("C35", "JsonExp", e);
			throw new MessagingException(MessagingException.RETURN_COMMAND_ERROR_GET_CONTACTS,e.getMessage());
		}

	}

}
