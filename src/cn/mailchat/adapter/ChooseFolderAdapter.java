package cn.mailchat.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.mailchat.Account;
import cn.mailchat.R;
import cn.mailchat.activity.FolderInfoHolder;
import cn.mailchat.beans.ChooseFolderBean;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.contacts.beans.ContactAttribute;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class ChooseFolderAdapter extends BaseAdapter {
	private Context context;
    private LayoutInflater mInflater;
	private Set<ChooseFolderBean> mSelectedFolders;
	protected List<ChooseFolderBean> mFolderList = new ArrayList<ChooseFolderBean>();    
	private boolean isShowCheckbox;
	private Account mAccount;
	public ChooseFolderAdapter(Context context,Account account,boolean isShowCheckbox) {
		super();
		this.context = context;
		this.isShowCheckbox=isShowCheckbox;
		this.mAccount=account;
		getLayoutInflater(context);
		mSelectedFolders = new HashSet<ChooseFolderBean>();
	}

	protected LayoutInflater getLayoutInflater(Context context) {
        if (mInflater == null) {
            mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        return mInflater;
    }

	@Override
	public int getCount() {
		return mFolderList.size();
	}

	@Override
	public Object getItem(int position) {
		return mFolderList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null || convertView.getTag() == null) {
            convertView =mInflater.inflate(R.layout.item_folder_shoose, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
    	if (!isShowCheckbox) {
			if (holder.cbChooseFolder.getVisibility() == View.VISIBLE)
				holder.cbChooseFolder.setVisibility(View.GONE);
		} else {
			if (holder.cbChooseFolder.getVisibility() == View.GONE)
				holder.cbChooseFolder.setVisibility(View.VISIBLE);
		}
        ChooseFolderBean folder = (ChooseFolderBean) mFolderList.get(position);
    	String newFolderName = FolderInfoHolder
				.getDisplayName(context,
						mAccount, folder.getFolderName());
        holder.tvFolderName.setText(newFolderName);
        
        
        
    	// 复选框多选设置start
//		holder.cbChooseFolder.setChecked(folder.isCheck());
//				mSelectedFolders.add(folder);
				holder.cbChooseFolder.setChecked(mSelectedFolders.contains(folder));

		// 复选框多选end
        return convertView;

	}
    static class ViewHolder {
        TextView tvFolderName;
        CheckBox cbChooseFolder;
        public ViewHolder(View view) {
        	tvFolderName = (TextView) view.findViewById(R.id.tv_folder_name);
            cbChooseFolder= (CheckBox) view.findViewById(R.id.cb_choose_folder);
        }
    }
	public Set<ChooseFolderBean> getmSelectedFolders() {
		return mSelectedFolders;
	}

	public List<ChooseFolderBean> getmFolderList() {
		return mFolderList;
	}


	public void setmSelectedFolders(Set<ChooseFolderBean> mSelectedFolders) {
		this.mSelectedFolders = mSelectedFolders;
	}
	public void setFolderLists(List<ChooseFolderBean> folderList) {
		clear();
		mFolderList.addAll(folderList);
		notifyDataSetChanged();
	}

	public void clear() {
		mFolderList.clear();
		notifyDataSetChanged();
	}


}
