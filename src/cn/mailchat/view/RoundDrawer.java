package cn.mailchat.view;

import cn.mailchat.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


/**
 * <pre>
 * &#064;Override
 * protected void onLayout(boolean changed, int left, int top, int right,
 * 		int bottom) {
 * 	super.onLayout(changed, left, top, right, bottom);
 * 	roundDrawer.layout(changed, left, top, right, bottom);
 * }
 * 
 * &#064;Override
 * public void draw(Canvas canvas) {
 * 	roundDrawer.draw(canvas);
 * }
 * 
 * &#064;Override
 * public void superDraw(Canvas canvas) {
 * 	super.draw(canvas);
 * }
 * </pre>
 * 
 * @author qxian
 * 
 */
public class RoundDrawer {
	private float rect_radius = 2.2f;
	public final static float abs_roung = -1;
	private SuperDrawer superDrawer;
	private View boundView;

	//start by shengli
	private boolean isRadiusPixel;
	private float rect_radius_pixel =0;
	//end

	public interface SuperDrawer {
		public View getView();

		public Context getContext();

		public void superDraw(Canvas canvas);
	}

	public interface DrawFilter {
		public void perDraw(Canvas canvas, float r, RectF clipRect);

		public void onRectCreated(RectF rect, float r);
	}

	private DrawFilter mDrawFilter;

	public void setDrawFilter(DrawFilter filter) {
		mDrawFilter = filter;
	}

	public RoundDrawer(SuperDrawer superDrawer, AttributeSet attrs) {
		if (attrs != null) {
			Context context = superDrawer.getContext();
			TypedArray array = context.getResources().obtainAttributes(attrs,
					R.styleable.RoundCornerView);
			rect_radius = array.getDimension(
					R.styleable.RoundCornerView_round_view_radius, rect_radius);
			//start by shengli
			isRadiusPixel = array.getBoolean(
					R.styleable.RoundCornerView_round_view_radius_pixel_open, isRadiusPixel);
			rect_radius_pixel = array.getDimension(
					R.styleable.RoundCornerView_round_view_radius_pixel, rect_radius_pixel);
			//end
			array.recycle();
		}
		this.boundView = superDrawer.getView();
		this.superDrawer = superDrawer;
		init();
	}

	public RoundDrawer(SuperDrawer superDrawer) {
		this(superDrawer, null);
	}

	private final RectF roundRect = new RectF();
	private final Paint maskPaint = new Paint();
	private final Paint zonePaint = new Paint();

	private void init() {
		maskPaint.setAntiAlias(true);
		maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		//
		zonePaint.setAntiAlias(true);
		zonePaint.setColor(Color.WHITE);
		//
		float density = boundView.getResources().getDisplayMetrics().density;

		//start by shengli
		if(isRadiusPixel){
			rect_radius = rect_radius_pixel;
		}else{
			rect_radius = rect_radius * density;
		}
		//end
	}

	public void setRectRadius(float radius) {
		rect_radius = radius;
		if (boundView != null) {
			boundView.invalidate();
		}
	}

	private int viewWidth;
	private int viewHeight;

	public void layout(boolean changed, int left, int top, int right, int bottom) {
		if (boundView != null) {
			viewWidth = boundView.getWidth();
			viewHeight = boundView.getHeight();
			roundRect.set(0, 0, viewWidth, viewHeight);
			// roundRect.set(0, 0, w, h + rect_adius);//仅上边圆角
			// roundRect.set(0, rect_adius, w, h); //仅下边圆角
			// roundRect.set(0, 0, w+ rect_adius, h+ rect_adius);//仅左上角是圆角
		}
		if (mDrawFilter != null) {
			mDrawFilter.onRectCreated(roundRect, rect_radius);
		}

	}

	public void draw(Canvas canvas) {
		float rectR = rect_radius;
		if (rectR < 0 || rectR == abs_roung) {
			rectR = (float) viewWidth;
		}
		if (mDrawFilter != null) {
			mDrawFilter.perDraw(canvas, rectR, roundRect);
		}
		canvas.saveLayer(roundRect, zonePaint, Canvas.ALL_SAVE_FLAG);
		canvas.drawRoundRect(roundRect, rectR, rectR, zonePaint);
		//
		canvas.saveLayer(roundRect, maskPaint, Canvas.ALL_SAVE_FLAG);
		superDrawer.superDraw(canvas);
		canvas.restore();
	}
}
