package gui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TerminalGUI {
    private static String chatName = "---";
    private static final String inputBox =
            "_________________________________________________________________\n" +
            "[ %s ] %s: ";

    private String username;
    private int maxMsgSize = 100;

    public TerminalGUI(String username, int maxMsgSize) {
        this.username = username;
        this.maxMsgSize = maxMsgSize;
    }

    // -----

    public void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public String readInput() {
        byte[] input_bytes = new byte[this.maxMsgSize];
        try {
            System.in.read(input_bytes);
        } catch (IOException e) {
            return "## ERROR: Could not read input message ##";
        }
        System.out.print("\033[1F"); // Move cursor para linha de cima
        return new String(input_bytes, StandardCharsets.US_ASCII);
    }

    public void printLn(MsgColor color, String msg) {
        // Limpa linha atual, move cursor para
        // cima e limpa a linha de cima
        System.out.print("\033[2K\033[1F\033[2K");

        System.out.println(color.ansiCode + msg + "\u001B[0m");
        System.out.printf(TerminalGUI.inputBox, TerminalGUI.chatName, this.username);
    }
}
