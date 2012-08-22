
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import net.gree.asdk.core.GLog;
import android.os.Environment;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class CorePackageStepDefinitions extends BasicStepDefinition {
    private static final String TAG = "CorePackage_Steps";

    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath();

    @And("I open file debug mode with file (.+)")
    public void openFileDebug(String fileName) {
        GLog.debugFile(SDCARD_PATH + "/" + fileName);
    }

    @When("I add (\\w+) log (.+)")
    public void addLogText(String logLevel, String text) {
        if ("Error".equals(logLevel)) {
            GLog.setLevel(GLog.ERROR);
            GLog.e(TAG, text);
        } else if ("Warn".equals(logLevel)) {
            GLog.setLevel(GLog.WARN);
            GLog.w(TAG, text);
        } else if ("Info".equals(logLevel)) {
            GLog.setLevel(GLog.INFO);
            GLog.i(TAG, text);
        } else if ("Debug".equals(logLevel)) {
            GLog.setLevel(GLog.DEBUG);
            GLog.d(TAG, text);
        } else if ("Verbose".equals(logLevel)) {
            GLog.setLevel(GLog.VERBOSE);
            GLog.v(TAG, text);
        }
    }

    @Then("log file (.+) should have log (.+)")
    public void verifyLogText(String fileName, String expectText) {
        try {
            assertTrue("debug file contains the expect log", fromFile(SDCARD_PATH + "/" + fileName)
                    .toString().contains(expectText));
        } catch (FileNotFoundException e) {
            fail("failed to open the debug file");
        } catch (IOException e) {
            fail("failed to read the debug file");
        }
    }

    private CharSequence fromFile(String filename) throws IOException {
        FileInputStream fis = new FileInputStream(filename);
        FileChannel fc = fis.getChannel();

        // Create a read-only CharBuffer on the file
        ByteBuffer bbuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, (int) fc.size());
        CharBuffer cbuf = Charset.forName("8859_1").newDecoder().decode(bbuf);
        return cbuf;
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
