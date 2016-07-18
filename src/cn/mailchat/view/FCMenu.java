package cn.mailchat.view;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.mailchat.R;
import cn.mailchat.activity.MailDetialActivity;
import cn.mailchat.adapter.FCMenuAdapter;

/**
 * 自定义菜单
 * 
 * @Description:
 * @author: huangyongxing
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-9-5
 */
public class FCMenu extends FCPopupWindow implements OnItemClickListener {

	private Context mContext;
	private Activity mActivity;
	private MenuItemOnClickListener callback;

	private LinearLayout container;
	// 因为菜单最多只需要两个gridview，所以这儿就直接定义
	private GridView gridView1;
	private MenuAdapter adapter1;
	private GridView gridView2;
	private MenuAdapter adapter2;

	public FCMenu(Context context) {
		super(context);
		this.mContext = context;
		init();
	}

	public FCMenu(Context context, MenuItemOnClickListener callback) {
		super(context);
		this.mContext = context;
		this.callback = callback;
		this.mActivity = (Activity) mContext;
		init();
	}

	public void setMenuItemOnclickListener(MenuItemOnClickListener callback) {
		this.callback = callback;
	}

	/**
	 * 初始化
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: 黄永兴 (huangyx2@35.cn)
	 * @date:2012-11-23
	 */
	private void init() {
		// 初始化窗口
		if(mActivity instanceof MailDetialActivity){
			this.container = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.fc_more_menu_layout, null);
		}else{
			this.container = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.fc_menu_layout, null);
		}
		// 初始化gridView
		this.gridView1 = (GridView) this.container.findViewById(R.id.fc_menu_gridview_1);
		this.gridView1.setOnItemClickListener(this);
		this.gridView2 = (GridView) this.container.findViewById(R.id.fc_menu_gridview_2);
		this.gridView2.setOnItemClickListener(this);
		// 初始化adapter
		this.adapter1 = new MenuAdapter();
		this.adapter2 = new MenuAdapter();
		// 为gridview添加adapter
		this.gridView1.setAdapter(adapter1);
		this.gridView2.setAdapter(adapter2);

		this.container.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// 按下menu键，并且菜单已打开，需要关闭菜单
				if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_MENU) {
					if (FCMenu.this.isShowing()) {
						FCMenu.this.close();
						return false;
					}
					return true;
				}
				return true;
			}
		});
	}

	/**
	 * 设置数据集
	 * 
	 * @Description:
	 * @param columnNum
	 * @param items
	 * @see:
	 * @since:
	 * @author: huangyongxing
	 * @date:2012-9-5
	 */
	public void setDatas(int columnNum, List<FCMenuItem> items) {
		int surplus = items.size() % columnNum;
		if (surplus == 0) { // 只需要一个gridview的情况
			this.gridView1.setVisibility(View.GONE);
			this.adapter2.clear();

			this.gridView2.setNumColumns(columnNum);
			this.adapter2.setDatas(items);
		} else {
			// 不能平分，最多只需要2个gridView
			List<FCMenuItem> temps1 = items.subList(0, surplus);
			this.gridView1.setNumColumns(surplus);
			this.adapter1.setDatas(temps1);
			List<FCMenuItem> temps2 = items.subList(surplus, items.size());
			this.gridView2.setNumColumns(columnNum);
			this.gridView2.setVisibility(View.VISIBLE);
			this.adapter2.setDatas(temps2);
		}
	}
	
	/**
	 * 设置数据集
	 * 
	 * @Description:
	 * @param items
	 * @see:
	 * @since:
	 * @author: sunzhongquan
	 * @date:2014-2-27
	 */
	public void setDatas(List<FCMenuItem> items) {
		this.gridView1.setVisibility(View.GONE);
		this.adapter2.clear();

		this.gridView2.setNumColumns(1);
		this.adapter2.setDatas(items);
	}

	/**
	 * 显示菜单
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: huangyongxing
	 * @date:2012-9-5
	 */
	public void showMenu() {
		if(mActivity instanceof MailDetialActivity){
			this.showAtRightBottom(container);
		}else{
			this.showAtBottom(container);
		}
	}

	/**
	 * 关闭菜单
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: huangyongxing
	 * @date:2012-9-5
	 */
	public void closeMenu() {
		this.close();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		closeMenu();
		this.callback.onItemClicked((FCMenuItem) parent.getAdapter().getItem(position));
	}

	public interface MenuItemOnClickListener {

		public void onItemClicked(FCMenuItem item);
	}

	public class MenuHolder {

		ImageView iconView;
		TextView textView;
	}

	private class MenuAdapter extends FCMenuAdapter<FCMenuItem> {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MenuHolder holder = null;
			if (holder == null || convertView.getTag() == null) {
				holder = new MenuHolder();
				if(mActivity instanceof MailDetialActivity){
					convertView = LayoutInflater.from(mContext).inflate(R.layout.item_more_menu, null);
				}else{
					convertView = LayoutInflater.from(mContext).inflate(R.layout.item_menu, null);
				}
				holder.iconView = (ImageView) convertView.findViewById(R.id.layout_menu_icon);
				holder.textView = (TextView) convertView.findViewById(R.id.layout_menu_txt);
				convertView.setTag(holder);
			}
			FCMenuItem item = getItem(position);
			holder.iconView.setImageResource(item.getIconRes());
			holder.textView.setText(item.getTextRes());
			return convertView;
		}
	}
}
