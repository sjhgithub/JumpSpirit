package com.c35.mtd.pushmail.util;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.c35.mtd.pushmail.Debug;

import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;

/**
 * 
 * @Description:邮件地址工具类
 * @author:gongfacun
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */

public class Address {

	/**
	 * Address part, in the form local_part@domain_part. No surrounding angle brackets.
	 */
	String mAddress;

	/**
	 * Name part. No surrounding double quote, and no MIME/base64 encoding. This must be null if Address has
	 * no name part.
	 */
	String mPersonal;

	// Regex that matches address surrounded by '<>' optionally. '^<?([^>]+)>?$'
	private static final Pattern REMOVE_OPTIONAL_BRACKET = Pattern.compile("^<?([^>]+)>?$");
	// Regex that matches personal name surrounded by '""' optionally.
	// '^"?([^"]+)"?$'
	private static final Pattern REMOVE_OPTIONAL_DQUOTE = Pattern.compile("^\"?([^\"]*)\"?$");
	// Regex that matches escaped character '\\([\\"])'
	private static final Pattern UNQUOTE = Pattern.compile("\\\\([\\\\\"])");

	private static final Address[] EMPTY_ADDRESS_ARRAY = new Address[0];

	// delimiters are chars that do not appear in an email address, used by
	// pack/unpack
	private static final char LIST_DELIMITER_EMAIL = '\1';
	private static final char LIST_DELIMITER_PERSONAL = '\2';
	private static final char EMAIL_SEPARATOR = '@';

	public Address(String address, String personal) {
		setAddress(address);
		setPersonal(personal);
	}

	public Address(String address, String personal, boolean fast) {
		this.mAddress = address;
		this.mPersonal = personal;
	}

	public Address(String address) {
		setAddress(address);
	}

	public String getAddress() {
		return mAddress;
	}

	public void setAddress(String address) {
		this.mAddress = REMOVE_OPTIONAL_BRACKET.matcher(address).replaceAll("$1");
		;
	}

	/**
	 * Get name part as UTF-16 string. No surrounding double quote, and no MIME/base64 encoding.
	 * 
	 * @return Name part of email address. Returns null if it is omitted.
	 */
	public String getPersonal() {
		return mPersonal;
	}

	/**
	 * Set name part from UTF-16 string. Optional surrounding double quote will be removed. It will be also
	 * unquoted and MIME/base64 decoded.
	 * 
	 * @param Personal
	 *            name part of email address as UTF-16 string. Null is acceptable.
	 */
	public void setPersonal(String personal) {
		if (personal != null) {
			personal = REMOVE_OPTIONAL_DQUOTE.matcher(personal).replaceAll("$1");
			personal = UNQUOTE.matcher(personal).replaceAll("$1");
			// personal = DecoderUtil.decodeEncodedWords(personal);
			if (personal.trim().length() == 0) {
				personal = null;
			}
		}
		this.mPersonal = personal;
	}

