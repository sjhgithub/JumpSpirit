package cn.mailchat.activity;

import java.net.URI;
import java.net.URISyntaxException;

import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.utils.CommonUtils;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.utils.SystemUtil;
import cn.mailchat.utils.Utility;
import cn.mailchat.view.LocusPassWordView;
import cn.mailchat.view.LocusPassWordView.OnCompleteListener;
import cn.mailchat.view.MailDialog;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SetPasswordActivity extends Activity {
	private static final String TAG = "SetPasswordActivity";
	// 标题
	// private TextView text_title;
	// 提示语
	private TextView text_notice;
	// 重新设置手势
	private TextView text_reinstall;
	// 忘记手势密码
	private TextView text_forget_password;
	// 密码验证view
	private LocusPassWordView lpwv;
	// 绘制的密码
	private String password;
	// 步骤 1：绘制解锁图案 2：再次绘制解锁图案  0:验证老的手势密码
	private int step = 1;
	// 是否取消验证
	private static boolean canCancel = false;
	// 是否是验证页面
	private static boolean check = false;
	// 是否是设置密码页面
	private static boolean setting = false;
	private final int MAX_INPUT_TIMES = 3;
	// 可以输入的次数
	private int count = MAX_INPUT_TIMES;

	private long exitTime;

	private static final String COUNT = "count";
	private final int DIALOG_ID_ACCOUNT_PW = 1000;
	private Dialog mFrogetPwdDialog;
	private boolean isClose;
	private boolean isModify;
	/**
	 * 
	 * method name: startActivity function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param context
	 * @param isCheck
	 *            是否要检查口令
	 * 
	 * @param isSetting
	 *            是否要设置口令
	 * 
	 * @param canCance
	 *            是否可以取消，在设置里进去时可以取消，其他界面进入时不可以
	 * 
	 *            void return type
	 * @History memory：
	 * @Date：2014-8-8 下午2:32:30 @Modified by：zhangyq
	 * @Description：
	 */
	public static void startActivity(Context context, boolean isCheck,
			boolean isSetting, boolean canCance,boolean isClose,boolean isModify) {
		Intent intent = new Intent(context, SetPasswordActivity.class);
		intent.putExtra("canCancel", canCance);
		// intent.putExtra("check", isCheck);
		intent.putExtra("setting", isSetting);
		intent.putExtra("close", isClose);
		intent.putExtra("modify", isModify);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.setpassword_activity);

		Intent intent = getIntent();
		canCancel = intent.getBooleanExtra("canCancel", false);
		check = intent.getBooleanExtra("check", false);
		setting = intent.getBooleanExtra("setting", true);
		isClose = intent.getBooleanExtra("close", false);
		isModify= intent.getBooleanExtra("modify", false);
		// 初始化控件
		initView();
		initEvent();
	}

	/**
	 * 初始化控件
	 * 
	 * method name: initView function @Description: TODO Parameters and return
	 * values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-8-8 下午2:33:42 @Modified by：zhangyq
	 * @Description：
	 */
	private void initView() {
		// text_title = (TextView) findViewById(R.id.text_title);
		text_notice = (TextView) findViewById(R.id.text);
		text_reinstall = (TextView) findViewById(R.id.text_reinstall);
		text_forget_password = (TextView) findViewById(R.id.text_forget_password);
		lpwv = (LocusPassWordView) findViewById(R.id.mLocusPassWordView);
		if(setting){
			if(!ifHasGPassword()){
				text_notice.setText(R.string.text_set_password);
			}else{
				text_notice.setText(R.string.text_check_password);
				text_forget_password.setVisibility(View.VISIBLE);
				step=0;
			}
			text_notice.setTextColor(getResources().getColor(R.color.white));
		}else{
			check = true;
			text_notice.setText(R.string.text_check_password);
			text_forget_password.setVisibility(View.VISIBLE);
			text_notice.setTextColor(getResources().getColor(R.color.white));
		}
		// 获取剩余的次数
		count = getCount();
		if (count == 0) {
			count = MAX_INPUT_TIMES;
		} else if (count == -1) {
			forgetGPasswordDialog();
		} else {
			text_notice.setText(R.string.text_check_password);
			text_forget_password.setVisibility(View.VISIBLE);
			text_notice.setTextColor(getResources().getColor(R.color.white));
		}
	}

	/**
	 * 初始化
	 * 
	 * method name: initEvent function @Description: TODO Parameters and return
	 * values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-8-8 下午2:34:05 @Modified by：zhangyq
	 * @Description：
	 */
	private void initEvent() {
		// 轨迹球绘制完成
		lpwv.setOnCompleteListener(new OnCompleteListener() {

			@Override
			public void onComplete(String mPassword) {
				if (setting) {
					// 设置密码
					if(!ifHasGPassword()){
						//未设置过手势密码,直接设置
						setupPassword(mPassword);
					}else{
						if(!isClose){
							//先验证,再设置
							checkAndSetupPassword(mPassword);
						}else{
							closePassword(mPassword);
						}
					}
				} else if (check) {
					// 验证密码
					checkPassword(mPassword);
				}
			}

			@Override
			public void onPasswordTooMin(int minSize) {
				Toast.makeText(
						SetPasswordActivity.this,
						String.format(getString(R.string.text_resume_load),
								minSize), Toast.LENGTH_SHORT).show();
			}
		});

		// 重新绘制
		text_reinstall.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				text_notice.setText(R.string.text_set_password);
				text_notice.setTextColor(getResources().getColor(R.color.white));
				step = 1;
				text_reinstall.setVisibility(View.GONE);
			}

		});

		// 忘记手势密码
		text_forget_password.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				forgetGPasswordDialog();
			}

		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	/**
	 * 设置手势密码
	 * 
	 * method name: setupPassword function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param mPassword
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-8-8 下午2:34:28 @Modified by：zhangyq
	 * @Description：
	 */
	private void setupPassword(String mPassword) {
		if (step == 1) {
			password = mPassword;
			text_notice.setText(R.string.text_draw_again);
			text_notice.setTextColor(getResources().getColor(R.color.white));
			lpwv.clearPassword();
			step = 2;
		} else {
			if (!password.equals(mPassword)) {
				text_notice.setText(R.string.text_draw_inconformity);
				text_notice.setTextColor(getResources().getColor(R.color.red));
				lpwv.clearPassword();
				//text_reinstall.setVisibility(View.VISIBLE);
				step = 1;
			} else {
				// 绘制成功
				lpwv.resetPassWord(password);
				lpwv.clearPassword();
				Toast.makeText(SetPasswordActivity.this,
						R.string.text_setup_complete, Toast.LENGTH_SHORT)
						.show();
				setupComplete();
			}
		}
	}

	/**
	 * 验证手势密码
	 * 
	 * method name: checkPassword function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param mPassword
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-8-8 下午2:34:48 @Modified by：zhangyq
	 * @Description：
	 */
	private void checkPassword(String mPassword) {
		if (lpwv.verifyPassword(mPassword)) {
			saveGestureUnclock(false);
			clear();
			finish();
		} else {
			if (--count > 0) {
				saveCount(count);
				text_notice.setText(String.format(
						getString(R.string.text_wrong_password), count));
				text_notice.setTextColor(getResources().getColor(R.color.red));
				lpwv.clearPassword();
			} else {
				count=-1;
				saveCount(count);
				forgetGPasswordDialog();
			}
		}
	}
	/**
	 * 验证手势密码,并重新设置
	 * 
	 * @param mPassword
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-4-27 下午2:34:48 @Modified by：shengli
	 * @Description：
	 */
	private void checkAndSetupPassword(String mPassword) {
		if(step==0){
			if (lpwv.verifyPassword(mPassword)) {
				saveGestureUnclock(false);
				clear();
				lpwv.clearPassword();
				step = 1;
				text_notice.setText(R.string.text_check_password_successful);
				text_notice.setTextColor(getResources().getColor(R.color.white));
				text_forget_password.setVisibility(View.GONE);
			} else {
				if (--count > 0) {
					saveCount(count);
					text_notice.setText(String.format(
							getString(R.string.text_wrong_password), count));
					text_notice.setTextColor(getResources().getColor(R.color.red));
					lpwv.clearPassword();
				} else {
					count=-1;
					saveCount(count);
					forgetGPasswordDialog();
				}
			}
		}else if (step == 1) {
			password = mPassword;
			lpwv.clearPassword();
			step = 2;
			text_notice.setText(R.string.text_draw_again);
			text_notice.setTextColor(getResources().getColor(R.color.white));
		} else {
			if (!password.equals(mPassword)) {
				text_notice.setText(R.string.text_draw_inconformity);
				text_notice.setTextColor(getResources().getColor(R.color.red));
				lpwv.clearPassword();
				//text_reinstall.setVisibility(View.VISIBLE);
				step = 1;
			} else {
				// 绘制成功
				lpwv.resetPassWord(password);
				lpwv.clearPassword();
				Toast.makeText(SetPasswordActivity.this,
						R.string.text_setup_complete, Toast.LENGTH_SHORT)
						.show();
				setupComplete();
			}
		}
	}
	private void closePassword(String mPassword) {
		if(step==0){
			if (lpwv.verifyPassword(mPassword)) {
				saveGestureUnclock(false);
				clear();
				lpwv.clearPassword();
				Toast.makeText(SetPasswordActivity.this,
						R.string.text_close_success, Toast.LENGTH_SHORT)
						.show();
				saveGestureUnclock(false);
				clearGPassword();
				clear();
				finish();
			} else {
				if (--count > 0) {
					saveCount(count);
					text_notice.setText(String.format(
							getString(R.string.text_wrong_password), count));
					text_notice.setTextColor(getResources().getColor(R.color.red));
					lpwv.clearPassword();
				} else {
					count=-1;
					saveCount(count);
					forgetGPasswordDialog();
				}
			}
		}
	}
	
	/**
	 * 校验邮箱密码以清除口令
	 * 
	 * method name: forgetGPasswordDialog function @Description: TODO Parameters
	 * and return values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-8-8 下午2:35:10 @Modified by：zhangyq
	 * @Description：
	 */
	private void forgetGPasswordDialog() {
		showClearCacheDialog();
	}

	public void showClearCacheDialog() {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.check_mail_password,
				(ViewGroup) findViewById(R.id.check_password_dialog));
		final EditText email_password = (EditText) layout
				.findViewById(R.id.email_password);
		final TextView tv_tips = (TextView) layout
				.findViewById(R.id.tv_email_check_pwd);
		tv_tips.setText(String.format(
				getString(R.string.check_email_password_message), Preferences.getPreferences(MailChat.getInstance()).getDefaultAccount().getEmail()));
		openInputMethod(email_password);
		MailDialog.Builder builder = new MailDialog.Builder(this);
		builder.setTitle(R.string.notice);
		builder.setContentView(layout);
		builder.setPositiveButton(getString(R.string.okay_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						String emailPassword = email_password.getText()
								.toString();
						if (emailPassword == null || emailPassword.equals("")) {
							Toast.makeText(SetPasswordActivity.this,
											getString(R.string.account_safe_setting_empty_password),
											Toast.LENGTH_SHORT).show();
						} else {
							String passWord = null;
							try {
								passWord = Utility.getUserPassword(new URI(Preferences.getPreferences(MailChat.getInstance()).getDefaultAccount().getStoreUri()));
							} catch (URISyntaxException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (emailPassword.equals(passWord)) {
								closeInputMethod(email_password);
								saveGestureUnclock(false);
								clearGPassword();
								clear();
								if(isModify){
									step=1;
									lpwv.clearPassword();
									text_notice.setText(R.string.text_set_password);
									text_forget_password.setVisibility(View.GONE);
									text_notice.setTextColor(getResources().getColor(R.color.white));
								}else{
									finish();
								}
								dialog.dismiss();
							} else {
								email_password.setText("");
								Toast.makeText(SetPasswordActivity.this,
										R.string.amend_password_title, Toast.LENGTH_SHORT)
										.show();
							}
						}
					}
				});
		builder.setNeutralButton(getString(R.string.cancel_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						closeInputMethod(email_password);
						if(setting){
							dialog.dismiss();
							if(count==-1){
								finish();
							}
						}else{
							dialog.dismiss();
						}
					}
				});
		builder.create().show();
	}

	private void closeInputMethod(View view) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	private void openInputMethod(View view) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view, InputMethodManager.RESULT_SHOWN);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
				InputMethodManager.HIDE_IMPLICIT_ONLY);
	}
	/**
	 * 保存还剩余次数
	 * 
	 * method name: saveCount function @Description: TODO Parameters and return
	 * values description：
	 * 
	 * @param count
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-8-8 下午2:35:33 @Modified by：zhangyq
	 * @Description：
	 */
	private void saveCount(int count) {
		SharedPreferences passwrodError = getSharedPreferences(this.getClass()
				.getName(), 0);
		Editor editor = passwrodError.edit();
		editor.putInt(COUNT, count);
		editor.commit();
	}

	/**
	 * 清除输入次数
	 * 
	 * method name: clear function @Description: TODO Parameters and return
	 * values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-8-8 下午2:35:47 @Modified by：zhangyq
	 * @Description：
	 */
	private void clear() {
		SharedPreferences passwrodError = getSharedPreferences(this.getClass()
				.getName(), 0);
		passwrodError.edit().clear().commit();
	}

	/**
	 * 清空手势密码
	 * 
	 * method name: clearGPassword function @Description: TODO Parameters and
	 * return values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-8-8 下午2:36:01 @Modified by：zhangyq
	 * @Description：
	 */
	public void clearGPassword() {
		SharedPreferences settings = getSharedPreferences(
				LocusPassWordView.class.getName(), 0);
		Editor editor = settings.edit();
		editor.putString(LocusPassWordView.PASSWORD, "");
		editor.commit();
	}

	/**
	 * 判断是否有手势密码 method name: ifHasGPassword function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @return field_name boolean return type
	 * @History memory：
	 * @Date：2014-8-8 下午2:36:13 @Modified by：zhangyq
	 * @Description：
	 */
	public static boolean ifHasGPassword() {
		SharedPreferences settings = MailChat.getInstance().getSharedPreferences(
				LocusPassWordView.class.getName(), 0);
		String strGPass = settings.getString(LocusPassWordView.PASSWORD, "");

		if ("".equals(strGPass)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 获取还剩余次数
	 * 
	 * method name: getCount function @Description: TODO Parameters and return
	 * values description：
	 * 
	 * @return field_name int return type
	 * @History memory：
	 * @Date：2014-8-8 下午2:36:26 @Modified by：zhangyq
	 * @Description：
	 */
	private int getCount() {
		SharedPreferences passwrodError = getSharedPreferences(this.getClass()
				.getName(), 0);
		return passwrodError.getInt(COUNT, 0);
	}

	/**
	 * 置完成 method name: setupComplete function @Description: TODO Parameters and
	 * return values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-8-8 下午2:36:42 @Modified by：zhangyq
	 * @Description：
	 */
	private void setupComplete() {
		// new SettingActivity().setupComplete();
		finish();
	}

	//
	// /**
	// * 忘记手势密码要做的
	// *
	// * @Description:
	// * @see:
	// * @since:
	// * @author: xuqq
	// * @date:2013-12-5
	// */
	// private void reLogin() {
	// finish();
	// }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if (canCancel) { //设置相关页面(设置，修改等)
				finish();
			}else {//解锁时
				moveTaskToBack(true);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public static void saveGestureUnclock(boolean isGestureUnclock) {
		SharedPreferences preferences = Preferences.getPreferences(MailChat.getInstance()).getPreferences();
		MailChat.setGestureUnclock(isGestureUnclock);
		Editor editor = preferences.edit();
		MailChat.save(editor);
		editor.commit();
	}
}
