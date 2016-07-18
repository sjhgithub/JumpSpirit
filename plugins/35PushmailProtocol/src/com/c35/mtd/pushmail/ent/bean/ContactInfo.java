package com.c35.mtd.pushmail.ent.bean;

import java.util.ArrayList;
import java.util.List;

import com.c35.mtd.pushmail.ent.database.EntContactColumns.TContactAttributes;
import com.c35.mtd.pushmail.ent.database.EntContactColumns.TContacts;
import com.c35.mtd.pushmail.util.StringUtil;

import android.content.ContentValues;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.text.TextUtils;

/**
 * 注意：目前只考虑了email属性。
 * @Description:联系人信息
 * @author: huangyongxing
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-6-29
 */
public class ContactInfo extends ContactBase{

	private int id;

	private String uuid;
	/** 生日 */
	private String birthday;
	/***/
	private String sex;

	private String account;
	
	/* 属于邮箱的属性集合 */
	private List<ContactAttribute> mailAttributes = new ArrayList<ContactAttribute>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getSex() {
		return sex;
	}

	
	public String getUuid() {
		return uuid;
	}

	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	
	public String getAccount() {
		return account;
	}

	
	public void setAccount(String account) {
		this.account = account;
	}

	
	public List<ContactAttribute> getMailAttributes() {
		return mailAttributes;
	}

	public void setMailAttributes(List<ContactAttribute> mailAttributes) {
		this.mailAttributes = mailAttributes;
	}


	// ----------------------------------Mail-----------------------------//
	public void clearMailAttributes() {
		mailAttributes.clear();
	}

	public void removeMailAttribute(ContactAttribute attri) {
		mailAttributes.remove(attri);
	}

	/* 添加Mail分类属性基本属性 */
	public void addMailAttribute(ContactAttribute attribute) {
		if (TextUtils.isEmpty(attribute.getValue()) || "".equals(attribute.getValue())) {
			return;
		}
		mailAttributes.add(attribute);
	}

	public ContentValues baseToContentValues() {
		ContentValues values = new ContentValues();
		values.put(TContacts.UUID, StringUtil.isNotEmpty(getUuid()) ? getUuid() : StringUtil.buildUUID());
		values.put(TContacts.USERNAME, getUserName());
		values.put(TContacts.NICKNAME, getNickName());
		values.put(TContacts.BIRTHDAY, getBirthday());
		values.put(TContacts.SEX, getSex());
		values.put(TContacts.ACCOUNT, getAccount());
		if(getId() > 0){
			values.put(TContacts._ID, getId());
		}
		return values;
	}
	

	/** 将联系人属性集合 转为 ContentValues */
	public List<ContentValues> attris2ContentValues() {

		List<ContentValues> allValues = new ArrayList<ContentValues>();
		attrisToContantValues(getMailAttributes(), allValues);

		return allValues;

	}


	/***
	 * 
	 * @Description: 将属性集合转为ContentValues
	 * @param attris
	 * @param allValues
	 * @return
	 * @see:
	 * @since:
	 * @author: Guang Hui
	 * @date:2012-5-4
	 */
	public List<ContentValues> attrisToContantValues(List<ContactAttribute> attris, List<ContentValues> allValues) {
		for (ContactAttribute eachAttri : attris) {
			if (!StringUtil.isNotEmpty(eachAttri.getValue())) {
				continue;
			}
			ContentValues values = new ContentValues();
			if(eachAttri.getType() == Email.TYPE_CUSTOM){
				values.put(TContactAttributes.NAME, eachAttri.getName());
			}
			values.put(TContactAttributes.VALUE, eachAttri.getValue());
			values.put(TContactAttributes.TYPE, eachAttri.getType());
			values.put(TContactAttributes.CATEGORY, Email.CONTENT_ITEM_TYPE);
			values.put(TContactAttributes.CID, getId());

			allValues.add(values);
		}

		return allValues;
	}

	@Override
	public boolean equals(Object o) {
		ContactInfo contact = (ContactInfo) o;
		return contact.getDistincID() == getDistincID() || this==o;
	}

	/**
	 * 使企业联系人ID和个人联系人的ID不一样
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: yangentao
	 * @date:2012-7-4
	 */

	public int getDistincID() {
		return (id + 1) << 16;
	}
}
