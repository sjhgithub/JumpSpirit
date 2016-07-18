package cn.mailchat.surprise;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import cn.mailchat.view.hlistview.AbsHListView.PositionScroller;

public class LeapSpiritView extends View {
	
	public LeapSpiritView(Context context) {
		super(context);
		init();
	}
	
	public LeapSpiritView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LeapSpiritView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	private void init (){
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
	}
	
	Paint paint;
	
	private List<LeapSpirit> spirits;
	
	public void drawSpirits (List<LeapSpirit> spirits){
		this.spirits = spirits;
		postInvalidate();
	}
	
	List<float[]> lines;
	
	public void drawLines (List<float[]> lines){
		this.lines = lines;
		postInvalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.drawCircle(200, 200, 50, paint);
//		if (lines != null){
//			for ( float[] line : lines ){
//				canvas.drawLine(line[0], line[1], line[2], line[3], paint);
//			}
//		}
		
		if (spirits != null){
			for ( LeapSpirit leapSpirit : spirits ){
				float[] postion = leapSpirit.getPosition();
				if (postion != null){
					//canvas.drawCircle(postion[0], postion[1], 50, paint);
					canvas.drawBitmap(leapSpirit.getDrawable(), postion[0], postion[1], paint);
				}
			}
		}
	}
}
