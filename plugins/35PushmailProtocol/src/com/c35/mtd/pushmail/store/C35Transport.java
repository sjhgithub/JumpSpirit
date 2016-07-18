package com.c35.mtd.pushmail.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.util.ExceptionUtils;
import org.json.JSONObject;

import android.util.Log;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.GlobalVariable;
import com.c35.mtd.pushmail.beans.C35Attachment;
import com.c35.mtd.pushmail.command.request.UpLoadAttachmentRequest;
import com.c35.mtd.pushmail.command.response.DownloadDataResponse;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.interfaces.AttDownLoadCallback;
import com.c35.mtd.pushmail.util.JsonUtil;
import com.c35.mtd.pushmail.util.MailUtil;
import com.c35.mtd.pushmail.util.ZipUtil;

/**
 * 与自有协议服务器做通讯的Transport工具. 异常处理：1、网络中断或超时，导致服务器连接失败；
 * 2、服务器响应错误：a、响应格式错误；b，格式正确，返回具体错误代号 3、磁盘读写失败
 * 
 * @author:liujie
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2011-10-27
 */
public class C35Transport {

	static final String TAG = "C35Transport";
	/** 邮件接入点 */
	private Socket mSocket = null;
	/** 附件及内嵌资源接入点 */
	private Socket mUpDownSocket = null;
	/** 和服务器的输入输出流 */
	private InputStream mIn;
	private OutputStream mOut;
	/** 服务器主机名 dp或代理 */
	private String mHost;
	/** 服务器端口号 dp或代理 */
	private int mPort;
	/** 用户信息[0]用户名[1]密码 */
	private String[] mUserInfoParts;
	// private BufferedReader sockin;
	// private PrintWriter mOutputStreamPrintWriter;
	public static int BLOCKSIZE = 1024000;// 单位，byte(102400=100K);
	/** 超时时间限制 */
	public static final int SOCKET_CONNECT_TIMEOUT = 60 * 1000;// test 15s缩减为5s
	// public static final int SOCKET_READ_TIMEOUT = 15000;
	/** 请求的序列号 */
	private static int commandId = 0;
	private final Object mOpen = new Object();
	Object readFullyLock = new Object();// 同步锁
	Object clearLineLock = new Object();// 同步锁
	Object readLineLock = new Object();// 同步锁

	/**
	 * 描述连接到邮件服务器的方法 new Socket() new BufferedInputStream(
	 * 
	 * @Description:
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-29
	 */
	public void openSocketAndStream() throws MessagingException {
		synchronized (mOpen) {
			if (isSocketAndStreamOpen()) {
				return;
			} else {
				forceOpenSocketAndStream();
				// String result = null;
				// try {
				// // long t = System.currentTimeMillis();
				// if (mSocket == null) {
				// mSocket = new Socket();
				// SocketAddress socAddress = new InetSocketAddress(getHost(),
				// getPort());
				// mSocket.connect(socAddress, SOCKET_CONNECT_TIMEOUT);// 5秒钟
				// // mSocket = new Socket(getHost(),
				// getPort());//若当服务器IP不存在会服务器没有响应时，程序会卡在这句代码老长一段时间
				// }
				// Debug.i(TAG, "mSocket.isConnected():" +
				// mSocket.isConnected());
				// mSocket.setSoTimeout(MailUtil.SOCKET_READ_TIMEOUT_TIME);
				// mIn = new BufferedInputStream(mSocket.getInputStream());//
				// Socket[address=wmail215.cn4e.com/124.202.154.215,port=5566,localPort=51013]
				// // sockin = new BufferedReader(new InputStreamReader(mIn,
				// "UTF-8"));
				// mOut = new BufferedOutputStream(mSocket.getOutputStream());//
				// Socket[address=wmail215.cn4e.com/124.202.154.215,port=5566,localPort=51013]
				// // mOutputStreamPrintWriter = new PrintWriter(mOut, true);
				// result = null;
				// // result = sockin.readLine();
				// Debug.i(TAG, "host____port:" + getHost() + "||" + getPort());
				// // Debug.i(TAG,
				// "MailUtil.PROXY_SERVER_DOMAIN_HOST.equals(getHost())" +
				// // MailUtil.DP_SERVER_DOMAIN_HOST.equals(getHost()));
				// if (!(MailUtil.DP_SERVER_DOMAIN_HOST.equals(getHost()) &&
				// MailUtil.DP_SERVER_DOMAIN_PORT ==
				// getPort())) {
				// result = readLine();
				// Debug.d(TAG, "open ------------------------>" + result);
				// }
				// } catch (Exception e) {
				// close();
				// String strErr = "";
				// if (e != null) {
				// strErr = e.getMessage();
				// }
				// throw new
				// MessagingException(MessagingException.RETURN_COMMAND_ERROR_LINK_TIMEOUT,
				// strErr);
				// }
			}
		}
	}

