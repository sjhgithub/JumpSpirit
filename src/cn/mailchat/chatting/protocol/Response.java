package cn.mailchat.chatting.protocol;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.input.CountingInputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import cn.mailchat.MailChat;
import cn.mailchat.controller.MessageException;
import cn.mailchat.utils.ZlibTools;

import android.util.Log;


/**
 * 服务器响应
 * 
 * @Description:
 * @author:xulei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:Jan 10, 2014
 */
public class Response {

	//
	public static final int ERROR = 10001;
	public static final int SUCCESS = 10002;
	public static final int ANTHER_ERROR=10003;//连接超时,IO流报错
	private int code;
	private InputStream inputStream;
	private CountingInputStream countingInputStream;
	private static final int BUFFER_SIZE = 4 * 1024;
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	// public static Response create(HttpURLConnection connection) {
	// Response response = new Response();
	// try {
	// response.code = connection.getResponseCode();
	// response.inputStream = connection.getInputStream();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return response;
	// }
	public JSONArray toJsonArray() throws MessageException {
		ByteArrayOutputStream outStream = null;
		JSONArray json = null;
		try {
			if (code == 200) {
				if (inputStream != null) {
					countingInputStream =new CountingInputStream(inputStream);
					outStream = new ByteArrayOutputStream();
					byte[] buffer = new byte[512];
					int length = -1;
					while ((length = countingInputStream.read(buffer)) != -1) {
						outStream.write(buffer, 0, length);
					}
					outStream.flush();
					Log.i(MailChat.LOG_COLLECTOR_TAG,"https ==> 响应:"+countingInputStream.getByteCount());
					String jsonString = new String(outStream.toByteArray());
					if (jsonString != null && !"".equals(jsonString)) {
						json = new JSONArray(jsonString);
					}
					code = SUCCESS;
				} else {
					Log.e("Response", "toJsonArray inputstream is null !");
					code = MessageException.UNKNOW_NET_EXCEPTION;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			code = MessageException.UNKNOW_IO_EXCEPTION;
		} finally {
			try {
				if (outStream != null) {
					outStream.close();
				}
				if (countingInputStream != null) {
					countingInputStream.close();
				}
				mop();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return json;
	}

	/**
	 * 将响应结果解析成JSON
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: xulei
	 * @throws MessageException
	 * @date:Jan 11, 2014
	 */
	public JSONObject toJson() throws MessageException {
		ByteArrayOutputStream outStream = null;
		JSONObject json = null;
		try {
			if (code == 200) {
				if (inputStream != null) {
					countingInputStream =new CountingInputStream(inputStream);
					outStream = new ByteArrayOutputStream();
					byte[] buffer = new byte[512];
					int length = -1;
					while ((length = countingInputStream.read(buffer)) != -1) {
						outStream.write(buffer, 0, length);
					}
					outStream.flush();
					Log.i(MailChat.LOG_COLLECTOR_TAG,"HTTPS ==> 响应:"+countingInputStream.getByteCount());
					String jsonString = new String(outStream.toByteArray());
					if (jsonString != null && !"".equals(jsonString)) {
						json = new JSONObject(jsonString);
						if (json.getInt("result")==0) {
							code = ERROR;
						} else {
							code = SUCCESS;
						}
					}
				} else {
					Log.e("Response", "toJson inputstream is null !");
					code = MessageException.UNKNOW_NET_EXCEPTION;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			code = MessageException.UNKNOW_IO_EXCEPTION;
		} finally {
			try {
				if (outStream != null) {
					outStream.close();
				}
				if (countingInputStream != null) {
					countingInputStream.close();
				}
				mop();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return json;
	}

	/**
	 * 将响应结果解析成JSON
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws MessageException
	 * @date:2015-09-21
	 */
	public JSONObject toOAJson() throws MessageException {
		ByteArrayOutputStream outStream = null;
		JSONObject json = null;
		try {
			if (code == 200) {
				if (inputStream != null) {
					countingInputStream =new CountingInputStream(inputStream);
					outStream = new ByteArrayOutputStream();
					byte[] buffer = new byte[512];
					int length = -1;
					while ((length = countingInputStream.read(buffer)) != -1) {
						outStream.write(buffer, 0, length);
					}
					outStream.flush();
					Log.i(MailChat.LOG_COLLECTOR_TAG,"HTTP ==> 响应:"+countingInputStream.getByteCount());
					String jsonString = new String(outStream.toByteArray());
					if (jsonString != null && !"".equals(jsonString)) {
						json = new JSONObject(jsonString);
						if (!json.isNull("success")&&!json.getBoolean("success")) {
							code = ERROR;
						} else {
							code = SUCCESS;
						}
					}
				} else {
					Log.e("Response", "toJson inputstream is null !");
					code = MessageException.UNKNOW_NET_EXCEPTION;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			code = MessageException.UNKNOW_IO_EXCEPTION;
		} finally {
			try {
				if (outStream != null) {
					outStream.close();
				}
				if (countingInputStream != null) {
					countingInputStream.close();
				}
				mop();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return json;
	}
	/**
	 * 将响应结果写入指定路径的文件
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws MessageException
	 * @date:2014-11-14
	 */
	public void toDwonFile(String attchmentId,String path,long fileSize,DownloadCallback downloadCallback)throws MessageException{
		FileOutputStream out = null;
		File file=null;
		try {
			if (code == 200) {
				if (inputStream != null) {
					countingInputStream =new CountingInputStream(inputStream);
					file = new File(path);
					out = new FileOutputStream(file);
					int lenght = -1;
					int count =0;
					byte[] buffer = new byte[BUFFER_SIZE];
					while ((lenght = countingInputStream.read(buffer)) != -1) {
						out.write(buffer, 0, lenght);
						count += lenght;
						int progress = (int) (count * 100 / fileSize);
						downloadCallback.downloadProgress(attchmentId,progress,0);
					}
					code = SUCCESS;
					downloadCallback.downloadFinished(attchmentId);
					Log.i(MailChat.LOG_COLLECTOR_TAG,"HTTPS ==> (文件下载) 下载:"+countingInputStream.getByteCount());
				}
			} else {
				Log.e("Response", "toFile inputstream is null !");
				code = MessageException.UNKNOW_NET_EXCEPTION;
			}
		} catch (Exception e) {
			// TODO: handle exception
			if(file!=null&&file.exists()){
				file.delete();
			}
			e.printStackTrace();
			code = MessageException.UNKNOW_IO_EXCEPTION;
		}finally{
			try {
				if (countingInputStream != null) {
					countingInputStream.close();
				}
				if (out != null) {
					out.close();
				}
				mop();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * 是否执行成功
	 * 
	 * @Description:
	 * @return
	 * @throws MessageException
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jan 11, 2014
	 */
	public boolean isExecuteSuccess() throws MessageException {
		toJson();
		return code == SUCCESS;
	}

	private boolean mop() throws MessageException {
		if (code == SUCCESS) {
			return true;
		} else {
			Log.e("Response", " ERROR CODE: " + code);
			throw new MessageException(code);
		}
	}

	/**
	 * 
	 * method name: decodeResponseByZlib function @Description: TODO Parameters
	 * and return values description:
	 * 
	 * @param response
	 * @return field_name JSONObject return type
	 * @History memory：
	 * @Date：2015-8-24 下午2:17:07 @Modified by：zhangjx
	 * @Description:解析经过zlib压缩的数据
	 */
	public JSONObject decodeResponseByZlib(Response response) {
		ByteArrayOutputStream outStream = null;
		JSONObject json = null;
		try {
			if (code == 200) {
				if (inputStream != null) {
					countingInputStream = new CountingInputStream(inputStream);
					outStream = new ByteArrayOutputStream();
					byte[] buffer = new byte[512];
					int length = -1;
					while ((length = countingInputStream.read(buffer)) != -1) {
						outStream.write(buffer, 0, length);
					}
					outStream.flush();
					Log.i(MailChat.LOG_COLLECTOR_TAG, "HTTPS ==> 响应:"
							+ countingInputStream.getByteCount());
					byte[] jsonByte = ZlibTools.decompress(outStream
							.toByteArray());
					String jsonString = new String(jsonByte, "UTF-8");
					if (jsonString != null && !"".equals(jsonString)) {
						json = new JSONObject(jsonString);
						if (json.getInt("result")==0) {
							code = ERROR;
						} else {
							code = SUCCESS;
						}
					}
				} else {
					Log.e("Response", "toJson inputstream is null !");
					code = MessageException.UNKNOW_NET_EXCEPTION;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			code = MessageException.UNKNOW_IO_EXCEPTION;
		} finally {
			try {
				if (outStream != null) {
					outStream.close();
				}
				if (countingInputStream != null) {
					countingInputStream.close();
				}
				mop();
			} catch (IOException | MessageException e) {
				e.printStackTrace();
			}
		}
		return json;
	}

	/**
	 * 获取服务端返回的json字符串，不做内容判断来抛异常。
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws MessageException
	 * @date:2016-1-15
	 */
	public JSONObject getJson() throws MessageException {
		ByteArrayOutputStream outStream = null;
		JSONObject json = null;
		try {
			if (code == 200) {
				if (inputStream != null) {
					countingInputStream =new CountingInputStream(inputStream);
					outStream = new ByteArrayOutputStream();
					byte[] buffer = new byte[512];
					int length = -1;
					while ((length = countingInputStream.read(buffer)) != -1) {
						outStream.write(buffer, 0, length);
					}
					outStream.flush();
					Log.i(MailChat.LOG_COLLECTOR_TAG,"HTTPS ==> 响应:"+countingInputStream.getByteCount());
					String jsonString = new String(outStream.toByteArray());
					if (jsonString != null && !"".equals(jsonString)) {
						json = new JSONObject(jsonString);
						code = SUCCESS;
					}
				} else {
					Log.e("Response", "toJson inputstream is null !");
					code = MessageException.UNKNOW_NET_EXCEPTION;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			code = MessageException.UNKNOW_IO_EXCEPTION;
		} finally {
			try {
				if (outStream != null) {
					outStream.close();
				}
				if (countingInputStream != null) {
					countingInputStream.close();
				}
				mop();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return json;
	}
}
