package com.c35.mtd.pushmail;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.NotificationManager;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * @Description: 日志管理类。主要负责5个级别的日志输出。
 * @author:
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class Debug {
	//发布时关闭log
	private static final boolean LOG = true;//true
	public static final boolean LOGTOAST = true;//true
	// failfast log
	private static final boolean LOGFF = false;
	private static final boolean LOGEGG = false;
	//用来防止msg为空时的异常
	private static final String NULL_STR = "msg is null!";

	public static void d(String tag, String msg) {
		if (LOG)
			Log.d(tag, msg != null ? msg : NULL_STR);
	}

	public static void d(String tag, String msg, Throwable tr) {
		if (LOG)
			Log.d(tag, msg != null ? msg : NULL_STR, tr);
	}

	public static void e(String tag, String msg) {
		if (LOG)
			Log.e(tag, msg != null ? msg : NULL_STR);
	}

	public static void e(String tag, String msg, Throwable tr) {
		try {
			if (LOG)
				Log.e(tag, msg != null ? msg : NULL_STR, tr);
			if (LOGFF) {
			    /*
				Intent intent = new Intent(EmailApplication.getInstance(), FailFastActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// 打印堆栈全部信息用 by cuiwei
				StringWriter sw = new StringWriter();
				tr.printStackTrace(new PrintWriter(sw, true));
				new PrintWriter(sw, true);
				String str = sw.toString();
				intent.putExtra("ExceptionTitle", tr.toString());
				intent.putExtra("ExceptionText", str);
				EmailApplication.getInstance().startActivity(intent);
				*/
			    // Modified by LL
                // BEGIN
			    StringWriter sw = new StringWriter();
			    tr.printStackTrace(new PrintWriter(sw, true));
			    Log.e(tag, sw.toString());
			}
			// if (LOGEGG)
			// C35AppServiceUtil.writeSubscribeInformationToSdcard(tr.getMessage(), Email.getInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void v(String tag, String msg) {
		if (LOG)
			Log.v(tag, msg != null ? msg : NULL_STR);

	}

	public static void v(String tag, String msg, Throwable tr) {
		if (LOG)
			Log.v(tag, msg != null ? msg : NULL_STR, tr);
	}

	public static void i(String tag, String msg) {
		if (LOG)
			Log.i(tag, msg != null ? msg : NULL_STR);
	}

	public static void i(String tag, String msg, Throwable tr) {
		if (LOG)
			Log.i(tag, msg != null ? msg : NULL_STR, tr);
	}

	public static void w(String tag, String msg) {
		if (LOG)
			Log.w(tag, msg != null ? msg : NULL_STR);
	}

	public static void w(String tag, String msg, Throwable tr) {
		if (LOG)
			Log.w(tag, msg != null ? msg : NULL_STR, tr);
	}

	public static void p(String msg) {
		if (LOG)
			Log.i("Ymnl", msg != null ? msg : NULL_STR);// 这个日志输出是我自己用的，方便查看。
	}

}