	/**
	 * 描述连接到邮件服务器的方法 new Socket() new BufferedInputStream(
	 * 
	 * @Description:
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-29
	 */
	public void forceOpenSocketAndStream() throws MessagingException {
		synchronized (mOpen) {
			String result = null;
			try {
				// long t = System.currentTimeMillis();
				if (mSocket == null) {
					mSocket = new Socket();
					SocketAddress socAddress;
					/**
					 * imcore
					 */
					if (getHost().equals("mail.gzsq.com")) {
						socAddress = new InetSocketAddress(getHost(), 88);
					} else {
						socAddress = new InetSocketAddress(getHost(), getPort());
					}
					mSocket.connect(socAddress, SOCKET_CONNECT_TIMEOUT);// 60秒钟
					Log.d("TAG", "发送地址：" + getHost() + ":" + getPort());
					// mSocket = new Socket(getHost(),
					// getPort());//若当服务器IP不存在会服务器没有响应时，程序会卡在这句代码老长一段时间
				}
				Debug.i(TAG, "mSocket.isConnected():" + mSocket.isConnected());
				mSocket.setSoTimeout(SOCKET_CONNECT_TIMEOUT);
				mIn = new BufferedInputStream(mSocket.getInputStream());// Socket[address=wmail215.cn4e.com/124.202.154.215,port=5566,localPort=51013]
				// sockin = new BufferedReader(new InputStreamReader(mIn,
				// "UTF-8"));
				mOut = new BufferedOutputStream(mSocket.getOutputStream());// Socket[address=wmail215.cn4e.com/124.202.154.215,port=5566,localPort=51013]
				// mOutputStreamPrintWriter = new PrintWriter(mOut, true);
				result = null;
				// result = sockin.readLine();
				Debug.i(TAG, "host____port:" + getHost() + "||" + getPort());
				// Debug.i(TAG,
				// "MailUtil.PROXY_SERVER_DOMAIN_HOST.equals(getHost())" +
				// MailUtil.DP_SERVER_DOMAIN_HOST.equals(getHost()));
				if (!(MailUtil.DP_SERVER_DOMAIN_HOST.equals(getHost()) && MailUtil.DP_SERVER_DOMAIN_PORT == getPort())) {
					result = readLine();// 登陆时读取服务器返回的 ProxyServer is ready
										// ，读不到时返回异常
					Debug.d(TAG, "open ------------------------>" + result);
				}
			} catch (Exception e) {
				close();
				String strErr = "";
				if (e != null) {
					strErr = e.getMessage();
				}
				throw new MessagingException(
						MessagingException.RETURN_COMMAND_ERROR_LINK_TIMEOUT,
						strErr);
			}
		}
	}

	/**
	 * 打开上传下载服务器的连接
	 * 
	 * @author liujie
	 * @date 2011-12-2
	 * @return Socket
	 * @throws
	 */
	public Socket connectUpDownloadServer(int updownloadport)
			throws MessagingException {
		String host = mHost;
		// int proxyServerUpDownloadPort = 9998;
		try {
			mUpDownSocket = new Socket(host, updownloadport);
			mUpDownSocket.setSoTimeout(MailUtil.SOCKET_ATT_READ_TIMEOUT_TIME);
		} catch (Exception e) {
			throw new MessagingException(MessagingException.CONNECT_ERROR);
		}
		return mUpDownSocket;
	}

	/**
	 * 关闭已经打开的上传下载服务器的socket
	 * 
	 * @Description:
	 * @param Socket
	 *            client
	 * @see:
	 * @since:
	 * @author: liujie
	 * @date:2012-2-28
	 */
	public void closeUpDownloadServer(Socket client) {
		if (client != null) {
			try {
				client.close();
			} catch (IOException e) {
				Debug.e("failfast", "failfast_AA", e);
			}
		}
	}

	// /**
	// * 上传附件或者内嵌的方法
	// * @param fileName
	// * @param fileFullPath
	// * @param ticket
	// * @return
	// * @throws Exception
	// * @see:
	// * @since:
	// * @author: liujie
	// * @date:2012-2-28
	// */
	// public String doUploadData(String fileName, String fileFullPath, String
	// ticket, int updownloadport)
	// throws IOException, MessagingException {
	// String serverPath = uploadCommand( fileName, fileFullPath, ticket,
	// updownloadport,BLOCKSIZE);
	// Debug.i(TAG, "serverPath--------:" + serverPath);
	// return serverPath;
	// }

