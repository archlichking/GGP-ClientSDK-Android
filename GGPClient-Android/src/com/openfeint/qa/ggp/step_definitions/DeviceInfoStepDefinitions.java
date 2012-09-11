
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HeaderIterator;

import net.gree.asdk.api.alarm.ScheduledNotification;
import net.gree.asdk.api.alarm.ScheduledNotificationListener;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.asdk.core.util.DeviceInfo;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class DeviceInfoStepDefinitions extends BasicStepDefinition {
    private static final String TAG = DeviceInfoStepDefinitions.class.getSimpleName();
    private static final String UDID = "udid";
    private static final String MAC_ADDRESS = "mac_address";
    private static final String UUID = "uuid";
    private static final String IS_ROOTED = "is_rooted";
    private static final String IS_SENDABLEANDROIDID = "is_sendableandroidid";
    private static final String IS_SENDABLEMACADDRESS = "is_sendablemacaddress";
    
    @When("I get the device udid")
    @And("I get the device udid")
    public void getUdid() {
    	String udid = DeviceInfo.getUdid();
    	Log.d(TAG, udid);
    	getBlockRepo().put(UDID, udid);
    }
    
    @Then("the device udid should starts with android-id-")
    @And("the device udid should starts with android-id-")
    public void verifyUdid() {
    	String udid;
    	udid = (String) getBlockRepo().get(UDID);
    	Log.d(TAG, udid);
    	assertTrue("expected udid starts with android-id- but got " + udid, udid.startsWith("android-id-") || udid.startsWith("android-emu-"));
    }
    
    @When("I get the device mac address")
    @And("I get the device mac address")
    public void getMacAddress() {
    	String mac_address = DeviceInfo.getMacAddress();
    	Log.d(TAG, mac_address);
    	getBlockRepo().put(MAC_ADDRESS, mac_address);
    }
    
    @Then("the device mac address should be a well formed mac address")
    @And("the device mac address should be a well formed mac address")
    public void verifyMacAddress() {
    	String mad_address;
    	mad_address = (String) getBlockRepo().get(MAC_ADDRESS);
    	Log.d(TAG, mad_address);
    	assertTrue("expected mac address to be 12 character long but got " + mad_address, ((mad_address == "ffffffffffff") || (mad_address.length() == 12)));
    }
    
    @When("I update the device uuid")
    @And("I update the device uuid")
    public void updateUuid() {
    	notifyStepWait();
    	DeviceInfo.updateUuid(new OnResponseCallback<String>() {
			
			@Override
			public void onSuccess(int responseCode, HeaderIterator headers,
					String response) {
				notifyStepPass();
			}
			
			@Override
			public void onFailure(int responseCode, HeaderIterator headers,
					String response) {
				fail("update uuid failed");
				notifyStepPass();
			}
		});
    }

    @When("I get the device uuid")
    @And("I get the device uuid")
    public void getUuid() {
    	String uuid = DeviceInfo.getUuid();
    	Log.d("uuid", (null == uuid) ? "null" : uuid);
    	getBlockRepo().put(UUID, uuid);
    }
    
    @Then("the device uuid should starts with uuid-")
    @And("the device uuid should starts with uuid-")
    public void verifyUuid() {
    	String uuid;
    	uuid = (String) getBlockRepo().get(UUID);
    	Log.d("uuid", (null == uuid) ? "null" : uuid);
    	assertTrue("expected uuid starts with uuid- but got " + uuid, uuid.startsWith("uuid-"));
    }
    
    @When("I check the device is rooted")
    @And("I check the device is rooted")
    public void getIsRooted() {
    	boolean isRooted = DeviceInfo.isRooted();
    	Log.d(TAG, String.valueOf(isRooted));
    	getBlockRepo().put(IS_ROOTED, isRooted);
    }
    
    @When("I check the device is SendableAndroidId")
    @And("I check the device is SendableAndroidId")
    public void getIsSendableAndroidId() {
    	boolean isSendableAndroidId = DeviceInfo.isSendableAndroidId();
    	Log.d(TAG, String.valueOf(isSendableAndroidId));
    	getBlockRepo().put(IS_SENDABLEANDROIDID, isSendableAndroidId);
    }
    
    @When("I check the device is SendableMacAddress")
    @And("I check the device is SendableMacAddress")
    public void getIsSendableMacAddress() {
    	boolean isSendableMacAddress = DeviceInfo.isSendableMacAddress();
    	Log.d(TAG, String.valueOf(isSendableMacAddress));
    	getBlockRepo().put(IS_SENDABLEMACADDRESS, isSendableMacAddress);
    }
    
    @Then("the device is rooted should be (\\w+)")
    @And("the device is rooted should be (\\w+)")
    public void VerifyIsRooted(String shouldBeRooted) {
    	boolean shouldBeRootedFlag = Boolean.valueOf(shouldBeRooted);
    	boolean isRooted = (Boolean) getBlockRepo().get(IS_ROOTED);
    	Log.d(TAG, String.valueOf(isRooted));
    	assertTrue("isRooted should be " + shouldBeRooted, shouldBeRootedFlag == isRooted);
    }
    
    @Then("the device isSendableAndroidId should be (\\w+)")
    @And("the device isSendableAndroidId should be (\\w+)")
    public void VerifyIsSendableAndroidId(String shouldBeSendableAndroidId) {
    	boolean shouldBeSendableAndroidIdFlag = Boolean.valueOf(shouldBeSendableAndroidId);
    	boolean isSendableAndroidId = (Boolean) getBlockRepo().get(IS_SENDABLEANDROIDID);
    	Log.d(TAG, String.valueOf(isSendableAndroidId));
    	assertTrue("isSendableAndroidId should be " + shouldBeSendableAndroidId, shouldBeSendableAndroidIdFlag == isSendableAndroidId);
    }
    
    @Then("the device isSendableMacAddress should be (\\w+)")
    @And("the device isSendableMacAddress should be (\\w+)")
    public void VerifyIsSendableMacAddress(String shouldBeSendableMacAddress) {
    	boolean shouldBeSendableMacAddressFlag = Boolean.valueOf(shouldBeSendableMacAddress);
    	boolean isSendableMacAddress = (Boolean) getBlockRepo().get(IS_SENDABLEMACADDRESS);
    	Log.d(TAG, String.valueOf(isSendableMacAddress));
    	assertTrue("isSendableAndroidId should be " + shouldBeSendableMacAddress, shouldBeSendableMacAddressFlag == isSendableMacAddress);
    }
}
