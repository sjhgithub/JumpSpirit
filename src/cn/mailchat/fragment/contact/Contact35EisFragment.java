package cn.mailchat.fragment.contact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.umeng.analytics.MobclickAgent;

import android.R.integer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.R;
import cn.mailchat.activity.ContactInfoActivity;
import cn.mailchat.adapter.CommonContactsAdapter;
import cn.mailchat.adapter.Contact35EisAdapter;
import cn.mailchat.adapter.ContactsListAdapter;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.mail.store.Columns.TbContactRemark;

/**
 * 
 * @copyright © 35.com
 * @file name ：Contact35EisFragment.java
 * @author ：zhangjx
 * @create Data ：2015-8-19下午2:25:28
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2015-8-19下午2:25:28
 * @Modified by：zhangjx
 * @Description :35企業通訊錄
 */
public class Contact35EisFragment extends BaseContactFragment {

	protected static final String TAG = Contact35EisFragment.class
			.getSimpleName();
	private MessagingController mController;
	private boolean isSwipeRefreshLoading = false;
	private MessagingListener listener = new MessagingListener() {

		@Override
		public void loadCommonContactForViewStarted(Account account) {

		}

		@Override
		public void loadCommonContactForViewFinished(Account account,
				final List<ContactAttribute> commonContacts) {
			if (account.getEmail().equals(mAccount.getEmail())&&!isInviteChat) {
				if (isAdded()) {
					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							mCommonAdapter.setData(commonContacts);
						}
					});
				}
			}
		}

		@Override
		public void loadCommonContactForViewFailed(Account account) {
		}

		public void list35EisStart(Account account) {
//			if (account.getEmail().equals(mAccount.getEmail())) {
//				if (isAdded()) {
//					getActivity().runOnUiThread(new Runnable() {
//
//						@Override
//						public void run() {
//							if (isShowLoadingView && !isSwipeRefreshLoading) {
//								setProgressView(LOADING);
//							}
//						}
//					});
//				}
//			}
		}

		public void list35EisSuccess(Account account,
				final List<ContactAttribute> result) {
			if (account.getEmail().equals(mAccount.getEmail())
					&& result != null&&!isShowCheckbox) {
				if (isAdded()) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							try {
								if (isShowLoadingView && !isSwipeRefreshLoading) {
									setProgressView(SHOW_CONTENT);
								}
								if (mEisAdapter != null){
									mEisAdapter.setAllTreeBeens(result);
								}
								if (isSwipeRefreshLoading) {
									isSwipeRefreshLoading = false;
									setSwipeRefreshLoadedState();
								}
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
						}
					});
				}
			}
		}

		public void list35EisFailed(Account account) {
			if (account.getEmail().equals(mAccount.getEmail())&&!isShowCheckbox) {
				if (isAdded()) {
					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							setProgressView(ERROR);
						}
					});
				}
			}
		}

		@Override
		public void addContactFinish(Account acc,
				List<ContactAttribute> contactList) {
			if (mAccount != null && acc != null) {
				if (mAccount.getUuid().equals(acc.getUuid())&&!isShowCheckbox&&!isInviteChat) {
						loadCommContacts(mAccount);
				}
			}
		}

		// 搜索联系人
		@Override
		public void searchContactStart(Account account) {
		}

		@Override
		public void searchContactSuccess(Account account,String currFragmentTag,
				final List<ContactAttribute> eisContacts,
				final List<ContactAttribute> commonContacts,
				final List<ContactAttribute> personalContacts,
				final List<ContactAttribute> sameDomainContacts) {
			if (account.getEmail().equals(mAccount.getEmail())&&isShowCheckbox&&currFragmentTag.equals("Contact35EisFragment")) {
				if (isAdded()) {
					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							setProgressView(SHOW_CONTENT);
							if (!isInviteChat&&commonContacts!=null) {
							mCommonAdapter.setData(commonContacts);
							}
							showAutoCompleteListView(mKeyWord,commonContacts,
											eisContacts,personalContacts,sameDomainContacts);
						}
					});
				}
			}
		}

		@Override
		public void searchContactFailed(Account account) {
		}
	};
	private boolean isFirstLoadAllData=true;

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_35eis_tab;
	}

	@Override
	protected boolean isEisList() {
		return true;
	}

	@Override
	public void onItemClick(cn.mailchat.view.hlistview.AdapterView<?> parent,
			View view, int position, long id) {
		if (!isShowCheckbox) {
			// 跳转联系人详情
			ContactInfoActivity.actionView(getActivity(), mAccount,
					mCommonAdapter.getData().get(position));
//		} else if (isAddGroupMembers | isComposeMail) {
//			if (mListView.getHeaderViewsCount() > 0) {
//				position = position - mListView.getHeaderViewsCount();
//			}
//			 addGroupMember( view,position);
		}else {
			choseContact(null, parent, view, position);
		}

	}

	/**
	 * 
	 * method name: onClick
	 * 
	 * @see cn.mailchat.adapter.Contact35EisAdapter.OnTreeClickListener#onClick(cn.mailchat.beans.TreeBeen,
	 *      int) function@Description: TODO
	 * @History memory:
	 * @Date：2015-8-26 下午5:54:43 @Modified by：zhangjx
	 * @Description：item点击事件
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view,
			ContactAttribute treeBeen, int position) {
		if (!isShowCheckbox) {
			// 跳转联系人详情
			jumpToContactDetial(treeBeen);
		}else if (isAddGroupMembers | isComposeMail) {
			 addGroupMember( view,position,false);
		} 
		else {
			seleteContact(parent, view, treeBeen, position);
		}
	}
	/**
	 * 
	 * method name: seleteContact function @Description: TODO Parameters and
	 * return values description:
	 * 
	 * @param parent
	 * @param view
	 * @param contactAttribute
	 * @param position
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-24 下午5:41:51 @Modified by：zhangjx
	 * @Description:由于eis的adapter和其他的联系人列表的不一样，所以这里需要重写seleteContact()
	 */
	private void seleteContact(AdapterView<?> parent, View view,
			ContactAttribute contactAttribute, int position) {
		CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox_child);
		// 如果是搜索的话isShowCheckbox=false
		if (!isShowCheckbox) {
			getChooseContactListener().showChoosedToast(
					contactAttribute.getEmail());
			changeContactCheckBoxStatus(checkBox, position, contactAttribute,
					parent, null);

		} else {
			changeContactCheckBoxStatus(checkBox, position, contactAttribute,
					parent, null);
		}
		getChooseContactListener().changeEditViewHeightAutoToContent();
	}

	private void jumpToContactDetial(ContactAttribute treeBeen) {
		ContactAttribute contact = new ContactAttribute();
		contact.setEmail(treeBeen.getEmail());
		contact.setNickName(treeBeen.getName());
		contact.setCompany(mAccount.getCompanyName());
		contact.setDepartment(treeBeen.getDepartment());
		contact.setPosition(treeBeen.getPosition());
		contact.setAddr(treeBeen.getAddr());
		contact.setPhones(treeBeen.getPhones());
		contact.setImgHeadHash(treeBeen.getImgHeadUrl());
		String imgUrl = (StringUtils.isNullOrEmpty(treeBeen.getImgHeadUrl())) ? "" : treeBeen.getImgHeadUrl();
		contact.setImgHeadPath(treeBeen.getImgHeadUrl().equals("null") ? ""
				: GlobalConstants.HOST_IMG + imgUrl);
//		String imgHeadPath=treeBeen.getImgHeadPath();
//		if (!StringUtils.isNullOrEmpty(imgHeadPath)) {
//			if (imgHeadPath.startsWith("http")) {
//				contact.setImgHeadPath(imgHeadPath);
//			} else if (!TextUtils.isEmpty(imgHeadPath)) {
//				contact.setImgHeadPath(GlobalConstants.HOST_IMG
//						+ imgHeadPath);
//			}
//		}
		contact.setOtherRemarks(treeBeen.getOtherRemarks());
		contact.setEisContact(true);
		contact.setrEmail(treeBeen.getrEmail());
		contact.setrName(treeBeen.getrName());
		contact.setrNickName(treeBeen.getrNickName());
		contact.setrImgHeadHash(treeBeen.getrImgHeadHash());
		contact.setrOtherRemarks(treeBeen.getrOtherRemarks());
		contact.setrPosition(treeBeen.getrPosition());
		contact.setrPhones(treeBeen.getrPhones());
		contact.setrAddr(treeBeen.getrAddr());
		contact.setrDepartment(treeBeen.getrDepartment());
		contact.setrCompany(treeBeen.getrCompany());
		ContactInfoActivity.actionView(getActivity(), mAccount, contact);
	}

	@Override
	public void onGroupClick(AdapterView<?> parent, View view,
			ContactAttribute treeBeen) {
		if (!isShowCheckbox) {
			//记录组是否展开
			mController.updateEisListExpandState(mAccount, treeBeen.getId(),
					treeBeen.isExpand());
		}
	}

	@Override
	public void onCheckedGroup(View v, ContactAttribute childContactAttribute,
			int position) {
		changeContactCheckBoxStatus(v, childContactAttribute, position);
	}

	/**
	 * 
	 * method name: changeContactCheckBoxStatus function @Description: TODO
	 * Parameters and return values description:
	 * 
	 * @param checkBox
	 * @param child
	 *            field_name void return type
	 * @History memory：
	 * @Date：2016-1-20 下午5:20:21 @Modified by：zhangjx
	 * @Description:父部门全选
	 */
	private void changeContactCheckBoxStatus(View view, ContactAttribute child,int position) {
		if (child != null) {
			List<ContactAttribute> childList = child.getChildList();
			if (childList != null && childList.size() > 0) {
				CheckBox checkBox = (CheckBox) view
						.findViewById(R.id.checkbox_group);
				// checkbox已是选中状态
				if (checkBox.isChecked()) {
					for (ContactAttribute contact : childList) {
						mSelectedContacts.remove(contact);
						// 回调删除输入框中的联系人
						getChooseContactListener().reomveContacts(
								contact.getEmail());
					}
					selectedGroupStr.remove(child.getName().toString());
					checkBox.setChecked(false);
				} else {
					for (ContactAttribute contact : childList) {
						mSelectedContacts.add(contact);
					}
					getChooseContactListener().addContacts(childList);
					selectedGroupStr.add(child.getName().toString());
					checkBox.setChecked(true);
				}
				mEisAdapter.setSelectedGroupStr(selectedGroupStr);

			}
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.errorStateButton:
			load35EisContacts(mAccount, true);
			break;

		default:
			break;
		}
	}

	@Override
	protected CommonContactsAdapter getCommonContactsAdapter() {
		return new CommonContactsAdapter(getActivity(), isShowCheckbox,
				mSelectedContacts,isShowCheckbox?getChooseContactListener().getSearchContactsEditView():null,mMembers);
	}

	@Override
	protected Contact35EisAdapter getContact35EisAdapter() {
		return new Contact35EisAdapter(getActivity(), mListView,
				mSelectedContacts,selectedGroupStr, isShowCheckbox, 10,
				isShowCheckbox?getChooseContactListener().getSearchContactsEditView():null,
				mMembers);
	}

	@Override
	protected ContactsListAdapter getContactsPersionAdapter() {
		return new ContactsListAdapter(getActivity(), mSelectedContacts,null);
	}

	@Override
	protected void loadDataForListView() {
		if (!isShowCheckbox) {
			load35EisContacts(mAccount, false);
		}else {
			doSearch("");
		}
	}

	@Override
	protected void loadCommDataForListView() {
		if (mController == null) {
			mController = MessagingController.getInstance(getActivity()
					.getApplication());
			mController.addListener(listener);
		}
		if (!isInviteChat) {
			loadCommContacts(mAccount);
		}
	}

	@Override
	public void onRefresh() {
		super.onRefresh();
		if (mController == null) {
			mController = MessagingController.getInstance(getActivity()
					.getApplication());
			mController.addListener(listener);
		}
		isSwipeRefreshLoading = true;
		// 同步联系人信息
		if (!isSearchView) {
			load35EisContacts(mAccount, true);
		}
	}

	protected void load35EisContacts(Account account, boolean syncRightNow) {
		if (isShowLoadingView && !isSwipeRefreshLoading) {
			setProgressView(LOADING);
		}
		mController.list35Eis(mAccount, syncRightNow,isInviteChat, listener);
	}

	protected void loadCommContacts(Account account) {
		mController.loadCommonContactForView(account, listener);
	}

	public void doSearch(String s) {
		if (s.equals("")&&isFirstLoadAllData) {
			setProgressView(LOADING);
		}
		mKeyWord=s;
		if (isShowCheckbox||isSearchView) {
			isShowLoadingView = false;
			isFirstLoadAllData=false;
			mController.searchContact(mAccount, mKeyWord,isInviteChat,isFirstLoadAllData,"Contact35EisFragment",listener);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("Contact35EisFragment");
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("Contact35EisFragment");
	}
	@Override
	public void onDestroy() {
		isFirstLoadAllData=true;
		if (mController != null) {
			mController.removeListener(listener);
		}
		super.onDestroy();
	}
}
