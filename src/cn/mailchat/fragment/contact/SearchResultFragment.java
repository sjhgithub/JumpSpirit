package cn.mailchat.fragment.contact;

import java.util.HashSet;
import java.util.List;

import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.ContactInfoActivity;
import cn.mailchat.adapter.ContactsListAdapter;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.fragment.contact.ContactTabsFragment.ContactTabsFragmentListener;
import cn.mailchat.view.pager.PagerSlidingTabStrip;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class SearchResultFragment extends Fragment implements
		OnItemClickListener {
	private MessagingController mController;
	private ContactsListAdapter mAdapter;
	private ListView mListView;
	private SearchAllContactListener mSearchAllContactListener;
	private Account mAccount;

	private MessagingListener listener = new MessagingListener() {
		public void searchAllContactStart(Account account) {
			mSearchAllContactListener.doSearchStrart();
		}

		public void searchAllContactSuccess(Account account,
				final List<ContactAttribute> contacts) {
			if (account.getEmail().equals(mAccount.getEmail())) {
				if (isAdded()) {
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							mAdapter.setContacts(null, contacts);
							mSearchAllContactListener.doSearchFinish(contacts
									.size());
							if (contacts.size() == 0) {
								mListView.setVisibility(View.GONE);
								tvNoResult.setVisibility(View.VISIBLE);
							} else {
								mListView.setVisibility(View.VISIBLE);
								tvNoResult.setVisibility(View.GONE);
							}
						}
					});
				}
			}
		}

		public void searchAllContactFailed(Account account) {
			mSearchAllContactListener.doSearchFailed();
		}
	};
	private TextView tvNoResult;

	public static SearchResultFragment newInstance() {
		SearchResultFragment fragment = new SearchResultFragment();
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mSearchAllContactListener = (SearchAllContactListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.getClass()
					+ " must implement SearchAllContactListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_list, container, false);
		initViews(view);
		initData();
		initListener();
		return view;

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mAdapter.getItem(position).isEisContact()) {
			jumpToContactDetial(mAdapter.getItem(position));
		} else {
			// 跳转联系人详情
			ContactInfoActivity.actionView(getActivity(), mAccount,
					mAdapter.getItem(position));
		}
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
		String imgUrl = (treeBeen.getImgHeadUrl().equals("null") || treeBeen
				.getImgHeadUrl() == null) ? "" : treeBeen.getImgHeadUrl();
		contact.setImgHeadPath(TextUtils.isEmpty(imgUrl) ? ""
				: GlobalConstants.HOST_IMG + imgUrl);
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

	private void initListener() {
		mListView.setOnItemClickListener(this);
	}

	private void initData() {
		mController = MessagingController.getInstance(getActivity()
				.getApplication());
		mAccount = Preferences.getPreferences(getActivity().getApplication())
				.getDefaultAccount();
		mAdapter = new ContactsListAdapter(getActivity(),
				new HashSet<ContactAttribute>(), null);
		mListView.setAdapter(mAdapter);

	}

	public void doSearch(String keyword) {
		mController.serchAllContacts(mAccount, keyword, false, listener);
	}

	private void initViews(View view) {
		mListView = (ListView) view.findViewById(R.id.list);
		tvNoResult = (TextView) view.findViewById(R.id.no_result);
	}

	@Override
	public void onResume() {
		if (mController != null) {
			mController.addListener(listener);
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		if (mController != null) {
			mController.removeListener(listener);
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
	}

	public interface SearchAllContactListener {
		void doSearchStrart();

		void doSearchFailed();

		void doSearchFinish(int size);
	}
}
