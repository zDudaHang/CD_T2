package app;

import gui.TerminalGUI;
import model.Survey;
import model.SurveyThread;
import org.jgroups.*;
import org.jgroups.blocks.atomic.Counter;
import org.jgroups.blocks.atomic.CounterService;
import util.ChatUtil;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static gui.MsgColor.WHITE;
import static java.lang.System.exit;

public class Chat extends ReceiverAdapter{
    private boolean isActive;
    private final String username;
    private final String chatname;
    private final App app;

    private JChannel channel;
    private View lastView;
    private CounterService service;
    private static final String SURVEYS_COUNTER = "surveyCounters";

//    Manage the votes of the user
    private final List<Long> votes = new ArrayList<>();

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
            InputStream configXML = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.xml");
            channel = new JChannel(configXML).setReceiver(this);
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
            TerminalGUI.printLnError("Comando '" + command + "' desconhecido. Caso não lembre dos comandos, digite !ajuda.");
            return false;
        }

        switch (c) {
            case ENTER:
                isActive = false;
                app.chats
                        .computeIfAbsent(c.argument, k -> new Chat(app, username, k, true))
                        .activate();
                break;
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
                sendPrivateMessage(c.argument);
                break;
            case SURVEY:
                makeSurvey(c.argument);
                break;
            case VOTE:
                vote(c.argument);
                break;
            case HELP:
                ChatUtil.showCommands();
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
            if (isMe(a)) {
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
        String[] splittedArgs = args.split(" ", 3);

        if (splittedArgs.length != 3) {
            TerminalGUI.printLnError("É necessário um timeout (em minutos), título e uma lista opções para mandar uma enquete");
            return;
        }

        long timeout = Long.parseLong(splittedArgs[0]);
        String title = splittedArgs[1];
        String options = splittedArgs[2];

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
                + "Ela acabará em " + timeout + " minuto(s)!\n"
                )
        );

        count.incrementAndGet();

        SurveyThread t = new SurveyThread(service, idx, survey, timeout, SURVEYS_COUNTER, channel);
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

        //       Verify if the user already voted on this survey
        if (votes.contains(survey)) {
            TerminalGUI.printLnError("Você já votou nessa enquete.");
            return;
        }

        int option = Integer.parseInt(splittedArgs[1]);

        String counterName = ChatUtil.createCounterName(survey, option);

        Counter count = service.getOrCreateCounter(counterName, -1);

        long value = count.get();

        //      Verify if the user don't send a invalid option
        if (value < 0) {
            TerminalGUI.printLnError("A enquete de número " + survey + " não tem a opção " + option);
            service.deleteCounter(counterName);
            return;
        }

        count.incrementAndGet();

        votes.add(survey);

        TerminalGUI.printLnSuccess("Voto contabilizado!");
    }

    private void sendPrivateMessage(String args) {
        String[] splittedArgs = args.split(" ", 2);
        if (splittedArgs.length != 2) {
            TerminalGUI.printLnError("Você deve passar o nome do usuário e a mensagem. Tente novamente com o comando: !priv <nome> <mensagem>");
            return;
        }

        String dest = splittedArgs[0];
        String msg = splittedArgs[1];

        Address addr = this.getAddress(dest);
        if (addr == null) {
            TerminalGUI.printLnError("Não existe um usuário com o nome '" + dest + "'. Tente novamente com outro nome.");
            return;
        }

        if (msg.length() == 0) {
            TerminalGUI.printLnError("A mensagem está vazia!");
            return;
        }

        sendMessage(new Message(addr, "!PrivateMSG#" + msg));

        TerminalGUI.printLn(WHITE, "Você" + ": " + msg);
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

    private void removeVote(Long surveyId) {
        votes.remove(surveyId);
    }
    
    private boolean isMe(Address addr) {
        return channel.getAddress().equals(addr);
    }

    @Override
    public void receive(Message msg) {
        if (!isActive)
            return;

        String content = msg.getObject();
        Address source = msg.getSrc();
        String nameSource = isMe(source) ? "Você" : source.toString();

        if (ChatUtil.isCommand(content)) {
            String[] contents = content.split("#", 2);
            if (contents[0].startsWith("!Remove")) {
                removeVote(Long.parseLong(contents[1]));
                return;
            }
            if (contents[0].startsWith("!PrivateMSG")) {
                TerminalGUI.printLn(WHITE, nameSource + ": (PRIVADO) " +  contents[1], true);
            }
        } else {
            TerminalGUI.printLn(WHITE, nameSource + ": " +  content);
        }

    }
}
