package cn.mailchat.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.helper.TreeHelper;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.view.ChoseAddressView;
import cn.mailchat.view.RoundImageView;

import android.R.integer;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Contact35EisAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	protected Context mContext;
	protected LayoutInflater mInflater;
	private boolean isCanCancleChecked = false;
	/**
	 * 存储所有的TreeBeen
	 */
	protected List<ContactAttribute> mAllTreeBeens;
	/**
	 * 存储所有可见的TreeBeen
	 */
	protected List<ContactAttribute> mTreeBeens;
	/**
	 * 点击的回调接口
	 */
	private OnTreeClickListener onTreeClickListener;
	private DisplayImageOptions options;
	// 是否显示checkbox
	private boolean isShowCheckbox = false;
	/**
	 * 复选框同步,由ContactsPersionAdapter传入
	 */
	private Set<ContactAttribute> selectedContacts;
	/**
	 * group复选框同步,由ContactsPersionAdapter传入
	 */
	private Set<String> selectedGroupStr;
	// 根据ComposeAddressView中值得变换，自动改变adapter中的选中状态
	private ChoseAddressView searchContactsEditView;
	protected int nickNameColor[] = { R.color.chat_up,
			R.color.bg_title_right_txt };
	protected int emailColor[] = { R.color.chat_down,
			R.color.bg_title_right_txt };
	private boolean isChooseEmailListView = false;// 如果是填充选择邮箱地址的时候
	private List<CGroupMember> mMembers;
	private ViewHolder mHolder = null;

	/**
	 * 
	 * @param listView
	 * @param context
	 * @param datas
	 * @param defaultExpandLevel
	 *            默认展开几级树
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public Contact35EisAdapter(Context context, final ListView listView,
			Set<ContactAttribute> selectedContacts, Set<String> selectedGroupStr,boolean isShowCheckbox,
			int defaultExpandLevel, ChoseAddressView searchContacts,
			List<CGroupMember> mMembers) {
		this.mContext = context;
		this.isShowCheckbox = isShowCheckbox;
		this.mInflater = LayoutInflater.from(context);
		this.selectedContacts = selectedContacts;
		this.selectedGroupStr=selectedGroupStr;
		this.searchContactsEditView = searchContacts;
		this.mMembers = mMembers;
		if (mAllTreeBeens == null) {
			mAllTreeBeens = new ArrayList<ContactAttribute>();
		}
		/**
		 * 过滤出可见的TreeBeen
		 */
		mTreeBeens = TreeHelper.filterVisibleTreeBeen(mAllTreeBeens);
		/**
		 * 设置节点点击时，可以展开以及关闭；并且将ItemClick事件继续往外公布
		 */
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (listView.getHeaderViewsCount() > 0) {
					position = position - listView.getHeaderViewsCount();
				}
				ContactAttribute treeBeen = mTreeBeens.get(position);
				expandOrCollapse(position);
				if (onTreeClickListener != null) {
					if (!treeBeen.isParent()) {
						onTreeClickListener.onItemClick(parent, view, treeBeen,
								position);
					} else {
						onTreeClickListener
								.onGroupClick(parent, view, treeBeen);
					}
				}
			}
		});
		if (options == null) {
			initImageLoader();
		}
	}

	private void initImageLoader() {
		options = MailChat.getInstance().initImageLoaderOptions();
	}

	/**
	 * 相应ListView的点击事件 展开或关闭某节点
	 * 
	 * @param position
	 */
	public void expandOrCollapse(int position) {
		ContactAttribute n = mTreeBeens.get(position);
		// 排除传入参数错误异常
		if (n != null) {
			if (!n.isLeaf()) {
				n.setExpand(!n.isExpand());
				mTreeBeens = TreeHelper.filterVisibleTreeBeen(mAllTreeBeens);
				notifyDataSetChanged();// 刷新视图
			}
		}
	}

	@Override
	public int getCount() {
		return mTreeBeens.size();
	}

	@Override
	public Object getItem(int position) {
		return mTreeBeens.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ContactAttribute treeBeen = mTreeBeens.get(position);
		convertView = getConvertView(treeBeen, position, convertView, parent);
		// 设置内边距
		convertView.setPadding(treeBeen.getLevel() * 50, 0, 0, 0);
		return convertView;
	}

	private View getConvertView(final ContactAttribute treeBeen, final int position,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			if (inflater == null) {
				inflater = LayoutInflater.from(mContext);
			}
			convertView = inflater.inflate(R.layout.item_35_eis, null);
			mHolder = new ViewHolder();
			mHolder.eisGroupLayout = (RelativeLayout) convertView
					.findViewById(R.id.layout_eis_group);
			mHolder.mTitle = (TextView) convertView.findViewById(R.id.tv_title);
			mHolder.mImgArrow = (ImageView) convertView
					.findViewById(R.id.img_arrow);
			mHolder.imgLeader = (ImageView) convertView
					.findViewById(R.id.img_leader);
			mHolder.mChildCount = (TextView) convertView
					.findViewById(R.id.tv_count);
			mHolder.groupCheckbox = (CheckBox) convertView
					.findViewById(R.id.checkbox_group);
			mHolder.mLayoutCheckbox = (LinearLayout) convertView
					.findViewById(R.id.layout_cb);

			mHolder.eisChildLayout = (RelativeLayout) convertView
					.findViewById(R.id.layout_eis_child);
			mHolder.mUserName = (TextView) convertView
					.findViewById(R.id.tv_user_name);
			mHolder.mUserEmail = (TextView) convertView
					.findViewById(R.id.tv_user_email);
			mHolder.mUsedMailchat = (TextView) convertView
					.findViewById(R.id.tv_used_mailchat);
			mHolder.imgHead = (RoundImageView) convertView
					.findViewById(R.id.contact_img_head);
			mHolder.childCheckBox = (CheckBox) convertView
					.findViewById(R.id.checkbox_child);
			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		if (treeBeen.isLeaf()) {
			mHolder.eisGroupLayout.setVisibility(View.GONE);
			mHolder.eisChildLayout.setVisibility(View.VISIBLE);
			String userName = treeBeen.getName().toString();
			String userEmail = treeBeen.getEmail().toString();
			String userNameChange = treeBeen.getrNickName();
			String userPosition = treeBeen.getPosition();
			String userPositionChange = treeBeen.getrPosition();
			if (!StringUtil.isEmpty(userNameChange)) {
				userName = userNameChange;
			}
			if (!StringUtil.isEmpty(userPositionChange)) {
				userPosition = userPositionChange;
			}
			if (!StringUtil.isEmpty(userPosition)) {
				mHolder.mUserName.setText(userName + "(" + userPosition + ")");
			} else {
				mHolder.mUserName.setText(userName);
			}
			mHolder.mUserEmail.setText(userEmail);
			if (treeBeen.isLeader()) {
				mHolder.imgLeader.setVisibility(View.VISIBLE);
			} else {
				mHolder.imgLeader.setVisibility(View.GONE);
			}
			// 设置默认头像
			String userHeadUrl = treeBeen.getImgHeadUrl();
			String userHeadUrlChange=treeBeen.getrImgHeadHash();
			if (!StringUtil.isEmpty(userHeadUrlChange)) {
				userHeadUrl=userHeadUrlChange;
			}
			if (!TextUtils.isEmpty(userHeadUrl) && !userHeadUrl.equals("null")) {
				String imgUrl = userHeadUrl;
				if (!userHeadUrl.startsWith("http")) {
					userHeadUrl = GlobalConstants.HOST_IMG + userHeadUrl;
				}
				if (!userHeadUrl.endsWith("_s")) {
					imgUrl = userHeadUrl + GlobalConstants.USER_SMALL_HEAD_END;
				}
				ImageLoader.getInstance().displayImage(imgUrl, mHolder.imgHead,
						options);
			} else {
				if (!StringUtil.isEmpty(userName)) {
					mHolder.imgHead.setImageBitmap(ImageUtils
							.getUserFirstTextBitmap(mContext, userName));
				} else {
					mHolder.imgHead.setImageBitmap(ImageUtils
							.getUserFirstTextBitmap(mContext, userEmail));
				}
			}
			// 是否使用邮洽
			if (treeBeen.isUsedMailchat()) {
				mHolder.mUsedMailchat.setVisibility(View.VISIBLE);
			} else {
				mHolder.mUsedMailchat.setVisibility(View.GONE);
			}
			// // 复选框多选设置start
			mHolder.childCheckBox.setChecked(selectedContacts
					.contains(treeBeen));
			if (searchContactsEditView != null) {
				if (searchContactsEditView.isDuplicateAddress(treeBeen
						.getEmail())) {
					selectedContacts.add(treeBeen);
					mHolder.childCheckBox.setChecked(selectedContacts
							.contains(treeBeen));
				} else {
					int i = 0;
					if (mMembers != null && mMembers.size() > 0
							&& !isCanCancleChecked) {
						for (CGroupMember member : mMembers) {
							if (member.getEmail().equals(treeBeen.getEmail())) {
								mHolder.childCheckBox.setChecked(true);
								i = 1;
								break;
							}
						}
					}
					if (i == 0) {
						if (selectedContacts.contains(treeBeen)) {
							selectedContacts.remove(treeBeen);
						}
						mHolder.childCheckBox.setChecked(selectedContacts
								.contains(treeBeen));
					}
				}
			}

			// ###########设置多选 end#############
		} else {
			mHolder.eisGroupLayout.setVisibility(View.VISIBLE);
			mHolder.eisChildLayout.setVisibility(View.GONE);
			mHolder.mTitle.setText(treeBeen.getName().toString());
			// if (treeBeen.getpId().equals("0")) {
			mHolder.mChildCount.setText(treeBeen.getTotalCount() + "");
			// } else {
			// mHolder.mChildCount
			// .setText(treeBeen.getChildList().size() + "");
			// }
			if (isShowCheckbox) {
				if (selectedGroupStr!=null) {
					mHolder.groupCheckbox.setChecked(selectedGroupStr
							.contains(treeBeen.getName().toString()));	
						// 部门选中后改变颜色
						 mHolder.mTitle.setTextColor(mContext.getResources().getColor(
						 (mHolder.groupCheckbox.isChecked()) ? nickNameColor[1]: nickNameColor[0]));
				}
			}
		}
		if (treeBeen.getIcon() == -1) {
			mHolder.mImgArrow.setVisibility(View.INVISIBLE);
		} else {
			mHolder.mImgArrow.setVisibility(View.VISIBLE);
			mHolder.mImgArrow.setImageResource(treeBeen.getIcon());
		}
		// ###########设置多选 start#############
		if (!isShowCheckbox) {
			mHolder.childCheckBox.setVisibility(View.GONE);
			mHolder.groupCheckbox.setVisibility(View.GONE);
		} else {
			mHolder.childCheckBox.setVisibility(View.VISIBLE);
			//没有子部门的话就支持多选
			if (treeBeen.getChildDepCount()==0&&treeBeen.getChildList().size()!=0) {
				 mHolder.groupCheckbox.setVisibility(View.VISIBLE);
					//该部门下如果有子部门不支持多选
					mHolder.mLayoutCheckbox
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									MobclickAgent.onEvent(mContext, "choose_parent_contacts");
										onTreeClickListener
												.onCheckedGroup(v,
														mTreeBeens.get(position),
														position);
										notifyDataSetChanged();
								}
							});
			}else {
				 mHolder.groupCheckbox.setVisibility(View.GONE);
			}
		}

		// 选中后改变颜色
		mHolder.mUserName.setTextColor(mContext.getResources().getColor(
				(mHolder.childCheckBox.isChecked()) ? nickNameColor[1]
						: nickNameColor[0]));
		mHolder.mUserEmail.setTextColor(mContext.getResources().getColor(
				(mHolder.childCheckBox.isChecked()) ? emailColor[1]
						: emailColor[0]));
		return convertView;
	}

	public void setAllTreeBeens(List<ContactAttribute> datas)
			throws IllegalArgumentException, IllegalAccessException {
		mAllTreeBeens.clear();
		mAllTreeBeens.addAll(datas);
		// 过滤出可见的TreeBeen
		mTreeBeens = TreeHelper.filterVisibleTreeBeen(mAllTreeBeens);
		notifyDataSetChanged();
	}

	public List<ContactAttribute> getmAllTreeBeens() {
		return mAllTreeBeens;
	}

	class ViewHolder {
		RelativeLayout eisGroupLayout, eisChildLayout;
		TextView mTitle, mChildCount, mUserName, mUserEmail, mUsedMailchat;
		ImageView mImgArrow, imgLeader;
		RoundImageView imgHead;
		LinearLayout mLayoutCheckbox;
		CheckBox groupCheckbox, childCheckBox;
	}

	public interface OnTreeClickListener {
		void onItemClick(AdapterView<?> parent, View view,
				ContactAttribute treeBeen, int position);

		void onCheckedGroup(View v,
				ContactAttribute contactAttribute, int position);

		void onGroupClick(AdapterView<?> parent, View view,
				ContactAttribute treeBeen);
	}

	public void setOnTreeClickListener(OnTreeClickListener onTreeClickListener) {
		this.onTreeClickListener = onTreeClickListener;
	}

	public ChoseAddressView getSearchContactsEditView() {
		return searchContactsEditView;
	}

	public void setSearchContactsEditView(
			ChoseAddressView searchContactsEditView, List<CGroupMember> mMembers) {
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

	public boolean isCanCancleChecked() {
		return isCanCancleChecked;
	}

	public void setCanCancleChecked(boolean isCanCancleChecked) {
		this.isCanCancleChecked = isCanCancleChecked;
	}

	public Set<String> getSelectedGroupStr() {
		return selectedGroupStr;
	}

	public void setSelectedGroupStr(Set<String> selectedGroupStr) {
		this.selectedGroupStr = selectedGroupStr;
	}
}
