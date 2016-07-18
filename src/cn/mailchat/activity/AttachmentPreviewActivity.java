package cn.mailchat.activity;

import java.util.Locale;

import com.umeng.analytics.MobclickAgent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.helper.SizeFormatter;
import cn.mailchat.mail.internet.MimeUtility;
import cn.mailchat.utils.WeemailUtil;
import cn.mailchat.view.AttachmentView;

public class AttachmentPreviewActivity extends BaseActionbarFragmentActivity {

    public static volatile AttachmentView sAttachmentView;

    private static final String TAG = "AttachmentPreviewActivity";
//    private static final String[] SUPPORTED_FILE_TYPES = {".txt", ".htm", ".html", ".doc",
//        ".docx", ".ppt", ".pptx", ".xls", ".xlsx", ".pdf", ".jpg", ".png", ".bmp", ".gif",
//        ".java", ".jsp", ".js", ".c", ".cpp", ".h", ".hpp", ".py", ".cs", ".sh", ".css",
//        ".rar", ".zip"};
    private static final String[] SUPPORTED_IMAGE_TYPES = {".jpg", ".png", ".bmp", ".gif"};

    private boolean mIsFullScreen;

    private TextView mTitleTextView;
    private ImageView mDownloadIcon;
    private ProgressBar mProgressIcon;
    private ImageView mOpenIcon;
    private ProgressBar mProgressBar;
    private WebView mWebView;
    private LinearLayout mErrorLayout;
    private TextView mErrorFilename;
    private TextView mErrorSize;
    private Button mRefreshButton;
    private Button mDownloadButton;
    private LinearLayout mInfoLayout;
    private TextView mInfoTitle;
    private TextView mInfoSubtitle;

