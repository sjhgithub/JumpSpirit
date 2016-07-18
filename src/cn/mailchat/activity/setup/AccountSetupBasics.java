package cn.mailchat.activity.setup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Documented;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.EmailAddressValidator;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.ChattingSingleActivity;
import cn.mailchat.activity.MailChatActivity;
import cn.mailchat.activity.WebViewWithErrorViewActivity;
import cn.mailchat.activity.setup.AccountSetupCheckSettings.CheckDirection;
import cn.mailchat.adapter.AccountAutoCompleteAdapter;
import cn.mailchat.beans.ImapAndSmtpSetting;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.helper.UrlEncodingHelper;
import cn.mailchat.helper.Utility;
import cn.mailchat.utils.Base64Utils;
import cn.mailchat.utils.CommonUtils;
import cn.mailchat.utils.GlobalTools;
import cn.mailchat.utils.NetUtil;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.view.GenericDialog;
import cn.mailchat.view.LoginDialog;

import com.umeng.analytics.MobclickAgent;

/**
 * 
 * @copyright © 35.com
 * @file name ：AccountSetupBasics.java
 * @author ：zhangjx
 * @create Data ：2014-11-18下午3:44:02
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-11-18下午3:44:02
 * @Modified by：zhangjx
 * @Description :登录页面完善
 */
public class AccountSetupBasics extends MailChatActivity {
	private final static String EXTRA_ACCOUNT = "cn.mailchat.AccountSetupBasics.account";
	private final static int DIALOG_NOTE = 1;
	private final static String STATE_KEY_PROVIDER = "cn.mailchat.AccountSetupBasics.provider";
	private final static String STATE_KEY_CHECKED_INCOMING = "cn.mailchat.AccountSetupBasics.checkedIncoming";
	// 地址框输入的字数
	public static int addresscharts;
	private static String emailPostfix = "";// 后缀名
	private static TextView email_postfix; // 显示后缀名容器
	private static AutoCompleteTextView mEmailView;/* 账号 */
	private EditText mPasswordView;/* 密码 */
	private TextView mRegister;
	private TextView mForgotPassword;
	private Account mAccount;
	private Provider mProvider;
	private ImageView img_emai_empty;
	private Button loginButton;
	private View email_view_1;
	private View email_view_2;
	private View email_view_3;
	private View password_view_1;
	private View password_view_2;
	private View password_view_3;
	// 验证邮箱格式
	private EmailAddressValidator mEmailValidator = new EmailAddressValidator();
	private boolean mCheckedIncoming = false;
	private MessagingController controller;
	private TextView help,tvLoginFailedHelp;
	private String currentEmail;
	private ProgressDialog dialog;
	private boolean is35Email;
	private boolean isEYouEmail;
	private int select_flag;
	private Handler mHander =new Handler();
	private String password;
	private TextView helpTitle;
	private CheckBox mShowPasswordCheckBox;
	private String centerEmail,centerPwd;
	/**
	 * 其他邮箱登陆
	 * 
	 * @param context
	 */
	public static void actionNewAccount(Context context) {
		Intent i = new Intent(context, AccountSetupBasics.class);
		Bundle bundle = new Bundle();// 创建Bundle对象
		bundle.putString("select_email", "None"); // 装入值
		i.putExtras(bundle);
		context.startActivity(i);
	}

