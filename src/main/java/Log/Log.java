package Log;


public class Log {

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	public static void success(String message) {
		System.out.println("[" + ANSI_GREEN + "OK" + ANSI_RESET + "] " + message);
	}

	public static void error(String message) {
		System.out.println("[" + ANSI_RED + "ERRO" + ANSI_RESET + "] " + message);
	}

	public static void info(String message) {
		System.out.println("[" + ANSI_BLUE + "INFO" + ANSI_RESET + "] " + message);
	}

	public static void request(String message) {
		System.out.println("[" + ANSI_YELLOW + "SERVIDOR" + ANSI_RESET + "] " + message);
	}

}