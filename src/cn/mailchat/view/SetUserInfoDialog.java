package cn.mailchat.view;

import java.util.List;

import android.annotation.SuppressLint;
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

public class SetUserInfoDialog extends Dialog {
	private Context context;
	private DialogAdapter mAdapter;

	private String[] values;

	public static final int LOCAL_IMAGE = 0;
	public static final int TAKE_PHOTOS = 1;
	public static final int INCOMING_SETTING = 2;
	public static final int OUTCOMING_SETTING = 3;
	public static final int CHAT_SING = 4;
	public static final int CHAT_GROUP = 5;
	private List<Integer> modes;
	private RoundedRectListView listView;
	private SetUserInfoDialogListener setUserInfoDialogListener;
	private String coustomStr = null;
	private int pos = -1;

	public SetUserInfoDialog(Context context, int theme, List<Integer> modes,
			SetUserInfoDialogListener setUserInfoDialogListener) {
		super(context, theme);
		this.context = context;
		this.modes = modes;
		this.setUserInfoDialogListener = setUserInfoDialogListener;
		values = context.getResources().getStringArray(R.array.set_user_info);
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
				setUserInfoDialogListener.onDialogClick(position);
				SetUserInfoDialog.this.cancel();
			}

		});
	}

	public void onClick(int position) {
		switch (modes.get(position)) {
		case TAKE_PHOTOS:
			break;
		case LOCAL_IMAGE:
			break;
		case INCOMING_SETTING:
			break;
		case OUTCOMING_SETTING:
			break;
		case CHAT_SING:
			break;
		case CHAT_GROUP:
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

		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = mInflater.inflate(R.layout.item_chatting_show_dialog,
					parent, false);
			TextView text = (TextView) view.findViewById(R.id.text);
			if (getPos() != -1 && getCoustomStr() != null
					&& modes.get(position) == CHAT_SING) {
				text.setText(values[modes.get(getPos())] + getCoustomStr());
			} else {
				text.setText(values[modes.get(position)]);
			}
			return view;
		}
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public String getCoustomStr() {
		return coustomStr;
	}

	public void setCoustomStr(String coustomStr) {
		this.coustomStr = coustomStr;
	}

	public interface SetUserInfoDialogListener {
		void onDialogClick(int position);
	}
}