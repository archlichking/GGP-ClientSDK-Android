
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Assert;
import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.IconDownloadListener;
import net.gree.asdk.api.Leaderboard;
import net.gree.asdk.api.Leaderboard.LeaderboardListener;
import net.gree.asdk.api.Leaderboard.Score;
import net.gree.asdk.api.Leaderboard.ScoreListener;
import net.gree.asdk.api.Leaderboard.SuccessListener;

import org.apache.http.HeaderIterator;

import util.Consts;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.ggp.R;

public class LeaderboardStepDefinitions extends BasicStepDefinition {
    private static final String TAG = "Leaderboard_Steps";

    private static final String LEADERBOARD_LIST = "leaderboardlist";

    private static final String SCORE = "score";

    private static final String ALL_SCORE = "allscore";

    private static final String ICON = "leaderboard_icon";

    private int defaultScore = 3000;

    private int transLeaderboardOrderStatus(String mark) {
        if ("YES".equals(mark))
            return 1;
        else if ("NO".equals(mark))
            return 0;
        else {
            fail("Unknown status " + mark + "!");
            return -1;
        }
    }

    private boolean transLeaderboardSecretStatus(String mark) {
        if ("YES".equals(mark))
            return true;
        else if ("NO".equals(mark))
            return false;
        else {
            fail("Unknown status " + mark + "!");
            return false;
        }
    }

    private int transSelector(String selector) {
        int iSelector = Consts.INVALID_INT;
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
        int iPeriod = Consts.INVALID_INT;
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
    @And("I load list of leaderboard")
    public void getLeaderboards() {
        getLeaderboard(Consts.STARTINDEX_1, Consts.PAGESIZE_ALL);
    }

    private void getLeaderboard(int startIndex, int pageSize) {
        notifyStepWait();
        Leaderboard.loadLeaderboards(startIndex, pageSize, new LeaderboardListener() {

            @Override
            public void onSuccess(int index, int totalListSize, Leaderboard[] leaderboards) {
                Log.d(TAG, "Get Leaderboards success!");
                getBlockRepo().put(LEADERBOARD_LIST, new ArrayList<Leaderboard>());
                for (int i = 0; i < leaderboards.length; i++) {
                    Log.d(TAG, "Leaderboard " + i + ": " + leaderboards[i].getName());
                }
                ((ArrayList<Leaderboard>) getBlockRepo().get(LEADERBOARD_LIST)).addAll(Arrays
                        .asList(leaderboards));
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
                assertEquals("leaderboard is asc order?", transLeaderboardOrderStatus(isAscMark),
                        board.getSort());
                assertEquals("leaderboard is secret?", transLeaderboardSecretStatus(isSecretMark),
                        board.isSecret());
                return;
            }
        }
        fail("cannot find the leaderboard named: " + boardName);
    }

    @Given("I make sure my score (\\w+) in leaderboard (.+)")
    @After("I make sure my score (\\w+) in leaderboard (.+)")
    @And("I make sure my score (\\w+) in leaderboard (.+)")
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
                                Log.e(TAG, "response: " + response);
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
        notifyStepWait();
        ArrayList<Leaderboard> l = (ArrayList<Leaderboard>) getBlockRepo().get(LEADERBOARD_LIST);
        String board_id = Consts.INVALID_INT_STRING;
        // Record score updated for the below steps
        for (Leaderboard board : l) {
            if (boardName.equals(board.getName())) {
                board_id = board.getId();
                break;
            }
        }
        Log.i(TAG, "Try to create new score for leaderboard...");
        Leaderboard.createScore(board_id, score, new SuccessListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Update leaderboard score success!");
                notifyStepPass();
            }

            @Override
            public void onFailure(int responseCode, HeaderIterator headers, String response) {
                Log.e(TAG, "Update Leadboard score failed!");
                if (response != null)
                    Log.e(TAG, "response: " + response);
                notifyStepPass();
            }
        });
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
                        Consts.STARTINDEX_1, Consts.PAGESIZE_ALL, new ScoreListener() {
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
        notifyStepWait();
        Leaderboard board = getBoardFromList(boardName);
        String boardId = Consts.INVALID_INT_STRING;
        // Record score updated for the below steps
        if (board != null) {
            boardId = board.getId();
        }

        Log.d(TAG, "Found the leaderboard " + boardName);
        Leaderboard.deleteScore(boardId, new SuccessListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "delete leaderboard score success!");
                notifyStepPass();
            }

