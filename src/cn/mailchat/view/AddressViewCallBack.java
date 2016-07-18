package cn.mailchat.view;

import cn.mailchat.mail.Address;

/**
 * 邮件地址栏控件的事件回调
 * 
 * @Description:
 * @author:xulei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:Jun 4, 2013
 */
public interface AddressViewCallBack {

	/**
	 * 地址栏状态改变时回调
	 * 
	 * @Description:
	 * @param view
	 *            被改变状态的地址栏控件
	 * @param active
	 *            被改变成的状态(true:被激活，false:离开)
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public void onActiveChanged(ChoseAddressView view, boolean active);

	/**
	 * 点击地址栏时回调
	 * 
	 * @Description:
	 * @param view
	 *            被点击标签所在的地址栏控件
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public void onNameClicked(ChoseAddressView view);
	
	/**
	 * 点击地址框时回调
	 * 
	 * @Description:
	 * @param view
	 *            被点击标签所在的地址栏控件
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public void onViewClicked(ChoseAddressView view);

	/**
	 * 点击添加按钮时回调
	 * @Description:
	 * @param view 被点击的按钮所在的控件
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public void onAddClicked(ChoseAddressView view);
	
	/**
	 * 地址删除回调(地址控件内点击)
	 * @Description:
	 * @see: 
	 * @since: 
	 * @author: Zhonggaoyong
	 * @date:2013-11-4
	 */
	public void onRemoveContactsCallBack(boolean isListValue);
	/**
	 * 地址下拉列表新增回调(地址控内点击)
	 * @Description:
	 * @see: 
	 * @since: 
	 * @author: Zhonggaoyong
	 * @date:2013-11-4
	 */
	public void onAddContactsCallBack(Address[] addresses);
	
	/**
	 * 地址监听输入的每一个字符
	 * @Description:
	 * @param s
	 * @see: 
	 * @since: 
	 * @author: Zhonggaoyong
	 * @date:2013-11-18
	 */
	public void onTextWatcherCallBack(String s);
	
	/**
	 * 写信界面更多选项
	 * @Description:
	 * @param action
	 * @see: 
	 * @since: 
	 * @author: sunzhongquan
	 * @date:2014-5-18
	 */
	public void onSelectedAction(int action);
}
