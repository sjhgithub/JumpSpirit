package cn.mailchat.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.Toast;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.utils.NetUtil;
import cn.mailchat.view.FitWidthImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
/**
 * 
 * @copyright © 35.com
 * @file name ：BigImageShowerActivity.java
 * @author ：zhangjx
 * @create Data ：2015-5-13下午1:58:54 
 * @Current Version：v1.0 
 * @History memory :
 * @Date : 2015-5-13下午1:58:54 
 * @Modified by：zhangjx
 * @Description :
 */
public class BigImageShowerActivity extends MailChatActivity {

	private static final String EXTRA_IMAGE_URL = "img_url";
	private static final String EXTRA_NICKNAME = "user_name";

	public static void newInstance(Context context, String imgUrl,
			String nickname) {
		Intent intent = new Intent(context, BigImageShowerActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(EXTRA_IMAGE_URL, imgUrl);
		intent.putExtra(EXTRA_NICKNAME, nickname);
		context.startActivity(intent);
	}

	private FitWidthImageView imgUserHead;
	private DisplayImageOptions options;

	@Override
	public void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_big_img_shower);
		initView();
		initImageLoader();
		initData();
	}

	private void initData() {
		Intent intent = getIntent();
		String userHeadUrl = intent.getStringExtra(EXTRA_IMAGE_URL);
		String nickName = intent.getStringExtra(EXTRA_NICKNAME);
		if (!StringUtils.isNullOrEmpty(userHeadUrl)) {
			if (!NetUtil.isActive()) {
				Toast.makeText(BigImageShowerActivity.this,
						getString(R.string.item_net_title), Toast.LENGTH_SHORT)
						.show();
				return;
			}
			ImageLoader.getInstance().displayImage(userHeadUrl, imgUserHead,
					options);
		} else {
			imgUserHead.setImageBitmap(ImageUtils.getUserFirstTextBitmap(this,
					nickName));
		}
	}

	private void initView() {
		imgUserHead = (FitWidthImageView) findViewById(R.id.img_user_head);

	}

	private void initImageLoader() {
		options =MailChat.getInstance().initImageLoaderOptions();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return true;
	}
}
