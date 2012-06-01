
package com.openfeint.qa.ggp;

import java.util.Arrays;

import net.gree.asdk.api.auth.Authorizer;
import net.gree.asdk.api.auth.Authorizer.AuthorizeListener;
import util.RawFileUtil;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.openfeint.qa.core.caze.TestCase;
import com.openfeint.qa.core.caze.builder.CaseBuilder;
import com.openfeint.qa.core.caze.builder.CaseBuilderFactory;
import com.openfeint.qa.core.exception.CaseBuildFailedException;
import com.openfeint.qa.core.net.TCMCommunicator;
import com.openfeint.qa.core.runner.TestRunner;

public class DailyRunActivity extends Activity {

    private static final String TAG = "DailyRunActivity";

    private TestRunner runner;

    private RawFileUtil rfu;

    private TestCase[] case_list;

    private final String suite_id = "188";

    private final String run_id = "458";

    private boolean need_reload = false;

    // TODO for debug
    private Button start_button;

    private void loadCase() {
        CaseBuilder builder = CaseBuilderFactory.makeBuilder(CaseBuilderFactory.TCM_BUILDER,
                rfu.getTextFromRawResource(R.raw.tcm),
                rfu.getTextFromRawResource(R.raw.step_def, "step_path"), DailyRunActivity.this);

        try {
            runner.emptyCases();
            runner.addCases(builder.buildCases(suite_id));
            need_reload = false;
        } catch (CaseBuildFailedException cbfe) {
            Log.e(TAG, "Load test case failed!");
            need_reload = true;
        }
    }

    private Runnable run_case_thread = new Runnable() {
        public void run() {
            case_list = runner.getAllCases();
            // TODO debug message
            Log.d(TAG, "All test case loaded are below:");
            for (TestCase tc : case_list) {
                Log.i(TAG, "id: " + tc.getId() + ", name: " + tc.getTitle());
            }
            runner.runAllCases();
            Log.i(TAG, "---------- Running done ---------");
            submitResult();
        }
    };

    private void runAndSubmitCase() {
        new Thread(run_case_thread).start();
    }

    private void submitResult() {
        Log.i(TAG, "---------- Submitting result to TCM ---------");
        TCMCommunicator tcm = new TCMCommunicator(rfu.getTextFromRawResource(R.raw.tcm), "");
        tcm.setTestCasesResult(run_id, Arrays.asList(case_list));
        Log.i(TAG, "---------- result submitted ----------");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.main);
        runner = TestRunner.getInstance(DailyRunActivity.this);
        rfu = RawFileUtil.getInstance(DailyRunActivity.this);

        LoginGGP();
    }

    @Override
    public void onResume() {
        super.onResume();
        // initDebugButton();
        Log.d(TAG, "Begin the daily run....");
//        do {
//            loadCase();
//        } while (need_reload);
//
//        runAndSubmitCase();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "Saving state!");
    }

    AuthorizeListener listener = new AuthorizeListener() {
        public void onAuthorized() {
            Log.i(TAG, "Login Success!");
        }

        public void onCancel() {
            Log.i(TAG, "Login cancel!");
        }

        public void onError() {
            Log.e(TAG, "Login failed!");
        }
    };

    // Login for ggp
    private void LoginGGP() {
        if (!Authorizer.isAuthorized()) {
            Authorizer.authorize(this, listener);
        }
    }

    private void initDebugButton() {
        start_button = (Button) findViewById(R.id.start_selected);
        start_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Click to debug running...");
                loadCase();
                runAndSubmitCase();
            }
        });
    }
}
