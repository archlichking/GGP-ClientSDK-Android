
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.core.GLog;
import net.gree.asdk.core.GreeContactList;
import android.os.Environment;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class CorePackageStepDefinitions extends BasicStepDefinition {
    private static final String TAG = "CorePackage_Steps";

    private static final String LOG_FILE_NAME = "log_file_name";

    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath();

    @And("I open file debug mode with file (.+)")
    public void openFileDebug(String fileName) {
        getBlockRepo().put(LOG_FILE_NAME, fileName);
        GLog.debugFile(SDCARD_PATH + "/" + fileName);
    }

    @When("I add (\\w+) log (.+)")
    public void addLogText(String logLevel, String text) {
        GLog.i(TAG, text);
    }

    @Then("log file (.+) should have log (.+)")
    public void verifyLogText(String fileName, String expectText) {
        try {
            FileReader reader = new FileReader(SDCARD_PATH + "/"
                    + getBlockRepo().get(LOG_FILE_NAME));
            StringBuffer buffer = new StringBuffer();
            int tmp;
            while ((tmp = reader.read()) != -1) {
                buffer.append((char) tmp);
            }
            Log.e(TAG, "Log text: " + buffer.toString());
            assertTrue(buffer.toString().contains(expectText));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After("I close file debug mode and remove file (.+)")
    public void closeFileDebug(String fileName) {
        GLog.closeFile();
        File logFile = new File(SDCARD_PATH + "/" + fileName);
        if (logFile != null && logFile.exists()) {
            logFile.delete();
            if (logFile.exists())
                Log.e(TAG, "delete log file failed!");
            else
                Log.i(TAG, "delete log file success!");
        } else {
            Log.d(TAG, "log file is not exists!");
        }
    }
}
