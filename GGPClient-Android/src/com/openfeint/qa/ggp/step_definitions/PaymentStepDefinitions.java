
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;

import net.gree.asdk.api.GreePlatform;
import util.PopupHandler;
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

    @SuppressWarnings("unchecked")
    @When("I add payment item with ID (\\w+) and NAME (.+) and UNIT_PRICE (\\d+) and QUANTITY (\\d+) and IMAGE_URL (.+) and DESCRIPTION (.+)")
    public void initPaymentItem(String itemId, String itemName, int unitPrice, int quantity,
            String imgUrl, String desc) {
        String[] params = {
                itemId, itemName, String.valueOf(unitPrice), String.valueOf(quantity), imgUrl, desc
        };
        if (getBlockRepo().get(PAYMENT_PARAMS) == null) {
            ArrayList<String[]> list = new ArrayList<String[]>();
            list.add(params);
            getBlockRepo().put(PAYMENT_PARAMS, list);
        } else {
            ((ArrayList<String[]>) getBlockRepo().get(PAYMENT_PARAMS)).add(params);
        }

    }

    @SuppressWarnings("unchecked")
    @And("I did open the payment request popup")
    public void sendPaymentRequest() {

        Intent intent_open = new Intent(PopupHandler.ACTION_PAYMENT_POPUP);
        intent_open.putExtra(PopupHandler.POPUP_PARAMS,
                (ArrayList<String[]>) getBlockRepo().get(PAYMENT_PARAMS));

        notifyStepWait();
        Step.setTimeout(40000);

        PopupHandler.is_popup_opened = false;
        // destroy payment params
        getBlockRepo().remove(PAYMENT_PARAMS);
        // ask action queue to open popup
        GreePlatform.getContext().sendBroadcast(intent_open);

        // wait payment popup dialog to open
        while (!PopupHandler.is_popup_opened) {
            Log.d(TAG, "waiting for payment popup open!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Intent intent_check_loaded = new Intent(PopupHandler.ACTION_CHECK_PAYMENT_POPUP_LOADED);
        GreePlatform.getContext().sendOrderedBroadcast(intent_check_loaded, null,
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (PopupHandler.RESULT_SUCCESS == getResultCode()) {
                            Log.d(TAG, "load payment popup success!");
                        } else if (PopupHandler.RESULT_FAILURE == getResultCode()) {
                            Log.e(TAG, "load payment popup failed!");
                        } else if (PopupHandler.RESULT_UNKNOWN == getResultCode()) {
                            Log.e(TAG, "something wrong, still unknown status!");
                        }
                        notifyStepPass();
                    }
                }, null, PopupHandler.RESULT_UNKNOWN, null, null);
    }

    @When("I check payment request popup info payment items")
    public void getPaymentPopupInfo() {
        PopupUtil.getValueFromPopup("fclass('flexible')) + stringify(ftag('img'))");
    }

    @Then("payment popup info (\\w+) should be (.+)")
    @And("payment popup info (\\w+) should be (.+)")
    public void verifyPaymentPopupInfo(String column, String expectValue) {
        assertTrue("value from payment popup", PopupHandler.valueToBeVerified.contains(expectValue));
    }
}
