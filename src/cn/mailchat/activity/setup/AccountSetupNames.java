package cn.mailchat.activity.setup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.text.method.TextKeyListener.Capitalize;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.BaseAccount;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.MailChatActivity;
import cn.mailchat.activity.Main4TabActivity;
import cn.mailchat.helper.Utility;
import cn.mailchat.search.LocalSearch;
import cn.mailchat.search.SearchAccount;


public class AccountSetupNames extends MailChatActivity implements OnClickListener {
	private String TAG = "AccountSetupNames";
	private static final String EXTRA_ACCOUNT = "account";

	private EditText mDescription;

	private EditText mName;

	private Account mAccount;

	private Button mDoneButton;

	public static void actionSetNames(Context context, Account account) {
		Intent i = new Intent(context, AccountSetupNames.class);
		i.putExtra(EXTRA_ACCOUNT, account.getUuid());
		context.startActivity(i);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置无标题
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_setup_names);
		initTitleBar();
		mDescription = (EditText) findViewById(R.id.account_description);
		mName = (EditText) findViewById(R.id.account_name);
		mDoneButton = (Button) findViewById(R.id.done);
		mDoneButton.setOnClickListener(this);

		TextWatcher validationTextWatcher = new TextWatcher() {
			public void afterTextChanged(Editable s) {
				validateFields();
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		};
		mName.addTextChangedListener(validationTextWatcher);

		mName.setKeyListener(TextKeyListener.getInstance(false,
				Capitalize.WORDS));

		String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
		mAccount = Preferences.getPreferences(this).getAccount(accountUuid);

		/*
		 * Since this field is considered optional, we don't set this here. If
		 * the user fills in a value we'll reset the current value, otherwise we
		 * just leave the saved value alone.
		 */
		// mDescription.setText(mAccount.getDescription());
		
		// modify by zhangyq 因account的getName是取identities.get(0).getName();
		// 所以当第一个账号登陆后，getName就将有值，这在多账号登陆时不符合需求
//		if (mAccount.getName() != null && !"".equals(mAccount.getName())) {
//			mName.setText(mAccount.getName());
//		} else {
			mName.setText(mAccount.getEmail().substring(0, mAccount.getEmail().indexOf("@")));
//		}
		if (!Utility.requiredFieldValid(mName)) {
			mDoneButton.setEnabled(false);
		}
		
		// Skip setup sender nickname step.
		// Modified by LL
		// BEGIN
		onNext();
		// END
	}
	private void initTitleBar() {
		ImageView imgBack=(ImageView) findViewById(R.id.back);
		imgBack.setVisibility(View.GONE);
		TextView tvTitle=(TextView) findViewById(R.id.title);
		tvTitle.setText(getString(R.string.account_setup_nickname));
	}
	private void validateFields() {
		mDoneButton.setEnabled(Utility.requiredFieldValid(mName));
		Utility.setCompoundDrawablesAlpha(mDoneButton,
				mDoneButton.isEnabled() ? 255 : 128);
	}

	protected void onNext() {
		if (Utility.requiredFieldValid(mDescription)) {
			mAccount.setDescription(mDescription.getText().toString());
		}
		mAccount.setName(mName.getText().toString());
		mAccount.save(Preferences.getPreferences(this));
		// Accounts.listAccounts(this);
		Account account = Preferences.getPreferences(this).getAccount(
				mAccount.getUuid());
		Preferences.getPreferences(this).setDefaultAccount(account);
		onOpenAccount(mAccount);
		finish();
	}
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.done:
			onNext();
			break;
		}
	}
	
	/**
	 * 
	 * method name: onKeyDown 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 * function@Description: TODO
	 *  @History memory：
	 *     @Date：2014-12-3 下午3:56:37	@Modified by：zhangjx
	 *     @Description：用户到该界面不让用户返回
	 */
	@Override    
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
	if(keyCode == KeyEvent.KEYCODE_BACK){      
	return  true;
	}  
	return  super.onKeyDown(keyCode, event);     

	} 
	/**
	 * Show that account's inbox or folder-list or return false if the account
	 * is not available.
	 * 
	 * @param account
	 *            the account to open ({@link SearchAccount} or {@link Account})
	 * @return false if unsuccessfull
	 */
	private boolean onOpenAccount(BaseAccount account) {
		if (account instanceof SearchAccount) {
			SearchAccount searchAccount = (SearchAccount) account;
			Main4TabActivity.actionDisplaySearch(this,
					searchAccount.getRelatedSearch(), false, false);
		} else {
			Account realAccount = (Account) account;
			if (!realAccount.isEnabled()) {
				// onActivateAccount(realAccount);
				return false;
			} else if (!realAccount.isAvailable(this)) {
				String toastText = getString(R.string.account_unavailable,
						account.getDescription());
				Toast toast = Toast.makeText(getApplication(), toastText,
						Toast.LENGTH_SHORT);
				toast.show();

				Log.i(MailChat.LOG_TAG,
						"refusing to open account that is not available");
				return false;
			}
			if (MailChat.FOLDER_NONE.equals(realAccount.getAutoExpandFolderName())) {
				//Main4TabActivity.actionHandleAccount(this, realAccount);
			} else {
				LocalSearch search = new LocalSearch(
						realAccount.getAutoExpandFolderName());
				search.addAllowedFolder(realAccount.getAutoExpandFolderName());
				search.addAccountUuid(realAccount.getUuid());
				Main4TabActivity.actionDisplaySearch(this, search, false, true);
			}
		}
		return true;
	}

}
