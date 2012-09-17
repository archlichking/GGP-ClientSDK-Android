
package com.openfeint.qa.ggp;

import java.io.BufferedReader;
import java.util.Arrays;

import net.gree.asdk.api.auth.Authorizer;
import net.gree.asdk.api.auth.Authorizer.AuthorizeListener;
import util.RawFileUtil;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.openfeint.qa.core.caze.TestCase;
import com.openfeint.qa.core.caze.builder.CaseBuilder;
import com.openfeint.qa.core.caze.builder.CaseBuilderFactory;
import com.openfeint.qa.core.exception.CaseBuildFailedException;
import com.openfeint.qa.core.exception.TCMIsnotReachableException;
import com.openfeint.qa.core.net.PlainHttpCommunicator;
import com.openfeint.qa.core.net.TCMCommunicator;
import com.openfeint.qa.core.runner.TestRunner;
import com.openfeint.qa.core.util.JsonUtil;

public class DailyRunActivity extends Activity {

    private static final String TAG = "DailyRunActivity";

    private TestRunner runner;

    private RawFileUtil rfu;

    private TestCase[] case_list;

    private boolean is_create_run;

    private String suite_id;

    private String run_id;

    private String run_desc;

    private boolean need_reload = false;

    // TODO for debug
    private Button start_button;

    private void loadCase() {
        Log.d(TAG, "==================== Load Cases From TCMS ====================");
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
            Log.d(TAG, "==================== Begin to Run ====================");
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
    }

    @Override
    public void onResume() {
        super.onResume();
        // initDebugButton();

        do {
            getConfig(); // Get Configuration for this run
        } while (need_reload);

        do {
            loadCase(); // Load test case from TCMS
        } while (need_reload);

        runAndSubmitCase(); // Run test cases loaded and submit result
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

    private void getConfig() {
        PlainHttpCommunicator http = new PlainHttpCommunicator(null, null);
        try {
            Log.d(TAG, "==================== Load Configuration ====================");
            BufferedReader br = http.getJsonResponse("http://10.64.17.51:3000/android/config?key=adfqet87983hiu783flkad09806g98adgk");
            if (br != null) {

                String mark = JsonUtil.getAutoConfigJsonValueByKey("is_create_run", br);
                Log.d(TAG, "is_create_run: " + mark);
                if ("true".equals(mark))
                    is_create_run = true;
                else
                    is_create_run = false;

                suite_id = JsonUtil.getAutoConfigJsonValueByKey("suite_id", br);
                Log.d(TAG, "suite_id: " + suite_id);

                run_id = JsonUtil.getAutoConfigJsonValueByKey("run_id", br);
                Log.d(TAG, "run_id: " + run_id);

                need_reload = false;
            }
        } catch (TCMIsnotReachableException e) {
            need_reload = true;
            e.printStackTrace();
        } finally {
        }
    }
}
