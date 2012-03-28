
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

import net.gree.asdk.api.IgnoreList;
import net.gree.asdk.api.IgnoreList.IgnoreListUpdateListener;

import org.apache.http.HeaderIterator;

import util.Consts;

import android.util.Log;

public class IgnoreList_StepDefinitions extends BasicStepDefinition {
    private static final String TAG = "People_Steps";

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

    IgnoreListUpdateListener listener = new IgnoreListUpdateListener() {

        @Override
        public void onReceive(int index, int totalListSize, String[] requestedElements) {
            Log.d(TAG, "Get ignore list success!");
            Log.i(TAG, "Index: " + index);
            Log.i(TAG, "TotalListSize: " + totalListSize);
            if (requestedElements != null) {
                for (int i = 0; i < requestedElements.length; i++) {
                    Log.i(TAG, "Block user " + (i + 1) + ": " + requestedElements[i]);
                }
            }
            status = Consts.SUCCESS;
        }

        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String response) {
            Log.e(TAG, "Get ignore list failed!");
            status = Consts.FAILED;
        }
    };

    IgnoreList ignoreList = new IgnoreList(listener);

    @When("I try to get the ignore list of current user")
    public void getIngoreList() {
        Log.d(TAG, "Begin to fetch ignore list");
        status = Consts.UNKNOWN;
        ignoreList.fetch();
        waitCallback();
    }

    @Then("I should get the list response")
    public void checkListResponse() {

    }

    @Given("user (\\w+) is not in my ignore list")
    public void makeSureUnblock() {

    }

    @When("I try to block user (\\w+)")
    public void blockUser(String uid) {
        // TODO blockUser api is not provide now
        ignoreList.blockUser(uid);
    }

    @Then("the user (\\w+) should be blocked")
    public void verifyUserBlocked(String uid) {
        // TODO blockUser api is not provide now
    }

    @When("I try to unblock user (\\w+)")
    public void unblockUser(String uid) {
        // TODO unblockUser api is not provide now
        ignoreList.unBlockUser(uid);
    }

    @Then("the user (\\w+) should be unblocked")
    public void verifyUserUnblocked(String uid) {
        // TODO unblockUser api is not provide now
    }

    @When("I try to check whether user (\\w+) is blocked")
    public void isBlocked(String uid) {
        status = Consts.UNKNOWN;
        // TODO isUserBlocked can not use now, will crash the app
        // ignoreList.isUserBlocked(uid);
        // waitCallback();
    }

    @Then("I should got the result that he is been blocked")
    public void verifyIsBlocked() {

    }

}
