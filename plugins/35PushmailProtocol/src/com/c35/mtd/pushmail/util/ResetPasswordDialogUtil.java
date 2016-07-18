package com.c35.mtd.pushmail.util;

import java.net.URI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.GlobalVariable;
import com.c35.mtd.pushmail.R;
import com.c35.mtd.pushmail.logic.C35AccountManager;
import com.c35.mtd.pushmail.store.C35Store;
import com.c35.mtd.pushmail.store.Store;
/**
 * 密码变更提示,重新登录对话框工具类
 * @author sunzhongquan
 *
 */
public class ResetPasswordDialogUtil {
	/**
	 * sunzhongquan密码变更提示
	 * 
	 * @return
	 */
	public static void passWordChanged(final Context mContext,String msg,final String TAG) {
		String error = mContext.getString(R.string.account_setup_failed_dlg_auth_message);
		if (EmailApplication.getCurrentAccount() != null) {
			if (error.equals(msg)) {
			    /*
				if (!GlobalVariable.isDialog) {
					if (creatErrorPasswordDialog(mContext,TAG) != null && mContext != null) {
						GlobalVariable.setDialog(true);
						((Activity) mContext).runOnUiThread(new Runnable() {

							@Override
							public void run() {
								creatErrorPasswordDialog(mContext,TAG).create().show();
							}
						});
					}
				}
				*/
			    // Modified by LL
			    // BEGIN
			    if (mContext != null) {
			        Log.e(TAG, mContext.getString(R.string.connect_cont_login_message));
			    }
			    // END
			}
		}
	}

	/**
	 * sunzhongquan 密码变更提示重新登录的方法
	 * 
	 * @return 密码变更提示对话框
	 */
	// Modified by LL
	/*
	public static MailDialog.Builder creatErrorPasswordDialog(final Context mContext,final String TAG) {
		if (mContext != null) {
			final MailDialog.Builder alert = new MailDialog.Builder(mContext);
			alert.setTitle(mContext.getString(R.string.connect_cont_login_title));
			alert.setMessage(mContext.getString(R.string.connect_cont_login_message));
			alert.setPositiveButton(mContext.getString(R.string.connect_cont_checkpass), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					creatResetPasswordDialog(mContext,TAG).show();
					dialog.cancel();
				}
			});
			alert.setNegativeButton(mContext.getString(R.string.cancel_action), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					GlobalVariable.setDialog(false);
					dialog.cancel();
				}
			});
			return alert;
		}
		return null;
	}
	*/

	/**
	 * sunzhongquan 重新输入密码的方法
	 * 
	 * @return 重新输入密码的对话框
	 */
	public static AlertDialog.Builder creatResetPasswordDialog(final Context mContext,final String TAG) {
		if (mContext != null) {
			final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
			final EditText input = new EditText(mContext);
			input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
			input.setTransformationMethod(PasswordTransformationMethod.getInstance());
			input.setMaxHeight(1);
			input.setMaxWidth(40);
			alert.setTitle(mContext.getString(R.string.amend_password_title));
			alert.setMessage(mContext.getString(R.string.account_id) + " : " + EmailApplication.getCurrentAccount().getmEmailShow());
			alert.setView(input);
			alert.setPositiveButton(mContext.getString(R.string.okay_action), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					String value = input.getText().toString().trim();
					amendPassword(mContext,value,TAG);
					GlobalVariable.setDialog(false);
				}
			});
			alert.setNegativeButton(mContext.getString(R.string.cancel_action), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					GlobalVariable.setDialog(false);
					dialog.cancel();
				}
			});
			return alert;
		}
		return null;
	}
	
	/**
	 * sunzhongquan 重新验证密码,注册IPPush的方法
	 * 
	 * @param newPassword
	 */
	public static void amendPassword(Context mContext,String password,String TAG) {
		String emailAccount = EmailApplication.getCurrentAccount().getEmail();
		if (emailAccount.endsWith(MailUtil.DOMAIN_35CN)) {
			emailAccount = emailAccount.replace(MailUtil.DOMAIN_35CN, MailUtil.DOMAIN_CHINACHANNEL);
		}
		if (emailAccount != null && password != null) {
			String[] emailParts = emailAccount.split("@");
			if (emailParts != null && emailParts.length > 0) {
				@SuppressWarnings("unused")
				String user = emailParts[0]; // 用户名
				URI incomingUri = null;
				try {
					C35Store store = (C35Store) Store.getInstance(EmailApplication.getCurrentAccount().getStoreUri());
					String[] host = store.getHostInfo();
					incomingUri = new URI(MailUtil.STORE_SCHEME_C35PROXY, emailAccount + ":" + password, host[0], Integer.parseInt(host[1]), null, null, null);
					EmailApplication.getCurrentAccount().setStoreUri(incomingUri.toString());
					EmailApplication.getCurrentAccount().save(C35AccountManager.getInstance(), false);
					// Debug.i(TAG, "incomingUri" + incomingUri.toString());
					Log.i(TAG, mContext.getString(R.string.amend_password_success));
					store.closeSocket();

					String content = " , " + emailAccount + " , RegisterIPPush , Send  Code: MessageList.amendPassword";
					C35AppServiceUtil.writeSubscribeInformationToSdcard(content);
					C35AppServiceUtil.registerIPPush(emailAccount, password);
				} catch (Exception use) {
					Debug.e(TAG, "failfast_AA", use);
				}
			}
		}
	}
}
