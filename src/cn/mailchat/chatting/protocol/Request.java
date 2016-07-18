package cn.mailchat.chatting.protocol;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.io.output.CountingOutputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.http.HttpResponseCache;
import android.util.Log;

import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.utils.EncryptUtil;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.utils.Utility;



/**
 * 请求
 * 
 * @Description:
 * @author:xulei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:Jan 9, 2014
 */
public class Request {

	private static final int TIME_OUT = 8 * 1000;
	private static final String CHARSET = "UTF-8";
	private static final String BOUNDARY_MARK = "--";
	private static final String LINE_END = "\r\n";
	private static final String QUOTATION_MARK = "\"";
	private static final int BUFFER_SIZE = 4 * 1024;
	private static final String POST = "POST";
	private static final String GET = "GET";
	private static final String CONNECTION ="close";

	static{
		SSLContext sslContext = null;
        try {

			KeyStore ts = KeyStore.getInstance("bks");
			ts.load(MailChat.app.getResources().openRawResource(R.raw.mailchat),
					"com35xm2014ptc".toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
			tmf.init(ts);
			TrustManager[] tm = tmf.getTrustManagers();
			sslContext = SSLContext.getInstance("TLSv1");
			sslContext.init(null, tm, null);
			sslContext.getSocketFactory();

		} catch (Exception e) {
			e.printStackTrace();
		}
		if (sslContext != null) {
			HttpsURLConnection.setDefaultHostnameVerifier(new Request().new NullHostNameVerifier());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
					.getSocketFactory());
		}

    }
	
	public class NullHostNameVerifier implements HostnameVerifier {

	    public boolean verify(String hostname, SSLSession session) {
	        return true;
	    }
	}

	/**
	 *
	 * @Description:
	 * @param requestUrl
	 * @return
	 * @throws IOException
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-13
	 */
	public static HttpURLConnection prepareHttpsConnection(String requestUrl, String method) throws IOException {
		URL url = new URL(requestUrl);
		HttpURLConnection connection = (HttpsURLConnection) url.openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(method.equalsIgnoreCase(POST));
		connection.setUseCaches(false);
		connection.setRequestMethod(method);
		connection.setReadTimeout(TIME_OUT);
		connection.setConnectTimeout(TIME_OUT);
		connection.setRequestProperty("Charset", CHARSET);
		connection.setRequestProperty("Connection", CONNECTION);
		return connection;
	}