	/**
	 * 上传附件或者内嵌的方法的执行命令
	 * 
	 * @param ticket
	 * @param fileFullPath
	 * @param fileName
	 * @param bLOCKSIZE2
	 * @return
	 * @throws IOException
	 * @throws MessagingException
	 * @see: serverPath
	 * @since:
	 * @author: hanlixia
	 * @date:2012-11-2
	 */
	public String uploadCommand(String fileName, String fileFullPath,
			String ticket, int updownloadport, int bLOCKSIZE2)
			throws IOException, MessagingException {
		// 50001---上传的文件不存在!
		// 50002---连接服务器失败
		// 50003---获得服务器输出流失败
		// 50004---装载本地数据失败
		// 50005---拼装发送命令失败
		// 50006---发送命令体失败
		// 50007---发送命令体失败
		// 50008---系统关闭输出流失败
		// 50009---获得输入流失败
		// 50010---读取输入流中的数据失败
		// 50011---读取输入流中的数据失败

		Debug.p("fileFullPath = " + fileFullPath);
		File file = new File(fileFullPath);
		if (!file.exists()) {//
			throw new MessagingException("50001 " + fileFullPath);
		}
		Socket updownclient = null;
		try {
			updownclient = connectUpDownloadServer(updownloadport);
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
			throw new MessagingException("50002" + e.getMessage());
		}
		// DataOutputStream dos = new
		// DataOutputStream(updownclient.getOutputStream());
		OutputStream dos = null;
		try {
			dos = updownclient.getOutputStream();
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
			throw new MessagingException("50003" + e.getMessage());
		}

		// 获取byte[]数据
		byte[] dataAll = null;
		try {
			dataAll = readFile(file);
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
			throw new MessagingException("50004" + e.getMessage());
		}
		// 分块发送数据//通过response输出所有的块，方法返回CODE_NO_RETURN
		List<byte[]> dataListTemp = splitBytes(dataAll, bLOCKSIZE2);

		// 发送命令头
		String sequenceid = "" + getCommandId();
		String commandString = sequenceid + " uploadData " + ticket + " 0";
		Debug.i(TAG, "request: " + commandString);
		try {
			dos.write(commandString.getBytes().length);
			dos.write(' ');
			dos.write(commandString.getBytes());
			dos.write(' ');
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
			throw new MessagingException("50005" + e.getMessage());
		}

		// 发命令体
		UpLoadAttachmentRequest uploadDataRequest = new UpLoadAttachmentRequest();
		for (int i = 0; i < dataListTemp.size(); i++) {
			uploadDataRequest.setFileName(fileName);
			uploadDataRequest.setTotalBlock(dataListTemp.size());
			uploadDataRequest.setCurrentBlock(i + 1);
			Debug.i(TAG, "upLoading file =" + fileName
					+ "---------total block = " + dataListTemp.size()
					+ "------current block =" + i);
			byte[] content = dataListTemp.get(i);
			try {
				uploadDataRequest.setDataContent(ZipUtil.compress(content));
			} catch (Exception e) {
				Debug.w("failfast", "failfast_AA", e);
				throw new MessagingException("50006" + e.getMessage());
			}

			try {
				writeObj(dos, uploadDataRequest);
			} catch (Exception e) {
				Debug.w("failfast", "failfast_AA", e);
				throw new MessagingException("50007" + e.getMessage());
			}
		}
		try {
			dos.flush();
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
			throw new MessagingException("50008" + e.getMessage());
		}

		// --------------

		InputStream dis = null;
		try {
			dis = updownclient.getInputStream();
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
			throw new MessagingException("50009" + e.getMessage());
		}
		String result = null;
		try {
			result = getResultString(dis, fileFullPath);
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
			throw new MessagingException("50010" + e.getMessage());
		}
		Debug.i(TAG, "response: " + result);
		closeUpDownloadServer(updownclient);

		String serverPath = null;
		boolean bRes = isResponseOk(result, fileFullPath);
		try {
			if (bRes) {
				String vsTmp = result.substring(0, result.lastIndexOf("\""));
				serverPath = vsTmp.substring(vsTmp.lastIndexOf("\"") + 1);
				Debug.i(TAG, "attachId: " + serverPath);
			}
		} catch (Exception e) {
			Debug.w("failfast", "failfast_AA", e);
			throw new MessagingException("50012" + e.getMessage());
		}

		return serverPath;
	}

	/**
	 * 读取输入流中的数据到字符串
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 * @see:
	 * @since:
	 * @author: liujie
	 * @date:2012-2-28
	 */
	private String getResultString(InputStream is, String strExceptionInfo)
			throws IOException, MessagingException {
		String result = null;
		try {
			DataInputStream dis = new DataInputStream(is);

			int resultStringSize = dis.read();
			if (resultStringSize < 1) {// 错误返回值
				throw new MessagingException(
						MessagingException.RETURN_COMMAND_ERROR_GET_RESULTSTRING,
						"dis.read()=" + resultStringSize);
			}
			dis.read();// ' '
			byte[] resultStringArray = new byte[resultStringSize];
			dis.readFully(resultStringArray);
			dis.read();// ' '
			result = new String(resultStringArray);
		} catch (MessagingException e) {
			throw e;
		} catch (SocketTimeoutException e) {
			// "-1 Exception";
			Debug.w("failfast", "failfast_AA", e);
			throw new MessagingException(
					MessagingException.RETURN_COMMAND_ERROR_GET_RESULTSTRING,
					"SocketTimeoutException " + strExceptionInfo);
		} catch (Exception e) {
			// "-1 Exception";
			Debug.w("failfast", "failfast_AA", e);
			throw new MessagingException(
					MessagingException.RETURN_COMMAND_ERROR_GET_RESULTSTRING,
					e.getMessage() + "  " + strExceptionInfo);
		}
		return result;
	}

