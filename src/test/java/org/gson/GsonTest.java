package org.gson;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GsonTest {
	public static void main(String[] args) {
		String data = "{\"author_name\":null,\"id\":2825396285,\"reply_num\":1,\"is_bakan\":0,\"vid\":\"\",\"is_good\":0,\"is_top\":0,\"is_protal\":0,\"fc_58\":null,\"sc_58\":null,\"fc_nuomi\":null,\"sc_nuomi\":null}";
		JsonObject json = new JsonParser().parse(data).getAsJsonObject();
		if (json.get("author_name").isJsonNull()) {

		} else {
			System.out.println(json.get("author_name").getAsString());
		}
	}
}
