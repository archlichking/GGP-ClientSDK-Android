package com.openfeint.qa.ggp.step_definitions;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;

import net.gree.asdk.api.ui.CloseMessage;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.ggp.GreeWebViewActivity;

public class CloseMessageStepDefinitions extends BasicStepDefinition {
	public static final String TAG = CloseMessageStepDefinitions.class.getSimpleName();
	public static final String CLOSE_MESSAGE = "close_message";
	public static final String CALLBACKURL = "callbackurl";
	public static final String SERVICE = "service";
	public static final String RECIPIENT_USER_ID_1 = "recipient_user_id_1";
	public static final String RECIPIENT_USER_ID_2 = "recipient_user_id_2";
	
	
	@When("I set the CloseMessage data with callbackurl (.+) service (.+) and recipient_user_ids (\\d+) and (\\d+)")
	public void initCloseMessageData(String callbackurl, String service, int recipient_user_id_1, int recipient_user_id_2) {
		
		// Store test data for later comparison
		getBlockRepo().put(CALLBACKURL, callbackurl);
		getBlockRepo().put(SERVICE, service);
		getBlockRepo().put(RECIPIENT_USER_ID_1, recipient_user_id_1);
		getBlockRepo().put(RECIPIENT_USER_ID_2, recipient_user_id_2);
		
		// Make CloseMessage
		String data = "{\"callback\":\"close\",\"data\":{\"callbackurl\":\"" + callbackurl + "\",\"service\":\""+ service + "\",\"recipient_user_ids\":[" + String.valueOf(recipient_user_id_1) + "," + String.valueOf(recipient_user_id_2) + "]}}";
		CloseMessage closeMessage = new CloseMessage();
		closeMessage.setData(data);
		
		// Store CloseMessage in Repo
		getBlockRepo().put(CLOSE_MESSAGE, closeMessage);
	}
	
	@Then("I verify the CloseMessage data")
	public void verifyCloseMessageData() throws NumberFormatException, JSONException {
		// Retrieve CloseMessage from Repo
		CloseMessage closeMessage = (CloseMessage) getBlockRepo().get(CLOSE_MESSAGE);
		String message = closeMessage.getData();
		String callbackurl = closeMessage.getCallbackUrl(message);
		String service = closeMessage.getService(message);
		JSONArray recipient_user_ids = closeMessage.getRecipientUserIds(message);
		int recipient_user_id_1 = recipient_user_ids.getInt(0);
		int recipient_user_id_2 = recipient_user_ids.getInt(1);
		
		// Assertion
		Assert.assertTrue("expected getBlockRepo().get(CALLBACKURL) but received" + callbackurl, getBlockRepo().get(CALLBACKURL).equals(callbackurl));
		Assert.assertTrue("expected getBlockRepo().get(SERVICE) but received" + callbackurl, getBlockRepo().get(SERVICE).equals(service));
		Assert.assertTrue("expected getBlockRepo().get(RECIPIENT_USER_ID_1) but received" + callbackurl, getBlockRepo().get(RECIPIENT_USER_ID_1).equals(recipient_user_id_1));
		Assert.assertTrue("expected getBlockRepo().get(RECIPIENT_USER_ID_2) but received" + callbackurl, getBlockRepo().get(RECIPIENT_USER_ID_2).equals(recipient_user_id_2));
	}
}
