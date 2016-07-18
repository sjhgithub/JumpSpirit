/**
 * @copyright © 35.com
 * file_name：MyGridViewInScrollView.java
 * @author ：zhangjx
 * @create Data ：2014-12-5上午10:13:00 
 * @Current Version：v1.0 
 * @History memory :
 * @Date : 2014-12-5上午10:13:00 
 * @Modified by：zhangjx
 * @Description :
 */
package cn.mailchat.view;

import android.widget.GridView;

/**
 * @copyright © 35.com
 * @file name ：MyGridViewInScrollView.java
 * @author ：zhangjx
 * @create Data ：2014-12-5上午10:13:00
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-12-5上午10:13:00
 * @Modified by：zhangjx
 * @Description :
 */
public class MyGridViewInScrollView extends GridView {
	public MyGridViewInScrollView(android.content.Context context,
			android.util.AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 设置不滚动
	 */
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);

	}

}