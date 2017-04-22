package com.zeroq6.moehelper.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * 自定义基础日志类
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class Logger {

    private final static String LOG_LEVEL_DEBUG = "DEBUG";
    private final static String LOG_LEVEL_INFO = "INFO";
    private final static String LOG_LEVEL_WARN = "WARN";
    private final static String LOG_LEVEL_ERROR = "ERROR";
    private final static String LOG_LEVEL_FATAL = "FATAL";

    private static Map<String, Integer> logLevelMap = new HashMap<String, Integer>();
    private static String defaultLogLevel = LOG_LEVEL_DEBUG;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

    private Logger() {
    }

    static {
        try {
            logLevelMap.put(LOG_LEVEL_FATAL, 4);
            logLevelMap.put(LOG_LEVEL_ERROR, 3);
            logLevelMap.put(LOG_LEVEL_WARN, 2);
            logLevelMap.put(LOG_LEVEL_INFO, 1);
            logLevelMap.put(LOG_LEVEL_DEBUG, 0);
            long offset = TimeZone.getDefault().getRawOffset();
            String tzStr = "GMT" + String.format("%s%02d:%02d", offset >= 0 ? "+" : "-", offset / 3600000, (offset / 60000) % 60);
            info("Use time zone " + tzStr);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    // 使用logLevel格式控制, 用于stderr.txt
    private static void formattedLog(String msg, String level) {
        if (logLevelMap.get(level) == null) {
            throw new RuntimeException("Unknown log level.");
        }
        if (logLevelMap.get(level) < logLevelMap.get(defaultLogLevel)) {
            return;
        }
        System.err.println(sdf.format(Calendar.getInstance().getTime()) + " - " + level + " - " + msg);
    }

    // 标准输出仅添加日期, 不使用logLevel格式控制, 用于stdout.txt
    public static void stdOut(String stdout) {
        System.out.println(sdf.format(Calendar.getInstance().getTime()) + " " + stdout);
    }

    public static void debug(String debug) {
        formattedLog(debug, LOG_LEVEL_DEBUG);
    }

    public static void info(String info) {
        formattedLog(info, LOG_LEVEL_INFO);
    }

    public static void warn(String warn) {
        formattedLog(warn, LOG_LEVEL_WARN);
    }

    public static void error(String error) {
        formattedLog(error, LOG_LEVEL_ERROR);
    }

    public static synchronized void fatal(String fatal, Exception... exceptions) {
        try {
            StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
            String msg = fatal + " in class " + stacks[2].getClassName() + "." + stacks[2].getMethodName() + " at line " + stacks[2].getLineNumber();
            formattedLog(msg, LOG_LEVEL_FATAL);
            for (Exception e : exceptions) {
                e.printStackTrace();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            System.exit(-1);
        }
    }

    public static void stdLine() {
        System.out.println();
    }

    public static void errLine() {
        System.err.println();
    }
}
