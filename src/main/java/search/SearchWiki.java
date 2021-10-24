package search;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SearchWiki {
	final static List<String> factorWords = Arrays.asList("原因","理由","要因");
	final static List<String> causeWords = Arrays.asList("影響","問題");

	public static String[] getSentences(String text,String type) {
		String[] sentences = null;
		try {
			Document document = Jsoup.parse(text);
			Element mwContentText = document.getElementById("mw-content-text");
			Elements els = mwContentText.children();
			boolean causal_flag = false;
			StringBuilder sb = new StringBuilder();
			for(Element el : els) {
				if(el.tagName().equals("h2")) {
					causal_flag = checkCausalH2(el.text(),type);
//					System.out.println(el.toString());
				} else if(causal_flag){
					if((el.className().equals("mw-editsection-bracket"))
							|| (el.className().equals("mw-editsection"))
							|| (el.className().equals("mw-headline"))) {
						continue;
					}
					sb.append(el.text() + "\n");
				}
			}
//			System.out.println(sb.toString());
			sentences = sb.toString().split("。");
		} catch(Exception e) {
			e.printStackTrace();
		}
		return sentences;
	}
	
	public static String getPlainText(String text) {
		String result = "";
		try {
			Document document = Jsoup.parse(text);
			result = document.text();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static ArrayList<String> getKeyphraseList(String sentence, int threashold) {
		ArrayList<String> keyphraseList = new ArrayList<String>();
		String yahooId = "";
		String apiURL = "https://jlp.yahooapis.jp/KeyphraseService/V1/extract?appid=";
		try{
			URL url = new URL(apiURL + yahooId + "&sentence=" + URLEncoder.encode(sentence, "UTF-8"));
			HttpURLConnection http;
			http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("GET");
			http.connect();
			InputStream is = http.getInputStream();
			Document doc = Jsoup.parse(is, "UTF-8", "");
			Elements results = doc.getElementsByTag("result");
			for(Element result : results) {
				String keyphrase = result.getElementsByTag("keyphrase").text();
				String score = result.getElementsByTag("score").text();
				if(Integer.parseInt(score) > threashold) {
					keyphraseList.add(keyphrase);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return keyphraseList;
	}
	
	//因果関係を記述する節を持っているか
	public static boolean hasCausalH2(String text, String type) {
		boolean result = false;
		try {
			Document document = Jsoup.parse(text);
			Elements h2s = document.getElementsByTag("h2");
			for(Element h2 : h2s) {
				result = checkCausalH2(h2.text(),type);
				if(result) {
					return result;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static boolean checkCausalH2(String h2, String type) {
		boolean result = false;
		try {
			if(type.equals("factor")) {
				for(String str : factorWords) {
					if(h2.contains(str)) {
						result = true;
					}
				}
			} else if(type.equals("cause")) {
				for(String str : causeWords) {
					if(h2.contains(str)) {
						result = true;
					}
				}
			} else {
				System.out.println("不明なtype: " + type);
				System.exit(0);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String getWikiText(String problem, String index, String type) {
		String result = "";
		try {
			TransportClient client = new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
			SearchResponse response = client.prepareSearch(index)
					.setTypes(type) 
					.addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setScroll(new TimeValue(60000))
					.setQuery(QueryBuilders.matchPhraseQuery("title", problem))                 // Query
					//.setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))     // Filter
					.setFrom(0).setSize(60).setExplain(true)
					.get();

			SearchHit[] sh = response.getHits().getHits();
			Map<String,Object> map = sh[0].sourceAsMap();
			result = (String) map.get("content");

		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void main(String[] args) {
		try {
			String problem = "シャッター通り";
			String index = "social";
			String type = "dead_mall_wiki";
			
			String text = getWikiText(problem,index,type);
			System.out.println(getPlainText(text));
			System.out.println(hasCausalH2(text,"cause"));
			String[] sentences = getSentences(text,"cause");
			System.out.println(sentences[5]);
			ArrayList<String> keyphraseList = getKeyphraseList(sentences[0],50);
			System.out.println(keyphraseList);
			
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

}
