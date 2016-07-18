package com.c35.mtd.pushmail.util;

import java.util.regex.Pattern;

/**
 * 
 * @Description:多媒体类型判断工具
 * @author:gongfacun
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class MimeUtility {

	public static final String DEFAULT_ATTACHMENT_MIME_TYPE = "application/octet-stream";

	/**
	 * Returns true if the given mimeType matches the matchAgainst specification.
	 * 
	 * @param mimeType
	 *            A MIME type to check.
	 * @param matchAgainst
	 *            A MIME type to check against. May include wildcards such as image/* or * /*.
	 * @return
	 */
	public static boolean mimeTypeMatches(String mimeType, String matchAgainst) {
		Pattern p = Pattern.compile(matchAgainst.replaceAll("\\*", "\\.\\*"), Pattern.CASE_INSENSITIVE);
		return p.matcher(mimeType).matches();
	}

	/**
	 * Returns true if the given mimeType matches any of the matchAgainst specifications.
	 * 
	 * @param mimeType
	 *            A MIME type to check.
	 * @param matchAgainst
	 *            An array of MIME types to check against. May include wildcards such as image/* or * /*.
	 * @return
	 */
	public static boolean mimeTypeMatches(String mimeType, String[] matchAgainst) {
		for (String matchType : matchAgainst) {
			if (mimeTypeMatches(mimeType, matchType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * INTERIM version of foldAndEncode that will be used only by Subject: headers. This is safer than
	 * implementing foldAndEncode() (see above) and risking unknown damage to other headers.
	 * 
	 * TODO: Copy this code to foldAndEncode(), get rid of this function, confirm all working OK.
	 * 
	 * @param s
	 *            original string to encode and fold
	 * @param usedCharacters
	 *            number of characters already used up by header name
	 * 
	 * @return the String ready to be transmitted
	 */
	// public static String foldAndEncode2(String s, int usedCharacters) {
	// // james.mime4j.codec.EncoderUtil.java
	// // encode: encodeIfNecessary(text, usage, numUsedInHeaderName)
	// // Usage.TEXT_TOKENlooks like the right thing for subjects
	// // use WORD_ENTITY for address/names
	//
	// try {
	// String encoded = EncoderUtil.encodeIfNecessary(s,
	// EncoderUtil.Usage.TEXT_TOKEN, usedCharacters);
	// return fold(encoded, usedCharacters);
	// } catch (Exception e) {
	// e.getMessage();
	// }
	// return null;
	// }
}
