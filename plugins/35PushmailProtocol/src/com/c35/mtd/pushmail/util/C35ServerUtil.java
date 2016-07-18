package com.c35.mtd.pushmail.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import com.c35.mtd.pushmail.C35MailThreadPool;
import com.c35.mtd.pushmail.C35MailThreadPool.ENUM_Thread_Level;
import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.GlobalConstants;
import com.c35.mtd.pushmail.R;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.logic.C35AccountManager;

/**
 * 与网络通信 1、初始化时与手机客户端管理平台服务器做用户校验 2、用户设置邮件推送时，通过手机客户端管理平台服务器控制服务器端的push开关
 * 
 * @Description:
 * @author:liujie
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-3-14
 */
public class C35ServerUtil {

//	private static Account mAccount;//正在验证的account
	private static String errorMessage = "";
	private static final String TAG = "C35ServerUtil";
	public static boolean PUSH = true;
	private static final String CHECK_NEW_VERSION_ERROR = "404";
	private static final String Action_Update = "com.c35.mtd.pushmail.newversion";

	static String resultXml = "";
	private static final String FORCE_UPDATE = "1";
	private static Socket client = null; // 非35邮箱连接35以外服务器的socket
	private static BufferedInputStream is = null;// 非35邮箱连接35以外服务器的BufferedInputStream
	private static BufferedReader sockin = null;// 非35邮箱连接35以外服务器的BufferedReader
	private static BufferedOutputStream os = null;// 非35邮箱连接35以外服务器的BufferedOutputStream
	private static PrintWriter sockout = null;// 非35邮箱连接35以

//	/**
//	 * 检查新版本的方法
//	 * 
//	 * @Description:
//	 * @param context
//	 * @see:
//	 * @since:
//	 * @author: liujie
//	 * @date:2012-3-14
//	 */
//	public static boolean isCheckUpdateVersion(Context context) {
//		String pkgName = EmailApplication.getInstance().getPackageName();
//		// 设备型号
//		String mod = Build.MODEL;
//		// MEID/IMEI号
//		String meid = ((TelephonyManager) EmailApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
//		// 版本
//		int versionCode = 0;
//		try {
//			versionCode = EmailApplication.getInstance().getPackageManager().getPackageInfo(pkgName, 0).versionCode;
//		} catch (NameNotFoundException e) {
//			e.printStackTrace();
//			Debug.e("failfast", "failfast_AA", e);
//		}
//		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(4);
//		nameValuePair.add(new BasicNameValuePair("pkgName", pkgName));
//		nameValuePair.add(new BasicNameValuePair("pkgVersion", Integer.toString(versionCode)));
//		nameValuePair.add(new BasicNameValuePair("mod", mod));
//		nameValuePair.add(new BasicNameValuePair("meid", meid));
//		String responseString;
//		try {
//			responseString = sendDateToServer(C35OpenCheckUpdateVersion.UPDATE_URI, nameValuePair);
//			Debug.v(TAG, "检查新版本 :" + responseString);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//
//		// TODO: check this
//		if (responseString == null)
//			return false;
//		if (responseString.contains(CHECK_NEW_VERSION_ERROR)) {
//			Debug.v(TAG, "检查新版本 :" + CHECK_NEW_VERSION_ERROR);
//			// SharedPreferences sp = context.getSharedPreferences(GlobalConstants.PUSHMAIL_PREF_FILE, 0);
//			// sp.edit().putBoolean(GlobalConstants.NEW_VERSION, false).commit();
//			return true;
//		} else {
//			if (responseString.contains("http://")) {
//				// TODO hanchunxue 后台下载新版本
//				// SharedPreferences sp = context.getSharedPreferences(GlobalConstants.PUSHMAIL_PREF_FILE, 0);
//				// sp.edit().putBoolean(GlobalConstants.NEW_VERSION, true).commit();
//				Debug.v(TAG, "response string is :" + responseString);
//				String force = responseString.split(",")[1];
//				C35UpdateDialogActivity.actionUpdateDialog(context, force.equals(FORCE_UPDATE) ? EmailApplication.getInstance().getString(R.string.update_dialog_msg1) : context.getString(R.string.update_dialog_msg2), force.equals(FORCE_UPDATE), responseString.split(",")[0]);
//
//				return true;
//			}
//		}
//		return false;
//	}

