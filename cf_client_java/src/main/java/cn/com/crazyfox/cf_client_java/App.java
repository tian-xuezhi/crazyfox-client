package cn.com.crazyfox.cf_client_java;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 本程序是用于获取Crazyfox.com.cn网站数据的客户端代码。
 *
 */
public class App {
	
	public static Properties props = new Properties();
	
	public static void main(String[] args) {

		// 留下来用于测试java运行环境是否正常
		System.out.println("Hello World!");

		//读取properties配置文件
		getProperties();
		String token = props.getProperty("token");
		
		// 从服务器获取的交易对象，包括股票、期货等。（当前示例用户只能获取20行，真实用户不受限制。）
		String outputStr = getAllTargetFromCF(token);
		try {

			// 解析返回码code,返回码对应的消息msg，以及数据data
			JSONObject jsonObject = new JSONObject(outputStr);
			int code = jsonObject.getInt("code");
			String msg = jsonObject.getString("msg");
			JSONArray dataArray = jsonObject.getJSONArray("data");

			System.out.println("code:" + code);
			System.out.println("msg:" + msg);

			// 逐条解析数据data。具体参数的意义见网站说明。
			for (int i = 0; i < dataArray.length(); i++) {
				JSONObject dataObject = dataArray.getJSONObject(i);
				String targetId = dataObject.getString("targetid");
				int recordId = dataObject.getInt("recordid");
				String targetName = dataObject.getString("targetname");
				String headDate = dataObject.isNull("headdate") ? null : dataObject.getString("headdate");
				String endDate = dataObject.isNull("enddate") ? null : dataObject.getString("enddate");

				// 打印每条信息的内容。
				System.out.print("Target ID: " + targetId);
				System.out.print(", Record ID: " + recordId);
				System.out.print(", Target Name: " + targetName);
				if (headDate != null)
					System.out.print(", Head Date: " + headDate);
				if (endDate != null)
					System.out.print(", End Date: " + endDate);
				System.out.println();
			}
		} catch (Exception e) {
			System.out.println("Caught Exception Type 1: " + e.getMessage());
		}
	}

	public static String getAllTargetFromCF(String token) {

		// 获取交易对象的连接如下
		String url = "https://www.crazyfox.com.cn:2087/target/all";
		InputStream in = null;
		String result = null;

		try {
			// 目前采用Https连接，保留http连接作为不时之需。
//			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();

			// （当前示例用户只能获取20行，真实用户不受限制。）
			conn.setRequestProperty("token", token);

			// 获取输入流
			in = conn.getInputStream();

			ByteArrayOutputStream bo = new ByteArrayOutputStream();

			// 将输入流缓冲到本地。
			int i;
			byte[] b = new byte[256];
			while ((i = in.read(b)) != -1)
				bo.write(b, 0, i);
			result = bo.toString();

			// 重置缓冲，关闭输入
			bo.reset();
			if (in != null)
				in.close();

			System.out.println(result);

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return result;
	}
	
	public static void getProperties(){
		
		try {
			InputStream in = new BufferedInputStream(new FileInputStream("./classes/app.properties"));
			props.load(in);
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return;
	}
}
