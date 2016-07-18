package cn.mailchat.drag;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import cn.mailchat.R;

/**
 * 视图拖拽时绘制所需的画布控件 
 */
public class Canvas4DragView extends View {

	public boolean mIsDraging = false;
	
	public Bitmap mBitmap;
	
	public void initForDraw( Bitmap bitmap ){
		mBitmap = bitmap;
	}
	
	public boolean mShouldDrawLine = true;  
	
	private Paint paint;
    
    float mCurX = -1;
    float mCurY = -1;
	
	float mSrcCX = -1;
    float mSrcCY = -1;
	
	
	private void initPaint (){
		paint = new Paint(); 
        paint.setColor(Color.rgb(255, 89, 76));   
        paint.setStrokeJoin(Paint.Join.ROUND);   
        paint.setStrokeCap(Paint.Cap.ROUND);   
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeWidth(8);
	}
	
	public Canvas4DragView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPaint();
        mBitmap= BitmapFactory.decodeResource(context.getResources(),R.drawable.badge_count_bg);
	}

	public Canvas4DragView(Context context, AttributeSet attrs) {
		super(context, attrs);
		 initPaint();
		 mBitmap= BitmapFactory.decodeResource(context.getResources(),R.drawable.badge_count_bg);
	}

    public Canvas4DragView(Context context) {
		super(context);   
		initPaint();
		mBitmap= BitmapFactory.decodeResource(context.getResources(),R.drawable.badge_count_bg);
	}
    
    public void setSrcCX ( float x ){
    	mSrcCX = x;
    }
    
    public void setSrcCY ( float y ){
    	mSrcCY = y;
    }
    
    public void setCurX ( float x ){
    	this.mCurX = x;
    }
 
    public void setCurY ( float y ){
    	this.mCurY = y;
    }

    //处理动作事件的视图
    private View mRpView;
    
    @SuppressLint("ClickableViewAccessibility")
	@Override
    public boolean onTouchEvent(MotionEvent event) {
    	if ( mRpView != null ){
    		return mRpView.dispatchTouchEvent(event);
    	}
    	return true;
    }
    
    /**
     * 特殊情况下（如在ListView的Item中）需用此方法注册处理动作事件的视图（即被拖拽视图）
     */
    protected void setTouchEventResponView ( View rpView ){
    	this.mRpView = rpView;
    }
    
    //画布对屏幕顶部的偏移
    int mTopMargin = 0;
    
    public void setTopMargin ( int topMargin ){
    	this.mTopMargin = topMargin;
    }
    
    public void reset (){
    	mSrcCX = -1;
    	mSrcCY = -1;
    	mShouldDrawLine = true;
    	setVisibility(View.GONE);
    }
    
    @Override   
    protected void onDraw(Canvas canvas) {
    	
    	if ( mSrcCX > 0 && mSrcCY > 0 ){
    		double dis = Math.sqrt( Math.pow(mSrcCX-mCurX, 2) + Math.pow(mSrcCY-mCurY, 2));
    		if (  mShouldDrawLine && dis < ViewDragHelper.MAX_DIS ){
    			paint.setStrokeWidth( (float)(18* ( 1-dis/ViewDragHelper.MAX_DIS )) );
    			float startX = mSrcCX;
    			float startY = mSrcCY-mTopMargin;
    			float endX = mCurX;
    			float endY = mCurY-mTopMargin;
    			canvas.drawLine(startX, startY, endX, endY, paint);
    			float cx = mSrcCX;
    			float cy = mSrcCY-mTopMargin;
    			canvas.drawCircle(cx, cy, (float)(20* ( 1- dis/ViewDragHelper.MAX_DIS )), paint);
    		}else{
    			mShouldDrawLine = false;
    		}
    	}
    	
    	if ( mCurX < 0 || mCurY < 0 ){
    		return;
    	}else{
    		float left = mCurX-mBitmap.getWidth()/2;
    		float top = mCurY-mBitmap.getHeight()/2-mTopMargin;
    		canvas.drawBitmap(mBitmap, left, top, paint);
    	}
    }   
}
