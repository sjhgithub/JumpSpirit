package com.c35.mtd.pushmail.store;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.GlobalConstants;
import com.c35.mtd.pushmail.GlobalVariable;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.beans.C35Attachment;
import com.c35.mtd.pushmail.beans.C35Folder;
import com.c35.mtd.pushmail.beans.C35Message;
import com.c35.mtd.pushmail.beans.Contact;
import com.c35.mtd.pushmail.beans.FolderObj;
import com.c35.mtd.pushmail.beans.ProxyServerDomain;
import com.c35.mtd.pushmail.command.request.AdvanceSearchMailsRequest;
import com.c35.mtd.pushmail.command.request.AttachmentRequest;
import com.c35.mtd.pushmail.command.request.BaseRequest;
import com.c35.mtd.pushmail.command.request.CommitMailsStatusRequest;
import com.c35.mtd.pushmail.command.request.DelMailRequest;
import com.c35.mtd.pushmail.command.request.FileViewByHtmlRequest;
import com.c35.mtd.pushmail.command.request.GetAttachmentListRequest;
import com.c35.mtd.pushmail.command.request.GetBetweenUsMailRequest;
import com.c35.mtd.pushmail.command.request.GetContactsRequest;
import com.c35.mtd.pushmail.command.request.GetFolderListRequest;
import com.c35.mtd.pushmail.command.request.GetIdsByIdRequest;
import com.c35.mtd.pushmail.command.request.GetIdsByIdRequest.GetIdsType;
import com.c35.mtd.pushmail.command.request.GetMailByIdRequest;
import com.c35.mtd.pushmail.command.request.GetMailListByMailIdsRequest;
import com.c35.mtd.pushmail.command.request.GetMailsStatusRequest;
import com.c35.mtd.pushmail.command.request.GetProxyServerDomainRequest;
import com.c35.mtd.pushmail.command.request.LoginRequest;
import com.c35.mtd.pushmail.command.request.RecallMailRequest;
import com.c35.mtd.pushmail.command.request.SearchIdsByTypeRequest;
import com.c35.mtd.pushmail.command.request.SendMailRequest;
import com.c35.mtd.pushmail.command.request.UpLoadAttachmentRequest;
import com.c35.mtd.pushmail.command.request.UpdateCalendarStateRequest;
import com.c35.mtd.pushmail.command.request.VersionRequest;
import com.c35.mtd.pushmail.command.response.AdvanceSearchMailsResponse;
import com.c35.mtd.pushmail.command.response.BaseResponse;
import com.c35.mtd.pushmail.command.response.CommitMailsStatusResponse;
import com.c35.mtd.pushmail.command.response.DelMailResponse;
import com.c35.mtd.pushmail.command.response.FileViewByHtmlResponse;
import com.c35.mtd.pushmail.command.response.GetAttachmentListResponse;
import com.c35.mtd.pushmail.command.response.GetContactsResponse;
import com.c35.mtd.pushmail.command.response.GetFolderListResponse;
import com.c35.mtd.pushmail.command.response.GetFolderListResponse.GetFolderType;
import com.c35.mtd.pushmail.command.response.GetIdsByIdResponse;
import com.c35.mtd.pushmail.command.response.GetMailByIdResponse;
import com.c35.mtd.pushmail.command.response.GetMailListByMailIdsResponse;
import com.c35.mtd.pushmail.command.response.GetMailsStatusResponse;
import com.c35.mtd.pushmail.command.response.GetProxyServerDomainResponse;
import com.c35.mtd.pushmail.command.response.LoginResponse;
import com.c35.mtd.pushmail.command.response.RecallMailResponse;
import com.c35.mtd.pushmail.command.response.SearchIdsByTypeResponse;
import com.c35.mtd.pushmail.command.response.SendMailResponse;
import com.c35.mtd.pushmail.command.response.UpdateCalendarStateResponse;
import com.c35.mtd.pushmail.command.response.VersionResponse;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.interfaces.AttDownLoadCallback;
import com.c35.mtd.pushmail.util.AesUtil;
import com.c35.mtd.pushmail.util.C35AppServiceUtil;
import com.c35.mtd.pushmail.util.C35ServerUtil;
import com.c35.mtd.pushmail.util.DESUtil;
import com.c35.mtd.pushmail.util.HexUtil;
import com.c35.mtd.pushmail.util.JsonUtil;
import com.c35.mtd.pushmail.util.MailUtil;
import com.c35.mtd.pushmail.util.StringUtil;
import com.c35.mtd.pushmail.util.ZipUtil;

/**
 * 支持ProxyServer(Sofia3.0)的核心,包含了各种调用传输层实现的命令 具体命令调用方法： 命令名： version public String version(String version) {
 * 1、构造VersionRequest对象，set需要的属性 2、调用executeRequest方法来实现请求， 如：VersionResponse response = (VersionResponse)
 * executeRequest(request, new VersionResponse()); 3、gc，并返回返回值 }
 * 
 * @author:xulei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2011-11-2
 */
public class C35Store extends Store {

	private String TAG = "C35Store";
	C35Transport mTransport = null;
	private String mUsername, mPassword;
	// int defaultPort = 9999;
	private static final String VERSION = "1.0";
	// public static final String GET_FOLDERLIST = "getFolderList";
	public static final String GET_SELF_DEFINED_FOLDERLIST = "getSelfDefinedFolderList";
	// public static final String GET_ARCHIVE_FOLDERLIST = "getArchiveFolderList";
	public static boolean responseIsNull = false;
	private String ticket = "";
	private String protocolKey = "";

	private final Object mDoExecuteCommand = new Object();

	/**
	 * 构造C35Store，new C35Transport()，set mUsername，mPassword
	 * 
	 * @param _uri
	 *            //c35proxy://pm3%4035.cn:qyyx12369A@wmail215.cn4e.com:5566
	 * @throws MessagingException
	 */
	public C35Store(String _uri) throws MessagingException {
		super(null, null, 1);
		URI uri;
		try {
			uri = new URI(_uri);
		} catch (URISyntaxException use) {
			throw new MessagingException(MessagingException.CONNECT_ERROR);
		}

		// mTransport = C35Transport.getInstance();
		mTransport = new C35Transport();
		mTransport.setUri(uri);
		mUsername = MailUtil.convert35CNToChinaChannel(mTransport.getUserInfoParts()[0]);// pm3@china-channel.com
		mPassword = mTransport.getUserInfoParts()[1];// qyyx12369A
	}

	/**
	 * 连接服务器，同时验证账户密码,do version
	 * 
	 * @return
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-29
	 */
	public String openAndGetTicket() throws MessagingException {
		if (!mTransport.isSocketAndStreamOpen()) {
			mTransport.openSocketAndStream();
		}
		if (ticket != null && !ticket.trim().equals("")) {
			return ticket;
		} else if (mTransport.isSocketAndStreamOpen()) {
			String loginKey = version(VERSION);
			if (StringUtil.isNotEmpty(loginKey)) {
				ticket = login(loginKey);
			}
			Debug.i(TAG, "getTicket() is " + ticket);
			return ticket;
		} else {
			// 说明ticket没能取得
			throw new MessagingException(MessagingException.RETURN_COMMAND_ERROR_VERSION, "openAndGetTicket failed!!");
		}

	}

