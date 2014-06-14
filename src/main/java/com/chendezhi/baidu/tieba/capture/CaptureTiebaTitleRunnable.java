package com.chendezhi.baidu.tieba.capture;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chendezhi.baidu.tieba.capture.hibernate.BaiduTiebaTitle;
import com.chendezhi.baidu.tieba.capture.hibernate.HibernateSessionFactory;
import com.chendezhi.baidu.tieba.capture.hibernate.CaptureDao;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CaptureTiebaTitleRunnable implements Runnable {
	static final String TIEBA_BASE_URL = "http://tieba.baidu.com/f?kw=%s&pn=%s";
	static final Logger logger = Logger
			.getLogger(CaptureTiebaTitleRunnable.class);

	private String key = "";
	private int pn = 0;
	private boolean hasNextPage = true;
	private int pages = 0;

	public CaptureTiebaTitleRunnable(String key) {
		this.key = key;
	}

	public CaptureTiebaTitleRunnable(String key, int pages) {
		this(key);
		this.pages = pages;
	}

	public void run() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("kw", key));
		params.add(new BasicNameValuePair("pn", pn + ""));
		logger.info("开始初始化" + key + "吧标题.");
		HttpClient client = new DefaultHttpClient();
		int p = 0;
		while ((pages == 0) ? hasNextPage : (p <= pages)) {
			pn = p * 50;
			HttpGet get = new HttpGet(String.format(TIEBA_BASE_URL,
					URLEncoder.encode(key), pn + ""));
			System.out.println(key
					+ "吧正在抓取标题... "
					+ String.format(TIEBA_BASE_URL, URLEncoder.encode(key), pn
							+ ""));
			p++;
			HttpResponse response = null;
			String htmlString = "";
			try {
				response = client.execute(get);
				HttpEntity entity = response.getEntity();
				htmlString = EntityUtils.toString(entity,
						Charset.forName("gbk"));
				// System.out.println(htmlString);
				response.getEntity().getContent().close();// !!!
			} catch (ClientProtocolException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			}

			if (!StringUtil.isBlank(htmlString)) {
				Document html = Jsoup.parse(htmlString);
				if (html.select("div.threadlist_title a").size() == 0) {
					// 贴吧被封
					System.out.println("尼玛," + key + "吧被封了.");
					break;
				}

				hasNextPage = html.select("a.next").size() > 0;
				Elements elements = html.select("li[data-field]");
				for (Element element : elements) {
					String data = element.attr("data-field");
					String titleString = element
							.select("div.threadlist_title a").first()
							.attr("title");
					// System.out.println(data);
					JsonObject json = new JsonParser().parse(data)
							.getAsJsonObject();

					BaiduTiebaTitle title = new BaiduTiebaTitle();
					title.setTiebaName(key);
					title.setTitle(titleString);
					title.setAuthorName(json.get("author_name").isJsonNull() ? ""
							: json.get("author_name").getAsString());
					title.setId(json.get("id").getAsLong());
					title.setReplyNum(json.get("reply_num").getAsLong());
					title.setIsGood(json.get("is_good").getAsInt());
					title.setIsTop(json.get("is_top").getAsInt());
					title.setIsNew(true);
					new CaptureDao().save(title);
				}
			}
		}
	}
}
