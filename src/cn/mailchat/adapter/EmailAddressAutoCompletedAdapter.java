package cn.mailchat.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.mail.Address;
import cn.mailchat.utils.ContactComparator;
import cn.mailchat.utils.GlobalTools;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.view.RoundImageView;

/**
 * 
 * @copyright © 35.com
 * @file name ：EmailAddressAutoCompletedAdapter.java
 * @author ：zhangjx
 * @create Data ：2014-11-13下午1:45:18
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-11-13下午1:45:18
 * @Modified by：zhangjx
 * @Description : * 邮件撰写界面添加联系人栏适配器。
 *              <p>
 *              检索规则
 *              <li>检索开始先从常用联系人中搜索、其优先级最高
 *              <li>常用联系人检索完毕，然后从企业联系人中检索
 */
public class EmailAddressAutoCompletedAdapter extends BaseAdapter implements
		Filterable {

	private Context mContext;
	private LayoutInflater mInflater;
	private EmailAddressFilter mFilter;
	private final Object mLock = new Object();
	private List<Address> adapterData;
	private Pattern mPattern;
	// 支持的邮箱后缀
	private List<String> normalDomains;
	// 所有企业联系人邮件地址
	private List<Address> entAddresss = new ArrayList<Address>();
	private List<Address>[] allAddressLists;
	// 所有用户联系人邮件地址
	private List<Address> userAddress = new ArrayList<Address>();
	private DisplayImageOptions options;

	public EmailAddressAutoCompletedAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		adapterData = new ArrayList<Address>();
		new Thread(new Runnable() {

			@Override
			public void run() {
				loadDomains();
			}
		}).start();
		if (options == null) {
			initImageLoader();
		}
	}

	public List<Address>[] getAllAddressLists() {
		return allAddressLists;
	}

	public void setAllAddressLists(List<ContactAttribute> contacts) {
		this.allAddressLists = getAllContactsAddress(contacts);
		userAddress = allAddressLists[0];
		entAddresss = allAddressLists[1];
	}

	public List<Address>[] getAllContactsAddress(List<ContactAttribute> contacts) {
		List<Address>[] addresses = new ArrayList[2];
		ArrayList<Address> oneaddress = new ArrayList<Address>();
		// 過濾重複
		// if (contacts != null) {
		// for (int i = 0; i < contacts.size() - 1; i++) {
		// for (int j = contacts.size() - 1; j > i; j--) {
		// if (contacts.get(j).getEmail()
		// .equals(contacts.get(i).getEmail())) {
		// contacts.remove(j);
		// }
		// }
		// }
		// }
		for (int i = 0; i < contacts.size(); i++) {
			Address address = new Address("");
			ContactAttribute contactAttribute = contacts.get(i);
			if (contactAttribute.isEisContact()) {
				address.setPersonal(contactAttribute.getName());
				address.setmUserHeadPath(contactAttribute.getImgHeadUrl());
			} else {
				address.setPersonal(contactAttribute.getNickName());
				address.setmUserHeadPath(contactAttribute.getImgHeadPath());
			}
			address.setAddress(contactAttribute.getEmail());
			oneaddress.add(address);
		}

		addresses[0] = oneaddress;
		return addresses;
	}

	public List<Address> getAdapterData() {
		return adapterData;
	}

	public void setAdapterData(List<Address> adapterData) {
		this.adapterData = adapterData;
		notifyDataSetChanged();
	}

	private void initImageLoader() {
		options = MailChat.getInstance().initImageLoaderOptions();
	}

	private void loadDomains() {
		if (normalDomains == null || normalDomains.size() == 0) {
			normalDomains = GlobalTools.getSupportDomains(mContext);
		}
	}

	@Override
	public int getCount() {
		return adapterData.size();
	}

	@Override
	public Object getItem(int position) {
		if (position >= 0 && position < adapterData.size()) {
			return adapterData.get(position);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_recipient_dropdown,
					parent, false);
			viewHolder = new ViewHolder();
			viewHolder.mTextViewName = (TextView) convertView
					.findViewById(R.id.txt_view_personal);
			viewHolder.mTextViewEmail = (TextView) convertView
					.findViewById(R.id.txt_view_value);
			viewHolder.iv_head = (RoundImageView) convertView
					.findViewById(R.id.contact_img_head);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		parent.setScrollbarFadingEnabled(false);
		convertView.setScrollbarFadingEnabled(false);
		try {
			String nickName = adapterData.get(position).getPersonal();
			if (nickName != null) {
				viewHolder.mTextViewName.setText(nickName);
			} else {
				viewHolder.mTextViewName.setVisibility(View.GONE);
			}
			viewHolder.mTextViewEmail.setText(adapterData.get(position)
					.getAddress());
			Address user = adapterData.get(position);
			String userHeadUrl = user.getmUserHeadPath();
			if (!TextUtils.isEmpty(userHeadUrl) && !userHeadUrl.equals("null")) {
				String imgUrl = userHeadUrl;
				if (!userHeadUrl.startsWith("http")) {
					userHeadUrl = GlobalConstants.HOST_IMG + userHeadUrl;
				}
				if (!userHeadUrl.endsWith("_s")) {
					imgUrl = userHeadUrl + GlobalConstants.USER_SMALL_HEAD_END;
				}
				if (!TextUtils.isEmpty(imgUrl)) {
					ImageLoader.getInstance().displayImage(imgUrl,
							viewHolder.iv_head, options);
				} else {
					if (StringUtil.isEmpty(nickName)) {
						nickName = user.getAddress();
					}
					viewHolder.iv_head.setImageBitmap(ImageUtils
							.getUserFirstTextBitmap(mContext, nickName));
				}
			} else {
				if (StringUtil.isEmpty(nickName)) {
					nickName = user.getAddress();
				}
				viewHolder.iv_head.setImageBitmap(ImageUtils
						.getUserFirstTextBitmap(mContext, nickName));
			}
			// mPattern = Pattern.compile(adapterData
			// .get(position)
			// .getEmail()
			// .substring(0,
			// adapterData.get(position).getEmail().indexOf("@")));
			// highLightKeyWord(viewHolder.mTextViewEmail,
			// adapterData.get(position).getEmail());
		} catch (Exception e) {
			// Debug.e("failfast", "failfast_AA", e);
		}
		return convertView;
	}

	private void highLightKeyWord(TextView textView, String content) {
		SpannableString span = new SpannableString(content);
		Matcher matcher = mPattern.matcher(content);
		while (matcher.find()) {
			span.setSpan(new ForegroundColorSpan(mContext.getResources()
					.getColor(R.color.bg_title_right_txt)), matcher.start(),
					matcher.end(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		}
		textView.setText(span);
	}

	@Override
	public Filter getFilter() {
		return mFilter == null ? mFilter = new EmailAddressFilter() : mFilter;
	}

	class EmailAddressFilter extends Filter {

		private FilterResults results;

		@Override
		public CharSequence convertResultToString(Object resultValue) {
			return "";
		}

		@SuppressWarnings("unused")
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			synchronized (EmailAddressAutoCompletedAdapter.class) {
				ArrayList<Address> newValues = new ArrayList<Address>();
				if (constraint == null || constraint.length() == 0) {
					synchronized (mLock) {
						newValues.clear();
					}
				} else {
					String prefixString = ((String) constraint).toLowerCase();

					if (userAddress != null) {
						if (prefixString.startsWith("@")) {
							synchronized (mLock) {
								newValues.clear();
							}
							// 输入@后的匹配内容
						} else {
							final int count = userAddress.size();
							for (int i = 0; i < count; i++) {
								final String value = userAddress.get(i)
										.getAddress();
								final String name = userAddress.get(i).getPersonal();
								final String valueText = value.toLowerCase();
								if (valueText.startsWith(prefixString)||name.contains(prefixString)) {
									newValues.add(userAddress.get(i));
								}
								// else if
								// (valueText.contains(prefixString)) {
								// newValues.add(contact);
								// }
							}
//							本地没有匹配到，输入@后开始匹配后缀
//							if (newValues.isEmpty()) {
//								if (!prefixString.startsWith("@")
//										&& occurTimes(prefixString, "@") == 1) {
//									// 仅有一个@
////									final int count = userAddress.size();
////									for (int i = 0; i < count; i++) {
////										final String value = userAddress.get(i)
////												.getAddress();
////										final String valueText = value.toLowerCase();
////										if (valueText.startsWith(prefixString)) {
////											newValues.add(userAddress.get(i));
////										}
////									}
//									// final int entAddresssCount =
//									// entAddresss.size();
//									// for (int i = 0; i < entAddresssCount; i++) {
//									// final String value = entAddresss.get(i)
//									// .getAddress();
//									// final String valueText = value
//									// .toLowerCase();
//									// if (valueText.startsWith(prefixString)) {
//									// newValues.add(userAddress.get(i));
//									// }
//									// }
//									String prefixString1 = prefixString.substring(0,
//											prefixString.indexOf("@"));// 截取@之前的字符
//									String prefixString2 = prefixString.substring(
//											prefixString.indexOf("@") + 1,
//											prefixString.length());// 截取@之后的字符
//									if (prefixString2 != null
//											&& prefixString2.length() > 0) {
//										for (String a : normalDomains) {
//											if (a.startsWith(prefixString2)) {
//												Address contact = new Address(
//														prefixString1 + "@" + a,
//														prefixString1);
//												newValues.add(contact);
//											}
//										}
//										if (newValues.size() == 0) {
//											// 如果没有匹配的邮箱后缀，显示用户输入的信息
//											Address contact = new Address(
//													prefixString1, prefixString1);
//											newValues.add(contact);
//										}
//									} else {
//										for (String a : normalDomains) {
//											Address contact = new Address(prefixString1
//													+ "@" + a, prefixString1);
//											newValues.add(contact);
//										}
//									}
//
//								} else {
//									// 多个@符，无匹配
//									synchronized (mLock) {
//										newValues.clear();
//									}
//								}
//							}
						}

					}
					
					results = new FilterResults();
					results.values = newValues;
					results.count = newValues.size();
				}
			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			adapterData = (List<Address>) results.values;
			if (adapterData != null && !adapterData.isEmpty()
					&& adapterData.size() > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}

		/**
		 * 判断某个字符个数
		 * 
		 * @param string
		 * @param a
		 * @return
		 */
		public int occurTimes(String string, String a) {
			int pos = -2;
			int n = 0;

			while (pos != -1) {
				if (pos == -2) {
					pos = -1;
				}
				pos = string.indexOf(a, pos + 1);
				if (pos != -1) {
					n++;
				}
			}
			return n;
		}

	}

	static class ViewHolder {
		TextView mTextViewName;
		TextView mTextViewEmail;
		RoundImageView iv_head;
	}

}
