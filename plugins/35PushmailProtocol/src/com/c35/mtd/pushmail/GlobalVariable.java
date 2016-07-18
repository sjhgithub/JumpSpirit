package com.c35.mtd.pushmail;

import java.util.LinkedHashMap;

/**
 * 描述 全局变量,这个常量类是初期开发人员使用的。里面还有许多程序正在使用的。
 * @author:
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class GlobalVariable {

	private static int screenWidth;
	private static int screenHeight;

	private static boolean WEBVIEW_CANMOVE_LEFTEND=false;
	
	public static boolean isWEBVIEW_CANMOVE_LEFTEND() {
		return WEBVIEW_CANMOVE_LEFTEND;
	}

	public static void setWEBVIEW_CANMOVE_LEFTEND(boolean wEBVIEW_CANMOVE_LEFTEND) {
		WEBVIEW_CANMOVE_LEFTEND = wEBVIEW_CANMOVE_LEFTEND;
	}

	private static boolean WEBVIEW_CANMOVE_RIGHTEND=false;

	public static boolean isWEBVIEW_CANMOVE_RIGHTEND() {
		return WEBVIEW_CANMOVE_RIGHTEND;
	}


	public static void setWEBVIEW_CANMOVE_RIGHTEND(boolean wEBVIEW_CANMOVE_RIGHTEND) {
		WEBVIEW_CANMOVE_RIGHTEND = wEBVIEW_CANMOVE_RIGHTEND;
	}

	public static void setScreenWidth(int screenWidth) {
		GlobalVariable.screenWidth = screenWidth;
	}

	public static int getScreenWidth() {
		return screenWidth;
	}

	public static void setScreenHeight(int screenHeight) {
		GlobalVariable.screenHeight = screenHeight;
	}

	public static int getScreenHeight() {
		return screenHeight;
	}

	private static boolean leftEdge = false;// webview左边到头

	public static boolean isLeftEdge() {
		return leftEdge;
	}

	public static void setLeftEdge(boolean leftEdge) {
		GlobalVariable.leftEdge = leftEdge;
	}

	public static boolean isRightEdge() {
		return rightEdge;
	}

	public static void setRightEdge(boolean rightEdge) {
		GlobalVariable.rightEdge = rightEdge;
	}

	private static boolean rightEdge = false;// webview右边到头

	private static boolean fromWidgetOrPush = false;// 是否来自挂件或者push

	public static boolean isFromWidgetOrPush() {
		return fromWidgetOrPush;
	}

	public static void setFromWidgetOrPush(boolean fromWidgetOrPush) {
		GlobalVariable.fromWidgetOrPush = fromWidgetOrPush;
	}

	// private static boolean noMail = false; // 是否服务器端0封邮件，true 是；false不是
	private static boolean inboxFront = false; // 邮箱界面是否是手机的当前显示页，判断弹窗是否弹出
	private static int localStoreType = -1; // 用于获取当前手机存储是在哪 1 手机内存中 2 SD卡中
	
	private static String TEMP_TIME = ""; // 用于临时的保存用户设置定时查收的值
	public static boolean isDialog = false; // 修改密码时的对话框只显示一次。 false 没有弹出
	public static boolean isAlert = false; // 无线网络连接提醒状态：false:未提醒设置; true:已提醒过;
	// public static int NOTIFY_ID = 1205; // 用于Notification的id，并存贮进ShardPreferences中. add by huxw at
	// 2011-02-15
	// // 09:53
	// public static long timerDate; // 用于LED灯定时去掉功能
	public static boolean checkPassword = true; // 是否弹出验证口令的变量；
	public static boolean isProgressing = false;// 是否在显示进度条
	public static int currentProgress = 0; // 当前的进度是
	public static boolean settingSetPass = false; // 设置页面中要弹出的设置口令对话框是否设置成功，成功则会勾选checkPassword勾选
	// public static boolean resetPassSuccess = false; // 输入3次口令错误后，输邮箱密码清除，清除成功后在设置，这时如果设置成功，则true，否则反之
	public static LinkedHashMap<String, Integer> downloadCatch = new LinkedHashMap<String, Integer>();//att_id,progress
	public static final int STOP_DOWNLOAD = 1205;
	public static boolean clearCache = false; // 是否清除缓存后保存邮件
	public static boolean isStopDownload = false; // 进入应用时停止wifi下自动下载

	// 以下为提速优化开关
	public static boolean isAutoSysFolderListInFolderListActivity = false;// 为true时会频繁的同步邮件夹列表,即避免每次从folderlist离开时同步
	public static boolean isCancelDownload = false;

	/**
	 * 描述 记录某附件下载进度到缓存的方法
	 * @Description: 
	 * @author liujie
	 * @date 2012-2-14
	 * @return void
	 * @throws
	 */
	public static void recordProgress(String uid, int progress) {
		if (progress == STOP_DOWNLOAD) {
			downloadCatch.put(uid, STOP_DOWNLOAD);
			return;
		}
		if (progress < 0 || progress >= 100) {
			if (downloadCatch.containsKey(uid)) {
				downloadCatch.remove(uid);
			}
		} else {
			downloadCatch.put(uid, progress);//5281dbb21170a9b44ac0fbbe_inbox_1
		}
	}

	/**
	 * 
	 * @Title: getAttProgress
	 * @Description: 描述
	 * @author liujie
	 * @date 2012-2-14
	 * @return int -1没有进度，则不是下载状态
	 * @throws
	 */
	public static int getAttProgress(String uid) {
		Integer progress = downloadCatch.get(uid);
		if (progress != null) {
			return progress;
		}
		return -1;
	}

	public static String getTEMP_TIME() {
		return TEMP_TIME;
	}

	public static void setTEMP_TIME(String tEMPTIME) {
		TEMP_TIME = tEMPTIME;
	}

	public static boolean isDialog() {
		return isDialog;
	}

	public static synchronized void setDialog(boolean isDialog) {
		GlobalVariable.isDialog = isDialog;
	}

	public static int getLocalStoreType() {
		return localStoreType;
	}

	public static void setLocalStoreType(int localStoreType) {
		GlobalVariable.localStoreType = localStoreType;
	}

	public static boolean isInboxFront() {
		// Log.d(TAG , "inboxFront?  is   "+ inboxFront);
		return inboxFront;
	}

	public static void setInboxFront(boolean inboxFront) {
		// Log.d(TAG , "inboxFront is   "+ inboxFront);
		GlobalVariable.inboxFront = inboxFront;
	}

	public static boolean isAlert() {
		return isAlert;
	}

	public static void setAlert(boolean isAlert) {
		GlobalVariable.isAlert = isAlert;
	}

	public static boolean isCheckPassword() {
		return checkPassword;
	}

	public static void setCheckPassword(boolean checkPassword) {
		GlobalVariable.checkPassword = checkPassword;
	}

	public static boolean isProgressing() {
		return isProgressing;
	}

	public static void setProgressing(boolean isProgressing) {
		GlobalVariable.isProgressing = isProgressing;
	}

	public static int getCurrentProgress() {
		return currentProgress;
	}

	public static void setCurrentProgress(int currentProgress) {
		GlobalVariable.currentProgress = currentProgress;
	}

}
