package cn.mailchat.view;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.format.DateUtils;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.FontSizes;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.helper.Contacts;
import cn.mailchat.helper.MessageHelper;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.mail.Address;
import cn.mailchat.mail.Flag;
import cn.mailchat.mail.Message;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.mail.internet.MimeUtility;
import cn.mailchat.utils.GlobalTools;

public class MessageHeader extends LinearLayout implements OnClickListener {
    private Context mContext;
    private TextView mFromView;
    private TextView mDateView;

    private TextView mFromLabel;
    private TextView mToLabel;
    private TextView mCcLabel;
    private TextView mSubjectView;
    private MessageViewAddressRowLayout mFromViewLayout;
    private MessageViewAddressRowLayout mToViewLayout;
    private MessageViewAddressRowLayout mCcViewLayout;
    private TextView textReceiver;
    private TextView textTime;
    private LinearLayout MessageHeaderDetails;
    
    private CheckBox mFlagged;
    private int defaultSubjectColor;
    private TextView mAdditionalHeadersView;
    private TextView imgUnFoldReceivers;
//    private ImageView imgUnFoldReceivers;
    private Message mMessage;
    private Account mAccount;
    private FontSizes mFontSizes = MailChat.getFontSizes();
    private Contacts mContacts;
    private SavedState mSavedState;

    private MessageHelper mMessageHelper;
    private boolean unfoldContactsFlag;
    private OnLayoutChangedListener mOnLayoutChangedListener;

    /**
     * Pair class is only available since API Level 5, so we need
     * this helper class unfortunately
     */
    private static class HeaderEntry {
        public String label;
        public String value;

        public HeaderEntry(String label, String value) {
            this.label = label;
            this.value = value;
        }
    }

