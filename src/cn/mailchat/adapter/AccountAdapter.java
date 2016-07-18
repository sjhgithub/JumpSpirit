package cn.mailchat.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.BaseAccount;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.view.BadgeView;
import cn.mailchat.view.RoundImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class AccountAdapter extends BaseAdapter {

		private List<BaseAccount> data;
		private LayoutInflater inflater;
		private String defaults;
		private ViewHolders holder;
		private DisplayImageOptions options;
		private Context mContext;
		private int dataSize;
		private boolean isHideDeleteBtn;
		private AccountAdapterListener accountAdapterListener;
		public AccountAdapter(Context context,
				String defaultAccount,boolean isHideDeleteBtn) {
			this.inflater = LayoutInflater.from(context);
			this.mContext=context;
			this.defaults = defaultAccount;
			this.isHideDeleteBtn=isHideDeleteBtn;
			if (options==null) {
				initImageLoader();
			}
		}
		public void setAccounts(List<BaseAccount> accounts,String defaultAccount) {
			if (accounts == null) {
				accounts = new ArrayList<BaseAccount>();
			}
			this.defaults = defaultAccount;
			this.data = accounts;
			this.dataSize = accounts.size();
			if (options==null) {
				initImageLoader();
			}
			notifyDataSetChanged();
		}
		private void initImageLoader() {
			options =MailChat.getInstance().initImageLoaderOptions();
		}
		@Override
		public int getCount() {
			return dataSize;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.fragment_mine_listview,
						null);
				holder = new ViewHolders();
				holder.layout = (LinearLayout) convertView
						.findViewById(R.id.mine_list_layout);
				holder.account = (TextView) convertView
						.findViewById(R.id.mine_list_account);
				holder.mine_delete_layout = (LinearLayout) convertView
						.findViewById(R.id.mine_delete_layout);
				holder.accountFocus = (TextView) convertView
						.findViewById(R.id.mine_list_focus);
				holder.accountIcon = (RoundImageView) convertView
						.findViewById(R.id.mine_list_icon);
				holder.mImgMoreIcon= (ImageView) convertView
						.findViewById(R.id.img_more);
				holder.layoutLogo=(LinearLayout) convertView.findViewById(R.id.layout_logo);
				holder.tvTips = (TextView) convertView.findViewById(R.id.tv_tips);
				holder.badgeNewMsg= new BadgeView(mContext, holder.layoutLogo);
				holder.badgeNewMsg.setBackgroundResource(R.drawable.icon_unread_bg);
				holder.badgeNewMsg.setHeight(10);
//				holder.badgeNewMsg.setBadgeBackgroundColor(mContext.getResources().getColor(
//						R.color.red));
				convertView.setTag(holder);// 绑定ViewHolder对
			} else {
				holder = (ViewHolders) convertView.getTag();// 取出ViewHolder对象
			}
			BaseAccount account=data.get(position);
			String dataAccountEmail = account.getEmail();
			String dataAccountName = account.getName();
			holder.account.setText(dataAccountEmail);
			if (dataAccountEmail.equals(defaults)) {
				holder.account.setTextColor(mContext.getResources().getColor(
						R.color.tv_choice_press));
				// holder.accountFocus.setVisibility(View.VISIBLE);
			} else {
				holder.account.setTextColor(mContext.getResources().getColor(
						R.color.black));
				// holder.accountFocus.setVisibility(View.GONE);
			}
			// TODO 设置默认头像
			String userHeadUrl = account.getAccountBigHeadImg();
			if (!TextUtils.isEmpty(userHeadUrl)) {
				String imgUrl=userHeadUrl;
				if (!userHeadUrl.endsWith("_s")) {
					imgUrl=userHeadUrl+GlobalConstants.USER_SMALL_HEAD_END;
				}
				ImageLoader.getInstance().displayImage(imgUrl,
							holder.accountIcon, options);
			} else {
				if (!StringUtil.isEmpty(dataAccountName)) {
					holder.accountIcon.setImageBitmap(ImageUtils
							.getUserFirstTextBitmap(mContext, dataAccountName));
				}else{
				holder.accountIcon.setImageBitmap(ImageUtils
						.getUserFirstTextBitmap(mContext, dataAccountEmail));
				}
			}
			
			if (isHideDeleteBtn) {
				holder.mImgMoreIcon.setVisibility(View.VISIBLE);
				holder.mine_delete_layout.setVisibility(View.GONE);
				if (MailChat.isShowAccountSettingPopo()) {
					holder.tvTips.setVisibility(View.VISIBLE);
				}else{
					holder.tvTips.setVisibility(View.GONE);
				}
			} else {
				holder.mImgMoreIcon.setVisibility(View.GONE);
				holder.mine_delete_layout.setVisibility(View.VISIBLE);
				holder.tvTips.setVisibility(View.GONE);
				// 删除账户
				String accountUuid = data.get(position).getUuid();
				// Account account =
				// Preferences.getPreferences(mContext).getAccount(
				// accountUuid);
				// Preferences.getPreferences(mContext).setDefaultAccount(account);
				BaseAccount realAccount = Preferences.getPreferences(mContext)
						.getAccount(accountUuid);
				holder.mine_delete_layout
						.setOnClickListener(new leaveOrDelAccountListener(data,
								position, realAccount));
			}
			
			if (data.get(position).getIsHaveUnreadMsg()) {
				if (isHideDeleteBtn) {
					 holder.badgeNewMsg.hide();
				}else {
					 holder.badgeNewMsg.show();
				}
			}else {
				 holder.badgeNewMsg.hide();
			}
			return convertView;
		}
		class leaveOrDelAccountListener implements OnClickListener {
			private int position;
			private BaseAccount realAccount;
			private List<BaseAccount> listData;

			leaveOrDelAccountListener(List<BaseAccount> listData, int pos,
					BaseAccount dataAccount) {
				this.position = pos;
				this.realAccount = dataAccount;
				this.listData = listData;
			}

			@Override
			public void onClick(View v) {
				int vid = v.getId();
				if (vid == holder.mine_delete_layout.getId()) {
					getAccountAdapterListener().leaveOrDelAccount(listData, position, realAccount);
				}
			}
		}

		public final class ViewHolders {
			public TextView account;
			public LinearLayout layout, mine_delete_layout,layoutLogo;
			public TextView accountFocus,tvTips;
			public RoundImageView accountIcon;
			public BadgeView badgeNewMsg;
			public ImageView mImgMoreIcon;
		}
		
		
		public interface  AccountAdapterListener{
			public void leaveOrDelAccount(final List<BaseAccount> listData,
					final int position, final BaseAccount realAccount);
		}


		public AccountAdapterListener getAccountAdapterListener() {
			return accountAdapterListener;
		}
		public void setAccountAdapterListener(
				AccountAdapterListener accountAdapterListener) {
			this.accountAdapterListener = accountAdapterListener;
		}
	
	}