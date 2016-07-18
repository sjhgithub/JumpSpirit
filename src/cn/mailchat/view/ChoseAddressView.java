package cn.mailchat.view;

import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.mail.Address;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.view.hlistview.HListView;

/**
 * 邮件撰写页的三个地址栏
 * 
 * @Description:
 * @author:xulei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:Jun 3, 2013
 */
public class ChoseAddressView extends LinearLayout implements OnClickListener {

	// private static final String TAG = "ComposeAddrView";
	private TextView nameLabel;
	private AddressViewControl addrsViewControl;
	private ImageView addAddrsView;
	private ImageView moreActionView;
	private HListView contactsGridView;
	private LinearLayout contactLayout;
	private AddressViewCallBack mCallBack;
	private Boolean activeFailure = false;// active 方法失效（用于单个地址栏）
	private Context mContext;
	private PopupWindow categoryPopupWindow;
	private CommonContactAdapter mAdapter;

	/**
	 * 地址栏类型
	 * 
	 * @Description:
	 * @author:xulei
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:Jun 4, 2013
	 */
	public enum AddressType {
		TO, // 发件人
		CC, // 抄送
		BCC// 密送
	}

	AddressType mType;
	private boolean isActive;

	// private List<ContactAttribute> commonContacts;

	public ChoseAddressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
	}

	/**
	 * 初始化
	 * 
	 * @Description:
	 * @param type
	 *            类型
	 * @param resId
	 *            地址栏名称的resource id
	 * @param callBack
	 *            回调
	 * @param adapter
	 * @param active
	 *            是否激活
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public <T extends ListAdapter & Filterable> void init(AddressType type,
			int resId, AddressViewCallBack callBack, T adapter, boolean active) {
		mCallBack = callBack;
		mType = type;
		nameLabel = (TextView) findViewById(R.id.address_view_name_tv);
		resetNameLableId(type);
		addrsViewControl = (AddressViewControl) findViewById(R.id.address_view_control);
		addAddrsView = (ImageView) findViewById(R.id.address_view_add_contact_iv);
		moreActionView = (ImageView) findViewById(R.id.address_view_more_action);
		contactsGridView = (HListView) findViewById(R.id.gridview_contact);
		contactLayout = (LinearLayout) findViewById(R.id.linear_layout_contact);
		nameLabel.setText(resId);
		addrsViewControl.init(this);
		setActive(active);
		setAdapter(adapter);
		// 写信页面常用联系人适配器
		mAdapter = new CommonContactAdapter(mContext);
		contactsGridView.setAdapter(mAdapter);
		contactsGridView.setScrollbarFadingEnabled(true);
		addAddrsView.setOnClickListener(this);
		moreActionView.setOnClickListener(this);
		nameLabel.setOnClickListener(this);
		addrsViewControl.setOnClickListener(this);
		activeFailure = false;
	}

	public void setCommonContacts(List<ContactAttribute> commonContacts) {
		if (commonContacts != null) {
			if (!commonContacts.isEmpty()) {
				contactLayout.setVisibility(View.VISIBLE);
				mAdapter.setCommonContacts(commonContacts);
			} else {
				contactLayout.setVisibility(View.GONE);
			}
		} else {
			contactLayout.setVisibility(View.GONE);
		}
	}

	/**
	 * 
	 * @Description:
	 * @param type
	 * @param resId
	 *            输入框名称
	 * @param callBack
	 * @param isActive
	 *            active失效。一直未true
	 * @see:
	 * @since:
	 * @author: Zhonggaoyong
	 * @date:2013-11-25
	 */
	public <T extends ListAdapter & Filterable> void init(AddressType type,
			String editTextNameStr, AddressViewCallBack callBack,
			boolean isActive1) {
		mCallBack = callBack;
		mType = type;
		nameLabel = (TextView) findViewById(R.id.address_view_name_tv);
		if (editTextNameStr.isEmpty()) {
			nameLabel.setVisibility(View.GONE);
		} else {
			nameLabel.setText(editTextNameStr);
		}
		resetNameLableId(type);
		addrsViewControl = (AddressViewControl) findViewById(R.id.address_view_control);

		addrsViewControl.init(this);
		nameLabel.setOnClickListener(this);
		addrsViewControl.setOnClickListener(this);
		this.invalidate();
		activeFailure = true;
		// 一进入发起群聊页面弹出软键盘
		setActive(false);
	}

	private void resetNameLableId(AddressType type) {
		switch (type) {
		case TO:
			nameLabel.setId(R.id.address_lable_to);
			break;
		case CC:
			nameLabel.setId(R.id.address_lable_cc);
			break;
		case BCC:
			nameLabel.setId(R.id.address_lable_bcc);
			break;
		}
	}

	/**
	 * 增加联系人
	 * 
	 * @Description:
	 * @param addresses
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public void addContacts(Address[] addresses) {
		addrsViewControl.addAddress(addresses);
	}

	/**
	 * 增加联系人
	 * 
	 * @Description:
	 * @param addresses
	 *            邮件地址列表
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public void addContacts(List<String> addressList) {
		for (String string : addressList) {
			addContacts(string);
		}
	}

	/**
	 * 增加联系人
	 * 
	 * @Description:
	 * @param addresses
	 *            邮件地址列表
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public void addContacts(String[] addresses) {
		for (int i = 0; i < addresses.length; i++) {
			addContacts(addresses[i]);
		}
	}

	/**
	 * 增加联系人
	 * 
	 * @Description:
	 * @param addressList
	 *            邮件地址字符串，用","分隔
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public void addContacts(String addressList) {
		addrsViewControl.addAddress(addressList);
	}

	/**
	 * 移除联系人
	 */
	public void removeContacts(String email) {

		addrsViewControl.removeAddress(email);
	}

	/**
	 * 添加联系人（本身控件下拉选择时出发）
	 */
	public void addContactsCallBack(Address[] addresses) {

		mCallBack.onAddContactsCallBack(addresses);
	}

	/**
	 * 移除联系人（本身控件点击出发）
	 */
	public void removeContactsCallBack(boolean isListValue) {

		mCallBack.onRemoveContactsCallBack(isListValue);
	}

	/**
	 * 监听地址栏中输入的每一个字符
	 * 
	 * @Description:
	 * @param s
	 * @see:
	 * @since:
	 * @author: Zhonggaoyong
	 * @date:2013-11-18
	 */
	public void onTextChangedCallBack(String s) {
		mCallBack.onTextWatcherCallBack(s);
	}

	/**
	 * 判断联系人是否存在
	 */
	public Boolean isDuplicateAddress(String emaill) {
		return addrsViewControl.isDuplicateAddress(emaill);
	}

	/**
	 * 获取已经输入的邮件地址
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public List<Address> getAddresses() {
		return addrsViewControl.getAddresses();
	}

	/**
	 * 获取正在编辑的邮件地址
	 * 
	 * @Description:
	 * @param addresses
	 *            邮件地址列表
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public Address[] getInputAddress() {
		return addrsViewControl.getInputAddress();
	}

	/**
	 * 设置adapter
	 * 
	 * @Description:
	 * @param adapter
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
		addrsViewControl.setAdapter(adapter);
	}

	/**
	 * 设置地址栏的激活状态（如果已经处于该状态，忽略）
	 * 
	 * @Description:
	 * @param active
	 * @param activeFailure
	 *            默认为false 当为true时 1 为当 addressInputACT 不被选中，不隐藏此控件 2
	 *            失去焦点时框中显示的仍旧是button
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public void setActive(boolean active) {
		if (isActive != active) {

			addrsViewControl.setExpanded(activeFailure, active);

			if (addAddrsView != null && moreActionView != null) {
				if (mType == AddressType.TO) {
					moreActionView.setVisibility(View.VISIBLE);
				} else {
					addAddrsView.setVisibility(active ? View.VISIBLE
							: View.GONE);
				}
			}

			isActive = active;
		}
		mCallBack.onActiveChanged(this, active);
	}

	/**
	 * 地址栏控件被点击时回调
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: Zhonggaoyong
	 * @date:2013-11-18
	 */
	public void setOnViewClickActivity() {
		mCallBack.onViewClicked(this);
	}

	/**
	 * 忽略当前的状态，强制设置地址栏的激活状态
	 * 
	 * @Description:
	 * @param active
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public void forceActive(boolean active) {
		isActive = !active;
		setActive(active);
	}

	/**
	 * 获取该地址栏的类型
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public AddressType getType() {
		return mType;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.address_view_name_tv:
		case R.id.address_lable_to:
		case R.id.address_lable_cc:
		case R.id.address_lable_bcc:
			mCallBack.onNameClicked(this);
			break;
		case R.id.address_view_control:
			setActive(true);
			break;
		case R.id.address_view_add_contact_iv:
			mCallBack.onAddClicked(this);
			break;
		case R.id.address_view_more_action:
			showPopupWindow(mContext);
			break;
		}
	}

	/**
	 * 获取名称的TextView
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public TextView getNameView() {
		return nameLabel;
	}

	/**
	 * 获取AddressViewControl AutoCompleteTextView
	 */

	public AutoCompleteTextView getAutoCompleteTextView() {
		return addrsViewControl.getAutoCompleteTextView();
	}

	/**
	 * 获取AddressViewControl AutoCompleteTextView
	 */

	public AddressViewControl getAddressViewControl() {
		return addrsViewControl;
	}

	/**
	 * 展开抄送、密送、联系人选择窗口
	 * 
	 * @Description:
	 * @param context
	 * @see:
	 * @since:
	 * @author: sunzhongquan
	 * @date:2014-5-18
	 */
	private void showPopupWindow(Context context) {
		final String[] categoryItem = new String[] {
				context.getString(R.string.message_compose_cc_hint),
				context.getString(R.string.message_compose_bcc_hint),
				context.getString(R.string.message_compose_contact) };
		LinearLayout actionDialogLayout = (LinearLayout) LayoutInflater.from(
				context).inflate(R.layout.message_compose_more_action_window,
				null);
		ListView categoryList = (ListView) actionDialogLayout
				.findViewById(R.id.lv_more_action);
		categoryList.setAdapter(new ArrayAdapter<String>(context,
				R.layout.message_compose_more_action_item, R.id.tv_text,
				categoryItem));
		categoryPopupWindow = new PopupWindow(context);
		categoryPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
		float density = dm.density;
		wmParams.height = (int) (132 * density);
		wmParams.width = (int) (90 * density);
		categoryPopupWindow.setHeight(wmParams.height);
		categoryPopupWindow.setWidth(wmParams.width);
		categoryPopupWindow.setFocusable(true);
		categoryPopupWindow.setContentView(actionDialogLayout);
		int yOffet = (int) (105 * density);
		categoryPopupWindow.showAtLocation(moreActionView.getRootView(),
				Gravity.RIGHT | Gravity.TOP, 20, yOffet);
		categoryList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				mCallBack.onSelectedAction(arg2);
				categoryPopupWindow.dismiss();
				categoryPopupWindow = null;
			}
		});
	}

	/**
	 * 常用联系人适配器
	 * 
	 * @Description:
	 * @author:sunzhongquan
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2014-5-18
	 */
	private class CommonContactAdapter extends BaseAdapter {

		private LayoutInflater mInflater = null;
		private List<ContactAttribute> commonContacts;
		private DisplayImageOptions options;

		public CommonContactAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
			initImageLoader();
			commonContacts = new ArrayList<ContactAttribute>();
		}

		private void initImageLoader() {
			options = MailChat.getInstance().initImageLoaderOptions();
		}

		public void setCommonContacts(List<ContactAttribute> commonContacts) {
			this.commonContacts = commonContacts;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return commonContacts.size();
		}

		@Override
		public Object getItem(int position) {
			return commonContacts.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.message_compose_contact_item, null);
				holder = new ViewHolder();
				holder.nameTextView = (TextView) convertView
						.findViewById(R.id.tv_name);
				holder.line = convertView.findViewById(R.id.view_line);
