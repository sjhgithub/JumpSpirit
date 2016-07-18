package com.c35.mtd.pushmail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.os.AsyncTask;

import com.c35.mtd.pushmail.logic.C35AccountManager;
import com.c35.mtd.pushmail.util.MailUtil;

/**
 * 
 * @Description:
 * @author:xulei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:May 18, 2013
 */
public class CrashHandler implements UncaughtExceptionHandler {

	public static final String TAG = "CrashHandler";

	private UncaughtExceptionHandler defaultExceptionHandler;

	private static final String REPORT_FILE_EXTENSION = ".cdata";

	private static final String PACKAGE_NAME = "pn";// 包名
	private static final String PACKAGE_VERSION = "vn";// 软件版本号
	private static final String DEVICE_NAME = "dn";// 设备名称
	private static final String OS_VERSION = "ov";// 系统版本
	private static final String HARDWARE_VERSION = "hv";// 硬件版本
	private static final String STACK_TRACE = "st";// 错误堆栈信息

	//
	private static final String CRASH_LOG = "_log";

	// private static final String CRASH_TIME = "ct";

	/**
	 * 值为false时，收集并上传错误日志；为true时，不收集不上传错误日志。 所以发布程序时，要确保取值为false。
	 */
	private static final boolean DEBUG = false;

	private CrashHandler() {
	}

	private static class SingletonHolder {

		public static final CrashHandler INSTANCE = new CrashHandler();
	}

	public static CrashHandler getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * 初始化，在application的onCreate里调用
	 * 
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:May 18, 2013
	 */
	public void init() {
		defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		CrashReportTask task = new CrashReportTask();
		task.execute(CrashReportTask.TASK_SEND_REPORT);
	}

