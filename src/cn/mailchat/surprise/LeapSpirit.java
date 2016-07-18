package cn.mailchat.surprise;

import java.util.Random;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import cn.mailchat.R;

public class LeapSpirit {
	
	private int fromBoardIndex;
	private int toBoardIndex;

	private float curStatePassTime;
	
	private BoardInfoProvider boardInfoProvider;
	
	private boolean isDead;
	
	private long lastTime;
	
	private Bitmap bitmap;
	
	private int height;
	
	private int width;
	
	private TimeInterpolator upInterpolator;
	
	private TimeInterpolator downInterpolator;
	
	private float firstBounds;
	
	private float secondBounds;
	
	private float direction;
	
	private float lastFirstBounds = -1;
	
	private float lastSecondBounds = -1;
	
	private float lastDirection = 0;
	
	private Random random;
	
	public LeapSpirit (Context context,BoardInfoProvider boardInfoProvider, int beginBoardIndex){
		fromBoardIndex = beginBoardIndex;
		toBoardIndex = fromBoardIndex+1;
		curStatePassTime = 0;
		isDead = false;
		this.boardInfoProvider = boardInfoProvider;
		
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.emoji_019);
		width = bitmap.getWidth();
		height = bitmap.getHeight();
		
		upInterpolator = new DecelerateInterpolator(0.6f);
		downInterpolator = new AccelerateInterpolator();
		
