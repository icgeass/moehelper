package com.zeroq6.moehelper.test.help;


/**
 * @author
 * @date 2018/8/15
 */
public class CloseUtils {


    public static void closeSilent(AutoCloseable autoCloseable) {
        try {
            if (null != autoCloseable) {
                autoCloseable.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
