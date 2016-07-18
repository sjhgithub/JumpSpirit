package cn.mailchat.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.mailchat.Account;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.fragment.contact.ContactTabsFragment;
import cn.mailchat.fragment.contact.ContactTabsFragment.ContactTabsFragmentListener;
import cn.mailchat.fragment.contact.SearchResultFragment;
import cn.mailchat.fragment.contact.SearchResultFragment.SearchAllContactListener;

import com.umeng.analytics.MobclickAgent;

public class ContactSearchActivity extends BaseActionbarFragmentActivity
		implements OnClickListener, SearchAllContactListener {
	public static final String ACCOUNTUUID = "accountUuid";
	public static final String EXTRA_SEARCH_ACCOUNT = "cn.mailchat.search_account";

	// private MessagingController controller;
	private View mCustomActionbarView;
	private Account mAccount;
	private String mKeyWord = "";
	private AutoCompleteTextView mSearchText;
	private ImageView mCleanButton;
	private SearchResultFragment mContactListFragment;
	private View emptyLayoutView;
	private View layoutContainer;
	private TextView mSearchButton;
	private TextView tvNoSearchData;
	private ImageView iconSearchbg;

	public static void actionContactSearch(Context context, Account account) {
		Intent intent = new Intent(context, ContactSearchActivity.class);
		intent.putExtra(EXTRA_SEARCH_ACCOUNT, account.getUuid());
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_fragment);

		String accountUuid = getIntent().getStringExtra(ACCOUNTUUID);
		if (accountUuid == null) {
			accountUuid = getIntent().getStringExtra(EXTRA_SEARCH_ACCOUNT);
		}
		mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
		initializeActionBar();
		initView();
		showSoftInput();
		initData();
		initEvent();
	}

	private void showSoftInput() {
		new Timer().schedule(new TimerTask() {
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) mSearchText
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(mSearchText, 0);
			}
		}, 1 * 1000);
	}

	private void initView() {
		mSearchText = (AutoCompleteTextView) mCustomActionbarView
				.findViewById(R.id.search_et);
		mSearchText.setHint(getString(R.string.hit_search_contact));
		mCleanButton = (ImageView) mCustomActionbarView
				.findViewById(R.id.clean_list_end);
		mSearchButton = (TextView) findViewById(R.id.tv_sure);
		emptyLayoutView = (RelativeLayout) findViewById(R.id.empty_view_layout);
		layoutContainer = (FrameLayout) findViewById(R.id.container);
		tvNoSearchData = (TextView) findViewById(R.id.tv_empty_view);
		iconSearchbg = (ImageView) mCustomActionbarView
				.findViewById(R.id.img_search);
		setEmptyViewShow(-1);
	}

	private void setEmptyViewShow(int searchCount) {
		if (!mSearchText.getText().toString().trim().isEmpty()
				|| searchCount != -1) {
			emptyLayoutView.setVisibility(View.GONE);
			layoutContainer.setVisibility(View.VISIBLE);
		} else {
			emptyLayoutView.setVisibility(View.VISIBLE);
			layoutContainer.setVisibility(View.GONE);
		}
	}

	private void initData() {
		mContactListFragment = SearchResultFragment.newInstance();
		FragmentTransaction trans = getSupportFragmentManager()
				.beginTransaction();
		trans.replace(R.id.container, mContactListFragment);
		trans.commit();
	}

	public void initializeActionBar() {
		mActionBar.setTitle(null);
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayUseLogoEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);

		// Inflate the custom view
		LayoutInflater inflater = LayoutInflater.from(this);
		mCustomActionbarView = inflater.inflate(
				R.layout.actionbar_custom_search_bar, null);
		mActionBar.setCustomView(mCustomActionbarView);

	}

	private void initEvent() {
		mCleanButton.setOnClickListener(this);
		mSearchButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.clean_list_end:
			mSearchText.setText("");
			break;
		case R.id.tv_sure:
			mContactListFragment.doSearch(mSearchText.getText().toString()
					.trim());
			break;
		default:
			break;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("ContactSearchActivity"); // 统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("ContactSearchActivity"); // 统计页面
		MobclickAgent.onPause(this);
	}

	@Override
	public void doSearchStrart() {

	}

	@Override
	public void doSearchFailed() {

	}

	@Override
	public void doSearchFinish(int size) {
		if (size == 0) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setEmptyViewShow(-1);
				}
			});
		} else {
			setEmptyViewShow(1);
		}
	}
}
