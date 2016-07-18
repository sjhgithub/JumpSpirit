package cn.mailchat.fragment;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.ChattingActivity;
import cn.mailchat.activity.ChattingSingleActivity;
import cn.mailchat.activity.ForwardActivity;
import cn.mailchat.activity.MailComposeActivity;
import cn.mailchat.activity.MessageReference;
import cn.mailchat.activity.misc.SingleAttachment;
import cn.mailchat.chatting.beans.CAttachment;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.CMessage;
import cn.mailchat.chatting.beans.DAttachment;
import cn.mailchat.chatting.beans.DChat;
import cn.mailchat.chatting.beans.DChatMessage;
import cn.mailchat.chatting.beans.MixedChatting;
import cn.mailchat.chatting.beans.CMessage.Type;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.mail.Message;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.utils.GlobalTools;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.view.AttachmentView;
import cn.mailchat.view.MailDialog;
import cn.mailchat.view.pager.base.BaseTabFragment;

public class ForwardViewPagerItemFragment extends BaseTabFragment {
	public static final String BUNDLE_KEY_CATALOG = "BUNDLE_KEY_CATALOG";
	private int mCatalog;
	private String accountUid;
	private Serializable mMessage;
	private SingleAttachment mSingleAttachment;
	private ListView listViewSelected;
	private TextView noResultView;
	private RelativeLayout mailItemView;
	private MessagingController controller;
	private MixedChatAdapter mixedChatAdapter;
	private ContactsAdapter contactsAdapter;
	private List<MixedChatting>  mmixedChattings;
	private List<ContactAttribute> contactList;
	private LruCache<String, Bitmap> mMemoryCache;
	private Context context;
	private Account mAccount ;
	private ProgressBar progressBar;
	private static MailDialog progressBarDilog;
	private static String noDownloadfilePath;
	private static String eMail;
	private static String noDownloadFileName;
	private Handler mHandler =new Handler();
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			mCatalog = args.getInt(BUNDLE_KEY_CATALOG);
			accountUid = args.getString(ForwardActivity.ACCOUNTUUID);
			mMessage = args.getSerializable(ForwardActivity.MESSAGE);
			mSingleAttachment = args.getParcelable(ForwardActivity.SINGLE_ATTACHMENT);
		}
		controller = MessagingController.getInstance(MailChat.getInstance());
		controller.addListener(listener);
		mMemoryCache =MailChat.getInstance().getmMemoryCache();
		context = getActivity();
		mAccount = Preferences.getPreferences(MailChat.getInstance()).getAccount(accountUid);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.item_forward, container,
				false);
		initViews(view);
		initData();
		return view;
	}

	private void initViews(View view) {
		// TODO Auto-generated method stub
		listViewSelected = (ListView) view.findViewById(R.id.list_contacts);
		mailItemView = (RelativeLayout)view.findViewById(R.id.layout_forward_email);
		noResultView = (TextView) view.findViewById(R.id.no_result);
		switch (mCatalog) {
		case ForwardTabInfo.CATALOG_CHAT:
			mailItemView.setVisibility(View.GONE);
			break;
		case ForwardTabInfo.CATALOG_MAIL:
			mailItemView.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}
	
	private void initData(){
		if(mAccount==null){
			return;
		}
		
		mailItemView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				forwardViaMail(null);
			}
		});
		
		switch (mCatalog) {
		case ForwardTabInfo.CATALOG_CHAT:
			mixedChatAdapter = new MixedChatAdapter();
			listViewSelected.setAdapter(mixedChatAdapter);
			controller.searchMixedChattingByForward(mAccount, "");
			listViewSelected.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					forwardViaChatMessage(mmixedChattings.get(position));
				}
			});
			break;
		case ForwardTabInfo.CATALOG_MAIL:
			contactsAdapter = new ContactsAdapter();
			listViewSelected.setAdapter(contactsAdapter);
			controller.searchContactsByForward(mAccount, "");
			listViewSelected.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					forwardViaMail(contactList.get(position));
				}
			});
			break;
		default:
			break;
		}
	}
	
	private void forwardViaMail(final ContactAttribute contactAttribute) {
		MailDialog.Builder builder = new MailDialog.Builder(context);
		builder.setTitle(context.getString(R.string.forward_to_mail));
		builder.setMessageGravity(Gravity.CENTER|Gravity.LEFT);
		builder.setMessageEllipsize(TruncateAt.END);
		if(contactAttribute!=null){
			eMail= contactAttribute.getEmail();
			builder.setMessage(contactAttribute.getNickName());
			Bitmap contactAttributeIcon=null;
			if (!TextUtils.isEmpty(contactAttribute.getImgHeadHash())) {
				String userHeadUrl =GlobalConstants.HOST_IMG+contactAttribute.getImgHeadHash()+GlobalConstants.USER_SMALL_HEAD_END;
				contactAttributeIcon = ImageUtils.createFramedPhoto(GlobalTools.dip2px(context, 26.7f),GlobalTools.dip2px(context, 26.7f), ImageLoader.getInstance().loadImageSync(userHeadUrl), 5);
			} else {
				contactAttributeIcon = ImageUtils.createFramedPhoto(GlobalTools.dip2px(context, 26.7f),GlobalTools.dip2px(context, 26.7f), ImageUtils.getUserFirstTextBitmap(context, contactAttribute.getNickName()), 5);
			}
			builder.setIcon(contactAttributeIcon);
		}else{
			builder.setMessage(getText(R.string.forwarding_new_mail));
			eMail=null;
		}
		builder.setPositiveButton(getString(R.string.okay_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						if (mSingleAttachment != null) {
							Message msg = null;
							try {
								msg = new MessageReference(mSingleAttachment.getMessageIdentity()).restoreToLocalMessage(context);
							} catch (MessagingException e) {
								// DO NOTHING
							}
							String toEmail =null;
							if(contactAttribute!=null){
								toEmail=contactAttribute.getEmail();
							}
							MailComposeActivity.actionForwardSingleAttachment(context,
									mAccount,
									toEmail,
									msg,
									mSingleAttachment.getAttachmentPosition(),
									mSingleAttachment.getMessageText());
							((Activity) context).finish();
						} else if (mMessage != null) {
							//TODO：有时间写一个父类接口ChatMessage
							if(mMessage instanceof CMessage){
								CMessage cMessage = (CMessage)mMessage;
								switch (cMessage.getMessageType()) {
								case TEXT:
									MailComposeActivity.actionForwardChatMessage(context, mAccount, eMail, null, cMessage.getContent(), null, null);
									((Activity) context).finish();
									break;
								case IMAGE:
									getCAttachmentPathForwardToMail(cMessage);
									break;
								case ATTACHMENT:
									getCAttachmentPathForwardToMail(cMessage);
									break;
								default:
									break;
								}
							}else{
								DChatMessage dMessage =(DChatMessage)mMessage;
								switch (dMessage.getMessageType()) {
								case TEXT:
									MailComposeActivity.actionForwardChatMessage(context, mAccount, eMail, null, dMessage.getMessageContent(), null, null);
									((Activity) context).finish();
									break;
								case IMAGE:
									getDAttachmentPathForwardToMail(dMessage);
									break;
								case ATTACHMENT:
									getDAttachmentPathForwardToMail(dMessage);
									break;
								default:
									break;
								}
							}
						}
						dialog.dismiss();
					}
					
				});
		builder.setNeutralButton(context.getString(R.string.cancel_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		controller.removeListener(listener);
	}
	private class MixedChatAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private MixedChatHolder holder;
		public MixedChatAdapter() {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mmixedChattings == null ? 0 : mmixedChattings.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			if (view == null) {
				holder = new MixedChatHolder();
				view = mInflater.inflate(R.layout.item_text, parent, false);
				holder.text = (TextView) view.findViewById(R.id.text);
				holder.logo = (ImageView) view.findViewById(R.id.iv_logo);
				view.setTag(holder);
			} else {
				holder = (MixedChatHolder) view.getTag();
			}
			
			MixedChatting mixedChatting = mmixedChattings.get(position);
			
			if(mixedChatting.isGroup()){
				CGroup group = mixedChatting.getGroup();
				holder.text.setText(group.getGroupName());
				Bitmap groupIcon = mMemoryCache.get(group.getUid());
				// 群图片
				holder.logo.setVisibility(View.VISIBLE);
				if (groupIcon == null) {
					if (groupIcon == null) {
						groupIcon = ImageUtils.getBitmapFromResources(context,
								R.drawable.icon_group_chat_head);
					}
					if (groupIcon != null) {
						mMemoryCache.put(group.getUid(), groupIcon);
					}

				}
				holder.logo.setImageBitmap(groupIcon);
			}else{
				DChat dChat = mixedChatting.getDchat();
				// 单聊对象
				final String toUserName = dChat.getNickName();
				holder.text.setText(toUserName);
				holder.logo.setVisibility(View.VISIBLE);
				String userHeadUrl = dChat.getImgHead() != null ? dChat.getImgHead()
						+ GlobalConstants.USER_SMALL_HEAD_END : "";
				ImageLoader.getInstance().displayImage(userHeadUrl, holder.logo,
						MailChat.getInstance().initImageLoaderOptions(),new ImageLoadingListener() {

							@Override
							public void onLoadingStarted(String arg0, View arg1) {
								// TODO Auto-generated method stub
							}

							@Override
							public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
								// TODO Auto-generated method stub
							}

							@Override
							public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
								// TODO Auto-generated method stub
								if(arg2==null){
									holder.logo.setImageBitmap(ImageUtils.getUserFirstTextBitmap(context, toUserName));
								}
							}

							@Override
							public void onLoadingCancelled(String arg0, View arg1) {
								// TODO Auto-generated method stub
							}
						});
			}
			return view;
		}

	}

	private class ContactsAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private ContactsHolder holder;
		public ContactsAdapter() {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return contactList == null ? 0 : contactList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			if (view == null) {
				holder = new ContactsHolder();
				view = mInflater.inflate(R.layout.item_contact, parent, false);
				holder.name = (TextView) view.findViewById(R.id.name);
				holder.email = (TextView) view.findViewById(R.id.email);
				holder.logo = (ImageView) view.findViewById(R.id.iv_logo);
				view.setTag(holder);
			} else {
				holder = (ContactsHolder) view.getTag();
			}
			ContactAttribute contactAttribute=contactList.get(position);
			final String nickName = contactAttribute.getNickName();
			holder.name.setText(nickName);
			holder.email.setText(contactAttribute.getEmail());
			String userHeadUrl = contactAttribute.getImgHeadPath() != null ? contactAttribute.getImgHeadPath()
					+ GlobalConstants.USER_SMALL_HEAD_END : "";
			ImageLoader.getInstance().displayImage(userHeadUrl, holder.logo,
					MailChat.getInstance().initImageLoaderOptions(),new ImageLoadingListener() {

						@Override
						public void onLoadingStarted(String arg0, View arg1) {
							// TODO Auto-generated method stub
						}

						@Override
						public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
							// TODO Auto-generated method stub
						}

						@Override
						public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
							// TODO Auto-generated method stub
							if(arg2==null){
								holder.logo.setImageBitmap(ImageUtils.getUserFirstTextBitmap(context, nickName));
							}
						}

						@Override
						public void onLoadingCancelled(String arg0, View arg1) {
							// TODO Auto-generated method stub
						}
					});
			return view;
		}

	}

	class MixedChatHolder {
		ImageView logo;
		TextView text;
	}

	class ContactsHolder {
		ImageView logo;
		TextView name;
		TextView email;
	}
	
	private MessagingListener listener = new MessagingListener() {

		public void fileDownloadStart(Account acc,final String id){
			if(acc.getUuid().equals(mAccount.getUuid())){
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
					}
				});
			}
		};

		public void fileDownloadProgress(Account acc,final String id,final int progress){
			if(acc.getUuid().equals(mAccount.getUuid())){
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(progressBar!=null){
							progressBar.setProgress(progress);
						}
					}
				});
			}
		}

		public void fileDownloadFinished(Account acc,final String id){
			if(acc.getUuid().equals(mAccount.getUuid())){
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(progressBarDilog!=null){
							progressBarDilog.dismiss();
							progressBarDilog=null;
							((Activity) context).finish();
							MailComposeActivity.actionForwardChatMessage(context, mAccount, eMail, null, null, getFilePathUri(noDownloadfilePath), noDownloadFileName);
							noDownloadfilePath=null;
							eMail=null;
						}
					}
				});
			}
		}

		public void fileDownloadFailed(Account acc,String id){
			if(acc.getUuid().equals(mAccount.getUuid())){
				if(progressBarDilog!=null){
					progressBarDilog.dismiss();
					progressBarDilog=null;
					noDownloadfilePath=null;
					eMail=null;
				}
			}
		};

		public void fileDownloadInterrupt(Account acc,String id){
			if(acc.getUuid().equals(mAccount.getUuid())){
				if(progressBarDilog!=null){
					progressBarDilog=null;
					noDownloadfilePath=null;
					eMail=null;
				}
			}
		};
		
		public void searchMixedChattingByForwardFinished(Account account,final List<MixedChatting> mixedChattings){
			if(accountUid.equals(account.getUuid())&& mCatalog==ForwardTabInfo.CATALOG_CHAT){
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(mixedChattings==null||mixedChattings.size()==0){
							noResultView.setVisibility(View.VISIBLE);
							listViewSelected.setVisibility(View.GONE);
						}else{
							noResultView.setVisibility(View.GONE);
							listViewSelected.setVisibility(View.VISIBLE);
							mmixedChattings = mixedChattings;
							mixedChatAdapter.notifyDataSetChanged();
						}
					}
				});
			}
		}

		public void searchContactsByForwardFinished(Account account,final List<ContactAttribute> contacts){
			if(accountUid.equals(account.getUuid())&& mCatalog==ForwardTabInfo.CATALOG_MAIL){
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(contacts==null||contacts.size()==0){
							noResultView.setVisibility(View.VISIBLE);
							listViewSelected.setVisibility(View.GONE);
						}else{
							noResultView.setVisibility(View.GONE);
							listViewSelected.setVisibility(View.VISIBLE);
							contactList=contacts;
							contactsAdapter.notifyDataSetChanged();
						}
					}
				});
			}
		}
	};

	public void forwardViaChatMessage(MixedChatting mixedChatting) {
		MailDialog.Builder builder = new MailDialog.Builder(context);
		builder.setTitle(context.getString(R.string.forward_to_chat));
		builder.setMessageGravity(Gravity.CENTER|Gravity.LEFT);
		builder.setMessageEllipsize(TruncateAt.END);
		if(mixedChatting.isGroup()){
			final CGroup cGroup  =mixedChatting.getGroup();
			Bitmap groupIcon = ImageUtils.createFramedPhoto(GlobalTools.dip2px(context, 26.7f),
							GlobalTools.dip2px(context, 26.7f), ImageUtils.getBitmapFromResources(context,
									R.drawable.icon_group_chat_head), 5);
			builder.setIcon(groupIcon);
			builder.setMessage(cGroup.getGroupName());
			builder.setPositiveButton(getString(R.string.okay_action),
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int id) {
							if (mSingleAttachment != null) {
								AttachmentView av = null;
								
								synchronized(MailChat.attachmentList) {
									av = MailChat.attachmentList.get(mSingleAttachment.getDownloadingId());
								}
								
								final AttachmentView attView = av;
								synchronized(MailChat.downloadingList) {
									if (av.mStatus == AttachmentView.Status.COMPLETE) {
										forwardFileToMessage(attView, cGroup);
										((Activity) context).finish();
									} else {
										forwardNoDownloadAttachment(av);
										av.setAttachmentListener(av.new AttachmentListener() {
									    	public void loadAttachmentFinished() {
									    		forwardFileToMessage(attView, cGroup);
									    		attView.setAttachmentListener(null);
									    		progressBarDilog.dismiss();
									    		progressBarDilog = null;
									    		((Activity) context).finish();
									    	}
									    	public void loadAttachmentFailed() {
									    		MailChat.toast(getString(R.string.forward_downloading_file_cancel_hint));
									    		attView.setAttachmentListener(null);
									    		progressBarDilog.dismiss();
									    		progressBarDilog = null;
									    		((Activity) context).finish();
									    	}
									    	public void progress(int progress) {
									    		progressBar.setProgress(progress);
									    	}
										});
										av.mIsTemp = true;
										
										if (av.mStatus == AttachmentView.Status.METADATA) {
											av.open();
										}
									}
								}
							} else if (mMessage != null) {
								CMessage cMessage =null;
								if(mMessage instanceof DChatMessage){
									cMessage = DMessage2CMessage((DChatMessage)mMessage);
								}else{
									cMessage =(CMessage)mMessage;
								}
								ChattingActivity.actionForwardMessage(context, cMessage,cGroup, mAccount);
								((Activity) context).finish();
							}
							dialog.dismiss();
						}
					});
		}else{	
			final DChat dChat = mixedChatting.getDchat();
			builder.setMessage(dChat.getNickName());
			Bitmap dChatIcon=null;
			if (!TextUtils.isEmpty(dChat.getImgHead())) {
				String userHeadUrl = dChat.getImgHead()+GlobalConstants.USER_SMALL_HEAD_END;
				dChatIcon = ImageUtils.createFramedPhoto(GlobalTools.dip2px(context, 26.7f),GlobalTools.dip2px(context, 26.7f), ImageLoader.getInstance().loadImageSync(userHeadUrl), 5);
			} else {
				dChatIcon = ImageUtils.createFramedPhoto(GlobalTools.dip2px(context, 26.7f),GlobalTools.dip2px(context, 26.7f), ImageUtils.getUserFirstTextBitmap(context, dChat.getNickName()), 5);
			}
			builder.setIcon(dChatIcon);
			builder.setPositiveButton(getString(R.string.okay_action),
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int id) {
							if (mSingleAttachment != null) {
								AttachmentView av = null;
								
								synchronized(MailChat.attachmentList) {
									av = MailChat.attachmentList.get(mSingleAttachment.getDownloadingId());
								}
								
								final AttachmentView attView = av;
								synchronized(MailChat.downloadingList) {
									if (av.mStatus == AttachmentView.Status.COMPLETE) {
										forwardFileToDMessage(attView, dChat);
										((Activity) context).finish();
									} else {
										forwardNoDownloadAttachment(av);
										av.setAttachmentListener(av.new AttachmentListener() {
									    	public void loadAttachmentFinished() {
									    		forwardFileToDMessage(attView, dChat);
									    		attView.setAttachmentListener(null);
									    		progressBarDilog.dismiss();
									    		progressBarDilog = null;
									    		((Activity) context).finish();
									    	}
									    	public void loadAttachmentFailed() {
									    		MailChat.toast(getString(R.string.forward_downloading_file_cancel_hint));
									    		attView.setAttachmentListener(null);
									    		progressBarDilog.dismiss();
									    		progressBarDilog = null;
									    		((Activity) context).finish();
									    	}
									    	public void progress(int progress) {
									    		progressBar.setProgress(progress);
									    	}
										});
										av.mIsTemp = true;
										
										if (av.mStatus == AttachmentView.Status.METADATA) {
											av.open();
										}
									}
								}
							} else if (mMessage != null) {
								DChatMessage dMessage =null;
								if(mMessage instanceof CMessage){
									dMessage = CMessage2DMessage((CMessage)mMessage);
								}else{
									dMessage =(DChatMessage)mMessage;
								}
								ChattingSingleActivity.actionForwardDMessage(context, dMessage, dChat, mAccount);
								((Activity) context).finish();
							}
							dialog.dismiss();
						}
					});
		}
		builder.setNeutralButton(context.getString(R.string.cancel_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}
	
	/**
	 * 将单聊消息转换为群聊消息
	 * 
	 * @Description:
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-9-12
	 */
	public CMessage DMessage2CMessage(DChatMessage dChatMessage){
		CMessage cMessage =new CMessage();		
		cMessage.setContent(dChatMessage.getMessageContent());
		cMessage.setMessageType(Type.values()[dChatMessage.getMessageType().ordinal()]);
		List<DAttachment> dAttachments =dChatMessage.getAttachments();
		if(dAttachments!=null&&dAttachments.size()>0){
			CAttachment cAttachment = new CAttachment();
			DAttachment att = dAttachments.get(0);
			cAttachment.setAttchmentId(att.getAttchmentId());
			cAttachment.setFilePath(att.getFilePath());
			cAttachment.setFileid(att.getFileid());
			cAttachment.setName(att.getName());
			cAttachment.setSize(att.getSize());
			cAttachment.setImageWidth(att.getImageWidth());
			cAttachment.setImageHeight(att.getImageHeight());
			cMessage.setAttachment(cAttachment);
		}
		return cMessage;
	}
	/**
	 * 将群聊消息转换为单聊消息
	 * 
	 * @Description:
	 * @param CMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-9-12
	 */
	public DChatMessage CMessage2DMessage(CMessage cMessage){
		DChatMessage dMessage =new DChatMessage(DChatMessage.Type.values()[cMessage.getMessageType().ordinal()]);	
		dMessage.setUuid(UUID.randomUUID().toString());
		dMessage.setMessageContent(cMessage.getContent());
		CAttachment cAtt =cMessage.getAttachment();
		if(cAtt!=null){
			ArrayList<DAttachment> atts =new ArrayList<DAttachment>();
			DAttachment att =new DAttachment();
			att.setAttchmentId(cAtt.getAttchmentId());
			att.setFilePath(cAtt.getFilePath());
			att.setFileid(cAtt.getFileid());
			att.setName(cAtt.getName());
			att.setSize(cAtt.getSize());
			att.setImageWidth(cAtt.getImageWidth());
			att.setImageHeight(cAtt.getImageHeight());
			atts.add(att);
			dMessage.setAttachments(atts);
		}
		return dMessage;
	}

	private void forwardNoDownloadFile(){
		MailDialog.Builder builder = new MailDialog.Builder(context);
		builder.setTitle(getString(R.string.forward_downloading_file_title));
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View progressLayout = inflater.inflate(R.layout.layout_progress, null);
		progressBar=(ProgressBar) progressLayout.findViewById(R.id.progress);
		builder.setCancelable(false);
		builder.setContentView(progressLayout);
		builder.setFourthButton(getString(R.string.forward_downloading_file_cancel_button),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						String attchmentId = null;
						if(mMessage instanceof CMessage){
							CMessage cMessage = (CMessage)mMessage;
							attchmentId=cMessage.getAttachment().getAttchmentId();
						}else{
							DChatMessage dMessage =(DChatMessage)mMessage;
							attchmentId=dMessage.getAttachments().get(0).getAttchmentId();
						}
						controller.cancelDownFile(mAccount, attchmentId,true);
						dialog.dismiss();
					}
				});
		progressBarDilog = builder.create();
		progressBarDilog.show();
		if(mMessage instanceof CMessage){
			CMessage cMessage = (CMessage)mMessage;
			controller.cGroupDownFile(mAccount, cMessage);
		}else{
			DChatMessage dMessage =(DChatMessage)mMessage;
			controller.dChatDownFile(mAccount, dMessage);
		}
	}

	private Uri getFilePathUri(String filePath){
		if (filePath != null && !filePath.startsWith("file://")) {
			filePath = "file://" + filePath;
		}else{
			filePath="";
		}
		return Uri.parse(filePath);
	}
	
	private void forwardNoDownloadAttachment(final AttachmentView attView){
		MailDialog.Builder builder = new MailDialog.Builder(context);
		builder.setTitle(getString(R.string.forward_downloading_file_title));
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View progressLayout = inflater.inflate(R.layout.layout_progress, null);
		progressBar=(ProgressBar) progressLayout.findViewById(R.id.progress);
		progressBar.setMax(100);
		builder.setCancelable(false);
		builder.setContentView(progressLayout);
		builder.setFourthButton(getString(R.string.forward_downloading_file_cancel_button),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						attView.mDownloadingNotification.isCanceled.set(true);
						dialog.dismiss();
					}
				});
		progressBarDilog = builder.create();
		progressBarDilog.show();
	}
	
	private void forwardFileToMessage(AttachmentView attView, CGroup cGroup) {
		File file = attView.saveAttachment();
		if (file != null && file.exists()) {
			CMessage cMessage =new CMessage();		
			cMessage.setContent(null);
			
			if (attView.mContentType.startsWith("image/")) {
				cMessage.setMessageType(CMessage.Type.IMAGE);
			} else {
				cMessage.setMessageType(CMessage.Type.ATTACHMENT);
			}
			
			CAttachment cAttachment = new CAttachment();
			cAttachment.setAttchmentId(UUID.randomUUID().toString());
			cAttachment.setFilePath(file.getPath());
			cAttachment.setFileid(null);
			cAttachment.setName(attView.mName);
			cAttachment.setSize(file.length());
			cMessage.setAttachment(cAttachment);
			
			ChattingActivity.actionForwardMessage(context, cMessage, cGroup, mAccount);
		} else {
			MailChat.toast(getString(R.string.forward_downloading_file_cancel_hint));
		}
	}
	
	private void forwardFileToDMessage(AttachmentView attView, DChat dChat) {
		File file = attView.saveAttachment();
		if (file != null && file.exists()) {
			DChatMessage dMessage = null;
			if (attView.mContentType.startsWith("image/")) {
				dMessage = new DChatMessage(DChatMessage.Type.IMAGE);
			} else {
				dMessage = new DChatMessage(DChatMessage.Type.ATTACHMENT);
			}
			dMessage.setUuid(UUID.randomUUID().toString());
			dMessage.setMessageContent(null);
			ArrayList<DAttachment> atts =new ArrayList<DAttachment>();
			DAttachment att =new DAttachment();
			att.setAttchmentId(UUID.randomUUID().toString());
			att.setFilePath(file.getPath());
			att.setFileid(null);
			att.setName(attView.mName);
			att.setSize(file.length());
			atts.add(att);
			dMessage.setAttachments(atts);
				
			ChattingSingleActivity.actionForwardDMessage(context, dMessage, dChat, mAccount);
		} else {
			MailChat.toast(getString(R.string.forward_downloading_file_cancel_hint));
		}
	}

	private void getCAttachmentPathForwardToMail(CMessage cMessage){
		CAttachment cAttachment = cMessage.getAttachment();
		String filePath ="";
		if(cAttachment!=null){
			filePath = cAttachment.getFilePath();
			if(StringUtil.isEmpty(filePath)){
				switch (cMessage.getMessageType()) {
				case IMAGE:
					filePath= MailChat.getInstance().getAttFilePath(MailChat.getInstance().getChatImageCacheDirectory(mAccount), cAttachment.getAttchmentId(), cAttachment.getName());
					break;
				case ATTACHMENT:
					filePath= MailChat.getInstance().getAttFilePath(MailChat.getInstance().getChatAttachmentDirectory(mAccount), cAttachment.getAttchmentId(), cAttachment.getName());
					break;
				default:
					filePath="";
					break;
				}
			}
		}
		if(!new File(filePath).exists()){
			noDownloadfilePath=filePath;
			noDownloadFileName=cAttachment.getName();
			forwardNoDownloadFile();
		}else{
			MailComposeActivity.actionForwardChatMessage(context, mAccount, eMail, null, cMessage.getContent(), getFilePathUri(filePath), cAttachment.getName());
			((Activity) context).finish();
		}
	}

	private void getDAttachmentPathForwardToMail(DChatMessage dMessage){
		List<DAttachment> dAttachments = dMessage.getAttachments();
		DAttachment dAttachment=null;
		String filePath ="";
		if(dAttachments!=null&&dAttachments.size()>0){
			dAttachment=dAttachments.get(0);
			filePath = dAttachment.getFilePath();
			if(StringUtil.isEmpty(filePath)){
				switch (dMessage.getMessageType()) {
				case IMAGE:
					filePath= MailChat.getInstance().getAttFilePath(MailChat.getInstance().getChatImageCacheDirectory(mAccount), dAttachment.getAttchmentId(), dAttachment.getName());
					break;
				case ATTACHMENT:
					filePath= MailChat.getInstance().getAttFilePath(MailChat.getInstance().getChatAttachmentDirectory(mAccount), dAttachment.getAttchmentId(), dAttachment.getName());
					break;
				default:
					filePath="";
					break;
				}
			}
		}
		if(!new File(filePath).exists()){
			noDownloadfilePath=filePath;
			noDownloadFileName=dAttachment.getName();
			forwardNoDownloadFile();
		}else{
			MailComposeActivity.actionForwardChatMessage(context, mAccount, eMail, null, dMessage.getMessageContent(), getFilePathUri(filePath), dAttachment.getName());
			((Activity) context).finish();
		}
	}
}
