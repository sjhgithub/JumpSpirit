package com.c35.mtd.pushmail.ent.protocol;

import java.util.List;

import android.provider.ContactsContract.CommonDataKinds.Email;

import com.c35.mtd.pushmail.ent.bean.ContactAttribute;
import com.c35.mtd.pushmail.ent.bean.ContactInfo;

/**
 * 企业联系人同步返回
 * 
 * @Description:
 * @author: huangyongxing
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-8-27
 */
public class SyncEntContactsResponse {

	public int error_code; // 错误码
	public String syncTime; // 返回的同步时间戳(yyyy-MM-dd HH:mm:ss)
	public EntContactItemData data; // 返回的同步数据对象

	/**
	 * 同步企业联系人返回的data
	 * 
	 * @Description:
	 * @author: huangyongxing
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2012-8-28
	 */
	public static class EntContactItemData {

		// 删除的企业联系人信息
		public List<String> deleteList;
		// 更新/添加的企业联系人信息
		public List<CompanyContact> updateList;
	}

	/**
	 * 
	 * @Description: 企业联系人信息
	 * @author: liguanghui
	 * @see:
	 * @since:
	 * @copyright ligh 35.com
	 * @Date:2012-6-1
	 */
	public static class CompanyContact {

		// 雇员ID
		public String employeeID;
		// 姓名
		public String name;
		// 邮箱
		public String email;

		// 扩展信息
		public List<EmployeeExtension> employeeExtension;

		public ContactInfo convert2ContactModel() {

			ContactInfo c = new ContactInfo();
			c.setUserName(name);
			c.setUuid(employeeID);
			// 工作邮箱
			c.addMailAttribute(new ContactAttribute(email, Email.TYPE_WORK));
			
			if (employeeExtension != null) {
				for (EmployeeExtension eachEx : employeeExtension) {
					if (eachEx.propertyID.equals(EmployeeExtension.PRO_NAME)) {// 昵称
						c.setNickName(eachEx.value);
					} else if (eachEx.propertyID.equals(EmployeeExtension.PRO_BIRTHDAY)) {// 生日
						c.setBirthday(eachEx.value);
					} else if (eachEx.propertyID.equals(EmployeeExtension.PRO_GENDER)) { // 性别
						c.setSex(eachEx.value);
					} else if (eachEx.propertyID.equals(EmployeeExtension.PRO_WORKEMAIL) && !eachEx.value.equals(email)) {// 工作邮箱
						c.addMailAttribute(new ContactAttribute(eachEx.value, Email.TYPE_WORK));
					} else if (eachEx.propertyID.equals(EmployeeExtension.PRO_HOMEEMIAL)) {// 家庭邮箱
						c.addMailAttribute(new ContactAttribute(eachEx.value, Email.TYPE_HOME));
					}
				}
			}
			return c;
		}
	}

	/** 联系人扩展属性信息 */
	public static class EmployeeExtension {

		/**
		 * 基本信息<br/>
		 * 昵称:PRO_NAME<br/>
		 * 年龄:PRO_AGE<br/>
		 * 生日:PRO_BIRTHDAY<br/>
		 * 性别:PRO_GENDER 部门:PRO_BRANCH<br/>
		 * 公司:PRO_COMPANY<br/>
		 * 职位:PRO_POSITION<br/>
		 */
		public static final String CAT_BASIC = "com_sys_cat_basic";
		/** 生日 */
		public static final String PRO_BIRTHDAY = "com_sys_pro_birthday";
		/** 性别 */
		public static final String PRO_GENDER = "com_sys_pro_gender";
		/** 昵称 */
		public static final String PRO_NAME = "com_sys_pro_name";
		/** 职位 */

		/**
		 * 电子邮件<br/>
		 * 家庭Email:PRO_HOMEEMIAL<br/>
		 * 工作Email:PRO_WORKEMAIL
		 */
		public static final String CAT_EMAIL = "com_sys_cat_email";
		/** 家庭Email */
		public static final String PRO_HOMEEMIAL = "com_sys_pro_homeemail";
		/** 工作Email */
		public static final String PRO_WORKEMAIL = "com_sys_pro_workemail";


		// 属性ID，对应如上“PRO_”开头的属性
		public String propertyID;
		// 属性分类，对应如上"CAT_"开头的属性分类
		public String categoryID;
		// 属性值
		public String value;
	}
}
