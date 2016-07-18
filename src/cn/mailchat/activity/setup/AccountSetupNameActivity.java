package cn.mailchat.activity.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.text.method.TextKeyListener.Capitalize;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.Main4TabActivity;
import cn.mailchat.activity.base.BaseSetUpUserHeadImgActivity;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.search.LocalSearch;
import cn.mailchat.utils.CommonUtils;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.utils.NetUtil;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.utils.SystemUtil;
import cn.mailchat.view.RoundImageView;
import cn.mailchat.view.SetUserInfoDialog;
import cn.mailchat.view.SetUserInfoDialog.SetUserInfoDialogListener;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

public class AccountSetupNameActivity extends BaseSetUpUserHeadImgActivity
		implements OnClickListener, SetUserInfoDialogListener {
	private String TAG = "AccountSetupNameActivity";
	public static final int EXTRA_START_ACTIVITY_TAG = 1;
	public static final String EXTRA_ACCOUNT = "account";
	public static final int RESULT_CHANGE_USER_NAME = 100;
	public static final String EXTRA_IS_CHANGE_USER_INFO = "is_change_user_info";
	private RoundImageView mUserHeadImg;
	private RelativeLayout layoutChangeHead;
	private EditText mUserName, mUserCompany, mUserDepartment, mUserPosition,mInvitationCode;
	private Account mAccount;
	private Button mStartButton, mSkipButton;
	private MessagingController mController;
	private boolean isDeleteAccount = true;
	private ImageView imgCleanEdit, imgCleanCompanyEdit,
			imgCleanDepartmentEdit, imgCleanPositionEdit,imgCleanimgInvitationCode;
	private String userHeadPath;
	private String accountUuid;
	private List<Integer> dialogItems;
	private boolean isChangeUserInfo;
	private MessagingListener listener = new MessagingListener() {
		public void loadUserInfoStart(Account account) {
			if (account.getEmail().equals(mAccount.getEmail())) {
				mHandler.sendEmptyMessage(HANDLER_LOAING_MSG_START);
			}
		}
		/**
		 * 
		 * method name: loadUserInfoSuccess function @Description: TODO
		 * Parameters and return values description：
		 * 
		 * @param account
		 * @param newContactAttribute
		 *            field_name void return type
		 * @History memory：
		 * @Date：2015-4-17 下午5:30:17 @Modified by：zhangjx
		 * @Description：获取用户信息成功
		 */
		public void loadUserInfoSuccess(Account account,
				final ContactAttribute newContactAttribute) {
			if (account.getEmail().equals(mAccount.getEmail())) {
				mHandler.sendEmptyMessage(HANDLER_LOAING_MSG_SUCCESS);
				runOnUiThread(new Runnable() {
					public void run() {
						setUserInfo(newContactAttribute);
					}
				});
			}
		}

		/**
		 * 
		 * method name: loadUserInfoFailed function @Description: TODO
		 * Parameters and return values description：
		 * 
		 * @param account
		 * @param err
		 *            field_name void return type
		 * @History memory：
		 * @Date：2015-4-17 下午5:31:22 @Modified by：zhangjx
		 * @Description：获取用户信息失败
		 */
		public void loadUserInfoFailed(Account account) {
			if (account.getEmail().equals(mAccount.getEmail())) {
				mHandler.sendEmptyMessage(HANDLER_LOAING_MSG_FAILED);
				runOnUiThread(new Runnable() {
					public void run() {
						setUserName();
						setUserHeadImgWithFirstChat(getUserName());
					}
				});
			}
		}

		/**
		 * 
		 * method name: uploadUserInfoStart function @Description: TODO
		 * Parameters and return values description：
		 * 
		 * @param account
		 *            field_name void return type
		 * @History memory：
		 * @Date：2015-4-17 下午2:57:19 @Modified by：zhangjx
		 * @Description：上传用户头像开始
		 */
		public void uploadUserInfoStart(Account account) {
			if (account.getEmail().equals(mAccount.getEmail())) {
				mHandler.sendEmptyMessage(HANDLER_UPLOAD_MSG_START);
			}
		}

		/**
		 * 
		 * method name: uploadUserInfoSuccess function @Description: TODO
		 * Parameters and return values description：
		 * 
		 * @param account
		 *            field_name void return type
		 * @History memory：
		 * @Date：2015-4-17 下午2:57:46 @Modified by：zhangjx
		 * @Description：上传头像成功
		 */
		public void uploadUserInfoSuccess(Account account,
				final ContactAttribute newContactAttribute) {
			if (account.getEmail().equals(mAccount.getEmail())) {
				if (newContactAttribute != null) {
					mHandler.sendEmptyMessage(HANDLER_UPLOAD_MSG_SUCCESS);
					onNext(newContactAttribute.getNickName(),
							newContactAttribute.getImgHeadPath());
				}
			}
		}

		/**
		 * 
		 * method name: uploadUserInfoFailed function @Description: TODO
		 * Parameters and return values description：
		 * 
		 * @param account
		 *            field_name void return type
		 * @History memory：
		 * @Date：2015-4-17 下午2:57:53 @Modified by：zhangjx
		 * @Description：上传头像失败
		 */
		public void uploadUserInfoFailed(Account account) {
			if (account.getEmail().equals(mAccount.getEmail())) {
				mHandler.sendEmptyMessage(HANDLER_UPLOAD_MSG_FAILED);
				runOnUiThread(new Runnable() {
					public void run() {
						mSkipButton.setVisibility(View.VISIBLE);
					}
				});
			}
		}
	};
	private LinearLayout mLayoutButtons;

	public static void actionSetNames(Activity activity, Account account,
			int position, boolean isChangeUserInfo) {
		Intent i = new Intent(activity, AccountSetupNameActivity.class);
		i.putExtra(EXTRA_ACCOUNT, account.getUuid());
		i.putExtra(EXTRA_IS_CHANGE_USER_INFO, isChangeUserInfo);
		if (isChangeUserInfo) {
			activity.startActivityForResult(i, RESULT_CHANGE_USER_NAME);
		} else {
			activity.startActivity(i);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		MobclickAgent.onEvent(getApplicationContext(),"into_set_user_info_act");
		setContentView(R.layout.activity_setup_img_names_new);
		initImageLoader();
		decodeExtras();
//		hideSoftInput();
		initView();
		showSoftInput();
		initTitleBar();
		initData();
		initEvent();
	}

	protected void hideSoftInput() {
		if (isChangeUserInfo) {
			WindowManager.LayoutParams params = getWindow().getAttributes();
			// 隐藏软键盘
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
		}
	}
	private void showSoftInput() {
		new Timer().schedule(new TimerTask(){
			public void run(){
				InputMethodManager inputManager =
				(InputMethodManager) mUserName.getContext().getSystemService(
						Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(mUserName, 0);
			}
		},
		2*1000);
	}
	private void decodeExtras() {
		Intent intent = getIntent();
		accountUuid = intent.getStringExtra(EXTRA_ACCOUNT);
		isChangeUserInfo = intent.getBooleanExtra(EXTRA_IS_CHANGE_USER_INFO, false);
		mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
	}

	private void initEvent() {
		layoutChangeHead.setOnClickListener(this);
		mStartButton.setOnClickListener(this);
		mSkipButton.setOnClickListener(this);
		imgCleanEdit.setOnClickListener(this);
		imgCleanCompanyEdit.setOnClickListener(this);
		imgCleanDepartmentEdit.setOnClickListener(this);
		imgCleanPositionEdit.setOnClickListener(this);
		imgCleanimgInvitationCode.setOnClickListener(this);

		mUserName.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					if (TextUtils.isEmpty(mUserName.getText().toString().trim())) {
						mUserName.setError(getString(R.string.must_set_contact_name));
					}
				}
			}
		});
		TextWatcher validationTextWatcher = new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (isCreateDefaultHeadImg) {
					changeUserHead();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				shoundShowCleanBtn(mUserName.getText().toString().trim(),
						imgCleanEdit);
			}
		};
		TextWatcher textCompanyChangeWatcher = new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				shoundShowCleanBtn(mUserCompany.getText().toString().trim(),
						imgCleanCompanyEdit);
			}
		};
		TextWatcher textDepartmentChangeWatcher = new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				shoundShowCleanBtn(mUserDepartment.getText().toString().trim(),
						imgCleanDepartmentEdit);
			}
		};
		TextWatcher textPositionChangeWatcher = new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				shoundShowCleanBtn(mUserPosition.getText().toString().trim(),
						imgCleanPositionEdit);
			}
		};
		TextWatcher textInvitationCodeChangeWatcher = new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				shoundShowCleanBtn(mInvitationCode.getText().toString().trim(),
						imgCleanimgInvitationCode);
			}
		};
		mUserName.addTextChangedListener(validationTextWatcher);
		mUserCompany.addTextChangedListener(textCompanyChangeWatcher);
		mUserDepartment.addTextChangedListener(textDepartmentChangeWatcher);
		mUserPosition.addTextChangedListener(textPositionChangeWatcher);
		mInvitationCode.addTextChangedListener(textInvitationCodeChangeWatcher);
		mUserName.setKeyListener(TextKeyListener.getInstance(false,
				Capitalize.WORDS));
	}

	private void shoundShowCleanBtn(String text, ImageView view) {
		if (!TextUtils.isEmpty(text)) {
			view.setVisibility(View.VISIBLE);
		} else {
			view.setVisibility(View.INVISIBLE);
		}
	}

	private void changeUserHead() {
		String newUserName = mUserName.getText().toString().trim();
		if (newUserName.length() > 0) {
			setUserHeadImgWithFirstChat(newUserName);
		} else {
//			Toast.makeText(AccountSetupNameActivity.this,
//					getString(R.string.must_set_name), Toast.LENGTH_SHORT)
//					.show();
			mUserName.setError(getString(R.string.must_set_contact_name));
		}
	}

	private void initData() {
		getScWidth();
		setUserHeadImgWithFirstChat(getUserName());
		setUserName();
		setInvationCodeValue();
		mController = MessagingController.getInstance(getApplication());
		mController.addListener(listener);
		dialogItems = new ArrayList<Integer>();
		mController.loadRemoteUserInfo(AccountSetupNameActivity.this,mAccount, listener);
	}

	private void setInvationCodeValue() {
		// 邀请码如果有值，说明该设备以及被邀请过了，输入框不允许被输入
		if (!TextUtils.isEmpty(mAccount.getAlreadyInvationCode())) {
			mInvitationCode.setFocusable(false);
			mInvitationCode.setHint(mAccount.getAlreadyInvationCode());
		}else if (!TextUtils.isEmpty(MailChat.getPhoneAlreadyInvationed())) {
			//保存第一次获取到的邀请码，避免新设备先登录旧账号（已被邀请）后再登录新账号（未被邀请），可以输入邀请码的问题
				mInvitationCode.setFocusable(false);
				mInvitationCode.setHint(MailChat.getPhoneAlreadyInvationed());
		}
	}
	private void setUserInfo(ContactAttribute newContactAttribute) {
		if (newContactAttribute != null) {
			setInvationCodeValue();
			userHeadPath = newContactAttribute.getImgHeadPath();
			String userNickName = newContactAttribute.getNickName();
			String userCompany = newContactAttribute.getCompany();
			String userDepartment = newContactAttribute.getDepartment();
			String userPosition = newContactAttribute.getPosition();
			if (!TextUtils.isEmpty(userNickName)) {
				mUserName.setText(userNickName);
				mAccount.setName(userNickName);
			} else {
				setUserName();
			}
			mUserCompany.setText(userCompany);
			mUserDepartment.setText(userDepartment);
			mUserPosition.setText(userPosition);
			if (!TextUtils.isEmpty(userHeadPath)) {
				setAccountValue(userNickName, userHeadPath);
				isCreateDefaultHeadImg = false;
				if (!userHeadPath.startsWith("http")) {
					userHeadPath = GlobalConstants.HOST_IMG + userHeadPath;
				}
				ImageLoader.getInstance().displayImage(
						userHeadPath + GlobalConstants.USER_SMALL_HEAD_END,
						mUserHeadImg, options);
			} else {
				setUserHeadImgWithFirstChat(TextUtils.isEmpty(userNickName) ? getUserName()
						: userNickName);
			}
		}
	}

	private String getUserName() {
		String userName = null;
		if (TextUtils.isEmpty(mAccount.getName())) {
			userName = StringUtil.getPrdfixStr(mAccount.getEmail());
		} else {
			userName = mAccount.getName();
		}
		return userName;
	}

	private void setUserName() {
		if (!StringUtil.isEmpty(mAccount.getName())) {
			mUserName.setText(mAccount.getName());
		} else {
			mUserName.setText(getUserName());
		}

	}

	private void initView() {
		mUserHeadImg = (RoundImageView) findViewById(R.id.info_set_picimg);
		mUserName = (EditText) findViewById(R.id.info_set_nickname);
		mUserCompany = (EditText) findViewById(R.id.info_set_company);
		mUserDepartment = (EditText) findViewById(R.id.info_set_department);
		mUserPosition = (EditText) findViewById(R.id.info_set_position);
		mInvitationCode = (EditText) findViewById(R.id.info_set_invitation_code);
		mStartButton = (Button) findViewById(R.id.btn_jump_to_main);
		layoutChangeHead = (RelativeLayout) findViewById(R.id.layout_change_img_head);
		mSkipButton = (Button) findViewById(R.id.btn_skip);
		mLayoutButtons = (LinearLayout) findViewById(R.id.layout_buttons);
		imgCleanEdit = (ImageView) findViewById(R.id.img_clean_edit);
		imgCleanCompanyEdit = (ImageView) findViewById(R.id.img_clean_company_edit);
		imgCleanDepartmentEdit = (ImageView) findViewById(R.id.img_clean_department_edit);
		imgCleanPositionEdit = (ImageView) findViewById(R.id.img_clean_position_edit);
		imgCleanimgInvitationCode= (ImageView) findViewById(R.id.img_clean_invitation_code_edit);
	}

	private void initTitleBar() {
		ImageView imgBack = (ImageView) findViewById(R.id.back);
		TextView tvTitle = (TextView) findViewById(R.id.title);
		TextView tvSecTitle = (TextView) findViewById(R.id.tv_sec_title);
		Button btnSure = (Button) findViewById(R.id.tv_sure);
		RelativeLayout layout_title_bar = (RelativeLayout) findViewById(R.id.layout_title_bar);

		if (!isChangeUserInfo) {
			layout_title_bar.setVisibility(View.GONE);
		} else {
			tvTitle.setText(getString(R.string.account_info_setting));
			// 修改个人资料
			layout_title_bar.setVisibility(View.VISIBLE);
			mLayoutButtons.setVisibility(View.GONE);
			btnSure.setVisibility(View.VISIBLE);
			tvSecTitle.setVisibility(View.VISIBLE);
			imgBack.setOnClickListener(this);
			btnSure.setOnClickListener(this);
			tvSecTitle.setText(mAccount.getEmail());
		}
	}
	private void setUserHeadImgWithFirstChat(String userName) {
		if (!StringUtil.isEmpty(mAccount.getAccountBigHeadImg())) {
			ImageLoader.getInstance().displayImage(
					mAccount.getAccountBigHeadImg()
							+ GlobalConstants.USER_SMALL_HEAD_END,
					mUserHeadImg, options);
		} else {
			mUserHeadImg.setImageBitmap(ImageUtils.getUserFirstTextBitmap(this,
					userName));
		}
	}

	protected void setAccountValue(final String nickName, String userHeadImg) {
		// runOnUiThread(new Runnable() {
		//
		// @Override
		// public void run() {
		// tvUsername.setText(nickName);
		// mUserHeadImg.setImageBitmap(protraitBitmap);
		// }
		// });
		mAccount.setName(nickName);
		if (!StringUtil.isEmpty(userHeadImg)) {
			if (!userHeadImg.startsWith("http")
					&& !TextUtils.isEmpty(userHeadImg)) {
				userHeadPath = GlobalConstants.HOST_IMG + userHeadImg;
			}
			mAccount.setAccountBigHeadImg(userHeadPath);
		}
		mAccount.save(Preferences.getPreferences(this));
		if (!isChangeUserInfo) {
			Account account = Preferences.getPreferences(this).getAccount(
					mAccount.getUuid());
			Preferences.getPreferences(this).setDefaultAccount(account);
		}

	}

	protected void onNext(String nickName, String userHeadImg) {
		if (nickName != null || userHeadImg != null) {
			setAccountValue(nickName, userHeadImg);
		}
		if (!isChangeUserInfo) {
			actionTOMainActivty(mAccount);
		}
		finish();
	}

