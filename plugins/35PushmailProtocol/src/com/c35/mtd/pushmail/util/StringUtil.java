package com.c35.mtd.pushmail.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentResolver;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.EmailApplication;

/**
 * 
 * @Description:字符串处理工具类
 * @author:gongfacun
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class StringUtil {

	// 应用的语言设置
	public static final String APP_LANGUAGE_AUTO = "auto";
	public static final String APP_LANGUAGE_CN = "zh_CN";
	public static final String APP_LANGUAGE_TW = "zh_TW";
	public static final String APP_LANGUAGE_US = "en_US";

	private static final String TAG = "StringUtil";

	/**
	 * 此方法处理传入的字符串，如果传入字符串需要按指定长度截取则返回一个以"..."结尾的字符串。
	 * 
	 * @param str
	 *            需要处理的字符串
	 * @param length
	 *            指定长度 注：按照字符串中2个字母的长度为1，一个汉字的长度为1的规律，指定一个字符串的长度；
	 * @return
	 * @author liujie
	 */
	public static String processString(String str, int length) {
		// if(str==null)return "pm1";
		long startTime = System.currentTimeMillis();
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager systemService = (WindowManager) EmailApplication.getInstance().getSystemService("window");
		systemService.getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;
		// Debug.d(TAG, "screenWidth = "+screenWidth );
		int screenHeight = dm.heightPixels;
		// Debug.d(TAG, "screenHeight = "+screenHeight );
		int gapPixels = screenWidth > screenHeight ? screenWidth - screenHeight : screenHeight - screenWidth;
		// Debug.d(TAG, "gapPixels = "+gapPixels );
		int gapLength = gapPixels / 13 + 1;
		// Debug.d(TAG, "gapLength = "+gapLength );

		String temp = str.trim();

		int cnt = 0, ent = 0, resultCount = 0, charAt = 0;
		// Debug.d(TAG, "length = "+length );
		if (screenWidth < screenHeight) {
			// Debug.d(TAG, "length = "+length );
			if (getStringLength(temp) > length) {
				for (int i = 0; i < temp.length(); i++) {
					if (resultCount == length)
						return temp.substring(0, charAt) + "...";
					if (isChinese(temp.charAt(i))) {
						cnt++;
					} else {
						ent++;
					}
					resultCount = cnt + ent / 2;
					charAt = i;
				}
			} else {
				return temp;
			}

		} else {
			length += gapLength / 2;
			// Debug.d(TAG, "length = "+length );
			if (getStringLength(temp) > length) {
				for (int i = 0; i < temp.length(); i++) {
					if (resultCount == length) {
						// Debug.d(TAG, "result = "+temp.substring(0, charAt)+"..." );
						return temp.substring(0, charAt) + "...";
					}
					if (isChinese(temp.charAt(i))) {
						cnt++;
					} else {
						ent++;
					}
					resultCount = cnt + ent / 2;
					charAt = i;
				}
			} else {
				return temp;
			}
		}
		// Debug.d(TAG, "length = "+length );
		long endTime = System.currentTimeMillis();
		Debug.i(TAG, "processString cost " + (endTime - startTime) + "ms");

		return "";
	}

	/**
	 * 判断一个字符是否是汉字
	 * 
	 * @param a
	 * @return
	 */
	private static boolean isChinese(Character c) {
		// return ((int)a>=19968 && (int)a <=171941);
		return c.toString().length() != c.toString().getBytes().length;
	}

	/**
	 * 按照字符串中2个字母的长度为1，一个汉字的长度为1的规律,获取一个字符串的长度
	 * 
	 * @param str
	 * @return
	 */
	private static int getStringLength(String str) {
		int cnt = 0, ent = 0;
		for (int i = 0; i < str.length(); i++) {
			if (isChinese(str.charAt(i))) {
				cnt++;
			} else {
				ent++;
			}
		}
		return cnt + ent / 2;
	}

	/**
	 * 为适应需求变化临时添加的方法
	 * 
	 * @Title: mergeString
	 * @author liujie
	 * @date 2011-9-22
	 * @return String
	 * @throws
	 */
	public static String mergeString(String senderList, String subject, int length) {
		String sender = "..";
		try {
			sender = Address.parse(senderList)[0].getPersonal();
		} catch (Exception e) {//ArrayIndexOutOfBoundsException
			Debug.e(TAG, "failfast_AA", e);
		}
		String totalString = "";
		totalString = sender + ":" + subject;

		return processString(totalString, length);

	}

	/**
	 * 判断当前时间是否在闲时内
	 * 
	 * @author liujie
	 * @param startTime
	 *            用户设置的闲时开始时间
	 * @param endTime
	 *            用户设置的显示结束时间
	 * @return true 闲时屏蔽开启，并且在当前在闲时需要屏蔽震动、铃声； false 闲时屏蔽关闭或者当前不在闲时；
	 */
	public static boolean isInFreeTime(String startTime, String endTime, boolean isFreeTime) {
		boolean isIn = false;
		if (isFreeTime) {
			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int min = calendar.get(Calendar.MINUTE);
			String hourString = hour + "";
			String minString = min + "";
			if (hourString.length() == 1) {
				hourString = "0" + hourString;
			}
			if (minString.length() == 1) {
				minString = "0" + minString;
			}
			String current = hourString + ":" + minString;
			if (startTime.compareTo(endTime) > 0) {
				if (!(current.compareTo(endTime) > 0 && current.compareTo(startTime) < 0)) {
					isIn = true;
				}
			} else {
				if (current.compareTo(startTime) > 0 && current.compareTo(endTime) < 0) {
					isIn = true;
				}
			}
		}
		return isIn;
	}

	/**
	 * @author liujie 设置页面右上方显示的邮箱地址处理方法 :整个email地址如果不超过21单字符，那么完整显示，如果超过，则取左起9个字符和右起九个字符，中间用“…”
	 * @param email
	 * @return
	 */
	public static String settingEmail(String email) {
		int length = email.length();
		if (length <= 21) {
			return email;
		}
		return email.substring(0, 9) + "..." + email.substring(email.length() - 9, email.length());
	}

	/**
	 * 
	 * @Title: noOneEmpty
	 * @Description: 描述 判断一批字符串是否都是非空的
	 * @author liujie
	 * @date 2011-12-6
	 * @return boolean
	 * @throws
	 */
	public static boolean noOneEmpty(Object... strArray) {
		for (Object s : strArray) {
			if (s instanceof String) {
				if (!isNotEmpty((String) s))
					return false;
			} else {
				if (s == null)
					return false;
			}
		}
		return true;

	}

	public static boolean isNotEmpty(String s) {
		return (s != null && s.trim().length() > 0);
	}

	/**
	 * 去掉字符串中的换行；用于邮件列表中预览的显示
	 * 
	 * @Description:
	 * @param str
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-9-25 注： \n 回车 \t 水平制表符 \s 空格 \r 换行
	 */
	public static String deleteEmptyLine(String str) {
		String dest = str;
		if (str != null) {
			Pattern p = Pattern.compile("\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}

	/**
	 * tab 换为一个空格，多个空格合并；用于邮件列表中预览的显示
	 * 
	 * @Description:
	 * @param str
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-9-25
	 */
	public static String combineBlank(String str) {
		String dest = str;
		if (str != null) {
			Pattern p = Pattern.compile("\t");
			Matcher m = p.matcher(str);
			dest = (m.replaceAll(" ")).trim().replaceAll(" +", " ");
		} else {
			dest = "";
		}
		return dest;
	}

	/**
	 * 从文件读取数组
	 * 
	 * @Description:
	 * @param file
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-10-10
	 */
	public static ArrayList<String> readArray(File file) {
		ArrayList<String> result = new ArrayList<String>();
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
			result = (ArrayList<String>) in.readObject();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Debug.e(TAG, "failfast_AA", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Debug.e(TAG, "failfast_AA", e);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Debug.e(TAG, "failfast_AA", e);
		}
		return result;
	}

	/**
	 * 把数组写入文件
	 * 
	 * @Description:
	 * @param email
	 * @param file
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-10-10
	 */
	public static void writeString2Array(String email, File file) {
		// 绑定账号时，先读取已经存储的绑定成功过的账号，若不存在当前绑定的账号，则添加到其中。
		ArrayList<String> a = new ArrayList<String>();
		if (file.exists()) {
			a = readArray(file);
		}
		if (a != null && !(a.contains(email))) {
			a.add(email);
			try {
				if (file.exists()) {
					file.delete();
					file.createNewFile();
				}
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
				out.writeObject(a);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Debug.e(TAG, "failfast_AA", e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Debug.e(TAG, "failfast_AA", e);
			}
		}
	}

	/**
	 * 根据系统时间设置来返回不同的时间格式 上午 10：20 或 下午 2：20
	 * 
	 * @Description:
	 * @param str
	 * @return
	 * @see:
	 * @since:
	 * @author: zhangran
	 * @date:2013-5-13
	 */
	public static String getTimeBySetting(String str) {
		String time = "";
		try {
			// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = EmailApplication.DateFormatYMDHMS.parse(str);
			ContentResolver cv = EmailApplication.getInstance().getContentResolver();
			String strTimeFormat = android.provider.Settings.System.getString(cv, android.provider.Settings.System.TIME_12_24);
			if (("24".equals(strTimeFormat))) {// 24小时
				java.text.SimpleDateFormat sd = new java.text.SimpleDateFormat("HH:mm");
				time = sd.format(date);
			} else {// 12小时
				java.text.SimpleDateFormat sd = new java.text.SimpleDateFormat("a hh:mm");
				time = sd.format(date);
			}

			return time;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return time;
	}

	/**
	 * 获取UUID
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-5-21
	 */
	public static String buildUUID() {
		String uid = UUID.randomUUID().toString();
		return uid.replaceAll("-", "");
	}

	/**
	 * 账号域后辍，@35.cn和@china-channel.com统一成@china-channel.com
	 * 
	 * @Description:
	 * @param account
	 * @return
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-5-21
	 */
	public static String getAccountSuffix(Account account) {
		String email = account.getEmail().toLowerCase(Locale.ENGLISH);
		if (email.endsWith(MailUtil.EMAIL_SUFFIX_35CN) || email.endsWith(MailUtil.EMAIL_SUFFIX_CHINACHANNEL)) {
			return MailUtil.EMAIL_SUFFIX_CHINACHANNEL;
		} else {
			return email.substring(account.getEmail().indexOf("@"));
		}
	}
}
