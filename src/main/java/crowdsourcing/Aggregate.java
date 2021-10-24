package crowdsourcing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;

public class Aggregate {

	private static ArrayList<String[]> readLancersCSV(String path) {
		ArrayList<String[]> csv = new ArrayList<String[]>();
		try {
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			String line="";
			String[] array;
			while ((line = br.readLine()) != null) {
				array = line.split(",");
				csv.add(array);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return csv;
	}
	
	public static void main(String[] args) {
		String path = "/Users/Shusaku/Dropbox/research/doctor/crowdsourcing/";
		String issue = "家庭内暴力";
		String issue_en = "domestic_violence";
		String type = "cause";
		String type_ja = "";
		ArrayList<String[]> csv = readLancersCSV(path + issue + ".csv");
		
		if(args[1].equals("factor")) {
			type_ja = "原因";
		} else {
			type_ja = "影響";
		}
		try {
			FileReader fr = new FileReader(path + issue + "_" + type + "_wordcloud_jaro_080_cabocha_delete_weight0_bing_decrypt.csv");
			BufferedReader br = new BufferedReader(fr);
			String line="";
			String[] wordcloud;
			List<String> words = new ArrayList<String>();
			line = br.readLine();
			wordcloud = line.split(",");
			words = Arrays.asList(wordcloud);
			
			FileWriter fw = new FileWriter("/Users/Shusaku/Dropbox/research/doctor/crowdsourcing/" + issue + "_" + type + "_cs_results.csv");
			BufferedWriter bw = new BufferedWriter(fw);
			ArrayList<Worker> workers = new ArrayList<Worker>();
			for(int i=1; i<csv.size(); i++) {
				String[] row = csv.get(i);
				String name = row[2];
				if(row[23].equals("☓")) { continue; }
				ArrayList<String> factor = new ArrayList<String>();
				ArrayList<String> cause = new ArrayList<String>();
				for(int j=0; j<10; j++) {
					factor.add(row[j+3]);
					cause.add(row[j+13]);
				}
				
				Worker worker = new Worker(name, i, factor, cause);
				workers.add(worker);
			}
			
			Map<String,ArrayList<Integer>> words_map = new HashMap<String,ArrayList<Integer>>();
			
			String first = "名前,";
			for(String word : words) {
				ArrayList<Integer> worker_id_list = new ArrayList<Integer>();
				for(Worker worker : workers) {
					ArrayList<String> factor = worker.getFactor();
					ArrayList<String> cause = worker.getCause();
					int id = worker.getId();
					if(type.contains("factor")) {
						if(factor.contains(word)) {
							worker_id_list.add(id);
						}
					} else {
						if(cause.contains(word)) {
							worker_id_list.add(id);
						}
					}
					
				}
				words_map.put(word, worker_id_list);
//				System.out.println(word + ": " + worker_id_list);
			}
			
			//最初の行を作っておく
			for(Worker worker : workers) { first += worker.getName() + ","; }
//			System.out.println(first.substring(0, first.length()-1) + "\n");
			
			//最初の行書き出し
			bw.write(first.substring(0, first.length()-1) + "\n");
			for(Map.Entry<String, ArrayList<Integer>> entry : words_map.entrySet()){
				String output = entry.getKey() + ",";
				ArrayList<Integer> id_list = entry.getValue();
				for(int i=0; i<workers.size(); i++) {
					if(id_list.contains(i)) {
						output += "1,";
					} else {
						output += "0,";
					}
				}
				bw.write(output.substring(0, output.length()-1) + "\n");
			}
			bw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
