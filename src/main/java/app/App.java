package app;

import gui.TerminalGUI;
import util.ChatUtil;

import java.util.HashMap;
import org.jgroups.logging.LogFactory;

import static gui.MsgColor.CYAN;

public class App {
    public final HashMap<String, Chat> chats = new HashMap<>();

    public App(long surveyTimeoutInMinutes) {
        TerminalGUI.clear();
        ChatUtil.greet();

        TerminalGUI.printLn("Seu nome de usuário:");
        String username = TerminalGUI.read(50);
        TerminalGUI.printLnInfo("Nome de usuário: " + username);

        TerminalGUI.printLn("Nome do chat:");
        String chatname = TerminalGUI.read(50);
        TerminalGUI.printLnInfo("Chat: " + chatname);

        // Remove logs
        LogFactory.setCustomLogFactory(new app.LogFactory()); // Omit logs

        Chat newChat = new Chat(this, username, chatname, true, surveyTimeoutInMinutes);

        this.chats.put(chatname, newChat);

        newChat.activate();
    }
}
