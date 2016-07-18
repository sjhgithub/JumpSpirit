package cn.mailchat.activity;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.beans.PickedFileInfo;
import cn.mailchat.utils.FileUtil;

/**
 * @Description:选择需要添加附件的Activity
 * @author:
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class FilePickerActivity extends BaseActionbarFragmentActivity {

	static final String TAG = "AttachmentPicker";
	private static final String PACKAGE_NAME = "cn.mailchat.activity";
	private static final String CLASS = PACKAGE_NAME + ".AttachmentPicker";

	public static final String DEF_DIR = GlobalConstants.DEF_DIR;
	public static final String FILE_NAME = CLASS + ".filename";
	public static final String BACK_DATA = CLASS + ".backdata";

	public static final String PREF_INFO = CLASS + ".pref_info";
	public static final String TAG_NAME = CLASS + ".dirname";
	public static final String ATTACHMENT_DATA = "attachment data";
	// public static final String ATTACHMENT_SIZE = "attachment size";

	public static final String ATTACHMENT_PATH = "attachmentpath";

	private String mCurDir = DEF_DIR;
	// private String mFileName;

	private ListView mListDir;

	private ArrayList<PickedFileInfo> mFolderInfoList = new ArrayList<PickedFileInfo>();
	private Stack<String> mDirHistory = new Stack<String>();

	public Map<String, PickedFileInfo> checkedFileMap = new HashMap<String, PickedFileInfo>();// 选中的文件集合
																								// <文件路径，文件路径>
	private Button fileupload_btn_ok;
	private Button fileupload_btn_cancel;
	private boolean singleChoice;// 是否是单选
	public static final String SINGLE_CHOICE = "single_choice";
	private View mCustomActionbarView;
	private TextView mActionBarTitle;
	private TextView mActionBarSubTitle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			mCurDir = getInitDir();
			setContentView(R.layout.activity_file_explorer);
			singleChoice = getIntent().getBooleanExtra(SINGLE_CHOICE, false);
			initializeActionBar();
			initActionbarView();
			initViews();
			updateList();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initializeActionBar() {
		mActionBar.setTitle(null);
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayUseLogoEnabled(true);
		// 返回按钮
		mActionBar.setDisplayHomeAsUpEnabled(true);
		// Inflate the custom view
		LayoutInflater inflater = LayoutInflater.from(this);
		mCustomActionbarView = inflater.inflate(
				R.layout.actionbar_custom_center_titles, null);
		mActionBar.setCustomView(mCustomActionbarView);
	}

	private void initActionbarView() {
		mActionBarTitle = (TextView) mCustomActionbarView
				.findViewById(R.id.actionbar_title_name);

		mActionBarSubTitle = (TextView) mCustomActionbarView
				.findViewById(R.id.actionbar_title_sub);
		mActionBarTitle.setText(getString(R.string.file_picker_title));
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (goBackFolder())
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void finalize() {
		mFolderInfoList.clear();
		mFolderInfoList = null;

		mDirHistory.clear();
		mDirHistory = null;
	}

	private void initViews() {
		mListDir = (ListView) findViewById(R.id.attachmentpicker_lv_directory);
		mListDir.setAdapter(new PickerAdapter(this, singleChoice));
		mListDir.setOnItemClickListener(mLsnListItemClick);

		fileupload_btn_cancel = (Button) findViewById(R.id.fileupload_btn_cancel);
		fileupload_btn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 取消上传
				finish();
			}
		});
		fileupload_btn_ok = (Button) findViewById(R.id.fileupload_btn_ok);
		fileupload_btn_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				upload();

				// for (Entry<String, PickedFileInfo> Entry : checkedFileMap.entrySet()) {
				// PickedFileInfo file = Entry.getValue();
				// System.out.println("file.mName：" + file.mName + " file.size：" + file.mSize + " file.url：" +
				// file.mUri);
				// }
			}
		});
		if (singleChoice) {
			fileupload_btn_cancel.setVisibility(View.GONE);
			fileupload_btn_ok.setVisibility(View.GONE);
		}
	}

	/**
	 * 确定上传
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: sunzhongquan
	 * @date:2014-5-15
	 */
	private void upload() {
		Intent intent = new Intent(FilePickerActivity.this, MailComposeActivity.class);
		intent.putExtra("checkedFileMap", (Serializable) checkedFileMap);
		FilePickerActivity.this.setResult(RESULT_OK, intent);
		finish();
	}

	// read init directory info from setting
	private String getInitDir() {
		String dir = "";
		try {
			SharedPreferences pref = this.getSharedPreferences(PREF_INFO, MODE_PRIVATE);
			dir = pref.getString(TAG_NAME, "");
			// 因为开始的根目录是“/sdcard/”，会出现同一文件可以加载两次的bug
			if (dir.startsWith("/sdcard/")) {
				dir = dir.replace("/sdcard/", "/mnt/sdcard/");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (dir.length() > 0)
			return dir;
		return DEF_DIR;
	}

	private boolean isRootDir() {
		return mCurDir.equals("/");
	}

	private void updateList() {
		listFiles();
		PickerAdapter a = (PickerAdapter) mListDir.getAdapter();
		if (a != null) {
			a.notifyDataSetChanged();
			mListDir.startLayoutAnimation();
			a = null;
		}
//		setTitle(mCurDir);
		mActionBarSubTitle.setText(mCurDir);
	}

	private void addHistory() {
		mDirHistory.push(mCurDir);
	}

	private void listFiles() {
		try {
			File folder = new File(mCurDir);
			PickerFilter filter = new PickerFilter();

			File[] files = folder.listFiles(filter);
			if (files == null || files.length < 1) {
				mFolderInfoList.clear();
				return;
			}
			if (files.length > 1) {
				Arrays.sort(files);
			}
			filter = null;

			/*
			 * String fmt = String.format("%s(has %d files)", mCurDir, files.length); Toast.makeText(this,
			 * fmt, Toast.LENGTH_LONG).show();
			 */

			ArrayList<Integer> dirIndex = new ArrayList<Integer>();
			ArrayList<Integer> fileIndex = new ArrayList<Integer>();
			for (int i = 0, c = files.length; i < c; i++) {
				if (files[i].isDirectory())
					dirIndex.add(i);
				else
					fileIndex.add(i);
			}
			mFolderInfoList.clear();

			for (int i : dirIndex) {
				PickedFileInfo holder = new PickedFileInfo();
				holder.mName = files[i].getName();
				holder.mIsDir = true;
				mFolderInfoList.add(holder);
			}
			for (int i : fileIndex) {
				PickedFileInfo holder = new PickedFileInfo();
				holder.mName = files[i].getName();
				holder.mIsDir = false;
				mFolderInfoList.add(holder);
			}

			files = null;
			dirIndex.clear();
			dirIndex = null;
			fileIndex.clear();
			fileIndex = null;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 返回上一级目录
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: zhangran
	 * @date:2013-5-26
	 */
	private void goUpFolder() {
		if (!isRootDir()) { // not root directory
			int i = mCurDir.lastIndexOf("/", mCurDir.length() - 2);
			if (i >= 0) {
				addHistory();
				mCurDir = mCurDir.substring(0, i + 1);
				updateList();
			}
		}
	}

	/**
	 * go back last history folder
	 * 
	 * @return true -- go back false -- empty stack or error
	 */
	private boolean goBackFolder() {
		try {
			if (!mDirHistory.isEmpty()) {
				mCurDir = mDirHistory.pop();
				updateList();
				return true;
			}
		} catch (EmptyStackException e) {
			e.printStackTrace();

		}
		return false;
	}

	/**
	 * 下一级文件
	 * 
	 * @Description:
	 * @param holder
	 * @see:
	 * @since:
	 * @author: zhangran
	 * @date:2013-5-26
	 */
	private void goSubFolder(PickedFileInfo info, PickerAdapter.ViewHolder holder) {
		if (info != null) { // enter sub-folder
			if (info.mIsDir) {// 目录
				addHistory();
				mCurDir += info.mName + "/";
				updateList();
			} else {// 文件
				// Intent data = new Intent(Intent.ACTION_VIEW);
				StringBuffer sb = new StringBuffer();
				sb.append(mCurDir);
				sb.append(info.mName);
				// Debug.d(TAG, "goSubFolder()->sb = " + sb.toString());
				// data.setData(Uri.parse("file://" + mCurDir + holder.mName));
				// Debug.d(TAG, "goSubFolder()->file = " + file);
				// data.setData(Uri.parse("file://" + sb.toString()));
				// Debug.d(TAG, "goSubFolder()->Uri = " + Uri.parse("file://" + sb.toString()));
				// sb.setLength(0);
				File file = new File(sb.toString());
				// Debug.d(TAG, "文件路径" + file.getPath());
				sb.setLength(0);
				String fileName = info.mName;
				if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".bmp") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif")) {
					file = new File(file.getPath());
				}
				long size = file.length();
				// Debug.d(TAG, "goSubFolder()->size = " + size);
				// data.setData(Uri.parse("file://" + file.getPath()));
				// data.putExtra(MessageCompose.EXTRA_ATTACHMENT_NAME, holder.mName);
				// data.putExtra(MessageCompose.EXTRA_ATTACHMENT_SIZE, size);
				// data.putExtra(ATTACHMENT_PATH, mCurDir);

				// SharedPreferences pref = this.getSharedPreferences(ATTACHMENT_DATA, MODE_PRIVATE);
				// long attachment_size = pref.getLong(ATTACHMENT_SIZE, 0);
				// size += attachment_size;

				PickedFileInfo fileholder = new PickedFileInfo();
				fileholder.mContentUri = file.getPath();
				fileholder.mSize = size;
				fileholder.mName = info.mName;
				fileholder.mContentType = FileUtil.getMIMEType(MailChat.getInstance(), fileholder.mContentUri, fileholder.mName);
				holder.mcheck.toggle();
				if (holder.mcheck.isChecked()) {
					if (size > 0) {
						if (size > GlobalConstants.MAX_ATTACHMENT_UPLOAD_SIZE) {
							Toast.makeText(MailChat.getInstance(), R.string.message_compose_attachment_size, Toast.LENGTH_LONG).show();
							holder.mcheck.setChecked(false);
							if (singleChoice) {
								return;
							}
						} else {
							checkedFileMap.put(holder.mName.getText().toString(), fileholder);
						}
					} else {
						// 文件大小为0，无法上传
						holder.mcheck.setChecked(false);
						Toast.makeText(MailChat.getInstance(), R.string.message_compose_attachment_size_zero, Toast.LENGTH_LONG).show();
						if (singleChoice) {
							return;
						}
					}
				} else {
					checkedFileMap.remove(holder.mName.getText().toString());
				}

//				if (selectedSize > GlobalConstants.MAX_ATTACHMENT_UPLOAD_SIZE) {
//					MailToast.makeText(MailChat.getInstance(), R.string.message_compose_attachment_size, Toast.LENGTH_LONG).show();
//
//					/** 保存附件大小 */
//					// size = MessageCompose2.allAttachmentSize;
//					// SharedPreferences prefSize = getSharedPreferences(ATTACHMENT_DATA, MODE_PRIVATE);
//					// SharedPreferences.Editor editorSize = prefSize.edit();
//					// editorSize.putLong(ATTACHMENT_SIZE, size);
//					// Debug.d("hanlx", "附件大小" + size);
//					// editorSize.commit();
//
//					fileupload_btn_ok.setText(FilePickerActivity.this.getResources().getString(R.string.okay_action));
//					fileupload_btn_ok.setEnabled(false);
//					if (singleChoice) {
//						return;
//					}
//				} else 
					if (size <= GlobalConstants.MAX_ATTACHMENT_UPLOAD_SIZE) {
					/** 保存文件目录 */
					SharedPreferences prefDir = getSharedPreferences(PREF_INFO, MODE_PRIVATE);
					SharedPreferences.Editor editor = prefDir.edit();
					editor.putString(TAG_NAME, mCurDir);

					editor.commit();

					/** 保存附件大小 */
					// MessageCompose2.allAttachmentSize = size;
					// SharedPreferences prefSize = getSharedPreferences(ATTACHMENT_DATA, MODE_PRIVATE);
					// SharedPreferences.Editor editorSize = prefSize.edit();
					// editorSize.putLong(ATTACHMENT_SIZE, size);
					// Debug.d("hanlx", "附件大小" + size);
					// editorSize.commit();

					// this.setResult(RESULT_OK, data);
					// finish();
					fileupload_btn_ok.setEnabled(true);
				}

				if (checkedFileMap.size() > 0) {
					// fileupload_btn_ok.setEnabled(true);
					fileupload_btn_ok.setText(FilePickerActivity.this.getResources().getString(R.string.okay_action) + "(" + checkedFileMap.size() + ")");
				} else if (checkedFileMap.size() == 0) {
					fileupload_btn_ok.setText(FilePickerActivity.this.getResources().getString(R.string.okay_action));
					fileupload_btn_ok.setEnabled(false);
				}
				if (singleChoice) {
					upload();
				}
			}

		}

	}

	private OnItemClickListener mLsnListItemClick = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
			try {
				if (isRootDir()) {// 目录
					goSubFolder(mFolderInfoList.get(pos), null);
				} else {
					if (pos == 0) {
						goUpFolder();
					} else {
						final int position = pos - 1;

						PickerAdapter.ViewHolder holder = (PickerAdapter.ViewHolder) v.getTag();
						goSubFolder(mFolderInfoList.get(position), holder);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	// List Adapter
	private class PickerAdapter extends BaseAdapter {

		private Bitmap mFolderIcon;
		private Bitmap mUpFolder;
		private Bitmap mFileIcon;
		private Bitmap mPdfIcon;
		private Bitmap mTxtIcon;
		private Bitmap mWordIcon;
		private Bitmap mExcelIcon;
		private Bitmap mPptIcon;
		private Bitmap mMusicIcon;
		private Bitmap mVideoIcon;
		private Bitmap mImageIcon;
		private Bitmap mPackageIcon;

		private LayoutInflater mInflater;
		private boolean singleChoice;

		public PickerAdapter(Context context, boolean singleChoice) {
			mInflater = LayoutInflater.from(context);
			final Resources res = context.getResources();
			mFolderIcon = BitmapFactory.decodeResource(res, R.drawable.icon_folder);
			mUpFolder = BitmapFactory.decodeResource(res, R.drawable.icon_folder_up);
			mFileIcon = BitmapFactory.decodeResource(res, R.drawable.icon_attach_blank);
			mPdfIcon = BitmapFactory.decodeResource(res, R.drawable.icon_attach_pdf);
			mTxtIcon = BitmapFactory.decodeResource(res, R.drawable.icon_attach_txt);
			mWordIcon = BitmapFactory.decodeResource(res, R.drawable.icon_attach_word);
			mExcelIcon = BitmapFactory.decodeResource(res, R.drawable.icon_attach_excel);
			mPptIcon = BitmapFactory.decodeResource(res, R.drawable.icon_attach_ppt);
			mMusicIcon = BitmapFactory.decodeResource(res, R.drawable.icon_attach_audio);
			mVideoIcon = BitmapFactory.decodeResource(res, R.drawable.icon_attach_video);
			mImageIcon = BitmapFactory.decodeResource(res, R.drawable.icon_attach_pic);
			mPackageIcon = BitmapFactory.decodeResource(res, R.drawable.icon_attach_rar);
			this.singleChoice = singleChoice;
		}

		@Override
		public int getCount() {
			int size = mFolderInfoList.size();
			if (isRootDir())
				return size;
			else
				return size + 1;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			try {
				ViewHolder holder = null;
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.item_file_picker, null);

					holder = new ViewHolder();
					holder.mIcon = (ImageView) convertView.findViewById(R.id.directorypicker_iv_icon);
					holder.mName = (TextView) convertView.findViewById(R.id.directorypicker_tv_name);
					holder.mcheck = (CheckBox) convertView.findViewById(R.id.directorypicker_ck_file);
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}

				// set item info
				if (isRootDir()) {// 上一级按钮
					holder.mcheck.setVisibility(View.GONE);
					PickedFileInfo PickedFileInfo = mFolderInfoList.get(position);
					if (PickedFileInfo != null) {
						/** modify by zhuangshn */
						if (PickedFileInfo.mIsDir) {
							holder.mIcon.setImageBitmap(mFolderIcon);
						} else {
							Bitmap fileIcon = selectFileIcon(PickedFileInfo.mName);
							holder.mIcon.setImageBitmap(fileIcon);
							// holder.mIcon.setImageBitmap(AttachmentUtil.getAttachmentIconForMessageCompose(FilePickerActivity.this,
							// PickedFileInfo.mName, PickedFileInfo.mUri));
						}
						// holder.mIcon.setImageBitmap(PickedFileInfo.mIsDir ? mFolderIcon : mFileIcon);
						holder.mName.setText(PickedFileInfo.mName);
					}
				} else {
					if (position == 0) {
						holder.mIcon.setImageBitmap(mUpFolder);
						holder.mName.setText(R.string.directorypicker_listview_up);
						holder.mcheck.setVisibility(View.GONE);
					} else {
						final int pos = position - 1;
						PickedFileInfo PickedFileInfo = mFolderInfoList.get(pos);
						if (PickedFileInfo != null) {
							if (PickedFileInfo.mIsDir) {// 是文件目录
								holder.mIcon.setImageBitmap(mFolderIcon);
								holder.mcheck.setVisibility(View.GONE);
							} else {// 是文件
								Bitmap fileIcon = selectFileIcon(PickedFileInfo.mName);
								holder.mIcon.setImageBitmap(fileIcon);
								// holder.mIcon.setImageBitmap(AttachmentUtil.getAttachmentIconForMessageCompose(FilePickerActivity.this,
								// PickedFileInfo.mName, PickedFileInfo.mUri));
								holder.mcheck.setVisibility(View.VISIBLE);
								holder.mcheck.setChecked(checkedFileMap.containsKey(PickedFileInfo.mName));
								if (holder.mcheck.isChecked()) {
									// convertView.setBackgroundResource(R.color.item_select_color);
								}
							}
							// holder.mIcon.setImageBitmap(PickedFileInfo.mIsDir ? mFolderIcon : mFileIcon);
							holder.mName.setText(PickedFileInfo.mName);
						}
					}
				}
				if (singleChoice) {
					holder.mcheck.setVisibility(View.GONE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return convertView;
		}

		/** add by zhuangshn */
		public Bitmap selectFileIcon(String fileName) {
			fileName = fileName.toLowerCase();
			if (fileName.endsWith(".pdf")) {
				return mPdfIcon;
			} else if (fileName.endsWith(".txt")) {
				return mTxtIcon;
			} else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
				return mWordIcon;
			} else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
				return mExcelIcon;
			} else if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
				return mPptIcon;
			} else if (fileName.endsWith(".zip") || fileName.endsWith(".rar") || fileName.endsWith(".apk")) {
				return mPackageIcon;
			} else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".bmp") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif")) {
				return mImageIcon;
			} else if (fileName.endsWith(".mp3") || fileName.endsWith(".wma") || fileName.endsWith(".wav") || fileName.endsWith(".midi") || fileName.endsWith(".mid") || fileName.endsWith(".amr") || fileName.endsWith(".aif") || fileName.endsWith(".m4a") || fileName.endsWith(".xmf") || fileName.endsWith(".ogg")) {
				return mMusicIcon;
			} else if (fileName.endsWith(".avi") || fileName.endsWith(".rm") || fileName.endsWith(".mpeg") || fileName.endsWith(".mpg") || fileName.endsWith(".dat") || fileName.endsWith(".ra") || fileName.endsWith(".rmvb") || fileName.endsWith(".mov") || fileName.endsWith(".qt") || fileName.endsWith(".mp4") || fileName.endsWith(".3gp")) {
				return mVideoIcon;
			} else {
				return mFileIcon;
			}
		}

		private class ViewHolder {

			CheckBox mcheck;
			ImageView mIcon;
			TextView mName;
		}
	}

	// File Filter
	private class PickerFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String filename) {
			return true; // accept all
		}

	}
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
