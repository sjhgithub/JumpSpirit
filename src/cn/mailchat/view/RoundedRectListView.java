package cn.mailchat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.ListView;
import cn.mailchat.R;

/**
 * 圆角listview
 * 
 * @Description:
 * @author:xuqq
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2014-4-4
 */
public class RoundedRectListView extends ListView {

	private Context mContext;

	public RoundedRectListView(Context context) {
		super(context);
		this.mContext = context;
	}

	public RoundedRectListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
	}

	public RoundedRectListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			int x = (int) ev.getX();
			int y = (int) ev.getY();
			int itemnum = pointToPosition(x, y);

			if (itemnum == AdapterView.INVALID_POSITION)
				break;
			else {
				if (itemnum == 0) {
					if (itemnum == (getAdapter().getCount() - 1))
						setSelector(R.drawable.selector_list_round);
					else
						setSelector(R.drawable.selector_list_top_round);
				} else if (itemnum == (getAdapter().getCount() - 1))
					setSelector(R.drawable.selector_list_bottom_round);
				else
					setSelector(R.drawable.selector_list_m_round);
			}
			break;
		case MotionEvent.ACTION_UP:
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}
}
