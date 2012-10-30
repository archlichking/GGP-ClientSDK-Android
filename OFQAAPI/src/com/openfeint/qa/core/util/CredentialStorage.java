
package com.openfeint.qa.core.util;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class CredentialStorage {

    private HashMap<String, HashMap<String, String>> userCredentials = new HashMap<String, HashMap<String, String>>();

    private HashMap<String, String> emailIdPairs = new HashMap<String, String>();

    private static String current_appid;

    public static final String KEY_EMAIL = "username";

    public static final String KEY_PWD = "password";

    public static final String KEY_USERID = "userid";

    public static final String KEY_TOKEN = "oauthKey";

    public static final String KEY_SECRET = "oauthSecret";

    private static CredentialStorage sInstance;

    private CredentialStorage() {

    }

    public static synchronized CredentialStorage initCredentialStorageWithAppId(String app_id,
            String data) {
        if (sInstance == null) {
            sInstance = new CredentialStorage();
        }

        if (current_appid == null || !current_appid.equals(app_id)) {
            storeCredentialByData(data);
            current_appid = app_id;
        }
        return sInstance;
    }

    public static synchronized CredentialStorage getInstance() {
        if (sInstance == null) {
            throw new RuntimeException("Not initialized CredentialStorage!");
        }
        return sInstance;
    }

    private static void storeCredentialByData(String data) {
        try {
            JSONObject json = new JSONObject(data);
            JSONArray items = json.getJSONArray("credentials");
            for (int i = 0; i < items.length(); i++) {
                JSONObject tmp_credential = items.getJSONObject(i);
                String key = tmp_credential.getString(KEY_EMAIL) + "&"
                        + tmp_credential.getString(KEY_PWD);
                HashMap<String, String> value = new HashMap<String, String>();
                value.put(KEY_USERID, tmp_credential.getString(KEY_USERID));
                value.put(KEY_TOKEN, tmp_credential.getString(KEY_TOKEN));
                value.put(KEY_SECRET, tmp_credential.getString(KEY_SECRET));
                sInstance.userCredentials.put(key, value);
                sInstance.emailIdPairs.put(tmp_credential.getString(KEY_EMAIL),
                        tmp_credential.getString(KEY_USERID));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> getCredentialByKey(String Key) {
        return userCredentials.get(Key);
    }

    public HashMap<String, String> getEmailIdPairs() {
        return emailIdPairs;
    }

}
