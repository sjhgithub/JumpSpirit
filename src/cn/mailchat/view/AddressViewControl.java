package cn.mailchat.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import cn.mailchat.R;
import cn.mailchat.mail.Address;
import cn.mailchat.utils.StringUtil;

/**
 * 地址栏的地址输入控件
 * 
 * @Description:
 * @author:xulei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:Jun 4, 2013
 */
public class AddressViewControl extends RelativeLayout {

	// private static final String TAG = "MailAddrsViewControl";
	// private boolean DEBUG = false;
	private ChoseAddressView composeAddrView;
	private List<Address> mAddressList;
	private List<Button> mNameBtnList;
	private AutoCompleteTextView addressInputACT;
	private TextView addressesTV;
	public final int[] PADDING = { 0, 5, 15, 5 };

	int minWidth = 50;
	int minTop;

	public AddressViewControl(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int mMeasure;
		int count = getChildCount();
		int paddingLeft = getPaddingLeft();
		int paddingTop = getPaddingTop();
		int width = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - getPaddingRight();
		int height = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - getPaddingBottom();
		int mode = MeasureSpec.getMode(heightMeasureSpec);
		mMeasure = MeasureSpec.makeMeasureSpec(height, mode);
		int index = 0;
		int viewMeasureWidth;
		int viewTop;
		int viewLeft;
		int realHeight;
		int left;
		minTop = 0;
		while (index < count) {
			View view = getChildAt(index);
			if (view.getVisibility() != View.GONE) {
				if (view == addressInputACT || view == addressesTV) {
					view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), mMeasure);
					int tvMeasuredWidth = view.getMeasuredWidth();
					int tvWidth = width - paddingLeft - PADDING[0];
					if (tvWidth < 0) {
						tvWidth = width;
					}
					if (tvWidth >= tvMeasuredWidth) {
						tvMeasuredWidth = tvWidth;
					}
					if (tvMeasuredWidth < minWidth) {
						tvMeasuredWidth = minWidth;
					}
					view.measure(MeasureSpec.makeMeasureSpec(tvMeasuredWidth, MeasureSpec.EXACTLY), mMeasure);
				} else {
					view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), mMeasure);
				}
				viewMeasureWidth = view.getMeasuredWidth();
				minTop = Math.max(minTop, view.getMeasuredHeight() + PADDING[1] + PADDING[3]);
				if (paddingLeft + viewMeasureWidth + PADDING[0] > width) {
					viewLeft = getPaddingLeft() + PADDING[0];
					viewTop = paddingTop + minTop;
					left = viewLeft;
				} else {
					viewTop = paddingTop;
					left = paddingLeft;
				}
				paddingLeft = left + (viewMeasureWidth + PADDING[2]);
				paddingTop = viewTop;
			}
			index++;
		}
		if (mode == MeasureSpec.UNSPECIFIED || mode == MeasureSpec.AT_MOST) {
			realHeight = paddingTop + minTop + getPaddingBottom();
		} else {
			realHeight = height;
		}
		setMeasuredDimension(width, realHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int width = r - l;
		int paddingLeft = getPaddingLeft();
		int paddingTop = getPaddingTop();
		int count = getChildCount();
		int top = paddingTop;
		int left = paddingLeft;
		int index = 0;
		while (index < count) {
			View view = getChildAt(index);
			if (view.getVisibility() != View.GONE) {
				int viewMeasureWidth = view.getMeasuredWidth();
				int viewMeasureHeight = view.getMeasuredHeight();
				if (left + viewMeasureWidth + PADDING[0] > width) {
					left = getPaddingLeft() + PADDING[0];
					top += minTop;
				}
				view.layout(left, top + PADDING[1], left + viewMeasureWidth, viewMeasureHeight + top + PADDING[1]);
				// if (view == autoCompleteTextView) {
				// autoCompleteTextView.requestFocus();
				// }
				left += viewMeasureWidth + PADDING[2];
			}
			index++;
		}
	}

	/**
	 * 初始化
	 * 
	 * @Description:
	 * @param addrView
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public void init(ChoseAddressView addrView) {
		composeAddrView = addrView;
		mAddressList = new ArrayList<Address>();
		mNameBtnList = new ArrayList<Button>();
		addressInputACT = (AutoCompleteTextView) findViewById(R.id.address_view_address_actv);
		addressInputACT.setDropDownAnchor(this.getId());
		addressesTV = (TextView) findViewById(R.id.address_view_addresses_tv);
		addressesTV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				composeAddrView.setActive(true);
			}
		});
		addressInputACT.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Address address = (Address) parent.getItemAtPosition(position);
				if (address != null) {
					addAddress(new Address[] { address });
				}
			}

		});
		addressInputACT.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					// 点击空格或回车键时，校验邮件地址。如果合法，添加邮件地址
					// 点击删除键时，如果没有可删除的字符，删除最后一个联系人
					if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SPACE) {
						addAddress(addressInputACT.getEditableText().toString());
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_DEL) {
						if (addressInputACT.length() == 0) {
							removeAddress(mAddressList.size() - 1,false);
						}
					}
				}
				return false;
			}
		});

		addressInputACT.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if ((actionId == EditorInfo.IME_ACTION_NEXT) || (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) || (actionId == EditorInfo.IME_ACTION_DONE) || (actionId == EditorInfo.IME_ACTION_GO) || (actionId == 66)) {
					addAddress(addressInputACT.getEditableText().toString());
					return true;
				}
				return false;
			}
		});
		addressInputACT.addTextChangedListener(new MailAddressWatcher(this, getContext()));

		addressInputACT.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				composeAddrView.setOnViewClickActivity();

			}
		});

		addressInputACT.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// LogX.e(TAG, "onFocusChange() " + hasFocus + "    " + composeAddrView.getType());
				if (!hasFocus) {
					addAddress(addressInputACT.getEditableText().toString());
					composeAddrView.setActive(false);
				} 
				else {		
					composeAddrView.setActive(true);
//					// 获取焦点后，强制弹出软键盘
//					if (!(getContext() instanceof CreateGroupActivity) && !(getContext() instanceof AddGroupMembersActivity)) {
//						InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//						imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
//					}else{
						composeAddrView.setOnViewClickActivity();
//					}
				}
			}
		});
	}

	/**
	 * 向地址栏中添加邮件地址
	 * 
	 * @Description:
	 * @param addresses
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public void addAddress(Address[] addresses) {
		for (int i = 0; i < addresses.length; i++) {
			if (addresses[i].getAddress()==null) {
					Toast.makeText(getContext(), getResources().getString(R.string.contact_email_error), Toast.LENGTH_SHORT).show();
				return;
			}
			if (!isDuplicateAddress(addresses[i])) {
				ViewGroup viewgroup = (ViewGroup) View.inflate(getContext(), R.layout.address_view_btn, null);
				Button button = (Button) viewgroup.findViewById(R.id.compose_addr_view_btn);
				viewgroup.removeView(button);
				if (addresses[i].getPersonal() == null) {// 昵称为空时（应对当发件人为自己，服务器返回昵称为空的情况）
					String address = addresses[i].getAddress();
					if (address != null) {
						addresses[i].setPersonal(address.substring(0, address.indexOf("@")));
					}
				}
				button.setText(addresses[i].getPersonal());
				button.setTextColor(getContext().getResources().getColor(R.color.reciever_txt_color));
				button.setGravity(Gravity.CENTER_VERTICAL);
				button.setTag(addresses[i].getAddress());
				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						removeAddress((String) v.getTag());
					}
				});
				addView(button, mAddressList.size());

				mAddressList.add(addresses[i]);
				// 不知道为何 addressInputACT.setText(null); 会出现尽200MS卡顿
				if (addressInputACT.getText() != null && !addressInputACT.getText().toString().trim().equals("")) {
					addressInputACT.setText(null);
				}

				addressInputACT.setHint("");
				mNameBtnList.add(button);
			}
		}
		composeAddrView.addContactsCallBack(addresses);
	}

	/**
	 * 向地址栏中添加邮件地址
	 * 
	 * @Description:
	 * @param addressList
	 *            邮件地址的字符串（邮件地址用","分隔）
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public void addAddress(String addressList) {
		Address[] addresses = parseEmailAddress(addressList);
		addAddress(addresses);
	}

	/**
	 * 监听地址栏中输入的每一个字符
	 * 
	 * @Description:
	 * @param s
	 * @see:
	 * @since:
	 * @author: Zhonggaoyong
	 * @date:2013-11-18
	 */
	public void addTextWatcherLintener(String s) {
		composeAddrView.onTextChangedCallBack(s);
	}

	/**
	 * 解析邮件地址字符串为邮件地址
	 * 
	 * @Description:
	 * @param addressList
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	private Address[] parseEmailAddress(String addressList) {
		Address[] addresses = Address.parse(addressList);
		for (int i = 0; i < addresses.length; i++) {
			if (addresses[i].getPersonal() == null) {
				addresses[i].setPersonal(getNameFromEmailAddress(addresses[i].getAddress()));
			}
		}
		return addresses;
	}

	/**
	 * 从邮件地址中截取用户名
	 * 
	 * @Description:
	 * @param address
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	private String getNameFromEmailAddress(String address) {
	    if (address == null) {
	        return null;
	    } else {
	        int index = address.indexOf("@");
	        if (index < 1) {
	            return address;
	        } else {
	            return address.substring(0, index);
	        }
	    }
	}

	/**
	 * 判断邮件地址是否已经存在（是否重复）
	 * 
	 * @Description:
	 * @param address
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	private boolean isDuplicateAddress(Address address) {
		for (Address address2 : mAddressList) {
			if (address2.getAddress().equals(address.getAddress())) {
				return true;
			}
		}
		return false;
	}

	public boolean isDuplicateAddress(String email) {
		for (Address address2 : mAddressList) {
			if (address2.getAddress().equals(email)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 刪除邮件地址
	 * 
	 * @Description:
	 * @param address
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public void removeAddress(String address) {
		for (int i = 0; i < mAddressList.size(); i++) {
			if (mAddressList.get(i).getAddress().equals(address)) {
				removeAddress(i,mAddressList.get(i).isChooseListUser());
				break;
			}
		}
	}

	/**
	 * 删除指定位置的邮件地址
	 * 
	 * @Description:
	 * @param position
	 * isListValue 是否是用户自己输入的address
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public void removeAddress(int position,boolean isListValue) {
		if (position >= 0 && position < mAddressList.size()) {
			mAddressList.remove(position);
			mNameBtnList.remove(position);
			// 不知道为何 addressInputACT.setText(null); 会出现尽200MS卡顿
//			if (addressInputACT.getText() != null && !addressInputACT.getText().toString().trim().equals("")) {
//				addressInputACT.setText(null);
//			}
			removeViewAt(position);
			composeAddrView.removeContactsCallBack(isListValue);
		}
	}

	/**
	 * 展开/收起 邮件地址
	 * 
	 * @Description:
	 * @param expand
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public void setExpanded(boolean expand) {
		setExpanded(false, expand);
	}

	/**
	 * 展开/收起
	 * 
	 * @Description:
	 * @param activeFailure
	 *            默认为false 意图 1 为当 addressInputACT 不被选中，不隐藏此控件 2 失去焦点时框中显示的仍旧是button 当自己不是焦点时不进行枪焦点
	 *            且不隐藏addressInputACT
	 * @param expand
	 * @see:
	 * @since:
	 * @author: Zhonggaoyong
	 * @date:2013-11-25
	 */
	public void setExpanded(Boolean activeFailure, boolean expand) {
		addressInputACT.setVisibility((activeFailure || expand) ? View.VISIBLE : View.GONE);
		if (expand) {
			if (!addressInputACT.isFocused()) {
				addressInputACT.requestFocus();
				addressInputACT.requestFocusFromTouch();
			}
		} else {
			if (addressInputACT.isFocused()) {
				addressInputACT.clearFocus();
			}
		}
		for (Button button : mNameBtnList) {
			button.setVisibility(expand ? View.VISIBLE : View.GONE);
		}
		String addresses = getAddressString();
		String inputStr = addressInputACT.getEditableText().toString();
		if (inputStr != null && !inputStr.equals("")) {
			addresses += inputStr;
		}
		addressesTV.setText((activeFailure || expand) ? "" : addresses);
		if (!activeFailure)
			addressesTV.setVisibility(expand ? View.GONE : View.VISIBLE);
	}

	/**
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	private String getAddressString() {
		String address = "";
		for (int i = 0; i < mAddressList.size(); i++) {
			address += mAddressList.get(i).getAddress();
			address += ",";
		}
		return address;
	}

	/**
	 * 设置地址栏的Adapter
	 * 
	 * @Description:
	 * @param adapter
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
		if (adapter != null) {
			addressInputACT.setAdapter(adapter);
		}
	}

	/**
	 * 获取地址栏中，已经输入完成的邮件地址
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public List<Address> getAddresses() {
		return mAddressList;
	}

	/**
	 * 获取地址栏中正在编辑的邮件地址
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jun 4, 2013
	 */
	public Address[] getInputAddress() {
		return parseEmailAddress(addressInputACT.getEditableText().toString());
	}

	/**
	 * 返回当前地址输入栏，以便扩展
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: Zhonggaoyong
	 * @date:2013-11-25
	 */
	public AutoCompleteTextView getAutoCompleteTextView() {
		return addressInputACT;
	}
}