    public MessageHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mContacts = Contacts.getInstance(mContext);
    }

    @Override
    protected void onFinishInflate() {

        mFromView = (TextView) findViewById(R.id.from_view);

        mFromLabel = (TextView) findViewById(R.id.from_label);
        mToLabel = (TextView) findViewById(R.id.to_label);
        mCcLabel = (TextView) findViewById(R.id.cc_label);
        
        mFromViewLayout = (MessageViewAddressRowLayout) findViewById(R.id.rowlayout_message_view_topreceivers_container_from);
        mToViewLayout = (MessageViewAddressRowLayout) findViewById(R.id.rowlayout_message_view_topreceivers_container_to);
        mCcViewLayout = (MessageViewAddressRowLayout) findViewById(R.id.rowlayout_message_view_topreceivers_container_cc);
        textReceiver = (TextView) findViewById(R.id.tv_receiver);
		textTime = (TextView) findViewById(R.id.text_msgview_body_top_time);
		MessageHeaderDetails=(LinearLayout) findViewById(R.id.MessageHeader_details);
		imgUnFoldReceivers= (TextView) findViewById(R.id.img_message_view_title_unfold);
		
        mSubjectView = (TextView) findViewById(R.id.subject);
        mAdditionalHeadersView = (TextView) findViewById(R.id.additional_headers_view);
        mDateView = (TextView) findViewById(R.id.date);
        mFlagged = (CheckBox) findViewById(R.id.flagged);

        defaultSubjectColor = mSubjectView.getCurrentTextColor();
        mFontSizes.setViewTextSize(mSubjectView, mFontSizes.getMessageViewSubject());
        mFontSizes.setViewTextSize(mDateView, mFontSizes.getMessageViewDate());
        mFontSizes.setViewTextSize(mAdditionalHeadersView, mFontSizes.getMessageViewAdditionalHeaders());

        mFontSizes.setViewTextSize(mFromView, mFontSizes.getMessageViewSender());
        mFontSizes.setViewTextSize(mFromLabel, mFontSizes.getMessageViewSender());
        mFontSizes.setViewTextSize(mToLabel, mFontSizes.getMessageViewTo());
        mFontSizes.setViewTextSize(mCcLabel, mFontSizes.getMessageViewCC());

        mFromView.setOnClickListener(this);

    	findViewById(R.id.rl_msgview_body_top).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				unfoldOrFoldReceiversContainer();
			}
		});
        findViewById(R.id.tv_message_header_details_fold).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                unfoldOrFoldReceiversContainer();
            }
        });

        mMessageHelper = MessageHelper.getInstance(mContext);

        mSubjectView.setVisibility(VISIBLE);
        hideAdditionalHeaders();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.from_view: {
                onAddSenderToContacts();
                break;
            }
        }
    }

    private void onAddSenderToContacts() {
        if (mMessage != null) {
            try {
                final Address senderEmail = mMessage.getFrom()[0];
                mContacts.createContact(senderEmail);
            } catch (Exception e) {
                Log.e(MailChat.LOG_TAG, "Couldn't create contact", e);
            }
        }
    }

    public void setOnFlagListener(OnClickListener listener) {
        if (mFlagged == null)
            return;
        mFlagged.setOnClickListener(listener);
    }


    public boolean additionalHeadersVisible() {
        return (mAdditionalHeadersView != null &&
                mAdditionalHeadersView.getVisibility() == View.VISIBLE);
    }

    /**
     * Clear the text field for the additional headers display if they are
     * not shown, to save UI resources.
     */
    private void hideAdditionalHeaders() {
        mAdditionalHeadersView.setVisibility(View.GONE);
        mAdditionalHeadersView.setText("");
    }


    /**
     * Set up and then show the additional headers view. Called by
     * {@link #onShowAdditionalHeaders()}
     * (when switching between messages).
     */
    private void showAdditionalHeaders() {
        Integer messageToShow = null;
        try {
            // Retrieve additional headers
            boolean allHeadersDownloaded = mMessage.isSet(Flag.X_GOT_ALL_HEADERS);
            List<HeaderEntry> additionalHeaders = getAdditionalHeaders(mMessage);
            if (!additionalHeaders.isEmpty()) {
                // Show the additional headers that we have got.
                populateAdditionalHeadersView(additionalHeaders);
                mAdditionalHeadersView.setVisibility(View.VISIBLE);
            }
            if (!allHeadersDownloaded) {
                /*
                * Tell the user about the "save all headers" setting
                *
                * NOTE: This is only a temporary solution... in fact,
                * the system should download headers on-demand when they
                * have not been saved in their entirety initially.
                */
                messageToShow = R.string.message_additional_headers_not_downloaded;
            } else if (additionalHeaders.isEmpty()) {
                // All headers have been downloaded, but there are no additional headers.
                messageToShow = R.string.message_no_additional_headers_available;
            }
        } catch (Exception e) {
            messageToShow = R.string.message_additional_headers_retrieval_failed;
        }
        // Show a message to the user, if any
        if (messageToShow != null) {
            Toast toast = Toast.makeText(mContext, messageToShow, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }

    }

    public void populate(final Message message, final Account account) throws MessagingException {
        final Contacts contacts = MailChat.showContactName() ? mContacts : null;
        final CharSequence from = Address.toFriendly(message.getFrom(), contacts);
        final CharSequence to = Address.toFriendly(message.getRecipients(Message.RecipientType.TO), contacts);
        final CharSequence cc = Address.toFriendly(message.getRecipients(Message.RecipientType.CC), contacts);

        Address[] fromAddrs = message.getFrom();
        Address[] toAddrs = message.getRecipients(Message.RecipientType.TO);
        Address[] ccAddrs = message.getRecipients(Message.RecipientType.CC);
        boolean fromMe = mMessageHelper.toMe(account, fromAddrs);

        Address counterpartyAddress = null;
        if (fromMe) {
            if (toAddrs.length > 0) {
                counterpartyAddress = toAddrs[0];
            } else if (ccAddrs.length > 0) {
                counterpartyAddress = ccAddrs[0];
            }
        } else if (fromAddrs.length > 0) {
            counterpartyAddress = fromAddrs[0];
        }
        MessageHeaderDetails.setVisibility(View.GONE);
        /*
         * Only reset visibility of the subject if populate() was called because a new
         * message is shown. If it is the same, do not force the subject visible, because
         * this breaks the MessageTitleView in the action bar, which may hide our subject
         * if it fits in the action bar but is only called when a new message is shown
         * or the device is rotated.
         */
        if (mMessage == null || mMessage.getId() != message.getId()) {
            mSubjectView.setVisibility(VISIBLE);
        }

        mMessage = message;
        mAccount = account;


        final String subject = message.getSubject();
        if (StringUtils.isNullOrEmpty(subject)) {
            mSubjectView.setText(mContext.getText(R.string.general_no_subject));
        } else {
            mSubjectView.setText(subject);
        }
        mSubjectView.setTextColor(0xff000000 | defaultSubjectColor);

        String dateTime = DateUtils.formatDateTime(mContext,
                message.getSentDate().getTime(),
                DateUtils.FORMAT_SHOW_DATE
                | DateUtils.FORMAT_ABBREV_ALL
                | DateUtils.FORMAT_SHOW_TIME
                | DateUtils.FORMAT_SHOW_YEAR);
        mDateView.setText(dateTime);

        mFromView.setText(from);

        updateAddressField(mFromViewLayout, Arrays.asList(fromAddrs), mFromLabel);
        updateAddressField(mToViewLayout, Arrays.asList(toAddrs), mToLabel);
        updateAddressField(mCcViewLayout, Arrays.asList(ccAddrs), mCcLabel);
		if (TextUtils.isEmpty(cc)) {
			textReceiver.setText(mContext.getString(
					R.string.MessageHeader_Receiver, from) + " " + to);
		} else {
			textReceiver.setText(mContext.getString(
					R.string.MessageHeader_Receiver, from)
					+ " "
					+ to
					+ ","
					+ cc);
		}

        textTime.setText(DateUtils.formatDateTime(mContext,
                message.getSentDate().getTime(),
                DateUtils.FORMAT_SHOW_DATE
                | DateUtils.FORMAT_ABBREV_ALL
                | DateUtils.FORMAT_SHOW_TIME));
        
        mFlagged.setChecked(message.isSet(Flag.FLAGGED));

        setVisibility(View.VISIBLE);

        if (mSavedState != null) {
            if (mSavedState.additionalHeadersVisible) {
                showAdditionalHeaders();
            }
            mSavedState = null;
        } else {
            hideAdditionalHeaders();
        }
    }

    public void onShowAdditionalHeaders() {
        int currentVisibility = mAdditionalHeadersView.getVisibility();
        if (currentVisibility == View.VISIBLE) {
            hideAdditionalHeaders();
        } else {
            showAdditionalHeaders();
        }
        layoutChanged();
    }

    private void updateAddressField(MessageViewAddressRowLayout v, List<Address> nameList, View label) {
       // boolean hasText = !TextUtils.isEmpty(text);
    	boolean hasText = nameList.size()>0?true:false;
        setAddresstUI(v, nameList, null);
        v.setVisibility(hasText ? View.VISIBLE : View.GONE);
        label.setVisibility(hasText ? View.VISIBLE : View.GONE);
    }
    /**
     * Expand or collapse a TextView by removing or adding the 2 lines limitation
     */
    private void expand(TextView v, boolean expand) {
       if (expand) {
           v.setMaxLines(Integer.MAX_VALUE);
           v.setEllipsize(null);
       } else {
           v.setMaxLines(2);
           v.setEllipsize(android.text.TextUtils.TruncateAt.END);
       }
    }

    private List<HeaderEntry> getAdditionalHeaders(final Message message)
    throws MessagingException {
        List<HeaderEntry> additionalHeaders = new LinkedList<HeaderEntry>();

        Set<String> headerNames = new LinkedHashSet<String>(message.getHeaderNames());
        for (String headerName : headerNames) {
            String[] headerValues = message.getHeader(headerName);
            for (String headerValue : headerValues) {
                additionalHeaders.add(new HeaderEntry(headerName, headerValue));
            }
        }
        return additionalHeaders;
    }

    /**
     * Set up the additional headers text view with the supplied header data.
     *
     * @param additionalHeaders List of header entries. Each entry consists of a header
     *                          name and a header value. Header names may appear multiple
     *                          times.
     *                          <p/>
     *                          This method is always called from within the UI thread by
     *                          {@link #showAdditionalHeaders()}.
     */
    private void populateAdditionalHeadersView(final List<HeaderEntry> additionalHeaders) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        boolean first = true;
        for (HeaderEntry additionalHeader : additionalHeaders) {
            if (!first) {
                sb.append("\n");
            } else {
                first = false;
            }
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            SpannableString label = new SpannableString(additionalHeader.label + ": ");
            label.setSpan(boldSpan, 0, label.length(), 0);
            sb.append(label);
            sb.append(MimeUtility.unfoldAndDecode(additionalHeader.value));
        }
        mAdditionalHeadersView.setText(sb);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState savedState = new SavedState(superState);

        savedState.additionalHeadersVisible = additionalHeadersVisible();

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState)state;
        super.onRestoreInstanceState(savedState.getSuperState());

        mSavedState = savedState;
    }

    static class SavedState extends BaseSavedState {
        boolean additionalHeadersVisible;

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };


        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.additionalHeadersVisible = (in.readInt() != 0);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt((this.additionalHeadersVisible) ? 1 : 0);
        }
    }

    public interface OnLayoutChangedListener {
        void onLayoutChanged();
    }

    public void setOnLayoutChangedListener(OnLayoutChangedListener listener) {
        mOnLayoutChangedListener = listener;
    }

    private void layoutChanged() {
        if (mOnLayoutChangedListener != null) {
            mOnLayoutChangedListener.onLayoutChanged();
        }
    }

    public void hideSubjectLine() {
        mSubjectView.setVisibility(GONE);
    }
    
	private void setAddresstUI(final MessageViewAddressRowLayout layout, final List<Address> nameList, final List<String> addressList) {
		layout.removeAllViews();
		if (nameList != null) {
			for (int i = 0; i < nameList.size(); i++) {
				TextView tv = new TextView(mContext);
				tv.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
				tv.setTextColor(getResources().getColor(R.color.address_color));
				tv.setId(i);
				tv.setGravity(Gravity.CENTER);
				tv.setPadding(15, 0, 0, 0);
				String name =null;
				try {
					name = nameList.get(i).getPersonal().trim();					
				} catch (Exception e) {
					// TODO: handle exception
					name=nameList.get(i).getAddress().trim();
				}
				tv.setText(name);
				tv.setSingleLine(true);
				tv.setIncludeFontPadding(false);							
				tv.setTextSize(11);
				tv.setEllipsize(TruncateAt.END);
				tv.setBackgroundResource(R.drawable.personnel_bg);	
				tv.setMinWidth(GlobalTools.dip2px(mContext, 45));
				final String email;
				if(nameList.get(i).getAddress().trim().endsWith("@china-channel.com")){
					email=nameList.get(i).getAddress().trim().replace("@china-channel.com", "@35.cn");
				}else{
					email=nameList.get(i).getAddress().trim();
				}
				tv.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO 联系人快速操作
						MessagingController.getInstance(MailChat.application).getContactForJump(mAccount,email,false);
					}
				});
				layout.addView(tv);
			}
		}

	}
	
	private void unfoldOrFoldReceiversContainer() {
		unfoldContactsFlag = !unfoldContactsFlag;

		textReceiver.setVisibility(unfoldContactsFlag ? View.GONE : View.VISIBLE);
		textTime.setVisibility(unfoldContactsFlag ? View.GONE : View.VISIBLE);
		MessageHeaderDetails.setVisibility(unfoldContactsFlag ? View.VISIBLE : View.GONE);
		imgUnFoldReceivers.setVisibility(unfoldContactsFlag ? View.GONE : View.VISIBLE);
	}
	
   public void foldReceiversContainer() {
        unfoldContactsFlag = false;

        textReceiver.setVisibility(View.VISIBLE);
        textTime.setVisibility(View.VISIBLE);
        MessageHeaderDetails.setVisibility(View.GONE);
        imgUnFoldReceivers.setVisibility(View.VISIBLE);
    }
}