	/**
	 * 常用邮箱登陆
	 * 
	 * @param context
	 * @param select_email
	 */
	public static void actionNewAccount(Context context, String select_email,int select) {
		Intent i = new Intent(context, AccountSetupBasics.class);
		Bundle bundle = new Bundle();// 创建Bundle对象
		bundle.putString("select_email", select_email);// 装入数据
		bundle.putInt("select_flag", select);//0为常用邮箱,1为三五企业入口,2为亿邮入口
		i.putExtras(bundle);
		context.startActivity(i);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置无标题
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_setup_basics);
		decodeExtras();
		initWidget();
		showSoftInput();
		initTitleBar();
		initEvent();
		controller = MessagingController.getInstance(getApplication());
		controller.addListener(listener);
	}

	private void showSoftInput() {
		new Timer().schedule(new TimerTask(){
			public void run(){
				InputMethodManager inputManager =
				(InputMethodManager) mEmailView.getContext().getSystemService(
						Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(mEmailView, 0);
			}
		},
		1*1000);
	}

	private void decodeExtras() {
		Intent intent = this.getIntent(); // 获取已有的intent对象
		Bundle bundle = intent.getExtras(); // 获取intent里面的bundle对象
		emailPostfix = bundle.getString("select_email"); // 获取Bundle里面的字符串
		select_flag = bundle.getInt("select_flag");
		if(select_flag==0){
		}else if(select_flag==1){
			is35Email=true;
			getAccountFrom35UserCenter();
			isLogined();
		}else if(select_flag==2){
			isEYouEmail=true;
		}
	}

	private void initTitleBar() {
		ImageView imgBack = (ImageView) findViewById(R.id.back);
		imgBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mEmailView.length() != 0){
					mEmailView.setText("");
				}
				SelectEmailActivity
						.showSelectEmailActivity(AccountSetupBasics.this);
				finish();
				CommonUtils.hideSoftInput(AccountSetupBasics.this);
			}
		});
		TextView tvTitle = (TextView) findViewById(R.id.title);
		tvTitle.setText(getString(R.string.account_setup_basics_title));
	}

	private void initWidget() {
	    mRegister = (TextView) findViewById(R.id.register);
	    mRegister.setVisibility(View.VISIBLE);
	    
	    mForgotPassword = (TextView) findViewById(R.id.forgot_password);
	    
		mEmailView = (AutoCompleteTextView) findViewById(R.id.account_email);
		mEmailView.setPadding(
				GlobalTools.dip2px(AccountSetupBasics.this, 10), 0,
				GlobalTools.dip2px(AccountSetupBasics.this, 35),
				GlobalTools.dip2px(AccountSetupBasics.this, 7));
		
		if (StringUtil.isEmpty(emailPostfix)) {
			ArrayList<String> arrayAccounts = new ArrayList<String>();
			AccountAutoCompleteAdapter adapter = new AccountAutoCompleteAdapter(AccountSetupBasics.this, arrayAccounts,null);
			mEmailView.setAdapter(adapter);
		}

		mPasswordView = (EditText) findViewById(R.id.account_password);
		if ("@163.com".equals(emailPostfix)
		        || "@126.com".equals(emailPostfix)
		        || "@qq.com".equals(emailPostfix)) {
		    mPasswordView.setHint(getString(R.string.authorization_code_hint));
		}

		mShowPasswordCheckBox = (CheckBox) findViewById(R.id.show_password);
		email_postfix = (TextView) findViewById(R.id.email_postfix);
		email_view_1 = findViewById(R.id.email_view_1);
		email_view_2 = findViewById(R.id.email_view_2);
		email_view_3 = findViewById(R.id.email_view_3);
		password_view_1 = findViewById(R.id.password_view_1);
		password_view_2 = findViewById(R.id.password_view_2);
		password_view_3 = findViewById(R.id.password_view_3);
		/* 重置账号信息 */
		img_emai_empty = (ImageView) findViewById(R.id.img_emai_empty);
		loginButton = (Button) findViewById(R.id.login_button_login);
		help = (TextView) findViewById(R.id.tv_login_prompt);
		LinearLayout imapLayoutTips=(LinearLayout) findViewById(R.id.layout_imap_tips);
		tvLoginFailedHelp = (TextView) findViewById(R.id.tv_login_failed_help);
//		help.setText(Html.fromHtml(getString(R.string.login_help_prompt)+" <font color=\"#41aae0\"> "+getString(R.string.login_help)+ " </font>"));
//		help.setText(Html.fromHtml(getString(R.string.login_help_prompt)+"<a href='http://www.mailchat.cn/mailchat.html#imap'style='text-decoration:none;'> <font color=\"#41aae0\"> "+getString(R.string.login_help)+ " </font></a>"));
//		help.setMovementMethod(LinkMovementMethod.getInstance());
		ClickableSpan clickableSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				if (widget instanceof TextView) {
					String seretUrl = "http://www.mailchat.cn/mailchat.html#imap";
					// 跳转到帮助
					WebViewWithErrorViewActivity.forwardOpenUrlActivity(AccountSetupBasics.this,
					        seretUrl, null, null, -1,false);
				}
			}
		};
		setClickableSpanForTextView(help,clickableSpan,getString(R.string.login_help_prompt)
				+ getString(R.string.login_help),getString(R.string.login_help_prompt).length(),
				getString(R.string.login_help_prompt).length()+ getString(R.string.login_help).length()); 
		dialog=new ProgressDialog(this);
		setHelpLayout(imapLayoutTips);
		//如果是35邮箱，去账户中心去账户
		if (is35Email && !isLogined) {
			mEmailView.setText(Base64Utils.getFromBASE64(centerEmail));
			mPasswordView.setText(Base64Utils.getFromBASE64(centerPwd));
		}
	}

	private void setHelpLayout(LinearLayout imapLayoutTips) {
		LinearLayout helpLayout = (LinearLayout) findViewById(R.id.layout_help);
		 helpTitle = (TextView) findViewById(R.id.tv_help_title);
		ImageView helpImg = (ImageView) findViewById(R.id.img_set_imp_help);
		if (emailPostfix.contains("163")) {
			imapLayoutTips.setVisibility(View.GONE);
			helpLayout.setVisibility(View.VISIBLE);
			helpTitle.setText("163" + getString(R.string.open_imap_help));
			helpImg.setBackgroundResource(R.drawable.bg_163_setinfo);
		} else if (emailPostfix.contains("qq")) {
			imapLayoutTips.setVisibility(View.GONE);
			helpLayout.setVisibility(View.VISIBLE);
			helpTitle.setText("qq" + getString(R.string.open_imap_help));
			helpImg.setBackgroundResource(R.drawable.bg_qq_setinfo);
		} else if (emailPostfix.contains("sina")) {
			imapLayoutTips.setVisibility(View.GONE);
			helpLayout.setVisibility(View.VISIBLE);
			helpTitle.setText("sina" + getString(R.string.open_imap_help));
			helpImg.setBackgroundResource(R.drawable.bg_sina_setinfo);
		}else if (emailPostfix.contains("126")) {
			imapLayoutTips.setVisibility(View.GONE);
			helpLayout.setVisibility(View.VISIBLE);
			helpTitle.setText("126" + getString(R.string.open_imap_help));
			helpImg.setBackgroundResource(R.drawable.bg_163_setinfo);
		}else if (emailPostfix.contains("yeah")) {
			imapLayoutTips.setVisibility(View.GONE);
			helpLayout.setVisibility(View.VISIBLE);
			helpTitle.setText("yeah" + getString(R.string.open_imap_help));
			helpImg.setBackgroundResource(R.drawable.bg_163_setinfo);
		}else if (emailPostfix.contains("139")) {
			imapLayoutTips.setVisibility(View.GONE);
			helpLayout.setVisibility(View.VISIBLE);
			helpTitle.setText("139" + getString(R.string.open_imap_help));
			helpImg.setBackgroundResource(R.drawable.bg_139_setinfo);
		}else if (emailPostfix.contains("aliyun")) {
			imapLayoutTips.setVisibility(View.GONE);
			helpLayout.setVisibility(View.VISIBLE);
			helpTitle.setText( getString(R.string.title_aliyun_login_help) + getString(R.string.open_imap_help));
			helpImg.setBackgroundResource(R.drawable.bg_aliyun_setinfo);
		}else if (is35Email) {
			imapLayoutTips.setVisibility(View.GONE);
		}
//		else {
//			LinearLayout layoutTop = (LinearLayout) findViewById(R.id.layout_top);
//			ScrollView s= (ScrollView) findViewById(R.id.scrollView);
//			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
//					s.getLayoutParams());
//			p.height=LayoutParams.MATCH_PARENT;
//			s.setLayoutParams(p);
//				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
//						layoutTop.getLayoutParams());
//				params.gravity = Gravity.CENTER_VERTICAL;
//				layoutTop.setLayoutParams(params);
//		}
	}
	private void initEvent() {
		/** 点击事件 **/
	    mRegister.setOnClickListener(clicklistener);
	    mForgotPassword.setOnClickListener(clicklistener);
		loginButton.setOnClickListener(clicklistener);// 登陆
		img_emai_empty.setOnClickListener(clicklistener);// 清空账号信息
		tvLoginFailedHelp.setOnClickListener(clicklistener);
		/** EditText焦点事件 **/
		mEmailView.setOnFocusChangeListener(focusListener);// email
		mPasswordView.setOnFocusChangeListener(focusListener);// password
		mShowPasswordCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						showPassword(isChecked);
					}
				});
		// 邮箱后缀名设置
		if (emailPostfix.equals("@sina.com")) {
			List<String>  sinaEmails=new ArrayList<String>();
			sinaEmails.add("sina.com");
			sinaEmails.add("sina.cn");
			email_postfix.setVisibility(View.GONE);
			mEmailView.setPadding(
					GlobalTools.dip2px(AccountSetupBasics.this, 10), 0,
					GlobalTools.dip2px(AccountSetupBasics.this, 35),
					GlobalTools.dip2px(AccountSetupBasics.this, 7));
			ArrayList<String> arrayAccounts = new ArrayList<String>();
			AccountAutoCompleteAdapter adapter = new AccountAutoCompleteAdapter(
					AccountSetupBasics.this, arrayAccounts,sinaEmails);
			mEmailView.setAdapter(adapter);
			mEmailView
					.setDropDownBackgroundResource(R.drawable.shape_edit_bg_normal_nopadding);
		}else if (emailPostfix.equals("@aliyun.com")) {
			List<String>  sinaEmails=new ArrayList<String>();
			sinaEmails.add("aliyun.com");
			email_postfix.setVisibility(View.GONE);
			mEmailView.setPadding(
					GlobalTools.dip2px(AccountSetupBasics.this, 10), 0,
					GlobalTools.dip2px(AccountSetupBasics.this, 35),
					GlobalTools.dip2px(AccountSetupBasics.this, 7));
			ArrayList<String> arrayAccounts = new ArrayList<String>();
			AccountAutoCompleteAdapter adapter = new AccountAutoCompleteAdapter(
					AccountSetupBasics.this, arrayAccounts,sinaEmails);
			mEmailView.setAdapter(adapter);
			mEmailView
					.setDropDownBackgroundResource(R.drawable.shape_edit_bg_normal_nopadding);
		}else if (emailPostfix.equals("@qq.com")) {
			List<String>  sinaEmails=new ArrayList<String>();
			sinaEmails.add("qq.com");
			sinaEmails.add("foxmail.com");
			email_postfix.setVisibility(View.GONE);
			mEmailView.setPadding(
					GlobalTools.dip2px(AccountSetupBasics.this, 10), 0,
					GlobalTools.dip2px(AccountSetupBasics.this, 35),
					GlobalTools.dip2px(AccountSetupBasics.this, 7));
			ArrayList<String> arrayAccounts = new ArrayList<String>();
			AccountAutoCompleteAdapter adapter = new AccountAutoCompleteAdapter(
					AccountSetupBasics.this, arrayAccounts,sinaEmails);
			mEmailView.setAdapter(adapter);
			mEmailView
					.setDropDownBackgroundResource(R.drawable.shape_edit_bg_normal_nopadding);
		}else if (emailPostfix.equals("@outlook.com")) {
			List<String>  sinaEmails=new ArrayList<String>();
			sinaEmails.add("outlook.com");
			sinaEmails.add("hotmail.com");
			sinaEmails.add("live.cn");
			sinaEmails.add("msn.com");
			email_postfix.setVisibility(View.GONE);
			mEmailView.setPadding(
					GlobalTools.dip2px(AccountSetupBasics.this, 10), 0,
					GlobalTools.dip2px(AccountSetupBasics.this, 35),
					GlobalTools.dip2px(AccountSetupBasics.this, 7));
			ArrayList<String> arrayAccounts = new ArrayList<String>();
			AccountAutoCompleteAdapter adapter = new AccountAutoCompleteAdapter(
					AccountSetupBasics.this, arrayAccounts,sinaEmails);
			mEmailView.setAdapter(adapter);
			mEmailView.setDropDownBackgroundResource(R.drawable.shape_edit_bg_normal_nopadding);
		}
		else if(1 == select_flag){
		    new getDomainsThread("domains.txt").start();
		}
		else if (!StringUtil.isEmpty(emailPostfix)) {
			email_postfix.setText(emailPostfix);
			email_postfix.setVisibility(View.VISIBLE);
		} else {
			email_postfix.setVisibility(View.GONE);
		}
		/** Email内容长度变化监测 **/
		mEmailView.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!TextUtils.isEmpty(mEmailView.getText())) {
					img_emai_empty.setVisibility(View.VISIBLE);
				} else {
					img_emai_empty.setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			    if (s != null && s.length() > 0) {
			        String email = s.toString();
			        String bjMail = StringUtil.qj2bj(email.trim());
			        addresscharts = bjMail.length();
			        if (!bjMail.equals(email)) {
			            s.clear();
			            s.append(bjMail);
			        }
			    } else {
			        addresscharts = 0;
			    }
			}
		});

		/** 密码长度变化检测 **/
		mPasswordView.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
