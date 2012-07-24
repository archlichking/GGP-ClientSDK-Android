
package util;

import java.util.TreeMap;

import net.gree.asdk.api.ui.RequestDialog;
import net.gree.asdk.core.ui.PopupDialog;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;

import com.openfeint.qa.ggp.MainActivity;

public class ActionQueue extends BroadcastReceiver {
    private final String TAG = "Action_Queue";

    public static final int POPUP_UNKNOWN = 0;

    public static final int POPUP_REQUEST = 1;

    public static final int POPUP_PAYMENT = 4;

    public static final String ACTION_REQUEST_POPUP = "util.ActionQueue.request_popup";

    public static final String PARAMS = "util.ActionQueue.params";

    public static final String LISTENER = "util.ActionQueue.listener";

    private static boolean is_popup_loading_done;

    public static String valueToBeVerified;

    private String popupElementId;

    private static PopupDialog popupDialog;

    private ActionQueueListener listener;

    private boolean is_dialog_opened;
    
    @Override
    public void onReceive(Context context, final Intent intent) {
        Log.d(TAG, "=============== Receive new action: " + intent.getAction() + "===============");

        String[] params = intent.getStringArrayExtra(PARAMS);
        listener = (ActionQueueListener) intent.getSerializableExtra(LISTENER);

        if (ACTION_REQUEST_POPUP == intent.getAction()) {
            TreeMap<String, Object> map = new TreeMap<String, Object>();
            map.put("title", params[0]);
            map.put("body", params[1]);
            popupElementId = "btn-msg-choosed";

            handlerSNSPopup(POPUP_REQUEST, map);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void checkPopupLoaded() {
        try {
            final WebView view = PopupUtil.getWebViewFromPopup(popupDialog);

            // add javascript interface into webview before it loaded
            Log.d(TAG, "add JS interface...");
            is_popup_loading_done = false;
            view.getSettings().setJavaScriptEnabled(true);
            view.addJavascriptInterface(new Object() {
                @SuppressWarnings("unused")
                public void notifyPopupLoadingDone() {
                    Log.d(TAG, "tell native that popup is loaded...");
                    is_popup_loading_done = true;
                }

                @SuppressWarnings("unused")
                public void returnValueFromPopup(String val) {
                    valueToBeVerified = val;
                }
            }, "popupStep");

            // Check if page is loaded
            final String data = "(function() {function waitPageLoading(){if(document.getElementById('"
                    + popupElementId
                    + "')&&'undefined'!=typeof(window.popupStep))"
                    + "{window.popupStep.notifyPopupLoadingDone()}} return(waitPageLoading()) }) ()";
            Log.d(TAG, "check if page loaded...");

            Runnable checkPageLoad_thread = new Runnable() {
                @Override
                public void run() {
                    int count = 0;
                    while (!is_popup_loading_done && count <= 5) {
                        Log.d(TAG, "waiting popup to load...");
                        MainActivity.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                view.loadUrl("javascript:" + data);
                            }
                        });
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        count++;
                    }
                    if (is_popup_loading_done) {
                        listener.onSuccess();
                    } else {
                        listener.onFailure();
                    }
                }
            };
            new Thread(checkPageLoad_thread).start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handlerSNSPopup(int popupType, TreeMap<String, Object> params) {
        switch (popupType) {
            case POPUP_REQUEST:
                Log.d(TAG, "Trying to open request dialog...");
                is_dialog_opened = false;
                Handler handler = new Handler() {
                    public void handleMessage(Message message) {
                        switch (message.what) {
                            case RequestDialog.OPENED:
                                Log.i(TAG, "Reqest Dialog opened.");
                                checkPopupLoaded();
                                break;
                            case RequestDialog.CLOSED:
                                Log.i(TAG, "Request Dialog closed.");
                                break;
                            default:
                        }
                    }
                };
                RequestDialog requestDialog = new RequestDialog(MainActivity.getInstance());
                requestDialog.setParams(params);
                requestDialog.setHandler(handler);
                requestDialog.show();
                popupDialog = requestDialog;
                break;
            default:
                break;
        // case POPUP_PAYMENT:
        // Log.d(TAG, "Trying to open payment dialog...");
        // final Payment payment = (Payment) message.obj;
        // payment.setHandler(new Handler() {
        // public void handleMessage(Message message) {
        // switch (message.what) {
        // case Payment.OPENED:
        // Log.d("Payment", "PaymentDialog opened.");
        // try {
        // Field dialog_field = Payment.class
        // .getDeclaredField("mPaymentDialog");
        // dialog_field.setAccessible(true);
        // int count = 0;
        // while (dialog_field.get(payment) == null && count < 10) {
        // Thread.sleep(1000);
        // count++;
        // }
        // popupDialog = (PopupDialog) dialog_field.get(payment);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // break;
        // case Payment.CANCELLED:
        // Log.d("Payment", "PaymentDialog canceled.");
        // case Payment.ABORTED:
        // Log.d("Payment", "PaymentDialog closed.");
        // break;
        // }
        // }
        // });
        // payment.request(MainActivity.getInstance(), new
        // PaymentListener() {
        // @Override
        // public void onSuccess(int responseCode, HeaderIterator
        // headers,
        // String paymentId) {
        // Log.d(TAG, "payment.request() succeeded.");
        // }
        //
        // @Override
        // public void onFailure(int responseCode, HeaderIterator
        // headers,
        // String paymentId, String response) {
        // Log.d(TAG, "payment.request() failed.");
        // }
        //
        // @Override
        // public void onCancel(int responseCode, HeaderIterator
        // headers, String
        // paymentId) {
        // Log.d(TAG, "payment.request() canceled.");
        // }
        // });
        //
        // default:
        }
    }
}
