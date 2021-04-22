package gui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static gui.MsgColor.*;
import static java.lang.String.format;
import static java.lang.System.exit;

public class TerminalGUI {
    private static final String chatUserHeader =
            "_________________________________________________________________\n" +
            "[ %s ] %s: ";

    public static void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static String read(int size) {
        byte[] input_bytes = new byte[size];
        int read;
        try {
            read = System.in.read(input_bytes);
        } catch (IOException e) {
            TerminalGUI.printLnError(e.getMessage());
            exit(1);
            return null; // completion only
        }

        // Remove o "\n" do final
        return new String(input_bytes, 0, read - 1, StandardCharsets.US_ASCII);
    }

    public static String readWithHeader(String header, int size) {
        // Desenha header escolhido
        System.out.print(header);

        // Pega o input do usuario
        String input = TerminalGUI.read(size);

        // Limpa o header
        int lines = header.split("\n").length;
        while (lines > 0) {
            System.out.print("\033[1F"); // Posiciona cursor na linha de cima
            System.out.print("\033[2K"); // Limpa a linha
            lines -= 1;
        }

        return input;
    }

    public static String readFromChatUser(String chatname, String username, int size) {
        return TerminalGUI.readWithHeader(format(chatUserHeader, chatname, username), size);
    }

    public static String readFromChatUser(String chatname, String username) {
        return TerminalGUI.readFromChatUser(chatname, username, 100);
    }

    public static void printLn(MsgColor color, String msg) {
        System.out.println(color.ansiCode + msg + "\u001B[0m"); // Reseta cor no final
    }

    public static void printLnUserMsg(MsgColor color, String username, String msg) {
        TerminalGUI.printLn(color, username + ": " + msg);
    }

    public static void printLnChatUserMsg(MsgColor color, String chatname, String username, String msg) {
        TerminalGUI.printLn(color, "[ " + chatname + " ] " + username + ": " + msg);
    }

    public static void printLnInfo(String msg) { TerminalGUI.printLn(CYAN, "[ INFO ] " + msg); }
    public static void printLnSuccess(String msg) { TerminalGUI.printLn(GREEN, "[ SUCCESS ] " + msg); }
    public static void printLnError(String msg) { TerminalGUI.printLn(RED, "[ ERROR ] " + msg); }
}