//				holder.imgHead = (RoundImageView) convertView
//						.findViewById(R.id.contact_img_head);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final ContactAttribute attribute = commonContacts.get(position);
//			if (attribute.isEisContact()) {
//				userName = attribute.getEisName();
//			}else {
			String	userName = attribute.getNickName();
//			}
			String userNameChange=attribute.getrNickName();
			if (!StringUtil.isEmpty(userNameChange)) {
				userName=userNameChange;
			}
			holder.nameTextView.setText(userName);
			if (position == commonContacts.size()-1) {
				holder.line.setVisibility(View.GONE);
			}else{
				holder.line.setVisibility(View.VISIBLE);
			}
			//常用联系人点击事件
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Address[] addresses = new Address[1];
					String	userName = attribute.getNickName();
					String userNameChange=attribute.getrNickName();
					if (!StringUtil.isEmpty(userNameChange)) {
						userName=userNameChange;
					}
					Address address = new Address(attribute.getEmail(),userName);
					addresses[0] = address;
					addContacts(addresses);
				}
			});
			// 设置默认头像
//			String userHeadUrl = attribute.getImgHeadPath();
//			if (!TextUtils.isEmpty(userHeadUrl) && !userHeadUrl.equals("null")) {
//				String imgUrl = userHeadUrl;
//				if (!userHeadUrl.startsWith("http")) {
//					userHeadUrl = GlobalConstants.HOST_IMG + userHeadUrl;
//				}
//				if (!userHeadUrl.endsWith("_s")) {
//					imgUrl = userHeadUrl + GlobalConstants.USER_SMALL_HEAD_END;
//				}
//				ImageLoader.getInstance().displayImage(imgUrl, holder.imgHead,
//						options);
//			} else {
//				if (!StringUtil.isEmpty(userName)) {
//					holder.imgHead.setImageBitmap(ImageUtils
//							.getUserFirstTextBitmap(mContext, userName));
//				} else {
//					holder.imgHead.setImageBitmap(ImageUtils
//							.getUserFirstTextBitmap(mContext, userEmail));
//				}
//			}
			return convertView;
		}

		class ViewHolder {
			TextView nameTextView;
			View line;
//			RoundImageView imgHead;
		}
	}
}
