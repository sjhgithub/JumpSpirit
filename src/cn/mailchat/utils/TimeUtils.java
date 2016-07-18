package cn.mailchat.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {
	// 公共的日期时间格式化对象，都要使用以下现成的方法
	public static final SimpleDateFormat DateFormatYMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat DateFormatMDHM = new SimpleDateFormat("MM-dd HH:mm");
	public static final SimpleDateFormat DateFormatYMD = new SimpleDateFormat("yyyyMMdd");
	public static final SimpleDateFormat DateFormatYYMMDDHHMMSS = new SimpleDateFormat("yyyyMMddHHmmss");
	public static final SimpleDateFormat DateFormatEEE = new SimpleDateFormat("EEEE");
	public static final SimpleDateFormat DateFormatHM = new SimpleDateFormat("HH:mm");
	public static final SimpleDateFormat DataFormatCHINESYYMMDD = new SimpleDateFormat("yyyy年MM月dd日");
	public static final SimpleDateFormat DataFormatCHINESYYMMDDHHMM = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
	public static final SimpleDateFormat DataFormatENYYMMDD = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat DataFormatCHINESMMDD = new SimpleDateFormat("MM月dd日");
	public static final SimpleDateFormat DataFormatENMMDD = new SimpleDateFormat("MM-dd");
	public static final SimpleDateFormat DateFormatHHMM = new SimpleDateFormat("a h:mm");
	public static final SimpleDateFormat DateFormatMMDDF = new SimpleDateFormat("MM月dd日 EEEE");
	public static final SimpleDateFormat DateFormatENMMDDF = new SimpleDateFormat("MM-dd EEEE");
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.CHINA);
	public static final SimpleDateFormat DateFormatDayOfWeek = new SimpleDateFormat("EEEE");
	public static final SimpleDateFormat DateFormatYMDHM = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	/**
	 * 
	 * method name: toFriendly 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param date
	 *      @return    field_name
	 *      String    return type
	 *  @History memory：
	 *     @Date：2014-11-5 下午6:38:52	@Modified by：zhangjx
	 *     @Description：将日期格式化成易读的格式 
	 */
	public static String toFriendly(Date date) {
		String result = "";
		long current = System.currentTimeMillis();
		long time = date.getTime();
		if (time <= current) {
			int intevalDays = intevalDays(date, new Date(current));
			switch (intevalDays) {
			case 0:
				long interval = current - time;
				int hour = (int) ((interval) / (3600 * 1000));
				int minute = (int) ((interval - hour * 3600 * 1000) / (60 * 1000));
				if (hour == 0) {
					if (minute == 0) {
						result = "刚刚";
					} else {
						result = minute + "分钟前";
					}
				} else {
					result = hour + "小时前";
				}
				break;
			default:
				result = DataFormatCHINESMMDD.format(date);
				break;
			}
		}
		return result;
	}
	public static int intevalDays(Date startDate, Date endDate) {
		long time1 = toBeginning(startDate).getTime();
		long time2 = toBeginning(endDate).getTime();
		long inteval = time1 - time2;
		return (int) (inteval / (25 * 60 * 60 * 1000));
	}
	public static Date toBeginning(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd ");
		Date beginDate = null;
		try {
			beginDate = format.parse(format.format(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return beginDate;
	}
	/**
	 *
	 * method name: getSystemCurTime
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @return    field_name
	 *      String    return type
	 *  @History memory：
	 *     @Date：2015-5-22 下午1:49:09	@Modified by：zhangjx
	 *     @Description：获取系统当前时间
	 */
	public static String getSystemCurTime(){
		String strTimestamp = Long.toString(System.currentTimeMillis() / 1000);
        return strTimestamp;
	}

	// 获得当天0点时间
	public static long getTimesmorning() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}

	// 获得当天24点时间
	public static long getTimesnight() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 24);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}

	// 获得本周一0点时间
	public static long getTimesWeekmorning() {
		Calendar cal = Calendar.getInstance();
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY),
				cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		return cal.getTimeInMillis();
	}

	// 获得本周日24点时间
	public static long getTimesWeeknight() {
		Calendar cal = Calendar.getInstance();
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY),
				cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		return cal.getTime().getTime() + (7 * 24 * 60 * 60 * 1000);
	}

	// 获得本月第一天0点时间
	public static long getTimesMonthmorning() {
		Calendar cal = Calendar.getInstance();
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY),
				cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		cal.set(Calendar.DAY_OF_MONTH,
				cal.getActualMinimum(Calendar.DAY_OF_MONTH));
		return cal.getTimeInMillis();
	}

	// 获得本月最后一天24点时间
	public static long getTimesMonthnight() {
		Calendar cal = Calendar.getInstance();
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY),
				cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		cal.set(Calendar.DAY_OF_MONTH,
				cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.HOUR_OF_DAY, 24);
		return cal.getTimeInMillis();
	}
}
