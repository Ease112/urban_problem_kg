package search;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.poi.util.SystemOutLogger;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseAnalyzer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer.Mode;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.dict.UserDictionary;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.BaseFormAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.PartOfSpeechAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.ReadingAttribute;
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

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.atilika.kuromoji.ipadic.Tokenizer.Builder;

import stringsimilarity.JaroWinkler;


public class Search {
	static String index = "social";
	static String type = "domestic_violence_cause";
	static int all_sentence = 0;
	static int sentence_count = 0;
	static int page_count = 0;
	static Builder builder;
	static ArrayList<String> urbanProblems = new ArrayList<String>();
	static String[] stop_words = {"尐","真","択","差","等","化","お","代","細","各","他","ごと","最","内","間","難","22人","一つ","二つ","三つ","１つ","２つ","３つ","3つ","前者","後者","もの","発","４つ","５つ","的","一人","１人","製","型","率","4人","上","下","右","左","最大","最小","基本","種","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
	private static void readDictionary() {
		try {
			builder = new Tokenizer.Builder();
			builder.userDictionary("user_dic.csv");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean checkStopWord(String term) {
		boolean stopword_flag = false;
		for(String stop: stop_words) {
			if(term.equals(stop)) {
				stopword_flag = true;
			}
		}
		return stopword_flag;
	}

	private static ArrayList<String> getMorphemes(String sentence) {
		ArrayList<String> results = new ArrayList<String>();
		try {
			UserDictionary userDict = null;
			Mode mode = JapaneseTokenizer.Mode.NORMAL;
			CharArraySet stopSet = JapaneseAnalyzer.getDefaultStopSet();
			Set<String> stopTags = JapaneseAnalyzer.getDefaultStopTags();
			JapaneseTokenizer tokenizer = new JapaneseTokenizer(userDict, false, mode);
			Reader reader = new StringReader(sentence);

			//			Tokenizer tokenizer = builder.build();
			//			List<Token> tokens = tokenizer.tokenize(sentence);
			tokenizer.setReader(reader);

			BaseFormAttribute baseAttr = tokenizer.addAttribute(BaseFormAttribute.class);
			CharTermAttribute charAttr = tokenizer.addAttribute(CharTermAttribute.class);
			PartOfSpeechAttribute posAttr = tokenizer.addAttribute(PartOfSpeechAttribute.class);
			ReadingAttribute readAttr = tokenizer.addAttribute(ReadingAttribute.class);

			tokenizer.reset();

			ArrayList<String[]> tokens = new ArrayList<String[]>();
			while (tokenizer.incrementToken()) {
				String[] token_arr = new String[2];
				token_arr[0] = charAttr.toString();
				token_arr[1] = posAttr.getPartOfSpeech();
				tokens.add(token_arr);
			}

			String noun = "";
			for (int i=0; i<tokens.size(); i++) {
				//				String text = charAttr.toString();                // 単語
				//				String partOfSpeech = posAttr.getPartOfSpeech();    // 品詞
				//				if(partOfSpeech.contains("名詞") && !partOfSpeech.equals("名詞-固有名詞-組織") && !partOfSpeech.equals("名詞-接尾-一般") && !partOfSpeech.equals("名詞-数")) {
				//					System.out.println(partOfSpeech);
				//					results.add(text);
				//				}
				if(tokens.get(i)[1].contains("名詞") 
						&& !tokens.get(i)[1].equals("名詞-数") 
						&& !tokens.get(i)[1].equals("名詞-非自立-一般")
						&& !tokens.get(i)[1].equals("名詞-非自立-形容動詞語幹")
						&& !tokens.get(i)[1].equals("名詞-非自立-助動詞語幹")
						&& !tokens.get(i)[1].equals("名詞-非自立-副詞可能")
						&& !tokens.get(i)[1].equals("名詞-特殊-助動詞語幹")
						&& !tokens.get(i)[1].equals("名詞-引用文字列")
						&& !tokens.get(i)[1].equals("名詞-ナイ形容詞語幹")
						&& !tokens.get(i)[1].equals("名詞-形容動詞語幹")
						&& !tokens.get(i)[1].equals("名詞-動詞非自立的")
						&& !tokens.get(i)[1].equals("名詞-副詞可能")
						&& !tokens.get(i)[1].equals("名詞-接続詞的")
						&& !tokens.get(i)[1].equals("名詞-接尾-特殊")
						&& !tokens.get(i)[1].equals("名詞-接尾-副詞可能")
						&& !tokens.get(i)[1].equals("名詞-接尾-人名")
						&& !tokens.get(i)[1].equals("名詞-接尾-助動詞語幹")
						&& !tokens.get(i)[1].equals("名詞-接尾-助数詞")
						&& !tokens.get(i)[1].equals("名詞-代名詞-一般")
						&& !tokens.get(i)[1].equals("名詞-代名詞-縮約")
						) {

					//					results.add(tokens.get(i)[0]);

					//					//サ変接続
					if((tokens.get(i)[1].equals("名詞-サ変接続") 
							)) {
						//最初
						if(i==0) {
							//後ろが名詞
							if(tokens.get(i+1)[1].contains("名詞")) {
								//後ろが接尾だった場合は無視
								if(tokens.get(i+1)[1].equals("名詞-接尾-一般")) {
									if(!checkStopWord(tokens.get(i)[0])) {
										results.add(tokens.get(i)[0]);
									}
								} else {
									noun = tokens.get(i)[0];
								}
							} 
							//後ろが名詞じゃない
							else {
								if(!checkStopWord(tokens.get(i)[0])) {
									results.add(tokens.get(i)[0]);
								}
								noun = "";
							}
						}
						//最後のサ変接続・接尾
						else if((i == tokens.size()-1)) {
							//前が名詞
							if(tokens.get(i-1)[1].contains("名詞")) {
								if(!checkStopWord(tokens.get(i)[0])) {
									results.add(noun + tokens.get(i)[0]);
								}
							} else if(tokens.get(i)[1].equals("名詞-サ変接続")){
								if(!checkStopWord(tokens.get(i)[0])) {
									results.add(tokens.get(i)[0]);
								}
							}
						} 
						//最初でも最後でもないサ変接続・接尾
						else {
							//後ろだけ名詞
							if(!tokens.get(i-1)[1].contains("名詞") && tokens.get(i+1)[1].contains("名詞")) {
								//後ろが接尾だった場合は後ろを無視
								//								if(tokens.get(i+1)[1].equals("名詞-接尾-一般")) {
								//									results.add(tokens.get(i)[0]);
								//								} else {
								noun = tokens.get(i)[0];
								//								}
							}
							//前後が名詞
							else if(tokens.get(i-1)[1].contains("名詞") && tokens.get(i+1)[1].contains("名詞")) {
								//								if(tokens.get(i+1)[1].equals("名詞-接尾-一般")) {
								//									results.add(tokens.get(i)[0]);
								//								} else {
								noun += tokens.get(i)[0];
								//								}
							}
							//前だけ名詞
							else if(tokens.get(i-1)[1].contains("名詞") && !tokens.get(i+1)[1].contains("名詞")) {
								if(!checkStopWord(tokens.get(i)[0])) {
									results.add(noun + tokens.get(i)[0]);
								}
								noun = "";
							}
						}
					} 
					//サ変接続以外の名詞
					else {
						//最後
						if(i == tokens.size()-1) {
							if(!checkStopWord(tokens.get(i)[0])) {
								results.add(noun + tokens.get(i)[0]);
							}
						}
						//最後じゃない
						else {
							//後ろがサ変接続
							if(tokens.get(i+1)[1].equals("名詞-サ変接続")) {
								if(!checkStopWord(tokens.get(i)[0])) {
									noun = noun + tokens.get(i)[0];
								}
							}
							//後ろがサ変接続でない
							else {
								if(!checkStopWord(tokens.get(i)[0])) {
									results.add(noun + tokens.get(i)[0]);
								}
								noun = "";
							}
						}
					}
				} else {
					noun = "";
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	public static ArrayList<String> text2lod(String sentence, String keyword) {
		ArrayList<String> extracted_phrases = new ArrayList<String>();
		ArrayList<String> extracted_words = new ArrayList<String>(); 
		String result = "";
		String endpoint = "http://text2lod.tk/?q=";
		sentence = sentence.replaceAll("%", "％");
		try {
			//			Thread.sleep(1000);
			URL url = new URL(endpoint + URLEncoder.encode(sentence,"UTF-8"));
			HttpURLConnection http;
			http = (HttpURLConnection)url.openConnection();
			http.setRequestMethod("GET");
			http.connect();
			InputStream is = http.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line;
			while (null != (line = br.readLine())) {
				sb.append(line+"\n");
			}
			result = sb.toString();
			br.close();
			is.close();
			//			System.out.println("+++++");
			//			System.out.println(sentence);
			//			System.out.println(result);

			java.io.Reader reader = new java.io.StringReader(result);

			Model model = ModelFactory.createDefaultModel();
			model.read(reader, null);
			String sparql = "SELECT DISTINCT "
					//+ "(?p2 AS ?成分) (?o2 AS ?値) "
					+ "(str(?o2) AS ?value) \n"
					+ "WHERE {\n"
					+ "{\n"
					+ "SELECT ?s ?p WHERE { ?s ?p ?o \n"
					+ "filter (?p != <http://www.uec.ac.jp/property/SENTENCE>)\n"
					+ "filter regex(str(?o),\"" + keyword + "\")\n"
					+ "}\n"
					+ "}\n"
					+ "OPTIONAL {"
					+ "?s ?p2 ?o2 .\n"
					+ "filter(?p2 != <http://www.uec.ac.jp/property/SENTENCE>)\n"
					+ "}"
					+ "}";
			//			String sparql = "SELECT DISTINCT "
			//					+ "(str(?o) AS ?value) \n"
			//					+ "WHERE { "
			//					+ "?s ?p ?o .\n"
			//					+ "filter(?p != <http://www.uec.ac.jp/property/SENTENCE>)\n"
			//					+ "filter(?p != <http://www.uec.ac.jp/property/ACTION>)\n"
			//					+ "}";
			showSPARQLResults(sparql,model);
			//model.write(System.out, "TTL");
			//System.out.println(result);

			Query query = QueryFactory.create(sparql);
			// Execute the query and obtain results
			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet rs = qe.execSelect();
			// Output query results	

			String extracted = "";

			QuerySolution qs;

			while(rs.hasNext()) {
				qs = rs.next();
				if(qs.contains("value")) {
					extracted = qs.get("value").asLiteral().toString();

					extracted_phrases.add(extracted);
				}
			}

			// Important - free up resources used running the query
			qe.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return extracted_phrases;
	}

	private static void showSPARQLResults(String sparql, Model model) {
		Query query = QueryFactory.create(sparql);
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();
		// Output query results	
		ResultSetFormatter.out(System.out, results, query);
		// Important - free up resources used running the query
		qe.close();
	}

	public static ArrayList<Map<String,Object>> searchArticles(String word) {
		Map<String,Object> result_map = new HashMap<String,Object>();
		ArrayList<Map<String,Object>> result_map_list = new ArrayList<Map<String,Object>>();
		try {
			TransportClient client = new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
			SearchResponse response = client.prepareSearch(index)
					.setTypes(type) 
					.addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setScroll(new TimeValue(60000))
					.setQuery(QueryBuilders.matchPhraseQuery("content", word))                 // Query
					//.setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))     // Filter
					.setFrom(0).setSize(60).setExplain(true)
					.get();

			do {
				for (SearchHit hit : response.getHits().getHits()) {
					result_map = hit.sourceAsMap();
					result_map.put("_id", hit.getId());
					result_map_list.add(result_map);
				}
				response = client.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
			} while(response.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.

		} catch(Exception e) {
			e.printStackTrace();
		}
		return result_map_list;
	}

	private static String getColor(int i) {
		String result = "";
		if(i % 8 == 0) { result = "#ff7c30"; }
		else if(i %  7 == 0) { result = "#1be20d"; }
		else if(i % 6 == 0) { result = "#13d8d5"; }
		else if(i % 5 == 0) { result = "#1361f2"; }
		else if(i % 4 == 0) { result = "#8f4df9"; }
		else if(i % 3 == 0) { result = "#05911f"; }
		else if(i % 2 == 0) { result = "#d60c0c"; }
		else { result = "#060049"; }
		return result;
	}

	public static void outputJSON(Map<String,Integer> map) {
		try {
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream("web/" + type + "_20171203_neologd_min3_wiki_notMoto_jaro_080_cabocha_delete_weight0_bing_decrypt.json"),"UTF-8");
			osw.write("{\r\n\"value\": [\r\n");
			int size = map.size();
			int i = 0;
			String word = "";
			int weight = 0;
			int threashold = 0;
			for(Map.Entry<String, Integer> e : map.entrySet()) {
				word = e.getKey();
				weight = e.getValue();
//				for(String up: urbanProblems) {
//					if(e.getKey().contains(up)) {
//						weight += 10;
//					}
//				}
				//e2d3用
				//				osw.write(word + "," + weight + "\r\n");
				if(weight > threashold) {
					if(i % 5 == 0) {
//						osw.write("{\"text\": \"" + word + "\", \"weight\": " + Math.log((double)Math.pow(weight*10,3)) + ", \"color\": \"" + getColor(i) + "\", \"html\": {\"class\": \"vertical\"}}");
						osw.write("{\"text\": \"" + word + "\", \"weight\": " + weight + ", \"color\": \"" + getColor(i) + "\", \"html\": {\"class\": \"vertical\"}}");
					} else {
//						osw.write("{\"text\": \"" + word + "\", \"weight\": " + Math.log((double)Math.pow(weight*10,3))  + ", \"color\": \"" + getColor(i) + "\" }");
						osw.write("{\"text\": \"" + word + "\", \"weight\": " + weight  + ", \"color\": \"" + getColor(i) + "\" }");
					}
					if(i < size-1) {
						osw.write(",\r\n");
					}
				}
				i++;
			}
			osw.write("]}");
			osw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
//	private static void getWikiLinkUrbanProblems() {
//		String sparql = "";
//		urbanProblems.add(line);
//	}

//	private static void readUrbanProbleFile() {
//		try {
//			FileReader fr = new FileReader("/Users/Shusaku/Dropbox/research/doctor/都市問題_リダイレクト含む.csv");
//			BufferedReader br = new BufferedReader(fr);
//			String line="";
//			while((line = br.readLine()) != null) {
//				urbanProblems.add(line);
//			}
//			br.close();
//			fr.close();
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}

	private static Map<String,Integer> weightSimilarWord(Map<String,Integer> map) {
		Map<String,Integer> result = new HashMap<String,Integer>(map);
		try {
			String word = "";
			int weight1 = 0;
			int weight2 = 0;
			double threashold = 0.80;
			ArrayList<String> list = new ArrayList<String>();
			for(Map.Entry<String, Integer> e : result.entrySet()) {
				word = e.getKey();
				list.add(word);
			}
			double distance = 0;
			String str1 = "";
			String str2 = "";
//			LevensteinDistance levensteinAlgorithm = new LevensteinDistance();
			JaroWinkler jw = new JaroWinkler();
			for(int i=0; i<list.size()-1; i++) {
				str1 = list.get(i);
				for(int j=i+1; j<list.size(); j++) {
					str2 = list.get(j);
					distance = jw.similarity(str1, str2);
//					distance = levensteinAlgorithm.getDistance(str1, str2);
					//					if(distance == 1.0) {
					//						System.out.println("distance:" + distance + ", str1=" + str1 + ":" + weight1 + ", str2=" + str2 + ":" + weight2);
					//						System.out.println("i:" + i + ", j:" + j);
					//						System.exit(0);
					//					}
					if(distance > threashold) {
						weight1 = result.get(str1);
						weight2 = result.get(str2);
						System.out.println("distance:" + distance + ", str1=" + str1 + ":" + weight1 + ", str2=" + str2 + ":" + weight2);
						//文字列が長い方に重みを統合。文字列長が同じ場合は重みが多い方に統合。重みも同じ場合はstr2に統合。
						if(str1.length() > str2.length()) {
							weight1 += weight2;
							weight2 = 0;
						} else if (str2.length() > str1.length()) {
							weight2 += weight1;
							weight1 = 0;
						} else {
							if(weight1 > weight2) {
								weight1 += weight2;
								weight2 = 0;
							} else {
								weight2 += weight1;
								weight1 = 0;
							}
						}
						result.put(str1, weight1);
						result.put(str2, weight2);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	//Wikipediaに関して　(sentence:String, phrase:String, 名詞リスト:ArrayList<String>)のMapを持つリスト 
	public static ArrayList<Map<String,Object>> createWikiExtractedList(String problem, String content, String factorOrCause) {
		ArrayList<Map<String,Object>> extracted_list = new ArrayList<Map<String,Object>>();
		try {
			String[] sentences = SearchWiki.getSentences(content,factorOrCause);
			int wikiThreashold = 50;	//wikipediaからyahooキーワードフレーズ解析で抽出する際の閾値
			for(String sentence : sentences) {
				ArrayList<String> tmp_noun_list = SearchWiki.getKeyphraseList(sentence, wikiThreashold);
				for(int tnl=0; tnl<tmp_noun_list.size(); tnl++) {
					if(tmp_noun_list.get(tnl).contains(problem)) {
						tmp_noun_list.remove(tnl);
					}
				}
				Map<String,Object> extracted = new HashMap<String,Object>();
				extracted.put("sentence", sentence);
				extracted.put("phrase", "wiki");
				extracted.put("value", tmp_noun_list);
				extracted_list.add(extracted);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return extracted_list;
	}

	public static void main(String[] args) {
		//readDictionary();
		
		//		String keyword = "factor";
		
		String problem = "家庭内暴力";
		String factorOrCause = "cause";
		try {
//			readUrbanProbleFile();
//			getWikiLinkUrbanProblems();
			String id = "";
			String title = "";
			String content = "";
			String url = "";
			WordNet wn = new WordNet("http://www.ohsuga.lab.uec.ac.jp/sparql");
			//ArrayList<String> word_list = wn.getWordSynsetList(keyword);
			ArrayList<String> word_list = new ArrayList<String>();
			
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
			
			ArrayList<Map<String,Object>> result_map_list = new ArrayList<Map<String,Object>>();	//elasticsearchから取得した結果
			ArrayList<String> match_sentences = new ArrayList<String>();
			
			//結果の保持
			ExtractResult er;	//文書のID,title,synonym_keywords(第二検索語をキー，extracted_listを値としたMap)を保持するクラス
			ExtractResultList er_list = new ExtractResultList();	//ExtractedResultのリスト. つまりこれをカウントすると抽出箇所のあった文書数になる
			Map<String,ArrayList<Map<String,Object>>> synonym_keywords;
			ArrayList<Map<String,Object>> extracted_list;	//(sentence:String, phrase:String, 名詞リスト:ArrayList<String>)のMapを持つリスト 
			Map<String,Object> extracted;
			boolean wikiFlag = false;	//wikipediaからはsynonym_keywords関係なく１回だけ抽出作業を行うため，wikiからの抽出が終了したかどうかのフラグ 
			boolean h2Flag = false;		//wikipediaに因果関係を記述する節があるか
			
			for(String word: word_list) {
				System.out.println(word);
				result_map_list = searchArticles(word);
				for(int i=0; i<result_map_list.size(); i++) {
					if(result_map_list.isEmpty()) { break; }

					id = (String) result_map_list.get(i).get("_id");
					title = (String) result_map_list.get(i).get("title");
					url = (String) result_map_list.get(i).get("url");
					content = (String) result_map_list.get(i).get("content");
//					System.out.println(url);
					if(url == null) {url = "";}	//webページじゃないときはnullになるため
					
					String[] sentences;
					extracted_list = new ArrayList<Map<String,Object>>();
					
					//wikipediaの場合は別の抽出プロセス
//					if(url.contains("wikipedia") && !wikiFlag) {
//						//因果関係を記述する節を持っているか
//						h2Flag = SearchWiki.hasCausalH2(content, factorOrCause);
//						if(h2Flag) {
//							extracted_list = createWikiExtractedList(problem,content,factorOrCause);
//						} else {
//							content = SearchWiki.getPlainText(content);
//						}
//						wikiFlag = true;
//					} 
//					
					//wikipedia以外，またはwikipediaだけど因果関係を記述する節を持っていなかった場合
					if(!url.contains("wikipedia") || (url.contains("wikipedia") && !h2Flag)) {
						
						if(content.contains("。")) {
							sentences = content.split("。");
						} else {
							sentences = content.split("．");
						}
						all_sentence += sentences.length;

						//「放置自転車」「違法駐輪」の含まれている回数で文書のフィルタリング
						int issue_count = 0;
						for(String sentence: sentences) {
							//違法駐輪などの類義語を含めることを忘れずに
							if(sentence.contains(problem)
//									|| sentence.contains("通勤ラッシュ")
//									|| sentence.contains("ラッシュアワー")
									) {
								issue_count++;
							}
						}
						if(issue_count >= 3) {
							//全センテンスループ
							for(String sentence: sentences) {
								//キーワードにマッチする文かつ長すぎない
								if(sentence.contains(word) && sentence.length() < 200) {
									//							System.out.println(sentences[i]);
									match_sentences.add(sentence);
									ArrayList<String> morphemes = new ArrayList<String>();	//形態素(名詞)の集合
									//形態素集合を取得
									morphemes = getMorphemes(sentence);
									System.out.println("形態素=" + morphemes);
									//text2lodの代わりにcabocha使用
									Map<String,String[]> cabocha_results = Cabocha.executeCabocha(sentence);
									ArrayList<String> extracted_phrases = Cabocha.getChunks(cabocha_results,word);	//フレーズの集合
									//text2lod使用
									//								ArrayList<String> extracted_phrases = text2lod(s,word);	//フレーズの集合

									//ArrayList<String> values = new ArrayList<String>();
									//フレーズごと
									for(String phrase : extracted_phrases) {
										if(phrase.length() > 0
												&& !phrase.equals("(unknown)") 
												&& !phrase.equals("\\")
												&& !phrase.equals("is-a")
												&& !phrase.contains(word)){

											//フレーズの中に予め形態素解析していた名詞が含まれていたら、その名詞を選択する．
											ArrayList<String> tmp_noun_list = new ArrayList<String>();
											for(String noun: morphemes) {
												if(phrase.contains(noun)
														&& !tmp_noun_list.contains(noun)
														&& !noun.contains(problem)	//違法駐輪などの類義語を含めることを忘れずに
														&& !noun.contains("DV")
//														&& !noun.contains("通勤ラッシュ")
//														&& !noun.contains("ラッシュアワー")
														&& !word_list.contains(noun)) {
													System.out.println("形態素->" + noun + ", フレーズ->" + phrase);
													tmp_noun_list.add(noun);
												}
											}
											//１フレーズごとにインスタンスを生成し、リストに保持
											if(tmp_noun_list.size() > 0) {
												extracted = new HashMap<String,Object>();
												extracted.put("sentence", sentence);
												extracted.put("phrase", phrase);
												extracted.put("value", tmp_noun_list);
												extracted_list.add(extracted);
											}

										}
									}
								}
							}
						}
					}
					
//					Thread.sleep(3000);
					match_sentences.clear();
					//synonym_keywordsは第二検索語をキー，extracted_listを値としたMap
					//extracted_listはsentence,phrase,valueのMapのリスト
					synonym_keywords = new HashMap<String,ArrayList<Map<String,Object>>>();
					synonym_keywords.put(word, extracted_list);
					//er_listはExtractResultListのインスタンス。ExractResultのリストを保持してる。
					if(er_list.contains(id)) {
						er = er_list.getById(id);
						er.put(word, extracted_list);
					} else {
						//最初は絶対elseに行く
						//erはid,title,synonym_keywordsを保持するインスタンス
						er = new ExtractResult(id,title);
						er.put(word, extracted_list);	//synonym_keywordsにextracted_listを加える
						er_list.add(er);
					}
					
				}
			}

//			System.out.println("抽出箇所のあった文書数: "+ er_list.size());

			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream("csv/" + type + "_20171203_neologd_min3_wiki_notMoto_jaro_080_cabocha_delete_weight0_bing_decrypt.csv"),"UTF-8");
			Map<String,Integer> final_result = new HashMap<String,Integer>();
			int cnt = 0;
			int docCount = 0;
			ArrayList<String> docIdList = new ArrayList<String>();
			for(int i=0; i<er_list.size(); i++) {
				ExtractResult er2 = er_list.get(i);
				//synonym_keywordsの取得
				Map<String,ArrayList<Map<String,Object>>> synonym_map = er2.getSynonym_keywords();
				for(String key: synonym_map.keySet()) {
					//extracted_list(sentence,phrase,valueのMapのリスト)の取得
					ArrayList<Map<String,Object>> res_list = synonym_map.get(key);
					String tmp_sentence = "";	//抽出文カウントで重複を省くために一時的に記憶しておく
					for(Map<String,Object> res_map : res_list) {
						if(!docIdList.contains(er2.getId())) {
							docCount++;
							docIdList.add(er2.getId());
						}
						if(!tmp_sentence.equals((String)res_map.get("sentence"))) {
							sentence_count += 1;
						}
						osw.write("title=" + er2.getTitle() + ", keyword=" + key + ", value=" + res_map.get("value") + ", phrase=" + res_map.get("phrase") + ", sentence=" + res_map.get("sentence")+"\r\n");
						ArrayList<String> temp = (ArrayList<String>) res_map.get("value"); 
						for(String term: temp) {
							if(final_result.containsKey(term)) {//名詞が既に出てきていた場合
								cnt = final_result.get(term);
								cnt++;
								final_result.put(term, cnt);
							} else { //初めて出てくる名詞
								final_result.put(term, 1);
							}
						}
						tmp_sentence = (String) res_map.get("sentence");
					}
				}
			}
			osw.close();

			final_result = weightSimilarWord(final_result);

			outputJSON(final_result);

			System.out.println("抽出文があった文書数:" + docCount);
			System.out.println("抽出された文:" + sentence_count);
			System.out.println("全文数:" + all_sentence);
		} catch(Exception e){
			e.printStackTrace();
		}

	}

}