	/**
	 * 描述 下载附件
	 * 
	 * @throws MessagingException
	 * @Title: doDownloadData
	 * @author liujie
	 * @date 2011-12-2
	 * @return void
	 * @throws
	 */
	public void doDownloadData(C35Attachment attachmentInfo, String ticket,
			String filePath, AttDownLoadCallback callback, int updownloadport)
			throws MessagingException {

		String mailId = attachmentInfo.getMailId();
		String attachId = attachmentInfo.getId();// 52956b1a1170c168639832d0_inbox_0

		String cid = attachmentInfo.getCid();
		int isIcon = 1;

		String compressFileName = attachmentInfo.getFileName();// rar:test\EwaveIOS_V2.1.12.ipa
		compressFileName = compressFileName.replaceAll("\\\\", "\\\\\\\\");// \\\\
																			// ，java解析为\\交给正则表达式，
																			// 正则表达式再经过一次转换，把\\转换成为\
		boolean isCompress = attachmentInfo.isCompress();

		// 发出请求
		Socket updownclient = null;
		String result;
		byte[] dataAll = null;
		BufferedInputStream dis;
		int progress = 0;
		String attId = null;
		List<byte[]> dataListTemp = null;
		try {
			attId = isCompress ? String.valueOf(attachmentInfo.getsID())
					: attachId;

			// try {
			updownclient = connectUpDownloadServer(updownloadport);
			// } catch (MessagingException e) {
			// Debug.e("failfast", "failfast_AA", e);
			// throw e;
			// // updownclient =
			// connectUpDownloadServer(updownloadport);//没必要重新打开连接吧？？？
			// }
			// DataOutputStream dos = new
			// DataOutputStream(updownclient.getOutputStream());
			OutputStream dos = updownclient.getOutputStream();
			result = null;
			// 发送命令
			String commandString = getCommandId() + " downloadData " + ticket
					+ " 0";
			Debug.d(TAG, "request: " + commandString);// 49 downloadData
														// 20131128103055893369
														// 0
			Debug.d(TAG, "request: compressFileName==" + compressFileName);// rar:test\EwaveIOS_V2.1.12.ipa
			dos.write(commandString.getBytes().length);//
			dos.write(' ');
			dos.write(commandString.getBytes());
			dos.write(' ');

			dos.flush();

			String parameterString = "{\"mailId\":\""
					+ mailId
					+ "\";\"attachId\":\""
					+ attachId
					+ "\";\"compressFileName\":\""
					+ (isCompress ? compressFileName : "")
					+ "\""
					+ ((cid == null || "".equals(cid)) ? "" : ";\"cid\":\""
							+ cid + "\";\"isIcon\":" + isIcon) + "}";
			Debug.d(TAG, "request: parameterString==" + parameterString);
			Debug.e(TAG, "pString length" + parameterString.getBytes().length);
			dos.write(parameterString.getBytes().length);
			dos.write(' ');
			dos.write(parameterString.getBytes());
			dos.flush();

			dis = new BufferedInputStream(updownclient.getInputStream());
			try {
				result = getResultString(dis, mailId + "," + compressFileName);

			} catch (MessagingException e) {
				if (e.getExceptionType() == MessagingException.RETURN_COMMAND_ERROR_GET_RESULTSTRING) {
					if (cid == null || cid.length() < 1) {
						throw new MessagingException(
								MessagingException.RETURN_COMMAND_ERROR_GET_RESULTSTRING,
								e.getMessage() + " \n" + parameterString);
					} else {
						throw new MessagingException(
								MessagingException.DOWNLOAD_CID_PHOTO_ERROR_MESSAGE,
								e.getMessage() + " " + cid);

					}
				} else {
					throw e;
				}
			}
			Debug.d(TAG, "response download start:" + result);
			// 解析下载的数据
			if (isResponseOk(result, attachmentInfo.getFileName())) {
				// 组装数据
				// try {

				checkStopDownload(attId, updownclient);// 停止下载啦吗？

				GlobalVariable.recordProgress(attId, 0);// 计入缓存
				DownloadDataResponse downloadDataResponse = readDownloadDataResponse(dis);// 获取返回的第一块，根据第一块来确定后面还有多少块来处理
				if (downloadDataResponse.getTotalBlock() == 1) {// 上传数据只有一个内容块，直接处理
					// 先从十六进制转成字符串，再解压缩
					dataAll = ZipUtil.uncompress(downloadDataResponse
							.getDataContent());
					progress = 100;
					callback.updateProgress(attachmentInfo, progress);
					GlobalVariable.recordProgress(attId, 100);// 计入缓存

					File tempFile = new File(makeAttFileDiskPath(filePath,
							attachmentInfo));

					writeFile(tempFile, dataAll);

				} else {// 上传内容有多个内容块，要缓存处理
					Map<String, DownloadDataResponse> downloadDataMap = new HashMap<String, DownloadDataResponse>();
					// 第一块放入容器中
					downloadDataMap.put(downloadDataResponse.getTotalBlock()
							+ "-" + downloadDataResponse.getCurrentBlock(),
							downloadDataResponse);
					// 获取其它块，放入容器中
					int count = downloadDataResponse.getTotalBlock();
					for (int i = 1; i < count; i++) {// 第一块不用再获取了
						checkStopDownload(attId, updownclient);// 停止下载啦
						downloadDataResponse = readDownloadDataResponse(dis);
						checkStopDownload(attId, updownclient);// 停止下载啦
						progress = (i * 100) / count;
						callback.updateProgress(attachmentInfo, progress);
						GlobalVariable.recordProgress(attId, progress);// 计入缓存
						// 放入容器中
						downloadDataMap.put(
								downloadDataResponse.getTotalBlock()
										+ "-"
										+ downloadDataResponse
												.getCurrentBlock(),
								downloadDataResponse);
					}
					GlobalVariable.recordProgress(attId, 100);// 计入缓存
					// 多个内容块齐全了，组装处理数据

					dataListTemp = new ArrayList<byte[]>();

					int totalSize = 0;
					byte[] tempData = null;

					int size = downloadDataResponse.getTotalBlock();

					for (int i = 0; i < size; i++) {
						// System.out.println("第"+i+"次开始");
						tempData = ZipUtil.uncompress(downloadDataMap.get(
								downloadDataResponse.getTotalBlock() + "-"
										+ (i + 1)).getDataContent());
						totalSize += tempData.length;
						dataListTemp.add(tempData);

						downloadDataMap.remove(downloadDataResponse
								.getTotalBlock() + "-" + (i + 1));
					}
					tempData = null;
					downloadDataMap = null;
					downloadDataResponse = null;
					dos.close();
					dis.close();

					File tempFile = new File(makeAttFileDiskPath(filePath,
							attachmentInfo));

					writeFile(tempFile, dataListTemp);
				}// //mnt/sdcard/35.com/35mail/database/35PushMail.db_att//52956b1a1170c168639832d0_inbox_0_test\MountainLion2.0用户需求说明书.doc
			}
			// closeUpDownloadServer(updownclient);
		} catch (IOException e) {// java.net.SocketException: sendto failed:
									// EPIPE (Broken pipe)
			// e.printStackTrace();
			// closeUpDownloadServer(updownclient);
			System.out.println("IOException");
			GlobalVariable.recordProgress(attId, 101);// 计入缓存
			throw new MessagingException(MessagingException.SERVER_IO_ERROR,
					e.getMessage());
		} catch (MessagingException e) {// com.c35.mtd.pushmail.exception.MessagingException:
										// 909,下载文件不成功！
			System.out.println("MessagingException");
			GlobalVariable.recordProgress(attId, 101);// 计入缓存
			throw e;
		} catch (Exception e) {
			// e.printStackTrace();
			// closeUpDownloadServer(updownclient);
			System.out.println("Exception" + e);
			GlobalVariable.recordProgress(attId, 101);// 计入缓存
			throw new MessagingException(
					MessagingException.DOWNLOAD_ATTACHMENT_ERROR_MESSAGE);
		} catch (Error e) {
			System.out.println("Error is " + e.toString());
		} finally {
			dataAll = null;
			closeUpDownloadServer(updownclient);
		}

	}

