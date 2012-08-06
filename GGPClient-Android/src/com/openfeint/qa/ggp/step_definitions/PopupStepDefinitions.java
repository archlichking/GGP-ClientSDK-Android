
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertTrue;

import java.util.HashMap;

import net.gree.asdk.api.GreePlatform;
import util.PopupHandler;
import util.PopupUtil;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.openfeint.qa.core.caze.step.Step;
import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.ggp.R;

public class PopupStepDefinitions extends BasicStepDefinition {

    private final String TAG = "Popup_Steps";

    private final String POPUP_PARAMS = "popup_params";

    private final String POPUP_ACTION = "popup_action";

    public final String HANDLER = "handler";

    @And("I initialize request popup with title (.+) and body (.+)")
    public void initRequestPopupDialog(String title, String body) {
        String[] params = {
                title, body
        };
        getBlockRepo().put(POPUP_PARAMS, params);
        getBlockRepo().put(POPUP_ACTION, PopupHandler.ACTION_REQUEST_POPUP);
    }

    @And("I initialize share popup with text (.+)")
    public void initSharePopupDialog(String text) {
        String[] params = {
            text
        };
        getBlockRepo().put(POPUP_PARAMS, params);
        getBlockRepo().put(POPUP_ACTION, PopupHandler.ACTION_SHARE_POPUP);
    }

    @And("I initialize invite popup with message (.+) and callback url (.+) and users (.+)")
    public void initInvitePopupDialog(String body, String callbackUrl, String userIds) {
        String[] userList = userIds.split(",");
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("body", body);
        map.put("to_user_id", userList);
        map.put("callbackurl", callbackUrl);
        getBlockRepo().put(POPUP_PARAMS, map);
        getBlockRepo().put(POPUP_ACTION, PopupHandler.ACTION_INVITE_POPUP);
    }

    @SuppressWarnings("unchecked")
    @When("I did open popup")
    @And("I did open popup")
    public void openPopupByType() {
        if ("".equals((String) getBlockRepo().get(POPUP_ACTION))) {
            return;
        }
        // Sent out broadcast to action queue
        Intent intent = new Intent((String) getBlockRepo().get(POPUP_ACTION));
        if (PopupHandler.ACTION_INVITE_POPUP.equals((String) getBlockRepo().get(POPUP_ACTION))) {
            intent.putExtra(PopupHandler.POPUP_PARAMS, (HashMap<String, Object>) getBlockRepo()
                    .get(POPUP_PARAMS));
        } else {
            intent.putExtra(PopupHandler.POPUP_PARAMS, (String[]) getBlockRepo().get(POPUP_PARAMS));
        }

        notifyStepWait();
        Step.setTimeout(40000);
        GreePlatform.getContext().sendOrderedBroadcast(intent, null, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (PopupHandler.RESULT_SUCCESS == getResultCode()) {
                    Log.d(TAG, "load popup success!");
                } else if (PopupHandler.RESULT_FAILURE == getResultCode()) {
                    Log.e(TAG, "load popup failed!");
                }
                notifyStepPass();
            }
        }, null, PopupHandler.RESULT_UNKNOWN, null, null);
    }

    @Then("request popup should open as we expected")
    public void verifyRequestPopup() {
        double sRate = PopupUtil.getSimilarityOfPopupView(R.drawable.expect_request_dialog);
        Log.d(TAG, "Similarity rate: " + sRate);
        assertTrue("popup similarity is bigger than 80%", sRate > 80);
    }

    @After("I did dismiss popup")
    public void dismissPopup() {
        Intent intent = new Intent(PopupHandler.ACTION_POPUP_DISMISS);
        notifyStepWait();
        GreePlatform.getContext().sendOrderedBroadcast(intent, null, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (PopupHandler.RESULT_FAILURE == getResultCode()) {
                    Log.e(TAG, "dismiss popup failed!");
                }
                notifyStepPass();
            }
        }, null, PopupHandler.RESULT_UNKNOWN, null, null);
    }

    @When("I check request popup setting info (\\w+)")
    @And("I check request popup setting info (\\w+)")
    public void getRequestPopupInfo(String column) {
        PopupUtil.getValueFromPopup("fid('msg-box')");
        getBlockRepo().put("request-" + column, PopupHandler.valueToBeVerified);
    }

    @Then("(request|invite|share) popup info (\\w+) should be (.+)")
    @And("(request|invite|share) popup info (\\w+) should be (.+)")
    public void verifySNSPopupInfo(String popupType, String column, String expectValue) {
        String resultValue = (String) getBlockRepo().get(popupType + "-" + column);
        assertTrue("value from sns popup", resultValue.contains(expectValue));
    }

    @When("I check share popup setting info (\\w+)")
    public void getSharePopupInfo(String column) {
        PopupUtil.getValueFromPopup("fid('ggp_share_mood_message_display')");
        getBlockRepo().put("share-" + column, PopupHandler.valueToBeVerified);
    }

    @When("I check invite popup setting info (\\w+)")
    public void getInvitePopupInfo(String column) {
        PopupUtil.getValueFromPopup("fclass('balloon bottom list-item round shrink')");
        getBlockRepo().put("invite-" + column, PopupHandler.valueToBeVerified);
    }
}
