package lod;

import java.io.*;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.SKOS;



public class MergeRDF {

	final static String qb = "http://purl.org/linked-data/cube#";
	final static String qb4o = "http://purl.org/qb4olap/cubes#";
	final static String upq = "http://www.ohsuga.lab.uec.ac.jp/urbanproblem/qb/";
	final static String upr = "http://www.ohsuga.lab.uec.ac.jp/urbanproblem/resource/";
	final static String upv = "http://www.ohsuga.lab.uec.ac.jp/urbanproblem/vocabulary#";
	final static String dbj = "http://ja.dbpedia.org/resource/";
	final static String dbp = "http://dbpedia.org/resource/";
	final static String sdmxAttribute = "http://purl.org/linked-data/sdmx/2009/attribute#";
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Model model1 = ModelFactory.createDefaultModel();
		Model model2 = ModelFactory.createDefaultModel();
		FileInputStream file1 = new FileInputStream("/Users/Shusaku/Desktop/Dropbox/research/doctor/crowdsourcing/urbanproblem_20170821.ttl");
		FileInputStream file2 = new FileInputStream("/Users/Shusaku/Desktop/N研オントロジー/rdf/nkenrdf20141203.rdf");
		model1.read(file1, null);
		model2.read(file2, null);
		model1.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
		model1.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		model1.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		model1.setNsPrefix("skos", SKOS.getURI());
		model1.setNsPrefix("dcterms", DCTerms.NS);
		model2.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
		model2.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		model2.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		model2.setNsPrefix("skos", SKOS.getURI());
		model2.setNsPrefix("dcterms", DCTerms.NS);
		Model model = ModelFactory.createDefaultModel();
		
		model = model1.union(model2);
		model.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
		model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		model.setNsPrefix("skos", SKOS.getURI());
		model.setNsPrefix("dcterms", DCTerms.NS);
		model.setNsPrefix("upv", upv);
		model.setNsPrefix("upr", upr);
		model.setNsPrefix("upq", upq);
		model.setNsPrefix("dbp", dbp);
		model.setNsPrefix("dbj", dbj);
		model.setNsPrefix("sdmx-attr", sdmxAttribute);
		FileOutputStream fout = new FileOutputStream("/Users/Shusaku/Desktop/N研オントロジー/rdf/merge20150202_all.ttl");
        model.write(fout, "TTL");
        fout.close();
        System.out.println("Finished");
	}

}
