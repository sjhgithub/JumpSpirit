package cn.mailchat.adapter;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import cn.mailchat.activity.ChooseContactsActivity;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.fragment.contact.ChooseContactListener;
import cn.mailchat.fragment.contact.Contact35Tab;
import cn.mailchat.fragment.contact.ContactOtherTab;
import cn.mailchat.view.pager.SlidingTabPagerAdapter;
import cn.mailchat.view.pager.base.BaseTabFragment;

public final class ContactTabPagerAdapter extends SlidingTabPagerAdapter {
	ContactOtherTab[] values;
	protected ChooseContactListener mChooseContactListener;
	private Fragment currentFragment;

	public ContactTabPagerAdapter(FragmentManager mgr, Context context,
			ViewPager vp, ChooseContactListener chooseContactListener,
			String keyWord, List<CGroupMember> members, String groupName,
			boolean is35Domain, boolean isShowCheckbox,
			boolean isAddGroupMembers, boolean isComposeMail,
			boolean isSearchView, boolean isInviteChat) {
		super(mgr, Contact35Tab.values().length, context
				.getApplicationContext(), vp);
		this.mChooseContactListener = chooseContactListener;
		if (is35Domain) {
			Contact35Tab[] values = Contact35Tab.values();
			for (int i = 0; i < values.length; i++) {
				Fragment fragment = null;
				List<Fragment> list = mgr.getFragments();
				if (list != null) {
					Iterator<Fragment> iterator = list.iterator();
					while (iterator.hasNext()) {
						fragment = iterator.next();
						if (fragment.getClass() == values[i].getClz()) {
							break;
						}
					}
				}
				BaseTabFragment tabFragment = (BaseTabFragment) fragment;
				if (tabFragment == null)
					try {
						tabFragment = (BaseTabFragment) values[i].getClz()
								.newInstance();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				tabFragment.addTabChangeListener(this);
				tabFragment.setChooseContactListener(mChooseContactListener);
				if (!tabFragment.isAdded()) {
					Bundle args = new Bundle();
					args.putBoolean(
							ChooseContactsActivity.EXTRA_IS_SHOW_CHECK_BOX,
							isShowCheckbox);
					args.putBoolean(
							ChooseContactsActivity.EXTRA_IS_ADD_GROUP_MEMBER,
							isAddGroupMembers);
					args.putBoolean(
							ChooseContactsActivity.EXTRA_IS_COMPOSE_MAIL,
							isComposeMail);
					args.putSerializable(ChooseContactsActivity.EXTRA_MENBERS,
							(Serializable) members);
					args.putString(ChooseContactsActivity.EXTRA_GROUP_NAME,
							groupName);
					args.putBoolean(
							ChooseContactsActivity.EXTRA_IS_INVITE_CHAT,
							isInviteChat);
					// 搜索相关参数
					args.putString(ChooseContactsActivity.EXTRA_SEARCH_KEYWORD,
							keyWord);
					args.putBoolean(
							ChooseContactsActivity.EXTRA_IS_SEARCH_VIEW,
							isSearchView);
					tabFragment.setArguments(args);
				}
				fragments[values[i].getIdx()] = tabFragment;
			}
		} else {
			ContactOtherTab[] values = ContactOtherTab.values();
			for (int i = 0; i < values.length; i++) {
				Fragment fragment = null;
				List<Fragment> list = mgr.getFragments();
				if (list != null) {
					Iterator<Fragment> iterator = list.iterator();
					while (iterator.hasNext()) {
						fragment = iterator.next();
						if (fragment.getClass() == values[i].getClz()) {
							break;
						}
					}
				}
				BaseTabFragment tabFragment = (BaseTabFragment) fragment;
				if (tabFragment == null)
					try {
						tabFragment = (BaseTabFragment) values[i].getClz()
								.newInstance();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				tabFragment.addTabChangeListener(this);
				tabFragment.setChooseContactListener(mChooseContactListener);
				if (!tabFragment.isAdded()) {
					Bundle args = new Bundle();
					args.putBoolean(
							ChooseContactsActivity.EXTRA_IS_SHOW_CHECK_BOX,
							isShowCheckbox);
					args.putBoolean(
							ChooseContactsActivity.EXTRA_IS_ADD_GROUP_MEMBER,
							isAddGroupMembers);
					args.putBoolean(
							ChooseContactsActivity.EXTRA_IS_COMPOSE_MAIL,
							isComposeMail);
					args.putSerializable(ChooseContactsActivity.EXTRA_MENBERS,
							(Serializable) members);
					args.putString(ChooseContactsActivity.EXTRA_GROUP_NAME,
							groupName);
					args.putBoolean(
							ChooseContactsActivity.EXTRA_IS_INVITE_CHAT,
							isInviteChat);
					// 搜索相关参数
					args.putString(ChooseContactsActivity.EXTRA_SEARCH_KEYWORD,
							keyWord);
					args.putBoolean(
							ChooseContactsActivity.EXTRA_IS_SEARCH_VIEW,
							isSearchView);
					tabFragment.setArguments(args);
				}
				fragments[values[i].getIdx()] = tabFragment;
			}
		}
	}

	public final int getCacheCount() {
		return 2;
	}

	public final int getCount() {
		return Contact35Tab.values().length;
	}

	public final CharSequence getPageTitle(int i) {
		Contact35Tab tab = Contact35Tab.getTabByIdx(i);
		int resId = 0;
		CharSequence title = "";
		if (tab != null)
			resId = tab.getTitle();
		if (resId != 0)
			title = context.getText(resId);
		return title;
	}

	/**
	 * 
	 * method name: setPrimaryItem
	 * 
	 * @see android.support.v4.app.FragmentPagerAdapter#setPrimaryItem(android.view.ViewGroup,
	 *      int, java.lang.Object) function@Description: TODO
	 * @History memory:
	 * @Date：2015-10-8 下午3:53:20 @Modified by：zhangjx
	 * @Description：获取到当前的Fragment
	 */
	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		// if(currentFragment != (Fragment) object) {
		currentFragment = (Fragment) object;
		// }
		super.setPrimaryItem(container, position, object);
	}

	public Fragment getCurrFragment() {
		return currentFragment;
	}
}