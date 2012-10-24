package com.openfeint.qa.ggp.step_definitions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.gree.asdk.api.incentive.IncentiveController;
import net.gree.asdk.api.incentive.callback.IncentivePostListener;
import net.gree.vendor.com.google.gson.JsonObject;

import org.apache.http.HeaderIterator;
import org.json.JSONArray;
import org.json.JSONException;

import util.RawFileUtil;
import android.R;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class IncentiveStepDefinitions extends BasicStepDefinition {
	public static final String TAG = IncentiveStepDefinitions.class
			.getSimpleName();
	private static final String INCENTIVE_PAYLOAD = "incentive_payload";
	private static final String PAYLOAD_TYPE = "payload_type";

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
	public void postIncentive(String email) {
		notifyStepWait();
		List<String> users = new ArrayList<String>();
		users.add(emailToId(email));
		IncentiveController.post(users,
				((Integer) getBlockRepo().get(PAYLOAD_TYPE)).intValue(),
				(String) getBlockRepo().get(INCENTIVE_PAYLOAD),
				new IncentivePostListener() {

					@Override
					public void onSuccess(String response) {
						notifyStepPass();
					}

					@Override
					public void onFailure(int responseCode,
							HeaderIterator headers, String response) {
						notifyStepPass();
					}
				});
	}

	private String emailToId(String email) {
		// see MainActivity.java
		RawFileUtil rfu = null;
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
		try {
			String data = rfu.getTextFromRawResource(resource_id);
			org.json.JSONObject jSONObject = new org.json.JSONObject(data);
			JSONArray jsonArray = jSONObject.getJSONArray("credentials");
			for (int i = 0; i < jsonArray.length(); i++) {
				org.json.JSONObject jobj = jsonArray.getJSONObject(i);
				String userid = jobj.getString("userid");
				String username = jobj.getString("username");
				if (username == email)
					return userid;
			}
		} catch (JSONException e) {
			return null;
		}
		return null;
	}

	@Then("incentive target should be (.*)")
	public void verifyIncentive(int number) {
		// do nothing
	}

	@After("I mark incentive as processed")
	public void processIncentive() {
	}
}
