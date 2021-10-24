package lod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseAnalyzer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer.Mode;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.dict.UserDictionary;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.BaseFormAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.PartOfSpeechAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.ReadingAttribute;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Causality {
	final static String up = "http://www.ohsuga.lab.uec.ac.jp/urbanproblem/vocabulary#";
	final static String upr = "http://www.ohsuga.lab.uec.ac.jp/urbanproblem/resource/";
	final static String prov = "http://www.w3.org/ns/prov#";
	//	final static String[] stop_words = {"拡大","増加","発生","不満",""};
	protected static String prefix = 
			"prefix rdf: <" + RDF.getURI() + ">\n"
					+ "prefix rdfs: <" + RDFS.getURI() + ">\n"
					+ "prefix owl: <" + OWL.NS + ">\n"
					+ "prefix dcterms: <" + DCTerms.NS + ">\n"
					+ "prefix skos: <" + SKOS.uri + ">\n"
					+ "prefix wdt: <http://www.wikidata.org/prop/direct/>\n";
	static Model model = ModelFactory.createDefaultModel();
	static Map<String,String> rules = new HashMap<String,String>();

	static Property alternateOf = (Property) model.createProperty(prov + "alternateOf");
	
	static Property related = (Property) model.createProperty(up + "related");
	//			.addProperty(RDF.type, OWL.TransitiveProperty);
	static Property factor = (Property) model.createProperty(up + "factor")
			.addProperty(RDF.type, OWL.ObjectProperty)
			.addProperty(RDFS.subPropertyOf, related);
	//			.addProperty(OWL.inverseOf, model.getProperty(up + "affect"));
	static Property affect = (Property) model.getProperty(up + "affect")
			.addProperty(RDF.type, OWL.ObjectProperty)
			.addProperty(RDFS.subPropertyOf, related);
	//			.addProperty(OWL.inverseOf, factor);
	static Property factor_level1 = (Property) model.createProperty(up + "factor_level1")
			//			.addProperty(RDF.type, OWL.TransitiveProperty)
			.addProperty(RDFS.subPropertyOf, factor)
			.addProperty(RDFS.label, "factor level1")
			.addProperty(RDFS.comment, "5 <= agreement < 15");
	//			.addProperty(OWL.inverseOf, model.getProperty(up + "affect_level1"));

	static Property factor_level2 = (Property) model.createProperty(up + "factor_level2")
			//			.addProperty(RDF.type, OWL.TransitiveProperty)
			.addProperty(RDFS.subPropertyOf, factor)
			.addProperty(RDFS.label, "factor level2")
			.addProperty(RDFS.comment, "15 <= agreement < 25");
	//			.addProperty(OWL.inverseOf, model.getProperty(up + "affect_level2"));

	static Property factor_level3 = (Property) model.createProperty(up + "factor_level3")
			//			.addProperty(RDF.type, OWL.TransitiveProperty)
			.addProperty(RDFS.subPropertyOf, factor)
			.addProperty(RDFS.label, "factor level3")
			.addProperty(RDFS.comment, "25 <= agreement < 35");
	//			.addProperty(OWL.inverseOf, model.getProperty(up + "affect_level3"));

	static Property factor_level4 = (Property) model.createProperty(up + "factor_level4")
			//			.addProperty(RDF.type, OWL.TransitiveProperty)
			.addProperty(RDFS.subPropertyOf, factor)
			.addProperty(RDFS.label, "factor level4")
			.addProperty(RDFS.comment, "35 <= agreement");
	//			.addProperty(OWL.inverseOf, model.getProperty(up + "affect_level3"));

	static Property affect_level1 = (Property) model.getProperty(up + "affect_level1")
			//			.addProperty(RDF.type, OWL.TransitiveProperty)
			.addProperty(RDFS.subPropertyOf, affect)
			.addProperty(RDFS.label, "affect level1")
			.addProperty(RDFS.comment, "5 <= agreement < 15");
	//			.addProperty(OWL.inverseOf, factor_level1);

	static Property affect_level2 = (Property) model.getProperty(up + "affect_level2")
			//			.addProperty(RDF.type, OWL.TransitiveProperty)
			.addProperty(RDFS.subPropertyOf, affect)
			.addProperty(RDFS.label, "affect level2")
			.addProperty(RDFS.comment, "15 <= agreement < 25");
	//			.addProperty(OWL.inverseOf, factor_level2);

	static Property affect_level3 = (Property) model.getProperty(up + "affect_level3")
			//			.addProperty(RDF.type, OWL.TransitiveProperty)
			.addProperty(RDFS.subPropertyOf, affect)
			.addProperty(RDFS.label, "affect level3")
			.addProperty(RDFS.comment, "25 <= agreement < 35");
	//			.addProperty(OWL.inverseOf, factor_level3);

	static Property affect_level4 = (Property) model.getProperty(up + "affect_level4")
			//			.addProperty(RDF.type, OWL.TransitiveProperty)
			.addProperty(RDFS.subPropertyOf, affect)
			.addProperty(RDFS.label, "affect level4")
			.addProperty(RDFS.comment, "35 <= agreement");
	//			.addProperty(OWL.inverseOf, factor_level4);

	static Resource causalEntity = model.createResource(up + "CausalEntity")
			.addProperty(RDF.type, OWL.Class)
			.addProperty(RDFS.label, model.createLiteral("因果要素","ja"))
			.addProperty(RDFS.label, model.createLiteral("Causal entity", "en"));

	static Resource urbanProblem = model.createResource(up + "UrbanProblem")
			.addProperty(RDF.type, OWL.Class)
			.addProperty(RDFS.label, model.createLiteral("都市問題","ja"))
			.addProperty(RDFS.label, model.createLiteral("Urban problem", "en"))
			.addProperty(RDFS.subClassOf, causalEntity)
			.addProperty(SKOS.broader, causalEntity);
	//	static Resource notUrbanProblem = model.createResource(up + "NotUrbanProblem")
	//			.addProperty(RDF.type, OWL.Class)
	//			.addProperty(RDFS.label, model.createLiteral("非都市問題","ja"))
	//			.addProperty(RDFS.label, model.createLiteral("Not urban problem", "en"));

	public static WikiData getWikiDataHypersFromAlt(String name) {
		WikiData wd = null;
		Map<String,ArrayList<String>> hyperMap = new HashMap<String,ArrayList<String>>();	//key:hyper, value: altHyperList
		ResultSet rs = null;
		Query query = null;
		QueryExecution qexec = null;
		String s = "";
		String sname = "";
		String hyperL = "";
		String hyperAL = "";
		ArrayList<String> altList = new ArrayList<String>();
		ArrayList<String> altHypers = new ArrayList<String>();	//上位概念のaltLabelリスト
		try {
			String sparql = prefix
					+ "SELECT DISTINCT ?s (str(?sn) AS ?sname) (str(?hyperLabel) AS ?hyperL) (str(?hyperAltLabel) AS ?hyperAL) WHERE {\n"
					+ "?s skos:altLabel \"" + name + "\"@ja ;\n"
					+ "rdfs:label ?sn ;\n"
					+ "wdt:P279|wdt:P31 ?hyper .\n"
					+ "?hyper rdfs:label ?hyperLabel .\n"
					+ "filter(LANG(?sn) = \"ja\")\n"
					+ "filter(LANG(?hyperLabel) = \"ja\")\n"
					+ "filter(?hyperLabel != \"" + name + "\"@ja && ?hyperLabel != \"ウィキメディアの曖昧さ回避ページ\"@ja && ?hyperLabel != \"ウィキメディアの一覧記事\"@ja && ?hyperLabel != \"ウィクショナリーへのリダイレクト\"@ja)"
					+ "OPTIONAL {"
					+ "?hyper skos:altLabel ?hyperAltLabel .\n"
					+ "filter(LANG(?hyperAltLabel) = \"ja\")\n"
					+ "}"
					+ "}";
			query = QueryFactory.create(sparql);
			qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", query);
			rs = qexec.execSelect();
			QuerySolution qs;
			while(rs.hasNext()) {
				qs = rs.nextSolution();
				s = qs.get("s").toString();
				sname = qs.get("sname").toString();
				hyperL = qs.get("hyperL").toString();
				if(qs.get("hyperAL")!= null) {
					hyperAL = qs.get("hyperAL").toString();
				} else {
					hyperAL = null;
				}

				if(!hyperMap.containsKey(hyperL)) {	//hyper初出
					System.out.println(name + ", wikidata_hyper: " + hyperL);
					altHypers = new ArrayList<String>();
					if(hyperAL != null) {	//hyperにaltLabelがある
						altHypers.add(hyperAL);
					}
					hyperMap.put(hyperL, altHypers);
				} else {	//hyper既出
					altHypers = hyperMap.get(hyperL);
					if(hyperAL != null) {	//hyperにaltLabelがある
						altHypers.add(hyperAL);
					}
					hyperMap.put(hyperL, altHypers);
				}
			}
			wd = new WikiData(s, sname, altList, hyperMap);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return wd;
	}
	
	//altとhyperとhyperのalt取得
	public static WikiData getWikiDataHypers(String name) {
		WikiData wd = null;
		Map<String,ArrayList<String>> hyperMap = new HashMap<String,ArrayList<String>>();	//key:hyper, value: altHyperList
		ResultSet rs = null;
		Query query = null;
		QueryExecution qexec = null;
		String s = "";
		String alt = "";
		String hyperL = "";
		String hyperAL = "";
		ArrayList<String> altList = new ArrayList<String>();
		ArrayList<String> altHypers = new ArrayList<String>();	//上位概念のaltLabelリスト
		try {
			String sparql = prefix
					+ "SELECT DISTINCT ?s (str(?salt) AS ?alt) (str(?hyperLabel) AS ?hyperL) (str(?hyperAltLabel) AS ?hyperAL) WHERE {\n"
					+ "?s rdfs:label \"" + name + "\"@ja ;\n"
					+ "wdt:P279|wdt:P31 ?hyper .\n"
					+ "OPTIONAL {"
					+ "?s skos:altLabel ?salt .\n"
					+ "filter(LANG(?salt) = \"ja\")\n"
					+ "}"
					+ "?hyper rdfs:label ?hyperLabel .\n"
					+ "filter(LANG(?hyperLabel) = \"ja\")\n"
					+ "filter(?hyperLabel != \"" + name + "\"@ja && ?hyperLabel != \"ウィキメディアの曖昧さ回避ページ\"@ja && ?hyperLabel != \"ウィキメディアの一覧記事\"@ja && ?hyperLabel != \"ウィクショナリーへのリダイレクト\"@ja)"
					+ "OPTIONAL {"
					+ "?hyper skos:altLabel ?hyperAltLabel .\n"
					+ "filter(LANG(?hyperAltLabel) = \"ja\")\n"
					+ "}"
					+ "}";
			query = QueryFactory.create(sparql);
			qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", query);
			rs = qexec.execSelect();
			QuerySolution qs;
			while(rs.hasNext()) {
				qs = rs.nextSolution();
				s = qs.get("s").toString();
				if(qs.get("alt") != null) {
					alt = qs.get("alt").toString();
				} else {
					alt = null;
				}
				hyperL = qs.get("hyperL").toString();
				if(qs.get("hyperAL")!= null) {
					hyperAL = qs.get("hyperAL").toString();
				} else {
					hyperAL = null;
				}
				if(!altList.contains(alt) && alt != null) {	//alt初出
					altList.add(alt);
				}

				if(!hyperMap.containsKey(hyperL)) {	//hyper初出
					System.out.println(name + ", wikidata_hyper: " + hyperL);
					altHypers = new ArrayList<String>();
					if(hyperAL != null) {	//hyperにaltLabelがある
						altHypers.add(hyperAL);
					}
					hyperMap.put(hyperL, altHypers);
				} else {	//hyper既出
					altHypers = hyperMap.get(hyperL);
					if(hyperAL != null) {	//hyperにaltLabelがある
						altHypers.add(hyperAL);
					}
					hyperMap.put(hyperL, altHypers);
				}
				System.out.println(name + ", wikidata_althyper: " + alt);
			}
			wd = new WikiData(s, altList, hyperMap);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return wd;
	}

	public static ArrayList<String> getHypers(String word) {
		ArrayList<String> results = new ArrayList<String>();

		try {
			UserDictionary userDict = null;
			Mode mode = JapaneseTokenizer.Mode.NORMAL;
			CharArraySet stopSet = JapaneseAnalyzer.getDefaultStopSet();
			Set<String> stopTags = JapaneseAnalyzer.getDefaultStopTags();

			JapaneseTokenizer analyzer = new JapaneseTokenizer(userDict, false, mode);

			Reader reader = new StringReader(word);
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

				if(partOfSpeech.contains("一般")) {
					if(word.indexOf(text) == 0 && (text.length() != 1)) {	//接頭辞
						results.add(text);
					} else if((word.lastIndexOf(text) == (word.length() - text.length())) && (text.length() != 1)) {	//接尾時
						results.add(text);
					}
				}
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	public static String translate(String sourceText) {
		String en = "";
		String google = "https://translation.googleapis.com/language/translate/v2";
		String apikey = "AIzaSyBXFwKg4yfjjzh66Gjah-PF3C5iH92j--U";
		try {

			URL url = new URL(google + "?key=" + apikey + "&source=ja&target=en&q=" + sourceText);
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
			JsonNode translatedText = rootNode.findValue("translatedText");
			en = translatedText.asText();
			en = StringUtils.capitalize(en);
			//			System.out.println(en);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return en;
	}

	public static Resource createCausalResource(String name, String name_en) {
		Resource r = model.getResource(upr + name_en.replaceAll(" ", "_"));
		if(!model.contains(r,RDFS.label)) {	//既にmodelに追加されていない
			r.addProperty(RDFS.label, model.createLiteral(name, "ja"));
			r.addProperty(RDFS.label, model.createLiteral(name_en, "en"));
			r.addProperty(RDF.type, OWL.Class);
			if(rules.containsKey(name)) {	//都市問題である
				r.addProperty(SKOS.broader, urbanProblem);
				r.addProperty(RDFS.subClassOf, urbanProblem);
			} else {	//非都市問題
				r.addProperty(SKOS.broader, causalEntity);
				r.addProperty(RDFS.subClassOf, causalEntity);

				//非都市問題の場合は上位クラスを作成・リンク付け
				WikiData wd = null;
				wd = getWikiDataHypers(name);	//nameに関するwikidataでのalt, hyper, hyperのaltを取得
				WikiData wda = null;
				wda = getWikiDataHypersFromAlt(name);
				if(wd != null) {	//WikiDataから結果取得可能
					String s = wd.getS();
					ArrayList<String> altList = wd.getAltList();
					Map<String,ArrayList<String>> hyperMap = wd.getHyperMap();
					r.addProperty(RDFS.seeAlso, model.createResource(s));	//WikiDataリソースへのリンク付け
					for(String alt : altList) {
						String alt_en = translate(alt);
						alt_en = alt_en.replaceAll(" ", "_");
						Resource alt_r = model.getResource(upr + alt_en);
						if(!model.contains(alt_r, RDF.type, OWL.Class)) {
							alt_r.addProperty(RDF.type, OWL.Class);
							alt_r.addProperty(RDFS.label, model.createLiteral(alt, "ja"));
							alt_r.addProperty(RDFS.label, model.createLiteral(alt_en, "en"));
							alt_r.addProperty(RDFS.subClassOf, causalEntity);
							alt_r.addProperty(SKOS.broader, causalEntity);
						}
						r.addProperty(alternateOf, alt_r);
						alt_r.addProperty(alternateOf, r);
					}
					String hyper = "";
					String hyper_en = "";
					String hyper_alt_en = "";
					ArrayList<String> hyperAltList = new ArrayList<String>();
					Resource hyper_r = null;
					for(Map.Entry<String, ArrayList<String>> e : hyperMap.entrySet()) {
						hyper = e.getKey();	//上位概念 日本語
						hyper_en = translate(hyper);	//上位概念 英語
						hyperAltList = e.getValue();	//上位概念のaltLabelリスト 日本語
						
						/* hyper追加 */
						hyper_en = hyper_en.replaceAll(" ", "_");
						hyper_r = model.getResource(upr + hyper_en);
						if(rules.containsKey(hyper)) {
							/* rulesにあるということは都市問題 */
							r.addProperty(SKOS.broader, hyper_r);
							r.addProperty(RDFS.subClassOf, hyper_r);
						} else {
							//hyperについて既にRDFグラフ追加済みかどうか
							if(!model.contains(hyper_r, RDF.type, OWL.Class)) {
								hyper_r.addProperty(RDF.type, OWL.Class);
								hyper_r.addProperty(RDFS.subClassOf, causalEntity);
								hyper_r.addProperty(SKOS.broader, causalEntity);
								hyper_r.addProperty(RDFS.label, model.createLiteral(hyper_en, "en"));
								hyper_r.addProperty(RDFS.label, model.createLiteral(hyper, "ja"));
							}
							r.addProperty(SKOS.broader, hyper_r);
							r.addProperty(RDFS.subClassOf, hyper_r);
						}
						
						/* hyperAlt追加 */
						if(hyperAltList.size() > 0) {
							//hyperのaltがある
							for(String ha : hyperAltList) {
								hyper_alt_en = translate(ha);
								hyper_alt_en = hyper_alt_en.replaceAll(" ", "_");
								if(hyper_alt_en == "") {
									System.out.println("翻訳エラー: " + ha);
									continue;
								}
								Resource hyperAlt_r = model.getResource(upr + hyper_alt_en);
								//hyperAltについて既にRDFグラフ追加済みかどうか
								if(!model.contains(hyperAlt_r, RDF.type, OWL.Class)) {
									hyperAlt_r.addProperty(RDF.type, OWL.Class);
									hyperAlt_r.addProperty(RDFS.subClassOf, causalEntity);
									hyperAlt_r.addProperty(SKOS.broader, causalEntity);
									hyperAlt_r.addProperty(RDFS.label, model.createLiteral(hyper_alt_en, "en"));
									hyperAlt_r.addProperty(RDFS.label, model.createLiteral(ha, "ja"));
								}
								hyper_r.addProperty(alternateOf, hyperAlt_r);
								hyperAlt_r.addProperty(alternateOf, hyper_r);
							}
						}
					}

				} else if(wda != null) {
					String s = wda.getS();
					String sname = wda.getSname();
					String sname_en = translate(sname);
					Map<String,ArrayList<String>> hyperMap = wda.getHyperMap();
					Resource wd_r = model.getResource(upr + sname);
					if(!model.contains(wd_r, RDF.type, OWL.Class)) {	//modelに追加済みでない
						wd_r.addProperty(RDF.type, OWL.Class);
						wd_r.addProperty(RDFS.subClassOf, causalEntity);
						wd_r.addProperty(SKOS.broader, causalEntity);
						wd_r.addProperty(RDFS.label, model.createLiteral(sname, "ja"));
						wd_r.addProperty(RDFS.label, model.createLiteral(sname_en, "en"));
						wd_r.addProperty(RDFS.seeAlso, model.getResource(s));
					}
					r.addProperty(alternateOf, wd_r);
					wd_r.addProperty(alternateOf, r);
					
					String hyper = "";
					String hyper_en = "";
					String hyper_alt_en = "";
					ArrayList<String> hyperAltList = new ArrayList<String>();
					Resource hyper_r = null;
					for(Map.Entry<String, ArrayList<String>> e : hyperMap.entrySet()) {
						hyper = e.getKey();	//上位概念 日本語
						hyper_en = translate(hyper);	//上位概念 英語
						hyperAltList = e.getValue();	//上位概念のaltLabelリスト 日本語
						
						/* hyper追加 */
						hyper_en = hyper_en.replaceAll(" ", "_");
						hyper_r = model.getResource(upr + hyper_en);
						if(rules.containsKey(hyper)) {
							/* rulesにあるということは都市問題 */
							r.addProperty(SKOS.broader, hyper_r);
							r.addProperty(RDFS.subClassOf, hyper_r);
						} else {
							//hyperについて既にRDFグラフ追加済みかどうか
							if(!model.contains(hyper_r, RDF.type, OWL.Class)) {
								hyper_r.addProperty(RDF.type, OWL.Class);
								hyper_r.addProperty(RDFS.subClassOf, model.getResource(upr + "CausalEntity"));
								hyper_r.addProperty(SKOS.broader, model.getResource(upr + "CausalEntity"));
								hyper_r.addProperty(RDFS.label, model.createLiteral(hyper_en, "en"));
								hyper_r.addProperty(RDFS.label, model.createLiteral(hyper, "ja"));
							}
							r.addProperty(SKOS.broader, hyper_r);
							r.addProperty(RDFS.subClassOf, hyper_r);
						}
						
						/* hyperAlt追加 */
						if(hyperAltList.size() > 0) {
							//hyperのaltがある
							for(String ha : hyperAltList) {
								hyper_alt_en = translate(ha);
								hyper_alt_en = hyper_alt_en.replaceAll(" ", "_");
								if(hyper_alt_en == "") {
									System.out.println("翻訳エラー: " + ha);
									continue;
								}
								Resource hyperAlt_r = model.getResource(upr + hyper_alt_en);
								//hyperAltについて既にRDFグラフ追加済みかどうか
								if(!model.contains(hyperAlt_r, RDF.type, OWL.Class)) {
									hyperAlt_r.addProperty(RDF.type, OWL.Class);
									hyperAlt_r.addProperty(RDFS.subClassOf, model.getResource(upr + "CausalEntity"));
									hyperAlt_r.addProperty(SKOS.broader, model.getResource(upr + "CausalEntity"));
									hyperAlt_r.addProperty(RDFS.label, model.createLiteral(hyper_alt_en, "en"));
									hyperAlt_r.addProperty(RDFS.label, model.createLiteral(ha, "ja"));
								}
								hyper_r.addProperty(alternateOf, hyperAlt_r);
								hyperAlt_r.addProperty(alternateOf, hyper_r);
							}
						}
					}
				} else { //WikiDataで結果取得できない
					ArrayList<String> hyperList = new ArrayList<String>();
					hyperList = getHypers(name);
					if(hyperList.size() > 0) {	//hyper有り
						if(hyperList.get(0).equals(name)) {return r;}	//形態素解析分割できてない
						for(String hyper : hyperList) {
							String hyper_en = "";
							if(rules.containsKey(hyper)) {
								/* rulesにあるということは都市問題
								 * 非都市問題の上位クラスが都市問題では矛盾してしまう
								 * 代わりにrelatedでリンクさせる
								 *  */
								hyper_en = rules.get(hyper);
								Resource hyper_r = model.getResource(upr + hyper_en.replaceAll(" ", "_"));
								r.addProperty(related, hyper_r);
							} else {
								/* rulesに無いということは非都市問題 */
								hyper_en = translate(hyper);
								Resource hyper_r = model.getResource(upr + hyper_en.replaceAll(" ", "_"));
								if(!model.contains(hyper_r,RDFS.label)) {
									hyper_r.addProperty(RDFS.label, model.createLiteral(hyper, "ja"));
									hyper_r.addProperty(RDFS.label, model.createLiteral(hyper_en, "en"));
									hyper_r.addProperty(RDF.type, OWL.Class);

									hyper_r.addProperty(SKOS.broader, causalEntity);
									hyper_r.addProperty(RDFS.subClassOf, causalEntity);
								}
								r.addProperty(SKOS.broader, hyper_r);
								r.addProperty(RDFS.subClassOf, hyper_r);
							}

						}
					}
				}
			}
		}
		return r;
	}

	public static void createRDF(String issue, Map<String,Integer> countMap, String type) {
		try {
			/* 都市問題取得 */
			String issue_en = rules.get(issue);
			Resource issue_r = model.getResource(upr + issue_en.replaceAll(" ", "_"));

			/* 因果関係構築 */
			for(Map.Entry<String, Integer> e : countMap.entrySet()) {
				String name = e.getKey();
				String name_en = "";
				int agreement = e.getValue();
				if((agreement >= 5 && !issue.equals("放置自転車")) || (agreement >= 4 && issue.equals("放置自転車"))) {
					/* 因果リソースの英語名を取得 */

					if(rules.containsKey(name)) {
						name_en = rules.get(name);
					} else {
						name_en = translate(name);
					}

					//因果リソース作成
					Resource causal_r = createCausalResource(name, name_en);
					if(type.equals("factor")) {
						/* 要因 */
						if(agreement >= 5 && agreement < 15) {
							issue_r.addProperty(factor_level1, causal_r);
							causal_r.addProperty(affect_level1, issue_r);
						} else if(agreement >= 15 && agreement < 25) {
							issue_r.addProperty(factor_level2, causal_r);
							causal_r.addProperty(affect_level2, issue_r);
						} else if(agreement >= 25 && agreement < 35) {
							issue_r.addProperty(factor_level3, causal_r);
							causal_r.addProperty(affect_level3, issue_r);
						} else if(agreement >=35) {
							issue_r.addProperty(factor_level4, causal_r);
							causal_r.addProperty(affect_level4, issue_r);
						}
					} else if(type.equals("cause")) {
						/* 影響 */
						if(agreement >= 5 && agreement < 15) {
							issue_r.addProperty(affect_level1, causal_r);
							causal_r.addProperty(factor_level1, issue_r);
						} else if(agreement >= 15 && agreement < 25) {
							issue_r.addProperty(affect_level2, causal_r);
							causal_r.addProperty(factor_level2, issue_r);
						} else if(agreement >= 25 && agreement < 35) {
							issue_r.addProperty(affect_level3, causal_r);
							causal_r.addProperty(factor_level3, issue_r);
						} else if(agreement >=35) {
							issue_r.addProperty(affect_level4, causal_r);
							causal_r.addProperty(factor_level4, issue_r);
						}
					} else {
						System.out.println("typeが変");
						System.exit(0);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static Map<String,Integer> getCountMap(String path, String issue, String type) {
		Map<String,Integer> map = new HashMap<String,Integer>();
		try {
			FileReader fr = new FileReader(path + issue + "_" + type + "_cs_results.csv");
			BufferedReader br = new BufferedReader(fr);
			String line="";
			String[] array = null;
			while((line = br.readLine()) != null) {
				array = line.split(",");
				if(array[0].equals("名前")) { continue;}
				int sum = 0;
				for(int i=1; i<array.length; i++) {
					sum += Integer.parseInt(array[i]);
				}
				map.put(array[0], sum);
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return map;
	}

	public static void main(String[] args) {
		String path = "/Users/Shusaku/Dropbox/research/doctor/crowdsourcing/";
		String[] issue = {"放置自転車","騒音","交通事故","景観破壊","渋滞","シャッター通り","郊外型犯罪","郊外化","ホームレス","不就学","ポイ捨て","貧困","貧困ビジネス","治安悪化","ネットカフェ難民","落書き","ラッシュ時"};
		String[] issue_en = {"Illegally parked bicycles", "Noise", "Traffic accident", "Landscape destruction", "Traffic jam", "Dead shopping street", "Suburban crime", "Suburbanization", "Homeless", "Truancy", "Littering", "Poverty", "Poverty business", "Deterioration of security","Netcafe refugee","Graffiti","Rush hour"};
		String[] causal_entities = {"賃金格差", "家庭内暴力"};
		String[] causal_eintities_en = {"Wage gap", "Domestic violence"};
		//		String[] issue = {"不就学"};
		//		String[] issue_en = {"Fushugaku"};
		String type[] = {"factor","cause"};
		//		ArrayList<String[]> csv = readLancersCSV(path + issue + ".csv");

		for(int i=0; i<issue.length; i++) {
			rules.put(issue[i], issue_en[i]);
			/* 都市問題を先に作成 */
			Resource issue_r = model.getResource(upr + issue_en[i].replaceAll(" ", "_"));
			issue_r.addProperty(RDFS.label, model.createLiteral(issue[i],"ja"));
			issue_r.addProperty(RDFS.label, model.createLiteral(issue_en[i],"en"));
			issue_r.addProperty(RDF.type, OWL.Class);
			issue_r.addProperty(SKOS.broader, urbanProblem);
			issue_r.addProperty(RDFS.subClassOf, urbanProblem);
		}
		//		
		for(int i=0; i<issue.length; i++) {
			for(int j=0; j<type.length; j++) {
				System.out.println(issue[i] + ": " + type[j]);
				Map<String,Integer> countMap = getCountMap(path, issue[i], type[j]);
				createRDF(issue[i], countMap, type[j]);
			}
		}

		try {
			FileOutputStream fout = new FileOutputStream(new File(path + "urbanproblem_wikidata_hyperalt_20171211.ttl"));
			model.setNsPrefix("up", up);
			model.setNsPrefix("upr", upr);
			model.setNsPrefix("rdf", RDF.getURI());
			model.setNsPrefix("rdfs", RDFS.getURI());
			model.setNsPrefix("owl", OWL.getURI());
			model.setNsPrefix("skos", SKOS.getURI());
			model.write(fout,"TTL");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
