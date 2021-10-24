package construct;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseAnalyzer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer.Mode;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.BaseFormAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.PartOfSpeechAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.ReadingAttribute;

public class CreateClass {
	
	private static ArrayList<String> getMorphemes(String keyword) {
		ArrayList<String> morphemes = new ArrayList<String>();
		try {
			Mode mode = JapaneseTokenizer.Mode.NORMAL;
			CharArraySet stopSet = JapaneseAnalyzer.getDefaultStopSet();
			Set<String> stopTags = JapaneseAnalyzer.getDefaultStopTags();

			JapaneseTokenizer tokenizer = new JapaneseTokenizer(null, false, mode);

			Reader reader = new StringReader(keyword);
			tokenizer.setReader(reader);

			BaseFormAttribute baseAttr = tokenizer.addAttribute(BaseFormAttribute.class);
			CharTermAttribute charAttr = tokenizer.addAttribute(CharTermAttribute.class);
			PartOfSpeechAttribute posAttr = tokenizer.addAttribute(PartOfSpeechAttribute.class);
			ReadingAttribute readAttr = tokenizer.addAttribute(ReadingAttribute.class);

			tokenizer.reset();
			while (tokenizer.incrementToken()) {
				String text = charAttr.toString();                // 単語
//				String baseForm = baseAttr.getBaseForm();       // 原型
//				String reading = readAttr.getReading();         // 読み
				String partOfSpeech = posAttr.getPartOfSpeech();    // 品詞
				
				if(partOfSpeech.contains("名詞") && !partOfSpeech.equals("名詞-接尾-一般")) {
					morphemes.add(text);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return morphemes;
	}
	
	private static String[] getHypers(String iri) {
		String[] hypers = new String[2];
		try {
			String query = "SELECT * WHERE {<" + iri + "> skos:broader ?o ; rdfs:label ?label . }";
			System.out.println(query);
			String api_url = "https://stirdf.jglobal.jst.go.jp/sparql?lang=jpn&lu=" + "" + "&format=csv&query=" + URLEncoder.encode(query, "UTF-8");
			
			SSLSocketFactory factory = null;
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null, new NonAuthentication[] { new NonAuthentication() },
					null);
			factory = ctx.getSocketFactory();
			URL url = new URL(api_url);
			HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
			https.setSSLSocketFactory(factory);
			InputStream is = https.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line;
			while (null != (line = reader.readLine())) {
//				System.out.println(line);
				if(!line.equals("\"o\",\"label\"") && !line.equals("")) {
					hypers = line.split(",");
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return hypers;
	}

	private static String[] getTerm(String term) {
		String[] terms = new String[2];
		try {
			String query = "SELECT * WHERE {?s xl:altLabel/xl:literalForm '" + term + "' . }";
			String api_url = "https://stirdf.jglobal.jst.go.jp/sparql?lang=jpn&lu=" + "" + "&format=csv&query=" + URLEncoder.encode(query, "UTF-8");
//			System.out.println(api_url);
			
			SSLSocketFactory factory = null;
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null, new NonAuthentication[] { new NonAuthentication() },
					null);
			factory = ctx.getSocketFactory();
			URL url = new URL(api_url);
			HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
			https.setSSLSocketFactory(factory);
			InputStream is = https.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line;
			while (null != (line = reader.readLine())) {
				if(!line.equals("\"o\",\"label\"") && !line.equals("")) {
					terms = line.split(",");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return terms;
	}
	
	public static void main(String[] args) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("src/main/java/construct/keywords.txt"),"UTF-8"));
			
			String keyword = "";
			
			ArrayList<String> notHyperList = new ArrayList<String>();
			Map<String,String> hyperMap = new HashMap<String,String>();
			
			while((keyword = br.readLine()) != null) {
				System.out.println(keyword);
				String[] term = getTerm(keyword);	//[IRI, Label]
				
				if(!term[0].equals("")) {
					//キーワードがJGlobal Knowledgeにある
					
					String[] hyper = getHypers(term[0]);	//[IRI, label]
					
					if(hyper[0] == null) {
						//JGlobal Knowledgeに有り，かつbroaderが無い = keywordでword2vec行き
						System.out.println("Broader無し");
						notHyperList.add(keyword);
						hyperMap.put(keyword, keyword);
					} else {
						//JGlobal Knowledgeに有り，かつbroaderがある = 最上位概念取得からのword2vec
						System.out.println("Broader有り");
					}
				} else {
					//キーワードがJGlobal Knowledgeに無い
					System.out.println("none");
					ArrayList<String> morphemes = new ArrayList<String>();
					morphemes = getMorphemes(keyword);
					if(morphemes.size() > 1) {
						for(int i=0; i<morphemes.size(); i++) {
							String hyperName = morphemes.get(morphemes.size()-1);
							
						}
					} else {
						//JGlobal Knowledgeに無く，かつ形態素解析もできない = keywordでword2vec行き
						
					}
				}
				Thread.sleep(1000);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}

class NonAuthentication implements X509TrustManager {
	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}
}