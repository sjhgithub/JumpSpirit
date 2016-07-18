package cn.mailchat.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.ImageView;
import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.fragment.WorkSpaceFragment;
import cn.mailchat.fragment.WorkSpaceFragment.WorkSpaceFragmentListener;
import cn.mailchat.search.LocalSearch;
import cn.mailchat.utils.ActivityManager;
import cn.mailchat.view.ScrollForeverTextView;

import com.umeng.analytics.MobclickAgent;

/**
 * 
 * @copyright © 35.com
 * @file name ：WebViewWithErrorViewActivity.java
 * @author ：zhangjx
 * @create Data ：2015-5-19下午1:47:25
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2015-5-19下午1:47:25
 * @Modified by：zhangjx
 * @Description :
 */
public class WebViewWithErrorViewActivity extends ActionBarActivity implements
		WorkSpaceFragmentListener {
	public static final String TAG_NOTIFY_OA_FLAG = "notify_into_oa";
	public static final String EXTRA_Web_URL = "web_url";
	public static final String EXTRA_WEB_TITLE = "web_title";
	public static final String EXTRA_WEB_FLAG = "web_flag";
	public static final String EXTRA_IS_OA = "is_oa";

	public static final String MENTIONS_SCHEMA = "cn.mailchat.activites://message_private_url";
	private static final String PARAM_UID = "uid";
	public static final int WEB_OPEN_URL = 1;
	private ScrollForeverTextView tvTitle;
	private ImageView layoutBack;
	private String url = "";
	private Account account;
	private String mAccountUuid;
	private FragmentManager mFragmentManager;
	private FragmentTransaction mFragmentTransaction;

	public static Intent forwardOpenUrlActivity(Context context,
			String url, String title, String accountUuid, int notifyFlag, boolean isOA) {
		Intent tIntent = new Intent(context, WebViewWithErrorViewActivity.class);
		tIntent.putExtra(EXTRA_Web_URL, url);
		tIntent.putExtra(EXTRA_WEB_TITLE, title);
		tIntent.putExtra(EXTRA_WEB_FLAG,
				WebViewWithErrorViewActivity.WEB_OPEN_URL);
		tIntent.putExtra(ChattingSingleActivity.ACCOUNTUUID, accountUuid);
		tIntent.putExtra(TAG_NOTIFY_OA_FLAG, notifyFlag);
		tIntent.putExtra(EXTRA_IS_OA, isOA);
		if (notifyFlag!=1) {
			tIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(tIntent);
		}
		return tIntent;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityManager.push(this);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_webview_with_error_view);
		extractUidFromUri();
		initView();
		initData();
		initListener();
	}

	private void extractUidFromUri() {
		Uri uri = getIntent().getData();
		Uri PROFILE_URI = Uri.parse(MENTIONS_SCHEMA);
		if (uri != null && PROFILE_URI.getScheme().equals(uri.getScheme())) {
			url = uri.getQueryParameter(PARAM_UID);
		}
		if (url.indexOf("www") == 0) {
			url = "http://" + url;
		} else if (url.indexOf("https") == 0) {
			String bUid = url.substring(5, url.length());
			url = "http" + bUid;
		}
	}

	private void initView() {
		layoutBack = (ImageView) findViewById(R.id.back);
		tvTitle = (ScrollForeverTextView) findViewById(R.id.title);

	}

	private void initData() {
		Intent intent = getIntent();
		String urlStr = intent.getStringExtra(EXTRA_Web_URL);
		String title = intent.getStringExtra(EXTRA_WEB_TITLE);
		boolean isOA = intent.getBooleanExtra(EXTRA_IS_OA, false);
		mAccountUuid = intent
				.getStringExtra(ChattingSingleActivity.ACCOUNTUUID);
		account = Preferences.getPreferences(this).getAccount(mAccountUuid);
		int webFlag = intent.getIntExtra(EXTRA_WEB_FLAG, -1);
		mAccountUuid = intent
				.getStringExtra(ChattingSingleActivity.ACCOUNTUUID);
		account = Preferences.getPreferences(this).getAccount(mAccountUuid);
		switch (webFlag) {
		case WEB_OPEN_URL:
			url = urlStr;
			// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		}
		initFragment(url, title, mAccountUuid, isOA);
	}

	private void initFragment(String url, String title, String accountUuid,
			boolean isOA) {
		mFragmentManager = getSupportFragmentManager();
		mFragmentTransaction = mFragmentManager.beginTransaction();
		WorkSpaceFragment mOaFragment = WorkSpaceFragment.newInstance(url,
				title, accountUuid, isOA);
		mFragmentTransaction.replace(R.id.detial_content, mOaFragment,
				WorkSpaceFragment.DRAWER_WORKSPACE_VIEW_TAG);
		mFragmentTransaction.commit();
	}

	private OnLongClickListener myLongClickListener() {
		return new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				ClipData textCd = ClipData.newPlainText("url", url);
				clipboard.setPrimaryClip(textCd);
				return true;
			}
		};
	}

	private void initListener() {
		layoutBack.setOnClickListener(myClickListener());
		tvTitle.setOnLongClickListener(myLongClickListener());
	}

	private OnClickListener myClickListener() {
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.back:
					if (getIntent().getIntExtra(TAG_NOTIFY_OA_FLAG, 0) == 1) {
						MailChat.isChat = true;
						changeAccount();
						ActivityManager.popAll();
						jumpToMain();
					}
					WebViewWithErrorViewActivity.this.finish();
					break;
				default:
					break;
				}
			}
		};

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (getIntent().getIntExtra(TAG_NOTIFY_OA_FLAG, 0) == 1) {
			MailChat.isChat = true;
			changeAccount();
			ActivityManager.popAll();
			jumpToMain();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("WebViewActivity"); // 统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("WebViewActivity"); // 统计页面
		MobclickAgent.onPause(this);
	}

	private void changeAccount() {
		if (account.getIsHaveUnreadMsg()) {
			account.setmIsHaveUnreadMsg(false);
		}
		account.save(Preferences.getPreferences(this));
		Preferences.getPreferences(this).setDefaultAccount(account);

	}

	private void jumpToMain() {
		LocalSearch search = new LocalSearch();
		search.addAllowedFolder(Account.INBOX);
		search.addAccountUuid(mAccountUuid);
		Main4TabActivity.actionDisplaySearch(this, search, false, true);
	}

	@Override
	public void setWebViewTitle(String title) {
		tvTitle.setText(title);
	}
}
