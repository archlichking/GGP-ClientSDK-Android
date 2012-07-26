
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertTrue;
import net.gree.asdk.api.GreePlatform;
import util.ActionQueue;
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

public class PopupStepDefinitions extends BasicStepDefinition{

    private static final String TAG = "Popup_Steps";

    private static final String POPUP_PARAMS = "popup_params";

    private static final String POPUP_TYPE = "popup_type";

    public static final String HANDLER = "handler";
    
    @And("I initialize (\\w+) popup with title (.+) and body (.+)")
    public void initPopupDialog(String type, String title, String body) {
        String[] params = {
                title, body
        };

        getBlockRepo().put(POPUP_PARAMS, params);
        if ("request".equals(type)) {
            getBlockRepo().put(POPUP_TYPE, ActionQueue.POPUP_REQUEST);
        } else {
            Log.e(TAG, "unknown popup type!");
            getBlockRepo().put(POPUP_TYPE, ActionQueue.POPUP_UNKNOWN);
        }
    }

    @When("I did open popup")
    public void openPopupByType() {
        if ((Integer) getBlockRepo().get(POPUP_TYPE) == ActionQueue.POPUP_UNKNOWN) {
            return;
        }
        // Sent out broadcast to action queue
        Intent intent = new Intent();
        intent.setAction(ActionQueue.ACTION_REQUEST_POPUP);
        intent.putExtra(ActionQueue.POPUP_PARAMS, (String[]) getBlockRepo().get(POPUP_PARAMS));

        notifyStepWait();
        Step.setTimeout(40000);
        GreePlatform.getContext().sendOrderedBroadcast(intent, null, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ActionQueue.RESULT_SUCCESS == getResultCode()) {
                    Log.d(TAG, "load popup success!");
                } else if (ActionQueue.RESULT_FAILURE == getResultCode()) {
                    Log.e(TAG, "load popup failed!");
                }
                notifyStepPass();
            }
        }, null, ActionQueue.RESULT_UNKNOWN, null, null);
    }

    @Then("request popup should open as we expected")
    public void verifyRequestPopup() {
        double sRate = PopupUtil.getSimilarityOfPopupView(R.drawable.expect_request_dialog);
        Log.d(TAG, "Similarity rate: " + sRate);
        assertTrue("popup similarity is bigger than 80%", sRate > 80);
    }

    @After("I did dismiss popup")
    public void dismissPopup() {
        PopupUtil.dismissPopupDialog();
    }

}
