package cn.mailchat.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.mail.store.Columns.TbContactRemark;
import cn.mailchat.mail.store.Columns.TbUserContacts;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.view.ChoseAddressView;
import cn.mailchat.view.RoundImageView;

/**
 * 
 * @copyright © 35.com
 * @file name ：CommonContactsAdapter.java
 * @author ：zhangjx
 * @create Data ：2014-10-17下午12:30:56
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-10-17下午12:30:56
 * @Modified by：zhangjx
 * @Description :常用联系人adapter
 */
public class CommonContactsAdapter extends BaseAdapter {
	private List<ContactAttribute> data = new ArrayList<ContactAttribute>();
	private LayoutInflater inflater;
	private Context mContext;
	private boolean isShowCheckbox = false;
	private boolean isCanCancleChecked = false;
	/**
	 * 复选框同步,由ContactsPersionAdapter传入
	 */
	private Set<ContactAttribute> selectedContacts;
	// 根据ComposeAddressView中值得变换，自动改变adapter中的选中状态
	private ChoseAddressView searchContactsEditView;
	protected int nickNameColor[] = { R.color.black, R.color.bg_title_right_txt };
	private DisplayImageOptions options;
	private List<CGroupMember> mMembers;
	private String contantName;

	public CommonContactsAdapter(Context context, boolean isShowCheckBox) {
		this.inflater = LayoutInflater.from(context);
		this.mContext = context;
		this.isShowCheckbox = isShowCheckBox;
		if (options == null) {
			initImageLoader();
		}
	}

	public CommonContactsAdapter(Context context, boolean isShowCheckBox,
			Set<ContactAttribute> selectedContacts,
			ChoseAddressView mSearchContacts,List<CGroupMember> mMembers) {
		this.inflater = LayoutInflater.from(context);
		this.mContext = context;
		this.selectedContacts = selectedContacts;
		this.searchContactsEditView = mSearchContacts;
		this.isShowCheckbox = isShowCheckBox;
		this.mMembers = mMembers;
		if (options == null) {
			initImageLoader();
		}
	}

	private void initImageLoader() {
		options = MailChat.getInstance().initImageLoaderOptions();
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(
					R.layout.item_fragment_contact_common_person, null);
			viewHolder.imgHead = (RoundImageView) convertView
					.findViewById(R.id.contact_image);
			viewHolder.contactName = (TextView) convertView
					.findViewById(R.id.contact_name);
			viewHolder.userCb = (CheckBox) convertView
					.findViewById(R.id.muliple_select_user_item_cb);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		ContactAttribute starContancts = data.get(position);
		contantName = starContancts.getNickName();
		String userNameChange=starContancts.getrNickName();
		if (!StringUtil.isEmpty(userNameChange)) {
			contantName=userNameChange;
		}

		viewHolder.contactName.setText(contantName);
		// 显示头像
		String userHeadUrl = starContancts.getImgHeadPath() != null ? starContancts
				.getImgHeadPath() + GlobalConstants.USER_SMALL_HEAD_END
				: "";
		ImageLoader.getInstance().displayImage(userHeadUrl, viewHolder.imgHead,
				MailChat.getInstance().initImageLoaderOptions(),
				new ImageLoadingListener() {

					@Override
					public void onLoadingStarted(String arg0, View arg1) {
					}

					@Override
					public void onLoadingFailed(String arg0, View arg1,
							FailReason arg2) {
					}

					@Override
					public void onLoadingComplete(String arg0, View arg1,
							Bitmap arg2) {
						// TODO Auto-generated method stub
						if (arg2 == null) {
							viewHolder.imgHead.setImageBitmap(ImageUtils
									.getUserFirstTextBitmap(mContext,
											contantName));
						}
					}

					@Override
					public void onLoadingCancelled(String arg0, View arg1) {
						// TODO Auto-generated method stub
					}
				});
		if (searchContactsEditView != null) {
			// 复选框多选设置start
			viewHolder.userCb.setChecked(selectedContacts
					.contains(starContancts));

			if (searchContactsEditView.isDuplicateAddress(starContancts
					.getEmail())) {

				selectedContacts.add(starContancts);
				viewHolder.userCb.setChecked(selectedContacts
						.contains(starContancts));
			} else {
				int i = 0;
				if (mMembers != null && mMembers.size() > 0&&!isCanCancleChecked) {
					for (CGroupMember member : mMembers) {
						if (member.getEmail().equals(starContancts.getEmail())) {
							viewHolder.userCb.setChecked(true);
							i = 1;
							break;
						}
					}
				}
				if (i == 0) {
					if (selectedContacts.contains(starContancts)) {
						selectedContacts.remove(starContancts);
					}
					viewHolder.userCb.setChecked(selectedContacts
							.contains(starContancts));
				}
			}
			viewHolder.contactName.setTextColor(mContext.getResources()
					.getColor(
							(viewHolder.userCb.isChecked()) ? nickNameColor[1]
									: nickNameColor[0]));
		}
		if (isShowCheckbox) {
			viewHolder.userCb.setVisibility(View.VISIBLE);
		} else {
			viewHolder.userCb.setVisibility(View.INVISIBLE);
		}
		return convertView;
	}

	public void setData(List<ContactAttribute> commData) {
		data.clear();
		data.addAll(commData);
		notifyDataSetChanged();
	}

	class ViewHolder {
		public RoundImageView imgHead;
		public TextView contactName;
		public CheckBox userCb;
	}

	public boolean isCanCancleChecked() {
		return isCanCancleChecked;
	}

	public void setCanCancleChecked(boolean isCanCancleChecked) {
		this.isCanCancleChecked = isCanCancleChecked;
	}

	public List<ContactAttribute> getData() {
		return data;
	}

	public boolean isShowCheckbox() {
		return isShowCheckbox;
	}

	public void setShowCheckbox(boolean isShowCheckbox) {
		this.isShowCheckbox = isShowCheckbox;
	}

	public ChoseAddressView getSearchContactsEditView() {
		return searchContactsEditView;
	}

	public void setSearchContactsEditView(ChoseAddressView searchContactsEditView,List<CGroupMember> mMembers) {
		this.searchContactsEditView = searchContactsEditView;
		this.mMembers = mMembers;
		notifyDataSetInvalidated();
	}

	public void setmMembers(List<CGroupMember> mMembers) {
		this.mMembers = mMembers;
	}
	public CGroupMember getMemberItem(int position) {
		return mMembers.get(position);
	}
}
