
package com.openfeint.qa.core.caze.step;
/***
 * 
 * @author thunderzhulei
 * @category step execute result. 
 */
public class StepResult {
    private int code;

    private String comment;
/**
 * 
 * @param code 1 for pass, 5 for fail
 * @param comment step result description
 */
    public StepResult(int code, String comment) {
        this.code = code;
        this.comment = comment;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