	/**
	 * 发生未捕获的异常时，系统回调此方法
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && defaultExceptionHandler != null) {
			defaultExceptionHandler.uncaughtException(thread, ex);
		} else {
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
		}
	}

	/**
	 * 处理未捕获的异常。 返回true表示已处理，返回false表示未处理，交给系统处理。 这里始终返回false，即保存完错误日志之后仍交给系统处理，未来可根据业务调整
	 * 
	 * @Description:
	 * @param ex
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:May 19, 2013
	 */
	private boolean handleException(Throwable ex) {
		if (ex != null && ex.getLocalizedMessage() != null) {
			CrashReportTask task = new CrashReportTask();
			task.execute(CrashReportTask.TASK_SAVE_REPORT, ex);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 将错误日志缓存起来，以备上传
	 * 
	 * @Description:
	 * @param ex
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:May 19, 2013
	 */
	private boolean saveCrashReport(final Throwable ex) {
		try {
			Properties crashReport = prepareCrashReport(ex);
			String fileName = System.currentTimeMillis() + REPORT_FILE_EXTENSION;
			FileOutputStream fos = EmailApplication.getInstance().openFileOutput(fileName, Context.MODE_PRIVATE);
			crashReport.store(fos, "");
			fos.flush();
			fos.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 处理Throwable，提取Throable的错误内容
	 * 
	 * @Description:
	 * @param ex
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:May 19, 2013
	 */
	private Properties prepareCrashReport(Throwable ex) {
		Properties pro = collectExtraInfo();
		//
		Writer causeWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(causeWriter);
		if (ex.getCause() != null) {
			ex.getCause().printStackTrace(printWriter);
		} else {
			ex.printStackTrace(printWriter);
		}
		//
		// SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String crashTime = "Crash time: " + EmailApplication.DateFormatYMDHMS.format(new Date()) + "\n";
		pro.put(STACK_TRACE, crashTime + causeWriter.toString());
		printWriter.close();
		return pro;
	}

	/**
	 * 收集设备的相关信息
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:May 19, 2013
	 */
	private Properties collectExtraInfo() {
		Properties pro = new Properties();
		pro.put(PACKAGE_NAME, EmailApplication.getInstance().getPackageName());
		try {
			PackageManager pm = EmailApplication.getInstance().getPackageManager();
			PackageInfo packageInfo = pm.getPackageInfo(EmailApplication.getInstance().getPackageName(), PackageManager.GET_ACTIVITIES);
			pro.put(PACKAGE_VERSION, packageInfo.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		pro.put(DEVICE_NAME, android.os.Build.MODEL);
		pro.put(HARDWARE_VERSION, android.os.Build.HARDWARE);
		pro.put(OS_VERSION, android.os.Build.VERSION.RELEASE);
		return pro;
	}

	/**
	 * TODO:未来服务端增加搜集错误报告的接口之后，错误信息可以存储在一个文件里
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:May 18, 2013
	 */
	private void sendCrashReports() {
		try {
			ConnectivityManager manager = (ConnectivityManager) EmailApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
			if (manager.getActiveNetworkInfo() != null) {
				String[] reportsFileNames = getCrashReportFiles();
				if (reportsFileNames != null && reportsFileNames.length > 0) {
					TreeSet<String> sortedFiles = new TreeSet<String>();
					sortedFiles.addAll(Arrays.asList(reportsFileNames));
					for (String fileName : sortedFiles) {
						File file = new File(EmailApplication.getInstance().getFilesDir(), fileName);
						if (sendCrashReport2Server(parseCrashReport(file))) {
							file.delete();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析缓存的错误日志文件
	 * 
	 * @Description:
	 * @param file
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:May 19, 2013
	 */
	private Properties parseCrashReport(File file) {
		try {
			Properties pro = new Properties();
			pro.load(new FileInputStream(file));
			return pro;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 取得全部未上传的错误日志文件
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:May 19, 2013
	 */
	private String[] getCrashReportFiles() {
		File filesDir = EmailApplication.getInstance().getFilesDir();
		FilenameFilter filter = new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.endsWith(REPORT_FILE_EXTENSION);
			}
		};
		return filesDir.list(filter);
	}

	/**
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:May 18, 2013
	 */
	public boolean sendCrashReport2Server(Properties report) {
		if (report != null) {
			String url = MailUtil.SERVER_FOR_FEEDBACK;// "http://ota.35.com:8080/35OTA/feedback/saveApp.html";
			return sendPost(url, packReport(report));
		} else {
			return true;
		}
	}

	/**
	 * 将错误日志打包，准备post提交
	 * 
	 * @Description:
	 * @param report
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:May 19, 2013
	 */
	private List<NameValuePair> packReport(Properties report) {
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("pkgName", report.getProperty(PACKAGE_NAME) + CRASH_LOG));
		values.add(new BasicNameValuePair("pkgVersion", report.getProperty(PACKAGE_VERSION)));
		values.add(new BasicNameValuePair("moduleType", report.getProperty(DEVICE_NAME)));
		if (EmailApplication.getCurrentAccount() != null) {
			values.add(new BasicNameValuePair("userId", EmailApplication.getCurrentAccount().getEmail()));
		} else {
			values.add(new BasicNameValuePair("userId", "null"));
		}
		values.add(new BasicNameValuePair("hwv", report.getProperty(HARDWARE_VERSION)));
		values.add(new BasicNameValuePair("swv", report.getProperty(OS_VERSION)));
		values.add(new BasicNameValuePair("feedback", report.getProperty(STACK_TRACE)));
		return values;
	}

	/**
	 * 发起post请求，提交数据至服务器
	 * 
	 * @Description:
	 * @param url
	 * @param params
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:May 19, 2013
	 */
	private boolean sendPost(String url, List<NameValuePair> params) {
		try {
			HttpPost httpPost = new HttpPost(url);
			HttpResponse httpResponse = null;
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			httpResponse = new DefaultHttpClient().execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 错误日志缓存、上传的异步处理类
	 * 
	 * @Description:
	 * @author:xulei
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:May 19, 2013
	 */
	class CrashReportTask extends AsyncTask<Object, Void, Void> {

		public static final int TASK_SAVE_REPORT = 0;// 保存错误日志
		public static final int TASK_SAVE_SEND_REPORT = 1;// 保存并且发送
		public static final int TASK_SEND_REPORT = 2;// 发送错误日志

		@Override
		protected Void doInBackground(Object... params) {
			if (!DEBUG) {
				if ((Integer) params[0] == TASK_SAVE_REPORT) {
					saveCrashReport((Throwable) params[1]);
				} else if ((Integer) params[0] == TASK_SEND_REPORT) {
					sendCrashReports();
				} else if ((Integer) params[0] == TASK_SAVE_SEND_REPORT) {
					if (saveCrashReport((Throwable) params[1])) {
						sendCrashReports();
					}
				}
			}
			return null;
		}
	}
}
