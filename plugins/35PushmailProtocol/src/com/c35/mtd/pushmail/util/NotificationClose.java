package com.c35.mtd.pushmail.util;

import java.util.Map.Entry;
import java.util.Set;

import com.c35.mtd.pushmail.EmailApplication;

import android.app.NotificationManager;
import android.content.Context;

/**
 * 用于处理进入Mail后，关闭所有状态栏通知提醒。
 * @author:zhang hailin
 * @see:
 * @since:
 * @copyright 35.com
 * @date:2012-6-13
 */

public class NotificationClose {

	/**
	 * 关闭所有通知栏
	 * @return: void
	 * @throws:
	 * @see:
	 * @since:
	 * @author: zhang hailin
	 * @date:2012-6-13
	 */

	public static void closeAllNotifications() {
	    // Modified by LL
	    // BEGIN
	    /*
		NotificationManager notifMgr = (NotificationManager) EmailApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

		if (!PushMailService.notifyIdentify.isEmpty()) {
			Set<Entry<Integer, Integer>> entrySet = PushMailService.notifyIdentify.entrySet();
			for (Entry<Integer, Integer> set : entrySet) {
				int id = set.getKey();
				notifMgr.cancel(id);
			}
			PushMailService.notifyIdentify.clear();
		}
		*/
	    // END
	}

	public static void closeDeskDeskTopNotifyActivity() {
	    // Modified by LL
	    // BEGIN
	    /*
		if (!DeskTopNotifyView.messages.isEmpty()) {
			DeskTopNotifyView.messages.clear();
		}
		*/
	    // END
	}
}