//				if (mPasswordView.length() != 0) {
//					mShowPasswordCheckBox.setVisibility(View.VISIBLE);
//				} else {
//					mShowPasswordCheckBox.setVisibility(View.INVISIBLE);
//				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			    if (s != null && s.length() > 0) {
                    String password = s.toString();
                    String bjPassword = StringUtil.qj2bj(password);
                    if (!bjPassword.equals(password)) {
                        s.clear();
                        s.append(bjPassword);
                    }
                }
			}
		});
	}

	private void setClickableSpanForTextView(TextView tv,
			ClickableSpan clickableSpan, String text, int start, int end) {
		SpannableString sp = new SpannableString(text);
		sp.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.setText(sp);
		tv.setLinkTextColor(getResources().getColor(R.color.bluebg));
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setFocusable(false);
		tv.setClickable(false);
		tv.setLongClickable(false);
	}

	private OnClickListener clicklistener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.register:
	             WebViewWithErrorViewActivity.forwardOpenUrlActivity(
	                     AccountSetupBasics.this,
	                     "https://mail.35.com/guide/",
	                     null,
	                     Preferences.getPreferences(MailChat.app).getDefaultAccount().getUuid(),
	                     -1,
	                     false);
			    break;
			case R.id.forgot_password:
			    new GenericDialog(AccountSetupBasics.this).show();
			    break;
			case R.id.tv_login_failed_help:
				String email = "";
				if (emailPostfix != null && !emailPostfix.equals("")
						&& !emailPostfix.equals("None")
						&& !emailPostfix.equals("@sina.com")
						&& !emailPostfix.equals("@outlook.com")
						&& !emailPostfix.equals("@qq.com")
						&& !emailPostfix.equals("@aliyun.com")) {
					email = mEmailView.getText().toString().trim()
							+ emailPostfix;
				} else {
					email = mEmailView.getText().toString().trim();
				}
