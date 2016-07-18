package cn.mailchat.activity;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.umeng.analytics.MobclickAgent;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBar.LayoutParams;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cn.mailchat.Account;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.adapter.ChattingAdapter;
import cn.mailchat.adapter.ChattingSearchAdapter;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.CMessage;
import cn.mailchat.chatting.beans.DChatMessage;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;

/**
 * 
 * @copyright © 35.com
 * @file name ：SearchChattingActivity.java
 * @author ：zhangjx
 * @create Data ：2014-11-5下午5:12:22
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-11-5下午5:12:22
 * @Modified by：zhangjx
 * @Description : 聊天信息搜索
 */
public class SearchChattingActivity extends BaseActionbarFragmentActivity
		implements OnClickListener {

	public static final String EXTRA_ACCOUNT_UUID = "uuid";
	public static final String EXTRA_GROUP_ID = "groupId";
	public static final String EXTRA_GROUP = "group";
	public static final String EXTRA_IS_DCHAT = "is_dChat";
	public static final String EXTRA_DCHAT_UID = "dChat_uid";
	private Context mContext;
	private Account mAccount;
	private String mGroupId;
	private CGroup mGroup;

	private MessagingController mController;
	private ChattingAdapter historyAdapter;
	private ChattingSearchAdapter searchAdapter;
	private String currentKeyWord;
	/*** views ***/
	private EditText searchEditText;
	private ListView historyListView;
	private ListView searchListView;

	private FrameLayout searchChattingLayout;

	private MediaPlayer mediaPlayer;
	private boolean isDChat;
	private String dChatUid;
	private View mCustomActionbarView;
	private ImageView clean;
	private TextView noResult;
	public static void actionSearch(Context context, Account account,
			CGroup mGroup) {
		Intent intent = new Intent(context, SearchChattingActivity.class);
		intent.putExtra(EXTRA_ACCOUNT_UUID, account.getUuid());
		intent.putExtra(EXTRA_GROUP, mGroup);
		context.startActivity(intent);
	}

	public static void actionSearch(boolean isDchat, Context context,
			Account account, String dChatUid) {
		Intent intent = new Intent(context, SearchChattingActivity.class);
		intent.putExtra(EXTRA_IS_DCHAT, isDchat);
		intent.putExtra(EXTRA_ACCOUNT_UUID, account.getUuid());
		intent.putExtra(EXTRA_DCHAT_UID, dChatUid);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_chatting_search);
		initParamFromIntent(getIntent());
		initParam();
		initView();
		initializeActionBar();
		
//		final InputMethodManager inputMethodManager =(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//		Timer timer = new Timer();
//		timer.schedule(new TimerTask(){
//		    @Override
//		    public void run() {
//		    	inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//		    }
//		}, 1000);
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
				R.layout.actionbar_custom_search_bar, null);
		mActionBar.setCustomView(mCustomActionbarView);
		initSearchView();

	}

	public void initSearchView() {
		searchEditText = (EditText) mCustomActionbarView
				.findViewById(R.id.search_et);
		clean = (ImageView) mCustomActionbarView
				.findViewById(R.id.clean_list_end);
		clean.setOnClickListener(this);
		mCustomActionbarView.findViewById(R.id.tv_sure)
				.setVisibility(View.GONE);
		searchEditText.setHint(getResources().getString(R.string.hint_chatting_search));
		searchEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				onSearch(s.toString());
			}
		});
	}

	private void initParamFromIntent(Intent intent) {
		mAccount = Preferences.getPreferences(this).getAccount(
				intent.getStringExtra(EXTRA_ACCOUNT_UUID));
		/**
		 * This shouldn't happen !
		 */
		if (mAccount == null) {
			finish();
		}
		// 单聊跳转过来的
		isDChat = intent.getBooleanExtra(EXTRA_IS_DCHAT, false);
		if (isDChat) {
			dChatUid = intent.getStringExtra(EXTRA_DCHAT_UID);
		} else {
			// 群聊跳转过来
			mGroup = (CGroup) intent.getSerializableExtra("group");
			mGroupId = mGroup.getUid();

		}
	}

	private void initParam() {
		mContext = SearchChattingActivity.this;
		mController = MessagingController.getInstance(getApplication());
		mController.addListener(listener);
		mediaPlayer = new MediaPlayer();
		searchListView = (ListView) findViewById(R.id.lv_chatting_search_result);
		if (!isDChat) {
			historyAdapter = new ChattingAdapter(mContext, searchListView,
					mAccount, mGroup, null, mediaPlayer);
		}
		searchAdapter = new ChattingSearchAdapter(mContext);
	}

	private void initView() {
		noResult = (TextView) findViewById(R.id.no_result);
		searchChattingLayout = (FrameLayout) findViewById(R.id.layoutView_search_chatting_content);
		searchChattingLayout.setVisibility(View.INVISIBLE);
		historyListView = (ListView) findViewById(R.id.lv_chatting_search_history);
		historyListView.setAdapter(historyAdapter);
		searchListView.setAdapter(searchAdapter);

		searchListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
//				CMessage message = (CMessage) parent
//						.getItemAtPosition(position);
//				onViewMessage(message);
			}
		});
	}

	private void onViewMessage(CMessage message) {
	}

	private void onSearch(String keyWord) {
		currentKeyWord = keyWord.trim();
		if (currentKeyWord.equals("")) {
			searchChattingLayout.setVisibility(View.INVISIBLE);
			searchListView.setVisibility(View.GONE);
			historyListView.setVisibility(View.VISIBLE);
		} else {
			searchChattingLayout.setVisibility(View.VISIBLE);
			searchListView.setVisibility(View.VISIBLE);
			historyListView.setVisibility(View.GONE);
			if (isDChat) {
				mController.searchDChatMessages(mAccount, dChatUid,
						currentKeyWord, listener);

			} else {
				mController.searchGroupMessages(mAccount, mGroupId,
						currentKeyWord, null);
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.clean_list_end:
			searchEditText.setText("");
			break;
		}
	}

	@Override
	protected void onDestroy() {
		if (mediaPlayer != null) {
			mediaPlayer.release();
		}
		mController.removeListener(listener);
		super.onDestroy();
	}

	private MessagingListener listener = new MessagingListener() {

		@Override
		public void searchGroupMessagesFinished(String uuid, String groupUid,
				final String keyWord, final List<CMessage> messages) {
			if (currentKeyWord.equals(keyWord) && groupUid.equals(mGroupId)) {
				runOnUiThread(new Runnable() {

					public void run() {
						if(messages.size()>0){
							searchAdapter.showSearchResult(messages, null,null,
									currentKeyWord);
							searchListView.setVisibility(View.VISIBLE);
							historyListView.setVisibility(View.GONE);
							noResult.setVisibility(View.GONE);
						}else{
							searchListView.setVisibility(View.GONE);
							if(searchEditText.getText().toString().trim().equals("")){
								noResult.setVisibility(View.INVISIBLE);
							}else{
								noResult.setVisibility(View.VISIBLE);
							}
						}
					}
				});
			}

		}

		@Override
		public void searchDChatMessagesFinished(String uuid, String dChatUid,
				final String currentKeyWord,
				final List<DChatMessage> localMessages,final List<String> nickNameList) {
			if (currentKeyWord.equals(currentKeyWord)
					&& dChatUid.equals(dChatUid)) {
				runOnUiThread(new Runnable() {
					public void run() {
						if(localMessages.size()>0){
							searchAdapter.showSearchResult(null, localMessages,nickNameList,
									currentKeyWord);
							searchListView.setVisibility(View.VISIBLE);
							historyListView.setVisibility(View.GONE);
							noResult.setVisibility(View.GONE);
						}else{
							searchListView.setVisibility(View.GONE);
							if(searchEditText.getText().toString().trim().equals("")){
								noResult.setVisibility(View.INVISIBLE);
							}else{
								noResult.setVisibility(View.VISIBLE);
							}
						}
					}
				});
			}
		}
	};
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("SearchChattingActivity"); //统计页面
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("SearchChattingActivity"); //统计页面
		MobclickAgent.onPause(this);
	}
}