		random = new Random();
		generateNextJumpStyle();
	}
	
	private void generateNextJumpStyle (){
		firstBounds = random.nextFloat()*0.25f;
		secondBounds = firstBounds*0.6f*random.nextFloat();
		direction = random.nextBoolean() ? 1 : -1;
	}
	
	public void updateStatus (){
		long curTime = System.currentTimeMillis();
		
		if (lastTime != 0){
			long passTime = curTime - lastTime;
			Log.d("xxx", "tim pass="+passTime);
			long secondCount = passTime /1500;
			Log.d("xxx", "secondCount="+secondCount);
			long statusPassTime = passTime % 1500;
			Log.d("xxx", "statusPassTime="+statusPassTime);
			
			fromBoardIndex += secondCount;
			toBoardIndex = fromBoardIndex+1;
			curStatePassTime += statusPassTime/1500f;
			
			Log.d("xxx", "now fromBoardIndex="+fromBoardIndex+" toBoardIndex="+toBoardIndex+" curStatePassTime="+curStatePassTime);
		}
		
		if ( curStatePassTime >= 1.5 ){
			curStatePassTime = 0;
			fromBoardIndex++;
			toBoardIndex = fromBoardIndex+1;
			lastDirection = direction;
			lastFirstBounds = firstBounds;
			lastSecondBounds = secondBounds;
			generateNextJumpStyle();
		}
		lastTime = curTime;
		
		if (fromBoardIndex >= boardInfoProvider.boardCount()-1 ){
			isDead = true;
		}
	}
	
	public Bitmap getDrawable (){
		return bitmap;
	}

	public float[] getPosition (){
		float[] fromBoard = boardInfoProvider.getBoardInfo(fromBoardIndex);
		float[] toBoard = boardInfoProvider.getBoardInfo(toBoardIndex);
		
		if ( fromBoard == null || toBoard ==  null ){
			return null;
		}
		
		float[] fromMidPoint = midPoint(fromBoard);
		float[] toMidPoint = midPoint(toBoard);
		
		float[] controllerPoint = new float[2];
		controllerPoint[0] = (fromMidPoint[0]+ toMidPoint[0])/2.0f;
		controllerPoint[1] = fromMidPoint[1]-300;
		
		float[] curPosition = new float[2];
		
		//大跳跃
		Path path = new Path();
		if (lastDirection == 0){
			path.moveTo(fromMidPoint[0], fromMidPoint[1]);
		}else{
			float fromBoardLength = fromBoard[2] - fromBoard[0];
			float[] newBeginPoint = new float[2];
			newBeginPoint[0] = fromMidPoint[0] + fromBoardLength*lastFirstBounds*lastDirection + fromBoardLength*lastSecondBounds*lastDirection;
			newBeginPoint[1] = fromMidPoint[1];
			path.moveTo(newBeginPoint[0], newBeginPoint[1]);
			
			controllerPoint = new float[2];
			controllerPoint[0] = (newBeginPoint[0]+ toMidPoint[0])/2.0f;
			controllerPoint[1] = newBeginPoint[1]-300;
		}
		path.quadTo(controllerPoint[0], controllerPoint[1], toMidPoint[0], toMidPoint[1]);
		PathMeasure measure = new PathMeasure(path,false);
		float totalLength = measure.getLength();
		float curLength = 0;
		
		float boardLength = toBoard[2] - toBoard[0]; 
		
		//第一次回弹
		float[] bounds1EndPosition = new float[2];
		bounds1EndPosition[0] = toMidPoint[0]+ direction*firstBounds*boardLength;
		bounds1EndPosition[1] = toMidPoint[1];
		float[] controllerPoint1 = new float[2];
		controllerPoint1[0] = (bounds1EndPosition[0]+toMidPoint[0])/2.0f;
		controllerPoint1[1] = toMidPoint[1]-80;
		Path path1 = new Path();
		path1.moveTo(toMidPoint[0],toMidPoint[1]);
		path1.quadTo(controllerPoint1[0], controllerPoint1[1], bounds1EndPosition[0], bounds1EndPosition[1]);
		PathMeasure measure1 = new PathMeasure(path1,false);
		float totalLength1 = measure1.getLength();
		float curLength1 = 0;
		
		//第二次回弹
		float[] bounds2EndPosition = new float[2];
		bounds2EndPosition[0] = bounds1EndPosition[0]+ direction*secondBounds*boardLength;
		bounds2EndPosition[1] = bounds1EndPosition[1];
		float[] controllerPoint2 = new float[2];
		controllerPoint2[0] = (bounds1EndPosition[0]+bounds2EndPosition[0])/2.0f;
		controllerPoint2[1] = bounds1EndPosition[1]-30;
		Path path2 = new Path();
		path2.moveTo(bounds1EndPosition[0],bounds1EndPosition[1]);
		path2.quadTo(controllerPoint2[0], controllerPoint2[1], bounds2EndPosition[0], bounds2EndPosition[1]);
		PathMeasure measure2 = new PathMeasure(path2,false);
		float totalLength2 = measure2.getLength();
		float curLength2 = 0;
		
		float thisStepPassTime;
		
		if (curStatePassTime <= 1){ //大跳跃期间
			
			thisStepPassTime = curStatePassTime;
			if (thisStepPassTime <= 0.2){
				curLength = totalLength*thisStepPassTime; //( upInterpolator.getInterpolation(curStatePassTime/0.5f) );
			}else{
				curLength = totalLength * 0.2f + totalLength * 0.8f * ( downInterpolator.getInterpolation((thisStepPassTime-0.2f)/0.8f) );
			}
			measure.getPosTan(curLength, curPosition, null);
			
		} else if (curStatePassTime <= 1.35){//第一次回弹
			
			thisStepPassTime = curStatePassTime-1;
			if (thisStepPassTime <= 0.175){
				curLength1 = totalLength1*(thisStepPassTime/0.35f); 
			}else{
				curLength1 = totalLength1 * 0.5f + totalLength1 * 0.5f * ( downInterpolator.getInterpolation((thisStepPassTime-0.175f)/0.175f) );
			}
			measure1.getPosTan(curLength1, curPosition, null);
			
		} else {//第二次回弹
			
			thisStepPassTime = curStatePassTime-1.35f;
			if (thisStepPassTime <= 0.075){
				curLength2 = totalLength2*(thisStepPassTime/0.15f); 
			}else{
				curLength2 = totalLength2 * 0.5f + totalLength2 * 0.5f * ( downInterpolator.getInterpolation((thisStepPassTime-0.075f)/0.075f) );
			}
			measure2.getPosTan(curLength2, curPosition, null);
		}
		
		curPosition[0] -= width/2;
		curPosition[1] -= height;
		return curPosition;
	}
	
	private float[] midPoint(float[] points){
		float[] midPoint = new float[2];
		midPoint[0] = (points[0]+ points[2])/2.0f;
		midPoint[1] = (points[1]+ points[3])/2.0f;
		return midPoint;
	}
	
	public boolean isDead (){
		return isDead;
	}
}
