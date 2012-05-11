
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.gree.asdk.api.FriendCode;
import net.gree.asdk.api.FriendCode.Code;
import net.gree.asdk.api.FriendCode.CodeListener;
import net.gree.asdk.api.FriendCode.SuccessListener;

import org.apache.http.HeaderIterator;

import android.R.integer;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class FriendCodeStepDefinitions extends BasicStepDefinition {
    private static final String TAG = "FriendCode_Steps";

    private static final String FRIEND_CODE = "friendCode";

    @Given("I make sure my friend code is NOTEXIST")
    public void updateCodeAsCondition() {
        deleteCode();
    }
    
    private void deleteCode() {
        notifyStepWait();
        FriendCode.deleteCode(new SuccessListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Delete friend code success!");
                notifyStepPass();
            }

            @Override
            public void onFailure(int responseCode, HeaderIterator headers, String response) {
                Log.e(TAG, "Delete friend code failed, " + response);
                notifyStepPass();
            }
        });
    }

    @When("I request friend code with no expire time")
    public void requestNoExpireTimeCode() {
        requestFriendCode("");
    }

    @When("I request friend code with expire time (.+)")
    public void requestFriendCode(String expireTime) {
        CodeListener listener = new CodeListener() {
            @Override
            public void onSuccess(Code code) {
                Log.d(TAG, "Add new friend code success: " + code.getCode());
                Log.d(TAG, "Expire time is: " + code.getExpireTime());
                getBlockRepo().put(FRIEND_CODE, code);
                notifyStepPass();
            }

            @Override
            public void onFailure(int responseCode, HeaderIterator headers, String response) {
                Log.e(TAG, "Add friend code failed, " + response);
                notifyStepPass();
            }
        };

        getBlockRepo().put(FRIEND_CODE, "");
        notifyStepWait();
        FriendCode.requestCode(expireTime, listener);
    }

    @Then("I should get my friend code")
    public void verifyFriendCode() {
        assertEquals("friend code length", ((Code) getBlockRepo().get(FRIEND_CODE)).getCode()
                .length(), 7);
    }

    private String getExpireDate(int days) {
        // Calculate the expire date
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, days);
        cal.add(Calendar.HOUR, -8); // reduce 8 hours to UTC time zone
        return formater.format(cal.getTime());
    }
    
    @And("my friend code expire time should be (.+)")
    public void verifyExpireTime(String expectTime) {
        String expireTime = ((Code) getBlockRepo().get(FRIEND_CODE)).getExpireTime();
        if (expectTime.endsWith("days")) {
            int days = Integer.parseInt(expectTime.split(" ")[0]);
            String expireDate = getExpireDate(days);
            Log.d(TAG, "Checking the expire date...");
            assertEquals("Expire date", expireDate, expireTime.substring(0, 10));
        } else {
            assertEquals("Expire date", expectTime, expireTime);
        }
    }
}
