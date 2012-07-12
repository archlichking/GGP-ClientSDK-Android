
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import net.gree.asdk.api.GreePlatformSettings;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.GLog.LogLevel;
import net.gree.asdk.core.Injector;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.storage.LocalStorage;
import net.gree.asdk.core.util.CoreData;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class GreePlatformSettingsStepDefinitions extends BasicStepDefinition {

    private final String TAG = "GreePlatformSettings_steps";

    @When("I enable debug logging")
    public void enableLogging() {
        GreePlatformSettings.enableLogging(true);
    }
    
    @And("I make sure debug logging is not enabled")
    public void disableLogging() {
        GreePlatformSettings.enableLogging(false);
    }

    @Then("debug logging should be opened")
    public void verifyLoggingOpen() {
        // check localStorage
        assertEquals("true",
                Injector.getInstance(LocalStorage.class).getString(InternalSettings.EnableLogging));
        // check CoreData
        assertEquals("true", CoreData.get(InternalSettings.EnableLogging));
    }

    @When("I set the logging level to (\\d+)")
    public void setLoggingLevel(int loglevel) {
        GLog.setLevel(loglevel);
    }

    @Then("debug logging level should be (\\w+)")
    public void verifyLoggingLevel(String loglevel) {
        LogLevel expect_level = transLogLevel(loglevel);
        assertEquals(expect_level, GLog.getLevel());
    }

    private LogLevel transLogLevel(String loglevel) {
        if ("ERROR".equals(loglevel)) {
            return LogLevel.Error;
        } else if ("WARN".equals(loglevel)) {
            return LogLevel.Warn;
        } else if ("INFO".equals(loglevel)) {
            return LogLevel.Info;
        } else if ("DEBUG".equals(loglevel)) {
            return LogLevel.Debug;
        } else if ("VERBOSE".equals(loglevel)) {
            return LogLevel.Verbose;
        }
        return null;
    }

    @When("I set the path of log file to (\\w+)")
    public void setLogPath(String filePath) {
        GreePlatformSettings.setWriteToFile(filePath);
    }

    @Then("the debug log file should write to (\\w+)")
    public void verifyLogPath(String filePath) {
        // check localStorage
        assertEquals(filePath,
                Injector.getInstance(LocalStorage.class).getString(InternalSettings.WriteToFile));
        // check CoreData
        assertEquals(filePath, CoreData.get(InternalSettings.WriteToFile));
    }

    @And("I make sure notification is not enabled")
    public void disableNotification() {
        setNotification(false);
    }

    @When("I enable notification")
    public void enableNotification() {
        setNotification(true);
    }

    private void setNotification(boolean isEnable) {
        GreePlatformSettings.setNotificationEnabled(isEnable);
    }

    @And("I make sure local notification is not enabled")
    public void disableLocalNotification() {
        setLocalNotification(false);
    }

    @When("I enable local notification")
    public void enableLocalNotification() {
        setLocalNotification(true);
    }

    private void setLocalNotification(boolean isEnable) {
        GreePlatformSettings.setLocalNotificationEnabled(isEnable);
    }

    @And("I make sure notification at screen bottom is not enabled")
    public void disableNotificationAtScreenBotton() {
        setNotificationAtScreenBotton(false);
    }

    @When("I enable notification at screen bottom")
    public void enableNotificationAtScreenBotton() {
        setNotificationAtScreenBotton(true);
    }

    private void setNotificationAtScreenBotton(boolean isBottom) {
        GreePlatformSettings.setNotificationsAtScreenBottom(isBottom);
    }

    @Then("notification should be enabled")
    public void verifyNotificationEnabled() {
        assertEquals(true, GreePlatformSettings.getNotificationEnabled());
    }

    @Then("local notification should be enabled")
    public void verifyLocalNotificationEnabled() {
        assertEquals(true, GreePlatformSettings.getLocalNotificationEnabled());
    }

    @Then("notification at screen bottom should be enabled")
    public void verifyNotificationAtScreenBottom() {
        assertEquals(true, GreePlatformSettings.isNotificationsAtScreenBottom());
    }
}
