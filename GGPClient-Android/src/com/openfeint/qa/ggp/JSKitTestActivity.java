
package com.openfeint.qa.ggp;

import net.gree.asdk.core.ui.GreeWebView;
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
}
