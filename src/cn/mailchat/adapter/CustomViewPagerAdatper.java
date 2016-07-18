package cn.mailchat.adapter;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

/**
 * 
 * @copyright © 35.com
 * @file name ：CustomViewPagerAdatper.java
 * @author ：zhangjx
 * @create Data ：2014-9-17下午8:59:15
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-9-17下午8:59:15
 * @Modified by：zhangjx
 * @Description :填充ViewPager的数据适配器
 */
public class CustomViewPagerAdatper extends FragmentPagerAdapter {
	private static final String TAG = "CustomViewPagerAdatper";
	private SparseArray<Fragment> fragmentClz;
	private FragmentManager fragmentManager;
	private SparseArray<String> recorder;
	private boolean isReplaceOneFragment;

	public CustomViewPagerAdatper(FragmentManager fm,
			SparseArray<Fragment> fragmentClz) {
		super(fm);
		fragmentManager = fm;
		this.fragmentClz = fragmentClz;
	}

	@Override
	public Fragment getItem(int arg0) {
		Fragment fc = fragmentClz.get(arg0);
		return fc;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		super.destroyItem(container, position, object);
		if (!isReplaceOneFragment) {
			FragmentTransaction trans = fragmentManager.beginTransaction();
			trans.remove((Fragment) object);
			trans.commitAllowingStateLoss();
		}
	}

	@Override
	public int getCount() {
		return fragmentClz.size();
	}

	public Fragment findFragment(int index) {
		if (recorder == null) {
			return null;
		}
		return fragmentManager.findFragmentByTag(recorder.get(index));
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Object object = super.instantiateItem(container, position);
		if (object instanceof Fragment) {
			Fragment f = (Fragment) object;
			if (recorder == null) {
				recorder = new SparseArray<String>();
			}
			recorder.put(position, f.getTag());
		}
		return object;
	}

	@Override
	public Parcelable saveState() {
		return new FragmentIdRecorder(recorder);
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
		if (state instanceof FragmentIdRecorder) {
			FragmentIdRecorder fir = (FragmentIdRecorder) state;
			recorder = fir.getRecorder();
		}
	}

	public static class FragmentIdRecorder implements Parcelable {
		private SparseArray<String> recorder;

		public SparseArray<String> getRecorder() {
			return recorder;
		}

		public FragmentIdRecorder(SparseArray<String> recorder) {
			this.recorder = recorder;
		}

		public FragmentIdRecorder(Parcel in) {
			recorder = new SparseArray<String>();
			int size = in.readInt();
			if (size <= 0) {
				return;
			}
			for (int i = 0; i < size; i++) {
				recorder.put(in.readInt(), in.readString());
			}
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			if (recorder == null || recorder.size() <= 0) {
				dest.writeInt(0);
				return;
			} else {
				int size = recorder.size();
				dest.writeInt(size);
				for (int i = 0; i < size; i++) {
					dest.writeInt(recorder.keyAt(i));
					dest.writeString(recorder.valueAt(i));
				}
			}
		}

		public static final Parcelable.Creator<FragmentIdRecorder> CREATOR = new Parcelable.Creator<FragmentIdRecorder>() {
			public FragmentIdRecorder createFromParcel(Parcel in) {
				return new FragmentIdRecorder(in);
			}

			public FragmentIdRecorder[] newArray(int size) {
				return new FragmentIdRecorder[size];
			}
		};
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	/**
	 * 
	 * method name: replaceTabItems function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param index
	 *            第几个fragment
	 * @param removeFragment
	 *            要移除的fragment
	 * @param addFragment
	 *            新添加的fragment void return type
	 * @History memory：
	 * @Date：2014-12-16 下午3:36:06 @Modified by：zhangjx
	 * @Description：更新viewpager中的某个fragment
	 */
	public void replaceTabItems(int index, Fragment removeFragment,
			Fragment addFragment) {
		isReplaceOneFragment=true;
		if (removeFragment != null) {
			fragmentManager.beginTransaction().remove(removeFragment).commit();
		}
		fragmentClz.remove(index);
		fragmentClz.put(index, addFragment);
		fragmentManager.executePendingTransactions();
		notifyDataSetChanged();
	}

	public void replaceAllTabs(int[] removeFalg,
			SparseArray<Fragment> addFragments) {
		isReplaceOneFragment=false;
		if (fragmentClz.size() > 0) {
			FragmentTransaction fm = fragmentManager.beginTransaction();
			for (int i = 0; i < fragmentClz.size(); i++) {
				fm.remove(fragmentClz.get(i));
			}
			fm.commitAllowingStateLoss();
			fm = null;
			fragmentManager.executePendingTransactions();
		}
//		fragmentClz.clear();
//		this.fragmentClz = addFragments;
//		notifyDataSetChanged();
//		 fragmentClz.clear();
		 for (int j = 0; j < addFragments.size(); j++) {
		 this.fragmentClz.put(j, addFragments.get(j));
		 }
		 fragmentManager.beginTransaction().commitAllowingStateLoss();
		 fragmentManager.executePendingTransactions();
		 notifyDataSetChanged();
	}
}