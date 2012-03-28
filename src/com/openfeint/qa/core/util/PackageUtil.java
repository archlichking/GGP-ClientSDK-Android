
package com.openfeint.qa.core.util;

import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

public class PackageUtil {
    private static final String DEFINITION_SUFFIX = "StepDefinitions";

    private static Class<?>[] getStepClass(String packageName, String apkPath)
            throws ClassNotFoundException, IOException {
        ArrayList<Class<?>> temp = new ArrayList<Class<?>>();
        DexFile dexFile = null;
        try {
            dexFile = new DexFile(apkPath);
            Enumeration<String> apkClassNames = dexFile.entries();
            while (apkClassNames.hasMoreElements()) {
                String className = apkClassNames.nextElement();
                if (className.startsWith(packageName) && className.endsWith(DEFINITION_SUFFIX)) {
                    Log.v(StringUtil.DEBUG_TAG, "reading step [" + className + "]");
                    temp.add(Class.forName(className));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp.toArray(new Class[temp.size()]);
    }

    public static List<Method> getAllStepMethods(String packageName, String apkPath)
            throws ClassNotFoundException, IOException {
        ArrayList<Method> methods = new ArrayList<Method>();
        Class<?>[] clazzes = getStepClass(packageName, apkPath);
        for (Class<?> c : clazzes) {
            for (Method m : c.getDeclaredMethods()) {
                methods.add(m);
            }
        }
        return methods;
    }
}
