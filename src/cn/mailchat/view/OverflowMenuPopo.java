package cn.mailchat.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.internal.widget.ListPopupWindow;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.mailchat.R;

public class OverflowMenuPopo {
	private Context mContext;
	private MenuAdapter mMenuAdapter;
	private ListPopupWindow mMenuWindow;
	private String[] menuArray ;
	private OverflowMenuPopoListener mOverflowMenuPopoListener;

	public OverflowMenuPopo(Context mContext, String[] menuArray,
			OverflowMenuPopoListener mOverflowMenuPopoListener) {
		this.mContext = mContext;
		this.menuArray = menuArray;
		this.mOverflowMenuPopoListener = mOverflowMenuPopoListener;
	}

	public void showMoreOptionMenu(View view) {
		mMenuWindow = new ListPopupWindow(mContext);
		if (mMenuAdapter == null) {
			mMenuAdapter = new MenuAdapter();
		}
		mMenuWindow.setModal(true);
		mMenuWindow.setContentWidth(mContext.getResources()
				.getDimensionPixelSize(
						R.dimen.popo_main_chatting_menu_dialog_width));
		mMenuWindow.setAdapter(mMenuAdapter);
		mMenuWindow.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mOverflowMenuPopoListener.onMenuItemClick(position);
				if (mMenuWindow != null) {
					mMenuWindow.dismiss();
					mMenuWindow = null;
				}
			}
		});
		mMenuWindow.setAnchorView(view);
		mMenuWindow.show();
	}

	class MenuAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return menuArray.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(
					R.layout.item_overflow_menu, null);
			TextView name = (TextView) convertView.findViewById(R.id.tv_name);
			name.setText(menuArray[position]);
			return convertView;
		}
	}

	public interface OverflowMenuPopoListener {
		void onMenuItemClick(int position);
	}
}
