package cn.mailchat.chatting.protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import cn.mailchat.MailChat;

public class Uploader {
	private static final String CHARSET = "UTF-8";
	private static final String BOUNDARY_MARK = "--";
	private static final String LINE_END = "\r\n";
	private static final String QUOTATION_MARK = "\"";
	private static final int BUFFER_SIZE = 4 * 1024;
	private boolean interrupt = false;
	private String currentId;
	
	public JSONObject uploadFile(String requestUrl, File file,Map<String, String> params,String requestCommand,String attchmentId,UploadCallback uploadCallback){
		currentId = attchmentId;
		JSONObject json = null;
		//上传
		CountingOutputStream countingOutputStream=null;
		FileInputStream inputStream =null;
		//响应
		CountingInputStream countingInputStream =null;
		ByteArrayOutputStream responseOutStream =null;
		try {
			HttpURLConnection connection = Request.prepareHttpsConnection(requestUrl, "POST");
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
				countingOutputStream =new CountingOutputStream(outStream);
				countingOutputStream.write(header);
				// write file
				inputStream = new FileInputStream(file);
				int length = -1;
				int count =0;
				int currentProgress = 0;
				byte[] buffer = new byte[BUFFER_SIZE];
				while (!interrupt&&(length = inputStream.read(buffer,0,BUFFER_SIZE)) != -1) {
					countingOutputStream.write(buffer, 0, length);
					count+=length;
					int progress = (int) (count * 100 / file.length());
					if (currentProgress != progress) {
						currentProgress = progress;
						uploadCallback.uploadProgress(attchmentId, progress);
					}
					
				}
				countingOutputStream.write(end);
				Log.i(MailChat.LOG_COLLECTOR_TAG, "HTTPS (上传)==> :"+countingOutputStream.getByteCount());
				countingOutputStream.flush();
				if(interrupt){
					uploadCallback.uploadInterrupt(attchmentId);
				}
				int code = connection.getResponseCode();
				if (!interrupt&&code == 200) {
					countingInputStream = new CountingInputStream(connection.getInputStream());
					responseOutStream = new ByteArrayOutputStream();
					byte[] responseBuffer = new byte[512];
					int responseLength = -1;
					while ((responseLength = countingInputStream.read(responseBuffer)) != -1) {
						responseOutStream.write(responseBuffer, 0, responseLength);
					}
					responseOutStream.flush();
					Log.i(MailChat.LOG_COLLECTOR_TAG, "HTTPS ==> 响应:"+ countingInputStream.getByteCount());
					String jsonString = new String(responseOutStream.toByteArray());
					if (jsonString != null && !"".equals(jsonString)) {
						json = new JSONObject(jsonString);
						if (!json.isNull("error")) {
							json=null;
						}
					}
				}
			}
		} catch (IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			json=null;
		} finally{
			try {
				if(inputStream!=null){
					inputStream.close();
				}
				if(countingOutputStream!=null){
					countingOutputStream.close();
				}
				if(countingInputStream!=null){
					countingInputStream.close();
				}
				if(responseOutStream!=null){
					responseOutStream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return json;
	}
	
	public void cancel(String id) {
		if (id.equals(currentId)) {
			interrupt = true;
		}
	}
}
