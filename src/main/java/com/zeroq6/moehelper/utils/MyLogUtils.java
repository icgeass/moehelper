package com.zeroq6.moehelper.utils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 自定义基础日志类
 *
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class MyLogUtils {

    private final static String LOG_LEVEL_DEBUG = "DEBUG";
    private final static String LOG_LEVEL_INFO = "INFO";
    private final static String LOG_LEVEL_WARN = "WARN";
    private final static String LOG_LEVEL_ERROR = "ERROR";
    private final static String LOG_LEVEL_FATAL = "FATAL";

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private MyLogUtils() {
    }

    static {
        long offset = TimeZone.getDefault().getRawOffset();
        String tzStr = "GMT" + String.format("%s%02d:%02d", offset >= 0 ? "+" : "-", offset / 3600000, (offset / 60000) % 60);
        info("use time zone " + tzStr);
        try {
            Thread.sleep(50);
        }catch (Exception e){
            // ignore
        }
    }

    // 用于stderr.txt
    private static void formattedLog(Object msg, String level) {
        System.err.println(sdf.format(new Date()) + " - " + level + " - " + String.valueOf(msg));
    }

    // 用于stdout.txt
    public static void stdOut(Object message) {
        System.out.println(sdf.format(new Date()) + " " + String.valueOf(message));
    }

    public static void stdOut() {
        System.out.println(sdf.format(new Date()));
    }


    public static void debug(Object message) {
        formattedLog(message, LOG_LEVEL_DEBUG);
    }

    public static void info(Object message) {
        formattedLog(message, LOG_LEVEL_INFO);
    }

    public static void warn(Object message) {
        formattedLog(message, LOG_LEVEL_WARN);
    }

    public static void error(Object message) {
        formattedLog(message, LOG_LEVEL_ERROR);
    }

    public static synchronized void fatal(Object message, Exception... exceptions) {
        try {
            StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
            String msg = message + " in class " + stacks[2].getClassName() + "." + stacks[2].getMethodName() + " at line " + stacks[2].getLineNumber();
            formattedLog(msg, LOG_LEVEL_FATAL);
            if (null != exceptions && exceptions.length > 0) {
                exceptions[0].printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(-2);
        }
    }
}
