package cn.mailchat.utils;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

public class CommonUtils {

	/**
	 * 隐藏软键盘
	 * 
	 * @param context
	 * @return
	 */
	public static boolean hideSoftInput(Activity context) {
		try {
			InputMethodManager inputMethodManager = (InputMethodManager) context
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (inputMethodManager != null && context.getCurrentFocus() != null
					&& context.getCurrentFocus().getWindowToken() != null) {
				return inputMethodManager.hideSoftInputFromWindow(context.getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		} catch (Exception e) {
		}
		return false;
	}
}
