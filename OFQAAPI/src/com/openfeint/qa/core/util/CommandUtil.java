
package com.openfeint.qa.core.util;

public class CommandUtil {
    public enum Command {
        GIVEN, WHEN, THEN, AND, NONE;
        public static Command toCommand(String comm) {
            return valueOf(Command.class, comm);
        }
    }

    public static String GIVEN_COM = "GIVEN";

    public static String WHEN_COM = "WHEN";

    public static String THEN_COM = "THEN";

    public static String AND_COM = "AND";

    public static String GIVEN_FILTER = "Given";

    public static String WHEN_FILTER = "When";

    public static String THEN_FILTER = "Then";

    public static String AND_FILTER = "And";

}
