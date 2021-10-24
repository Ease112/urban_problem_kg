package construct;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

public class SPARQL {
	private String endpoint;
	
	public SPARQL(String endpoint) {
		this.endpoint = endpoint;
	}
	
	protected static String prefix = "prefix rdfs: <" + RDFS.getURI() + ">\n"
			+ "prefix owl: <" + OWL.NS + ">\n"
			+ "prefix dcterms: <" + DCTerms.NS + ">\n"
			+ "prefix ngeo: <http://geovocab.org/geometry#>\n"
			+ "prefix ogcgs: <http://www.opengis.net/ont/geosparql#>\n"
			+ "prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"
			+ "PREFIX xl: <http://www.w3.org/2008/05/skos-xl#>\n"
			+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n";
	
	public String createAskQuery(String s) {
		String q = "";
		q = prefix + "ask { <" + s + "> ?p ?o }";
		return q;
	}
	
	public String createSelectQuery(String s) {
		String q = "";
		q = prefix + "select distinct * where { <" + s + "> ?p ?o . }";
		return q;
	}
	
	public String createSelectSubClassOfQuery(String s) {
		String q = "";
		q = prefix + "select distinct * where { <" + s + "> rdfs:subClassOf ?o . }";
		return q;
	}
	
	public String createSelectHyperQuery(String s) {
		String q = "";
		q = prefix + "select distinct * where { <" + s + "> ?p ?o . "
				+ "filter(?p = <http://purl.org/dc/terms/subject> || "
				+ "?p = <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> || "
				+ "?p = <http://www.w3.org/2004/02/skos/core#broader> || "
				+ "?p = <http://www.w3.org/2000/01/rdf-schema#subClassOf> || "
				+ "?p = <http://www.w3.org/2006/03/wn/wn20/schema/hyponymOf> || "
				+ "?p = <http://www.w3.org/2006/03/wn/wn20/schema/partMeronyOf>) }";
		return q;
	}
	
	public String createInboundQuery(String s) {
		String q = "";
		q = prefix + "select distinct * where { ?s ?p <" + s + "> . filter(!contains(str(?s), '☆')) }";
		return q;
	}
	
	public boolean askResource(String sparql) {
		boolean result = true;
		Query query = null;
		QueryExecution qexec = null;
		try {
			query = QueryFactory.create(sparql);
			qexec = QueryExecutionFactory.sparqlService(this.endpoint, query);
			result = qexec.execAsk();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
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
	
	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
}
