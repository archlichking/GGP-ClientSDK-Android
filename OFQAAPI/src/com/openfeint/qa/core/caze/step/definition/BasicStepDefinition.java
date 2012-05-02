
package com.openfeint.qa.core.caze.step.definition;

import java.util.Hashtable;
import java.util.Observable;

public abstract class BasicStepDefinition extends Observable {
    protected static Hashtable<String, Object> blockRepo = null;

    protected static Hashtable<String, Object> getBlockRepo() {
        if (null == blockRepo) {
            blockRepo = new Hashtable<String, Object>();
        }
        return blockRepo;
    }

    protected boolean WAIT = true;

    private int tiktak = 0;

    private static final int TIMEOUT = 10;

    /**
     * notifys automation framework that current step is finished
     */
    protected void notifyStepPass() {
        setChanged();
        notifyObservers("update waiting");
    }

    /**
     * resets async semaphore to its initial state
     */
    protected void resetAsyncInStep() {
        WAIT = true;
        tiktak = 0;
    }
    /**
     * keeps waiting until a notifyAsync() call invoked
     */
    protected void waitForAsyncInStep() {
        while (WAIT && tiktak < TIMEOUT) {
            try {
                Thread.sleep(1000);
                tiktak++;
            } catch (Exception e) {
                return;
            }
        }
    }
    /**
     * notifys async semaphore that current async call is finished
     */
    protected void notifyAsyncInStep() {
        WAIT = false;
    }
}
