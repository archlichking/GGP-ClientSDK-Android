
package com.openfeint.qa.ggp;

import net.gree.asdk.api.Achievement;
import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.GreeUser;
import net.gree.asdk.api.Leaderboard;
import net.gree.asdk.api.ModeratedText;
import net.gree.asdk.core.Injector;
import net.gree.asdk.core.track.Tracker;
import android.app.Application;

public class GGPClientAutomationApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        GreePlatform.initialize(getApplicationContext(), R.xml.gree_platform_configuration, null);
        Injector.getInstance(Tracker.class).setMaxRetryCount(0);
        openDebugAndVerboseMode();
    }

    private void openDebugAndVerboseMode() {
        GreePlatform.setDebug(true);
        GreePlatform.setVerbose(true);
        ModeratedText.setDebug(true);
        ModeratedText.setVerbose(true);
        Achievement.setDebug(true);
        Achievement.setVerbose(true);
        Leaderboard.setDebug(true);
        Leaderboard.setVerbose(true);
        GreeUser.setDebug(true);
        GreeUser.setVerbose(true);
    }
}
