package util;

public class ChatUtil {

	public static boolean isCommand(String msg) { return msg.charAt(0) == '!'; }

	public static String createCounterName(long surveyNumber, int optionNumber) {
		return "ENQ#" + surveyNumber + "#" + optionNumber;
	}
}
