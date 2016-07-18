package cn.mailchat.fragment;

import java.util.ArrayList;
import java.util.List;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.ChattingSingleActivity;
import cn.mailchat.activity.ChooseContactsActivity;
import cn.mailchat.activity.Main4TabActivity;
import cn.mailchat.adapter.ChatListAdapter;
import cn.mailchat.adapter.InviteChatAdapter;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.DChat;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.drag.ViewDragHelper;
import cn.mailchat.drag.Canvas4DragView;
import cn.mailchat.provider.ChattingProvider;

public class ChatListFragment extends Fragment implements LoaderCallbacks<Cursor>{
	private static String EXTRA_SEARCH_KEYWORD = "searchKeyword";
	private static String EXTRA_IS_SEARCH_VIEW = "searchView";
	private View view;
	private String accountUuid;
	private ChatListAdapter mAdapter;
	private ListView mListView;
	private Account account;
	private boolean isSearchView=false;
	private String mKeyWord;
	private ChatListFragmentListener mChatListFragmentListener;
	// 逻辑处理接口
	private MessagingController messageController;
	private InviteChatAdapter inviteChatAdapter;
	private List<ContactAttribute> contactList;
	private TextView searchResult;
	private LinearLayout inviteLayout;
	private ListView contactListView;
	private LinearLayout inviteItemLayout,authenticateLayout,chatLayout;
	private Handler mHandler = new Handler();

