
package util;

import com.google.common.io.ByteStreams;
import com.openfeint.qa.core.util.StringUtil;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public class RawFileUtil {
    private static RawFileUtil rfu = null;

    private Context context;

    private RawFileUtil(Context context) {
        this.context = context;
    }

    public static RawFileUtil getInstance(Context context) {
        if (rfu == null) {
            rfu = new RawFileUtil(context);
        }
        return rfu;
    }

    public static RawFileUtil getInstance() {
        if (rfu != null) {
            return rfu;
        }
        return null;
    }

    public String getTextFromRawResource(int id) {
        InputStream is = context.getResources().openRawResource(id);
        String r = null;
        try {
            r = new String(ByteStreams.toByteArray(is));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return r;
    }

    public String getTextFromRawResource(int id, String properties) {
        return StringUtil.buildProperties(getTextFromRawResource(id)).getProperty(properties);
    }
}
