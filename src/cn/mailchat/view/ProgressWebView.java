package cn.mailchat.view;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.mailchat.R;

/**
 * 
 * @copyright © 35.com
 * @file name ：ProgressWebView.java
 * @author ：zhangjx
 * @create Data ：2014-8-11下午7:35:20
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-8-11下午7:35:20
 * @Modified by：zhangjx
 * @Description :自定义头部进度条的webView
 */

public class ProgressWebView extends WebView {

	private ProgressBar progressbar;
	private TextView textView;

	@SuppressWarnings("deprecation")
	public ProgressWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		progressbar = new ProgressBar(context, null,
				android.R.attr.progressBarStyleHorizontal);
		progressbar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				6, 0, 0));
		// 设置进度条颜色
		progressbar.setProgressDrawable(getResources().getDrawable(
				R.drawable.web_bar_color));
		addView(progressbar);
		// setWebViewClient(new WebViewClient(){});
		setWebChromeClient(new WebChromeClient());
	}

	public class WebChromeClient extends android.webkit.WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int newProgress) {

			if (newProgress == 100) {
				progressbar.setVisibility(GONE);

			} else {
				if (progressbar.getVisibility() == GONE)
					progressbar.setVisibility(VISIBLE);
				progressbar.setProgress(newProgress);
			}
			super.onProgressChanged(view, newProgress);
		}

		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
			getTextView().setText(title);
		}
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		LayoutParams lp = (LayoutParams) progressbar.getLayoutParams();
		lp.x = l;
		lp.y = t;
		progressbar.setLayoutParams(lp);
		super.onScrollChanged(l, t, oldl, oldt);
	}

	public TextView getTextView() {
		return textView;
	}

	public void setTextView(TextView textView) {
		this.textView = textView;
	}

}