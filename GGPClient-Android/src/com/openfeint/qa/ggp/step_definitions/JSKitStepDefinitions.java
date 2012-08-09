
package com.openfeint.qa.ggp.step_definitions;

import net.gree.asdk.api.GreePlatform;
import util.PopupUtil;
import android.content.Intent;
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

    @And("I open JSkit popup")
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

    @When("use JSkit to set configuration (\\w+) to (\\w+)")
    public void setConfigByJSkit(final String config, final String value) {
        final GreeWebViewActivity activity = GreeWebViewActivity.getInstance();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebView view = (WebView) activity.findViewById(R.id.greewebview);
                view.loadUrl("javascript:" + PopupUtil.BASIC_JS_COMMAND);
                view.loadUrl("javascript:fid('configuration-key').value=" + config + ";"
                        + "fid('configuration-value').value='" + value + "';"
                        + "ftext(ftag('button'),'setConfig').click()");
            }
        });
    }

    @Then("configuration (\\w+) should be (\\w+)")
    public void verifyConfig() {
        // TODO
    }
}
