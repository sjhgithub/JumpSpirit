package cn.mailchat.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.mailchat.R;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.utils.ImageUtils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactAttributeAdapter extends BaseAdapter implements Filterable {
	private ArrayFilter mFilter;
	private Context context;
	private List<ContactAttribute> contactAttribute;
	private ArrayList<ContactAttribute> mUnfilteredData;

	public ContactAttributeAdapter(Context context, List<ContactAttribute> contactAttribute) {
		this.context = context;
		this.contactAttribute = contactAttribute;
	}

	@Override
	public int getCount() {
		return contactAttribute == null ? 0 : contactAttribute.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return contactAttribute.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view;
		ViewHolder holder;
		if (convertView == null) {
			view = View.inflate(context, R.layout.contact_attribute_item, null);
			holder = new ViewHolder();
			holder.tv_name = (TextView) view.findViewById(R.id.contact_item_name);
			holder.tv_email = (TextView) view.findViewById(R.id.contact_item_eamil);
			holder.iv_head = (ImageView) view.findViewById(R.id.contact_item_head);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		ContactAttribute contact = contactAttribute.get(position);
		holder.tv_name.setText(contact.getNickName().toString().trim());
		holder.tv_email.setText(contact.getEmail().toString().trim());
//		createTextBitmap(36, 36, head.substring(0, 1), 24)
		holder.iv_head.setImageBitmap(ImageUtils.getUserFirstTextBitmap(context, contact.getNickName().toString().trim()));
		return view;
	}

	@Override
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new ArrayFilter();
		}
		return mFilter;
	}

	static class ViewHolder {
		public TextView tv_name;
		public TextView tv_email;
		public ImageView iv_head;
	}

	private class ArrayFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();

			if (mUnfilteredData == null) {
				mUnfilteredData = new ArrayList<ContactAttribute>(contactAttribute);
			}

			if (prefix == null || prefix.length() == 0) {
				ArrayList<ContactAttribute> list = mUnfilteredData;
				results.values = list;
				results.count = list.size();
			} else {
				String prefixString = prefix.toString().toLowerCase(Locale.US);

				ArrayList<ContactAttribute> unfilteredValues = mUnfilteredData;
				int count = unfilteredValues.size();

				ArrayList<ContactAttribute> newValues = new ArrayList<ContactAttribute>(count);

				for (int i = 0; i < count; i++) {
					ContactAttribute pc = unfilteredValues.get(i);
					if (pc != null) {

						if (pc.getEmail() != null && pc.getEmail().startsWith(prefixString)) {

							newValues.add(pc);
						} else if (pc.getEmail() != null && pc.getEmail().startsWith(prefixString)) {

							newValues.add(pc);
						}
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			contactAttribute = (List<ContactAttribute>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}

	}
}
