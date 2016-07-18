package cn.mailchat.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.Toast;

import cn.mailchat.R;
import cn.mailchat.mail.Address;
import cn.mailchat.utils.StringUtil;

/**
 * 邮件地址栏的输入监听
 * 
 * @Description:
 * @author:xulei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:Jun 4, 2013
 */
public class MailAddressWatcher implements TextWatcher {

	// private static final String TAG = "MailAddressWatcher";

	private AddressViewControl mControl;
	private Context mContext;

	public MailAddressWatcher(AddressViewControl control, Context context) {
		mControl = control;
		mContext = context;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		String text = s.toString();
		String newAddr = null;
		if (text != null && text.trim().length() > 0) {
			if ((text.endsWith(",") || text.endsWith(";") || text.endsWith(" ") || text
					.endsWith("\n"))) {
				if (text.contains(",")) {
					int Chatstart = text.indexOf(",");
					newAddr = text.substring(0, Chatstart);
				} else if (text.contains(";")) {
					int Chatstart = text.indexOf(";");
					newAddr = text.substring(0, Chatstart);
				} else if (text.contains(" ")) {
					int Chatstart = text.indexOf(" ");
					newAddr = text.substring(0, Chatstart);
				} else if (text.contains("\n")) {
					int Chatstart = text.indexOf("\n");
					newAddr = text.substring(0, Chatstart);
				}
				addAddressToView(newAddr);
			}else {
				mControl.addTextWatcherLintener(text);
			}

		} else {
			mControl.addTextWatcherLintener(text);
		}

	}

	private void addAddressToView(String textValue) {
		if (StringUtil.isValidEmailAddress(textValue.trim())) {
			mControl.addAddress(textValue);
		} else {
			Toast toast = Toast.makeText(mContext,
					R.string.message_compose_error_wrong_recipients,
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 6, 2);
			toast.show();
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		// String text = s.toString();
		// if (text.endsWith(",") || text.endsWith(";") || text.endsWith(" ") ||
		// text.endsWith("\n")) {
		// s.delete(s.length() - 1, s.length());
		// }
	}
}
