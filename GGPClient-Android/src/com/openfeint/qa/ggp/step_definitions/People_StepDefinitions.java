
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

import net.gree.asdk.api.People;
import net.gree.asdk.api.People.PeopleListener;
import net.gree.asdk.api.People.Person;

import org.apache.http.HeaderIterator;

import util.Consts;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class People_StepDefinitions extends BasicStepDefinition {
    private static final String TAG = "People_Steps";

    private static List<Person> peopleList;

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

    PeopleListener listener = new PeopleListener() {
        @Override
        public void onSuccess(int responseCode, HeaderIterator headers, int index, int count,
                Person[] people) {
            Log.d(TAG, "Get people success!");
            if (people != null) {
                Log.i(TAG, "Adding people datas");
                Log.i(TAG, "Get " + people.length + "people");
                peopleList = new ArrayList<Person>();
                for (int i = 0; i < people.length; i++) {
                    peopleList.add(people[i]);
                }
            }
            status = Consts.SUCCESS;
        }

        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String response) {
            Log.e(TAG, "Get people failed!");
            status = Consts.FAILED;
        }
    };

    private void verifyCurrentUserInfo(Person person) {
        if (!person.id.equals("57574"))
            fail("Now we can only check auto-grade3-user, please relogin that user!");
        assertEquals("auto-grade3-user", person.nickname);
        assertEquals(String.valueOf(3), person.userGrade);
        assertEquals("US", person.region);

    }

    @Then("I can get my info from native storage")
    public void verifyNativeCurrentUserInfo() {
        Person person = People.getSelf();
        Log.i(TAG, "User info get from native:");
        Log.i(TAG, "ID: " + person.id);
        Log.i(TAG, "SelfID: " + People.selfId());
        Log.i(TAG, "Nickname: " + person.nickname);
        Log.i(TAG, "DisplayName: " + person.displayName);
        Log.i(TAG, "UserGrade: " + person.userGrade);
        Log.i(TAG, "Region: " + person.region);
        Log.i(TAG, "ThumbnailUrl: " + person.thumbnailUrl);

        verifyCurrentUserInfo(person);
    }

    @And("I can request from server to get my info")
    public void verifyServerCurrentUserInfo() {
        People people = new People();
        status = Consts.UNKNOWN;
        people.self(null, listener);
        waitCallback();
        if (peopleList == null)
            fail();
        Person person = peopleList.get(0);
        Log.i(TAG, "User info get from server:");
        Log.i(TAG, "ID: " + person.id);
        Log.i(TAG, "Nickname: " + person.nickname);
        Log.i(TAG, "DisplayName: " + person.displayName);
        Log.i(TAG, "UserGrade: " + person.userGrade);
        Log.i(TAG, "Region: " + person.region);
        Log.i(TAG, "ThumbnailUrl: " + person.thumbnailUrl);

        verifyCurrentUserInfo(person);
    }

    private void getFriends(String guid) {
        People people = new People();
        status = Consts.UNKNOWN;
        // people.friends(Consts.startIndex_0, Consts.pageSize, null, "@me",
        // listener);
        people.friends(Consts.startIndex_0, Consts.pageSize, null, guid, listener);
        waitCallback();
        if (peopleList == null)
            fail();
        for (Person person : peopleList) {
            Log.i(TAG, "Friend " + (peopleList.indexOf(person) + 1));
            Log.i(TAG, "ID: " + person.id);
            Log.i(TAG, "Nickname: " + person.nickname);
            Log.i(TAG, "DisplayName: " + person.displayName);
            Log.i(TAG, "UserGrade: " + person.userGrade);
            Log.i(TAG, "ThumbnailUrl: " + person.thumbnailUrl);
        }
    }

    @When("I try to get friends info of current user")
    public void getCurrentUserFriends() {
        getFriends(People.selfId());

    }

    @Then("I should get the friend list success")
    public void verifyFriendList() {
        // TODO add verification after got a solution for create test data
    }

    @When("I try to get friends info of user (\\w+)")
    public void getSpecificUserFriends(String guid) {
        getFriends(guid);
    }

}
