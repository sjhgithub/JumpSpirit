package cn.mailchat.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.XMLReader;

import cn.mailchat.helper.Regex;

import android.text.Annotation;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.util.Log;

/**
 * 与邮件体富文本相关的工具类
 * 
 * @Description:
 * @author: zhuanggy
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-8-27
 */
public class HyperTextUtil {

	private static final String TAG = "HyperTextUtil";
	// <img\s+.*?>
	private static final String HTML_WEB_IMAGE = "<img\\s+.*?>";// 内嵌、网络图片的html正则表达式
	// private static final String HTML_INNER_IMAGE = "<img[ ]*src[ ]*=\"[ ]*cid";// 内嵌图片的html正则表达式

	private static Pattern p_script;
	private static Matcher matcher;
	/**
	 * When generating previews, Spannable objects that can't be converted into a String are represented as
	 * 0xfffc. When displayed, these show up as undisplayed squares. These constants define the object
	 * character and the replacement character.
	 */
	private static final char PREVIEW_OBJECT_CHARACTER = (char) 0xfffc;
	private static final char PREVIEW_OBJECT_REPLACEMENT = (char) 0x20; // space

	/**
	 * toHtml() converts non-breaking spaces into the UTF-8 non-breaking space, which doesn't get rendered
	 * properly in some clients. Replace it with a simple space.
	 */
	private static final char NBSP_CHARACTER = (char) 0x00a0; // utf-8 non-breaking space
	private static final char NBSP_REPLACEMENT = (char) 0x20; // space

	/**
	 * 判断是否包含网络图片、内嵌图片
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-8-27
	 */
	public static boolean containsWebInnerImage(String hyperText) {
		p_script = Pattern.compile(HTML_WEB_IMAGE, Pattern.CASE_INSENSITIVE);
		matcher = p_script.matcher(hyperText);
		if (matcher.find()) {
			Log.v(TAG, "包含网络图片或内嵌图片");
			return true;
		}
		Log.v(TAG, "不包含网络图片或内嵌图片");
		return false;
	}

	/**
	 * 是否包含内嵌图片，用于判断是否需要替换img标签
	 * 
	 * @Description:
	 * @param hyperText
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-9-22
	 */
	// public static boolean containsInnerImage(String hyperText) {
	// p_script = Pattern.compile(HTML_INNER_IMAGE, Pattern.CASE_INSENSITIVE);
	// matcher = p_script.matcher(hyperText);
	// if (matcher.find()) {
	// LogX.v(TAG, "包含内嵌图片，需要替换img标签");
	// return true;
	// }
	// LogX.v(TAG, "不包含内嵌图片，不需要替换img标签");
	// return false;
	// }

	/**
	 * 内嵌图片的html标签替换成本地uri
	 * 
	 * @Description:
	 * @param text
	 * @param message
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-9-22
	 */
	// public static String processHyperTextOfInnerImage(LocalMessage message) {
	// String text = message.getHyperText();
	//
	// if (StringUtil.isEmpty(text)) {
	// return "";
	// }
	// List<Attachment> atts = message.getAttachments();
	// if (atts != null && atts.size() > 0) {
	// for (Attachment att : atts) {
	// if (att.isInLine() && !StringUtil.isEmpty(att.getFilePath())) {
	// Uri contentUri = Uri.parse(att.getFilePath());// 内嵌图片本地uri
	// String contentId = att.getContentId();
	// String contentIdRe = "\\s+(?i)src=\"cid(?-i):\\Q" + contentId + "\\E\"";
	// text = text.replaceAll(contentIdRe, " src=\"file:\\" + contentUri + "\"");
	// }
	// }
	// }
	//
	// return text;
	// }

