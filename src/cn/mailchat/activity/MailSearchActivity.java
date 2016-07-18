package cn.mailchat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.adapter.SearchAutoCompleteAdapter;
import cn.mailchat.fragment.MessageListFragment;
import cn.mailchat.fragment.MessageListFragment.MessageListFragmentListener;
import cn.mailchat.mail.Flag;
import cn.mailchat.mail.Message;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.search.LocalSearch;
import cn.mailchat.search.SearchSpecification.Attribute;
import cn.mailchat.search.SearchSpecification.SearchCondition;
import cn.mailchat.search.SearchSpecification.Searchfield;
import cn.mailchat.utils.GlobalTools;

import com.umeng.analytics.MobclickAgent;

public class MailSearchActivity extends BaseActionbarFragmentActivity
        implements OnClickListener,MessageListFragmentListener {
    public static final String ACCOUNTUUID = "accountUuid";
    public static final String EXTRA_SEARCH_ACCOUNT = "cn.mailchat.search_account";
    private static final String EXTRA_SEARCH_FOLDER = "cn.mailchat.search_folder";

    public static enum SearchRange {
        SENDER, RECIPIENTS, SUBJECT, TEXT
    }
    
    // private MessagingController controller;
    private View mCustomActionbarView;
    private Account mAccount;
    private String mFolder;
    private MessageListFragment mMessageListFragment;
    private LocalSearch mSearch;
    private String mKeyWord = "";
    private AutoCompleteTextView mSearchText;
    private ImageView mCleanButton;
    private TextView mSearchButton;
    private boolean mInitRemoteSearch = false;
    
    //#MessageListFragmentListener start
        @Override
        public void updateMenu() {
            
        }

        @Override
        public boolean startSearch(Account account, String folderName) {
            return true;
        }

        @Override
        public void showThread(Account account, String folderName, long rootId) {
            
        }

        @Override
        public void showMoreFromSameSender(String senderAddress) {
            
        }

        @Override
        public void setUnreadCount(int unread) {
            
        }

        @Override
        public void setMessageListTitle(String title) {
            
        }

        @Override
        public void setMessageListSubTitle(String subTitle) {
            
        }

        @Override
        public void setMessageListProgress(int level) {
            
        }

        @Override
        public void remoteSearchStarted() {
            
        }

        @Override
        public void openMessage(MessageReference messageReference) {
            Preferences prefs = Preferences.getPreferences(this);
            Account account = prefs.getAccount(messageReference.accountUuid);
            
            String folderName = messageReference.folderName;

            if (folderName.equals(account.getDraftsFolderName())) {
                MailComposeActivity.actionEdit(this,
                        messageReference,
                        MailComposeActivity.SourceFolder.DRAFT);
            } else {
                // 跳转到邮件详情页面
                MailDetialActivity.actionDisplayMailDetial(this, messageReference);
            }
        }

        @Override
        public void onResendMessage(Message message) {
            
        }


        @Override
        public void onForward(Message message) {
            MailComposeActivity.actionForward(this,
                    message.getFolder().getAccount(),
                    message,
                    null);
        }

        @Override
        public void onReply(Message message) {
            MailComposeActivity.actionReply(this,
                    message.getFolder().getAccount(),
                    message,
                    false,
                    null);
        }

        @Override
        public void onReplyAll(Message message) {
            MailComposeActivity.actionReply(this,
                    message.getFolder().getAccount(),
                    message,
                    true,
                    null);
        }


        @Override
        public void onCompose(Account account) {

        }

        @Override
        public void isHideHomeBottomBar(boolean b) {
            
        }

        @Override
        public void goBack() {
            
        }
        
        @Override
        public void onResumed() {
            if (mInitRemoteSearch && mMessageListFragment != null) {
                mInitRemoteSearch = false;
                mMessageListFragment.onRemoteSearch();
            }
        }

        @Override
        public void enableActionBarProgress(boolean enable) {
            
        }

        @Override
        public void updateMailSearchReasut(int searchReasutCount) {
            
        }
    //#MessageListFragmentListener end

    private RelativeLayout empty_view_layout;
    private FrameLayout mail_list_layout;
    
   public static void actionFolderSearch(Context context, Account account, String folder) {
        Intent intent = new Intent(context, MailSearchActivity.class);
        intent.putExtra(EXTRA_SEARCH_ACCOUNT, account.getUuid());
        intent.putExtra(EXTRA_SEARCH_FOLDER, folder);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_search);

        String accountUuid = getIntent().getStringExtra(ACCOUNTUUID);
        if (accountUuid == null) {
            accountUuid = getIntent().getStringExtra(EXTRA_SEARCH_ACCOUNT);
        }
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
        mFolder = getIntent().getStringExtra(EXTRA_SEARCH_FOLDER);

        initializeActionBar();
        initView();
        initData();
        initEvent();
    }

    public boolean onSearchRequested(Account account, String folderName) {
        if (account != null && folderName != null) {
            final Bundle appData = new Bundle();
            appData.putString(EXTRA_SEARCH_ACCOUNT, account.getUuid());
            appData.putString(EXTRA_SEARCH_FOLDER, folderName);
            startSearch(null, false, appData, false);
        } else {
            startSearch(null, false, null, false);
        }

        return true;
    }

    private void initView() {
        mSearchText = (AutoCompleteTextView) mCustomActionbarView.findViewById(R.id.search_et);
        
        if (mFolder != null && mFolder.length() > 0) {
            String folderName = FolderInfoHolder.getDisplayName(getApplicationContext(),
                    mAccount,
                    mFolder);
            String hintText = String.format(getString(R.string.search_hint_text), folderName);
            mSearchText.setHint(hintText);
        }
        
        mCleanButton = (ImageView) mCustomActionbarView.findViewById(R.id.clean_list_end);
        mSearchButton=(TextView) findViewById(R.id.tv_sure);
        empty_view_layout = (RelativeLayout) findViewById(R.id.empty_view_layout);
        mail_list_layout = (FrameLayout) findViewById(R.id.mail_list_layout);
        TextView tv = (TextView) findViewById(R.id.tv_empty_view);
        setEmptyViewShow(-1);
        tv.setHint(getResources().getString(R.string.no_search_data));
    }

    private void setEmptyViewShow(int searchCount) {
        if (!mSearchText.getText().toString().trim().isEmpty() || searchCount != -1) {
            empty_view_layout.setVisibility(View.GONE);
            mail_list_layout.setVisibility(View.VISIBLE);
        } else {
            empty_view_layout.setVisibility(View.VISIBLE);
            mail_list_layout.setVisibility(View.GONE);
        }
    }

    private LocalSearch createKeyWord(SearchRange range) {
        String query = mKeyWord;
        mSearch = new LocalSearch(getString(R.string.search_results));
        mSearch.setManualSearch(true);
        
        switch (range) {
        case SENDER:
            mSearch.or(new SearchCondition(Searchfield.SENDER, Attribute.CONTAINS, query));
            mSearch.or(new SearchCondition(Searchfield.REPLY_TO, Attribute.CONTAINS, query));
            break;
        case RECIPIENTS:
            mSearch.or(new SearchCondition(Searchfield.TO, Attribute.CONTAINS, query));
            mSearch.or(new SearchCondition(Searchfield.CC, Attribute.CONTAINS, query));
            mSearch.or(new SearchCondition(Searchfield.BCC, Attribute.CONTAINS, query));
            break;
        case SUBJECT:
            mSearch.or(new SearchCondition(Searchfield.SUBJECT, Attribute.CONTAINS, query));
            break;
        case TEXT:
            mSearch.or(new SearchCondition(Searchfield.SENDER, Attribute.CONTAINS, query));
            mSearch.or(new SearchCondition(Searchfield.REPLY_TO, Attribute.CONTAINS, query));
            mSearch.or(new SearchCondition(Searchfield.TO, Attribute.CONTAINS, query));
            mSearch.or(new SearchCondition(Searchfield.CC, Attribute.CONTAINS, query));
            mSearch.or(new SearchCondition(Searchfield.BCC, Attribute.CONTAINS, query));
            mSearch.or(new SearchCondition(Searchfield.SUBJECT, Attribute.CONTAINS, query));
            mSearch.or(new SearchCondition(Searchfield.MESSAGE_CONTENTS, Attribute.CONTAINS, query));
            break;
        }
        
        mSearch.or(new SearchCondition(Searchfield.FLAG, Attribute.CONTAINS, "X_REMOTE_SEARCH_MAIL"));
        mSearch.addAccountUuid(mAccount.getUuid());
        if (mFolder != null) {
            mSearch.addAllowedFolder(mFolder);
        }
        return mSearch;
    }

    private void initData() {
        SearchAutoCompleteAdapter adapter = new SearchAutoCompleteAdapter(mSearchText);
        mSearchText.setAdapter(adapter);
        mSearchText.setThreshold(1);
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

    private void initEvent() {
        mCleanButton.setOnClickListener(this);
        mSearchButton.setOnClickListener(this);

        mSearchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                    View view,
                    int position,
                    long id) {
                switch (position) {
                case 0:
                    MailChat.remoteSearchRange = SearchRange.SENDER;
                    MailSearchActivity.this.search(SearchRange.SENDER);
                    break;
                case 1:
                    MailChat.remoteSearchRange = SearchRange.RECIPIENTS;
                    MailSearchActivity.this.search(SearchRange.RECIPIENTS);
                    break;
                case 2:
                    MailChat.remoteSearchRange = SearchRange.SUBJECT;
                    MailSearchActivity.this.search(SearchRange.SUBJECT);
                    break;
                case 3:
                    MailChat.remoteSearchRange = SearchRange.TEXT;
                    MailSearchActivity.this.search(SearchRange.TEXT);
                    break;
                default:
                    MailChat.remoteSearchRange = SearchRange.TEXT;
                    MailSearchActivity.this.search(SearchRange.TEXT);
                }
            }
        });
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.clean_list_end:
            mSearchText.setText("");
            break;
        case R.id.tv_sure:
            search(MailChat.remoteSearchRange);
            break;
        default:
            break;
        }
    }
    
    public void search(SearchRange range) {
        if (mMessageListFragment != null && mMessageListFragment.mIsLoadOngoing.get()) {
            MailChat.toast(getString(R.string.message_list_refresh_ongoing));
            return;
        }
        
        if (mFolder != null && mFolder.length() > 0) {
            try {
                Message[] messages = mAccount.getLocalStore().getFolder(mFolder).getMessages(null);
                for (Message message : messages) {
                    if (message.isSet(Flag.X_REMOTE_SEARCH_MAIL)) {
                        message.setFlag(Flag.X_REMOTE_SEARCH_MAIL, false);
                    }
                }
            } catch (MessagingException e) {
                // DO NOTHING
            }
        }
        
        mKeyWord = mSearchText.getText().toString().trim();
        if (mKeyWord.length() <=0 && mKeyWord.equals("")) {
            Toast.makeText(MailSearchActivity.this, R.string.no_search_data, Toast.LENGTH_SHORT).show();
            return;
        }
        
        MailChat.remoteSearchPaging = false;
        MailChat.remoteSearchStart = 0;
        MailChat.remoteSearchSize = 20;
        MailChat.remoteSearchQuery = null;
        
        setEmptyViewShow(1);
        
        mAccount.setAllowRemoteSearch(true);
        mAccount.setRemoteSearchNumResults(10);
        mInitRemoteSearch = true;
        
        if (mMessageListFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(mMessageListFragment).commit();
        }
        
        mMessageListFragment = MessageListFragment.newInstance(
                createKeyWord(range),
                false,
                false);

        getSupportFragmentManager().beginTransaction()
            .add(R.id.mail_list_layout, mMessageListFragment).commit();

        MobclickAgent.onEvent(this, "search");

        GlobalTools.hideSoftInput(this);
        
        //mMessageListFragment.onRemoteSearch();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //controller.removeListener(listener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("MailSearchActivity"); //统计页面
        MobclickAgent.onResume(this);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("MailSearchActivity"); //统计页面
        MobclickAgent.onPause(this);
    }
}