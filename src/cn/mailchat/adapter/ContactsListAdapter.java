package cn.mailchat.adapter;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.chatting.beans.CMessage;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.view.ChoseAddressView;
/**
 * 
 * @copyright © 35.com
 * @file name ：ContactsListAdapter.java
 * @author ：zhangjx
 * @create Data ：2015-9-24下午3:45:35
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2015-9-24下午3:45:35
 * @Modified by：zhangjx
 * @Description :企業通訊錄和個人通訊錄列表适配器
 */
public class ContactsListAdapter extends BaseContactsAdapter {

	// 首字母及对应位置
	private Map<Character, Integer> positionMap = new HashMap<Character, Integer>();
	private boolean isShowCheckbox = false;
	private boolean isCanCancleChecked = false;
	private List<CGroupMember> mMembers;
	/**
	 * 复选框同步
	 */
	private Set<ContactAttribute> selectedContacts;
	// 根据ComposeAddressView中值得变换，自动改变adapter中的选中状态
	private ChoseAddressView searchContactsEditView;
	public ContactsListAdapter(Context context,Set<ContactAttribute> selectedContacts,ChoseAddressView mSearchContacts) {
		super(context);
		this.mContext = context;
		this.searchContactsEditView = mSearchContacts;
		this.selectedContacts=selectedContacts;
		super.isShowSearchResult = true;// 不显示联系人数量
	}

	@Override
	public int getCount() {
		return allContacts.size() == 0 ? 1 : allContacts.size();
	}

	@Override
	public ContactAttribute getItem(int position) {
		return allContacts.get(position);
	}
	
	public CGroupMember getMemberItem(int position) {
		return mMembers.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		UserItemHolder holder = super.initView(convertView);

		if (allContacts == null || allContacts.size() == 0) {
			holder.convertView.setVisibility(View.INVISIBLE);
			return holder.convertView;
		}

		if (holder.convertView.getVisibility() == View.INVISIBLE) {
			holder.convertView.setVisibility(View.VISIBLE);
		}

		ContactAttribute user = allContacts.get(position);

		super.initData(holder, user, position);
		// 复选框多选设置start
		holder.userCb.setChecked(selectedContacts.contains(user));

		if (searchContactsEditView != null) {

			if (searchContactsEditView.isDuplicateAddress(user.getEmail())) {

				selectedContacts.add(user);
				holder.userCb.setChecked(selectedContacts.contains(user));
			} else {
				int i = 0;
				if (mMembers != null && mMembers.size() > 0&&!isCanCancleChecked) {
					for (CGroupMember member : mMembers) {
						if (member.getEmail().equals(user.getEmail())) {
							holder.userCb.setChecked(true);
							i = 1;
							break;
						}
					}
				}
				if (i == 0) {
					if (selectedContacts.contains(user)) {
						selectedContacts.remove(user);
					}
					holder.userCb.setChecked(selectedContacts.contains(user));
				}
			}
		}

		if (!isShowCheckbox) {
			if (holder.userCb.getVisibility() == View.VISIBLE)
				holder.userCb.setVisibility(View.GONE);
		} else {
			if (holder.userCb.getVisibility() == View.GONE)
				holder.userCb.setVisibility(View.VISIBLE);
		}
		
		// 复选框多选end

		/**
		 * 按字母进行分组
		 */
		if (positionMap != null && getPosition(user.getFirstChar()) == position) {

			holder.groupLayout.setVisibility(View.VISIBLE);

			holder.week_show.setText(user.getFirstChar().toString());
			holder.topView.setVisibility(View.VISIBLE);

			if (allContacts.size() > position + 1) {
				if (getPosition(allContacts.get(position + 1).getFirstChar()) == position + 1) {
					holder.bottomView.setVisibility(View.GONE);

				} else {
					holder.bottomView.setVisibility(View.VISIBLE);
				}
			}
		} else {
			holder.groupLayout.setVisibility(View.GONE);
			holder.topView.setVisibility(View.GONE);

			if (position != 0) {

				if (allContacts.size() > position + 1) {

					if (getPosition(allContacts.get(position + 1)
							.getFirstChar()) == position + 1) {
						holder.bottomView.setVisibility(View.GONE);

					} else {
						holder.bottomView.setVisibility(View.VISIBLE);
					}
				} else {
					holder.bottomView.setVisibility(View.VISIBLE);
				}
			}
		}
		// 常用联系人标题设置
		if (position == 0) {
			holder.groupLayout.setVisibility(View.VISIBLE);
//			holder.week_show.setText(R.string.star_contacts1);
			holder.topView.setVisibility(View.VISIBLE);
			holder.bottomView.setVisibility(View.VISIBLE);
		}
		holder.nameTv.setTextColor(mContext.getResources().getColor(
				(holder.userCb.isChecked()) ? nickNameColor[1]
						: nickNameColor[0]));
		holder.emailTv.setTextColor(mContext.getResources().getColor(
				(holder.userCb.isChecked()) ? emailColor[1] : emailColor[0]));
		return holder.convertView;
	}

	@Override
	public void setContacts(List<ContactAttribute> starUsers,
			List<ContactAttribute> normalUsers) {

		super.setContacts(starUsers, normalUsers);

		// 获取所有数据的不重复的首字母以及对应的索引
		positionMap = super.getMapPositionChar();

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
			return -2;
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
	public boolean isShowCheckbox() {
		return isShowCheckbox;
	}
	public void setShowCheckbox(boolean isShowCheckbox) {
		this.isShowCheckbox = isShowCheckbox;
	}
	public void setmMembers(List<CGroupMember> mMembers) {
		this.mMembers = mMembers;
	}
    /** 根据索引获取位置*/
    public int getPositionByIndex(char indexLetter) {
		Integer value = positionMap.get(indexLetter);
        if(value == null) {
            return -1;
        }
        return value;
    }
	public ChoseAddressView getSearchContactsEditView() {
		return searchContactsEditView;
	}
	public void setSearchContactsEditView(ChoseAddressView searchContactsEditView,List<CGroupMember> mMembers) {
		this.searchContactsEditView = searchContactsEditView;
		this.mMembers = mMembers;
		notifyDataSetInvalidated();
	}

	public boolean isCanCancleChecked() {
		return isCanCancleChecked;
	}

	public void setCanCancleChecked(boolean isCanCancleChecked) {
		this.isCanCancleChecked = isCanCancleChecked;
	}

}