	/**
	 * 执行noop，测试通道是否连通。 可以间隔服务器断开周期后再noop，以便提高效率？todo
	 * 
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-12-11
	 */
	private void execNoopCmd(String commandName) throws MessagingException {
		if (!(commandName.equals("version") || commandName.equals("login") || commandName.equals("getProxyServerDomain"))) {
			BaseRequest noopRequest = new BaseRequest();
			BaseResponse noopResponse = (BaseResponse) executeNoopRequest(noopRequest, new BaseResponse(), "noop", GlobalConstants.NO_ZIP_ONLY_JSON);
			if (noopResponse != null && noopResponse.getCommandCode() == 200) {
				// 说明没有问题
				return;
			} else {
				C35AppServiceUtil.writeSubscribeInformationToSdcard("noop失败,opensocket" + noopResponse != null ? noopResponse.getCommandCode() + "." : "。");// 彩蛋log写入
				mTransport.forceOpenSocketAndStream();
			}
		}
	}

	/**
	 * 为连接dp打开连接
	 * 
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:Aug 13, 2013
	 */
	public void dpOpen() throws MessagingException {
		if (!mTransport.isSocketAndStreamOpen()) {
			mTransport.openSocketAndStream();
		}
	}

	/**
	 * 设置版本号，登陆进行服务器验证
	 * 
	 * @param version
	 * @return
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: hanlixia
	 * @date:2012-11-2
	 */
	private String version(String version) throws MessagingException {
		if (StringUtil.isNotEmpty(version)) {
			VersionRequest request = new VersionRequest();
			request.setVersion(version);
			VersionResponse response = (VersionResponse) executeRequest(request, new VersionResponse(), "version", GlobalConstants.NO_ZIP_ONLY_JSON);
			// System.gc();
			return response.getLoginKey();
		} else {
			throw new MessagingException(MessagingException.CODE_VERSION_NO, "version为空，无法请求");
		}
	}

	/**
	 * 描述 登录
	 * 
	 * @throws MessagingException
	 * @Title: login
	 * @author liujie
	 * @date 2011-11-23
	 * @param String
	 *            loginKey 用于加密的串，必传
	 * @return String Ticket
	 * @throws
	 */
	private String login(String loginKey) throws MessagingException {
		if (StringUtil.isNotEmpty(loginKey)) {
			/**************************** 1、new对象 set feild ************************/
			LoginRequest request = new LoginRequest();// loginKey, mUsername, mPassword
			String encryptUser = HexUtil.byte2hex(DESUtil.encrypt(mUsername.getBytes(), loginKey.getBytes()));
			String encryptPwd = HexUtil.byte2hex(DESUtil.encrypt(mPassword.getBytes(), loginKey.getBytes()));
			request.setUser(encryptUser);
			request.setPwd(encryptPwd);
			request.setClientProductType("pushmail");
			request.setScreenWidth((int) (GlobalVariable.getScreenWidth() * GlobalConstants.RATE));
			request.setScreenHeight(GlobalVariable.getScreenHeight());
			/**************************** 2、执行请求，并返回返回值 ***************************/
			LoginResponse response = (LoginResponse) executeRequest(request, new LoginResponse(), "login", GlobalConstants.NO_ZIP_ONLY_JSON);
			/**************************** 3、gc，并返回返回值 ********************************/
			// System.gc();
			protocolKey = response.getProtocolKey();
			return response.getTicket();
		} else {
			throw new MessagingException(MessagingException.CODE_LOGIN_NO, "loginkey为空，无法进行登录命令");
		}
	}

	// 未被使用
	// public void loginout() throws MessagingException {
	// if (1 > 0) {
	// return;
	// }
	// if (!mTransport.isOpen())
	// return;
	// if (!StringUtil.isNotEmpty(ticket)) {
	// ticket = open();
	// }
	// LoginoutRequest request = new LoginoutRequest();
	// LoginoutResponse response = (LoginoutResponse) executeRequest(request, new LoginoutResponse(),
	// "logout", false);
	// System.gc();
	// closeSocket();
	// Log.d(TAG, "response = " + response.toString());
	// }
	// /**
	// * 退出服务器
	// * @param ticket
	// * @throws MessagingException
	// * @see:
	// * @since:
	// * @author: hanlixia
	// * @date:2012-11-2
	// */
	// private void loginout(String ticket) throws MessagingException {
	// if (1 > 0) {
	// return;
	// }
	// if (!StringUtil.isNotEmpty(ticket)) {
	// ticket = openAndGetTicket();
	// }
	// LogoutRequest request = new LogoutRequest();
	// LogoutResponse response = (LogoutResponse) executeRequest(request, new LogoutResponse(), "logout",
	// GlobalConstants.NO_ZIP_ONLY_JSON);
	// // System.gc();
	// closeSocket();
	// Debug.d(TAG, "response = " + response.toString());
	// }

	// public void close() throws MessagingException {
	// CloseRequest request = new CloseRequest();
	// CloseResponse response = (CloseResponse) executeRequest(request, new CloseResponse(), "close", false);
	// System.gc();
	// mTransport.close();
	// Log.d(TAG, "response = " + response.toString());
	// }
	/**
	 * 关闭与服务器的连接
	 * 
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-29
	 */
	public void closeSocket() {
		ticket = "";
		mTransport.close();
	}

	/**
	 * 加一个参数区分要拿的类型：全部、归档、自定义
	 * 
	 * @param ticket
	 * @param type
	 * @return
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: hanlixia
	 * @date:2012-11-2
	 */
	public ArrayList<C35Folder> getFolderList(String ticket, GetFolderType type) throws MessagingException {
		if (!StringUtil.isNotEmpty(ticket)) {
			ticket = openAndGetTicket();
		}
		GetFolderListRequest request = new GetFolderListRequest();
		request.setUser(mUsername);
		GetFolderListResponse response = new GetFolderListResponse();
		String commandName = "";
		switch (type) {
		// case DEFAULT:
		// commandName = GET_FOLDERLIST;
		// break;
		// case ARCHIVEFOLDER:
		// commandName = GET_ARCHIVE_FOLDERLIST;
		// break;
		case SELFDEFINED:
			commandName = GET_SELF_DEFINED_FOLDERLIST;
			break;
		default:
			commandName = GET_SELF_DEFINED_FOLDERLIST;// GET_FOLDERLIST
			break;
		}
		response = (GetFolderListResponse) executeRequest(request, response, commandName, GlobalConstants.NO_ZIP_ONLY_JSON);
		if (response != null) {
			ArrayList<C35Folder> folderResp = response.getFolders();
			if (folderResp.size() == 0) {
				// C35AppServiceUtil.writeSubscribeInformationToSdcard("getfolderresponse:" +
				// response.toString());// 彩蛋log写入
			}
			return folderResp;
		}
		// System.gc();
		responseIsNull = true;
		return null;
	}

	/**
	 * 按类型查询邮件ID列表
	 * 
	 * @param type
	 *            类型Type=1，“主送我的” Type=2，“来自重要发件人” Type=3，“紧急的邮件” Type=4，“来自发件人邮件” Type=5，“收藏邮件” Type=6，“未读邮件”
	 * 
	 * @param searchKey
	 *            关键字
	 * @param pageNo
	 *            第几页
	 * @param pageSize
	 *            页大小
	 * @param sortFieldNo
	 *            排序字段
	 * @param ascending
	 *            正倒序
	 * @return
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2013-1-21
	 */
	public List<String> searchIdsByType(int type, String searchKey, int pageNo, int pageSize, int sortFieldNo, int ascending) throws MessagingException {

		switch (type) {
		case 1:
			searchKey = "";
			break;
		case 2:
			searchKey = "";
			break;
		case 3:
			searchKey = "";
			break;
		case 4:
			break;
		case 5:
			searchKey = "";
			break;
		case 6:
			searchKey = "";
			break;
		default:
			break;
		}

		SearchIdsByTypeRequest request = new SearchIdsByTypeRequest();
		request.setUser(mUsername);
		request.setType(type);
		request.setSearchKey(searchKey);
		request.setPageNo(pageNo);
		request.setPageSize(pageSize);
		request.setSortFieldNo(sortFieldNo);
		request.setAscending(ascending);
		SearchIdsByTypeResponse response = null;
		response = (SearchIdsByTypeResponse) executeRequest(request, new SearchIdsByTypeResponse(), "searchIdsByType", GlobalConstants.NO_ZIP_ONLY_JSON);
		if (response != null) {
			return response.getIds();
		}
		return null;
	}

