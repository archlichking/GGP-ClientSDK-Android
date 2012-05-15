package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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

import com.google.common.collect.Iterators;
import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.core.command.After;

/*
 * Template: https://git.gree-dev.net/ggpautomation/common-steps/wiki/TS-Moderation
 */
public class ModerationStepDefinitions extends BasicStepDefinition {
  private static final String TAG = "Moderation_Steps";

  private static String MODERATION_LIST = "moderationlist";

  @When("I send to moderation server with text (.+)")
  public void createModerationText(String text) {
    getBlockRepo().put(MODERATION_LIST, new ArrayList<ModeratedText>());
    notifyStepWait();
    ModeratedText.create(text, new ModeratedTextListener() {
      
      @Override
      public void onSuccess(ModeratedText[] textInfo) {
        Log.d(TAG, "Get moderated text success.");
        ModeratedText.logTextInfo(textInfo);
        ((ArrayList<ModeratedText>) getBlockRepo().get(MODERATION_LIST)).addAll(Arrays
            .asList(textInfo));
        notifyStepPass();
      }

      @Override
      public void onFailure(int responseCode, HeaderIterator headers, String response) {
        Log.e(TAG, "Get moderated text failed!");
        notifyStepPass();
      }
    });
  }
  
  /*
   * Helper: get a list of textIds from the repo
   */
  private String[] getTextIdsSaved() {
    ArrayList<ModeratedText> moderationList = (ArrayList<ModeratedText>) getBlockRepo().get(MODERATION_LIST);
    ArrayList<String> textIdsA = new ArrayList<String>();
    Iterator<ModeratedText> iterator = moderationList.iterator();
    while (iterator.hasNext()) {
      textIdsA.add(iterator.next().getTextId());
      Log.d(TAG, textIdsA.toString());
    }
    return (String[]) textIdsA.toArray(new String[textIdsA.size()]);
  }
  
  @When("I load from (.+ ) with moderation text (.+)")
  public void loadFromServer(String source, final String text) {
    
    if ("SERVER".equals(source)) {
      notifyStepWait();
      
      ModeratedText.loadFromIds(getTextIdsSaved(), new ModeratedTextListener() {
          //ArrayList<String> textA = new ArrayList<String>();
          
          @Override
          public void onSuccess(ModeratedText[] textInfo) {
            Log.d(TAG, "Get moderated text from Id success!");
            /*
             * replace the existing MODERATION_LIST
             */ 
            getBlockRepo().put(MODERATION_LIST, new ArrayList<ModeratedText>(Arrays.asList(textInfo)));
            notifyStepPass();
          }
          
          @Override
          public void onFailure(int responseCode, HeaderIterator headers, String response) {
            Log.e(TAG, "Get moderated text from Id failed!");
        
            notifyStepPass();
          }
       });
    }
  }
  
  
  // From Vincent's upcoming commit
  public int convertModeratedTextStatus(String status) {
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
    fail("only CHECKING, APPROVED, DELETED, REJECTED, UNKNOWN status can be checked");   
    return -1;
}
      
  @Then("status of text (.+) should be (.+)")
  public void checkStatus(String text, String status) {
    ArrayList<ModeratedText> l = (ArrayList<ModeratedText>) getBlockRepo().get(MODERATION_LIST);
    assertTrue(l.size() > 0);
    int _found = 0;
    
    for (ModeratedText m : l) {
      if(text.equals(m.getContent())) {
        _found++;
        assertEquals("status should be " + status + " but is " + String.valueOf(m.getStatus()), convertModeratedTextStatus(status), m.getStatus());
      }
    }
    assertTrue(_found==1);
  }
 
  @Given("I make sure moderation server (\\w+) text (.*)")
  @After("I make sure moderation server (\\w+) text (.*)")
  public void makeSureTextInServer(String ad, String text) {
    if ("NOTINCLUDES".equals(ad)) {
//      
//      for (ModeratedText m : (ArrayList<ModeratedText>) getBlockRepo().get(MODERATION_LIST)) {
//        m.delete(new SuccessListener() {
//          
//          @Override
//          public void onSuccess() {
//            Log.d(TAG, "Delete moderated text success.");
//            notifyAsyncInStep();
//          }
//          
//          @Override
//          public void onFailure(int responseCode, HeaderIterator headers, String response) {
//            Log.d(TAG, "Delete moderated text failed!");
//            notifyAsyncInStep();
//          }
//        });
//        waitForAsyncInStep();
//      }
    }
  }
}