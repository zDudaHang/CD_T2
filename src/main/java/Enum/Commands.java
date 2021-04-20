package Enum;

public enum Commands {
	CONNECT("!entrar"),
	QUIT("!sair"),
	MEMBERS("!membros"),
	DISCONNECT("!desconectar"),
	;

	private final String cmd;

	Commands(String cmd) {
		this.cmd = cmd;
	}

	public static Commands fromString(String cmd) {
		for (Commands c : Commands.values()) {
			if (c.cmd.equals(cmd)) {
				return c;
			}
		}
		return null;
	}
}