            @Override
            public void onFailure(int responseCode, HeaderIterator headers, String response) {
                Log.e(TAG, "delete Leadboard score failed!");
                if (response != null)
                    Log.e(TAG, "response: " + response);
                notifyStepPass();
            }
        });
    }

    private Leaderboard getBoardFromList(String boardName) {
        ArrayList<Leaderboard> l = (ArrayList<Leaderboard>) getBlockRepo().get(LEADERBOARD_LIST);
        for (Leaderboard board : l) {
            if (boardName.equals(board.getName())) {
                return board;
            }
        }
        return null;
    }

    @Then("my score (-?\\d+) should be updated in leaderboard (.+)")
    public void verifyMyScoreInLeaderboard(int score, final String boardName) {
        int format = getMyScore(boardName);
        waitForAsyncInStep();

        if (format == Leaderboard.FORMAT_TIME) {
            String time = "";
            if (score != 0)
                time = String.format("%01d:%02d:%02d", score / 3600, score % 3600 / 60, score % 60);
            assertEquals("high score", time, ((Score) getBlockRepo().get(SCORE)).getScoreAsString());
        } else {
            // a small trick to match the step with ios
            if (score == 0)
                score = -1;
            long returnScore;
            try {
                returnScore = ((Score) getBlockRepo().get(SCORE)).getScore();
            } catch (NullPointerException e) {
                returnScore = -1;
            }
            assertEquals("high score", score, returnScore);
        }
    }

    @Then("my score (-?\\d+) should not be updated in leaderboard (.+)")
    public void verifyMyScoreNotBeUpdated(int score, String boardName) {
        int format = getMyScore(boardName);
        waitForAsyncInStep();
        if (format == Leaderboard.FORMAT_TIME) {
            String time = String.format("%01d:%02d:%02d", score / 3600, score % 3600 / 60,
                    score % 60);
            assertTrue("score is not updated",
                    !time.equals(((Score) getBlockRepo().get(SCORE)).getScoreAsString()));
        } else {
            long returnScore;
            try {
                returnScore = ((Score) getBlockRepo().get(SCORE)).getScore();
            } catch (NullPointerException e) {
                returnScore = Consts.INVALID_INT;
            }
            assertTrue("score is not updated", score != returnScore);
        }
    }

    private int getMyScore(String boardName) {
        int format = Leaderboard.FORMAT_VALUE;
        String boardId = Consts.INVALID_INT_STRING;

        Leaderboard board = getBoardFromList(boardName);
        if (board != null) {
            boardId = board.getId();
            format = board.getFormat();
        }

        Log.i(TAG, "Try to get leaderboard ranking and score...");
        Leaderboard.getScore(boardId, transSelector("ME"), transPeriod("TOTAL"),
                Consts.STARTINDEX_1, Consts.PAGESIZE_ALL, new ScoreListener() {
                    @Override
                    public void onSuccess(Score[] entry) {
                        Log.d(TAG, "Get leaderboard score success!");
                        // SDK updates and return empty entry instead of
                        // return failed when score is deleted
                        if (entry == null || entry.length == 0) {
                            getBlockRepo().put(SCORE, new Score());
                        } else {
                            getBlockRepo().put(SCORE, entry[0]);
                        }
                        notifyAsyncInStep();
                    }

                    @Override
                    public void onFailure(int responseCode, HeaderIterator headers, String response) {
                        Log.d(TAG, "No leaderboard score!");
                        getBlockRepo().put(SCORE, new Score());
                        notifyAsyncInStep();
                    }
                });
        return format;
    }

    @Then("my (\\w+) score ranking of leaderboard (.+) should be (\\d+)")
    public void verifyRankingByName(String period, final String boardName, int ranking) {
        ArrayList<Leaderboard> l = (ArrayList<Leaderboard>) getBlockRepo().get(LEADERBOARD_LIST);
        for (Leaderboard board : l) {
            if (boardName.equals(board.getName())) {
                Log.i(TAG, "Try to get leaderboard ranking and score...");
                Leaderboard.getScore(board.getId(), transSelector("EVERYONE"), transPeriod(period),
                        Consts.STARTINDEX_1, Consts.PAGESIZE_ALL, new ScoreListener() {
                            @Override
                            public void onSuccess(Score[] entry) {
                                Log.d(TAG, "Get leaderboard score success!");
                                if (entry == null || entry.length == 0)
                                    getBlockRepo().put(SCORE, new Score());
                                else
                                    getBlockRepo().put(SCORE, entry[0]);
                                notifyAsyncInStep();
                            }

                            @Override
                            public void onFailure(int responseCode, HeaderIterator headers,
                                    String response) {
                                Log.d(TAG, "No leaderboard score!");
                                getBlockRepo().put(SCORE, new Score());
                                notifyAsyncInStep();
                            }
                        });
                waitForAsyncInStep();
                // a small trick to match the step with ios
                if (ranking == 0)
                    ranking = -1;
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

    @When("I load the first page of leaderboard list with page size (.+)")
    public void getTheFirstPageOfLeaderboards(String pageSize) {
        getLeaderboard(Consts.STARTINDEX_1, Integer.valueOf(pageSize));
    }

    @When("I load icon of leaderboard (.+)")
    public void loadIcon(String boardName) {
        ArrayList<Leaderboard> l = (ArrayList<Leaderboard>) getBlockRepo().get(LEADERBOARD_LIST);
        for (Leaderboard board : l) {
            if (boardName.equals(board.getName())) {
                notifyStepWait();
                board.loadThumbnail(new IconDownloadListener() {
                    @Override
                    public void onSuccess(Bitmap image) {
                        Log.d(TAG, "load icon success!");
                        getBlockRepo().put(ICON, image);
                        notifyStepPass();
                    }

                    @Override
                    public void onFailure(int responseCode, HeaderIterator headers, String response) {
                        Log.e(TAG, "load icon failed!");
                        notifyStepPass();
                    }
                });
                return;
            }
        }
    }

    //TODO now all leaderboard have the same icon
    @Then("leaderboard icon of (.+) should be correct")
    public void verifyIcon(String boardName) {
        if (getBlockRepo().get(ICON) == null)
            fail("leaderboard icon is null!");
        Bitmap bitmap = (Bitmap) getBlockRepo().get(ICON);
        Bitmap expect_image = PopupStepDefinitions.zoomBitmap(BitmapFactory.decodeResource(
                GreePlatform.getContext().getResources(), R.drawable.leaderboard_icon), bitmap
                .getWidth(), bitmap.getHeight());
        double sRate = PopupStepDefinitions.compareImage(bitmap, expect_image);
        Log.d(TAG, "Similarity rate: " + sRate);
        Assert.assertTrue("leaderboard icon similarity is bigger than 80%", sRate > 80);
        // saveIconAsExpectedResult(Environment.getExternalStorageDirectory().getAbsolutePath(),
        // "leaderboard_icon.png");
    }

    // TODO for data preparation
    private void saveIconAsExpectedResult(String path, String icon_name) {
        try {
            Bitmap bitmap = (Bitmap) getBlockRepo().get(ICON);
            File icon = new File(path, icon_name);
            FileOutputStream fos = new FileOutputStream(icon);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
                Log.d(TAG, "Create expected icon for leaderboard success!");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
