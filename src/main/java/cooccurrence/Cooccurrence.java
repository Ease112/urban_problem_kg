package cooccurrence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseAnalyzer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer.Mode;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.dict.UserDictionary;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.BaseFormAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.PartOfSpeechAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.ReadingAttribute;

import crawling.Controller;
/*
 * これを実行してからAggregate.java
 * */
public class Cooccurrence {
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

			while (tokenizer.incrementToken()) {
				String[] token_arr = new String[2];
				token_arr[0] = charAttr.toString();
				token_arr[1] = posAttr.getPartOfSpeech();
				if(token_arr[1].contains("名詞")) {
					results.add(token_arr[0]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	public static Map<String,Integer> count(Map<String,Integer> map, String[] sentences) {
		try {
			ArrayList<String> morphemes = new ArrayList<String>();
			for(String sentence : sentences) {
				morphemes = getMorphemes(sentence);
				int cnt = 0;
				for(String noun : morphemes) {
					if(map.containsKey(noun)) {
						cnt = map.get(noun);
						cnt++;
						map.put(noun, cnt);
					} else {
						map.put(noun, 1);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public static void main(String[] args) {
		String index = "cooccurrence";
		String type = "bicycle_parking";
		
		
		Map<String,Integer> countMap = new HashMap<String,Integer>();

		try {
			
			FileReader fr = new FileReader("/Users/Shusaku/Dropbox/research/doctor/共起/中間ノード.csv");
			BufferedReader br = new BufferedReader(fr);
			String line="";
			ArrayList<String> problems = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				problems.add(line);
			}

			ArrayList<String> sites = new ArrayList<String>();
			String[] title_content = {};
			String[] sentences = null;
			FileWriter fw = null;
			for(String problem : problems) {
				countMap = new HashMap<String,Integer>();	//problem毎に初期化
				System.out.println(problem);
				for(int i=1; i<50; i+=10) {
					sites = Controller.getGoogleSiteList(problem, i);
					for(String site: sites) {
						//controller.addSeed(site);
//						System.out.println(site);
						if(!site.equals("\"https://www.city.matsuyama.ehime.jp/iken/faq/Anser/FAQ12237.html\"")
								&& !site.equals("\"http://www.patagonia.jp/resource-use.html\"")) {

							//全文抽出
							try {	//サイト取得エラー回避
								title_content = Controller.getSiteText(site);
								
								//elasticsearchに保存
//								if(title_content[1].length() > 0 && !title_list.contains(title_content[0])) {
//									Controller.store(title_content[0], title_content[1], site, index, type);
//									System.out.println("stored");
//								}
								
								if(title_content[1].contains("。")) {
									sentences = title_content[1].split("。");
								} else {
									sentences = title_content[1].split("．");
								}
								
								//1サイトにおける名詞カウント
								countMap = count(countMap,sentences);
							} catch(Exception e2) {
								e2.printStackTrace();
							}
							
						}
					}
				}
//				System.out.println(countMap);
				fw = new FileWriter("/Users/Shusaku/Dropbox/research/doctor/共起/20171231/" + problem + "_20171231.csv");
				for(Map.Entry<String, Integer> entry : countMap.entrySet()) {
					fw.write(entry.getKey() + "," + entry.getValue() + "\n");
				}
				fw.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
