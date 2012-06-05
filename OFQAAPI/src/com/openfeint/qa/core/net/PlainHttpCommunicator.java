package com.openfeint.qa.core.net;

import com.openfeint.qa.core.exception.TCMIsnotReachableException;
import com.openfeint.qa.core.util.StringUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PlainHttpCommunicator extends NetCommunicator {

    public PlainHttpCommunicator(String type_raw, String step_raw) {
        super(type_raw, step_raw);
        // TODO Auto-generated constructor stub
    }
    
    private HttpGet buildHttpGet(String getUrl) {
        return new HttpGet(getUrl);
    }
    
    public BufferedReader getJsonResponse(String url) throws TCMIsnotReachableException {
          try {
            HttpResponse response = this.pullGet(this.buildHttpGet(url));
            return new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        } catch (Exception e) {
            return null;
        }
    }

}
