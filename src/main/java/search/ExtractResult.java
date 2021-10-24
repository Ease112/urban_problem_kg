package search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExtractResult {
	String id;
	String title;
	Map<String,ArrayList<Map<String,Object>>> synonym_keywords;
	
	public ExtractResult(String id, String title) {
		super();
		this.id = id;
		this.title = title;
		this.synonym_keywords = new HashMap<String,ArrayList<Map<String,Object>>>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Map<String, ArrayList<Map<String, Object>>> getSynonym_keywords() {
		return synonym_keywords;
	}

	public void setSynonym_keywords(Map<String, ArrayList<Map<String, Object>>> synonym_keywords) {
		this.synonym_keywords = synonym_keywords;
	}
	
	public void put(String word, ArrayList<Map<String,Object>> extracted_list) {
		synonym_keywords.put(word, extracted_list);
	}
	
}
