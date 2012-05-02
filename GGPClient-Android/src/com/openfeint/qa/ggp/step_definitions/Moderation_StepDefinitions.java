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
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class Moderation_StepDefinitions extends BasicStepDefinition {
  private static final String TAG = "Moderation_Steps";

  private static String MODERATION_LIST = "moderationlist";

  @When("I send to moderation server with text (.*)")
  public void sendModeration(String text) {
    ModeratedText.create(text, new ModeratedTextListener() {
      @Override
      public void onSuccess(ModeratedText[] textInfo) {
        Log.d(TAG, "onSuccess");
        getBlockRepo().put(MODERATION_LIST, new ArrayList<ModeratedText>());
        ModeratedText.logTextInfo(textInfo);

        ((ArrayList<ModeratedText>) getBlockRepo().get(MODERATION_LIST)).addAll(Arrays
            .asList(textInfo));
        notifyStepPass();
      }

      @Override
      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        Log.e(TAG, "Get moderated text failed!");
        getBlockRepo().put(MODERATION_LIST, new ArrayList<ModeratedText>());
        notifyStepPass();
      }
    });
  }

}
