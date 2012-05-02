
package com.openfeint.qa.core.net;

import com.openfeint.qa.core.caze.TestCase;
import com.openfeint.qa.core.exception.TCMIsnotReachableException;
import com.openfeint.qa.core.util.StringUtil;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/***
 * @author thunderzhulei
 * @category
 
 
 */
public class TCMCommunicator extends NetCommunicator {
    private static final String TCM_CASE_URL = "case_url";

    private static final String TCM_RESULT_URL = "result_url";

    private static final String TCM_KEY = "key";

    public TCMCommunicator(String type_raw, String packageName) {
        super(type_raw, packageName);
    }

    public BufferedReader getTCMResponse(String suite_id) throws TCMIsnotReachableException {
        String url = net_prop.getProperty(TCM_CASE_URL) + suite_id + "&key="
                + net_prop.getProperty(TCM_KEY);
        try {
            HttpResponse response = this.pullGet(this.buildHttpGet(url));
            return new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        } catch (Exception e) {
            Log.e(StringUtil.DEBUG_TAG, e.getMessage());
            throw new TCMIsnotReachableException("TCM is not reachable, please try again");
        }
    }

    public void setTestCaseResult(String run_id, TestCase tc) {
        String url = net_prop.getProperty(TCM_RESULT_URL) + run_id + "/" + tc.getId() + "&key="
                + net_prop.getProperty(TCM_KEY);

        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("status_id", String.valueOf(tc.getResult())));
        list.add(new BasicNameValuePair("comment", tc.getResultComment()));
        this.pushPost(this.buildHttpPost(url, list));
        System.out.println("post " + tc.getId());
    }

    public void setTestCasesResult(String run_id, Collection<TestCase> tcs) {
        for (TestCase tc : tcs) {
            if (tc.isExecuted()) {
                this.setTestCaseResult(run_id, tc);
            }
        }
    }

    private HttpPost buildHttpPost(String postUrl, List<NameValuePair> pairList) {
        HttpPost post = new HttpPost(postUrl);
        try {
            post.setEntity(new UrlEncodedFormEntity(pairList));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return post;
    }

    private HttpGet buildHttpGet(String getUrl) {
        return new HttpGet(getUrl);
    }

}
