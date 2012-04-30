
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
            public void onSuccess(int index, int totalListSize, Leaderboard[] leaderboards) {
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
            public void onFailure(int responseCode, HeaderIterator headers, String response) {
                Log.e(TAG, "Get Leadboards failed!");
                boards = null;
                status = Consts.FAILED;
            }
        };

        status = Consts.UNKNOWN;
        Leaderboard.loadLeaderboards(Consts.startIndex_1, Consts.pageSize, leaderboardListener);
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
    public void verifyLeaderboardInfo(String boardName, String updateStrategy, String isSecretMark,
            String isAscMark) {
        Leaderboard board = getBoardByName(boardName);
        String isSecret = transLeaderboardStatus(isSecretMark);
        String isAsc = transLeaderboardStatus(isAscMark);
        Log.i(TAG, "Checking leaderboard info of " + boardName + "...");
        // We could not get allowWorseScore from sdk api
        assertEquals("leaderboard is asc order?", isAsc, board.getSort());
        assertEquals("leaderboard is secret?", isSecret, board.getSecret());
    }

    @Given("I load list of leaderboard")
    public void getLeaderboardsAsCondition() {
        getLeaderboards();
    }

    private ScoreListener scoreListener = new ScoreListener() {
        @Override
        public void onSuccess(Score[] entry) {
            Log.d(TAG, "Get leaderboard score success!");
            ranks = entry;
            status = Consts.SUCCESS;
        }

        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String response) {
            Log.d(TAG, "No leaderboard score!");
            ranks = null;
            // When leaderboard doesn't have score, is also return 404. Sucks
            // and I have to keep status not to failed
            status = Consts.SUCCESS;
        }
    };

    private SuccessListener successListener = new SuccessListener() {
        @Override
        public void onSuccess() {
            Log.d(TAG, "Update leaderboard score success!");
            status = Consts.SUCCESS;
        }

        @Override
        public void onFailure(int responseCode, HeaderIterator headers, String response) {
            Log.e(TAG, "Update Leadboard score failed!");
            if (response != null)
                Log.e(TAG, "response: " + response.substring(response.indexOf("\"message\"")));
            status = Consts.FAILED;
        }
    };

    private void getScore(String selector, String period, String lid) {
        int iSelector = -1;
        if ("FRIENDS".equals(selector)) {
            iSelector = Score.FRIENDS_SCORES;
        } else if ("EVERYONE".equals(selector)) {
            iSelector = Score.ALL_SCORES;
        } else if ("ME".equals(selector)) {
            iSelector = Score.MY_SCORES;
        } else {
            fail("Using an unknown selector " + selector + " to getScore!");
        }

        int iPeroid = -1;
        if ("DAILY".equals(period)) {
            iPeroid = Score.DAILY;
        } else if ("WEEKLY".equals(period)) {
            iPeroid = Score.WEEKLY;
        } else if ("TOTAL".equals(period)) {
            iPeroid = Score.ALL_TIME;
        } else {
            fail("Using an unknown period " + period + " to getScore!");
        }

        status = Consts.UNKNOWN;
        Log.i(TAG, "Try to get leaderboard ranking and score...");
        Leaderboard.getScore(lid, iSelector, iPeroid, Consts.startIndex_0, Consts.pageSize,
                scoreListener);
        waitCallback();
    }

    private void deleteScore(String lid) {
        status = Consts.UNKNOWN;
        Log.i(TAG, "Try to delete score of leaderboard...");
        Leaderboard.deleteScore(lid, successListener);
        waitCallback();
    }

    private void createScore(int score, String lid) {
        status = Consts.UNKNOWN;
        Log.i(TAG, "Try to create new score for leaderboard...");
        Leaderboard.createScore(lid, score, successListener);
        waitCallback();
    }

    @Given("I make sure my score (\\w+) in leaderboard (.+)")
    public void updateScoreAsCondition(String isExistsMark, String boardName) {
        Leaderboard board = getBoardByName(boardName);
        if (!("NOTEXISTS".equals(isExistsMark) || "EXISTS".equals(isExistsMark)))
            fail("Unknown isExistsMark!");
        getScore("ME", "TOTAL", board.getId());
        Log.i(TAG, "Expect the score of leaderboard " + boardName + " is " + isExistsMark);

        if ("NOTEXISTS".equals(isExistsMark) && ranks != null && ranks.length > 0) {
            Log.i(TAG, "But no as we expected");
            deleteScore(board.getId());
        } else if ("EXISTS".equals(isExistsMark) && ranks == null) {
            Log.i(TAG, "But no as we expected");
            createScore(defaultScore, board.getId());
        }
    }

    @When("I add score to leaderboard (.+) with score (-?\\d+)")
    public void createScoreByName(String boardName, int score) {
        Leaderboard board = getBoardByName(boardName);
        // Record score updated for the below steps
        updatedScore = score;
        createScore(score, board.getId());
    }

    @Then("my (\\w+) score ranking of leaderboard (.+) should be (\\d+)")
    public void verifyRankingByName(String period, String boardName, int ranking) {
        Leaderboard board = getBoardByName(boardName);
        getScore("ME", period, board.getId());
        Log.i(TAG, "Verify the ranking...");
        if (ranks == null)
            fail("Failed to verify ranking, no score or get score failed!");
        Log.d(TAG, "Now score is: " + ranks[0].getScore());
        assertEquals("ranking", ranking, ranks[0].getRank());
    }

    @After("I make sure my score (\\w+) in leaderboard (.+)")
    public void recoverScoreData(String isExistsMark, String boardName) {
        updateScoreAsCondition(isExistsMark, boardName);
    }
}
