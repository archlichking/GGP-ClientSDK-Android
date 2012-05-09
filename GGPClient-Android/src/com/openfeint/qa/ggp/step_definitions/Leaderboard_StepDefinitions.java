
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

import net.gree.asdk.api.Leaderboard;
import net.gree.asdk.api.Leaderboard.LeaderboardListener;
import net.gree.asdk.api.Leaderboard.Score;
import net.gree.asdk.api.Leaderboard.ScoreListener;
import net.gree.asdk.api.Leaderboard.SuccessListener;

import org.apache.http.HeaderIterator;

import util.Consts;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.AssertionFailedError;

public class Leaderboard_StepDefinitions extends BasicStepDefinition {
    private static final String TAG = "Leaderboard_Steps";

    private static String LEADERBOARD_LIST = "leaderboardlist";

    private static String SCORE = "score";

    private static String ALL_SCORE = "allscore";

    private int defaultScore = 3000;

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

    private int transSelector(String selector) {
        int iSelector = -100;
        if ("FRIENDS".equals(selector)) {
            iSelector = Score.FRIENDS_SCORES;
        } else if ("EVERYONE".equals(selector)) {
            iSelector = Score.ALL_SCORES;
        } else if ("ME".equals(selector)) {
            iSelector = Score.MY_SCORES;
        }
        return iSelector;
    }

    private int transPeriod(String period) {
        int iPeriod = -100;
        if ("DAILY".equals(period)) {
            iPeriod = Score.DAILY;
        } else if ("WEEKLY".equals(period)) {
            iPeriod = Score.WEEKLY;
        } else if ("TOTAL".equals(period)) {
            iPeriod = Score.ALL_TIME;
        }
        return iPeriod;
    }

    @Given("I load list of leaderboard")
    @When("I load list of leaderboard")
    public void getLeaderboards() {
        notifyStepWait();
        Leaderboard.loadLeaderboards(Consts.startIndex_1, Consts.pageSize,
                new LeaderboardListener() {

                    @Override
                    public void onSuccess(int index, int totalListSize, Leaderboard[] leaderboards) {
                        Log.d(TAG, "Get Leaderboards success!");
                        getBlockRepo().put(LEADERBOARD_LIST, new ArrayList<Leaderboard>());
                        for (int i = 0; i < leaderboards.length; i++) {
                            Log.d(TAG, "Leaderboard " + i + ": " + leaderboards[i].getName());
                        }
                        ((ArrayList<Leaderboard>) getBlockRepo().get(LEADERBOARD_LIST))
                                .addAll(Arrays.asList(leaderboards));
                        notifyStepPass();

                    }

                    @Override
                    public void onFailure(int responseCode, HeaderIterator headers, String response) {
                        Log.e(TAG, "Get Leadboards failed!");
                        getBlockRepo().put(LEADERBOARD_LIST, new ArrayList<Leaderboard>());
                        notifyStepPass();
                    }
                });
    }

    @Then("I should have total leaderboards (\\d+)")
    public void verifyLeaderboardCount(int expectCount) {
        ArrayList<Leaderboard> l = (ArrayList<Leaderboard>) getBlockRepo().get(LEADERBOARD_LIST);

        if (l == null)
            fail("No leaderboard in the list!");

        Log.i(TAG, "Verifing the leaderboard count...");
        assertEquals("leaderboard count", expectCount, l.size());
    }

    @Then("I should have leaderboard of name (.+) with allowWorseScore (\\w+) and secret (\\w+) and order asc (\\w+)")
    public void verifyLeaderboardInfo(String boardName, String updateStrategy, String isSecretMark,
            String isAscMark) {
        ArrayList<Leaderboard> l = (ArrayList<Leaderboard>) getBlockRepo().get(LEADERBOARD_LIST);
        if (l == null)
            fail("No leaderboard in the list!");
        for (Leaderboard board : l) {
            if (boardName.equals(board.getName())) {
                Log.d(TAG, "Found the leaderboard " + boardName);
                assertEquals("leaderboard is asc order?", transLeaderboardStatus(isAscMark),
                        board.getSort());
                assertEquals("leaderboard is secret?", transLeaderboardStatus(isSecretMark),
                        board.getSecret());
                return;
            }
        }
        fail("cannot find the leaderboard named: " + boardName);
    }

