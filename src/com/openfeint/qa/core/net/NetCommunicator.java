
package com.openfeint.qa.core.net;

import java.io.IOException;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import com.openfeint.qa.core.util.StringUtil;

public abstract class NetCommunicator {
    protected Properties net_prop = new Properties();

    protected HttpClient httpClient = new DefaultHttpClient();

    public NetCommunicator(String type_raw, String step_raw) {
        net_prop = StringUtil.buildProperties(type_raw);
    }

    protected void pushPost(HttpPost post) {
        try {
            httpClient.execute(post);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected HttpResponse pullGet(HttpGet get) {
        HttpResponse response = null;
        try {
            response = httpClient.execute(get);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
