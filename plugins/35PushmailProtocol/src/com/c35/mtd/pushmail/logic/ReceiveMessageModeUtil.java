package com.c35.mtd.pushmail.logic;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.util.NetworkUtil;

/**
 * 读取邮件若为智能切换模式，则通过此类来判断为哪一个状态，然后设置一个标记
 * 
 * @Description:
 * @author:zhuanggy
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-10-17
 */
public class ReceiveMessageModeUtil {

	// public static final File DIR_RECEIVEMAILMODE_AUTO_ALL = new
	// File("/data/data/com.c35.mtd.pushmail/receiveall");
	public static boolean receveAllInAutoMode = false;// 为true表示智能模式下，收取邮件内容,false不收取内容，

	/**
	 * 剩余存储空间是否大于50M
	 * 
	 * @Description:
	 * @param cm
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-10-17
	 */
	public static boolean storageIsEnough() {
		long availableSize = 0;
		if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			/** 获取存储卡剩余存储空间 */
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs statFs = new StatFs(sdcardDir.getPath());
			long blockSize = statFs.getBlockSize();
			long availableBlocks = statFs.getAvailableBlocks();
			availableSize = (blockSize * (availableBlocks)) / 1024 / 1024;
		} else {
			/** 获取剩余内部存储空间 **/
			File path = Environment.getDataDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			availableSize = (blockSize * (availableBlocks)) / 1024 / 1024;
		}
		Debug.v("ReceiveMessageModeUtil", "storage available size: " + availableSize);
		if ((availableSize - 50) < 0) {
			return false;
		}
		return true;
	}

	/**
	 * 设置邮件读取状态。仅在智能切换模式下生效
	 * @param receiveMode
	 * @return true当前为完整模式，false当前为省流量模式
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-10-18
	 */
	public static boolean setReceiveMailMode(int receiveMode) {
		if (receiveMode != 0) {
			if (receiveMode == 1) {
				Debug.v("ReceiveMessageModeUtil", "省流量模式");
				return false;
			} else {
				Debug.v("ReceiveMessageModeUtil", "完整模式");
				return true;
			}
		}
		if (NetworkUtil.isNetworkAvailable()) {// 网络可用
			if (storageIsEnough()) {// 剩余存储空间大于50M
				if (NetworkUtil.isWifi()) {// wifi网络
					// 设置完整模式标记
					Debug.v("ReceiveMessageModeUtil", "智能切换模式下变为： 完整模式");
					receveAllInAutoMode = true;
					// if (!DIR_RECEIVEMAILMODE_AUTO_ALL.exists()) {
					// DIR_RECEIVEMAILMODE_AUTO_ALL.mkdir();
					// }
					return true;
				}
			}
		}
		// 设置省流量模式标记
		Debug.v("ReceiveMessageModeUtil", "智能切换模式下变为：省流量模式；  或 此时无网络");
		receveAllInAutoMode = false;
		// if (DIR_RECEIVEMAILMODE_AUTO_ALL.exists()) {
		// DIR_RECEIVEMAILMODE_AUTO_ALL.delete();
		// }
		return false;
	}

	/**
	 * 获取邮件收取模式
	 * 
	 * @param account  邮件收取模式getRecvMailMode 0智能切换，1省流量模式，2完整模式
	 * @return true收完整/false 省流量
	 */
	public static boolean getReceiveMode(Account account) {
		if ((account.getRecvMailMode() == 2) || (account.getRecvMailMode() == 0 && ReceiveMessageModeUtil.receveAllInAutoMode)) {
			return true;
		} else {
			return false;
		}
	}

}
