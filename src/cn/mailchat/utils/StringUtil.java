package cn.mailchat.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.mailchat.helper.Regex;

import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.util.Log;

public class StringUtil {
	// 35域名
	public static final String DOMAIN_35CN = "35.cn";
	// china-channel域名
	public static final String DOMAIN_CHINACHANNEL = "china-channel.com";
	// 35邮箱后缀
	public static final String EMAIL_SUFFIX_35CN = "@" + DOMAIN_35CN;
	// china-channel邮箱后缀
	public static final String EMAIL_SUFFIX_CHINACHANNEL = "@"
			+ DOMAIN_CHINACHANNEL;

	private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String bytes2HexString(byte[] data) {
		StringBuffer sb = new StringBuffer(32);
		for (byte b : data) {
			char low = DIGITS[b & 0x0F];
			char high = DIGITS[(b & 0xF0) >>> 4];
			sb.append(high);
			sb.append(low);
		}
		return sb.toString();
	}

	/**
	 * 字符串equal比较
	 * 
	 * @Description:
	 * @param s1
	 * @param s2
	 * @return
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-7-24
	 */
	public static boolean equal(String s1, String s2) {
		if (s1 == s2) {
			return true;
		}
		if (s1 == null) {
			return false;
		}
		return s1.equals(s2);
	}

	/**
	 * 
	 * @Description:
	 * @param str
	 * @return
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-7-24
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	/**
	 * val 属于区间[from, to]
	 * 
	 * @Description:
	 * @param val
	 * @param from
	 * @param to
	 * @return
	 * @see:
	 * @since:
	 * @author: yangentao
	 * @date:2012-7-18
	 */

	public static boolean inRange11(int val, int from, int to) {
		return val >= from && val <= to;
	}

	/**
	 * val 属于区间[from, to)
	 * 
	 * @Description:
	 * @param val
	 * @param from
	 * @param to
	 * @return
	 * @see:
	 * @since:
	 * @author: yangentao
	 * @date:2012-7-18
	 */

	public static boolean inRange10(int val, int from, int to) {
		return val >= from && val < to;
	}

	/**
	 * 将a-z的小写字符转换成A-Z的大写字符, 如果给定的字符不在[a,z]区间内, 则返回原字符
	 * 
	 * @Description:
	 * @param ch
	 * @return
	 * @see:
	 * @since:
	 * @author: yangentao
	 * @date:2012-7-18
	 */

	public static char toUpper(char ch) {
		if (inRange11(ch, 'a', 'z')) {
			return (char) (ch - 'a' + 'A');
		}
		return ch;
	}

	/**
	 * 去掉字符串中的换行；用于邮件列表中预览的显示
	 * 
	 * @Description:
	 * @param str
	 * @return
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2013-8-2
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
	 * tab 换为一个空格，多个空格合并；用于邮件列表中预览显示
	 * 
	 * @Description:
	 * @param str
	 * @return
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2013-8-2
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
	 * 将以“,”分隔的字符串转换成String[]
	 * 
	 * @Description:
	 * @param str
	 * @return
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-8-7
	 */
	public static String[] str2Array(String str) {
		if (isEmpty(str)) {
			return null;
		}
		return str.split(",");
	}

	/**
	 * 将String[]转换成以“,”分隔的字符串
	 * 
	 * @Description:
	 * @param str
	 * @return
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-8-7
	 */
	public static String array2String(String[] arr) {
		if (arr == null || arr.length == 0) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		int i = 0;
		for (String str : arr) {
			if (i > 0) {
				buffer.append(",");
			}
			buffer.append(str);
			i++;
		}
		return buffer.toString();
	}

	/**
	 * 将String集合转换成以“,”分隔的字符串
	 * 
	 * @Description:
	 * @param str
	 * @return
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-8-7
	 */
	public static String list2String(Collection<String> c) {
		if (c == null || c.isEmpty()) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		int i = 0;
		for (String str : c) {
			if (i > 0) {
				buffer.append(",");
			}
			buffer.append(str);
			i++;
		}
		return buffer.toString();
	}

	/**
	 * 将以","分隔的string转成list
	 * 
	 * @Description:
	 * @param str
	 * @return
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-8-12
	 */
	public static List<String> str2List(String str) {
		return str2List(str, ",");
	}

	/**
	 * string转成list
	 * 
	 * @Description:
	 * @param str
	 * @param split
	 *            分隔符
	 * @return
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-8-12
	 */
	public static List<String> str2List(String str, String split) {
		if (isEmpty(str)) {
			return null;
		}
		List<String> list = new ArrayList<String>();
		if (split == null) {
			list.add(str);
		} else {
			list.addAll(Arrays.asList(str.split(split)));
		}
		return list;
	}

