package cn.mailchat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.os.Environment;

/**
 * collect log & exception<br>
 * 
 * @author xulei
 * 
 */
public class Logbot implements UncaughtExceptionHandler {

	public static final String LOG_ID = "log_msg";
	private static final long MIN_LOG_FILE_SIZE = 1024;
	private static final long AUTO_COMMIT_FILE_SIZE = 50 * 1024;
	private static final long AUTO_DUMP_FILE_SIZE = 200 * 1024;
	private static final long MAX_LOG_FILE_SIZE = 500 * 1024;
	private static final int LOG_FILE_LIMIT = 3;
	private static final String SD_LOG_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "mailchat_log/";
	private static final String LOG_FILE_NAME = "mailchat_log.txt";

	private static final String FILE_EXTENSION = ".txt";
	private static final String FILE_PRIFIX = "mailchat_d_";
	public static final String NEW_LINE = "\r\n";
	private SimpleDateFormat logDateFormat;
	private SimpleDateFormat fileNameDateFormat;

	private UncaughtExceptionHandler defaultExceptionHandler;

	private File logFile;
	private FileOutputStream out;

	private static class SingletonHolder {

		public static final Logbot INSTANCE = new Logbot();
	}

	public static Logbot getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private Logbot() {
		logDateFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
		fileNameDateFormat = new SimpleDateFormat("MMdd_HHmmss", Locale.US);
		checkReport();
	}

