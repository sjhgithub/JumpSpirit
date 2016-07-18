package cn.mailchat.fragment.contact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.R.integer;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.ChooseContactsActivity;
import cn.mailchat.adapter.CommonContactsAdapter;
import cn.mailchat.adapter.Contact35EisAdapter;
import cn.mailchat.adapter.Contact35EisAdapter.OnTreeClickListener;
import cn.mailchat.adapter.ContactsListAdapter;
import cn.mailchat.adapter.SearchContactsEmailAdapter;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.view.IndexView;
import cn.mailchat.view.IndexView.OnIndexTouchListener;
import cn.mailchat.view.ProgressView;
import cn.mailchat.view.hlistview.HListView;
import cn.mailchat.view.pager.base.BaseTabFragment;

public abstract class BaseContactFragment extends BaseTabFragment implements
		OnIndexTouchListener, OnTreeClickListener, OnItemClickListener,
		cn.mailchat.view.hlistview.AdapterView.OnItemClickListener,
		SwipeRefreshLayout.OnRefreshListener {

	public static final String BUNDLE_KEY_CATALOG = "BUNDLE_KEY_CATALOG";
	public final int LOADING = 1;
	public final int EMPTY = 2;
	public final int ERROR = 3;
	public final int SHOW_CONTENT = 4;

	protected ListView mListView;
	protected Contact35EisAdapter mEisAdapter;
	protected HListView mHListView;
	protected RelativeLayout layoutCommContact;
	protected ContactsListAdapter mAdapter;
	protected CommonContactsAdapter mCommonAdapter;
	// 无搜索结果的时候显示 邮箱后缀供选择
	protected SearchContactsEmailAdapter mSearchContactsEmailAdapter;
	protected Account mAccount;
	private IndexView mIndexView;
	private TextView slideSelectResult;
	protected ProgressView mProgressView;
	protected SwipeRefreshLayout mSwipeRefreshLayout;
	private int top;
	protected boolean isShowCheckbox;// 發起聊天，顯示多選
	protected boolean isAddGroupMembers;
	protected boolean isComposeMail;
	protected boolean isSearchView;
	protected String mKeyWord;
	protected boolean isInviteChat;
	protected List<CGroupMember> mMembers;
	protected String mGroupName;
	protected LinearLayout mLayoutSlide;
	protected Set<ContactAttribute> mSelectedContacts;
	protected boolean isShowLoadingView = true;
	protected Set<String> selectedGroupStr;

	protected int getLayoutId() {
		return R.layout.fragment_persion_contact;
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
		}
		mAccount = Preferences.getPreferences(MailChat.application)
				.getDefaultAccount();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(getLayoutId(), container, false);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mSelectedContacts = new HashSet<ContactAttribute>();
		if (isShowCheckbox) {
			selectedGroupStr=new HashSet<String>();
		}
		initRefreshView(view);
		//设置某些情况下不显示加载条
//		if (isShowCheckbox) {
//			isShowLoadingView=false;
//		}
		initView(view);
		//设置完联系人后更新列表
//		if (isAddContact()) {
//			getChooseContactListener().setMenbers();
//		}
		setListener();
	}

