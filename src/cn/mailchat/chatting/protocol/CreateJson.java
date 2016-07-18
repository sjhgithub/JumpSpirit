package cn.mailchat.chatting.protocol;

import java.util.List;
import java.util.UUID;

import org.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;
import android.util.Log;

import cn.mailchat.chatting.beans.CAttachment;
import cn.mailchat.chatting.beans.CMessage;
import cn.mailchat.chatting.beans.DAttachment;
import cn.mailchat.chatting.beans.DChatMessage;
import cn.mailchat.chatting.beans.CMessage.Type;
import cn.mailchat.helper.Utility;
import cn.mailchat.contacts.beans.ContactAttribute;

public class CreateJson {
	/*-------------------------------华丽的分割线：MQTT对应客户端信息封装---------------------------------*/
	/**
	 * 单聊数据封装Json 备注: f(from):来自谁, m(message):消息{ c(content) 内容 ,t(type) 类型} 格式:
	 * {"f":"email@35.cn","m":{"c":[根据消息类型,扩展需要的内容] ,"t":0 }}
	 * 
	 * @Description:
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-9-26
	 */
	public static JSONObject createDmessage(DChatMessage dChatMessage) {
		JSONObject json = new JSONObject();
		try {
			json.put("f", dChatMessage.getSenderEmail());
			JSONObject messageJson = new JSONObject();
			JSONArray messageContentJson = new JSONArray();
			DAttachment dAttachment = null;
			switch (dChatMessage.getMessageType()) {
			case TEXT:
				messageContentJson.put(dChatMessage.getMessageContent());
				messageContentJson.put(dChatMessage.getUuid());
				break;
			case IMAGE:
				dAttachment = dChatMessage.getAttachments().get(0);
				messageContentJson.put(dAttachment.getName());
				messageContentJson.put(dAttachment.getFileid());
				messageContentJson.put(dAttachment.getSize());
				messageContentJson.put(dChatMessage.getUuid());
				// imageUI尺寸需要，扩展添加
				messageContentJson.put(dAttachment.getImageWidth() + "*"
						+ dAttachment.getImageHeight());
				break;
			case ATTACHMENT:
				dAttachment = dChatMessage.getAttachments().get(0);
				messageContentJson.put(dAttachment.getName());
				messageContentJson.put(dAttachment.getFileid());
				messageContentJson.put(dAttachment.getSize());
				messageContentJson.put(dChatMessage.getUuid());
				break;
			case VOICE:
				dAttachment = dChatMessage.getAttachments().get(0);
				messageContentJson.put(Base64.encodeToString(
						dAttachment.getFileByte(), Base64.DEFAULT));
				messageContentJson.put(dAttachment.getVoiceLength());
				messageContentJson.put(dChatMessage.getUuid());
				break;
			case LOCATION:
				break;
			case NOTIFICATION:
				break;
			case FROM_MAIL_INFO:
				messageContentJson.put(dChatMessage.getMailFrom());
				messageContentJson.put(dChatMessage.getMailSubject());
				messageContentJson.put(dChatMessage.getMailPreview());
				messageContentJson.put(dChatMessage.getUuid());
				break;
			default:
				break;
			}
			messageJson.put("c", messageContentJson);
			messageJson.put("t", dChatMessage.getMessageType().ordinal());
			json.put("m", messageJson);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	/**
	 * 创建群邀请信息封装Json 备注: f(from):来自谁, m(message):消息{ c(content) 内容 ,t(type)
	 * MQTT系统消息类型} 格式: {"f":"email@35.cn","m":{"c":[根据消息类型,扩展需要的内容] ,"t":0 }}
	 * 
	 * @Description:
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-16
	 */
	public static JSONObject invitationMessage(String email,
			String memberEmail, String subscribeCGroupID, String messageUid) {
		JSONObject json = new JSONObject();
		try {
			json.put("f", email);
			JSONObject messageJSONObject = new JSONObject();
			JSONArray messageContentJSONArray = new JSONArray();
			messageContentJSONArray.put(subscribeCGroupID);
			messageContentJSONArray.put(messageUid);// IOS判断重复消息使用,后期安卓也可以修改
			messageJSONObject.put("c", messageContentJSONArray);
			messageJSONObject
					.put("t", MQTTMessageType.SYSTEM_JOINGROUP_MESSAGE);
			json.put("m", messageJSONObject);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	/**
	 * 群聊数据封装Json 备注: f(from):来自谁, m(message):消息{ c(content) 内容 ,t(type) 类型} 格式:
	 * {"f":"email@35.cn","m":{"c":[根据消息类型,扩展需要的内容] ,"t":0 }}
	 * 
	 * @Description:
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-9-26
	 */
	public static JSONObject createCmessage(CMessage cMessage) {
		JSONObject json = new JSONObject();
		try {
			json.put("f", cMessage.getMember().getEmail());
			JSONObject messageJSONObject = new JSONObject();
			JSONArray messageContentJSONArray = new JSONArray();
			CAttachment cAttachment = null;
			switch (cMessage.getMessageType()) {
			case TEXT:
				messageContentJSONArray.put(cMessage.getContent());
				messageContentJSONArray.put(cMessage.getUid());
				break;
			case IMAGE:
				cAttachment = cMessage.getAttachment();
				messageContentJSONArray.put(cAttachment.getName());
				messageContentJSONArray.put(cAttachment.getFileid());
				messageContentJSONArray.put(cAttachment.getSize());
				messageContentJSONArray.put(cMessage.getUid());
				// imageUI尺寸需要，扩展添加
				messageContentJSONArray.put(cAttachment.getImageWidth() + "*"
						+ cAttachment.getImageHeight());
				break;
			case ATTACHMENT:
				cAttachment = cMessage.getAttachment();
				messageContentJSONArray.put(cAttachment.getName());
				messageContentJSONArray.put(cAttachment.getFileid());
				messageContentJSONArray.put(cAttachment.getSize());
				messageContentJSONArray.put(cMessage.getUid());
				break;
			case VOICE:
				cAttachment = cMessage.getAttachment();
				messageContentJSONArray.put(Base64.encodeToString(
						cAttachment.getFileByte(), Base64.DEFAULT));
				messageContentJSONArray.put(cAttachment.getVoiceLength());
				messageContentJSONArray.put(cMessage.getUid());
				break;
			case LOCATION:
				break;
			case NOTIFICATION:
				messageContentJSONArray.put(cMessage.getContent());
				messageContentJSONArray.put(cMessage.getUid());
				break;
			case FROM_MAIL_INFO:
				messageContentJSONArray.put(cMessage.getMailFrom());
				messageContentJSONArray.put(cMessage.getMailSubject());
				messageContentJSONArray.put(cMessage.getMailPreview());
				messageContentJSONArray.put(cMessage.getUid());
				break;
			default:
				break;
			}
			messageJSONObject.put("c", messageContentJSONArray);
			messageJSONObject.put("t", cMessage.getMessageType().ordinal());
			json.put("m", messageJSONObject);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	/**
	 * 删除群信息封装Json 备注: f(from):来自谁, m(message):消息{ c(content) 内容 ,t(type)
	 * MQTT系统消息类型} 格式: {"f":"email@35.cn","m":{"c":[根据消息类型,扩展需要的内容] ,"t":0 }}
	 * 
	 * @Description:
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-16
	 */
	public static JSONObject deleteCGroupMessage(String email,
			String unsubscribeCGroupID, String messageUid) {
		JSONObject json = new JSONObject();
		try {
			json.put("f", email);
			JSONObject messageJSONObject = new JSONObject();
			JSONArray messageContentJSONArray = new JSONArray();
			messageContentJSONArray.put(unsubscribeCGroupID);
			messageContentJSONArray.put(messageUid);// IOS判断重复消息使用,后期安卓也可以修改
			messageJSONObject.put("c", messageContentJSONArray);
			messageJSONObject.put("t",
					MQTTMessageType.SYSTEM_DELETEGROUP_MESSAGE);
			json.put("m", messageJSONObject);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	/**
	 * 踢出群信息封装Json 备注: f(from):来自谁, m(message):消息{ c(content) 内容 ,t(type)
	 * MQTT系统消息类型} 格式: {"f":"email@35.cn","m":{"c":[根据消息类型,扩展需要的内容] ,"t":0 }}
	 * 
	 * @Description:
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-16
	 */
	public static JSONObject kickedOutMemberMessage(String email,
			String unsubscribeCGroupID, String messageUid) {
		JSONObject json = new JSONObject();
		try {
			json.put("f", email);
			JSONObject messageJSONObject = new JSONObject();
			JSONArray messageContentJSONArray = new JSONArray();
			messageContentJSONArray.put(unsubscribeCGroupID);
			messageContentJSONArray.put(messageUid);// IOS判断重复消息使用,后期安卓也可以修改
			messageJSONObject.put("c", messageContentJSONArray);
			messageJSONObject.put("t",
					MQTTMessageType.SYSTEM_KICKED_OUT_GROUPR_MESSAGE);
			json.put("m", messageJSONObject);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	/**
	 * 踢出群信息封装Json,用于通知其他群成员删除该成员数据 备注: f(from):来自谁, m(message):消息{ c(content)
	 * 内容 ,t(type) MQTT系统消息类型} 格式: {"f":"email@35.cn","m":{"c":[根据消息类型,扩展需要的内容]
	 * ,"t":0 }}
	 * 
	 * @Description:
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-16
	 */
	public static JSONObject kickedOutByAntherMemberMessage(String email,
			String unsubscribeCGroupID, String kickedOutEmail, String messageUid) {
		JSONObject json = new JSONObject();
		try {
			json.put("f", email);
			JSONObject messageJSONObject = new JSONObject();
			JSONArray messageContentJSONArray = new JSONArray();
			messageContentJSONArray.put(unsubscribeCGroupID);
			messageContentJSONArray.put(kickedOutEmail);
			messageContentJSONArray.put(messageUid);// IOS判断重复消息使用,后期安卓也可以修改
			messageJSONObject.put("c", messageContentJSONArray);
			messageJSONObject.put("t",
					MQTTMessageType.SYSTEM_KICKED_OUT_MEMBER_MESSAGE);
			json.put("m", messageJSONObject);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	/**
	 * 退出群信息封装Json 备注: f(from):来自谁, m(message):消息{ c(content) 内容 ,t(type)
	 * MQTT系统消息类型} 格式: {"f":"email@35.cn","m":{"c":[根据消息类型,扩展需要的内容] ,"t":0 }}
	 * 
	 * @Description:
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-16
	 */
	public static JSONObject leaveCGroupMessage(String email,
			String subscribeCGroupID, String messageUid) {
		JSONObject json = new JSONObject();
		try {
			json.put("f", email);
			JSONObject messageJSONObject = new JSONObject();
			JSONArray messageContentJSONArray = new JSONArray();
			messageContentJSONArray.put(subscribeCGroupID);
			messageContentJSONArray.put(messageUid);// IOS判断重复消息使用,后期安卓也可以修改
			messageJSONObject.put("c", messageContentJSONArray);
			messageJSONObject.put("t",
					MQTTMessageType.SYSTEM_LEAVEGROUP_MESSAGE);
			json.put("m", messageJSONObject);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	/**
	 * 修改群名称信息封装Json 备注: f(from):来自谁, m(message):消息{ c(content) 内容 ,t(type)
	 * MQTT系统消息类型} 格式: {"f":"email@35.cn","m":{"c":[根据消息类型,扩展需要的内容] ,"t":0 }}
	 * 
	 * @Description:
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-26
	 */
	public static JSONObject cGroupReNameMessage(String email,
			String subscribeCGroupID, String reName, String messageUid) {
		JSONObject json = new JSONObject();
		try {
			json.put("f", email);
			JSONObject messageJSONObject = new JSONObject();
			JSONArray messageContentJSONArray = new JSONArray();
			messageContentJSONArray.put(subscribeCGroupID);
			messageContentJSONArray.put(reName);
			messageContentJSONArray.put(messageUid);// IOS判断重复消息使用,后期安卓也可以修改
			messageJSONObject.put("c", messageContentJSONArray);
			messageJSONObject.put("t",
					MQTTMessageType.SYSTEM_GROUPRENAME_MESSAGE);
			json.put("m", messageJSONObject);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	/**
	 * 
	 * method name: createSyncUserInfo function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param allContacts
	 * @return field_name JSONObject return type
	 * @History memory：
	 * @Date：2015-4-22 下午1:21:38 @Modified by：zhangjx
	 * @Description：请求服务端用户头像有编号的信息
	 */
	public synchronized static JSONObject createSyncUserInfo(
			List<ContactAttribute> allContacts) {
			JSONObject jsonObject = new JSONObject();
			try {
				if (allContacts != null && allContacts.size() > 0) {
					JSONArray array = new JSONArray();
					for (int i = 0; i < allContacts.size(); i++) {
						JSONObject json = new JSONObject();
						ContactAttribute contactAttribute = allContacts.get(i);
						if (contactAttribute != null) {
							json.put("e", contactAttribute.getEmail());
							json.put("a", contactAttribute.getImgHeadHash());
						}
						array.put(json);
					}
					jsonObject.put("data", array);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		return jsonObject;
	}

	/**
	 * 
	 * method name: sendUserMessageToHelpAccount function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param topic
	 * @param email
	 * @param uuid
	 * @param clientVersion
	 * @return field_name JSONObject return type
	 * @History memory：
	 * @Date：2016-3-16 下午3:38:59 @Modified by：zhangjx
	 * @Description：登录页面发起小助手聊天，主动发一条消息给小助手，服务端截取该1002消息，回复
	 */
	public static JSONObject sendUserMessageToHelpAccount(String topic,
			String email, String uuid, String clientVersion) {
		JSONObject json = new JSONObject();
		try {
			json.put("f", topic);
			JSONObject messageJSONObject = new JSONObject();
			JSONArray messageContentJSONArray = new JSONArray();
			messageContentJSONArray.put(uuid);
			messageContentJSONArray.put(email);
			messageContentJSONArray.put("android");
			messageContentJSONArray.put(clientVersion);
			messageJSONObject.put("c", messageContentJSONArray);
			messageJSONObject.put("t",
					MQTTMessageType.SEND_USER_MESSAGE_TO_HELP_ACCOUNT);
			json.put("m", messageJSONObject);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
}
