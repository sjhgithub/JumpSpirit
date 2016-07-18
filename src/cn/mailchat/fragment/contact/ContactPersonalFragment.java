package cn.mailchat.fragment.contact;

import java.util.List;

import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.model.UserInfo;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import cn.mailchat.Account;
import cn.mailchat.R;
import cn.mailchat.activity.AddOrEditContactActivity;
import cn.mailchat.activity.ContactInfoActivity;
import cn.mailchat.adapter.CommonContactsAdapter;
import cn.mailchat.adapter.Contact35EisAdapter;
import cn.mailchat.adapter.ContactsListAdapter;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;

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
 * @Description :个人联系人
 */
public class ContactPersonalFragment extends BaseContactFragment {

	protected static final String TAG = ContactPersonalFragment.class
			.getSimpleName();
	private MessagingController mController;
	// 获取头像和昵称信息只请求一次
	private boolean isFirstReflash = true;
	private List<ContactAttribute> mPersonalContact;
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

		@Override
		public void loadPersonalContactForViewStarted(Account account) {
			if (account.getEmail().equals(mAccount.getEmail())) {
				if (isAdded() && isShowLoadingView&&!isShowCheckbox) {
					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							setSwipeRefreshLoadingState();
							setProgressView(LOADING);
						}
					});
				}
			}
		}

		@Override
		public void loadPersonalContactForViewFinished(final Account account,
				final List<ContactAttribute> personalContact) {
			//供下来使用的数据
			mPersonalContact = personalContact;
			if (isAdded()) {
				if (account.getEmail().equals(mAccount.getEmail())
						&& personalContact != null&&!isShowCheckbox) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							try {
								mAdapter.setContacts(null, personalContact);
								setProgressView(SHOW_CONTENT);
								setSwipeRefreshLoadedState();
								if (isFirstReflash) {
									isFirstReflash = false;
									syncUserInfo(account, mPersonalContact);
								}
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}
						}
					});
				}
			}
		}

		@Override
		public void loadPersonalContactForViewFailed(Account account) {
			if (account.getEmail().equals(mAccount.getEmail())&&!isShowCheckbox) {
				if (isAdded()) {
					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							setProgressView(ERROR);
							setSwipeRefreshLoadedState();
						}
					});
				}
			}
		}

		@Override
		public void addContactFinish(Account acc,
				List<ContactAttribute> contactList) {
			if (mAccount != null && acc != null) {
				if (mAccount.getUuid().equals(acc.getUuid())&&!isShowCheckbox) {
					if (isAdded()) {
						isShowLoadingView = false;
						loadContacts(mAccount);
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setSwipeRefreshLoadedState();
							}
						});
						// 只获取新加入联系人的头像和昵称信息
						if (contactList != null) {
							if (contactList.size() > 0) {
								syncUserInfo(acc, contactList);
							} else {
								isFirstReflash = true;
							}
						}
						if (!isInviteChat) {
							loadCommContacts(mAccount);
						}
					}
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
			if (account.getEmail().equals(mAccount.getEmail())&&isShowCheckbox&&currFragmentTag.equals("ContactPersonalFragment")) {
				if (isAdded()) {
					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							if (!isInviteChat&&commonContacts!=null) {
								mCommonAdapter.setData(commonContacts);
							}
//							mAdapter.setContacts(null, personalContacts);
							showAutoCompleteListView(mKeyWord,commonContacts,
									eisContacts,personalContacts,sameDomainContacts);
//							setSwipeRefreshLoadedState();
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
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		if (!isShowCheckbox) {
			if (mListView.getHeaderViewsCount() > 0) {
				position = position - mListView.getHeaderViewsCount();
			}
			// 跳转联系人详情
			ContactInfoActivity.actionView(getActivity(), mAccount, mAdapter
					.getAllContactsInList().get(position));
		} else if (isAddGroupMembers | isComposeMail) {
			if (mListView.getHeaderViewsCount() > 0) {
				position = position - mListView.getHeaderViewsCount();
			}
			 addGroupMember( view,position,true);
		} else {
			choseContact(parent, null, view, position);
		}

	}

	@Override
	public void onItemClick(cn.mailchat.view.hlistview.AdapterView<?> parent,
			View view, int position, long id) {
		if (!isShowCheckbox) {
			// 跳转联系人详情
			ContactInfoActivity.actionView(getActivity(), mAccount,
					mCommonAdapter.getData().get(position));
		} else {
			choseContact(null, parent, view, position);
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.errorStateButton:
			isShowLoadingView = true;
			loadContacts(mAccount);
			break;
		default:
			break;
		}
	}

	@Override
	protected CommonContactsAdapter getCommonContactsAdapter() {
		return new CommonContactsAdapter(getActivity(), isShowCheckbox,
				mSelectedContacts, isShowCheckbox?getChooseContactListener().getSearchContactsEditView():null,mMembers);
	}

	@Override
	protected ContactsListAdapter getContactsPersionAdapter() {
		return new ContactsListAdapter(getActivity(), mSelectedContacts,  isShowCheckbox?getChooseContactListener().getSearchContactsEditView():null);
	}

	@Override
	protected Contact35EisAdapter getContact35EisAdapter() {
		return null;
	}

	@Override
	protected void loadDataForListView() {
		if (!isShowCheckbox) {
			loadContacts(mAccount);
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
		// 同步联系人信息
		if (mPersonalContact != null & mPersonalContact.size() > 0&&!isSearchView) {
			syncUserInfo(mAccount, mPersonalContact);
		}else{
			setSwipeRefreshLoadedState();
		}
	}

	protected void loadCommContacts(Account account) {
		mController.loadCommonContactForView(account, listener);
	}

	protected void loadContacts(Account account) {
		mController.listLocalPersonalContactForView(mAccount,true,false, listener);
	}

	private void syncUserInfo(Account account,
			List<ContactAttribute> contactList) {
		mController.syncRemoteUserInfo(account, contactList, listener);
	}

	public void doSearch(String s) {
		if (isShowCheckbox||isSearchView) {
			isFirstLoadAllData=false;
			mController.searchContact(mAccount, s,isInviteChat,isFirstLoadAllData,"ContactPersonalFragment",listener);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("ContactPersonalFragment");
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("ContactPersonalFragment");
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