//				String domainStr=StringUtil.isEmpty(emailPostfix)?"":emailPostfix;
//				String email = mEmailView.getText().toString().trim()+domainStr;
//				UmengFeekbackActivity.actionChatList(AccountSetupBasics.this,email);
				Account mAccount = Preferences.getPreferences(getApplication()).getDefaultAccount();
				if(mAccount==null){
					return;
				}
				ChattingSingleActivity.actionChatList(AccountSetupBasics.this, controller.getHelpDChat(mAccount), mAccount,email);
				break;
			case R.id.img_emai_empty:
				// 清空账号信息
				if (mEmailView.length() != 0){
					mEmailView.setText("");
				}
				break;
			case R.id.login_button_login:
				// 登陆事件
				if (!Utility.requiredFieldValid(mPasswordView)) {
				    Toast.makeText(AccountSetupBasics.this,
				            getString(R.string.account_password_wrong),
                            Toast.LENGTH_LONG).show();
				} else if (!validateFields()) {
                    Toast.makeText(AccountSetupBasics.this,
                            getString(R.string.account_format_wrong),
                            Toast.LENGTH_LONG).show();
				} else {
                   if (!NetUtil.isActive()) {
                        NetUtil.showNoConnectedAlertDlg(AccountSetupBasics.this);
                        return;
                    }
                    onNext();
                    MobclickAgent.onEvent(AccountSetupBasics.this, "login");
				}
				break;
			}

		}
	};
    private void showPassword(boolean isShowPassword) {
		// 密码显示状态
		if (isShowPassword) {
			// 文本正常显示
			mPasswordView
					.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
		} else {
			// 文本以密码形式显示
			mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		}
		// 输入框光标一直在输入文本后面
		Editable etable = mPasswordView.getText();
		Selection.setSelection(etable, etable.length());
    }
	private OnFocusChangeListener focusListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			switch (v.getId()) {
			case R.id.account_email:
				emailFocus(hasFocus);
				break;
			case R.id.account_password:
				passwordFocus(hasFocus);
				break;
			}
		}
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		/* 使用InstanceState保存和恢复数据 */
		super.onSaveInstanceState(outState);
		if (mAccount != null) {
			outState.putString(EXTRA_ACCOUNT, mAccount.getUuid());
		}
		if (mProvider != null) {
			outState.putSerializable(STATE_KEY_PROVIDER, mProvider);
		}
		outState.putBoolean(STATE_KEY_CHECKED_INCOMING, mCheckedIncoming);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		if (savedInstanceState.containsKey(EXTRA_ACCOUNT)) {
			String accountUuid = savedInstanceState.getString(EXTRA_ACCOUNT);
			mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
		}

		if (savedInstanceState.containsKey(STATE_KEY_PROVIDER)) {
			mProvider = (Provider) savedInstanceState
					.getSerializable(STATE_KEY_PROVIDER);
		}

		mCheckedIncoming = savedInstanceState
				.getBoolean(STATE_KEY_CHECKED_INCOMING);
	}

	/**
	 * 验证邮箱账号格式
	 * 
	 * @author liwent
	 */
	private boolean validateFields() {
		String email = mEmailView.getText().toString().trim();
		if (email.contains("@china-channel.com")) {
			email = email.replace("@china-channel.com", "@35.cn");
		}
		if (emailPostfix != null && !emailPostfix.equals("")
				&& !emailPostfix.equals("None")&&!emailPostfix.equals("@sina.com")
				&&!emailPostfix.equals("@outlook.com")&&!emailPostfix.equals("@qq.com")&&!emailPostfix.equals("@aliyun.com")) {
			email = mEmailView.getText().toString().trim() + emailPostfix;
		}else{
			email = mEmailView.getText().toString().trim();
		}
		boolean valid = Utility.requiredFieldValid(mEmailView)// 验证账号是否为空
				&& mEmailValidator.isValidAddressOnly(email);// 验证邮箱格式
		return valid;
	}
	private String getOwnerName() {
		String name = "";
		return name;
	}

