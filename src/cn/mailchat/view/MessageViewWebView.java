package cn.mailchat.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.widget.Toast;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.helper.HtmlConverter;

/**
 * pushmail专用webView子类，拦截左右滑到边界事件
 * 
 * MessaveViewViewPagerItemBodyView里的WebView
 * 
 * 作用：判断WebView是否滑动到左右边界
 * 
 * @Description:
 * @author: cuiwei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-5-13
 */
public class MessageViewWebView extends RigidWebView {

//	private SlideListener slideListener;
    public MessageViewWebView(Context context) {
        super(context);
    	setFocusableInTouchMode(false);
    }

	public MessageViewWebView(Context context, AttributeSet atts) {
		super(context, atts);
		// setFocusable(false);
		setFocusableInTouchMode(false);
	}

    public MessageViewWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    	setFocusableInTouchMode(false);
    }

	public boolean canScrollHor(float direction) {
		final int offset = computeHorizontalScrollOffset();
		final int range = computeHorizontalScrollRange() - computeHorizontalScrollExtent();
		if (range == 0) {
			return false;
		}
		if (direction < 0) {
			return offset > 0;
		} else {
			return offset < range - 1;
		}
	}
	/**
	 * 
	 * @Description:webview显示结束时回调
	 * @author:sunzhongquan  
	 * @see:   
	 * @since:      
	 * @copyright © 35.com
	 * @Date:2014-3-5
	 */
	public interface DisplayFinish{   
        void After();   
    }
    DisplayFinish df;   
    public void setDf(DisplayFinish df) {   
        this.df = df;   
    }
    
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		df.After();
	}
	/**
     * Configure a web view to load or not load network data. A <b>true</b> setting here means that
     * network data will be blocked.
     * @param shouldBlockNetworkData True if network data should be blocked, false to allow network data.
     */
    public void blockNetworkData(final boolean shouldBlockNetworkData) {
        /*
         * Block network loads.
         *
         * Images with content: URIs will not be blocked, nor
         * will network images that are already in the WebView cache.
         *
         */
        getSettings().setBlockNetworkLoads(shouldBlockNetworkData);
    }


    /**
     * Configure a {@link android.webkit.WebView} to display a Message. This method takes into account a user's
     * preferences when configuring the view. This message is used to view a message and to display a message being
     * replied to.
     */
    public void configure() {
        this.setVerticalScrollBarEnabled(true);
        this.setVerticalScrollbarOverlay(true);
        this.setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
        this.setLongClickable(true);

        if (MailChat.getK9MessageViewTheme() == MailChat.Theme.DARK) {
            // Black theme should get a black webview background
            // we'll set the background of the messages on load
            this.setBackgroundColor(0xff000000);
        }

        final WebSettings webSettings = this.getSettings();

        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);
        if (MailChat.autofitWidth()) {
            webSettings.setLoadWithOverviewMode(true);
        }

        disableDisplayZoomControls();

        webSettings.setJavaScriptEnabled(false);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        // TODO:  Review alternatives.  NARROW_COLUMNS is deprecated on KITKAT
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);

        setOverScrollMode(OVER_SCROLL_NEVER);

        webSettings.setTextZoom(MailChat.getFontSizes().getMessageViewContentAsPercent());

        // Disable network images by default.  This is overridden by preferences.
        blockNetworkData(true);
    }

    /**
     * Disable on-screen zoom controls on devices that support zooming via pinch-to-zoom.
     */
    private void disableDisplayZoomControls() {
        PackageManager pm = getContext().getPackageManager();
        boolean supportsMultiTouch =
                pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH) ||
                pm.hasSystemFeature(PackageManager.FEATURE_FAKETOUCH_MULTITOUCH_DISTINCT);

        getSettings().setDisplayZoomControls(!supportsMultiTouch);
    }

    /**
     * Load a message body into a {@code MessageWebView}
     *
     * <p>
     * Before loading, the text is wrapped in an HTML header and footer
     * so that it displays properly.
     * </p>
     *
     * @param text
     *      The message body to display.  Assumed to be MIME type text/html.
     */
    public void setText(String text) {
     // Include a meta tag so the WebView will not use a fixed viewport width of 980 px
        String content = "<html><head><meta name=\"viewport\" content=\"width=device-width\"/>";
        if (MailChat.getK9MessageViewTheme() == MailChat.Theme.DARK)  {
            content += "<style type=\"text/css\">" +
                   "* { background: black ! important; color: #F3F3F3 !important }" +
                   ":link, :link * { color: #CCFF33 !important }" +
                   ":visited, :visited * { color: #551A8B !important }</style> ";
        }
        content += HtmlConverter.cssStylePre();
        content += "</head><body>" + text + "</body></html>";
        loadDataWithBaseURL("http://", content, "text/html", "utf-8", null);
        resumeTimers();
    }

    /*
     * Emulate the shift key being pressed to trigger the text selection mode
     * of a WebView.
     */
    public void emulateShiftHeld() {
        try {

            KeyEvent shiftPressEvent = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
                                                    KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0);
            shiftPressEvent.dispatch(this, null, null);
            Toast.makeText(getContext() , R.string.select_text_now, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(MailChat.LOG_TAG, "Exception in emulateShiftHeld()", e);
        }
    }
//	public interface SlideListener {
//
//		/**
//		 * 
//		 * @Description:
//		 * @param id
//		 * @see:
//		 * @since:
//		 * @author: 温楠
//		 * @date:2013-5-18
//		 */
//		public void onToLeftEdge();
//
//		/**
//		 * 
//		 * @Description:
//		 * @param id
//		 * @see:
//		 * @since:
//		 * @author: 温楠
//		 * @date:2013-5-18
//		 */
//		public void onToRightEdge();
//
//	}

//	public void setSlideListener(SlideListener mListener) {
//		this.slideListener = mListener;
//	}
}
