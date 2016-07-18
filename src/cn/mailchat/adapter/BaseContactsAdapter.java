package cn.mailchat.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.utils.GlobalTools;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.view.RoundImageView;

public abstract class BaseContactsAdapter extends BaseAdapter {

	// 常用联系人
	private List<ContactAttribute> starContacts = new ArrayList<ContactAttribute>();
	// 常用联系人数量
	private int starCount = 0;
	// 所有联系人
	private List<ContactAttribute> normalContacts = new ArrayList<ContactAttribute>();
	// 所有联系人数量
	private int normalCount = 0;
	// adpater填充数量
	protected List<ContactAttribute> allContacts = new ArrayList<ContactAttribute>();

	protected Context mContext;
	// 显示的是否为搜索结果
	protected boolean isShowSearchResult = false;
	// 是否显示头像
	protected boolean isShowImgHead = false;
	// 获取tagtv高度
	private int tagHeight = 0;

	protected int nickNameColor[] = { R.color.chat_up, R.color.bg_title_right_txt };
	protected int emailColor[] = { R.color.chat_down,
			R.color.bg_title_right_txt };
	private DisplayImageOptions options;
	public BaseContactsAdapter(Context context) {
		this.mContext = context;
		if (options==null) {
			initImageLoader();
		}
	}

	/**
	 * 为adapter填充数据
	 * 
	 * @Description:
	 * @param starUsers
	 * @param normalUsers
	 * @see:
	 * @since:
	 * @author: Zhonggaoyong
	 * @date:2013-11-21
	 */
	public void setContacts(List<ContactAttribute> starUsers,
			List<ContactAttribute> normalUsers) {
		if (starUsers == null) {
			starUsers = new ArrayList<ContactAttribute>();
		}
		if (normalUsers == null) {
			normalUsers = new ArrayList<ContactAttribute>();
		}
		this.starContacts = starUsers;
		this.starCount = starUsers.size();
		
		if ( normalContacts == null ){
			normalContacts = new ArrayList<ContactAttribute>();
		}
		normalContacts.clear();
		normalContacts.addAll(normalUsers);
		this.normalCount = normalContacts.size();
		
		
		allContacts.clear();

		allContacts.addAll(this.starContacts);
		allContacts.addAll(this.normalContacts);
		notifyDataSetChanged();
	}

	private void initImageLoader() {
		options =MailChat.getInstance().initImageLoaderOptions();
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return allContacts.size();
	}

	@Override
	public ContactAttribute getItem(int position) {
		return allContacts.get(position);
	}

	private String formteString(int resId, int value) {
		return String.format(mContext.getString(resId), value);
	}

	/**
	 * 获取所有联系人
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: Zhonggaoyong
	 * @date:2013-11-21
	 */
	public List<ContactAttribute> getAllContactsInList() {
		return allContacts;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		return convertView;
	}

