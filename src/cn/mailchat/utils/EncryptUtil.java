package cn.mailchat.utils;

import java.security.MessageDigest;

/**
 * 加密工具类
 * 
 * @Description:
 * @author:xulei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:Sep 29, 2013
 */
public class EncryptUtil {

	/**
	 * 获得字符串的md5密文
	 * 
	 * @Description:
	 * @param data
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Sep 29, 2013
	 */
	public static String getMd5(String data) {
		try {
			byte[] dataArray = data.getBytes("UTF-8");
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(dataArray);
			byte[] encrypteData = messageDigest.digest();
			return HexUtil.byte2Hex(encrypteData);
		} catch (Exception e) {
			return null;
		}
	}
}
