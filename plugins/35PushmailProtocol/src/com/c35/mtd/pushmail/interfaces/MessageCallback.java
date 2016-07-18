package com.c35.mtd.pushmail.interfaces;

/**
 * 描述对某封邮件的回调接口
 * 
 * @Description:
 * @author: zhuanggy
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public interface MessageCallback {

	/**
	 * 邮件下载完成
	 * 
	 * @Description:
	 * @param uid
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-11-2
	 */
	void getMessageFinished(String uid);

	/**
	 * 邮件下载失败
	 * 
	 * @Description:
	 * @param uid
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-11-2
	 */
	void getMessageFailed(String uid);
}
