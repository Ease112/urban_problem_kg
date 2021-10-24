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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.vocabulary.RDFS;
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

public class CrawlDBpediaRelations {
	static ArrayList<String> visited = new ArrayList<String>();
	
	public static ArrayList<String> getDBpediaRelations(String urbanproblem) {
		ArrayList<String> result = new ArrayList<String>();
		try {
			String prefix = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
			String sparql = 
					"select distinct (str(?label) AS ?word) where { "
					+ "{"
					+ "?s ?p <http://ja.dbpedia.org/resource/" + urbanproblem + "> ; "
					+ "rdfs:label ?label . "
					+ "} union { "
					+ "<http://ja.dbpedia.org/resource/" + urbanproblem + "> ?p2 ?o . "
					+ "?o rdfs:label ?label . "
					+ "}"
					+ "}";
			Query query = QueryFactory.create(prefix+sparql);
			QueryExecution qexec = QueryExecutionFactory.sparqlService("http://ja.dbpedia.org/sparql", query);
			ResultSet rs = qexec.execSelect();
			QuerySolution qs = null;
			while(rs.hasNext()) {
				qs = rs.nextSolution();
				System.out.println(qs.getLiteral("word").toString());
				result.add(qs.getLiteral("word").toString());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		try {
//			String keyword = "factor";
//			WordNet wn = new WordNet("http://www.ohsuga.lab.uec.ac.jp/sparql");
//			ArrayList<String> word_list = wn.getWordSynsetList(keyword);
			//[factor, ファクタ, エレメント, ファクター, 因子, 素因, 要因, 要素, 導因, cause, もと, 原因, 誘因, 起こり, 起り]
			String index = "social";
			String type = "fushugaku2_factor";
			String problem = "不就学";
			String factorOrCause = "factor";
			ArrayList<String> title_list = new ArrayList<String>();
			title_list = Controller.getExistances(index, type);
			ArrayList<String> dbpediaRelations = getDBpediaRelations(problem);
			
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
			//int id = 1;
			
			for(int i=1; i<50; i+=10) {
				for(String search_word: word_list) {
					for(String dbpedia : dbpediaRelations) {
						ArrayList<String> sites = Controller.getGoogleSiteList(problem + "+" + dbpedia + "+" + search_word, i);
						sites.addAll(Controller.getBingSiteList(problem + " " + search_word, i-1));
						for(String site: sites) {
							//controller.addSeed(site);
							System.out.println(site);
							if(!site.equals("\"https://www.city.matsuyama.ehime.jp/iken/faq/Anser/FAQ12237.html\"")
									&& !site.equals("\"http://www.patagonia.jp/resource-use.html\"")
									&& !site.contains("gaosan.com/")
									&& !site.contains(".cn/")) {
								//Mercury APIを使用してメインコンテンツを抽出
								//String[] title_content = getSiteContents(site);

								//全文抽出
								String[] title_content = Controller.getSiteText(site);
								try {
									if(title_content[1].length() > 0 && !title_list.contains(title_content[0])) {
										Controller.store(title_content[0], title_content[1], site, index, type);
										System.out.println("stored");
									}
//									Thread.sleep(1000);
								} catch(Exception e2) {
									e2.printStackTrace();
								}
							}
						}
					}
				}
			}
			
//			singleSiteIndex("https://ja.wikipedia.org/wiki/都市型犯罪",index,type,title_list);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
//		//controller.addSeed();
//
//		/*
//		 * Start the crawl. This is a blocking operation, meaning that your code
//		 * will reach the line after this only when crawling is finished.
//		 */
//		controller.start(MyCrawler.class, numberOfCrawlers);
	}
}
