package com.openfeint.qa.ggp.step_definitions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import net.gree.asdk.api.ScreenShot;
import net.gree.asdk.api.ui.Dashboard;
import net.gree.asdk.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.ggp.MainActivity;

public class DashboardStepDefinitions extends BasicStepDefinition {

	private static final String TAG = DashboardStepDefinitions.class.getSimpleName();

	@When("I launch the Dashboard")
	public void luanchDashboard() {
		Activity activity = MainActivity.getInstance();
		Dashboard.launch(activity);
		captureAndSave();
	}
	@And("I take the screenshot")
	public void captureAndSave() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// this does not work, either, possibly looking at the view in MainActivity only
		Activity activity = MainActivity.getInstance();
		WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay();
		View view = activity.findViewById(R.id.gree_u_menu); 
		if (null != view) {
			Log.d(TAG, "found it");
		}
		View view2 = activity.getWindow().getCurrentFocus(); //This does not work
		Bitmap bitmap = ScreenShot.capture(view2);
		saveScreenshoAtPath(Environment.getExternalStorageDirectory().getAbsolutePath(), "bitmap1.png", bitmap);
	}
	
	public void saveScreenshoAtPath(String path, String bitmap_name, Bitmap bitmap) {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			try {
				File bitmap_file = new File(path, bitmap_name);
				FileOutputStream fos = new FileOutputStream(bitmap_file);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			}
			catch (FileNotFoundException e) {
				Log.d(TAG, e.getStackTrace().toString());
			}
		}
	}
}
