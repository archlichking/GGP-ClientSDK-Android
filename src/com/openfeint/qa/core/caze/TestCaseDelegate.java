
package com.openfeint.qa.core.caze;

import com.openfeint.qa.core.exception.TCMIsnotReachableException;

import java.util.Collection;

public abstract class TestCaseDelegate {
    public abstract void pushCaseResults(Collection<TestCase> cases)
            throws TCMIsnotReachableException;
}
