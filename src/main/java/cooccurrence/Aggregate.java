package cooccurrence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Aggregate {

	public static void main(String[] args) {
		String path = "/Users/Shusaku/Dropbox/research/doctor/共起/20171201/";
		try {
			File dir = new File(path);
			File[] files = dir.listFiles();
			FileReader fr;
			BufferedReader br;
			String problem = "";
			Map<String,Integer> results = new HashMap<String,Integer>();
			for (int i = 0; i < files.length; i++) {
				problem = files[i].getName();
				problem = problem.replaceAll("_20171201.csv", "");
				fr = new FileReader(files[i]);
				br = new BufferedReader(fr);
				String line="";
				String[] array;
				int cnt = 0;
				while ((line = br.readLine()) != null) {
					array = line.split(",");
					if((array[0].equals("原因") || array[0].equals("要因") || array[0].equals("影響") || array[0].equals("都市") || array[0].equals("地域"))
							&& !problem.contains("原因")
							&& !problem.contains("要因")
							&& !problem.contains("影響")
							) {
						cnt += Integer.parseInt(array[1]);
					}
				}
				br.close();
				fr.close();
				results.put(problem, cnt);
			}
			FileWriter fw = new FileWriter("/Users/Shusaku/Dropbox/research/doctor/共起/total_20180113.csv");
			for(Map.Entry<String, Integer> entry : results.entrySet()) {
				fw.write(entry.getKey() + "," + entry.getValue() + "\n");
			}
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
