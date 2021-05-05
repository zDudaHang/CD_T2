package app;

public enum Commands {
	ENTER("!entrar", true),
	QUIT("!sair", false),
	MEMBERS("!membros", false),
	DISCONNECT("!desconectar", false),
	PRIVATE_MSG("!priv", true),
	SURVEY("!enquete", true),
	VOTE("!votar", true),
	HELP("!ajuda", false);

	public String argument;
	private final String cmd;
	private final boolean hasArg;

	Commands(String cmd, boolean hasArg) {
		this.cmd = cmd;
		this.hasArg = hasArg;
	}

	public static Commands fromString(String cmd) throws Exception {
		for (Commands c : Commands.values()) {
			if (cmd.startsWith(c.cmd)) {
				if (!c.hasArg)
					return c;

				String[] split = cmd.split(" ", 2);
				if (split.length != 2)
					throw new Exception("Comando '" + c.cmd + "' necessita de um argumento");

				c.argument = split[1];
				return c;
			}
		}
		return null;
	}
}
