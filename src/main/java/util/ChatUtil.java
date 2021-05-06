package util;

import gui.MsgColor;
import gui.TerminalGUI;

public class ChatUtil {

	public static boolean isCommand(String msg) { return msg.charAt(0) == '!'; }

	public static String createCounterName(long surveyNumber, int optionNumber) {
		return "ENQ#" + surveyNumber + "#" + optionNumber;
	}

	public static void greet() {
		TerminalGUI.printLn(MsgColor.CYAN,
				"||\n" +
				"|| UFSC - Universidade Federal de Santa Catarina\n" +
				"|| Computação Distribuída - INE5418\n" +
				"|| Trabalho 2 - Comunicação em Grupo\n" +
				"|| Matheus Leonel Balduino - 17202305\n" +
				"|| Maria Eduarda de Melo Hang - 17202304\n" +
				"||\n" +
				"|| Seja bem-vindo ao UFSCzap!\n"
		);
		showCommands();
	}

	public static void showCommands() {
		TerminalGUI.printLn(MsgColor.CYAN,
				"|| - Comandos disponíveis:\n" +
						"||      !entrar <nome_chat>: Se conectar com um chat\n" +
						"||      !sair: Sair do programa\n" +
						"||      !membros: Ver os membros do chat atual\n" +
						"||      !desconectar: Sair do chat atual\n" +
						"||      !priv <nome_usuario> <mensagem>: Mandar uma mensagem privada\n" +
						"||      !enquete <titulo> <opcao1>,<opcao2>,... : Criar uma enquete\n" +
						"||      !votar <num_enquete> <opcao>: Votar em uma enquete\n" +
						"||      !ajuda: Mostrar os comandos\n" +
						"||\n"
		);
	}
}
