package cn.mailchat.view.pager.base;

import cn.mailchat.fragment.contact.ChooseContactListener;

public class BaseTabFragment extends BaseFragment {

	private ChooseContactListener chooseContactListener;
	private TabChangedListener mListener;

	public BaseTabFragment() {
	}

	public final void addTabChangeListener(TabChangedListener listener) {
		mListener = listener;
	}

	protected final boolean e() {
		return mListener.isCurrent(this);
	}

	public void f() {
	}

	public void g() {
	}

	public void h() {
	}

	public void i() {
	}

	public ChooseContactListener getChooseContactListener() {
		return chooseContactListener;
	}

	public void setChooseContactListener(
			ChooseContactListener chooseContactListener) {
		this.chooseContactListener = chooseContactListener;
	}

	public static interface TabChangedListener {
		public abstract boolean isCurrent(BaseTabFragment fragment);
	}

}
