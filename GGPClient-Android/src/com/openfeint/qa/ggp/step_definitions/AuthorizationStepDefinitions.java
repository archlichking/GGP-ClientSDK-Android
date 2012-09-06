
package com.openfeint.qa.ggp.step_definitions;

import static junit.framework.Assert.assertEquals;

import java.lang.reflect.Field;

import net.gree.asdk.api.auth.Authorizer;
import net.gree.asdk.api.auth.Authorizer.AuthorizeListener;
import net.gree.asdk.api.auth.Authorizer.LogoutListener;
import net.gree.asdk.core.Injector;
import net.gree.asdk.core.auth.AuthorizerCore;
import net.gree.asdk.core.auth.IAuthorizer;
import net.gree.asdk.core.auth.OAuthStorage;
import net.gree.oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.util.Log;
import android.view.KeyEvent;

import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.ggp.MainActivity;

public class AuthorizationStepDefinitions extends BasicStepDefinition {

    private final static String TAG = "authorization_step";

    private final static String ORIGINAL_TOKEN = "originalToken";

    private final static String ORIGINAL_SECRET = "originalSecret";

    @And("I replace my token with invalid value")
    public void setInvalidToken() {
        storeOriginalTokenAndSecret();
        setTokenAndSecret("invalidToken", "invalidSecret");
    }

    private void storeOriginalTokenAndSecret() {
        AuthorizerCore core = (AuthorizerCore) Injector.getInstance(IAuthorizer.class);
        try {
            // get mOAuthStorage field of AuthorizerCore
            Field mOAuthStorage_field = core.getClass().getDeclaredField("mOAuthStorage");
            mOAuthStorage_field.setAccessible(true);
            // set user_id, token & secret
            OAuthStorage oAuth_storage = (OAuthStorage) mOAuthStorage_field.get(core);

            getBlockRepo().put(ORIGINAL_TOKEN, oAuth_storage.getToken());
            getBlockRepo().put(ORIGINAL_SECRET, oAuth_storage.getSecret());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTokenAndSecret(String token, String secret) {
        AuthorizerCore core = (AuthorizerCore) Injector.getInstance(IAuthorizer.class);
        try {
            // get mOAuth field of AuthorizerCore
            Field oAuth_field = core.getClass().getDeclaredField("mOAuth");
            oAuth_field.setAccessible(true);
            // get OAuth class
            Class<?> OAuthClass = Class.forName("net.gree.asdk.core.auth.OAuth");
            // get consumer by invoke getConsumer method of OAuth class
            final CommonsHttpOAuthConsumer consumer = (CommonsHttpOAuthConsumer) OAuthClass
                    .getMethod("getConsumer").invoke(oAuth_field.get(core));
            // Set token & secret
            consumer.setTokenWithSecret(token, secret);

            // get mOAuthStorage field of AuthorizerCore
            Field mOAuthStorage_field = core.getClass().getDeclaredField("mOAuthStorage");
            mOAuthStorage_field.setAccessible(true);
            // set user_id, token & secret
            OAuthStorage oAuth_storage = (OAuthStorage) mOAuthStorage_field.get(core);
            oAuth_storage.setToken(token);
            oAuth_storage.setSecret(secret);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @When("I do a reauthorization")
    public void doAuthorization() {
        AuthorizeListener listener = new AuthorizeListener() {
            public void onAuthorized() {
                Log.i(TAG, "Login Success!");
            }

            public void onCancel() {
                Log.i(TAG, "Login cancel!");
            }

            public void onError() {
                Log.e(TAG, "Login failed!");
            }
        };

        // Login for ggp
        Authorizer.authorize(MainActivity.getInstance(), listener);
    }

    @Then("authorization failed confirm popup should display well")
    public void verifyAuthorizationViewPopup() {
        verifyCurrentActivity("net.gree.asdk.core.auth.SetupActivity");
    }

    private void verifyCurrentActivity(String expectActivityName) {
        // wait Authorization view to popup
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ActivityManager am = (ActivityManager) MainActivity.getInstance().getSystemService(
                Application.ACTIVITY_SERVICE);
        ComponentName componentName = am.getRunningTasks(1).get(0).topActivity;
        assertEquals("SetupActivity launched", expectActivityName, componentName.getClassName());
    }

    @After("I recover my token with correct value")
    public void recoverTokenAndSecret() {
        String oriToken = (String) getBlockRepo().get(ORIGINAL_TOKEN);
        String oriSecret = (String) getBlockRepo().get(ORIGINAL_SECRET);
        setTokenAndSecret(oriToken, oriSecret);
    }

    @After("I dismiss authorization popup")
    @And("I dismiss authorization popup")
    public void dismissAuthorizationPopup() {
        // wait Authorization view to load
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Instrumentation inst = new Instrumentation();
        // click back button to close view
        inst.sendCharacterSync(KeyEvent.KEYCODE_BACK);
    }

    @And("I tend to logout")
    public void logoutCurrentUser() {
        AuthorizeListener loginListener = new AuthorizeListener() {
            public void onAuthorized() {
                Log.i(TAG, "Login Success!");
            }

            public void onCancel() {
                Log.i(TAG, "Login cancel!");
            }

            public void onError() {
                Log.e(TAG, "Login failed!");
            }
        };

        LogoutListener logoutListener = new LogoutListener() {
            @Override
            public void onLogout() {
                Log.i(TAG, "Logout Success!");
            }

            @Override
            public void onError() {
                Log.i(TAG, "Logout cancel!");
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "Logout cancel!");
            }
        };
        if (Authorizer.isAuthorized()) {
            Authorizer.logout(MainActivity.getInstance(), logoutListener, loginListener);
        } else {
            Log.e(TAG, "already logout status!");
        }
    }

    @Then("logout confirm popup should display well")
    public void verifyLogoutViewPopup() {
        verifyCurrentActivity("net.gree.asdk.core.auth.SetupActivity");
    }
}