	/**
	 * 把纯文本转换成html格式
	 * 
	 * @Description:
	 * @param text
	 * @return
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2013-9-27
	 */
	public static String pText2Html(String text) {
		Matcher m = Regex.WEB_URL_PATTERN.matcher(text);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			int start = m.start();
			if (start != 0 && text.charAt(start - 1) != '@') {
				m.appendReplacement(sb, "<a href=\"$0\">$0</a>");
			} else {
				m.appendReplacement(sb, "$0");
			}
		}
		m.appendTail(sb);
		int index = 0;
		while (index++ < 3) {
			try {
				StringBuffer strbuf = new StringBuffer();
				strbuf.append("<html><body>");
				strbuf.append(sb.toString().replaceAll("\r?\n", "<br>"));
				strbuf.append("</body></html>");
				text = strbuf.toString();
				sb = null;
				strbuf = null;
				break;
			} catch (RuntimeException e) {

			} finally {
				System.gc();
			}
		}
		return text;
	}

	/**
	 * html转换为纯文本
	 * 
	 * @Description:
	 * @param html
	 * @return
	 * @see:
	 * @since:
	 * @author: sunzhongquan
	 * @date:2014-4-25
	 */
	public static String htmlToText(final String html) {
		return Html.fromHtml(html, null, new HtmlToTextTagHandler()).toString().replace(PREVIEW_OBJECT_CHARACTER, PREVIEW_OBJECT_REPLACEMENT).replace(NBSP_CHARACTER, NBSP_REPLACEMENT);
	}

	/**
	 * Custom tag handler to use when converting HTML messages to text. It currently handles text
	 * representations of HTML tags that Android's built-in parser doesn't understand and hides code contained
	 * in STYLE and SCRIPT blocks.
	 */
	private static class HtmlToTextTagHandler implements Html.TagHandler {

		// List of tags whose content should be ignored.
		private static final Set<String> TAGS_WITH_IGNORED_CONTENT;
		static {
			Set<String> set = new HashSet<String>();
			set.add("style");
			set.add("script");
			set.add("title");
			set.add("!"); // comments
			TAGS_WITH_IGNORED_CONTENT = Collections.unmodifiableSet(set);
		}

		@Override
		public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
			tag = tag.toLowerCase(Locale.US);
			if (tag.equals("hr") && opening) {
				// In the case of an <hr>, replace it with a bunch of underscores. This is roughly
				// the behaviour of Outlook in Rich Text mode.
				output.append("_____________________________________________\r\n");
			} else if (TAGS_WITH_IGNORED_CONTENT.contains(tag)) {
				handleIgnoredTag(opening, output);
			}
		}

		private static final String IGNORED_ANNOTATION_KEY = "K9_ANNOTATION";
		private static final String IGNORED_ANNOTATION_VALUE = "hiddenSpan";

		/**
		 * When we come upon an ignored tag, we mark it with an Annotation object with a specific key and
		 * value as above. We don't really need to be checking these values since Html.fromHtml() doesn't use
		 * Annotation spans, but we should do it now to be safe in case they do start using it in the future.
		 * 
		 * @param opening
		 *            If this is an opening tag or not.
		 * @param output
		 *            Spannable string that we're working with.
		 */
		private void handleIgnoredTag(boolean opening, Editable output) {
			int len = output.length();
			if (opening) {
				output.setSpan(new Annotation(IGNORED_ANNOTATION_KEY, IGNORED_ANNOTATION_VALUE), len, len, Spannable.SPAN_MARK_MARK);
			} else {
				Object start = getOpeningAnnotation(output);
				if (start != null) {
					int where = output.getSpanStart(start);
					// Remove the temporary Annotation span.
					output.removeSpan(start);
					// Delete everything between the start of the Annotation and the end of the string
					// (what we've generated so far).
					output.delete(where, len);
				}
			}
		}

		/**
		 * Fetch the matching opening Annotation object and verify that it's the one added by K9.
		 * 
		 * @param output
		 *            Spannable string we're working with.
		 * @return Starting Annotation object.
		 */
		private Object getOpeningAnnotation(Editable output) {
			Object[] objs = output.getSpans(0, output.length(), Annotation.class);
			for (int i = objs.length - 1; i >= 0; i--) {
				Annotation span = (Annotation) objs[i];
				if (output.getSpanFlags(objs[i]) == Spannable.SPAN_MARK_MARK && span.getKey().equals(IGNORED_ANNOTATION_KEY) && span.getValue().equals(IGNORED_ANNOTATION_VALUE)) {
					return objs[i];
				}
			}
			return null;
		}
	}
}
