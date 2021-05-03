package app;

import gui.TerminalGUI;
import org.jgroups.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static gui.MsgColor.*;
import static java.lang.System.exit;

public class Chat extends ReceiverAdapter {
    private boolean isActive;
    private final String username;
    private final String chatname;
    private final App app;

    private JChannel channel;
    private View lastView;

    public Chat(App app, String username, String chatname, boolean isActive) {
        lastView = new View(new ViewId(), new ArrayList<>());
        this.app = app;
        this.username = username;
        this.chatname = chatname;
        this.isActive = isActive;
    }

    public void activate() {
        this.isActive = true;
        TerminalGUI.clear();
        TerminalGUI.printLnInfo("Activating chat '" + this.chatname + "'");

        // Conecta no chat escolhido
        try {
            this.channel = new JChannel().setReceiver(this);
        } catch (Exception e) {
            TerminalGUI.printLnError(e.getMessage());
            return;
        }
        this.channel.setName(this.username);
        try {
            this.channel.connect(chatname);
        } catch (Exception e) {
            TerminalGUI.printLnError(e.getMessage());
        }

        TerminalGUI.clear();
        TerminalGUI.printLnInfo("You've entered chat '" + this.chatname + "'");

        while (true) {
            String userInput = TerminalGUI.read(100);
            if (userInput.length() == 0)
                continue;

            if (isCommand(userInput)) {
                boolean shouldQuit = handleCommand(userInput);
                if (shouldQuit)
                    break;

                continue;
            }

            if (this.channel == null) {
                TerminalGUI.printLnError("Você não está conectado a nenhum chat");
                continue;
            }

            Message msg = new Message(null, userInput);
            try {
                this.channel.send(msg);
            } catch (Exception e) {
                TerminalGUI.printLnError(e.getMessage());
            }
        }

        exit(0);
    }

    private boolean handleCommand(String command) {
        Commands c;
        try {
            c = Commands.fromString(command);
        } catch (Exception e) {
            TerminalGUI.printLnError(e.getMessage());
            return false;
        }
        if (c == null) {
            TerminalGUI.printLnError("Comando '" + command + "' desconhecido");
            return false;
        }

        switch (c) {
            case ENTER:
                this.isActive = false;
                this.app.chats
                        .computeIfAbsent(c.argument, k -> new Chat(this.app, this.username, k, true))
                        .activate();
                return false;
            case QUIT:
                quit();
                return true;
            case MEMBERS:
                getMembers();
                break;
            case DISCONNECT:
                disconnect();
                break;
            case PRIVATE_MSG:
                String[] args = c.argument.split(" ", 2);
                if (args.length != 2) {
                    TerminalGUI.printLnError("You must pass a destiny and a message, look: !private <dest_name> <msg>");
                    break;
                }

                String dest = args[0];
                String msg = args[1];

                Address addr = this.getAddress(dest);
                if (addr == null) {
                    TerminalGUI.printLnError("There's no user named '" + dest + "'. Fix it and try again");
                    break;
                }

                if (msg.length() == 0) {
                    TerminalGUI.printLnError("The message is empty");
                    break;
                }
                msg = "(PRIVATE) " + msg;

                try {
                    this.channel.send(new Message(addr, msg));
                } catch (Exception e) {
                    TerminalGUI.printLnError(e.getMessage());
                }
                TerminalGUI.printLn(WHITE, this.username + ": " + msg);
                break;
        }
        return false;
    }

    private Address getAddress(String name) {
        View view = channel.view();
        return view.getMembers().stream()
                .filter(address -> name.equals(address.toString()))
                .findAny().orElse(null);
    }


    private void getMembers() {
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
        TerminalGUI.printLnInfo(membersNames.toString());
    }

    private void disconnect() {
        this.channel.disconnect();
        if (!this.channel.isConnected())
            TerminalGUI.printLnSuccess("Desconectado do Chat");
        else
            TerminalGUI.printLnError("Nao foi possivel desconectar do Chat");
    }

    private void quit() {
        this.channel.close();
        if (this.channel.isClosed())
            TerminalGUI.printLnSuccess("Você saiu do Chat");
    }

    @Override
    public void viewAccepted(View newView) {
        if (!isActive)
            return;

        if (newView != null) {
            List<Address> newMembers = View.newMembers(lastView, newView);
            List<Address> exitedMembers = View.leftMembers(lastView, newView);

            newMembers = newMembers.stream()
                    .filter(a -> !a.equals(channel.getAddress()))
                    .collect(Collectors.toList()); // Removing myself of that report

            if (!newMembers.isEmpty()) {
                String msg = newMembers.size() > 1 ? "Novos membros entraram: ": "Um novo membro entrou: ";
                TerminalGUI.printLnInfo(msg + newMembers.toString());
            }
            if (!exitedMembers.isEmpty()) {
                String msg = exitedMembers.size() > 1 ? "Alguns membros saíram: ": "Um membro saiu: ";
                TerminalGUI.printLnInfo(msg + exitedMembers.toString());
            }
        }
        lastView = newView;
    }

    @Override
    public void receive(Message msg) {
        if (!isActive)
            return;

        TerminalGUI.printLn(WHITE, msg.getSrc().toString() + ": " +  msg.getObject());
    }

    private boolean isCommand(String msg) { return msg.charAt(0) == '!'; }
}
