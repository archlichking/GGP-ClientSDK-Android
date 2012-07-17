
package com.openfeint.qa.ggp.step_definitions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.TreeMap;

import junit.framework.Assert;
import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.ui.RequestDialog;
import net.gree.asdk.core.ui.AuthorizableDialog;
import net.gree.asdk.core.ui.PopupDialog;
import net.gree.asdk.core.ui.WebViewPopupDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.ggp.MainActivity;
import com.openfeint.qa.ggp.R;

public class PopupStepDefinitions extends BasicStepDefinition {

    private final String TAG = "Popup_Steps";

    private static final String POPUP_PARAMS = "popup_params";

    private static final String POPUP_TYPE = "popup_type";

    public static final String HANDLER = "handler";

    private boolean is_popup_loading_done;

    public static final int UNKNOWN_POPUP = 0;

    public static final int REQUEST_POPUP = 1;

    @And("I initialize (\\w+) popup with title (.+) and body (.+)")
    public void initPopupDialog(String type, String title, String body) {
        TreeMap<String, Object> params = new TreeMap<String, Object>();
        params.put("title", title);
        params.put("body", body);

        getBlockRepo().put(POPUP_PARAMS, params);
        if ("request".equals(type)) {
            getBlockRepo().put(POPUP_TYPE, REQUEST_POPUP);
        } else {
            Log.e(TAG, "unknown popup type!");
            getBlockRepo().put(POPUP_TYPE, UNKNOWN_POPUP);
        }
    }

    @When("I did open popup")
    public void openPopup() {
        MainActivity activity = MainActivity.getInstance();
        if ((Integer) getBlockRepo().get(POPUP_TYPE) == UNKNOWN_POPUP) {
            return;
        }
        Message msg = activity.popup_handler
                .obtainMessage((Integer) getBlockRepo().get(POPUP_TYPE));
        msg.obj = getBlockRepo().get(POPUP_PARAMS);
        activity.popup_handler.sendMessage(msg);

        while (!MainActivity.is_dialog_opened) {
            Log.d(TAG, "waiting popup to open...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        final RequestDialog requestDialog = activity.getRequestDialog();
        if (requestDialog == null)
            Log.e(TAG, "Request Dialog is null!!!");

        try {
            final WebView view = getWebViewFromPopup(requestDialog);

            // add javascript interface into webview before it loaded
            is_popup_loading_done = false;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.getSettings().setJavaScriptEnabled(true);
                    view.addJavascriptInterface(new Object() {
                        @SuppressWarnings("unused")
                        public void notifyPopupLoadingDone() {
                            Log.d(TAG, "tell native that popup is loaded...");
                            is_popup_loading_done = true;
                        }
                    }, "popupStep");
                }
            });

            // Check if page is loaded
            Thread.sleep(3000);

            int times = 0;
            Runnable check_task = new Runnable() {
                @Override
                public void run() {
                    String data = "(function() {function waitPageLoading(){if(document.getElementById('btn-msg-choosed')"
                            + "&&'undefined'!=typeof(window.popupStep))"
                            + "{window.popupStep.notifyPopupLoadingDone()}} return(waitPageLoading()) }) ()";
                    view.loadUrl("javascript:" + data);
                }
            };
            while (!is_popup_loading_done && times <= 10) {
                Log.d(TAG, "still waiting...");
                activity.runOnUiThread(check_task);
                Thread.sleep(5000);
                times++;
            }

            if (is_popup_loading_done) {
                Log.d(TAG, "Popup is loaded!!!");

            }

            // saveImageAsExpectedResult(Environment.getExternalStorageDirectory().getAbsolutePath(),
            // "expect_request_dialog.png");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // TODO just for debug ignore this
    @When("I debug picture comparison")
    public void screenshotComparison() {
        String sdcard_path = Environment.getExternalStorageDirectory().getAbsolutePath();
        Bitmap image_from_laptop = zoomBitmap(BitmapFactory.decodeResource(GreePlatform
                .getContext().getResources(), R.drawable.expect_request_dialog), 408, 580);
        File file_from_device = new File(sdcard_path + "/expect_request_dialog.png");
        double sRate = compareImage(image_from_laptop, getBitmapFromFile(file_from_device));
        Log.e(TAG, "sRate: " + sRate);
    }

    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        return newbmp;
    }

    private WebView getWebViewFromPopup(PopupDialog popup) throws Exception {
        Method getWebViewClientMethod = WebViewPopupDialog.class.getDeclaredMethod("getWebView");
        getWebViewClientMethod.setAccessible(true);
        return (WebView) getWebViewClientMethod.invoke(popup);
    }

    private Bitmap getBitmapFromFile(File file) {
        FileInputStream fis = null;
        Bitmap bm = null;
        try {
            fis = new FileInputStream(file);
            bm = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        return bm;
    }

    private static Object[][] getRGBList(Bitmap bm) {

        if (bm == null) {
            Log.e("Image Compare", "Bitmap is null!");
            return null;
        }

        int width = bm.getWidth();
        int height = bm.getHeight();
        Object[][] list = new Object[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int color = bm.getPixel(i, j);
                int rgb[] = new int[3];
                rgb[0] = (color & 0xff);
                rgb[1] = (color & 0xff00) >> 8;
                rgb[2] = (color & 0xff0000) >> 16;

                list[i][j] = rgb;
            }
        }
        return list;
    }

    // return similarity as a integer rate between 0~100
    public static double compareImage(Bitmap img1, Bitmap img2) {
        Object[][] list1 = getRGBList(img1);
        Object[][] list2 = getRGBList(img2);

        if (list1 == null || list2 == null) {
            return 0;
        }

        double xiangsi = 0;
        double busi = 0;
        double sRate = 0;
        for (int i = 0; i < list1.length; i++) {
            for (int j = 0; j < list1[i].length; j++) {
                try {
                    int[] value1 = (int[]) list1[i][j];
                    int[] value2 = (int[]) list2[i][j];
                    boolean flag = true;
                    for (int k = 0; k < value1.length && k < value2.length; k++) {
                        if (Math.abs(value1[k] - value2[k]) > 16) {
                            flag = false;
                            busi++;
                            break;
                        }
                    }
                    if (flag) {
                        xiangsi++;
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    continue;
                }
                j++;
            }
            i++;
        }
        sRate = xiangsi / (xiangsi + busi) * 100;
        return sRate;
    }

    @Then("request popup should open as we expected")
    public void verifyRequestPopup() {
        MainActivity activity = MainActivity.getInstance();
        final RequestDialog requestDialog = activity.getRequestDialog();
        if (requestDialog == null)
            Log.e(TAG, "Request Dialog is null!!!");
        try {
            final WebView view = getWebViewFromPopup(requestDialog);

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

            Bitmap expect_image = zoomBitmap(BitmapFactory.decodeResource(GreePlatform.getContext()
                    .getResources(), R.drawable.expect_request_dialog), 408, 580);
            double sRate = compareImage(MainActivity.dialog_bitmap, expect_image);
            Log.d(TAG, "Similarity rate: " + sRate);
            Assert.assertTrue("popup similarity is bigger than 80%", sRate > 80);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO for data preparation
    private void saveImageAsExpectedResult(String path, String img_name) {
        File img = new File(path, img_name);
        MainActivity activity = MainActivity.getInstance();
        final RequestDialog requestDialog = activity.getRequestDialog();
        try {
            final WebView view = getWebViewFromPopup(requestDialog);
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
    public void dismissPopup() {
        MainActivity activity = MainActivity.getInstance();
        final AuthorizableDialog dialog = activity.getRequestDialog();

        // call main thread to dismiss the popup
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        });
    }

}