	/**
	 * 
	 * @Description:
	 * @return filePath //mnt/sdcard/35.com/35mail/database/35PushMail.db_att/
	 *         rar:test\MountainLion2.0用户需求说明书.doc
	 *         test\MountainLion2.0用户需求说明书.doc
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-28
	 */
	public static String makeAttFileDiskPath(String filePath,
			C35Attachment attachment) {
		boolean isCompress = attachment.isCompress();
		String strReturn;

		if (!isCompress) {
			strReturn = filePath + File.separator + attachment.getId();
		} else {
			String strFileNameChange = attachment.getFileName().split(":")[1]
					.replaceAll("\\\\", "__");
			strReturn = filePath + File.separator + attachment.getId() + "_"
					+ strFileNameChange;// rar:test\EwaveIOS_V2.1.12.ipa
		}
		return strReturn;
	}

	/**
	 * 附件名称在存本地时需要替换斜线
	 * 
	 * @param attachment
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-28
	 */
	public static String getAttFileNameChange(C35Attachment attachment) {
		boolean isCompress = attachment.isCompress();
		String strReturn;
		// (attachment.getC35Attachment().isCompress() ?
		// attachment.getC35Attachment().getFileName().split(":")[1] :
		// attachment.getC35Attachment().getFileName())
		if (!isCompress) {
			strReturn = attachment.getFileName();
		} else {
			strReturn = attachment.getFileName().split(":")[1].replaceAll(
					"\\\\", "__");// rar:test\EwaveIOS_V2.1.12.ipa
		}
		return strReturn;
	}

	/**
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-28
	 */
	public static String makeAttFileDiskID(C35Attachment attachment) {
		boolean isCompress = attachment.isCompress();
		String strReturn;
		if (!isCompress) {
			strReturn = attachment.getId();
		} else {
			strReturn = attachment.getId() + "_"
					+ attachment.getFileName().split(":")[1];// rar:test\EwaveIOS_V2.1.12.ipa
		}
		return strReturn;
	}

	/**
	 * 顶层调用停止下载后执行
	 * 
	 * @see:
	 * @since:
	 * @author: liujie
	 * @param updownclient
	 * @throws MessagingException
	 * @date:2012-2-16
	 */
	private void checkStopDownload(String uid, Socket updownclient)
			throws MessagingException {
		// TODO Auto-generated method stub
		Debug.d(TAG, "checkStopDownload = ");
		int progress = GlobalVariable.getAttProgress(uid);// 5281dbb21170a9b44ac0fbbe_inbox_1
		if (progress == GlobalVariable.STOP_DOWNLOAD
				|| GlobalVariable.isCancelDownload) {
			Debug.d(TAG, "checkStopDownload = " + uid);
			GlobalVariable.recordProgress(uid, -1);
			closeUpDownloadServer(updownclient);
			throw new MessagingException(MessagingException.STOP_LOAD_ATT);
		}
	}

