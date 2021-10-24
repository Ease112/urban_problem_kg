package lod;

import java.util.ArrayList;
import java.util.Map;

public class WikiData {
	String s = "";
	String sname = "";
	ArrayList<String> altList;
	Map<String,ArrayList<String>> hyperMap;
	
	public WikiData(String s, ArrayList<String> altList, Map<String, ArrayList<String>> hyperMap) {
		super();
		this.s = s;
		this.altList = altList;
		this.hyperMap = hyperMap;
	}
	
	public WikiData(String s, String sname, ArrayList<String> altList, Map<String, ArrayList<String>> hyperMap) {
		super();
		this.s = s;
		this.sname = sname;
		this.altList = altList;
		this.hyperMap = hyperMap;
	}

	public String getS() {
		return s;
	}

	public void setS(String s) {
		this.s = s;
	}

	public ArrayList<String> getAltList() {
		return altList;
	}

	public void setAltList(ArrayList<String> altList) {
		this.altList = altList;
	}

	public Map<String, ArrayList<String>> getHyperMap() {
		return hyperMap;
	}

	public void setHyperMap(Map<String, ArrayList<String>> hyperMap) {
		this.hyperMap = hyperMap;
	}

	public String getSname() {
		return sname;
	}

	public void setSname(String sname) {
		this.sname = sname;
	}
	
	
}
