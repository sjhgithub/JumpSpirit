package cn.mailchat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.james.mime4j.codec.EncoderUtil;
import org.apache.james.mime4j.util.MimeUtil;

import cn.mailchat.controller.MessagingController;
import cn.mailchat.mail.Address;
import cn.mailchat.mail.Flag;
import cn.mailchat.mail.Message.RecipientType;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.mail.internet.MimeBodyPart;
import cn.mailchat.mail.internet.MimeHeader;
import cn.mailchat.mail.internet.MimeMessage;
import cn.mailchat.mail.internet.MimeMultipart;
import cn.mailchat.mail.internet.MimeUtility;
import cn.mailchat.mail.internet.TextBody;
import cn.mailchat.mail.store.LocalStore.TempFileBody;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * 收集logcat日志
 * 
 * @author LL
 */
public class LogCollector {
	
	private static final int MAX_LOG_LINES = 500;
	private static final String LINE_SEPARATOR = "\r\n";
	private static final String LOG_SUBJECT = "邮洽调试日志";
	
	public static final String LOG_FILE = "log.txt";
	public static final String LOG_RECIPIENT = "fb@mailchat.cn";
	
	private Context mContext;
    private String[] mFilterSpecs;
    private String mFormat;
    private String mBuffer;
    
    private CollectLogTask mTask;
    
    public LogCollector(String[] filterSpecs,
    		String format,
    		String buffer) {
    	mContext = MailChat.getInstance();
    	mFilterSpecs = filterSpecs;
    	mFormat = format;
    	mBuffer = buffer;
    }
    
    public void startLog() {
        ArrayList<String> list = new ArrayList<String>();
        
        if (mFormat != null) {
            list.add("-v");
            list.add(mFormat);
        }
        
        if (mBuffer != null) {
            list.add("-b");
            list.add(mBuffer);
        }
        
        if (mFilterSpecs != null) {
            for (String filterSpec : mFilterSpecs) {
                list.add(filterSpec);
            }
        }
        
        mTask = new CollectLogTask();
        //mTask.execute(list);
        mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, list);
        
