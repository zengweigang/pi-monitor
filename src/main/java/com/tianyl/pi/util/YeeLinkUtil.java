package com.tianyl.pi.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class YeeLinkUtil {

	public static String post(String url, Map<String, String> paramMap) {
		return post(url, paramMap, "utf-8");
	}

	public static String post(String url, Map<String, String> paramMap, String charsetName) {
		String result = "";
		if (paramMap == null) {
			paramMap = new HashMap<String, String>();
		}
		try {
			HttpURLConnection conn = (HttpURLConnection) (new URL(checkUrl(url)).openConnection());
			if (!paramMap.isEmpty()) {
				conn.setDoInput(true);
			}
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			// 仅对当前请求自动重定向
			conn.setInstanceFollowRedirects(true);
			// header 设置编码
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			// 连接
			conn.connect();
			writeParameters(conn, paramMap);
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException();
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), charsetName));
			String temp = null;
			while ((temp = reader.readLine()) != null) {
				result += temp + "\n";
			}
			reader.close();
			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static RequestResult post(String url, String body) {
		RequestResult rr = new RequestResult();
		try {
			HttpURLConnection conn = (HttpURLConnection) (new URL(checkUrl(url)).openConnection());
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			// 仅对当前请求自动重定向
			conn.setInstanceFollowRedirects(true);
			// header 设置编码
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("U-ApiKey", getKey());
			// 连接
			conn.connect();
			writeBody(conn, body);
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				rr.setOk(false);
				rr.setResultBytes(IOUtils.toByteArray(conn.getErrorStream()));
			} else {
				byte[] bs = IOUtils.toByteArray(conn.getInputStream());
				rr.setOk(true);
				rr.setResultBytes(bs);
			}
			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			rr.setOk(false);
		}
		return rr;
	}

	public static String get(String url) {
		String result = "";
		try {
			HttpURLConnection conn = (HttpURLConnection) (new URL(checkUrl(url)).openConnection());
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setUseCaches(false);
			// 仅对当前请求自动重定向
			conn.setInstanceFollowRedirects(true);
			// header 设置编码
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("U-ApiKey", getKey());
			// 连接
			conn.connect();
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				String str = readStream(conn.getErrorStream());
				System.out.println(conn.getResponseCode() + ":" + str);
				throw new IOException();
			}
			result = readStream(conn.getInputStream());
			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private static String getKey() {
		String key = FileUtil.read(new File("/home/pi/pidata/yeelink.key"));
		if (StringUtil.isBlank(key)) {
			throw new RuntimeException("获取 U-ApiKey错误");
		}
		return key;
	}

	private static String readStream(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int readLen = -1;
		while ((readLen = is.read(buff)) != -1) {
			baos.write(buff, 0, readLen);
		}
		return new String(baos.toByteArray());
	}

	private static void writeBody(HttpURLConnection conn, String body) throws IOException {
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		out.writeBytes(body);
		out.flush();
		out.close();
	}

	private static void writeParameters(HttpURLConnection conn, Map<String, String> map) throws IOException {
		if (map == null) {
			return;
		}
		String content = "";
		Set<String> keySet = map.keySet();
		int i = 0;
		for (String key : keySet) {
			String val = map.get(key);
			content += (i == 0 ? "" : "&") + key + "=" + URLEncoder.encode(val, "utf-8");
			i++;
		}
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		out.writeBytes(content);
		out.flush();
		out.close();
	}

	private static String checkUrl(String url) {
		String result = url;
		if (url.startsWith("http://")) {
			url = url.replaceFirst("http://", "");
			if (url.contains("//")) {
				url = url.replaceAll("//", "/");
			}
			result = "http://" + url;
		}
		return result;
	}

}
