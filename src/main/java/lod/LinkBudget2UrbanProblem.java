package lod;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseAnalyzer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer.Mode;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.dict.UserDictionary;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.BaseFormAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.PartOfSpeechAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.ReadingAttribute;

import com.sun.syndication.io.SyndFeedOutput;



public class LinkBudget2UrbanProblem {
	protected static String prefix = 
			"prefix rdf: <" + RDF.getURI() + ">\n"
			+ "prefix rdfs: <" + RDFS.getURI() + ">\n"
			+ "prefix owl: <" + OWL.NS + ">\n"
			+ "prefix dcterms: <" + DCTerms.NS + ">\n";

	private static String[] stop_words = {"対策","用","職員","事業","整備","他","物質","行為","支援","家族","向上","利用",
			"推進","駐車場","的","活動","集中","ビジネス","もの","保護","関係","能力","損失","制限",
			"変化","変動","機能","拡大","地域","分散","錯乱","開発","成長","産業","会社","低下","上昇",
			"価値","空間","組織","構造","通り","捜査","手段","企業","経験","生育","時間","社会","並み",
			"費用","債務","支給","普及","期間","供給","人間" ,"崩壊","破壊","増加","形成","秩序","防止",
			"啓蒙","条例","事務","関係","実施","管理","運営","等","費","社会的","取り組み","縮小","減少",
			"速度","建設","施設","料金","運転"};
	private static Map<String,String> mapping_rule = new HashMap<String,String>();

	private static void createMappingRules() {
		mapping_rule.put("水質汚濁","地下水汚染");
		mapping_rule.put("青色防犯パトロール", "犯罪");
		mapping_rule.put("防犯", "犯罪");
		mapping_rule.put("就学", "不就学");
		mapping_rule.put("美化", "落書き");
	}
	
