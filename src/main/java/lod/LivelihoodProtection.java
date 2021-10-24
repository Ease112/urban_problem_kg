package lod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.vocabulary.XSD;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class LivelihoodProtection {
	final static String qb = "http://purl.org/linked-data/cube#";
	final static String qb4o = "http://purl.org/qb4olap/cubes#";
	final static String upq = "http://www.ohsuga.lab.uec.ac.jp/urbanproblem/qb/";
	final static String upr = "http://www.ohsuga.lab.uec.ac.jp/urbanproblem/resource/";
	final static String dbj = "http://ja.dbpedia.org/resource/";
	final static String dbp = "http://dbpedia.org/resource/";
	final static String sdmxAttribute = "http://purl.org/linked-data/sdmx/2009/attribute#";
	static ArrayList<String> managerList = new ArrayList<String>();
	static Map<String,String> translationList = new HashMap<String,String>();
	
	private static Model createProperty(Model model) {
		try {
			Property budget = model.createProperty(upq + "budget");
			budget
			.addProperty(RDF.type, model.getResource(qb + "MeasureProperty"))
			.addProperty(RDFS.label, "予算")
			.addProperty(RDFS.label, model.createLiteral("Budget","en"));

			Property annualExpenditure = model.createProperty(upq + "annualExpenditure");
			annualExpenditure
			.addProperty(RDF.type, model.getResource(qb + "MeasureProperty"))
			.addProperty(RDFS.label, model.createLiteral("歳出額", "ja"))
			.addProperty(RDFS.label, model.createLiteral("Annual expenditure", "en"));

			Property generalRevenue = model.createProperty(upq + "generalRevenue");
			generalRevenue
			.addProperty(RDF.type, model.getResource(qb + "MeasureProperty"))
			.addProperty(RDFS.label, model.createLiteral("所要一般財源", "ja"))
			.addProperty(RDFS.label, model.createLiteral("General revenue", "en"));

			Property businessName = model.createProperty(upq + "business");
			businessName
			.addProperty(RDF.type, model.getResource(qb + "DimensionProperty"))
			.addProperty(RDFS.label, model.createLiteral("事業名", "ja"))
			.addProperty(RDFS.label, model.createLiteral("Business", "en"));

			Property bureauName = model.createProperty(upq + "bureau");
			bureauName
			.addProperty(RDF.type, model.getResource(qb4o + "LevelProperty"))
			.addProperty(RDFS.label, model.createLiteral("局名", "ja"))
			.addProperty(RDFS.label, model.createLiteral("Bureau", "en"));

			/*Property manager = model.createProperty(up + "manager");
			manager
			.addProperty(RDF.type, model.getResource(qb + "DimensionProperty"))
			.addProperty(RDFS.label, model.createLiteral("予算編成主管", "ja"))
			.addProperty(RDFS.label, model.createLiteral("Manager of budgeting", "en"));*/

			Property city = model.createProperty(upq + "city");
			city
			.addProperty(RDF.type, model.getResource(qb4o + "LevelProperty"))
			.addProperty(RDFS.label, model.createLiteral("予算編成主管（市）", "ja"))
			.addProperty(RDFS.label, model.createLiteral("Manager (city)", "en"));
			//.addProperty(model.getProperty(qb4o + "inDimension"), manager);

			Property ward = model.createProperty(upq + "ward");
			ward
			.addProperty(RDF.type, model.getResource(qb4o + "LevelProperty"))
			.addProperty(RDFS.label, model.createLiteral("予算編成主管（区）", "ja"))
			.addProperty(RDFS.label, model.createLiteral("Manager (ward)", "en"));
//			.addProperty(model.getProperty(qb4o + "parentLevel"), city)
			//.addProperty(model.getProperty(qb4o + "inDimension"), manager);
			
//			Resource city_r = model.createResource(up + "City");
//			city_r.addProperty(RDF.type, model.getResource(qb4o + "LevelMember"));
			

//			Property unit = model.createProperty(up + "単位");
//			ward
//			.addProperty(RDF.type, model.getResource(qb + "AttributeProperty"))
//			.addProperty(RDFS.label, model.createLiteral("単位", "ja"))
//			.addProperty(RDFS.label, model.createLiteral("unit", "en"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;
	}
	
	//事業名,会計名,局名などのリソース作成
	private static Resource getDimensionResource(Model model, String ja, String type) {
		String en = "";
		if(translationList.containsKey(ja)) {
			en = translationList.get(ja);
		} else {
			en = Causality.translate(ja);
			translationList.put(ja, en);
		}
		Resource r = model.getResource(upq + en.replaceAll(" ","_"));
		if(!model.contains(r,RDFS.label)) {
			r
				.addProperty(RDFS.label, model.createLiteral(ja,"ja"))
				.addProperty(RDFS.label, model.createLiteral(en,"en"))
				.addProperty(RDF.type, model.getResource(upq + type));
			
			//事業リソースからUrban Problem LODへのリンク付け
			if(type.equals("Business")) {
				ArrayList<String> linkable_list = LinkBudget2UrbanProblem.getLinkableList(ja);
				for(String up_uri : linkable_list) {
					r.addProperty(DCTerms.subject, model.getResource(up_uri));
				}
			}
		}
		return r;
	}

	private static Map<String,Integer> extract(String datasetName, String sheetName) {
		Map<String,Integer> result = new HashMap<String,Integer>();
		try {
			File file = new File("/Users/Shusaku/Dropbox/research/doctor/osaka_city_data/" + datasetName + ".xls");
			Workbook workbook = WorkbookFactory.create(file);
			Sheet sheet = workbook.getSheet(sheetName);

			for(int i=6; i<sheet.getPhysicalNumberOfRows(); i++) {
				Row row = sheet.getRow(i);
				Cell ku = row.getCell(0);
				Cell hogohi = row.getCell(4);
//				System.out.println(ku.getStringCellValue());
//				System.out.println((int) hogohi.getNumericCellValue());
				result.put(ku.getStringCellValue(), (int) hogohi.getNumericCellValue());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void main(String[] args) {
		String city = "大阪市";
		String datasetName = "平成27年度大阪市生活保護費";
		String datasetNameEn = "Osaka City livelihood protection budget 2015";
		String sheetName = "Sheet1";
		Model model = ModelFactory.createDefaultModel();
		model = createProperty(model);
		Map<String,Integer> map = extract(datasetName, sheetName);
		
		try {
			//DataStructureDefinitionの定義
			//DataStructureDefinitionは他のデータセットにも再利用可能なため年度情報は省く
			Property component = model.createProperty(qb + "component");
			Property dimension = model.createProperty(qb + "dimension");
			Property measure = model.createProperty(qb + "measure");
			Property attribute = model.createProperty(qb + "attribute");
			Property level = model.createProperty(qb4o + "level");
			Property hasAggregateFunction = model.createProperty(qb4o + "hasAggregateFunction");
			Resource dataStructureDefinition = model.createResource(upq + "dsd_" + datasetNameEn.replaceAll(" 2015", "").replaceAll(" ", "_"));
			dataStructureDefinition
				.addProperty(RDF.type, model.createResource(qb + "DataStructureDefinition"))
				.addProperty(RDFS.label, model.createLiteral(datasetName + "のData Structure Definition", "ja"))
				.addProperty(RDFS.label, model.createLiteral("Data Structure Definition of 'The list of businesses whose budgets are freely managed by each city & ward manager in Osaka'","en"))
				.addProperty(component, model.createResource().addProperty(dimension, model.getProperty(upq + "business")))
				.addProperty(component, model.createResource().addProperty(level, model.getProperty(upq + "bureau")))
				.addProperty(component, model.createResource().addProperty(level, model.getProperty(upq + "ward")))
				.addProperty(component, model.createResource()
						.addProperty(measure, model.getProperty(upq + "annualExpenditure"))
						.addProperty(hasAggregateFunction, model.getProperty(qb4o + "sum")))
				.addProperty(component, model.createResource()
											.addProperty(measure, model.getProperty(upq + "generalRevenue"))
											.addProperty(hasAggregateFunction, model.getProperty(qb4o + "sum")))
				.addProperty(component, model.createResource().addProperty(attribute, model.getProperty(sdmxAttribute + "unitMeasure")));
				
			
			//DataSetの作成
			String datasetUri = upq + datasetNameEn.replaceAll(" ", "_");
			model.createResource(datasetUri)
			.addProperty(RDF.type, model.createResource(qb + "DataSet"))
			.addProperty(RDFS.label, model.createLiteral(datasetName,"ja"))
			.addProperty(RDFS.label, model.createLiteral(datasetNameEn,"en"))
			.addProperty(model.createProperty(qb + "structure"), dataStructureDefinition);

			//市のクラス作成
			Resource cityLevel = model.createResource(upq + "City");
			cityLevel
			.addProperty(RDF.type, OWL.Class)
			.addProperty(RDFS.subClassOf, model.getResource(qb4o + "LevelMember"))
			.addProperty(RDFS.label, model.createLiteral("City", "en"))
			.addProperty(RDFS.label, model.createLiteral("市", "ja"));
			
			//区のクラス作成
			Resource wardLevel = model.createResource(upq + "Ward");
			cityLevel
			.addProperty(RDF.type, OWL.Class)
			.addProperty(RDFS.subClassOf, model.getResource(qb4o + "LevelMember"))
			.addProperty(SKOS.broader, cityLevel)
			.addProperty(RDFS.label, model.createLiteral("Ward", "en"))
			.addProperty(RDFS.label, model.createLiteral("区", "ja"));
			
			
			//大阪市リソースの作成
			model.createResource(upq + "Osaka_city")
			.addProperty(RDF.type, cityLevel)
			.addProperty(RDFS.label, model.createLiteral(city, "ja"))
			.addProperty(RDFS.label, model.createLiteral("Osaka", "en"))
			.addProperty(SKOS.closeMatch, model.createResource(dbj + city));

			//事業クラス
			Resource businessLevel = model.createResource(upq + "Business");
			businessLevel.addProperty(RDF.type, OWL.Class)
			.addProperty(RDFS.label, model.createLiteral("Business", "en"))
			.addProperty(RDFS.label, model.createLiteral("事業","ja"))
			.addProperty(RDFS.subClassOf, model.getResource(qb4o + "LevelMember"));
			
			//生活保護
			Resource livelihood = model.createResource(upq + "Livelihood_protection");
			livelihood
			.addProperty(RDF.type, businessLevel)
			.addProperty(RDFS.label, model.createLiteral("生活保護","ja"))
			.addProperty(RDFS.label, model.createLiteral("Livelihood protection", "en"))
			.addProperty(DCTerms.subject, model.getResource(upr + "Welfare"))
			.addProperty(DCTerms.subject, model.getResource(upr + "Welfare_recipient"));
			
			//局
			Resource bureauLevel = model.createResource(upq + "Bureau");
			bureauLevel.addProperty(RDF.type, OWL.Class)
			.addProperty(RDFS.label, model.createLiteral("局","ja"))
			.addProperty(RDFS.label, model.createLiteral("Bureau", "en"))
			.addProperty(RDFS.subClassOf, model.getResource(qb4o + "LevelMember"));
			
			//福祉局
			Resource welfareBureau = model.createResource(upq + "welfareBureau");
			welfareBureau.addProperty(RDF.type, bureauLevel)
			.addProperty(RDFS.label, model.createLiteral("福祉局", "ja"))
			.addProperty(RDFS.label, model.createLiteral("Welfare bureau", "en"));
			
			//Observationの作成
			for(Map.Entry<String, Integer> e : map.entrySet()) {
				Resource ward_r =  model.createResource(upq + Causality.translate(city + e.getKey()).replaceAll(" ", "_"));
				ward_r.addProperty(RDF.type, wardLevel);
				
				Resource cell = model.createResource(upq + RandomStringUtils.randomAlphanumeric(10));
				cell
				.addProperty(RDF.type, model.getResource(qb + "Observation"))
				.addProperty(model.getProperty(qb + "dataSet"), model.getResource(datasetUri))
				.addProperty(model.getProperty(upq + "business"), livelihood)
				.addProperty(model.getProperty(upq + "bureau"), welfareBureau)
				.addProperty(model.getProperty(upq + "ward"),ward_r)
				.addProperty(model.getProperty(sdmxAttribute + "unitMeasure"), model.createResource(dbp + "Japanese_yen"))
				.addProperty(model.getProperty(upq + "annualExpenditure"), model.createTypedLiteral(e.getValue()))
				.addProperty(model.getProperty(upq + "generalRevenue"), model.createTypedLiteral(e.getValue()));
				
			}
			
			
//			RDFWriter writer = model.getWriter();
//			writer.setProperty("allowBadURIs", "true");
			FileOutputStream fout = new FileOutputStream("/Users/Shusaku/Dropbox/research/doctor/osaka_livelihood_protection_budget_20170828.ttl");
			model.setNsPrefix("upq", upq);
			model.setNsPrefix("qb", qb);
			model.setNsPrefix("qb4o", qb4o);
			model.setNsPrefix("sdmx-attribute", sdmxAttribute);
			model.setNsPrefix("dbpedia-ja", dbj);
			model.setNsPrefix("xsd", XSD.NS);
			model.setNsPrefix("rdfs", RDFS.getURI());
			model.setNsPrefix("skos", SKOS.getURI());
			RDFDataMgr.write(fout, model, Lang.TTL);
		} catch(Exception e) {
			e.printStackTrace();
		}
		//System.out.println(mapList.get(110));
	}

}
