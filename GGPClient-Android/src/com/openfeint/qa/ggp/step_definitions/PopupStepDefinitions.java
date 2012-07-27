
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertTrue;
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

    @And("I initialize (\\w+) popup with title (.+) and body (.+)")
    public void initPopupDialog(String type, String title, String body) {
        String[] params = {
                title, body
        };

        getBlockRepo().put(POPUP_PARAMS, params);
        if ("request".equals(type)) {
            getBlockRepo().put(POPUP_ACTION, PopupHandler.ACTION_REQUEST_POPUP);
        } else {
            Log.e(TAG, "unknown popup type!");
            getBlockRepo().put(POPUP_ACTION, "");
        }
    }

    @When("I did open popup")
    public void openPopupByType() {
        if ("".equals((String) getBlockRepo().get(POPUP_ACTION))) {
            return;
        }
        // Sent out broadcast to action queue
        Intent intent = new Intent((String) getBlockRepo().get(POPUP_ACTION));
        intent.putExtra(PopupHandler.POPUP_PARAMS, (String[]) getBlockRepo().get(POPUP_PARAMS));

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

}
