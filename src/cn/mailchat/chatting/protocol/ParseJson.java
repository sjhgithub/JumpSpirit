package cn.mailchat.chatting.protocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;
import android.util.Log;

import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.activity.misc.Attachment;
import cn.mailchat.beans.Eis35Bean;
import cn.mailchat.beans.ImapAndSmtpSetting;
import cn.mailchat.chatting.beans.CAttachment;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.chatting.beans.CMessage;
import cn.mailchat.chatting.beans.DAttachment;
import cn.mailchat.chatting.beans.DChatMessage;
import cn.mailchat.chatting.beans.DChatMessage.Type;
import cn.mailchat.chatting.beans.PendingHTTPSCommand;
import cn.mailchat.chatting.beans.PendingMQTTConmmand;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.utils.EncryptUtil;
import cn.mailchat.utils.StringUtil;

public class ParseJson {
	/*-------------------------------华丽的分割线：MQTT对应客户端的消息解析---------------------------------*/
	/**
	 * 解析单聊消息
	 * 备注: f(from):来自谁, m(message):消息{ c(content) 内容 ,t(type) 类型},(mt)publishMQTTMessageType:MQTT类型 
	 * 
	 * @Description:
	 * @param dChatMessagejson
	 * @see:
	 * @since:
	 * @return DChatMessage 当为null时，即还未支持类型消息，界面不做处理。
	 * @author: shengli
	 * @date:2014-9-26
	 */
	public static DChatMessage parseDmessage(JSONObject dChatMessagejson,String receiverEmail) {
		DChatMessage dmessage = new DChatMessage();
		try {
			JSONObject  messageJSONObject = dChatMessagejson.getJSONObject("m");
			JSONArray  messageContentJSONArray = messageJSONObject.getJSONArray("c");
			if (messageJSONObject.getInt("t")==1002) {
				return null;
			}
			dmessage.setMessageType(DChatMessage.Type.values()[messageJSONObject.getInt("t")]);
			dmessage.setUuid(UUID.randomUUID().toString());
			dmessage.setSenderEmail(dChatMessagejson.getString("f"));
			dmessage.setReceiverEmail(receiverEmail);
			DAttachment dAttachment =null;
			List<DAttachment> dAttachments =null;
			//服务器push消息类型解析
			int serverMessageType =0;
			if(!messageJSONObject.isNull("s")){
				serverMessageType=messageJSONObject.getInt("s");
			}
			switch (dmessage.getMessageType()) {
			case TEXT:
				dmessage.setMessageContent(messageContentJSONArray.getString(0));
				if(!messageContentJSONArray.isNull(1)){
					dmessage.setUuid(messageContentJSONArray.getString(1));
				}
				if(serverMessageType!=0){
					dmessage.setServerMessageType(serverMessageType);
				}
				break;
			case IMAGE:
				dAttachment =new DAttachment();
				dAttachments=new ArrayList<DAttachment>();
				dAttachment.setAttchmentId(UUID.randomUUID().toString());
				dAttachment.setName(messageContentJSONArray.getString(0));
				dAttachment.setFileid(messageContentJSONArray.getString(1));
				dAttachment.setSize(messageContentJSONArray.getLong(2));
				dAttachments.add(dAttachment);
				dmessage.setAttachments(dAttachments);
				if(!messageContentJSONArray.isNull(3)){
					dmessage.setUuid(messageContentJSONArray.getString(3));
				}
				dAttachment.setMessageUid(dmessage.getUuid());
				if(!messageContentJSONArray.isNull(4)){
					String imageSize = messageContentJSONArray.getString(4);
					if(imageSize.indexOf("*") != -1){
						String[] size = imageSize.split("\\*");
						//由于IOS端发送的是float浮点型
						dAttachment.setImageWidth((int)Float.parseFloat(size[0]));
						dAttachment.setImageHeight((int)Float.parseFloat(size[1]));
					}
				}
				break;
			case ATTACHMENT:
				dAttachment =new DAttachment();
				dAttachments=new ArrayList<DAttachment>();
				dAttachment.setAttchmentId(UUID.randomUUID().toString());
				dAttachment.setName(messageContentJSONArray.getString(0));
				dAttachment.setFileid(messageContentJSONArray.getString(1));
				dAttachment.setSize(messageContentJSONArray.getLong(2));
				dAttachments.add(dAttachment);
				dmessage.setAttachments(dAttachments);
				if(!messageContentJSONArray.isNull(3)){
					dmessage.setUuid(messageContentJSONArray.getString(3));
				}
				dAttachment.setMessageUid(dmessage.getUuid());
				break;
			case VOICE:
				dAttachment =new DAttachment();
				dAttachments=new ArrayList<DAttachment>();
				dAttachment.setAttchmentId(UUID.randomUUID().toString());
				dAttachment.setFileByte(Base64.decode(messageContentJSONArray.getString(0), Base64.DEFAULT));
				dAttachment.setVoiceLength(messageContentJSONArray.getInt(1));
				if(!messageContentJSONArray.isNull(2)){
					dmessage.setUuid(messageContentJSONArray.getString(2));
				}
				dAttachment.setMessageUid(dmessage.getUuid());
				dAttachments.add(dAttachment);
				dmessage.setAttachments(dAttachments);
				break;
			case LOCATION:
				return null;
			case NOTIFICATION:
				return null;
			case FROM_MAIL_INFO:
				dmessage.setMailFrom(messageContentJSONArray.getString(0));
				dmessage.setMailSubject(messageContentJSONArray.getString(1));
				dmessage.setMailPreview(messageContentJSONArray.getString(2));
				if(!messageContentJSONArray.isNull(3)){
					dmessage.setUuid(messageContentJSONArray.getString(3));
				}
				break;
			default:
				return null;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return dmessage;
	}
	
	/**
	 * 解析群组消息
	 * 备注: f(from):来自谁, m(message):消息{ c(content) 内容 ,t(type) 类型},(mt)publishMQTTMessageType:MQTT类型  
	 * 		  
	 * @Description:
	 * @param groupjson
	 * @see:
	 * @since:
	 * @return CMessage 当为null时，即还未支持类型消息，界面不做处理。
	 * @author: shengli
	 * @date:2014-9-26
	 */
	public static CMessage parseCMessage(JSONObject groupjson,String groupUid){
		CMessage cMessage=new CMessage();
		try {
			cMessage.setUid(UUID.randomUUID().toString());
			cMessage.setGroupUid(groupUid);		
			JSONObject  messageJSONObject  =	groupjson.getJSONObject("m");
			JSONArray messageContentJSONArray = messageJSONObject.getJSONArray("c");
			CMessage.Type type = CMessage.Type.values()[messageJSONObject.getInt("t")];
			cMessage.setMessageType(type);
			CAttachment cAttachment=null;
			int serverMessageType =0;
			if(!messageJSONObject.isNull("s")){
				serverMessageType=messageJSONObject.getInt("s");
			}
			switch (type) {
			case TEXT:
				cMessage.setContent(messageContentJSONArray.getString(0));
				if(!messageContentJSONArray.isNull(1)){
					cMessage.setUid(messageContentJSONArray.getString(1));
				}
				if(serverMessageType!=0){
					cMessage.setServerMessageType(serverMessageType);
				}
				break;
			case IMAGE:
				cAttachment =new CAttachment();
				cAttachment.setAttchmentId(UUID.randomUUID().toString());
				cAttachment.setName(messageContentJSONArray.getString(0));
				cAttachment.setFileid(messageContentJSONArray.getString(1));
				cAttachment.setSize(messageContentJSONArray.getLong(2));
				if(!messageContentJSONArray.isNull(3)){
					cMessage.setUid(messageContentJSONArray.getString(3));
				}
				cAttachment.setMessageUid(cMessage.getUid());
				if(!messageContentJSONArray.isNull(4)){
					String imageSize = messageContentJSONArray.getString(4);
					if(imageSize.indexOf("*") != -1){
						String[] size = imageSize.split("\\*");
						//由于IOS端发送的是float浮点型
						cAttachment.setImageWidth((int)Float.parseFloat(size[0]));
						cAttachment.setImageHeight((int)Float.parseFloat(size[1]));
					}
				}
				cMessage.setAttachment(cAttachment);
				break;
			case ATTACHMENT:
				cAttachment =new CAttachment();
				cAttachment.setAttchmentId(UUID.randomUUID().toString());
				cAttachment.setName(messageContentJSONArray.getString(0));
				cAttachment.setFileid(messageContentJSONArray.getString(1));
				cAttachment.setSize(messageContentJSONArray.getLong(2));
				if(!messageContentJSONArray.isNull(3)){
					cMessage.setUid(messageContentJSONArray.getString(3));
				}
				cAttachment.setMessageUid(cMessage.getUid());
				cMessage.setAttachment(cAttachment);
				break;
			case VOICE:
				cAttachment =new CAttachment();
				cAttachment.setAttchmentId(UUID.randomUUID().toString());
				cAttachment.setFileByte(Base64.decode(messageContentJSONArray.getString(0), Base64.DEFAULT));
				cAttachment.setVoiceLength(messageContentJSONArray.getInt(1));
				if(!messageContentJSONArray.isNull(2)){
					cMessage.setUid(messageContentJSONArray.getString(2));
				}
				cAttachment.setMessageUid(cMessage.getUid());
				cMessage.setAttachment(cAttachment);
				break;
			case LOCATION:
				return null;
			case NOTIFICATION:
				cMessage.setContent(messageContentJSONArray.getString(0));
				if(!messageContentJSONArray.isNull(1)){
					cMessage.setUid(messageContentJSONArray.getString(1));
				}
				break;
			case FROM_MAIL_INFO:
				cMessage.setMailFrom(messageContentJSONArray.getString(0));
				cMessage.setMailSubject(messageContentJSONArray.getString(1));
				cMessage.setMailPreview(messageContentJSONArray.getString(2));
				if(!messageContentJSONArray.isNull(3)){
					cMessage.setUid(messageContentJSONArray.getString(3));
				}
				break;
			default:
				return null;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cMessage;
	}

	/*-------------------------------华丽的分割线：HTTP服务端的解析---------------------------------*/
	/**
	 * 解析创建群组
	 * 
	 * @Description:
	 * @param groupjson
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-9-26
	 */
	public static CGroup createGroup(JSONObject groupjson){
		CGroup cGroup =new CGroup();
		try {
			cGroup.setUid(groupjson.getString("group_id"));
			cGroup.setGroupName(groupjson.getString("group_name"));		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cGroup;
	}
	
	/**
	 * 解析加入及获取群组
	 * 
	 * @Description:
	 * @param groupjson
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-9-26
	 */
	public static CGroup joinAndGetGroup(String accountEmail, JSONObject groupjson){
		
		CGroup cGroup =new CGroup();
		try {
			JSONObject dataJson = groupjson.getJSONObject("data");
			cGroup.setUid(dataJson.getString("id"));
			cGroup.setGroupName(dataJson.getString("name"));
			cGroup.setLastSendDate(dataJson.getLong("createtime")*1000);
			String creatorEmail=dataJson.getString("creator");
			//是否为第一次修改名称
			if(!dataJson.isNull("rename")){
				cGroup.setReName(true);
			}else{
				cGroup.setReName(false);
			}
			if(accountEmail.equals(creatorEmail)) {
				cGroup.setIsAdmin(true);
			} else {
				cGroup.setIsAdmin(false);
			}
			//已经加入的邮箱
			JSONArray membersJson = groupjson.getJSONArray("members");
			List<CGroupMember> members = new ArrayList<CGroupMember>();
			for(int i = 0; i < membersJson.length(); i++){
				JSONObject memberInfoJson = membersJson.getJSONObject(i);
				CGroupMember member =new CGroupMember();
				String email =memberInfoJson.getString("email");
				member.setUid(EncryptUtil.getMd5(email));
				member.setEmail(email);
				member.setInviteMember(false);
				if(!memberInfoJson.isNull("name")){
					member.setNickName(memberInfoJson.getString("name"));
				}else{
					member.setNickName(email.substring(0, email.indexOf("@")));
				}
				if(!memberInfoJson.isNull("avatar")){
					member.setAvatarHash(memberInfoJson.getString("avatar"));
				}
				if(!memberInfoJson.isNull("is_admin")){
					member.setAdmin(true);
				}else{
					member.setAdmin(false);
				}
				members.add(member);
			}
			//邀请的邮箱
			JSONArray inviteMembersJson = groupjson.getJSONArray("invite");		
			for(int i = 0; i < inviteMembersJson.length(); i++){
				JSONObject inviteMemberJson = inviteMembersJson.getJSONObject(i);
				CGroupMember member =new CGroupMember();
				String email =inviteMemberJson.getString("email");
				member.setUid(EncryptUtil.getMd5(email));
				member.setEmail(email);
				member.setInviteMember(true);
				if(!inviteMemberJson.isNull("name")){
					member.setNickName(inviteMemberJson.getString("name"));
				}else{
					member.setNickName(email.substring(0, email.indexOf("@")));
				}
				if(!inviteMemberJson.isNull("avatar")){
					member.setAvatarHash(inviteMemberJson.getString("avatar"));
				}
				if(!inviteMemberJson.isNull("is_admin")){
					member.setAdmin(true);
				}else{
					member.setAdmin(false);
				}
				members.add(member);
			}
			cGroup.setMembers(members);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cGroup;
	}
	/**
	 * 解析加入及获取群组
	 *
	 * @Description:
	 * @param groupjson
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-24
	 */
	public static List<PendingMQTTConmmand> parseGroupInvitation(JSONObject groupjson){
		List<PendingMQTTConmmand> pendingMQTTConmmandCGroupCommands =new ArrayList<PendingMQTTConmmand>();
		try {
			if(!groupjson.isNull("data")){
				JSONArray dataJson=groupjson.getJSONArray("data");
				for (int i = 0; i < dataJson.length(); i++) {
					JSONObject inviterJSONObject = dataJson.getJSONObject(i);
					PendingMQTTConmmand PendingHTTPSJoinCGroupCommand=new PendingMQTTConmmand(-1, ActionListener.Action.SUBSCRIBE, MQTTCommand.JOIN_GROUP, inviterJSONObject.getString("group_id"), inviterJSONObject.getString("inviter"));
					pendingMQTTConmmandCGroupCommands.add(PendingHTTPSJoinCGroupCommand);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pendingMQTTConmmandCGroupCommands;
	}
	/**
	 * 
	 * method name: parseGroupInvitation
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param groupjson
	 *      @return    field_name
	 *      List<PendingMQTTConmmand>    return type
	 *  @History memory：
	 *     @Date：2016-3-21 下午5:33:45	@Modified by：zhangjx
	 *     @Description：me接口解析并加入群
	 */
	public static List<PendingMQTTConmmand> parseGroupInvitation2(JSONObject groupjson){
		List<PendingMQTTConmmand> pendingMQTTConmmandCGroupCommands =new ArrayList<PendingMQTTConmmand>();
		try {
			if(!groupjson.isNull("invitations")){
				JSONArray dataJson=groupjson.getJSONArray("invitations");
				for (int i = 0; i < dataJson.length(); i++) {
					JSONObject inviterJSONObject = dataJson.getJSONObject(i);
					PendingMQTTConmmand PendingHTTPSJoinCGroupCommand=new PendingMQTTConmmand(-1, ActionListener.Action.SUBSCRIBE, MQTTCommand.JOIN_GROUP, inviterJSONObject.getString("gid"), inviterJSONObject.getString("by"));
					pendingMQTTConmmandCGroupCommands.add(PendingHTTPSJoinCGroupCommand);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pendingMQTTConmmandCGroupCommands;
	}
	/**
	 * 解析获取群组列表
	 *
	 * @Description:
	 * @param groupjson
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-24
	 */
	public static List<CGroup> parseGroups(String email, JSONObject groupjson){
		List<CGroup> cGroups =new ArrayList<CGroup>();
		try {
			if(!groupjson.isNull("data")){
				JSONArray dataJson=groupjson.getJSONArray("data");
				for (int i = 0; i < dataJson.length(); i++) {
					CGroup cGroup =new CGroup();
					JSONObject groupInfo = dataJson.getJSONObject(i);
					cGroup.setUid(groupInfo.getString("id"));
					cGroup.setGroupName(groupInfo.getString("name"));
					List<CGroupMember> members =new ArrayList<CGroupMember>();
					CGroupMember member =new CGroupMember();
					String creatorEmail=groupInfo.getString("creator");
					if(creatorEmail.equals(email)){
						cGroup.setIsAdmin(true);
					}else{
						cGroup.setIsAdmin(false);
					}
					member.setUid(EncryptUtil.getMd5(creatorEmail));
					member.setEmail(creatorEmail);
					member.setAdmin(true);
					members.add(member);
					cGroup.setMembers(members);
					cGroup.setLastSendDate(groupInfo.getLong("createtime")*1000);
					cGroups.add(cGroup);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cGroups;
	}
	public static ImapAndSmtpSetting parseImapAndSmtpSetting(JSONArray setJson){
		ImapAndSmtpSetting imapAndSmtpSetting =null;
		try {
			JSONArray  imapJSONArray = setJson.getJSONArray(0);
			JSONArray  smtpJSONArray = setJson.getJSONArray(1);
			imapAndSmtpSetting =new ImapAndSmtpSetting();
			imapAndSmtpSetting.setImapHost(imapJSONArray.getString(0));
			imapAndSmtpSetting.setImapPost(-1);
			imapAndSmtpSetting.setImapSafety(imapJSONArray.getInt(2));
			imapAndSmtpSetting.setSmtpHost(smtpJSONArray.getString(0));
			imapAndSmtpSetting.setSmtpPost(-1);
			imapAndSmtpSetting.setSmtpSafety(smtpJSONArray.getInt(2));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			imapAndSmtpSetting=null;
			e.printStackTrace();
		}
		return imapAndSmtpSetting;
	}

	/**
	 * 解析OA消息
	 * 备注: {
	 * 			'a': 'oa',               //应用类型，目前只有oa
     *       	'u': 'qiushzh@35.cn',    //消息接收人的邮件地址
     *          's': '请假条xxxx',        //消息标题，公告标题或者待办事务标题
     *          'f': '测试<test@35.cn>',  //公告或者消息的发起人
     *          't': 时间,                //时间（暂为服务器收到推送的时间）
     *          'c': '类型',              //消息类型：NEW_TRANS为待办事务，ANNOUNCE为公告
     *          'i': '6666',             //事件ID：待办事务ID或者公告ID
     *          'w': payload.url         //跳转的URL
     *      }
     *
	 * @Description:
	 * @param OAMessagejson
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-8-11
	 */
	public static DChatMessage parseOA2Dmessage(JSONObject OAMessagejson,String toEmail) {
		DChatMessage dmessage = new DChatMessage();
		try {
			dmessage.setUuid(UUID.randomUUID().toString());
			dmessage.setSenderEmail(GlobalConstants.DCHAT_OA);
			dmessage.setReceiverEmail(toEmail);
			dmessage.setURL(OAMessagejson.getString("w"));
			dmessage.setOAId(OAMessagejson.getString("i"));
			dmessage.setOASubject(OAMessagejson.getString("s"));
			/**
			 * OAMessagejson.getString("f") 格式：盛力<shengli@35.cn>
			 */
			String fromInfo = OAMessagejson.getString("f");
			dmessage.setOAFrom(fromInfo);
			fromInfo = StringUtil.getInfoNickName(fromInfo);
			String  oaType = OAMessagejson.getString("c");
			String messageContent ="";
			Type type = null;
			switch (oaType) {
			case "NEW_TRANS":
				type = Type.OA_NEW_TRANS;
				messageContent="["+MailChat.getInstance().getString(R.string.oa_trans)+"]"+fromInfo+ ":"+ OAMessagejson.getString("s");
				break;
			case "ANNOUNCE":
				type = Type.OA_ANNOUNCE;
				messageContent="["+MailChat.getInstance().getString(R.string.oa_announce)+"]"+fromInfo+ ":"+ OAMessagejson.getString("s");
				break;
			default:
				break;
			}
			dmessage.setMessageType(type);
			dmessage.setMessageContent(messageContent);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return dmessage;
	}

	private static void setChilds(String id, JSONArray relArray,
			JSONArray usersArray, List<Eis35Bean> resultList,String company)
			throws JSONException {
		for (int i = 0; i < relArray.length(); i++) {
			// 'b': 部门ID, 'u': 员工邮箱, 'l': 是否领导(1/0)
			JSONObject relJSONObject = relArray.getJSONObject(i);
			String pId = relJSONObject.optString("b");
			String email = relJSONObject.optString("u");
			String isLeader = relJSONObject.optString("l");
			if (pId.equals(id)) {
				for (int j = 0; j < usersArray.length(); j++) {
					// 'n': 姓名, 'e': 邮箱, 'k': 邮洽昵称, 'a': 邮洽头像
					JSONObject usersJSONObject = usersArray.getJSONObject(j);
					String name = usersJSONObject.optString("n");
					String email1 = usersJSONObject.optString("e");
					String nickName = usersJSONObject.optString("k");
					String userHeadImg = usersJSONObject.optString("a");
					String usedMailchat = usersJSONObject.optString("s");
					if (email.equals(email1)) {
						Eis35Bean userBean = new Eis35Bean();
						userBean.setId(email1);
						userBean.setParentId(id);
						userBean.setName(name);
						userBean.setEmail(email1);
						userBean.setMailChatName(nickName);
						userBean.setImgHeadUrl(userHeadImg);
						userBean.setLeader(isLeader.equals("1") ? true : false);
						userBean.setUsedMailchat(usedMailchat.equals("1") ? true : false);
						userBean.setParent(false);
						resultList.add(userBean);
					}
				}
			}
		}
	}

	public static List<Eis35Bean> parse35EisList(JSONObject json) {
		List<Eis35Bean> resultList = new ArrayList<Eis35Bean>();
		try {
			if (!json.isNull("result")) {
				if (json.optInt("result") == 1) {
					// 公司名称
					String company = json.optString("name");
					// 部门集合
					JSONArray depsArray = json.optJSONArray("deps");
					// 员工集合
					JSONArray usersArray = json.optJSONArray("users");
					// 部门员工关系
					JSONArray relArray = json.optJSONArray("rels");
					/*
					 * 1.select * form deps 2.create ContactAttribute 3.select userids
					 * from rels where depid = dep.id 4.select * from users
					 * where email in userids 5.create ContactAttribute 6.next
					 */
					for (int i = 0; i < depsArray.length(); i++) {
						Eis35Bean depsBean = new Eis35Bean();
						JSONObject depsJSONObject = depsArray.getJSONObject(i);
						// 'i': 编号, 'n': 名称, 'p': 上级部门ID（顶级为0）, 's': 排序ID
						String id = depsJSONObject.optString("i");
						String parentId = depsJSONObject.optString("p");
						String name = depsJSONObject.optString("n");
						String sort = depsJSONObject.optString("s");
							depsBean.setId(id);
							depsBean.setParentId(parentId);
							depsBean.setName(name);
							depsBean.setSort(sort);
							depsBean.setParent(true);
							resultList.add(depsBean);
							//解析所有成员
							setChilds(id, relArray, usersArray, resultList,company);
					}
				} else {

				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return resultList;
	}
}
