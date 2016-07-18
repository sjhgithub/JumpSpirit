package com.c35.mtd.pushmail.ent.logic;

import java.util.Comparator;
import java.util.Locale;

import com.c35.mtd.pushmail.ent.bean.ContactAttribute;

/**
 * 联系人列表排序
 * @Description:
 * @author:huangyx2  
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2013-5-20
 */
public class ContactComparator implements Comparator<ContactAttribute> {

	// 是否字母,a-z,A-Z
	public static boolean isAlpha(char ch) {
		if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch<= 'Z')) {
			return true;
		}
		return false;
	}

	public int compare(ContactAttribute u1, ContactAttribute u2) {
		Character ch1 = u1.getFirstChar();
		Character ch2 = u2.getFirstChar();
		
		if(isAlpha(ch1)){
			if(!isAlpha(ch2)){
				return -1;
			}
		}else{
			if(isAlpha(ch2)){
				return 1;
			}
		}
		
		if(ch1 == ch2){
			return u1.getValue().compareTo(u2.getValue());
		}
		return ch1.compareTo(ch2);
	}
}
