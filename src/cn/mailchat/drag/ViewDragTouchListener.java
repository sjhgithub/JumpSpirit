package cn.mailchat.drag;

import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.activity.Main4TabActivity;

@SuppressLint("ClickableViewAccessibility")
public class ViewDragTouchListener implements OnTouchListener {
	
	private ViewDragWare mBadgeDragWare;
	
	private int mMarginLeftInCanvas;
	private int mMarginTopInCanvas;
	
	/** 是否首次触碰 */
	private boolean mFirstTouchMark = true;
	
	/** 被拖拽view原来的中心坐标(相对于screen) */
	private float mSrcCX;
	private float mSrcCY;
	
	private Bitmap mCacheBitmap;
	
	private Canvas4DragView mCanvas4DragView;
	
	public ViewDragTouchListener( ViewDragWare badgeDragWare, Canvas4DragView canvas4DragView ) {
		this.mBadgeDragWare = badgeDragWare;
		mCanvas4DragView = canvas4DragView;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				if (mFirstTouchMark) {
					int[] positionCanvas = new int[2];
					mCanvas4DragView.getLocationOnScreen(positionCanvas);
					int marginTop = positionCanvas[1];
					mCanvas4DragView.setTopMargin(marginTop);
					
					mCanvas4DragView.mIsDraging = true;
					mCanvas4DragView.setTouchEventResponView(mBadgeDragWare.getDragView());
					
					int[] positions = new int[2];
					mBadgeDragWare.getDragView().getLocationOnScreen(positions);
					mSrcCX = (float) (positions[0] + mBadgeDragWare.getDragView().getWidth() / 2);
					mSrcCY = (float) (positions[1] + mBadgeDragWare.getDragView().getHeight() / 2);
					
					mMarginLeftInCanvas = (int) positions[0];
					mMarginTopInCanvas = (int) positions[1];
					
					mCanvas4DragView.setSrcCX(mSrcCX);
					mCanvas4DragView.setSrcCY(mSrcCY);
					mCacheBitmap = ViewDragHelper.converViewToBitMap(mBadgeDragWare.getDragView());
					mCanvas4DragView.initForDraw(mCacheBitmap);
					mFirstTouchMark = false;
					mCanvas4DragView.setVisibility(View.VISIBLE);
					mBadgeDragWare.hideOrgView();
				}
				mCanvas4DragView.setCurX(event.getRawX());
				mCanvas4DragView.setCurY(event.getRawY());
				mCanvas4DragView.invalidate();
				break;
			case MotionEvent.ACTION_UP:
				mCanvas4DragView.reset();
				mFirstTouchMark = true;
				float endX = event.getRawX();
				float endY = event.getRawY();
				double dis = Math.sqrt(Math.pow(mSrcCX - endX, 2) + Math.pow(mSrcCY - endY, 2));
				if (dis < ViewDragHelper.MAX_DIS) {//近处释放拖拽动作，抖动动画
					// 动画图层
					Activity context = mBadgeDragWare.getActivity();
					final ViewGroup rootView = (ViewGroup) context.getWindow().getDecorView();
					final LinearLayout animLayout = new LinearLayout(context);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
					animLayout.setLayoutParams(lp);
					animLayout.setBackgroundResource(android.R.color.transparent);
					rootView.addView(animLayout);
					// 在动画图层中加入动画组件
					final ImageView tempImgView = new ImageView(context);
					tempImgView.setImageBitmap(mCacheBitmap);
					LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					lp2.leftMargin = mMarginLeftInCanvas;
					lp2.topMargin = mMarginTopInCanvas;
					tempImgView.setLayoutParams(lp2);
					animLayout.addView(tempImgView);
					// 位移动画
					AnimationSet animationSet = new AnimationSet(true);
					animationSet.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {

						}

						@Override
						public void onAnimationRepeat(Animation animation) {

						}

						@Override
						public void onAnimationEnd(Animation animation) {
							// 动画图层及组件剥离
							animLayout.removeView(tempImgView);
							rootView.removeView(animLayout);
							// 原图层组件复原
							mBadgeDragWare.reShowOrgView();
						}
					});
					TranslateAnimation translateAnimation = new TranslateAnimation(0, (mSrcCX - endX) / 3, 0,
							(mSrcCY - endY) / 3);
					translateAnimation.setDuration(170);
					TranslateAnimation translateAnimation2 = new TranslateAnimation(0,
							-((mSrcCX - endX) / 3 * 1.5f), 0, -((mSrcCY - endY) / 3 * 1.5f));
					translateAnimation2.setStartOffset(170);
					translateAnimation2.setDuration(170);
					animationSet.addAnimation(translateAnimation);
					animationSet.addAnimation(translateAnimation2);
					tempImgView.startAnimation(animationSet);
				} else { //远处释放拖拽动作
					MobclickAgent.onEvent(MailChat.getInstance(), "drag_badge");
					mBadgeDragWare.doOnActionSuccess();
				}
				mCanvas4DragView.mIsDraging = false;
				break;
			}
			return true;
	}
}
