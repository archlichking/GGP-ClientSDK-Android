
package com.openfeint.qa.core.caze.builder;

import android.content.Context;

import com.openfeint.qa.core.caze.TestCase;
import com.openfeint.qa.core.caze.step.StepParser;
import com.openfeint.qa.core.exception.CaseBuildFailedException;

public abstract class CaseBuilder {
    protected String step_mode;

    protected StepParser parser;

    public CaseBuilder(String packageName, Context contxt) {
        parser = new StepParser(packageName, contxt.getPackageCodePath());
    }

    public abstract TestCase[] buildCases(String suite_id) throws CaseBuildFailedException;

    public abstract TestCase buildCase(String suite_id, String id) throws CaseBuildFailedException;

}