	public void init(Context context) {
		defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && defaultExceptionHandler != null) {
			defaultExceptionHandler.uncaughtException(thread, ex);
		} else {
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
		}
	}

	private boolean handleException(Throwable ex) {
		write(formatException(ex, "crash"));
		return false;
	}

	public synchronized void onLog(String tag, String message) {
		write(formatLog(message));
	}

	public synchronized void onLog(String message) {
		onLog("-", message);
	}

	public synchronized void onException(Throwable e) {
		write(formatException(e, "error"));
	}

	private String formatException(Throwable ex, String tag) {
		StringBuilder builder = new StringBuilder();
		Writer causeWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(causeWriter);
		if (ex != null) {
			if (ex.getCause() != null) {
				ex.getCause().printStackTrace(printWriter);
			} else {
				ex.printStackTrace(printWriter);
			}
		} else {
			printWriter.write("exception is null ");
		}
		builder.append(NEW_LINE);
		builder.append(formatLog(tag));
		builder.append(causeWriter.toString());
		builder.append(NEW_LINE);
		return builder.toString();
	}

	private String formatLog(String log) {
		StringBuilder builder = new StringBuilder();
		builder.append(logDateFormat.format(new Date()));
		builder.append(" ");
		builder.append(log);
		builder.append(NEW_LINE);
		return builder.toString();
	}

	public void exprot2SDCard() {

	}

	private void write(String data) {
		try {
			if (checkReport()) {
				out.write(data.getBytes());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean checkReport() {
		boolean prepared = false;
		try {
			if (logFile == null || out == null) {
				logFile = new File(MailChat.getInstance().getFilesDir(), LOG_FILE_NAME);
				out = new FileOutputStream(logFile, true);
				write(NEW_LINE + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) + NEW_LINE);
				prepared = true;
			} else {
				if (logFile.length() > AUTO_DUMP_FILE_SIZE) {
					dumpReport();
					List<File> logs = getDumpedLogs();
					if (logs.size() > LOG_FILE_LIMIT) {
						for (int i = 0; i < logs.size() - LOG_FILE_LIMIT; i++) {
							logs.get(i).delete();
						}
					}
					// commit();
				}
				prepared = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			prepared = false;
		}
		return prepared;
	}

	private String getFileName() {
		StringBuilder builder = new StringBuilder();
		builder.append(FILE_PRIFIX);
		builder.append(fileNameDateFormat.format(new Date()));
		builder.append(FILE_EXTENSION);
		return builder.toString();
	}

	private String readReport(File file) throws Exception {
		FileInputStream in = new FileInputStream(file);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(in, out);
		return new String(out.toByteArray());
	}

	// public void forceCommit() {
	// try {
	// dumpReport();
	// List<File> logs = getDumpedLogs();
	// uploadLogs(logs);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	public void dumpReport() throws Exception {
		logFile.renameTo(new File(MailChat.getInstance().getFilesDir(), getFileName()));
		logFile = new File(MailChat.getInstance().getFilesDir(), LOG_FILE_NAME);
		out = new FileOutputStream(logFile, true);
	}

	// public void commit() {
	// boolean commit = false;
	// List<File> logs = filterDumpedLogs(MAX_LOG_FILE_SIZE, MIN_LOG_FILE_SIZE, LOG_FILE_LIMIT);
	// for (File file : logs) {
	// if (file.length() > AUTO_COMMIT_FILE_SIZE) {
	// commit = true;
	// break;
	// }
	// }
	// if (commit && NetUtil.isWifiConnecting(MailChat.getInstance())) {
	// SharedPreferences preferences = MailChat.getInstance().getSharedPreferences("common_config", 0);
	// int autoTimes = preferences.getInt("auto_commit_times", 0);
	// if (autoTimes < 3) {
	// commit2Server(logs);
	// autoTimes++;
	// preferences.edit().putInt("auto_commit_times", autoTimes).commit();
	// }
	// }
	// }

	// private void uploadLogs(List<File> logs) {
	// AccountLogic logic = AccountLogic.getInstance();
	// if (logic != null) {
	// Account account = logic.getDefaultAccount();
	// MailTransport transport = MessageController.getInstance().mailTransport;
	// if (logs != null && logs.size() > 0 && account != null && transport != null && transport.connection !=
	// null && transport.connection.isOpen()) {
	// LocalMessage message = report2Message(account, logs);
	// if (message != null) {
	// transport.sendMessage(account, message);
	// }
	// }
	// }
	// }

	public void clearLogs() {
		List<File> files = getDumpedLogs();
		for (File file : files) {
			file.delete();
		}
	}

	public List<File> getDumpedLogs() {
		List<File> results = new ArrayList<File>();
		File[] files = MailChat.getInstance().getFilesDir().listFiles();
		for (File file : files) {
			if (file.getName().startsWith(FILE_PRIFIX)) {
				results.add(file);
			}
		}
		return results;
	}

	public List<File> filterDumpedLogs(long maxSize, long minSize, int limit) {
		List<File> results = new ArrayList<File>();
		List<File> allLogs = getDumpedLogs();
		for (File file : allLogs) {
			if (file.length() < maxSize && file.length() > minSize) {
				results.add(file);
			}
		}
		Collections.reverse(results);
		if (results.size() > limit) {
			return results.subList(0, limit);
		}
		return results;
	}

	// private LocalMessage report2Message(Account account, List<File> logs) {
	// LocalMessage message = null;
	// try {
	// message = new LocalMessage();
	// message.setSid(LOG_ID);
	// message.setSender(account.getEmail());
	// List<String> me = new ArrayList<String>();
	// me.add(LOG_SEND_TO);
	// message.setToAddressList(me);
	// message.setSubject(LOG_SUBJECT);
	// List<Attachment> attachments = new ArrayList<Attachment>();
	// if (logs.size() > 1) {
	// long totalSize = 0;
	// for (File file : logs) {
	// totalSize = totalSize + file.length();
	// if (totalSize > 1024 * 1024) {
	// break;
	// }
	// Attachment attachment = new Attachment();
	// attachment.setAttachmentId(0);
	// attachment.setName(file.getName());
	// attachment.setFilePath(file.getPath());
	// attachments.add(attachment);
	// }
	// message.setAttachments(attachments);
	// } else if (logs.size() == 1) {
	// message.setPlainText(getDeviceInfo() + NEW_LINE + readReport(logs.get(0)));
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return message;
	// }

}
