package cn.mailchat.view;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.mailchat.R;
import cn.mailchat.activity.misc.Attachment;
import cn.mailchat.utils.AttachmentUtil;
import cn.mailchat.utils.FileUtil;

/**
 * 
 * 详情页某个附件的view
 * 
 * @Description:
 * @author: zhuanggy
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-8-29
 */

public class AttachmentItemOfMessageComposeView extends LinearLayout {

	private Context mContext;

	private ViewGroup mMainView;

	private ImageView imgIcon;
	private ImageView imgDelete;
	private TextView textName;
	private TextView textInfo;

	private Attachment mAttachment;
	private View progressBar; 
	private AttachmentRemovedListener mListener;
	public AttachmentItemOfMessageComposeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public AttachmentItemOfMessageComposeView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		if (mContext == null) {
			mContext = context;
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			mMainView = (ViewGroup) inflater.inflate(R.layout.message_compose_attachments, null);
			imgIcon = (ImageView) mMainView.findViewById(R.id.attachment_img);
			textName = (TextView) mMainView.findViewById(R.id.attachment_name);
			textInfo = (TextView) mMainView.findViewById(R.id.attachment_size);
			imgDelete = (ImageView) mMainView.findViewById(R.id.attachment_close);
			 progressBar = mMainView.findViewById(R.id.progressBar);
		}
	}

	/**
	 * 显示某个附件
	 * 
	 * @Description:
	 * @param attachment
	 * @param showChildViewIfContent
	 *            是否显示压缩包里的附件
	 * @param util
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-9-3
	 */
	public void setView(boolean hasMetadata,boolean isLoadingComplete ,Attachment attachment) {
		this.mAttachment = attachment;
		if (hasMetadata) {
			textName.setText(attachment.name);
		} else {
			textName.setText(R.string.loading_attachment);
		}
		progressBar.setVisibility(isLoadingComplete ? View.GONE : View.VISIBLE);
		imgIcon.setImageBitmap(AttachmentUtil.getAttachmentIconForMessageCompose(mContext, mAttachment.name,uri2Path(mAttachment.uri)));
//		textName.setText(mAttachment.getName());
		textInfo.setText(FileUtil.sizeLongToString(mAttachment.size));
		imgDelete.setTag(mMainView);
		imgDelete.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 删除附件
				if (mListener != null) {
					mListener.onRemoved(v,mAttachment);
				}
			}
		});

		mMainView.setTag(attachment);
		this.addView(mMainView);
	}

	public void setOnAttachmentRemovedListener(AttachmentRemovedListener listener) {
		this.mListener = listener;
	}

	public interface AttachmentRemovedListener {

		public void onRemoved(View mMainView,Attachment attachment);
	}
	 public String uri2Path(Uri uri)
	    {
	       int actual_image_column_index;
	       String img_path;
	       String[] proj = { MediaStore.Images.Media.DATA };
	       Cursor cursor = mContext.getContentResolver().query(uri, proj, null, null, null);
	       actual_image_column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	       cursor.moveToFirst();
	       img_path = cursor.getString(actual_image_column_index);
	       return img_path;
	    }
}
