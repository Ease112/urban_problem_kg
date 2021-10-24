package index;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import static org.elasticsearch.common.xcontent.XContentFactory.*;

public class IndexPDF extends Index {

	private static String streamToString(InputStream is) {
		String str = "";
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line;
			while (null != (line = reader.readLine())) {
				sb.append(line);
			}
			str = sb.toString();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return str;
	}

	public static void main(String[] args) {

		boolean sort = false;
		boolean separateBeads = true;
		String password = "";
		int startPage = 0;
		int endPage = 2147483647;
		//String title = "富岡市きれいなまちづくり条例";
		//String pdfFile = "/Users/Shusaku/Dropbox/research/doctor/pdf2/" + title + ".pdf";
		PDDocument document = null;
		StringWriter output = null;
		String index = "social";
		String type = "domestic_violence_cause";
		
		ArrayList<String> title_list = new ArrayList<String>();
		title_list = getExistances(index, type);
		try {
			String dir = "/Users/Shusaku/Dropbox/research/doctor/" + type + "/";
			File[] files = readFiles(new File(dir));
			for(int i=0; i<files.length; i++) {
				try {
					String fileName = files[i].getName();
					System.out.println(i + " filename:" + fileName);
					if(title_list.contains(fileName)) { System.out.println("skip"); continue; }
					
					document = PDDocument.load(files[i], password);
					if (document.isEncrypted()) {
				        try {
				            document.setAllSecurityToBeRemoved(true);
				        }
				        catch (Exception e) {
				            throw new Exception("The document is encrypted, and we can't decrypt it.", e);
				        }
				    }
//					AccessPermission ap = document.getCurrentAccessPermission();
//					if (!(ap.canExtractContent())) {
//						throw new IOException("You do not have permission to extract text");
//					}

					output = new StringWriter();
					PDFTextStripper stripper = new PDFTextStripper();
					stripper.setSortByPosition(sort);
					stripper.setShouldSeparateByBeads(separateBeads);
					stripper.setStartPage(startPage);
					stripper.setEndPage(endPage);
					stripper.writeText(document, output);

					String content = output.toString();
					content = content.replaceAll("\n","");
					content = content.replaceAll("  ", "");
					//				System.out.println(content);

					store(fileName, content, index, type);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		catch( Exception e ) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(output);
			IOUtils.closeQuietly(document);
		}
	}

}
