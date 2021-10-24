package search;

import java.util.ArrayList;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.vocabulary.RDFS;

public class WordNet {
	private String endpoint;
	
	public WordNet(String endpoint) {
		this.endpoint = endpoint;
	}

	public ArrayList<String> getWordSynsetList(String word) {
		ArrayList<String> word_list = new ArrayList<String>();
		String sparql = "prefix rdfs: <" + RDFS.getURI() + ">\n"
				+ "SELECT DISTINCT (str(?label) AS ?word) FROM <http://wordnet.jp> \n"
				+ "WHERE {{ \n"
				+ "<http://www.w3.org/2006/03/wn/wn30/instances/synset-" + word + "-noun-1> <http://www.w3.org/2006/03/wn/wn20/schema/containsWordSense> ?ws . \n"
				+ "?ws rdfs:label ?label . } UNION {\n"
				+ "<http://www.w3.org/2006/03/wn/wn30/instances/synset-" + word + "-noun-1> <http://www.w3.org/2006/03/wn/wn20/schema/hyponymOf> ?hyponym .\n"
				+ "?hyponym <http://www.w3.org/2006/03/wn/wn20/schema/containsWordSense>/rdfs:label ?label .\n"
				+ "}}";
		System.out.println(sparql);
		try {
			ResultSet rs = selectResource(sparql);
			QuerySolution qs;
			while(rs.hasNext()) {
				qs = rs.nextSolution();
				if(qs.get("word").asLiteral().toString().length()>1)
					word_list.add(qs.get("word").asLiteral().toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return word_list;
	}
	
	public ResultSet selectResource(String sparql) {
		ResultSet rs = null;
		Query query = null;
		QueryExecution qexec = null;
		try {
			query = QueryFactory.create(sparql);
			qexec = QueryExecutionFactory.sparqlService(this.endpoint, query);
			rs = qexec.execSelect();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	public static void main(String[] args) {
		WordNet wn = new WordNet("http://www.ohsuga.lab.uec.ac.jp/sparql");
		System.out.println(wn.getWordSynsetList("factor"));
	}
	
	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
}
