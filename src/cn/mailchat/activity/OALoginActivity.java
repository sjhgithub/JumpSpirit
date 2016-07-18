package cn.mailchat.activity;

import com.umeng.analytics.MobclickAgent;

import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.setup.AccountSetupBasics;
import cn.mailchat.chatting.beans.DChat;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.utils.CommonUtils;
import cn.mailchat.utils.StringUtil;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

/**
 * 登录oa
 * 
 * @author zhangjx
 * 
 */
public class OALoginActivity extends BaseActionbarFragmentActivity {
	private static String EXTRA_ACCOUNT_UID = "account_uid";
	private TextView mTvOABindEmail;
	private EditText mEtOAPwd;
	private CheckBox mCbShowPwd;
	private Button loginInOA;
	private View mCustomActionbarView;
	private Account mAccount;
	private MessagingController mController;
	private ProgressDialog dialog;

	public static void actionLoginOA(Context context, Account account) {
		Intent i = actionLoginOAForIntent(context, account);
		context.startActivity(i);
	}

	public static Intent actionLoginOAForIntent(Context context, Account account) {
		Intent i = new Intent(context, OALoginActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString(EXTRA_ACCOUNT_UID, account.getUuid());
		i.putExtras(bundle);
		return i;
	}

	private MessagingListener listener = new MessagingListener() {
		@Override
		public void loginInOAStart() {

		}

		@Override
		public void loginInOASucceed(Account account) {
			if (account.getEmail().equals(mAccount.getEmail())) {
				account.setOAUser(true);
				account.save(Preferences.getPreferences(OALoginActivity.this));
				jumpToOAView(account);
				dialog.dismiss();
				finish();
			}
		}

		@Override
		public void loginInOAFailed() {
			runOnUiThread(new Runnable() {
				public void run() {
					dialog.dismiss();
					Toast.makeText(OALoginActivity.this,
							getString(R.string.error_oa_pwd),
							Toast.LENGTH_SHORT).show();
				}
			});
		}

		@Override
		public void unBindOA(Account account) {
			if (account.getEmail().equals(mAccount.getEmail())) {
				WebViewWithErrorViewActivity.forwardOpenUrlActivity(
						OALoginActivity.this, GlobalConstants.OA_NO_OPEN_URL,
						null, account.getUuid(), -1,true);
				MobclickAgent.onEvent(OALoginActivity.this, "open_no_oa_view");
				finish();
			}
		};
	};
	private TextView mActionbarTitle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_oa_login);
		initView();
		initializeActionBar();
		initActionbarView();
		initData();
		setListener();
	}

	private void jumpToOAView(Account account) {
		DChat dChat = new DChat();
		dChat.setdChatType(DChat.Type.OA);
		dChat.setUid(DChat.getDchatUid(account.getEmail() + ","
				+ GlobalConstants.DCHAT_OA));
		dChat.setLastTime(System.currentTimeMillis());
		dChat.setEmail(GlobalConstants.DCHAT_OA);
		dChat.setNickName(GlobalConstants.DCHAT_OA);
		dChat.setVisibility(true);
		dChat.setDChatAlert(true);
		dChat.setSticked(false);
		dChat.setUnReadCount(0);
		ChattingSingleActivity.actionChatList(OALoginActivity.this, dChat,
				account);
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
	}

	private void initActionbarView() {
		mActionbarTitle = (TextView) mCustomActionbarView
				.findViewById(R.id.tv_title);
		TextView mActionbarSure = (TextView) mCustomActionbarView
				.findViewById(R.id.tv_sure);
		setActionbarCenterTitle(mCustomActionbarView, mActionbarTitle,
				getString(R.string.title_login_oa));
		mActionbarSure.setVisibility(View.INVISIBLE);
	}

	private void setListener() {
		mCbShowPwd.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				showPassword(isChecked);
			}
		});
		loginInOA.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String oaPwd = mEtOAPwd.getText().toString().trim();
				if (!StringUtil.isEmpty(oaPwd)) {
					dialog.setMessage(getString(R.string.login_in_oa));
					dialog.show();
					mAccount.setoAPwd(oaPwd);
					mAccount.save(Preferences
							.getPreferences(OALoginActivity.this));
					doLogin();
					MobclickAgent.onEvent(OALoginActivity.this, "login_oa");
				} else {
					Toast.makeText(
							OALoginActivity.this,
							getString(R.string.account_safe_setting_empty_password),
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	protected void doLogin() {
		CommonUtils.hideSoftInput(OALoginActivity.this);
		mController.loginInOA(mAccount);
	}

	private void initData() {
		Intent i = getIntent();
		String accountUid = i.getStringExtra(EXTRA_ACCOUNT_UID);
		mAccount = Preferences.getPreferences(OALoginActivity.this).getAccount(
				accountUid);
		mTvOABindEmail.setText(getString(R.string.oa_binded,
				mAccount.getoAEmail()));
		mController = MessagingController.getInstance(getApplication());
		mController.addListener(listener);
		mController.checkIsBindOA(mAccount, true,listener);
	}

	private void initView() {
		mTvOABindEmail = (TextView) findViewById(R.id.textView_oa_bind_email);
		mCbShowPwd = (CheckBox) findViewById(R.id.show_password);
		mEtOAPwd = (EditText) findViewById(R.id.editText_oa_password);
		loginInOA = (Button) findViewById(R.id.login_button_login);
		dialog = new ProgressDialog(OALoginActivity.this);
		dialog.setCancelable(true);
	}

	private void showPassword(boolean isShowPassword) {
		// 密码显示状态
		if (isShowPassword) {
			// 文本正常显示
			mEtOAPwd.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
		} else {
			// 文本以密码形式显示
			mEtOAPwd.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		}
		// 输入框光标一直在输入文本后面
		Editable etable = mEtOAPwd.getText();
		Selection.setSelection(etable, etable.length());
	}

	@Override
	protected void onDestroy() {
		if (listener != null) {
			mController.removeListener(listener);
		}
		super.onDestroy();
	}
}
