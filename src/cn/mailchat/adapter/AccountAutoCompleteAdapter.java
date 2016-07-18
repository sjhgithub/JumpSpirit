package cn.mailchat.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import cn.mailchat.R;
import cn.mailchat.activity.setup.AccountSetupBasics;
import cn.mailchat.utils.GlobalTools;

/**
 * 登录时，账户自动匹配的适配器
 * 
 * @Description:
 * @author:xuqq
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-8-19
 */
public class AccountAutoCompleteAdapter extends BaseAdapter implements
		Filterable {

	private Context context;
	private ArrayFilter mFilter;
	private ArrayList<String> mOriginalValues;// 所有的Item
	private List<String> mObjects;// 过滤后的item
	private final Object mLock = new Object();
	// 支持的邮箱后缀
	private List<String> normalDomains;

	private int lineTag = -1;// 用来标记以前登陆过的账户和通用邮箱地址之间的横线显示位置

	private static final String TAG = "AutoCompleteAdapter";

	public AccountAutoCompleteAdapter(Context context,
			ArrayList<String> mOriginalValues,List<String> normalDomains) {
		this.context = context;
		this.mOriginalValues = mOriginalValues;
		if (normalDomains != null) {
			this.normalDomains = normalDomains;
		} else {
			loadDomains();
		}
	}

	@Override
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new ArrayFilter();
		}
		return mFilter;
	}

	/**
	 * 加载邮箱后缀
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2013-8-21
	 */
	private void loadDomains() {
		if (normalDomains == null || normalDomains.size() == 0) {
			normalDomains = GlobalTools.getSupportDomains(context);
		}
	}

	private class ArrayFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence prefix) {

			FilterResults results = new FilterResults();

			if (prefix == null || prefix.length() == 0) {
				synchronized (mLock) {
					ArrayList<String> list = new ArrayList<String>();
					results.values = list;
					results.count = list.size();
					return results;
				}
			} else {
				String prefixString = prefix.toString().toLowerCase();
				ArrayList<String> newValues = new ArrayList<String>();

				if (normalDomains != null) {
					if (prefixString.contains("@")) {
						// 输入@后的匹配内容
						if (occurTimes(prefixString, "@") == 1) {
							// 仅有一个@
							final int count = mOriginalValues.size();
							for (int i = 0; i < count; i++) {
								final String value = mOriginalValues.get(i);
								final String valueText = value.toLowerCase();
								if (valueText.startsWith(prefixString)) {
									newValues.add(value);
								}
							}
							lineTag = newValues.size();

							String prefixString1 = prefixString.substring(0,
									prefixString.indexOf("@"));// 截取@之前的字符
							String prefixString2 = prefixString.substring(
									prefixString.indexOf("@") + 1,
									prefixString.length());// 截取@之后的字符
							if (prefixString2 != null
									&& prefixString2.length() > 0) {
								for (String a : normalDomains) {
									if (a.startsWith(prefixString2)) {
										newValues.add(prefixString1 + "@" + a);
									}
								}
							} else {

								for (String a : normalDomains) {
									newValues.add(prefixString1 + "@" + a);
								}
							}

						} else {
							// 多个@符，无匹配
							synchronized (mLock) {
								ArrayList<String> list = new ArrayList<String>();
								results.values = list;
								results.count = list.size();
								return results;
							}
						}

					} else {
						// 尚未输入@时的匹配内容
						final int count = mOriginalValues.size();
						for (int i = 0; i < count; i++) {
							final String value = mOriginalValues.get(i);
							final String valueText = value.toLowerCase();

							if (valueText.startsWith(prefixString)) {
								newValues.add(value);
							}

						}
						lineTag = newValues.size();
						for (String a : normalDomains) {
							newValues.add(prefixString + "@" + a);
						}
					}
				}

				// 滤重，原因是过滤掉 XXX@35.cn重复的可能情况
				for (int m = 0; m < lineTag; m++) {
					// 注意此处的“a = lineTag”，需要确保newValues的size大于lineTag
					for (int a = lineTag; a < newValues.size(); a++) {
						if (newValues.get(a).equals(newValues.get(m))) {
							newValues.remove(a);
							// Debug.v(TAG, "remove id=" + a);
						}
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			mObjects = (List<String>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}

	}

	@Override
	public int getCount() {
		return mObjects.size();
	}

	@Override
	public Object getItem(int position) {
		return mObjects.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;

		if (convertView == null) {
			holder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.item_account_autocomplete,null);
			holder.tv = (TextView) convertView.findViewById(R.id.autocomplete_text);
			holder.viewDive = (View) convertView.findViewById(R.id.autocomplete_diver);
			convertView.setTag(holder);
			convertView.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		 if (context instanceof AccountSetupBasics) {
		// 根据地址框输入的字数变色
		int charts = AccountSetupBasics.addresscharts;// 地址框输入的字数

		SpannableString ss = new SpannableString(mObjects.get(position));
//		ForegroundColorSpan colorspan = new ForegroundColorSpan(context
//				.getResources().getColor(R.color.bluebg));
//
//		ss.setSpan(colorspan, 0, charts, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		holder.tv.setText(ss);
		 } else {
		 holder.tv.setText(mObjects.get(position));
		 }

		if (position == (lineTag - 1)) {
			holder.viewDive.setVisibility(View.VISIBLE);
		} else {
			holder.viewDive.setVisibility(View.GONE);
		}

		return convertView;
	}

	class ViewHolder {

		TextView tv;
		View viewDive;
	}

	/**
	 * 判断某个字符个数
	 * 
	 * @param string
	 * @param a
	 * @return
	 */
	public static int occurTimes(String string, String a) {
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
