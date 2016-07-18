package cn.mailchat.chatting.protocol;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.input.CountingInputStream;

import android.util.Log;
import cn.mailchat.MailChat;
import cn.mailchat.utils.EncryptUtil;

public class Downloader {

	private static final String CHARSET = "UTF-8";
	private static final int BUFFER_SIZE = 4 * 1024;
	private static final String POST = "POST";
	private static final String GET = "GET";

	private boolean interrupt = false;
	private String currentId;

	public void downloadFile(String requestUrl, String savePath, String attchmentId,String fileid,String fileName,long fileSize,DownloadCallback callback) {
		try {
			StringBuilder builder = new StringBuilder();
			fileName=URLEncoder.encode(fileName,CHARSET);
			builder.append("/").append(fileid).append("/").append(fileName);
			requestUrl =requestUrl+builder.toString();
			interrupt = false;
			currentId = attchmentId;
			String tempPath = savePath+EncryptUtil.getMd5(currentId);
			File tempFile = new File(tempPath + ".tem");
			if (tempFile.length() > 0) {
				callback.downloadProgress(currentId,(int) (tempFile.length() * 100 / fileSize),tempFile.length());
			}
			Map<String, String> extraProperties = new HashMap<String, String>();
			extraProperties.put("Range", "bytes=" + tempFile.length() + "-");
			HttpURLConnection connection = Request.prepareHttpsConnection(requestUrl, GET);
			if (extraProperties != null && extraProperties.size() > 0) {
				for (Map.Entry<String, String> entry : extraProperties.entrySet()) {
					connection.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}
			int code = connection.getResponseCode();
			if (code == 200 || code == 206) {
				long size = connection.getContentLength();
				long downloadedSize = tempFile.length();
				RandomAccessFile out = new RandomAccessFile(tempFile, "rw");
				InputStream in = connection.getInputStream();
				CountingInputStream countingInputStream =new CountingInputStream(in);
				byte[] buffer = new byte[BUFFER_SIZE];
				out.seek(out.length());
				int count = 0, n = 0;
				int currentProgress = 0;
				while (!interrupt && (n = countingInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
					out.write(buffer, 0, n);
					count += n;
					int progress = (int) ((downloadedSize + count) * 100 / (downloadedSize + size));
					if (currentProgress != progress) {
						currentProgress = progress;
						callback.downloadProgress(currentId,progress,downloadedSize + count);
					}
				}
				Log.i(MailChat.LOG_COLLECTOR_TAG,"HTTPS ==> (断点续传-文件下载) 下载:"+countingInputStream.getByteCount());
				out.close();
				countingInputStream.close();
				connection.disconnect();
				String attPath = MailChat.getInstance().getAttFilePath(savePath, currentId, fileName);
				File file = new File(attPath);
				if (interrupt) {
					callback.downloadInterrupt(currentId);
					if (count == size) {
						tempFile.renameTo(file);
						callback.downloadFinished(currentId);
					}
				} else {
					if (count != size) {
						Log.e(MailChat.LOG_TAG,"download finished but file size seems wrong!!!");
					}
					tempFile.renameTo(file);
					callback.downloadFinished(currentId);
				}
				currentId = null;
			} else {
				callback.downloadFailed(currentId, new Exception("error: "+ code));
				currentId = null;
			}
		} catch (Exception e) {
			callback.downloadFailed(currentId, e);
			currentId = null;
		}
	}

	public void cancel(String id) {
		if (id.equals(currentId)) {
			interrupt = true;
		}
	}
}
