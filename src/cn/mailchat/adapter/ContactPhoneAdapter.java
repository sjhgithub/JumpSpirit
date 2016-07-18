package cn.mailchat.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.R;
import cn.mailchat.activity.AddOrEditContactActivity;
import cn.mailchat.utils.StringUtil;

/**
 * 
 * @copyright © 35.com
 * @file name ：ContactPhoneAdapter.java
 * @author ：zhangjx
 * @create Data ：2015-9-14下午4:21:12
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2015-9-14下午4:21:12
 * @Modified by：zhangjx
 * @Description :电话
 */
public class ContactPhoneAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private ViewHolders holder;
	private Context mContext;
	private DeletePhoneAdapterListener deletePhoneAdapterListener;
	// 用于判断按钮是否点击了减号删除
	@SuppressLint("UseSparseArrays")
	private Map<Integer, Integer> delMap = new HashMap<Integer, Integer>();
	private List<String> dataList;
	private boolean isJustShowPhone = false;
	public ContactPhoneAdapter(Context context) {
		this.inflater = LayoutInflater.from(context);
		this.mContext = context;
		dataList = new ArrayList<String>();
	}

	public List<String> getData() {
		return dataList;
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int position) {
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_add_contact_phone,
					parent, false);
			holder = new ViewHolders();
			holder.imgDelete = (ImageView) convertView
					.findViewById(R.id.img_delete_contact);
			holder.delPhoneReal = (TextView) convertView
					.findViewById(R.id.tv_del);
			holder.editPhone = (EditText) convertView
					.findViewById(R.id.edit_phone);
			holder.tvPhone = (TextView) convertView.findViewById(R.id.tv_phone);
			holder.tvPhoneTitle = (TextView) convertView
					.findViewById(R.id.tv_phone_title);
			holder.layoutAddPhones = (LinearLayout) convertView
					.findViewById(R.id.layout_add_phones);
			holder.layout_just_show_phone = (LinearLayout) convertView
					.findViewById(R.id.layout_just_show_phone);
//			if (!isJustShowPhone) {
//				holder.watcher = new EtTextChanged(position);
//				// holder.focusChange = new ETFocusChange(position,
//				// holder.editPhone);
//				holder.editPhone.addTextChangedListener(holder.watcher);
//				// holder.editPhone.setOnFocusChangeListener(holder.focusChange);
//			}
			convertView.setTag(holder);
		} else {
			holder = (ViewHolders) convertView.getTag();
		}
		String phone = dataList.get(position);
		if (!isJustShowPhone) {
			if (holder.watcher==null) {
				holder.watcher = new EtTextChanged(position);
			}
			holder.watcher.setPosition(position);
			holder.editPhone.addTextChangedListener(holder.watcher);
			holder.editPhone.setText(phone);
			// holder.focusChange.setPosition(position);
			holder.layoutAddPhones.setVisibility(View.VISIBLE);
			holder.layout_just_show_phone.setVisibility(View.GONE);
			holder.editPhone.setEnabled(true);
			holder.delPhoneReal.setOnClickListener(new deletePhoneListener(
					dataList, position, phone));
			holder.imgDelete.setOnClickListener(new deletePhoneListener(
					dataList, position, phone));
			holder.editPhone.requestFocus();
			if (delMap.containsKey(position)) {
				holder.imgDelete.setVisibility(View.GONE);
				holder.delPhoneReal.setVisibility(View.VISIBLE);
			} else {
				holder.imgDelete.setVisibility(View.VISIBLE);
				holder.delPhoneReal.setVisibility(View.GONE);
			}
		} else {
			holder.layoutAddPhones.setVisibility(View.GONE);
			holder.layout_just_show_phone.setVisibility(View.VISIBLE);
			holder.editPhone.setEnabled(false);
			holder.tvPhoneTitle.setText(mContext
					.getString(R.string.contact_phone) + (position==0 ?"  ":position));
			holder.tvPhone.setText(phone);
		}
		return convertView;
	}

	// 验证手机号是否输入合法
	// class ETFocusChange implements OnFocusChangeListener {
	// private int position;
	// private EditText etEditText;
	//
	// public ETFocusChange(int position, EditText etEditText) {
	// this.position = position;
	// this.etEditText = etEditText;
	// }
	//
	// public void setPosition(int position) {
	// this.position = position;
	// }
	//
	// @Override
	// public void onFocusChange(View v, boolean hasFocus) {
	// 校验没个item 号码是否正确
	// if (!hasFocus) {
	// if
	// (!StringUtil.isValidPhoneNo(etEditText.getText().toString().trim()))
	// {
	// Toast.makeText(mContext,
	// mContext.getString(R.string.phone_err),
	// Toast.LENGTH_SHORT).show();
	// }
	// }
	// }
	// }

	class EtTextChanged implements TextWatcher {
		private int position;

		public EtTextChanged(int position) {
			this.position = position;
		}

		public void setPosition(int position) {
			this.position = position;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
//			if (s.toString().trim().equals("")) {
//				return;
//			}
			String phone = dataList.get(position);
			if (!phone.equals(s.toString().trim())) {
				phone = s.toString().trim();
				dataList.set(position, phone);
			}
		}
	}

	class deletePhoneListener implements OnClickListener {
		private int position;
		private String realPhone;
		private List<String> listData;

		deletePhoneListener(List<String> listData, int pos, String realPhone) {
			this.position = pos;
			this.realPhone = realPhone;
			this.listData = listData;
		}

		@Override
		public void onClick(View v) {
			int vid = v.getId();
			switch (vid) {
			case R.id.tv_del:
				getDeletePhoneAdapterListener().deletePhone(listData, position,
						realPhone);
				break;
			case R.id.img_delete_contact:
				delMap.put(position, position);
				notifyDataSetChanged();
				break;
			default:
				break;
			}
		}
	}

	public final class ViewHolders {
		public ImageView imgDelete;
		public EditText editPhone;
		public TextView delPhoneReal;
		TextView tvPhoneTitle, tvPhone;
		LinearLayout layoutAddPhones, layout_just_show_phone;
		EtTextChanged watcher;
		// ETFocusChange focusChange;
	}

	public interface DeletePhoneAdapterListener {
		public void deletePhone(final List<String> listData,
				final int position, final String deletePhone);

		void setListViewGone();
	}

	public DeletePhoneAdapterListener getDeletePhoneAdapterListener() {
		return deletePhoneAdapterListener;
	}

	public void setDeletePhoneAdapterListener(
			DeletePhoneAdapterListener deletePhoneAdapterListener) {
		this.deletePhoneAdapterListener = deletePhoneAdapterListener;
	}

	public Map<Integer, Integer> getDelMap() {
		return delMap;
	}

	public void setDelMap(Map<Integer, Integer> delMap) {
		this.delMap = delMap;
	}

	public void addDataList(List<String> list) {
		dataList.clear();
		dataList.addAll(list);
		notifyDataSetChanged();
	}

	@SuppressLint("UseSparseArrays")
	public void add(String str) {
		dataList.add(str.toString().trim());
		notifyDataSetChanged();
	}

	public void remove(int pos) {
		this.dataList.remove(dataList.get(pos));
		if (dataList.isEmpty()) {
			getDeletePhoneAdapterListener().setListViewGone();
		}
		notifyDataSetChanged();
	}

	public void setJustShowPhone(boolean isJustShowPhone) {
		this.isJustShowPhone = isJustShowPhone;

	}
}