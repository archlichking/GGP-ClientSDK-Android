package com.openfeint.qa.ggp.step_definitions;

import net.gree.asdk.api.ui.Dashboard;
import android.app.Activity;
import android.content.Context;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.ggp.MainActivity;

public class DashboardStepDefinitions extends BasicStepDefinition {

	@When("I launch the Dashboard")
	public void luanchDashboard() {
		Activity activity = MainActivity.getInstance();
		Context context = activity.getApplicationContext();
		Dashboard.launch(context);
	}
}
