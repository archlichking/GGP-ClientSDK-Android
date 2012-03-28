
package com.openfeint.qa.core;

import com.openfeint.qa.core.caze.step.StepPair;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.regex.Matcher;

public class Scanner {
    public static Method scanMethod(Method m) {
        return m;
    }

    public static Type[] scanParameterTypes(Method m) {
        return m.getGenericParameterTypes();
    }

    public static Object[] scanParameterValues(StepPair sp, String inst) {
        ArrayList<String> raw_values = new ArrayList<String>();

        Matcher matcher = sp.getP().matcher(inst);
        matcher.matches();
        for (int i = 1; i <= matcher.groupCount(); i++) {
            raw_values.add(matcher.group(i));
        }
        return raw_values.toArray();
    }

    public static Class<?> scanClass(Method m) {
        return m.getDeclaringClass();
    }
}
