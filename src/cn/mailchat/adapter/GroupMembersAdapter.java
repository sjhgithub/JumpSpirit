package cn.mailchat.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.utils.CGroupMenberComparator;
import cn.mailchat.utils.ContactComparator;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.view.RoundImageView;

/**
 * 组成员视图
 * 
 * @Description:
 * @author:huangyx2
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-5-20
 */
public class GroupMembersAdapter extends BaseAdapter implements OnClickListener {

	private List<CGroupMember> mGroupMembers = new ArrayList<CGroupMember>();
	private Context mContext;
	// 首字母及对应位置
	private Map<Character, Integer> positionMap = new HashMap<Character, Integer>();

	private Account mAccount;

	private MessagingController controller;
	private String mGroupUid;
	private CGroup mGroup;
	// 用于判断按钮是否点击了减号删除
	private Map<Integer, Integer> delMemberMap = new HashMap<Integer, Integer>();
	// 显示的是否为搜索结果
	protected boolean isShowSearchResult = false;
	// 获取tagtv高度
	private int tagHeight = 0;
	// 所有联系人数量
	private int normalCount = 0;

	public GroupMembersAdapter(String groupUid, Context context, String uuid,
			Application mApplication) {
		this.mContext = context;
		mAccount = Preferences.getPreferences(mContext).getDefaultAccount();
		controller = MessagingController.getInstance(mApplication);
		this.mGroupUid = groupUid;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mGroupMembers.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		UserItemHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.fetch_members_listitem, null);
			holder = new UserItemHolder();
			holder.tagTv = (TextView) convertView
					.findViewById(R.id.muliple_select_user_item_tag);
			holder.nameTv = (TextView) convertView
					.findViewById(R.id.tvView_user_name_tv);
			holder.emailTv = (TextView) convertView
					.findViewById(R.id.tvView_user_email);
			holder.week_show = (TextView) convertView
					.findViewById(R.id.messagelist_week_show);
			holder.groupLayout = (LinearLayout) convertView
					.findViewById(R.id.layoutView_group);
			holder.delMemberLv = (ImageView) convertView
					.findViewById(R.id.lvView_del_member);
			holder.delMemberOtherTv = (TextView) convertView
					.findViewById(R.id.tvView_del_member);
			holder.topView = convertView
					.findViewById(R.id.view_select_top_view);
			holder.bottomView = convertView
					.findViewById(R.id.view_select_bottom_view);
			holder.memberImgHead = (RoundImageView) convertView
					.findViewById(R.id.contact_img_head);
			holder.inviteMember = (TextView) convertView
					.findViewById(R.id.tvView_inviter_user);
			holder.delMemberLv.setOnClickListener(this);
			holder.delMemberOtherTv.setOnClickListener(this);

