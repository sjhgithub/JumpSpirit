package cn.mailchat.view;

import cn.mailchat.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

public class DialogSetUpUserInfo extends Dialog {
	Context context;
	int theme;

	public DialogSetUpUserInfo(Context context) {
		super(context);
		this.context = context;
	}

	public DialogSetUpUserInfo(Context context, int theme) {
		super(context, theme);
		this.context = context;
		this.theme = theme;
	}
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_setup_user_info);
	}


}
