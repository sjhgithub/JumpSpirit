package com.c35.mtd.pushmail.ent.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.util.StringUtil;

/**
 * 
 * @Description:与服务器交互工具类
 * @author:黄永兴(huangyx2@35.cn)
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-2-17
 */
public class HttpUtil {

	private static final int TIME_OUT = 10000;//30000

	private static final String CHARSET = "UTF-8";

	private static HttpUtil instance = new HttpUtil();
	
	public static HttpUtil getInstance() {
		return instance;
	}


	/**
	 * 提交POST请求
	 * 
	 * @Description:
	 * @param url
	 * @param params
	 * @return
	 * @throws IOException
	 * @see:
	 * @since:
	 * @author: huangyongxing
	 * @date:2012-8-8
	 */
	private String postMethod(String url, List<NameValuePair> params) throws IOException {

		HttpURLConnection conn = getConnection(url);
		if(conn==null){
			return "";
		}
		conn.setRequestMethod("POST");
		// Post 请求不能使用缓存
		conn.setUseCaches(false);

		conn.setRequestProperty(" Content-Type ", " application/x-www-form-urlencoded ");
		// 连接，从postUrl.openConnection()至此的配置必须要在 connect之前完成，
		// 要注意的是connection.getOutputStream()会隐含的进行调用 connect()，所以这里可以省略 connection.connect();
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		// 正文内容其实跟get的URL中'?'后的参数字符串一致
		StringBuffer buffer = new StringBuffer();
		int i = 0;
		for (NameValuePair value : params) {
			buffer.append(i == 0 ? "" : "&");
			buffer.append(value.getName());
			buffer.append("=");
			if(StringUtil.isNotEmpty(value.getValue())){
				buffer.append(URLEncoder.encode(value.getValue(), CHARSET));
			}
			i++;
		}
		// DataOutputStream.writeBytes将字符串中的16位的 unicode字符以8位的字符形式写道流里面
		out.writeBytes(buffer.toString());
		out.flush();
		out.close(); // flush and close

		String result = null;
		try{
			if (conn.getResponseCode() == 200) {
				if ("gzip".equalsIgnoreCase(conn.getContentEncoding())) {
					result = requestResult(conn.getInputStream(), true);
				} else {
					result = requestResult(conn.getInputStream(), false);
				}
			} 
			conn.disconnect();
		} catch (java.net.SocketTimeoutException e) {
			Debug.w("failfast", e.getMessage());
		} catch (Exception e) {
			Debug.w("failfast", e.getMessage());
		}
		return result;
	}

	/**
	 * 提交GET请求，不用httpurlconnection是为了和4.0兼容
	 * 
	 * @Description:
	 * @param url
	 * @param params
	 * @return
	 * @throws IOException
	 * @see:
	 * @since:
	 * @author: huangyongxing
	 * @date:2012-8-8
	 */
	private String getMethod(String url, List<NameValuePair> params) throws IOException {
		String result = null;
		try{
			StringBuffer buffer = new StringBuffer();
			int i = 0;
			// 拼音url
			for (NameValuePair value : params) {
				buffer.append(i == 0 ? "?" : "&");
				buffer.append(value.getName());
				buffer.append("=");
				buffer.append(URLEncoder.encode(value.getValue(), CHARSET));
				i++;
			}
			HttpParams httpParams = new BasicHttpParams();
			// 设置超时时间
			HttpConnectionParams.setConnectionTimeout(httpParams, TIME_OUT);
			HttpConnectionParams.setSoTimeout(httpParams, TIME_OUT);
			HttpClient client = new DefaultHttpClient(httpParams);
			HttpGet get = new HttpGet(url + buffer.toString());//http://prm.35.com:9012/system/get_prm_base_url?account=cuiwei%40china-channel.com
			HttpResponse response = client.execute(get);
	
	
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				Header header = entity.getContentEncoding();
				if (header == null || !"gzip".equalsIgnoreCase(header.getValue())) {
					result = requestResult(entity.getContent(), false);
				} else {
					result = requestResult(entity.getContent(), true);
				}
			}

		} catch (java.net.SocketTimeoutException e) {
			Debug.w("failfast", e.getMessage());
			
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		}
		return result;
	}

	/**
	 * 解析服务器返回数据
	 * 
	 * @Description:
	 * @param is
	 * @param isGzip
	 * @return
	 * @throws IOException
	 * @see:
	 * @since:
	 * @author: huangyongxing
	 * @date:2012-8-8
	 */
	private String requestResult(InputStream is, boolean isGzip) throws IOException{
		int i = -1;
		if (isGzip) {
			is = new GZIPInputStream(is);//java.lang.OutOfMemoryError
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((i = is.read()) != -1) {
		    baos.write(i);
		}
		baos.close();
		System.gc();
		return baos.toString(CHARSET);
	}

	/**
	 * 发送请求
	 * 
	 * @Description:
	 * @param url 目标url  http://prm.35.com:9012/system/get_prm_base_url
	 * @param params 参数 [account=zhonggy@35.cn]
	 * @param isPost 是否为Post提交
	 * @return
	 * @see:
	 * @since:
	 * @author: huangyongxing
	 * @date:2012-6-29
	 */
	public String sendRequest(String url, List<NameValuePair> params, boolean isPost) {
		String result = null;
		try {
			if (isPost) {
				result = postMethod(url, params);
			} else {
				result = getMethod(url, params);
			}
		} catch (UnsupportedEncodingException e) {
			Debug.e("failfast", "failfast_AA", e);
		} catch (ClientProtocolException e) {
			Debug.e("failfast", "failfast_AA", e);
		} catch (IOException e) {
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			
		}

		return result;
	}

	private HttpURLConnection getConnection(String uri) {
		HttpURLConnection httpConn = null;
		URL url = null;
		try {
			url = new URL(uri);
			httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setConnectTimeout(TIME_OUT);
			httpConn.setReadTimeout(TIME_OUT);
			// 打开读写属性，默认均为false
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			httpConn.setInstanceFollowRedirects(true);
		} catch (Exception e) {
			Debug.w("failfast", e.getMessage());
		}
		return httpConn;
	}
}
