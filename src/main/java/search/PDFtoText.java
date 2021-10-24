package search;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.tools.ExtractText;

public class PDFtoText{

	public static void main(String[] args) {
		boolean sort = false;
		boolean separateBeads = true;
		String password = "";
		int startPage = 0;
		int endPage = 2147483647;

		String pdfFile = "/Users/Shusaku/Dropbox/research/doctor/pdf/効率的な放置自転車対策のために－駅周辺の放置自転車の要因分析－.pdf";
		PDDocument document = null;
		StringWriter output = null;
		try
		{
		  document = PDDocument.load(new File(pdfFile), password);
		  AccessPermission ap = document.getCurrentAccessPermission();
		  if (!(ap.canExtractContent())) {
		    throw new IOException("You do not have permission to extract text");
		  }

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
		  String[] sentences = content.split("。");
		  if(!content.contains("。") && !content.contains("．")) {
			  sentences = content.split("\\.");
		  }
		  String keyword = "要因";
		  int cnt = 0;
		  for(int i=0; i<sentences.length; i++) {
			  if(sentences[i].contains(keyword)) {
				  System.out.println(sentences[i]);
				  cnt++;
			  }
		  }
		  System.out.println(cnt);
		  //System.out.println(content);
		}
		catch( Exception e )
		{
		  System.err.println("Error processing " + pdfFile + e.getMessage() );
		} finally {
		  IOUtils.closeQuietly(output);
		  IOUtils.closeQuietly(document);
		}
	}

}
