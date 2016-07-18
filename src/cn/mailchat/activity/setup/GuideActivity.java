package cn.mailchat.activity.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.Accounts;
import cn.mailchat.adapter.ViewPagerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class GuideActivity extends Activity{
	private ViewPager pager;
	private GuideViewPagerAdapter adpter;
	private List<View> pageViews;
	private int[] src;

	public static void  actionGuide(Context context){
		Intent i = new Intent(context,GuideActivity.class);
		context.startActivity(i);
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		initView();
		initData();
		savePrefsParameter();
	}

	private void initView(){
		pager = (ViewPager) findViewById(R.id.guide_pager);
	}

	private void initData(){
		String language = MailChat.application.getLanguage();
//		if(language.equals(Locale.SIMPLIFIED_CHINESE.toString())){
			src = new int []{
			        R.drawable.guide_1,
			        R.drawable.guide_2,
			        R.drawable.guide_3
//			        , R.drawable.guide_4
			};
//		}else if(language.equals(Locale.TAIWAN.toString())){
//			src =new int []{R.drawable.guide_tw_1,R.drawable.guide_tw_2,R.drawable.guide_tw_3,R.drawable.guide_tw_4};
//		}else{
//			src =new int []{R.drawable.guide_en_1,R.drawable.guide_en_2,R.drawable.guide_en_3,R.drawable.guide_en_4};
//		}
		LayoutInflater inflater = getLayoutInflater();
		View view1 = inflater.inflate(R.layout.item_guide, null);
		View view2 = inflater.inflate(R.layout.item_guide, null);
		View view3 = inflater.inflate(R.layout.item_guide, null);
//		View view4 = inflater.inflate(R.layout.item_guide, null);
		pageViews = new ArrayList<View>();
		pageViews.add(view1);
		pageViews.add(view2);
		pageViews.add(view3);
//		pageViews.add(view4);
		adpter = new GuideViewPagerAdapter(pageViews);
		pager.setAdapter(adpter);
		pager.setOnPageChangeListener(new OnPageChangeListener() {
			int mScrollState;
			boolean canOnEdge = true;
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				if (mScrollState == 1 && arg0 == pageViews.size() - 1) {
					if (canOnEdge) {
						canOnEdge = false;
						Accounts.showAccounts(GuideActivity.this);
						finish();
					}
				}
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				mScrollState = arg0;
			}
		});
	}
	
	class GuideViewPagerAdapter extends ViewPagerAdapter{

		public List<View> mListViews;

		public GuideViewPagerAdapter(List<View> pageViews) {
			super(pageViews);
			mListViews = pageViews;
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(View arg0, int position) {
			View view = mListViews.get(position);
			((ViewPager) arg0).addView(view);
			ImageView iv = (ImageView) view.findViewById(R.id.iv_guide_show);
			iv.setBackgroundResource((src[position]));
//			if(position == 3){
//				TextView tv = (TextView) view.findViewById(R.id.tv_guide_bt);
//				tv.setVisibility(View.VISIBLE);
//				tv.setOnClickListener(new OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						Accounts.showAccounts(GuideActivity.this);
//						finish();
//					}
//				});
//			}
			return view;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mListViews.get(arg1));
		}
	}
	
	/**
	 * 保存控制参数
	 */
	private void savePrefsParameter() {
		SharedPreferences preferences = Preferences.getPreferences(this).getPreferences();
		MailChat.setGuideVersionCode(GlobalConstants.guideVersionCode);
		Editor editor = preferences.edit();
		MailChat.save(editor);
		editor.commit();
	}
}
