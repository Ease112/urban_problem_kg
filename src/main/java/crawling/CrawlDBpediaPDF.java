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

public class CrawlDBpediaPDF {
	static ArrayList<String> visited = new ArrayList<String>();

	public static void main(String[] args) throws Exception {
		String crawlStorageFolder = "/Users/Shusaku/Dropbox/research/doctor/fushugaku2_factor/";

		try {
			String issue = "非就学";
			String factorOrCause = "factor";
			ArrayList<String> word_list = new ArrayList<String>();
			ArrayList<String> dbpediaRelations = CrawlDBpediaRelations.getDBpediaRelations(issue);

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
			

			ArrayList<String[]> site_list = new ArrayList<String[]>();
			for(int i=1; i<50; i+=10) {
				for(String search_word: word_list) {
					for(String dbpedia : dbpediaRelations) {
						site_list = CrawlPDF.getSiteList(issue + "+" + dbpedia + "+" + search_word, i);
						for(String[] site : site_list) {
							try {
								if(
										site[0].equals("http://www.town.yamada.iwate.jp/20_fukkou/pdf/fu_vision.pdf")
										|| site[0].equals("http://www.ana.co.jp/group/ari/pdf/publishing/takeoff/tw124_04f2.pdf")
										|| site[0].equals("http://www.npa.go.jp/hakusyo/h15/pdf/E0200000.pdf")
										|| site[0].equals("http://library.jsce.or.jp/jsce/open/00039/200811_no38/pdf/230.pdf")
										|| site[0].equals("http://repository.lib.tottori-u.ac.jp/Repository/file/3430/20131101094519/G31_15101A00532.pdf")
										|| site[0].equals("http://www.tetsujin.ne.jp/ir/pdf/iro20040611b.pdf")
										|| site[0].equals("http://www.city.akita.akita.jp/city/ur/im/keikaku/6thmasterplan/zentai.pdf")
										|| site[0].equals("http://www.setagaya-ido.or.jp/data/2014/141108/sir/4_siryo3.pdf")
										|| site[0].equals("http://www.city.nasushiobara.lg.jp/41/documents/0305.pdf")
										|| site[0].equals("http://www.mlit.go.jp/common/000048913.pdf")
										|| site[0].equals("http://www.shizuokabank.co.jp/companyinfo/pdf/kankyou-5.pdf")
										|| site[0].equals("http://www.city.hida.gifu.jp/b_shimin/w_manabu_tanoshimu/w_gakkou/data/ibunka.pdf")
										|| site[0].equals("http://www2.rikkyo.ac.jp/web/taki/contents/2006/report2006e.pdf")
										|| site[0].equals("http://www.pref.nara.jp/secure/13981/plan_4.pdf")) { continue;}
								System.out.println(site[0]);
								URL url = new URL(site[0]);
								HttpURLConnection http;
								http = (HttpURLConnection) url.openConnection();
								http.setRequestMethod("GET");
								http.setConnectTimeout(20000);
								http.connect();
								InputStream is = http.getInputStream();
								File file = new File(crawlStorageFolder, site[1] + ".pdf");
								Files.copy(is, file.toPath());
								is.close();
							} catch(Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
