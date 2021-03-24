package util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class ClipboardUtil {
    public static boolean copyToClipboard(String string) {
        try {
            StringSelection stringSelection = new StringSelection(string);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