	/**
	 * 切割一个整体附件
	 * 
	 * @param dataAll
	 * @param blockSize
	 * @return
	 * @see:
	 * @since:
	 * @author: liujie
	 * @date:2012-2-28
	 */
	private List<byte[]> splitBytes(byte[] dataAll, int blockSize) {
		List<byte[]> dataList = new ArrayList<byte[]>();
		if (dataAll.length <= blockSize) {// 不用切分
			dataList.add(dataAll);
		} else {
			int count = (dataAll.length + blockSize) / blockSize;// 多少个块数
			byte[] tempData = null;
			int startInd = 0;
			int currentBlockSize = 0;
			for (int i = 0; i < count; i++) {
				currentBlockSize = (i == count - 1) ? dataAll.length
						% blockSize : blockSize;
				tempData = new byte[currentBlockSize];
				System.arraycopy(dataAll, startInd, tempData, 0,
						currentBlockSize);
				startInd += blockSize;
				dataList.add(tempData);
			}
		}

		return dataList;
	}

	/**
	 * 组装数据
	 * 
	 * @param dataList
	 * @param total
	 * @return
	 * @see:
	 * @since:
	 * @author: liujie
	 * @date:2012-2-28
	 */
	private byte[] joinBytes(List<byte[]> dataList, int total) {
		byte[] result = null;
		try {
			if (dataList == null || dataList.size() == 0 || total <= 0) {
				return null;
			}

			result = new byte[total];
			byte[] tempData = null;
			int startInd = 0;

			for (int i = 0; i < dataList.size(); i++) {
				tempData = dataList.get(i);
				System.arraycopy(tempData, 0, result, startInd, tempData.length);
				startInd += tempData.length;
			}

		} catch (Exception e) {
			System.out.println("joinBytes  Exception" + e.toString());
		} catch (Error e) {
			System.out.println("joinBytes  Error  " + e.toString());
			throw new Error();
		} finally {
			return result;
		}

	}

	/**
	 * 往流里写数据的工具方法
	 * 
	 * @param dos
	 * @param obj
	 * @throws Exception
	 * @see:
	 * @since:
	 * @author: liujie
	 * @date:2012-2-28
	 */
	private void writeObj(OutputStream dos, UpLoadAttachmentRequest obj)
			throws Exception {
		byte[] dataContent = obj.getDataContent();
		obj.setDataContent(null);
		obj.setDataLength(dataContent.length);

		String blockHeader = JsonUtil.toJson(obj, false);
		byte[] blockHeaderBytes = blockHeader.getBytes();

		dos.write(' ');
		dos.write(blockHeaderBytes.length);
		dos.write(' ');
		dos.write(blockHeaderBytes);
		dos.write(' ');
		dos.write(dataContent);
		dos.write(' ');

		dos.flush();

	}

