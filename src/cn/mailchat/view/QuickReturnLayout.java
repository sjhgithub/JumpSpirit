package cn.mailchat.view;

import cn.mailchat.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 可以排挤的Title View
 * 
 * @author sky
 * 
 */
public class QuickReturnLayout extends ViewGroup {
	public static final int def_content_id = R.id.lv_view_muliple_select_user;
	private int id_content = def_content_id;

	private View contentView;
	private boolean isFlolowing;
	private float preMoveY;
	private boolean lastTouchDownInvokeMoved;

	public QuickReturnLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public QuickReturnLayout(Context context) {
		super(context);
		init();
	}

	/**
	 * 设置内容的id
	 * 
	 * @param contentId
	 */
	public void setContentId(int contentId) {
		this.id_content = contentId;
	}

	private void init() {
		setChildrenDrawingOrderEnabled(true);
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		return childCount - i - 1;
	}

	private int titlePartHideHeight;// 隐藏的高度
	private int titlePartHeight;

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childCount = getChildCount();
		if (childCount <= 0) {
			return;
		}
		int w = r - l;
		// int h = b - t;
		int cl = 0;
		int ct = 0;
		int cr = w;
		int cb = 0;
		View contentView = null;
		int titleShowableHeight = titlePartHeight - titlePartHideHeight;
		if (titleShowableHeight < 0) {
			titleShowableHeight = 0;
		}
		//
		int cLeftMargin = 0;
		int cTopMargin = 0;
		int cRightMargin = 0;
		int cBottomMargin = 0;
		//
		int topOffset = 0;
		MarginLayoutParams tempCMarginLp;
		for (int i = 0; i < childCount; i++) {
			View c = getChildAt(i);
			if (c.getVisibility() == View.GONE) {
				continue;
			}
			if (c.getId() == id_content) {
				contentView = c;
				continue;
			}
			tempCMarginLp = (MarginLayoutParams) c.getLayoutParams();
			cLeftMargin = tempCMarginLp.leftMargin;
			cTopMargin = tempCMarginLp.topMargin;
			cRightMargin = tempCMarginLp.rightMargin;
			cBottomMargin = tempCMarginLp.bottomMargin;
			//
			ct = topOffset + cTopMargin;
			cb = ct + c.getMeasuredHeight();
			if (cb + cBottomMargin > titleShowableHeight) {
				ct -= cb + cBottomMargin - titleShowableHeight;
				cb = titleShowableHeight - cBottomMargin;
			}
			c.layout(cl + cLeftMargin, ct, cr - cRightMargin, cb);
			//
			topOffset = cb + cBottomMargin;
		}
		// layout content view
		if (contentView != null) {
			tempCMarginLp = (MarginLayoutParams) contentView.getLayoutParams();
			cLeftMargin = tempCMarginLp.leftMargin;
			cTopMargin = tempCMarginLp.topMargin;
			cRightMargin = tempCMarginLp.rightMargin;
			cBottomMargin = tempCMarginLp.bottomMargin;
			//
			ct = topOffset + cTopMargin;
			cb = ct + contentView.getMeasuredHeight();
			contentView.layout(cl + cLeftMargin, ct, cr - cRightMargin, cb);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY
		// || MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
		// throw new IllegalArgumentException(
		// "we only support fill_parent,martch_parent,or exactly size");
		// }
		int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
		int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(maxWidth, maxHeight);
		//
		int childCount = getChildCount();
		int tph = 0;
		View contentView = null;
		MarginLayoutParams lp = null;
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (child.getVisibility() == View.GONE) {
				continue;
			}
			if (child.getId() == id_content) {
				contentView = child;
				continue;
			}
			measureChildWithMargins(child, widthMeasureSpec, 0,
					heightMeasureSpec, tph);
			lp = (MarginLayoutParams) child.getLayoutParams();
			tph += lp.topMargin;
			tph += child.getMeasuredHeight();
			tph += lp.bottomMargin;
		}
		titlePartHeight = tph;
		if (contentView != null) {
			measureChildWithMargins(
					contentView,
					MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY),
					0,
					MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY),
					0);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			preMoveY = ev.getY();
			lastTouchDownInvokeMoved = false;
			isFlolowing = false;
			break;
		case MotionEvent.ACTION_MOVE:
			float curActionY = ev.getY();
			float preActionY = preMoveY;
			preMoveY = curActionY;
			if (titlePartHeight - titlePartHideHeight > 0) {// title 显示出来的高度大于0
				boolean pastScrollToContentView = false;
				if (titlePartHideHeight <= 0) {// 如果title全部显示了
					if (preActionY < curActionY) {// scroll down
						// 此时我们希望content view来处理scroll
						pastScrollToContentView = true;
					}
				}
				if (!pastScrollToContentView) {
					checkDoFollow(preActionY, curActionY);
					return true;
				}
			} else {
				if (preActionY < curActionY) {// scroll down
					checkDoFollow(preActionY, curActionY);
					return true;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if (lastTouchDownInvokeMoved) {
				ev.setAction(MotionEvent.ACTION_CANCEL);
			}
			break;
		}
		super.dispatchTouchEvent(ev);
		return true;
	}


	private void checkDoFollow(float lastActionY, float curActionY) {
		if (isFlolowing) {
			doFollow(lastActionY, curActionY);
		} else if (Math.abs(lastActionY - curActionY) > 2f) {
			isFlolowing = true;
			lastTouchDownInvokeMoved = true;
			checkDoFollow(lastActionY, curActionY);
		}
	}

	public View getContentView() {
		if (contentView == null) {
			contentView = findViewById(id_content);
		} else {
			if (contentView.getId() != id_content
					|| contentView.getParent() != this) {
				contentView = null;
				return getContentView();
			}
		}
		return contentView;
	}

	private void doFollow(float lastMoveY, float currentMoveY) {
		titlePartHideHeight += (int) (lastMoveY - currentMoveY);
		forceLayout();
		requestLayout();
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new MarginLayoutParams(getContext(), attrs);
	}
}
