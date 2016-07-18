package com.c35.mtd.pushmail.beans;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Attachment {

	public Button previewButton;
	public Button viewButton;
	public Button saveButton;
	public Button cancelButton;
	public Button downloadButton;
	public ImageView iconView;
	public ProgressBar progressBar;
	public TextView unfoldView;
	public LinearLayout compressitemsContainer;
	private C35Attachment c35Attachment = null;
	public boolean unfoldFlag = false;
	public C35Message msg;

	// public TextView attName;

	public C35Message getMsg() {
		return msg;
	}

	public void setMsg(C35Message msg) {
		this.msg = msg;
	}

	public C35Attachment getC35Attachment() {
		return c35Attachment;
	}

	public void setC35Attachment(C35Attachment c35Attachment) {
		try {
			this.c35Attachment = (C35Attachment) c35Attachment.clone();
		} catch (CloneNotSupportedException e) {
			// Debug.e(TAG, "failfast_AA", e);
		}
	}

}
