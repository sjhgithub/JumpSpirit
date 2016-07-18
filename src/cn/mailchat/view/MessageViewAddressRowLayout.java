package cn.mailchat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * 
 * 邮件详情页里，收件人展示时需要
 * 
 * @Description:邮件地址自动换行控件(韩布和)
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-7-13
 */
public class MessageViewAddressRowLayout extends LinearLayout {

	public MessageViewAddressRowLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public MessageViewAddressRowLayout(Context context) {
		super(context);
	}

	@Override
	protected void onLayout(boolean arg0, int left, int top, int right, int bottom) {

		int childCount = getChildCount();
		int usedWidth = 0;
		int height = 0;

		int width = right - left;

		this.measureChildren(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.UNSPECIFIED));

		// LogX.v("width", "width:" + getWidth());
		for (int i = 0; i < childCount; i++) {

			if (usedWidth + getChildAt(i).getMeasuredWidth() + 20 >= width) {
				if (usedWidth != 0) {
					usedWidth = 0;
					height += getChildAt(i).getMeasuredHeight()+5;
				}
			}

			getChildAt(i).layout(usedWidth, height, usedWidth + getChildAt(i).getMeasuredWidth() + 12, height + getChildAt(i).getMeasuredHeight());
			usedWidth += getChildAt(i).getMeasuredWidth() + 20;
		}
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int childCount = getChildCount();
		int usedWidth = 0;
		int height = 32;
		if (childCount >= 1) {
			height = getChildAt(0).getMeasuredHeight();
		}

		int counter = 0;

		int width = MeasureSpec.getSize(widthMeasureSpec);
		if (width <= 0) {
			width = 200;
		}
		this.measureChildren(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.UNSPECIFIED));
		for (int i = 0; i < childCount; i++) {

			if (usedWidth + getChildAt(i).getMeasuredWidth() > width) {
				if (usedWidth != 0) {
					usedWidth = 0;
					height += getChildAt(i).getMeasuredHeight()+5;
				}

			}
			usedWidth += getChildAt(i).getMeasuredWidth() + 20;
		}
	
		int desiredHeight = View.resolveSize(height, heightMeasureSpec);

		this.setMeasuredDimension(width, desiredHeight);
	}

}
