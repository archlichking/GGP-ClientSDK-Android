
package util;

import static junit.framework.Assert.fail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import android.util.Log;

public class FileUtil {
    private final static String TAG = "File_Util";
    
    public static String getTextFromFile(String filePath) {
        FileInputStream fis = null;
        FileChannel fc = null;
        CharBuffer cbuf = null;
        try {
            fis = new FileInputStream(filePath);
            fc = fis.getChannel();

            // Create a read-only CharBuffer on the file
            ByteBuffer bbuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, (int) fc.size());
            cbuf = Charset.forName("UTF-8").newDecoder().decode(bbuf);
        } catch (FileNotFoundException e) {
            fail("failed to open the debug file");
        } catch (IOException e) {
            fail("failed to read the debug file");
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (fc != null) {
                    fc.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException when clean up");
            }
        }
        return cbuf.toString();
    }
}
