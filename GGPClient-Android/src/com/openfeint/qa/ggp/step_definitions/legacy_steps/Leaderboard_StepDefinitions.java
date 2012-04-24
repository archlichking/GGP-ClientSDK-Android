package com.openfeint.qa.ggp.step_definitions.legacy_steps;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.Leaderboard;
import net.gree.asdk.api.Leaderboard.LeaderboardListener;
import net.gree.asdk.api.Leaderboard.Score;
import net.gree.asdk.api.Leaderboard.ScoreListener;
import net.gree.asdk.api.Leaderboard.SuccessListener;

import org.apache.http.HeaderIterator;

import util.Consts;
import android.R.integer;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class Leaderboard_StepDefinitions extends BasicStepDefinition {
	private static final String TAG = "Leaderboard_Steps";

	private List<String> leaderboardNames;

	private static Leaderboard[] leaders;

	private static Score[] ranks;

	private static long originalScore = 0;

	private static long updatedScore = 0;

	private static long newScore = 0;

	private String flag = " ";

	private String status;

	@When("hello dude")
	public void loadLeaderboard() {
		Log.i(TAG, "Hello");
	}

	// waiting for the async callback and assert it success
	private void waitCallback() {
		int count = 0;
		while (count < 30) {
			count++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if ("Deleted".equals(flag)) {
				Log.e(TAG, "Don't have score now!");
				assertEquals(Consts.FAILED, status);
				break;
			}

			if (status != Consts.UNKNOWN) {
				assertEquals(Consts.SUCCESS, status);
				break;
			}
		}
	}

	@When("I try to load out all leaderboards for current user")
	public void getLeaderboards() {

		LeaderboardListener arrayListener = new LeaderboardListener() {

			@Override
			public void onSuccess(int index, int totalListSize,
					Leaderboard[] leaderboards) {
				Log.d(TAG, "Get Leaderboards success!");
				for (int i = 0; i < leaderboards.length; i++) {
					Leaderboard one = leaderboards[i];
					if (one != null)
						Log.d(TAG, "Leaderboard " + i + ": " + one.getName());
				}
				leaders = leaderboards;
				status = Consts.SUCCESS;
			}

			@Override
			public void onFailure(int responseCode, HeaderIterator headers,
					String response) {
				Log.e(TAG, "Get Leadboards failed!");
				status = Consts.FAILED;
			}
		};

		status = Consts.UNKNOWN;
		Leaderboard.loadLeaderboards(Consts.startIndex_1, Consts.pageSize,
				arrayListener);
		waitCallback();

	}

	@Then("(\\w+) leaderboards I have should be return")
	public void verifyLeaderboards() {
		leaderboardNames = new ArrayList<String>();
		leaderboardNames.add("Desc Board 01");
		leaderboardNames.add("Desc Board 02");
		leaderboardNames.add("Asc Board 01");
		leaderboardNames.add("Asc Board 02");
		leaderboardNames.add("Update with lastest point");

		// Verify
		if (leaders == null) {
			fail();
		} else {
			assertEquals(leaderboardNames.size(), leaders.length);
			for (int i = 0; i < leaders.length; i++) {
				assertTrue(leaderboardNames.contains(leaders[0].getName()));
			}
		}
	}

	ScoreListener scoreListener = new ScoreListener() {
		@Override
		public void onSuccess(Score[] entry) {
			Log.d(TAG, "Get leaderboard score success!");
			ranks = entry;
			status = Consts.SUCCESS;
		}

		@Override
		public void onFailure(int responseCode, HeaderIterator headers,
				String response) {
			Log.e(TAG, "Get Leadboard score failed!");
			status = Consts.FAILED;
		}
	};

	public void getScore(String period, String selector, String lid) {
		status = Consts.UNKNOWN;
		int s = Score.MY_SCORES;
		if ("friends".equals(selector)) {
			s = Score.FRIENDS_SCORES;
		} else if ("all".equals(selector)) {
			s = Score.ALL_SCORES;
		}

		int p = Score.DAILY;
		if ("weekly".equals(period)) {
			p = Score.WEEKLY;
		} else if ("total".equals(period)) {
			p = Score.ALL_TIME;
		}

		Leaderboard.getScore(lid, s, p, Consts.startIndex_0, Consts.pageSize,
				scoreListener);
		waitCallback();
	}

	@When("I try to get the (\\w+) ranking of (\\w+) in leaderboard (\\w+)")
	public void getDailyRanking(String period, String selector, String lid) {
		getScore(period, selector, lid);
	}

	@Then("the (\\w+) ranking of (\\w+) in leaderboard (\\w+) should return correctly")
	public void verifyRanking(String period, String selector, String lid) {
		if (ranks == null) {
			fail();
		} else {
			assertEquals(1, ranks[0].getRank());
			assertEquals(GreePlatform.getLocalUser().getNickname(),
					ranks[0].getNickname());
			assertEquals(newScore, ranks[0].getScore());
		}
	}

	private SuccessListener successListener = new SuccessListener() {
		@Override
		public void onSuccess() {
			Log.d(TAG, "Update leaderboard score success!");
			status = Consts.SUCCESS;
		}

		@Override
		public void onFailure(int responseCode, HeaderIterator headers,
				String response) {
			Log.e(TAG, "Update Leadboard score failed!");
			status = Consts.FAILED;
		}
	};

	@Given("I have logged in")
	public void checkLogin() {
		assertNotNull(GreePlatform.getLocalUser());
	}

	@When("I try to get the score of leaderboard which id is (\\w+)")
	public void getScoreAtBeginning(String lid) {
		getScore("total", "me", lid);
	}

	@Then("I should get the score response")
	public void checkScoreResponse() {
		originalScore = ranks[0].getScore();
		Log.i(TAG, "Score at the beginning is: " + originalScore);
	}

	@When("I try to update the score of leaderboard (\\w+) by increase (\\d+)")
	public void updateScore(String lid, int inc) {
		status = Consts.UNKNOWN;
		updatedScore = originalScore + inc;
		Log.i(TAG, "Try to update score to: " + updatedScore);
		Leaderboard.createScore(lid, updatedScore, successListener);
		waitCallback();
	}

	@Then("the score of leaderboard (\\w+) should increase")
	public void verifyScoreUpdated(String lid) {
		getScore("daily", "me", lid);
		assertEquals(updatedScore, ranks[0].getScore());
	}

	@When("I try to delete the score of leaderboard (\\w+)")
	public void deleteScore(String lid) {
		status = Consts.UNKNOWN;
		Leaderboard.deleteScore(lid, successListener);
		waitCallback();
	}

	@Then("the score of leaderboard (\\w+) should be deleted")
	public void verifyScoreDeleted(String lid) {
		flag = "Deleted";
		getScore("daily", "me", lid);
		// assertEquals(0, ranks[0].getScore());
	}

	@When("I try to create a new score (\\d+) for leaderboard (\\w+)")
	public void createScore(int score, String lid) {
		status = Consts.UNKNOWN;
		newScore = score;
		Leaderboard.createScore(lid, newScore, successListener);
		waitCallback();
	}

	@Then("a new score of leaderboard (\\w+) should be created")
	public void verifyScoreCreated(String lid) {
		getScore("daily", "me", lid);
		assertEquals(newScore, ranks[0].getScore());
	}
}
