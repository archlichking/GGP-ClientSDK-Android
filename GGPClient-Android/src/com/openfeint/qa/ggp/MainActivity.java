package com.openfeint.qa.ggp;

import com.openfeint.qa.core.caze.TestCase;
import com.openfeint.qa.core.caze.TestCaseDelegate;
import com.openfeint.qa.core.caze.builder.CaseBuilder;
import com.openfeint.qa.core.caze.builder.CaseBuilderFactory;
import com.openfeint.qa.core.exception.CaseBuildFailedException;
import com.openfeint.qa.core.net.TCMCommunicator;
import com.openfeint.qa.core.runner.TestRunner;
import com.openfeint.qa.ggp.adapter.CaseWrapper;
import com.openfeint.qa.ggp.adapter.TestCasesAdapter;

import net.gree.asdk.api.auth.Authorizer;
import net.gree.asdk.api.auth.Authorizer.AuthorizeListener;
import util.RawFileUtil;

import android.app.Activity;
import android.app.ProgressDialog;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

	private Handler load_done_handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH_RESULT_LIST:
				displayResult(runner.getAllCases());
				break;
			case TOAST_DISPLAY:
				Toast.makeText(getBaseContext(), (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				break;
			case TIME_OUT_LIMITATION:
				Toast.makeText(getBaseContext(), "Loading time out!",
						Toast.LENGTH_SHORT).show();
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
			int lastTime = 0;
			while (is_under_progress) {
				if (lastTime++ > TIME_OUT_LIMITATION) {
					Log.w(TAG, "Loading time out!");
					load_done_handler.sendMessage(load_done_handler
							.obtainMessage(TIME_OUT));
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (!is_under_progress) {
				load_progress_dialog.dismiss();
				load_done_handler.sendMessage(load_done_handler
						.obtainMessage(REFRESH_RESULT_LIST));
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
			CaseBuilder builder = CaseBuilderFactory.makeBuilder(
					CaseBuilderFactory.TCM_BUILDER,
					rfu.getTextFromRawResource(R.raw.tcm),
					rfu.getTextFromRawResource(R.raw.step_def, "step_path"),
					MainActivity.this);

			try {
				runner.emptyCases();
				runner.addCases(builder.buildCases(suite_text.getText()
						.toString()));
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
			runner.runCasesByIds(adapter.getSelectedCaseIds(), /* optional */
					new TestCaseDelegate() {
						public void pushCaseResults(Collection<TestCase> cases) {
							// comment this line if you are debugging from
							// sample_case.txt

							// TODO: comments this out first;
							Log.i(TAG,
									"---------- Submitting result to TCM ---------");
							submitToTCM(cases);
							// Log.i(TAG,
							// "--------Test Finished!, NOT post to TCM ----");
						}

					});
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
							getResources().getString(R.string.no_suiteid_load),
							Toast.LENGTH_SHORT).show();
					return;
				}
				run_text = (EditText) findViewById(R.id.text_run_id);
				if ("".equals(run_text.getText().toString())) {
					Toast.makeText(getBaseContext(),
							getResources().getString(R.string.no_runid_load),
							Toast.LENGTH_SHORT).show();
					return;
				}
				// --- end check
				// --- start make progress dialog work
				initProgressDialog(getResources()
						.getString(R.string.load_cases));

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
							getResources().getString(R.string.no_case_load),
							Toast.LENGTH_SHORT).show();
					return;
				}
				initProgressDialog(getResources().getString(
						R.string.execute_cases));

				new Thread(progressbar_thread).start();
				new Thread(submit_case_thread).start();
			}
		});

	}

	private void initResultList() {
		result_list = (ListView) findViewById(R.id.result_list);
	}

	@SuppressWarnings("unused")
	private void submitToTCM(Collection<TestCase> tcs) {
		TCMCommunicator tcm = new TCMCommunicator(
				rfu.getTextFromRawResource(R.raw.tcm), "");
		tcm.setTestCasesResult(run_text.getText().toString(),
				tcs.toArray(new TestCase[tcs.size()]));
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
		LoginGGP();
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
}
