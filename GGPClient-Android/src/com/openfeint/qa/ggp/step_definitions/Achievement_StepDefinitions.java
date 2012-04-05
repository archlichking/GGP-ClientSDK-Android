
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

import net.gree.asdk.api.Achievement;
import net.gree.asdk.api.Achievement.AchievementListUpdateListener;
import net.gree.asdk.core.request.OnResponseCallback;

import org.apache.http.HeaderIterator;

import util.Consts;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Achievement_StepDefinitions extends BasicStepDefinition {
    private static final String TAG = "Achievement_Steps";

    private static List<Achievement> achievementList;

    private List<String> achievementNames;

    private static Achievement testAchi;

    private static int expectLockStatus;

    private String status;

    // waiting for the async callback and assert it success
    private void waitCallback() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (status != Consts.UNKNOWN) {
                assertEquals(Consts.SUCCESS, status);
                break;
            }
        }
    }

    AchievementListUpdateListener achiListener = new AchievementListUpdateListener() {

        @Override
        public void onSuccess(int index, int totalListSize, Achievement[] requestedElements) {
            Log.d(TAG, "Get achievement list success!");
            if (requestedElements != null) {
                Log.i(TAG, "Adding achievement datas");
                achievementList = new ArrayList<Achievement>();
                for (int i = 0; i < requestedElements.length; i++) {
                    achievementList.add(requestedElements[i]);
                }
            }
            status = Consts.SUCCESS;
        }

        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String response) {
            Log.e(TAG, "Get achievement list failed!");
            status = Consts.FAILED;
        }
    };

    @When("I try to load out all achievements for current user")
    public void getAchievements() {
        status = Consts.UNKNOWN;
        Achievement.fetch(achiListener);
        waitCallback();
    }

    @Then("all achievements I have should be return")
    public void verifyAchievements() {
        achievementNames = new ArrayList<String>();
        achievementNames.add("Achievement01");
        achievementNames.add("Achievement02");
        achievementNames.add("Achievement03");
        achievementNames.add("Achievement04");
        achievementNames.add("Achievement05");

        // Verify
        if (achievementList == null)
            fail();
        Log.i(TAG, "Verifing the achievementNames");
        assertEquals(achievementNames.size(), achievementList.size());
        for (Achievement achi : achievementList) {
            assertTrue(achievementNames.contains(achi.getName()));
        }
    }

    @Given("I get the achievement list success")
    public void getAchievementsAtFirst() {
        getAchievements();
    }

    @Then("I can get all infos of achievement (\\w+)")
    public void getAchievementInfos(String achiName) {
        if (achievementList == null)
            fail();
        Log.i(TAG, "Verifing the achievement infos");
        for (Achievement achi : achievementList) {
            if (achiName.equals(achi.getName())) {
                Log.d(TAG, "Name: " + achi.getName());
                Log.d(TAG, "Id: " + achi.getId());
                Log.d(TAG, "Description: " + achi.getDescription());
                Log.d(TAG, "Score: " + achi.getScore());
                Log.d(TAG, "isSecret: " + achi.isSecret());
                Log.d(TAG, "isUnlocked: " + achi.isUnlocked());
                assertEquals(true, achi.isSecret());
                assertEquals(10, achi.getScore());
                assertEquals("Achievement 01", achi.getDescription());
                assertEquals(true, achi.isSecret());
            }
        }
    }

    @When("I get the lock status of achievement (\\w+)")
    public void getLockStatus(String achiName) {
        if (achievementList == null)
            fail();
        Log.i(TAG, "Get the achievement you want to verify");
        for (Achievement achi : achievementList) {
            if (achiName.equals(achi.getName())) {
                testAchi = achi;
                Log.d(TAG, "Now lock status is :" + testAchi.isUnlocked());
            }
        }
    }

    private OnResponseCallback<Void> lockListener = new OnResponseCallback<Void>() {

        @Override
        public void onSuccess(int responseCode, HeaderIterator headers, Void response) {
            Log.d(TAG, "Change lock status success");
            status = Consts.SUCCESS;
        }

        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String response) {
            Log.e(TAG, "Change lock status failed");
            status = Consts.FAILED;
        }

    };

    @And("I update its status")
    public void updateLockStatus() {
        if (testAchi == null)
            fail();
        status = Consts.UNKNOWN;
        if (testAchi.isUnlocked() == Achievement.LOCKED) {
            Log.i(TAG, "Unlocking the achievement");
            testAchi.unlock(null, lockListener);
            expectLockStatus = Achievement.UNLOCKED;
        } else if (testAchi.isUnlocked() == Achievement.UNLOCKED) {
            Log.i(TAG, "Locking the achievement");
            testAchi.lock(lockListener);
            expectLockStatus = Achievement.LOCKED;
        } else {
            Log.e(TAG, "Unknow lock status of the achievement: " + testAchi.getName());
            fail();
        }
        waitCallback();
    }

    @Then("its status should be updated")
    public void verifyLockStatus() {
        getLockStatus(testAchi.getName());
        // verify
        assertEquals(expectLockStatus, testAchi.isUnlocked());
    }
}