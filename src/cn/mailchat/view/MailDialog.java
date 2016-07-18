package cn.mailchat.view;

import cn.mailchat.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @Description:
 * @author:cuiwei get from Tienfook Chang
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-9-7
 */
public class MailDialog extends Dialog {

	public MailDialog(Context context, int theme) {
		super(context, theme);
	}

	public MailDialog(Context context) {
		super(context);
	}

	public static class Builder {

		private Context context;
		private String title;
		private int titleColor;
		private CharSequence message;
		private String positiveButtonText;
		private String negativeButtonText;
		private String neutralButtonText;
		private String singleButtonText;
		private View contentView;
		private Bitmap icon;
		private boolean isCancelable = true;
		private int messageGravity=-1;
		private TruncateAt truncateAt=null;
		private DialogInterface.OnClickListener positiveButtonClickListener, negativeButtonClickListener,
				neutralButtonClickListener,singleButtonClickListener;
		private int singleButtonColor;

		public int getSingleButtonColor() {
			return singleButtonColor;
		}

		public void setSingleButtonColor(int singleButtonColor) {
			this.singleButtonColor = singleButtonColor;
		}

		public Builder(Context context) {
			this.context = context;

		}

		public Builder setMessage(CharSequence message) {
			this.message = message;

			return this;

		}

		public Builder setMessage(int message) {
			this.message = (String) context.getText(message);

			return this;

		}

		public Builder setMessageGravity(int gravity){
			this.messageGravity = gravity;

			return this;

		}

		public Builder setMessageEllipsize(TruncateAt truncateAt) {
			this.truncateAt = truncateAt;
			return this;
		}

		public Builder setTitleColor(int titleColor) {
			this.titleColor = titleColor;

			return this;

		}

		public Builder setTitle(String title) {
			this.title = title;

			return this;

		}

		public Builder setTitle(int title) {
			this.title = (String) context.getText(title);

			return this;

		}

		public Builder setContentView(View v) {
			this.contentView = v;

			return this;

		}
		
		public void setIcon(Bitmap icon) {
			this.icon = icon;
		}

		public void setCancelable(boolean isCancelable) {
			this.isCancelable = isCancelable;
		}

		public Builder setPositiveButton(int positiveButtonText,

		DialogInterface.OnClickListener listener) {

			this.positiveButtonText = (String) context

			.getText(positiveButtonText);

			this.positiveButtonClickListener = listener;

			return this;

		}

		public Builder setPositiveButton(String positiveButtonText,

		DialogInterface.OnClickListener listener) {

			this.positiveButtonText = positiveButtonText;

			this.positiveButtonClickListener = listener;

			return this;

		}

		/**
		 * 
		 * @Description:初始化dialog中第二个按钮的文字和点击事件
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 * @see:
		 * @since:
		 * @author: hanlx
		 * @date:2013-2-21
		 */
		public Builder setNegativeButton(int negativeButtonText,

		DialogInterface.OnClickListener listener) {
			this.negativeButtonText = (String) context.getText(negativeButtonText);
			this.negativeButtonClickListener = listener;

			return this;

		}

		public Builder setNegativeButton(String negativeButtonText,

		DialogInterface.OnClickListener listener) {
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;

			return this;

		}

		/**
		 * 
		 * @Description:初始化dialog中第三个按钮的值和点击事件
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 * @see:
		 * @since:
		 * @author: hanlx
		 * @date:2013-2-21
		 */
		public Builder setNeutralButton(int neutralButtonText, DialogInterface.OnClickListener listener) {
			this.neutralButtonText = (String) context.getText(neutralButtonText);
			this.neutralButtonClickListener = listener;
			return this;
		}

		public Builder setNeutralButton(String neutralButtonText, DialogInterface.OnClickListener listener) {
			this.neutralButtonText = neutralButtonText;
			this.neutralButtonClickListener = listener;
			return this;
		}

