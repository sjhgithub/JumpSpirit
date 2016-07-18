package cn.mailchat.view;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import com.umeng.analytics.MobclickAgent;

import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.SetImapInfoActivity;
import cn.mailchat.activity.UmengFeekbackActivity;
import cn.mailchat.activity.setup.AccountSetupIncoming;
import cn.mailchat.beans.ImapAndSmtpSetting;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.helper.UrlEncodingHelper;
import cn.mailchat.mail.internet.DecoderUtil;
import android.app.Dialog;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LoginDialog extends Dialog {
	private TextView openView;
	private Button cancelView, positiveButton, negativeButton;
	private Context context;
	private int errorCode;
	private String emailStr;
	private Account mAccount;
	private String password;
	private boolean isShowmanualSettingImp;
	private MessagingController controller;
	private Handler mHander = new Handler();
	private boolean is35Email;

	public LoginDialog(Context context, int errorCode, String email,
			String password, boolean isShowmanualSettingImp, boolean is35Mail) {
		super(context, R.style.dialog);
		this.context = context;
		this.errorCode = errorCode;
		this.emailStr = email;
		this.password = password;
		this.is35Email = is35Mail;
		this.isShowmanualSettingImp = isShowmanualSettingImp;
		controller = MessagingController.getInstance(MailChat.app);
	}

	private MessagingListener listener = new MessagingListener() {

		public void getEmailSetFail(String email, final int errorCode,
				boolean isGoImapSetting) {
			if (isGoImapSetting) {

				mHander.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(
								context,
								context.getString(R.string.advanced_logining_fail),
								Toast.LENGTH_SHORT).show();
						failedGetServerMailSet();
						controller.removeListener(listener);
					}
				});

			}
		}

		public void getEmailSetSuccess(String email,
				final ImapAndSmtpSetting imapAndSmtpSetting,
				boolean isGoImapSetting) {
			if (isGoImapSetting) {
				mHander.post(new Runnable() {
					@Override
					public void run() {
						finishHttpsAutoSetup(imapAndSmtpSetting);
						controller.removeListener(listener);
					}
				});

			}
		}

	};

	private void failedGetServerMailSet() {
		if (emailStr.contains("@china-channel.com")) {
			emailStr = emailStr.replace("@china-channel.com", "@35.cn");
		}
		String user;
		String[] emailParts = splitEmail(emailStr);
		// if (emailParts[1].equals("35.cn")) {
		user = emailStr;
		// } else {
		// user = emailParts[0];
		// }
		String domain = emailParts[1];
		String passwordEnc = UrlEncodingHelper.encodeUtf8(password);
		mAccount = Preferences.getPreferences(context).newAccount(emailStr);
		Log.i(MailChat.LOG_COLLECTOR_TAG, LoginDialog.class.getSimpleName()+"  failedGetServerMailSet : newAccount==>>>" + emailStr);
		URI uri;
		try {
			uri = new URI("placeholder", emailStr + ":" + passwordEnc, "mail."
					+ domain, -1, null, null, null);
			mAccount.setStoreUri(uri.toString());
			// System.out.println("StoreUri:" + uri.toString());
			mAccount.setTransportUri(uri.toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		mAccount.setName(getOwnerName());
		mAccount.setEmail(emailStr);
		setupFolderNames(domain);
		onImap();
	}

	private void onImap() {
		try {
			URI uri = new URI(mAccount.getStoreUri());
			uri = new URI("imap+ssl+", uri.getUserInfo(), uri.getHost(),
					uri.getPort(), null, null, null);
			mAccount.setStoreUri(uri.toString());

			uri = new URI(mAccount.getTransportUri());
			uri = new URI("smtp+tls+", uri.getUserInfo(), uri.getHost(),
					uri.getPort(), null, null, null);
			mAccount.setTransportUri(uri.toString());

			AccountSetupIncoming.actionIncomingSettings(context, mAccount,
					false, true, is35Email);
		} catch (Exception use) {
		}

	}

	/**
	 * 以@分割Email
	 * 
	 * @author liwent
	 * @param email
	 * @return String[0]为账号String[1]邮箱后缀
	 */
	private String[] splitEmail(String email) {
		String[] retParts = new String[2];
		String[] emailParts = email.split("@");
		retParts[0] = (emailParts.length > 0) ? emailParts[0] : "";
		/* 小数点后至少有长度为1的地址 */
		retParts[1] = (emailParts.length > 1) ? emailParts[1] : "";
		return retParts;
	}

	/**
	 * 获取使用者名称
	 * 
	 * @author liwent
	 */
	private String getOwnerName() {
		String name = null;
		try {
			name = getDefaultAccountName();
		} catch (Exception e) {
			Log.e(MailChat.LOG_TAG, "Could not get default account name", e);
		}

		if (name == null) {
			name = "";
		}
		return name;
	}

	private String getDefaultAccountName() {
		String name = null;
		Account account = Preferences.getPreferences(context)
				.getDefaultAccount();
		if (account != null) {
			name = account.getName();
		}
		return name;
	}

	private void onManualSetup() {
		if (emailStr.contains("@china-channel.com")) {
			emailStr = emailStr.replace("@china-channel.com", "@35.cn");
		}
		String user;
		String[] emailParts = splitEmail(emailStr);
		// if (emailParts[1].equals("35.cn")) {
		user = emailStr;
		// } else {
		// user = emailParts[0];
		// }
		String domain = emailParts[1];
		URI incomingUri = null;
		URI outgoingUri = null;
		Provider mProvider = findProviderForDomain(domain);
		String userEnc = UrlEncodingHelper.encodeUtf8(user);
		String passwordEnc = UrlEncodingHelper.encodeUtf8(password);

		try {
			if (mProvider != null) {
				String incomingUsername = mProvider.incomingUsernameTemplate;
				incomingUsername = incomingUsername.replaceAll("\\$email",
						emailStr);
				incomingUsername = incomingUsername.replaceAll("\\$user",
						userEnc);
				incomingUsername = incomingUsername.replaceAll("\\$domain",
						domain);

				URI incomingUriTemplate = mProvider.incomingUriTemplate;
				incomingUri = new URI(incomingUriTemplate.getScheme(),
						incomingUsername + ":" + passwordEnc,
						incomingUriTemplate.getHost(),
						incomingUriTemplate.getPort(), null, null, null);
				String outgoingUsername = mProvider.outgoingUsernameTemplate;
				URI outgoingUriTemplate = mProvider.outgoingUriTemplate;

				if (outgoingUsername != null) {
					outgoingUsername = outgoingUsername.replaceAll("\\$email",
							emailStr);
					outgoingUsername = outgoingUsername.replaceAll("\\$user",
							user);
					outgoingUsername = outgoingUsername.replaceAll("\\$domain",
							domain);
					outgoingUri = new URI(outgoingUriTemplate.getScheme(),
							outgoingUsername + ":" + passwordEnc,
							outgoingUriTemplate.getHost(),
							outgoingUriTemplate.getPort(), null, null, null);

				} else {
					outgoingUri = new URI(outgoingUriTemplate.getScheme(),
							null, outgoingUriTemplate.getHost(),
							outgoingUriTemplate.getPort(), null, null, null);

				}

				// 登陆失败后会删除账户，需重新创建
				mAccount = Preferences.getPreferences(context).newAccount(
						emailStr);
				Log.i(MailChat.LOG_COLLECTOR_TAG, LoginDialog.class.getSimpleName()+"  onManualSetup : newAccount==>>>" + emailStr);
				mAccount.setStoreUri(incomingUri.toString());
				mAccount.setTransportUri(outgoingUri.toString());
				setupFolderNames(incomingUriTemplate.getHost().toLowerCase(
						Locale.US));
				if (incomingUri.toString().startsWith("imap")) {
					mAccount.setDeletePolicy(Account.DELETE_POLICY_ON_DELETE);
				} else if (incomingUri.toString().startsWith("pop3")) {
					mAccount.setDeletePolicy(Account.DELETE_POLICY_NEVER);
				}
				mAccount.setName(getOwnerName());
				mAccount.setEmail(emailStr);
				setupFolderNames(domain);
				onImap();
			} else {
				controller.addListener(listener);
				controller.getEmailSet(emailStr, true);
			}

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setupFolderNames(String domain) {
		mAccount.setDraftsFolderName(context
				.getString(R.string.special_mailbox_name_drafts));
		mAccount.setTrashFolderName(context
				.getString(R.string.special_mailbox_name_trash));
		mAccount.setSentFolderName(context
				.getString(R.string.special_mailbox_name_sent));
		mAccount.setArchiveFolderName(context
				.getString(R.string.special_mailbox_name_archive));

		// Yahoo! has a special folder for Spam, called "Bulk Mail".
		if (domain.endsWith(".yahoo.com")) {
			mAccount.setSpamFolderName("Bulk Mail");
		} else {
			mAccount.setSpamFolderName(context
					.getString(R.string.special_mailbox_name_spam));
		}
	}

	/**
	 * 获取邮件服务供应商
	 * 
	 * @note liwent
	 * @param domain
	 */
	private Provider findProviderForDomain(String domain) {
		try {
			XmlResourceParser xml = context.getResources().getXml(
					R.xml.providers);
			int xmlEventType;
			Provider provider = null;
			while ((xmlEventType = xml.next()) != XmlResourceParser.END_DOCUMENT) {
				if (xmlEventType == XmlResourceParser.START_TAG
						&& "provider".equals(xml.getName())
						&& domain.equalsIgnoreCase(getXmlAttribute(xml,
								"domain"))) {
					provider = new Provider();
					provider.id = getXmlAttribute(xml, "id");
					provider.label = getXmlAttribute(xml, "label");
					provider.domain = getXmlAttribute(xml, "domain");
					provider.note = getXmlAttribute(xml, "note");
				} else if (xmlEventType == XmlResourceParser.START_TAG
						&& "incoming".equals(xml.getName()) && provider != null) {
					provider.incomingUriTemplate = new URI(getXmlAttribute(xml,
							"uri"));
					provider.incomingUsernameTemplate = getXmlAttribute(xml,
							"username");
				} else if (xmlEventType == XmlResourceParser.START_TAG
						&& "outgoing".equals(xml.getName()) && provider != null) {
					provider.outgoingUriTemplate = new URI(getXmlAttribute(xml,
							"uri"));
					provider.outgoingUsernameTemplate = getXmlAttribute(xml,
							"username");
				} else if (xmlEventType == XmlResourceParser.END_TAG
						&& "provider".equals(xml.getName()) && provider != null) {
					return provider;
				}
			}
		} catch (Exception e) {
			Log.e(MailChat.LOG_TAG,
					"Error while trying to load provider settings.", e);
		}
		return null;
	}

	private String getXmlAttribute(XmlResourceParser xml, String name) {
		int resId = xml.getAttributeResourceValue(null, name, 0);
		if (resId == 0) {
			return xml.getAttributeValue(null, name);
		} else {
			return context.getString(resId);
		}
	}

	/**
	 * 邮件服务供应商信息
	 * 
	 * @author liwent
	 * @see Documented
	 */
	static class Provider implements Serializable {
		private static final long serialVersionUID = 8511656164616538989L;
		public String id;/* 编号 */
		public String label;/* 商标 */
		public String domain;/* 域名 */
		public URI incomingUriTemplate;/* URL传入模板 */
		public String incomingUsernameTemplate;/* 用户名传入模板 */
		public URI outgoingUriTemplate;/* URL输出模板 */
		public String outgoingUsernameTemplate;/* 用户名输出模板 */
		public String note;/* 注解 */

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("编号:").append(id).append("\n");
			builder.append("商标:").append(label).append("\n");
			builder.append("域名:").append(domain).append("\n");
			builder.append("发送服务器:").append(incomingUriTemplate).append("\n");
			builder.append("用户传入：").append(incomingUsernameTemplate)
					.append("\n");
			builder.append("接收服务器：").append(outgoingUriTemplate).append("\n");
			builder.append("用户输出：").append(outgoingUsernameTemplate)
					.append("\n");
			builder.append("注解：").append(note).append("\n");
			return builder.toString();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_login);
		initView();
		setListener();
	}

	private void setListener() {
		openView.setOnClickListener(myClickListener());
		positiveButton.setOnClickListener(myClickListener());
		negativeButton.setOnClickListener(myClickListener());
		cancelView.setOnClickListener(myClickListener());
	}

	private android.view.View.OnClickListener myClickListener() {
		// TODO Auto-generated method stub
		return new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.positiveButton:
					SetImapInfoActivity.actionDisplaySetImapInfo(context);
					controller.removeListener(listener);
					break;
				case R.id.negativeButton:
					onManualSetup();
					MobclickAgent.onEvent(context, "manual_setting_imp");
					// controller.removeListener(listener);
					break;
				// case R.id.btn_contac_support:
				// UmengFeekbackActivity.actionChatList(context,emailStr);
				// controller.removeListener(listener);
				// break;
				case R.id.third_button:
					controller.removeListener(listener);
					break;
				default:
					break;
				}
				LoginDialog.this.cancel();
			}
		};
	}

	private void initView() {
		openView = (TextView) findViewById(R.id.tv_open);

		cancelView = (Button) findViewById(R.id.third_button);
		positiveButton = (Button) findViewById(R.id.positiveButton);
		negativeButton = (Button) findViewById(R.id.negativeButton);
		TextView tv_content_1 = (TextView) findViewById(R.id.login_fail_dialog_tv_content_1);
		TextView tv_content_2 = (TextView) findViewById(R.id.login_fail_dialog_tv_content_2);
		TextView tv_content_3 = (TextView) findViewById(R.id.login_fail_dialog_tv_content_3);
		
		TextView tv_exception = (TextView) findViewById(R.id.login_fail_dialog_exception);
		
		if (MailChat.lastAccountSetupCheckException != null) {
		    tv_exception.setText(DecoderUtil.decodeString(MailChat.lastAccountSetupCheckException));
		} else {
		    tv_exception.setText("");
		}
		
		TextView tv_title = (TextView) findViewById(R.id.tv_title);
		String content = context
				.getString(R.string.login_fail_dialog_tv_content_open);
		openView.setText(colorTextContent(context, content, 1,
				content.length() - 1));
		if (errorCode == 0) {
			tv_content_3.setVisibility(View.VISIBLE);
		}
		if (is35Email) {
			tv_title.setVisibility(View.GONE);
			tv_content_1.setVisibility(View.GONE);
			tv_content_2.setVisibility(View.GONE);
			tv_content_3.setText(context.getResources().getString(
					R.string.login_error));
			cancelView.setText(context.getResources().getString(
					R.string.okay_action));
			positiveButton.setVisibility(View.GONE);
			cancelView
					.setBackgroundResource(R.drawable.selecter_btn_dialog_radius_left_right);
			cancelView.setTextColor(context.getResources().getColor(
					R.color.light_blue));
		} else if (!emailStr.contains("163") && !emailStr.contains("126")
				&& !emailStr.contains("yeah") && !emailStr.contains("qq")
				&& !emailStr.contains("sina")) {
			cancelView.setText(context.getResources().getString(
					R.string.okay_action));
			positiveButton.setVisibility(View.GONE);
			cancelView
					.setBackgroundResource(R.drawable.selecter_btn_dialog_radius_left_right);
		}
		if (emailStr.contains("qq")||is35Email) {
			tv_content_3.setVisibility(View.VISIBLE);
		} else if (emailStr.contains("163") || emailStr.contains("126")
				|| emailStr.contains("yeah")) {
			tv_content_3.setVisibility(View.VISIBLE);
			tv_content_3.setText(context.getResources().getString(
					R.string.login_fail_dialog_tv_content_4));
		} else {
			tv_content_3.setVisibility(View.GONE);
		}

		// if (isShowmanualSettingImp) {
		// manualSettingImp.setVisibility(View.VISIBLE);
		// } else {
		negativeButton.setVisibility(View.GONE);
		positiveButton.setText(R.string.tv_login_help);
		negativeButton.setText(R.string.manual_setting_action);
		// }
	}

	public SpannableString colorTextContent(Context mContext, String content,
			int Start, int end) {
		SpannableString span = new SpannableString(content);
		span.setSpan(
				new ForegroundColorSpan(mContext.getResources().getColor(
						R.color.light_blue_font)), Start, end,
				Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		return span;
	}

	/* 从服务器获取下来IMAP设置 */
	private void finishHttpsAutoSetup(ImapAndSmtpSetting imapAndSmtpSetting) {
		// 获取邮箱账号
		String[] emailParts = splitEmail(emailStr);
		// String user;
		// if (emailParts[1].equals("35.cn")) {
		String user = emailStr;
		// } else {
		// user = emailParts[0];
		// }
		String domain = emailParts[1];
		try {
			String userEnc = UrlEncodingHelper.encodeUtf8(user);
			String passwordEnc = UrlEncodingHelper.encodeUtf8(password);
			int imapSafety = imapAndSmtpSetting.getImapSafety();
			String imapScheme = null;
			if (imapSafety == 0) {
				imapScheme = "imap";
			} else if (imapSafety == 1) {
				imapScheme = "imap+tls+";
			} else if (imapSafety == 2) {
				imapScheme = "imap+ssl+";
			}
			int smtpSafety = imapAndSmtpSetting.getSmtpSafety();
			String smtpScheme = null;
			if (smtpSafety == 0) {
				smtpScheme = "smtp";
			} else if (smtpSafety == 1) {
				smtpScheme = "smtp+tls+";
			} else if (smtpSafety == 2) {
				smtpScheme = "smtp+ssl+";
			}
			URI incomingUri = new URI(imapScheme, userEnc + ":" + passwordEnc,
					imapAndSmtpSetting.getImapHost(),
					imapAndSmtpSetting.getImapPost(), null, null, null);
			URI outgoingUri = new URI(smtpScheme, userEnc + ":" + passwordEnc,
					imapAndSmtpSetting.getSmtpHost(),
					imapAndSmtpSetting.getSmtpPost(), null, null, null);
			// 登陆失败后会删除账户，需重新创建
			mAccount = Preferences.getPreferences(context).newAccount(emailStr);
			Log.i(MailChat.LOG_COLLECTOR_TAG, LoginDialog.class.getSimpleName()+"  finishHttpsAutoSetup : newAccount==>>>" + emailStr);
			mAccount.setName(getOwnerName());
			mAccount.setEmail(emailStr);
			mAccount.setStoreUri(incomingUri.toString());
			mAccount.setTransportUri(outgoingUri.toString());
			setupFolderNames(imapAndSmtpSetting.getImapHost().toLowerCase(
					Locale.US));
			if (incomingUri.toString().startsWith("imap")) {
				mAccount.setDeletePolicy(Account.DELETE_POLICY_ON_DELETE);
			} else if (incomingUri.toString().startsWith("pop3")) {
				mAccount.setDeletePolicy(Account.DELETE_POLICY_NEVER);
			}
			onImap();
		} catch (URISyntaxException use) {
			/*
			 * If there is some problem with the URI we give up and go on to
			 * manual setup.
			 */
			onManualSetup();
		}
	}

}