	/**
	 * getIdsById
	 * 
	 * @param folderId
	 *            邮件箱id（比如inbox）
	 * @param limit
	 *            数量限制
	 * @param uid
	 *            参考UID
	 * @param type
	 * @return
	 * @throws MessagingException
	 */
	public List<String> getIdsById(String folderId, int limit, String uid, GetIdsType type) throws MessagingException {
		GetIdsByIdRequest request = new GetIdsByIdRequest();
		request.setUser(mUsername);
		request.setFolderId(folderId);
		request.setId(uid);
		int type1 = 0;
		switch (type) {
		case NEW:
			type1 = 1;
			break;
		case OLD:
			type1 = 2;
			break;
		}
		request.setType(type1);
		request.setLimit(limit);
		GetIdsByIdResponse response = null;
		response = (GetIdsByIdResponse) executeRequest(request, new GetIdsByIdResponse(), "getIdsById", GlobalConstants.NO_ZIP_ONLY_JSON);
		if (response != null) {
			return response.getIds();
		}
		return null;
	}

	/**
	 * DP接口 11.4. 获取ProxyServer域名配置 getProxyServerDomain
	 * 
	 * @Description:
	 * @param emailOrDomain
	 * @return
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-10-8
	 */
	public ProxyServerDomain getProxyServerDomainFromDP(String emailOrDomain) throws MessagingException {
		GetProxyServerDomainRequest request = new GetProxyServerDomainRequest();
		request.setEmailOrDomain(emailOrDomain);// pm1@35.cn
		GetProxyServerDomainResponse response = new GetProxyServerDomainResponse();
		response = (GetProxyServerDomainResponse) executeRequest(request, response, "getProxyServerDomain", 0);
		close();// {"port":9999,"updownport":9998,"host":"mail.china-channel.com","domain":"35.cn","aliastype":1,"domaintype":3}
		if (response != null) {// commandId = 1 commandName =getProxyServerDomain commandCode = 200
								// commandMessage =
								// {"port":9989,"updownport":9988,"host":"mail.szdev.com","domain":"35.cn","aliastype":0,"domaintype":1}
			return response.getProxyServerDomain();
		} else {
			return null;
		}
	}

	/**
	 * 获取往来邮件
	 * 
	 * @param folderId
	 * @param limit
	 * @param uid
	 * @param type
	 * @return
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: gongfacun
	 * @date:2013-1-10
	 */
	public List<String> getBetweenUsMail(String user, String searchKey, String betweenUser, int pageNo, int pageSize, int sortFieldNo, int ascending) throws MessagingException {
		GetBetweenUsMailRequest request = new GetBetweenUsMailRequest();
		request.setUser(user);
		request.setSearchKey(searchKey);
		request.setBetweenUser(betweenUser);
		request.setPageNo(pageNo);
		request.setPageSize(pageSize);
		request.setSortFieldNo(sortFieldNo);
		request.setAscending(ascending);
		GetIdsByIdResponse response = null;
		response = (GetIdsByIdResponse) executeRequest(request, new GetIdsByIdResponse(), "getBetweenUsMail", GlobalConstants.NO_ZIP_ONLY_JSON);
		if (response != null) {
			return response.getIds();
		}
		return null;
	}

	/**
	 * 描述 通过mailId拿整封邮件
	 * 
	 * @author liujie
	 * @param mailId
	 * @param startByte
	 *            开始字节
	 * @param endByte
	 *            结束字节（startByte，endByte这两个属性只是截取富文本的大小限制,都传0，则表示不截取）
	 * @param zip
	 *            是否压缩
	 * @param returnParam
	 *            收取的类型：返回的数据： 1.只返回纯文本 2.只返回富文本 3.只返回纯文本与富文本 （startByte与endByte只对富文本的作截取） 4.接口中定义的所有内容都返回
	 *            （startByte与endByte只对富文本的作截取） 5.接口中定义的所有内容都返回(不返回富文本和纯文本)
	 * @date 2011-11-30
	 * @return C35Message
	 * @throws MessagingException
	 */
	public C35Message getMailById(String mailId, int startByte, int endByte, int zip, int returnParam) throws MessagingException {
		GetMailByIdRequest request = new GetMailByIdRequest();
		request.setMailId(mailId);
		request.setUser(mUsername);
		request.setReturnParam(returnParam);
		request.setStartByte(startByte);
		request.setEndByte(endByte);
		request.setAssemblePicture(0);
		GetMailByIdResponse response = null;
		response = (GetMailByIdResponse) executeRequest(request, new GetMailByIdResponse(), "getMailById", zip);
		// System.gc();
		// close(ticket);
		return response.getMessage();

	}

	/**
	 * 11.78. 获取真实邮箱账号(主域名中的主账号)
	 * 
	 * @Description:
	 * @param mailName
	 * @return
	 * @throws Exception
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-10-30
	 */
	public String getRealMail(String mailName) throws Exception {
		synchronized (mDoExecuteCommand) {
			mTransport.openSocketAndStream();
			String commandString = "1 getRealEmail getRealEmail 0 {\"user\":\"" + mailName + "\"}";
			Debug.i(TAG, "request: " + commandString);
			mTransport.writeLine(commandString);
			String result = mTransport.readLine();
			// close();
			Debug.i(TAG, "response: " + result);
			if (result != null && C35ServerUtil.isResponseOk(result)) {
				String realmailJson = result.substring(result.indexOf(",200,") + 5);
				JSONObject resultuser = new JSONObject(realmailJson);
				String realmail = resultuser.optString("realemail");
				return realmail;
			} else {
				C35AppServiceUtil.writeSubscribeInformationToSdcard("getRealMail:" + result);// 彩蛋log写入
				return null;
			}
		}
	}

	/**
	 * 通过一批mailId得到邮件列表
	 * 
	 * @throws MessagingException
	 * @Title: getMailListByMailIds
	 * @author liujie
	 * @date 2011-11-30
	 * @return void
	 * @throws
	 */
	public List<C35Message> getMailListByMailIds(List<String> mailIds, int previewSize) throws MessagingException {
		GetMailListByMailIdsRequest request = new GetMailListByMailIdsRequest();
		request.setUser(mUsername);
		request.setMailIds(mailIds);
		request.setPreviewSize(previewSize);
		GetMailListByMailIdsResponse response = null;
		response = (GetMailListByMailIdsResponse) executeRequest(request, new GetMailListByMailIdsResponse(), "getMailListByMailIds", GlobalConstants.GZIP_BASE64);
		// System.gc();
		return response.getMails();
	}

