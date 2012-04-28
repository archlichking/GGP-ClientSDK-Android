
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.GreeUser;
import net.gree.asdk.api.GreeUser.GreeIgnoredUserListener;
import net.gree.asdk.api.GreeUser.GreeUserListener;

import org.apache.http.HeaderIterator;

import util.Consts;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class People_StepDefinitions extends BasicStepDefinition {
    private static final String TAG = "People_Steps";

    private static List<GreeUser> peopleList = new ArrayList<GreeUser>();

    private static GreeUser me;

    private static GreeUser friend;

    private static String[] ignoreUsers;

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
                assertEquals("server response", Consts.SUCCESS, status);
                break;
            }
        }
    }

    GreeUserListener listener = new GreeUserListener() {
        @Override
        public void onSuccess(int index, int count, GreeUser[] people) {
            Log.d(TAG, "Get people success!");
            peopleList.clear();
            if (people != null) {
                Log.i(TAG, "Get " + people.length + " people");
                Log.i(TAG, "Adding people datas");
                for (int i = 0; i < people.length; i++) {
                    peopleList.add(people[i]);
                }
            }
            status = Consts.SUCCESS;
        }

        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String response) {
            Log.e(TAG, "Get people failed!");
            peopleList.clear();
            status = Consts.FAILED;
        }
    };

    @Given("I logged in with email (.+) and password (\\w+)")
    public void checkLogin(String email, String password) {
        // TODO Login action is now can not be automated
        me = GreePlatform.getLocalUser();
        if (me == null)
            fail("No login yet！");
        Log.i(TAG, "Logined as user: " + me.getNickname());
    }

    @When("I see my info from native cache")
    public void getUserInfoFromCache() {
        // Nothing to do here
    }

    @Then("my (\\w+) should be (.+)")
    public void verifyUserInfo(String column, String value) {
        if ("displayName".equals(column)) {
            assertEquals("userName", value, me.getNickname());
        } else if ("id".equals(column)) {
            assertEquals("userId", value, me.getId());
        } else if ("userGrade".equals(column)) {
            assertEquals("userGrade", value, me.getUserGrade());
        } else if ("region".equals(column)) {
            assertEquals("userRegion", value, me.getRegion());
        } else {
            fail("Unknown column of user info!");
        }
    }

    @When("I see my info from server")
    public void getUserInfoFromServer() {
        status = Consts.UNKNOWN;
        GreeUser.loadUserWithId(1, 1, "@me", listener);
        waitCallback();
        if (peopleList == null)
            fail("No user info got!");
        me = peopleList.get(0);
        Log.i(TAG, "User info get from server: " + me.getNickname());
    }

    private void getFriends(GreeUser owner) {
        status = Consts.UNKNOWN;
        owner.loadFriends(Consts.startIndex_1, Consts.pageSize, listener);
        waitCallback();
        if (peopleList == null)
            fail("No friend got!");
        for (GreeUser person : peopleList) {
            Log.i(TAG, "Friend " + (peopleList.indexOf(person) + 1));
            Log.i(TAG, "ID: " + person.getId());
            Log.i(TAG, "Nickname: " + person.getNickname());
            Log.i(TAG, "UserGrade: " + person.getUserGrade());
        }
    }

    @When("I check my friend list")
    public void getCurrentUserFriends() {
        GreeUser me = GreePlatform.getLocalUser();
        getFriends(me);
    }

    @Then("friend list should be size of (\\d+)")
    public void verifyFriendNumber(int num) {
        Log.d(TAG, "Checking friend number...");
        assertEquals("friend count", num, peopleList.size());
    }

    @Then("friend list should have (.+)")
    public void verifyFriendExists(String friendName) {
        friend = null;
        Log.d(TAG, "Check if " + friendName + " is in the friend list");
        boolean isExists = false;
        for (GreeUser user : peopleList) {
            if (friendName.equals(user.getNickname())) {
                Log.d(TAG, "Got the user");
                isExists = true;
                friend = user;
            }
        }
        assertTrue("user in friend list", isExists);
    }

    @Then("userid of (.+) should be (\\w+) and grade should be (\\w+)")
    public void verifyFriendInfo(String name, String id, String grade) {
        if (friend == null)
            fail("Don't get the friend in the list");
        Log.d(TAG, "Checking friend info...");
        assertEquals("userId", id, friend.getId());
        assertEquals("userGrade", grade, friend.getUserGrade());
    }

    // This step is not using now, seems the sdk is not allow to get another
    // user instance except the current login user
    @When("I check friend list of user (\\w+)")
    public void getSpecificUserFriends(String userId) {
        status = Consts.UNKNOWN;
        GreeUser.loadUserWithId(1, 1, userId, listener);
        waitCallback();
        if (peopleList == null)
            fail("Don't get the friend in the list");
        Log.d(TAG, "specific user is: " + peopleList.get(0).getNickname());
        getFriends(peopleList.get(0));
    }

    private GreeIgnoredUserListener ignoredUserListener = new GreeIgnoredUserListener() {
        @Override
        public void onSuccess(int index, int count, String[] list) {
            Log.d(TAG, "Get ignore list success!");
            Log.i(TAG, "Index: " + index);
            Log.i(TAG, "TotalListSize: " + count);
            if (list != null) {
                ignoreUsers = list;
                for (int i = 0; i < list.length; i++) {
                    Log.i(TAG, "Block user " + (i + 1) + ": " + list[i]);
                }
            }
            status = Consts.SUCCESS;
        }

        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String response) {
            Log.e(TAG, "Get ignore list failed!");
            ignoreUsers = null;
            status = Consts.FAILED;
        }
    };

    @Given("I make sure user (\\w+) is in my ignore list")
    public void checkUserInIgnoreListAsCondition(String userId) {
        Log.i(TAG, "Expect user " + userId + " is in my ignore list...");
        status = Consts.UNKNOWN;
        me.isIgnoringUserWithId(userId, ignoredUserListener);
        waitCallback();
        if (ignoreUsers == null)
            fail("Did not get the ignore list!");
        if (ignoreUsers.length == 0 || !userId.equals(ignoreUsers[0]))
            fail("User " + userId + " is not in the ignore list, please add it from sb.gree.net");
    }

    @When("I get my ignore list")
    public void getMyIgnoreList() {
        status = Consts.UNKNOWN;
        Log.e(TAG, "me is " + me.getNickname());
        me = GreePlatform.getLocalUser();
        me.loadIgnoredUserIds(Consts.startIndex_1, Consts.pageSize, ignoredUserListener);
        waitCallback();
    }

    @Then("there should be (\\d+) user in my ignore list")
    public void verifyIgnoreUserCount(String count) {
        if (ignoreUsers == null)
            fail("Do not have user in the ignore list!");
        assertEquals("ingore user count", count, ignoreUsers.length);
    }

    @And("user (\\w+) should be in my ignore list")
    public void checkUserInIgnoreList(String userId) {
        boolean isExists = false;
        if (ignoreUsers == null)
            fail("Did not get the ignore list!");
        for (String id : ignoreUsers) {
            if (userId.equals(id)) {
                Log.i(TAG, "Find user: " + userId + " in the list");
                isExists = true;
            }
        }
        assertTrue("user in ignore list", isExists);
    }
}
