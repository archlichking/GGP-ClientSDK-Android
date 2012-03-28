
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

import net.gree.asdk.api.Leaderboard;
import net.gree.asdk.api.Leaderboard.LeaderboardArrayListener;
import net.gree.asdk.api.Leaderboard.LeaderboardInfo;
import net.gree.asdk.api.Leaderboard.Ranking;
import net.gree.asdk.api.Leaderboard.RankingListener;
import net.gree.asdk.api.Leaderboard.RankingResponse;
import net.gree.asdk.api.Leaderboard.ResponseArray;
import net.gree.asdk.api.Leaderboard.ScoreListener;
import net.gree.asdk.api.Leaderboard.ScoreResponse;
import net.gree.asdk.api.People;

import org.apache.http.HeaderIterator;

import util.Consts;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Leaderboard_StepDefinitions extends BasicStepDefinition {
    private static final String TAG = "Leaderboard_Steps";

    private List<String> leaderboardNames;

    private static LeaderboardInfo[] leaders;

    private static Ranking[] ranks;

    private static int score = 0;

    private static int updatedScore = 0;

    private static int newScore = 0;

    private String status;

    Leaderboard leaderboard = new Leaderboard();

    @When("hello dude")
    public void loadLeaderboard() {
        Log.i(TAG, "Hello");
    }

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

    @When("I try to load out all leaderboards for current user")
    public void getLeaderboards() {

        LeaderboardArrayListener arrayListener = new LeaderboardArrayListener() {

            @Override
            public void onSuccess(int responseCode, HeaderIterator headers, ResponseArray info) {
                Log.d(TAG, "Get Leaderboards success!");
                for (int i = 0; i < info.entry.length; i++) {
                    LeaderboardInfo one = info.entry[i];
                    if (one != null)
                        Log.d(TAG, "Leaderboard " + i + ": " + one.name);
                }
                leaders = info.entry;
                status = Consts.SUCCESS;
            }

            @Override
            public void onFailure(int responseCode, HeaderIterator headers, String response) {
                Log.e(TAG, "Get Leadboards failed!");
                status = Consts.FAILED;
            }
        };

        status = Consts.UNKNOWN;
        leaderboard.getLeaderboards(People.selfId(), Consts.startIndex_1, Consts.pageSize, null,
                arrayListener);
        waitCallback();

    }

    @Then("all leaderboards I have should be return")
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
                assertTrue(leaderboardNames.contains(leaders[0].name));
            }
        }
    }

    RankingListener rankingListener = new RankingListener() {
        @Override
        public void onSuccess(int responseCode, HeaderIterator headers, RankingResponse info) {
            Log.d(TAG, "Get leaderboard ranking success!");
            for (int i = 0; i < info.entry.length; i++) {
                Ranking one = info.entry[i];
                if (one != null) {
                    Log.i(TAG, "Id: " + one.id);
                    Log.i(TAG, "Nickname: " + one.nickname);
                    Log.i(TAG, "ThumbnailUrl: " + one.thumbnailUrl);
                    Log.i(TAG, "Rank: " + one.rank);
                    Log.i(TAG, "Score: " + one.score);
                }
            }
            ranks = info.entry;
            status = Consts.SUCCESS;
        }

        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String response) {
            Log.e(TAG, "Get Leadboard ranking failed!");
            status = Consts.FAILED;
        }
    };

    public void getRanking(String period, String selector, String lid) {
        status = Consts.UNKNOWN;
        // leaderboard.getRanking(lid, "@friends", "@daily", 0, 10, null,
        // listener);
        // leaderboard.getRanking(lid, "@all", period, 0, 10, null,
        // rankingListener);
        leaderboard.getRanking(lid, "@" + selector, period, Consts.startIndex_0, Consts.pageSize,
                null, rankingListener);
        waitCallback();
    }

    @When("I try to get the (\\w+) ranking of (\\w+) in leaderboard (\\w+)")
    public void getDailyRanking(String period, String selector, String lid) {
        getRanking(period, selector, lid);
    }

    @Then("the (\\w+) ranking of (\\w+) in leaderboard (\\w+) should return correctly")
    public void verifyRanking(String period, String selector, String lid) {
        if (ranks == null) {
            fail();
        } else {
            assertEquals(String.valueOf(1), ranks[0].rank);
            assertEquals(People.getSelf().nickname, ranks[0].nickname);
            assertEquals(String.valueOf(newScore), ranks[0].score);
        }
    }

    private ScoreListener scoreListener = new ScoreListener() {
        @Override
        public void onSuccess(int responseCode, HeaderIterator headers, ScoreResponse info) {
            Log.d(TAG, "Get leaderboard score success!");
            if (info != null) {
                Log.i(TAG, "Score is: " + info.entry.score);
                score = Integer.parseInt(info.entry.score);
            } else
                score = 0;
            status = Consts.SUCCESS;
        }

        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String response) {
            Log.e(TAG, "Get Leadboard ranking failed!");
            status = Consts.FAILED;
        }
    };

    @Given("I have logged in")
    public void checkLogin() {
        assertNotNull(People.selfId());
    }

    @When("I try to get the score of leaderboard which id is (\\w+)")
    public void getScoreAtBeginning(String lid) {
        status = Consts.UNKNOWN;
        leaderboard.getScore("@me", lid, "@self", scoreListener);
        waitCallback();
    }

    @Then("I should get the score response")
    public void checkScoreResponse() {
        Log.i(TAG, "Score at the beginning is: " + score);
    }

    @When("I try to update the score of leaderboard (\\w+) by increase (\\d+)")
    public void updateScore(String lid, int inc) {
        status = Consts.UNKNOWN;
        updatedScore = score + inc;
        leaderboard.createScore(lid, String.valueOf(updatedScore), scoreListener);
        waitCallback();
    }

    @Then("the score should increase")
    public void verifyScoreUpdated() {
        assertEquals(updatedScore, score);

    }

    @When("I try to delete the score of leaderboard (\\w+)")
    public void deleteScore(String lid) {
        status = Consts.UNKNOWN;
        leaderboard.deleteScore(lid, scoreListener);
        waitCallback();
    }

    @Then("the score should be deleted")
    public void verifyScoreDeleted() {
        assertEquals(0, score);
    }

    @When("I try to create a new score (\\d+) for leaderboard (\\w+)")
    public void createScore(int score, String lid) {
        status = Consts.UNKNOWN;
        newScore = score;
        leaderboard.createScore(lid, String.valueOf(newScore), scoreListener);
        waitCallback();
    }

    @Then("a new score should be created")
    public void verifyScoreCreated() {
        assertEquals(newScore, score);
    }
}
