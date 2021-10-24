package search;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseAnalyzer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.JapaneseTokenizer.Mode;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.dict.UserDictionary;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.BaseFormAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.PartOfSpeechAttribute;
import org.codelibs.neologd.ipadic.lucene.analysis.ja.tokenattributes.ReadingAttribute;


import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.atilika.kuromoji.ipadic.Tokenizer.Builder;

public class KuromojiTest {
	private static void createDictionary() {
		String filePath = "jawiki-latest-all-titles-in-ns0";

		BufferedReader br = null;
		OutputStreamWriter osw = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),"UTF-8"));
			osw = new OutputStreamWriter(new FileOutputStream("user_dic.csv"),"UTF-8");

			String line;

			while ((line = br.readLine()) != null) {
				line = line.replaceAll(",", "_");
				line = line.replaceAll("\"", "");
				if(line.length() > 3) osw.write(line + "," + line + "," + line + ",名詞\r\n");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				osw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {

		try {
			//createDictionary();

			UserDictionary userDict = null;
			Mode mode = JapaneseTokenizer.Mode.NORMAL;
			CharArraySet stopSet = JapaneseAnalyzer.getDefaultStopSet();
			Set<String> stopTags = JapaneseAnalyzer.getDefaultStopTags();

			JapaneseTokenizer analyzer = new JapaneseTokenizer(userDict, false, mode);

			Reader reader = new StringReader("騒音暴露");
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

				System.out.println(text + "\t|\t" + baseForm + "\t|\t" + reading + "\t|\t" + partOfSpeech);
			}

			//Builder builder = new Tokenizer.Builder();
			//builder.userDictionary("user_dic.csv");
			//Tokenizer tokenizer = builder.build();
			//List<Token> tokens = tokenizer.tokenize("また、監視カメラや場内の明るさ、清潔さ、営業時間の長さなどのソフト面の要素も重要な要素である");
//			String noun = "";
//			for (int i=0; i<tokens.size(); i++) {
//				/*
//				if(tokens.get(i).getPartOfSpeechLevel1().equals("名詞") && !tokens.get(i).getPartOfSpeechLevel2().equals("非自立")) {
//					if((tokens.get(i).getPartOfSpeechLevel2().equals("サ変接続") || tokens.get(i).getPartOfSpeechLevel2().equals("接尾")) && i>0) {
//						if(tokens.get(i-1).getPartOfSpeechLevel1().equals("名詞")) {
//							System.out.print(noun);
//							noun = noun + tokens.get(i).getSurface();
//						}
//					} else {
//						noun = tokens.get(i).getSurface();
//					}
//					System.out.println(tokens.get(i).getSurface() + ": " + tokens.get(i).getPartOfSpeechLevel2());
//				}
//				 */
//				System.out.println(tokens.get(i).getSurface() + ": " + tokens.get(i).getPartOfSpeechLevel2());
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
