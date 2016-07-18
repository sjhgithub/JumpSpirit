package cn.mailchat.surprise;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.LinearGradient;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import cn.mailchat.drag.Canvas4DragView;

public class SurpriseAnimationManager {
	
	public static final int CANVAS_VIEW_ID = 1234;
	
	private BoardInfoProvider mBoardInfoProvider;
	private List<LeapSpirit> spirits;
	private LeapSpiritView canvasView;
	private Context context;
	
	
	public SurpriseAnimationManager (Context context, BoardInfoProvider boardInfoProvider){
		this.mBoardInfoProvider = boardInfoProvider;
		this.context = context;
	}
	
	public void startAnim (){
		Activity activity = mBoardInfoProvider.getActivity();
		ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
		canvasView  = (LeapSpiritView) rootView.findViewById(CANVAS_VIEW_ID);
		if ( canvasView == null ){
			canvasView = new LeapSpiritView(activity);
			canvasView.setId(CANVAS_VIEW_ID);
			ViewGroup.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			canvasView.setLayoutParams(lp);
			//canvasView.setVisibility(View.GONE);
			rootView.addView(canvasView);
		}
		spirits = new LinkedList<>();
		LeapSpirit leapSpirit = new LeapSpirit(context, mBoardInfoProvider, mBoardInfoProvider.boardCount()-10);
		spirits.add(leapSpirit);
		new AnimThread().start();
	}
	
	private void drawSpirit (List<LeapSpirit> spirits){
		canvasView.drawSpirits(spirits);
	}
	
	private void logLine (float[] line){
		Log.d("xxxxxxxxxxxxxxxx  line", ""+line[0]+" "+line[1]+" "+line[2]+" "+line[3]);
	}
	class AnimThread extends Thread {
		
		@Override
		public void run() {
//			while ( true ){
				//canvasView.postInvalidate();
//				List<float[]> lines = new LinkedList<>();
//				float[] line = null;
//				line = mBoardInfoProvider.getBoardInfo(mBoardInfoProvider.boardCount()-3);
//				if (line != null){
//					lines.add(line);
//					logLine(line);
//				}
//				line = mBoardInfoProvider.getBoardInfo(mBoardInfoProvider.boardCount()-2);
//				if (line != null){
//					lines.add(line);
//					logLine(line);
//				}
				
				//canvasView.drawLines(lines);
//				try {
//					sleep(20);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}	
//			}
			
			while (!spirits.isEmpty()){
				for ( int i = 0; i < spirits.size(); i++ ){
					LeapSpirit spirit = spirits.get(i);
					spirit.updateStatus();
					if (spirit.isDead()){
						spirits.remove(i);
						i--;
					}
				}
				drawSpirit(spirits);
//				try {
//					sleep(20);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			}
		}
	}

}