    @Given("I make sure my score (\\w+) in leaderboard (.+)")
    @After("I make sure my score (\\w+) in leaderboard (.+)")
    public void updateScoreAsCondition(String isExistsMark, String boardName) {
        ArrayList<Leaderboard> l = (ArrayList<Leaderboard>) getBlockRepo().get(LEADERBOARD_LIST);
        if (!"NOTEXISTS".equals(isExistsMark) && !"EXISTS".equals(isExistsMark))
            fail("Unknown isExistsMark!");

        for (Leaderboard board : l) {
            if (boardName.equals(board.getName())) {
                notifyStepWait();
                if ("NOTEXISTS".equals(isExistsMark)) {
                    Leaderboard.deleteScore(board.getId(), new SuccessListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "delete leaderboard score success!");
                            notifyStepPass();
                        }

                        @Override
                        public void onFailure(int responseCode, HeaderIterator headers,
                                String response) {
                            Log.e(TAG, "delete Leadboard score failed!");
                            if (response != null)
                                Log.e(TAG,
                                        "response: "
                                                + response.substring(response
                                                        .indexOf("\"message\"")));
                            notifyStepPass();
                        }
                    });
                } else {
                    Leaderboard.createScore(board.getId(), defaultScore, new SuccessListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "create leaderboard score success!");
                            notifyStepPass();
                        }

