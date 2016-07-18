package cn.mailchat.view;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.DChat;
import cn.mailchat.controller.MessagingController;

public class ChatListDialog extends Dialog {
	private Context context;
	private Account mAccount;
	private DialogAdapter mAdapter;

	private String[] values;

	public static final int TOP = 0;
	public static final int DEL = 1;
	public static final int CANCELTOP = 2;

	private List<Integer> modes;
	private CGroup cGroup;
	private DChat dChat;
	private RoundedRectListView listView;
	private boolean isGroup;
	private MessagingController controller;

	public ChatListDialog(Context context, int theme, List<Integer> modes,
			Account mAccount, CGroup cGroup) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.controller = MessagingController.getInstance(MailChat.app);
		this.mAccount = mAccount;
		this.cGroup = cGroup;
		this.modes = modes;
		this.isGroup = true;
		values = context.getResources().getStringArray(R.array.chat_list_long_press_titles);
	}

	public ChatListDialog(Context context, int theme, List<Integer> modes,
			Account mAccount, DChat dChat) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.controller = MessagingController.getInstance(MailChat.app);
		this.mAccount = mAccount;
		this.dChat = dChat;
		this.modes = modes;
		this.isGroup = false;
		values = context.getResources().getStringArray(R.array.chat_list_long_press_titles);
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
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				onClick(position);
				ChatListDialog.this.cancel();
			}

		});
	}

	private void onClick(int position) {
		switch (modes.get(position)) {
		case CANCELTOP:
			if (isGroup) {
				controller.stickGroup(mAccount, cGroup.getUid(), false, null);
			} else {
				controller.setDChatStickMsgTop(mAccount, dChat.getUid(), false,
						null);
			}
			break;
		case TOP:
			if (isGroup) {
				controller.stickGroup(mAccount, cGroup.getUid(), true, null);
			} else {
				controller.setDChatStickMsgTop(mAccount, dChat.getUid(), true,
						null);
			}
			break;
		case DEL:
			if (isGroup) {
				controller.hiddenGroup(mAccount, cGroup.getUid(), true, null);
			} else {
				controller.deleteDChat(mAccount, dChat.getUid(), null);
			}
			break;
		default:
			break;
		}
	}

	class DialogAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public DialogAdapter() {
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			View view = mInflater.inflate(R.layout.item_chatting_show_dialog,
					parent, false);
			TextView text = (TextView) view.findViewById(R.id.text);
			text.setText(values[modes.get(position)]);
			return view;
		}
	}

}