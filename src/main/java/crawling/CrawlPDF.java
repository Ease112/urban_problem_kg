package crawling;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class CrawlPDF {
	static ArrayList<String> visited = new ArrayList<String>();
	
	public static ArrayList<String[]> getSiteList(String keyword, int start) {
		ArrayList<String[]> results = new ArrayList<String[]>();
		String apikey = "";
		String engine_id = "";
		String google = "https://www.googleapis.com/customsearch/v1?key=" + apikey + "&cx=" + engine_id + "&q=" + keyword + "&fileType=pdf&hl=ja&start=" + start;

		try {
			URL url = new URL(google);
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("GET");
			http.setConnectTimeout(30000);
			http.connect();
			InputStream is = http.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line;
			while (null != (line = reader.readLine())) {
				sb.append(line+"\n");
			}
			String result = sb.toString();
			System.out.println(result);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(result);
			JsonNode items = rootNode.findValue("items");
			for(JsonNode item: items) {
				String site = item.findValue("link").toString();
				String title = item.findValue("title").toString();
				site = site.replaceAll("\"", "");
				title = title.replaceAll("\"", "");
				if(!visited.contains(site)) {
					results.add(new String[]{site,title});
					visited.add(site);
				}
			}
			//http.disconnect();
		} catch(Exception e) {
			e.printStackTrace();
		}

		return results;
	}
	
	public static void main(String[] args) throws Exception {
		String issue = "都市縮小";
		String problem_eng = "shrinking_cities";
		String factorOrCause = "cause";
		String crawlStorageFolder = "/Users/Shusaku/Dropbox/research/doctor/" + problem_eng + "_" + factorOrCause + "/";

		try {

			
			ArrayList<String> word_list = new ArrayList<String>();
			
			ArrayList<String[]> site_list = new ArrayList<String[]>();
			
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
					site_list = getSiteList(issue + "+" + search_word, i);
					for(String[] site : site_list) {
						try {
							if(site[0].equals("http://www.town.yamada.iwate.jp/20_fukkou/pdf/fu_vision.pdf")
									|| site[0].equals("http://www.mlit.go.jp/pri/houkoku/gaiyou/pdf/kkk76_1.pdf")
									|| site[0].equals("http://dl.ndl.go.jp/view/download/digidepo_9484231_po_077505.pdf?contentNo=1")) { continue;}
							System.out.println(site[0]);
							URL url = new URL(site[0]);
							HttpURLConnection http;
							http = (HttpURLConnection) url.openConnection();
							http.setRequestMethod("GET");
							http.connect();
							InputStream is = http.getInputStream();
							File file = new File(crawlStorageFolder, site[1] + ".pdf");
							Files.copy(is, file.toPath());
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			
//			BinaryCrawler.configure(domains, crawlStorageFolder);
			//controller.addSeed("http://www.city.nagoya.jp/ryokuseidoboku/cmsfiles/contents/0000030/30618/zenbun.pdf");
//			controller.start(BinaryCrawler.class, numberOfCrawlers);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
