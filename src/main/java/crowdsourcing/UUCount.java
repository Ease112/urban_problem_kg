package crowdsourcing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;

public class  UUCount {
	
	static Map<String,Integer> map = new HashMap<String,Integer>();

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
	
	public static int countnewU(File file) {
		int newu = 0;
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line="";
			while((line = br.readLine()) != null) {
				if(line.contains("lancersTaskId")) {
					continue;
				}
				String[] arr = line.split(",");
				String name = arr[2];
				if(map.containsKey(name)) {
					int tmp = map.get(name);
					tmp++;
					map.put(name, tmp);
				} else {
					map.put(name, 1);
					newu++;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return newu;
	}
	
	public static void main(String[] args) {
		String path = "/Users/Shusaku/Dropbox/research/doctor/IJSCAI_experiment/";
//		ArrayList<String[]> csv = readLancersCSV(path + issue + ".csv");
		
	    File dir = new File(path);
	    File[] files = dir.listFiles();
	    for (int i = 0; i < files.length; i++) {
	        File file = files[i];
	        int newu = countnewU(file);
	        System.out.println(file);
	        System.out.println("newU: " + newu);
	        System.out.println("Repeat: " + (50 - newu));
	    }
	    int repeat = 0;
	    for(Map.Entry<String, Integer> e : map.entrySet()) {
	    	if(e.getValue() > 1) {
	    		repeat++;
	    	}
	    }
	    System.out.println("repeater: " + repeat);
	}

}
