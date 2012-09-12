
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import net.gree.asdk.core.request.OnResponseCallback;
import net.gree.asdk.core.util.DeviceInfo;

import org.apache.http.HeaderIterator;

import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;

public class DeviceInfoStepDefinitions extends BasicStepDefinition {
    private static final String TAG = DeviceInfoStepDefinitions.class.getSimpleName();
    
    @When("I get the DeviceInfo")
    @And("I get the DeviceInfo")
    public void getDeviceInfo() {
    }
    
    @Then("the device udid should starts with android-id-")
    @And("the device udid should starts with android-id-")
    public void verifyUdid() {
    	Log.d(TAG, DeviceInfo.getUdid());
    	assertTrue("expected udid starts with android-id- but got " + DeviceInfo.getUdid(), DeviceInfo.getUdid().startsWith("android-id-") || DeviceInfo.getUdid().startsWith("android-emu-"));
    }
    
    @Then("the device mac address should be a well formed mac address")
    @And("the device mac address should be a well formed mac address")
    public void verifyMacAddress() {
//    	String mad_address;
//    	mad_address = (String) getBlockRepo().get(MAC_ADDRESS);
    	Log.d(TAG, DeviceInfo.getMacAddress());
    	assertTrue("expected mac address to be 12 character long but got " + DeviceInfo.getMacAddress(), ((DeviceInfo.getMacAddress() == "ffffffffffff") || (DeviceInfo.getMacAddress().length() == 12)));
    }
    
    @Given("I update the device uuid")
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
    
    @Then("the device uuid should starts with uuid-")
    @And("the device uuid should starts with uuid-")
    public void verifyUuid() {
    	Log.d(TAG, (null == DeviceInfo.getUuid()) ? "null" : DeviceInfo.getUuid());
    	assertTrue("expected uuid starts with uuid- but got " + DeviceInfo.getUuid(), DeviceInfo.getUuid().startsWith("uuid-"));
    }
    
    @Then("the device is rooted should be (\\w+)")
    @And("the device is rooted should be (\\w+)")
    public void VerifyIsRooted(boolean shouldBeRooted) {
    	Log.d(TAG, String.valueOf(DeviceInfo.isRooted()));
    	assertTrue("isRooted should be " + shouldBeRooted, shouldBeRooted == DeviceInfo.isRooted());
    }
    
    @Then("the device isSendableAndroidId should be (\\w+)")
    @And("the device isSendableAndroidId should be (\\w+)")
    public void VerifyIsSendableAndroidId(boolean shouldBeSendableAndroidId) {
    	Log.d(TAG, String.valueOf(DeviceInfo.isSendableAndroidId()));
    	assertTrue("isSendableAndroidId should be " + shouldBeSendableAndroidId, shouldBeSendableAndroidId == DeviceInfo.isSendableAndroidId());
    }
    
    @Then("the device isSendableMacAddress should be (\\w+)")
    @And("the device isSendableMacAddress should be (\\w+)")
    public void VerifyIsSendableMacAddress(boolean shouldBeSendableMacAddress) {
    	Log.d(TAG, String.valueOf(DeviceInfo.isSendableMacAddress()));
    	assertTrue("isSendableAndroidId should be " + shouldBeSendableMacAddress, shouldBeSendableMacAddress == DeviceInfo.isSendableMacAddress());
    }
}
