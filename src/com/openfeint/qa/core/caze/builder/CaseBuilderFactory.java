
package com.openfeint.qa.core.caze.builder;

import android.content.Context;

public class CaseBuilderFactory {
    public static final int TCM_BUILDER = 0;

    public static final int FILE_BUILDER = 1;

    public static CaseBuilder makeBuilder(int type, String type_raw, String packageName,
            Context context) {
        switch (type) {
            case TCM_BUILDER:
                return new TCMCaseBuilder(type_raw, packageName, context);
            case FILE_BUILDER:
                return new FileCaseBuilder(type_raw, packageName, context);
            default:
                return null;
        }
    }
}
