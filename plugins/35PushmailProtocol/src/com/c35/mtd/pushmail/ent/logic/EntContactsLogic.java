package com.c35.mtd.pushmail.ent.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.R;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.ent.bean.ContactAttribute;
import com.c35.mtd.pushmail.ent.database.EntContactDb;
import com.c35.mtd.pushmail.ent.protocol.SyncEntContactsResponse;
import com.c35.mtd.pushmail.ent.protocol.SyncEntContactsResponse.CompanyContact;
import com.c35.mtd.pushmail.ent.protocol.SyncEntProtocol;
import com.c35.mtd.pushmail.ent.utils.EntSettingUtil;
import com.c35.mtd.pushmail.logic.AccountUtil;
import com.c35.mtd.pushmail.util.StringUtil;

/**
 * 企业联系人逻辑操作
 * @Description:
 * @author:huangyx2  
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2013-5-20
 */
public class EntContactsLogic {
    
    private static final String TAG = "EntContactsLogic";
	
	// 这个类有很多可以优化的地方：
	// 1、没有考虑切换账号后，删除缓存中非默认账号域的联系人
	// 2、fireOn.. 方法中都重新获取默认联系人作判断，看是否需要通知
	// 2、默认账号改变后没有停止加载默认账号的线程
	// 可以监听默认账号的变化，来对这个类中的逻辑进行优化
	
	// 如果切换账号会执行MessageList.onCreate方法的话,
	// 那么loadCache()放在MessageList.onCreate中执行可以省去切换账号后重新加载企业联系人的一些逻辑判断，
	// 但要注意，如果是添加账号（非默认）需要调用sync方法，否则的话不用做任何操作
	
	// 0:no cache; -1:loading; 1:loaded
	private static final int CACHE_NONE = 0;
	private static final int CACHED = 1;
	private static final int CACHE_LOGIND = -1;
	
	private static EntContactsLogic instance = new EntContactsLogic();
	// 所有联系人，email重复的算一个
	private Map<String, List<ContactAttribute>> entContacts = new HashMap<String, List<ContactAttribute>>();
	// 联系人排序工具
	private static ContactComparator comparator = new ContactComparator();
	
	private static EntContactDb entDb;
	// 加载企业联系人的账号及状态
	private static Map<String, Integer> loadContactsAccount = new HashMap<String, Integer>();
	// 正在同步的账号域名后辍
	private static List<String> onSyncingAccount = new ArrayList<String>();
	
	// 企业联系人事件监听
	private static List<EntContactsListener> listeners = new ArrayList<EntContactsListener>();
	
	private EntContactsLogic(){
		entDb = EntContactDb.getInstance();
	}
	
	public static EntContactsLogic getInstance(){
		return instance;
	}
	
