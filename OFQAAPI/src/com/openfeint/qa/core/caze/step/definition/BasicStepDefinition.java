
package com.openfeint.qa.core.caze.step.definition;

import java.util.Hashtable;
import java.util.Observable;

public abstract class BasicStepDefinition extends Observable{
    protected static Hashtable<String, Object> blockRepo = null;
    protected static Hashtable<String, Object> getBlockRepo() {
        if (null == blockRepo) {
            blockRepo = new Hashtable<String, Object>();
        }
        return blockRepo;
    }

    protected void notifyNext() {
        setChanged();
        notifyObservers("update waiting");
    }
}