	/**
	 * 提供UUID
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-8-12
	 */
	public static String getUUid() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * 将一组数据（字符串）按照给定的分割符组合成一个字符串<br>
	 * 分隔符只出现在数据中间
	 * 
	 * @Description:
	 * @param parts
	 * @param separator
	 *            分隔符
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Aug 22, 2013
	 */
	public static String combine(Object[] parts, char separator) {
		if (parts == null) {
			return null;
		} else if (parts.length == 0) {
			return "";
		} else if (parts.length == 1) {
			return parts[0].toString();
		}
		StringBuilder sb = new StringBuilder();
		sb.append(parts[0]);
		for (int i = 1; i < parts.length; ++i) {
			sb.append(separator);
			sb.append(parts[i]);
		}
		return sb.toString();
	}

	/**
	 * 获取UUID
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: Zhonggaoyong
	 * @date:2013-10-8
	 */
	public static String buildUUID() {
		String uid = UUID.randomUUID().toString();
		return uid.replaceAll("-", "");
	}

	/**
	 * 账号域后辍，将@china-channel.com统一成@35.cn
	 * 
	 * @param email
	 * @return
	 */
	public static String getEmailSuffix(String email) {
		Pattern pattern = Pattern.compile("[\\s*\t\n\r]");
		Matcher matcher = pattern.matcher(email);
		String finalEmail = matcher.replaceAll("");
		if (finalEmail.contains("@china-channel.com")) {
			return finalEmail.replace("@china-channel.com", "@35.cn");
		}
		return email;
	}

