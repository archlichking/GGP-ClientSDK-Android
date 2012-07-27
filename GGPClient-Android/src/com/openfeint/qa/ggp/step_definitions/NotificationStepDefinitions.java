
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import net.gree.asdk.api.alarm.ScheduledNotification;
import net.gree.asdk.api.alarm.ScheduledNotificationListener;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class NotificationStepDefinitions extends BasicStepDefinition {
    private static final String TAG = "Notification_Steps";

    private final String NOTIFICATION_PARAMS = "notification_params";

    private final String ADD_TIMESTAMP = "add_timestamp";

    private final String NOTIFY_TIMESTAMP = "notifyTimestamp";

    private final String CALLBACK_PARAM = "callbackParam";

    private final String IS_NOTIFICATION_ADDED = "isNotificationAdded";

    private final String LOCAL_NOTIFICATION = "localNotification";

    private final String IS_NOTIFICATION_CALLED = "isNotificationCalled";

    @And("I initialize notification with TITLE (.+), BODY (.+), BAR_TITLE (.+), INTERVAL (\\d+), ID (\\d+) and CALLBACK_PARAM (.+)")
    public void setupNotification(String title, String body, String barTitle, int interval, int id,
            String callbackParam) {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("title", title);
        map.put("message", body);
        map.put("barMessage", barTitle);
        map.put("interval", interval);
        map.put("notifyId", id);
        map.put("callbackParam", callbackParam);

        getBlockRepo().put(NOTIFICATION_PARAMS, map);
    }

    @When("I push local notification")
    public void pushLocalNotificationAsNormal() {
        notifyStepWait();
        pushLocalNotification(new ScheduledNotificationListener() {
            @Override
            public void onNotified(String param) {
                Log.i(TAG, "Notification called..");
                getBlockRepo().put(NOTIFY_TIMESTAMP, System.currentTimeMillis());
                getBlockRepo().put(CALLBACK_PARAM, param);
                notifyStepPass();
            }
        });
    }

    @And("I push local notification at first")
    public void pushLocalNotificationAsCondition() {
        getBlockRepo().put(IS_NOTIFICATION_CALLED, false);
        pushLocalNotification(new ScheduledNotificationListener() {
            @Override
            public void onNotified(String param) {
                Log.e(TAG, "Notification called!!");
                getBlockRepo().put(IS_NOTIFICATION_CALLED, true);
            }
        });
    }

    private void pushLocalNotification(ScheduledNotificationListener listener) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) getBlockRepo().get(NOTIFICATION_PARAMS);
        ScheduledNotification queue = new ScheduledNotification();
        getBlockRepo().put(ADD_TIMESTAMP, System.currentTimeMillis());
        getBlockRepo().put(NOTIFY_TIMESTAMP, 0);
        // add notification into the queue
        boolean res = queue.set(map, listener);
        getBlockRepo().put(IS_NOTIFICATION_ADDED, res);
        getBlockRepo().put(LOCAL_NOTIFICATION, queue);
    }

    @Then("local notification should be called after interval (\\d+)")
    public void verifyNotificationInterval(int expected_interval) {
        if (!(Boolean) getBlockRepo().get(IS_NOTIFICATION_ADDED)) {
            fail("add notification failed!");
        }
        long addTimestamp = (Long) getBlockRepo().get(ADD_TIMESTAMP);
        long notifyTimestamp = (Long) getBlockRepo().get(NOTIFY_TIMESTAMP);
        assertEquals("notification interval", expected_interval,
                (notifyTimestamp - addTimestamp) / 1000);
    }

    @And("local notification should called with CALLBACK_PARAM (.+)")
    public void verifyCallbackParam(String expected_callback) {
        if (!(Boolean) getBlockRepo().get(IS_NOTIFICATION_ADDED)) {
            fail("add notification failed!");
        }
        assertEquals("callback param", expected_callback,
                (String) getBlockRepo().get(CALLBACK_PARAM));
    }

    @When("I cancel local notification")
    public void cancelLocalNotification() {
        ((ScheduledNotification) getBlockRepo().get(LOCAL_NOTIFICATION)).cancel();
    }

    @Then("local notification should not be called after interval (\\d+)")
    public void verifyNotificationNotBeCalled(int interval) {
        if (!(Boolean) getBlockRepo().get(IS_NOTIFICATION_ADDED)) {
            fail("add notification failed!");
        }
        try {
            Thread.sleep((interval + 5) * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue("Notification not be called",
                !(Boolean) getBlockRepo().get(IS_NOTIFICATION_CALLED));
    }
}
