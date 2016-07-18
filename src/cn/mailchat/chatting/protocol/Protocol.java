package cn.mailchat.chatting.protocol;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.location.b.j;

import android.util.Base64;
import android.util.Log;
import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.chatting.beans.PendingMQTTConmmand;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessageException;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.utils.RSAUtils;
import cn.mailchat.utils.SystemUtil;
import cn.mailchat.utils.Utility;

/**
 * 封装群聊协议层
 * 
 * @Description:
 * @author:xulei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:Jan 6, 2014
 */
public class Protocol {
	private static final String TAG =Protocol.class.getSimpleName();
	private static Protocol instance;
	private static String BASE_URL;

	private ConcurrentHashMap<String, String> params;
	public static Protocol getInstance() {
		if (instance == null) {
			instance = new Protocol();
		} 
		return instance;
	}

	private Protocol() {
		init();
	}

	private void init() {
		try {
			if(BASE_URL==null){
				BASE_URL=GlobalConstants.BASE_URL;
			}
			params = new ConcurrentHashMap<String, String>();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getBASE_URL() {
		return BASE_URL;
	}

	public static void setBASE_URL(String bASE_URL) {
		BASE_URL = bASE_URL;
	}

	private void prepareParams() {
		params.clear();
	}

	public String getUrl(Command command) {
		return getUrl(command, "");
	}

	private String getUrl(Command command, String extra) {
		StringBuilder builder = new StringBuilder(BASE_URL);
		String requestCommand = "";
		switch (command) {
			case CREATE_GROUP:
				requestCommand = "cg";
				break;
			case QUIT_GROUP:
				requestCommand = "qg";
				break;
			case DELETE_GROUP:
				requestCommand = "dg";
				break;
			case JOIN_GROUP:
				requestCommand = "jg3";
				break;
			case GET_GROUP:
				requestCommand = "ggi3";
				break;
			case GET_GROUP_INVITATION:
				requestCommand = "sm";
				break;
			case LIST_GROUPS:
				requestCommand="gmg";
				break;
	//		case LIST_MESSAGES:
	//			requestCommand = "group_messages";
	//			break;
			case ADD_MEMBER:
				requestCommand = "agm";
				break;
			case REMOVE_MEMBER:
				requestCommand = "dgm";
				break;
			case CHANGE_GROUP_NAME:
				requestCommand = "rng";
				break;
			case IS_USER:
				requestCommand = "cu";
				break;
			case INVITATION_EMAIL:
				requestCommand = "invite";
				break;
			case GET_EMAIL_SETTING:
				requestCommand = "ms2";
				break;
	//		case SEND_MESSAGE:
	//			requestCommand = "post_message";
	//			break;
	//		case LIST_MEMBERS:
	//			requestCommand = "members";
	//			break;
	//		case DELETE_MESSAGE:
	//			requestCommand = "delmsg";
	//			break;
			case UPLOAD_FILE:
				requestCommand = "upload";
				break;
			case DOWN_FILE:
				requestCommand = "file";
				break;
			case GET_35EMAIL_VERSION:
				requestCommand = "email/check";
				break;
			case UPLOAD_USER_HEAD_FILE:
				requestCommand = "sa";
				break;
			case GET_USER_INFO:
				requestCommand = "me";
				break;
			case SYNC_USER_INFO:
				requestCommand = "gc2";
				break;
			case REGISTER_PUSH:
				requestCommand="push/reg";
				break;
			case UNREGISTER_PUSH:
				requestCommand="push/unreg";
				break;
			case INVITATION_EMAILS:
				requestCommand ="invite2";
				break;
			case SYNC_35EIS_LIST:
				requestCommand="eis/list";
				break;
			case DCHAT_OFF_LINE:
				requestCommand ="uim";
				break;
			case USER_CHECK:
				requestCommand ="user/auth";
				break;
			case USER_ADD_EMAIL:
				requestCommand ="user/am";
				break;
			case GET_INVITATION_CODE:
				requestCommand ="ic";
				break;
			case REGISTER_NEW_CLIENT:
				requestCommand ="user/newclient";
				break;
		}
		builder.append(requestCommand);
		if (extra != null && !extra.equals("")) {
			builder.append("/").append(extra);
		}
		return builder.toString();
	}


	/**
	 * 创建群组
	 * 
	 * @Description:
	 * @param emial
	 * @param group
	 * @param language 系统语言环境 (en（英文）、zh-cn（简体中文）、zh-tw（繁体中文），默认为简体中文)
	 * @return
	 * @throws MessageException
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-16
	 */
	public CGroup createGroup(String email,CGroup group,String language) throws MessageException {
		prepareParams();
		params.put("email", email);
		params.put("id", group.getUid());
		params.put("name", group.getGroupName());
		params.put("lang", language);
		String members=null;
		List<CGroupMember> CGroupMembers= group.getMembers();
		for(CGroupMember cGroupMember:CGroupMembers){
			if (members == null) {
				members = cGroupMember.getEmail();
			} else {
				members += "," + cGroupMember.getEmail();
			}
		}
		params.put("members",members);
		Response response = Request.post(getUrl(Command.CREATE_GROUP), params);
		return ParseJson.createGroup(response.toJson());
	}
	
	/**
	 * 加入群组
	 * 
	 * @Description:
	 * @param emial
	 * @param group
	 * @return
	 * @throws MessageException
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return 
	 * @date:2014-10-16
	 */
	
	public CGroup joinGroup(String email,String groupId) throws MessageException {
		prepareParams();
		params.put("email", email);
		params.put("id", groupId);
		Response response = Request.get(getUrl(Command.JOIN_GROUP), params);
		return ParseJson.joinAndGetGroup(email, response.toJson());
	}
	
	/**
	 * 获取群组信息
	 * 
	 * @Description:
	 * @param emial
	 * @param group
	 * @return
	 * @throws MessageException
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return 
	 * @date:2014-10-16
	 */
	public CGroup getGroup(String email,String groupId) throws MessageException {
		prepareParams();
		params.put("email", email);
		params.put("id", groupId);
		Response response = Request.get(getUrl(Command.GET_GROUP), params);
		return ParseJson.joinAndGetGroup(email, response.toJson());
	}
	/**
	 * 登陆邮箱时，获取群邀请信息
	 *
	 * @Description:
	 * @param emial 本用户邮箱
	 * @return
	 * @throws MessageException
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-24
	 */
	public List<PendingMQTTConmmand> getGroupInvitation(String... param) throws MessageException {
		// TODO Auto-generated method stub
		prepareParams();
		if(param.length>1){
			params.put("email", param[0]);
			params.put("imap", param[1]);
			params.put("smtp", param[2]);
		}else{
			params.put("email", param[0]);
		}
		Response response = Request.get(getUrl(Command.GET_GROUP_INVITATION), params);
		return ParseJson.parseGroupInvitation(response.toJson());
	}

	/**
	 * 获取账户群组列表
	 *
	 * @Description:
	 * @param emial 本用户邮箱
	 * @return
	 * @throws MessageException
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @date:2014-10-24
	 */
	public List<CGroup> getCGroups(String email) throws MessageException {
		// TODO Auto-generated method stub
		prepareParams();
		params.put("email", email);
		Response response = Request.get(getUrl(Command.LIST_GROUPS), params);
		return ParseJson.parseGroups(email,response.toJson()) ;
	}

	/**
	 * 群成员退出群
	 *
	 * @Description:
	 * @param emial 本用户邮箱
	 * @param id    群组ID
	 * @return
	 * @throws MessageException
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @date:2014-10-30
	 */
	public void leaveCGroup(String email,String cGroupId) throws MessageException{
		prepareParams();
		params.put("email", email);
		params.put("id", cGroupId);
		Response response =Request.get(getUrl(Command.QUIT_GROUP), params);
		response.toJson();
	}
	/**
	 * 群管理员删除群
	 *
	 * @Description:
	 * @param emial 本用户邮箱
	 * @param id    群组ID
	 * @return
	 * @throws MessageException
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @date:2014-10-30
	 */
	public void deleteCGroup(String email,String cGroupId) throws MessageException{
		prepareParams();
		params.put("email", email);
		params.put("id", cGroupId);
		Response response =Request.get(getUrl(Command.DELETE_GROUP), params);
		response.toJson();
	}

	/**
	 * 群管理员移除群成员
	 *
	 * @Description:
	 * @param emial 本用户邮箱
	 * @param id    群组ID
	 * @param members 需要删除的群成员列表
	 * @return
	 * @throws MessageException
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @date:2014-10-30
	 */
	public void deleteMembers(String email,String cGroupId,String memberEmail) throws MessageException{
		prepareParams();
		params.put("email", email);
		params.put("id", cGroupId);
		params.put("members", memberEmail);
		Response response =Request.post(getUrl(Command.REMOVE_MEMBER), params);
		response.toJson();
	}
	
	/**
	 * 增加群成员（是否管理员才能增加——尚未验证）
	 * 
	 * method name: addMembers 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param email
	 * @param cGroupId
	 * @param memberEmail
	 * @param language 系统语言环境 (en（英文）、zh-cn（简体中文）、zh-tw（繁体中文），默认为简体中文)
	 * @throws MessageException    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-11-10 下午6:03:22	@Modified by：zhangyq
	 * @Description：
	 */
	public void addMembers(String email,String cGroupId, String membersStr,String language) throws MessageException{
		prepareParams();
		params.put("email", email);
		params.put("id", cGroupId);
		params.put("members", membersStr);
		params.put("lang", language);
		Response response =Request.post(getUrl(Command.ADD_MEMBER), params);
		response.toJson();
	}

	/** 
	 * method name: updateGroupName 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param group    field_name
	 *      void    return type
	 * @throws MessageException 
	 *  @History memory：
	 *     @Date：2014-11-6 下午8:04:44	@Modified by：zhangjx
	 *     @Description：、
	 *     /rng(email, id, new_name)
	 */
	public void updateGroupName(String email,CGroup group) throws MessageException {
		prepareParams();
		params.put("email",email );
		params.put("id", group.getUid());
		params.put("new_name", group.getGroupName());
		Response response =Request.get(getUrl(Command.CHANGE_GROUP_NAME), params);
		response.toJson();
	}
	/**
	 * 用户上传文件
	 *
	 * method name: uploadFile
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param file
	 * @throws MessageException    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-11-13 下午2:51:05	@Modified by：zhangyq
	 * @Description：
	 */
	public JSONObject uploadFile(File file,String dAttachmentId,UploadCallback uploadCallback) throws MessageException {
		prepareParams();	
		Response response = Request.postWithFile(getUrl(Command.UPLOAD_FILE), file,null,"file",dAttachmentId,uploadCallback);
		return response.toJson();
	}
	/**
	 * 下载文件
	 * 
	 * method name: downFile 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param file
	 * @throws MessageException    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-11-13 下午2:51:05	@Modified by：zhangyq
	 * @Description：
	 */
	public void downFile(String attchmentId,String path,String fileid,String fileName,boolean isthumbnail,long fileSize,DownloadCallback downloadCallback) throws MessageException {
		Response response =Request.getDownFile(attchmentId,getUrl(Command.DOWN_FILE), fileid,fileName,isthumbnail,downloadCallback);
		response.toDwonFile(attchmentId,path,fileSize,downloadCallback);
	}
	/**
	 * 是否用过邮恰
	 *
	 * @Description:
	 * @param emial 要单聊的用户邮箱
	 * @return
	 * @throws MessageException
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return 0 没有用过邮洽
	 * @return 1 是邮洽用户
	 * @return -1 连接超时,或请求出错,或解析出错
	 * @date:2014-11-20
	 */
	public List<Object> isUser(String email){
		prepareParams();
		params.put("email",email );
		Response response = Request.get(getUrl(Command.IS_USER), params);
		List<Object> resultList = new ArrayList<Object>();
		int result =-1;
		try {
			JSONObject json = response.toJson();
			result = json.getInt("result");
			if(result==1){
				resultList.add(result);
				if(!json.isNull("name")){
					resultList.add(json.getString("name"));
				}
				if(!json.isNull("avatar")){
					resultList.add(json.getString("avatar"));
				}
			}else{
				resultList.add(result);
			}
		} catch (MessageException |JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultList.clear();
			resultList.add(result);
		}
		return resultList;
	}
	/**
	 * 发送邀请邮件
	 *
	 * @Description:
	 * @param emial 要单聊的用户邮箱
	 * @param language 系统语言环境 (en（英文）、zh-cn（简体中文）、zh-tw（繁体中文），默认为简体中文)
	 * @return
	 * @throws MessageException
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @date:2014-11-20
	 */
	public void invitationUser(String email,String invitationEmail,String language) throws MessageException {
		prepareParams();
		params.put("email",email );
		params.put("invite",invitationEmail );
		params.put("lang", language);
		Response  response  =Request.get(getUrl(Command.INVITATION_EMAIL), params);	
		response.toJson();
	}
	/**
	 * 获取邮箱设置
	 *
	 * @Description:
	 * @param emial 要获取设置的邮箱
	 * @return
	 * @throws MessageException
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @date:2015-1-14
	 */
	public List<Object> getEmailSet(String email) throws MessageException{
		List<Object> paramList = new ArrayList<Object>(); 
		prepareParams();
		params.put("email",email );
		Response  response  =Request.get(getUrl(Command.GET_EMAIL_SETTING), params);	
		int result =-1;
		JSONObject json =response.toJson();
			try {
				result = json.getInt("result");
				if(result==0){
					paramList.add(result);
				}else if(result==1){
					if(json.isNull("data")){
						paramList.add(result);
					}else{
						paramList.add(result);
						paramList.add(ParseJson.parseImapAndSmtpSetting(json.getJSONArray("data")));
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result=-1;
				paramList.add(result);
			}
		return paramList;
	}

	/**
	 * 获取35邮箱ES的host
	 * 
	 * @Description:
	 * @param domain后缀
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return -1:失败 0:请求成功,但没有host,既不是35支持的域 1:35支持的域
	 * @throws MessageException
	 * @date:2015-2-3
	 */
	public int get35MailHostFromES(String domain) throws MessageException{
		Response response =Request.postBy35PushVerification(domain);
		JSONObject json =response.toJson();
		int host =-1;
		try {
			if(!json.isNull("code")&&json.getInt("code")==0){
				if(!json.getJSONArray("list").isNull(0)){
					host=1;
				}else{
					host=0;
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			host=-1;
		}
		return host;
	}

	/**
	 * 获取35邮箱版本
	 * 
	 * @Description:
	 * @param domain后缀
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return -1:失败(请求超时) 0:404即不为35邮箱 1:35支持的域3.0邮箱  2：35支持的域2.0邮箱
	 * @throws MessageException
	 * @date:2015-3-25
	 */
	public int get35MailVersion(String domain){
		int host =-1;
		try {
			Response response =Request.getBy35PushVerification(domain);
			JSONObject json =response.getJson();
			String version = json.getJSONObject("Service").getString("Version");
			if (version.equals("3.0")) {
				host = 1;
			} else if (version.equals("2.0")) {
				host = 2;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (MailChat.DEBUG) {
				Log.e(MailChat.LOG_TAG, e.toString());
			}
			host=-1;
		} catch (MessageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (MailChat.DEBUG) {
				Log.e(MailChat.LOG_TAG, e.toString());
			}
			int type = e.getExceptionType();
			if(type==Response.ANTHER_ERROR){
				//连接超时
				host=-1;
			}else {
				//表示该邮箱不为35域
				host=0;
			}
		}
		return host;
	}
	
	/**
	 * 获取35邮箱版本
	 * 
	 * @Description:
	 * @param domain后缀
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return -1:失败(请求超时) 0:即不为35邮箱 1:35支持的域3.0邮箱  2：35支持的域2.0邮箱
	 * @throws MessageException
	 * @date:2015-3-25
	 */
	public int get35MailVersionForLogin(String domain){
		int i=-1;
		prepareParams();
		params.put("domain",domain);
		Response  response  =Request.get(getUrl(Command.GET_35EMAIL_VERSION), params);	
		try {
			JSONObject json = response.toJson();
			if(json.getBoolean("is_eis")){
				String version = json.getString("version");
				if (version.equals("3.0")) {
					i = 1;
				} else if (version.equals("2.0")) {
					i = 2;
				}
			}else{
				i=0;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			i=-1;
		} catch (MessageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			i=-1;
		}
		return i;
	}

	/**
	 * 
	 * method name: uploadFile 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param file
	 *      @return
	 *      @throws MessageException    field_name
	 *      JSONObject    return type
	 *  @History memory：
	 *     @Date：2015-4-17 下午1:35:48	@Modified by：zhangjx
	 *     @Description：上传用户头像
	 */
	public JSONObject uploadUserHeadFile(ContactAttribute contact ,File file,final String cliendId) throws MessageException {
		String urlStr=getUrl(Command.UPLOAD_USER_HEAD_FILE);
		String RSAtoken=null;
		prepareParams();
		params.put("email", contact.getEmail());
		params.put("name", contact.getNickName());
		params.put("com", contact.getCompany());
		params.put("dep", contact.getDepartment());
		params.put("title", contact.getPosition());
		try {
			//登记邀请码  t=设备识别码&c=邀请码   , rsa加密
			String token = "c=" + contact.getInvitationCode() + "&t=" + cliendId;
			RSAtoken = Base64.encodeToString(RSAUtils.encryptByPublicKey(token.getBytes(), GlobalConstants.PUBLIC_KEY),Base64.DEFAULT);
		} catch (Exception e) {
			e.printStackTrace();
			return null ;
		}
		params.put("token", RSAtoken);//设备标识码
		Response response = Request.postWithFile(urlStr,file,params,"avatar",null,null);
		return response.toJson();
	}
	/**
	 * 
	 * method name: uploadFile 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param file
	 *      @return
	 *      @throws MessageException    field_name
	 *      JSONObject    return type
	 *  @History memory：
	 *     @Date：2015-4-17 下午2:35:48	@Modified by：zhangjx
	 *     @Description：获取放个用户头像信息
	 */
	public JSONObject loadRemoteUserInfo(String email)  throws MessageException{
		String urlStr=getUrl(Command.GET_USER_INFO);
		prepareParams();
		params.put("email", email);
		params.put("id", SystemUtil.getCliendId(MailChat.getInstance()));//（设备ID，对应邀请码使用的设备ID）
		Response response = Request.get(urlStr, params);
		return response.toJson();
	}
	/**
	 *
	 * method name: uploadFile 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param file
	 *      @return
	 *      @throws MessageException    field_name
	 *      JSONObject    return type
	 *  @History memory：
	 *     @Date：2015-4-17 下午4:35:48	@Modified by：zhangjx
	 *     @Description：获取一堆用户信息
	 */
	public JSONObject syncRemoteUserInfo(String email,
			JSONObject jsonObject)  throws MessageException{
		String urlStr=getUrl(Command.SYNC_USER_INFO);
		prepareParams();
		params.put("data",jsonObject.toString());
		Response response = Request.post(urlStr, params);
		return response.toJson();
	}

	/**
	 * 注册push.如果为2.0邮箱就注册sail push(IPush)
	 * 		       如果为3.0邮箱注册ma push(最新push)失败就去注册sail push(IPPush)
	 * 
	 * @Description:
	 * @param 
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return 
	 * @return 
	 * @throws MessageException
	 * @date:2016-1-12
	 */
	public boolean registerPush(Account account)  {
		prepareParams();
		boolean isPushSuccess=false;
		// email=邮箱帐号&password=密码&token=设备号或TOKEN
		String RSAtoken = null;
		try {
			String token = "email=" + account.getEmail() + "&password="+ Utility.getUserPassword(new URI(account.getStoreUri()))
					+ "&token=" + SystemUtil.getCliendId(MailChat.getInstance());
			RSAtoken = Base64.encodeToString(RSAUtils.encryptByPublicKey(token.getBytes(), GlobalConstants.PUBLIC_KEY),Base64.DEFAULT);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return isPushSuccess;
		}
		params.put("token", RSAtoken);
		Response response = Request.get(getUrl(Command.REGISTER_PUSH), params);
		try {
			JSONObject json  = response.toJson();
			if(json.getBoolean("is_eis")){
				if(!json.getString("push").equals("err")){
					isPushSuccess=true;
				}
			}else{
				isPushSuccess =true;
			}
		} catch (MessageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isPushSuccess;
	}
	
	/**
	 * 注销push.
	 * 
	 * @Description:
	 * @param 
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return 
	 * @return 
	 * @throws MessageException
	 * @date:2015-5-12
	 */
	public void unRegisterPush(String email,String clientId) throws MessageException  {
		prepareParams();
		String RSAtoken = null;
		try {
			String token = "email=" + email + "&token=" + clientId;
			RSAtoken = Base64.encodeToString(RSAUtils.encryptByPublicKey(token.getBytes(), GlobalConstants.PUBLIC_KEY),Base64.DEFAULT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		params.put("token", RSAtoken);
		Response response = Request.get(getUrl(Command.UNREGISTER_PUSH), params);
		JSONObject json =response.toJson();
		Log.i(TAG, json.toString());
	}
	/**
	 * 发送邀请邮件
	 *
	 * @Description:
	 * @param emial 要单聊的用户邮箱
	 * @param language 系统语言环境
	 * @return
	 * @throws MessageException
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @date:2015-7-27
	 */
	public void inviteAtionUsers(String email,String invitAtionEmails,String language) throws MessageException {
		prepareParams();
		params.put("email",email);
		params.put("invites",invitAtionEmails);
		params.put("lang", language);
		Response  response  =Request.post(getUrl(Command.INVITATION_EMAILS), params);
		response.toJson();
	}

	/**
	 * 
	 * method name: sync35EisList function @Description: TODO Parameters and
	 * return values description:
	 * 
	 * @param account
	 * @param isGetFromRemoteSession
	 *            可选参数，debug=1不从缓存获取 void return type
	 * @throws MessageException
	 * @History memory：
	 * @Date：2015-8-24 上午10:17:17 @Modified by：zhangjx
	 * @Description:获取EIS列表
	 */
	public JSONObject sync35EisList(Account account,
			boolean isGetFromRemoteSession) throws MessageException {
		prepareParams();
		if (StringUtils.isNullOrEmpty(account.getEmail())) {
			return null;
		}
		String[] splitEmail = account.getEmail().split("@");
		String domain = splitEmail[1];
		String RSAtoken = null;
		try {
			String token = "domain=" + domain;
			RSAtoken = Base64.encodeToString(
					RSAUtils.encryptByPublicKey(token.getBytes(), GlobalConstants.PUBLIC_KEY),
					Base64.DEFAULT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		params.put("token", RSAtoken);
//		params.put("debug", isGetFromRemoteSession ? "-1" : "1");
		Response response = Request
				.get(getUrl(Command.SYNC_35EIS_LIST), params);
		return response.decodeResponseByZlib(response);
    }

	/**
	 * 根据域名判断，是否为OA用户
	 *
	 * @Description:
	 * @param domain
	 *            需要验证的域名
	 * @param domain
	 *            邮箱地址
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-09-21
	 */
	public boolean isOAUser(String domain,String email){
		boolean isOAUser =false;
		try {
			JSONObject json =Request.isOAUser(domain, email).toOAJson();
			int code = json.getInt("code");
			if(code==0){
				isOAUser=true;
			}
		} catch (MessageException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isOAUser;
	}

	/**
	 * 开启单聊离线
	 *
	 * @Description:
	 * @param email
	 *            用户邮箱
	 * @param toEmail
	 *            对方邮箱
	 * @param t
	 *            开启或关闭（1/0）
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws MessageException
	 * @throws JSONException
	 * @date:2015-10-10
	 */
	public void openDChatOffLine(String email,String toEmail,int t) throws MessageException{
		prepareParams();
		params.put("email",email );
		params.put("to",toEmail );
		params.put("t", t+"");
		Response  response  =Request.get(getUrl(Command.DCHAT_OFF_LINE), params);
		response.toJson();
	}
	/**
	 * 用户认证
	 *
	 * @Description:
	 * @param Token 加密信息
	 * @return
	 * @throws MessageException
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @date:2015-10-28
	 */
	public void userCheck(String token) throws MessageException{
		prepareParams();
		params.put("token",token);
		Response  response  =Request.get(getUrl(Command.USER_CHECK), params);
		response.toJson();
	}
	/**
	 * 用户绑定邮箱
	 *
	 * @Description:
	 * @param Token 加密信息
	 * @return
	 * @throws MessageException
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @date:2015-10-28
	 */
	public void bingEmailToUser(String token) throws MessageException{
		prepareParams();
		params.put("token",token);
		Response  response  =Request.get(getUrl(Command.USER_ADD_EMAIL), params);
		response.toJson();
	}

	/**
	 * 
	 * method name: getInvitationCode function @Description: TODO Parameters and
	 * return values description:
	 * 
	 * @param account
	 * @param cliendId
	 * @return
	 * @throws MessageException
	 *             field_name JSONObject return type
	 * @History memory：
	 * @Date：2015-12-15 上午10:51:02 @Modified by：zhangjx
	 * @Description:注册邀请码
	 */
	public JSONObject getInvitationCode(Account account, String cliendId)
			throws MessageException {
		prepareParams();
		// e=邮箱&t=设备标识码
		String RSAtoken = null;
		try {
			String token = "e=" + account.getEmail() + "&t=" + cliendId;
			RSAtoken = Base64.encodeToString(RSAUtils.encryptByPublicKey(
					token.getBytes(), GlobalConstants.PUBLIC_KEY),
					Base64.DEFAULT);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		params.put("token", RSAtoken);
		JSONObject json = Request.get(getUrl(Command.GET_INVITATION_CODE),
				params).toJson();
		return json;
	}

	public JSONObject registerNewClient(String cliendId) throws MessageException {
		prepareParams();
		params.put("id",cliendId);
		params.put("type","android");
		JSONObject json =  Request.get(getUrl(Command.REGISTER_NEW_CLIENT), params).toJson();
		return json;
	}

	public String getWeixinShareOpenId(String code) {
		String tokenUrl = String.format(GlobalConstants.weixin_share_url, GlobalConstants.WEICHAT_APPID,
				GlobalConstants.WEICHAT_SECRET, code);
		prepareParams();
		Response response = Request.get(tokenUrl, params);
		return response.toString();
	}

	/**
	 * 获取文件的下载URL
	 *
	 * @Description:
	 * @param fileid
	 * @param fileName
	 * @param isthumbnail 图片缩略图判断
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @date:2016-2-23
	 */
	public String getFileURL(String fileid, String fileName, boolean isthumbnail) {
		StringBuilder builder = new StringBuilder();
		try {
			fileName = URLEncoder.encode(fileName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (isthumbnail) {
			builder.append("/").append(fileid).append("_s/").append(fileName);
		} else {
			builder.append("/").append(fileid).append("/").append(fileName);
		}
		return getUrl(Command.DOWN_FILE) + builder.toString();
	}

	/**
	 * 判断是否绑定了oa
	 * 
	 * @param email
	 * @return
	 */
	public JSONObject checkIsBindOA(String email) {
		JSONObject json = null;
		try {
			json = Request.checkIsBindOA(email).toOAJson();
		} catch (MessageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	/**
	 * 登录oa
	 * 
	 * @param account
	 * @throws MessageException
	 */
	public boolean loginInOA(Account account) throws MessageException {
//		int type = 0;
		JSONObject json = Request.loginInOA(account).toOAJson();
//		Log.d("qxian", json.toString());
		boolean isSuccess = json.optBoolean("success");
//		if (isSuccess) {
//			type = json.optInt("type");
//		}
		return isSuccess;
	}
}