	/**
	 * 读文件到内存中
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 * @see:
	 * @since:
	 * @author: liujie
	 * @date:2012-2-28
	 */
	private byte[] readFile(File file) throws IOException {
		String exceptionData = null;
		if (!file.exists()) {
			return null;
		}
		byte[] dataAll = null;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		try {
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);

			dataAll = new byte[bis.available()];
			bis.read(dataAll);
		} catch (Exception e) {
			exceptionData = e.getMessage();
			throw new IOException(e.getMessage());
		} finally {
			try {
				bis.close();
				fis.close();
			} catch (Exception e1) {
				throw new IOException(exceptionData);
			}
		}
		return dataAll;
	}

	/**
	 * 写数据到一个文件中
	 * 
	 * @param file
	 * @param data
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: liujie
	 * @date:2012-2-28
	 */
	private void writeFile(File file, byte[] data) throws MessagingException {
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			if (!file.exists()) {
				File parentFile = file.getParentFile();
				if (!parentFile.exists()) {
					parentFile.mkdirs();
				}
				file.createNewFile();
			}
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(data);
			bos.flush();
		} catch (IOException e) {
			throw new MessagingException(MessagingException.DISK_IO_ERROR,
					e.getMessage());
		} finally {
			try {
				if (bos != null) {
					bos.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				Debug.e("failfast", "failfast_AA", e);
			}
		}
	}

	/**
	 * 写数据到一个文件中
	 * 
	 * @param file
	 * @param data
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: liujie
	 * @date:2012-2-28
	 */
	private void writeFile(File file, List<byte[]> data)
			throws MessagingException {
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			if (!file.exists()) {
				File parentFile = file.getParentFile();
				if (!parentFile.exists()) {
					parentFile.mkdirs();
				}
				file.createNewFile();
			}
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);

			for (int i = 0; i < data.size(); i++) {
				bos.write(data.get(i));
			}

			bos.flush();
		} catch (IOException e) {
			throw new MessagingException(MessagingException.DISK_IO_ERROR,
					e.getMessage());
		} finally {
			try {
				if (bos != null) {
					bos.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				Debug.e("failfast", "failfast_AA", e);
			}
		}
	}

	/**
	 * 获取加密压缩数据
	 * 
	 * @param length
	 * @return
	 * @throws IOException
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2013-5-29
	 */
	public byte[] readFully(int length) throws IOException {
		synchronized (readFullyLock) {
			// int totalCount = length;
			byte[] commandBytes = new byte[length];
			int readCount = 0; // 已经成功读取的字节的个数
			while (readCount < length) {
				if (mIn.available() > 0) {// 这个方法可以在读写操作前先得知数据流里有多少个字节可以读取
					readCount += mIn.read(commandBytes, readCount, length
							- readCount);
				} else {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						Debug.e(TAG, e.getMessage());
					}
				}

			}
			Debug.i("getmailid_data", new String(commandBytes));
			return commandBytes;
		}
	}

	/**
	 * 消耗掉最后的换行符号
	 * 
	 * @throws IOException
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2013-5-29
	 */
	public void clearLine() throws IOException {
		synchronized (clearLineLock) {
			byte[] data = new byte[1];
			int count = 0;
			for (; true;) {
				count = mIn.read(data);
				if (count == 1) {// 取到数据
					if (data[0] == '\n') {
						break;
					}
				} else {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						Debug.e(TAG, e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * 读取输入流的数据并解析，下载某一块儿数据时做的
	 * 
	 * @param bis
	 * @return
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: liujie
	 * @date:2012-2-28
	 */
	private DownloadDataResponse readDownloadDataResponse(InputStream bis)
			throws MessagingException {
		try {
			DataInputStream dis = new DataInputStream(bis);
			int temp;
			temp = dis.read();// " "
			Debug.i(TAG, "------------temp:1:" + temp);
			// int blockHeaderSize = Math.abs(dis.readInt());
			int blockHeaderSize = dis.read();
			// blockHeaderSize);
			Debug.i(TAG, "-----------------blockHeaderSize:" + blockHeaderSize);
			temp = dis.read();// " "
			Debug.d(TAG, "blockHeaderSize = " + blockHeaderSize);
			if (blockHeaderSize < 0 || blockHeaderSize > 20000) {
				throw new MessagingException(MessagingException.OUT_OF_MEMORY,
						"拿到的块信息错误");
			}
			byte[] blockHeaderBytes = new byte[blockHeaderSize];
			dis.readFully(blockHeaderBytes);
			String blockHeader = new String(blockHeaderBytes);
			Debug.d(TAG, "blockHeader = " + blockHeader);
			temp = dis.read();// " "

			JSONObject jsonObject = new JSONObject(blockHeader);
			DownloadDataResponse rquest = JsonUtil.parseAtt(jsonObject);

			byte[] dataContent = new byte[rquest.getDataLength()];
			dis.readFully(dataContent);
			rquest.setDataContent(dataContent);
			temp = dis.read();// " "
			Debug.v(TAG, "-download-header:" + blockHeader);
			Debug.v(TAG, "-download-data-length:" + dataContent.length);
			return rquest;
		} catch (IOException e) {
			e.printStackTrace();
			throw new MessagingException(MessagingException.CONNECT_ERROR);
		} catch (Exception e) {
			throw new MessagingException(MessagingException.OUT_OF_MEMORY);
		}
	}

	/**
	 * 服务器返回值判断
	 * 
	 * @param responseStr
	 * @return
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: hanlixia
	 * @date:2012-11-2
	 */
	protected boolean isResponseOk(String responseStr, String strExceptionInfo)
			throws MessagingException {
		if (responseStr == null || responseStr.length() == 0)
			throw new MessagingException(
					MessagingException.RESPONSE_ERROR_FORMAT);
		responseStr = responseStr.substring(responseStr.indexOf(",") + 1);// downloadData,200,{"totalBlock":1,"dataContent":"789C9CB765501C51132EC2CFFD93CCA3753","currentBlock":1}
		responseStr = responseStr.substring(responseStr.indexOf(",") + 1);// 200,{"totalBlock":1,"dataContent":"789C9CB765501C51132EC2CFFD93CCA3753","currentBlock":1}
		if (responseStr.startsWith("200")) {
			return true;
		} else {
			Debug.w(TAG, responseStr);
			if (responseStr.contains("903,")) {
				throw new MessagingException(MessagingException.CODE_AUTH_NO);
			} else {// 909
				throw new MessagingException(
						MessagingException.DOWNLOAD_ATTACHMENT_ERROR_MESSAGE,
						responseStr + "\n " + strExceptionInfo);
			}
		}
	}

	/**
	 * 向socket 传入命令
	 * 
	 * @param commandString
	 * @throws IOException
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-12-2
	 */
	public void writeLine(String commandString) throws IOException {
		// mOutputStreamPrintWriter.println(commandString);
		mOut.write(commandString.getBytes());
		mOut.write("\r\n".getBytes());
		mOut.flush();
	}

	// /**
	// * 从socket 读出数据
	// * @return
	// * @throws IOException
	// * @see:
	// * @since:
	// * @author: hanlixia
	// * @date:2012-11-2
	// */
	// public String readLine2() throws IOException {
	// String result = "";
	// while ((result = sockin.readLine()) != null) {
	// return result;
	// }
	// return result;
	// }
	// 10是换行键，13是回车键
	// private static final int _CR = 13;
	// private static final int _LF = 10;
	// private int _last = -1; // The last char we've read
	// private int _ch = -1; // currently read char
	// /**
	// * InputStream.read 返回
	// * @Description:
	// * @return
	// * @throws IOException
	// * @see:
	// * @since:
	// * @author: cuiwei
	// * @date:2013-11-20
	// */
	// public String readLine() throws IOException {
	// synchronized (readLineLock) {
	// ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// if (_last != -1)
	// baos.write(_last);
	// int _ch = mIn.read();
	// while (_ch != _CR && _ch != _LF && _ch != _last) {
	// baos.write(_ch);
	// _ch = mIn.read();
	// }
	// // Read the next byte and check if it's a LF
	// _last = mIn.read();
	// if (_last == _LF) {
	// _last = -1;
	// }
	// return new String(baos.toByteArray());
	// }
	// }
	/**
	 * InputStream.read 返回
	 * 
	 * @Description:
	 * @return
	 * @throws IOException
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-20
	 */
	public String readLine() throws IOException {
		// return sockin.readLine();
		String retLine = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] data = new byte[1];// 最多读取这么多个字节(最少1个)
		int count = 0;
		for (; true;) {
			try {
				count = mIn.read(data);
			} catch (IOException e) {
				long started = System.currentTimeMillis();
			}
			if (-1 == count) {// 得到空数据? read()方法的API说如果因为已经到达流末尾而没有可用的字节，则返回值 -1
								// 。在输入数据可用、检测到流末尾或者抛出异常前，此方法一直阻塞。
				System.out.println("-1-1-1");
				return "";// 读到的是Netty的信号量？
			} else if (1 == count) {// 取到数据
				if (data[0] == '\r') {// '\r' ASCII码为13 是回车
					byte[] dataSecond = new byte[1];
					if (mIn.read(dataSecond) == 1) {
						if (dataSecond[0] == '\n') {// '\n'ASCII码为10
													// 是换行，将当前位置移到下一行
							break;
						} else {
							baos.write(data);
							baos.write(dataSecond);
						}
					}
				} else {
					baos.write(data);
				}
			} else {// 一般不可能走到这里
				return "count=" + count;
			}
		}
		retLine = new String(baos.toByteArray());
		return retLine;
	}

	/**
	 * 初始化mHost,mPort,mUserInfoParts,连接服务器前必调
	 * 
	 * @param uri
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-29
	 */
	public void setUri(URI uri) {
		mHost = uri.getHost();// mail.35.cn
		if (uri.getPort() >= 0) {
			mPort = uri.getPort();
		} else {
			mPort = MailUtil.PROXY_SERVER_MAIL_PORT; // 9999
		}
		if(mHost.equals("mail.gzsq.com")){
			mPort = 88;
		}
		if (uri.getUserInfo() != null) {
			mUserInfoParts = uri.getUserInfo().split(":", 2);// [pm3@35.cn,
																// qyyx12369A]
		}
	}

	public String[] getUserInfoParts() {
		// TODO Auto-generated method stub
		return mUserInfoParts;
	}

	/**
	 * transport是否开通,mSocket.isConnected()
	 * 
	 * @return
	 * @see:
	 * @since:
	 * @author: hanlixia
	 * @date:2012-11-2
	 */
	public boolean isSocketAndStreamOpen() throws MessagingException {
		// Debug.i(TAG, "mIn != null:" + (mIn != null) + "  mOut != null:" +
		// (mOut != null) +
		// "  mSocket != null: " + (mSocket != null));
		// if (mSocket != null) {
		// Debug.i(TAG, "   mSocket.isConnected() :" + mSocket.isConnected() +
		// "   mSocket.isClosed():" +
		// mSocket.isClosed());
		// }
		Log.d("TAG", "搜索端口号"+mHost+":"+mPort);
		try {// mSocket.sendUrgentData(SOCKET_CONNECT_TIMEOUT);
			return (mIn != null && mIn.available() == 0 && mOut != null
					&& mSocket != null && mSocket.isConnected() && !mSocket
						.isClosed());
		} catch (IOException e) {
			throw new MessagingException(
					MessagingException.RETURN_COMMAND_ERROR_VERSION,
					"openAndGetTicket failed!!mIn.available()");
		}
	}

	/**
	 * 关闭与服务器的连接
	 * 
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-11-29
	 */
	public void close() {
		commandId = 0;
		try {
			if (mIn != null) {
				mIn.close();
			}
		} catch (Exception e) {
		} finally {
			try {
				if (mOut != null) {
					mOut.close();
				}
			} catch (IOException e) {
				// java.net.SocketException: socket closed 连接已经断开,尝试重新连接服务器
				// Debug.e("failfast", "failfast_AA", e);
			} finally {
				try {
					if (mSocket != null) {
						mSocket.close();
					}
				} catch (IOException e) {
					Debug.e("failfast", "failfast_AA", e);
				} finally {
					mIn = null;
					mOut = null;
					mSocket = null;
				}
			}
		}
	}

	public String getHost() {
		// TODO Auto-generated method stub
		return mHost;
	}

	public int getPort() {
		// TODO Auto-generated method stub
		return mPort;
	}

	public static int getCommandId() {
		return ++commandId;
	}
}