	/**
	 * 搜索邮件
	 * 
	 * @param keyWord
	 *            关键字
	 * @param searchKeyPosi
	 *            搜索字段
	 * @param sortFieldNo
	 *            排序字段
	 * @param ascending
	 *            正序/倒序
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            页大小
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jul 18, 2012
	 */
	public List<C35Message> advanceSearchMails(String folderId, String keyWord, String from, String to, int searchKeyPosi, int sortFieldNo, int ascending, int pageNo, int pageSize) throws MessagingException {
		AdvanceSearchMailsRequest request = new AdvanceSearchMailsRequest();
		request.setUser(mUsername);
		request.setSearchKey(keyWord);// 关键字
		request.setSearchKeyPosi(searchKeyPosi);// 搜索字段
		request.setSortFieldNo(sortFieldNo);// 排序字段
		request.setAscending(ascending);// 正倒序
		request.setPageNo(pageNo);// 第几页
		request.setPageSize(pageSize);// 页大小
		request.setFrom(from);
		request.setImportantFlag(-1);
		request.setFetchBodyType(130);
		request.setTo(to);
		request.setFolderId(folderId);
		request.setSourceProcessState(-1);
		AdvanceSearchMailsResponse response = null;
		try {
			response = (AdvanceSearchMailsResponse) executeRequest(request, new AdvanceSearchMailsResponse(), "advanceSearchMails", GlobalConstants.NO_ZIP_ONLY_JSON);
		} catch (MessagingException e) {
			Debug.e("failfast", "failfast_AA", e);
			throw new MessagingException(MessagingException.CODE_SEARCH_TIME_OUT_FOR_IMAP_ERROR, "搜索邮件超时未响应");
		}
		// System.gc();
		return response == null ? null : response.getMails();
	}

	// Modified by LL
	// BEGIN
    public List<C35Message> advanceSearchMails(String user, String folderId, String subject, String startSendTime, String endSendTime) throws MessagingException {
        AdvanceSearchMailsRequest request = new AdvanceSearchMailsRequest();
        request.setUser(user);
        request.setFolderId(folderId);
        request.setStartSendTime(startSendTime);
        request.setEndSendTime(endSendTime);
        
        request.setSearchKey(subject);// 关键字
        request.setSearchKeyPosi(0);// 搜索字段
        
        request.setSortFieldNo(1);// 排序字段
        request.setAscending(0);// 正倒序
        request.setPageNo(1);// 第几页
        request.setPageSize(2);// 页大小
        request.setImportantFlag(-1);
        request.setFetchBodyType(0);
        request.setSourceProcessState(-1);
        
        AdvanceSearchMailsResponse response = null;
        try {
            response = (AdvanceSearchMailsResponse) executeRequest(request, new AdvanceSearchMailsResponse(), "advanceSearchMails", GlobalConstants.NO_ZIP_ONLY_JSON);
        } catch (MessagingException e) {
            Debug.e("failfast", "failfast_AA", e);
            throw new MessagingException(MessagingException.CODE_SEARCH_TIME_OUT_FOR_IMAP_ERROR, "搜索邮件超时未响应");
        }
        // System.gc();
        return response == null ? null : response.getMails();
    }
    // END

	/**
	 * 提交本地一批邮件的状态（同步用）
	 * 
	 * @param folderObjs
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @throws MessagingException
	 * @date:2012-10-22
	 */
	public CommitMailsStatusResponse commitMailsStatus(List<FolderObj> folderObjs) throws MessagingException {
		CommitMailsStatusRequest request = new CommitMailsStatusRequest();
		request.setUser(mUsername);
		request.setFolderObjs(folderObjs);// 文件夹级的同步对象list
		CommitMailsStatusResponse response = null;
		response = (CommitMailsStatusResponse) executeRequest(request, new CommitMailsStatusResponse(), "commitMailsStatus", GlobalConstants.NO_ZIP_ONLY_JSON);
		return response;
	}

	/**
	 * 会议状态更新
	 * 
	 * @param mailId
	 * @param state
	 * @return
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2013-3-12
	 */
	public UpdateCalendarStateResponse updateCalendarState(String mailId, int state) throws MessagingException {
		UpdateCalendarStateRequest request = new UpdateCalendarStateRequest();
		request.setUser(mUsername);
		request.setMailId(mailId);
		request.setState(state);
		UpdateCalendarStateResponse response = null;
		response = (UpdateCalendarStateResponse) executeRequest(request, new UpdateCalendarStateResponse(), "updateCalendarState", GlobalConstants.NO_ZIP_ONLY_JSON);
		return response;
	}

	/**
	 * 取得本地一批邮件的最新状态（同步用）
	 * 
	 * @param folderId
	 * @param mailIds
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @throws MessagingException
	 * @date:2012-10-23
	 */
	public GetMailsStatusResponse getMailsStatus(String folderId, List<String> mailIds) throws MessagingException {
		GetMailsStatusRequest request = new GetMailsStatusRequest();
		request.setUser(mUsername);
		request.setFolderId(folderId);
		request.setMailIds(mailIds);
		GetMailsStatusResponse response = null;
		response = (GetMailsStatusResponse) executeRequest(request, new GetMailsStatusResponse(), "getMailsStatus", GlobalConstants.NO_ZIP_ONLY_JSON);
		return response;
	}

	/**
	 * 取得附件预览地址
	 * 
	 * @param mailId
	 * @param attachId
	 * @param cid
	 * @param compressFileName
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @throws MessagingException
	 * @date:2012-10-30
	 */
	public String fileViewByHtml(String mailId, String attachId, String cid, String compressFileName) throws MessagingException {
		FileViewByHtmlRequest request = new FileViewByHtmlRequest();
		request.setUser(mUsername);
		request.setMailId(mailId);
		request.setAttachId(attachId);
		request.setCid(cid);
		request.setCompressFileName(compressFileName);
		FileViewByHtmlResponse response = null;
		response = (FileViewByHtmlResponse) executeRequest(request, new FileViewByHtmlResponse(), "fileViewByHtml", GlobalConstants.NO_ZIP_ONLY_JSON);
		return response == null ? null : response.getUrl();
	}

	/**
	 * 获取联系人用
	 * 
	 * @param type
	 * @param size
	 * @return
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: wennan
	 * @date:2013-5-30
	 */
	public List<Contact> getContacts(int type, int size) throws MessagingException {
		GetContactsRequest request = new GetContactsRequest();
		request.setUser(mUsername);
		request.setType(type);
		request.setSize(size);
		GetContactsResponse response = (GetContactsResponse) executeRequest(request, new GetContactsResponse(), "getContacts", GlobalConstants.NO_ZIP_ONLY_JSON);
		return response == null ? null : response.getContacts();
	}

	/**
	 * 描述 删除服务器上的邮件
	 * 
	 * @throws MessagingException
	 * @author liujie
	 * @param mailIds
	 *            要删除的id，
	 * @param forever
	 *            是否永久删除
	 * @date 2011-12-30
	 * @return void
	 * @throws
	 */
	public void deleteMail(List<String> mailIds, boolean forever) throws MessagingException {
		DelMailRequest request = new DelMailRequest();
		request.setUser(mUsername);
		request.setMailIds(mailIds);
		DelMailResponse response = (DelMailResponse) executeRequest(request, new DelMailResponse(), forever ? "deleteMailForever" : "deleteMail", GlobalConstants.NO_ZIP_ONLY_JSON);
		// loginout(ticket);
	}

	/**
	 * 邮件召回
	 * 
	 * @throws MessagingException
	 * @param mailId
	 * @param recallEmails
	 * @see:
	 * @since:
	 * @author: hanlx
	 * @date:2013-1-7
	 */
	public void recallMail(String mailId, List<String> recallEmails) throws MessagingException {
		RecallMailRequest request = new RecallMailRequest();
		request.setUser(mUsername);
		request.setMailId(mailId);
		request.setRecallEmails(recallEmails);
		executeRequest(request, new RecallMailResponse(), "recallMail", GlobalConstants.NO_ZIP_ONLY_JSON);
	}

