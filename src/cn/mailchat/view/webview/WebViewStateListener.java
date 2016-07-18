package cn.mailchat.view.webview;

import android.graphics.Bitmap;
import android.webkit.WebView;

public interface WebViewStateListener {

    public void onStartLoading(String url, Bitmap favicon);

    public void onError(int errorCode, String description, String failingUrl);

    public void onFinishLoaded(WebView view,String loadedUrl);

    public void onProgressChanged(WebView view, int progress);

}
