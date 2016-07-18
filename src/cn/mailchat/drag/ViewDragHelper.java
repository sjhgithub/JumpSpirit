package cn.mailchat.drag;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.widget.ListView;

public class ViewDragHelper {
	
	/**
	 * 拖拽到离原视图中心多远为有效动作
	 */
	public static final int MAX_DIS = 200;
	
	public static final int CANVAS_VIEW_ID = 1234;
	
	/**
	 * 为view添加拖拽功能 
	 */
	public static void makeViewDragable(View badgeView, ViewDragWare badgeDragWare){
		Activity activity = badgeDragWare.getActivity();
		ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
		Canvas4DragView canvas4DragView = (Canvas4DragView) rootView.findViewById(CANVAS_VIEW_ID);
		if ( canvas4DragView == null ){
			canvas4DragView = new Canvas4DragView(activity);
			canvas4DragView.setId(CANVAS_VIEW_ID);
			ViewGroup.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			canvas4DragView.setLayoutParams(lp);
			canvas4DragView.setVisibility(View.GONE);
			rootView.addView(canvas4DragView);
		}
		badgeView.setOnTouchListener(new ViewDragTouchListener(badgeDragWare, canvas4DragView));
	}
	
	/**
	 * 移除拖拽功能
	 */
	public static void recover (View badgeView){
		badgeView.setOnTouchListener(null);
	}
	
	/**
	 * 在ListView的Item中使用拖拽需先调用此方法初始化 
	 */
	@SuppressLint("ClickableViewAccessibility")
	public static void prepareListForViewDrag ( final ListView listView, final Activity activity){
		listView.setOnTouchListener(new OnTouchListener() {
			
			Canvas4DragView mCanvas4DragView;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
				mCanvas4DragView = (Canvas4DragView) rootView.findViewById(ViewDragHelper.CANVAS_VIEW_ID);
				if (mCanvas4DragView != null &&
						mCanvas4DragView.mIsDraging){
					return mCanvas4DragView.dispatchTouchEvent(event);
				}else{
					return listView.onTouchEvent(event);
				}
			}
		});
	}
	
	public static Bitmap converViewToBitMap(View view) {
		view.setDrawingCacheEnabled(true);
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache(true);
		return Bitmap.createBitmap(view.getDrawingCache());
	}
}
