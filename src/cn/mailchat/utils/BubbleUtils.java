package cn.mailchat.utils;

import cn.mailchat.R;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

public class BubbleUtils {
    
    public static enum Position {
        TOP, BOTTOM, LEFT, RIGHT
    }
    
    public static void showBubble(Context context, View anchorView, Position position, View contentView) {
        PopupWindow popupWindow = new PopupWindow(context);
        
        Drawable background;
        switch (position) {
            case TOP:
                background = context.getResources().getDrawable(R.drawable.bubble_top);
                break;
            case BOTTOM:
                background = context.getResources().getDrawable(R.drawable.bubble_bottom);
                break;
            case LEFT:
                background = context.getResources().getDrawable(R.drawable.bubble_left);
                break;
            case RIGHT:
                background = context.getResources().getDrawable(R.drawable.bubble_right);
                break;
            default:
                background = context.getResources().getDrawable(R.drawable.icon_no_chat_list_conent);
                break;
        }
        //background.setAlpha(200);
        popupWindow.setBackgroundDrawable(background);
        
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        float density = context.getResources().getDisplayMetrics().density;
        contentView.measure(0, 0);
        wmParams.height = (int)(contentView.getMeasuredHeight() * density);
        wmParams.width = (int)(contentView.getMeasuredWidth() * density);
        popupWindow.setHeight(wmParams.height);
        popupWindow.setWidth(wmParams.width);
        popupWindow.setFocusable(true);
        popupWindow.setContentView(contentView);
        
        int[] pos = new int[2];
        anchorView.getLocationOnScreen(pos);
        
        int xOffset = 0;
        int yOffset = 0;
        
        switch (position) {
            case TOP:
                xOffset = pos[0] + anchorView.getWidth() / 2 - wmParams.width / 2;
                yOffset = pos[1] - wmParams.height;
                break;
            case BOTTOM:
                xOffset = pos[0] + anchorView.getWidth() / 2 - wmParams.width / 2;
                yOffset = pos[1] + anchorView.getHeight();
                break;
            case LEFT:
                xOffset = pos[0] - wmParams.width;
                yOffset = pos[1] + anchorView.getHeight() / 2 - wmParams.height / 2;
                break;
            case RIGHT:
                xOffset = pos[0] + anchorView.getWidth();
                yOffset = pos[1] + anchorView.getHeight() / 2 - wmParams.height / 2;
                break;
            default:
                xOffset = pos[0] + anchorView.getWidth() / 2 - wmParams.width / 2;
                yOffset = pos[1] + anchorView.getHeight() / 2 - wmParams.height / 2;
                break;
        }
        
        popupWindow.showAtLocation(anchorView, Gravity.LEFT | Gravity.TOP, xOffset, yOffset);
    }

    private static PopupWindow popupWindow;
    private static PopupWindow popupArrow;
    public static void popupWindowTest(Context context,View anchor){
    	popupWindow = new PopupWindow(context);
    	LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	View root = (ViewGroup) inflater.inflate(R.layout.popupwindow_test, null);
    	ImageView mArrowDown = (ImageView) root.findViewById(R.id.arrow_down);
    	popupWindow.setContentView(root);
    	popupWindow.setTouchable(false);
		popupWindow.setFocusable(false);
		popupWindow.setOutsideTouchable(false);
		// 得到anchor的位置
		int[] location = new int[2];
		anchor.getLocationOnScreen(location);
		root.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		root.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		int rootWidth = root.getMeasuredWidth();
		int rootHeight = root.getMeasuredHeight();
		popupWindow.setWidth(rootWidth);
		popupWindow.setHeight(rootHeight);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		// 设置弹窗弹出的位置的x/y
		int xPos = location[0]-rootWidth+ anchor.getWidth()/2+19+30;//.9图片圆弧19PX
		int yPos = location[1] - rootHeight;
		// 根据弹出位置，设置不同方向箭头图片
		ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) mArrowDown
				.getLayoutParams();
		// 以此设置距离左边的距离
		param.leftMargin =rootWidth-20-19-10;//.9图片宽度20PX

		// 在指定位置弹出弹窗
		popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);

		//箭头popipWindow
		popupArrow = new PopupWindow(context);
		Drawable aBackground = context.getResources().getDrawable(
				R.drawable.popu_arrows_down);
		//aBackground.setAlpha(128);
		popupArrow.setBackgroundDrawable(aBackground);
		WindowManager.LayoutParams awmParams = new WindowManager.LayoutParams();
		awmParams.height = 12;
		awmParams.width = 20;
		popupArrow.setHeight(awmParams.height);
		popupArrow.setWidth(awmParams.width);
		popupArrow.setTouchable(false);
		popupArrow.setFocusable(false);
		popupArrow.setOutsideTouchable(false);
		popupArrow.setContentView(new TextView(context));
		int xOffset = 0;
		int yOffset = 0;
		int axOffset = 0;
		int ayOffset = 0;
		WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
		xOffset = location[0] + anchor.getWidth() / 2 - wmParams.width / 2;
		yOffset = location[1] - wmParams.height;
		axOffset = xOffset + wmParams.width / 2 - awmParams.width / 2;
		ayOffset = yOffset + wmParams.height;

		popupArrow.showAtLocation(anchor, Gravity.LEFT | Gravity.TOP,
				axOffset, ayOffset);
    }

    public static void popupWindowTestDismiss(){
		if(popupWindow!=null&&popupArrow!=null&&popupWindow.isShowing()&&popupArrow.isShowing()){
			popupWindow.dismiss();
			popupArrow.dismiss();
		}
    }

	public static boolean isPopupWindowTestShow() {
		return popupWindow != null && popupArrow != null
				&& popupWindow.isShowing() && popupArrow.isShowing();
	}
}
