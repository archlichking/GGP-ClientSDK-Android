
package com.openfeint.qa.ggp.step_definitions;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.core.ui.GreeWebViewUtil;
import net.gree.asdk.core.util.CoreData;
import util.PopupUtil;
import android.app.Instrumentation;
import android.content.Intent;
import android.view.KeyEvent;
import android.webkit.WebView;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.ggp.GreeWebViewActivity;
import com.openfeint.qa.ggp.MainActivity;
import com.openfeint.qa.ggp.R;

public class JSKitStepDefinitions extends BasicStepDefinition {
    private static final String TAG = "JSKit_Steps";

    private static final String KEY_TEST_DONE = "jskitTestDone";

    private static final String KEY_POPUP_LOADED = "popupLoaded";

    @And("I launch jskit popup")
    public void openJSkitPopup() {
        final MainActivity activity = MainActivity.getInstance();
        final Intent intent = new Intent(GreePlatform.getContext(), GreeWebViewActivity.class);
        activity.startActivity(intent);
        // wait main thread to open activity
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @When("I click invoke button (\\w+)")
    public void invokeNonPopupMethods(String buttonId) {
        // default waiting for the last UI action
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        doActionInJSKitPopup("click(fid('" + buttonId + "'))");
    }

    private void doActionInJSKitPopup(final String command) {
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

    @Then("I need to wait for test done (\\w+)")
    public void verifyResult(String param) {
        int count = 0;
        while (count < 10 && !"true".equals(CoreData.get(KEY_TEST_DONE))) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        // wait another seconds to let callbacks return
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CoreData.put(KEY_TEST_DONE, "");
    }

    @After("I dismiss jskit base popup")
    public void closeJSKitView() {
        GreeWebViewActivity activity = GreeWebViewActivity.getInstance();
        activity.finish();
    }

    private void waitPopupToLoad() {
        int count = 0;
        while (count < 10 && !"true".equals(CoreData.get(KEY_POPUP_LOADED))) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        CoreData.put(KEY_POPUP_LOADED, "");
        // need another seconds for UI thread to popup window
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @And("I dismiss last opened popup")
    public void closePopup() {
        waitPopupToLoad();
        GreeWebViewActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (GreeWebViewUtil.getDialog() != null) {
                    GreeWebViewUtil.getDialog().dismiss();
                    GreeWebViewUtil.releaseDialog();
                }
            }
        });
    }

    @Then("I dismiss last opened viewControl")
    public void closeViewControl() {
        waitPopupToLoad();
        Instrumentation inst = new Instrumentation();
        // click back button to stop view loading
        inst.sendCharacterSync(KeyEvent.KEYCODE_BACK);
        // wait view to stop loading
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // click back button to close view
        inst.sendCharacterSync(KeyEvent.KEYCODE_BACK);
    }
}
