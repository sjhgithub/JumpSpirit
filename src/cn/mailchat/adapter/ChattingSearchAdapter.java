package cn.mailchat.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.mailchat.R;
import cn.mailchat.chatting.beans.CMessage;
import cn.mailchat.chatting.beans.DChatMessage;
import cn.mailchat.utils.AttachmentUtil;
import cn.mailchat.utils.FileUtil;
import cn.mailchat.utils.TimeUtils;

public class ChattingSearchAdapter extends BaseAdapter {

	Context mContext;
	LayoutInflater mInflater;
	List<CMessage> mMessages ;
	List<DChatMessage> mDchatMessages;
	List<String > dnickNameList;
	ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.BLUE);
	String mKeyWord;

	public ChattingSearchAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mMessages = new ArrayList<CMessage>();
		mDchatMessages = new ArrayList<DChatMessage>();
		dnickNameList=new ArrayList<String>();
	}

	public void showSearchResult(List<CMessage> messages,
			List<DChatMessage> dchatMessages,List<String > dnickNameList, String keyWord) {
		mMessages = messages;
		mDchatMessages = dchatMessages;
		mKeyWord = keyWord;
		this.dnickNameList=dnickNameList;
		// mPattern = Pattern.compile(mKeyWord);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		int size = 0;
		if (mMessages != null) {
			size = mMessages.size();
		} else {
			size = mDchatMessages.size();
		}
		return size;
	}

	@Override
	public int getViewTypeCount() {
		return 5;
	}

	@Override
	public int getItemViewType(int position) {
		cn.mailchat.chatting.beans.DChatMessage.Type dType;
		cn.mailchat.chatting.beans.CMessage.Type cType;
		int result = 0;
		if (mMessages != null) {
			cType = mMessages.get(position).getMessageType();

			switch (cType) {
			case TEXT:
				result = 0;
				break;
			case IMAGE:
				result = 1;
				break;
			case VOICE:
				result = 2;
				break;
			case ATTACHMENT:
				result = 3;
			case LOCATION:
				result = 4;
				break;
			case NOTIFICATION:
				break;
			}
		} else {
			dType = mDchatMessages.get(position).getMessageType();
			switch (dType) {
			case TEXT:
				result = 0;
				break;
			case IMAGE:
				result = 1;
				break;
			case VOICE:
				result = 2;
				break;
			case ATTACHMENT:
				result = 3;
			case LOCATION:
				result = 4;
				break;
			case NOTIFICATION:
				break;
			}
		}

		return result;
	}

	@Override
	public Object getItem(int position) {
		Object pos = 0;
		if (mMessages != null) {
			pos = mMessages.get(position);
		} else {
			pos = mDchatMessages.get(position);
		}
		return pos;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (mMessages != null) {
			CMessage message = mMessages.get(position);
			convertView = setCMessageViewData(convertView, parent, message);
		} else {
			DChatMessage message = mDchatMessages.get(position);
			convertView = setDMessageViewData(convertView, parent, message,dnickNameList.get(position));
		}
		return convertView;
	}

	private View setCMessageViewData(View convertView, ViewGroup parent,
			CMessage message) {
		if (convertView == null) {
			switch (message.getMessageType()) {
			case TEXT:
				convertView = mInflater.inflate(
						R.layout.chatting_search_item_text, parent, false);
				break;
			case IMAGE:
				convertView = mInflater.inflate(
						R.layout.chatting_search_item_image, parent, false);
				break;
			case VOICE:
				convertView = mInflater.inflate(
						R.layout.chatting_search_item_voice, parent, false);
				break;
			case ATTACHMENT:
				convertView = mInflater
						.inflate(R.layout.chatting_search_item_attachment,
								parent, false);
				break;
			case LOCATION:
				convertView = mInflater.inflate(
						R.layout.chatting_search_item_location, parent, false);
				break;
			case NOTIFICATION:
				break;
			}
			convertView.setTag(message.getMessageType());
		}
		TextView nameView = (TextView) convertView
				.findViewById(R.id.tv_chatting_search_item_member_name);
		switch (message.getMessageType()) {
		case TEXT:
			TextView textContentView = (TextView) convertView
					.findViewById(R.id.tv_chatting_search_text_content);
			highLightKeyWord(textContentView, message.getContent());
			break;
		case IMAGE:
			ImageView imageContentView = (ImageView) convertView
					.findViewById(R.id.iv_chatting_search_item_image_content);

			imageContentView.setImageBitmap(getNativeImage(message
					.getAttachment().getFilePath()));
			break;
		case VOICE:
			TextView voiceLengthView = (TextView) convertView
					.findViewById(R.id.tv_chatting_voice_length);
			int voiceLength = message.getAttachment().getVoiceLength();
			voiceLengthView.setText(String.valueOf(voiceLength) + "'");
			break;
		case ATTACHMENT:
			ImageView attIconView = (ImageView) convertView
					.findViewById(R.id.iv_chatting_search_item_attachment_icon);
			TextView attNameView = (TextView) convertView
					.findViewById(R.id.tv_chatting_search_item_attachment_name);
			TextView attSizeView = (TextView) convertView
					.findViewById(R.id.tv_chatting_search_item_attachment_size);
			attIconView
					.setImageBitmap(AttachmentUtil.getInstance(mContext)
							.getAttachmentIcon(
									message.getAttachment().getName(), false));
			highLightKeyWord(attNameView, message.getAttachment().getName());
			attSizeView.setText(FileUtil.sizeLongToString(message
					.getAttachment().getSize()));
			break;
		case LOCATION:
			TextView locationNameView = (TextView) convertView
					.findViewById(R.id.tv_chatting_search_item_location_name);
			locationNameView.setText(message.getContent());

			break;
		case NOTIFICATION:
			break;
		}
		String nickName = message.getMember().getNickName();
		if (nickName != null) {
			highLightKeyWord(nameView, nickName);
		} else {
			highLightKeyWord(nameView, message.getMember().getEmail());
		}
		TextView dateView = (TextView) convertView
				.findViewById(R.id.tv_chatting_search_item_date);
		dateView.setText(TimeUtils.toFriendly(new Date(message.getSendTime())));
		return convertView;
	}

	private View setDMessageViewData(View convertView, ViewGroup parent,
			DChatMessage message,String nickName) {
		if (convertView == null) {
			switch (message.getMessageType()) {
			case TEXT:
				convertView = mInflater.inflate(
						R.layout.chatting_search_item_text, parent, false);
				break;
			case IMAGE:
				convertView = mInflater.inflate(
						R.layout.chatting_search_item_image, parent, false);
				break;
			case VOICE:
				convertView = mInflater.inflate(
						R.layout.chatting_search_item_voice, parent, false);
				break;
			case ATTACHMENT:
				convertView = mInflater
						.inflate(R.layout.chatting_search_item_attachment,
								parent, false);
				break;
			case LOCATION:
				convertView = mInflater.inflate(
						R.layout.chatting_search_item_location, parent, false);
				break;
			case NOTIFICATION:
				break;
			}
			convertView.setTag(message.getMessageType());
		}
		TextView nameView = (TextView) convertView
				.findViewById(R.id.tv_chatting_search_item_member_name);
		switch (message.getMessageType()) {
		case TEXT:
			TextView textContentView = (TextView) convertView
					.findViewById(R.id.tv_chatting_search_text_content);
			highLightKeyWord(textContentView, message.getMessageContent());
			break;
		// case IMAGE:
		// ImageView imageContentView = (ImageView) convertView
		// .findViewById(R.id.iv_chatting_search_item_image_content);
		//
		// imageContentView.setImageBitmap(getNativeImage(message
		// .getAttachments().getFilePath()));
		// break;
		// case VOICE:
		// TextView voiceLengthView = (TextView) convertView
		// .findViewById(R.id.tv_chatting_voice_length);
		// int voiceLength = message.getAttachments().getVoiceLength();
		// voiceLengthView.setText(String.valueOf(voiceLength) + "'");
		// break;
		// case ATTACHMENT:
		// ImageView attIconView = (ImageView) convertView
		// .findViewById(R.id.iv_chatting_search_item_attachment_icon);
		// TextView attNameView = (TextView) convertView
		// .findViewById(R.id.tv_chatting_search_item_attachment_name);
		// TextView attSizeView = (TextView) convertView
		// .findViewById(R.id.tv_chatting_search_item_attachment_size);
		// attIconView
		// .setImageBitmap(AttachmentUtil.getInstance(mContext)
		// .getAttachmentIcon(
		// message.getAttachments().getName(), false));
		// highLightKeyWord(attNameView, message.getAttachments().getName());
		// attSizeView.setText(FileUtil.sizeLongToString(message
		// .getAttachments().getSize()));
		// break;
		// case LOCATION:
		// TextView locationNameView = (TextView) convertView
		// .findViewById(R.id.tv_chatting_search_item_location_name);
		// locationNameView.setText(message.getMessageContent());
		//
		// break;
		// case NOTIFICATION:
		// break;
		}
		highLightKeyWord(nameView, nickName);
		TextView dateView = (TextView) convertView
				.findViewById(R.id.tv_chatting_search_item_date);
		dateView.setText(TimeUtils.toFriendly(new Date(message.getTime())));
		return convertView;
	}

	private void highLightKeyWord(TextView textView, String content) {
		SpannableString span = new SpannableString(content);
		int start = content.indexOf(mKeyWord);
		if (start != -1) {
			span.setSpan(new ForegroundColorSpan(mContext.getResources()
					.getColor(R.color.bg_title_right_txt)), start, start
					+ mKeyWord.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		}
		textView.setText(span);
	}

	public Bitmap getNativeImage(String imagePath) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高
		Bitmap myBitmap = BitmapFactory.decodeFile(imagePath, options); // 此时返回myBitmap为空
		// 计算缩放比
		int be = (int) (options.outHeight / (float) 200);
		int ys = options.outHeight % 200;// 求余数
		float fe = ys / (float) 200;
		if (fe >= 0.5)
			be = be + 1;
		if (be <= 0)
			be = 1;
		options.inSampleSize = be;

		// 重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false
		options.inJustDecodeBounds = false;

		myBitmap = BitmapFactory.decodeFile(imagePath, options);
		// TODO 默认图 暂时定义，以后更加策略修改
		if (myBitmap == null) {
			myBitmap = BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.icon_chatting_undown);
		}
		return myBitmap;
	}
}
