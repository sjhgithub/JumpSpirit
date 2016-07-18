package com.c35.mtd.pushmail;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.c35.mtd.pushmail.util.C35AppServiceUtil;

/**
 * mail自用线程池
 * 
 * @Description:
 * @param args
 * @see:
 * @since:
 * @author: cuiwei
 * @date:2013-7-26
 */
public class C35MailThreadPool {

	public enum ENUM_Thread_Level {
		TL_common, TL_AtOnce
	}

	// public enum ENUM_Thread_Level {
	// TL_common, TL_AccountSelf, TL_AtOnce,TL_BackGround
	// }
	private final static int I_COMMON_POOL_NUM = 2;
	private final static int I_ACCOUNT_POOL_NUM = 1;
	// 账户无关线程，如流量统计，装机量等
	private static ThreadPoolExecutor poolInstance_common;
	// 账户相关线程，切换账户时清空
	// private static ThreadPoolExecutor poolInstance_AccountSelf = (ThreadPoolExecutor)
	// Executors.newFixedThreadPool(I_ACCOUNT_POOL_NUM);
	// 可随时清空的线程池，UI线程用
	private static ThreadPoolExecutor poolInstance_AtOnce = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

	// 账户无关线程，如流量统计，装机量等
	// private static ThreadPoolExecutor poolInstance_BackGround = (ThreadPoolExecutor)
	// Executors.newFixedThreadPool(1);
	private C35MailThreadPool() {

	}

	/**
	 * 调用方法
	 * 
	 * @Description:
	 * @param e_t_l
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-7-26
	 */
	public static ThreadPoolExecutor getInstance(ENUM_Thread_Level e_t_l) {
		switch (e_t_l) {
		case TL_common:// 账户无关线程
			return getInst_CommonPool();
			// case TL_AccountSelf:// 账户相关线程
			// return getInst_AccountPool();
		case TL_AtOnce:// 可随时清空的线程
			return getInst_AtOncePool();
			// case TL_BackGround:// 可随时清空的线程
			// return getInst_BackGroundPool();
		default:
			return getInst_CommonPool();
		}

	}

	/**
	 * 账户无关线程pool
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-7-26
	 */
	private static ThreadPoolExecutor getInst_CommonPool() {
		if (poolInstance_common == null || poolInstance_common.isShutdown()) {
			poolInstance_common = (ThreadPoolExecutor) Executors.newFixedThreadPool(I_COMMON_POOL_NUM);
			poolInstance_common.setKeepAliveTime(10, TimeUnit.SECONDS);
			poolInstance_common.allowCoreThreadTimeOut(true);
		}
		poolInstance_common.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		return poolInstance_common;
	}

	// /**
	// * BackGround线程池
	// *
	// * @Description:
	// * @return
	// * @see:
	// * @since:
	// * @author: cuiwei
	// * @date:2013-7-26
	// */
	// private static ThreadPoolExecutor getInst_BackGroundPool() {
	// if (poolInstance_BackGround == null || poolInstance_BackGround.isShutdown())
	// poolInstance_BackGround = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
	// return poolInstance_BackGround;
	// }
	// /**
	// * 账户相关线程pool
	// *
	// * @Description:
	// * @return
	// * @see:
	// * @since:
	// * @author: cuiwei
	// * @date:2013-7-26
	// */
	// private static ThreadPoolExecutor getInst_AccountPool() {
	// if (poolInstance_AccountSelf == null || poolInstance_AccountSelf.isShutdown())
	// poolInstance_AccountSelf = (ThreadPoolExecutor) Executors.newFixedThreadPool(I_ACCOUNT_POOL_NUM);
	// return poolInstance_AccountSelf;
	// }

	/**
	 * 可随时清空的线程池，UI线程用
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-7-26
	 */
	private static ThreadPoolExecutor getInst_AtOncePool() {
		try {
			if (poolInstance_AtOnce == null || poolInstance_AtOnce.isShutdown()) {
				poolInstance_AtOnce = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
				// } else if (poolInstance_AtOnce.getQueue().size() > 0) {//
			} else if (poolInstance_AtOnce != null) {//
				poolInstance_AtOnce.shutdownNow();
				poolInstance_AtOnce = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
			} else {
				poolInstance_AtOnce = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
			}
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
			C35AppServiceUtil.writeSubscribeInformationToSdcard("getInst_AtOncePool:" + e.getMessage());// 彩蛋log写入
		}
		poolInstance_AtOnce.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		return poolInstance_AtOnce;
	}

}