//	private boolean isAddContact() {
//		return isAddGroupMembers || isComposeMail || mMembers != null;
//	}

	public void initView(View view) {
		//如果是选择联系人的页面，不支持下拉刷新
		if (!isShowCheckbox) {
			mSwipeRefreshLayout.setVisibility(View.VISIBLE);
			mListView = (ListView) view.findViewById(R.id.lv_with_fresh_layout);
		}else {
			mSwipeRefreshLayout.setVisibility(View.GONE);
			mListView = (ListView) view.findViewById(R.id.lv_no_fresh_layout);
			mListView.setVisibility(View.VISIBLE);
		}

		mProgressView = (ProgressView) view.findViewById(R.id.progress);

		View commContactListView = getHList();
		if (mCommonAdapter != null) {
			mHListView.setAdapter(mCommonAdapter);
		} else {
			mCommonAdapter = getCommonContactsAdapter();
			mHListView.setAdapter(mCommonAdapter);
			loadCommDataForListView();
		}
		mListView.addHeaderView(commContactListView);
		if (!isEisList()) {
			mIndexView = (IndexView) view
					.findViewById(R.id.llv_view_muliple_select_user);
			slideSelectResult = (TextView) view
					.findViewById(R.id.slide_select_result);
			mLayoutSlide = (LinearLayout) view.findViewById(R.id.slide_layout);
			if (mAdapter != null) {
				mListView.setAdapter(mAdapter);
			} else {
				mAdapter = getContactsPersionAdapter();
				mListView.setAdapter(mAdapter);
				loadDataForListView();
			}
			mAdapter.setShowCheckbox(isShowCheckbox);
		} else {
			if (mEisAdapter != null) {
				mListView.setAdapter(mEisAdapter);
			} else {
				mEisAdapter = getContact35EisAdapter();
				mListView.setAdapter(mEisAdapter);
				loadDataForListView();
			}
			mEisAdapter.setOnTreeClickListener(this);
		}

		// 无搜索结果显示的列表
		ArrayList<String> arrayAccounts = new ArrayList<String>();
		mSearchContactsEmailAdapter = new SearchContactsEmailAdapter(
				getActivity(), arrayAccounts);
	}

	private void initRefreshView(View view) {
		mSwipeRefreshLayout = (SwipeRefreshLayout) view
				.findViewById(R.id.swiperefreshlayout);
		mSwipeRefreshLayout.setOnRefreshListener(this);
		mSwipeRefreshLayout.setColorSchemeResources(
				R.color.swiperefresh_color1, R.color.swiperefresh_color2,
				R.color.swiperefresh_color3, R.color.swiperefresh_color4);
	}
	/**
	 * method name: setSwipeRefreshLoadingState function @Description: TODO
	 * Parameters and return values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2015-6-19 下午6:04:52 @Modified by：zhangjx
	 * @Description：设置顶部正在加载的状态
	 */
	protected void setSwipeRefreshLoadingState() {
		if (mSwipeRefreshLayout != null) {
			mSwipeRefreshLayout.setRefreshing(true);
			// 防止多次重复刷新
			mSwipeRefreshLayout.setEnabled(false);
		}
	}

	/**
	 * method name: setSwipeRefreshLoadedState function @Description: TODO
	 * Parameters and return values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2015-6-19 下午6:04:44 @Modified by：zhangjx
	 * @Description：设置顶部加载完毕的状态
	 */
	protected void setSwipeRefreshLoadedState() {
		if (mSwipeRefreshLayout != null) {
			mSwipeRefreshLayout.setRefreshing(false);
			mSwipeRefreshLayout.setEnabled(true);
		}
	}

	protected void hideView(View hideView) {
		hideView.setVisibility(View.GONE);
		hideView.forceLayout();
		hideView.postInvalidate();
	}

	protected void showView(View showView) {
		showView.setVisibility(View.VISIBLE);
		showView.forceLayout();
		showView.postInvalidate();
	}

	protected void setListener() {
		if (!isEisList()) {
			mIndexView.setOnIndexTouchListener(this);
			mListView.setOnItemClickListener(this);
		}
		mHListView.setOnItemClickListener(this);
	}

	/**
	 * 
	 * method name: showAutoView function @Description: TODO Parameters and
	 * return values description:
	 * 
	 * @param commonContacts
	 * @param results
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-23 下午5:37:56 @Modified by：zhangjx
	 * @Description:搜索不到联系人的时候显示邮箱后缀
	 */
	protected void showAutoCompleteListView(String keyWord,
			List<ContactAttribute> commonContacts,
			List<ContactAttribute> eisResults,
			List<ContactAttribute> personalContacts,
			List<ContactAttribute> sameDomainContacts) {
		if (!isAdded()) {
			return;
		}
		if (mListView.getHeaderViewsCount() > 0) {
			mListView.setSelection(mListView.getHeaderViewsCount());
		}
		if (isSearchView) {
			if (commonContacts != null || eisResults != null
					|| personalContacts != null || sameDomainContacts != null) {
				mSearchContactsEmailAdapter.notifyDataSetChanged();
			}
		}
		// 如果是35账号
		if (mAccount.getVersion_35Mail() == 1) {
			//eis没有搜索到数据并且个人通讯录也没有
				if (eisResults.isEmpty()&& personalContacts.isEmpty()) {
					// 显示
					getChooseContactListener().ShowAutoCompleteListView();
					// 赋值
					getChooseContactListener().getAutoCompleteListView()
					.setAdapter(mSearchContactsEmailAdapter);
					String str = getChooseContactListener()
							.getSearchContactsEditViewText();
					mSearchContactsEmailAdapter.performFiltering(str);
					if (!isEisList()) {
						hideView(mLayoutSlide);
					}
			} else {
				// 隐藏
				getChooseContactListener().hideAutoCompleteListView();
				//是个人通讯录
				if (!isEisList()) {
					if (personalContacts.isEmpty()&&!isSearchView) {
						mListView.setAdapter(mSearchContactsEmailAdapter);
						String str = getChooseContactListener()
								.getSearchContactsEditViewText();
						mSearchContactsEmailAdapter.performFiltering(str);
					} else {
						showView(mLayoutSlide);
						mListView.setAdapter(mAdapter);
						mAdapter.setContacts(null, personalContacts);
					}
				} else {
					if (eisResults.isEmpty()&&!isSearchView) {
						mListView.setAdapter(mSearchContactsEmailAdapter);
						String str = getChooseContactListener()
								.getSearchContactsEditViewText();
						mSearchContactsEmailAdapter.performFiltering(str);
					} else {
						if (keyWord==null||keyWord.equals("")) {
							mEisAdapter=null;
							mEisAdapter = getContact35EisAdapter();
							mListView.setAdapter(mEisAdapter);
							mEisAdapter.setOnTreeClickListener(this);
							try {
								mEisAdapter.setAllTreeBeens(eisResults);
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
						}else {
							//匹配出来后用普通的adapter展示
							if (mAdapter==null) {
								mAdapter = getContactsPersionAdapter();
							}
							mListView.setAdapter(mAdapter);
							mAdapter.setSearchContactsEditView( getChooseContactListener().getSearchContactsEditView(), null);
							mAdapter.setShowCheckbox(true);
							mAdapter.setContacts(null, eisResults);
							mListView.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id) {
									if (mListView.getHeaderViewsCount() > 0) {
										position = position - mListView.getHeaderViewsCount();
									}
									 addGroupMember( view,position,true);
								}
							});
						}
					}
				}
			}
		} else {
			if (sameDomainContacts.isEmpty() && personalContacts.isEmpty()) {
				// 显示
				getChooseContactListener().ShowAutoCompleteListView();
				// 赋值
				getChooseContactListener().getAutoCompleteListView()
						.setAdapter(mSearchContactsEmailAdapter);
				String str = getChooseContactListener()
						.getSearchContactsEditViewText();
				mSearchContactsEmailAdapter.performFiltering(str);
				mSearchContactsEmailAdapter.notifyDataSetChanged();
				if (!isEisList()) {
					hideView(mLayoutSlide);
				}
			} else {
				// 隐藏
				getChooseContactListener().hideAutoCompleteListView();
				if (!isSameDomainContactsList()) {
					if (personalContacts.isEmpty()&&!isSearchView) {
						mListView.setAdapter(mSearchContactsEmailAdapter);
						String str = getChooseContactListener()
								.getSearchContactsEditViewText();
						mSearchContactsEmailAdapter.performFiltering(str);
						mSearchContactsEmailAdapter.notifyDataSetChanged();
					} else {
						showView(mLayoutSlide);
						mListView.setAdapter(mAdapter);
						mAdapter.setContacts(null, personalContacts);
					}
				} else {
					if (sameDomainContacts.isEmpty()&&!isSearchView) {
						mListView.setAdapter(mSearchContactsEmailAdapter);
						String str = getChooseContactListener()
								.getSearchContactsEditViewText();
						mSearchContactsEmailAdapter.performFiltering(str);
						mSearchContactsEmailAdapter.notifyDataSetChanged();
					} else {
						showView(mLayoutSlide);
						mListView.setAdapter(mAdapter);
						mAdapter.setContacts(null, sameDomainContacts);
					}
				}
			}
		}
		// 如果没有常用联系人隐藏掉啊
		if (commonContacts==null||commonContacts.size() == 0||(((HeaderViewListAdapter) mListView.getAdapter())
				.getWrappedAdapter() instanceof SearchContactsEmailAdapter)) {
			hideView(layoutCommContact);
		} else {
			showView(layoutCommContact);
		}
	}

	/**
	 * 
	 * method name: setProgressView function @Description: TODO Parameters and
	 * return values description:
	 * 
	 * @param state
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-18 下午3:30:58 @Modified by：zhangjx
	 * @Description:显示隐藏加载
	 */
	protected void setProgressView(int state) {
		switch (state) {
		case LOADING:
			mProgressView.showLoading();
			break;
		case EMPTY:
			Drawable emptyDrawable = getResources().getDrawable(
					R.drawable.email_no_data);
			mProgressView.showEmpty(emptyDrawable,
					getString(R.string.progressViewEmptyTitlePlaceholder),
					getString(R.string.progressViewEmptyContentPlaceholder));
			break;
		case ERROR:
			Drawable errorDrawable = getResources().getDrawable(
					R.drawable.email_no_data);
			mProgressView.showError(errorDrawable,
					getString(R.string.progressViewErrorTitlePlaceholder),
					getString(R.string.progressViewErrorContentPlaceholder),
					getString(R.string.progressViewErrorButton), this);
			break;
		case SHOW_CONTENT:
			mProgressView.showContent();
			break;
		}
	}

	protected boolean requestDataIfViewCreated() {
		return true;
	}

	protected boolean isEisList() {
		return false;
	}
	protected boolean isSameDomainContactsList() {
		return false;
	}
	/**
	 * 
	 * method name: getHList function @Description: TODO Parameters and return
	 * values description:
	 * 
	 * @return field_name View return type
	 * @History memory：
	 * @Date：2015-9-18 下午3:25:16 @Modified by：zhangjx
	 * @Description:常用联系人
	 */
	protected View getHList() {
		// View commHListView = inflater.inflate(
		// R.layout.fragment_contact_person_comm_list_new, null);
		Context context = getActivity().getApplicationContext();
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View commHListView = inflater.inflate(
				R.layout.fragment_contact_person_comm_list_new, null);
		mHListView = (HListView) commHListView.findViewById(R.id.mHListView);
		layoutCommContact = (RelativeLayout) commHListView
				.findViewById(R.id.layout_comm_contact);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 0, 0, 26);
		layoutCommContact.setLayoutParams(params);
		return commHListView;
	}

	// ####################字母导航 start######################
	@Override
	public void onIndexTouchMove(char indexLeter) {
		// 显示悬浮界面
		if (slideSelectResult.getVisibility() != View.VISIBLE) {
			//隐藏软键盘
			hideSoftInput();
			slideSelectResult.setVisibility(View.VISIBLE);
		}
		slideSelectResult.setText(String.valueOf(indexLeter));
		// 索引值对应的位置
		int positoin;
		if (indexLeter == '☆') {
			positoin = 0;
		} else {
			positoin = mAdapter.getPositionByIndex(indexLeter);
		}
		top = -layoutCommContact.getMeasuredHeight() + mAdapter.getTagHeight()
				* positoin;
		// Log.d(TAG, "index:" + indexLeter + "  position:" +
		// positoin);
		if (positoin != -1) {
			mListView.setSelectionFromTop(positoin, top);
		}
	}
	private void hideSoftInput() {
		if (isShowCheckbox&&getChooseContactListener().getSearchContactsEditView()!=null) {
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
			imm.hideSoftInputFromWindow(getChooseContactListener().getSearchContactsEditView().getWindowToken(),0);
		}
	}
	@Override
	public void onIndexTouchUp() {
		slideSelectResult.setVisibility(View.GONE);
	}

	// ####################字母导航 end######################
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
	}

	@Override
	public void onItemClick(cn.mailchat.view.hlistview.AdapterView<?> parent,
			View view, int position, long id) {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view,
			ContactAttribute treeBeen, int position) {
	}

	@Override
	public void onGroupClick(AdapterView<?> parent, View view,
			ContactAttribute treeBeen) {
	}
	@Override
	public void onCheckedGroup(View v,ContactAttribute childList, int position) {
	}
	@Override
	public void onRefresh() {
	}
	public void choseContact(AdapterView<?> parent,
			cn.mailchat.view.hlistview.AdapterView<?> commParent, View view,
			int position) {
		if ((((HeaderViewListAdapter) mListView.getAdapter())
				.getWrappedAdapter() instanceof SearchContactsEmailAdapter)) {
			if (mListView.getHeaderViewsCount() > 0) {
				position = position - mListView.getHeaderViewsCount();
			}
			String email = mSearchContactsEmailAdapter.getItem(position)
					.toString();
			if (getChooseContactListener().isContactChoosed(email)) {
				getChooseContactListener().reomveContacts(email);
			} else {
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
				getChooseContactListener().addContacts(contactAttributes);
			}
			return;
		}
		if (parent!=null) {
			switch (parent.getId()) {
			case R.id.lv_no_fresh_layout:
			case R.id.lv_with_fresh_layout:
				seleteContact(parent, null, view, position);
				break;
			}
		} else if (commParent!=null) {
			switch (commParent.getId()) {
			case R.id.mHListView:
				seleteContact(null, commParent, view, position);
				break;

			}
		}
	}

	protected void seleteContact(AdapterView<?> parent,
			cn.mailchat.view.hlistview.AdapterView<?> commParent, View view,
			int position) {
		ContactAttribute contactAttribute = null;
		if (parent!=null ) {
			contactAttribute = (ContactAttribute) mListView.getAdapter()
					.getItem(position);
		} else if (commParent!= null) {
			// 常用联系人
			contactAttribute = (ContactAttribute) mHListView.getAdapter()
					.getItem(position);
		}
		CheckBox checkBox = (CheckBox) view
				.findViewById(R.id.muliple_select_user_item_cb);
		// 如果是搜索的话isShowCheckbox=false
		if (!isShowCheckbox) {
			getChooseContactListener().showChoosedToast(
					contactAttribute.getEmail());
			if (null != parent) {
				changeContactCheckBoxStatus(checkBox, position,
						contactAttribute, parent, null);
			} else if (null != commParent) {
				changeContactCheckBoxStatus(checkBox, position,
						contactAttribute, null, commParent);
			}

		} else {
			if (null != parent) {
				changeContactCheckBoxStatus(checkBox, position,
						contactAttribute, parent, null);
			} else if (null != commParent) {
				changeContactCheckBoxStatus(checkBox, position,
						contactAttribute, null, commParent);
			}

		}
		getChooseContactListener().changeEditViewHeightAutoToContent();
	}

	/**
	 * 
	 * method name: changeContactCheckBoxStatus function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param checkBox
	 * @param position
	 * @param contactAttribute
	 * @param adapter
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-10-17 下午12:21:37 @Modified by：zhangjx
	 * @Description：联系人选中状态改变
	 */
	public void changeContactCheckBoxStatus(CheckBox checkBox, int position,
			ContactAttribute contactAttribute, AdapterView<?> adapterView,
			cn.mailchat.view.hlistview.AdapterView<?> commAdapterView) {
		if ((((HeaderViewListAdapter) mListView.getAdapter())
				.getWrappedAdapter() instanceof SearchContactsEmailAdapter)) {
			String email = mSearchContactsEmailAdapter.getItem(position)
					.toString();
			if (getChooseContactListener().isContactChoosed(email)) {
				getChooseContactListener().reomveContacts(email);
			} else {
				String userName = email.substring(0, email.indexOf("@"));
				if (!StringUtil.isEmailAllValid(email)) {
					Toast toast = Toast.makeText(getActivity(),
							R.string.message_compose_error_wrong_recipients,
							Toast.LENGTH_LONG);
					toast.show();
					return;
				}
				ContactAttribute contact = new ContactAttribute();
				contact.setEmail(email);
				contact.setNickName(userName);
				ArrayList<ContactAttribute> contactAttributes = new ArrayList<ContactAttribute>();
				contactAttributes.add(contact);
				getChooseContactListener().addContacts(contactAttributes);
			}
			return;
		}
		if (checkBox==null) {
			return;
		}
		// 判断所有联系人选择框内是否存在此联系人。如果存在。那么点击则取消。否则添加
		if (checkBox.isChecked()) {
			if (null != adapterView) {
				if (!isEisList()) {
					position = position - mListView.getHeaderViewsCount();
					mSelectedContacts.remove(mAdapter.getAllContactsInList()
							.get(position));
				} else {
					ContactAttribute contactRemove=mEisAdapter.getmAllTreeBeens().get(position);
					mSelectedContacts.remove(contactRemove);
					//同步父级checkbox状态
					setParentCheckboxState(contactAttribute,true,position);
				}
			} else {
				mSelectedContacts
						.remove(mCommonAdapter.getData().get(position));
			}
			checkBox.setChecked(false);
			// 回调删除输入框中的联系人
			getChooseContactListener().reomveContacts(
					contactAttribute.getEmail());
			refreshListView();
		} else {
			if (adapterView != null) {
				switch (adapterView.getId()) {
				case R.id.lv_no_fresh_layout:
				case R.id.lv_with_fresh_layout:
					if (!isEisList()) {
						position = position - mListView.getHeaderViewsCount();
						mSelectedContacts.add(mAdapter.getAllContactsInList()
								.get(position));
					} else {
						mSelectedContacts.add(mEisAdapter.getmAllTreeBeens()
								.get(position));
						//同步父级checkbox状态
						setParentCheckboxState(contactAttribute,false,position);
					}
					break;
				}
			} else if (commAdapterView != null) {
				switch (commAdapterView.getId()) {
				case R.id.mHListView:
					mSelectedContacts.add(mCommonAdapter.getData()
							.get(position));
					break;
				}
			}
			checkBox.setChecked(true);
			ArrayList<ContactAttribute> contactAttributes = new ArrayList<ContactAttribute>();
			contactAttributes.add(contactAttribute);
			getChooseContactListener().addContacts(contactAttributes);
			refreshListView();
		}
	}

	/**
	 * 
	 * method name: setParentCheckboxState function @Description: TODO
	 * Parameters and return values description:
	 * 
	 * @param contactAttribute
	 * @param isRemove
	 *            field_name void return type
	 * @History memory：
	 * @Date：2016-1-20 下午7:46:42 @Modified by：zhangjx
	 * @Description:同步父级部门checkbox状态
	 */
	private void setParentCheckboxState(ContactAttribute contactAttribute,
			boolean isRemove,int position) {
		// 判断是否是最后一级+是否该部门下有成员
		if (selectedGroupStr != null && contactAttribute.getParent() != null) {
			if (contactAttribute.getParent().getChildDepCount() == 0) {
				if (!isRemove) {
					int chooseCount =0;
					// 计算已选的联系人中有多少个是同一个部门的
					for (ContactAttribute contact : mSelectedContacts) {
						if (contact.getParent()!=null) {
							if (contact.getParent().getName()
									.equals(contactAttribute.getParent().getName())) {
								chooseCount++;
							}
						}
					}
					// 如果已选中的联系人数=该部门下的所有联系人总数，选中部门
					if (contactAttribute.getParent().getTotalCount() == chooseCount) {
						selectedGroupStr.add(contactAttribute.getParent()
								.getName().toString());
					}
				} else {
					selectedGroupStr.remove(contactAttribute.getParent()
							.getName().toString());
				}
				mEisAdapter.setSelectedGroupStr(selectedGroupStr);
			}
		}

	}

	protected void refreshListView() {
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
		if (mEisAdapter != null) {
			mEisAdapter.notifyDataSetChanged();
		}
		mCommonAdapter.notifyDataSetChanged();
	}

	/**
	 * 
	 * method name: addGroupMember function @Description: TODO Parameters and
	 * return values description:
	 * 
	 * @param view
	 * @param position
	 * @param isPersionAdapter
	 *            eis列表匹配后去掉部门，显示成员用mAdapter来展示 field_name void return type
	 * @History memory：
	 * @Date：2015-11-13 下午5:22:09 @Modified by：zhangjx
	 * @Description:
	 */
	protected void addGroupMember(View view, int position,boolean isPersionAdapter) {
		ContactAttribute contactAttribute = null;
		if ((((HeaderViewListAdapter) mListView.getAdapter())
				.getWrappedAdapter() instanceof SearchContactsEmailAdapter)) {
			String email = mSearchContactsEmailAdapter.getItem(position)
					.toString();
			if (getChooseContactListener().isContactChoosed(email)) {
				getChooseContactListener().reomveContacts(email);
			} else {
				String userName = email.substring(0, email.indexOf("@"));
				if (!StringUtil.isEmailAllValid(email)) {
					Toast toast = Toast.makeText(getActivity(),
							R.string.message_compose_error_wrong_recipients,
							Toast.LENGTH_LONG);
					toast.show();
					return;
				}
				contactAttribute = new ContactAttribute();
				contactAttribute.setEmail(email);
				contactAttribute.setNickName(userName);
				ArrayList<ContactAttribute> contactAttributes = new ArrayList<ContactAttribute>();
				contactAttributes.add(contactAttribute);
				getChooseContactListener().addContacts(contactAttributes);
			}
			if (isEisList()) {
				mListView.setAdapter(mEisAdapter);
			} else {
				mListView.setAdapter(mAdapter);
			}
			return;
		}
		CheckBox checkBox ;
		if (isEisList()&&!isPersionAdapter) {
			checkBox = (CheckBox) view
					.findViewById(R.id.checkbox_child);
			contactAttribute = (ContactAttribute) mEisAdapter.getItem(position);
		} else {
			checkBox = (CheckBox) view
					.findViewById(R.id.muliple_select_user_item_cb);
			contactAttribute = (ContactAttribute) mAdapter.getItem(position);
		}
		if (checkBox==null) {
			return;
		}
		// 判断所有联系人选择框内是否存在此联系人。如果存在。那么点击则取消。否则添加
		if (checkBox.isChecked()) {
			// 联系人列表中已经是群成员的默认勾选，不可取消勾选
			String email;
			if (!isEisList()&&isPersionAdapter) {
			email = mAdapter.getAllContactsInList().get(position).getEmail();
			}else {
				email = mEisAdapter.getmAllTreeBeens().get(position).getEmail();
			}
			if (mMembers != null && mMembers.size() > 0) {
				for (CGroupMember member : mMembers) {
					if (member.getEmail().equals(email)) {
						return;
					}
				}
			}
			if (!isEisList()&&isPersionAdapter) {
				mSelectedContacts.remove(mAdapter.getAllContactsInList().get(
						position));
			} else {
				mSelectedContacts.remove(mEisAdapter.getmAllTreeBeens().get(
						position));
			}
			checkBox.setChecked(false);
			// 回调删除输入框中的联系人
			getChooseContactListener().reomveContacts(
					contactAttribute.getEmail());
			if (!isEisList()&&isPersionAdapter) {
				mSelectedContacts.remove(mAdapter.getAllContactsInList().get(
						position));
			}else {
				mSelectedContacts
				.remove(mEisAdapter.getmAllTreeBeens().get(
						position));
			}
			checkBox.setChecked(false);
			getChooseContactListener().reomveContacts(
					contactAttribute.getEmail());
		} else {
			if (!isEisList()&&isPersionAdapter) {
				mSelectedContacts
				.add(mAdapter.getAllContactsInList().get(position));
			}else {
				mSelectedContacts
				.add(mEisAdapter.getmAllTreeBeens().get(
						position));
			}

			checkBox.setChecked(true);
			ArrayList<ContactAttribute> contactAttributes = new ArrayList<ContactAttribute>();
			contactAttributes.add(contactAttribute);
			getChooseContactListener().addContacts(contactAttributes);
		}
		refreshListView();
	}
	public Contact35EisAdapter getmEisAdapter() {
		if (mEisAdapter==null) {
			return getContact35EisAdapter();
		}
		return mEisAdapter;
	}

	public void setmEisAdapter(Contact35EisAdapter mEisAdapter) {
		this.mEisAdapter = mEisAdapter;
	}

	public ContactsListAdapter getmAdapter() {
		return mAdapter;
	}

	public void setmAdapter(ContactsListAdapter mAdapter) {
		this.mAdapter = mAdapter;
	}

	public CommonContactsAdapter getmCommonAdapter() {
		return mCommonAdapter;
	}

	public void setmCommonAdapter(CommonContactsAdapter mCommonAdapter) {
		this.mCommonAdapter = mCommonAdapter;
	}

	public Set<ContactAttribute> getSelectedContacts() {
		return mSelectedContacts;
	}

	public void setSelectedContacts(Set<ContactAttribute> selectedContacts) {
		this.mSelectedContacts = selectedContacts;
	}

	public SearchContactsEmailAdapter getmSearchContactsEmailAdapter() {
		return mSearchContactsEmailAdapter;
	}

	public void setmSearchContactsEmailAdapter(
			SearchContactsEmailAdapter mSearchContactsEmailAdapter) {
		this.mSearchContactsEmailAdapter = mSearchContactsEmailAdapter;
	}

	/**
	 * 
	 * method name: getCommonContactsAdapter function @Description: TODO
	 * Parameters and return values description:
	 * 
	 * @return field_name CommonContactsAdapter return type
	 * @History memory：
	 * @Date：2015-9-18 下午3:14:20 @Modified by：zhangjx
	 * @Description:常用联系人adapter
	 */
	protected abstract CommonContactsAdapter getCommonContactsAdapter();

	/**
	 * 
	 * method name: getContactsPersionAdapter function @Description: TODO
	 * Parameters and return values description:
	 * 
	 * @return field_name ContactsPersionAdapter return type
	 * @History memory：
	 * @Date：2015-9-18 下午3:38:14 @Modified by：zhangjx
	 * @Description:获取联系人adapter
	 */
	protected abstract ContactsListAdapter getContactsPersionAdapter();

	/**
	 * 
	 * method name: getContact35EisAdapter function @Description: TODO
	 * Parameters and return values description:
	 * 
	 * @return field_name ContactsPersionAdapter return type
	 * @History memory：
	 * @Date：2015-9-18 下午7:44:29 @Modified by：zhangjx
	 * @Description:获取eis列表联系人adapter
	 */
	protected abstract Contact35EisAdapter getContact35EisAdapter();

	/**
	 * 
	 * method name: loadDataForListView function @Description: TODO Parameters
	 * and return values description: field_name void return type
	 * 
	 * @History memory：
	 * @Date：2015-9-18 下午6:36:42 @Modified by：zhangjx
	 * @Description:加载联系人列表数据
	 */
	protected abstract void loadDataForListView();

	/**
	 * 
	 * method name: loadCommDataForListView function @Description: TODO
	 * Parameters and return values description: field_name void return type
	 * 
	 * @History memory：
	 * @Date：2015-9-18 下午6:37:01 @Modified by：zhangjx
	 * @Description:加载常用联系人数据
	 */
	protected abstract void loadCommDataForListView();

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (mEisAdapter != null) {
			mEisAdapter = null;
		}
	}
}
