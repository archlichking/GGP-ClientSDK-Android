
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Assert;
import net.gree.asdk.api.Achievement;
import net.gree.asdk.api.Achievement.AchievementChangeListener;
import net.gree.asdk.api.Achievement.AchievementListUpdateListener;
import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.IconDownloadListener;
import net.gree.asdk.core.InternalSettings;
import net.gree.asdk.core.util.CoreData;

import org.apache.http.HeaderIterator;

import util.Consts;
import util.ImageUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class AchievementStepDefinitions extends BasicStepDefinition {
    private static final String TAG = "Achievement_Steps";

    private static final String ACHIEVEMENT_LIST = "achievement_list";

    private static final String ICON = "achievement_icon";

    private static final String ACHIEVEMENT = "achievement";

    private boolean transLockStatus(String statusMark) {
        if ("LOCK".equals(statusMark))
            return false;
        else if ("UNLOCK".equals(statusMark))
            return true;
        else {
            fail("Got invalid statusMark!");
            return false;
        }
    }

    private AchievementChangeListener lockListener = new AchievementChangeListener() {

        @Override
        public void onSuccess() {
            Log.d(TAG, "Change lock status success");
            notifyStepPass();
        }

        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String response) {
            Log.e(TAG, "Change lock status failed! " + responseCode + " || " + response);

            notifyStepPass();
        }

    };

    @When("I load list of achievement")
    @Given("I load list of achievement")
    @And("I load list of achievement")
    public void getAllAchievements() {
        // make sdk to EnablePerformanceLogging
        CoreData.put(InternalSettings.EnablePerformanceLogging, "true");
        getAchievements(Consts.STARTINDEX_1, Consts.PAGESIZE_ALL);
    }

    private void getAchievements(int startIndex, int pageSize) {
        notifyStepWait();
        getBlockRepo().put(ACHIEVEMENT_LIST, new ArrayList<Achievement>());
        Achievement.loadAchievements(startIndex, pageSize, new AchievementListUpdateListener() {
            @Override
            public void onSuccess(int index, int totalListSize, Achievement[] requestedElements) {
                Log.d(TAG, "Get achievement list success!");
                if (requestedElements != null) {
                    Log.i(TAG, "Adding achievement datas");
                    ((ArrayList<Achievement>) getBlockRepo().get(ACHIEVEMENT_LIST)).addAll(Arrays
                            .asList(requestedElements));
                }
                notifyStepPass();
            }

            @Override
            public void onFailure(int responseCode, HeaderIterator headers, String response) {
                Log.e(TAG, "Get achievement list failed!");
                notifyStepPass();
            }
        });
    }

    @Then("I should have total achievements (\\d+)")
    public void verifyAchievementCount(int size) {
        ArrayList<Achievement> a = (ArrayList<Achievement>) getBlockRepo().get(ACHIEVEMENT_LIST);
        if (a == null)
            fail("No achievement found!");

        Log.i(TAG, "Verifing the achievement count...");
        assertEquals("achievement count", size, a.size());
    }

    @Then("I should have achievement of name (.+) with status (\\w+) and score (\\d+)")
    public void verifyAchievementInList(String achiName, String statusMark, int score) {
        ArrayList<Achievement> a = (ArrayList<Achievement>) getBlockRepo().get(ACHIEVEMENT_LIST);
        if (a == null)
            fail("No achievement in the list!");

        for (Achievement achi : a) {
            if (achiName.equals(achi.getName())) {
                Log.d(TAG, "Found the achievement " + achiName);
                assertEquals("lock status of achievement " + achiName, transLockStatus(statusMark),
                        achi.isUnlocked());
                assertEquals("score of achievement " + achiName, score, achi.getScore());
                return;
            }
        }
        fail("Could not find the achievement named " + achiName);
    }

    @Given("I make sure status of achievement (.+) is (\\w+)")
    @After("I make sure status of achievement (.+) is (\\w+)")
    @And("I make sure status of achievement (.+) is (\\w+)")
    @When("I update status of achievement (.+) to (\\w+)")
    public void updateLockStatusByName(String achiName, String statusMark) {
        ArrayList<Achievement> a = (ArrayList<Achievement>) getBlockRepo().get(ACHIEVEMENT_LIST);
        if (a == null)
            fail("No achievement in the list!");

        for (Achievement achi : a) {
            if (achiName.equals(achi.getName())) {
                notifyStepWait();
                Log.d(TAG, "Found the achievement " + achiName);

                boolean isUnlocked = transLockStatus(statusMark);
                if (isUnlocked == true) {
                    Log.i(TAG, "Unlocking the achievement " + achi.getName());
                    achi.unlock(lockListener);
                } else if (isUnlocked == false) {
                    Log.i(TAG, "Locking the achievement " + achi.getName());
                    achi.lock(lockListener);
                }
                return;
            }
        }
    }

    @Then("status of achievement (.+) should be (\\w+)")
    public void verifyLockStatusByName(String achiName, String statusMark) {
        ArrayList<Achievement> a = (ArrayList<Achievement>) getBlockRepo().get(ACHIEVEMENT_LIST);
        if (a == null)
            fail("No achievement in the list!");

        for (Achievement achi : a) {
            if (achiName.equals(achi.getName())) {
                Log.d(TAG, "Found the achievement " + achiName);
                Log.i(TAG, "Verifying the lock status of achievement " + achiName);
                assertEquals("lock status of achievement " + achiName, transLockStatus(statusMark),
                        achi.isUnlocked());
                return;
            }
        }
        fail("Could not find the achievement named " + achiName);
    }

    @Then("my score should be (\\w+) by (\\d+)")
    public void verifyScoreUpdated(String operator, int changeValue) {
        // TODO So far, sdk is not support to get the total score of user, will
        // add this verification after sdk to support it
    }

    @When("I load the first page of achievement list with page size (\\d+)")
    public void getFirstPageAchievements(String pageSize) {
        getAchievements(Consts.STARTINDEX_1, Integer.valueOf(pageSize));
    }

    @When("I load icon of achievement (.+)")
    public void loadIcon(String achiName) {
        ArrayList<Achievement> a = (ArrayList<Achievement>) getBlockRepo().get(ACHIEVEMENT_LIST);
        if (a == null)
            fail("No achievement in the list!");

        for (Achievement achi : a) {
            if (achiName.equals(achi.getName())) {
                notifyStepWait();
                getBlockRepo().remove(ICON);
                achi.loadThumbnail(new IconDownloadListener() {
                    @Override
                    public void onSuccess(Bitmap image) {
                        Log.d(TAG, "load icon success!");
                        getBlockRepo().put(ICON, image);
                        notifyStepPass();
                    }

                    @Override
                    public void onFailure(int responseCode, HeaderIterator headers, String response) {
                        Log.e(TAG, "load icon failed!");
                        notifyStepPass();
                    }
                });
                return;
            }
        }
    }
    
    @When("I get icon of achievement (.+)")
    public void getIcon(String achiName) {
        ArrayList<Achievement> a = (ArrayList<Achievement>) getBlockRepo().get(ACHIEVEMENT_LIST);    
        
        if (a == null)
            fail("No achievement in the list!");
        
        for (final Achievement achi : a) {
            if (achiName.equals(achi.getName())) {
                notifyStepWait();
                getBlockRepo().remove(ICON);
                achi.loadThumbnail(new IconDownloadListener() {
                    @Override
                    public void onSuccess(Bitmap image) {
                        Log.d(TAG, "load icon success!");
                        if (achi.getIcon() == null){
                        	fail("get icon failed!");
                        }
                        // Loads achi.getIcon() to ICON
                        getBlockRepo().put(ICON, achi.getIcon());
                        Log.d(TAG, "get icon success!");
                        notifyStepPass(); 
                    }

                    @Override
                    public void onFailure(int responseCode, HeaderIterator headers, String response) {
                        Log.e(TAG, "load icon failed!");
                        notifyStepPass();
                    }
                });           
                return;
            }
        }
        // If loop for achiName does not match achi.getName() list, fail case
        fail("Achievement list not matching!");
    }   

    @Then("achievement icon of (.+) should be (.+)")
    public void verifyIcon(String achiName, String type) {
        if (getBlockRepo().get(ICON) == null)
            fail("achievement icon is null!");

        int icon_id = -100;
        if ("locked icon".equals(type)) {
            icon_id = GreePlatform.getResource(GreePlatform.getContext(),
                    "drawable/achievement_locked_icon");
        } else if ("unlocked icon".equals(type)) {
            icon_id = GreePlatform.getResource(GreePlatform.getContext(),
                    "drawable/achievement_unlocked_icon");
        }

        Bitmap bitmap = (Bitmap) getBlockRepo().get(ICON);
        Bitmap expect_image = ImageUtil.zoomBitmap(
                BitmapFactory.decodeResource(GreePlatform.getContext().getResources(), icon_id),
                bitmap.getWidth(), bitmap.getHeight());
        double sRate = ImageUtil.compareImage(bitmap, expect_image);
        Log.d(TAG, "Similarity rate: " + sRate);
        Assert.assertTrue("achievement icon similarity is bigger than 80%", sRate > 80);
    }

    // TODO for data preparation
    private void saveIconAsExpectedResult(String path, String icon_name) {
        try {
            Bitmap bitmap = (Bitmap) getBlockRepo().get(ICON);
            File icon = new File(path, icon_name);
            FileOutputStream fos = new FileOutputStream(icon);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
                Log.d(TAG, "Create expected icon for achievement success!");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @When("I check basic info of achievement (.+)")
    public void getAchievementFromList(String achiName) {
        ArrayList<Achievement> list = (ArrayList<Achievement>) getBlockRepo().get(ACHIEVEMENT_LIST);
        if (list == null)
            fail("No achievement in the list!");

        for (Achievement achi : list) {
            if (achiName.equals(achi.getName())) {
                Log.d(TAG, "Found the achievement " + achiName);
                getBlockRepo().put(ACHIEVEMENT, achi);
                return;
            }
        }
        fail("Could not find the achievement named " + achiName);
    }

    @Then("info (\\w+) of achievement (.+) should be (.+)")
    @And("info (\\w+) of achievement (.+) should be (.+)")
    public void verifyAchievementInfo(String column, String achiName, String expectValue) {
        Achievement achi = (Achievement) getBlockRepo().get(ACHIEVEMENT);
        if (achi == null)
            fail("No achievement to be verified!");
        String actualValue = "";
        if ("identifier".equals(column)) {
            actualValue = achi.getId();
        } else if ("name".equals(column)) {
            actualValue = achi.getName();
        } else if ("descriptionText".equals(column)) {
            actualValue = achi.getDescription();
        } else if ("isSecret".equals(column)) {
            if (achi.isSecret()) {
                actualValue = "YES";
            } else {
                actualValue = "NO";
            }
        } else if ("score".equals(column)) {
            actualValue = String.valueOf(achi.getScore());
        } else if ("isUnlocked".equals(column)) {
            if (achi.isUnlocked()) {
                actualValue = "YES";
            } else {
                actualValue = "NO";
            }
        }
        assertEquals("achi info " + column, expectValue, actualValue);
    }

}