//	private String getDefaultAccountName() {
//		String name = null;
//		Account account = Preferences.getPreferences(this).getDefaultAccount();
//		if (account != null) {
//			name = account.getName();
//		}
//		return name;
//	}

	@Override
	public Dialog onCreateDialog(int id) {
		if (id == DIALOG_NOTE) {
			if (mProvider != null && mProvider.note != null) {
				return new AlertDialog.Builder(this)
						.setMessage(mProvider.note)
						.setPositiveButton(getString(R.string.okay_action),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										finishAutoSetup();
									}
								})
						.setNegativeButton(getString(R.string.cancel_action),
								null).create();
			}
		}
		return null;
	}

	/* 自动设置邮件服务器信息 */
	private void finishAutoSetup() {
		// 获取邮箱账号
		String email;
		if (emailPostfix.length() != 0 && !emailPostfix.equals("")
				&& !emailPostfix.equals("None")&& !emailPostfix.equals("@sina.com")
				&&!emailPostfix.equals("@outlook.com")&&!emailPostfix.equals("@qq.com")&&!emailPostfix.equals("@aliyun.com")) {
			email = mEmailView.getText().toString().trim() + emailPostfix;
		} else {
			email = mEmailView.getText().toString().trim();
		}
		if (email.contains("@china-channel.com")) {
			email = email.replace("@china-channel.com", "@35.cn");
		}
		String password = mPasswordView.getText().toString();
		String[] emailParts = splitEmail(email);
		String user;
		if (emailParts[1].equals("35.cn")) {
			user = email;
		} else {
			user = emailParts[0];
		}
		String domain = emailParts[1];
		URI incomingUri = null;
		URI outgoingUri = null;
		try {
			String userEnc = UrlEncodingHelper.encodeUtf8(user);
			String passwordEnc = UrlEncodingHelper.encodeUtf8(password);

			String incomingUsername = mProvider.incomingUsernameTemplate;
			incomingUsername = incomingUsername.replaceAll("\\$email", email);
			incomingUsername = incomingUsername.replaceAll("\\$user", userEnc);
			incomingUsername = incomingUsername.replaceAll("\\$domain", domain);

			URI incomingUriTemplate = mProvider.incomingUriTemplate;
			incomingUri = new URI(incomingUriTemplate.getScheme(),
					incomingUsername + ":" + passwordEnc,
					incomingUriTemplate.getHost(),
					incomingUriTemplate.getPort(), null, null, null);
			// System.out.println("计划：" + incomingUriTemplate.getScheme());
			// System.out.println("口令：" + incomingUsername + ":" + passwordEnc);
			// System.out.println("主机：" + incomingUriTemplate.getHost());
			// System.out.println("端口：" + incomingUriTemplate.getPort());
			String outgoingUsername = mProvider.outgoingUsernameTemplate;
			// System.out.println("outgoingUsername:" + outgoingUsername);
			URI outgoingUriTemplate = mProvider.outgoingUriTemplate;
			// System.out.println("outgoingUriTemplate:" + outgoingUriTemplate);

			if (outgoingUsername != null) {
				outgoingUsername = outgoingUsername.replaceAll("\\$email",
						email);
				// System.out.println("Emai：" + email);
				outgoingUsername = outgoingUsername.replaceAll("\\$user", user);
				// System.out.println("用户：" + user);
				outgoingUsername = outgoingUsername.replaceAll("\\$domain",
						domain);
				// System.out.println("域名：" + domain);
				outgoingUri = new URI(outgoingUriTemplate.getScheme(),
						outgoingUsername + ":" + passwordEnc,
						outgoingUriTemplate.getHost(),
						outgoingUriTemplate.getPort(), null, null, null);

			} else {
				outgoingUri = new URI(outgoingUriTemplate.getScheme(), null,
						outgoingUriTemplate.getHost(),
						outgoingUriTemplate.getPort(), null, null, null);

			}
			// 登陆失败后会删除账户，需重新创建
			mAccount = Preferences.getPreferences(this).newAccount(email);
			Log.i(MailChat.LOG_COLLECTOR_TAG, "finishAutoSetup : newAccount==>>>" + email);
			mAccount.setName(getOwnerName());
			// System.out.println("getOwnerName:" + getOwnerName());
			mAccount.setEmail(email);
			// System.out.println("email:" + email);
			mAccount.setStoreUri(incomingUri.toString());
			// System.out.println("incomingUri:" + incomingUri.toString());
			mAccount.setTransportUri(outgoingUri.toString());
			// System.out.println("outgoingUri:" + outgoingUri.toString());
			setupFolderNames(incomingUriTemplate.getHost().toLowerCase(Locale.US));
			if (incomingUri.toString().startsWith("imap")) {
				mAccount.setDeletePolicy(Account.DELETE_POLICY_ON_DELETE);
			} else if (incomingUri.toString().startsWith("pop3")) {
				mAccount.setDeletePolicy(Account.DELETE_POLICY_NEVER);
			}
			// Check incoming here. Then check outgoing in onActivityResult()
			
			/*
			AccountSetupCheckSettings.actionCheckSettings(this, mAccount,
					CheckDirection.INCOMING);
			*/
			// Modified by LL
			AccountSetupCheckSettings.actionCheckSettings(this, mAccount,
					CheckDirection.INCOMING, false,true,is35Email);
			
		} catch (URISyntaxException use) {
			/*
			 * If there is some problem with the URI we give up and go on to
			 * manual setup.
			 */
			currentEmail=email;
			controller.getEmailSet(email,false);
			
		}
	}

	// 下一步
	protected void onNext() {
		String email;
		if (emailPostfix.length() != 0 && !emailPostfix.equals("")
				&& !emailPostfix.equals("None")&& !emailPostfix.equals("@sina.com")
				&&!emailPostfix.equals("@outlook.com")&&!emailPostfix.equals("@qq.com")&&!emailPostfix.equals("@aliyun.com")) {
			email = mEmailView.getText().toString().trim() + emailPostfix;
		} else {
			email = mEmailView.getText().toString().trim();
		}
		if (email.contains("@china-channel.com")) {
			email = email.replace("@china-channel.com", "@35.cn");
		}
		// 判断邮件是否已经登录，如果已经登录提示不让登录了
		List<Account> accounts = Preferences.getPreferences(
				AccountSetupBasics.this).getAccounts();
		if (accounts.size() > 0) {
			for (int i = 0; i < accounts.size(); i++) {
				if (accounts.get(i).getEmail().toString().equals(email)) {
					Toast.makeText(AccountSetupBasics.this,
							getString(R.string.account_logined),
							Toast.LENGTH_SHORT).show();
					return;
				}
			}
		}
		// 截取后缀，与账号
		String[] emailParts = splitEmail(email);
		/* 后缀 */
		String domain = emailParts[1];
		/* make in china 改改R.xml.providers文件就好 ps.赤裸裸的欺负咱啊，一个邮件服务器都没有！ */
		mProvider = findProviderForDomain(domain);
		// System.out.println(mProvider);
		currentEmail=email;
		if (mProvider == null) {
			// xml中无该邮件服务器配置，手动设置IMAP,POP3,SMTP服务器地址
			dialog.setMessage( getString(R.string.advanced_logining));
			dialog.show();
			if(is35Email){
				controller.get35MailVersionForLogin(null,currentEmail);
			}else if(isEYouEmail){
				controller.getEmailSet(email,false);
			}else{
				controller.get35MailVersionForLogin(null,currentEmail);
			}
			return;
		}
		// xml没有配置，下面这无法执行
		if (mProvider.note != null) {
			showDialog(DIALOG_NOTE);
		} else {
			finishAutoSetup();
		}
	}

	/**
	 * 导入配置文件
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			hideSoftInput();
			if (!mCheckedIncoming) {
				// We've successfully checked incoming. Now check outgoing.
				mCheckedIncoming = true;
				if(is35Email||isEYouEmail){
					AccountSetupCheckSettings.actionCheck35Settings(this, mAccount,
							CheckDirection.OUTGOING, true, true);
				}else{
					AccountSetupCheckSettings.actionCheckSettings(this, mAccount,
							CheckDirection.OUTGOING, false, true, is35Email);
				}
				
			} else {
				// We've successfully checked outgoing as well.
				setDefaultAccountSettings();
				if(is35Email){
				    saveUserInfo(mEmailView.getText().toString().trim(),
				            mPasswordView.getText().toString().trim());
				}
				AccountSetupNameActivity.actionSetNames(this, mAccount, -1, false);
				controller.subscribeAccount(mAccount);
				controller.getGroupInvitation(mAccount, null, false);
				controller.get35MailVersion(mAccount, true);
				controller.registerPush(mAccount);
				//判断该账户是否有绑定OA
				controller.checkIsBindOA(mAccount,false,null);
				//测试
				controller.bingEmailToUser(mAccount, false, "", "");
				finish();
			}
		}
	}
	private void hideSoftInput() {
		WindowManager.LayoutParams params = getWindow().getAttributes();
        if (params.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
            // 隐藏软键盘
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
		}
	}

	private void setDefaultAccountSettings() {
		mAccount.setDescription(mAccount.getEmail());
		mAccount.setNotifyNewMail(true);// 通知视图

		/*
		int version = mAccount.getVersion_35Mail();
		boolean isRemoteStorePushCapable = false;
		boolean is35Mail = version == 1 || version == 2;
		try {
		    isRemoteStorePushCapable = mAccount.getRemoteStore().isPushCapable();
		} catch (Exception e) {
		    Log.e(MailChat.LOG_TAG, e.toString());
		    MailChat.toast("!");
		}
		if (is35Mail || isRemoteStorePushCapable) {
		    mAccount.setAutomaticCheckIntervalMinutes(GlobalConstants.DEFAULT_PUSH_CAPABLE_POLL_INTERVAL);
		} else {
		    mAccount.setAutomaticCheckIntervalMinutes(GlobalConstants.DEFAULT_POLL_INTERVAL);
		}
		*/
        // 自动检查间隔分钟
        mAccount.setAutomaticCheckIntervalMinutes(GlobalConstants.DEFAULT_POLL_INTERVAL);

		mAccount.setDisplayCount(MailChat.DEFAULT_VISIBLE_LIMIT);// 显示数量
		mAccount.setShowOngoing(true);// 显示发送状态
		mAccount.setFolderPushMode(Account.FolderMode.FIRST_CLASS);
		mAccount.save(Preferences.getPreferences(this));
		Preferences.getPreferences(this).setDefaultAccount(mAccount);
		MailChat.setServicesEnabled(this);
	}
	/**
	 * Show that account's inbox or folder-list or return false if the account
	 * is not available.
	 * 
	 * @param account
	 *            the account to open ({@link SearchAccount} or {@link Account})
	 * @return false if unsuccessfull
	 */
