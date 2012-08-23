package com.openfeint.qa.ggp.step_definitions;

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

	GreeWebViewActivity activity = GreeWebViewActivity.getInstance();
	Context context = activity.getApplicationContext();
	
	Intent intent = new Intent(context, GreeWebViewActivity.class);
	
	@When("I set the CloseMessage data")
	public void initCloseMessageData() {
		CloseMessage closeMessage = (CloseMessage) intent.getSerializableExtra(CloseMessage.DATA);
		Log.d(TAG, closeMessage.toString());
//		getBlockRepo().put(CLOSE_MESSAGE, closeMessage);
	}
	
	@Then("I verify the CloseMessage data")
	public void verifyCloseMessageData() {
		CloseMessage closeMessage = (CloseMessage) getBlockRepo().get(CLOSE_MESSAGE);
		Log.d(TAG, closeMessage.getData());
		Log.d(TAG, closeMessage.getCallbackUrl("test"));
		Log.d(TAG, closeMessage.getService("test"));
		Log.d(TAG, closeMessage.getRecipientUserIds("test").toString());
	}
}
