package cn.mailchat.fragment;

import java.io.File;
import java.io.Serializable;

import com.umeng.analytics.MobclickAgent;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnViewTapListener;

import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.ImageFullActivity;
import cn.mailchat.chatting.beans.CAttachment;
import cn.mailchat.chatting.beans.CMessage;
import cn.mailchat.chatting.beans.DAttachment;
import cn.mailchat.chatting.beans.DChatMessage;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.utils.ImageUtils;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ImageFullFragment extends Fragment {
	private ImageView backgroundImageView;
	private ImageView mImageView;
	private PhotoViewAttacher mAttacher;
	private ProgressBar progressBar;
	private MessagingController controller;

	private String bigImageUrl;
	// 群聊进入
	private CMessage cmessage;
	private CAttachment cAttachment;
	// 单聊进入
	private DChatMessage dmessage;
	private DAttachment dAttachment;
	private String attachmentId;
	private Handler handler = new Handler();
	private boolean isCMessage;
	private Account account;
	private LruCache<String, Bitmap> imageCache;
	public static ImageFullFragment newInstance(Serializable message,
			boolean isCMessage, String accountUid) {
		ImageFullFragment f = new ImageFullFragment();
		Bundle args = new Bundle();
		args.putSerializable("message",message);
		args.putBoolean("isCMessage", isCMessage);
		args.putString("accountUid", accountUid);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		controller = MessagingController.getInstance(getActivity()
				.getApplication());
		controller.addListener(listener);
		imageCache=MailChat.application.getmMemoryCache();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_image_full, container,
				false);
		initView(view);
		initData();
		return view;
	}

	private void initView(View view) {
		mImageView = (ImageView) view.findViewById(R.id.tiv_big);
		mAttacher = new PhotoViewAttacher(mImageView);
		progressBar = (ProgressBar) view.findViewById(R.id.process_imageonline);
		backgroundImageView = (ImageView) view
				.findViewById(R.id.iv_full_image_background);
		mAttacher.setOnViewTapListener(new OnViewTapListener() {

			@Override
			public void onViewTap(View view, float x, float y) {
				// TODO Auto-generated method stub
				if (ImageFullActivity.titleLayout != null) {
					if (ImageFullActivity.titleLayout.getVisibility() == View.VISIBLE) {
						ImageFullActivity.setTitleHideAnimation();
					} else {
						ImageFullActivity.setTitleShowAnimation();
					}
				}
			}
		});
	}

	private void initData() {
		Bundle args = getArguments();
		account = Preferences.getPreferences(getActivity()).getAccount(
				args.getString("accountUid"));
		isCMessage=args.getBoolean("isCMessage");
		if (!isCMessage) {
			dmessage = (DChatMessage) args.getSerializable("message");
			dAttachment = dmessage.getAttachments().get(0);
			attachmentId = dAttachment.getAttchmentId();
			if (dAttachment.getLocalPathFlag() != 1) {
				bigImageUrl = MailChat.application.getAttFilePath(MailChat.application.getChatImageCacheDirectory(account), attachmentId, dAttachment.getName());
			} else {
				bigImageUrl = dAttachment.getFilePath();
			}
			File dFile = new File(bigImageUrl);
			if (!dFile.exists()) {
				controller.dChatDownFile(account, dmessage);
			} else if (dFile.length() == dAttachment.getSize()
					|| dAttachment.getLocalPathFlag() == 1) {
				progressBar.setVisibility(View.GONE);
				backgroundImageView.setVisibility(View.GONE);
				mImageView.setImageBitmap(getImage(attachmentId));
				mAttacher.update();
			}
		} else {
			cmessage = (CMessage) args.getSerializable("message");
			cAttachment = cmessage.getAttachment();
			attachmentId = cAttachment.getAttchmentId();
			if (cAttachment.getLocalPathFlag() != 1) {
				bigImageUrl = MailChat.application.getAttFilePath(MailChat.application.getChatImageCacheDirectory(account), attachmentId, cAttachment.getName());
			} else {
				bigImageUrl = cAttachment.getFilePath();
			}
			File cFile = new File(bigImageUrl);
			if (!cFile.exists()) {
				controller.cGroupDownFile(account, cmessage);
			} else if (cFile.length() == cAttachment.getSize()
					|| cAttachment.getLocalPathFlag() == 1) {
				progressBar.setVisibility(View.GONE);
				backgroundImageView.setVisibility(View.GONE);
				mImageView.setImageBitmap(getImage(attachmentId));
				mAttacher.update();
			}
		}
	}

	private MessagingListener listener = new MessagingListener() {
		public void fileDownloadProgress(Account acc, String id, final int progress) {
			if (account.getUuid().equals(acc.getUuid())
					&& attachmentId.equals(id)) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						progressBar.setVisibility(View.VISIBLE);
						backgroundImageView.setVisibility(View.VISIBLE);
						progressBar.setProgress(progress);
					}
				});
			}
		};

		public void fileDownloadFinished(Account acc, String id) {
			if (account.getUuid().equals(acc.getUuid())
					&& attachmentId.equals(id)) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						mImageView.setImageBitmap(getImage(attachmentId));
						mAttacher.update();//设置图片后更新，否则会出现图片位置不对问题
						progressBar.setVisibility(View.GONE);
						backgroundImageView.setVisibility(View.GONE);
					}
				});

			}
		};

		public void fileDownloadFailed(Account acc, String id) {
			if (account.getUuid().equals(acc.getUuid())
					&& attachmentId.equals(id)) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						backgroundImageView.setVisibility(View.VISIBLE);
						progressBar.setProgress(0);
						progressBar.setVisibility(View.VISIBLE);
					}
				});
			}
		};
	};

	public void onDestroyView() {
		super.onDestroyView();
		controller.removeListener(listener);
	};
	
	public Bitmap getImage(String imageUid){
		Bitmap bitmap = imageCache.get(imageUid);
		if(bitmap==null){
			bitmap = ImageUtils.getNativeImage(bigImageUrl,true);
			if(bitmap!=null){
				imageCache.put(imageUid, bitmap);
			}
		}
		return bitmap;
	}
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("ImageFullActivity"); //统计页面
	}
	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("ImageFullActivity"); //统计页面
	}
}