	/**
	 * 描述 下载附件
	 * 
	 * @throws Exception
	 * @Title: downloadData
	 * @author liujie
	 * @date 2011-12-2
	 * @return void
	 * @throws
	 */
	public void downloadData(C35Attachment attachmentInfo, String filePath, AttDownLoadCallback callback, int updownloadport) throws MessagingException {
		try {

			// mTransport.doDownloadData(attachmentInfo,attachmentInfo.getMailId(), ticket,
			// attachmentInfo.getId(), attachmentInfo.getCid(), attachmentInfo.getIsIcon(),
			// attachmentInfo.getId(), filePath,
			// attachmentInfo.getFileName(),callback,attachmentInfo.isCompress());
			mTransport.doDownloadData(attachmentInfo, openAndGetTicket(), filePath, callback, updownloadport);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			Debug.w("C35", "downloadData" + e.getMessage());
			// if (e.getExceptionType() == MessagingException.CODE_AUTH_NO) {
			// Debug.e(TAG, "附件下载时ticket失效重新拿ticket");//???
			// mTransport.close();
			// mTransport.doDownloadData(attachmentInfo,attachmentInfo.getMailId(), open(),
			// attachmentInfo.getId(), attachmentInfo.getCid(), attachmentInfo.getIsIcon(),
			// attachmentInfo.getId(), filePath,
			// attachmentInfo.getFileName(),callback,attachmentInfo.isCompress());
			// mTransport.doDownloadData(attachmentInfo, openAndGetTicket(), filePath, callback,
			// updownloadport);
			// } else {
			GlobalVariable.recordProgress(attachmentInfo.getId(), 101);// 计入缓存
			throw e;
			// }
		}
	}

	/**
	 * 描述 发送普通邮件
	 * 
	 * @throws MessagingException
	 * @Title: sendMail
	 * @author liujie
	 * @date 2011-12-5
	 * @return void
	 * @throws
	 */
	public String sendMail(C35Message message, Account account, int updownloadport) throws Exception {
		LocalStore mLocalStore = (LocalStore) Store.getInstance(account.getLocalStoreUri());
		SendMailRequest request = new SendMailRequest();
		request.setUser(mUsername); // 用户
		request.setOversea(0); // 0=不海外转发 1=海外转发
		request.setByOneSelf(0); // 独立发送 =0不 1=独立
		request.setSaveSent(1); // 是否保存到服务器的已发送箱 1=保存 0=不保存
		// 旧邮件id,用于回复及转发的操作的邮件id,如果是本地的uid，那么会造成发送不成功
		request.setReMailId(message.getReMailId().startsWith("Local") ? "" : message.getReMailId());
		request.setSendType(message.getSendType()); // 发送类型0=普通发送 1=回复 2=转发
		request.setDraftMailId(""); // 草稿邮件的id
		request.setPriority(message.getPriority()); // 重要性 1=高 5=低,3普通
		request.setMailsessionId(""); // 会话的id
		request.setTimingSendTime(""); // 发送时间,如果为空，表示立即发送
		request.setFrom(message.getFrom()); // 发送人
		request.setTo(message.getTo()); // 收件人
		request.setCc(message.getCc()); // 抄送人
		request.setBcc(message.getBcc()); // 暗送人
		request.setSubject(message.getSubject()); // 主题
		request.setAsk(message.getAcknowledgme()); // 回执标识 0=不 1=要
		request.setHyperText(message.getHyperText()); // 邮件富文本
		request.setHyperTextCharset(message.getHyperTextCharset()); // Webmail默认用UTF-8
		request.setPlainText(message.getPlainText()); // 邮件纯文本(如果纯文本为空/需转富文本当纯文本)
		request.setReplyTo(message.getReplyTo()); // 回复到
		request.setSignId(message.getSignId()); // 签名id
		request.setPlainTextCharset(message.getPlainTextCharset()); // 纯文本编码0
		request.setSourceSystem(message.getSourceSystem()); // 来自哪个系统
		request.setSourceProcessState(message.getSourceProcessState()); // 外部系统处理状态
		request.setSourceSystemData(message.getSourceSystemData()); // 客户数据
		request.setCalendarLocation(message.getCalendarLocation()); // 会议地点
		request.setCalendarStartTime(message.getCalendarStartTime()); // 会议开始时间
		request.setCalendarEndTime(message.getCalendarEndTime()); // 会议结束时间
		request.setCalendarRemindTime(message.getCalendarRemindTime()); // 会议提醒时间
		request.setRemindTime(""); // 邮件提醒
		// 资源文件或附件
		List<C35Attachment> atts = message.getAttachs();
		if (atts != null && atts.size() > 0) {
			List<AttachmentRequest> attachments = new ArrayList<AttachmentRequest>();
			for (C35Attachment att : atts) {
				AttachmentRequest attachmentRequest = new AttachmentRequest();
				Debug.p("finally attachment infos ---sourceAttachmentId--- " + att.getSourceAttachmentId() + "---FileName:---" + att.getFileName() + "----id =" + att.getId() + "---mailId---" + att.getMailId() + "---contentUri---" + att.getContent_uri() + "---downState---" + att.getDownState() + "---type---" + att.getContentType() + "---sourceMessageId---" + att.getSourceMessageUid());
				if (att.getSourceAttachmentId() != null && att.getCid() == null /*
																				 * && att.getDownState() !=
																				 * C35Attachment.DOWNLOADED
																				 */) {
					// 服务器转发附件
					String fileName = att.getFileName();
					attachmentRequest.setMailId(att.getSourceMessageUid());
					attachmentRequest.setAttachId(att.getSourceAttachmentId());
					attachmentRequest.setCid(att.getCid() == null ? "" : att.getCid());
					attachmentRequest.setType(UpLoadAttachmentRequest.AttachmentType.RELAY_ATT.getType());
					attachmentRequest.setFileName(fileName);
					attachments.add(attachmentRequest);
				} else if (att.getDownState() == C35Attachment.DOWNLOADED) {
					// 本地附件上传到服务器。
					String fileName = att.getFileName();
					String path = att.getPath();
					String serverPath = null;
					try {
						// 通过socket上传附件
						serverPath = mTransport.uploadCommand(fileName, path, openAndGetTicket(), updownloadport, C35Transport.BLOCKSIZE);

					} catch (MessagingException e) {
						if (att.getCid() != null && att.getCid().length() > 0) {// 内嵌附件，不提示错误
							serverPath = "内嵌附件:" + e.getMessage();
							C35AppServiceUtil.writeSubscribeInformationToSdcard(serverPath);// 彩蛋log写入
							Debug.w("failfast", serverPath, e);
						} else {
							throw e;
						}
					} catch (Exception e) {
						throw new MessagingException(e.getMessage());
					}
					attachmentRequest.setCid(att.getCid() == null ? "" : att.getCid());
					attachmentRequest.setType(att.getCid() == null ? UpLoadAttachmentRequest.AttachmentType.ATT.getType() : UpLoadAttachmentRequest.AttachmentType.RESOURCE.getType());
					attachmentRequest.setPath(serverPath);
					attachmentRequest.setFileName(fileName);
					attachments.add(attachmentRequest);
				}
			}
			request.setAttachments(attachments);
		}
		// try {
		executeRequest(request, new SendMailResponse(), "sendMail", GlobalConstants.NO_ZIP_ONLY_JSON);

		// 09-13 15:46:58.003: I/C35Store(30179): ServerResponse :5,sendMail,100515,Command execute
		// failed.100515:找不到原邮件中的附件/Can't find the original mail attachments
		message.setFolderId(EmailApplication.MAILBOX_SENTBOX);
		message.setSendStats(C35Message.SEND_SUCCESS);
		return mLocalStore.saveMessages(message, EmailApplication.MAILBOX_SENTBOX, account);

	}
	
