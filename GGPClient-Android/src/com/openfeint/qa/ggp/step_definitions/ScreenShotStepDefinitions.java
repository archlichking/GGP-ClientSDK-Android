package com.openfeint.qa.ggp.step_definitions;

import java.io.File;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.Assert;

import net.gree.asdk.api.ScreenShot;
import util.ImageUtil;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.ggp.MainActivity;

public class ScreenShotStepDefinitions extends BasicStepDefinition {
    
    public static final String BITMAP1 = "bitmap1";
    public static final String BITMAP2 = "bitmap2";
    private static final String TAG = ScreenShotStepDefinitions.class.getSimpleName();
    
    @When("I take screenshot")
    public void takeScreenShot(){
        MainActivity activity = MainActivity.getInstance();
        Bitmap bitmap1 = ScreenShot.capture(activity.getWindow().getCurrentFocus());
        getBlockRepo().put(BITMAP1, bitmap1);
        saveScreenShotAsExpectedResult(Environment.getExternalStorageDirectory().getAbsolutePath(), BITMAP1 + ".png", bitmap1);        
        Bitmap bitmap2 = ScreenShot.capture(activity.getWindow().getCurrentFocus());
        getBlockRepo().put(BITMAP2, bitmap2);
        saveScreenShotAsExpectedResult(Environment.getExternalStorageDirectory().getAbsolutePath(), BITMAP2 + ".png", bitmap2);
    }
    @Then("the percenetage similarity of the image comparison should be greater then (\\d+) %")
    public void compareScreenshot(int percentage){
        double rate = ImageUtil.compareImage((Bitmap) getBlockRepo().get(BITMAP1), (Bitmap) getBlockRepo().get(BITMAP2));
        Assert.assertTrue("the percenetage similarity of the image comparison should be greater then" + String.valueOf(percentage) + "%", rate > (double) percentage);
    }
    
    private void saveScreenShotAsExpectedResult(String path, String icon_name, Bitmap bitmap) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            try {
                File icon = new File(path, icon_name);
                FileOutputStream fos = new FileOutputStream(icon);
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
                    Log.d(TAG, "Create expected icon for leaderboard success!");
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }   
        }
        else {
            fail("readable and writable external storage is not there!");
        }
    }
}
