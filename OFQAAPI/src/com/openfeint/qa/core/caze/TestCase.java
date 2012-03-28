
package com.openfeint.qa.core.caze;

import com.openfeint.qa.core.caze.step.Step;
import com.openfeint.qa.core.caze.step.StepResult;
import com.openfeint.qa.core.util.StringUtil;

import android.util.Log;
/***
 * 
 * @author thunderzhulei
 * @category class for a single scenario, including executing itself
 */
public class TestCase {
    public interface RESULT {
        int FAILED = 5;
        int RETESTED = 4;
        int PASSED = 1;
        int UNTEST = 0;
    }
/***
 * 
 * @param id case_id
 * @param title case_title
 * @param steps steps for final executing method
 */
    public TestCase(String id, String title, Step[] steps) {
        this.id = id;
        this.title = title;
        this.steps = steps;
        this.result = TestCase.RESULT.UNTEST;
        this.resultComment = "";
        this.isExecuted = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Step[] getSteps() {
        return steps;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public boolean isExecuted() {
        return this.isExecuted;
    }

    private String id;

    private String title;

    private Step[] steps;

    private int result;

    private boolean isExecuted;

    public void setExecuted(boolean isExecuted) {
        this.isExecuted = isExecuted;
    }

    private String resultComment;

    public void execute() {
        Log.v(StringUtil.DEBUG_TAG, "executing case [id: " + id + ", title: " + title + "]");
        int res = TestCase.RESULT.PASSED;
        // no step in this case, failed directly
        if (steps.length == 0) {
            res = TestCase.RESULT.RETESTED;
            this.resultComment = "No Step Found for this case, maybe a parse error, need retested";
        } else {
            StringBuffer sb = new StringBuffer();
            for (Step s : steps) {
                StepResult sr = s.invoke();
                // merge results with or operation
                res = res | sr.getCode();
                sb.append(sr.getComment() + StringUtil.FILE_LINE_SPLIT);
            }
            this.resultComment = sb.toString();
        }
        this.result = res;
        this.isExecuted = true;
    }

    public void setResultComment(String result_comment) {
        this.resultComment = result_comment;
    }

    public String getResultComment() {
        return resultComment;
    }

}