    public void sendMail(C35Message message, Account account) throws MessagingException {
        SendMailRequest request = new SendMailRequest();
        request.setUser(mUsername); // 用户
        request.setOversea(0); // 0=不海外转发 1=海外转发
        request.setByOneSelf(0); // 独立发送 =0不 1=独立
        request.setSaveSent(1); // 是否保存到服务器的已发送箱 1=保存 0=不保存
        // 旧邮件id,用于回复及转发的操作的邮件id,如果是本地的uid，那么会造成发送不成功
        request.setReMailId(message.getReMailId().startsWith("Local") ? "" : message.getReMailId());
        request.setSendType(message.getSendType()); // 发送类型0=普通发送 1=回复 2=转发
        request.setDraftMailId(""); // 草稿邮件的id
        request.setPriority(message.getPriority()); // 重要性 1=高 5=低,3普通
        request.setMailsessionId(""); // 会话的id
        request.setTimingSendTime(""); // 发送时间,如果为空，表示立即发送
        request.setFrom(message.getFrom()); // 发送人
        request.setTo(message.getTo()); // 收件人
        request.setCc(message.getCc()); // 抄送人
        request.setBcc(message.getBcc()); // 暗送人
        request.setSubject(message.getSubject()); // 主题
        request.setAsk(message.getAcknowledgme()); // 回执标识 0=不 1=要
        request.setHyperText(message.getHyperText()); // 邮件富文本
        request.setHyperTextCharset(message.getHyperTextCharset()); // Webmail默认用UTF-8
        request.setPlainText(message.getPlainText()); // 邮件纯文本(如果纯文本为空/需转富文本当纯文本)
        request.setReplyTo(message.getReplyTo()); // 回复到
        request.setSignId(message.getSignId()); // 签名id
        request.setPlainTextCharset(message.getPlainTextCharset()); // 纯文本编码0
        request.setSourceSystem(message.getSourceSystem()); // 来自哪个系统
        request.setSourceProcessState(message.getSourceProcessState()); // 外部系统处理状态
        request.setSourceSystemData(message.getSourceSystemData()); // 客户数据
        request.setCalendarLocation(message.getCalendarLocation()); // 会议地点
        request.setCalendarStartTime(message.getCalendarStartTime()); // 会议开始时间
        request.setCalendarEndTime(message.getCalendarEndTime()); // 会议结束时间
        request.setCalendarRemindTime(message.getCalendarRemindTime()); // 会议提醒时间
        request.setRemindTime(""); // 邮件提醒
        // 资源文件或附件
        List<C35Attachment> atts = message.getAttachs();
        if (atts != null && atts.size() > 0) {
            List<AttachmentRequest> attachments = new ArrayList<AttachmentRequest>();
            for (C35Attachment att : atts) {
                AttachmentRequest attachmentRequest = new AttachmentRequest();
                Debug.p("finally attachment infos ---sourceAttachmentId--- " + att.getSourceAttachmentId() + "---FileName:---" + att.getFileName() + "----id =" + att.getId() + "---mailId---" + att.getMailId() + "---contentUri---" + att.getContent_uri() + "---downState---" + att.getDownState() + "---type---" + att.getContentType() + "---sourceMessageId---" + att.getSourceMessageUid());

//                if (att.getSourceAttachmentId() != null && att.getCid() == null /*
//                                                                                 * && att.getDownState() !=
//                                                                                 * C35Attachment.DOWNLOADED
//                                                                                 */) {
                // Modified by LL
                // BEGIN
                if (att.getType() == UpLoadAttachmentRequest.AttachmentType.RELAY_ATT.getType()
                        || (att.getSourceAttachmentId() != null && att.getCid() == null)) {
                // END
                    
                    // 服务器转发附件
                    String fileName = att.getFileName();
                    attachmentRequest.setMailId(att.getSourceMessageUid());
                    attachmentRequest.setAttachId(att.getSourceAttachmentId());
                    attachmentRequest.setCid(att.getCid() == null ? "" : att.getCid());
                    attachmentRequest.setType(UpLoadAttachmentRequest.AttachmentType.RELAY_ATT.getType());
                    attachmentRequest.setFileName(fileName);
                    attachments.add(attachmentRequest);
                } else if (att.getDownState() == C35Attachment.DOWNLOADED) {
                    // 本地附件上传到服务器。
                    String fileName = att.getFileName();
                    String path = att.getPath();
                    String serverPath = null;
                    try {
                        // 通过socket上传附件
                        serverPath = mTransport.uploadCommand(fileName, path, openAndGetTicket(), account.getUpdownloadport(), C35Transport.BLOCKSIZE);

                    } catch (MessagingException e) {
                        if (att.getCid() != null && att.getCid().length() > 0) {// 内嵌附件，不提示错误
                            serverPath = "内嵌附件:" + e.getMessage();
                            C35AppServiceUtil.writeSubscribeInformationToSdcard(serverPath);// 彩蛋log写入
                            Debug.w("failfast", serverPath, e);
                        } else {
                            throw e;
                        }
                    } catch (Exception e) {
                        throw new MessagingException(e.getMessage());
                    }
                    attachmentRequest.setCid(att.getCid() == null ? "" : att.getCid());
                    attachmentRequest.setType(att.getCid() == null ? UpLoadAttachmentRequest.AttachmentType.ATT.getType() : UpLoadAttachmentRequest.AttachmentType.RESOURCE.getType());
                    attachmentRequest.setPath(serverPath);
                    attachmentRequest.setFileName(fileName);
                    attachments.add(attachmentRequest);
                }
            }
            request.setAttachments(attachments);
        }
        // try {
        executeRequest(request, new SendMailResponse(), "sendMail", GlobalConstants.NO_ZIP_ONLY_JSON);

        // 09-13 15:46:58.003: I/C35Store(30179): ServerResponse :5,sendMail,100515,Command execute
        // failed.100515:找不到原邮件中的附件/Can't find the original mail attachments
        message.setFolderId(EmailApplication.MAILBOX_SENTBOX);
        message.setSendStats(C35Message.SEND_SUCCESS);
    }

