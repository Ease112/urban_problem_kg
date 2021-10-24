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

//各予算セルが上段と下段で別れている場合
public class XLS2RDF2 {
	final static String qb = "http://purl.org/linked-data/cube#";
	final static String qb4o = "http://purl.org/qb4olap/cubes#";
	final static String up = "http://www.ohsuga.lab.uec.ac.jp/urbanproblem/qb/";
	final static String dbj = "http://ja.dbpedia.org/resource/";
	final static String dbp = "http://dbpedia.org/resource/";
	final static String sdmxAttribute = "http://purl.org/linked-data/sdmx/2009/attribute#";
	static ArrayList<String> managerList = new ArrayList<String>();
	static Map<String,String> translationList = new HashMap<String,String>();
	
	private static Model createProperty(Model model) {
		try {
			Property accountingName = model.createProperty(up + "accounting");
			accountingName
			.addProperty(RDF.type, model.getResource(qb + "DimensionProperty"))
			.addProperty(RDFS.label, model.createLiteral("会計名", "ja"))
			.addProperty(RDFS.label, model.createLiteral("Accounting type", "en"));

			Property annualExpenditure = model.createProperty(up + "annualExpenditure");
			annualExpenditure
			.addProperty(RDF.type, model.getResource(qb + "MeasureProperty"))
			.addProperty(RDFS.label, model.createLiteral("歳出額", "ja"))
			.addProperty(RDFS.label, model.createLiteral("Annual expenditure", "en"));

			Property generalRevenue = model.createProperty(up + "generalRevenue");
			generalRevenue
			.addProperty(RDF.type, model.getResource(qb + "MeasureProperty"))
			.addProperty(RDFS.label, model.createLiteral("所要一般財源", "ja"))
			.addProperty(RDFS.label, model.createLiteral("General revenue", "en"));

			Property id = model.createProperty(up + "id");
			id
			.addProperty(RDF.type, model.getResource(qb + "MeasureProperty"))
			.addProperty(RDFS.label, model.createLiteral("通し番号", "ja"))
			.addProperty(RDFS.label, model.createLiteral("ID", "en"));

			Property businessName = model.createProperty(up + "business");
			businessName
			.addProperty(RDF.type, model.getResource(qb + "DimensionProperty"))
			.addProperty(RDFS.label, model.createLiteral("事業名", "ja"))
			.addProperty(RDFS.label, model.createLiteral("Business", "en"));

			Property bureauName = model.createProperty(up + "bureau");
			bureauName
			.addProperty(RDF.type, model.getResource(qb4o + "LevelProperty"))
			.addProperty(RDFS.label, model.createLiteral("局名", "ja"))
			.addProperty(RDFS.label, model.createLiteral("Bureau", "en"));

			/*Property manager = model.createProperty(up + "manager");
			manager
			.addProperty(RDF.type, model.getResource(qb + "DimensionProperty"))
			.addProperty(RDFS.label, model.createLiteral("予算編成主管", "ja"))
			.addProperty(RDFS.label, model.createLiteral("Manager of budgeting", "en"));*/

			Property city = model.createProperty(up + "city");
			city
			.addProperty(RDF.type, model.getResource(qb4o + "LevelProperty"))
			.addProperty(RDFS.label, model.createLiteral("予算編成主管（市）", "ja"));
//			.addProperty(RDFS.label, model.createLiteral("Manager (city)", "en"));
			//.addProperty(model.getProperty(qb4o + "inDimension"), manager);

			Property ward = model.createProperty(up + "ward");
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
	private static Resource getDimensionResource(Model model, String ja, String type) throws InterruptedException {
		String en = "";
		if(translationList.containsKey(ja)) {
			en = translationList.get(ja);
		} else {
//			Thread.sleep(3000);
			ja = ja.replaceAll(" ", "%20");
			en = Causality.translate(ja);
			translationList.put(ja, en);
			System.out.println(ja);
		}
		
		Resource r = model.getResource(up + en.replaceAll(" ","_"));
		if(!model.contains(r,RDFS.label)) {
			r
				.addProperty(RDFS.label, model.createLiteral(ja,"ja"))
				.addProperty(RDFS.label, model.createLiteral(en,"en"))
				.addProperty(RDF.type, model.getResource(up + type));
			
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

	private static void createRDF(Model model, ArrayList<Map<String,String>> mapList, String datasetName, String city) {
		try {
			//DataStructureDefinitionの定義
			//DataStructureDefinitionは他のデータセットにも再利用可能なため年度情報は省く
			Property component = model.createProperty(qb + "component");
			Property dimension = model.createProperty(qb + "dimension");
			Property measure = model.createProperty(qb + "measure");
			Property attribute = model.createProperty(qb + "attribute");
			Property level = model.createProperty(qb4o + "level");
			Property hasAggregateFunction = model.createProperty(qb4o + "hasAggregateFunction");
			Resource dataStructureDefinition = model.createResource(up + "dsd_" + datasetName.replaceAll("平成28年度", ""));
			dataStructureDefinition
				.addProperty(RDF.type, model.createResource(qb + "DataStructureDefinition"))
				.addProperty(RDFS.label, model.createLiteral(datasetName + "のData Structure Definition"))
				.addProperty(RDFS.label, model.createLiteral("Data Structure Definition of 'The list of businesses whose budgets are freely managed by each city & ward manager in Osaka'","en"))
				.addProperty(component, model.createResource().addProperty(dimension, model.getProperty(up + "accounting")))
				.addProperty(component, model.createResource().addProperty(dimension, model.getProperty(up + "business")))
				.addProperty(component, model.createResource().addProperty(level, model.getProperty(up + "bureau")))
				.addProperty(component, model.createResource().addProperty(level, model.getProperty(up + "ward")))
				.addProperty(component, model.createResource()
						.addProperty(measure, model.getProperty(up + "annualExpenditure"))
						.addProperty(hasAggregateFunction, model.getProperty(qb4o + "sum")))
				.addProperty(component, model.createResource()
											.addProperty(measure, model.getProperty(up + "generalRevenue"))
											.addProperty(hasAggregateFunction, model.getProperty(qb4o + "sum")))
				.addProperty(component, model.createResource().addProperty(attribute, model.getProperty(sdmxAttribute + "unitMeasure")));
				
			
			//DataSetの作成
			String datasetUri = up + "The_list_of_businesses_whose_budgets_are_freely_managed_by_city_and_ward_managers_in_Osaka_(2016)";
			model.createResource(datasetUri)
			.addProperty(RDF.type, model.createResource(qb + "DataSet"))
			.addProperty(RDFS.label, model.createLiteral(datasetName,"ja"))
			.addProperty(RDFS.label, model.createLiteral("The list of businesses whose budgets are freely managed by city & ward managers in Osaka (2016)","en"))
			.addProperty(model.createProperty(qb + "structure"), dataStructureDefinition);

			//大阪市リソースの作成
			model.createResource(up + city)
			.addProperty(RDF.type, model.getResource(qb4o + "LevelMember"))
			.addProperty(RDFS.label, model.createLiteral(city, "ja"))
			.addProperty(RDFS.label, model.createLiteral("Osaka", "en"))
			.addProperty(SKOS.closeMatch, model.createResource(dbj + city));

			//大阪市各区リソースの作成
			for(String manager : managerList) {
				model.createResource(up + city + manager)
				.addProperty(RDF.type, model.getResource(qb4o + "LevelMember"))
				.addProperty(SKOS.broader, model.getResource(up + city))
				.addProperty(model.getProperty(qb4o + "inLevel"), model.getProperty(up + "ward"));
			}

			//Observationの作成
			for(Map<String,String> map : mapList) {
				/* 事業名 */
				Resource business = getDimensionResource(model,map.get("事業名"),"Business");
				
				/* 会計名 */
				Resource accounting = getDimensionResource(model,map.get("会計名"),"Accounting");
				
				/* 局名 */
				Resource bureau = getDimensionResource(model,map.get("局名"),"Bureau");
				
				
				for(String manager : managerList) {
					String uid = RandomStringUtils.randomAlphanumeric(10);
					Resource cell = model.createResource(up + uid);
					
					/* 区 */
					Resource ward = getDimensionResource(model,city+manager,"Ward");
					
					cell
					.addProperty(RDF.type, model.getResource(qb + "Observation"))
					.addProperty(model.getProperty(qb + "dataSet"), model.getResource(datasetUri))
					.addProperty(model.getProperty(up + "accounting"), accounting)
					.addProperty(model.getProperty(up + "id"), map.get("通し番号"))
					.addProperty(model.getProperty(up + "business"), business)
					.addProperty(model.getProperty(up + "bureau"), bureau)
					.addProperty(model.getProperty(up + "ward"), ward)
					.addProperty(model.getProperty(sdmxAttribute + "unitMeasure"), model.createResource(dbp + "Japanese_yen"))
					.addProperty(model.getProperty(up + "annualExpenditure"), model.createTypedLiteral(Integer.parseInt(map.get(manager + "歳出")) * 1000))
					.addProperty(model.getProperty(up + "generalRevenue"), model.createTypedLiteral(Integer.parseInt(map.get(manager + "所要一般財源")) * 1000));
				}
			}
			
			
//			RDFWriter writer = model.getWriter();
//			writer.setProperty("allowBadURIs", "true");
			FileOutputStream fout = new FileOutputStream("/Users/Shusaku/Dropbox/research/doctor/H28_osaka_city_manager_budget_20171110.ttl");
			model.setNsPrefix("upq", up);
			model.setNsPrefix("qb", qb);
			model.setNsPrefix("qb4o", qb4o);
			model.setNsPrefix("sdmx-attribute", sdmxAttribute);
			model.setNsPrefix("dbpedia-ja", dbj);
			model.setNsPrefix("xsd", XSD.NS);
			model.setNsPrefix("rdfs", RDFS.getURI());
			model.setNsPrefix("skos", SKOS.getURI());
			RDFDataMgr.write(fout, model, Lang.TTL);
//			writer.write(model, fout, "TTL");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static ArrayList<Map<String,String>> extract(String datasetName, String sheetName) {
		ArrayList<Map<String,String>> result = new ArrayList<Map<String,String>>();
		try {
			File file = new File("/Users/Shusaku/Dropbox/research/doctor/osaka_city_data/" + datasetName + ".xls");
			Workbook workbook = WorkbookFactory.create(file);
			Sheet sheet = workbook.getSheet(sheetName);
			String accounting = "";

			//区名のリストを抽出
			for(int j=4; j<sheet.getRow(6).getPhysicalNumberOfCells(); j++) {
				managerList.add(sheet.getRow(6).getCell(j).getStringCellValue());
			}

			Map<String,String> rowMap = new HashMap<String,String>();
			for(int i=0; i<sheet.getPhysicalNumberOfRows(); i++) {

				Row row = sheet.getRow(i);
				Cell firstCell = row.getCell(0);

				//1列目のセルの書式によって場合分け
				switch(firstCell.getCellTypeEnum()) {	//deprecatedになっているのはApache POI 3.15のバグ。3.16で改善(?)
				case STRING:
					String cellStrValue = firstCell.getStringCellValue();
					//					System.out.println(cellStrValue);
					if(cellStrValue.contains("会計名")) {
						accounting = cellStrValue.split("  ")[1];
						//						System.out.println(accounting);
					}
					
//					for(int k=0; k<managerList.size(); k++) {
//						System.out.println(row.getCell(k));
//					}
					
					break;
				case NUMERIC:
					rowMap = new HashMap<String,String>();		//先頭列がNUMERICの行と先頭列が空白の行で1セットのため、ここで初期化
					int cellNumValue = (int) firstCell.getNumericCellValue();
					rowMap.put("会計名", accounting);
					rowMap.put("通し番号", Integer.toString(cellNumValue));
					rowMap.put("事業名", row.getCell(1).getStringCellValue().replaceAll("\n", "").replaceAll("  ", ""));
					rowMap.put("局名", row.getCell(2).getStringCellValue());
					//各区の予算(上段)
					for(int k=0; k<managerList.size(); k++) {
//						System.out.println(row.getCell(k));
						rowMap.put(managerList.get(k) + "歳出", 
								Integer.toString((int) row.getCell(k+4).getNumericCellValue()));
					}

					break;
					
				case BLANK:
					//先頭列が空セル(連結されているため)かつ5列目が数字
					if(row.getCell(4).getCellTypeEnum().toString().equals("NUMERIC")) {
						for(int k=0; k<managerList.size(); k++) {
//							System.out.println(row.getCell(k));
							rowMap.put(managerList.get(k) + "所要一般財源", 
									Integer.toString((int) row.getCell(k+4).getNumericCellValue()));
						}
						result.add(rowMap);	//先頭列がNUMERICの行と先頭列が空白の行で1セットのためここでadd
					}
					
					break;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void main(String[] args) {
		String city = "大阪市";
		String datasetName = "平成28年度区シティ・マネージャー自由経費予算事業一覧";
		String sheetName = "Table 1";
		Model model = ModelFactory.createDefaultModel();
		model = createProperty(model);
		ArrayList<Map<String,String>> mapList = extract(datasetName, sheetName);
		createRDF(model, mapList, datasetName, city);
		//System.out.println(mapList.get(110));
	}

}
