package index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;

public class IndexTextFile extends Index {

	public static String readAll(String path) throws IOException {
		StringBuilder builder = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			String string = reader.readLine();
			while (string != null){
				builder.append(string + System.getProperty("line.separator"));
				string = reader.readLine();
			}
		}

		return builder.toString();
	}
	public static void main(String[] args) {
		try {
			String index = "social";
			String type = "poisute_factor";
			
			ArrayList<String> title_list = new ArrayList<String>();
			title_list = getExistances(index, type);
			
			String dir = "/Users/Shusaku/Dropbox/research/doctor/poisute_factor/text/";
			File[] files = readFiles(new File(dir));
			for(int i=0; i<files.length; i++) {
				String fileName = files[i].getName();
				System.out.println(i + " filename:" + fileName);
				if(title_list.contains(fileName)) { System.out.println("skip"); continue; }
				String content = readAll(dir + fileName);
				content = content.replaceAll("\n","");
				content = content.replaceAll("  ", "");
				store(fileName, content, index, type);
				System.out.println("ok");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
