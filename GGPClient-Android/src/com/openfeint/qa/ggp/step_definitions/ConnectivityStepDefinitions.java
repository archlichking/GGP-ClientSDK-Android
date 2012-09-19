package com.openfeint.qa.ggp.step_definitions;


import java.lang.reflect.InvocationTargetException;

import java.lang.reflect.Method;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.core.GConnectivityManager;
import net.gree.asdk.core.GConnectivityManager.ConnectivityListener;
import net.gree.asdk.core.Injector;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.ggp.MainActivity;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;


public class ConnectivityStepDefinitions extends BasicStepDefinition {
	

	private static final String TAG = "Connectivity_Steps";
	private static final String NOTIFY = "false";
	private static final String LISTENER = "listener";
	private static ConnectivityListener connection;
	
	private GConnectivityManager connectivityManager = Injector.getInstance(GConnectivityManager.class);
	  
	  
	@When("I register a listener")
	@And("I register a listener")
	public void registerListener() {
		getBlockRepo().remove(NOTIFY);
		
		connection = new GConnectivityManager.ConnectivityListener() {
			
			@Override
			public void onConnectivityChanged(boolean isConnected) {
				System.out.println(isConnected);
				if (isConnected){
					Log.d(TAG, "listener notified");
					getBlockRepo().put(NOTIFY, "true");
				}
			}
			
		};
	
		connectivityManager.registerListener(connection);
		System.out.println(connection);
		getBlockRepo().put(LISTENER, connection);
		Log.d(TAG, "listener registered");
		getBlockRepo().put(NOTIFY, "false");
	}
	
	@When("I unregister a listener")
	@And("I unregister a listener")
	public void unregisterListener() {
	
		connectivityManager.unregisterListener(connection);
		getBlockRepo().put(NOTIFY, "false");
		Log.d(TAG, "listener unregistered");
	}
	
	@When("I set device connectivity to (.+)")
	@And("I set device connectivity to (.+)")
	public void setConnectivity(boolean state) {
	
		try {
			Method m = GConnectivityManager.class.getDeclaredMethod("setCurrentConnectivity", boolean.class);
			m.setAccessible(true);
			m.invoke(connectivityManager, state);

		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
	}
	
	@When("I check the connectivity")
	@And("I check the connectivity")
	public void checkConnectivity() {
		
		getBlockRepo().put(NOTIFY, "false");
			connectivityManager.checkAndNotifyConnectivity();
	}
	
	@Then("the listener should be notified as (.+)")
	public void verifyListener(String state) {
		
		if (getBlockRepo().get(NOTIFY).equals(state)) {
			Log.d(TAG, "listener verified");
			notifyStepPass();
		} else {
			fail("listener not verified");
		}
	}
}