	public static ArrayList<String> getMorphemes(String sentence) {
		ArrayList<String> list = new ArrayList<String>();
		try {
			UserDictionary userDict = null;
			Mode mode = JapaneseTokenizer.Mode.NORMAL;
			CharArraySet stopSet = JapaneseAnalyzer.getDefaultStopSet();
			Set<String> stopTags = JapaneseAnalyzer.getDefaultStopTags();

			JapaneseTokenizer analyzer = new JapaneseTokenizer(userDict, false, mode);

			Reader reader = new StringReader(sentence);
			analyzer.setReader(reader);

			BaseFormAttribute baseAttr = analyzer.addAttribute(BaseFormAttribute.class);
			CharTermAttribute charAttr = analyzer.addAttribute(CharTermAttribute.class);
			PartOfSpeechAttribute posAttr = analyzer.addAttribute(PartOfSpeechAttribute.class);
			ReadingAttribute readAttr = analyzer.addAttribute(ReadingAttribute.class);

			analyzer.reset();
			while (analyzer.incrementToken()) {
				String text = charAttr.toString();                // 単語
				String baseForm = baseAttr.getBaseForm();       // 原型
				String reading = readAttr.getReading();         // 読み
				String partOfSpeech = posAttr.getPartOfSpeech();    // 品詞

				if(!checkStopWord(text) && partOfSpeech.contains("名詞") && text.length() > 1) {
					list.add(text);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return list;
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
	
	//Urban Problem LODから単語マップ取得<日本語ラベル,URI>
	public static Map<String,String> getUPLODClassMap() {
		Map<String,String> map = new HashMap<String,String>();
		String endpoint = "http://www.ohsuga.lab.uec.ac.jp/sparql";
		try{
			String sparql =
					prefix
					+ "SELECT ?s (str(?l) AS ?label ) from <http://www.ohsuga.lab.uec.ac.jp/urbanproblem> WHERE {\n"
					+ "?s rdf:type owl:Class . \n"
					+ "?s rdfs:label ?l .\n"
					+ "filter(LANG(?l) = 'ja')\n"
					+ "}";
//			System.out.println(sparql);
			ResultSet rs = selectResource(endpoint, sparql);
			while(rs.hasNext()) {
				QuerySolution qs = rs.nextSolution();
				String s = qs.get("s").toString();
				String label = qs.get("label").toString();
				map.put(label, s);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	
	public static ArrayList<String> getWordNetList(String word) {
		String endpoint = "http://www.ohsuga.lab.uec.ac.jp/sparql";
		ArrayList<String> wordnet_list = new ArrayList<String>();
		try {
			String sparql = 
					prefix +
					"SELECT distinct (str(?label) AS ?synword) (str(?gloss) AS ?sememe) from <http://wordnet.jp> where {\n"
					+ "{?s rdfs:label \"" + word + "\"@en-us ;\n"
					+ "<http://www.w3.org/2006/03/wn/wn20/schema/inSynset> ?synset .\n"
					+ "?synset <http://www.w3.org/2006/03/wn/wn20/schema/gloss> ?gloss .\n"
					+ "filter(LANG(?gloss) = \"ja\")"
					+ "} union {\n"
					+ "?s rdfs:label \"" + word + "\"@en-us ;\n"
					+ "<http://www.w3.org/2006/03/wn/wn20/schema/inSynset> ?synset .\n"
					+ "?synonym <http://www.w3.org/2006/03/wn/wn20/schema/inSynset> ?synset;\n"
					+ "rdfs:label ?label . \n"
					+ "filter(contains(str(?synonym),\"http://wordnet.jp/ja11/instances/\"))"
					+ "}\n"
					+ "}";
//			System.out.println(sparql);
			ResultSet rs = selectResource(endpoint, sparql);
			String synword = "", sememe = ""; 
			ArrayList<String> morphemes = new ArrayList<String>();
			while(rs.hasNext()) {
				QuerySolution qs = rs.nextSolution();
				if(qs.get("synword") != null) {
					synword = qs.get("synword").toString();
				} else {
					synword = "";
				}
				if(qs.get("sememe") != null) {
					sememe = qs.get("sememe").toString();
				} else {
					sememe = "";
				}
				morphemes = new ArrayList<String>();
				if(!sememe.isEmpty()) {
					morphemes = getMorphemes(sememe.split(";")[0]);
					wordnet_list.addAll(morphemes);
				} else {
					if(synword.length() > 1 && !checkStopWord(synword))
						wordnet_list.add(synword);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return wordnet_list;
	}
	
	public static ResultSet selectResource(String endpoint, String sparql) {
		ResultSet rs = null;
		Query query = null;
		QueryExecution qexec = null;
		try {
			query = QueryFactory.create(sparql);
			qexec = QueryExecutionFactory.sparqlService(endpoint, query);
			rs = qexec.execSelect();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	public static ArrayList<String> getLinkableList(String business) {
		createMappingRules();
		ArrayList<String> list = new ArrayList<String>();
		try {
			Map<String,String> uplod_class_map = getUPLODClassMap();
			ArrayList<String> morphemes = getMorphemes(business);
			for(String noun : morphemes) {
				ArrayList<String> wordnet_list = getWordNetList(noun);
				if(wordnet_list.isEmpty()) {
					wordnet_list.add(noun);
				}
				if(mapping_rule.containsKey(noun)) {
					wordnet_list.add(mapping_rule.get(noun));
				}
				for(Map.Entry<String, String> e : uplod_class_map.entrySet()) {
					String class_name = e.getKey();
					String class_uri = e.getValue();
					for(String wn : wordnet_list) {
						if(class_name.equals(wn) && !list.contains(class_uri)) {
							System.out.println(class_name + " : " + wn);
							list.add(class_uri);
						}
					}
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		System.out.println(getUPLODClassList());
//		System.out.println(getWordNetList("水質汚濁"));
		System.out.println(getLinkableList("土壌汚染・水質汚濁対策事業"));
	}

}
