package com.chendezhi.baidu.tieba.capture;

import java.io.File;

import org.apache.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.simpl.CascadingClassLoadHelper;
import org.quartz.simpl.InitThreadContextClassLoadHelper;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;
import org.quartz.xml.XMLSchedulingDataProcessor;

/**
 * @ClassName: LoadJobPlugin.java
 * @Package cn.coweibo.scheduled
 * @Description: 加载Job插件
 * @author GuoJie guojie@coweibo.cn
 * @date 2012-7-25 下午10:45:59
 * @version V1.0
 */
public class CaptureJobsLoder implements SchedulerPlugin {

	private static Logger logger = Logger.getLogger(CaptureJobsLoder.class);

	private String jobsConfDirectory;
	private Scheduler scheduler;
	private String pluginName;
	private File[] jobConfigFiles;

	/** @return jobsConfDirectory */
	public String getJobsConfDirectory() {
		return jobsConfDirectory;
	}

	/**
	 * @param jobsConfDirectory
	 *            要设置的 jobsConfDirectory
	 */
	public void setJobsConfDirectory(String jobsConfDirectory) {
		this.jobsConfDirectory = jobsConfDirectory;
	}

	public File[] getJobConfigFiles() {
		return jobConfigFiles;
	}

	/*
	 * (non-Javadoc) <p>Title: initialize</p> <p>Description: </p>
	 * 
	 * @param name
	 * 
	 * @param scheduler
	 * 
	 * @throws SchedulerException
	 * 
	 * @see org.quartz.spi.SchedulerPlugin#initialize(java.lang.String,
	 * org.quartz.Scheduler)
	 */
	public void initialize(String name, Scheduler scheduler)
			throws SchedulerException {
		this.pluginName = name;
		this.scheduler = scheduler;
		logger.info("启动插件:" + pluginName);
		loadJobs();
	}

	/*
	 * (non-Javadoc) <p>Title: start</p> <p>Description: </p>
	 * 
	 * @see org.quartz.spi.SchedulerPlugin#start()
	 */
	public void start() {
		processJobs();
	}

	/*
	 * (non-Javadoc) <p>Title: shutdown</p> <p>Description: </p>
	 * 
	 * @see org.quartz.spi.SchedulerPlugin#shutdown()
	 */
	public void shutdown() {
		try {
			this.scheduler.shutdown();
		} catch (SchedulerException e) {
			logger.error("调度退出异常。", e);
		}
	}

	private void loadJobs() throws SchedulerException {
		String path = this.getClass().getClassLoader()
				.getResource(getJobsConfDirectory()).getPath();
		// String path = getJobsConfDirectory();
		System.out.println(path);
		File dir = new File(path);
		if (getJobsConfDirectory() == null || !dir.exists()) {
			throw new SchedulerConfigException("未找到任务配置目录：" + jobsConfDirectory);
		}
		// new XMLFileOnlyFilter()
		this.jobConfigFiles = dir.listFiles();
		logger.info("任务配置目录：" + dir.getName() + ",任务数量："
				+ this.jobConfigFiles.length);
	}

	public void processJobs() {
		if (getJobConfigFiles() == null || getJobConfigFiles().length == 0) {
			return;
		}
		String fileName = null;
		try {
			ClassLoadHelper clhelper = new CascadingClassLoadHelper();
			clhelper.initialize();
			XMLSchedulingDataProcessor processor = new XMLSchedulingDataProcessor(
					clhelper);
			int size = getJobConfigFiles().length;
			for (int i = 0; i < size; i++) {
				File jobFile = getJobConfigFiles()[i];
				fileName = jobFile.getAbsolutePath();
				processor.processFileAndScheduleJobs(fileName, scheduler);
				logger.info("加载任务配置文件成功: " + fileName);
			}
		} catch (Exception ex) {
			logger.error("加载任务配置文件失败: " + fileName, ex);
		}

	}

}
