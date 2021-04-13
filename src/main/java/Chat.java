import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.ViewId;

public class Chat extends ReceiverAdapter {
	private JChannel channel;
	private View lastView;
	private BufferedReader in;

	public Chat() {
		channel = null;
		lastView = new View(new ViewId(), new ArrayList<>());
		in = new BufferedReader(new InputStreamReader(System.in));
	}

	private void connect() throws Exception {
		sayWelcome();

		channel = new JChannel().setReceiver(this);

		String clusterName = "";
		String username = "";
		try {
			System.out.println("Qual o nome do chat que você deseja entrar?");
			clusterName = in.readLine();
			System.out.println("Qual o seu nome?");
			username = in.readLine();

		} catch(Exception e) {
			e.printStackTrace();
		}

		channel.setName(username);
		channel.connect(clusterName);

		eventLoop();

		channel.close();
	}

	private void sayWelcome() {
		System.out.printf("Seja bem-vindo au UFSCzap! A seguir alguns comandos para o programa:\n"
				+ "!entrar: Se conectar com um chat\n"
				+ "!sair: Sair do programa\n"
				+ "!membros: Ver os membros do chat atual\n"
				+ "!desconectar: Sair do chat atual\n");

	}

	private void eventLoop() {
		while(true) {
			try {
				String line=in.readLine().toLowerCase();
				if(line.startsWith("quit") || line.startsWith("exit"))
					break;
				Message msg = new Message(null, line);
				channel.send(msg);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void viewAccepted(View newView) {
		if (newView != null) {
			List<Address> newMembers = View.newMembers(lastView, newView);
			List<Address> exitedMembers = View.leftMembers(lastView, newView);
			newMembers = newMembers.stream().filter(a -> !a.equals(channel.getAddress())).collect(Collectors.toList()); // Removing myself of that report
			if (!newMembers.isEmpty()) {
				String msg = newMembers.size() > 1 ? "Novos membros entraram": "Um novo membro entrou";
				System.out.printf("[INFO] %s :) ! ", msg);
				System.out.println(newMembers.toString());
			}

			if (!exitedMembers.isEmpty()) {
				String msg = exitedMembers.size() > 1 ? "Alguns membros saíram ": "Um membro saiu ";
				System.out.printf("[INFO] %s :( ! ", msg);
				System.out.println(exitedMembers.toString());
			}
		}
		lastView = newView;
	}

	@Override
	public void receive(Message msg) {
		String line=msg.getSrc() + ": " + msg.getObject();
		System.out.println(line);
	}


	public static void main(String[] args) throws Exception {

		new Chat().connect();

	}
}