package cn.mailchat.adapter;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import cn.mailchat.view.pager.SlidingTabPagerAdapter;
import cn.mailchat.view.pager.base.BaseTabFragment;
import cn.mailchat.activity.ForwardActivity;
import cn.mailchat.fragment.ForwardTabInfo;
import cn.mailchat.fragment.ForwardViewPagerItemFragment;

public class ForwordViewPagerAdapter extends SlidingTabPagerAdapter {

	public ForwordViewPagerAdapter(FragmentManager mgr,
			Context context,
			ViewPager vp,
			String accountUid,
			Serializable message,
			Parcelable singleAttachment) {
		super(mgr, ForwardTabInfo.values().length, context
				.getApplicationContext(), vp);
		// TODO Auto-generated constructor stub
		ForwardTabInfo[] values = ForwardTabInfo.values();
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
			if (!tabFragment.isAdded()) {
				Bundle args = new Bundle();
				args.putInt(ForwardViewPagerItemFragment.BUNDLE_KEY_CATALOG,
						values[i].getCatalog());
				args.putString(ForwardActivity.ACCOUNTUUID, accountUid);
				args.putSerializable(ForwardActivity.MESSAGE, message);
				args.putParcelable(ForwardActivity.SINGLE_ATTACHMENT, singleAttachment);
				tabFragment.setArguments(args);
			}
			fragments[values[i].getIdx()] = tabFragment;
		}
	}

	public final int getCacheCount() {
		return 2;
	}

	public final int getCount() {
		return ForwardTabInfo.values().length;
	}

	public final CharSequence getPageTitle(int i) {
		ForwardTabInfo tab = ForwardTabInfo.getTabByIdx(i);
		int resId = 0;
		CharSequence title = "";
		if (tab != null)
			resId = tab.getTitle();
		if (resId != 0)
			title = context.getText(resId);
		return title;
	}

}
