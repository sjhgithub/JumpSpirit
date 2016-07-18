/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.mailchat.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.umeng.socialize.utils.Log;

import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.activity.WebViewWithErrorViewActivity;
import cn.mailchat.mail.AuthType;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.util.Base64;
import android.widget.TextView;

/**
 * 
 * @Description:通用小工具类：判断日期是否今天，读取数据流，64位转换等
 * @author:gongfacun
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class Utility {

	private static Handler sMainThreadHandler;

	public static Handler getMainThreadHandler() {
		if (sMainThreadHandler == null) {
			// No need to synchronize -- it's okay to create an extra Handler, which will be used
			// only once and then thrown away.
			sMainThreadHandler = new Handler(Looper.getMainLooper());
		}
		return sMainThreadHandler;
	}

	/*
	 * Formats the given size as a String in bytes, kB, MB or GB with a single digit of precision. Ex:
	 * 12,315,000 = 12.3 MB
	 */
	public static String formatSize(float size) {
		long kb = 1024;
		long mb = (kb * 1024);
		long gb = (mb * 1024);
		if (size < kb) {
			return String.format("%d bytes", (int) size);
		} else if (size < mb) {
			return String.format("%.1f KB", size / kb);
		} else if (size < gb) {
			return String.format("%.1f MB", size / mb);
		} else {
			return String.format("%.1f GB", size / gb);
		}
	}

	public final static String readInputStream(InputStream in, String encoding) throws IOException {
		InputStreamReader reader = new InputStreamReader(in, encoding);
		StringBuffer sb = new StringBuffer();
		int count;
		char[] buf = new char[512];
		while ((count = reader.read(buf)) != -1) {
			sb.append(buf, 0, count);
		}
		return sb.toString();
	}

	public final static boolean arrayContains(Object[] a, Object o) {
		for (int i = 0, count = a.length; i < count; i++) {
			if (a[i].equals(o)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Combines the given array of Objects into a single string using the seperator character and each
	 * Object's toString() method. between each part.
	 * 
	 * @param parts
	 * @param seperator
	 * @return
	 */
	public static String combine(Object[] parts, char seperator) {
		if (parts == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0, c = parts.length; i < c; i++) {
			sb.append(parts[i].toString());
			if (i < c - 1) {
				sb.append(seperator);
			}
		}
		return sb.toString();
	}

	public static String base64Decode(String encoded) {
		if (encoded == null) {
			return null;
		}
		byte[] decoded = Base64.decode(encoded.getBytes(), Base64.DEFAULT);
		return new String(decoded);
	}

	public static String base64Encode(String s) {
		if (s == null) {
			return s;
		}
		byte[] encoded = Base64.encode(s.getBytes(), Base64.DEFAULT);
		return new String(encoded);
	}

	public static boolean requiredFieldValid(TextView view) {
		return view.getText() != null && view.getText().length() > 0;
	}

	public static boolean requiredFieldValid(Editable s) {
		return s != null && s.length() > 0;
	}

	/**
	 * Ensures that the given string starts and ends with the double quote character. The string is not
	 * modified in any way except to add the double quote character to start and end if it's not already
	 * there.
	 * 
	 * TODO: Rename this, because "quoteString()" can mean so many different things.
	 * 
	 * sample -> "sample" "sample" -> "sample" ""sample"" -> "sample"
	 * "sample"" -> "sample" sa"mp"le -> "sa"mp"le" "sa"mp"le" -> "sa"mp"le" (empty string) -> "" " -> ""
	 * 
	 * @param s
	 * @return
	 */
	public static String quoteString(String s) {
		if (s == null) {
			return null;
		}
		if (!s.matches("^\".*\"$")) {
			return "\"" + s + "\"";
		} else {
			return s;
		}
	}

	/**
	 * Apply quoting rules per IMAP RFC, quoted = DQUOTE *QUOTED-CHAR DQUOTE QUOTED-CHAR = <any TEXT-CHAR
	 * except quoted-specials> / "\" quoted-specials quoted-specials = DQUOTE / "\"
	 * 
	 * This is used primarily for IMAP login, but might be useful elsewhere.
	 * 
	 * NOTE: Not very efficient - you may wish to preflight this, or perhaps it should check for trouble chars
	 * before calling the replace functions.
	 * 
	 * @param s
	 *            The string to be quoted.
	 * @return A copy of the string, having undergone quoting as described above
	 */
	public static String imapQuoted(String s) {

		// First, quote any backslashes by replacing \ with \\
		// regex Pattern: \\ (Java string const = \\\\)
		// Substitute: \\\\ (Java string const = \\\\\\\\)
		String result = s.replaceAll("\\\\", "\\\\\\\\");

		// Then, quote any double-quotes by replacing " with \"
		// regex Pattern: " (Java string const = \")
		// Substitute: \\" (Java string const = \\\\\")
		result = result.replaceAll("\"", "\\\\\"");

		// return string with quotes around it
		return "\"" + result + "\"";
	}

	/**
	 * A fast version of URLDecoder.decode() that works only with UTF-8 and does only two allocations. This
	 * version is around 3x as fast as the standard one and I'm using it hundreds of times in places that slow
	 * down the UI, so it helps.
	 */
	public static String fastUrlDecode(String s) {
		try {
			byte[] bytes = s.getBytes("UTF-8");
			byte ch;
			int length = 0;
			for (int i = 0, count = bytes.length; i < count; i++) {
				ch = bytes[i];
				if (ch == '%') {
					int h = (bytes[i + 1] - '0');
					int l = (bytes[i + 2] - '0');
					if (h > 9) {
						h -= 7;
					}
					if (l > 9) {
						l -= 7;
					}
					bytes[length] = (byte) ((h << 4) | l);
					i += 2;
				} else if (ch == '+') {
					bytes[length] = ' ';
				} else {
					bytes[length] = bytes[i];
				}
				length++;
			}
			return new String(bytes, 0, length, "UTF-8");
		} catch (UnsupportedEncodingException uee) {
			return null;
		}
	}

	/**
	 * Returns true if the specified date is within today. Returns false otherwise.
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isDateToday(Date date) {
		// TODO But Calendar is so slowwwwwww....
		Date today = new Date();
		if (date.getYear() == today.getYear() && date.getMonth() == today.getMonth() && date.getDate() == today.getDate()) {
			return true;
		}
		return false;
	}

	/*
	 * TODO disabled this method globally. It is used in all the settings screens but I just noticed that an
	 * unrelated icon was dimmed. Android must share drawables internally.
	 */
	public static void setCompoundDrawablesAlpha(TextView view, int alpha) {
		// Drawable[] drawables = view.getCompoundDrawables();
		// for (Drawable drawable : drawables) {
		// if (drawable != null) {
		// drawable.setAlpha(alpha);
		// }
		// }
	}

	public static boolean isEmpty(List<?> list) {
		return list == null || list.size() == 0;
	}

	public static boolean isEmpty(Object[] arr) {
		return arr == null || arr.length == 0;
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
	public static String getUserPassword(URI imapUri){
        AuthType authenticationType = null;
        String username = null;
        String password = null;
        String clientCertificateAlias = null;
        if (imapUri.getUserInfo() != null) {
            try {
                String userinfo = imapUri.getUserInfo();
                String[] userInfoParts = userinfo.split(":");

                if (userinfo.endsWith(":")) {
                    // Password is empty. This can only happen after an account was imported.
                    authenticationType = AuthType.valueOf(userInfoParts[0]);
                    username = URLDecoder.decode(userInfoParts[1], "UTF-8");
                } else if (userInfoParts.length == 2) {
                    authenticationType = AuthType.PLAIN;
                    username = URLDecoder.decode(userInfoParts[0], "UTF-8");
                    password = URLDecoder.decode(userInfoParts[1], "UTF-8");
                } else if (userInfoParts.length == 3) {
                    authenticationType = AuthType.valueOf(userInfoParts[0]);
                    username = URLDecoder.decode(userInfoParts[1], "UTF-8");
                    if (AuthType.EXTERNAL == authenticationType) {
                        clientCertificateAlias = URLDecoder.decode(userInfoParts[2], "UTF-8");
                    } else {
                        password = URLDecoder.decode(userInfoParts[2], "UTF-8");
                    }
                }
            } catch (UnsupportedEncodingException enc) {
                // This shouldn't happen since the encoding is hardcoded to UTF-8
                throw new IllegalArgumentException("Couldn't urldecode username or password.", enc);
            }
        }
		return password;
	}

	public static String getOAUserParam(Account account){
		String resultUrl = "";
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("u=");
			String urlUsername = URLEncoder.encode(account.getoAEmail()!=null?account.getoAEmail():account.getEmail(), "UTF-8");
			String urlPassword = URLEncoder.encode(account.getoAPwd()!=null?account.getoAPwd():Utility.getUserPassword(new URI(account.getStoreUri())), "UTF-8");
			long l = System.currentTimeMillis() / 1000;
			String strTimestamp = Long.toString(l);
			sb.append(urlUsername);
			sb.append("&");
			sb.append("toPage=webapp");
			sb.append("&");
			sb.append("p=");
			sb.append(urlPassword);
			sb.append("&");
			sb.append("t=");
			sb.append(strTimestamp);
			//个人邮箱地址,用于判断是否已经解绑
			sb.append("&");
			sb.append("m=");
			sb.append(account.getEmail());
//			Log.d("qxian",">>>"+sb.toString());
			resultUrl = URLEncoder.encode(Base64.encodeToString(RSAUtils.encryptByPublicKey(
					sb.toString().getBytes("UTF-8"), GlobalConstants.OA_PUBLIC_KEY),
					Base64.DEFAULT),"UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultUrl;
	}

	public static String getEmailDomain(String email) {
		String domain ="";
		if(email.indexOf("@")!=-1){
			String[] emailParts = email.split("@");
			/* 小数点后至少有长度为1的地址 */
			domain=(emailParts.length > 1) ? emailParts[1] : "";;
		}
		return domain;
	}
}
