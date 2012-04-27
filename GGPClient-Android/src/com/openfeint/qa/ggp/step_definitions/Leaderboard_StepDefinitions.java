package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import net.gree.asdk.api.Leaderboard;
import net.gree.asdk.api.Leaderboard.LeaderboardListener;
import net.gree.asdk.api.Leaderboard.Score;
import net.gree.asdk.api.Leaderboard.ScoreListener;
import net.gree.asdk.api.Leaderboard.SuccessListener;

import org.apache.http.HeaderIterator;

import util.Consts;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class Leaderboard_StepDefinitions extends BasicStepDefinition {
	private static final String TAG = "Leaderboard_Steps";

	private static Leaderboard[] boards;

	private static Score[] ranks;

	private static int updatedScore;

	private int defaultScore = 1;

	private String status;

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

	@When("I load list of leaderboard")
	public void getLeaderboards() {
		LeaderboardListener leaderboardListener = new LeaderboardListener() {

			@Override
			public void onSuccess(int index, int totalListSize,
					Leaderboard[] leaderboards) {
				Log.d(TAG, "Get Leaderboards success!");
				for (int i = 0; i < leaderboards.length; i++) {
					Leaderboard one = leaderboards[i];
					if (one != null)
						Log.d(TAG, "Leaderboard " + i + ": " + one.getName());
				}
				boards = leaderboards;
				status = Consts.SUCCESS;
			}

			@Override
			public void onFailure(int responseCode, HeaderIterator headers,
					String response) {
				Log.e(TAG, "Get Leadboards failed!");
				boards = null;
				status = Consts.FAILED;
			}
		};

		status = Consts.UNKNOWN;
		Leaderboard.loadLeaderboards(Consts.startIndex_1, Consts.pageSize,
				leaderboardListener);
		waitCallback();
	}

	@Then("I should have total leaderboards (\\d+)")
	public void verifyLeaderboardCount(int expectCount) {
		if (boards == null)
			fail("No leaderboard in the list!");
		Log.i(TAG, "Verifing the leaderboard count...");
		assertEquals("leaderboard count", expectCount, boards.length);
	}

	private Leaderboard getBoardByName(String boardName) {
		if (boards == null)
			fail("No leaderboard in the list!");
		for (Leaderboard board : boards) {
			if (boardName.equals(board.getName())) {
				Log.d(TAG, "Found the leaderboard " + boardName);
				return board;
			}
		}
		fail("Count not find the leaderboard named: " + boardName);
		return null;
	}

	private String transLeaderboardStatus(String mark) {
		if ("YES".equals(mark))
			return "1";
		else if ("NO".equals(mark))
			return "0";
		else {
			fail("Unknown status " + mark + "!");
			return "Unknown Status!";
		}
	}

	@Then("I should have leaderboard of name (.+) with allowWorseScore (\\w+) and secret (\\w+) and order asc (\\w+)")
	public void verifyLeaderboardInfo(String boardName, String updateStrategy,
			String isSecretMark, String isAscMark) {
		Leaderboard board = getBoardByName(boardName);
		String isSecret = transLeaderboardStatus(isSecretMark);
		String isAsc = transLeaderboardStatus(isAscMark);
		Log.i(TAG, "Checking leaderboard info of " + boardName + "...");
		// We could not get allowWorseScore from sdk api
		assertEquals("leaderboard is asc order?", isAsc, board.getSort());
		assertEquals("leaderboard is secret?", isSecret, board.getSecret());
	}

}
