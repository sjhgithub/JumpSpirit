package cn.mailchat.activity;

import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.fragment.ChatListFragment;
import cn.mailchat.fragment.ChatListFragment.ChatListFragmentListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ChatSearchActivity extends BaseActionbarFragmentActivity implements ChatListFragmentListener,OnClickListener {
	public static final String ACCOUNTUUID = "accountUuid";
	private Account mAccount;
	private View mCustomActionbarView;
	private String mKeyWord = "";
    private AutoCompleteTextView mSearchText;
    private ImageView mCleanButton;
    private TextView mSearchButton;
    private RelativeLayout emptyLayoutView;
    private FrameLayout chatListLayout;
    private ChatListFragment mChatListFragment;
    
    public static void actionChatSearch(Context context, Account account) {
        Intent intent = new Intent(context, ChatSearchActivity.class);
        intent.putExtra(ACCOUNTUUID, account.getUuid());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_search);
		String accountUuid = getIntent().getStringExtra(ACCOUNTUUID);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
        initializeActionBar();
        initView();
        initEvent();
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
        mCustomActionbarView = inflater.inflate(R.layout.actionbar_custom_search_bar, null);
        mActionBar.setCustomView(mCustomActionbarView);

    }
	private void initView() {
        mSearchText = (AutoCompleteTextView) mCustomActionbarView.findViewById(R.id.search_et);
        mSearchText.setHint(getString(R.string.search_msg_hint_text));
        mCleanButton = (ImageView) mCustomActionbarView.findViewById(R.id.clean_list_end);
        mSearchButton=(TextView) findViewById(R.id.tv_sure);
        emptyLayoutView = (RelativeLayout) findViewById(R.id.empty_view_layout);
        chatListLayout = (FrameLayout) findViewById(R.id.chat_list_layout);
        TextView tv = (TextView) findViewById(R.id.tv_empty_view);
        tv.setHint(getResources().getString(R.string.no_search_data));
        setEmptyViewShow(-1);
    }
	private void initEvent(){
		mSearchButton.setOnClickListener(this);
		mCleanButton.setOnClickListener(this);
	}
	private void setEmptyViewShow(int searchCount) {
        if (!mSearchText.getText().toString().trim().isEmpty() || searchCount != -1) {
        	emptyLayoutView.setVisibility(View.GONE);
        	chatListLayout.setVisibility(View.VISIBLE);
        } else {
        	emptyLayoutView.setVisibility(View.VISIBLE);
        	chatListLayout.setVisibility(View.GONE);
        }
    }
	
	private void onSearch(){
		mKeyWord = mSearchText.getText().toString().trim();
        if (mKeyWord.length() <=0 && mKeyWord.equals("")) {
            Toast.makeText(ChatSearchActivity.this, R.string.no_search_data, Toast.LENGTH_SHORT).show();
            return;
        }
		if (mChatListFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(mChatListFragment).commit();
        }
		mChatListFragment = ChatListFragment.newInstance(mSearchText.getText().toString().trim(),true);
        getSupportFragmentManager().beginTransaction().add(R.id.chat_list_layout, mChatListFragment).commit();
        setEmptyViewShow(1);
	}

	@Override
	public void updateChatSearchReasut(int searchReasutCount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.clean_list_end:
			mSearchText.setText("");
			break;
		case R.id.tv_sure:
			onSearch();
			break;
		default:
			break;
		}
	}
}
