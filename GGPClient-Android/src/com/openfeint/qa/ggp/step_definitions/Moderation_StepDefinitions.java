
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.GreeUser;
import net.gree.asdk.api.GreeUser.GreeUserListener;
import net.gree.asdk.api.Moderation;
import net.gree.asdk.core.GLog;

import org.apache.http.HeaderIterator;

import util.Consts;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class Moderation_StepDefinitions extends BasicStepDefinition {
    private static final String TAG = "Moderation_Steps";

    private static List<Moderation.ResponseArray> moderationList = new ArrayList<Moderation.ResponseArray>();

    private String status;

    // waiting for the async callback and assert it success
    private void waitCallback() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (status != Consts.UNKNOWN) {
                assertEquals("server response", Consts.SUCCESS, status);
                break;
            }
        }
    }
    
    Moderation.TextArrayListener moderationListener = new Moderation.TextArrayListener() {
      @Override
      public void onSuccess(int responseCode, HeaderIterator headers,
          Moderation.ResponseArray textInfo) {
        String id = textInfo.entry[0].textId;
        String statusStr = textInfo.entry[0].status;
        int statusi = 0;
        try {
          statusi = Integer.parseInt(statusStr);
        } catch (Exception ex) {}
        Moderation.logTextInfo(textInfo.entry);
        status = Consts.SUCCESS;
      }
      @Override
      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        Log.e(TAG, "Get moderation failed!");
        moderationList.clear();
        status = Consts.FAILED;
      }
    };
    
    @When("I send to moderation server with text (.*)")
    public void sendModeration(String text) {
      Moderation moderation = new Moderation();
      moderation.create(text, moderationListener);
      waitCallback(); // add here?
    }
    
    @Then("Then status of text (.*) in server should be CHECKED")
    public void checkStatusText() {
      //TODO
    }

}
