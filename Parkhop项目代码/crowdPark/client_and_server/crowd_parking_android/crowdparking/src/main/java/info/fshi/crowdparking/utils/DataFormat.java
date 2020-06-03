package info.fshi.crowdparking.utils;

public class DataFormat {

	public static String string2Unicode(String string) {

		StringBuffer unicode = new StringBuffer();

		for (int i = 0; i < string.length(); i++) {

			char c = string.charAt(i);

			System.out.println(Integer.toHexString(c));
			
			unicode.append("\\u" + Integer.toHexString(c));
		}

		return unicode.toString();
	}
}
