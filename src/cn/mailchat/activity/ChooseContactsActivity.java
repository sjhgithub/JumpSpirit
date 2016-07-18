package cn.mailchat.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.R;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.fragment.contact.ContactTabsFragment;
import cn.mailchat.fragment.contact.ContactTabsFragment.ContactTabsFragmentListener;
import cn.mailchat.fragment.MessageViewFragment;
import cn.mailchat.view.MailDialog;
import cn.mailchat.utils.CommonUtils;
import cn.mailchat.utils.DataTransfer;

/**
 * 
 * @copyright © 35.com
 * @file name ：CreateChattingActivity.java
 * @author ：zhangjx
 * @create Data ：2014-10-17下午2:07:09
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-10-17下午2:07:09
 * @Modified by：zhangjx
 * @Description :发起聊天/新增群聊
 */
public class ChooseContactsActivity extends BaseActionbarFragmentActivity
		implements ContactTabsFragmentListener {
	private static String EXTRA_IS_ADD_GROUP_MEMBERS = "isAddGroupMembers";
	private static String EXTRA_GROUP_UID = "groupUid";
	public static final String EXTRA_ACCOUNTUUID = "accountUuid";
	private static final String EXTRA_CONTACTS = "contacts";
	private static final String EXTRA_IS_COMPOSE = "compose_mail";

	public static final String EXTRA_IS_SHOW_CHECK_BOX = "isShowCheckBox";
	public static final String EXTRA_MENBERS = "menbers";
	public static final String EXTRA_GROUP_NAME = "groupName";
	public static final String EXTRA_IS_ADD_GROUP_MEMBER = "isAddGroupMembers";
	public static final String EXTRA_IS_COMPOSE_MAIL = "isComposeMail";
	public static final String EXTRA_IS_INVITE_CHAT = "isInviteChat";
	public static final String EXTRA_SEARCH_KEYWORD = "searchKeyword";
	public static final String EXTRA_IS_SEARCH_VIEW = "searchView";
	private FragmentManager mFragmentManager;
	private View mCustomActionbarView;
	private TextView mActionbarTitle, mActionbarSure;

	private FragmentTransaction ft;
	private boolean isShowCheckBox;
	private boolean isAddGroupMembers;
	private List<CGroupMember> mMembers;
	private String groupName;
	private String groupUid;
	private TextView btnSure;
	private int mChoseCount;
	private boolean isInviteChat;

	private String formteString(int resId, int value) {
		return String.format(getString(resId), value);
	}

	private boolean isComposeMail;
	private ContactTabsFragment mContactListFragment;

	public static void forwardAction(Context context, Account mAccount,
			boolean isShowCheckBox) {
		Intent intent = new Intent(context, ChooseContactsActivity.class);
		intent.putExtra(EXTRA_IS_SHOW_CHECK_BOX, isShowCheckBox);
		context.startActivity(intent);
	}

	/**
	 * 
	 * method name: startActivityToCreateGroupChatting function @Description:
	 * TODO Parameters and return values description：
	 * 
	 * @param mContext
	 *            上下文
	 * @param mMembers
	 *            成员
	 * @param subject
	 *            群聊主题
	 * @param uuid
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-10-23 下午3:18:41 @Modified by：zhangjx
	 * @Description：其他界面跳转到发起聊天界面来创建群聊
	 */
	public static void startActivityToCreateGroupChatting(Context mContext,
			boolean isShowCheckBox, List<CGroupMember> mMembers, String subject) {
		Intent intent = new Intent(mContext, ChooseContactsActivity.class);
		if (mMembers != null && mMembers.size() > 0) {
			intent.putParcelableArrayListExtra(EXTRA_MENBERS,
					(ArrayList<? extends Parcelable>) mMembers);
		}
		intent.putExtra(EXTRA_GROUP_NAME, subject);
		intent.putExtra(EXTRA_IS_SHOW_CHECK_BOX, isShowCheckBox);
		((Activity) mContext).startActivity(intent);
	}

	/**
	 * 增加组员
	 * 
	 * method name: startActivityToCreateGroupChatting function @Description:
	 * TODO Parameters and return values description：
	 * 
	 * @param mContext
	 * @param isShowCheckBox
	 * @param mMembers
	 * @param subject
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-11-10 下午4:31:51 @Modified by：zhangyq
	 * @Description：
	 */
	public static void startActivityToAddGroupMembers(Context mContext,
			boolean isShowCheckBox, boolean isAddGroupMembers,
			List<CGroupMember> mMembers, String subject, String groupUid) {
		Intent intent = new Intent(mContext, ChooseContactsActivity.class);
		if (mMembers != null && mMembers.size() > 0) {
			intent.putParcelableArrayListExtra(EXTRA_MENBERS,
					(ArrayList<? extends Parcelable>) mMembers);
		}
		intent.putExtra(EXTRA_GROUP_NAME, subject);
		intent.putExtra(EXTRA_IS_SHOW_CHECK_BOX, isShowCheckBox);
		intent.putExtra(EXTRA_IS_ADD_GROUP_MEMBERS, isAddGroupMembers);
		intent.putExtra(EXTRA_GROUP_UID, groupUid);
		((Activity) mContext).startActivity(intent);
	}

	/**
	 * 
	 * method name: actionComposeMailView function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param context
	 * @param isComposeMail
	 * @param emailAddress
	 * @param mAccount
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-11-17 上午10:41:22 @Modified by：zhangjx
	 * @Description：写信页面新增联系人
	 */
	public static void actionComposeMailView(Context context,
			boolean isShowCheckBox, boolean isComposeMail,
			List<CGroupMember> mMembers, Account mAccount, int intentType) {
		Intent intent = new Intent(context, ChooseContactsActivity.class);
		if (mMembers != null && mMembers.size() > 0) {
			intent.putParcelableArrayListExtra(EXTRA_MENBERS,
					(ArrayList<? extends Parcelable>) mMembers);
		}
		intent.putExtra(EXTRA_IS_SHOW_CHECK_BOX, isShowCheckBox);
		intent.putExtra(EXTRA_ACCOUNTUUID, mAccount.getUuid());
		intent.putExtra(EXTRA_IS_COMPOSE, isComposeMail);
		((Activity) context).startActivityForResult(intent, intentType);
	}

	/**
	 * 发送邀请邮件
	 * 
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-27
	 */
	public static void startActivityToInviteChat(Context mContext,
			boolean isShowCheckBox, boolean isInviteChat) {
		Intent intent = new Intent(mContext, ChooseContactsActivity.class);
		intent.putExtra(EXTRA_IS_SHOW_CHECK_BOX, isShowCheckBox);
		intent.putExtra(EXTRA_IS_INVITE_CHAT, isInviteChat);
		((Activity) mContext).startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detial);
		initializeActionBar();
		if (!decodeExtras(getIntent())) {
			return;
		}
		initActionbarView();
		initFragment(savedInstanceState);
	}

	private boolean decodeExtras(Intent intent) {
		isShowCheckBox = intent.getBooleanExtra(EXTRA_IS_SHOW_CHECK_BOX, false);
		isAddGroupMembers = intent.getBooleanExtra(EXTRA_IS_ADD_GROUP_MEMBERS,
				false);
		isComposeMail = intent.getBooleanExtra(EXTRA_IS_COMPOSE, false);
		groupUid = intent.getStringExtra(EXTRA_GROUP_UID);
		mMembers = (List<CGroupMember>) getIntent().getSerializableExtra(
				EXTRA_MENBERS);
		groupName = getIntent().getStringExtra(EXTRA_GROUP_NAME);
		isInviteChat = intent.getBooleanExtra(EXTRA_IS_INVITE_CHAT, false);
		if (isInviteChat) {
			MobclickAgent.onEvent(getApplicationContext(),"into_invite_contact_act");
		}
		return true;
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
		btnSure = (TextView) mCustomActionbarView.findViewById(R.id.tv_sure);
	}

	private void initActionbarView() {
		// TODO Auto-generated method stub
		mActionbarTitle = (TextView) mCustomActionbarView
				.findViewById(R.id.tv_title);
		mActionbarSure = (TextView) mCustomActionbarView
				.findViewById(R.id.tv_sure);
		if (isAddGroupMembers) {
			setActionbarCenterTitle(mCustomActionbarView, mActionbarTitle,
					getResources().getString(R.string.add_group_members));
		} else if (isComposeMail) {
			setActionbarCenterTitle(mCustomActionbarView, mActionbarTitle,
					getResources().getString(R.string.main_tab_contant));
		} else if (isInviteChat) {
			setActionbarCenterTitle(mCustomActionbarView, mActionbarTitle,
					getResources().getString(R.string.invite_contacts));
		} else {
			setActionbarCenterTitle(mCustomActionbarView, mActionbarTitle,
					getResources().getString(R.string.create_chatting_title));
		}
		mActionbarSure.setOnClickListener(myClickListener());
	}

	private OnClickListener myClickListener() {
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.tv_sure:
					if (isAddGroupMembers) {
						mContactListFragment.onAddGroupMembers(groupUid);
					} else if (isComposeMail) {
						Set<ContactAttribute> newAddContacts = mContactListFragment
								.onAddComposeMailMember();
						if (newAddContacts != null) {
							// 被选中的
							if (newAddContacts.size() > 0) {
								// 回传
								DataTransfer
										.getInstance()
										.put(GlobalConstants.DATA_TRANSFER_FETCH_CONTACTS,
												new ArrayList<ContactAttribute>(
														newAddContacts));
								setResult(RESULT_OK);
							}
							finish();
						}
					} else if (isInviteChat) {
						MobclickAgent.onEvent(ChooseContactsActivity.this, "invite_contact_commit");
						mContactListFragment.onInviteChat();
					} else {
						mContactListFragment.onCreateChatting();
					}
					CommonUtils.hideSoftInput(ChooseContactsActivity.this);
					break;

				default:
					break;
				}
			}
		};
	}

	private void initFragment(Bundle savedInstanceState) {
		mFragmentManager = this.getSupportFragmentManager();
		if (savedInstanceState == null) {
			if (mFragmentManager
					.findFragmentByTag(ContactTabsFragment.CONTACT_TAB_FRAGMENT) == null) {
				ft = mFragmentManager.beginTransaction();
				mContactListFragment = ContactTabsFragment.newInstance(
						isShowCheckBox, mMembers, groupName, "", false,
						isAddGroupMembers, isComposeMail, isInviteChat,false,null);
				ft.replace(R.id.detial_content, mContactListFragment,
						ContactTabsFragment.CONTACT_TAB_FRAGMENT);
				ft.commit();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			if (isShowCheckBox && mChoseCount > 0) {
				isSureBackDialog();
			} else {
				finish();
			}
			CommonUtils.hideSoftInput(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mChoseCount > 0) {
				isSureBackDialog();
			} else {
				finish();
			}
		}
		return false;
	}

	private void isSureBackDialog() {
		MailDialog.Builder builder = new MailDialog.Builder(
				ChooseContactsActivity.this);
		builder.setTitle(R.string.operate_notice);

		/*
		 * if (isComposeMail) {
		 * builder.setMessage(getString(R.string.chose_contact_back_tips,
		 * mChoseCount)); }else{
		 * builder.setMessage(getString(R.string.create_chatting_back_tips,
		 * mChoseCount)); }
		 * builder.setPositiveButton(getString(R.string.okay_action), new
		 * DialogInterface.OnClickListener() {
		 * 
		 * public void onClick(DialogInterface dialog, int id) { finish();
		 * dialog.dismiss(); } });
		 * builder.setNeutralButton(getString(R.string.cancel_action), new
		 * DialogInterface.OnClickListener() {
		 * 
		 * public void onClick(DialogInterface dialog, int id) {
		 * dialog.dismiss(); } });
		 */
		// 修改放弃发起群聊提示
		// Modified by LL
		// BEGIN
		String okayString = null;
		String cancelString = null;

		if (isComposeMail||isInviteChat) {
			builder.setMessage(getString(R.string.chose_contact_back_tips,
					mChoseCount));
			okayString = getString(R.string.okay_action);
			cancelString = getString(R.string.cancel_action);
		} else{
			builder.setMessage(getString(R.string.create_chatting_back_tips,
					mChoseCount));
			okayString = getString(R.string.discard_create_chatting_okay_action);
			cancelString = getString(R.string.discard_create_chatting_cancel_action);
		}
		builder.setPositiveButton(okayString,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						finish();
						dialog.dismiss();
					}
				});
		builder.setNeutralButton(cancelString,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		// END

		builder.create().show();
	}

	// #ContactTabsFragmentListener start

	@Override
	public void updateContactSearchReasut(int searchReasutCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateChoseCount(int choseCount) {
		if (choseCount > 0) {
			mChoseCount = choseCount;
			btnSure.setText(formteString(R.string.sure_action, choseCount));
		} else {
			mChoseCount = -1;
			btnSure.setText(getString(R.string.okay_action));
		}

	}

	// #ContactTabsFragmentListener end
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
	public void haveSearchResult() {
		// TODO Auto-generated method stub
		
	}
}