	/**
	 * This method is used to check that all the addresses that the user entered in a list (e.g. To:) are
	 * valid, so that none is dropped.
	 */
	public static boolean isAllValid(String addressList) {
		// This code mimics the parse() method below.
		// I don't know how to better avoid the code-duplication.
		if (addressList != null && addressList.length() > 0) {
			Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(addressList);
			for (int i = 0, length = tokens.length; i < length; ++i) {
				Rfc822Token token = tokens[i];
				String address = token.getAddress();
				if (!TextUtils.isEmpty(address) && !isValidAddress(address)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Parse a comma-delimited list of addresses in RFC822 format and return an array of Address objects.
	 * 
	 * @param addressList
	 *            Address list in comma-delimited string.
	 * @return An array of 0 or more Addresses.
	 */
	public static Address[] parse(String addressList) {
		if (addressList == null || addressList.length() == 0) {
			return EMPTY_ADDRESS_ARRAY;
		}
		Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(addressList);
		ArrayList<Address> addresses = new ArrayList<Address>();
		for (int i = 0, length = tokens.length; i < length; ++i) {
			Rfc822Token token = tokens[i];
			String address = token.getAddress();
			if (!TextUtils.isEmpty(address)) {
				String name = token.getName();
				addresses.add(new Address(address, name));
			}
		}
		return addresses.toArray(new Address[] {});
	}

	public static String toUsersParse(String address) {
		StringBuffer strtmp = new StringBuffer();
		StringBuffer strname = new StringBuffer();
		int size = address.length();
		int i = 0;
		while (i < size) {
			char c = address.charAt(i);
			if (c != '') {
				if (c == '<') {
					strname.append(strtmp.toString());
					// Debug.i("Address", "strname:" + strname.toString());
				}
				strtmp.append(c);
				if (c == '>') {
					strtmp.setLength(0);
				}
			}
			i++;
		}
		return strname.toString();
	}

	public static String reAddress(String oldAddressList) {
		String addrs[] = oldAddressList.split(",");
		String newAddressList = "";
		for (int i = 0; i < addrs.length; i++) {
			String mAddrs[] = addrs[i].split("<");
			if (i != 0) {
				newAddressList += ",";
			}
			for (int k = 0; k < mAddrs.length; k++) {
				if (k != 0) {
					newAddressList += "<";
				}
				newAddressList += mAddrs[k];// MimeUtility.foldAndEncode2(mAddrs[k],
											// 0) + "";
			}
		}
		return newAddressList.equals("") ? oldAddressList : newAddressList;
	}

	// add by guozhh.
	// public static boolean isValidAddress(String address){
	// int len = address.length();
	// int firstAt = address.indexOf('@');
	// int lastAt = address.lastIndexOf('@');
	// int firstDot = address.indexOf('.', lastAt + 1);
	// int lastDot = address.lastIndexOf('.');
	// return firstAt > 0 && firstAt == lastAt && lastAt + 1 < firstDot
	// && firstDot <= lastDot && lastDot < len - 1;
	// }

	/**
	 * Checks whether a string email address is valid. E.g. name@domain.com is valid.
	 */
	/* package */public static boolean isValidAddress(String address) {
		// Note: Some email provider may violate the standard, so here we only
		// check that
		// address consists of two part that are separated by '@', and domain
		// part contains
		// at least one '.'.
		int len = address.length();
		int firstAt = address.indexOf('@');
		int lastAt = address.lastIndexOf('@');
		int firstDot = address.indexOf('.', lastAt + 1);
		int lastDot = address.lastIndexOf('.');
		return firstAt > 0 && firstAt == lastAt && lastAt + 1 < firstDot && firstDot <= lastDot && lastDot < len - 1;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Address) {
			// It seems that the spec says that the "user" part is
			// case-sensitive,
			// while the domain part in case-insesitive.
			// So foo@yahoo.com and Foo@yahoo.com are different.
			// This may seem non-intuitive from the user POV, so we
			// may re-consider it if it creates UI trouble.
			// A problem case is "replyAll" sending to both
			// a@b.c and to A@b.c, which turn out to be the same on the server.
			// Leave unchanged for now (i.e. case-sensitive).
			return getAddress().equals(((Address) o).getAddress());
		}
		return super.equals(o);
	}

	/**
	 * Get human readable address string. Do not use this for email header.
	 * 
	 * @return Human readable address string. Not quoted and not encoded.
	 */
	public String toString() {
		if (mPersonal != null) {
			if (mPersonal.matches(".*[\\(\\)<>@,;:\\\\\".\\[\\]].*")) {
				return Utility.quoteString(mPersonal) + " <" + mAddress + ">";
			} else {
				return mPersonal + " <" + mAddress + ">";
			}
		} else {
			return mAddress;
		}
	}

	/**
	 * Get human readable comma-delimited address string.
	 * 
	 * @param addresses
	 *            Address array
	 * @return Human readable comma-delimited address string.
	 */
	public static String toString(Address[] addresses) {
		if (addresses == null || addresses.length == 0) {
			return null;
		}
		if (addresses.length == 1) {
			return addresses[0].toString();
		}
		StringBuffer sb = new StringBuffer(addresses[0].toString());
		for (int i = 1; i < addresses.length; i++) {
			sb.append(',');
			sb.append(addresses[i].toString());
		}
		return sb.toString();
	}

	/**
	 * Get Human friendly address string.
	 * 
	 * @return the personal part of this Address, or the address part if the personal part is not available
	 */
	public String toFriendly() {
		if (mPersonal != null && mPersonal.trim().length() > 0) {
			return mPersonal;
		} else {
			return mAddress;
		}
	}

	/**
	 * Creates a comma-delimited list of addresses in the "friendly" format (see toFriendly() for details on
	 * the per-address conversion).
	 * 
	 * @param addresses
	 *            Array of Address[] values
	 * @return A comma-delimited string listing all of the addresses supplied. Null if source was null or
	 *         empty.
	 */
	public static String toFriendly(Address[] addresses) {
		if (addresses == null || addresses.length == 0) {
			return null;
		}
		if (addresses.length == 1) {
			return addresses[0].toFriendly();
		}
		StringBuffer sb = new StringBuffer(addresses[0].toFriendly());
		for (int i = 1; i < addresses.length; i++) {
			sb.append(',');
			sb.append(addresses[i].toFriendly());
		}
		return sb.toString();
	}

	/**
	 * Returns exactly the same result as Address.toString(Address.unpack(packedList)).
	 */
	public static String unpackToString(String packedList) {
		return toString(unpack(packedList));
	}

	/**
	 * Returns exactly the same result as Address.pack(Address.parse(textList)).
	 */
	public static String parseAndPack(String textList) {
		return Address.pack(Address.parse(textList));
	}

	/**
	 * Returns null if the packedList has 0 addresses, otherwise returns the first address. The same as
	 * Address.unpack(packedList)[0] for non-empty list. This is an utility method that offers some
	 * performance optimization opportunities.
	 */
	public static Address unpackFirst(String packedList) {
		Address[] array = unpack(packedList);
		return array.length > 0 ? array[0] : null;
	}

	/**
	 * Unpacks an address list previously packed with pack()
	 * 
	 * @param addressList
	 *            String with packed addresses as returned by pack()
	 * @return array of addresses resulting from unpack
	 */
	public static Address[] unpack(String addressList) {
		if (addressList == null || addressList.length() == 0) {
			return EMPTY_ADDRESS_ARRAY;
		}
		ArrayList<Address> addresses = new ArrayList<Address>();
		int length = addressList.length();
		int pairStartIndex = 0;
		int pairEndIndex = 0;

		/*
		 * addressEndIndex is only re-scanned (indexOf()) when a LIST_DELIMITER_PERSONAL is used, not for
		 * every email address; i.e. not for every iteration of the while(). This reduces the theoretical
		 * complexity from quadratic to linear, and provides some speed-up in practice by removing redundant
		 * scans of the string.
		 */
		int addressEndIndex = addressList.indexOf(LIST_DELIMITER_PERSONAL);

		while (pairStartIndex < length) {
			pairEndIndex = addressList.indexOf(LIST_DELIMITER_EMAIL, pairStartIndex);
			if (pairEndIndex == -1) {
				pairEndIndex = length;
			}
			Address address;
			if (addressEndIndex == -1 || pairEndIndex <= addressEndIndex) {
				// in this case the DELIMITER_PERSONAL is in a future pair,
				// so don't use personal, and don't update addressEndIndex
				address = new Address(addressList.substring(pairStartIndex, pairEndIndex), null);
			} else {
				address = new Address(addressList.substring(pairStartIndex, addressEndIndex), addressList.substring(addressEndIndex + 1, pairEndIndex));
				// only update addressEndIndex when we use the
				// LIST_DELIMITER_PERSONAL
				addressEndIndex = addressList.indexOf(LIST_DELIMITER_PERSONAL, pairEndIndex + 1);
			}
			addresses.add(address);
			pairStartIndex = pairEndIndex + 1;
		}
		return addresses.toArray(EMPTY_ADDRESS_ARRAY);
	}

	/**
	 * 从addressList中将邮箱地址提取出来 add by zengld
	 * 
	 * @param addressList
	 * @return
	 */
	public static String getMailAddress(String addressList) {
		String mailAddress = "";
		int personIndex = addressList.indexOf(LIST_DELIMITER_PERSONAL);

		/*
		 * Altered by xulei @2011-01-11 The addressList may just contains the Email Address,then the
		 * personIndex will be -1. In this situation ,we may return null or just the addressList
		 */
		if (personIndex == -1) {
			personIndex = addressList.indexOf(EMAIL_SEPARATOR);
		}
		mailAddress = addressList.substring(0, personIndex);
		return mailAddress;
	}

	/**
	 * Packs an address list into a String that is very quick to read and parse. Packed lists can be unpacked
	 * with unpack(). The format is a series of packed addresses separated by LIST_DELIMITER_EMAIL. Each
	 * address is packed as a pair of address and personal separated by LIST_DELIMITER_PERSONAL, where the
	 * personal and delimiter are optional. E.g. "foo@x.com\1joe@x.com\2Joe Doe"
	 * 
	 * @param addresses
	 *            Array of addresses
	 * @return a string containing the packed addresses.
	 */
	public static String pack(Address[] addresses) {
		// TODO: return same value for both null & empty list
		if (addresses == null) {
			return null;
		}
		final int nAddr = addresses.length;
		if (nAddr == 0) {
			return "";
		}

		// shortcut: one email with no displayName
		if (nAddr == 1 && addresses[0].getPersonal() == null) {
			return addresses[0].getAddress();
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < nAddr; i++) {
			if (i != 0) {
				sb.append(LIST_DELIMITER_EMAIL);
			}
			final Address address = addresses[i];
			sb.append(address.getAddress());
			final String displayName = address.getPersonal();
			if (displayName != null) {
				sb.append(LIST_DELIMITER_PERSONAL);
				sb.append(displayName);
			}
		}
		return sb.toString();
	}

	/**
	 * Produces the same result as pack(array), but only packs one (this) address.
	 */
	public String pack() {
		final String address = getAddress();
		final String personal = getPersonal();
		if (personal == null) {
			return address;
		} else {
			return address + LIST_DELIMITER_PERSONAL + personal;
		}
	}

	/**
	 * Legacy unpack() used for reading the old data (migration), as found in LocalStore (Donut; db version up
	 * to 24).
	 * 
	 * @See unpack()
	 */
	/* package */static Address[] legacyUnpack(String addressList) {
		if (addressList == null || addressList.length() == 0) {
			return new Address[] {};
		}
		ArrayList<Address> addresses = new ArrayList<Address>();
		int length = addressList.length();
		int pairStartIndex = 0;
		int pairEndIndex = 0;
		int addressEndIndex = 0;
		while (pairStartIndex < length) {
			pairEndIndex = addressList.indexOf(',', pairStartIndex);
			if (pairEndIndex == -1) {
				pairEndIndex = length;
			}
			addressEndIndex = addressList.indexOf(';', pairStartIndex);
			String address = null;
			String personal = null;
			if (addressEndIndex == -1 || addressEndIndex > pairEndIndex) {
				address = Utility.fastUrlDecode(addressList.substring(pairStartIndex, pairEndIndex));
			} else {
				address = Utility.fastUrlDecode(addressList.substring(pairStartIndex, addressEndIndex));
				personal = Utility.fastUrlDecode(addressList.substring(addressEndIndex + 1, pairEndIndex));
			}
			addresses.add(new Address(address, personal));
			pairStartIndex = pairEndIndex + 1;
		}
		return addresses.toArray(new Address[] {});
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 1;
	}

}
