
package com.openfeint.qa.core.runner;

import com.openfeint.qa.core.R;
import com.openfeint.qa.core.caze.TestCase;
import com.openfeint.qa.core.caze.TestCaseDelegate;
import com.openfeint.qa.core.exception.TCMIsnotReachableException;
import com.openfeint.qa.core.util.StringUtil;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;

public class TestRunner {

    private static TestRunner runner = null;

    private Context context;

    private TestRunner(Context context) {
        this.context = context;
        cases = new HashMap<String, TestCase>();
    }

    public static TestRunner getInstance(Context context) {
        if (runner == null) {
            runner = new TestRunner(context);
        }
        return runner;
    }

    private HashMap<String, TestCase> cases;

    public void addCase(TestCase caze) {
        cases.put(caze.getId(), caze);
    }

    public void addCases(TestCase[] cazes) {
        for (TestCase caze : cazes) {
            cases.put(caze.getId(), caze);
        }
    }

    public void emptyCases() {
        cases.clear();
    }

    public boolean hasCase() {
        return 0 != cases.values().size();
    }

    public void runAllCases() {
        for (TestCase tc : cases.values()) {
            if (!tc.isExecuted() && tc.getResult() != TestCase.RESULT.NEVER) {
                tc.execute();
            }
        }

    }

    public void runCasesByIds(String[] ids) {
        for (String id : ids) {
            TestCase tc = cases.get(id);
            if (!tc.isExecuted() && tc.getResult() != TestCase.RESULT.NEVER) {
                tc.execute();
            }
        }
    }

    public TestCase getCaseById(String id) {
        return cases.get(id);
    }

    public void runCase(String id) {
        // set case result
        TestCase tc = cases.get(id);
        tc.execute();
    }

    public TestCase[] getAllCases() {
        return this.cases.values().toArray(new TestCase[cases.values().size()]);
    }
}
