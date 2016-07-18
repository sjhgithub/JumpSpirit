package cn.mailchat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
/**
 * 
 * @copyright © 35.com
 * @file name ：FitWidthImageView.java
 * @author ：zhangjx
 * @create Data ：2015-5-13下午1:20:10 
 * @Current Version：v1.0 
 * @History memory :
 * @Date : 2015-5-13下午1:20:10 
 * @Modified by：zhangjx
 * @Description :重写imageView，使其长宽一致
 */
public class FitWidthImageView extends ImageView {

	private float aspectRatio = 1f;

	public FitWidthImageView(Context context) {
		super(context);
	}

	public FitWidthImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FitWidthImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = (int) (width * aspectRatio);
		super.onMeasure(widthMeasureSpec,
				MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
	}
}
