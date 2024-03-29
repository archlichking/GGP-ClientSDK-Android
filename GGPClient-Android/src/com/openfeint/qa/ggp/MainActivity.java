
package com.openfeint.qa.ggp;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.ui.StatusBar;
import util.RawFileUtil;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.openfeint.qa.core.caze.TestCase;
import com.openfeint.qa.core.caze.builder.CaseBuilder;
import com.openfeint.qa.core.caze.builder.CaseBuilderFactory;
import com.openfeint.qa.core.exception.CaseBuildFailedException;
import com.openfeint.qa.core.exception.TCMIsnotReachableException;
import com.openfeint.qa.core.net.PlainHttpCommunicator;
import com.openfeint.qa.core.net.TCMCommunicator;
import com.openfeint.qa.core.runner.TestRunner;
import com.openfeint.qa.core.util.CredentialStorage;
import com.openfeint.qa.core.util.JsonUtil;
import com.openfeint.qa.ggp.adapter.CaseWrapper;
import com.openfeint.qa.ggp.adapter.TestCasesAdapter;

public class MainActivity extends Activity {
    private Button start_button;

    private Button load_button;

    private EditText suite_text;

    private EditText run_text;

    private ListView result_list;

    private ProgressDialog load_progress_dialog;

    private boolean is_under_progress = false;

    private static final int REFRESH_RESULT_LIST = 0;

    private static final int TOAST_DISPLAY = 1;

    private static final String TAG = "MainActivity";

    private TestCasesAdapter adapter;

    private static MainActivity mainActivity;

    public static Bitmap dialog_bitmap;

    public static boolean is_dialog_opened;

    public static boolean is_dialog_closed;

    private String originalId;

    private Handler load_done_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_RESULT_LIST:
                    displayResult(runner.getAllCases());
                    break;
                case TOAST_DISPLAY:
                    Toast.makeText(getBaseContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    private TestRunner runner;

    private RawFileUtil rfu;

    private Runnable progressbar_thread = new Runnable() {
        public void run() {
            Looper.prepare();
            while (is_under_progress) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!is_under_progress) {
                load_progress_dialog.dismiss();
                load_done_handler.sendMessage(load_done_handler.obtainMessage(REFRESH_RESULT_LIST));
            }
        }
    };

    private Runnable loading_case_thread = new Runnable() {
        public void run() {

            // change to CaseBuilderFactory.FILE_BUILDER to load from
            // res.raw/sample_case.txt

            // or use TCM_BUILDER
            // rfu.getTextFromRawResource(R.raw.tcm)
            // to use TCM
            CaseBuilder builder = CaseBuilderFactory.makeBuilder(CaseBuilderFactory.TCM_BUILDER,
                    rfu.getTextFromRawResource(R.raw.tcm),
                    rfu.getTextFromRawResource(R.raw.step_def, "step_path"), MainActivity.this);

            try {
                runner.emptyCases();
                runner.addCases(builder.buildCases(suite_text.getText().toString()));
            } catch (CaseBuildFailedException cbfe) {
                Message msg = load_done_handler.obtainMessage(TOAST_DISPLAY);
                msg.obj = cbfe.getMessage();
                load_done_handler.sendMessage(msg);
            } finally {
                is_under_progress = false;
            }
        }
    };

    private Runnable submit_case_thread = new Runnable() {
        public void run() {
            runner.runCasesByIds(adapter.getSelectedCaseIds());
            /* optional */

            Log.i(TAG, "---------- Submitting result to TCM ---------");
            TCMCommunicator tcm = new TCMCommunicator(rfu.getTextFromRawResource(R.raw.tcm), "");
            tcm.setTestCasesResult(run_text.getText().toString(), adapter.getSelectedCases());
            Log.i(TAG, "---------- result submitted ----");

            is_under_progress = false;
        }
    };

