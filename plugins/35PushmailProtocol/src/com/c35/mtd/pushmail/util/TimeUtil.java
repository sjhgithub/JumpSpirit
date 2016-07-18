package com.c35.mtd.pushmail.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.c35.mtd.pushmail.EmailApplication;

/**
 * 时间类型相关的列
 * 
 * @Description:
 * @author: zhuanggy
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-1-9
 */
public class TimeUtil {

	// /**
	// * 将日期时间字符串转化为 long 型 (24小时制)
	// *
	// * @Description:
	// * @param time
	// * 如 2013-01-01 18:22:22 [日期和时间之间有空格]
	// * @return
	// * @see:
	// * @since:
	// * @author: zhuanggy
	// * @date:2013-1-9
	// */
	// public static long timeStringToLong(String datetime) {
	// // Debug.e("", datetime);
	// long ll = 0l;
	// int yy = 0;
	// int mm = 0;
	// int dd = 0;
	// int hh = 0;
	// int mi = 0;
	// int sec = 0;
	// if (datetime != null && !"".equals(datetime)) {
	//
	// String[] array1 = datetime.split(" ");
	// String date = array1[0];
	// String time = array1[1];
	//
	// String[] arrayDate = date.split("-");
	// String[] arrayTime = time.split(":");
	// yy = Integer.parseInt(arrayDate[0]);
	// mm = Integer.parseInt(arrayDate[1]) - 1;
	// dd = Integer.parseInt(arrayDate[2]);
	//
	// hh = Integer.parseInt(arrayTime[0]);
	// mi = Integer.parseInt(arrayTime[1]);
	// sec = Integer.parseInt(arrayTime[2]);
	// GregorianCalendar gc = new GregorianCalendar(yy, mm, dd, hh, mi, sec);
	// Date d = gc.getTime();
	// ll = d.getTime();
	// } else {
	// ll = Long.MAX_VALUE;
	// }
	//
	// return ll;
	// }

	/**
	 * 日期+时间的string 型转为long型
	 * 
	 * @Description:
	 * @param dateTime
	 *            (型如 ： 2013-03-08 13:51:00)[日期和时间之间有空格]
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-3-8
	 */
	public static long timeStringToLong(String dateTime) {
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dt2;
		try {
			dt2 = EmailApplication.DateFormatYMDHMS.parse(dateTime);
			return dt2.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	
	/**
	 * 
	 * @Description:将指定格式的时间转换成long型
	 * @param dateTime
	 * @param format
	 * @return
	 * @see: 
	 * @since: 
	 * @author: 温楠
	 * @date:2013-7-19
	 */
	public static long timeStringToLong(String dateTime,String format) {
		 if(dateTime!=null &&dateTime.length()>0){
			try {
				 SimpleDateFormat sdf = new SimpleDateFormat(format);
				 Date date = sdf.parse(dateTime);
				return date.getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		 }
		return -1;
	}

	/**
	 * 时间long转字符串 (24小时制)
	 * 
	 * @Description:
	 * @param millis
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-8
	 */
	public static String timeLongToString(long millis) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return EmailApplication.DateFormatYMDHMS.format(cal.getTime());
	}

}