			convertView.setTag(holder);

		} else {
			holder = (UserItemHolder) convertView.getTag();
		}
		if (tagHeight == 0) {
			holder.tagTv.measure(0, 0);
			tagHeight = holder.tagTv.getMeasuredHeight();
		}

		holder.delMemberLv.setTag(position);
		holder.delMemberOtherTv.setTag(position);

		fillView(holder, mGroupMembers.get(position), position);
		return convertView;
	}

	private void fillView(final UserItemHolder holder,
			final CGroupMember groupMember, int position) {
		if (groupMember.isAdmin()) {
			holder.nameTv.setText(groupMember.getNickName()
					+ mContext.getString(R.string.group_admin));
			holder.emailTv.setText(groupMember.getEmail());
			holder.delMemberLv.setVisibility(View.GONE);
			holder.delMemberOtherTv.setVisibility(View.GONE);
			holder.inviteMember.setVisibility(View.GONE);
		} else {
			holder.nameTv.setText(groupMember.getNickName());
			holder.emailTv.setText(groupMember.getEmail());
			if (mGroup != null && mGroup.getIsAdmin()
					&& delMemberMap.containsKey(position)) {
				holder.delMemberLv.setVisibility(View.GONE);
				holder.delMemberOtherTv.setVisibility(View.VISIBLE);
			} else if (mGroup != null && mGroup.getIsAdmin()) {
				holder.delMemberLv.setVisibility(View.VISIBLE);
				holder.delMemberOtherTv.setVisibility(View.GONE);
			}
			if (groupMember.isInviteMember()) {
//				holder.delMemberLv.setVisibility(View.GONE);
//				holder.delMemberOtherTv.setVisibility(View.GONE);
				holder.inviteMember.setVisibility(View.VISIBLE);
			} else {
				holder.inviteMember.setVisibility(View.GONE);
			}
		}
		holder.tagTv.setVisibility(View.GONE);
		// 设置头像
		holder.memberImgHead.setVisibility(View.VISIBLE);
		if (starCount > 0 && !isShowSearchResult) {
			if (position == 0) {
				holder.tagTv.setVisibility(View.VISIBLE);
				holder.tagTv.setText(formteString(R.string.star_contacts,
						starCount));
			}
		}
		if (position == starCount && !isShowSearchResult) {
			holder.tagTv.setVisibility(View.VISIBLE);
			holder.tagTv.setText(formteString(R.string.all_contacts,
					normalCount));
		}
		//头像设置
		String avatarHash = groupMember.getAvatarHash() != null ? GlobalConstants.HOST_IMG +groupMember.getAvatarHash()
				+ GlobalConstants.USER_SMALL_HEAD_END : "";
		ImageLoader.getInstance().displayImage(avatarHash, holder.memberImgHead,
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
							holder.memberImgHead.setImageBitmap(ImageUtils.getUserFirstTextBitmap(mContext, groupMember.getNickName()));
						}
					}

					@Override
					public void onLoadingCancelled(String arg0, View arg1) {
						// TODO Auto-generated method stub
					}
				});
		/**
		 * 按字母进行分组
		 */

		if (positionMap != null
				&& getPosition(groupMember.getFirstChar()) == position) {

			holder.groupLayout.setVisibility(View.VISIBLE);
			holder.week_show.setText(groupMember.getFirstChar().toString());
			holder.topView.setVisibility(View.VISIBLE);
			if (mGroupMembers.size() > position + 1) {
				if (getPosition(mGroupMembers.get(position + 1).getFirstChar()) == position + 1) {
					holder.bottomView.setVisibility(View.GONE);
				} else {
					holder.bottomView.setVisibility(View.VISIBLE);
				}
			}
		} else {
			holder.groupLayout.setVisibility(View.GONE);
			holder.topView.setVisibility(View.GONE);
			if (mGroupMembers.size() > position + 1) {
				if (getPosition(mGroupMembers.get(position + 1).getFirstChar()) == position + 1) {
					holder.bottomView.setVisibility(View.GONE);
				} else {
					holder.bottomView.setVisibility(View.VISIBLE);
				}
			} else {
				holder.bottomView.setVisibility(View.VISIBLE);
			}
		}

	}

	class UserItemHolder {

		TextView tagTv;
		TextView nameTv;
		TextView emailTv;
		ImageView delMemberLv;
		TextView delMemberOtherTv;
		LinearLayout groupLayout;
		TextView week_show;
		View topView;
		View bottomView;
		RoundImageView memberImgHead;
		TextView inviteMember;
	}

	public void setData(List<CGroupMember> groupMembers) {

		if (this.mGroupMembers == null) {
			this.mGroupMembers = new ArrayList<CGroupMember>();
		}

		this.mGroupMembers.clear();
		this.normalCount = groupMembers.size();
		groupMembers = restructureContacts(groupMembers);
		if (groupMembers != null && groupMembers.size() > 0) {
			for (CGroupMember member : groupMembers) {
				if (member.isAdmin()) {// 管理员
					this.mGroupMembers.add(member);
				}
			}
			for (CGroupMember member : groupMembers) {
				if (!member.isAdmin()) {// 非管理员
					this.mGroupMembers.add(member);
				}
			}
		}

		// 获取所有数据的不重复的首字母以及对应的索引
		positionMap = getMapPositionChar();
		notifyDataSetChanged();
	}

	public List<CGroupMember> getData() {
		return this.mGroupMembers;
	}

	public void setGroup(CGroup mGroup) {
		this.mGroup = mGroup;
	}

	// 常用联系人数量
	private int starCount = 0;

	protected Map<Character, Integer> getMapPositionChar() {
		positionMap.clear();
		for (int i = 0; i < mGroupMembers.size(); i++) {
			CGroupMember ub = mGroupMembers.get(i);
			if (ub.isAdmin() != true
					&& !positionMap.containsKey(ub.getFirstChar())) {
				positionMap.put(ub.getFirstChar(), i + starCount);
			}
		}
		return positionMap;
	}

	/**
	 * 获取首字母对应的listview的索引
	 * 
	 * @Description:
	 * @param c
	 * @return
	 * @see:
	 * @since:
	 * @author: Zhonggaoyong
	 * @date:2013-11-21
	 */
	public int getPosition(char c) {
		if (c == '★') {
			return 0;
		}
		if (positionMap == null) {
			return -1;
		}
		Integer i = positionMap.get(c);
		if (i == null) {
			return -1;
		}
		return i;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvView_del_member:
		case R.id.lvView_del_member:
			int position = (Integer) v.getTag();
			if (delMemberMap.containsKey(position)) {
				//重新传入一个集合是避免，删除线程和主线程同时操作mGroupMembers集合引起ConcurrentModificationException异常。
				controller.deleteGroupMember(mAccount, mGroupUid,
						new ArrayList<CGroupMember>(mGroupMembers),position,null);
				delMemberMap.remove(position);
			} else {
				delMemberMap.put(position, position);
				notifyDataSetChanged();
			}
			break;

		default:
			break;
		}

	}

	// 比较器异常，搜索后任务是jdk1.7以后sort函数的实现变了
	public class SortComparator implements Comparator<CGroupMember> {

		@Override
		public int compare(CGroupMember lhs, CGroupMember rhs) {
			try {
				if (lhs.isAdmin() && rhs.isAdmin()) {
					return 0;
				} else if (lhs.isAdmin()) {
					return -1;
				} else {
					return 1;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}

		}
	}

	/**
	 * 获取tagtv高度
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: Zhonggaoyong
	 * @date:2013-12-30
	 */
	public int getTagHeight() {
		return tagHeight;
	}

	private String formteString(int resId, int value) {
		return String.format(mContext.getString(resId), value);
	}

	/**
	 * 
	 * method name: restructureContacts function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param groupMembers
	 * @return field_name List<CGroupMember> return type
	 * @History memory：
	 * @Date：2014-12-10 上午11:53:37 @Modified by：zhangjx
	 * @Description：群成员排序
	 */
	synchronized public List<CGroupMember> restructureContacts(
			List<CGroupMember> groupMembers) {
		ArrayList<CGroupMember> tempContactAttributes = new ArrayList<CGroupMember>();
		tempContactAttributes.addAll(groupMembers);
		Collections.sort(tempContactAttributes, new CGroupMenberComparator());
		return tempContactAttributes;
	}
}
