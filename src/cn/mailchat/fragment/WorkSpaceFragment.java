package cn.mailchat.fragment;

import java.net.URLDecoder;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.ChattingSingleActivity;
import cn.mailchat.activity.OALoginActivity;
import cn.mailchat.activity.WebViewWithErrorViewActivity;
import cn.mailchat.beans.AddrInfo;
import cn.mailchat.chatting.beans.DChat;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.controller.NotificationCenter;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.utils.BaiduLocationUtil;
import cn.mailchat.utils.SystemUtil;
import cn.mailchat.utils.Utility;
import cn.mailchat.view.webview.LoadingInterceptor;
import cn.mailchat.view.webview.WebViewContainerView;
import cn.mailchat.view.webview.WebViewStateListener;

/**
 * 
 * @author zhangjx 工作台
 * 
 */
public class WorkSpaceFragment extends Fragment {
	public static final String DRAWER_WORKSPACE_VIEW_TAG = "WorkSpaceFragment";
	private NotificationCenter notificationCenter;
	private WebViewContainerView mWebView;
	private Account mAccount;
	private boolean isOA;
	private String urlStr;
	private WorkSpaceFragmentListener mWorkSpaceFragmentListener;
	private boolean isOaError = false;

	public static WorkSpaceFragment newInstance(String url, String title,
			String accountUuid, boolean isOA) {
		WorkSpaceFragment fragment = new WorkSpaceFragment();
		Bundle args = new Bundle();
		args.putString(WebViewWithErrorViewActivity.EXTRA_Web_URL, url);
		args.putString(ChattingSingleActivity.ACCOUNTUUID, accountUuid);
		args.putString(WebViewWithErrorViewActivity.EXTRA_WEB_TITLE, title);
		args.putBoolean(WebViewWithErrorViewActivity.EXTRA_IS_OA, isOA);
		fragment.setArguments(args);
		return fragment;
	}

