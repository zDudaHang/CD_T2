package app;

import gui.TerminalGUI;
import org.jgroups.*;
import org.jgroups.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static gui.MsgColor.*;
import static java.lang.System.exit;

public class Chat extends ReceiverAdapter {
    private JChannel channel;
    private View lastView;
    private final TerminalGUI gui;
    private final String username;

    public Chat(TerminalGUI gui, String username, String chatname) {
        lastView = new View(new ViewId(), new ArrayList<>());
        this.gui = gui;
        this.username = username;

        boolean shouldQuit = false;
        do {
            String userInput = this.gui.readInput();
            if (userInput.length() == 0)
                continue;

            if (isCommand(userInput)) {
                shouldQuit = handleCommand(userInput);
                continue;
            }
            if (this.channel == null) {
                this.printError("Você não está conectado a nenhum chat");
                continue;
            }

            Message msg = new Message(null, userInput);
            try {
                this.channel.send(msg);
            } catch (Exception e) {
                this.printError(e.getMessage());
                continue;
            }

            this.printUserMsg(this.username, userInput);
        } while (!shouldQuit);
        exit(0);
    }

    private boolean handleCommand(String command) {
        Commands c;
        try {
            c = Commands.fromString(command);
        } catch (Exception e) {
            this.printError(e.getMessage());
            return false;
        }
        if (c == null) {
            this.printError("Comando '" + command + "' desconhecido");
            return false;
        }

        switch (c) {
            case QUIT:
                quit();
                return true;
            case MEMBERS:
                getMembers();
                break;
            case DISCONNECT:
                disconnect();
                break;
        }
        return false;
    }

    private void connectToChat(String chatName) {
        if (this.channel != null && this.channel.isConnected()) {
            printError("Para entrar em outro chat, se desconecte do atual usando !desconectar");
            return;
        }

        LogFactory.setCustomLogFactory(new app.LogFactory()); // Omitir logs
        try {
            this.channel = new JChannel().setReceiver(this);
        } catch (Exception e) {
            this.printError(e.getMessage());
            return;
        }

        this.channel.setName(this.username);
        try {
            this.channel.connect(chatName);
        } catch (Exception e) {
            this.printError(e.getMessage());
        }
    }

    private void getMembers() {
        if (this.channel == null) {
            this.printError("Você precisa conectar a um Chat antes de poder ver os membros.");
            return;
        }

        List<Address> members = this.lastView.getMembers();
        List<String> membersNames = new ArrayList<>();
        members.forEach(a -> {
            String name = a.toString();
            if (a.equals(channel.getAddress())) {
                name += " (Você)";
            }
            if (a.equals(lastView.getCoord())) {
                name += " (Admin)";
            }
            membersNames.add(name);
        });
        this.printInfo(membersNames.toString());
    }

    private void disconnect() {
        if (this.channel == null) {
            this.printError("Precisa estar conectado a um Chat");
            return;
        }

        this.channel.disconnect();
        if (!this.channel.isConnected())
            this.printSuccess("Desconectado do Chat");
        else
            this.printError("Nao foi possivel desconectar do Chat");
    }

    private void quit() {
        if (this.channel != null) {
            this.channel.close();

            if (this.channel.isClosed())
                this.printSuccess("Você saiu do Chat");
        }
    }

    @Override
    public void viewAccepted(View newView) {
        if (newView != null) {
            List<Address> newMembers = View.newMembers(lastView, newView);
            List<Address> exitedMembers = View.leftMembers(lastView, newView);

            newMembers = newMembers.stream()
                    .filter(a -> !a.equals(channel.getAddress()))
                    .collect(Collectors.toList()); // Removing myself of that report

            if (!newMembers.isEmpty()) {
                String msg = newMembers.size() > 1 ? "Novos membros entraram: ": "Um novo membro entrou: ";
                this.printInfo(msg + newMembers.toString());
            }
            if (!exitedMembers.isEmpty()) {
                String msg = exitedMembers.size() > 1 ? "Alguns membros saíram: ": "Um membro saiu: ";
                this.printInfo(msg + exitedMembers.toString());
            }
        }
        lastView = newView;
    }

    @Override
    public void receive(Message msg) { this.printUserMsg(msg.getSrc().toString(), msg.getObject()); }

    private boolean isCommand(String msg) { return msg.charAt(0) == '!'; }
    private void printUserMsg(String username, String msg) { gui.printLn(YELLOW, "[ " + username + " ]: " + msg); }
    private void printInfo(String msg) { gui.printLn(CYAN, "--> " + msg); }
    private void printSuccess(String msg) { gui.printLn(GREEN, "--> " + msg); }
    private void printError(String msg) { gui.printLn(RED, "--> " + msg); }
}
