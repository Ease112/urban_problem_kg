package crawling;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WikipediaScraping {

	public static byte[] convertIStoByte(InputStream is) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		buffer.flush();

		return buffer.toByteArray();
	}
	
	public static String[] mojibakeSiteText(String site) {
		String[] title_contents = new String[2];
		try {
			URL url = new URL(site);
			HttpURLConnection http;
			http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("GET");
			http.setConnectTimeout(20000);
			http.connect();
			InputStream is = http.getInputStream();
			//encoding analyzerのためにbyteに変換
			byte[] bytes = convertIStoByte(is);
			String encode = EncodingAnalyzer.analyze(bytes);
			BufferedReader reader = null;
			System.out.println(encode);
			//isをbyteに変換するともともとのisが使えなくなるため、byteからisに再変換
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			if(encode.equals("UTF8")) {
				reader = new BufferedReader(new InputStreamReader(bis, "UTF-8"));
			} else if(encode.equals("SJIS")) {
				reader = new BufferedReader(new InputStreamReader(bis, "Shift_JIS"));
			} else if(encode.equals("EUC_JP")) {
				reader = new BufferedReader(new InputStreamReader(bis, "EUC_JP"));
			} else if(encode.equals("ISO2022JP")) {
				reader = new BufferedReader(new InputStreamReader(bis, "ISO-2022-JP"));
			} else {
				reader = new BufferedReader(new InputStreamReader(bis, "US-ASCII"));
			}
			StringBuilder sb = new StringBuilder();
			String line;
			while (null != (line = reader.readLine())) {
				sb.append(line+"\n");
			}
			Document document = Jsoup.parse(sb.toString());
			Elements elements = document.body().getAllElements();
			StringBuilder builder = new StringBuilder();
			for (Element element : elements) {
				if (element.ownText() == null) {
					continue;
				}
				builder.append(element.ownText()).append("\n");
			}
			title_contents[0] = document.title();
			System.out.println(title_contents[0]);
			title_contents[1] = builder.toString();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return title_contents;
	}
	
	public static String[] getSiteText(String site) {
		String[] title_contents = new String[2];
		site = site.replaceAll("\"", "");
		try {
			Document document = Jsoup.connect(site).timeout(0).get();
			Elements elements = document.body().getAllElements();
			StringBuilder builder = new StringBuilder();
			for (Element element : elements) {
				if (element.ownText() == null) {
					continue;
				}
				builder.append(element.html()).append("\n");
			}
			title_contents[0] = document.title();
			System.out.println(title_contents[0]);
			title_contents[1] = builder.toString();
			System.out.println(title_contents[1]);
			if(title_contents[0].contains("�")) {
				System.out.println("文字化け対策実行");
				title_contents = mojibakeSiteText(site);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return title_contents;
	}
	
	
	public static void store(String title, String content, String url, String index, String type) {
		String endpoint = "http://localhost:9200";
		try {
			TransportClient client = new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
			IndexResponse response = client.prepareIndex(index, type)
					.setSource(jsonBuilder()
							.startObject()
							.field("title", title)
							.field("postDate", new Date())
							.field("content", content)
							.field("url", url)
							.endObject()
							)
					.get();
			
			RestStatus status = response.status();
			System.out.println(status.getStatus());
			client.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static void index(String site, String index, String type, ArrayList<String> title_list) {
		try {
			String[] title_content = getSiteText(site);
			if(title_content[1].length() > 0 && !title_list.contains(title_content[0])) {
				store(title_content[0], title_content[1], site, index, type);
				System.out.println("stored");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			String index = "social";
			String type = "dead_mall_factor2";
			String problem = "シャッター通り";
			ArrayList<String> title_list = new ArrayList<String>();
			index("https://ja.wikipedia.org/wiki/" + problem,index,type,title_list);
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

}
