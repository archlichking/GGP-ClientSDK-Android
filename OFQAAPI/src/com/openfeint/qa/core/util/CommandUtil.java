
package com.openfeint.qa.core.util;

public class CommandUtil {
    public enum Command {
        GIVEN, WHEN, THEN, AND, BEFORE, AFTER, COMMAND, NONE;
        public static Command toCommand(String comm) {
            return valueOf(Command.class, comm);
        }
    }

    public static String GIVEN_COM = "GIVEN";

    public static String WHEN_COM = "WHEN";

    public static String THEN_COM = "THEN";

    public static String AND_COM = "AND";

    public static String CMD_COM = "COMMAND";

    public static String BEFORE_COM = "BEFORE";

    public static String AFTER_COM = "AFTER";

    public static String GIVEN_FILTER = "Given";

    public static String WHEN_FILTER = "When";

    public static String THEN_FILTER = "Then";

    public static String AND_FILTER = "And";

    public static String BEFORE_FILTER = "Before";

    public static String AFTER_FILTER = "After";

    public static String CMD_FILTER = "Cmd";

}
