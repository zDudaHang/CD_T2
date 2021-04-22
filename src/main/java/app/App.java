package app;

import gui.TerminalGUI;

import java.util.HashMap;

import static gui.MsgColor.CYAN;

public class App {
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
            "||\n";

    private String username;
    private final HashMap<String, Chat> chats = new HashMap<>();

    public App() {
        // Apresenta um cabeçalho com nomes e instruções
        TerminalGUI.clear();
        TerminalGUI.printLn(CYAN, greetings);

        // Pede o nome de usuario
        this.username = TerminalGUI.readWithHeader("Nome de usuário: ", 50);
        TerminalGUI.printLnInfo("Username: " + this.username);

        // Pede o nome do primeiro chat, conecta e limpa a tela
        String chatname = TerminalGUI.readWithHeader("Conectar no chat: ", 50);
        TerminalGUI.printLnInfo("Chatname: " + chatname);
//        Chat newChat = new Chat(, this.username, chatname);
//        this.chats.put(chatname, newChat);
//        gui.clear();
//
//        newChat.activate();
    }
}
