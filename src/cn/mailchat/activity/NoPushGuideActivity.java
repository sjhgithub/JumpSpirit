package cn.mailchat.activity;

import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.chatting.beans.DChat;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.utils.SystemUtil;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class NoPushGuideActivity extends BaseActionbarFragmentActivity {
	private TextView titleTv, step1Tv, step2Tv, step3Tv, step4Tv,helpTv,noRemindTv;

	private View mCustomActionbarView;
	private TextView mTitle;
	private Account mAccount;
	private MessagingController controller;
	private SharedPreferences prefs;
	public static void actionNoPushGuideActivity(Context mContext,Account account) {
		Intent tIntent = new Intent(mContext, NoPushGuideActivity.class);
		tIntent.putExtra(GlobalConstants.ACCOUNTUUID, account.getUuid());
		mContext.startActivity(tIntent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_no_push_guide);
		initializeActionBar();
		initView();
		initData();
	}

	private void initView() {
		titleTv = (TextView) findViewById(R.id.tv_phone_push_help_title);
		step1Tv = (TextView) findViewById(R.id.tv_phone_push_help_step_1);
		step2Tv = (TextView) findViewById(R.id.tv_phone_push_help_step_2);
		step3Tv = (TextView) findViewById(R.id.tv_phone_push_help_step_3);
		step4Tv = (TextView) findViewById(R.id.tv_phone_push_help_step_4);
		helpTv = (TextView) findViewById(R.id.tv_help);
		noRemindTv = (TextView) findViewById(R.id.tv_no_remind);
	}

	private void initData() {
		prefs = getSharedPreferences("mqtt_ping",Context.MODE_PRIVATE);
		mAccount=Preferences.getPreferences(this).getDefaultAccount();
		controller = MessagingController.getInstance(getApplication());
		if (SystemUtil.isHuaWei()) {
			titleTv.setText(String.format(
					getString(R.string.phone_push_help_title), "华为"));
			step1Tv.setText(getString(R.string.huawei_phone_push_help_step_1));
			step2Tv.setText(getString(R.string.huawei_phone_push_help_step_2));
			step3Tv.setText(getString(R.string.huawei_phone_push_help_step_3));
		} else if (SystemUtil.isMIUI()) {
			titleTv.setText(String.format(
					getString(R.string.phone_push_help_title), "小米"));
			step1Tv.setText(getString(R.string.xiaomi_phone_push_help_step_1));
			step2Tv.setText(getString(R.string.xiaomi_phone_push_help_step_2));
			step3Tv.setText(getString(R.string.xiaomi_phone_push_help_step_3));
			step4Tv.setVisibility(View.VISIBLE);
			step4Tv.setText(getString(R.string.xiaomi_phone_push_help_step_4));
		} else if (SystemUtil.isFlyme()) {
			titleTv.setText(String.format(
					getString(R.string.phone_push_help_title), "魅族"));
			step1Tv.setText(getString(R.string.meizu_phone_push_help_step_1));
			step2Tv.setText(getString(R.string.meizu_phone_push_help_step_2));
			step3Tv.setText(getString(R.string.meizu_phone_push_help_step_3));
		} else {
			titleTv.setText(R.string.other_phone_push_help_title);
			step1Tv.setText(getString(R.string.other_phone_push_help_step_1));
			step2Tv.setVisibility(View.GONE);
			step3Tv.setVisibility(View.GONE);
		}

        helpTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DChat dchat = controller.getHelpDChat(mAccount);
				ChattingSingleActivity.actionChatList(NoPushGuideActivity.this, dchat, mAccount);
			}
		});

        noRemindTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				saveNoRemind(true);
				Toast.makeText(NoPushGuideActivity.this, R.string.no_remind_success, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void initializeActionBar() {
		mActionBar.setTitle(null);
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayUseLogoEnabled(true);
		// 返回按钮
		mActionBar.setDisplayHomeAsUpEnabled(true);
		// Inflate the custom view
		LayoutInflater inflater = LayoutInflater.from(this);
		mCustomActionbarView = inflater.inflate(
				R.layout.actionbar_custom_only_title, null);
		mActionBar.setCustomView(mCustomActionbarView);
		mTitle = (TextView) mCustomActionbarView.findViewById(R.id.tv_title);
		setActionbarCenterTitle(mCustomActionbarView, mTitle,
				getString(R.string.no_push_service_activity_title));
	}

	/**
	 * 记录不再提醒标记
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-12-3
	 */
	private void saveNoRemind(boolean isNoRemind){
		Editor editor =prefs.edit();
		editor.putBoolean("isNoRemind", isNoRemind);
		editor.commit();
	}
}
