package app;

import gui.TerminalGUI;

import java.util.HashMap;
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
            "||      !entrar <nome_chat>: Se conectar com um chat\n" +
            "||      !sair: Sair do programa\n" +
            "||      !membros: Ver os membros do chat atual\n" +
            "||      !desconectar: Sair do chat atual\n" +
            "||      !priv <nome_usuario> <mensagem>: Mandar uma mensagem privada\n" +
            "||      !enquete <titulo> <opcao1>,<opcao2>,... : Criar uma enquete\n" +
            "||      !votar <num_enquete> <opcao>: Votar em uma enquete\n" +
            "||\n";

    public final HashMap<String, Chat> chats = new HashMap<>();

    public App() {
        // Apresenta um cabeçalho com nomes e instruções
        TerminalGUI.clear();
        TerminalGUI.printLn(CYAN, greetings);

        // Pede o nome de usuario
        TerminalGUI.printLn("Seu nome de usuário:");
        String username = TerminalGUI.read(50);
        TerminalGUI.printLnInfo("Nome de usuário: " + username);

        // Pede o nome do primeiro chat, conecta e limpa a tela
        TerminalGUI.printLn("Nome do chat:");
        String chatname = TerminalGUI.read(50);
        TerminalGUI.printLnInfo("Chat: " + chatname);

        // Remove logs
        LogFactory.setCustomLogFactory(new app.LogFactory()); // Omitir logs

        Chat newChat = new Chat(this, username, chatname, true);

        this.chats.put(chatname, newChat);
        newChat.activate();
    }
}
