package cn.mailchat.view;

import java.io.File;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.activity.ForwardActivity;
import cn.mailchat.adapter.ChattingAdapter;
import cn.mailchat.adapter.DChattingAdapter;
import cn.mailchat.chatting.beans.CAttachment;
import cn.mailchat.chatting.beans.CMessage;
import cn.mailchat.chatting.beans.CMessage.Type;
import cn.mailchat.chatting.beans.DAttachment;
import cn.mailchat.chatting.beans.DChatMessage;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.utils.EncryptUtil;
import cn.mailchat.utils.FileUtil;
import cn.mailchat.utils.NetUtil;
import cn.mailchat.utils.StringUtil;

public class ChattingShowDialog extends Dialog {

	private Context context;
	private Account mAccount;
	private DialogAdapter mAdapter;
	private String[] values;
	public static final int COPY = 0;
	public static final int FORWARD = 1;
	public static final int DEL = 2;
	public static final int SENDER = 3;
	public static final int OPEN=4;
	public static final int DOWNLOAD=5;
	public static final int CANCEL_DOWNLOAD=6;
	
	private List<Integer> modes;

	private RoundedRectListView listView;

	private CMessage mMessage;
	private ChattingAdapter adapter;
	
	//单聊相关
	private DChatMessage mDMessage;
	private DChattingAdapter dChatAdapte;
	private boolean isDMessage;
	public ChattingShowDialog(Context context) {
		super(context);
		this.context = context;
	}
	
	public ChattingShowDialog(Context context, DChattingAdapter adapter, int theme, List<Integer> modes, DChatMessage mDMessage,Account mAccount) {
		super(context, theme);
		this.context = context;
		this.modes = modes;
		this.dChatAdapte = adapter;
		this.mDMessage = mDMessage;
		this.mAccount=mAccount;
		isDMessage=true;
		values = context.getResources().getStringArray(R.array.chat_long_press_titles);
	}
	
	public ChattingShowDialog(Context context, ChattingAdapter adapter, int theme, List<Integer> modes, CMessage mMessage,Account mAccount) {
		super(context, theme);
		this.context = context;
		this.modes = modes;
		this.adapter = adapter;
		this.mMessage = mMessage;
		this.mAccount=mAccount;
		values = context.getResources().getStringArray(R.array.chat_long_press_titles);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_chatting);
		mAdapter = new DialogAdapter();
		setCancelable(true);

