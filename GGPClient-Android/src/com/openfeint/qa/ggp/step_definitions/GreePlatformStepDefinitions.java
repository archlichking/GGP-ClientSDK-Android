
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.TreeMap;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.GreePlatform.BadgeListener;
import util.Consts;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class GreePlatformStepDefinitions extends BasicStepDefinition {
    private final String TAG = "GreePlatform_steps";

    private final String UPDATE_RESULT = "update_result";
    private final String GREEPLATFORM_INFO = "greeplateform_info";
    private final String SDK_BUILD = "sdk_build";
    private final String SDK_VERSION = "sdk_version";

    @When("I update badge value to latest one")
    public void updateBadgeCount() {
        notifyStepWait();
        getBlockRepo().put(UPDATE_RESULT, Consts.UNKNOWN);
        GreePlatform.updateBadgeValues(new BadgeListener() {
            @Override
            public void onBadgeCountUpdated(int newCount) {
                Log.d(TAG, "update badge count success!");
                getBlockRepo().put(UPDATE_RESULT, Consts.SUCCESS);
                notifyStepPass();
            }
        });
    }
    
    @When("I check basic platform info")
    public void checkPlatformInfo() {
        HashMap<String, String> greeplatform_info = new HashMap<String, String>();
        greeplatform_info.put(SDK_BUILD, GreePlatform.getSdkBuild());
        greeplatform_info.put(SDK_VERSION, GreePlatform.getSdkVersion());
        getBlockRepo().put(GREEPLATFORM_INFO, greeplatform_info);
    }

    // TODO update social badge value is not supported in android SDK now
    @Then("my social badge value should be (\\d+)")
    public void verifySocialBadgeCount(int count) {
    }

    @Then("my in game badge value should be (\\d+)")
    public void verifyInGameBadgeCount(int count) {
        int return_count = GreePlatform.getBadgeValues();
        assertEquals("update badge count success", Consts.SUCCESS, getBlockRepo()
                .get(UPDATE_RESULT));
        assertEquals("badge count", count, return_count);
    }

    @Then("get (\\w+) from GreePlatform options should be (.+)")
    public void verifyAppVersion(String key, String value) {
        assertEquals("get " + key + " from options", value, GreePlatform.getOption(key));
    }

    @Then("get resource id of (.+) should be (\\d+)")
    public void verifyResourceId(String resource_name, int expect_id) {
        assertEquals("id of resource", expect_id,
                GreePlatform.getResource(GreePlatform.getContext(), resource_name));
    }

    @Then("get resource name of id (\\d+) should be (.+)")
    public void verifyResourceName(int resource_id, String expect_name) {
        assertEquals("name of resource", expect_name, GreePlatform.getRString(resource_id));
    }
    
    @Then("my sdk build should be (.+)")
    public void verifySdkBuild(String sdk_build) {
        HashMap<String, String> greeplatform_info = (HashMap<String, String>) getBlockRepo().get(GREEPLATFORM_INFO);
        assertEquals(sdk_build, greeplatform_info.get(SDK_BUILD));
    }
    
    @Then("my sdk version should be (.+)")
    public void verifySdkVersion(String sdk_version) {
        HashMap<String, String> greeplatform_info = (HashMap<String, String>) getBlockRepo().get(GREEPLATFORM_INFO);
        assertEquals(sdk_version, greeplatform_info.get(SDK_VERSION));
    }
}
