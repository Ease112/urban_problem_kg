package ViciousCycle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import com.google.common.base.Joiner;

public class EvaluateRootProblem {
	static String path = "/Users/Shusaku/Dropbox/research/doctor/";

	public static ArrayList<String[]> readCorrectData() {
		ArrayList<String[]> correct_list = new ArrayList<String[]>();
		try {
			FileReader fr = new FileReader(path + "正解悪循環リスト.csv");
			BufferedReader br = new BufferedReader(fr);
			String line="";
			String[] array = null;
			
			
			while((line = br.readLine()) != null) {
				array = line.split(",");
				correct_list.add(array);
			}
			br.close();
			fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return correct_list;
	}
	
	private static ResultSet selectResource(String sparql) {
		ResultSet rs = null;
		Query query = null;
		QueryExecution qexec = null;
		try {
			query = QueryFactory.create(sparql);
			qexec = QueryExecutionFactory.sparqlService("http://www.ohsuga.lab.uec.ac.jp/sparql", query);
			rs = qexec.execSelect();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	private static String getRootProblem(String elem1, String elem2) {
		String result = null;
		try {
			String sparql = 
					"PREFIX upv: <http://www.ohsuga.lab.uec.ac.jp/urbanproblem/vocabulary#>\n"
					+ "PREFIX upr: <http://www.ohsuga.lab.uec.ac.jp/urbanproblem/resource/>"
					+ "SELECT DISTINCT ?s FROM <http://www.ohsuga.lab.uec.ac.jp/urbanproblem> WHERE {\n"
					+ "?s upv:affect_level1|upv:affect_level2|upv:affect_level3|upv:affect_level4 upr:" + elem1 + " ;\n"
						+ "upv:affect_level1|upv:affect_level2|upv:affect_level3|upv:affect_level4 upr:" + elem2 + " .\n"
					+ "}";
			
			ResultSet rs = selectResource(sparql);
			while(rs.hasNext()) {
				QuerySolution qs = rs.nextSolution();
				result = qs.get("s").toString();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void main(String[] args) {
		ArrayList<String[]> list = readCorrectData();
		String[] vc1;
		String[] vc2;
		HashSet<String> checked = new HashSet<String>();
		try {
			File file = new File(path + "正解中核課題.csv");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			String root_problem = null;
			for(int i=0; i<list.size(); i++) {
				vc1 = list.get(i);
				String vc1JoinStr = Joiner.on("->").skipNulls().join(vc1);
				for(int j=i; j<list.size(); j++) {
					vc2 = list.get(j);
					String vc2JoinStr = Joiner.on("->").skipNulls().join(vc2);
					for(String vcelem1 : vc1) {
						for(String vcelem2 : vc2) {
							if(!checked.contains(vcelem1 + ":" + vcelem2) 
									&& !vcelem1.equals(vcelem2)
									&& !vc1JoinStr.equals(vc2JoinStr)) {
								root_problem = getRootProblem(vcelem1,vcelem2);
								if(root_problem != null) {
									System.out.println(root_problem);
									pw.println(root_problem.replace("http://www.ohsuga.lab.uec.ac.jp/urbanproblem/resource/","") + "," + vcelem1 + "," + vcelem2 + "," + vc1JoinStr + "," + vc2JoinStr);
								}
								checked.add(vcelem1 + ":" + vcelem2);
							}
						}
					}
				}
			}
			pw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
