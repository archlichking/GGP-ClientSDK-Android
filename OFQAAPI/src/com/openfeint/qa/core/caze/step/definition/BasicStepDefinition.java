
package com.openfeint.qa.core.caze.step.definition;

import java.util.Hashtable;

public abstract class BasicStepDefinition {
    protected static Hashtable<String, Object> blockRepo = null;
    
    protected static Hashtable<String, Object> getBlockRepo(){
        if(null == blockRepo){
            blockRepo = new Hashtable<String, Object>();
        }
        return blockRepo;
    }
}
