package com.openfeint.qa.ggp.step_definitions;

import util.Consts;
import util.ImageUtil;
import net.gree.asdk.api.ScreenShot;
import android.graphics.Bitmap;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.ggp.MainActivity;

public class ScreenShotStepDefinitions extends BasicStepDefinition {
    public static final String BITMAP1 = "bitmap1";
    public static final String BITMAP2 = "bitmap2";
    
    @When("I take screenshot")
    public void takeScreenShot(){
        MainActivity activity = MainActivity.getInstance();
        Bitmap bitmap1 = ScreenShot.capture(activity.getWindow().getCurrentFocus());
        getBlockRepo().put(BITMAP1, bitmap1);
        Bitmap bitmap2 = ScreenShot.capture(activity.getWindow().getCurrentFocus());
        getBlockRepo().put(BITMAP2, bitmap2);
    }
    @Then("Screenshot should match")
    public void ignoreServerStep(){
        ImageUtil.compareImage((Bitmap) getBlockRepo().get(BITMAP1), (Bitmap) getBlockRepo().get(BITMAP2));
    }
}
