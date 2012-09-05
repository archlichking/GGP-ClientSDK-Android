
package com.openfeint.qa.ggp;

import net.gree.asdk.core.ui.GreeWebView;
import net.gree.asdk.core.ui.WebViewPopupDialog;
import util.PopupUtil;
import android.app.Activity;
import android.os.Bundle;

public class JSKitTestActivity extends Activity {
    
    private static JSKitTestActivity activity;
    
    public static final String JSKIT_BASE_PAGE = "file:///android_asset/jslib/test.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.greewebview);
        GreeWebView webview = (GreeWebView) findViewById(R.id.greewebview);
        webview.setUp();
        webview.loadUrl(JSKIT_BASE_PAGE);

        activity = JSKitTestActivity.this;
    }
    
    public static JSKitTestActivity getInstance() {
        return activity;
    }

    
    public void launchDialog() {
        WebViewPopupDialog dialog = new WebViewPopupDialog(this) {

            @Override
            protected int getOpenedEvent() {
                return 0;
            }

            @Override
            protected String getEndPoint() {
                return null;
            }

            @Override
            protected int getClosedEvent() {
                return 0;
            }

            @Override
            protected void createWebViewClient() {
                PopupDialogWebViewClient webViewClient = new PopupDialogWebViewClient(getContext()) {
                    @Override
                    protected void onDialogClose(String url) {
                    }
                };
                setWebViewClient(webViewClient);
            }
        };
        GreeWebView webview;
        try {
            dialog.show();
            webview = (GreeWebView) PopupUtil.getWebViewFromPopup(dialog);
            webview.setUp();
            webview.loadUrl("file:///android_asset/jslib/test.html");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
