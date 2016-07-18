package cn.mailchat.activity.setup;

import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.ChattingSingleActivity;
import cn.mailchat.activity.SetPasswordActivity;
import cn.mailchat.chatting.beans.DChat;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.preferences.Storage;
import cn.mailchat.utils.Base64Utils;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.utils.SystemUtil;

public class SelectEmailActivity extends Activity {
	//从中心获取账户，第一次直接跳转至登录页
	private static final String IS_SHOW_ONCE = "is_show_once";
	private LinearLayout mail_163;
	private LinearLayout mail_126;
	private LinearLayout mail_qq;
	private LinearLayout mail_gmail;
	private LinearLayout mail_aliyun;
	private LinearLayout mail_139;
	private LinearLayout mail_189;
	private LinearLayout mail_sina;
	private LinearLayout mail_outlook;
	private LinearLayout mail_add;
	private LinearLayout mail_35;
	private LinearLayout mail_eyou;
	private ImageView mail_google, mail_outlook_en, mail_aol, mail_icloud,
			mail_yahoo, mail_others;
	private TextView chatHelpTxt;
	/* 选择的Email */
	private String select_email;
	private int select;
	private String mEmail;
	private boolean isLogined=false;
	private MessagingController controller;
	public static void showSelectEmailActivity(Context context) {
		Intent intent = new Intent(context, SelectEmailActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		destJumpToLoginFor35Mail();
		setContentView(R.layout.activity_chose_mails);
		controller = MessagingController.getInstance(getApplication());
		initWidget();
		initTitleBar();
		initEvent();
		createHideAccount();
	}

	/**
	 * 
	 * method name: destJumpToLoginFor35Mail function @Description: TODO
	 * Parameters and return values description: field_name void return type
	 * 
	 * @History memory：
	 * @Date：2015-8-3 下午2:15:49 @Modified by：zhangjx
	 * @Description:如果中心数据库有35账户，默认获取该账户并直接跳转至登录页面 
	 * 第二次会判断是否已经登录，如果没有登录会跳转至登录页并填充账户信息
	 */
	private void destJumpToLoginFor35Mail() {
		if (isAccountIn35Center()) {
			isLogined();
			Storage storage = Storage.getStorage(this);
			boolean isShowOnce = storage.getBoolean(IS_SHOW_ONCE, false);
			if (!isLogined && !isShowOnce) {
				gotoLoginAct("", 1);
				Editor edit = storage.edit();
				edit.putBoolean(IS_SHOW_ONCE, true);
				edit.commit();
				finish();
			}
		}
	}
	private void initTitleBar() {
		ImageView imgBack = (ImageView) findViewById(R.id.back);
		imgBack.setVisibility(View.GONE);
		TextView tvTitle = (TextView) findViewById(R.id.title);
		tvTitle.setText(getString(R.string.select_email_login));
	}

	private void initWidget() {
		mail_163 = (LinearLayout) findViewById(R.id.mail_163);
		mail_126 = (LinearLayout) findViewById(R.id.mail_126);
		mail_qq = (LinearLayout) findViewById(R.id.mail_qq);
		mail_gmail = (LinearLayout) findViewById(R.id.mail_gmail);
		mail_aliyun = (LinearLayout) findViewById(R.id.mail_aliyun);
		mail_139 = (LinearLayout) findViewById(R.id.mail_139);
		mail_189 = (LinearLayout) findViewById(R.id.mail_189);
		mail_sina = (LinearLayout) findViewById(R.id.mail_sina);
		mail_outlook = (LinearLayout) findViewById(R.id.mail_outlook);
		mail_add = (LinearLayout) findViewById(R.id.mail_add);
		mail_35 = (LinearLayout) findViewById(R.id.mail_35);
		mail_eyou = (LinearLayout) findViewById(R.id.mail_eyou);
		mail_google = (ImageView) findViewById(R.id.mail_google);
		mail_outlook_en = (ImageView) findViewById(R.id.mail_outlook_en);
		mail_aol = (ImageView) findViewById(R.id.mail_aol);
		mail_icloud = (ImageView) findViewById(R.id.mail_icloud);
		mail_yahoo = (ImageView) findViewById(R.id.mail_yahoo);
		mail_others = (ImageView) findViewById(R.id.mail_others);
		chatHelpTxt = (TextView) findViewById(R.id.tv_chat_help);
	}

	private void initEvent() {
		mail_163.setOnClickListener(clicklisener);
		mail_126.setOnClickListener(clicklisener);
		mail_qq.setOnClickListener(clicklisener);
		mail_gmail.setOnClickListener(clicklisener);
		mail_aliyun.setOnClickListener(clicklisener);
		mail_139.setOnClickListener(clicklisener);
		mail_189.setOnClickListener(clicklisener);
		mail_sina.setOnClickListener(clicklisener);
		mail_outlook.setOnClickListener(clicklisener);
		mail_add.setOnClickListener(clicklisener);
		mail_35.setOnClickListener(clicklisener);
		mail_eyou.setOnClickListener(clicklisener);
		mail_google.setOnClickListener(clicklisener);
		mail_outlook_en.setOnClickListener(clicklisener);
		mail_aol.setOnClickListener(clicklisener);
		mail_icloud.setOnClickListener(clicklisener);
		mail_yahoo.setOnClickListener(clicklisener);
		mail_others.setOnClickListener(clicklisener);
		chatHelpTxt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Account mAccount = Preferences.getPreferences(getApplication()).getDefaultAccount();
				if (mAccount == null) {
					return;
				}
				DChat dchat = controller.getHelpDChat(mAccount);
				ChattingSingleActivity.actionChatList(SelectEmailActivity.this, dchat, mAccount);
			}
		});
	}

	private OnClickListener clicklisener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.mail_163:
				select_email = "@163.com";
				MobclickAgent.onEvent(SelectEmailActivity.this, "login_type_mail_163");
				break;
			case R.id.mail_126:
				select_email = "@126.com";
				MobclickAgent.onEvent(SelectEmailActivity.this, "login_type_mail_126");
				break;
			case R.id.mail_qq:
				select_email = "@qq.com";
				MobclickAgent.onEvent(SelectEmailActivity.this, "login_type_mail_qq");
				break;
			case R.id.mail_google:
			case R.id.mail_gmail:
				select_email = "@gmail.com";
				MobclickAgent.onEvent(SelectEmailActivity.this, "login_type_mail_google");
				break;
			case R.id.mail_aliyun:
				select_email = "@aliyun.com";
				MobclickAgent.onEvent(SelectEmailActivity.this, "login_type_mail_aliyun");
				break;
			case R.id.mail_139:
				select_email = "@139.com";
				MobclickAgent.onEvent(SelectEmailActivity.this, "login_type_mail_139");
				break;
			case R.id.mail_189:
				select_email = "@189.cn";
				MobclickAgent.onEvent(SelectEmailActivity.this, "login_type_mail_189");
				break;
			case R.id.mail_sina:
				select_email = "@sina.com";
				MobclickAgent.onEvent(SelectEmailActivity.this, "login_type_mail_sina");
				break;
			case R.id.mail_outlook_en:
			case R.id.mail_outlook:
				select_email = "@outlook.com";
				MobclickAgent.onEvent(SelectEmailActivity.this, "login_type_mail_outlook");
				break;
			case R.id.mail_others:
			case R.id.mail_add:
				select_email = "";
				MobclickAgent.onEvent(SelectEmailActivity.this, "login_type_mail_other");
				break;
			case R.id.mail_35:
				select_email = "";
				select = 1;// 三五为1
				MobclickAgent.onEvent(SelectEmailActivity.this, "login_type_enterprise_mail");
				break;
			case R.id.mail_eyou:
				select_email = "";
				select = 2;// eyou为2
				MobclickAgent.onEvent(SelectEmailActivity.this, "login_type_mail_eyou");
				break;
			case R.id.mail_aol:
				select_email = "";
				MobclickAgent.onEvent(SelectEmailActivity.this, "login_type_mail_aol");
				break;
			case R.id.mail_icloud:
				select_email = "";
				MobclickAgent.onEvent(SelectEmailActivity.this, "login_type_mail_iclude");
				break;
			case R.id.mail_yahoo:
				select_email = "@yahoo.com";
				MobclickAgent.onEvent(SelectEmailActivity.this, "login_type_mail_yahoo");
				break;
			}
			/** 启动账户设置 */
			gotoLoginAct(select_email,select);
			SelectEmailActivity.this.finish();
		}
	};


	private void gotoLoginAct(String select_email, int select) {
		AccountSetupBasics.actionNewAccount(SelectEmailActivity.this,
				select_email, select);
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		if (MailChat.isGesture() && MailChat.isGestureUnclock()
				&& SetPasswordActivity.ifHasGPassword()) {
			SetPasswordActivity.startActivity(this, true, false, false, false,
					false);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void createHideAccount() {
		Preferences prefs = Preferences.getPreferences(getApplication());
		List<Account> accounts = prefs.getAccounts();
		if (accounts.size() == 0) {
			Preferences preferences = Preferences.getPreferences(this);
			String email = SystemUtil.getCliendId(this)
					+ GlobalConstants.HIDE_ACCOUNT_SUFFIX;
			Account mAccount = preferences.newAccount(email);
			mAccount.setName(email);
			mAccount.setEmail(email);
			mAccount.setHideAccount(true);
			try {
				String userEnc = URLEncoder.encode("mc@35.cn", "UTF-8");
				String passwordEnc = URLEncoder.encode("123456", "UTF-8");
				String imapScheme = "imap";
				int imapPost = 143;
				String smtpScheme = "smtp";
				int smtpPost = 25;
				URI inUri = new URI(imapScheme, userEnc + ":" + passwordEnc,
						"mail.35.cn", imapPost, null, null, null);
				URI outUri = new URI(smtpScheme, userEnc + ":" + passwordEnc,
						"mail.35.cn", smtpPost, null, null, null);
				mAccount.setStoreUri(inUri.toString());
				mAccount.setTransportUri(outUri.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mAccount.setDraftsFolderName(getString(R.string.special_mailbox_name_drafts));
			mAccount.setTrashFolderName(getString(R.string.special_mailbox_name_trash));
			mAccount.setSentFolderName(getString(R.string.special_mailbox_name_sent));
			mAccount.setArchiveFolderName(getString(R.string.special_mailbox_name_archive));
			mAccount.setLoginSuccessedAccount(true);
			mAccount.save(preferences);
			Preferences.getPreferences(this).setDefaultAccount(mAccount);
			try {
				mAccount.getLocalStore().savaContact(GlobalConstants.HELP_ACCOUNT_EMAIL, getString(R.string.mailchat_help),false);
				controller.syncRemoteDChatUserInfo(mAccount, GlobalConstants.HELP_ACCOUNT_EMAIL);
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 35用户中心是否有账号信息
	 */
	private boolean isAccountIn35Center() {
		ContentResolver cr = getContentResolver();
		if (cr==null) {
			return false;
		}
		Uri myUri = Uri.parse(GlobalConstants.CONTENT_URI);
		Cursor cursor=null;
        try {
            cursor = cr.query(myUri, null, null, null, null);
        } catch (Exception e) {
            return false;
        }
		if (cursor == null) {
			return false;
		} else {
			while (cursor.moveToNext()) {
				mEmail = cursor.getString(cursor.getColumnIndex("email"));
			}
			cursor.close();
			return true;
		}
	}

	private void isLogined() {
		if (StringUtils.isNullOrEmpty(mEmail)) {
			isLogined= true;
			return;
		}
		List<Account> accounts = Preferences.getPreferences(
				SelectEmailActivity.this).getAccounts();
		if (accounts.size() > 0) {
			for (int i = 0; i < accounts.size(); i++) {
				if (accounts.get(i).getEmail().toString()
						.equals(Base64Utils.getFromBASE64(mEmail))) {
					isLogined = true;
					return;
				} else {
					isLogined = false;
				}
			}
		}
	}
}
