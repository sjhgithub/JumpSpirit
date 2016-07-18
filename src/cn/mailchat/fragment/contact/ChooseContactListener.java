package cn.mailchat.fragment.contact;

import java.util.List;

import android.widget.ListView;

import cn.mailchat.adapter.CommonContactsAdapter;
import cn.mailchat.adapter.Contact35EisAdapter;
import cn.mailchat.adapter.ContactsListAdapter;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.view.ChoseAddressView;
/**
 * 
 * @copyright © 35.com
 * @file name ：ChooseContactListener.java
 * @author ：zhangjx
 * @create Data ：2015-9-23上午11:42:22
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2015-9-23上午11:42:22
 * @Modified by：zhangjx
 * @Description :tab fragment 回调
 */
public interface ChooseContactListener {
	
	void addContacts(List<ContactAttribute> contact);
	void reomveContacts(String email);
	void showChoosedToast(String email);//已经选过了
	void changeEditViewHeightAutoToContent();
	String getSearchContactsEditViewText();
	boolean isContactChoosed(String email);//是否已经选过了
	ListView getAutoCompleteListView();
	void ShowAutoCompleteListView();
	void hideAutoCompleteListView();
//	void setMenbers();
	ChoseAddressView getSearchContactsEditView();
}
