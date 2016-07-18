package cn.mailchat.adapter;

import java.util.List;

import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.utils.ImageUtils;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class InviteChatAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<ContactAttribute> contactList;

	private Context context;
	private Account account;
	private ContactsHolder holder;
	public InviteChatAdapter(Account account,Context context,List<ContactAttribute> contactList) {
		mInflater = LayoutInflater.from(context);
		this.account = account;
		this.context= context;
		this.contactList = contactList;
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
	public View getView(final int position, View view, ViewGroup parent) {
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
	
	public List<ContactAttribute> getContactList() {
		return contactList;
	}

	public void setContactList(List<ContactAttribute> contactList) {
		this.contactList = contactList;
	}
	
	class ContactsHolder {
		ImageView logo;
		TextView name;
		TextView email;
	}
}