    public static void actionPreview(Context context) {
        Intent intent = new Intent(context, AttachmentPreviewActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
        initView();
        initListener();
        initData();
    }

    @Override
    public void onBackPressed() {
        if (sAttachmentView != null) {
            sAttachmentView.mAttachmentPreviewActivity = null;
            sAttachmentView = null;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            if (sAttachmentView != null) {
                sAttachmentView.mAttachmentPreviewActivity = null;
                sAttachmentView = null;
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initActionBar() {
        mActionBar.setTitle(null);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayUseLogoEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        LayoutInflater inflater = LayoutInflater.from(this);
        RelativeLayout actionbarLayout = (RelativeLayout)inflater.inflate(
                R.layout.actionbar_attachment_preview,
                null);
        mActionBar.setCustomView(actionbarLayout);

        mTitleTextView = (TextView)actionbarLayout.findViewById(R.id.tv_title);
        mDownloadIcon = (ImageView)actionbarLayout.findViewById(R.id.icon_download);
        mProgressIcon = (ProgressBar)actionbarLayout.findViewById(R.id.icon_progress);
        mOpenIcon = (ImageView)actionbarLayout.findViewById(R.id.icon_open);
    }

    private void initView() {
        setContentView(R.layout.activity_attachment_preview);
        mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
        mWebView = (WebView)findViewById(R.id.web_view);
        mErrorLayout = (LinearLayout)findViewById(R.id.layout_error);
        mErrorFilename = (TextView)findViewById(R.id.text_view_error_filename);
        mErrorSize = (TextView)findViewById(R.id.text_view_error_size);
        mRefreshButton = (Button)findViewById(R.id.button_refresh);
        mDownloadButton = (Button)findViewById(R.id.button_download);
        mInfoLayout = (LinearLayout)findViewById(R.id.layout_info);
        mInfoTitle = (TextView)findViewById(R.id.text_view_info_title);
        mInfoSubtitle = (TextView)findViewById(R.id.text_view_info_subtitle);
    }

    private void initListener() {
        mRefreshButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                initData();
            }

        });

        mDownloadButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (sAttachmentView != null) {
                    sAttachmentView.open();
                }
            }

        });

        mDownloadIcon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (sAttachmentView != null) {
                    sAttachmentView.open();
                }
            }

        });

        mOpenIcon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (sAttachmentView != null) {
                    sAttachmentView.open();
                }
            }

        });

        mWebView.setOnTouchListener(new View.OnTouchListener() {

            private static final int MOVE_THRESHOLD_DP = 5;

            private float mDownPositionX;
            private float mDownPositionY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDownPositionX = event.getX();
                    mDownPositionY = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    if ((Math.abs(event.getX() - mDownPositionX) < MOVE_THRESHOLD_DP)
                            && (Math.abs(event.getY() - mDownPositionY) < MOVE_THRESHOLD_DP)) {
                        if (mIsFullScreen) {
                            mActionBar.show();
                            mInfoLayout.setVisibility(View.VISIBLE);
                            mIsFullScreen = false;
                        } else {
                            mActionBar.hide();
                            mInfoLayout.setVisibility(View.GONE);
                            mIsFullScreen = true;
                        }
                    }
                    break;
                }
                return false;
            }

        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setProgress(newProgress);
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }

        });

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && url.length() > 0 && !url.endsWith("/error/portfolio_error.html")) {
                    mWebView.loadUrl(url);
                } else {
                    Log.e(TAG, "文件类型不支持预览");
                    AttachmentPreviewActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mInfoTitle.setText(getString(R.string.attachment_preview_error_title));
                            mInfoSubtitle.setText(getString(R.string.attachment_preview_error_attachment_unsupported));
                            updateErrorUi(true, false, true);
                        }

                    });
                }
                return true;
            }

        });

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
    }

    private void initData() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mIsFullScreen = false;
                mTitleTextView.setText(getString(R.string.attachment_preview_title));
                mActionBar.show();
                mInfoLayout.setVisibility(View.VISIBLE);

                if (sAttachmentView == null) {
                    mInfoTitle.setText(getString(R.string.attachment_preview_error_title));
                    mInfoSubtitle.setText(getString(R.string.attachment_preview_error_attachment_null));
                    updateErrorUi(true, false, false);
                } else {
                    sAttachmentView.mAttachmentPreviewActivity = AttachmentPreviewActivity.this;
                    mInfoTitle.setText(sAttachmentView.mName);
                    mInfoSubtitle.setText(SizeFormatter.formatSize(AttachmentPreviewActivity.this,
                            sAttachmentView.mStatus != AttachmentView.Status.COMPLETE
                            ? MimeUtility.getDecodeBase64Size(sAttachmentView.mSize)
                            : sAttachmentView.mSize));
                    updateUi(sAttachmentView.mStatus);
                    updateErrorUi(false, false, false);
                    loadPreview();
                }
            }

        });

    }

    private void updateErrorUi(boolean isShow, boolean isShowRefresh, boolean isShowDownload) {
        if (isShow) {
            if (sAttachmentView == null) {
                mErrorFilename.setText(getString(R.string.attachment_preview_error_title));
                mErrorSize.setText(getString(R.string.attachment_preview_error_attachment_null));
            } else {
                mErrorFilename.setText(sAttachmentView.mName);
                mErrorSize.setText(SizeFormatter.formatSize(AttachmentPreviewActivity.this,
                        sAttachmentView.mStatus != AttachmentView.Status.COMPLETE
                        ? MimeUtility.getDecodeBase64Size(sAttachmentView.mSize)
                        : sAttachmentView.mSize));
            }

            if (isShowRefresh) {
                mRefreshButton.setVisibility(View.VISIBLE);
            } else {
                mRefreshButton.setVisibility(View.GONE);
            }

            if (isShowDownload) {
                mDownloadButton.setVisibility(View.VISIBLE);
            } else {
                mDownloadButton.setVisibility(View.GONE);
            }

            mErrorLayout.setVisibility(View.VISIBLE);
        } else {
            mErrorLayout.setVisibility(View.GONE);
        }
    }

    public void updateUi(AttachmentView.Status status) {
        switch (status) {
        case METADATA:
            mDownloadIcon.setVisibility(View.VISIBLE);
            mProgressIcon.setVisibility(View.GONE);
            mOpenIcon.setVisibility(View.GONE);
            mDownloadButton.setText(R.string.attachment_preview_download_button);
            break;
        case DOWNLOADING:
            mDownloadIcon.setVisibility(View.GONE);
            mProgressIcon.setVisibility(View.VISIBLE);
            mOpenIcon.setVisibility(View.GONE);
            mDownloadButton.setText(R.string.attachment_preview_downloading_button);
            break;
        case COMPLETE:
            mDownloadIcon.setVisibility(View.GONE);
            mProgressIcon.setVisibility(View.GONE);
            mOpenIcon.setVisibility(View.VISIBLE);
            mDownloadButton.setText(R.string.attachment_preview_open_button);
            break;
        default:
            mDownloadIcon.setVisibility(View.GONE);
            mProgressIcon.setVisibility(View.GONE);
            mOpenIcon.setVisibility(View.GONE);
            mDownloadButton.setText(R.string.attachment_preview_download_button);
        }
    }

    private void loadPreview() {
        new Thread() {

            @Override
            public void run() {
                try {
                    MobclickAgent.onEvent(AttachmentPreviewActivity.this, "cloud_service_preview_att");

                    String filename = sAttachmentView.mName;
                    boolean isSupportedImage = false;
                    if (filename != null && filename.length() > 1) {
                        filename = filename.trim().toLowerCase(Locale.US);
                        for (String suffix : SUPPORTED_IMAGE_TYPES) {
                            if (filename.endsWith(suffix)) {
                                isSupportedImage = true;
                                break;
                            }
                        }
                    }

                    final boolean isImage = isSupportedImage;
                    final String url = WeemailUtil.getC35PreviewUrl(sAttachmentView.mAccount,
                            sAttachmentView.mMessage,
                            sAttachmentView.mName);

                    AttachmentPreviewActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (isImage) {
                                mWebView.loadData("<html><body><img src=\"" + url + "\" /></body></html>", "text/html", null);
                            } else {
                                mWebView.loadUrl(url);
                            }
                        }

                    });

                    MobclickAgent.onEvent(AttachmentPreviewActivity.this, "cloud_service_preview_att_success");
                } catch (final com.c35.mtd.pushmail.exception.MessagingException e) {
                    Log.e(TAG, "预览附件失败", e);
                    AttachmentPreviewActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mInfoTitle.setText(getString(R.string.attachment_preview_error_title));
                            mInfoSubtitle.setText(MailChat.getRootCauseMessage(e));
                            updateErrorUi(true, true, true);
                        }

                    });
                }
            }

        }.start();
    }
}