	/**
	 *
	 * @Description:
	 * @param requestUrl
	 * @return
	 * @throws IOException
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-3-25
	 */
	public static HttpURLConnection prepareHttpConnection(String requestUrl, String method) throws IOException {
		URL url = new URL(requestUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(method.equalsIgnoreCase(POST));
		connection.setUseCaches(false);
		connection.setRequestMethod(method);
		connection.setReadTimeout(TIME_OUT);
		connection.setConnectTimeout(TIME_OUT);
		connection.setRequestProperty("Charset", CHARSET);
		connection.setRequestProperty("Connection", CONNECTION);
		return connection;
	}

	public static Response get(String requestUrl, Map<String, String> params) {
		Response response = new Response();
		try {
			StringBuilder builder = new StringBuilder();
			if (params != null) {
				builder.append("?");
				for (Map.Entry<String, String> entry : params.entrySet()) {
					builder.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), CHARSET)).append("&");
				}
				builder.deleteCharAt(builder.length() - 1);
			}
			String url = requestUrl + builder.toString();
			HttpURLConnection connection = prepareHttpsConnection(url, GET);
			Log.i(MailChat.LOG_COLLECTOR_TAG,"HTTPS(GET) : "+StringUtil.removePartCharacters("https://api.mailchat.cn:80/", requestUrl)+" ==> 请求:"+url.getBytes().length);
			int code = connection.getResponseCode();
			response.setCode(code);
			if (code == 200) {
				response.setInputStream(connection.getInputStream());
			}
		} catch (IOException e) {
			e.printStackTrace();
			response.setCode(Response.ANTHER_ERROR);
		}
		return response;
	}

	/**
	 * 执行post请求
	 * 
	 * @Description:
	 * @param requestUrl
	 *            请求的地址
	 * @param params
	 *            请求参数
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jan 10, 2014
	 */
	public static Response post(String requestUrl, Map<String, String> params) {
		Response response = new Response();
		try {
			StringBuilder builder = new StringBuilder();
			if (params != null) {
				for (Map.Entry<String, String> entry : params.entrySet()) {
					builder.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), CHARSET)).append("&");
				}
				if(builder.length()>0){
					builder.deleteCharAt(builder.length() - 1);
				}
			}
			byte[] data = builder.toString().getBytes(CHARSET);
			HttpURLConnection connection = prepareHttpsConnection(requestUrl, POST);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", String.valueOf(data.length));
			DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
			CountingOutputStream countingOutputStream =new CountingOutputStream(outStream);
			countingOutputStream.write(data);
			countingOutputStream.flush();
			Log.i(MailChat.LOG_COLLECTOR_TAG,"HTTPS(POST) : "+StringUtil.removePartCharacters("https://api.mailchat.cn:80/", requestUrl)+" ==> 请求:"+countingOutputStream.getByteCount());
			countingOutputStream.close();
			int code = connection.getResponseCode();
			response.setCode(code);
			if (code == 200) {
				response.setInputStream(connection.getInputStream());
			}
		} catch (IOException e) {
			e.printStackTrace();
			response.setCode(Response.ANTHER_ERROR);
		}
		return response;
	}
	/**
	 * 执行post请求，并上传文件
	 * 
	 * @Description:
	 * @param requestUrl
	 * @param params
	 * @param file
	 * @param requestCommand
	 * @return 
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jan 10, 2014
	 */
	public static Response postWithFile(String requestUrl, File file,Map<String, String> params, String requestCommand,String attchmentId,UploadCallback uploadCallback) {
		Response response = new Response();
		try {
			HttpURLConnection connection = prepareHttpsConnection(requestUrl, POST);
			StringBuilder builder = new StringBuilder();
			String boundary = UUID.randomUUID().toString();
			// header
			connection.setRequestProperty("Content-Type", "multipart/form-data" + ";boundary=" + boundary);
			// file
			if (params != null) {
				for (Map.Entry<String, String> entry : params.entrySet()) {
					// boundary
					builder.append(BOUNDARY_MARK).append(boundary).append(LINE_END);
					// key
					builder.append("Content-Disposition: form-data; ");
					builder.append("name=").append(QUOTATION_MARK).append(entry.getKey()).append(QUOTATION_MARK).append(LINE_END);
					builder.append(LINE_END);
					// value
					builder.append(entry.getValue()).append(LINE_END);
				}
			}
			if (file!=null) {
				// boundary
				builder.append(BOUNDARY_MARK).append(boundary).append(LINE_END);
				// file info
				builder.append("Content-Disposition: form-data; ");
				builder.append("name=").append(QUOTATION_MARK).append(requestCommand).append(QUOTATION_MARK).append("; ");
				builder.append("filename=").append(QUOTATION_MARK).append(file.getName()).append(QUOTATION_MARK);
				builder.append(LINE_END);
				// content type
				builder.append("Content-Type: ").append("application/octet-stream").append(LINE_END);
				// header end
				builder.append(LINE_END);
				byte[] header = builder.toString().getBytes(CHARSET);
				builder.setLength(0);
				builder.append(LINE_END).append(BOUNDARY_MARK).append(boundary).append(BOUNDARY_MARK).append(LINE_END);
				byte[] end = builder.toString().getBytes(CHARSET);
				connection.setRequestProperty("Content-Length", String.valueOf(header.length + file.length() + end.length));
				connection.setFixedLengthStreamingMode((int)(header.length + file.length() + end.length));
				DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
				CountingOutputStream countingOutputStream =new CountingOutputStream(outStream);
				countingOutputStream.write(header);
				// write file
				FileInputStream inputStream = new FileInputStream(file);
				int lenght = -1;
				int count =0;
				int currentProgress = 0;
				byte[] buffer = new byte[BUFFER_SIZE];
				while ((lenght = inputStream.read(buffer)) != -1) {
					countingOutputStream.write(buffer, 0, lenght);
					count+=lenght;
					int progress = (int) (count * 100 / file.length());
					if(attchmentId!=null&&uploadCallback!=null&&currentProgress != progress){
						currentProgress = progress;
						uploadCallback.uploadProgress(attchmentId, progress);
					}
				}
				countingOutputStream.write(end);
				Log.i(MailChat.LOG_COLLECTOR_TAG, "HTTPS (上传)==> :"+countingOutputStream.getByteCount());
				inputStream.close();
				countingOutputStream.flush();
				countingOutputStream.close();
			}else {
				byte[] header = builder.toString().getBytes(CHARSET);
				builder.setLength(0);
				builder.append(LINE_END).append(BOUNDARY_MARK).append(boundary).append(BOUNDARY_MARK).append(LINE_END);
				byte[] end = builder.toString().getBytes(CHARSET);
				connection.setRequestProperty("Content-Length", String.valueOf(header.length + end.length));
				DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
				CountingOutputStream countingOutputStream =new CountingOutputStream(outStream);
				countingOutputStream.write(header);
				countingOutputStream.write(end);
				Log.i(MailChat.LOG_COLLECTOR_TAG, "HTTPS (无附件上传)==> :"+countingOutputStream.getByteCount());
				countingOutputStream.flush();
				countingOutputStream.close();
			}
			int code = connection.getResponseCode();
			response.setCode(code);
			if (code == 200) {
				response.setInputStream(connection.getInputStream());
			}
		} catch (IOException e) {
			e.printStackTrace();
			response.setCode(Response.ANTHER_ERROR);
		}
		return response;
	}

	public static Response getDownFile(String attchmentId,String requestUrl,String fileid,String fileName,boolean isthumbnail,DownloadCallback downloadCallback){	
		Response response = new Response();
		try {
			StringBuilder builder = new StringBuilder();
			fileName=URLEncoder.encode(fileName,CHARSET);
			if(isthumbnail){
				builder.append("/").append(fileid).append("_s/").append(fileName);	
			}else{
				builder.append("/").append(fileid).append("/").append(fileName);	
			}
			String url = requestUrl + builder.toString();
			HttpURLConnection connection = prepareHttpsConnection(url, GET);
			int code = connection.getResponseCode();
			Log.i(MailChat.LOG_COLLECTOR_TAG,"HTTPS(GET) : "+StringUtil.removePartCharacters("https://api.mailchat.cn:80/", requestUrl)+" (下载接口) ==> 请求:"+url.toString().getBytes().length);
			response.setCode(code);
			if (code == 200) {
				response.setInputStream(connection.getInputStream());	
			}else{
				downloadCallback.downloadFailed(attchmentId, null);
			}
		} catch (IOException e) {
			e.printStackTrace();
			response.setCode(Response.ANTHER_ERROR);
			downloadCallback.downloadFailed(attchmentId, e);
		}
		return response;
	}
	
	/**
	 * 执行post请求,并加入ER需要的请求头,验证是否为35邮箱.
	 * 
	 * @Description:
	 * @param domain
	 *            需要验证的后缀
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-02-03
	 */
	public static Response postBy35PushVerification(String domain) {
		Response response = new Response();
		try {
			// 请求头
			String timeStamp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
					.format(new Date(System.currentTimeMillis()));
			String sign = EncryptUtil.getMd5("ER" + timeStamp + "DS4rd3drsa^(~^7d");
			String serialnumber = "CCEIS " + timeStamp;
			// 请求体
			StringBuilder builder = new StringBuilder();
			builder.append("request=");
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("businesscode", "ROUTE");
			jsonObject.put("opttype", 2);
			jsonObject.put("serialnumber", serialnumber);
			JSONArray jsonArray = new JSONArray();
			JSONObject domainJson = new JSONObject();
			domainJson.put("domain",domain);
			jsonArray.put(domainJson);
			jsonObject.put("list", jsonArray);
			builder.append(jsonObject.toString());
			byte[] data = builder.toString().getBytes(CHARSET);
			
			HttpURLConnection connection = prepareHttpConnection("http://eis.35.com/ER/route", POST);
			// add_header
			connection.setAllowUserInteraction(true);
			connection.addRequestProperty("clientid", "ER");
			connection.addRequestProperty("timestamp", timeStamp);
			connection.addRequestProperty("sign", sign);
			connection.addRequestProperty("serialnumber", serialnumber);
			
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", String.valueOf(data.length));
			DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
			outStream.write(data);
			outStream.flush();
			outStream.close();
			int code = connection.getResponseCode();
			response.setCode(code);
			if (code == 200) {
				response.setInputStream(connection.getInputStream());
			}
		} catch (IOException e) {
			e.printStackTrace();
			response.setCode(Response.ANTHER_ERROR);
		}catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
	
	/**
	 * 根据后缀截取,拼接出来的域名,来判断35邮箱版本
	 * 
	 * @Description:
	 * @param domain
	 *            需要验证的后缀
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-03-24
	 */
	public static Response getBy35PushVerification(String domain) {
		Response response = new Response();
		try {
			//http://mail.35.cn/servlet/ServiceAction
			String requestUrl;
			if(domain.endsWith(".cn4e.com")){
				requestUrl = "http://"+domain+"/servlet/ServiceAction";
			}else{
				requestUrl = "http://mail."+domain+"/servlet/ServiceAction";
			}
			HttpURLConnection connection =prepareHttpConnection(requestUrl, GET);
			int code = connection.getResponseCode();
			Log.i(MailChat.LOG_COLLECTOR_TAG,"HTTP(GET) : 获取版本  ==> 请求:"+requestUrl.toString().getBytes().length);
			response.setCode(code);
			if (code == 200) {
				response.setInputStream(connection.getInputStream());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			response.setCode(Response.ANTHER_ERROR);
		}
		return response;
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
	public static Response isOAUser(String domain,String email) {
		Response response = new Response();
		try {
			//http://oa.${域名}/outerPostAction.do?actionType=4001&mail=${邮件地址}
			String requestUrl = "http://oa."+domain+"/outerPostAction.do?actionType=4001&mail="+email;
			HttpURLConnection connection =prepareHttpConnection(requestUrl, GET);
			int code = connection.getResponseCode();
			Log.i(MailChat.LOG_COLLECTOR_TAG,"HTTP(GET) : 判断是否为OA用户  ==> 请求:"+requestUrl.toString().getBytes().length);
			response.setCode(code);
			if (code == 200) {
				response.setInputStream(connection.getInputStream());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setCode(Response.ANTHER_ERROR);
		}
		return response;
	}

	public static Response checkIsBindOA(String email) {
		Response response = new Response();
		try {
			String requestUrl = GlobalConstants.OA_CHECK_BIND_URL+email;
			HttpURLConnection connection =prepareHttpConnection(requestUrl, GET);
//			Log.e("qxian",">>response message>"+ connection.getContentType() + "");
//			if(connection.getHeaderFields()!=null){
//			    Log.d("qxian",">>header>"+connection.getHeaderFields().toString());
//			}
			int code = connection.getResponseCode();
			Log.i(MailChat.LOG_COLLECTOR_TAG,"HTTP(GET) : 判断是否绑定OA  ==> 请求:"+requestUrl.toString().getBytes().length);
			response.setCode(code);
			if (code == 200) {
				response.setInputStream(connection.getInputStream());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setCode(Response.ANTHER_ERROR);
		}
		return response;
	}

	public static Response loginInOA(Account account) {
		Response response = new Response();
		try {
				String requestUrl = GlobalConstants.OA_BASE_URL_START+(account.getoAHost()!=null?account.getoAHost():"oa."+Utility.getEmailDomain(account.getEmail()))
						+ GlobalConstants.OA_LOGIN_IN
						+ Utility.getOAUserParam(account);
				HttpURLConnection connection = prepareHttpConnection(
						requestUrl, GET);
//				Log.d("qxian",">>requestUrl>"+requestUrl);
				// Log.e("qxian",">>response message>"+
				// connection.getContentType() + "");
				// if(connection.getHeaderFields()!=null){
				// Log.d("qxian",">>header>"+connection.getHeaderFields().toString());
				// }
				int code = connection.getResponseCode();
				Log.i(MailChat.LOG_COLLECTOR_TAG, "HTTP(GET) : 登录OA  ==> 请求:"
						+ requestUrl.toString().getBytes().length);
				response.setCode(code);
				if (code == 200) {
					response.setInputStream(connection.getInputStream());
				}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setCode(Response.ANTHER_ERROR);
		}
		return response;
	}
}
