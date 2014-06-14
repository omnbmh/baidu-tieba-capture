package com.chendezhi.baidu.tieba.capture;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chendezhi.baidu.tieba.capture.hibernate.BaiduTiebaContent;
import com.chendezhi.baidu.tieba.capture.hibernate.BaiduTiebaContentId;
import com.chendezhi.baidu.tieba.capture.hibernate.BaiduTiebaContentTask;
import com.chendezhi.baidu.tieba.capture.hibernate.BaiduTiebaTitle;
import com.chendezhi.baidu.tieba.capture.hibernate.CaptureDao;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CaptureTiebaContentRunnable implements Runnable {
	static Logger logger = Logger.getLogger(CaptureImageRunnable.class);
	final String TIEBA_URL = "http://tieba.baidu.com/p/%s?see_lz=1&pn=%s";
	List<BaiduTiebaTitle> titles;

	public CaptureTiebaContentRunnable(List<BaiduTiebaTitle> titles) {
		this.titles = titles;
	}

	public void run() {
		for (BaiduTiebaTitle title : titles) {
			logger.info("开始初始化" + title.getTiebaName() + "吧标题.");
			long id = title.getId();
			int pn = 0;// 要抓取的起始页码
			long floorNum = 0;
			BaiduTiebaContentTask task = new CaptureDao().hasInitContent(id);
			if (null != task) {
				pn = task.getPage();
				floorNum = task.getFloor();
			}
			int pages = getPages(id);
			while (pn <= pages) {
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(String.format(TIEBA_URL, id + "", pn
						+ ""));
				logger.info("开始抓取" + title.getTiebaName() + "吧的内容..."
						+ String.format(TIEBA_URL, id + "", pn + ""));
				pn++;
				HttpResponse response;
				try {
					response = client.execute(get);
					HttpEntity entity = response.getEntity();
					// String htmlString = new
					// String(EntityUtils.toString(entity)
					// .getBytes("iso-8859-1"), "UTF-8");
					String htmlString = EntityUtils.toString(entity,
							Charset.forName("gbk"));
					response.getEntity().getContent().close();// !!!
					// System.out.println(htmlString);
					if (!StringUtil.isBlank(htmlString)) {
						Document html = Jsoup.parse(htmlString);
						// 获取楼层
						Elements elements = html.select("div.l_post");
						for (Element element : elements) {
							JsonObject floorJson = new JsonParser().parse(
									element.attr("data-field"))
									.getAsJsonObject();
							floorNum = floorJson.get("content")
									.getAsJsonObject().get("post_no")
									.getAsLong();
							// Long floorNum = Long
							// .parseLong(Pattern
							// .compile("[^0-9]")
							// .matcher(
							// element.select(
							// "div.core_reply ul.p_tail li span")
							// .get(0).text())
							// .replaceAll(""));
							Elements floors = element.select("img.BDE_Image");
							for (int p = 0, q = floors.size(); p < q; p++) {
								Element floor = floors.get(p);
								String src = floor.attr("src");
								if (src.contains("http://imgsrc.baidu.com/forum/")) {
									logger.info("命中图片 "
											+ String.format("%s %s %s ", id
													+ "", floorNum + "", p + ""));
									System.out.println(src);
									BaiduTiebaContentId contentId = new BaiduTiebaContentId();
									contentId.setTitleId(id);
									contentId.setFloor(floorNum);
									contentId.setImgIdx(p);
									BaiduTiebaContent content = new BaiduTiebaContent();
									content.setId(contentId);
									content.setImgUrl(src);
									content.setIsDownload(false);
									new CaptureDao().saveContent(content);
								}
							}
						}
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			task = new BaiduTiebaContentTask();
			task.setTiebaId(id);
			task.setPage(pages);
			task.setFloor(floorNum);
			new CaptureDao().saveTask(task);
			title.setIsNew(false);
			new CaptureDao().save(title);
		}
	}

	int getPages(long id) {
		int pages = 0;
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(String.format(TIEBA_URL, id + "", "1"));
		HttpResponse response;
		try {
			response = client.execute(get);
			HttpEntity entity = response.getEntity();
			String htmlString = EntityUtils.toString(entity,
					Charset.forName("gbk"));
			response.getEntity().getContent().close();// !!!
			// System.out.println(htmlString);
			if (!StringUtil.isBlank(htmlString)) {
				Document html = Jsoup.parse(htmlString);
				if (html.select("li.l_reply_num span").size() != 0) {
					// 帖子没有被删除
					pages = Integer.parseInt(html.select("li.l_reply_num span")
							.get(1).text());
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("帖子 id :" + id + ", 共" + pages + "页.");
		return pages;
	}
}
