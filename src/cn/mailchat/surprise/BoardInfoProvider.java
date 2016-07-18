package cn.mailchat.surprise;

import java.util.List;

import android.app.Activity;

public interface BoardInfoProvider {
	
	float[] getBoardInfo(int index);
	
	int boardCount();
	
	Activity getActivity();
}
