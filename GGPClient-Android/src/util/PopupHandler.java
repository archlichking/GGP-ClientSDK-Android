
package util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import net.gree.asdk.api.ui.InviteDialog;
import net.gree.asdk.api.ui.RequestDialog;
import net.gree.asdk.api.ui.ShareDialog;
import net.gree.asdk.api.wallet.Payment;
import net.gree.asdk.api.wallet.Payment.PaymentListener;
import net.gree.asdk.api.wallet.PaymentItem;
import net.gree.asdk.core.ui.PopupDialog;

import org.apache.http.HeaderIterator;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;

import com.openfeint.qa.ggp.MainActivity;

public class PopupHandler extends BroadcastReceiver {
    private final String TAG = "Popup_Handler";

    public static final int POPUP_REQUEST = 1;

    public static final int POPUP_SHARE = 2;

    public static final int POPUP_INVITE = 3;

    public static final int RESULT_UNKNOWN = 0;

    public static final int RESULT_SUCCESS = 1;

    public static final int RESULT_FAILURE = 2;

    public static final String ACTION_REQUEST_POPUP = "util.PopupHandler.request_popup";

    public static final String ACTION_SHARE_POPUP = "util.PopupHandler.share_popup";

    public static final String ACTION_INVITE_POPUP = "util.PopupHandler.invite_popup";

    public static final String ACTION_PAYMENT_POPUP = "util.PopupHandler.payment_popup";

    public static final String ACTION_CHECK_PAYMENT_POPUP_LOADED = "util.PopupHandler.check_popup_loaded";

    public static final String ACTION_POPUP_DISMISS = "util.PopupHandler.dismiss";

    public static final String POPUP_PARAMS = "util.PopupHandler.params";

    public static final String PAYMENT_MESSAGE = "util.PopupHandler.paymentMessage";

    public static boolean is_popup_opened;

    public static boolean is_popup_closed;
    
    public static boolean is_popup_canceled;

    private static boolean is_popup_loading_done;

    public static String valueToBeVerified;

    private String popupElementId;

    private static PopupDialog popupDialog;

    private static Handler snsPopupHandler;

