package com.chendezhi.baidu.tieba.capture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.print.Doc;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.chendezhi.baidu.tieba.capture.entity.TiebaConfig;
import com.chendezhi.baidu.tieba.capture.hibernate.BaiduTiebaContent;
import com.chendezhi.baidu.tieba.capture.hibernate.BaiduTiebaContentId;
import com.chendezhi.baidu.tieba.capture.hibernate.BaiduTiebaContentTask;
import com.chendezhi.baidu.tieba.capture.hibernate.BaiduTiebaTitle;
import com.chendezhi.baidu.tieba.capture.hibernate.HibernateSessionFactory;
import com.chendezhi.baidu.tieba.capture.hibernate.CaptureDao;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CaptureTiebaContentJob implements Job {
	static final String TIEBA_URL = "http://tieba.baidu.com/p/%s?see_lz=1&pn=%s";

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		// 获取帖子列表
		for (String key : TiebaConfig.getKeys()) {
			List<BaiduTiebaTitle> list = new CaptureDao().listTitleNew(key,
					1000);

			if (list.size() <= 10) {
				ExecutorService service = Executors.newCachedThreadPool();
				service.execute(new CaptureTiebaContentRunnable(list));
				service.shutdown();
			} else {
				int perPage = list.size() / 10;
				ExecutorService service = Executors.newCachedThreadPool();
				for (int i = 1; i <= 10; i++) {
					service.execute(new CaptureTiebaContentRunnable(list
							.subList((i - 1) * perPage, i * perPage - 1)));
				}
				if (list.size() > 10 * perPage) {
					service.execute(new CaptureTiebaContentRunnable(list
							.subList(10 * perPage, list.size() - 1)));
				}
				service.shutdown();
			}
		}
	}
}
