package cn.mailchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import cn.mailchat.MailChat;
import cn.mailchat.R;

public class SearchAutoCompleteAdapter extends BaseAdapter implements Filterable {
    
    private String[] mSearchRanges = new String[] {
            MailChat.app.getString(R.string.search_range_sender),
            MailChat.app.getString(R.string.search_range_recipients),
            MailChat.app.getString(R.string.search_range_subject),
            MailChat.app.getString(R.string.search_range_text)
    };

    private AutoCompleteTextView mTextView;
    private SearchFilter mFilter;
    
    public SearchAutoCompleteAdapter(AutoCompleteTextView textView) {
        mTextView = textView;
    }

    @Override
    public int getCount() {
        return mSearchRanges.length;
    }

    @Override
    public Object getItem(int position) {
        return mTextView.getText();
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
            LayoutInflater inflater
                = (LayoutInflater) MailChat.app.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_search_autocomplete, null);
            
            holder.tv = (TextView) convertView.findViewById(R.id.item_search_autocomplete_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv.setText(mSearchRanges[position] + "    " + mTextView.getText());

        return convertView;
    }

    private class ViewHolder {
        TextView tv;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new SearchFilter();
        }
        return mFilter;
    }
    
    private class SearchFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence text) {
            if (text == null || text.length() < 1) {
                return null;
            } else {
                FilterResults results = new FilterResults();
                results.values = mSearchRanges;
                results.count = mSearchRanges.length;
                return results;
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null && results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
        
    }

}
