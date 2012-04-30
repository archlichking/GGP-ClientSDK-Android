
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.GreeUser;
import net.gree.asdk.api.GreeUser.GreeUserListener;

import org.apache.http.HeaderIterator;

import util.Consts;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class People_StepDefinitions extends BasicStepDefinition {
    private static final String TAG = "People_Steps";

    private final static String PEOPLE_LIST = "friendList";

    private final static String MYSELF = "me";

    private final static String FRIEND = "friend";

    @Given("I logged in with email (.+) and password (\\w+)")
    public void checkLogin(String email, String password) {
        // nothing to do in this method
        notifyNext();
    }

    @When("I see my info from native cache")
    public void getUserInfoFromCache() {
        if (GreePlatform.getLocalUser() == null) {
            fail("No login yet！");
        } else {
            getBlockRepo().put(MYSELF, GreePlatform.getLocalUser());
            Log.i(TAG, "Logined as user: " + GreePlatform.getLocalUser().getNickname());
        }
        notifyNext();
    }

    @Then("my (\\w+) should be (.+)")
    public void verifyUserInfo(String column, String value) {
        GreeUser me = (GreeUser) getBlockRepo().get(MYSELF);

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
        notifyNext();
    }

    @When("I see my info from server")
    public void getUserInfoFromServer() {
        GreeUser.loadUserWithId(1, 1, "@me", new GreeUserListener() {
            @Override
            public void onSuccess(int index, int count, GreeUser[] people) {
                Log.d(TAG, "Get people success!");
                getBlockRepo().put(PEOPLE_LIST, new ArrayList<GreeUser>());
                if (people != null) {
                    Log.i(TAG, "Get " + people.length + " people");
                    Log.i(TAG, "Adding people datas");
                    getBlockRepo().put(MYSELF, people[0]);
                    Log.i(TAG, "User info get from server: " + people[0]);
                }
                notifyNext();
            }

            @Override
            public void onFailure(int responseCode, HeaderIterator headers, String response) {
                Log.e(TAG, "Get people failed!");
                notifyNext();
            }
        });
    }

    @When("I check my friend list")
    public void getCurrentUserFriends() {
        GreeUser me = GreePlatform.getLocalUser();
        me.loadFriends(Consts.startIndex_1, Consts.pageSize, new GreeUserListener() {
            @Override
            public void onSuccess(int index, int count, GreeUser[] people) {
                Log.d(TAG, "Get people success!");
                getBlockRepo().put(PEOPLE_LIST, new ArrayList<GreeUser>());
                if (people != null) {
                    Log.i(TAG, "Get " + people.length + " people");
                    Log.i(TAG, "Adding people datas");
                    ((ArrayList<GreeUser>) getBlockRepo().get(PEOPLE_LIST)).addAll(Arrays
                            .asList(people));
                }
                notifyNext();
            }

            @Override
            public void onFailure(int responseCode, HeaderIterator headers, String response) {
                getBlockRepo().put(PEOPLE_LIST, new ArrayList<GreeUser>());
                Log.e(TAG, "Get people failed!");
                notifyNext();
            }
        });
    }

    @Then("friend list should be size of (\\d+)")
    public void verifyFriendNumber(int num) {
        Log.d(TAG, "Checking friend number...");
        ArrayList<GreeUser> l = (ArrayList<GreeUser>) getBlockRepo().get(PEOPLE_LIST);
        assertEquals("friend count", num, l.size());
        notifyNext();
    }

    @Then("friend list should have (.+)")
    public void verifyFriendExists(String friendName) {
        ArrayList<GreeUser> l = (ArrayList<GreeUser>) getBlockRepo().get(PEOPLE_LIST);
        Log.d(TAG, "Check if " + friendName + " is in the friend list");

        for (GreeUser user : l) {
            if (friendName.equals(user.getNickname())) {
                Log.d(TAG, "Got the user");
                assertTrue("user in friend list", true);
                getBlockRepo().put(FRIEND, user);
                notifyNext();
                return;
            }
        }
        assertTrue("user " + friendName + " in friend list", false);
        notifyNext();
    }

    @Then("userid of (.+) should be (\\w+) and grade should be (\\w+)")
    public void verifyFriendInfo(String name, String id, String grade) {
        GreeUser friend = (GreeUser) getBlockRepo().get(FRIEND);

        if (friend == null)
            fail("Don't get the friend " + name + " in the list");

        Log.d(TAG, "Checking friend info...");
        assertEquals("userId", id, friend.getId());
        assertEquals("userGrade", grade, friend.getUserGrade());
        notifyNext();
    }

    // This step is not using now, seems the sdk is not allow to get another
    // user instance except the current login user
    @When("I check friend list of user (\\w+)")
    public void getSpecificUserFriends(String userId) {
        GreeUser.loadUserWithId(1, 1, userId, new GreeUserListener() {
            @Override
            public void onSuccess(int index, int count, GreeUser[] people) {
                Log.d(TAG, "Get people success!");
                getBlockRepo().put(PEOPLE_LIST, new ArrayList<GreeUser>());
                if (people != null) {
                    Log.i(TAG, "Get " + people.length + " people");
                    Log.i(TAG, "Adding people datas");
                    ((ArrayList<GreeUser>) getBlockRepo().get(PEOPLE_LIST)).addAll(Arrays
                            .asList(people));
                }
                notifyNext();
            }

            @Override
            public void onFailure(int responseCode, HeaderIterator headers, String response) {
                getBlockRepo().put(PEOPLE_LIST, new ArrayList<GreeUser>());
                Log.e(TAG, "Get people failed!");
                notifyNext();
            }
        });
    }
}
