
package com.openfeint.qa.core.caze.step;

import java.lang.reflect.Method;
import java.util.regex.Pattern;
/***
 * 
 * @author thunderzhulei
 * @category named step annotation and it's aim method pair  
 */
public class StepPair {
    private Pattern p;

    private Method m;

    public StepPair(Pattern p, Method m) {
        super();
        this.p = p;
        this.m = m;
    }

    public Pattern getP() {
        return p;
    }

    public void setP(Pattern p) {
        this.p = p;
    }

    public Method getM() {
        return m;
    }

    public void setM(Method m) {
        this.m = m;
    }
}
