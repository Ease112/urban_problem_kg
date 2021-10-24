package ViciousCycle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EvaluateSingleViciousCycle {

	static HashSet<String> correctSet = new HashSet<String>();
	static String path = "/Users/Shusaku/Dropbox/research/doctor/";
	
	private static void readCorrectSet() {
		try {
			FileReader fr = new FileReader(path + "専門家評価.csv");
			BufferedReader br = new BufferedReader(fr);
			String line="";
			String[] array = null;
			
			while((line = br.readLine()) != null) {
				array = line.split(",");
				for(int i=0; i<array.length; i++) {
					array[i] = array[i].trim();
				}
				if(array[2].equals("1") || array[2].equals("2")) {
					correctSet.add(array[0] + "->" + array[1]);
				}
			}
			br.close();
			fr.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static ArrayList<String[]> readViciousCycles() {
		ArrayList<String[]> results = new ArrayList<String[]>();
		try {
			FileReader fr = new FileReader(path + "vicious_cycles.csv");
			BufferedReader br = new BufferedReader(fr);
			String line="";
			String[] array = null;
			int cnt = 0;
			while((line = br.readLine()) != null) {
				if(line.contains("Homeless") || line.contains("Crime") || line.contains("Suburban_crime")) {
					cnt++;
					array = line.split(",");
					for(int i=0; i<array.length; i++) {
						array[i] = array[i].trim();
					}
					results.add(array);
				}
			}
			br.close();
			fr.close();
			System.out.println(cnt);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return results;
	}
	
	public static void main(String[] args) {
		readCorrectSet();
		ArrayList<String[]> vc_list = readViciousCycles();
		try {
			File file = new File(path + "正解悪循環リスト_ホームレスと犯罪.csv");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			
			for(String[] vc : vc_list) {
				boolean tf = true;
				String vcname = "";
				for(int i=0; i<vc.length; i++) {
					if(i<vc.length-1) {//最後じゃない場合
						vcname += vc[i] + ",";
						if(!correctSet.contains(vc[i] + "->" + vc[i+1])) {	//因果関係が正解データセットにない場合はfalseフラグ
							tf = false;
							System.out.println("誤:" + vc[i] + "->" + vc[i+1]);
							break;
						} else {
//							System.out.println("正解" + vc[i] + "->" + vc[i+1]);
						}
					} else {	//配列最後の場合は先頭とつなげる
						vcname += vc[i];
						if(!correctSet.contains(vc[i] + "->" + vc[0])) {
							System.out.println("誤:" + vc[i] + "->" + vc[0]);
							tf = false;
							break;
						} else {
//							System.out.println("正解" + vc[i] + "->" + vc[0]);
						}
					}
				}
//				System.out.println(vcname);
				if(tf) {	//全ての因果関係が合っている場合
					System.out.println("正解");
					pw.println(vcname);
				} else {
//					System.out.println(vcname);
				}
			}
			pw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