	// 判断email格式是否正确
	public static boolean isEmail(String email) {
		String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);
		return m.matches();
	}

	/**
	 * 验证邮箱的合法性
	 * 
	 * @param addressList
	 * @return
	 */
	public static boolean isEmailAllValid(String addressList) {// 验证邮箱的正则表达式
		// String format =
		// "[a-zA-Z0-9_\\-\\.]+@[a-zA-Z0-9_\\-\\.]+(\\.(com|cn|org|edu|hk))";
		if (addressList != null && addressList.length() > 0) {
			Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(addressList);
			for (int i = 0, length = tokens.length; i < length; ++i) {
				Rfc822Token token = tokens[i];
				String address = token.getAddress();
				if (!TextUtils.isEmpty(address)
						&& !isValidEmailAddress(address)) {
					return false;// 邮箱名不合法，返回false
				}

			}
		}
		return true;// 邮箱名合法，返回true
	}

	public static boolean isValidEmailAddress(CharSequence text) {
		return cn.mailchat.helper.Regex.EMAIL_ADDRESS_PATTERN.matcher(text)
				.matches();
	}

	public static boolean isValidPhoneNo(CharSequence num) {
		Pattern p = Pattern.compile("^(1[3,4,5,7,8][0-9])\\d{8}$");
		Matcher m = p.matcher(num);
		return m.matches();
	}

	/**
	 * method name: getPrdfixStr function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param toEmail
	 * @return field_name String return type
	 * @History memory：
	 * @Date：2014-10-23 下午7:39:46 @Modified by：zhangjx
	 * @Description：获取邮箱地址前缀（@前字符串）
	 */
	public static String getPrdfixStr(String toEmail) {
		// TODO Auto-generated method stub
		String emailString;
		if (toEmail.contains("@")) {
			emailString = toEmail.substring(0, toEmail.lastIndexOf("@"));
		} else {
			emailString = toEmail;
		}
		return emailString;
	}

	/**
	 * 获取字符串出现的次数
	 * 
	 * @Description:
	 * @param email
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-3-17
	 */
	public static int getStringCount(String c, String content) {
		int count = 0;
		while (true) {
			int index = content.indexOf(c);
			if (index == -1) {
				return count;
			}
			count++;
			content = content.substring(index + 1, content.length());
		}
	}

	/**
	 * 将35.cn域的邮件地址转换为china-channel域的地址，非35.cn域的直接返回
	 * 
	 * @param email
	 *            ：要转换的邮件地址
	 * @return 转换后的邮件地址
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Aug 14, 2012
	 */
	public static String convert35CNToChinaChannel(String email) {
		if (email != null && email.endsWith(EMAIL_SUFFIX_35CN)) {
			return email.replace(EMAIL_SUFFIX_35CN, EMAIL_SUFFIX_CHINACHANNEL);
		} else {
			return email;
		}
	}

	/**
	 * 将china-channel域的邮件地址转换为35.cn域的地址，非china-channel域的直接返回
	 * 
	 * @param email
	 *            ： 要转换的邮件地址
	 * @return 转换后的邮件地址
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Aug 14, 2012
	 */
	public static String convertChinaChannelTo35CN(String email) {
		if (email != null && email.endsWith(EMAIL_SUFFIX_CHINACHANNEL)) {
			return email.replace(EMAIL_SUFFIX_CHINACHANNEL, EMAIL_SUFFIX_35CN);
		} else {
			return email;
		}
	}

	/**
	 * 去掉某一部分字符
	 * 
	 * @Description:
	 * @param removePart
	 *            去掉的部分
	 * @param allCharacters
	 *            空字符串
	 * @see:
	 * @since:
	 * @return:如果包含这个字符串就去掉,没有就返回原字符串
	 * @author: shengli
	 * @date:2015-4-29
	 */
	public static String removePartCharacters(String removePart,
			String allCharacters) {
		if (allCharacters.contains(removePart)) {
			allCharacters = allCharacters.replace(removePart, "");
		} else {
			allCharacters = "";
		}
		return allCharacters;
	}

	/**
	 * 获取昵称 格式：盛力<shengli@35.cn>
	 * 
	 * @Description:
	 * @param info
	 * @see:
	 * @since:
	 * @return:如果包含这个字符串就去掉,没有就返回原字符串
	 * @author: shengli
	 * @date:2015-9-2
	 */
	public static String getInfoNickName(String info) {
		int index = info.indexOf("<");
		if (index != -1) {
			info = info.substring(0, index);
		}
		return info;
	}

	/**
	 * 获取email 格式：盛力<shengli@35.cn>
	 * 
	 * @Description:
	 * @param info
	 * @see:
	 * @since:
	 * @return:如果包含这个字符串就去掉,没有就返回原字符串
	 * @author: shengli
	 * @date:2015-9-2
	 */
	public static String getInfoEMail(String info) {
		int startIndex = info.indexOf("<");
		int endIndex = info.lastIndexOf(">");
		if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
			info = info.substring(startIndex + 1, endIndex);
		}
		return info;
	}

	/**
	 * 
	 * method name: findStringArray function @Description: TODO Parameters and
	 * return values description:
	 * 
	 * @param source
	 * @param target
	 * @param num
	 * @return field_name List<String> return type
	 * @History memory：
	 * @Date：2015-9-9 下午4:12:33 @Modified by：zhangjx
	 * @Description:循环遍历出string下的手机号
	 */
	public static List<String> decodeStrToList(String str) {
		List<String> result = new ArrayList<String>();
		if (str!=null) {
			if (str.contains(",")) {
				String[] splitstr = str.split(",");
				for (String s : splitstr) {
					result.add(s);
				}
			}else {
				result.add(str);
			}
		}
		return result;
	}

    /**
     * ASCII表中可见字符从!开始，偏移位值为33(Decimal)
     */
    private static final char DBC_CHAR_START = 33; // 半角!

    /**
     * ASCII表中可见字符到~结束，偏移位值为126(Decimal)
     */
    private static final char DBC_CHAR_END = 126; // 半角~

    /**
     * 全角对应于ASCII表的可见字符从！开始，偏移值为65281
     */
    private static final char SBC_CHAR_START = 65281; // 全角！

    /**
     * 全角对应于ASCII表的可见字符到～结束，偏移值为65374
     */
    private static final char SBC_CHAR_END = 65374; // 全角～

    /**
     * ASCII表中除空格外的可见字符与对应的全角字符的相对偏移
     */
    private static final int CONVERT_STEP = 65248; // 全角半角转换间隔

    /**
     * 全角空格的值，它没有遵从与ASCII的相对偏移，必须单独处理
     */
    private static final char SBC_SPACE = 12288; // 全角空格 12288

    /**
     * 半角空格的值，在ASCII中为32(Decimal)
     */
    private static final char DBC_SPACE = ' '; // 半角空格

    /**
     * 全角字符->半角字符转换
     * 只处理全角的空格，全角！到全角～之间的字符，忽略其他
     */
    public static String qj2bj(String src) {
        if (src == null) {
            return src;
        }

        StringBuilder buf = new StringBuilder(src.length());
        char[] ca = src.toCharArray();
        for (int i = 0; i < src.length(); i++) {
            if (ca[i] >= SBC_CHAR_START && ca[i] <= SBC_CHAR_END) { // 如果位于全角！到全角～区间内
                buf.append((char) (ca[i] - CONVERT_STEP));
            } else if (ca[i] == SBC_SPACE) { // 如果是全角空格
                buf.append(DBC_SPACE);
            } else { // 不处理全角空格，全角！到全角～区间外的字符
                buf.append(ca[i]);
            }
        }

        return buf.toString();
    }
}
