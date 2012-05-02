
package com.openfeint.qa.core.caze.step.definition;

import android.util.Log;

import java.util.Hashtable;
import java.util.Observable;
import java.util.concurrent.Semaphore;

public abstract class BasicStepDefinition extends Observable {
    protected static Hashtable<String, Object> blockRepo = null;

    private final Semaphore inStepSync = new Semaphore(0, true);

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
     * keeps waiting until a notifyAsync() call invoked
     */
    protected void waitForAsyncInStep() {
        try {
            inStepSync.acquire();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * notifys async semaphore that current async call is finished
     */
    protected void notifyAsyncInStep() {
        Log.d("DEBUG", "after release");
    }
}