//	protected void uploadUserName(String username) {
//		ContactAttribute contact = new ContactAttribute();
//		contact.setNickName(username);
//		contact.setCompany(mUserCompany.getText().toString());
//		contact.setDepartment(mUserDepartment.getText().toString());
//		contact.setPosition(mUserPosition.getText().toString());
//		contact.setEmail(mAccount.getEmail());
//		String invitationCode=mInvitationCode.getText().toString();
//		contact.setInvitationCode(invitationCode);
//		//不允许邀请自己
//		if (invitationCode != null && MailChat.getInvationCode() != null) {
//			if (MailChat.getInvationCode().equals(invitationCode)) {
//				Toast.makeText(AccountSetupNameActivity.this, getString(R.string.invita_self_no_allow),
//						Toast.LENGTH_SHORT).show();
//				return;
//			}
//		}
//		mController.uploadUserInfo(mAccount, contact,SystemUtil.getCliendId(AccountSetupNameActivity.this),listener);
//	}

	protected void uploadUserInfo() {
		// 获取头像缩略图
		CommonUtils.hideSoftInput(this);
		ContactAttribute contact = new ContactAttribute();
		if (bigHeadPath != null) {
			contact.setImgHeadPath(bigHeadPath);
		}
		// 没有设置头像
		contact.setNickName(mUserName.getText().toString());
		contact.setCompany(mUserCompany.getText().toString());
		contact.setDepartment(mUserDepartment.getText().toString());
		contact.setPosition(mUserPosition.getText().toString());
		contact.setEmail(mAccount.getEmail());
		String invitationCode = mInvitationCode.getText().toString();
		contact.setInvitationCode(invitationCode);
		// 不允许邀请自己
		boolean canInvitation= cannotToInvitation(mAccount,invitationCode);
		if (canInvitation) {
			mController.uploadUserInfo(mAccount, contact,
					SystemUtil.getCliendId(AccountSetupNameActivity.this),
					listener);
		}
	}

	/**
	 * 
	 * method name: cannotToInvitation function @Description: TODO Parameters
	 * and return values description:
	 * 
	 * @param invitationCode
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-12-17 下午3:49:29 @Modified by：zhangjx
	 * @Description:不允许邀请自己
	 */
	private boolean cannotToInvitation(Account account,String invitationCode) {
		if (!StringUtils.isNullOrEmpty(invitationCode) && !StringUtils.isNullOrEmpty(account.getInvationCode())) {
			if (account.getInvationCode().equals(invitationCode)) {
				Toast.makeText(AccountSetupNameActivity.this,
						getString(R.string.invita_self_no_allow),
						Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		return true;
	}
	private void actionTOMainActivty(Account realAccount) {
		LocalSearch search = new LocalSearch(
				realAccount.getAutoExpandFolderName());
		search.addAllowedFolder(realAccount.getAutoExpandFolderName());
		search.addAccountUuid(realAccount.getUuid());
		Main4TabActivity.actionDisplaySearch(this, search, false, true);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_clean_edit:
			mUserName.setText(null);
			mUserName.setError(getString(R.string.must_set_contact_name));
			break;
		case R.id.img_clean_company_edit:
			mUserCompany.setText(null);
			break;
		case R.id.img_clean_department_edit:
			mUserDepartment.setText(null);
			break;
		case R.id.img_clean_position_edit:
			mUserPosition.setText(null);
			break;
		case R.id.img_clean_invitation_code_edit:
			mInvitationCode.setText(null);
			break;
		case R.id.btn_jump_to_main:
			isDeleteAccount = false;
			if (TextUtils.isEmpty(mUserName.getText().toString().trim())) {
//				Toast.makeText(AccountSetupNameActivity.this,
//						getString(R.string.must_set_name), Toast.LENGTH_SHORT)
//						.show();
				mUserName.setError(getString(R.string.must_set_contact_name));
				return;
			}
			if (!NetUtil.isActive()) {
				NetUtil.showNoConnectedAlertDlg(AccountSetupNameActivity.this);
				return;
			}
			uploadUserInfo();
			MobclickAgent.onEvent(AccountSetupNameActivity.this, "start_mailchat");
			break;
		case R.id.back:
			if (isChangeUserInfo) {
				CommonUtils.hideSoftInput(this);
				finish();
			}
			break;
		case R.id.btn_skip:
			onNext(null, null);
			MobclickAgent.onEvent(AccountSetupNameActivity.this, "skip_set_user_info");
			break;
		case R.id.layout_change_img_head:
			if (!NetUtil.isActive()) {
				NetUtil.showNoConnectedAlertDlg(AccountSetupNameActivity.this);
				return;
			}
			customChoseImgDialog();
			MobclickAgent.onEvent(AccountSetupNameActivity.this, "set_user_head");
			break;
		case R.id.tv_sure:
			CommonUtils.hideSoftInput(this);
			onSaveMailSign();
		   break;
		}
	}

	private void customChoseImgDialog() {
		dialogItems.clear();
		dialogItems.add(SetUserInfoDialog.LOCAL_IMAGE);
		dialogItems.add(SetUserInfoDialog.TAKE_PHOTOS);
		SetUserInfoDialog dialog = new SetUserInfoDialog(this, R.style.dialog,
				dialogItems, this);
		dialog.show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isChangeUserInfo) {
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isDeleteAccount&&!isChangeUserInfo) {
			Preferences.getPreferences(getApplicationContext()).deleteAccount(
					mAccount);
		}
        mController.removeListener(listener);
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case PICK_FROM_CAMERA:
			startActionCrop(mAccount, imgUri);
			break;
		case PICK_FROM_FILE:
			startActionCrop(mAccount, data.getData());
			break;
		case CROP_FROM_CAMERA:
			if (bigHeadPath != null) {
				isCreateDefaultHeadImg = false;
				mUserHeadImg.setImageBitmap(ImageUtils
						.getNativeImage(bigHeadPath,false));
			}
			break;
		}
	}

	protected void customChoseServerSettingDialog(final Account account) {
		dialogItems.clear();
		dialogItems.add(SetUserInfoDialog.INCOMING_SETTING);
		dialogItems.add(SetUserInfoDialog.OUTCOMING_SETTING);
		SetUserInfoDialog dialog = new SetUserInfoDialog(this, R.style.dialog,
				dialogItems, this);
		dialog.show();
	}

	@Override
	public void onDialogClick(int position) {
		switch (dialogItems.get(position)) {
		case SetUserInfoDialog.TAKE_PHOTOS:
			jumpToTakePhoto(mAccount);
			break;
		case SetUserInfoDialog.LOCAL_IMAGE:
			jumpToDCIM();
			break;
		default:
			break;
		}
	}

	private void onSaveMailSign() {
		String nickName = mUserName.getText().toString().trim();
		if (TextUtils.isEmpty(nickName)) {
			mUserName.setError(getString(R.string.must_set_contact_name));
			return;
		}
		String company = mUserCompany.getText().toString().trim();
		String department = mUserDepartment.getText().toString().trim();
		String position = mUserPosition.getText().toString().trim();
		String invitationCode=mInvitationCode.getText().toString().trim();
		//不允许邀请自己
		boolean canInvitation=cannotToInvitation(mAccount,invitationCode);
		if (canInvitation) {
			ContactAttribute contact = new ContactAttribute();
			contact.setNickName(nickName);
			contact.setCompany(company);
			contact.setDepartment(department);
			if (bigHeadPath != null&&!bigHeadPath.endsWith( GlobalConstants.USER_DIRECTORY
						+ GlobalConstants.IMAGE_DIRECTORY)) {
				contact.setImgHeadPath(bigHeadPath);
			}
			contact.setPosition(position);
			contact.setInvitationCode(invitationCode);
			Intent mIntent = new Intent();
			mIntent.putExtra("userInfo", contact);
			setResult(RESULT_OK, mIntent);
			finish();
		}
	}
}
