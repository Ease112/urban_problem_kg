package search;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Cabocha {
	public static Map<String,String[]> executeCabocha (String sentence) {
		String cabochaPath = "/usr/local/bin/cabocha -f3 -d /usr/local/mecab/lib/mecab/dic/mecab-ipadic-neologd/";
		Map<String,String[]> chunk_map = new HashMap<String,String[]>();
		try {
			//cabochaの起動
			Process process = Runtime.getRuntime().exec(cabochaPath);
			OutputStream out= process.getOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(out, "utf-8");
			writer.write(sentence);
			writer.close();
			InputStream is = process.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
				System.out.println(line);
			}
			process.destroy();
			process.waitFor();
			
			StringBuilder chunk_str;
			String chunk_id = "";
			String link_id = "";
			Document document = Jsoup.parse(sb.toString());
			Elements elements = document.getAllElements();
			Elements chunks = elements.get(0).getElementsByTag("chunk");
			for(Element chunk : chunks) {
				chunk_id = chunk.id();
				link_id = chunk.attr("link");
				Elements tokens = chunk.getElementsByTag("tok");
				chunk_str = new StringBuilder();
				for(Element token : tokens) {
					chunk_str.append(token.text());
				}
				chunk_map.put(chunk_id, new String[]{chunk_str.toString(),link_id});
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return chunk_map;
	}
	
	public static ArrayList<String> getPrevious(Map<String,String[]> chunk_map, ArrayList<String> chunks, String id) {
		try {
			for(Map.Entry<String, String[]> e : chunk_map.entrySet()) {
				String[] value = e.getValue();
				String key = "";
				if(value[1].equals(id)) {//idに係っているチャンク
					key = e.getKey();
					chunks.add(value[0]);
					chunks = getPrevious(chunk_map, chunks, key);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return chunks;
	}
	
	public static ArrayList<String> getNext(Map<String,String[]> chunk_map, ArrayList<String> chunks, String link) {
		try {
			for(Map.Entry<String, String[]> e : chunk_map.entrySet()) {
				String[] value = e.getValue();
				String key = "";
				if(e.getKey().equals(link)) {//係り先
					key =  value[1];
					chunks.add(value[0]);
					getNext(chunk_map, chunks, key);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return chunks;
	}
	
	public static ArrayList<String> getChunks(Map<String,String[]> chunk_map, String keyword) { 
		ArrayList<String> chunks = new ArrayList<String>();
		try {
			String id = "";
			String link = "";
			for(Map.Entry<String, String[]> e : chunk_map.entrySet()) {
				String[] value = e.getValue();
				if(value[0].contains(keyword)) {
					//条件一致してもループを止めないことで、複数一致した場合後者を優先する仕様
					id = e.getKey();
					link = value[1];
					
				}
			}
			for(Map.Entry<String, String[]> e : chunk_map.entrySet()) {
				String[] value = e.getValue();
				String key = "";
				if(value[1].equals(id)) {//idに係っているチャンク
//					key = e.getKey();
					chunks.add(value[0]);
//					chunks = getPrevious(chunk_map, chunks, key);
				}
				if(e.getKey().equals(link)) {//係り先
//					key =  value[1];
					chunks.add(value[0]);
//					getNext(chunk_map, chunks, key);
				}
			}
//			chunks = getPrevious(chunk_map, chunks, id);
//			chunks = getNext(chunk_map, chunks, link);
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("chunks:" + chunks);
		return chunks;
	}
	
	public static void main(String[] args) {
		Map<String, String[]> cabocha_map = executeCabocha("竹原駅前商店街は、駐車場不足などが原因で、閉店が目立ち始めていた");
		getChunks(cabocha_map, "原因");
	}
}
