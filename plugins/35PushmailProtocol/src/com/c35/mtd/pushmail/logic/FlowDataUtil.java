package com.c35.mtd.pushmail.logic;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.MessagingController;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.store.LocalStore;
import com.c35.mtd.pushmail.store.Store;
import com.c35.mtd.pushmail.util.StoreDirectory;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.TrafficStats;

public class FlowDataUtil {

	public static Object flow_data_lock = new Object();
//	public static final String SHUTDOWNFLAG = "SHATDOWN"; // 关机标志位
	public static final String WIFISTARTFLAG = "wifistart"; // wifi开始标志
	public static final String WIFIENDFLAG = "wifiend"; // wifi结束标志
	public static final String WIFISAVE = "wifisave";// wifi累加标志
//	public static final String GNETSTARTFLAG = "gnetstart"; // 3g网络开始标志
//	public static final String GNETENDFLAG = "gnetend"; // 3g网络结束标志
//	public static final String GNETSAVE = "gnetsave";// 3g网络累加标志
	public static final String MAILTYPE = "mail";// 邮件类型
	public static final String SYSTEMFLOW = "systemflow";// 系统总流量标志
	public static final String SYSTEMGNETFLOW = "systemgnetflow";// 系统3g流量标志
	public static final String AMSAVEFLOW = "flow_start_amon";// 早晨存储广播
	public static final String PMSAVFLOW = "flow_start_onpm";// 晚上存储广播
	/**
	 * 分析，并保存流量数据到数据库
	 * @param wififlag
	 * @see: 
	 * @since: 
	 * @author: cuiwei
	 * @date:2013-12-2
	 */
	public static void saveflowdata(String wififlag) {
		synchronized (flow_data_lock) {
			long rdata, tdata, rdatatotal, tdatatotal, rdatagnet, tdatagnet;
			int uid;
			PackageManager pm = EmailApplication.getInstance().getPackageManager();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String flowdate = df.format(new Date());
			try {
				uid = pm.getApplicationInfo(EmailApplication.getInstance().getPackageName(), PackageManager.GET_META_DATA).uid;
				rdata = TrafficStats.getUidRxBytes(uid);
				tdata = TrafficStats.getUidTxBytes(uid);
				rdatagnet = TrafficStats.getMobileRxBytes(); // 获取通过Mobile连接收到的字节总数
				tdatagnet = TrafficStats.getMobileTxBytes(); // Mobile发送的总字节数
				rdatatotal = TrafficStats.getTotalRxBytes(); // 获取总的接受字节数，包含Mobile和WiFi等
				tdatatotal = TrafficStats.getTotalTxBytes(); // 总的发送字节数，包含Mobile和WiFi等
				flowdatasave(flowdate, wififlag, rdata, tdata, rdatatotal, tdatatotal, rdatagnet, tdatagnet);
			} catch (Exception e) {
				Debug.e("failfast", "failfast_AA", e);
			}
		}

	}


	/**
	 * 清除流量表里的数据
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2013-5-17
	 */
	public static void clearFlowData() {
		LocalStore localStore;
		try {
			localStore = (LocalStore) Store.getInstance(StoreDirectory.getStoreageUri());
			localStore.clearflowdata();
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 从DB中查询打算显示的流量数据
	 * @return
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2013-5-11
	 */
	public static Cursor searchFlowData() {
		Cursor cursor = null;
		try {
			LocalStore localStore = (LocalStore) Store.getInstance(StoreDirectory.getStoreageUri());
			cursor = localStore.selecttopdata();
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		return cursor;
	}
	/**
	 * 设置定时发送广播
	 * @param am
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2013-5-11
	 */
	public static void startflowdata(boolean am) {
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		Intent it;
		if (am) {
			calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
			calendar.set(java.util.Calendar.MINUTE, 3);
			it = new Intent(AMSAVEFLOW);

		} else {
			calendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
			calendar.set(java.util.Calendar.MINUTE, 58);
			it = new Intent(PMSAVFLOW);
		}
		AlarmManager alarmManager = (AlarmManager) EmailApplication.getInstance().getSystemService(EmailApplication.getInstance().ALARM_SERVICE);
		PendingIntent peit = PendingIntent.getBroadcast(EmailApplication.getInstance(), 0, it, 0);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 86400 * 1000, peit);
	}
	/**
	 * 存储流量数据到DB，insertflowdata
	 * @param date
	 * @param wififlag
	 * @param rdata
	 * @param tdata
	 * @param rdatatotal
	 * @param tdatatotal
	 * @param rdatagnet
	 * @param tdatagnet
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2013-5-11
	 */
	public static void flowdatasave(String flowdate, String wififlag, long rdata, long tdata, long rdatatotal, long tdatatotal, long rdatagnet, long tdatagnet) {

		try {
			LocalStore localStore = (LocalStore) Store.getInstance(StoreDirectory.getStoreageUri());
			localStore.insertflowdata(flowdate, wififlag, rdata, tdata, rdatatotal, tdatatotal, rdatagnet, tdatagnet);
		} catch (Exception e) {// android.database.sqlite.SQLiteDiskIOException: disk I/O error (code 1034)
			// SQLiteException: cannot commit - no transaction is active
			Debug.e("failfast", "failfast_AA", e);
		}

	}
}
