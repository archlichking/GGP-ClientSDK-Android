
package com.openfeint.qa.ggp.step_definitions;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.Cmd;
import com.openfeint.qa.core.command.Given;

public class SampleStepDefinitions extends BasicStepDefinition {
    @Cmd("test cmd (\\w+)")
    @Given("test cmd (\\w+)")
    public void testThis(String pop) {
        System.out.println(pop);
    }
    
    @After("test cmd (\\d+)")
    public void testThis(int d) {
        System.out.println("123123" + d);
    }
}
