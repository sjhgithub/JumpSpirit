package com.c35.mtd.pushmail.interfaces;

import java.util.HashMap;

/**
 * 
 * @Description:加载邮件体的监听器
 * @author:sunzhongquan  
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2014-1-21
 */
public class LoadMessageListener {

	public static HashMap<String, LoadMessageCallback> listeners = new HashMap<String, LoadMessageListener.LoadMessageCallback>();

	public static void addListener(String uid, LoadMessageCallback callback) {
		if (!listeners.containsKey(uid)) {
			listeners.put(uid, callback);
		}
	}

	public static void removeListener(String uid) {
		if (listeners.containsKey(uid)) {
			listeners.remove(uid);
		}
	}

	public static LoadMessageCallback getListener(String uid) {
		if (listeners.containsKey(uid)) {
			return listeners.get(uid);
		} else {
			return null;
		}
	}

	public interface LoadMessageCallback {

		/**
		 * 网络加载成功或者失败都刷新界面
		 * @Description:
		 * @see: 
		 * @since: 
		 * @author: sunzhongquan
		 * @date:2014-1-21
		 */
		public void loadMessageFinishedOrFailed();
	}
}
