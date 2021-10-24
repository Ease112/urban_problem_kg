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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import search.WordNet;

public class Controller {
	static ArrayList<String> visited = new ArrayList<String>();
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
	
	public static ArrayList<String> getBingSiteList(String keyword, int start) {
		ArrayList<String> results = new ArrayList<String>();
		String apiKey = "";
		
		try {
			String bing = "https://api.cognitive.microsoft.com/bing/v7.0/search?q="
					+ URLEncoder.encode(keyword,"utf-8") + "+filetype:html"
					+ "&offset=" + start;
			URL url = new URL(bing);
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("GET");
			http.setRequestProperty("Content-Type", "application/ld+json;");
			http.setRequestProperty("Ocp-Apim-Subscription-Key", apiKey);
			http.setRequestProperty("Accept-Language", "ja-JP");
			http.connect();
			InputStream is = http.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line;
			while (null != (line = reader.readLine())) {
				sb.append(line+"\n");
			}
			String result = sb.toString();
//			System.out.println(result);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(result);
			JsonNode webPages = rootNode.findValue("webPages");
			JsonNode values = webPages.findValue("value");
			for(JsonNode item: values) {
				String site = item.get("url").toString();
				//リダイレクト先URLを取得する
				//V7.0で変更?
//				String[] sitePar = site.split("&r=");
//				String redirect = sitePar[1].split("&p=DevEx")[0];
//				redirect = URLDecoder.decode(redirect,"UTF-8");
				site = site.replaceAll("\\\\", "");
				if(!visited.contains(site)) {
					results.add(site);
					visited.add(site);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return results;
	}
	
	public static ArrayList<String> getGoogleSiteList(String keyword, int start) {
		ArrayList<String> results = new ArrayList<String>();
		String apikey = "";
		String engine_id = "";
		
		try {
			String google = "https://www.googleapis.com/customsearch/v1?key=" + apikey + "&cx=" + engine_id + "&q=" + keyword + "&fileType=html&hl=ja&start=" + start;
			URL url = new URL(google);
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("GET");
			http.connect();
			InputStream is = http.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line;
			while (null != (line = reader.readLine())) {
				sb.append(line+"\n");
			}
			String result = sb.toString();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(result);
			JsonNode items = rootNode.findValue("items");
			for(JsonNode item: items) {
				String site = item.findValue("link").toString();
				if(!visited.contains(site)) {
					results.add(site);
					visited.add(site);
				}
			}
			//http.disconnect();
		} catch(Exception e) {
			e.printStackTrace();
		}

		return results;
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
			Document document = Jsoup.connect(site).timeout(30000).get();
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
			if(title_contents[0].contains("�")) {
				System.out.println("文字化け対策実行");
				title_contents = mojibakeSiteText(site);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return title_contents;
	}
	
	public static String[] getSiteContents(String site) {
		String title = "";
		String content = "";
		String[] results = new String[2];	//title,content
		try {
			site = site.replaceAll("\"", "");
			site = URLDecoder.decode(site, "UTF-8");
			String mercury = "https://mercury.postlight.com/parser?url=" + site;
			System.out.println(mercury);
			URL url = new URL(mercury);
			HttpURLConnection http;
			http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("GET");
			http.setRequestProperty("Content-Type", "application/json;");
			http.setRequestProperty("x-api-key", "");
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
			String rs = sb.toString();
			//System.out.println(rs);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(rs);
			JsonNode contentNode = rootNode.findValue("content");
			content = contentNode.toString();
			JsonNode titleNode = rootNode.findValue("title");
			title = titleNode.toString();
			System.out.println(title);
			content = NCR.ncr(content);
			content = content.replaceAll("<.+?>", "");
			//System.out.println(content);
			results[0] = title;
			results[1] = content;
			//http.disconnect();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return results;
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
	
	public static ArrayList<String> getExistances(String index, String type) {
		ArrayList<String> title_list = new ArrayList<String>();
		try {
			TransportClient client = new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
			SearchResponse response = client.prepareSearch(index)
					.setTypes(type) 
					.addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setScroll(new TimeValue(60000))
					.setQuery(QueryBuilders.matchAllQuery())                 // Query
					//.setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))     // Filter
					.setFrom(0).setSize(60).setExplain(true)
					.get();
			
			do {
				for (SearchHit hit : response.getHits().getHits()) {
					title_list.add((String)hit.getSource().get("title"));
				}
				response = client.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
			} while(response.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.
		} catch(Exception e) {
			e.printStackTrace();
		}
		return title_list;
	}
	
	private static void singleSiteIndex(String site, String index, String type, ArrayList<String> title_list) {
		try {
			String[] title_content = getSiteContents(site);
			if(title_content[1].length() > 0 && !title_list.contains(title_content[0])) {
				store(title_content[0], title_content[1], site, index, type);
				System.out.println("stored");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		try {
//			String keyword = "factor";
//			WordNet wn = new WordNet("http://www.ohsuga.lab.uec.ac.jp/sparql");
//			ArrayList<String> word_list = wn.getWordSynsetList(keyword);
			//[factor, ファクタ, エレメント, ファクター, 因子, 素因, 要因, 要素, 導因, cause, もと, 原因, 誘因, 起こり, 起り]
			String index = "social";
			String factorOrCause = "cause";
			String type = "shrinking_cities_" + factorOrCause;
			String problem = "都市縮小";
			ArrayList<String> title_list = new ArrayList<String>();
			title_list = getExistances(index, type);
			
			ArrayList<String> word_list = new ArrayList<String>();
//			
			switch(factorOrCause) {
			case "factor":
				word_list.add("原因");
				word_list.add("要因");
				word_list.add("要素");
				break;
			case "cause":
				word_list.add("影響");
				word_list.add("引き起こす");
				word_list.add("招く");
				word_list.add("もたらす");
				word_list.add("生み出す");
				word_list.add("誘発");
				break;
			default:
				System.out.println("不明なfactorOrCause");
				System.exit(0);
				break;
			}
			
			System.out.println(word_list);
			for(int i=1; i<50; i+=10) {
				for(String search_word: word_list) {
					ArrayList<String> sites = getGoogleSiteList(problem + "+" + search_word, i);
					sites.addAll(getBingSiteList(problem + " " + search_word, i-1));
					for(String site: sites) {
						//controller.addSeed(site);
						System.out.println(site);
						if(!site.equals("\"https://www.city.matsuyama.ehime.jp/iken/faq/Anser/FAQ12237.html\"")
								&& !site.equals("\"http://www.patagonia.jp/resource-use.html\"")) {
							
							//全文抽出
							String[] title_content = getSiteText(site);
							try {
								if(title_content[1].length() > 0 && !title_list.contains(title_content[0])) {
									store(title_content[0], title_content[1], site, index, type);
									System.out.println("stored");
								}
							} catch(Exception e2) {
								e2.printStackTrace();
							}
						}
					}
				}
			}
			
//			singleSiteIndex("https://ja.wikipedia.org/wiki/不就学",index,type,title_list);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
