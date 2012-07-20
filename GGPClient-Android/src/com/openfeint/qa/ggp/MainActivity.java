
package com.openfeint.qa.ggp;

import java.io.BufferedReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import net.gree.asdk.api.auth.Authorizer;
import net.gree.asdk.api.auth.Authorizer.AuthorizeListener;
import net.gree.asdk.api.ui.RequestDialog;
import net.gree.asdk.api.wallet.Payment;
import net.gree.asdk.api.wallet.Payment.PaymentListener;
import net.gree.asdk.core.ui.PopupDialog;

import org.apache.http.HeaderIterator;

import util.RawFileUtil;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.openfeint.qa.core.runner.TestRunner;
import com.openfeint.qa.core.util.CredentialStorage;
import com.openfeint.qa.core.util.JsonUtil;
import com.openfeint.qa.ggp.adapter.CaseWrapper;
import com.openfeint.qa.ggp.adapter.TestCasesAdapter;
import com.openfeint.qa.ggp.step_definitions.PopupStepDefinitions;

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

    private static final int TIME_OUT = 2;

    private static final String TAG = "MainActivity";

    private TestCasesAdapter adapter;

    private static final int TIME_OUT_LIMITATION = 30000;

    private static MainActivity mainActivity;

    private static PopupDialog popupDialog;

    public static Bitmap dialog_bitmap;

    public static boolean is_dialog_opened;

    public static boolean is_dialog_closed;

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
                // case TIME_OUT_LIMITATION:
                // Toast.makeText(getBaseContext(), "Loading time out!",
                // Toast.LENGTH_SHORT)
                // .show();
                // break;
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
            int lastTime = 0;
            while (is_under_progress) {
                if (lastTime++ > TIME_OUT_LIMITATION) {
                    Log.w(TAG, "Loading time out!");
                    load_done_handler.sendMessage(load_done_handler.obtainMessage(TIME_OUT));
                }
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
            // TCMCommunicator tcm = new
            // TCMCommunicator(rfu.getTextFromRawResource(R.raw.tcm), "");
            // tcm.setTestCasesResult(run_text.getText().toString(),
            // adapter.getSelectedCases());
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
                suite_text = (EditText) findViewById(R.id.text_suite_id);
                if ("".equals(suite_text.getText().toString())) {
                    Toast.makeText(getBaseContext(),
                            getResources().getString(R.string.no_suiteid_load), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                run_text = (EditText) findViewById(R.id.text_run_id);
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
                adapter.ToggleSelectFailed(isChecked);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        runner = TestRunner.getInstance(MainActivity.this);
        rfu = RawFileUtil.getInstance(MainActivity.this);

        initLoadCaseButton();
        initRunCaseButton();
        initResultList();
        loadCredentialJson();
        mainActivity = MainActivity.this;

        // testScreenshot();
        // LoginGGP();
        // For debug
        // testJsonConfig();
    }

    @Override
    public void onResume() {
        super.onResume();
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

    // Test coffe api
    private void testJsonConfig() {

        PlainHttpCommunicator http = new PlainHttpCommunicator(null, null);
        try {
            BufferedReader br = http.getJsonResponse("http://10.64.20.98:3000/config");
            if (br != null) {
                String is_Create_Run = JsonUtil.getAutoConfigJsonValueByKey("is_create_run", br);
                Log.d(TAG, "is_create_run: " + is_Create_Run);
                String suite_id = JsonUtil.getAutoConfigJsonValueByKey("suite_id", br);
                Log.d(TAG, "suite_id: " + suite_id);
                String run_id = JsonUtil.getAutoConfigJsonValueByKey("run_id", br);
                Log.d(TAG, "run_id: " + run_id);
                String desc = JsonUtil.getAutoConfigJsonValueByKey("description", br);
                Log.d(TAG, "description: " + desc);
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
        // Log.e(TAG, "Json content: \n" + data);
        CredentialStorage.initCredentialStorageWithAppId(app_id, data);
    };

    public Handler popup_handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case PopupStepDefinitions.POPUP_REQUEST:
                    Log.d(TAG, "Trying to open request dialog...");
                    is_dialog_opened = false;
                    is_dialog_closed = false;
                    Handler handler = new Handler() {
                        public void handleMessage(Message message) {
                            switch (message.what) {
                                case RequestDialog.OPENED:
                                    Log.i(TAG, "Reqest Dialog opened.");
                                    is_dialog_opened = true;
                                    break;
                                case RequestDialog.CLOSED:
                                    Log.i(TAG, "Request Dialog closed.");
                                    is_dialog_closed = true;
                                    break;
                                default:
                            }
                        }
                    };
                    RequestDialog requestDialog = new RequestDialog(MainActivity.this);
                    requestDialog.setParams((TreeMap<String, Object>) message.obj);
                    requestDialog.setHandler(handler);
                    requestDialog.show();
                    popupDialog = requestDialog;
                    break;
                case PopupStepDefinitions.POPUP_PAYMENT:
                    Log.d(TAG, "Trying to open payment dialog...");
                    is_dialog_opened = false;
                    is_dialog_closed = false;
                    final Payment payment = (Payment) message.obj;
                    payment.setHandler(new Handler() {
                        public void handleMessage(Message message) {
                            switch (message.what) {
                                case Payment.OPENED:
                                    Log.d("Payment", "PaymentDialog opened.");
                                    is_dialog_opened = true;
                                    try {
                                        Field dialog_field = Payment.class
                                                .getDeclaredField("mPaymentDialog");
                                        dialog_field.setAccessible(true);
                                        int count = 0;
                                        while (dialog_field.get(payment) == null && count < 10) {
                                            Thread.sleep(1000);
                                            count++;
                                        }
                                        popupDialog = (PopupDialog) dialog_field.get(payment);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case Payment.CANCELLED:
                                    Log.d("Payment", "PaymentDialog canceled.");
                                case Payment.ABORTED:
                                    Log.d("Payment", "PaymentDialog closed.");
                                    is_dialog_closed = true;
                                    break;
                            }
                        }
                    });
                    payment.request(MainActivity.this, new PaymentListener() {
                        @Override
                        public void onSuccess(int responseCode, HeaderIterator headers,
                                String paymentId) {
                            Log.d(TAG, "payment.request() succeeded.");
                        }

                        @Override
                        public void onFailure(int responseCode, HeaderIterator headers,
                                String paymentId, String response) {
                            Log.d(TAG, "payment.request() failed.");
                        }

                        @Override
                        public void onCancel(int responseCode, HeaderIterator headers,
                                String paymentId) {
                            Log.d(TAG, "payment.request() canceled.");
                        }
                    });

                default:
            }
        }
    };

    public static MainActivity getInstance() {
        return mainActivity;
    }

    public PopupDialog getPopupDialog() {
        return popupDialog;
    }
}