	public void addListener(EntContactsListener listener) {
		if(!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	public void removeListener(EntContactsListener listener) {
		listeners.remove(listener);
	}
	/**
	 * 是否正在加载缓存
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: huangyongxing
	 * @date:2012-8-2
	 */
	private boolean isLoading(String account) {
		if(loadContactsAccount.containsKey(account)){
			return loadContactsAccount.get(account) == CACHE_LOGIND;
		}
		return false;
	}

	/**
	 * 是否已加载完缓存
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: huangyongxing
	 * @date:2012-8-2
	 */
	private boolean isCached(String account) {
		if(loadContactsAccount.containsKey(account)){
			return loadContactsAccount.get(account) == CACHED;
		}
		return false;
	}
	/**
	 * 加载联系人数据到缓存中
	 * @Description:
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-20
	 */
	public void loadCache(){
		// 首先取得默认账号
		final Account account = EmailApplication.getCurrentAccount();
		if(account == null){
			return;
		}
		// 没有同步过企业联系人，则进行同步
		if(!EntSettingUtil.getInstance().isSynced(account)){
			sync(account,false);
			return;
		}
		// 账号域名后辍
		final String suffix = StringUtil.getAccountSuffix(account);
		// 没加载过（CACHE_NONE的情况下当作为加载过）且不在加载的状态
		if (!isCached(suffix) && !isLoading(suffix)) {
			new AsyncTask<Void, Void, Boolean>(){
				protected void onPreExecute() {
					loadContactsAccount.put(suffix, CACHE_LOGIND);
					fireOnLoadingContact(suffix);
				};
				@Override
				protected Boolean doInBackground(Void... params) {
					boolean result = true;
					try {
						long start = System.currentTimeMillis();
						Debug.i("ent_sync", "load ent from db start");
						// 企业联系人email address信息
						List<ContactAttribute> entTemp = entDb.getContactMails(account);
						long end = System.currentTimeMillis();
						Debug.i("ent_sync", "load ent from db used:" + (end-start));
						if(entTemp.isEmpty()){
							return false;
						}
						sortContacts(entTemp);
						long end2 = System.currentTimeMillis();
						Debug.i("ent_sync", "load ent sort used:" + (end2-end));
						entContacts.put(suffix, entTemp);
					} catch (Exception e) {// 空指针
						Debug.e("failfast", "failfast_AA", e);
						result = false;
					}
					return result;
				}
				protected void onPostExecute(Boolean result) {
					fireOnDataSetChange(suffix);
					if(result){
						loadContactsAccount.put(suffix, CACHED);
					}else{
						loadContactsAccount.put(suffix, CACHE_NONE);
					}
				};
			}.execute();
		}
	}
	
	
	/**
	 * 所有企业联系人
	 * @Description:
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-21
	 */
	public List<ContactAttribute> getAllEntContacts(){
		Account account = EmailApplication.getCurrentAccount();
		if(account == null){
			return new ArrayList<ContactAttribute>(0);
		}
		String suffix = StringUtil.getAccountSuffix(account);
		if(entContacts.containsKey(suffix)){
			return new ArrayList<ContactAttribute>(entContacts.get(suffix));
		}
		return new ArrayList<ContactAttribute>(0);
	}
	
	/**
	 * 联系人排序
	 * @Description:
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-20
	 */
	private void sortContacts(List<ContactAttribute> entContact){
		Collections.sort(entContact, comparator);
	}
	

	/**
	 * 通知联系人改变
	 * @Description:
	 * @param reason
	 * @see: 
	 * @since: 
	 * @author: huangyongxing
	 * @date:2012-12-19
	 */
	private void fireOnDataSetChange(String suffix) {
		Account account = EmailApplication.getCurrentAccount();
		if(account == null){
			return;
		}
		String suffix2 = StringUtil.getAccountSuffix(account);
		if(suffix2.equals(suffix)){
			for(EntContactsListener listener : listeners){
				listener.onDateChanged();
			}
		}
	}
	
	private void fireOnGetContacts(String suffix){
		Account account = EmailApplication.getCurrentAccount();
		if(account == null){
			return;
		}
		String suffix2 = StringUtil.getAccountSuffix(account);
		if(suffix2.equals(suffix)){
			for(EntContactsListener listener : listeners){
				listener.onGetContacts();
			}
		}
	}
	
	private void fireOnFail(String suffix){
		Account account = EmailApplication.getCurrentAccount();
		if(account == null){
			return;
		}
		String suffix2 = StringUtil.getAccountSuffix(account);
		if(suffix2.equals(suffix)){
			for(EntContactsListener listener : listeners){
				listener.onFial();
			}
		}
	}
	
	private void fireOnSucc(String suffix){
		Account account = EmailApplication.getCurrentAccount();
		if(account == null){
			return;
		}
		String suffix2 = StringUtil.getAccountSuffix(account);
		if(suffix2.equals(suffix)){
			for(EntContactsListener listener : listeners){
				listener.onSucc();
			}
		}
	}
	
	private void fireOnSearch(List<ContactAttribute> results){
		for(EntContactsListener listener : listeners){
			listener.onSearch(results);
		}
	}
	/**
	 * 正在加载
	 * @Description:
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-27
	 */
	private void fireOnLoadingContact(String suffix){
		Account account = EmailApplication.getCurrentAccount();
		if(account == null){
			return;
		}
		String suffix2 = StringUtil.getAccountSuffix(account);
		if(suffix2.equals(suffix)){
			for(EntContactsListener listener : listeners){
				listener.onLoadingContacts();
			}
		}
	}

	/**
	 * 同步企业联系人
	 * @Description:
	 * @see: 
	 * @since: 
	 * @author: huangyongxing
	 * @date:2012-8-27
	 */
	public void sync(final Account account,boolean isClick){
		// 账号域名后辍
		if(!AccountUtil.isSupportRequest("synEntContacts", account)){
			if(isClick){
				Log.e(TAG, EmailApplication.getInstance().getString(R.string.fetch_contact_notSupport));
			}
			return;
		}
		Debug.e("ent_sync", "sync");
		final String suffix = StringUtil.getAccountSuffix(account);
		// 正在同步的账号就不再进行同步
		if(onSyncingAccount.contains(suffix)){
			// 同步企业联系人信息
			Debug.e("ent_sync", "sync  正在同步的账号就不再进行同步");
			fireOnGetContacts(suffix);
			return;
		}
		
		new AsyncTask<Void, Void, Boolean>(){
			protected void onPreExecute() {
				onSyncingAccount.add(suffix);
				// 同步企业联系人信息
				fireOnGetContacts(suffix);
			};
			@Override
			protected Boolean doInBackground(Void... params) {
				boolean syncSuccessed = false;
				try {
					Debug.i("ent_sync", "sync-------start");
					SyncEntContactsResponse contacts = SyncEntProtocol.getInstance().requestEntContacts(account, entDb.getLastSyncEntTime(account));
					if(contacts == null){
						return syncSuccessed;
					}
					if(contacts.error_code != 0){
						return syncSuccessed;
					}
					// 已删除的联系人
					List<String> deletedContacts = contacts.data.deleteList;
					// 更新或添加的联系人
					List<CompanyContact> updatedContacts = contacts.data.updateList;
					Log.e("ent_sync", " 保存或修改企业联系人");
					syncSuccessed = entDb.updEntContacts(deletedContacts, updatedContacts, contacts.syncTime, account);
					updatedContacts.clear();
					deletedContacts.clear();
					System.gc();
				} catch (Exception e) {// 空指针
					Debug.e("failfast", "failfast_AA", e);
					syncSuccessed = false;
				}
				return syncSuccessed;
			}
			
			protected void onPostExecute(Boolean result) {
				// 完成后从正在同步中移除
				Debug.e("ent_sync", "sync 完成后从正在同步中移除");
				onSyncingAccount.remove(suffix);
				if(result){
					EntSettingUtil.getInstance().setSynced(account);
					fireOnSucc(suffix);
					loadContactsAccount.put(suffix, CACHE_NONE);//重设缓存状态
					loadCache();
				}else{
					fireOnFail(suffix);
				}
			};
			
		}.execute();
	}
	
	/**
	 * 搜索联系人
	 * @Description:
	 * @param keyword
	 * @see: 
	 * @since: 
	 * @author: huangyx2
	 * @date:2013-5-23
	 */
	public void search(String keyword){
		if(StringUtil.isNotEmpty(keyword)){
			new AsyncTask<String, Void, List<ContactAttribute>>(){
				protected void onPreExecute() {};
				@Override
				protected List<ContactAttribute> doInBackground(String... params) {
					String key = params[0].trim().toLowerCase(Locale.ENGLISH);
					List<ContactAttribute> temp = getAllEntContacts();
					List<ContactAttribute> results = new ArrayList<ContactAttribute>();
					for(ContactAttribute c : temp){
						if(c.getValue().toLowerCase(Locale.ENGLISH).contains(key)||c.getDisplayName().toLowerCase(Locale.ENGLISH).contains(key)){
							results.add(c);
						}
					}
					temp.clear();
					return results;
				}
				protected void onPostExecute(List<ContactAttribute> results) {
					fireOnSearch(results);
				};
			}.execute(keyword);
		}
	}
}
