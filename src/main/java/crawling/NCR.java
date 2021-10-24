package crawling;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NCR {
	private static final Pattern P = Pattern.compile(
			"&#(x([0-9a-f]+)|([0-9]+));",
			Pattern.CASE_INSENSITIVE
			);

	private static boolean isHex(final String str) {
		final char x = str.charAt(0);
		return 'x' == x || 'X' == x;
	}

	public static String ncr(final String str) {
		final StringBuffer rtn = new StringBuffer();
		final Matcher matcher = P.matcher(str);
		while (matcher.find()) {
			final String group = matcher.group(1);
			int parseInt;
			if (isHex(group)) {
				parseInt = Integer.parseInt(group.substring(1), 16);
			} else {
				parseInt = Integer.parseInt(group, 10);
			}

			final char c;
			if (0 != (0x0ffff & parseInt)) {
				c = (char) parseInt;
			} else {
				c = '?';
			}
			matcher.appendReplacement(rtn, Character.toString(c));
		}
		matcher.appendTail(rtn);

		return rtn.toString();
	}
}