
package util;

import java.io.IOException;
import java.io.Serializable;

public abstract class ActionQueueListener implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -8774863812614906886L;

    protected abstract void onSuccess();

    protected abstract void onFailure();


    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
    }
}
