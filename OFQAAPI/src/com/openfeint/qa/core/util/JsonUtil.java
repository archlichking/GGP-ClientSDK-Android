
package com.openfeint.qa.core.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;

public class JsonUtil {

    public static String getAutoConfigJsonValueByKey(String key, BufferedReader json) {
        String value = "";
        try {
            JSONObject tempJsonObject = new JSONObject(json.readLine());
            JSONObject configObject = tempJsonObject.getJSONObject("auto_config");
            value = configObject.getString(key);
            json.reset();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value;
    }
    
}


