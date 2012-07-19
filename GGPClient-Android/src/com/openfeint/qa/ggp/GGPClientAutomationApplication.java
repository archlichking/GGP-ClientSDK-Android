
package com.openfeint.qa.ggp;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.core.Injector;
import net.gree.asdk.core.track.Tracker;

import android.app.Application;

public class GGPClientAutomationApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        GreePlatform.initialize(getApplicationContext(), R.xml.gree_platform_configuration, null);
        Injector.getInstance(Tracker.class).setMaxRetryCount(0);
    }
}
