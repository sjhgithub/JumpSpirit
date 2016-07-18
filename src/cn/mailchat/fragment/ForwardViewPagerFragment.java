package cn.mailchat.fragment;

import java.io.Serializable;

import cn.mailchat.R;
import cn.mailchat.activity.ForwardActivity;
import cn.mailchat.adapter.ForwordViewPagerAdapter;
import cn.mailchat.view.pager.PagerSlidingTabStrip;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ForwardViewPagerFragment extends Fragment implements
		OnPageChangeListener {
	private PagerSlidingTabStrip mTabStrip;
	private ViewPager mViewPager;
	private ForwordViewPagerAdapter mAdapter;
	private String mAccountUid;
	private Serializable mMessage;
	private Parcelable mSingleAttachment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			mAccountUid = args.getString(ForwardActivity.ACCOUNTUUID);
			mMessage = args.getSerializable(ForwardActivity.MESSAGE);
			mSingleAttachment = args.getParcelable(ForwardActivity.SINGLE_ATTACHMENT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_viewpager, container,
				false);
		mTabStrip = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
		mViewPager = (ViewPager) view.findViewById(R.id.main_tab_pager);

		if (mAdapter == null) {
			mAdapter = new ForwordViewPagerAdapter(
					getChildFragmentManager(),
					getActivity(),
					mViewPager,
					mAccountUid,
					mMessage,
					mSingleAttachment);
		}
		mViewPager.setOffscreenPageLimit(mAdapter.getCacheCount());
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOnPageChangeListener(this);
		mTabStrip.setViewPager(mViewPager);

		return view;
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		mTabStrip.onPageScrollStateChanged(arg0);
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		mTabStrip.onPageScrolled(arg0, arg1, arg2);
		mAdapter.onPageScrolled(arg0);
	}

	@Override
	public void onPageSelected(int arg0) {
		mTabStrip.onPageSelected(arg0);
		mAdapter.onPageSelected(arg0);
	}
}