    @SuppressWarnings("unchecked")
    @Override
    public void onReceive(Context context, final Intent intent) {
        Log.d(TAG, "=============== Receive new action: " + intent.getAction() + "===============");

        if (ACTION_REQUEST_POPUP.equals(intent.getAction())) {
            String[] params = intent.getStringArrayExtra(POPUP_PARAMS);
            TreeMap<String, Object> map = new TreeMap<String, Object>();
            map.put("title", params[0]);
            map.put("body", params[1]);
            popupElementId = "btn-msg-choosed";

            openSNSPopup(POPUP_REQUEST, map);
            checkPopupLoaded();

        } else if (ACTION_SHARE_POPUP.equals(intent.getAction())) {
            String[] params = intent.getStringArrayExtra(POPUP_PARAMS);
            TreeMap<String, Object> map = new TreeMap<String, Object>();
            map.put("message", params[0]);
            popupElementId = "ggp_share_submit";

            openSNSPopup(POPUP_SHARE, map);
            checkPopupLoaded();

        } else if (ACTION_INVITE_POPUP.equals(intent.getAction())) {
            TreeMap<String, Object> params = new TreeMap<String, Object>();
            params.putAll((HashMap<String, Object>) intent.getSerializableExtra(POPUP_PARAMS));
            popupElementId = "form-submit";

            openSNSPopup(POPUP_INVITE, params);
            checkPopupLoaded();

        } else if (ACTION_PAYMENT_POPUP.equals(intent.getAction())) {
            ArrayList<String[]> params = (ArrayList<String[]>) intent
                    .getSerializableExtra(POPUP_PARAMS);
            String message = intent.getStringExtra(PAYMENT_MESSAGE);
            openPaymentPopup(params, message);
        } else if (ACTION_CHECK_PAYMENT_POPUP_LOADED.equals(intent.getAction())) {
            popupElementId = "submit_btn";
            checkPopupLoaded();
        } else if (ACTION_POPUP_DISMISS.equals(intent.getAction())) {
            dismissPopupDialog();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void checkPopupLoaded() {
        try {
            final WebView view = PopupUtil.getWebViewFromPopup(popupDialog);
            if (view == null) {
                Log.e(TAG, "popup is not opened!");
                return;
            }

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

            int count = 0;
            while (!is_popup_loading_done && count <= 5) {
                Log.d(TAG, "waiting popup to load...");
                view.loadUrl("javascript:" + data);
                Thread.sleep(5000);
                count++;
            }
            if (is_popup_loading_done) {
                setResultCode(RESULT_SUCCESS);
            } else {
                setResultCode(RESULT_FAILURE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void openSNSPopup(int popupType, TreeMap<String, Object> params) {
        switch (popupType) {
            case POPUP_REQUEST:
                Log.d(TAG, "Trying to open request dialog...");
                snsPopupHandler = new Handler() {
                    public void handleMessage(Message message) {
                        switch (message.what) {
                            case RequestDialog.OPENED:
                                is_popup_opened = true;
                                Log.i(TAG, "Request dialog opened.");
                                break;
                            case RequestDialog.CLOSED:
                                is_popup_closed = true;
                                Log.i(TAG, "Request dialog closed.");
                                break;
                            default:
                        }
                    }
                };
                RequestDialog requestDialog = new RequestDialog(MainActivity.getInstance());
                requestDialog.setParams(params);
                requestDialog.setHandler(snsPopupHandler);
                requestDialog.show();
                popupDialog = requestDialog;
                break;
            case POPUP_SHARE:
                snsPopupHandler = new Handler() {
                    public void handleMessage(Message message) {
                        switch (message.what) {
                            case ShareDialog.OPENED:
                                Log.i(TAG, "Share dialog opened.");
                                break;
                            case ShareDialog.CLOSED:
                                Log.i(TAG, "Share dialog closed.");
                                break;
                            default:
                        }
                    }
                };
                ShareDialog shareDialog = new ShareDialog(MainActivity.getInstance());
                shareDialog.setParams(params);
                shareDialog.setHandler(snsPopupHandler);
                shareDialog.show();
                popupDialog = shareDialog;
                break;
            case POPUP_INVITE:
                snsPopupHandler = new Handler() {
                    public void handleMessage(Message message) {
                        switch (message.what) {
                            case InviteDialog.OPENED:
                                Log.d("Invite", "InviteDialog opened.");
                                break;
                            case InviteDialog.CLOSED:
                                Log.d("Invite", "InviteDialog closed.");
                                break;
                        }
                    }
                };
                InviteDialog inviteDialog = new InviteDialog(MainActivity.getInstance());
                inviteDialog.setParams(params);
                inviteDialog.setHandler(snsPopupHandler);
                inviteDialog.show();
                popupDialog = inviteDialog;
                break;
            default:
                break;
        }
    }

    private void openPaymentPopup(ArrayList<String[]> paramList, String message) {
        // add payment item and init payment class
        ArrayList<PaymentItem> itemList = new ArrayList<PaymentItem>();
        for (String[] params : paramList) {
            PaymentItem item = new PaymentItem(params[0], params[1], Integer.parseInt(params[2]),
                    Integer.parseInt(params[3]));
            item.setImageUrl(params[4]);
            item.setDescription(params[5]);
            itemList.add(item);
        }

        final Payment payment = new Payment(message, itemList);
        payment.setCallbackUrl("");

        payment.setHandler(new Handler() {
            public void handleMessage(Message message) {
                switch (message.what) {
                    case Payment.OPENED:
                        Log.d("Payment", "PaymentDialog opened.");
                        try {
                            Field dialog_field = Payment.class.getDeclaredField("mPaymentDialog");
                            dialog_field.setAccessible(true);
                            if (dialog_field.get(payment) == null) {
                                Log.e(TAG, "null got!");
                            }
                            popupDialog = (PopupDialog) dialog_field.get(payment);
                            is_popup_opened = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case Payment.CANCELLED:
                        Log.d("Payment", "PaymentDialog canceled.");
                        break;
                    case Payment.ABORTED:
                        Log.d("Payment", "PaymentDialog closed.");
                        break;
                }
            }
        });

        Log.d(TAG, "Trying to open payment dialog...");
        payment.request(MainActivity.getInstance(), new PaymentListener() {
            @Override
            public void onSuccess(int responseCode, HeaderIterator headers, String paymentId) {
                Log.d(TAG, "payment.request() succeeded.");
            }

            @Override
            public void onFailure(int responseCode, HeaderIterator headers, String paymentId,
                    String response) {
                Log.d(TAG, "payment.request() failed.");
            }

            @Override
            public void onCancel(int responseCode, HeaderIterator headers, String paymentId) {
                Log.d(TAG, "payment.request() canceled.");
                is_popup_canceled = true;
            }
        });
    }

    public static PopupDialog getPopupDialog() {
        return popupDialog;
    }

    public static void dismissPopupDialog() {
        if (popupDialog == null)
            return;
        popupDialog.dismiss();
    }
}
