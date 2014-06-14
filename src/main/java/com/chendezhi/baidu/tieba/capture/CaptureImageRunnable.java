package com.chendezhi.baidu.tieba.capture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.UUID;

public class CaptureImageRunnable implements Runnable {
	String url;

	public CaptureImageRunnable(String url) {
		this.url = url;
	}

	public void run() {
		try {

			URL url = new URL(this.url);
			File outFile = new File("E:/baidu-tieba/" + UUID.randomUUID()
					+ ".jpg");
			OutputStream os = new FileOutputStream(outFile);
			InputStream is = url.openStream();
			byte[] buff = new byte[1024];
			while (true) {
				int readed = is.read(buff);
				if (readed == -1) {
					break;
				}
				byte[] temp = new byte[readed];
				System.arraycopy(buff, 0, temp, 0, readed);
				os.write(temp);
			}
			is.close();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
