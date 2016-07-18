package cn.mailchat.view.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ZoomButtonsController;

import java.util.ArrayList;
import java.util.List;

import cn.mailchat.R;

public class WebViewWithErrorView extends WebView {

	private WebViewState state = WebViewState.STOP;

	private List<WebViewStateListener> webViewStateListeners = new ArrayList<>();

	private List<LoadingInterceptor> loadingInterceptors = new ArrayList<>();

	public WebViewWithErrorView(Context context) {
		super(context);
		initialize();
	}

	public WebViewWithErrorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(attrs);
	}

	public WebViewWithErrorView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initialize(attrs);
	}

	public void addOnWebViewStateListener(
			WebViewStateListener webViewStateListener) {
		webViewStateListeners.add(webViewStateListener);
	}

	private void initialize() {
		setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		setWebViewClient(new WebServiceViewClient());
		setWebChromeClient(new WebServiceChromeClient());
	}

	private void initialize(AttributeSet attrs) {
		TypedArray args = getContext().obtainStyledAttributes(attrs,
				R.styleable.wv);
		setupWebSettings(args);
		args.recycle();
		setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		setWebViewClient(new WebServiceViewClient());
		setWebChromeClient(new WebServiceChromeClient());
	}

	@SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
	protected void setupWebSettings(TypedArray args) {
		boolean allowContentAccess = args.getBoolean(
				R.styleable.wv_allow_content_access, true);
		boolean allowFileAccess = args.getBoolean(
				R.styleable.wv_allow_file_access, true);
		boolean allowFileAccessFromFileURLs = args.getBoolean(
				R.styleable.wv_allow_file_access_from_file_urls, true);
		boolean allowUniversalAccessFromFileURLs = args.getBoolean(
				R.styleable.wv_allow_universal_access_from_file_urls, false);
		boolean appCacheEnabled = args.getBoolean(
				R.styleable.wv_app_cache_enabled, false);
		boolean blockNetworkImage = args.getBoolean(
				R.styleable.wv_block_network_image, false);
		boolean blockBlockNetworkLoads = args.getBoolean(
				R.styleable.wv_block_network_loads, false);
		boolean builtInZoomControls = args.getBoolean(
				R.styleable.wv_built_in_zoom_controls, false);
		int cacheMode = args.getInt(R.styleable.wv_cache_mode,
				WebSettings.LOAD_DEFAULT);
		boolean databaseEnabled = args.getBoolean(
				R.styleable.wv_database_enabled, false);
		boolean displayZoomControls = args.getBoolean(
				R.styleable.wv_display_zoom_controls, false);
		boolean domStorageEnabled = args.getBoolean(
				R.styleable.wv_dom_storage_enabled, false);
		boolean geolocationEnabled = args.getBoolean(
				R.styleable.wv_geolocation_enabled, true);
		boolean javaScriptCanOpenWindowsAutomatically = args.getBoolean(
				R.styleable.wv_java_script_can_open_windows_automatically,
				false);
		boolean jsEnabled = args.getBoolean(R.styleable.wv_java_script_enabled,
				false);
		boolean loadWithOverviewMode = args.getBoolean(
				R.styleable.wv_load_with_overview_mode, false);
		boolean loadsImagesAutomatically = args.getBoolean(
				R.styleable.wv_loads_images_automatically, true);
		boolean needInitialFocus = args.getBoolean(
				R.styleable.wv_need_initial_focus, false);
		boolean saveFormEnabled = args.getBoolean(
				R.styleable.wv_save_form_data, true);
		boolean supportMultipleWindows = args.getBoolean(
				R.styleable.wv_support_multiple_windows, false);
		boolean supportZoom = args
				.getBoolean(R.styleable.wv_support_zoom, true);
		boolean useWideViewPort = args.getBoolean(
				R.styleable.wv_use_wide_view_port, true);

		WebSettings setting = getSettings();
		setting.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
		setting.setAllowContentAccess(allowContentAccess);
		setting.setAllowFileAccess(allowFileAccess);
		setting.setAllowFileAccessFromFileURLs(allowFileAccessFromFileURLs);
		setting.setAllowUniversalAccessFromFileURLs(allowUniversalAccessFromFileURLs);
		setting.setAppCacheEnabled(appCacheEnabled);
		setting.setBlockNetworkImage(blockNetworkImage);
		setting.setBlockNetworkLoads(blockBlockNetworkLoads);
		setting.setBuiltInZoomControls(builtInZoomControls);
		setting.setCacheMode(cacheMode);
		setting.setDatabaseEnabled(databaseEnabled);
		// 设置出现缩放工具
		if (Build.VERSION.SDK_INT >= 11) {
			setting.setDisplayZoomControls(displayZoomControls);
		} else {
			ZoomButtonsController zbc = new ZoomButtonsController(this);
			zbc.getZoomControls().setVisibility(View.GONE);
		}
		setting.setDomStorageEnabled(domStorageEnabled);
		setting.setGeolocationEnabled(geolocationEnabled);
		setting.setJavaScriptCanOpenWindowsAutomatically(javaScriptCanOpenWindowsAutomatically);
		setting.setJavaScriptEnabled(jsEnabled);
		// 充满全屏
		setting.setLoadWithOverviewMode(loadWithOverviewMode);
		setting.setLoadsImagesAutomatically(loadsImagesAutomatically);
		setting.setNeedInitialFocus(needInitialFocus);
		setting.setSaveFormData(saveFormEnabled);
		setting.setSupportMultipleWindows(supportMultipleWindows);
		setting.setSupportZoom(supportZoom);
		// 让浏览器支持用户自定义view
		setting.setUseWideViewPort(useWideViewPort);
		setting.setDefaultFontSize(20);
	}

	public void addLoadingInterceptor(LoadingInterceptor loadingInterceptor) {
		this.loadingInterceptors.add(loadingInterceptor);
	}

	private class WebServiceChromeClient extends WebChromeClient {

		@Override
		public void onProgressChanged(WebView view, int progress) {
			super.onProgressChanged(view, progress);
			if (state == WebViewState.LOADING) {
				for (WebViewStateListener listener : webViewStateListeners) {
					listener.onProgressChanged(view, progress);
				}
			}
		}
	}

	private class WebServiceViewClient extends WebViewClient {

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			if (state == WebViewState.STOP) {
				state = WebViewState.LOADING;
				for (WebViewStateListener listener : webViewStateListeners) {
					listener.onStartLoading(url, favicon);
				}
			}
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			state = WebViewState.ERROR;
			for (WebViewStateListener listener : webViewStateListeners) {
				listener.onError(errorCode, description, failingUrl);
			}
		}

	    @Override
	    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
	        // GeoTrust的证书默认不被信任，屏蔽特定页面的证书检查
	        if (error != null && (
	                error.getUrl().equals("https://a.mailchat.cn/static/mcpage/EmptyMailList.html")
	                || error.getUrl().equals("https://mail.35.com/guide/"))) {
	            handler.proceed();
	        } else {
	            super.onReceivedSslError(view, handler, error);
	        }
	    }

		@Override
		public void onPageFinished(WebView view, String loadedUrl) {
			super.onPageFinished(view, loadedUrl);
			if (state == WebViewState.LOADING) {
				for (WebViewStateListener listener : webViewStateListeners) {
					listener.onProgressChanged(view, 100);
					listener.onFinishLoaded(view, loadedUrl);
				}
			}
			state = WebViewState.STOP;
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String loadingUrl) {
			if (loadingUrl == null || loadingInterceptors == null) {
				return false;
			}
			return intercept(Uri.parse(loadingUrl));
		}

	}

	@Override
	public void loadUrl(String url) {
//		if (intercept(Uri.parse(url))) {
//			return;
//		}
		super.loadUrl(url);
	}

	private boolean intercept(Uri uri) {
		for (LoadingInterceptor loadingInterceptor : loadingInterceptors) {
			if (loadingInterceptor.validate(uri)) {
				loadingInterceptor.exec(uri);
				return true;
			}
		}
		return false;
	}

}
