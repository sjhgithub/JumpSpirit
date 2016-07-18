package cn.mailchat.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.widget.Toast;

import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.helper.HtmlConverter;

public class MessageWebView extends RigidWebView {
	private DisplayFinish df;  
    public MessageWebView(Context context) {
        super(context);
    }

    public MessageWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MessageWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
        //双击webView放大,如果设置这两个方法会导致webView 出现一堆空白区域
		// webSettings.setUseWideViewPort(true);
		// if (MailChat.autofitWidth()) {
		// webSettings.setLoadWithOverviewMode(true);
		// }

        disableDisplayZoomControls();

        webSettings.setJavaScriptEnabled(false);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);

		// TODO: Review alternatives. NARROW_COLUMNS is deprecated on KITKAT
		 webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
		// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
		// webSettings.setLayoutAlgorithm(LayoutAlgorithm.TEXT_AUTOSIZING);
		// } else {
		// webSettings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
		// }
        setOverScrollMode(OVER_SCROLL_NEVER);

        webSettings.setTextZoom(MailChat.getFontSizes().getMessageViewContentAsPercent());
        int font = 0;
        int screenDensity=getResources().getDisplayMetrics().densityDpi;
		if (screenDensity <= DisplayMetrics.DENSITY_LOW) {
			font = -50;
		} else if (screenDensity > DisplayMetrics.DENSITY_LOW
				&& screenDensity <= DisplayMetrics.DENSITY_MEDIUM) {
			font = 0;
		} else if (screenDensity > DisplayMetrics.DENSITY_MEDIUM
				&& screenDensity <= DisplayMetrics.DENSITY_HIGH) {
			font = 0;
		} else if (screenDensity > DisplayMetrics.DENSITY_HIGH
				&& screenDensity <= DisplayMetrics.DENSITY_XHIGH) {
			font = 120;
		} else if (screenDensity > DisplayMetrics.DENSITY_XHIGH
				&& screenDensity <= DisplayMetrics.DENSITY_400) {
			font = 200;
		} else if (screenDensity > DisplayMetrics.DENSITY_400
				&& screenDensity <= DisplayMetrics.DENSITY_XXHIGH) {
			font = 240;
		} else if (screenDensity > DisplayMetrics.DENSITY_XXHIGH
				&& screenDensity < DisplayMetrics.DENSITY_XXXHIGH) {
			// 560DIP
			font = 280;
		} else if (screenDensity >= DisplayMetrics.DENSITY_XXXHIGH) {
			font = 320;
		}
		setInitialScale(font+MailChat.getFontSizes().getMessageViewContentAsPercent());
        // Disable network images by default.  This is overridden by preferences.
        blockNetworkData(true);
        
        setWebViewClient(MailChatWebViewClient.newInstance());
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
    
    @Override  
    protected void onDraw(Canvas canvas) {  
        super.onDraw(canvas);  
        df.After();  
    }  
    interface DisplayFinish{  
        void After();  
    }  
    public void setDf(DisplayFinish df) {  
        this.df = df;  
    }  
}
