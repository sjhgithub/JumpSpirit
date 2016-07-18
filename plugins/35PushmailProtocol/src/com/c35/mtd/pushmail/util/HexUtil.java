/* 
 * Copyright (C), 2004-2010, 三五互联科技股份有限公司
 * Encoding UTF-8 
 * Version: 1.0 
 * Date: 2011-9-5
 * History:
 * 1. Date: 2011-9-5
 *    Author: guojl
 *    Modification: 新建
 * 2. ...
 */
package com.c35.mtd.pushmail.util;

/**
 * 
 * @description 转化为十六进制数据
 * @param b
 * @return
 * @author guojl
 */
public class HexUtil {
	public static String byte2hex(byte[] b) {
		StringBuffer hs = new StringBuffer();
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs.append("0" + stmp);
			else
				hs.append(stmp);
		}
		return hs.toString();
	}

	/**
	 * 
	 * @description 从十六进制数据转为数组，
	 * @param hexString十六进制数据
	 * @return
	 * @author guojl
	 */
	public static byte[] hex2byte(String hexString) {
		byte[] b = hexString.getBytes();
		if ((b.length % 2) != 0)
			throw new IllegalArgumentException("长度不是偶数");
		byte[] b2 = new byte[b.length / 2];
		for (int n = 0; n < b.length; n += 2) {
			String item = new String(b, n, 2);
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		return b2;
	}

}
