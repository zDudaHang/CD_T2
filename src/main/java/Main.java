import app.Chat;
import gui.TerminalGUI;

import static gui.MsgColor.CYAN;

public class Main {
    private static final String greetings =
            "||\n" +
            "|| UFSC - Universidade Federal de Santa Catarina\n" +
            "|| Computação Distribuída - INE5418\n" +
            "|| Trabalho 2 - T2\n" +
            "|| Matheus Leonel Balduino - 17202305\n" +
            "|| Maria Eduarda de Melo Hang - XXXXXXXX\n" +
            "||\n" +
            "|| Seja bem-vindo ao UFSCzap!\n" +
            "|| - Comandos disponíveis:\n" +
            "||      !entrar <chat_name>: Se conectar com um chat\n" +
            "||      !sair: Sair do programa\n" +
            "||      !membros: Ver os membros do chat atual\n" +
            "||      !desconectar: Sair do chat atual\n" +
            "||\n\n";

    public static void main(String[] args) {
        // Pega nome a partir do user name do sistema
        String username = System.getProperty("user.name", "n/a");

        // Inicializa uma GUI. No nosso caso, um terminal
        TerminalGUI gui = new TerminalGUI(username, 100);

        // Apresenta um cabeçalho com nomes e instruções
        gui.clear();
        gui.printLn(CYAN, greetings);

        // Inicia um chat
        new Chat(gui, "teste");
    }
}
