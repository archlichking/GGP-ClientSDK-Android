package com.openfeint.qa.ggp.step_definitions;

import java.util.ArrayList;
import java.util.HashSet;

import net.gree.asdk.api.ui.CloseMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class CloseMessageStepDefinitions extends BasicStepDefinition {
	public static final String TAG = CloseMessageStepDefinitions.class.getSimpleName();
	public static final String CLOSE_MESSAGE = "close_message";
	public static final String CALLBACKURL = "callbackurl";
	public static final String SERVICE = "service";
	public static final String RECIPIENT_USER_ID_1 = "recipient_user_id_1";
	public static final String RECIPIENT_USER_ID_2 = "recipient_user_id_2";
	
	
	@When("I set the CloseMessage data with callbackurl (.+) service (.+) and recipient_user_ids (\\d+) and (\\d+)")
	public void initCloseMessageData(String callbackurl, String service, int recipient_user_id_1, int recipient_user_id_2) {
		
		// Make CloseMessage
		String data = "{\"callback\":\"close\",\"data\":{\"callbackurl\":\"" + callbackurl + "\",\"service\":\""+ service + "\",\"recipient_user_ids\":[" + String.valueOf(recipient_user_id_1) + "," + String.valueOf(recipient_user_id_2) + "]}}";
		CloseMessage closeMessage = new CloseMessage();
		closeMessage.setData(data);
		
		// Store CloseMessage in Repo
		getBlockRepo().put(CLOSE_MESSAGE, closeMessage);
	}
	
	@Then("callbackurl of closemessage should be (.+)")
	@And("callbackurl of closemessage should be (.+)")
	public void verifyCallbackurl(String expected_callbackurl) throws NumberFormatException, JSONException {
		CloseMessage closeMessage = (CloseMessage) getBlockRepo().get(CLOSE_MESSAGE);
		String message = closeMessage.getData();
		String callbackurl = CloseMessage.getCallbackUrl(message);
		Assert.assertTrue("expected " + expected_callbackurl + " but received" + callbackurl, expected_callbackurl.equals(callbackurl));	
	}
		
	@Then("service of CloseMessage should be (.+)")
	@And("service of CloseMessage should be (.+)")
	public void verifyService(String expected_service) throws NumberFormatException, JSONException {
		CloseMessage closeMessage = (CloseMessage) getBlockRepo().get(CLOSE_MESSAGE);
		String message = closeMessage.getData();
		String service = CloseMessage.getService(message);
		Assert.assertTrue("expected " + expected_service + " but received" + service, service.equals(expected_service));	
	}
	
	@Then("recipient_user_ids of CloseMessage should contain (.+)")
	@And("recipient_user_ids of CloseMessage should contain (.+)")
	public void verifyRecipientUserId(int recipient_user_id) throws NumberFormatException, JSONException {
		CloseMessage closeMessage = (CloseMessage) getBlockRepo().get(CLOSE_MESSAGE);
		String message = closeMessage.getData();
		JSONArray recipient_user_ids = CloseMessage.getRecipientUserIds(message);
		ArrayList<Integer> recipient_user_ids_array = new ArrayList<Integer>();
		for (int i=0; i<recipient_user_ids.length(); i++) {
			int current_recipient_user_id = recipient_user_ids.getInt(i);
			recipient_user_ids_array.add(current_recipient_user_id);
		}
		Assert.assertTrue("expected " + recipient_user_id + " there", recipient_user_ids_array.contains(Integer.valueOf(recipient_user_id)));	
	}
}
