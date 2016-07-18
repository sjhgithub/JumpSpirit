 package cn.mailchat.activity;

import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.adapter.ContactPhoneAdapter;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.helper.Regex;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.view.MailDialog;
import cn.mailchat.view.RoundImageView;

/**
 * 
 * @copyright © 35.com
 * @file name ：ContactInfoActivity.java
 * @author ：zhangjx
 * @create Data ：2014-10-10下午2:48:11
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-10-10下午2:48:11
 * @Modified by：zhangjx
 * @Description :联系人详情页面
 */
public class ContactInfoActivity extends BaseActionbarFragmentActivity
		implements OnClickListener {
	private static final String TAG = "ContactInfoActivity";

	public static final String ACCOUNTUUID = "accountUuid";
	public static final String EXTRA_CONTACR = "contact";
	public static final String EXTRA_D_CHAT = "signle";
	public static final String EXTRA_MEMBER = "member";
	private static final String ACTION_SINGLE_SET = "signleSet";
	private Context mContext;
	private ContactAttribute mContact;
	private TextView tvSendMessage;
	private TextView tvCallPhone;
	private TextView tvNickName,tvMyChangeName;
	private TextView tvEmail;
	private TextView tvSendEmail;
	// 公司
	private TextView tvCompany;
	// 部门
	private TextView tvDepartment;
	// 职位
	private TextView tvPosition;
	// 地址
	private TextView tvAddr;
	// 其他
	private TextView tvOther;
	private ImageView ivSetContactImportant;
	private RoundImageView contactHead;
	private ProgressDialog dialog;
	private View mCustomActionbarView;
	private TextView mTitle;
	private String nickName;
	private String email;
	private Account mAccount;
	private Handler mHandler = new Handler();
	private MessagingController messagingController;
	private boolean isSingleSet;
	private DisplayImageOptions options;
	private TextView mActionbarTitle;
	private TextView mActionbarSure;
	private ContactPhoneAdapter mPhoneAdapter;

	private ListView listPhone;

	public static void actionView(Context context, Account account,
			ContactAttribute contact) {
		Intent intent = new Intent(context, ContactInfoActivity.class);
		intent.putExtra(ACCOUNTUUID, account.getUuid());
		intent.putExtra(EXTRA_CONTACR, contact);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public static void actionViewForSingleSetting(Context context,
			Account account, ContactAttribute contact) {
		Intent intent = new Intent(context, ContactInfoActivity.class);
		intent.putExtra(ACCOUNTUUID, account.getUuid());
		intent.putExtra(EXTRA_CONTACR, contact);
		intent.setAction(ACTION_SINGLE_SET);
		context.startActivity(intent);
	}

	private MessagingListener listener = new MessagingListener() {
		public void addContactSuccess(Account account,
				final ContactAttribute contact, final boolean needRefreshView) {
			runOnUiThread(new Runnable() {
				public void run() {
					if (needRefreshView) {
						// 需要刷新联系人信息
						if (contact != null) {
							setContactInfo(contact);
						}
					}
				}
			});
		}

		public void addContactFailed(Account account) {
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_info1);
		initializeActionBar();
		initActionbarView();
		initView();
		initParamFromIntent(getIntent());
		initImageLoader();
		initData();
		initListener();
	}

	private void initImageLoader() {
		options = MailChat.getInstance().initImageLoaderOptions();
	}

	private void initData() {
		messagingController = MessagingController.getInstance(getApplication());
		messagingController.addListener(listener);
	}

	private void setContactInfo(ContactAttribute contact) {
		this.mContact = contact;
//		if (mContact.isEisContact()) {
		if (mAccount.getEmail().equals(mContact.getEmail())) {
			//如果是自己不显示编辑
			 mActionbarSure.setVisibility(View.GONE);
			// eis用户展示不支持拨打电话
			// tvCallPhone.setVisibility(View.GONE);
		}
		// 邮箱
		email = mContact.getEmail();
		tvEmail.setText(email);
		// 昵称
		nickName = mContact.getNickName();

		if (nickName == null || nickName.equals("")) {
			nickName = email.substring(0, email.lastIndexOf("@"));
		}
		setTextViewNameValue(tvNickName,tvMyChangeName, nickName, mContact.getrNickName());
		// 头像
		if (!TextUtils.isEmpty(mContact.getImgHeadPath())) {
			String userHeadUrl = mContact.getImgHeadPath()
					+ GlobalConstants.USER_SMALL_HEAD_END;
			if (MailChat.DEBUG) {
				Log.d(MailChat.LOG_TAG, "user header image url is ::"
						+ userHeadUrl);
			}
			ImageLoader.getInstance().displayImage(userHeadUrl, contactHead,
					options);
		} else {
			if (!StringUtil.isEmpty(mContact.getrNickName())){
				nickName=mContact.getrNickName();
			}
			contactHead.setImageBitmap(ImageUtils.getUserFirstTextBitmap(
					mContext, nickName));
		}
		// 公司
		if (!TextUtils.isEmpty(mContact.getCompany())
				|| !TextUtils.isEmpty(mContact.getrCompany())) {
			boolean isShowView=setTextViewValue(tvCompany, mContact.getrEmail(),  mContact.getCompany(),
					mContact.getrCompany());
			if (isShowView) {
				findViewById(R.id.layout_company).setVisibility(View.VISIBLE);
			}else {
				findViewById(R.id.layout_company).setVisibility(View.GONE);
			}
		} else {
				findViewById(R.id.layout_company).setVisibility(View.GONE);
		}
		// 部门
		if (!TextUtils.isEmpty(mContact.getDepartment())
				|| !TextUtils.isEmpty(mContact.getrDepartment())) {
			boolean isShowView=setTextViewValue(tvDepartment, mContact.getrEmail(), mContact.getDepartment(),
					mContact.getrDepartment());
			if (isShowView) {
				findViewById(R.id.layout_department).setVisibility(View.VISIBLE);
			}else {
				findViewById(R.id.layout_department).setVisibility(View.GONE);
			}
			// tvDepartment.setText(mContact.getDepartment());
		} else {
			findViewById(R.id.layout_department).setVisibility(View.GONE);
		}
		// 電話
		String phones = mContact.getPhones();
		String rPhones = mContact.getrPhones();
		if (!TextUtils.isEmpty(phones) && phones != null
				|| !StringUtils.isNullOrEmpty(rPhones)) {
			if (TextUtils.isEmpty(phones) && !TextUtils.isEmpty(rPhones)) {
				phones = rPhones;
			} else if (!TextUtils.isEmpty(phones)
					&& !TextUtils.isEmpty(rPhones) && !phones.equals("null")
					&& !rPhones.equals("null")) {
				phones = phones + "," + rPhones;
			}
			listPhone.setVisibility(View.VISIBLE);
			mPhoneAdapter.addDataList(StringUtil.decodeStrToList(phones));
		} else {
			listPhone.setVisibility(View.GONE);
		}
		// 职位
		if (!TextUtils.isEmpty(mContact.getPosition())
				|| !TextUtils.isEmpty(mContact.getrPosition())) {
			boolean isShowView=setTextViewValue(tvPosition, mContact.getrEmail(), mContact.getPosition(),
					mContact.getrPosition());
			if (isShowView) {
				findViewById(R.id.layout_position).setVisibility(View.VISIBLE);
			}else {
				findViewById(R.id.layout_position).setVisibility(View.GONE);
			}
		} else {
			findViewById(R.id.layout_position).setVisibility(View.GONE);
		}
		// 地址
		if (!TextUtils.isEmpty(mContact.getAddr())
				|| !TextUtils.isEmpty(mContact.getrAddr())) {
			boolean isShowView=setTextViewValue(tvAddr, mContact.getrEmail(), mContact.getAddr(), mContact.getrAddr());
			// tvAddr.setText(mContact.getAddr());
			if (isShowView) {
				findViewById(R.id.layout_addr).setVisibility(View.VISIBLE);
			}else {
				findViewById(R.id.layout_addr).setVisibility(View.GONE);
			}
		} else {
			findViewById(R.id.layout_addr).setVisibility(View.GONE);
		}
		// 其他
		if (!TextUtils.isEmpty(mContact.getOtherRemarks())
				|| !TextUtils.isEmpty(mContact.getrOtherRemarks())) {
			boolean isShowView=setTextViewValue(tvOther, mContact.getrEmail(),mContact.getOtherRemarks(),
					mContact.getrOtherRemarks());
			// tvOther.setText(mContact.getOtherRemarks());
			if (isShowView) {
				findViewById(R.id.layout_other).setVisibility(View.VISIBLE);
			}else {
				findViewById(R.id.layout_other).setVisibility(View.GONE);
			}
		} else {
			findViewById(R.id.layout_other).setVisibility(View.GONE);
		}
	}

	private void setTextViewNameValue(TextView tView,TextView myChanegView, String valueDefault,
			String valueChange) {
		if (!StringUtil.isEmpty(valueChange)
				&& !StringUtil.isEmpty(valueDefault)) {
			if (valueChange.equals(valueDefault)) {
				tvMyChangeName.setVisibility(View.GONE);
				tView.setText(valueDefault);
			}else {
				tView.setText("(" +valueDefault+ ")" );
				tvMyChangeName.setVisibility(View.VISIBLE);
				tvMyChangeName.setText( valueChange );
			}
		} else {
			if (!StringUtil.isEmpty(valueChange)) {
				tView.setText(valueChange);
			} else {
				tView.setText(valueDefault);
			}
		}
	}

	/**
	 * 
	 * method name: setTextViewValue function @Description: TODO Parameters and
	 * return values description:
	 * 
	 * @param view
	 *            要显示的view
	 * @param valueDefault
	 *            服务端的value
	 * @param valueChange
	 *            本地修改的value field_name void return type
	 * @History memory：
	 * @Date：2015-11-20 下午8:23:39 @Modified by：zhangjx
	 * @Description:
	 */
	private boolean setTextViewValue(TextView tView, String valueREmail,
			String valueDefault, String valueChange) {
		boolean isShowView = false;
		// remark 表中有该用户的值
		if (!StringUtil.isEmpty(valueREmail)) {
			if (!StringUtil.isEmpty(valueChange)) {
				tView.setText(valueChange);
				isShowView = true;
			} else {
				if (!StringUtil.isEmpty(valueDefault)) {
					tView.setText(valueDefault);
					isShowView = true;
				} else {
					isShowView = false;
				}
			}
		} else {
			tView.setText(valueDefault);
			isShowView = true;
		}
		return isShowView;
	}

	private void initializeActionBar() {
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayUseLogoEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setCustomView(R.layout.actionbar_custom_right_btn);
		mCustomActionbarView = mActionBar.getCustomView();
		mActionBar.setTitle(null);
	}

	private void initActionbarView() {
		mActionbarTitle = (TextView) mCustomActionbarView
				.findViewById(R.id.tv_title);
		mActionbarSure = (TextView) mCustomActionbarView
				.findViewById(R.id.tv_sure);
		setActionbarCenterTitle(mCustomActionbarView, mActionbarTitle,
				getString(R.string.contacts_info_title));
		mActionbarSure.setText(getString(R.string.edit_contact));
		mActionbarSure.setOnClickListener(this);
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		messagingController.removeListener(listener);
		super.onDestroy();
	}

	private void initView() {
		mContext = ContactInfoActivity.this;
		tvSendEmail = (TextView) findViewById(R.id.tv_contact_sendmail);
		tvSendMessage = (TextView) findViewById(R.id.tv_contact_sendmessage);
		tvCallPhone = (TextView) findViewById(R.id.tv_contact_phonecall);
		tvNickName = (TextView) findViewById(R.id.tv_contact_nickname);
		tvMyChangeName = (TextView) findViewById(R.id.tv_my_nickname);
		tvEmail = (TextView) findViewById(R.id.tv_contact_email);
		contactHead = (RoundImageView) findViewById(R.id.img_contact_head);
		// 公司
		tvCompany = (TextView) findViewById(R.id.tv_company);
		// 部门
		tvDepartment = (TextView) findViewById(R.id.tv_department);
		// 职位
		tvPosition = (TextView) findViewById(R.id.tv_position);
		// 地址
		tvAddr = (TextView) findViewById(R.id.tv_addr);
		// 其他
		tvOther = (TextView) findViewById(R.id.tv_other);
		// 電話
		listPhone = (ListView) findViewById(R.id.listView_phone);
		// 设置为重要联系人
		ivSetContactImportant = (ImageView) findViewById(R.id.iv_set_contact_important);
		ivSetContactImportant.setVisibility(View.GONE);
		dialog = new ProgressDialog(mContext);
		dialog.setCancelable(false);
		dialog.setMessage(getString(R.string.create_dchat_dialog));
		mPhoneAdapter = new ContactPhoneAdapter(ContactInfoActivity.this);
		mPhoneAdapter.setJustShowPhone(true);
		listPhone.setAdapter(mPhoneAdapter);
	}

	private void initListener() {
		tvSendEmail.setOnClickListener(this);
		tvSendMessage.setOnClickListener(this);
		tvCallPhone.setOnClickListener(this);
		contactHead.setOnClickListener(this);
		ivSetContactImportant.setOnClickListener(this);
		listPhone.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mPhoneAdapter.getData().size() > 0) {
					callPhone(mPhoneAdapter.getData().get(position));
				}
			}
		});
	}

	private void initParamFromIntent(Intent intent) {
		String action = intent.getAction();
		if (action != null && action.equals(ACTION_SINGLE_SET)) {
			isSingleSet = true;
		}
		ContactAttribute contact = (ContactAttribute) intent
				.getSerializableExtra(EXTRA_CONTACR);

		mAccount = Preferences.getPreferences(this).getAccount(
				intent.getStringExtra(ACCOUNTUUID));
		setContactInfo(contact);
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.layout_back:
			finish();
			break;
		case R.id.tv_contact_sendmail:
			actionSendMessage();
			MobclickAgent.onEvent(mContext, "user_info_view_jump_to_mail");
			break;
		case R.id.tv_contact_sendmessage:
			actionViewChat();
			MobclickAgent.onEvent(mContext, "user_info_view_jump_to_chat");
			break;
		case R.id.tv_contact_phonecall:
			// 打电话
			String phones = mContact.getPhones();
			String rPhones = mContact.getrPhones();
			if (!StringUtils.isNullOrEmpty(phones)
					|| !StringUtils.isNullOrEmpty(rPhones)) {
				if (TextUtils.isEmpty(phones) && !TextUtils.isEmpty(rPhones)) {
					phones = rPhones;
				} else if (!TextUtils.isEmpty(phones)
						&& !TextUtils.isEmpty(rPhones)
						&& !phones.equals("null") && !rPhones.equals("null")) {
					phones = phones + "," + rPhones;
				}
				List<String> phoneList = StringUtil.decodeStrToList(phones);
				if (phoneList != null && phoneList.size() > 0) {
					if (phoneList.size() == 1) {
						callPhone(phoneList.get(0));
					} else {
						showCallPhonesDialog(phoneList);
					}
				}
			} else {
				showAddPhoneDialog();
			}
			MobclickAgent.onEvent(mContext, "take_phone");
			break;
		case R.id.img_contact_head:
			// 查看大图
			BigImageShowerActivity.newInstance(ContactInfoActivity.this,
					mContact.getImgHeadPath(), nickName);
			break;
		case R.id.iv_set_contact_important:
			// 设置为重要联系人
			// ivSetContactImportant.setImageDrawable(getDrawable(R.drawable.icon_make_contact_important_p));
			break;
		case R.id.tv_sure:
			// 跳转编辑
			//1、如果是自己
			if (!mAccount.getEmail().equals(mContact.getEmail())) {
				AddOrEditContactActivity.actionAddOrEditContact(
						ContactInfoActivity.this, mAccount, mContact);
			}
			MobclickAgent.onEvent(mContext, "edit_contact");
				
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * method name: actionViewChat function @Description: TODO Parameters and
	 * return values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-10-22 下午3:32:27 @Modified by：zhangjx
	 * @Description：跳转至单聊界面
	 */
	private void actionViewChat() {
		if (isSingleSet) {
			messagingController.finishForSingleSet(mAccount);
			finish();
		} else {
			messagingController.actionDChatOrInvitation(mAccount, mContext,
					mHandler,true, dialog,email,nickName);
		}
	}

	/**
	 * 
	 * method name: actionSendMessage function @Description: TODO Parameters and
	 * return values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-10-22 下午3:32:53 @Modified by：zhangjx
	 * @Description：跳转至写邮件界面
	 */
	private void actionSendMessage() {
	    if (mAccount.isHideAccount()) {
	        MailChat.toast(getString(R.string.contact_info_send_mail_anonymous_account_error));
	    } else if (!Regex.EMAIL_ADDRESS_PATTERN.matcher(email).matches()) {
	        MailChat.toast(getString(R.string.contact_info_send_mail_anonymous_account_error));
	    } else {
    		MailComposeActivity.actionCompose(this, mAccount, email);
    		finish();
	    }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case RESULT_OK:
			//删除账号
			boolean sureDel = data.getBooleanExtra(
					AddOrEditContactActivity.EXTRA_SURE_DEL, false);
			if (sureDel) {
				messagingController.updateContactVisibilityFlag(mAccount,
						mContact.getEmail(),
						false);
				finish();
			}
			break;
		default:
			break;
		}
	}

	private void showCallPhonesDialog(List<String> phones) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_list_phone, null);
		final ListView phoneList = (ListView) layout
				.findViewById(R.id.listView_phone);
		mPhoneAdapter = new ContactPhoneAdapter(ContactInfoActivity.this);
		mPhoneAdapter.setJustShowPhone(true);
		phoneList.setAdapter(mPhoneAdapter);
		mPhoneAdapter.addDataList(phones);

		MailDialog.Builder builder = new MailDialog.Builder(this);
		builder.setContentView(layout);
		builder.setTitle(R.string.chose_one_phone);
		builder.setFourthButton(getString(R.string.cancel_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		final MailDialog dialog = builder.create();
		dialog.show();
		phoneList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				callPhone(mPhoneAdapter.getItem(position).toString());
				if (dialog != null) {
					dialog.dismiss();
				}
			}
		});
	}

	private void callPhone(String phoneno) {
		Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
				+ phoneno));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	private void savePhone(String phone) {
		mContact.setrPhones(phone);
		mContact.setrEmail(TextUtils.isEmpty(mContact.getrEmail())?mContact.getEmail():mContact.getrEmail());
		mContact.setrNickName(TextUtils.isEmpty(mContact.getrNickName())?mContact.getNickName():mContact.getrNickName());
		mContact.setrCompany(TextUtils.isEmpty(mContact.getrCompany())?mContact.getCompany():mContact.getrCompany());
		mContact.setrPosition(TextUtils.isEmpty(mContact.getrPosition())?mContact.getPosition():mContact.getrPosition());
		mContact.setrDepartment(TextUtils.isEmpty(mContact.getrDepartment())?mContact.getDepartment():mContact.getrDepartment());
		mContact.setrAddr(TextUtils.isEmpty(mContact.getrAddr())?mContact.getAddr():mContact.getrAddr());
		mContact.setrOtherRemarks(TextUtils.isEmpty(mContact.getrOtherRemarks())?mContact.getOtherRemarks():mContact.getrOtherRemarks());
		messagingController.saveContactLocal(mAccount, mContact, true);
	}

	public void showAddPhoneDialog() {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_add_phone, null);
		final EditText etPhone = (EditText) layout.findViewById(R.id.et_phone);
		openInputMethod(etPhone);
		MailDialog.Builder builder = new MailDialog.Builder(this);
		builder.setTitle(R.string.add_phone_title);
		builder.setContentView(layout);
		builder.setPositiveButton(getString(R.string.save_phone_call),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						String phone = etPhone.getText().toString();
						if (phone == null || phone.equals("")) {
							etPhone.setError(getString(R.string.phone_cannot_empty));
							return;
						}
							// if (!StringUtil.isValidPhoneNo(phone)) {
							// etPhone.setError(getString(R.string.phone_error));
							// return;
							// }
							savePhone(phone);
							callPhone(phone);
							dialog.dismiss();
					}
				});
		builder.setNeutralButton(getString(R.string.cancel_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						closeInputMethod(etPhone);
						dialog.dismiss();
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
}