	/**
	 * 初始化UI
	 * 
	 * @Description:
	 * @param convertView
	 * @return
	 * @see:
	 * @since:
	 * @author: Zhonggaoyong
	 * @date:2013-11-21
	 */
	protected UserItemHolder initView(View convertView) {
		UserItemHolder holder = null;
		if (convertView == null) {

			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_contacts_tab, null);
			holder = new UserItemHolder();
			holder.convertView = convertView;
			holder.tagTv = (TextView) holder.convertView
					.findViewById(R.id.muliple_select_user_item_tag);
			holder.userCb = (CheckBox) holder.convertView
					.findViewById(R.id.muliple_select_user_item_cb);
			holder.nameTv = (TextView) holder.convertView
					.findViewById(R.id.muliple_select_user_name_tv);
			holder.emailTv = (TextView) holder.convertView
					.findViewById(R.id.muliple_select_user_email_tv);
			holder.week_show = (TextView) holder.convertView
					.findViewById(R.id.messagelist_week_show);
			holder.groupLayout = (LinearLayout) holder.convertView
					.findViewById(R.id.muliple_select_user_group);
			holder.topView = holder.convertView
					.findViewById(R.id.muliple_select_top_view);
			holder.bottomView = holder.convertView
					.findViewById(R.id.muliple_select_bottom_view);
			holder.contactImgHead = (RoundImageView) holder.convertView
					.findViewById(R.id.contact_img_head);
			holder.tvUsedMailchat= (TextView) holder.convertView
					.findViewById(R.id.tv_used_mailchat);
			convertView.setTag(holder);

		} else {
			holder = (UserItemHolder) convertView.getTag();
			holder.convertView = convertView;
		}
		if (tagHeight == 0) {
			holder.tagTv.measure(0, 0);
			tagHeight = holder.tagTv.getMeasuredHeight();
		}
		return holder;
	}
	private  void setMargins (View v, int l, int t, int r, int b) {
	    if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
	        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
	        p.setMargins(l, t, r, b);
	        v.requestLayout();
	    }
	}
	/**
	 * 初始化最基本数据
	 * 
	 * @Description:
	 * @param holder
	 * @param user
	 * @param position
	 * @see:
	 * @since:
	 * @author: Zhonggaoyong
	 * @date:2013-11-21
	 */
	protected void initData(final UserItemHolder holder,
			final ContactAttribute user, int position) {
		holder.tagTv.setVisibility(View.GONE);
		if (starCount > 0 && !isShowSearchResult) {
			if (position == 0) {
				holder.tagTv.setVisibility(View.VISIBLE);
				holder.tagTv.setText(formteString(R.string.star_contacts,
						starCount));
			}
		}
		if (user.isUsedMailchat()) {
			setMargins(holder.tvUsedMailchat, 0, 0,  GlobalTools.dip2px(mContext, 26), 0);
			holder.tvUsedMailchat.setVisibility(View.VISIBLE);
			holder.tvUsedMailchat.setText(mContext.getString(R.string.iUsedMailchat));
		}else {
			holder.tvUsedMailchat.setVisibility(View.GONE);
		}
		// 设置头像
		holder.contactImgHead.setVisibility(View.VISIBLE);
		if (position == starCount && !isShowSearchResult) {
			holder.tagTv.setVisibility(View.VISIBLE);
			holder.tagTv.setText(formteString(R.string.all_contacts,
					normalCount));
		}
		String userName=user.getNickName();
		String userNameChange=user.getrNickName();
		String userPosition=user.getPosition();
		String userPositionChange=user.getrPosition();
		if (!StringUtil.isEmpty(userNameChange)) {
			userName=userNameChange;
		}
		if (!StringUtil.isEmpty(userPositionChange)) {
			userPosition=userPositionChange;
		}
		//一种情况，备注表没有这个emil表示该用户没有被修改过，如果有的话，说明该用户被编辑过，
		//如果是将职位删除的话，原来用户表也有职位值，那么应该不显示职位了
		if (!StringUtil.isEmpty(user.getrEmail())&&
				StringUtil.isEmpty(userPositionChange)&&
				!StringUtil.isEmpty(userPosition)) {
			userPosition=null;
		}
		String userHeadUrl=(!TextUtils.isEmpty(user.getImgHeadPath()))?user.getImgHeadPath():user.getImgHeadUrl();
		if (!TextUtils.isEmpty(userHeadUrl)&& !userHeadUrl.equals("null")) {
			String imgUrl = userHeadUrl;
			if (!userHeadUrl.startsWith("http")) {
				userHeadUrl = GlobalConstants.HOST_IMG + userHeadUrl;
			}
			if (!userHeadUrl.endsWith("_s")) {
				imgUrl = userHeadUrl + GlobalConstants.USER_SMALL_HEAD_END;
			}
			if (!TextUtils.isEmpty(imgUrl)) {
				ImageLoader.getInstance().displayImage(imgUrl,
						holder.contactImgHead, options);
			} else {
				if (StringUtil.isEmpty(userName)) {
					userName=user.getName();
				}
				if (StringUtil.isEmpty(userName)) {
					userName=user.getEmail();
				}
				holder.contactImgHead.setImageBitmap(ImageUtils
						.getUserFirstTextBitmap(mContext, userName));
			}
		} else {
			if (StringUtil.isEmpty(userName)) {
				userName=user.getName();
			}
			if (StringUtil.isEmpty(userName)) {
				userName=user.getEmail();
			}
			holder.contactImgHead.setImageBitmap(ImageUtils
					.getUserFirstTextBitmap(mContext, userName));
		}
		if (StringUtil.isEmpty(userName)) {
			userName=user.getName();
		}
		if (!StringUtil.isEmpty(userPosition)) {
			holder.nameTv.setText(userName+"("+userPosition+")");
		}else {
			holder.nameTv.setText(userName);
		}

		holder.emailTv.setText(user.getEmail());

	}
	class UserItemHolder {

		View convertView;
		TextView tagTv;
		CheckBox userCb;
		TextView nameTv;
		TextView emailTv;
		LinearLayout groupLayout;
		TextView week_show,tvUsedMailchat;
		View topView;
		View bottomView;
		RoundImageView contactImgHead;
	}

	/**
	 * 生成首字母和联系人位置对应关系
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-5-23
	 */
	// 首字母及对应位置
	private Map<Character, Integer> positionMap = new HashMap<Character, Integer>();

	protected Map<Character, Integer> getMapPositionChar() {
		positionMap.clear();
		if (normalContacts!=null&&normalContacts.size()>0) {
			for (int i = 0; i < normalContacts.size(); i++) {
				ContactAttribute ub = normalContacts.get(i);
				if ( ub != null ){
					if (!positionMap.containsKey(ub.getFirstChar())) {
						positionMap.put(ub.getFirstChar(), i + starCount);
					}
				}
			}
		}
		return positionMap;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
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

	public boolean isShowImgHead() {
		return isShowImgHead;
	}

	public void setShowImgHead(boolean isShowImgHead) {
		this.isShowImgHead = isShowImgHead;
	}

	public int getNormalCount() {
		return normalCount;
	}

}
