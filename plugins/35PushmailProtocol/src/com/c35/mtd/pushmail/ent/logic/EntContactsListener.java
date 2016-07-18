package com.c35.mtd.pushmail.ent.logic;

import java.util.List;

import com.c35.mtd.pushmail.ent.bean.ContactAttribute;


/**
 * 联系人同步事件监听
 * @Description:
 * @author: huangyongxing
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2012-8-27
 */
public interface EntContactsListener {

	// 要考虑的员工信息同步问题：1、添加员工，2、删除员工，3、修改员工信息
	
	public void onDateChanged();
	
	/**
	 * 正在加载企业联系人
	 * @Description:
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-27
	 */
	public void onLoadingContacts();
	
	/**
	 * 正在同步企业联系人
	 * @Description:
	 * @see: 
	 * @since: 
	 * @author: huangyongxing
	 * @date:2012-9-27
	 */
	public void onGetContacts();
	
	
	
	/**
	 * 失败
	 * @Description:
	 * @see: 
	 * @since: 
	 * @author: huangyongxing
	 * @date:2012-9-27
	 */
	public void onFial();
	
	/**
	 * 成功
	 * @Description:
	 * @see: 
	 * @since: 
	 * @author: huangyongxing
	 * @date:2012-9-27
	 */
	public void onSucc();
	
	/**
	 * 搜索结果
	 * @Description:
	 * @param results
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-23
	 */
	public void onSearch(List<ContactAttribute> results);
}
