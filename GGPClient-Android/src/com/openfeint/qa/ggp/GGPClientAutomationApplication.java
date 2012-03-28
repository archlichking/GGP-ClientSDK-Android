
package com.openfeint.qa.ggp;

import net.gree.asdk.api.GreePlatform;

import android.app.Application;

public class GGPClientAutomationApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        GreePlatform.initialize(getApplicationContext(), R.xml.gree_platform_configuration, null);
    }
}
