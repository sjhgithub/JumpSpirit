package cn.mailchat.activity.setup;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import cn.mailchat.*;
import cn.mailchat.activity.MailChatActivity;
import cn.mailchat.activity.setup.AccountSetupCheckSettings.CheckDirection;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.helper.Utility;
import cn.mailchat.mail.AuthType;
import cn.mailchat.mail.ConnectionSecurity;
import cn.mailchat.mail.ServerSettings;
import cn.mailchat.mail.Store;
import cn.mailchat.mail.Transport;
import cn.mailchat.mail.transport.SmtpTransport;
import cn.mailchat.view.ClientCertificateSpinner;
import cn.mailchat.view.LoginDialog;
import cn.mailchat.view.ClientCertificateSpinner.OnClientCertificateChangedListener;


import java.net.URI;
import java.net.URISyntaxException;

import com.umeng.analytics.MobclickAgent;

public class AccountSetupOutgoing extends MailChatActivity implements OnClickListener,
    OnCheckedChangeListener {
    private static final String EXTRA_ACCOUNT = "account";
    private static final String EXTRA_MAKE_DEFAULT = "makeDefault";
    private static final String EXTRA_IS_BACK = "isBack";
    private static final String EXTRA_IS_AUTH = "isAuth";
    private static final String STATE_SECURITY_TYPE_POSITION = "stateSecurityTypePosition";
    private static final String STATE_AUTH_TYPE_POSITION = "authTypePosition";
    private static final String EXTRA_IS_35EMAIL = "is35Mail";
    private static final String SMTP = "25";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_SSL_PORT = "465";


    private EditText mUsernameView;
    private EditText mPasswordView;
    private ClientCertificateSpinner mClientCertificateSpinner;
    private TextView mClientCertificateLabelView;
    private TextView mPasswordLabelView;
    private EditText mServerView;
    private EditText mPortView;
    private String mCurrentPortViewSetting;
    private CheckBox mRequireLoginView;
    private ViewGroup mRequireLoginSettingsView;
    private Spinner mSecurityTypeView;
    private int mCurrentSecurityTypeViewPosition;
    private Spinner mAuthTypeView;
    private int mCurrentAuthTypeViewPosition;
    private ArrayAdapter<AuthType> mAuthTypeAdapter;
    private Account mAccount;
    private boolean mMakeDefault;

	private boolean mIsBack;
	private boolean is35Email;
	private String username = null;
	private String password = null;
	// 记录是否因认证错误需要对已有账号配置进行更新
	private boolean mIsAccountUpdate;
	private boolean mIsAuth;
	private String oldTransportUri;

	private ImageView imgShowPassword;

	private boolean isShowPassword=false;

	private TextView mNextBtn;

    public static void actionOutgoingSettings(Context context, Account account, boolean makeDefault,boolean isBack,boolean is35mail) {
        Intent i = new Intent(context, AccountSetupOutgoing.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
        i.putExtra(EXTRA_IS_BACK, isBack);
        i.putExtra(EXTRA_IS_35EMAIL, is35mail);
        context.startActivity(i);
    }

    public static void actionEditOutgoingSettings(Context context, Account account) {
        context.startActivity(intentActionEditOutgoingSettings(context, account));
    }

    public static Intent intentActionEditOutgoingSettings(Context context, Account account) {
        Intent i = new Intent(context, AccountSetupOutgoing.class);
        i.setAction(Intent.ACTION_EDIT);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        return i;
    }

    //编辑认证错误，并更新账户配置
    public static void editAndUpdateOutgoingSettings(Context context, Account account) {
        Intent i = new Intent(context, AccountSetupOutgoing.class);
        i.setAction(Intent.ACTION_EDIT);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        // 标记因认证错误需要对已有账号配置进行更新
        i.putExtra(MailChat.EXTRA_ACCOUNT_UPDATE, true);
        i.putExtra(EXTRA_IS_AUTH, true);
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_setup_outgoing_new);
        MobclickAgent.onEvent(getApplicationContext(), "into_outgoing_setting_act");
        initTitleBar();
        String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);

        try {
            if (new URI(mAccount.getStoreUri()).getScheme().startsWith("webdav")) {
                mAccount.setTransportUri(mAccount.getStoreUri());
                
                //AccountSetupCheckSettings.actionCheckSettings(this, mAccount, CheckDirection.OUTGOING);
                AccountSetupCheckSettings.actionCheckSettings(this, mAccount, CheckDirection.OUTGOING, false, false, is35Email);
            }
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        mUsernameView = (EditText)findViewById(R.id.account_username);
        mPasswordView = (EditText)findViewById(R.id.account_password);
        mClientCertificateSpinner = (ClientCertificateSpinner)findViewById(R.id.account_client_certificate_spinner);
        mClientCertificateLabelView = (TextView)findViewById(R.id.account_client_certificate_label);
        mPasswordLabelView = (TextView)findViewById(R.id.account_password_label);
        mServerView = (EditText)findViewById(R.id.account_server);
        mPortView = (EditText)findViewById(R.id.account_port);
        mRequireLoginView = (CheckBox)findViewById(R.id.account_require_login);
        mRequireLoginSettingsView = (ViewGroup)findViewById(R.id.account_require_login_settings);
        mSecurityTypeView = (Spinner)findViewById(R.id.account_security_type);
        mAuthTypeView = (Spinner)findViewById(R.id.account_auth_type);
//        mNextButton = (Button)findViewById(R.id.next);
        imgShowPassword=(ImageView)findViewById(R.id.img_show_password);
//        mNextButton.setOnClickListener(this);
        imgShowPassword.setOnClickListener(this);
        mSecurityTypeView.setAdapter(ConnectionSecurity.getArrayAdapter(this));

        mAuthTypeAdapter = AuthType.getArrayAdapter(this);
        mAuthTypeView.setAdapter(mAuthTypeAdapter);

        /*
         * Only allow digits in the port field.
         */
        mPortView.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

        //FIXME: get Account object again?
        accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
        mMakeDefault = getIntent().getBooleanExtra(EXTRA_MAKE_DEFAULT, false);
        mIsBack=getIntent().getBooleanExtra(EXTRA_IS_BACK, false);
        is35Email=getIntent().getBooleanExtra(EXTRA_IS_35EMAIL, false);
        
        // 确认是否因认证错误需要对已有账号配置进行更新
        mIsAccountUpdate = getIntent().getBooleanExtra(MailChat.EXTRA_ACCOUNT_UPDATE, false);
        mIsAuth = getIntent().getBooleanExtra(EXTRA_IS_AUTH, false);

        /*
         * If we're being reloaded we override the original account with the one
         * we saved
         */
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_ACCOUNT)) {
            accountUuid = savedInstanceState.getString(EXTRA_ACCOUNT);
            mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
        }

        try {
            ServerSettings settings = Transport.decodeTransportUri(mAccount.getTransportUri());

            updateAuthPlainTextFromSecurityType(settings.connectionSecurity);

            if (savedInstanceState == null) {
                // The first item is selected if settings.authenticationType is null or is not in mAuthTypeAdapter
                mCurrentAuthTypeViewPosition = mAuthTypeAdapter.getPosition(settings.authenticationType);
            } else {
                mCurrentAuthTypeViewPosition = savedInstanceState.getInt(STATE_AUTH_TYPE_POSITION);
            }
            mAuthTypeView.setSelection(mCurrentAuthTypeViewPosition, false);
            updateViewFromAuthType();

            // Select currently configured security type
            if (savedInstanceState == null) {
                mCurrentSecurityTypeViewPosition = settings.connectionSecurity.ordinal();
            } else {

                /*
                 * Restore the spinner state now, before calling
                 * setOnItemSelectedListener(), thus avoiding a call to
                 * onItemSelected(). Then, when the system restores the state
                 * (again) in onRestoreInstanceState(), The system will see that
                 * the new state is the same as the current state (set here), so
                 * once again onItemSelected() will not be called.
                 */
                mCurrentSecurityTypeViewPosition = savedInstanceState.getInt(STATE_SECURITY_TYPE_POSITION);
            }
            mSecurityTypeView.setSelection(mCurrentSecurityTypeViewPosition, false);

            if (settings.username != null && !settings.username.isEmpty()) {
                mUsernameView.setText(settings.username);
                mRequireLoginView.setChecked(true);
                //身份验证方法
                mRequireLoginSettingsView.setVisibility(View.GONE);
            }

            if (mIsAuth) {
                ServerSettings incomingSettings = Store.decodeStoreUri(mAccount.getStoreUri());
                if (incomingSettings.password != null) {
                    mPasswordView.setText(incomingSettings.password);
                }
            } else {
                if (settings.password != null) {
                    mPasswordView.setText(settings.password);
                }
            }

            if (settings.clientCertificateAlias != null) {
                mClientCertificateSpinner.setAlias(settings.clientCertificateAlias);
            }

            if (settings.host != null) {
                mServerView.setText(settings.host);
            }

            if (settings.port != -1) {
                mPortView.setText(Integer.toString(settings.port));
            } else {
                updatePortFromSecurityType();
            }
            mCurrentPortViewSetting = mPortView.getText().toString();
            
            // Add listener for login failure prompt.
            // Modified by LL
            // BEGIN
            mController = MessagingController.getInstance(getApplication());
    		mController.addListener(listener);
    		// END
        } catch (Exception e) {
            /*
             * We should always be able to parse our own settings.
             */
            failure(e);
        }

    }
	private void showPassword() {
		// 密码显示状态
		if (isShowPassword) {
			// 文本正常显示
			mPasswordView.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
			imgShowPassword
					.setImageResource(R.drawable.login_show_password_false);
			isShowPassword = false;
		} else {
			// 文本以密码形式显示
			mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);
			imgShowPassword
					.setImageResource(R.drawable.login_show_password_true);
			isShowPassword = true;
		}
		// 输入框光标一直在输入文本后面
		Editable etable = mPasswordView.getText();
		Selection.setSelection(etable, etable.length());
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mIsBack) {
				Preferences.getPreferences(getApplicationContext()).deleteAccount(
						mAccount);
			}
			finish();
			return true;
		}
	return  super.onKeyDown(keyCode, event);

	}
	private void initTitleBar() {
		ImageView imgBack=(ImageView) findViewById(R.id.back);
		mNextBtn=(TextView) findViewById(R.id.tv_sure);
		mNextBtn.setText(getString(R.string.next_action));
		mNextBtn.setVisibility(View.VISIBLE);
		imgBack.setOnClickListener(this);
		mNextBtn.setOnClickListener(this);
		TextView tvTitle=(TextView) findViewById(R.id.title);
		tvTitle.setText(getString(R.string.account_setup_outgoing_title));
	}
    /**
     * Called at the end of either {@code onCreate()} or
     * {@code onRestoreInstanceState()}, after the views have been initialized,
     * so that the listeners are not triggered during the view initialization.
     * This avoids needless calls to {@code validateFields()} which is called
     * immediately after this is called.
     */
    private void initializeViewListeners() {

        /*
         * Updates the port when the user changes the security type. This allows
         * us to show a reasonable default which the user can change.
         */
        mSecurityTypeView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position,
                    long id) {

                /*
                 * We keep our own record of the spinner state so we
                 * know for sure that onItemSelected() was called
                 * because of user input, not because of spinner
                 * state initialization. This assures that the port
                 * will not be replaced with a default value except
                 * on user input.
                 */
                if (mCurrentSecurityTypeViewPosition != position) {
                    updatePortFromSecurityType();

                    boolean isInsecure = (ConnectionSecurity.NONE == mSecurityTypeView.getSelectedItem());
                    boolean isAuthExternal = (AuthType.EXTERNAL == mAuthTypeView.getSelectedItem());
                    boolean loginNotRequired = !mRequireLoginView.isChecked();

                    /*
                     * If the user selects ConnectionSecurity.NONE, a
                     * warning would normally pop up if the authentication
                     * is AuthType.EXTERNAL (i.e., using client
                     * certificates). But such a warning is irrelevant if
                     * login is not required. So to avoid such a warning
                     * (generated in validateFields()) under those
                     * conditions, we change the (irrelevant) authentication
                     * method to PLAIN.
                     */
                    if (isInsecure && isAuthExternal && loginNotRequired) {
                        OnItemSelectedListener onItemSelectedListener = mAuthTypeView.getOnItemSelectedListener();
                        mAuthTypeView.setOnItemSelectedListener(null);
                        mCurrentAuthTypeViewPosition = mAuthTypeAdapter.getPosition(AuthType.PLAIN);
                        mAuthTypeView.setSelection(mCurrentAuthTypeViewPosition, false);
                        mAuthTypeView.setOnItemSelectedListener(onItemSelectedListener);
                        updateViewFromAuthType();
                    }

                    validateFields();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { /* unused */ }
        });

        mAuthTypeView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position,
                    long id) {
                if (mCurrentAuthTypeViewPosition == position) {
                    return;
                }

                updateViewFromAuthType();
                validateFields();
                AuthType selection = (AuthType) mAuthTypeView.getSelectedItem();

                // Have the user select (or confirm) the client certificate
                if (AuthType.EXTERNAL == selection) {

                    // This may again invoke validateFields()
                    mClientCertificateSpinner.chooseCertificate();
                } else {
                    mPasswordView.requestFocus();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { /* unused */ }
        });

        mRequireLoginView.setOnCheckedChangeListener(this);
        mClientCertificateSpinner.setOnClientCertificateChangedListener(clientCertificateChangedListener);
        mUsernameView.addTextChangedListener(validationTextWatcher);
        mPasswordView.addTextChangedListener(validationTextWatcher);
        mServerView.addTextChangedListener(validationTextWatcher);
        mPortView.addTextChangedListener(validationTextWatcher);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_ACCOUNT, mAccount.getUuid());
        outState.putInt(STATE_SECURITY_TYPE_POSITION, mCurrentSecurityTypeViewPosition);
        outState.putInt(STATE_AUTH_TYPE_POSITION, mCurrentAuthTypeViewPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (mRequireLoginView.isChecked()) {
            mRequireLoginSettingsView.setVisibility(View.VISIBLE);
        } else {
            mRequireLoginSettingsView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        /*
         * We didn't want the listeners active while the state was being restored
         * because they could overwrite the restored port with a default port when
         * the security type was restored.
         */
        initializeViewListeners();
        validateFields();
    }

    /**
     * Shows/hides password field and client certificate spinner
     */
    private void updateViewFromAuthType() {
        AuthType authType = (AuthType) mAuthTypeView.getSelectedItem();
        boolean isAuthTypeExternal = (AuthType.EXTERNAL == authType);

        if (isAuthTypeExternal) {

            // hide password fields, show client certificate fields
            mPasswordView.setVisibility(View.GONE);
            mPasswordLabelView.setVisibility(View.GONE);
            mClientCertificateLabelView.setVisibility(View.VISIBLE);
            mClientCertificateSpinner.setVisibility(View.VISIBLE);
        } else {

            // show password fields, hide client certificate fields
            mPasswordView.setVisibility(View.VISIBLE);
            mPasswordLabelView.setVisibility(View.VISIBLE);
            mClientCertificateLabelView.setVisibility(View.GONE);
            mClientCertificateSpinner.setVisibility(View.GONE);
        }
    }

    /**
     * This is invoked only when the user makes changes to a widget, not when
     * widgets are changed programmatically.  (The logic is simpler when you know
     * that this is the last thing called after an input change.)
     */
    private void validateFields() {
        AuthType authType = (AuthType) mAuthTypeView.getSelectedItem();
        boolean isAuthTypeExternal = (AuthType.EXTERNAL == authType);

        ConnectionSecurity connectionSecurity = (ConnectionSecurity) mSecurityTypeView.getSelectedItem();
        boolean hasConnectionSecurity = (connectionSecurity != ConnectionSecurity.NONE);

        if (isAuthTypeExternal && !hasConnectionSecurity) {

            // Notify user of an invalid combination of AuthType.EXTERNAL & ConnectionSecurity.NONE
            String toastText = getString(R.string.account_setup_outgoing_invalid_setting_combo_notice,
                    getString(R.string.account_setup_incoming_auth_type_label),
                    AuthType.EXTERNAL.toString(),
                    getString(R.string.account_setup_incoming_security_label),
                    ConnectionSecurity.NONE.toString());
            Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();

            // Reset the views back to their previous settings without recursing through here again
            OnItemSelectedListener onItemSelectedListener = mAuthTypeView.getOnItemSelectedListener();
            mAuthTypeView.setOnItemSelectedListener(null);
            mAuthTypeView.setSelection(mCurrentAuthTypeViewPosition, false);
            mAuthTypeView.setOnItemSelectedListener(onItemSelectedListener);
            updateViewFromAuthType();

            onItemSelectedListener = mSecurityTypeView.getOnItemSelectedListener();
            mSecurityTypeView.setOnItemSelectedListener(null);
            mSecurityTypeView.setSelection(mCurrentSecurityTypeViewPosition, false);
            mSecurityTypeView.setOnItemSelectedListener(onItemSelectedListener);
            updateAuthPlainTextFromSecurityType((ConnectionSecurity) mSecurityTypeView.getSelectedItem());

            mPortView.removeTextChangedListener(validationTextWatcher);
            mPortView.setText(mCurrentPortViewSetting);
            mPortView.addTextChangedListener(validationTextWatcher);

            authType = (AuthType) mAuthTypeView.getSelectedItem();
            isAuthTypeExternal = (AuthType.EXTERNAL == authType);

            connectionSecurity = (ConnectionSecurity) mSecurityTypeView.getSelectedItem();
            hasConnectionSecurity = (connectionSecurity != ConnectionSecurity.NONE);
        } else {
            mCurrentAuthTypeViewPosition = mAuthTypeView.getSelectedItemPosition();
            mCurrentSecurityTypeViewPosition = mSecurityTypeView.getSelectedItemPosition();
            mCurrentPortViewSetting = mPortView.getText().toString();
        }

        boolean hasValidCertificateAlias = mClientCertificateSpinner.getAlias() != null;
        boolean hasValidUserName = Utility.requiredFieldValid(mUsernameView);

        boolean hasValidPasswordSettings = hasValidUserName
                && !isAuthTypeExternal
                && Utility.requiredFieldValid(mPasswordView);

        boolean hasValidExternalAuthSettings = hasValidUserName
                && isAuthTypeExternal
                && hasConnectionSecurity
                && hasValidCertificateAlias;

        mNextBtn
                .setEnabled(Utility.domainFieldValid(mServerView)
                        && Utility.requiredFieldValid(mPortView)
                        && (!mRequireLoginView.isChecked()
                                || hasValidPasswordSettings || hasValidExternalAuthSettings));
        Utility.setCompoundDrawablesAlpha(mNextBtn, mNextBtn.isEnabled() ? 255 : 128);
    }

    private void updatePortFromSecurityType() {
        ConnectionSecurity securityType = (ConnectionSecurity) mSecurityTypeView.getSelectedItem();
        updateAuthPlainTextFromSecurityType(securityType);

        // Remove listener so as not to trigger validateFields() which is called
        // elsewhere as a result of user interaction.
        mPortView.removeTextChangedListener(validationTextWatcher);
        mPortView.setText(getDefaultSmtpPort(securityType));
        mPortView.addTextChangedListener(validationTextWatcher);
    }

    private String getDefaultSmtpPort(ConnectionSecurity securityType) {
        String port;
        switch (securityType) {
        case NONE:
        	 port=SMTP;
        	 break;
        case STARTTLS_REQUIRED:
            port = SMTP_PORT;
            break;
        case SSL_TLS_REQUIRED:
            port = SMTP_SSL_PORT;
            break;
        default:
            port = "";
            Log.e(MailChat.LOG_TAG, "Unhandled ConnectionSecurity type encountered");
        }
        return port;
    }

    private void updateAuthPlainTextFromSecurityType(ConnectionSecurity securityType) {
        switch (securityType) {
        case NONE:
            AuthType.PLAIN.useInsecureText(true, mAuthTypeAdapter);
            break;
        default:
            AuthType.PLAIN.useInsecureText(false, mAuthTypeAdapter);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (Intent.ACTION_EDIT.equals(getIntent().getAction())) {
                mAccount.save(Preferences.getPreferences(this));
                finish();
            } else {
                mController.subscribeAccount(mAccount);
            	mController.getGroupInvitation(mAccount, null,true);
            	mController.get35MailVersion(mAccount,true);
            	mController.registerPush(mAccount);
            	//测试
            	mController.bingEmailToUser(mAccount, false, "", "");
            	Preferences.getPreferences(this).setDefaultAccount(mAccount);
				onDone();
            }
        } else {
            mAccount.setTransportUri(oldTransportUri);
        }
    }

    protected void onNext() {
        oldTransportUri = mAccount.getTransportUri();

        ConnectionSecurity securityType = (ConnectionSecurity) mSecurityTypeView.getSelectedItem();
        String uri;

        String clientCertificateAlias = null;
        AuthType authType = null;
        if (mRequireLoginView.isChecked()) {
            username = mUsernameView.getText().toString();

            authType = (AuthType) mAuthTypeView.getSelectedItem();
            if (AuthType.EXTERNAL == authType) {
                clientCertificateAlias = mClientCertificateSpinner.getAlias();
            } else {
                password = mPasswordView.getText().toString();
            }
        }

        String newHost = mServerView.getText().toString();
        int newPort = Integer.parseInt(mPortView.getText().toString());
        String type = SmtpTransport.TRANSPORT_TYPE;
        ServerSettings server = new ServerSettings(type, newHost, newPort, securityType, authType, username, password, clientCertificateAlias);
        uri = Transport.createTransportUri(server);
        mAccount.deleteCertificate(newHost, newPort, CheckDirection.OUTGOING);
        mAccount.setTransportUri(uri);
        
        // Add mAccount into Preferences again in case it was
        // removed after login failure.
        Preferences prefs = Preferences.getPreferences(this);
        Account acc = prefs.getAccount(mAccount.getUuid());
        if (acc == null) {
        	prefs.addAccount(mAccount);
        }
        
        //AccountSetupCheckSettings.actionCheckSettings(this, mAccount, CheckDirection.OUTGOING);
        AccountSetupCheckSettings.actionCheckSettings(this, mAccount, CheckDirection.OUTGOING, mIsAccountUpdate,false,is35Email);
    }

	private void onDone() {
		mAccount.setDescription(mAccount.getEmail());
		mAccount.setNotifyNewMail(true);
		mAccount.setShowOngoing(true);
		mAccount.setAutomaticCheckIntervalMinutes(GlobalConstants.DEFAULT_POLL_INTERVAL);// 自动检查间隔分钟
		mAccount.setDisplayCount(MailChat.DEFAULT_VISIBLE_LIMIT);// 显示数量
		mAccount.setFolderPushMode(Account.FolderMode.FIRST_CLASS);

		mAccount.save(Preferences.getPreferences(this));
		if (mAccount.equals(Preferences.getPreferences(this)
				.getDefaultAccount())) {
			Preferences.getPreferences(this).setDefaultAccount(mAccount);
		}
		MailChat.setServicesEnabled(this);
		AccountSetupNameActivity.actionSetNames(this, mAccount,-1,false);
		finish();
	}
    public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			if (mIsBack) {
				Preferences.getPreferences(getApplicationContext())
						.deleteAccount(mAccount);
			}
			finish();
			break;
		case R.id.tv_sure:
			onNext();
			break;
		case R.id.img_show_password:
			showPassword();
			break;
		}
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mRequireLoginSettingsView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        validateFields();
    }
    private void failure(Exception use) {
        Log.e(MailChat.LOG_TAG, "Failure", use);
        String toastText = getString(R.string.account_setup_bad_uri, use.getMessage());

        Toast toast = Toast.makeText(getApplication(), toastText, Toast.LENGTH_LONG);
        toast.show();
    }

    /*
     * Calls validateFields() which enables or disables the Next button
     * based on the fields' validity.
     */
    TextWatcher validationTextWatcher = new TextWatcher() {
		public void afterTextChanged(Editable s) {
			validateFields();
			if (mPasswordView.length() != 0) {
				imgShowPassword.setVisibility(View.VISIBLE);
			} else {
				imgShowPassword.setVisibility(View.INVISIBLE);
			}
		}

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    };

    OnClientCertificateChangedListener clientCertificateChangedListener = new OnClientCertificateChangedListener() {
        @Override
        public void onClientCertificateChanged(String alias) {
            validateFields();
        }
    };
    
	// Enable login failure prompt.
    // Modified by LL
    // BEGIN
    private MessagingController mController;
    private Handler mHandler = new Handler();
    
    private MessagingListener listener  =new MessagingListener() {
		public void loginDialogShow(final String email,
				final boolean isShowmanualSettingImp, final int errorCode,final boolean is35Mail) {
			if (mAccount.getEmail().equals(email) && !isShowmanualSettingImp) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						new LoginDialog(AccountSetupOutgoing.this, errorCode, email, mPasswordView
										.getText().toString(),
								isShowmanualSettingImp,is35Mail).show();
					}
				});
			}
		}
	};
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mController.removeListener(listener);
	}
	// END
}
