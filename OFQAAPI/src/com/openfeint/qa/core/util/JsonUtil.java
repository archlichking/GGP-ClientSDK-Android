
package com.openfeint.qa.core.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;

public class JsonUtil {
    
    private JSONObject configObject;
    
    public JsonUtil(BufferedReader json) {
        try {
            configObject = new JSONObject(json.readLine()).getJSONObject("auto_config");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public String getJsonValueByKey(String key) {
        String value = "";
        try {
            value = configObject.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return value;
    }
}
