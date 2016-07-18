package cn.mailchat.utils;

import java.util.Comparator;

import cn.mailchat.contacts.beans.ContactAttribute;


/**
 * 联系人列表排序
 * 
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
		if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
			return true;
		}
		return false;
	}

	public int compare(ContactAttribute u1, ContactAttribute u2) {
		Character ch1 = u1.getFirstChar();
		Character ch2 = u2.getFirstChar();

		if (isAlpha(ch1)) {
			if (!isAlpha(ch2)) {
				return -1;
			}
		} else {
			if (isAlpha(ch2)) {
				return 1;
			}
		}

		if (ch1 == ch2) {

			// //搜索联系人时往来邮件的靠上些
			// String u1Count=u1.getCount();
			// String u2Count=u2.getCount();
			// if(!u1Count.equals(u2Count)){
			// return u2.getCount().compareTo(u1.getCount());
			// }
			return u1.getEmail().compareTo(u2.getEmail());
		}
		return ch1.compareTo(ch2);
	}
}
