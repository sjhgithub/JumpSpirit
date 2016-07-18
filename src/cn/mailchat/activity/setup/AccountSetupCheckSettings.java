package cn.mailchat.activity.setup;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.BaseFragmentActivity;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.fragment.ConfirmationDialogFragment;
import cn.mailchat.fragment.ConfirmationDialogFragment.ConfirmationDialogFragmentListener;
import cn.mailchat.fragment.MessageListFragment;
import cn.mailchat.mail.AuthenticationFailedException;
import cn.mailchat.mail.CertificateValidationException;
import cn.mailchat.mail.Store;
import cn.mailchat.mail.Transport;
import cn.mailchat.mail.filter.Hex;
import cn.mailchat.mail.store.WebDavStore;

/**
 * Checks the given settings to make sure that they can be used to send and
 * receive mail.
 * 
 * XXX NOTE: The manifest for this app has it ignore config changes, because
 * it doesn't correctly deal with restarting while its thread is running.
 */
public class AccountSetupCheckSettings extends BaseFragmentActivity implements OnClickListener,
        ConfirmationDialogFragmentListener{

    public static final int ACTIVITY_REQUEST_CODE = 1;

    private static final String EXTRA_ACCOUNT = "account";

    private static final String EXTRA_CHECK_DIRECTION ="checkDirection";
    //对话框是否显示设置入口
    private static final String EXTRA_ACCOUNT_IS_SHOW_SETTING ="isShowmanualSettingImp";
    
    private final static String IS_35EMAIL="cn.mailchat.AccountSetupCheckSettings.35email";
    
    public enum CheckDirection {
        INCOMING,
        OUTGOING
    }

    private Handler mHandler = new Handler();

    private ProgressBar mProgressBar;

    private TextView mMessageView;

    private Account mAccount;

    private CheckDirection mDirection;

    private boolean mCanceled;

    private boolean mDestroyed;
    private MessagingController controller;
    private boolean is35Email;
    
	// 记录是否因认证错误需要对已有账号配置进行更新
    private boolean mIsAccountUpdate;
    
    private boolean isShowmanualSettingImp;
    
    /*
    public static void actionCheckSettings(Activity context, Account account,
            CheckDirection direction) {
        Intent i = new Intent(context, AccountSetupCheckSettings.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        i.putExtra(EXTRA_CHECK_DIRECTION, direction);
        context.startActivityForResult(i, ACTIVITY_REQUEST_CODE);
    }
    */
	// 增加isAccountUpdate参数，记录是否因认证错误需要对已有账号配置进行更新
    public static void actionCheckSettings(Activity context, Account account,
            CheckDirection direction, boolean isAccountUpdate, boolean isShowmanualSettingImp,boolean is35Email) {
        Intent i = new Intent(context, AccountSetupCheckSettings.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        i.putExtra(EXTRA_CHECK_DIRECTION, direction);
        i.putExtra(IS_35EMAIL, is35Email);
        i.putExtra(MailChat.EXTRA_ACCOUNT_UPDATE, isAccountUpdate);
        i.putExtra(EXTRA_ACCOUNT_IS_SHOW_SETTING, isShowmanualSettingImp);
        context.startActivityForResult(i, ACTIVITY_REQUEST_CODE);
    }

    public static void actionCheck35Settings(Activity context, Account account,
            CheckDirection direction,boolean is35Email, boolean isShowmanualSettingImp) {
        Intent i = new Intent(context, AccountSetupCheckSettings.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        i.putExtra(EXTRA_CHECK_DIRECTION, direction);
        i.putExtra(IS_35EMAIL, is35Email);
        i.putExtra(EXTRA_ACCOUNT_IS_SHOW_SETTING, isShowmanualSettingImp);
        context.startActivityForResult(i, ACTIVITY_REQUEST_CODE);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    	requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置无标题
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_setup_check_settings);
        initTitleBar();
        mMessageView = (TextView)findViewById(R.id.message);
        mProgressBar = (ProgressBar)findViewById(R.id.progress);
        ((Button)findViewById(R.id.cancel)).setOnClickListener(this);

        setMessage(R.string.account_setup_check_settings_retr_info_msg);
        mProgressBar.setIndeterminate(true);

        String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
        controller=  MessagingController.getInstance(getApplication());
        mDirection = (CheckDirection) getIntent().getSerializableExtra(EXTRA_CHECK_DIRECTION);
        is35Email=getIntent().getBooleanExtra(IS_35EMAIL, false);
        
        // 确认是否因认证错误需要对已有账号配置进行更新
        mIsAccountUpdate = getIntent().getBooleanExtra(MailChat.EXTRA_ACCOUNT_UPDATE, false);
        isShowmanualSettingImp= getIntent().getBooleanExtra(EXTRA_ACCOUNT_IS_SHOW_SETTING, false);
        
        accountSetupCheck();
    }
	private void initTitleBar() {
		ImageView imgBack=(ImageView) findViewById(R.id.back);
		imgBack.setVisibility(View.GONE);
		TextView tvTitle=(TextView) findViewById(R.id.title);
		tvTitle.setText(getString(R.string.account_setup_check_settings_title));
	}
    private void handleCertificateValidationException(CertificateValidationException cve) {
        Log.e(MailChat.LOG_TAG, "Error while testing settings", cve);

        X509Certificate[] chain = cve.getCertChain();
        // Avoid NullPointerException in acceptKeyDialog()
        if (chain != null) {
			acceptKeyDialog(
					R.string.account_setup_failed_dlg_certificate_message_fmt,
					cve);
        } else {
//            showErrorDialog(
//                    R.string.account_setup_failed_dlg_server_message_fmt,
//                    (cve.getMessage() == null ? "" : cve.getMessage()));
			if (mAccount.getEmail().endsWith("@qq.com")) {
				controller.loginDialogShow(mAccount.getEmail(),
						isShowmanualSettingImp, 0,is35Email);
			} else {
				controller.loginDialogShow(mAccount.getEmail(),
						isShowmanualSettingImp, 1,is35Email);
			}
           
           //Preferences.getPreferences(getApplicationContext()).deleteAccount(mAccount);
           // 因认证错误对已有账号配置进行更新的场景，不需要删除账号
           if (!mIsAccountUpdate) {
        	   Preferences.getPreferences(getApplicationContext()).deleteAccount(mAccount);
           }
           
           finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDestroyed = true;
        mCanceled = true;
    }

    private void setMessage(final int resId) {
        mHandler.post(new Runnable() {
            public void run() {
                if (mDestroyed) {
                    return;
                }
                mMessageView.setText(getString(resId));
            }
        });
    }

    private void acceptKeyDialog(final int msgResId, final CertificateValidationException ex) {
        mHandler.post(new Runnable() {
            public void run() {
                if (mDestroyed) {
                    return;
                }
                String exMessage = "Unknown Error";

                if (ex != null) {
                    if (ex.getCause() != null) {
                        if (ex.getCause().getCause() != null) {
                            exMessage = ex.getCause().getCause().getMessage();

                        } else {
                            exMessage = ex.getCause().getMessage();
                        }
                    } else {
                        exMessage = ex.getMessage();
                    }
                }

                mProgressBar.setIndeterminate(false);
                StringBuilder chainInfo = new StringBuilder(100);
                MessageDigest sha1 = null;
                try {
                    sha1 = MessageDigest.getInstance("SHA-1");
                } catch (NoSuchAlgorithmException e) {
                    Log.e(MailChat.LOG_TAG, "Error while initializing MessageDigest", e);
                }

                final X509Certificate[] chain = ex.getCertChain();
                // We already know chain != null (tested before calling this method)
                for (int i = 0; i < chain.length; i++) {
                    // display certificate chain information
                    //TODO: localize this strings
                    chainInfo.append("Certificate chain[").append(i).append("]:\n");
                    chainInfo.append("Subject: ").append(chain[i].getSubjectDN().toString()).append("\n");

                    // display SubjectAltNames too
                    // (the user may be mislead into mistrusting a certificate
                    //  by a subjectDN not matching the server even though a
                    //  SubjectAltName matches)
                    try {
                        final Collection < List<? >> subjectAlternativeNames = chain[i].getSubjectAlternativeNames();
                        if (subjectAlternativeNames != null) {
                            // The list of SubjectAltNames may be very long
                            //TODO: localize this string
                            StringBuilder altNamesText = new StringBuilder();
                            altNamesText.append("Subject has ").append(subjectAlternativeNames.size()).append(" alternative names\n");

                            // we need these for matching
                            String storeURIHost = (Uri.parse(mAccount.getStoreUri())).getHost();
                            String transportURIHost = (Uri.parse(mAccount.getTransportUri())).getHost();

                            for (List<?> subjectAlternativeName : subjectAlternativeNames) {
                                Integer type = (Integer)subjectAlternativeName.get(0);
                                Object value = subjectAlternativeName.get(1);
                                String name = "";
                                switch (type.intValue()) {
                                case 0:
                                    Log.w(MailChat.LOG_TAG, "SubjectAltName of type OtherName not supported.");
                                    continue;
                                case 1: // RFC822Name
                                    name = (String)value;
                                    break;
                                case 2:  // DNSName
                                    name = (String)value;
                                    break;
                                case 3:
                                    Log.w(MailChat.LOG_TAG, "unsupported SubjectAltName of type x400Address");
                                    continue;
                                case 4:
                                    Log.w(MailChat.LOG_TAG, "unsupported SubjectAltName of type directoryName");
                                    continue;
                                case 5:
                                    Log.w(MailChat.LOG_TAG, "unsupported SubjectAltName of type ediPartyName");
                                    continue;
                                case 6:  // Uri
                                    name = (String)value;
                                    break;
                                case 7: // ip-address
                                    name = (String)value;
                                    break;
                                default:
                                    Log.w(MailChat.LOG_TAG, "unsupported SubjectAltName of unknown type");
                                    continue;
                                }

                                // if some of the SubjectAltNames match the store or transport -host,
                                // display them
                                if (name.equalsIgnoreCase(storeURIHost) || name.equalsIgnoreCase(transportURIHost)) {
                                    //TODO: localize this string
                                    altNamesText.append("Subject(alt): ").append(name).append(",...\n");
                                } else if (name.startsWith("*.") && (
                                            storeURIHost.endsWith(name.substring(2)) ||
                                            transportURIHost.endsWith(name.substring(2)))) {
                                    //TODO: localize this string
                                    altNamesText.append("Subject(alt): ").append(name).append(",...\n");
                                }
                            }
                            chainInfo.append(altNamesText);
                        }
                    } catch (Exception e1) {
                        // don't fail just because of subjectAltNames
                        Log.w(MailChat.LOG_TAG, "cannot display SubjectAltNames in dialog", e1);
                    }

                    chainInfo.append("Issuer: ").append(chain[i].getIssuerDN().toString()).append("\n");
                    if (sha1 != null) {
                        sha1.reset();
                        try {
                            char[] sha1sum = Hex.encodeHex(sha1.digest(chain[i].getEncoded()));
                            chainInfo.append("Fingerprint (SHA-1): ").append(new String(sha1sum)).append("\n");
                        } catch (CertificateEncodingException e) {
                            Log.e(MailChat.LOG_TAG, "Error while encoding certificate", e);
                        }
                    }
                }
				// TODO: refactor with DialogFragment.
				// This is difficult because we need to pass through chain[0]
				// for onClick()
				new AlertDialog.Builder(AccountSetupCheckSettings.this)
						.setTitle(
								getString(R.string.account_setup_failed_dlg_invalid_certificate_title))
						// .setMessage(getString(R.string.account_setup_failed_dlg_invalid_certificate)
						.setMessage(
								getString(msgResId, exMessage) + " "
										+ chainInfo.toString())
						.setCancelable(true)
						.setPositiveButton(
								getString(R.string.account_setup_failed_dlg_invalid_certificate_accept),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										acceptCertificate(chain[0]);
									}
								})
						.setNegativeButton(
								getString(R.string.account_setup_failed_dlg_invalid_certificate_reject),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										finish();
									}
								}).show();
            }
        });
    }

    /**
     * Permanently accepts a certificate for the INCOMING or OUTGOING direction
     * by adding it to the local key store.
     * 
     * @param certificate
     */
    private void acceptCertificate(X509Certificate certificate) {
        try {
            mAccount.addCertificate(mDirection, certificate);
        } catch (CertificateException e) {
//            showErrorDialog(
//                    R.string.account_setup_failed_dlg_certificate_message_fmt,
//                    e.getMessage() == null ? "" : e.getMessage());
        	Toast.makeText(this, R.string.account_setup_failed_dlg_certificate_message_fmt, Toast.LENGTH_SHORT).show();
        	
        	//Preferences.getPreferences(getApplicationContext()).deleteAccount(mAccount);
        	// 因认证错误对已有账号配置进行更新的场景，不需要删除账号
            if (!mIsAccountUpdate) {
         	   Preferences.getPreferences(getApplicationContext()).deleteAccount(mAccount);
            }
        	
            finish();
        }
        
        /*
        AccountSetupCheckSettings.actionCheckSettings(AccountSetupCheckSettings.this, mAccount,
                mDirection);
        */
        // Modified by LL
        AccountSetupCheckSettings.actionCheckSettings(AccountSetupCheckSettings.this, mAccount,
                mDirection, mIsAccountUpdate,false,is35Email);
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        setResult(resCode);
        finish();
    }

    private void onCancel() {
        mCanceled = true;
        setMessage(R.string.account_setup_check_settings_canceling_msg);
    }

    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.cancel:
            onCancel();
            break;
        }
    }

    private void showErrorDialog(final int msgResId, final Object... args) {
        mHandler.post(new Runnable() {
            public void run() {
                showDialogFragment(R.id.dialog_account_setup_error, getString(msgResId, args));
            }
        });
    }

    private void showDialogFragment(int dialogId, String customMessage) {
        if (mDestroyed) {
            return;
        }
        mProgressBar.setIndeterminate(false);

        DialogFragment fragment;
        switch (dialogId) {
            case R.id.dialog_account_setup_error: {
                fragment = ConfirmationDialogFragment.newInstance(dialogId,
                        getString(R.string.account_setup_failed_dlg_title),
                        customMessage,
                        getString(R.string.account_setup_failed_dlg_edit_details_action),
                        getString(R.string.account_setup_failed_dlg_continue_action)
                );
                break;
            }
            default: {
                throw new RuntimeException("Called showDialog(int) with unknown dialog id.");
            }
        }

        FragmentTransaction ta = this.getSupportFragmentManager().beginTransaction();
        ta.add(fragment, getDialogTag(dialogId));
        ta.commitAllowingStateLoss();

        // TODO: commitAllowingStateLoss() is used to prevent https://code.google.com/p/android/issues/detail?id=23761
        // but is a bad...
        //fragment.show(ta, getDialogTag(dialogId));
    }

    private String getDialogTag(int dialogId) {
        return String.format(Locale.US, "dialog-%d", dialogId);
    }

    @Override
    public void doPositiveClick(int dialogId) {
        switch (dialogId) {
            case R.id.dialog_account_setup_error: {
                finish();
                break;
            }
        }
    }

    @Override
    public void doNegativeClick(int dialogId) {
        switch (dialogId) {
            case R.id.dialog_account_setup_error: {
                //mCanceled = false;
                //setResult(RESULT_OK);
                finish();
                break;
            }
        }
    }

    @Override
    public void dialogCancelled(int dialogId) {
        // nothing to do here...
    }
    
    private void accountSetupCheck(){
        new Thread() {
            @Override
            public void run() {
                Store store = null;
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                try {
                    if (mDestroyed) {
                        Preferences.getPreferences(getApplicationContext()).deleteAccount(mAccount);
                        finish();
                        return;
                    }
                    if (mCanceled) {
                        Preferences.getPreferences(getApplicationContext()).deleteAccount(mAccount);
                        finish();
                        return;
                    }

                    final MessagingController ctrl = MessagingController.getInstance(getApplication());
                    ctrl.clearCertificateErrorNotifications(AccountSetupCheckSettings.this,
                            mAccount, mDirection);

                    if (mDirection == CheckDirection.INCOMING) {
//这里会保存账号。
//                        if (!mIsAccountUpdate) {
//    						MessagingController.getInstance(getApplication())
//    								.get35MailVersion(mAccount, false);
//                        }
                        store = mAccount.getRemoteStore();

                        if (store instanceof WebDavStore) {
                            setMessage(R.string.account_setup_check_settings_authenticate);
                        } else {
                            setMessage(R.string.account_setup_check_settings_check_incoming_msg);
                        }
                        store.checkSettings();

                        if (store instanceof WebDavStore) {
                            setMessage(R.string.account_setup_check_settings_fetch);
                        }
                        MessagingController.getInstance(getApplication()).listFoldersSynchronous(mAccount, true, null);
                        
                        //MessagingController.getInstance(getApplication()).synchronizeMailbox(mAccount, mAccount.getInboxFolderName(), null, null);
                        // Trigger message list refreshing
                        MailChat.forceRefresh = true;
                    }
                    if (mDestroyed) {
                        Preferences.getPreferences(getApplicationContext()).deleteAccount(mAccount);
                        finish();
                        return;
                    }
                    if (mCanceled) {
                        Preferences.getPreferences(getApplicationContext()).deleteAccount(mAccount);
                        finish();
                        return;
                    }
                    if (mDirection == CheckDirection.OUTGOING) {
                        if (!(mAccount.getRemoteStore() instanceof WebDavStore)) {
                            setMessage(R.string.account_setup_check_settings_check_outgoing_msg);
                        }
                        Transport transport = Transport.getInstance(mAccount);
                        transport.close();
                        transport.open();
                        transport.close();
                    }
                    if (mDestroyed) {
                        Preferences.getPreferences(getApplicationContext()).deleteAccount(mAccount);
                        finish();
                        return;
                    }
                    if (mCanceled) {
                        Preferences.getPreferences(getApplicationContext()).deleteAccount(mAccount);
                        finish();
                        return;
                    }
                    //标记登陆成功（收件服务器或都检测通过时）
                    mAccount.setLoginSuccessedAccount(true);
                    mAccount.save(Preferences.getPreferences(getApplicationContext()));

                    setResult(RESULT_OK);
                    finish();
                } catch (final AuthenticationFailedException afe) {
                	MailChat.lastAccountSetupCheckException = afe.getMessage();
                	
                    Log.e(MailChat.LOG_TAG, "Error while testing settings", afe);   
//                    showErrorDialog(
//                            R.string.account_setup_failed_dlg_auth_message_fmt,
//                            (afe.getMessage() == null ? "" : afe.getMessage()));  
					if (mAccount.getEmail().endsWith("@qq.com")) {
						controller.loginDialogShow(mAccount.getEmail(),
								isShowmanualSettingImp, 0,is35Email);
					} else {
						controller.loginDialogShow(mAccount.getEmail(),
								isShowmanualSettingImp, 1,is35Email);
					}
                    
                    //Preferences.getPreferences(getApplicationContext()).deleteAccount(mAccount);
                    // 因认证错误对已有账号配置进行更新的场景，不需要删除账号
                    // Modified by LL
                    // BEGIN
                    if (!mIsAccountUpdate) {
                 	   Preferences.getPreferences(getApplicationContext()).deleteAccount(mAccount);
                    }
                    // END
                    
                    finish();
                } catch (final CertificateValidationException cve) {
                	MailChat.lastAccountSetupCheckException = cve.getMessage();
                	
                	// 所有邮箱SSL都自动接收
                    // Modified by shengli
                	//if(is35Email){
                	     try {
                	    	 X509Certificate[] chain = cve.getCertChain();
                	    	  if (chain != null) {
                	    		  mAccount.addCertificate(mDirection, chain[0]);
                	    		  accountSetupCheck();
                	    	  }else{
								// AccountSetupAccountType.actionSelectAccountType(AccountSetupCheckSettings.this,
								// mAccount, false);
								onImap(false);
								finish();
                	    	  }
                	     } catch (CertificateException e) {
                	    	 MailChat.lastAccountSetupCheckException = e.getMessage();
                	    	 
                	         //Preferences.getPreferences(getApplicationContext()).deleteAccount(mAccount);
                	    	 // 因认证错误对已有账号配置进行更新的场景，不需要删除账号
                	    	 // Modified by LL
                	         // BEGIN
                	         if (!mIsAccountUpdate) {
                	        	 Preferences.getPreferences(getApplicationContext()).deleteAccount(mAccount);
                	         }
                	         // END
                	         
                	         finish();
                	     }
//                	}else{
//                		handleCertificateValidationException(cve);
//                	}
                } catch (final Throwable t) {
                	MailChat.lastAccountSetupCheckException = t.getMessage();
                	
                    Log.e(MailChat.LOG_TAG, "Error while testing settings", t);
//                    showErrorDialog(
//                        R.string.account_setup_failed_dlg_server_message_fmt,
//                        (t.getMessage() == null ? "" : t.getMessage()));  
                    if(is35Email){
						// AccountSetupAccountType.actionSelectAccountType(AccountSetupCheckSettings.this,
						// mAccount, false);
						onImap(false);
                    }else{
						if (mAccount.getEmail().endsWith("@qq.com")) {
							controller.loginDialogShow(mAccount.getEmail(),
									isShowmanualSettingImp, 0,is35Email);
						} else {
							controller.loginDialogShow(mAccount.getEmail(),
									isShowmanualSettingImp, 1,is35Email);
						}
                       
                       //Preferences.getPreferences(getApplicationContext()).deleteAccount(mAccount);
                       // 因认证错误对已有账号配置进行更新的场景，不需要删除账号
                       // Modified by LL
                       // BEGIN
                       if (!mIsAccountUpdate) {
                    	   Preferences.getPreferences(getApplicationContext()).deleteAccount(mAccount);
                       }
                       // END
                    }
                    finish();
                }
            }

        }
        .start();
    }

	private void onImap(boolean mMakeDefault) {
		try {
			URI uri = new URI(mAccount.getStoreUri());
			uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
					uri.getPort(), null, null, null);
			mAccount.setStoreUri(uri.toString());

			uri = new URI(mAccount.getTransportUri());
			uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
					uri.getPort(), null, null, null);
			mAccount.setTransportUri(uri.toString());

			AccountSetupIncoming.actionIncomingSettings(this, mAccount,
					mMakeDefault, true,is35Email);

		} catch (Exception use) {
			Log.e(MailChat.LOG_TAG, "Failure", use);
			MailChat.toast(getString(R.string.account_setup_bad_uri, use.getMessage()));
		}

	}
}