                        @Override
                        public void onFailure(int responseCode, HeaderIterator headers,
                                String response) {
                            Log.e(TAG, "create Leadboard score failed!");
                            if (response != null)
                                Log.e(TAG,
                                        "response: "
                                                + response.substring(response
                                                        .indexOf("\"message\"")));
                            notifyStepPass();
                        }
                    });
                }
                return;
            }
        }

        fail("cannot find the leaderboard named: " + boardName);
    }

    @When("I add score to leaderboard (.+) with score (-?\\d+)")
    public void createScoreByName(String boardName, int score) {
        ArrayList<Leaderboard> l = (ArrayList<Leaderboard>) getBlockRepo().get(LEADERBOARD_LIST);
        // Record score updated for the below steps
        for (Leaderboard board : l) {
            if (boardName.equals(board.getName())) {
                notifyStepWait();
                Log.d(TAG, "Fond the leaderboard " + boardName);
                Log.i(TAG, "Try to create new score for leaderboard...");
                Leaderboard.createScore(board.getId(), score, new SuccessListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Update leaderboard score success!");
                        notifyStepPass();
                    }

                    @Override
                    public void onFailure(int responseCode, HeaderIterator headers, String response) {
                        Log.e(TAG, "Update Leadboard score failed!");
                        if (response != null)
                            Log.e(TAG,
                                    "response: "
                                            + response.substring(response.indexOf("\"message\"")));
                        notifyStepPass();
                    }
                });
                return;
            }
        }
        fail("cannot find the leaderboard named: " + boardName);
    }

    @Given("I load top friend score list for leaderboard (.*) for period (\\w+)")
    @When("I load top friend score list for leaderboard (.*) for period (\\w+)")
    public void loadTopFriendScoreListOnDemand(String boardName, String period) {
        loadScoreListOnDemand("FRIENDS", boardName, period);
    }

    @Given("I load top score list for leaderboard (.*) for period (\\w+)")
    @When("I load top score list for leaderboard (.*) for period (\\w+)")
    public void loadTopScoreListOnDemand(String boardName, String period) {
        loadScoreListOnDemand("EVERYONE", boardName, period);
    }

    @Given("I load score list of (\\w+) section for leaderboard (.*) for period (\\w+)")
    @When("I load score list of (\\w+) section for leaderboard (.*) for period (\\w+)")
    public void loadScoreListOnDemand(String selection, String boardName, String period) {
        ArrayList<Leaderboard> l = (ArrayList<Leaderboard>) getBlockRepo().get(LEADERBOARD_LIST);
        for (Leaderboard board : l) {
            if (boardName.equals(board.getName())) {
                notifyStepWait();
                Log.d(TAG, "Found the leaderboard " + boardName);
                getBlockRepo().put(ALL_SCORE, new ArrayList<Score>());
                Leaderboard.getScore(board.getId(), transSelector(selection), transPeriod(period),
                        Consts.startIndex_0, Consts.pageSize, new ScoreListener() {
                            @Override
                            public void onSuccess(Score[] entry) {

                                Log.d(TAG, "Get leaderboard score success!");
                                ((ArrayList<Score>) getBlockRepo().get(ALL_SCORE)).addAll(Arrays
                                        .asList(entry));
                                notifyStepPass();
                            }

                            @Override
                            public void onFailure(int responseCode, HeaderIterator headers,
                                    String response) {
                                Log.d(TAG, "No leaderboard score!");
                                // When leaderboard doesn't have score, is also
                                // return 404. Sucks
                                // and I have to keep status not to failed
                                notifyStepPass();
                            }
                        });

                return;
            }
        }
        fail("cannot find the leaderboard named: " + boardName);
    }

    @When("I delete my score in leaderboard (.+)")
    public void deleteMyScoreInLeaderboard(final String boardName) {
        ArrayList<Leaderboard> l = (ArrayList<Leaderboard>) getBlockRepo().get(LEADERBOARD_LIST);
        // Record score updated for the below steps
        for (Leaderboard board : l) {
            if (boardName.equals(board.getName())) {
                notifyStepWait();
                Log.d(TAG, "Found the leaderboard " + boardName);
                Leaderboard.deleteScore(board.getId(), new SuccessListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "delete leaderboard score success!");
                        notifyStepPass();
                    }

                    @Override
                    public void onFailure(int responseCode, HeaderIterator headers, String response) {
                        Log.e(TAG, "delete Leadboard score failed!");
                        if (response != null)
                            Log.e(TAG,
                                    "response: "
                                            + response.substring(response.indexOf("\"message\"")));
                        notifyStepPass();
                    }
                });
                return;
            }
        }
        fail("cannot find the leaderboard named: " + boardName);
    }

    @Then("my score (-?\\d+) should be updated in leaderboard (.+)")
    public void verifyMyScoreInLeaderboard(final int score, final String boardName) {
        ArrayList<Leaderboard> l = (ArrayList<Leaderboard>) getBlockRepo().get(LEADERBOARD_LIST);
        for (Leaderboard board : l) {
            if (boardName.equals(board.getName())) {
                Log.i(TAG, "Try to get leaderboard ranking and score...");
                Leaderboard.getScore(board.getId(), transSelector("ME"), transPeriod("TOTAL"),
                        Consts.startIndex_0, Consts.pageSize, new ScoreListener() {
                            @Override
                            public void onSuccess(Score[] entry) {

                                Log.d(TAG, "Get leaderboard score success!");
                                getBlockRepo().put(SCORE, entry[0]);
                                notifyAsyncInStep();
                            }

                            @Override
                            public void onFailure(int responseCode, HeaderIterator headers,
                                    String response) {
                                Log.d(TAG, "No leaderboard score!");
                                // When leaderboard doesn't have score, is also
                                // return 404. Sucks
                                // and I have to keep status not to failed
                                getBlockRepo().put(SCORE, new Score());
                                notifyAsyncInStep();
                            }
                        });

                waitForAsyncInStep();

                assertEquals(score, ((Score) getBlockRepo().get(SCORE)).getScore());
                return;
            }
        }
        fail("cannot find the leaderboard named: " + boardName);
    }

    @Then("my (\\w+) score ranking of leaderboard (.+) should be (\\d+)")
    public void verifyRankingByName(String period, final String boardName, final int ranking) {
        ArrayList<Leaderboard> l = (ArrayList<Leaderboard>) getBlockRepo().get(LEADERBOARD_LIST);
        for (Leaderboard board : l) {
            if (boardName.equals(board.getName())) {
                Log.i(TAG, "Try to get leaderboard ranking and score...");
                Leaderboard.getScore(board.getId(), transSelector("EVERYONE"), transPeriod(period),
                        Consts.startIndex_0, Consts.pageSize, new ScoreListener() {
                            @Override
                            public void onSuccess(Score[] entry) {

                                Log.d(TAG, "Get leaderboard score success!");
                                getBlockRepo().put(SCORE, entry[0]);
                                notifyAsyncInStep();
                            }

                            @Override
                            public void onFailure(int responseCode, HeaderIterator headers,
                                    String response) {
                                Log.d(TAG, "No leaderboard score!");
                                // When leaderboard doesn't have score, is also
                                // return 404. Sucks
                                // and I have to keep status not to failed
                                getBlockRepo().put(SCORE, new Score());
                                notifyAsyncInStep();
                            }
                        });
                waitForAsyncInStep();
                assertEquals(ranking, ((Score) getBlockRepo().get(SCORE)).getRank());
                return;
            }
        }
        fail("cannot find the leaderboard named: " + boardName);
    }

    @Then("list should have score (\\d+) of player (.*) with rank (\\d+)")
    public void verifyScoreInAllScoreList(int score, String pName, long rank) {
        ArrayList<Score> l = (ArrayList<Score>) getBlockRepo().get(ALL_SCORE);
        for (Score s : l) {
            if (s.getNickname().equals(pName)) {
                assertEquals(s.getScore(), score);
                assertEquals(s.getRank(), rank);
                return;
            }
        }
        fail("cannot find the score of user name: " + pName);
    }

}
