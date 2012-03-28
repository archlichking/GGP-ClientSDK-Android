
package com.openfeint.qa.ggp.adapter;

import com.openfeint.qa.core.caze.TestCase;

public class CaseWrapper implements Comparable<CaseWrapper> {
    private TestCase theCase;

    private boolean isSelected;

    public CaseWrapper(TestCase theCase, boolean isSelected) {
        this.theCase = theCase;
        this.isSelected = isSelected;
    }

    public TestCase getTheCase() {
        return theCase;
    }

    public void setTheCase(TestCase theCase) {
        this.theCase = theCase;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    @Override
    public int compareTo(CaseWrapper another) {
        return Integer.parseInt(theCase.getId()) - Integer.parseInt(another.getTheCase().getId());
    }
}
