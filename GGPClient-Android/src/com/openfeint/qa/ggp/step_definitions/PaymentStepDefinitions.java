
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertTrue;

import org.hamcrest.SelfDescribing;

import net.gree.asdk.api.GreePlatform;
import util.ActionQueue;
import util.PopupUtil;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.openfeint.qa.core.caze.step.Step;
import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class PaymentStepDefinitions extends BasicStepDefinition {
    private static final String TAG = "Payment_Steps";

    private static final String PAYMENT_PARAMS = "payment_params";

    @When("I add payment item with ID (\\w+), NAME (.+), UNIT_PRICE (\\d+), QUANTITY (\\d+), IMAGE_URL (.+) and DESCRIPTION (.+)")
    public void initPaymentItem(String itemId, String itemName, int unitPrice, int quantity,
            String imgUrl, String desc) {
        String[] params = {
                itemId, itemName, String.valueOf(unitPrice), String.valueOf(quantity), imgUrl, desc
        };

        getBlockRepo().put(PAYMENT_PARAMS, params);
    }

    @And("I did open the payment request popup")
    public void sendPaymentRequest() {
        
        Intent intent_open = new Intent();
        intent_open.setAction(ActionQueue.ACTION_PAYMENT_POPUP);
        intent_open.putExtra(ActionQueue.POPUP_PARAMS, (String[]) getBlockRepo().get(PAYMENT_PARAMS));

        notifyStepWait();
        Step.setTimeout(40000);
        // ask action queue to open popup
        GreePlatform.getContext().sendBroadcast(intent_open);
        
        //wait payment popup dialog to open
        while (!ActionQueue.is_popup_opened) {
            Log.d(TAG, "waiting for payment popup open!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        Intent intent_check_loaded = new Intent();
        intent_check_loaded.setAction(ActionQueue.ACTION_CHECK_PAYMENT_POPUP_LOADED);
        GreePlatform.getContext().sendOrderedBroadcast(intent_check_loaded, null, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //TODO I don't find a way to let result return correct for payment 
                if (ActionQueue.RESULT_SUCCESS == getResultCode()) {
                    Log.d(TAG, "load payment popup success!");
                } else if (ActionQueue.RESULT_FAILURE == getResultCode()) {
                    Log.e(TAG, "load payment popup failed!");
                } else if (ActionQueue.RESULT_UNKNOWN == getResultCode()) {
                    Log.e(TAG, "something wrong, still unknown status!");
                }
                notifyStepPass();
            }
        }, null, ActionQueue.RESULT_UNKNOWN, null, null);
    }

    @Then("payment popup info (\\w+) should be (.+)")
    @And("payment popup info (\\w+) should be (.+)")
    public void verifyPaymentPopupInfo(String column, String expectValue) {
        String statementToGetElement = "";
        if ("NAME".equals(column)) {
            statementToGetElement = "document.getElementsByClassName('title large')[0].textContent";
        } else if ("UNIT_PRICE".equals(column)) {
            statementToGetElement = "document.getElementsByTagName('li')[0].textContent";
        } else if ("QUANTITY".equals(column)) {
            statementToGetElement = "document.getElementsByTagName('li')[1].textContent";
        } else if ("IMAGE_URL".equals(column)) {
            statementToGetElement = "document.getElementsByTagName('img')[0].src";
        } else if ("DESCRIPTION".equals(column)) {
            statementToGetElement = "document.getElementsByTagName('li')[2].textContent";
        }
        ActionQueue.valueToBeVerified = null;
        PopupUtil.getValueFromPopup(statementToGetElement);
        assertTrue("value from payment popup", ActionQueue.valueToBeVerified.contains(expectValue));
    }
}
