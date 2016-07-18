package cn.mailchat.activity;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import cn.mailchat.Account;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.fragment.ForwardViewPagerFragment;

public class ForwardActivity extends BaseActionbarFragmentActivity implements
		TextWatcher, OnClickListener {
	public static final String ACCOUNTUUID = "accountUuid";
	public static final String MESSAGE = "message";
	public static final String SINGLE_ATTACHMENT = "single_attachment";
	private Account account;
	private MessagingController controller;
	/**
	 * 从聊天界面跳转
	 * 
	 * @Description:
	 * @param context
	 * @param mMessage
	 * @param mAccount
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-6-5
	 */
	public static void actionForChat(Context context, Serializable mMessage,
			Account mAccount) {
		Intent intent = new Intent(context, ForwardActivity.class);
		intent.putExtra(MESSAGE, mMessage);
		intent.putExtra(ACCOUNTUUID, mAccount.getUuid());
		context.startActivity(intent);
	}

	/**
	 * 从读信页面附件选项菜单跳转，转发单个附件
	 *
	 * @author linli1
	 */
	public static void actionForwardSingleAttachment(Context context,
			Parcelable singleAttachment, Account account) {
		Intent intent = new Intent(context, ForwardActivity.class);
		intent.putExtra(SINGLE_ATTACHMENT, singleAttachment);
		intent.putExtra(ACCOUNTUUID, account.getUuid());
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	private TextView mTitle;
	private EditText searchText;
	private ImageView clean;
	private MenuItem menuSearch;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_share, menu);
		menuSearch = menu.findItem(R.id.action_search_share_item);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case R.id.action_search_share_item:
			View v = replayToSearchView();
			v.findViewById(R.id.tv_sure).setVisibility(View.GONE);
			menuSearch.setVisible(false);
			searchText = (EditText) v.findViewById(R.id.search_et);
			searchText.setFocusable(true);
			clean = (ImageView) v.findViewById(R.id.clean_list_end);
			clean.setOnClickListener(this);
			searchText.addTextChangedListener(this);
			showSoftInput(searchText);
		}
		return super.onOptionsItemSelected(item);
	}

	private void showSoftInput(final EditText view) {
		new Timer().schedule(new TimerTask() {
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) view
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(view, 0);
			}
		}, 688);
	}

	private View replayToSearchView() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View mSearchActionbarView = inflater.inflate(
				R.layout.actionbar_custom_search_bar, null);
		mActionBar.setCustomView(mSearchActionbarView);
		return mSearchActionbarView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_viewpager_container);
		initData(savedInstanceState);
		initializeActionBar();
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
		View mCustomActionbarView = inflater.inflate(
				R.layout.actionbar_custom_only_title, null);
		mActionBar.setCustomView(mCustomActionbarView);
		mTitle = (TextView) mCustomActionbarView.findViewById(R.id.tv_title);
		setActionbarCenterTitle(mCustomActionbarView, mTitle,
				getString(R.string.forwarding_to));
	}

	private void initData(Bundle savedInstanceState) {
		Intent intent = getIntent();
		String uuid=null;
		if(intent!=null){
			uuid = intent.getStringExtra(ACCOUNTUUID);
		}else {
			return;
		}
		if (savedInstanceState == null) {
			ForwardViewPagerFragment forwardViewPagerFragment = new ForwardViewPagerFragment();
			Bundle args = new Bundle();
			args.putSerializable(MESSAGE, intent.getSerializableExtra(MESSAGE));
			args.putString(ACCOUNTUUID, uuid);
			args.putParcelable(SINGLE_ATTACHMENT,
					intent.getParcelableExtra(SINGLE_ATTACHMENT));
			forwardViewPagerFragment.setArguments(args);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, forwardViewPagerFragment).commit();
		}
		account = Preferences.getPreferences(getApplication()).getAccount(uuid);
		controller = MessagingController.getInstance(getApplication());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.clean_list_end:
			searchText.setText("");
			break;
		}

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		Log.d("shengli", ""+ s);
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		String keyword=s.toString().trim();
		controller.searchMixedChattingByForward(account,keyword);
		controller.searchContactsByForward(account,keyword);
	}

}