//	private boolean onOpenAccount(BaseAccount account) {
//		if (account instanceof SearchAccount) {
//			SearchAccount searchAccount = (SearchAccount) account;
//			Main4TabActivity.actionDisplaySearch(this,
//					searchAccount.getRelatedSearch(), false, false);
//		} else {
//			Account realAccount = (Account) account;
//			if (!realAccount.isEnabled()) {
//				// onActivateAccount(realAccount);
//				return false;
//			} else if (!realAccount.isAvailable(this)) {
//				String toastText = getString(R.string.account_unavailable,
//						account.getDescription());
//				Toast toast = Toast.makeText(getApplication(), toastText,
//						Toast.LENGTH_SHORT);
//				toast.show();
//
//				Log.i(MailChat.LOG_TAG,
//						"refusing to open account that is not available");
//				return false;
//			}
//			if (MailChat.FOLDER_NONE.equals(realAccount.getAutoExpandFolderName())) {
//				//Main4TabActivity.actionHandleAccount(this, realAccount);
//			} else {
//				LocalSearch search = new LocalSearch(
//						realAccount.getAutoExpandFolderName());
//				search.addAllowedFolder(realAccount.getAutoExpandFolderName());
//				search.addAccountUuid(realAccount.getUuid());
//				Main4TabActivity.actionDisplaySearch(this, search, false, true);
//			}
//		}
//		return true;
//	}
	/**
	 * 手动设置
	 * 
	 * @author liwent
	 */
	private void onManualSetup() {
		String email;
		if (emailPostfix.length() != 0 && !emailPostfix.equals("")
				&& !emailPostfix.equals("None")&& !emailPostfix.equals("@sina.com")
				&&!emailPostfix.equals("@outlook.com")&&!emailPostfix.equals("@qq.com")&&!emailPostfix.equals("@aliyun.com")) {
			email = mEmailView.getText().toString().trim() + emailPostfix;
		} else {
			email = mEmailView.getText().toString().trim();
		}
		if (email.contains("@china-channel.com")) {
			email = email.replace("@china-channel.com", "@35.cn");
		}
		 password = mPasswordView.getText().toString();
		String[] emailParts = splitEmail(email);
		String domain = emailParts[1];
		// 登陆失败后会删除账户，需重新创建
		mAccount = Preferences.getPreferences(this).newAccount(email);
		Log.i(MailChat.LOG_COLLECTOR_TAG, "onManualSetup : newAccount==>>>" + email);
		mAccount.setName(getOwnerName());
		mAccount.setEmail(email);
		try {
			String userEnc = UrlEncodingHelper.encodeUtf8(email);
			String passwordEnc =UrlEncodingHelper.encodeUtf8(password);;
//			URI uri = new URI("placeholder", userEnc + ":" + passwordEnc,
//					"mail." + domain, -1, null, null, null);
//			mAccount.setStoreUri(uri.toString());
//			// System.out.println("StoreUri:" + uri.toString());
//			mAccount.setTransportUri(uri.toString());
			URI uri = new URI("imap+ssl+", userEnc + ":" + passwordEnc,
					"mail." + domain,-1, null, null, null);
			mAccount.setStoreUri(uri.toString());
			uri = new URI("smtp+tls+",userEnc + ":" + passwordEnc,
					"mail." + domain,-1, null, null, null);
			mAccount.setTransportUri(uri.toString());
		} catch (URISyntaxException use) {
			/*
			 * If we can't set up the URL we just continue. It's only for
			 * convenience.
			 */
		}
		setupFolderNames(domain);
		AccountSetupIncoming.actionIncomingSettings(this, mAccount,
				false, true,is35Email);
	}
	private String getXmlAttribute(XmlResourceParser xml, String name) {
		int resId = xml.getAttributeResourceValue(null, name, 0);
		if (resId == 0) {
			return xml.getAttributeValue(null, name);
		} else {
			return getString(resId);
		}
	}

	/**
	 * 获取邮件服务供应商
	 * 
	 * @note liwent
	 * @param domain
	 */
	private Provider findProviderForDomain(String domain) {
		try {
			XmlResourceParser xml = getResources().getXml(R.xml.providers);
			int xmlEventType;
			Provider provider = null;
			while ((xmlEventType = xml.next()) != XmlResourceParser.END_DOCUMENT) {
				if (xmlEventType == XmlResourceParser.START_TAG
						&& "provider".equals(xml.getName())
						&& domain.equalsIgnoreCase(getXmlAttribute(xml,
								"domain"))) {
					provider = new Provider();
					provider.id = getXmlAttribute(xml, "id");
					provider.label = getXmlAttribute(xml, "label");
					provider.domain = getXmlAttribute(xml, "domain");
					provider.note = getXmlAttribute(xml, "note");
				} else if (xmlEventType == XmlResourceParser.START_TAG
						&& "incoming".equals(xml.getName()) && provider != null) {
					provider.incomingUriTemplate = new URI(getXmlAttribute(xml,
							"uri"));
					provider.incomingUsernameTemplate = getXmlAttribute(xml,
							"username");
				} else if (xmlEventType == XmlResourceParser.START_TAG
						&& "outgoing".equals(xml.getName()) && provider != null) {
					provider.outgoingUriTemplate = new URI(getXmlAttribute(xml,
							"uri"));
					provider.outgoingUsernameTemplate = getXmlAttribute(xml,
							"username");
				} else if (xmlEventType == XmlResourceParser.END_TAG
						&& "provider".equals(xml.getName()) && provider != null) {
					return provider;
				}
			}
		} catch (Exception e) {
			Log.e(MailChat.LOG_TAG,
					"Error while trying to load provider settings.", e);
		}
		return null;
	}

	/**
	 * 以@分割Email
	 * 
	 * @author liwent
	 * @param email
	 * @return String[0]为账号String[1]邮箱后缀
	 */
	private String[] splitEmail(String email) {
		String[] retParts = new String[2];
		String[] emailParts = email.split("@");
		retParts[0] = (emailParts.length > 0) ? emailParts[0] : "";
		/* 小数点后至少有长度为1的地址 */
		retParts[1] = (emailParts.length > 1) ? emailParts[1] : "";
		return retParts;
	}

	private void emailFocus(final boolean focus) {
		if (focus) {
			email_view_1.setBackgroundColor(Color.parseColor("#128FBB"));
			email_view_2.setBackgroundColor(Color.parseColor("#128FBB"));
			email_view_3.setBackgroundColor(Color.parseColor("#128FBB"));
		} else {
			email_view_1.setBackgroundColor(Color.parseColor("#888888"));
			email_view_2.setBackgroundColor(Color.parseColor("#888888"));
			email_view_3.setBackgroundColor(Color.parseColor("#888888"));
		}
	}

	private void passwordFocus(final boolean focus) {
		if (focus) {
			password_view_1.setBackgroundColor(Color.parseColor("#128FBB"));
			password_view_2.setBackgroundColor(Color.parseColor("#128FBB"));
			password_view_3.setBackgroundColor(Color.parseColor("#128FBB"));
		} else {
			password_view_1.setBackgroundColor(Color.parseColor("#888888"));
			password_view_2.setBackgroundColor(Color.parseColor("#888888"));
			password_view_3.setBackgroundColor(Color.parseColor("#888888"));
		}
	}

	/**
	 * 邮件服务供应商信息
	 * 
	 * @author liwent
	 * @see Documented
	 */
	static class Provider implements Serializable {
		private static final long serialVersionUID = 8511656164616538989L;
		public String id;/* 编号 */
		public String label;/* 商标 */
		public String domain;/* 域名 */
		public URI incomingUriTemplate;/* URL传入模板 */
		public String incomingUsernameTemplate;/* 用户名传入模板 */
		public URI outgoingUriTemplate;/* URL输出模板 */
		public String outgoingUsernameTemplate;/* 用户名输出模板 */
		public String note;/* 注解 */

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("编号:").append(id).append("\n");
			builder.append("商标:").append(label).append("\n");
			builder.append("域名:").append(domain).append("\n");
			builder.append("发送服务器:").append(incomingUriTemplate).append("\n");
			builder.append("用户传入：").append(incomingUsernameTemplate)
					.append("\n");
			builder.append("接收服务器：").append(outgoingUriTemplate).append("\n");
			builder.append("用户输出：").append(outgoingUsernameTemplate)
					.append("\n");
			builder.append("注解：").append(note).append("\n");
			return builder.toString();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (mEmailView.length() != 0){
				mEmailView.setText("");
			}
			SelectEmailActivity.showSelectEmailActivity(this);
			finish();
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		controller.removeListener(listener);
	}
	private MessagingListener listener  =new MessagingListener(){
		public void loginDialogShow(final String  email,final boolean isShowmanualSettingImp,final int errorCode,final boolean is35Mail){
			if(mAccount.getEmail().equals(email)&&isShowmanualSettingImp){
				mHander.post(new Runnable() {
					@Override
					public void run() {
						new LoginDialog(AccountSetupBasics.this,errorCode,mAccount.getEmail(),mPasswordView.getText().toString(),isShowmanualSettingImp,is35Mail).show();
					}
				});
			}
		}
		
		public void getEmailSetFail(String  email,final int errorCode,boolean isGoImapSetting){
			if(currentEmail.equals(email)&&!isGoImapSetting){
				
				mHander.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						dialog.hide();
						dialog.dismiss();
						if(isEYouEmail){
							finish35AutoSetup(true);
						}else{
							Toast.makeText(AccountSetupBasics.this, getString(R.string.advanced_logining_fail), Toast.LENGTH_SHORT).show();
							onManualSetup();
						}
					}
				});
			
			}
		}
		
		public void getEmailSetSuccess(String  email,final ImapAndSmtpSetting imapAndSmtpSetting,boolean isGoImapSetting){
			if(currentEmail.equals(email)&&!isGoImapSetting){
				
				mHander.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						dialog.hide();
						dialog.dismiss();
						finishHttpsAutoSetup(imapAndSmtpSetting);
					}
				});
				
			}
		}
		
		public void advancedLoggingSuccess(String  email){
			if(currentEmail.equals(email)){
				finish();
			}
		}
		public void get35MailVersionForLogin(final String email,final int version) {
			if (currentEmail.equals(email)) {

				mHander.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (version == 2) {
							dialog.hide();
							dialog.dismiss();
							finish35AutoSetup(false);
						} else if(version==1){
							dialog.hide();
							dialog.dismiss();
							finish35AutoSetup(true);
						} else{
							controller.getEmailSet(email,false);
						}
					}
				});

			}
		}
	};
	private boolean isLogined;
	
	/* 从服务器获取下来IMAP设置 */
	private void finishHttpsAutoSetup(ImapAndSmtpSetting imapAndSmtpSetting) {
		// 获取邮箱账号
		String email;
		if (emailPostfix.length() != 0 && !emailPostfix.equals("")
				&& !emailPostfix.equals("None")&& !emailPostfix.equals("@sina.com")
				&&!emailPostfix.equals("@outlook.com")&&!emailPostfix.equals("@qq.com")&&!emailPostfix.equals("@aliyun.com")) {
			email = mEmailView.getText().toString().trim() + emailPostfix;
		} else {
			email = mEmailView.getText().toString().trim();
		}
		if (email.contains("@china-channel.com")) {
			email = email.replace("@china-channel.com", "@35.cn");
		}
		String password = mPasswordView.getText().toString();
		String[] emailParts = splitEmail(email);
		String user;
//		if (emailParts[1].equals("35.cn")) {
			user = email;
//		} else {
//			user = emailParts[0];
//		}
		String domain = emailParts[1];
		try {
			String userEnc = UrlEncodingHelper.encodeUtf8(user);
			String passwordEnc = UrlEncodingHelper.encodeUtf8(password);
			int  imapSafety = imapAndSmtpSetting.getImapSafety();
			String imapScheme =null;
			int imapPost=imapAndSmtpSetting.getImapPost();
			if(imapSafety==0){
				imapScheme="imap";
				if(imapPost==-1){
					imapPost=143;
				}
			}else if(imapSafety==1){
				imapScheme="imap+tls+";
				if(imapPost==-1){
					imapPost=143;
				}
			}else if(imapSafety==2){
				imapScheme="imap+ssl+";
				if(imapPost==-1){
					imapPost=993;
				}
			}
			int  smtpSafety = imapAndSmtpSetting.getSmtpSafety();
			String smtpScheme =null;
			int smtpPost=imapAndSmtpSetting.getImapPost();
			if(smtpSafety==0){
				smtpScheme ="smtp";
				if(smtpPost==-1){
					smtpPost=25;
				}
			}else if(smtpSafety==1){
				smtpScheme ="smtp+tls+";
				if(smtpPost==-1){
					smtpPost=587;
				}
			}else if(smtpSafety==2){
				smtpScheme ="smtp+ssl+";
				if(smtpPost==-1){
					smtpPost=465;
				}
			}
			URI incomingUri = new URI(imapScheme,userEnc + ":" + passwordEnc,imapAndSmtpSetting.getImapHost(),imapPost, null, null, null);
			URI outgoingUri = new URI(smtpScheme,userEnc + ":" + passwordEnc,imapAndSmtpSetting.getSmtpHost(),smtpPost, null, null, null);
			// 登陆失败后会删除账户，需重新创建
			mAccount = Preferences.getPreferences(this).newAccount(email);
			Log.i(MailChat.LOG_COLLECTOR_TAG, "finishHttpsAutoSetup : newAccount==>>>" + email);
			mAccount.setName(getOwnerName());
			mAccount.setEmail(email);
			mAccount.setStoreUri(incomingUri.toString());
			mAccount.setTransportUri(outgoingUri.toString());
			setupFolderNames(imapAndSmtpSetting.getImapHost().toLowerCase(Locale.US));
			if (incomingUri.toString().startsWith("imap")) {
				mAccount.setDeletePolicy(Account.DELETE_POLICY_ON_DELETE);
			} else if (incomingUri.toString().startsWith("pop3")) {
				mAccount.setDeletePolicy(Account.DELETE_POLICY_NEVER);
			}
			// Check incoming here. Then check outgoing in onActivityResult()
			AccountSetupCheckSettings.actionCheckSettings(this, mAccount,
					CheckDirection.INCOMING, false, true, is35Email);
		} catch (URISyntaxException use) {
			/*
			 * If there is some problem with the URI we give up and go on to
			 * manual setup.
			 */
			onManualSetup();
		}
	}
	/**
	 * 35邮箱自动设置
	 * 
	 * @author shengli
	 * @see Documented
	 */
	private void finish35AutoSetup(boolean isSSL) {
		String email;
		if (emailPostfix.length() != 0 && !emailPostfix.equals("")
				&& !emailPostfix.equals("None")&& !emailPostfix.equals("@sina.com")
				&&!emailPostfix.equals("@outlook.com")&&!emailPostfix.equals("@qq.com")&&!emailPostfix.equals("@aliyun.com")) {
			email = mEmailView.getText().toString().trim() + emailPostfix;
		} else {
			email = mEmailView.getText().toString().trim();
		}
		if (email.contains("@china-channel.com")) {
			email = email.replace("@china-channel.com", "@35.cn");
		}
		String password = mPasswordView.getText().toString();
		String[] emailParts = splitEmail(email);
		String domain = emailParts[1];
		// 登陆失败后会删除账户，需重新创建
		mAccount = Preferences.getPreferences(this).newAccount(email);
		Log.i(MailChat.LOG_COLLECTOR_TAG, "finish35AutoSetup : newAccount==>>>" + email);
		mAccount.setName(getOwnerName());
		mAccount.setEmail(email);
		try {
			String userEnc = URLEncoder.encode(email, "UTF-8");
			String passwordEnc = URLEncoder.encode(password, "UTF-8");
			String imapScheme =null;
			int imapPost=-1;
			String smtpScheme =null;
			int smtpPost=-1;
			if(isSSL){
				imapScheme ="imap+ssl+";
				imapPost=993;
				smtpScheme ="smtp+ssl+";
				smtpPost= 465;
			}else{
				imapScheme ="imap";
				imapPost=143;
				smtpScheme ="smtp";
				smtpPost=25;
			}
			URI inUri = new URI(imapScheme, userEnc + ":" + passwordEnc,
					"mail." + domain, imapPost, null, null, null);
			URI outUri = new URI(smtpScheme, userEnc + ":" + passwordEnc,
					"mail." + domain,smtpPost, null, null, null);
			mAccount.setStoreUri(inUri.toString());
			mAccount.setTransportUri(outUri.toString());
			if (inUri.toString().startsWith("imap")) {
				mAccount.setDeletePolicy(Account.DELETE_POLICY_ON_DELETE);
			} 
		} catch (UnsupportedEncodingException enc) {
			// This really shouldn't happen since the encoding is hardcoded to
			// UTF-8
			Log.e(MailChat.LOG_TAG, "Couldn't urlencode username or password.",
					enc);
		} catch (URISyntaxException use) {
			/*
			 * If we can't set up the URL we just continue. It's only for
			 * convenience.
			 */
		}
		setupFolderNames(domain);
		AccountSetupCheckSettings.actionCheck35Settings(this, mAccount,CheckDirection.INCOMING,is35Email,true);
	}

    private void setupFolderNames(String domain) {
        mAccount.setDraftsFolderName(getString(R.string.special_mailbox_name_drafts));
        mAccount.setTrashFolderName(getString(R.string.special_mailbox_name_trash));
        mAccount.setSentFolderName(getString(R.string.special_mailbox_name_sent));
        mAccount.setArchiveFolderName(getString(R.string.special_mailbox_name_archive));

        // Yahoo! has a special folder for Spam, called "Bulk Mail".
        if (domain.endsWith(".yahoo.com")) {
            mAccount.setSpamFolderName("Bulk Mail");
        } else {
            mAccount.setSpamFolderName(getString(R.string.special_mailbox_name_spam));
        }
    }

	/**
	 * 从35用户中心取得账号信息
	 */
	private void getAccountFrom35UserCenter() {
		try {
			ContentResolver cr = getContentResolver();
			Uri myUri = Uri.parse(GlobalConstants.CONTENT_URI);
			Cursor cursor = cr.query(myUri, null, null, null, null);
			if (cursor == null) {
//				Toast.makeText(this, "查询不到用户信息", Toast.LENGTH_SHORT).show();
				return;
			}
			while (cursor.moveToNext()) {
				 centerEmail = cursor.getString(cursor.getColumnIndex("email"));
				 centerPwd = cursor
						.getString(cursor.getColumnIndex("password"));
			}
			cursor.close();
		} catch (Exception ex) {
		}
	}
	/**
	 * 保存用户到中心，供其他35客户端使用
	 */
	private void saveUserInfo(String email,String pwd) {
		try {
			ContentValues values = new ContentValues();
			values.put("email",
					Base64Utils.getBASE64(email.trim()));
			values.put("password", Base64Utils.getBASE64(pwd.trim()));
			getContentResolver().insert(Uri.parse(GlobalConstants.CONTENT_URI), values);
		} catch (Exception ex) {

		}
	}
	private void isLogined(){
		if (StringUtils.isNullOrEmpty(centerEmail)) {
			isLogined= true;
			return;
		}
		List<Account> accounts = Preferences.getPreferences(
				AccountSetupBasics.this).getAccounts();
		if (accounts.size() > 0) {
			for (int i = 0; i < accounts.size(); i++) {
				if (accounts.get(i).getEmail().toString().equals(Base64Utils.getFromBASE64(centerEmail))) {
					isLogined= true;
					return;
				}else {
					isLogined= false;
				}
			}
		}
	}
	
	static List<String> domainsList = new ArrayList<>();
  	 GetDomainsHandler handler = new GetDomainsHandler(this);
  	 
	 class getDomainsThread extends Thread {
		private String domainType; 
		public getDomainsThread(String type){
			domainType = type;
		} 
       public void run() {
       	getDomainsFromTxt(domainType);
       	handler.sendEmptyMessage(0);
       };
	  };
	 //从asset中获取domains内的域名 
	 private void getDomainsFromTxt(String domainsFile){
		 try {
           InputStream inputStream = getResources().getAssets().open(domainsFile);
           InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
           BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
           String info = "";
           while ((info = bufferedReader.readLine()) != null) {
           	domainsList.add(info);
           }
           
       } catch (IOException e) {
           e.printStackTrace();
       }
	}
	
	static class GetDomainsHandler extends Handler{
		 WeakReference<Activity> mActivityReference;
		 GetDomainsHandler(Activity activity) {
		    mActivityReference= new WeakReference<Activity>(activity);
		 } 
		 @Override
		public void handleMessage(Message msg) {
			final Activity activity = mActivityReference.get();
		    if (activity != null) {
	    	   switch (msg.what) {
           	case 0:
           		email_postfix.setVisibility(View.GONE);
       			ArrayList<String> arrayAccounts = new ArrayList<String>();
       			AccountAutoCompleteAdapter adapter = new AccountAutoCompleteAdapter(activity, arrayAccounts,domainsList);
       			mEmailView.setAdapter(adapter);
       			mEmailView.setDropDownBackgroundResource(R.drawable.shape_edit_bg_normal_nopadding);
           		break;
	            }
		    }
		}
	}

}