	public static ChatListFragment newInstance(String keyWord,boolean isSearchView) {
		ChatListFragment fragment = new ChatListFragment();
		Bundle args = new Bundle();
		args.putString(EXTRA_SEARCH_KEYWORD, keyWord);
		args.putBoolean(EXTRA_IS_SEARCH_VIEW, isSearchView);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		decodeArguments();
		if(isSearchView){
			try {
				mChatListFragmentListener = (ChatListFragmentListener) activity;
			} catch (ClassCastException e) {
				throw new ClassCastException(activity.getClass()
						+ " must implement ChatListFragmentListener");
			}
		}
	}
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(0, null, this);
	}
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		//Log.d("qxian", "--ChatListFragment--onCreateView");
		view = inflater.inflate(R.layout.fragment_tab_chat_list, container, false);
		messageController=MessagingController.getInstance(MailChat.app);
		messageController.addListener(listener);
		initView();
		initData();
		return view;
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		showAuthenticateLy();
	}
	private void decodeArguments() {
		Bundle args = getArguments();
		if (args!=null) {
			isSearchView = args.getBoolean(EXTRA_IS_SEARCH_VIEW);
			mKeyWord = args.getString(EXTRA_SEARCH_KEYWORD);
		}
	}

	private void initView() {
		// TODO Auto-generated method stub
		chatLayout = (LinearLayout) view.findViewById(R.id.ly_chat);
		mListView = (ListView) view.findViewById(R.id.chat_listview);
		searchResult = (TextView) view.findViewById(R.id.search_result_no_data);
		inviteLayout = (LinearLayout) view.findViewById(R.id.chat_list_no_data_invite);
		contactListView = (ListView) view.findViewById(R.id.contact_chat);
		contactList = new ArrayList<ContactAttribute>();
		inviteChatAdapter = new InviteChatAdapter(account, getActivity(),contactList);
		contactListView.setAdapter(inviteChatAdapter);
		inviteItemLayout = (LinearLayout) view.findViewById(R.id.ly_invite_contact);
		inviteItemLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				CreateChattingActivity.startActivityToInviteChat(getActivity(), true, true);
//				InviteContactActivity.actionContactInvite(getActivity(), account,true);
				ChooseContactsActivity.startActivityToInviteChat(getActivity(),true, true);				
				MobclickAgent.onEvent(getActivity(), "chat_list_nodata_jump_to_invite_contact_act");
			}
		});
		contactListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				ContactAttribute contact =contactList.get(position);
				DChat dchat=null;
				if(contact.getEmail().equals(GlobalConstants.HELP_ACCOUNT_EMAIL)){
					dchat=messageController.getHelpDChat(account);
				}else{
					dchat =new DChat();
					dchat.setUid(DChat.getDchatUid(account.getEmail()+","+contact.getEmail()));
					dchat.setEmail(contact.getEmail());
					dchat.setVisibility(false);
					dchat.setDChatAlert(true);
					dchat.setSticked(false);
					dchat.setNickName(contact.getNickName());
					messageController.saveDChat(account, dchat);
				}
				ChattingSingleActivity.actionChatList(getActivity(), dchat, account);
			}
		});
		authenticateLayout = (LinearLayout) view.findViewById(R.id.ly_authenticate);
	}
	private void initData() {
		// TODO Auto-generated method stub
		account=Preferences.getPreferences(getActivity()).getDefaultAccount();
		accountUuid= account.getUuid();
		mAdapter =new ChatListAdapter(getActivity(), null,account);
		ViewDragHelper.prepareListForViewDrag(mListView, getActivity());
		mListView.setAdapter(mAdapter);
		if(!isSearchView){
			messageController.createJumpItem(account, GlobalConstants.DCHAT_JUMP_INVITE_COLLEAGUES, getString(R.string.chat_list_invite_colleagues_message));
			messageController.syncGroups(account, null);
			messageController.getTotalMsgUnreadCount(account, null);
			messageController.addOAItem(account);
		}
	}
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// TODO Auto-generated method stub
		Uri uri=null;
		if(!isSearchView){
			uri =  Uri.withAppendedPath(ChattingProvider.CONTENT_URI, "account/" + accountUuid +
	                "/chattings");
		}else{
			uri =  Uri.withAppendedPath(ChattingProvider.CONTENT_URI, "account/" + accountUuid +
	                "/chattings/search/"+mKeyWord);
		}
		return new CursorLoader(getActivity(), uri, null, null,
				null, null);
	}
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// TODO Auto-generated method stub
		if(data!=null){
			int count = data.getCount();
			if(isSearchView){
				inviteLayout.setVisibility(View.GONE);
				if(count>0){
					messageController.addHelpDChatForChatList(account);
				}else{
					mChatListFragmentListener.updateChatSearchReasut(count);
					searchResult.setVisibility(View.VISIBLE);
					if (isAdded()) {
						searchResult.setText(getString(R.string.search_no_result));
					}
				}
			}else{
				if(count>1){//由于会自动生成createJumpItem这个条目
					inviteLayout.setVisibility(View.GONE);
					messageController.addHelpDChatForChatList(account);
				}else{
					inviteLayout.setVisibility(View.VISIBLE);
					messageController.synAndGetContactAttributesByUsedMailChat(account);
				}
			}
			mAdapter.swapCursor(data);
		}
	}
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
	}

	public interface ChatListFragmentListener {
		void updateChatSearchReasut(int searchReasutCount);
	}
	
	private MessagingListener listener =new MessagingListener(){
		public void getContactsByUsedMailChatFinished(Account acc,final List<ContactAttribute> contacts){
			if(account.getUuid().equals(acc.getUuid())&&!isSearchView){
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						contactList=contacts;
						inviteChatAdapter.setContactList(contacts);
						inviteChatAdapter.notifyDataSetChanged();
					}
				});
			}
		}
		
		public void updateChattingImgHead(Account acc,final ContactAttribute newContactAttribute){
			if(account.getUuid().equals(acc.getUuid())){
				messageController.getContactAttributesByUsedMailChat(account);
			}
		}

		public void kickedOutGroupByMemberSuccess(Account acc,final CGroup cGroup) {
			if (account.getUuid().equals(acc.getUuid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getActivity(), String.format(getResources().getString(R.string.kicked_out_member),cGroup.getGroupName()), Toast.LENGTH_LONG).show();
					}
				});
			}
		}
		public void delteGroupInfoByMemberSuccess(Account acc,final CGroup cGroup) {
			if (account.getUuid().equals(acc.getUuid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getActivity(), String.format(getResources().getString(R.string.deleted_member),cGroup.getGroupName()), Toast.LENGTH_LONG).show();
					}
				});
			}
		}

		public void UserIfCertificateProblem(Account acc) {
			if (account.getUuid().equals(acc.getUuid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						account = Preferences.getPreferences(getActivity())
								.getAccount(account.getUuid());
						showAuthenticateLy();
					}
				});
			}
		}
	};
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		messageController.removeListener(listener);
	}
	/**
	 * 判断是否显示账号异常提醒
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-12-22
	 */
	private void showAuthenticateLy(){
		if(!account.isAuthenticated()){
			authenticateLayout.setVisibility(View.VISIBLE);
			chatLayout.setVisibility(View.GONE);
		}else{
			authenticateLayout.setVisibility(View.GONE);
			chatLayout.setVisibility(View.VISIBLE);
		}
	}
}
