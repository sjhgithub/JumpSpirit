package cn.mailchat.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * 
 * @Description: 简单封装的Adapter 提供对数据的 删除 添加功能
 * @author:李光辉 (ligh@35.cn)
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-3-5
 */
public class FCMenuAdapter<T> extends BaseAdapter {

	// 数据
	private List<T> mDatas = new ArrayList<T>();

	// 数据变化观察者
	private Set<DataCallback<List<T>>> dataObservers = new HashSet<DataCallback<List<T>>>();

	@Override
	public int getCount() {
		return mDatas.size();
	}

	@Override
	public T getItem(int position) {

		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return null;
	}

	public Pair<Integer, Character> getLetterTagIndexByLetter(char letter) {
		return null;
	}

	public T getLastItem() {

		if (getCount() > 0) {
			return getItem(getCount() - 1);
		}

		return null;
	}

	/**
	 * 
	 * @Description:注册数据观察者
	 * @param dataObserver
	 *            观察者
	 * @see:
	 * @since:
	 * @author: liguanghui
	 * @date:2012-5-31
	 */
	public void registerDataObserver(DataCallback<List<T>> dataObserver) {
		this.dataObservers.add(dataObserver);
	}

	/**
	 * 
	 * @Description:移除数据观察者
	 * @param dataObserver
	 *            观察者
	 * @see:
	 * @since:
	 * @author: liguanghui
	 * @date:2012-5-31
	 */
	public void unRegisterDataObserver(DataCallback<List<T>> dataObserver) {
		this.dataObservers.remove(dataObserver);
	}

	/**
	 * 
	 * @Description:通知数据改变
	 * @param dataObserver
	 *            观察者
	 * @see:
	 * @since:
	 * @author: liguanghui
	 * @date:2012-5-31
	 */
	public void notifyObervers(List<T> datas) {
		for (DataCallback<List<T>> eachObserver : dataObservers) {
			eachObserver.callback(datas);
		}
	}

	/**
	 * 
	 * @Description: 设置数据
	 * @param datas
	 *            数据
	 * @see:
	 * @since:
	 * @author: liguanghui
	 * @date:2012-5-31
	 */

	public void setDatas(List<T> datas) {
		getDatas().clear();

		if (datas != null) {
			this.mDatas.addAll(datas);
		}
		notifyObervers(getDatas());
		this.notifyDataSetChanged();

	}

	/**
	 * 在已有数据基础上添加数据
	 * 
	 * @Description:
	 * @param datas
	 * @see:
	 * @since:
	 * @author: huangyongxing
	 * @date:2012-7-2
	 */
	public void addDatas(List<T> datas) {
		if (datas != null) {
			this.mDatas.addAll(datas);
			notifyObervers(getDatas());
			this.notifyDataSetChanged();
		}
	}

	/**
	 * 
	 * @Description: 移除数据
	 * @param datas
	 * @see:
	 * @since:
	 * @author: liguangui
	 * @date:2012-5-31
	 */
	public void removeDatas(Collection<T> datas) {
		this.mDatas.removeAll(datas);
		notifyObervers(getDatas());
		this.notifyDataSetChanged();
	}

	/**
	 * 清除所有数据
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: huangyongxing
	 * @date:2012-7-2
	 */
	public void removeAll() {
		this.mDatas.clear();
		notifyObervers(getDatas());
		this.notifyDataSetChanged();
	}

	/**
	 * 
	 * @Description: 添加数据
	 * @param data
	 *            数据
	 * @see:
	 * @since:
	 * @author: liguanghui
	 * @date:2012-5-31
	 */
	public void addData(T data) {
		if (data == null) {
			return;
		}
		mDatas.add(data);
		notifyObervers(getDatas());
		this.notifyDataSetChanged();
	}

	public void removeData(T data) {
		if (data == null) {
			return;
		}
		mDatas.remove(data);
		notifyObervers(getDatas());
		this.notifyDataSetChanged();
	}

	public void replace(T c) {

		int pos = mDatas.indexOf(c);

		if (pos != -1) {
			mDatas.set(pos, c);
			notifyObervers(getDatas());
			this.notifyDataSetChanged();
		}

	}

	/**
	 * @Description: 清除所有数据
	 * @see:
	 * @since:
	 * @author: liguanghui
	 * @date:2012-5-31
	 */
	public void clear() {
		getDatas().clear();
		notifyObervers(getDatas());
		this.notifyDataSetChanged();

	}

	/**
	 * 
	 * @Description:数据是否为空
	 * @see:
	 * @since:
	 * @author: liguanghui
	 * @date:2012-5-31
	 */
	@Override
	public boolean isEmpty() {
		return this.mDatas.isEmpty();
	}

	/**
	 * 
	 * @Description: 获取所有数据
	 * @see:
	 * @since:
	 * @author: liguanghui
	 * @date:2012-5-31
	 */
	public List<T> getDatas() {
		return mDatas;
	}

	/**
	 * 
	 * @Description: 数据回调
	 * @author: liguanghui
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2012-5-30
	 */
	public interface DataCallback<T> {

		public void callback(T data);
	}
}
