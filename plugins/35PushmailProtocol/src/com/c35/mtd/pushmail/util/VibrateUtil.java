package com.c35.mtd.pushmail.util;

/**
 * 
 * @Description:调用系统振动工具类
 * @author:gongfacun
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class VibrateUtil {

	/**
	 * 
	 * @param pattern
	 *            振动参数
	 * @param times
	 *            振动次数
	 * @return
	 */
	public static long[] getVibration(int pattern, int times) {
		// These are "off, on" patterns, specified in milliseconds
		long[] pattern0 = new long[] { 300, 200 }; // like the default pattern
		long[] pattern1 = new long[] { 100, 200 };
		long[] pattern2 = new long[] { 100, 500 };
		long[] pattern3 = new long[] { 200, 200 };
		long[] pattern4 = new long[] { 2000, 500 };
		long[] pattern5 = new long[] { 500, 500 };

		long[] selectedPattern = pattern0; // default pattern

		switch (pattern) {
		case 0:
			selectedPattern = pattern0;
			break;
		case 1:
			selectedPattern = pattern1;
			break;
		case 2:
			selectedPattern = pattern2;
			break;
		case 3:
			selectedPattern = pattern3;
			break;
		case 4:
			selectedPattern = pattern4;
			break;
		case 5:
			selectedPattern = pattern5;
			break;
		}

		long[] repeatedPattern = new long[selectedPattern.length * times];
		for (int n = 0; n < times; n++) {
			System.arraycopy(selectedPattern, 0, repeatedPattern, n * selectedPattern.length, selectedPattern.length);
		}
		// Do not wait before starting the vibration pattern.
		repeatedPattern[0] = 0;
		return repeatedPattern;
	}
}
