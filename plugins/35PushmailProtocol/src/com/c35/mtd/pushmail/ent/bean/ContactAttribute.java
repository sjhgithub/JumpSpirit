package com.c35.mtd.pushmail.ent.bean;

import java.util.Locale;

import com.c35.mtd.pushmail.ent.logic.ContactComparator;
import com.c35.mtd.pushmail.util.StringUtil;

/**
 * 注意：目前只考虑了email属性。
 * @Description:联系人属性
 * @author:huangyx2  
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2013-5-20
 */
public class ContactAttribute extends ContactBase implements Cloneable {

	private String name;
	
	private String value;
	
	private int type;
	
	public ContactAttribute(){}
	
	public ContactAttribute(String value, int type){
		this.value = value;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}

	
	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	
	public int getType() {
		return type;
	}

	
	public void setType(int type) {
		this.type = type;
	}


	@Override
	public Character getFirstChar() {
		if(firstChar == null){
			firstChar = value.toUpperCase(Locale.ENGLISH).charAt(0);
			if (!ContactComparator.isAlpha(firstChar)) {
				firstChar = '#';
			}
		}
		return firstChar;
	}
	
	@Override
	public String getDisplayName() {
		String tmp =  super.getDisplayName();
		if(!StringUtil.isNotEmpty(tmp)){
			tmp = value;
		}
		return tmp;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	public ContactAttribute cloneSelf(){
		ContactAttribute ca = null;
		try {
			ca = (ContactAttribute)clone();
		} catch (CloneNotSupportedException e) {
			ca = new ContactAttribute();
		}
		return ca;
	}
}
