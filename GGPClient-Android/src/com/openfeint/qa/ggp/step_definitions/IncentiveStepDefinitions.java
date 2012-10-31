package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.fail;
import static junit.framework.Assert.assertEquals;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.gree.asdk.api.incentive.IncentiveController;
import net.gree.asdk.api.incentive.callback.IncentiveGetListener;
import net.gree.asdk.api.incentive.callback.IncentivePostListener;
import net.gree.asdk.api.incentive.callback.IncentivePutListener;
import net.gree.asdk.api.incentive.model.IncentiveEvent;
import net.gree.asdk.api.incentive.model.IncentiveEventArray;
import net.gree.vendor.com.google.gson.JsonObject;

import org.apache.http.HeaderIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import util.RawFileUtil;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.core.util.CredentialStorage;
import com.openfeint.qa.ggp.MainActivity;
import com.openfeint.qa.ggp.R;

public class IncentiveStepDefinitions extends BasicStepDefinition {
	public static final String TAG = IncentiveStepDefinitions.class
			.getSimpleName();
	private static final String INCENTIVE_PAYLOAD = "incentive_payload";
	private static final String PAYLOAD_TYPE = "payload_type";
	private static final String ENTRIES = "entries";
	private static final String INCENTIVE_EVENTS = "incentive_events";
	private static final String EVENT_IDS = "event_ids";

	@And("I initialize incentive with incentive type (.*) and message (.*)")
	public void initializeIncentive(String type, String message) {
		
		int payloadType;
		if ("request".equals(type)) {
			payloadType = IncentiveController.PAYLOAD_TYPE_REQUEST;
		} else if ("invite".equals(type)) {
			payloadType = IncentiveController.PAYLOAD_TYPE_INVITE;
		} else {
			payloadType = IncentiveController.PAYLOAD_TYPE_REQUEST;
			Log.d(TAG,
					type
							+ "is an unknow request type.  Please selecte either request or invite.");
		}
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("message", message);
		String json = jsonObject.toString();
		
		getBlockRepo().put(PAYLOAD_TYPE, payloadType);
		getBlockRepo().put(INCENTIVE_PAYLOAD, json);
	}

	@When("I send incentive to user of email (.*)")
	public void postIncentive(String concatenatedEmails) {
		String[] emails = concatenatedEmails.split(",");
		List<String> users = new ArrayList<String>();
		CredentialStorage credentialStorage = CredentialStorage.getInstance();
		HashMap<String, String> pair = credentialStorage.getEmailIdPairs();
		for (String email : emails) {
			String userId = pair.get(email);			
			users.add(userId);
			Log.d(TAG, "send incentive to " + email + " whose user id is " + userId);
		}
		getBlockRepo().remove(ENTRIES);
		notifyStepWait();
		IncentiveController.post(users,
				((Integer) getBlockRepo().get(PAYLOAD_TYPE)).intValue(),
				(String) getBlockRepo().get(INCENTIVE_PAYLOAD),
				new IncentivePostListener() {

					@Override
					public void onSuccess(String response) {
						Log.d(TAG, "send incentive success");
						try {
							int number = parseNumberOfInsertedRows(response);
							getBlockRepo().put(ENTRIES, number);
						} catch (JSONException e) {
							fail("The server does not return the number of inserted rows: "
									+ response);
							e.printStackTrace();
						}
						notifyStepPass();
					}

					@Override
					public void onFailure(int responseCode,
							HeaderIterator headers, String response) {
						Log.d(TAG, "send incentive failed");
						notifyStepPass();
					}
				});
	}

	private int parseNumberOfInsertedRows(String json) throws JSONException {
		org.json.JSONObject jsonObject = new JSONObject(json);
		int number = jsonObject.getInt("entry");
		return number;
	}

	@Then("incentive target should be (.*)")
	public void verifyIncentive(int number) {
		int entries = (Integer) getBlockRepo().get(
				ENTRIES);
		assertEquals("server returns "+ number, number, entries);
	}

	@After("I mark incentive as processed")
	@And("I mark incentive as processed")
	public void processIncentive() {
		notifyStepWait();
		getIncentive();
		List<String> ids = (List<String>) getBlockRepo().get(EVENT_IDS);

		IncentiveController.put(ids, new IncentivePutListener() {

			@Override
			public void onSuccess(IncentiveEventArray result) {
				Log.d(TAG, result.toString());
				notifyStepPass();
			}

			@Override
			public void onFailure(int responseCode, HeaderIterator headers,
					String response) {
				fail("failed to mark incentive as processed");
				notifyStepPass();
			}
		});
	}

	private void getIncentive() {
		getBlockRepo().remove(INCENTIVE_EVENTS);
		HashMap<String, String> params = new HashMap<String, String>();
//		params.put(IncentiveController.KEY_COUNT, "100");
		IncentiveController.get(false, params, new IncentiveGetListener() {
			
			@Override
			public void onSuccess(IncentiveEventArray result) {
				int totalResult = result.getTotalResults();
				IncentiveEvent[] incentiveEvents = result.getEntry();
				getBlockRepo().put(INCENTIVE_EVENTS, incentiveEvents);
				int len = incentiveEvents.length;
				Log.d(TAG, "total" + totalResult);
				Log.d(TAG, "len" + len);
				List<String> ids = new ArrayList<String>();
				for (int i = 0; i < len; i++) {
					Log.d(TAG, incentiveEvents[i].toString());
					ids.add(incentiveEvents[i].getId());
				}
				getBlockRepo().put(EVENT_IDS, ids);
				notifyAsyncInStep();
			}

			@Override
			public void onFailure(int responseCode, HeaderIterator headers,
					String response) {
				fail("failed to get incentive events");
				notifyAsyncInStep();
			}
		});
		waitForAsyncInStep();
	}
}