	/**
	 * 发送请求，获得附件列表，包含附件各个属性值
	 * 
	 * @Description:
	 * @param account
	 * @param pageSize页大小
	 *            ; <=0时为默认值
	 * @param pageNo页数
	 *            ; <=0时为默认值
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-10
	 */
	public GetAttachmentListResponse getAttachmentListResponse(Account account, int pageSize, int pageNo) throws MessagingException {
		GetAttachmentListRequest request = new GetAttachmentListRequest();
		request.setUser(account.getEmail());
		if (pageSize != 0 && pageNo != 0) {
			request.setPageSize(pageSize);
			request.setPageNo(pageNo);
		}
		GetAttachmentListResponse response = null;
		try {
			response = (GetAttachmentListResponse) executeRequest(request, new GetAttachmentListResponse(), "searchAttachList", GlobalConstants.GZIP_BASE64);
		} catch (MessagingException e) {
			throw new MessagingException(MessagingException.COMMAND_ERROR_SEARCH_ATTACH_LIST);
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		// System.gc();
		return response;
	}

	/**
	 * 描述 执行普通（非Noop）命令的方法
	 * 
	 * @throws MessagingException
	 * @Title: executeRequest
	 * @author liujie
	 * @date 2011-11-23
	 * @param request
	 *            BaseRequest response BaseResponse ticket 票 commandName 命令名字 zip 是否压缩
	 * @return BaseResponse
	 * @throws
	 */
	private BaseResponse executeRequest(BaseRequest request, BaseResponse response, String commandName, int zip) throws MessagingException {
		try {
			execNoopCmd(commandName);
			// 初始化RequestCommand
			String requestCommand = initRequestCommand(request, ticket, commandName, zip);// sendMail
			// 执行 //3 sendMail 20131122102211308444 0 {"ask":0,"atta
			BaseResponse res = doExecuteCommand(response, requestCommand, zip);
			// retrtRequest = true;
			return res;
		} catch (MessagingException e) {
			// if (((e.getExceptionType() == MessagingException.RESPONSE_ERROR_FORMAT) ||
			// (e.getExceptionType() == MessagingException.RETURN_COMMAND_ERROR_LINK_TIMEOUT))) {// &&
			// retrtRequest
			// Debug.e(TAG, "连接已经断开,尝试重新连接服务器");
			// retrtRequest = false;
			// mTransport.close();
			// mTransport.open();
			// return executeRequest(request, response, commandName, zip);
			// } else if (e.getExceptionType() == MessagingException.CODE_AUTH_NO) {// && retrtRequest
			// Debug.e(TAG, "截获ticket异常，正在重试刚才的命令");
			// retrtRequest = false;
			// return executeRequest(request, response, commandName, zip);
			// } else {
			// Debug.e(TAG, "依旧失败");
			closeSocket();
			// retrtRequest = true;
			throw e;
		}
		// } finally {
		// if (!(commandName.equals("version") || commandName.equals("login") || commandName.equals("close")))
		// {
		// // close();
		// }
		// }
	}

	/**
	 * 描述 执行Noop命令的方法
	 * 
	 * @param request
	 * @param response
	 * @param commandName
	 * @param zip
	 * @return
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-12-11
	 */
	private BaseResponse executeNoopRequest(BaseRequest request, BaseResponse response, String commandName, int zip) throws MessagingException {
		try {
			// 初始化RequestCommand
			String requestCommand = initRequestCommand(request, ticket, commandName, zip);
			// 执行 //3 sendMail 20131122102211308444 0 {"ask":0,"atta
			BaseResponse res = doExecuteCommand(response, requestCommand, zip);
			// retrtRequest = true;
			return res;
		} catch (MessagingException e) {

			closeSocket();
			throw e;
		}

	}

	/**
	 * @throws MessagingException
	 * @Title: initRequestCommand
	 * @Description: 描述 初始化RequestCommand
	 * @author liujie
	 * @date 2011-11-30
	 * @return String
	 * @throws
	 */
	private String initRequestCommand(BaseRequest request, String ticket, String commandName, int zip) throws MessagingException {
		if ("getProxyServerDomain".equals(commandName)) {// dp特殊命令，此处不能改
			ticket = MailUtil.DP_SERVER_DOMAIN_PROTOCOLKEY;// getProxyServerDomain
		} else {
			if (!(commandName.equals("version") || commandName.equals("login"))) {
				if (!StringUtil.noOneEmpty(request, ticket, commandName)) {
					C35AppServiceUtil.writeSubscribeInformationToSdcard("request error:" + MessagingException.REQUEST_ERROR_FORMAT + " request: " + request + " ticket: " + ticket + " commandName: " + commandName);// 彩蛋log写入
					throw new MessagingException(MessagingException.REQUEST_ERROR_FORMAT,"请求格式错误");
				}
			}
		}
		String jsonParms = "";
		// 回去提成成员！！！！！！！！！！！！！！！！！！！！！！
		ArrayList<String> noJson = new ArrayList<String>();
		noJson.add("logout");
		noJson.add("close");
		ArrayList<String> noSuperField = new ArrayList<String>();
		noSuperField.add("version");
		noSuperField.add("getProxyServerDomain");
		if (StringUtil.isNotEmpty(commandName)) {
			if (noJson.contains(commandName)) { // 目的是給jsonParms保持空
			} else if (noSuperField.contains(commandName)) {// 需要json数据的命令集合,且不需要拿父类的属性
				jsonParms = " " + JsonUtil.toJson(request, false);
			} else { // 需要json数据的命令集合,且需要拿父类的属性
				jsonParms = " " + JsonUtil.toJson(request, true);
			}
			String requestCommand = "";
			if (StringUtil.isNotEmpty(ticket)) {
				// requestCommand = C35Transport.getCommandId() + " " + commandName + " " + ticket + " " +
				// (zip ? "1" : "0") + jsonParms;
				requestCommand = C35Transport.getCommandId() + " " + commandName + " " + ticket + " " + zip + jsonParms;
				Debug.i(TAG, "requestCommand = " + requestCommand);
			} else {
				// requestCommand = C35Transport.getCommandId() + " " + commandName + " " + commandName + " "
				// + (zip ? "1" : "0") + jsonParms;
				requestCommand = C35Transport.getCommandId() + " " + commandName + " " + commandName + " " + zip + jsonParms;
				Debug.i(TAG, "requestCommand = " + requestCommand);
			}
			return requestCommand;
		}
		return null;
	}

	/**
	 * 描述执行命令
	 * 
	 * @throws MessagingException
	 * @Title: doExecuteCommand
	 * @author liujie
	 * @date 2011-11-30
	 * @return BaseResponse
	 * @throws
	 */
	private BaseResponse doExecuteCommand(BaseResponse response, String requestCommand, int zip) throws MessagingException {
		synchronized (mDoExecuteCommand) {
			mTransport.openSocketAndStream();
			String responseResult = null;
			// int requestNum = Integer.parseInt(requestCommand.substring(0, requestCommand.indexOf(" ")));
			// String commandName = requestCommand.split(" ")[1];
			try {
				// long t = System.currentTimeMillis();
				// synchronized (response) {
				mTransport.writeLine(requestCommand);// 3 sendMail 20131122102211308444 0 {"ask
				Debug.d(TAG, "mTransport.writeLine _______________________is ok");
				responseResult = mTransport.readLine();
				// }
				// t= System.currentTimeMillis()-t;
				// new RecordSms(new LogData("Execute "+commandName, t)).run();
				Debug.i(TAG, "ServerResponse :" + responseResult);

			} catch (SocketTimeoutException ee) {
				// Debug.e("failfast", "failfast_AA", ee);
				// new RecordSms(new LogData("Execute " + requestCommand + " SocketTimeout", -1)).run();
				throw new MessagingException(MessagingException.RETURN_COMMAND_ERROR_LINK_TIMEOUT, "连接超时");
			} catch (IOException e) {
				// Debug.e("failfast", "failfast_AA", e);
				// new RecordSms(new LogData("Execute " + requestCommand + " IOException", -1)).run();
				throw new MessagingException(MessagingException.RETURN_COMMAND_ERROR_LINK_TIMEOUT, "与服务器通信失败");
			} catch (Exception e) {
				throw new MessagingException("读取响应结果出错" + e.getMessage());
			}
			if (StringUtil.isNotEmpty(responseResult) && responseResult.contains(",")) {
				responseResult = prcessResponse(responseResult, zip, requestCommand);// requestCommand为异常时调查用
				if (requestCommand.equals("recallMail")) {
					Debug.d(TAG, "邮件列表解锁" + responseResult);
				}
				Debug.d(TAG, "邮件列表解锁" + responseResult);
				response.initFeild(responseResult);
				return response;
			} else {
				if (requestCommand.substring(0, 30).contains("noop")) {// noop命令不用抛出异常
					return response;
				}
				if (requestCommand != null) {
					requestCommand = requestCommand.substring(0, requestCommand.length() < 400 ? requestCommand.length() : 400);
				}
				if (responseResult != null) {
					responseResult = responseResult.substring(0, responseResult.length() < 400 ? responseResult.length() : 400);
				}
				C35AppServiceUtil.writeSubscribeInformationToSdcard("requestCommand:" + requestCommand + MessagingException.RESPONSE_ERROR_FORMAT + " responseResult: " + responseResult);// 彩蛋log写入
				// new RecordSms(new LogData("Execute " + requestCommand + " response null", -1)).run();
//				throw new MessagingException(MessagingException.RESPONSE_ERROR_FORMAT, "服务器返回为空，或网络不稳定导致读取数据为-1");
				throw new MessagingException(MessagingException.RESPONSE_ERROR_FORMAT, "网络不稳定或收件人地址不正确，请分别检查。若还无法发送请联系技术人员。");
			}
		}
	}

	/**
	 * 解析命令返回结果
	 * 
	 * @Description:
	 * @param responseResult
	 * @param zip
	 *            是否压缩
	 * @return
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-5-15
	 */
	private String prcessResponse(String responseResult, int zip, String requestCommand) throws MessagingException {
		String temp = responseResult;
		int commandCode = -1;
		int commandNum = -1;
		String commandMessage = "";
		String commandName = "";
		try {
			commandNum = Integer.parseInt(responseResult.substring(0, responseResult.indexOf(",")));
			responseResult = responseResult.substring(responseResult.indexOf(",") + 1);
			if (responseResult.contains(",")) {
				commandName = responseResult.substring(0, responseResult.indexOf(","));
			}
			responseResult = responseResult.substring(responseResult.indexOf(",") + 1);
			if (responseResult.contains(",")) {
				commandCode = Integer.parseInt(responseResult.substring(0, responseResult.indexOf(",")));
			}
			commandMessage = responseResult = responseResult.substring(responseResult.indexOf(",") + 1);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			C35AppServiceUtil.writeSubscribeInformationToSdcard("prcessResponse NumberFormatException:" + MessagingException.RESPONSE_ERROR_FORMAT + " responseResult: " + responseResult);// 彩蛋log写入
			throw new MessagingException(MessagingException.RESPONSE_ERROR_FORMAT);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			C35AppServiceUtil.writeSubscribeInformationToSdcard("prcessResponse Exception:" + MessagingException.RESPONSE_ERROR_FORMAT + " responseResult: " + responseResult);// 彩蛋log写入
			throw new MessagingException(MessagingException.RESPONSE_ERROR_FORMAT);
		}
		if (processResponseCode(commandCode, requestCommand, responseResult)) {// 处理结果代码code的异常抛出
			switch (zip) {
			case GlobalConstants.GZIP_BASE64:
				return commandNum + "," + commandName + "," + commandCode + "," + reZipData(commandMessage);
			case GlobalConstants.GZIP_ENCRYPT_ENTER:
				try {
					JSONObject nJsonMessage = new JSONObject(commandMessage);
					int byteLenth = nJsonMessage.optInt("length");
					Debug.i("getmailid_data", "bytelenth:" + byteLenth);

					byte[] transize = mTransport.readFully(byteLenth);
					mTransport.clearLine();
					Debug.i("getmailid_data", "time__________________________time");
					String strData = new String(ZipUtil.uncompress(AesUtil.datadecrypt(transize, protocolKey.getBytes())));
					Debug.i("getmailid_data", "data:" + commandNum + "," + commandName + "," + commandCode + "," + strData);
					return commandNum + "," + commandName + "," + commandCode + "," + strData;

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					C35AppServiceUtil.writeSubscribeInformationToSdcard("processResponseCode JSONException:" + " commandMessage: " + commandMessage);// 彩蛋log写入
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					C35AppServiceUtil.writeSubscribeInformationToSdcard("processResponseCode IOException:" + " commandMessage: " + commandMessage);// 彩蛋log写入
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					C35AppServiceUtil.writeSubscribeInformationToSdcard("processResponseCode Exception:" + " commandMessage: " + commandMessage);// 彩蛋log写入
				}
				break;
			case GlobalConstants.GZIP_ENTER:

				break;
			}
			// if (zip) {// 是否需要解压缩
			// return commandNum + "," + commandName + "," + commandCode + "," + reZipData(commandMessage);
			// }
		}
		return temp;
	}

	/**
	 * 解压缩以及返回UTF-8编码格式
	 * 
	 * @Description:
	 * @param commandMessage
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-5-15
	 */
	private static String reZipData(String commandMessage) {
		// TODO Auto-generated method stub
		byte[] result = null;
		try {
			result = ZipUtil.uncompress(HexUtil.hex2byte(commandMessage));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Debug.e("failfast", "failfast_AA", e);
		}
		String ss = null;
		try {
			ss = new String(result, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Debug.e("failfast", "failfast_AA", e);
		}
		return ss;
	}

	public String getTiket() {
		return ticket;
	}

	// public void setTiket(String tiket) {
	// ticket = tiket;
	// }

	/**
	 * @throws MessagingException
	 * @Title: processResponseCode requestCommand 调查用 responseResult 调查用
	 * @Description: 处理响应码的工具方法
	 * @author liujie
	 * @date 2011-11-14
	 * @return boolean 是否是200
	 * @throws
	 */
	private boolean processResponseCode(int code, String requestCommand, String responseResult) throws MessagingException {
		boolean responseOK = false;
		// 只要不是200，就要关闭一下socket
		if (code != MessagingException.CODE_COMMAND_SUCCESS) {
			// closeSocket();
			C35AppServiceUtil.writeSubscribeInformationToSdcard("processResponseCode code:" + code + " requestCommand: " + requestCommand + " responseResult: " + responseResult);// 彩蛋log写入
		}
		switch (code) {
		case MessagingException.CODE_COMMAND_SUCCESS:
			responseOK = true;
			return responseOK;
		case MessagingException.CODE_COMMAND_NO:
			throw new MessagingException(MessagingException.CODE_COMMAND_NO, "命令没找到，不支持");
		case MessagingException.CODE_VERSION_NO:
			throw new MessagingException(MessagingException.CODE_VERSION_NO, "版本号不正确，不支持");
		case MessagingException.CODE_LOGIN_NO:
			throw new MessagingException(MessagingException.CODE_LOGIN_NO, "登录失败");
		case MessagingException.CODE_COMMAND_ERROR:
			throw new MessagingException(MessagingException.CODE_COMMAND_ERROR, "不支持该请求，命令格式错误");
		case MessagingException.CODE_PROGRAM_ERROR:
			throw new MessagingException(MessagingException.CODE_PROGRAM_ERROR, "服务器返回909错误");
		case MessagingException.CODE_CONNECT_ERROR:
			ticket = "";
			throw new MessagingException(MessagingException.CODE_CONNECT_ERROR, "连接错误");
		case MessagingException.UNFIND_MESSAGE:
			throw new MessagingException(MessagingException.UNFIND_MESSAGE, "找不到该条记录");
		case MessagingException.CODE_AUTH_NO:
			ticket = "";
			Debug.e(TAG, "ticket失效，需要重新获取");
			openAndGetTicket();
			Debug.e(TAG, "ticket失效，重新获取成功，抛出903异常");
			throw new MessagingException(MessagingException.CODE_AUTH_NO, "ticket失效需重试");
		case MessagingException.REQUEST_DATA_ERROE:
			throw new MessagingException(MessagingException.REQUEST_DATA_ERROE, "请求参数错误");
		case MessagingException.CODE_SEARCH_TIME_OUT_FOR_IMAP_ERROR:
			throw new MessagingException(MessagingException.CODE_SEARCH_TIME_OUT_FOR_IMAP_ERROR, "搜索邮件超时未响应");
		default:
			if (requestCommand != null) {
				requestCommand = requestCommand.substring(0, requestCommand.length() < 100 ? requestCommand.length() : 100);
			}
			throw new MessagingException(MessagingException.CODE_PROGRAM_ERROR, "未知错误" + code + "\n" + requestCommand);
		}
	}

	public String[] getHostInfo() {
		return new String[] { mTransport.getHost(), "" + mTransport.getPort() };
	}

	// @Override
	// public void checkSettings() throws MessagingException {
	// // TODO Auto-generated method stub
	// }

	@Override
	public Folder getFolder(Account account, String name) throws MessagingException {
		// TODO Auto-generated method stub
		return null;
	}

}
