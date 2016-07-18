package com.c35.mtd.pushmail.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.beans.C35Attachment;
import com.c35.mtd.pushmail.beans.C35CompressItem;
import com.c35.mtd.pushmail.beans.C35Folder;
import com.c35.mtd.pushmail.beans.C35Message;
import com.c35.mtd.pushmail.beans.Contact;
import com.c35.mtd.pushmail.beans.ErrorObj;
import com.c35.mtd.pushmail.beans.Label;
import com.c35.mtd.pushmail.beans.MailStatusObj;
import com.c35.mtd.pushmail.command.response.DownloadDataResponse;
import com.c35.mtd.pushmail.command.response.GetAttachmentListResponse;
import com.google.gson.JsonArray;

/**
 * 
 * @Description:Json创建与解析工具类
 * @author:gongfacun
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public final class JsonUtil {

	private static String TAG = "JsonUtil";

	// private int totalBlock;
	// private int currentBlock;//1-*,以1开始
	// private int dataLength;
	// private byte[] dataContent;//资源的内容，转成字符再传输，要先进行解压缩//文档数据先切分成合适大小的块，再对每块进行压缩传输
	public static DownloadDataResponse parseAtt(JSONObject jsonObj) throws JSONException {
		DownloadDataResponse result = new DownloadDataResponse();
		try {
			result.setTotalBlock(jsonObj.getInt("totalBlock"));
			result.setCurrentBlock(jsonObj.getInt("currentBlock"));
			result.setDataLength(jsonObj.getInt("dataLength"));
			// result.setDataContent((byte[])(jsonObj.get("dataContent")));
		} catch (JSONException e) {
			Debug.w("C35", "JsonExp", e);
			throw e;
		}
		return result;
	}

	/**
	 * @throws JSONException
	 * @Title: parseMessage
	 * @Description: 描述 解析拿一封邮件的响应GetMailByIdResponse
	 * 
	 *               【邮件内容】(added by zhuanggy)
	 * 
	 * 
	 * @author liujie
	 * @date 2011-11-29
	 * @return C35Message
	 * @throws
	 */
	public static C35Message parseMessage(JSONObject jsonObj) throws JSONException {
		C35Message message = new C35Message();
		try {
			message.setOversea(jsonObj.getInt("oversea"));
			message.setPlainTextCharset(jsonObj.getString("plainTextCharset"));
			message.setSubject(jsonObj.getString("subject"));
			message.setPlainText(jsonObj.getString("plainText"));
			message.setTo(parseStringArray(jsonObj.getJSONArray("to")));
			message.setSignId(jsonObj.getString("signId"));
			message.setCalendarStartTime(jsonObj.getString("calendarStartTime"));
			message.setCalendarEndTime(jsonObj.getString("calendarEndTime"));
			message.setCalendarLocation(jsonObj.getString("calendarLocation"));
			message.setCalendarRemindTime(jsonObj.getString("calendarRemindTime"));
			message.setRelay(jsonObj.getString("relay"));
			String mailId = jsonObj.getString("mailId");
			message.setMailId(mailId);
			message.setCompressItems(parseCompressItemArray(jsonObj.getJSONArray("compressItems")));
			message.setAcknowledgme(jsonObj.getInt("acknowledgme"));
			message.setCalendarText(jsonObj.getString("calendarText"));
			message.setFrom(jsonObj.getString("from"));
			String folder_id = jsonObj.getString("folderId");
			message.setFolderId(jsonObj.getString("folderId"));
			message.setFolderName(folder_id);
			message.setMailType(Integer.valueOf(jsonObj.getString("mailType")));
			message.setRecallFlag(jsonObj.getString("recallFlag"));
			message.setTimingSendTime(jsonObj.getString("timingSendTime"));
			message.setSaveSent(jsonObj.getInt("saveSent"));
			message.setByOneSelf(jsonObj.getInt("byOneSelf"));
			message.setPriority(jsonObj.getInt("priority"));
			message.setReply(jsonObj.getString("reply"));
			message.setRead(jsonObj.getInt("read"));// jsonObj.getInt("read")
			message.setSenderEmail(jsonObj.getString("senderEmail"));
			message.setCalendarState(Integer.valueOf(jsonObj.getString("calendarState")));
			message.setCalendarCloseRemind(jsonObj.getInt("calendarCloseRemind"));
			message.setReportMessage(jsonObj.getInt("reportMessage"));
			message.setHyperText(jsonObj.getString("hyperText"));
			message.setLabels(parseLabelArray(jsonObj.getJSONArray("labels")));
			message.setSendTime(jsonObj.getString("sendTime"));
			message.setBcc(parseStringArray(jsonObj.getJSONArray("bcc")));
			message.setSize(jsonObj.getInt("size"));
			message.setDeliverStatus(jsonObj.getInt("deliverStatus"));
			message.setImportantFlag(jsonObj.getInt("importantFlag"));
			message.setReplyTo(jsonObj.getString("replyTo"));
			message.setHyperTextCharset(jsonObj.getString("hyperTextCharset"));
			message.setCalendarTextCharset(jsonObj.getString("calendarTextCharset"));
			message.setCc(parseStringArray(jsonObj.getJSONArray("cc")));

			message.setHyperTextSize(jsonObj.getInt("hyperTextSize"));
			message.setCompressedToSize(jsonObj.optLong("compressedToSize"));//imcore

			if (jsonObj.has("deliveredReadCount")) {
				message.setDeliveredReadCount(jsonObj.getInt("deliveredReadCount"));
				message.setDeliveredReadUsers(parseStringArray(jsonObj.getJSONArray("deliveredReadUsers")));
			}
			message.setAttachs(parseAttaArray(jsonObj.getJSONArray("attachs"), mailId, message.getFrom(), message.getSubject(), message.getFolderId(), message.getSendTime()));
			int count = 0;
			for (C35Attachment att : message.getAttachs()) {
				if (att.getCid() != null && att.getCid().length() > 0 && !att.getCid().equals("")) {
					count++;
				}
			}
			message.setAttachSize(message.getAttachs().size() - count);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Debug.w("C35", "JsonExp", e);
			throw e;
		}
		return message;
	}

	/**
	 * @throws JSONException
	 * @Title: parseMessage
	 * @Description: 描述 解析拿一封邮件的响应GetMailByIdResponse
	 * 
	 *               【邮件头信息】(added by zhuanggy)
	 * 
	 * @author liujie
	 * @date 2011-11-29
	 * @return C35Message
	 * @throws
	 */
	// "to":["liujie <liujie@szdep.com>"],
	// "relay":"",
	// "subject":"dfdfadsf",
	// "sourceProcessState":0,
	// "acknowledgme":0,
	// "from":"admin <admin@szdep.com>",
	// "attachSize":0,
	// "folderId":"inbox",
	// "mailType":0,
	// "recallFlag":"",
	// "sourceSystemData":"",
	// "priority":3,
	// "read":0,
	// "reply":"",
	// "senderEmail":"admin <admin@szdep.com>",
	// "calendarState":3,
	// "reportMessage":0,
	// "labels":[],
	// "bcc":[],
	// "sendTime":"2012-02-24 16:11:26",
	// "size":1893,
	// "hyperTextSize":107,
	// "importantFlag":0,
	// "replyTo":"",
	// "sourceSystem":"DEF",
	// "mailId":"4f47462ee4b0b72c996ac478_inbox",
	// "cc":[]}
	public static C35Message parseMessageHead(JSONObject jsonObj) throws JSONException {
		C35Message message = null;
		try {
			message = new C35Message();
			message.setMailType(Integer.valueOf(jsonObj.getString("mailType")));
			message.setCalendarState(Integer.valueOf(jsonObj.getString("calendarState")));
			message.setSourceProcessState(jsonObj.getInt("sourceProcessState"));
			message.setPreview(jsonObj.getString("preview"));
			message.setSendTime(jsonObj.getString("sendTime"));
			message.setSubject(jsonObj.getString("subject"));
			message.setSendTime(jsonObj.getString("sendTime"));
			message.setTo(parseStringArray(jsonObj.getJSONArray("to")));
			message.setRelay(jsonObj.getString("relay"));
			message.setAcknowledgme(jsonObj.getInt("acknowledgme"));
			message.setFrom(jsonObj.getString("from"));
			message.setAttachSize(jsonObj.getInt("attachSize"));
			message.setFolderId(jsonObj.getString("folderId"));
			Log.d(TAG, "message.getFolderId==" + message.getFolderId());
			message.setMailType(jsonObj.getInt("mailType"));
			message.setRecallFlag(jsonObj.getString("recallFlag"));
			message.setSourceSystemData(jsonObj.getString("sourceSystemData"));
			message.setPriority(jsonObj.getInt("priority"));
			message.setReply(jsonObj.getString("reply"));
			message.setRead(jsonObj.getInt("read"));
			message.setSenderEmail(jsonObj.getString("senderEmail"));
			message.setCalendarState(jsonObj.getInt("calendarState"));
			message.setReportMessage(jsonObj.getInt("reportMessage"));
			message.setLabels(parseLabelArray(jsonObj.getJSONArray("labels")));
			message.setBcc(parseStringArray(jsonObj.getJSONArray("bcc")));
			message.setCc(parseStringArray(jsonObj.getJSONArray("cc")));
			message.setSize(jsonObj.getInt("size"));
			message.setImportantFlag(jsonObj.getInt("importantFlag"));
			message.setReplyTo(jsonObj.getString("replyTo"));
			message.setMailId(jsonObj.getString("mailId"));
			Log.d(TAG, "mailId==" + message.getMailId());
			message.setSourceSystem(jsonObj.getString("sourceSystem"));
			message.setHyperTextSize(jsonObj.getInt("hyperTextSize"));
			if (jsonObj.has("deliveredReadCount")) {
				message.setDeliveredReadCount(jsonObj.getInt("deliveredReadCount"));
				Log.d(TAG, "getDeliveredReadCount==" + message.getDeliveredReadCount());
				message.setDeliveredReadUsers(parseStringArray(jsonObj.getJSONArray("deliveredReadUsers")));
				Log.d(TAG, "message.getDeliveredReadUsers()==" + message.getDeliveredReadUsers().toString());
			}

			String isImportantFrom = jsonObj.getString("isImportantFrom");
			if (isImportantFrom != null && isImportantFrom.equals("Y")) {
				message.setImportantFrom(1);
			} else {
				message.setImportantFrom(0);
			}
			// Debug.e("读取邮件头", "getAttachSize=" + message.getAttachSize());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Debug.w("C35", "JsonExp", e);
			throw e;
		}
		return message;
	}

	/**
	 * @throws JSONException
	 * @Title: parseStringArray
	 * @Description: 描述解析集合<String>类的JSONObject字段
	 * @author liujie
	 * @date 2011-11-29
	 * @return List<String>
	 * @throws
	 */
	private static List<String> parseStringArray(JSONArray array) throws JSONException {
		// TODO Auto-generated method stub
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < array.length(); i++) {
			String aa = "";
			try {
				aa = (String) array.get(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Debug.w("C35", "JsonExp", e);
				throw e;
			}
			result.add(aa);
		}
		return result;
	}

	/**
	 * @throws JSONException
	 * @Title: parseAttaArray
	 * @Description: 描述解析集合<Attachment>类的JSONObject字段
	 * @author liujie
	 * @date 2011-11-29
	 * @return List<Attachment>
	 * @throws
	 */
	private static List<C35Attachment> parseAttaArray(JSONArray array, String mailId, String sender, String subject, String folderId, String sendTime) throws JSONException {
		// TODO Auto-generated method stub
		if (array != null) {
			List<C35Attachment> result = new ArrayList<C35Attachment>();
			for (int i = 0; i < array.length(); i++) {
				C35Attachment attachmentInfo = null;
				try {
					JSONObject obj = array.getJSONObject(i);
					attachmentInfo = new C35Attachment();
					attachmentInfo.setMailId(mailId);
					attachmentInfo.setId(obj.getString("id"));
					attachmentInfo.setCid(obj.getString("cid"));
					attachmentInfo.setFileName(obj.getString("fileName"));
					attachmentInfo.setFileSize(obj.getLong("fileSize"));
					attachmentInfo.setCompressItemNum(obj.getInt("compressItemNum"));
					// added by zhuanggy 存入attachments_list用到以下字段
					attachmentInfo.setMailSubject(subject);
					attachmentInfo.setFromAddr(sender);
					attachmentInfo.setSendTime(TimeUtil.timeStringToLong(sendTime));
					attachmentInfo.setFolderId(folderId);
				} catch (JSONException e) {
					Debug.w("C35", "JsonExp", e);
					throw e;
				}
				result.add(attachmentInfo);
			}
			return result;
		}
		return null;
	}

	/**
	 * @throws JSONException
	 * @Title: parseLabelArray
	 * @Description: 描述解析集合<Label>类的JSONObject字段
	 * @author liujie
	 * @date 2011-11-29
	 * @return List<Label>
	 * @throws
	 */
	private static List<Label> parseLabelArray(JSONArray jsonArray) throws JSONException {
		// TODO Auto-generated method stub
		if (jsonArray != null) {
			List<Label> result = new ArrayList<Label>();
			for (int i = 0; i < jsonArray.length(); i++) {
				Label label = null;
				try {
					JSONObject obj = jsonArray.getJSONObject(i);
					label = new Label(obj.getString("labelId"), obj.getString("labelName"), obj.getString("labelColor"), obj.getLong("orderValue"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Debug.w("C35", "JsonExp", e);
					throw e;
				}
				result.add(label);
			}
			return result;
		}
		return null;
	}

	/**
	 * 把对象封装为JSON格式
	 * 
	 * @param o
	 *            对象
	 * @return JSON格式
	 */
	@SuppressWarnings("unchecked")
	public static String toJson(final Object o, boolean getSuper) {
		if (o == null) {
			return "null";
		}
		if (o instanceof String) // String
		{
			return string2Json((String) o);
		}
		if (o instanceof Boolean) // Boolean
		{
			return boolean2Json((Boolean) o);
		}
		if (o instanceof Number) // Number
		{
			return number2Json((Number) o);
		}
		if (o instanceof Map) // Map
		{
			return map2Json((Map<String, Object>) o);
		}
		if (o instanceof Collection) // List Set
		{
			return collection2Json((Collection) o);
		}
		if (o instanceof Object[]) // 对象数组
		{
			return array2Json((Object[]) o);
		}
		if (o instanceof int[])// 基本类型数组
		{
			return intArray2Json((int[]) o);
		}
		if (o instanceof boolean[])// 基本类型数组
		{
			return booleanArray2Json((boolean[]) o);
		}
		if (o instanceof long[])// 基本类型数组
		{
			return longArray2Json((long[]) o);
		}
		if (o instanceof float[])// 基本类型数组
		{
			return floatArray2Json((float[]) o);
		}
		if (o instanceof double[])// 基本类型数组
		{
			return doubleArray2Json((double[]) o);
		}
		if (o instanceof short[])// 基本类型数组
		{
			return shortArray2Json((short[]) o);
		}
		if (o instanceof byte[])// 基本类型数组
		{
			return byteArray2Json((byte[]) o);
		}
		if (o instanceof Object) // 保底收尾对象
		{
			return object2Json(o, getSuper);
		}
		throw new RuntimeException("不支持的类型: " + o.getClass().getName());
	}

	/**
	 * 将 String 对象编码为 JSON格式，只需处理好特殊字符
	 * 
	 * @param s
	 *            String 对象
	 * @return JSON格式
	 */
	static String string2Json(final String s) {
		final StringBuilder sb = new StringBuilder(s.length() + 20);
		sb.append('\"');
		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			switch (c) {
			case '\"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '/':
				sb.append("\\/");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			default:
				sb.append(c);
			}
		}
		sb.append('\"');
		return sb.toString();
	}

	/**
	 * 将 Number 表示为 JSON格式
	 * 
	 * @param number
	 *            Number
	 * @return JSON格式
	 */
	static String number2Json(final Number number) {
		return number.toString();
	}

	/**
	 * 将 Boolean 表示为 JSON格式
	 * 
	 * @param bool
	 *            Boolean
	 * @return JSON格式
	 */
	static String boolean2Json(final Boolean bool) {
		return bool.toString();
	}

	/**
	 * 将 Collection 编码为 JSON 格式 (List,Set)
	 * 
	 * @param c
	 * @return
	 */
	static String collection2Json(final Collection<Object> c) {
		final Object[] arrObj = c.toArray();
		return toJson(arrObj, true);
	}

	/**
	 * 将 Map<String, Object> 编码为 JSON 格式
	 * 
	 * @param map
	 * @return
	 */
	static String map2Json(final Map<String, Object> map) {
		if (map.isEmpty()) {
			return "{}";
		}
		final StringBuilder sb = new StringBuilder(map.size() << 4); // 4次方
		sb.append('{');
		final Set<String> keys = map.keySet();
		for (final String key : keys) {
			final Object value = map.get(key);
			sb.append('\"');
			sb.append(key); // 不能包含特殊字符
			sb.append('\"');
			sb.append(':');
			sb.append(toJson(value, true)); // 循环引用的对象会引发无限递归
			sb.append(',');
		}
		// 将最后的 ',' 变为 '}':
		sb.setCharAt(sb.length() - 1, '}');
		return sb.toString();
	}

	/**
	 * 将数组编码为 JSON 格式
	 * 
	 * @param array
	 *            数组
	 * @return JSON 格式
	 */
	static String array2Json(final Object[] array) {
		if (array.length == 0) {
			return "[]";
		}
		final StringBuilder sb = new StringBuilder(array.length << 4); // 4次方
		sb.append('[');
		for (final Object o : array) {
			sb.append(toJson(o, true));
			sb.append(',');
		}
		// 将最后添加的 ',' 变为 ']':
		sb.setCharAt(sb.length() - 1, ']');
		return sb.toString();
	}

	static String intArray2Json(final int[] array) {
		if (array.length == 0) {
			return "[]";
		}
		final StringBuilder sb = new StringBuilder(array.length << 4);
		sb.append('[');
		for (final int o : array) {
			sb.append(Integer.toString(o));
			sb.append(',');
		}
		// set last ',' to ']':
		sb.setCharAt(sb.length() - 1, ']');
		return sb.toString();
	}

	static String longArray2Json(final long[] array) {
		if (array.length == 0) {
			return "[]";
		}
		final StringBuilder sb = new StringBuilder(array.length << 4);
		sb.append('[');
		for (final long o : array) {
			sb.append(Long.toString(o));
			sb.append(',');
		}
		// set last ',' to ']':
		sb.setCharAt(sb.length() - 1, ']');
		return sb.toString();
	}

	static String booleanArray2Json(final boolean[] array) {
		if (array.length == 0) {
			return "[]";
		}
		final StringBuilder sb = new StringBuilder(array.length << 4);
		sb.append('[');
		for (final boolean o : array) {
			sb.append(Boolean.toString(o));
			sb.append(',');
		}
		// set last ',' to ']':
		sb.setCharAt(sb.length() - 1, ']');
		return sb.toString();
	}

	static String floatArray2Json(final float[] array) {
		if (array.length == 0) {
			return "[]";
		}
		final StringBuilder sb = new StringBuilder(array.length << 4);
		sb.append('[');
		for (final float o : array) {
			sb.append(Float.toString(o));
			sb.append(',');
		}
		// set last ',' to ']':
		sb.setCharAt(sb.length() - 1, ']');
		return sb.toString();
	}

	static String doubleArray2Json(final double[] array) {
		if (array.length == 0) {
			return "[]";
		}
		final StringBuilder sb = new StringBuilder(array.length << 4);
		sb.append('[');
		for (final double o : array) {
			sb.append(Double.toString(o));
			sb.append(',');
		}
		// set last ',' to ']':
		sb.setCharAt(sb.length() - 1, ']');
		return sb.toString();
	}

	static String shortArray2Json(final short[] array) {
		if (array.length == 0) {
			return "[]";
		}
		final StringBuilder sb = new StringBuilder(array.length << 4);
		sb.append('[');
		for (final short o : array) {
			sb.append(Short.toString(o));
			sb.append(',');
		}
		// set last ',' to ']':
		sb.setCharAt(sb.length() - 1, ']');
		return sb.toString();
	}

	static String byteArray2Json(final byte[] array) {
		if (array.length == 0) {
			return "[]";
		}
		final StringBuilder sb = new StringBuilder(array.length << 4);
		sb.append('[');
		for (final byte o : array) {
			sb.append(Byte.toString(o));
			sb.append(',');
		}
		// set last ',' to ']':
		sb.setCharAt(sb.length() - 1, ']');
		return sb.toString();
	}

	public static String object2Json(final Object bean, boolean getSuper) {
		// 数据检查
		if (bean == null) {
			return "{}";
		}
		final Method[] methods = getSuper ? bean.getClass().getMethods() : bean.getClass().getDeclaredMethods(); // 方法数组
		final StringBuilder sb = new StringBuilder(methods.length << 4); // 4次方
		sb.append('{');
		for (final Method method : methods) {
			try {
				final String name = method.getName();
				String key = "";
				if (name.startsWith("get")) {
					key = name.substring(3);
					// 防死循环
					final String[] arrs = { "Class" };
					boolean bl = false;
					for (final String s : arrs) {
						if (s.equals(key)) {
							bl = true;
							continue;
						}
					}
					if (bl) {
						continue; // 防死循环
					}
				} else if (name.startsWith("is")) {
					key = name.substring(2);
				}
				if (key.length() > 0 && Character.isUpperCase(key.charAt(0)) && method.getParameterTypes().length == 0) {
					if (key.length() == 1) {
						key = key.toLowerCase();
					} else if (!Character.isUpperCase(key.charAt(1))) {
						key = key.substring(0, 1).toLowerCase() + key.substring(1);
					}
					final Object elementObj = method.invoke(bean);
					sb.append('\"');
					sb.append(key); // 不能包含特殊字符
					sb.append('\"');
					sb.append(':');
					sb.append(toJson(elementObj, getSuper)); // 循环引用的对象会引发无限递归
					sb.append(',');
				}
			} catch (final Exception e) {
				// e.getMessage();
				throw new RuntimeException("在将bean封装成JSON格式时异常：" + e.getMessage(), e);
			}
		}
		if (sb.length() == 1) {
			return bean.toString();
		} else {
			sb.setCharAt(sb.length() - 1, '}');
			return sb.toString();
		}
	}

	private JsonUtil() {
	}

	/**
	 * @throws JSONException
	 * @Title: parseFolders
	 * @Description: 描述 拿箱子的解析GetFolderListResponse
	 * @author liujie
	 * @date 2011-11-29
	 * @return ArrayList<C35Folder>
	 * @throws
	 */
	public static ArrayList<C35Folder> parseFolders(JSONObject jsonObject) throws JSONException {
		// TODO Auto-generated method stub
		ArrayList<C35Folder> folders = null;
		try {
			JSONArray array = jsonObject.getJSONArray("folderList");
			folders = new ArrayList<C35Folder>();
			for (int i = 0; i < array.length(); i++) {
				JSONObject resultObj = array.getJSONObject(i);
				C35Folder folder = new C35Folder();
				folder.setParentId(resultObj.getString("parentId"));
				folder.setOrderValue(resultObj.getLong("orderValue"));
				folder.setFolderName(resultObj.getString("folderName"));
				folder.setFolderType(resultObj.getInt("folderType"));
				folder.setFolderId(resultObj.getString("folderId"));
				Debug.i(TAG, "parentId:" + folder.getParentId() + "|orderValue:" + folder.getOrderValue() + "|folderName:" + folder.getFolderName() + "|foldertype:" + folder.getFolderType() + "|forderid:" + folder.getFolderId());
				folders.add(folder);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Debug.w("C35", "JsonExp", e);
			throw e;
		}
		return folders;
	}

	public static C35Message parseMessageInfo(JSONObject jsonObj) throws JSONException {
		// TODO Auto-generated method stub
		C35Message message = new C35Message();
		try {
			message.setMailId(jsonObj.getString("mailId"));
			message.setFrom(jsonObj.getString("from"));
			message.setSendTime(jsonObj.getString("sendTime"));
			message.setTo(parseStringArray(jsonObj.getJSONArray("to")));
			message.setCc(parseStringArray(jsonObj.getJSONArray("cc")));
			message.setBcc(parseStringArray(jsonObj.getJSONArray("bcc")));
			message.setSubject(jsonObj.getString("subject"));
			message.setAttachSize(Integer.parseInt((jsonObj.getString("attachSize"))));
			message.setSize(jsonObj.getInt("size"));
			message.setPreview(jsonObj.getString("preview"));
			message.setRead(0);
		} catch (JSONException e) {
			Debug.w("C35", "JsonExp", e);
			throw e;
		}
		return message;
	}

	/**
	 * @Description:压缩条目列表
	 * @param array
	 * @param mailId
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @throws JSONException
	 * @date:2012-8-21
	 */

	private static List<C35CompressItem> parseCompressItemArray(JSONArray array) throws JSONException {
		if (array != null) {
			List<C35CompressItem> result = new ArrayList<C35CompressItem>();
			for (int i = 0; i < array.length(); i++) {
				C35CompressItem attachmentInfo = null;
				try {
					JSONObject obj = array.getJSONObject(i);
					attachmentInfo = new C35CompressItem();
					attachmentInfo.setAttachId(obj.getString("attachId"));
					attachmentInfo.setFileName(obj.getString("fileName"));
					attachmentInfo.setFileSize(obj.getLong("fileSize"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Debug.w("C35", "JsonExp", e);
					throw e;
				}
				result.add(attachmentInfo);
			}
			return result;
		}
		return null;
	}

	/**
	 * 
	 * @Description:获取失败对象
	 * @param jsonObject
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @throws JSONException
	 * @date:2012-10-18
	 */
	public static List<ErrorObj> parseErrorObjs(JSONObject jsonObject) throws JSONException {
		List<ErrorObj> errorObjs = new ArrayList<ErrorObj>();
		try {
			JSONArray array = jsonObject.getJSONArray("errorObjs");
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					JSONObject resultObj = array.getJSONObject(i);
					ErrorObj errorObj = new ErrorObj();
					errorObj.setFolderId(resultObj.getString("folderId"));
					errorObj.setMailId(resultObj.getString("mailId"));
					errorObj.setErrorCode(resultObj.getInt("errorCode"));
					errorObj.setStatusId(resultObj.getInt("statusId"));
					errorObjs.add(errorObj);
				}
			}

		} catch (JSONException e) {
			Debug.w("C35", "JsonExp", e);
			throw e;
		}
		return errorObjs;
	}

	/**
	 * 
	 * @Description:
	 * @param jsonObject
	 * @return
	 * @throws JSONException
	 * @see:
	 * @since:
	 * @author: wennan
	 * @date:2013-5-30
	 */
	public static List<Contact> parseContacts(JSONObject jsonObject) throws JSONException {
		List<Contact> contacts = new ArrayList<Contact>();
		try {
			JSONArray array = jsonObject.getJSONArray("contacts");
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					JSONObject resultObj = array.getJSONObject(i);
					Contact contact = new Contact();
					contact.setEmail(resultObj.getString("email"));
					contact.setName(resultObj.getString("name"));
					contacts.add(contact);
				}
			}

		} catch (JSONException e) {
			Debug.w("C35", "JsonExp", e);
			throw e;
		}
		return contacts;
	}

	/**
	 * 
	 * @Description:获取邮件状态对象 ，如果邮件被删除或移动就没有这条的返回值
	 * @param jsonObject
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @throws JSONException
	 * @date:2012-10-18
	 */
	public static List<MailStatusObj> parseMailStatusObjs(JSONObject jsonObject) throws JSONException {
		List<MailStatusObj> mailStatusObjs = new ArrayList<MailStatusObj>();
		try {
			JSONArray array = jsonObject.getJSONArray("mailStatusObjs");
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					JSONObject resultObj = array.getJSONObject(i);
					MailStatusObj mailStatusObj = new MailStatusObj();
					mailStatusObj.setMailId(resultObj.getString("mailId"));
					mailStatusObj.setRead(resultObj.getInt("read"));
					mailStatusObj.setImportantFlag(resultObj.getInt("importantFlag"));
					mailStatusObj.setCalendarState(resultObj.getString("calendarState"));
					if (resultObj.has("deliveredReadCount")) {
						mailStatusObj.setDeliveredReadCount(resultObj.getInt("deliveredReadCount"));
						mailStatusObj.setDeliveredReadUsers(parseStringArray(resultObj.getJSONArray("deliveredReadUsers")));
					}

					mailStatusObjs.add(mailStatusObj);
				}
			}

		} catch (JSONException e) {
			Debug.w("C35", "JsonExp", e);
			throw e;
		}
		return mailStatusObjs;
	}

	/**
	 * 获取附件列表时，解析附件对象
	 * 
	 * @Description:
	 * @param jsonObj
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @throws JSONException
	 * @date:2013-1-6
	 */
	public static C35Attachment parseAttachmentInfo(JSONObject jsonObj) throws JSONException {
		// TODO Auto-generated method stub
		C35Attachment attachment = new C35Attachment();
		try {
			attachment.setId(jsonObj.getString("id"));
			attachment.setFileName(jsonObj.getString("fileName"));
			attachment.setFileSize(jsonObj.getInt("fileSize"));
			attachment.setMailId(jsonObj.getString("mailId"));
			// sessionMailId String 会话id
			attachment.setMailSubject(jsonObj.getString("subject"));
			attachment.setFolderId(jsonObj.getString("folderId"));
			attachment.setFromAddr(jsonObj.getString("from"));
			attachment.setSendTime(TimeUtil.timeStringToLong(jsonObj.getString("sendTime")));

		} catch (JSONException e) {
			Debug.w("C35", "JsonExp", e);
			throw e;
		}
		return attachment;
	}

	/**
	 * 获取附件列表Id时，解析附件对象
	 * 
	 * @Description:
	 * @param jsonObj
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @throws JSONException
	 * @date:2013-1-6
	 */
	public static String parseAttachmentInfoId(JSONObject jsonObj) throws JSONException {
		String id = "";
		try {
			id = jsonObj.getString("id");
		} catch (JSONException e) {
			Debug.w("C35", "JsonExp", e);
			throw e;
		}
		return id;
	}
}
