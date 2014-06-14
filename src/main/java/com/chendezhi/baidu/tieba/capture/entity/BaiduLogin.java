package com.chendezhi.baidu.tieba.capture.entity;

public class BaiduLogin {
	private static String bdid;
	private static String bduss;

	public static String getBdid() {
		return bdid;
	}

	public static void setBdid(String bdid) {
		BaiduLogin.bdid = bdid;
	}

	public static String getBduss() {
		return bduss;
	}

	public static void setBduss(String bduss) {
		BaiduLogin.bduss = bduss;
	}
}
