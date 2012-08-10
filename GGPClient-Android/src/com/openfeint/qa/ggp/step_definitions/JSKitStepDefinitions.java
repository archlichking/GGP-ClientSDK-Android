
package com.openfeint.qa.ggp.step_definitions;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.core.util.CoreData;
import util.PopupUtil;
import android.content.Intent;
import android.util.Log;
import android.webkit.WebView;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.ggp.GreeWebViewActivity;
import com.openfeint.qa.ggp.MainActivity;
import com.openfeint.qa.ggp.R;

public class JSKitStepDefinitions extends BasicStepDefinition {
    private static final String TAG = "JSKit_Steps";

    private static final String JSKIT_RESULT = "fid('content').textContent";

    @And("I open JSKit popup")
    public void openJSkitPopup() {
        final MainActivity activity = MainActivity.getInstance();
        Intent intent = new Intent(GreePlatform.getContext(), GreeWebViewActivity.class);
        activity.startActivity(intent);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @When("I click invoke all button")
    public void invokeAll() {
        doActionByJSKit("fid('invokeAll').click()");
    }

    @Then("all JSKit methods on popup should be tested")
    public void verifyResult() {
        int count = 0;
        while (count < 10 && CoreData.get("jskitTestDone") == null) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        Log.e(TAG, "result is: " + CoreData.get("jskitTestDone"));
    }

    private void doActionByJSKit(final String command) {
        final GreeWebViewActivity activity = GreeWebViewActivity.getInstance();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebView view = (WebView) activity.findViewById(R.id.greewebview);
                view.loadUrl("javascript:" + PopupUtil.BASIC_JS_COMMAND);
                view.loadUrl("javascript:" + command);
            }
        });
    }

    @When("use JSKit to set configuration (\\w+) to (\\w+)")
    public void setConfigByJSkit(final String config, final String value) {
        doActionByJSKit("fid('configuration-key').value=" + config + ";"
                + "fid('configuration-value').value='" + value + "';"
                + "ftext(ftag('button'),'setConfig').click()");
    }

    @Then("configuration (\\w+) should be (\\w+)")
    public void verifyConfig() {
        // TODO
    }

    @When("I use JSKit to open dashboard")
    public void openDashboardByJSKit() {
        doActionByJSKit("ftext(ftag('button'),'showDashboardTest').click()");
    }

    @Then("dashboard should be opened")
    public void verifyDashboardOpened() {
        doActionByJSKit("console.log('" + JSKIT_RESULT + "')");
    }
}