		listView = (RoundedRectListView) findViewById(R.id.value_list);
		listView.setAdapter(mAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onClick(position);
				ChattingShowDialog.this.cancel();

			}

		});

	}

	private void onClick(int position) {
		switch (modes.get(position)) {
		case COPY:
			if(isDMessage){
				textCopy(context, mDMessage.getMessageContent());
			}else{
				textCopy(context, mMessage.getContent());	
			}
			Toast.makeText(context, context.getString(R.string.copy_content), Toast.LENGTH_SHORT).show();
			break;
		case FORWARD:
			if(isDMessage){
				ForwardActivity.actionForChat(context,mDMessage,mAccount);
			}else{
				ForwardActivity.actionForChat(context, mMessage, mAccount);
			}
			break;
		case DEL:
			if(isDMessage){
				dChatAdapte.deleteMessage(mDMessage.getUuid());
				if(mDMessage.getMessageType()==DChatMessage.Type.VOICE){
					dChatAdapte.stopPlay(mDMessage.getAttachments().get(0).getAttchmentId());
				}
			}else{
				adapter.deleteMessage(mMessage.getUid());
				if(mMessage.getMessageType()==Type.VOICE){
					adapter.stopPlay(mMessage.getAttachment().getAttchmentId());
				}
			}
			break;
		case SENDER:		
			if(isDMessage){
				dChatAdapte.senderMessageAgain(mDMessage);
			}else{
				adapter.senderMessageAgain(mMessage);
			}
//			Toast.makeText(context, "重发", Toast.LENGTH_SHORT).show();
			break;
		case OPEN:
			String openFilePath=null;
			String attchmentId=null;
			String fileName=null;
			if(isDMessage){
				if(mDMessage.getMessageType()==DChatMessage.Type.ATTACHMENT){
					DAttachment attach = mDMessage.getAttachments().get(0);
					openFilePath = attach.getFilePath();
					attchmentId= attach.getAttchmentId();
					fileName= attach.getName();
				}
			}else{
				if(mMessage.getMessageType()==Type.ATTACHMENT){
					CAttachment attach = mMessage.getAttachment();
					openFilePath = attach.getFilePath();
					attchmentId= attach.getAttchmentId();
					fileName= attach.getName();
				}
			}
			if(StringUtil.isEmpty(openFilePath)){
				openFilePath =MailChat.getInstance().getAttFilePath(MailChat.getInstance().getChatAttachmentDirectory(mAccount), attchmentId,fileName);
			}
			// 打开
			FileUtil.viewFile(context, openFilePath, null,fileName);
			break;
		case DOWNLOAD:
			if(isDMessage){
				if(mDMessage.getMessageType()==DChatMessage.Type.ATTACHMENT){
					DAttachment attach = mDMessage.getAttachments().get(0);
					String filePath = attach.getFilePath();
					if(StringUtil.isEmpty(filePath)){
						filePath = MailChat.getInstance().getAttFilePath(MailChat.getInstance().getChatAttachmentDirectory(mAccount), attach.getAttchmentId(),attach.getName());
					}
					if (!NetUtil.isActive()) {
						NetUtil.showNoConnectedAlertDlg(context);
						return;
					}
					MessagingController.getInstance(MailChat.getInstance()).dChatDownFile(mAccount, mDMessage);
				}
			}else{
				if(mMessage.getMessageType()==Type.ATTACHMENT){
					CAttachment attach = mMessage.getAttachment();
					String filePath = attach.getFilePath();
					if(StringUtil.isEmpty(filePath)){
						filePath = MailChat.getInstance().getAttFilePath(MailChat.getInstance().getChatAttachmentDirectory(mAccount), attach.getAttchmentId(),attach.getName());
					}
					if (!NetUtil.isActive()) {
						NetUtil.showNoConnectedAlertDlg(context);
						return;
					}
					MessagingController.getInstance(MailChat.getInstance()).cGroupDownFile(mAccount, mMessage);
				}
			}
			break;
		case CANCEL_DOWNLOAD:
			if(isDMessage){
				if(mDMessage.getMessageType()==DChatMessage.Type.ATTACHMENT){
					DAttachment attach = mDMessage.getAttachments().get(0);
					String filePath =MailChat.getInstance().getChatAttachmentDirectory(mAccount)+EncryptUtil.getMd5(attach.getAttchmentId())+".tem";
					File file =new File(filePath);
					if(file.exists()){
						file.delete();
					}
					MessagingController.getInstance(MailChat.getInstance()).cancelDownFile(mAccount, attach.getAttchmentId(),false);
					List<DChatMessage> dMessages = dChatAdapte.getmDChatMessages();
					for (int i = 0; i < dMessages.size(); i++) {
						DChatMessage dMessage = dMessages.get(i);
						if (dMessage.getMessageType() == DChatMessage.Type.ATTACHMENT) {
							DAttachment dAttachment = dMessage.getAttachments().get(0);
							if (dAttachment.getAttchmentId().equals(attach.getAttchmentId())) {
								dAttachment.setDownloadPause(true);
								dAttachment.setDownloadProgress(0);
								dChatAdapte.updateView(i);
								MessagingController.getInstance(MailChat.getInstance()).updateDChatDownFileState(mAccount, dAttachment);
								break;
							}
						}
					}
				}
			}else{
				if(mMessage.getMessageType()==Type.ATTACHMENT){
					CAttachment attach = mMessage.getAttachment();
					String filePath =MailChat.getInstance().getChatAttachmentDirectory(mAccount)+EncryptUtil.getMd5(attach.getAttchmentId())+".tem";
					File file =new File(filePath);
					if(file.exists()){
						file.delete();
					}
					MessagingController.getInstance(MailChat.getInstance()).cancelDownFile(mAccount, attach.getAttchmentId(),false);
					List<CMessage> cMessages = adapter.getcMessages();
					for (int i = 0; i < cMessages.size(); i++) {
						CMessage cMessage = cMessages.get(i);
						if (cMessage.getMessageType() == Type.ATTACHMENT) {
							CAttachment cAttachment = cMessage.getAttachment();
							if (cAttachment.getAttchmentId().equals(attach.getAttchmentId())) {
								cAttachment.setDownloadPause(true);
								cAttachment.setDownloadProgress(0);
								adapter.updateView(i);
								MessagingController.getInstance(MailChat.getInstance()).updateCGroupDownFileState(mAccount, cAttachment);
								break;
							}
						}
					}
				}
			}
			break;
		default:
			break;
		}
	}

	class DialogAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public DialogAdapter() {
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return modes.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = mInflater.inflate(R.layout.item_chatting_show_dialog, parent, false);
			TextView text = (TextView) view.findViewById(R.id.text);

			text.setText(values[modes.get(position)]);

			return view;
		}

	}

	/**
	 * 文本复制
	 * 
	 * @Description:
	 * @param text
	 * @param context
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2014-2-28
	 */
	public void textCopy(Context context, String text) {
		// 得到剪贴板管理器
		ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		cmb.setText(text.trim());
	}
}
