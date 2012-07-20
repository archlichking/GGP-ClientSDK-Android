
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;

import net.gree.asdk.api.wallet.Payment;
import net.gree.asdk.api.wallet.PaymentItem;
import net.gree.asdk.core.ui.PopupDialog;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.ggp.MainActivity;

public class PaymentStepDefinitions extends BasicStepDefinition {
    private static final String TAG = "Payment_Steps";

    private static final String ITEM_LIST = "item_list";

    @SuppressWarnings("unchecked")
    @When("I add payment item \\[(.+)\\]")
    public void addPaymentItem(String itemString) {
        String[] values = itemString.split(",");
        getBlockRepo().put(ITEM_LIST, new ArrayList<PaymentItem>());
        PaymentItem item = new PaymentItem(values[0], values[1], Double.parseDouble(values[2]),
                Integer.parseInt(values[3]));
        ((ArrayList<PaymentItem>) getBlockRepo().get(ITEM_LIST)).add(item);
    }

    @SuppressWarnings("unchecked")
    @And("I send out the payment request")
    public void sendPaymentRequest() {
        ArrayList<PaymentItem> itemList = new ArrayList<PaymentItem>();
        if (getBlockRepo().get(ITEM_LIST) != null) {
            itemList = (ArrayList<PaymentItem>) getBlockRepo().get(ITEM_LIST);
        }
        Payment payment = new Payment("test item", itemList);
        payment.setCallbackUrl("");

        MainActivity activity = MainActivity.getInstance();
        Message msg = activity.popup_handler.obtainMessage(PopupStepDefinitions.POPUP_PAYMENT);
        msg.obj = payment;
        PopupStepDefinitions.openPopup(msg, "submit_btn");
    }

    @Then("payment popup info (\\w+) should be (.+)")
    @And("payment popup info (\\w+) should be (.+)")
    public void verifyPaymentPopupInfo(String column, String expectValue) {
        String statementToGetElement = "";
        if ("NAME".equals(column)) {
            statementToGetElement = "document.getElementsByClassName('title large')[0]";
        } else if ("UNIT_PRICE".equals(column)) {
            statementToGetElement = "document.getElementsByTagName('li')[0]";
        } else if ("QUANTITY".equals(column)) {
            statementToGetElement = "document.getElementsByTagName('li')[1]";
        } else if ("IMAGE_URL".equals(column)) {
            statementToGetElement = "document.getElementsByClassName('sentence medium minor')[0]";
        } else if ("DESCRIPTION".equals(column)) {
            statementToGetElement = "document.getElementsByTagName('li')[2]";
        }
        Log.d(TAG, "get value of column: " + statementToGetElement);
        PopupStepDefinitions.valueToBeVerified = null;
        getValueFromPopup(statementToGetElement);
        int count = 0;
        while ((PopupStepDefinitions.valueToBeVerified == null || ""
                .equals(PopupStepDefinitions.valueToBeVerified)) && count < 5) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        verifyValueFromPopup(column, expectValue);
    }

    private void verifyValueFromPopup(String column, String expectValue) {
        Log.d(TAG, "expect: " + expectValue + " and actual: "
                + PopupStepDefinitions.valueToBeVerified);
        if ("DESCRIPTION".equals(column) || "IMAGE_URL".equals(column)) {
            assertEquals("value from payment popup", expectValue,
                    PopupStepDefinitions.valueToBeVerified.trim());
        } else if ("NAME".equals(column) || "UNIT_PRICE".equals(column)
                || "QUANTITY".equals(column)) {
            assertTrue("value from payment popup",
                    PopupStepDefinitions.valueToBeVerified.contains(expectValue));
        }
    }

    private void getValueFromPopup(final String statementToGetElement) {
        final MainActivity activity = MainActivity.getInstance();
        final PopupDialog popupDialog = activity.getPopupDialog();
        if (popupDialog == null)
            Log.e(TAG, "Popup Dialog is null!!!");
        try {
            final WebView view = PopupStepDefinitions.getWebViewFromPopup(popupDialog);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.loadUrl("javascript:(function(){window.popupStep.returnValueFromPopup("
                            + statementToGetElement + ".textContent)}) ()");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
