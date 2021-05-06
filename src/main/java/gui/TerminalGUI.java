package gui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static gui.MsgColor.*;
import static java.lang.System.exit;

public class TerminalGUI {

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
        return new String(input_bytes, 0, read - 1, StandardCharsets.UTF_8);
    }

    public static void printLn(MsgColor color, String msg, boolean underline) {
        String ansiCode = underline ? color.ansiCode_underline : color.ansiCode;
        System.out.println(ansiCode + msg + "\u001B[0m");
    }

    public static void printLn(MsgColor color, String msg) { TerminalGUI.printLn(color, msg, false); }
    public static void printLn(String msg) { TerminalGUI.printLn(WHITE, msg); }
    public static void printLnInfo(String msg) { TerminalGUI.printLn(CYAN, "[ INFO ] " + msg); }
    public static void printLnSuccess(String msg) { TerminalGUI.printLn(GREEN, "[ SUCCESSO ] " + msg); }
    public static void printLnError(String msg) { TerminalGUI.printLn(RED, "[ ERRO ] " + msg); }
}