	MessagingListener listener = new MessagingListener() {
		/**
		 * 已经绑定了oa
		 * 
		 * @param account
		 */
		@Override
		public void alreadyBindOA(Account a) {
			if (mAccount.getEmail().equals(a.getEmail())) {
				OALoginActivity.actionLoginOA(getActivity(), mAccount);
				MobclickAgent.onEvent(getActivity(), "open_login_oa");
				getActivity().finish();
			}
		}
	};
	private MessagingController mController;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mWorkSpaceFragmentListener = (WorkSpaceFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.getClass()
					+ " must implement OaFragmentListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_webview, container,
				false);
		decodeExtras();
		initView(view);
		initData();
		initListener();
		return view;
	}

	private void initListener() {
		mWebView.addOnWebViewStateListener(new WebViewStateListener() {
			@Override
			public void onStartLoading(String url, Bitmap favicon) {

			}

			@Override
			public void onError(int errorCode, String description,
					String failingUrl) {

			}

			@Override
			public void onFinishLoaded(WebView view, String loadedUrl) {
				if (TextUtils.isEmpty(view.getTitle())) {
					mWorkSpaceFragmentListener.setWebViewTitle("无标题");
				} else {
					mWorkSpaceFragmentListener.setWebViewTitle(view.getTitle());
				}
			}

			@Override
			public void onProgressChanged(WebView view, int progress) {

			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		notificationCenter.notifyClean(mAccount);
	}

	private void decodeExtras() {
		Bundle bundle = getArguments();
		urlStr = bundle.getString(WebViewWithErrorViewActivity.EXTRA_Web_URL);
		String title = bundle
				.getString(WebViewWithErrorViewActivity.EXTRA_WEB_TITLE);
		isOA = bundle.getBoolean(WebViewWithErrorViewActivity.EXTRA_IS_OA,
				false);
		String mAccountUuid = bundle
				.getString(ChattingSingleActivity.ACCOUNTUUID);
		mAccount = Preferences.getPreferences(getActivity()).getAccount(
				mAccountUuid);
	}

	private void initData() {
		notificationCenter = NotificationCenter.getInstance();
		mController = MessagingController.getInstance(getActivity()
				.getApplication());
		if (isOA) {
			mWebView.addLoadingInterceptor(new MailChatJsSdkInterceptor());
			mWebView.addLoadingInterceptor(new MailChatOaErrorInterceptor());
			if (!mAccount.isOAUser()) {
				// 判断该账户是否有绑定OA
				mController.checkIsBindOA(mAccount, true, listener);
			}
		}
		mWebView.setInitialScale(getInitialScale());
		mWebView.requestFocus();
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		mWebView.addLoadingInterceptor(new UnsupportedProtcolInterceptor(
				getActivity()));
		loadUrl(urlStr);

		if (mAccount != null) {
			MessagingController.getInstance(getActivity().getApplication())
					.updateDChatReadState(
							mAccount,
							DChat.getDchatUid(mAccount.getEmail() + ","
									+ GlobalConstants.DCHAT_OA), 0, null);
		}

	}

	public void loadUrl(String url) {
		if (mWebView != null) {
			if (!url.startsWith("http")) {
				url = "http://" + url;
			}
			mWebView.loadUrl(url);
		}
	}

	private void initView(View view) {
		mWebView = (WebViewContainerView) view
				.findViewById(R.id.webview_error_view);
	}

	@Override
	public void onDestroy() {
		recycleWebView();
		super.onDestroy();
	}

	protected void recycleWebView() {
		if (mWebView != null) {
			mWebView.setVisibility(View.GONE);
			mWebView.removeAllViews();
			mWebView = null;
		}
	}

	private class UnsupportedProtcolInterceptor implements LoadingInterceptor {

		private Activity activity;

		public UnsupportedProtcolInterceptor(Activity activity) {
			this.activity = activity;
		}

		@Override
		public boolean validate(Uri uri) {
			return !uri.getScheme().equals("http")
					&& !uri.getScheme().equals("https")
					&& !uri.getScheme().equals("mc")
					&& !uri.getScheme().equals("javascript");
		}

		@Override
		public void exec(Uri uri) {
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			activity.startActivity(intent);
		}
	}

	public int getInitialScale() {
		// TODO 获取屏幕像素，找个统一调用的位置
		int screenDensity = getResources().getDisplayMetrics().densityDpi;
		int font = 0;
		switch (screenDensity) {
		case DisplayMetrics.DENSITY_LOW:
			font = -50;
			// webSettings.setDefaultZoom(ZoomDensity.CLOSE);
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			font = 0;
			// webSettings.setDefaultZoom(ZoomDensity.MEDIUM);
			break;
		case DisplayMetrics.DENSITY_HIGH:
			font = 0;
			// webSettings.setDefaultZoom(ZoomDensity.FAR);
			break;
		case DisplayMetrics.DENSITY_XHIGH:
			font = 80;
			// webSettings.setDefaultZoom(ZoomDensity.FAR);
			break;
		case 480:
			font = 120;
			break;
		}
		return font;

	}

	private class MailChatJsSdkInterceptor implements LoadingInterceptor {

		private final static String CALLBACK_PREFIX = "javascript:window.mc.mailchatBridge.resultForCallback(";
		private final static String CALLBACK_SUFFIX = ");";

		private final static String CALLBACK_TYPE_SUCCESS = "ok";
		private final static String CALLBACK_TYPE_FAILURE = "fail";

		private final static String FUNCTION_CHECK_JS_API = "checkJsApi";
		private final static String FUNCTION_GET_DEVICE_ID = "getDeviceId";
		private final static String FUNCTION_GET_LOCATION = "getLocation";
		private final static String FUNCTION_GET_AUTH_TOKEN = "getAuthToken";

		@Override
		public boolean validate(Uri uri) {
			return uri.getScheme().equals("mc");
		}

		@Override
		public void exec(Uri uri) {
			String param1 = null;
			String param2 = null;
			String param3 = null;

			try {
				String[] parts = uri.toString().split(":");
				param1 = parts[2];
				param2 = "\'" + CALLBACK_TYPE_SUCCESS +"\'";

				if (FUNCTION_CHECK_JS_API.equals(parts[1])) {

					String jsonString = URLDecoder.decode(parts[3], "UTF-8");
					JSONArray apiList = new JSONObject(jsonString)
							.getJSONArray("jsApiList");

					StringBuffer sb = new StringBuffer("[{");
					for (int i = 0; i < apiList.length(); i++) {
						String api = apiList.getString(i);
						if (i != 0) {
							sb.append(", \'");
						} else {
							sb.append("\'");
						}
						sb.append(api + "\':");

						if (FUNCTION_CHECK_JS_API.equals(api)
								|| FUNCTION_GET_DEVICE_ID.equals(api)
								|| FUNCTION_GET_LOCATION.equals(api)
								|| FUNCTION_GET_AUTH_TOKEN.equals(api)) {
							sb.append("true");
						} else {
							sb.append("false");
						}
					}
					sb.append("}]");

					param3 = sb.toString();

				} else if (FUNCTION_GET_DEVICE_ID.equals(parts[1])) {

					param3 = "[\'" + SystemUtil.getCliendId(MailChat.app)
							+ "\']";

				} else if (FUNCTION_GET_LOCATION.equals(parts[1])) {

					final String finalParam1 = param1;
					new BaiduLocationUtil(
							getActivity(),
							new BaiduLocationUtil.ReceiveBaiduLocationListener() {
								@Override
								public void onReceiveLocationSuccess(
										List<AddrInfo> list) {
									String param1 = finalParam1;
									String param2 = null;
									String param3 = null;
									if (list != null && list.size() > 0) {
										AddrInfo addrInfo = list.get(0);
										param2 = "\'" + CALLBACK_TYPE_SUCCESS + "\'";
										param3 = "[{" + "latitude:"
												+ addrInfo.getmLatitude()
												+ ", " + "longitude:"
												+ addrInfo.getmLongitude()
												+ "}]";
									} else {
										param2 = "\'" + CALLBACK_TYPE_FAILURE + "\'";
										param3 = "[\'" + "定位失败" + "\']";
									}
									if (param1 != null) {
										mWebView.loadUrl(CALLBACK_PREFIX
												+ param1 + ", " + param2 + ", "
												+ param3 + CALLBACK_SUFFIX);
									}
								}

								@Override
								public void onReceiveLocationFailed() {
									String param1 = finalParam1;
									String param2 = "\'" + CALLBACK_TYPE_FAILURE + "\'";
									String param3 = "[\'" + "定位失败" + "\']";
									if (param1 != null) {
										mWebView.loadUrl(CALLBACK_PREFIX
												+ param1 + ", " + param2 + ", "
												+ param3 + CALLBACK_SUFFIX);
									}
								}
							});
					return;

				} else if (FUNCTION_GET_AUTH_TOKEN.equals(parts[1])) {

				    param3 = "[\'" + Utility.getOAUserParam(mAccount)
                            + "\']";

				} else {
					throw new MessagingException("不支持的MailChat JS SDK指令");
				}
			} catch (Exception e) {
				if (param1 != null) {
					param2 = "\'" + CALLBACK_TYPE_FAILURE + "\'";
					param3 = "[\'" + MailChat.getRootCauseMessage(e) + "\']";
				}
				Log.e(MailChat.LOG_TAG, "MailChat JS SDK指令调用失败", e);
			}

			if (param1 != null) {
				mWebView.loadUrl(CALLBACK_PREFIX + param1 + ", " + param2
						+ ", " + param3 + CALLBACK_SUFFIX);
			}
		}

	}

	private class MailChatOaErrorInterceptor implements LoadingInterceptor {

		@Override
		public boolean validate(Uri uri) {
			// 如果用户解除绑定后，清空本地数据
			if (uri.toString().endsWith("mailchaterror.html")) {
				isOaError = true;
				mAccount.setoAHost(null);
				mAccount.setoAEmail(null);
				mAccount.setOAUser(false);
				mAccount.setBindOA(false);
				mAccount.setBindOAUser(false);
				mAccount.save(Preferences.getPreferences(MailChat.application));
			}
			return false;
		}

		@Override
		public void exec(Uri uri) {
			// TODO Auto-generated method stub

		}

	}

	public interface WorkSpaceFragmentListener {
		void setWebViewTitle(String title);
	}
}
