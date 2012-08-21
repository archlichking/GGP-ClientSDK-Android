
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;

import java.security.Key;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.GreePlatform.BadgeListener;
import net.gree.asdk.core.codec.Base64;
import net.gree.asdk.core.util.Util;
import util.Consts;
import android.util.Log;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.ggp.R;

public class GreePlatformStepDefinitions extends BasicStepDefinition {
    private final String TAG = "GreePlatform_steps";

    private final String UPDATE_RESULT = "update_result";

    private final String SDK_BUILD = "sdk_build";

    private final String SDK_VERSION = "sdk_version";

    @When("I update badge value to latest one")
    public void updateBadgeCount() {
        notifyStepWait();
        getBlockRepo().put(UPDATE_RESULT, Consts.UNKNOWN);
        GreePlatform.updateBadgeValues(new BadgeListener() {
            @Override
            public void onBadgeCountUpdated(int newCount) {
                Log.d(TAG, "update badge count success!");
                getBlockRepo().put(UPDATE_RESULT, Consts.SUCCESS);
                notifyStepPass();
            }
        });
    }

    @When("I check basic platform info")
    public void checkPlatformInfo() {
        getBlockRepo().put(SDK_BUILD, GreePlatform.getSdkBuild());
        getBlockRepo().put(SDK_VERSION, GreePlatform.getSdkVersion());
    }

    // TODO update social badge value is not supported in android SDK now
    @Then("my social badge value should be (\\d+)")
    public void verifySocialBadgeCount(int count) {
    }

    @Then("my in game badge value should be (\\d+)")
    public void verifyInGameBadgeCount(int count) {
        int return_count = GreePlatform.getBadgeValues();
        assertEquals("update badge count success", Consts.SUCCESS, getBlockRepo()
                .get(UPDATE_RESULT));
        assertEquals("badge count", count, return_count);
    }

    @Then("get (\\w+) from GreePlatform options should be (.+)")
    @And("get (\\w+) from GreePlatform options should be (.+)")
    public void verifySpecificSetting(String key, String value) {
        assertEquals("get " + key + " from options", value, GreePlatform.getOption(key).trim());
    }

    @Then("get resource id of (.+) should be (\\d+)")
    public void verifyResourceId(String resource_name, int expect_id) {
        assertEquals("id of resource", expect_id,
                GreePlatform.getResource(GreePlatform.getContext(), resource_name));
    }

    @Then("get resource name of id (\\d+) should be (.+)")
    public void verifyResourceName(int resource_id, String expect_name) {
        assertEquals("name of resource", expect_name, GreePlatform.getRString(resource_id));
    }

    @Then("my sdk build should be (.+)")
    public void verifySdkBuild(String sdk_build) {
        assertEquals(sdk_build, getBlockRepo().get(SDK_BUILD));
    }

    @Then("my sdk version should be (.+)")
    public void verifySdkVersion(String sdk_version) {
        assertEquals(sdk_version, getBlockRepo().get(SDK_VERSION));
    }

    @When("I initialize settings with appId (\\w+) unencrypted consumerKey (.+) and consumerSecret (.+)")
    public void initWithUnencryptedKeyAndSecret(String appId, String key, String secret) {
        GreePlatform.initializeWithUnencryptedConsumerKeyAndSecret(GreePlatform.getContext(),
                appId, key, secret, null, true);
    }

    @When("I initialize settings with appId (\\w+) encrypted consumerKey (.+) and consumerSecret (.+)")
    public void initWithEncryptedKeyAndSecret(String appId, String key, String secret) {
        GreePlatform.initialize(GreePlatform.getContext(), appId, key, secret, null, true);
    }

    @After("I initialize with test app settings")
    public void initWithTestAppSettings() {
        GreePlatform.initialize(GreePlatform.getContext(), R.xml.gree_platform_configuration, null);
        getAllSettings();
    }
    
    @When("I initialize settings with customized json file (.+)")
    public void initCustomizedSettings(String path) {
        GreePlatform.initialize(GreePlatform.getContext(), R.xml.gree_platform_configuration, path);
        getAllSettings();
    }
    
    @After("I remove customized setting by key (\\w+)")
    public void clearCustomizedSettingByKey(String key) {
        GreePlatform.setOption(key, "");
        getAllSettings();
    }

    // TODO For debug
    @Then("I want to see all settings")
    public void getAllSettings() {
        Map<String, Object> map = GreePlatform.getOptions();
        for (String key : map.keySet()) {
            Log.i(TAG, "key: " + key + ", value: " + map.get(key));
        }
    }

    // For data preparation, get encrypted string
    @When("I get encrypted string of consumerKey (\\w+) and consumerSecret (\\w+)")
    public void getEncryptedKeyAndSecret(String key, String secret) {
        String encryptedKey = getEncryptedString(key);
        String encryptedSecret = getEncryptedString(secret);
        Log.e(TAG, "encrypted key " + key + " : " + encryptedKey);
        Log.e(TAG, "encrypted secret " + secret + " : " + encryptedSecret);
    }

    private String getEncryptedString(String src) {
        String encryptedText = "";
        try {
            byte[] key = Util.getScrambleDigest(GreePlatform.getContext());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivspec = new IvParameterSpec(key);
            Key skey = new SecretKeySpec(key, "AES");

            cipher.init(Cipher.ENCRYPT_MODE, skey, ivspec);
            byte[] srcByte = cipher.doFinal(src.getBytes("UTF8"));
            encryptedText = Base64.encodeBytes(srcByte, Base64.NO_OPTIONS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedText;
    }
}
