package cn.mailchat.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Gallery;
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
import cn.mailchat.activity.base.BaseSetUpUserHeadImgActivity;
import cn.mailchat.activity.setup.AccountSetupIncoming;
import cn.mailchat.activity.setup.AccountSetupNameActivity;
import cn.mailchat.activity.setup.AccountSetupOutgoing;
import cn.mailchat.chatting.beans.DChat;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.helper.SizeFormatter;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.mail.store.LocalStore;
import cn.mailchat.utils.CommonUtils;
import cn.mailchat.utils.GlobalTools;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.utils.SystemUtil;
import cn.mailchat.view.MailDialog;
import cn.mailchat.view.RoundImageView;
import cn.mailchat.view.SetUserInfoDialog;
import cn.mailchat.view.SetUserInfoDialog.SetUserInfoDialogListener;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
/**
 * 
 * @copyright © 35.com
 * @file name ：AccountSettingActivity.java
 * @author ：zhangjx
 * @create Data ：2016-1-12下午5:23:42
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2016-1-12下午5:23:42
 * @Modified by：zhangjx
 * @Description :账号设置页面
 * 1、提醒范围设置 二级标题功能隐藏
 */
public class AccountSettingActivity extends BaseSetUpUserHeadImgActivity
		implements OnClickListener, SetUserInfoDialogListener {

	public static final String EXTRA_ACCOUNT = "account";
	public static final int EXTRA_START_ACTIVITY_TAG = 1;
	public static final String EXTRA_ACCOUNT_POSITION = "-1";
	private String accountUuid;
	private Account mAccount;
	private Button mDeleteAccount,mCleanCache;
	private MessagingController mController;
	private boolean isDeleteAccount = true;
	private TextView tvMailComposeSignature,
			tvSettingAccountNotifationScopeSecondTitle,
			tvSettingAccountNotifationScopeTitle, tvUsername;
	private RelativeLayout  layoutAlwaysBccSelf, layoutNewMailNotify,
			layoutNewMsgNotify, layoutSetEmailComposeSign, layoutNotyfyScope,
			layoutServerSetting, layoutSettingUsername,layoutNewOANotify;
	private CheckBox checkboxAlwaysBccSelf, checkboxNewMailNotify,
	        checkboxNewMsgNotify,checkboxNewOANotify;
	private LinearLayout layoutSettingAccound, layoutName;
	private String userHeadPath;
	private int position;
	private File protraitFile;
	private Bitmap protraitBitmap;
	private List<Integer> dialogItems;
	private ProgressDialog mDialog;

	public static void actionAccountSettingActivityForResult(Activity activity, Account account,
			int position) {
		Intent i = new Intent(activity, AccountSettingActivity.class);
		i.putExtra(EXTRA_ACCOUNT, account.getUuid());
		i.putExtra(EXTRA_ACCOUNT_POSITION, position);
		activity.startActivityForResult(i, EXTRA_START_ACTIVITY_TAG);
	}

	public static void actionSetNames(Activity activity, Account account,
			int position) {
		Intent i = new Intent(activity, AccountSettingActivity.class);
		i.putExtra(EXTRA_ACCOUNT, account.getUuid());
		i.putExtra(EXTRA_ACCOUNT_POSITION, position);
		activity.startActivity(i);
	}

	private MessagingListener listener = new MessagingListener() {
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
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setUserInfo(newContactAttribute);
						}
					});
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
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(
							AccountSettingActivity.this,
							getString(R.string.upload_failed) + ","
									+ getString(R.string.item_net_subtitle),
							Toast.LENGTH_SHORT).show();
					// if (isDialogCreate) {
					// isDialogCreate = false;
					// changUserInfoFailedDialog();
					// }
				}
			});

		}

		public void getAccountCacheSize(Account account,final long size) {
			if (account.getEmail().equals(mAccount.getEmail())) {
				runOnUiThread(new Runnable() {
					public void run() {
						setCleanCacheViewTxt(size);
					}
				});
			}
		}

		public void cleanAccountCache(Account account,final boolean isCleanSuccess) {
			if (account.getEmail().equals(mAccount.getEmail())) {
				runOnUiThread(new Runnable() {
					public void run() {
						mDialog.dismiss();
						if(isCleanSuccess){
							MailChat.toast(String.format(
									getString(R.string.account_clean_cache_success),
									mAccount.getEmail()));
						}else{
							MailChat.toast(String.format(
									getString(R.string.account_clean_cache_fail),
									mAccount.getEmail()));
						}
						mController.getAccountCacheSize(mAccount);
					}
				});
			}
		}
	};
	private RoundImageView mUserHeadImg;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_setting);
		initImageLoader();
		decodeExtras();
		initTitleBar();
		initView();
		initData();
		initEvent();
	}

	private void initData() {
		getScWidth();
		setUserHeadImgWithFirstChat(getUserName());
		setUserName();
		mController = MessagingController.getInstance(getApplication());
		mController.addListener(listener);

		dialogItems = new ArrayList<Integer>();
		mController.loadRemoteUserInfo(AccountSettingActivity.this,mAccount, listener);
		checkboxNewMsgNotify.setChecked(MailChat.isTopMsgNotify() ? mAccount
				.isNewMsgNotifation() : false);
		checkboxNewMailNotify.setChecked(MailChat.isTopNotifyOn() ? mAccount
				.isNewMailNotifation() : false);
		checkboxNewOANotify.setChecked(mAccount.isNewOANotifation());
		tvMailComposeSignature.setText(mAccount.getSignature());
		checkboxAlwaysBccSelf.setChecked(
		        mAccount.getEmail().equalsIgnoreCase(mAccount.getAlwaysBcc()));
		setCleanCacheViewTxt(-1);
		mController.getAccountCacheSize(mAccount);
		mDialog=new ProgressDialog(this);
	}

	private void decodeExtras() {
		Intent intent = getIntent();
		accountUuid = intent.getStringExtra(EXTRA_ACCOUNT);
		position = intent.getIntExtra(EXTRA_ACCOUNT_POSITION, -1);
		mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
	}

	private void initEvent() {
		mUserHeadImg.setOnClickListener(this);
		mDeleteAccount.setOnClickListener(this);
		layoutNewMailNotify.setOnClickListener(this);
		layoutNewMsgNotify.setOnClickListener(this);
		layoutSetEmailComposeSign.setOnClickListener(this);
		layoutSettingUsername.setOnClickListener(this);
		layoutNotyfyScope.setOnClickListener(this);
		layoutServerSetting.setOnClickListener(this);
		checkboxNewMailNotify.setOnClickListener(this);
		checkboxNewMsgNotify.setOnClickListener(this);
		layoutNewOANotify.setOnClickListener(this);
		checkboxNewOANotify.setOnClickListener(this);
		layoutAlwaysBccSelf.setOnClickListener(this);
		checkboxAlwaysBccSelf.setOnClickListener(this);
		mCleanCache.setOnClickListener(this);
		TextWatcher validationTextWatcher = new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (isCreateDefaultHeadImg) {
//					changeUserHead();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		};
	}

//	private void changeUserHead() {
//		String newUserName = mUserName.getText().toString().trim();
//		if (newUserName.length() > 0) {
//			setUserHeadImgWithFirstChat(newUserName);
//		} else {
//			Toast.makeText(AccountSettingActivity.this,
//					getString(R.string.must_set_name), Toast.LENGTH_SHORT)
//					.show();
//		}
//	}

	private void setUserInfo(ContactAttribute newContactAttribute) {
		if (newContactAttribute != null) {
			userHeadPath = newContactAttribute.getImgHeadPath();
			String userNickName = newContactAttribute.getNickName();
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

			if (!TextUtils.isEmpty(userNickName)) {
				tvUsername.setText(userNickName);
				mAccount.setName(userNickName);
			} else {
				setUserName();
			}
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
			tvUsername.setText(mAccount.getName());
		} else {
			tvUsername.setText(getUserName());
		}

	}

	private void initView() {
		mUserHeadImg = (RoundImageView) findViewById(R.id.info_set_picimg);
		mDeleteAccount = (Button) findViewById(R.id.btn_delete_account);
		tvMailComposeSignature = (TextView) findViewById(R.id.mail_compose_signature);
		layoutAlwaysBccSelf = (RelativeLayout) findViewById(R.id.layout_always_bcc_self);
		checkboxAlwaysBccSelf = (CheckBox) findViewById(R.id.checkbox_always_bcc_self);
		layoutNewMailNotify = (RelativeLayout) findViewById(R.id.layout_new_mail_notify);
		checkboxNewMailNotify = (CheckBox) findViewById(R.id.checkbox_new_mail_notify);
		layoutNewMsgNotify = (RelativeLayout) findViewById(R.id.layout_new_msg_notify);
		checkboxNewMsgNotify = (CheckBox) findViewById(R.id.checkbox_new_msg_notify);
		layoutSettingAccound = (LinearLayout) findViewById(R.id.layout_setting_accound);
		layoutName = (LinearLayout) findViewById(R.id.layout_name);
		layoutSetEmailComposeSign = (RelativeLayout) findViewById(R.id.setting_account_email_sign_parent);
		layoutNotyfyScope = (RelativeLayout) findViewById(R.id.setting_account_notifation_scope_parent);
		layoutServerSetting = (RelativeLayout) findViewById(R.id.setting_account_server_setting_parent);
		layoutSettingUsername = (RelativeLayout) findViewById(R.id.setting_username_parent);
		tvSettingAccountNotifationScopeTitle = (TextView) findViewById(R.id.setting_account_notifation_scope_title);
		tvSettingAccountNotifationScopeSecondTitle = (TextView) findViewById(R.id.setting_account_notifation_scope_second_title);
		tvUsername = (TextView) findViewById(R.id.username);
		layoutSettingAccound.setVisibility(View.VISIBLE);
		mDeleteAccount.setVisibility(View.VISIBLE);
		String secondTitle = mAccount
				.getSettingAccountNotifationScopeSecondTitle();
		tvSettingAccountNotifationScopeSecondTitle.setText(secondTitle);
//		hiveViewAndCenter(secondTitle);
		layoutNewOANotify = (RelativeLayout) findViewById(R.id.layout_new_oa_notify);
		checkboxNewOANotify = (CheckBox) findViewById(R.id.checkbox_new_oa_notify);
		DChat dChat =null;
		try {
			dChat = mAccount.getLocalStore().getDChat(DChat.getDchatUid(mAccount.getEmail()+ "," + GlobalConstants.DCHAT_OA));
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(dChat==null){
			layoutNewOANotify.setVisibility(View.GONE);
		}
		mCleanCache = (Button) findViewById(R.id.btn_clean_cache);
	}

	private void initTitleBar() {
		ImageView imgBack = (ImageView) findViewById(R.id.back);
		TextView tvTitle = (TextView) findViewById(R.id.title);
		TextView tvSecTitle = (TextView) findViewById(R.id.tv_sec_title);
		RelativeLayout layout_title_bar = (RelativeLayout) findViewById(R.id.layout_title_bar);
		tvTitle.setText(getString(R.string.account_setup_nickname));
		layout_title_bar.setVisibility(View.VISIBLE);
		tvSecTitle.setVisibility(View.VISIBLE);
		imgBack.setOnClickListener(this);
		tvSecTitle.setText(mAccount.getEmail());
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

	}
//
//	/**
//	 * 
//	 * method name: hiveViewAndCenter function @Description: TODO Parameters and
//	 * return values description:
//	 * 
//	 * @param secondTitle
//	 *            field_name void return type
//	 * @History memory：
//	 * @Date：2016-1-12 下午5:21:31 @Modified by：zhangjx
//	 * @Description:
//	 */
//	private void hiveViewAndCenter(String secondTitle) {
//		if (TextUtils.isEmpty(secondTitle)) {
//			tvSettingAccountNotifationScopeSecondTitle.setVisibility(View.GONE);
//			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
//					RelativeLayout.LayoutParams.WRAP_CONTENT,
//					RelativeLayout.LayoutParams.WRAP_CONTENT);
//			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
//			tvSettingAccountNotifationScopeTitle.setLayoutParams(layoutParams);
//		} else {
//			tvSettingAccountNotifationScopeSecondTitle
//					.setVisibility(View.VISIBLE);
//			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
//					RelativeLayout.LayoutParams.WRAP_CONTENT,
//					RelativeLayout.LayoutParams.WRAP_CONTENT);
//			layoutParams.addRule(RelativeLayout.ALIGN_TOP);
//			layoutParams.setMargins(0,
//					GlobalTools.px2dip(AccountSettingActivity.this, 20), 0,
//					GlobalTools.px2dip(AccountSettingActivity.this, 20));
//			tvSettingAccountNotifationScopeTitle.setLayoutParams(layoutParams);
//		}
//	}

	protected void onNext(String nickName, String userHeadImg) {
		if (nickName != null || userHeadImg != null) {
			setAccountValue(nickName, userHeadImg);
		}
	}

//	protected void uploadUserInfo() {
//		// 获取头像缩略图
//		if (!StringUtil.isEmpty(bigHeadPath) && protraitFile.exists()) {
//			protraitBitmap = ImageUtils.getNativeImage(bigHeadPath);
//		} else {
//			Toast.makeText(this, getString(R.string.setting_upload_failed),
//					Toast.LENGTH_SHORT).show();
//			return;
//		}
//		CommonUtils.hideSoftInput(this);
//		ContactAttribute contact = new ContactAttribute();
//		if (bigHeadPath != null) {
//			contact.setNickName(mUserName.getText().toString());
//			contact.setImgHeadPath(bigHeadPath);
//			contact.setEmail(mAccount.getEmail());
//		} else {
//			// 没有设置头像
//			contact.setNickName(mUserName.getText().toString());
//			contact.setEmail(mAccount.getEmail());
//		}
//		mController.uploadUserInfo(mAccount, contact, listener);
//	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.info_set_picimg:
//			if (!NetUtil.isActive()) {
//				NetUtil.showNoConnectedAlertDlg(AccountSettingActivity.this);
//				return;
//			}
//			customChoseImgDialog();
			break;
		case R.id.layout_always_bcc_self:
		    checkboxAlwaysBccSelf.setChecked(!checkboxAlwaysBccSelf.isChecked());
		case R.id.checkbox_always_bcc_self:
		    if (checkboxAlwaysBccSelf.isChecked()) {
                mAccount.setAlwaysBcc(mAccount.getEmail());
				MobclickAgent.onEvent(AccountSettingActivity.this, "bcc_self");
            } else {
                mAccount.setAlwaysBcc(null);
            }
		    break;
		case R.id.layout_new_mail_notify:
			checkboxNewMailNotify
					.setChecked(!checkboxNewMailNotify.isChecked());
			mAccount.setNewMailNotifation(checkboxNewMailNotify.isChecked());
			break;
		case R.id.checkbox_new_mail_notify:
			mAccount.setNewMailNotifation(checkboxNewMailNotify.isChecked());
			break;
		case R.id.layout_new_msg_notify:
			checkboxNewMsgNotify.setChecked(!checkboxNewMsgNotify.isChecked());
			mAccount.setNewMsgNotifation(checkboxNewMsgNotify.isChecked());
			break;
		case R.id.checkbox_new_msg_notify:
			mAccount.setNewMsgNotifation(checkboxNewMsgNotify.isChecked());
			break;
		case R.id.layout_new_oa_notify:
			checkboxNewOANotify.setChecked(!checkboxNewOANotify.isChecked());
			mAccount.setNewOANotifation(checkboxNewOANotify.isChecked());
			mAccount.save(Preferences.getPreferences(this));
			break;
		case R.id.checkbox_new_oa_notify:
			mAccount.setNewOANotifation(checkboxNewOANotify.isChecked());
			mAccount.save(Preferences.getPreferences(this));
			break;
		// 修改姓名
		case R.id.setting_username_parent:
			AccountSetupNameActivity.actionSetNames(this, mAccount,-1,true);
			break;
		case R.id.setting_account_email_sign_parent:
			GroupChattingInfoChangeActivity.forwardContentEditActivity(
					AccountSettingActivity.this, tvMailComposeSignature
							.getText().toString(),
					GroupChattingInfoChangeActivity.CHANGE_MAIL_SIGN);
			break;
		case R.id.setting_account_notifation_scope_parent:
			ChooseFolder.displayFolderChoice(AccountSettingActivity.this,
					mAccount);
			break;
		case R.id.setting_account_server_setting_parent:
			customChoseServerSettingDialog(mAccount);
			break;
		// 删除账号
		case R.id.btn_delete_account:
			deleteDialog();
			break;
		case R.id.btn_clean_cache:
			cleanCacheDialog();
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
	protected void customChoseServerSettingDialog(final Account account) {
		dialogItems.clear();
		dialogItems.add(SetUserInfoDialog.INCOMING_SETTING);
		dialogItems.add(SetUserInfoDialog.OUTCOMING_SETTING);
		SetUserInfoDialog dialog = new SetUserInfoDialog(this, R.style.dialog,
				dialogItems, this);
		dialog.show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mController.removeListener(listener);
	}

	protected void uploadUserName(String username) {
		ContactAttribute contact = new ContactAttribute();
		contact.setNickName(username);
		contact.setEmail(mAccount.getEmail());
		mController.uploadUserInfo(mAccount, contact,SystemUtil.getCliendId(AccountSettingActivity.this), listener);
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
			// 选择完图片后就要上传到服务端
//			ContactAttribute cAttribute=new ContactAttribute();
//			cAttribute.setImgHeadPath(bigHeadPath);
//			uploadUserInfo(cAttribute);
			break;
		case GroupChattingInfoChangeActivity.RESULT_CHANGE_MAIL_SIGN:
			String content = data.getStringExtra("content");
			tvMailComposeSignature.setText(content);
			mAccount.setSignature(content);
			mAccount.save(Preferences.getPreferences(this));
			break;
		case AccountSetupNameActivity.RESULT_CHANGE_USER_NAME:
			//修改用户信息
			ContactAttribute contact = (ContactAttribute) data.getSerializableExtra("userInfo");
			uploadUserInfo(contact);
			setUserHeadImgWithFirstChat(contact.getNickName());
			break;
		case ChooseFolder.CHOOSE_PUSH_FOLDER:
//			String secondTitle = data
//					.getStringExtra(ChooseFolder.EXTRA_SECONT_TITLE);
//			tvSettingAccountNotifationScopeSecondTitle.setText(secondTitle);
//			hiveViewAndCenter(secondTitle);
//			mAccount.setSettingAccountNotifationScopeSecondTitle(secondTitle);
			mAccount.save(Preferences.getPreferences(this));
			break;
		}
	}

	protected void uploadUserInfo(ContactAttribute contact) {
		// 获取头像缩略图
		CommonUtils.hideSoftInput(this);
		if (contact.getImgHeadPath() != null) {
			bigHeadPath = contact.getImgHeadPath();
			contact.setImgHeadPath(bigHeadPath);
		}
		// 没有设置头像
		contact.setNickName(contact.getNickName());
		contact.setCompany(contact.getCompany());
		contact.setDepartment(contact.getDepartment());
		contact.setPosition(contact.getPosition());
		contact.setInvitationCode(contact.getInvitationCode());
		contact.setEmail(mAccount.getEmail());
		mController.uploadUserInfo(mAccount, contact,
				SystemUtil.getCliendId(AccountSettingActivity.this), listener);
	}


	private void deleteDialog() {
		MailDialog.Builder builder = new MailDialog.Builder(this);
		builder.setTitle(R.string.operate_notice);
		builder.setMessage(getString(R.string.account_delete_tips,
				mAccount.getEmail()));
		builder.setPositiveButton(getString(R.string.okay_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent();
						intent.putExtra(EXTRA_ACCOUNT, mAccount.getUuid());
						intent.putExtra(EXTRA_ACCOUNT_POSITION, position);
						setResult(RESULT_OK, intent);
						finish();
						dialog.dismiss();
					}
				});
		builder.setNeutralButton(getString(R.string.cancel_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		builder.create().show();
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
		case SetUserInfoDialog.INCOMING_SETTING:
			// 跳转至收件服务器设置页
			Intent intent = AccountSetupIncoming
					.intentActionEditIncomingSettings(
							AccountSettingActivity.this, mAccount);
			intent.putExtra(MailChat.EXTRA_ACCOUNT_UPDATE, true);
			startActivity(intent);
			break;
		case SetUserInfoDialog.OUTCOMING_SETTING:
			// 跳转至发件服务器设置页
			Intent intent1 = AccountSetupOutgoing
					.intentActionEditOutgoingSettings(
							AccountSettingActivity.this, mAccount);
			intent1.putExtra(MailChat.EXTRA_ACCOUNT_UPDATE, true);
			startActivity(intent1);
			break;
		default:
			break;
		}
	}
	/**
	 * 清除缓存提示框
	 */
	private void cleanCacheDialog(){
		MailDialog.Builder builder = new MailDialog.Builder(this);
		builder.setTitle(getString(R.string.operate_notice));
		TextView textView =new TextView(this);
		textView.setText(String.format(getString(R.string.account_clean_cache_dialog_content), mAccount.getEmail()));
		textView.setTextColor(getResources().getColor(R.color.gray_black));
		textView.setPadding(0, 0, 0, GlobalTools.dip2px(this, 10));
		builder.setContentView(textView);
		builder.setPositiveButton(getString(R.string.okay_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						mDialog.setMessage(getString(R.string.cleaning));
						mDialog.show();
						mController.cleanAccountCache(mAccount);
						dialog.dismiss();
						MobclickAgent.onEvent(AccountSettingActivity.this, "clean_cache");
					}
				});
		builder.setNeutralButton(getString(R.string.cancel_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	// 清理缓存显示
	private void setCleanCacheViewTxt(long cacheSize) {
		String srcCleanCacheTxt = getString(R.string.account_clean_cache_size);
		String cleanCacheTxt = srcCleanCacheTxt.substring(0,
				srcCleanCacheTxt.indexOf("("));
		if (cacheSize != -1) {
			cleanCacheTxt = String.format(srcCleanCacheTxt,
					SizeFormatter.formatSize(this, cacheSize));
		}
		mCleanCache.setText(cleanCacheTxt);
	}
}
