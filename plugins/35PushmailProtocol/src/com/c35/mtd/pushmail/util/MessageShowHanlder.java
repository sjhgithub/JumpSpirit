package com.c35.mtd.pushmail.util;

import java.util.LinkedList;
import java.util.List;

import android.text.Editable;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.c35.mtd.pushmail.Debug;

/**
 * @Description:
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-7-11
 */
public class MessageShowHanlder {
	private static final String TAG = "MessageShowHanlder";

	private MessageShowHanlder() {
	}

	public static class MailMatchResult {

		public String url = null;
		public int start = -1;
		public int end = -1;

		public MailMatchResult() {
			this(null, -1, -1);
		}

		public MailMatchResult(String url, int start, int end) {
			this.url = url;
			this.start = start;
			this.end = end;
		}
	}

	public static List<MailMatchResult> matchMail(Editable msgBody) {

		List<MailMatchResult> results = new LinkedList<MailMatchResult>();
		if (msgBody != null) {
			String[] mailAddress = msgBody.toString().split(",");
			int start = 0;
			int end = 0;
			for (int i = 0; i < mailAddress.length; i++) {
				if (i == 0) {
					start = 0;
					end = mailAddress[i].length();
				} else {
					start = start + mailAddress[i - 1].length() + 1;
					end = end + mailAddress[i].length() + 1;
				}

				MailMatchResult result = new MailMatchResult(mailAddress[i], start, end);
				results.add(result);
			}
		}

		return results;
	}

	public static void formatContent(TextView contentText) {

		final List<MailMatchResult> mailTags = matchMail(contentText.getEditableText());

		for (final MailMatchResult tag : mailTags) {
			CharacterStyle span = new ClickableSpan() {

				@Override
				public void onClick(View widget) {
					// MyIntent.startMail(MainActivity.instance, tag.url);
					Debug.d(TAG, "dianle");
				}
			};
			contentText.getEditableText().setSpan(span, tag.start, tag.end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			contentText.setMovementMethod(LinkMovementMethod.getInstance());
		}
	}
}
