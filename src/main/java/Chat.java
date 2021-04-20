import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.ViewId;

import Enum.Commands;
import Log.Log;

public class Chat extends ReceiverAdapter {
	private JChannel channel;
	private View lastView;
	private final BufferedReader in;
	private String username;

	public Chat() {
		channel = null;
		lastView = new View(new ViewId(), new ArrayList<>());
		in = new BufferedReader(new InputStreamReader(System.in));

		sayWelcome();

		try {
			Log.request("Qual o seu nome de usuário?");
			this.username = in.readLine();

		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	private boolean isCommand(String msg) {
		return msg.charAt(0) == '!';
	}

	private void connect() throws Exception {
		if (channel != null && channel.isConnected()) {
			Log.error("Se você deseja entrar em outro chat, primeiro se desconecte do atual com o comando !desconectar.");
			return;
		}

		channel = new JChannel().setReceiver(this);

		String clusterName = "";

		try {
			Log.request("Qual o nome do chat que você deseja entrar?");
			clusterName = in.readLine();

		} catch(Exception e) {
			e.printStackTrace();
		}

		channel.setName(username);
		channel.connect(clusterName);
	}

	private void getMembers() {
		if (channel == null) {
			Log.error("Você precisa conectar a um Chat antes de poder ver os membros.");
			return;
		}

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

		Log.info(membersNames.toString());
	}

	private void disconnect() {
		if (channel == null) {
			Log.error("Você precisa conectar a um Chat antes de poder desconectar.");
			return;
		}

		channel.disconnect();

		if (!channel.isConnected()) {
			Log.success("Desconectado do Chat.");
		}
	}

	private void quit() {
		if (channel != null) {
			channel.close();

			if (channel.isClosed()) {
				Log.success("Você saiu do Chat.");
			}
		}
	}

	private void sayWelcome() {
		System.out.print("Seja bem-vindo ao UFSCzap!\n"
				+ "A seguir alguns comandos para o programa:\n"
				+ "!entrar: Se conectar com um chat\n"
				+ "!sair: Sair do programa\n"
				+ "!membros: Ver os membros do chat atual\n"
				+ "!desconectar: Sair do chat atual\n");

	}

	private void receiveUserCommands() {
		boolean wantToQuit = false;
		do {
			try {
				String line = in.readLine().toLowerCase();

				if(isCommand(line)) {
					Commands c = Commands.fromString(line);

					if (c == null) {
						Log.error("Comando " + line + " desconhecido");
						continue;
					}

					switch (c) {
						case CONNECT:
							connect();
							break;
						case QUIT:
							quit();
							wantToQuit = true;
							break;
						case MEMBERS:
							getMembers();
							break;
						case DISCONNECT:
							disconnect();
							break;
					}
				} else {
					Message msg = new Message(null, line);
					channel.send(msg);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		} while (!wantToQuit);
		System.exit(0);
	}

	@Override
	public void viewAccepted(View newView) {
		if (newView != null) {
			List<Address> newMembers = View.newMembers(lastView, newView);
			List<Address> exitedMembers = View.leftMembers(lastView, newView);

			newMembers = newMembers.stream().filter(a -> !a.equals(channel.getAddress())).collect(Collectors.toList()); // Removing myself of that report

			if (!newMembers.isEmpty()) {
				String msg = newMembers.size() > 1 ? "Novos membros entraram: ": "Um novo membro entrou: ";
				Log.info(msg + newMembers.toString());
			}

			if (!exitedMembers.isEmpty()) {
				String msg = exitedMembers.size() > 1 ? "Alguns membros saíram: ": "Um membro saiu: ";
				Log.info(msg + exitedMembers.toString());
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
		new Chat().receiveUserCommands();
	}
}