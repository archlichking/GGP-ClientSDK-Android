
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.GreeUser;
import net.gree.asdk.api.GreeUser.GreeUserListener;

import org.apache.http.HeaderIterator;

import util.Consts;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class People_StepDefinitions extends BasicStepDefinition {
    private static final String TAG = "People_Steps";

    private final static String PEOPLE_LIST = "friendList";

    private final static String MYSELF = "me";

    private final static String FRIEND = "friend";

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
            getBlockRepo().put(PEOPLE_LIST, new ArrayList<GreeUser>());
            if (people != null) {
                Log.i(TAG, "Get " + people.length + " people");
                Log.i(TAG, "Adding people datas");
                ((ArrayList<GreeUser>)getBlockRepo().get(PEOPLE_LIST)).addAll(Arrays.asList(people));
            }
            status = Consts.SUCCESS;
        }

        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String response) {
            getBlockRepo().put(PEOPLE_LIST, new ArrayList<GreeUser>());
            Log.e(TAG, "Get people failed!");
            status = Consts.FAILED;
        }
    };

    @Given("I logged in with email (.+) and password (\\w+)")
    public void checkLogin(String email, String password) {
        // nothing to do in this method
    }

    @When("I see my info from native cache")
    public void getUserInfoFromCache() {
        if (GreePlatform.getLocalUser() == null) {
            fail("No login yet！");
        } else {
            getBlockRepo().put(MYSELF, GreePlatform.getLocalUser());
            Log.i(TAG, "Logined as user: " + GreePlatform.getLocalUser().getNickname());
        }
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
    }

    @When("I see my info from server")
    public void getUserInfoFromServer() {
        status = Consts.UNKNOWN;
        GreeUser.loadUserWithId(1, 1, "@me", listener);
        waitCallback();
        ArrayList<GreeUser> l = (ArrayList<GreeUser>) getBlockRepo().get(PEOPLE_LIST);
        if (l == null)
            fail("No user info got!");

        getBlockRepo().put(MYSELF, l.get(0));
        Log.i(TAG, "User info get from server: " + l.get(0).getNickname());
    }

    private void getFriends(GreeUser owner) {
        status = Consts.UNKNOWN;
        owner.loadFriends(Consts.startIndex_1, Consts.pageSize, listener);
        waitCallback();
        ArrayList<GreeUser> l = (ArrayList<GreeUser>) getBlockRepo().get(PEOPLE_LIST);
        if (l == null)
            fail("No friend got!");

        for (GreeUser person : l) {
            Log.i(TAG, "Friend " + (l.indexOf(person) + 1));
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
        ArrayList<GreeUser> l = (ArrayList<GreeUser>) getBlockRepo().get(PEOPLE_LIST);
        assertEquals("friend count", num, l.size());
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
                return;
            }
        }
        assertTrue("user " + friendName + " in friend list", false);
    }

    @Then("userid of (.+) should be (\\w+) and grade should be (\\w+)")
    public void verifyFriendInfo(String name, String id, String grade) {
        GreeUser friend = (GreeUser) getBlockRepo().get(FRIEND);
        if (friend == null)
            fail("Don't get the friend " + name + " in the list");

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
        ArrayList<GreeUser> l = (ArrayList<GreeUser>) getBlockRepo().get(PEOPLE_LIST);
        if (l == null)
            fail("Don't get the friend in the list");

        Log.d(TAG, "specific user is: " + l.get(0).getNickname());
        getFriends(l.get(0));
    }
}
