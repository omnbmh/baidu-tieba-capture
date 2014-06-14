import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntegerTest {
	public static void main(String[] args) {
		String str = "123楼abc";
		// System.out.println(Integer.valueOf(str));
		// System.out.println(Integer.parseInt(str));
		// System.out.println(new Integer(str));

		System.out.println( Pattern.compile("[^0-9]").matcher(str).replaceAll(""));
	}
}
