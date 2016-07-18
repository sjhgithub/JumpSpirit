package cn.mailchat.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.umeng.analytics.MobclickAgent;

import u.aly.p;

import cn.mailchat.Account;
import cn.mailchat.BaseAccount;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.setup.AccountSetupBasics;
import cn.mailchat.activity.setup.AccountSetupNameActivity;
import cn.mailchat.adapter.AccountAutoCompleteAdapter;
import cn.mailchat.adapter.ContactPhoneAdapter;
import cn.mailchat.adapter.ContactPhoneAdapter.DeletePhoneAdapterListener;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.utils.CommonUtils;
import cn.mailchat.utils.NetUtil;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.view.MailDialog;
import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AddOrEditContactActivity extends MailChatActivity implements
		OnClickListener, DeletePhoneAdapterListener {

	private static final String EXTRA_ACCOUNT = "account";
	public static final String EXTRA_CONTACR = "contactInfo";
	public static final int REQUEST_CODE = 0;
	public static final String EXTRA_SURE_DEL = "sureDel";

	public static void actionAddOrEditContact(Context context, Account account,
			ContactAttribute contact) {
		Intent intent = new Intent(context, AddOrEditContactActivity.class);
		intent.putExtra(EXTRA_CONTACR, contact);
		intent.putExtra(EXTRA_ACCOUNT, account.getUuid());
		if (contact != null) {
			((Activity) context).startActivityForResult(intent, REQUEST_CODE);
		} else {
			context.startActivity(intent);
		}
	}

	private EditText editName, editCompany, editDepartment, editPosition,
			eidtAddr, editOther;
	private AutoCompleteTextView editEmail;
	private ListView listPhone;
	private LinearLayout addPhoneLayout;
	private ContactPhoneAdapter mPhoneAdapter;
	private MessagingController mController;
	private Account mAccount;
	private ContactAttribute contact;
	private boolean isEditModel = false;
	private Button btnDeleteContact;
	private TextView tvTitle;
	private MessagingListener listener = new MessagingListener() {
		@Override
		public void addContactSuccess(Account account,
				ContactAttribute contact, final boolean needRefreshView) {
			if(account.getUuid().equals(mAccount.getUuid())){
				AddOrEditContactActivity.this.finish();
			}
		}

		@Override
		public void addContactFailed(Account account) {
		}
		public void AddContactHasExist(Account account) {
			if(account.getUuid().equals(mAccount.getUuid())){
				Toast.makeText(AddOrEditContactActivity.this, getString(R.string.add_contact_exist), Toast.LENGTH_SHORT).show();
			}
		}
	};
	private RelativeLayout layoutEmail;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_contact);
		initParamFromIntent(getIntent());
		initTitleBar();
		initView();
		showSoftInput();
		initData();
		initListener();
	}

	private void initParamFromIntent(Intent intent) {
		contact = (ContactAttribute) intent.getSerializableExtra(EXTRA_CONTACR);
		if (contact != null) {
			isEditModel = true;
		}
		String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
		mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
	}

	private void initData() {
		mController = MessagingController.getInstance(getApplication());
		mController.addListener(listener);
		ArrayList<String> arrayEmails = new ArrayList<String>();
		AccountAutoCompleteAdapter adapter = new AccountAutoCompleteAdapter(
				AddOrEditContactActivity.this, arrayEmails, null);
		editEmail.setAdapter(adapter);
		mPhoneAdapter = new ContactPhoneAdapter(AddOrEditContactActivity.this);
		listPhone.setAdapter(mPhoneAdapter);
		mPhoneAdapter.setDeletePhoneAdapterListener(this);
		if (isEditModel) {
			//eis联系人不允许修改email/不允许删除
			if (contact.isEisContact()) {
				layoutEmail.setVisibility(View.GONE);
				btnDeleteContact.setVisibility(View.GONE);
			}else {
				btnDeleteContact.setVisibility(View.VISIBLE);
			}
			tvTitle.setText(getString(R.string.title_contact));
			hideSoftInput();
			// 姓名
			setTextViewValue(editName, contact.getNickName(),
					contact.getrNickName());
			// 郵箱
			setTextViewValue(editEmail, contact.getEmail(), contact.getrEmail());
			// 電話
			String phones = contact.getPhones();
			// if (!TextUtils.isEmpty(phones)&&phones!=null) {
			// listPhone.setVisibility(View.VISIBLE);
			// List<String>
			// phoneList=StringUtil.decodeStrToList(contact.getPhones());
			// mPhoneAdapter.addDataList(phoneList);
			// hideOrAddPhoneLayout(phoneList);
			// }
			String rPhones = contact.getrPhones();
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
				hideOrAddPhoneLayout(StringUtil.decodeStrToList(phones));
			} else {
				listPhone.setVisibility(View.GONE);
			}
			// 公司
			setTextViewValue(editCompany,contact.getrEmail(), contact.getCompany(),
					contact.getrCompany());
			// 部門
			setTextViewValue(editDepartment,contact.getrEmail(), contact.getDepartment(),
					contact.getrDepartment());
			// 職位
			setTextViewValue(editPosition,contact.getrEmail(), contact.getPosition(),
					contact.getrPosition());
			// 地址
			setTextViewValue(eidtAddr,contact.getrEmail(), contact.getAddr(), contact.getrAddr());
			// 其他
			setTextViewValue(editOther,contact.getrEmail(), contact.getOtherRemarks(),
					contact.getrOtherRemarks());
		}
	}
	private void setTextViewValue(TextView tView, String valueDefault,
			String valueChange) {
		if (!StringUtil.isEmpty(valueChange)) {
			tView.setText(valueChange);
		} else {
			tView.setText(valueDefault);
		}
	}
	private void setTextViewValue(TextView tView, String valueREmail, String valueDefault,
			String valueChange) {
		if (!StringUtil.isEmpty(valueREmail)) {
//			tView.setText(valueChange + "(" + valueDefault + ")");
			tView.setText(valueChange);
		} else {
			tView.setText(valueDefault);
		}
	}

	private void initListener() {
		btnDeleteContact.setOnClickListener(this);
		addPhoneLayout.setOnClickListener(this);
		editName.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					if (TextUtils.isEmpty(editName.getText().toString().trim())) {
//						 Toast.makeText(AddOrEditContactActivity.this,
//						 getString(R.string.must_set_name),
//						 Toast.LENGTH_SHORT).show();
						CharSequence html1 = Html.fromHtml("<font color='white'>"+getString(R.string.must_set_contact_name)+"</font>");
						editName.setError(html1);
					}
				}
			}
		});
		editEmail
				.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (!hasFocus) {
							if (TextUtils.isEmpty(editEmail.getText()
									.toString().trim())) {
								// Toast.makeText(
								// AddContactActivity.this,
								// getString(R.string.message_compose_error_no_recipient_email),
								// Toast.LENGTH_SHORT).show();
								editEmail
										.setError(getString(R.string.must_set_email));
								return;
							}
							if (!StringUtil.isValidEmailAddress(editEmail
									.getText().toString().trim())) {
								// Toast.makeText(
								// AddContactActivity.this,
								// getString(R.string.contact_email_error),
								// Toast.LENGTH_SHORT).show();
								editEmail
										.setError(getString(R.string.contact_email_error));
							}
						}
					}
				});
	}

	private void initView() {
		// 姓名
		editName = (EditText) findViewById(R.id.info_set_nickname);
		// 郵箱
		editEmail = (AutoCompleteTextView) findViewById(R.id.info_set_email);
		// 電話
		listPhone = (ListView) findViewById(R.id.listView_phone);
		// 添加電話
		addPhoneLayout = (LinearLayout) findViewById(R.id.layout_add_phone);
		layoutEmail = (RelativeLayout) findViewById(R.id.layout_email);
		// 公司
		editCompany = (EditText) findViewById(R.id.info_set_company);
		// 部門
		editDepartment = (EditText) findViewById(R.id.info_set_department);
		// 職位
		editPosition = (EditText) findViewById(R.id.info_set_position);
		// 地址
		eidtAddr = (EditText) findViewById(R.id.info_set_address);
		// 其他
		editOther = (EditText) findViewById(R.id.info_set_other);
		btnDeleteContact = (Button) findViewById(R.id.btn_delete_contact);
		listPhone.setVisibility(View.GONE);
	}

	protected void showSoftInput() {
		InputMethodManager inputManager = (InputMethodManager) editName
				.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.showSoftInput(editName, 0);
	}

	protected void hideSoftInput() {
		WindowManager.LayoutParams params = getWindow().getAttributes();
		// 隐藏软键盘
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
	}

	private void initTitleBar() {
		ImageView imgBack = (ImageView) findViewById(R.id.back);
		tvTitle = (TextView) findViewById(R.id.title);
		TextView tvSecTitle = (TextView) findViewById(R.id.tv_sec_title);
		Button btnSure = (Button) findViewById(R.id.tv_sure);
		RelativeLayout layout_title_bar = (RelativeLayout) findViewById(R.id.layout_title_bar);

		tvTitle.setText(getString(R.string.menu_add_contact));
		layout_title_bar.setVisibility(View.VISIBLE);
		btnSure.setVisibility(View.VISIBLE);
		tvSecTitle.setVisibility(View.GONE);
		imgBack.setOnClickListener(this);
		btnSure.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			CommonUtils.hideSoftInput(this);
			finish();
			break;
		case R.id.tv_sure:
			CommonUtils.hideSoftInput(this);
			// //姓名
			String name = editName.getText().toString().trim();
			// //郵箱
			String email = editEmail.getText().toString().trim();
			// //電話
			String phone = getPhones();
			// //公司
			String company = editCompany.getText().toString().trim();
			// //部門
			String department = editDepartment.getText().toString().trim();
			// //職位
			String position = editPosition.getText().toString().trim();
			// //地址
			String addr = eidtAddr.getText().toString().trim();
			// //其他
			String other = editOther.getText().toString().trim();
			if (TextUtils.isEmpty(name)) {
				editName.setError(getString(R.string.must_set_contact_name));
				return;
			}
			if (TextUtils.isEmpty(email)) {
				editEmail.setError(getString(R.string.must_set_email));
				// Toast.makeText(
				// AddContactActivity.this,
				// getString(R.string.message_compose_error_no_recipient_email),
				// Toast.LENGTH_SHORT).show();
				return;
			}
			if (!StringUtil.isValidEmailAddress(email)) {
				editEmail.setError(getString(R.string.contact_email_error));
				return;
			}
			ContactAttribute newContact = new ContactAttribute();
			// 修改的时候如果email有修改，更新表中值，其他的更新备注表中
			if (isEditModel) {
				newContact.setId(contact.getId());
				newContact.setEmail(email);
				newContact.setImgHeadHash(contact.getImgHeadHash());
				newContact.setImgHeadPath(contact.getImgHeadPath());
				newContact.setImgHeadUrl(contact.getImgHeadUrl());
				newContact.setEisContact(contact.isEisContact());
			} else {
				// 新增的时候email和name存到本地联系人表中，其他的存在备注表中
				newContact.setEmail(email);
				newContact.setNickName(name);
				if (email.contains(mAccount.getEmail())) {
					newContact.setEisContact(true);
				}
			}
			newContact.setrEmail(email);
			newContact.setrNickName(name);
			newContact.setrCompany(company);
			newContact.setrDepartment(department);
			newContact.setrPosition(position);
			newContact.setrPhones(phone);
			newContact.setrAddr(addr);
			newContact.setrOtherRemarks(other);
			mController.saveContactLocal(mAccount, newContact, isEditModel);
			//AddOrEditContactActivity.this.finish();
			break;
		case R.id.layout_add_phone:
			// int viewCount = mPhoneAdapter.getCount();
			// if (viewCount > 0) {
			// //没有空白item 再添加新的项
			// if (StringUtil.isEmpty(mPhoneAdapter.getData().get(
			// viewCount - 1))) {
			// Toast.makeText(AddContactActivity.this,
			// getString(R.string.set_empty_edit_first),
			// Toast.LENGTH_SHORT).show();
			// return;
			// }
			// //判断输入的手机号码格式是否正确
			// if (!StringUtil.isValidPhoneNo(mPhoneAdapter.getData().get(
			// viewCount - 1))) {
			// Toast.makeText(AddContactActivity.this,
			// getString(R.string.phone_err),
			// Toast.LENGTH_SHORT).show();
			// return;
			// }
			// }
			mPhoneAdapter.add("");
			listPhone.setVisibility(View.VISIBLE);
			hideOrAddPhoneLayout(mPhoneAdapter.getData());
			break;
		case R.id.btn_delete_contact:
			delContact(contact.getNickName());
			MobclickAgent.onEvent(AddOrEditContactActivity.this,
					"eidt_contact_view_delete");
			break;
		default:
			break;
		}
	}

	private String getPhones() {
		String phones = "";
		if (mPhoneAdapter != null) {
			List<String> phoneList = mPhoneAdapter.getData();
			if (phoneList.size() > 0) {
				int phoneSize = phoneList.size();
				for (int i = 0; i < phoneSize; i++) {
					if (!StringUtil.isEmpty(phoneList.get(i))) {
						phones += phoneList.get(i) + ",";
					}
				}
			}
		}
		if (phones.endsWith(",")) {
			phones = phones.substring(0, phones.length() - 1);
		}
		return phones;
	}

	@Override
	public void deletePhone(List<String> listData, int position,
			String deletePhone) {
		Map<Integer, Integer> delMap = mPhoneAdapter.getDelMap();
		if (delMap.containsKey(position)) {
			if (listData.size() > 0) {
				mPhoneAdapter.remove(position);
				delMap.remove(position);
			}
		}
		mPhoneAdapter.notifyDataSetChanged();
		hideOrAddPhoneLayout(listData);
	}

	private void hideOrAddPhoneLayout(List<String> listData) {
		if (listData.size() < 5) {
			addPhoneLayout.setVisibility(View.VISIBLE);
		} else {
			addPhoneLayout.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		mController.removeListener(listener);
		super.onDestroy();
	}

	@Override
	public void setListViewGone() {
		if (listPhone != null) {
			listPhone.setVisibility(View.GONE);

		}
	}

	public void delContact(String name) {
		MailDialog.Builder builder = new MailDialog.Builder(this);
		builder.setTitle(R.string.operate_notice);
		builder.setMessage(getString(R.string.contact_delete_tips, name));
		builder.setPositiveButton(getString(R.string.okay_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						Intent mIntent = new Intent();
						mIntent.putExtra(EXTRA_SURE_DEL, true);
						setResult(RESULT_OK, mIntent);
						dialog.dismiss();
						finish();
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
}
