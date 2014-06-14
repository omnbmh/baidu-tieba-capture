package com.chendezhi.baidu.tieba.capture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.chendezhi.baidu.tieba.capture.entity.TiebaConfig;

public class CaptureTiebaTitleJob implements Job {
	static final String TIEBA_URL = "http://tieba.baidu.com/p/%s?see_lz=1&pn=%s";

	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		if (TiebaConfig.getKeys().size() > 0) {
			ExecutorService service = Executors.newCachedThreadPool();
			for (String key : TiebaConfig.getKeys()) {
				service.execute(new CaptureTiebaTitleRunnable(key, TiebaConfig
						.getQuartz_page()));
			}
			service.shutdown();
		}
	}
}
