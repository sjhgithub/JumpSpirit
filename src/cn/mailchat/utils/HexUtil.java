package cn.mailchat.utils;

/**
 * 
 * @Description: 十六进制数据处理工具类
 * @author:xulei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:Sep 29, 2013
 */
public class HexUtil {

	/**
	 * 
	 * @Description: 将二进制数组转换为十六进制字符串
	 * @param data
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Sep 29, 2013
	 */
	public static String byte2Hex(byte[] data) {
		StringBuffer hs = new StringBuffer();
		String result = "";
		for (int n = 0; n < data.length; n++) {
			result = Integer.toHexString(data[n] & 0XFF);
			if (result.length() == 1) {
				hs.append("0" + result);
			} else {
				hs.append(result);
			}
		}
		return hs.toString();
	}
}