        //MailChat.DEBUG = true;
        Log.v(MailChat.LOG_COLLECTOR_TAG, "Start collect log");
    }
    
    public void stopLog() {
    	//MailChat.DEBUG = false;
    	Log.v(MailChat.LOG_COLLECTOR_TAG, "Stop collect log");
    	try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// DO NOTHING
		}
    	
    	mTask.cancel(false);
    	Log.v(MailChat.LOG_COLLECTOR_TAG, "NOOP");
    }
    
	private class CollectLogTask extends AsyncTask<ArrayList<String>, Void, StringBuilder> {
		
		@Override
	    protected void onPreExecute() {
	    	
	    }
	    
	    @Override
	    protected StringBuilder doInBackground(ArrayList<String>... params) {
	        
	    	File logFile = new File(mContext.getFilesDir(), LOG_FILE);
	    	logFile.delete();
	    	
	    	StringBuilder mLog = new StringBuilder();
	    	Process mProcess = null;
	    	BufferedReader mReader = null;
	    	
	        try {
	            ArrayList<String> commandLine = new ArrayList<String>();
	            commandLine.add("logcat");
	            ArrayList<String> arguments = ((params != null) && (params.length > 0)) ? params[0] : null;
	            if (null != arguments) {
	                commandLine.addAll(arguments);
	            }
	            
	            mProcess = Runtime.getRuntime().exec(commandLine.toArray(new String[0]));
	            mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
	            
	            String line = "";
	            int lines = 0;
	            do {
	                mLog.append(line);
	                mLog.append(LINE_SEPARATOR);
	                
	                if (lines++ >= MAX_LOG_LINES) {
	                	lines = 0;
	                	appendLog(mLog);
	                }
	            } while ((line = mReader.readLine()) != null
	            		&& !isCancelled());
	        } catch (IOException e) {
	            Log.e(MailChat.LOG_TAG, "CollectLogTask.doInBackground failed", e);
	        } finally {
	        	if (mReader != null) {
	        		try {
	        			mReader.close();
	        		} catch(Exception e) {
	        			// DO NOTHING
	        		}
	        		mReader = null;
	        	}
	        	
	        	if (mProcess != null) {
	        		mProcess.destroy();
	        		mProcess = null;
	        	}
	        }
	        
	        return mLog;
	    }
	    
	    @Override
	    protected void onPostExecute(StringBuilder log) {
	    	sendLog(LOG_RECIPIENT);
	    }
	    
	    @Override
	    protected void onCancelled(StringBuilder log) {
    		appendLog(log);
    		sendLog(LOG_RECIPIENT);
	    }
	    
	    private void appendLog(StringBuilder log) {
	    	if (log != null) {
	    		
	    		FileOutputStream outputStream;
	    		try {
	    		  outputStream = mContext.openFileOutput(LOG_FILE, Context.MODE_APPEND);
	    		  outputStream.write(log.toString().getBytes());
	    		  outputStream.close();
	    		} catch (Exception e) {
	    		  e.printStackTrace();
	    		}
	    			    		
	            log.delete(0, log.length());
	        }
	    }
	}
	
	public static void saveLog(File file) {
	    Log.d(MailChat.LOG_COLLECTOR_TAG, "SAVE LOG");
	    
        StringBuilder log = new StringBuilder();
        Process process = null;
        BufferedReader reader = null;
        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file, false);
            
            ArrayList<String> commandLine = new ArrayList<String>();
            commandLine.add("logcat");
            commandLine.add("-d");
            commandLine.add("-v");
            commandLine.add("threadtime");
            commandLine.add(MailChat.LOG_COLLECTOR_TAG + ":V");
            commandLine.add("*:E");
            
            process = Runtime.getRuntime().exec(commandLine.toArray(new String[0]));
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line = "SAVE LOG";
            int lines = 0;
            do {
                log.append(line);
                log.append(LINE_SEPARATOR);
                
                if (lines++ >= MAX_LOG_LINES) {
                    lines = 0;
                    outputStream.write(log.toString().getBytes());
                    log.delete(0, log.length());
                }
            } while ((line = reader.readLine()) != null);
            
            if (log.length() > 0) {
                outputStream.write(log.toString().getBytes());
            }
            outputStream.write((LINE_SEPARATOR
                    + "-------------------------------"
                    + LINE_SEPARATOR).getBytes());
            outputStream.write(buildLogBody().getBytes());
        } catch (IOException e) {
            Log.e(MailChat.LOG_TAG, "Save log failed", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch(Exception e) {
                    // DO NOTHING
                }
                outputStream = null;
            }
            
            if (reader != null) {
                try {
                    reader.close();
                } catch(Exception e) {
                    // DO NOTHING
                }
                reader = null;
            }
            
            if (process != null) {
                process.destroy();
                process = null;
            }
        }
	}
	
    public static void sendLog(String recipient) {
    	Application app = MailChat.getInstance();
    	
    	try {
	    	File logFile = new File(app.getFilesDir(), LOG_FILE);
	    	if (!logFile.exists()) {
	    		MailChat.toast(app.getString(R.string.log_file_does_not_exist));
	    		throw new MessagingException("LOG FILE DOES NOT EXIST!");
	    	}
	    	
	    	MessagingController messagingController = MessagingController.getInstance(app);
	    	Account account = Preferences.getPreferences(app).getDefaultAccount();
	    	
	    	MimeMessage message = new MimeMessage();
	    	message.setSendMessage(true);
	    	message.addSentDate(new Date());
	    	message.setFrom(new Address(account.getEmail(), account.getName()));
	    	message.setRecipient(RecipientType.TO, new Address(recipient));
	    	message.setSubject(LOG_SUBJECT);
	    	
	    	/*
			StringBuilder data = new StringBuilder("");
	        try {
	            FileInputStream fIn = mContext.openFileInput(LOG_FILE) ;
	            InputStreamReader isr = new InputStreamReader(fIn) ;
	            BufferedReader reader = new BufferedReader(isr) ;
	
	            String readString = reader.readLine() ;
	            while (readString != null) {
	                data.append(readString);
	                readString = reader.readLine() ;
	            }
	
	            isr.close();
	        } catch(IOException e) {
	            throw new MessagingException("Read log failed.", e);
	        }
	        */
	    	
	    	MimeMultipart mp = new MimeMultipart();
			mp.addBodyPart(new MimeBodyPart(new TextBody(buildLogBody()), "text/plain"));
			
			TempFileBody tfb = new TempFileBody(logFile.getAbsolutePath());
			MimeBodyPart mbp = new MimeBodyPart(tfb);
			mbp.addHeader(MimeHeader.HEADER_CONTENT_TYPE,
					String.format("application/octet-stream;\r\n name=\"%s\"", LOG_FILE));
			mbp.setEncoding(MimeUtil.ENC_BASE64);
			mbp.addHeader(MimeHeader.HEADER_CONTENT_DISPOSITION,
					String.format("attachment;\r\n filename=\"%s\"", LOG_FILE));
			mp.addBodyPart(mbp);
			message.setBody(mp);
			message.setFlag(Flag.X_DOWNLOADED_FULL, true);
			
			if (!messagingController.sendMessage(account, message, null)) {
			    throw new MessagingException("Send message failed");
			}
    	} catch (MessagingException e) {
    		MailChat.toast(app.getString(R.string.send_log_failed));
    		Log.e(MailChat.LOG_TAG, "Send log failed", e);
    	}
    }
	
	private static String buildLogBody() {
		StringBuilder body = new StringBuilder();
		body.append(String.format("%-20s%s%n", "Board:", Build.BOARD));
		body.append(String.format("%-20s%s%n", "Brand:", Build.BRAND));
		body.append(String.format("%-20s%s%n", "Device:", Build.DEVICE));
		body.append(String.format("%-20s%s%n", "Display:", Build.DISPLAY));
		body.append(String.format("%-20s%s%n", "Fingerprint:", Build.FINGERPRINT));
		body.append(String.format("%-20s%s%n", "Hardware:", Build.HARDWARE));
		body.append(String.format("%-20s%s%n", "Host:", Build.HOST));
		body.append(String.format("%-20s%s%n", "ID:", Build.ID));
		body.append(String.format("%-20s%s%n", "Manufacturer:", Build.MANUFACTURER));
		body.append(String.format("%-20s%s%n", "Model:", Build.MODEL));
		body.append(String.format("%-20s%s%n", "Product:", Build.PRODUCT));
		body.append(String.format("%-20s%s%n", "Radio:", Build.getRadioVersion()));
		body.append(String.format("%-20s%s%n", "Serial:", Build.SERIAL));
		body.append(String.format("%-20s%s%n", "Tags:", Build.TAGS));
		body.append(String.format("%-20s%s%n", "Time:", Build.TIME));
		body.append(String.format("%-20s%s%n", "Type:", Build.TYPE));
		body.append(String.format("%-20s%s%n", "User:", Build.USER));

		Context context = MailChat.getInstance();
		TelephonyManager telephonyManager
			= (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		body.append(String.format("%-20s%s%n", "IMEI:", telephonyManager.getDeviceId()));

		body.append(String.format("%-20s%s%n",
				"MailChat:",
				context.getString(R.string.version_name)));

		return body.toString();
	}
}
