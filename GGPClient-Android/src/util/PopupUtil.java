
package util;

import java.lang.reflect.Method;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.core.ui.PopupDialog;
import net.gree.asdk.core.ui.WebViewPopupDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.webkit.WebView;

import com.openfeint.qa.ggp.MainActivity;

public class PopupUtil {
    private final static String TAG = "Popup_Util";

    public static final String BASIC_JS_COMMAND = "var STEP_TIMEOUT=250;"
            + "var STR_TEMPLATE='{\"id\":\"#id\", \"tag\":\"#tag\", \"class\":\"#class\", \"text\":\"#text\","
            + " \"src\":\"#src\", \"index\":\"#index\"}';"
            + "function hl(e){var d=e.style.outline;e.style.outline='#FDFF47 solid';setTimeout(function(){e.style.outline=d},STEP_TIMEOUT)}"
            + "function fid(id){return document.getElementById(id)}"
            + "function fclass(clazz){return document.getElementsByClassName(clazz)}"
            + "function ftag(g){return document.getElementsByTagName(g)}"
            + "function click(e){var t=document.createEvent('HTMLEvents');t.initEvent('click',false,false);"
            + "setTimeout(function(){hl(e);setTimeout(function(){e.dispatchEvent(t)},STEP_TIMEOUT)},STEP_TIMEOUT)}"
            + "function setText(e,t){setTimeout(function(){hl(e);setTimeout(function(){e.value=t},STEP_TIMEOUT)},STEP_TIMEOUT)}"
            + "function getText(e){var r=e.value;if(r===''||typeof(r)=='undefined'){r=e.innerText}hl(e);return r}"
            + "function stringify(es){var r='{elements:[';if(es.constructor==NodeList){for(var i=0;i<es.length;i++){"
            + "var ret='';ret=STR_TEMPLATE.replace('#tag',es[i].tagName).replace('#id',es[i].getAttribute('id'))."
            + "replace('#class',es[i].getAttribute('class')).replace('#text',getText(es[i])).replace('#src',es[i].getAttribute('src'))."
            + "replace('#index',i);r=r+ret+','}r=r+']}'}else{r=r+STR_TEMPLATE.replace('#tag',es.tagName)."
            + "replace('#id',es.getAttribute('id')).replace('#class',es.getAttribute('class'))."
            + "replace('#text',getText(es)).replace('#src',es.getAttribute('src')).replace('#index',0)+']}'}return r}";

    public static double getSimilarityOfPopupView(int expectImageId) {
        // Wait the main thread to refresh view of popup
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        MainActivity activity = MainActivity.getInstance();
        final PopupDialog popupDialog = PopupHandler.getPopupDialog();
        if (popupDialog == null) {
            Log.e(TAG, "Popup Dialog is null!!!");
            return 0;
        }
        double sRate = 0;
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
                Thread.sleep(2000);
            }

            Bitmap expect_image = ImageUtil.zoomBitmap(BitmapFactory.decodeResource(GreePlatform
                    .getContext().getResources(), expectImageId), MainActivity.dialog_bitmap
                    .getWidth(), MainActivity.dialog_bitmap.getHeight());
            sRate = ImageUtil.compareImage(MainActivity.dialog_bitmap, expect_image);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sRate;
    }

    public static void dismissPopupDialog() {
        MainActivity activity = MainActivity.getInstance();
        final PopupDialog dialog = PopupHandler.getPopupDialog();
        if (dialog == null)
            return;
        // call main thread to dismiss the popup
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        });
        // Wait main Thread to dissmiss the popup
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static WebView getWebViewFromPopup(PopupDialog popup) throws Exception {
        Method getWebViewClientMethod = WebViewPopupDialog.class.getDeclaredMethod("getWebView");
        getWebViewClientMethod.setAccessible(true);
        return (WebView) getWebViewClientMethod.invoke(popup);
    }

    public static void getValueFromPopup(final String statementToGetElement) {
        final MainActivity activity = MainActivity.getInstance();
        final PopupDialog popupDialog = PopupHandler.getPopupDialog();

        if (popupDialog == null)
            Log.e(TAG, "Popup Dialog is null!!!");
        try {
            PopupHandler.valueToBeVerified = null;
            final WebView view = PopupUtil.getWebViewFromPopup(popupDialog);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.loadUrl("javascript:" + BASIC_JS_COMMAND);
                    view.loadUrl("javascript:(function(){window.popupStep.returnValueFromPopup(stringify("
                            + statementToGetElement + ")}) ()");
                }
            });

            // wait main thread to execute the JS
            int count = 0;
            while ((PopupHandler.valueToBeVerified == null || ""
                    .equals(PopupHandler.valueToBeVerified)) && count < 10) {
                Thread.sleep(1000);
                count++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
