package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

import net.gree.asdk.api.GreePerson;
import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.GreePerson.GreePersonListener;

import org.apache.http.HeaderIterator;

import util.Consts;

import android.util.Log;

import java.security.acl.Owner;
import java.util.ArrayList;
import java.util.List;

public class People_StepDefinitions extends BasicStepDefinition {
	private static final String TAG = "People_Steps";

	private static List<GreePerson> peopleList;

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

	GreePersonListener listener = new GreePersonListener() {
		@Override
		public void onSuccess(int index, int count, GreePerson[] people) {
			Log.d(TAG, "Get people success!");
			if (people != null) {
				Log.i(TAG, "Adding people datas");
				Log.i(TAG, "Get " + people.length + "people");
				peopleList = new ArrayList<GreePerson>();
				for (int i = 0; i < people.length; i++) {
					peopleList.add(people[i]);
				}
			}
			status = Consts.SUCCESS;
		}

		@Override
		public void onFailure(int responseCode, HeaderIterator headers,
				String response) {
			Log.e(TAG, "Get people failed!");
			status = Consts.FAILED;
		}
	};

	private void verifyCurrentUserInfo(GreePerson person) {
		if (!person.getId().equals("57574"))
			fail("Now we can only check auto-grade3-user, please relogin that user!");
		assertEquals("auto-grade3-user", person.getNickname());
		assertEquals(String.valueOf(3), person.getUserGrade());
		assertEquals("US", person.getRegion());

	}

	@Then("I can get my info from native storage")
	public void verifyNativeCurrentUserInfo() {
		GreePerson person = GreePlatform.getLocalUser();
		Log.i(TAG, "User info get from native:");
		Log.i(TAG, "ID: " + person.getId());
		Log.i(TAG, "Nickname: " + person.getNickname());
		Log.i(TAG, "DisplayName: " + person.getDisplayName());
		Log.i(TAG, "UserGrade: " + person.getUserGrade());
		Log.i(TAG, "Region: " + person.getRegion());
		Log.i(TAG, "ThumbnailUrl: " + person.getThumbnailUrl());

		verifyCurrentUserInfo(person);
	}

	@And("I can request from server to get my info")
	public void verifyServerCurrentUserInfo() {
		status = Consts.UNKNOWN;
		GreePerson.loadUserWithId(1, 1, "@me", listener);
		waitCallback();
		if (peopleList == null)
			fail();
		GreePerson person = peopleList.get(0);
		Log.i(TAG, "User info get from server:");
		Log.i(TAG, "ID: " + person.getId());
		Log.i(TAG, "Nickname: " + person.getNickname());
		Log.i(TAG, "DisplayName: " + person.getDisplayName());
		Log.i(TAG, "UserGrade: " + person.getUserGrade());
		Log.i(TAG, "Region: " + person.getRegion());
		Log.i(TAG, "ThumbnailUrl: " + person.getThumbnailUrl());

		verifyCurrentUserInfo(person);
	}

	private void getFriends(GreePerson owner) {
		status = Consts.UNKNOWN;
		owner.loadFriends(Consts.startIndex_1, Consts.pageSize, listener);
		waitCallback();
		if (peopleList == null)
			fail();
		for (GreePerson person : peopleList) {
			Log.i(TAG, "Friend " + (peopleList.indexOf(person) + 1));
			Log.i(TAG, "ID: " + person.getId());
			Log.i(TAG, "Nickname: " + person.getNickname());
			Log.i(TAG, "DisplayName: " + person.getDisplayName());
			Log.i(TAG, "UserGrade: " + person.getUserGrade());
			Log.i(TAG, "ThumbnailUrl: " + person.getThumbnailUrl());
		}
	}

	@When("I try to get friends info of current user")
	public void getCurrentUserFriends() {
		GreePerson me = GreePlatform.getLocalUser();
		getFriends(me);

	}

	@Then("I should get the friend list success")
	public void verifyFriendList() {
		// TODO add verification after got a solution for create test data
	}

	@When("I try to get friends info of user (\\w+)")
	public void getSpecificUserFriends(String pid) {
		GreePerson.loadUserWithId(1, 1, pid, listener);
		getFriends(peopleList.get(0));
	}

}
