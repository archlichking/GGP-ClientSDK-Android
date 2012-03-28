
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

import net.gree.asdk.api.Moderation;
import net.gree.asdk.api.Moderation.ResponseArray;
import net.gree.asdk.api.Moderation.Status;
import net.gree.asdk.api.Moderation.TextArrayListener;
import net.gree.asdk.api.Moderation.TextInfo;

import org.apache.http.HeaderIterator;

import util.Consts;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Moderation_StepDefinitions extends BasicStepDefinition {
    private static final String TAG = "Moderation_Steps";

    private static List<TextInfo> words = new ArrayList<TextInfo>();

    // private static String[] wordIds;

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
                assertEquals(Consts.SUCCESS, status);
                break;
            }
        }
    }

    TextArrayListener listener = new TextArrayListener() {

        @Override
        public void onSuccess(int responseCode, HeaderIterator headers, ResponseArray textInfo) {
            Log.d(TAG, "Get moderation words success!");
            for (TextInfo info : textInfo.entry) {
                Log.d(TAG, "id: " + info.textId);
                Log.d(TAG, "Data: " + info.data);
                Log.d(TAG, "Status: " + info.status);
                words.add(info);
            }
            status = Consts.SUCCESS;
        }

        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String response) {
            Log.e(TAG, "Get moderation words failed!");
            status = Consts.FAILED;
        }
    };

    Moderation moderation = new Moderation();

    @When("I try to send text (\\w+) for moderation")
    public void getWords(String text) {

        // create a new word for moderation
        status = Consts.UNKNOWN;
        moderation.create(text, listener);
        waitCallback();

        Status[] words = moderation.words();
        if (words.length == 0)
            Log.w(TAG, "No sensitive word!");
        else
            for (Status word : words) {
                Log.i(TAG, "Words in native cache: ");
                Log.i(TAG, "Word is: " + word.word);
                Log.i(TAG, "id: " + word.id);
                Log.i(TAG, "status: " + word.status);
                Log.i(TAG, "time: " + word.time);
            }
    }

    @Then("I should get its approve status")
    public void verifyWords() {
        for (int i = 0; i < words.size(); i++) {
            assertEquals(Moderation.UnderInspection, Integer.parseInt(words.get(i).status));
        }
    }

    @And("I try to check the approve status")
    public void checkStatus() {
        words.clear();
        for (int i = 0; i < words.size() && i < 3; i++) {
            status = Consts.UNKNOWN;
            moderation.check(words.get(i).textId, listener);
            waitCallback();
        }
    }
}
