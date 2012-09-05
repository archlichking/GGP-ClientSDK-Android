
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.fail;
import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.core.ui.GreeWebView;
import net.gree.asdk.core.ui.GreeWebViewUtil;
import net.gree.asdk.core.util.CoreData;
import util.PopupUtil;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Looper;
import android.view.KeyEvent;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.ggp.JSKitTestActivity;
import com.openfeint.qa.ggp.JSKitTestDialog;
import com.openfeint.qa.ggp.MainActivity;
import com.openfeint.qa.ggp.R;

public class JSKitStepDefinitions extends BasicStepDefinition {
    private static final String TAG = "JSKit_Steps";

    private static final String KEY_TEST_DONE = "jskitTestDone";

    private static final String KEY_POPUP_LOADED = "popupLoaded";

    private static final String POPUP_TYPE = "popupType";

    private static final String TYPE_VIEW_CONTROL = "view_control";

    private static final String TYPE_DIALOG = "dialog";

    @And("I launch jskit popup")
    public void openJSkitPopupView() {
        MainActivity activity = MainActivity.getInstance();
        Intent intent = new Intent(GreePlatform.getContext(), JSKitTestActivity.class);
        activity.startActivity(intent);
        getBlockRepo().put(POPUP_TYPE, TYPE_VIEW_CONTROL);

        // wait main thread to open activity
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // inject JS basic command
        JSKitTestActivity jsActivity = JSKitTestActivity.getInstance();
        loadCommand(jsActivity, (GreeWebView) jsActivity.findViewById(R.id.greewebview),
                PopupUtil.BASIC_JS_COMMAND);
    }

    @And("I launch jskit dialog")
    public void openJSKitDialog() {
        final MainActivity activity = MainActivity.getInstance();
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                JSKitTestDialog dialog = new JSKitTestDialog(activity);
                dialog.show();
            }
        });
        getBlockRepo().put(POPUP_TYPE, TYPE_DIALOG);

        // wait main thread to open dialog
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // inject JS basic command
        loadCommand(activity, JSKitTestDialog.getInstance().getWebView(),
                PopupUtil.BASIC_JS_COMMAND);
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
        // waiting for native sdk to take action
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doActionInJSKitPopup(final String command) {
        String type = (String) getBlockRepo().get(POPUP_TYPE);
        if (type == null)
            fail("Launch JSKit popup first!");

        Activity activity = null;
        GreeWebView webView = null;
        if (TYPE_VIEW_CONTROL.equals(type)) {
            activity = JSKitTestActivity.getInstance();
            webView = (GreeWebView) activity.findViewById(R.id.greewebview);
        } else if (TYPE_DIALOG.equals(type)) {
            activity = MainActivity.getInstance();
            webView = JSKitTestDialog.getInstance().getWebView();
        }
        loadCommand(activity, webView, command);
    }

    private void loadCommand(final Activity activity, final GreeWebView webview,
            final String command) {
        if (Looper.myLooper() == null
                || Looper.myLooper().getThread() != Looper.getMainLooper().getThread()) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webview.loadUrl("javascript:" + command);
                }
            });
        } else {
            webview.loadUrl("javascript:" + command);
        }
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
        String type = (String) getBlockRepo().get(POPUP_TYPE);
        if (TYPE_VIEW_CONTROL.equals(type)) {
            JSKitTestActivity.getInstance().finish();
        } else if (TYPE_DIALOG.equals(type)) {
            MainActivity.getInstance().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    JSKitTestDialog.getInstance().dismiss();
                }
            });
        }
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
        JSKitTestActivity.getInstance().runOnUiThread(new Runnable() {
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

    @And("I go back to jskit test page")
    public void goBackToBasePage() {
        waitPopupToLoad();
        final MainActivity activity = MainActivity.getInstance();
        final GreeWebView webView = JSKitTestDialog.getInstance().getWebView();
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (webView.canGoBack()) {
                    JSKitTestDialog.getInstance().getWebView().goBack();
                } else {
                    webView.loadUrl(JSKitTestActivity.JSKIT_BASE_PAGE);
                }
                // wait page to go back
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // inject JS basic command
                loadCommand(activity, JSKitTestDialog.getInstance().getWebView(),
                        PopupUtil.BASIC_JS_COMMAND);
            }
        });
    }
}
