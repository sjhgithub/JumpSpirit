package cn.mailchat.activity.base;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.activity.MailChatActivity;
import cn.mailchat.activity.setup.AccountSetupNameActivity;
import cn.mailchat.utils.FileUtil;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.view.DialogSetUpUserInfo;

public class BaseSetUpUserHeadImgActivity extends MailChatActivity {
	protected int width;
	protected Uri imgUri;
	protected File mUserBigHeadFile;
	protected static final int PICK_FROM_CAMERA = 1;
	protected static final int CROP_FROM_CAMERA = 2;
	protected static final int PICK_FROM_FILE = 3;
	protected static final int HANDLER_LOAING_MSG_START = 0;
	protected static final int HANDLER_LOAING_MSG_SUCCESS = 1;
	protected static final int HANDLER_LOAING_MSG_FAILED = 2;
	protected static final int HANDLER_UPLOAD_MSG_START = 3;
	protected static final int HANDLER_UPLOAD_MSG_SUCCESS = 4;
	protected static final int HANDLER_UPLOAD_MSG_FAILED = 5;
	protected String bigHeadPath;
	protected DisplayImageOptions options;
	protected File protraitFile;
	protected ProgressDialog mDialog;
	protected MyHandler mHandler;
	// 是否生成首字母头像
	protected boolean isCreateDefaultHeadImg = true;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mDialog =new ProgressDialog(this);
		mDialog.setCancelable(true);
		mHandler=new MyHandler();
	}

	public class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case HANDLER_LOAING_MSG_START:
				mDialog.setMessage(getString(R.string.account_setup_check_settings_retr_info_msg));
				mDialog.show();
				break;
			case HANDLER_LOAING_MSG_SUCCESS:
				mDialog.hide();
				break;
			case HANDLER_LOAING_MSG_FAILED:
				mDialog.hide();
				Toast.makeText(BaseSetUpUserHeadImgActivity.this,
						getString(R.string.load_failed), Toast.LENGTH_SHORT)
						.show();
				break;
			case HANDLER_UPLOAD_MSG_START:
				mDialog.setMessage(getString(R.string.uploading_img_head));
				mDialog.show();
				break;
			case HANDLER_UPLOAD_MSG_SUCCESS:
				mDialog.hide();
				break;
			case HANDLER_UPLOAD_MSG_FAILED:
				mDialog.hide();
				Toast.makeText(BaseSetUpUserHeadImgActivity.this,
						getString(R.string.upload_failed), Toast.LENGTH_SHORT)
						.show();
				break;

			default:
				break;
			}
		}

	}
	protected void initImageLoader() {
		options = MailChat.getInstance().initImageLoaderOptions();
	}

	protected void getScWidth() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
	}
	protected void startActionCrop(Account account,Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("output", getUploadTempFile(account,uri));
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);// 裁剪框比例
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 240);// 输出图片大小
		intent.putExtra("outputY", 240);
		intent.putExtra("scale", true);// 去黑边
		intent.putExtra("scaleUpIfNeeded", true);// 去黑边
		startActivityForResult(intent, CROP_FROM_CAMERA);
	}
	/**
	 * 
	 * method name: getUploadTempFile
	 * function @Description: TODO
	 * Parameters and return values description:
	 *      @param uri
	 *      @return   field_name
	 *      Uri   return type
	 *  @History memory：
	 *     @Date：2015-7-8 下午2:10:36	@Modified by：zhangjx
	 *     @Description:裁剪头像的绝对路径
	 */
	@SuppressLint("SimpleDateFormat")
	private Uri getUploadTempFile(Account account,Uri uri) {
		String imgTempPath = MailChat.application
				.getUserBigImageCacheDirectory(account);
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(new Date());
		String thePath = ImageUtils.getAbsolutePathFromNoStandardUri(uri);
		// 如果是标准Uri
		if (StringUtil.isEmpty(thePath)) {
			thePath = ImageUtils.getAbsoluteImagePath(BaseSetUpUserHeadImgActivity.this, uri);
		}
		String ext = FileUtil.getFileFormat(thePath);
		ext = StringUtil.isEmpty(ext) ? "jpg" : ext;
		// 照片命名
		String cropFileName = "mailchat_crop_" + timeStamp + "." + ext;
		// 裁剪头像的绝对路径
		bigHeadPath = imgTempPath + cropFileName;
		protraitFile = new File(bigHeadPath);
		Uri cropUri = Uri.fromFile(protraitFile);
		return cropUri;
	}
	// protected void customChoseImgDialog(final Account account) {
	// final DialogSetUpUserInfo dialog=createDialog();
	// dialog.findViewById(R.id.infoset_dialog_gallery).setOnClickListener(
	// new OnClickListener() {
	// @Override
	// public void onClick(View v) {
	// // 跳转至图库
	// jumpToDCIM();
	// dialog.dismiss();
	// }
	// });
	//
	// dialog.findViewById(R.id.infoset_dialog_camera).setOnClickListener(
	// new OnClickListener() {
	// @Override
	// public void onClick(View v) {
	// // 跳转至相机拍摄
	// jumpToTakePhoto(account);
	// dialog.dismiss();
	// }
	// });
	// }
	// protected void customChoseServerSettingDialog(final Account account) {
	// final DialogSetUpUserInfo dialog=createDialog();
	// TextView tvInComing=(TextView)
	// dialog.findViewById(R.id.infoset_dialog_gallery);
	// TextView
	// tvOutComing=(TextView)dialog.findViewById(R.id.infoset_dialog_camera);
	// tvInComing.setText(getString(R.string.account_setup_incoming_title));
	// tvOutComing.setText(getString(R.string.account_setup_outgoing_title));
	// tvInComing.setOnClickListener(
	// new OnClickListener() {
	// @Override
	// public void onClick(View v) {
	// // 跳转至收件服务器设置页
	// Intent intent =
	// AccountSetupIncoming.intentActionEditIncomingSettings(BaseSetUpUserHeadImgActivity.this,
	// account);
	// intent.putExtra(MailChat.EXTRA_ACCOUNT_UPDATE, true);
	// startActivity(intent);
	// dialog.dismiss();
	// }
	// });
	//
	// tvOutComing.setOnClickListener(
	// new OnClickListener() {
	// @Override
	// public void onClick(View v) {
	// // 跳转至发件服务器设置页
	// Intent intent =
	// AccountSetupOutgoing.intentActionEditOutgoingSettings(BaseSetUpUserHeadImgActivity.this,
	// account);
	// intent.putExtra(MailChat.EXTRA_ACCOUNT_UPDATE, true);
	// startActivity(intent);
	// dialog.dismiss();
	// }
	// });
	// }
	private DialogSetUpUserInfo createDialog() {
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
		final DialogSetUpUserInfo dialog = new DialogSetUpUserInfo(this,
				R.style.dialog);
		dialog.setContentView(R.layout.dialog_setup_user_info);
		if (isKitKat) {
			LinearLayout layout = (LinearLayout) dialog
					.findViewById(R.id.infoset_show_dialog_root);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layout.setLayoutParams(params);
			layout.setGravity(Gravity.CENTER);
		} else {
			LinearLayout layout = (LinearLayout) dialog
					.findViewById(R.id.infoset_show_dialog_root);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					width / 2, LayoutParams.WRAP_CONTENT);
			layout.setLayoutParams(params);
			layout.setGravity(Gravity.CENTER);
		}
		dialog.show();
		return dialog;
	}

	public void jumpToDCIM() {
		// 方式1，直接打开图库，只能选择图库的图片
		// Intent i = new Intent(Intent.ACTION_PICK,
		// MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		// 方式2，会先让用户选择接收到该请求的APP，可以从文件系统直接选取图片
		Intent intent;
		if (Build.VERSION.SDK_INT < 19) {
			intent = new Intent();
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			startActivityForResult(Intent.createChooser(intent,
					getString(R.string.info_set_gallery)), PICK_FROM_FILE);
		} else {
			intent = new Intent(Intent.ACTION_PICK,
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			intent.setType("image/*");
			startActivityForResult(Intent.createChooser(intent,
					getString(R.string.info_set_gallery)), PICK_FROM_FILE);
		}
	}

	public void jumpToTakePhoto(Account account) {
		bigHeadPath = MailChat.application
				.getUserBigImageCacheDirectory(account);
		mUserBigHeadFile = new File(bigHeadPath,
				FileUtil.getCameraFilePngName());
		imgUri = Uri.fromFile(mUserBigHeadFile);
		Intent intentTakePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intentTakePicture.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
		startActivityForResult(intentTakePicture, PICK_FROM_CAMERA);
	}

	public class CropOption {
		public CharSequence title;
		public Drawable icon;
		public Intent appIntent;
	}
}
