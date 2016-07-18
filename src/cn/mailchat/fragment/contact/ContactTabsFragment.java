package cn.mailchat.fragment.contact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.ChattingActivity;
import cn.mailchat.activity.ChooseContactsActivity;
import cn.mailchat.activity.CreateChattingActivity;
import cn.mailchat.adapter.ContactTabPagerAdapter;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.mail.Address;
import cn.mailchat.utils.GlobalTools;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.view.AddressViewCallBack;
import cn.mailchat.view.AddressViewControl;
import cn.mailchat.view.ChoseAddressView;
import cn.mailchat.view.pager.PagerSlidingTabStrip;
import cn.mailchat.view.pager.base.BaseFragment;

public class ContactTabsFragment extends BaseFragment implements
		OnPageChangeListener, AddressViewCallBack, ChooseContactListener {
	public static final String CONTACT_TAB_FRAGMENT = "cn.mailchat.fragment.contact.ContactTabsFragment";
	private PagerSlidingTabStrip mTabStrip;
	private ViewPager mViewPager;
	private ContactTabPagerAdapter mTabAdapter;
	private Account mAccount;
	private boolean isShowCheckbox = false;
	private boolean isAddGroupMembers;
	private boolean isComposeMail;
	private boolean isSearchView;
	private String mKeyWord;
	private boolean isInviteChat;
	private List<CGroupMember> mMembers;
	private String mGroupName;
	private ChoseAddressView mSearchContactsEditView;
	// scrollview最大高度
	public static int maxHeightScroll = 0;
	// 联系人输入框显示的最大内容条数
	private final static double SHOWCONTENTSCOUNT = 4.5;
	private ScrollView mSearchScroll;
	private LinearLayout mSearchContactsLayout;
	private RelativeLayout mLayoutContat;
	private TextView mEmptyView;
	private String editTextHit;
	private String keyWord = "";
	private MessagingController messagingController;

	private ContactTabsFragmentListener mContactTabsFragmentListener;
	private Preferences mPreferences;
	private ProgressDialog dialog;
	//mailInfo相关参数
	private boolean isMailInfo;
	private String[] mailInfoArray ;
	private Handler mHandler = new Handler();
	// ##################
	private MessagingListener listener = new MessagingListener() {
		/**
		 * 创建群成功
		 * 
		 * @Description:
		 * @param accountUuid
		 * @see:
		 * @since:
		 * @author: shengli
		 * @date:2014-10-16
		 */
		public void createGroupSuccess(String uuid, final CGroup cGroup) {
			if (mAccount.getUuid().equals(uuid) && isShowCheckbox) {
				// TODO Auto-generated method stub
				getActivity().runOnUiThread(new Runnable() {

					public void run() {
						if (isAdded()) {
							Toast.makeText(
									getActivity(),
									getActivity().getString(
											R.string.create_group_success),
									Toast.LENGTH_SHORT).show();
						}
						if(isMailInfo){
							ChattingActivity.actionGroupChatFromMailInfo(getActivity(), cGroup, mAccount, getNewMailInfoArray(cGroup));
						}else{
							ChattingActivity.actionCreateGroupSuccess(
									getActivity(), cGroup, mAccount);
						}

						dialog.hide();
						dialog.dismiss();
						getActivity().finish();
					}
				});
			}
		}

		public void createGroupFail(String uuid) {
			if (mAccount.getUuid().equals(uuid) && isShowCheckbox) {
				// TODO Auto-generated method stub
				getActivity().runOnUiThread(new Runnable() {

					public void run() {
						if (isAdded()) {
							Toast.makeText(
									getActivity(),
									getActivity().getString(
											R.string.create_group_fail),
									Toast.LENGTH_SHORT).show();
						}
						dialog.hide();
					}
				});
			}
		}

		// 增加组员成功
		public void addGroupMemberSuccess(Account acc, final String cGroupUid,
				List<CGroupMember> member) {
			if (mAccount.getUuid().equals(acc.getUuid()) && isShowCheckbox) {
				isAddMenberClick=true;
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (isAdded()) {
							Toast.makeText(
									getActivity(),
									getActivity().getResources().getString(
											R.string.add_group_member_success),
									Toast.LENGTH_SHORT).show();
						}
						messagingController.listLocalCGroupMember(mAccount,
								cGroupUid, null);
						getActivity().finish();
					}
				});
			}
		}

		// 增加组员失败
		public void addGroupMemberFailed(Account acc, List<CGroupMember> member) {
			if (mAccount.getUuid().equals(acc.getUuid()) && isShowCheckbox) {
				isAddMenberClick=true;
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (isAdded()) {
							Toast.makeText(getActivity(),
									getString(R.string.add_group_member_fail),
									Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		};

		public void inviteActionUsersSuccess(Account acc) {
			if (mAccount.getUuid().equals(acc.getUuid()) && isShowCheckbox) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (isAdded()) {
							MobclickAgent.onEvent(getActivity(), "invite_contact_success");
							Toast.makeText(getActivity(),
									R.string.invite_contacts_success,
									Toast.LENGTH_SHORT).show();
							dialog.dismiss();
							getActivity().finish();
						}
					}
				});
			}
		}

		public void inviteActionUsersFailed(Account acc) {
			if (mAccount.getUuid().equals(acc.getUuid()) && isShowCheckbox) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (isAdded()) {
							Toast.makeText(getActivity(),
									R.string.invite_contacts_fail,
									Toast.LENGTH_SHORT).show();
							dialog.dismiss();
						}
					}
				});
			}
		}
	};
	private ListView mListViewAutoComplete;
	private LinearLayout layoutViewpager;
	private List<Address> mAddresses;// 通过intent传递过来转换获得的地址
	//防止重复请求加群
	private boolean isAddMenberClick=true;

	// ##################
	public static ContactTabsFragment newInstance(boolean isShowCheckBox,
			List<CGroupMember> members, String groupName, String keyWord,
			boolean isSearchView, boolean isAddGroupMembers,
			boolean isComposeMail, boolean isInviteChat,boolean isMailInfo,String [] mailInfoArray) {
		ContactTabsFragment fragment = new ContactTabsFragment();
		Bundle args = new Bundle();
		args.putBoolean(ChooseContactsActivity.EXTRA_IS_SHOW_CHECK_BOX,
				isShowCheckBox);
		args.putBoolean(ChooseContactsActivity.EXTRA_IS_ADD_GROUP_MEMBER,
				isAddGroupMembers);
		args.putBoolean(ChooseContactsActivity.EXTRA_IS_COMPOSE_MAIL,
				isComposeMail);
		args.putSerializable(ChooseContactsActivity.EXTRA_MENBERS,
				(Serializable) members);
		args.putString(ChooseContactsActivity.EXTRA_GROUP_NAME, groupName);
		args.putBoolean(ChooseContactsActivity.EXTRA_IS_INVITE_CHAT,
				isInviteChat);
		// 搜索相关参数
		args.putString(ChooseContactsActivity.EXTRA_SEARCH_KEYWORD, keyWord);
		args.putBoolean(ChooseContactsActivity.EXTRA_IS_SEARCH_VIEW,
				isSearchView);
		//邮件界面创建群聊参数
		args.putBoolean(CreateChattingActivity.EXTRA_IS_MAIL_INFO, isMailInfo);
		args.putStringArray(CreateChattingActivity.EXTRA_MAIL_INFO, mailInfoArray);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mContactTabsFragmentListener = (ContactTabsFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.getClass()
					+ " must implement ContactTabsFragmentListener");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			isShowCheckbox = args
					.getBoolean(ChooseContactsActivity.EXTRA_IS_SHOW_CHECK_BOX);
			isAddGroupMembers = args
					.getBoolean(ChooseContactsActivity.EXTRA_IS_ADD_GROUP_MEMBER);
			isComposeMail = args
					.getBoolean(ChooseContactsActivity.EXTRA_IS_COMPOSE_MAIL);
			isSearchView = args
					.getBoolean(ChooseContactsActivity.EXTRA_IS_SEARCH_VIEW);
			mKeyWord = args
					.getString(ChooseContactsActivity.EXTRA_SEARCH_KEYWORD);
			isInviteChat = args
					.getBoolean(ChooseContactsActivity.EXTRA_IS_INVITE_CHAT);
			mMembers = (List<CGroupMember>) args
					.getSerializable(ChooseContactsActivity.EXTRA_MENBERS);
			mGroupName = args
					.getString(ChooseContactsActivity.EXTRA_GROUP_NAME);
			//mailInfo 邮件透传消息
			isMailInfo =  args.getBoolean(CreateChattingActivity.EXTRA_IS_MAIL_INFO);
			if(isMailInfo){
				mailInfoArray = args.getStringArray(CreateChattingActivity.EXTRA_MAIL_INFO);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		//Log.d("qxian", "--ContactTabsFragment--onCreateView");
		View view = inflater.inflate(R.layout.fragment_viewpager, container,
				false);
		initViews(view);
		initSearchEditView(view);
		initData();
		addAddressesFromOtherView();
		initListener();
		return view;
	}

	private void initListener() {
		mListViewAutoComplete.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 输入email没有匹配的，点击生成气泡 start
				if (mListViewAutoComplete.getHeaderViewsCount() > 0) {
					position = position
							- mListViewAutoComplete.getHeaderViewsCount();
				}
				String email = "";
				Fragment fragment = mTabAdapter.getCurrFragment();
				if (fragment instanceof ContactPersonalFragment) {
					ContactPersonalFragment currFragment = (ContactPersonalFragment) fragment;
					if (currFragment.getmSearchContactsEmailAdapter()
							.getmObjects() != null) {
						if (currFragment.getmSearchContactsEmailAdapter()
								.getmObjects().size() > 0) {
							email = currFragment
									.getmSearchContactsEmailAdapter()
									.getmObjects().get(position).toString();
							makeContact(email);
						}
					}
				} else if (fragment instanceof ContactSameDomainFragment) {
					ContactSameDomainFragment currFragment = (ContactSameDomainFragment) fragment;
					if (currFragment.getmSearchContactsEmailAdapter()
							.getmObjects() != null) {
						if (currFragment.getmSearchContactsEmailAdapter()
								.getmObjects().size() > 0) {
							email = currFragment
									.getmSearchContactsEmailAdapter()
									.getmObjects().get(position).toString();
							makeContact(email);
						}
					}
				} else if (fragment instanceof Contact35EisFragment) {
					Contact35EisFragment currFragment = (Contact35EisFragment) fragment;
					if (currFragment.getmSearchContactsEmailAdapter()
							.getmObjects() != null) {
						if (currFragment.getmSearchContactsEmailAdapter()
								.getmObjects().size() > 0) {
							email = currFragment
									.getmSearchContactsEmailAdapter()
									.getmObjects().get(position).toString();
							makeContact(email);
						}
					}
				}
				// 输入email没有匹配的，点击生成气泡 end
			}
		});

	}

	private void makeContact(String email) {
		if (!email.contains("@")) {
			return;
		}
		String userName = email.substring(0, email.indexOf("@"));
		if (!StringUtil.isEmailAllValid(email)) {
			Toast toast = Toast.makeText(getActivity(),
					R.string.message_compose_error_wrong_recipients,
					Toast.LENGTH_LONG);
			toast.show();
			return;
		}
		ContactAttribute contactAttribute = new ContactAttribute();
		contactAttribute.setEmail(email);
		contactAttribute.setNickName(userName);
		ArrayList<ContactAttribute> contactAttributes = new ArrayList<ContactAttribute>();
		contactAttributes.add(contactAttribute);
		addContacts(contactAttributes);
	}

	@Override
	public void onPause() {
		super.onPause();
//		messagingController.removeListener(listener);
		MobclickAgent.onPageEnd("ContactListFragment");
	}

	@Override
	public void onDestroyView() {
		mSearchContactsEditView.getAddresses().clear();
		mSearchContactsEditView.getAutoCompleteTextView().setText("");
		super.onDestroyView();
		messagingController.removeListener(listener);
		dialog.dismiss();
	}

	private void initSearchEditView(View view) {
		mSearchScroll = (ScrollView) view
				.findViewById(R.id.address_view_scroll);
		mSearchContactsLayout = (LinearLayout) view
				.findViewById(R.id.layout_view_contacts_search);
		mSearchContactsEditView = (ChoseAddressView) view
				.findViewById(R.id.cview_contacts_search);
		mLayoutContat = (RelativeLayout) view.findViewById(R.id.layout_contat);
		mEmptyView = (TextView) view.findViewById(R.id.txt_view_no_data);
		if (isShowCheckbox) {
			mSearchContactsLayout.setVisibility(View.VISIBLE);
			mSearchContactsEditView.setFocusable(true);
			mSearchContactsEditView.setFocusableInTouchMode(true);
		}
		// 选择框适配器
		mSearchContactsEditView.init(ChoseAddressView.AddressType.TO, "", this,
				true);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isShowCheckbox) {
			// hideView(searchLayout);
			if (mSearchContactsEditView.getAddresses() != null
					&& mSearchContactsEditView.getAddresses().size() > 0) {// 地址栏已经有成员
			} else {
				highLightKeyWord(
						mSearchContactsEditView.getAutoCompleteTextView(),
						getResources().getString(
								R.string.message_compose_edittext_tips_2));
			}
		} else {
			// searchLayout.setVisibility(View.VISIBLE);
		}
		// setSearchView();
		MobclickAgent.onPageStart("ContactListFragment");
	}

	private void highLightKeyWord(TextView textView, String content) {
		String editTextHit = getResources().getString(
				R.string.message_compose_edittext_tips);

		Pattern mPattern = Pattern.compile(content);
		SpannableString span = new SpannableString(editTextHit);
		Matcher matcher = mPattern.matcher(editTextHit);
		while (matcher.find()) {
			span.setSpan(
					new ForegroundColorSpan(getResources().getColor(
							R.color.message_op_bg)), matcher.start(),
					matcher.end(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		}
		textView.setHint(span);
	}

	private void initData() {
		mAccount = Preferences.getPreferences(getActivity())
				.getDefaultAccount();

		if (mTabAdapter == null) {
			// account.getVersion_35Mail() == 1 属于3.0邮箱
			mTabAdapter = new ContactTabPagerAdapter(getChildFragmentManager(),
					getActivity(), mViewPager, this, mKeyWord, mMembers,
					mGroupName, mAccount.getVersion_35Mail() == 1,
					isShowCheckbox, isAddGroupMembers, isComposeMail,
					isSearchView, isInviteChat);
		}
		mViewPager.setOffscreenPageLimit(mTabAdapter.getCacheCount());
		mViewPager.setAdapter(mTabAdapter);
		mViewPager.setOnPageChangeListener(this);
		mTabStrip.setViewPager(mViewPager);
		messagingController = MessagingController.getInstance(getActivity()
				.getApplication());
		mPreferences = Preferences.getPreferences(getActivity());
		messagingController.addListener(listener);
	}

	private void initViews(View view) {
		mTabStrip = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
		mViewPager = (ViewPager) view.findViewById(R.id.main_tab_pager);
		mListViewAutoComplete = (ListView) view
				.findViewById(R.id.lv_autoComplete);
		layoutViewpager = (LinearLayout) view
				.findViewById(R.id.layout_viewpager);
		dialog = new ProgressDialog(getActivity());
		dialog.setCancelable(false);
	}

	/**
	 * 
	 * method name: onPageScrollStateChanged
	 * 
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrollStateChanged(int)
	 *      function@Description: TODO
	 * @History memory:
	 * @Date：2015-10-8 下午3:41:51 @Modified by：zhangjx
	 * @Description：在状态改变的时候调用，其中arg0这个参数有三种状态（0，1，2） arg0
	 *                                                ==1的时辰默示正在滑动，arg0==2的时辰默示滑动完毕了
	 *                                                ，arg0==0的时辰默示什么都没做。
	 */
	@Override
	public void onPageScrollStateChanged(int arg0) {
		mTabStrip.onPageScrollStateChanged(arg0);
	}

	/**
	 * 
	 * method name: onPageScrolled
	 * 
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrolled(int,
	 *      float, int) function@Description: TODO
	 * @History memory:
	 * @Date：2015-10-8 下午3:42:57 @Modified by：zhangjx
	 * @Description：当页面在滑动的时候会调用此方法，在滑动被停止之前，此方法回一直得到调用。 arg0 :当前页面，及你点击滑动的页面
	 *                                                   arg1:当前页面偏移的百分比
	 *                                                   arg2:当前页面偏移的像素位置
	 * 
	 */
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		mTabStrip.onPageScrolled(arg0, arg1, arg2);
		mTabAdapter.onPageScrolled(arg0);
	}

	/**
	 * 
	 * method name: onPageSelected
	 * 
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageSelected(int)
	 *      function@Description: TODO
	 * @History memory:
	 * @Date：2015-10-8 下午3:44:41 @Modified by：zhangjx
	 * @Description：页面跳转完后得到调用，arg0是你当前选中的页面的Position（位置编号）
	 */
	@Override
	public void onPageSelected(int position) {
		mTabStrip.onPageSelected(position);
		mTabAdapter.onPageSelected(position);
		if (isShowCheckbox) {
			// 切换过来刷新下列表数据
			refrashListView();
			if (!isSearchView&&isShowCheckbox) {
				searchContacts(keyWord);
			}
		}
	}
	/**
	 * 动态设置联系人搜索框显示的最大高度 <li>现在设置的高度为显示4行半联系人的高度
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: Zhonggaoyong
	 * @date:2013-12-4
	 */
	protected void changeComposeAddressViewHeightByContent() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(100);
				} catch (Exception e) {

				}
				if (!isAdded()) {
					return;
				}
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						AddressViewControl control = mSearchContactsEditView
								.getAddressViewControl();
						// AddressViewControl 不可点击
						if (control.isClickable()) {
							control.setClickable(false);
						}
						// 如果联系人输入框次此时的高度小于给定高度的最大值，那么将其设置为自适应
						if (control.getHeight() < maxHeightScroll) {
							LinearLayout.LayoutParams paramsScroLayout = (LinearLayout.LayoutParams) mSearchScroll
									.getLayoutParams();
							if (paramsScroLayout.height != LinearLayout.LayoutParams.WRAP_CONTENT) {
								paramsScroLayout.height = LinearLayout.LayoutParams.WRAP_CONTENT;
								mSearchScroll.setLayoutParams(paramsScroLayout);
								mSearchScroll.invalidate();
							}
						}

						// 如果联系人输入框次此时的高度将要大于给定高度的最大值，那么将其固定在最大值
						if (maxHeightScroll == 0
								|| control.getHeight() >= maxHeightScroll) {
							for (int i = 0; i < control.getChildCount(); i++) {
								if (control.getChildAt(i) instanceof Button) {
									LinearLayout.LayoutParams paramsScroLayout = (LinearLayout.LayoutParams) mSearchScroll
											.getLayoutParams();
									if (maxHeightScroll != 0
											&& paramsScroLayout.height != maxHeightScroll) {
										paramsScroLayout.height = maxHeightScroll;
										mSearchScroll
												.setLayoutParams(paramsScroLayout);
										mSearchScroll.invalidate();
									} else if (maxHeightScroll == 0) {
										Button button = (Button) control
												.getChildAt(i);
										RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) button
												.getLayoutParams();
										// scrollview应该显示的总高度
										final int marginTop = GlobalTools
												.dip2px(getActivity(),
														control.PADDING[1]);
										final int marginBottom = GlobalTools
												.dip2px(getActivity(),
														control.PADDING[3]);
										maxHeightScroll = (int) ((params.height
												+ marginTop + marginBottom) * SHOWCONTENTSCOUNT);
									}
									mSearchScroll
											.fullScroll(ScrollView.FOCUS_DOWN);
									break;
								}
							}
						}
					}
				});

			}
		}).start();

	}

	// 联系人选择框 回调start
	@Override
	public void onActiveChanged(ChoseAddressView view, boolean active) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNameClicked(ChoseAddressView view) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onViewClicked(ChoseAddressView view) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAddClicked(ChoseAddressView view) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRemoveContactsCallBack(boolean isListValue) {
		// 删除的时候获取相应adapter更新数据
		refrashListView();
		updateChooseCount();
		// 如果没有群成员那么hint恢复
		if (mSearchContactsEditView.getAddresses().size() == 0) {
			mSearchContactsEditView.getAutoCompleteTextView().setHint(
					editTextHit);
			highLightKeyWord(
					mSearchContactsEditView.getAutoCompleteTextView(),
					getResources().getString(
							R.string.message_compose_edittext_tips_2));
		}
		changeComposeAddressViewHeightByContent();

	}

	@Override
	public void onAddContactsCallBack(Address[] addresses) {
		refrashListView();
		updateChooseCount();
		changeComposeAddressViewHeightByContent();
	}

	private void updateChooseCount() {
		mContactTabsFragmentListener.updateChoseCount(mSearchContactsEditView
				.getAddresses().size());
	}

	private void refrashListView() {
		Fragment fragment = mTabAdapter.getCurrFragment();
		if (fragment instanceof ContactPersonalFragment) {
			ContactPersonalFragment currFragment = (ContactPersonalFragment) fragment;
			currFragment.getmAdapter().setSearchContactsEditView(
					mSearchContactsEditView,mMembers);
			currFragment.getmCommonAdapter().setSearchContactsEditView(
					mSearchContactsEditView,mMembers);
			currFragment.getmAdapter().notifyDataSetChanged();
			currFragment.getmCommonAdapter().notifyDataSetChanged();
			if (mMembers!=null&&isShowCheckbox&&!isAddGroupMembers) {
				//是否允许删取消
				currFragment.getmCommonAdapter().setCanCancleChecked(true);
				currFragment.getmAdapter().setCanCancleChecked(true);
			}
		} else if (fragment instanceof ContactSameDomainFragment) {
			ContactSameDomainFragment currFragment = (ContactSameDomainFragment) fragment;
			currFragment.getmAdapter().setSearchContactsEditView(
					mSearchContactsEditView,mMembers);
			currFragment.getmCommonAdapter().setSearchContactsEditView(
					mSearchContactsEditView,mMembers);
			currFragment.getmAdapter().notifyDataSetChanged();
			currFragment.getmCommonAdapter().notifyDataSetChanged();
			if (mMembers!=null&&isShowCheckbox&&!isAddGroupMembers) {
				currFragment.getmCommonAdapter().setCanCancleChecked(true);
				currFragment.getmAdapter().setCanCancleChecked(true);
			}
		} else if (fragment instanceof Contact35EisFragment) {
			Contact35EisFragment currFragment = (Contact35EisFragment) fragment;
			currFragment.getmEisAdapter().setSearchContactsEditView(
					mSearchContactsEditView,mMembers);
			currFragment.getmCommonAdapter().setSearchContactsEditView(
					mSearchContactsEditView,mMembers);
			currFragment.getmEisAdapter().notifyDataSetChanged();
			currFragment.getmCommonAdapter().notifyDataSetChanged();
			if (mMembers!=null&&isShowCheckbox&&!isAddGroupMembers) {
				currFragment.getmCommonAdapter().setCanCancleChecked(true);
				currFragment.getmEisAdapter().setCanCancleChecked(true);
			}
		}
	}

	/**
	 * method name: addAddresses function @Description: TODO Parameters and
	 * return values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-10-18 下午3:03:51 @Modified by：zhangjx
	 * @Description：把从其他界面传过来的地址添加到输入框
	 */
	protected void addAddressesFromOtherView() {
		if (!isAddGroupMembers&&isShowCheckbox) {
			List<ContactAttribute> contactAttribute = new ArrayList<ContactAttribute>();
			if (mMembers != null && mMembers.size() > 0) {
				for (CGroupMember member : mMembers) {
					ContactAttribute userAttribute = new ContactAttribute();
					userAttribute.setNickName(member.getNickName());
					userAttribute.setEmail(member.getEmail());
					contactAttribute.add(userAttribute);
				}
			}
			if (contactAttribute != null && !contactAttribute.isEmpty()) {
				addContacts(contactAttribute);
			}
		}
	}
	@Override
	public void onSelectedAction(int action) {
	}

	@Override
	public void onTextWatcherCallBack(String s) {
		if (isShowCheckbox) {
			keyWord = s;
			searchContacts(keyWord);
		}
	}

	public void searchContacts(String s) {
		Fragment fragment = mTabAdapter.getCurrFragment();
		if (fragment instanceof ContactPersonalFragment) {
			ContactPersonalFragment currFragment = (ContactPersonalFragment) fragment;
			currFragment.doSearch(s);
		} else if (fragment instanceof ContactSameDomainFragment) {
			ContactSameDomainFragment currFragment = (ContactSameDomainFragment) fragment;
			currFragment.doSearch(s);
		} else if (fragment instanceof Contact35EisFragment) {
			Contact35EisFragment currFragment = (Contact35EisFragment) fragment;
			currFragment.doSearch(s);
		}
	}

	// 联系人选择框 回调 end

	/**
	 * 联系人转换为地址模式
	 * 
	 * @Description:
	 * @param contactAttributes
	 * @return
	 * @see:
	 * @since:
	 * @author: ZhongGaoYong
	 * @date:2013-10-29
	 */

	private Address[] changeAddresssByContacts(List<ContactAttribute> contactAttributes) {

		if (contactAttributes == null) {
			return null;
		}
		Address[] addresses = new Address[contactAttributes.size()];
		for (int i = 0; i < contactAttributes.size(); i++) {
			ContactAttribute contactAttribute = contactAttributes.get(i);
			Address address = new Address("");
			address.setAddress(contactAttribute.getEmail());
			String	userName = contactAttribute.getNickName();
			String userNameChange=contactAttribute.getrNickName();
			if (!StringUtil.isEmpty(userNameChange)) {
				userName=userNameChange;
			}
			if (contactAttribute.isEisContact()&&StringUtil.isEmpty(userName)) {
				userName=contactAttribute.getName();
			}
			address.setPersonal(userName);
			addresses[i] = address;

		}
		return addresses;
	}

	/**
	 * 判断联系人是否存在
	 */
	private Boolean isDuplicateAddress(String emaill) {
		return mSearchContactsEditView.isDuplicateAddress(emaill);
	}

	@Override
	public void showChoosedToast(String email) {
		// 如果已经选过了，提示用户
		if (mSearchContactsEditView.isDuplicateAddress(email)) {
			Toast.makeText(getActivity(),
					R.string.message_compose_contact_repeat, Toast.LENGTH_LONG)
					.show();
			return;
		}
	}

	@Override
	public void addContacts(List<ContactAttribute> contact) {
		mSearchContactsEditView.addContacts(changeAddresssByContacts(contact));
	}

	@Override
	public void reomveContacts(String email) {
		mSearchContactsEditView.removeContacts(email);
	}

	@Override
	public void changeEditViewHeightAutoToContent() {
		changeComposeAddressViewHeightByContent();
	}

	@Override
	public String getSearchContactsEditViewText() {
		return mSearchContactsEditView.getAutoCompleteTextView().getText()
				.toString();
	}

	public interface ContactTabsFragmentListener {
		void updateContactSearchReasut(int searchReasutCount);
		// 已选择的联系人数
		void updateChoseCount(int choseCount);
		void haveSearchResult();
	}

	@Override
	public boolean isContactChoosed(String email) {
		return isDuplicateAddress(email);
	}

	// #############发起单群聊 start###############
	/**
	 * 
	 * method name: onCreateChatting function @Description: TODO Parameters and
	 * return values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-10-17 下午1:51:10 @Modified by：zhangjx
	 * @Description：创建单聊或群聊
	 */
	public void onCreateChatting() {
		// 判断输入邮箱地址是否正确，若正确加入泡泡联系人中
		String Email = mSearchContactsEditView.getAddressViewControl()
				.getAutoCompleteTextView().getEditableText().toString();
		if (Email.contains("@china-channel.com")) {
			Email = Email.replace("@china-channel.com", "@35.cn");
		}
		if (mSearchContactsEditView.getAddresses().size() > 0) {
			mSearchContactsEditView.addContacts(Email);
		} else if (!TextUtils.isEmpty(mSearchContactsEditView
				.getAutoCompleteTextView().getText())) {
			if (!StringUtil.isValidEmailAddress(Email)) {
				Toast.makeText(getActivity(),
						R.string.message_compose_error_wrong_recipients,
						Toast.LENGTH_SHORT).show();
				return;
			} else {
				mSearchContactsEditView.addContacts(Email);
			}
		} else {
			Toast.makeText(getActivity(),
					R.string.message_compose_error_no_recipients,
					Toast.LENGTH_SHORT).show();
			return;
		}

		List<Address> addresses = mSearchContactsEditView.getAddresses();
		// List<Address> newAddresses = new ArrayList<Address>();
		// // 判断输入联系人与已选择联系人是否重复。
		// for (int i = 0; i < addresses.size(); i++) {
		// if (Email.equals(addresses.get(i).getAddress())) {
		// searchContactsEditView.getAddressViewControl()
		// .getAutoCompleteTextView().setText("");
		// }
		//
		// String str = addresses.get(i).getAddress();
		// if (str.contains("@china-channel.com")) {
		// String emailStr = str.replace("@china-channel.com", "@35.cn");
		// Address addr = new Address(emailStr);
		// newAddresses.add(i, addr);
		// } else {
		// newAddresses.add(i, new Address(str));
		// }
		//
		// }
		// 判断输入联系人与已选择联系人是否重复。
		for (int i = 0; i < addresses.size(); i++) {
			if (Email.equals(addresses.get(i).getAddress())) {
				mSearchContactsEditView.getAddressViewControl()
						.getAutoCompleteTextView().setText("");
			}
		}
		if (addresses.size() == 1) {
			if(isMailInfo){
				//创建群聊只发起一个用户
				createDChatWithMailInfo(addresses);
			}else{
				createDChat(addresses);
			}
			MobclickAgent.onEvent(getActivity(), "create_d_chat");
		} else {
			createGroupChat(addresses);
			MobclickAgent.onEvent(getActivity(), "create_g_chat");
		}
	}

	/**
	 * 
	 * method name: createDChat function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param newAddresses
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午1:54:57 @Modified by：zhangjx
	 * @Description：创建单聊
	 */
	private void createDChat(final List<Address> newAddresses) {
		dialog.setMessage(getString(R.string.create_dchat_dialog));
		messagingController.actionDChatOrInvitation(mAccount, getActivity(),
				mHandler, true, dialog,newAddresses.get(0).getAddress(), newAddresses.get(0)
				.getPersonal());
	}

	private void createDChatWithMailInfo(List<Address> newAddresses) {
		String email = newAddresses.get(0).getAddress();
		String name = newAddresses.get(0).getPersonal();
		String subject = mailInfoArray[2];
		String mailPreview = mailInfoArray[3];
		messagingController.actionDChatOrInvitation(mAccount, getActivity(),
				mHandler, true, dialog, email, name, subject, mailPreview);
	}
	/**
	 * 
	 * method name: chreteGroupChat function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param newAddresses
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午1:54:43 @Modified by：zhangjx
	 * @Description：创建群聊
	 */
	private void createGroupChat(List<Address> newAddresses) {
		List<CGroupMember> cGroupMembers = new ArrayList<CGroupMember>();
		for (int j = 0; j < newAddresses.size(); j++) {
			CGroupMember cGroupMember = new CGroupMember(newAddresses.get(j)
					.getPersonal(), newAddresses.get(j).getAddress());
			cGroupMembers.add(cGroupMember);
		}
		CGroup cGroup = new CGroup();
		cGroup.setUid(UUID.randomUUID().toString());
		cGroup.setMembers(cGroupMembers);
		if (cGroupMembers.size() >= 100) {
			Toast.makeText(getActivity(), R.string.create_group_members_limit,
					Toast.LENGTH_SHORT).show();
			return;
		}
		// cGroup.setGroupName(getResources().getString(R.string.no_group_name));
		messagingController.createGroup(mAccount, cGroup, null);
		dialog.setMessage(getString(R.string.create_group));
		dialog.show();
	}

	// #############发起单群聊 end###############
	// #############增加群成员 start#############
	/**
	 * 增加群成员
	 * 
	 * method name: onAddGroupMembers function @Description: TODO Parameters and
	 * return values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-11-10 下午4:55:26 @Modified by：zhangyq
	 * @Description：
	 */
	public void onAddGroupMembers(String groupUid) {
		// 增加组员，不生成泡泡
		// 判断输入邮箱地址是否正确，若正确加入泡泡联系人中
		String Email = mSearchContactsEditView.getAddressViewControl()
				.getAutoCompleteTextView().getEditableText().toString();
		if (Email.contains("@china-channel.com")) {
			Email = Email.replace("@china-channel.com", "@35.cn");
		}
		// 输入的完整邮箱是否已经存在
		if (mMembers != null && mMembers.size() > 0) {
			for (CGroupMember member : mMembers) {
				if (member.getEmail().equals(Email)) {
					Toast.makeText(getActivity(),
							getString(R.string.add_group_menber_exist),
							Toast.LENGTH_SHORT).show();
					return;
				}
			}
		}
		if (mSearchContactsEditView.getAddresses().size() > 0) {
			mSearchContactsEditView.addContacts(Email);
		} else if (!TextUtils.isEmpty(mSearchContactsEditView
				.getAutoCompleteTextView().getText())) {
			if (!StringUtil.isValidEmailAddress(Email)) {
				Toast.makeText(getActivity(),
						R.string.message_compose_error_wrong_recipients,
						Toast.LENGTH_SHORT).show();
				return;
			} else {
				mSearchContactsEditView.addContacts(Email);
			}
		} else {
			Toast.makeText(getActivity(),
					R.string.message_compose_error_no_recipients,
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (mMembers != null
				&& mMembers.size()
						+ mSearchContactsEditView.getAddresses().size() > 100) {
			Toast.makeText(getActivity(), R.string.create_group_members_limit,
					Toast.LENGTH_SHORT).show();
			return;
		}
		List<Address> addresses = mSearchContactsEditView.getAddresses();
		// 判断输入联系人与已选择联系人是否重复。
		for (int i = 0; i < addresses.size(); i++) {
			if (Email.equals(addresses.get(i).getAddress())) {
				mSearchContactsEditView.getAddressViewControl()
						.getAutoCompleteTextView().setText("");
			}
		}
		ArrayList<CGroupMember> addMembers = CGroup
				.getAddressChangeMembers(mSearchContactsEditView.getAddresses());
		if (isAddMenberClick) {
			isAddMenberClick=false;
			messagingController
			.addGroupMember(mAccount, groupUid, addMembers, null);
		}
	}

	// #############增加群成员 end#############
	/**
	 * method name: onAddComposeMailMember function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param void return type
	 * @History memory：
	 * @Date：2014-11-17 上午10:49:02 @Modified by：zhangjx
	 * @Description：
	 */
	public Set<ContactAttribute> onAddComposeMailMember() {
		List<Address> allAddresses = mSearchContactsEditView.getAddresses();
		Set<ContactAttribute> newContactSet=new HashSet<ContactAttribute>();
//		Fragment fragment = mTabAdapter.getCurrFragment();
//		if (fragment instanceof ContactPersonalFragment) {
//			ContactPersonalFragment currFragment = (ContactPersonalFragment) fragment;
//			newContactSet = currFragment.getSelectedContacts();
//		} else if (fragment instanceof ContactSameDomainFragment) {
//			ContactSameDomainFragment currFragment = (ContactSameDomainFragment) fragment;
//			newContactSet = currFragment.getSelectedContacts();
//		} else if (fragment instanceof Contact35EisFragment) {
//			Contact35EisFragment currFragment = (Contact35EisFragment) fragment;
//			newContactSet = currFragment.getSelectedContacts();
//		}
		ContactAttribute tempContact;
		if (allAddresses != null) {
			// 处理因为空格而加入到输入框的邮件地址
			for (Address address : allAddresses) {
				tempContact = new ContactAttribute();
				tempContact.setEmail(address.getAddress());
				tempContact.setNickName(address.getPersonal());
				newContactSet.add(tempContact);
			}
		}
//		// // 判断输入邮箱地址是否正确，若正确加入泡泡联系人中
//		String email = mSearchContactsEditView.getAddressViewControl()
//				.getAutoCompleteTextView().getEditableText().toString();
//		// if (StringUtil.isValidEmailAddress(Email)) {
//		// searchContactsEditView.addContacts(Email);
//		// if (!Email.equals("")) {// 由于空格添加完邮件对象后引起的
//		// // 处理手动输入加入到已选中集合中
//		// newAddContact = new ContactAttribute();
//		// newAddContact.setEmail(Email);
//		// newAddContacts.add(newAddContact);
//		// }
//		// } else {
//		// Toast.makeText(mActivity,
//		// R.string.message_compose_error_wrong_recipients, 0).show();
//		// return null;
//		// }
//		// 判断是否有联系人
//		if (mSearchContactsEditView.getAddresses().size() == 0) {
//			Toast.makeText(getActivity(),
//					R.string.message_compose_error_no_recipients,
//					Toast.LENGTH_SHORT).show();
//			return null;
//		}
//		List<Address> addresses = mSearchContactsEditView.getAddresses();
//		// 判断输入联系人与已选择联系人是否重复。
//		for (int i = 0; i < addresses.size(); i++) {
//			if (email.equals(addresses.get(i).getAddress())) {
//				mSearchContactsEditView.getAddressViewControl()
//						.getAutoCompleteTextView().setText("");
//				Toast.makeText(getActivity(), getString(R.string.same_contact),
//						Toast.LENGTH_SHORT).show();
//			}
//		}
		return newContactSet;

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
	public void onInviteChat() {
		// 判断输入邮箱地址是否正确，若正确加入泡泡联系人中
		String Email = mSearchContactsEditView.getAddressViewControl()
				.getAutoCompleteTextView().getEditableText().toString();
		if (Email.contains("@china-channel.com")) {
			Email = Email.replace("@china-channel.com", "@35.cn");
		}
		if (mSearchContactsEditView.getAddresses().size() > 0) {
			mSearchContactsEditView.addContacts(Email);
		} else if (!TextUtils.isEmpty(mSearchContactsEditView
				.getAutoCompleteTextView().getText())) {
			if (!StringUtil.isValidEmailAddress(Email)) {
				Toast.makeText(getActivity(),
						R.string.message_compose_error_wrong_recipients,
						Toast.LENGTH_SHORT).show();
				return;
			} else {
				mSearchContactsEditView.addContacts(Email);
			}
		} else {
			Toast.makeText(getActivity(),
					R.string.message_compose_error_no_recipients,
					Toast.LENGTH_SHORT).show();
			return;
		}
		List<Address> addresses = mSearchContactsEditView.getAddresses();
		// 判断输入联系人与已选择联系人是否重复。
		for (int i = 0; i < addresses.size(); i++) {
			if (Email.equals(addresses.get(i).getAddress())) {
				mSearchContactsEditView.getAddressViewControl()
						.getAutoCompleteTextView().setText("");
			}
		}
		String invitAtionEmails = null;
		for (Address address : addresses) {
			if (invitAtionEmails == null) {
				invitAtionEmails = address.getAddress();
			} else {
				invitAtionEmails += "," + address.getAddress();
			}
		}
		messagingController.inviteActionUsers(mAccount, invitAtionEmails);
		dialog.setMessage(getString(R.string.inviting_contacts));
		dialog.show();
	}
	@Override
	public ListView getAutoCompleteListView() {
		return mListViewAutoComplete;
	}

	@Override
	public void ShowAutoCompleteListView() {
		mListViewAutoComplete.setVisibility(View.VISIBLE);
		layoutViewpager.setVisibility(View.GONE);
	}

	@Override
	public void hideAutoCompleteListView() {
		layoutViewpager.setVisibility(View.VISIBLE);
		mListViewAutoComplete.setVisibility(View.GONE);
	}

//	@Override
//	public void setMenbers() {
//		refrashListView();
//	}

	@Override
	public ChoseAddressView getSearchContactsEditView() {
		return mSearchContactsEditView;
	}

	private String[] getNewMailInfoArray(CGroup cGroup) {
		List<CGroupMember> cGroupMembers = cGroup.getMembers();
		boolean isChange = false;
		String email = mailInfoArray[0];
		String nickName = mailInfoArray[1];
		// 情况一 ：当邮件取nickName的是空的时候
		if (nickName == null) {
			isChange = true;
		}
		for (CGroupMember cGroupMember : cGroupMembers) {
			if (cGroupMember.getEmail().equals(email)) {
				if (isChange) {
					nickName = cGroupMember.getNickName();
				} else {
					// 情况二：当邮件取nickName和本地通讯录不为空时，且不相等
					String localNickName = cGroupMember.getNickName();
					if (localNickName != null
							&& !nickName.equals(localNickName)) {
						nickName = localNickName;
						isChange = true;
					}
				}
				break;
			}
		}
		String[] newMailInfoArray = null;
		if (isChange) {
			newMailInfoArray = new String[] { mailInfoArray[0], nickName,
					mailInfoArray[2], mailInfoArray[3] };
		} else {
			newMailInfoArray = mailInfoArray;
		}
		return newMailInfoArray;
	}
}