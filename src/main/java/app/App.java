package app;

import gui.TerminalGUI;

import java.util.HashMap;

import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.logging.LogFactory;

import static gui.MsgColor.CYAN;

public class App {
    private static final String greetings =
            "||\n" +
            "|| UFSC - Universidade Federal de Santa Catarina\n" +
            "|| Computação Distribuída - INE5418\n" +
            "|| Trabalho 2 - T2\n" +
            "|| Matheus Leonel Balduino - 17202305\n" +
            "|| Maria Eduarda de Melo Hang - 17202304\n" +
            "||\n" +
            "|| Seja bem-vindo ao UFSCzap!\n" +
            "|| - Comandos disponíveis:\n" +
            "||      !entrar <chat_name>: Se conectar com um chat\n" +
            "||      !sair: Sair do programa\n" +
            "||      !membros: Ver os membros do chat atual\n" +
            "||      !desconectar: Sair do chat atual\n" +
            "||\n";

    private String username;
    public final HashMap<String, Chat> chats = new HashMap<>();

    public App() {
        // Apresenta um cabeçalho com nomes e instruções
        TerminalGUI.clear();
        TerminalGUI.printLn(CYAN, greetings);

        // Pede o nome de usuario
        TerminalGUI.printLn("Seu nome de usuário:");
        this.username = TerminalGUI.read(50);
        TerminalGUI.printLnInfo("Nome de usuário: " + this.username);

        // Pede o nome do primeiro chat, conecta e limpa a tela
        TerminalGUI.printLn("Nome do chat:");
        String chatname = TerminalGUI.read(50);
        TerminalGUI.printLnInfo("Chat: " + chatname);

        // Remove logs
        LogFactory.setCustomLogFactory(new app.LogFactory()); // Omitir logs

        Chat newChat = new Chat(this, this.username, chatname, true);

        this.chats.put(chatname, newChat);
        newChat.activate();
    }
}
