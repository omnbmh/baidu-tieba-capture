package com.chendezhi.baidu.tieba.capture.entity;

import java.util.ArrayList;
import java.util.List;

public class TiebaConfig {
	private static List<String> keys = new ArrayList<String>();
	private static int quartz_page = 0;

	public static List<String> getKeys() {
		return keys;
	}

	public static void setKeys(List<String> keys) {
		TiebaConfig.keys = keys;
	}

	public static int getQuartz_page() {
		return quartz_page;
	}

	public static void setQuartz_page(int quartz_page) {
		TiebaConfig.quartz_page = quartz_page;
	}

}
