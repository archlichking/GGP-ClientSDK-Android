
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.ui.RequestDialog;
import net.gree.asdk.core.ui.PopupDialog;
import util.ActionQueue;
import util.ActionQueueListener;
import util.PopupUtil;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;

import com.openfeint.qa.core.caze.step.Step;
import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.ggp.MainActivity;
import com.openfeint.qa.ggp.R;

public class PopupStepDefinitions extends BasicStepDefinition implements Serializable {

    private static final String TAG = "Popup_Steps";

    private static final String POPUP_PARAMS = "popup_params";

    private static final String POPUP_TYPE = "popup_type";

    public static final String HANDLER = "handler";
    
    private static boolean is_action_end_success;
    
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
        intent.putExtra(ActionQueue.PARAMS, (String[]) getBlockRepo().get(POPUP_PARAMS));

        intent.putExtra(ActionQueue.LISTENER, new ActionQueueListener() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSuccess() {
                Log.d(TAG, "Popup is loaded success...");
                notifyEnd();
            }

            @Override
            protected void onFailure() {
                Log.e(TAG, "Popup can not load!!");
                notifyEnd();
            }
        });
        is_action_end_success = false;
        GreePlatform.getContext().sendBroadcast(intent);
        int count = 0;
        while (!is_action_end_success) {
            if (count >=8) {
                Log.e(TAG, "open popup time out!");
                return;
            } 
            try {
                Log.d(TAG, "wait 5 secs...");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
    }
    
    private static void notifyEnd() {
        Log.d(TAG, "notify end!");
        is_action_end_success = true;
    }

    @Then("request popup should open as we expected")
    public void mock() {

    }

    public void verifyRequestPopup() {
        MainActivity activity = MainActivity.getInstance();
        final PopupDialog popupDialog = activity.getPopupDialog();
        if (popupDialog == null)
            Log.e(TAG, "Popup Dialog is null!!!");
        try {
            final WebView view = PopupUtil.getWebViewFromPopup(popupDialog);

            // Call main thread to build bitmap of popup
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.dialog_bitmap = null;
                    view.buildDrawingCache();
                    MainActivity.dialog_bitmap = view.getDrawingCache();
                }
            });

            // wait bitmap of popup created
            int times = 0;
            while (MainActivity.dialog_bitmap == null && times < 5) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Bitmap expect_image = PopupUtil.zoomBitmap(BitmapFactory.decodeResource(GreePlatform
                    .getContext().getResources(), R.drawable.expect_request_dialog),
                    MainActivity.dialog_bitmap.getWidth(), MainActivity.dialog_bitmap.getHeight());
            double sRate = PopupUtil.compareImage(MainActivity.dialog_bitmap, expect_image);
            Log.d(TAG, "Similarity rate: " + sRate);
            assertTrue("popup similarity is bigger than 80%", sRate > 80);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO for data preparation
    private void saveImageAsExpectedResult(String path, String img_name) {
        File img = new File(path, img_name);
        MainActivity activity = MainActivity.getInstance();
        final RequestDialog requestDialog = (RequestDialog) activity.getPopupDialog();
        try {
            final WebView view = PopupUtil.getWebViewFromPopup(requestDialog);
            FileOutputStream fos = new FileOutputStream(img);
            // Call main thread to build bitmap of popup
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.dialog_bitmap = null;
                    view.buildDrawingCache();
                    MainActivity.dialog_bitmap = view.getDrawingCache();
                }
            });

            // wait bitmap of popup created
            int times = 0;
            while (MainActivity.dialog_bitmap == null && times < 5) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (MainActivity.dialog_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)) {
                Log.d(TAG, "Create expected image for popup success!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @After("I did dismiss popup")
    public void mock2() {
    }

    public void dismissPopup() {
        MainActivity activity = MainActivity.getInstance();
        final PopupDialog dialog = activity.getPopupDialog();
        if (dialog == null)
            return;
        // call main thread to dismiss the popup
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        });
    }

}
