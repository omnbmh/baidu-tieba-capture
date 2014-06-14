package com.chendezhi.baidu.tieba.capture;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.impl.StdSchedulerFactory;

import com.chendezhi.baidu.tieba.capture.entity.TiebaConfig;
import com.chendezhi.baidu.tieba.capture.hibernate.BaiduTiebaTitle;
import com.chendezhi.baidu.tieba.capture.hibernate.CaptureDao;
import com.chendezhi.baidu.tieba.capture.hibernate.HibernateSessionFactory;

public class App {
	static Logger logger = Logger.getLogger(App.class);

	static final String BAIDU_TIEZI = "http://www.baidu.com/p/";
	static final String GET_API = "https://passport.baidu.com/v2/api/?getapi&class=login&tpl=mn&tangram=true";
	static final String LOGIN = "https://passport.baidu.com/v2/api/?login";

	public static void main(String[] args) throws URISyntaxException,
			ClientProtocolException, IOException {
		PropertyConfigurator.configure(new App().getClass().getClassLoader()
				.getResourceAsStream("log4j.properties"));
		HibernateSessionFactory.initSessionFactory("hibernate.cfg.xml");
		Properties properties = new Properties();
		properties.load(new App().getClass().getClassLoader()
				.getResourceAsStream("tieba.properties"));
		TiebaConfig.setKeys(Arrays.asList(properties.getProperty("keys").split(
				",")));
		TiebaConfig.setQuartz_page(Integer.parseInt(properties.get(
				"quartz.page").toString()));
		logger.info("系统配置加载完毕.");
		// 贴吧初始化
		// 初始化帖子标题
		if (TiebaConfig.getKeys().size() > 0) {
			ExecutorService service = Executors.newCachedThreadPool();
			for (String key : TiebaConfig.getKeys()) {
				if (null == new CaptureDao().hasInitTitle(key)) {
					service.execute(new CaptureTiebaTitleRunnable(key));
				}
			}
			service.shutdown();
		}
		// 初始化定时器并启动
		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler sched = null;
		try {
			sched = sf.getScheduler();
		} catch (Exception ex) {
			logger.error("获取调度程序失败", ex);
			return;
		}

		logger.info("完成任务调度初始化");
		logger.info("开始启动任务调度");

		try {
			sched.start();
		} catch (Exception ex) {
			logger.error("启动任务调度失败", ex);
			return;
		}
		logger.info("任务调度已正常启动");
		while (true) {
			SchedulerMetaData metaData;
			try {
				metaData = sched.getMetaData();
				logger.info("当前共执行了" + metaData.getNumberOfJobsExecuted()
						+ "个任务,运行状态:" + sched.isStarted());
			} catch (Exception ex) {
				logger.error("获取定时任务统计信息失败", ex);
			}
			try {
				Thread.sleep(60 * 1000L);
			} catch (Exception ex) {
				logger.error("将主线程进入睡眠状态时失败", ex);
			}
		}

	}
}
