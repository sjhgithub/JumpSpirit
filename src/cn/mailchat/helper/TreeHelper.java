package cn.mailchat.helper;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import cn.mailchat.R;
import cn.mailchat.beans.Eis35Bean;
import cn.mailchat.contacts.beans.ContactAttribute;

public class TreeHelper {
	/**
	 * 传入我们的普通bean，转化为我们排序后的tree
	 * 
	 * @param datas
	 * @param defaultExpandLevel
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static List<ContactAttribute> getSortedEis(List<Eis35Bean> datas,
			int defaultExpandLevel) throws IllegalArgumentException,
			IllegalAccessException {
		List<ContactAttribute> result = new ArrayList<ContactAttribute>();
		// 将用户数据转化为List<Eis35TreeBeen>
		List<ContactAttribute> eis35TreeBeens = convetEis35Bean2TreeBeen(datas);
		// 拿到根节点
		List<ContactAttribute> rootEis35TreeBeens = getRootEis35TreeBeens(eis35TreeBeens);
		// 排序以及设置Eis35TreeBeen间关系
		for (ContactAttribute been : rootEis35TreeBeens) {
			addEis35TreeBeen(result, been, defaultExpandLevel, 1);
		}
		return result;
	}

	private static List<ContactAttribute> convetEis35Bean2TreeBeen(List<Eis35Bean> datas)
			throws IllegalArgumentException, IllegalAccessException {
		List<ContactAttribute> treeBeens = new ArrayList<ContactAttribute>();
		ContactAttribute tree = null;
		for (Eis35Bean eis : datas) {
			tree = new ContactAttribute(eis.getId(), eis.getParentId(), eis.getName(),
					eis.getSort(), eis.getEmail(), eis.getMailChatName(),
					eis.getImgHeadUrl(),eis.getTotalCount(), eis.isLeader(), eis.isParent(),
					eis.isUsedMailchat(),eis.isExpand(),eis.getDepartment(),eis.getPosition(),eis.getPhones(),eis.getAddr(),eis.getOtherRemarks(),
					eis.getrEmail(),eis.getrName(),eis.getrNickName(),eis.getrImgHeadHash(),eis.getrCompany(),eis.getrBirthday(),eis.getrDepartment(),
					eis.getrPosition(),eis.getrPhones(),eis.getrAddr(),eis.getrOtherRemarks(),eis.getChildDepCount(),eis.getrData());
			treeBeens.add(tree);
		}

		/**
		 * 设置TreeBeen间，父子关系;让每两个节点都比较一次，即可设置其中的关系
		 */
		for (int i = 0; i < treeBeens.size(); i++) {
			ContactAttribute t = treeBeens.get(i);
			for (int j = i + 1; j < treeBeens.size(); j++) {
				ContactAttribute m = treeBeens.get(j);
//				if (m.getpId()==null) {
//                    Log.d("qxian", "m.getpId() is null"+m.getName());
//                }
//				if (t.getId()==null) {
//                    Log.d("qxian", "m.getId() is null");
//                }
				if (m.getpId().equals(t.getId())) {
					t.getChildList().add(m);
					m.setParent(t);
				} else if (m.getId().equals(t.getpId())) {
					m.getChildList().add(t);
					t.setParent(m);
				}
			}
		}
		return treeBeens;
	}

	/**
	 * 过滤出所有可见的节点
	 * 
	 * @return
	 */
	public static List<ContactAttribute> filterVisibleTreeBeen(List<ContactAttribute> treeBeens) {
		List<ContactAttribute> result = new ArrayList<ContactAttribute>();
		for (ContactAttribute node : treeBeens) {
			// 如果为跟节点，或者上层目录为展开状态
			if (node.isRoot() || node.isParentExpand()||node.isExpand()) {
				setNodeIcon(node);
				result.add(node);
			}
		}
		return result;
	}

	/**
	 * 设置节点的图标
	 * 
	 * @param node
	 */
	private static void setNodeIcon(ContactAttribute node) {
		if (node.getChildList().size() > 0 && node.isExpand()) {
			node.setIcon(R.drawable.expander_open_holo_light);
		} else if (node.getChildList().size() > 0 && !node.isExpand()) {
			node.setIcon(R.drawable.expander_close_holo_light);
		} else
			node.setIcon(-1);

	}

	private static List<ContactAttribute> getRootEis35TreeBeens(List<ContactAttribute> treeBeens) {
		List<ContactAttribute> root = new ArrayList<ContactAttribute>();
		for (ContactAttribute bean : treeBeens) {
			if (bean.isRoot())
				root.add(bean);
		}
		return root;
	}

	/**
	 * 把一个节点上的所有的内容都挂上去
	 */
	private static void addEis35TreeBeen(List<ContactAttribute> result,
			ContactAttribute treeBeen, int defaultExpandLeval, int currentLevel) {
		result.add(treeBeen);
		if (defaultExpandLeval >= currentLevel) {
			treeBeen.setExpand(true);
		}
		if (treeBeen.isLeaf()) {
			return;
		}
		int count = treeBeen.getChildList().size();
		for (int i = 0; i < count; i++) {
			addEis35TreeBeen(result, treeBeen.getChildList().get(i),
					defaultExpandLeval, currentLevel + 1);
		}
	}
}
