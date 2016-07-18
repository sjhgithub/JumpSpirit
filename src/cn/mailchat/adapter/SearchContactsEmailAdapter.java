package cn.mailchat.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.mailchat.R;
import cn.mailchat.utils.GlobalTools;

public class SearchContactsEmailAdapter extends BaseAdapter {

	Pattern mPattern;
	private Context context;
	private ArrayList<String> mOriginalValues;// 所有的Item
	private List<String> mObjects = new ArrayList<String>();// 过滤后的item
	private final Object mLock = new Object();
	// 支持的邮箱后缀
	private List<String> normalDomains;

	private int lineTag = -1;// 用来标记以前登陆过的账户和通用邮箱地址之间的横线显示位置
	public SearchContactsEmailAdapter(Context context,
			ArrayList<String> mOriginalValues) {
		this.context = context;
		this.mOriginalValues = mOriginalValues;
//		loadDomains();
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

	public void performFiltering(String s) {

		ArrayList<String> newValues = new ArrayList<String>();

//		if (s == null || s.length() == 0) {
//			synchronized (mLock) {
//				newValues = new ArrayList<String>();
//			}
//		} else {
//			String prefixString = s.toLowerCase();
//			if (normalDomains != null) {
//				if (prefixString.contains("@")) {
//					// 输入@后的匹配内容
//					if (occurTimes(prefixString, "@") == 1) {
//						// 仅有一个@
//						final int count = mOriginalValues.size();
//						for (int i = 0; i < count; i++) {
//							final String value = mOriginalValues.get(i);
//							final String valueText = value.toLowerCase();
//							if (valueText.startsWith(prefixString)) {
//								newValues.add(value);
//							}
//						}
//						lineTag = newValues.size();
//
//						String prefixString1 = prefixString.substring(0,
//								prefixString.indexOf("@"));// 截取@之前的字符
//						String prefixString2 = prefixString.substring(
//								prefixString.indexOf("@") + 1,
//								prefixString.length());// 截取@之后的字符
//						if (prefixString2 != null && prefixString2.length() > 0) {
//							for (String a : normalDomains) {
//								if (a.startsWith(prefixString2)) {
//									newValues.add(prefixString1 + "@" + a);
//								}
//							}
//							if (newValues.size() == 0) {
//								// 如果没有匹配的邮箱后缀，显示用户输入的信息
//								newValues.add(prefixString);
//							}
//						} else {
//							for (String a : normalDomains) {
//								newValues.add(prefixString1 + "@" + a);
//							}
//						}
//
//					} else {
//						// 多个@符，无匹配
//						synchronized (mLock) {
//							newValues = new ArrayList<String>();
//						}
//					}
//
//				} else {
//					// 尚未输入@时的匹配内容
//					final int count = mOriginalValues.size();
//					for (int i = 0; i < count; i++) {
//						final String value = mOriginalValues.get(i);
//						final String valueText = value.toLowerCase();
//
//						if (valueText.startsWith(prefixString)) {
//							newValues.add(value);
//						}
//
//					}
//					lineTag = newValues.size();
//					for (String a : normalDomains) {
//						newValues.add(prefixString + "@" + a);
//					}
//				}
//			}
//
//			// 滤重，原因是过滤掉 XXX@35.cn重复的可能情况
//			for (int m = 0; m < lineTag; m++) {
//				// 注意此处的“a = lineTag”，需要确保newValues的size大于lineTag
//				for (int a = lineTag; a < newValues.size(); a++) {
//					if (newValues.get(a).equals(newValues.get(m))) {
//						newValues.remove(a);
//						// Debug.v(TAG, "remove id=" + a);
//					}
//				}
//			}
//
//		}
		newValues.add("");
		publishResults(newValues);

	}

	protected void publishResults(ArrayList<String> newValues) {
		mObjects = newValues;
		if (newValues.size() > 0) {
			notifyDataSetChanged();
		} else {
			notifyDataSetInvalidated();
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
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			convertView = inflater.inflate(R.layout.view_no_contact,
			null);
//			convertView = inflater.inflate(R.layout.item_search_contacts_email,
//					null);
//			holder.tv = (TextView) convertView
//					.findViewById(R.id.txtView_search_email);
			holder.noDataContactsLayout = (LinearLayout) convertView
					.findViewById(R.id.layoutView_no_data_search_contacts);
			convertView.setTag(holder);
//			convertView.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (position == 0) {
			if (holder.noDataContactsLayout.getVisibility() == View.GONE) {
				holder.noDataContactsLayout.setVisibility(View.VISIBLE);
			}
		} else {
			if (holder.noDataContactsLayout.getVisibility() == View.VISIBLE) {
				holder.noDataContactsLayout.setVisibility(View.GONE);
			}
		}
//		try {
//			mPattern = Pattern.compile(mObjects.get(position).substring(0,
//					mObjects.get(position).indexOf("@")));
//			highLightKeyWord(holder.tv, mObjects.get(position));
//		} catch (Exception e) {
//
//		}

		return convertView;
	}

	class ViewHolder {
//		TextView tv;
		LinearLayout noDataContactsLayout;
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

	public List<String> getmObjects() {
		return mObjects;
	}

	public void setmObjects(List<String> mObjects) {
		this.mObjects = mObjects;
	}

	private void highLightKeyWord(TextView textView, String content) {
		SpannableString span = new SpannableString(content);
		Matcher matcher = mPattern.matcher(content);
		while (matcher.find()) {
			span.setSpan(new ForegroundColorSpan(context.getResources()
					.getColor(R.color.bg_title_right_txt)), matcher.start(),
					matcher.end(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		}
		textView.setText(span);
	}
}
