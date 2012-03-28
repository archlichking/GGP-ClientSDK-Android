
package com.openfeint.qa.core.caze.builder;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import com.openfeint.qa.core.caze.TestCase;
import com.openfeint.qa.core.caze.step.Step;
import com.openfeint.qa.core.exception.NoSuchStepException;
import com.openfeint.qa.core.util.StringUtil;

public class FileCaseBuilder extends CaseBuilder {
    private String type_raw;

    public FileCaseBuilder(String type_raw, String packageName, Context context) {
        super(packageName, context);
        this.type_raw = type_raw;
    }

    @Override
    public TestCase buildCase(String suite_id, String id) {
        return buildFromSampleTest();
    }

    private TestCase buildFromSampleTest() {
        String all = this.type_raw;
        String[] raw_case = StringUtil.splitSteps(all, StringUtil.FILE_LINE_SPLIT);
        String[] raw_steps = StringUtil.extractSteps(raw_case);

        // pase case steps
        ArrayList<Step> steps = new ArrayList<Step>();
        for (String step : raw_steps) {
            try {
                steps.add(this.parser.parse(step));
            } catch (NoSuchStepException nsse) {
                Log.e(StringUtil.DEBUG_TAG, nsse.getMessage());
            }
        }

        return new TestCase("1", "sample case from res/raw/sample_case.txt",
                steps.toArray(new Step[steps.size()]));

    }

    @Override
    public TestCase[] buildCases(String suite_id) {
        ArrayList<TestCase> list = new ArrayList<TestCase>();
        list.add(buildFromSampleTest());
        return list.toArray(new TestCase[list.size()]);
    }

}
