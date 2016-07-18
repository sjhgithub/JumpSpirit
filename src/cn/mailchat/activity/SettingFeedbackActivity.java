package cn.mailchat.activity;

import java.io.File;
import java.util.Date;

import org.apache.james.mime4j.util.MimeUtil;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.LogCollector;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.mail.Address;
import cn.mailchat.mail.Flag;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.mail.Message.RecipientType;
import cn.mailchat.mail.internet.MimeBodyPart;
import cn.mailchat.mail.internet.MimeHeader;
import cn.mailchat.mail.internet.MimeMessage;
import cn.mailchat.mail.internet.MimeMultipart;
import cn.mailchat.mail.internet.TextBody;
import cn.mailchat.mail.store.LocalStore.TempFileBody;
import cn.mailchat.utils.CommonUtils;
import cn.mailchat.utils.SystemUtil;

public class SettingFeedbackActivity extends BaseActionbarFragmentActivity
		implements OnClickListener {
	private EditText feedback;
	// private LinearLayout layoutBack;
	// private TextView operation;
	private MessagingController controller;
	private Account mAccount;
	private View mCustomActionbarView;
	private TextView btnSure;
	private TextView mActionbarTitle, mActionbarSure;

	public static void actionView(Context context) {
		Intent intent = new Intent(context, SettingFeedbackActivity.class);
		context.startActivity(intent);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);
		controller = MessagingController.getInstance(getApplication());
		initializeActionBar();
		initView();
		initActionbarView();
		mAccount = Preferences.getPreferences(this).getDefaultAccount();
	}

	private void initializeActionBar() {
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayUseLogoEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setCustomView(R.layout.actionbar_custom_right_btn);
		mCustomActionbarView = mActionBar.getCustomView();
		mActionBar.setTitle(null);
		btnSure = (TextView) mCustomActionbarView.findViewById(R.id.tv_sure);
	}

	private void initActionbarView() {
		// TODO Auto-generated method stub
		mActionbarTitle = (TextView) mCustomActionbarView
				.findViewById(R.id.tv_title);
		mActionbarSure = (TextView) mCustomActionbarView
				.findViewById(R.id.tv_sure);
		setActionbarCenterTitle(mCustomActionbarView, mActionbarTitle,
				getResources().getString(R.string.feedback_title));

		mActionbarSure.setOnClickListener(this);
	}

	private void initView() {
		feedback = (EditText) findViewById(R.id.feedback);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.layout_back:
			finish();
			CommonUtils.hideSoftInput(this);
			break;
		case R.id.tv_sure:
			String content = feedback.getText().toString().trim();
			if (content == null || "".equals(content)) {
				Toast.makeText(SettingFeedbackActivity.this,
						getString(R.string.feedback_empty), Toast.LENGTH_LONG)
						.show();
			} else {
				// controller.feedBack(mAccount, content);
				try {
					sendFeedback(MailChat.BUG_EMAIL, content);
				} catch (MessagingException e) {
					Log.e(MailChat.LOG_TAG, "Send feedback failed", e);
				}
				Toast.makeText(SettingFeedbackActivity.this,
						getString(R.string.feedback_succee), Toast.LENGTH_LONG)
						.show();
				finish();
				CommonUtils.hideSoftInput(this);
			}
			break;
		// case R.id.layout_right_operation :
		// controller.feedBack(feedback.getText().toString());
		// Toast.makeText(SettingFeedbackActivity.this,
		// getString(R.string.feedback_succee),
		// Toast.LENGTH_LONG).show();
		// finish();
		// break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void sendFeedback(String recipient, String content)
			throws MessagingException {
		MessagingController messagingController = MessagingController.getInstance(MailChat.app);
		Account account = Preferences.getPreferences(this).getDefaultAccount();

		MimeMessage message = new MimeMessage();
		message.setSendMessage(true);
		message.addSentDate(new Date());
		message.setFrom(new Address(account.getEmail()));
		message.setRecipient(RecipientType.TO, new Address(recipient));
		message.setSubject(getString(R.string.feedback_start)
				+ account.getEmail() + getString(R.string.feedback_end));

		MimeMultipart mp = new MimeMultipart();
		mp.addBodyPart(new MimeBodyPart(new TextBody(buildFeedbackBody(content)),
		        "text/plain"));
		
		try {
    		File logFile = new File(MailChat.app.getFilesDir(), LogCollector.LOG_FILE);
    		LogCollector.saveLog(logFile);
    		TempFileBody tfb = new TempFileBody(logFile.getAbsolutePath());
            MimeBodyPart mbp = new MimeBodyPart(tfb);
            mbp.addHeader(MimeHeader.HEADER_CONTENT_TYPE,
                    String.format("application/octet-stream;\r\n name=\"%s\"", LogCollector.LOG_FILE));
            mbp.setEncoding(MimeUtil.ENC_BASE64);
            mbp.addHeader(MimeHeader.HEADER_CONTENT_DISPOSITION,
                    String.format("attachment;\r\n filename=\"%s\"", LogCollector.LOG_FILE));
            mp.addBodyPart(mbp);
		} catch (Exception e) {
		    Log.e(MailChat.LOG_TAG, e.toString());
		}
		
		message.setBody(mp);
		message.setFlag(Flag.X_DOWNLOADED_FULL, true);

		if (!messagingController.sendMessage(account, message, null)) {
		    MailChat.toast(getString(R.string.feedback_failed));
            Log.e(MailChat.LOG_TAG, "Send feedback failed");
		}
	}

	private String buildFeedbackBody(String content) {
		StringBuilder body = new StringBuilder();
		body.append(content + "\r\n");
		body.append("***************************** \r\n");
		body.append(String.format("%-20s%s%n", "Board:", Build.BOARD));
		body.append(String.format("%-20s%s%n", "Brand:", Build.BRAND));
		body.append(String.format("%-20s%s%n", "Device:", Build.DEVICE));
		body.append(String.format("%-20s%s%n", "Display:", Build.DISPLAY));
		body.append(String.format("%-20s%s%n", "Fingerprint:",
				Build.FINGERPRINT));
		body.append(String.format("%-20s%s%n", "Hardware:", Build.HARDWARE));
		body.append(String.format("%-20s%s%n", "Host:", Build.HOST));
		body.append(String.format("%-20s%s%n", "ID:", Build.ID));
		body.append(String.format("%-20s%s%n", "Manufacturer:",
				Build.MANUFACTURER));
		body.append(String.format("%-20s%s%n", "Model:", Build.MODEL));
		body.append(String.format("%-20s%s%n", "Product:", Build.PRODUCT));
		body.append(String.format("%-20s%s%n", "Radio:",
				Build.getRadioVersion()));
		body.append(String.format("%-20s%s%n", "Serial:", Build.SERIAL));
		body.append(String.format("%-20s%s%n", "Tags:", Build.TAGS));
		body.append(String.format("%-20s%s%n", "Time:", Build.TIME));
		body.append(String.format("%-20s%s%n", "Type:", Build.TYPE));
		body.append(String.format("%-20s%s%n", "User:", Build.USER));

		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		body.append(String.format("%-20s%s%n", "IMEI:",
				telephonyManager.getDeviceId()));

		body.append(String.format("%-20s%s%n", "MailChat:",
				getString(R.string.version_name)));

		return body.toString();

	}
}
