
package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;

import com.openfeint.qa.core.command.When;
import com.openfeint.qa.ggp.R;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.core.ui.PopupDialog;
import net.gree.asdk.core.ui.WebViewPopupDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebView;

public class PopupUtil {
    private final String TAG = "Popup_Util"; 

    public static WebView getWebViewFromPopup(PopupDialog popup) throws Exception {
        Method getWebViewClientMethod = WebViewPopupDialog.class.getDeclaredMethod("getWebView");
        getWebViewClientMethod.setAccessible(true);
        return (WebView) getWebViewClientMethod.invoke(popup);
    }

    // TODO just for debug ignore this
    @When("I debug picture comparison")
    public void screenshotComparison() {
        String sdcard_path = Environment.getExternalStorageDirectory().getAbsolutePath();
        Bitmap image_from_laptop = zoomBitmap(BitmapFactory.decodeResource(GreePlatform
                .getContext().getResources(), R.drawable.expect_request_dialog), 408, 580);
        File file_from_device = new File(sdcard_path + "/expect_request_dialog.png");
        double sRate = compareImage(image_from_laptop, getBitmapFromFile(file_from_device));
        Log.e(TAG, "sRate: " + sRate);
    }

    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        return newbmp;
    }

    private Bitmap getBitmapFromFile(File file) {
        FileInputStream fis = null;
        Bitmap bm = null;
        try {
            fis = new FileInputStream(file);
            bm = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        return bm;
    }

    private static Object[][] getRGBList(Bitmap bm) {

        if (bm == null) {
            Log.e("Image Compare", "Bitmap is null!");
            return null;
        }

        int width = bm.getWidth();
        int height = bm.getHeight();
        Object[][] list = new Object[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int color = bm.getPixel(i, j);
                int rgb[] = new int[3];
                rgb[0] = (color & 0xff);
                rgb[1] = (color & 0xff00) >> 8;
                rgb[2] = (color & 0xff0000) >> 16;

                list[i][j] = rgb;
            }
        }
        return list;
    }

    /**
     * return similarity as a integer rate between 0~100
     * 
     * @param img1 the image to be compare
     * @param img2 the image to be compare
     * @return the similarity rate of the comparison, if it bigger than 80, we
     *         can say the view show is what we want
     */
    public static double compareImage(Bitmap img1, Bitmap img2) {
        Object[][] list1 = getRGBList(img1);
        Object[][] list2 = getRGBList(img2);

        if (list1 == null || list2 == null) {
            return 0;
        }

        double xiangsi = 0;
        double busi = 0;
        double sRate = 0;
        for (int i = 0; i < list1.length; i++) {
            for (int j = 0; j < list1[i].length; j++) {
                try {
                    int[] value1 = (int[]) list1[i][j];
                    int[] value2 = (int[]) list2[i][j];
                    boolean flag = true;
                    for (int k = 0; k < value1.length && k < value2.length; k++) {
                        if (Math.abs(value1[k] - value2[k]) > 16) {
                            flag = false;
                            busi++;
                            break;
                        }
                    }
                    if (flag) {
                        xiangsi++;
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    continue;
                }
                j++;
            }
            i++;
        }
        sRate = xiangsi / (xiangsi + busi) * 100;
        return sRate;
    }
}