		/**
		 *
		 * @Description:初始化dialog中第四个按钮的值和点击事件（左右都圆角，用于单独一个按键时）
		 * @param singleButtonText
		 * @param listener
		 * @return
		 * @see:
		 * @since:
		 * @author: shengli
		 * @date:2015-6-16
		 */
		public Builder setFourthButton(int singleButtonText, DialogInterface.OnClickListener listener) {
			this.singleButtonText = (String) context.getText(singleButtonText);
			this.singleButtonClickListener = listener;
			return this;
		}

		public Builder setFourthButton(String singleButtonText, DialogInterface.OnClickListener listener) {
			this.singleButtonText = singleButtonText;
			this.singleButtonClickListener = listener;
			return this;
		}

		public MailDialog create() {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			final MailDialog dialog = new MailDialog(context, R.style.dialog);// R.style.dialog

			View layout = inflater.inflate(R.layout.dialog_mail, null);
			dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

			TextView tv = ((TextView) layout.findViewById(R.id.title));
			if(title==null){
				tv.setVisibility(View.GONE);
			}else{
				tv.setText(title);
			}
			if(titleColor!=0){
				tv.setTextColor(titleColor);
			}

			if (icon != null) {
				ImageView image = (ImageView) layout.findViewById(R.id.icon);
				image.setVisibility(View.VISIBLE);
				image.setImageBitmap(icon);
			}

			if (positiveButtonText != null) {
				((Button) layout.findViewById(R.id.positiveButton)).setText(positiveButtonText);

				if (positiveButtonClickListener != null) {
					((Button) layout.findViewById(R.id.positiveButton)).setOnClickListener(new View.OnClickListener() {

						public void onClick(View v) {
							positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
						}
					});
				}
			} else {
				layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);

			}

			if (negativeButtonText != null) {
				((Button) layout.findViewById(R.id.negativeButton)).setText(negativeButtonText);

				if (negativeButtonClickListener != null) {
					((Button) layout.findViewById(R.id.negativeButton)).setOnClickListener(new View.OnClickListener() {

						public void onClick(View v) {
							negativeButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);

						}
					});
				}
			} else {
				layout.findViewById(R.id.negativeButton).setVisibility(View.GONE);
			}

			if (neutralButtonText != null) {
				Button neutralButton = (Button) layout.findViewById(R.id.third_button);
				neutralButton.setText(neutralButtonText);
				neutralButton.setVisibility(View.VISIBLE);
				if (neutralButtonClickListener != null) {
					neutralButton.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							neutralButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEUTRAL);
						}
					});
				}
			} else {
				layout.findViewById(R.id.third_button).setVisibility(View.GONE);
			}

			if (singleButtonText != null) {
				Button singleButton = (Button) layout.findViewById(R.id.fourth_button);
				singleButton.setText(singleButtonText);
				if(singleButtonColor!=0){
					singleButton.setTextColor(singleButtonColor);
				}
				singleButton.setVisibility(View.VISIBLE);
				if (singleButtonClickListener != null) {
					singleButton.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							singleButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEUTRAL);
						}
					});
				}
			} else {
				layout.findViewById(R.id.fourth_button).setVisibility(View.GONE);
			}

			if (positiveButtonText == null && negativeButtonText == null && neutralButtonText == null && singleButtonText == null) {
				layout.findViewById(R.id.positiveButtonLayout).setVisibility(View.GONE);
			}
			if (message != null) {
				TextView messageTextView =((TextView) layout.findViewById(R.id.message));
				messageTextView.setText(message);
				if(messageGravity!=-1){
					messageTextView.setGravity(messageGravity);
				}
				if(truncateAt!=null){
					messageTextView.setEllipsize(truncateAt);
					messageTextView.setSingleLine();
				}
			} else if (contentView != null) {
				((LinearLayout) layout.findViewById(R.id.content)).removeAllViews();
				((LinearLayout) layout.findViewById(R.id.content)).addView(contentView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

			}
			dialog.setCancelable(isCancelable);
			dialog.setContentView(layout);
			return dialog;

		}

	}

}
