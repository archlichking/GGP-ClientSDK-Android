
package com.openfeint.qa.ggp;

import net.gree.asdk.core.ui.GreeWebView;
import net.gree.asdk.core.ui.WebViewPopupDialog;
import android.content.Context;

public class JSKitTestDialog extends WebViewPopupDialog {
    
    private static JSKitTestDialog dialog;

    public JSKitTestDialog(Context context) {
        super(context);
        dialog = null;
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

    @Override
    protected int getOpenedEvent() {
        return 0;
    }

    @Override
    protected int getClosedEvent() {
        return 0;
    }

    @Override
    protected String getEndPoint() {
        return null;
    }

    @Override
    public void show() {
        super.show();
        dialog = JSKitTestDialog.this;
        GreeWebView webview = super.getWebView();
        webview.setUp();
        webview.loadUrl(JSKitTestActivity.JSKIT_BASE_PAGE);
    }

    public GreeWebView getWebView() {
        return super.getWebView();
    }
    
    public static JSKitTestDialog getInstance() {
        return dialog;
    }
}
