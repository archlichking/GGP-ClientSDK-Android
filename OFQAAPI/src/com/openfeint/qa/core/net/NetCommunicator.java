
package com.openfeint.qa.core.net;

import android.util.Log;

import com.openfeint.qa.core.net.ssl.QASSLSocketFactory;
import com.openfeint.qa.core.util.StringUtil;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.security.KeyStore;
import java.util.Properties;

public abstract class NetCommunicator {
    protected Properties net_prop = new Properties();

    protected HttpClient httpClient = buildHttpClient();

    protected HttpClient buildHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new QASSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 1000 * 30);
            params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1000 * 30);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            Log.e("NetCommunicator",
                    "create http client with params failed and create with default params!");
            return new DefaultHttpClient();
        }
    }

    public NetCommunicator(String type_raw, String step_raw) {
        net_prop = StringUtil.buildProperties(type_raw);
    }

    protected void pushPost(HttpPost post) {
        try {
            httpClient.execute(post).getEntity().consumeContent();
            // need to consume whole entity, or there will be warnings
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
