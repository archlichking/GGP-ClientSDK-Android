
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.GreeUser;
import net.gree.asdk.api.GreeUser.GreeUserListener;
import net.gree.asdk.api.ModeratedText;
import net.gree.asdk.api.ModeratedText.ModeratedTextListener;
import net.gree.asdk.api.ModeratedText.SuccessListener;
import net.gree.asdk.core.GLog;

import org.apache.http.HeaderIterator;

import util.Consts;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class ModerationStepDefinitions extends BasicStepDefinition {
    private static final String TAG = "Moderation_Steps";

    private static String MODERATION_LIST = "moderationlist";

    @When("I send to moderation server with text (.+)")
    public void sendModeration(String text) {
        notifyStepWait();
        ModeratedText.create(text, new ModeratedTextListener() {
            @Override
            public void onSuccess(ModeratedText[] textInfo) {
                Log.d(TAG, "onSuccess");
                ModeratedText.logTextInfo(textInfo);
                notifyStepPass();
            }

            @Override
            public void onFailure(int responseCode, HeaderIterator headers, String response) {
                Log.e(TAG, "Get moderated text failed!");
                notifyStepPass();
            }
        });
    }

    private boolean transTarget(String target) {
        if ("LOCAL_CACHE".equals(target)) {
            return false;
        } else if ("SERVER".equals(target)) {
            return true;
        } else {
            fail("Unknown target to load moderation text!");
            return false;
        }
    }

    @When("I load moderation text from (\\w+)")
    @After("I load moderation text from (\\w+)")
    public void loadModerationText(String target) {
        ModeratedTextListener listener = new ModeratedTextListener() {

            @Override
            public void onSuccess(ModeratedText[] textInfo) {
                Log.d(TAG, "Get moderated text success!");
                Log.d(TAG, "Get " + textInfo.length + " text!");
                ((ArrayList<ModeratedText>) getBlockRepo().get(MODERATION_LIST)).addAll(Arrays
                        .asList(textInfo));
                notifyStepPass();
            }

            @Override
            public void onFailure(int responseCode, HeaderIterator headers, String response) {
                Log.e(TAG, "Get moderated text failed! " + response);
                notifyStepPass();
            }
        };

        boolean isLoadFromServer = transTarget(target);
        notifyStepWait();
        getBlockRepo().put(MODERATION_LIST, new ArrayList<ModeratedText>());
        ModeratedText.loadFromLocalCache(isLoadFromServer, listener);

    }

    private int transStatus(String status) {
        if ("CHECKING".equals(status))
            return ModeratedText.STATUS_BEING_CHECKED;
        else if ("APPROVED".equals(status))
            return ModeratedText.STATUS_RESULT_APPROVED;
        else if ("DELETED".equals(status))
            return ModeratedText.STATUS_DELETED;
        else if ("REJECTED".equals(status))
            return ModeratedText.STATUS_RESULT_REJECTED;
        else if ("UNKNOWN".equals(status))
            return ModeratedText.STATUS_UNKNOWN;
        else
            fail("Unknow status of the moderation text!");
        return -100;
    }

    @Then("status of text (.+) should be (\\w+)")
    public void verifyModerationText(String text, String status) {
        ArrayList<ModeratedText> list = (ArrayList<ModeratedText>) getBlockRepo().get(
                MODERATION_LIST);
        if (list == null || list.size() == 0)
            fail("No moderation text in the list!");
        for (ModeratedText item : list) {
            if (text.equals(item.getContent())) {
                Log.d(TAG, "Get the text \"" + text + "\" and checking its status...");
                assertEquals(ModeratedText.STATUS_BEING_CHECKED, transStatus(status));
            }
            return;
        }
        fail("Could not find the moderation text: " + text);
    }

    @After("I make sure the moderation text is clean up")
    public void cleanUpText() {
        ArrayList<ModeratedText> list = (ArrayList<ModeratedText>) getBlockRepo().get(
                MODERATION_LIST);
        if (list == null || list.size() == 0) {
            Log.d(TAG, "No moderation text to be clean!");
            return;
        }
        for (ModeratedText item : list) {
            item.delete(new SuccessListener() {

                @Override
                public void onSuccess() {
                    Log.d(TAG, "Clean one text...");
                }

                @Override
                public void onFailure(int responseCode, HeaderIterator headers, String response) {
                    Log.e(TAG, "Clean up failed, " + response);
                }
            });
        }
    }
}
