
package com.openfeint.qa.core.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class StringUtil {
    public final static String FILE_LINE_SPLIT = "\n";

    public final static String TCM_LINE_SPLIT = "\r\n";

    public final static String SPACE = " ";

    public final static String DEBUG_TAG = "OFQA";

    public static String extractTitle(String str) {
        return str.replace("Scenario: ", "");
    }

    public static String[] splitSteps(String str, String split) {
        return str.split(split);
    }

    public static String[] extractSteps(String[] str) {
        ArrayList<String> temp = new ArrayList<String>(Arrays.asList(str));
        int i = 0;
        // keep only GIVEN, THEN, WHEN
        while (i < temp.size()) {
            String s = temp.get(i).replace("\t", "");
            if (!s.startsWith(CommandUtil.GIVEN_FILTER) 
                    && !s.startsWith(CommandUtil.WHEN_FILTER)
                    && !s.startsWith(CommandUtil.THEN_FILTER)
                    && !s.startsWith(CommandUtil.AND_FILTER)
                    && !s.startsWith(CommandUtil.BEFORE_FILTER)
                    && !s.startsWith(CommandUtil.AFTER_FILTER)
                    && !s.startsWith(CommandUtil.CMD_FILTER)) {
                temp.remove(i);
            } else {
                temp.set(i, s);
                i++;
            }
        }
        return temp.toArray(new String[temp.size()]);
    }

    public static String getFirstWord(String str) {
        return str.split(SPACE)[0];
    }

    public static String getRestWord(String str, String del) {
        return str.replace(del + " ", "");
    }

    public static Properties buildProperties(String config) {
        Properties p = new Properties();

        try {
            StringReader sr = new StringReader(config);
            p.load(sr);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch(NullPointerException npe){
            // do nothing, used by plainhttpexception
        }
        return p;
    }
}
