package com.chendezhi.baidu.tieba.capture;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * Hello world!
 * 
 */
public class BaiduLogin {
	static final String BAIDU = "http://www.baidu.com";
	static final String GET_API = "https://passport.baidu.com/v2/api/?getapi&class=login&tpl=mn&tangram=true";
	static final String LOGIN = "https://passport.baidu.com/v2/api/?login";

	public static void main(String[] args) throws URISyntaxException,
			ClientProtocolException, IOException {

		String cookiesString = "";

		HttpClient client = new DefaultHttpClient();
		// 1.
		HttpGet get = new HttpGet(BAIDU);
		HttpResponse response = client.execute(get);
		for (Header header : response.getHeaders("Set-Cookie")) {
			cookiesString += header.getValue() + ";";
		}
		response.getEntity().getContent().close();// !!!
		// 2.
		get = new HttpGet(GET_API);
		response = client.execute(get);
		HttpEntity entity = response.getEntity();
		String tokenString = new String(EntityUtils.toString(entity).getBytes(
				"iso-8859-1"), "UTF-8");
		System.out.println(tokenString);
		Pattern tokenPattern = Pattern
				.compile("bdPass\\.api\\.params\\.login_token='(?<tokenVal>\\w+)';");
		Matcher tokenMatcher = tokenPattern.matcher(tokenString);
		tokenMatcher.find();
		String token = tokenMatcher.group("tokenVal");
		System.out.println("token : " + token);
		response.getEntity().getContent().close();
		// 3.
		HttpPost post = new HttpPost(LOGIN);

		post.setEntity(new UrlEncodedFormEntity(buildLoginPostData(token, "",
				""), HTTP.UTF_8));
		// post.addHeader("Accept-Encoding", "gzip,deflate,sdch");
		post.addHeader("Content-Type", "application/x-www-form-urlencoded");
		post.addHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36");
		post.addHeader("Cookie", cookiesString);
		response = client.execute(post);
		for (Header header : response.getAllHeaders()) {
			System.out.println(header);
		}

		entity = response.getEntity();
		String source = new String(EntityUtils.toString(entity).getBytes(
				"iso-8859-1"), "UTF-8");
		// System.out.println(EntityUtils.toString(entity));
		System.out.println(source);
	}

	static List<NameValuePair> buildLoginPostData(String token,
			String codeString, String vCode) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("staticpage",
				"https://passport.baidu.com/static/passpc-account/html/v3Jump.html"));
		params.add(new BasicNameValuePair("username", "deathwingo"));
		params.add(new BasicNameValuePair("password", "@dezhi"));
		params.add(new BasicNameValuePair("charset", "UTF-8"));
		params.add(new BasicNameValuePair("token", token));
		params.add(new BasicNameValuePair("tpl", "mn"));
		params.add(new BasicNameValuePair("subpro", ""));
		params.add(new BasicNameValuePair("apiver", "v3"));
		params.add(new BasicNameValuePair("tt", ""));
		params.add(new BasicNameValuePair("codestring", codeString));
		params.add(new BasicNameValuePair("safeflg", "0"));
		params.add(new BasicNameValuePair("u", "https://www.baidu.com/"));
		params.add(new BasicNameValuePair("isPhone", "false"));
		params.add(new BasicNameValuePair("quick_user", "0"));
		params.add(new BasicNameValuePair("logintype", "dialogLogin"));
		params.add(new BasicNameValuePair("logLoginType", "pc_loginDialog"));
		params.add(new BasicNameValuePair("loginmerge", "true"));
		params.add(new BasicNameValuePair("splogin", "rate"));
		params.add(new BasicNameValuePair("verifycode", vCode));
		params.add(new BasicNameValuePair("mem_pass", "on"));
		params.add(new BasicNameValuePair("ppui_logintime", "1634"));
		return params;
	}
}
