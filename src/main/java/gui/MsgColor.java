package gui;

public enum MsgColor {
    BLACK("\u001B[30m", "\u001B[4;30m"),
    RED("\u001B[31m", "\u001B[4;31m"),
    GREEN("\u001B[32m", "\u001B[4;32m"),
    YELLOW("\u001B[33m", "\u001B[4;33m"),
    BLUE("\u001B[34m", "\u001B[4;34m"),
    PURPLE("\u001B[35m", "\u001B[4;35m"),
    CYAN("\u001B[36m", "\u001B[4;36m"),
    WHITE("\u001B[37m", "\u001B[4;37m");

    // -----

    MsgColor(String ansiCode, String ansiCode_underline) {
        this.ansiCode = ansiCode;
        this.ansiCode_underline = ansiCode_underline;
    }
    String ansiCode;
    String ansiCode_underline;
}