    private void initLoadCaseButton() {
        load_button = (Button) findViewById(R.id.load_button);
        load_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // --- start do some check here
                if ("".equals(suite_text.getText().toString())) {
                    Toast.makeText(getBaseContext(),
                            getResources().getString(R.string.no_suiteid_load), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if ("".equals(run_text.getText().toString())) {
                    Toast.makeText(getBaseContext(),
                            getResources().getString(R.string.no_runid_load), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                // --- end check
                // --- start make progress dialog work
                initProgressDialog(getResources().getString(R.string.load_cases));

                new Thread(loading_case_thread).start();
                new Thread(progressbar_thread).start();
            }
        });
    }

    private List<CaseWrapper> getData(TestCase[] tcs) {
        List<CaseWrapper> data = new ArrayList<CaseWrapper>();
        for (TestCase tc : tcs) {
            data.add(new CaseWrapper(tc, false));
        }
        Collections.sort(data);
        return data;

    }

    private void displayResult(TestCase[] tcs) {

        adapter = new TestCasesAdapter(MainActivity.this, getData(tcs));
        result_list.setAdapter(adapter);

        CheckBox select_all = (CheckBox) findViewById(R.id.use_all);
        select_all.setVisibility(View.VISIBLE);
        select_all.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                adapter.ToggleSelectAll(arg1);
            }

        });

        CheckBox select_failed = (CheckBox) findViewById(R.id.use_failed);
        select_failed.setVisibility(View.VISIBLE);
        select_failed.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ArrayList<String> failedIds = adapter.ToggleSelectFailed(isChecked);
                    StringBuffer ids = new StringBuffer();
                    for (String id : failedIds) {
                        ids.append(id);
                        if (failedIds.indexOf(id) != failedIds.size() - 1) {
                            ids.append(", ");
                        }
                    }
                    Toast.makeText(getBaseContext(), failedIds.size() + " failed cases: " + ids,
                            Toast.LENGTH_SHORT).show();
                } else {
                    adapter.ToggleSelectFailed(isChecked);
                }
            }
        });
    }

    private void initProgressDialog(String msg) {
        load_progress_dialog = new ProgressDialog(MainActivity.this);
        load_progress_dialog.setCancelable(false);
        load_progress_dialog.setMessage(msg);
        load_progress_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        load_progress_dialog.show();
        is_under_progress = true;
    }

    private void initRunCaseButton() {
        start_button = (Button) findViewById(R.id.start_selected);
        start_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!runner.hasCase()) {
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.no_case_load), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                initProgressDialog(getResources().getString(R.string.execute_cases));

                new Thread(progressbar_thread).start();
                new Thread(submit_case_thread).start();
            }
        });

    }

    private void initResultList() {
        result_list = (ListView) findViewById(R.id.result_list);
    }

    private void initTextFileds() {
        run_text = (EditText) findViewById(R.id.text_run_id);
        run_text.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    originalId = run_text.getText().toString();
                    run_text.setText("");
                } else {
                    if (run_text.getText().length() == 0) {
                        run_text.setText(originalId);
                        originalId = "";
                    }
                }
            }
        });

        suite_text = (EditText) findViewById(R.id.text_suite_id);
        suite_text.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    originalId = suite_text.getText().toString();
                    suite_text.setText("");
                } else {
                    if (suite_text.getText().length() == 0) {
                        suite_text.setText(originalId);
                        originalId = "";
                    }
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        runner = TestRunner.getInstance(MainActivity.this);
        rfu = RawFileUtil.getInstance(MainActivity.this);

        initTextFileds();
        initLoadCaseButton();
        initRunCaseButton();
        initResultList();
        loadCredentialJson();
        mainActivity = MainActivity.this;
        GreePlatform.activityOnCreate(this, false);
        hiddenStatusBar();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "Saving state!");
    }

    // Test coffee api
    private void getConfig() {
        PlainHttpCommunicator http = new PlainHttpCommunicator(null, null);
        try {
            Log.d(TAG, "==================== Load Configuration ====================");
            BufferedReader br = http
                    .getJsonResponse("http://10.64.17.40:3000/android/config?key=adfqet87983hiu783flkad09806g98adgk");
            if (br != null) {
                String val;
                Log.e(TAG, "print out all json data:");
                try {
                    while ((val = br.readLine()) != null) {
                        Log.e(TAG, val);
                    }
                    br.reset();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String mark = JsonUtil.getAutoConfigJsonValueByKey("is_create_run", br);
                Log.d(TAG, "is_create_run: " + mark);

                String suite_id = JsonUtil.getAutoConfigJsonValueByKey("suite_id", br);
                Log.d(TAG, "suite_id: " + suite_id);

                String run_id = JsonUtil.getAutoConfigJsonValueByKey("run_id", br);
                Log.d(TAG, "run_id: " + run_id);

            }
        } catch (TCMIsnotReachableException e) {
            e.printStackTrace();
        } finally {
        }
    }

    private void loadCredentialJson() {
        String app_id = "15265";
        String field_name = "credentials_config_" + app_id;
        int resource_id = 0;
        try {
            Field field = R.raw.class.getDeclaredField(field_name);
            resource_id = (Integer) field.get(null);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        String data = rfu.getTextFromRawResource(resource_id);
        CredentialStorage.initCredentialStorageWithAppId(app_id, data);
    };

    public static MainActivity getInstance() {
        return mainActivity;
    }

    // Add menu button
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "QUIT");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case Menu.FIRST + 1:
                Log.d(TAG, "exiting app...");
                System.exit(0);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void hiddenStatusBar() {
        StatusBar bar_expandable = (StatusBar) findViewById(R.id.statusBarExpandable);
        bar_expandable.setVisibility(View.GONE);
        StatusBar bar_normal = (StatusBar) findViewById(R.id.statusBarNormal);
        bar_normal.setVisibility(View.GONE);
    }
}
