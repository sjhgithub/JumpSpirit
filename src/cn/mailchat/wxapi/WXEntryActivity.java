package cn.mailchat.wxapi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import cn.mailchat.GlobalConstants;
import cn.mailchat.R;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.mail.store.StorageManager;

import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;

/**
 * 微信回调的activity
 */
public class WXEntryActivity extends Activity {
    public static final String BUNDLE_KEY_OPENIDINFO = "bundle_key_openid_info";
	MessagingController mMessagingController;
	MessagingListener mCallback = new MessagingListener() {
        @Override
        public void weiXinShareStart() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    waitDialog.show();
                }
            });
        }

        @Override
        public void weiXinShareSucceed(String openid_info)  {
            Intent intent = new Intent(GlobalConstants.WECHAT);
            intent.putExtra(BUNDLE_KEY_OPENIDINFO, openid_info);
            sendBroadcast(intent);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(WXEntryActivity.this, getString(R.string.share_success), Toast.LENGTH_SHORT).show();
                }
            });
            finish();
        }

        @Override
        public void weiXinShareFailed() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    waitDialog.dismiss();
                }

            });
        }
    };
    private ProgressDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weixin_entry);
        mMessagingController = MessagingController.getInstance(getApplication());
		mMessagingController.addListener(mCallback);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        SendAuth.Resp resp = new SendAuth.Resp(intent.getExtras());
        if (resp.errCode == BaseResp.ErrCode.ERR_OK) {
            //用户同意
            String code = resp.code;
            String state = resp.state;
            // 如果不是登录
            if (!("wechat_login").equals(state)) {
                finish();
            }
            //上面的code就是接入指南里要拿到的code
            getAccessTokenAndOpenId(code);
        } else {
            finish();
        }
    }

    // 使用code获取微信的access_token和openid
    private void getAccessTokenAndOpenId(String code) {
        waitDialog = new ProgressDialog(WXEntryActivity.this);
        waitDialog.setMessage(getString(R.string.sharing));
        mMessagingController.shareToWeixin(code, mCallback);
    }
    @Override
    protected void onDestroy() {
    	mMessagingController.removeListener(mCallback);
    	super.onDestroy();
    }
}
