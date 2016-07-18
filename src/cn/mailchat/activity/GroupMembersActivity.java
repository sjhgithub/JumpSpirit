package cn.mailchat.activity;

import java.util.List;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.adapter.GroupMembersAdapter;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.mail.MessagingException;

/**
 * 群成员查看
 * 
 * @copyright © 35.com
 * @file name ：GroupMembersActivity.java
 * @author ：zhangyq
 * @create Data ：2014-11-5下午7:00:35 
 * @Current Version：v1.0 
 * @History memory :
 * @Date : 2014-11-5下午7:00:35 
 * @Modified by：zhangyq
 * @Description :
 */
public class GroupMembersActivity extends BaseActionbarFragmentActivity implements OnItemClickListener, OnClickListener{
	public static final String ACCOUNTUUID = "accountUuid";
	private static final String EXTRA_GROUP = "group";;
	// 顶部栏actionBar
	private TextView mTitle;
	private View mCustomActionbarView;
	// 群组UID
	private CGroup mGroup;

	private GroupMembersAdapter mAdapter;
	private MessagingController controller;
	private Account mAccount;
	// 返回按钮
	private LinearLayout back;
	// 搜索框
	private EditText searshEdit;
	// 群成员结果集
	private List<CGroupMember> mGroupMembers;
	// 群成员listview
	private ListView groupMemberLv;
	// 数据为空是显示的文字提示
	private TextView emptyView;
	private ImageView imgView_search;
	private ImageView imgView_operation_add;
	private static final int GROUPCHAT_MEMBER = 1; // 群成员管理
	// 添加群成员
	private static final int GROUPCHAT_ADDMEMBER = 2;
	private static int mGroupChatMember;
	private Handler mHandler =new Handler();
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_members_view_group);

		mAccount = Preferences.getPreferences(this).getDefaultAccount();
		
		initializeActionBar();
		initView();
		initEvent();
		initData();
		controller = MessagingController.getInstance(getApplication());
		controller.addListener(listener);
		controller.listMembers(mAccount, mGroup, null);
		controller.getCGroupInfo(mAccount, mGroup, listener, true);
	}

	public static void actionView(Context context, CGroup group, int groupchat_member, Account mAccount) {
		mGroupChatMember = groupchat_member;
		Intent intent = new Intent(context, GroupMembersActivity.class);
		intent.putExtra(ACCOUNTUUID, mAccount.getUuid());
		intent.putExtra(EXTRA_GROUP, group);
		((Activity) context).startActivity(intent);
	}
	
	public void initializeActionBar() {
		mActionBar.setTitle(null);
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayUseLogoEnabled(true);
		// 返回按钮
		mActionBar.setDisplayHomeAsUpEnabled(true);
		// Inflate the custom view
		LayoutInflater inflater = LayoutInflater.from(this);
		mCustomActionbarView = inflater.inflate(
				R.layout.actionbar_custom_only_title, null);
		mActionBar.setCustomView(mCustomActionbarView);
		mTitle = (TextView) mCustomActionbarView.findViewById(R.id.tv_title);
		setActionbarCenterTitle(mCustomActionbarView, mTitle, getResources()
				.getString(R.string.chatting_see_group_member));
	}

	private void initView() {

		groupMemberLv = (ListView) findViewById(R.id.listview_view_members_group);
//		searshEdit = (EditText) findViewById(R.id.et_view_members_group_search);
//		back = (LinearLayout) findViewById(R.id.layout_back);
//		((TextView) findViewById(R.id.txtView_title)).setText(getString(R.string.group_set_gfind_member));
//		imgView_search=((ImageView) findViewById(R.id.imgView_search));
//		imgView_search.setVisibility(View.VISIBLE);
//		imgView_operation_add=((ImageView) findViewById(R.id.imgView_operation_add));
//		imgView_operation_add.setVisibility(View.VISIBLE);
		emptyView = new TextView(this);
		emptyView.setTextSize(20);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		emptyView.setText(getString(R.string.group_no_member));
		emptyView.setLayoutParams(lp);
		emptyView.setGravity(Gravity.CENTER);
		emptyView.setVisibility(View.GONE);
		((ViewGroup) groupMemberLv.getParent()).addView(emptyView);
		groupMemberLv.setEmptyView(emptyView);

	}

	private void initEvent() {
//		back.setOnClickListener(this);
		groupMemberLv.setOnItemClickListener(this);
//		imgView_search.setOnClickListener(this);
//		imgView_operation_add.setOnClickListener(this);
	}

	private void initData() {
		mGroup = (CGroup)getIntent().getSerializableExtra(EXTRA_GROUP);
		mAdapter = new GroupMembersAdapter(mGroup.getUid(), this, mAccount.getUuid(), this.getApplication());
		//mAdapter.setGroup(mGroup);
		//mAdapter.setData(mGroup.getMembers());
		groupMemberLv.setAdapter(mAdapter);
	}
	
	@Override
	protected void onDestroy() {
		controller.removeListener(listener);
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
//		if (resultCode == RESULT_OK) {
//			controller.addListener(listener);
//			controller.listMembers(mAccount, mGroup, ns, null);
//		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mGroupMembers = mAdapter.getData();
		CGroupMember member = mGroupMembers.get(position);
		ContactAttribute contact = new ContactAttribute();
		contact.setEmail(member.getEmail());
		contact.setNickName(member.getNickName());
		String avatarHash = member.getAvatarHash();
		contact.setImgHeadHash(avatarHash);
		if(avatarHash!=null){
			contact.setImgHeadPath(GlobalConstants.HOST_IMG+member.getAvatarHash());
		}
		//本来要放到逻辑层的。但是跳转的地方太多，简单处理了.
		try {
			contact = mAccount.getLocalStore().getContactsInfoForChat(contact,mAccount.getVersion_35Mail()==1);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ContactInfoActivity.actionView(
				GroupMembersActivity.this,
				mAccount, contact);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) { // 监控/拦截/屏蔽返回键
			setResult(RESULT_OK);
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.layout_back:

			setResult(RESULT_OK);
			this.finish();

			break;
//		case R.id.imgView_search:
//			controller.removeCallback(callback);
//			SearchCmembersActivity.actionView(GroupMembersActivity.this, mGroupUid, mGroupChatMember,mAccount);
//			break;
//		case R.id.imgView_operation_add:
//			AddGroupMembersActivity.actionView(GroupMembersActivity.this, mGroupUid, GROUPCHAT_ADDMEMBER,mAccount);
//			break;
		default:
			break;
		}

	}

	/**
	 * 回调
	 */
	private MessagingListener listener = new MessagingListener() {
		
		public void getGroupInfoSuccess(Account acc,final CGroup cGroup,final List<CGroupMember> members) {
			if(mAccount.getUuid().equals(acc.getUuid())){
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mGroup = cGroup;
						mAdapter.setGroup(mGroup);
						mAdapter.setData(cGroup.getMembers());
					}
				});
			}
		};
		
		public void delGroupMemberSuccess(Account acc,CGroupMember member) {
			if(mAccount.getUuid().equals(acc.getUuid())){
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						controller.listMembers(mAccount, mGroup, null);
						Toast.makeText(GroupMembersActivity.this, R.string.group_killout_member_successful, Toast.LENGTH_SHORT).show();
					}
				});
				
			}
		};
		
		public void delGroupMemberFailed(Account acc,CGroupMember member) {
			if(mAccount.getUuid().equals(acc.getUuid())){
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(GroupMembersActivity.this, R.string.group_killout_member_fialed, Toast.LENGTH_SHORT).show();
					}
				});
			}
		};
		
		public void listMembersSuccess(Account acc,final List<CGroupMember> members) {
			if(mAccount.getUuid().equals(acc.getUuid())){
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mGroupMembers = members;
						mAdapter.setGroup(mGroup);
						mAdapter.setData(members);
					}
				});
			}
		};
	};
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
}
