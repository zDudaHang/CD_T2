package app;

import gui.TerminalGUI;
import model.Survey;
import model.SurveyThread;
import util.ChatUtil;

import org.jgroups.*;
import org.jgroups.blocks.atomic.Counter;
import org.jgroups.blocks.atomic.CounterService;
import org.jgroups.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private CounterService service;
    private static final String SURVEYS_COUNTER = "surveyCounters";

    public Chat(App app, String username, String chatname, boolean isActive) {
        lastView = new View(new ViewId(), new ArrayList<>());
        this.app = app;
        this.username = username;
        this.chatname = chatname;
        this.isActive = isActive;
    }

    public void activate() {
        isActive = true;
        TerminalGUI.clear();

        try {
            channel = new JChannel("/home/bridge/CD_T2/src/main/resources/config.xml").setReceiver(this);
        } catch (Exception e) {
            TerminalGUI.printLnError(e.getMessage());
            return;
        }

        channel.setName(username);

        try {
            channel.connect(chatname);
        } catch (Exception e) {
            TerminalGUI.printLnError(e.getMessage());
        }

        service = new CounterService(channel);

        if (isCoord())
            service.getOrCreateCounter(SURVEYS_COUNTER, 0);


        TerminalGUI.clear();
        TerminalGUI.printLnInfo("Você entrou no chat '" + chatname + "'");

        handleUserInput();
    }

    private void handleUserInput() {
        while (true) {
            String userInput = TerminalGUI.read(100);

            assert userInput != null;

            if (userInput.length() == 0)
                continue;

            if (ChatUtil.isCommand(userInput)) {
                boolean shouldQuit = handleCommand(userInput);
                if (shouldQuit)
                    break;
                continue;
            }

            if (channel == null) {
                TerminalGUI.printLnError("Você não está conectado a nenhum chat");
                continue;
            }

            sendMessage(new Message(null, userInput));
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
                isActive = false;
                app.chats
                        .computeIfAbsent(c.argument, k -> new Chat(app, username, k, true))
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

                sendMessage(new Message(addr, "(PRIVATE) " + msg));

                TerminalGUI.printLn(WHITE, username + ": " + msg);

            case SURVEY:
                makeSurvey(c.argument);
                break;
            case VOTE:
                vote(c.argument);
                break;
        }
        return false;
    }

    private void sendMessage(Message msg) {
        try {
            channel.send(msg);
        } catch (Exception e) {
            TerminalGUI.printLnError(e.getMessage());
        }
    }

    private Address getAddress(String name) {
        View view = channel.view();
        return view.getMembers().stream()
                .filter(address -> name.equals(address.toString()))
                .findAny().orElse(null);
    }

    private void getMembers() {
        List<Address> members = lastView.getMembers();
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
        channel.disconnect();
        if (!channel.isConnected())
            TerminalGUI.printLnSuccess("Desconectado do Chat");
        else
            TerminalGUI.printLnError("Nao foi possivel desconectar do Chat");
    }

    private void quit() {
        channel.close();
        if (channel.isClosed())
            TerminalGUI.printLnSuccess("Você saiu do Chat");
    }

    private void makeSurvey(String args) {
        String[] splittedArgs = args.split(" ", 2);

        if (splittedArgs.length < 2) {
            TerminalGUI.printLnError("É necessário um título e uma lista opções para mandar uma enquete");
            return;
        }

        String title = splittedArgs[0];
        String options = splittedArgs[1];

        List<String> splitedOptions = Arrays.asList(options.split(","));

        if (splitedOptions.size() <= 1) {
            TerminalGUI.printLnError("É necessário mais de uma opção para ser uma enquete");
            return;
        }

        Survey survey = new Survey(title, splitedOptions);

        Counter count = service.getOrCreateCounter(SURVEYS_COUNTER, -1);

        long idx = count.get();

        if (idx < 0) {
            TerminalGUI.printLnError("Não foi possível criar uma enquete no momento. Tente novamente mais tarde.");
            return;
        }

        for (int i = 0; i < splitedOptions.size(); i++) {
            service.getOrCreateCounter(ChatUtil.createCounterName(idx, i), 0);
        }

        sendMessage(new Message(null,"Criei uma nova enquete!\n"
                + survey.toString()
                + "Para votar nela, digite: !votar " + idx + " <opcao>\n"
                + "Ela acabará em 1 minuto!\n"
                )
        );

        count.incrementAndGet();
        
        SurveyThread t = new SurveyThread(service, idx, survey, 1, SURVEYS_COUNTER, channel);
        t.start();

    }

    public void vote(String args) {
        String[] splittedArgs = args.split(" ", 2);

        if (splittedArgs.length < 2) {
            TerminalGUI.printLnError("É necessário o número da enquete e qual a opção que você votou");
            return;
        }

        long survey = Long.parseLong(splittedArgs[0]);

        Counter surveyCounter = service.getOrCreateCounter(SURVEYS_COUNTER, -1);

        long numOfSurveys = surveyCounter.get();

        //      Was possible to acess the surveys counter?
        if (numOfSurveys < 0) {
            TerminalGUI.printLnError("Não foi possível ver enquete no momento");
            return;
        }

        //      Verify if is a valid number of a survey
        if (survey > numOfSurveys - 1) {
            TerminalGUI.printLnError("A enquete de número " + survey + " não existe.");
            return;
        }

        int option = Integer.parseInt(splittedArgs[1]);

        String counterName = ChatUtil.createCounterName(survey, option);

        Counter count = service.getOrCreateCounter(counterName, -1);

        long value = count.get();

        if (value < 0) {
            TerminalGUI.printLnError("A enquete de número " + survey + " não tem a opção " + option);
            service.deleteCounter(counterName);
            return;
        }

        count.incrementAndGet();

        TerminalGUI.printLnSuccess("Voto contabilizado!");

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

    public boolean isCoord() {
        return channel.getAddress() == channel.getView().getCoord();
    }

    @Override
    public void receive(Message msg) {
        if (!isActive)
            return;
        TerminalGUI.printLn(WHITE, msg.getSrc().toString() + ": " +  msg.getObject());
    }
}