	/**
	 * 发送POST请求的方法
	 * 
	 * @Description:
	 * @param url
	 * @param nameValuePair
	 * @return
	 * @throws Exception
	 * @see:
	 * @since:
	 * @author: liujie
	 * @date:2012-3-14
	 */
	public static String sendDateToServer(String url, List<NameValuePair> nameValuePair) throws Exception {
//		SystemClock.sleep(1000*30);
		DefaultHttpClient httpClient = new DefaultHttpClient();

		HttpPost httpPost = new HttpPost(url);

		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair, "UTF-8"));
		// TODO 状态处理 500 200
		int res = 0;
		HttpResponse httpResponse = httpClient.execute(httpPost);
		res = httpResponse.getStatusLine().getStatusCode();
		if (res == 200) {
			/*
			 * 当返回码为200时，做处理 得到服务器端返回json数据，并做处理
			 */
			StringBuilder builder = new StringBuilder();
			BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
			for (String s = bufferedReader2.readLine(); s != null; s = bufferedReader2.readLine()) {
				builder.append(s);
			}

			Debug.i("cat", ">>>>>>" + builder.toString());
			return builder.toString();

		}

		return null;
	}

	/**
	 * 做手机客户端管理平台服务器端的校验并且解析结果是否是Push账户，是则允许使用35Mail，只在入口调用
	 * 
	 * @param emailAddress
	 * @return boolean 是否通过验证
	 * @throws Exception
	 * @throws IOException
	 */
	public static void checkPush(Account account, String emailAddress) throws MessagingException {
		try {
			resultXml = sendPost(emailAddress, account.getHostIP());
			C35AppServiceUtil.writeSubscribeInformationToSdcard("checkPush resultXml:" + resultXml + " emailAddress:" + emailAddress);// 彩蛋log写入
			if (resultXml != null && !resultXml.trim().equals("")) {//不为空时执行以下
				Debug.v("checkPush", "resultXml = " + resultXml);
				parseXmlAndSave(account, resultXml);
			}
		} catch (Exception e) {
			throw new MessagingException(MessagingException.LOGIN_CHECKPUSH_ERROR,e.getMessage());
		}
	}

	public static void showNotPushDialog(Context ctx1) {
		/*
		final Context ctx = ctx1;
		MailDialog.Builder builder = new MailDialog.Builder(ctx);
		if (errorMessage != null && !errorMessage.equals("")) {
			builder.setMessage(errorMessage);
			errorMessage = "";
		} else {
			builder.setMessage(ctx.getString(R.string.check_no_push));
		}
		builder.setNegativeButton(ctx.getString(R.string.okay_action), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		builder.create().show();
        */
	    // Modified by LL
	    // BEGIN
        if (errorMessage != null && !errorMessage.equals("")) {
            Log.e(TAG, errorMessage);
        } else {
            Log.e(TAG, ctx1.getString(R.string.check_no_push));
        }
        // END
	}

	/**
	 * 存储push信息
	 * 
	 * @Description:
	 * @param account
	 * @param xmlResult
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-10-9
	 */
	public static void parseXmlAndSave(Account account, String xmlResult) {
//		mAccount = account;
		if (xmlResult != null && xmlResult.length() > 0) {
			if (xmlResult.startsWith("1|1|1")) {
				account.setPushOpen(true);
				account.setLocalPushOpen(true);
			} else if (xmlResult.startsWith("1|1|0")) {
				if (account.isPushOpen()) {
					String email = account.getEmail();
					String content = " , " + email + " , UnregisterIPPush , Send  Code: C35ServerUtil.parseXml";
					C35AppServiceUtil.writeSubscribeInformationToSdcard(content);
					C35AppServiceUtil.unregisterIPPush(email);
				}
				account.setPushOpen(false);
				errorMessage = EmailApplication.getInstance().getString(R.string.check_no_push);
			} else if (xmlResult.startsWith("1|0")) {
				account.setPushOpen(false);
				account.setLocalPushOpen(false);
				errorMessage = EmailApplication.getInstance().getString(R.string.login_35Account_account_closed);
			} else if (xmlResult.startsWith("1|2")) {
				account.setPushOpen(false);
				account.setLocalPushOpen(false);
				errorMessage = EmailApplication.getInstance().getString(R.string.login_35Account_account_unusered);
			} else if (xmlResult.startsWith("1|3")) {
				account.setPushOpen(false);
				account.setLocalPushOpen(false);
				errorMessage = EmailApplication.getInstance().getString(R.string.login_35Account_account_closed);
			}else{//出现异常状况，默认开启
				C35AppServiceUtil.writeSubscribeInformationToSdcard("xmlResult"+xmlResult);// 彩蛋log写入
				account.setPushOpen(true);
				account.setLocalPushOpen(true);
			}
		}else{//出现异常状况，默认开启
			C35AppServiceUtil.writeSubscribeInformationToSdcard("xmlResult null！"+xmlResult);// 彩蛋log写入
			account.setPushOpen(true);
			account.setLocalPushOpen(true);
		}
		// mAccount.save_checkPush_xmlResult(C35AccountManager.getInstance(), mAccount.getUuid(), xmlResult);
		account.setCheckPush_xmlResult(xmlResult);
		account.save(C35AccountManager.getInstance(), false);
	}

	/**
	 * 做手机客户端管理平台服务器端的校验
	 * 
	 * @param emailAddress
	 * @param  strHostIP
	 * @return
	 * @throws IOException
	 */
	private static String sendPost(String emailAddress, String strHostIP) throws Exception {
		if (emailAddress.endsWith(MailUtil.DOMAIN_SZDEP)||"192.168.1.112".equals(MailUtil.DP_SERVER_DOMAIN_HOST)) {
			return "1|1|1|1";
		}
		
		// if (emailAddress.endsWith(MailUtil.EMAIL_SUFFIX_35CN)) {
		// emailAddress = emailAddress.replace(MailUtil.EMAIL_SUFFIX_35CN,
		// MailUtil.EMAIL_SUFFIX_CHINACHANNEL);
		// }
		emailAddress = MailUtil.convert35CNToChinaChannel(emailAddress);
		if (emailAddress != null && emailAddress.length() > 0) {
			String[] temp = emailAddress.split("@");
			String domain = temp[1];
			String requestUrl = "";
			// if (domain.equalsIgnoreCase(MailUtil.DOMAIN_CHECK)) {
			requestUrl = MailUtil.CHECK_ACCOUNT_ADRESSE.replace(MailUtil.PROXY_SERVER_HOST, strHostIP);// ProxyServer的主机(必填)
			// } else {
			// requestUrl = "http://mail." + domain + MailUtil.CHECK_ACCOUNT_RELADR;
			// }
			Debug.i("==", requestUrl);// http://mail.china-channel.com:8088/servlet/PushAction
			Map<String, String> requestParams = new HashMap<String, String>();
			requestParams.put("action", "valid");
			requestParams.put("user", emailAddress);
			StringBuilder params = new StringBuilder();
			for (Map.Entry<String, String> entry : requestParams.entrySet()) {
				params.append(entry.getKey());
				params.append("=");
				params.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
				params.append("&");
			}
			if (params.length() > 0) {
				params.deleteCharAt(params.length() - 1);
			}
			Debug.i(TAG, "params:" + params.toString());
			byte[] data = params.toString().getBytes();
			URL realUrl = new URL(requestUrl);// http://mail.china-channel.com:8088/servlet/PushAction
			HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();// http://mail.magic.35.com/servlet/PushAction
			conn.setDoOutput(true);// 发送POST请求必须设置允许输出
			conn.setUseCaches(false);// 不使用Cache
			conn.setConnectTimeout(MailUtil.SOCKET_CONN_TIMEOUT_TIME);
			conn.setReadTimeout(MailUtil.SOCKET_READ_TIMEOUT_TIME);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Content-Length", String.valueOf(data.length));
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			DataOutputStream outStream = null;
			try {
				outStream = new DataOutputStream(conn.getOutputStream());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				if (conn != null) {
					conn.disconnect();
				}
				if (outStream != null) {
					outStream.close();
				}
				e.printStackTrace();
				throw new Exception(e);
			}
			if (outStream != null) {
				outStream.write(data);
				outStream.flush();
				if (conn.getResponseCode() == 200) {
					String result = readAsString(conn.getInputStream(), "UTF-8");
					// outStream.close();
					return result;
				}
			}
			return null;
		} else {
			return null;
		}

	}

	// 关闭连接
	private static void close() {
		try {
			if (is != null) {
				is.close();
			}
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			try {
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Debug.e("failfast", "failfast_AA", e);
			} finally {
				try {
					if (client != null) {
						client.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Debug.e("failfast", "failfast_AA", e);
				} finally {
					is = null;
					os = null;
					client = null;
				}
			}
		}
	}

	// 判断连接是否打开
	private static boolean isOpen() {
		// TODO Auto-generated method stub
		return (is != null && os != null && client != null && client.isConnected() && !client.isClosed());
	}

	/**
	 * 
	 * @Description:判断命令是否执行成功，
	 * @param:
	 * @return:
	 * @throws:
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2012-9-11
	 */
	public static boolean isResponseOk(String responseStr) {
		String responseJson = responseStr;// 3,downloadData,200,{"totalBlock":1,"dataContent":"789C9CB765501C51132EC2CFFD93CCA3753","currentBlock":1}
		responseJson = responseJson.substring(responseJson.indexOf(",") + 1);// downloadData,200,{"totalBlock":1,"dataContent":"789C9CB765501C51132EC2CFFD93CCA3753","currentBlock":1}
		responseJson = responseJson.substring(responseJson.indexOf(",") + 1);// 200,{"totalBlock":1,"dataContent":"789C9CB765501C51132EC2CFFD93CCA3753","currentBlock":1}
		if (responseJson.startsWith("200")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 开关本地push开关的方法
	 * 
	 * @param emailAddress
	 *            用户邮箱地址
	 * @param isOpenPush
	 *            1 告诉服务器用户要开启push 0 关闭
	 * @return 是否开启成功
	 * @throws IOException
	 */
	public static boolean localPushControl(Account account, String emailAddress, String isOpenPush) throws Exception {
		String domainType = account.getDomainType();
		if(domainType!=null&&domainType.equals("1")){//账号类型为代收
			return true;
		}
		if (emailAddress != null && emailAddress.length() > 0) {
			String safecode = "";
			String safeString = "@$#@$@#(**^^$%#%";
			safecode = Md5(emailAddress + safeString);
			emailAddress = MailUtil.convert35CNToChinaChannel(emailAddress);
			// String[] temp = emailAddress.split("@");
			// String domain = temp[1];
			// String requestUrl = "http://mail." + domain + MailUtil.CHECK_ACCOUNT_RELADR;
			String requestUrl = "http://" + account.getHostIP() + MailUtil.CHECK_ACCOUNT_RELADR;
			Map<String, String> requestParams = new HashMap<String, String>();
			requestParams.put("action", "setopen");
			requestParams.put("user", emailAddress);
			requestParams.put("safecode", safecode);
			requestParams.put("state", isOpenPush);
			StringBuilder params = new StringBuilder();
			for (Map.Entry<String, String> entry : requestParams.entrySet()) {
				params.append(entry.getKey());
				params.append("=");
				params.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
				params.append("&");
			}
			if (params.length() > 0) {
				params.deleteCharAt(params.length() - 1);
			}
			byte[] data = params.toString().getBytes();
			URL realUrl = new URL(requestUrl);
			HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
			conn.setDoOutput(true);// 发送POST请求必须设置允许输出
			conn.setUseCaches(false);// 不使用Cache
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Content-Length", String.valueOf(data.length));
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			DataOutputStream outStream = null;
			try {
				outStream = new DataOutputStream(conn.getOutputStream());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				if (conn != null) {
					conn.disconnect();
				}
				if (outStream != null) {
					outStream.close();
				}
				throw e;
			}
			if (outStream != null) {
				outStream.write(data);
				outStream.flush();
				if (conn.getResponseCode() == 200) {
					String result = readAsString(conn.getInputStream(), "UTF-8");
					// outStream.close();
					return true;
				}
			}
			return false;
		} else {
			return false;
		}

	}

	/**
	 * 对安全码进行MD5加密
	 * 
	 * @param plainText
	 * @return MD5加密后的串
	 */
	private static String Md5(String plainText) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			return buf.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		}
		return null;
	}

	/**
	 * 读流里的数据做成字符串返回
	 * 
	 * @param is
	 * @param string
	 * @return
	 * @throws IOException
	 */
	private static String readAsString(InputStream is, String string) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = is.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		is.close();
		return new String(outStream.toByteArray(), "utf-8");
	}

	/**
	 * 注册个人试用账户
	 * 
	 * 请求URL的格式：（以get方式发起请求）
	 * 
	 * http://{EIS服务器IP或对应域名}:7799/eis/GetCheck?method={方法名}&data={加密后数据}&checkStr={请求验证字符串}。
	 * 
	 * 2、方法名：registerEmployee
	 * 
	 * 加密后数据： name:【】|email:【】|password:【】|clientIP:【】|mailSize:【】|clientType:【】
	 * 
	 * 请求验证字符串 = 对以下串进行md5加密：
	 * {方法名}name:【】|email:【】|password:【】|clientIP:【】|mailSize:【】|clientType:【】d*Ddgfadf&^df~d
	 * 
	 * 
	 * 注：【】里的数据进行base64加密；请求URL里不包含{}
	 * 
	 * @Description:
	 * @param emailAddress
	 * @param pwd
	 * @param ip
	 * @param type
	 * @return
	 * @throws IOException
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-11-20
	 */
	public static int regTryAccount(String name, String emailAddress, String pwd, String length, String type) throws IOException {

		int result = 4;// 初始化结果是错误的

		if (emailAddress != null && emailAddress.length() > 0) {
		    /*
			String safeString = ExperienceRegPersionActivity.SAFE_STRING;// 新安全码
			String serverDomain = ExperienceRegPersionActivity.SERVER_DOMIN;// EIS服务器IP或对应域名
			String methordName = ExperienceRegPersionActivity.METHORD_NAME;// 方法名
			*/
		    // Modified by LL
		    // BEGIN
		    String safeString = "DS4rd3drsa^(~^7d";// 新安全码
		    String serverDomain = "mail.try.35.cn";// EIS服务器IP或对应域名
		    String methordName = "registerEmployee";// 方法名
		    // END
			
			String data = "";// 加密后数据
			String checkKey = "";// 请求验证字符串

			// Base64加密，切记此处需用此参数Base64.NO_WRAP，不然md5加密结果不正确！
			name = Base64.encodeToString(name.getBytes(), Base64.NO_WRAP);
			emailAddress = Base64.encodeToString(emailAddress.getBytes(), Base64.NO_WRAP);
			pwd = Base64.encodeToString(pwd.getBytes(), Base64.NO_WRAP);
			length = Base64.encodeToString(length.getBytes(), Base64.NO_WRAP);
			type = Base64.encodeToString(type.getBytes(), Base64.NO_WRAP);

			// 构造加密后数据
			StringBuilder params = new StringBuilder();
			params.append("name:" + name + "|");
			params.append("email:" + emailAddress + "|");
			params.append("password:" + pwd + "|");
			params.append("mailSize:" + length + "|");
			params.append("type:" + type);

			data = params.toString();
			String a = methordName + data + safeString;
			checkKey = Md5(a);// MD5加密后得请求验证字符串
			// Debug.v(TAG, "data : " + data);
			// Debug.v(TAG, "checkKey : " + checkKey);
			String requestUrl = "http://" + serverDomain + MailUtil.TRY_ACCOUNT_CHECK_URL_EIS + methordName + "&data=" + data + "&checkStr=" + checkKey;// String直接相加不影响效率
			Debug.v(TAG, "reg try account requestUrl : " + requestUrl);
			URL realUrl = new URL(requestUrl);
			HttpURLConnection conn = null;
			try {
				conn = (HttpURLConnection) realUrl.openConnection();
				conn.setConnectTimeout(MailUtil.SOCKET_CONN_TIMEOUT_TIME);// 设置连接超时6 * 1000
				conn.setRequestMethod("GET");// 以get方式发起请求
				if (conn.getResponseCode() == 200) {
					InputStream is = conn.getInputStream();// 得到网络返回的输入流
					result = getResultFromRegTry(readAsString(is, "UTF-8"));
				}
			} catch (Exception e) {
				e.printStackTrace();
				result = 5;// 连接错误（超时等）
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}
		}
		return result;
	}

	/**
	 * 判断注册个人体验账户的返回值，返回结果转为int
	 * 
	 * @Description:
	 * @param strResult
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-11-20
	 */
	private static int getResultFromRegTry(String strResult) {
		Debug.v(TAG, "reg try account result string : " + strResult);
		int result = 4;// 初始化结果是错误的
		try {
			JSONObject jo = new JSONObject(strResult);
			if (jo.get("result").equals("200")) {
				// 成功
				result = 0;
			} else if (jo.get("result").equals("1")) {
				// 账户已存在
				result = 1;
			} else if (jo.get("result").equals("2")) {
				// 域不存在(email格式错误未包括domain信息、域不存在)
				result = 2;
			} else if (jo.get("result").equals("3")) {
				// 密码格式错误
				result = 3;
			} else if (jo.get("result").equals("4")) {
				// 其它错误
				result = 4;
			} else {
				// 其它错误
				Debug.e(TAG, "Register Try Account Error: " + jo.get("result"));
				result = 4;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = 4;
		}
		return result;
	}

	/**
	 * 开通试用域
	 * 
	 * @Description:
	 * @param domain
	 * @param serverUrl
	 * @return 0:请求失败 -1:连接错误,超时等 -2：返回值错误或返回失败 -3: 服务器返回失败 1：已经存在 2：成功
	 * @throws IOException
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-12-24
	 */
	public static int regTryDomain(String domain, String serverUrl, String checkStr) throws IOException {

		int result = 0;// 初始化为请求失败

		if (domain != null && domain.length() > 0) {
			String checkStrMd5 = Md5(checkStr);
			// String requestUrl = serverUrl + "&domain=" + domain + "&checkStr=" + checkStrMd5;//
			// String直接相加不影响效率
			// Debug.v(TAG, "checkStr=" + checkStr);
			String requestUrl = serverUrl + "&domain=" + domain + "&checkStr=" + checkStrMd5;// String直接相加不影响效率
			// Debug.v(TAG, "reg try Domain requestUrl : " + requestUrl);// 注释掉，因为未加密
			URL realUrl = new URL(requestUrl);
			HttpURLConnection conn = null;
			try {
				conn = (HttpURLConnection) realUrl.openConnection();
				conn.setConnectTimeout(MailUtil.SOCKET_CONN_TIMEOUT_TIME);// 设置连接超时
				conn.setRequestMethod("GET");// 以get方式发起请求
				if (conn.getResponseCode() == 200) {
					InputStream is = conn.getInputStream();// 得到网络返回的输入流
					result = getResultFromRegDomain(readAsString(is, "UTF-8"));
				}
			} catch (Exception e) {
				e.printStackTrace();
				result = -1;// 连接错误（超时等）
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}
		}
		return result;
	}

	/**
	 * 开通试用域内账户
	 * 
	 * @Description:
	 * @param userName
	 * @param pwd
	 * @param mobile
	 * @param serverUrl
	 * @return 0:请求失败 -1:连接错误,超时等 -2：返回值错误或返回失败 -3: 服务器返回失败 1：已经存在 2：成功
	 * @throws IOException
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-12-24
	 */

	public static int regTryDomainAccount(String userName, String pwd, String mobile, String serverUrl, String checkStr) throws IOException {

		int result = 0;// 初始化为请求失败

		String requestUrl = "";

		if (mobile != null && mobile.length() > 0) {
			requestUrl = serverUrl + "&userName=" + userName + "&userPwd=" + pwd + "&mobile=" + mobile;// String直接相加不影响效率
		} else {
			requestUrl = serverUrl + "&userName=" + userName + "&userPwd=" + pwd;// String直接相加不影响效率
		}

		String checkStrMd5 = Md5(checkStr);
		// Debug.v(TAG, "checkStr=" + checkStr);
		requestUrl = requestUrl + "&checkStr=" + checkStrMd5;
		// Debug.v(TAG, "reg try domain account requestUrl : " + requestUrl);//注释掉，因为未加密
		URL realUrl = new URL(requestUrl);
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) realUrl.openConnection();
			conn.setConnectTimeout(MailUtil.SOCKET_CONN_TIMEOUT_TIME);// 设置连接超时
			conn.setRequestMethod("GET");// 以get方式发起请求
			if (conn.getResponseCode() == 200) {
				InputStream is = conn.getInputStream();// 得到网络返回的输入流
				result = getResultFromRegDomain(readAsString(is, "UTF-8"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = -1;// 连接错误（超时等）
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return result;
	}

	/**
	 * 解析注册试用域以及域内用户的返回值
	 * 
	 * @Description:
	 * @param strResult
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-12-24
	 */
	private static int getResultFromRegDomain(String strResult) {
		Debug.v(TAG, "reg try Domain result string : " + strResult);
		int result = -2;// 初始化结果是错误的
		try {
			JSONObject jo = new JSONObject(strResult);

			if (jo.getBoolean("success")) {

				JSONObject joResult = jo.getJSONObject("result");

				if (joResult.get("isHas").equals("1")) {
					// 已经存在
					result = 1;
				} else if (joResult.get("isHas").equals("0")) {
					// 不存在过，且成功
					result = 2;
				}
			} else {
				result = -3;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			result = -2;
		}
		return result;
	}

	/**
	 * 
	 * @Description:下载背景图片
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:Jul 18, 2013
	 */
	public static void getBackgroundImage() {
		C35MailThreadPool.getInstance(ENUM_Thread_Level.TL_common).submit(new Runnable() {

			public void run() {
				// 在配置文件中记录版本和最后下载时间
				SharedPreferences sp = EmailApplication.getInstance().getSharedPreferences(GlobalConstants.PUSHMAIL_PREF_FILE, 0);
				String version = sp.getString(GlobalConstants.BACKGROUND_IMAGE_VERSION, "");// 版本
				long lasttime = sp.getLong(GlobalConstants.LAST_GET_IMAGE_TIME, 0);// 最近一次下载时间
				if (lasttime == 0 || System.currentTimeMillis() - lasttime > GlobalConstants.INTERVAL_TIME) {
					// Modified by LL
				    // BEGIN
				    /*
				    if (!ShowNoConnectionActivity.isConnectInternet()) {
						return;
					}
					*/
				    // END
				    
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();// http请求参数
					nameValuePairs.add(new BasicNameValuePair("os", "android"));
					nameValuePairs.add(new BasicNameValuePair("ver", version));
					// nameValuePairs.add(new BasicNameValuePair("lan", "zh_CN"));

					try {
						String getImageStr = sendDateToServer(GlobalConstants.BACKGROUND_IMAGE_URL, nameValuePairs);
						if (getImageStr != null) {// {"isNew":"1","ver":"1","st":"2013-07-01","et":"2013-07-31","imgs":["http://y.35.com/35pushmail/1_Kobe.jpg"]}
							Debug.i(TAG, "getImageStr___is:" + getImageStr);// {"isNew":"0"}
							JSONObject getImageJson = new JSONObject(getImageStr);// {"imgs":["http:\/\/y.35.com\/35pushmail\/1_Kobe.jpg"],"st":"2013-07-01","ver":"1","et":"2013-07-31","isNew":"1"}
							if ("1".equals(getImageJson.optString("isNew"))) {
								version = getImageJson.optString("ver");
								String realImageUrl = getImageJson.getJSONArray("imgs").getString(0);
								String starttime = getImageJson.optString("st");
								String endtime = getImageJson.optString("et");

								if (downloadFile(realImageUrl, GlobalConstants.BACKGROUND_SPLASH_IMAGE)) {
									Debug.i(TAG, "download_______is ok");
									Editor edit = sp.edit();
									edit.putLong(GlobalConstants.LAST_GET_IMAGE_TIME, System.currentTimeMillis());
									edit.putString(GlobalConstants.BACKGROUND_IMAGE_VERSION, version);
									edit.putString(GlobalConstants.BACKGROUND_START_TIME, starttime);
									edit.putString(GlobalConstants.BACKGROUND_END_TIME, endtime);
									edit.commit();
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * 
	 * @Description: 下载方法
	 * @param downloadUrl
	 * @param file
	 * @return
	 * @throws Exception
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:Jul 18, 2013
	 */
	public static boolean downloadFile(String downloadUrl, String file) throws Exception {
		HttpURLConnection httpConnection = null;
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			URL url = new URL(downloadUrl);
			httpConnection = (HttpURLConnection) url.openConnection(); // 创建连接
			httpConnection.setConnectTimeout(10000);
			httpConnection.setReadTimeout(20000);
			if (httpConnection.getResponseCode() == 404) {// 判断返回值
				throw new Exception();
			}
			is = httpConnection.getInputStream();
			String path = EmailApplication.getInstance().getFilesDir().toString();
			String realpath = path + file;
			fos = new FileOutputStream(realpath, false);
			byte buffer[] = new byte[4096];
			int readsize = 0;
			while ((readsize = is.read(buffer)) > 0) {
				fos.write(buffer, 0, readsize);
			}
			return true;
		} catch (Exception ex) {
			return false;
		} finally {
			if (httpConnection != null) {
				httpConnection.disconnect();
			}
			if (is != null) {
				is.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}
}
