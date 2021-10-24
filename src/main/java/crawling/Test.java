package crawling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Test {

	public static void main(String[] args) {
		double a, b, wa, sa, seki, syou;
		       BufferedReader d = new BufferedReader(new InputStreamReader(System.in));
		       try {
		         System.out.print("数を入力してください: ");
		         a = Double.valueOf(d.readLine()).doubleValue();
		         System.out.print("数を入力してください: ");
		         b = Double.valueOf(d.readLine()).doubleValue();
		         wa = a + b;
		         sa = a - b;
		         seki = a * b;
		         syou = a / b;
		         System.out.println("和=" + wa + ", 差=" + sa
		                            + ", 積=" + seki + ", 商=" + syou);
		       }
		       catch(IOException e) {
		         System.out.println("IO Error");
		         System.exit(1);
		       }
	}

}
