package cn.mailchat.activity.misc;

import android.os.Parcel;
import android.os.Parcelable;

public class SingleAttachment implements Parcelable {
	private String mDownloadingId;
	private String mMessageIdentity;
	private String mAttachmentPosition;
	private String mMessageText;
	
	public String getDownloadingId() {
		return mDownloadingId;
	}
	
	public String getMessageIdentity() {
		return mMessageIdentity;
	}
	
	public String getAttachmentPosition() {
		return mAttachmentPosition;
	}
	
	public String getMessageText() {
		return mMessageText;
	}
	
	public SingleAttachment(String downloadingId,
			String messageIdentity,
			String attachmentPosition,
			String messageText) {
		mDownloadingId = downloadingId;
		mMessageIdentity = messageIdentity;
		mAttachmentPosition = attachmentPosition;
		mMessageText = messageText;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mDownloadingId);
		dest.writeString(mMessageIdentity);
		dest.writeString(mAttachmentPosition);
		dest.writeString(mMessageText);
	}
	
	public static final Parcelable.Creator<SingleAttachment> CREATOR
		= new Parcelable.Creator<SingleAttachment>() {
		
		public SingleAttachment createFromParcel(Parcel in) {
			return new SingleAttachment(in);
		}

		public SingleAttachment[] newArray(int size) {
			return new SingleAttachment[size];
		}
	};

	private SingleAttachment(Parcel in) {
		mDownloadingId = in.readString();
		mMessageIdentity = in.readString();
		mAttachmentPosition = in.readString();
		mMessageText = in.readString();
	}
}
