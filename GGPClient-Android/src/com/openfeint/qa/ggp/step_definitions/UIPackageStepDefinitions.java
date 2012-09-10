
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertTrue;

import java.lang.reflect.Field;

import net.gree.asdk.api.ui.StatusBar;
import net.gree.asdk.core.Injector;
import net.gree.asdk.core.notifications.NotificationCounts;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.ggp.MainActivity;

public class UIPackageStepDefinitions extends BasicStepDefinition {

    private final static String TAG = "UIPackage_Step";

    @And("I active default widget")
    public void activateStatusBar() {
        final StatusBar bar = getStatusBar();
        MainActivity.getInstance().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                bar.setVisibility(View.VISIBLE);
            }
        });
    }

    private StatusBar getStatusBar() {
        return (StatusBar) MainActivity.getInstance().findViewById(
                com.openfeint.qa.ggp.R.id.statusBarBottom);
    }

    @When("I set SNS notification to (\\d+) and App notification to (\\d+)")
    public void setNotificationBarForTest(int snsCount, int appCount) {
        try {
            NotificationCounts mCounts = Injector.getInstance(NotificationCounts.class);
            // set notification count
            Field mSnsCount_field = NotificationCounts.class.getDeclaredField("mSnsCount");
            mSnsCount_field.setAccessible(true);
            mSnsCount_field.set(mCounts, snsCount);
            Field mAppCount_field = NotificationCounts.class.getDeclaredField("mAppCount");
            mAppCount_field.setAccessible(true);
            mAppCount_field.set(mCounts, appCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @And("I update the all notification count")
    public void updateNotificationCount() {
        final StatusBar bar = getStatusBar();
        MainActivity.getInstance().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                bar.onUpdate();
            }
        });
    }

    @Then("all notification count badge should be (\\d+)")
    public void verifyNotificationCount(String count) {
        // wait UI thread to update badge
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        StatusBar bar = getStatusBar();
        try {
            Field mBadgeCount_field = StatusBar.class.getDeclaredField("mBadgeCount");
            mBadgeCount_field.setAccessible(true);
            TextView badgeCountView = (TextView) mBadgeCount_field.get(bar);
            assertTrue("badge count", badgeCountView != null
                    && badgeCountView.getText().toString().contains(count));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After("I hide widget")
    public void hiddenStatusBar() {
        MainActivity.getInstance().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                getStatusBar().setVisibility(View.GONE);
            }
        });
    }
}
